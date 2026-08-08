package com.example.repeatalarm.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AlarmGroupDao {
    @Query("SELECT * FROM alarm_groups ORDER BY id DESC")
    fun getAllGroups(): LiveData<List<AlarmGroup>>

    @Query("SELECT * FROM alarm_groups WHERE isEnabled = 1")
    suspend fun getEnabledGroups(): List<AlarmGroup>

    @Query("SELECT * FROM alarm_groups WHERE id = :id")
    suspend fun getGroupById(id: Int): AlarmGroup?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(group: AlarmGroup): Long

    @Update
    suspend fun update(group: AlarmGroup)

    @Delete
    suspend fun delete(group: AlarmGroup)

    @Transaction
    @Query("SELECT * FROM alarm_groups ORDER BY id DESC")
    fun getAllGroupsWithItems(): LiveData<List<AlarmGroupWithItems>>
}
