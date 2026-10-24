package com.tweber.stickfighter.p037g;

import android.os.AsyncTask;
import com.tweber.stickfighter.p033c.C0834a;
import com.tweber.stickfighter.p038h.AbstractC0908f;
import com.tweber.stickfighter.p038h.C0905c;
import com.tweber.stickfighter.p038h.C0918p;
import com.tweber.stickfighter.p040j.C0935d;
import java.util.Iterator;

/* JADX INFO: loaded from: classes.dex */
class az extends AsyncTask<Void, Integer, Void> {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ ae f4077a;

    private az(ae aeVar) {
        this.f4077a = aeVar;
    }

    /* synthetic */ az(ae aeVar, af afVar) {
        this(aeVar);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.os.AsyncTask
    /* JADX INFO: renamed from: a, reason: merged with bridge method [inline-methods] */
    public Void doInBackground(Void... voidArr) {
        C0834a c0834aM4939f = this.f4077a.f4040l.m4939f();
        c0834aM4939f.m4543b(Integer.MAX_VALUE);
        int i = this.f4077a.f4041m;
        for (int i2 = 0; i2 < i && !isCancelled(); i2++) {
            C0918p c0918pM4539a = c0834aM4939f.m4539a(i2);
            try {
                c0918pM4539a.m4929d();
                Iterator it = c0918pM4539a.m4922a().iterator();
                while (it.hasNext()) {
                    Iterator it2 = ((AbstractC0908f) it.next()).m4872a().iterator();
                    while (it2.hasNext()) {
                        ((C0905c) it2.next()).m4870i();
                    }
                }
            } catch (Exception e) {
            }
            publishProgress(Integer.valueOf(i2 + 1));
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.os.AsyncTask
    /* JADX INFO: renamed from: a, reason: merged with bridge method [inline-methods] */
    public void onPostExecute(Void r3) {
        this.f4077a.f4036h = null;
        if (this.f4077a.getView() == null) {
            return;
        }
        C0935d.m4958a(this.f4077a.f4049u);
        this.f4077a.f4049u.setOnTouchListener(null);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.os.AsyncTask
    /* JADX INFO: renamed from: a, reason: merged with bridge method [inline-methods] */
    public void onProgressUpdate(Integer... numArr) {
        if (this.f4077a.getView() == null) {
            return;
        }
        this.f4077a.f4050v.setProgress(numArr[0].intValue());
    }

    @Override // android.os.AsyncTask
    protected void onPreExecute() {
        this.f4077a.f4050v.setMax(this.f4077a.f4041m);
    }
}
