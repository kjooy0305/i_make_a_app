package com.tweber.stickfighter.p037g;

import com.google.android.material.snackbar.Snackbar;
import android.view.View;
import com.tweber.stickfighter.activities.R;
import com.tweber.stickfighter.p038h.C0924v;

/* JADX INFO: loaded from: classes.dex */
class ac implements View.OnClickListener {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ aa f4009a;

    ac(aa aaVar) {
        this.f4009a = aaVar;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        if (C0924v.m4932a().m4939f().m4538a() < 2) {
            Snackbar.make(view, this.f4009a.getString(R.string.minimum_frames_to_create_video), 0).show();
        } else {
            this.f4009a.m4678b();
        }
    }
}
