package com.tweber.stickfighter.p037g;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.cardview.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.tweber.stickfighter.activities.R;
import com.tweber.stickfighter.p035e.C0843e;
import com.tweber.stickfighter.p038h.C0924v;
import com.tweber.stickfighter.p040j.C0935d;

/* JADX INFO: loaded from: classes.dex */
public class aa extends Fragment {
    /* JADX INFO: renamed from: a */
    public static aa m4676a() {
        return new aa();
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: renamed from: b */
    public void m4678b() {
        if (false) {
            requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 15);
        } else {
            new C0843e().show(getFragmentManager(), "CreateVideoDialogTag");
        }
    }

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View viewInflate = layoutInflater.inflate(R.layout.fragment_sequence_details, viewGroup, false);
        C0935d.m4959a(viewInflate, R.id.titleTextView, C0924v.m4932a().m4936c());
        ((CardView) viewInflate.findViewById(R.id.playCardView)).setOnClickListener(new ab(this));
        ((CardView) viewInflate.findViewById(R.id.saveAsVideoCardView)).setOnClickListener(new ac(this));
        return viewInflate;
    }

    @Override // androidx.fragment.app.Fragment
    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        switch (i) {
            case 15:
                if (iArr.length > 0 && iArr[0] == 0) {
                    m4678b();
                    break;
                }
                break;
        }
    }
}
