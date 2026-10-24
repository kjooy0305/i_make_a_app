package com.tweber.stickfighter.p037g;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import com.tweber.stickfighter.StickFighterApplication;
import com.tweber.stickfighter.activities.C0802a;
import com.tweber.stickfighter.activities.C0808g;
import com.tweber.stickfighter.activities.R;
import com.tweber.stickfighter.p031a.C0800a;
import com.tweber.stickfighter.p032b.C0809a;
import com.tweber.stickfighter.p035e.af;
import com.tweber.stickfighter.p035e.an;
import com.tweber.stickfighter.p036f.C0869e;
import com.tweber.stickfighter.p036f.C0870f;
import com.tweber.stickfighter.p036f.C0872h;
import com.tweber.stickfighter.p038h.AbstractC0903a;
import com.tweber.stickfighter.p038h.AbstractC0908f;
import com.tweber.stickfighter.p038h.C0905c;
import com.tweber.stickfighter.p038h.C0912j;
import com.tweber.stickfighter.p038h.C0914l;
import com.tweber.stickfighter.p038h.C0918p;
import com.tweber.stickfighter.p038h.C0924v;
import com.tweber.stickfighter.p038h.C0925w;
import com.tweber.stickfighter.p038h.EnumC0917o;
import com.tweber.stickfighter.p039i.AsyncTaskC0928c;
import com.tweber.stickfighter.p039i.AsyncTaskC0929d;
import com.tweber.stickfighter.p039i.AsyncTaskC0930e;
import com.tweber.stickfighter.views.EditorToolbar;
import com.tweber.stickfighter.views.InterfaceC0947k;
import java.util.Iterator;

/* JADX INFO: renamed from: com.tweber.stickfighter.g.k */
/* JADX INFO: loaded from: classes.dex */
public class C0887k extends Fragment implements an, InterfaceC0947k {

    /* JADX INFO: renamed from: c */
    af f4120c;

    /* JADX INFO: renamed from: d */
    ViewOnTouchListenerC0897u f4121d;

    /* JADX INFO: renamed from: e */
    C0918p f4122e;

    /* JADX INFO: renamed from: f */
    AbstractC0903a f4123f;

    /* JADX INFO: renamed from: h */
    RelativeLayout f4125h;

    /* JADX INFO: renamed from: j */
    SparseArray f4127j;

    /* JADX INFO: renamed from: k */
    PointF f4128k;

    /* JADX INFO: renamed from: m */
    EditorToolbar f4130m;

    /* JADX INFO: renamed from: n */
    ImageButton f4131n;

    /* JADX INFO: renamed from: o */
    Spinner f4132o;

    /* JADX INFO: renamed from: p */
    View f4133p;

    /* JADX INFO: renamed from: q */
    View f4134q;

    /* JADX INFO: renamed from: r */
    ImageView f4135r;

    /* JADX INFO: renamed from: s */
    Bitmap f4136s;

    /* JADX INFO: renamed from: t */
    View f4137t;

    /* JADX INFO: renamed from: u */
    ImageView f4138u;

    /* JADX INFO: renamed from: v */
    Bitmap f4139v;

    /* JADX INFO: renamed from: a */
    final String f4118a = "SelectFigure";

    /* JADX INFO: renamed from: b */
    final float f4119b = StickFighterApplication.m4490a().getResources().getDimensionPixelSize(R.dimen.select_threshold);

    /* JADX INFO: renamed from: g */
    C0912j f4124g = null;

    /* JADX INFO: renamed from: i */
    boolean f4126i = false;

    /* JADX INFO: renamed from: l */
    C0925w f4129l = new C0925w();

    /* JADX INFO: renamed from: w */
    PointF f4140w = null;

    /* JADX INFO: renamed from: x */
    long f4141x = 0;

    /* JADX INFO: renamed from: y */
    boolean f4142y = false;

    /* JADX INFO: renamed from: z */
    AbstractC0903a f4143z = new C0893q(this);

    /* JADX INFO: renamed from: A */
    AbstractC0903a f4116A = new C0894r(this);

    /* JADX INFO: renamed from: B */
    AbstractC0903a f4117B = new C0896t(this);

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: renamed from: a */
    public C0808g m4801a(float f, float f2) {
        C0808g c0808g = null;
        Iterator it = this.f4122e.m4922a().iterator();
        while (it.hasNext()) {
            for (C0905c c0905c : (Iterable<C0905c>) ((AbstractC0908f) it.next()).m4872a()) {
                if (c0905c.m4858a()) {
                    float fM4887a = this.f4124g.m4887a(c0905c.m4863c());
                    float fM4889b = this.f4124g.m4889b(c0905c.m4865d());
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
    public static C0887k m4803a(int i) {
        C0887k c0887k = new C0887k();
        Bundle bundle = new Bundle();
        bundle.putInt("Position", i);
        c0887k.setArguments(bundle);
        return c0887k;
    }

    /* JADX INFO: renamed from: a */
    private void m4805a(int i, boolean z) {
        int iM4927b = this.f4122e == null ? -1 : this.f4122e.m4927b();
        if (this.f4122e != null && this.f4136s == null) {
            int width = this.f4135r.getWidth();
            int height = this.f4135r.getHeight();
            this.f4136s = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            this.f4139v = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        }
        if (z) {
            if (i > iM4927b) {
                this.f4121d.draw(new Canvas(this.f4136s));
                this.f4135r.setImageBitmap(this.f4136s);
            } else {
                this.f4121d.draw(new Canvas(this.f4139v));
                this.f4138u.setImageBitmap(this.f4139v);
            }
        }
        this.f4128k = null;
        this.f4126i = false;
        this.f4122e = C0924v.m4932a().m4939f().m4539a(i);
        if (z) {
            if (i > iM4927b) {
                this.f4121d.draw(new Canvas(this.f4139v));
                this.f4138u.setImageBitmap(this.f4139v);
            } else {
                this.f4121d.draw(new Canvas(this.f4136s));
                this.f4135r.setImageBitmap(this.f4136s);
            }
        }
        this.f4127j = new SparseArray();
        Iterator it = this.f4122e.m4922a().iterator();
        while (it.hasNext()) {
            for (C0905c c0905c : (Iterable<C0905c>) ((AbstractC0908f) it.next()).m4872a()) {
                this.f4127j.put(c0905c.f4164a, c0905c);
            }
        }
        if (this.f4122e.m4927b() > 0) {
            this.f4130m.m4969a(z);
        } else {
            this.f4130m.m4971b(z);
        }
        if (z) {
            if (iM4927b < i) {
                m4809b();
            } else if (iM4927b > i) {
                m4813c();
            }
        }
    }

    /* JADX INFO: renamed from: b */
    private void m4809b() {
        Animation animationLoadAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.frame_inward);
        animationLoadAnimation.setAnimationListener(new AnimationAnimationListenerC0889m(this));
        this.f4134q.startAnimation(animationLoadAnimation);
        Animation animationLoadAnimation2 = AnimationUtils.loadAnimation(getActivity(), R.anim.frame_slide_in_from_right);
        animationLoadAnimation2.setAnimationListener(new AnimationAnimationListenerC0890n(this));
        this.f4137t.startAnimation(animationLoadAnimation2);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: renamed from: b */
    public void m4810b(int i) {
        C0802a.f3819a = i;
        switch (i) {
            case 0:
                this.f4123f = this.f4116A;
                break;
            case 1:
                this.f4123f = this.f4143z;
                break;
            case 2:
                this.f4123f = this.f4117B;
                break;
        }
        this.f4130m.setActiveActionMode(i);
    }

    /* JADX INFO: renamed from: b */
    private boolean m4811b(float f, float f2) {
        if (System.currentTimeMillis() - this.f4141x > 300) {
            return false;
        }
        return Math.abs(this.f4140w.x - f) <= 10.0f && Math.abs(this.f4140w.y - f2) <= 10.0f;
    }

    /* JADX INFO: renamed from: c */
    private void m4813c() {
        Animation animationLoadAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.frame_outward);
        animationLoadAnimation.setAnimationListener(new AnimationAnimationListenerC0891o(this));
        this.f4134q.startAnimation(animationLoadAnimation);
        Animation animationLoadAnimation2 = AnimationUtils.loadAnimation(getActivity(), R.anim.frame_slide_out_to_left);
        animationLoadAnimation2.setAnimationListener(new AnimationAnimationListenerC0892p(this));
        this.f4137t.startAnimation(animationLoadAnimation2);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: renamed from: d */
    public void m4815d() {
        this.f4121d.invalidate();
    }

    /* JADX INFO: renamed from: e */
    private void m4817e() {
        if (this.f4120c == null) {
            this.f4120c = new af();
            this.f4120c.m4602a(EnumC0917o.All);
        }
        if (this.f4120c.isAdded()) {
            return;
        }
        this.f4120c.show(getChildFragmentManager(), "SelectFigure");
    }

    /* JADX INFO: renamed from: f */
    private void m4819f(View view) {
        this.f4132o.performClick();
    }

    /* JADX INFO: renamed from: a */
    public void m4826a() {
        if (this.f4142y) {
            return;
        }
        this.f4142y = true;
        this.f4130m.m4975f();
        getView().findViewById(R.id.saveProgressBar).setVisibility(0);
        new AsyncTaskC0930e(this.f4122e).execute(new Void[0]);
    }

    @Override // com.tweber.stickfighter.p035e.an
    /* JADX INFO: renamed from: a */
    public void mo4607a(long j) {
        C0914l.m4895a(j, getActivity(), this.f4124g, this.f4122e, this.f4127j);
        m4815d();
    }

    @Override // com.tweber.stickfighter.views.InterfaceC0947k
    /* JADX INFO: renamed from: a */
    public void mo4785a(View view) {
        m4819f(view);
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    @Override // com.tweber.stickfighter.views.InterfaceC0947k
    /* JADX INFO: renamed from: a */
    public boolean mo4786a(MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case 0:
                this.f4126i = true;
                this.f4128k = new PointF(motionEvent.getRawX(), motionEvent.getRawY());
                this.f4141x = System.currentTimeMillis();
                this.f4140w = new PointF(motionEvent.getRawX(), motionEvent.getRawY());
                return true;
            case 1:
                this.f4126i = false;
                if (m4811b(motionEvent.getRawX(), motionEvent.getRawY())) {
                }
                return true;
            case 2:
                if (this.f4126i) {
                    float rawX = motionEvent.getRawX();
                    float rawY = motionEvent.getRawY();
                    float f = rawX - this.f4128k.x;
                    float f2 = rawY - this.f4128k.y;
                    this.f4128k.x = rawX;
                    this.f4128k.y = rawY;
                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.f4130m.getLayoutParams();
                    layoutParams.leftMargin += (int) f;
                    layoutParams.topMargin += (int) f2;
                    this.f4130m.setLayoutParams(layoutParams);
                    this.f4130m.invalidate();
                }
                return true;
            default:
                return true;
        }
    }

    @Override // com.tweber.stickfighter.views.InterfaceC0947k
    /* JADX INFO: renamed from: b */
    public void mo4787b(View view) {
        m4817e();
        m4815d();
    }

    @Override // com.tweber.stickfighter.views.InterfaceC0947k
    /* JADX INFO: renamed from: c */
    public void mo4788c(View view) {
        this.f4142y = true;
        this.f4130m.m4973d();
        this.f4130m.m4975f();
        new AsyncTaskC0928c(this.f4122e).execute(new Void[0]);
    }

    @Override // com.tweber.stickfighter.views.InterfaceC0947k
    /* JADX INFO: renamed from: d */
    public void mo4789d(View view) {
        if (this.f4122e.m4927b() == 0) {
            return;
        }
        this.f4142y = true;
        this.f4130m.m4970b();
        this.f4130m.m4975f();
        new AsyncTaskC0929d(this.f4122e).execute(new Void[0]);
    }

    @Override // com.tweber.stickfighter.views.InterfaceC0947k
    /* JADX INFO: renamed from: e */
    public void mo4790e(View view) {
        this.f4130m.m4968a();
        new AsyncTaskC0930e(this.f4122e).execute(new Void[0]);
    }

    @Override // androidx.fragment.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (bundle == null) {
            C0800a.m4492a();
        }
        de.greenrobot.event.EventBus.getDefault().register(this);
    }

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View viewInflate = layoutInflater.inflate(R.layout.fragment_edit_frame, viewGroup, false);
        this.f4125h = (RelativeLayout) viewInflate.findViewById(R.id.FrameEditorLayout);
        this.f4121d = new ViewOnTouchListenerC0897u(this, getActivity());
        this.f4125h.addView(this.f4121d, 0);
        this.f4130m = (EditorToolbar) viewInflate.findViewById(R.id.EditFrameToolbar);
        this.f4130m.setOnInteractionListener(this);
        this.f4131n = (ImageButton) viewInflate.findViewById(R.id.DeleteAreaView);
        this.f4133p = viewInflate.findViewById(R.id.backgroundCoverup);
        this.f4135r = (ImageView) viewInflate.findViewById(R.id.bottomCacheImageView);
        this.f4134q = viewInflate.findViewById(R.id.bottomCacheImageContainer);
        this.f4138u = (ImageView) viewInflate.findViewById(R.id.topCacheImageView);
        this.f4137t = viewInflate.findViewById(R.id.topCacheImageContainer);
        m4810b(0);
        this.f4132o = (Spinner) viewInflate.findViewById(R.id.actionModeSpinner);
        this.f4132o.setAdapter((SpinnerAdapter) new C0809a());
        this.f4132o.setOnItemSelectedListener(new C0888l(this));
        if (C0924v.m4932a() == null) {
            getActivity().finish();
            return null;
        }
        m4805a(bundle == null ? getArguments().getInt("Position", -1) : bundle.getInt("CurrentPosition"), false);
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

    public void onEvent(C0870f c0870f) {
        m4805a(this.f4122e.m4927b() + 1, true);
        this.f4142y = false;
        this.f4130m.m4976g();
        this.f4130m.m4974e();
        m4815d();
    }

    public void onEvent(C0872h c0872h) {
        m4805a(this.f4122e.m4927b() - 1, true);
        this.f4142y = false;
        this.f4130m.m4976g();
        this.f4130m.m4972c();
        m4815d();
    }

    @Override // androidx.fragment.app.Fragment
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putInt("CurrentPosition", this.f4122e.m4927b());
    }
}
