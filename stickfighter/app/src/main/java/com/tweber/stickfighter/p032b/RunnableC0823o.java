package com.tweber.stickfighter.p032b;

/* JADX INFO: renamed from: com.tweber.stickfighter.b.o */
/* JADX INFO: loaded from: classes.dex */
class RunnableC0823o implements Runnable {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ int f3856a;

    /* JADX INFO: renamed from: b */
    final /* synthetic */ C0819k f3857b;

    RunnableC0823o(C0819k c0819k, int i) {
        this.f3857b = c0819k;
        this.f3856a = i;
    }

    @Override // java.lang.Runnable
    public void run() {
        this.f3857b.notifyItemRangeChanged(0, this.f3857b.f3848a.m4939f().m4538a() - this.f3856a);
    }
}
