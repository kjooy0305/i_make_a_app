package com.tweber.stickfighter.views;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.ScaleAnimation;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import com.tweber.stickfighter.activities.R;

/* JADX INFO: loaded from: classes.dex */
public class EditorToolbar extends LinearLayout {

    /* JADX INFO: renamed from: a */
    FloatingActionButton f4261a;

    /* JADX INFO: renamed from: b */
    FloatingActionButton f4262b;

    /* JADX INFO: renamed from: c */
    FloatingActionButton f4263c;

    /* JADX INFO: renamed from: d */
    FloatingActionButton f4264d;

    /* JADX INFO: renamed from: e */
    FloatingActionButton f4265e;

    /* JADX INFO: renamed from: f */
    FloatingActionButton f4266f;

    /* JADX INFO: renamed from: g */
    ProgressBar f4267g;

    /* JADX INFO: renamed from: h */
    ProgressBar f4268h;

    /* JADX INFO: renamed from: i */
    ProgressBar f4269i;

    /* JADX INFO: renamed from: j */
    boolean f4270j;

    /* JADX INFO: renamed from: k */
    boolean f4271k;

    /* JADX INFO: renamed from: l */
    InterfaceC0947k f4272l;

    public EditorToolbar(Context context) {
        this(context, null);
    }

    public EditorToolbar(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public EditorToolbar(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.f4270j = true;
        this.f4272l = null;
        View viewInflate = inflate(getContext(), R.layout.editor_toolbar, null);
        addView(viewInflate);
        this.f4271k = true;
        this.f4261a = (FloatingActionButton) viewInflate.findViewById(R.id.SaveFrameButton);
        this.f4263c = (FloatingActionButton) viewInflate.findViewById(R.id.ChangeActionModeButton);
        this.f4262b = (FloatingActionButton) viewInflate.findViewById(R.id.AddAnimationObjectButton);
        this.f4264d = (FloatingActionButton) viewInflate.findViewById(R.id.NextFrameButton);
        this.f4265e = (FloatingActionButton) viewInflate.findViewById(R.id.PreviousFrameButton);
        this.f4266f = (FloatingActionButton) viewInflate.findViewById(R.id.MoveEditToolbarImageView);
        this.f4267g = (ProgressBar) viewInflate.findViewById(R.id.saveFrameProgressBar);
        this.f4268h = (ProgressBar) viewInflate.findViewById(R.id.previousFrameProgressBar);
        this.f4269i = (ProgressBar) viewInflate.findViewById(R.id.nextFrameProgressBar);
        this.f4261a.setOnClickListener(new ViewOnClickListenerC0937a(this));
        this.f4263c.setOnClickListener(new ViewOnClickListenerC0940d(this));
        this.f4262b.setOnClickListener(new ViewOnClickListenerC0941e(this));
        this.f4264d.setOnClickListener(new ViewOnClickListenerC0942f(this));
        this.f4265e.setOnClickListener(new ViewOnClickListenerC0943g(this));
        this.f4266f.setOnTouchListener(new ViewOnTouchListenerC0944h(this));
    }

    /* JADX INFO: renamed from: a */
    public void m4968a() {
        this.f4267g.clearAnimation();
        this.f4267g.setVisibility(0);
        int integer = this.f4267g.getContext().getResources().getInteger(android.R.integer.config_mediumAnimTime);
        ScaleAnimation scaleAnimation = new ScaleAnimation(0.9f, 1.0f, 0.9f, 1.0f, 1, 0.5f, 1, 0.5f);
        scaleAnimation.setDuration(integer);
        this.f4267g.startAnimation(scaleAnimation);
    }

    /* JADX INFO: renamed from: a */
    public void m4969a(boolean z) {
        if (this.f4270j) {
            return;
        }
        this.f4265e.setEnabled(true);
        this.f4270j = true;
        int color = this.f4265e.getContext().getResources().getColor(R.color.editor_toolbar_mini_button_enabled);
        int color2 = this.f4265e.getContext().getResources().getColor(R.color.editor_toolbar_mini_button_disabled);
        int integer = this.f4265e.getContext().getResources().getInteger(android.R.integer.config_mediumAnimTime);
        if (!z) {
            this.f4265e.setBackgroundTintList(ColorStateList.valueOf(color));
            return;
        }
        ValueAnimator valueAnimatorOfObject = ValueAnimator.ofObject(new ArgbEvaluator(), Integer.valueOf(color2), Integer.valueOf(color));
        valueAnimatorOfObject.addUpdateListener(new C0938b(this));
        valueAnimatorOfObject.setDuration(integer);
        valueAnimatorOfObject.start();
    }

    /* JADX INFO: renamed from: b */
    public void m4970b() {
        this.f4268h.clearAnimation();
        this.f4268h.setVisibility(0);
        int integer = this.f4268h.getContext().getResources().getInteger(android.R.integer.config_mediumAnimTime);
        ScaleAnimation scaleAnimation = new ScaleAnimation(0.9f, 1.0f, 0.9f, 1.0f, 1, 0.5f, 1, 0.5f);
        scaleAnimation.setDuration(integer);
        this.f4268h.startAnimation(scaleAnimation);
    }

    /* JADX INFO: renamed from: b */
    public void m4971b(boolean z) {
        if (this.f4270j) {
            this.f4265e.setEnabled(false);
            this.f4270j = false;
            int color = this.f4265e.getContext().getResources().getColor(R.color.editor_toolbar_mini_button_enabled);
            int color2 = this.f4265e.getContext().getResources().getColor(R.color.editor_toolbar_mini_button_disabled);
            int integer = this.f4265e.getContext().getResources().getInteger(android.R.integer.config_mediumAnimTime);
            if (!z) {
                this.f4265e.setBackgroundTintList(ColorStateList.valueOf(color2));
                return;
            }
            ValueAnimator valueAnimatorOfObject = ValueAnimator.ofObject(new ArgbEvaluator(), Integer.valueOf(color), Integer.valueOf(color2));
            valueAnimatorOfObject.addUpdateListener(new C0939c(this));
            valueAnimatorOfObject.setDuration(integer);
            valueAnimatorOfObject.start();
        }
    }

    /* JADX INFO: renamed from: c */
    public void m4972c() {
        this.f4268h.clearAnimation();
        int integer = this.f4268h.getContext().getResources().getInteger(android.R.integer.config_mediumAnimTime);
        ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 0.9f, 1.0f, 0.9f, 1, 0.5f, 1, 0.5f);
        scaleAnimation.setDuration(integer);
        scaleAnimation.setAnimationListener(new AnimationAnimationListenerC0945i(this));
        this.f4268h.startAnimation(scaleAnimation);
    }

    /* JADX INFO: renamed from: d */
    public void m4973d() {
        this.f4269i.clearAnimation();
        this.f4269i.setVisibility(0);
        int integer = this.f4269i.getContext().getResources().getInteger(android.R.integer.config_mediumAnimTime);
        ScaleAnimation scaleAnimation = new ScaleAnimation(0.9f, 1.0f, 0.9f, 1.0f, 1, 0.5f, 1, 0.5f);
        scaleAnimation.setDuration(integer);
        this.f4269i.startAnimation(scaleAnimation);
    }

    /* JADX INFO: renamed from: e */
    public void m4974e() {
        this.f4269i.clearAnimation();
        int integer = this.f4269i.getContext().getResources().getInteger(android.R.integer.config_mediumAnimTime);
        ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 0.9f, 1.0f, 0.9f, 1, 0.5f, 1, 0.5f);
        scaleAnimation.setDuration(integer);
        scaleAnimation.setAnimationListener(new AnimationAnimationListenerC0946j(this));
        this.f4269i.startAnimation(scaleAnimation);
    }

    /* JADX INFO: renamed from: f */
    public void m4975f() {
        this.f4261a.setEnabled(false);
        this.f4262b.setEnabled(false);
        this.f4263c.setEnabled(false);
        this.f4264d.setEnabled(false);
        this.f4265e.setEnabled(false);
        this.f4266f.setEnabled(false);
    }

    /* JADX INFO: renamed from: g */
    public void m4976g() {
        this.f4261a.setEnabled(true);
        this.f4262b.setEnabled(true);
        this.f4263c.setEnabled(true);
        this.f4264d.setEnabled(true);
        this.f4265e.setEnabled(true);
        this.f4266f.setEnabled(true);
    }

    /* JADX INFO: renamed from: h */
    public void m4977h() {
        findViewById(R.id.previousFrameButtonContainer).setVisibility(8);
        findViewById(R.id.nextFrameButtonContainer).setVisibility(8);
    }

    public void setActiveActionMode(int i) {
        switch (i) {
            case 0:
                this.f4263c.setImageResource(R.drawable.ic_action_open_with_white18dp);
                break;
            case 1:
                this.f4263c.setImageResource(R.drawable.ic_action_photo_size_select_large_white18dp);
                break;
            case 2:
                this.f4263c.setImageResource(R.drawable.ic_action_flip_white18dp);
                break;
        }
    }

    public void setOnInteractionListener(InterfaceC0947k interfaceC0947k) {
        this.f4272l = interfaceC0947k;
    }
}
