package com.tweber.stickfighter.p035e;

import android.app.Dialog;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import com.tweber.stickfighter.activities.R;
import com.tweber.stickfighter.p037g.ad;

/* JADX INFO: loaded from: classes.dex */
public class ao extends DialogFragment {
    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: renamed from: a */
    public boolean m4608a() {
        EditText editText = (EditText) getView().findViewById(R.id.sequenceNameEditText);
        String string = editText.getText().toString();
        if (string.trim().isEmpty()) {
            editText.setError(getString(R.string.error_no_sequence_title));
            return false;
        }
        ((ad) getParentFragment()).m4687a(string);
        dismiss();
        return true;
    }

    @Override // androidx.fragment.app.DialogFragment
    public Dialog onCreateDialog(Bundle bundle) {
        Dialog dialogOnCreateDialog = super.onCreateDialog(bundle);
        dialogOnCreateDialog.getWindow().requestFeature(1);
        return dialogOnCreateDialog;
    }

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View viewInflate = layoutInflater.inflate(R.layout.dialog_sequence_name, viewGroup, false);
        ((Button) viewInflate.findViewById(R.id.okButton)).setOnClickListener(new ap(this));
        ((Button) viewInflate.findViewById(R.id.cancelButton)).setOnClickListener(new aq(this));
        ((EditText) viewInflate.findViewById(R.id.sequenceNameEditText)).setOnEditorActionListener(new ar(this));
        return viewInflate;
    }
}
