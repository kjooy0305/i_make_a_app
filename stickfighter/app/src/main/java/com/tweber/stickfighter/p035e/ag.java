package com.tweber.stickfighter.p035e;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import com.tweber.stickfighter.activities.R;
import com.tweber.stickfighter.p038h.C0915m;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
class ag extends RecyclerView.Adapter<ai> {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ af f3911a;

    /* JADX INFO: renamed from: b */
    final int f3912b = 0;

    /* JADX INFO: renamed from: c */
    List f3913c;

    public ag(af afVar, List list) {
        this.f3911a = afVar;
        this.f3913c = list;
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    /* JADX INFO: renamed from: a, reason: merged with bridge method [inline-methods] */
    public ai onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ai(this, ((LayoutInflater) this.f3911a.getActivity().getSystemService("layout_inflater")).inflate(R.layout.list_entry_category, viewGroup, false));
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    /* JADX INFO: renamed from: a, reason: merged with bridge method [inline-methods] */
    public void onBindViewHolder(ai aiVar, int i) {
        C0915m c0915m = (C0915m) this.f3913c.get(i);
        aiVar.f3916a.setText(c0915m.f4207g);
        aiVar.itemView.setOnClickListener(new ah(this, c0915m));
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemCount() {
        return this.f3913c.size();
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemViewType(int i) {
        return 0;
    }
}
