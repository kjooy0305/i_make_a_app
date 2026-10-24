package com.tweber.stickfighter.p037g;

import android.graphics.PointF;
import com.tweber.stickfighter.activities.C0808g;
import com.tweber.stickfighter.p035e.ab;
import com.tweber.stickfighter.p038h.AbstractC0903a;
import com.tweber.stickfighter.p038h.AbstractC0908f;
import com.tweber.stickfighter.p038h.C0905c;
import com.tweber.stickfighter.p038h.InterfaceC0923u;
import com.tweber.stickfighter.p040j.C0932a;
import java.util.ArrayList;

/* JADX INFO: renamed from: com.tweber.stickfighter.g.g */
/* JADX INFO: loaded from: classes.dex */
class C0883g extends AbstractC0903a {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ C0881e f4111a;

    C0883g(C0881e c0881e) {
        this.f4111a = c0881e;
    }

    /* JADX INFO: renamed from: a */
    private void m4791a(InterfaceC0923u interfaceC0923u) {
        ArrayList arrayListM4850a = ((C0905c) interfaceC0923u).m4850a(this.f4111a.f4098k);
        ab abVar = new ab();
        abVar.m4596a(arrayListM4850a);
        abVar.m4595a(((AbstractC0908f) arrayListM4850a.get(0)).m4880b());
        abVar.show(this.f4111a.getChildFragmentManager(), (String) null);
    }

    @Override // com.tweber.stickfighter.p038h.AbstractC0903a
    /* JADX INFO: renamed from: a */
    protected void mo4792a(float f, float f2) {
        this.f4111a.f4100m.f4250a = new PointF(f, f2);
        this.f4111a.f4100m.f4251b = new PointF(f, f2);
        this.f4111a.f4100m.f4252c = new PointF(f, f2);
        C0808g c0808gM4764a = this.f4111a.m4764a(f, f2);
        if (c0808gM4764a == null || c0808gM4764a.f3828d >= this.f4111a.f4088a) {
            this.f4111a.f4100m.f4253d = null;
        } else {
            this.f4111a.f4100m.f4253d = c0808gM4764a.f3825a;
        }
    }

    @Override // com.tweber.stickfighter.p038h.AbstractC0903a
    /* JADX INFO: renamed from: b */
    protected void mo4793b(float f, float f2) {
        this.f4111a.f4100m.f4251b = this.f4111a.f4100m.f4252c;
        this.f4111a.f4100m.f4252c = new PointF(f, f2);
        if (this.f4111a.f4100m.f4253d != null) {
            if (((C0905c) this.f4111a.f4100m.f4253d).m4859b() == 4 || ((C0905c) this.f4111a.f4100m.f4253d).m4859b() == 8) {
                this.f4111a.f4100m.f4253d.mo4864c(this.f4111a.f4098k);
                this.f4111a.f4100m.f4253d.mo4854a(this.f4111a.f4100m.f4251b, this.f4111a.f4100m.f4252c, this.f4111a.f4095h, this.f4111a.f4098k);
                this.f4111a.m4772b();
            }
        }
    }

    @Override // com.tweber.stickfighter.p038h.AbstractC0903a
    /* JADX INFO: renamed from: c */
    protected void mo4794c(float f, float f2) {
        if (this.f4111a.f4100m.f4253d == null) {
            return;
        }
        if (((C0905c) this.f4111a.f4100m.f4253d).m4859b() == 2) {
            if (Math.abs(f - this.f4111a.f4100m.f4250a.x) + Math.abs(f2 - this.f4111a.f4100m.f4250a.y) > C0932a.m4948a(10) || ((C0905c) this.f4111a.f4100m.f4253d).m4859b() != 2) {
                return;
            } else {
                m4791a(this.f4111a.f4100m.f4253d);
            }
        } else if (((C0905c) this.f4111a.f4100m.f4253d).m4859b() == 4 || ((C0905c) this.f4111a.f4100m.f4253d).m4859b() == 8) {
            this.f4111a.f4100m.f4253d.mo4854a(this.f4111a.f4100m.f4251b, this.f4111a.f4100m.f4252c, this.f4111a.f4095h, this.f4111a.f4098k);
            this.f4111a.m4772b();
        }
        if (this.f4111a.f4100m.f4253d != null) {
            this.f4111a.f4100m.f4253d.mo4864c(this.f4111a.f4098k);
        }
        this.f4111a.f4100m.f4253d = null;
        this.f4111a.m4772b();
    }
}
