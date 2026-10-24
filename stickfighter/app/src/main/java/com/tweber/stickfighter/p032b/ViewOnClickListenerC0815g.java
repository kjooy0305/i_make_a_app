package com.tweber.stickfighter.p032b;

import android.view.View;
import com.tweber.stickfighter.p038h.C0924v;

/* JADX INFO: renamed from: com.tweber.stickfighter.b.g */
/* JADX INFO: loaded from: classes.dex */
class ViewOnClickListenerC0815g implements View.OnClickListener {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ C0924v f3837a;

    /* JADX INFO: renamed from: b */
    final /* synthetic */ C0813e f3838b;

    ViewOnClickListenerC0815g(C0813e c0813e, C0924v c0924v) {
        this.f3838b = c0813e;
        this.f3837a = c0924v;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        this.f3838b.m4511a(this.f3837a, view.getContext());
    }
}
