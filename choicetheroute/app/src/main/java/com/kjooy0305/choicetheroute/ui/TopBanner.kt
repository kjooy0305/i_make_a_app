package com.kjooy0305.choicetheroute.ui

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.kjooy0305.choicetheroute.R

/**
 * 화면 위쪽에 잠깐 떠올랐다 사라지는 알림.
 *
 * Snackbar는 화면 아래에 뜨면서 하단 액션 패널과 FAB를 가려 버려서
 * 대신 쓴다. 액티비티의 content 프레임에 직접 붙이므로 레이아웃마다
 * 따로 뷰를 둘 필요가 없다.
 */
object TopBanner {

    private const val TAG = "topBanner"
    private const val SHORT_MS = 1900L
    private const val LONG_MS = 3200L

    private val handler = Handler(Looper.getMainLooper())

    fun show(activity: Activity, message: String, longer: Boolean = false) {
        val root = activity.findViewById<FrameLayout>(android.R.id.content) ?: return
        val d = activity.resources.displayMetrics.density
        val shift = -12f * d

        val tv = root.findViewWithTag<TextView>(TAG) ?: TextView(activity).apply {
            tag = TAG
            setBackgroundResource(R.drawable.bg_banner)
            setTextColor(ContextCompat.getColor(activity, R.color.text_primary))
            textSize = 14f
            maxLines = 2
            gravity = Gravity.CENTER
            elevation = 12f * d
            maxWidth = activity.resources.displayMetrics.widthPixels - (56 * d).toInt()
            val ph = (18 * d).toInt(); val pv = (10 * d).toInt()
            setPadding(ph, pv, ph, pv)
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.TOP or Gravity.CENTER_HORIZONTAL
            ).apply { topMargin = (72 * d).toInt() }
            root.addView(this)
        }

        handler.removeCallbacksAndMessages(tv)
        tv.animate().cancel()
        tv.text = message
        tv.alpha = 0f
        tv.translationY = shift
        tv.visibility = View.VISIBLE
        tv.bringToFront()
        tv.animate().alpha(1f).translationY(0f).setDuration(160L).start()

        handler.postAtTime(
            {
                tv.animate().alpha(0f).translationY(shift).setDuration(180L)
                    .withEndAction { tv.visibility = View.GONE }.start()
            },
            tv,
            SystemClock.uptimeMillis() + if (longer) LONG_MS else SHORT_MS
        )
    }
}
