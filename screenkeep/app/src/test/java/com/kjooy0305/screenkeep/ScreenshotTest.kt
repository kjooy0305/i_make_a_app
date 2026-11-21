package com.kjooy0305.screenkeep

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.widget.ScrollView
import androidx.test.core.app.ApplicationProvider
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowSettings
import java.io.File

/** 화면을 PNG로 저장해 눈으로 확인하는 시험. 결과는 임시 폴더의 sk-shots 에 쌓인다. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w360dp-h760dp-xxhdpi")
class ScreenshotTest {
    private val ctx get() = ApplicationProvider.getApplicationContext<Application>()
    private val out = File(System.getProperty("java.io.tmpdir"), "sk-shots").apply { mkdirs() }

    private fun shot(name: String, view: View) {
        val w = (360 * 3); val h = 760 * 3
        view.measure(View.MeasureSpec.makeMeasureSpec(w, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
        val vh = maxOf(h, view.measuredHeight)
        view.measure(View.MeasureSpec.makeMeasureSpec(w, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(vh, View.MeasureSpec.EXACTLY))
        view.layout(0, 0, w, vh)
        val bmp = Bitmap.createBitmap(w, vh, Bitmap.Config.ARGB_8888)
        Canvas(bmp).also { it.drawColor(android.graphics.Color.WHITE) }.let { view.draw(it) }
        File(out, "$name.png").outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    private fun screen(): View {
        val a = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        return ((a.findViewById<View>(android.R.id.content) as android.view.ViewGroup).getChildAt(0) as ScrollView).getChildAt(0)
    }

    @Test fun shots() {
        ShadowSettings.setCanDrawOverlays(false)
        Prefs.setEnabled(ctx, false); Prefs.setDurationMin(ctx, 0); Prefs.clearLogs(ctx)
        shot("1-off-nopermission", screen())

        ShadowSettings.setCanDrawOverlays(true)
        val t = System.currentTimeMillis()
        listOf(
            LogEntry(t - 3_600_000, "켜기 (1시간 30분)", true, 71, false, false),
            LogEntry(t - 2_000_000, "화면 꺼짐", true, 64, false, false),
            LogEntry(t - 1_999_000, "서비스가 종료됨 (기기가 앱을 정리함)", true, 64, false, true),
            LogEntry(t - 1_500_000, "화면 켜짐", true, 62, false, true),
            LogEntry(t - 1_400_000, "서비스가 다시 시작됨 (기기가 앱을 종료했다가 되살림)", true, 62, true, false),
        ).forEach { Prefs.addLog(ctx, it) }
        Prefs.setEnabled(ctx, true); Prefs.setDurationMin(ctx, 90); Prefs.setEndAt(ctx, t + 80 * 60_000)
        shot("2-on-timer-logs", screen())
    }
}
