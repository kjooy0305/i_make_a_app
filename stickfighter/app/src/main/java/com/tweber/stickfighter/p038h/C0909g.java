package com.tweber.stickfighter.p038h;

import java.util.ArrayList;

/* JADX INFO: renamed from: com.tweber.stickfighter.h.g */
/* JADX INFO: loaded from: classes.dex */
public class C0909g {
    /* JADX INFO: renamed from: a */
    public static AbstractC0908f m4885a(int i, long j, long j2, ArrayList arrayList, int i2) {
        switch (i) {
            case 1:
                return new C0922t(j, j2, arrayList, i2);
            case 2:
                return new C0910h(j, j2, arrayList, i2);
            case 3:
                return new C0911i(j, j2, arrayList, i2);
            default:
                throw new IllegalArgumentException("Animation object type not recognized!");
        }
    }
}
