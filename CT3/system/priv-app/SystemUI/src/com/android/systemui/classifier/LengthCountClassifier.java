package com.android.systemui.classifier;
/* loaded from: a.zip:com/android/systemui/classifier/LengthCountClassifier.class */
public class LengthCountClassifier extends StrokeClassifier {
    public LengthCountClassifier(ClassifierData classifierData) {
    }

    @Override // com.android.systemui.classifier.StrokeClassifier
    public float getFalseTouchEvaluation(int i, Stroke stroke) {
        return LengthCountEvaluator.evaluate(stroke.getTotalLength() / Math.max(1.0f, stroke.getCount() - 2));
    }

    @Override // com.android.systemui.classifier.Classifier
    public String getTag() {
        return "LEN_CNT";
    }
}
