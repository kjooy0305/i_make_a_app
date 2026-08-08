package com.example.repeatalarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.repeatalarm.data.AlarmRepository
import com.example.repeatalarm.util.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != "android.intent.action.LOCKED_BOOT_COMPLETED") return

        val result = goAsync()
        val repository = AlarmRepository(context)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.getAllActiveItems().forEach { item ->
                    AlarmScheduler.schedule(context, item)
                }
            } finally {
                result.finish()
            }
        }
    }
}
