package com.kjooy0305.dayclean.ui

import android.content.Context
import android.graphics.Paint
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.kjooy0305.dayclean.logic.Dates
import com.kjooy0305.dayclean.model.AppState
import com.kjooy0305.dayclean.model.Item
import com.kjooy0305.dayclean.model.Kind
import java.time.LocalDate

/** 홈과 저녁 정리가 같이 쓰는 기록 한 줄. */
object ItemRows {

    class Row(val view: View, val check: CheckCircle?, val text: TextView, val meta: TextView)

    fun build(
        context: Context, item: Item, state: AppState, today: LocalDate,
        yellow: Boolean, checkable: Boolean,
        onToggle: ((Item, Row) -> Unit)? = null, onLong: ((Item) -> Unit)? = null
    ): Row {
        val row = context.hbox().apply {
            minimumHeight = context.dp(52)
            if (yellow) setBackgroundColor(C.YELLOW)
            isLongClickable = onLong != null
        }
        var check: CheckCircle? = null
        if (checkable && item.kind != Kind.MEMO) {
            check = CheckCircle(context).apply { checked = item.done }
            row.add(check, context.dp(44), context.dp(44))
        } else {
            row.add(View(context), context.dp(8), 1)
        }
        row.add(context.label(item.kind.glyph, 15f, bold = true, mono = true).apply { gravity = android.view.Gravity.CENTER },
            context.dp(18), WRAP, 0f, 2, 0, 6, 0)
        val textBox = context.vbox()
        val text = context.label(item.text, 15f)
        applyDone(text, item.done)
        textBox.add(text)
        if (item.goalId != null) {
            val goal = state.goals.firstOrNull { it.id == item.goalId }
            val tag = if (goal != null) "장기 목표 ${Dates.dText(Dates.daysBetween(today, goal.target))}" else "장기 목표"
            textBox.add(context.label(tag, 11f, bold = true).apply {
                background = context.shape(strokeDp = 1f, radiusDp = 6f)
                setPadding(context.dp(6), context.dp(1), context.dp(6), context.dp(1))
            }, WRAP, WRAP, 0f, 0, 3, 0, 0)
        }
        row.add(textBox, 0, WRAP, 1f, 0, 6, 8, 6)
        val meta = context.label(if (item.done && item.doneAt != null) Dates.hhmm(item.doneAt) else "", 12f, mono = true)
        row.add(meta, WRAP, WRAP, 0f, 0, 0, 8, 0)

        val result = Row(wrapWithLine(context, row), check, text, meta)
        check?.setOnClickListener { onToggle?.invoke(item, result) }
        if (onLong != null) row.setOnLongClickListener { onLong(item); true }
        return result
    }

    private fun wrapWithLine(context: Context, row: View): View = context.vbox().apply {
        add(row)
        add(context.hline(1f))
    }

    fun applyDone(text: TextView, done: Boolean) {
        text.paintFlags = if (done) text.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG else text.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
    }
}
