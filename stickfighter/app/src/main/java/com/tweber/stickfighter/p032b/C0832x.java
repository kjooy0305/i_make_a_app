package com.tweber.stickfighter.p032b;

import android.database.DataSetObserver;
import android.media.CamcorderProfile;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import com.tweber.stickfighter.activities.R;
import java.util.ArrayList;

/* JADX INFO: renamed from: com.tweber.stickfighter.b.x */
/* JADX INFO: loaded from: classes.dex */
public class C0832x implements SpinnerAdapter {

    /* JADX INFO: renamed from: a */
    final ArrayList f3880a = new ArrayList();

    public C0832x() {
        m4537a(this.f3880a, 2, R.string.video_quality_qcif);
        m4537a(this.f3880a, 7, R.string.video_quality_qvga);
        m4537a(this.f3880a, 3, R.string.video_quality_cif);
        m4537a(this.f3880a, 4, R.string.video_quality_480p);
        m4537a(this.f3880a, 5, R.string.video_quality_720p);
        m4537a(this.f3880a, 6, R.string.video_quality_1080p);
        if (this.f3880a.isEmpty()) {
            this.f3880a.add(new C0833y(4, R.string.video_quality_480p));
            this.f3880a.add(new C0833y(5, R.string.video_quality_720p));
            this.f3880a.add(new C0833y(6, R.string.video_quality_1080p));
        }
    }

    /* JADX INFO: renamed from: a */
    private void m4537a(ArrayList arrayList, int i, int i2) {
        if (CamcorderProfile.hasProfile(i)) {
            arrayList.add(new C0833y(i, i2));
        }
    }

    @Override // android.widget.Adapter
    public int getCount() {
        return this.f3880a.size();
    }

    @Override // android.widget.SpinnerAdapter
    public View getDropDownView(int i, View view, ViewGroup viewGroup) {
        View viewInflate = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.spinner_dropdown_item_video_quality, viewGroup, false);
        ((TextView) viewInflate.findViewById(R.id.titleTextView)).setText(((C0833y) this.f3880a.get(i)).f3882b);
        return viewInflate;
    }

    @Override // android.widget.Adapter
    public Object getItem(int i) {
        return this.f3880a.get(i);
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
        View viewInflate = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.spinner_item_video_quality, viewGroup, false);
        ((TextView) viewInflate.findViewById(R.id.titleTextView)).setText(((C0833y) this.f3880a.get(i)).f3882b);
        return viewInflate;
    }

    @Override // android.widget.Adapter
    public int getViewTypeCount() {
        return 1;
    }

    @Override // android.widget.Adapter
    public boolean hasStableIds() {
        return false;
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
