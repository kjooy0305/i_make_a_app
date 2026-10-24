package com.tweber.stickfighter.p038h;

import com.tweber.stickfighter.p033c.C0834a;

/* JADX INFO: renamed from: com.tweber.stickfighter.h.v */
/* JADX INFO: loaded from: classes.dex */
public class C0924v {

    /* JADX INFO: renamed from: a */
    static C0924v f4244a = null;

    /* JADX INFO: renamed from: b */
    int f4245b;

    /* JADX INFO: renamed from: c */
    String f4246c;

    /* JADX INFO: renamed from: d */
    float f4247d;

    /* JADX INFO: renamed from: e */
    int f4248e;

    /* JADX INFO: renamed from: f */
    C0834a f4249f;

    public C0924v(int i, String str, float f, int i2) {
        this.f4248e = -1;
        this.f4245b = i;
        this.f4246c = str;
        this.f4247d = f;
        this.f4248e = i2 == 0 ? -1 : i2;
        this.f4249f = new C0834a(i);
    }

    /* JADX INFO: renamed from: a */
    public static C0924v m4932a() {
        return f4244a;
    }

    /* JADX INFO: renamed from: a */
    public static void m4933a(C0924v c0924v) {
        f4244a = c0924v;
    }

    /* JADX INFO: renamed from: a */
    public void m4934a(int i) {
        this.f4248e = i;
    }

    /* JADX INFO: renamed from: b */
    public int m4935b() {
        return this.f4245b;
    }

    /* JADX INFO: renamed from: c */
    public String m4936c() {
        return this.f4246c;
    }

    /* JADX INFO: renamed from: d */
    public float m4937d() {
        return this.f4247d;
    }

    /* JADX INFO: renamed from: e */
    public int m4938e() {
        return this.f4248e;
    }

    public boolean equals(Object obj) {
        return obj != null && (obj instanceof C0924v) && ((C0924v) obj).m4935b() == m4935b();
    }

    /* JADX INFO: renamed from: f */
    public C0834a m4939f() {
        return this.f4249f;
    }

    public int hashCode() {
        return m4935b();
    }
}
