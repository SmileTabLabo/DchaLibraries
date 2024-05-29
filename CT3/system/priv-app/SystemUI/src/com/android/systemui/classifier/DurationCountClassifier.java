package com.android.systemui.classifier;
/* loaded from: a.zip:com/android/systemui/classifier/DurationCountClassifier.class */
public class DurationCountClassifier extends StrokeClassifier {
    public DurationCountClassifier(ClassifierData classifierData) {
    }

    @Override // com.android.systemui.classifier.StrokeClassifier
    public float getFalseTouchEvaluation(int i, Stroke stroke) {
        return DurationCountEvaluator.evaluate(stroke.getDurationSeconds() / stroke.getCount());
    }

    @Override // com.android.systemui.classifier.Classifier
    public String getTag() {
        return "DUR";
    }
}
