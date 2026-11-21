package com.kjooy0305.screenkeep

import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

/** 빠른 설정 패널의 한 칸. 한 번 누르면 켜고, 다시 누르면 끈다. */
class KeepTile : TileService() {
    override fun onStartListening() = refresh()

    override fun onClick() {
        if (Prefs.enabled(this)) { KeepService.stop(this); refresh(); return }
        val ok = Settings.canDrawOverlays(this) && try { KeepService.start(this); true } catch (e: Exception) { false }
        if (!ok) {
            val open = Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (Build.VERSION.SDK_INT >= 34) startActivityAndCollapse(PendingIntent.getActivity(this, 2, open, PendingIntent.FLAG_IMMUTABLE))
            else @Suppress("DEPRECATION") startActivityAndCollapse(open)
        }
        refresh()
    }

    private fun refresh() {
        val t = qsTile ?: return
        val on = Prefs.enabled(this)
        t.state = if (on) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        t.label = "화면 유지"
        t.subtitle = if (on) "켜짐" else "꺼짐"
        t.icon = Icon.createWithResource(this, R.drawable.ic_tile)
        t.updateTile()
    }
}
