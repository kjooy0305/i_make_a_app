package com.tweber.stickfighter.views;

import android.content.res.Resources;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import com.tweber.stickfighter.StickFighterApplication;
import com.tweber.stickfighter.activities.R;

/* JADX INFO: renamed from: com.tweber.stickfighter.views.l */
/* JADX INFO: loaded from: classes.dex */
public class C0948l {

    /* JADX INFO: renamed from: o */
    static final int f4283o = StickFighterApplication.m4490a().getResources().getDimensionPixelSize(R.dimen.minimum_figure_width);

    /* JADX INFO: renamed from: a */
    public final Paint f4284a;

    /* JADX INFO: renamed from: b */
    public final Paint f4285b;

    /* JADX INFO: renamed from: c */
    public final Paint f4286c;

    /* JADX INFO: renamed from: d */
    public final Paint f4287d;

    /* JADX INFO: renamed from: e */
    public final Paint f4288e;

    /* JADX INFO: renamed from: f */
    public final Paint f4289f;

    /* JADX INFO: renamed from: g */
    public final Paint f4290g;

    /* JADX INFO: renamed from: h */
    public final Paint f4291h;

    /* JADX INFO: renamed from: i */
    public final Paint f4292i;

    /* JADX INFO: renamed from: j */
    public final Paint f4293j;

    /* JADX INFO: renamed from: k */
    public final Paint f4294k;

    /* JADX INFO: renamed from: l */
    public final Paint f4295l;

    /* JADX INFO: renamed from: m */
    public final Paint f4296m;

    /* JADX INFO: renamed from: n */
    public final Paint f4297n;

    public C0948l(float f) {
        Resources resources = StickFighterApplication.m4490a().getResources();
        this.f4284a = new Paint();
        this.f4284a.setStyle(Paint.Style.STROKE);
        this.f4284a.setStrokeWidth(resources.getDimensionPixelSize(R.dimen.selection_circle_border));
        this.f4284a.setColor(resources.getColor(R.color.selection_circle_border));
        this.f4284a.setAntiAlias(true);
        this.f4284a.setDither(true);
        this.f4285b = new Paint();
        this.f4285b.setStyle(Paint.Style.STROKE);
        this.f4285b.setStrokeWidth(resources.getDimensionPixelSize(R.dimen.selection_circle_ring_fill));
        this.f4285b.setColor(resources.getColor(R.color.selection_circle_ring_fill));
        this.f4285b.setAntiAlias(true);
        this.f4285b.setDither(true);
        this.f4286c = new Paint();
        this.f4286c.setStyle(Paint.Style.FILL);
        this.f4286c.setColor(resources.getColor(R.color.selection_circle_fill));
        this.f4286c.setAntiAlias(true);
        this.f4286c.setDither(true);
        this.f4287d = new Paint();
        this.f4287d.setColor(-1);
        this.f4287d.setStyle(Paint.Style.FILL);
        this.f4287d.setDither(true);
        this.f4288e = new Paint();
        this.f4288e.setColor(resources.getColor(R.color.frame_number));
        this.f4288e.setStyle(Paint.Style.FILL);
        this.f4288e.setTextAlign(Paint.Align.LEFT);
        this.f4288e.setTextSize(resources.getDimensionPixelSize(R.dimen.frame_number_text));
        this.f4288e.setAntiAlias(true);
        this.f4288e.setDither(true);
        this.f4289f = new Paint();
        this.f4289f.setStyle(Paint.Style.STROKE);
        this.f4289f.setColor(resources.getColor(R.color.animation_object_highlight));
        this.f4289f.setStrokeWidth(f * 0.0078125f * 2.0f);
        this.f4289f.setAntiAlias(true);
        this.f4289f.setDither(true);
        this.f4289f.setStrokeCap(Paint.Cap.ROUND);
        this.f4290g = new Paint();
        this.f4290g.setStyle(Paint.Style.STROKE);
        this.f4290g.setColor(-16777216);
        this.f4290g.setAntiAlias(true);
        this.f4290g.setDither(true);
        this.f4290g.setStrokeCap(Paint.Cap.ROUND);
        this.f4290g.setStrokeWidth(Math.max(f4283o, f * 0.0078125f));
        this.f4291h = new Paint();
        this.f4291h.setStyle(Paint.Style.STROKE);
        this.f4291h.setColor(resources.getColor(R.color.animation_object_shadow));
        this.f4291h.setAntiAlias(true);
        this.f4291h.setDither(true);
        this.f4291h.setStrokeCap(Paint.Cap.ROUND);
        this.f4291h.setStrokeWidth(Math.max(f4283o, f * 0.0078125f));
        this.f4292i = new Paint();
        this.f4292i.setColor(resources.getColor(R.color.edit_mark_translate));
        this.f4292i.setAntiAlias(true);
        this.f4292i.setDither(true);
        this.f4292i.setStyle(Paint.Style.FILL);
        this.f4293j = new Paint();
        this.f4293j.setColor(resources.getColor(R.color.edit_mark_move));
        this.f4293j.setAntiAlias(true);
        this.f4293j.setDither(true);
        this.f4293j.setStyle(Paint.Style.FILL);
        this.f4294k = new Paint();
        this.f4294k.setColor(resources.getColor(R.color.edit_mark_stretch));
        this.f4294k.setAntiAlias(true);
        this.f4294k.setDither(true);
        this.f4294k.setStyle(Paint.Style.FILL);
        this.f4295l = new Paint();
        this.f4295l.setColor(resources.getColor(R.color.edit_mark_modify));
        this.f4295l.setAntiAlias(true);
        this.f4295l.setDither(true);
        this.f4295l.setStyle(Paint.Style.FILL);
        this.f4296m = new Paint();
        this.f4296m.setColor(resources.getColor(R.color.edit_mark_flip));
        this.f4296m.setAntiAlias(true);
        this.f4296m.setDither(true);
        this.f4296m.setStyle(Paint.Style.FILL);
        this.f4297n = new Paint();
        this.f4297n.setColor(-3355444);
        this.f4297n.setStyle(Paint.Style.STROKE);
        this.f4297n.setAntiAlias(true);
        this.f4297n.setDither(true);
        this.f4297n.setStrokeWidth(resources.getDimensionPixelSize(R.dimen.curve_edit_path_width));
        this.f4297n.setPathEffect(new DashPathEffect(new float[]{resources.getDimensionPixelSize(R.dimen.curve_edit_path_color_length), resources.getDimensionPixelSize(R.dimen.curve_edit_path_empty_length)}, 0.0f));
    }
}
