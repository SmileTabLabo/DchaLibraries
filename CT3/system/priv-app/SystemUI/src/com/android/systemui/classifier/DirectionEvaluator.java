package com.android.systemui.classifier;
/* loaded from: a.zip:com/android/systemui/classifier/DirectionEvaluator.class */
public class DirectionEvaluator {
    public static float evaluate(float f, float f2, int i) {
        boolean z = Math.abs(f2) >= Math.abs(f);
        switch (i) {
            case 0:
            case 2:
                return (!z || ((double) f2) <= 0.0d) ? 5.5f : 0.0f;
            case 1:
                return z ? 5.5f : 0.0f;
            case 3:
            default:
                return 0.0f;
            case 4:
                return (!z || ((double) f2) >= 0.0d) ? 5.5f : 0.0f;
            case 5:
                return (((double) f) >= 0.0d || ((double) f2) <= 0.0d) ? 0.0f : 5.5f;
            case 6:
                return (((double) f) <= 0.0d || ((double) f2) <= 0.0d) ? 0.0f : 5.5f;
        }
    }
}
