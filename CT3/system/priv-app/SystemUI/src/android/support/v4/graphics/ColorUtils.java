package android.support.v4.graphics;

import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
/* loaded from: a.zip:android/support/v4/graphics/ColorUtils.class */
public final class ColorUtils {
    private static final ThreadLocal<double[]> TEMP_ARRAY = new ThreadLocal<>();

    private ColorUtils() {
    }

    @ColorInt
    public static int HSLToColor(@NonNull float[] fArr) {
        float f = fArr[0];
        float f2 = fArr[1];
        float f3 = fArr[2];
        float abs = (1.0f - Math.abs((2.0f * f3) - 1.0f)) * f2;
        float f4 = f3 - (0.5f * abs);
        float abs2 = abs * (1.0f - Math.abs(((f / 60.0f) % 2.0f) - 1.0f));
        int i = 0;
        int i2 = 0;
        int i3 = 0;
        switch (((int) f) / 60) {
            case 0:
                i = Math.round((abs + f4) * 255.0f);
                i2 = Math.round((abs2 + f4) * 255.0f);
                i3 = Math.round(255.0f * f4);
                break;
            case 1:
                i = Math.round((abs2 + f4) * 255.0f);
                i2 = Math.round((abs + f4) * 255.0f);
                i3 = Math.round(255.0f * f4);
                break;
            case 2:
                i = Math.round(255.0f * f4);
                i2 = Math.round((abs + f4) * 255.0f);
                i3 = Math.round((abs2 + f4) * 255.0f);
                break;
            case 3:
                i = Math.round(255.0f * f4);
                i2 = Math.round((abs2 + f4) * 255.0f);
                i3 = Math.round((abs + f4) * 255.0f);
                break;
            case 4:
                i = Math.round((abs2 + f4) * 255.0f);
                i2 = Math.round(255.0f * f4);
                i3 = Math.round((abs + f4) * 255.0f);
                break;
            case 5:
            case 6:
                i = Math.round((abs + f4) * 255.0f);
                i2 = Math.round(255.0f * f4);
                i3 = Math.round((abs2 + f4) * 255.0f);
                break;
        }
        return Color.rgb(constrain(i, 0, 255), constrain(i2, 0, 255), constrain(i3, 0, 255));
    }

    public static void RGBToHSL(@IntRange(from = 0, to = 255) int i, @IntRange(from = 0, to = 255) int i2, @IntRange(from = 0, to = 255) int i3, @NonNull float[] fArr) {
        float f;
        float abs;
        float f2 = i / 255.0f;
        float f3 = i2 / 255.0f;
        float f4 = i3 / 255.0f;
        float max = Math.max(f2, Math.max(f3, f4));
        float min = Math.min(f2, Math.min(f3, f4));
        float f5 = max - min;
        float f6 = (max + min) / 2.0f;
        if (max == min) {
            abs = 0.0f;
            f = 0.0f;
        } else {
            f = max == f2 ? ((f3 - f4) / f5) % 6.0f : max == f3 ? ((f4 - f2) / f5) + 2.0f : ((f2 - f3) / f5) + 4.0f;
            abs = f5 / (1.0f - Math.abs((2.0f * f6) - 1.0f));
        }
        float f7 = (60.0f * f) % 360.0f;
        float f8 = f7;
        if (f7 < 0.0f) {
            f8 = f7 + 360.0f;
        }
        fArr[0] = constrain(f8, 0.0f, 360.0f);
        fArr[1] = constrain(abs, 0.0f, 1.0f);
        fArr[2] = constrain(f6, 0.0f, 1.0f);
    }

    public static void RGBToXYZ(@IntRange(from = 0, to = 255) int i, @IntRange(from = 0, to = 255) int i2, @IntRange(from = 0, to = 255) int i3, @NonNull double[] dArr) {
        if (dArr.length != 3) {
            throw new IllegalArgumentException("outXyz must have a length of 3.");
        }
        double d = i / 255.0d;
        double pow = d < 0.04045d ? d / 12.92d : Math.pow((0.055d + d) / 1.055d, 2.4d);
        double d2 = i2 / 255.0d;
        double pow2 = d2 < 0.04045d ? d2 / 12.92d : Math.pow((0.055d + d2) / 1.055d, 2.4d);
        double d3 = i3 / 255.0d;
        double pow3 = d3 < 0.04045d ? d3 / 12.92d : Math.pow((0.055d + d3) / 1.055d, 2.4d);
        dArr[0] = ((0.4124d * pow) + (0.3576d * pow2) + (0.1805d * pow3)) * 100.0d;
        dArr[1] = ((0.2126d * pow) + (0.7152d * pow2) + (0.0722d * pow3)) * 100.0d;
        dArr[2] = ((0.0193d * pow) + (0.1192d * pow2) + (0.9505d * pow3)) * 100.0d;
    }

    @FloatRange(from = 0.0d, to = 1.0d)
    public static double calculateLuminance(@ColorInt int i) {
        double[] tempDouble3Array = getTempDouble3Array();
        colorToXYZ(i, tempDouble3Array);
        return tempDouble3Array[1] / 100.0d;
    }

    @VisibleForTesting
    static float circularInterpolate(float f, float f2, float f3) {
        float f4 = f;
        float f5 = f2;
        if (Math.abs(f2 - f) > 180.0f) {
            if (f2 > f) {
                f4 = f + 360.0f;
                f5 = f2;
            } else {
                f5 = f2 + 360.0f;
                f4 = f;
            }
        }
        return (((f5 - f4) * f3) + f4) % 360.0f;
    }

    public static void colorToHSL(@ColorInt int i, @NonNull float[] fArr) {
        RGBToHSL(Color.red(i), Color.green(i), Color.blue(i), fArr);
    }

    public static void colorToXYZ(@ColorInt int i, @NonNull double[] dArr) {
        RGBToXYZ(Color.red(i), Color.green(i), Color.blue(i), dArr);
    }

    private static int compositeAlpha(int i, int i2) {
        return 255 - (((255 - i2) * (255 - i)) / 255);
    }

    public static int compositeColors(@ColorInt int i, @ColorInt int i2) {
        int alpha = Color.alpha(i2);
        int alpha2 = Color.alpha(i);
        int compositeAlpha = compositeAlpha(alpha2, alpha);
        return Color.argb(compositeAlpha, compositeComponent(Color.red(i), alpha2, Color.red(i2), alpha, compositeAlpha), compositeComponent(Color.green(i), alpha2, Color.green(i2), alpha, compositeAlpha), compositeComponent(Color.blue(i), alpha2, Color.blue(i2), alpha, compositeAlpha));
    }

    private static int compositeComponent(int i, int i2, int i3, int i4, int i5) {
        if (i5 == 0) {
            return 0;
        }
        return (((i * 255) * i2) + ((i3 * i4) * (255 - i2))) / (i5 * 255);
    }

    private static float constrain(float f, float f2, float f3) {
        if (f >= f2) {
            f2 = f > f3 ? f3 : f;
        }
        return f2;
    }

    private static int constrain(int i, int i2, int i3) {
        if (i >= i2) {
            i2 = i > i3 ? i3 : i;
        }
        return i2;
    }

    private static double[] getTempDouble3Array() {
        double[] dArr = TEMP_ARRAY.get();
        double[] dArr2 = dArr;
        if (dArr == null) {
            dArr2 = new double[3];
            TEMP_ARRAY.set(dArr2);
        }
        return dArr2;
    }

    @ColorInt
    public static int setAlphaComponent(@ColorInt int i, @IntRange(from = 0, to = 255) int i2) {
        if (i2 < 0 || i2 > 255) {
            throw new IllegalArgumentException("alpha must be between 0 and 255.");
        }
        return (16777215 & i) | (i2 << 24);
    }
}
