package com.kjooy0305.dayclean.data

import com.kjooy0305.dayclean.model.AppState
import com.kjooy0305.dayclean.model.CleanupTime
import com.kjooy0305.dayclean.model.CleanupType
import com.kjooy0305.dayclean.model.DayArchive
import com.kjooy0305.dayclean.model.Disposition
import com.kjooy0305.dayclean.model.DoneRec
import com.kjooy0305.dayclean.model.Goal
import com.kjooy0305.dayclean.model.GoalReviewState
import com.kjooy0305.dayclean.model.Item
import com.kjooy0305.dayclean.model.Kind
import com.kjooy0305.dayclean.model.LateRec
import com.kjooy0305.dayclean.model.MemoRec
import com.kjooy0305.dayclean.model.ModeState
import com.kjooy0305.dayclean.model.Period
import com.kjooy0305.dayclean.model.RestInterval
import com.kjooy0305.dayclean.model.Settings
import org.json.JSONArray
import org.json.JSONObject
import java.time.DayOfWeek
import java.time.LocalDate

/** 저장 파일과 상태 사이의 변환. 형식을 바꿀 때는 VERSION을 올린다. */
object StateJson {
    const val VERSION = 1

    private fun <T> JSONArray.map(f: (JSONObject) -> T): List<T> = (0 until length()).map { f(getJSONObject(it)) }
    private fun JSONObject.longOrNull(k: String): Long? = if (has(k) && !isNull(k)) getLong(k) else null
    private fun JSONObject.strOrNull(k: String): String? = if (has(k) && !isNull(k)) getString(k) else null

    fun toJson(s: AppState): JSONObject = JSONObject().apply {
        put("v", VERSION)
        put("nextId", s.nextId)
        put("onboarded", s.onboarded)
        put("items", JSONArray().apply { s.items.forEach { put(item(it)) } })
        put("goals", JSONArray().apply { s.goals.forEach { put(goal(it)) } })
        put("archive", JSONArray().apply { s.archive.forEach { put(day(it)) } })
        put("settings", settings(s.settings))
        put("mode", JSONObject().apply {
            put("date", s.mode.date.toString())
            put("completed", JSONArray(s.mode.completed.toList()))
            put("fired", JSONArray(s.mode.fired.toList()))
        })
        put("review", JSONObject().apply {
            put("w", s.review.last1w?.toString() ?: JSONObject.NULL)
            put("m", s.review.last1m?.toString() ?: JSONObject.NULL)
            put("y", s.review.last1y ?: JSONObject.NULL)
        })
    }

    fun fromJson(o: JSONObject): AppState {
        val mode = o.getJSONObject("mode")
        val review = o.optJSONObject("review")
        return AppState(
            items = o.getJSONArray("items").map { item(it) },
            goals = o.getJSONArray("goals").map { goal(it) },
            settings = settings(o.getJSONObject("settings")),
            archive = o.getJSONArray("archive").map { day(it) },
            mode = ModeState(
                LocalDate.parse(mode.getString("date")),
                longs(mode.getJSONArray("completed")), longs(mode.getJSONArray("fired"))
            ),
            review = GoalReviewState(
                review?.strOrNull("w")?.let { LocalDate.parse(it) },
                review?.strOrNull("m")?.let { LocalDate.parse(it) },
                review?.strOrNull("y")
            ),
            nextId = o.getLong("nextId"),
            onboarded = o.optBoolean("onboarded", false)
        )
    }

    private fun longs(a: JSONArray): Set<Long> = (0 until a.length()).map { a.getLong(it) }.toSet()

    private fun item(i: Item) = JSONObject().apply {
        put("id", i.id); put("k", i.kind.name); put("t", i.text); put("d", i.date.toString()); put("c", i.createdAt)
        put("done", i.done); put("doneAt", i.doneAt ?: JSONObject.NULL)
        put("carried", i.carried)
        put("disp", i.disposition?.name ?: JSONObject.NULL); put("decAt", i.decidedAt ?: JSONObject.NULL)
        put("start", i.startCleanupId ?: JSONObject.NULL); put("alert", i.nextAlertAt ?: JSONObject.NULL)
        put("goal", i.goalId ?: JSONObject.NULL)
    }

    private fun item(o: JSONObject) = Item(
        id = o.getLong("id"), kind = Kind.valueOf(o.getString("k")), text = o.getString("t"),
        date = LocalDate.parse(o.getString("d")), createdAt = o.getLong("c"),
        done = o.optBoolean("done"), doneAt = o.longOrNull("doneAt"), carried = o.optBoolean("carried"),
        disposition = o.strOrNull("disp")?.let { Disposition.valueOf(it) }, decidedAt = o.longOrNull("decAt"),
        startCleanupId = o.longOrNull("start"), nextAlertAt = o.longOrNull("alert"), goalId = o.longOrNull("goal")
    )

    private fun goal(g: Goal) = JSONObject().apply {
        put("id", g.id); put("t", g.text); put("p", g.period.key); put("target", g.target.toString())
        put("seq", g.seq); put("on", g.createdOn.toString())
    }

    private fun goal(o: JSONObject) = Goal(
        o.getLong("id"), o.getString("t"), Period.of(o.getString("p")), LocalDate.parse(o.getString("target")),
        o.getLong("seq"), LocalDate.parse(o.getString("on"))
    )

    private fun day(d: DayArchive) = JSONObject().apply {
        put("date", d.date.toString())
        put("late", JSONArray().apply { d.late.forEach { put(JSONObject().put("t", it.time).put("x", it.text)) } })
        put("memo", JSONArray().apply { d.memos.forEach { put(JSONObject().put("t", it.time).put("x", it.text)) } })
        put("done", JSONArray().apply {
            d.done.forEach { put(JSONObject().put("w", it.written).put("r", it.resolved).put("k", it.kind.name).put("x", it.text)) }
        })
    }

    private fun day(o: JSONObject) = DayArchive(
        LocalDate.parse(o.getString("date")),
        o.getJSONArray("late").map { LateRec(it.getString("t"), it.getString("x")) },
        o.getJSONArray("memo").map { MemoRec(it.getString("t"), it.getString("x")) },
        o.getJSONArray("done").map { DoneRec(it.getString("w"), it.getString("r"), Kind.valueOf(it.getString("k")), it.getString("x")) }
    )

    private fun settings(s: Settings) = JSONObject().apply {
        put("fs", s.fontScale.toDouble())
        put("goalsOn", s.goalsOn); put("w1", s.w1On); put("m1", s.m1On); put("y1", s.y1On)
        put("auto", s.autoAdd); put("laterStart", s.defaultLaterStartId)
        put("cleanups", JSONArray().apply {
            s.cleanups.forEach { put(JSONObject().put("id", it.id).put("n", it.name).put("m", it.minuteOfDay).put("type", it.type.name).put("b", it.builtIn)) }
        })
        put("rests", JSONArray().apply {
            s.rests.forEach { r ->
                put(JSONObject().put("id", r.id).put("s", r.startMin).put("e", r.endMin)
                    .put("d", JSONArray(r.days.map { it.value }.sorted())))
            }
        })
    }

    private fun settings(o: JSONObject) = Settings(
        fontScale = o.optDouble("fs", 1.0).toFloat(),
        goalsOn = o.optBoolean("goalsOn", true), w1On = o.optBoolean("w1", true), m1On = o.optBoolean("m1", true),
        y1On = o.optBoolean("y1", true), autoAdd = o.optBoolean("auto", true),
        defaultLaterStartId = o.optLong("laterStart", 2L),
        cleanups = o.getJSONArray("cleanups").map {
            CleanupTime(it.getLong("id"), it.getString("n"), it.getInt("m"), CleanupType.valueOf(it.getString("type")), it.getBoolean("b"))
        },
        rests = o.getJSONArray("rests").map { r ->
            val days = r.getJSONArray("d")
            RestInterval(r.getLong("id"), r.getInt("s"), r.getInt("e"), (0 until days.length()).map { DayOfWeek.of(days.getInt(it)) }.toSet())
        }
    )
}
