package com.example.repeatalarm.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val intervalMinutes: Int,
    val isEnabled: Boolean = true
) {
    fun startTimeLabel(): String = String.format("%02d:%02d", startHour, startMinute)
    fun endTimeLabel(): String = String.format("%02d:%02d", endHour, endMinute)
    fun intervalLabel(): String = if (intervalMinutes >= 60) {
        val h = intervalMinutes / 60
        val m = intervalMinutes % 60
        if (m == 0) "${h}시간마다" else "${h}시간 ${m}분마다"
    } else {
        "${intervalMinutes}분마다"
    }
}
