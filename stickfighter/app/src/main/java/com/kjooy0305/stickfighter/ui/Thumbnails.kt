package com.kjooy0305.stickfighter.ui

import android.graphics.Bitmap
import com.kjooy0305.stickfighter.model.Animation
import com.kjooy0305.stickfighter.model.Figure
import com.kjooy0305.stickfighter.model.Frame
import com.kjooy0305.stickfighter.model.Kinematics
import com.kjooy0305.stickfighter.model.Shape
import com.kjooy0305.stickfighter.render.FrameRenderer
import kotlin.math.max
import kotlin.math.min

object Thumbnails {
    private val renderer = FrameRenderer()

    fun animation(a: Animation, width: Int = 256): Bitmap =
        renderer.thumbnail(a, a.frames.first(), width)

    /** 피규어 전체가 보이도록 크기와 위치를 맞춰 그린다. */
    fun figure(f: Figure, width: Int = 256): Bitmap {
        val canvas = Animation("thumb", f.name)
        val unit = f.copy(x = 0f, y = 0f, scale = 1f, flip = false)
        var minX = 0f; var maxX = 0f; var minY = 0f; var maxY = 0f
        for (p in Kinematics.layout(unit)) {
            val r = if (p.segment.shape == Shape.LINE) 0f else p.segment.length / 2f
            val pad = p.segment.thickness / 2f + r
            val cx = if (r > 0f) p.mid.x else 0f
            val cy = if (r > 0f) p.mid.y else 0f
            for (pt in listOf(p.start, p.end, com.kjooy0305.stickfighter.model.Pt(cx, cy))) {
                minX = min(minX, pt.x - pad); maxX = max(maxX, pt.x + pad)
                minY = min(minY, pt.y - pad); maxY = max(maxY, pt.y + pad)
            }
        }
        val w = max(maxX - minX, 1f)
        val h = max(maxY - minY, 1f)
        val scale = min(canvas.width * 0.8f / w, canvas.height * 0.8f / h)
        val placed = unit.copy(
            x = canvas.width / 2f - (minX + maxX) / 2f * scale,
            y = canvas.height / 2f - (minY + maxY) / 2f * scale,
            scale = scale
        )
        return renderer.thumbnail(canvas, Frame(listOf(placed)), width)
    }
}
