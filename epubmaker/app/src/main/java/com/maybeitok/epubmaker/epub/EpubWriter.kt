package com.maybeitok.epubmaker.epub

import com.maybeitok.epubmaker.model.CustomFont
import com.maybeitok.epubmaker.model.EpubChapter
import com.maybeitok.epubmaker.model.EpubDocument
import com.maybeitok.epubmaker.model.ImageData
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object EpubWriter {

    fun write(document: EpubDocument, file: File) {
        file.parentFile?.mkdirs()
        write(document, FileOutputStream(file))
    }

    fun write(document: EpubDocument, outputStream: OutputStream) {
        ZipOutputStream(outputStream.buffered()).use { zos ->
            // mimetype MUST be first, STORED (uncompressed), no extra fields
            writeMimetype(zos)

            // META-INF/container.xml
            writeText(zos, "META-INF/container.xml", buildContainerXml())

            // Collect all images across chapters
            val allImages = collectImages(document)

            // OEBPS/content.opf
            writeText(zos, "OEBPS/content.opf", buildOpf(document, allImages))

            // OEBPS/nav.xhtml
            writeText(zos, "OEBPS/nav.xhtml", buildNavXhtml(document))

            // OEBPS/toc.ncx
            writeText(zos, "OEBPS/toc.ncx", buildTocNcx(document))

            // OEBPS/css/styles.css
            writeText(zos, "OEBPS/css/styles.css", buildStylesCss(document.customFonts))

            // Custom fonts
            for (font in document.customFonts) {
                val fontFile = File(font.filePath)
                if (fontFile.exists()) {
                    writeBytes(zos, "OEBPS/fonts/${fontFile.name}", fontFile.readBytes())
                }
            }

            // Images
            for (image in allImages) {
                val imageBytes: ByteArray = when {
                    !image.localPath.isNullOrEmpty() -> runCatching { File(image.localPath!!).readBytes() }.getOrNull()
                    image.base64.isNotEmpty() -> runCatching { android.util.Base64.decode(image.base64, android.util.Base64.DEFAULT) }.getOrNull()
                    else -> null
                } ?: continue
                writeBytes(zos, "OEBPS/images/${image.filename}", imageBytes)
            }

            // Chapters
            for ((index, chapter) in document.chapters.withIndex()) {
                val chapterNum = String.format("%03d", index + 1)
                val xhtml = buildChapterXhtml(chapter, document.customFonts)
                writeText(zos, "OEBPS/chapters/chapter_$chapterNum.xhtml", xhtml)
            }
        }
    }

    private fun writeMimetype(zos: ZipOutputStream) {
        val bytes = "application/epub+zip".toByteArray(Charsets.US_ASCII)
        val entry = ZipEntry("mimetype")
        entry.method = ZipEntry.STORED
        entry.size = bytes.size.toLong()
        entry.compressedSize = bytes.size.toLong()
        val crc = CRC32()
        crc.update(bytes)
        entry.crc = crc.value
        zos.putNextEntry(entry)
        zos.write(bytes)
        zos.closeEntry()
    }

    private fun writeText(zos: ZipOutputStream, path: String, content: String) {
        writeBytes(zos, path, content.toByteArray(Charsets.UTF_8))
    }

    private fun writeBytes(zos: ZipOutputStream, path: String, bytes: ByteArray) {
        val entry = ZipEntry(path)
        entry.method = ZipEntry.DEFLATED
        zos.putNextEntry(entry)
        zos.write(bytes)
        zos.closeEntry()
    }

    private fun buildContainerXml(): String = """<?xml version="1.0" encoding="UTF-8"?>
<container version="1.0" xmlns="urn:oasis:names:tc:opendocument:xmlns:container">
    <rootfiles>
        <rootfile full-path="OEBPS/content.opf" media-type="application/oebps-package+xml"/>
    </rootfiles>
</container>"""

    private fun buildOpf(document: EpubDocument, allImages: List<ImageData>): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        val modified = sdf.format(Date())

        val sb = StringBuilder()
        sb.appendLine("""<?xml version="1.0" encoding="UTF-8"?>
<package xmlns="http://www.idpf.org/2007/opf" version="3.0" unique-identifier="uid" xml:lang="${escapeXml(document.language)}">
    <metadata xmlns:dc="http://purl.org/dc/elements/1.1/">
        <dc:identifier id="uid">uuid:${escapeXml(document.id)}</dc:identifier>
        <dc:title>${escapeXml(document.title)}</dc:title>
        <dc:creator>${escapeXml(document.author)}</dc:creator>
        <dc:language>${escapeXml(document.language)}</dc:language>
        <meta property="dcterms:modified">$modified</meta>
    </metadata>
    <manifest>
        <item id="nav" href="nav.xhtml" media-type="application/xhtml+xml" properties="nav"/>
        <item id="ncx" href="toc.ncx" media-type="application/x-dtbncx+xml"/>
        <item id="css" href="css/styles.css" media-type="text/css"/>""")

        for ((index, chapter) in document.chapters.withIndex()) {
            val num = String.format("%03d", index + 1)
            sb.appendLine("""        <item id="chapter$num" href="chapters/chapter_$num.xhtml" media-type="application/xhtml+xml"/>""")
        }

        for ((index, image) in allImages.withIndex()) {
            val mime = escapeXml(image.mimeType)
            sb.appendLine("""        <item id="image${String.format("%03d", index + 1)}" href="images/${escapeXml(image.filename)}" media-type="$mime"/>""")
        }

        for ((index, font) in document.customFonts.withIndex()) {
            val fontFile = File(font.filePath)
            val mime = escapeXml(font.mimeType)
            sb.appendLine("""        <item id="font${String.format("%03d", index + 1)}" href="fonts/${escapeXml(fontFile.name)}" media-type="$mime"/>""")
        }

        sb.appendLine("    </manifest>")
        sb.appendLine("    <spine toc=\"ncx\">")
        for ((index, _) in document.chapters.withIndex()) {
            val num = String.format("%03d", index + 1)
            sb.appendLine("""        <itemref idref="chapter$num"/>""")
        }
        sb.appendLine("    </spine>")
        sb.append("</package>")

        return sb.toString()
    }

    private fun buildNavXhtml(document: EpubDocument): String {
        val sb = StringBuilder()
        sb.appendLine("""<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:epub="http://www.idpf.org/2007/ops" xml:lang="${escapeXml(document.language)}">
<head>
    <meta charset="UTF-8"/>
    <title>${escapeXml(document.title)}</title>
</head>
<body>
    <nav epub:type="toc" id="toc">
        <h1>목차</h1>
        <ol>""")

        for ((index, chapter) in document.chapters.withIndex()) {
            val num = String.format("%03d", index + 1)
            sb.appendLine("""            <li><a href="chapters/chapter_$num.xhtml">${escapeXml(chapter.title)}</a></li>""")
        }

        sb.appendLine("""        </ol>
    </nav>
</body>
</html>""")
        return sb.toString()
    }

    private fun buildTocNcx(document: EpubDocument): String {
        val sb = StringBuilder()
        sb.appendLine("""<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE ncx PUBLIC "-//NISO//DTD ncx 2005-1//EN" "http://www.daisy.org/z3986/2005/ncx-2005-1.dtd">
<ncx xmlns="http://www.daisy.org/z3986/2005/ncx/" version="2005-1">
    <head>
        <meta name="dtb:uid" content="uuid:${escapeXml(document.id)}"/>
        <meta name="dtb:depth" content="1"/>
        <meta name="dtb:totalPageCount" content="0"/>
        <meta name="dtb:maxPageNumber" content="0"/>
    </head>
    <docTitle><text>${escapeXml(document.title)}</text></docTitle>
    <navMap>""")

        for ((index, chapter) in document.chapters.withIndex()) {
            val num = String.format("%03d", index + 1)
            val playOrder = index + 1
            sb.appendLine("""        <navPoint id="navPoint-$playOrder" playOrder="$playOrder">
            <navLabel><text>${escapeXml(chapter.title)}</text></navLabel>
            <content src="chapters/chapter_$num.xhtml"/>
        </navPoint>""")
        }

        sb.appendLine("    </navMap>")
        sb.append("</ncx>")
        return sb.toString()
    }

    private fun buildStylesCss(fonts: List<CustomFont>): String {
        val sb = StringBuilder()
        sb.appendLine("@charset \"UTF-8\";")
        sb.appendLine()
        for (font in fonts) {
            val fontFile = File(font.filePath)
            sb.appendLine("@font-face {")
            sb.appendLine("    font-family: \"${font.name}\";")
            sb.appendLine("    src: url(\"../fonts/${fontFile.name}\");")
            sb.appendLine("}")
        }
        sb.appendLine()
        sb.appendLine("""body {
    font-family: serif;
    line-height: 1.6;
    margin: 0 auto;
    max-width: 800px;
    padding: 20px;
}

p { margin: 0 0 1em 0; }
img { max-width: 100%; height: auto; }
h1, h2, h3 { line-height: 1.3; }""")
        return sb.toString()
    }

    private fun buildChapterXhtml(chapter: EpubChapter, fonts: List<CustomFont>): String {
        val processedContent = replaceDataUrisWithRelativePaths(chapter.content, chapter.embeddedImages)
        return """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:epub="http://www.idpf.org/2007/ops" xml:lang="ko">
<head>
    <meta charset="UTF-8"/>
    <title>${escapeXml(chapter.title)}</title>
    <link rel="stylesheet" type="text/css" href="../css/styles.css"/>
</head>
<body>
$processedContent
</body>
</html>"""
    }

    private fun replaceDataUrisWithRelativePaths(html: String, images: List<ImageData>): String {
        var result = html
        for (image in images) {
            // file:// URI 교체
            if (!image.localPath.isNullOrEmpty()) {
                result = result.replace("src=\"file://${image.localPath}\"", "src=\"../images/${image.filename}\"")
                result = result.replace("src='file://${image.localPath}'", "src='../images/${image.filename}'")
            }
            // data: URI 교체
            if (image.base64.isNotEmpty()) {
                val dataUri = "data:${image.mimeType};base64,${image.base64}"
                result = result.replace(dataUri, "../images/${image.filename}")
            }
        }
        // 남은 data: URI 정규식으로 처리
        result = result.replace(Regex("""data:[^;]+;base64,[A-Za-z0-9+/=]+""")) { match ->
            val uri = match.value
            images.find { uri.startsWith("data:${it.mimeType};base64,") }
                ?.let { "../images/${it.filename}" } ?: uri
        }
        // 남은 file:// URI 정규식으로 처리
        result = result.replace(
            Regex("""src="file://[^"]+/([^/"]+\.(jpe?g|png|gif|webp|svg))"""", RegexOption.IGNORE_CASE)
        ) { match ->
            val filename = match.groupValues[1]
            images.find { it.filename.equals(filename, ignoreCase = true) }
                ?.let { "src=\"../images/${it.filename}\"" } ?: match.value
        }
        return result
    }

    private fun collectImages(document: EpubDocument): List<ImageData> {
        val allImages = mutableListOf<ImageData>()
        val seenFilenames = mutableSetOf<String>()
        for (chapter in document.chapters) {
            for (image in chapter.embeddedImages) {
                if (seenFilenames.add(image.filename)) {
                    allImages.add(image)
                }
            }
        }
        return allImages
    }

    fun escapeXml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}
