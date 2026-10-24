package com.larswerkman.holocolorpicker;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/* JADX INFO: loaded from: classes.dex */
public class ColorPicker extends View {

    /* JADX INFO: renamed from: a */
    private static final int[] f3656a = {-65536, -65281, -16776961, -16711681, -16711936, -256, -65536};

    /* JADX INFO: renamed from: A */
    private Paint f3657A;

    /* JADX INFO: renamed from: B */
    private float[] f3658B;

    /* JADX INFO: renamed from: C */
    private SVBar f3659C;

    /* JADX INFO: renamed from: D */
    private C0765c f3660D;

    /* JADX INFO: renamed from: E */
    private C0770h f3661E;

    /* JADX INFO: renamed from: F */
    private boolean f3662F;

    /* JADX INFO: renamed from: G */
    private C0772j f3663G;

    /* JADX INFO: renamed from: H */
    private InterfaceC0763a f3664H;

    /* JADX INFO: renamed from: I */
    private InterfaceC0764b f3665I;

    /* JADX INFO: renamed from: J */
    private int f3666J;

    /* JADX INFO: renamed from: K */
    private int f3667K;

    /* JADX INFO: renamed from: b */
    private Paint f3668b;

    /* JADX INFO: renamed from: c */
    private Paint f3669c;

    /* JADX INFO: renamed from: d */
    private Paint f3670d;

    /* JADX INFO: renamed from: e */
    private int f3671e;

    /* JADX INFO: renamed from: f */
    private int f3672f;

    /* JADX INFO: renamed from: g */
    private int f3673g;

    /* JADX INFO: renamed from: h */
    private int f3674h;

    /* JADX INFO: renamed from: i */
    private int f3675i;

    /* JADX INFO: renamed from: j */
    private int f3676j;

    /* JADX INFO: renamed from: k */
    private int f3677k;

    /* JADX INFO: renamed from: l */
    private int f3678l;

    /* JADX INFO: renamed from: m */
    private int f3679m;

    /* JADX INFO: renamed from: n */
    private RectF f3680n;

    /* JADX INFO: renamed from: o */
    private RectF f3681o;

    /* JADX INFO: renamed from: p */
    private boolean f3682p;

    /* JADX INFO: renamed from: q */
    private int f3683q;

    /* JADX INFO: renamed from: r */
    private int f3684r;

    /* JADX INFO: renamed from: s */
    private boolean f3685s;

    /* JADX INFO: renamed from: t */
    private int f3686t;

    /* JADX INFO: renamed from: u */
    private float f3687u;

    /* JADX INFO: renamed from: v */
    private float f3688v;

    /* JADX INFO: renamed from: w */
    private float f3689w;

    /* JADX INFO: renamed from: x */
    private float f3690x;

    /* JADX INFO: renamed from: y */
    private Paint f3691y;

    /* JADX INFO: renamed from: z */
    private Paint f3692z;

    public ColorPicker(Context context) {
        super(context);
        this.f3680n = new RectF();
        this.f3681o = new RectF();
        this.f3682p = false;
        this.f3658B = new float[3];
        this.f3659C = null;
        this.f3660D = null;
        this.f3661E = null;
        this.f3662F = true;
        this.f3663G = null;
        m4428a(null, 0);
    }

    public ColorPicker(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.f3680n = new RectF();
        this.f3681o = new RectF();
        this.f3682p = false;
        this.f3658B = new float[3];
        this.f3659C = null;
        this.f3660D = null;
        this.f3661E = null;
        this.f3662F = true;
        this.f3663G = null;
        m4428a(attributeSet, 0);
    }

    public ColorPicker(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.f3680n = new RectF();
        this.f3681o = new RectF();
        this.f3682p = false;
        this.f3658B = new float[3];
        this.f3659C = null;
        this.f3660D = null;
        this.f3661E = null;
        this.f3662F = true;
        this.f3663G = null;
        m4428a(attributeSet, i);
    }

    /* JADX INFO: renamed from: a */
    private int m4426a(float f) {
        float f2 = (float) (((double) f) / 6.283185307179586d);
        if (f2 < 0.0f) {
            f2 += 1.0f;
        }
        if (f2 <= 0.0f) {
            this.f3683q = f3656a[0];
            return f3656a[0];
        }
        if (f2 >= 1.0f) {
            this.f3683q = f3656a[f3656a.length - 1];
            return f3656a[f3656a.length - 1];
        }
        float length = f2 * (f3656a.length - 1);
        int i = (int) length;
        float f3 = length - i;
        int i2 = f3656a[i];
        int i3 = f3656a[i + 1];
        int iM4427a = m4427a(Color.alpha(i2), Color.alpha(i3), f3);
        int iM4427a2 = m4427a(Color.red(i2), Color.red(i3), f3);
        int iM4427a3 = m4427a(Color.green(i2), Color.green(i3), f3);
        int iM4427a4 = m4427a(Color.blue(i2), Color.blue(i3), f3);
        this.f3683q = Color.argb(iM4427a, iM4427a2, iM4427a3, iM4427a4);
        return Color.argb(iM4427a, iM4427a2, iM4427a3, iM4427a4);
    }

    /* JADX INFO: renamed from: a */
    private int m4427a(int i, int i2, float f) {
        return Math.round((i2 - i) * f) + i;
    }

    /* JADX INFO: renamed from: a */
    private void m4428a(AttributeSet attributeSet, int i) {
        TypedArray typedArrayObtainStyledAttributes = getContext().obtainStyledAttributes(attributeSet, C0769g.ColorPicker, i, 0);
        Resources resources = getContext().getResources();
        this.f3671e = typedArrayObtainStyledAttributes.getDimensionPixelSize(C0769g.ColorPicker_color_wheel_thickness, resources.getDimensionPixelSize(C0768f.color_wheel_thickness));
        this.f3672f = typedArrayObtainStyledAttributes.getDimensionPixelSize(C0769g.ColorPicker_color_wheel_radius, resources.getDimensionPixelSize(C0768f.color_wheel_radius));
        this.f3673g = this.f3672f;
        this.f3674h = typedArrayObtainStyledAttributes.getDimensionPixelSize(C0769g.ColorPicker_color_center_radius, resources.getDimensionPixelSize(C0768f.color_center_radius));
        this.f3675i = this.f3674h;
        this.f3676j = typedArrayObtainStyledAttributes.getDimensionPixelSize(C0769g.ColorPicker_color_center_halo_radius, resources.getDimensionPixelSize(C0768f.color_center_halo_radius));
        this.f3677k = this.f3676j;
        this.f3678l = typedArrayObtainStyledAttributes.getDimensionPixelSize(C0769g.ColorPicker_color_pointer_radius, resources.getDimensionPixelSize(C0768f.color_pointer_radius));
        this.f3679m = typedArrayObtainStyledAttributes.getDimensionPixelSize(C0769g.ColorPicker_color_pointer_halo_radius, resources.getDimensionPixelSize(C0768f.color_pointer_halo_radius));
        typedArrayObtainStyledAttributes.recycle();
        this.f3690x = -1.5707964f;
        SweepGradient sweepGradient = new SweepGradient(0.0f, 0.0f, f3656a, (float[]) null);
        this.f3668b = new Paint(1);
        this.f3668b.setShader(sweepGradient);
        this.f3668b.setStyle(Paint.Style.STROKE);
        this.f3668b.setStrokeWidth(this.f3671e);
        this.f3669c = new Paint(1);
        this.f3669c.setColor(-16777216);
        this.f3669c.setAlpha(80);
        this.f3670d = new Paint(1);
        this.f3670d.setColor(m4426a(this.f3690x));
        this.f3692z = new Paint(1);
        this.f3692z.setColor(m4426a(this.f3690x));
        this.f3692z.setStyle(Paint.Style.FILL);
        this.f3691y = new Paint(1);
        this.f3691y.setColor(m4426a(this.f3690x));
        this.f3691y.setStyle(Paint.Style.FILL);
        this.f3657A = new Paint(1);
        this.f3657A.setColor(-16777216);
        this.f3657A.setAlpha(0);
        this.f3686t = m4426a(this.f3690x);
        this.f3684r = m4426a(this.f3690x);
        this.f3685s = true;
    }

    /* JADX INFO: renamed from: b */
    private float[] m4429b(float f) {
        return new float[]{(float) (((double) this.f3672f) * Math.cos(f)), (float) (((double) this.f3672f) * Math.sin(f))};
    }

    /* JADX INFO: renamed from: c */
    private float m4430c(int i) {
        float[] fArr = new float[3];
        Color.colorToHSV(i, fArr);
        return (float) Math.toRadians(-fArr[0]);
    }

    /* JADX INFO: renamed from: a */
    public void m4431a(int i) {
        if (this.f3660D != null) {
            this.f3660D.setColor(i);
        }
    }

    /* JADX INFO: renamed from: a */
    public void m4432a(SVBar sVBar) {
        this.f3659C = sVBar;
        this.f3659C.setColorPicker(this);
        this.f3659C.setColor(this.f3683q);
    }

    /* JADX INFO: renamed from: a */
    public boolean m4433a() {
        return this.f3660D != null;
    }

    /* JADX INFO: renamed from: b */
    public void m4434b(int i) {
        if (this.f3663G != null) {
            this.f3663G.setColor(i);
        }
    }

    /* JADX INFO: renamed from: b */
    public boolean m4435b() {
        return this.f3663G != null;
    }

    public int getColor() {
        return this.f3686t;
    }

    public int getOldCenterColor() {
        return this.f3684r;
    }

    public InterfaceC0763a getOnColorChangedListener() {
        return this.f3664H;
    }

    public InterfaceC0764b getOnColorSelectedListener() {
        return this.f3665I;
    }

    public boolean getShowOldCenterColor() {
        return this.f3685s;
    }

    public boolean getTouchAnywhereOnColorWheel() {
        return this.f3662F;
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        canvas.translate(this.f3687u, this.f3687u);
        canvas.drawOval(this.f3680n, this.f3668b);
        float[] fArrM4429b = m4429b(this.f3690x);
        canvas.drawCircle(fArrM4429b[0], fArrM4429b[1], this.f3679m, this.f3669c);
        canvas.drawCircle(fArrM4429b[0], fArrM4429b[1], this.f3678l, this.f3670d);
        canvas.drawCircle(0.0f, 0.0f, this.f3676j, this.f3657A);
        if (!this.f3685s) {
            canvas.drawArc(this.f3681o, 0.0f, 360.0f, true, this.f3692z);
        } else {
            canvas.drawArc(this.f3681o, 90.0f, 180.0f, true, this.f3691y);
            canvas.drawArc(this.f3681o, 270.0f, 180.0f, true, this.f3692z);
        }
    }

    @Override // android.view.View
    protected void onMeasure(int i, int i2) {
        int i3 = (this.f3673g + this.f3679m) * 2;
        int mode = View.MeasureSpec.getMode(i);
        int size = View.MeasureSpec.getSize(i);
        int mode2 = View.MeasureSpec.getMode(i2);
        int size2 = View.MeasureSpec.getSize(i2);
        if (mode != 1073741824) {
            size = mode == Integer.MIN_VALUE ? Math.min(i3, size) : i3;
        }
        if (mode2 != 1073741824) {
            size2 = mode2 == Integer.MIN_VALUE ? Math.min(i3, size2) : i3;
        }
        int iMin = Math.min(size, size2);
        setMeasuredDimension(iMin, iMin);
        this.f3687u = iMin * 0.5f;
        this.f3672f = ((iMin / 2) - this.f3671e) - this.f3679m;
        this.f3680n.set(-this.f3672f, -this.f3672f, this.f3672f, this.f3672f);
        this.f3674h = (int) (this.f3675i * (this.f3672f / this.f3673g));
        this.f3676j = (int) (this.f3677k * (this.f3672f / this.f3673g));
        this.f3681o.set(-this.f3674h, -this.f3674h, this.f3674h, this.f3674h);
    }

    @Override // android.view.View
    protected void onRestoreInstanceState(Parcelable parcelable) {
        Bundle bundle = (Bundle) parcelable;
        super.onRestoreInstanceState(bundle.getParcelable("parent"));
        this.f3690x = bundle.getFloat("angle");
        setOldCenterColor(bundle.getInt("color"));
        this.f3685s = bundle.getBoolean("showColor");
        int iM4426a = m4426a(this.f3690x);
        this.f3670d.setColor(iM4426a);
        setNewCenterColor(iM4426a);
    }

    @Override // android.view.View
    protected Parcelable onSaveInstanceState() {
        Parcelable parcelableOnSaveInstanceState = super.onSaveInstanceState();
        Bundle bundle = new Bundle();
        bundle.putParcelable("parent", parcelableOnSaveInstanceState);
        bundle.putFloat("angle", this.f3690x);
        bundle.putInt("color", this.f3684r);
        bundle.putBoolean("showColor", this.f3685s);
        return bundle;
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        getParent().requestDisallowInterceptTouchEvent(true);
        float x = motionEvent.getX() - this.f3687u;
        float y = motionEvent.getY() - this.f3687u;
        switch (motionEvent.getAction()) {
            case 0:
                float[] fArrM4429b = m4429b(this.f3690x);
                if (x >= fArrM4429b[0] - this.f3679m && x <= fArrM4429b[0] + this.f3679m && y >= fArrM4429b[1] - this.f3679m && y <= fArrM4429b[1] + this.f3679m) {
                    this.f3688v = x - fArrM4429b[0];
                    this.f3689w = y - fArrM4429b[1];
                    this.f3682p = true;
                    invalidate();
                } else if (x >= (-this.f3674h) && x <= this.f3674h && y >= (-this.f3674h) && y <= this.f3674h && this.f3685s) {
                    this.f3657A.setAlpha(80);
                    setColor(getOldCenterColor());
                    invalidate();
                } else {
                    if (Math.sqrt((x * x) + (y * y)) > this.f3672f + this.f3679m || Math.sqrt((x * x) + (y * y)) < this.f3672f - this.f3679m || !this.f3662F) {
                        getParent().requestDisallowInterceptTouchEvent(false);
                        return false;
                    }
                    this.f3682p = true;
                    invalidate();
                }
                break;
            case 1:
                this.f3682p = false;
                this.f3657A.setAlpha(0);
                if (this.f3665I != null && this.f3686t != this.f3667K) {
                    this.f3665I.m4439a(this.f3686t);
                    this.f3667K = this.f3686t;
                }
                invalidate();
                break;
            case 2:
                if (!this.f3682p) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                    return false;
                }
                this.f3690x = (float) Math.atan2(y - this.f3689w, x - this.f3688v);
                this.f3670d.setColor(m4426a(this.f3690x));
                int iM4426a = m4426a(this.f3690x);
                this.f3686t = iM4426a;
                setNewCenterColor(iM4426a);
                if (this.f3660D != null) {
                    this.f3660D.setColor(this.f3683q);
                }
                if (this.f3663G != null) {
                    this.f3663G.setColor(this.f3683q);
                }
                if (this.f3661E != null) {
                    this.f3661E.setColor(this.f3683q);
                }
                if (this.f3659C != null) {
                    this.f3659C.setColor(this.f3683q);
                }
                invalidate();
                break;
            case 3:
                if (this.f3665I != null && this.f3686t != this.f3667K) {
                    this.f3665I.m4439a(this.f3686t);
                    this.f3667K = this.f3686t;
                }
                break;
        }
        return true;
    }

    public void setColor(int i) {
        this.f3690x = m4430c(i);
        this.f3670d.setColor(m4426a(this.f3690x));
        if (this.f3660D != null) {
            this.f3660D.setColor(this.f3683q);
            this.f3660D.setOpacity(Color.alpha(i));
        }
        if (this.f3659C != null) {
            Color.colorToHSV(i, this.f3658B);
            this.f3659C.setColor(this.f3683q);
            if (this.f3658B[1] < this.f3658B[2]) {
                this.f3659C.setSaturation(this.f3658B[1]);
            } else if (this.f3658B[1] > this.f3658B[2]) {
                this.f3659C.setValue(this.f3658B[2]);
            }
        }
        if (this.f3661E != null) {
            Color.colorToHSV(i, this.f3658B);
            this.f3661E.setColor(this.f3683q);
            this.f3661E.setSaturation(this.f3658B[1]);
        }
        if (this.f3663G != null && this.f3661E == null) {
            Color.colorToHSV(i, this.f3658B);
            this.f3663G.setColor(this.f3683q);
            this.f3663G.setValue(this.f3658B[2]);
        } else if (this.f3663G != null) {
            Color.colorToHSV(i, this.f3658B);
            this.f3663G.setValue(this.f3658B[2]);
        }
        setNewCenterColor(i);
    }

    public void setNewCenterColor(int i) {
        this.f3686t = i;
        this.f3692z.setColor(i);
        if (this.f3684r == 0) {
            this.f3684r = i;
            this.f3691y.setColor(i);
        }
        if (this.f3664H != null && i != this.f3666J) {
            this.f3664H.m4438a(i);
            this.f3666J = i;
        }
        invalidate();
    }

    public void setOldCenterColor(int i) {
        this.f3684r = i;
        this.f3691y.setColor(i);
        invalidate();
    }

    public void setOnColorChangedListener(InterfaceC0763a interfaceC0763a) {
        this.f3664H = interfaceC0763a;
    }

    public void setOnColorSelectedListener(InterfaceC0764b interfaceC0764b) {
        this.f3665I = interfaceC0764b;
    }

    public void setShowOldCenterColor(boolean z) {
        this.f3685s = z;
        invalidate();
    }

    public void setTouchAnywhereOnColorWheelEnabled(boolean z) {
        this.f3662F = z;
    }
}
