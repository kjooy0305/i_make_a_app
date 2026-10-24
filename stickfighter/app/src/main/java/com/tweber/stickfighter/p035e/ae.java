package com.tweber.stickfighter.p035e;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import java.util.ArrayList;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
class ae extends FragmentPagerAdapter {

    /* JADX INFO: renamed from: a */
    final List f3906a;

    /* JADX INFO: renamed from: b */
    final List f3907b;

    public ae(FragmentManager alVar) {
        super(alVar);
        this.f3906a = new ArrayList();
        this.f3907b = new ArrayList();
    }

    @Override // androidx.fragment.app.FragmentPagerAdapter
    /* JADX INFO: renamed from: a */
    public Fragment getItem(int i) {
        return (Fragment) this.f3906a.get(i);
    }

    /* JADX INFO: renamed from: a */
    public void m4597a(Fragment fragment, String str) {
        this.f3906a.add(fragment);
        this.f3907b.add(str);
    }

    @Override // androidx.viewpager.widget.PagerAdapter
    /* JADX INFO: renamed from: b */
    public int getCount() {
        return this.f3906a.size();
    }

    @Override // androidx.viewpager.widget.PagerAdapter
    /* JADX INFO: renamed from: c */
    public CharSequence getPageTitle(int i) {
        return (CharSequence) this.f3907b.get(i);
    }
}
