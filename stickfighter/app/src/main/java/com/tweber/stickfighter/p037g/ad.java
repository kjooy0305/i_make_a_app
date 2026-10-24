package com.tweber.stickfighter.p037g;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.tweber.stickfighter.activities.AboutActivity;
import com.tweber.stickfighter.activities.HelpActivity;
import com.tweber.stickfighter.activities.R;
import com.tweber.stickfighter.activities.SequenceDetailsActivity;
import com.tweber.stickfighter.p032b.C0826r;
import com.tweber.stickfighter.p034d.C0836a;
import com.tweber.stickfighter.p035e.C0860v;
import com.tweber.stickfighter.p035e.ao;
import com.tweber.stickfighter.p036f.C0865a;
import com.tweber.stickfighter.p036f.C0871g;
import com.tweber.stickfighter.p036f.C0873i;
import com.tweber.stickfighter.p036f.C0875k;
import com.tweber.stickfighter.p036f.C0876l;
import com.tweber.stickfighter.p038h.C0924v;
import com.tweber.stickfighter.p040j.C0935d;
import java.util.ArrayList;

/* JADX INFO: loaded from: classes.dex */
public class ad extends Fragment {

    /* JADX INFO: renamed from: a */
    RecyclerView f4010a;

    /* JADX INFO: renamed from: b */
    ArrayList f4011b;

    /* JADX INFO: renamed from: c */
    boolean f4012c = false;

    /* JADX INFO: renamed from: d */
    Uri f4013d;

    /* JADX INFO: renamed from: a */
    public static ad m4679a(Uri uri) {
        ad adVar = new ad();
        Bundle bundle = new Bundle();
        if (uri != null) {
            bundle.putString("EXTRA_IMPORT_URI", uri.toString());
        }
        adVar.setArguments(bundle);
        return adVar;
    }

    /* JADX INFO: renamed from: a */
    private void m4680a(View view) {
        this.f4011b = C0836a.m4547a().mo4554a();
        if (this.f4011b.isEmpty()) {
            view.findViewById(R.id.noResultsView).setVisibility(0);
        }
        ((RecyclerView) view.findViewById(R.id.sequenceRecyclerView)).setAdapter(new C0826r(this.f4011b, m4681b(), getActivity()));
    }

    /* JADX INFO: renamed from: b */
    private int m4681b() {
        int i = getResources().getConfiguration().orientation;
        if (C0935d.m4960a((Context) getActivity())) {
            return i == 2 ? 3 : 2;
        }
        return i != 2 ? 1 : 2;
    }

    /* JADX INFO: renamed from: b */
    private void m4682b(Uri uri) {
        C0860v.m4653a(uri).show(getFragmentManager(), (String) null);
    }

    /* JADX INFO: renamed from: c */
    private void m4683c() {
        startActivity(new Intent(getActivity(), (Class<?>) AboutActivity.class));
    }

    /* JADX INFO: renamed from: d */
    private void m4684d() {
        startActivity(new Intent(getActivity(), (Class<?>) HelpActivity.class));
    }

    /* JADX INFO: renamed from: e */
    private void m4685e() {
        if (false) {
            requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 12);
            return;
        }
        Intent intent = new Intent("android.intent.action.OPEN_DOCUMENT");
        intent.addCategory("android.intent.category.OPENABLE");
        intent.setType("*/*");
        getActivity().startActivityForResult(intent, 5);
    }

    /* JADX INFO: renamed from: a */
    public void m4686a() {
        new ao().show(getChildFragmentManager(), (String) null);
    }

    /* JADX INFO: renamed from: a */
    public void m4687a(String str) {
        C0924v c0924vMo4552a = C0836a.m4547a().mo4552a(str, C0935d.m4956a((Activity) getActivity()));
        C0836a.m4547a().mo4550a(c0924vMo4552a.m4935b(), 0);
        this.f4011b.add(0, c0924vMo4552a);
        this.f4010a.getAdapter().notifyItemInserted(0);
        this.f4010a.scrollToPosition(0);
        getView().findViewById(R.id.noResultsView).setVisibility(8);
        Intent intent = new Intent(getActivity(), (Class<?>) SequenceDetailsActivity.class);
        intent.putExtra("SequenceId", c0924vMo4552a.m4935b());
        getActivity().startActivityForResult(intent, 99);
    }

    @Override // androidx.fragment.app.Fragment
    public void onActivityResult(int i, int i2, Intent intent) {
        int iIndexOf;
        if (i == 5) {
            if (i2 == -1) {
                this.f4013d = intent.getData();
                this.f4012c = true;
                return;
            }
            return;
        }
        if (i != 99 || (iIndexOf = this.f4011b.indexOf(C0924v.m4932a())) < 0) {
            return;
        }
        C0924v c0924v = (C0924v) this.f4011b.get(iIndexOf);
        c0924v.m4934a(C0924v.m4932a().m4938e());
        c0924v.m4939f().m4542b();
        C0924v.m4933a((C0924v) null);
        this.f4010a.getAdapter().notifyItemRangeChanged(0, this.f4011b.size());
    }

    @Override // androidx.fragment.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setHasOptionsMenu(true);
        this.f4011b = new ArrayList();
        de.greenrobot.event.EventBus.getDefault().register(this);
        if (bundle != null || getArguments().getString("EXTRA_IMPORT_URI") == null) {
            return;
        }
        this.f4013d = Uri.parse(getArguments().getString("EXTRA_IMPORT_URI"));
        this.f4012c = true;
    }

    @Override // androidx.fragment.app.Fragment
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.sequence_list, menu);
    }

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View viewInflate = layoutInflater.inflate(R.layout.fragment_sequence_list, viewGroup, false);
        this.f4010a = (RecyclerView) viewInflate.findViewById(R.id.sequenceRecyclerView);
        this.f4010a.setLayoutManager(new GridLayoutManager((Context) getActivity(), m4681b(), 1, false));
        this.f4010a.setItemAnimator(new DefaultItemAnimator());
        m4680a(viewInflate);
        return viewInflate;
    }

    @Override // androidx.fragment.app.Fragment
    public void onDestroy() {
        super.onDestroy();
        de.greenrobot.event.EventBus.getDefault().unregister(this);
    }

    public void onEvent(C0865a c0865a) {
        if (getView() == null) {
            return;
        }
        if (c0865a.m4666c()) {
            Toast.makeText(getActivity(), "File created at: " + c0865a.m4664a(), 1).show();
        } else {
            Toast.makeText(getActivity(), "ERROR: " + c0865a.m4665b(), 1).show();
        }
    }

    public void onEvent(C0871g c0871g) {
        if (getView() == null) {
            return;
        }
        C0924v c0924v = (C0924v) this.f4011b.get(0);
        FragmentActivity activity = getActivity();
        Intent intent = new Intent(activity, (Class<?>) SequenceDetailsActivity.class);
        intent.putExtra("SequenceId", c0924v.m4935b());
        activity.startActivityForResult(intent, 99);
    }

    public void onEvent(C0873i c0873i) {
        int iIndexOf = this.f4011b.indexOf(c0873i.m4668a());
        if (iIndexOf < 0) {
            return;
        }
        this.f4011b.remove(iIndexOf);
        View view = getView();
        if (view != null) {
            Snackbar.make(getActivity().findViewById(R.id.coordinatorLayout), getString(R.string.sequence_deleted, c0873i.m4668a().m4936c()), 0).show();
            ((RecyclerView) view.findViewById(R.id.sequenceRecyclerView)).getAdapter().notifyItemRemoved(iIndexOf);
            if (this.f4011b.isEmpty()) {
                view.findViewById(R.id.noResultsView).setVisibility(0);
            }
        }
    }

    public void onEvent(C0875k c0875k) {
        if (this.f4010a == null || getView() == null) {
            return;
        }
        this.f4011b.add(0, c0875k.m4669a());
        this.f4010a.getAdapter().notifyItemInserted(0);
        this.f4010a.scrollToPosition(0);
        getView().findViewById(R.id.noResultsView).setVisibility(8);
    }

    public void onEvent(C0876l c0876l) {
        if (getView() == null) {
            return;
        }
        if (c0876l.m4672c()) {
            Toast.makeText(getActivity(), "Video created at: " + c0876l.m4670a(), 1).show();
        } else {
            Toast.makeText(getActivity(), "ERROR: " + c0876l.m4671b(), 1).show();
        }
    }

    @Override // androidx.fragment.app.Fragment
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.import_animation /* 2131493116 */:
                m4685e();
                return true;
            case R.id.help /* 2131493117 */:
                m4684d();
                return true;
            case R.id.about /* 2131493118 */:
                m4683c();
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    @Override // androidx.fragment.app.Fragment
    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        switch (i) {
            case 12:
                if (iArr.length > 0 && iArr[0] == 0) {
                    m4685e();
                    break;
                }
                break;
        }
    }

    @Override // androidx.fragment.app.Fragment
    public void onResume() {
        super.onResume();
        if (this.f4012c) {
            this.f4012c = false;
            m4682b(this.f4013d);
        }
    }
}
