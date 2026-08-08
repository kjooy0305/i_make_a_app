package com.example.repeatalarm

import android.app.Application
import com.example.repeatalarm.util.NotificationHelper

class RepeatAlarmApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
    }
}
