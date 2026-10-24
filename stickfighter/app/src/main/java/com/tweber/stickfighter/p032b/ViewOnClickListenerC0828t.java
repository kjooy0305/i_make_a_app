package com.tweber.stickfighter.p032b;

import android.view.View;
import com.tweber.stickfighter.p038h.C0924v;

/* JADX INFO: renamed from: com.tweber.stickfighter.b.t */
/* JADX INFO: loaded from: classes.dex */
class ViewOnClickListenerC0828t implements View.OnClickListener {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ C0924v f3869a;

    /* JADX INFO: renamed from: b */
    final /* synthetic */ C0826r f3870b;

    ViewOnClickListenerC0828t(C0826r c0826r, C0924v c0924v) {
        this.f3870b = c0826r;
        this.f3869a = c0924v;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        this.f3870b.m4532a(this.f3869a, view.getContext());
    }
}
