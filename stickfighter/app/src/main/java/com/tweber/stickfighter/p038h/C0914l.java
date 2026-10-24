package com.tweber.stickfighter.p038h;

import android.content.Context;
import android.graphics.PointF;
import android.util.SparseArray;
import com.tweber.stickfighter.p034d.C0836a;
import com.tweber.stickfighter.p034d.InterfaceC0837b;
import java.util.ArrayList;

/* JADX INFO: renamed from: com.tweber.stickfighter.h.l */
/* JADX INFO: loaded from: classes.dex */
public class C0914l {

    /* JADX INFO: renamed from: c */
    static ArrayList f4197c;

    /* JADX INFO: renamed from: a */
    public final long f4198a;

    /* JADX INFO: renamed from: b */
    public final ArrayList f4199b;

    /* JADX INFO: renamed from: d */
    String f4200d;

    public C0914l(long j, String str, ArrayList arrayList) {
        this.f4198a = j;
        this.f4200d = str;
        this.f4199b = arrayList;
    }

    /* JADX INFO: renamed from: a */
    private static ArrayList m4893a(long j) {
        return C0836a.m4547a().mo4551a(j).m4939f().m4539a(0).m4922a();
    }

    /* JADX INFO: renamed from: a */
    public static void m4895a(long j, Context context, C0912j c0912j, C0918p c0918p, SparseArray sparseArray) {
        if (j == C0916n.f4209a.f4198a) {
            m4898a(c0912j, c0918p, sparseArray);
            return;
        }
        if (j == C0916n.f4210b.f4198a) {
            m4900b(c0912j, c0918p, sparseArray);
            return;
        }
        if (j == C0916n.f4211c.f4198a) {
            m4909k(c0912j, c0918p, sparseArray);
            return;
        }
        if (j == C0916n.f4212d.f4198a) {
            m4901c(c0912j, c0918p, sparseArray);
            return;
        }
        if (j == C0916n.f4213e.f4198a) {
            m4902d(c0912j, c0918p, sparseArray);
            return;
        }
        if (j == C0916n.f4214f.f4198a) {
            m4903e(c0912j, c0918p, sparseArray);
            return;
        }
        if (j == C0916n.f4215g.f4198a) {
            m4904f(c0912j, c0918p, sparseArray);
            return;
        }
        if (j == C0916n.f4216h.f4198a) {
            m4905g(c0912j, c0918p, sparseArray);
            return;
        }
        if (j == C0916n.f4217i.f4198a) {
            m4906h(c0912j, c0918p, sparseArray);
            return;
        }
        if (j == C0916n.f4218j.f4198a) {
            m4907i(c0912j, c0918p, sparseArray);
            return;
        }
        if (j == C0916n.f4219k.f4198a) {
            m4908j(c0912j, c0918p, sparseArray);
            return;
        }
        if (j == C0916n.f4220l.f4198a) {
            m4910l(c0912j, c0918p, sparseArray);
            return;
        }
        if (j == C0916n.f4221m.f4198a) {
            m4911m(c0912j, c0918p, sparseArray);
            return;
        }
        if (j == C0916n.f4222n.f4198a) {
            m4912n(c0912j, c0918p, sparseArray);
            return;
        }
        if (j == C0916n.f4223o.f4198a) {
            m4913o(c0912j, c0918p, sparseArray);
            return;
        }
        if (j == C0916n.f4224p.f4198a) {
            m4914p(c0912j, c0918p, sparseArray);
            return;
        }
        if (j == C0916n.f4225q.f4198a) {
            m4915q(c0912j, c0918p, sparseArray);
        } else if (j == C0916n.f4226r.f4198a) {
            m4916r(c0912j, c0918p, sparseArray);
        } else {
            m4896a(j, c0912j, c0918p, sparseArray);
        }
    }

    /* JADX INFO: renamed from: a */
    private static void m4896a(long j, C0912j c0912j, C0918p c0918p, SparseArray sparseArray) {
        ArrayList arrayListM4893a = m4893a(j);
        InterfaceC0837b interfaceC0837bM4547a = C0836a.m4547a();
        try {
            interfaceC0837bM4547a.mo4569c();
            SparseArray sparseArray2 = new SparseArray();
            for (int i = 0; i < arrayListM4893a.size(); i++) {
                AbstractC0908f abstractC0908f = (AbstractC0908f) arrayListM4893a.get(i);
                sparseArray2.put(abstractC0908f.f4181b, interfaceC0837bM4547a.mo4549a(c0918p, abstractC0908f.f4182c, abstractC0908f.m4880b(), new C0907e[0]));
            }
            SparseArray sparseArray3 = new SparseArray();
            for (int i2 = 0; i2 < arrayListM4893a.size(); i2++) {
                AbstractC0908f abstractC0908f2 = (AbstractC0908f) arrayListM4893a.get(i2);
                AbstractC0908f abstractC0908f3 = (AbstractC0908f) sparseArray2.get(abstractC0908f2.f4181b);
                for (int i3 = 0; i3 < abstractC0908f2.m4872a().size(); i3++) {
                    C0905c c0905c = (C0905c) abstractC0908f2.m4872a().get(i3);
                    C0905c c0905cMo4548a = interfaceC0837bM4547a.mo4548a(abstractC0908f3.f4181b, c0918p.f4232a, new PointF(c0905c.m4863c(), c0905c.m4865d()), c0905c.f4166c, c0905c.f4165b);
                    sparseArray3.put(c0905c.f4164a, c0905cMo4548a);
                    abstractC0908f3.m4876a(c0905cMo4548a);
                }
                m4897a(sparseArray, abstractC0908f3);
            }
            for (int i4 = 0; i4 < arrayListM4893a.size(); i4++) {
                AbstractC0908f abstractC0908f4 = (AbstractC0908f) arrayListM4893a.get(i4);
                for (int i5 = 0; i5 < abstractC0908f4.m4872a().size(); i5++) {
                    C0905c c0905c2 = (C0905c) abstractC0908f4.m4872a().get(i5);
                    C0905c c0905c3 = (C0905c) sparseArray3.get(c0905c2.f4164a);
                    for (int i6 = 0; i6 < c0905c2.m4866e().size(); i6++) {
                        c0905c3.m4855a(interfaceC0837bM4547a.mo4563b(c0905c3.f4164a, ((C0905c) sparseArray3.get(((C0904b) c0905c2.m4866e().get(i6)).f4162a)).f4164a));
                    }
                }
            }
            interfaceC0837bM4547a.mo4573d();
        } finally {
            interfaceC0837bM4547a.mo4574e();
        }
    }

    /* JADX INFO: renamed from: a */
    private static void m4897a(SparseArray sparseArray, AbstractC0908f abstractC0908f) {
        for (C0905c c0905c : (Iterable<C0905c>) abstractC0908f.m4872a()) {
            sparseArray.put(c0905c.f4164a, c0905c);
        }
    }

    /* JADX INFO: renamed from: a */
    private static void m4898a(C0912j c0912j, C0918p c0918p, SparseArray sparseArray) {
        InterfaceC0837b interfaceC0837bM4547a = C0836a.m4547a();
        try {
            float fM4889b = c0912j.m4889b(15.0f);
            float fM4887a = c0912j.m4887a(50.0f);
            PointF pointF = new PointF(50.0f, 5.0f);
            PointF pointF2 = new PointF(50.0f, 20.0f);
            PointF pointF3 = new PointF(c0912j.m4890c(fM4887a - (fM4889b / 2.0f)), 30.0f);
            PointF pointF4 = new PointF(c0912j.m4890c(fM4887a - fM4889b), 40.0f);
            PointF pointF5 = new PointF(c0912j.m4890c((fM4889b / 2.0f) + fM4887a), 30.0f);
            PointF pointF6 = new PointF(c0912j.m4890c(fM4887a + fM4889b), 40.0f);
            PointF pointF7 = new PointF(50.0f, 40.0f);
            PointF pointF8 = new PointF(c0912j.m4890c(fM4887a - (fM4889b / 2.0f)), 52.0f);
            PointF pointF9 = new PointF(c0912j.m4890c(fM4887a - (fM4889b / 2.0f)), 62.0f);
            PointF pointF10 = new PointF(c0912j.m4890c((fM4889b / 2.0f) + fM4887a), 52.0f);
            PointF pointF11 = new PointF(c0912j.m4890c((fM4889b / 2.0f) + fM4887a), 62.0f);
            interfaceC0837bM4547a.mo4569c();
            AbstractC0908f abstractC0908fMo4549a = interfaceC0837bM4547a.mo4549a(c0918p, 2, -16777216, new C0907e(pointF, 5), new C0907e(pointF2, 5));
            AbstractC0908f abstractC0908fMo4549a2 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF2, 4), new C0907e(pointF7, 3));
            AbstractC0908f abstractC0908fMo4549a3 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF3, 5), new C0907e(pointF2, 4));
            AbstractC0908f abstractC0908fMo4549a4 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF4, 5), new C0907e(pointF3, 4));
            AbstractC0908f abstractC0908fMo4549a5 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF5, 5), new C0907e(pointF2, 4));
            AbstractC0908f abstractC0908fMo4549a6 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF6, 5), new C0907e(pointF5, 4));
            AbstractC0908f abstractC0908fMo4549a7 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF7, 2), new C0907e(pointF8, 5));
            AbstractC0908f abstractC0908fMo4549a8 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF8, 4), new C0907e(pointF9, 5));
            AbstractC0908f abstractC0908fMo4549a9 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF7, 2), new C0907e(pointF10, 5));
            AbstractC0908f abstractC0908fMo4549a10 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF10, 4), new C0907e(pointF11, 5));
            m4897a(sparseArray, abstractC0908fMo4549a);
            m4897a(sparseArray, abstractC0908fMo4549a2);
            m4897a(sparseArray, abstractC0908fMo4549a3);
            m4897a(sparseArray, abstractC0908fMo4549a4);
            m4897a(sparseArray, abstractC0908fMo4549a5);
            m4897a(sparseArray, abstractC0908fMo4549a6);
            m4897a(sparseArray, abstractC0908fMo4549a7);
            m4897a(sparseArray, abstractC0908fMo4549a8);
            m4897a(sparseArray, abstractC0908fMo4549a9);
            m4897a(sparseArray, abstractC0908fMo4549a10);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a2, abstractC0908fMo4549a3);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a, abstractC0908fMo4549a2);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a3, abstractC0908fMo4549a4);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a2, abstractC0908fMo4549a5);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a5, abstractC0908fMo4549a6);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a2, abstractC0908fMo4549a7);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a2, abstractC0908fMo4549a9);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a7, abstractC0908fMo4549a8);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a9, abstractC0908fMo4549a10);
            interfaceC0837bM4547a.mo4573d();
        } finally {
            interfaceC0837bM4547a.mo4574e();
        }
    }

    /* JADX INFO: renamed from: b */
    private static void m4900b(C0912j c0912j, C0918p c0918p, SparseArray sparseArray) {
        InterfaceC0837b interfaceC0837bM4547a = C0836a.m4547a();
        try {
            float fM4889b = c0912j.m4889b(50.0f) - c0912j.m4889b(5.0f);
            float fM4887a = c0912j.m4887a(50.0f);
            PointF pointF = new PointF(50.0f, 5.0f);
            PointF pointF2 = new PointF(c0912j.m4890c(fM4887a - (fM4889b / 20.0f)), 12.0f);
            PointF pointF3 = new PointF(c0912j.m4890c((fM4889b / 20.0f) + fM4887a), 12.0f);
            PointF pointF4 = new PointF(c0912j.m4890c(fM4887a - (fM4889b / 20.0f)), 50.0f);
            PointF pointF5 = new PointF(c0912j.m4890c((fM4889b / 20.0f) + fM4887a), 50.0f);
            PointF pointF6 = new PointF(c0912j.m4890c(fM4887a - (fM4889b / 8.0f)), 48.0f);
            PointF pointF7 = new PointF(c0912j.m4890c((fM4889b / 8.0f) + fM4887a), 48.0f);
            PointF pointF8 = new PointF(50.0f, 50.0f);
            PointF pointF9 = new PointF(50.0f, 60.0f);
            PointF pointF10 = new PointF(50.0f, 63.0f);
            interfaceC0837bM4547a.mo4569c();
            AbstractC0908f abstractC0908fMo4549a = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF, 9), new C0907e(pointF2, 8));
            AbstractC0908f abstractC0908fMo4549a2 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF, 8), new C0907e(pointF3, 8));
            AbstractC0908f abstractC0908fMo4549a3 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF3, 8), new C0907e(pointF5, 8));
            AbstractC0908f abstractC0908fMo4549a4 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF2, 8), new C0907e(pointF4, 8));
            AbstractC0908f abstractC0908fMo4549a5 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF4, 8), new C0907e(pointF8, 8));
            AbstractC0908f abstractC0908fMo4549a6 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF5, 8), new C0907e(pointF8, 8));
            AbstractC0908f abstractC0908fMo4549a7 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF6, 8), new C0907e(pointF4, 8));
            AbstractC0908f abstractC0908fMo4549a8 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF7, 8), new C0907e(pointF5, 8));
            AbstractC0908f abstractC0908fMo4549a9 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF8, 3), new C0907e(pointF9, 8));
            AbstractC0908f abstractC0908fMo4549a10 = interfaceC0837bM4547a.mo4549a(c0918p, 2, -16777216, new C0907e(pointF9, 8), new C0907e(pointF10, 9));
            m4897a(sparseArray, abstractC0908fMo4549a);
            m4897a(sparseArray, abstractC0908fMo4549a2);
            m4897a(sparseArray, abstractC0908fMo4549a3);
            m4897a(sparseArray, abstractC0908fMo4549a4);
            m4897a(sparseArray, abstractC0908fMo4549a5);
            m4897a(sparseArray, abstractC0908fMo4549a6);
            m4897a(sparseArray, abstractC0908fMo4549a7);
            m4897a(sparseArray, abstractC0908fMo4549a8);
            m4897a(sparseArray, abstractC0908fMo4549a9);
            m4897a(sparseArray, abstractC0908fMo4549a10);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a, abstractC0908fMo4549a2);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a, abstractC0908fMo4549a4);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a2, abstractC0908fMo4549a3);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a7, abstractC0908fMo4549a5);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a7, abstractC0908fMo4549a4);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a8, abstractC0908fMo4549a3);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a8, abstractC0908fMo4549a6);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a5, abstractC0908fMo4549a6);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a9, abstractC0908fMo4549a6);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a5, abstractC0908fMo4549a9);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a10, abstractC0908fMo4549a9);
            interfaceC0837bM4547a.mo4573d();
        } finally {
            interfaceC0837bM4547a.mo4574e();
        }
    }

    /* JADX INFO: renamed from: c */
    private static void m4901c(C0912j c0912j, C0918p c0918p, SparseArray sparseArray) {
        InterfaceC0837b interfaceC0837bM4547a = C0836a.m4547a();
        try {
            float fM4889b = c0912j.m4889b(50.0f) - c0912j.m4889b(48.0f);
            float fM4887a = c0912j.m4887a(50.0f);
            PointF pointF = new PointF(50.0f, 51.0f);
            PointF pointF2 = new PointF(c0912j.m4890c(fM4887a - (7.0f * fM4889b)), 51.0f);
            PointF pointF3 = new PointF(50.0f, 55.0f);
            PointF pointF4 = new PointF(c0912j.m4890c(fM4887a - (7.0f * fM4889b)), 55.0f);
            PointF pointF5 = new PointF(c0912j.m4890c(fM4887a - (6.0f * fM4889b)), 55.0f);
            PointF pointF6 = new PointF(c0912j.m4890c(fM4887a - (4.0f * fM4889b)), 55.0f);
            PointF pointF7 = new PointF(c0912j.m4890c(fM4887a - (7.0f * fM4889b)), 60.0f);
            PointF pointF8 = new PointF(c0912j.m4890c(fM4887a - (fM4889b * 5.0f)), 60.0f);
            interfaceC0837bM4547a.mo4569c();
            AbstractC0908f abstractC0908fMo4549a = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF, 8), new C0907e(pointF2, 3));
            AbstractC0908f abstractC0908fMo4549a2 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF2, 8), new C0907e(pointF4, 8));
            AbstractC0908f abstractC0908fMo4549a3 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF3, 8), new C0907e(pointF, 9));
            AbstractC0908f abstractC0908fMo4549a4 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF4, 8), new C0907e(pointF5, 8));
            AbstractC0908f abstractC0908fMo4549a5 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF6, 8), new C0907e(pointF3, 8));
            AbstractC0908f abstractC0908fMo4549a6 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF5, 8), new C0907e(pointF7, 8));
            AbstractC0908f abstractC0908fMo4549a7 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF6, 8), new C0907e(pointF8, 8));
            AbstractC0908f abstractC0908fMo4549a8 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF7, 9), new C0907e(pointF8, 8));
            m4897a(sparseArray, abstractC0908fMo4549a);
            m4897a(sparseArray, abstractC0908fMo4549a2);
            m4897a(sparseArray, abstractC0908fMo4549a3);
            m4897a(sparseArray, abstractC0908fMo4549a4);
            m4897a(sparseArray, abstractC0908fMo4549a5);
            m4897a(sparseArray, abstractC0908fMo4549a6);
            m4897a(sparseArray, abstractC0908fMo4549a7);
            m4897a(sparseArray, abstractC0908fMo4549a8);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a2, abstractC0908fMo4549a4);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a2, abstractC0908fMo4549a);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a, abstractC0908fMo4549a3);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a5, abstractC0908fMo4549a3);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a6, abstractC0908fMo4549a4);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a6, abstractC0908fMo4549a8);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a7, abstractC0908fMo4549a8);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a7, abstractC0908fMo4549a5);
            interfaceC0837bM4547a.mo4573d();
        } finally {
            interfaceC0837bM4547a.mo4574e();
        }
    }

    /* JADX INFO: renamed from: d */
    private static void m4902d(C0912j c0912j, C0918p c0918p, SparseArray sparseArray) {
        InterfaceC0837b interfaceC0837bM4547a = C0836a.m4547a();
        try {
            float fM4889b = c0912j.m4889b(50.0f) - c0912j.m4889b(46.0f);
            float fM4887a = c0912j.m4887a(50.0f);
            PointF pointF = new PointF(50.0f, 50.0f);
            PointF pointF2 = new PointF(c0912j.m4890c(fM4887a - (fM4889b / 2.0f)), 48.0f);
            PointF pointF3 = new PointF(c0912j.m4890c(fM4887a - (fM4889b / 2.0f)), 52.0f);
            PointF pointF4 = new PointF(c0912j.m4890c(fM4887a - (fM4889b * 2.0f)), 48.0f);
            PointF pointF5 = new PointF(c0912j.m4890c(fM4887a - (fM4889b * 2.0f)), 52.0f);
            interfaceC0837bM4547a.mo4569c();
            AbstractC0908f abstractC0908fMo4549a = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF2, 8), new C0907e(pointF, 3));
            AbstractC0908f abstractC0908fMo4549a2 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF3, 8), new C0907e(pointF, 8));
            AbstractC0908f abstractC0908fMo4549a3 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF2, 8), new C0907e(pointF4, 8));
            AbstractC0908f abstractC0908fMo4549a4 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF3, 8), new C0907e(pointF5, 8));
            AbstractC0908f abstractC0908fMo4549a5 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF4, 9), new C0907e(pointF5, 9));
            m4897a(sparseArray, abstractC0908fMo4549a);
            m4897a(sparseArray, abstractC0908fMo4549a2);
            m4897a(sparseArray, abstractC0908fMo4549a3);
            m4897a(sparseArray, abstractC0908fMo4549a4);
            m4897a(sparseArray, abstractC0908fMo4549a5);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a, abstractC0908fMo4549a2);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a, abstractC0908fMo4549a3);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a3, abstractC0908fMo4549a5);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a5, abstractC0908fMo4549a4);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a4, abstractC0908fMo4549a2);
            interfaceC0837bM4547a.mo4573d();
        } finally {
            interfaceC0837bM4547a.mo4574e();
        }
    }

    /* JADX INFO: renamed from: e */
    private static void m4903e(C0912j c0912j, C0918p c0918p, SparseArray sparseArray) {
        InterfaceC0837b interfaceC0837bM4547a = C0836a.m4547a();
        try {
            float fM4889b = c0912j.m4889b(50.0f) - c0912j.m4889b(5.0f);
            float fM4887a = c0912j.m4887a(50.0f);
            PointF pointF = new PointF(50.0f, 5.0f);
            PointF pointF2 = new PointF(50.0f, 7.0f);
            PointF pointF3 = new PointF(50.0f, 48.0f);
            PointF pointF4 = new PointF(50.0f, 50.0f);
            PointF pointF5 = new PointF(50.0f, 27.5f);
            PointF pointF6 = new PointF(c0912j.m4890c((fM4889b / 3.0f) + fM4887a), 23.4f);
            PointF pointF7 = new PointF(c0912j.m4890c((fM4889b / 3.0f) + fM4887a), 31.599998f);
            PointF pointF8 = new PointF(c0912j.m4890c((fM4889b / 5.0f) + fM4887a), 15.2f);
            PointF pointF9 = new PointF(c0912j.m4890c((fM4889b / 5.0f) + fM4887a), 39.8f);
            interfaceC0837bM4547a.mo4569c();
            AbstractC0908f abstractC0908fMo4549a = interfaceC0837bM4547a.mo4549a(c0918p, 2, -16777216, new C0907e(pointF2, 8), new C0907e(pointF, 9));
            AbstractC0908f abstractC0908fMo4549a2 = interfaceC0837bM4547a.mo4549a(c0918p, 2, -16777216, new C0907e(pointF3, 8), new C0907e(pointF4, 9));
            AbstractC0908f abstractC0908fMo4549a3 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF, 8), new C0907e(pointF5, 9));
            AbstractC0908f abstractC0908fMo4549a4 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF4, 8), new C0907e(pointF5, 8));
            AbstractC0908f abstractC0908fMo4549a5 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF2, 8), new C0907e(pointF8, 8));
            AbstractC0908f abstractC0908fMo4549a6 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF3, 8), new C0907e(pointF9, 8));
            AbstractC0908f abstractC0908fMo4549a7 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF6, 8), new C0907e(pointF8, 8));
            AbstractC0908f abstractC0908fMo4549a8 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF7, 8), new C0907e(pointF9, 8));
            AbstractC0908f abstractC0908fMo4549a9 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF6, 3), new C0907e(pointF7, 9));
            m4897a(sparseArray, abstractC0908fMo4549a);
            m4897a(sparseArray, abstractC0908fMo4549a2);
            m4897a(sparseArray, abstractC0908fMo4549a3);
            m4897a(sparseArray, abstractC0908fMo4549a4);
            m4897a(sparseArray, abstractC0908fMo4549a5);
            m4897a(sparseArray, abstractC0908fMo4549a6);
            m4897a(sparseArray, abstractC0908fMo4549a7);
            m4897a(sparseArray, abstractC0908fMo4549a8);
            m4897a(sparseArray, abstractC0908fMo4549a9);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a3, abstractC0908fMo4549a4);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a, abstractC0908fMo4549a3);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a4, abstractC0908fMo4549a2);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a, abstractC0908fMo4549a5);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a2, abstractC0908fMo4549a6);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a5, abstractC0908fMo4549a7);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a6, abstractC0908fMo4549a8);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a7, abstractC0908fMo4549a8);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a7, abstractC0908fMo4549a9);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a8, abstractC0908fMo4549a9);
            interfaceC0837bM4547a.mo4573d();
        } finally {
            interfaceC0837bM4547a.mo4574e();
        }
    }

    /* JADX INFO: renamed from: f */
    private static void m4904f(C0912j c0912j, C0918p c0918p, SparseArray sparseArray) {
        InterfaceC0837b interfaceC0837bM4547a = C0836a.m4547a();
        try {
            float fM4889b = c0912j.m4889b(80.0f) - c0912j.m4889b(40.0f);
            PointF pointF = new PointF(50.0f, 40.0f);
            PointF pointF2 = new PointF(50.0f, 80.0f);
            PointF pointF3 = new PointF(50.0f - c0912j.m4890c(fM4889b / 14.0f), 44.0f);
            PointF pointF4 = new PointF(50.0f + c0912j.m4890c(fM4889b / 14.0f), 44.0f);
            PointF pointF5 = new PointF(50.0f - c0912j.m4890c(fM4889b / 10.0f), 84.0f);
            PointF pointF6 = new PointF(50.0f + c0912j.m4890c(fM4889b / 10.0f), 84.0f);
            PointF pointF7 = new PointF(50.0f, 78.0f);
            PointF pointF8 = new PointF(50.0f - c0912j.m4890c(fM4889b / 10.0f), 82.0f);
            PointF pointF9 = new PointF(c0912j.m4890c(fM4889b / 10.0f) + 50.0f, 82.0f);
            interfaceC0837bM4547a.mo4569c();
            AbstractC0908f abstractC0908fMo4549a = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF, 8), new C0907e(pointF7, 8));
            AbstractC0908f abstractC0908fMo4549a2 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF7, 8), new C0907e(pointF2, 9));
            AbstractC0908f abstractC0908fMo4549a3 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF, 8), new C0907e(pointF3, 8));
            AbstractC0908f abstractC0908fMo4549a4 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF, 3), new C0907e(pointF4, 8));
            AbstractC0908f abstractC0908fMo4549a5 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF2, 8), new C0907e(pointF5, 8));
            AbstractC0908f abstractC0908fMo4549a6 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF2, 8), new C0907e(pointF6, 8));
            AbstractC0908f abstractC0908fMo4549a7 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF7, 8), new C0907e(pointF8, 8));
            AbstractC0908f abstractC0908fMo4549a8 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF7, 8), new C0907e(pointF9, 8));
            m4897a(sparseArray, abstractC0908fMo4549a);
            m4897a(sparseArray, abstractC0908fMo4549a2);
            m4897a(sparseArray, abstractC0908fMo4549a3);
            m4897a(sparseArray, abstractC0908fMo4549a4);
            m4897a(sparseArray, abstractC0908fMo4549a5);
            m4897a(sparseArray, abstractC0908fMo4549a6);
            m4897a(sparseArray, abstractC0908fMo4549a7);
            m4897a(sparseArray, abstractC0908fMo4549a8);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a, abstractC0908fMo4549a3);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a, abstractC0908fMo4549a4);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a, abstractC0908fMo4549a7);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a, abstractC0908fMo4549a8);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a, abstractC0908fMo4549a2);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a2, abstractC0908fMo4549a5);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a2, abstractC0908fMo4549a6);
            interfaceC0837bM4547a.mo4573d();
        } finally {
            interfaceC0837bM4547a.mo4574e();
        }
    }

    /* JADX INFO: renamed from: g */
    private static void m4905g(C0912j c0912j, C0918p c0918p, SparseArray sparseArray) {
        InterfaceC0837b interfaceC0837bM4547a = C0836a.m4547a();
        try {
            PointF pointF = new PointF(50.0f, 40.0f);
            PointF pointF2 = new PointF(50.0f, 60.0f);
            interfaceC0837bM4547a.mo4569c();
            m4897a(sparseArray, interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF, 9), new C0907e(pointF2, 3)));
            interfaceC0837bM4547a.mo4573d();
        } finally {
            interfaceC0837bM4547a.mo4574e();
        }
    }

    /* JADX INFO: renamed from: h */
    private static void m4906h(C0912j c0912j, C0918p c0918p, SparseArray sparseArray) {
        InterfaceC0837b interfaceC0837bM4547a = C0836a.m4547a();
        try {
            PointF pointF = new PointF(50.0f, 40.0f);
            PointF pointF2 = new PointF(50.0f, 60.0f);
            interfaceC0837bM4547a.mo4569c();
            m4897a(sparseArray, interfaceC0837bM4547a.mo4549a(c0918p, 2, -16777216, new C0907e(pointF, 9), new C0907e(pointF2, 3)));
            interfaceC0837bM4547a.mo4573d();
        } finally {
            interfaceC0837bM4547a.mo4574e();
        }
    }

    /* JADX INFO: renamed from: i */
    private static void m4907i(C0912j c0912j, C0918p c0918p, SparseArray sparseArray) {
        InterfaceC0837b interfaceC0837bM4547a = C0836a.m4547a();
        try {
            float fM4889b = c0912j.m4889b(55.0f) - c0912j.m4889b(45.0f);
            float fM4887a = c0912j.m4887a(50.0f);
            float fSqrt = (float) (((double) (fM4889b * 2.0f)) / Math.sqrt(3.0d));
            PointF pointF = new PointF(50.0f, 45.0f);
            PointF pointF2 = new PointF(c0912j.m4890c(fM4887a - (fSqrt / 2.0f)), 55.0f);
            PointF pointF3 = new PointF(c0912j.m4890c((fSqrt / 2.0f) + fM4887a), 55.0f);
            interfaceC0837bM4547a.mo4569c();
            AbstractC0908f abstractC0908fMo4549a = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF, 3), new C0907e(pointF2, 9));
            AbstractC0908f abstractC0908fMo4549a2 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF, 8), new C0907e(pointF3, 8));
            AbstractC0908f abstractC0908fMo4549a3 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF2, 8), new C0907e(pointF3, 9));
            m4897a(sparseArray, abstractC0908fMo4549a);
            m4897a(sparseArray, abstractC0908fMo4549a2);
            m4897a(sparseArray, abstractC0908fMo4549a3);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a, abstractC0908fMo4549a2);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a, abstractC0908fMo4549a3);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a2, abstractC0908fMo4549a3);
            interfaceC0837bM4547a.mo4573d();
        } finally {
            interfaceC0837bM4547a.mo4574e();
        }
    }

    /* JADX INFO: renamed from: j */
    private static void m4908j(C0912j c0912j, C0918p c0918p, SparseArray sparseArray) {
        InterfaceC0837b interfaceC0837bM4547a = C0836a.m4547a();
        try {
            float fM4889b = c0912j.m4889b(55.0f) - c0912j.m4889b(45.0f);
            float fM4887a = c0912j.m4887a(50.0f);
            PointF pointF = new PointF(50.0f, 45.0f);
            PointF pointF2 = new PointF(c0912j.m4890c(fM4887a + fM4889b), 45.0f);
            PointF pointF3 = new PointF(50.0f, 55.0f);
            PointF pointF4 = new PointF(c0912j.m4890c(fM4889b + fM4887a), 55.0f);
            interfaceC0837bM4547a.mo4569c();
            AbstractC0908f abstractC0908fMo4549a = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF, 3), new C0907e(pointF2, 9));
            AbstractC0908f abstractC0908fMo4549a2 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF, 8), new C0907e(pointF3, 9));
            AbstractC0908f abstractC0908fMo4549a3 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF2, 8), new C0907e(pointF4, 8));
            AbstractC0908f abstractC0908fMo4549a4 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF3, 8), new C0907e(pointF4, 9));
            m4897a(sparseArray, abstractC0908fMo4549a);
            m4897a(sparseArray, abstractC0908fMo4549a2);
            m4897a(sparseArray, abstractC0908fMo4549a3);
            m4897a(sparseArray, abstractC0908fMo4549a4);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a, abstractC0908fMo4549a3);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a, abstractC0908fMo4549a2);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a3, abstractC0908fMo4549a4);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a2, abstractC0908fMo4549a4);
            interfaceC0837bM4547a.mo4573d();
        } finally {
            interfaceC0837bM4547a.mo4574e();
        }
    }

    /* JADX INFO: renamed from: k */
    private static void m4909k(C0912j c0912j, C0918p c0918p, SparseArray sparseArray) {
        InterfaceC0837b interfaceC0837bM4547a = C0836a.m4547a();
        try {
            float fM4889b = c0912j.m4889b(80.0f) - c0912j.m4889b(25.0f);
            float fM4887a = c0912j.m4887a(50.0f);
            PointF pointF = new PointF(50.0f, 25.0f);
            PointF pointF2 = new PointF(50.0f, 29.0f);
            PointF pointF3 = new PointF(50.0f, 80.0f);
            PointF pointF4 = new PointF(c0912j.m4890c((fM4889b / 12.0f) + fM4887a), 27.0f - 4.0f);
            PointF pointF5 = new PointF(c0912j.m4890c((fM4889b / 10.0f) + fM4887a), 27.0f - 8.0f);
            PointF pointF6 = new PointF(c0912j.m4890c((fM4889b / 6.0f) + fM4887a), 27.0f - 4.0f);
            PointF pointF7 = new PointF(c0912j.m4890c((fM4889b / 12.0f) + fM4887a), 4.0f + 27.0f);
            PointF pointF8 = new PointF(c0912j.m4890c((fM4889b / 10.0f) + fM4887a), 8.0f + 27.0f);
            PointF pointF9 = new PointF(c0912j.m4890c((fM4889b / 6.0f) + fM4887a), 4.0f + 27.0f);
            PointF pointF10 = new PointF(c0912j.m4890c(fM4887a - (fM4889b / 12.0f)), 27.0f - 4.0f);
            PointF pointF11 = new PointF(c0912j.m4890c(fM4887a - (fM4889b / 10.0f)), 27.0f - 8.0f);
            PointF pointF12 = new PointF(c0912j.m4890c(fM4887a - (fM4889b / 6.0f)), 27.0f - 4.0f);
            PointF pointF13 = new PointF(c0912j.m4890c(fM4887a - (fM4889b / 12.0f)), 4.0f + 27.0f);
            PointF pointF14 = new PointF(c0912j.m4890c(fM4887a - (fM4889b / 10.0f)), 8.0f + 27.0f);
            PointF pointF15 = new PointF(c0912j.m4890c(fM4887a - (fM4889b / 6.0f)), 4.0f + 27.0f);
            interfaceC0837bM4547a.mo4569c();
            AbstractC0908f abstractC0908fMo4549a = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF2, 3), new C0907e(pointF3, 9));
            AbstractC0908f abstractC0908fMo4549a2 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF2, 8), new C0907e(pointF, 8));
            AbstractC0908f abstractC0908fMo4549a3 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF, 8), new C0907e(pointF4, 8));
            AbstractC0908f abstractC0908fMo4549a4 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF4, 8), new C0907e(pointF5, 9));
            AbstractC0908f abstractC0908fMo4549a5 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF5, 8), new C0907e(pointF6, 8));
            AbstractC0908f abstractC0908fMo4549a6 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF6, 8), new C0907e(pointF9, 8));
            AbstractC0908f abstractC0908fMo4549a7 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF2, 8), new C0907e(pointF7, 8));
            AbstractC0908f abstractC0908fMo4549a8 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF7, 8), new C0907e(pointF8, 9));
            AbstractC0908f abstractC0908fMo4549a9 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF8, 8), new C0907e(pointF9, 8));
            AbstractC0908f abstractC0908fMo4549a10 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF, 8), new C0907e(pointF10, 8));
            AbstractC0908f abstractC0908fMo4549a11 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF10, 8), new C0907e(pointF11, 9));
            AbstractC0908f abstractC0908fMo4549a12 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF11, 8), new C0907e(pointF12, 8));
            AbstractC0908f abstractC0908fMo4549a13 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF12, 8), new C0907e(pointF15, 8));
            AbstractC0908f abstractC0908fMo4549a14 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF2, 8), new C0907e(pointF13, 8));
            AbstractC0908f abstractC0908fMo4549a15 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF13, 8), new C0907e(pointF14, 9));
            AbstractC0908f abstractC0908fMo4549a16 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF14, 8), new C0907e(pointF15, 8));
            m4897a(sparseArray, abstractC0908fMo4549a);
            m4897a(sparseArray, abstractC0908fMo4549a2);
            m4897a(sparseArray, abstractC0908fMo4549a3);
            m4897a(sparseArray, abstractC0908fMo4549a4);
            m4897a(sparseArray, abstractC0908fMo4549a5);
            m4897a(sparseArray, abstractC0908fMo4549a6);
            m4897a(sparseArray, abstractC0908fMo4549a7);
            m4897a(sparseArray, abstractC0908fMo4549a8);
            m4897a(sparseArray, abstractC0908fMo4549a9);
            m4897a(sparseArray, abstractC0908fMo4549a10);
            m4897a(sparseArray, abstractC0908fMo4549a11);
            m4897a(sparseArray, abstractC0908fMo4549a12);
            m4897a(sparseArray, abstractC0908fMo4549a13);
            m4897a(sparseArray, abstractC0908fMo4549a14);
            m4897a(sparseArray, abstractC0908fMo4549a15);
            m4897a(sparseArray, abstractC0908fMo4549a16);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a, abstractC0908fMo4549a2);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a2, abstractC0908fMo4549a3);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a3, abstractC0908fMo4549a4);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a4, abstractC0908fMo4549a5);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a5, abstractC0908fMo4549a6);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a6, abstractC0908fMo4549a9);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a9, abstractC0908fMo4549a8);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a8, abstractC0908fMo4549a7);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a7, abstractC0908fMo4549a);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a2, abstractC0908fMo4549a10);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a10, abstractC0908fMo4549a11);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a11, abstractC0908fMo4549a12);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a12, abstractC0908fMo4549a13);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a13, abstractC0908fMo4549a16);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a16, abstractC0908fMo4549a15);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a15, abstractC0908fMo4549a14);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a14, abstractC0908fMo4549a);
            interfaceC0837bM4547a.mo4573d();
        } finally {
            interfaceC0837bM4547a.mo4574e();
        }
    }

    /* JADX INFO: renamed from: l */
    private static void m4910l(C0912j c0912j, C0918p c0918p, SparseArray sparseArray) {
        InterfaceC0837b interfaceC0837bM4547a = C0836a.m4547a();
        try {
            float fM4889b = c0912j.m4889b(65.0f) - c0912j.m4889b(25.0f);
            float fM4887a = c0912j.m4887a(50.0f);
            PointF pointF = new PointF(50.0f, 25.0f);
            PointF pointF2 = new PointF(50.0f, 35.0f);
            PointF pointF3 = new PointF(50.0f, 45.0f);
            PointF pointF4 = new PointF(50.0f, 55.0f);
            PointF pointF5 = new PointF(50.0f, 65.0f);
            PointF pointF6 = new PointF(c0912j.m4890c((fM4889b / 4.0f) + fM4887a), 25.0f);
            PointF pointF7 = new PointF(c0912j.m4890c((fM4889b / 4.0f) + fM4887a), 35.0f);
            PointF pointF8 = new PointF(c0912j.m4890c((fM4889b / 4.0f) + fM4887a), 45.0f);
            PointF pointF9 = new PointF(c0912j.m4890c((fM4889b / 4.0f) + fM4887a), 55.0f);
            PointF pointF10 = new PointF(c0912j.m4890c((fM4889b / 4.0f) + fM4887a), 65.0f);
            interfaceC0837bM4547a.mo4569c();
            AbstractC0908f abstractC0908fMo4549a = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF, 9), new C0907e(pointF2, 8));
            AbstractC0908f abstractC0908fMo4549a2 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF2, 9), new C0907e(pointF3, 8));
            AbstractC0908f abstractC0908fMo4549a3 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF3, 9), new C0907e(pointF4, 8));
            AbstractC0908f abstractC0908fMo4549a4 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF4, 9), new C0907e(pointF5, 3));
            AbstractC0908f abstractC0908fMo4549a5 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF6, 9), new C0907e(pointF7, 9));
            AbstractC0908f abstractC0908fMo4549a6 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF7, 8), new C0907e(pointF8, 9));
            AbstractC0908f abstractC0908fMo4549a7 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF8, 8), new C0907e(pointF9, 9));
            AbstractC0908f abstractC0908fMo4549a8 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF9, 8), new C0907e(pointF10, 9));
            AbstractC0908f abstractC0908fMo4549a9 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF2, 8), new C0907e(pointF7, 8));
            AbstractC0908f abstractC0908fMo4549a10 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF3, 8), new C0907e(pointF8, 8));
            AbstractC0908f abstractC0908fMo4549a11 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF4, 8), new C0907e(pointF9, 8));
            m4897a(sparseArray, abstractC0908fMo4549a);
            m4897a(sparseArray, abstractC0908fMo4549a2);
            m4897a(sparseArray, abstractC0908fMo4549a3);
            m4897a(sparseArray, abstractC0908fMo4549a4);
            m4897a(sparseArray, abstractC0908fMo4549a5);
            m4897a(sparseArray, abstractC0908fMo4549a6);
            m4897a(sparseArray, abstractC0908fMo4549a7);
            m4897a(sparseArray, abstractC0908fMo4549a8);
            m4897a(sparseArray, abstractC0908fMo4549a9);
            m4897a(sparseArray, abstractC0908fMo4549a10);
            m4897a(sparseArray, abstractC0908fMo4549a11);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a, abstractC0908fMo4549a2);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a2, abstractC0908fMo4549a3);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a3, abstractC0908fMo4549a4);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a5, abstractC0908fMo4549a6);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a6, abstractC0908fMo4549a7);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a7, abstractC0908fMo4549a8);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a9, abstractC0908fMo4549a2);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a10, abstractC0908fMo4549a2);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a11, abstractC0908fMo4549a3);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a9, abstractC0908fMo4549a6);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a10, abstractC0908fMo4549a6);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a11, abstractC0908fMo4549a7);
            interfaceC0837bM4547a.mo4573d();
        } finally {
            interfaceC0837bM4547a.mo4574e();
        }
    }

    /* JADX INFO: renamed from: m */
    private static void m4911m(C0912j c0912j, C0918p c0918p, SparseArray sparseArray) {
        InterfaceC0837b interfaceC0837bM4547a = C0836a.m4547a();
        try {
            float fM4889b = c0912j.m4889b(55.0f) - c0912j.m4889b(25.0f);
            float fM4887a = c0912j.m4887a(50.0f);
            PointF pointF = new PointF(50.0f, 25.0f);
            PointF pointF2 = new PointF(c0912j.m4890c(fM4887a - (fM4889b / 3.0f)), 25.0f);
            PointF pointF3 = new PointF(c0912j.m4890c(fM4887a - (fM4889b / 3.0f)), 35.0f);
            PointF pointF4 = new PointF(c0912j.m4890c(fM4887a - (((float) fM4889b / 3.0f) * 2.0f)), 35.0f);
            PointF pointF5 = new PointF(c0912j.m4890c(fM4887a - (((float) fM4889b / 3.0f) * 2.0f)), 45.0f);
            PointF pointF6 = new PointF(c0912j.m4890c(fM4887a - fM4889b), 45.0f);
            PointF pointF7 = new PointF(c0912j.m4890c(fM4887a - fM4889b), 55.0f);
            interfaceC0837bM4547a.mo4569c();
            AbstractC0908f abstractC0908fMo4549a = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF, 9), new C0907e(pointF2, 8));
            AbstractC0908f abstractC0908fMo4549a2 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF2, 9), new C0907e(pointF3, 8));
            AbstractC0908f abstractC0908fMo4549a3 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF3, 9), new C0907e(pointF4, 8));
            AbstractC0908f abstractC0908fMo4549a4 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF4, 9), new C0907e(pointF5, 8));
            AbstractC0908f abstractC0908fMo4549a5 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF5, 9), new C0907e(pointF6, 8));
            AbstractC0908f abstractC0908fMo4549a6 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF6, 9), new C0907e(pointF7, 3));
            m4897a(sparseArray, abstractC0908fMo4549a);
            m4897a(sparseArray, abstractC0908fMo4549a2);
            m4897a(sparseArray, abstractC0908fMo4549a3);
            m4897a(sparseArray, abstractC0908fMo4549a4);
            m4897a(sparseArray, abstractC0908fMo4549a5);
            m4897a(sparseArray, abstractC0908fMo4549a6);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a, abstractC0908fMo4549a2);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a2, abstractC0908fMo4549a3);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a3, abstractC0908fMo4549a4);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a4, abstractC0908fMo4549a5);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a5, abstractC0908fMo4549a6);
            interfaceC0837bM4547a.mo4573d();
        } finally {
            interfaceC0837bM4547a.mo4574e();
        }
    }

    /* JADX INFO: renamed from: n */
    private static void m4912n(C0912j c0912j, C0918p c0918p, SparseArray sparseArray) {
        InterfaceC0837b interfaceC0837bM4547a = C0836a.m4547a();
        try {
            float fM4889b = c0912j.m4889b(55.0f) - c0912j.m4889b(25.0f);
            float fM4887a = c0912j.m4887a(50.0f);
            PointF pointF = new PointF(50.0f, 25.0f);
            PointF pointF2 = new PointF(c0912j.m4890c((fM4889b / 2.0f) + fM4887a), 25.0f);
            PointF pointF3 = new PointF(c0912j.m4890c(fM4887a - (fM4889b / 5.0f)), 36.0f);
            PointF pointF4 = new PointF(c0912j.m4890c((fM4889b / 2.0f) + fM4887a + (fM4889b / 5.0f)), 36.0f);
            PointF pointF5 = new PointF(c0912j.m4890c((fM4889b / 4.0f) + fM4887a), 55.0f);
            interfaceC0837bM4547a.mo4569c();
            AbstractC0908f abstractC0908fMo4549a = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF, 9), new C0907e(pointF2, 8));
            AbstractC0908f abstractC0908fMo4549a2 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF, 8), new C0907e(pointF3, 8));
            AbstractC0908f abstractC0908fMo4549a3 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF2, 9), new C0907e(pointF4, 8));
            AbstractC0908f abstractC0908fMo4549a4 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF3, 9), new C0907e(pointF5, 8));
            AbstractC0908f abstractC0908fMo4549a5 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF4, 9), new C0907e(pointF5, 3));
            m4897a(sparseArray, abstractC0908fMo4549a);
            m4897a(sparseArray, abstractC0908fMo4549a2);
            m4897a(sparseArray, abstractC0908fMo4549a3);
            m4897a(sparseArray, abstractC0908fMo4549a4);
            m4897a(sparseArray, abstractC0908fMo4549a5);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a, abstractC0908fMo4549a2);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a2, abstractC0908fMo4549a4);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a4, abstractC0908fMo4549a5);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a5, abstractC0908fMo4549a3);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a3, abstractC0908fMo4549a);
            interfaceC0837bM4547a.mo4573d();
        } finally {
            interfaceC0837bM4547a.mo4574e();
        }
    }

    /* JADX INFO: renamed from: o */
    private static void m4913o(C0912j c0912j, C0918p c0918p, SparseArray sparseArray) {
        InterfaceC0837b interfaceC0837bM4547a = C0836a.m4547a();
        try {
            float fM4889b = c0912j.m4889b(55.0f) - c0912j.m4889b(25.0f);
            float fM4887a = c0912j.m4887a(50.0f);
            float f = fM4889b / 20.0f;
            PointF pointF = new PointF(50.0f, 25.0f);
            PointF pointF2 = new PointF(c0912j.m4890c(fM4887a - ((2.0f * f) * 0.75f)), 30.0f);
            PointF pointF3 = new PointF(c0912j.m4890c(fM4887a - ((2.0f * f) * 0.75f)), 35.0f);
            PointF pointF4 = new PointF(c0912j.m4890c(fM4887a - ((3.0f * f) * 0.75f)), 40.0f);
            PointF pointF5 = new PointF(c0912j.m4890c(fM4887a - ((2.0f * f) * 0.75f)), 40.0f);
            PointF pointF6 = new PointF(c0912j.m4890c(fM4887a - ((2.0f * f) * 0.75f)), 49.0f);
            PointF pointF7 = new PointF(c0912j.m4890c(fM4887a - ((3.5f * f) * 0.75f)), 55.0f);
            PointF pointF8 = new PointF(c0912j.m4890c((2.0f * f * 0.75f) + fM4887a), 30.0f);
            PointF pointF9 = new PointF(c0912j.m4890c((2.0f * f * 0.75f) + fM4887a), 35.0f);
            PointF pointF10 = new PointF(c0912j.m4890c((3.0f * f * 0.75f) + fM4887a), 40.0f);
            PointF pointF11 = new PointF(c0912j.m4890c((2.0f * f * 0.75f) + fM4887a), 40.0f);
            PointF pointF12 = new PointF(c0912j.m4890c((2.0f * f * 0.75f) + fM4887a), 49.0f);
            PointF pointF13 = new PointF(c0912j.m4890c((f * 3.5f * 0.75f) + fM4887a), 55.0f);
            interfaceC0837bM4547a.mo4569c();
            AbstractC0908f abstractC0908fMo4549a = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF2, 8), new C0907e(pointF, 3));
            AbstractC0908f abstractC0908fMo4549a2 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF2, 8), new C0907e(pointF3, 8));
            AbstractC0908f abstractC0908fMo4549a3 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF3, 8), new C0907e(pointF4, 8));
            AbstractC0908f abstractC0908fMo4549a4 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF4, 8), new C0907e(pointF5, 8));
            AbstractC0908f abstractC0908fMo4549a5 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF5, 8), new C0907e(pointF6, 8));
            AbstractC0908f abstractC0908fMo4549a6 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF6, 8), new C0907e(pointF7, 8));
            AbstractC0908f abstractC0908fMo4549a7 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF13, 9), new C0907e(pointF7, 9));
            AbstractC0908f abstractC0908fMo4549a8 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF8, 8), new C0907e(pointF, 1));
            AbstractC0908f abstractC0908fMo4549a9 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF8, 8), new C0907e(pointF9, 8));
            AbstractC0908f abstractC0908fMo4549a10 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF9, 8), new C0907e(pointF10, 8));
            AbstractC0908f abstractC0908fMo4549a11 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF10, 8), new C0907e(pointF11, 8));
            AbstractC0908f abstractC0908fMo4549a12 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF11, 8), new C0907e(pointF12, 8));
            AbstractC0908f abstractC0908fMo4549a13 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF12, 8), new C0907e(pointF13, 8));
            m4897a(sparseArray, abstractC0908fMo4549a);
            m4897a(sparseArray, abstractC0908fMo4549a2);
            m4897a(sparseArray, abstractC0908fMo4549a3);
            m4897a(sparseArray, abstractC0908fMo4549a4);
            m4897a(sparseArray, abstractC0908fMo4549a5);
            m4897a(sparseArray, abstractC0908fMo4549a6);
            m4897a(sparseArray, abstractC0908fMo4549a7);
            m4897a(sparseArray, abstractC0908fMo4549a8);
            m4897a(sparseArray, abstractC0908fMo4549a9);
            m4897a(sparseArray, abstractC0908fMo4549a10);
            m4897a(sparseArray, abstractC0908fMo4549a11);
            m4897a(sparseArray, abstractC0908fMo4549a12);
            m4897a(sparseArray, abstractC0908fMo4549a13);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a, abstractC0908fMo4549a2);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a2, abstractC0908fMo4549a3);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a3, abstractC0908fMo4549a4);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a4, abstractC0908fMo4549a5);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a5, abstractC0908fMo4549a6);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a7, abstractC0908fMo4549a6);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a7, abstractC0908fMo4549a13);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a, abstractC0908fMo4549a8);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a8, abstractC0908fMo4549a9);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a9, abstractC0908fMo4549a10);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a10, abstractC0908fMo4549a11);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a11, abstractC0908fMo4549a12);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a12, abstractC0908fMo4549a13);
            interfaceC0837bM4547a.mo4573d();
        } finally {
            interfaceC0837bM4547a.mo4574e();
        }
    }

    /* JADX INFO: renamed from: p */
    private static void m4914p(C0912j c0912j, C0918p c0918p, SparseArray sparseArray) {
        InterfaceC0837b interfaceC0837bM4547a = C0836a.m4547a();
        try {
            float fM4889b = c0912j.m4889b(55.0f) - c0912j.m4889b(35.0f);
            float fM4887a = c0912j.m4887a(30.0f);
            float f = fM4889b / 10.0f;
            PointF pointF = new PointF(30.0f, 35.0f);
            PointF pointF2 = new PointF(c0912j.m4890c((3.0f * f) + fM4887a), 38.0f);
            PointF pointF3 = new PointF(c0912j.m4890c((24.0f * f) + fM4887a), 38.0f);
            PointF pointF4 = new PointF(c0912j.m4890c((24.0f * f) + fM4887a), 46.0f);
            PointF pointF5 = new PointF(c0912j.m4890c((14.0f * f) + fM4887a), 46.0f);
            PointF pointF6 = new PointF(c0912j.m4890c((14.0f * f) + fM4887a), 53.0f);
            PointF pointF7 = new PointF(c0912j.m4890c((12.0f * f) + fM4887a), 53.0f);
            PointF pointF8 = new PointF(c0912j.m4890c((12.0f * f) + fM4887a), 46.0f);
            PointF pointF9 = new PointF(c0912j.m4890c((f * 3.0f) + fM4887a), 46.0f);
            PointF pointF10 = new PointF(c0912j.m4890c(fM4887a), 49.0f);
            interfaceC0837bM4547a.mo4569c();
            AbstractC0908f abstractC0908fMo4549a = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF, 3), new C0907e(pointF2, 8));
            AbstractC0908f abstractC0908fMo4549a2 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF2, 8), new C0907e(pointF3, 9));
            AbstractC0908f abstractC0908fMo4549a3 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF3, 8), new C0907e(pointF4, 9));
            AbstractC0908f abstractC0908fMo4549a4 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF4, 8), new C0907e(pointF5, 8));
            AbstractC0908f abstractC0908fMo4549a5 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF5, 8), new C0907e(pointF6, 8));
            AbstractC0908f abstractC0908fMo4549a6 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF6, 8), new C0907e(pointF7, 8));
            AbstractC0908f abstractC0908fMo4549a7 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF7, 8), new C0907e(pointF8, 8));
            AbstractC0908f abstractC0908fMo4549a8 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF8, 8), new C0907e(pointF9, 8));
            AbstractC0908f abstractC0908fMo4549a9 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF9, 8), new C0907e(pointF10, 9));
            AbstractC0908f abstractC0908fMo4549a10 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF10, 8), new C0907e(pointF, 8));
            m4897a(sparseArray, abstractC0908fMo4549a);
            m4897a(sparseArray, abstractC0908fMo4549a2);
            m4897a(sparseArray, abstractC0908fMo4549a3);
            m4897a(sparseArray, abstractC0908fMo4549a4);
            m4897a(sparseArray, abstractC0908fMo4549a5);
            m4897a(sparseArray, abstractC0908fMo4549a6);
            m4897a(sparseArray, abstractC0908fMo4549a7);
            m4897a(sparseArray, abstractC0908fMo4549a8);
            m4897a(sparseArray, abstractC0908fMo4549a9);
            m4897a(sparseArray, abstractC0908fMo4549a10);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a, abstractC0908fMo4549a2);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a2, abstractC0908fMo4549a3);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a3, abstractC0908fMo4549a4);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a4, abstractC0908fMo4549a5);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a5, abstractC0908fMo4549a6);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a6, abstractC0908fMo4549a7);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a7, abstractC0908fMo4549a8);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a8, abstractC0908fMo4549a9);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a9, abstractC0908fMo4549a10);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a, abstractC0908fMo4549a10);
            interfaceC0837bM4547a.mo4573d();
        } finally {
            interfaceC0837bM4547a.mo4574e();
        }
    }

    /* JADX INFO: renamed from: q */
    private static void m4915q(C0912j c0912j, C0918p c0918p, SparseArray sparseArray) {
        InterfaceC0837b interfaceC0837bM4547a = C0836a.m4547a();
        try {
            float fM4889b = c0912j.m4889b(40.0f) - c0912j.m4889b(30.0f);
            float fM4887a = c0912j.m4887a(40.0f);
            float f = fM4889b / 10.0f;
            PointF pointF = new PointF(40.0f, 30.0f);
            PointF pointF2 = new PointF(c0912j.m4890c((10.0f * f) + fM4887a), 33.0f);
            PointF pointF3 = new PointF(c0912j.m4890c(fM4887a), 33.0f);
            PointF pointF4 = new PointF(c0912j.m4890c((10.0f * f) + fM4887a), 36.0f);
            PointF pointF5 = new PointF(c0912j.m4890c(fM4887a), 40.0f);
            PointF pointF6 = new PointF(c0912j.m4890c((10.0f * f) + fM4887a), 43.0f);
            PointF pointF7 = new PointF(c0912j.m4890c(fM4887a - (3.0f * f)), 40.0f);
            PointF pointF8 = new PointF(c0912j.m4890c((f * 7.0f) + fM4887a), 43.0f);
            interfaceC0837bM4547a.mo4569c();
            AbstractC0908f abstractC0908fMo4549a = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF, 3), new C0907e(pointF2, 9));
            AbstractC0908f abstractC0908fMo4549a2 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF, 8), new C0907e(pointF3, 8));
            AbstractC0908f abstractC0908fMo4549a3 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF2, 8), new C0907e(pointF4, 8));
            AbstractC0908f abstractC0908fMo4549a4 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF3, 8), new C0907e(pointF5, 9));
            AbstractC0908f abstractC0908fMo4549a5 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF4, 8), new C0907e(pointF6, 9));
            AbstractC0908f abstractC0908fMo4549a6 = interfaceC0837bM4547a.mo4549a(c0918p, 2, -16777216, new C0907e(pointF7, 8), new C0907e(pointF5, 8));
            AbstractC0908f abstractC0908fMo4549a7 = interfaceC0837bM4547a.mo4549a(c0918p, 2, -16777216, new C0907e(pointF8, 8), new C0907e(pointF6, 8));
            AbstractC0908f abstractC0908fMo4549a8 = interfaceC0837bM4547a.mo4549a(c0918p, 1, -16777216, new C0907e(pointF3, 8), new C0907e(pointF4, 8));
            m4897a(sparseArray, abstractC0908fMo4549a);
            m4897a(sparseArray, abstractC0908fMo4549a2);
            m4897a(sparseArray, abstractC0908fMo4549a3);
            m4897a(sparseArray, abstractC0908fMo4549a4);
            m4897a(sparseArray, abstractC0908fMo4549a5);
            m4897a(sparseArray, abstractC0908fMo4549a6);
            m4897a(sparseArray, abstractC0908fMo4549a7);
            m4897a(sparseArray, abstractC0908fMo4549a8);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a6, abstractC0908fMo4549a4);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a4, abstractC0908fMo4549a2);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a2, abstractC0908fMo4549a);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a, abstractC0908fMo4549a3);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a3, abstractC0908fMo4549a8);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a3, abstractC0908fMo4549a5);
            interfaceC0837bM4547a.mo4558a(abstractC0908fMo4549a5, abstractC0908fMo4549a7);
            interfaceC0837bM4547a.mo4573d();
        } finally {
            interfaceC0837bM4547a.mo4574e();
        }
    }

    /* JADX INFO: renamed from: r */
    private static void m4916r(C0912j c0912j, C0918p c0918p, SparseArray sparseArray) {
        InterfaceC0837b interfaceC0837bM4547a = C0836a.m4547a();
        try {
            PointF pointF = new PointF(40.0f, 30.0f);
            PointF pointF2 = new PointF(45.0f, 25.0f);
            PointF pointF3 = new PointF(50.0f, 30.0f);
            interfaceC0837bM4547a.mo4569c();
            m4897a(sparseArray, interfaceC0837bM4547a.mo4549a(c0918p, 3, -16777216, new C0907e(pointF, 3), new C0907e(pointF2, 9), new C0907e(pointF3, 9)));
            interfaceC0837bM4547a.mo4573d();
        } finally {
            interfaceC0837bM4547a.mo4574e();
        }
    }

    /* JADX INFO: renamed from: a */
    public String m4917a() {
        return this.f4200d;
    }
}
