package com.tweber.stickfighter.p032b;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import com.tweber.stickfighter.activities.EditFrameActivity;
import com.tweber.stickfighter.p036f.C0866b;
import com.tweber.stickfighter.p036f.EnumC0867c;

/* JADX INFO: renamed from: com.tweber.stickfighter.b.l */
/* JADX INFO: loaded from: classes.dex */
class ViewOnClickListenerC0820l implements View.OnClickListener {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ C0825q f3851a;

    /* JADX INFO: renamed from: b */
    final /* synthetic */ C0819k f3852b;

    ViewOnClickListenerC0820l(C0819k c0819k, C0825q c0825q) {
        this.f3852b = c0819k;
        this.f3851a = c0825q;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        de.greenrobot.event.EventBus.getDefault().post(new C0866b(EnumC0867c.FRAME_DRAW));
        Intent intent = new Intent(view.getContext(), (Class<?>) EditFrameActivity.class);
        intent.putExtra("Position", (this.f3852b.f3848a.m4939f().m4538a() - this.f3851a.getAdapterPosition()) - 1);
        ((Activity) view.getContext()).startActivityForResult(intent, 1);
    }
}
