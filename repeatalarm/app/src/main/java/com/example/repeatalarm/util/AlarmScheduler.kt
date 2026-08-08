package com.example.repeatalarm.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.repeatalarm.data.AlarmItem
import com.example.repeatalarm.receiver.AlarmReceiver
import java.util.Calendar

object AlarmScheduler {

    fun schedule(context: Context, item: AlarmItem) {
        val triggerAt = calculateNextTriggerTime(item)
        setExactAlarm(context, item.id, triggerAt)
    }

    fun cancel(context: Context, itemId: Int) {
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        manager.cancel(buildPendingIntent(context, itemId))
    }

    fun scheduleNextFromReceiver(context: Context, item: AlarmItem) {
        val now = System.currentTimeMillis()
        val intervalMs = item.intervalMinutes * 60_000L
        val nextTime = now + intervalMs

        val endCal = todayCalendarAt(item.endHour, item.endMinute)

        val triggerAt = if (nextTime <= endCal.timeInMillis) {
            nextTime
        } else {
            // Done for today — schedule start time tomorrow
            val startTomorrow = todayCalendarAt(item.startHour, item.startMinute)
            startTomorrow.add(Calendar.DAY_OF_MONTH, 1)
            startTomorrow.timeInMillis
        }

        setExactAlarm(context, item.id, triggerAt)
    }

    private fun calculateNextTriggerTime(item: AlarmItem): Long {
        val now = System.currentTimeMillis()
        val startCal = todayCalendarAt(item.startHour, item.startMinute)
        val endCal = todayCalendarAt(item.endHour, item.endMinute)

        return when {
            now < startCal.timeInMillis -> startCal.timeInMillis
            now < endCal.timeInMillis -> now + item.intervalMinutes * 60_000L
            else -> {
                startCal.add(Calendar.DAY_OF_MONTH, 1)
                startCal.timeInMillis
            }
        }
    }

    private fun setExactAlarm(context: Context, itemId: Int, triggerAtMs: Long) {
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = buildPendingIntent(context, itemId)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent)
        } else {
            manager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent)
        }
    }

    private fun buildPendingIntent(context: Context, itemId: Int): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_ITEM_ID, itemId)
        }
        return PendingIntent.getBroadcast(
            context, itemId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun todayCalendarAt(hour: Int, minute: Int): Calendar =
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
}
