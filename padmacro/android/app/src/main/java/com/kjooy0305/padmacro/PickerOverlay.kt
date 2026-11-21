package com.kjooy0305.padmacro

import android.accessibilityservice.GestureDescription
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.math.hypot

/**
 * 화면 전체를 덮는 투명 오버레이.
 *  - POINT: 한 번 터치한 자리의 좌표를 돌려준다 (탭·길게 누르기 단계용)
 *  - SWIPE: 드래그한 시작·끝 좌표를 돌려준다
 *  - RECORD: 터치를 계속 기록한다. 기록한 터치는 곧바로 제스처로 다시 보내서(그 순간만 오버레이를 터치 불가로)
 *            아래 앱이 실제로 반응하게 한다. 터치 사이 시간은 '대기' 단계로 남긴다.
 */
class PickerOverlay(
    private val service: MacroService,
    private val mode: Mode,
    private val macroId: String,
    private val callback: Callback,
) {
    enum class Mode { POINT, SWIPE, RECORD }

    interface Callback {
        fun onPicked(step: Step)
        fun onRecorded(steps: List<Step>)
        fun onCancelled()
    }

    private val wm = service.getSystemService(WindowManager::class.java)
    private val dens = service.resources.displayMetrics.density
    private fun dp(v: Int) = (v * dens).toInt()

    private val recorded = mutableListOf<Step>()
    private var lastGestureEnd = 0L
    private var downX = 0f; private var downY = 0f; private var downT = 0L
    private var replaying = false
    private var shown = false

    private val lp = WindowManager.LayoutParams(
        WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        PixelFormat.TRANSLUCENT,
    ).apply { gravity = Gravity.TOP or Gravity.START }

    private val marks = MarkView(service)
    private val banner: LinearLayout
    private val title: TextView
    private val count: TextView
    private val root: FrameLayout

    init {
        title = TextView(service).apply {
            setTextColor(Color.WHITE); textSize = 14f; typeface = Typeface.DEFAULT_BOLD
            text = when (mode) {
                Mode.POINT -> "원하는 위치를 한 번 누르세요"
                Mode.SWIPE -> "시작점에서 끝점까지 드래그하세요"
                Mode.RECORD -> "녹화 중 — 터치하는 대로 기록됩니다"
            }
        }
        count = TextView(service).apply { setTextColor(0xFFBBBBBB.toInt()); textSize = 12f; text = if (mode == Mode.RECORD) "0개 기록됨" else "" }
        val texts = LinearLayout(service).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            addView(title); addView(count)
        }
        banner = LinearLayout(service).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = GradientDrawable().apply { setColor(0xE6111111.toInt()); cornerRadius = dp(14).toFloat() }
            setPadding(dp(14), dp(10), dp(10), dp(10))
            elevation = dp(8).toFloat()
            addView(texts)
            if (mode == Mode.RECORD) {
                addView(pill("↶ 취소", 0xFF555555.toInt()) {
                    if (recorded.isNotEmpty()) {
                        recorded.removeAt(recorded.size - 1)
                        if (recorded.isNotEmpty() && recorded.last() is Step.Wait) recorded.removeAt(recorded.size - 1)
                        refreshCount()
                    }
                })
                addView(pill("■ 끝내기", 0xFFE74C3C.toInt()) { callback.onRecorded(recorded.toList()) })
            } else {
                addView(pill("취소", 0xFF555555.toInt()) { callback.onCancelled() })
            }
            // 배너 위 터치는 아래 투명 영역으로 넘기지 않는다
            setOnTouchListener { _, _ -> true }
        }
        root = FrameLayout(service).apply {
            setBackgroundColor(if (mode == Mode.RECORD) 0x08FF0000 else 0x22000000)
            addView(marks, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
            addView(banner, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.TOP; topMargin = statusBarHeight() + dp(8); leftMargin = dp(12); rightMargin = dp(12)
            })
            setOnTouchListener { _, e -> onTouch(e) }
        }
    }

    private fun statusBarHeight(): Int {
        val id = service.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (id > 0) service.resources.getDimensionPixelSize(id) else dp(24)
    }

    private fun pill(text: String, color: Int, onClick: () -> Unit) = TextView(service).apply {
        this.text = text; setTextColor(Color.WHITE); textSize = 13f; typeface = Typeface.DEFAULT_BOLD
        setPadding(dp(12), dp(8), dp(12), dp(8))
        background = GradientDrawable().apply { setColor(color); cornerRadius = dp(18).toFloat() }
        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { marginStart = dp(6) }
        setOnClickListener { onClick() }
    }

    private fun refreshCount() {
        val n = recorded.count { it !is Step.Wait }
        count.text = "${n}개 기록됨" + if (n > 0) "  ·  마지막: ${recorded.last().describe()}" else ""
    }

    private fun onTouch(e: MotionEvent): Boolean {
        if (replaying) return true
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> { downX = e.rawX; downY = e.rawY; downT = System.currentTimeMillis(); marks.start(e.rawX, e.rawY) }
            MotionEvent.ACTION_MOVE -> marks.move(e.rawX, e.rawY)
            MotionEvent.ACTION_UP -> {
                val upX = e.rawX; val upY = e.rawY
                val dur = System.currentTimeMillis() - downT
                val dist = hypot(upX - downX, upY - downY)
                val step: Step = when {
                    mode == Mode.SWIPE -> Step.Swipe(downX.toInt(), downY.toInt(), upX.toInt(), upY.toInt(), dur.coerceIn(100, 5000))
                    dist > dp(18) -> Step.Swipe(downX.toInt(), downY.toInt(), upX.toInt(), upY.toInt(), dur.coerceIn(100, 5000))
                    dur >= 450 -> Step.LongPress(downX.toInt(), downY.toInt(), dur.coerceAtMost(5000))
                    else -> Step.Tap(downX.toInt(), downY.toInt())
                }
                marks.end()
                if (mode == Mode.RECORD) record(step, downT) else callback.onPicked(step)
            }
            MotionEvent.ACTION_CANCEL -> marks.end()
        }
        return true
    }

    /** 기록하고, 같은 터치를 아래 앱에 실제로 보낸다 */
    private fun record(step: Step, downTime: Long) {
        if (service.store.recordTiming && lastGestureEnd > 0) {
            val gap = downTime - lastGestureEnd
            if (gap >= 150) recorded.add(Step.Wait((gap / 100) * 100))
        }
        recorded.add(step)
        refreshCount()
        replay(step)
    }

    private fun replay(step: Step) {
        replaying = true
        lp.flags = lp.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        try { wm.updateViewLayout(root, lp) } catch (_: Exception) {}
        val duration: Long
        val builder: GestureDescription.Builder.() -> Unit = when (step) {
            is Step.Tap -> { duration = 60; { addStroke(GestureDescription.StrokeDescription(Path().apply { moveTo(step.x.toFloat(), step.y.toFloat()) }, 0, 60)) } }
            is Step.LongPress -> { duration = step.durationMs; { addStroke(GestureDescription.StrokeDescription(Path().apply { moveTo(step.x.toFloat(), step.y.toFloat()) }, 0, step.durationMs)) } }
            is Step.Swipe -> { duration = step.durationMs; { addStroke(GestureDescription.StrokeDescription(Path().apply { moveTo(step.x1.toFloat(), step.y1.toFloat()); lineTo(step.x2.toFloat(), step.y2.toFloat()) }, 0, step.durationMs)) } }
            else -> { duration = 0; {} }
        }
        Thread {
            service.gesture(builder, duration)
            lastGestureEnd = System.currentTimeMillis()
            service.handler.postDelayed({
                lp.flags = lp.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv()
                try { wm.updateViewLayout(root, lp) } catch (_: Exception) {}
                replaying = false
            }, 80)
        }.start()
    }

    fun show() {
        if (shown) return
        try { wm.addView(root, lp); shown = true } catch (e: Exception) { service.toast("오버레이를 띄울 수 없습니다: ${e.message}") }
    }

    fun remove() {
        if (!shown) return
        try { wm.removeView(root) } catch (_: Exception) {}
        shown = false
    }

    /** 누른 자리·드래그 자취를 잠깐 그려 준다 */
    private class MarkView(ctx: android.content.Context) : View(ctx) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xCCFFFFFF.toInt(); style = Paint.Style.STROKE; strokeWidth = 4f }
        private val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x66FF5252 }
        private var sx = -1f; private var sy = -1f; private var cx = -1f; private var cy = -1f
        private var active = false
        fun start(x: Float, y: Float) { sx = x; sy = y; cx = x; cy = y; active = true; invalidate() }
        fun move(x: Float, y: Float) { cx = x; cy = y; invalidate() }
        fun end() { active = false; postDelayed({ if (!active) { sx = -1f; invalidate() } }, 400) }
        override fun onDraw(c: Canvas) {
            if (sx < 0) return
            val loc = IntArray(2); getLocationOnScreen(loc)
            val ox = loc[0].toFloat(); val oy = loc[1].toFloat()
            c.drawCircle(sx - ox, sy - oy, 28f, fill)
            c.drawCircle(sx - ox, sy - oy, 28f, paint)
            if (cx != sx || cy != sy) { c.drawLine(sx - ox, sy - oy, cx - ox, cy - oy, paint); c.drawCircle(cx - ox, cy - oy, 18f, paint) }
        }
    }
}
