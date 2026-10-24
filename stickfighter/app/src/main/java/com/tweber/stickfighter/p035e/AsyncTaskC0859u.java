package com.tweber.stickfighter.p035e;

import android.os.AsyncTask;
import android.os.Environment;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.tweber.stickfighter.activities.R;
import com.tweber.stickfighter.p034d.C0836a;
import com.tweber.stickfighter.p038h.C0924v;
import com.tweber.stickfighter.p040j.C0934c;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/* JADX INFO: renamed from: com.tweber.stickfighter.e.u */
/* JADX INFO: loaded from: classes.dex */
class AsyncTaskC0859u extends AsyncTask<Void, Integer, Void> {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ C0855q f3973a;

    /* JADX INFO: renamed from: b */
    final long f3974b;

    public AsyncTaskC0859u(C0855q c0855q, long j) {
        this.f3973a = c0855q;
        this.f3974b = j;
    }

    /* JADX INFO: renamed from: a */
    private void m4648a(C0924v c0924v) throws IOException {
        int iM4538a = c0924v.m4939f().m4538a();
        this.f3973a.f3967c = new File(C0934c.m4955a(com.tweber.stickfighter.StickFighterApplication.m4490a().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/", c0924v.m4936c() + "_backup", "Animation_backup", ".xml"));
        this.f3973a.f3967c.getParentFile().mkdirs();
        FileOutputStream fileOutputStream = new FileOutputStream(this.f3973a.f3967c);
        fileOutputStream.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>".getBytes());
        fileOutputStream.write("<Sequence Name=\"".getBytes());
        fileOutputStream.write(C0934c.m4954a(c0924v.m4936c()).getBytes());
        fileOutputStream.write("\" HeightWidthRatio=\"".getBytes());
        fileOutputStream.write(String.valueOf(c0924v.m4937d()).getBytes());
        fileOutputStream.write("\" FrameCount=\"".getBytes());
        fileOutputStream.write(String.valueOf(iM4538a).getBytes());
        fileOutputStream.write("\" BackgroundColor=\"".getBytes());
        fileOutputStream.write(String.valueOf(c0924v.m4938e()).getBytes());
        fileOutputStream.write("\">".getBytes());
        for (int i = 0; i < iM4538a; i++) {
            if (isCancelled()) {
                fileOutputStream.close();
                this.f3973a.f3967c.delete();
                return;
            } else {
                fileOutputStream.write(c0924v.m4939f().m4539a(i).m4930e().getBytes());
                publishProgress(Integer.valueOf((int) (((float) i / iM4538a) * 50.0f)));
            }
        }
        fileOutputStream.write("</Sequence>".getBytes());
        fileOutputStream.close();
    }

    /* JADX INFO: renamed from: a */
    private void m4649a(C0924v c0924v, File file) throws IOException {
        this.f3973a.f3968d = new File(C0934c.m4955a(com.tweber.stickfighter.StickFighterApplication.m4490a().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/", c0924v.m4936c() + "_backup", "Animation_backup", ".sfa"));
        this.f3973a.f3968d.getParentFile().mkdirs();
        long length = file.length();
        ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(this.f3973a.f3968d)));
        byte[] bArr = new byte[RecyclerView.ItemAnimator.FLAG_MOVED];
        BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file), RecyclerView.ItemAnimator.FLAG_MOVED);
        zipOutputStream.putNextEntry(new ZipEntry(file.getName()));
        long j = 0;
        while (true) {
            int i = bufferedInputStream.read(bArr, 0, RecyclerView.ItemAnimator.FLAG_MOVED);
            if (i == -1) {
                bufferedInputStream.close();
                zipOutputStream.close();
                this.f3973a.f3967c.delete();
                return;
            } else {
                if (isCancelled()) {
                    bufferedInputStream.close();
                    zipOutputStream.close();
                    this.f3973a.f3967c.delete();
                    this.f3973a.f3968d.delete();
                    return;
                }
                zipOutputStream.write(bArr, 0, i);
                j += (long) i;
                publishProgress(Integer.valueOf((int) ((((float) j / length) * 50.0f) + 50.0f)));
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.os.AsyncTask
    /* JADX INFO: renamed from: a, reason: merged with bridge method [inline-methods] */
    public Void doInBackground(Void... voidArr) {
        C0924v c0924vMo4551a = C0836a.m4547a().mo4551a(this.f3974b);
        try {
            m4648a(c0924vMo4551a);
            if (isCancelled()) {
                return null;
            }
            m4649a(c0924vMo4551a, this.f3973a.f3967c);
            this.f3973a.f3969e = true;
            return null;
        } catch (Exception e) {
            Log.e("ERROR", e.getMessage());
            return null;
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.os.AsyncTask
    /* JADX INFO: renamed from: a, reason: merged with bridge method [inline-methods] */
    public void onPostExecute(Void r4) {
        View view = this.f3973a.getView();
        view.findViewById(R.id.exportingContainer).setVisibility(8);
        view.findViewById(R.id.finishedContainer).setVisibility(0);
        ((TextView) view.findViewById(R.id.finishedLabelTextView)).setText(this.f3973a.f3968d.getAbsolutePath());
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.os.AsyncTask
    /* JADX INFO: renamed from: a, reason: merged with bridge method [inline-methods] */
    public void onProgressUpdate(Integer... numArr) {
        this.f3973a.f3965a.setProgress(numArr[0].intValue());
    }
}
