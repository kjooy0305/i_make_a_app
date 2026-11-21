package com.kjooy0305.padmacro

import android.app.Activity
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.kjooy0305.padmacro.Ui.big
import com.kjooy0305.padmacro.Ui.body
import com.kjooy0305.padmacro.Ui.button
import com.kjooy0305.padmacro.Ui.card
import com.kjooy0305.padmacro.Ui.check
import com.kjooy0305.padmacro.Ui.chip
import com.kjooy0305.padmacro.Ui.column
import com.kjooy0305.padmacro.Ui.divider
import com.kjooy0305.padmacro.Ui.dp
import com.kjooy0305.padmacro.Ui.edit
import com.kjooy0305.padmacro.Ui.lp
import com.kjooy0305.padmacro.Ui.outline
import com.kjooy0305.padmacro.Ui.row
import com.kjooy0305.padmacro.Ui.section
import com.kjooy0305.padmacro.Ui.small
import com.kjooy0305.padmacro.Ui.title

/** 메인 화면: 서비스 상태 · 매크로 목록 · 설정 · 사용법 · 최근 기록 */
class MainActivity : Activity() {

    private lateinit var store: MacroStore
    private lateinit var statusBox: LinearLayout
    private lateinit var listBox: LinearLayout
    private lateinit var logBox: LinearLayout

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) = refresh()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        store = MacroStore(this)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(24), dp(20), dp(40))
        }
        root.addView(title("패드 매크로"))
        root.addView(body("터치·스와이프·대기·캡쳐 같은 단계를 순서대로 모아 매크로를 만들고, 어느 앱 위에서나 떠 있는 패널의 ▶ 로 돌립니다. 터치를 그대로 따라 하며 녹화할 수도 있습니다."))

        root.addView(section("1. 준비"))
        statusBox = column(); root.addView(statusBox)

        root.addView(section("2. 매크로"))
        listBox = column(); root.addView(listBox)
        root.addView(button("＋ 새 매크로") { newMacro() })

        root.addView(section("3. 설정"))
        root.addView(check("플로팅 패널 보이기 (▶ 시작/중지, ● 녹화)", store.showPanel) {
            store.showPanel = it; MacroService.instance?.refreshPanel()
        })
        root.addView(check("볼륨 아래 버튼 두 번 → 선택한 매크로 시작/중지", store.volumeTrigger) { store.volumeTrigger = it })
        root.addView(small("볼륨키는 서비스가 켜져 있을 때만 잡힙니다. 두 번째 누름만 가로채므로 소리가 한 단계 줄어들 수 있습니다."))
        root.addView(check("녹화할 때 터치 사이 시간도 ‘대기’ 로 기록", store.recordTiming) { store.recordTiming = it })
        root.addView(check("시작·끝날 때 작게 알려 주기(토스트)", store.toast) { store.toast = it })

        root.addView(section("4. 사용법"))
        root.addView(body(
            "① 접근성 서비스를 켜면 화면에 작은 패널이 뜹니다.\n" +
            "② 매크로를 만들고 단계를 추가합니다. 좌표는 ‘화면에서 찍기’ 로 직접 누르거나, 편집 화면의 ‘● 터치 녹화’ 로 실제 터치를 그대로 기록합니다.\n" +
            "③ 돌릴 앱을 연 뒤 패널의 ▶ 를 누르면 ‘시작 전 대기’ 뒤에 실행됩니다. 패널 ■ 또는 볼륨 아래 두 번으로 멈춥니다.\n" +
            "④ ‘화면 캡쳐’ 단계는 갤러리 › Pictures/패드매크로 에 PNG 로 저장됩니다 (안드로이드 11 이상).\n" +
            "⑤ 패널의 매크로 이름을 끌면 자리를 옮기고, 살짝 누르면 이 앱이 열립니다. 패널 ○ 표시(목록의 ●)로 패널이 돌릴 매크로를 고릅니다."
        ))
        root.addView(small("실행 중에는 패널이 화면 위에 남아 있으니, 탭할 자리와 겹치면 패널을 다른 곳으로 옮겨 두세요. 캡쳐할 때는 패널이 잠깐 숨겨집니다."))

        root.addView(section("5. 최근 기록"))
        logBox = column(); root.addView(logBox)
        root.addView(outline("기록 지우기") { store.clearLog(); refresh() })

        setContentView(ScrollView(this).apply { addView(root); isFillViewport = true })
    }

    override fun onResume() {
        super.onResume()
        val f = IntentFilter(MacroService.ACTION_STATE)
        if (Build.VERSION.SDK_INT >= 33) registerReceiver(receiver, f, Context.RECEIVER_NOT_EXPORTED) else registerReceiver(receiver, f)
        refresh()
    }

    override fun onPause() {
        super.onPause()
        try { unregisterReceiver(receiver) } catch (_: Exception) {}
    }

    private fun refresh() {
        // ---- 상태
        statusBox.removeAllViews()
        val on = Perms.accessibilityOn(this)
        statusBox.addView(big(if (on) "● 접근성 서비스 켜짐" else "○ 접근성 서비스 꺼짐"))
        if (!on) {
            statusBox.addView(small("설정 › 접근성 › ‘패드 매크로 (자동 터치)’ 를 켜 주세요. 이 서비스가 터치를 대신 하고 화면을 캡쳐합니다."))
            statusBox.addView(button("접근성 설정 열기") {
                try { Perms.openAccessibility(this); Toast.makeText(this, "목록에서 ‘패드 매크로 (자동 터치)’ 를 켜 주세요", Toast.LENGTH_LONG).show() } catch (_: Exception) {}
            })
            if (Build.VERSION.SDK_INT >= 33) {
                statusBox.addView(small("안드로이드 13 이상에서 ‘제한된 설정’ 이라고 막히면: 설정 › 애플리케이션 › 패드 매크로 › 오른쪽 위 ⋮ › 제한된 설정 허용 을 누른 뒤 다시 켭니다."))
                statusBox.addView(outline("이 앱의 앱 정보 열기") { Perms.openAppInfo(this) })
            }
        }
        val overlay = Perms.canDrawOverlays(this)
        statusBox.addView(big(if (overlay) "● 다른 앱 위에 표시 허용됨" else "○ 다른 앱 위에 표시 (선택)"))
        if (!overlay) {
            statusBox.addView(small("좌표 찍기·녹화가 끝났을 때 이 앱으로 자동으로 돌아오고, ‘앱 실행’ 단계가 다른 앱을 띄우려면 필요합니다. 패널 자체는 이 권한 없이도 뜹니다."))
            statusBox.addView(outline("다른 앱 위에 표시 허용하기") { try { Perms.openOverlay(this) } catch (_: Exception) {} })
        }
        if (Build.VERSION.SDK_INT < 30) statusBox.addView(small("이 기기(안드로이드 ${Build.VERSION.RELEASE})에서는 ‘화면 캡쳐’ 단계가 지원되지 않습니다(11 이상 필요). 대신 ‘시스템 › 스크린샷’ 을 쓸 수 있습니다."))

        // ---- 목록
        listBox.removeAllViews()
        val macros = store.loadAll()
        val selectedId = store.selected()?.id
        if (macros.isEmpty()) listBox.addView(small("아직 매크로가 없습니다. 아래 ‘＋ 새 매크로’ 로 만들어 보세요."))
        val running = MacroService.instance?.currentMacro?.takeIf { MacroService.isRunning() }
        for (m in macros) {
            val card = card()
            val top = row()
            val mark = TextView(this).apply {
                text = if (m.id == selectedId) "●" else "○"; textSize = 20f; setTextColor(Color.BLACK)
                setPadding(0, 0, dp(12), 0)
                setOnClickListener { store.selectedId = m.id; MacroService.instance?.panel?.update(); refresh() }
            }
            top.addView(mark)
            val texts = column().apply { layoutParams = lp(0, weight = 1f) }
            texts.addView(TextView(this).apply { text = m.name; textSize = 17f; typeface = Typeface.DEFAULT_BOLD; setTextColor(Color.BLACK) })
            texts.addView(TextView(this).apply {
                text = m.summary() + if (running?.id == m.id) "  ·  실행 중" else ""
                textSize = 12.5f; setTextColor(0xFF666666.toInt())
            })
            texts.setOnClickListener { openEditor(m.id) }
            top.addView(texts)
            top.addView(chip(if (running?.id == m.id) "■" else "▶") {
                if (running?.id == m.id) MacroService.instance?.stopMacro() else start(m)
            })
            top.addView(chip("편집") { openEditor(m.id) })
            top.addView(chip("⋮") { v -> menu(v as TextView, m) })
            card.addView(top)
            listBox.addView(card)
        }

        // ---- 기록
        logBox.removeAllViews()
        val lines = store.logLines()
        if (lines.isEmpty()) logBox.addView(small("아직 기록이 없습니다."))
        else logBox.addView(TextView(this).apply {
            text = lines.take(30).joinToString("\n"); textSize = 12f; setTextColor(0xFF444444.toInt())
            typeface = Typeface.MONOSPACE; setLineSpacing(0f, 1.3f)
        })
    }

    private fun start(m: Macro) {
        val svc = MacroService.instance
        if (svc == null) { Toast.makeText(this, "먼저 접근성 서비스를 켜 주세요", Toast.LENGTH_LONG).show(); return }
        if (m.steps.isEmpty()) { Toast.makeText(this, "단계가 없습니다. 편집에서 단계를 추가하세요", Toast.LENGTH_SHORT).show(); return }
        store.selectedId = m.id
        svc.startMacro(m)
        val sec = (m.startDelayMs + 999) / 1000
        Toast.makeText(this, if (sec > 0) "${sec}초 뒤 시작합니다. 매크로를 돌릴 화면을 열어 두세요" else "시작합니다", Toast.LENGTH_LONG).show()
        moveTaskToBack(true)
    }

    private fun menu(anchor: TextView, m: Macro) {
        val pm = PopupMenu(this, anchor)
        pm.menu.add("이름 바꾸기"); pm.menu.add("복제"); pm.menu.add("위로"); pm.menu.add("아래로"); pm.menu.add("삭제")
        pm.setOnMenuItemClickListener {
            when (it.title) {
                "이름 바꾸기" -> rename(m)
                "복제" -> { store.put(m.copy(newName = m.name + " 복사")); refresh() }
                "위로" -> { store.move(m.id, -1); refresh() }
                "아래로" -> { store.move(m.id, 1); refresh() }
                "삭제" -> AlertDialog.Builder(this).setMessage("‘${m.name}’ 을 삭제할까요?")
                    .setPositiveButton("삭제") { _, _ -> store.delete(m.id); MacroService.instance?.panel?.update(); refresh() }
                    .setNegativeButton("취소", null).show()
            }
            true
        }
        pm.show()
    }

    private fun rename(m: Macro) {
        val input = edit("이름", m.name)
        AlertDialog.Builder(this).setTitle("이름 바꾸기").setView(wrap(input))
            .setPositiveButton("저장") { _, _ ->
                m.name = input.text.toString().trim().ifEmpty { m.name }; store.put(m); MacroService.instance?.panel?.update(); refresh()
            }.setNegativeButton("취소", null).show()
    }

    private fun newMacro() {
        val input = edit("이름", "매크로 ${store.loadAll().size + 1}")
        AlertDialog.Builder(this).setTitle("새 매크로").setView(wrap(input))
            .setPositiveButton("만들기") { _, _ ->
                val m = Macro(name = input.text.toString().trim().ifEmpty { "새 매크로" })
                store.put(m)
                if (store.selectedId == null) store.selectedId = m.id
                MacroService.instance?.panel?.update()
                openEditor(m.id)
            }.setNegativeButton("취소", null).show()
    }

    private fun wrap(v: android.view.View) = LinearLayout(this).apply { setPadding(dp(20), dp(8), dp(20), 0); addView(v) }

    private fun openEditor(id: String) {
        startActivity(Intent(this, EditorActivity::class.java).putExtra(EditorActivity.EXTRA_ID, id))
    }
}
