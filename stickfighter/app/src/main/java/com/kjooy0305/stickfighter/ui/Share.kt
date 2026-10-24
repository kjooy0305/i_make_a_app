package com.kjooy0305.stickfighter.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.kjooy0305.stickfighter.R
import com.kjooy0305.stickfighter.data.Sfa
import com.kjooy0305.stickfighter.model.Animation
import java.io.File

object Share {
    fun uri(context: Context, file: File): Uri =
        FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)

    fun fileName(title: String) =
        title.replace(Regex("[\\\\/:*?\"<>|\\s]+"), "_").trim('_').ifEmpty { "animation" } + "." + Sfa.EXTENSION

    /** 백업 파일을 만들어 다른 앱으로 보낸다. */
    fun backup(context: Context, animation: Animation) {
        val dir = File(context.cacheDir, "share").apply { mkdirs() }
        val file = File(dir, fileName(animation.title))
        file.outputStream().use { Sfa.write(animation, it) }
        send(context, file, "application/octet-stream")
    }

    fun video(context: Context, file: File) = send(context, file, "video/mp4")

    private fun send(context: Context, file: File, mime: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(Intent.EXTRA_STREAM, uri(context, file))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.share)))
    }
}
