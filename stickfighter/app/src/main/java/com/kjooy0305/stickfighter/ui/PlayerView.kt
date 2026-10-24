package com.kjooy0305.stickfighter.ui

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import com.kjooy0305.stickfighter.model.Animation
import com.kjooy0305.stickfighter.model.Interpolator
import com.kjooy0305.stickfighter.render.Fit
import com.kjooy0305.stickfighter.render.FrameRenderer

/** 키 프레임 위치(0..프레임 수-1)를 받아 보간된 장면을 그린다. */
class PlayerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private val renderer = FrameRenderer()

    var animation: Animation = Animation("", "")
        set(value) { field = value; invalidate() }

    var position: Float = 0f
        set(value) { field = value; invalidate() }

    override fun onDraw(canvas: Canvas) {
        val fit = Fit.of(width.toFloat(), height.toFloat(), animation)
        renderer.draw(canvas, animation, Interpolator.at(animation.frames, position), fit)
    }
}
