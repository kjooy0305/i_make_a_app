package com.example.repeatalarm.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.repeatalarm.data.AlarmGroupWithItems
import com.example.repeatalarm.databinding.ItemAlarmGroupBinding

class AlarmGroupAdapter(
    private val onEdit: (AlarmGroupWithItems) -> Unit,
    private val onDelete: (AlarmGroupWithItems) -> Unit,
    private val onToggle: (AlarmGroupWithItems) -> Unit
) : ListAdapter<AlarmGroupWithItems, AlarmGroupAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(private val b: ItemAlarmGroupBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(gw: AlarmGroupWithItems) {
            val group = gw.group
            val items = gw.items

            b.tvGroupName.text = group.name
            b.switchEnabled.isChecked = group.isEnabled

            if (items.isEmpty()) {
                b.tvItemSummary.text = "알림 항목 없음"
                b.tvItemPreview.visibility = View.GONE
            } else {
                b.tvItemSummary.text = "${items.size}개 알림 항목"
                b.tvItemPreview.visibility = View.VISIBLE
                b.tvItemPreview.text = items.take(3).joinToString("\n") { item ->
                    "• ${item.startLabel()}~${item.endLabel()} ${item.intervalLabel()} — ${item.content}"
                }
            }

            b.switchEnabled.setOnCheckedChangeListener(null)
            b.switchEnabled.setOnCheckedChangeListener { _, _ -> onToggle(gw) }
            b.btnEdit.setOnClickListener { onEdit(gw) }
            b.btnDelete.setOnClickListener { onDelete(gw) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val b = ItemAlarmGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(b)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<AlarmGroupWithItems>() {
            override fun areItemsTheSame(a: AlarmGroupWithItems, b: AlarmGroupWithItems) =
                a.group.id == b.group.id
            override fun areContentsTheSame(a: AlarmGroupWithItems, b: AlarmGroupWithItems) =
                a == b
        }
    }
}
