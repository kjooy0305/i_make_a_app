package com.tweber.stickfighter.p032b;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.AsyncTask;
import com.google.android.material.snackbar.Snackbar;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import com.tweber.stickfighter.StickFighterApplication;
import com.tweber.stickfighter.activities.R;
import com.tweber.stickfighter.p034d.C0836a;
import com.tweber.stickfighter.p036f.C0868d;
import com.tweber.stickfighter.p038h.C0924v;
import com.tweber.stickfighter.p040j.C0935d;

/* JADX INFO: renamed from: com.tweber.stickfighter.b.k */
/* JADX INFO: loaded from: classes.dex */
public class C0819k extends RecyclerView.Adapter<C0825q> {

    /* JADX INFO: renamed from: a */
    C0924v f3848a;

    /* JADX INFO: renamed from: b */
    int f3849b;

    /* JADX INFO: renamed from: c */
    Bitmap f3850c;

    public C0819k(C0924v c0924v, int i, Activity activity) {
        this.f3848a = c0924v;
        this.f3849b = i;
        this.f3850c = m4516a(activity);
    }

    /* JADX INFO: renamed from: a */
    private Bitmap m4516a(Activity activity) {
        int iM4962c = C0935d.m4962c(activity) / this.f3849b;
        this.f3850c = Bitmap.createBitmap(iM4962c, (int) (iM4962c * this.f3848a.m4937d()), Bitmap.Config.RGB_565);
        new Canvas(this.f3850c).drawColor(-3355444);
        return this.f3850c;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: renamed from: a */
    public void m4519a(C0825q c0825q) {
        if (this.f3848a.m4939f().m4538a() == 1) {
            Snackbar.make(c0825q.itemView, StickFighterApplication.m4490a().getString(R.string.frame_deletion_last), -1).show();
            return;
        }
        int adapterPosition = c0825q.getAdapterPosition();
        int iM4538a = (this.f3848a.m4939f().m4538a() - adapterPosition) - 1;
        C0836a.m4547a().mo4572c(this.f3848a.m4939f().m4539a(iM4538a));
        this.f3848a.m4939f().m4545c(iM4538a);
        notifyItemRemoved(adapterPosition);
        notifyItemRangeChanged(0, adapterPosition);
        Snackbar.make(c0825q.itemView, StickFighterApplication.m4490a().getString(R.string.frame_deleted, Integer.valueOf(iM4538a + 1)), -1).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: renamed from: b */
    public void m4522b(C0825q c0825q) {
        int adapterPosition = c0825q.getAdapterPosition();
        int iM4538a = (this.f3848a.m4939f().m4538a() - adapterPosition) - 1;
        C0836a.m4547a().mo4571c(this.f3848a.m4935b(), iM4538a + 1);
        this.f3848a.m4939f().m4540a(iM4538a + 1, C0836a.m4547a().mo4565b(this.f3848a.m4939f().m4539a(iM4538a)));
        notifyItemInserted(adapterPosition);
        c0825q.itemView.post(new RunnableC0823o(this, iM4538a));
        Snackbar.make(c0825q.itemView, StickFighterApplication.m4490a().getString(R.string.frame_copied, Integer.valueOf(iM4538a + 1)), -1).show();
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    /* JADX INFO: renamed from: a, reason: merged with bridge method [inline-methods] */
    public C0825q onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new C0825q(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_entry_frame, viewGroup, false));
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    /* JADX INFO: renamed from: a, reason: merged with bridge method [inline-methods] */
    public void onBindViewHolder(C0825q c0825q, int i) {
        c0825q.f3863a.setImageBitmap(this.f3850c);
        AsyncTaskC0824p asyncTaskC0824p = (AsyncTaskC0824p) c0825q.itemView.getTag();
        if (asyncTaskC0824p != null && asyncTaskC0824p.getStatus() == AsyncTask.Status.RUNNING) {
            asyncTaskC0824p.cancel(true);
        }
        AsyncTaskC0824p asyncTaskC0824p2 = new AsyncTaskC0824p(this, this.f3848a, (this.f3848a.m4939f().m4538a() - i) - 1, c0825q.f3863a);
        asyncTaskC0824p2.execute(new Void[0]);
        c0825q.itemView.setTag(asyncTaskC0824p2);
        c0825q.itemView.setOnClickListener(new ViewOnClickListenerC0820l(this, c0825q));
        c0825q.itemView.setOnLongClickListener(new ViewOnLongClickListenerC0821m(this, c0825q));
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemCount() {
        return this.f3848a.m4939f().m4538a();
    }

    public void onEvent(C0868d c0868d) {
    }
}
