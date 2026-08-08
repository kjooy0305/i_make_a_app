package com.maybeitok.epubmaker.adapter

import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.maybeitok.epubmaker.databinding.ItemImageGalleryBinding
import com.maybeitok.epubmaker.model.ImageData
import java.io.File

data class GalleryImage(
    val imageData: ImageData,
    val chapterTitle: String,
    val chapterIndex: Int = 0
)

class ImageGalleryAdapter(
    private val images: List<GalleryImage>,
    private val onClick: (GalleryImage) -> Unit
) : RecyclerView.Adapter<ImageGalleryAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemImageGalleryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemImageGalleryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = images[position]
        val displayName = item.imageData.filename.substringAfterLast("__")
            .ifEmpty { item.imageData.filename }
        holder.binding.tvImageName.text = "${item.chapterTitle} · $displayName"
        holder.binding.imgThumb.setImageBitmap(null)
        holder.binding.imgThumb.setBackgroundColor(Color.parseColor("#F5F5F5"))

        Thread {
            try {
                val bitmap = when {
                    !item.imageData.localPath.isNullOrEmpty() && !isSvg(item.imageData.filename) -> {
                        val f = File(item.imageData.localPath!!)
                        if (f.exists()) BitmapFactory.decodeFile(f.absolutePath) else null
                    }
                    item.imageData.base64.isNotEmpty() && !isSvg(item.imageData.filename) -> {
                        val bytes = Base64.decode(item.imageData.base64, Base64.DEFAULT)
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    }
                    else -> null
                }
                holder.binding.imgThumb.post {
                    if (bitmap != null) {
                        holder.binding.imgThumb.setImageBitmap(bitmap)
                        holder.binding.imgThumb.setBackgroundColor(Color.TRANSPARENT)
                    } else {
                        // SVG 또는 미지원 포맷: 텍스트 플레이스홀더
                        val ext = item.imageData.filename.substringAfterLast('.').uppercase()
                        holder.binding.imgThumb.setBackgroundColor(Color.parseColor("#E8F4FD"))
                        holder.binding.tvImageName.text =
                            "[$ext] ${item.chapterTitle} · ${item.imageData.filename}"
                    }
                }
            } catch (_: Exception) {
                holder.binding.imgThumb.post {
                    holder.binding.imgThumb.setBackgroundColor(Color.parseColor("#FFEBEE"))
                }
            }
        }.start()

        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = images.size

    private fun isSvg(filename: String) = filename.endsWith(".svg", ignoreCase = true)
}
