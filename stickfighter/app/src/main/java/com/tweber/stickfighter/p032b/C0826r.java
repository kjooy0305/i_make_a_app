package com.tweber.stickfighter.p032b;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import com.tweber.stickfighter.activities.R;
import com.tweber.stickfighter.p036f.C0873i;
import com.tweber.stickfighter.p038h.C0924v;
import com.tweber.stickfighter.p039i.AsyncTaskC0927b;
import com.tweber.stickfighter.p040j.C0935d;
import java.util.ArrayList;

/* JADX INFO: renamed from: com.tweber.stickfighter.b.r */
/* JADX INFO: loaded from: classes.dex */
public class C0826r extends RecyclerView.Adapter<C0831w> {

    /* JADX INFO: renamed from: a */
    ArrayList f3864a;

    /* JADX INFO: renamed from: b */
    int f3865b;

    /* JADX INFO: renamed from: c */
    Bitmap f3866c;

    public C0826r(ArrayList arrayList, int i, Activity activity) {
        this.f3864a = arrayList;
        this.f3865b = i;
        this.f3866c = m4528a(activity);
    }

    /* JADX INFO: renamed from: a */
    private Bitmap m4528a(Activity activity) {
        int iM4962c = C0935d.m4962c(activity) / this.f3865b;
        this.f3866c = Bitmap.createBitmap(iM4962c, (int) (iM4962c * C0935d.m4956a(activity)), Bitmap.Config.ALPHA_8);
        new Canvas(this.f3866c).drawColor(-1);
        return this.f3866c;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: renamed from: a */
    public void m4531a(C0924v c0924v) {
        de.greenrobot.event.EventBus.getDefault().post(new C0873i(c0924v));
        new AsyncTaskC0927b(c0924v).execute(new Void[0]);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: renamed from: a */
    public void m4532a(C0924v c0924v, Context context) {
        new AlertDialog.Builder(context).setTitle(R.string.delete).setMessage(R.string.delete_sequence_confirmation).setPositiveButton(R.string.delete, new DialogInterfaceOnClickListenerC0829u(this, c0924v)).setNegativeButton(R.string.cancel, (DialogInterface.OnClickListener) null).show();
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    /* JADX INFO: renamed from: a, reason: merged with bridge method [inline-methods] */
    public C0831w onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new C0831w(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_entry_sequence, viewGroup, false));
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    /* JADX INFO: renamed from: a, reason: merged with bridge method [inline-methods] */
    public void onBindViewHolder(C0831w c0831w, int i) {
        C0924v c0924v = (C0924v) this.f3864a.get(i);
        c0831w.f3877a.setImageBitmap(this.f3866c);
        c0831w.itemView.setOnClickListener(new ViewOnClickListenerC0827s(this, c0924v));
        c0831w.f3878b.setText(c0924v.m4936c());
        new AsyncTaskC0830v(this, c0924v, c0831w.f3877a).execute(new Void[0]);
        c0831w.f3879c.setOnClickListener(new ViewOnClickListenerC0828t(this, c0924v));
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemCount() {
        return this.f3864a.size();
    }
}
