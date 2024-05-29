package com.android.systemui.statusbar.phone;

import android.util.Pools;
import android.view.MotionEvent;
import java.util.ArrayDeque;
import java.util.Iterator;
/* loaded from: a.zip:com/android/systemui/statusbar/phone/NoisyVelocityTracker.class */
public class NoisyVelocityTracker implements VelocityTrackerInterface {
    private static final Pools.SynchronizedPool<NoisyVelocityTracker> sNoisyPool = new Pools.SynchronizedPool<>(2);
    private float mVX;
    private final int MAX_EVENTS = 8;
    private ArrayDeque<MotionEventCopy> mEventBuf = new ArrayDeque<>(8);
    private float mVY = 0.0f;

    /* loaded from: a.zip:com/android/systemui/statusbar/phone/NoisyVelocityTracker$MotionEventCopy.class */
    private static class MotionEventCopy {
        long t;
        float x;
        float y;

        public MotionEventCopy(float f, float f2, long j) {
            this.x = f;
            this.y = f2;
            this.t = j;
        }
    }

    private NoisyVelocityTracker() {
    }

    public static NoisyVelocityTracker obtain() {
        NoisyVelocityTracker noisyVelocityTracker = (NoisyVelocityTracker) sNoisyPool.acquire();
        if (noisyVelocityTracker == null) {
            noisyVelocityTracker = new NoisyVelocityTracker();
        }
        return noisyVelocityTracker;
    }

    @Override // com.android.systemui.statusbar.phone.VelocityTrackerInterface
    public void addMovement(MotionEvent motionEvent) {
        if (this.mEventBuf.size() == 8) {
            this.mEventBuf.remove();
        }
        this.mEventBuf.add(new MotionEventCopy(motionEvent.getX(), motionEvent.getY(), motionEvent.getEventTime()));
    }

    @Override // com.android.systemui.statusbar.phone.VelocityTrackerInterface
    public void computeCurrentVelocity(int i) {
        this.mVY = 0.0f;
        this.mVX = 0.0f;
        MotionEventCopy motionEventCopy = null;
        int i2 = 0;
        float f = 0.0f;
        float f2 = 10.0f;
        Iterator<MotionEventCopy> it = this.mEventBuf.iterator();
        while (it.hasNext()) {
            MotionEventCopy next = it.next();
            float f3 = f;
            float f4 = f2;
            if (motionEventCopy != null) {
                float f5 = ((float) (next.t - motionEventCopy.t)) / i;
                float f6 = next.x;
                float f7 = motionEventCopy.x;
                float f8 = next.y;
                float f9 = motionEventCopy.y;
                if (next.t != motionEventCopy.t) {
                    this.mVX += (f2 * (f6 - f7)) / f5;
                    this.mVY += (f2 * (f8 - f9)) / f5;
                    f3 = f + f2;
                    f4 = f2 * 0.75f;
                }
            }
            motionEventCopy = next;
            i2++;
            f = f3;
            f2 = f4;
        }
        if (f > 0.0f) {
            this.mVX /= f;
            this.mVY /= f;
            return;
        }
        this.mVY = 0.0f;
        this.mVX = 0.0f;
    }

    @Override // com.android.systemui.statusbar.phone.VelocityTrackerInterface
    public float getXVelocity() {
        if (Float.isNaN(this.mVX) || Float.isInfinite(this.mVX)) {
            this.mVX = 0.0f;
        }
        return this.mVX;
    }

    @Override // com.android.systemui.statusbar.phone.VelocityTrackerInterface
    public float getYVelocity() {
        if (Float.isNaN(this.mVY) || Float.isInfinite(this.mVX)) {
            this.mVY = 0.0f;
        }
        return this.mVY;
    }

    @Override // com.android.systemui.statusbar.phone.VelocityTrackerInterface
    public void recycle() {
        this.mEventBuf.clear();
        sNoisyPool.release(this);
    }
}
