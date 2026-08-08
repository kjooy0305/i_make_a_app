package com.maybeitok.epubmaker.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.maybeitok.epubmaker.databinding.ItemEpubBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EpubListAdapter(
    private val onItemClick: (File) -> Unit,
    private val onItemLongClick: (File) -> Unit
) : RecyclerView.Adapter<EpubListAdapter.ViewHolder>() {

    private val items = mutableListOf<File>()

    fun submitList(list: List<File>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEpubBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(private val binding: ItemEpubBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(file: File) {
            binding.tvFilename.text = file.nameWithoutExtension
            val sizeKb = file.length() / 1024
            val sdf = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())
            val dateStr = sdf.format(Date(file.lastModified()))
            val source = if (file.absolutePath.contains("/files/library/")) "📚 라이브러리" else "⬇ 다운로드"
            binding.tvSubtitle.text = "$source · ${sizeKb}KB · $dateStr"

            binding.root.setOnClickListener { onItemClick(file) }
            binding.root.setOnLongClickListener {
                onItemLongClick(file)
                true
            }
        }
    }
}
