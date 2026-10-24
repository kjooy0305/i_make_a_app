package com.tweber.stickfighter.activities;

import android.os.Bundle;
import com.tweber.stickfighter.p037g.ae;

/* JADX INFO: loaded from: classes.dex */
public class SequencePlayerActivity extends AbstractActivityC0804c {
    @Override // com.tweber.stickfighter.activities.AbstractActivityC0804c
    /* JADX INFO: renamed from: a */
    protected int mo4497a() {
        return R.layout.activity_sequence_player;
    }

    @Override // com.tweber.stickfighter.activities.AbstractActivityC0804c, androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, android.support.v4.app.AbstractActivityC0100x, android.app.Activity
    public void onCreate(Bundle bundle) {
        requestWindowFeature(1);
        getWindow().setFlags(1024, 1024);
        super.onCreate(bundle);
        getWindow().getDecorView().setSystemUiVisibility(1792);
        if (bundle == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.playerFragmentContainer, ae.m4698a()).commit();
        }
    }
}
