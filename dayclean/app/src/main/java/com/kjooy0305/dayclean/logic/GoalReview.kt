package com.kjooy0305.dayclean.logic

import com.kjooy0305.dayclean.model.AppState
import com.kjooy0305.dayclean.model.Goal
import com.kjooy0305.dayclean.model.GoalReviewState
import com.kjooy0305.dayclean.model.Period
import java.time.LocalDate

/** 장기 목표 점검 일정. 1주는 매일, 1달은 7일마다, 1년은 매달 1일에 아침 정리 뒤에 나온다. */
object GoalReview {
    const val MONTH_CYCLE_DAYS = 7L

    /** 오늘 점검해야 하는 주기. 1주, 1달, 1년 순이다. 꺼 둔 주기와 목표가 없는 주기는 빠진다. */
    fun dueTypes(state: AppState, today: LocalDate): List<Period> {
        val r = state.review
        val out = ArrayList<Period>()
        for (p in Period.values()) {
            if (!state.settings.typeOn(p)) continue
            if (state.goals.none { it.period == p }) continue
            val due = when (p) {
                Period.W1 -> r.last1w != today
                Period.M1 -> r.last1m == null || Dates.daysBetween(r.last1m, today) >= MONTH_CYCLE_DAYS
                Period.Y1 -> today.dayOfMonth == 1 && r.last1y != Dates.monthKey(today)
            }
            if (due) out.add(p)
        }
        return out
    }

    fun markDone(state: AppState, p: Period, today: LocalDate): AppState {
        val r = state.review
        val next: GoalReviewState = when (p) {
            Period.W1 -> r.copy(last1w = today)
            Period.M1 -> r.copy(last1m = today)
            Period.Y1 -> r.copy(last1y = Dates.monthKey(today))
        }
        return state.copy(review = next)
    }

    fun goalsOf(state: AppState, p: Period): List<Goal> =
        state.goals.filter { it.period == p }.sortedBy { it.target }
}
