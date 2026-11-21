package com.kjooy0305.screenkeep

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.provider.Settings
import android.text.InputType
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast

/** 한 화면짜리 앱: 켜기/끄기, 유지 시간, 기기 기본 꺼짐 시간, 준비 상태, 꺼짐 기록. */
class MainActivity : Activity() {
    private lateinit var box: LinearLayout
    private var statusView: TextView? = null
    private val handler = Handler(Looper.getMainLooper())
    private val ticker = object : Runnable {
        override fun run() { refreshStatusText(); handler.postDelayed(this, 15_000) }
    }

    private val pkg get() = Uri.parse("package:$packageName")
    private fun dp(v: Int) = (v * resources.displayMetrics.density + 0.5f).toInt()

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val sv = ScrollView(this).apply { isFillViewport = true; setBackgroundColor(Color.WHITE) }
        box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(18), dp(22), dp(18), dp(40)) }
        sv.addView(box, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        sv.setOnApplyWindowInsetsListener { v, insets ->
            v.setPadding(0, insets.systemWindowInsetTop, 0, insets.systemWindowInsetBottom); insets
        }
        setContentView(sv)
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }
    }

    override fun onResume() {
        super.onResume()
        // 프로세스가 죽었는데 켜짐 상태로 남아 있으면 다시 켠다
        if (Prefs.enabled(this) && !KeepService.alive && Settings.canDrawOverlays(this)) {
            try { KeepService.start(this) } catch (e: Exception) { }
        }
        render()
        handler.postDelayed(ticker, 15_000)
    }

    override fun onPause() { handler.removeCallbacks(ticker); super.onPause() }

    override fun onRequestPermissionsResult(c: Int, p: Array<out String>, r: IntArray) { render() }

    // ---------- 그리기 ----------
    private fun render() {
        val on = Prefs.enabled(this)
        // 켜 둔 동안에는 이 앱 화면도 꺼지지 않게(권한이 없어도 최소한 이 화면에서는 동작)
        if (on) window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) else window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        box.removeAllViews()
        box.addView(text("화면 유지", 26f, bold = true))
        box.addView(text("전원 버튼을 누르기 전에는 화면이 꺼지지 않게 해요.", 13f).apply { setPadding(0, dp(2), 0, dp(14)) })

        // 상태 카드 + 켜기/끄기
        val card = column().apply { background = shape(Color.WHITE, 2f); setPadding(dp(16), dp(16), dp(16), dp(16)) }
        card.addView(text(if (on) "화면 유지 중" else "꺼져 있음", 22f, bold = true))
        statusView = text("", 13f).apply { setPadding(0, dp(4), 0, dp(12)) }
        card.addView(statusView)
        val main = button(if (on) "끄기" else "켜기", filled = !on).apply {
            setOnClickListener { if (on) { KeepService.stop(this@MainActivity); render() } else turnOn() }
        }
        card.addView(main, lpMatch())
        box.addView(card, lpMatch(top = 0))
        refreshStatusText()

        // 유지 시간
        box.addView(section("유지 시간", "정한 시간이 지나면 자동으로 끄고, 그 뒤에는 기기 기본 꺼짐 시간을 따라요."))
        val cur = Prefs.durationMin(this)
        val chips = wrap()
        Logic.DURATIONS.forEach { m -> chips.addView(chip(Logic.durationLabel(m), cur == m) { setDuration(m) }) }
        val custom = cur !in Logic.DURATIONS
        chips.addView(chip(if (custom) "직접 (" + Logic.durationLabel(cur) + ")" else "직접", custom) { askCustom() })
        box.addView(chips)

        // 기기 기본 꺼짐 시간
        box.addView(section("기기 기본 화면 꺼짐 시간", "화면 유지를 껐을 때 기기가 따르는 시간이에요."))
        val sys = try { Settings.System.getInt(contentResolver, Settings.System.SCREEN_OFF_TIMEOUT) } catch (e: Exception) { -1 }
        box.addView(text("지금: " + if (sys > 0) Logic.timeoutLabel(sys) else "알 수 없음", 13f).apply { setPadding(0, 0, 0, dp(6)) })
        val tchips = wrap()
        Logic.TIMEOUTS.forEach { ms -> tchips.addView(chip(Logic.timeoutLabel(ms), sys == ms) { setSystemTimeout(ms) }) }
        box.addView(tchips)
        if (!Settings.System.canWrite(this)) {
            box.addView(button("설정 변경 허용 (이 시간을 바꾸려면 필요해요)", filled = false).apply {
                setOnClickListener { open(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, pkg)) }
            }, lpMatch(top = 8))
        }

        // 준비 상태
        box.addView(section("준비 상태", "아래가 모두 켜져 있어야 화면이 안정적으로 유지돼요."))
        box.addView(permRow("다른 앱 위에 표시", "필수 · 다른 앱을 쓰는 동안에도 유지", Settings.canDrawOverlays(this)) { open(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, pkg)) })
        if (Build.VERSION.SDK_INT >= 33) {
            val ok = checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            box.addView(permRow("알림 허용", "유지 중이라는 알림과 '끄기' 버튼", ok) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) || true) open(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, packageName))
            })
        }
        val ign = (getSystemService(Context.POWER_SERVICE) as PowerManager).isIgnoringBatteryOptimizations(packageName)
        box.addView(permRow("배터리 최적화 제외", "권장 · 기기가 이 앱을 몰래 끄는 것을 막아요", ign) {
            try { startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, pkg)) } catch (e: Exception) { open(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)) }
        })
        val sw = Switch(this).apply {
            text = "휴대폰을 켜면 자동으로 시작"; textSize = 14f; isChecked = Prefs.autoStart(this@MainActivity)
            setOnCheckedChangeListener { _, v -> Prefs.setAutoStart(this@MainActivity, v) }
            setPadding(0, dp(10), 0, dp(4))
        }
        box.addView(sw)

        // 꺼짐 기록
        box.addView(section("화면 꺼짐 기록", "화면이 꺼지거나 켜질 때마다 그때의 상태를 남겨요."))
        val logs = Prefs.logs(this).reversed().take(25)
        if (logs.isEmpty()) box.addView(text("아직 기록이 없어요.", 13f))
        logs.forEach { e ->
            box.addView(text(e.line(), 12f, bold = e.unexpected).apply {
                typeface = if (e.unexpected) Typeface.DEFAULT_BOLD else Typeface.MONOSPACE
                setPadding(0, dp(3), 0, dp(3))
            })
        }
        if (logs.isNotEmpty()) {
            box.addView(button("기록 지우기", filled = false).apply { setOnClickListener { Prefs.clearLogs(this@MainActivity); render() } }, lpMatch(top = 8))
        }
        box.addView(text("굵게 보이는 '화면 꺼짐'은 유지 중에 꺼진 경우예요.\n전원 버튼을 누른 게 아니라면, 바로 앞뒤에 '서비스가 종료됨' 기록이 있는지 보세요. 있다면 기기가 이 앱을 정리한 거예요. 그럴 땐 '배터리 최적화 제외'를 켜고, 삼성 기기는 설정 > 배터리 > 백그라운드 사용 제한에서 이 앱을 제한하지 않게 하세요.", 12f).apply { setPadding(0, dp(12), 0, 0) })
    }

    private fun refreshStatusText() {
        val on = Prefs.enabled(this)
        statusView?.text = if (on) Logic.remainingText(Prefs.endAt(this), System.currentTimeMillis()) else "켜면 전원 버튼을 누를 때까지 화면이 켜져 있어요"
    }

    // ---------- 동작 ----------
    private fun turnOn() {
        if (!Settings.canDrawOverlays(this)) {
            AlertDialog.Builder(this).setTitle("권한이 필요해요")
                .setMessage("다른 앱을 쓰는 동안에도 화면을 켜 두려면 '다른 앱 위에 표시'를 허용해야 해요. 화면에는 아무것도 보이지 않아요.")
                .setNegativeButton("취소", null)
                .setPositiveButton("허용하러 가기") { _, _ -> open(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, pkg)) }.show()
            return
        }
        try { KeepService.start(this) } catch (e: Exception) { Toast.makeText(this, "켜지 못했어요", Toast.LENGTH_SHORT).show() }
        handler.postDelayed({ render() }, 300)
    }

    private fun setDuration(min: Int) {
        Prefs.setDurationMin(this, min)
        if (Prefs.enabled(this) && Settings.canDrawOverlays(this)) { try { KeepService.start(this) } catch (e: Exception) { } }
        handler.postDelayed({ render() }, 200)
    }

    private fun askCustom() {
        val et = EditText(this).apply { inputType = InputType.TYPE_CLASS_NUMBER; hint = "분 (예: 90)"; setPadding(dp(20), dp(12), dp(20), dp(12)) }
        AlertDialog.Builder(this).setTitle("유지 시간 직접 입력").setView(et).setNegativeButton("취소", null)
            .setPositiveButton("확인") { _, _ ->
                val v = et.text.toString().toIntOrNull()
                if (v == null || v < 1 || v > 24 * 60) Toast.makeText(this, "1분부터 24시간(1440분)까지 입력하세요", Toast.LENGTH_SHORT).show() else setDuration(v)
            }.show()
    }

    private fun setSystemTimeout(ms: Int) {
        if (!Settings.System.canWrite(this)) { open(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, pkg)); return }
        try { Settings.System.putInt(contentResolver, Settings.System.SCREEN_OFF_TIMEOUT, ms); Toast.makeText(this, "기본 꺼짐 시간 " + Logic.timeoutLabel(ms), Toast.LENGTH_SHORT).show() }
        catch (e: Exception) { Toast.makeText(this, "바꾸지 못했어요", Toast.LENGTH_SHORT).show() }
        render()
    }

    private fun open(i: Intent) { try { startActivity(i) } catch (e: Exception) { Toast.makeText(this, "설정 화면을 열 수 없어요", Toast.LENGTH_SHORT).show() } }

    // ---------- 작은 부품 ----------
    private fun shape(fill: Int, strokeDp: Float, radiusDp: Float = 14f) = GradientDrawable().apply {
        setColor(fill); setStroke(dp(1).coerceAtLeast((strokeDp * resources.displayMetrics.density).toInt()), Color.BLACK); cornerRadius = dp(radiusDp.toInt()).toFloat()
    }
    private fun column() = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
    private fun wrap() = WrapLayout(this)
    private fun lpMatch(top: Int = 0) = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(top) }

    private fun text(s: String, sp: Float, bold: Boolean = false) = TextView(this).apply {
        text = s; setTextSize(TypedValue.COMPLEX_UNIT_SP, sp); setTextColor(Color.BLACK); if (bold) typeface = Typeface.DEFAULT_BOLD
    }

    private fun section(title: String, sub: String) = column().apply {
        setPadding(0, dp(22), 0, dp(8))
        addView(text(title, 16f, bold = true))
        addView(text(sub, 12f))
    }

    private fun button(label: String, filled: Boolean) = TextView(this).apply {
        text = label; gravity = Gravity.CENTER; setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f); typeface = Typeface.DEFAULT_BOLD
        setTextColor(if (filled) Color.WHITE else Color.BLACK); background = shape(if (filled) Color.BLACK else Color.WHITE, 1.5f, 12f)
        setPadding(dp(14), dp(13), dp(14), dp(13)); isClickable = true
    }

    private fun chip(label: String, selected: Boolean, onClick: () -> Unit) = TextView(this).apply {
        text = label; setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f); gravity = Gravity.CENTER
        setTextColor(if (selected) Color.WHITE else Color.BLACK); if (selected) typeface = Typeface.DEFAULT_BOLD
        background = shape(if (selected) Color.BLACK else Color.WHITE, 1.5f, 20f)
        setPadding(dp(14), dp(8), dp(14), dp(8)); isClickable = true; setOnClickListener { onClick() }
    }

    private fun permRow(title: String, sub: String, ok: Boolean, onClick: () -> Unit) = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(0, dp(8), 0, dp(8))
        val t = column().apply { addView(text(title, 15f, bold = true)); addView(text(sub, 12f)) }
        addView(t, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        addView(TextView(this@MainActivity).apply {
            text = if (ok) "✓ 켜짐" else "설정"; setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f); typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER; setTextColor(if (ok) Color.BLACK else Color.WHITE)
            background = shape(if (ok) Color.WHITE else Color.BLACK, 1.5f, 10f); setPadding(dp(12), dp(8), dp(12), dp(8))
            isClickable = !ok; if (!ok) setOnClickListener { onClick() }
        })
    }
}

/** 칩을 가로로 늘어놓다가 넘치면 아랫줄로 내리는 간단한 배치. */
class WrapLayout(c: Context) : ViewGroup(c) {
    private val gap = (8 * c.resources.displayMetrics.density).toInt()

    override fun onMeasure(wSpec: Int, hSpec: Int) {
        val maxW = MeasureSpec.getSize(wSpec)
        var x = 0; var y = 0; var rowH = 0
        for (i in 0 until childCount) {
            val v = getChildAt(i); v.measure(MeasureSpec.makeMeasureSpec(maxW, MeasureSpec.AT_MOST), MeasureSpec.UNSPECIFIED)
            if (x > 0 && x + v.measuredWidth > maxW) { x = 0; y += rowH + gap; rowH = 0 }
            x += v.measuredWidth + gap; rowH = maxOf(rowH, v.measuredHeight)
        }
        setMeasuredDimension(maxW, y + rowH)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val maxW = r - l
        var x = 0; var y = 0; var rowH = 0
        for (i in 0 until childCount) {
            val v = getChildAt(i)
            if (x > 0 && x + v.measuredWidth > maxW) { x = 0; y += rowH + gap; rowH = 0 }
            v.layout(x, y, x + v.measuredWidth, y + v.measuredHeight)
            x += v.measuredWidth + gap; rowH = maxOf(rowH, v.measuredHeight)
        }
    }
}
