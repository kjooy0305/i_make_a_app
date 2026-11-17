package com.kjooy0305.adskip

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.accessibility.AccessibilityManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

/** 흑백 한 화면: 상태 · 켜기 · 적용 대상 · 건너뛴 횟수 · 최근 기록 · 원리. */
class MainActivity : Activity() {

    private lateinit var prefs: Prefs
    private lateinit var statusText: TextView
    private lateinit var statusHint: TextView
    private lateinit var enableBtn: Button
    private lateinit var countText: TextView
    private lateinit var logText: TextView

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) = refresh()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs(this)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(24), dp(20), dp(32))
        }

        root.addView(title("광고 건너뛰기"))
        root.addView(body("유튜브에 '광고 건너뛰기' 버튼이 뜨면 대신 눌러 줍니다. 앱이 꺼져 있어도 접근성 서비스가 켜져 있으면 동작합니다."))

        // 1. 상태 + 켜기
        root.addView(section("1. 서비스 상태"))
        statusText = big("확인 중…"); root.addView(statusText)
        statusHint = body(""); root.addView(statusHint)
        enableBtn = button("접근성 설정 열기") {
            try {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                Toast.makeText(this, "목록에서 '광고 건너뛰기 (자동 클릭)'를 찾아 켜 주세요", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this, "설정 화면을 열 수 없습니다", Toast.LENGTH_SHORT).show()
            }
        }
        root.addView(enableBtn)
        if (Build.VERSION.SDK_INT >= 33) {
            root.addView(small("안드로이드 13 이상에서 '제한된 설정'이라고 막히면: 설정 › 애플리케이션 › 광고 건너뛰기 › 오른쪽 위 ⋮ › 제한된 설정 허용 을 누른 뒤 다시 켭니다."))
            root.addView(button("이 앱의 앱 정보 열기") {
                startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName")))
            })
        }

        // 2. 적용 대상
        root.addView(section("2. 어디서 건너뛸까"))
        val ytBox = check("유튜브 앱 (항상)", true) { }
        ytBox.isEnabled = false
        root.addView(ytBox)
        root.addView(check("브라우저에서 보는 유튜브 (크롬·삼성 인터넷·엣지·파이어폭스·웨일·네이버)", prefs.browsers) { prefs.browsers = it })
        root.addView(small("브라우저는 버튼 글자만 보고 찾기 때문에 유튜브 앱보다 느릴 수 있습니다. 컴퓨터용 확장 프로그램은 광고 영상 자체를 끝으로 돌려 더 빠릅니다."))
        root.addView(check("건너뛸 때 작게 알려 주기", prefs.toast) { prefs.toast = it })

        // 3. 통계
        root.addView(section("3. 건너뛴 광고"))
        countText = big("0회"); root.addView(countText)
        logText = small(""); root.addView(logText)
        root.addView(button("기록 지우기") { prefs.resetStats(); refresh() })

        // 4. 원리
        root.addView(section("4. 원리"))
        root.addView(body(
            "① 안드로이드 접근성 서비스는 화면에 보이는 버튼·글자를 '트리'로 읽을 수 있습니다.\n" +
            "② 유튜브 화면이 바뀔 때마다 그 트리에서 아이디가 skip_ad_button 이거나 글자가 '광고 건너뛰기'인 버튼을 찾습니다.\n" +
            "③ 찾으면 클릭 동작(ACTION_CLICK)을 보내고, 거절되면 버튼 좌표를 손가락처럼 탭합니다.\n" +
            "화면을 읽기만 하고 어디에도 보내지 않습니다. 인터넷 권한도 없습니다."
        ))
        root.addView(button("유튜브 열기") {
            val i = packageManager.getLaunchIntentForPackage(Prefs.YT_PKG)
            if (i != null) startActivity(i) else Toast.makeText(this, "유튜브 앱이 설치되어 있지 않습니다", Toast.LENGTH_SHORT).show()
        })
        root.addView(small("버전 ${BuildConfig.VERSION_NAME} · 컴퓨터용 확장 프로그램과 설명: kjooy0305.github.io/i_make_a_app/adskip/"))

        setContentView(ScrollView(this).apply { addView(root) })
    }

    override fun onResume() {
        super.onResume()
        val f = IntentFilter(SkipService.ACTION_UPDATED)
        if (Build.VERSION.SDK_INT >= 33) registerReceiver(receiver, f, Context.RECEIVER_NOT_EXPORTED) else registerReceiver(receiver, f)
        refresh()
    }

    override fun onPause() {
        super.onPause()
        try { unregisterReceiver(receiver) } catch (_: Exception) {}
    }

    private fun refresh() {
        val on = isServiceEnabled()
        statusText.text = if (on) "● 켜짐" else "○ 꺼짐"
        statusHint.text = if (on) "유튜브를 열어 두면 광고가 자동으로 넘어갑니다."
        else "아래 버튼으로 접근성 설정을 열고 '광고 건너뛰기 (자동 클릭)'를 켜 주세요."
        enableBtn.text = if (on) "접근성 설정 열기 (끄기)" else "접근성 설정 열기 (켜기)"
        countText.text = "${prefs.count}회"
        val log = prefs.log.asReversed()
        logText.text = if (log.isEmpty()) "아직 기록이 없습니다." else log.take(10).joinToString("\n") { it.replace("|", "  ") }
    }

    /** 시스템에 등록된 '켜진 접근성 서비스' 목록에 이 앱 서비스가 있는지. */
    private fun isServiceEnabled(): Boolean {
        val am = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val list = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        return list.any { it.resolveInfo?.serviceInfo?.packageName == packageName } || SkipService.running
    }

    // ---- 작은 뷰 도우미 (흑백) ----
    private fun dp(v: Int) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), resources.displayMetrics).toInt()

    private fun title(s: String) = TextView(this).apply {
        text = s; textSize = 26f; setTypeface(null, Typeface.BOLD); setTextColor(Color.BLACK)
        setPadding(0, 0, 0, dp(8))
    }
    private fun section(s: String) = TextView(this).apply {
        text = s; textSize = 17f; setTypeface(null, Typeface.BOLD); setTextColor(Color.BLACK)
        setPadding(0, dp(24), 0, dp(6))
    }
    private fun body(s: String) = TextView(this).apply {
        text = s; textSize = 15f; setTextColor(Color.BLACK); setLineSpacing(0f, 1.25f)
        setPadding(0, dp(2), 0, dp(6))
    }
    private fun small(s: String) = TextView(this).apply {
        text = s; textSize = 13f; setTextColor(Color.DKGRAY); setLineSpacing(0f, 1.2f)
        setPadding(0, dp(2), 0, dp(6))
    }
    private fun big(s: String) = TextView(this).apply {
        text = s; textSize = 30f; setTypeface(null, Typeface.BOLD); setTextColor(Color.BLACK)
        setPadding(0, dp(4), 0, dp(4))
    }
    private fun button(label: String, onClick: () -> Unit) = Button(this).apply {
        text = label; isAllCaps = false; textSize = 15f
        setTextColor(Color.WHITE); setBackgroundColor(Color.BLACK)
        gravity = Gravity.CENTER
        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(48)).apply { topMargin = dp(8); bottomMargin = dp(4) }
        setOnClickListener { onClick() }
    }
    private fun check(label: String, checked: Boolean, onChange: (Boolean) -> Unit) = CheckBox(this).apply {
        text = label; isChecked = checked; textSize = 15f; setTextColor(Color.BLACK)
        setPadding(dp(4), dp(6), 0, dp(6))
        setOnCheckedChangeListener { _, v -> onChange(v) }
    }
}
