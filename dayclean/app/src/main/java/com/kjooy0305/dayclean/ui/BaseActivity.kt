package com.kjooy0305.dayclean.ui

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.core.view.WindowCompat
import com.kjooy0305.dayclean.data.Store
import com.kjooy0305.dayclean.logic.Dates
import com.kjooy0305.dayclean.model.AppState
import com.kjooy0305.dayclean.platform.AlarmScheduler
import java.time.LocalDate

/** 모든 화면의 바탕: 글씨 크기 설정 적용, 흰 상태 표시줄, 저장소 접근. */
abstract class BaseActivity : ComponentActivity() {
    protected val store: Store get() = Store.get(this)

    override fun attachBaseContext(newBase: Context) {
        val scale = runCatching { Store.get(newBase).state.settings.fontScale }.getOrDefault(1f)
        val cfg = Configuration(newBase.resources.configuration)
        cfg.fontScale = scale
        super.attachBaseContext(newBase.createConfigurationContext(cfg))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = C.WHITE
        window.navigationBarColor = C.WHITE
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    protected fun show(root: View) {
        root.setBackgroundColor(C.WHITE)
        setContentView(root)
        WindowCompat.getInsetsController(window, root).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }
    }

    protected val state: AppState get() = store.state
    protected fun today(): LocalDate = Dates.dateOf(store.now())

    /** 기록을 바꾸고 알람도 다시 건다. */
    protected fun mutate(change: (AppState) -> AppState): AppState {
        val s = store.update(change)
        AlarmScheduler.schedule(applicationContext)
        return s
    }

    /** 위쪽 막대: 뒤로 가기 버튼(선택), 제목, 오른쪽 보조 글씨. */
    protected fun topBar(title: String, back: Boolean, right: View? = null): View = hbox().apply {
        setPadding(dp(8), dp(6), dp(16), dp(8))
        if (back) {
            add(button("←") { finish() }.apply { contentDescription = "뒤로" }, WRAP, WRAP, 0f, 0, 0, 8, 0)
        } else add(View(this@BaseActivity), dp(8), 1)
        add(label(title, 19f, bold = true), WRAP, WRAP, 1f)
        if (right != null) add(right)
    }

    /** 위는 막대, 아래는 내용, 맨 아래는 고정 버튼 줄인 기본 틀. */
    protected fun page(title: String, back: Boolean, right: View? = null, content: View, bottom: View? = null): View =
        vbox().apply {
            add(topBar(title, back, right))
            add(content, MATCH, 0, 1f)
            if (bottom != null) {
                add(hline(2f))
                add(bottom.apply { pad(16, 10, 16, 10) })
            }
        }

    protected fun scrollOf(inner: View): ScrollViewCompat = ScrollViewCompat(this).also { it.addView(inner, MATCH, WRAP) }
}

/** 안쪽 여백을 준 스크롤 화면. */
class ScrollViewCompat(context: Context) : android.widget.ScrollView(context) {
    init { isVerticalScrollBarEnabled = true; clipToPadding = false; setPadding(context.dp(16), 0, context.dp(16), context.dp(24)) }
}
