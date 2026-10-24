package com.tweber.stickfighter.p037g;

import android.view.View;

/* JADX INFO: loaded from: classes.dex */
class aj implements View.OnSystemUiVisibilityChangeListener {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ ae f4059a;

    aj(ae aeVar) {
        this.f4059a = aeVar;
    }

    @Override // android.view.View.OnSystemUiVisibilityChangeListener
    public void onSystemUiVisibilityChange(int i) {
        if ((i & 4) == 0) {
            this.f4059a.m4712b(false);
            this.f4059a.f4048t.postDelayed(new ak(this), 3000L);
        }
    }
}
