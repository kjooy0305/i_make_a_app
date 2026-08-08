package com.maybeitok.epubmaker.model

import java.io.Serializable
import java.util.UUID

data class EpubDocument(
    val id: String = UUID.randomUUID().toString(),
    var title: String = "제목 없음",
    var author: String = "",
    var language: String = "ko",
    val chapters: MutableList<EpubChapter> = mutableListOf(EpubChapter(title = "제1장")),
    val customFonts: MutableList<CustomFont> = mutableListOf(),
    var filePath: String? = null
) : Serializable
