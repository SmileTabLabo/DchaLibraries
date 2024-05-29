package com.android.systemui.classifier;
/* loaded from: a.zip:com/android/systemui/classifier/EndPointLengthEvaluator.class */
public class EndPointLengthEvaluator {
    public static float evaluate(float f) {
        float f2 = 0.0f;
        if (f < 0.05d) {
            f2 = (float) 2.0d;
        }
        float f3 = f2;
        if (f < 0.1d) {
            f3 = (float) (f2 + 2.0d);
        }
        float f4 = f3;
        if (f < 0.2d) {
            f4 = (float) (f3 + 2.0d);
        }
        float f5 = f4;
        if (f < 0.3d) {
            f5 = (float) (f4 + 2.0d);
        }
        float f6 = f5;
        if (f < 0.4d) {
            f6 = (float) (f5 + 2.0d);
        }
        float f7 = f6;
        if (f < 0.5d) {
            f7 = (float) (f6 + 2.0d);
        }
        return f7;
    }
}
