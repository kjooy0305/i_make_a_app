package com.kjooy0305.dayclean.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.kjooy0305.dayclean.logic.Dates
import com.kjooy0305.dayclean.logic.Gate
import com.kjooy0305.dayclean.logic.Ops
import com.kjooy0305.dayclean.model.CleanupType
import com.kjooy0305.dayclean.model.Item
import com.kjooy0305.dayclean.model.Kind
import com.kjooy0305.dayclean.platform.AlarmScheduler
import com.kjooy0305.dayclean.platform.Notifier

/** 홈: 오늘 날짜, 이동 버튼, 기록장. 정리 시간이 열려 있으면 홈 대신 그 화면으로 바로 간다. */
class HomeActivity : BaseActivity() {
    private lateinit var dateText: TextView
    private lateinit var weekText: TextView
    private lateinit var goalsButton: TextView
    private lateinit var listBox: LinearLayout
    private lateinit var scroll: ScrollView
    private lateinit var input: EditText
    private lateinit var startHint: TextView

    private var kind = Kind.NOW
    private var startId: Long? = null
    private val frozen = HashMap<Long, Boolean>()   // 체크한 항목은 화면을 떠났다 오기 전까지 제자리에 둔다
    private var left = false

    private val askNotifications = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = vbox()

        val date = vbox().apply { gravityCenter() }
        dateText = label("", 28f, bold = true).apply { gravity = android.view.Gravity.CENTER }
        weekText = label("", 13f).apply { gravity = android.view.Gravity.CENTER }
        date.add(dateText, MATCH, WRAP, 0f, 16, 12, 16, 0)
        date.add(weekText, MATCH, WRAP, 0f, 16, 2, 16, 8)
        root.add(date)

        val nav = hbox().apply { setPadding(dp(12), 0, dp(12), dp(10)) }
        nav.add(button("설정") { startActivity(Intent(this, SettingsActivity::class.java)) })
        nav.add(android.view.View(this), 0, 1, 1f)
        goalsButton = button("") { startActivity(Intent(this, GoalsActivity::class.java)) }
        nav.add(goalsButton, WRAP, WRAP, 0f, 0, 0, 8, 0)
        nav.add(button("메모리") { startActivity(Intent(this, MemoryActivity::class.java)) })
        root.add(nav)

        root.add(hline(2f))
        root.add(composer())
        root.add(hline(1f))
        scroll = ScrollView(this).apply { isVerticalScrollBarEnabled = true }
        listBox = vbox().apply { setPadding(dp(16), 0, dp(16), dp(24)) }
        scroll.addView(listBox, MATCH, WRAP)
        root.add(scroll, MATCH, 0, 1f)
        show(root)

        maybeAskNotifications()
    }

    private fun LinearLayout.gravityCenter() { gravity = android.view.Gravity.CENTER_HORIZONTAL }

    private fun composer(): android.view.View {
        val box = vbox().apply { setPadding(dp(16), dp(10), dp(16), dp(10)) }
        box.add(Seg(this, listOf("• 지금", "- 나중에", "= 메모"), 0) { i ->
            kind = listOf(Kind.NOW, Kind.LATER, Kind.MEMO)[i]
            input.hint = when (kind) {
                Kind.NOW -> "지금 기억나는 것을 적어요"
                Kind.LATER -> "오늘 안에 해야 할 것"
                Kind.MEMO -> "메모를 적어요 (알림 없음)"
            }
            updateStartHint()
        })
        val row = hbox()
        input = textInput("지금 기억나는 것을 적어요").apply {
            imeOptions = EditorInfo.IME_ACTION_DONE
            setOnEditorActionListener { _, id, _ -> if (id == EditorInfo.IME_ACTION_DONE) { addItem(); true } else false }
        }
        row.add(input, 0, WRAP, 1f, 0, 0, 8, 0)
        row.add(button("추가", primary = true) { addItem() })
        box.add(row, t = 8)
        startHint = label("", 12f).apply {
            setOnClickListener { chooseStart() }
            isClickable = true
        }
        box.add(startHint, MATCH, WRAP, 0f, 0, 6, 0, 0)
        updateStartHint()
        return box
    }

    private fun updateStartHint() {
        if (kind != Kind.LATER) { startHint.visibility = android.view.View.GONE; return }
        startHint.visibility = android.view.View.VISIBLE
        val c = state.settings.cleanup(startId) ?: state.settings.cleanup(state.settings.defaultLaterStartId)
        startHint.text = "‘-’ 알림 시작: ${c?.let { "${it.name} ${Dates.hhmm(it.minuteOfDay)}" } ?: "-"} ▾"
        startHint.paintFlags = startHint.paintFlags or android.graphics.Paint.UNDERLINE_TEXT_FLAG
        startHint.minHeight = dp(36)
        startHint.gravity = android.view.Gravity.CENTER_VERTICAL
    }

    private fun chooseStart() {
        val list = state.settings.cleanups.sortedBy { it.minuteOfDay }
        val cur = list.indexOfFirst { it.id == (startId ?: state.settings.defaultLaterStartId) }
        Dialogs.pick(this, "‘-’ 알림 시작 시간", list.map { Dialogs.cleanupLabel(it) }, cur) { startId = list[it].id; updateStartHint() }
    }

    private fun addItem() {
        val text = input.text.toString().trim()
        if (text.isEmpty()) { input.error = "내용을 적어 주세요"; return }
        mutate { Ops.addItem(it, kind, text, store.now(), if (kind == Kind.LATER) startId else null) }
        input.setText("")
        rebuild()
    }

    private fun maybeAskNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !state.onboarded &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            askNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (!state.onboarded) store.update { it.copy(onboarded = true) }
    }

    override fun onStop() {
        super.onStop()
        left = true
    }

    override fun onResume() {
        super.onResume()
        // 날짜가 바뀌었으면 메모리로 옮기고, 지금 울릴 알림이 있으면 울린다.
        AlarmScheduler.run(applicationContext)
        if (left) { frozen.clear(); left = false }
        if (openGate()) return
        rebuild()
    }

    /** 정리 시간이 열려 있으면 그 화면으로 바로 간다. */
    private fun openGate(): Boolean {
        val screen = Gate.active(state, store.now()) ?: return false
        val intent = when (screen) {
            is Gate.Screen.Cleanup -> Intent(this, when (screen.cleanup.type) {
                CleanupType.MORNING -> MorningActivity::class.java
                CleanupType.EVENING -> EveningActivity::class.java
                CleanupType.REVIEW -> ReviewActivity::class.java
                CleanupType.NOTIFY -> return false
            }).putExtra(EXTRA_CLEANUP_ID, screen.cleanup.id)
            is Gate.Screen.GoalCheck -> Intent(this, GoalCheckActivity::class.java)
        }
        startActivity(intent)
        return true
    }

    private fun rebuild() {
        val t = today()
        dateText.text = "${t.year}년 ${t.monthValue}월 ${t.dayOfMonth}일"
        weekText.text = Dates.dow(t.dayOfWeek) + "요일"
        goalsButton.text = "장기 목표 ${Dates.dText(Dates.daysToNextFirst(t))}"
        goalsButton.contentDescription = "장기 목표, 다음 달 1일까지 ${Dates.daysToNextFirst(t)}일"
        updateStartHint()

        val s = state
        val items = Ops.today(s, t)
        fun shownDone(i: Item) = frozen[i.id] ?: i.done
        val carry = items.filter { it.carried && it.kind != Kind.MEMO && !shownDone(it) }
        val now = items.filter { !it.carried && it.kind == Kind.NOW && !shownDone(it) }
        val later = items.filter { !it.carried && it.kind == Kind.LATER && !shownDone(it) }
        val memos = items.filter { it.kind == Kind.MEMO }
        val done = items.filter { it.kind != Kind.MEMO && shownDone(it) }.sortedBy { it.doneAt ?: it.createdAt }

        listBox.removeAllViews()
        fun section(title: String, right: String?, list: List<Item>, yellow: Boolean, checkable: Boolean = true) {
            if (list.isEmpty()) return
            listBox.add(groupHeader(title, right))
            list.forEach { i ->
                listBox.add(ItemRows.build(this, i, s, t, yellow = yellow && !shownDone(i), checkable = checkable,
                    onToggle = { item, row -> toggle(item, row) }, onLong = { edit(it) }).view)
            }
        }
        section("어제 넘긴 기록", null, carry, true)
        section("지금 해야 할 것 •", "10분마다 알림", now, false)
        section("오늘 안에 -", "정한 정리 시간부터 알림", later, false)
        section("메모 =", null, memos, false, checkable = false)
        section("해결한 것", "체크를 풀면 위로 올라가요", done, false)
        if (items.isEmpty()) {
            listBox.add(label("아직 기록이 없어요.\n위에서 유형을 고르고 지금 기억나는 것을 적어 보세요.", 14f).apply {
                gravity = android.view.Gravity.CENTER
            }, MATCH, WRAP, 0f, 0, 40, 0, 0)
        }
    }

    private fun toggle(item: Item, row: ItemRows.Row) {
        val before = state.items.firstOrNull { it.id == item.id } ?: return
        frozen.putIfAbsent(item.id, before.done)
        val s = mutate { Ops.toggleDone(it, item.id, store.now()) }
        val now = s.items.firstOrNull { it.id == item.id } ?: return
        row.check?.checked = now.done
        ItemRows.applyDone(row.text, now.done)
        row.meta.text = if (now.done && now.doneAt != null) Dates.hhmm(now.doneAt) else ""
        if (now.done) Notifier.cancelItem(applicationContext, item.id)
    }

    private fun edit(item: Item) {
        Dialogs.editItem(this, item.text, item.kind, item.startCleanupId, state.settings,
            onApply = { k, text, start ->
                mutate { Ops.editItem(it, item.id, k, text, start, store.now()) }
                rebuild()
            },
            onDelete = {
                mutate { Ops.deleteItem(it, item.id) }
                Notifier.cancelItem(applicationContext, item.id)
                rebuild()
            })
    }

    companion object {
        const val EXTRA_CLEANUP_ID = "cleanupId"
    }
}
