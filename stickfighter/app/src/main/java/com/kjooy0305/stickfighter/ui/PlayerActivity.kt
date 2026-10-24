package com.kjooy0305.stickfighter.ui

import android.os.Bundle
import android.view.Choreographer
import android.view.WindowManager
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.kjooy0305.stickfighter.R
import com.kjooy0305.stickfighter.data.Repository
import com.kjooy0305.stickfighter.databinding.ActivityPlayerBinding
import com.kjooy0305.stickfighter.model.Animation

/** 전체 화면 재생. 시간은 Choreographer가 주는 프레임 시각으로 잰다. */
class PlayerActivity : AppCompatActivity(), Choreographer.FrameCallback {

    private lateinit var b: ActivityPlayerBinding
    private lateinit var repo: Repository
    private lateinit var anim: Animation
    private var position = 0f
    private var playing = true
    private var lastNanos = 0L
    private var lastKey = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(b.root)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, b.root).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        repo = Repository(filesDir)
        anim = repo.animation(intent.getStringExtra(EXTRA_ANIMATION) ?: "") ?: run { finish(); return }
        lastKey = (anim.frames.size - 1).coerceAtLeast(0).toFloat()
        b.playerView.animation = anim

        b.closeButton.setOnClickListener { finish() }
        b.playPause.setOnClickListener {
            if (!playing && position >= lastKey) position = 0f
            setPlaying(!playing)
        }
        b.repeatButton.isSelected = anim.repeat
        b.repeatButton.setOnClickListener {
            anim = anim.copy(repeat = !anim.repeat)
            b.repeatButton.isSelected = anim.repeat
        }
        b.slower.setOnClickListener { changeSpeed(-0.5f) }
        b.faster.setOnClickListener { changeSpeed(0.5f) }
        b.progress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) {
                if (fromUser) {
                    position = lastKey * p / 1000f
                    b.playerView.position = position
                }
            }
            override fun onStartTrackingTouch(s: SeekBar?) { setPlaying(false) }
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })
        updateSpeedText()
    }

    override fun onStart() {
        super.onStart()
        lastNanos = 0L
        if (playing) Choreographer.getInstance().postFrameCallback(this)
    }

    override fun onStop() {
        super.onStop()
        Choreographer.getInstance().removeFrameCallback(this)
        repo.animation(anim.id)?.let { saved ->
            // 속도와 반복 설정만 저장한다. 그 사이 편집된 내용은 건드리지 않는다.
            repo.save(saved.copy(keyFramesPerSecond = anim.keyFramesPerSecond, repeat = anim.repeat), saved.updatedAt)
        }
    }

    private fun setPlaying(value: Boolean) {
        playing = value
        b.playPause.setImageResource(if (value) R.drawable.ic_pause else R.drawable.ic_play)
        b.playPause.contentDescription = getString(if (value) R.string.pause else R.string.play)
        lastNanos = 0L
        if (value) Choreographer.getInstance().postFrameCallback(this)
    }

    private fun changeSpeed(delta: Float) {
        anim = anim.copy(keyFramesPerSecond = (anim.keyFramesPerSecond + delta).coerceIn(0.5f, 10f))
        updateSpeedText()
    }

    private fun updateSpeedText() {
        b.speedText.text = getString(R.string.player_speed, anim.keyFramesPerSecond)
    }

    override fun doFrame(frameTimeNanos: Long) {
        if (!playing) return
        if (lastNanos != 0L) {
            position += (frameTimeNanos - lastNanos) / 1_000_000_000f * anim.keyFramesPerSecond
            if (position >= lastKey) {
                if (anim.repeat && lastKey > 0f) {
                    position %= lastKey
                } else {
                    position = lastKey
                    b.playerView.position = position
                    b.progress.progress = 1000
                    setPlaying(false)
                    return
                }
            }
        }
        lastNanos = frameTimeNanos
        b.playerView.position = position
        b.progress.progress = if (lastKey == 0f) 0 else (position / lastKey * 1000).toInt()
        Choreographer.getInstance().postFrameCallback(this)
    }

    companion object {
        const val EXTRA_ANIMATION = "animation"
    }
}
