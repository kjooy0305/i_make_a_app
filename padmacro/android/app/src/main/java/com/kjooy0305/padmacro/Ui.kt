package com.kjooy0305.padmacro

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.InputType
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView

/** 흑백 프로그램형 뷰 헬퍼 (다른 앱들과 같은 톤) */
object Ui {
    fun Context.dp(v: Int): Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), resources.displayMetrics).toInt()

    fun lp(w: Int = LinearLayout.LayoutParams.MATCH_PARENT, h: Int = LinearLayout.LayoutParams.WRAP_CONTENT, weight: Float = 0f) =
        LinearLayout.LayoutParams(w, h, weight)

    fun Context.title(text: String) = TextView(this).apply {
        this.text = text; textSize = 26f; typeface = Typeface.DEFAULT_BOLD; setTextColor(Color.BLACK)
        setPadding(0, 0, 0, dp(6))
    }

    fun Context.section(text: String) = TextView(this).apply {
        this.text = text; textSize = 17f; typeface = Typeface.DEFAULT_BOLD; setTextColor(Color.BLACK)
        setPadding(0, dp(22), 0, dp(8))
    }

    fun Context.body(text: String) = TextView(this).apply {
        this.text = text; textSize = 14.5f; setTextColor(0xFF222222.toInt()); setLineSpacing(0f, 1.25f)
        setPadding(0, dp(2), 0, dp(6))
    }

    fun Context.small(text: String) = TextView(this).apply {
        this.text = text; textSize = 12.5f; setTextColor(0xFF666666.toInt()); setLineSpacing(0f, 1.2f)
        setPadding(0, dp(2), 0, dp(6))
    }

    fun Context.big(text: String) = TextView(this).apply {
        this.text = text; textSize = 19f; typeface = Typeface.DEFAULT_BOLD; setTextColor(Color.BLACK)
        setPadding(0, dp(4), 0, dp(2))
    }

    /** 검은 바탕 흰 글자 버튼 */
    fun Context.button(text: String, onClick: (View) -> Unit) = Button(this).apply {
        this.text = text; isAllCaps = false; textSize = 15f
        setTextColor(Color.WHITE)
        background = GradientDrawable().apply { setColor(Color.BLACK); cornerRadius = dp(10).toFloat() }
        setPadding(dp(16), dp(12), dp(16), dp(12))
        layoutParams = lp().apply { topMargin = dp(6); bottomMargin = dp(6) }
        stateListAnimator = null
        setOnClickListener(onClick)
    }

    /** 테두리만 있는 버튼 */
    fun Context.outline(text: String, onClick: (View) -> Unit) = Button(this).apply {
        this.text = text; isAllCaps = false; textSize = 14.5f
        setTextColor(Color.BLACK)
        background = GradientDrawable().apply { setColor(Color.WHITE); setStroke(dp(1), Color.BLACK); cornerRadius = dp(10).toFloat() }
        setPadding(dp(14), dp(10), dp(14), dp(10))
        layoutParams = lp().apply { topMargin = dp(4); bottomMargin = dp(4) }
        stateListAnimator = null
        setOnClickListener(onClick)
    }

    /** 작은 글자 버튼(행 안에서 쓰는 용도) */
    fun Context.chip(text: String, onClick: (View) -> Unit) = TextView(this).apply {
        this.text = text; textSize = 14f; setTextColor(Color.BLACK); gravity = Gravity.CENTER
        background = GradientDrawable().apply { setColor(Color.WHITE); setStroke(dp(1), 0xFF888888.toInt()); cornerRadius = dp(8).toFloat() }
        setPadding(dp(10), dp(8), dp(10), dp(8))
        layoutParams = lp(LinearLayout.LayoutParams.WRAP_CONTENT).apply { marginStart = dp(6) }
        setOnClickListener(onClick)
    }

    fun Context.check(text: String, checked: Boolean, onChange: (Boolean) -> Unit) = CheckBox(this).apply {
        this.text = text; textSize = 14.5f; setTextColor(Color.BLACK); isChecked = checked
        setPadding(dp(4), dp(6), 0, dp(6))
        setOnCheckedChangeListener { _, b -> onChange(b) }
    }

    fun Context.row(gravityV: Int = Gravity.CENTER_VERTICAL) = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL; gravity = gravityV
        layoutParams = lp()
    }

    fun Context.column() = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; layoutParams = lp() }

    /** 테두리 카드 */
    fun Context.card() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        background = GradientDrawable().apply { setColor(Color.WHITE); setStroke(dp(1), 0xFFCCCCCC.toInt()); cornerRadius = dp(12).toFloat() }
        setPadding(dp(14), dp(12), dp(14), dp(12))
        layoutParams = lp().apply { topMargin = dp(6); bottomMargin = dp(6) }
    }

    fun Context.divider() = View(this).apply {
        setBackgroundColor(0xFFE0E0E0.toInt())
        layoutParams = lp(h = dp(1)).apply { topMargin = dp(8); bottomMargin = dp(8) }
    }

    fun Context.edit(hint: String, value: String, number: Boolean = false, decimal: Boolean = false) = EditText(this).apply {
        this.hint = hint; setText(value); textSize = 15f; setTextColor(Color.BLACK)
        inputType = when {
            decimal -> InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            number -> InputType.TYPE_CLASS_NUMBER
            else -> InputType.TYPE_CLASS_TEXT
        }
        setSingleLine(true)
        background = GradientDrawable().apply { setColor(Color.WHITE); setStroke(dp(1), 0xFF999999.toInt()); cornerRadius = dp(8).toFloat() }
        setPadding(dp(12), dp(10), dp(12), dp(10))
        layoutParams = lp().apply { topMargin = dp(4); bottomMargin = dp(4) }
    }

    /** 라벨 + 입력칸 한 줄 */
    fun Context.field(label: String, input: EditText): LinearLayout = row().apply {
        addView(TextView(this@field).apply { text = label; textSize = 14f; setTextColor(0xFF444444.toInt()); layoutParams = lp(LinearLayout.LayoutParams.WRAP_CONTENT).apply { marginEnd = dp(10); width = dp(110) } })
        input.layoutParams = lp(0, weight = 1f).apply { topMargin = dp(4); bottomMargin = dp(4) }
        addView(input)
    }

    fun EditText.long(default: Long): Long = text.toString().trim().toLongOrNull() ?: default
    fun EditText.int(default: Int): Int = text.toString().trim().toIntOrNull() ?: default
    /** 초 단위 소수 입력 → ms */
    fun EditText.secondsToMs(defaultMs: Long): Long = text.toString().trim().toDoubleOrNull()?.let { (it * 1000).toLong() } ?: defaultMs
    fun msToSeconds(ms: Long): String = if (ms % 1000 == 0L) (ms / 1000).toString() else (ms / 1000.0).toString()
}
