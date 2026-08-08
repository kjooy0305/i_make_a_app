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
        ensureChannel(context)
        when (intent.action) {
            "com.kjooy.novelwiki.REMINDER" ->
                intent.getStringExtra("reminder_json")?.let { handleCustom(context, it) }
            "com.kjooy.novelwiki.WRITING_REMINDER" ->
                intent.getStringExtra("writing_json")?.let { handleWriting(context, it) }
        }
    }

    private fun handleCustom(context: Context, json: String) {
        try {
            val r = JSONObject(json)
            if (!r.optBoolean("enabled", false)) return

            val cal = Calendar.getInstance()
            val todayLabel = arrayOf("일","월","화","수","목","금","토")[cal.get(Calendar.DAY_OF_WEEK) - 1]
            val days = r.optJSONArray("days")
            val dayOk = days == null || days.length() == 0 ||
                (0 until days.length()).any { days.getString(it) == todayLabel }

            val nowMin = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
            val (sh, sm) = parseParts(r.optString("startTime", "00:00"))
            val (eh, em) = parseParts(r.optString("endTime", "23:59"))
            val startMin = sh * 60 + sm
            val endMin = eh * 60 + em
            val inRange = if (startMin <= endMin) {
                nowMin >= startMin && nowMin < endMin
            } else {
                // 야간 범위 (예: 22:00~04:00)
                nowMin >= startMin || nowMin < endMin
            }

            if (dayOk && inRange) {
                notify(context, r.optString("message", "알림"), r.optString("id", "").hashCode())
            }

            ReminderScheduler.scheduleOne(context, r)
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
                val (gh, gm) = parseParts(s.optString("gentleStart", "18:00"))
                val (uh, um) = parseParts(s.optString("urgentStart", "21:00"))
                val gentleMin = gh * 60 + gm
                val urgentMin = uh * 60 + um

                val msg = when {
                    nowMin >= urgentMin -> "⚠️ 오늘 아직 글을 쓰지 않으셨습니다!"
                    nowMin >= gentleMin -> "📝 오늘 글쓰기 시간이에요!"
                    else -> null
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
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(id, notif)
    }

    private fun ensureChannel(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.getNotificationChannel(CHANNEL_ID) == null) {
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "소설 창작위키 알림", NotificationManager.IMPORTANCE_DEFAULT)
            )
        }
    }

    private fun parseParts(time: String): Pair<Int, Int> {
        val parts = time.split(":")
        return Pair(parts.getOrNull(0)?.toIntOrNull() ?: 0, parts.getOrNull(1)?.toIntOrNull() ?: 0)
    }

    companion object {
        const val CHANNEL_ID = "novelwiki_reminders"
    }
}
