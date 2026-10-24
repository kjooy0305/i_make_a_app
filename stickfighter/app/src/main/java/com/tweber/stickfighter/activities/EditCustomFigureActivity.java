package com.tweber.stickfighter.activities;

import android.os.Bundle;
import com.tweber.stickfighter.p037g.C0881e;

/* JADX INFO: loaded from: classes.dex */
public class EditCustomFigureActivity extends AbstractActivityC0804c {
    @Override // com.tweber.stickfighter.activities.AbstractActivityC0804c
    /* JADX INFO: renamed from: a */
    protected int mo4497a() {
        return R.layout.activity_edit_custom_figure;
    }

    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    public void onBackPressed() {
        ((C0881e) getSupportFragmentManager().findFragmentById(R.id.editFragmentContainer)).m4784a();
    }

    @Override // com.tweber.stickfighter.activities.AbstractActivityC0804c, androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, android.support.v4.app.AbstractActivityC0100x, android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getWindow().getDecorView().setSystemUiVisibility(5894);
        if (bundle == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.editFragmentContainer, C0881e.m4766a(getIntent().getIntExtra("CustomFigureID", -1))).commit();
        }
    }

    @Override // android.app.Activity, android.view.Window.Callback
    public void onWindowFocusChanged(boolean z) {
        super.onWindowFocusChanged(z);
        if (z) {
            getWindow().getDecorView().setSystemUiVisibility(5894);
        }
    }
}
