package com.tweber.stickfighter.p037g;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import com.tweber.stickfighter.activities.R;
import com.tweber.stickfighter.p032b.C0819k;
import com.tweber.stickfighter.p035e.C0839a;
import com.tweber.stickfighter.p036f.C0866b;
import com.tweber.stickfighter.p036f.EnumC0867c;
import com.tweber.stickfighter.p038h.C0924v;
import com.tweber.stickfighter.p040j.C0935d;

/* JADX INFO: renamed from: com.tweber.stickfighter.g.v */
/* JADX INFO: loaded from: classes.dex */
public class C0898v extends Fragment {

    /* JADX INFO: renamed from: a */
    C0819k f4156a;

    /* JADX INFO: renamed from: a */
    public static C0898v m4831a() {
        return new C0898v();
    }

    /* JADX INFO: renamed from: c */
    private int m4832c() {
        int i = getResources().getConfiguration().orientation;
        if (C0935d.m4960a((Context) getActivity())) {
            return i == 2 ? 6 : 4;
        }
        return i == 2 ? 5 : 3;
    }

    /* JADX INFO: renamed from: d */
    private void m4833d() {
        C0839a.m4583a(C0924v.m4932a().m4938e()).show(getChildFragmentManager(), (String) null);
    }

    /* JADX INFO: renamed from: b */
    public void m4834b() {
        this.f4156a.notifyDataSetChanged();
    }

    @Override // androidx.fragment.app.Fragment
    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        RecyclerView recyclerView = (RecyclerView) getView().findViewById(R.id.frameRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), m4832c()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        this.f4156a = new C0819k(C0924v.m4932a(), m4832c(), getActivity());
        recyclerView.setAdapter(this.f4156a);
    }

    @Override // androidx.fragment.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setHasOptionsMenu(true);
    }

    @Override // androidx.fragment.app.Fragment
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.frame_list, menu);
    }

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View viewInflate = layoutInflater.inflate(R.layout.fragment_frame_list, viewGroup, false);
        RecyclerView recyclerView = (RecyclerView) viewInflate.findViewById(R.id.frameRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), m4832c()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        this.f4156a = new C0819k(C0924v.m4932a(), m4832c(), getActivity());
        recyclerView.setAdapter(this.f4156a);
        return viewInflate;
    }

    @Override // androidx.fragment.app.Fragment
    public void onDestroyView() {
        super.onDestroyView();
        de.greenrobot.event.EventBus.getDefault().post(new C0866b(EnumC0867c.FRAME_DRAW));
    }

    @Override // androidx.fragment.app.Fragment
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.change_background /* 2131493113 */:
                m4833d();
                return true;
            default:
                return false;
        }
    }

    @Override // androidx.fragment.app.Fragment
    public void onResume() {
        super.onResume();
    }
}
