package com.tweber.stickfighter.p037g;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.tweber.stickfighter.activities.EditCustomFigureActivity;
import com.tweber.stickfighter.activities.R;
import com.tweber.stickfighter.p032b.C0813e;
import com.tweber.stickfighter.p034d.C0836a;
import com.tweber.stickfighter.p035e.C0851m;
import com.tweber.stickfighter.p036f.C0873i;
import com.tweber.stickfighter.p038h.C0924v;
import com.tweber.stickfighter.p040j.C0935d;
import java.util.ArrayList;

/* JADX INFO: renamed from: com.tweber.stickfighter.g.d */
/* JADX INFO: loaded from: classes.dex */
public class C0880d extends Fragment {

    /* JADX INFO: renamed from: a */
    RecyclerView f4086a;

    /* JADX INFO: renamed from: b */
    ArrayList f4087b;

    /* JADX INFO: renamed from: a */
    private void m4760a(View view) {
        this.f4087b = C0836a.m4547a().mo4567b();
        if (this.f4087b.isEmpty()) {
            view.findViewById(R.id.noResultsView).setVisibility(0);
        }
        this.f4086a.setAdapter(new C0813e(this.f4087b, m4761b()));
    }

    /* JADX INFO: renamed from: b */
    private int m4761b() {
        int i = getResources().getConfiguration().orientation;
        if (C0935d.m4960a((Context) getActivity())) {
            return i == 2 ? 4 : 3;
        }
        return i != 2 ? 2 : 3;
    }

    /* JADX INFO: renamed from: a */
    public void m4762a() {
        new C0851m().show(getChildFragmentManager(), (String) null);
    }

    /* JADX INFO: renamed from: a */
    public void m4763a(String str) {
        C0924v c0924vMo4566b = C0836a.m4547a().mo4566b(str, C0935d.m4956a((Activity) getActivity()));
        C0836a.m4547a().mo4550a(c0924vMo4566b.m4935b(), 0);
        this.f4087b.add(0, c0924vMo4566b);
        this.f4086a.getAdapter().notifyItemInserted(0);
        this.f4086a.scrollToPosition(0);
        getView().findViewById(R.id.noResultsView).setVisibility(8);
        Intent intent = new Intent(getActivity(), (Class<?>) EditCustomFigureActivity.class);
        intent.putExtra("CustomFigureID", c0924vMo4566b.m4935b());
        getActivity().startActivityForResult(intent, 102);
    }

    @Override // androidx.fragment.app.Fragment
    public void onActivityResult(int i, int i2, Intent intent) {
        int iIndexOf;
        super.onActivityResult(i, i2, intent);
        if (i == 102) {
            C0924v.m4933a((C0924v) null);
            this.f4086a.getAdapter().notifyItemRangeChanged(0, this.f4087b.size());
        } else {
            if (i != 99 || (iIndexOf = this.f4087b.indexOf(C0924v.m4932a())) < 0) {
                return;
            }
            ((C0924v) this.f4087b.get(iIndexOf)).m4939f().m4542b();
            C0924v.m4933a((C0924v) null);
            this.f4086a.getAdapter().notifyItemRangeChanged(0, this.f4087b.size());
        }
    }

    @Override // androidx.fragment.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setHasOptionsMenu(false);
        this.f4087b = new ArrayList();
        de.greenrobot.event.EventBus.getDefault().register(this);
    }

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View viewInflate = layoutInflater.inflate(R.layout.fragment_custom_figure_list, viewGroup, false);
        this.f4086a = (RecyclerView) viewInflate.findViewById(R.id.sequenceRecyclerView);
        this.f4086a.setLayoutManager(new StaggeredGridLayoutManager(m4761b(), 1));
        this.f4086a.setItemAnimator(new DefaultItemAnimator());
        m4760a(viewInflate);
        return viewInflate;
    }

    @Override // androidx.fragment.app.Fragment
    public void onDestroy() {
        super.onDestroy();
        de.greenrobot.event.EventBus.getDefault().unregister(this);
    }

    public void onEvent(C0873i c0873i) {
        int iIndexOf = this.f4087b.indexOf(c0873i.m4668a());
        if (iIndexOf < 0) {
            return;
        }
        this.f4087b.remove(iIndexOf);
        View view = getView();
        if (view != null) {
            Snackbar.make(getActivity().findViewById(R.id.coordinatorLayout), getString(R.string.custom_figure_deleted, c0873i.m4668a().m4936c()), 0).show();
            ((RecyclerView) view.findViewById(R.id.sequenceRecyclerView)).getAdapter().notifyItemRemoved(iIndexOf);
            if (this.f4087b.isEmpty()) {
                view.findViewById(R.id.noResultsView).setVisibility(0);
            }
        }
    }
}
