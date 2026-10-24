package com.tweber.stickfighter.p032b;

import android.content.DialogInterface;
import com.tweber.stickfighter.p038h.C0924v;

/* JADX INFO: renamed from: com.tweber.stickfighter.b.u */
/* JADX INFO: loaded from: classes.dex */
class DialogInterfaceOnClickListenerC0829u implements DialogInterface.OnClickListener {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ C0924v f3871a;

    /* JADX INFO: renamed from: b */
    final /* synthetic */ C0826r f3872b;

    DialogInterfaceOnClickListenerC0829u(C0826r c0826r, C0924v c0924v) {
        this.f3872b = c0826r;
        this.f3871a = c0924v;
    }

    @Override // android.content.DialogInterface.OnClickListener
    public void onClick(DialogInterface dialogInterface, int i) {
        this.f3872b.m4531a(this.f3871a);
    }
}
