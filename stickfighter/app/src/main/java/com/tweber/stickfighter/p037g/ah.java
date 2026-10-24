package com.tweber.stickfighter.p037g;

/* JADX INFO: loaded from: classes.dex */
class ah implements Runnable {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ ag f4057a;

    ah(ag agVar) {
        this.f4057a = agVar;
    }

    @Override // java.lang.Runnable
    public void run() {
        if (this.f4057a.f4056a.f4024K && (this.f4057a.f4056a.getActivity().getWindow().getDecorView().getSystemUiVisibility() & 4) == 0) {
            this.f4057a.f4056a.m4726f();
        }
    }
}
