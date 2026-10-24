package com.tweber.stickfighter.p035e;

import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.tweber.stickfighter.activities.R;
import com.tweber.stickfighter.p038h.C0924v;

/* JADX INFO: renamed from: com.tweber.stickfighter.e.v */
/* JADX INFO: loaded from: classes.dex */
public class C0860v extends DialogFragment {

    /* JADX INFO: renamed from: a */
    ProgressBar f3975a;

    /* JADX INFO: renamed from: b */
    AsyncTaskC0864z f3976b;

    /* JADX INFO: renamed from: c */
    Uri f3977c;

    /* JADX INFO: renamed from: d */
    boolean f3978d;

    /* JADX INFO: renamed from: e */
    C0924v f3979e;

    /* JADX INFO: renamed from: a */
    public static C0860v m4653a(Uri uri) {
        C0860v c0860v = new C0860v();
        Bundle bundle = new Bundle();
        bundle.putString("EXTRA_SFA_FILE_URI", uri.toString());
        c0860v.setArguments(bundle);
        return c0860v;
    }

    @Override // androidx.fragment.app.DialogFragment, androidx.fragment.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setCancelable(false);
        setRetainInstance(true);
        this.f3977c = Uri.parse(getArguments().getString("EXTRA_SFA_FILE_URI"));
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
        View viewInflate = layoutInflater.inflate(R.layout.dialog_import_sequence, viewGroup, false);
        this.f3975a = (ProgressBar) viewInflate.findViewById(R.id.progressBarImporting);
        ((Button) viewInflate.findViewById(R.id.cancelButton)).setOnClickListener(new ViewOnClickListenerC0861w(this));
        ((Button) viewInflate.findViewById(R.id.closeButton)).setOnClickListener(new ViewOnClickListenerC0862x(this));
        ((Button) viewInflate.findViewById(R.id.openButton)).setOnClickListener(new ViewOnClickListenerC0863y(this));
        if (this.f3978d) {
            viewInflate.findViewById(R.id.importingContainer).setVisibility(8);
            viewInflate.findViewById(R.id.finishedContainer).setVisibility(0);
            ((TextView) viewInflate.findViewById(R.id.finishedLabelTextView)).setText(this.f3979e.m4936c());
        }
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

    @Override // androidx.fragment.app.Fragment
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        if (this.f3976b == null) {
            this.f3976b = new AsyncTaskC0864z(this, this.f3977c);
            this.f3976b.execute(new Void[0]);
        }
    }
}
