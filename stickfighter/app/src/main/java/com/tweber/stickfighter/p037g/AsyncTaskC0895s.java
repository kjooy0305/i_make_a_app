package com.tweber.stickfighter.p037g;

import android.os.AsyncTask;
import com.tweber.stickfighter.p034d.C0836a;
import com.tweber.stickfighter.p034d.InterfaceC0837b;
import com.tweber.stickfighter.p038h.AbstractC0908f;
import java.util.ArrayList;
import java.util.Iterator;

/* JADX INFO: renamed from: com.tweber.stickfighter.g.s */
/* JADX INFO: loaded from: classes.dex */
class AsyncTaskC0895s extends AsyncTask {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ ArrayList f4151a;

    /* JADX INFO: renamed from: b */
    final /* synthetic */ C0894r f4152b;

    AsyncTaskC0895s(C0894r c0894r, ArrayList arrayList) {
        this.f4152b = c0894r;
        this.f4151a = arrayList;
    }

    @Override // android.os.AsyncTask
    protected Object doInBackground(Object[] objArr) {
        InterfaceC0837b interfaceC0837bM4547a = C0836a.m4547a();
        try {
            interfaceC0837bM4547a.mo4569c();
            Iterator it = this.f4151a.iterator();
            while (it.hasNext()) {
                interfaceC0837bM4547a.mo4560a(this.f4152b.f4150a.f4122e, (AbstractC0908f) it.next());
            }
            interfaceC0837bM4547a.mo4573d();
            return null;
        } finally {
            interfaceC0837bM4547a.mo4574e();
        }
    }
}
