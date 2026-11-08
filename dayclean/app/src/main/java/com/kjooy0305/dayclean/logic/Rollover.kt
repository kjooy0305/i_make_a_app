package com.kjooy0305.dayclean.logic

import com.kjooy0305.dayclean.model.AppState
import com.kjooy0305.dayclean.model.DayArchive
import com.kjooy0305.dayclean.model.Disposition
import com.kjooy0305.dayclean.model.DoneRec
import com.kjooy0305.dayclean.model.GoalDoneRec
import com.kjooy0305.dayclean.model.GoalRec
import com.kjooy0305.dayclean.model.Item
import com.kjooy0305.dayclean.model.Kind
import com.kjooy0305.dayclean.model.LateRec
import com.kjooy0305.dayclean.model.MemoRec
import com.kjooy0305.dayclean.model.ModeState
import java.time.LocalDate

/** 00시에 지난 기록을 메모리로 옮기고 새 하루를 연다. */
object Rollover {
    const val AUTO_ADD_DAYS = 10L
    private const val MAX_CATCH_UP = 60L

    /** today 이전 날짜의 기록을 모두 메모리로 옮기고, 넘기기로 한 것은 다음 날로 옮긴다. */
    fun ensureDay(state0: AppState, today: LocalDate, nowMs: Long): AppState {
        var s = state0
        if (s.mode.date != today) {
            // 앱을 열지 않은 날도 장기 목표 기록은 남겨야 하므로 지난 날짜부터 훑는다(처음 실행이면 없음)
            val from = if (s.mode.date.year > 2001) maxOf(s.mode.date, today.minusDays(MAX_CATCH_UP)) else null
            s = archivePast(s, today, from)
            s = s.copy(mode = ModeState(date = today))
        }
        s = addAutoGoalItems(s, today, nowMs)
        return ensureAlerts(s, today, nowMs)
    }

    fun archivePast(state: AppState, today: LocalDate, from: LocalDate? = null): AppState {
        var items = state.items
        var archive = state.archive
        var resolved = state.resolvedGoals
        var rn = from
        while (true) {
            val cands = ArrayList<LocalDate>()
            items.filter { it.date.isBefore(today) }.minOfOrNull { it.date }?.let { cands.add(it) }
            resolved.map { Dates.dateOf(it.at) }.filter { it.isBefore(today) }.minOrNull()?.let { cands.add(it) }
            if (rn != null && rn.isBefore(today)) cands.add(rn)
            val day = cands.minOrNull() ?: break
            val ofDay = items.filter { it.date == day }
            val doneGoals = resolved.filter { Dates.dateOf(it.at) == day }
            val rec = buildArchive(day, ofDay).copy(goals = GoalLog.marks(state.goals, day), goalDone = doneGoals.map { GoalLog.doneRec(it) })
            archive = mergeArchive(archive, rec)
            resolved = resolved - doneGoals.toSet()
            val carried = ofDay
                .filter { !it.done && it.kind != Kind.MEMO && it.disposition == Disposition.CARRY }
                .map { it.copy(date = day.plusDays(1), carried = true, disposition = null, decidedAt = null, nextAlertAt = null) }
            items = items.filterNot { it.date == day } + carried
            if (rn != null && !day.isBefore(rn)) rn = day.plusDays(1)
        }
        return state.copy(items = items, resolvedGoals = resolved, archive = archive.sortedByDescending { it.date })
    }

    fun buildArchive(day: LocalDate, items: List<Item>): DayArchive {
        val late = ArrayList<LateRec>()
        val memos = ArrayList<MemoRec>()
        val done = ArrayList<DoneRec>()
        for (i in items) {
            when {
                i.kind == Kind.MEMO -> memos.add(MemoRec(Dates.hhmm(i.createdAt), i.text))
                // 장기 목표에서 들어온 항목은 여기에 남기지 않는다. 목표의 기록은 GoalLog 가 D-n 으로 따로 남긴다.
                // (해결한 날 하나만 남기면 그날 쓰고 끝낸 것처럼 보이기 때문)
                i.goalId != null -> Unit
                i.done -> done.add(DoneRec(Dates.hhmm(i.createdAt), Dates.hhmm(i.doneAt ?: i.createdAt), i.kind, i.text))
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
            (old.done + add.done).distinct(),
            (old.goals + add.goals).distinct(),
            (old.goalDone + add.goalDone).distinct()
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
