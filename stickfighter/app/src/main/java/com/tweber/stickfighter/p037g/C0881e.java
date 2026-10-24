package com.tweber.stickfighter.p037g;

import android.graphics.PointF;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import com.tweber.stickfighter.StickFighterApplication;
import com.tweber.stickfighter.activities.C0802a;
import com.tweber.stickfighter.activities.C0808g;
import com.tweber.stickfighter.activities.R;
import com.tweber.stickfighter.p032b.C0809a;
import com.tweber.stickfighter.p034d.C0836a;
import com.tweber.stickfighter.p035e.af;
import com.tweber.stickfighter.p035e.an;
import com.tweber.stickfighter.p036f.C0869e;
import com.tweber.stickfighter.p038h.AbstractC0903a;
import com.tweber.stickfighter.p038h.AbstractC0908f;
import com.tweber.stickfighter.p038h.C0905c;
import com.tweber.stickfighter.p038h.C0912j;
import com.tweber.stickfighter.p038h.C0914l;
import com.tweber.stickfighter.p038h.C0918p;
import com.tweber.stickfighter.p038h.C0924v;
import com.tweber.stickfighter.p038h.C0925w;
import com.tweber.stickfighter.p038h.EnumC0917o;
import com.tweber.stickfighter.p039i.AsyncTaskC0930e;
import com.tweber.stickfighter.views.EditorToolbar;
import com.tweber.stickfighter.views.InterfaceC0947k;
import java.util.Iterator;

/* JADX INFO: renamed from: com.tweber.stickfighter.g.e */
/* JADX INFO: loaded from: classes.dex */
public class C0881e extends Fragment implements an, InterfaceC0947k {

    /* JADX INFO: renamed from: d */
    af f4091d;

    /* JADX INFO: renamed from: e */
    ViewOnTouchListenerC0886j f4092e;

    /* JADX INFO: renamed from: f */
    C0918p f4093f;

    /* JADX INFO: renamed from: g */
    AbstractC0903a f4094g;

    /* JADX INFO: renamed from: i */
    RelativeLayout f4096i;

    /* JADX INFO: renamed from: k */
    SparseArray f4098k;

    /* JADX INFO: renamed from: l */
    PointF f4099l;

    /* JADX INFO: renamed from: n */
    EditorToolbar f4101n;

    /* JADX INFO: renamed from: o */
    ImageButton f4102o;

    /* JADX INFO: renamed from: p */
    Spinner f4103p;

    /* JADX INFO: renamed from: a */
    final float f4088a = StickFighterApplication.m4490a().getResources().getDimensionPixelSize(R.dimen.select_threshold);

    /* JADX INFO: renamed from: b */
    final float f4089b = StickFighterApplication.m4490a().getResources().getDimensionPixelSize(R.dimen.snap_threshold);

    /* JADX INFO: renamed from: c */
    final String f4090c = "SelectFigure";

    /* JADX INFO: renamed from: h */
    C0912j f4095h = null;

    /* JADX INFO: renamed from: j */
    boolean f4097j = false;

    /* JADX INFO: renamed from: m */
    C0925w f4100m = new C0925w();

    /* JADX INFO: renamed from: q */
    PointF f4104q = null;

    /* JADX INFO: renamed from: r */
    long f4105r = 0;

    /* JADX INFO: renamed from: s */
    boolean f4106s = false;

    /* JADX INFO: renamed from: t */
    AbstractC0903a f4107t = new C0883g(this);

    /* JADX INFO: renamed from: u */
    AbstractC0903a f4108u = new C0884h(this);

    /* JADX INFO: renamed from: v */
    AbstractC0903a f4109v = new C0885i(this);

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: renamed from: a */
    public C0808g m4764a(float f, float f2) {
        C0808g c0808g = null;
        Iterator it = this.f4093f.m4922a().iterator();
        while (it.hasNext()) {
            for (C0905c c0905c : (Iterable<C0905c>) ((AbstractC0908f) it.next()).m4872a()) {
                if (c0905c.m4858a()) {
                    float fM4887a = this.f4095h.m4887a(c0905c.m4863c());
                    float fM4889b = this.f4095h.m4889b(c0905c.m4865d());
                    float fSqrt = (float) Math.sqrt(Math.pow(fM4889b - f2, 2.0d) + Math.pow(fM4887a - f, 2.0d));
                    if (c0808g == null || fSqrt < c0808g.f3828d) {
                        c0808g = new C0808g(c0905c, fM4887a, fM4889b, fSqrt);
                    }
                    c0808g = c0808g;
                }
            }
        }
        return c0808g;
    }

    /* JADX INFO: renamed from: a */
    public static C0881e m4766a(int i) {
        C0881e c0881e = new C0881e();
        Bundle bundle = new Bundle();
        bundle.putInt("CustomFigureID", i);
        c0881e.setArguments(bundle);
        return c0881e;
    }

    /* JADX INFO: renamed from: a */
    private void m4768a(int i, boolean z) {
        this.f4099l = null;
        this.f4097j = false;
        this.f4093f = C0924v.m4932a().m4939f().m4539a(i);
        this.f4098k = new SparseArray();
        Iterator it = this.f4093f.m4922a().iterator();
        while (it.hasNext()) {
            for (C0905c c0905c : (Iterable<C0905c>) ((AbstractC0908f) it.next()).m4872a()) {
                this.f4098k.put(c0905c.f4164a, c0905c);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: renamed from: b */
    public void m4772b() {
        this.f4092e.invalidate();
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: renamed from: b */
    public void m4773b(int i) {
        C0802a.f3819a = i;
        switch (i) {
            case 0:
                this.f4094g = this.f4108u;
                break;
            case 1:
                this.f4094g = this.f4107t;
                break;
            case 2:
                this.f4094g = this.f4109v;
                break;
        }
        this.f4101n.setActiveActionMode(i);
    }

    /* JADX INFO: renamed from: b */
    private boolean m4774b(float f, float f2) {
        if (System.currentTimeMillis() - this.f4105r > 300) {
            return false;
        }
        return Math.abs(this.f4104q.x - f) <= 10.0f && Math.abs(this.f4104q.y - f2) <= 10.0f;
    }

    /* JADX INFO: renamed from: c */
    private void m4776c() {
        if (this.f4091d == null) {
            this.f4091d = new af();
            this.f4091d.m4602a(EnumC0917o.Primitives);
        }
        this.f4091d.show(getChildFragmentManager(), "SelectFigure");
    }

    /* JADX INFO: renamed from: f */
    private void m4780f(View view) {
        this.f4103p.performClick();
    }

    /* JADX INFO: renamed from: a */
    public void m4784a() {
        if (this.f4106s) {
            return;
        }
        this.f4106s = true;
        this.f4101n.m4975f();
        getView().findViewById(R.id.saveProgressBar).setVisibility(0);
        new AsyncTaskC0930e(this.f4093f).execute(new Void[0]);
    }

    @Override // com.tweber.stickfighter.p035e.an
    /* JADX INFO: renamed from: a */
    public void mo4607a(long j) {
        C0914l.m4895a(j, getActivity(), this.f4095h, this.f4093f, this.f4098k);
        m4772b();
    }

    @Override // com.tweber.stickfighter.views.InterfaceC0947k
    /* JADX INFO: renamed from: a */
    public void mo4785a(View view) {
        m4780f(view);
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    @Override // com.tweber.stickfighter.views.InterfaceC0947k
    /* JADX INFO: renamed from: a */
    public boolean mo4786a(MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case 0:
                this.f4097j = true;
                this.f4099l = new PointF(motionEvent.getRawX(), motionEvent.getRawY());
                this.f4105r = System.currentTimeMillis();
                this.f4104q = new PointF(motionEvent.getRawX(), motionEvent.getRawY());
                return true;
            case 1:
                this.f4097j = false;
                if (m4774b(motionEvent.getRawX(), motionEvent.getRawY())) {
                }
                return true;
            case 2:
                if (this.f4097j) {
                    float rawX = motionEvent.getRawX();
                    float rawY = motionEvent.getRawY();
                    float f = rawX - this.f4099l.x;
                    float f2 = rawY - this.f4099l.y;
                    this.f4099l.x = rawX;
                    this.f4099l.y = rawY;
                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.f4101n.getLayoutParams();
                    layoutParams.leftMargin += (int) f;
                    layoutParams.topMargin += (int) f2;
                    this.f4101n.setLayoutParams(layoutParams);
                    this.f4101n.invalidate();
                }
                return true;
            default:
                return true;
        }
    }

    @Override // com.tweber.stickfighter.views.InterfaceC0947k
    /* JADX INFO: renamed from: b */
    public void mo4787b(View view) {
        m4776c();
        m4772b();
    }

    @Override // com.tweber.stickfighter.views.InterfaceC0947k
    /* JADX INFO: renamed from: c */
    public void mo4788c(View view) {
        throw new RuntimeException("Next frame should not be showing in custom figure editor");
    }

    @Override // com.tweber.stickfighter.views.InterfaceC0947k
    /* JADX INFO: renamed from: d */
    public void mo4789d(View view) {
        throw new RuntimeException("Previous frame should not be showing in custom figure editor");
    }

    @Override // com.tweber.stickfighter.views.InterfaceC0947k
    /* JADX INFO: renamed from: e */
    public void mo4790e(View view) {
        this.f4101n.m4968a();
        new AsyncTaskC0930e(this.f4093f).execute(new Void[0]);
    }

    @Override // androidx.fragment.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        de.greenrobot.event.EventBus.getDefault().register(this);
        C0924v.m4933a(C0836a.m4547a().mo4551a(getArguments().getInt("CustomFigureID", -1)));
    }

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View viewInflate = layoutInflater.inflate(R.layout.fragment_edit_frame, viewGroup, false);
        this.f4096i = (RelativeLayout) viewInflate.findViewById(R.id.FrameEditorLayout);
        this.f4092e = new ViewOnTouchListenerC0886j(this, getActivity());
        this.f4096i.addView(this.f4092e, 0);
        this.f4101n = (EditorToolbar) viewInflate.findViewById(R.id.EditFrameToolbar);
        this.f4101n.m4977h();
        this.f4101n.setOnInteractionListener(this);
        this.f4102o = (ImageButton) viewInflate.findViewById(R.id.DeleteAreaView);
        m4773b(0);
        this.f4103p = (Spinner) viewInflate.findViewById(R.id.actionModeSpinner);
        this.f4103p.setAdapter((SpinnerAdapter) new C0809a());
        this.f4103p.setOnItemSelectedListener(new C0882f(this));
        m4768a(0, false);
        return viewInflate;
    }

    @Override // androidx.fragment.app.Fragment
    public void onDestroy() {
        super.onDestroy();
        de.greenrobot.event.EventBus.getDefault().unregister(this);
    }

    public void onEvent(C0869e c0869e) {
        getActivity().finish();
    }
}
