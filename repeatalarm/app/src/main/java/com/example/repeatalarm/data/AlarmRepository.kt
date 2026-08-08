package com.example.repeatalarm.data

import android.content.Context
import androidx.lifecycle.LiveData

class AlarmRepository(context: Context) {
    private val db = AlarmDatabase.getInstance(context)
    private val groupDao = db.alarmGroupDao()
    private val itemDao = db.alarmItemDao()

    val allGroups: LiveData<List<AlarmGroup>> = groupDao.getAllGroups()
    val allGroupsWithItems: LiveData<List<AlarmGroupWithItems>> = groupDao.getAllGroupsWithItems()

    suspend fun insertGroup(group: AlarmGroup): Int = groupDao.insert(group).toInt()
    suspend fun updateGroup(group: AlarmGroup) = groupDao.update(group)
    suspend fun deleteGroup(group: AlarmGroup) = groupDao.delete(group)
    suspend fun getGroupById(id: Int): AlarmGroup? = groupDao.getGroupById(id)
    suspend fun getEnabledGroups(): List<AlarmGroup> = groupDao.getEnabledGroups()

    suspend fun insertItem(item: AlarmItem): Int = itemDao.insert(item).toInt()
    suspend fun replaceItemsForGroup(groupId: Int, items: List<AlarmItem>) {
        itemDao.deleteAllForGroup(groupId)
        itemDao.insertAll(items.map { it.copy(groupId = groupId) })
    }
    suspend fun getItemsForGroupSync(groupId: Int): List<AlarmItem> = itemDao.getItemsForGroupSync(groupId)
    suspend fun getItemById(id: Int): AlarmItem? = itemDao.getItemById(id)
    suspend fun getAllActiveItems(): List<AlarmItem> = itemDao.getAllActiveItems()
    fun getItemsForGroupLive(groupId: Int): LiveData<List<AlarmItem>> = itemDao.getItemsForGroup(groupId)
}
