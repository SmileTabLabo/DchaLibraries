package com.android.quicksearchbox;

import android.os.SystemClock;
/* loaded from: a.zip:com/android/quicksearchbox/LatencyTracker.class */
public class LatencyTracker {
    private long mStartTime = SystemClock.uptimeMillis();

    public int getLatency() {
        return (int) (SystemClock.uptimeMillis() - this.mStartTime);
    }
}
