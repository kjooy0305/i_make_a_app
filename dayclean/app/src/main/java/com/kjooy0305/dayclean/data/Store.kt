package com.kjooy0305.dayclean.data

import android.content.Context
import com.kjooy0305.dayclean.logic.Dates
import com.kjooy0305.dayclean.logic.Engine
import com.kjooy0305.dayclean.logic.EngineResult
import com.kjooy0305.dayclean.model.AppState
import com.kjooy0305.dayclean.model.ModeState
import org.json.JSONObject
import java.io.File
import java.time.LocalDate
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 앱의 모든 기록을 한 파일(JSON)에 둔다. 화면과 알람 수신기가 같은 인스턴스를 쓰므로 변경은 한 줄씩 차례로 일어난다.
 * 쓰는 도중 꺼져도 기존 파일이 깨지지 않게 임시 파일에 쓴 뒤 바꾼다.
 */
class Store(private val file: File, private val clock: () -> Long = { System.currentTimeMillis() }) {

    @Volatile
    var state: AppState = load()
        private set

    private val listeners = CopyOnWriteArrayList<() -> Unit>()

    fun now(): Long = clock()

    fun today(): LocalDate = Dates.dateOf(clock())

    fun addListener(l: () -> Unit) { listeners.add(l) }
    fun removeListener(l: () -> Unit) { listeners.remove(l) }

    @Synchronized
    fun update(change: (AppState) -> AppState): AppState {
        state = change(state)
        save()
        listeners.forEach { it() }
        return state
    }

    /** 날짜가 바뀌었으면 메모리로 옮기고, 지금 울릴 알림을 계산해서 저장한다. */
    @Synchronized
    fun process(): EngineResult {
        val result = Engine.process(state, clock())
        state = result.state
        save()
        listeners.forEach { it() }
        return result
    }

    @Synchronized
    private fun save() {
        val text = StateJson.toJson(state).toString()
        val tmp = File(file.parentFile, file.name + ".tmp")
        tmp.writeText(text)
        if (!tmp.renameTo(file)) { file.writeText(text); tmp.delete() }
    }

    private fun load(): AppState {
        if (file.exists()) {
            runCatching { return StateJson.fromJson(JSONObject(file.readText())) }
        }
        return AppState(mode = ModeState(Dates.dateOf(clock())))
    }

    companion object {
        @Volatile private var instance: Store? = null

        fun get(context: Context): Store = instance ?: synchronized(this) {
            instance ?: Store(File(context.applicationContext.filesDir, "dayclean.json")).also { instance = it }
        }

        /** 시험에서 새 인스턴스로 바꾼다. */
        fun reset(store: Store?) { instance = store }
    }
}
