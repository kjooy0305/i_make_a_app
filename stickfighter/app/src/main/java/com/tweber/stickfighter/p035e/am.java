package com.tweber.stickfighter.p035e;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.tweber.stickfighter.activities.R;

/* JADX INFO: loaded from: classes.dex */
public class am extends RecyclerView.ViewHolder {

    /* JADX INFO: renamed from: a */
    public TextView f3925a;

    /* JADX INFO: renamed from: b */
    public ImageView f3926b;

    /* JADX INFO: renamed from: c */
    final /* synthetic */ aj f3927c;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public am(aj ajVar, View view) {
        super(view);
        this.f3927c = ajVar;
        this.f3925a = (TextView) view.findViewById(R.id.titleTextView);
        this.f3926b = (ImageView) view.findViewById(R.id.iconImageView);
    }
}
