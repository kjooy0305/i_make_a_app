package com.kjooy0305.choicetheroute.model

import org.json.JSONObject

data class BranchEdge(
    val id: String,
    val fromId: String,
    val toId: String,
    val label: String = "",
    val isStraight: Boolean = false
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("fromId", fromId)
        put("toId", toId)
        put("label", label)
        put("isStraight", isStraight)
    }

    companion object {
        fun fromJson(obj: JSONObject) = BranchEdge(
            id = obj.getString("id"),
            fromId = obj.getString("fromId"),
            toId = obj.getString("toId"),
            label = obj.optString("label", ""),
            isStraight = obj.optBoolean("isStraight", false)
        )
    }
}
