package com.kjooy0305.dayclean.ui

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.kjooy0305.dayclean.logic.Dates
import com.kjooy0305.dayclean.model.CleanupTime
import com.kjooy0305.dayclean.model.Kind
import com.kjooy0305.dayclean.model.Settings
import java.time.DayOfWeek
import java.time.LocalDate

/** 시간 입력칸: 시와 분을 숫자로 직접 적는다. */
class TimeField(context: Context, minute: Int) : LinearLayout(context) {
    private val hour = context.textInput("시", "%02d".format(minute / 60), numeric = true, maxLen = 2)
    private val min = context.textInput("분", "%02d".format(minute % 60), numeric = true, maxLen = 2)

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        for (e in listOf(hour, min)) { e.gravity = Gravity.CENTER; e.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 22f); e.typeface = android.graphics.Typeface.MONOSPACE }
        add(hour, 0, WRAP, 1f)
        add(context.label(":", 22f, bold = true), WRAP, WRAP, 0f, 8, 0, 8, 0)
        add(min, 0, WRAP, 1f)
    }

    /** 0~1439, 틀린 값이면 null. */
    fun minute(): Int? {
        val h = hour.text.toString().toIntOrNull() ?: return null
        val m = min.text.toString().toIntOrNull() ?: return null
        return if (h in 0..23 && m in 0..59) h * 60 + m else null
    }
}

object Dialogs {

    private fun base(context: Context, gravity: Int): Dialog = Dialog(context).apply {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setGravity(gravity)
            setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
            setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        }
    }

    /** 아래에서 올라오는 시트. */
    fun sheet(context: Context, content: LinearLayout, scrollable: Boolean = true): Dialog {
        val d = base(context, Gravity.BOTTOM)
        content.background = context.shape(radiusDp = 22f, strokeDp = 3f)
        content.setPadding(context.dp(16), context.dp(12), context.dp(16), context.dp(20))
        val body: View = if (scrollable) android.widget.ScrollView(context).also { it.addView(content, MATCH, WRAP) } else content
        d.setContentView(body)
        return d
    }

    /** 가운데에 뜨는 대화상자. 확인 버튼이 true를 돌려주면 닫힌다. */
    fun modal(
        context: Context, title: String, content: View, positive: String = "확인", negative: String? = "취소",
        onPositive: (Dialog) -> Boolean
    ): Dialog {
        val d = base(context, Gravity.CENTER)
        val box = context.vbox().apply {
            background = context.shape(radiusDp = 18f, strokeDp = 3f)
            setPadding(context.dp(18), context.dp(16), context.dp(18), context.dp(14))
        }
        box.add(context.label(title, 17f, bold = true), b = 10)
        box.add(content)
        val row = context.hbox()
        row.add(View(context), 0, 1, 1f)
        if (negative != null) row.add(context.button(negative) { d.dismiss() }, WRAP, WRAP, 0f, 0, 0, 8, 0)
        row.add(context.button(positive, primary = true) { if (onPositive(d)) d.dismiss() })
        box.add(row, t = 14)
        val holder = FrameLayout(context).apply {
            setPadding(context.dp(20), context.dp(20), context.dp(20), context.dp(20))
            addView(android.widget.ScrollView(context).also { it.addView(box, MATCH, WRAP) }, MATCH, WRAP)
        }
        d.setContentView(holder)
        return d
    }

    fun message(context: Context, text: String, onClose: (() -> Unit)? = null) {
        val d = modal(context, "알림", context.label(text, 15f), "확인", null) { onClose?.invoke(); true }
        d.show()
    }

    fun confirm(context: Context, text: String, action: String, onYes: () -> Unit) {
        modal(context, "확인", context.label(text, 15f), action, "취소") { onYes(); true }.show()
    }

    /** 목록에서 하나 고르기. */
    fun pick(context: Context, title: String, options: List<String>, selected: Int, onPick: (Int) -> Unit) {
        val list = context.vbox()
        lateinit var d: Dialog
        options.forEachIndexed { i, text ->
            val row = context.hbox().apply {
                background = context.pressable(context.shape(strokeDp = if (i == selected) 3f else 1f, radiusDp = 10f))
                minimumHeight = context.dp(48)
                setPadding(context.dp(14), 0, context.dp(14), 0)
                isClickable = true
                setOnClickListener { d.dismiss(); onPick(i) }
            }
            row.add(context.label(text, 15f, bold = i == selected), WRAP, WRAP, 1f)
            if (i == selected) row.add(context.label("✓", 16f, bold = true))
            list.add(row, t = if (i == 0) 0 else 6)
        }
        d = modal(context, title, list, "닫기", null) { true }
        d.show()
    }

    fun cleanupLabel(c: CleanupTime) = "${c.name} · ${Dates.hhmm(c.minuteOfDay)} 부터"

    /** 기록의 유형·내용·알림 시작 시간을 고치는 시트. */
    fun editItem(
        context: Context, text: String, kind: Kind, startId: Long?, settings: Settings,
        onApply: (Kind, String, Long?) -> Unit, onDelete: () -> Unit
    ) {
        val box = context.vbox()
        var chosenKind = kind
        var chosenStart: Long? = startId ?: settings.defaultLaterStartId
        val input: EditText = context.textInput("내용", text)
        val startBox = context.vbox()
        lateinit var dialog: Dialog

        fun rebuildStart() {
            startBox.removeAllViews()
            if (chosenKind != Kind.LATER) return
            startBox.add(context.label("‘-’ 알림이 시작될 정리 시간", 12f), t = 6, b = 4)
            settings.cleanups.sortedBy { it.minuteOfDay }.forEach { c ->
                val on = c.id == chosenStart
                val row = context.hbox().apply {
                    background = context.pressable(context.shape(strokeDp = if (on) 3f else 1f, radiusDp = 10f))
                    minimumHeight = context.dp(44)
                    setPadding(context.dp(14), 0, context.dp(14), 0)
                    isClickable = true
                    setOnClickListener { chosenStart = c.id; rebuildStart() }
                }
                row.add(context.label(cleanupLabel(c), 14f, bold = on), WRAP, WRAP, 1f)
                if (on) row.add(context.label("✓", 15f, bold = true))
                startBox.add(row, t = 6)
            }
        }

        box.add(context.label("유형과 내용 수정", 16f, bold = true), b = 10)
        box.add(input)
        val kinds = listOf(Kind.NOW, Kind.LATER, Kind.MEMO)
        box.add(Seg(context, listOf("• 지금", "- 나중에", "= 메모"), kinds.indexOf(kind)) { chosenKind = kinds[it]; rebuildStart() }, t = 10)
        box.add(startBox)
        val actions = context.hbox()
        actions.add(context.button("삭제") {
            confirm(context, "이 기록을 지울까요?", "삭제") { dialog.dismiss(); onDelete() }
        }, WRAP, WRAP, 0f, 0, 0, 8, 0)
        actions.add(View(context), 0, 1, 1f)
        actions.add(context.button("취소") { dialog.dismiss() }, WRAP, WRAP, 0f, 0, 0, 8, 0)
        actions.add(context.button("적용", primary = true) {
            val t = input.text.toString().trim()
            if (t.isEmpty()) { input.error = "내용을 적어 주세요"; return@button }
            dialog.dismiss()
            onApply(chosenKind, t, if (chosenKind == Kind.LATER) chosenStart else null)
        })
        box.add(actions, t = 14)
        rebuildStart()
        dialog = sheet(context, box)
        dialog.show()
    }

    /** 요일을 켜고 끄는 줄과 빠른 선택(매일, 평일, 주말). 고른 요일은 selected()로 읽는다. */
    class DayPicker(context: Context, initial: Set<DayOfWeek>) : LinearLayout(context) {
        private val chosen = initial.toMutableSet()
        private val cells = LinkedHashMap<DayOfWeek, TextView>()

        init {
            orientation = VERTICAL
            val quick = context.hbox()
            fun q(label: String, days: Set<DayOfWeek>) =
                quick.add(context.button(label, small = true) { chosen.clear(); chosen.addAll(days); paint() }, WRAP, WRAP, 0f, 0, 0, 6, 0)
            q("매일", DayOfWeek.values().toSet())
            q("평일", setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY))
            q("주말", setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY))
            add(quick, b = 8)
            val row = context.hbox()
            DayOfWeek.values().forEachIndexed { i, d ->
                val cell = TextView(context).apply {
                    text = Dates.dow(d)
                    setTextColor(C.BLACK)
                    gravity = Gravity.CENTER
                    isClickable = true
                    contentDescription = Dates.dow(d) + "요일"
                    setOnClickListener { if (!chosen.add(d)) chosen.remove(d); paint() }
                }
                cells[d] = cell
                row.add(cell, 0, context.dp(40), 1f, if (i == 0) 0 else 4, 0, 0, 0)
            }
            add(row)
            paint()
        }

        private fun paint() {
            cells.forEach { (d, c) ->
                val on = d in chosen
                c.background = context.pressable(context.shape(strokeDp = if (on) 3f else 1f, radiusDp = 20f))
                c.typeface = if (on) android.graphics.Typeface.DEFAULT_BOLD else android.graphics.Typeface.DEFAULT
            }
        }

        fun selected(): Set<DayOfWeek> = chosen.toSet()
    }

    /** 날짜를 달력에서 고른다. 오늘 이전은 고를 수 없다. */
    fun calendar(context: Context, initial: LocalDate, today: LocalDate, onPick: (LocalDate) -> Unit) {
        var month = java.time.YearMonth.from(initial)
        var picked = initial
        val box = context.vbox()
        val head = context.hbox()
        val title = context.label("", 18f, bold = true).apply { gravity = Gravity.CENTER }
        val grid = context.vbox()
        val footer = context.label("", 16f, bold = true)
        lateinit var d: Dialog

        fun draw() {
            title.text = "${month.year}년 ${month.monthValue}월"
            footer.text = "${picked.year}.%02d.%02d · ${Dates.dText(Dates.daysBetween(today, picked))}".format(picked.monthValue, picked.dayOfMonth)
            grid.removeAllViews()
            val names = listOf("일", "월", "화", "수", "목", "금", "토")
            val wd = context.hbox()
            names.forEach { wd.add(context.label(it, 12f, bold = true).apply { gravity = Gravity.CENTER }, 0, WRAP, 1f) }
            grid.add(wd, b = 4)
            grid.add(context.hline(2f), b = 4)
            val first = month.atDay(1).dayOfWeek.value % 7   // 일요일 = 0
            val total = month.lengthOfMonth()
            var week = 0
            while (week * 7 - first + 1 <= total) {
                val row = context.hbox()
                for (col in 0..6) {
                    val num = week * 7 + col - first + 1
                    if (num < 1 || num > total) {
                        row.add(View(context), 0, context.dp(44), 1f)
                        continue
                    }
                    val date = month.atDay(num)
                    val past = date.isBefore(today)
                    val on = date == picked
                    val cell = TextView(context).apply {
                        text = num.toString()
                        setTextColor(C.BLACK)
                        gravity = Gravity.CENTER
                        typeface = if (on) android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD) else android.graphics.Typeface.MONOSPACE
                        paintFlags = if (past) paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG else paintFlags
                        background = context.shape(strokeDp = if (on) 4f else if (date == today) 2f else if (past) 0f else 1f, radiusDp = 10f)
                        isClickable = !past
                        contentDescription = "${date.monthValue}월 ${date.dayOfMonth}일"
                        if (!past) setOnClickListener { picked = date; draw() }
                    }
                    row.add(cell, 0, context.dp(44), 1f, 2, 2, 2, 2)
                }
                grid.add(row)
                week++
            }
        }

        head.add(context.button("◀") { month = month.minusMonths(1); draw() }.apply { contentDescription = "이전 달" })
        head.add(title, 0, WRAP, 1f)
        head.add(context.button("▶") { month = month.plusMonths(1); draw() }.apply { contentDescription = "다음 달" })
        box.add(head)
        box.add(grid, t = 8)
        box.add(context.label("점선 칸은 오늘이에요. 지난 날짜는 고를 수 없어요.", 12f), t = 8)
        box.add(footer, t = 8)
        d = modal(context, "날짜 고르기", box, "이 날짜로 정하기", "취소") { onPick(picked); true }
        draw()
        d.show()
    }
}
