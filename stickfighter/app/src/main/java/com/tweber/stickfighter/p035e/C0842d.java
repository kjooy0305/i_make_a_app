package com.tweber.stickfighter.p035e;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import java.util.ArrayList;
import java.util.List;

/* JADX INFO: renamed from: com.tweber.stickfighter.e.d */
/* JADX INFO: loaded from: classes.dex */
class C0842d extends FragmentStatePagerAdapter {

    /* JADX INFO: renamed from: a */
    final List f3933a;

    /* JADX INFO: renamed from: b */
    final List f3934b;

    public C0842d(FragmentManager alVar) {
        super(alVar);
        this.f3933a = new ArrayList();
        this.f3934b = new ArrayList();
    }

    @Override // androidx.fragment.app.FragmentStatePagerAdapter
    /* JADX INFO: renamed from: a */
    public Fragment getItem(int i) {
        return (Fragment) this.f3933a.get(i);
    }

    /* JADX INFO: renamed from: a */
    public void m4610a(Fragment fragment, String str) {
        this.f3933a.add(fragment);
        this.f3934b.add(str);
    }

    @Override // androidx.viewpager.widget.PagerAdapter
    /* JADX INFO: renamed from: b */
    public int getCount() {
        return this.f3933a.size();
    }

    @Override // androidx.viewpager.widget.PagerAdapter
    /* JADX INFO: renamed from: c */
    public CharSequence getPageTitle(int i) {
        return (CharSequence) this.f3934b.get(i);
    }
}
