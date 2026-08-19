package com.kjooy0305.choicetheroute.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.kjooy0305.choicetheroute.data.GraphRepository
import com.kjooy0305.choicetheroute.model.BranchEdge
import com.kjooy0305.choicetheroute.model.BranchGraph
import com.kjooy0305.choicetheroute.model.BranchNode
import com.kjooy0305.choicetheroute.model.NodeMetrics
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.UUID

/** connectNodes가 실제로 무엇을 했는지 */
enum class ConnectResult { ADDED, REMOVED, DUPLICATE }

class GraphViewModel(app: Application) : AndroidViewModel(app) {

    private companion object {
        const val HGAP = 110f            // 레이어 사이 가로 간격
        const val VGAP = 46f             // 같은 레이어 안 세로 간격
        const val CROSSING_PASSES = 12   // 교차 줄이기 스윕 횟수
        const val CENTERING_PASSES = 4   // 부모를 자식 중앙에 맞추는 반복 횟수
    }

    private val repo = GraphRepository(app)

    val graphs = MutableLiveData<List<BranchGraph>>(emptyList())
    val current = MutableLiveData<BranchGraph?>()

    // BFS layer per node id (for forward/back edge classification)
    val nodeLayer = MutableLiveData<Map<String, Int>>(emptyMap())

    // ── Home ──────────────────────────────────────────────────────

    fun loadAll() = viewModelScope.launch {
        graphs.value = repo.loadAll()
    }

    fun createGraph(name: String) = viewModelScope.launch {
        val id = UUID.randomUUID().toString()
        val startNode = BranchNode(
            id = UUID.randomUUID().toString(),
            title = name,
            content = "",
            x = 80f,
            y = 200f,
            isStart = true
        )
        val graph = BranchGraph(id = id, name = name, nodes = mutableListOf(startNode))
        repo.save(graph)
        graphs.value = repo.loadAll()
    }

    fun deleteGraph(id: String) = viewModelScope.launch {
        repo.delete(id)
        graphs.value = repo.loadAll()
    }

    // ── Graph Editor ──────────────────────────────────────────────

    fun openGraph(id: String) = viewModelScope.launch {
        val g = repo.load(id) ?: return@launch
        current.value = g
        recomputeLayers(g)
    }

    fun saveGraph() = viewModelScope.launch {
        current.value?.let { repo.save(it) }
    }

    /** 시작 노드가 여럿이면 그 모두를 0층으로 두고 동시에 퍼져 나간다. */
    private fun recomputeLayers(g: BranchGraph) {
        val starts = g.startNodes.ifEmpty { listOfNotNull(g.nodes.firstOrNull()) }
        if (starts.isEmpty()) return
        val adj = buildAdjacency(g)
        val layers = mutableMapOf<String, Int>()
        val queue = ArrayDeque<String>()
        for (s in starts) {
            layers[s.id] = 0
            queue.add(s.id)
        }
        while (queue.isNotEmpty()) {
            val cur = queue.removeFirst()
            val curLayer = layers[cur] ?: 0
            for (nxt in (adj[cur] ?: emptyList())) {
                if (!layers.containsKey(nxt)) {
                    layers[nxt] = curLayer + 1
                    queue.add(nxt)
                }
            }
        }
        // Assign remaining (isolated or in cycles)
        val maxLayer = if (layers.isEmpty()) 0 else layers.values.max()
        for (node in g.nodes) {
            if (!layers.containsKey(node.id)) layers[node.id] = maxLayer + 1
        }
        nodeLayer.value = layers
    }

    private fun buildAdjacency(g: BranchGraph): Map<String, List<String>> {
        val adj = mutableMapOf<String, MutableList<String>>()
        for (node in g.nodes) adj[node.id] = mutableListOf()
        for (edge in g.edges) adj[edge.fromId]?.add(edge.toId)
        return adj
    }

    // ── Node operations ───────────────────────────────────────────

    fun addBranchFrom(fromNodeId: String) {
        val g = current.value ?: return
        val fromNode = g.nodes.find { it.id == fromNodeId } ?: return
        val childCount = g.edges.count { it.fromId == fromNodeId && it.toId != fromNodeId }
        val newNode = BranchNode(
            id = UUID.randomUUID().toString(),
            title = "분기점",
            content = "",
            x = fromNode.x + NodeMetrics.widthOf(fromNode) + HGAP,
            y = fromNode.y + childCount * (NodeMetrics.HEIGHT + VGAP),
            isStart = false
        )
        val edge = BranchEdge(
            id = UUID.randomUUID().toString(),
            fromId = fromNodeId,
            toId = newNode.id
        )
        g.nodes.add(newNode)
        g.edges.add(edge)
        current.value = g
        recomputeLayers(g)
        saveGraph()
    }

    /** (x, y)를 새 노드의 중심으로 삼아 추가한다. */
    fun addStandaloneNode(x: Float, y: Float) {
        val g = current.value ?: return
        val title = "분기점"
        val newNode = BranchNode(
            id = UUID.randomUUID().toString(),
            title = title,
            content = "",
            x = x - NodeMetrics.widthFor(title) / 2,
            y = y - NodeMetrics.HEIGHT / 2
        )
        g.nodes.add(newNode)
        current.value = g
        saveGraph()
    }

    fun connectNodes(fromId: String, toId: String): ConnectResult {
        val g = current.value ?: return ConnectResult.DUPLICATE
        val existing = g.edges.filter { it.fromId == fromId && it.toId == toId }
        if (existing.isNotEmpty()) {
            // 자기참조는 같은 노드를 다시 고르면 해제된다 (스위치처럼 껐다 켰다)
            if (fromId == toId) {
                g.edges.removeAll(existing)
                current.value = g
                recomputeLayers(g)
                saveGraph()
                return ConnectResult.REMOVED
            }
            return ConnectResult.DUPLICATE
        }
        val edge = BranchEdge(
            id = UUID.randomUUID().toString(),
            fromId = fromId,
            toId = toId
        )
        g.edges.add(edge)
        current.value = g
        recomputeLayers(g)
        saveGraph()
        return ConnectResult.ADDED
    }

    fun updateNodePosition(nodeId: String, x: Float, y: Float) {
        val g = current.value ?: return
        val idx = g.nodes.indexOfFirst { it.id == nodeId }
        if (idx < 0) return
        g.nodes[idx] = g.nodes[idx].copy(x = x, y = y)
        current.value = g
    }

    fun updateNode(nodeId: String, title: String, content: String, color: Int) {
        val g = current.value ?: return
        val idx = g.nodes.indexOfFirst { it.id == nodeId }
        if (idx < 0) return
        g.nodes[idx] = g.nodes[idx].copy(title = title, content = content, color = color)
        current.value = g
        saveGraph()
    }

    /**
     * 시작 노드 지정/해제.
     * 마지막 남은 시작 노드는 해제할 수 없다 — 하나도 없으면 층 계산의 기준이 사라진다.
     *
     * @return 실제로 바뀌었으면 true
     */
    fun setNodeStart(nodeId: String, isStart: Boolean): Boolean {
        val g = current.value ?: return false
        val idx = g.nodes.indexOfFirst { it.id == nodeId }
        if (idx < 0) return false
        val node = g.nodes[idx]
        if (node.isStart == isStart) return false
        if (!isStart && g.nodes.count { it.isStart } <= 1) return false
        g.nodes[idx] = node.copy(isStart = isStart)
        current.value = g
        recomputeLayers(g)
        saveGraph()
        return true
    }

    /** 시작 노드는 다른 시작 노드가 남아 있을 때만 지울 수 있다. */
    fun canDeleteNode(nodeId: String): Boolean {
        val g = current.value ?: return false
        val node = g.nodes.find { it.id == nodeId } ?: return false
        return !node.isStart || g.nodes.count { it.isStart } > 1
    }

    fun deleteNode(nodeId: String) {
        val g = current.value ?: return
        val node = g.nodes.find { it.id == nodeId } ?: return
        if (!canDeleteNode(nodeId)) return
        g.nodes.remove(node)
        g.edges.removeAll { it.fromId == nodeId || it.toId == nodeId }
        current.value = g
        recomputeLayers(g)
        saveGraph()
    }

    // Position save on drag end
    fun onNodeMoveEnd() {
        saveGraph()
    }

    fun toggleEdgeStyle(edgeId: String) {
        val g = current.value ?: return
        val idx = g.edges.indexOfFirst { it.id == edgeId }
        if (idx < 0) return
        g.edges[idx] = g.edges[idx].copy(isStraight = !g.edges[idx].isStraight)
        current.value = g
        saveGraph()
    }

    fun deleteEdge(edgeId: String) {
        val g = current.value ?: return
        g.edges.removeAll { it.id == edgeId }
        current.value = g
        recomputeLayers(g)
        saveGraph()
    }

    /**
     * 자동 정리 — 레이어별로 세우되, 선이 서로 X자로 엇갈리는 횟수를 줄인다.
     *
     * Sugiyama 방식의 median heuristic: 각 노드를 이웃 레이어에서 자기와
     * 이어진 노드들의 중앙값 위치로 끌어당기며 앞뒤로 여러 번 훑는다.
     * 노드 폭은 제목 길이에 따라 다르므로 레이어 폭은 그 레이어의
     * 가장 넓은 노드에 맞춘다.
     */
    fun autoLayout() {
        val g = current.value ?: return
        val layers = nodeLayer.value ?: return

        val byLayer = mutableMapOf<Int, MutableList<BranchNode>>()
        for (node in g.nodes) {
            byLayer.getOrPut(layers[node.id] ?: 0) { mutableListOf() }.add(node)
        }
        val layerKeys = byLayer.keys.sorted()
        // 시작 순서는 지금 화면에 놓인 위아래 순서를 그대로 존중한다
        for (list in byLayer.values) list.sortBy { it.y }

        val preds = HashMap<String, MutableList<String>>()
        val succs = HashMap<String, MutableList<String>>()
        for (e in g.edges) {
            if (e.fromId == e.toId) continue
            succs.getOrPut(e.fromId) { mutableListOf() }.add(e.toId)
            preds.getOrPut(e.toId) { mutableListOf() }.add(e.fromId)
        }

        val pos = HashMap<String, Int>()
        repeat(CROSSING_PASSES) { pass ->
            for (k in layerKeys) byLayer[k]!!.forEachIndexed { i, n -> pos[n.id] = i }
            val forward = pass % 2 == 0
            val sweep = if (forward) layerKeys else layerKeys.asReversed()
            // 앞으로 훑을 땐 앞 레이어(부모)를, 뒤로 훑을 땐 뒤 레이어(자식)를 기준으로 삼는다
            val neighbors = if (forward) preds else succs
            for (k in sweep) {
                val list = byLayer[k]!!
                // sortedBy는 안정 정렬이라 median이 같은 노드들은 지금 순서를 유지한다
                val reordered = list.withIndex()
                    .map { (i, n) ->
                        val idxs = (neighbors[n.id] ?: emptyList()).mapNotNull { pos[it] }.sorted()
                        // 이웃이 없으면 원래 자리를 유지시킨다
                        val median = if (idxs.isEmpty()) i.toFloat() else idxs[idxs.size / 2].toFloat()
                        n to median
                    }
                    .sortedBy { it.second }
                    .map { it.first }
                list.clear(); list.addAll(reordered)
                reordered.forEachIndexed { i, n -> pos[n.id] = i }
            }
        }

        // 레이어별 x 위치 (레이어마다 폭이 다르므로 누적으로 계산)
        val layerX = HashMap<Int, Float>()
        val layerW = HashMap<Int, Float>()
        var x = 60f
        for (k in layerKeys) {
            val w = byLayer[k]!!.maxOf { NodeMetrics.widthOf(it) }
            layerX[k] = x; layerW[k] = w
            x += w + HGAP
        }

        // ── y 좌표: 상위 노드를 자기 하위 분기점들의 한가운데에 둔다 ──
        val slot = NodeMetrics.HEIGHT + VGAP
        val yOf = HashMap<String, Float>()
        for (k in layerKeys) {
            byLayer[k]!!.forEachIndexed { i, n -> yOf[n.id] = i * slot }
        }

        // 층이 뒤로 가는 연결만 "하위 분기"로 친다 (역방향·자기참조는 중심 계산에서 뺀다)
        val children = HashMap<String, MutableList<String>>()
        for (e in g.edges) {
            if (e.fromId == e.toId) continue
            val lf = layers[e.fromId] ?: 0
            val lt = layers[e.toId] ?: 0
            if (lt > lf) children.getOrPut(e.fromId) { mutableListOf() }.add(e.toId)
        }

        // 맨 뒤 층부터 앞으로 거슬러 오며 부모를 자식들 평균 높이에 맞춘다
        repeat(CENTERING_PASSES) {
            for (k in layerKeys.asReversed()) {
                val list = byLayer[k]!!
                val desired = list.map { n ->
                    val kids = (children[n.id] ?: emptyList()).mapNotNull { yOf[it] }
                    if (kids.isEmpty()) yOf[n.id]!! else kids.average().toFloat()
                }
                packLayer(list, desired, slot, yOf)
            }
        }

        val topY = (yOf.values.minOrNull() ?: 0f) - 80f
        for (k in layerKeys) {
            for (node in byLayer[k]!!) {
                // 폭이 제각각이므로 레이어 안에서 가운데 정렬
                val nx = layerX[k]!! + (layerW[k]!! - NodeMetrics.widthOf(node)) / 2
                val idx = g.nodes.indexOfFirst { it.id == node.id }
                if (idx >= 0) g.nodes[idx] = node.copy(x = nx, y = yOf[node.id]!! - topY)
            }
        }

        current.value = g
        saveGraph()
    }

    /**
     * 원하는 높이(desired)에 최대한 가깝게 두되, 같은 층 안에서 순서를 지키고
     * 상자끼리 겹치지 않게 최소 간격을 확보한다. 아래로 밀어낸 만큼 층 전체를
     * 되밀어서 평균 위치는 원래 원하던 곳에 남게 한다.
     */
    private fun packLayer(
        list: List<BranchNode>,
        desired: List<Float>,
        slot: Float,
        yOf: MutableMap<String, Float>
    ) {
        if (list.isEmpty()) return
        val ys = FloatArray(list.size)
        ys[0] = desired[0]
        for (i in 1 until list.size) ys[i] = maxOf(desired[i], ys[i - 1] + slot)
        val shift = desired.average().toFloat() - ys.average().toFloat()
        for (i in list.indices) yOf[list[i].id] = ys[i] + shift
    }

    fun importGraph(jsonStr: String, onError: (() -> Unit)? = null) = viewModelScope.launch {
        try {
            val imported = BranchGraph.fromJson(JSONObject(jsonStr))
            repo.save(imported)
            current.value = imported
            recomputeLayers(imported)
            graphs.value = repo.loadAll()
        } catch (_: Exception) {
            onError?.invoke()
        }
    }
}
