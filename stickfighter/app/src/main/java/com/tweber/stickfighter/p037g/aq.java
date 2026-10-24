package com.tweber.stickfighter.p037g;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/* JADX INFO: loaded from: classes.dex */
class aq extends Handler {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ ae f4066a;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    aq(ae aeVar, Looper looper) {
        super(looper);
        this.f4066a = aeVar;
    }

    @Override // android.os.Handler
    public void handleMessage(Message message) {
        this.f4066a.m4707a(((Boolean) message.obj).booleanValue());
    }
}
