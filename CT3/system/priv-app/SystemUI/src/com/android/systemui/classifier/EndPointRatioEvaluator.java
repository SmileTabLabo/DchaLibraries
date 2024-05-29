package com.android.systemui.classifier;
/* loaded from: a.zip:com/android/systemui/classifier/EndPointRatioEvaluator.class */
public class EndPointRatioEvaluator {
    public static float evaluate(float f) {
        float f2 = 0.0f;
        if (f < 0.85d) {
            f2 = 1.0f;
        }
        float f3 = f2;
        if (f < 0.75d) {
            f3 = f2 + 1.0f;
        }
        float f4 = f3;
        if (f < 0.65d) {
            f4 = f3 + 1.0f;
        }
        float f5 = f4;
        if (f < 0.55d) {
            f5 = f4 + 1.0f;
        }
        float f6 = f5;
        if (f < 0.45d) {
            f6 = f5 + 1.0f;
        }
        float f7 = f6;
        if (f < 0.35d) {
            f7 = f6 + 1.0f;
        }
        return f7;
    }
}
