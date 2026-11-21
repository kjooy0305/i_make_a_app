package com.kjooy0305.screenkeep

import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** 화면에 쓰는 시간 계산과 글. 안드로이드 기능 없이 시험할 수 있게 따로 뒀다. */
object Logic {
    /** 유지 시간 후보(분). 0은 전원 버튼을 누를 때까지 계속. */
    val DURATIONS = listOf(0, 15, 30, 60, 120, 240)

    /** 기기 기본 화면 꺼짐 시간 후보(밀리초). */
    val TIMEOUTS = listOf(15_000, 30_000, 60_000, 120_000, 300_000, 600_000, 1_800_000)

    fun durationLabel(min: Int): String = when {
        min <= 0 -> "계속"
        min < 60 -> "${min}분"
        min % 60 == 0 -> "${min / 60}시간"
        else -> "${min / 60}시간 ${min % 60}분"
    }

    fun timeoutLabel(ms: Int): String = when {
        ms < 60_000 -> "${ms / 1000}초"
        ms % 60_000 == 0 -> "${ms / 60_000}분"
        else -> "${ms / 1000}초"
    }

    fun endAtFor(now: Long, min: Int): Long = if (min <= 0) 0L else now + min * 60_000L

    fun isExpired(endAt: Long, now: Long): Boolean = endAt > 0 && now >= endAt

    /** 남은 시간 글. 끝나는 시각이 없으면 계속 켜 둔다는 뜻. */
    fun remainingText(endAt: Long, now: Long): String {
        if (endAt <= 0) return "전원 버튼을 누를 때까지 계속"
        val left = endAt - now
        if (left <= 0) return "곧 종료"
        val min = ((left + 59_999) / 60_000).toInt()
        val clock = SimpleDateFormat("a h:mm", Locale.KOREA).format(Date(endAt))
        return "$clock 까지 (${durationLabel(min)} 남음)"
    }
}

/** 화면이 꺼지거나 켜질 때 남기는 한 줄. 어떤 상황이었는지 나중에 비교하려고 배터리·절전 상태를 같이 적는다. */
data class LogEntry(
    val t: Long,
    val what: String,
    val keepOn: Boolean,
    val battery: Int,
    val charging: Boolean,
    val saver: Boolean,
) {
    fun toJson(): JSONObject = JSONObject().put("t", t).put("w", what).put("k", keepOn).put("b", battery).put("c", charging).put("s", saver)

    /** 유지 중인데 화면이 꺼진 기록(전원 버튼이거나 기기가 끈 경우)인지. */
    val unexpected: Boolean get() = keepOn && what == "화면 꺼짐"

    fun line(): String {
        val time = SimpleDateFormat("M/d HH:mm:ss", Locale.KOREA).format(Date(t))
        val parts = mutableListOf(what)
        if (battery >= 0) parts.add("배터리 $battery%" + if (charging) " 충전 중" else "")
        if (saver) parts.add("절전 모드")
        return "$time  " + parts.joinToString(" · ")
    }

    companion object {
        fun fromJson(o: JSONObject) = LogEntry(o.optLong("t"), o.optString("w"), o.optBoolean("k"), o.optInt("b", -1), o.optBoolean("c"), o.optBoolean("s"))

        const val MAX = 150

        fun parse(s: String?): List<LogEntry> {
            if (s.isNullOrBlank()) return emptyList()
            return try {
                val a = JSONArray(s)
                (0 until a.length()).map { fromJson(a.getJSONObject(it)) }
            } catch (e: Exception) { emptyList() }
        }

        fun serialize(list: List<LogEntry>): String = JSONArray().also { a -> list.forEach { a.put(it.toJson()) } }.toString()

        /** 새 기록을 뒤에 붙이고 오래된 것부터 버려 MAX개만 남긴다. */
        fun append(list: List<LogEntry>, e: LogEntry): List<LogEntry> = (list + e).takeLast(MAX)
    }
}
