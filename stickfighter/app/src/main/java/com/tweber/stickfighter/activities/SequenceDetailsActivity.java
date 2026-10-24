package com.tweber.stickfighter.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import android.view.Menu;
import android.view.MenuItem;
import com.tweber.stickfighter.p031a.C0800a;
import com.tweber.stickfighter.p034d.C0836a;
import com.tweber.stickfighter.p035e.C0855q;
import com.tweber.stickfighter.p036f.C0866b;
import com.tweber.stickfighter.p036f.EnumC0867c;
import com.tweber.stickfighter.p037g.C0898v;
import com.tweber.stickfighter.p037g.aa;
import com.tweber.stickfighter.p038h.C0924v;

/* JADX INFO: loaded from: classes.dex */
public class SequenceDetailsActivity extends AbstractActivityC0804c {
    /* JADX INFO: renamed from: c */
    private void m4502c() {
        if (false) {
            ActivityCompat.requestPermissions(this, new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 13);
        } else {
            new C0855q().show(getSupportFragmentManager(), "ExportDialogTag");
        }
    }

    @Override // com.tweber.stickfighter.activities.AbstractActivityC0804c
    /* JADX INFO: renamed from: a */
    protected int mo4497a() {
        return R.layout.activity_sequence_details;
    }

    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onActivityResult(int i, int i2, Intent intent) {
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment != null) {
                fragment.onActivityResult(i, i2, intent);
            }
        }
    }

    @Override // com.tweber.stickfighter.activities.AbstractActivityC0804c, androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, android.support.v4.app.AbstractActivityC0100x, android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        int intExtra = getIntent().getIntExtra("SequenceId", -1);
        C0924v c0924vMo4551a = C0836a.m4547a().mo4551a(intExtra);
        C0924v.m4933a(c0924vMo4551a);
        if (c0924vMo4551a.m4939f().m4538a() == 0) {
            c0924vMo4551a.m4939f().m4541a(C0836a.m4547a().mo4550a(intExtra, 0));
        }
        if (bundle == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.detailsFragmentContainer, aa.m4676a()).commit();
            getSupportFragmentManager().beginTransaction().replace(R.id.listFragmentContainer, C0898v.m4831a()).commit();
            C0800a.m4492a().m4496b();
        }
        m4504b().setTitle(R.string.app_name);
    }

    @Override // android.app.Activity
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sequence_details, menu);
        return true;
    }

    @Override // android.app.Activity
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                de.greenrobot.event.EventBus.getDefault().post(new C0866b(EnumC0867c.FRAME_DRAW));
                finish();
                return true;
            case R.id.export /* 2131493115 */:
                m4502c();
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity, android.support.v4.app.InterfaceC0079c
    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        switch (i) {
            case 13:
                if (iArr.length > 0 && iArr[0] == 0) {
                    m4502c();
                    break;
                }
                break;
        }
    }
}
