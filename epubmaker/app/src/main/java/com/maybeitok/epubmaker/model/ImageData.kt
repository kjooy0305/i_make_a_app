package com.maybeitok.epubmaker.model

import java.io.Serializable

data class ImageData(
    val filename: String,
    val base64: String = "",
    val mimeType: String,
    val localPath: String? = null
) : Serializable
