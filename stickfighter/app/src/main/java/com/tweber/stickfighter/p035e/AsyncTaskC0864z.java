package com.tweber.stickfighter.p035e;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.tweber.stickfighter.activities.R;
import com.tweber.stickfighter.p034d.C0836a;
import com.tweber.stickfighter.p034d.InterfaceC0837b;
import com.tweber.stickfighter.p036f.C0875k;
import com.tweber.stickfighter.p038h.C0924v;
import com.tweber.stickfighter.StickFighterApplication;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipInputStream;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/* JADX INFO: renamed from: com.tweber.stickfighter.e.z */
/* JADX INFO: loaded from: classes.dex */
class AsyncTaskC0864z extends AsyncTask<Void, Integer, C0924v> {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ C0860v f3983a;

    /* JADX INFO: renamed from: b */
    Uri f3984b;

    /* JADX INFO: renamed from: c */
    InterfaceC0837b f3985c = C0836a.m4547a();

    public AsyncTaskC0864z(C0860v c0860v, Uri uri) {
        this.f3983a = c0860v;
        this.f3984b = uri;
    }

    void publish(Integer... numArr) {
        publishProgress(numArr);
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX INFO: renamed from: a */
    private C0924v m4659a(Uri uri) throws Exception {
        C0924v c0924vM4588a = null;
        Object[] objArr = null;
        ZipInputStream zipInputStream = new ZipInputStream(StickFighterApplication.m4490a().getContentResolver().openInputStream(uri));
        if (zipInputStream.getNextEntry() != null) {
            this.f3985c.mo4569c();
            aa aaVar = new aa(this, null);
            try {
                XMLReader xMLReader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
                xMLReader.setContentHandler(aaVar);
                xMLReader.parse(new InputSource(zipInputStream));
                this.f3985c.mo4573d();
                c0924vM4588a = aaVar.m4588a();
                this.f3985c.mo4574e();
            } catch (Throwable th) {
                this.f3985c.mo4574e();
                throw th;
            }
        }
        zipInputStream.close();
        return c0924vM4588a;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.os.AsyncTask
    /* JADX INFO: renamed from: a, reason: merged with bridge method [inline-methods] */
    public C0924v doInBackground(Void... voidArr) {
        C0924v c0924vM4659a;
        Exception e;
        try {
            c0924vM4659a = m4659a(this.f3984b);
            try {
                this.f3983a.f3978d = true;
            } catch (Exception e2) {
                e = e2;
                Log.e("ERROR", e.getMessage());
            }
        } catch (Exception e3) {
            c0924vM4659a = null;
            e = e3;
        }
        return c0924vM4659a;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.os.AsyncTask
    /* JADX INFO: renamed from: a, reason: merged with bridge method [inline-methods] */
    public void onPostExecute(C0924v c0924v) {
        View view = this.f3983a.getView();
        view.findViewById(R.id.importingContainer).setVisibility(8);
        view.findViewById(R.id.finishedContainer).setVisibility(0);
        this.f3983a.f3979e = c0924v;
        ((TextView) view.findViewById(R.id.finishedLabelTextView)).setText(c0924v.m4936c());
        de.greenrobot.event.EventBus.getDefault().post(new C0875k(c0924v, true, null));
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.os.AsyncTask
    /* JADX INFO: renamed from: a, reason: merged with bridge method [inline-methods] */
    public void onProgressUpdate(Integer... numArr) {
        this.f3983a.f3975a.setProgress(numArr[0].intValue());
    }
}
