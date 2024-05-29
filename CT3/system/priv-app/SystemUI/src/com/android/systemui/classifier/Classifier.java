package com.android.systemui.classifier;

import android.hardware.SensorEvent;
import android.view.MotionEvent;
/* loaded from: a.zip:com/android/systemui/classifier/Classifier.class */
public abstract class Classifier {
    protected ClassifierData mClassifierData;

    public abstract String getTag();

    public void onSensorChanged(SensorEvent sensorEvent) {
    }

    public void onTouchEvent(MotionEvent motionEvent) {
    }
}
