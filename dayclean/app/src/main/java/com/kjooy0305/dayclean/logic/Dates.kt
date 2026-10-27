package com.kjooy0305.dayclean.logic

import com.kjooy0305.dayclean.model.Period
import com.kjooy0305.dayclean.model.Tz
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.temporal.ChronoUnit

/** 시간 변환과 장기 목표의 날짜 규칙. */
object Dates {
    fun toMillis(dt: LocalDateTime): Long = dt.atZone(Tz.zone).toInstant().toEpochMilli()
    fun toMillis(date: LocalDate, minuteOfDay: Int): Long = toMillis(date.atTime(minuteOfDay / 60, minuteOfDay % 60))
    fun startOfDay(date: LocalDate): Long = toMillis(date.atStartOfDay())
    fun dateTime(ms: Long): LocalDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(ms), Tz.zone)
    fun dateOf(ms: Long): LocalDate = dateTime(ms).toLocalDate()
    fun minuteOfDay(ms: Long): Int { val t = dateTime(ms); return t.hour * 60 + t.minute }

    fun hhmm(ms: Long): String { val t = dateTime(ms).toLocalTime(); return hhmm(t.hour * 60 + t.minute) }
    fun hhmm(minuteOfDay: Int): String = "%02d:%02d".format(minuteOfDay / 60, minuteOfDay % 60)

    /** "HH:mm" 문자열을 분으로. 형식이 틀리면 null. */
    fun parseHhmm(text: String): Int? {
        val m = Regex("^(\\d{1,2}):(\\d{2})$").matchEntire(text.trim()) ?: return null
        val h = m.groupValues[1].toInt(); val min = m.groupValues[2].toInt()
        return if (h in 0..23 && min in 0..59) h * 60 + min else null
    }

    fun atTime(date: LocalDate, hhmm: String): Long? = parseHhmm(hhmm)?.let { toMillis(date, it) }

    // ---- 장기 목표 날짜 ----

    /** 1달 뒤. 같은 날짜로 하되, 오늘이 그 달의 말일이면 다음 달의 말일. 없는 날짜는 그 달 말일. */
    fun addMonthRule(t: LocalDate): LocalDate {
        val isLast = t.dayOfMonth == t.lengthOfMonth()
        return if (isLast) YearMonth.from(t).plusMonths(1).atEndOfMonth() else t.plusMonths(1)
    }

    /** 1년 뒤. 같은 날짜로 하고, 윤년의 2월 29일은 2월 28일. */
    fun addYearRule(t: LocalDate): LocalDate = t.plusYears(1)

    /** 주기를 골랐을 때 채워 줄 목표 날짜. 1주는 내일부터 7일째, 곧 오늘 + 7일. */
    fun goalDate(period: Period, today: LocalDate): LocalDate = when (period) {
        Period.W1 -> today.plusDays(7)
        Period.M1 -> addMonthRule(today)
        Period.Y1 -> addYearRule(today)
    }

    /** 남은 날 수로 보는 주기 분류. 목표를 연장할 때 쓴다. */
    fun classify(daysLeft: Long): Period = when {
        daysLeft <= 7 -> Period.W1
        daysLeft <= 31 -> Period.M1
        else -> Period.Y1
    }

    fun daysBetween(from: LocalDate, to: LocalDate): Long = ChronoUnit.DAYS.between(from, to)

    fun dText(days: Long): String = when {
        days == 0L -> "D-DAY"
        days > 0 -> "D-$days"
        else -> "D+${-days}"
    }

    /** 다음 달 1일까지 남은 날 수. 오늘이 1일이어도 다음 달 1일을 센다. */
    fun daysToNextFirst(today: LocalDate): Long =
        daysBetween(today, today.withDayOfMonth(1).plusMonths(1))

    fun monthKey(d: LocalDate): String = "%04d-%02d".format(d.year, d.monthValue)

    fun dow(d: java.time.DayOfWeek): String = when (d) {
        java.time.DayOfWeek.MONDAY -> "월"; java.time.DayOfWeek.TUESDAY -> "화"; java.time.DayOfWeek.WEDNESDAY -> "수"
        java.time.DayOfWeek.THURSDAY -> "목"; java.time.DayOfWeek.FRIDAY -> "금"; java.time.DayOfWeek.SATURDAY -> "토"; java.time.DayOfWeek.SUNDAY -> "일"
    }

    val MIDNIGHT: LocalTime = LocalTime.MIDNIGHT
}
