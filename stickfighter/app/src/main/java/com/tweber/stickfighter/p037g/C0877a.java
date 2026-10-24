package com.tweber.stickfighter.p037g;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.tweber.stickfighter.activities.R;
import com.tweber.stickfighter.activities.SequenceDetailsActivity;
import com.tweber.stickfighter.p034d.C0836a;
import com.tweber.stickfighter.p038h.C0924v;
import com.tweber.stickfighter.p040j.C0935d;

/* JADX INFO: renamed from: com.tweber.stickfighter.g.a */
/* JADX INFO: loaded from: classes.dex */
public class C0877a extends Fragment {
    /* JADX INFO: renamed from: a */
    public static C0877a m4673a() {
        return new C0877a();
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: renamed from: b */
    public void m4675b() {
        FragmentActivity activity = getActivity();
        View view = getView();
        String strM4957a = C0935d.m4957a(view, R.id.titleEditText);
        C0935d.m4957a(view, R.id.descriptionEditText);
        C0935d.m4957a(view, R.id.creatorEditText);
        C0924v c0924vMo4552a = C0836a.m4547a().mo4552a(strM4957a, C0935d.m4956a((Activity) activity));
        Intent intent = new Intent(activity, (Class<?>) SequenceDetailsActivity.class);
        intent.putExtra("SequenceId", c0924vMo4552a.m4935b());
        activity.startActivity(intent);
        activity.finish();
    }

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View viewInflate = layoutInflater.inflate(R.layout.fragment_add_sequence, viewGroup, false);
        ((FloatingActionButton) viewInflate.findViewById(R.id.fab)).setOnClickListener(new ViewOnClickListenerC0878b(this));
        return viewInflate;
    }
}
