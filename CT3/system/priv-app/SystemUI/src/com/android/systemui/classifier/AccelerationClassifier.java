package com.android.systemui.classifier;

import android.view.MotionEvent;
import java.util.HashMap;
/* loaded from: a.zip:com/android/systemui/classifier/AccelerationClassifier.class */
public class AccelerationClassifier extends StrokeClassifier {
    private final HashMap<Stroke, Data> mStrokeMap = new HashMap<>();

    /* loaded from: a.zip:com/android/systemui/classifier/AccelerationClassifier$Data.class */
    private static class Data {
        public Point previousPoint;
        public float previousDistance = 0.0f;
        public float previousSpeed = 0.0f;
        public float maxSpeedRatio = 0.0f;
        public float maxDistanceRatio = 0.0f;

        public Data(Point point) {
            this.previousPoint = point;
        }

        public void addPoint(Point point) {
            float dist = this.previousPoint.dist(point);
            float f = dist / ((float) ((point.timeOffsetNano - this.previousPoint.timeOffsetNano) + 1));
            if (this.previousDistance != 0.0f) {
                this.maxDistanceRatio = Math.max(this.maxDistanceRatio, dist / this.previousDistance);
            }
            if (this.previousSpeed != 0.0f) {
                this.maxSpeedRatio = Math.max(this.maxSpeedRatio, f / this.previousSpeed);
            }
            this.previousDistance = dist;
            this.previousSpeed = f;
            this.previousPoint = point;
        }
    }

    public AccelerationClassifier(ClassifierData classifierData) {
        this.mClassifierData = classifierData;
    }

    @Override // com.android.systemui.classifier.StrokeClassifier
    public float getFalseTouchEvaluation(int i, Stroke stroke) {
        Data data = this.mStrokeMap.get(stroke);
        return SpeedRatioEvaluator.evaluate(data.maxSpeedRatio) + DistanceRatioEvaluator.evaluate(data.maxDistanceRatio);
    }

    @Override // com.android.systemui.classifier.Classifier
    public String getTag() {
        return "ACC";
    }

    @Override // com.android.systemui.classifier.Classifier
    public void onTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getActionMasked() == 0) {
            this.mStrokeMap.clear();
        }
        for (int i = 0; i < motionEvent.getPointerCount(); i++) {
            Stroke stroke = this.mClassifierData.getStroke(motionEvent.getPointerId(i));
            Point point = stroke.getPoints().get(stroke.getPoints().size() - 1);
            if (this.mStrokeMap.get(stroke) == null) {
                this.mStrokeMap.put(stroke, new Data(point));
            } else {
                this.mStrokeMap.get(stroke).addPoint(point);
            }
        }
    }
}
