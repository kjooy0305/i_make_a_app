package com.kjooy0305.dayclean.ui

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import com.kjooy0305.dayclean.logic.Dates
import com.kjooy0305.dayclean.logic.Ops
import com.kjooy0305.dayclean.model.CleanupTime
import com.kjooy0305.dayclean.model.CleanupType
import com.kjooy0305.dayclean.model.RestInterval

/** 설정: 글씨 크기, 장기 목표 끄기, 정리 시간, 알림 쉬는 시간, 알림 권한. */
class SettingsActivity : BaseActivity() {
    private lateinit var box: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        box = vbox()
        show(page("설정", true, null, scrollOf(box)))
    }

    override fun onResume() {
        super.onResume()
        redraw()
    }

    private fun redraw() {
        val s = state.settings
        box.removeAllViews()

        // ---- 화면 ----
        box.add(sectionTitle("화면"))
        box.add(label("글씨 크기", 14f, bold = true), t = 10, b = 6)
        val scales = listOf(0.9f, 1f, 1.15f, 1.3f)
        val current = scales.indexOfFirst { kotlin.math.abs(it - s.fontScale) < 0.01f }.coerceAtLeast(0)
        box.add(Seg(this, listOf("작게", "보통", "크게", "아주 크게"), current) { i ->
            mutate { Ops.setSettings(it) { st -> st.copy(fontScale = scales[i]) } }
            recreate()
        })
        box.add(label("가나다라마바사 · 보고서 초안 보내기 · 07:00", 14f).apply {
            background = shape(strokeDp = 1.5f, radiusDp = 10f)
            setPadding(dp(12), dp(8), dp(12), dp(8))
        }, MATCH, WRAP, 0f, 0, 8, 0, 0)

        // ---- 장기 목표 ----
        box.add(sectionTitle("장기 목표"))
        switchRow("장기 목표 정리 사용", "끄면 목표는 계속 쓸 수 있지만 점검 창과 점검 알림이 멈춰요", s.goalsOn, true) { v ->
            mutate { Ops.setSettings(it) { st -> st.copy(goalsOn = v) } }; redraw()
        }
        switchRow("1주 정리", if (s.goalsOn) "아침 정리 때마다" else "전체가 꺼져 있어요", s.goalsOn && s.w1On, s.goalsOn) { v ->
            mutate { Ops.setSettings(it) { st -> st.copy(w1On = v) } }
        }
        switchRow("1달 정리", if (s.goalsOn) "7일마다 · 점검 알림도 함께 켜고 꺼요" else "전체가 꺼져 있어요", s.goalsOn && s.m1On, s.goalsOn) { v ->
            mutate { Ops.setSettings(it) { st -> st.copy(m1On = v) } }
        }
        switchRow("1년 정리", if (s.goalsOn) "매달 1일 · 점검 알림도 함께 켜고 꺼요" else "전체가 꺼져 있어요", s.goalsOn && s.y1On, s.goalsOn) { v ->
            mutate { Ops.setSettings(it) { st -> st.copy(y1On = v) } }
        }
        switchRow("10일 남은 목표를 기록장에 자동 추가", "정리 설정과 따로 켜고 꺼요", s.autoAdd, true) { v ->
            mutate { Ops.setSettings(it) { st -> st.copy(autoAdd = v) } }
        }

        // ---- 정리 시간 ----
        box.add(sectionTitle("정리 시간"))
        s.cleanups.sortedBy { it.minuteOfDay }.forEach { c -> cleanupRow(c) }
        box.add(button("+ 정리 시간 추가") { cleanupDialog(null) }, MATCH, WRAP, 0f, 0, 10, 0, 0)

        box.add(sectionTitle("‘-’ 알림"))
        val def = s.cleanup(s.defaultLaterStartId)
        row("기본 알림 시작 시간", "‘-’를 쓸 때 따로 안 고르면 이 정리 시간부터", button("${def?.name ?: "-"} ▾", small = true) {
            val list = s.cleanups.sortedBy { it.minuteOfDay }
            Dialogs.pick(this, "기본 알림 시작 시간", list.map { Dialogs.cleanupLabel(it) }, list.indexOfFirst { it.id == s.defaultLaterStartId }) { i ->
                mutate { Ops.setSettings(it) { st -> st.copy(defaultLaterStartId = list[i].id) } }; redraw()
            }
        })

        // ---- 알림 쉬는 시간 ----
        box.add(sectionTitle("알림 쉬는 시간"))
        box.add(label("쉬는 시간에는 10분마다 오는 반복 알림이 쉬고, 끝나면 남은 항목을 한 번 알려요. 아침·저녁 정리와 회고 알림은 그대로 울려요.", 12f), t = 6)
        s.rests.forEach { r -> restRow(r) }
        box.add(button("+ 쉬는 시간 구간 추가") { restDialog(null) }, MATCH, WRAP, 0f, 0, 10, 0, 0)

        // ---- 알림이 꺼진 앱에서도 울리려면 ----
        box.add(sectionTitle("알림이 꺼진 앱에서도 울리려면"))
        val nm = androidx.core.app.NotificationManagerCompat.from(this)
        row("알림 권한", if (nm.areNotificationsEnabled()) "허용됨" else "꺼져 있어요. 눌러서 켜 주세요",
            button("설정 열기", small = true) { open(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, packageName)) })
        val exact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || (getSystemService(Context.ALARM_SERVICE) as AlarmManager).canScheduleExactAlarms()
        row("정확한 알람", if (exact) "허용됨" else "허용해야 정해진 시각에 울려요",
            button("설정 열기", small = true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) open(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:$packageName")))
            })
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        row("배터리 최적화 제외", if (pm.isIgnoringBatteryOptimizations(packageName)) "제외됨" else "제외하면 앱이 꺼져 있어도 더 안정적으로 울려요",
            button("설정 열기", small = true) {
                open(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:$packageName")),
                    fallback = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
            })
        box.add(label("설정에서 앱을 ‘강제 종료’하면 다시 앱을 열 때까지는 알림이 울리지 않아요.", 12f), t = 8)
    }

    private fun open(intent: Intent, fallback: Intent? = null) {
        runCatching { startActivity(intent) }.onFailure {
            if (fallback != null) runCatching { startActivity(fallback) }
        }
    }

    private fun row(title: String, sub: String?, right: View) {
        val row = hbox().apply { minimumHeight = dp(52); setPadding(0, dp(6), 0, dp(6)) }
        val left = vbox()
        left.add(label(title, 15f, bold = true))
        if (sub != null) left.add(label(sub, 12f), t = 2)
        row.add(left, 0, WRAP, 1f, 0, 0, 8, 0)
        row.add(right)
        box.add(row)
        box.add(hline(1f))
    }

    private fun switchRow(title: String, sub: String, checked: Boolean, enabled: Boolean, onChange: (Boolean) -> Unit) {
        val sw = BwSwitch(this, checked) { onChange(it) }.apply { isEnabled = enabled; contentDescription = title }
        row(title, sub, sw)
    }

    private fun cleanupRow(c: CleanupTime) {
        val tag = if (c.builtIn) "기본" else when (c.type) {
            CleanupType.MORNING -> "아침형"; CleanupType.EVENING -> "저녁형"; CleanupType.NOTIFY -> "알림만"; CleanupType.REVIEW -> "회고"
        }
        val row = hbox().apply { minimumHeight = dp(56); setPadding(0, dp(6), 0, dp(6)) }
        val left = hbox()
        left.add(label(c.name, 15f, bold = true), WRAP, WRAP)
        left.add(label(tag, 11f, bold = true).apply {
            background = shape(strokeDp = 1f, radiusDp = 6f); setPadding(dp(6), dp(1), dp(6), dp(1))
        }, WRAP, WRAP, 0f, 8, 0, 0, 0)
        row.add(left, 0, WRAP, 1f)
        row.add(button(Dates.hhmm(c.minuteOfDay), small = true) { cleanupDialog(c) }.apply {
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD)
        })
        if (!c.builtIn) row.add(button("삭제", small = true) {
            Dialogs.confirm(this, "‘${c.name}’ 정리 시간을 지울까요?", "삭제") { mutate { Ops.removeCleanup(it, c.id) }; redraw() }
        }, WRAP, WRAP, 0f, 6, 0, 0, 0)
        box.add(row)
        box.add(hline(1f))
    }

    private fun cleanupDialog(existing: CleanupTime?) {
        val content = vbox()
        val name: EditText = textInput("이름 (예: 점심 점검)", existing?.name ?: "")
        val time = TimeField(this, existing?.minuteOfDay ?: 12 * 60 + 30)
        val types = listOf(CleanupType.MORNING, CleanupType.EVENING, CleanupType.NOTIFY)
        var type = existing?.type?.takeIf { it in types } ?: CleanupType.MORNING
        if (existing == null || !existing.builtIn) {
            content.add(label("이름", 12f), b = 2); content.add(name)
        }
        content.add(label("시간", 12f), t = 10, b = 2); content.add(time)
        if (existing == null || !existing.builtIn) {
            content.add(label("종류", 12f), t = 10, b = 4)
            content.add(Seg(this, listOf("아침형", "저녁형", "알림만"), types.indexOf(type)) { type = types[it] })
            content.add(label("아침형: 어제 넘긴 기록과 ‘-’ 항목을 확인하고 새로 추가해요.\n저녁형: 남은 기록 체크만 가능해요.\n알림만: 알림만 울려요.", 12f), t = 8)
        }
        val title = when { existing == null -> "정리 시간 추가"; existing.builtIn -> "${existing.name} 시간"; else -> "정리 시간 수정" }
        Dialogs.modal(this, title, content) { d ->
            val m = time.minute()
            val text = name.text.toString().trim()
            when {
                m == null -> { Dialogs.message(this, "시간은 00:00부터 23:59 사이로 적어 주세요."); false }
                (existing == null || !existing.builtIn) && text.isEmpty() -> { name.error = "이름을 적어 주세요"; false }
                else -> {
                    if (existing == null) mutate { Ops.addCleanup(it, text, m, type) }
                    else mutate { Ops.editCleanup(it, existing.id, text, m, type) }
                    redraw(); true
                }
            }
        }.show()
    }

    private fun restRow(r: RestInterval) {
        val row = vbox().apply { setPadding(0, dp(6), 0, dp(6)) }
        val top = hbox()
        top.add(button("${Dates.hhmm(r.startMin)} – ${Dates.hhmm(r.endMin)}", small = true) { restDialog(r) }.apply {
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD)
        })
        top.add(View(this), 0, 1, 1f)
        top.add(button("삭제", small = true) { mutate { Ops.removeRest(it, r.id) }; redraw() })
        row.add(top)
        val days = hbox()
        java.time.DayOfWeek.values().forEach { d ->
            val on = d in r.days
            days.add(label(Dates.dow(d), 12f, bold = on).apply {
                gravity = Gravity.CENTER
                background = shape(strokeDp = if (on) 3f else 1f, radiusDp = 15f)
            }, dp(30), dp(30), 0f, 0, 0, 4, 0)
        }
        row.add(days, t = 6)
        box.add(row)
        box.add(hline(1f))
    }

    private fun restDialog(existing: RestInterval?) {
        val content = vbox()
        val start = TimeField(this, existing?.startMin ?: 23 * 60)
        val end = TimeField(this, existing?.endMin ?: 7 * 60)
        val picker = Dialogs.DayPicker(this, existing?.days ?: java.time.DayOfWeek.values().toSet())
        val times = hbox()
        times.add(vbox().apply { add(label("시작", 12f), b = 2); add(start) }, 0, WRAP, 1f, 0, 0, 8, 0)
        times.add(vbox().apply { add(label("끝", 12f), b = 2); add(end) }, 0, WRAP, 1f)
        content.add(times)
        content.add(label("끝 시간이 시작보다 이르면 다음 날 아침까지 이어져요.", 12f), t = 6)
        content.add(label("요일", 12f), t = 10, b = 4)
        content.add(picker)
        Dialogs.modal(this, if (existing == null) "쉬는 시간 구간 추가" else "쉬는 시간 구간 수정", content) { _ ->
            val a = start.minute(); val b = end.minute()
            when {
                a == null || b == null -> { Dialogs.message(this, "시간은 00:00부터 23:59 사이로 적어 주세요."); false }
                a == b -> { Dialogs.message(this, "시작과 끝이 같아요. 다른 시간으로 정해 주세요."); false }
                picker.selected().isEmpty() -> { Dialogs.message(this, "요일을 하나 이상 골라 주세요."); false }
                else -> {
                    if (existing == null) mutate { Ops.addRest(it, a, b, picker.selected()) }
                    else mutate { Ops.updateRest(it, existing.id, a, b, picker.selected()) }
                    redraw(); true
                }
            }
        }.show()
    }
}
