package com.kjooy0305.dayclean.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId

/** 시간대. 테스트에서 고정할 수 있게 한곳에 둔다. */
object Tz {
    @Volatile
    var zone: ZoneId = ZoneId.systemDefault()
}

/** 기록 유형. • 지금 당장, - 오늘 안에(나중에), = 메모 */
enum class Kind(val glyph: String) {
    NOW("•"), LATER("-"), MEMO("=");

    companion object {
        fun of(glyph: String): Kind? = values().firstOrNull { it.glyph == glyph }
    }
}

/** 회고에서 못한 일을 어떻게 했는지. */
enum class Disposition { CARRY, DROP }

/**
 * 기록장의 한 줄.
 * date는 이 기록이 속한 날이다. 내일 할 일로 미리 적은 것은 date가 내일이고, 그날이 되면 기록장에 나타난다.
 */
data class Item(
    val id: Long,
    val kind: Kind,
    val text: String,
    val date: LocalDate,
    val createdAt: Long,
    val done: Boolean = false,
    val doneAt: Long? = null,
    /** 어제 넘긴 기록이라서 연노랑으로 보이는 것 */
    val carried: Boolean = false,
    val disposition: Disposition? = null,
    val decidedAt: Long? = null,
    /** '-' 알림이 시작될 정리 시간. null이면 설정의 기본값 */
    val startCleanupId: Long? = null,
    val nextAlertAt: Long? = null,
    /** 장기 목표에서 자동으로 들어온 항목이면 그 목표 */
    val goalId: Long? = null
) {
    /** 회고에서 이미 처리한 항목은 오늘 목록에서 숨긴다. */
    val decided: Boolean get() = disposition != null
}

enum class CleanupType { MORNING, EVENING, NOTIFY, REVIEW }

const val MORNING_ID = 1L
const val EVENING_ID = 2L
const val REVIEW_ID = 3L
const val FIRST_FREE_ID = 100L

data class CleanupTime(
    val id: Long,
    val name: String,
    val minuteOfDay: Int,
    val type: CleanupType,
    val builtIn: Boolean
)

fun defaultCleanups(): List<CleanupTime> = listOf(
    CleanupTime(MORNING_ID, "아침 정리", 7 * 60, CleanupType.MORNING, true),
    CleanupTime(EVENING_ID, "저녁 정리", 19 * 60, CleanupType.EVENING, true),
    CleanupTime(REVIEW_ID, "회고", 22 * 60, CleanupType.REVIEW, true)
)

/** 알림 쉬는 시간. endMin이 startMin보다 작거나 같으면 다음 날까지 이어진다. */
data class RestInterval(val id: Long, val startMin: Int, val endMin: Int, val days: Set<DayOfWeek>)

enum class Period(val key: String, val label: String) {
    W1("1w", "1주"), M1("1m", "1달"), Y1("1y", "1년");

    companion object {
        fun of(key: String): Period = values().firstOrNull { it.key == key } ?: M1
    }
}

data class Goal(
    val id: Long,
    val text: String,
    val period: Period,
    val target: LocalDate,
    /** 작성 순서. 클수록 나중에 쓴 것 */
    val seq: Long,
    val createdOn: LocalDate
)

data class Settings(
    val fontScale: Float = 1f,
    val goalsOn: Boolean = true,
    val w1On: Boolean = true,
    val m1On: Boolean = true,
    val y1On: Boolean = true,
    val autoAdd: Boolean = true,
    val defaultLaterStartId: Long = EVENING_ID,
    val cleanups: List<CleanupTime> = defaultCleanups(),
    val rests: List<RestInterval> = emptyList()
) {
    fun typeOn(p: Period): Boolean = goalsOn && when (p) {
        Period.W1 -> w1On
        Period.M1 -> m1On
        Period.Y1 -> y1On
    }

    fun cleanup(id: Long?): CleanupTime? = cleanups.firstOrNull { it.id == id }
}

// ---- 메모리(자정에 옮겨 담는 지난 기록) ----

data class LateRec(val time: String, val text: String)
data class MemoRec(val time: String, val text: String)
data class DoneRec(val written: String, val resolved: String, val kind: Kind, val text: String) {
    val priority: String get() = if (kind == Kind.NOW) "지금 우선" else "나중 일"
}

data class DayArchive(
    val date: LocalDate,
    val late: List<LateRec> = emptyList(),
    val memos: List<MemoRec> = emptyList(),
    val done: List<DoneRec> = emptyList()
) {
    val isEmpty: Boolean get() = late.isEmpty() && memos.isEmpty() && done.isEmpty()
}

/** 오늘의 정리 진행 상황. 날짜가 바뀌면 새로 시작한다. */
data class ModeState(
    val date: LocalDate,
    val completed: Set<Long> = emptySet(),
    val fired: Set<Long> = emptySet()
)

/** 장기 목표 점검을 마지막으로 한 때. */
data class GoalReviewState(
    val last1w: LocalDate? = null,
    val last1m: LocalDate? = null,
    /** "2026-10" 형식 */
    val last1y: String? = null
)

data class AppState(
    val items: List<Item> = emptyList(),
    val goals: List<Goal> = emptyList(),
    val settings: Settings = Settings(),
    val archive: List<DayArchive> = emptyList(),
    val mode: ModeState = ModeState(LocalDate.of(2000, 1, 1)),
    val review: GoalReviewState = GoalReviewState(),
    val nextId: Long = FIRST_FREE_ID,
    val onboarded: Boolean = false
) {
    /** 새 id를 하나 받고 갱신된 상태를 함께 돌려준다. */
    fun alloc(): Pair<Long, AppState> = nextId to copy(nextId = nextId + 1)
}
