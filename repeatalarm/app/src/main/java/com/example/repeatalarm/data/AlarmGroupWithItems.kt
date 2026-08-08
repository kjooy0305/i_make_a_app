package com.example.repeatalarm.data

import androidx.room.Embedded
import androidx.room.Relation

data class AlarmGroupWithItems(
    @Embedded val group: AlarmGroup,
    @Relation(parentColumn = "id", entityColumn = "groupId")
    val items: List<AlarmItem>
)
