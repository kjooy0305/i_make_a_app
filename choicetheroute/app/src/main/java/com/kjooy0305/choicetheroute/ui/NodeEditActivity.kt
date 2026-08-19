package com.kjooy0305.choicetheroute.ui

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kjooy0305.choicetheroute.R
import com.kjooy0305.choicetheroute.databinding.ActivityNodeEditBinding
import com.kjooy0305.choicetheroute.model.NodeColors
import com.kjooy0305.choicetheroute.viewmodel.GraphViewModel

class NodeEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNodeEditBinding
    private val vm: GraphViewModel by viewModels()
    private var graphId: String = ""
    private var nodeId: String = ""

    /** 입력란은 처음 한 번만 채운다 — 이후 그래프가 갱신돼도 편집 중인 글을 덮지 않도록 */
    private var fieldsFilled = false
    private var selectedColor = NodeColors.NONE
    private val swatches = mutableListOf<Pair<Int, View>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNodeEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        graphId = intent.getStringExtra("graphId") ?: run { finish(); return }
        nodeId = intent.getStringExtra("nodeId") ?: run { finish(); return }

        buildColorRow()

        vm.openGraph(graphId)
        vm.current.observe(this) { g ->
            g ?: return@observe
            val node = g.nodes.find { it.id == nodeId } ?: return@observe
            if (!fieldsFilled) {
                fieldsFilled = true
                binding.etTitle.setText(node.title)
                binding.etContent.setText(node.content)
                binding.swStart.isChecked = node.isStart
                selectedColor = node.color
                refreshSwatches()
            }
            binding.toolbar.title = if (node.isStart) "시작 노드 편집" else "분기점 편집"

            // 마지막 하나 남은 시작 노드는 해제도 삭제도 못 한다
            val isLastStart = node.isStart && g.startNodes.size <= 1
            binding.swStart.isEnabled = !isLastStart
            binding.tvStartHint.text =
                if (isLastStart) "마지막 시작 노드라 해제할 수 없습니다. 다른 노드를 시작으로 지정하면 풀 수 있어요."
                else "이야기가 시작되는 지점. 여러 개를 둘 수 있습니다."
            binding.btnDelete.isEnabled = !isLastStart
            binding.btnDelete.alpha = if (isLastStart) 0.3f else 1f
        }

        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnSave.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val content = binding.etContent.text.toString()
            if (title.isEmpty()) {
                TopBanner.show(this, "제목을 입력하세요")
                return@setOnClickListener
            }
            vm.setNodeStart(nodeId, binding.swStart.isChecked)
            vm.updateNode(nodeId, title, content, selectedColor)
            TopBanner.show(this, "저장됨")
            finish()
        }

        binding.btnDelete.setOnClickListener {
            val node = vm.current.value?.nodes?.find { it.id == nodeId } ?: return@setOnClickListener
            if (!vm.canDeleteNode(nodeId)) return@setOnClickListener
            MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialog_Dark)
                .setTitle("노드 삭제")
                .setMessage("「${node.title}」을(를) 삭제할까요?\n연결된 모든 선도 함께 삭제됩니다.")
                .setPositiveButton("삭제") { _, _ ->
                    vm.deleteNode(nodeId)
                    finish()
                }
                .setNegativeButton("취소", null)
                .show()
        }
    }

    // ── 색상 팔레트 ───────────────────────────────────────────────
    private fun buildColorRow() {
        val d = resources.displayMetrics.density
        val size = (40 * d).toInt()
        for (c in NodeColors.PALETTE) {
            val v = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(size, size).apply {
                    marginEnd = (10 * d).toInt()
                }
                contentDescription = if (c == NodeColors.NONE) "기본색" else "색상"
                setOnClickListener {
                    selectedColor = c
                    refreshSwatches()
                }
            }
            binding.colorRow.addView(v)
            swatches += c to v
        }
        refreshSwatches()
    }

    private fun refreshSwatches() {
        val d = resources.displayMetrics.density
        for ((c, v) in swatches) {
            val chosen = (c == selectedColor)
            v.background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(if (c == NodeColors.NONE) getColor(R.color.card) else c)
                setStroke(
                    ((if (chosen) 3 else 1) * d).toInt(),
                    getColor(if (chosen) R.color.text_primary else R.color.border)
                )
            }
        }
    }
}
