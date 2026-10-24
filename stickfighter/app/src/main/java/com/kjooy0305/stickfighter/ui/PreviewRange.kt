package com.kjooy0305.stickfighter.ui

/** 미리보기 창이 되풀이해서 보여 줄 키 프레임 구간. 값은 보간 위치(0..프레임 수-1)다. */
object PreviewRange {
    enum class Scope { TRANSITION, ALL }

    class Range(val start: Float, val end: Float) {
        val length: Float get() = end - start
    }

    /** TRANSITION: 지금 장면에서 다음 장면으로. 마지막 장면이면 이전 장면에서 지금 장면으로. */
    fun of(frameCount: Int, index: Int, scope: Scope): Range {
        if (frameCount < 2) return Range(0f, 0f)
        return when (scope) {
            Scope.ALL -> Range(0f, (frameCount - 1).toFloat())
            Scope.TRANSITION -> {
                val i = index.coerceIn(0, frameCount - 1)
                if (i < frameCount - 1) Range(i.toFloat(), (i + 1).toFloat()) else Range((i - 1).toFloat(), i.toFloat())
            }
        }
    }
}
