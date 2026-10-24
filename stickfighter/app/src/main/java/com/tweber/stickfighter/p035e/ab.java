package com.tweber.stickfighter.p035e;

import android.app.Dialog;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.DialogFragment;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.tweber.stickfighter.activities.R;
import com.tweber.stickfighter.p037g.C0879c;
import com.tweber.stickfighter.p037g.C0899w;
import com.tweber.stickfighter.p038h.AbstractC0908f;
import com.tweber.stickfighter.p038h.C0905c;
import java.util.ArrayList;
import java.util.Iterator;

/* JADX INFO: loaded from: classes.dex */
public class ab extends DialogFragment {

    /* JADX INFO: renamed from: a */
    ViewPager f3900a;

    /* JADX INFO: renamed from: b */
    C0879c f3901b = new C0879c();

    /* JADX INFO: renamed from: c */
    C0899w f3902c = new C0899w();

    /* JADX INFO: renamed from: d */
    ArrayList f3903d;

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: renamed from: a */
    public void m4589a() {
        m4592b();
        m4593c();
    }

    /* JADX INFO: renamed from: a */
    private void m4590a(ViewPager viewPager) {
        ae aeVar = new ae(getChildFragmentManager());
        aeVar.m4597a(this.f3901b, getString(R.string.color));
        aeVar.m4597a(this.f3902c, getString(R.string.size));
        viewPager.setAdapter(aeVar);
    }

    /* JADX INFO: renamed from: b */
    private void m4592b() {
        int iM4758a = this.f3901b.m4758a();
        Iterator it = this.f3903d.iterator();
        while (it.hasNext()) {
            ((AbstractC0908f) it.next()).m4875a(iM4758a);
        }
    }

    /* JADX INFO: renamed from: c */
    private void m4593c() {
        int iM4837a = this.f3902c.m4837a();
        if (iM4837a == 100) {
            return;
        }
        float f = iM4837a / 100.0f;
        C0905c c0905cM4594d = m4594d();
        float fM4863c = c0905cM4594d.m4863c();
        float fM4865d = c0905cM4594d.m4865d();
        Iterator it = this.f3903d.iterator();
        while (it.hasNext()) {
            for (C0905c c0905c : (Iterable<C0905c>) ((AbstractC0908f) it.next()).m4872a()) {
                c0905c.m4860b(((c0905c.m4863c() - fM4863c) * f) + fM4863c, ((c0905c.m4865d() - fM4865d) * f) + fM4865d);
            }
        }
    }

    /* JADX INFO: renamed from: d */
    private C0905c m4594d() {
        Iterator it = this.f3903d.iterator();
        while (it.hasNext()) {
            for (C0905c c0905c : (Iterable<C0905c>) ((AbstractC0908f) it.next()).m4872a()) {
                if (c0905c.m4859b() == 2) {
                    return c0905c;
                }
            }
        }
        return null;
    }

    /* JADX INFO: renamed from: a */
    public void m4595a(int i) {
        this.f3901b.m4759a(i);
    }

    /* JADX INFO: renamed from: a */
    public void m4596a(ArrayList arrayList) {
        this.f3903d = arrayList;
    }

    @Override // androidx.fragment.app.DialogFragment
    public Dialog onCreateDialog(Bundle bundle) {
        Dialog dialogOnCreateDialog = super.onCreateDialog(bundle);
        dialogOnCreateDialog.getWindow().requestFeature(1);
        return dialogOnCreateDialog;
    }

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View viewInflate = layoutInflater.inflate(R.layout.dialog_modify_figure, viewGroup, false);
        this.f3900a = (ViewPager) viewInflate.findViewById(R.id.viewPager);
        m4590a(this.f3900a);
        TabLayout tabLayout = (TabLayout) viewInflate.findViewById(R.id.tabs);
        tabLayout.setTabMode(1);
        tabLayout.setTabGravity(0);
        tabLayout.setupWithViewPager(this.f3900a);
        ((Button) viewInflate.findViewById(R.id.okButton)).setOnClickListener(new ac(this));
        ((Button) viewInflate.findViewById(R.id.cancelButton)).setOnClickListener(new ad(this));
        return viewInflate;
    }
}
