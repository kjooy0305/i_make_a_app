package com.kjooy0305.dayclean.ui

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import com.kjooy0305.dayclean.logic.Dates
import com.kjooy0305.dayclean.logic.Ops
import com.kjooy0305.dayclean.model.Goal
import com.kjooy0305.dayclean.model.Period
import java.time.LocalDate

/** 장기 목표 목록. 가장 빠른 목표 3개는 스크롤하면 올라가고, 기간·정렬 줄은 맨 위에 남는다. */
class GoalsActivity : BaseActivity() {
    private lateinit var sticky: StickyScroll
    private lateinit var dLabel: TextView
    private var tab: Period? = null
    private var sort = Sort.SOON

    private enum class Sort(val label: String) {
        SOON("빠른 목표순"), EARLY("빠른 추가순"), LATE("느린 목표순"), LATEST("느린 추가순")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sticky = StickyScroll(this)
        dLabel = label("", 12f)
        show(page("장기 목표", true, dLabel, sticky))
    }

    override fun onResume() {
        super.onResume()
        redraw()
    }

    private fun redraw() {
        val s = state; val t = today()
        val head = sticky.head; val body = sticky.body
        head.removeAllViews(); body.removeAllViews()
        dLabel.text = "다음 달 1일까지 ${Dates.dText(Dates.daysToNextFirst(t))}"

        if (!s.settings.goalsOn) {
            head.add(label("장기 목표 정리가 꺼져 있어요. 목표는 쓸 수 있고, 점검만 쉬어요.", 13f).apply {
                gravity = Gravity.CENTER
                background = shape(strokeDp = 1.5f, radiusDp = 10f)
                setPadding(dp(12), dp(10), dp(12), dp(10))
            }, MATCH, WRAP, 0f, 0, 0, 0, 10)
        }
        val top3 = s.goals.sortedBy { it.target }.take(3)
        val box = vbox().apply { background = shape(strokeDp = 3f, radiusDp = 14f); setPadding(dp(14), dp(10), dp(14), dp(10)) }
        box.add(label("가장 빠른 목표 3개", 13f, bold = true), b = 4)
        if (top3.isEmpty()) box.add(label("아직 목표가 없어요. ‘작성’으로 추가해 보세요.", 14f))
        top3.forEachIndexed { i, g ->
            if (i > 0) box.add(hline(1f))
            val row = hbox().apply { minimumHeight = dp(34) }
            row.add(label(Dates.dText(Dates.daysBetween(t, g.target)), 14f, bold = true, mono = true), dp(64), WRAP)
            row.add(label(g.text, 15f), 0, WRAP, 1f)
            box.add(row)
        }
        head.add(box, MATCH, WRAP, 0f, 0, 0, 0, 10)

        sticky.setSticky(stickyBar())

        val list = s.goals.filter { tab == null || it.period == tab }.let { l ->
            when (sort) {
                Sort.SOON -> l.sortedBy { it.target }
                Sort.LATE -> l.sortedByDescending { it.target }
                Sort.EARLY -> l.sortedBy { it.seq }
                Sort.LATEST -> l.sortedByDescending { it.seq }
            }
        }
        list.forEach { g ->
            val row = hbox().apply {
                minimumHeight = dp(48)
                isClickable = true
                background = pressable(shape(fill = C.WHITE, strokeDp = 0f, radiusDp = 0f))
                setOnClickListener { edit(g) }
            }
            row.add(label("%02d.%02d.%02d".format(g.target.year % 100, g.target.monthValue, g.target.dayOfMonth), 12f, mono = true), dp(76), WRAP)
            val tb = hbox()
            tb.add(label(g.text, 15f), WRAP, WRAP, 1f)
            if (!s.settings.typeOn(g.period)) tb.add(label("정리 꺼짐", 11f, bold = true).apply {
                background = shape(strokeDp = 1f, radiusDp = 6f); setPadding(dp(6), dp(1), dp(6), dp(1))
            }, WRAP, WRAP, 0f, 6, 0, 0, 0)
            row.add(tb, 0, WRAP, 1f, 0, 8, 8, 8)
            row.add(label(Dates.dText(Dates.daysBetween(t, g.target)), 13f, bold = true, mono = true))
            body.add(row)
            body.add(hline(1f))
        }
        body.add(label("${list.size}개", 12f).apply { gravity = Gravity.CENTER }, MATCH, WRAP, 0f, 0, 10, 0, 0)
    }

    private fun stickyBar(): View {
        val bar = vbox().apply { setPadding(0, dp(8), 0, dp(8)) }
        val tabs = hbox()
        val tabNames = listOf("전체", "1주", "1달", "1년")
        val cur = when (tab) { null -> 0; Period.W1 -> 1; Period.M1 -> 2; Period.Y1 -> 3 }
        tabs.add(Seg(this, tabNames, cur) { i -> tab = listOf(null, Period.W1, Period.M1, Period.Y1)[i]; redraw() }, 0, WRAP, 4f)
        tabs.add(button("작성") { startActivity(Intent(this, GoalEditActivity::class.java).putExtra(GoalEditActivity.EXTRA_MODE, GoalEditActivity.NEW)) }, 0, WRAP, 1.1f, 6, 0, 0, 0)
        bar.add(tabs)
        val scroll = HorizontalScrollView(this).apply { isHorizontalScrollBarEnabled = false }
        val chips = hbox()
        Sort.values().forEach { o ->
            val on = o == sort
            chips.add(label(o.label, 12f, bold = on).apply {
                gravity = Gravity.CENTER
                minHeight = dp(36)
                setPadding(dp(12), 0, dp(12), 0)
                background = pressable(shape(strokeDp = if (on) 3f else 1f, radiusDp = 18f))
                isClickable = true
                setOnClickListener { sort = o; redraw() }
            }, WRAP, WRAP, 0f, 0, 0, 6, 0)
        }
        scroll.addView(chips)
        bar.add(scroll, MATCH, WRAP, 0f, 0, 6, 0, 0)
        bar.add(hline(2f), MATCH, WRAP, 0f, 0, 8, 0, 0)
        return bar
    }

    private fun edit(g: Goal) {
        startActivity(Intent(this, GoalEditActivity::class.java)
            .putExtra(GoalEditActivity.EXTRA_MODE, GoalEditActivity.EDIT).putExtra(GoalEditActivity.EXTRA_ID, g.id))
    }
}

/** 장기 목표 작성, 수정, 연장. 목표 날짜는 숫자로 입력하거나 달력에서 고른다. */
class GoalEditActivity : BaseActivity() {
    private var mode = NEW
    private var goal: Goal? = null
    private var period = Period.M1
    private var date: LocalDate = LocalDate.now()
    private lateinit var yearField: EditText
    private lateinit var monthField: EditText
    private lateinit var dayField: EditText
    private lateinit var info: TextView
    private var updating = false
    private lateinit var seg: Seg

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mode = intent.getStringExtra(EXTRA_MODE) ?: NEW
        val id = intent.getLongExtra(EXTRA_ID, -1L)
        goal = state.goals.firstOrNull { it.id == id }
        if (mode != NEW && goal == null) { finish(); return }
        val t = today()
        goal?.let { period = it.period; date = if (mode == EXTEND) Dates.goalDate(Period.W1, t) else it.target }
        if (mode == NEW) date = Dates.goalDate(period, t)

        val box = vbox()
        val content: EditText? = if (mode == EXTEND) null else textArea("이루고 싶은 것을 적어요", goal?.text ?: "")

        if (mode == EXTEND) {
            val g = goal!!
            val card = vbox().apply { background = shape(strokeDp = 1.5f, radiusDp = 12f); setPadding(dp(12), dp(10), dp(12), dp(10)) }
            val row = hbox()
            row.add(label(Dates.dText(Dates.daysBetween(t, g.target)), 14f, bold = true, mono = true), dp(60), WRAP)
            row.add(label(g.text, 15f, bold = true), 0, WRAP, 1f)
            card.add(row)
            card.add(label("목표 날짜(${g.target})가 지났어요. 새 날짜를 정하면 이어서 점검해요.", 12f), t = 4)
            box.add(card, MATCH, WRAP, 0f, 0, 8, 0, 0)
            box.add(sectionTitle("새 목표 날짜"))
            val quick = hbox()
            listOf(Period.W1 to "+1주", Period.M1 to "+1달", Period.Y1 to "+1년").forEach { (p, text) ->
                quick.add(button(text, small = true) { setDate(Dates.goalDate(p, t)) }, WRAP, WRAP, 0f, 0, 0, 6, 0)
            }
            box.add(quick, t = 8)
        } else {
            box.add(sectionTitle("주기"))
            seg = Seg(this, Period.values().map { it.label }, period.ordinal) { i ->
                period = Period.values()[i]
                setDate(Dates.goalDate(period, t))
            }
            box.add(seg, t = 8)
            box.add(label("1주 = 내일부터 7일 · 1달 = 같은 날짜(말일이면 다음 달 말일) · 1년 = 같은 날짜 1년 뒤(2월 29일은 2월 28일)", 12f), t = 6)
            box.add(sectionTitle("목표 날짜"))
        }

        yearField = dateField("년", 4); monthField = dateField("월", 2); dayField = dateField("일", 2)
        val dateRow = hbox()
        dateRow.add(labeled("년", yearField), 0, WRAP, 1.4f, 0, 0, 8, 0)
        dateRow.add(labeled("월", monthField), 0, WRAP, 1f, 0, 0, 8, 0)
        dateRow.add(labeled("일", dayField), 0, WRAP, 1f)
        box.add(dateRow, t = 8)
        info = label("", 18f, bold = true)
        box.add(info, t = 8)
        box.add(button("달력에서 고르기") {
            Dialogs.calendar(this, currentDate() ?: date, t) { setDate(it) }
        }, MATCH, WRAP, 0f, 0, 10, 0, 0)
        if (content != null) {
            box.add(sectionTitle("내용"))
            box.add(content, MATCH, WRAP, 0f, 0, 8, 0, 0)
        }
        if (mode == EDIT) {
            box.add(button("이 목표 삭제") {
                Dialogs.confirm(this, "이 목표를 지울까요?", "삭제") { mutate { Ops.removeGoal(it, goal!!.id) }; finish() }
            }, MATCH, WRAP, 0f, 0, 16, 0, 0)
        }

        val bottom = hbox()
        bottom.add(button("취소") { finish() }, 0, WRAP, 1f, 0, 0, 8, 0)
        bottom.add(button(if (mode == EXTEND) "연장하기" else "저장", primary = true) { save(content) }, 0, WRAP, 1f)
        val title = when (mode) { EXTEND -> "목표 연장"; EDIT -> "장기 목표 수정"; else -> "장기 목표 작성" }
        show(page(title, true, null, scrollOf(box), bottom))
        setDate(date)
    }

    private fun dateField(hint: String, max: Int): EditText = textInput(hint, "", numeric = true, maxLen = max).apply {
        gravity = Gravity.CENTER
        setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 22f)
        typeface = android.graphics.Typeface.MONOSPACE
        addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) { if (!updating) showInfo() }
        })
    }

    private fun labeled(name: String, field: EditText): View = vbox().apply {
        add(label(name, 12f), b = 2)
        add(field)
    }

    private fun setDate(d: LocalDate) {
        date = d
        updating = true
        yearField.setText(d.year.toString()); monthField.setText(d.monthValue.toString()); dayField.setText(d.dayOfMonth.toString())
        updating = false
        showInfo()
    }

    private fun currentDate(): LocalDate? {
        val y = yearField.text.toString().toIntOrNull() ?: return null
        val m = monthField.text.toString().toIntOrNull() ?: return null
        val d = dayField.text.toString().toIntOrNull() ?: return null
        return runCatching { LocalDate.of(y, m, d) }.getOrNull()
    }

    private fun showInfo() {
        val d = currentDate()
        info.text = if (d == null) "없는 날짜예요. 년 · 월 · 일을 확인해 주세요."
        else "%04d.%02d.%02d · %s".format(d.year, d.monthValue, d.dayOfMonth, Dates.dText(Dates.daysBetween(today(), d)))
    }

    private fun save(content: EditText?) {
        val d = currentDate()
        if (d == null) { info.text = "없는 날짜예요. 년 · 월 · 일을 확인해 주세요."; return }
        if (d.isBefore(today())) { info.text = "지난 날짜는 고를 수 없어요."; return }
        when (mode) {
            EXTEND -> mutate { Ops.extendGoal(it, goal!!.id, d, today()) }
            else -> {
                val text = content!!.text.toString().trim()
                if (text.isEmpty()) { content.error = "내용을 적어 주세요"; return }
                if (mode == EDIT) mutate { Ops.updateGoal(it, goal!!.id, text, period, d) }
                else mutate { Ops.addGoal(it, text, period, d, today()) }
            }
        }
        finish()
    }

    companion object {
        const val EXTRA_MODE = "mode"
        const val EXTRA_ID = "goalId"
        const val NEW = "new"
        const val EDIT = "edit"
        const val EXTEND = "extend"
    }
}
