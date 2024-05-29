package com.android.gallery3d.common;

import android.graphics.RectF;
import android.util.Log;
import java.io.Closeable;
import java.io.IOException;
/* loaded from: a.zip:com/android/gallery3d/common/Utils.class */
public class Utils {
    public static void assertTrue(boolean z) {
        if (!z) {
            throw new AssertionError();
        }
    }

    public static int ceilLog2(float f) {
        int i = 0;
        while (i < 31 && (1 << i) < f) {
            i++;
        }
        return i;
    }

    public static int clamp(int i, int i2, int i3) {
        return i > i3 ? i3 : i < i2 ? i2 : i;
    }

    public static void closeSilently(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException e) {
            Log.w("Utils", "close fail ", e);
        }
    }

    public static int floorLog2(float f) {
        int i = 0;
        while (i < 31 && (1 << i) <= f) {
            i++;
        }
        return i - 1;
    }

    public static RectF getMaxCropRect(int i, int i2, int i3, int i4, boolean z) {
        RectF rectF = new RectF();
        if (i / i2 > i3 / i4) {
            rectF.top = 0.0f;
            rectF.bottom = i2;
            rectF.left = (i - ((i3 / i4) * i2)) / 2.0f;
            rectF.right = i - rectF.left;
            if (z) {
                rectF.right -= rectF.left;
                rectF.left = 0.0f;
            }
        } else {
            rectF.left = 0.0f;
            rectF.right = i;
            rectF.top = (i2 - ((i4 / i3) * i)) / 2.0f;
            rectF.bottom = i2 - rectF.top;
        }
        return rectF;
    }

    public static int nextPowerOf2(int i) {
        if (i <= 0 || i > 1073741824) {
            throw new IllegalArgumentException("n is invalid: " + i);
        }
        int i2 = i - 1;
        int i3 = i2 | (i2 >> 16);
        int i4 = i3 | (i3 >> 8);
        int i5 = i4 | (i4 >> 4);
        int i6 = i5 | (i5 >> 2);
        return (i6 | (i6 >> 1)) + 1;
    }

    public static int prevPowerOf2(int i) {
        if (i <= 0) {
            throw new IllegalArgumentException();
        }
        return Integer.highestOneBit(i);
    }
}
