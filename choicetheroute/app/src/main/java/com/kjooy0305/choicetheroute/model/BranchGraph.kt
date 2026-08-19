package com.kjooy0305.choicetheroute.model

import org.json.JSONArray
import org.json.JSONObject

data class BranchGraph(
    val id: String,
    val name: String,
    val nodes: MutableList<BranchNode> = mutableListOf(),
    val edges: MutableList<BranchEdge> = mutableListOf(),
    val createdAt: Long = System.currentTimeMillis()
) {
    /** 시작 노드는 여러 개일 수 있다 (서로 이어지지 않은 갈래가 여럿인 경우) */
    val startNodes: List<BranchNode> get() = nodes.filter { it.isStart }
    val startNode: BranchNode? get() = nodes.find { it.isStart }
    val nodeCount: Int get() = nodes.size
    val edgeCount: Int get() = edges.size

    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("createdAt", createdAt)
        put("nodes", JSONArray().also { arr -> nodes.forEach { arr.put(it.toJson()) } })
        put("edges", JSONArray().also { arr -> edges.forEach { arr.put(it.toJson()) } })
    }

    companion object {
        fun fromJson(obj: JSONObject) = BranchGraph(
            id = obj.getString("id"),
            name = obj.optString("name", ""),
            createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
            nodes = mutableListOf<BranchNode>().also { list ->
                val arr = obj.optJSONArray("nodes") ?: JSONArray()
                for (i in 0 until arr.length()) list.add(BranchNode.fromJson(arr.getJSONObject(i)))
            },
            edges = mutableListOf<BranchEdge>().also { list ->
                val arr = obj.optJSONArray("edges") ?: JSONArray()
                for (i in 0 until arr.length()) list.add(BranchEdge.fromJson(arr.getJSONObject(i)))
            }
        )
    }
}
