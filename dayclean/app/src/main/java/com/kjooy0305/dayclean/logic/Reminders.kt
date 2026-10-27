package com.kjooy0305.dayclean.logic

import com.kjooy0305.dayclean.model.CleanupType
import com.kjooy0305.dayclean.model.Item
import com.kjooy0305.dayclean.model.Kind
import com.kjooy0305.dayclean.model.MORNING_ID
import com.kjooy0305.dayclean.model.Settings

/** 항목 알림이 처음 울릴 시각과 그 뒤 간격. */
object Reminders {
    const val INTERVAL_MS = 10 * 60_000L

    /** 알림이 필요한 항목인가. 메모, 해결한 것, 회고에서 처리한 것은 울리지 않는다. */
    fun needsAlert(item: Item): Boolean = item.kind != Kind.MEMO && !item.done && !item.decided

    /**
     * 처음 울릴 시각.
     *  • : 쓰고 10분 뒤. 어제 넘긴 것이면 아침 정리 시간부터.
     *  - : 고른 정리 시간부터. 이미 지났으면 쓰고 10분 뒤.
     */
    fun initialAlert(item: Item, settings: Settings, nowMs: Long): Long? {
        if (!needsAlert(item)) return null
        val raw = when (item.kind) {
            Kind.NOW -> if (item.carried) morningStart(item, settings) else item.createdAt + INTERVAL_MS
            Kind.LATER -> {
                val start = laterStart(item, settings)
                if (item.carried || item.createdAt < start) start else item.createdAt + INTERVAL_MS
            }
            Kind.MEMO -> return null
        }
        return if (raw > nowMs) raw else nowMs + 60_000L
    }

    private fun morningStart(item: Item, settings: Settings): Long {
        val m = settings.cleanups.firstOrNull { it.id == MORNING_ID } ?: settings.cleanups.firstOrNull { it.type == CleanupType.MORNING }
        return Dates.toMillis(item.date, m?.minuteOfDay ?: 7 * 60)
    }

    fun laterStart(item: Item, settings: Settings): Long {
        val c = settings.cleanup(item.startCleanupId) ?: settings.cleanup(settings.defaultLaterStartId)
            ?: settings.cleanups.first()
        return Dates.toMillis(item.date, c.minuteOfDay)
    }

    fun title(item: Item): String = "${item.kind.glyph} ${item.text}"

    fun body(item: Item): String = when (item.kind) {
        Kind.NOW -> "지금 해야 할 것"
        Kind.LATER -> "오늘 안에 해야 할 것"
        Kind.MEMO -> ""
    }
}
