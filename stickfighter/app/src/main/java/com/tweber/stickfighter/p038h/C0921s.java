package com.tweber.stickfighter.p038h;

import android.graphics.Canvas;
import com.tweber.stickfighter.StickFighterApplication;
import com.tweber.stickfighter.activities.R;

/* JADX INFO: renamed from: com.tweber.stickfighter.h.s */
/* JADX INFO: loaded from: classes.dex */
public class C0921s implements InterfaceC0913k {

    /* JADX INFO: renamed from: a */
    static int f4242a = StickFighterApplication.m4490a().getResources().getColor(R.color.canvas);

    /* JADX INFO: renamed from: b */
    final C0920r[] f4243b;

    public C0921s(C0920r[] c0920rArr) {
        this.f4243b = c0920rArr;
    }

    @Override // com.tweber.stickfighter.p038h.InterfaceC0913k
    /* JADX INFO: renamed from: a */
    public void mo4892a(C0912j c0912j, Canvas canvas, boolean z, C0918p c0918p, int i) {
        canvas.drawColor(f4242a);
        c0912j.f4186a.f4287d.setColor(i);
        canvas.drawRect(c0912j.m4888a(), c0912j.f4186a.f4287d);
        for (C0920r c0920r : this.f4243b) {
            c0912j.f4186a.f4290g.setColor(c0920r.f4240b);
            c0920r.m4931a(c0912j, canvas, c0912j.f4186a.f4290g, z);
        }
    }
}
