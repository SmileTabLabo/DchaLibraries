package com.android.systemui.classifier;
/* loaded from: a.zip:com/android/systemui/classifier/LengthCountEvaluator.class */
public class LengthCountEvaluator {
    public static float evaluate(float f) {
        float f2 = 0.0f;
        if (f < 0.09d) {
            f2 = 1.0f;
        }
        float f3 = f2;
        if (f < 0.05d) {
            f3 = f2 + 1.0f;
        }
        float f4 = f3;
        if (f < 0.02d) {
            f4 = f3 + 1.0f;
        }
        float f5 = f4;
        if (f > 0.6d) {
            f5 = f4 + 1.0f;
        }
        float f6 = f5;
        if (f > 0.9d) {
            f6 = f5 + 1.0f;
        }
        float f7 = f6;
        if (f > 1.2d) {
            f7 = f6 + 1.0f;
        }
        return f7;
    }
}
