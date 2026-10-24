package com.tweber.stickfighter.views;

import android.animation.ValueAnimator;
import android.content.res.ColorStateList;

/* JADX INFO: renamed from: com.tweber.stickfighter.views.b */
/* JADX INFO: loaded from: classes.dex */
class C0938b implements ValueAnimator.AnimatorUpdateListener {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ EditorToolbar f4274a;

    C0938b(EditorToolbar editorToolbar) {
        this.f4274a = editorToolbar;
    }

    @Override // android.animation.ValueAnimator.AnimatorUpdateListener
    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        this.f4274a.f4265e.setBackgroundTintList(ColorStateList.valueOf(((Integer) valueAnimator.getAnimatedValue()).intValue()));
    }
}
