package com.tweber.stickfighter.p032b;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.widget.ImageView;
import com.tweber.stickfighter.p036f.C0866b;
import com.tweber.stickfighter.p036f.EnumC0867c;
import com.tweber.stickfighter.p038h.C0912j;
import com.tweber.stickfighter.p038h.C0918p;
import com.tweber.stickfighter.p038h.C0924v;
import com.tweber.stickfighter.p040j.C0935d;

/* JADX INFO: renamed from: com.tweber.stickfighter.b.p */
/* JADX INFO: loaded from: classes.dex */
class AsyncTaskC0824p extends AsyncTask<Void, Void, Bitmap> {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ C0819k f3858a;

    /* JADX INFO: renamed from: b */
    C0924v f3859b;

    /* JADX INFO: renamed from: c */
    int f3860c;

    /* JADX INFO: renamed from: d */
    ImageView f3861d;

    /* JADX INFO: renamed from: e */
    Context f3862e;

    public AsyncTaskC0824p(C0819k c0819k, C0924v c0924v, int i, ImageView imageView) {
        this.f3858a = c0819k;
        this.f3859b = c0924v;
        this.f3860c = i;
        this.f3861d = imageView;
        this.f3862e = imageView.getContext();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.os.AsyncTask
    /* JADX INFO: renamed from: a, reason: merged with bridge method [inline-methods] */
    public Bitmap doInBackground(Void... voidArr) {
        if (isCancelled()) {
            return null;
        }
        C0918p c0918pM4539a = this.f3859b.m4939f().m4539a(this.f3860c);
        int iM4962c = C0935d.m4962c((Activity) this.f3862e) / this.f3858a.f3849b;
        int iM4937d = (int) (iM4962c * this.f3859b.m4937d());
        if (isCancelled()) {
            return null;
        }
        Bitmap bitmapCreateBitmap = Bitmap.createBitmap(iM4962c, iM4937d, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmapCreateBitmap);
        canvas.drawColor(-3355444);
        c0918pM4539a.m4926a(new C0912j(this.f3859b.m4937d(), canvas), canvas, false, null, true, this.f3859b.m4938e(), true);
        return bitmapCreateBitmap;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.os.AsyncTask
    /* JADX INFO: renamed from: a, reason: merged with bridge method [inline-methods] */
    public void onPostExecute(Bitmap bitmap) {
        de.greenrobot.event.EventBus.getDefault().unregister(this);
        if (this.f3861d == null || bitmap == null) {
            return;
        }
        this.f3861d.setImageBitmap(bitmap);
    }

    @Override // android.os.AsyncTask
    protected void onCancelled() {
        de.greenrobot.event.EventBus.getDefault().unregister(this);
    }

    public void onEvent(C0866b c0866b) {
        if (c0866b.m4667a() == EnumC0867c.FRAME_DRAW) {
            cancel(false);
        }
    }

    @Override // android.os.AsyncTask
    protected void onPreExecute() {
        de.greenrobot.event.EventBus.getDefault().register(this);
    }
}
