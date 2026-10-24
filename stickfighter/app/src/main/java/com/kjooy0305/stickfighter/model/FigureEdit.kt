package com.kjooy0305.stickfighter.model

import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.max

/** 피규어 하나를 고치는 순수 함수 모음. 항상 새 Figure를 돌려준다. */
object FigureEdit {
    const val MIN_LENGTH = 4f
    const val MIN_SCALE = 0.2f
    const val MAX_SCALE = 6f
    const val MIN_THICKNESS = 1f
    const val MAX_THICKNESS = 40f

    fun move(f: Figure, to: Pt): Figure = f.copy(x = to.x, y = to.y)

    /** 마디의 끝이 target을 향하도록 돌린다. 자식 마디는 상대 각도를 유지한 채 따라온다. */
    fun pose(f: Figure, segmentId: Int, target: Pt): Figure {
        val placed = Kinematics.layout(f).firstOrNull { it.segment.id == segmentId } ?: return f
        val start = Kinematics.toLocal(f, placed.start)
        val t = Kinematics.toLocal(f, target)
        val abs = atan2(t.y - start.y, t.x - start.x)
        val parentAbs = placed.absAngle - placed.segment.angle
        val rel = Kinematics.normalize(abs - parentAbs)
        return replace(f, segmentId) { it.copy(angle = rel) }
    }

    /** 마디의 끝을 target까지 늘이거나 줄인다. 방향은 target 쪽으로 바뀐다. */
    fun stretch(f: Figure, segmentId: Int, target: Pt): Figure {
        val placed = Kinematics.layout(f).firstOrNull { it.segment.id == segmentId } ?: return f
        val start = Kinematics.toLocal(f, placed.start)
        val t = Kinematics.toLocal(f, target)
        val length = max(MIN_LENGTH, hypot(t.x - start.x, t.y - start.y))
        val abs = atan2(t.y - start.y, t.x - start.x)
        val parentAbs = placed.absAngle - placed.segment.angle
        return replace(f, segmentId) { it.copy(length = length, angle = Kinematics.normalize(abs - parentAbs)) }
    }

    fun flip(f: Figure): Figure = f.copy(flip = !f.flip)

    fun scaleBy(f: Figure, factor: Float): Figure =
        f.copy(scale = (f.scale * factor).coerceIn(MIN_SCALE, MAX_SCALE))

    /** segmentId가 null이면 피규어 전체에 적용한다. */
    fun style(f: Figure, segmentId: Int?, color: Int, thickness: Float): Figure {
        val t = thickness.coerceIn(MIN_THICKNESS, MAX_THICKNESS)
        return f.copy(segments = f.segments.map {
            if (segmentId == null || it.id == segmentId) it.copy(color = color, thickness = t) else it
        })
    }

    /** parentId(-1이면 기준점)에 새 마디를 붙인다. */
    fun addSegment(f: Figure, parentId: Int, shape: Shape, length: Float, color: Int, thickness: Float): Figure {
        val id = (f.segments.maxOfOrNull { it.id } ?: -1) + 1
        val seg = Segment(id, parentId, angleHint(f, parentId), length, thickness, color, shape)
        return f.copy(segments = f.segments + seg)
    }

    /** 마디와 그 아래 달린 모든 마디를 지운다. 마지막 하나 남은 마디는 지우지 않는다. */
    fun removeSegment(f: Figure, segmentId: Int): Figure {
        val doomed = HashSet<Int>()
        doomed.add(segmentId)
        for (s in f.segments) if (s.parent in doomed) doomed.add(s.id)
        val rest = f.segments.filter { it.id !in doomed }
        return if (rest.isEmpty()) f else f.copy(segments = rest)
    }

    private fun angleHint(f: Figure, parentId: Int): Float {
        // 새 마디가 기존 마디와 겹쳐 보이지 않도록 형제 수만큼 비튼다.
        val siblings = f.segments.count { it.parent == parentId }
        return Kinematics.normalize(0.6f + siblings * 0.9f)
    }

    private fun replace(f: Figure, id: Int, change: (Segment) -> Segment): Figure =
        f.copy(segments = f.segments.map { if (it.id == id) change(it) else it })
}
