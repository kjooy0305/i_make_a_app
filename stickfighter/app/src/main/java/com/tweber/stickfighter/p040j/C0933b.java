package com.tweber.stickfighter.p040j;

import android.graphics.Bitmap;

/* JADX INFO: renamed from: com.tweber.stickfighter.j.b */
/* JADX INFO: loaded from: classes.dex */
public class C0933b {
    /* JADX INFO: renamed from: a */
    private static void m4949a(byte[] bArr, int[] iArr, int i, int i2) {
        int i3;
        int i4;
        int i5;
        int i6 = i * i2;
        int i7 = 0;
        int i8 = 0;
        int i9 = 0;
        while (i9 < i2) {
            int i10 = 0;
            int i11 = i8;
            int i12 = i7;
            while (i10 < i) {
                int i13 = (iArr[i11] & 16711680) >> 16;
                int i14 = (iArr[i11] & 65280) >> 8;
                int i15 = iArr[i11] & 255;
                int i16 = (((((i15 * 66) + (i14 * 129)) + (i13 * 25)) + 128) >> 8) + 16;
                int i17 = (((((i15 * (-38)) - (i14 * 74)) + (i13 * 112)) + 128) >> 8) + 128;
                int i18 = (((((i15 * 112) - (i14 * 94)) - (i13 * 18)) + 128) >> 8) + 128;
                int i19 = i12 + 1;
                if (i16 < 0) {
                    i16 = 0;
                } else if (i16 > 255) {
                    i16 = 255;
                }
                bArr[i12] = (byte) i16;
                if (i9 % 2 == 0 && i11 % 2 == 0) {
                    int i20 = i6 + 1;
                    if (i18 < 0) {
                        i4 = 0;
                    } else {
                        i4 = i18 > 255 ? 255 : i18;
                    }
                    bArr[i6] = (byte) i4;
                    int i21 = i20 + 1;
                    if (i17 < 0) {
                        i5 = 0;
                    } else {
                        i5 = i17 > 255 ? 255 : i17;
                    }
                    bArr[i20] = (byte) i5;
                    i3 = i21;
                } else {
                    i3 = i6;
                }
                i10++;
                i11++;
                i6 = i3;
                i12 = i19;
            }
            i9++;
            i8 = i11;
            i7 = i12;
        }
    }

    /* JADX INFO: renamed from: a */
    public static boolean m4950a(int i) {
        switch (i) {
            case 19:
            case 20:
                return false;
            case 21:
            case 39:
            case 2130706688:
                return true;
            default:
                throw new RuntimeException("unknown format " + i);
        }
    }

    /* JADX INFO: renamed from: a */
    public static byte[] m4951a(int i, int i2, Bitmap bitmap) {
        int[] iArr = new int[i * i2];
        bitmap.getPixels(iArr, 0, i, 0, 0, i, i2);
        byte[] bArr = new byte[((i * i2) * 3) / 2];
        m4949a(bArr, iArr, i, i2);
        return bArr;
    }

    /* JADX INFO: renamed from: b */
    public static byte[] m4952b(int i, int i2, Bitmap bitmap) {
        int[] iArr = new int[i * i2];
        bitmap.getPixels(iArr, 0, i, 0, 0, i, i2);
        byte[] bArr = new byte[((i * i2) * 3) / 2];
        m4953b(bArr, iArr, i, i2);
        return bArr;
    }

    /* JADX INFO: renamed from: b */
    private static byte[] m4953b(byte[] bArr, int[] iArr, int i, int i2) {
        int i3;
        int i4;
        int i5;
        int i6;
        int i7 = i * i2;
        int i8 = 0;
        int i9 = i7 + (i7 / 4);
        int i10 = 0;
        int i11 = 0;
        while (i11 < i2) {
            int i12 = 0;
            int i13 = i10;
            int i14 = i8;
            while (i12 < i) {
                int i15 = (iArr[i13] & 16711680) >> 16;
                int i16 = (iArr[i13] & 65280) >> 8;
                int i17 = iArr[i13] & 255;
                int i18 = (((((i15 * 66) + (i16 * 129)) + (i17 * 25)) + 128) >> 8) + 16;
                int i19 = (((((i15 * (-38)) - (i16 * 74)) + (i17 * 112)) + 128) >> 8) + 128;
                int i20 = (((((i15 * 112) - (i16 * 94)) - (i17 * 18)) + 128) >> 8) + 128;
                int i21 = i14 + 1;
                if (i18 < 0) {
                    i18 = 0;
                } else if (i18 > 255) {
                    i18 = 255;
                }
                bArr[i14] = (byte) i18;
                if (i11 % 2 == 0 && i13 % 2 == 0) {
                    int i22 = i7 + 1;
                    if (i19 < 0) {
                        i5 = 0;
                    } else {
                        i5 = i19 > 255 ? 255 : i19;
                    }
                    bArr[i7] = (byte) i5;
                    int i23 = i9 + 1;
                    if (i20 < 0) {
                        i6 = 0;
                    } else {
                        i6 = i20 > 255 ? 255 : i20;
                    }
                    bArr[i9] = (byte) i6;
                    i3 = i23;
                    i4 = i22;
                } else {
                    i3 = i9;
                    i4 = i7;
                }
                i12++;
                i13++;
                i7 = i4;
                i14 = i21;
                i9 = i3;
            }
            i11++;
            i10 = i13;
            i8 = i14;
        }
        return bArr;
    }
}
