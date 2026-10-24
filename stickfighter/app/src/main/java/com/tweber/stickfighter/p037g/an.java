package com.tweber.stickfighter.p037g;

import android.view.animation.Animation;

/* JADX INFO: loaded from: classes.dex */
class an implements Animation.AnimationListener {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ ae f4063a;

    an(ae aeVar) {
        this.f4063a = aeVar;
    }

    @Override // android.view.animation.Animation.AnimationListener
    public void onAnimationEnd(Animation animation) {
        this.f4063a.f4051w.setVisibility(4);
    }

    @Override // android.view.animation.Animation.AnimationListener
    public void onAnimationRepeat(Animation animation) {
    }

    @Override // android.view.animation.Animation.AnimationListener
    public void onAnimationStart(Animation animation) {
    }
}
