package com.android.systemui.classifier;
/* loaded from: a.zip:com/android/systemui/classifier/SpeedClassifier.class */
public class SpeedClassifier extends StrokeClassifier {
    private final float NANOS_TO_SECONDS = 1.0E9f;

    public SpeedClassifier(ClassifierData classifierData) {
    }

    @Override // com.android.systemui.classifier.StrokeClassifier
    public float getFalseTouchEvaluation(int i, Stroke stroke) {
        float durationNanos = ((float) stroke.getDurationNanos()) / 1.0E9f;
        return durationNanos == 0.0f ? SpeedEvaluator.evaluate(0.0f) : SpeedEvaluator.evaluate(stroke.getTotalLength() / durationNanos);
    }

    @Override // com.android.systemui.classifier.Classifier
    public String getTag() {
        return "SPD";
    }
}
