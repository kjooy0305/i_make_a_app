package com.tweber.stickfighter.p035e;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import com.tweber.stickfighter.activities.R;

/* JADX INFO: renamed from: com.tweber.stickfighter.e.s */
/* JADX INFO: loaded from: classes.dex */
class ViewOnClickListenerC0857s implements View.OnClickListener {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ C0855q f3971a;

    ViewOnClickListenerC0857s(C0855q c0855q) {
        this.f3971a = c0855q;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.SEND");
        intent.putExtra("android.intent.extra.STREAM", com.tweber.stickfighter.util.FileShare.uri(this.f3971a.f3968d));
        intent.addFlags(1);
        intent.setType("xml/sfa");
        this.f3971a.startActivity(Intent.createChooser(intent, this.f3971a.getResources().getText(R.string.share)));
    }
}
