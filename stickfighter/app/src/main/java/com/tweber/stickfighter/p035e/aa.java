package com.tweber.stickfighter.p035e;

import android.graphics.PointF;
import com.tweber.stickfighter.p038h.C0905c;
import com.tweber.stickfighter.p038h.C0907e;
import com.tweber.stickfighter.p038h.C0918p;
import com.tweber.stickfighter.p038h.C0924v;
import java.util.ArrayList;
import java.util.HashMap;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/* JADX INFO: loaded from: classes.dex */
class aa extends DefaultHandler {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ AsyncTaskC0864z f3890a;

    /* JADX INFO: renamed from: b */
    HashMap f3891b;

    /* JADX INFO: renamed from: c */
    HashMap f3892c;

    /* JADX INFO: renamed from: d */
    ArrayList f3893d;

    /* JADX INFO: renamed from: e */
    ArrayList f3894e;

    /* JADX INFO: renamed from: f */
    int f3895f;

    /* JADX INFO: renamed from: g */
    C0924v f3896g;

    /* JADX INFO: renamed from: h */
    C0918p f3897h;

    /* JADX INFO: renamed from: i */
    Integer f3898i;

    /* JADX INFO: renamed from: j */
    C0905c f3899j;

    private aa(AsyncTaskC0864z asyncTaskC0864z) {
        this.f3890a = asyncTaskC0864z;
        this.f3891b = new HashMap();
        this.f3892c = new HashMap();
        this.f3893d = new ArrayList();
        this.f3894e = new ArrayList();
        this.f3896g = null;
        this.f3897h = null;
        this.f3898i = null;
        this.f3899j = null;
    }

    /* synthetic */ aa(AsyncTaskC0864z asyncTaskC0864z, ViewOnClickListenerC0861w viewOnClickListenerC0861w) {
        this(asyncTaskC0864z);
    }

    /* JADX INFO: renamed from: a */
    public C0924v m4588a() {
        return this.f3896g;
    }

    @Override // org.xml.sax.helpers.DefaultHandler, org.xml.sax.ContentHandler
    public void endDocument() {
        int i = 0;
        while (true) {
            int i2 = i;
            if (i2 >= this.f3893d.size()) {
                return;
            }
            this.f3890a.f3985c.mo4563b(((Integer) this.f3893d.get(i2)).intValue(), ((Integer) this.f3892c.get(Integer.valueOf(((Integer) this.f3894e.get(i2)).intValue()))).intValue());
            i = i2 + 1;
        }
    }

    @Override // org.xml.sax.helpers.DefaultHandler, org.xml.sax.ContentHandler
    public void endElement(String str, String str2, String str3) {
    }

    @Override // org.xml.sax.helpers.DefaultHandler, org.xml.sax.ContentHandler
    public void startDocument() {
    }

    @Override // org.xml.sax.helpers.DefaultHandler, org.xml.sax.ContentHandler
    public void startElement(String str, String str2, String str3, Attributes attributes) {
        if (str2.equals("AC")) {
            int i = Integer.parseInt(attributes.getValue("ConnPtId"));
            this.f3893d.add(Integer.valueOf(this.f3899j.f4164a));
            this.f3894e.add(Integer.valueOf(i));
            return;
        }
        if (str2.equals("AP")) {
            int i2 = Integer.parseInt(attributes.getValue("ID"));
            this.f3899j = this.f3890a.f3985c.mo4548a(this.f3898i.intValue(), this.f3897h.f4232a, new PointF(Float.parseFloat(attributes.getValue("X")), Float.parseFloat(attributes.getValue("Y"))), Integer.parseInt(attributes.getValue("Index")), Short.parseShort(attributes.getValue("Flags")));
            this.f3892c.put(Integer.valueOf(i2), Integer.valueOf(this.f3899j.f4164a));
            return;
        }
        if (str2.equals("AO")) {
            int i3 = Integer.parseInt(attributes.getValue("ID"));
            int i4 = Integer.parseInt(attributes.getValue("TypeId"));
            int i5 = Integer.parseInt(attributes.getValue("Color"));
            this.f3898i = (Integer) this.f3891b.get(Integer.valueOf(i3));
            if (this.f3898i == null) {
                this.f3898i = Integer.valueOf(this.f3890a.f3985c.mo4549a(this.f3897h, i4, i5, new C0907e[0]).f4181b);
                this.f3891b.put(Integer.valueOf(i3), this.f3898i);
            }
            this.f3890a.f3985c.mo4555a(this.f3898i.intValue(), this.f3897h.f4232a, i5);
            return;
        }
        if (str2.equals("Frame")) {
            Long.parseLong(attributes.getValue("ID"));
            int i6 = Integer.parseInt(attributes.getValue("Position"));
            this.f3897h = this.f3890a.f3985c.mo4550a(this.f3896g.m4935b(), i6);
            this.f3890a.publish(Integer.valueOf((int) (((float) i6 / this.f3895f) * 100.0f)));
            return;
        }
        if (str2.equals("Sequence")) {
            String value = attributes.getValue("Name");
            float f = Float.parseFloat(attributes.getValue("HeightWidthRatio"));
            this.f3895f = Integer.parseInt(attributes.getValue("FrameCount"));
            String value2 = attributes.getValue("BackgroundColor");
            this.f3896g = this.f3890a.f3985c.mo4553a(value, f, value2 == null ? -1 : Integer.parseInt(value2));
        }
    }
}
