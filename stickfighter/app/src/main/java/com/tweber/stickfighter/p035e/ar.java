package com.tweber.stickfighter.p035e;

import android.view.KeyEvent;
import android.widget.TextView;

/* JADX INFO: loaded from: classes.dex */
class ar implements TextView.OnEditorActionListener {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ ao f3930a;

    ar(ao aoVar) {
        this.f3930a = aoVar;
    }

    @Override // android.widget.TextView.OnEditorActionListener
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        if (i == 2) {
            return this.f3930a.m4608a();
        }
        return false;
    }
}
