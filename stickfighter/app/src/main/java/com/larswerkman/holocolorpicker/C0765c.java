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

/* JADX INFO: renamed from: com.larswerkman.holocolorpicker.c */
/* JADX INFO: loaded from: classes.dex */
public class C0765c extends View {
    public C0765c(android.content.Context c) { super(c); }
    public C0765c(android.content.Context c, android.util.AttributeSet a) { super(c, a); }
    public C0765c(android.content.Context c, android.util.AttributeSet a, int d) { super(c, a, d); }

    /* JADX INFO: renamed from: a */
    private int f3711a;

    /* JADX INFO: renamed from: b */
    private int f3712b;

    /* JADX INFO: renamed from: c */
    private int f3713c;

    /* JADX INFO: renamed from: d */
    private int f3714d;

    /* JADX INFO: renamed from: e */
    private int f3715e;

    /* JADX INFO: renamed from: f */
    private int f3716f;

    /* JADX INFO: renamed from: g */
    private Paint f3717g;

    /* JADX INFO: renamed from: h */
    private Paint f3718h;

    /* JADX INFO: renamed from: i */
    private Paint f3719i;

    /* JADX INFO: renamed from: j */
    private RectF f3720j;

    /* JADX INFO: renamed from: k */
    private Shader f3721k;

    /* JADX INFO: renamed from: l */
    private boolean f3722l;

    /* JADX INFO: renamed from: m */
    private int f3723m;

    /* JADX INFO: renamed from: n */
    private float[] f3724n;

    /* JADX INFO: renamed from: o */
    private float f3725o;

    /* JADX INFO: renamed from: p */
    private float f3726p;

    /* JADX INFO: renamed from: q */
    private InterfaceC0766d f3727q;

    /* JADX INFO: renamed from: r */
    private int f3728r;

    /* JADX INFO: renamed from: s */
    private ColorPicker f3729s;

    /* JADX INFO: renamed from: t */
    private boolean f3730t;

    /* JADX INFO: renamed from: a */
    private void m4440a(int i) {
        int i2 = i - this.f3715e;
        if (i2 < 0) {
            i2 = 0;
        } else if (i2 > this.f3712b) {
            i2 = this.f3712b;
        }
        this.f3723m = Color.HSVToColor(Math.round(i2 * this.f3725o), this.f3724n);
        if (Color.alpha(this.f3723m) > 250) {
            this.f3723m = Color.HSVToColor(this.f3724n);
        } else if (Color.alpha(this.f3723m) < 5) {
            this.f3723m = 0;
        }
    }

    public int getColor() {
        return this.f3723m;
    }

    public InterfaceC0766d getOnOpacityChangedListener() {
        return this.f3727q;
    }

    public int getOpacity() {
        int iRound = Math.round(this.f3725o * (this.f3716f - this.f3715e));
        if (iRound < 5) {
            return 0;
        }
        if (iRound > 250) {
            return 255;
        }
        return iRound;
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        int i;
        int i2;
        canvas.drawRect(this.f3720j, this.f3717g);
        if (this.f3730t) {
            i = this.f3716f;
            i2 = this.f3715e;
        } else {
            i = this.f3715e;
            i2 = this.f3716f;
        }
        canvas.drawCircle(i, i2, this.f3715e, this.f3719i);
        canvas.drawCircle(i, i2, this.f3714d, this.f3718h);
    }

    @Override // android.view.View
    protected void onMeasure(int i, int i2) {
        int i3 = (this.f3715e * 2) + this.f3713c;
        if (!this.f3730t) {
            i = i2;
        }
        int mode = View.MeasureSpec.getMode(i);
        int size = View.MeasureSpec.getSize(i);
        if (mode != 1073741824) {
            size = mode == Integer.MIN_VALUE ? Math.min(i3, size) : i3;
        }
        int i4 = this.f3715e * 2;
        this.f3712b = size - i4;
        if (this.f3730t) {
            setMeasuredDimension(this.f3712b + i4, i4);
        } else {
            setMeasuredDimension(i4, this.f3712b + i4);
        }
    }

    @Override // android.view.View
    protected void onRestoreInstanceState(Parcelable parcelable) {
        Bundle bundle = (Bundle) parcelable;
        super.onRestoreInstanceState(bundle.getParcelable("parent"));
        setColor(Color.HSVToColor(bundle.getFloatArray("color")));
        setOpacity(bundle.getInt("opacity"));
    }

    @Override // android.view.View
    protected Parcelable onSaveInstanceState() {
        Parcelable parcelableOnSaveInstanceState = super.onSaveInstanceState();
        Bundle bundle = new Bundle();
        bundle.putParcelable("parent", parcelableOnSaveInstanceState);
        bundle.putFloatArray("color", this.f3724n);
        bundle.putInt("opacity", getOpacity());
        return bundle;
    }

    @Override // android.view.View
    protected void onSizeChanged(int i, int i2, int i3, int i4) {
        int i5;
        int i6;
        super.onSizeChanged(i, i2, i3, i4);
        if (this.f3730t) {
            int i7 = this.f3715e + this.f3712b;
            int i8 = this.f3711a;
            this.f3712b = i - (this.f3715e * 2);
            this.f3720j.set(this.f3715e, this.f3715e - (this.f3711a / 2), this.f3712b + this.f3715e, this.f3715e + (this.f3711a / 2));
            i5 = i8;
            i6 = i7;
        } else {
            int i9 = this.f3711a;
            int i10 = this.f3712b + this.f3715e;
            this.f3712b = i2 - (this.f3715e * 2);
            this.f3720j.set(this.f3715e - (this.f3711a / 2), this.f3715e, this.f3715e + (this.f3711a / 2), this.f3712b + this.f3715e);
            i5 = i10;
            i6 = i9;
        }
        if (isInEditMode()) {
            this.f3721k = new LinearGradient(this.f3715e, 0.0f, i6, i5, new int[]{8519424, -8257792}, (float[]) null, Shader.TileMode.CLAMP);
            Color.colorToHSV(-8257792, this.f3724n);
        } else {
            this.f3721k = new LinearGradient(this.f3715e, 0.0f, i6, i5, new int[]{Color.HSVToColor(0, this.f3724n), Color.HSVToColor(255, this.f3724n)}, (float[]) null, Shader.TileMode.CLAMP);
        }
        this.f3717g.setShader(this.f3721k);
        this.f3725o = 255.0f / this.f3712b;
        this.f3726p = this.f3712b / 255.0f;
        Color.colorToHSV(this.f3723m, new float[3]);
        if (isInEditMode()) {
            this.f3716f = this.f3712b + this.f3715e;
        } else {
            this.f3716f = Math.round((this.f3726p * Color.alpha(this.f3723m)) + this.f3715e);
        }
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        getParent().requestDisallowInterceptTouchEvent(true);
        float x = this.f3730t ? motionEvent.getX() : motionEvent.getY();
        switch (motionEvent.getAction()) {
            case 0:
                this.f3722l = true;
                if (x >= this.f3715e && x <= this.f3715e + this.f3712b) {
                    this.f3716f = Math.round(x);
                    m4440a(Math.round(x));
                    this.f3718h.setColor(this.f3723m);
                    invalidate();
                }
                return true;
            case 1:
                this.f3722l = false;
                return true;
            case 2:
                if (this.f3722l) {
                    if (x >= this.f3715e && x <= this.f3715e + this.f3712b) {
                        this.f3716f = Math.round(x);
                        m4440a(Math.round(x));
                        this.f3718h.setColor(this.f3723m);
                        if (this.f3729s != null) {
                            this.f3729s.setNewCenterColor(this.f3723m);
                        }
                        invalidate();
                    } else if (x < this.f3715e) {
                        this.f3716f = this.f3715e;
                        this.f3723m = 0;
                        this.f3718h.setColor(this.f3723m);
                        if (this.f3729s != null) {
                            this.f3729s.setNewCenterColor(this.f3723m);
                        }
                        invalidate();
                    } else if (x > this.f3715e + this.f3712b) {
                        this.f3716f = this.f3715e + this.f3712b;
                        this.f3723m = Color.HSVToColor(this.f3724n);
                        this.f3718h.setColor(this.f3723m);
                        if (this.f3729s != null) {
                            this.f3729s.setNewCenterColor(this.f3723m);
                        }
                        invalidate();
                    }
                }
                if (this.f3727q != null && this.f3728r != getOpacity()) {
                    this.f3727q.m4441a(getOpacity());
                    this.f3728r = getOpacity();
                }
                return true;
            default:
                return true;
        }
    }

    public void setColor(int i) {
        int i2;
        int i3;
        if (this.f3730t) {
            int i4 = this.f3715e + this.f3712b;
            i2 = this.f3711a;
            i3 = i4;
        } else {
            int i5 = this.f3711a;
            i2 = this.f3712b + this.f3715e;
            i3 = i5;
        }
        Color.colorToHSV(i, this.f3724n);
        this.f3721k = new LinearGradient(this.f3715e, 0.0f, i3, i2, new int[]{Color.HSVToColor(0, this.f3724n), i}, (float[]) null, Shader.TileMode.CLAMP);
        this.f3717g.setShader(this.f3721k);
        m4440a(this.f3716f);
        this.f3718h.setColor(this.f3723m);
        if (this.f3729s != null) {
            this.f3729s.setNewCenterColor(this.f3723m);
        }
        invalidate();
    }

    public void setColorPicker(ColorPicker colorPicker) {
        this.f3729s = colorPicker;
    }

    public void setOnOpacityChangedListener(InterfaceC0766d interfaceC0766d) {
        this.f3727q = interfaceC0766d;
    }

    public void setOpacity(int i) {
        this.f3716f = Math.round(this.f3726p * i) + this.f3715e;
        m4440a(this.f3716f);
        this.f3718h.setColor(this.f3723m);
        if (this.f3729s != null) {
            this.f3729s.setNewCenterColor(this.f3723m);
        }
        invalidate();
    }
}
