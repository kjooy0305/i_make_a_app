package com.kjooy0305.dayclean.ui

import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import com.kjooy0305.dayclean.logic.Dates
import com.kjooy0305.dayclean.logic.DoneOrder
import com.kjooy0305.dayclean.logic.MemorySearch
import com.kjooy0305.dayclean.logic.MemoryText
import com.kjooy0305.dayclean.platform.AlarmScheduler

/** 메모리: 지난 기록을 날짜별로 보고, 검색하고, txt로 내보내거나 불러온다. */
class MemoryActivity : BaseActivity() {
    private lateinit var sticky: StickyScroll
    private lateinit var search: EditText
    private lateinit var count: TextView
    private var newestFirst = true
    private var order = DoneOrder.WRITTEN
    private var pendingText: String = ""
    private var importToday = true

    private val createFile = registerForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
        if (uri == null) return@registerForActivityResult
        val ok = runCatching { contentResolver.openOutputStream(uri)!!.use { it.write(pendingText.toByteArray(Charsets.UTF_8)) } }.isSuccess
        Dialogs.message(this, if (ok) "내보냈어요." else "내보내지 못했어요. 저장 위치를 확인해 주세요.")
    }

    private val openFile = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@registerForActivityResult
        val text = runCatching { contentResolver.openInputStream(uri)!!.use { it.readBytes().toString(Charsets.UTF_8) } }.getOrNull()
        if (text == null) { Dialogs.message(this, "파일을 읽지 못했어요."); return@registerForActivityResult }
        if (!text.contains(MemoryText.HEADER) && !text.contains("[")) { Dialogs.message(this, "하루 정리에서 내보낸 txt 파일이 아니에요."); return@registerForActivityResult }
        val now = store.now()
        var result: MemoryText.ImportResult? = null
        mutate { s ->
            val r = if (importToday) MemoryText.importToday(s, text, Dates.dateOf(now), now) else MemoryText.importAll(s, text, Dates.dateOf(now), now)
            result = r; r.state
        }
        AlarmScheduler.schedule(applicationContext)
        val r = result!!
        Dialogs.message(this, "${r.entries}건을 불러왔어요." + if (r.skipped > 0) " (이미 있거나 넣을 수 없는 ${r.skipped}건은 건너뛰었어요)" else "")
        redraw()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sticky = StickyScroll(this)

        val buttons = hbox()
        fun b(text: String, onClick: () -> Unit, last: Boolean = false) {
            buttons.add(button(text, small = true) { onClick() }.apply { minHeight = dp(52); setLineSpacing(0f, 1.1f); setPadding(dp(2), 0, dp(2), 0) },
                0, WRAP, 1f, 0, 0, if (last) 0 else 6, 0)
        }
        b("오늘\n불러오기", { importToday = true; openFile.launch(arrayOf("text/plain", "*/*")) })
        b("전체\n불러오기", { importToday = false; openFile.launch(arrayOf("text/plain", "*/*")) })
        b("오늘\n내보내기", { export(true) })
        b("전체\n내보내기", { export(false) }, last = true)
        sticky.head.add(buttons, MATCH, WRAP, 0f, 0, 0, 0, 10)
        sticky.head.add(label("오늘 기록은 00:00에 이 목록으로 옮겨져요. 그 전에는 기록장에서만 볼 수 있어요.", 12f).apply {
            background = shape(strokeDp = 1.5f, radiusDp = 10f)
            setPadding(dp(12), dp(8), dp(12), dp(8))
        }, MATCH, WRAP, 0f, 0, 0, 0, 10)

        sticky.setSticky(stickyBar())
        show(page("메모리", true, null, sticky))
        redraw()
    }

    private fun stickyBar(): View {
        val bar = vbox().apply { setPadding(0, dp(6), 0, dp(6)) }
        val row = hbox()
        search = textInput("날짜나 내용으로 검색 (예: 10.24, 빨래)").apply {
            inputType = android.text.InputType.TYPE_CLASS_TEXT
            imeOptions = android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
                override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
                override fun afterTextChanged(s: Editable?) { redraw() }
            })
        }
        row.add(search, 0, WRAP, 1f, 0, 0, 8, 0)
        row.add(button("✕") { search.setText("") }.apply { contentDescription = "검색 지우기" })
        bar.add(row)
        val scroll = HorizontalScrollView(this).apply { isHorizontalScrollBarEnabled = false }
        val chips = hbox()
        fun chip(text: String, on: Boolean, f: () -> Unit) {
            chips.add(label(text, 12f, bold = on).apply {
                gravity = Gravity.CENTER; minHeight = dp(36); setPadding(dp(12), 0, dp(12), 0)
                background = pressable(shape(strokeDp = if (on) 3f else 1f, radiusDp = 18f))
                isClickable = true; setOnClickListener { f() }
            }, WRAP, WRAP, 0f, 0, 0, 6, 0)
        }
        chip("최근 날짜 먼저", newestFirst) { newestFirst = true; stickyRefresh() }
        chip("오래된 날짜 먼저", !newestFirst) { newestFirst = false; stickyRefresh() }
        chip("해결 목록: 작성 시간순", order == DoneOrder.WRITTEN) { order = DoneOrder.WRITTEN; stickyRefresh() }
        chip("해결 목록: 해결 시간순", order == DoneOrder.RESOLVED) { order = DoneOrder.RESOLVED; stickyRefresh() }
        scroll.addView(chips)
        bar.add(scroll, MATCH, WRAP, 0f, 0, 6, 0, 0)
        count = label("", 12f, bold = true)
        bar.add(count, MATCH, WRAP, 0f, 0, 8, 0, 0)
        bar.add(hline(2f), MATCH, WRAP, 0f, 0, 6, 0, 0)
        return bar
    }

    private fun stickyRefresh() {
        val text = search.text.toString()
        sticky.setSticky(stickyBar())
        search.setText(text)
        search.setSelection(text.length)
        redraw()
    }

    private fun export(todayOnly: Boolean) {
        val now = store.now()
        val day = Dates.dateOf(now)
        pendingText = if (todayOnly) MemoryText.exportToday(state, day) else MemoryText.exportAll(state, day)
        val stamp = "%04d%02d%02d".format(day.year, day.monthValue, day.dayOfMonth)
        createFile.launch(if (todayOnly) "하루정리_오늘_$stamp.txt" else "하루정리_전체_$stamp.txt")
    }

    private fun redraw() {
        val query = search.text.toString()
        val r = MemorySearch.search(state.archive, query, newestFirst, order)
        count.text = if (query.isBlank()) "전체 ${r.count}건 · ${r.days.size}일" else "검색 결과 ${r.count}건 · ${r.days.size}일"
        val body = sticky.body
        body.removeAllViews()
        if (r.days.isEmpty()) {
            body.add(vbox().apply {
                background = shape(strokeDp = 1.5f, radiusDp = 12f)
                setPadding(dp(12), dp(18), dp(12), dp(18))
                gravity = Gravity.CENTER_HORIZONTAL
                add(label(if (query.isBlank()) "아직 옮겨진 기록이 없어요" else "검색 결과가 없어요", 15f, bold = true))
                add(label(if (query.isBlank()) "기록장의 내용은 매일 00:00에 이곳으로 옮겨져요." else "날짜(10.24, 2026-10-24)나 내용의 일부를 입력해 보세요.", 12f), t = 4)
            }, MATCH, WRAP, 0f, 0, 12, 0, 0)
            return
        }
        r.days.forEach { hit ->
            val day = hit.archive.date
            body.add(highlight("%04d-%02d-%02d (%s)".format(day.year, day.monthValue, day.dayOfMonth, Dates.dow(day.dayOfWeek)), r.tokens, 15f, bold = true), MATCH, WRAP, 0f, 0, 14, 0, 2)
            body.add(hline(2f))
            fun section(title: String, rows: List<Pair<String, String>>) {
                if (rows.isEmpty()) return
                val l1 = Indent(this)
                l1.inner.add(label("ㄴ $title", 14f, bold = true), t = 6)
                rows.forEach { (meta, text) ->
                    val l2 = Indent(this)
                    l2.inner.add(label("ㄴ $meta", 12f, mono = true), t = 4)
                    val l3 = Indent(this)
                    l3.inner.add(highlight("ㄴ $text", r.tokens, 14f), t = 2, b = 2)
                    l2.inner.add(l3, MATCH, WRAP, 0f, 8, 0, 0, 0)
                    l1.inner.add(l2, MATCH, WRAP, 0f, 8, 0, 0, 0)
                }
                body.add(l1, MATCH, WRAP, 0f, 8, 0, 0, 0)
            }
            section("미룬 것", hit.late.map { it.time to it.text })
            section("메모", hit.memos.map { it.time to it.text })
            section("해결한 내용", hit.done.map { "작성 ${it.written} · 해결 ${it.resolved} · ${it.priority}" to it.text })
        }
    }

    private fun highlight(text: String, tokens: List<String>, sp: Float, bold: Boolean = false): TextView {
        val s = SpannableString(text)
        val lower = text.lowercase()
        tokens.forEach { t ->
            var from = 0
            while (t.isNotEmpty()) {
                val i = lower.indexOf(t, from)
                if (i < 0) break
                s.setSpan(UnderlineSpan(), i, i + t.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                s.setSpan(StyleSpan(Typeface.BOLD), i, i + t.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                from = i + t.length
            }
        }
        return label("", sp, bold).apply { setText(s) }
    }
}

/** 한 단계 안으로 들어간 블록: 왼쪽에 세로선을 긋고 안쪽에 내용을 넣는다. */
private class Indent(context: android.content.Context) : LinearLayout(context) {
    val inner: LinearLayout = context.vbox()

    init {
        orientation = HORIZONTAL
        addView(View(context).apply { setBackgroundColor(C.BLACK) }, LayoutParams(context.dp(1), MATCH))
        addView(inner, LayoutParams(0, WRAP, 1f).apply { leftMargin = context.dp(8) })
    }
}
