package com.kjooy0305.choicetheroute.model

import android.graphics.Paint
import android.graphics.Typeface

/**
 * 노드 상자의 크기 계산.
 *
 * 제목이 길어지면 상자도 함께 넓어진다. 캔버스 렌더링과 자동 정리가
 * 같은 값을 써야 하므로 한곳에 모아 둔다.
 */
object NodeMetrics {

    const val HEIGHT = 74f
    const val CORNER = 14f
    const val TITLE_SIZE = 36f

    private const val MIN_WIDTH = 160f
    private const val MAX_WIDTH = 620f
    private const val PAD_H = 30f

    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = TITLE_SIZE
        typeface = Typeface.DEFAULT_BOLD
    }

    fun widthFor(title: String): Float =
        (titlePaint.measureText(title) + PAD_H * 2).coerceIn(MIN_WIDTH, MAX_WIDTH)

    fun widthOf(node: BranchNode): Float = widthFor(node.title)

    /** MAX_WIDTH도 넘길 만큼 긴 제목만 말줄임한다. */
    fun displayTitle(title: String): String {
        if (titlePaint.measureText(title) + PAD_H * 2 <= MAX_WIDTH) return title
        var s = title
        while (s.length > 1 && titlePaint.measureText("$s…") + PAD_H * 2 > MAX_WIDTH) {
            s = s.dropLast(1)
        }
        return "$s…"
    }
}
