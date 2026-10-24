package com.kjooy0305.stickfighter.ui

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.kjooy0305.stickfighter.R

object Dialogs {

    fun dp(context: Context, v: Int) = (v * context.resources.displayMetrics.density).toInt()

    /** 한 줄 입력 대화상자. 비어 있으면 오류를 보여 주고 닫지 않는다. */
    fun prompt(context: Context, title: Int, hint: Int, initial: String, error: Int, onOk: (String) -> Unit) {
        val input = EditText(context).apply {
            setText(initial)
            setSelection(initial.length)
            this.hint = context.getString(hint)
            inputType = InputType.TYPE_CLASS_TEXT
            setSingleLine()
        }
        val holder = LinearLayout(context).apply {
            setPadding(dp(context, 20), dp(context, 8), dp(context, 20), 0)
            addView(input, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        }
        val dialog = AlertDialog.Builder(context)
            .setTitle(title)
            .setView(holder)
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel, null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val text = input.text.toString().trim()
                if (text.isEmpty()) {
                    input.error = context.getString(error)
                } else {
                    dialog.dismiss()
                    onOk(text)
                }
            }
        }
        dialog.show()
    }

    fun message(context: Context, text: String, onClose: (() -> Unit)? = null) {
        AlertDialog.Builder(context)
            .setMessage(text)
            .setPositiveButton(R.string.ok) { _, _ -> onClose?.invoke() }
            .show()
    }

    fun confirm(context: Context, text: String, action: Int, onYes: () -> Unit) {
        AlertDialog.Builder(context)
            .setMessage(text)
            .setPositiveButton(action) { _, _ -> onYes() }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private val palette = intArrayOf(
        0xFF222222.toInt(), 0xFF555555.toInt(), 0xFF999999.toInt(), 0xFFFFFFFF.toInt(),
        0xFFD32F2F.toInt(), 0xFFF57C00.toInt(), 0xFFFBC02D.toInt(), 0xFF7CB342.toInt(),
        0xFF2E7D32.toInt(), 0xFF00897B.toInt(), 0xFF0288D1.toInt(), 0xFF1565C0.toInt(),
        0xFF5E35B1.toInt(), 0xFF8E24AA.toInt(), 0xFFD81B60.toInt(), 0xFF5D4037.toInt(),
        0xFFFFCDD2.toInt(), 0xFFFFE0B2.toInt(), 0xFFFFF59D.toInt(), 0xFFC8E6C9.toInt(),
        0xFFB3E5FC.toInt(), 0xFFD1C4E9.toInt(), 0xFF263238.toInt(), 0xFF000000.toInt()
    )

    /** 색 고르기 영역(팔레트 + RGB 슬라이더). 현재 색은 color()로 읽는다. */
    class ColorPane(context: Context, initial: Int) {
        val view: LinearLayout = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
        private var current = initial or 0xFF000000.toInt()
        private val preview = View(context)
        private val bars = ArrayList<SeekBar>()
        private var updating = false

        init {
            val grid = GridLayout(context).apply { columnCount = 8 }
            val size = dp(context, 34)
            for (c in palette) {
                val swatch = View(context).apply {
                    background = GradientDrawable().apply {
                        setColor(c)
                        setStroke(dp(context, 1), 0xFF888888.toInt())
                        cornerRadius = dp(context, 4).toFloat()
                    }
                    contentDescription = String.format("#%06X", c and 0xFFFFFF)
                    setOnClickListener { set(c) }
                }
                grid.addView(swatch, GridLayout.LayoutParams().apply {
                    width = size; height = size
                    setMargins(dp(context, 3), dp(context, 3), dp(context, 3), dp(context, 3))
                })
            }
            view.addView(grid)
            preview.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(context, 28)).apply {
                topMargin = dp(context, 8)
            }
            view.addView(preview)
            for (channel in 0..2) {
                val bar = SeekBar(context).apply {
                    max = 255
                    setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) {
                            if (!fromUser || updating) return
                            set(Color.rgb(bars[0].progress, bars[1].progress, bars[2].progress))
                        }
                        override fun onStartTrackingTouch(s: SeekBar?) {}
                        override fun onStopTrackingTouch(s: SeekBar?) {}
                    })
                }
                bars.add(bar)
                view.addView(bar)
            }
            set(current)
        }

        private fun set(color: Int) {
            current = color or 0xFF000000.toInt()
            updating = true
            bars[0].progress = Color.red(current)
            bars[1].progress = Color.green(current)
            bars[2].progress = Color.blue(current)
            updating = false
            preview.setBackgroundColor(current)
        }

        fun color(): Int = current
    }

    fun pickColor(context: Context, title: Int, initial: Int, onPick: (Int) -> Unit) {
        val pane = ColorPane(context, initial)
        pane.view.setPadding(dp(context, 20), dp(context, 8), dp(context, 20), 0)
        AlertDialog.Builder(context)
            .setTitle(title)
            .setView(pane.view)
            .setPositiveButton(R.string.apply) { _, _ -> onPick(pane.color()) }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    /** 라벨이 붙은 SeekBar 한 줄. 값이 바뀔 때마다 label을 갱신한다. */
    fun labeledSeek(context: Context, max: Int, start: Int, label: (Int) -> String): Pair<LinearLayout, SeekBar> {
        val text = TextView(context).apply { textSize = 14f }
        val bar = SeekBar(context).apply {
            this.max = max
            progress = start
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) { text.text = label(p) }
                override fun onStartTrackingTouch(s: SeekBar?) {}
                override fun onStopTrackingTouch(s: SeekBar?) {}
            })
        }
        text.text = label(start)
        val row = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(text)
            addView(bar)
        }
        return row to bar
    }
}
