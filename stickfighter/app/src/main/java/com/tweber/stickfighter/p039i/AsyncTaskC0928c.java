package com.tweber.stickfighter.p039i;

import com.tweber.stickfighter.p034d.C0836a;
import com.tweber.stickfighter.p036f.C0870f;
import com.tweber.stickfighter.p038h.AbstractC0908f;
import com.tweber.stickfighter.p038h.C0905c;
import com.tweber.stickfighter.p038h.C0918p;
import com.tweber.stickfighter.p038h.C0924v;
import java.util.Iterator;

/* JADX INFO: renamed from: com.tweber.stickfighter.i.c */
/* JADX INFO: loaded from: classes.dex */
public class AsyncTaskC0928c extends AbstractAsyncTaskC0931f {

    /* JADX INFO: renamed from: a */
    final C0918p f4256a;

    public AsyncTaskC0928c(C0918p c0918p) {
        this.f4256a = c0918p;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.tweber.stickfighter.p039i.AbstractAsyncTaskC0931f
    /* JADX INFO: renamed from: a, reason: merged with bridge method [inline-methods] */
    public C0870f mo4943d() {
        C0836a.m4547a().mo4559a(this.f4256a);
        if (C0924v.m4932a().m4939f().m4539a(this.f4256a.m4927b() + 1) == null) {
            C0924v.m4932a().m4939f().m4541a(C0836a.m4547a().mo4565b(this.f4256a));
        }
        this.f4256a.m4929d();
        Iterator it = this.f4256a.m4922a().iterator();
        while (it.hasNext()) {
            Iterator it2 = ((AbstractC0908f) it.next()).m4872a().iterator();
            while (it2.hasNext()) {
                ((C0905c) it2.next()).m4870i();
            }
        }
        System.gc();
        return new C0870f(this.f4256a);
    }
}
