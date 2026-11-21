package com.kjooy0305.padmacro

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.kjooy0305.padmacro.Ui.body
import com.kjooy0305.padmacro.Ui.button
import com.kjooy0305.padmacro.Ui.card
import com.kjooy0305.padmacro.Ui.check
import com.kjooy0305.padmacro.Ui.chip
import com.kjooy0305.padmacro.Ui.column
import com.kjooy0305.padmacro.Ui.dp
import com.kjooy0305.padmacro.Ui.edit
import com.kjooy0305.padmacro.Ui.field
import com.kjooy0305.padmacro.Ui.int
import com.kjooy0305.padmacro.Ui.lp
import com.kjooy0305.padmacro.Ui.msToSeconds
import com.kjooy0305.padmacro.Ui.outline
import com.kjooy0305.padmacro.Ui.row
import com.kjooy0305.padmacro.Ui.secondsToMs
import com.kjooy0305.padmacro.Ui.section
import com.kjooy0305.padmacro.Ui.small

/**
 * 매크로 편집: 이름 · 실행 설정(반복/대기) · 단계 목록 · 단계 추가/녹화/실행.
 * 바뀌는 즉시 저장한다. 좌표 찍기·녹화는 MacroService 의 오버레이가 하고, 끝나면 이 화면이 다시 앞으로 온다.
 */
class EditorActivity : Activity() {

    companion object { const val EXTRA_ID = "id" }

    private lateinit var store: MacroStore
    lateinit var macro: Macro
        private set
    private lateinit var stepsBox: LinearLayout
    private lateinit var countText: TextView
    private lateinit var dialogs: StepEditor

    /** 화면에서 좌표를 찍고 돌아올 때 어느 단계 대화상자를 다시 열지 */
    private var pendingIndex: Int? = null
    private var pendingDraft: Step? = null
    private var pendingReplace = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        store = MacroStore(this)
        val id = intent.getStringExtra(EXTRA_ID)
        macro = id?.let { store.get(it) } ?: run { finish(); return }
        dialogs = StepEditor(this)

        val root = column().apply { setPadding(dp(20), dp(16), dp(20), dp(40)) }

        // 상단: 뒤로 + 이름
        val top = row()
        top.addView(chip("‹ 목록") { finish() }.apply { (layoutParams as LinearLayout.LayoutParams).marginStart = 0 })
        val name = edit("매크로 이름", macro.name).apply { textSize = 19f; typeface = Typeface.DEFAULT_BOLD }
        name.layoutParams = lp(0, weight = 1f).apply { marginStart = dp(10) }
        name.watch { macro.name = it.ifBlank { macro.name }; save() }
        top.addView(name)
        root.addView(top)

        // 실행 설정
        root.addView(section("실행 설정"))
        val settings = card()
        val repeat = edit("0 = 무한", macro.repeat.toString(), number = true)
        repeat.watch { macro.repeat = repeat.int(1); save() }
        settings.addView(field("반복 횟수", repeat))
        settings.addView(small("0 을 넣으면 ■ 를 누를 때까지 반복합니다."))
        val gap = edit("초", msToSeconds(macro.gapMs), decimal = true)
        gap.watch { macro.gapMs = gap.secondsToMs(500).coerceIn(0, 600_000); save() }
        settings.addView(field("단계 사이 대기", gap))
        val start = edit("초", msToSeconds(macro.startDelayMs), decimal = true)
        start.watch { macro.startDelayMs = start.secondsToMs(3000).coerceIn(0, 600_000); save() }
        settings.addView(field("시작 전 대기", start))
        val rgap = edit("초 (0 = 단계 사이 대기와 같게)", msToSeconds(macro.repeatGapMs), decimal = true)
        rgap.watch { macro.repeatGapMs = rgap.secondsToMs(0).coerceIn(0, 3_600_000); save() }
        settings.addView(field("반복 사이 대기", rgap))
        settings.addView(check("끝나면 진동으로 알리기", macro.vibrateOnEnd) { macro.vibrateOnEnd = it; save() })
        root.addView(settings)

        // 단계
        val head = row()
        head.addView(section("단계").apply { layoutParams = lp(0, weight = 1f) })
        countText = TextView(this).apply { textSize = 13f; setTextColor(0xFF666666.toInt()); setPadding(0, dp(22), 0, dp(8)) }
        head.addView(countText)
        root.addView(head)
        stepsBox = column(); root.addView(stepsBox)

        root.addView(button("＋ 단계 추가") { dialogs.addNew(macro.steps.size) })
        root.addView(outline("● 터치 녹화로 단계 추가") { record() })
        root.addView(small("녹화: 이 앱이 뒤로 빠지고 화면 위에 빨간 띠가 뜹니다. 그때부터 하는 터치·드래그가 그대로 기록되고 아래 앱에도 전달됩니다. 띠의 ‘■ 끝내기’ 로 마칩니다."))
        root.addView(button("▶ 지금 실행") { run() })
        root.addView(small("‘시작 전 대기’ 동안 매크로를 돌릴 앱을 열어 두세요. 플로팅 패널의 ▶ 를 쓰면 이 화면을 거치지 않아도 됩니다."))

        setContentView(ScrollView(this).apply { addView(root); isFillViewport = true })
        rebuild()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        // 녹화로 단계가 추가됐거나 다른 곳에서 바뀐 경우 다시 읽는다
        store.get(macro.id)?.let { fresh ->
            if (fresh.steps.size != macro.steps.size || fresh.steps != macro.steps) { macro = fresh; rebuild() }
        }
        if (MacroService.recordedMacroId == macro.id) MacroService.recordedMacroId = null
        // 좌표 찍기 결과
        val picked = MacroService.consumePicked()
        val idx = pendingIndex; val draft = pendingDraft; val rep = pendingReplace
        pendingIndex = null; pendingDraft = null; pendingReplace = false
        if (picked != null && draft != null && idx != null) dialogs.reopenWithPick(idx, draft, picked, rep)
        else if (picked != null && draft == null) {
            // 대화상자 정보가 사라졌으면(앱이 종료됐다가 돌아온 경우) 끝에 그대로 추가
            macro.steps.add(picked); save(); rebuild()
        }
    }

    fun save() { store.put(macro); MacroService.instance?.panel?.update() }

    fun rebuild() {
        stepsBox.removeAllViews()
        countText.text = "${macro.steps.size}단계"
        if (macro.steps.isEmpty()) stepsBox.addView(small("단계가 없습니다. ‘＋ 단계 추가’ 또는 ‘● 터치 녹화’ 로 넣어 주세요."))
        macro.steps.forEachIndexed { i, s ->
            val r = row().apply {
                setPadding(0, dp(8), 0, dp(8))
                setOnClickListener { dialogs.edit(i, s) }
            }
            r.addView(TextView(this).apply {
                text = "${i + 1}"; textSize = 13f; setTextColor(Color.WHITE); gravity = Gravity.CENTER
                background = android.graphics.drawable.GradientDrawable().apply { shape = android.graphics.drawable.GradientDrawable.OVAL; setColor(Color.BLACK) }
                layoutParams = lp(dp(26), dp(26)).apply { marginEnd = dp(10) }
            })
            r.addView(TextView(this).apply {
                text = s.describe(); textSize = 15f; setTextColor(Color.BLACK)
                layoutParams = lp(0, weight = 1f)
            })
            r.addView(chip("↑") { move(i, -1) })
            r.addView(chip("↓") { move(i, 1) })
            r.addView(chip("⋮") { v -> stepMenu(v as TextView, i, s) })
            stepsBox.addView(r)
            stepsBox.addView(android.view.View(this).apply { setBackgroundColor(0xFFEEEEEE.toInt()); layoutParams = lp(h = dp(1)) })
        }
    }

    private fun stepMenu(anchor: TextView, i: Int, s: Step) {
        val pm = PopupMenu(this, anchor)
        pm.menu.add("편집"); pm.menu.add("복제"); pm.menu.add("아래에 추가"); pm.menu.add("삭제")
        pm.setOnMenuItemClickListener {
            when (it.title) {
                "편집" -> dialogs.edit(i, s)
                "복제" -> { macro.steps.add(i + 1, s); save(); rebuild() }
                "아래에 추가" -> dialogs.addNew(i + 1)
                "삭제" -> { macro.steps.removeAt(i); save(); rebuild() }
            }
            true
        }
        pm.show()
    }

    private fun move(i: Int, d: Int) {
        val j = i + d
        if (j < 0 || j >= macro.steps.size) return
        val s = macro.steps.removeAt(i); macro.steps.add(j, s); save(); rebuild()
    }

    /** 대화상자에서 결정된 단계를 넣는다. index 가 목록 길이 이상이면 끝에 추가, replace 면 그 자리 교체 */
    fun commit(index: Int, step: Step, replace: Boolean) {
        if (replace && index < macro.steps.size) macro.steps[index] = step
        else macro.steps.add(index.coerceIn(0, macro.steps.size), step)
        save(); rebuild()
    }

    /** 좌표 찍기 시작: 지금 대화상자 내용을 기억해 두고 서비스 오버레이로 넘어간다 */
    fun requestPick(index: Int, draft: Step, wantSwipe: Boolean, replace: Boolean) {
        val svc = MacroService.instance
        if (svc == null) { Toast.makeText(this, "먼저 접근성 서비스를 켜 주세요", Toast.LENGTH_LONG).show(); return }
        pendingIndex = index; pendingDraft = draft; pendingReplace = replace
        svc.startPick(macro.id, wantSwipe)
        Toast.makeText(this, if (wantSwipe) "원하는 앱 화면에서 드래그하세요" else "원하는 앱 화면에서 위치를 누르세요", Toast.LENGTH_LONG).show()
        moveTaskToBack(true)
    }

    private fun record() {
        val svc = MacroService.instance
        if (svc == null) { Toast.makeText(this, "먼저 접근성 서비스를 켜 주세요", Toast.LENGTH_LONG).show(); return }
        save()
        svc.startRecord(macro.id)
        Toast.makeText(this, "녹화 중입니다. 원하는 앱을 열고 터치하세요", Toast.LENGTH_LONG).show()
        moveTaskToBack(true)
    }

    private fun run() {
        val svc = MacroService.instance
        if (svc == null) { Toast.makeText(this, "먼저 접근성 서비스를 켜 주세요", Toast.LENGTH_LONG).show(); return }
        if (macro.steps.isEmpty()) { Toast.makeText(this, "단계가 없습니다", Toast.LENGTH_SHORT).show(); return }
        save()
        store.selectedId = macro.id
        svc.startMacro(macro)
        val sec = (macro.startDelayMs + 999) / 1000
        Toast.makeText(this, if (sec > 0) "${sec}초 뒤 시작합니다. 매크로를 돌릴 화면을 열어 두세요" else "시작합니다", Toast.LENGTH_LONG).show()
        moveTaskToBack(true)
    }

    private fun EditText.watch(onChange: (String) -> Unit) = addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
        override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
        override fun afterTextChanged(s: Editable?) = onChange(s?.toString() ?: "")
    })
}
