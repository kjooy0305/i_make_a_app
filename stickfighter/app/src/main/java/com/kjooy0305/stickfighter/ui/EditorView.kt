package com.kjooy0305.stickfighter.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.kjooy0305.stickfighter.R
import com.kjooy0305.stickfighter.model.Animation
import com.kjooy0305.stickfighter.model.Figure
import com.kjooy0305.stickfighter.model.FigureEdit
import com.kjooy0305.stickfighter.model.Frame
import com.kjooy0305.stickfighter.model.Kinematics
import com.kjooy0305.stickfighter.model.Pt
import com.kjooy0305.stickfighter.model.Shape
import com.kjooy0305.stickfighter.render.Fit
import com.kjooy0305.stickfighter.render.FrameRenderer
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

enum class EditMode { POSE, STRETCH, FLIP }

/** 프레임 하나를 보여 주고, 점을 끌어 피규어를 고치는 뷰. 상태는 Activity가 쥐고 이 뷰는 그리기와 터치만 맡는다. */
class EditorView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    interface Listener {
        /** 고치기 직전. 되돌리기 기록을 남길 때 쓴다. */
        fun onGestureStart()
        fun onFrameChanged(frame: Frame)
        fun onGestureEnd()
        fun onSelectionChanged(figureId: Long?, segmentId: Int?)
        fun onModifyRequested(figureId: Long, segmentId: Int)
    }

    private enum class Kind { MOVE, POSE, STRETCH, MODIFY, FLIP }
    private class Dot(val kind: Kind, val figureId: Long, val segmentId: Int, val at: Pt)

    var listener: Listener? = null
    var animation: Animation = Animation("", "")
        set(value) { field = value; invalidate() }
    var frame: Frame = Frame()
        set(value) { field = value; invalidate() }
    var mode: EditMode = EditMode.POSE
        set(value) { field = value; invalidate() }
    var selectedFigureId: Long? = null
        set(value) { field = value; invalidate() }
    var selectedSegmentId: Int? = null
        set(value) { field = value; invalidate() }

    private val renderer = FrameRenderer()
    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; color = 0xFFFFFFFF.toInt() }
    private val density = resources.displayMetrics.density

    private var active: Kind? = null
    private var activeFigure = 0L
    private var activeSegment = -1
    private var offsetX = 0f
    private var offsetY = 0f
    private var downX = 0f
    private var downY = 0f
    private var gestureStarted = false
    private var moved = false

    private fun fit(): Fit = Fit.of(width.toFloat(), height.toFloat(), animation, 12f * density)

    override fun onDraw(canvas: Canvas) {
        val fit = fit()
        renderer.draw(canvas, animation, frame, fit)
        val figure = selectedFigure() ?: return
        val r = 9f * density
        for (dot in dots(figure)) {
            dotPaint.color = ContextCompat.getColor(context, colorOf(dot.kind))
            val x = fit.toView(dot.at.x)
            val y = fit.toViewY(dot.at.y)
            canvas.drawCircle(x, y, r, dotPaint)
            ringPaint.strokeWidth = 2f * density
            canvas.drawCircle(x, y, r, ringPaint)
            if (dot.segmentId >= 0 && dot.segmentId == selectedSegmentId && dot.kind != Kind.MODIFY) {
                ringPaint.strokeWidth = 3f * density
                canvas.drawCircle(x, y, r + 5f * density, ringPaint)
            }
        }
    }

    private fun colorOf(kind: Kind) = when (kind) {
        Kind.MOVE -> R.color.dotMove
        Kind.POSE -> R.color.dotPose
        Kind.STRETCH -> R.color.dotStretch
        Kind.MODIFY -> R.color.dotModify
        Kind.FLIP -> R.color.dotFlip
    }

    private fun selectedFigure(): Figure? = frame.figures.firstOrNull { it.id == selectedFigureId }

    private fun dots(f: Figure): List<Dot> {
        val placed = Kinematics.layout(f)
        return when (mode) {
            EditMode.POSE -> listOf(Dot(Kind.MOVE, f.id, -1, Pt(f.x, f.y))) +
                placed.map { Dot(Kind.POSE, f.id, it.segment.id, it.end) }
            EditMode.STRETCH -> placed.flatMap {
                listOf(Dot(Kind.MODIFY, f.id, it.segment.id, it.mid), Dot(Kind.STRETCH, f.id, it.segment.id, it.end))
            }
            EditMode.FLIP -> listOf(Dot(Kind.FLIP, f.id, -1, Pt(f.x, f.y)))
        }
    }

    private fun hitDot(vx: Float, vy: Float): Dot? {
        val figure = selectedFigure() ?: return null
        val fit = fit()
        val reach = 26f * density
        var best: Dot? = null
        var bestDistance = reach
        // 나중에 그린 점이 위에 있으므로 뒤에서부터 본다.
        for (dot in dots(figure).asReversed()) {
            val d = hypot(fit.toView(dot.at.x) - vx, fit.toViewY(dot.at.y) - vy)
            if (d <= bestDistance) {
                best = dot
                bestDistance = d - 0.01f
            }
        }
        return best
    }

    private fun figureAt(lx: Float, ly: Float, slop: Float): Figure? {
        for (figure in frame.figures.asReversed()) {
            for (p in Kinematics.layout(figure)) {
                val s = p.segment
                val half = s.thickness * figure.scale / 2f
                val hit = when (s.shape) {
                    Shape.LINE -> distanceToSegment(lx, ly, p.start, p.end) <= half + slop
                    Shape.RING -> abs(hypot(lx - p.mid.x, ly - p.mid.y) - s.length * figure.scale / 2f) <= half + slop
                    Shape.DISC -> hypot(lx - p.mid.x, ly - p.mid.y) <= s.length * figure.scale / 2f + slop
                }
                if (hit) return figure
            }
        }
        return null
    }

    private fun distanceToSegment(px: Float, py: Float, a: Pt, b: Pt): Float {
        val dx = b.x - a.x
        val dy = b.y - a.y
        val len2 = dx * dx + dy * dy
        val t = if (len2 == 0f) 0f else max(0f, min(1f, ((px - a.x) * dx + (py - a.y) * dy) / len2))
        return hypot(px - (a.x + t * dx), py - (a.y + t * dy))
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        val fit = fit()
        val lx = fit.toLogicalX(e.x)
        val ly = fit.toLogicalY(e.y)
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                parent?.requestDisallowInterceptTouchEvent(true)
                downX = e.x; downY = e.y
                moved = false
                gestureStarted = false
                val dot = hitDot(e.x, e.y)
                if (dot != null) {
                    active = dot.kind
                    activeFigure = dot.figureId
                    activeSegment = dot.segmentId
                    offsetX = dot.at.x - lx
                    offsetY = dot.at.y - ly
                    if (dot.segmentId >= 0 && dot.kind != Kind.MODIFY) {
                        selectedSegmentId = dot.segmentId
                        listener?.onSelectionChanged(dot.figureId, dot.segmentId)
                    }
                } else {
                    active = null
                    val hit = figureAt(lx, ly, 14f * density / fit.scale)
                    selectedFigureId = hit?.id
                    selectedSegmentId = null
                    listener?.onSelectionChanged(hit?.id, null)
                }
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val kind = active ?: return true
                if (!moved && hypot(e.x - downX, e.y - downY) > 8f * density) moved = true
                if (!moved) return true
                if (kind == Kind.MODIFY || kind == Kind.FLIP) return true
                val figure = frame.figures.firstOrNull { it.id == activeFigure } ?: return true
                val target = Pt(lx + offsetX, ly + offsetY)
                val edited = when (kind) {
                    Kind.MOVE -> FigureEdit.move(figure, target)
                    Kind.POSE -> FigureEdit.pose(figure, activeSegment, target)
                    Kind.STRETCH -> FigureEdit.stretch(figure, activeSegment, target)
                    else -> figure
                }
                if (!gestureStarted) {
                    gestureStarted = true
                    listener?.onGestureStart()
                }
                replace(edited)
                return true
            }
            MotionEvent.ACTION_UP -> {
                val kind = active
                if (kind == Kind.MODIFY && !moved) {
                    listener?.onModifyRequested(activeFigure, activeSegment)
                } else if (kind == Kind.FLIP && !moved) {
                    frame.figures.firstOrNull { it.id == activeFigure }?.let {
                        listener?.onGestureStart()
                        replace(FigureEdit.flip(it))
                        listener?.onGestureEnd()
                    }
                } else if (gestureStarted) {
                    listener?.onGestureEnd()
                }
                active = null
                gestureStarted = false
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                if (gestureStarted) listener?.onGestureEnd()
                active = null
                gestureStarted = false
                return true
            }
        }
        return super.onTouchEvent(e)
    }

    private fun replace(figure: Figure) {
        frame = Frame(frame.figures.map { if (it.id == figure.id) figure else it })
        listener?.onFrameChanged(frame)
    }
}
