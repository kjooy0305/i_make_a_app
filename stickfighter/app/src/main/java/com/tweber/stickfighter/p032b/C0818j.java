package com.tweber.stickfighter.p032b;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.tweber.stickfighter.activities.R;

/* JADX INFO: renamed from: com.tweber.stickfighter.b.j */
/* JADX INFO: loaded from: classes.dex */
public class C0818j extends RecyclerView.ViewHolder {

    /* JADX INFO: renamed from: a */
    public ImageView f3845a;

    /* JADX INFO: renamed from: b */
    public TextView f3846b;

    /* JADX INFO: renamed from: c */
    public View f3847c;

    public C0818j(View view) {
        super(view);
        this.f3845a = (ImageView) view.findViewById(R.id.previewImageView);
        this.f3846b = (TextView) view.findViewById(R.id.titleTextView);
        this.f3847c = view.findViewById(R.id.deleteImageView);
    }
}
