package com.maybeitok.epubmaker.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.maybeitok.epubmaker.R
import com.maybeitok.epubmaker.databinding.ItemChapterBinding
import com.maybeitok.epubmaker.model.EpubChapter

class ChapterAdapter(
    private val onItemClick: (Int) -> Unit,       // original index
    private val onItemLongClick: (Int) -> Unit = {}
) : RecyclerView.Adapter<ChapterAdapter.ViewHolder>() {

    private val allItems = mutableListOf<EpubChapter>()
    private val displayed = mutableListOf<Pair<Int, EpubChapter>>() // originalIndex, chapter
    private var selectedIndex: Int = 0

    fun submitList(list: List<EpubChapter>, selectedIdx: Int = selectedIndex) {
        allItems.clear()
        allItems.addAll(list)
        selectedIndex = selectedIdx
        applyFilter(currentQuery)
    }

    fun setSelectedIndex(index: Int) {
        selectedIndex = index
        notifyDataSetChanged()
    }

    private var currentQuery = ""

    fun filter(query: String) {
        currentQuery = query
        applyFilter(query)
    }

    private fun applyFilter(query: String) {
        displayed.clear()
        if (query.isBlank()) {
            allItems.forEachIndexed { idx, ch -> displayed.add(idx to ch) }
        } else {
            allItems.forEachIndexed { idx, ch ->
                if (ch.title.contains(query, ignoreCase = true)) displayed.add(idx to ch)
            }
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (originalIdx, chapter) = displayed[position]
        holder.bind(chapter, originalIdx == selectedIndex, originalIdx)
    }

    override fun getItemCount() = displayed.size

    inner class ViewHolder(private val binding: ItemChapterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(chapter: EpubChapter, isSelected: Boolean, originalIdx: Int) {
            binding.tvChapterTitle.text = chapter.title
            val ctx = binding.root.context
            if (isSelected) {
                binding.root.setBackgroundColor(ContextCompat.getColor(ctx, R.color.colorPrimary))
                binding.tvChapterTitle.setTextColor(ContextCompat.getColor(ctx, R.color.white))
            } else {
                binding.root.setBackgroundColor(ContextCompat.getColor(ctx, R.color.white))
                binding.tvChapterTitle.setTextColor(ContextCompat.getColor(ctx, R.color.black))
            }
            binding.root.setOnClickListener { onItemClick(originalIdx) }
            binding.root.setOnLongClickListener { onItemLongClick(originalIdx); true }
        }
    }
}
