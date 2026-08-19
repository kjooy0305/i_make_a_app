package com.kjooy0305.choicetheroute.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kjooy0305.choicetheroute.R
import com.kjooy0305.choicetheroute.databinding.ActivityHomeBinding
import com.kjooy0305.choicetheroute.databinding.ItemGraphCardBinding
import com.kjooy0305.choicetheroute.model.BranchGraph
import com.kjooy0305.choicetheroute.viewmodel.GraphViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val vm: GraphViewModel by viewModels()
    private lateinit var adapter: GraphAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = GraphAdapter(
            onOpen = { graph ->
                startActivity(Intent(this, GraphActivity::class.java)
                    .putExtra("graphId", graph.id))
            },
            onDelete = { graph -> confirmDelete(graph) }
        )
        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter

        vm.graphs.observe(this) { list ->
            adapter.submitList(list)
            binding.emptyView.visibility =
                if (list.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        }

        binding.fab.setOnClickListener { showCreateDialog() }
        vm.loadAll()
    }

    override fun onResume() {
        super.onResume()
        vm.loadAll()
    }

    private fun showCreateDialog() {
        val input = EditText(this).apply {
            hint = "분기점 기록 이름"
            setSingleLine(true)
            setPadding(48, 32, 48, 16)
        }
        MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialog_Dark)
            .setTitle("새 분기점 기록 만들기")
            .setView(input)
            .setPositiveButton("만들기") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isEmpty()) {
                    TopBanner.show(this, "이름을 입력하세요")
                } else {
                    vm.createGraph(name)
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun confirmDelete(graph: BranchGraph) {
        MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialog_Dark)
            .setTitle("삭제 확인")
            .setMessage("「${graph.name}」을(를) 삭제할까요?\n이 작업은 되돌릴 수 없습니다.")
            .setPositiveButton("삭제") { _, _ -> vm.deleteGraph(graph.id) }
            .setNegativeButton("취소", null)
            .show()
    }
}

// ── Adapter ───────────────────────────────────────────────────────────────────
class GraphAdapter(
    private val onOpen: (BranchGraph) -> Unit,
    private val onDelete: (BranchGraph) -> Unit
) : RecyclerView.Adapter<GraphAdapter.VH>() {

    private var list: List<BranchGraph> = emptyList()
    private val fmt = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.KOREAN)

    fun submitList(l: List<BranchGraph>) { list = l; notifyDataSetChanged() }

    inner class VH(val binding: ItemGraphCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemGraphCardBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = list.size

    override fun onBindViewHolder(h: VH, pos: Int) {
        val g = list[pos]
        h.binding.tvName.text = g.name
        h.binding.tvMeta.text = "${g.nodeCount}개 분기점 · ${fmt.format(Date(g.createdAt))}"
        h.binding.tvPreview.text = g.startNode?.content?.take(60)
            ?.let { if (it.length < (g.startNode?.content?.length ?: 0)) "$it…" else it }
            ?: g.startNode?.let { "제목: ${it.title}" }
            ?: "내용 없음"
        h.binding.root.setOnClickListener { onOpen(g) }
        h.binding.btnDelete.setOnClickListener { onDelete(g) }
    }
}
