package com.kjooy0305.apkextractor

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileInputStream
import java.io.FilterOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/** 추출 중 사용자가 취소했을 때 */
class ExtractCancelled : IOException("추출을 취소했습니다")

object ApkExtractor {

    const val FOLDER = "APK추출기"
    private const val BUFFER = 1 shl 20 // 1MiB: 큰 파일을 작은 조각으로 쓰다 느려지지 않게

    /**
     * 앱 하나를 다운로드/APK추출기 폴더에 저장한다.
     * 단일 APK는 .apk, 여러 파일로 나뉜 앱은 .apks(zip) 하나로 묶는다.
     *
     * 끝까지 쓰고 검증(크기, zip 구조, CRC)을 통과한 뒤에야 파일이 보이게 한다.
     * 중간에 실패하거나 취소하면 만들던 파일을 지운다 (끊긴 파일이 남지 않게).
     */
    fun extract(
        context: Context,
        item: AppItem,
        isCancelled: () -> Boolean = { false },
        onProgress: (done: Long, total: Long) -> Unit = { _, _ -> },
    ): String {
        val sources = (listOf(item.sourceDir) + item.splitDirs).map { File(it) }
        sources.forEach { if (!it.canRead()) throw IOException("원본을 읽을 수 없습니다: ${it.name}") }
        val total = sources.sumOf { it.length() }
        val base = safeName("${item.label}_${item.versionName}")
        val name = if (item.isSplit) "$base.apks" else "$base.apk"
        val mime = if (item.isSplit) "application/octet-stream" else "application/vnd.android.package-archive"

        val target = Target.create(context, name, mime)
        var finished = false
        try {
            var done = 0L
            val counting = CountingOutputStream(target.openStream())
            counting.use { raw ->
                val sink: OutputStream = if (item.isSplit) ZipOutputStream(raw.buffered(BUFFER)) else raw
                if (item.isSplit) {
                    val zip = sink as ZipOutputStream
                    zip.setLevel(0) // APK는 이미 압축되어 있어 다시 압축해도 크기가 거의 줄지 않는다.
                    for (f in sources) {
                        zip.putNextEntry(ZipEntry(f.name))
                        done = copy(f, zip, isCancelled, done, total, onProgress)
                        zip.closeEntry()
                    }
                    zip.finish()
                    zip.flush()
                } else {
                    done = copy(sources[0], sink, isCancelled, done, total, onProgress)
                    sink.flush()
                }
            }
            val written = counting.count
            target.verify(written, if (item.isSplit) sources.size else 0, if (item.isSplit) null else sources[0].length())
            target.publish()
            finished = true
            return name
        } finally {
            if (!finished) target.discard()
        }
    }

    private fun copy(
        file: File,
        out: OutputStream,
        isCancelled: () -> Boolean,
        startDone: Long,
        total: Long,
        onProgress: (Long, Long) -> Unit,
    ): Long {
        var done = startDone
        val buf = ByteArray(BUFFER)
        var lastReport = 0L
        FileInputStream(file).use { input ->
            while (true) {
                if (isCancelled()) throw ExtractCancelled()
                val n = input.read(buf)
                if (n < 0) break
                out.write(buf, 0, n)
                done += n
                val now = System.nanoTime()
                if (now - lastReport > 150_000_000L) { onProgress(done, total); lastReport = now }
            }
        }
        onProgress(done, total)
        if (done - startDone != file.length()) {
            throw IOException("원본 크기와 읽은 크기가 다릅니다: ${file.name}")
        }
        return done
    }

    private class CountingOutputStream(out: OutputStream) : FilterOutputStream(out) {
        var count = 0L
        override fun write(b: Int) { out.write(b); count++ }
        override fun write(b: ByteArray, off: Int, len: Int) { out.write(b, off, len); count += len }
    }

    /** 저장 위치. Android 10+는 MediaStore(작성 중에는 보이지 않음), 그 이하는 .part 파일 */
    private class Target private constructor(
        private val context: Context,
        private val uri: Uri?,
        private val file: File?,
        private val finalName: String,
    ) {
        fun openStream(): OutputStream = when {
            uri != null -> context.contentResolver.openOutputStream(uri, "w") ?: throw IOException("파일을 열 수 없습니다")
            else -> file!!.outputStream()
        }

        private fun openInput(): InputStream = when {
            uri != null -> context.contentResolver.openInputStream(uri) ?: throw IOException("저장한 파일을 다시 열 수 없습니다")
            else -> file!!.inputStream()
        }

        private fun size(): Long = when {
            uri != null -> context.contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize } ?: -1L
            else -> file!!.length()
        }

        /** 쓴 바이트 수와 실제 파일 크기가 같은지, zip이면 구조와 CRC까지 다시 읽어 확인 */
        fun verify(written: Long, zipEntries: Int, expectedSize: Long?) {
            val actual = size()
            if (actual != written) throw IOException("저장한 크기가 맞지 않습니다 (쓴 것 $written, 실제 $actual)")
            if (expectedSize != null && actual != expectedSize) throw IOException("원본과 크기가 다릅니다")
            if (zipEntries > 0) {
                var entries = 0
                ZipInputStream(openInput().buffered(BUFFER)).use { zin ->
                    val crc = CRC32()
                    val buf = ByteArray(BUFFER)
                    while (true) {
                        val e = zin.nextEntry ?: break
                        crc.reset()
                        while (true) {
                            val n = zin.read(buf)
                            if (n < 0) break
                            crc.update(buf, 0, n)
                        }
                        if (e.crc != -1L && e.crc != crc.value) throw IOException("압축 파일 검사에 실패했습니다 (${e.name})")
                        entries++
                    }
                }
                if (entries != zipEntries) throw IOException("압축 파일에 들어 있는 항목 수가 맞지 않습니다 ($entries/$zipEntries)")
            }
        }

        fun publish() {
            if (uri != null) {
                val v = ContentValues().apply { put(MediaStore.Downloads.IS_PENDING, 0) }
                context.contentResolver.update(uri, v, null, null)
            } else {
                val dest = File(file!!.parentFile, finalName)
                if (dest.exists()) dest.delete()
                if (!file.renameTo(dest)) throw IOException("저장한 파일의 이름을 바꿀 수 없습니다")
            }
        }

        fun discard() {
            try {
                if (uri != null) context.contentResolver.delete(uri, null, null) else file?.delete()
            } catch (_: Exception) {
                // 지우지 못해도 보이지 않는 임시 파일이라 넘어간다
            }
        }

        companion object {
            fun create(context: Context, name: String, mime: String): Target {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val values = ContentValues().apply {
                        put(MediaStore.Downloads.DISPLAY_NAME, name)
                        put(MediaStore.Downloads.MIME_TYPE, mime)
                        put(MediaStore.Downloads.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/$FOLDER")
                        put(MediaStore.Downloads.IS_PENDING, 1)
                    }
                    val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                        ?: throw IOException("저장 위치를 만들 수 없습니다")
                    return Target(context, uri, null, name)
                }
                @Suppress("DEPRECATION")
                val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), FOLDER)
                if (!dir.exists() && !dir.mkdirs()) throw IOException("폴더를 만들 수 없습니다")
                return Target(context, null, File(dir, "$name.part"), name)
            }
        }
    }

    private fun safeName(raw: String): String =
        raw.replace(Regex("[\\\\/:*?\"<>|\\s]+"), "_").trim('_').ifEmpty { "app" }
}
