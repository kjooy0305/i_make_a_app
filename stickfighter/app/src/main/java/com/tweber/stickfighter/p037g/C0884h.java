package com.tweber.stickfighter.p037g;

import android.graphics.PointF;
import android.widget.ImageView;
import com.tweber.stickfighter.activities.C0808g;
import com.tweber.stickfighter.activities.R;
import com.tweber.stickfighter.p034d.C0836a;
import com.tweber.stickfighter.p034d.InterfaceC0837b;
import com.tweber.stickfighter.p038h.AbstractC0903a;
import com.tweber.stickfighter.p038h.AbstractC0908f;
import com.tweber.stickfighter.p038h.C0904b;
import com.tweber.stickfighter.p038h.C0905c;
import java.util.ArrayList;
import java.util.Iterator;

/* JADX INFO: renamed from: com.tweber.stickfighter.g.h */
/* JADX INFO: loaded from: classes.dex */
class C0884h extends AbstractC0903a {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ C0881e f4112a;

    C0884h(C0881e c0881e) {
        this.f4112a = c0881e;
    }

    /* JADX INFO: renamed from: a */
    private C0808g m4795a(C0905c c0905c, float f, float f2) {
        if (c0905c.m4868g().f4182c == 3 && c0905c.f4166c == 1) {
            return null;
        }
        C0808g c0808g = null;
        for (AbstractC0908f abstractC0908f : (Iterable<AbstractC0908f>) this.f4112a.f4093f.m4922a()) {
            if (c0905c.m4868g().f4181b != abstractC0908f.f4181b) {
                for (C0905c c0905c2 : (Iterable<C0905c>) abstractC0908f.m4872a()) {
                    if (c0905c2.m4858a() && !c0905c2.equals(c0905c) && !c0905c2.m4868g().equals(c0905c.m4868g()) && !m4798b(c0905c2, c0905c) && (c0905c2.m4868g().f4182c != 3 || c0905c2.f4166c != 1)) {
                        float fM4887a = this.f4112a.f4095h.m4887a(c0905c2.m4863c());
                        float fM4889b = this.f4112a.f4095h.m4889b(c0905c2.m4865d());
                        float fSqrt = (float) Math.sqrt(Math.pow(fM4889b - f2, 2.0d) + Math.pow(fM4887a - f, 2.0d));
                        if (c0808g == null || fSqrt < c0808g.f3828d) {
                            c0808g = new C0808g(c0905c2, fM4887a, fM4889b, fSqrt);
                        }
                        c0808g = c0808g;
                    }
                }
            }
        }
        return c0808g;
    }

    /* JADX INFO: renamed from: a */
    private void m4796a(C0905c c0905c, C0905c c0905c2) {
        C0905c c0905c3;
        ArrayList arrayList = new ArrayList();
        c0905c.m4857a(arrayList, this.f4112a.f4098k);
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            ((C0905c) it.next()).m4860b(c0905c2.m4863c(), c0905c2.m4865d());
        }
        InterfaceC0837b interfaceC0837bM4547a = C0836a.m4547a();
        interfaceC0837bM4547a.mo4557a(c0905c, c0905c2);
        c0905c.f4167d = null;
        c0905c2.f4167d = null;
        Iterator it2 = c0905c2.m4850a(this.f4112a.f4098k).iterator();
        while (it2.hasNext()) {
            Iterator it3 = ((AbstractC0908f) it2.next()).m4872a().iterator();
            while (it3.hasNext()) {
                ((C0905c) it3.next()).f4167d = null;
            }
        }
        if (c0905c.m4859b() == 2) {
            c0905c.f4165b -= 2;
            c0905c.f4165b += 8;
            interfaceC0837bM4547a.mo4570c(c0905c.f4164a, c0905c.f4165b);
        } else {
            Iterator it4 = c0905c.m4850a(this.f4112a.f4098k).iterator();
            C0905c c0905c4 = null;
            C0905c c0905c5 = null;
            while (it4.hasNext()) {
                for (C0905c c0905c6 : (Iterable<C0905c>) ((AbstractC0908f) it4.next()).m4872a()) {
                    c0905c6.f4167d = null;
                    if (c0905c5 == null && c0905c6.m4859b() == 2 && c0905c6 != c0905c) {
                        C0905c c0905c7 = c0905c4;
                        c0905c3 = c0905c6;
                        c0905c6 = c0905c7;
                    } else if (c0905c4 == null && c0905c6.m4859b() == 2 && c0905c6 != c0905c) {
                        c0905c3 = c0905c5;
                    } else {
                        c0905c6 = c0905c4;
                        c0905c3 = c0905c5;
                    }
                    c0905c5 = c0905c3;
                    c0905c4 = c0905c6;
                }
            }
            if (!(c0905c4 == null)) {
                if (c0905c5.m4863c() == c0905c4.m4863c() && c0905c5.m4865d() == c0905c4.m4865d()) {
                    c0905c5.f4165b -= 2;
                    c0905c5.f4165b += 8;
                    c0905c5.f4165b--;
                    interfaceC0837bM4547a.mo4570c(c0905c5.f4164a, c0905c5.f4165b);
                } else {
                    c0905c5.f4165b -= 2;
                    c0905c5.f4165b += 8;
                    interfaceC0837bM4547a.mo4570c(c0905c5.f4164a, c0905c5.f4165b);
                }
            }
        }
        c0905c.f4165b--;
        interfaceC0837bM4547a.mo4570c(c0905c.f4164a, c0905c.f4165b);
        this.f4112a.m4772b();
    }

    /* JADX INFO: renamed from: a */
    private boolean m4797a(C0905c c0905c) {
        ImageView imageView = (ImageView) this.f4112a.getView().findViewById(R.id.DeleteAreaView);
        return this.f4112a.f4095h.m4887a(c0905c.m4863c()) > ((float) imageView.getLeft()) && this.f4112a.f4095h.m4889b(c0905c.m4865d()) > ((float) imageView.getTop());
    }

    /* JADX INFO: renamed from: b */
    private boolean m4798b(C0905c c0905c, C0905c c0905c2) {
        ArrayList arrayListM4866e = c0905c.m4866e();
        for (int i = 0; i < arrayListM4866e.size(); i++) {
            if (((C0904b) arrayListM4866e.get(i)).f4162a == c0905c2.f4164a) {
                return true;
            }
        }
        ArrayList arrayListM4866e2 = c0905c2.m4866e();
        for (int i2 = 0; i2 < arrayListM4866e2.size(); i2++) {
            if (((C0904b) arrayListM4866e2.get(i2)).f4162a == c0905c.f4164a) {
                return true;
            }
        }
        return false;
    }

    @Override // com.tweber.stickfighter.p038h.AbstractC0903a
    /* JADX INFO: renamed from: a */
    public void mo4792a(float f, float f2) {
        this.f4112a.f4100m.f4250a = new PointF(f, f2);
        this.f4112a.f4100m.f4251b = new PointF(f, f2);
        this.f4112a.f4100m.f4252c = new PointF(f, f2);
        C0808g c0808gM4764a = this.f4112a.m4764a(f, f2);
        if (c0808gM4764a == null || c0808gM4764a.f3828d >= this.f4112a.f4088a) {
            this.f4112a.f4100m.f4253d = null;
        } else {
            this.f4112a.f4100m.f4253d = c0808gM4764a.f3825a;
        }
    }

    @Override // com.tweber.stickfighter.p038h.AbstractC0903a
    /* JADX INFO: renamed from: b */
    public void mo4793b(float f, float f2) {
        this.f4112a.f4100m.f4251b = this.f4112a.f4100m.f4252c;
        this.f4112a.f4100m.f4252c = new PointF(f, f2);
        if (this.f4112a.f4100m.f4253d != null) {
            C0905c c0905c = (C0905c) this.f4112a.f4100m.f4253d;
            if (this.f4112a.f4100m.f4253d.mo4869h() && m4797a(c0905c)) {
                this.f4112a.f4100m.f4253d.mo4862b(this.f4112a.f4098k);
                this.f4112a.f4100m.f4253d.mo4854a(this.f4112a.f4100m.f4251b, this.f4112a.f4100m.f4252c, this.f4112a.f4095h, this.f4112a.f4098k);
                this.f4112a.m4772b();
                return;
            }
            float fM4887a = this.f4112a.f4095h.m4887a(c0905c.m4863c());
            float fM4889b = this.f4112a.f4095h.m4889b(c0905c.m4865d());
            this.f4112a.f4100m.f4253d.mo4864c(this.f4112a.f4098k);
            C0808g c0808gM4795a = m4795a(c0905c, fM4887a, fM4889b);
            if (c0808gM4795a == null || c0808gM4795a.f3828d >= this.f4112a.f4089b) {
                this.f4112a.f4100m.f4254e = null;
            } else {
                this.f4112a.f4100m.f4254e = c0808gM4795a.f3825a;
            }
            this.f4112a.f4100m.f4253d.mo4854a(this.f4112a.f4100m.f4251b, this.f4112a.f4100m.f4252c, this.f4112a.f4095h, this.f4112a.f4098k);
            this.f4112a.m4772b();
        }
    }

    @Override // com.tweber.stickfighter.p038h.AbstractC0903a
    /* JADX INFO: renamed from: c */
    public void mo4794c(float f, float f2) {
        if (this.f4112a.f4100m.f4253d != null) {
            this.f4112a.f4100m.f4253d.mo4864c(this.f4112a.f4098k);
            C0905c c0905c = (C0905c) this.f4112a.f4100m.f4253d;
            if (c0905c.m4859b() == 2 && m4797a(c0905c)) {
                ArrayList<AbstractC0908f> arrayListM4867f = c0905c.m4867f();
                InterfaceC0837b interfaceC0837bM4547a = C0836a.m4547a();
                try {
                    interfaceC0837bM4547a.mo4569c();
                    for (AbstractC0908f abstractC0908f : arrayListM4867f) {
                        interfaceC0837bM4547a.mo4560a(this.f4112a.f4093f, abstractC0908f);
                        this.f4112a.f4093f.m4922a().remove(abstractC0908f);
                    }
                    interfaceC0837bM4547a.mo4573d();
                    interfaceC0837bM4547a.mo4574e();
                } catch (Throwable th) {
                    interfaceC0837bM4547a.mo4574e();
                    throw th;
                }
            } else if (this.f4112a.f4100m.f4254e != null) {
                m4796a(c0905c, (C0905c) this.f4112a.f4100m.f4254e);
            }
        }
        this.f4112a.f4100m.f4253d = null;
        this.f4112a.f4100m.f4254e = null;
        this.f4112a.m4772b();
    }
}
