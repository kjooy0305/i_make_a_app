package com.kjooy0305.stickfighter.render

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import com.kjooy0305.stickfighter.model.Animation
import com.kjooy0305.stickfighter.model.Frame
import com.kjooy0305.stickfighter.model.Kinematics
import com.kjooy0305.stickfighter.model.Shape
import kotlin.math.abs
import kotlin.math.min

/** 논리 좌표(폭 1000)를 뷰 크기에 맞추는 값. 남는 곳은 가장자리로 둔다. */
class Fit(val scale: Float, val offsetX: Float, val offsetY: Float) {
    fun toView(x: Float) = x * scale + offsetX
    fun toViewY(y: Float) = y * scale + offsetY
    fun toLogicalX(vx: Float) = (vx - offsetX) / scale
    fun toLogicalY(vy: Float) = (vy - offsetY) / scale

    companion object {
        fun of(viewW: Float, viewH: Float, a: Animation, margin: Float = 0f): Fit {
            val s = min((viewW - 2 * margin) / a.width, (viewH - 2 * margin) / a.height)
            return Fit(s, (viewW - a.width * s) / 2f, (viewH - a.height * s) / 2f)
        }
    }
}

/** 한 장면을 캔버스에 그린다. 편집기, 썸네일, 재생, 영상이 모두 이 그리기를 쓴다. */
class FrameRenderer {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    /** 도화지(0,0)-(width,height)를 fit에 맞춰 그린다. */
    fun draw(canvas: Canvas, animation: Animation, frame: Frame, fit: Fit) {
        val save = canvas.save()
        canvas.translate(fit.offsetX, fit.offsetY)
        canvas.scale(fit.scale, fit.scale)
        paint.style = Paint.Style.FILL
        paint.color = animation.background
        canvas.drawRect(0f, 0f, animation.width, animation.height, paint)
        canvas.clipRect(0f, 0f, animation.width, animation.height)
        for (figure in frame.figures) {
            for (p in Kinematics.layout(figure)) {
                val s = p.segment
                paint.color = s.color
                val w = s.thickness * figure.scale
                when (s.shape) {
                    Shape.LINE -> {
                        paint.style = Paint.Style.STROKE
                        paint.strokeWidth = w
                        canvas.drawLine(p.start.x, p.start.y, p.end.x, p.end.y, paint)
                    }
                    Shape.RING, Shape.DISC -> {
                        val r = abs(s.length * figure.scale) / 2f
                        paint.style = if (s.shape == Shape.DISC) Paint.Style.FILL else Paint.Style.STROKE
                        paint.strokeWidth = w
                        canvas.drawCircle(p.mid.x, p.mid.y, r, paint)
                    }
                }
            }
        }
        canvas.restoreToCount(save)
    }

    /** 썸네일 등으로 쓸 작은 비트맵. */
    fun thumbnail(animation: Animation, frame: Frame, width: Int): Bitmap {
        val height = (width * animation.ratio).toInt().coerceAtLeast(1)
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        draw(Canvas(bmp), animation, frame, Fit.of(width.toFloat(), height.toFloat(), animation))
        return bmp
    }
}
