package com.tweber.stickfighter.p040j;

import android.text.TextUtils;
import java.io.File;

/* JADX INFO: renamed from: com.tweber.stickfighter.j.c */
/* JADX INFO: loaded from: classes.dex */
public class C0934c {

    /* JADX INFO: renamed from: a */
    static final String[] f4259a = {"|", "\\", "?", "*", "<", "\"", ":", ">", "+", "[", "]", "/", "'", "#", "~", "!", "@", "$", "%", "^", "&", "*", "(", ")", "+", ","};

    /* JADX INFO: renamed from: a */
    public static String m4954a(String str) {
        return str.replace("&", "&amp;").replace(">", "&gt;").replace("<", "&lt;").replace("\"", "&quot;");
    }

    /* JADX INFO: renamed from: a */
    public static String m4955a(String str, String str2, String str3, String str4) {
        String strReplaceAll = str2.replaceAll("[^A-Za-z0-9_-]", "");
        if (!TextUtils.isEmpty(strReplaceAll)) {
            str3 = strReplaceAll;
        }
        String str5 = "";
        File file = new File(str + str3 + "" + str4);
        int i = 0;
        while (file.exists()) {
            i++;
            str5 = String.format(" (%s)", Integer.valueOf(i));
            file = new File(str + str3 + str5 + str4);
        }
        return str + str3 + str5 + str4;
    }
}
