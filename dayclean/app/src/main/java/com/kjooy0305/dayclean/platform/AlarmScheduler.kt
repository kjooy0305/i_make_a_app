package com.kjooy0305.dayclean.platform

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.kjooy0305.dayclean.data.Store
import com.kjooy0305.dayclean.logic.Engine

/** 다음에 깨울 시각 하나만 정확한 알람으로 걸어 둔다. 앱이 꺼져 있어도 울린다. */
object AlarmScheduler {
    private const val REQUEST = 7

    /** 알람이 울렸거나 상태가 바뀌었을 때 부른다. 지금 해야 할 알림을 내고, 다음 알람을 건다. */
    fun run(context: Context) {
        val store = Store.get(context)
        val result = store.process()
        Notifier.createChannels(context)
        result.cleanupAlert?.let { Notifier.postCleanup(context, it) }
        result.itemAlerts.forEach { Notifier.postItem(context, it) }
        // 해결했거나 회고로 처리한 항목의 알림은 지운다.
        val active = store.state.items.filter { com.kjooy0305.dayclean.logic.Reminders.needsAlert(it) }.map { it.id }.toSet()
        runCatching { Notifier.cancelExcept(context, active) }
        schedule(context)
    }

    fun schedule(context: Context) {
        val store = Store.get(context)
        val at = Engine.nextWake(store.state, store.now())
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = PendingIntent.getBroadcast(
            context, REQUEST, Intent(context, AlarmReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || am.canScheduleExactAlarms()
        runCatching {
            if (canExact) am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
            else am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
        }
    }
}
