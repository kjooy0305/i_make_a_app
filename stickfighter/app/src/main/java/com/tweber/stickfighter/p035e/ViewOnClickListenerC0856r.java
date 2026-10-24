package com.tweber.stickfighter.p035e;

import android.view.View;

/* JADX INFO: renamed from: com.tweber.stickfighter.e.r */
/* JADX INFO: loaded from: classes.dex */
class ViewOnClickListenerC0856r implements View.OnClickListener {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ C0855q f3970a;

    ViewOnClickListenerC0856r(C0855q c0855q) {
        this.f3970a = c0855q;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        if (this.f3970a.f3966b != null) {
            this.f3970a.f3966b.cancel(false);
        }
        this.f3970a.dismiss();
    }
}
