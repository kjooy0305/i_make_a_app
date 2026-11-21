package com.kjooy0305.padmacro

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * 매크로 하나: 이름 + 단계 목록 + 실행 설정.
 * repeat 0 = 멈출 때까지 무한 반복. gapMs 는 단계 사이 기본 대기, startDelayMs 는 시작 버튼을 누른 뒤 카운트다운.
 */
data class Macro(
    val id: String = UUID.randomUUID().toString(),
    var name: String = "새 매크로",
    var steps: MutableList<Step> = mutableListOf(),
    var repeat: Int = 1,
    var gapMs: Long = 500,
    var startDelayMs: Long = 3000,
    var repeatGapMs: Long = 0,
    var vibrateOnEnd: Boolean = true,
) {
    fun toJson(): JSONObject = JSONObject()
        .put("id", id)
        .put("name", name)
        .put("repeat", repeat)
        .put("gap", gapMs)
        .put("startDelay", startDelayMs)
        .put("repeatGap", repeatGapMs)
        .put("vibrateOnEnd", vibrateOnEnd)
        .put("steps", JSONArray().also { arr -> steps.forEach { arr.put(it.toJson()) } })

    fun copy(newName: String): Macro = Macro(
        name = newName, steps = steps.toMutableList(), repeat = repeat, gapMs = gapMs,
        startDelayMs = startDelayMs, repeatGapMs = repeatGapMs, vibrateOnEnd = vibrateOnEnd,
    )

    /** 반복·대기 설정을 한 줄로 */
    fun summary(): String {
        val rep = if (repeat <= 0) "무한 반복" else if (repeat == 1) "1회" else "${repeat}회 반복"
        return "${steps.size}단계 · $rep · 사이 ${Step.fmtMs(gapMs)}"
    }

    companion object {
        fun fromJson(o: JSONObject): Macro {
            val steps = mutableListOf<Step>()
            val arr = o.optJSONArray("steps") ?: JSONArray()
            for (i in 0 until arr.length()) Step.fromJson(arr.getJSONObject(i))?.let { steps.add(it) }
            return Macro(
                id = o.optString("id", UUID.randomUUID().toString()),
                name = o.optString("name", "매크로"),
                steps = steps,
                repeat = o.optInt("repeat", 1),
                gapMs = o.optLong("gap", 500),
                startDelayMs = o.optLong("startDelay", 3000),
                repeatGapMs = o.optLong("repeatGap", 0),
                vibrateOnEnd = o.optBoolean("vibrateOnEnd", true),
            )
        }
    }
}
