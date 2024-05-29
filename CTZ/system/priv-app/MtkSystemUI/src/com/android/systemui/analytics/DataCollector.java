package com.android.systemui.analytics;

import android.content.Context;
import android.database.ContentObserver;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.MotionEvent;
import android.widget.Toast;
import com.android.systemui.statusbar.phone.nano.TouchAnalyticsProto;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
/* loaded from: classes.dex */
public class DataCollector implements SensorEventListener {
    private static DataCollector sInstance = null;
    private final Context mContext;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private SensorLoggerSession mCurrentSession = null;
    private boolean mEnableCollector = false;
    private boolean mTimeoutActive = false;
    private boolean mCollectBadTouches = false;
    private boolean mCornerSwiping = false;
    private boolean mTrackingStarted = false;
    private boolean mAllowReportRejectedTouch = false;
    protected final ContentObserver mSettingsObserver = new ContentObserver(this.mHandler) { // from class: com.android.systemui.analytics.DataCollector.1
        @Override // android.database.ContentObserver
        public void onChange(boolean z) {
            DataCollector.this.updateConfiguration();
        }
    };

    private DataCollector(Context context) {
        this.mContext = context;
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("data_collector_enable"), false, this.mSettingsObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("data_collector_collect_bad_touches"), false, this.mSettingsObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("data_collector_allow_rejected_touch_reports"), false, this.mSettingsObserver, -1);
        updateConfiguration();
    }

    public static DataCollector getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DataCollector(context);
        }
        return sInstance;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateConfiguration() {
        boolean z = true;
        this.mEnableCollector = Build.IS_DEBUGGABLE && Settings.Secure.getInt(this.mContext.getContentResolver(), "data_collector_enable", 0) != 0;
        this.mCollectBadTouches = this.mEnableCollector && Settings.Secure.getInt(this.mContext.getContentResolver(), "data_collector_collect_bad_touches", 0) != 0;
        if (!Build.IS_DEBUGGABLE || Settings.Secure.getInt(this.mContext.getContentResolver(), "data_collector_allow_rejected_touch_reports", 0) == 0) {
            z = false;
        }
        this.mAllowReportRejectedTouch = z;
    }

    private boolean sessionEntrypoint() {
        if (isEnabled() && this.mCurrentSession == null) {
            onSessionStart();
            return true;
        }
        return false;
    }

    private void sessionExitpoint(int i) {
        if (this.mCurrentSession != null) {
            onSessionEnd(i);
        }
    }

    private void onSessionStart() {
        this.mCornerSwiping = false;
        this.mTrackingStarted = false;
        this.mCurrentSession = new SensorLoggerSession(System.currentTimeMillis(), System.nanoTime());
    }

    private void onSessionEnd(int i) {
        SensorLoggerSession sensorLoggerSession = this.mCurrentSession;
        this.mCurrentSession = null;
        if (this.mEnableCollector) {
            sensorLoggerSession.end(System.currentTimeMillis(), i);
            queueSession(sensorLoggerSession);
        }
    }

    public Uri reportRejectedTouch() {
        if (this.mCurrentSession == null) {
            Toast.makeText(this.mContext, "Generating rejected touch report failed: session timed out.", 1).show();
            return null;
        }
        SensorLoggerSession sensorLoggerSession = this.mCurrentSession;
        sensorLoggerSession.setType(4);
        sensorLoggerSession.end(System.currentTimeMillis(), 1);
        byte[] byteArray = TouchAnalyticsProto.Session.toByteArray(sensorLoggerSession.toProto());
        File file = new File(this.mContext.getExternalCacheDir(), "rejected_touch_reports");
        file.mkdir();
        File file2 = new File(file, "rejected_touch_report_" + System.currentTimeMillis());
        try {
            new FileOutputStream(file2).write(byteArray);
            return Uri.fromFile(file2);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void queueSession(final SensorLoggerSession sensorLoggerSession) {
        AsyncTask.execute(new Runnable() { // from class: com.android.systemui.analytics.DataCollector.2
            @Override // java.lang.Runnable
            public void run() {
                String str;
                byte[] byteArray = TouchAnalyticsProto.Session.toByteArray(sensorLoggerSession.toProto());
                String absolutePath = DataCollector.this.mContext.getFilesDir().getAbsolutePath();
                if (sensorLoggerSession.getResult() != 1) {
                    if (!DataCollector.this.mCollectBadTouches) {
                        return;
                    }
                    str = absolutePath + "/bad_touches";
                } else {
                    str = absolutePath + "/good_touches";
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

    @Override // android.hardware.SensorEventListener
    public synchronized void onSensorChanged(SensorEvent sensorEvent) {
        if (isEnabled() && this.mCurrentSession != null) {
            this.mCurrentSession.addSensorEvent(sensorEvent, System.nanoTime());
            enforceTimeout();
        }
    }

    private void enforceTimeout() {
        if (this.mTimeoutActive && System.currentTimeMillis() - this.mCurrentSession.getStartTimestampMillis() > 11000) {
            onSessionEnd(2);
        }
    }

    @Override // android.hardware.SensorEventListener
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    public boolean isEnabled() {
        return this.mEnableCollector || this.mAllowReportRejectedTouch;
    }

    public boolean isEnabledFull() {
        return this.mEnableCollector;
    }

    public void onScreenTurningOn() {
        if (sessionEntrypoint()) {
            addEvent(0);
        }
    }

    public void onScreenOnFromTouch() {
        if (sessionEntrypoint()) {
            addEvent(1);
        }
    }

    public void onScreenOff() {
        addEvent(2);
        sessionExitpoint(0);
    }

    public void onSucccessfulUnlock() {
        addEvent(3);
        sessionExitpoint(1);
    }

    public void onBouncerShown() {
        addEvent(4);
    }

    public void onBouncerHidden() {
        addEvent(5);
    }

    public void onQsDown() {
        addEvent(6);
    }

    public void setQsExpanded(boolean z) {
        if (z) {
            addEvent(7);
        } else {
            addEvent(8);
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

    public void onNotificationActive() {
        addEvent(11);
    }

    public void onNotificationDoubleTap() {
        addEvent(13);
    }

    public void setNotificationExpanded() {
        addEvent(14);
    }

    public void onNotificatonStartDraggingDown() {
        addEvent(16);
    }

    public void onNotificatonStopDraggingDown() {
        addEvent(17);
    }

    public void onNotificationDismissed() {
        addEvent(18);
    }

    public void onNotificatonStartDismissing() {
        addEvent(19);
    }

    public void onNotificatonStopDismissing() {
        addEvent(20);
    }

    public void onCameraOn() {
        addEvent(24);
    }

    public void onLeftAffordanceOn() {
        addEvent(25);
    }

    public void onAffordanceSwipingStarted(boolean z) {
        this.mCornerSwiping = true;
        if (z) {
            addEvent(21);
        } else {
            addEvent(22);
        }
    }

    public void onAffordanceSwipingAborted() {
        if (this.mCornerSwiping) {
            this.mCornerSwiping = false;
            addEvent(23);
        }
    }

    public void onUnlockHintStarted() {
        addEvent(26);
    }

    public void onCameraHintStarted() {
        addEvent(27);
    }

    public void onLeftAffordanceHintStarted() {
        addEvent(28);
    }

    public void onTouchEvent(MotionEvent motionEvent, int i, int i2) {
        if (this.mCurrentSession != null) {
            this.mCurrentSession.addMotionEvent(motionEvent);
            this.mCurrentSession.setTouchArea(i, i2);
            enforceTimeout();
        }
    }

    private void addEvent(int i) {
        if (isEnabled() && this.mCurrentSession != null) {
            this.mCurrentSession.addPhoneEvent(i, System.nanoTime());
        }
    }

    public boolean isReportingEnabled() {
        return this.mAllowReportRejectedTouch;
    }

    public void onFalsingSessionStarted() {
        sessionEntrypoint();
    }
}
