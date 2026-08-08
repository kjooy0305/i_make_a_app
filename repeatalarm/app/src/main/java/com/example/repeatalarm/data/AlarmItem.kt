package com.example.repeatalarm.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "alarm_items",
    foreignKeys = [ForeignKey(
        entity = AlarmGroup::class,
        parentColumns = ["id"],
        childColumns = ["groupId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class AlarmItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val groupId: Int,
    val content: String,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val intervalMinutes: Int
) {
    fun startLabel() = String.format("%02d:%02d", startHour, startMinute)
    fun endLabel() = String.format("%02d:%02d", endHour, endMinute)
    fun intervalLabel() = if (intervalMinutes >= 60) {
        val h = intervalMinutes / 60; val m = intervalMinutes % 60
        if (m == 0) "${h}시간마다" else "${h}시간 ${m}분마다"
    } else "${intervalMinutes}분마다"
}
