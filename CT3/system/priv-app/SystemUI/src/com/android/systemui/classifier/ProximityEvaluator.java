package com.android.systemui.classifier;
/* loaded from: a.zip:com/android/systemui/classifier/ProximityEvaluator.class */
public class ProximityEvaluator {
    public static float evaluate(float f, int i) {
        float f2 = 0.0f;
        float f3 = 0.1f;
        if (i == 0) {
            f3 = 1.0f;
        }
        if (f >= f3) {
            f2 = (float) 2.0d;
        }
        return f2;
    }
}
