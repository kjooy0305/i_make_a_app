package com.larswerkman.holocolorpicker;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.MotionEvent;
import android.view.View;

/* JADX INFO: renamed from: com.larswerkman.holocolorpicker.h */
/* JADX INFO: loaded from: classes.dex */
public class C0770h extends View {
    public C0770h(android.content.Context c) { super(c); }
    public C0770h(android.content.Context c, android.util.AttributeSet a) { super(c, a); }
    public C0770h(android.content.Context c, android.util.AttributeSet a, int d) { super(c, a, d); }

    /* JADX INFO: renamed from: a */
    private int f3731a;

    /* JADX INFO: renamed from: b */
    private int f3732b;

    /* JADX INFO: renamed from: c */
    private int f3733c;

    /* JADX INFO: renamed from: d */
    private int f3734d;

    /* JADX INFO: renamed from: e */
    private int f3735e;

    /* JADX INFO: renamed from: f */
    private int f3736f;

    /* JADX INFO: renamed from: g */
    private Paint f3737g;

    /* JADX INFO: renamed from: h */
    private Paint f3738h;

    /* JADX INFO: renamed from: i */
    private Paint f3739i;

    /* JADX INFO: renamed from: j */
    private RectF f3740j;

    /* JADX INFO: renamed from: k */
    private Shader f3741k;

    /* JADX INFO: renamed from: l */
    private boolean f3742l;

    /* JADX INFO: renamed from: m */
    private int f3743m;

    /* JADX INFO: renamed from: n */
    private float[] f3744n;

    /* JADX INFO: renamed from: o */
    private float f3745o;

    /* JADX INFO: renamed from: p */
    private float f3746p;

    /* JADX INFO: renamed from: q */
    private ColorPicker f3747q;

    /* JADX INFO: renamed from: r */
    private boolean f3748r;

    /* JADX INFO: renamed from: s */
    private InterfaceC0771i f3749s;

    /* JADX INFO: renamed from: t */
    private int f3750t;

    /* JADX INFO: renamed from: a */
    private void m4442a(int i) {
        int i2 = i - this.f3735e;
        if (i2 < 0) {
            i2 = 0;
        } else if (i2 > this.f3732b) {
            i2 = this.f3732b;
        }
        this.f3743m = Color.HSVToColor(new float[]{this.f3744n[0], i2 * this.f3745o, 1.0f});
    }

    public int getColor() {
        return this.f3743m;
    }

    public InterfaceC0771i getOnSaturationChangedListener() {
        return this.f3749s;
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        int i;
        int i2;
        canvas.drawRect(this.f3740j, this.f3737g);
        if (this.f3748r) {
            i = this.f3736f;
            i2 = this.f3735e;
        } else {
            i = this.f3735e;
            i2 = this.f3736f;
        }
        canvas.drawCircle(i, i2, this.f3735e, this.f3739i);
        canvas.drawCircle(i, i2, this.f3734d, this.f3738h);
    }

    @Override // android.view.View
    protected void onMeasure(int i, int i2) {
        int i3 = (this.f3735e * 2) + this.f3733c;
        if (!this.f3748r) {
            i = i2;
        }
        int mode = View.MeasureSpec.getMode(i);
        int size = View.MeasureSpec.getSize(i);
        if (mode != 1073741824) {
            size = mode == Integer.MIN_VALUE ? Math.min(i3, size) : i3;
        }
        int i4 = this.f3735e * 2;
        this.f3732b = size - i4;
        if (this.f3748r) {
            setMeasuredDimension(this.f3732b + i4, i4);
        } else {
            setMeasuredDimension(i4, this.f3732b + i4);
        }
    }

    @Override // android.view.View
    protected void onRestoreInstanceState(Parcelable parcelable) {
        Bundle bundle = (Bundle) parcelable;
        super.onRestoreInstanceState(bundle.getParcelable("parent"));
        setColor(Color.HSVToColor(bundle.getFloatArray("color")));
        setSaturation(bundle.getFloat("saturation"));
    }

    @Override // android.view.View
    protected Parcelable onSaveInstanceState() {
        Parcelable parcelableOnSaveInstanceState = super.onSaveInstanceState();
        Bundle bundle = new Bundle();
        bundle.putParcelable("parent", parcelableOnSaveInstanceState);
        bundle.putFloatArray("color", this.f3744n);
        float[] fArr = new float[3];
        Color.colorToHSV(this.f3743m, fArr);
        bundle.putFloat("saturation", fArr[1]);
        return bundle;
    }

    @Override // android.view.View
    protected void onSizeChanged(int i, int i2, int i3, int i4) {
        int i5;
        int i6;
        super.onSizeChanged(i, i2, i3, i4);
        if (this.f3748r) {
            int i7 = this.f3735e + this.f3732b;
            int i8 = this.f3731a;
            this.f3732b = i - (this.f3735e * 2);
            this.f3740j.set(this.f3735e, this.f3735e - (this.f3731a / 2), this.f3732b + this.f3735e, this.f3735e + (this.f3731a / 2));
            i5 = i8;
            i6 = i7;
        } else {
            int i9 = this.f3731a;
            int i10 = this.f3732b + this.f3735e;
            this.f3732b = i2 - (this.f3735e * 2);
            this.f3740j.set(this.f3735e - (this.f3731a / 2), this.f3735e, this.f3735e + (this.f3731a / 2), this.f3732b + this.f3735e);
            i5 = i10;
            i6 = i9;
        }
        if (isInEditMode()) {
            this.f3741k = new LinearGradient(this.f3735e, 0.0f, i6, i5, new int[]{-1, -8257792}, (float[]) null, Shader.TileMode.CLAMP);
            Color.colorToHSV(-8257792, this.f3744n);
        } else {
            this.f3741k = new LinearGradient(this.f3735e, 0.0f, i6, i5, new int[]{-1, Color.HSVToColor(255, this.f3744n)}, (float[]) null, Shader.TileMode.CLAMP);
        }
        this.f3737g.setShader(this.f3741k);
        this.f3745o = 1.0f / this.f3732b;
        this.f3746p = this.f3732b / 1.0f;
        float[] fArr = new float[3];
        Color.colorToHSV(this.f3743m, fArr);
        if (isInEditMode()) {
            this.f3736f = this.f3732b + this.f3735e;
        } else {
            this.f3736f = Math.round((fArr[1] * this.f3746p) + this.f3735e);
        }
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        getParent().requestDisallowInterceptTouchEvent(true);
        float x = this.f3748r ? motionEvent.getX() : motionEvent.getY();
        switch (motionEvent.getAction()) {
            case 0:
                this.f3742l = true;
                if (x >= this.f3735e && x <= this.f3735e + this.f3732b) {
                    this.f3736f = Math.round(x);
                    m4442a(Math.round(x));
                    this.f3738h.setColor(this.f3743m);
                    invalidate();
                }
                return true;
            case 1:
                this.f3742l = false;
                return true;
            case 2:
                if (this.f3742l) {
                    if (x >= this.f3735e && x <= this.f3735e + this.f3732b) {
                        this.f3736f = Math.round(x);
                        m4442a(Math.round(x));
                        this.f3738h.setColor(this.f3743m);
                        if (this.f3747q != null) {
                            this.f3747q.setNewCenterColor(this.f3743m);
                            this.f3747q.m4434b(this.f3743m);
                            this.f3747q.m4431a(this.f3743m);
                        }
                        invalidate();
                    } else if (x < this.f3735e) {
                        this.f3736f = this.f3735e;
                        this.f3743m = -1;
                        this.f3738h.setColor(this.f3743m);
                        if (this.f3747q != null) {
                            this.f3747q.setNewCenterColor(this.f3743m);
                            this.f3747q.m4434b(this.f3743m);
                            this.f3747q.m4431a(this.f3743m);
                        }
                        invalidate();
                    } else if (x > this.f3735e + this.f3732b) {
                        this.f3736f = this.f3735e + this.f3732b;
                        this.f3743m = Color.HSVToColor(this.f3744n);
                        this.f3738h.setColor(this.f3743m);
                        if (this.f3747q != null) {
                            this.f3747q.setNewCenterColor(this.f3743m);
                            this.f3747q.m4434b(this.f3743m);
                            this.f3747q.m4431a(this.f3743m);
                        }
                        invalidate();
                    }
                }
                if (this.f3749s != null && this.f3750t != this.f3743m) {
                    this.f3749s.m4443a(this.f3743m);
                    this.f3750t = this.f3743m;
                }
                return true;
            default:
                return true;
        }
    }

    public void setColor(int i) {
        int i2;
        int i3;
        if (this.f3748r) {
            int i4 = this.f3735e + this.f3732b;
            i2 = this.f3731a;
            i3 = i4;
        } else {
            int i5 = this.f3731a;
            i2 = this.f3732b + this.f3735e;
            i3 = i5;
        }
        Color.colorToHSV(i, this.f3744n);
        this.f3741k = new LinearGradient(this.f3735e, 0.0f, i3, i2, new int[]{-1, i}, (float[]) null, Shader.TileMode.CLAMP);
        this.f3737g.setShader(this.f3741k);
        m4442a(this.f3736f);
        this.f3738h.setColor(this.f3743m);
        if (this.f3747q != null) {
            this.f3747q.setNewCenterColor(this.f3743m);
            if (this.f3747q.m4435b()) {
                this.f3747q.m4434b(this.f3743m);
            } else if (this.f3747q.m4433a()) {
                this.f3747q.m4431a(this.f3743m);
            }
        }
        invalidate();
    }

    public void setColorPicker(ColorPicker colorPicker) {
        this.f3747q = colorPicker;
    }

    public void setOnSaturationChangedListener(InterfaceC0771i interfaceC0771i) {
        this.f3749s = interfaceC0771i;
    }

    public void setSaturation(float f) {
        this.f3736f = Math.round(this.f3746p * f) + this.f3735e;
        m4442a(this.f3736f);
        this.f3738h.setColor(this.f3743m);
        if (this.f3747q != null) {
            this.f3747q.setNewCenterColor(this.f3743m);
            this.f3747q.m4434b(this.f3743m);
            this.f3747q.m4431a(this.f3743m);
        }
        invalidate();
    }
}
