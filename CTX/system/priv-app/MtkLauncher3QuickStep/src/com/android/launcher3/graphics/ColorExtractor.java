package com.android.launcher3.graphics;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v4.view.ViewCompat;
import android.util.SparseArray;
/* loaded from: classes.dex */
public class ColorExtractor {
    public static int findDominantColorByHue(Bitmap bitmap) {
        return findDominantColorByHue(bitmap, 20);
    }

    public static int findDominantColorByHue(Bitmap bitmap, int i) {
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        int sqrt = (int) Math.sqrt((height * width) / i);
        if (sqrt < 1) {
            sqrt = 1;
        }
        float[] fArr = new float[3];
        float[] fArr2 = new float[360];
        int[] iArr = new int[i];
        int i2 = -1;
        int i3 = 0;
        int i4 = 0;
        float f = -1.0f;
        while (true) {
            int i5 = ViewCompat.MEASURED_STATE_MASK;
            if (i3 >= height) {
                break;
            }
            float f2 = f;
            int i6 = i2;
            int i7 = i4;
            int i8 = 0;
            while (i8 < width) {
                int pixel = bitmap.getPixel(i8, i3);
                if ((255 & (pixel >> 24)) >= 128) {
                    int i9 = pixel | i5;
                    Color.colorToHSV(i9, fArr);
                    int i10 = (int) fArr[0];
                    if (i10 >= 0 && i10 < fArr2.length) {
                        if (i7 < i) {
                            iArr[i7] = i9;
                            i7++;
                        }
                        fArr2[i10] = fArr2[i10] + (fArr[1] * fArr[2]);
                        if (fArr2[i10] > f2) {
                            f2 = fArr2[i10];
                            i6 = i10;
                        }
                    }
                }
                i8 += sqrt;
                i5 = ViewCompat.MEASURED_STATE_MASK;
            }
            i3 += sqrt;
            i4 = i7;
            i2 = i6;
            f = f2;
        }
        SparseArray sparseArray = new SparseArray();
        float f3 = -1.0f;
        int i11 = ViewCompat.MEASURED_STATE_MASK;
        for (int i12 = 0; i12 < i4; i12++) {
            int i13 = iArr[i12];
            Color.colorToHSV(i13, fArr);
            if (((int) fArr[0]) == i2) {
                float f4 = fArr[1];
                float f5 = fArr[2];
                int i14 = ((int) (100.0f * f4)) + ((int) (10000.0f * f5));
                float f6 = f4 * f5;
                Float f7 = (Float) sparseArray.get(i14);
                if (f7 != null) {
                    f6 += f7.floatValue();
                }
                sparseArray.put(i14, Float.valueOf(f6));
                if (f6 > f3) {
                    i11 = i13;
                    f3 = f6;
                }
            }
        }
        return i11;
    }
}
