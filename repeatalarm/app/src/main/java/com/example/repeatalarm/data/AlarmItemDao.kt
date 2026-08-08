package com.example.repeatalarm.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AlarmItemDao {
    @Query("SELECT * FROM alarm_items WHERE groupId = :groupId ORDER BY startHour ASC, startMinute ASC")
    fun getItemsForGroup(groupId: Int): LiveData<List<AlarmItem>>

    @Query("SELECT * FROM alarm_items WHERE groupId = :groupId ORDER BY startHour ASC, startMinute ASC")
    suspend fun getItemsForGroupSync(groupId: Int): List<AlarmItem>

    @Query("SELECT * FROM alarm_items WHERE id = :id")
    suspend fun getItemById(id: Int): AlarmItem?

    @Query("SELECT ai.* FROM alarm_items ai INNER JOIN alarm_groups ag ON ai.groupId = ag.id WHERE ag.isEnabled = 1")
    suspend fun getAllActiveItems(): List<AlarmItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: AlarmItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<AlarmItem>)

    @Update
    suspend fun update(item: AlarmItem)

    @Delete
    suspend fun delete(item: AlarmItem)

    @Query("DELETE FROM alarm_items WHERE groupId = :groupId")
    suspend fun deleteAllForGroup(groupId: Int)
}
