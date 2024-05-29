package com.android.systemui.classifier;
/* loaded from: a.zip:com/android/systemui/classifier/SpeedEvaluator.class */
public class SpeedEvaluator {
    public static float evaluate(float f) {
        float f2 = 0.0f;
        if (f < 4.0d) {
            f2 = 1.0f;
        }
        float f3 = f2;
        if (f < 2.2d) {
            f3 = f2 + 1.0f;
        }
        float f4 = f3;
        if (f > 35.0d) {
            f4 = f3 + 1.0f;
        }
        float f5 = f4;
        if (f > 50.0d) {
            f5 = f4 + 1.0f;
        }
        return f5;
    }
}
