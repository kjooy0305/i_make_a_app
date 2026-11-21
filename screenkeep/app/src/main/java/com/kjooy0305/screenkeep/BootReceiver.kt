package com.kjooy0305.screenkeep

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings

/** 휴대폰을 켜면(설정이 켜져 있을 때) 다시 시작하고, 앱을 업데이트한 뒤에는 켜져 있던 상태를 되살린다. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) {
        if (!Settings.canDrawOverlays(c)) return
        val boot = i.action == Intent.ACTION_BOOT_COMPLETED
        if (boot && Prefs.autoStart(c) || i.action == Intent.ACTION_MY_PACKAGE_REPLACED && Prefs.enabled(c)) {
            try { KeepService.start(c) } catch (e: Exception) { ScreenLog.add(c, "자동 시작 실패", false) }
        }
    }
}
