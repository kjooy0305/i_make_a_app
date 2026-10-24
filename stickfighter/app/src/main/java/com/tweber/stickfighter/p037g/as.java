package com.tweber.stickfighter.p037g;

import android.view.View;
import android.widget.Toast;
import com.tweber.stickfighter.activities.R;

/* JADX INFO: loaded from: classes.dex */
class as implements View.OnClickListener {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ ae f4068a;

    as(ae aeVar) {
        this.f4068a = aeVar;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        if (this.f4068a.f4027N == null || this.f4068a.f4027N.isEmpty()) {
            Toast.makeText(view.getContext(), R.string.play_loading_frames, 0).show();
        } else {
            this.f4068a.m4715c();
        }
    }
}
