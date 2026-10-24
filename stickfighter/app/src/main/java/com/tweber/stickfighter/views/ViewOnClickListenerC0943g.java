package com.tweber.stickfighter.views;

import android.view.View;

/* JADX INFO: renamed from: com.tweber.stickfighter.views.g */
/* JADX INFO: loaded from: classes.dex */
class ViewOnClickListenerC0943g implements View.OnClickListener {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ EditorToolbar f4279a;

    ViewOnClickListenerC0943g(EditorToolbar editorToolbar) {
        this.f4279a = editorToolbar;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        if (this.f4279a.f4272l == null) {
            return;
        }
        this.f4279a.f4272l.mo4789d(view);
    }
}
