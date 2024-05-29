package android.support.v4.view;

import android.os.Build;
import android.view.VelocityTracker;
/* loaded from: b.zip:android/support/v4/view/VelocityTrackerCompat.class */
public final class VelocityTrackerCompat {
    static final VelocityTrackerVersionImpl IMPL;

    /* loaded from: b.zip:android/support/v4/view/VelocityTrackerCompat$BaseVelocityTrackerVersionImpl.class */
    static class BaseVelocityTrackerVersionImpl implements VelocityTrackerVersionImpl {
        BaseVelocityTrackerVersionImpl() {
        }

        @Override // android.support.v4.view.VelocityTrackerCompat.VelocityTrackerVersionImpl
        public float getXVelocity(VelocityTracker velocityTracker, int i) {
            return velocityTracker.getXVelocity();
        }
    }

    /* loaded from: b.zip:android/support/v4/view/VelocityTrackerCompat$HoneycombVelocityTrackerVersionImpl.class */
    static class HoneycombVelocityTrackerVersionImpl implements VelocityTrackerVersionImpl {
        HoneycombVelocityTrackerVersionImpl() {
        }

        @Override // android.support.v4.view.VelocityTrackerCompat.VelocityTrackerVersionImpl
        public float getXVelocity(VelocityTracker velocityTracker, int i) {
            return VelocityTrackerCompatHoneycomb.getXVelocity(velocityTracker, i);
        }
    }

    /* loaded from: b.zip:android/support/v4/view/VelocityTrackerCompat$VelocityTrackerVersionImpl.class */
    interface VelocityTrackerVersionImpl {
        float getXVelocity(VelocityTracker velocityTracker, int i);
    }

    static {
        if (Build.VERSION.SDK_INT >= 11) {
            IMPL = new HoneycombVelocityTrackerVersionImpl();
        } else {
            IMPL = new BaseVelocityTrackerVersionImpl();
        }
    }

    private VelocityTrackerCompat() {
    }

    public static float getXVelocity(VelocityTracker velocityTracker, int i) {
        return IMPL.getXVelocity(velocityTracker, i);
    }
}
