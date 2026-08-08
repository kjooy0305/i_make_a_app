package com.maybeitok.epubmaker.epub

import android.content.Context
import android.net.Uri
import com.maybeitok.epubmaker.model.CustomFont
import com.maybeitok.epubmaker.model.EpubChapter
import com.maybeitok.epubmaker.model.EpubDocument
import com.maybeitok.epubmaker.model.ImageData
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.File
import java.net.URLDecoder
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import javax.xml.parsers.DocumentBuilderFactory

object EpubParser {

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * URI에서 스트리밍 파싱 — 모든 항목을 tmpDir에 기록 후 처리 (메모리 OOM 방지)
     */
    fun parseFromUri(
        context: Context,
        uri: Uri,
        fontCacheDir: File,
        imageCacheDir: File,
        onProgress: (String) -> Unit = {}
    ): EpubDocument? {
        val sessionId = java.util.UUID.randomUUID().toString().take(8)
        val imgDir = File(imageCacheDir, sessionId).also { it.mkdirs() }
        val tmpDir = File(imageCacheDir, "tmp_$sessionId").also { it.mkdirs() }

        return try {
            onProgress("ZIP 스트리밍 중...")
            var count = 0

            context.contentResolver.openInputStream(uri)?.buffered(65536)?.use { raw ->
                ZipInputStream(raw).use { zis ->
                    var entry = zis.nextEntry
                    while (entry != null) {
                        if (!entry.isDirectory) {
                            val name = entry.name
                            if (isImagePath(name)) {
                                // Use full ZIP path as unique filename to avoid basename collision
                                val uniqueFilename = sanitizeFilename(name.replace("/", "__"))
                                File(imgDir, uniqueFilename).outputStream().use { zis.copyTo(it) }
                            } else {
                                val dest = File(tmpDir, sanitizePath(name))
                                dest.parentFile?.mkdirs()
                                dest.outputStream().use { zis.copyTo(it) }
                            }
                            count++
                            if (count % 20 == 0) onProgress("$count 항목 처리 중...")
                        }
                        zis.closeEntry()
                        entry = zis.nextEntry
                    }
                }
            } ?: return null

            parseFromTempDir(tmpDir, imgDir, fontCacheDir, null, onProgress)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            tmpDir.deleteRecursively()
        }
    }

    /**
     * 로컬 파일에서 파싱 (ZipFile 랜덤 액세스)
     */
    fun parse(
        file: File,
        fontCacheDir: File,
        imageCacheDir: File,
        onProgress: (String) -> Unit = {}
    ): EpubDocument? {
        return try {
            val sessionId = java.util.UUID.randomUUID().toString().take(8)
            val imgDir = File(imageCacheDir, sessionId).also { it.mkdirs() }
            onProgress("EPUB 구조 파싱 중...")

            ZipFile(file).use { zip ->
                val containerEntry = zip.getEntry("META-INF/container.xml") ?: return null
                val containerXml = zip.getInputStream(containerEntry).use { it.readBytes() }
                val opfPath = extractOpfPath(containerXml) ?: return null
                val opfDir = opfPath.substringBeforeLast("/", "")
                val opfEntry = findZipEntry(zip, opfPath) ?: return null
                val opfBytes = zip.getInputStream(opfEntry).use { it.readBytes() }
                parseOpfWithZip(opfBytes, opfDir, zip, imgDir, fontCacheDir, file.absolutePath, onProgress)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // ── Disk-based streaming path ─────────────────────────────────────────────

    private fun parseFromTempDir(
        tmpDir: File,
        imgDir: File,
        fontCacheDir: File,
        sourceFilePath: String?,
        onProgress: (String) -> Unit
    ): EpubDocument? {
        val containerFile = findTempFile(tmpDir, "META-INF/container.xml") ?: return null
        val opfPath = extractOpfPath(containerFile.readBytes()) ?: return null
        val opfDir = opfPath.substringBeforeLast("/", "")
        val opfFile = findTempFile(tmpDir, opfPath) ?: return null
        val opfBytes = opfFile.readBytes()

        val doc = parseXml(opfBytes)
        val title    = getTextByTagNS(doc, "title")      ?: "제목 없음"
        val author   = getTextByTagNS(doc, "creator")    ?: ""
        val language = getTextByTagNS(doc, "language")   ?: "ko"
        val uid      = getTextByTagNS(doc, "identifier") ?: java.util.UUID.randomUUID().toString()

        val manifest = buildManifest(doc, opfDir)
        val spineOrder = buildSpine(doc)

        val customFonts = mutableListOf<CustomFont>()
        fontCacheDir.mkdirs()
        for ((_, item) in manifest) {
            if (!isFontItem(item.href, item.mediaType)) continue
            val fontFile = findTempFile(tmpDir, item.fullPath) ?: continue
            val filename = item.href.substringAfterLast("/")
            val dest = File(fontCacheDir, filename).also { it.writeBytes(fontFile.readBytes()) }
            customFonts.add(CustomFont(name = filename.substringBeforeLast("."),
                filePath = dest.absolutePath, mimeType = item.mediaType.ifEmpty { "font/ttf" }))
        }

        val diskImages = buildDiskImageMap(imgDir)
        val globalImageCache = mutableMapOf<String, ImageData>()
        val chapters = mutableListOf<EpubChapter>()

        for ((idx, idref) in spineOrder.withIndex()) {
            val item = manifest[idref] ?: continue
            if (!item.mediaType.contains("xhtml") && !item.mediaType.contains("html")) continue
            val chapterFile = findTempFile(tmpDir, item.fullPath) ?: continue
            onProgress("챕터 ${idx + 1}/${spineOrder.size} 처리 중...")

            val xhtmlText = chapterFile.readText(Charsets.UTF_8)
            val chapterTitle = extractXhtmlTitle(xhtmlText)
                ?: item.href.substringAfterLast("/").substringBeforeLast(".")
            val bodyContent = extractBodyContent(xhtmlText)
            val chapterDir = item.fullPath.substringBeforeLast("/")

            val chapterImages = mutableListOf<ImageData>()
            val restoredContent = restoreImagesFromDisk(bodyContent, chapterDir, diskImages, globalImageCache, chapterImages)
            chapters.add(EpubChapter(title = chapterTitle, content = restoredContent, embeddedImages = chapterImages))
        }

        if (chapters.isEmpty()) chapters.add(EpubChapter(title = "제1장", content = ""))

        return EpubDocument(
            id = uid.removePrefix("uuid:"),
            title = title, author = author, language = language,
            chapters = chapters, customFonts = customFonts, filePath = sourceFilePath
        )
    }

    // ── ZipFile path ─────────────────────────────────────────────────────────

    private fun parseOpfWithZip(
        opfBytes: ByteArray,
        opfDir: String,
        zip: ZipFile,
        imgDir: File,
        fontCacheDir: File,
        sourceFilePath: String,
        onProgress: (String) -> Unit
    ): EpubDocument? {
        val doc = parseXml(opfBytes)
        val title    = getTextByTagNS(doc, "title")      ?: "제목 없음"
        val author   = getTextByTagNS(doc, "creator")    ?: ""
        val language = getTextByTagNS(doc, "language")   ?: "ko"
        val uid      = getTextByTagNS(doc, "identifier") ?: java.util.UUID.randomUUID().toString()

        val manifest = buildManifest(doc, opfDir)
        val spineOrder = buildSpine(doc)

        val customFonts = mutableListOf<CustomFont>()
        fontCacheDir.mkdirs()
        for ((_, item) in manifest) {
            if (!isFontItem(item.href, item.mediaType)) continue
            val entry = findZipEntry(zip, item.fullPath) ?: continue
            val bytes = zip.getInputStream(entry).use { it.readBytes() }
            val filename = item.href.substringAfterLast("/")
            val dest = File(fontCacheDir, filename).also { it.writeBytes(bytes) }
            customFonts.add(CustomFont(name = filename.substringBeforeLast("."),
                filePath = dest.absolutePath, mimeType = item.mediaType.ifEmpty { "font/ttf" }))
        }

        val globalImageCache = mutableMapOf<String, ImageData>()
        val chapters = mutableListOf<EpubChapter>()

        for ((idx, idref) in spineOrder.withIndex()) {
            val item = manifest[idref] ?: continue
            if (!item.mediaType.contains("xhtml") && !item.mediaType.contains("html")) continue
            val entry = findZipEntry(zip, item.fullPath) ?: continue
            onProgress("챕터 ${idx + 1}/${spineOrder.size} 처리 중...")

            val xhtmlText = zip.getInputStream(entry).use { it.readBytes() }.toString(Charsets.UTF_8)
            val chapterTitle = extractXhtmlTitle(xhtmlText)
                ?: item.href.substringAfterLast("/").substringBeforeLast(".")
            val bodyContent = extractBodyContent(xhtmlText)
            val chapterDir = item.fullPath.substringBeforeLast("/")

            val chapterImages = mutableListOf<ImageData>()
            val restoredContent = restoreImagesFromZip(bodyContent, chapterDir, zip, imgDir, globalImageCache, chapterImages)
            chapters.add(EpubChapter(title = chapterTitle, content = restoredContent, embeddedImages = chapterImages))
        }

        if (chapters.isEmpty()) chapters.add(EpubChapter(title = "제1장", content = ""))

        return EpubDocument(
            id = uid.removePrefix("uuid:"),
            title = title, author = author, language = language,
            chapters = chapters, customFonts = customFonts, filePath = sourceFilePath
        )
    }

    // ── Image restoration ─────────────────────────────────────────────────────

    private fun restoreImagesFromDisk(
        html: String,
        chapterDir: String,
        diskImages: Map<String, File>,
        globalCache: MutableMap<String, ImageData>,
        chapterImages: MutableList<ImageData>
    ): String {
        return Regex("""src=(["'])([^"']+)\1""").replace(html) { match ->
            val src = match.groupValues[2]
            if (src.startsWith("data:") || src.startsWith("http") || src.startsWith("file:")) return@replace match.value
            val cacheKey = resolveRelativePath("$chapterDir/$src")

            globalCache[cacheKey]?.let { img ->
                if (chapterImages.none { it.filename == img.filename }) chapterImages.add(img)
                return@replace "src=\"file://${img.localPath}\""
            }

            // Primary lookup: match by unique filename derived from full ZIP path
            val uniqueFilename = sanitizeFilename(cacheKey.replace("/", "__"))
            val basenameFilename = sanitizeFilename(decodeName(src.substringAfterLast("/")))
            val diskFile = diskImages[uniqueFilename]
                // Fallback: match any disk file ending with the same basename
                ?: diskImages.entries.firstOrNull { it.key.endsWith("__$basenameFilename", ignoreCase = true) }?.value
                ?: diskImages.entries.firstOrNull { it.key.equals(basenameFilename, ignoreCase = true) }?.value
                ?: return@replace match.value

            val mime = guessMimeType(diskFile.name)
            val img = ImageData(filename = diskFile.name, base64 = "", mimeType = mime, localPath = diskFile.absolutePath)
            globalCache[cacheKey] = img
            if (chapterImages.none { it.filename == img.filename }) chapterImages.add(img)
            "src=\"file://${diskFile.absolutePath}\""
        }
    }

    private fun restoreImagesFromZip(
        html: String,
        chapterDir: String,
        zip: ZipFile,
        imgDir: File,
        globalCache: MutableMap<String, ImageData>,
        chapterImages: MutableList<ImageData>
    ): String {
        return Regex("""src=(["'])([^"']+)\1""").replace(html) { match ->
            val src = match.groupValues[2]
            if (src.startsWith("data:") || src.startsWith("http") || src.startsWith("file:")) return@replace match.value

            val fullPath = resolveRelativePath("$chapterDir/$src")
            val filename = sanitizeFilename(decodeName(src.substringAfterLast("/")))

            globalCache[fullPath]?.let { img ->
                if (chapterImages.none { it.filename == img.filename }) chapterImages.add(img)
                return@replace "src=\"file://${img.localPath}\""
            }

            val entry = findZipEntry(zip, fullPath) ?: return@replace match.value
            if (entry.size > 0 && entry.size > 50 * 1024 * 1024) return@replace match.value

            // Use full ZIP path as unique filename to avoid basename collisions across chapters
            val uniqueFilename = sanitizeFilename(fullPath.replace("/", "__"))
            val imgFile = File(imgDir, uniqueFilename)
            try {
                zip.getInputStream(entry).use { input -> imgFile.outputStream().use { input.copyTo(it) } }
            } catch (_: Exception) { return@replace match.value }

            val mime = guessMimeType(uniqueFilename)
            val img = ImageData(filename = uniqueFilename, base64 = "", mimeType = mime, localPath = imgFile.absolutePath)
            globalCache[fullPath] = img
            if (chapterImages.none { it.filename == img.filename }) chapterImages.add(img)
            "src=\"file://${imgFile.absolutePath}\""
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private data class ManifestItem(val href: String, val fullPath: String, val mediaType: String)

    private fun buildManifest(doc: Document, opfDir: String): Map<String, ManifestItem> {
        val manifest = mutableMapOf<String, ManifestItem>()
        val nodes = doc.getElementsByTagNameNS("*", "item")
        for (i in 0 until nodes.length) {
            val item = nodes.item(i) as? Element ?: continue
            val id = item.getAttribute("id").trim()
            val href = item.getAttribute("href").trim()
            val mediaType = item.getAttribute("media-type").trim()
            if (id.isNotEmpty() && href.isNotEmpty()) {
                manifest[id] = ManifestItem(href, buildZipPath(opfDir, href), mediaType)
            }
        }
        return manifest
    }

    private fun buildSpine(doc: Document): List<String> {
        val spine = mutableListOf<String>()
        val nodes = doc.getElementsByTagNameNS("*", "itemref")
        for (i in 0 until nodes.length) {
            val idref = (nodes.item(i) as? Element)?.getAttribute("idref")?.trim() ?: continue
            if (idref.isNotEmpty()) spine.add(idref)
        }
        return spine
    }

    private fun buildDiskImageMap(imgDir: File): Map<String, File> {
        val map = mutableMapOf<String, File>()
        imgDir.walkTopDown().filter { it.isFile }.forEach { f -> map[f.name] = f }
        return map
    }

    private fun findTempFile(tmpDir: File, entryName: String): File? {
        val safe = sanitizePath(entryName)
        File(tmpDir, safe).takeIf { it.exists() }?.let { return it }
        val decoded = try { URLDecoder.decode(entryName, "UTF-8") } catch (_: Exception) { entryName }
        if (decoded != entryName) {
            File(tmpDir, sanitizePath(decoded)).takeIf { it.exists() }?.let { return it }
        }
        val lowerSafe = safe.lowercase()
        return tmpDir.walkTopDown().firstOrNull { it.isFile &&
            it.relativeTo(tmpDir).path.replace('\\', '/').lowercase() == lowerSafe
        }
    }

    private fun findZipEntry(zip: ZipFile, path: String): java.util.zip.ZipEntry? {
        zip.getEntry(path)?.let { return it }
        val decoded = try { URLDecoder.decode(path, "UTF-8") } catch (_: Exception) { path }
        if (decoded != path) zip.getEntry(decoded)?.let { return it }
        val lower = path.lowercase()
        val lowerDecoded = decoded.lowercase()
        val entries = zip.entries()
        while (entries.hasMoreElements()) {
            val e = entries.nextElement()
            val name = e.name
            if (name.lowercase() == lower || name.lowercase() == lowerDecoded) return e
        }
        return null
    }

    private fun sanitizePath(name: String): String =
        name.split("/").joinToString("/") { seg ->
            seg.replace(':', '_').replace('*', '_').replace('?', '_')
                .replace('<', '_').replace('>', '_').replace('|', '_')
        }

    private fun sanitizeFilename(name: String): String =
        name.replace(':', '_').replace('*', '_').replace('?', '_')
            .replace('<', '_').replace('>', '_').replace('|', '_')
            .replace('/', '_').replace('\\', '_')

    private fun isImagePath(name: String): Boolean {
        val lower = name.lowercase()
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") ||
               lower.endsWith(".gif") || lower.endsWith(".webp") || lower.endsWith(".svg") ||
               lower.endsWith(".bmp") || lower.endsWith(".tiff") || lower.endsWith(".tif")
    }

    private fun isFontItem(href: String, mime: String): Boolean {
        return mime.startsWith("font/") || mime == "application/x-font-ttf" ||
               mime == "application/x-font-otf" ||
               href.endsWith(".ttf", ignoreCase = true) || href.endsWith(".otf", ignoreCase = true)
    }

    private fun guessMimeType(filename: String): String {
        return when (filename.substringAfterLast('.').lowercase()) {
            "png"        -> "image/png"
            "gif"        -> "image/gif"
            "webp"       -> "image/webp"
            "svg"        -> "image/svg+xml"
            "bmp"        -> "image/bmp"
            "tiff", "tif"-> "image/tiff"
            else         -> "image/jpeg"
        }
    }

    private fun decodeName(name: String): String =
        try { URLDecoder.decode(name, "UTF-8") } catch (_: Exception) { name }

    private fun buildZipPath(opfDir: String, href: String): String {
        val decoded = decodeName(href)
        if (decoded.startsWith("/")) return decoded.trimStart('/')
        if (opfDir.isEmpty()) return decoded
        return resolveRelativePath("$opfDir/$decoded")
    }

    private fun resolveRelativePath(raw: String): String {
        val parts = mutableListOf<String>()
        for (seg in raw.split("/")) when (seg) {
            ".."    -> if (parts.isNotEmpty()) parts.removeAt(parts.lastIndex)
            ".", "" -> {}
            else    -> parts.add(seg)
        }
        return parts.joinToString("/")
    }

    private fun extractOpfPath(xml: ByteArray): String? {
        return try {
            val doc = parseXml(xml)
            val nodes = doc.getElementsByTagNameNS("*", "rootfile")
            if (nodes.length > 0) (nodes.item(0) as? Element)?.getAttribute("full-path") else null
        } catch (_: Exception) {
            Regex("""full-path="([^"]+\.opf)"""").find(xml.toString(Charsets.UTF_8))?.groupValues?.get(1)
        }
    }

    private fun extractXhtmlTitle(xhtml: String): String? =
        Regex("""<title[^>]*>([^<]+)</title>""", RegexOption.IGNORE_CASE)
            .find(xhtml)?.groupValues?.get(1)?.trim()

    private fun extractBodyContent(xhtml: String): String {
        val bodyMatch = Regex("""<body[^>]*>""", RegexOption.IGNORE_CASE).find(xhtml) ?: return xhtml
        val closeIdx = xhtml.lastIndexOf("</body>").takeIf { it >= 0 }
            ?: xhtml.lastIndexOf("</BODY>").takeIf { it >= 0 } ?: return xhtml
        return xhtml.substring(bodyMatch.range.last + 1, closeIdx).trim()
    }

    private fun parseXml(bytes: ByteArray): Document {
        val factory = DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = true
            isValidating = false
            try {
                setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
                setFeature("http://xml.org/sax/features/external-general-entities", false)
                setFeature("http://xml.org/sax/features/external-parameter-entities", false)
            } catch (_: Exception) {}
        }
        val builder = factory.newDocumentBuilder()
        builder.setEntityResolver { _, _ -> org.xml.sax.InputSource(java.io.StringReader("")) }
        return builder.parse(bytes.inputStream())
    }

    private fun getTextByTagNS(doc: Document, localName: String): String? {
        val nodes = doc.getElementsByTagNameNS("*", localName)
        return if (nodes.length > 0) nodes.item(0)?.textContent?.trim() else null
    }
}
