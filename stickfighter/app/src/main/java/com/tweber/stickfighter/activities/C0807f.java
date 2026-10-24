package com.tweber.stickfighter.activities;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import java.util.ArrayList;
import java.util.List;

/* JADX INFO: renamed from: com.tweber.stickfighter.activities.f */
/* JADX INFO: loaded from: classes.dex */
class C0807f extends FragmentPagerAdapter {

    /* JADX INFO: renamed from: a */
    final List f3823a;

    /* JADX INFO: renamed from: b */
    final List f3824b;

    public C0807f(FragmentManager alVar) {
        super(alVar);
        this.f3823a = new ArrayList();
        this.f3824b = new ArrayList();
    }

    @Override // androidx.fragment.app.FragmentPagerAdapter
    /* JADX INFO: renamed from: a */
    public Fragment getItem(int i) {
        return (Fragment) this.f3823a.get(i);
    }

    /* JADX INFO: renamed from: a */
    public void m4506a(Fragment fragment, String str) {
        this.f3823a.add(fragment);
        this.f3824b.add(str);
    }

    @Override // androidx.viewpager.widget.PagerAdapter
    /* JADX INFO: renamed from: b */
    public int getCount() {
        return this.f3823a.size();
    }

    @Override // androidx.viewpager.widget.PagerAdapter
    /* JADX INFO: renamed from: c */
    public CharSequence getPageTitle(int i) {
        return (CharSequence) this.f3824b.get(i);
    }
}
