package com.android.systemui.classifier;

import java.util.ArrayList;
/* loaded from: a.zip:com/android/systemui/classifier/HistoryEvaluator.class */
public class HistoryEvaluator {
    private final ArrayList<Data> mStrokes = new ArrayList<>();
    private final ArrayList<Data> mGestureWeights = new ArrayList<>();
    private long mLastUpdate = System.currentTimeMillis();

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/classifier/HistoryEvaluator$Data.class */
    public static class Data {
        public float evaluation;
        public float weight = 1.0f;

        public Data(float f) {
            this.evaluation = f;
        }
    }

    private void decayValue() {
        long currentTimeMillis = System.currentTimeMillis();
        float pow = (float) Math.pow(0.8999999761581421d, ((float) (currentTimeMillis - this.mLastUpdate)) / 50.0f);
        decayValue(this.mStrokes, pow);
        decayValue(this.mGestureWeights, pow);
        this.mLastUpdate = currentTimeMillis;
    }

    private void decayValue(ArrayList<Data> arrayList, float f) {
        int size = arrayList.size();
        for (int i = 0; i < size; i++) {
            arrayList.get(i).weight *= f;
        }
        while (!arrayList.isEmpty() && isZero(arrayList.get(0).weight)) {
            arrayList.remove(0);
        }
    }

    private boolean isZero(float f) {
        boolean z = false;
        if (f <= 1.0E-5f) {
            z = false;
            if (f >= -1.0E-5f) {
                z = true;
            }
        }
        return z;
    }

    private float weightedAverage(ArrayList<Data> arrayList) {
        float f = 0.0f;
        float f2 = 0.0f;
        int size = arrayList.size();
        for (int i = 0; i < size; i++) {
            Data data = arrayList.get(i);
            f += data.evaluation * data.weight;
            f2 += data.weight;
        }
        if (f2 == 0.0f) {
            return 0.0f;
        }
        return f / f2;
    }

    public void addGesture(float f) {
        decayValue();
        this.mGestureWeights.add(new Data(f));
    }

    public void addStroke(float f) {
        decayValue();
        this.mStrokes.add(new Data(f));
    }

    public float getEvaluation() {
        return weightedAverage(this.mStrokes) + weightedAverage(this.mGestureWeights);
    }
}
