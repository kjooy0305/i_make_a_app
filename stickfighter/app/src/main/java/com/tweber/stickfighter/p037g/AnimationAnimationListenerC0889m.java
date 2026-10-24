package com.tweber.stickfighter.p037g;

import android.view.animation.Animation;

/* JADX INFO: renamed from: com.tweber.stickfighter.g.m */
/* JADX INFO: loaded from: classes.dex */
class AnimationAnimationListenerC0889m implements Animation.AnimationListener {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ C0887k f4145a;

    AnimationAnimationListenerC0889m(C0887k c0887k) {
        this.f4145a = c0887k;
    }

    @Override // android.view.animation.Animation.AnimationListener
    public void onAnimationEnd(Animation animation) {
        this.f4145a.f4134q.setVisibility(4);
    }

    @Override // android.view.animation.Animation.AnimationListener
    public void onAnimationRepeat(Animation animation) {
    }

    @Override // android.view.animation.Animation.AnimationListener
    public void onAnimationStart(Animation animation) {
        this.f4145a.f4134q.setVisibility(0);
    }
}
