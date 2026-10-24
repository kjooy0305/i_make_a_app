package com.tweber.stickfighter.p032b;

import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import com.tweber.stickfighter.activities.C0802a;
import com.tweber.stickfighter.activities.R;

/* JADX INFO: renamed from: com.tweber.stickfighter.b.a */
/* JADX INFO: loaded from: classes.dex */
public class C0809a implements SpinnerAdapter {

    /* JADX INFO: renamed from: a */
    int[] f3829a = {0, 1, 2};

    @Override // android.widget.Adapter
    public int getCount() {
        return this.f3829a.length;
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    @Override // android.widget.SpinnerAdapter
    public View getDropDownView(int i, View view, ViewGroup viewGroup) {
        View viewInflate = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.spinner_entry_action_mode, viewGroup, false);
        ImageView imageView = (ImageView) viewInflate.findViewById(R.id.iconImageView);
        TextView textView = (TextView) viewInflate.findViewById(R.id.actionModeTextView);
        View viewFindViewById = viewInflate.findViewById(R.id.actionModeCircleLeft);
        View viewFindViewById2 = viewInflate.findViewById(R.id.actionModeCircleRight);
        ImageView imageView2 = (ImageView) viewInflate.findViewById(R.id.imageViewHelp);
        if (C0802a.f3819a == i) {
            viewInflate.setBackgroundColor(viewInflate.getContext().getResources().getColor(R.color.light_gray));
        }
        switch (i) {
            case 0:
                imageView.setImageResource(R.drawable.ic_action_open_with_black24dp);
                textView.setText(R.string.action_mode_translate_pivot);
                viewFindViewById.setBackgroundResource(R.drawable.action_mode_circle_translate);
                viewFindViewById2.setBackgroundResource(R.drawable.action_mode_circle_move);
                imageView2.setOnClickListener(new ViewOnClickListenerC0810b(this));
                return viewInflate;
            case 1:
                imageView.setImageResource(R.drawable.ic_action_photo_size_select_large_black24dp);
                textView.setText(R.string.action_mode_stretch_modify);
                viewFindViewById.setBackgroundResource(R.drawable.action_mode_circle_modify);
                viewFindViewById2.setBackgroundResource(R.drawable.action_mode_circle_stretch);
                imageView2.setOnClickListener(new ViewOnClickListenerC0811c(this));
                return viewInflate;
            case 2:
                imageView.setImageResource(R.drawable.ic_action_flip_black24dp);
                textView.setText(R.string.action_mode_reflect);
                viewFindViewById.setBackgroundResource(R.drawable.action_mode_circle_reflect);
                imageView2.setOnClickListener(new ViewOnClickListenerC0812d(this));
                return viewInflate;
            default:
                return viewInflate;
        }
    }

    @Override // android.widget.Adapter
    public Object getItem(int i) {
        return Integer.valueOf(this.f3829a[i]);
    }

    @Override // android.widget.Adapter
    public long getItemId(int i) {
        return i;
    }

    @Override // android.widget.Adapter
    public int getItemViewType(int i) {
        return 0;
    }

    @Override // android.widget.Adapter
    public View getView(int i, View view, ViewGroup viewGroup) {
        return LayoutInflater.from(viewGroup.getContext()).inflate(android.R.layout.simple_spinner_dropdown_item, viewGroup, false);
    }

    @Override // android.widget.Adapter
    public int getViewTypeCount() {
        return 1;
    }

    @Override // android.widget.Adapter
    public boolean hasStableIds() {
        return true;
    }

    @Override // android.widget.Adapter
    public boolean isEmpty() {
        return false;
    }

    @Override // android.widget.Adapter
    public void registerDataSetObserver(DataSetObserver dataSetObserver) {
    }

    @Override // android.widget.Adapter
    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {
    }
}
