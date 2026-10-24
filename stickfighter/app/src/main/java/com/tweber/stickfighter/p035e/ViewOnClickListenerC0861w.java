package com.tweber.stickfighter.p035e;

import android.view.View;

/* JADX INFO: renamed from: com.tweber.stickfighter.e.w */
/* JADX INFO: loaded from: classes.dex */
class ViewOnClickListenerC0861w implements View.OnClickListener {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ C0860v f3980a;

    ViewOnClickListenerC0861w(C0860v c0860v) {
        this.f3980a = c0860v;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        if (this.f3980a.f3976b != null) {
            this.f3980a.f3976b.cancel(false);
        }
        this.f3980a.dismiss();
    }
}
