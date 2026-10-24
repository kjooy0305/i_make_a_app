package com.tweber.stickfighter.p037g;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import com.tweber.stickfighter.activities.R;

/* JADX INFO: renamed from: com.tweber.stickfighter.g.w */
/* JADX INFO: loaded from: classes.dex */
public class C0899w extends Fragment {

    /* JADX INFO: renamed from: a */
    final String f4157a = "100";

    /* JADX INFO: renamed from: b */
    EditText f4158b;

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: renamed from: a */
    public void m4835a(float f) {
        this.f4158b.setText(String.valueOf((int) (m4837a() + f)));
    }

    /* JADX INFO: renamed from: a */
    public int m4837a() {
        try {
            return Integer.parseInt(this.f4158b.getText().toString());
        } catch (Exception e) {
            return 100;
        }
    }

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View viewInflate = layoutInflater.inflate(R.layout.fragment_figure_scale, viewGroup, false);
        this.f4158b = (EditText) viewInflate.findViewById(R.id.scaleFactorEditText);
        this.f4158b.setText("100");
        this.f4158b.addTextChangedListener(new C0900x(this));
        viewInflate.findViewById(R.id.increaseButton).setOnClickListener(new ViewOnClickListenerC0901y(this));
        viewInflate.findViewById(R.id.decreaseButton).setOnClickListener(new ViewOnClickListenerC0902z(this));
        return viewInflate;
    }
}
