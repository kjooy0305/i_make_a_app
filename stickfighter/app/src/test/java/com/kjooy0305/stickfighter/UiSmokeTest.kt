package com.kjooy0305.stickfighter

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.SystemClock
import android.view.MotionEvent
import androidx.test.core.app.ApplicationProvider
import androidx.recyclerview.widget.RecyclerView
import com.kjooy0305.stickfighter.data.Repository
import com.kjooy0305.stickfighter.model.*
import com.kjooy0305.stickfighter.render.Fit
import com.kjooy0305.stickfighter.ui.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.GraphicsMode.Mode.NATIVE
import org.robolectric.shadows.ShadowLooper
import java.io.File

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(NATIVE)
class UiSmokeTest {
    private val context get() = ApplicationProvider.getApplicationContext<android.app.Application>()

    private fun seed(frames: Int = 2): Animation {
        val repo = Repository(context.filesDir)
        val base = Animation("a1", "테스트 애니메이션")
        val man = Templates.create("stickman", 1, base.width / 2f, base.height * 0.55f)
        val posed = FigureEdit.pose(FigureEdit.move(man, Pt(700f, 300f)), 2, Pt(800f, 150f))
        val list = listOf(Frame(listOf(man)), Frame(listOf(posed))).take(frames)
        return repo.save(base.copy(frames = list))
    }

    private fun touch(view: android.view.View, action: Int, x: Float, y: Float) {
        val t = SystemClock.uptimeMillis()
        val e = MotionEvent.obtain(t, t, action, x, y, 0)
        view.dispatchTouchEvent(e)
        e.recycle()
    }

    @Test fun homeShowsEmptyStateThenRows() {
        val ctrl = Robolectric.buildActivity(HomeActivity::class.java).setup()
        val home = ctrl.get()
        val empty = home.findViewById<android.widget.TextView>(R.id.emptyText)
        assertEquals(android.view.View.VISIBLE, empty.visibility)
        seed()
        ctrl.pause().resume()
        assertEquals(android.view.View.GONE, empty.visibility)
        val list = home.findViewById<RecyclerView>(R.id.list)
        list.measure(1080, 1920); list.layout(0, 0, 1080, 1920)
        assertEquals(1, list.adapter!!.itemCount)
    }

    @Test fun editorOpensAndDragsAPoseDot() {
        val anim = seed()
        val intent = EditorActivity.forAnimation(context, anim.id)
        val activity = Robolectric.buildActivity(EditorActivity::class.java, intent).setup().get()
        val view = activity.findViewById<EditorView>(R.id.editorView)
        view.measure(
            android.view.View.MeasureSpec.makeMeasureSpec(1600, android.view.View.MeasureSpec.EXACTLY),
            android.view.View.MeasureSpec.makeMeasureSpec(700, android.view.View.MeasureSpec.EXACTLY))
        view.layout(0, 0, 1600, 700)
        val fit = Fit.of(1600f, 700f, view.animation, 12f * context.resources.displayMetrics.density)
        val figure = view.frame.figures[0]
        // 왼팔 윗마디 끝의 빨간 점을 끈다
        val end = Kinematics.layout(figure)[2].end
        val sx = fit.toView(end.x); val sy = fit.toViewY(end.y)
        touch(view, MotionEvent.ACTION_DOWN, sx, sy)
        touch(view, MotionEvent.ACTION_MOVE, sx + 60, sy - 80)
        touch(view, MotionEvent.ACTION_MOVE, sx + 120, sy - 160)
        touch(view, MotionEvent.ACTION_UP, sx + 120, sy - 160)
        val after = view.frame.figures[0]
        assertNotEquals(figure.segments[2].angle, after.segments[2].angle)
        // 되돌리기
        activity.findViewById<android.view.View>(R.id.undoButton).performClick()
        assertEquals(figure.segments[2].angle, view.frame.figures[0].segments[2].angle, 1e-6f)
    }

    @Test fun addAndDeleteFrames() {
        val anim = seed()
        val activity = Robolectric.buildActivity(EditorActivity::class.java, EditorActivity.forAnimation(context, anim.id)).setup().get()
        val strip = activity.findViewById<RecyclerView>(R.id.frameStrip)
        assertEquals(3, strip.adapter!!.itemCount) // 프레임 2 + 추가 버튼
        // 추가 버튼은 마지막 칸
        strip.measure(1600, 200); strip.layout(0, 0, 1600, 200)
        activity.findViewById<RecyclerView>(R.id.frameStrip).findViewHolderForAdapterPosition(2)!!.itemView.performClick()
        assertEquals(4, strip.adapter!!.itemCount)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        assertEquals(3, Repository(context.filesDir).animation(anim.id)!!.frames.size)
    }

    @Test fun playerPlaysThroughAndStops() {
        val anim = seed()
        val ctrl = Robolectric.buildActivity(PlayerActivity::class.java,
            Intent(context, PlayerActivity::class.java).putExtra(PlayerActivity.EXTRA_ANIMATION, anim.id)).setup()
        val activity: Activity = ctrl.get()
        assertNotNull(activity.findViewById<PlayerView>(R.id.playerView))
        ctrl.pause().stop().destroy()
    }

    @Test fun figureEditorOpens() {
        val repo = Repository(context.filesDir)
        repo.saveFigure(Templates.create("sword", 42, 0f, 0f).copy(name = "내 검"))
        val activity = Robolectric.buildActivity(EditorActivity::class.java, EditorActivity.forFigure(context, 42)).setup().get()
        assertEquals("내 검", activity.findViewById<android.widget.TextView>(R.id.titleText).text.toString())
    }

    @Test fun renderTemplatesToPng() {
        val out = File(System.getProperty("java.io.tmpdir"), "sf-render").apply { mkdirs() }
        val anim = seed()
        val bmp = Thumbnails.animation(anim.copy(frames = listOf(
            Frame(Templates.all.mapIndexed { i, t -> t.build(i + 1L, 120f + i * 150f, 300f) }))), 800)
        File(out, "templates.png").outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        val mid = Interpolator.at(anim.frames, 0.5f)
        val b2 = Thumbnails.animation(anim.copy(frames = listOf(mid)), 800)
        File(out, "midframe.png").outputStream().use { b2.compress(Bitmap.CompressFormat.PNG, 100, it) }
        assertTrue(File(out, "templates.png").length() > 1000)
    }
}
