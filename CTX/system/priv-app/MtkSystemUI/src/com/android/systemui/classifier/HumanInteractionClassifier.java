package com.android.systemui.classifier;

import android.content.Context;
import android.database.ContentObserver;
import android.hardware.SensorEvent;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import com.android.systemui.R;
import java.util.ArrayDeque;
/* loaded from: classes.dex */
public class HumanInteractionClassifier extends Classifier {
    private static HumanInteractionClassifier sInstance = null;
    private final Context mContext;
    private final float mDpi;
    private final GestureClassifier[] mGestureClassifiers;
    private final HistoryEvaluator mHistoryEvaluator;
    private final StrokeClassifier[] mStrokeClassifiers;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final ArrayDeque<MotionEvent> mBufferedEvents = new ArrayDeque<>();
    private boolean mEnableClassifier = false;
    private int mCurrentType = 7;
    protected final ContentObserver mSettingsObserver = new ContentObserver(this.mHandler) { // from class: com.android.systemui.classifier.HumanInteractionClassifier.1
        @Override // android.database.ContentObserver
        public void onChange(boolean z) {
            HumanInteractionClassifier.this.updateConfiguration();
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

    public static HumanInteractionClassifier getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new HumanInteractionClassifier(context);
        }
        return sInstance;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateConfiguration() {
        this.mEnableClassifier = Settings.Global.getInt(this.mContext.getContentResolver(), "HIC_enable", this.mContext.getResources().getBoolean(R.bool.config_lockscreenAntiFalsingClassifierEnabled) ? 1 : 0) != 0;
    }

    public void setType(int i) {
        this.mCurrentType = i;
    }

    @Override // com.android.systemui.classifier.Classifier
    public void onTouchEvent(MotionEvent motionEvent) {
        if (!this.mEnableClassifier) {
            return;
        }
        if (this.mCurrentType == 2) {
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
                return;
            }
            return;
        }
        addTouchEvent(motionEvent);
    }

    private void addTouchEvent(MotionEvent motionEvent) {
        float f;
        StringBuilder sb;
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
        int i = 0;
        while (true) {
            f = 0.0f;
            if (i >= size) {
                break;
            }
            Stroke stroke = this.mClassifierData.getEndingStrokes().get(i);
            sb = FalsingLog.ENABLED ? new StringBuilder("stroke") : null;
            float f2 = 0.0f;
            for (StrokeClassifier strokeClassifier2 : this.mStrokeClassifiers) {
                float falseTouchEvaluation = strokeClassifier2.getFalseTouchEvaluation(this.mCurrentType, stroke);
                if (FalsingLog.ENABLED) {
                    String tag = strokeClassifier2.getTag();
                    sb.append(" ");
                    if (falseTouchEvaluation < 1.0f) {
                        tag = tag.toLowerCase();
                    }
                    sb.append(tag);
                    sb.append("=");
                    sb.append(falseTouchEvaluation);
                }
                f2 += falseTouchEvaluation;
            }
            if (FalsingLog.ENABLED) {
                FalsingLog.i(" addTouchEvent", sb.toString());
            }
            this.mHistoryEvaluator.addStroke(f2);
            i++;
        }
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == 1 || actionMasked == 3) {
            sb = FalsingLog.ENABLED ? new StringBuilder("gesture") : null;
            for (GestureClassifier gestureClassifier2 : this.mGestureClassifiers) {
                float falseTouchEvaluation2 = gestureClassifier2.getFalseTouchEvaluation(this.mCurrentType);
                if (FalsingLog.ENABLED) {
                    String tag2 = gestureClassifier2.getTag();
                    sb.append(" ");
                    if (falseTouchEvaluation2 < 1.0f) {
                        tag2 = tag2.toLowerCase();
                    }
                    sb.append(tag2);
                    sb.append("=");
                    sb.append(falseTouchEvaluation2);
                }
                f += falseTouchEvaluation2;
            }
            if (FalsingLog.ENABLED) {
                FalsingLog.i(" addTouchEvent", sb.toString());
            }
            this.mHistoryEvaluator.addGesture(f);
            setType(7);
        }
        this.mClassifierData.cleanUp(motionEvent);
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

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r1v0 */
    /* JADX WARN: Type inference failed for: r1v1, types: [boolean, int] */
    /* JADX WARN: Type inference failed for: r1v2 */
    public boolean isFalseTouch() {
        if (this.mEnableClassifier) {
            float evaluation = this.mHistoryEvaluator.getEvaluation();
            ?? r1 = evaluation >= 5.0f ? 1 : 0;
            if (FalsingLog.ENABLED) {
                FalsingLog.i("isFalseTouch", "eval=" + evaluation + " result=" + ((int) r1));
            }
            return r1;
        }
        return false;
    }

    public boolean isEnabled() {
        return this.mEnableClassifier;
    }

    @Override // com.android.systemui.classifier.Classifier
    public String getTag() {
        return "HIC";
    }
}
