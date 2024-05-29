package com.android.systemui.classifier;
/* loaded from: a.zip:com/android/systemui/classifier/EndPointRatioClassifier.class */
public class EndPointRatioClassifier extends StrokeClassifier {
    public EndPointRatioClassifier(ClassifierData classifierData) {
        this.mClassifierData = classifierData;
    }

    @Override // com.android.systemui.classifier.StrokeClassifier
    public float getFalseTouchEvaluation(int i, Stroke stroke) {
        return EndPointRatioEvaluator.evaluate(stroke.getTotalLength() == 0.0f ? 1.0f : stroke.getEndPointLength() / stroke.getTotalLength());
    }

    @Override // com.android.systemui.classifier.Classifier
    public String getTag() {
        return "END_RTIO";
    }
}
