package com.tweber.stickfighter.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

/* JADX INFO: renamed from: com.tweber.stickfighter.activities.c */
/* JADX INFO: loaded from: classes.dex */
public abstract class AbstractActivityC0804c extends AppCompatActivity {

    /* JADX INFO: renamed from: a */
    Toolbar f3820a;

    /* JADX INFO: renamed from: a */
    protected abstract int mo4497a();

    /* JADX INFO: renamed from: b */
    protected Toolbar m4504b() {
        return this.f3820a;
    }

    @Override // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, android.support.v4.app.AbstractActivityC0100x, android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(mo4497a());
        this.f3820a = (Toolbar) findViewById(R.id.toolbar);
        if (this.f3820a != null) {
            setSupportActionBar(this.f3820a);
        }
    }
}
