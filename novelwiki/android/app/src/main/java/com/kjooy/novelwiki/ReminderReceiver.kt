package com.kjooy.novelwiki

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // goAsync로 BroadcastReceiver 실행 시간 연장 → 앱 종료 상태에서도 완전 실행 보장
        val result = goAsync()
        Thread {
            try {
                ensureChannel(context)
                when (intent.action) {
                    "com.kjooy.novelwiki.REMINDER" ->
                        intent.getStringExtra("reminder_json")?.let { handleCustom(context, it) }
                    "com.kjooy.novelwiki.WRITING_REMINDER" ->
                        intent.getStringExtra("writing_json")?.let { handleWriting(context, it) }
                }
            } finally {
                result.finish()
            }
        }.start()
    }

    private fun handleCustom(context: Context, json: String) {
        try {
            val r = JSONObject(json)
            if (!r.optBoolean("enabled", false)) return

            val cal = Calendar.getInstance()
            val nowMin = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)

            val startParts = r.optString("startTime", "00:00").split(":")
            val endParts   = r.optString("endTime", "23:59").split(":")
            val startMin = (startParts.getOrNull(0)?.toIntOrNull() ?: 0) * 60 +
                           (startParts.getOrNull(1)?.toIntOrNull() ?: 0)
            val endMin   = (endParts.getOrNull(0)?.toIntOrNull()   ?: 23) * 60 +
                           (endParts.getOrNull(1)?.toIntOrNull()   ?: 59)
            val overnight = startMin > endMin

            val inRange = ReminderScheduler.inRange(nowMin, startMin, endMin, overnight)

            val todayLabel = arrayOf("일","월","화","수","목","금","토")[cal.get(Calendar.DAY_OF_WEEK) - 1]
            val daysArr = r.optJSONArray("days")
            val dayOk = daysArr == null || daysArr.length() == 0 ||
                (0 until daysArr.length()).any { daysArr.getString(it) == todayLabel }

            if (inRange && dayOk) {
                notify(context, r.optString("message", "알림"), r.optString("id", "").hashCode())
            }

            // 요일/범위를 고려한 다음 알람 예약
            ReminderScheduler.scheduleOneFromReceiver(context, r, inRange, dayOk)
        } catch (e: Exception) { /* ignore */ }
    }

    private fun handleWriting(context: Context, json: String) {
        try {
            val s = JSONObject(json)
            if (!s.optBoolean("enabled", false)) return

            val prefs = context.getSharedPreferences("novelwiki_reminders", Context.MODE_PRIVATE)
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val writtenToday = prefs.getString("written_date", "") == today &&
                prefs.getBoolean("written_today", false)

            if (!writtenToday) {
                val cal = Calendar.getInstance()
                val nowMin = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
                val gentleParts = s.optString("gentleStart", "18:00").split(":")
                val urgentParts = s.optString("urgentStart", "21:00").split(":")
                val gentleMin = (gentleParts.getOrNull(0)?.toIntOrNull() ?: 18) * 60 +
                                (gentleParts.getOrNull(1)?.toIntOrNull() ?: 0)
                val urgentMin = (urgentParts.getOrNull(0)?.toIntOrNull() ?: 21) * 60 +
                                (urgentParts.getOrNull(1)?.toIntOrNull() ?: 0)

                val msg = when {
                    nowMin >= urgentMin -> "⚠️ 오늘 아직 글을 쓰지 않으셨습니다!"
                    nowMin >= gentleMin -> "📝 오늘 글쓰기 시간이에요!"
                    else                -> null
                }
                if (msg != null) notify(context, msg, "writing_reminder".hashCode())
            }

            ReminderScheduler.scheduleWriting(context, json)
        } catch (e: Exception) { /* ignore */ }
    }

    private fun notify(context: Context, body: String, id: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) return
        }
        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("소설 창작위키")
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)  // heads-up 알림
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(id, notif)
        } catch (e: SecurityException) { /* 권한 없을 때 */ }
    }

    private fun ensureChannel(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.getNotificationChannel(CHANNEL_ID) == null) {
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "소설 창작위키 알림",
                    NotificationManager.IMPORTANCE_HIGH).apply {
                    enableVibration(true)
                }
            )
        }
    }

    companion object {
        const val CHANNEL_ID = "novelwiki_reminders"
    }
}
