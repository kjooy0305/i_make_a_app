package com.tweber.stickfighter.p035e;

import android.view.View;
import com.tweber.stickfighter.p038h.C0915m;

/* JADX INFO: loaded from: classes.dex */
class ah implements View.OnClickListener {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ C0915m f3914a;

    /* JADX INFO: renamed from: b */
    final /* synthetic */ ag f3915b;

    ah(ag agVar, C0915m c0915m) {
        this.f3915b = agVar;
        this.f3914a = c0915m;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        this.f3915b.f3911a.m4601a(this.f3914a);
    }
}
