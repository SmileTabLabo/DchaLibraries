package com.android.systemui.doze;

import android.app.AlarmManager;
import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.text.format.Formatter;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.systemui.doze.DozeHost;
import com.android.systemui.doze.DozeMachine;
import com.android.systemui.statusbar.phone.DozeParameters;
import com.android.systemui.util.AlarmTimeout;
import com.android.systemui.util.wakelock.WakeLock;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Objects;
/* loaded from: classes.dex */
public class DozeUi implements DozeMachine.Part {
    private final boolean mCanAnimateTransition;
    private final Context mContext;
    private final DozeParameters mDozeParameters;
    private final Handler mHandler;
    private final DozeHost mHost;
    private boolean mKeyguardShowing;
    private final KeyguardUpdateMonitorCallback mKeyguardVisibilityCallback = new KeyguardUpdateMonitorCallback() { // from class: com.android.systemui.doze.DozeUi.1
        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onKeyguardVisibilityChanged(boolean z) {
            DozeUi.this.mKeyguardShowing = z;
            DozeUi.this.updateAnimateScreenOff();
        }
    };
    private long mLastTimeTickElapsed = 0;
    private final DozeMachine mMachine;
    private final AlarmTimeout mTimeTicker;
    private final WakeLock mWakeLock;

    public DozeUi(Context context, AlarmManager alarmManager, DozeMachine dozeMachine, WakeLock wakeLock, DozeHost dozeHost, Handler handler, DozeParameters dozeParameters, KeyguardUpdateMonitor keyguardUpdateMonitor) {
        this.mContext = context;
        this.mMachine = dozeMachine;
        this.mWakeLock = wakeLock;
        this.mHost = dozeHost;
        this.mHandler = handler;
        this.mCanAnimateTransition = !dozeParameters.getDisplayNeedsBlanking();
        this.mDozeParameters = dozeParameters;
        this.mTimeTicker = new AlarmTimeout(alarmManager, new AlarmManager.OnAlarmListener() { // from class: com.android.systemui.doze.-$$Lambda$DozeUi$FO90hbI6xqXYUh2DtwuwM-uzJzs
            @Override // android.app.AlarmManager.OnAlarmListener
            public final void onAlarm() {
                DozeUi.this.onTimeTick();
            }
        }, "doze_time_tick", handler);
        keyguardUpdateMonitor.registerCallback(this.mKeyguardVisibilityCallback);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateAnimateScreenOff() {
        if (this.mCanAnimateTransition) {
            boolean z = this.mDozeParameters.getAlwaysOn() && this.mKeyguardShowing;
            this.mDozeParameters.setControlScreenOffAnimation(z);
            this.mHost.setAnimateScreenOff(z);
        }
    }

    private void pulseWhileDozing(int i) {
        this.mHost.pulseWhileDozing(new DozeHost.PulseCallback() { // from class: com.android.systemui.doze.DozeUi.2
            @Override // com.android.systemui.doze.DozeHost.PulseCallback
            public void onPulseStarted() {
                DozeUi.this.mMachine.requestState(DozeMachine.State.DOZE_PULSING);
            }

            @Override // com.android.systemui.doze.DozeHost.PulseCallback
            public void onPulseFinished() {
                DozeUi.this.mMachine.requestState(DozeMachine.State.DOZE_PULSE_DONE);
            }
        }, i);
    }

    @Override // com.android.systemui.doze.DozeMachine.Part
    public void transitionTo(DozeMachine.State state, DozeMachine.State state2) {
        switch (state2) {
            case DOZE_AOD:
                if (state == DozeMachine.State.DOZE_AOD_PAUSED) {
                    this.mHost.dozeTimeTick();
                    Handler handler = this.mHandler;
                    WakeLock wakeLock = this.mWakeLock;
                    final DozeHost dozeHost = this.mHost;
                    Objects.requireNonNull(dozeHost);
                    handler.postDelayed(wakeLock.wrap(new Runnable() { // from class: com.android.systemui.doze.-$$Lambda$TvDuFxrq6WnRSNRP7k8oBY4uOBc
                        @Override // java.lang.Runnable
                        public final void run() {
                            DozeHost.this.dozeTimeTick();
                        }
                    }), 100L);
                    Handler handler2 = this.mHandler;
                    WakeLock wakeLock2 = this.mWakeLock;
                    final DozeHost dozeHost2 = this.mHost;
                    Objects.requireNonNull(dozeHost2);
                    handler2.postDelayed(wakeLock2.wrap(new Runnable() { // from class: com.android.systemui.doze.-$$Lambda$TvDuFxrq6WnRSNRP7k8oBY4uOBc
                        @Override // java.lang.Runnable
                        public final void run() {
                            DozeHost.this.dozeTimeTick();
                        }
                    }), 1000L);
                }
                scheduleTimeTick();
                break;
            case DOZE_AOD_PAUSING:
                scheduleTimeTick();
                break;
            case DOZE:
            case DOZE_AOD_PAUSED:
                unscheduleTimeTick();
                break;
            case DOZE_REQUEST_PULSE:
                pulseWhileDozing(this.mMachine.getPulseReason());
                break;
            case INITIALIZED:
                this.mHost.startDozing();
                break;
            case FINISH:
                this.mHost.stopDozing();
                unscheduleTimeTick();
                break;
        }
        updateAnimateWakeup(state2);
    }

    private void updateAnimateWakeup(DozeMachine.State state) {
        int i = AnonymousClass3.$SwitchMap$com$android$systemui$doze$DozeMachine$State[state.ordinal()];
        boolean z = true;
        if (i != 5) {
            switch (i) {
                case 7:
                    return;
                case 8:
                case 9:
                    break;
                default:
                    this.mHost.setAnimateWakeup((this.mCanAnimateTransition && this.mDozeParameters.getAlwaysOn()) ? false : false);
                    return;
            }
        }
        this.mHost.setAnimateWakeup(true);
    }

    private void scheduleTimeTick() {
        if (this.mTimeTicker.isScheduled()) {
            return;
        }
        this.mTimeTicker.schedule(roundToNextMinute(System.currentTimeMillis()) - System.currentTimeMillis(), 1);
        this.mLastTimeTickElapsed = SystemClock.elapsedRealtime();
    }

    private void unscheduleTimeTick() {
        if (!this.mTimeTicker.isScheduled()) {
            return;
        }
        verifyLastTimeTick();
        this.mTimeTicker.cancel();
    }

    private void verifyLastTimeTick() {
        long elapsedRealtime = SystemClock.elapsedRealtime() - this.mLastTimeTickElapsed;
        if (elapsedRealtime > 90000) {
            String formatShortElapsedTime = Formatter.formatShortElapsedTime(this.mContext, elapsedRealtime);
            DozeLog.traceMissedTick(formatShortElapsedTime);
            Log.e("DozeMachine", "Missed AOD time tick by " + formatShortElapsedTime);
        }
    }

    private long roundToNextMinute(long j) {
        Calendar gregorianCalendar = GregorianCalendar.getInstance();
        gregorianCalendar.setTimeInMillis(j);
        gregorianCalendar.set(14, 0);
        gregorianCalendar.set(13, 0);
        gregorianCalendar.add(12, 1);
        return gregorianCalendar.getTimeInMillis();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onTimeTick() {
        verifyLastTimeTick();
        this.mHost.dozeTimeTick();
        this.mHandler.post(this.mWakeLock.wrap(new Runnable() { // from class: com.android.systemui.doze.-$$Lambda$DozeUi$lHTcknku1GKi6pFF17CHlz1K3H8
            @Override // java.lang.Runnable
            public final void run() {
                DozeUi.lambda$onTimeTick$0();
            }
        }));
        scheduleTimeTick();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ void lambda$onTimeTick$0() {
    }

    @VisibleForTesting
    KeyguardUpdateMonitorCallback getKeyguardCallback() {
        return this.mKeyguardVisibilityCallback;
    }
}
