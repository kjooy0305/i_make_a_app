package com.tweber.stickfighter.activities;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import android.view.View;
import com.tweber.stickfighter.p037g.C0880d;
import com.tweber.stickfighter.p037g.ad;

/* JADX INFO: renamed from: com.tweber.stickfighter.activities.d */
/* JADX INFO: loaded from: classes.dex */
class ViewOnClickListenerC0805d implements View.OnClickListener {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ HomeScreenActivity f3821a;

    ViewOnClickListenerC0805d(HomeScreenActivity homeScreenActivity) {
        this.f3821a = homeScreenActivity;
    }

    /* JADX INFO: renamed from: a */
    public Fragment m4505a(ViewPager viewPager, int i) {
        return this.f3821a.getSupportFragmentManager().findFragmentByTag(this.f3821a.m4499a(viewPager.getId(), i));
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        switch (this.f3821a.f3817a.getCurrentItem()) {
            case 0:
                ((ad) m4505a(this.f3821a.f3817a, 0)).m4686a();
                break;
            case 1:
                ((C0880d) m4505a(this.f3821a.f3817a, 1)).m4762a();
                break;
        }
    }
}
