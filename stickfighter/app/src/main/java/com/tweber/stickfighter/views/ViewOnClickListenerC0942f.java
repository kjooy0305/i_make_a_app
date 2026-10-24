package com.tweber.stickfighter.views;

import android.view.View;

/* JADX INFO: renamed from: com.tweber.stickfighter.views.f */
/* JADX INFO: loaded from: classes.dex */
class ViewOnClickListenerC0942f implements View.OnClickListener {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ EditorToolbar f4278a;

    ViewOnClickListenerC0942f(EditorToolbar editorToolbar) {
        this.f4278a = editorToolbar;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        if (this.f4278a.f4272l == null) {
            return;
        }
        this.f4278a.f4272l.mo4788c(view);
    }
}
