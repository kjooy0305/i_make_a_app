package com.kjooy0305.padmacro

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * 매크로 목록을 앱 내부 파일(macros.json)에 저장한다.
 * 설정(선택된 매크로, 패널 표시 여부 등)은 SharedPreferences.
 */
class MacroStore(context: Context) {
    private val file = File(context.filesDir, "macros.json")
    private val prefs = context.getSharedPreferences("padmacro", Context.MODE_PRIVATE)

    fun loadAll(): MutableList<Macro> {
        if (!file.exists()) return mutableListOf()
        return try {
            val arr = JSONArray(file.readText())
            MutableList(arr.length()) { Macro.fromJson(arr.getJSONObject(it)) }
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    fun saveAll(list: List<Macro>) {
        val arr = JSONArray()
        list.forEach { arr.put(it.toJson()) }
        file.writeText(arr.toString())
    }

    fun get(id: String): Macro? = loadAll().firstOrNull { it.id == id }

    /** 같은 id 가 있으면 바꾸고 없으면 끝에 추가 */
    fun put(macro: Macro) {
        val list = loadAll()
        val idx = list.indexOfFirst { it.id == macro.id }
        if (idx >= 0) list[idx] = macro else list.add(macro)
        saveAll(list)
    }

    fun delete(id: String) {
        saveAll(loadAll().filterNot { it.id == id })
        if (selectedId == id) selectedId = null
    }

    fun move(id: String, delta: Int) {
        val list = loadAll()
        val i = list.indexOfFirst { it.id == id }
        val j = i + delta
        if (i < 0 || j < 0 || j >= list.size) return
        val m = list.removeAt(i); list.add(j, m)
        saveAll(list)
    }

    /** 플로팅 패널·볼륨키로 실행할 매크로 */
    var selectedId: String?
        get() = prefs.getString("selected", null)
        set(v) = prefs.edit().putString("selected", v).apply()

    fun selected(): Macro? {
        val list = loadAll()
        return list.firstOrNull { it.id == selectedId } ?: list.firstOrNull()
    }

    var showPanel: Boolean
        get() = prefs.getBoolean("panel", true)
        set(v) = prefs.edit().putBoolean("panel", v).apply()

    var volumeTrigger: Boolean
        get() = prefs.getBoolean("volume", false)
        set(v) = prefs.edit().putBoolean("volume", v).apply()

    var toast: Boolean
        get() = prefs.getBoolean("toast", true)
        set(v) = prefs.edit().putBoolean("toast", v).apply()

    /** 녹화할 때 터치 사이 시간을 그대로 기록할지(끄면 gapMs 만 사용) */
    var recordTiming: Boolean
        get() = prefs.getBoolean("recordTiming", true)
        set(v) = prefs.edit().putBoolean("recordTiming", v).apply()

    var panelX: Int
        get() = prefs.getInt("panelX", -1)
        set(v) = prefs.edit().putInt("panelX", v).apply()
    var panelY: Int
        get() = prefs.getInt("panelY", -1)
        set(v) = prefs.edit().putInt("panelY", v).apply()

    // ---- 최근 기록 (최대 60줄) ----
    fun log(line: String) {
        val arr = try { JSONArray(prefs.getString("log", "[]")) } catch (e: Exception) { JSONArray() }
        val time = java.text.SimpleDateFormat("MM-dd HH:mm:ss", java.util.Locale.KOREA).format(java.util.Date())
        arr.put("$time  $line")
        while (arr.length() > 60) arr.remove(0)
        prefs.edit().putString("log", arr.toString()).apply()
    }

    fun logLines(): List<String> {
        val arr = try { JSONArray(prefs.getString("log", "[]")) } catch (e: Exception) { JSONArray() }
        return List(arr.length()) { arr.getString(it) }.reversed()
    }

    fun clearLog() = prefs.edit().remove("log").apply()
}
