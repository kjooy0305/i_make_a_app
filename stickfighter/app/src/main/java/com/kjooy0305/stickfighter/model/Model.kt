package com.kjooy0305.stickfighter.model

/** 도화지의 논리 좌표 폭. 높이는 비율(ratio)로 정해진다. */
const val LOGICAL_WIDTH = 1000f

enum class Shape {
    /** 시작점에서 끝점까지 긋는 선 */
    LINE,

    /** 시작점과 끝점을 지름으로 하는 빈 원 (머리 등) */
    RING,

    /** 시작점과 끝점을 지름으로 하는 채운 원 */
    DISC
}

/**
 * 피규어를 이루는 한 마디.
 * angle은 부모 마디의 방향에 대한 상대 각도(라디안)이고, 부모가 없으면(-1) 피규어 기준점에서 시작하는 절대 각도다.
 * 0은 오른쪽, 화면 좌표(y가 아래로 증가)를 쓰므로 -PI/2가 위쪽이다.
 */
data class Segment(
    val id: Int,
    val parent: Int,
    val angle: Float,
    val length: Float,
    val thickness: Float,
    val color: Int,
    val shape: Shape = Shape.LINE
)

/** 한 프레임 안의 피규어 하나. segments는 부모가 항상 자식보다 앞에 오도록 정렬된다. */
data class Figure(
    val id: Long,
    val name: String,
    val x: Float,
    val y: Float,
    val scale: Float = 1f,
    val flip: Boolean = false,
    val segments: List<Segment>
)

data class Frame(val figures: List<Figure> = emptyList())

data class Animation(
    val id: String,
    val title: String,
    /** 높이 / 폭 */
    val ratio: Float = 9f / 16f,
    val background: Int = 0xFFFFFFFF.toInt(),
    /** 초당 키 프레임 수(애니메이션 속도) */
    val keyFramesPerSecond: Float = 2f,
    /** 재생과 영상에서 초당 그리는 장면 수(부드러움) */
    val fps: Int = 30,
    val repeat: Boolean = false,
    val frames: List<Frame> = listOf(Frame()),
    val updatedAt: Long = 0L
) {
    val width: Float get() = LOGICAL_WIDTH
    val height: Float get() = LOGICAL_WIDTH * ratio

    /** 처음부터 끝까지 재생하는 데 걸리는 시간(초) */
    val durationSeconds: Float
        get() = if (frames.size < 2) 0f else (frames.size - 1) / keyFramesPerSecond
}
