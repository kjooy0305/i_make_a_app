package com.tweber.stickfighter.p037g;

import android.view.animation.Animation;

/* JADX INFO: loaded from: classes.dex */
class ap implements Animation.AnimationListener {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ ae f4065a;

    ap(ae aeVar) {
        this.f4065a = aeVar;
    }

    @Override // android.view.animation.Animation.AnimationListener
    public void onAnimationEnd(Animation animation) {
        this.f4065a.f4015B.setVisibility(4);
    }

    @Override // android.view.animation.Animation.AnimationListener
    public void onAnimationRepeat(Animation animation) {
    }

    @Override // android.view.animation.Animation.AnimationListener
    public void onAnimationStart(Animation animation) {
    }
}
