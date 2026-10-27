package com.kjooy0305.dayclean.logic

import com.kjooy0305.dayclean.model.RestInterval
import com.kjooy0305.dayclean.model.Tz
import java.time.LocalDateTime

/** 알림 쉬는 시간. 구간마다 요일이 다르고, 자정을 넘길 수 있다. */
object Rests {
    /** 시각 t가 쉬는 시간 안이면 그 구간이 끝나는 시각, 아니면 null. 이어진 구간은 끝까지 따라간다. */
    fun endIfResting(rests: List<RestInterval>, tMs: Long): Long? {
        var t = tMs
        var moved = false
        // 구간이 연달아 겹쳐 있어도 끝나는 곳까지 따라가되, 무한 반복은 막는다.
        repeat(20) {
            val end = oneEnd(rests, t) ?: return if (moved) t else null
            moved = true
            t = end
        }
        return t
    }

    private fun oneEnd(rests: List<RestInterval>, tMs: Long): Long? {
        val t = Dates.dateTime(tMs)
        var best: Long? = null
        for (r in rests) {
            if (r.startMin == r.endMin) continue
            for (offset in 0..1) {
                val day = t.toLocalDate().minusDays(offset.toLong())
                if (day.dayOfWeek !in r.days) continue
                val start = day.atTime(r.startMin / 60, r.startMin % 60)
                val end: LocalDateTime = if (r.endMin > r.startMin) day.atTime(r.endMin / 60, r.endMin % 60)
                else day.plusDays(1).atTime(r.endMin / 60, r.endMin % 60)
                if (!t.isBefore(start) && t.isBefore(end)) {
                    val endMs = Dates.toMillis(end)
                    if (best == null || endMs > best) best = endMs
                }
            }
        }
        return best
    }

    fun isResting(rests: List<RestInterval>, tMs: Long): Boolean = endIfResting(rests, tMs) != null

    /** 쉬는 시간에 걸리는 알림 시각은 구간이 끝나는 때로 미룬다. */
    fun effective(rests: List<RestInterval>, tMs: Long): Long = endIfResting(rests, tMs) ?: tMs

    @Suppress("unused")
    private val zone get() = Tz.zone
}
