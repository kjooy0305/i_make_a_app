package com.tweber.stickfighter.p035e;

import android.view.View;
import com.tweber.stickfighter.p036f.C0871g;

/* JADX INFO: renamed from: com.tweber.stickfighter.e.y */
/* JADX INFO: loaded from: classes.dex */
class ViewOnClickListenerC0863y implements View.OnClickListener {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ C0860v f3982a;

    ViewOnClickListenerC0863y(C0860v c0860v) {
        this.f3982a = c0860v;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        this.f3982a.dismiss();
        de.greenrobot.event.EventBus.getDefault().post(new C0871g());
    }
}
