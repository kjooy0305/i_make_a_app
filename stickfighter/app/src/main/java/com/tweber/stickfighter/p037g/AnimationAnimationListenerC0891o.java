package com.tweber.stickfighter.p037g;

import android.view.animation.Animation;

/* JADX INFO: renamed from: com.tweber.stickfighter.g.o */
/* JADX INFO: loaded from: classes.dex */
class AnimationAnimationListenerC0891o implements Animation.AnimationListener {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ C0887k f4147a;

    AnimationAnimationListenerC0891o(C0887k c0887k) {
        this.f4147a = c0887k;
    }

    @Override // android.view.animation.Animation.AnimationListener
    public void onAnimationEnd(Animation animation) {
        this.f4147a.f4134q.setVisibility(4);
        this.f4147a.f4133p.setVisibility(4);
        this.f4147a.m4815d();
    }

    @Override // android.view.animation.Animation.AnimationListener
    public void onAnimationRepeat(Animation animation) {
    }

    @Override // android.view.animation.Animation.AnimationListener
    public void onAnimationStart(Animation animation) {
        this.f4147a.f4134q.setVisibility(0);
    }
}
