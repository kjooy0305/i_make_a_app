package com.example.repeatalarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.repeatalarm.data.AlarmRepository
import com.example.repeatalarm.util.AlarmScheduler
import com.example.repeatalarm.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_ITEM_ID = "extra_item_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val itemId = intent.getIntExtra(EXTRA_ITEM_ID, -1)
        if (itemId == -1) return

        val result = goAsync()
        val repository = AlarmRepository(context)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val item = repository.getItemById(itemId) ?: return@launch
                val group = repository.getGroupById(item.groupId) ?: return@launch

                if (group.isEnabled) {
                    NotificationHelper.showNotification(context, itemId, group.name, item.content)
                }

                AlarmScheduler.scheduleNextFromReceiver(context, item)
            } finally {
                result.finish()
            }
        }
    }
}
