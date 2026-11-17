package com.kjooy0305.adskip

import android.content.Context
import android.content.SharedPreferences

/** 설정과 통계. SharedPreferences 하나로 서비스와 화면이 함께 쓴다. */
class Prefs(context: Context) {
    val sp: SharedPreferences = context.getSharedPreferences("adskip", Context.MODE_PRIVATE)

    /** 유튜브 앱 외에 브라우저(크롬 등)에서 보는 유튜브도 처리할지. */
    var browsers: Boolean
        get() = sp.getBoolean(KEY_BROWSERS, false)
        set(v) = sp.edit().putBoolean(KEY_BROWSERS, v).apply()

    /** 건너뛸 때 화면 아래에 작게 알려 줄지. */
    var toast: Boolean
        get() = sp.getBoolean(KEY_TOAST, true)
        set(v) = sp.edit().putBoolean(KEY_TOAST, v).apply()

    /** 지금까지 건너뛴 광고 수. */
    var count: Int
        get() = sp.getInt(KEY_COUNT, 0)
        set(v) = sp.edit().putInt(KEY_COUNT, v).apply()

    /** 최근 기록: "시각|방법" 줄바꿈 구분, 최대 30줄. */
    var log: List<String>
        get() = sp.getString(KEY_LOG, "")!!.split('\n').filter { it.isNotBlank() }
        set(v) = sp.edit().putString(KEY_LOG, v.takeLast(30).joinToString("\n")).apply()

    fun recordSkip(line: String) {
        count += 1
        log = log + line
    }

    fun resetStats() {
        sp.edit().remove(KEY_COUNT).remove(KEY_LOG).apply()
    }

    /** 서비스가 이벤트를 받아볼 앱 패키지 목록. */
    fun targetPackages(): Array<String> {
        val list = mutableListOf(YT_PKG)
        if (browsers) list += BROWSER_PKGS
        return list.toTypedArray()
    }

    companion object {
        const val KEY_BROWSERS = "browsers"
        const val KEY_TOAST = "toast"
        const val KEY_COUNT = "count"
        const val KEY_LOG = "log"
        const val YT_PKG = "com.google.android.youtube"
        val BROWSER_PKGS = listOf(
            "com.android.chrome",
            "com.chrome.beta",
            "com.sec.android.app.sbrowser",
            "com.microsoft.emmx",
            "org.mozilla.firefox",
            "com.naver.whale",
            "com.nhn.android.search",
            "com.brave.browser",
            "com.kiwibrowser.browser",
        )
    }
}
