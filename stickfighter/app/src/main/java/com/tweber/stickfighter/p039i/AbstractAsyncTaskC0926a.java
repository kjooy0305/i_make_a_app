package com.tweber.stickfighter.p039i;

import com.tweber.stickfighter.p036f.C0866b;
import com.tweber.stickfighter.p036f.EnumC0867c;

/* JADX INFO: renamed from: com.tweber.stickfighter.i.a */
/* JADX INFO: loaded from: classes.dex */
public abstract class AbstractAsyncTaskC0926a extends AbstractAsyncTaskC0931f {
    /* JADX INFO: renamed from: a */
    protected abstract EnumC0867c mo4940a();

    /* JADX INFO: renamed from: b */
    protected boolean m4941b() {
        return false;
    }

    @Override // android.os.AsyncTask
    protected final void onCancelled() {
        de.greenrobot.event.EventBus.getDefault().unregister(this);
    }

    public final void onEvent(C0866b c0866b) {
        if (c0866b.m4667a() == mo4940a()) {
            cancel(!m4941b());
        }
    }

    @Override // com.tweber.stickfighter.p039i.AbstractAsyncTaskC0931f, android.os.AsyncTask
    protected final void onPostExecute(Object obj) {
        de.greenrobot.event.EventBus.getDefault().post(obj);
        de.greenrobot.event.EventBus.getDefault().unregister(this);
    }

    @Override // android.os.AsyncTask
    protected final void onPreExecute() {
        de.greenrobot.event.EventBus.getDefault().register(this);
    }
}
