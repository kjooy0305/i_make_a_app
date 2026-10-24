package com.tweber.stickfighter.views;

import android.view.View;

/* JADX INFO: renamed from: com.tweber.stickfighter.views.a */
/* JADX INFO: loaded from: classes.dex */
class ViewOnClickListenerC0937a implements View.OnClickListener {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ EditorToolbar f4273a;

    ViewOnClickListenerC0937a(EditorToolbar editorToolbar) {
        this.f4273a = editorToolbar;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        if (this.f4273a.f4272l == null) {
            return;
        }
        this.f4273a.f4272l.mo4790e(view);
    }
}
