package com.kjooy0305.stickfighter.model

import kotlin.math.floor

/** 키 프레임 사이를 이어 주는 보간. 같은 id를 가진 피규어와 마디끼리 짝지어 섞는다. */
object Interpolator {

    /** position은 0 이상 frames.size-1 이하. 정수면 그 키 프레임 그대로다. */
    fun at(frames: List<Frame>, position: Float): Frame {
        if (frames.isEmpty()) return Frame()
        if (frames.size == 1) return frames[0]
        val p = position.coerceIn(0f, (frames.size - 1).toFloat())
        val i = floor(p).toInt().coerceAtMost(frames.size - 2)
        return blend(frames[i], frames[i + 1], p - i)
    }

    fun blend(a: Frame, b: Frame, t: Float): Frame {
        if (t <= 0f) return a
        if (t >= 1f) return b
        val bById = b.figures.associateBy { it.id }
        val aIds = a.figures.map { it.id }.toSet()
        val figures = ArrayList<Figure>()
        for (fa in a.figures) {
            val fb = bById[fa.id]
            figures.add(if (fb == null) fa else blend(fa, fb, t))
        }
        // 새로 생긴 피규어는 절반 지점에서 나타난다.
        if (t >= 0.5f) figures.addAll(b.figures.filter { it.id !in aIds })
        return Frame(figures)
    }

    fun blend(a: Figure, b: Figure, t: Float): Figure {
        val bSegs = b.segments.associateBy { it.id }
        val aIds = a.segments.map { it.id }.toSet()
        val segments = ArrayList<Segment>()
        for (sa in a.segments) {
            val sb = bSegs[sa.id]
            segments.add(if (sb == null) sa else blend(sa, sb, t))
        }
        if (t >= 0.5f) segments.addAll(b.segments.filter { it.id !in aIds })
        return Figure(
            id = a.id,
            name = a.name,
            x = lerp(a.x, b.x, t),
            y = lerp(a.y, b.y, t),
            scale = lerp(a.scale, b.scale, t),
            flip = if (t < 0.5f) a.flip else b.flip,
            segments = segments
        )
    }

    private fun blend(a: Segment, b: Segment, t: Float): Segment = a.copy(
        angle = lerpAngle(a.angle, b.angle, t),
        length = lerp(a.length, b.length, t),
        thickness = lerp(a.thickness, b.thickness, t),
        color = lerpColor(a.color, b.color, t),
        shape = if (t < 0.5f) a.shape else b.shape
    )

    fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t

    /** 가까운 쪽으로 도는 각도 보간. */
    fun lerpAngle(a: Float, b: Float, t: Float): Float =
        Kinematics.normalize(a + Kinematics.normalize(b - a) * t)

    fun lerpColor(a: Int, b: Int, t: Float): Int {
        fun ch(shift: Int): Int {
            val ca = (a ushr shift) and 0xFF
            val cb = (b ushr shift) and 0xFF
            return (ca + (cb - ca) * t + 0.5f).toInt().coerceIn(0, 255)
        }
        return (ch(24) shl 24) or (ch(16) shl 16) or (ch(8) shl 8) or ch(0)
    }
}
