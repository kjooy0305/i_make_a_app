package com.tweber.stickfighter.p032b;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.tweber.stickfighter.activities.R;

/* JADX INFO: renamed from: com.tweber.stickfighter.b.w */
/* JADX INFO: loaded from: classes.dex */
public class C0831w extends RecyclerView.ViewHolder {

    /* JADX INFO: renamed from: a */
    public ImageView f3877a;

    /* JADX INFO: renamed from: b */
    public TextView f3878b;

    /* JADX INFO: renamed from: c */
    public View f3879c;

    public C0831w(View view) {
        super(view);
        this.f3877a = (ImageView) view.findViewById(R.id.previewImageView);
        this.f3878b = (TextView) view.findViewById(R.id.titleTextView);
        this.f3879c = view.findViewById(R.id.deleteImageView);
    }
}
