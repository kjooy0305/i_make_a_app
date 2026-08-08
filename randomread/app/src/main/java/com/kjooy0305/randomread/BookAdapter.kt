package com.kjooy0305.randomread

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kjooy0305.randomread.databinding.ItemBookBinding
import com.kjooy0305.randomread.databinding.ItemSectionHeaderBinding

class BookAdapter(
    private val onLongClick: (BookItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items: List<ListItem> = emptyList()

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_BOOK = 1
    }

    fun submit(newItems: List<ListItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int) =
        if (items[position] is ListItem.Header) TYPE_HEADER else TYPE_BOOK

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inf = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_HEADER)
            HeaderVH(ItemSectionHeaderBinding.inflate(inf, parent, false))
        else
            BookVH(ItemBookBinding.inflate(inf, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ListItem.Header -> (holder as HeaderVH).bind(item)
            is ListItem.Book   -> (holder as BookVH).bind(item)
        }
    }

    inner class HeaderVH(private val b: ItemSectionHeaderBinding) :
        RecyclerView.ViewHolder(b.root) {
        fun bind(h: ListItem.Header) {
            b.tvType.text = h.type.label
            b.tvCount.text = "${h.count}${h.type.unit}"
            val color = if (h.type == BookType.EPUB) 0xFFA78BFA.toInt() else 0xFF34D399.toInt()
            b.tvType.setTextColor(color)
            b.tvCount.setTextColor(color)
            b.accent.setBackgroundColor(color)
        }
    }

    inner class BookVH(private val b: ItemBookBinding) :
        RecyclerView.ViewHolder(b.root) {
        fun bind(item: ListItem.Book) {
            b.tvIndex.text = item.index.toString()
            b.tvName.text = item.item.name
            b.root.setOnLongClickListener { onLongClick(item.item); true }
        }
    }
}
