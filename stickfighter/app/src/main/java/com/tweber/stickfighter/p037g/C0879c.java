package com.tweber.stickfighter.p037g;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.SVBar;
import com.tweber.stickfighter.activities.R;

/* JADX INFO: renamed from: com.tweber.stickfighter.g.c */
/* JADX INFO: loaded from: classes.dex */
public class C0879c extends Fragment {

    /* JADX INFO: renamed from: a */
    ColorPicker f4083a;

    /* JADX INFO: renamed from: b */
    SVBar f4084b;

    /* JADX INFO: renamed from: c */
    int f4085c;

    /* JADX INFO: renamed from: a */
    public int m4758a() {
        return this.f4083a.getColor();
    }

    /* JADX INFO: renamed from: a */
    public void m4759a(int i) {
        this.f4085c = i;
        if (this.f4083a != null) {
            this.f4083a.setOldCenterColor(i);
            this.f4083a.setColor(i);
        }
    }

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View viewInflate = layoutInflater.inflate(R.layout.fragment_figure_change_color, viewGroup, false);
        this.f4083a = (ColorPicker) viewInflate.findViewById(R.id.colorPicker);
        this.f4084b = (SVBar) viewInflate.findViewById(R.id.svBar);
        this.f4083a.m4432a(this.f4084b);
        this.f4083a.setTouchAnywhereOnColorWheelEnabled(true);
        this.f4083a.setOldCenterColor(this.f4085c);
        this.f4083a.setColor(this.f4085c);
        return viewInflate;
    }
}
