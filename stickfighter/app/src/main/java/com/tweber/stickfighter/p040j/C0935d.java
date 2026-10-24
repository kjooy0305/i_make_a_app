package com.tweber.stickfighter.p040j;

import android.R;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

/* JADX INFO: renamed from: com.tweber.stickfighter.j.d */
/* JADX INFO: loaded from: classes.dex */
public class C0935d {
    /* JADX INFO: renamed from: a */
    public static float m4956a(Activity activity) {
        Point point = new Point();
        activity.getWindowManager().getDefaultDisplay().getRealSize(point);
        return Math.min(point.x, point.y) / Math.max(point.x, point.y);
    }

    /* JADX INFO: renamed from: a */
    public static String m4957a(View view, int i) {
        return ((EditText) view.findViewById(i)).getText().toString();
    }

    /* JADX INFO: renamed from: a */
    public static void m4958a(View view) {
        if (view == null) {
            return;
        }
        view.animate().alpha(0.0f).setDuration(view.getContext().getResources().getInteger(R.integer.config_shortAnimTime)).setListener(new C0936e(view));
    }

    /* JADX INFO: renamed from: a */
    public static void m4959a(View view, int i, String str) {
        ((TextView) view.findViewById(i)).setText(str);
    }

    /* JADX INFO: renamed from: a */
    public static boolean m4960a(Context context) {
        return (context.getResources().getConfiguration().screenLayout & 15) >= 3;
    }

    /* JADX INFO: renamed from: b */
    public static int m4961b(Activity activity) {
        Point point = new Point();
        activity.getWindowManager().getDefaultDisplay().getRealSize(point);
        return point.y;
    }

    /* JADX INFO: renamed from: c */
    public static int m4962c(Activity activity) {
        Point point = new Point();
        activity.getWindowManager().getDefaultDisplay().getRealSize(point);
        return point.x;
    }
}
