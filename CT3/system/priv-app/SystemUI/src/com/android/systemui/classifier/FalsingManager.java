package com.android.systemui.classifier;

import android.content.Context;
import android.database.ContentObserver;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.accessibility.AccessibilityManager;
import com.android.systemui.analytics.DataCollector;
import com.android.systemui.statusbar.StatusBarState;
import java.io.PrintWriter;
/* loaded from: a.zip:com/android/systemui/classifier/FalsingManager.class */
public class FalsingManager implements SensorEventListener {
    private static final int[] CLASSIFIER_SENSORS = {8};
    private static final int[] COLLECTOR_SENSORS = {1, 4, 8, 5, 11};
    private static FalsingManager sInstance = null;
    private final AccessibilityManager mAccessibilityManager;
    private final Context mContext;
    private final DataCollector mDataCollector;
    private final HumanInteractionClassifier mHumanInteractionClassifier;
    private boolean mScreenOn;
    private final SensorManager mSensorManager;
    private final Handler mHandler = new Handler();
    private boolean mEnforceBouncer = false;
    private boolean mBouncerOn = false;
    private boolean mSessionActive = false;
    private int mState = 0;
    protected final ContentObserver mSettingsObserver = new ContentObserver(this, this.mHandler) { // from class: com.android.systemui.classifier.FalsingManager.1
        final FalsingManager this$0;

        {
            this.this$0 = this;
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z) {
            this.this$0.updateConfiguration();
        }
    };

    private FalsingManager(Context context) {
        this.mContext = context;
        this.mSensorManager = (SensorManager) this.mContext.getSystemService(SensorManager.class);
        this.mAccessibilityManager = (AccessibilityManager) context.getSystemService(AccessibilityManager.class);
        this.mDataCollector = DataCollector.getInstance(this.mContext);
        this.mHumanInteractionClassifier = HumanInteractionClassifier.getInstance(this.mContext);
        this.mScreenOn = ((PowerManager) context.getSystemService(PowerManager.class)).isInteractive();
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("falsing_manager_enforce_bouncer"), false, this.mSettingsObserver, -1);
        updateConfiguration();
    }

    public static FalsingManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new FalsingManager(context);
        }
        return sInstance;
    }

    private boolean isEnabled() {
        return !this.mHumanInteractionClassifier.isEnabled() ? this.mDataCollector.isEnabled() : true;
    }

    private void onSessionStart() {
        if (FalsingLog.ENABLED) {
            FalsingLog.i("onSessionStart", "classifierEnabled=" + isClassiferEnabled());
        }
        this.mBouncerOn = false;
        this.mSessionActive = true;
        if (this.mHumanInteractionClassifier.isEnabled()) {
            registerSensors(CLASSIFIER_SENSORS);
        }
        if (this.mDataCollector.isEnabled()) {
            registerSensors(COLLECTOR_SENSORS);
        }
    }

    private void registerSensors(int[] iArr) {
        for (int i : iArr) {
            Sensor defaultSensor = this.mSensorManager.getDefaultSensor(i);
            if (defaultSensor != null) {
                this.mSensorManager.registerListener(this, defaultSensor, 1);
            }
        }
    }

    private boolean sessionEntrypoint() {
        if (this.mSessionActive || !shouldSessionBeActive()) {
            return false;
        }
        onSessionStart();
        return true;
    }

    private void sessionExitpoint(boolean z) {
        if (this.mSessionActive) {
            if (z || !shouldSessionBeActive()) {
                this.mSessionActive = false;
                this.mSensorManager.unregisterListener(this);
            }
        }
    }

    private boolean shouldSessionBeActive() {
        boolean z = true;
        if (FalsingLog.ENABLED) {
        }
        if (!isEnabled() || !this.mScreenOn || this.mState != 1) {
            z = false;
        }
        return z;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateConfiguration() {
        boolean z = false;
        if (Settings.Secure.getInt(this.mContext.getContentResolver(), "falsing_manager_enforce_bouncer", 0) != 0) {
            z = true;
        }
        this.mEnforceBouncer = z;
    }

    public void dump(PrintWriter printWriter) {
        printWriter.println("FALSING MANAGER");
        printWriter.print("classifierEnabled=");
        printWriter.println(isClassiferEnabled() ? 1 : 0);
        printWriter.print("mSessionActive=");
        printWriter.println(this.mSessionActive ? 1 : 0);
        printWriter.print("mBouncerOn=");
        printWriter.println(this.mSessionActive ? 1 : 0);
        printWriter.print("mState=");
        printWriter.println(StatusBarState.toShortString(this.mState));
        printWriter.print("mScreenOn=");
        printWriter.println(this.mScreenOn ? 1 : 0);
        printWriter.println();
    }

    public boolean isClassiferEnabled() {
        return this.mHumanInteractionClassifier.isEnabled();
    }

    public boolean isFalseTouch() {
        if (FalsingLog.ENABLED && !this.mSessionActive && ((PowerManager) this.mContext.getSystemService(PowerManager.class)).isInteractive()) {
            FalsingLog.wtf("isFalseTouch", "Session is not active, yet there's a query for a false touch. enabled=" + (isEnabled() ? 1 : 0) + " mScreenOn=" + (this.mScreenOn ? 1 : 0) + " mState=" + StatusBarState.toShortString(this.mState));
        }
        if (this.mAccessibilityManager.isTouchExplorationEnabled()) {
            return false;
        }
        return this.mHumanInteractionClassifier.isFalseTouch();
    }

    @Override // android.hardware.SensorEventListener
    public void onAccuracyChanged(Sensor sensor, int i) {
        this.mDataCollector.onAccuracyChanged(sensor, i);
    }

    public void onAffordanceSwipingAborted() {
        this.mDataCollector.onAffordanceSwipingAborted();
    }

    public void onAffordanceSwipingStarted(boolean z) {
        if (FalsingLog.ENABLED) {
            FalsingLog.i("onAffordanceSwipingStarted", "");
        }
        if (z) {
            this.mHumanInteractionClassifier.setType(6);
        } else {
            this.mHumanInteractionClassifier.setType(5);
        }
        this.mDataCollector.onAffordanceSwipingStarted(z);
    }

    public void onBouncerHidden() {
        if (FalsingLog.ENABLED) {
            FalsingLog.i("onBouncerHidden", "from=" + (this.mBouncerOn ? 1 : 0));
        }
        if (this.mBouncerOn) {
            this.mBouncerOn = false;
            this.mDataCollector.onBouncerHidden();
        }
    }

    public void onBouncerShown() {
        if (FalsingLog.ENABLED) {
            FalsingLog.i("onBouncerShown", "from=" + (this.mBouncerOn ? 1 : 0));
        }
        if (this.mBouncerOn) {
            return;
        }
        this.mBouncerOn = true;
        this.mDataCollector.onBouncerShown();
    }

    public void onCameraHintStarted() {
        this.mDataCollector.onCameraHintStarted();
    }

    public void onCameraOn() {
        this.mDataCollector.onCameraOn();
    }

    public void onLeftAffordanceHintStarted() {
        this.mDataCollector.onLeftAffordanceHintStarted();
    }

    public void onLeftAffordanceOn() {
        this.mDataCollector.onLeftAffordanceOn();
    }

    public void onNotificationActive() {
        this.mDataCollector.onNotificationActive();
    }

    public void onNotificationDismissed() {
        this.mDataCollector.onNotificationDismissed();
    }

    public void onNotificationDoubleTap() {
        this.mDataCollector.onNotificationDoubleTap();
    }

    public void onNotificatonStartDismissing() {
        if (FalsingLog.ENABLED) {
            FalsingLog.i("onNotificatonStartDismissing", "");
        }
        this.mHumanInteractionClassifier.setType(1);
        this.mDataCollector.onNotificatonStartDismissing();
    }

    public void onNotificatonStartDraggingDown() {
        if (FalsingLog.ENABLED) {
            FalsingLog.i("onNotificatonStartDraggingDown", "");
        }
        this.mHumanInteractionClassifier.setType(2);
        this.mDataCollector.onNotificatonStartDraggingDown();
    }

    public void onNotificatonStopDismissing() {
        this.mDataCollector.onNotificatonStopDismissing();
    }

    public void onNotificatonStopDraggingDown() {
        this.mDataCollector.onNotificatonStopDraggingDown();
    }

    public void onQsDown() {
        if (FalsingLog.ENABLED) {
            FalsingLog.i("onQsDown", "");
        }
        this.mHumanInteractionClassifier.setType(0);
        this.mDataCollector.onQsDown();
    }

    public void onScreenOff() {
        if (FalsingLog.ENABLED) {
            FalsingLog.i("onScreenOff", "from=" + (this.mScreenOn ? 1 : 0));
        }
        this.mDataCollector.onScreenOff();
        this.mScreenOn = false;
        sessionExitpoint(false);
    }

    public void onScreenOnFromTouch() {
        if (FalsingLog.ENABLED) {
            FalsingLog.i("onScreenOnFromTouch", "from=" + (this.mScreenOn ? 1 : 0));
        }
        this.mScreenOn = true;
        if (sessionEntrypoint()) {
            this.mDataCollector.onScreenOnFromTouch();
        }
    }

    public void onScreenTurningOn() {
        if (FalsingLog.ENABLED) {
            FalsingLog.i("onScreenTurningOn", "from=" + (this.mScreenOn ? 1 : 0));
        }
        this.mScreenOn = true;
        if (sessionEntrypoint()) {
            this.mDataCollector.onScreenTurningOn();
        }
    }

    @Override // android.hardware.SensorEventListener
    public void onSensorChanged(SensorEvent sensorEvent) {
        synchronized (this) {
            this.mDataCollector.onSensorChanged(sensorEvent);
            this.mHumanInteractionClassifier.onSensorChanged(sensorEvent);
        }
    }

    public void onSucccessfulUnlock() {
        if (FalsingLog.ENABLED) {
            FalsingLog.i("onSucccessfulUnlock", "");
        }
        this.mDataCollector.onSucccessfulUnlock();
    }

    public void onTouchEvent(MotionEvent motionEvent, int i, int i2) {
        if (!this.mSessionActive || this.mBouncerOn) {
            return;
        }
        this.mDataCollector.onTouchEvent(motionEvent, i, i2);
        this.mHumanInteractionClassifier.onTouchEvent(motionEvent);
    }

    public void onTrackingStarted() {
        if (FalsingLog.ENABLED) {
            FalsingLog.i("onTrackingStarted", "");
        }
        this.mHumanInteractionClassifier.setType(4);
        this.mDataCollector.onTrackingStarted();
    }

    public void onTrackingStopped() {
        this.mDataCollector.onTrackingStopped();
    }

    public void onUnlockHintStarted() {
        this.mDataCollector.onUnlockHintStarted();
    }

    public void setNotificationExpanded() {
        this.mDataCollector.setNotificationExpanded();
    }

    public void setQsExpanded(boolean z) {
        this.mDataCollector.setQsExpanded(z);
    }

    public void setStatusBarState(int i) {
        if (FalsingLog.ENABLED) {
            FalsingLog.i("setStatusBarState", "from=" + StatusBarState.toShortString(this.mState) + " to=" + StatusBarState.toShortString(i));
        }
        this.mState = i;
        if (shouldSessionBeActive()) {
            sessionEntrypoint();
        } else {
            sessionExitpoint(false);
        }
    }

    public boolean shouldEnforceBouncer() {
        return this.mEnforceBouncer;
    }
}
