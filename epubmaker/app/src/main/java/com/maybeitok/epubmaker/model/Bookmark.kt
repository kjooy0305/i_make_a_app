package com.maybeitok.epubmaker.model

import java.io.Serializable

data class Bookmark(
    val chapterIndex: Int,
    val chapterTitle: String,
    val scrollY: Int,
    val label: String,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable
