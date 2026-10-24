package com.tweber.stickfighter.p037g;

import android.graphics.PointF;
import com.tweber.stickfighter.activities.C0808g;
import com.tweber.stickfighter.p038h.AbstractC0903a;
import com.tweber.stickfighter.p038h.AbstractC0908f;
import com.tweber.stickfighter.p038h.C0905c;
import java.util.ArrayList;
import java.util.Iterator;

/* JADX INFO: renamed from: com.tweber.stickfighter.g.r */
/* JADX INFO: loaded from: classes.dex */
class C0894r extends AbstractC0903a {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ C0887k f4150a;

    C0894r(C0887k c0887k) {
        this.f4150a = c0887k;
    }

    /* JADX INFO: renamed from: a */
    private boolean m4828a(C0905c c0905c) {
        return this.f4150a.f4124g.m4887a(c0905c.m4863c()) > ((float) this.f4150a.f4131n.getLeft()) && this.f4150a.f4124g.m4889b(c0905c.m4865d()) > ((float) this.f4150a.f4131n.getTop());
    }

    @Override // com.tweber.stickfighter.p038h.AbstractC0903a
    /* JADX INFO: renamed from: a */
    public void mo4792a(float f, float f2) {
        this.f4150a.f4129l.f4250a = new PointF(f, f2);
        this.f4150a.f4129l.f4251b = new PointF(f, f2);
        this.f4150a.f4129l.f4252c = new PointF(f, f2);
        C0808g c0808gM4801a = this.f4150a.m4801a(f, f2);
        if (c0808gM4801a == null || c0808gM4801a.f3828d >= this.f4150a.f4119b) {
            this.f4150a.f4129l.f4253d = null;
        } else {
            this.f4150a.f4129l.f4253d = c0808gM4801a.f3825a;
        }
    }

    @Override // com.tweber.stickfighter.p038h.AbstractC0903a
    /* JADX INFO: renamed from: b */
    public void mo4793b(float f, float f2) {
        this.f4150a.f4129l.f4251b = this.f4150a.f4129l.f4252c;
        this.f4150a.f4129l.f4252c = new PointF(f, f2);
        if (this.f4150a.f4129l.f4253d != null) {
            C0905c c0905c = (C0905c) this.f4150a.f4129l.f4253d;
            if (this.f4150a.f4129l.f4253d.mo4869h() && m4828a(c0905c)) {
                this.f4150a.f4129l.f4253d.mo4862b(this.f4150a.f4127j);
            } else {
                this.f4150a.f4129l.f4253d.mo4864c(this.f4150a.f4127j);
            }
            this.f4150a.f4129l.f4253d.mo4854a(this.f4150a.f4129l.f4251b, this.f4150a.f4129l.f4252c, this.f4150a.f4124g, this.f4150a.f4127j);
            this.f4150a.m4815d();
        }
    }

    @Override // com.tweber.stickfighter.p038h.AbstractC0903a
    /* JADX INFO: renamed from: c */
    public void mo4794c(float f, float f2) {
        if (this.f4150a.f4129l.f4253d != null) {
            this.f4150a.f4129l.f4253d.mo4864c(this.f4150a.f4127j);
            C0905c c0905c = (C0905c) this.f4150a.f4129l.f4253d;
            if (c0905c.m4859b() == 2 && m4828a(c0905c)) {
                ArrayList arrayListM4867f = c0905c.m4867f();
                Iterator it = arrayListM4867f.iterator();
                while (it.hasNext()) {
                    this.f4150a.f4122e.m4922a().remove((AbstractC0908f) it.next());
                }
                new AsyncTaskC0895s(this, arrayListM4867f).execute(new Object[0]);
            }
        }
        this.f4150a.f4129l.f4253d = null;
        this.f4150a.m4815d();
    }
}
