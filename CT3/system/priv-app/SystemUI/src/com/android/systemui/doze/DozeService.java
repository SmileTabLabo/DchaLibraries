package com.android.systemui.doze;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.UiModeManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.media.AudioAttributes;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.Vibrator;
import android.service.dreams.DreamService;
import android.util.Log;
import com.android.systemui.SystemUIApplication;
import com.android.systemui.doze.DozeHost;
import com.android.systemui.statusbar.phone.DozeParameters;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Date;
/* loaded from: a.zip:com/android/systemui/doze/DozeService.class */
public class DozeService extends DreamService {
    private static final boolean DEBUG = Log.isLoggable("DozeService", 3);
    private AlarmManager mAlarmManager;
    private boolean mBroadcastReceiverRegistered;
    private boolean mCarMode;
    private boolean mDisplayStateSupported;
    private boolean mDreaming;
    private long mEarliestPulseDueToLight;
    private DozeHost mHost;
    private long mLastScheduleResetTime;
    private boolean mNotificationLightOn;
    private long mNotificationPulseTime;
    private TriggerSensor mPickupSensor;
    private PowerManager mPowerManager;
    private boolean mPowerSaveActive;
    private boolean mPulsing;
    private int mScheduleResetsRemaining;
    private SensorManager mSensors;
    private TriggerSensor mSigMotionSensor;
    private UiModeManager mUiModeManager;
    private PowerManager.WakeLock mWakeLock;
    private final String mTag = String.format("DozeService.%08x", Integer.valueOf(hashCode()));
    private final Context mContext = this;
    private final DozeParameters mDozeParameters = new DozeParameters(this.mContext);
    private final Handler mHandler = new Handler();
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver(this) { // from class: com.android.systemui.doze.DozeService.1
        final DozeService this$0;

        {
            this.this$0 = this;
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if ("com.android.systemui.doze.pulse".equals(intent.getAction())) {
                if (DozeService.DEBUG) {
                    Log.d(this.this$0.mTag, "Received pulse intent");
                }
                this.this$0.requestPulse(0);
            }
            if ("com.android.systemui.doze.notification_pulse".equals(intent.getAction())) {
                long longExtra = intent.getLongExtra("instance", -1L);
                if (DozeService.DEBUG) {
                    Log.d(this.this$0.mTag, "Received notification pulse intent instance=" + longExtra);
                }
                DozeLog.traceNotificationPulse(longExtra);
                this.this$0.requestPulse(1);
                this.this$0.rescheduleNotificationPulse(this.this$0.mNotificationLightOn);
            }
            if (UiModeManager.ACTION_ENTER_CAR_MODE.equals(intent.getAction())) {
                this.this$0.mCarMode = true;
                if (this.this$0.mCarMode && this.this$0.mDreaming) {
                    this.this$0.finishForCarMode();
                }
            }
        }
    };
    private final DozeHost.Callback mHostCallback = new DozeHost.Callback(this) { // from class: com.android.systemui.doze.DozeService.2
        final DozeService this$0;

        {
            this.this$0 = this;
        }

        @Override // com.android.systemui.doze.DozeHost.Callback
        public void onBuzzBeepBlinked() {
            if (DozeService.DEBUG) {
                Log.d(this.this$0.mTag, "onBuzzBeepBlinked");
            }
            this.this$0.updateNotificationPulse(System.currentTimeMillis());
        }

        @Override // com.android.systemui.doze.DozeHost.Callback
        public void onNewNotifications() {
            if (DozeService.DEBUG) {
                Log.d(this.this$0.mTag, "onNewNotifications (noop)");
            }
        }

        @Override // com.android.systemui.doze.DozeHost.Callback
        public void onNotificationLight(boolean z) {
            if (DozeService.DEBUG) {
                Log.d(this.this$0.mTag, "onNotificationLight on=" + z);
            }
            if (this.this$0.mNotificationLightOn == z) {
                return;
            }
            this.this$0.mNotificationLightOn = z;
            if (this.this$0.mNotificationLightOn) {
                this.this$0.updateNotificationPulseDueToLight();
            }
        }

        @Override // com.android.systemui.doze.DozeHost.Callback
        public void onPowerSaveChanged(boolean z) {
            this.this$0.mPowerSaveActive = z;
            if (this.this$0.mPowerSaveActive && this.this$0.mDreaming) {
                this.this$0.finishToSavePower();
            }
        }
    };

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/doze/DozeService$ProximityCheck.class */
    public abstract class ProximityCheck implements SensorEventListener, Runnable {
        private boolean mFinished;
        private float mMaxRange;
        private boolean mRegistered;
        private final String mTag;
        final DozeService this$0;

        private ProximityCheck(DozeService dozeService) {
            this.this$0 = dozeService;
            this.mTag = this.this$0.mTag + ".ProximityCheck";
        }

        /* synthetic */ ProximityCheck(DozeService dozeService, ProximityCheck proximityCheck) {
            this(dozeService);
        }

        private void finishWithResult(int i) {
            if (this.mFinished) {
                return;
            }
            if (this.mRegistered) {
                this.this$0.mHandler.removeCallbacks(this);
                this.this$0.mSensors.unregisterListener(this);
                this.this$0.mPickupSensor.setDisabled(false);
                this.mRegistered = false;
            }
            onProximityResult(i);
            this.mFinished = true;
        }

        public void check() {
            if (this.mFinished || this.mRegistered) {
                return;
            }
            Sensor defaultSensor = this.this$0.mSensors.getDefaultSensor(8);
            if (defaultSensor == null) {
                if (DozeService.DEBUG) {
                    Log.d(this.mTag, "No sensor found");
                }
                finishWithResult(0);
                return;
            }
            this.this$0.mPickupSensor.setDisabled(true);
            this.mMaxRange = defaultSensor.getMaximumRange();
            this.this$0.mSensors.registerListener(this, defaultSensor, 3, 0, this.this$0.mHandler);
            this.this$0.mHandler.postDelayed(this, 500L);
            this.mRegistered = true;
        }

        @Override // android.hardware.SensorEventListener
        public void onAccuracyChanged(Sensor sensor, int i) {
        }

        public abstract void onProximityResult(int i);

        @Override // android.hardware.SensorEventListener
        public void onSensorChanged(SensorEvent sensorEvent) {
            boolean z = false;
            if (sensorEvent.values.length == 0) {
                if (DozeService.DEBUG) {
                    Log.d(this.mTag, "Event has no values!");
                }
                finishWithResult(0);
                return;
            }
            if (DozeService.DEBUG) {
                Log.d(this.mTag, "Event: value=" + sensorEvent.values[0] + " max=" + this.mMaxRange);
            }
            if (sensorEvent.values[0] < this.mMaxRange) {
                z = true;
            }
            finishWithResult(z ? 1 : 2);
        }

        @Override // java.lang.Runnable
        public void run() {
            if (DozeService.DEBUG) {
                Log.d(this.mTag, "No event received before timeout");
            }
            finishWithResult(0);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/doze/DozeService$TriggerSensor.class */
    public class TriggerSensor extends TriggerEventListener {
        private final boolean mConfigured;
        private final boolean mDebugVibrate;
        private boolean mDisabled;
        private final int mPulseReason;
        private boolean mRegistered;
        private boolean mRequested;
        private final Sensor mSensor;
        final DozeService this$0;

        public TriggerSensor(DozeService dozeService, int i, boolean z, boolean z2, int i2) {
            this.this$0 = dozeService;
            this.mSensor = dozeService.mSensors.getDefaultSensor(i);
            this.mConfigured = z;
            this.mDebugVibrate = z2;
            this.mPulseReason = i2;
        }

        private void updateListener() {
            if (!this.mConfigured || this.mSensor == null) {
                return;
            }
            if (this.mRequested && !this.mDisabled && !this.mRegistered) {
                this.mRegistered = this.this$0.mSensors.requestTriggerSensor(this, this.mSensor);
                if (DozeService.DEBUG) {
                    Log.d(this.this$0.mTag, "requestTriggerSensor " + this.mRegistered);
                }
            } else if (this.mRegistered) {
                boolean cancelTriggerSensor = this.this$0.mSensors.cancelTriggerSensor(this, this.mSensor);
                if (DozeService.DEBUG) {
                    Log.d(this.this$0.mTag, "cancelTriggerSensor " + cancelTriggerSensor);
                }
                this.mRegistered = false;
            }
        }

        @Override // android.hardware.TriggerEventListener
        public void onTrigger(TriggerEvent triggerEvent) {
            Vibrator vibrator;
            boolean z = false;
            this.this$0.mWakeLock.acquire();
            try {
                if (DozeService.DEBUG) {
                    Log.d(this.this$0.mTag, "onTrigger: " + DozeService.triggerEventToString(triggerEvent));
                }
                if (this.mDebugVibrate && (vibrator = (Vibrator) this.this$0.mContext.getSystemService("vibrator")) != null) {
                    vibrator.vibrate(1000L, new AudioAttributes.Builder().setContentType(4).setUsage(13).build());
                }
                this.mRegistered = false;
                this.this$0.requestPulse(this.mPulseReason);
                updateListener();
                if (System.currentTimeMillis() - this.this$0.mNotificationPulseTime < this.this$0.mDozeParameters.getPickupVibrationThreshold()) {
                    z = true;
                }
                if (!z) {
                    this.this$0.resetNotificationResets();
                } else if (DozeService.DEBUG) {
                    Log.d(this.this$0.mTag, "Not resetting schedule, recent notification");
                }
                if (this.mSensor.getType() == 25) {
                    DozeLog.tracePickupPulse(z);
                }
            } finally {
                this.this$0.mWakeLock.release();
            }
        }

        public void setDisabled(boolean z) {
            if (this.mDisabled == z) {
                return;
            }
            this.mDisabled = z;
            updateListener();
        }

        public void setListening(boolean z) {
            if (this.mRequested == z) {
                return;
            }
            this.mRequested = z;
            updateListener();
        }

        public String toString() {
            return "{mRegistered=" + this.mRegistered + ", mRequested=" + this.mRequested + ", mDisabled=" + this.mDisabled + ", mConfigured=" + this.mConfigured + ", mDebugVibrate=" + this.mDebugVibrate + ", mSensor=" + this.mSensor + "}";
        }
    }

    public DozeService() {
        if (DEBUG) {
            Log.d(this.mTag, "new DozeService()");
        }
        setDebug(DEBUG);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void continuePulsing(int i) {
        if (!this.mHost.isPulsingBlocked()) {
            this.mHost.pulseWhileDozing(new DozeHost.PulseCallback(this) { // from class: com.android.systemui.doze.DozeService.5
                final DozeService this$0;

                {
                    this.this$0 = this;
                }

                @Override // com.android.systemui.doze.DozeHost.PulseCallback
                public void onPulseFinished() {
                    if (this.this$0.mPulsing && this.this$0.mDreaming) {
                        this.this$0.mPulsing = false;
                        this.this$0.turnDisplayOff();
                    }
                    this.this$0.mWakeLock.release();
                }

                @Override // com.android.systemui.doze.DozeHost.PulseCallback
                public void onPulseStarted() {
                    if (this.this$0.mPulsing && this.this$0.mDreaming) {
                        this.this$0.turnDisplayOn();
                    }
                }
            }, i);
            return;
        }
        this.mPulsing = false;
        this.mWakeLock.release();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void finishForCarMode() {
        Log.w(this.mTag, "Exiting ambient mode, not allowed in car mode");
        finish();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void finishToSavePower() {
        Log.w(this.mTag, "Exiting ambient mode due to low power battery saver");
        finish();
    }

    private void listenForBroadcasts(boolean z) {
        if (!z) {
            if (this.mBroadcastReceiverRegistered) {
                this.mContext.unregisterReceiver(this.mBroadcastReceiver);
            }
            this.mBroadcastReceiverRegistered = false;
            return;
        }
        IntentFilter intentFilter = new IntentFilter("com.android.systemui.doze.pulse");
        intentFilter.addAction("com.android.systemui.doze.notification_pulse");
        intentFilter.addAction(UiModeManager.ACTION_ENTER_CAR_MODE);
        this.mContext.registerReceiver(this.mBroadcastReceiver, intentFilter);
        this.mBroadcastReceiverRegistered = true;
    }

    private void listenForNotifications(boolean z) {
        if (!z) {
            this.mHost.removeCallback(this.mHostCallback);
            return;
        }
        resetNotificationResets();
        this.mHost.addCallback(this.mHostCallback);
        this.mNotificationLightOn = this.mHost.isNotificationLightOn();
        if (this.mNotificationLightOn) {
            updateNotificationPulseDueToLight();
        }
    }

    private void listenForPulseSignals(boolean z) {
        if (DEBUG) {
            Log.d(this.mTag, "listenForPulseSignals: " + z);
        }
        this.mSigMotionSensor.setListening(z);
        this.mPickupSensor.setListening(z);
        listenForBroadcasts(z);
        listenForNotifications(z);
    }

    private PendingIntent notificationPulseIntent(long j) {
        return PendingIntent.getBroadcast(this.mContext, 0, new Intent("com.android.systemui.doze.notification_pulse").setPackage(getPackageName()).putExtra("instance", j).setFlags(268435456), 134217728);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void requestPulse(int i) {
        if (this.mHost == null || !this.mDreaming || this.mPulsing) {
            return;
        }
        this.mWakeLock.acquire();
        this.mPulsing = true;
        if (!this.mDozeParameters.getProxCheckBeforePulse()) {
            continuePulsing(i);
            return;
        }
        long uptimeMillis = SystemClock.uptimeMillis();
        boolean pickupPerformsProxCheck = i == 3 ? this.mDozeParameters.getPickupPerformsProxCheck() : false;
        if (pickupPerformsProxCheck) {
            continuePulsing(i);
        }
        new ProximityCheck(this, this, uptimeMillis, i, pickupPerformsProxCheck) { // from class: com.android.systemui.doze.DozeService.4
            final DozeService this$0;
            final boolean val$nonBlocking;
            final int val$reason;
            final long val$start;

            /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
            {
                super(this, null);
                this.this$0 = this;
                this.val$start = uptimeMillis;
                this.val$reason = i;
                this.val$nonBlocking = pickupPerformsProxCheck;
            }

            @Override // com.android.systemui.doze.DozeService.ProximityCheck
            public void onProximityResult(int i2) {
                boolean z = i2 == 1;
                DozeLog.traceProximityResult(this.this$0.mContext, z, SystemClock.uptimeMillis() - this.val$start, this.val$reason);
                if (this.val$nonBlocking) {
                    return;
                }
                if (!z) {
                    this.this$0.continuePulsing(this.val$reason);
                    return;
                }
                this.this$0.mPulsing = false;
                this.this$0.mWakeLock.release();
            }
        }.check();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void rescheduleNotificationPulse(boolean z) {
        if (DEBUG) {
            Log.d(this.mTag, "rescheduleNotificationPulse predicate=" + z);
        }
        this.mAlarmManager.cancel(notificationPulseIntent(0L));
        if (!z) {
            if (DEBUG) {
                Log.d(this.mTag, "  don't reschedule: predicate is false");
                return;
            }
            return;
        }
        DozeParameters.PulseSchedule pulseSchedule = this.mDozeParameters.getPulseSchedule();
        if (pulseSchedule == null) {
            if (DEBUG) {
                Log.d(this.mTag, "  don't reschedule: schedule is null");
                return;
            }
            return;
        }
        long currentTimeMillis = System.currentTimeMillis();
        long nextTime = pulseSchedule.getNextTime(currentTimeMillis, this.mNotificationPulseTime);
        if (nextTime <= 0) {
            if (DEBUG) {
                Log.d(this.mTag, "  don't reschedule: time is " + nextTime);
                return;
            }
            return;
        }
        long j = nextTime - currentTimeMillis;
        if (j <= 0) {
            if (DEBUG) {
                Log.d(this.mTag, "  don't reschedule: delta is " + j);
                return;
            }
            return;
        }
        long j2 = nextTime - this.mNotificationPulseTime;
        if (DEBUG) {
            Log.d(this.mTag, "Scheduling pulse " + j2 + " in " + j + "ms for " + new Date(nextTime));
        }
        this.mAlarmManager.setExact(0, nextTime, notificationPulseIntent(j2));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void resetNotificationResets() {
        if (DEBUG) {
            Log.d(this.mTag, "resetNotificationResets");
        }
        this.mScheduleResetsRemaining = this.mDozeParameters.getPulseScheduleResets();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static String triggerEventToString(TriggerEvent triggerEvent) {
        if (triggerEvent == null) {
            return null;
        }
        StringBuilder append = new StringBuilder("TriggerEvent[").append(triggerEvent.timestamp).append(',').append(triggerEvent.sensor.getName());
        if (triggerEvent.values != null) {
            for (int i = 0; i < triggerEvent.values.length; i++) {
                append.append(',').append(triggerEvent.values[i]);
            }
        }
        return append.append(']').toString();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void turnDisplayOff() {
        if (DEBUG) {
            Log.d(this.mTag, "Display off");
        }
        setDozeScreenState(1);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void turnDisplayOn() {
        if (DEBUG) {
            Log.d(this.mTag, "Display on");
        }
        setDozeScreenState(this.mDisplayStateSupported ? 3 : 2);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateNotificationPulse(long j) {
        if (DEBUG) {
            Log.d(this.mTag, "updateNotificationPulse notificationTimeMs=" + j);
        }
        if (this.mDozeParameters.getPulseOnNotifications()) {
            if (this.mScheduleResetsRemaining <= 0) {
                if (DEBUG) {
                    Log.d(this.mTag, "No more schedule resets remaining");
                    return;
                }
                return;
            }
            long pulseDuration = this.mDozeParameters.getPulseDuration(false);
            boolean z = System.currentTimeMillis() >= j;
            if (j - this.mLastScheduleResetTime >= pulseDuration) {
                this.mScheduleResetsRemaining--;
                this.mLastScheduleResetTime = j;
            } else if (!z) {
                if (DEBUG) {
                    Log.d(this.mTag, "Recently updated, not resetting schedule");
                    return;
                }
                return;
            }
            if (DEBUG) {
                Log.d(this.mTag, "mScheduleResetsRemaining = " + this.mScheduleResetsRemaining);
            }
            this.mNotificationPulseTime = j;
            if (z) {
                DozeLog.traceNotificationPulse(0L);
                requestPulse(1);
            }
            rescheduleNotificationPulse(true);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateNotificationPulseDueToLight() {
        updateNotificationPulse(Math.max(System.currentTimeMillis(), this.mEarliestPulseDueToLight));
    }

    protected void dumpOnHandler(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        super.dumpOnHandler(fileDescriptor, printWriter, strArr);
        printWriter.print("  mDreaming: ");
        printWriter.println(this.mDreaming);
        printWriter.print("  mPulsing: ");
        printWriter.println(this.mPulsing);
        printWriter.print("  mWakeLock: held=");
        printWriter.println(this.mWakeLock.isHeld());
        printWriter.print("  mHost: ");
        printWriter.println(this.mHost);
        printWriter.print("  mBroadcastReceiverRegistered: ");
        printWriter.println(this.mBroadcastReceiverRegistered);
        printWriter.print("  mSigMotionSensor: ");
        printWriter.println(this.mSigMotionSensor);
        printWriter.print("  mPickupSensor:");
        printWriter.println(this.mPickupSensor);
        printWriter.print("  mDisplayStateSupported: ");
        printWriter.println(this.mDisplayStateSupported);
        printWriter.print("  mNotificationLightOn: ");
        printWriter.println(this.mNotificationLightOn);
        printWriter.print("  mPowerSaveActive: ");
        printWriter.println(this.mPowerSaveActive);
        printWriter.print("  mCarMode: ");
        printWriter.println(this.mCarMode);
        printWriter.print("  mNotificationPulseTime: ");
        printWriter.println(this.mNotificationPulseTime);
        printWriter.print("  mScheduleResetsRemaining: ");
        printWriter.println(this.mScheduleResetsRemaining);
        this.mDozeParameters.dump(printWriter);
    }

    @Override // android.service.dreams.DreamService, android.view.Window.Callback
    public void onAttachedToWindow() {
        if (DEBUG) {
            Log.d(this.mTag, "onAttachedToWindow");
        }
        super.onAttachedToWindow();
    }

    @Override // android.service.dreams.DreamService, android.app.Service
    public void onCreate() {
        if (DEBUG) {
            Log.d(this.mTag, "onCreate");
        }
        super.onCreate();
        if (getApplication() instanceof SystemUIApplication) {
            this.mHost = (DozeHost) ((SystemUIApplication) getApplication()).getComponent(DozeHost.class);
        }
        if (this.mHost == null) {
            Log.w("DozeService", "No doze service host found.");
        }
        setWindowless(true);
        this.mSensors = (SensorManager) this.mContext.getSystemService("sensor");
        this.mSigMotionSensor = new TriggerSensor(this, 17, this.mDozeParameters.getPulseOnSigMotion(), this.mDozeParameters.getVibrateOnSigMotion(), 2);
        this.mPickupSensor = new TriggerSensor(this, 25, this.mDozeParameters.getPulseOnPickup(), this.mDozeParameters.getVibrateOnPickup(), 3);
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        this.mWakeLock = this.mPowerManager.newWakeLock(1, "DozeService");
        this.mWakeLock.setReferenceCounted(true);
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        this.mDisplayStateSupported = this.mDozeParameters.getDisplayStateSupported();
        this.mUiModeManager = (UiModeManager) this.mContext.getSystemService("uimode");
        turnDisplayOff();
    }

    @Override // android.service.dreams.DreamService
    public void onDreamingStarted() {
        super.onDreamingStarted();
        if (this.mHost == null) {
            finish();
            return;
        }
        this.mPowerSaveActive = this.mHost.isPowerSaveActive();
        this.mCarMode = this.mUiModeManager.getCurrentModeType() == 3;
        if (DEBUG) {
            Log.d(this.mTag, "onDreamingStarted canDoze=" + canDoze() + " mPowerSaveActive=" + this.mPowerSaveActive + " mCarMode=" + this.mCarMode);
        }
        if (this.mPowerSaveActive) {
            finishToSavePower();
        } else if (this.mCarMode) {
            finishForCarMode();
        } else {
            this.mDreaming = true;
            rescheduleNotificationPulse(false);
            this.mEarliestPulseDueToLight = System.currentTimeMillis() + 10000;
            listenForPulseSignals(true);
            this.mHost.startDozing(new Runnable(this) { // from class: com.android.systemui.doze.DozeService.3
                final DozeService this$0;

                {
                    this.this$0 = this;
                }

                @Override // java.lang.Runnable
                public void run() {
                    if (this.this$0.mDreaming) {
                        this.this$0.startDozing();
                    }
                }
            });
        }
    }

    @Override // android.service.dreams.DreamService
    public void onDreamingStopped() {
        if (DEBUG) {
            Log.d(this.mTag, "onDreamingStopped isDozing=" + isDozing());
        }
        super.onDreamingStopped();
        if (this.mHost == null) {
            return;
        }
        this.mDreaming = false;
        listenForPulseSignals(false);
        this.mHost.stopDozing();
    }
}
