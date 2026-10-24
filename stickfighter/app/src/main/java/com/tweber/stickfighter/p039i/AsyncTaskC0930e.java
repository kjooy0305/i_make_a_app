package com.tweber.stickfighter.p039i;

import android.util.Log;
import com.tweber.stickfighter.p034d.C0836a;
import com.tweber.stickfighter.p036f.C0869e;
import com.tweber.stickfighter.p038h.AbstractC0908f;
import com.tweber.stickfighter.p038h.C0905c;
import com.tweber.stickfighter.p038h.C0918p;
import java.util.Iterator;

/* JADX INFO: renamed from: com.tweber.stickfighter.i.e */
/* JADX INFO: loaded from: classes.dex */
public class AsyncTaskC0930e extends AbstractAsyncTaskC0931f {

    /* JADX INFO: renamed from: a */
    final C0918p f4258a;

    public AsyncTaskC0930e(C0918p c0918p) {
        this.f4258a = c0918p;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.tweber.stickfighter.p039i.AbstractAsyncTaskC0931f
    /* JADX INFO: renamed from: a, reason: merged with bridge method [inline-methods] */
    public C0869e mo4943d() {
        try {
            C0836a.m4547a().mo4559a(this.f4258a);
        } catch (Exception e) {
            Log.e("HELLO", "FAILED SaveFrameTask: " + e.getMessage());
        }
        this.f4258a.m4929d();
        Iterator it = this.f4258a.m4922a().iterator();
        while (it.hasNext()) {
            Iterator it2 = ((AbstractC0908f) it.next()).m4872a().iterator();
            while (it2.hasNext()) {
                ((C0905c) it2.next()).m4870i();
            }
        }
        System.gc();
        return new C0869e(this.f4258a);
    }
}
