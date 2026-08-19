package com.kjooy0305.choicetheroute.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kjooy0305.choicetheroute.R
import com.kjooy0305.choicetheroute.databinding.ActivityGraphBinding
import com.kjooy0305.choicetheroute.model.BranchEdge
import com.kjooy0305.choicetheroute.model.BranchNode
import com.kjooy0305.choicetheroute.viewmodel.ConnectResult
import com.kjooy0305.choicetheroute.viewmodel.GraphViewModel

class GraphActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGraphBinding
    private val vm: GraphViewModel by viewModels()
    private var graphId: String = ""

    private var selectedNode: BranchNode? = null
    private var selectedEdge: BranchEdge? = null
    private var centeredOnce = false

    /** 선택한 노드에서 몇 단계 뒤까지 보일지. 0이면 전체. */
    private var focusDepth = 0

    private val exportLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> uri?.let { writeExportTo(it) } }

    private val importLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { readImportFrom(it) } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGraphBinding.inflate(layoutInflater)
        setContentView(binding.root)

        graphId = intent.getStringExtra("graphId") ?: run { finish(); return }

        setupToolbar()
        setupCanvas()
        setupActions()
        observeData()

        vm.openGraph(graphId)
    }

    // ── Toolbar ───────────────────────────────────────────────────
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.toolbar.inflateMenu(R.menu.menu_graph)
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menuExport -> {
                    val name = vm.current.value?.name?.replace(Regex("[^a-zA-Z0-9가-힣_\\-]"), "_") ?: "graph"
                    exportLauncher.launch("$name.json")
                    true
                }
                R.id.menuImport -> {
                    importLauncher.launch(arrayOf("application/json", "*/*"))
                    true
                }
                else -> false
            }
        }

        binding.btnCenter.setOnClickListener { binding.canvas.centerView() }
        binding.btnAutoLayout.setOnClickListener {
            vm.autoLayout()
            binding.canvas.post { binding.canvas.centerView() }
            TopBanner.show(this, "자동 정리 완료")
        }
    }

    // ── Canvas ────────────────────────────────────────────────────
    private fun setupCanvas() {
        val canvas = binding.canvas

        canvas.onSelectionChanged = { node ->
            selectedNode = node
            selectedEdge = null
            if (node != null) showNodePanel(node) else { clearFocus(); hidePanel() }
        }

        canvas.onEdgeSelectionChanged = { edge ->
            selectedEdge = edge
            selectedNode = null
            if (edge != null) showEdgePanel(edge) else { clearFocus(); hidePanel() }
        }

        canvas.onNodeMoved = { id, x, y -> vm.updateNodePosition(id, x, y) }
        canvas.onMoveEnd = { vm.onNodeMoveEnd() }

        canvas.onConnect = { fromId, toId ->
            val result = vm.connectNodes(fromId, toId)
            canvas.connectMode = false
            val node = selectedNode
            if (node != null) showNodePanel(node) else hidePanel()
            val self = (fromId == toId)
            TopBanner.show(this, when (result) {
                ConnectResult.ADDED -> if (self) "자기참조 연결됨 (다시 연결하면 해제)" else "연결 완료"
                ConnectResult.REMOVED -> "자기참조 해제됨"
                ConnectResult.DUPLICATE -> "이미 연결되어 있습니다"
            })
        }
        canvas.onNodeEdit = { node -> openNodeEdit(node) }
        canvas.onAddStandaloneNode = { x, y -> vm.addStandaloneNode(x, y) }
    }

    // ── Actions ───────────────────────────────────────────────────
    private fun setupActions() {
        // 노드 버튼
        binding.btnBranch.setOnClickListener {
            val node = selectedNode ?: return@setOnClickListener
            vm.addBranchFrom(node.id)
            TopBanner.show(this, "분기점 추가됨")
        }
        binding.btnConnect.setOnClickListener {
            val node = selectedNode ?: return@setOnClickListener
            binding.canvas.startConnectFrom(node.id)
            hideNodeButtonsForConnect()
        }
        binding.btnCancelConnect.setOnClickListener {
            binding.canvas.connectMode = false
            showNodePanel(selectedNode ?: return@setOnClickListener)
        }
        binding.btnEdit.setOnClickListener {
            selectedNode?.let { openNodeEdit(it) }
        }
        binding.btnDelete.setOnClickListener {
            val node = selectedNode ?: return@setOnClickListener
            if (!vm.canDeleteNode(node.id)) {
                TopBanner.show(this, "마지막 시작 노드는 삭제할 수 없습니다")
                return@setOnClickListener
            }
            MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialog_Dark)
                .setTitle("노드 삭제")
                .setMessage("「${node.title}」을(를) 삭제할까요?\n연결된 모든 선도 함께 삭제됩니다.")
                .setPositiveButton("삭제") { _, _ ->
                    vm.deleteNode(node.id)
                    binding.canvas.selectedId = null
                    hidePanel()
                }
                .setNegativeButton("취소", null)
                .show()
        }

        // 엣지 버튼
        binding.btnEdgeToggleStraight.setOnClickListener {
            val edge = selectedEdge ?: return@setOnClickListener
            vm.toggleEdgeStyle(edge.id)
        }
        binding.btnEdgeDelete.setOnClickListener {
            val edge = selectedEdge ?: return@setOnClickListener
            vm.deleteEdge(edge.id)
            binding.canvas.selectedEdgeId = null
            hidePanel()
        }

        // 하위 분기 표시 단계
        binding.btnDepth1.setOnClickListener { applyDepth(1) }
        binding.btnDepth2.setOnClickListener { applyDepth(2) }
        binding.btnDepth3.setOnClickListener { applyDepth(3) }
        binding.btnDepthAll.setOnClickListener { applyDepth(0) }

        // FAB
        binding.fabAdd.setOnClickListener {
            val (cx, cy) = binding.canvas.getCanvasCenter()
            val offset = (vm.current.value?.nodes?.size ?: 0) * 20f
            vm.addStandaloneNode(cx + offset % 80f, cy + offset % 60f)
            TopBanner.show(this, "새 노드 추가됨 (빈 곳 더블탭으로도 추가)")
        }
    }

    // ── 하위 분기 표시 단계 ────────────────────────────────────────
    private fun applyDepth(depth: Int) {
        focusDepth = depth
        binding.canvas.focusRootId = selectedNode?.id
        binding.canvas.focusDepth = depth
        updateDepthButtons()
        if (depth == 0) {
            TopBanner.show(this, "전체 분기 표시")
        } else {
            val name = selectedNode?.title ?: "선택 노드"
            TopBanner.show(this, "「$name」 기준 $depth 단계까지 표시")
        }
    }

    /** 선택이 풀리면 필터도 함께 푼다 — 안 그러면 일부만 보이는 채로 갇힌다. */
    private fun clearFocus() {
        focusDepth = 0
        binding.canvas.focusDepth = 0
        binding.canvas.focusRootId = null
        updateDepthButtons()
    }

    private fun updateDepthButtons() {
        val buttons = listOf(
            1 to binding.btnDepth1,
            2 to binding.btnDepth2,
            3 to binding.btnDepth3,
            0 to binding.btnDepthAll
        )
        for ((depth, btn) in buttons) markDepthButton(btn, depth == focusDepth)
    }

    private fun markDepthButton(btn: MaterialButton, active: Boolean) {
        val color = getColor(if (active) R.color.accent else R.color.text_secondary)
        btn.setTextColor(color)
        btn.strokeColor = android.content.res.ColorStateList.valueOf(
            getColor(if (active) R.color.accent else R.color.border)
        )
    }

    // ── Observe ───────────────────────────────────────────────────
    private fun observeData() {
        vm.current.observe(this) { g ->
            g ?: return@observe
            binding.toolbar.title = g.name
            binding.canvas.nodes = g.nodes.toList()
            binding.canvas.edges = g.edges.toList()
            if (!centeredOnce && g.nodes.isNotEmpty()) {
                centeredOnce = true
                binding.canvas.centerView()
            }
            // 선택 중인 항목은 최신 객체로 갱신 (편집 화면에서 시작 지정·색을 바꾸고 돌아온 경우)
            selectedEdge?.let { old ->
                val updated = g.edges.find { it.id == old.id }
                if (updated != null) {
                    selectedEdge = updated
                    updateEdgePanelButton(updated)
                }
            }
            selectedNode?.let { old ->
                val updated = g.nodes.find { it.id == old.id }
                if (updated != null && !binding.canvas.connectMode) {
                    selectedNode = updated
                    showNodePanel(updated)
                }
            }
        }
        vm.nodeLayer.observe(this) { layers -> binding.canvas.nodeLayers = layers }
    }

    // ── Panel helpers ─────────────────────────────────────────────
    private fun showNodePanel(node: BranchNode) {
        binding.actionPanel.visibility = View.VISIBLE
        binding.tvSelectedTitle.text = node.title
        binding.nodeButtonGroup.visibility = View.VISIBLE
        binding.edgeButtonGroup.visibility = View.GONE
        binding.depthGroup.visibility = View.VISIBLE
        binding.btnDelete.visibility = if (vm.canDeleteNode(node.id)) View.VISIBLE else View.GONE
        binding.btnCancelConnect.visibility = View.GONE
        binding.btnEdit.visibility = View.VISIBLE
        binding.btnBranch.visibility = View.VISIBLE
        binding.btnConnect.visibility = View.VISIBLE

        // 단계 필터가 켜져 있으면 방금 고른 노드를 기준으로 옮겨 준다
        binding.canvas.focusRootId = node.id
        binding.canvas.focusDepth = focusDepth
        updateDepthButtons()
    }

    private fun showEdgePanel(edge: BranchEdge) {
        binding.actionPanel.visibility = View.VISIBLE
        binding.tvSelectedTitle.text = "연결선 선택됨"
        binding.nodeButtonGroup.visibility = View.GONE
        binding.edgeButtonGroup.visibility = View.VISIBLE
        binding.depthGroup.visibility = View.GONE
        updateEdgePanelButton(edge)
    }

    private fun updateEdgePanelButton(edge: BranchEdge) {
        binding.btnEdgeToggleStraight.text = if (edge.isStraight) "곡선으로 전환" else "직선으로 전환"
    }

    private fun hidePanel() {
        binding.actionPanel.visibility = View.GONE
        binding.nodeButtonGroup.visibility = View.GONE
        binding.edgeButtonGroup.visibility = View.GONE
        binding.depthGroup.visibility = View.GONE
    }

    private fun hideNodeButtonsForConnect() {
        // 안내는 패널 제목 줄에 계속 띄워 둔다 (캔버스에도 같은 문구를 그리지 않는다)
        binding.tvSelectedTitle.text = "연결할 대상 노드를 탭하세요"
        binding.depthGroup.visibility = View.GONE
        binding.btnEdit.visibility = View.GONE
        binding.btnBranch.visibility = View.GONE
        binding.btnConnect.visibility = View.GONE
        binding.btnDelete.visibility = View.GONE
        binding.btnCancelConnect.visibility = View.VISIBLE
    }

    // ── Export / Import ───────────────────────────────────────────
    private fun writeExportTo(uri: Uri) {
        try {
            val json = vm.current.value?.toJson()?.toString(2) ?: return
            contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray(Charsets.UTF_8)) }
            TopBanner.show(this, "내보내기 완료")
        } catch (_: Exception) {
            TopBanner.show(this, "내보내기 실패")
        }
    }

    private fun readImportFrom(uri: Uri) {
        try {
            val json = contentResolver.openInputStream(uri)
                ?.use { it.readBytes().toString(Charsets.UTF_8) } ?: return
            vm.importGraph(json) {
                runOnUiThread {
                    TopBanner.show(this, "가져오기 실패: 올바른 JSON 파일이 아닙니다", longer = true)
                }
            }
            centeredOnce = false
            TopBanner.show(this, "가져오기 완료")
        } catch (_: Exception) {
            TopBanner.show(this, "가져오기 실패: 파일을 읽을 수 없습니다", longer = true)
        }
    }

    // ── Navigation ────────────────────────────────────────────────
    private fun openNodeEdit(node: BranchNode) {
        startActivity(
            Intent(this, NodeEditActivity::class.java)
                .putExtra("graphId", graphId)
                .putExtra("nodeId", node.id)
        )
    }

    override fun onResume() {
        super.onResume()
        vm.openGraph(graphId)
    }
}
