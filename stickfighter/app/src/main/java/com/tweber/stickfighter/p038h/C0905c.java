package com.tweber.stickfighter.p038h;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.SparseArray;
import com.tweber.stickfighter.StickFighterApplication;
import com.tweber.stickfighter.activities.C0802a;
import com.tweber.stickfighter.activities.R;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/* JADX INFO: renamed from: com.tweber.stickfighter.h.c */
/* JADX INFO: loaded from: classes.dex */
public class C0905c implements InterfaceC0923u, Comparable<C0905c> {

    /* JADX INFO: renamed from: e */
    static float f4163e = StickFighterApplication.m4490a().getResources().getDimensionPixelSize(R.dimen.edit_mark_thickness);

    /* JADX INFO: renamed from: a */
    public final int f4164a;

    /* JADX INFO: renamed from: b */
    public int f4165b;

    /* JADX INFO: renamed from: c */
    public final int f4166c;

    /* JADX INFO: renamed from: d */
    public transient C0906d f4167d = null;

    /* JADX INFO: renamed from: f */
    float f4168f;

    /* JADX INFO: renamed from: g */
    float f4169g;

    /* JADX INFO: renamed from: h */
    ArrayList f4170h;

    /* JADX INFO: renamed from: i */
    transient AbstractC0908f f4171i;

    public C0905c(long j, int i, float f, float f2, int i2) {
        this.f4164a = (int) j;
        this.f4168f = f;
        this.f4169g = f2;
        this.f4166c = i;
        if (j > -1) {
            this.f4170h = new ArrayList();
        }
        this.f4165b = i2;
    }

    /* JADX INFO: renamed from: a */
    private void m4840a(C0905c c0905c, ArrayList arrayList, SparseArray sparseArray) {
        if ((c0905c.f4168f == this.f4168f && c0905c.f4169g == this.f4169g) || arrayList.contains(this)) {
            return;
        }
        arrayList.add(this);
        ArrayList arrayList2 = new ArrayList();
        m4857a(arrayList2, sparseArray);
        ArrayList arrayListM4843d = m4843d(sparseArray);
        Iterator it = arrayList2.iterator();
        while (it.hasNext()) {
            ((C0905c) it.next()).m4840a(c0905c, arrayList, sparseArray);
        }
        Iterator it2 = arrayListM4843d.iterator();
        while (it2.hasNext()) {
            ((C0905c) it2.next()).m4840a(c0905c, arrayList, sparseArray);
        }
    }

    /* JADX INFO: renamed from: b */
    private void m4841b(C0905c c0905c) {
        m4860b(((c0905c.m4863c() - m4863c()) * 2.0f) + this.f4168f, this.f4169g);
    }

    /* JADX INFO: renamed from: c */
    private void m4842c(PointF pointF, PointF pointF2, C0912j c0912j, SparseArray sparseArray) {
        if (this.f4167d == null || this.f4167d.f4173b == null) {
            m4846e(sparseArray);
        }
        float fM4848a = m4848a(pointF, pointF2, this.f4167d.f4173b, c0912j);
        for (C0905c c0905c : (Iterable<C0905c>) this.f4167d.f4174c) {
            if (c0905c.m4859b() != 2 || !c0905c.m4858a()) {
                c0905c.m4852a(fM4848a, this.f4167d.f4173b, c0912j);
            }
        }
    }

    /* JADX INFO: renamed from: d */
    private ArrayList m4843d(SparseArray sparseArray) {
        ArrayList arrayList = new ArrayList();
        ArrayList<C0905c> arrayList2 = new ArrayList();
        m4857a(arrayList2, sparseArray);
        for (C0905c c0905c : arrayList2) {
            for (C0905c c0905c2 : (Iterable<C0905c>) c0905c.m4868g().m4872a()) {
                if (c0905c != c0905c2) {
                    arrayList.add(c0905c2);
                }
            }
        }
        return arrayList;
    }

    /* JADX INFO: renamed from: d */
    private void m4844d(PointF pointF, PointF pointF2, C0912j c0912j, SparseArray sparseArray) {
        if (this.f4167d == null) {
            this.f4167d = new C0906d(this);
        }
        if (this.f4167d.f4176e == null) {
            Iterator it = m4850a(sparseArray).iterator();
            while (it.hasNext()) {
                for (C0905c c0905c : (Iterable<C0905c>) ((AbstractC0908f) it.next()).m4872a()) {
                    if (c0905c.m4859b() == 2) {
                        this.f4167d.f4176e = c0905c;
                        break;
                    }
                }
            }
        }
        float fM4848a = m4848a(pointF, pointF2, this.f4167d.f4176e, c0912j);
        Iterator it2 = this.f4167d.f4172a.iterator();
        while (it2.hasNext()) {
            for (C0905c c0905c2 : (Iterable<C0905c>) ((AbstractC0908f) it2.next()).m4872a()) {
                if (c0905c2.m4859b() != 2 || !c0905c2.m4858a()) {
                    c0905c2.m4852a(fM4848a, this.f4167d.f4176e, c0912j);
                }
            }
        }
    }

    /* JADX INFO: renamed from: e */
    private void m4845e(PointF pointF, PointF pointF2, C0912j c0912j, SparseArray sparseArray) {
        if (this.f4167d == null || this.f4167d.f4173b == null) {
            m4846e(sparseArray);
        }
        float fM4890c = c0912j.m4890c(pointF2.x);
        float fM4891d = c0912j.m4891d(pointF2.y);
        Iterator it = this.f4167d.f4175d.iterator();
        while (it.hasNext()) {
            ((C0905c) it.next()).m4860b(fM4890c, fM4891d);
        }
    }

    /* JADX INFO: renamed from: e */
    private void m4846e(SparseArray sparseArray) {
        int size;
        C0905c c0905c;
        if (this.f4167d == null) {
            this.f4167d = new C0906d(this);
        }
        ArrayList arrayList = new ArrayList();
        m4857a(arrayList, sparseArray);
        this.f4167d.f4175d = arrayList;
        ArrayList<C0905c> arrayListM4843d = m4843d(sparseArray);
        HashMap map = new HashMap();
        C0905c c0905c2 = null;
        int i = -1;
        for (C0905c c0905c3 : arrayListM4843d) {
            ArrayList arrayList2 = new ArrayList();
            c0905c3.m4840a(this, arrayList2, sparseArray);
            map.put(c0905c3, arrayList2);
            if (arrayList2.size() > i) {
                c0905c = c0905c3;
                size = arrayList2.size();
            } else {
                size = i;
                c0905c = c0905c2;
            }
            c0905c2 = c0905c;
            i = size;
        }
        this.f4167d.f4173b = c0905c2;
        this.f4167d.f4174c = new ArrayList();
        this.f4167d.f4174c.addAll(arrayList);
        for (C0905c c0905c4 : arrayListM4843d) {
            if (c0905c4 != c0905c2) {
                this.f4167d.f4174c.addAll((Collection) map.get(c0905c4));
            }
        }
    }

    /* JADX INFO: renamed from: f */
    private void m4847f(PointF pointF, PointF pointF2, C0912j c0912j, SparseArray sparseArray) {
        if (this.f4167d == null) {
            this.f4167d = new C0906d(this);
        }
        if (this.f4167d.f4172a == null) {
            this.f4167d.f4172a = m4850a(sparseArray);
        }
        if (this.f4167d.f4176e == null) {
            Iterator it = m4850a(sparseArray).iterator();
            while (it.hasNext()) {
                for (C0905c c0905c : (Iterable<C0905c>) ((AbstractC0908f) it.next()).m4872a()) {
                    if (c0905c.m4859b() == 2) {
                        this.f4167d.f4176e = c0905c;
                        break;
                    }
                }
            }
        }
        Iterator it2 = this.f4167d.f4172a.iterator();
        while (it2.hasNext()) {
            Iterator it3 = ((AbstractC0908f) it2.next()).m4872a().iterator();
            while (it3.hasNext()) {
                ((C0905c) it3.next()).m4841b(this.f4167d.f4176e);
            }
        }
    }

    /* JADX INFO: renamed from: a */
    public float m4848a(PointF pointF, PointF pointF2, C0905c c0905c, C0912j c0912j) {
        if (pointF2 == null || pointF == null) {
            return 0.0f;
        }
        float fM4887a = c0912j.m4887a(c0905c.m4863c());
        float fM4889b = c0912j.m4889b(c0905c.m4865d());
        return (float) (Math.atan2(pointF2.y - fM4889b, pointF2.x - fM4887a) - Math.atan2(pointF.y - fM4889b, pointF.x - fM4887a));
    }

    @Override // java.lang.Comparable
    /* JADX INFO: renamed from: a, reason: merged with bridge method [inline-methods] */
    public int compareTo(C0905c c0905c) {
        return this.f4166c - c0905c.f4166c;
    }

    /* JADX INFO: renamed from: a */
    public ArrayList m4850a(SparseArray sparseArray) {
        int size;
        C0905c c0905c;
        if (this.f4167d != null && this.f4167d.f4172a != null) {
            return this.f4167d.f4172a;
        }
        if (this.f4167d == null) {
            this.f4167d = new C0906d(this);
        }
        if (this.f4167d.f4175d == null) {
            ArrayList arrayList = new ArrayList();
            m4857a(arrayList, sparseArray);
            this.f4167d.f4175d = arrayList;
        }
        ArrayList<C0905c> arrayListM4843d = m4843d(sparseArray);
        HashMap map = new HashMap();
        C0905c c0905c2 = null;
        int i = -1;
        for (C0905c c0905c3 : arrayListM4843d) {
            ArrayList arrayList2 = new ArrayList();
            c0905c3.m4840a(this, arrayList2, sparseArray);
            map.put(c0905c3, arrayList2);
            if (arrayList2.size() > i) {
                c0905c = c0905c3;
                size = arrayList2.size();
            } else {
                size = i;
                c0905c = c0905c2;
            }
            c0905c2 = c0905c;
            i = size;
        }
        this.f4167d.f4173b = c0905c2;
        this.f4167d.f4174c = new ArrayList();
        this.f4167d.f4174c.addAll(this.f4167d.f4175d);
        for (C0905c c0905c4 : arrayListM4843d) {
            if (c0905c4 != c0905c2) {
                this.f4167d.f4174c.addAll((Collection) map.get(c0905c4));
            }
        }
        HashMap map2 = new HashMap();
        Iterator it = map.keySet().iterator();
        while (it.hasNext()) {
            Iterator it2 = ((ArrayList) map.get((C0905c) it.next())).iterator();
            while (it2.hasNext()) {
                map2.put(((C0905c) it2.next()).m4868g(), 0L);
            }
        }
        return new ArrayList(map2.keySet());
    }

    /* JADX INFO: renamed from: a */
    public void m4851a(float f, float f2) {
        this.f4168f += f;
        this.f4169g += f2;
    }

    /* JADX INFO: renamed from: a */
    public void m4852a(float f, C0905c c0905c, C0912j c0912j) {
        float fM4887a = c0912j.m4887a(m4863c());
        float fM4889b = c0912j.m4889b(m4865d());
        float fM4887a2 = c0912j.m4887a(c0905c.m4863c());
        float fM4889b2 = c0912j.m4889b(c0905c.m4865d());
        float f2 = fM4887a - fM4887a2;
        float f3 = fM4889b - fM4889b2;
        float fSin = (float) Math.sin(f);
        float fCos = (float) Math.cos(f);
        this.f4168f = c0912j.m4890c(fM4887a2 + ((f2 * fCos) - (f3 * fSin)));
        this.f4169g = c0912j.m4891d((f2 * fSin) + (f3 * fCos) + fM4889b2);
    }

    /* JADX INFO: renamed from: a */
    public void m4853a(Canvas canvas, C0912j c0912j) {
        Paint paint = null;
        if (m4858a()) {
            float fM4887a = c0912j.m4887a(m4863c());
            float fM4889b = c0912j.m4889b(m4865d());
            int iM4859b = m4859b();
            if (iM4859b == 8 || iM4859b == 4) {
                if (C0802a.f3819a == 0) {
                    paint = c0912j.f4186a.f4293j;
                } else if (C0802a.f3819a == 1) {
                    paint = c0912j.f4186a.f4294k;
                } else if (C0802a.f3819a == 2) {
                }
            } else if (iM4859b == 2) {
                if (C0802a.f3819a == 0) {
                    paint = c0912j.f4186a.f4292i;
                } else if (C0802a.f3819a == 1) {
                    paint = c0912j.f4186a.f4295l;
                } else if (C0802a.f3819a == 2) {
                    paint = c0912j.f4186a.f4296m;
                }
            }
            if (paint != null) {
                canvas.drawCircle(fM4887a, fM4889b, f4163e, paint);
            }
        }
    }

    @Override // com.tweber.stickfighter.p038h.InterfaceC0923u
    /* JADX INFO: renamed from: a */
    public void mo4854a(PointF pointF, PointF pointF2, C0912j c0912j, SparseArray sparseArray) {
        if (C0802a.f3819a != 0) {
            if (C0802a.f3819a == 1) {
                switch (m4859b()) {
                    case 4:
                    case 8:
                        m4845e(pointF, pointF2, c0912j, sparseArray);
                        break;
                }
            }
            return;
        }
        switch (m4859b()) {
            case 2:
                this.f4171i.m4873a(c0912j.m4890c(pointF2.x) - c0912j.m4890c(pointF.x), c0912j.m4891d(pointF2.y) - c0912j.m4891d(pointF.y), sparseArray);
                break;
            case 4:
                m4842c(pointF, pointF2, c0912j, sparseArray);
                break;
            case 8:
                m4844d(pointF, pointF2, c0912j, sparseArray);
                break;
        }
    }

    /* JADX INFO: renamed from: a */
    public void m4855a(C0904b c0904b) {
        this.f4170h.add(c0904b);
    }

    /* JADX INFO: renamed from: a */
    public void m4856a(AbstractC0908f abstractC0908f) {
        this.f4171i = abstractC0908f;
    }

    /* JADX INFO: renamed from: a */
    public void m4857a(ArrayList arrayList, SparseArray sparseArray) {
        if (arrayList.contains(this)) {
            return;
        }
        arrayList.add(this);
        Iterator it = m4866e().iterator();
        while (it.hasNext()) {
            C0905c c0905c = (C0905c) sparseArray.get(((C0904b) it.next()).f4162a);
            if (c0905c != null) {
                c0905c.m4857a(arrayList, sparseArray);
            }
        }
    }

    /* JADX INFO: renamed from: a */
    public boolean m4858a() {
        return (this.f4165b & 1) == 1;
    }

    /* JADX INFO: renamed from: b */
    public int m4859b() {
        return (this.f4165b & 2) | (this.f4165b & 4) | (this.f4165b & 8);
    }

    /* JADX INFO: renamed from: b */
    public void m4860b(float f, float f2) {
        this.f4168f = f;
        this.f4169g = f2;
    }

    @Override // com.tweber.stickfighter.p038h.InterfaceC0923u
    /* JADX INFO: renamed from: b */
    public void mo4861b(PointF pointF, PointF pointF2, C0912j c0912j, SparseArray sparseArray) {
        m4847f(pointF, pointF2, c0912j, sparseArray);
    }

    @Override // com.tweber.stickfighter.p038h.InterfaceC0923u
    /* JADX INFO: renamed from: b */
    public void mo4862b(SparseArray sparseArray) {
        if (this.f4167d == null) {
            this.f4167d = new C0906d(this);
        }
        if (this.f4167d.f4172a == null) {
            this.f4167d.f4172a = m4850a(sparseArray);
        }
        Iterator it = this.f4167d.f4172a.iterator();
        while (it.hasNext()) {
            ((AbstractC0908f) it.next()).m4882c();
        }
    }

    /* JADX INFO: renamed from: c */
    public float m4863c() {
        return this.f4168f;
    }

    @Override // com.tweber.stickfighter.p038h.InterfaceC0923u
    /* JADX INFO: renamed from: c */
    public void mo4864c(SparseArray sparseArray) {
        if (this.f4167d == null) {
            this.f4167d = new C0906d(this);
        }
        if (this.f4167d.f4172a == null) {
            this.f4167d.f4172a = m4850a(sparseArray);
        }
        Iterator it = this.f4167d.f4172a.iterator();
        while (it.hasNext()) {
            ((AbstractC0908f) it.next()).m4883d();
        }
    }

    /* JADX INFO: renamed from: d */
    public float m4865d() {
        return this.f4169g;
    }

    /* JADX INFO: renamed from: e */
    public ArrayList m4866e() {
        return this.f4170h;
    }

    /* JADX INFO: renamed from: f */
    public ArrayList m4867f() {
        if (this.f4167d == null) {
            return null;
        }
        return this.f4167d.f4172a;
    }

    /* JADX INFO: renamed from: g */
    public AbstractC0908f m4868g() {
        return this.f4171i;
    }

    @Override // com.tweber.stickfighter.p038h.InterfaceC0923u
    /* JADX INFO: renamed from: h */
    public boolean mo4869h() {
        return m4859b() == 2;
    }

    /* JADX INFO: renamed from: i */
    public void m4870i() {
        this.f4167d = null;
    }

    /* JADX INFO: renamed from: j */
    public String m4871j() {
        StringBuilder sb = new StringBuilder("<AP ID=\"");
        sb.append(this.f4164a).append("\" Flags=\"").append(this.f4165b).append("\" Index=\"").append(this.f4166c).append("\" X=\"").append(this.f4168f).append("\" Y=\"").append(this.f4169g).append("\">");
        Iterator it = this.f4170h.iterator();
        while (it.hasNext()) {
            sb.append(((C0904b) it.next()).m4839a());
        }
        sb.append("</AP>");
        return sb.toString();
    }
}
