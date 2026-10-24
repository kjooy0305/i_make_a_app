package com.tweber.stickfighter.p037g;

import android.graphics.Canvas;
import android.os.Message;
import android.view.TextureView;
import com.tweber.stickfighter.p038h.InterfaceC0913k;
import java.util.ArrayList;

/* JADX INFO: loaded from: classes.dex */
class ba extends Thread {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ ae f4079a;

    /* JADX INFO: renamed from: b */
    final TextureView f4080b;

    /* JADX INFO: renamed from: c */
    final int f4081c;

    /* JADX INFO: renamed from: d */
    volatile boolean f4082d = true;

    public ba(ae aeVar, TextureView textureView) {
        this.f4079a = aeVar;
        this.f4081c = (int) (1000.0f / this.f4079a.f4037i);
        this.f4080b = textureView;
    }

    /* JADX INFO: renamed from: b */
    private void m4755b() {
        ArrayList arrayList = this.f4079a.f4027N;
        this.f4079a.f4027N = this.f4079a.f4028O;
        this.f4079a.f4028O = arrayList;
    }

    /* JADX INFO: renamed from: c */
    private void m4756c() {
        ae.m4692E(this.f4079a);
        this.f4079a.f4026M = 0;
        int iCeil = ((int) Math.ceil((this.f4079a.f4041m - 1.0f) / this.f4079a.f4038j)) - 1;
        if (this.f4079a.f4025L > iCeil && this.f4079a.f4039k) {
            this.f4079a.f4025L = 0;
        }
        m4755b();
        if (this.f4079a.f4025L == iCeil && this.f4079a.f4039k) {
            new ay(this.f4079a, 0, this.f4079a.f4028O).execute(new Void[0]);
        } else {
            new ay(this.f4079a, (this.f4079a.f4025L + 1) * this.f4079a.f4038j, this.f4079a.f4028O).execute(new Void[0]);
        }
    }

    /* JADX INFO: renamed from: a */
    public void m4757a() {
        interrupt();
        this.f4082d = false;
    }

    @Override // java.lang.Thread, java.lang.Runnable
    public void run() {
        while (this.f4082d && !isInterrupted()) {
            long jCurrentTimeMillis = System.currentTimeMillis();
            if (this.f4079a.f4026M == this.f4079a.f4027N.size()) {
                synchronized (this.f4079a.f4028O) {
                    m4756c();
                }
                if (this.f4079a.f4027N.isEmpty()) {
                    this.f4079a.f4025L = 0;
                    Message message = new Message();
                    message.obj = true;
                    this.f4079a.f4047s.sendMessage(message);
                    return;
                }
                Message message2 = new Message();
                message2.obj = Integer.valueOf(this.f4079a.f4025L);
                this.f4079a.f4046r.sendMessage(message2);
            }
            InterfaceC0913k interfaceC0913k = (InterfaceC0913k) this.f4079a.f4027N.get(this.f4079a.f4026M);
            Canvas canvasLockCanvas = this.f4080b.lockCanvas(null);
            try {
                this.f4079a.m4703a(canvasLockCanvas, interfaceC0913k);
                Thread.sleep(Math.max(0L, ((long) this.f4081c) - (System.currentTimeMillis() - jCurrentTimeMillis)));
            } catch (InterruptedException e) {
            } finally {
                this.f4080b.unlockCanvasAndPost(canvasLockCanvas);
            }
            ae.m4696I(this.f4079a);
        }
    }
}
