package com.tweber.stickfighter.p038h;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import com.tweber.stickfighter.activities.C0802a;
import java.util.ArrayList;

/* JADX INFO: renamed from: com.tweber.stickfighter.h.i */
/* JADX INFO: loaded from: classes.dex */
public class C0911i extends AbstractC0908f {

    /* JADX INFO: renamed from: e */
    public static final Path f4185e = new Path();

    public C0911i(long j, long j2, ArrayList arrayList, int i) {
        super(j, j2, arrayList, (byte) 3, i);
    }

    @Override // com.tweber.stickfighter.p038h.AbstractC0908f
    /* JADX INFO: renamed from: a */
    protected void mo4877a(C0912j c0912j, Canvas canvas) {
        C0905c c0905c = (C0905c) m4872a().get(0);
        C0905c c0905c2 = (C0905c) m4872a().get(1);
        C0905c c0905c3 = (C0905c) m4872a().get(2);
        float fM4887a = c0912j.m4887a(c0905c.m4863c());
        float fM4889b = c0912j.m4889b(c0905c.m4865d());
        float fM4887a2 = c0912j.m4887a(c0905c2.m4863c());
        float fM4889b2 = c0912j.m4889b(c0905c2.m4865d());
        float fM4887a3 = c0912j.m4887a(c0905c3.m4863c());
        float fM4889b3 = c0912j.m4889b(c0905c3.m4865d());
        f4185e.rewind();
        f4185e.moveTo(fM4887a, fM4889b);
        f4185e.quadTo(fM4887a2, fM4889b2, fM4887a3, fM4889b3);
        canvas.drawPath(f4185e, c0912j.f4186a.f4289f);
    }

    @Override // com.tweber.stickfighter.p038h.AbstractC0908f
    /* JADX INFO: renamed from: a */
    protected void mo4878a(C0912j c0912j, Canvas canvas, Paint paint) {
        C0905c c0905c = (C0905c) m4872a().get(0);
        C0905c c0905c2 = (C0905c) m4872a().get(1);
        C0905c c0905c3 = (C0905c) m4872a().get(2);
        float fM4887a = c0912j.m4887a(c0905c.m4863c());
        float fM4889b = c0912j.m4889b(c0905c.m4865d());
        float fM4887a2 = c0912j.m4887a(c0905c2.m4863c());
        float fM4889b2 = c0912j.m4889b(c0905c2.m4865d());
        float fM4887a3 = c0912j.m4887a(c0905c3.m4863c());
        float fM4889b3 = c0912j.m4889b(c0905c3.m4865d());
        f4185e.rewind();
        f4185e.moveTo(fM4887a, fM4889b);
        f4185e.quadTo(fM4887a2, fM4889b2, fM4887a3, fM4889b3);
        canvas.drawPath(f4185e, paint);
    }

    @Override // com.tweber.stickfighter.p038h.AbstractC0908f
    /* JADX INFO: renamed from: b */
    public void mo4881b(C0912j c0912j, Canvas canvas) {
        if (C0802a.f3819a == 0) {
            c0912j.f4186a.f4297n.setColor(Color.argb(170, 204, 0, 0));
        } else if (C0802a.f3819a == 1) {
            c0912j.f4186a.f4297n.setColor(Color.argb(170, 51, 229, 181));
        } else if (C0802a.f3819a == 2) {
            super.mo4881b(c0912j, canvas);
            return;
        }
        C0905c c0905c = (C0905c) m4872a().get(0);
        C0905c c0905c2 = (C0905c) m4872a().get(1);
        C0905c c0905c3 = (C0905c) m4872a().get(2);
        float fM4887a = c0912j.m4887a(c0905c.m4863c());
        float fM4889b = c0912j.m4889b(c0905c.m4865d());
        float fM4887a2 = c0912j.m4887a(c0905c2.m4863c());
        float fM4889b2 = c0912j.m4889b(c0905c2.m4865d());
        float fM4887a3 = c0912j.m4887a(c0905c3.m4863c());
        float fM4889b3 = c0912j.m4889b(c0905c3.m4865d());
        f4185e.rewind();
        f4185e.moveTo(fM4887a, fM4889b);
        f4185e.lineTo(fM4887a2, fM4889b2);
        canvas.drawPath(f4185e, c0912j.f4186a.f4297n);
        f4185e.rewind();
        f4185e.moveTo(fM4887a3, fM4889b3);
        f4185e.lineTo(fM4887a2, fM4889b2);
        canvas.drawPath(f4185e, c0912j.f4186a.f4297n);
        super.mo4881b(c0912j, canvas);
    }
}
