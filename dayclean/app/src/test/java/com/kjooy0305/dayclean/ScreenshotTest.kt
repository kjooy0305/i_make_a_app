package com.kjooy0305.dayclean

import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.test.core.app.ApplicationProvider
import com.kjooy0305.dayclean.data.Store
import com.kjooy0305.dayclean.logic.Dates
import com.kjooy0305.dayclean.logic.Gate
import com.kjooy0305.dayclean.logic.Ops
import com.kjooy0305.dayclean.model.*
import com.kjooy0305.dayclean.ui.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.nio.file.Files
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/** 화면을 PNG로 저장해서 눈으로 확인하기 위한 시험. 결과는 임시 폴더의 dc-shots 에 쌓인다. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w360dp-h760dp-xxhdpi")
class ScreenshotTest {
    private val context get() = ApplicationProvider.getApplicationContext<Application>()
    private val out = File(System.getProperty("java.io.tmpdir"), "dc-shots").apply { mkdirs() }

    private fun ms(y: Int, m: Int, d: Int, h: Int = 0, min: Int = 0) = Dates.toMillis(LocalDateTime.of(y, m, d, h, min))

    private fun seed(now: Long): Store {
        Tz.zone = ZoneId.of("UTC")
        val today = LocalDate.of(2026, 10, 26)
        val store = Store(File(Files.createTempDirectory("dcs").toFile(), "dayclean.json")) { now }
        store.update { it.copy(mode = ModeState(today), onboarded = true) }
        fun add(k: Kind, t: String, h: Int, min: Int = 0, carried: Boolean = false) =
            store.update { Ops.addItem(it, k, t, ms(2026, 10, 26, h, min), carried = carried) }
        add(Kind.NOW, "병원 예약 전화하기", 6, 10, carried = true)
        add(Kind.LATER, "서랍 정리", 6, 11, carried = true)
        add(Kind.NOW, "보고서 초안 보내기", 6, 20); add(Kind.NOW, "약 먹기", 6, 25); add(Kind.NOW, "세탁소에 전화하기", 7, 0)
        add(Kind.LATER, "빨래 널기", 7, 10); add(Kind.LATER, "택배 상자 정리", 7, 12); add(Kind.LATER, "화분 물 주기", 7, 14)
        add(Kind.MEMO, "새 아이디어: 주간 목표는 월요일 아침에 쓰기", 7, 30); add(Kind.MEMO, "와이파이 비밀번호는 냉장고 옆 메모에 있음", 7, 31)
        add(Kind.NOW, "물 마시기", 6, 0); add(Kind.NOW, "스트레칭 10분", 6, 5)
        val done = store.state.items.filter { it.text == "물 마시기" || it.text == "스트레칭 10분" }
        done.forEachIndexed { i, it -> store.update { s -> Ops.toggleDone(s, it.id, ms(2026, 10, 26, 6, 48 + i)) } }
        Store.reset(store)
        val names = listOf("이력서 초안 끝내기" to 3, "운동 12회 채우기" to 7, "월간 지출 정리하기" to 9, "자격증 접수" to 10, "독서 모임 발표 준비" to 14,
            "책상 서랍 전부 비우기" to 17, "독서 4권" to 21, "연말 모임 장소 예약" to 24, "이사 준비 시작" to 28, "건강검진 받기" to 31,
            "영어 점수 올리기" to 60, "여행 계획 확정" to 90, "저축 목표 달성" to 200, "1년 회고 쓰기" to 365, "지난 목표 예시" to -2)
        names.forEach { (n, off) ->
            store.update { s ->
                val d = today.plusDays(off.toLong())
                Ops.addGoal(s, n, Dates.classify(off.toLong()), d, today)
            }
        }
        store.update { s ->
            var x = Ops.addCleanup(s, "점심 점검", 12 * 60 + 30, CleanupType.MORNING)
            x = Ops.addRest(x, 23 * 60, 7 * 60, DayOfWeek.values().toSet())
            x = Ops.addRest(x, 13 * 60, 14 * 60, setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY))
            x.copy(archive = listOf(
                DayArchive(LocalDate.of(2026, 10, 24), late = listOf(LateRec("21:40", "병원 예약 전화하기"), LateRec("21:40", "서랍 정리")),
                    memos = listOf(MemoRec("15:12", "새 아이디어: 주간 목표는 월요일 아침에 쓰기")),
                    done = listOf(DoneRec("06:30", "08:05", Kind.NOW, "보고서 초안 보내기"), DoneRec("09:10", "18:44", Kind.LATER, "빨래 널기"))),
                DayArchive(LocalDate.of(2026, 10, 23), late = listOf(LateRec("21:35", "택배 상자 정리")),
                    done = listOf(DoneRec("07:05", "07:50", Kind.NOW, "약 먹기")))
            ))
        }
        return store
    }

    private fun shot(name: String, view: View) {
        val w = 1080; val h = 2280
        view.measure(View.MeasureSpec.makeMeasureSpec(w, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(h, View.MeasureSpec.EXACTLY))
        view.layout(0, 0, w, h)
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        Canvas(bmp).also { it.drawColor(android.graphics.Color.WHITE) }.let { view.draw(it) }
        File(out, "$name.png").outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    private fun <T : android.app.Activity> open(c: Class<T>, intent: Intent? = null): T =
        (if (intent != null) Robolectric.buildActivity(c, intent) else Robolectric.buildActivity(c)).setup().get()

    private fun cleanupIntent(c: Class<*>, id: Long) = Intent(context, c).putExtra(HomeActivity.EXTRA_CLEANUP_ID, id)

    @Test fun shots() {
        val store = seed(ms(2026, 10, 26, 10, 0))
        for (c in store.state.settings.cleanups) store.update { Gate.complete(it, c.id) }
        // 장기 목표 점검 화면은 점검 전에 찍고, 나머지 화면을 위해 점검을 끝낸 상태로 바꾼다.
        shot("10_goalcheck", open(GoalCheckActivity::class.java).window.decorView)
        val today = LocalDate.of(2026, 10, 26)
        store.update { it.copy(review = GoalReviewState(today, today, "2026-10")) }
        shot("01_home", open(HomeActivity::class.java).window.decorView)
        shot("02_settings", open(SettingsActivity::class.java).window.decorView)
        shot("03_memory", open(MemoryActivity::class.java).window.decorView)
        shot("04_goals", open(GoalsActivity::class.java).window.decorView)
        shot("05_goal_new", open(GoalEditActivity::class.java, Intent(context, GoalEditActivity::class.java).putExtra(GoalEditActivity.EXTRA_MODE, GoalEditActivity.NEW)).window.decorView)
        val late = store.state.goals.first { it.text == "지난 목표 예시" }
        shot("06_goal_extend", open(GoalEditActivity::class.java, Intent(context, GoalEditActivity::class.java)
            .putExtra(GoalEditActivity.EXTRA_MODE, GoalEditActivity.EXTEND).putExtra(GoalEditActivity.EXTRA_ID, late.id)).window.decorView)
        shot("07_morning", open(MorningActivity::class.java, cleanupIntent(MorningActivity::class.java, MORNING_ID)).window.decorView)
        shot("08_evening", open(EveningActivity::class.java, cleanupIntent(EveningActivity::class.java, EVENING_ID)).window.decorView)
        shot("09_review", open(ReviewActivity::class.java, cleanupIntent(ReviewActivity::class.java, REVIEW_ID)).window.decorView)

        // 대화상자
        val home = open(HomeActivity::class.java)
        val it = store.state.items.first { i -> i.text == "병원 예약 전화하기" }
        Dialogs.editItem(home, it.text, Kind.LATER, null, store.state.settings, { _, _, _ -> }, { })
        shot("11_edit_sheet", org.robolectric.shadows.ShadowDialog.getLatestDialog().window!!.decorView)
        Dialogs.calendar(home, LocalDate.of(2026, 11, 26), LocalDate.of(2026, 10, 26)) { }
        shot("12_calendar", org.robolectric.shadows.ShadowDialog.getLatestDialog().window!!.decorView)
    }
}
