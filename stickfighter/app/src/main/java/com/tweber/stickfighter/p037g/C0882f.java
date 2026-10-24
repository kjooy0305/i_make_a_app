package com.tweber.stickfighter.p037g;

import android.view.View;
import android.widget.AdapterView;

/* JADX INFO: renamed from: com.tweber.stickfighter.g.f */
/* JADX INFO: loaded from: classes.dex */
class C0882f implements AdapterView.OnItemSelectedListener {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ C0881e f4110a;

    C0882f(C0881e c0881e) {
        this.f4110a = c0881e;
    }

    @Override // android.widget.AdapterView.OnItemSelectedListener
    public void onItemSelected(AdapterView adapterView, View view, int i, long j) {
        this.f4110a.m4773b(i);
        this.f4110a.m4772b();
    }

    @Override // android.widget.AdapterView.OnItemSelectedListener
    public void onNothingSelected(AdapterView adapterView) {
    }
}
