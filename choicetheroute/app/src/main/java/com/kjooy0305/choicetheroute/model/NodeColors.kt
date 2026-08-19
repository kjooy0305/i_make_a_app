package com.kjooy0305.choicetheroute.model

/**
 * 분기점 상자에 입힐 수 있는 색.
 *
 * 편집 화면의 색상 팔레트와 캔버스 렌더링이 같은 목록을 봐야 하므로
 * 한곳에 모아 둔다. 저장에는 ARGB 정수를 그대로 쓴다.
 */
object NodeColors {

    /** 색을 지정하지 않은 상태 */
    const val NONE = 0

    val PALETTE = listOf(
        NONE,
        0xFF3A8AEE.toInt(),  // 파랑
        0xFF4DCEA0.toInt(),  // 초록
        0xFFFFB300.toInt(),  // 노랑
        0xFFFF7043.toInt(),  // 주황
        0xFFE84040.toInt(),  // 빨강
        0xFFB06AF0.toInt(),  // 보라
        0xFF00BCD4.toInt()   // 청록
    )

    /** 기본 상자 배경 (colors.xml의 card와 같은 값) */
    private const val BASE = 0xFF111B2B.toInt()

    /** 상자 배경용으로 어두운 바탕에 색을 옅게 섞는다 — 글자가 계속 읽히도록 */
    fun tint(color: Int): Int = blend(BASE, color, 0.28f)

    private fun blend(a: Int, b: Int, ratio: Float): Int {
        val inv = 1f - ratio
        val r = ((a shr 16 and 0xFF) * inv + (b shr 16 and 0xFF) * ratio).toInt()
        val g = ((a shr 8 and 0xFF) * inv + (b shr 8 and 0xFF) * ratio).toInt()
        val bl = ((a and 0xFF) * inv + (b and 0xFF) * ratio).toInt()
        return (0xFF shl 24) or (r shl 16) or (g shl 8) or bl
    }
}
