package com.kjooy0305.apkextractor

import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kjooy0305.apkextractor.databinding.ItemAppBinding

class AppAdapter(private val onSelectionChanged: () -> Unit) :
    RecyclerView.Adapter<AppAdapter.Holder>() {

    private var items: List<AppItem> = emptyList()
    val selected = linkedSetOf<String>()

    fun submit(list: List<AppItem>) {
        items = list
        notifyDataSetChanged()
    }

    fun selectAllVisible() {
        items.forEach { selected.add(it.packageName) }
        notifyDataSetChanged()
        onSelectionChanged()
    }

    fun clearSelection() {
        selected.clear()
        notifyDataSetChanged()
        onSelectionChanged()
    }

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(ItemAppBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = items[position]
        val b = holder.binding
        val ctx = b.root.context
        b.label.text = item.label
        b.pkg.text = item.packageName
        val size = Formatter.formatShortFileSize(ctx, item.sizeBytes)
        val split = if (item.isSplit) " · APK ${item.splitDirs.size + 1}개 → .apks" else ""
        b.meta.text = ctx.getString(R.string.item_meta, item.versionName, size) + split
        b.icon.setImageDrawable(item.icon)
        b.check.isChecked = item.packageName in selected
        b.root.setOnClickListener {
            if (!selected.remove(item.packageName)) selected.add(item.packageName)
            b.check.isChecked = item.packageName in selected
            onSelectionChanged()
        }
    }

    class Holder(val binding: ItemAppBinding) : RecyclerView.ViewHolder(binding.root)
}
