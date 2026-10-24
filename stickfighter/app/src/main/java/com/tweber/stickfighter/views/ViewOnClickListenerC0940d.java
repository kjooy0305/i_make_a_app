package com.tweber.stickfighter.views;

import android.view.View;

/* JADX INFO: renamed from: com.tweber.stickfighter.views.d */
/* JADX INFO: loaded from: classes.dex */
class ViewOnClickListenerC0940d implements View.OnClickListener {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ EditorToolbar f4276a;

    ViewOnClickListenerC0940d(EditorToolbar editorToolbar) {
        this.f4276a = editorToolbar;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        if (this.f4276a.f4272l == null) {
            return;
        }
        this.f4276a.f4272l.mo4785a(view);
    }
}
