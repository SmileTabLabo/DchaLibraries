package com.android.systemui.classifier;

import android.content.Context;
import android.database.ContentObserver;
import android.hardware.SensorEvent;
import android.os.Handler;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import java.util.ArrayDeque;
/* loaded from: a.zip:com/android/systemui/classifier/HumanInteractionClassifier.class */
public class HumanInteractionClassifier extends Classifier {
    private static HumanInteractionClassifier sInstance = null;
    private final Context mContext;
    private final float mDpi;
    private final GestureClassifier[] mGestureClassifiers;
    private final HistoryEvaluator mHistoryEvaluator;
    private final StrokeClassifier[] mStrokeClassifiers;
    private final Handler mHandler = new Handler();
    private final ArrayDeque<MotionEvent> mBufferedEvents = new ArrayDeque<>();
    private boolean mEnableClassifier = false;
    private int mCurrentType = 7;
    protected final ContentObserver mSettingsObserver = new ContentObserver(this, this.mHandler) { // from class: com.android.systemui.classifier.HumanInteractionClassifier.1
        final HumanInteractionClassifier this$0;

        {
            this.this$0 = this;
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z) {
            this.this$0.updateConfiguration();
        }
    };

    private HumanInteractionClassifier(Context context) {
        this.mContext = context;
        DisplayMetrics displayMetrics = this.mContext.getResources().getDisplayMetrics();
        this.mDpi = (displayMetrics.xdpi + displayMetrics.ydpi) / 2.0f;
        this.mClassifierData = new ClassifierData(this.mDpi);
        this.mHistoryEvaluator = new HistoryEvaluator();
        this.mStrokeClassifiers = new StrokeClassifier[]{new AnglesClassifier(this.mClassifierData), new SpeedClassifier(this.mClassifierData), new DurationCountClassifier(this.mClassifierData), new EndPointRatioClassifier(this.mClassifierData), new EndPointLengthClassifier(this.mClassifierData), new AccelerationClassifier(this.mClassifierData), new SpeedAnglesClassifier(this.mClassifierData), new LengthCountClassifier(this.mClassifierData), new DirectionClassifier(this.mClassifierData)};
        this.mGestureClassifiers = new GestureClassifier[]{new PointerCountClassifier(this.mClassifierData), new ProximityClassifier(this.mClassifierData)};
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("HIC_enable"), false, this.mSettingsObserver, -1);
        updateConfiguration();
    }

    private void addTouchEvent(MotionEvent motionEvent) {
        GestureClassifier[] gestureClassifierArr;
        StrokeClassifier[] strokeClassifierArr;
        this.mClassifierData.update(motionEvent);
        for (StrokeClassifier strokeClassifier : this.mStrokeClassifiers) {
            strokeClassifier.onTouchEvent(motionEvent);
        }
        for (GestureClassifier gestureClassifier : this.mGestureClassifiers) {
            gestureClassifier.onTouchEvent(motionEvent);
        }
        int size = this.mClassifierData.getEndingStrokes().size();
        for (int i = 0; i < size; i++) {
            Stroke stroke = this.mClassifierData.getEndingStrokes().get(i);
            float f = 0.0f;
            StringBuilder sb = FalsingLog.ENABLED ? new StringBuilder("stroke") : null;
            for (StrokeClassifier strokeClassifier2 : this.mStrokeClassifiers) {
                float falseTouchEvaluation = strokeClassifier2.getFalseTouchEvaluation(this.mCurrentType, stroke);
                if (FalsingLog.ENABLED) {
                    String tag = strokeClassifier2.getTag();
                    StringBuilder append = sb.append(" ");
                    if (falseTouchEvaluation < 1.0f) {
                        tag = tag.toLowerCase();
                    }
                    append.append(tag).append("=").append(falseTouchEvaluation);
                }
                f += falseTouchEvaluation;
            }
            if (FalsingLog.ENABLED) {
                FalsingLog.i(" addTouchEvent", sb.toString());
            }
            this.mHistoryEvaluator.addStroke(f);
        }
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == 1 || actionMasked == 3) {
            float f2 = 0.0f;
            StringBuilder sb2 = FalsingLog.ENABLED ? new StringBuilder("gesture") : null;
            for (GestureClassifier gestureClassifier2 : this.mGestureClassifiers) {
                float falseTouchEvaluation2 = gestureClassifier2.getFalseTouchEvaluation(this.mCurrentType);
                if (FalsingLog.ENABLED) {
                    String tag2 = gestureClassifier2.getTag();
                    StringBuilder append2 = sb2.append(" ");
                    if (falseTouchEvaluation2 < 1.0f) {
                        tag2 = tag2.toLowerCase();
                    }
                    append2.append(tag2).append("=").append(falseTouchEvaluation2);
                }
                f2 += falseTouchEvaluation2;
            }
            if (FalsingLog.ENABLED) {
                FalsingLog.i(" addTouchEvent", sb2.toString());
            }
            this.mHistoryEvaluator.addGesture(f2);
            setType(7);
        }
        this.mClassifierData.cleanUp(motionEvent);
    }

    public static HumanInteractionClassifier getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new HumanInteractionClassifier(context);
        }
        return sInstance;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateConfiguration() {
        boolean z = true;
        if (Settings.Global.getInt(this.mContext.getContentResolver(), "HIC_enable", 1) == 0) {
            z = false;
        }
        this.mEnableClassifier = z;
    }

    @Override // com.android.systemui.classifier.Classifier
    public String getTag() {
        return "HIC";
    }

    public boolean isEnabled() {
        return this.mEnableClassifier;
    }

    public boolean isFalseTouch() {
        int i = 0;
        if (this.mEnableClassifier) {
            float evaluation = this.mHistoryEvaluator.getEvaluation();
            boolean z = evaluation >= 5.0f;
            if (FalsingLog.ENABLED) {
                StringBuilder append = new StringBuilder().append("eval=").append(evaluation).append(" result=");
                if (z) {
                    i = 1;
                }
                FalsingLog.i("isFalseTouch", append.append(i).toString());
            }
            return z;
        }
        return false;
    }

    @Override // com.android.systemui.classifier.Classifier
    public void onSensorChanged(SensorEvent sensorEvent) {
        for (StrokeClassifier strokeClassifier : this.mStrokeClassifiers) {
            strokeClassifier.onSensorChanged(sensorEvent);
        }
        for (GestureClassifier gestureClassifier : this.mGestureClassifiers) {
            gestureClassifier.onSensorChanged(sensorEvent);
        }
    }

    @Override // com.android.systemui.classifier.Classifier
    public void onTouchEvent(MotionEvent motionEvent) {
        if (this.mEnableClassifier) {
            if (this.mCurrentType != 2) {
                addTouchEvent(motionEvent);
                return;
            }
            this.mBufferedEvents.add(MotionEvent.obtain(motionEvent));
            Point point = new Point(motionEvent.getX() / this.mDpi, motionEvent.getY() / this.mDpi);
            while (point.dist(new Point(this.mBufferedEvents.getFirst().getX() / this.mDpi, this.mBufferedEvents.getFirst().getY() / this.mDpi)) > 0.1f) {
                addTouchEvent(this.mBufferedEvents.getFirst());
                this.mBufferedEvents.remove();
            }
            if (motionEvent.getActionMasked() == 1) {
                this.mBufferedEvents.getFirst().setAction(1);
                addTouchEvent(this.mBufferedEvents.getFirst());
                this.mBufferedEvents.clear();
            }
        }
    }

    public void setType(int i) {
        this.mCurrentType = i;
    }
}
