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

/* JADX INFO: renamed from: com.tweber.stickfighter.b.v */
/* JADX INFO: loaded from: classes.dex */
class AsyncTaskC0830v extends AsyncTask<Void, Void, Bitmap> {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ C0826r f3873a;

    /* JADX INFO: renamed from: b */
    C0924v f3874b;

    /* JADX INFO: renamed from: c */
    ImageView f3875c;

    /* JADX INFO: renamed from: d */
    Bitmap f3876d;

    public AsyncTaskC0830v(C0826r c0826r, C0924v c0924v, ImageView imageView) {
        this.f3873a = c0826r;
        this.f3874b = c0924v;
        this.f3875c = imageView;
        int width = this.f3875c.getWidth();
        int iM4937d = (int) (width * this.f3874b.m4937d());
        if (width == 0 || iM4937d == 0) {
            width = C0935d.m4962c((Activity) this.f3875c.getContext()) / c0826r.f3865b;
            iM4937d = (int) (width * this.f3874b.m4937d());
        }
        this.f3876d = Bitmap.createBitmap(width, iM4937d, Bitmap.Config.RGB_565);
        new Canvas(this.f3876d).drawColor(-1);
        this.f3875c.setImageBitmap(this.f3876d);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.os.AsyncTask
    /* JADX INFO: renamed from: a, reason: merged with bridge method [inline-methods] */
    public Bitmap doInBackground(Void... voidArr) {
        C0918p c0918pM4539a = this.f3874b.m4939f().m4539a(0);
        Canvas canvas = new Canvas(this.f3876d);
        if (c0918pM4539a != null) {
            canvas.drawColor(-3355444);
            c0918pM4539a.m4926a(new C0912j(this.f3874b.m4937d(), canvas), canvas, false, null, true, this.f3874b.m4938e(), false);
        }
        return this.f3876d;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.os.AsyncTask
    /* JADX INFO: renamed from: a, reason: merged with bridge method [inline-methods] */
    public void onPostExecute(Bitmap bitmap) {
        this.f3875c.setImageBitmap(bitmap);
    }
}
