package com.tweber.stickfighter.p039i;

import com.tweber.stickfighter.p034d.C0836a;
import com.tweber.stickfighter.p036f.C0872h;
import com.tweber.stickfighter.p038h.AbstractC0908f;
import com.tweber.stickfighter.p038h.C0905c;
import com.tweber.stickfighter.p038h.C0918p;
import java.util.Iterator;

/* JADX INFO: renamed from: com.tweber.stickfighter.i.d */
/* JADX INFO: loaded from: classes.dex */
public class AsyncTaskC0929d extends AbstractAsyncTaskC0931f {

    /* JADX INFO: renamed from: a */
    final C0918p f4257a;

    public AsyncTaskC0929d(C0918p c0918p) {
        this.f4257a = c0918p;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.tweber.stickfighter.p039i.AbstractAsyncTaskC0931f
    /* JADX INFO: renamed from: a, reason: merged with bridge method [inline-methods] */
    public C0872h mo4943d() {
        C0836a.m4547a().mo4559a(this.f4257a);
        this.f4257a.m4929d();
        Iterator it = this.f4257a.m4922a().iterator();
        while (it.hasNext()) {
            Iterator it2 = ((AbstractC0908f) it.next()).m4872a().iterator();
            while (it2.hasNext()) {
                ((C0905c) it2.next()).m4870i();
            }
        }
        System.gc();
        return new C0872h(this.f4257a);
    }
}
