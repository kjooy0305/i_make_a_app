package com.tweber.stickfighter.activities;

import android.os.Bundle;
import com.tweber.stickfighter.p037g.C0877a;

/* JADX INFO: loaded from: classes.dex */
public class AddSequenceActivity extends AbstractActivityC0804c {
    @Override // com.tweber.stickfighter.activities.AbstractActivityC0804c
    /* JADX INFO: renamed from: a */
    protected int mo4497a() {
        return R.layout.activity_add_sequence;
    }

    @Override // com.tweber.stickfighter.activities.AbstractActivityC0804c, androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, android.support.v4.app.AbstractActivityC0100x, android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (bundle == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.addFragmentContainer, C0877a.m4673a()).commit();
        }
    }
}
