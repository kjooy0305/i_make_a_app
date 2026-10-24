package com.tweber.stickfighter.p032b;

import android.view.View;
import android.widget.Toast;
import com.tweber.stickfighter.activities.R;

/* JADX INFO: renamed from: com.tweber.stickfighter.b.d */
/* JADX INFO: loaded from: classes.dex */
class ViewOnClickListenerC0812d implements View.OnClickListener {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ C0809a f3832a;

    ViewOnClickListenerC0812d(C0809a c0809a) {
        this.f3832a = c0809a;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        Toast.makeText(view.getContext(), R.string.action_mode_flip, 1).show();
    }
}
