package com.kjooy.novelwiki

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

object ReminderScheduler {
    private const val PREFS = "novelwiki_reminders"
    const val KEY_REMINDERS = "reminders_json"
    const val KEY_WRITING = "writing_json"
    private const val WRITING_REQUEST_CODE = -99999

    fun saveAndScheduleAll(context: Context, json: String) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val oldJson = prefs.getString(KEY_REMINDERS, null)
        if (!oldJson.isNullOrEmpty()) cancelAll(context, oldJson)
        prefs.edit().putString(KEY_REMINDERS, json).apply()
        scheduleAll(context, json)
    }

    fun saveAndScheduleWriting(context: Context, json: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_WRITING, json).apply()
        cancelWriting(context)
        scheduleWriting(context, json)
    }

    fun restoreAfterBoot(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.getString(KEY_REMINDERS, null)?.let { scheduleAll(context, it) }
        prefs.getString(KEY_WRITING, null)?.let { scheduleWriting(context, it) }
    }

    fun scheduleAll(context: Context, json: String) {
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val r = arr.getJSONObject(i)
                if (r.optBoolean("enabled", false)) scheduleOne(context, r)
            }
        } catch (e: Exception) { /* ignore */ }
    }

    fun scheduleOne(context: Context, r: JSONObject) {
        val id = r.optString("id", "")
        val intervalMin = r.optInt("interval", 60)
        val triggerMs = nextTriggerMs(
            r.optString("startTime", "00:00"),
            r.optString("endTime", "23:59"),
            intervalMin
        )
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = "com.kjooy.novelwiki.REMINDER"
            putExtra("reminder_json", r.toString())
        }
        val pi = PendingIntent.getBroadcast(
            context, id.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        setExact(context, triggerMs, pi)
    }

    fun scheduleWriting(context: Context, json: String) {
        try {
            val s = JSONObject(json)
            if (!s.optBoolean("enabled", false)) return
            val intervalMin = s.optInt("interval", 60)
            val intent = Intent(context, ReminderReceiver::class.java).apply {
                action = "com.kjooy.novelwiki.WRITING_REMINDER"
                putExtra("writing_json", json)
            }
            val pi = PendingIntent.getBroadcast(
                context, WRITING_REQUEST_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            setExact(context, System.currentTimeMillis() + intervalMin * 60_000L, pi)
        } catch (e: Exception) { /* ignore */ }
    }

    fun cancelAll(context: Context, json: String) {
        try {
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val id = arr.getJSONObject(i).optString("id", "")
                val pi = PendingIntent.getBroadcast(
                    context, id.hashCode(),
                    Intent(context, ReminderReceiver::class.java),
                    PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                )
                if (pi != null) am.cancel(pi)
            }
        } catch (e: Exception) { /* ignore */ }
    }

    fun cancelWriting(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = PendingIntent.getBroadcast(
            context, WRITING_REQUEST_CODE,
            Intent(context, ReminderReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pi != null) am.cancel(pi)
    }

    fun nextTriggerMs(startTime: String, endTime: String, intervalMin: Int): Long {
        val cal = Calendar.getInstance()
        val nowMin = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        val (sh, sm) = parseParts(startTime)
        val (eh, em) = parseParts(endTime)
        val startMin = sh * 60 + sm
        val endMin = eh * 60 + em

        return if (startMin <= endMin) {
            // 일반 범위 (예: 10:00~15:00)
            when {
                nowMin < startMin -> calAtTime(sh, sm).timeInMillis
                nowMin < endMin   -> System.currentTimeMillis() + intervalMin * 60_000L
                else              -> calAtTime(sh, sm, daysOffset = 1).timeInMillis
            }
        } else {
            // 야간 범위 (예: 22:00~04:00, startMin > endMin)
            if (nowMin >= startMin || nowMin < endMin) {
                // 현재 범위 안 → interval 후
                System.currentTimeMillis() + intervalMin * 60_000L
            } else {
                // 범위 밖 (04:00~22:00) → 오늘 시작 시각
                calAtTime(sh, sm).timeInMillis
            }
        }
    }

    private fun parseParts(time: String): Pair<Int, Int> {
        val parts = time.split(":")
        return Pair(parts.getOrNull(0)?.toIntOrNull() ?: 0, parts.getOrNull(1)?.toIntOrNull() ?: 0)
    }

    private fun calAtTime(h: Int, m: Int, daysOffset: Int = 0): Calendar =
        Calendar.getInstance().apply {
            if (daysOffset > 0) add(Calendar.DAY_OF_YEAR, daysOffset)
            set(Calendar.HOUR_OF_DAY, h)
            set(Calendar.MINUTE, m)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

    private fun setExact(context: Context, triggerMs: Long, pi: PendingIntent) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (am.canScheduleExactAlarms()) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMs, pi)
            } else {
                am.setWindow(AlarmManager.RTC_WAKEUP, triggerMs, 10 * 60_000L, pi)
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMs, pi)
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, triggerMs, pi)
        }
    }
}
