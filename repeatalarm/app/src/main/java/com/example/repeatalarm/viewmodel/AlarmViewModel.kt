package com.example.repeatalarm.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.repeatalarm.data.AlarmGroup
import com.example.repeatalarm.data.AlarmGroupWithItems
import com.example.repeatalarm.data.AlarmItem
import com.example.repeatalarm.data.AlarmRepository
import com.example.repeatalarm.util.AlarmScheduler
import kotlinx.coroutines.launch

class AlarmViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = AlarmRepository(app)

    val allGroupsWithItems: LiveData<List<AlarmGroupWithItems>> = repository.allGroupsWithItems

    fun saveGroup(group: AlarmGroup, items: List<AlarmItem>) = viewModelScope.launch {
        if (group.id != 0) {
            repository.getItemsForGroupSync(group.id).forEach {
                AlarmScheduler.cancel(getApplication(), it.id)
            }
        }

        val groupId = if (group.id == 0) {
            repository.insertGroup(group).also { id ->
                repository.replaceItemsForGroup(id, items)
            }
        } else {
            repository.updateGroup(group)
            repository.replaceItemsForGroup(group.id, items)
            group.id
        }

        if (group.isEnabled) {
            repository.getItemsForGroupSync(groupId).forEach {
                AlarmScheduler.schedule(getApplication(), it)
            }
        }
    }

    fun deleteGroup(group: AlarmGroup) = viewModelScope.launch {
        repository.getItemsForGroupSync(group.id).forEach {
            AlarmScheduler.cancel(getApplication(), it.id)
        }
        repository.deleteGroup(group)
    }

    fun toggleGroup(group: AlarmGroup) = viewModelScope.launch {
        val updated = group.copy(isEnabled = !group.isEnabled)
        repository.updateGroup(updated)
        val items = repository.getItemsForGroupSync(group.id)
        if (updated.isEnabled) {
            items.forEach { AlarmScheduler.schedule(getApplication(), it) }
        } else {
            items.forEach { AlarmScheduler.cancel(getApplication(), it.id) }
        }
    }

    suspend fun getGroupById(id: Int): AlarmGroup? = repository.getGroupById(id)
    suspend fun getItemsForGroup(groupId: Int): List<AlarmItem> = repository.getItemsForGroupSync(groupId)
}
