package com.android.systemui.classifier;
/* loaded from: a.zip:com/android/systemui/classifier/AnglesPercentageEvaluator.class */
public class AnglesPercentageEvaluator {
    public static float evaluate(float f) {
        float f2 = 0.0f;
        if (f < 1.0d) {
            f2 = 1.0f;
        }
        float f3 = f2;
        if (f < 0.9d) {
            f3 = f2 + 1.0f;
        }
        float f4 = f3;
        if (f < 0.7d) {
            f4 = f3 + 1.0f;
        }
        return f4;
    }
}
