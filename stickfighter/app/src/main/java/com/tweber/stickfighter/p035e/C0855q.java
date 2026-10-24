package com.tweber.stickfighter.p035e;

import android.app.Dialog;
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
import java.io.File;

/* JADX INFO: renamed from: com.tweber.stickfighter.e.q */
/* JADX INFO: loaded from: classes.dex */
public class C0855q extends DialogFragment {

    /* JADX INFO: renamed from: a */
    ProgressBar f3965a;

    /* JADX INFO: renamed from: b */
    AsyncTaskC0859u f3966b;

    /* JADX INFO: renamed from: c */
    File f3967c;

    /* JADX INFO: renamed from: d */
    File f3968d;

    /* JADX INFO: renamed from: e */
    boolean f3969e;

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
        View viewInflate = layoutInflater.inflate(R.layout.dialog_export_sequence, viewGroup, false);
        this.f3965a = (ProgressBar) viewInflate.findViewById(R.id.progressBarExporting);
        ((Button) viewInflate.findViewById(R.id.cancelButton)).setOnClickListener(new ViewOnClickListenerC0856r(this));
        ((Button) viewInflate.findViewById(R.id.shareButton)).setOnClickListener(new ViewOnClickListenerC0857s(this));
        ((Button) viewInflate.findViewById(R.id.closeButton)).setOnClickListener(new ViewOnClickListenerC0858t(this));
        if (this.f3969e) {
            viewInflate.findViewById(R.id.exportingContainer).setVisibility(8);
            viewInflate.findViewById(R.id.finishedContainer).setVisibility(0);
            ((TextView) viewInflate.findViewById(R.id.finishedLabelTextView)).setText(this.f3968d.getAbsolutePath());
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
        if (this.f3966b == null) {
            this.f3966b = new AsyncTaskC0859u(this, C0924v.m4932a().m4935b());
            this.f3966b.execute(new Void[0]);
        }
    }
}
