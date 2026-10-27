package com.kjooy0305.dayclean.logic

import com.kjooy0305.dayclean.model.AppState
import com.kjooy0305.dayclean.model.CleanupTime
import com.kjooy0305.dayclean.model.CleanupType
import com.kjooy0305.dayclean.model.EVENING_ID
import com.kjooy0305.dayclean.model.Period
import com.kjooy0305.dayclean.model.MORNING_ID
import java.time.LocalDate

/** 지금 앱을 열었을 때 홈 대신 바로 보여 줘야 하는 정리 화면. */
object Gate {
    sealed class Screen {
        data class Cleanup(val cleanup: CleanupTime) : Screen()
        data class GoalCheck(val types: List<Period>) : Screen()
    }

    /**
     * 정리 시간이 시작된 것 중 가장 늦은 것을 보여 준다(앞선 정리는 다음 정리가 시작되면 닫힌다).
     * 회고는 저녁 정리를 끝낸 뒤에만 열리고, 끝내지 않았으면 저녁 정리로 먼저 안내한다.
     */
    fun active(state: AppState, nowMs: Long): Screen? {
        val today = Dates.dateOf(nowMs)
        if (state.mode.date != today) return null
        val minute = Dates.minuteOfDay(nowMs)
        val started = state.settings.cleanups
            .filter { it.type != CleanupType.NOTIFY && it.minuteOfDay <= minute }
            .sortedBy { it.minuteOfDay }
        var candidate = started.lastOrNull()
        if (candidate != null && candidate.type == CleanupType.REVIEW) {
            val evening = started.lastOrNull { it.id == EVENING_ID }
            if (evening != null && evening.id !in state.mode.completed) candidate = evening
        }
        if (candidate != null && candidate.id !in state.mode.completed) return Screen.Cleanup(candidate)
        // 아침 정리를 마쳤는데 아직 점검하지 않은 장기 목표가 있으면 이어서 보여 준다.
        if (MORNING_ID in state.mode.completed) {
            val due = GoalReview.dueTypes(state, today)
            if (due.isNotEmpty()) return Screen.GoalCheck(due)
        }
        return null
    }

    fun complete(state: AppState, cleanupId: Long): AppState =
        state.copy(mode = state.mode.copy(completed = state.mode.completed + cleanupId))

    @Suppress("unused")
    fun isToday(state: AppState, date: LocalDate) = state.mode.date == date
}
