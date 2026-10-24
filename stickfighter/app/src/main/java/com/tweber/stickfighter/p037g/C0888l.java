package com.tweber.stickfighter.p037g;

import android.view.View;
import android.widget.AdapterView;

/* JADX INFO: renamed from: com.tweber.stickfighter.g.l */
/* JADX INFO: loaded from: classes.dex */
class C0888l implements AdapterView.OnItemSelectedListener {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ C0887k f4144a;

    C0888l(C0887k c0887k) {
        this.f4144a = c0887k;
    }

    @Override // android.widget.AdapterView.OnItemSelectedListener
    public void onItemSelected(AdapterView adapterView, View view, int i, long j) {
        this.f4144a.m4810b(i);
        this.f4144a.m4815d();
    }

    @Override // android.widget.AdapterView.OnItemSelectedListener
    public void onNothingSelected(AdapterView adapterView) {
    }
}
