package com.tweber.stickfighter.p037g;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;
import com.tweber.stickfighter.activities.R;
import com.tweber.stickfighter.p038h.C0905c;
import com.tweber.stickfighter.p038h.C0912j;
import com.tweber.stickfighter.p038h.C0924v;

/* JADX INFO: renamed from: com.tweber.stickfighter.g.j */
/* JADX INFO: loaded from: classes.dex */
class ViewOnTouchListenerC0886j extends View implements View.OnTouchListener {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ C0881e f4114a;

    /* JADX INFO: renamed from: b */
    final int f4115b;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public ViewOnTouchListenerC0886j(C0881e c0881e, Context context) {
        super(context);
        this.f4114a = c0881e;
        this.f4115b = context.getResources().getDimensionPixelSize(R.dimen.selected_circle_radius);
        m4800a();
    }

    /* JADX INFO: renamed from: a */
    private void m4800a() {
        setOnTouchListener(this);
    }

    @Override // android.view.View
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (this.f4114a.f4095h == null) {
            this.f4114a.f4095h = new C0912j(C0924v.m4932a().m4937d(), this.f4114a.f4096i);
        }
        this.f4114a.f4093f.m4925a(this.f4114a.f4095h, canvas, true, null, C0924v.m4932a().m4938e(), false);
        if (this.f4114a.f4100m.f4253d != null) {
            C0905c c0905c = (C0905c) this.f4114a.f4100m.f4253d;
            float fM4887a = this.f4114a.f4095h.m4887a(c0905c.m4863c());
            float fM4889b = this.f4114a.f4095h.m4889b(c0905c.m4865d());
            canvas.drawCircle(fM4887a, fM4889b, this.f4115b, this.f4114a.f4095h.f4186a.f4284a);
            canvas.drawCircle(fM4887a, fM4889b, this.f4115b, this.f4114a.f4095h.f4186a.f4285b);
        }
    }

    @Override // android.view.View.OnTouchListener
    public boolean onTouch(View view, MotionEvent motionEvent) {
        this.f4114a.f4094g.m4838a(motionEvent);
        return true;
    }
}
