package com.tweber.stickfighter.p038h;

import com.tweber.stickfighter.p034d.C0836a;
import java.util.ArrayList;

/* JADX INFO: renamed from: com.tweber.stickfighter.h.m */
/* JADX INFO: loaded from: classes.dex */
public class C0915m {

    /* JADX INFO: renamed from: a */
    public static final C0915m f4201a = new C0915m("Figures", C0916n.f4209a);

    /* JADX INFO: renamed from: b */
    public static final C0915m f4202b = new C0915m("Weapons", C0916n.f4210b, C0916n.f4222n, C0916n.f4211c, C0916n.f4214f, C0916n.f4212d, C0916n.f4224p);

    /* JADX INFO: renamed from: c */
    public static final C0915m f4203c = new C0915m("Ammo", C0916n.f4215g, C0916n.f4213e, C0916n.f4223o);

    /* JADX INFO: renamed from: d */
    public static final C0915m f4204d = new C0915m("Shapes", C0916n.f4216h, C0916n.f4217i, C0916n.f4226r, C0916n.f4218j, C0916n.f4219k);

    /* JADX INFO: renamed from: e */
    public static final C0915m f4205e = new C0915m("Misc", C0916n.f4220l, C0916n.f4221m, C0916n.f4225q);

    /* JADX INFO: renamed from: f */
    public static final C0915m f4206f = new C0915m("Building blocks", C0916n.f4216h, C0916n.f4217i, C0916n.f4226r);

    /* JADX INFO: renamed from: g */
    public final String f4207g;

    /* JADX INFO: renamed from: h */
    public final C0914l[] f4208h;

    private C0915m(String str, C0914l... c0914lArr) {
        this.f4207g = str;
        this.f4208h = c0914lArr;
    }

    /* JADX INFO: renamed from: a */
    public static C0915m[] m4918a(EnumC0917o enumC0917o) {
        if (enumC0917o == EnumC0917o.Primitives) {
            return new C0915m[]{f4206f};
        }
        ArrayList unused = C0914l.f4197c = C0836a.m4547a().mo4567b();
        C0914l[] c0914lArr = new C0914l[C0914l.f4197c.size()];
        for (int i = 0; i < C0914l.f4197c.size(); i++) {
            c0914lArr[i] = new C0914l(((C0924v) C0914l.f4197c.get(i)).m4935b(), ((C0924v) C0914l.f4197c.get(i)).m4936c(), null);
        }
        return new C0915m[]{f4201a, f4202b, f4203c, f4204d, f4205e, new C0915m("Custom", c0914lArr)};
    }

    public boolean equals(Object obj) {
        return (obj instanceof C0915m) && ((C0915m) obj).hashCode() == hashCode();
    }

    public int hashCode() {
        return this.f4207g.hashCode();
    }
}
