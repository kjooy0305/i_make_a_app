package com.tweber.stickfighter.p035e;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.tweber.stickfighter.activities.R;

/* JADX INFO: loaded from: classes.dex */
public class ai extends RecyclerView.ViewHolder {

    /* JADX INFO: renamed from: a */
    public TextView f3916a;

    /* JADX INFO: renamed from: b */
    public ImageView f3917b;

    /* JADX INFO: renamed from: c */
    final /* synthetic */ ag f3918c;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public ai(ag agVar, View view) {
        super(view);
        this.f3918c = agVar;
        this.f3916a = (TextView) view.findViewById(R.id.titleTextView);
        this.f3917b = (ImageView) view.findViewById(R.id.iconImageView);
    }
}
