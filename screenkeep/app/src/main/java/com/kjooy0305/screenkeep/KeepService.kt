package com.kjooy0305.screenkeep

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat

/**
 * 화면 유지 서비스.
 * 다른 앱 위에 보이지 않는 1픽셀 창을 띄우고, 그 창에 "화면 켜 둠" 표시를 단다.
 * 창이 떠 있는 동안은 기기의 자동 꺼짐이 막히고, 전원 버튼은 평소처럼 화면을 끈다.
 */
class KeepService : Service() {
    private lateinit var wm: WindowManager
    private val handler = Handler(Looper.getMainLooper())
    private var overlay: View? = null
    internal var overlayParams: WindowManager.LayoutParams? = null
        private set

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context, i: Intent) {
            when (i.action) {
                Intent.ACTION_SCREEN_OFF -> ScreenLog.add(c, "화면 꺼짐")
                Intent.ACTION_SCREEN_ON -> {
                    ScreenLog.add(c, "화면 켜짐")
                    if (!checkExpired()) ensureOverlay()
                }
            }
        }
    }

    /** 30초마다: 창이 사라졌으면 다시 띄우고, 정한 시간이 끝났는지 본다. */
    private val watchdog = object : Runnable {
        override fun run() {
            if (!checkExpired()) { ensureOverlay(); handler.postDelayed(this, 30_000) }
        }
    }

    override fun onBind(i: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        ContextCompat.registerReceiver(this, screenReceiver,
            IntentFilter(Intent.ACTION_SCREEN_OFF).apply { addAction(Intent.ACTION_SCREEN_ON) }, ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> { finish("끄기 (알림에서)"); return START_NOT_STICKY }
            ACTION_START -> {
                val min = Prefs.durationMin(this)
                val wasOn = Prefs.enabled(this) && alive
                Prefs.setEnabled(this, true)
                Prefs.setEndAt(this, Logic.endAtFor(System.currentTimeMillis(), min))
                ScreenLog.add(this, (if (wasOn) "시간 변경 " else "켜기 ") + "(" + Logic.durationLabel(min) + ")")
            }
            else -> {
                // 기기가 앱을 정리했다가 서비스를 되살린 경우
                if (!Prefs.enabled(this)) { stopSelf(); return START_NOT_STICKY }
                ScreenLog.add(this, "서비스가 다시 시작됨 (기기가 앱을 종료했다가 되살림)")
            }
        }
        startForegroundNow()
        if (!Settings.canDrawOverlays(this)) { finish("켜지 못함: 다른 앱 위에 표시 권한이 없어요"); return START_NOT_STICKY }
        if (checkExpired()) return START_NOT_STICKY
        alive = true
        ensureOverlay()
        handler.removeCallbacks(watchdog)
        handler.postDelayed(watchdog, 30_000)
        return START_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(watchdog)
        try { unregisterReceiver(screenReceiver) } catch (e: Exception) { }
        removeOverlay()
        alive = false
        // 사용자가 끈 게 아닌데 서비스가 사라졌다면 기기가 앱을 정리한 것
        if (Prefs.enabled(this)) ScreenLog.add(this, "서비스가 종료됨 (기기가 앱을 정리함)")
        super.onDestroy()
    }

    private fun checkExpired(): Boolean {
        if (!Logic.isExpired(Prefs.endAt(this), System.currentTimeMillis())) return false
        finish("정한 시간이 끝나 꺼짐")
        return true
    }

    private fun finish(reason: String) {
        ScreenLog.add(this, reason)
        Prefs.setEnabled(this, false)
        handler.removeCallbacks(watchdog)
        removeOverlay()
        alive = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun ensureOverlay() {
        val v = overlay
        if (v != null && v.isAttachedToWindow) return
        removeOverlay()
        val view = View(this).apply { keepScreenOn = true }
        val lp = WindowManager.LayoutParams(
            1, 1, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, OVERLAY_FLAGS, PixelFormat.TRANSLUCENT,
        ).apply { gravity = Gravity.TOP or Gravity.START }
        try {
            wm.addView(view, lp)
            overlay = view; overlayParams = lp
        } catch (e: Exception) {
            finish("켜지 못함: 화면 위에 창을 띄우지 못했어요")
        }
    }

    private fun removeOverlay() {
        overlay?.let { try { wm.removeView(it) } catch (e: Exception) { } }
        overlay = null
    }

    private fun startForegroundNow() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(NotificationChannel(CHANNEL, "화면 유지", NotificationManager.IMPORTANCE_LOW).apply { setShowBadge(false) })
        val open = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        val stop = PendingIntent.getService(this, 1, Intent(this, KeepService::class.java).setAction(ACTION_STOP), PendingIntent.FLAG_IMMUTABLE)
        val n = Notification.Builder(this, CHANNEL)
            .setSmallIcon(R.drawable.ic_stat)
            .setContentTitle("화면 유지 중")
            .setContentText(Logic.remainingText(Prefs.endAt(this), System.currentTimeMillis()))
            .setContentIntent(open)
            .setOngoing(true)
            .addAction(Notification.Action.Builder(null, "끄기", stop).build())
            .build()
        val type = if (Build.VERSION.SDK_INT >= 34) ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE else 0
        ServiceCompat.startForeground(this, NOTI_ID, n, type)
    }

    companion object {
        const val ACTION_START = "com.kjooy0305.screenkeep.START"
        const val ACTION_STOP = "com.kjooy0305.screenkeep.STOP"
        private const val CHANNEL = "keep"
        private const val NOTI_ID = 1

        /** 터치·포커스를 받지 않는 1픽셀 창. KEEP_SCREEN_ON 이 핵심이다. */
        const val OVERLAY_FLAGS = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS

        /** 이 프로세스에서 서비스가 실제로 돌고 있는지. 프로세스가 죽으면 false 로 돌아간다. */
        @Volatile var alive = false

        fun start(c: Context) {
            ContextCompat.startForegroundService(c, Intent(c, KeepService::class.java).setAction(ACTION_START))
        }

        /** 사용자가 끈다. 서비스가 이미 없어도 상태가 꺼짐으로 정리된다. */
        fun stop(c: Context) {
            if (Prefs.enabled(c)) ScreenLog.add(c, "끄기")
            Prefs.setEnabled(c, false)
            c.stopService(Intent(c, KeepService::class.java))
        }
    }
}
