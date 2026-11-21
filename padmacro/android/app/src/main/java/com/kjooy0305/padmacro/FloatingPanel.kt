package com.kjooy0305.padmacro

import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.math.abs

/**
 * 어느 앱 위에서나 떠 있는 작은 조작 패널: [▶/■] [● 녹화] 매크로 이름·진행 상태 [✕].
 * 손가락으로 끌어 옮길 수 있고 위치는 저장된다. 접근성 오버레이라 별도 권한이 없어도 뜬다.
 */
class FloatingPanel(private val service: MacroService) {
    private val wm = service.getSystemService(WindowManager::class.java)
    private val store = service.store
    private val dens = service.resources.displayMetrics.density
    private fun dp(v: Int) = (v * dens).toInt()

    private val root: LinearLayout
    private val playBtn: TextView
    private val recBtn: TextView
    private val label: TextView
    private var shown = false

    private val lp = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
        PixelFormat.TRANSLUCENT,
    ).apply {
        gravity = Gravity.TOP or Gravity.START
        val sx = store.panelX; val sy = store.panelY
        x = if (sx >= 0) sx else dp(12)
        y = if (sy >= 0) sy else dp(120)
    }

    init {
        root = LinearLayout(service).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = GradientDrawable().apply { setColor(0xE6111111.toInt()); cornerRadius = dp(22).toFloat() }
            setPadding(dp(6), dp(4), dp(6), dp(4))
            elevation = dp(6).toFloat()
        }
        playBtn = circle("▶", 0xFF2ECC71.toInt()) { if (MacroService.isRunning()) service.stopMacro() else service.toggleSelected() }
        recBtn = circle("●", 0xFFE74C3C.toInt()) {
            val m = store.selected()
            if (m == null) service.toast("매크로가 없습니다. 앱에서 먼저 만들어 주세요") else service.startRecord(m.id)
        }
        label = TextView(service).apply {
            setTextColor(Color.WHITE); textSize = 12f; typeface = Typeface.DEFAULT_BOLD
            maxWidth = dp(150); maxLines = 2
            setPadding(dp(6), 0, dp(6), 0)
        }
        val close = TextView(service).apply {
            text = "✕"; setTextColor(0xFFBBBBBB.toInt()); textSize = 14f
            setPadding(dp(6), dp(4), dp(8), dp(4))
            setOnClickListener { store.showPanel = false; service.refreshPanel(); service.toast("패널을 숨겼습니다. 앱 설정에서 다시 켤 수 있어요") }
        }
        root.addView(playBtn); root.addView(recBtn); root.addView(label); root.addView(close)

        // 이름 부분을 끌면 이동, 살짝 누르면 앱 열기
        var downX = 0f; var downY = 0f; var startX = 0; var startY = 0; var moved = false
        label.setOnTouchListener { _, e ->
            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN -> { downX = e.rawX; downY = e.rawY; startX = lp.x; startY = lp.y; moved = false; true }
                MotionEvent.ACTION_MOVE -> {
                    val dx = e.rawX - downX; val dy = e.rawY - downY
                    if (abs(dx) > dp(6) || abs(dy) > dp(6)) moved = true
                    if (moved) { lp.x = (startX + dx).toInt().coerceAtLeast(0); lp.y = (startY + dy).toInt().coerceAtLeast(0); if (shown) wm.updateViewLayout(root, lp) }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (moved) { store.panelX = lp.x; store.panelY = lp.y } else service.openMain()
                    true
                }
                else -> false
            }
        }
    }

    private fun circle(text: String, color: Int, onClick: () -> Unit) = TextView(service).apply {
        this.text = text; setTextColor(Color.WHITE); textSize = 15f; gravity = Gravity.CENTER
        typeface = Typeface.DEFAULT_BOLD
        layoutParams = LinearLayout.LayoutParams(dp(36), dp(36)).apply { marginEnd = dp(4) }
        background = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(color) }
        setOnClickListener { onClick() }
    }

    fun show() {
        if (shown) return
        try { wm.addView(root, lp); shown = true } catch (_: Exception) {}
        update()
    }

    fun hide() {
        if (!shown) return
        try { wm.removeView(root) } catch (_: Exception) {}
        shown = false
    }

    fun remove() = hide()

    /** 실행 상태와 선택된 매크로 이름으로 패널 글자를 새로 그린다 */
    fun update() {
        val running = MacroService.isRunning()
        playBtn.text = if (running) "■" else "▶"
        (playBtn.background as GradientDrawable).setColor(if (running) 0xFFE67E22.toInt() else 0xFF2ECC71.toInt())
        recBtn.visibility = if (running) View.GONE else View.VISIBLE
        if (!running) label.text = store.selected()?.name ?: "매크로 없음"
    }

    fun setStatus(text: String) {
        val name = service.currentMacro?.name ?: ""
        label.text = if (name.isEmpty()) text else "$name\n$text"
    }
}
