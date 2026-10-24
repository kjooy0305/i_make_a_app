package com.tweber.stickfighter.p032b;

import android.content.Context;
import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import com.tweber.stickfighter.activities.R;
import com.tweber.stickfighter.p036f.C0873i;
import com.tweber.stickfighter.p038h.C0924v;
import com.tweber.stickfighter.p039i.AsyncTaskC0927b;
import java.util.ArrayList;

/* JADX INFO: renamed from: com.tweber.stickfighter.b.e */
/* JADX INFO: loaded from: classes.dex */
public class C0813e extends RecyclerView.Adapter<C0818j> {

    /* JADX INFO: renamed from: a */
    ArrayList f3833a;

    /* JADX INFO: renamed from: b */
    int f3834b;

    public C0813e(ArrayList arrayList, int i) {
        this.f3833a = arrayList;
        this.f3834b = i;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: renamed from: a */
    public void m4510a(C0924v c0924v) {
        de.greenrobot.event.EventBus.getDefault().post(new C0873i(c0924v));
        new AsyncTaskC0927b(c0924v).execute(new Void[0]);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: renamed from: a */
    public void m4511a(C0924v c0924v, Context context) {
        new AlertDialog.Builder(context).setTitle(R.string.delete).setMessage(R.string.delete_custom_figure_confirmation).setPositiveButton(R.string.delete, new DialogInterfaceOnClickListenerC0816h(this, c0924v)).setNegativeButton(R.string.cancel, (DialogInterface.OnClickListener) null).show();
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    /* JADX INFO: renamed from: a, reason: merged with bridge method [inline-methods] */
    public C0818j onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new C0818j(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_entry_custom_figure, viewGroup, false));
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    /* JADX INFO: renamed from: a, reason: merged with bridge method [inline-methods] */
    public void onBindViewHolder(C0818j c0818j, int i) {
        C0924v c0924v = (C0924v) this.f3833a.get(i);
        c0818j.itemView.setOnClickListener(new ViewOnClickListenerC0814f(this, c0924v));
        c0818j.f3846b.setText(c0924v.m4936c());
        new AsyncTaskC0817i(this, c0924v, c0818j.f3845a).execute(new Void[0]);
        c0818j.f3847c.setOnClickListener(new ViewOnClickListenerC0815g(this, c0924v));
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemCount() {
        return this.f3833a.size();
    }
}
