package com.maybeitok.epubmaker.model

import java.io.Serializable

data class EpubChapter(
    var title: String = "챕터",
    var content: String = "",
    val embeddedImages: MutableList<ImageData> = mutableListOf()
) : Serializable
