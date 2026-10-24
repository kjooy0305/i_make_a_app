package com.tweber.stickfighter.p032b;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import com.tweber.stickfighter.activities.EditCustomFigureActivity;
import com.tweber.stickfighter.p038h.C0924v;

/* JADX INFO: renamed from: com.tweber.stickfighter.b.f */
/* JADX INFO: loaded from: classes.dex */
class ViewOnClickListenerC0814f implements View.OnClickListener {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ C0924v f3835a;

    /* JADX INFO: renamed from: b */
    final /* synthetic */ C0813e f3836b;

    ViewOnClickListenerC0814f(C0813e c0813e, C0924v c0924v) {
        this.f3836b = c0813e;
        this.f3835a = c0924v;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        Activity activity = (Activity) view.getContext();
        Intent intent = new Intent(activity, (Class<?>) EditCustomFigureActivity.class);
        intent.putExtra("CustomFigureID", this.f3835a.m4935b());
        activity.startActivityForResult(intent, 102);
    }
}
