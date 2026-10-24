package com.tweber.stickfighter.p037g;

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.view.View;
import com.tweber.stickfighter.StickFighterApplication;

/* JADX INFO: loaded from: classes.dex */
class au implements View.OnClickListener {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ ae f4070a;

    au(ae aeVar) {
        this.f4070a = aeVar;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        int progress = this.f4070a.f4016C.getProgress();
        int progress2 = this.f4070a.f4018E.getProgress();
        boolean zIsChecked = this.f4070a.f4020G.isChecked();
        SharedPreferences.Editor editorEdit = StickFighterApplication.m4491b().edit();
        editorEdit.putInt("PlayerFPS", progress);
        editorEdit.putInt("PlayerKeyFPS", progress2);
        editorEdit.putBoolean("PlayerRepeat", zIsChecked);
        editorEdit.apply();
        boolean z = (this.f4070a.f4037i == this.f4070a.f4032d[progress] && this.f4070a.f4038j == this.f4070a.f4033e[progress2] && zIsChecked == this.f4070a.f4039k) ? false : true;
        this.f4070a.f4037i = this.f4070a.f4032d[progress];
        this.f4070a.f4038j = this.f4070a.f4033e[progress2];
        this.f4070a.f4039k = zIsChecked;
        if (z) {
            this.f4070a.f4025L = 0;
            this.f4070a.f4026M = 0;
            this.f4070a.m4710b();
            Canvas canvasLockCanvas = this.f4070a.f4043o.lockCanvas();
            this.f4070a.m4703a(canvasLockCanvas, this.f4070a.f4042n);
            this.f4070a.f4043o.unlockCanvasAndPost(canvasLockCanvas);
        }
        this.f4070a.m4729h();
    }
}
