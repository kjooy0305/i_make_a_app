package com.tweber.stickfighter.p037g;

import android.view.animation.Animation;
import com.tweber.stickfighter.p031a.C0800a;

/* JADX INFO: renamed from: com.tweber.stickfighter.g.n */
/* JADX INFO: loaded from: classes.dex */
class AnimationAnimationListenerC0890n implements Animation.AnimationListener {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ C0887k f4146a;

    AnimationAnimationListenerC0890n(C0887k c0887k) {
        this.f4146a = c0887k;
    }

    @Override // android.view.animation.Animation.AnimationListener
    public void onAnimationEnd(Animation animation) {
        this.f4146a.f4137t.setVisibility(4);
        this.f4146a.f4133p.setVisibility(4);
        this.f4146a.m4815d();
        C0800a.m4492a().m4496b();
    }

    @Override // android.view.animation.Animation.AnimationListener
    public void onAnimationRepeat(Animation animation) {
    }

    @Override // android.view.animation.Animation.AnimationListener
    public void onAnimationStart(Animation animation) {
        this.f4146a.f4137t.setVisibility(0);
        this.f4146a.f4133p.setVisibility(0);
    }
}
