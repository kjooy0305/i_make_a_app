package com.tweber.stickfighter.p037g;

import android.view.animation.Animation;

/* JADX INFO: renamed from: com.tweber.stickfighter.g.p */
/* JADX INFO: loaded from: classes.dex */
class AnimationAnimationListenerC0892p implements Animation.AnimationListener {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ C0887k f4148a;

    AnimationAnimationListenerC0892p(C0887k c0887k) {
        this.f4148a = c0887k;
    }

    @Override // android.view.animation.Animation.AnimationListener
    public void onAnimationEnd(Animation animation) {
        this.f4148a.f4137t.setVisibility(4);
    }

    @Override // android.view.animation.Animation.AnimationListener
    public void onAnimationRepeat(Animation animation) {
    }

    @Override // android.view.animation.Animation.AnimationListener
    public void onAnimationStart(Animation animation) {
        this.f4148a.f4137t.setVisibility(0);
        this.f4148a.f4133p.setVisibility(0);
    }
}
