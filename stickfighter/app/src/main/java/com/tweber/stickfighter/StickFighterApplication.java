package com.tweber.stickfighter;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import com.tweber.stickfighter.p031a.C0800a;
import com.tweber.stickfighter.p038h.C0924v;

/* JADX INFO: loaded from: classes.dex */
public class StickFighterApplication extends Application {

    /* JADX INFO: renamed from: a */
    static Context f3812a;

    /* JADX INFO: renamed from: a */
    public static Context m4490a() {
        return f3812a;
    }

    /* JADX INFO: renamed from: b */
    public static SharedPreferences m4491b() {
        return f3812a.getSharedPreferences("SharedPrefs", 0);
    }

    @Override // android.app.Application
    public void onCreate() {
        super.onCreate();
        f3812a = this;
        C0800a.m4492a();
    }

    @Override // android.app.Application, android.content.ComponentCallbacks
    public void onLowMemory() {
        super.onLowMemory();
        if (C0924v.m4932a() != null) {
            C0924v.m4932a().m4939f().m4544c();
        }
    }
}
