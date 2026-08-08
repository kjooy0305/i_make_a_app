package com.example.repeatalarm.util

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import java.io.File

object AudioTrimmer {

    fun copyToAppStorage(context: Context, sourceUri: Uri): File? {
        val soundsDir = File(context.filesDir, "sounds").also { it.mkdirs() }
        val outputFile = File(soundsDir, "sound_${System.currentTimeMillis()}.mp3")
        return try {
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                outputFile.outputStream().use { output -> input.copyTo(output) }
            }
            outputFile
        } catch (e: Exception) {
            null
        }
    }

    fun getDurationMs(context: Context, uri: Uri): Int {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toIntOrNull() ?: 0
        } catch (e: Exception) {
            0
        } finally {
            retriever.release()
        }
    }

    fun deleteFile(filePath: String) = File(filePath).delete()
}
