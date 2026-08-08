package com.maybeitok.epubmaker.model

import java.io.Serializable

data class CustomFont(
    val name: String,
    val filePath: String,
    val mimeType: String = "font/ttf"
) : Serializable
