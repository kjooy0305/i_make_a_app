package com.kjooy0305.dayclean.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import com.kjooy0305.dayclean.logic.Dates
import com.kjooy0305.dayclean.logic.Gate
import com.kjooy0305.dayclean.logic.GoalReview
import com.kjooy0305.dayclean.logic.Ops
import com.kjooy0305.dayclean.model.CleanupTime
import com.kjooy0305.dayclean.model.Disposition
import com.kjooy0305.dayclean.model.Item
import com.kjooy0305.dayclean.model.Kind
import com.kjooy0305.dayclean.model.MORNING_ID
import com.kjooy0305.dayclean.model.Period
import com.kjooy0305.dayclean.platform.Notifier

/** 정리 화면의 공통 부분: 뒤로 가기를 막고, 끝내면 정리 완료로 기록한다. */
abstract class CleanupActivity : BaseActivity() {
    protected lateinit var cleanup: CleanupTime

    protected fun loadCleanup(): Boolean {
        val id = intent.getLongExtra(HomeActivity.EXTRA_CLEANUP_ID, -1L)
        val c = state.settings.cleanup(id)
        if (c == null) { finish(); return false }
        cleanup = c
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { /* 정리를 끝내기 전에는 나갈 수 없다 */ }
        })
        return true
    }

    protected fun complete() {
        mutate { Gate.complete(it, cleanup.id) }
        finish()
    }

    protected fun title(): String = "${cleanup.name} · ${Dates.hhmm(cleanup.minuteOfDay)}"

    /** 유형 · 내용 시트를 열고 적용하면 다시 그린다. */
    protected fun editSheet(item: Item, redraw: () -> Unit) {
        Dialogs.editItem(this, item.text, item.kind, item.startCleanupId, state.settings,
            onApply = { k, text, start -> mutate { Ops.editItem(it, item.id, k, text, start, store.now()) }; redraw() },
            onDelete = { mutate { Ops.deleteItem(it, item.id) }; Notifier.cancelItem(applicationContext, item.id); redraw() })
    }
}

/** 아침 정리: 어제 넘긴 기록 확인, '-' 기록 확인, 오늘 할 일 추가. */
class MorningActivity : CleanupActivity() {
    private lateinit var box: LinearLayout
    private val added = ArrayList<Long>()
    private var addKind = Kind.NOW
    private lateinit var input: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!loadCleanup()) return
        savedInstanceState?.getLongArray("added")?.let { added.addAll(it.toList()) }
        box = vbox()
        val sc = scrollOf(box)
        val hasGoalCheck = cleanup.id == MORNING_ID && GoalReview.dueTypes(state, today()).isNotEmpty()
        val finish = button(if (hasGoalCheck) "아침 정리 완료 ▸" else "${cleanup.name} 끝내기", primary = true) { complete() }
        show(page(title(), false, null, sc, finish))
        redraw()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLongArray("added", added.toLongArray())
    }

    private fun redraw() {
        val s = state; val t = today()
        val items = Ops.today(s, t)
        box.removeAllViews()

        val carry = items.filter { it.carried && it.kind != Kind.MEMO && !it.done }
        if (carry.isNotEmpty()) {
            box.add(groupHeader("어제 넘긴 기록", "유형을 눌러 수정"))
            carry.forEach { i ->
                val row = hbox().apply { setBackgroundColor(C.YELLOW); minimumHeight = dp(52) }
                row.add(label(i.kind.glyph, 15f, bold = true, mono = true).apply { gravity = android.view.Gravity.CENTER }, dp(26), WRAP, 0f, 8, 0, 6, 0)
                row.add(label(i.text, 15f), 0, WRAP, 1f, 0, 6, 8, 6)
                row.add(button("유형 ▾", small = true) { editSheet(i) { redraw() } }, WRAP, WRAP, 0f, 0, 0, 8, 0)
                box.add(row); box.add(hline(1f))
            }
        }
        val later = items.filter { it.kind == Kind.LATER && !it.carried && !it.done }
        if (later.isNotEmpty()) {
            box.add(groupHeader("오늘 ‘-’ 기록", "알림 시작 정리 시간"))
            later.forEach { i ->
                val row = hbox().apply { minimumHeight = dp(52) }
                row.add(label("-", 15f, bold = true, mono = true).apply { gravity = android.view.Gravity.CENTER }, dp(26), WRAP, 0f, 8, 0, 6, 0)
                val textBox = vbox()
                textBox.add(label(i.text, 15f))
                row.add(textBox, 0, WRAP, 1f, 0, 6, 8, 6)
                val c = s.settings.cleanup(i.startCleanupId) ?: s.settings.cleanup(s.settings.defaultLaterStartId)
                row.add(button("${c?.name ?: "-"} ▾", small = true) { chooseStart(i) }, WRAP, WRAP, 0f, 0, 0, 8, 0)
                box.add(row); box.add(hline(1f))
            }
        }

        box.add(groupHeader("오늘 할 일 추가"))
        box.add(Seg(this, listOf("• 지금", "- 나중에", "= 메모"), listOf(Kind.NOW, Kind.LATER, Kind.MEMO).indexOf(addKind)) {
            addKind = listOf(Kind.NOW, Kind.LATER, Kind.MEMO)[it]
        }, t = 8)
        val row = hbox()
        input = textInput("오늘 해야 할 것").apply {
            imeOptions = EditorInfo.IME_ACTION_DONE
            setOnEditorActionListener { _, id, _ -> if (id == EditorInfo.IME_ACTION_DONE) { add(); true } else false }
        }
        row.add(input, 0, WRAP, 1f, 0, 0, 8, 0)
        row.add(button("추가", primary = true) { add() })
        box.add(row, t = 8)

        val mine = added.mapNotNull { id -> items.firstOrNull { it.id == id } }
        if (mine.isNotEmpty()) {
            box.add(groupHeader("방금 추가한 것", "${mine.size}개"))
            mine.forEach { i ->
                val r = hbox().apply { minimumHeight = dp(52) }
                r.add(label(i.kind.glyph, 15f, bold = true, mono = true).apply { gravity = android.view.Gravity.CENTER }, dp(26), WRAP, 0f, 8, 0, 6, 0)
                r.add(label(i.text, 15f), 0, WRAP, 1f, 0, 6, 8, 6)
                r.add(button("삭제", small = true) {
                    mutate { Ops.deleteItem(it, i.id) }; added.remove(i.id); Notifier.cancelItem(applicationContext, i.id); redraw()
                })
                box.add(r); box.add(hline(1f))
            }
        }
    }

    private fun add() {
        val text = input.text.toString().trim()
        if (text.isEmpty()) { input.error = "내용을 적어 주세요"; return }
        val s = mutate { Ops.addItem(it, addKind, text, store.now()) }
        added.add(0, s.items.last().id)
        redraw()
    }

    private fun chooseStart(item: Item) {
        val list = state.settings.cleanups.sortedBy { it.minuteOfDay }
        val cur = list.indexOfFirst { it.id == (item.startCleanupId ?: state.settings.defaultLaterStartId) }
        Dialogs.pick(this, "‘-’ 알림 시작 시간", list.map { Dialogs.cleanupLabel(it) }, cur) { i ->
            mutate { Ops.editItem(it, item.id, item.kind, item.text, list[i].id, store.now()) }
            redraw()
        }
    }
}

/** 저녁 정리: 남은 기록을 체크만 할 수 있다. 메모와 새 기록은 잠긴다. */
class EveningActivity : CleanupActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!loadCleanup()) return
        val box = vbox()
        val s = state; val t = today()
        box.add(label("지금은 체크만 할 수 있어요. 메모와 새 기록은 잠겨 있어요.", 13f).apply {
            gravity = android.view.Gravity.CENTER
            background = shape(strokeDp = 1.5f, radiusDp = 10f)
            setPadding(dp(12), dp(10), dp(12), dp(10))
        }, MATCH, WRAP, 0f, 0, 8, 0, 4)
        val items = Ops.today(s, t).filter { it.kind != Kind.MEMO }
        fun part(kind: Kind, title: String) {
            val list = items.filter { it.kind == kind }.sortedBy { it.done }
            if (list.isEmpty()) return
            box.add(groupHeader(title))
            list.forEach { i ->
                box.add(ItemRows.build(this, i, s, t, yellow = i.carried && !i.done, checkable = true, onToggle = { item, row ->
                    val after = mutate { Ops.toggleDone(it, item.id, store.now()) }.items.first { it.id == item.id }
                    row.check?.checked = after.done
                    ItemRows.applyDone(row.text, after.done)
                    row.meta.text = if (after.done && after.doneAt != null) Dates.hhmm(after.doneAt) else ""
                    if (after.done) Notifier.cancelItem(applicationContext, item.id)
                }).view)
            }
        }
        part(Kind.NOW, "• 지금")
        part(Kind.LATER, "- 나중에")
        if (items.isEmpty()) box.add(label("남은 기록이 없어요.", 14f).apply { gravity = android.view.Gravity.CENTER }, MATCH, WRAP, 0f, 0, 30, 0, 0)
        box.add(label("= 메모는 저녁 정리에 나오지 않아요.", 12f), MATCH, WRAP, 0f, 0, 14, 0, 0)
        val bottom = vbox()
        bottom.add(button("확인 · ${cleanup.name} 끝내기", primary = true) { complete() })
        bottom.add(label("끝내기 전에는 뒤로 갈 수 없어요.", 12f).apply { gravity = android.view.Gravity.CENTER }, MATCH, WRAP, 0f, 0, 6, 0, 0)
        show(page(title(), false, null, scrollOf(box), bottom))
    }
}

/** 회고: 못한 일을 내일로 넘기거나 끝내고, 내일 할 일을 미리 적는다. */
class ReviewActivity : CleanupActivity() {
    private lateinit var box: LinearLayout
    private var addKind = Kind.NOW
    private lateinit var input: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!loadCleanup()) return
        box = vbox()
        show(page(title(), false, null, scrollOf(box), button("${cleanup.name} 끝내기", primary = true) { complete() }))
        redraw()
    }

    private fun redraw() {
        val s = state; val t = today()
        box.removeAllViews()
        val open = Ops.today(s, t).filter { it.kind != Kind.MEMO && !it.done }
        box.add(groupHeader("못한 일", "${open.size}개"))
        if (open.isEmpty()) box.add(label("남은 일이 없어요.", 14f), MATCH, WRAP, 0f, 0, 12, 0, 4)
        open.forEach { i ->
            val card = vbox().apply { background = shape(strokeDp = 1.5f, radiusDp = 12f); setPadding(dp(12), dp(10), dp(12), dp(10)) }
            card.add(label("${i.kind.glyph} ${i.text}", 15f))
            val acts = hbox()
            acts.add(button("내일로 넘기기", small = true) { mutate { Ops.decide(it, i.id, Disposition.CARRY, store.now()) }; redraw() }, 0, WRAP, 1f, 0, 0, 6, 0)
            acts.add(button("끝내기", small = true) { mutate { Ops.decide(it, i.id, Disposition.DROP, store.now()) }; Notifier.cancelItem(applicationContext, i.id); redraw() }, 0, WRAP, 1f)
            card.add(acts, t = 8)
            box.add(card, MATCH, WRAP, 0f, 0, 8, 0, 0)
        }

        box.add(groupHeader("내일 할 일 미리 적기"))
        box.add(Seg(this, listOf("• 지금", "- 나중에", "= 메모"), listOf(Kind.NOW, Kind.LATER, Kind.MEMO).indexOf(addKind)) {
            addKind = listOf(Kind.NOW, Kind.LATER, Kind.MEMO)[it]
        }, t = 8)
        val row = hbox()
        input = textInput("내일 해야 할 것").apply {
            imeOptions = EditorInfo.IME_ACTION_DONE
            setOnEditorActionListener { _, id, _ -> if (id == EditorInfo.IME_ACTION_DONE) { addTomorrow(); true } else false }
        }
        row.add(input, 0, WRAP, 1f, 0, 0, 8, 0)
        row.add(button("추가", primary = true) { addTomorrow() })
        box.add(row, t = 8)

        val tomorrow = Ops.tomorrowList(s, t)
        box.add(groupHeader("내일로 넘어가는 것", "${tomorrow.size}개"))
        tomorrow.forEach { i ->
            val r = hbox().apply { setBackgroundColor(C.YELLOW); minimumHeight = dp(52) }
            r.add(label(i.kind.glyph, 15f, bold = true, mono = true).apply { gravity = android.view.Gravity.CENTER }, dp(26), WRAP, 0f, 8, 0, 6, 0)
            r.add(label(i.text, 15f), 0, WRAP, 1f, 0, 6, 8, 6)
            val prewritten = i.date != t
            r.add(button(if (prewritten) "삭제" else "취소", small = true) {
                if (prewritten) mutate { Ops.deleteItem(it, i.id) } else mutate { Ops.undecide(it, i.id, store.now()) }
                redraw()
            })
            box.add(r); box.add(hline(1f))
        }
    }

    private fun addTomorrow() {
        val text = input.text.toString().trim()
        if (text.isEmpty()) { input.error = "내용을 적어 주세요"; return }
        mutate { Ops.addItem(it, addKind, text, store.now(), date = today().plusDays(1), carried = true) }
        redraw()
    }
}

/** 아침 정리 뒤의 장기 목표 점검. 1주, 1달, 1년 순으로 나온다. */
class GoalCheckActivity : BaseActivity() {
    private lateinit var box: LinearLayout
    private lateinit var steps: LinearLayout
    private lateinit var next: android.widget.TextView
    private var types: List<Period> = emptyList()
    private var step = 0
    private val kept = HashSet<Long>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        types = GoalReview.dueTypes(state, today())
        if (types.isEmpty()) { finish(); return }
        step = savedInstanceState?.getInt("step", 0) ?: 0
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { }
        })
        box = vbox()
        steps = hbox()
        next = button("", primary = true) { advance() }
        val head = vbox()
        head.add(steps, MATCH, WRAP, 0f, 16, 0, 16, 8)
        val root = vbox()
        root.add(topBar("장기 목표 점검", false))
        root.add(head)
        root.add(scrollOf(box), MATCH, 0, 1f)
        root.add(hline(2f))
        root.add(next.apply { pad(0, 0, 0, 0) }, MATCH, WRAP, 0f, 16, 10, 16, 10)
        show(root)
        redraw()
    }

    override fun onResume() {
        super.onResume()
        if (::box.isInitialized) redraw()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("step", step)
    }

    private fun redraw() {
        val s = state; val t = today()
        val p = types[step]
        steps.removeAllViews()
        types.forEachIndexed { i, type ->
            steps.add(label(type.label, 13f, bold = i == step).apply {
                gravity = android.view.Gravity.CENTER
                background = shape(strokeDp = if (i == step) 3f else 1f, radiusDp = 8f)
                setPadding(0, dp(5), 0, dp(5))
            }, 0, WRAP, 1f, if (i == 0) 0 else 6, 0, 0, 0)
        }
        next.text = if (step < types.size - 1) "${types[step + 1].label} 점검으로 ▸" else "정리 끝내기"
        box.removeAllViews()
        val goals = GoalReview.goalsOf(s, p).filter { it.id !in kept }
        box.add(groupHeader("${p.label} 목표", "${goals.size}개"))
        if (goals.isEmpty()) box.add(label("점검할 목표가 없어요.", 14f), MATCH, WRAP, 0f, 0, 16, 0, 0)
        goals.forEach { g ->
            val days = Dates.daysBetween(t, g.target)
            val late = days < 0
            val card = vbox().apply { background = shape(strokeDp = 1.5f, radiusDp = 12f); setPadding(dp(12), dp(10), dp(12), dp(10)) }
            val head = hbox()
            head.add(label(Dates.dText(days), 14f, bold = true, mono = true), dp(60), WRAP)
            head.add(label(g.text, 15f, bold = true), 0, WRAP, 1f)
            card.add(head)
            if (late) card.add(label("목표 날짜(${g.target})가 지났어요", 12f), t = 4)
            val acts = hbox()
            acts.add(button("달성", small = true) { mutate { Ops.removeGoal(it, g.id) }; redraw() }, 0, WRAP, 1f, 0, 0, 6, 0)
            if (late) acts.add(button("연장", small = true) {
                startActivity(Intent(this, GoalEditActivity::class.java).putExtra(GoalEditActivity.EXTRA_MODE, GoalEditActivity.EXTEND).putExtra(GoalEditActivity.EXTRA_ID, g.id))
            }, 0, WRAP, 1f, 0, 0, 6, 0)
            else acts.add(button("계속", small = true) { kept.add(g.id); redraw() }, 0, WRAP, 1f, 0, 0, 6, 0)
            acts.add(button("삭제", small = true) {
                Dialogs.confirm(this, "‘${g.text}’ 목표를 지울까요?", "삭제") { mutate { Ops.removeGoal(it, g.id) }; redraw() }
            }, 0, WRAP, 1f)
            card.add(acts, t = 8)
            box.add(card, MATCH, WRAP, 0f, 0, 8, 0, 0)
        }
        box.add(label("같은 날 여러 점검이 있으면 1주, 1달, 1년 순으로 나와요.", 12f), MATCH, WRAP, 0f, 0, 12, 0, 0)
    }

    private fun advance() {
        val t = today()
        mutate { GoalReview.markDone(it, types[step], t) }
        kept.clear()
        if (step < types.size - 1) { step++; redraw() } else finish()
    }
}
