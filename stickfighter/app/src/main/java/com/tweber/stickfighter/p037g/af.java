package com.tweber.stickfighter.p037g;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/* JADX INFO: loaded from: classes.dex */
class af extends Handler {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ ae f4055a;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    af(ae aeVar, Looper looper) {
        super(looper);
        this.f4055a = aeVar;
    }

    @Override // android.os.Handler
    public void handleMessage(Message message) {
        this.f4055a.m4702a(((Integer) message.obj).intValue(), true);
    }
}
