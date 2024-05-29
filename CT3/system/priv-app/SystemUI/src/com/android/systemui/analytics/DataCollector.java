package com.android.systemui.analytics;

import android.content.Context;
import android.database.ContentObserver;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.view.MotionEvent;
import com.android.systemui.statusbar.phone.TouchAnalyticsProto$Session;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
/* loaded from: a.zip:com/android/systemui/analytics/DataCollector.class */
public class DataCollector implements SensorEventListener {
    private static DataCollector sInstance = null;
    private final Context mContext;
    private final Handler mHandler = new Handler();
    private SensorLoggerSession mCurrentSession = null;
    private boolean mEnableCollector = false;
    private boolean mTimeoutActive = false;
    private boolean mCollectBadTouches = false;
    private boolean mCornerSwiping = false;
    private boolean mTrackingStarted = false;
    protected final ContentObserver mSettingsObserver = new ContentObserver(this, this.mHandler) { // from class: com.android.systemui.analytics.DataCollector.1
        final DataCollector this$0;

        {
            this.this$0 = this;
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z) {
            this.this$0.updateConfiguration();
        }
    };

    private DataCollector(Context context) {
        this.mContext = context;
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("data_collector_enable"), false, this.mSettingsObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("data_collector_collect_bad_touches"), false, this.mSettingsObserver, -1);
        updateConfiguration();
    }

    private void addEvent(int i) {
        if (!this.mEnableCollector || this.mCurrentSession == null) {
            return;
        }
        this.mCurrentSession.addPhoneEvent(i, System.nanoTime());
    }

    private void enforceTimeout() {
        if (!this.mTimeoutActive || System.currentTimeMillis() - this.mCurrentSession.getStartTimestampMillis() <= 11000) {
            return;
        }
        onSessionEnd(2);
    }

    public static DataCollector getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DataCollector(context);
        }
        return sInstance;
    }

    private void onSessionEnd(int i) {
        SensorLoggerSession sensorLoggerSession = this.mCurrentSession;
        this.mCurrentSession = null;
        sensorLoggerSession.end(System.currentTimeMillis(), i);
        queueSession(sensorLoggerSession);
    }

    private void onSessionStart() {
        this.mCornerSwiping = false;
        this.mTrackingStarted = false;
        this.mCurrentSession = new SensorLoggerSession(System.currentTimeMillis(), System.nanoTime());
    }

    private void queueSession(SensorLoggerSession sensorLoggerSession) {
        AsyncTask.execute(new Runnable(this, sensorLoggerSession) { // from class: com.android.systemui.analytics.DataCollector.2
            final DataCollector this$0;
            final SensorLoggerSession val$currentSession;

            {
                this.this$0 = this;
                this.val$currentSession = sensorLoggerSession;
            }

            @Override // java.lang.Runnable
            public void run() {
                String str;
                byte[] byteArray = TouchAnalyticsProto$Session.toByteArray(this.val$currentSession.toProto());
                String absolutePath = this.this$0.mContext.getFilesDir().getAbsolutePath();
                if (this.val$currentSession.getResult() == 1) {
                    str = absolutePath + "/good_touches";
                } else if (!this.this$0.mCollectBadTouches) {
                    return;
                } else {
                    str = absolutePath + "/bad_touches";
                }
                File file = new File(str);
                file.mkdir();
                try {
                    new FileOutputStream(new File(file, "trace_" + System.currentTimeMillis())).write(byteArray);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private boolean sessionEntrypoint() {
        if (this.mEnableCollector && this.mCurrentSession == null) {
            onSessionStart();
            return true;
        }
        return false;
    }

    private void sessionExitpoint(int i) {
        if (!this.mEnableCollector || this.mCurrentSession == null) {
            return;
        }
        onSessionEnd(i);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateConfiguration() {
        this.mEnableCollector = Build.IS_DEBUGGABLE && Settings.Secure.getInt(this.mContext.getContentResolver(), "data_collector_enable", 0) != 0;
        boolean z = false;
        if (this.mEnableCollector) {
            z = false;
            if (Settings.Secure.getInt(this.mContext.getContentResolver(), "data_collector_collect_bad_touches", 0) != 0) {
                z = true;
            }
        }
        this.mCollectBadTouches = z;
    }

    public boolean isEnabled() {
        return this.mEnableCollector;
    }

    @Override // android.hardware.SensorEventListener
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    public void onAffordanceSwipingAborted() {
        if (this.mCornerSwiping) {
            this.mCornerSwiping = false;
            addEvent(23);
        }
    }

    public void onAffordanceSwipingStarted(boolean z) {
        this.mCornerSwiping = true;
        if (z) {
            addEvent(21);
        } else {
            addEvent(22);
        }
    }

    public void onBouncerHidden() {
        addEvent(5);
    }

    public void onBouncerShown() {
        addEvent(4);
    }

    public void onCameraHintStarted() {
        addEvent(27);
    }

    public void onCameraOn() {
        addEvent(24);
    }

    public void onLeftAffordanceHintStarted() {
        addEvent(28);
    }

    public void onLeftAffordanceOn() {
        addEvent(25);
    }

    public void onNotificationActive() {
        addEvent(11);
    }

    public void onNotificationDismissed() {
        addEvent(18);
    }

    public void onNotificationDoubleTap() {
        addEvent(13);
    }

    public void onNotificatonStartDismissing() {
        addEvent(19);
    }

    public void onNotificatonStartDraggingDown() {
        addEvent(16);
    }

    public void onNotificatonStopDismissing() {
        addEvent(20);
    }

    public void onNotificatonStopDraggingDown() {
        addEvent(17);
    }

    public void onQsDown() {
        addEvent(6);
    }

    public void onScreenOff() {
        addEvent(2);
        sessionExitpoint(0);
    }

    public void onScreenOnFromTouch() {
        if (sessionEntrypoint()) {
            addEvent(1);
        }
    }

    public void onScreenTurningOn() {
        if (sessionEntrypoint()) {
            addEvent(0);
        }
    }

    @Override // android.hardware.SensorEventListener
    public void onSensorChanged(SensorEvent sensorEvent) {
        synchronized (this) {
            if (this.mEnableCollector && this.mCurrentSession != null) {
                this.mCurrentSession.addSensorEvent(sensorEvent, System.nanoTime());
                enforceTimeout();
            }
        }
    }

    public void onSucccessfulUnlock() {
        addEvent(3);
        sessionExitpoint(1);
    }

    public void onTouchEvent(MotionEvent motionEvent, int i, int i2) {
        if (this.mCurrentSession != null) {
            this.mCurrentSession.addMotionEvent(motionEvent);
            this.mCurrentSession.setTouchArea(i, i2);
            enforceTimeout();
        }
    }

    public void onTrackingStarted() {
        this.mTrackingStarted = true;
        addEvent(9);
    }

    public void onTrackingStopped() {
        if (this.mTrackingStarted) {
            this.mTrackingStarted = false;
            addEvent(10);
        }
    }

    public void onUnlockHintStarted() {
        addEvent(26);
    }

    public void setNotificationExpanded() {
        addEvent(14);
    }

    public void setQsExpanded(boolean z) {
        if (z) {
            addEvent(7);
        } else {
            addEvent(8);
        }
    }
}
