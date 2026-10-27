package com.kjooy0305.dayclean.platform

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.kjooy0305.dayclean.R
import com.kjooy0305.dayclean.logic.Reminders
import com.kjooy0305.dayclean.model.CleanupTime
import com.kjooy0305.dayclean.model.Item
import com.kjooy0305.dayclean.ui.HomeActivity

/** 알림 채널과 알림 만들기. 항목마다 알림이 하나씩 있고, 완료 버튼은 그 항목만 처리한다. */
object Notifier {
    const val CHANNEL_ITEMS = "items"
    const val CHANNEL_CLEANUP = "cleanup"
    const val ACTION_DONE = "com.kjooy0305.dayclean.ACTION_DONE"
    const val EXTRA_ITEM_ID = "itemId"
    private const val CLEANUP_NOTIFICATION_BASE = 1_000_000

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_ITEMS, "해야 할 것", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "지금 해야 할 것과 오늘 안에 해야 할 것을 10분마다 알려요"
            }
        )
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_CLEANUP, "정리 시간", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "아침 정리, 저녁 정리, 회고 시간을 알려요"
            }
        )
    }

    fun canPost(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    private fun openApp(context: Context, requestCode: Int): PendingIntent = PendingIntent.getActivity(
        context, requestCode,
        Intent(context, HomeActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    fun postItem(context: Context, item: Item) {
        if (!canPost(context)) return
        val doneIntent = PendingIntent.getBroadcast(
            context, item.id.toInt(),
            Intent(context, ActionReceiver::class.java).setAction(ACTION_DONE).putExtra(EXTRA_ITEM_ID, item.id),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val n = NotificationCompat.Builder(context, CHANNEL_ITEMS)
            .setSmallIcon(R.drawable.ic_stat)
            .setContentTitle(Reminders.title(item))
            .setContentText(Reminders.body(item))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setOnlyAlertOnce(false)
            .setWhen(System.currentTimeMillis())
            .setShowWhen(true)
            .setContentIntent(openApp(context, 1))
            .addAction(0, "완료", doneIntent)
            .setAutoCancel(false)
            .build()
        notify(context, item.id.toInt(), n)
    }

    fun postCleanup(context: Context, cleanup: CleanupTime) {
        if (!canPost(context)) return
        val n = NotificationCompat.Builder(context, CHANNEL_CLEANUP)
            .setSmallIcon(R.drawable.ic_stat)
            .setContentTitle("${cleanup.name} 시간이에요")
            .setContentText("눌러서 바로 시작해요")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(openApp(context, 2))
            .setAutoCancel(true)
            .build()
        notify(context, CLEANUP_NOTIFICATION_BASE + cleanup.id.toInt(), n)
    }

    fun cancelItem(context: Context, itemId: Long) {
        NotificationManagerCompat.from(context).cancel(itemId.toInt())
    }

    /** 오늘 목록에 없는 항목의 알림이 남지 않게 정리한다. */
    fun cancelExcept(context: Context, activeIds: Set<Long>) {
        val nm = context.getSystemService(NotificationManager::class.java)
        for (sbn in nm.activeNotifications) {
            if (sbn.id < CLEANUP_NOTIFICATION_BASE && sbn.id.toLong() !in activeIds) nm.cancel(sbn.id)
        }
    }

    @Suppress("MissingPermission")
    private fun notify(context: Context, id: Int, n: Notification) {
        runCatching { NotificationManagerCompat.from(context).notify(id, n) }
    }
}
