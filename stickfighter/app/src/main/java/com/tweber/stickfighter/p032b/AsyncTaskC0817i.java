package com.tweber.stickfighter.p032b;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.widget.ImageView;
import com.tweber.stickfighter.p038h.C0912j;
import com.tweber.stickfighter.p038h.C0918p;
import com.tweber.stickfighter.p038h.C0924v;
import com.tweber.stickfighter.p040j.C0935d;

/* JADX INFO: renamed from: com.tweber.stickfighter.b.i */
/* JADX INFO: loaded from: classes.dex */
class AsyncTaskC0817i extends AsyncTask<Void, Void, Bitmap> {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ C0813e f3841a;

    /* JADX INFO: renamed from: b */
    C0924v f3842b;

    /* JADX INFO: renamed from: c */
    ImageView f3843c;

    /* JADX INFO: renamed from: d */
    Bitmap f3844d;

    public AsyncTaskC0817i(C0813e c0813e, C0924v c0924v, ImageView imageView) {
        this.f3841a = c0813e;
        this.f3842b = c0924v;
        this.f3843c = imageView;
        int width = this.f3843c.getWidth();
        int iM4937d = (int) (width * this.f3842b.m4937d());
        if (width == 0 || iM4937d == 0) {
            width = C0935d.m4962c((Activity) this.f3843c.getContext()) / c0813e.f3834b;
            iM4937d = (int) (width * this.f3842b.m4937d());
        }
        this.f3844d = Bitmap.createBitmap(width, iM4937d, Bitmap.Config.RGB_565);
        new Canvas(this.f3844d).drawColor(-1);
        this.f3843c.setImageBitmap(this.f3844d);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.os.AsyncTask
    /* JADX INFO: renamed from: a, reason: merged with bridge method [inline-methods] */
    public Bitmap doInBackground(Void... voidArr) {
        C0918p c0918pM4539a = this.f3842b.m4939f().m4539a(0);
        Canvas canvas = new Canvas(this.f3844d);
        canvas.drawColor(-3355444);
        c0918pM4539a.m4926a(new C0912j(this.f3842b.m4937d(), canvas), canvas, false, null, true, this.f3842b.m4938e(), false);
        return this.f3844d;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.os.AsyncTask
    /* JADX INFO: renamed from: a, reason: merged with bridge method [inline-methods] */
    public void onPostExecute(Bitmap bitmap) {
        this.f3843c.setImageBitmap(bitmap);
    }
}
