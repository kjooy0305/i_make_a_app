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

/* JADX INFO: renamed from: com.larswerkman.holocolorpicker.j */
/* JADX INFO: loaded from: classes.dex */
public class C0772j extends View {
    public C0772j(android.content.Context c) { super(c); }
    public C0772j(android.content.Context c, android.util.AttributeSet a) { super(c, a); }
    public C0772j(android.content.Context c, android.util.AttributeSet a, int d) { super(c, a, d); }

    /* JADX INFO: renamed from: a */
    private int f3751a;

    /* JADX INFO: renamed from: b */
    private int f3752b;

    /* JADX INFO: renamed from: c */
    private int f3753c;

    /* JADX INFO: renamed from: d */
    private int f3754d;

    /* JADX INFO: renamed from: e */
    private int f3755e;

    /* JADX INFO: renamed from: f */
    private int f3756f;

    /* JADX INFO: renamed from: g */
    private Paint f3757g;

    /* JADX INFO: renamed from: h */
    private Paint f3758h;

    /* JADX INFO: renamed from: i */
    private Paint f3759i;

    /* JADX INFO: renamed from: j */
    private RectF f3760j;

    /* JADX INFO: renamed from: k */
    private Shader f3761k;

    /* JADX INFO: renamed from: l */
    private boolean f3762l;

    /* JADX INFO: renamed from: m */
    private int f3763m;

    /* JADX INFO: renamed from: n */
    private float[] f3764n;

    /* JADX INFO: renamed from: o */
    private float f3765o;

    /* JADX INFO: renamed from: p */
    private float f3766p;

    /* JADX INFO: renamed from: q */
    private ColorPicker f3767q;

    /* JADX INFO: renamed from: r */
    private boolean f3768r;

    /* JADX INFO: renamed from: s */
    private InterfaceC0773k f3769s;

    /* JADX INFO: renamed from: t */
    private int f3770t;

    /* JADX INFO: renamed from: a */
    private void m4444a(int i) {
        int i2 = i - this.f3755e;
        if (i2 < 0) {
            i2 = 0;
        } else if (i2 > this.f3752b) {
            i2 = this.f3752b;
        }
        this.f3763m = Color.HSVToColor(new float[]{this.f3764n[0], this.f3764n[1], 1.0f - (i2 * this.f3765o)});
    }

    public int getColor() {
        return this.f3763m;
    }

    public InterfaceC0773k getOnValueChangedListener() {
        return this.f3769s;
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        int i;
        int i2;
        canvas.drawRect(this.f3760j, this.f3757g);
        if (this.f3768r) {
            i = this.f3756f;
            i2 = this.f3755e;
        } else {
            i = this.f3755e;
            i2 = this.f3756f;
        }
        canvas.drawCircle(i, i2, this.f3755e, this.f3759i);
        canvas.drawCircle(i, i2, this.f3754d, this.f3758h);
    }

    @Override // android.view.View
    protected void onMeasure(int i, int i2) {
        int i3 = (this.f3755e * 2) + this.f3753c;
        if (!this.f3768r) {
            i = i2;
        }
        int mode = View.MeasureSpec.getMode(i);
        int size = View.MeasureSpec.getSize(i);
        if (mode != 1073741824) {
            size = mode == Integer.MIN_VALUE ? Math.min(i3, size) : i3;
        }
        int i4 = this.f3755e * 2;
        this.f3752b = size - i4;
        if (this.f3768r) {
            setMeasuredDimension(this.f3752b + i4, i4);
        } else {
            setMeasuredDimension(i4, this.f3752b + i4);
        }
    }

    @Override // android.view.View
    protected void onRestoreInstanceState(Parcelable parcelable) {
        Bundle bundle = (Bundle) parcelable;
        super.onRestoreInstanceState(bundle.getParcelable("parent"));
        setColor(Color.HSVToColor(bundle.getFloatArray("color")));
        setValue(bundle.getFloat("value"));
    }

    @Override // android.view.View
    protected Parcelable onSaveInstanceState() {
        Parcelable parcelableOnSaveInstanceState = super.onSaveInstanceState();
        Bundle bundle = new Bundle();
        bundle.putParcelable("parent", parcelableOnSaveInstanceState);
        bundle.putFloatArray("color", this.f3764n);
        float[] fArr = new float[3];
        Color.colorToHSV(this.f3763m, fArr);
        bundle.putFloat("value", fArr[2]);
        return bundle;
    }

    @Override // android.view.View
    protected void onSizeChanged(int i, int i2, int i3, int i4) {
        int i5;
        int i6;
        super.onSizeChanged(i, i2, i3, i4);
        if (this.f3768r) {
            int i7 = this.f3755e + this.f3752b;
            int i8 = this.f3751a;
            this.f3752b = i - (this.f3755e * 2);
            this.f3760j.set(this.f3755e, this.f3755e - (this.f3751a / 2), this.f3752b + this.f3755e, this.f3755e + (this.f3751a / 2));
            i5 = i8;
            i6 = i7;
        } else {
            int i9 = this.f3751a;
            int i10 = this.f3752b + this.f3755e;
            this.f3752b = i2 - (this.f3755e * 2);
            this.f3760j.set(this.f3755e - (this.f3751a / 2), this.f3755e, this.f3755e + (this.f3751a / 2), this.f3752b + this.f3755e);
            i5 = i10;
            i6 = i9;
        }
        if (isInEditMode()) {
            this.f3761k = new LinearGradient(this.f3755e, 0.0f, i6, i5, new int[]{-8257792, -16777216}, (float[]) null, Shader.TileMode.CLAMP);
            Color.colorToHSV(-8257792, this.f3764n);
        } else {
            this.f3761k = new LinearGradient(this.f3755e, 0.0f, i6, i5, new int[]{Color.HSVToColor(255, this.f3764n), -16777216}, (float[]) null, Shader.TileMode.CLAMP);
        }
        this.f3757g.setShader(this.f3761k);
        this.f3765o = 1.0f / this.f3752b;
        this.f3766p = this.f3752b / 1.0f;
        float[] fArr = new float[3];
        Color.colorToHSV(this.f3763m, fArr);
        if (isInEditMode()) {
            this.f3756f = this.f3755e;
        } else {
            this.f3756f = Math.round((this.f3752b - (fArr[2] * this.f3766p)) + this.f3755e);
        }
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        getParent().requestDisallowInterceptTouchEvent(true);
        float x = this.f3768r ? motionEvent.getX() : motionEvent.getY();
        switch (motionEvent.getAction()) {
            case 0:
                this.f3762l = true;
                if (x >= this.f3755e && x <= this.f3755e + this.f3752b) {
                    this.f3756f = Math.round(x);
                    m4444a(Math.round(x));
                    this.f3758h.setColor(this.f3763m);
                    invalidate();
                }
                return true;
            case 1:
                this.f3762l = false;
                return true;
            case 2:
                if (this.f3762l) {
                    if (x >= this.f3755e && x <= this.f3755e + this.f3752b) {
                        this.f3756f = Math.round(x);
                        m4444a(Math.round(x));
                        this.f3758h.setColor(this.f3763m);
                        if (this.f3767q != null) {
                            this.f3767q.setNewCenterColor(this.f3763m);
                            this.f3767q.m4431a(this.f3763m);
                        }
                        invalidate();
                    } else if (x < this.f3755e) {
                        this.f3756f = this.f3755e;
                        this.f3763m = Color.HSVToColor(this.f3764n);
                        this.f3758h.setColor(this.f3763m);
                        if (this.f3767q != null) {
                            this.f3767q.setNewCenterColor(this.f3763m);
                            this.f3767q.m4431a(this.f3763m);
                        }
                        invalidate();
                    } else if (x > this.f3755e + this.f3752b) {
                        this.f3756f = this.f3755e + this.f3752b;
                        this.f3763m = -16777216;
                        this.f3758h.setColor(this.f3763m);
                        if (this.f3767q != null) {
                            this.f3767q.setNewCenterColor(this.f3763m);
                            this.f3767q.m4431a(this.f3763m);
                        }
                        invalidate();
                    }
                }
                if (this.f3769s != null && this.f3770t != this.f3763m) {
                    this.f3769s.m4445a(this.f3763m);
                    this.f3770t = this.f3763m;
                }
                return true;
            default:
                return true;
        }
    }

    public void setColor(int i) {
        int i2;
        int i3;
        if (this.f3768r) {
            int i4 = this.f3755e + this.f3752b;
            i2 = this.f3751a;
            i3 = i4;
        } else {
            int i5 = this.f3751a;
            i2 = this.f3752b + this.f3755e;
            i3 = i5;
        }
        Color.colorToHSV(i, this.f3764n);
        this.f3761k = new LinearGradient(this.f3755e, 0.0f, i3, i2, new int[]{i, -16777216}, (float[]) null, Shader.TileMode.CLAMP);
        this.f3757g.setShader(this.f3761k);
        m4444a(this.f3756f);
        this.f3758h.setColor(this.f3763m);
        if (this.f3767q != null) {
            this.f3767q.setNewCenterColor(this.f3763m);
            if (this.f3767q.m4433a()) {
                this.f3767q.m4431a(this.f3763m);
            }
        }
        invalidate();
    }

    public void setColorPicker(ColorPicker colorPicker) {
        this.f3767q = colorPicker;
    }

    public void setOnValueChangedListener(InterfaceC0773k interfaceC0773k) {
        this.f3769s = interfaceC0773k;
    }

    public void setValue(float f) {
        this.f3756f = Math.round((this.f3752b - (this.f3766p * f)) + this.f3755e);
        m4444a(this.f3756f);
        this.f3758h.setColor(this.f3763m);
        if (this.f3767q != null) {
            this.f3767q.setNewCenterColor(this.f3763m);
            this.f3767q.m4431a(this.f3763m);
        }
        invalidate();
    }
}
