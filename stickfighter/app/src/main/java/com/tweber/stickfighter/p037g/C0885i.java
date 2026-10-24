package com.tweber.stickfighter.p037g;

import android.graphics.PointF;
import com.tweber.stickfighter.activities.C0808g;
import com.tweber.stickfighter.p038h.AbstractC0903a;
import com.tweber.stickfighter.p038h.C0905c;
import com.tweber.stickfighter.p038h.InterfaceC0923u;

/* JADX INFO: renamed from: com.tweber.stickfighter.g.i */
/* JADX INFO: loaded from: classes.dex */
class C0885i extends AbstractC0903a {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ C0881e f4113a;

    C0885i(C0881e c0881e) {
        this.f4113a = c0881e;
    }

    /* JADX INFO: renamed from: a */
    private void m4799a(InterfaceC0923u interfaceC0923u) {
        interfaceC0923u.mo4861b(this.f4113a.f4100m.f4251b, this.f4113a.f4100m.f4252c, this.f4113a.f4095h, this.f4113a.f4098k);
        this.f4113a.m4772b();
    }

    @Override // com.tweber.stickfighter.p038h.AbstractC0903a
    /* JADX INFO: renamed from: a */
    protected void mo4792a(float f, float f2) {
        this.f4113a.f4100m.f4250a = new PointF(f, f2);
        this.f4113a.f4100m.f4251b = new PointF(f, f2);
        this.f4113a.f4100m.f4252c = new PointF(f, f2);
        C0808g c0808gM4764a = this.f4113a.m4764a(f, f2);
        if (c0808gM4764a == null || c0808gM4764a.f3828d >= this.f4113a.f4088a) {
            this.f4113a.f4100m.f4253d = null;
        } else {
            this.f4113a.f4100m.f4253d = c0808gM4764a.f3825a;
        }
    }

    @Override // com.tweber.stickfighter.p038h.AbstractC0903a
    /* JADX INFO: renamed from: b */
    protected void mo4793b(float f, float f2) {
    }

    @Override // com.tweber.stickfighter.p038h.AbstractC0903a
    /* JADX INFO: renamed from: c */
    protected void mo4794c(float f, float f2) {
        if (this.f4113a.f4100m.f4253d != null && Math.abs(f - this.f4113a.f4100m.f4250a.x) + Math.abs(f2 - this.f4113a.f4100m.f4250a.y) <= 20.0f && ((C0905c) this.f4113a.f4100m.f4253d).m4859b() == 2) {
            m4799a(this.f4113a.f4100m.f4253d);
            if (this.f4113a.f4100m.f4253d != null) {
                this.f4113a.f4100m.f4253d.mo4864c(this.f4113a.f4098k);
            }
            this.f4113a.f4100m.f4253d = null;
            this.f4113a.m4772b();
        }
    }
}
