package com.kjooy0305.dayclean

import com.kjooy0305.dayclean.data.StateJson
import com.kjooy0305.dayclean.data.Store
import com.kjooy0305.dayclean.logic.*
import com.kjooy0305.dayclean.model.*
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.nio.file.Files
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class LogicTest {
    @Before fun zone() { Tz.zone = ZoneId.of("UTC") }

    private fun ms(y: Int, m: Int, d: Int, h: Int = 0, min: Int = 0) = Dates.toMillis(LocalDateTime.of(y, m, d, h, min))
    private fun d(y: Int, m: Int, dd: Int) = LocalDate.of(y, m, dd)
    private fun fresh(day: LocalDate = d(2026, 10, 26)) = AppState(mode = ModeState(day))
    private fun alerts(s: AppState) = s.items.mapNotNull { it.nextAlertAt }

    // ---- 날짜 규칙 ----

    @Test fun monthRule() {
        assertEquals(d(2026, 2, 28), Dates.addMonthRule(d(2026, 1, 31)))   // 말일 → 다음 달 말일
        assertEquals(d(2026, 3, 31), Dates.addMonthRule(d(2026, 2, 28)))
        assertEquals(d(2028, 3, 31), Dates.addMonthRule(d(2028, 2, 29)))   // 윤년 말일도 같다
        assertEquals(d(2026, 6, 30), Dates.addMonthRule(d(2026, 5, 31)))
        assertEquals(d(2026, 5, 31), Dates.addMonthRule(d(2026, 4, 30)))
        assertEquals(d(2026, 2, 28), Dates.addMonthRule(d(2026, 1, 30)))   // 없는 날짜는 그 달 말일
        assertEquals(d(2026, 11, 26), Dates.addMonthRule(d(2026, 10, 26)))
        assertEquals(d(2027, 1, 15), Dates.addMonthRule(d(2026, 12, 15)))
    }

    @Test fun yearAndWeekRules() {
        assertEquals(d(2027, 10, 26), Dates.addYearRule(d(2026, 10, 26)))
        assertEquals(d(2029, 2, 28), Dates.addYearRule(d(2028, 2, 29)))
        assertEquals(d(2028, 2, 28), Dates.addYearRule(d(2027, 2, 28)))
        assertEquals(d(2026, 11, 2), Dates.goalDate(Period.W1, d(2026, 10, 26)))
        assertEquals(Period.W1, Dates.classify(7)); assertEquals(Period.M1, Dates.classify(8))
        assertEquals(Period.M1, Dates.classify(31)); assertEquals(Period.Y1, Dates.classify(32))
    }

    @Test fun dDayText() {
        assertEquals("D-3", Dates.dText(3)); assertEquals("D-DAY", Dates.dText(0)); assertEquals("D+2", Dates.dText(-2))
        assertEquals(6L, Dates.daysToNextFirst(d(2026, 10, 26)))
        assertEquals(31L, Dates.daysToNextFirst(d(2026, 10, 1)))
    }

    @Test fun parseTimes() {
        assertEquals(7 * 60, Dates.parseHhmm("07:00")); assertEquals(23 * 60 + 59, Dates.parseHhmm("23:59"))
        assertNull(Dates.parseHhmm("24:00")); assertNull(Dates.parseHhmm("7")); assertNull(Dates.parseHhmm("12:60"))
    }

    // ---- 쉬는 시간 ----

    @Test fun restAcrossMidnightAndWeekdays() {
        val all = RestInterval(1, 23 * 60, 7 * 60, DayOfWeek.values().toSet())
        assertEquals(ms(2026, 10, 27, 7), Rests.endIfResting(listOf(all), ms(2026, 10, 26, 23, 30)))
        assertEquals(ms(2026, 10, 27, 7), Rests.endIfResting(listOf(all), ms(2026, 10, 27, 2, 0)))
        assertNull(Rests.endIfResting(listOf(all), ms(2026, 10, 27, 7, 0)))
        val lunch = RestInterval(2, 13 * 60, 14 * 60, setOf(DayOfWeek.MONDAY))   // 2026-10-26은 월요일
        assertNotNull(Rests.endIfResting(listOf(lunch), ms(2026, 10, 26, 13, 30)))
        assertNull(Rests.endIfResting(listOf(lunch), ms(2026, 10, 27, 13, 30)))
    }

    @Test fun overnightRestUsesStartDayWeekday() {
        // 금요일 밤 23시 시작은 토요일 아침까지 이어진다. 토요일 밤에는 적용되지 않는다.
        val fri = RestInterval(1, 23 * 60, 7 * 60, setOf(DayOfWeek.FRIDAY))
        assertNotNull(Rests.endIfResting(listOf(fri), ms(2026, 10, 24, 3, 0)))   // 토요일 새벽
        assertNull(Rests.endIfResting(listOf(fri), ms(2026, 10, 25, 3, 0)))      // 일요일 새벽
    }

    @Test fun chainedRestsFollowToTheEnd() {
        val a = RestInterval(1, 9 * 60, 10 * 60, DayOfWeek.values().toSet())
        val b = RestInterval(2, 10 * 60, 11 * 60, DayOfWeek.values().toSet())
        assertEquals(ms(2026, 10, 26, 11), Rests.effective(listOf(a, b), ms(2026, 10, 26, 9, 10)))
    }

    // ---- 알림 시각 ----

    @Test fun firstAlertTimes() {
        val now = ms(2026, 10, 26, 8, 0)
        var s = Ops.addItem(fresh(), Kind.NOW, "보고서", now)
        assertEquals(now + 10 * 60_000, s.items[0].nextAlertAt)
        s = Ops.addItem(s, Kind.LATER, "빨래", now)                       // 기본 시작: 저녁 정리 19:00
        assertEquals(ms(2026, 10, 26, 19), s.items[1].nextAlertAt)
        s = Ops.addItem(s, Kind.LATER, "점심 뒤", now, startCleanupId = null)
        s = Ops.addItem(s, Kind.MEMO, "메모", now)
        assertNull(s.items[3].nextAlertAt)
        val late = Ops.addItem(fresh(), Kind.LATER, "늦게", ms(2026, 10, 26, 20, 0))
        assertEquals(ms(2026, 10, 26, 20, 10), late.items[0].nextAlertAt)     // 시작 시간이 이미 지났으면 10분 뒤
    }

    @Test fun laterUsesChosenCleanup() {
        var s = Ops.addCleanup(fresh(), "점심 점검", 12 * 60 + 30, CleanupType.MORNING)
        val id = s.settings.cleanups.last().id
        s = Ops.addItem(s, Kind.LATER, "택배", ms(2026, 10, 26, 8), startCleanupId = id)
        assertEquals(ms(2026, 10, 26, 12, 30), s.items[0].nextAlertAt)
    }

    @Test fun doneStopsAlertAndUndoRestoresIt() {
        val now = ms(2026, 10, 26, 8)
        var s = Ops.addItem(fresh(), Kind.NOW, "약", now)
        val id = s.items[0].id
        s = Ops.toggleDone(s, id, now + 1000)
        assertTrue(s.items[0].done); assertNull(s.items[0].nextAlertAt)
        s = Ops.toggleDone(s, id, now + 5000)
        assertFalse(s.items[0].done); assertNull(s.items[0].doneAt); assertNotNull(s.items[0].nextAlertAt)
    }

    // ---- 알람 처리 ----

    @Test fun engineAlertsEveryTenMinutesAndSkipsDone() {
        val t0 = ms(2026, 10, 26, 8)
        var s = Ops.addItem(fresh(), Kind.NOW, "A", t0)
        s = Ops.addItem(s, Kind.NOW, "B", t0)
        s = Ops.toggleDone(s, s.items[1].id, t0 + 1)
        val r1 = Engine.process(s, t0 + 10 * 60_000)
        assertEquals(listOf("A"), r1.itemAlerts.map { it.text })
        assertEquals(t0 + 20 * 60_000, r1.state.items[0].nextAlertAt)
        val r2 = Engine.process(r1.state, t0 + 15 * 60_000)
        assertTrue(r2.itemAlerts.isEmpty())
        val r3 = Engine.process(r2.state, t0 + 20 * 60_000)
        assertEquals(1, r3.itemAlerts.size)
    }

    @Test fun restHoldsAlertsUntilItEnds() {
        val t0 = ms(2026, 10, 26, 22, 55)
        var s = Ops.addRest(fresh(), 23 * 60, 7 * 60, DayOfWeek.values().toSet())
        s = Ops.addItem(s, Kind.NOW, "A", t0)                                  // 23:05에 울릴 차례
        val during = Engine.process(s, ms(2026, 10, 26, 23, 5))
        assertTrue(during.itemAlerts.isEmpty())
        // 다음에 깨울 시각은 쉬는 시간이 끝나는 07:00이 아니라 그 사이 자정 이관이나 정리 시간 중 가장 이른 것
        val wake = Engine.nextWake(during.state, ms(2026, 10, 26, 23, 5))
        assertTrue(wake <= ms(2026, 10, 27, 0))
        // 아침 7시에 한 번 알린다 (이어진 하루로 보기 위해 같은 날짜 항목으로 시험)
        val s2 = Ops.addItem(fresh(d(2026, 10, 27)), Kind.NOW, "B", ms(2026, 10, 27, 6, 30))
        val s3 = Ops.addRest(s2, 23 * 60, 7 * 60, DayOfWeek.values().toSet())
        val atSix = Engine.process(s3, ms(2026, 10, 27, 6, 40))
        assertTrue(atSix.itemAlerts.isEmpty())
        assertEquals(ms(2026, 10, 27, 7), Engine.nextWake(atSix.state, ms(2026, 10, 27, 6, 40)).coerceAtMost(ms(2026, 10, 27, 7)))
        val atSeven = Engine.process(atSix.state, ms(2026, 10, 27, 7))
        assertEquals(listOf("B"), atSeven.itemAlerts.map { it.text })
    }

    @Test fun cleanupAlertFiresOnceAndOnlyTheLatestWhenLate() {
        val s = fresh()
        val r = Engine.process(s, ms(2026, 10, 26, 7, 0))
        assertEquals("아침 정리", r.cleanupAlert?.name)
        val again = Engine.process(r.state, ms(2026, 10, 26, 7, 1))
        assertNull(again.cleanupAlert)
        val late = Engine.process(fresh(), ms(2026, 10, 26, 22, 30))          // 꺼져 있다가 늦게 켜짐
        assertEquals("회고", late.cleanupAlert?.name)
        assertNull(Engine.process(late.state, ms(2026, 10, 26, 22, 31)).cleanupAlert)
    }

    @Test fun nextWakeIsTheEarliestEvent() {
        val s = fresh()
        val now = ms(2026, 10, 26, 6, 0)
        assertEquals(ms(2026, 10, 26, 7), Engine.nextWake(s, now))
        val withItem = Ops.addItem(s, Kind.NOW, "A", now)
        assertEquals(now + 10 * 60_000, Engine.nextWake(withItem, now))
        val night = ms(2026, 10, 26, 22, 30)
        val fired = Engine.process(s, night).state
        assertEquals(ms(2026, 10, 27, 0), Engine.nextWake(fired, night))
    }

    // ---- 자정 이관 ----

    @Test fun midnightMovesRecordsToMemory() {
        val day = d(2026, 10, 26)
        var s = fresh(day)
        s = Ops.addItem(s, Kind.NOW, "끝낸 일", ms(2026, 10, 26, 6, 30))
        s = Ops.toggleDone(s, s.items[0].id, ms(2026, 10, 26, 8, 5))
        s = Ops.addItem(s, Kind.MEMO, "생각", ms(2026, 10, 26, 15, 12))
        s = Ops.addItem(s, Kind.LATER, "미룰 일", ms(2026, 10, 26, 9, 0))
        s = Ops.addItem(s, Kind.NOW, "포기할 일", ms(2026, 10, 26, 9, 1))
        s = Ops.addItem(s, Kind.NOW, "손 안 댄 일", ms(2026, 10, 26, 9, 2))
        s = Ops.decide(s, s.items[2].id, Disposition.CARRY, ms(2026, 10, 26, 22, 3))
        s = Ops.decide(s, s.items[3].id, Disposition.DROP, ms(2026, 10, 26, 22, 4))
        val next = Rollover.ensureDay(s, d(2026, 10, 27), ms(2026, 10, 27, 0, 0))
        val a = next.archive.single()
        assertEquals(day, a.date)
        assertEquals(listOf(DoneRec("06:30", "08:05", Kind.NOW, "끝낸 일")), a.done)
        assertEquals(listOf(MemoRec("15:12", "생각")), a.memos)
        assertEquals(listOf("미룰 일", "포기할 일", "손 안 댄 일"), a.late.map { it.text })
        assertEquals(listOf("22:03", "22:04", "09:02"), a.late.map { it.time })
        // 넘기기로 한 것만 내일 연노랑으로 나온다
        val today = Ops.today(next, d(2026, 10, 27))
        assertEquals(listOf("미룰 일"), today.map { it.text })
        assertTrue(today[0].carried); assertNull(today[0].disposition)
        assertEquals(ModeState(d(2026, 10, 27)), next.mode)
    }

    @Test fun tomorrowItemsAppearOnTheNextDay() {
        var s = fresh(d(2026, 10, 26))
        s = Ops.addItem(s, Kind.NOW, "내일 일", ms(2026, 10, 26, 22, 10), date = d(2026, 10, 27), carried = true)
        assertNull(s.items[0].nextAlertAt)
        assertTrue(Ops.today(s, d(2026, 10, 26)).isEmpty())
        val next = Rollover.ensureDay(s, d(2026, 10, 27), ms(2026, 10, 27, 0))
        val item = Ops.today(next, d(2026, 10, 27)).single()
        assertEquals(ms(2026, 10, 27, 7), item.nextAlertAt)                    // 어제 넘긴 • 는 아침 정리 시간부터
        assertTrue(next.archive.isEmpty())
    }

    @Test fun skippedDaysAreArchivedOneByOne() {
        var s = fresh(d(2026, 10, 24))
        s = Ops.addItem(s, Kind.NOW, "오래된 일", ms(2026, 10, 24, 9))
        s = Ops.decide(s, s.items[0].id, Disposition.CARRY, ms(2026, 10, 24, 22))
        val next = Rollover.ensureDay(s, d(2026, 10, 27), ms(2026, 10, 27, 0))
        // 24일에 넘겼지만 25, 26일 동안 아무도 회고하지 않아서 25일 기록으로 한 번 더 남고 사라진다
        assertEquals(listOf(d(2026, 10, 25), d(2026, 10, 24)), next.archive.map { it.date })
        assertTrue(Ops.today(next, d(2026, 10, 27)).isEmpty())
    }

    @Test fun autoGoalItemsAppearTenDaysBefore() {
        val today = d(2026, 10, 26)
        var s = Ops.addGoal(fresh(today), "이력서", Period.W1, d(2026, 11, 5), today)   // D-10
        s = Ops.addGoal(s, "여행", Period.M1, d(2026, 11, 6), today)                    // D-11
        val r = Rollover.ensureDay(s, today, ms(2026, 10, 26, 0, 5))
        assertEquals(listOf("이력서"), Ops.today(r, today).map { it.text })
        assertEquals(Kind.LATER, r.items[0].kind)
        val again = Rollover.ensureDay(r, today, ms(2026, 10, 26, 1))
        assertEquals(1, again.items.size)                                               // 하루에 하나
        val off = Rollover.ensureDay(Ops.setSettings(s) { it.copy(autoAdd = false) }, today, ms(2026, 10, 26, 0, 5))
        assertTrue(off.items.isEmpty())
        // 못 한 자동 항목은 메모리에 남기지 않는다
        val nextDay = Rollover.ensureDay(r, d(2026, 10, 27), ms(2026, 10, 27, 0))
        assertTrue(nextDay.archive.isEmpty())
    }

    // ---- 정리 모드 ----

    @Test fun gateFollowsTheDay() {
        var s = fresh()
        assertNull(Gate.active(s, ms(2026, 10, 26, 6, 59)))
        assertEquals("아침 정리", (Gate.active(s, ms(2026, 10, 26, 7, 0)) as Gate.Screen.Cleanup).cleanup.name)
        s = Gate.complete(s, MORNING_ID)
        assertNull(Gate.active(s, ms(2026, 10, 26, 12)))
        // 저녁 정리 시작되면 아침 정리를 못 끝냈어도 저녁 정리로 넘어간다
        val e = Gate.active(fresh(), ms(2026, 10, 26, 19, 5)) as Gate.Screen.Cleanup
        assertEquals("저녁 정리", e.cleanup.name)
        // 회고 시간이어도 저녁 정리를 끝내지 않았으면 저녁 정리가 먼저
        assertEquals("저녁 정리", (Gate.active(fresh(), ms(2026, 10, 26, 22, 30)) as Gate.Screen.Cleanup).cleanup.name)
        val afterEvening = Gate.complete(fresh(), EVENING_ID)
        assertEquals("회고", (Gate.active(afterEvening, ms(2026, 10, 26, 22, 30)) as Gate.Screen.Cleanup).cleanup.name)
        assertNull(Gate.active(Gate.complete(afterEvening, REVIEW_ID), ms(2026, 10, 26, 23)))
        // 날짜가 아직 안 바뀐 상태(이관 전)에서는 화면을 띄우지 않는다
        assertNull(Gate.active(fresh(d(2026, 10, 25)), ms(2026, 10, 26, 7, 1)))
    }

    @Test fun notifyOnlyCleanupNeverGates() {
        val s = Ops.addCleanup(fresh(), "물 마시기", 10 * 60, CleanupType.NOTIFY)
        val g = Gate.active(Gate.complete(s, MORNING_ID), ms(2026, 10, 26, 10, 5))
        assertNull(g)
    }

    @Test fun addedMorningTypeGatesLikeMorning() {
        var s = Ops.addCleanup(fresh(), "점심 점검", 12 * 60 + 30, CleanupType.MORNING)
        s = Gate.complete(s, MORNING_ID)
        val g = Gate.active(s, ms(2026, 10, 26, 12, 31)) as Gate.Screen.Cleanup
        assertEquals("점심 점검", g.cleanup.name)
    }

    @Test fun goalReviewSchedule() {
        val today = d(2026, 10, 26)
        var s = fresh(today)
        s = Ops.addGoal(s, "주", Period.W1, d(2026, 11, 1), today)
        s = Ops.addGoal(s, "달", Period.M1, d(2026, 11, 20), today)
        s = Ops.addGoal(s, "년", Period.Y1, d(2027, 5, 1), today)
        assertEquals(listOf(Period.W1, Period.M1), GoalReview.dueTypes(s, today))            // 년은 1일에만
        assertEquals(listOf(Period.W1, Period.M1, Period.Y1), GoalReview.dueTypes(s, d(2026, 11, 1)))
        s = GoalReview.markDone(s, Period.W1, today)
        s = GoalReview.markDone(s, Period.M1, today)
        assertTrue(GoalReview.dueTypes(s, today).isEmpty())
        assertEquals(listOf(Period.W1), GoalReview.dueTypes(s, d(2026, 10, 27)))             // 1달은 7일마다
        assertEquals(listOf(Period.W1, Period.M1), GoalReview.dueTypes(s, d(2026, 11, 2)))
        val off = Ops.setSettings(s) { it.copy(m1On = false) }
        assertEquals(listOf(Period.W1), GoalReview.dueTypes(off, d(2026, 11, 2)))
        val all = Ops.setSettings(s) { it.copy(goalsOn = false) }
        assertTrue(GoalReview.dueTypes(all, d(2026, 11, 2)).isEmpty())
    }

    @Test fun goalCheckFollowsMorning() {
        val today = d(2026, 10, 26)
        var s = Ops.addGoal(fresh(today), "주", Period.W1, d(2026, 11, 1), today)
        s = Gate.complete(s, MORNING_ID)
        val g = Gate.active(s, ms(2026, 10, 26, 9)) as Gate.Screen.GoalCheck
        assertEquals(listOf(Period.W1), g.types)
        assertNull(Gate.active(GoalReview.markDone(s, Period.W1, today), ms(2026, 10, 26, 9)))
    }

    // ---- 동작 ----

    @Test fun builtInCleanupsCannotBeRemoved() {
        var s = fresh()
        assertEquals(3, Ops.removeCleanup(s, MORNING_ID).settings.cleanups.size)
        s = Ops.addCleanup(s, "추가", 12 * 60, CleanupType.NOTIFY)
        val id = s.settings.cleanups.last().id
        s = Ops.setSettings(s) { it.copy(defaultLaterStartId = id) }
        val removed = Ops.removeCleanup(s, id)
        assertEquals(3, removed.settings.cleanups.size)
        assertEquals(EVENING_ID, removed.settings.defaultLaterStartId)
    }

    @Test fun extendingReclassifiesPeriod() {
        val today = d(2026, 10, 26)
        var s = Ops.addGoal(fresh(today), "청소", Period.W1, d(2026, 10, 24), today)
        val id = s.goals[0].id
        s = Ops.extendGoal(s, id, d(2026, 11, 26), today)
        assertEquals(Period.M1, s.goals[0].period)
        s = Ops.extendGoal(s, id, d(2027, 10, 26), today)
        assertEquals(Period.Y1, s.goals[0].period)
    }

    @Test fun removingGoalClearsItsOpenAutoItems() {
        val today = d(2026, 10, 26)
        var s = Ops.addGoal(fresh(today), "이력서", Period.W1, d(2026, 10, 28), today)
        s = Rollover.ensureDay(s, today, ms(2026, 10, 26, 1))
        assertEquals(1, s.items.size)
        assertTrue(Ops.removeGoal(s, s.goals[0].id).items.isEmpty())
    }

    // ---- txt ----

    @Test fun exportAndImportRoundTrip() {
        Tz.zone = ZoneId.of("UTC")
        val archive = listOf(
            DayArchive(d(2026, 10, 24),
                late = listOf(LateRec("21:40", "병원 | 예약\\전화")),
                memos = listOf(MemoRec("15:12", "첫 줄\n둘째 줄")),
                done = listOf(DoneRec("06:30", "08:05", Kind.NOW, "보고서"), DoneRec("09:10", "18:44", Kind.LATER, "빨래")))
        )
        val s = fresh().copy(archive = archive)
        val text = MemoryText.exportAll(s, d(2026, 10, 26))
        val back = MemoryText.importAll(fresh(), text, d(2026, 10, 26), ms(2026, 10, 26, 9))
        assertEquals(archive, back.state.archive)
        // 같은 파일을 한 번 더 불러와도 늘어나지 않는다
        val twice = MemoryText.importAll(back.state, text, d(2026, 10, 26), ms(2026, 10, 26, 9))
        assertEquals(archive, twice.state.archive)
        assertEquals(0, twice.entries)
    }

    @Test fun todayExportAndImport() {
        val now = ms(2026, 10, 26, 10)
        var s = fresh()
        s = Ops.addItem(s, Kind.NOW, "A | 일", ms(2026, 10, 26, 6, 30))
        s = Ops.toggleDone(s, s.items[0].id, ms(2026, 10, 26, 7, 5))
        s = Ops.addItem(s, Kind.LATER, "B", ms(2026, 10, 26, 8))
        s = Ops.addItem(s, Kind.MEMO, "C", ms(2026, 10, 26, 9))
        val text = MemoryText.exportToday(s, d(2026, 10, 26))
        assertTrue(text.startsWith(MemoryText.HEADER))
        val imported = MemoryText.importToday(fresh(), text, d(2026, 10, 26), now)
        assertEquals(3, imported.entries)
        val items = imported.state.items.sortedBy { it.createdAt }
        assertEquals(listOf("A | 일", "B", "C"), items.map { it.text })
        assertTrue(items[0].done); assertEquals(ms(2026, 10, 26, 7, 5), items[0].doneAt)
        assertNotNull(items[1].nextAlertAt)
        assertEquals(0, MemoryText.importToday(imported.state, text, d(2026, 10, 26), now).entries)
    }

    @Test fun importSkipsFutureDatesAndGarbage() {
        val text = "아무 글\n\n[2030-01-01]\n메모\n  10:00 | 미래\n[2026-10-20]\n메모\n  25:00 | 시간 틀림\n  09:00 | 정상\n"
        val r = MemoryText.importAll(fresh(), text, d(2026, 10, 26), ms(2026, 10, 26, 9))
        assertEquals(listOf(MemoRec("09:00", "정상")), r.state.archive.single().memos)
    }

    // ---- 검색 ----

    @Test fun searchByDateAndText() {
        val a1 = DayArchive(d(2026, 10, 24), memos = listOf(MemoRec("15:12", "새 아이디어")),
            done = listOf(DoneRec("09:10", "18:44", Kind.LATER, "빨래 널기"), DoneRec("06:30", "08:05", Kind.NOW, "보고서")))
        val a2 = DayArchive(d(2026, 10, 22), late = listOf(LateRec("21:50", "화분 물 주기")))
        val all = listOf(a1, a2)
        assertEquals(4, MemorySearch.search(all, "", true, DoneOrder.WRITTEN).count)
        assertEquals(3, MemorySearch.search(all, "10.24", true, DoneOrder.WRITTEN).count)
        assertEquals(3, MemorySearch.search(all, "10-24", true, DoneOrder.WRITTEN).count)
        assertEquals(1, MemorySearch.search(all, "빨래", true, DoneOrder.WRITTEN).count)
        assertEquals(1, MemorySearch.search(all, "10-22 화분", true, DoneOrder.WRITTEN).count)
        assertEquals(1, MemorySearch.search(all, "메모", true, DoneOrder.WRITTEN).count)
        assertEquals(0, MemorySearch.search(all, "없는말", true, DoneOrder.WRITTEN).count)
        val r = MemorySearch.search(all, "", false, DoneOrder.WRITTEN)
        assertEquals(d(2026, 10, 22), r.days.first().archive.date)
        assertEquals(listOf("보고서", "빨래 널기"), MemorySearch.search(all, "", true, DoneOrder.WRITTEN).days.first().done.map { it.text })
        assertEquals(listOf("보고서", "빨래 널기"), MemorySearch.search(all, "", true, DoneOrder.RESOLVED).days.first().done.map { it.text })
    }

    // ---- 저장 ----

    @Test fun stateJsonRoundTrip() {
        val today = d(2026, 10, 26)
        var s = fresh(today)
        s = Ops.addItem(s, Kind.LATER, "A", ms(2026, 10, 26, 8))
        s = Ops.addItem(s, Kind.NOW, "B", ms(2026, 10, 26, 8), date = d(2026, 10, 27), carried = true)
        s = Ops.decide(s, s.items[0].id, Disposition.CARRY, ms(2026, 10, 26, 22))
        s = Ops.addGoal(s, "목표", Period.Y1, d(2027, 10, 26), today)
        s = Ops.addCleanup(s, "점심", 750, CleanupType.EVENING)
        s = Ops.addRest(s, 1380, 420, setOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY))
        s = Gate.complete(s, MORNING_ID)
        s = GoalReview.markDone(s, Period.M1, today)
        s = s.copy(archive = listOf(DayArchive(d(2026, 10, 1), memos = listOf(MemoRec("10:00", "x")))), onboarded = true)
        val back = StateJson.fromJson(JSONObject(StateJson.toJson(s).toString()))
        assertEquals(s, back)
    }

    @Test fun storePersistsAndSurvivesBrokenFile() {
        val dir = Files.createTempDirectory("dc").toFile()
        val file = java.io.File(dir, "dayclean.json")
        var now = ms(2026, 10, 26, 8)
        val store = Store(file) { now }
        store.update { Ops.addItem(it, Kind.NOW, "저장", now) }
        val again = Store(file) { now }
        assertEquals("저장", again.state.items.single().text)
        file.writeText("{깨진 파일")
        assertTrue(Store(file) { now }.state.items.isEmpty())
        now += 10 * 60_000
        val r = store.process()
        assertEquals(1, r.itemAlerts.size)
    }
}
