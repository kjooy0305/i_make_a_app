package com.kjooy0305.screenkeep

import org.junit.Assert.*
import org.junit.Test

class LogicTest {
    @Test fun durationLabels() {
        assertEquals("계속", Logic.durationLabel(0))
        assertEquals("15분", Logic.durationLabel(15))
        assertEquals("1시간", Logic.durationLabel(60))
        assertEquals("1시간 30분", Logic.durationLabel(90))
        assertEquals("4시간", Logic.durationLabel(240))
    }

    @Test fun timeoutLabels() {
        assertEquals("15초", Logic.timeoutLabel(15_000))
        assertEquals("1분", Logic.timeoutLabel(60_000))
        assertEquals("30분", Logic.timeoutLabel(1_800_000))
    }

    @Test fun endAndExpiry() {
        assertEquals(0L, Logic.endAtFor(1000, 0))
        assertEquals(1000L + 15 * 60_000, Logic.endAtFor(1000, 15))
        assertFalse(Logic.isExpired(0, Long.MAX_VALUE))           // 계속은 끝나지 않는다
        assertFalse(Logic.isExpired(5000, 4999))
        assertTrue(Logic.isExpired(5000, 5000))
    }

    @Test fun remainingText() {
        assertEquals("전원 버튼을 누를 때까지 계속", Logic.remainingText(0, 123))
        assertTrue(Logic.remainingText(1_000_000 + 90 * 60_000, 1_000_000).contains("1시간 30분 남음"))
        assertTrue(Logic.remainingText(1_000_000 + 30_000, 1_000_000).contains("1분 남음")) // 올림
        assertEquals("곧 종료", Logic.remainingText(1000, 2000))
    }

    @Test fun logRoundTripAndTrim() {
        var list = emptyList<LogEntry>()
        for (i in 1..(LogEntry.MAX + 20)) list = LogEntry.append(list, LogEntry(i.toLong(), "화면 꺼짐", true, 50, false, false))
        assertEquals(LogEntry.MAX, list.size)
        assertEquals(21L, list.first().t)                          // 오래된 것부터 버린다
        val back = LogEntry.parse(LogEntry.serialize(list))
        assertEquals(list, back)
        assertTrue(back.first().unexpected)
        assertFalse(LogEntry(1, "화면 꺼짐", false, 1, false, false).unexpected)
        assertFalse(LogEntry(1, "화면 켜짐", true, 1, false, false).unexpected)
        assertTrue(LogEntry.parse("깨진 글").isEmpty())
        assertTrue(LogEntry.parse(null).isEmpty())
    }

    @Test fun logLine() {
        val l = LogEntry(0, "화면 꺼짐", true, 62, true, true).line()
        assertTrue(l.contains("배터리 62% 충전 중"))
        assertTrue(l.contains("절전 모드"))
        assertTrue(LogEntry(0, "켜기", false, -1, false, false).line().let { !it.contains("배터리") })
    }
}
