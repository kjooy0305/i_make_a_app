package com.larswerkman.holocolorpicker;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/* JADX INFO: loaded from: classes.dex */
public class SVBar extends View {

    /* JADX INFO: renamed from: a */
    private int f3693a;

    /* JADX INFO: renamed from: b */
    private int f3694b;

    /* JADX INFO: renamed from: c */
    private int f3695c;

    /* JADX INFO: renamed from: d */
    private int f3696d;

    /* JADX INFO: renamed from: e */
    private int f3697e;

    /* JADX INFO: renamed from: f */
    private int f3698f;

    /* JADX INFO: renamed from: g */
    private Paint f3699g;

    /* JADX INFO: renamed from: h */
    private Paint f3700h;

    /* JADX INFO: renamed from: i */
    private Paint f3701i;

    /* JADX INFO: renamed from: j */
    private RectF f3702j;

    /* JADX INFO: renamed from: k */
    private Shader f3703k;

    /* JADX INFO: renamed from: l */
    private boolean f3704l;

    /* JADX INFO: renamed from: m */
    private int f3705m;

    /* JADX INFO: renamed from: n */
    private float[] f3706n;

    /* JADX INFO: renamed from: o */
    private float f3707o;

    /* JADX INFO: renamed from: p */
    private float f3708p;

    /* JADX INFO: renamed from: q */
    private ColorPicker f3709q;

    /* JADX INFO: renamed from: r */
    private boolean f3710r;

    public SVBar(Context context) {
        super(context);
        this.f3702j = new RectF();
        this.f3706n = new float[3];
        this.f3709q = null;
        m4437a(null, 0);
    }

    public SVBar(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.f3702j = new RectF();
        this.f3706n = new float[3];
        this.f3709q = null;
        m4437a(attributeSet, 0);
    }

    public SVBar(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.f3702j = new RectF();
        this.f3706n = new float[3];
        this.f3709q = null;
        m4437a(attributeSet, i);
    }

    /* JADX INFO: renamed from: a */
    private void m4436a(int i) {
        int i2 = i - this.f3697e;
        if (i2 > this.f3694b / 2 && i2 < this.f3694b) {
            this.f3705m = Color.HSVToColor(new float[]{this.f3706n[0], 1.0f, 1.0f - ((i2 - (this.f3694b / 2)) * this.f3707o)});
            return;
        }
        if (i2 > 0 && i2 < this.f3694b) {
            this.f3705m = Color.HSVToColor(new float[]{this.f3706n[0], i2 * this.f3707o, 1.0f});
            return;
        }
        if (i2 == this.f3694b / 2) {
            this.f3705m = Color.HSVToColor(new float[]{this.f3706n[0], 1.0f, 1.0f});
        } else if (i2 <= 0) {
            this.f3705m = -1;
        } else if (i2 >= this.f3694b) {
            this.f3705m = -16777216;
        }
    }

    /* JADX INFO: renamed from: a */
    private void m4437a(AttributeSet attributeSet, int i) {
        TypedArray typedArrayObtainStyledAttributes = getContext().obtainStyledAttributes(attributeSet, C0769g.ColorBars, i, 0);
        Resources resources = getContext().getResources();
        this.f3693a = typedArrayObtainStyledAttributes.getDimensionPixelSize(C0769g.ColorBars_bar_thickness, resources.getDimensionPixelSize(C0768f.bar_thickness));
        this.f3694b = typedArrayObtainStyledAttributes.getDimensionPixelSize(C0769g.ColorBars_bar_length, resources.getDimensionPixelSize(C0768f.bar_length));
        this.f3695c = this.f3694b;
        this.f3696d = typedArrayObtainStyledAttributes.getDimensionPixelSize(C0769g.ColorBars_bar_pointer_radius, resources.getDimensionPixelSize(C0768f.bar_pointer_radius));
        this.f3697e = typedArrayObtainStyledAttributes.getDimensionPixelSize(C0769g.ColorBars_bar_pointer_halo_radius, resources.getDimensionPixelSize(C0768f.bar_pointer_halo_radius));
        this.f3710r = typedArrayObtainStyledAttributes.getBoolean(C0769g.ColorBars_bar_orientation_horizontal, true);
        typedArrayObtainStyledAttributes.recycle();
        this.f3699g = new Paint(1);
        this.f3699g.setShader(this.f3703k);
        this.f3698f = (this.f3694b / 2) + this.f3697e;
        this.f3701i = new Paint(1);
        this.f3701i.setColor(-16777216);
        this.f3701i.setAlpha(80);
        this.f3700h = new Paint(1);
        this.f3700h.setColor(-8257792);
        this.f3707o = 1.0f / (this.f3694b / 2.0f);
        this.f3708p = (this.f3694b / 2.0f) / 1.0f;
    }

    public int getColor() {
        return this.f3705m;
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        int i;
        int i2;
        canvas.drawRect(this.f3702j, this.f3699g);
        if (this.f3710r) {
            i = this.f3698f;
            i2 = this.f3697e;
        } else {
            i = this.f3697e;
            i2 = this.f3698f;
        }
        canvas.drawCircle(i, i2, this.f3697e, this.f3701i);
        canvas.drawCircle(i, i2, this.f3696d, this.f3700h);
    }

    @Override // android.view.View
    protected void onMeasure(int i, int i2) {
        int i3 = (this.f3697e * 2) + this.f3695c;
        if (!this.f3710r) {
            i = i2;
        }
        int mode = View.MeasureSpec.getMode(i);
        int size = View.MeasureSpec.getSize(i);
        if (mode != 1073741824) {
            size = mode == Integer.MIN_VALUE ? Math.min(i3, size) : i3;
        }
        int i4 = this.f3697e * 2;
        this.f3694b = size - i4;
        if (this.f3710r) {
            setMeasuredDimension(this.f3694b + i4, i4);
        } else {
            setMeasuredDimension(i4, this.f3694b + i4);
        }
    }

    @Override // android.view.View
    protected void onRestoreInstanceState(Parcelable parcelable) {
        Bundle bundle = (Bundle) parcelable;
        super.onRestoreInstanceState(bundle.getParcelable("parent"));
        setColor(Color.HSVToColor(bundle.getFloatArray("color")));
        if (bundle.containsKey("saturation")) {
            setSaturation(bundle.getFloat("saturation"));
        } else {
            setValue(bundle.getFloat("value"));
        }
    }

    @Override // android.view.View
    protected Parcelable onSaveInstanceState() {
        Parcelable parcelableOnSaveInstanceState = super.onSaveInstanceState();
        Bundle bundle = new Bundle();
        bundle.putParcelable("parent", parcelableOnSaveInstanceState);
        bundle.putFloatArray("color", this.f3706n);
        float[] fArr = new float[3];
        Color.colorToHSV(this.f3705m, fArr);
        if (fArr[1] < fArr[2]) {
            bundle.putFloat("saturation", fArr[1]);
        } else {
            bundle.putFloat("value", fArr[2]);
        }
        return bundle;
    }

    @Override // android.view.View
    protected void onSizeChanged(int i, int i2, int i3, int i4) {
        int i5;
        int i6;
        super.onSizeChanged(i, i2, i3, i4);
        if (this.f3710r) {
            int i7 = this.f3697e + this.f3694b;
            int i8 = this.f3693a;
            this.f3694b = i - (this.f3697e * 2);
            this.f3702j.set(this.f3697e, this.f3697e - (this.f3693a / 2), this.f3694b + this.f3697e, this.f3697e + (this.f3693a / 2));
            i5 = i8;
            i6 = i7;
        } else {
            int i9 = this.f3693a;
            int i10 = this.f3694b + this.f3697e;
            this.f3694b = i2 - (this.f3697e * 2);
            this.f3702j.set(this.f3697e - (this.f3693a / 2), this.f3697e, this.f3697e + (this.f3693a / 2), this.f3694b + this.f3697e);
            i5 = i10;
            i6 = i9;
        }
        if (isInEditMode()) {
            this.f3703k = new LinearGradient(this.f3697e, 0.0f, i6, i5, new int[]{-1, -8257792, -16777216}, (float[]) null, Shader.TileMode.CLAMP);
            Color.colorToHSV(-8257792, this.f3706n);
        } else {
            this.f3703k = new LinearGradient(this.f3697e, 0.0f, i6, i5, new int[]{-1, Color.HSVToColor(this.f3706n), -16777216}, (float[]) null, Shader.TileMode.CLAMP);
        }
        this.f3699g.setShader(this.f3703k);
        this.f3707o = 1.0f / (this.f3694b / 2.0f);
        this.f3708p = (this.f3694b / 2.0f) / 1.0f;
        float[] fArr = new float[3];
        Color.colorToHSV(this.f3705m, fArr);
        if (fArr[1] < fArr[2]) {
            this.f3698f = Math.round((fArr[1] * this.f3708p) + this.f3697e);
        } else {
            this.f3698f = Math.round(((1.0f - fArr[2]) * this.f3708p) + this.f3697e + (this.f3694b / 2));
        }
        if (isInEditMode()) {
            this.f3698f = (this.f3694b / 2) + this.f3697e;
        }
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        getParent().requestDisallowInterceptTouchEvent(true);
        float x = this.f3710r ? motionEvent.getX() : motionEvent.getY();
        switch (motionEvent.getAction()) {
            case 0:
                this.f3704l = true;
                if (x >= this.f3697e && x <= this.f3697e + this.f3694b) {
                    this.f3698f = Math.round(x);
                    m4436a(Math.round(x));
                    this.f3700h.setColor(this.f3705m);
                    invalidate();
                }
                return true;
            case 1:
                this.f3704l = false;
                return true;
            case 2:
                if (this.f3704l) {
                    if (x >= this.f3697e && x <= this.f3697e + this.f3694b) {
                        this.f3698f = Math.round(x);
                        m4436a(Math.round(x));
                        this.f3700h.setColor(this.f3705m);
                        if (this.f3709q != null) {
                            this.f3709q.setNewCenterColor(this.f3705m);
                            this.f3709q.m4431a(this.f3705m);
                        }
                        invalidate();
                    } else if (x < this.f3697e) {
                        this.f3698f = this.f3697e;
                        this.f3705m = -1;
                        this.f3700h.setColor(this.f3705m);
                        if (this.f3709q != null) {
                            this.f3709q.setNewCenterColor(this.f3705m);
                            this.f3709q.m4431a(this.f3705m);
                        }
                        invalidate();
                    } else if (x > this.f3697e + this.f3694b) {
                        this.f3698f = this.f3697e + this.f3694b;
                        this.f3705m = -16777216;
                        this.f3700h.setColor(this.f3705m);
                        if (this.f3709q != null) {
                            this.f3709q.setNewCenterColor(this.f3705m);
                            this.f3709q.m4431a(this.f3705m);
                        }
                        invalidate();
                    }
                }
                return true;
            default:
                return true;
        }
    }

    public void setColor(int i) {
        int i2;
        int i3;
        if (this.f3710r) {
            int i4 = this.f3697e + this.f3694b;
            i2 = this.f3693a;
            i3 = i4;
        } else {
            int i5 = this.f3693a;
            i2 = this.f3694b + this.f3697e;
            i3 = i5;
        }
        Color.colorToHSV(i, this.f3706n);
        this.f3703k = new LinearGradient(this.f3697e, 0.0f, i3, i2, new int[]{-1, i, -16777216}, (float[]) null, Shader.TileMode.CLAMP);
        this.f3699g.setShader(this.f3703k);
        m4436a(this.f3698f);
        this.f3700h.setColor(this.f3705m);
        if (this.f3709q != null) {
            this.f3709q.setNewCenterColor(this.f3705m);
            if (this.f3709q.m4433a()) {
                this.f3709q.m4431a(this.f3705m);
            }
        }
        invalidate();
    }

    public void setColorPicker(ColorPicker colorPicker) {
        this.f3709q = colorPicker;
    }

    public void setSaturation(float f) {
        this.f3698f = Math.round((this.f3708p * f) + this.f3697e);
        m4436a(this.f3698f);
        this.f3700h.setColor(this.f3705m);
        if (this.f3709q != null) {
            this.f3709q.setNewCenterColor(this.f3705m);
            this.f3709q.m4431a(this.f3705m);
        }
        invalidate();
    }

    public void setValue(float f) {
        this.f3698f = Math.round((this.f3708p * (1.0f - f)) + this.f3697e + (this.f3694b / 2));
        m4436a(this.f3698f);
        this.f3700h.setColor(this.f3705m);
        if (this.f3709q != null) {
            this.f3709q.setNewCenterColor(this.f3705m);
            this.f3709q.m4431a(this.f3705m);
        }
        invalidate();
    }
}
