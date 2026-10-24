package com.tweber.stickfighter.p038h;

import android.view.MotionEvent;

/* JADX INFO: renamed from: com.tweber.stickfighter.h.a */
/* JADX INFO: loaded from: classes.dex */
public abstract class AbstractC0903a {
    /* JADX INFO: renamed from: a */
    protected abstract void mo4792a(float f, float f2);

    /* JADX INFO: renamed from: a */
    public void m4838a(MotionEvent motionEvent) {
        float x = motionEvent.getX();
        float y = motionEvent.getY();
        switch (motionEvent.getAction()) {
            case 0:
                mo4792a(x, y);
                break;
            case 1:
                mo4794c(x, y);
                break;
            case 2:
                mo4793b(x, y);
                break;
        }
    }

    /* JADX INFO: renamed from: b */
    protected abstract void mo4793b(float f, float f2);

    /* JADX INFO: renamed from: c */
    protected abstract void mo4794c(float f, float f2);
}
