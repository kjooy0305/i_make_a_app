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

/* JADX INFO: renamed from: com.tweber.stickfighter.g.q */
/* JADX INFO: loaded from: classes.dex */
class C0893q extends AbstractC0903a {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ C0887k f4149a;

    C0893q(C0887k c0887k) {
        this.f4149a = c0887k;
    }

    /* JADX INFO: renamed from: a */
    private void m4827a(InterfaceC0923u interfaceC0923u) {
        ArrayList arrayListM4850a = ((C0905c) interfaceC0923u).m4850a(this.f4149a.f4127j);
        ab abVar = new ab();
        abVar.m4596a(arrayListM4850a);
        abVar.m4595a(((AbstractC0908f) arrayListM4850a.get(0)).m4880b());
        abVar.show(this.f4149a.getChildFragmentManager(), (String) null);
    }

    @Override // com.tweber.stickfighter.p038h.AbstractC0903a
    /* JADX INFO: renamed from: a */
    protected void mo4792a(float f, float f2) {
        this.f4149a.f4129l.f4250a = new PointF(f, f2);
        this.f4149a.f4129l.f4251b = new PointF(f, f2);
        this.f4149a.f4129l.f4252c = new PointF(f, f2);
        C0808g c0808gM4801a = this.f4149a.m4801a(f, f2);
        if (c0808gM4801a == null || c0808gM4801a.f3828d >= this.f4149a.f4119b) {
            this.f4149a.f4129l.f4253d = null;
        } else {
            this.f4149a.f4129l.f4253d = c0808gM4801a.f3825a;
        }
    }

    @Override // com.tweber.stickfighter.p038h.AbstractC0903a
    /* JADX INFO: renamed from: b */
    protected void mo4793b(float f, float f2) {
        this.f4149a.f4129l.f4251b = this.f4149a.f4129l.f4252c;
        this.f4149a.f4129l.f4252c = new PointF(f, f2);
        if (this.f4149a.f4129l.f4253d != null) {
            if (((C0905c) this.f4149a.f4129l.f4253d).m4859b() == 4 || ((C0905c) this.f4149a.f4129l.f4253d).m4859b() == 8) {
                this.f4149a.f4129l.f4253d.mo4864c(this.f4149a.f4127j);
                this.f4149a.f4129l.f4253d.mo4854a(this.f4149a.f4129l.f4251b, this.f4149a.f4129l.f4252c, this.f4149a.f4124g, this.f4149a.f4127j);
                this.f4149a.m4815d();
            }
        }
    }

    @Override // com.tweber.stickfighter.p038h.AbstractC0903a
    /* JADX INFO: renamed from: c */
    protected void mo4794c(float f, float f2) {
        if (this.f4149a.f4129l.f4253d == null) {
            return;
        }
        if (((C0905c) this.f4149a.f4129l.f4253d).m4859b() == 2) {
            if (Math.abs(f - this.f4149a.f4129l.f4250a.x) + Math.abs(f2 - this.f4149a.f4129l.f4250a.y) > C0932a.m4948a(10) || ((C0905c) this.f4149a.f4129l.f4253d).m4859b() != 2) {
                return;
            } else {
                m4827a(this.f4149a.f4129l.f4253d);
            }
        } else if (((C0905c) this.f4149a.f4129l.f4253d).m4859b() == 4 || ((C0905c) this.f4149a.f4129l.f4253d).m4859b() == 8) {
            this.f4149a.f4129l.f4253d.mo4854a(this.f4149a.f4129l.f4251b, this.f4149a.f4129l.f4252c, this.f4149a.f4124g, this.f4149a.f4127j);
            this.f4149a.m4815d();
        }
        if (this.f4149a.f4129l.f4253d != null) {
            this.f4149a.f4129l.f4253d.mo4864c(this.f4149a.f4127j);
        }
        this.f4149a.f4129l.f4253d = null;
        this.f4149a.m4815d();
    }
}
