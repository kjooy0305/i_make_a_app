package com.maybeitok.epubmaker.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.maybeitok.epubmaker.databinding.ItemFontBinding
import com.maybeitok.epubmaker.model.CustomFont

class FontAdapter(
    private val onItemClick: (CustomFont) -> Unit,
    private val onItemLongClick: (CustomFont) -> Unit
) : RecyclerView.Adapter<FontAdapter.ViewHolder>() {

    private val items = mutableListOf<CustomFont>()

    fun submitList(list: List<CustomFont>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    fun getItems(): List<CustomFont> = items.toList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFontBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(private val binding: ItemFontBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(font: CustomFont) {
            binding.tvFontName.text = font.name
            binding.tvFontPath.text = font.filePath
            binding.root.setOnClickListener { onItemClick(font) }
            binding.root.setOnLongClickListener {
                onItemLongClick(font)
                true
            }
        }
    }
}
