package com.tweber.stickfighter.p035e;

import android.view.View;

/* JADX INFO: renamed from: com.tweber.stickfighter.e.b */
/* JADX INFO: loaded from: classes.dex */
class ViewOnClickListenerC0840b implements View.OnClickListener {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ C0839a f3931a;

    ViewOnClickListenerC0840b(C0839a c0839a) {
        this.f3931a = c0839a;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        this.f3931a.m4584a();
        this.f3931a.dismiss();
    }
}
