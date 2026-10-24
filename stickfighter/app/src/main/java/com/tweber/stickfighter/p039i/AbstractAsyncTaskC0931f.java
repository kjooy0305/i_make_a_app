package com.tweber.stickfighter.p039i;

import android.os.AsyncTask;

/* JADX INFO: renamed from: com.tweber.stickfighter.i.f */
/* JADX INFO: loaded from: classes.dex */
public abstract class AbstractAsyncTaskC0931f extends AsyncTask<Void, Void, Object> {
    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.os.AsyncTask
    /* JADX INFO: renamed from: a, reason: merged with bridge method [inline-methods] */
    public final Object doInBackground(Void... voidArr) {
        return mo4943d();
    }

    /* JADX INFO: renamed from: d */
    protected abstract Object mo4943d();

    @Override // android.os.AsyncTask
    protected void onPostExecute(Object obj) {
        if (obj == null) {
            return;
        }
        de.greenrobot.event.EventBus.getDefault().post(obj);
    }
}
