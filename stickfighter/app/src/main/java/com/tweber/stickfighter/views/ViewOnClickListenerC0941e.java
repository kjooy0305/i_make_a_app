package com.tweber.stickfighter.views;

import android.view.View;

/* JADX INFO: renamed from: com.tweber.stickfighter.views.e */
/* JADX INFO: loaded from: classes.dex */
class ViewOnClickListenerC0941e implements View.OnClickListener {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ EditorToolbar f4277a;

    ViewOnClickListenerC0941e(EditorToolbar editorToolbar) {
        this.f4277a = editorToolbar;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        if (this.f4277a.f4272l == null) {
            return;
        }
        this.f4277a.f4272l.mo4787b(view);
    }
}
