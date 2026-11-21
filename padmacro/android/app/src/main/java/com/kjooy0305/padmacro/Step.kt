package com.kjooy0305.padmacro

import org.json.JSONObject

/**
 * 매크로의 한 단계. 종류마다 필요한 값만 가진다.
 * JSON 으로 저장되므로 type 이름은 바꾸지 않는다.
 */
sealed class Step {
    abstract val type: String

    /** 좌표 탭. count 가 2면 두 번 연속 탭 */
    data class Tap(val x: Int, val y: Int, val count: Int = 1) : Step() { override val type = "tap" }

    /** 길게 누르기 */
    data class LongPress(val x: Int, val y: Int, val durationMs: Long = 600) : Step() { override val type = "long" }

    /** 스와이프(드래그) */
    data class Swipe(val x1: Int, val y1: Int, val x2: Int, val y2: Int, val durationMs: Long = 300) : Step() { override val type = "swipe" }

    /** 대기 */
    data class Wait(val ms: Long) : Step() { override val type = "wait" }

    /** 화면 캡쳐(갤러리 > Pictures/패드매크로 에 저장) */
    data class Capture(val label: String = "") : Step() { override val type = "capture" }

    /** 다른 앱 실행 */
    data class LaunchApp(val packageName: String, val label: String = "") : Step() { override val type = "app" }

    /** 뒤로·홈·최근 앱 등 시스템 동작 */
    data class Global(val action: String) : Step() { override val type = "global" }

    /** 화면에서 글자를 찾아 누르기. 보일 때까지 timeoutMs 동안 기다린다 */
    data class ClickText(val text: String, val timeoutMs: Long = 5000, val exact: Boolean = false) : Step() { override val type = "clicktext" }

    /** 글자가 나타날 때까지 기다리기(누르지는 않음). 안 나타나면 매크로 중단 여부는 stopIfMissing */
    data class WaitText(val text: String, val timeoutMs: Long = 10000, val stopIfMissing: Boolean = true) : Step() { override val type = "waittext" }

    /** URL 열기 */
    data class OpenUrl(val url: String) : Step() { override val type = "url" }

    /** 진동으로 알려 주기 */
    data class Vibrate(val ms: Long = 300) : Step() { override val type = "vibrate" }

    /** 사람이 읽기 좋은 한 줄 설명 */
    fun describe(): String = when (this) {
        is Tap -> if (count > 1) "탭 ×$count  ($x, $y)" else "탭  ($x, $y)"
        is LongPress -> "길게 누르기 ${durationMs}ms  ($x, $y)"
        is Swipe -> "스와이프  ($x1, $y1) → ($x2, $y2)  ${durationMs}ms"
        is Wait -> "대기  ${fmtMs(ms)}"
        is Capture -> if (label.isBlank()) "화면 캡쳐" else "화면 캡쳐  “$label”"
        is LaunchApp -> "앱 실행  ${if (label.isBlank()) packageName else label}"
        is Global -> "시스템  ${GLOBAL_LABELS[action] ?: action}"
        is ClickText -> "글자 찾아 누르기  “$text”" + (if (exact) " (정확히)" else "") + "  최대 ${fmtMs(timeoutMs)}"
        is WaitText -> "글자 기다리기  “$text”  최대 ${fmtMs(timeoutMs)}" + (if (stopIfMissing) ", 없으면 중단" else "")
        is OpenUrl -> "URL 열기  $url"
        is Vibrate -> "진동  ${ms}ms"
    }

    fun toJson(): JSONObject {
        val o = JSONObject().put("type", type)
        when (this) {
            is Tap -> o.put("x", x).put("y", y).put("count", count)
            is LongPress -> o.put("x", x).put("y", y).put("duration", durationMs)
            is Swipe -> o.put("x1", x1).put("y1", y1).put("x2", x2).put("y2", y2).put("duration", durationMs)
            is Wait -> o.put("ms", ms)
            is Capture -> o.put("label", label)
            is LaunchApp -> o.put("package", packageName).put("label", label)
            is Global -> o.put("action", action)
            is ClickText -> o.put("text", text).put("timeout", timeoutMs).put("exact", exact)
            is WaitText -> o.put("text", text).put("timeout", timeoutMs).put("stop", stopIfMissing)
            is OpenUrl -> o.put("url", url)
            is Vibrate -> o.put("ms", ms)
        }
        return o
    }

    companion object {
        const val BACK = "back"
        const val HOME = "home"
        const val RECENTS = "recents"
        const val NOTIFICATIONS = "notifications"
        const val QUICK_SETTINGS = "quick_settings"
        const val LOCK = "lock"
        const val POWER = "power"
        const val SCREENSHOT_SYS = "screenshot"

        val GLOBAL_LABELS = linkedMapOf(
            BACK to "뒤로",
            HOME to "홈",
            RECENTS to "최근 앱",
            NOTIFICATIONS to "알림창 열기",
            QUICK_SETTINGS to "빠른 설정 열기",
            LOCK to "화면 잠금 (안드로이드 9+)",
            POWER to "전원 메뉴",
        )

        fun fmtMs(ms: Long): String = when {
            ms % 1000 == 0L -> "${ms / 1000}초"
            ms >= 1000 -> String.format("%.1f초", ms / 1000.0)
            else -> "${ms}ms"
        }

        fun fromJson(o: JSONObject): Step? = when (o.optString("type")) {
            "tap" -> Tap(o.optInt("x"), o.optInt("y"), o.optInt("count", 1).coerceIn(1, 10))
            "long" -> LongPress(o.optInt("x"), o.optInt("y"), o.optLong("duration", 600))
            "swipe" -> Swipe(o.optInt("x1"), o.optInt("y1"), o.optInt("x2"), o.optInt("y2"), o.optLong("duration", 300))
            "wait" -> Wait(o.optLong("ms", 1000))
            "capture" -> Capture(o.optString("label", ""))
            "app" -> LaunchApp(o.optString("package"), o.optString("label", ""))
            "global" -> Global(o.optString("action", BACK))
            "clicktext" -> ClickText(o.optString("text"), o.optLong("timeout", 5000), o.optBoolean("exact", false))
            "waittext" -> WaitText(o.optString("text"), o.optLong("timeout", 10000), o.optBoolean("stop", true))
            "url" -> OpenUrl(o.optString("url"))
            "vibrate" -> Vibrate(o.optLong("ms", 300))
            else -> null
        }
    }
}
