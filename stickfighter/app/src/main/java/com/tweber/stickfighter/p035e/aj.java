package com.tweber.stickfighter.p035e;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import com.tweber.stickfighter.activities.R;
import com.tweber.stickfighter.p038h.C0914l;

/* JADX INFO: loaded from: classes.dex */
class aj extends RecyclerView.Adapter<am> {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ af f3919a;

    /* JADX INFO: renamed from: b */
    final int f3920b = 0;

    /* JADX INFO: renamed from: c */
    C0914l[] f3921c;

    public aj(af afVar, C0914l[] c0914lArr) {
        this.f3919a = afVar;
        this.f3921c = c0914lArr;
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    /* JADX INFO: renamed from: a, reason: merged with bridge method [inline-methods] */
    public am onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new am(this, ((LayoutInflater) this.f3919a.getActivity().getSystemService("layout_inflater")).inflate(R.layout.list_entry_figure, viewGroup, false));
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    /* JADX INFO: renamed from: a, reason: merged with bridge method [inline-methods] */
    public void onBindViewHolder(am amVar, int i) {
        if (i >= this.f3921c.length) {
            amVar.f3925a.setText(R.string.no_custom_figures_created_yet);
            amVar.itemView.setOnClickListener(new ak(this));
        } else {
            C0914l c0914l = this.f3921c[i];
            amVar.f3925a.setText(c0914l.m4917a());
            amVar.itemView.setOnClickListener(new al(this, c0914l));
        }
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemCount() {
        if (this.f3921c == null) {
            return 0;
        }
        return Math.max(1, this.f3921c.length);
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemViewType(int i) {
        return 0;
    }
}
