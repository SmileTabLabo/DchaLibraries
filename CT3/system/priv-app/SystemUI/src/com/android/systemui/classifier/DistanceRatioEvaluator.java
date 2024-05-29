package com.android.systemui.classifier;
/* loaded from: a.zip:com/android/systemui/classifier/DistanceRatioEvaluator.class */
public class DistanceRatioEvaluator {
    public static float evaluate(float f) {
        float f2 = 0.0f;
        if (f <= 1.0d) {
            f2 = 1.0f;
        }
        float f3 = f2;
        if (f <= 0.5d) {
            f3 = f2 + 1.0f;
        }
        float f4 = f3;
        if (f > 4.0d) {
            f4 = f3 + 1.0f;
        }
        float f5 = f4;
        if (f > 7.0d) {
            f5 = f4 + 1.0f;
        }
        float f6 = f5;
        if (f > 14.0d) {
            f6 = f5 + 1.0f;
        }
        return f6;
    }
}
