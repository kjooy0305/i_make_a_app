package com.tweber.stickfighter.p032b;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import com.tweber.stickfighter.activities.SequenceDetailsActivity;
import com.tweber.stickfighter.p038h.C0924v;

/* JADX INFO: renamed from: com.tweber.stickfighter.b.s */
/* JADX INFO: loaded from: classes.dex */
class ViewOnClickListenerC0827s implements View.OnClickListener {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ C0924v f3867a;

    /* JADX INFO: renamed from: b */
    final /* synthetic */ C0826r f3868b;

    ViewOnClickListenerC0827s(C0826r c0826r, C0924v c0924v) {
        this.f3868b = c0826r;
        this.f3867a = c0924v;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        Activity activity = (Activity) view.getContext();
        Intent intent = new Intent(activity, (Class<?>) SequenceDetailsActivity.class);
        intent.putExtra("SequenceId", this.f3867a.m4935b());
        activity.startActivityForResult(intent, 99);
    }
}
