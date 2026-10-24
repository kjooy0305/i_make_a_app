package com.tweber.stickfighter.p037g;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;
import com.tweber.stickfighter.activities.R;
import com.tweber.stickfighter.p038h.C0905c;
import com.tweber.stickfighter.p038h.C0912j;
import com.tweber.stickfighter.p038h.C0924v;

/* JADX INFO: renamed from: com.tweber.stickfighter.g.u */
/* JADX INFO: loaded from: classes.dex */
class ViewOnTouchListenerC0897u extends View implements View.OnTouchListener {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ C0887k f4154a;

    /* JADX INFO: renamed from: b */
    final int f4155b;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public ViewOnTouchListenerC0897u(C0887k c0887k, Context context) {
        super(context);
        this.f4154a = c0887k;
        this.f4155b = context.getResources().getDimensionPixelSize(R.dimen.selected_circle_radius);
        m4830a();
    }

    /* JADX INFO: renamed from: a */
    private void m4830a() {
        setOnTouchListener(this);
    }

    @Override // android.view.View
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (this.f4154a.f4124g == null) {
            this.f4154a.f4124g = new C0912j(C0924v.m4932a().m4937d(), this.f4154a.f4125h);
        }
        this.f4154a.f4122e.m4925a(this.f4154a.f4124g, canvas, true, this.f4154a.f4122e.m4927b() > 0 ? C0924v.m4932a().m4939f().m4539a(this.f4154a.f4122e.m4927b() - 1) : null, C0924v.m4932a().m4938e(), true);
        if (this.f4154a.f4129l.f4253d != null) {
            C0905c c0905c = (C0905c) this.f4154a.f4129l.f4253d;
            float fM4887a = this.f4154a.f4124g.m4887a(c0905c.m4863c());
            float fM4889b = this.f4154a.f4124g.m4889b(c0905c.m4865d());
            canvas.drawCircle(fM4887a, fM4889b, this.f4155b, this.f4154a.f4124g.f4186a.f4284a);
            canvas.drawCircle(fM4887a, fM4889b, this.f4155b, this.f4154a.f4124g.f4186a.f4285b);
        }
    }

    @Override // android.view.View.OnTouchListener
    public boolean onTouch(View view, MotionEvent motionEvent) {
        this.f4154a.f4123f.m4838a(motionEvent);
        return true;
    }
}
