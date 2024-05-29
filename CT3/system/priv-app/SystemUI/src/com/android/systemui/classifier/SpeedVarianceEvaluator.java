package com.android.systemui.classifier;
/* loaded from: a.zip:com/android/systemui/classifier/SpeedVarianceEvaluator.class */
public class SpeedVarianceEvaluator {
    public static float evaluate(float f) {
        float f2 = 0.0f;
        if (f > 0.06d) {
            f2 = 1.0f;
        }
        float f3 = f2;
        if (f > 0.15d) {
            f3 = f2 + 1.0f;
        }
        float f4 = f3;
        if (f > 0.3d) {
            f4 = f3 + 1.0f;
        }
        float f5 = f4;
        if (f > 0.6d) {
            f5 = f4 + 1.0f;
        }
        return f5;
    }
}
