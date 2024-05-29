package com.android.systemui.classifier;
/* loaded from: a.zip:com/android/systemui/classifier/DurationCountEvaluator.class */
public class DurationCountEvaluator {
    public static float evaluate(float f) {
        float f2 = 0.0f;
        if (f < 0.0105d) {
            f2 = 1.0f;
        }
        float f3 = f2;
        if (f < 0.00909d) {
            f3 = f2 + 1.0f;
        }
        float f4 = f3;
        if (f < 0.00667d) {
            f4 = f3 + 1.0f;
        }
        float f5 = f4;
        if (f > 0.0333d) {
            f5 = f4 + 1.0f;
        }
        float f6 = f5;
        if (f > 0.05d) {
            f6 = f5 + 1.0f;
        }
        return f6;
    }
}
