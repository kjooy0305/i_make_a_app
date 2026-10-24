package com.tweber.stickfighter.p037g;

import android.view.MotionEvent;
import android.view.View;

/* JADX INFO: loaded from: classes.dex */
class ag implements View.OnTouchListener {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ ae f4056a;

    ag(ae aeVar) {
        this.f4056a = aeVar;
    }

    @Override // android.view.View.OnTouchListener
    public boolean onTouch(View view, MotionEvent motionEvent) {
        this.f4056a.m4729h();
        if (this.f4056a.f4051w.getVisibility() != 0) {
            return false;
        }
        this.f4056a.f4048t.removeCallbacksAndMessages(null);
        this.f4056a.f4048t.postDelayed(new ah(this), 3000L);
        return false;
    }
}
