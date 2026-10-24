package com.kjooy0305.apkextractor

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileInputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ApkExtractor {

    const val FOLDER = "APK추출기"

    /**
     * 앱 하나를 다운로드/APK추출기 폴더에 저장한다.
     * 단일 APK는 .apk, 여러 파일로 나뉜 앱은 .apks(zip) 하나로 묶는다.
     */
    fun extract(context: Context, item: AppItem) {
        val base = safeName("${item.label}_${item.versionName}")
        val name = if (item.isSplit) "$base.apks" else "$base.apk"
        val mime = if (item.isSplit) "application/octet-stream" else "application/vnd.android.package-archive"
        openOutput(context, name, mime).use { out ->
            if (item.isSplit) writeZip(item, out) else copy(File(item.sourceDir), out)
        }
    }

    private fun copy(file: File, out: OutputStream) {
        FileInputStream(file).use { it.copyTo(out) }
    }

    private fun writeZip(item: AppItem, out: OutputStream) {
        val zip = ZipOutputStream(out.buffered())
        // APK는 이미 압축되어 있어 다시 압축해도 크기가 거의 줄지 않는다.
        zip.setLevel(0)
        for (path in listOf(item.sourceDir) + item.splitDirs) {
            val file = File(path)
            zip.putNextEntry(ZipEntry(file.name))
            copy(file, zip)
            zip.closeEntry()
        }
        zip.finish()
        zip.flush()
    }

    private fun openOutput(context: Context, name: String, mime: String): OutputStream {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, name)
                put(MediaStore.Downloads.MIME_TYPE, mime)
                put(MediaStore.Downloads.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/$FOLDER")
            }
            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: error("저장 위치를 만들 수 없습니다")
            return context.contentResolver.openOutputStream(uri) ?: error("파일을 열 수 없습니다")
        }
        @Suppress("DEPRECATION")
        val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), FOLDER)
        if (!dir.exists() && !dir.mkdirs()) error("폴더를 만들 수 없습니다")
        return File(dir, name).outputStream()
    }

    private fun safeName(raw: String): String =
        raw.replace(Regex("[\\\\/:*?\"<>|\\s]+"), "_").trim('_').ifEmpty { "app" }
}
