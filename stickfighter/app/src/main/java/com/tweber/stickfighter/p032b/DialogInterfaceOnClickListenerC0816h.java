package com.tweber.stickfighter.p032b;

import android.content.DialogInterface;
import com.tweber.stickfighter.p038h.C0924v;

/* JADX INFO: renamed from: com.tweber.stickfighter.b.h */
/* JADX INFO: loaded from: classes.dex */
class DialogInterfaceOnClickListenerC0816h implements DialogInterface.OnClickListener {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ C0924v f3839a;

    /* JADX INFO: renamed from: b */
    final /* synthetic */ C0813e f3840b;

    DialogInterfaceOnClickListenerC0816h(C0813e c0813e, C0924v c0924v) {
        this.f3840b = c0813e;
        this.f3839a = c0924v;
    }

    @Override // android.content.DialogInterface.OnClickListener
    public void onClick(DialogInterface dialogInterface, int i) {
        this.f3840b.m4510a(this.f3839a);
    }
}
