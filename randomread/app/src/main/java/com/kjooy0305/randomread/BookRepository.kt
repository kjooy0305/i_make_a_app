package com.kjooy0305.randomread

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class BookRepository(context: Context) {

    private val prefs = context.getSharedPreferences("randomread_data", Context.MODE_PRIVATE)

    fun load(): MutableList<BookItem> {
        val json = prefs.getString("books", "[]") ?: "[]"
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map {
                val obj = arr.getJSONObject(it)
                BookItem(obj.getString("name"), BookType.valueOf(obj.getString("type")))
            }.toMutableList()
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    fun save(books: List<BookItem>) {
        val arr = JSONArray()
        books.forEach {
            arr.put(JSONObject().apply {
                put("name", it.name)
                put("type", it.type.name)
            })
        }
        prefs.edit().putString("books", arr.toString()).apply()
    }

    fun clear() = prefs.edit().remove("books").apply()
}
