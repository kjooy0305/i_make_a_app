package com.tweber.stickfighter.p038h;

import android.graphics.Canvas;
import android.graphics.RectF;
import android.view.View;
import com.tweber.stickfighter.views.C0948l;

/* JADX INFO: renamed from: com.tweber.stickfighter.h.j */
/* JADX INFO: loaded from: classes.dex */
public class C0912j {

    /* JADX INFO: renamed from: a */
    public final C0948l f4186a;

    /* JADX INFO: renamed from: b */
    float f4187b;

    /* JADX INFO: renamed from: c */
    float f4188c;

    /* JADX INFO: renamed from: d */
    float f4189d;

    /* JADX INFO: renamed from: e */
    float f4190e;

    /* JADX INFO: renamed from: f */
    float f4191f;

    /* JADX INFO: renamed from: g */
    float f4192g;

    /* JADX INFO: renamed from: h */
    float f4193h;

    /* JADX INFO: renamed from: i */
    float f4194i;

    /* JADX INFO: renamed from: j */
    float f4195j;

    /* JADX INFO: renamed from: k */
    RectF f4196k;

    public C0912j(float f, Canvas canvas) {
        this.f4196k = new RectF();
        this.f4187b = f;
        this.f4190e = Math.max(canvas.getWidth(), canvas.getHeight());
        this.f4191f = Math.min(canvas.getWidth(), canvas.getHeight());
        m4886b();
        this.f4196k = new RectF();
        this.f4196k.left = this.f4188c;
        this.f4196k.top = this.f4189d;
        this.f4196k.right = this.f4188c + this.f4193h;
        this.f4196k.bottom = this.f4189d + this.f4192g;
        this.f4186a = new C0948l(this.f4190e);
    }

    public C0912j(float f, View view) {
        this.f4196k = new RectF();
        this.f4187b = f;
        this.f4190e = view.getWidth();
        this.f4191f = view.getHeight();
        m4886b();
        this.f4196k = new RectF();
        this.f4196k.left = this.f4188c;
        this.f4196k.top = this.f4189d;
        this.f4196k.right = this.f4188c + this.f4193h;
        this.f4196k.bottom = this.f4189d + this.f4192g;
        this.f4186a = new C0948l(this.f4190e);
    }

    /* JADX INFO: renamed from: b */
    private void m4886b() {
        if (this.f4191f / this.f4190e > this.f4187b) {
            this.f4192g = this.f4190e * this.f4187b;
            this.f4193h = this.f4190e;
        } else {
            this.f4192g = this.f4191f;
            this.f4193h = this.f4191f / this.f4187b;
        }
        this.f4194i = this.f4192g * 0.01f;
        this.f4195j = this.f4193h * 0.01f;
        this.f4188c = (this.f4190e - this.f4193h) / 2.0f;
        this.f4189d = (this.f4191f - this.f4192g) / 2.0f;
    }

    /* JADX INFO: renamed from: a */
    public float m4887a(float f) {
        return this.f4188c + (this.f4195j * f);
    }

    /* JADX INFO: renamed from: a */
    public RectF m4888a() {
        return this.f4196k;
    }

    /* JADX INFO: renamed from: b */
    public float m4889b(float f) {
        return this.f4189d + (this.f4194i * f);
    }

    /* JADX INFO: renamed from: c */
    public float m4890c(float f) {
        return ((f - this.f4188c) / this.f4193h) * 100.0f;
    }

    /* JADX INFO: renamed from: d */
    public float m4891d(float f) {
        return ((f - this.f4189d) / this.f4192g) * 100.0f;
    }
}
