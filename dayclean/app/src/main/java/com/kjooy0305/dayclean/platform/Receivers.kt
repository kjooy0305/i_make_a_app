package com.kjooy0305.dayclean.platform

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kjooy0305.dayclean.data.Store
import com.kjooy0305.dayclean.logic.Ops

/** 정확한 알람이 깨웠을 때. */
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        Thread {
            try { AlarmScheduler.run(context.applicationContext) } finally { pending.finish() }
        }.start()
    }
}

/** 재부팅, 시간·시간대 변경, 앱 업데이트 뒤에 알람을 다시 건다. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        Thread {
            try { AlarmScheduler.run(context.applicationContext) } finally { pending.finish() }
        }.start()
    }
}

/** 알림의 [완료] 버튼. 눌린 항목 하나만 해결로 처리한다. */
class ActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Notifier.ACTION_DONE) return
        val id = intent.getLongExtra(Notifier.EXTRA_ITEM_ID, -1L)
        if (id < 0) return
        val pending = goAsync()
        Thread {
            try {
                val store = Store.get(context.applicationContext)
                store.update { Ops.setDone(it, id, true, store.now()) }
                Notifier.cancelItem(context.applicationContext, id)
                AlarmScheduler.schedule(context.applicationContext)
            } finally { pending.finish() }
        }.start()
    }
}
