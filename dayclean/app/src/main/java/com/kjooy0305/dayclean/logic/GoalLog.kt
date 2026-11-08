package com.kjooy0305.dayclean.logic

import com.kjooy0305.dayclean.model.Goal
import com.kjooy0305.dayclean.model.GoalDoneRec
import com.kjooy0305.dayclean.model.GoalRec
import com.kjooy0305.dayclean.model.ResolvedGoal
import java.time.LocalDate
import java.time.YearMonth

/**
 * 장기 목표의 메모리 기록 규칙.
 * - 한 달 넘게 남았을 때: 목표일에서 한 달씩 거슬러 올라간 날(D-92, D-61, D-31 …)
 * - 한 달 이내: 1주일마다(D-28, D-21, D-14, D-7, D-DAY)
 * - 목표일이 지나면 해결할 때까지 매일(D+1, D+2 …)
 * 해결하면 그날 작성·해결 날짜와 시간, 걸린 시간을 남긴다.
 */
object GoalLog {
    private const val MAX_MONTHS = 24

    /** 같은 날짜의 k달 전. 그 달에 없는 날짜면 말일. */
    fun minusMonths(target: LocalDate, k: Int): LocalDate {
        val ym = YearMonth.from(target).minusMonths(k.toLong())
        return ym.atDay(minOf(target.dayOfMonth, ym.lengthOfMonth()))
    }

    fun createdOf(g: Goal): LocalDate = g.createdOn

    fun isMark(created: LocalDate, target: LocalDate, day: LocalDate): Boolean {
        if (day.isBefore(created)) return false
        if (day.isAfter(target)) return true
        var first: LocalDate? = null
        for (k in 1..MAX_MONTHS) {
            val md = minusMonths(target, k)
            if (md.isBefore(created)) break
            if (k == 1) first = md
            if (md == day) return true
        }
        val limit = first ?: target.minusDays(28)
        return day.isAfter(limit) && Dates.daysBetween(day, target) % 7 == 0L
    }

    /** 이 날 메모리에 남길 목표 기록. */
    fun marks(goals: List<Goal>, day: LocalDate): List<GoalRec> =
        goals.filter { isMark(it.createdOn, it.target, day) }
            .map { GoalRec(Dates.daysBetween(day, it.target).toInt(), it.text) }

    fun writtenAt(g: Goal): Long = if (g.createdAt > 0) g.createdAt else Dates.startOfDay(g.createdOn)

    fun stamp(ms: Long): String {
        val t = Dates.dateTime(ms)
        return "%04d-%02d-%02d %02d:%02d".format(t.year, t.monthValue, t.dayOfMonth, t.hour, t.minute)
    }

    fun span(fromMs: Long, toMs: Long): String {
        val m = maxOf(0L, (toMs - fromMs) / 60000)
        val d = m / 1440
        val h = (m % 1440) / 60
        return (if (d > 0) "${d}일 " else "") + "${h}시간"
    }

    fun doneRec(r: ResolvedGoal): GoalDoneRec {
        val w = writtenAt(r.goal)
        return GoalDoneRec(
            Dates.daysBetween(Dates.dateOf(r.at), r.goal.target).toInt(), r.goal.text,
            stamp(w), stamp(r.at), span(w, r.at)
        )
    }
}
