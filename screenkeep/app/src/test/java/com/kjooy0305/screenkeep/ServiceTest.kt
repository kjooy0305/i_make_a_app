package com.kjooy0305.screenkeep

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.view.WindowManager
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowSettings

@RunWith(RobolectricTestRunner::class)
class ServiceTest {
    private val ctx get() = ApplicationProvider.getApplicationContext<Application>()
    private fun intent(action: String) = Intent(ctx, KeepService::class.java).setAction(action)

    @Before fun reset() {
        Prefs.setEnabled(ctx, false); Prefs.setEndAt(ctx, 0); Prefs.setDurationMin(ctx, 0); Prefs.clearLogs(ctx)
        KeepService.alive = false
    }

    @Test fun startCreatesKeepScreenOnOverlay() {
        ShadowSettings.setCanDrawOverlays(true)
        val ctl = Robolectric.buildService(KeepService::class.java, intent(KeepService.ACTION_START)).create().startCommand(0, 0)
        val lp = ctl.get().overlayParams!!
        assertTrue("화면 켜 둠 표시", lp.flags and WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON != 0)
        assertTrue("터치를 가로채지 않음", lp.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE != 0)
        assertTrue("포커스를 가로채지 않음", lp.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE != 0)
        assertEquals(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, lp.type)
        assertEquals(1, lp.width); assertEquals(1, lp.height)
        assertTrue(Prefs.enabled(ctx)); assertTrue(KeepService.alive)
        assertEquals(0L, Prefs.endAt(ctx))
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        assertEquals(1, shadowOf(nm).allNotifications.size)
        ctl.destroy()
    }

    @Test fun timerSetsEndAt() {
        ShadowSettings.setCanDrawOverlays(true)
        Prefs.setDurationMin(ctx, 30)
        val before = System.currentTimeMillis()
        Robolectric.buildService(KeepService::class.java, intent(KeepService.ACTION_START)).create().startCommand(0, 0)
        val end = Prefs.endAt(ctx)
        assertTrue(end >= before + 30 * 60_000 && end < before + 30 * 60_000 + 5_000)
    }

    @Test fun noPermissionDoesNotTurnOn() {
        ShadowSettings.setCanDrawOverlays(false)
        val ctl = Robolectric.buildService(KeepService::class.java, intent(KeepService.ACTION_START)).create().startCommand(0, 0)
        assertFalse(Prefs.enabled(ctx)); assertFalse(KeepService.alive)
        assertNull(ctl.get().overlayParams)
        assertTrue(Prefs.logs(ctx).any { it.what.contains("권한이 없어요") })
    }

    @Test fun expiredTimerTurnsOffOnRestart() {
        ShadowSettings.setCanDrawOverlays(true)
        Prefs.setEnabled(ctx, true); Prefs.setEndAt(ctx, System.currentTimeMillis() - 1000)
        Robolectric.buildService(KeepService::class.java, Intent(ctx, KeepService::class.java)).create().startCommand(0, 0)
        assertFalse(Prefs.enabled(ctx)); assertFalse(KeepService.alive)
        assertTrue(Prefs.logs(ctx).any { it.what.contains("정한 시간이 끝나") })
    }

    @Test fun restartAfterSystemKillResumes() {
        ShadowSettings.setCanDrawOverlays(true)
        Prefs.setEnabled(ctx, true)                                   // 켜 둔 채로 프로세스가 죽었다가 되살아난 상황
        val ctl = Robolectric.buildService(KeepService::class.java, Intent(ctx, KeepService::class.java)).create().startCommand(0, 0)
        assertTrue(KeepService.alive); assertNotNull(ctl.get().overlayParams)
        assertTrue(Prefs.logs(ctx).any { it.what.contains("다시 시작됨") })
        ctl.destroy()
        assertTrue("사용자가 끄지 않았는데 사라지면 기록", Prefs.logs(ctx).any { it.what.contains("서비스가 종료됨") })
    }

    @Test fun userStopIsNotLoggedAsKilled() {
        ShadowSettings.setCanDrawOverlays(true)
        val ctl = Robolectric.buildService(KeepService::class.java, intent(KeepService.ACTION_START)).create().startCommand(0, 0)
        Prefs.setEnabled(ctx, false)                                  // KeepService.stop 과 같은 순서
        ctl.destroy()
        assertFalse(Prefs.logs(ctx).any { it.what.contains("서비스가 종료됨") })
    }

    @Test fun stopFromNotificationButton() {
        ShadowSettings.setCanDrawOverlays(true)
        val ctl = Robolectric.buildService(KeepService::class.java, intent(KeepService.ACTION_START)).create().startCommand(0, 0)
        ctl.get().onStartCommand(intent(KeepService.ACTION_STOP), 0, 1)
        assertFalse(Prefs.enabled(ctx)); assertFalse(KeepService.alive)
    }
}
