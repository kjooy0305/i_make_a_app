package com.kjooy0305.dayclean.ui

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.text.InputType
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import kotlin.math.max

/** 앱의 색은 흰색, 검은색, 그리고 넘긴 기록의 연노랑뿐이다. */
object C {
    const val BLACK = 0xFF000000.toInt()
    const val WHITE = 0xFFFFFFFF.toInt()
    const val YELLOW = 0xFFFFF3A8.toInt()
}

const val MATCH = ViewGroup.LayoutParams.MATCH_PARENT
const val WRAP = ViewGroup.LayoutParams.WRAP_CONTENT

/** add()에서 너비를 따로 정하지 않았다는 표시. 세로 줄에서는 가득, 가로 줄에서는 내용만큼. */
const val AUTO = -3

fun Context.dp(v: Int): Int = (v * resources.displayMetrics.density + 0.5f).toInt()
fun Context.dpf(v: Float): Float = v * resources.displayMetrics.density

fun Context.shape(fill: Int = C.WHITE, stroke: Int = C.BLACK, strokeDp: Float = 1.5f, radiusDp: Float = 12f): GradientDrawable =
    GradientDrawable().apply {
        setColor(fill)
        if (strokeDp > 0f) setStroke(dpf(strokeDp).toInt().coerceAtLeast(1), stroke)
        cornerRadius = dpf(radiusDp)
    }

/** 누르면 잠깐 검게 번지는 효과가 붙은 배경. */
fun Context.pressable(base: GradientDrawable): RippleDrawable =
    RippleDrawable(ColorStateList.valueOf(0x33000000), base, null)

fun Context.label(text: CharSequence, sp: Float = 14f, bold: Boolean = false, mono: Boolean = false): TextView =
    TextView(this).apply {
        this.text = text
        setTextColor(C.BLACK)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, sp)
        typeface = when {
            mono && bold -> Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            mono -> Typeface.MONOSPACE
            bold -> Typeface.DEFAULT_BOLD
            else -> Typeface.DEFAULT
        }
        includeFontPadding = false
        setLineSpacing(0f, 1.15f)
    }

fun Context.button(text: CharSequence, primary: Boolean = false, small: Boolean = false, onClick: () -> Unit): TextView =
    TextView(this).apply {
        this.text = text
        setTextColor(C.BLACK)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, if (small) 13f else 15f)
        typeface = Typeface.DEFAULT_BOLD
        gravity = Gravity.CENTER
        minHeight = dp(if (small) 36 else 44)
        minimumWidth = dp(44)
        setPadding(dp(if (small) 10 else 14), 0, dp(if (small) 10 else 14), 0)
        background = pressable(shape(strokeDp = if (primary) 3f else 1.5f))
        isClickable = true
        isFocusable = true
        contentDescription = text
        setOnClickListener { onClick() }
    }

/** 가는 가로선. 보통의 View는 wrap_content일 때 남는 공간을 다 차지하므로 높이를 직접 정한다. */
class Line(context: Context, private val thickness: Int) : View(context) {
    init { setBackgroundColor(C.BLACK) }
    override fun onMeasure(w: Int, h: Int) = setMeasuredDimension(getDefaultSize(0, w), thickness)
}

fun Context.hline(thicknessDp: Float = 1f): View = Line(this, max(1, dpf(thicknessDp).toInt()))

fun Context.vbox(): LinearLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
fun Context.hbox(): LinearLayout = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }

fun LinearLayout.add(
    v: View, w: Int = AUTO, h: Int = WRAP, weight: Float = 0f,
    l: Int = 0, t: Int = 0, r: Int = 0, b: Int = 0
): View {
    val c = context
    val width = if (w != AUTO) w else if (orientation == LinearLayout.HORIZONTAL) WRAP else MATCH
    addView(v, LinearLayout.LayoutParams(width, h, weight).apply { setMargins(c.dp(l), c.dp(t), c.dp(r), c.dp(b)) })
    return v
}

fun View.pad(l: Int = 0, t: Int = 0, r: Int = 0, b: Int = 0): View {
    setPadding(context.dp(l), context.dp(t), context.dp(r), context.dp(b)); return this
}

/** 구분선이 있는 구역 제목. 오른쪽에 짧은 설명을 붙일 수 있다. */
fun Context.groupHeader(left: String, right: String? = null): View = vbox().apply {
    val row = hbox()
    row.add(label(left, 12f, bold = true), WRAP, WRAP, 1f)
    if (right != null) row.add(label(right, 12f), WRAP, WRAP)
    add(row, t = 12, b = 3)
    add(hline(2f))
}

fun Context.sectionTitle(text: String): View = vbox().apply {
    add(label(text, 15f, bold = true), t = 14, b = 3)
    add(hline(2f))
}

fun Context.textInput(hint: String, value: String = "", numeric: Boolean = false, maxLen: Int = 0): EditText = EditText(this).apply {
    setText(value)
    this.hint = hint
    setHintTextColor(0x99000000.toInt())
    setTextColor(C.BLACK)
    setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
    setSingleLine(true)
    inputType = if (numeric) InputType.TYPE_CLASS_NUMBER else InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
    background = shape(strokeDp = 1.5f, radiusDp = 10f)
    minHeight = dp(44)
    setPadding(dp(12), 0, dp(12), 0)
    if (maxLen > 0) filters = arrayOf(android.text.InputFilter.LengthFilter(maxLen))
}

/** 여러 줄 입력칸. */
fun Context.textArea(hint: String, value: String = ""): EditText = textInput(hint, value).apply {
    setSingleLine(false)
    minLines = 3
    gravity = Gravity.TOP
    setPadding(dp(12), dp(10), dp(12), dp(10))
    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
}

/** 한 번에 하나만 고르는 줄. 고른 칸은 굵은 테두리로 보인다. */
class Seg(context: Context, private val labels: List<String>, selected: Int, private val onSelect: (Int) -> Unit) : LinearLayout(context) {
    var selected: Int = selected
        private set
    private val cells = ArrayList<TextView>()

    init {
        orientation = HORIZONTAL
        labels.forEachIndexed { i, text ->
            val cell = TextView(context).apply {
                this.text = text
                setTextColor(C.BLACK)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                gravity = Gravity.CENTER
                minHeight = context.dp(42)
                isClickable = true
                isFocusable = true
                contentDescription = text
                setOnClickListener { select(i, true) }
            }
            cells.add(cell)
            addView(cell, LayoutParams(0, WRAP, 1f).apply { if (i > 0) leftMargin = context.dp(6) })
        }
        paint()
    }

    fun select(i: Int, notify: Boolean) {
        selected = i
        paint()
        if (notify) onSelect(i)
    }

    private fun paint() {
        cells.forEachIndexed { i, c ->
            val on = i == selected
            c.background = context.pressable(context.shape(strokeDp = if (on) 3f else 1f, radiusDp = 10f))
            c.typeface = if (on) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            c.isSelected = on
        }
    }
}

/** 완료 표시용 동그라미. 켜면 안에 체크가 그려진다. */
class CheckCircle(context: Context) : View(context) {
    var checked: Boolean = false
        set(v) { field = v; contentDescription = if (v) "해결 취소" else "완료"; invalidate() }
    private val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; color = C.BLACK; strokeWidth = context.dpf(2f) }
    private val mark = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; color = C.BLACK; strokeWidth = context.dpf(2.6f); strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND }

    init {
        isClickable = true
        isFocusable = true
        contentDescription = "완료"
    }

    override fun onMeasure(w: Int, h: Int) {
        val s = context.dp(44)   // 누르기 쉬운 크기
        setMeasuredDimension(s, s)
    }

    override fun onDraw(c: Canvas) {
        val cx = width / 2f; val cy = height / 2f; val r = context.dpf(13f)
        c.drawCircle(cx, cy, r, ring)
        if (checked) {
            val p = android.graphics.Path().apply {
                moveTo(cx - r * 0.45f, cy + r * 0.02f); lineTo(cx - r * 0.1f, cy + r * 0.4f); lineTo(cx + r * 0.5f, cy - r * 0.35f)
            }
            c.drawPath(p, mark)
        }
    }
}

/** 켜고 끄는 스위치. 흑백이라 켜짐은 굵은 테두리와 채워진 동그라미로 나타낸다. */
class BwSwitch(context: Context, checked: Boolean, private val onChange: (Boolean) -> Unit) : View(context) {
    var checked: Boolean = checked
        set(v) { field = v; invalidate() }
    private val track = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; color = C.BLACK }
    private val knobFill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val knobRing = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; color = C.BLACK; strokeWidth = context.dpf(2f) }

    init {
        isClickable = true
        isFocusable = true
        setOnClickListener { if (isEnabled) { this.checked = !this.checked; onChange(this.checked) } }
    }

    override fun onMeasure(w: Int, h: Int) = setMeasuredDimension(context.dp(58), context.dp(44))

    override fun onDraw(c: Canvas) {
        val trackH = context.dpf(32f); val trackW = context.dpf(56f)
        val left = (width - trackW) / 2f; val top = (height - trackH) / 2f
        track.strokeWidth = context.dpf(if (checked) 4f else 2f)
        track.pathEffect = if (isEnabled) null else android.graphics.DashPathEffect(floatArrayOf(10f, 8f), 0f)
        c.drawRoundRect(left, top, left + trackW, top + trackH, trackH / 2, trackH / 2, track)
        val r = context.dpf(11f)
        val cx = if (checked) left + trackW - trackH / 2f else left + trackH / 2f
        knobFill.color = if (checked) C.BLACK else C.WHITE
        c.drawCircle(cx, top + trackH / 2f, r, knobFill)
        c.drawCircle(cx, top + trackH / 2f, r, knobRing)
    }
}

/**
 * 위쪽 부분은 함께 스크롤되어 올라가고, sticky 부분은 올라가다 맨 위에 멈추는 화면.
 * 장기 목표의 기간·정렬 줄과 메모리의 검색 줄이 이렇게 동작한다.
 */
class StickyScroll(context: Context) : FrameLayout(context) {
    val scroll = ScrollView(context).apply { isFillViewport = false; isVerticalScrollBarEnabled = true }
    private val column = context.vbox()
    val head = context.vbox()
    private val spacer = View(context)
    val body = context.vbox()
    private var sticky: View? = null

    init {
        column.add(head)
        column.add(spacer, MATCH, 0)
        column.add(body)
        column.setPadding(context.dp(16), context.dp(8), context.dp(16), context.dp(24))
        scroll.addView(column, ViewGroup.LayoutParams(MATCH, WRAP))
        addView(scroll, LayoutParams(MATCH, MATCH))
        scroll.setOnScrollChangeListener { _, _, y, _, _ -> follow(y) }
    }

    fun setSticky(v: View) {
        sticky?.let { removeView(it) }
        sticky = v
        v.setBackgroundColor(C.WHITE)
        addView(v, LayoutParams(MATCH, WRAP).apply { leftMargin = context.dp(16); rightMargin = context.dp(16) })
        v.addOnLayoutChangeListener { _, _, top, _, bottom, _, oldTop, _, oldBottom ->
            if (bottom - top != oldBottom - oldTop) {
                spacer.layoutParams = (spacer.layoutParams as LinearLayout.LayoutParams).apply { height = bottom - top }
                spacer.requestLayout()
            }
        }
        post { follow(scroll.scrollY) }
    }

    private fun follow(y: Int) {
        val s = sticky ?: return
        s.translationY = max(0, spacer.top - y).toFloat()
        s.bringToFront()
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        follow(scroll.scrollY)
    }
}

fun Context.pageBackground(): ColorDrawable = ColorDrawable(C.WHITE)
