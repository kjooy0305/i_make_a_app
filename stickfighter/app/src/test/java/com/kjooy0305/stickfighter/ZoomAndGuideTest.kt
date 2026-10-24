package com.kjooy0305.stickfighter

import android.graphics.Bitmap
import android.graphics.Color
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import androidx.test.core.app.ApplicationProvider
import com.kjooy0305.stickfighter.data.Repository
import com.kjooy0305.stickfighter.model.*
import com.kjooy0305.stickfighter.render.Fit
import com.kjooy0305.stickfighter.render.FrameRenderer
import com.kjooy0305.stickfighter.render.ViewTransform
import com.kjooy0305.stickfighter.ui.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.GraphicsMode.Mode.NATIVE
import java.io.File
import kotlin.math.abs

class ViewTransformTest {
    private fun t() = ViewTransform().apply {
        setPaper(1000f, 500f)
        setView(2000f, 1000f)
    }

    private fun near(a: Float, b: Float, eps: Float = 0.01f) = assertTrue("$a != $b", abs(a - b) < eps)

    @Test fun fitsPaperAndCentersIt() {
        val v = t()
        near(v.scale, 2f); near(v.offsetX, 0f); near(v.offsetY, 0f); near(v.zoom, 1f)
        assertFalse(v.adjusted)
    }

    @Test fun insetsKeepPaperClearOfToolbars() {
        val v = t()
        v.setInsets(0f, 100f, 200f, 100f)
        // 가용 폭 1800, 높이 800 → 배율 min(1.8, 1.6)
        near(v.scale, 1.6f)
        assertTrue(v.offsetY >= 100f)
        assertTrue(v.offsetX + 1000f * v.scale <= 2000f - 200f + 0.01f)
    }

    @Test fun zoomKeepsTheFocusPointInPlace() {
        val v = t()
        val lx = (700f - v.offsetX) / v.scale
        val ly = (300f - v.offsetY) / v.scale
        v.zoomAround(700f, 300f, 2f)
        near(v.zoom, 2f)
        near(lx * v.scale + v.offsetX, 700f); near(ly * v.scale + v.offsetY, 300f)
        assertTrue(v.adjusted)
    }

    @Test fun zoomIsClamped() {
        val v = t()
        repeat(40) { v.zoomCenter(2f) }
        near(v.zoom, ViewTransform.MAX_ZOOM)
        repeat(80) { v.zoomCenter(0.5f) }
        near(v.zoom, ViewTransform.MIN_ZOOM)
    }

    @Test fun panCannotLoseThePaper() {
        val v = t()
        v.panBy(-999999f, 999999f)
        assertTrue(v.offsetX + 1000f * v.scale >= 96f - 0.01f)
        assertTrue(v.offsetY <= 1000f - 96f + 0.01f)
    }

    @Test fun fitResetsAndResizeKeepsUserZoom() {
        val v = t()
        v.zoomCenter(3f)
        v.setView(1000f, 500f)           // 화면 크기가 바뀌어도
        assertTrue(v.adjusted)           // 사용자가 맞춘 확대는 유지
        v.fit()
        assertFalse(v.adjusted); near(v.zoom, 1f)
    }

    @Test fun previewRanges() {
        val one = PreviewRange.of(1, 0, PreviewRange.Scope.TRANSITION)
        assertEquals(0f, one.length, 0f)
        val mid = PreviewRange.of(5, 2, PreviewRange.Scope.TRANSITION)
        assertEquals(2f, mid.start, 0f); assertEquals(3f, mid.end, 0f)
        val last = PreviewRange.of(5, 4, PreviewRange.Scope.TRANSITION)
        assertEquals(3f, last.start, 0f); assertEquals(4f, last.end, 0f)
        val all = PreviewRange.of(5, 2, PreviewRange.Scope.ALL)
        assertEquals(0f, all.start, 0f); assertEquals(4f, all.end, 0f)
    }
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(NATIVE)
class ZoomAndGuideUiTest {
    private val context get() = ApplicationProvider.getApplicationContext<android.app.Application>()

    private fun seed(): Animation {
        val repo = Repository(context.filesDir)
        val base = Animation("g1", "가이드")
        val a = Templates.create("stickman", 1, 300f, 300f)
        val b = FigureEdit.move(a, Pt(700f, 300f))
        return repo.save(base.copy(frames = listOf(Frame(listOf(a)), Frame(listOf(b)))))
    }

    @Test fun guideShowsOnlyPreviousFrameInGrayAndNeverInPlayback() {
        val anim = seed()
        val bmp = Bitmap.createBitmap(800, 450, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bmp)
        val fit = Fit.of(800f, 450f, anim)
        FrameRenderer().draw(canvas, anim, anim.frames[1], fit, guide = anim.frames[0])
        // 이전 장면 몸통(x=300) 자리에는 연한 회색, 지금 장면(x=700) 자리에는 짙은 검정
        val guidePixel = bmp.getPixel((300 * fit.scale).toInt(), (255 * fit.scale).toInt())
        val currentPixel = bmp.getPixel((700 * fit.scale).toInt(), (255 * fit.scale).toInt())
        assertTrue("guide=${Integer.toHexString(guidePixel)}", Color.red(guidePixel) in 150..235 && abs(Color.red(guidePixel) - Color.blue(guidePixel)) < 15)
        assertTrue("current=${Integer.toHexString(currentPixel)}", Color.red(currentPixel) < 80)
        // 밑그림 없이 그리면 이전 자리는 흰 종이
        val plain = Bitmap.createBitmap(800, 450, Bitmap.Config.ARGB_8888)
        FrameRenderer().draw(android.graphics.Canvas(plain), anim, anim.frames[1], fit)
        assertEquals(Color.WHITE, plain.getPixel((300 * fit.scale).toInt(), (255 * fit.scale).toInt()))
        val out = File(System.getProperty("java.io.tmpdir"), "sf-render").apply { mkdirs() }
        File(out, "guide.png").outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    private fun editor(index: Int): EditorActivity {
        val anim = seed()
        val ctrl = Robolectric.buildActivity(EditorActivity::class.java, EditorActivity.forAnimation(context, anim.id)).setup()
        val activity = ctrl.get()
        if (index > 0) activity.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.frameStrip).let { rv ->
            rv.measure(1600, 200); rv.layout(0, 0, 1600, 200)
            rv.findViewHolderForAdapterPosition(index)!!.itemView.performClick()
        }
        return activity
    }

    @Test fun editorUsesPreviousFrameAsGuide() {
        val first = editor(0)
        assertNull(first.findViewById<EditorView>(R.id.editorView).guideFrame)
        val second = editor(1)
        assertNotNull(second.findViewById<EditorView>(R.id.editorView).guideFrame)
        // 끄면 사라진다
        second.findViewById<View>(R.id.onionButton).performClick()
        assertNull(second.findViewById<EditorView>(R.id.editorView).guideFrame)
    }

    @Test fun zoomButtonsChangeZoom() {
        val a = editor(0)
        val v = a.findViewById<EditorView>(R.id.editorView)
        v.measure(View.MeasureSpec.makeMeasureSpec(1600, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(700, View.MeasureSpec.EXACTLY))
        v.layout(0, 0, 1600, 700)
        val before = v.zoomLevel
        a.findViewById<View>(R.id.zoomIn).performClick()
        assertTrue(v.zoomLevel > before)
        a.findViewById<View>(R.id.zoomFit).performClick()
        assertEquals(1f, v.zoomLevel, 0.001f)
        a.findViewById<View>(R.id.zoomOut).performClick()
        assertTrue(v.zoomLevel < 1f)
    }

    @Test fun pinchZoomsTheCanvas() {
        val a = editor(0)
        val v = a.findViewById<EditorView>(R.id.editorView)
        v.measure(View.MeasureSpec.makeMeasureSpec(1600, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(700, View.MeasureSpec.EXACTLY))
        v.layout(0, 0, 1600, 700)
        val t0 = SystemClock.uptimeMillis()
        fun ev(action: Int, p0: Pair<Float, Float>, p1: Pair<Float, Float>?, t: Long): MotionEvent {
            val n = if (p1 == null) 1 else 2
            val props = Array(n) { MotionEvent.PointerProperties().apply { id = it; toolType = MotionEvent.TOOL_TYPE_FINGER } }
            val coords = Array(n) { i ->
                MotionEvent.PointerCoords().apply {
                    val p = if (i == 0) p0 else p1!!
                    x = p.first; y = p.second; pressure = 1f; size = 1f
                }
            }
            return MotionEvent.obtain(t0, t, action, n, props, coords, 0, 0, 1f, 1f, 0, 0, 0, 0)
        }
        v.dispatchTouchEvent(ev(MotionEvent.ACTION_DOWN, 700f to 350f, null, t0))
        v.dispatchTouchEvent(ev(MotionEvent.ACTION_POINTER_DOWN or (1 shl MotionEvent.ACTION_POINTER_INDEX_SHIFT), 700f to 350f, 900f to 350f, t0 + 10))
        for (i in 1..6) {
            v.dispatchTouchEvent(ev(MotionEvent.ACTION_MOVE, (700f - i * 40) to 350f, (900f + i * 40) to 350f, t0 + 20L + i * 10))
        }
        v.dispatchTouchEvent(ev(MotionEvent.ACTION_POINTER_UP or (1 shl MotionEvent.ACTION_POINTER_INDEX_SHIFT), 460f to 350f, 1140f to 350f, t0 + 100))
        v.dispatchTouchEvent(ev(MotionEvent.ACTION_UP, 460f to 350f, null, t0 + 110))
        assertTrue("zoom=${v.zoomLevel}", v.zoomLevel > 1.5f)
    }

    @Test fun windowsToggle() {
        val a = editor(0)
        val preview = a.findViewById<View>(R.id.previewWindow)
        assertEquals(View.GONE, preview.visibility)
        a.findViewById<View>(R.id.previewButton).performClick()
        assertEquals(View.VISIBLE, preview.visibility)
        a.findViewById<View>(R.id.previewClose).performClick()
        assertEquals(View.GONE, preview.visibility)
        val strip = a.findViewById<View>(R.id.frameStrip)
        assertEquals(View.VISIBLE, strip.visibility)
        a.findViewById<View>(R.id.framesButton).performClick()
        assertEquals(View.GONE, strip.visibility)
        a.findViewById<View>(R.id.hideUiButton).performClick()
        assertEquals(View.GONE, a.findViewById<View>(R.id.topBar).visibility)
        assertEquals(View.VISIBLE, a.findViewById<View>(R.id.showUiButton).visibility)
        a.findViewById<View>(R.id.showUiButton).performClick()
        assertEquals(View.VISIBLE, a.findViewById<View>(R.id.topBar).visibility)
    }
}
