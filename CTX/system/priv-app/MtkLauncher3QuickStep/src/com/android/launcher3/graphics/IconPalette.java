package com.android.launcher3.graphics;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.graphics.ColorUtils;
import com.android.launcher3.R;
import com.android.launcher3.util.Themes;
/* loaded from: classes.dex */
public class IconPalette {
    private static final boolean DEBUG = false;
    private static final float MIN_PRELOAD_COLOR_LIGHTNESS = 0.6f;
    private static final float MIN_PRELOAD_COLOR_SATURATION = 0.2f;
    private static final String TAG = "IconPalette";

    public static int getPreloadProgressColor(Context context, int i) {
        float[] fArr = new float[3];
        Color.colorToHSV(i, fArr);
        if (fArr[1] < 0.2f) {
            return Themes.getColorAccent(context);
        }
        fArr[2] = Math.max((float) MIN_PRELOAD_COLOR_LIGHTNESS, fArr[2]);
        return Color.HSVToColor(fArr);
    }

    public static int resolveContrastColor(Context context, int i, int i2) {
        return ensureTextContrast(resolveColor(context, i), i2);
    }

    private static int resolveColor(Context context, int i) {
        if (i == 0) {
            return context.getColor(R.color.notification_icon_default_color);
        }
        return i;
    }

    private static String contrastChange(int i, int i2, int i3) {
        return String.format("from %.2f:1 to %.2f:1", Double.valueOf(ColorUtils.calculateContrast(i, i3)), Double.valueOf(ColorUtils.calculateContrast(i2, i3)));
    }

    private static int ensureTextContrast(int i, int i2) {
        return findContrastColor(i, i2, 4.5d);
    }

    private static int findContrastColor(int i, int i2, double d) {
        if (ColorUtils.calculateContrast(i, i2) >= d) {
            return i;
        }
        double[] dArr = new double[3];
        ColorUtils.colorToLAB(i2, dArr);
        double d2 = dArr[0];
        ColorUtils.colorToLAB(i, dArr);
        double d3 = dArr[0];
        boolean z = d2 < 50.0d;
        double d4 = z ? d3 : 0.0d;
        if (z) {
            d3 = 100.0d;
        }
        double d5 = dArr[1];
        double d6 = dArr[2];
        for (int i3 = 0; i3 < 15 && d3 - d4 > 1.0E-5d; i3++) {
            double d7 = (d4 + d3) / 2.0d;
            if (ColorUtils.calculateContrast(ColorUtils.LABToColor(d7, d5, d6), i2) <= d ? !z : z) {
                d3 = d7;
            } else {
                d4 = d7;
            }
        }
        return ColorUtils.LABToColor(d4, d5, d6);
    }

    public static int getMutedColor(int i, float f) {
        return ColorUtils.compositeColors(ColorUtils.setAlphaComponent(-1, (int) (255.0f * f)), i);
    }
}
