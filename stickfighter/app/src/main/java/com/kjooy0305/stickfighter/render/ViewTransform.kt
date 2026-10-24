package com.kjooy0305.stickfighter.render

import kotlin.math.max
import kotlin.math.min

/**
 * 도화지(논리 좌표)를 화면에 놓는 위치와 배율. 확대·축소·이동을 모두 여기서 계산한다.
 * insets는 도구 막대 등이 가리는 영역이다. 처음 맞춤 크기는 그 안쪽에 들어오게 잡는다.
 */
class ViewTransform {
    var viewW = 0f; private set
    var viewH = 0f; private set
    private var animW = 1000f
    private var animH = 562f
    private var insetL = 0f
    private var insetT = 0f
    private var insetR = 0f
    private var insetB = 0f
    private var margin = 0f

    var scale = 1f; private set
    var offsetX = 0f; private set
    var offsetY = 0f; private set

    /** 사용자가 직접 확대·이동했으면 크기가 바뀌어도 맞춤으로 되돌리지 않는다. */
    var adjusted = false; private set

    private var baseScale = 1f
    private var baseX = 0f
    private var baseY = 0f

    /** 맞춤 크기 대비 현재 배율 (1이면 맞춤). */
    val zoom: Float get() = if (baseScale == 0f) 1f else scale / baseScale

    fun setView(w: Float, h: Float) { viewW = w; viewH = h; relayout() }

    fun setPaper(w: Float, h: Float) { animW = w; animH = h; relayout() }

    fun setInsets(l: Float, t: Float, r: Float, b: Float, margin: Float = this.margin) {
        insetL = l; insetT = t; insetR = r; insetB = b; this.margin = margin; relayout()
    }

    private fun relayout() {
        val availW = max(1f, viewW - insetL - insetR - 2 * margin)
        val availH = max(1f, viewH - insetT - insetB - 2 * margin)
        baseScale = min(availW / animW, availH / animH)
        baseX = insetL + margin + (availW - animW * baseScale) / 2f
        baseY = insetT + margin + (availH - animH * baseScale) / 2f
        if (!adjusted) fit() else clampPan()
    }

    fun fit() {
        scale = baseScale; offsetX = baseX; offsetY = baseY
        adjusted = false
    }

    /** (fx, fy) 화면 좌표가 제자리에 머물도록 factor배 확대한다. */
    fun zoomAround(fx: Float, fy: Float, factor: Float) {
        val target = (scale * factor).coerceIn(baseScale * MIN_ZOOM, baseScale * MAX_ZOOM)
        val k = target / scale
        offsetX = fx - (fx - offsetX) * k
        offsetY = fy - (fy - offsetY) * k
        scale = target
        adjusted = true
        clampPan()
    }

    fun zoomCenter(factor: Float) = zoomAround(viewW / 2f, viewH / 2f, factor)

    fun panBy(dx: Float, dy: Float) {
        offsetX += dx; offsetY += dy
        adjusted = true
        clampPan()
    }

    // 도화지가 화면 밖으로 다 사라져 찾을 수 없게 되는 일을 막는다.
    private fun clampPan() {
        val keep = 96f
        val w = animW * scale
        val h = animH * scale
        offsetX = offsetX.coerceIn(keep - w, max(keep - w, viewW - keep))
        offsetY = offsetY.coerceIn(keep - h, max(keep - h, viewH - keep))
    }

    fun toFit() = Fit(scale, offsetX, offsetY)

    companion object {
        const val MIN_ZOOM = 0.5f
        const val MAX_ZOOM = 10f
    }
}
