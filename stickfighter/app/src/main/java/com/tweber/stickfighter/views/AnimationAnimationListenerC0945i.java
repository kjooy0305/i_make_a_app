package com.tweber.stickfighter.views;

import android.view.animation.Animation;

/* JADX INFO: renamed from: com.tweber.stickfighter.views.i */
/* JADX INFO: loaded from: classes.dex */
class AnimationAnimationListenerC0945i implements Animation.AnimationListener {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ EditorToolbar f4281a;

    AnimationAnimationListenerC0945i(EditorToolbar editorToolbar) {
        this.f4281a = editorToolbar;
    }

    @Override // android.view.animation.Animation.AnimationListener
    public void onAnimationEnd(Animation animation) {
        this.f4281a.f4268h.setVisibility(4);
    }

    @Override // android.view.animation.Animation.AnimationListener
    public void onAnimationRepeat(Animation animation) {
    }

    @Override // android.view.animation.Animation.AnimationListener
    public void onAnimationStart(Animation animation) {
    }
}
