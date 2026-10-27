package com.kjooy0305.dayclean.logic

import com.kjooy0305.dayclean.model.AppState
import com.kjooy0305.dayclean.model.CleanupTime
import com.kjooy0305.dayclean.model.CleanupType
import com.kjooy0305.dayclean.model.Disposition
import com.kjooy0305.dayclean.model.EVENING_ID
import com.kjooy0305.dayclean.model.Goal
import com.kjooy0305.dayclean.model.Item
import com.kjooy0305.dayclean.model.Kind
import com.kjooy0305.dayclean.model.Period
import com.kjooy0305.dayclean.model.RestInterval
import com.kjooy0305.dayclean.model.Settings
import java.time.LocalDate

/** 상태를 바꾸는 동작들. 모두 새 AppState를 돌려주는 순수 함수다. */
object Ops {

    // ---- 기록 ----

    /** date가 오늘이면 알림 시각도 정한다. 내일 할 일(date가 내일)은 그날이 되면 정한다. */
    fun addItem(
        s: AppState, kind: Kind, text: String, nowMs: Long,
        startCleanupId: Long? = null, date: LocalDate? = null, carried: Boolean = false
    ): AppState {
        val today = Dates.dateOf(nowMs)
        val (id, next) = s.alloc()
        val day = date ?: today
        var item = Item(id, kind, text.trim(), day, nowMs, carried = carried,
            startCleanupId = if (kind == Kind.LATER) startCleanupId else null)
        if (day == today) item = item.copy(nextAlertAt = Reminders.initialAlert(item, next.settings, nowMs))
        return next.copy(items = next.items + item)
    }

    private fun mapItem(s: AppState, id: Long, f: (Item) -> Item): AppState =
        s.copy(items = s.items.map { if (it.id == id) f(it) else it })

    fun toggleDone(s: AppState, id: Long, nowMs: Long): AppState = mapItem(s, id) { i ->
        if (!i.done) i.copy(done = true, doneAt = nowMs, nextAlertAt = null)
        else {
            val back = i.copy(done = false, doneAt = null)
            back.copy(nextAlertAt = Reminders.initialAlert(back, s.settings, nowMs))
        }
    }

    fun setDone(s: AppState, id: Long, done: Boolean, nowMs: Long): AppState {
        val cur = s.items.firstOrNull { it.id == id } ?: return s
        return if (cur.done == done) s else toggleDone(s, id, nowMs)
    }

    fun editItem(s: AppState, id: Long, kind: Kind, text: String, startCleanupId: Long?, nowMs: Long): AppState =
        mapItem(s, id) { i ->
            val changed = i.copy(kind = kind, text = text.trim(), startCleanupId = if (kind == Kind.LATER) startCleanupId else null)
            changed.copy(nextAlertAt = if (changed.date == Dates.dateOf(nowMs)) Reminders.initialAlert(changed, s.settings, nowMs) else null)
        }

    fun deleteItem(s: AppState, id: Long): AppState = s.copy(items = s.items.filterNot { it.id == id })

    /** 회고에서 못한 일을 내일로 넘기거나 끝낸다. 오늘 목록에서는 사라지고 알림도 멈춘다. */
    fun decide(s: AppState, id: Long, d: Disposition, nowMs: Long): AppState =
        mapItem(s, id) { it.copy(disposition = d, decidedAt = nowMs, nextAlertAt = null) }

    fun undecide(s: AppState, id: Long, nowMs: Long): AppState = mapItem(s, id) {
        val back = it.copy(disposition = null, decidedAt = null)
        back.copy(nextAlertAt = Reminders.initialAlert(back, s.settings, nowMs))
    }

    /** 오늘 기록장에 보이는 항목. */
    fun today(s: AppState, today: LocalDate): List<Item> =
        s.items.filter { it.date == today && !it.decided }

    /** 회고 화면의 '내일로 넘어가는 것': 내일 미리 적은 것과 넘기기로 한 것. */
    fun tomorrowList(s: AppState, today: LocalDate): List<Item> =
        s.items.filter { (it.date == today.plusDays(1)) || (it.date == today && it.disposition == Disposition.CARRY) }

    // ---- 장기 목표 ----

    fun addGoal(s: AppState, text: String, period: Period, target: LocalDate, today: LocalDate): AppState {
        val (id, next) = s.alloc()
        val seq = (s.goals.maxOfOrNull { it.seq } ?: 0L) + 1
        return next.copy(goals = next.goals + Goal(id, text.trim(), period, target, seq, today))
    }

    fun updateGoal(s: AppState, id: Long, text: String, period: Period, target: LocalDate): AppState =
        s.copy(goals = s.goals.map { if (it.id == id) it.copy(text = text.trim(), period = period, target = target) else it })

    /** 지난 목표를 새 날짜로 미룬다. 주기는 새 날짜까지 남은 날 수로 다시 나눈다. */
    fun extendGoal(s: AppState, id: Long, newTarget: LocalDate, today: LocalDate): AppState =
        s.copy(goals = s.goals.map {
            if (it.id == id) it.copy(target = newTarget, period = Dates.classify(Dates.daysBetween(today, newTarget))) else it
        })

    /** 달성하거나 지운 목표. 오늘 자동으로 들어온 항목도 함께 정리한다. */
    fun removeGoal(s: AppState, id: Long): AppState =
        s.copy(goals = s.goals.filterNot { it.id == id }, items = s.items.filterNot { it.goalId == id && !it.done })

    // ---- 설정 ----

    fun setSettings(s: AppState, f: (Settings) -> Settings): AppState = s.copy(settings = f(s.settings))

    fun addCleanup(s: AppState, name: String, minuteOfDay: Int, type: CleanupType): AppState {
        val (id, next) = s.alloc()
        val c = CleanupTime(id, name.trim(), minuteOfDay, type, false)
        return next.copy(settings = next.settings.copy(cleanups = next.settings.cleanups + c))
    }

    fun setCleanupTime(s: AppState, id: Long, minuteOfDay: Int): AppState = s.copy(
        settings = s.settings.copy(cleanups = s.settings.cleanups.map { if (it.id == id) it.copy(minuteOfDay = minuteOfDay) else it })
    )

    fun editCleanup(s: AppState, id: Long, name: String, minuteOfDay: Int, type: CleanupType): AppState = s.copy(
        settings = s.settings.copy(cleanups = s.settings.cleanups.map {
            if (it.id != id) it else if (it.builtIn) it.copy(minuteOfDay = minuteOfDay) else it.copy(name = name.trim(), minuteOfDay = minuteOfDay, type = type)
        })
    )

    /** 기본 정리 시간은 지울 수 없다. */
    fun removeCleanup(s: AppState, id: Long): AppState {
        val target = s.settings.cleanup(id) ?: return s
        if (target.builtIn) return s
        val cleanups = s.settings.cleanups.filterNot { it.id == id }
        val defaultStart = if (s.settings.defaultLaterStartId == id) EVENING_ID else s.settings.defaultLaterStartId
        return s.copy(
            settings = s.settings.copy(cleanups = cleanups, defaultLaterStartId = defaultStart),
            items = s.items.map { if (it.startCleanupId == id) it.copy(startCleanupId = null) else it }
        )
    }

    fun addRest(s: AppState, startMin: Int, endMin: Int, days: Set<java.time.DayOfWeek>): AppState {
        val (id, next) = s.alloc()
        return next.copy(settings = next.settings.copy(rests = next.settings.rests + RestInterval(id, startMin, endMin, days)))
    }

    fun updateRest(s: AppState, id: Long, startMin: Int, endMin: Int, days: Set<java.time.DayOfWeek>): AppState =
        s.copy(settings = s.settings.copy(rests = s.settings.rests.map { if (it.id == id) it.copy(startMin = startMin, endMin = endMin, days = days) else it }))

    fun removeRest(s: AppState, id: Long): AppState =
        s.copy(settings = s.settings.copy(rests = s.settings.rests.filterNot { it.id == id }))
}
