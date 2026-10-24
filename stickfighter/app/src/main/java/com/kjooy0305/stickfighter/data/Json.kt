package com.kjooy0305.stickfighter.data

import com.kjooy0305.stickfighter.model.Animation
import com.kjooy0305.stickfighter.model.Figure
import com.kjooy0305.stickfighter.model.Frame
import com.kjooy0305.stickfighter.model.Segment
import com.kjooy0305.stickfighter.model.Shape
import org.json.JSONArray
import org.json.JSONObject

/** 저장 파일(JSON)과 모델 사이의 변환. 파일 형식을 바꿀 때는 VERSION을 올린다. */
object Json {
    const val VERSION = 1

    fun toJson(a: Animation): JSONObject = JSONObject().apply {
        put("v", VERSION)
        put("id", a.id)
        put("title", a.title)
        put("ratio", a.ratio.toDouble())
        put("bg", a.background)
        put("kps", a.keyFramesPerSecond.toDouble())
        put("fps", a.fps)
        put("repeat", a.repeat)
        put("updated", a.updatedAt)
        put("frames", JSONArray().apply {
            a.frames.forEach { frame -> put(JSONArray().apply { frame.figures.forEach { put(toJson(it)) } }) }
        })
    }

    fun animationFrom(o: JSONObject): Animation {
        val frames = o.getJSONArray("frames").let { arr ->
            (0 until arr.length()).map { i ->
                val figs = arr.getJSONArray(i)
                Frame((0 until figs.length()).map { figureFrom(figs.getJSONObject(it)) })
            }
        }
        return Animation(
            id = o.getString("id"),
            title = o.optString("title", ""),
            ratio = o.optDouble("ratio", 9.0 / 16.0).toFloat(),
            background = o.optInt("bg", 0xFFFFFFFF.toInt()),
            keyFramesPerSecond = o.optDouble("kps", 2.0).toFloat(),
            fps = o.optInt("fps", 30),
            repeat = o.optBoolean("repeat", false),
            frames = frames.ifEmpty { listOf(Frame()) },
            updatedAt = o.optLong("updated", 0L)
        )
    }

    fun toJson(f: Figure): JSONObject = JSONObject().apply {
        put("id", f.id)
        put("name", f.name)
        put("x", f.x.toDouble())
        put("y", f.y.toDouble())
        put("scale", f.scale.toDouble())
        put("flip", f.flip)
        put("segs", JSONArray().apply {
            f.segments.forEach { s ->
                put(JSONObject().apply {
                    put("id", s.id)
                    put("p", s.parent)
                    put("a", s.angle.toDouble())
                    put("l", s.length.toDouble())
                    put("t", s.thickness.toDouble())
                    put("c", s.color)
                    put("s", s.shape.name)
                })
            }
        })
    }

    fun figureFrom(o: JSONObject): Figure {
        val segs = o.getJSONArray("segs")
        return Figure(
            id = o.getLong("id"),
            name = o.optString("name", ""),
            x = o.getDouble("x").toFloat(),
            y = o.getDouble("y").toFloat(),
            scale = o.optDouble("scale", 1.0).toFloat(),
            flip = o.optBoolean("flip", false),
            segments = (0 until segs.length()).map { i ->
                val s = segs.getJSONObject(i)
                Segment(
                    id = s.getInt("id"),
                    parent = s.getInt("p"),
                    angle = s.getDouble("a").toFloat(),
                    length = s.getDouble("l").toFloat(),
                    thickness = s.getDouble("t").toFloat(),
                    color = s.getInt("c"),
                    shape = runCatching { Shape.valueOf(s.getString("s")) }.getOrDefault(Shape.LINE)
                )
            }
        )
    }
}
