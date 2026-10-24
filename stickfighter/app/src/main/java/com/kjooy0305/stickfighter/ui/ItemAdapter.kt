package com.kjooy0305.stickfighter.ui

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kjooy0305.stickfighter.databinding.ItemAnimationBinding

/** 홈 목록 한 줄. 애니메이션과 내 피규어가 같은 모양을 쓴다. */
class Row(val key: String, val title: String, val meta: String, val thumb: Bitmap)

class ItemAdapter(
    private val onOpen: (Row) -> Unit,
    private val onMenu: (Row, View) -> Unit
) : RecyclerView.Adapter<ItemAdapter.Holder>() {

    private var rows: List<Row> = emptyList()

    fun submit(rows: List<Row>) {
        this.rows = rows
        notifyDataSetChanged()
    }

    override fun getItemCount() = rows.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(ItemAnimationBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val row = rows[position]
        holder.b.thumb.setImageBitmap(row.thumb)
        holder.b.title.text = row.title
        holder.b.meta.text = row.meta
        holder.b.meta.visibility = if (row.meta.isEmpty()) View.GONE else View.VISIBLE
        holder.b.root.setOnClickListener { onOpen(row) }
        holder.b.root.setOnLongClickListener { onMenu(row, holder.b.more); true }
        holder.b.more.setOnClickListener { onMenu(row, it) }
    }

    class Holder(val b: ItemAnimationBinding) : RecyclerView.ViewHolder(b.root)
}
