package com.tweber.stickfighter.p037g;

import android.graphics.PointF;
import com.tweber.stickfighter.activities.C0808g;
import com.tweber.stickfighter.p038h.AbstractC0903a;
import com.tweber.stickfighter.p038h.C0905c;
import com.tweber.stickfighter.p038h.InterfaceC0923u;

/* JADX INFO: renamed from: com.tweber.stickfighter.g.t */
/* JADX INFO: loaded from: classes.dex */
class C0896t extends AbstractC0903a {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ C0887k f4153a;

    C0896t(C0887k c0887k) {
        this.f4153a = c0887k;
    }

    /* JADX INFO: renamed from: a */
    private void m4829a(InterfaceC0923u interfaceC0923u) {
        interfaceC0923u.mo4861b(this.f4153a.f4129l.f4251b, this.f4153a.f4129l.f4252c, this.f4153a.f4124g, this.f4153a.f4127j);
        this.f4153a.m4815d();
    }

    @Override // com.tweber.stickfighter.p038h.AbstractC0903a
    /* JADX INFO: renamed from: a */
    protected void mo4792a(float f, float f2) {
        this.f4153a.f4129l.f4250a = new PointF(f, f2);
        this.f4153a.f4129l.f4251b = new PointF(f, f2);
        this.f4153a.f4129l.f4252c = new PointF(f, f2);
        C0808g c0808gM4801a = this.f4153a.m4801a(f, f2);
        if (c0808gM4801a == null || c0808gM4801a.f3828d >= this.f4153a.f4119b) {
            this.f4153a.f4129l.f4253d = null;
        } else {
            this.f4153a.f4129l.f4253d = c0808gM4801a.f3825a;
        }
    }

    @Override // com.tweber.stickfighter.p038h.AbstractC0903a
    /* JADX INFO: renamed from: b */
    protected void mo4793b(float f, float f2) {
    }

    @Override // com.tweber.stickfighter.p038h.AbstractC0903a
    /* JADX INFO: renamed from: c */
    protected void mo4794c(float f, float f2) {
        if (this.f4153a.f4129l.f4253d != null && Math.abs(f - this.f4153a.f4129l.f4250a.x) + Math.abs(f2 - this.f4153a.f4129l.f4250a.y) <= 20.0f && ((C0905c) this.f4153a.f4129l.f4253d).m4859b() == 2) {
            m4829a(this.f4153a.f4129l.f4253d);
            if (this.f4153a.f4129l.f4253d != null) {
                this.f4153a.f4129l.f4253d.mo4864c(this.f4153a.f4127j);
            }
            this.f4153a.f4129l.f4253d = null;
            this.f4153a.m4815d();
        }
    }
}
