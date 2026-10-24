package com.tweber.stickfighter.p037g;

/* JADX INFO: loaded from: classes.dex */
class ak implements Runnable {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ aj f4060a;

    ak(aj ajVar) {
        this.f4060a = ajVar;
    }

    @Override // java.lang.Runnable
    public void run() {
        if (this.f4060a.f4059a.f4024K && (this.f4060a.f4059a.getActivity().getWindow().getDecorView().getSystemUiVisibility() & 4) == 0) {
            this.f4060a.f4059a.m4726f();
        }
    }
}
