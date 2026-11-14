package com.kjooy0305.dayclean

import android.app.AlarmManager
import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import com.kjooy0305.dayclean.data.Store
import com.kjooy0305.dayclean.logic.Dates
import com.kjooy0305.dayclean.logic.Ops
import com.kjooy0305.dayclean.model.*
import com.kjooy0305.dayclean.platform.ActionReceiver
import com.kjooy0305.dayclean.platform.AlarmScheduler
import com.kjooy0305.dayclean.platform.Notifier
import com.kjooy0305.dayclean.ui.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ActivityController
import java.io.File
import java.nio.file.Files
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

@RunWith(RobolectricTestRunner::class)
class UiTest {
    private val context get() = ApplicationProvider.getApplicationContext<Application>()
    private var now = 0L
    private lateinit var store: Store

    private fun ms(y: Int, m: Int, d: Int, h: Int = 0, min: Int = 0) = Dates.toMillis(LocalDateTime.of(y, m, d, h, min))

    @Before fun setUp() {
        Tz.zone = ZoneId.of("UTC")
        now = ms(2026, 10, 26, 10, 0)
        val dir = Files.createTempDirectory("dcui").toFile()
        store = Store(File(dir, "dayclean.json")) { now }
        store.update { it.copy(mode = ModeState(LocalDate.of(2026, 10, 26)), onboarded = true) }
        Store.reset(store)
    }

    @After fun tearDown() { Store.reset(null) }

    /** 홈이 정리 화면으로 넘어가지 않도록 오늘 정리를 모두 끝낸 상태로 만든다. */
    private fun finishCleanups() {
        store.update { var s = it; for (c in s.settings.cleanups) s = com.kjooy0305.dayclean.logic.Gate.complete(s, c.id); s }
    }

    private fun grantNotifications() {
        shadowOf(context).grantPermissions(android.Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun texts(v: View, out: MutableList<String> = ArrayList()): List<String> {
        if (v is TextView && v.text.isNotBlank()) out.add(v.text.toString())
        if (v is ViewGroup) for (i in 0 until v.childCount) texts(v.getChildAt(i), out)
        return out
    }

    private fun find(v: View, pred: (View) -> Boolean, out: MutableList<View> = ArrayList()): List<View> {
        if (pred(v)) out.add(v)
        if (v is ViewGroup) for (i in 0 until v.childCount) find(v.getChildAt(i), pred, out)
        return out
    }

    private fun root(a: android.app.Activity): View = a.findViewById<ViewGroup>(android.R.id.content)

    private fun click(a: android.app.Activity, text: String) {
        val v = find(root(a), { it is TextView && it.text.toString() == text && it.isClickable }).firstOrNull()
            ?: error("버튼 없음: $text / 있는 글자: ${texts(root(a))}")
        v.performClick()
    }

    /** 가장 최근에 띄운 대화상자 안의 버튼 누르기. */
    private fun latestDialog() = org.robolectric.shadows.ShadowDialog.getLatestDialog() ?: error("열린 대화상자가 없어요")

    private fun dialogTexts(): List<String> = texts(latestDialog().window!!.decorView)

    private fun dialogClick(text: String) {
        val v = find(latestDialog().window!!.decorView, { it is TextView && it.text.toString() == text && it.isClickable }).firstOrNull()
            ?: error("대화상자에 버튼 없음: $text / 있는 글자: ${dialogTexts()}")
        v.performClick()
    }

    private fun <T : android.app.Activity> start(c: Class<T>, intent: Intent? = null): ActivityController<T> =
        (if (intent != null) Robolectric.buildActivity(c, intent) else Robolectric.buildActivity(c)).setup()

    // ---- 홈 ----

    @Test fun homeShowsDateAndAddsItems() {
        finishCleanups()
        val a = start(HomeActivity::class.java).get()
        val all = texts(root(a))
        assertTrue(all.any { it.contains("2026년 10월 26일") })
        assertTrue(all.any { it.contains("월요일") })
        assertTrue(all.any { it.startsWith("장기 목표 D-") })
        assertTrue(all.contains("설정") && all.contains("메모리"))
        val input = find(root(a), { it is EditText }).first() as EditText
        input.setText("보고서 초안")
        click(a, "추가")
        assertEquals("보고서 초안", store.state.items.single().text)
        assertEquals(Kind.NOW, store.state.items.single().kind)
        assertTrue(texts(root(a)).any { it == "보고서 초안" })
        assertEquals("", input.text.toString())
    }

    @Test fun addingLaterItemUsesDefaultCleanupStart() {
        finishCleanups()
        val a = start(HomeActivity::class.java).get()
        click(a, "- 나중에")
        assertTrue(texts(root(a)).any { it.contains("‘-’ 알림 시작: 저녁 정리 19:00") })
        (find(root(a), { it is EditText }).first() as EditText).setText("빨래")
        click(a, "추가")
        val item = store.state.items.single()
        assertEquals(Kind.LATER, item.kind)
        assertEquals(ms(2026, 10, 26, 19), item.nextAlertAt)
    }

    @Test fun checkedItemsMoveOnlyAfterLeavingTheScreen() {
        finishCleanups()
        store.update { Ops.addItem(it, Kind.NOW, "약 먹기", now) }
        val ctrl = start(HomeActivity::class.java)
        val a = ctrl.get()
        val check = find(root(a), { it is CheckCircle }).first() as CheckCircle
        check.performClick()
        assertTrue(store.state.items[0].done)
        assertTrue(check.checked)
        // 같은 화면에서는 '해결한 것'으로 내려가지 않는다
        val before = texts(root(a))
        assertTrue("before=$before", before.indexOf("지금 해야 할 것 •") >= 0 && before.indexOf("지금 해야 할 것 •") < before.indexOf("약 먹기"))
        // 다른 화면으로 갔다 오면 해결한 것으로 간다
        ctrl.pause().stop().restart().start().resume().visible()
        val after = texts(root(a))
        assertTrue("after=$after", after.indexOf("해결한 것") in 0 until after.indexOf("약 먹기"))
        // 체크를 풀고 다녀오면 다시 위로 올라간다
        val again = find(root(a), { it is CheckCircle }).first() as CheckCircle
        again.performClick()
        assertFalse(store.state.items[0].done)
        ctrl.pause().stop().restart().start().resume().visible()
        val back = texts(root(a))
        assertTrue("back=$back", back.indexOf("지금 해야 할 것 •") in 0 until back.indexOf("약 먹기"))
    }

    // ---- 정리 모드로 바로 열기 ----

    @Test fun homeOpensMorningCleanupDirectlyWhenItsTimeHasCome() {
        now = ms(2026, 10, 26, 7, 30)
        val a = start(HomeActivity::class.java).get()
        val next = shadowOf(a).nextStartedActivity
        assertEquals(MorningActivity::class.java.name, next.component?.className)
        assertEquals(MORNING_ID, next.getLongExtra(HomeActivity.EXTRA_CLEANUP_ID, -1))
    }

    @Test fun eveningThenReviewOrder() {
        now = ms(2026, 10, 26, 22, 30)
        val a = start(HomeActivity::class.java).get()
        assertEquals(EveningActivity::class.java.name, shadowOf(a).nextStartedActivity.component?.className)
        store.update { com.kjooy0305.dayclean.logic.Gate.complete(it, EVENING_ID) }
        val b = start(HomeActivity::class.java).get()
        assertEquals(ReviewActivity::class.java.name, shadowOf(b).nextStartedActivity.component?.className)
    }

    @Test fun morningCleanupFlowCompletesAndLeadsToGoalCheck() {
        now = ms(2026, 10, 26, 7, 30)
        store.update {
            var s = Ops.addItem(it, Kind.NOW, "어제 일", ms(2026, 10, 25, 9)).copy(mode = ModeState(LocalDate.of(2026, 10, 26)))
            s = s.copy(items = s.items.map { i -> i.copy(date = LocalDate.of(2026, 10, 26), carried = true) })
            Ops.addGoal(s, "이력서", Period.W1, LocalDate.of(2026, 11, 2), LocalDate.of(2026, 10, 26))
        }
        val ctrl = start(MorningActivity::class.java, Intent(context, MorningActivity::class.java).putExtra(HomeActivity.EXTRA_CLEANUP_ID, MORNING_ID))
        val a = ctrl.get()
        val all = texts(root(a))
        assertTrue(all.contains("아침 정리 · 07:00"))
        assertTrue(all.contains("어제 넘긴 기록") && all.contains("어제 일"))
        (find(root(a), { it is EditText }).first() as EditText).setText("오늘 새 일")
        click(a, "추가")
        assertTrue(store.state.items.any { it.text == "오늘 새 일" })
        assertTrue(texts(root(a)).contains("방금 추가한 것"))
        click(a, "아침 정리 완료 ▸")
        assertTrue(MORNING_ID in store.state.mode.completed)
        assertTrue(a.isFinishing)
        val home = start(HomeActivity::class.java).get()
        assertEquals(GoalCheckActivity::class.java.name, shadowOf(home).nextStartedActivity.component?.className)
    }

    @Test fun backButtonIsBlockedDuringCleanup() {
        now = ms(2026, 10, 26, 19, 30)
        val ctrl = start(EveningActivity::class.java, Intent(context, EveningActivity::class.java).putExtra(HomeActivity.EXTRA_CLEANUP_ID, EVENING_ID))
        val a = ctrl.get()
        a.onBackPressedDispatcher.onBackPressed()
        assertFalse(a.isFinishing)
        click(a, "확인 · 저녁 정리 끝내기")
        assertTrue(EVENING_ID in store.state.mode.completed)
        assertTrue(a.isFinishing)
    }

    @Test fun eveningListAllowsOnlyChecking() {
        now = ms(2026, 10, 26, 19, 30)
        store.update { var s = Ops.addItem(it, Kind.NOW, "A", ms(2026, 10, 26, 8)); s = Ops.addItem(s, Kind.MEMO, "메모", ms(2026, 10, 26, 8)); Ops.addItem(s, Kind.LATER, "B", ms(2026, 10, 26, 8)) }
        val a = start(EveningActivity::class.java, Intent(context, EveningActivity::class.java).putExtra(HomeActivity.EXTRA_CLEANUP_ID, EVENING_ID)).get()
        val t = texts(root(a))
        assertTrue(t.contains("A") && t.contains("B"))
        assertFalse(t.contains("메모"))
        assertTrue(find(root(a), { it is EditText }).isEmpty())
        (find(root(a), { it is CheckCircle }).first() as CheckCircle).performClick()
        assertEquals(1, store.state.items.count { it.done })
    }

    @Test fun reviewCarriesAndDrops() {
        now = ms(2026, 10, 26, 22, 30)
        store.update { var s = Ops.addItem(it, Kind.NOW, "넘길 일", ms(2026, 10, 26, 8)); Ops.addItem(s, Kind.LATER, "끝낼 일", ms(2026, 10, 26, 8)) }
        val a = start(ReviewActivity::class.java, Intent(context, ReviewActivity::class.java).putExtra(HomeActivity.EXTRA_CLEANUP_ID, REVIEW_ID)).get()
        val carry = find(root(a), { it is TextView && it.text.toString() == "내일로 넘기기" }).first()
        carry.performClick()
        val drop = find(root(a), { it is TextView && it.text.toString() == "끝내기" }).first()
        drop.performClick()
        val items = store.state.items
        assertEquals(Disposition.CARRY, items.first { it.text == "넘길 일" }.disposition)
        assertEquals(Disposition.DROP, items.first { it.text == "끝낼 일" }.disposition)
        (find(root(a), { it is EditText }).first() as EditText).setText("내일 일")
        click(a, "추가")
        assertEquals(LocalDate.of(2026, 10, 27), store.state.items.first { it.text == "내일 일" }.date)
        assertTrue(texts(root(a)).contains("내일 일"))
        click(a, "회고 끝내기")
        assertTrue(REVIEW_ID in store.state.mode.completed)
    }

    @Test fun goalCheckFollowsPeriodOrderAndMarksReviews() {
        val today = LocalDate.of(2026, 10, 26)
        store.update {
            var s = Ops.addGoal(it, "주 목표", Period.W1, today.plusDays(5), today)
            s = Ops.addGoal(s, "달 목표", Period.M1, today.plusDays(20), today)
            Ops.addGoal(s, "지난 목표", Period.W1, today.minusDays(2), today)
        }
        val ctrl = start(GoalCheckActivity::class.java)
        val a = ctrl.get()
        assertTrue(texts(root(a)).contains("1주 목표"))
        assertTrue(texts(root(a)).contains("연장"))        // 지난 목표에만
        click(a, "달성")                                      // 첫 카드(가장 빠른 날짜)
        assertTrue(dialogTexts().any { it.contains("정말 해결했나요?") })
        assertEquals(3, store.state.goals.size)               // 확인 전에는 그대로
        dialogClick("해결했어요")
        assertEquals(2, store.state.goals.size)
        click(a, "1달 점검으로 ▸")
        assertEquals(today, store.state.review.last1w)
        assertTrue(texts(root(a)).contains("1달 목표"))
        click(a, "정리 끝내기")
        assertEquals(today, store.state.review.last1m)
        assertTrue(a.isFinishing)
    }

    @Test fun goalCheckResolveCanBeCancelled() {
        val today = LocalDate.of(2026, 10, 26)
        store.update { Ops.addGoal(it, "주 목표", Period.W1, today.plusDays(5), today) }
        val a = start(GoalCheckActivity::class.java).get()
        click(a, "달성")
        assertTrue(dialogTexts().contains("취소") && dialogTexts().contains("해결했어요"))
        dialogClick("취소")
        assertFalse(latestDialog().isShowing)
        assertEquals(1, store.state.goals.size)
        assertTrue(store.state.resolvedGoals.isEmpty())
    }

    // ---- 장기 목표 ----

    @Test fun goalEditResolveAsksThenResolvesOrCancels() {
        val today = LocalDate.of(2026, 10, 26)
        store.update { Ops.addGoal(it, "이력서 끝내기", Period.M1, today.plusDays(20), today) }
        val id = store.state.goals.single().id
        val intent = Intent(context, GoalEditActivity::class.java).putExtra(GoalEditActivity.EXTRA_MODE, GoalEditActivity.EDIT).putExtra(GoalEditActivity.EXTRA_ID, id)
        val a = start(GoalEditActivity::class.java, intent).get()
        assertTrue(texts(root(a)).contains("이 목표 해결"))
        click(a, "이 목표 해결")
        assertTrue(dialogTexts().any { it.contains("이력서 끝내기") && it.contains("정말 해결했나요?") })
        dialogClick("취소")                                       // 취소하면 아무 일도 없다
        assertEquals(1, store.state.goals.size)
        assertFalse(a.isFinishing)
        click(a, "이 목표 해결")
        dialogClick("해결했어요")
        assertTrue(store.state.goals.isEmpty())
        assertEquals("이력서 끝내기", store.state.resolvedGoals.single().goal.text)
        assertTrue(a.isFinishing)
    }

    @Test fun newGoalScreenHasNoResolveButton() {
        val a = start(GoalEditActivity::class.java, Intent(context, GoalEditActivity::class.java).putExtra(GoalEditActivity.EXTRA_MODE, GoalEditActivity.NEW)).get()
        assertFalse(texts(root(a)).contains("이 목표 해결"))
    }

    @Test fun goalsScreenListsSortsAndFilters() {
        val today = LocalDate.of(2026, 10, 26)
        store.update {
            var s = Ops.addGoal(it, "나중 목표", Period.Y1, today.plusDays(200), today)
            s = Ops.addGoal(s, "곧 목표", Period.W1, today.plusDays(3), today)
            Ops.addGoal(s, "중간 목표", Period.M1, today.plusDays(20), today)
        }
        val a = start(GoalsActivity::class.java).get()
        val t = texts(root(a))
        assertTrue(t.contains("가장 빠른 목표 3개"))
        assertTrue(t.indexOf("곧 목표") < t.lastIndexOf("나중 목표"))
        click(a, "1달")
        val filtered = texts(root(a))
        assertTrue(filtered.contains("중간 목표"))
        assertEquals(1, filtered.count { it == "나중 목표" })   // 가장 빠른 3개 상자에만 남는다
        click(a, "작성")
        assertEquals(GoalEditActivity::class.java.name, shadowOf(a).nextStartedActivity.component?.className)
    }

    @Test fun goalEditComputesDatesFromPeriodAndSaves() {
        val a = start(GoalEditActivity::class.java, Intent(context, GoalEditActivity::class.java).putExtra(GoalEditActivity.EXTRA_MODE, GoalEditActivity.NEW)).get()
        val fields = find(root(a), { it is EditText }).map { it as EditText }
        // 년, 월, 일, 내용 순서. 기본 주기는 1달 → 2026-11-26
        assertEquals(listOf("2026", "11", "26"), fields.take(3).map { it.text.toString() })
        click(a, "1주")
        assertEquals(listOf("2026", "11", "2"), fields.take(3).map { it.text.toString() })
        click(a, "1년")
        assertEquals(listOf("2027", "10", "26"), fields.take(3).map { it.text.toString() })
        fields[1].setText("2"); fields[2].setText("30")
        assertTrue(texts(root(a)).any { it.contains("없는 날짜") })
        click(a, "저장")
        assertTrue(store.state.goals.isEmpty())
        fields[1].setText("12"); fields[2].setText("31")
        fields[3].setText("새 목표")
        click(a, "저장")
        val g = store.state.goals.single()
        assertEquals("새 목표", g.text)
        assertEquals(LocalDate.of(2027, 12, 31), g.target)
        assertEquals(Period.Y1, g.period)
    }

    @Test fun extendingAnOverdueGoal() {
        val today = LocalDate.of(2026, 10, 26)
        store.update { Ops.addGoal(it, "지난 일", Period.W1, today.minusDays(2), today) }
        val id = store.state.goals.single().id
        val a = start(GoalEditActivity::class.java, Intent(context, GoalEditActivity::class.java)
            .putExtra(GoalEditActivity.EXTRA_MODE, GoalEditActivity.EXTEND).putExtra(GoalEditActivity.EXTRA_ID, id)).get()
        click(a, "+1달")
        click(a, "연장하기")
        val g = store.state.goals.single()
        assertEquals(LocalDate.of(2026, 11, 26), g.target)
        assertEquals(Period.M1, g.period)
    }

    // ---- 설정 ----

    @Test fun settingsTogglesAndFontScale() {
        val ctrl = start(SettingsActivity::class.java)
        val a = ctrl.get()
        val switches = find(root(a), { it is BwSwitch }).map { it as BwSwitch }
        assertEquals(5, switches.size)
        switches[2].performClick()   // 1달 정리 끄기
        assertFalse(store.state.settings.m1On)
        switches[0].performClick()   // 전체 끄기
        assertFalse(store.state.settings.goalsOn)
        click(root(a).let { a }, "크게")
        assertEquals(1.15f, store.state.settings.fontScale, 0.001f)
    }

    @Test fun fontScaleIsAppliedToScreens() {
        store.update { Ops.setSettings(it) { s -> s.copy(fontScale = 1.3f) } }
        val a = start(HomeActivity::class.java).get()
        assertEquals(1.3f, a.resources.configuration.fontScale, 0.001f)
    }

    // ---- 메모리 ----

    @Test fun memorySearchFiltersTheList() {
        store.update { it.copy(archive = listOf(
            DayArchive(LocalDate.of(2026, 10, 24), memos = listOf(MemoRec("15:12", "새 아이디어")),
                done = listOf(DoneRec("09:10", "18:44", Kind.LATER, "빨래 널기"))),
            DayArchive(LocalDate.of(2026, 10, 22), late = listOf(LateRec("21:50", "화분 물 주기"))))) }
        val a = start(MemoryActivity::class.java).get()
        assertTrue(texts(root(a)).any { it.startsWith("전체 3건") })
        val search = find(root(a), { it is EditText }).first() as EditText
        search.setText("빨래")
        assertTrue(texts(root(a)).any { it.startsWith("검색 결과 1건") })
        search.setText("10-22")
        assertTrue(texts(root(a)).any { it.startsWith("검색 결과 1건") })
        search.setText("없는말")
        assertTrue(texts(root(a)).contains("검색 결과가 없어요"))
        val buttons = texts(root(a))
        assertTrue(buttons.contains("오늘\n불러오기") && buttons.contains("전체\n내보내기"))
    }

    @Test fun memoryResetDeletesOnlyUpToPickedDate() {
        store.update { it.copy(archive = listOf(
            DayArchive(LocalDate.of(2026, 10, 25), memos = listOf(MemoRec("15:12", "어제 메모"))),
            DayArchive(LocalDate.of(2026, 10, 24), memos = listOf(MemoRec("15:12", "새 아이디어")), late = listOf(LateRec("21:50", "화분"))),
            DayArchive(LocalDate.of(2026, 10, 22), late = listOf(LateRec("21:50", "화분 물 주기"))))) }
        val a = start(MemoryActivity::class.java).get()
        assertTrue(texts(root(a)).contains("메모리 리셋 (날짜까지 지우기)"))
        click(a, "메모리 리셋 (날짜까지 지우기)")
        assertTrue(dialogTexts().contains("날짜 고르기"))
        // 기본 선택은 어제(10.25). 24일로 바꾼다.
        val day24 = find(latestDialog().window!!.decorView, { it is TextView && it.contentDescription?.toString() == "10월 24일" }).single()
        day24.performClick()
        dialogClick("이 날짜로 정하기")
        val ask = dialogTexts().first { it.contains("지울까요?") }
        assertTrue(ask.contains("2026.10.24까지") && ask.contains("2일") && ask.contains("3건"))
        assertTrue(ask.contains("기록장") && ask.contains("장기 목표") && ask.contains("설정"))
        dialogClick("취소")
        assertEquals(3, store.state.archive.size)                 // 취소하면 그대로
        click(a, "메모리 리셋 (날짜까지 지우기)")
        find(latestDialog().window!!.decorView, { it is TextView && it.contentDescription?.toString() == "10월 24일" }).single().performClick()
        dialogClick("이 날짜로 정하기")
        dialogClick("지우기")
        assertEquals(listOf(LocalDate.of(2026, 10, 25)), store.state.archive.map { it.date })   // 25일만 남는다
    }

    @Test fun memoryResetKeepsGoalsItemsAndSettings() {
        val today = LocalDate.of(2026, 10, 26)
        store.update {
            var s = Ops.addGoal(it, "진행 중 목표", Period.M1, today.plusDays(20), today)
            s = s.copy(archive = listOf(DayArchive(LocalDate.of(2026, 10, 20), memos = listOf(MemoRec("09:00", "옛 메모")))))
            Ops.resetMemoryUpTo(s, LocalDate.of(2026, 10, 25))
        }
        assertTrue(store.state.archive.isEmpty())
        assertEquals("진행 중 목표", store.state.goals.single().text)
        assertTrue(store.state.onboarded)
    }

    @Test fun memoryShowsGoalRecordsInThreeLines() {
        store.update { it.copy(archive = listOf(DayArchive(LocalDate.of(2026, 10, 24),
            goals = listOf(GoalRec(28, "여행 계획")),
            goalDone = listOf(GoalDoneRec(27, "이력서", "2026-10-05 09:12", "2026-10-24 08:05", "18일 22시간"))))) }
        val a = start(MemoryActivity::class.java).get()
        val t = texts(root(a))
        assertTrue(t.contains("ㄴ ✔ 해결 D-27") && t.contains("ㄴ D-28"))
        assertTrue(t.contains("작성 2026-10-05 09:12") && t.contains("해결 2026-10-24 08:05") && t.contains("작성 후 18일 22시간 걸림"))
        assertFalse(t.any { it.contains("보다") && it.contains("남기고") })
    }

    // ---- 알림과 알람 ----

    @Test fun dueItemPostsOneNotificationPerItemAndSchedulesNextAlarm() {
        grantNotifications()
        store.update { it.copy(mode = it.mode.copy(fired = it.settings.cleanups.map { c -> c.id }.toSet())) }
        store.update { var s = Ops.addItem(it, Kind.NOW, "A", now); Ops.addItem(s, Kind.NOW, "B", now) }
        now += 10 * 60_000
        AlarmScheduler.run(context)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val shown = shadowOf(nm).allNotifications
        assertEquals(2, shown.size)
        val titles = shown.map { it.extras.getCharSequence("android.title").toString() }.sorted()
        assertEquals(listOf("• A", "• B"), titles)
        assertTrue(shown.all { it.actions.size == 1 && it.actions[0].title == "완료" })
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        assertNotNull(shadowOf(am).peekNextScheduledAlarm())
    }

    @Test fun doneActionResolvesOnlyThatItem() {
        store.update { var s = Ops.addItem(it, Kind.NOW, "A", now); Ops.addItem(s, Kind.NOW, "B", now) }
        val idA = store.state.items.first { it.text == "A" }.id
        ActionReceiver().onReceive(context, Intent(Notifier.ACTION_DONE).putExtra(Notifier.EXTRA_ITEM_ID, idA))
        // goAsync 스레드가 끝나기를 기다린다
        val end = System.currentTimeMillis() + 3000
        while (System.currentTimeMillis() < end && !store.state.items.first { it.text == "A" }.done) Thread.sleep(20)
        assertTrue(store.state.items.first { it.text == "A" }.done)
        assertFalse(store.state.items.first { it.text == "B" }.done)
    }

    @Test fun cleanupNotificationIsPostedAtItsTime() {
        grantNotifications()
        now = ms(2026, 10, 26, 19, 0)
        AlarmScheduler.run(context)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val titles = shadowOf(nm).allNotifications.map { it.extras.getCharSequence("android.title").toString() }
        assertEquals(listOf("저녁 정리 시간이에요"), titles)
    }
}
