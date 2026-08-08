package com.kjooy0305.randomread

enum class BookType(val label: String, val unit: String) {
    EPUB("EPUB", "권"),
    TXT("TXT", "개")
}

data class BookItem(
    val name: String,
    val type: BookType
)

sealed class ListItem {
    data class Header(val type: BookType, val count: Int) : ListItem()
    data class Book(val item: BookItem, val index: Int) : ListItem()
}
