package com.tweber.stickfighter.p035e;

import android.view.KeyEvent;
import android.widget.TextView;

/* JADX INFO: renamed from: com.tweber.stickfighter.e.p */
/* JADX INFO: loaded from: classes.dex */
class C0854p implements TextView.OnEditorActionListener {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ C0851m f3964a;

    C0854p(C0851m c0851m) {
        this.f3964a = c0851m;
    }

    @Override // android.widget.TextView.OnEditorActionListener
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        if (i == 2) {
            return this.f3964a.m4639a();
        }
        return false;
    }
}
