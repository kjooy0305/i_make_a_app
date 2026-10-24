package com.kjooy0305.stickfighter.model

import kotlin.math.PI

/** 기본으로 제공하는 피규어 틀. */
object Templates {
    const val DEFAULT_COLOR = 0xFF222222.toInt()

    data class Template(val key: String, val build: (id: Long, x: Float, y: Float) -> Figure)

    val all: List<Template> = listOf(
        Template("stickman") { id, x, y -> stickman(id, x, y, headLength = 50f) },
        Template("bighead") { id, x, y -> stickman(id, x, y, headLength = 84f) },
        Template("circle") { id, x, y ->
            Figure(id, "circle", x, y, segments = listOf(Segment(0, -1, 0f, 90f, 6f, DEFAULT_COLOR, Shape.RING)))
        },
        Template("disc") { id, x, y ->
            Figure(id, "disc", x, y, segments = listOf(Segment(0, -1, 0f, 60f, 6f, DEFAULT_COLOR, Shape.DISC)))
        },
        Template("line") { id, x, y ->
            Figure(id, "line", x, y, segments = listOf(Segment(0, -1, 0f, 160f, 8f, DEFAULT_COLOR)))
        },
        Template("sword") { id, x, y -> sword(id, x, y) }
    )

    fun create(key: String, id: Long, x: Float, y: Float): Figure =
        (all.firstOrNull { it.key == key } ?: all.first()).build(id, x, y)

    /** 허리가 기준점인 막대 인간. 팔다리는 두 마디씩이다. */
    private fun stickman(id: Long, x: Float, y: Float, headLength: Float): Figure {
        val b = Builder()
        val torso = b.add(-1, -90f, 90f, 8f)
        b.add(torso, -90f, headLength, 7f, Shape.RING)
        val armL = b.add(torso, 150f, 46f, 7f)
        b.add(armL, 120f, 46f, 7f)
        val armR = b.add(torso, 30f, 46f, 7f)
        b.add(armR, 60f, 46f, 7f)
        val legL = b.add(-1, 105f, 56f, 8f)
        b.add(legL, 100f, 56f, 8f)
        val legR = b.add(-1, 75f, 56f, 8f)
        b.add(legR, 80f, 56f, 8f)
        return Figure(id, if (headLength > 60f) "bighead" else "stickman", x, y, segments = b.segments)
    }

    private fun sword(id: Long, x: Float, y: Float): Figure {
        val wood = 0xFF5B3A1E.toInt()
        val b = Builder()
        val handle = b.add(-1, -90f, 40f, 9f, color = wood)
        b.add(handle, 0f, 28f, 9f, color = wood)
        b.add(handle, 180f, 28f, 9f, color = wood)
        b.add(handle, -90f, 130f, 7f, color = 0xFF7A8794.toInt())
        return Figure(id, "sword", x, y, segments = b.segments)
    }

    /** 각도를 도(degree) 절대값으로 받아 부모 기준 상대 각도로 바꿔 쌓는 도우미. */
    private class Builder {
        val segments = ArrayList<Segment>()
        private val absolute = HashMap<Int, Float>()

        fun add(parent: Int, absDegrees: Float, length: Float, thickness: Float,
                shape: Shape = Shape.LINE, color: Int = DEFAULT_COLOR): Int {
            val abs = (absDegrees * PI / 180.0).toFloat()
            val rel = Kinematics.normalize(abs - (absolute[parent] ?: 0f))
            val id = segments.size
            segments.add(Segment(id, parent, rel, length, thickness, color, shape))
            absolute[id] = abs
            return id
        }
    }
}
