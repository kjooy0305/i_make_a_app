package com.kjooy0305.stickfighter.model

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class Pt(val x: Float, val y: Float)

/** 화면(논리 좌표)에 놓인 마디. absAngle은 반전 전 피규어 좌표계에서의 절대 각도다. */
data class Placed(val segment: Segment, val start: Pt, val end: Pt, val absAngle: Float) {
    val mid: Pt get() = Pt((start.x + end.x) / 2f, (start.y + end.y) / 2f)
}

object Kinematics {

    fun layout(figure: Figure): List<Placed> {
        val localEnd = HashMap<Int, Pt>()
        val absAngle = HashMap<Int, Float>()
        val result = ArrayList<Placed>(figure.segments.size)
        for (s in figure.segments) {
            val parentAngle = absAngle[s.parent] ?: 0f
            val ls = localEnd[s.parent] ?: Pt(0f, 0f)
            val a = parentAngle + s.angle
            val le = Pt(ls.x + cos(a) * s.length, ls.y + sin(a) * s.length)
            absAngle[s.id] = a
            localEnd[s.id] = le
            result.add(Placed(s, toWorld(figure, ls), toWorld(figure, le), a))
        }
        return result
    }

    fun toWorld(f: Figure, p: Pt): Pt =
        Pt(f.x + (if (f.flip) -p.x else p.x) * f.scale, f.y + p.y * f.scale)

    fun toLocal(f: Figure, w: Pt): Pt {
        val x = (w.x - f.x) / f.scale
        return Pt(if (f.flip) -x else x, (w.y - f.y) / f.scale)
    }

    /** 각도를 (-PI, PI] 범위로 맞춘다. */
    fun normalize(angle: Float): Float {
        var a = angle
        val twoPi = (2 * PI).toFloat()
        while (a > PI) a -= twoPi
        while (a <= -PI) a += twoPi
        return a
    }
}
