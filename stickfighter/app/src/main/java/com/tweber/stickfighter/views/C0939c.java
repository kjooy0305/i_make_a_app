package com.tweber.stickfighter.views;

import android.animation.ValueAnimator;
import android.content.res.ColorStateList;

/* JADX INFO: renamed from: com.tweber.stickfighter.views.c */
/* JADX INFO: loaded from: classes.dex */
class C0939c implements ValueAnimator.AnimatorUpdateListener {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ EditorToolbar f4275a;

    C0939c(EditorToolbar editorToolbar) {
        this.f4275a = editorToolbar;
    }

    @Override // android.animation.ValueAnimator.AnimatorUpdateListener
    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        this.f4275a.f4265e.setBackgroundTintList(ColorStateList.valueOf(((Integer) valueAnimator.getAnimatedValue()).intValue()));
    }
}
