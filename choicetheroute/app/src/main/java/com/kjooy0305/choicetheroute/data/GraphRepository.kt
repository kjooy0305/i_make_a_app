package com.kjooy0305.choicetheroute.data

import android.content.Context
import com.kjooy0305.choicetheroute.model.BranchGraph
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class GraphRepository(private val context: Context) {

    private val graphsDir: File get() = File(context.filesDir, "graphs").also { it.mkdirs() }
    private val indexFile: File get() = File(graphsDir, "_index.json")

    // ── Index ─────────────────────────────────────────────────────

    private fun readIndex(): List<String> {
        if (!indexFile.exists()) return emptyList()
        return try {
            val arr = JSONArray(indexFile.readText())
            (0 until arr.length()).map { arr.getString(it) }
        } catch (_: Exception) { emptyList() }
    }

    private fun writeIndex(ids: List<String>) {
        val arr = JSONArray()
        ids.forEach { arr.put(it) }
        indexFile.writeText(arr.toString())
    }

    // ── CRUD ──────────────────────────────────────────────────────

    suspend fun loadAll(): List<BranchGraph> = withContext(Dispatchers.IO) {
        readIndex().mapNotNull { id ->
            try {
                val f = File(graphsDir, "$id.json")
                if (f.exists()) BranchGraph.fromJson(JSONObject(f.readText())) else null
            } catch (_: Exception) { null }
        }.sortedByDescending { it.createdAt }
    }

    suspend fun load(id: String): BranchGraph? = withContext(Dispatchers.IO) {
        try {
            val f = File(graphsDir, "$id.json")
            if (f.exists()) BranchGraph.fromJson(JSONObject(f.readText())) else null
        } catch (_: Exception) { null }
    }

    suspend fun save(graph: BranchGraph) = withContext(Dispatchers.IO) {
        File(graphsDir, "${graph.id}.json").writeText(graph.toJson().toString())
        val index = readIndex().toMutableList()
        if (!index.contains(graph.id)) {
            index.add(0, graph.id)
            writeIndex(index)
        }
    }

    suspend fun delete(id: String) = withContext(Dispatchers.IO) {
        File(graphsDir, "$id.json").delete()
        writeIndex(readIndex().filter { it != id })
    }
}
