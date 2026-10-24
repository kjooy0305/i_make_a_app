package com.kjooy0305.stickfighter.ui

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kjooy0305.stickfighter.databinding.ItemFrameBinding
import com.kjooy0305.stickfighter.model.Animation
import com.kjooy0305.stickfighter.render.FrameRenderer
import android.view.View

/** 편집기 아래의 키 프레임 목록. 마지막 칸은 프레임 추가 버튼이다. */
class FrameStripAdapter(
    private val onSelect: (Int) -> Unit,
    private val onAdd: () -> Unit,
    private val onMenu: (Int, View) -> Unit,
    private val showAdd: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val renderer = FrameRenderer()
    private var animation: Animation = Animation("", "")
    private var selected = 0

    // 프레임은 바뀔 때마다 새 객체가 되므로 객체 정체성으로 썸네일을 재사용한다.
    private val thumbs = java.util.IdentityHashMap<Any, Bitmap>()

    fun submit(animation: Animation, selected: Int) {
        this.animation = animation
        this.selected = selected
        thumbs.keys.retainAll(animation.frames.toSet())
        notifyDataSetChanged()
    }

    override fun getItemCount() = animation.frames.size + if (showAdd) 1 else 0

    override fun getItemViewType(position: Int) = if (position < animation.frames.size) 0 else 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == 0) {
            FrameHolder(ItemFrameBinding.inflate(inflater, parent, false))
        } else {
            object : RecyclerView.ViewHolder(
                inflater.inflate(com.kjooy0305.stickfighter.R.layout.item_frame_add, parent, false)
            ) {}
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is FrameHolder) {
            val frame = animation.frames[position]
            val bmp = thumbs.getOrPut(frame) { renderer.thumbnail(animation, frame, 232) }
            holder.b.thumb.setImageBitmap(bmp)
            holder.b.number.text = (position + 1).toString()
            holder.b.root.isSelected = position == selected
            holder.b.root.setOnClickListener { onSelect(holder.bindingAdapterPosition) }
            holder.b.root.setOnLongClickListener {
                onMenu(holder.bindingAdapterPosition, it)
                true
            }
        } else {
            holder.itemView.setOnClickListener { onAdd() }
        }
    }

    class FrameHolder(val b: ItemFrameBinding) : RecyclerView.ViewHolder(b.root)
}
