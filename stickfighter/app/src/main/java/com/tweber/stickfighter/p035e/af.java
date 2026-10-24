package com.tweber.stickfighter.p035e;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.tweber.stickfighter.activities.R;
import com.tweber.stickfighter.p038h.C0914l;
import com.tweber.stickfighter.p038h.C0915m;
import com.tweber.stickfighter.p038h.EnumC0917o;
import java.util.Arrays;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public class af extends DialogFragment {

    /* JADX INFO: renamed from: a */
    an f3908a;

    /* JADX INFO: renamed from: b */
    List f3909b;

    /* JADX INFO: renamed from: c */
    C0915m f3910c;

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: renamed from: a */
    public void m4600a(C0914l c0914l) {
        this.f3908a.mo4607a(c0914l.f4198a);
        dismiss();
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: renamed from: a */
    public void m4601a(C0915m c0915m) {
        this.f3910c = c0915m;
        ((RecyclerView) getView().findViewById(R.id.figureRecyclerView)).setAdapter(new aj(this, c0915m.f4208h));
    }

    /* JADX INFO: renamed from: a */
    public void m4602a(EnumC0917o enumC0917o) {
        if (this.f3909b == null) {
            this.f3909b = Arrays.asList(C0915m.m4918a(enumC0917o));
        }
    }

    @Override // androidx.fragment.app.DialogFragment
    public Dialog onCreateDialog(Bundle bundle) {
        Dialog dialogOnCreateDialog = super.onCreateDialog(bundle);
        dialogOnCreateDialog.getWindow().requestFeature(1);
        dialogOnCreateDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        return dialogOnCreateDialog;
    }

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View viewInflate = layoutInflater.inflate(R.layout.dialog_select_figure_horizontal, viewGroup, false);
        this.f3908a = (an) getParentFragment();
        RecyclerView recyclerView = (RecyclerView) viewInflate.findViewById(R.id.categoryRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), 1, false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(new ag(this, this.f3909b));
        RecyclerView recyclerView2 = (RecyclerView) viewInflate.findViewById(R.id.figureRecyclerView);
        recyclerView2.setLayoutManager(new LinearLayoutManager(getActivity(), 1, false));
        recyclerView2.setItemAnimator(new DefaultItemAnimator());
        recyclerView2.setAdapter(new aj(this, this.f3910c == null ? null : this.f3910c.f4208h));
        return viewInflate;
    }
}
