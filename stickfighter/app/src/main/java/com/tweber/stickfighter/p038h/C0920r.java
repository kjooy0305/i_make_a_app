package com.tweber.stickfighter.p038h;

import android.graphics.Canvas;
import android.graphics.Paint;

/* JADX INFO: renamed from: com.tweber.stickfighter.h.r */
/* JADX INFO: loaded from: classes.dex */
public class C0920r {

    /* JADX INFO: renamed from: a */
    public final C0919q[] f4239a;

    /* JADX INFO: renamed from: b */
    public final int f4240b;

    /* JADX INFO: renamed from: c */
    public final int f4241c;

    public C0920r(C0919q[] c0919qArr, int i, int i2) {
        this.f4239a = c0919qArr;
        this.f4241c = i;
        this.f4240b = i2;
    }

    /* JADX INFO: renamed from: a */
    public void m4931a(C0912j c0912j, Canvas canvas, Paint paint, boolean z) {
        if (this.f4241c == 1) {
            C0919q c0919q = this.f4239a[0];
            C0919q c0919q2 = this.f4239a[1];
            canvas.drawLine(c0912j.m4887a(c0919q.f4237a), c0912j.m4889b(c0919q.f4238b), c0912j.m4887a(c0919q2.f4237a), c0912j.m4889b(c0919q2.f4238b), paint);
            return;
        }
        if (this.f4241c == 2) {
            C0919q c0919q3 = this.f4239a[0];
            C0919q c0919q4 = this.f4239a[1];
            float fM4887a = c0912j.m4887a(c0919q3.f4237a);
            float fM4889b = c0912j.m4889b(c0919q3.f4238b);
            float fM4887a2 = c0912j.m4887a(c0919q4.f4237a);
            float fM4889b2 = c0912j.m4889b(c0919q4.f4238b);
            float fAbs = Math.abs(fM4887a - fM4887a2);
            canvas.drawCircle((fM4887a + fM4887a2) / 2.0f, (fM4889b + fM4889b2) / 2.0f, (float) (Math.sqrt(Math.pow(Math.abs(fM4889b - fM4889b2), 2.0d) + Math.pow(fAbs, 2.0d)) / 2.0d), paint);
            return;
        }
        if (this.f4241c == 3) {
            C0919q c0919q5 = this.f4239a[0];
            C0919q c0919q6 = this.f4239a[1];
            C0919q c0919q7 = this.f4239a[2];
            float fM4887a3 = c0912j.m4887a(c0919q5.f4237a);
            float fM4889b3 = c0912j.m4889b(c0919q5.f4238b);
            float fM4887a4 = c0912j.m4887a(c0919q6.f4237a);
            float fM4889b4 = c0912j.m4889b(c0919q6.f4238b);
            float fM4887a5 = c0912j.m4887a(c0919q7.f4237a);
            float fM4889b5 = c0912j.m4889b(c0919q7.f4238b);
            C0911i.f4185e.rewind();
            C0911i.f4185e.moveTo(fM4887a3, fM4889b3);
            C0911i.f4185e.quadTo(fM4887a4, fM4889b4, fM4887a5, fM4889b5);
            canvas.drawPath(C0911i.f4185e, paint);
        }
    }
}
