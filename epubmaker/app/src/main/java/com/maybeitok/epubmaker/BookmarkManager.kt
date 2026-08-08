package com.maybeitok.epubmaker

import android.content.Context
import com.maybeitok.epubmaker.model.Bookmark
import org.json.JSONArray
import org.json.JSONObject

class BookmarkManager(context: Context, filePath: String) {
    private val prefs = context.getSharedPreferences("bookmarks", Context.MODE_PRIVATE)
    private val key = "bm_${filePath.hashCode()}"

    fun getAll(): MutableList<Bookmark> {
        val json = prefs.getString(key, "[]") ?: "[]"
        val arr = JSONArray(json)
        val list = mutableListOf<Bookmark>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list.add(
                Bookmark(
                    chapterIndex = o.getInt("ci"),
                    chapterTitle = o.getString("ct"),
                    scrollY = o.getInt("sy"),
                    label = o.getString("lb"),
                    timestamp = o.getLong("ts")
                )
            )
        }
        return list
    }

    fun add(bm: Bookmark) {
        val list = getAll()
        list.add(0, bm)
        save(list)
    }

    fun remove(index: Int) {
        val list = getAll()
        if (index in list.indices) {
            list.removeAt(index)
            save(list)
        }
    }

    private fun save(list: List<Bookmark>) {
        val arr = JSONArray()
        list.forEach { bm ->
            arr.put(JSONObject().apply {
                put("ci", bm.chapterIndex)
                put("ct", bm.chapterTitle)
                put("sy", bm.scrollY)
                put("lb", bm.label)
                put("ts", bm.timestamp)
            })
        }
        prefs.edit().putString(key, arr.toString()).apply()
    }
}
