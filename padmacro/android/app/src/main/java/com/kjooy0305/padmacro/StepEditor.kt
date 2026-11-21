package com.kjooy0305.padmacro

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.kjooy0305.padmacro.Ui.body
import com.kjooy0305.padmacro.Ui.check
import com.kjooy0305.padmacro.Ui.column
import com.kjooy0305.padmacro.Ui.dp
import com.kjooy0305.padmacro.Ui.edit
import com.kjooy0305.padmacro.Ui.field
import com.kjooy0305.padmacro.Ui.int
import com.kjooy0305.padmacro.Ui.long
import com.kjooy0305.padmacro.Ui.lp
import com.kjooy0305.padmacro.Ui.msToSeconds
import com.kjooy0305.padmacro.Ui.outline
import com.kjooy0305.padmacro.Ui.row
import com.kjooy0305.padmacro.Ui.secondsToMs
import com.kjooy0305.padmacro.Ui.small

/** 단계 종류 고르기 + 종류별 입력 대화상자 */
class StepEditor(private val a: EditorActivity) {

    private data class Kind(val label: String, val hint: String, val make: () -> Step)

    private val kinds = listOf(
        Kind("탭", "정한 좌표를 한 번(또는 여러 번) 누릅니다") { Step.Tap(0, 0) },
        Kind("길게 누르기", "정한 좌표를 정한 시간 동안 누르고 있습니다") { Step.LongPress(0, 0) },
        Kind("스와이프 (드래그)", "시작점에서 끝점까지 끌어 스크롤·넘기기를 합니다") { Step.Swipe(0, 0, 0, 0) },
        Kind("대기", "정한 시간만큼 기다립니다") { Step.Wait(1000) },
        Kind("화면 캡쳐", "지금 화면을 갤러리 › Pictures/패드매크로 에 저장합니다 (안드로이드 11+)") { Step.Capture() },
        Kind("글자 찾아 누르기", "화면에서 글자가 있는 버튼을 찾아 누릅니다. 나타날 때까지 기다립니다") { Step.ClickText("") },
        Kind("글자 기다리기", "글자가 화면에 나타날 때까지 기다립니다 (로딩 끝 확인용)") { Step.WaitText("") },
        Kind("앱 실행", "다른 앱을 띄웁니다") { Step.LaunchApp("") },
        Kind("시스템 동작", "뒤로 · 홈 · 최근 앱 · 알림창 등") { Step.Global(Step.BACK) },
        Kind("URL 열기", "브라우저로 주소를 엽니다") { Step.OpenUrl("") },
        Kind("진동", "진동으로 알려 줍니다") { Step.Vibrate() },
    )

    fun addNew(index: Int) {
        AlertDialog.Builder(a).setTitle("어떤 단계를 넣을까요?")
            .setItems(kinds.map { "${it.label}  —  ${it.hint}" }.toTypedArray()) { _, which -> open(index, kinds[which].make(), replace = false) }
            .setNegativeButton("취소", null).show()
    }

    fun edit(index: Int, step: Step) = open(index, step, replace = true)

    /** 좌표 찍기에서 돌아왔을 때: 찍은 좌표를 원래 입력 중이던 단계에 넣어 다시 연다 */
    fun reopenWithPick(index: Int, draft: Step, picked: Step, replace: Boolean) {
        val merged: Step = when (draft) {
            is Step.Tap -> when (picked) { is Step.Tap -> draft.copy(x = picked.x, y = picked.y); is Step.LongPress -> draft.copy(x = picked.x, y = picked.y); is Step.Swipe -> draft.copy(x = picked.x1, y = picked.y1); else -> draft }
            is Step.LongPress -> when (picked) { is Step.Tap -> draft.copy(x = picked.x, y = picked.y); is Step.LongPress -> draft.copy(x = picked.x, y = picked.y, durationMs = picked.durationMs); is Step.Swipe -> draft.copy(x = picked.x1, y = picked.y1); else -> draft }
            is Step.Swipe -> when (picked) { is Step.Swipe -> draft.copy(x1 = picked.x1, y1 = picked.y1, x2 = picked.x2, y2 = picked.y2, durationMs = picked.durationMs); is Step.Tap -> draft.copy(x1 = picked.x, y1 = picked.y); is Step.LongPress -> draft.copy(x1 = picked.x, y1 = picked.y); else -> draft }
            else -> draft
        }
        open(index, merged, replace)
    }

    private fun open(index: Int, step: Step, replace: Boolean) {
        when (step) {
            is Step.Tap -> coordDialog("탭", index, step, replace)
            is Step.LongPress -> coordDialog("길게 누르기", index, step, replace)
            is Step.Swipe -> coordDialog("스와이프", index, step, replace)
            is Step.Wait -> simple("대기", index, replace, listOf("초" to a.edit("예: 1.5", msToSeconds(step.ms), decimal = true))) { f -> Step.Wait(f[0].secondsToMs(1000).coerceIn(0, 3_600_000)) }
            is Step.Capture -> simple("화면 캡쳐", index, replace, listOf("파일 이름에 붙일 말" to a.edit("비워도 됩니다", step.label)), "저장 위치: 갤러리 › Pictures/패드매크로. 캡쳐 순간 플로팅 패널은 잠깐 숨겨집니다.") { f -> Step.Capture(f[0].text.toString().trim()) }
            is Step.ClickText -> textDialog("글자 찾아 누르기", index, step.text, step.timeoutMs, step.exact, "글자와 정확히 같아야 함 (끄면 포함되면 됨)", replace) { t, ms, flag -> Step.ClickText(t, ms, flag) }
            is Step.WaitText -> textDialog("글자 기다리기", index, step.text, step.timeoutMs, step.stopIfMissing, "시간 안에 안 나타나면 매크로 중단", replace) { t, ms, flag -> Step.WaitText(t, ms, flag) }
            is Step.LaunchApp -> appDialog(index, step, replace)
            is Step.Global -> {
                val keys = Step.GLOBAL_LABELS.keys.toList()
                AlertDialog.Builder(a).setTitle("시스템 동작")
                    .setItems(Step.GLOBAL_LABELS.values.toTypedArray()) { _, w -> a.commit(index, Step.Global(keys[w]), replace) }
                    .setNegativeButton("취소", null).show()
            }
            is Step.OpenUrl -> simple("URL 열기", index, replace, listOf("주소" to a.edit("https://…", step.url).apply { inputType = android.text.InputType.TYPE_TEXT_VARIATION_URI })) { f -> Step.OpenUrl(f[0].text.toString().trim()) }
            is Step.Vibrate -> simple("진동", index, replace, listOf("길이 (ms)" to a.edit("300", step.ms.toString(), number = true))) { f -> Step.Vibrate(f[0].long(300).coerceIn(10, 3000)) }
        }
    }

    // ---------------------------------------------------------------- 공통 대화상자들

    private fun box(): LinearLayout = a.column().apply { setPadding(a.dp(20), a.dp(8), a.dp(20), 0) }

    private fun simple(title: String, index: Int, replace: Boolean, fields: List<Pair<String, EditText>>, note: String? = null, build: (List<EditText>) -> Step) {
        val v = box()
        fields.forEach { (label, e) -> v.addView(a.field(label, e)) }
        note?.let { v.addView(a.small(it)) }
        AlertDialog.Builder(a).setTitle(title).setView(ScrollView(a).apply { addView(v) })
            .setPositiveButton(if (replace) "저장" else "추가") { _, _ -> a.commit(index, build(fields.map { it.second }), replace) }
            .setNegativeButton("취소", null).show()
    }

    private fun textDialog(title: String, index: Int, text: String, timeoutMs: Long, flag: Boolean, flagLabel: String, replace: Boolean, build: (String, Long, Boolean) -> Step) {
        val v = box()
        val t = a.edit("예: 확인, 다음, 건너뛰기", text)
        val to = a.edit("초", msToSeconds(timeoutMs), decimal = true)
        val cb = a.check(flagLabel, flag) {}
        v.addView(a.field("글자", t)); v.addView(a.field("최대 기다림", to)); v.addView(cb)
        v.addView(a.small("화면 트리에서 글자(또는 설명)를 찾습니다. 그림만 있는 버튼은 못 찾으니 그때는 ‘탭’ 좌표를 쓰세요."))
        AlertDialog.Builder(a).setTitle(title).setView(ScrollView(a).apply { addView(v) })
            .setPositiveButton(if (replace) "저장" else "추가") { _, _ ->
                val s = t.text.toString().trim()
                if (s.isEmpty()) { android.widget.Toast.makeText(a, "글자를 입력하세요", android.widget.Toast.LENGTH_SHORT).show(); return@setPositiveButton }
                a.commit(index, build(s, to.secondsToMs(5000).coerceIn(300, 600_000), cb.isChecked), replace)
            }
            .setNegativeButton("취소", null).show()
    }

    /** 탭·길게 누르기·스와이프: 좌표 입력 + ‘화면에서 찍기’ */
    private fun coordDialog(title: String, index: Int, step: Step, replace: Boolean) {
        val v = box()
        val swipe = step is Step.Swipe
        val x1 = a.edit("x", when (step) { is Step.Tap -> step.x; is Step.LongPress -> step.x; is Step.Swipe -> step.x1; else -> 0 }.toString(), number = true)
        val y1 = a.edit("y", when (step) { is Step.Tap -> step.y; is Step.LongPress -> step.y; is Step.Swipe -> step.y1; else -> 0 }.toString(), number = true)
        val x2 = a.edit("x", (step as? Step.Swipe)?.x2?.toString() ?: "0", number = true)
        val y2 = a.edit("y", (step as? Step.Swipe)?.y2?.toString() ?: "0", number = true)
        val extra = when (step) {
            is Step.Tap -> a.edit("1", step.count.toString(), number = true)
            is Step.LongPress -> a.edit("600", step.durationMs.toString(), number = true)
            is Step.Swipe -> a.edit("300", step.durationMs.toString(), number = true)
            else -> a.edit("", "")
        }
        fun xy(label: String, x: EditText, y: EditText) = a.row().apply {
            addView(TextView(a).apply { text = label; textSize = 14f; setTextColor(0xFF444444.toInt()); layoutParams = lp(a.dp(110)) })
            x.layoutParams = lp(0, weight = 1f).apply { marginEnd = a.dp(6) }; y.layoutParams = lp(0, weight = 1f)
            addView(x); addView(y)
        }
        v.addView(xy(if (swipe) "시작 (x, y)" else "위치 (x, y)", x1, y1))
        if (swipe) v.addView(xy("끝 (x, y)", x2, y2))
        v.addView(a.field(when (step) { is Step.Tap -> "탭 횟수"; is Step.LongPress -> "누르는 시간 (ms)"; else -> "걸리는 시간 (ms)" }, extra))
        val dm = a.resources.displayMetrics
        v.addView(a.small("좌표는 화면 왼쪽 위가 (0, 0)인 픽셀 값입니다. 이 화면: ${dm.widthPixels} × ${dm.heightPixels}. 가로/세로를 바꾸면 좌표도 달라집니다."))

        fun current(): Step = when (step) {
            is Step.Tap -> Step.Tap(x1.int(0), y1.int(0), extra.int(1).coerceIn(1, 10))
            is Step.LongPress -> Step.LongPress(x1.int(0), y1.int(0), extra.long(600).coerceIn(100, 10_000))
            is Step.Swipe -> Step.Swipe(x1.int(0), y1.int(0), x2.int(0), y2.int(0), extra.long(300).coerceIn(50, 10_000))
            else -> step
        }

        val dlg = AlertDialog.Builder(a).setTitle(title).setView(ScrollView(a).apply { addView(v) })
            .setPositiveButton(if (replace) "저장" else "추가") { _, _ -> a.commit(index, current(), replace) }
            .setNeutralButton(if (swipe) "화면에서 그리기" else "화면에서 찍기", null)
            .setNegativeButton("취소", null).create()
        dlg.setOnShowListener {
            dlg.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                dlg.dismiss()
                a.requestPick(index, current(), swipe, replace)
            }
        }
        dlg.show()
    }

    /** 설치된 앱 목록에서 고르기 (검색 가능) */
    private fun appDialog(index: Int, step: Step.LaunchApp, replace: Boolean) {
        val pm = a.packageManager
        val list = a.column()
        val filter = a.edit("앱 이름 검색", "")
        val v = box().apply { addView(filter); addView(a.small("불러오는 중…")) }
        val scroll = ScrollView(a).apply { addView(list) }
        v.addView(scroll, lp(h = a.dp(360)))
        val dlg = AlertDialog.Builder(a).setTitle("앱 실행").setView(v).setNegativeButton("취소", null).create()
        dlg.show()

        Thread {
            val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            val apps = try {
                pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
                    .map { it.activityInfo.packageName to (it.loadLabel(pm)?.toString() ?: it.activityInfo.packageName) }
                    .distinctBy { it.first }
                    .sortedBy { it.second.lowercase() }
            } catch (e: Exception) { emptyList() }
            a.runOnUiThread {
                if (v.childCount > 2) v.removeViewAt(1)
                fun render(q: String) {
                    list.removeAllViews()
                    apps.filter { q.isBlank() || it.second.contains(q, true) || it.first.contains(q, true) }.take(300).forEach { (pkg, label) ->
                        list.addView(TextView(a).apply {
                            text = "$label\n$pkg"; textSize = 15f; setTextColor(Color.BLACK)
                            setPadding(a.dp(8), a.dp(10), a.dp(8), a.dp(10))
                            setOnClickListener { dlg.dismiss(); a.commit(index, Step.LaunchApp(pkg, label), replace) }
                        })
                    }
                    if (list.childCount == 0) list.addView(a.small("일치하는 앱이 없습니다"))
                }
                render("")
                filter.addTextChangedListener(object : android.text.TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
                    override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
                    override fun afterTextChanged(s: android.text.Editable?) = render(s?.toString() ?: "")
                })
            }
        }.start()
    }
}
