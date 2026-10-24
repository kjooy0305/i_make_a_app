package com.tweber.stickfighter.p037g;

import android.os.AsyncTask;
import com.tweber.stickfighter.p038h.C0918p;
import java.util.ArrayList;

/* JADX INFO: loaded from: classes.dex */
class ay extends AsyncTask<Void, Void, Void> {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ ae f4074a;

    /* JADX INFO: renamed from: b */
    int f4075b;

    /* JADX INFO: renamed from: c */
    ArrayList f4076c;

    public ay(ae aeVar, int i, ArrayList arrayList) {
        this.f4074a = aeVar;
        this.f4075b = i;
        this.f4076c = arrayList;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.os.AsyncTask
    /* JADX INFO: renamed from: a, reason: merged with bridge method [inline-methods] */
    public Void doInBackground(Void... voidArr) {
        synchronized (this.f4076c) {
            this.f4076c.clear();
            C0918p[] c0918pArr = new C0918p[this.f4074a.f4038j + 1];
            for (int i = 0; i < this.f4074a.f4038j + 1; i++) {
                c0918pArr[i] = this.f4074a.f4040l.m4939f().m4539a(this.f4075b + i);
            }
            float f = (1.0f * this.f4074a.f4038j) / this.f4074a.f4037i;
            for (int i2 = 0; i2 < this.f4074a.f4038j; i2++) {
                C0918p c0918p = c0918pArr[i2];
                C0918p c0918p2 = c0918pArr[i2 + 1];
                if (c0918p == null || c0918p2 == null) {
                    break;
                }
                c0918p.m4928c();
                c0918p2.m4928c();
                this.f4076c.add(c0918p);
                for (int i3 = 1; i3 < this.f4074a.f4037i / this.f4074a.f4038j; i3++) {
                    this.f4076c.add(C0918p.m4920a(c0918p, c0918p2, i3 * f));
                }
                c0918p.m4929d();
            }
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.os.AsyncTask
    /* JADX INFO: renamed from: a, reason: merged with bridge method [inline-methods] */
    public void onPostExecute(Void r1) {
    }
}
