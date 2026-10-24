package com.tweber.stickfighter.p035e;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.CamcorderProfile;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCrypto;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.TextView;
import com.tweber.stickfighter.activities.R;
import com.tweber.stickfighter.p032b.C0833y;
import com.tweber.stickfighter.p034d.C0836a;
import com.tweber.stickfighter.p038h.C0912j;
import com.tweber.stickfighter.p038h.C0918p;
import com.tweber.stickfighter.p038h.C0924v;
import com.tweber.stickfighter.p038h.InterfaceC0913k;
import com.tweber.stickfighter.p040j.C0933b;
import com.tweber.stickfighter.p040j.C0934c;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/* JADX INFO: renamed from: com.tweber.stickfighter.e.l */
/* JADX INFO: loaded from: classes.dex */
class AsyncTaskC0850l extends AsyncTask<Void, Integer, Void> {

    /* JADX INFO: renamed from: c */
    final /* synthetic */ C0843e f3955c;

    /* JADX INFO: renamed from: e */
    int f3957e;

    /* JADX INFO: renamed from: f */
    final int f3958f;

    /* JADX INFO: renamed from: g */
    int f3959g;

    /* JADX INFO: renamed from: h */
    int f3960h;

    /* JADX INFO: renamed from: i */
    int f3961i;

    /* JADX INFO: renamed from: a */
    final int f3953a = -1;

    /* JADX INFO: renamed from: b */
    final String f3954b = "video/avc";

    /* JADX INFO: renamed from: d */
    final C0924v f3956d = C0924v.m4932a();

    public AsyncTaskC0850l(C0843e c0843e, int i) {
        this.f3955c = c0843e;
        this.f3958f = i;
    }

    /* JADX INFO: renamed from: a */
    private int m4630a(MediaCodec mediaCodec) {
        MediaCodecInfo.CodecCapabilities capabilitiesForType = mediaCodec.getCodecInfo().getCapabilitiesForType("video/avc");
        ArrayList arrayList = new ArrayList();
        int[] iArr = capabilitiesForType.colorFormats;
        for (int i : iArr) {
            arrayList.add(Integer.valueOf(i));
        }
        if (arrayList.contains(21)) {
            return 21;
        }
        if (arrayList.contains(39)) {
            return 39;
        }
        if (arrayList.contains(2130706688)) {
            return 2130706688;
        }
        if (arrayList.contains(19)) {
            return 19;
        }
        if (arrayList.contains(20)) {
            return 20;
        }
        throw new RuntimeException("Could not find a compatible encoder color format");
    }

    /* JADX INFO: renamed from: a */
    private long m4631a(MediaCodec mediaCodec, MediaMuxer mediaMuxer, ByteBuffer[] byteBufferArr, ByteBuffer[] byteBufferArr2, InterfaceC0913k interfaceC0913k, C0912j c0912j, long j, Canvas canvas, Bitmap bitmap) {
        interfaceC0913k.mo4892a(c0912j, canvas, false, null, this.f3956d.m4938e());
        int iDequeueInputBuffer = mediaCodec.dequeueInputBuffer(-1L);
        byte[] bArrM4951a = C0933b.m4950a(this.f3961i) ? C0933b.m4951a(this.f3959g, this.f3960h, bitmap) : C0933b.m4952b(this.f3959g, this.f3960h, bitmap);
        int length = bArrM4951a.length;
        byteBufferArr[iDequeueInputBuffer].put(bArrM4951a);
        mediaCodec.queueInputBuffer(iDequeueInputBuffer, 0, length, j, 0);
        long j2 = (long) (j + (1000000.0f / this.f3957e));
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int iDequeueOutputBuffer = mediaCodec.dequeueOutputBuffer(bufferInfo, -1L);
        if (iDequeueOutputBuffer >= 0) {
            mediaMuxer.writeSampleData(0, byteBufferArr2[iDequeueOutputBuffer], bufferInfo);
            byteBufferArr2[iDequeueOutputBuffer].clear();
            mediaCodec.releaseOutputBuffer(iDequeueOutputBuffer, false);
        } else if (iDequeueOutputBuffer == -2) {
            mediaMuxer.addTrack(mediaCodec.getOutputFormat());
            mediaMuxer.start();
        }
        byteBufferArr[iDequeueInputBuffer].clear();
        return j2;
    }

    /* JADX INFO: renamed from: a */
    private MediaCodec m4632a() throws IOException {
        int i;
        C0833y c0833y = (C0833y) this.f3955c.f3938d.getSelectedItem();
        CamcorderProfile camcorderProfile = CamcorderProfile.get(c0833y.f3881a);
        if (camcorderProfile == null) {
            switch (c0833y.f3881a) {
                case 4:
                    this.f3959g = 720;
                    this.f3960h = 480;
                    this.f3957e = 30;
                    i = 6000000;
                    break;
                case 5:
                    this.f3959g = 1280;
                    this.f3960h = 720;
                    this.f3957e = 30;
                    i = 12000000;
                    break;
                case 6:
                    this.f3959g = 1920;
                    this.f3960h = 1080;
                    this.f3957e = 30;
                    i = 17000000;
                    break;
                default:
                    throw new RuntimeException("This will never happen");
            }
        } else {
            this.f3959g = camcorderProfile.videoFrameWidth;
            this.f3960h = camcorderProfile.videoFrameHeight;
            this.f3957e = camcorderProfile.videoFrameRate;
            i = camcorderProfile.videoBitRate;
        }
        MediaCodec mediaCodecCreateEncoderByType = MediaCodec.createEncoderByType("video/avc");
        this.f3961i = m4630a(mediaCodecCreateEncoderByType);
        MediaFormat mediaFormatCreateVideoFormat = MediaFormat.createVideoFormat("video/avc", this.f3959g, this.f3960h);
        mediaFormatCreateVideoFormat.setInteger("bitrate", i);
        mediaFormatCreateVideoFormat.setInteger("frame-rate", this.f3957e);
        mediaFormatCreateVideoFormat.setInteger("color-format", this.f3961i);
        mediaFormatCreateVideoFormat.setInteger("i-frame-interval", 1);
        mediaCodecCreateEncoderByType.configure(mediaFormatCreateVideoFormat, (Surface) null, (MediaCrypto) null, 1);
        mediaCodecCreateEncoderByType.start();
        return mediaCodecCreateEncoderByType;
    }

    /* JADX INFO: renamed from: a */
    private void m4633a(MediaCodec mediaCodec, MediaMuxer mediaMuxer) {
        ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
        ByteBuffer[] outputBuffers = mediaCodec.getOutputBuffers();
        Bitmap bitmapCreateBitmap = Bitmap.createBitmap(this.f3959g, this.f3960h, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmapCreateBitmap);
        C0912j c0912j = new C0912j(this.f3956d.m4937d(), canvas);
        long j = 0;
        int iMo4562b = C0836a.m4547a().mo4562b(this.f3956d.m4935b());
        int i = 0;
        while (i < iMo4562b - 1) {
            C0918p c0918pM4539a = this.f3956d.m4939f().m4539a(i);
            c0918pM4539a.m4928c();
            C0918p c0918pM4539a2 = this.f3956d.m4939f().m4539a(i + 1);
            c0918pM4539a2.m4928c();
            long jM4631a = m4631a(mediaCodec, mediaMuxer, inputBuffers, outputBuffers, c0918pM4539a, c0912j, j, canvas, bitmapCreateBitmap);
            int i2 = this.f3957e / this.f3958f;
            for (int i3 = 1; i3 <= i2; i3++) {
                jM4631a = m4631a(mediaCodec, mediaMuxer, inputBuffers, outputBuffers, C0918p.m4920a(c0918pM4539a, c0918pM4539a2, (1.0f / (i2 + 1)) * i3), c0912j, jM4631a, canvas, bitmapCreateBitmap);
                if (isCancelled()) {
                    this.f3955c.f3944j.delete();
                    return;
                }
            }
            publishProgress(Integer.valueOf((int) (((i + 1) / iMo4562b) * 100.0f)));
            i++;
            j = jM4631a;
        }
        long jM4631a2 = m4631a(mediaCodec, mediaMuxer, inputBuffers, outputBuffers, this.f3956d.m4939f().m4539a(iMo4562b - 1), c0912j, j, canvas, bitmapCreateBitmap);
        while (true) {
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int iDequeueOutputBuffer = mediaCodec.dequeueOutputBuffer(bufferInfo, 1000000L);
            if (iDequeueOutputBuffer < 0) {
                int iDequeueInputBuffer = mediaCodec.dequeueInputBuffer(-1L);
                mediaCodec.queueInputBuffer(iDequeueInputBuffer, 0, 0, jM4631a2, 4);
                MediaCodec.BufferInfo bufferInfo2 = new MediaCodec.BufferInfo();
                int iDequeueOutputBuffer2 = mediaCodec.dequeueOutputBuffer(bufferInfo2, -1L);
                outputBuffers[iDequeueOutputBuffer2].get(new byte[bufferInfo2.size]);
                mediaCodec.releaseOutputBuffer(iDequeueOutputBuffer2, false);
                inputBuffers[iDequeueInputBuffer].clear();
                outputBuffers[iDequeueOutputBuffer2].clear();
                mediaCodec.flush();
                mediaCodec.stop();
                mediaCodec.release();
                mediaMuxer.stop();
                mediaMuxer.release();
                return;
            }
            mediaMuxer.writeSampleData(0, outputBuffers[iDequeueOutputBuffer], bufferInfo);
            outputBuffers[iDequeueOutputBuffer].clear();
            mediaCodec.releaseOutputBuffer(iDequeueOutputBuffer, false);
        }
    }

    /* JADX INFO: renamed from: b */
    private MediaMuxer m4634b() throws java.io.IOException {
        this.f3955c.f3944j = m4635c();
        return new MediaMuxer(this.f3955c.f3944j.getAbsolutePath(), 0);
    }

    /* JADX INFO: renamed from: c */
    private File m4635c() {
        File file = new File(C0934c.m4955a(com.tweber.stickfighter.StickFighterApplication.m4490a().getExternalFilesDir(Environment.DIRECTORY_MOVIES).getAbsolutePath() + "/", this.f3956d.m4936c(), "Animation", ".mp4"));
        file.getParentFile().mkdirs();
        return file;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.os.AsyncTask
    /* JADX INFO: renamed from: a, reason: merged with bridge method [inline-methods] */
    public Void doInBackground(Void... voidArr) {
        try {
            m4633a(m4632a(), m4634b());
            this.f3955c.f3946l = true;
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Create video", e.getMessage());
            return null;
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.os.AsyncTask
    /* JADX INFO: renamed from: a, reason: merged with bridge method [inline-methods] */
    public void onPostExecute(Void r6) {
        View view = this.f3955c.getView();
        view.findViewById(R.id.videoSettingsContainer).setVisibility(8);
        view.findViewById(R.id.creatingContainer).setVisibility(8);
        view.findViewById(R.id.finishedContainer).setVisibility(0);
        if (this.f3955c.f3944j != null) {
            ((TextView) view.findViewById(R.id.finishedLabelTextView)).setText(this.f3955c.f3944j.getAbsolutePath());
            MediaScannerConnection.scanFile(this.f3955c.getContext(), new String[]{this.f3955c.f3944j.getPath()}, new String[]{"video/mp4"}, null);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.os.AsyncTask
    /* JADX INFO: renamed from: a, reason: merged with bridge method [inline-methods] */
    public void onProgressUpdate(Integer... numArr) {
        this.f3955c.f3941g.setProgress(numArr[0].intValue());
    }
}
