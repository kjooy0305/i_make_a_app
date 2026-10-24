package com.tweber.stickfighter.p035e;

import com.google.android.material.snackbar.Snackbar;
import android.view.View;
import com.tweber.stickfighter.activities.R;

/* JADX INFO: loaded from: classes.dex */
class ak implements View.OnClickListener {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ aj f3922a;

    ak(aj ajVar) {
        this.f3922a = ajVar;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        Snackbar.make(view, R.string.no_custom_figures, 0).show();
    }
}
