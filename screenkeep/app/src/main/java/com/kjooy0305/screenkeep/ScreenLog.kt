package com.kjooy0305.screenkeep

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.PowerManager

/** 꺼짐 기록 남기기: 그 순간의 배터리·충전·절전 모드를 같이 적는다. */
object ScreenLog {
    fun add(c: Context, what: String, keepOn: Boolean = Prefs.enabled(c)) {
        var level = -1
        var charging = false
        try {
            val b: Intent? = c.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            if (b != null) {
                val l = b.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val s = b.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                if (l >= 0 && s > 0) level = l * 100 / s
                val st = b.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                charging = st == BatteryManager.BATTERY_STATUS_CHARGING || st == BatteryManager.BATTERY_STATUS_FULL
            }
        } catch (e: Exception) { /* 기록만 못 남길 뿐 */ }
        val saver = try { (c.getSystemService(Context.POWER_SERVICE) as PowerManager).isPowerSaveMode } catch (e: Exception) { false }
        Prefs.addLog(c, LogEntry(System.currentTimeMillis(), what, keepOn, level, charging, saver))
    }
}
