package com.tweber.stickfighter.p033c;

import com.tweber.stickfighter.p034d.C0836a;
import com.tweber.stickfighter.p038h.C0918p;
import java.util.Map;

/* JADX INFO: renamed from: com.tweber.stickfighter.c.a */
/* JADX INFO: loaded from: classes.dex */
public class C0834a {

    /* JADX INFO: renamed from: a */
    C0835b f3883a;

    /* JADX INFO: renamed from: b */
    int f3884b;

    public C0834a(long j) {
        this.f3883a = new C0835b(j);
        this.f3884b = C0836a.m4547a().mo4562b(j);
    }

    /* JADX INFO: renamed from: a */
    public int m4538a() {
        return this.f3884b;
    }

    /* JADX INFO: renamed from: a */
    public C0918p m4539a(int i) {
        return (C0918p) this.f3883a.get(Integer.valueOf(i));
    }

    /* JADX INFO: renamed from: a */
    public void m4540a(int i, C0918p c0918p) {
        Map mapSnapshot = this.f3883a.snapshot();
        this.f3883a.evictAll();
        for (Map.Entry entry : (Iterable<Map.Entry>) mapSnapshot.entrySet()) {
            int iM4927b = ((C0918p) entry.getValue()).m4927b();
            if (iM4927b < i) {
                this.f3883a.put(Integer.valueOf(iM4927b), (C0918p) entry.getValue());
            } else if (iM4927b >= i) {
                this.f3883a.put(Integer.valueOf(iM4927b + 1), (C0918p) entry.getValue());
                ((C0918p) entry.getValue()).m4923a(iM4927b + 1);
            }
        }
        this.f3883a.put(Integer.valueOf(i), c0918p);
        this.f3884b++;
    }

    /* JADX INFO: renamed from: a */
    public void m4541a(C0918p c0918p) {
        this.f3883a.put(Integer.valueOf(c0918p.m4927b()), c0918p);
        this.f3884b++;
    }

    /* JADX INFO: renamed from: b */
    public void m4542b() {
        this.f3883a.evictAll();
    }

    /* JADX INFO: renamed from: b */
    public void m4543b(int i) {
        this.f3883a.resize(i);
    }

    /* JADX INFO: renamed from: c */
    public void m4544c() {
        this.f3883a.resize(25);
    }

    /* JADX INFO: renamed from: c */
    public void m4545c(int i) {
        Map mapSnapshot = this.f3883a.snapshot();
        this.f3883a.evictAll();
        for (Map.Entry entry : (Iterable<Map.Entry>) mapSnapshot.entrySet()) {
            int iM4927b = ((C0918p) entry.getValue()).m4927b();
            if (iM4927b < i) {
                this.f3883a.put(Integer.valueOf(iM4927b), (C0918p) entry.getValue());
            } else if (iM4927b > i) {
                this.f3883a.put(Integer.valueOf(iM4927b - 1), (C0918p) entry.getValue());
                ((C0918p) entry.getValue()).m4923a(iM4927b - 1);
            }
        }
        this.f3884b--;
    }
}
