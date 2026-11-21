package com.kjooy0305.screenkeep

import android.content.Context

object Prefs {
    private fun sp(c: Context) = c.applicationContext.getSharedPreferences("screenkeep", Context.MODE_PRIVATE)

    /** 지금 화면 유지를 켜 둔 상태인지(사용자가 끄기 전까지 true). */
    fun enabled(c: Context) = sp(c).getBoolean("enabled", false)
    fun setEnabled(c: Context, v: Boolean) = sp(c).edit().putBoolean("enabled", v).apply()

    /** 유지가 끝나는 시각(밀리초). 0이면 계속. */
    fun endAt(c: Context) = sp(c).getLong("endAt", 0L)
    fun setEndAt(c: Context, v: Long) = sp(c).edit().putLong("endAt", v).apply()

    /** 고른 유지 시간(분). 0이면 계속. */
    fun durationMin(c: Context) = sp(c).getInt("durationMin", 0)
    fun setDurationMin(c: Context, v: Int) = sp(c).edit().putInt("durationMin", v).apply()

    fun autoStart(c: Context) = sp(c).getBoolean("autoStart", true)
    fun setAutoStart(c: Context, v: Boolean) = sp(c).edit().putBoolean("autoStart", v).apply()

    fun logs(c: Context): List<LogEntry> = LogEntry.parse(sp(c).getString("log", null))
    fun addLog(c: Context, e: LogEntry) = synchronized(this) { sp(c).edit().putString("log", LogEntry.serialize(LogEntry.append(logs(c), e))).apply() }
    fun clearLogs(c: Context) = sp(c).edit().remove("log").apply()
}
