package com.android.launcher3;

import android.os.Handler;
/* loaded from: a.zip:com/android/launcher3/Alarm.class */
public class Alarm implements Runnable {
    private OnAlarmListener mAlarmListener;
    private long mAlarmTriggerTime;
    private boolean mWaitingForCallback;
    private boolean mAlarmPending = false;
    private Handler mHandler = new Handler();

    public boolean alarmPending() {
        return this.mAlarmPending;
    }

    public void cancelAlarm() {
        this.mAlarmTriggerTime = 0L;
        this.mAlarmPending = false;
    }

    @Override // java.lang.Runnable
    public void run() {
        this.mWaitingForCallback = false;
        if (this.mAlarmTriggerTime != 0) {
            long currentTimeMillis = System.currentTimeMillis();
            if (this.mAlarmTriggerTime > currentTimeMillis) {
                this.mHandler.postDelayed(this, Math.max(0L, this.mAlarmTriggerTime - currentTimeMillis));
                this.mWaitingForCallback = true;
                return;
            }
            this.mAlarmPending = false;
            if (this.mAlarmListener != null) {
                this.mAlarmListener.onAlarm(this);
            }
        }
    }

    public void setAlarm(long j) {
        long currentTimeMillis = System.currentTimeMillis();
        this.mAlarmPending = true;
        this.mAlarmTriggerTime = currentTimeMillis + j;
        if (this.mWaitingForCallback) {
            return;
        }
        this.mHandler.postDelayed(this, this.mAlarmTriggerTime - currentTimeMillis);
        this.mWaitingForCallback = true;
    }

    public void setOnAlarmListener(OnAlarmListener onAlarmListener) {
        this.mAlarmListener = onAlarmListener;
    }
}
