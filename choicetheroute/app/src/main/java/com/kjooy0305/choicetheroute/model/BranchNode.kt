package com.kjooy0305.choicetheroute.model

import org.json.JSONObject

data class BranchNode(
    val id: String,
    val title: String,
    val content: String,
    val x: Float,
    val y: Float,
    val isStart: Boolean = false,
    /** 상자에 입힐 색. NodeColors.NONE(0)이면 기본색을 쓴다. */
    val color: Int = NodeColors.NONE
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("content", content)
        put("x", x.toDouble())
        put("y", y.toDouble())
        put("isStart", isStart)
        put("color", color)
    }

    companion object {
        fun fromJson(obj: JSONObject) = BranchNode(
            id = obj.getString("id"),
            title = obj.optString("title", ""),
            content = obj.optString("content", ""),
            x = obj.optDouble("x", 100.0).toFloat(),
            y = obj.optDouble("y", 100.0).toFloat(),
            isStart = obj.optBoolean("isStart", false),
            color = obj.optInt("color", NodeColors.NONE)
        )
    }
}
