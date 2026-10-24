package com.tweber.stickfighter.activities;

import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import com.tweber.stickfighter.p037g.C0880d;
import com.tweber.stickfighter.p037g.ad;

/* JADX INFO: loaded from: classes.dex */
public class HomeScreenActivity extends AbstractActivityC0804c {

    /* JADX INFO: renamed from: a */
    ViewPager f3817a;

    /* JADX INFO: renamed from: b */
    FloatingActionButton f3818b;

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: renamed from: a */
    public String m4499a(int i, int i2) {
        return "android:switcher:" + i + ":" + i2;
    }

    /* JADX INFO: renamed from: a */
    private void m4501a(ViewPager viewPager) {
        C0807f c0807f = new C0807f(getSupportFragmentManager());
        c0807f.m4506a(ad.m4679a(getIntent().getData()), getString(R.string.animations));
        c0807f.m4506a(new C0880d(), getString(R.string.custom_figures));
        viewPager.setAdapter(c0807f);
        viewPager.addOnPageChangeListener(new C0806e(this));
    }

    @Override // com.tweber.stickfighter.activities.AbstractActivityC0804c
    /* JADX INFO: renamed from: a */
    protected int mo4497a() {
        return R.layout.activity_home_screen;
    }

    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onActivityResult(int i, int i2, Intent intent) {
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment != null) {
                fragment.onActivityResult(i, i2, intent);
            }
        }
    }

    @Override // com.tweber.stickfighter.activities.AbstractActivityC0804c, androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, android.support.v4.app.AbstractActivityC0100x, android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.f3817a = (ViewPager) findViewById(R.id.viewPager);
        m4501a(this.f3817a);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setTabMode(1);
        tabLayout.setTabGravity(0);
        tabLayout.setupWithViewPager(this.f3817a);
        this.f3818b = (FloatingActionButton) findViewById(R.id.fab);
        this.f3818b.setOnClickListener(new ViewOnClickListenerC0805d(this));
    }

    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity, android.support.v4.app.InterfaceC0079c
    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        super.onRequestPermissionsResult(i, strArr, iArr);
    }
}
