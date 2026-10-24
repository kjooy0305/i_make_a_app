package com.kjooy0305.stickfighter.ui

import android.annotation.SuppressLint
import android.view.Choreographer
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import com.kjooy0305.stickfighter.R
import com.kjooy0305.stickfighter.model.Animation

/**
 * 편집 화면 위에 떠 있는 작은 미리보기 창. 편집을 막지 않고, 고치는 대로 바로 반영된다.
 * 지금 장면에서 다음 장면으로 넘어가는 모습(전환)이나 전체 장면을 되풀이해서 보여 준다.
 */
class PreviewWindow(private val root: View) : Choreographer.FrameCallback {
    private val header: View = root.findViewById(R.id.previewHeader)
    private val player: PlayerView = root.findViewById(R.id.previewPlayer)
    private val label: TextView = root.findViewById(R.id.previewLabel)
    private val playButton: ImageButton = root.findViewById(R.id.previewPlay)
    private val seek: SeekBar = root.findViewById(R.id.previewSeek)
    private val scopeButton: TextView = root.findViewById(R.id.previewScope)
    private val sizeButton: ImageButton = root.findViewById(R.id.previewSize)
    private val closeButton: ImageButton = root.findViewById(R.id.previewClose)
    private val density = root.resources.displayMetrics.density

    private var anim = Animation("", "")
    private var index = 0
    private var scope = PreviewRange.Scope.TRANSITION
    private var range = PreviewRange.Range(0f, 0f)
    private var position = 0f
    private var holdLeft = 0f
    private var playing = true
    private var lastNanos = 0L
    private var big = false
    private var running = false

    var onClose: (() -> Unit)? = null

    val isShowing: Boolean get() = root.visibility == View.VISIBLE

    init {
        playButton.setOnClickListener { setPlaying(!playing) }
        scopeButton.setOnClickListener {
            scope = if (scope == PreviewRange.Scope.TRANSITION) PreviewRange.Scope.ALL else PreviewRange.Scope.TRANSITION
            recompute(reset = true)
        }
        sizeButton.setOnClickListener { big = !big; applySize() }
        closeButton.setOnClickListener { onClose?.invoke() }
        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) {
                if (!fromUser) return
                setPlaying(false)
                position = range.start + range.length * p / 1000f
                player.position = position
            }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })
        enableDrag()
        applySize()
    }

    fun show() {
        root.visibility = View.VISIBLE
        recompute(reset = true)
        start()
    }

    fun hide() {
        root.visibility = View.GONE
        stop()
    }

    fun resume() { if (isShowing) start() }

    fun pause() = stop()

    /** 편집 내용이나 선택한 장면이 바뀔 때마다 부른다. */
    fun update(animation: Animation, frameIndex: Int) {
        val ratioChanged = anim.ratio != animation.ratio
        val indexChanged = index != frameIndex
        anim = animation
        index = frameIndex
        player.animation = animation
        if (ratioChanged) applySize()
        recompute(reset = indexChanged)
    }

    private fun recompute(reset: Boolean) {
        range = PreviewRange.of(anim.frames.size, index, scope)
        if (reset || position < range.start || position > range.end) {
            position = range.start
            holdLeft = 0f
        }
        scopeButton.text = root.context.getString(
            if (scope == PreviewRange.Scope.TRANSITION) R.string.preview_transition else R.string.preview_all)
        label.text = when {
            anim.frames.size < 2 -> root.context.getString(R.string.preview_single)
            scope == PreviewRange.Scope.ALL -> root.context.getString(R.string.preview_all_label)
            else -> root.context.getString(R.string.preview_transition_label, range.start.toInt() + 1, range.end.toInt() + 1)
        }
        seek.isEnabled = range.length > 0f
        showPosition()
        if (isShowing && playing) start()
    }

    private fun showPosition() {
        player.position = position
        seek.progress = if (range.length <= 0f) 0 else ((position - range.start) / range.length * 1000f).toInt()
    }

    private fun setPlaying(value: Boolean) {
        playing = value
        playButton.setImageResource(if (value) R.drawable.ic_pause else R.drawable.ic_play)
        playButton.contentDescription = root.context.getString(if (value) R.string.pause else R.string.play)
        lastNanos = 0L
        if (value) start()
    }

    private fun start() {
        if (running || !playing || range.length <= 0f) return
        running = true
        lastNanos = 0L
        Choreographer.getInstance().postFrameCallback(this)
    }

    private fun stop() {
        running = false
        Choreographer.getInstance().removeFrameCallback(this)
    }

    override fun doFrame(frameTimeNanos: Long) {
        if (!running) return
        if (!playing || !isShowing || range.length <= 0f) { running = false; return }
        val dt = if (lastNanos == 0L) 0f else (frameTimeNanos - lastNanos) / 1_000_000_000f
        lastNanos = frameTimeNanos
        if (holdLeft > 0f) {
            // 끝 장면에서 잠깐 멈췄다가 처음으로 돌아간다.
            holdLeft -= dt
            if (holdLeft <= 0f) { holdLeft = 0f; position = range.start }
        } else {
            position += dt * anim.keyFramesPerSecond
            if (position >= range.end) { position = range.end; holdLeft = HOLD_SECONDS }
        }
        showPosition()
        Choreographer.getInstance().postFrameCallback(this)
    }

    private fun applySize() {
        val width = ((if (big) 380 else 220) * density).toInt()
        val height = (width * anim.ratio).toInt().coerceAtLeast(1)
        player.layoutParams = player.layoutParams.apply { this.width = width; this.height = height }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun enableDrag() {
        var startX = 0f
        var startY = 0f
        var baseX = 0f
        var baseY = 0f
        header.setOnTouchListener { _, e ->
            val parent = root.parent as? View ?: return@setOnTouchListener false
            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    startX = e.rawX; startY = e.rawY
                    baseX = root.translationX; baseY = root.translationY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val minX = -root.left.toFloat()
                    val maxX = (parent.width - root.width - root.left).toFloat()
                    val minY = -root.top.toFloat()
                    val maxY = (parent.height - root.height - root.top).toFloat()
                    root.translationX = (baseX + e.rawX - startX).coerceIn(minX, maxOf(minX, maxX))
                    root.translationY = (baseY + e.rawY - startY).coerceIn(minY, maxOf(minY, maxY))
                    true
                }
                else -> true
            }
        }
    }

    companion object {
        private const val HOLD_SECONDS = 0.35f
    }
}
