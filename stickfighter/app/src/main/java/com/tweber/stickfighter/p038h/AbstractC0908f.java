package com.tweber.stickfighter.p038h;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.SparseArray;
import java.util.ArrayList;
import java.util.Iterator;

/* JADX INFO: renamed from: com.tweber.stickfighter.h.f */
/* JADX INFO: loaded from: classes.dex */
public abstract class AbstractC0908f {

    /* JADX INFO: renamed from: a */
    protected ArrayList f4180a;

    /* JADX INFO: renamed from: b */
    public final int f4181b;

    /* JADX INFO: renamed from: c */
    public final byte f4182c;

    /* JADX INFO: renamed from: d */
    protected transient boolean f4183d = false;

    /* JADX INFO: renamed from: e */
    int f4184e;

    protected AbstractC0908f(long j, long j2, ArrayList arrayList, byte b, int i) {
        this.f4180a = null;
        this.f4181b = (int) j;
        this.f4180a = arrayList == null ? new ArrayList(2) : arrayList;
        this.f4182c = b;
        this.f4184e = i;
        Iterator it = this.f4180a.iterator();
        while (it.hasNext()) {
            ((C0905c) it.next()).m4856a(this);
        }
    }

    /* JADX INFO: renamed from: a */
    public ArrayList m4872a() {
        return this.f4180a;
    }

    /* JADX INFO: renamed from: a */
    public void m4873a(float f, float f2, SparseArray sparseArray) {
        m4874a(f, f2, new ArrayList(), sparseArray);
    }

    /* JADX INFO: renamed from: a */
    public void m4874a(float f, float f2, ArrayList arrayList, SparseArray sparseArray) {
        if (arrayList.contains(this)) {
            return;
        }
        arrayList.add(this);
        for (C0905c c0905c : (Iterable<C0905c>) m4872a()) {
            c0905c.m4851a(f, f2);
            Iterator it = c0905c.m4866e().iterator();
            while (it.hasNext()) {
                C0905c c0905c2 = (C0905c) sparseArray.get(((C0904b) it.next()).f4162a);
                if (c0905c2 != null) {
                    c0905c2.m4868g().m4874a(f, f2, arrayList, sparseArray);
                }
            }
        }
    }

    /* JADX INFO: renamed from: a */
    public void m4875a(int i) {
        this.f4184e = i;
    }

    /* JADX INFO: renamed from: a */
    public void m4876a(C0905c c0905c) {
        this.f4180a.add(c0905c);
        c0905c.m4856a(this);
    }

    /* JADX INFO: renamed from: a */
    protected abstract void mo4877a(C0912j c0912j, Canvas canvas);

    /* JADX INFO: renamed from: a */
    protected abstract void mo4878a(C0912j c0912j, Canvas canvas, Paint paint);

    /* JADX INFO: renamed from: a */
    public void m4879a(C0912j c0912j, Canvas canvas, Paint paint, boolean z) {
        mo4878a(c0912j, canvas, paint);
    }

    /* JADX INFO: renamed from: b */
    public int m4880b() {
        return this.f4184e;
    }

    /* JADX INFO: renamed from: b */
    public void mo4881b(C0912j c0912j, Canvas canvas) {
        Iterator it = m4872a().iterator();
        while (it.hasNext()) {
            ((C0905c) it.next()).m4853a(canvas, c0912j);
        }
    }

    /* JADX INFO: renamed from: c */
    public void m4882c() {
        this.f4183d = true;
    }

    /* JADX INFO: renamed from: d */
    public void m4883d() {
        this.f4183d = false;
    }

    /* JADX INFO: renamed from: e */
    public String m4884e() {
        StringBuilder sb = new StringBuilder("<AO ID=\"");
        sb.append(this.f4181b).append("\" TypeId=\"").append((int) this.f4182c).append("\" Color=\"").append(this.f4184e).append("\">");
        Iterator it = this.f4180a.iterator();
        while (it.hasNext()) {
            sb.append(((C0905c) it.next()).m4871j());
        }
        sb.append("</AO>");
        return sb.toString();
    }
}
