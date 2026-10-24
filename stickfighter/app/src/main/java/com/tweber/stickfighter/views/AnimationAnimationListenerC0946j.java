package com.tweber.stickfighter.views;

import android.view.animation.Animation;

/* JADX INFO: renamed from: com.tweber.stickfighter.views.j */
/* JADX INFO: loaded from: classes.dex */
class AnimationAnimationListenerC0946j implements Animation.AnimationListener {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ EditorToolbar f4282a;

    AnimationAnimationListenerC0946j(EditorToolbar editorToolbar) {
        this.f4282a = editorToolbar;
    }

    @Override // android.view.animation.Animation.AnimationListener
    public void onAnimationEnd(Animation animation) {
        this.f4282a.f4269i.setVisibility(4);
    }

    @Override // android.view.animation.Animation.AnimationListener
    public void onAnimationRepeat(Animation animation) {
    }

    @Override // android.view.animation.Animation.AnimationListener
    public void onAnimationStart(Animation animation) {
    }
}
