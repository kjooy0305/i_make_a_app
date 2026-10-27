package com.kjooy0305.dayclean.logic

import com.kjooy0305.dayclean.model.AppState
import com.kjooy0305.dayclean.model.CleanupTime
import com.kjooy0305.dayclean.model.CleanupType
import com.kjooy0305.dayclean.model.Item

class EngineResult(
    val state: AppState,
    val itemAlerts: List<Item>,
    val cleanupAlert: CleanupTime?
)

/** 알람이 깨웠을 때 할 일과 다음에 깨울 시각을 계산한다. 안드로이드 API를 쓰지 않아서 시험하기 쉽다. */
object Engine {

    fun process(state0: AppState, nowMs: Long): EngineResult {
        val today = Dates.dateOf(nowMs)
        var s = Rollover.ensureDay(state0, today, nowMs)

        // 정리 시간: 이미 지난 것 중 가장 늦은 것 하나만 알리고, 나머지는 울린 것으로 친다.
        val due = s.settings.cleanups
            .filter { it.id !in s.mode.fired && Dates.toMillis(today, it.minuteOfDay) <= nowMs }
            .sortedBy { it.minuteOfDay }
        val cleanupAlert = due.lastOrNull()
        if (due.isNotEmpty()) s = s.copy(mode = s.mode.copy(fired = s.mode.fired + due.map { it.id }))

        // 항목: 알림 시각이 됐고 쉬는 시간이 아니면 알린다. 쉬는 시간이 끝나는 때에도 한 번 알린다.
        val alerts = ArrayList<Item>()
        val items = s.items.map { item ->
            val at = item.nextAlertAt
            if (at != null && item.date == today && Reminders.needsAlert(item) &&
                Rests.effective(s.settings.rests, at) <= nowMs
            ) {
                alerts.add(item)
                item.copy(nextAlertAt = nowMs + Reminders.INTERVAL_MS)
            } else item
        }
        s = s.copy(items = items)
        return EngineResult(s, alerts, cleanupAlert)
    }

    /** 다음에 깨울 시각. 항상 now보다 뒤다. */
    fun nextWake(state: AppState, nowMs: Long): Long {
        val today = Dates.dateOf(nowMs)
        val candidates = ArrayList<Long>()
        for (c in state.settings.cleanups) {
            val todayMs = Dates.toMillis(today, c.minuteOfDay)
            val fired = state.mode.date == today && c.id in state.mode.fired
            candidates.add(if (todayMs > nowMs && !fired) todayMs else Dates.toMillis(today.plusDays(1), c.minuteOfDay))
        }
        candidates.add(Dates.startOfDay(today.plusDays(1)))
        for (i in state.items) {
            val at = i.nextAlertAt ?: continue
            if (i.date != today || !Reminders.needsAlert(i)) continue
            candidates.add(Rests.effective(state.settings.rests, at))
        }
        val min = candidates.min()
        return if (min > nowMs) min else nowMs + 1_000L
    }

    @Suppress("unused")
    fun isNotifyOnly(c: CleanupTime): Boolean = c.type == CleanupType.NOTIFY
}
