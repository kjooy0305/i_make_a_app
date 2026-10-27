package com.kjooy0305.dayclean

import android.app.Application
import com.kjooy0305.dayclean.platform.AlarmScheduler
import com.kjooy0305.dayclean.platform.Notifier

class DayCleanApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Notifier.createChannels(this)
        // 앱이 열릴 때마다 알람이 걸려 있는지 확인한다(강제 종료 뒤 처음 열었을 때도 복구된다).
        AlarmScheduler.schedule(this)
    }
}
