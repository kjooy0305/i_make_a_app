package com.tweber.stickfighter.p032b;

import android.view.View;
import android.widget.Toast;
import com.tweber.stickfighter.activities.R;

/* JADX INFO: renamed from: com.tweber.stickfighter.b.c */
/* JADX INFO: loaded from: classes.dex */
class ViewOnClickListenerC0811c implements View.OnClickListener {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ C0809a f3831a;

    ViewOnClickListenerC0811c(C0809a c0809a) {
        this.f3831a = c0809a;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        Toast.makeText(view.getContext(), R.string.action_mode_modify_stretch, 1).show();
    }
}
