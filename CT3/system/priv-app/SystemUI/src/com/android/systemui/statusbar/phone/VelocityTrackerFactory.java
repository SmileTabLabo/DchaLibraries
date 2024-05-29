package com.android.systemui.statusbar.phone;

import android.content.Context;
/* loaded from: a.zip:com/android/systemui/statusbar/phone/VelocityTrackerFactory.class */
public class VelocityTrackerFactory {
    public static VelocityTrackerInterface obtain(Context context) {
        String string = context.getResources().getString(2131493278);
        if (string.equals("noisy")) {
            return NoisyVelocityTracker.obtain();
        }
        if (string.equals("platform")) {
            return PlatformVelocityTracker.obtain();
        }
        throw new IllegalStateException("Invalid tracker: " + string);
    }
}
