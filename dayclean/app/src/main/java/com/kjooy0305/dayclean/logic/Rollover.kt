package com.kjooy0305.dayclean.logic

import com.kjooy0305.dayclean.model.AppState
import com.kjooy0305.dayclean.model.DayArchive
import com.kjooy0305.dayclean.model.Disposition
import com.kjooy0305.dayclean.model.DoneRec
import com.kjooy0305.dayclean.model.Item
import com.kjooy0305.dayclean.model.Kind
import com.kjooy0305.dayclean.model.LateRec
import com.kjooy0305.dayclean.model.MemoRec
import com.kjooy0305.dayclean.model.ModeState
import java.time.LocalDate

/** 00시에 지난 기록을 메모리로 옮기고 새 하루를 연다. */
object Rollover {
    const val AUTO_ADD_DAYS = 10L

    /** today 이전 날짜의 기록을 모두 메모리로 옮기고, 넘기기로 한 것은 다음 날로 옮긴다. */
    fun ensureDay(state0: AppState, today: LocalDate, nowMs: Long): AppState {
        var s = state0
        if (s.mode.date != today) {
            s = archivePast(s, today)
            s = s.copy(mode = ModeState(date = today))
        }
        s = addAutoGoalItems(s, today, nowMs)
        return ensureAlerts(s, today, nowMs)
    }

    fun archivePast(state: AppState, today: LocalDate): AppState {
        var items = state.items
        var archive = state.archive
        while (true) {
            val past = items.filter { it.date.isBefore(today) }
            if (past.isEmpty()) break
            val day = past.minOf { it.date }
            val ofDay = past.filter { it.date == day }
            archive = mergeArchive(archive, buildArchive(day, ofDay))
            val carried = ofDay
                .filter { !it.done && it.kind != Kind.MEMO && it.disposition == Disposition.CARRY }
                .map { it.copy(date = day.plusDays(1), carried = true, disposition = null, decidedAt = null, nextAlertAt = null) }
            items = items.filterNot { it.date == day } + carried
        }
        return state.copy(items = items, archive = archive.sortedByDescending { it.date })
    }

    fun buildArchive(day: LocalDate, items: List<Item>): DayArchive {
        val late = ArrayList<LateRec>()
        val memos = ArrayList<MemoRec>()
        val done = ArrayList<DoneRec>()
        for (i in items) {
            when {
                i.kind == Kind.MEMO -> memos.add(MemoRec(Dates.hhmm(i.createdAt), i.text))
                i.done -> done.add(DoneRec(Dates.hhmm(i.createdAt), Dates.hhmm(i.doneAt ?: i.createdAt), i.kind, i.text))
                // 장기 목표에서 자동으로 들어온 항목은 매일 새로 만들어지므로 못 한 것은 남기지 않는다.
                i.goalId != null -> Unit
                else -> late.add(LateRec(Dates.hhmm(i.decidedAt ?: i.createdAt), i.text))
            }
        }
        return DayArchive(day, late, memos, done)
    }

    fun mergeArchive(list: List<DayArchive>, add: DayArchive): List<DayArchive> {
        if (add.isEmpty) return list
        val old = list.firstOrNull { it.date == add.date }
        val merged = if (old == null) add else DayArchive(
            add.date,
            (old.late + add.late).distinct(),
            (old.memos + add.memos).distinct(),
            (old.done + add.done).distinct()
        )
        return list.filterNot { it.date == add.date } + merged
    }

    /** 목표 날짜가 10일 안으로 들어온 목표를 오늘의 '-' 항목으로 넣는다. 하루에 목표당 하나. */
    fun addAutoGoalItems(state: AppState, today: LocalDate, nowMs: Long): AppState {
        if (!state.settings.autoAdd) return state
        var s = state
        for (g in state.goals) {
            if (Dates.daysBetween(today, g.target) > AUTO_ADD_DAYS) continue
            if (s.items.any { it.goalId == g.id && it.date == today }) continue
            val (id, next) = s.alloc()
            val item = Item(id, Kind.LATER, g.text, today, createdAt = Dates.startOfDay(today), goalId = g.id)
            s = next.copy(items = next.items + item.copy(nextAlertAt = Reminders.initialAlert(item, next.settings, nowMs)))
        }
        return s
    }

    /** 오늘 항목 중 알림 시각이 비어 있는 것에 처음 시각을 채운다. */
    fun ensureAlerts(state: AppState, today: LocalDate, nowMs: Long): AppState {
        val items = state.items.map {
            if (it.date == today && it.nextAlertAt == null && Reminders.needsAlert(it))
                it.copy(nextAlertAt = Reminders.initialAlert(it, state.settings, nowMs)) else it
        }
        return if (items == state.items) state else state.copy(items = items)
    }
}
