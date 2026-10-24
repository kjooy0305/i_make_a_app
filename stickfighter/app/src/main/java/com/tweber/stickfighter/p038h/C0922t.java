package com.tweber.stickfighter.p038h;

import android.graphics.Canvas;
import android.graphics.Paint;
import java.util.ArrayList;

/* JADX INFO: renamed from: com.tweber.stickfighter.h.t */
/* JADX INFO: loaded from: classes.dex */
public class C0922t extends AbstractC0908f {
    public C0922t(long j, long j2, ArrayList arrayList, int i) {
        super(j, j2, arrayList, (byte) 1, i);
    }

    @Override // com.tweber.stickfighter.p038h.AbstractC0908f
    /* JADX INFO: renamed from: a */
    protected void mo4877a(C0912j c0912j, Canvas canvas) {
        C0905c c0905c = (C0905c) m4872a().get(0);
        C0905c c0905c2 = (C0905c) m4872a().get(1);
        canvas.drawLine(c0912j.m4887a(c0905c.m4863c()), c0912j.m4889b(c0905c.m4865d()), c0912j.m4887a(c0905c2.m4863c()), c0912j.m4889b(c0905c2.m4865d()), c0912j.f4186a.f4289f);
    }

    @Override // com.tweber.stickfighter.p038h.AbstractC0908f
    /* JADX INFO: renamed from: a */
    protected void mo4878a(C0912j c0912j, Canvas canvas, Paint paint) {
        C0905c c0905c = (C0905c) m4872a().get(0);
        C0905c c0905c2 = (C0905c) m4872a().get(1);
        canvas.drawLine(c0912j.m4887a(c0905c.m4863c()), c0912j.m4889b(c0905c.m4865d()), c0912j.m4887a(c0905c2.m4863c()), c0912j.m4889b(c0905c2.m4865d()), paint);
    }
}
