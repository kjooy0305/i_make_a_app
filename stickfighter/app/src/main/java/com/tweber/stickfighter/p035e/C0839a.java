package com.tweber.stickfighter.p035e;

import android.app.Dialog;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.tweber.stickfighter.activities.R;
import com.tweber.stickfighter.p034d.C0836a;
import com.tweber.stickfighter.p037g.C0879c;
import com.tweber.stickfighter.p037g.C0898v;
import com.tweber.stickfighter.p038h.C0924v;

/* JADX INFO: renamed from: com.tweber.stickfighter.e.a */
/* JADX INFO: loaded from: classes.dex */
public class C0839a extends DialogFragment {

    /* JADX INFO: renamed from: a */
    ViewPager f3889a;

    /* JADX INFO: renamed from: a */
    public static C0839a m4583a(int i) {
        Bundle bundle = new Bundle();
        bundle.putInt("EXTRA_INITIAL_COLOR", i);
        C0839a c0839a = new C0839a();
        c0839a.setArguments(bundle);
        return c0839a;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: renamed from: a */
    public void m4584a() {
        m4587b();
    }

    /* JADX INFO: renamed from: a */
    private void m4585a(ViewPager viewPager) {
        C0842d c0842d = new C0842d(getChildFragmentManager());
        C0879c c0879c = new C0879c();
        c0879c.m4759a(getArguments().getInt("EXTRA_INITIAL_COLOR"));
        c0842d.m4610a(c0879c, getString(R.string.color));
        viewPager.setAdapter(c0842d);
    }

    /* JADX INFO: renamed from: b */
    private void m4587b() {
        int iM4758a = ((C0879c) getChildFragmentManager().getFragments().get(0)).m4758a();
        C0924v.m4932a().m4934a(iM4758a);
        C0836a.m4547a().mo4556a(C0924v.m4932a().m4935b(), iM4758a);
        ((C0898v) getParentFragment()).m4834b();
    }

    @Override // androidx.fragment.app.DialogFragment
    public Dialog onCreateDialog(Bundle bundle) {
        return super.onCreateDialog(bundle);
    }

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View viewInflate = layoutInflater.inflate(R.layout.dialog_change_background, viewGroup, false);
        this.f3889a = (ViewPager) viewInflate.findViewById(R.id.viewPager);
        m4585a(this.f3889a);
        ((Button) viewInflate.findViewById(R.id.okButton)).setOnClickListener(new ViewOnClickListenerC0840b(this));
        ((Button) viewInflate.findViewById(R.id.cancelButton)).setOnClickListener(new ViewOnClickListenerC0841c(this));
        return viewInflate;
    }
}
