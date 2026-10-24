package com.tweber.stickfighter.p037g;

import android.view.animation.Animation;

/* JADX INFO: loaded from: classes.dex */
class am implements Animation.AnimationListener {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ ae f4062a;

    am(ae aeVar) {
        this.f4062a = aeVar;
    }

    @Override // android.view.animation.Animation.AnimationListener
    public void onAnimationEnd(Animation animation) {
        this.f4062a.f4051w.setVisibility(0);
    }

    @Override // android.view.animation.Animation.AnimationListener
    public void onAnimationRepeat(Animation animation) {
    }

    @Override // android.view.animation.Animation.AnimationListener
    public void onAnimationStart(Animation animation) {
    }
}
