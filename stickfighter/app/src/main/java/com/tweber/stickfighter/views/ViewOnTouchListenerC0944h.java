package com.tweber.stickfighter.views;

import android.view.MotionEvent;
import android.view.View;
import com.tweber.stickfighter.activities.R;

/* JADX INFO: renamed from: com.tweber.stickfighter.views.h */
/* JADX INFO: loaded from: classes.dex */
class ViewOnTouchListenerC0944h implements View.OnTouchListener {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ EditorToolbar f4280a;

    ViewOnTouchListenerC0944h(EditorToolbar editorToolbar) {
        this.f4280a = editorToolbar;
    }

    @Override // android.view.View.OnTouchListener
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (this.f4280a.f4272l == null) {
            return false;
        }
        switch (motionEvent.getAction()) {
            case 0:
                this.f4280a.f4266f.setImageResource(R.drawable.ic_action_drag_closed_white24dp);
                break;
            case 1:
            case 3:
                this.f4280a.f4266f.setImageResource(R.drawable.ic_action_drag_open_white24dp);
                break;
        }
        return this.f4280a.f4272l.mo4786a(motionEvent);
    }
}
