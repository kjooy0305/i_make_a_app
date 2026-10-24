package com.tweber.stickfighter.p038h;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import com.tweber.stickfighter.StickFighterApplication;
import com.tweber.stickfighter.activities.R;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/* JADX INFO: renamed from: com.tweber.stickfighter.h.p */
/* JADX INFO: loaded from: classes.dex */
public class C0918p implements InterfaceC0913k {

    /* JADX INFO: renamed from: f */
    static transient int f4230f = StickFighterApplication.m4490a().getResources().getColor(R.color.canvas);

    /* JADX INFO: renamed from: g */
    static transient int f4231g = StickFighterApplication.m4490a().getResources().getDimensionPixelSize(R.dimen.frame_number_offset);

    /* JADX INFO: renamed from: a */
    public final int f4232a;

    /* JADX INFO: renamed from: b */
    public final int f4233b;

    /* JADX INFO: renamed from: d */
    int f4235d;

    /* JADX INFO: renamed from: e */
    ArrayList f4236e = null;

    /* JADX INFO: renamed from: c */
    public transient HashMap f4234c = null;

    public C0918p(int i, int i2, int i3) {
        this.f4232a = i;
        this.f4235d = i2;
        this.f4233b = i3;
    }

    /* JADX INFO: renamed from: a */
    private static C0905c m4919a(C0905c c0905c, ArrayList arrayList) {
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            C0905c c0905c2 = (C0905c) it.next();
            if (c0905c.f4166c == c0905c2.f4166c) {
                return c0905c2;
            }
        }
        return null;
    }

    /* JADX INFO: renamed from: a */
    public static InterfaceC0913k m4920a(C0918p c0918p, C0918p c0918p2, float f) {
        ArrayList arrayList = new ArrayList();
        for (AbstractC0908f abstractC0908f : (Iterable<AbstractC0908f>) c0918p.m4922a()) {
            AbstractC0908f abstractC0908f2 = (AbstractC0908f) c0918p2.f4234c.get(Integer.valueOf(abstractC0908f.f4181b));
            if (abstractC0908f2 != null) {
                arrayList.add(m4921a(abstractC0908f, abstractC0908f2, f));
            }
        }
        return new C0921s((C0920r[]) arrayList.toArray(new C0920r[arrayList.size()]));
    }

    /* JADX INFO: renamed from: a */
    private static C0920r m4921a(AbstractC0908f abstractC0908f, AbstractC0908f abstractC0908f2, float f) {
        int iM4880b = abstractC0908f.m4880b();
        int iM4880b2 = abstractC0908f2.m4880b();
        int iArgb = Color.argb(Color.alpha(iM4880b) + ((int) ((Color.alpha(iM4880b2) - Color.alpha(iM4880b)) * f)), Color.red(iM4880b) + ((int) ((Color.red(iM4880b2) - Color.red(iM4880b)) * f)), Color.green(iM4880b) + ((int) ((Color.green(iM4880b2) - Color.green(iM4880b)) * f)), ((int) ((Color.blue(iM4880b2) - Color.blue(iM4880b)) * f)) + Color.blue(iM4880b));
        ArrayList arrayListM4872a = abstractC0908f.m4872a();
        ArrayList arrayListM4872a2 = abstractC0908f2.m4872a();
        C0919q[] c0919qArr = new C0919q[arrayListM4872a.size()];
        int i = 0;
        while (true) {
            int i2 = i;
            if (i2 >= arrayListM4872a.size()) {
                return new C0920r(c0919qArr, abstractC0908f.f4182c, iArgb);
            }
            C0905c c0905c = (C0905c) arrayListM4872a.get(i2);
            float fM4863c = c0905c.m4863c();
            float fM4865d = c0905c.m4865d();
            C0905c c0905cM4919a = m4919a(c0905c, arrayListM4872a2);
            c0919qArr[i2] = new C0919q(fM4863c + ((c0905cM4919a.m4863c() - fM4863c) * f), ((c0905cM4919a.m4865d() - fM4865d) * f) + fM4865d);
            i = i2 + 1;
        }
    }

    /* JADX INFO: renamed from: a */
    public ArrayList m4922a() {
        if (this.f4236e == null) {
            this.f4236e = new ArrayList();
        }
        return this.f4236e;
    }

    /* JADX INFO: renamed from: a */
    public void m4923a(int i) {
        this.f4235d = i;
    }

    /* JADX INFO: renamed from: a */
    public void m4924a(AbstractC0908f abstractC0908f) {
        if (this.f4236e == null) {
            this.f4236e = new ArrayList();
        }
        this.f4236e.add(abstractC0908f);
    }

    @Override // com.tweber.stickfighter.p038h.InterfaceC0913k
    /* JADX INFO: renamed from: a */
    public void mo4892a(C0912j c0912j, Canvas canvas, boolean z, C0918p c0918p, int i) {
        m4926a(c0912j, canvas, z, c0918p, false, i, false);
    }

    /* JADX INFO: renamed from: a */
    public void m4925a(C0912j c0912j, Canvas canvas, boolean z, C0918p c0918p, int i, boolean z2) {
        m4926a(c0912j, canvas, z, c0918p, false, i, z2);
    }

    /* JADX INFO: renamed from: a */
    public void m4926a(C0912j c0912j, Canvas canvas, boolean z, C0918p c0918p, boolean z2, int i, boolean z3) {
        if (canvas == null) {
            return;
        }
        canvas.drawColor(f4230f);
        RectF rectFM4888a = c0912j.m4888a();
        c0912j.f4186a.f4287d.setColor(i);
        canvas.drawRect(rectFM4888a, c0912j.f4186a.f4287d);
        if (z) {
            if (z3) {
                canvas.drawText(String.valueOf(this.f4235d + 1), f4231g, canvas.getHeight() - f4231g, c0912j.f4186a.f4288e);
            }
            if (c0918p != null) {
                Iterator it = c0918p.m4922a().iterator();
                while (it.hasNext()) {
                    ((AbstractC0908f) it.next()).mo4878a(c0912j, canvas, c0912j.f4186a.f4291h);
                }
            }
        }
        for (AbstractC0908f abstractC0908f : (Iterable<AbstractC0908f>) m4922a()) {
            if (abstractC0908f.f4183d) {
                abstractC0908f.mo4877a(c0912j, canvas);
            }
        }
        for (AbstractC0908f abstractC0908f2 : (Iterable<AbstractC0908f>) m4922a()) {
            c0912j.f4186a.f4290g.setColor(abstractC0908f2.m4880b());
            abstractC0908f2.m4879a(c0912j, canvas, c0912j.f4186a.f4290g, z);
        }
        if (z) {
            Iterator it2 = m4922a().iterator();
            while (it2.hasNext()) {
                ((AbstractC0908f) it2.next()).mo4881b(c0912j, canvas);
            }
        }
        if (z3 && z2) {
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            paint.setColor(StickFighterApplication.m4490a().getResources().getColor(R.color.accent_dark));
            paint.setTextAlign(Paint.Align.RIGHT);
            paint.setTextSize((canvas.getHeight() / 10) * 3);
            canvas.drawText(String.valueOf(this.f4235d + 1), canvas.getWidth() - 15, canvas.getHeight() - 15, paint);
        }
    }

    /* JADX INFO: renamed from: b */
    public int m4927b() {
        return this.f4235d;
    }

    /* JADX INFO: renamed from: c */
    public void m4928c() {
        if (this.f4234c != null) {
            return;
        }
        if (this.f4236e == null) {
            this.f4236e = new ArrayList();
        }
        this.f4234c = new HashMap();
        for (AbstractC0908f abstractC0908f : (Iterable<AbstractC0908f>) this.f4236e) {
            this.f4234c.put(Integer.valueOf(abstractC0908f.f4181b), abstractC0908f);
        }
    }

    /* JADX INFO: renamed from: d */
    public void m4929d() {
        this.f4234c = null;
    }

    /* JADX INFO: renamed from: e */
    public String m4930e() {
        StringBuilder sb = new StringBuilder("<Frame ID=\"");
        sb.append(this.f4232a).append("\" Position=\"").append(this.f4235d).append("\">");
        if (this.f4236e == null) {
            this.f4236e = new ArrayList();
        }
        Iterator it = this.f4236e.iterator();
        while (it.hasNext()) {
            sb.append(((AbstractC0908f) it.next()).m4884e());
        }
        sb.append("</Frame>");
        return sb.toString();
    }
}
