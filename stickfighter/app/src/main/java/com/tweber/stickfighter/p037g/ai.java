package com.tweber.stickfighter.p037g;

import android.graphics.Canvas;
import android.widget.SeekBar;
import com.tweber.stickfighter.p034d.C0836a;
import com.tweber.stickfighter.p038h.C0918p;

/* JADX INFO: loaded from: classes.dex */
class ai implements SeekBar.OnSeekBarChangeListener {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ ae f4058a;

    ai(ae aeVar) {
        this.f4058a = aeVar;
    }

    @Override // android.widget.SeekBar.OnSeekBarChangeListener
    public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
        this.f4058a.m4702a(i, false);
    }

    @Override // android.widget.SeekBar.OnSeekBarChangeListener
    public void onStartTrackingTouch(SeekBar seekBar) {
        this.f4058a.m4724e();
    }

    @Override // android.widget.SeekBar.OnSeekBarChangeListener
    public void onStopTrackingTouch(SeekBar seekBar) {
        int progress = seekBar.getProgress();
        if (progress == this.f4058a.f4025L) {
            return;
        }
        this.f4058a.f4025L = progress;
        this.f4058a.f4026M = 0;
        if (this.f4058a.f4039k) {
            int iCeil = ((int) Math.ceil((this.f4058a.f4041m - 1.0f) / this.f4058a.f4038j)) - 1;
            if (this.f4058a.f4025L > iCeil) {
                this.f4058a.f4025L = 0;
                new ay(this.f4058a, 0, this.f4058a.f4027N).execute(new Void[0]);
                new ay(this.f4058a, this.f4058a.f4038j, this.f4058a.f4028O).execute(new Void[0]);
            } else if (this.f4058a.f4025L == iCeil) {
                new ay(this.f4058a, this.f4058a.f4025L * this.f4058a.f4038j, this.f4058a.f4027N).execute(new Void[0]);
                new ay(this.f4058a, 0, this.f4058a.f4028O).execute(new Void[0]);
            } else {
                new ay(this.f4058a, this.f4058a.f4025L * this.f4058a.f4038j, this.f4058a.f4027N).execute(new Void[0]);
                new ay(this.f4058a, (this.f4058a.f4025L + 1) * this.f4058a.f4038j, this.f4058a.f4028O).execute(new Void[0]);
            }
        } else {
            new ay(this.f4058a, this.f4058a.f4025L * this.f4058a.f4038j, this.f4058a.f4027N).execute(new Void[0]);
            new ay(this.f4058a, (this.f4058a.f4025L + 1) * this.f4058a.f4038j, this.f4058a.f4028O).execute(new Void[0]);
        }
        C0918p c0918pMo4564b = C0836a.m4547a().mo4564b(this.f4058a.f4040l.m4935b(), this.f4058a.f4038j * progress);
        if (c0918pMo4564b == null) {
            c0918pMo4564b = C0836a.m4547a().mo4564b(this.f4058a.f4040l.m4935b(), (progress * this.f4058a.f4038j) - 1);
        }
        Canvas canvasLockCanvas = this.f4058a.f4043o.lockCanvas();
        this.f4058a.m4703a(canvasLockCanvas, c0918pMo4564b);
        this.f4058a.f4043o.unlockCanvasAndPost(canvasLockCanvas);
    }
}
