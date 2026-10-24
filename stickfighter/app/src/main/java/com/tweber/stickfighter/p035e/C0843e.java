package com.tweber.stickfighter.p035e;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import com.tweber.stickfighter.StickFighterApplication;
import com.tweber.stickfighter.activities.R;
import com.tweber.stickfighter.p032b.C0832x;
import com.tweber.stickfighter.p038h.C0924v;
import java.io.File;

/* JADX INFO: renamed from: com.tweber.stickfighter.e.e */
/* JADX INFO: loaded from: classes.dex */
public class C0843e extends DialogFragment {

    /* JADX INFO: renamed from: a */
    final int[] f3935a = {1, 2, 3, 4, 6, 8};

    /* JADX INFO: renamed from: b */
    final String[] f3936b = {"0.5x", "1x", "1.5x", "2x", "3x", "4x"};

    /* JADX INFO: renamed from: c */
    final String f3937c = "PlayerKeyFPS";

    /* JADX INFO: renamed from: d */
    Spinner f3938d;

    /* JADX INFO: renamed from: e */
    SeekBar f3939e;

    /* JADX INFO: renamed from: f */
    TextView f3940f;

    /* JADX INFO: renamed from: g */
    ProgressBar f3941g;

    /* JADX INFO: renamed from: h */
    AsyncTaskC0850l f3942h;

    /* JADX INFO: renamed from: i */
    int f3943i;

    /* JADX INFO: renamed from: j */
    File f3944j;

    /* JADX INFO: renamed from: k */
    boolean f3945k;

    /* JADX INFO: renamed from: l */
    boolean f3946l;

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: renamed from: a */
    public void m4613a() {
        dismiss();
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: renamed from: b */
    public void m4616b() {
        this.f3945k = true;
        View view = getView();
        view.findViewById(R.id.videoSettingsContainer).setVisibility(8);
        view.findViewById(R.id.creatingContainer).setVisibility(0);
        view.findViewById(R.id.finishedContainer).setVisibility(8);
        this.f3942h = new AsyncTaskC0850l(this, this.f3943i);
        this.f3942h.execute(new Void[0]);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: renamed from: c */
    public void m4618c() {
        if (this.f3942h != null) {
            this.f3942h.cancel(false);
        }
        dismiss();
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: renamed from: d */
    public void m4620d() {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.SEND");
        intent.putExtra("android.intent.extra.STREAM", com.tweber.stickfighter.util.FileShare.uri(this.f3944j));
        intent.addFlags(1);
        intent.putExtra("android.intent.extra.TITLE", C0924v.m4932a().m4936c());
        intent.setType("video/mp4");
        startActivity(Intent.createChooser(intent, getResources().getText(R.string.share)));
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: renamed from: e */
    public void m4622e() {
        dismiss();
    }

    @Override // androidx.fragment.app.DialogFragment, androidx.fragment.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setCancelable(false);
        setRetainInstance(true);
    }

    @Override // androidx.fragment.app.DialogFragment
    public Dialog onCreateDialog(Bundle bundle) {
        Dialog dialogOnCreateDialog = super.onCreateDialog(bundle);
        dialogOnCreateDialog.setCanceledOnTouchOutside(false);
        dialogOnCreateDialog.setCancelable(false);
        dialogOnCreateDialog.getWindow().requestFeature(1);
        return dialogOnCreateDialog;
    }

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View viewInflate = layoutInflater.inflate(R.layout.dialog_create_video, viewGroup, false);
        this.f3941g = (ProgressBar) viewInflate.findViewById(R.id.progressBarCreatingVideo);
        ((Button) viewInflate.findViewById(R.id.cancelInitialButton)).setOnClickListener(new ViewOnClickListenerC0844f(this));
        ((Button) viewInflate.findViewById(R.id.createButton)).setOnClickListener(new ViewOnClickListenerC0845g(this));
        ((Button) viewInflate.findViewById(R.id.cancelCreatingButton)).setOnClickListener(new ViewOnClickListenerC0846h(this));
        ((Button) viewInflate.findViewById(R.id.shareButton)).setOnClickListener(new ViewOnClickListenerC0847i(this));
        ((Button) viewInflate.findViewById(R.id.closeButton)).setOnClickListener(new ViewOnClickListenerC0848j(this));
        if (this.f3946l) {
            viewInflate.findViewById(R.id.videoSettingsContainer).setVisibility(8);
            viewInflate.findViewById(R.id.creatingContainer).setVisibility(8);
            viewInflate.findViewById(R.id.finishedContainer).setVisibility(0);
            ((TextView) viewInflate.findViewById(R.id.finishedLabelTextView)).setText(this.f3944j.getAbsolutePath());
        } else if (this.f3945k) {
            viewInflate.findViewById(R.id.videoSettingsContainer).setVisibility(8);
            viewInflate.findViewById(R.id.creatingContainer).setVisibility(0);
            viewInflate.findViewById(R.id.finishedContainer).setVisibility(8);
        } else {
            viewInflate.findViewById(R.id.videoSettingsContainer).setVisibility(0);
            viewInflate.findViewById(R.id.creatingContainer).setVisibility(8);
            viewInflate.findViewById(R.id.finishedContainer).setVisibility(8);
        }
        this.f3938d = (Spinner) viewInflate.findViewById(R.id.videoQualitySpinner);
        this.f3938d.setAdapter((SpinnerAdapter) new C0832x());
        this.f3938d.setSelection((int) Math.ceil(((double) (this.f3938d.getAdapter().getCount() - 1)) / 2.0d));
        this.f3939e = (SeekBar) viewInflate.findViewById(R.id.keyFpsSeekBar);
        this.f3940f = (TextView) viewInflate.findViewById(R.id.keyFpsValueTextView);
        int i = StickFighterApplication.m4491b().getInt("PlayerKeyFPS", 3);
        this.f3939e.setMax(this.f3935a.length - 1);
        this.f3939e.setProgress(i);
        this.f3940f.setText(this.f3936b[i]);
        this.f3943i = this.f3935a[i];
        this.f3939e.setOnSeekBarChangeListener(new C0849k(this));
        return viewInflate;
    }

    @Override // androidx.fragment.app.DialogFragment, androidx.fragment.app.Fragment
    public void onDestroyView() {
        Dialog dialog = getDialog();
        if (dialog != null && getRetainInstance()) {
            dialog.setDismissMessage(null);
        }
        super.onDestroyView();
    }
}
