package com.kjooy0305.apkextractor

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat

/**
 * 큰 앱을 저장하는 동안 시스템이 앱을 정리해 버리지 않게 앞에서 실행 중임을 알린다.
 * 파일 작업은 MainActivity 쪽에서 하고, 이 서비스는 알림만 띄운다.
 */
class ExtractGuardService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val text = intent?.getStringExtra(EXTRA_TEXT) ?: getString(R.string.guard_text)
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL, getString(R.string.guard_channel), NotificationManager.IMPORTANCE_LOW)
            )
        }
        val n = NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(text)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC else 0
        ServiceCompat.startForeground(this, ID, n, type)
        return START_NOT_STICKY
    }

    companion object {
        const val EXTRA_TEXT = "text"
        private const val CHANNEL = "extract_guard"
        private const val ID = 7
    }
}
