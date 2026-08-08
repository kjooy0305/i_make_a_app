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
    private val DAY_LABELS = arrayOf("일", "월", "화", "수", "목", "금", "토")

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

    /** 최초 예약 (앱 실행 시 or 저장 시). */
    fun scheduleOne(context: Context, r: JSONObject) {
        val (sh, sm, eh, em, startMin, endMin, overnight, intervalMin, days) = parseReminder(r)
        val cal = Calendar.getInstance()
        val nowMin = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        val todayOk = isDayAllowed(cal, days)

        val triggerMs = when {
            !todayOk                                -> nextAllowedStartMs(sh, sm, days)
            inRange(nowMin, startMin, endMin, overnight) ->
                System.currentTimeMillis() + intervalMin * 60_000L
            // 시작 시각 이전 (비야간: nowMin < startMin, 야간: endMin <= nowMin < startMin)
            (!overnight && nowMin < startMin) ||
            (overnight  && nowMin >= endMin && nowMin < startMin) ->
                calAtTime(sh, sm).timeInMillis
            else                                    -> nextAllowedStartMs(sh, sm, days)
        }
        sendAlarm(context, r, r.optString("id", "").hashCode(), triggerMs)
    }

    /** 알람 수신 후 다음 알람 예약 — 요일/범위를 고려해 최적 시간 계산. */
    fun scheduleOneFromReceiver(context: Context, r: JSONObject, wasInRange: Boolean, dayOk: Boolean) {
        val (sh, sm, _, _, startMin, endMin, overnight, intervalMin, days) = parseReminder(r)

        val triggerMs = if (wasInRange && dayOk) {
            val nextMs = System.currentTimeMillis() + intervalMin * 60_000L
            val nextCal = Calendar.getInstance().apply { timeInMillis = nextMs }
            val nextMin = nextCal.get(Calendar.HOUR_OF_DAY) * 60 + nextCal.get(Calendar.MINUTE)
            val nextInRange = inRange(nextMin, startMin, endMin, overnight)
            val nextDayOk = isDayAllowed(nextCal, days)
            if (nextInRange && nextDayOk) nextMs else nextAllowedStartMs(sh, sm, days)
        } else {
            nextAllowedStartMs(sh, sm, days)
        }
        sendAlarm(context, r, r.optString("id", "").hashCode(), triggerMs)
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
                // action이 일치해야 PendingIntent가 매칭됨
                val intent = Intent(context, ReminderReceiver::class.java).apply {
                    action = "com.kjooy.novelwiki.REMINDER"
                }
                val pi = PendingIntent.getBroadcast(
                    context, id.hashCode(), intent,
                    PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                )
                if (pi != null) am.cancel(pi)
            }
        } catch (e: Exception) { /* ignore */ }
    }

    fun cancelWriting(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = "com.kjooy.novelwiki.WRITING_REMINDER"
        }
        val pi = PendingIntent.getBroadcast(
            context, WRITING_REQUEST_CODE, intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pi != null) am.cancel(pi)
    }

    fun inRange(nowMin: Int, startMin: Int, endMin: Int, overnight: Boolean): Boolean =
        if (!overnight) nowMin >= startMin && nowMin < endMin
        else nowMin >= startMin || nowMin < endMin

    // ── 내부 유틸 ────────────────────────────────────────────────────────────

    private data class ReminderParams(
        val sh: Int, val sm: Int, val eh: Int, val em: Int,
        val startMin: Int, val endMin: Int, val overnight: Boolean,
        val intervalMin: Int, val days: List<String>
    )

    private fun parseReminder(r: JSONObject): ReminderParams {
        val (sh, sm) = parseParts(r.optString("startTime", "00:00"))
        val (eh, em) = parseParts(r.optString("endTime", "23:59"))
        val startMin = sh * 60 + sm
        val endMin = eh * 60 + em
        return ReminderParams(sh, sm, eh, em, startMin, endMin, startMin > endMin,
            r.optInt("interval", 60), getDaysList(r))
    }

    private fun isDayAllowed(cal: Calendar, days: List<String>): Boolean {
        if (days.isEmpty()) return true
        return days.contains(DAY_LABELS[cal.get(Calendar.DAY_OF_WEEK) - 1])
    }

    /** 다음 허용 요일의 startTime 타임스탬프 반환 (최대 7일 탐색). */
    private fun nextAllowedStartMs(sh: Int, sm: Int, days: List<String>): Long {
        val cal = Calendar.getInstance()
        repeat(7) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
            if (isDayAllowed(cal, days)) {
                return cal.apply {
                    set(Calendar.HOUR_OF_DAY, sh)
                    set(Calendar.MINUTE, sm)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            }
        }
        // 폴백: 내일 startTime
        return Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, sh)
            set(Calendar.MINUTE, sm)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun getDaysList(r: JSONObject): List<String> {
        val arr = r.optJSONArray("days") ?: return emptyList()
        return (0 until arr.length()).map { arr.getString(it) }
    }

    private fun sendAlarm(context: Context, r: JSONObject, requestCode: Int, triggerMs: Long) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = "com.kjooy.novelwiki.REMINDER"
            putExtra("reminder_json", r.toString())
        }
        val pi = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        setExact(context, triggerMs, pi)
    }

    private fun parseParts(time: String): Pair<Int, Int> {
        val parts = time.split(":")
        return Pair(parts.getOrNull(0)?.toIntOrNull() ?: 0, parts.getOrNull(1)?.toIntOrNull() ?: 0)
    }

    private fun calAtTime(h: Int, m: Int): Calendar =
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, h)
            set(Calendar.MINUTE, m)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

    private fun setExact(context: Context, triggerMs: Long, pi: PendingIntent) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // USE_EXACT_ALARM(API 33+) 또는 SCHEDULE_EXACT_ALARM 권한으로 별도 체크 없이 직접 호출
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMs, pi)
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, triggerMs, pi)
        }
    }
}
