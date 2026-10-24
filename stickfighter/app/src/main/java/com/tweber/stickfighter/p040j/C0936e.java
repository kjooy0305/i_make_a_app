package com.tweber.stickfighter.p040j;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;

/* JADX INFO: renamed from: com.tweber.stickfighter.j.e */
/* JADX INFO: loaded from: classes.dex */
final class C0936e extends AnimatorListenerAdapter {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ View f4260a;

    C0936e(View view) {
        this.f4260a = view;
    }

    @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
    public void onAnimationEnd(Animator animator) {
        this.f4260a.setVisibility(4);
    }
}
