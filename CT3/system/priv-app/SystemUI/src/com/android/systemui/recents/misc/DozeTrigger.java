package com.android.systemui.recents.misc;

import android.os.Handler;
import android.view.ViewDebug;
/* loaded from: a.zip:com/android/systemui/recents/misc/DozeTrigger.class */
public class DozeTrigger {
    @ViewDebug.ExportedProperty(category = "recents")
    int mDozeDurationMilliseconds;
    Runnable mDozeRunnable = new Runnable(this) { // from class: com.android.systemui.recents.misc.DozeTrigger.1
        final DozeTrigger this$0;

        {
            this.this$0 = this;
        }

        @Override // java.lang.Runnable
        public void run() {
            this.this$0.mIsDozing = false;
            this.this$0.mIsAsleep = true;
            this.this$0.mOnSleepRunnable.run();
        }
    };
    Handler mHandler = new Handler();
    @ViewDebug.ExportedProperty(category = "recents")
    boolean mIsAsleep;
    @ViewDebug.ExportedProperty(category = "recents")
    boolean mIsDozing;
    Runnable mOnSleepRunnable;

    public DozeTrigger(int i, Runnable runnable) {
        this.mDozeDurationMilliseconds = i;
        this.mOnSleepRunnable = runnable;
    }

    void forcePoke() {
        this.mHandler.removeCallbacks(this.mDozeRunnable);
        this.mHandler.postDelayed(this.mDozeRunnable, this.mDozeDurationMilliseconds);
        this.mIsDozing = true;
    }

    public boolean isAsleep() {
        return this.mIsAsleep;
    }

    public boolean isDozing() {
        return this.mIsDozing;
    }

    public void poke() {
        if (this.mIsDozing) {
            forcePoke();
        }
    }

    public void setDozeDuration(int i) {
        this.mDozeDurationMilliseconds = i;
    }

    public void startDozing() {
        forcePoke();
        this.mIsAsleep = false;
    }

    public void stopDozing() {
        this.mHandler.removeCallbacks(this.mDozeRunnable);
        this.mIsDozing = false;
        this.mIsAsleep = false;
    }
}
