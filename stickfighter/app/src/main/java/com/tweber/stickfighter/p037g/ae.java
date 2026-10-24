package com.tweber.stickfighter.p037g;

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import com.tweber.stickfighter.StickFighterApplication;
import com.tweber.stickfighter.activities.R;
import com.tweber.stickfighter.p034d.C0836a;
import com.tweber.stickfighter.p038h.C0912j;
import com.tweber.stickfighter.p038h.C0918p;
import com.tweber.stickfighter.p038h.C0924v;
import com.tweber.stickfighter.p038h.InterfaceC0913k;
import com.tweber.stickfighter.p040j.C0935d;
import java.util.ArrayList;

/* JADX INFO: loaded from: classes.dex */
public class ae extends Fragment implements TextureView.SurfaceTextureListener {

    /* JADX INFO: renamed from: A */
    ImageView f4014A;

    /* JADX INFO: renamed from: B */
    View f4015B;

    /* JADX INFO: renamed from: C */
    SeekBar f4016C;

    /* JADX INFO: renamed from: D */
    TextView f4017D;

    /* JADX INFO: renamed from: E */
    SeekBar f4018E;

    /* JADX INFO: renamed from: F */
    TextView f4019F;

    /* JADX INFO: renamed from: G */
    SwitchCompat f4020G;

    /* JADX INFO: renamed from: H */
    Button f4021H;

    /* JADX INFO: renamed from: I */
    Button f4022I;

    /* JADX INFO: renamed from: J */
    ImageView f4023J;

    /* JADX INFO: renamed from: L */
    volatile int f4025L;

    /* JADX INFO: renamed from: M */
    volatile int f4026M;

    /* JADX INFO: renamed from: j */
    int f4038j;

    /* JADX INFO: renamed from: k */
    boolean f4039k;

    /* JADX INFO: renamed from: l */
    C0924v f4040l;

    /* JADX INFO: renamed from: m */
    int f4041m;

    /* JADX INFO: renamed from: n */
    C0918p f4042n;

    /* JADX INFO: renamed from: o */
    TextureView f4043o;

    /* JADX INFO: renamed from: p */
    ba f4044p;

    /* JADX INFO: renamed from: q */
    volatile C0912j f4045q;

    /* JADX INFO: renamed from: r */
    Handler f4046r;

    /* JADX INFO: renamed from: s */
    Handler f4047s;

    /* JADX INFO: renamed from: t */
    Handler f4048t;

    /* JADX INFO: renamed from: u */
    View f4049u;

    /* JADX INFO: renamed from: v */
    ProgressBar f4050v;

    /* JADX INFO: renamed from: w */
    View f4051w;

    /* JADX INFO: renamed from: x */
    TextView f4052x;

    /* JADX INFO: renamed from: y */
    TextView f4053y;

    /* JADX INFO: renamed from: z */
    SeekBar f4054z;

    /* JADX INFO: renamed from: a */
    final String f4029a = "PlayerFPS";

    /* JADX INFO: renamed from: b */
    final String f4030b = "PlayerKeyFPS";

    /* JADX INFO: renamed from: c */
    final String f4031c = "PlayerRepeat";

    /* JADX INFO: renamed from: d */
    final int[] f4032d = {24, 36, 48, 60};

    /* JADX INFO: renamed from: e */
    final int[] f4033e = {1, 2, 3, 4, 6, 8};

    /* JADX INFO: renamed from: f */
    final String[] f4034f = {"0.5x", "1x", "1.5x", "2x", "3x", "4x"};

    /* JADX INFO: renamed from: g */
    final int f4035g = 3000;

    /* JADX INFO: renamed from: h */
    az f4036h = null;

    /* JADX INFO: renamed from: K */
    volatile boolean f4024K = false;

    /* JADX INFO: renamed from: i */
    int f4037i;

    /* JADX INFO: renamed from: N */
    ArrayList f4027N = new ArrayList(this.f4037i);

    /* JADX INFO: renamed from: O */
    ArrayList f4028O = new ArrayList(this.f4037i);

    /* JADX INFO: renamed from: E */
    static /* synthetic */ int m4692E(ae aeVar) {
        int i = aeVar.f4025L;
        aeVar.f4025L = i + 1;
        return i;
    }

    /* JADX INFO: renamed from: I */
    static /* synthetic */ int m4696I(ae aeVar) {
        int i = aeVar.f4026M;
        aeVar.f4026M = i + 1;
        return i;
    }

    /* JADX INFO: renamed from: a */
    public static ae m4698a() {
        return new ae();
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: renamed from: a */
    public void m4702a(int i, boolean z) {
        this.f4052x.setText(String.format("%d:%02d", Integer.valueOf(i / 60), Integer.valueOf(i % 60)));
        if (z) {
            this.f4054z.setMax(((int) Math.ceil(this.f4041m - 1)) / this.f4038j);
            this.f4054z.setProgress(i);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: renamed from: a */
    public void m4703a(Canvas canvas, InterfaceC0913k interfaceC0913k) {
        if (this.f4045q == null) {
            this.f4045q = new C0912j(this.f4040l.m4937d(), this.f4043o);
        }
        interfaceC0913k.mo4892a(this.f4045q, canvas, false, null, this.f4040l.m4938e());
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: renamed from: a */
    public void m4707a(boolean z) {
        this.f4051w.clearAnimation();
        m4724e();
        this.f4025L = 0;
        this.f4026M = 0;
        new ay(this, 0, this.f4027N).execute(new Void[0]);
        if (this.f4041m - 1 > this.f4038j || !this.f4039k) {
            new ay(this, this.f4038j, this.f4028O).execute(new Void[0]);
        } else {
            new ay(this, 0, this.f4028O).execute(new Void[0]);
        }
        if (z) {
            Canvas canvasLockCanvas = this.f4043o.lockCanvas();
            m4703a(canvasLockCanvas, this.f4042n);
            this.f4043o.unlockCanvasAndPost(canvasLockCanvas);
            m4702a(this.f4025L, true);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: renamed from: b */
    public void m4710b() {
        SharedPreferences sharedPreferencesM4491b = StickFighterApplication.m4491b();
        this.f4037i = this.f4032d[sharedPreferencesM4491b.getInt("PlayerFPS", 1)];
        this.f4038j = this.f4033e[sharedPreferencesM4491b.getInt("PlayerKeyFPS", 3)];
        this.f4039k = sharedPreferencesM4491b.getBoolean("PlayerRepeat", false);
        this.f4025L = 0;
        this.f4026M = 0;
        m4702a(this.f4025L, true);
        m4732i();
        new ay(this, 0, this.f4027N).execute(new Void[0]);
        if (this.f4041m - 1 > this.f4038j || !this.f4039k) {
            new ay(this, this.f4038j, this.f4028O).execute(new Void[0]);
        } else {
            new ay(this, 0, this.f4028O).execute(new Void[0]);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: renamed from: b */
    public void m4712b(boolean z) {
        getActivity().getWindow().getDecorView().setSystemUiVisibility(1792);
        m4718c(z);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: renamed from: c */
    public void m4715c() {
        if (this.f4024K) {
            m4724e();
        } else {
            m4721d();
        }
    }

    /* JADX INFO: renamed from: c */
    private void m4718c(boolean z) {
        if (this.f4051w.getVisibility() == 0) {
            return;
        }
        if (!z) {
            this.f4051w.setVisibility(0);
            return;
        }
        Animation animationLoadAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.video_toolbar_show);
        animationLoadAnimation.setAnimationListener(new am(this));
        this.f4051w.startAnimation(animationLoadAnimation);
    }

    /* JADX INFO: renamed from: d */
    private void m4721d() {
        this.f4024K = true;
        getActivity().getWindow().addFlags(128);
        this.f4044p = new ba(this, this.f4043o);
        this.f4044p.start();
        this.f4023J.setImageResource(R.drawable.ic_pause_white_48dp);
        m4702a(this.f4025L, true);
        m4726f();
    }

    /* JADX INFO: renamed from: d */
    private void m4722d(boolean z) {
        if (!z) {
            this.f4051w.setVisibility(4);
            return;
        }
        Animation animationLoadAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.video_toolbar_hide);
        animationLoadAnimation.setAnimationListener(new an(this));
        this.f4051w.startAnimation(animationLoadAnimation);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: renamed from: e */
    public void m4724e() {
        this.f4024K = false;
        getActivity().getWindow().clearFlags(128);
        if (this.f4044p != null) {
            this.f4044p.m4757a();
            this.f4044p = null;
        }
        this.f4023J.setImageResource(R.drawable.ic_action_play_arrow_white48dp);
        m4712b(true);
        this.f4048t.removeCallbacksAndMessages(null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: renamed from: f */
    public void m4726f() {
        this.f4048t.removeCallbacksAndMessages(null);
        getActivity().getWindow().getDecorView().setSystemUiVisibility(1798);
        m4722d(true);
        m4729h();
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: renamed from: g */
    public void m4728g() {
        m4724e();
        SharedPreferences sharedPreferencesM4491b = StickFighterApplication.m4491b();
        int i = sharedPreferencesM4491b.getInt("PlayerFPS", 1);
        this.f4016C.setMax(this.f4032d.length - 1);
        this.f4016C.setProgress(i);
        int i2 = sharedPreferencesM4491b.getInt("PlayerKeyFPS", 3);
        this.f4018E.setMax(this.f4033e.length - 1);
        this.f4018E.setProgress(i2);
        this.f4020G.setChecked(sharedPreferencesM4491b.getBoolean("PlayerRepeat", false));
        this.f4015B.setVisibility(0);
        ScaleAnimation scaleAnimation = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, 1, 1.0f, 1, 1.0f);
        scaleAnimation.setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
        this.f4015B.startAnimation(scaleAnimation);
        this.f4015B.setOnClickListener(new ao(this));
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: renamed from: h */
    public void m4729h() {
        if (this.f4015B.getVisibility() == 4) {
            return;
        }
        ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 0.0f, 1.0f, 0.0f, 1, 1.0f, 1, 1.0f);
        scaleAnimation.setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
        scaleAnimation.setAnimationListener(new ap(this));
        this.f4015B.startAnimation(scaleAnimation);
    }

    /* JADX INFO: renamed from: i */
    private void m4732i() {
        int iCeil = (int) Math.ceil((this.f4041m - 1.0f) / this.f4038j);
        this.f4053y.setText(String.format("%d:%02d", Integer.valueOf(iCeil / 60), Integer.valueOf(iCeil % 60)));
        this.f4054z.setMax(iCeil);
    }

    @Override // androidx.fragment.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.f4040l = C0924v.m4932a();
        if (this.f4040l == null) {
            getActivity().finish();
            return;
        }
        this.f4041m = C0836a.m4547a().mo4562b(this.f4040l.m4935b());
        this.f4042n = this.f4040l.m4939f().m4539a(0);
        this.f4046r = new af(this, Looper.getMainLooper());
        this.f4047s = new aq(this, Looper.getMainLooper());
        this.f4048t = new Handler(Looper.getMainLooper());
    }

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        int i;
        af afVar = null;
        if (this.f4040l == null) {
            return null;
        }
        Rect rect = new Rect();
        getActivity().getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        int iM4962c = C0935d.m4962c(getActivity());
        int iM4961b = C0935d.m4961b(getActivity());
        boolean z = iM4962c != rect.right;
        boolean z2 = iM4961b != rect.bottom;
        if (z && z2) {
            i = R.layout.fragment_sequence_player_both_nav;
        } else if (z) {
            i = R.layout.fragment_sequence_player;
        } else {
            i = z2 ? R.layout.fragment_sequence_player_tablet : R.layout.fragment_sequence_player_no_nav;
        }
        View viewInflate = layoutInflater.inflate(i, viewGroup, false);
        this.f4050v = (ProgressBar) viewInflate.findViewById(R.id.progressBarLoading);
        this.f4049u = viewInflate.findViewById(R.id.progressBarContainer);
        this.f4049u.setOnTouchListener(new ar(this));
        this.f4051w = viewInflate.findViewById(R.id.videoToolbar);
        this.f4023J = (ImageView) viewInflate.findViewById(R.id.playPauseButton);
        viewInflate.findViewById(R.id.playPauseContainer).setOnClickListener(new as(this));
        this.f4014A = (ImageView) viewInflate.findViewById(R.id.videoSettingsButton);
        this.f4014A.setOnClickListener(new at(this));
        this.f4021H = (Button) viewInflate.findViewById(R.id.okButton);
        this.f4021H.setOnClickListener(new au(this));
        this.f4022I = (Button) viewInflate.findViewById(R.id.cancelButton);
        this.f4022I.setOnClickListener(new av(this));
        this.f4052x = (TextView) viewInflate.findViewById(R.id.playedDurationTextView);
        this.f4053y = (TextView) viewInflate.findViewById(R.id.totalDurationTextView);
        this.f4054z = (SeekBar) viewInflate.findViewById(R.id.playbackDurationSeekBar);
        this.f4015B = viewInflate.findViewById(R.id.videoSettingsMenuContainer);
        this.f4016C = (SeekBar) viewInflate.findViewById(R.id.playbackFpsSeekBar);
        this.f4017D = (TextView) viewInflate.findViewById(R.id.fpsValueTextView);
        this.f4018E = (SeekBar) viewInflate.findViewById(R.id.keyFpsSeekBar);
        this.f4019F = (TextView) viewInflate.findViewById(R.id.keyFpsValueTextView);
        this.f4020G = (SwitchCompat) viewInflate.findViewById(R.id.repeatSwitch);
        this.f4016C.setOnSeekBarChangeListener(new aw(this));
        this.f4018E.setOnSeekBarChangeListener(new ax(this));
        this.f4043o = new TextureView(getActivity());
        this.f4043o.setSurfaceTextureListener(this);
        this.f4043o.setOpaque(false);
        ((ViewGroup) viewInflate).addView(this.f4043o, 0);
        this.f4043o.setOnTouchListener(new ag(this));
        m4710b();
        if (bundle == null) {
            this.f4036h = new az(this, afVar);
            this.f4036h.execute(new Void[0]);
        } else {
            this.f4049u.setVisibility(4);
        }
        this.f4054z.setOnSeekBarChangeListener(new ai(this));
        getActivity().getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new aj(this));
        new Handler().postDelayed(new al(this), 300L);
        return viewInflate;
    }

    @Override // androidx.fragment.app.Fragment
    public void onDestroy() {
        super.onDestroy();
        this.f4040l.m4939f().m4543b(50);
        if (this.f4036h != null && this.f4036h.getStatus() == AsyncTask.Status.RUNNING && getActivity().isFinishing()) {
            this.f4036h.cancel(false);
        }
        this.f4036h = null;
    }

    @Override // androidx.fragment.app.Fragment
    public void onPause() {
        super.onPause();
        m4724e();
    }

    @Override // android.view.TextureView.SurfaceTextureListener
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i2) {
        InterfaceC0913k interfaceC0913k = (this.f4025L == 0 && this.f4026M == 0) ? this.f4042n : (InterfaceC0913k) this.f4027N.get(this.f4026M);
        Canvas canvasLockCanvas = this.f4043o.lockCanvas();
        m4703a(canvasLockCanvas, interfaceC0913k);
        this.f4043o.unlockCanvasAndPost(canvasLockCanvas);
    }

    @Override // android.view.TextureView.SurfaceTextureListener
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return false;
    }

    @Override // android.view.TextureView.SurfaceTextureListener
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i2) {
    }

    @Override // android.view.TextureView.SurfaceTextureListener
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
    }
}
