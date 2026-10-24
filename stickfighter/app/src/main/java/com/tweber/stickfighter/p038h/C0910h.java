package com.tweber.stickfighter.p038h;

import android.graphics.Canvas;
import android.graphics.Paint;
import java.util.ArrayList;

/* JADX INFO: renamed from: com.tweber.stickfighter.h.h */
/* JADX INFO: loaded from: classes.dex */
public class C0910h extends AbstractC0908f {
    public C0910h(long j, long j2, ArrayList arrayList, int i) {
        super(j, j2, arrayList, (byte) 2, i);
    }

    @Override // com.tweber.stickfighter.p038h.AbstractC0908f
    /* JADX INFO: renamed from: a */
    protected void mo4877a(C0912j c0912j, Canvas canvas) {
        C0905c c0905c = (C0905c) m4872a().get(0);
        C0905c c0905c2 = (C0905c) m4872a().get(1);
        float fM4887a = c0912j.m4887a(c0905c.m4863c());
        float fM4889b = c0912j.m4889b(c0905c.m4865d());
        float fM4887a2 = c0912j.m4887a(c0905c2.m4863c());
        float fM4889b2 = c0912j.m4889b(c0905c2.m4865d());
        float fAbs = Math.abs(fM4887a - fM4887a2);
        canvas.drawCircle((fM4887a + fM4887a2) / 2.0f, (fM4889b + fM4889b2) / 2.0f, (float) (Math.sqrt(Math.pow(Math.abs(fM4889b - fM4889b2), 2.0d) + Math.pow(fAbs, 2.0d)) / 2.0d), c0912j.f4186a.f4289f);
    }

    @Override // com.tweber.stickfighter.p038h.AbstractC0908f
    /* JADX INFO: renamed from: a */
    protected void mo4878a(C0912j c0912j, Canvas canvas, Paint paint) {
        C0905c c0905c = (C0905c) m4872a().get(0);
        C0905c c0905c2 = (C0905c) m4872a().get(1);
        float fM4887a = c0912j.m4887a(c0905c.m4863c());
        float fM4889b = c0912j.m4889b(c0905c.m4865d());
        float fM4887a2 = c0912j.m4887a(c0905c2.m4863c());
        float fM4889b2 = c0912j.m4889b(c0905c2.m4865d());
        float fAbs = Math.abs(fM4887a - fM4887a2);
        canvas.drawCircle((fM4887a + fM4887a2) / 2.0f, (fM4889b + fM4889b2) / 2.0f, (float) (Math.sqrt(Math.pow(Math.abs(fM4889b - fM4889b2), 2.0d) + Math.pow(fAbs, 2.0d)) / 2.0d), paint);
    }
}
