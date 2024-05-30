package com.android.systemui.statusbar.phone;

import android.content.Context;
import com.android.systemui.R;
/* loaded from: classes.dex */
public class VelocityTrackerFactory {
    public static VelocityTrackerInterface obtain(Context context) {
        char c;
        String string = context.getResources().getString(R.string.velocity_tracker_impl);
        int hashCode = string.hashCode();
        if (hashCode != 104998702) {
            if (hashCode == 1874684019 && string.equals("platform")) {
                c = 1;
            }
            c = 65535;
        } else {
            if (string.equals("noisy")) {
                c = 0;
            }
            c = 65535;
        }
        switch (c) {
            case 0:
                return NoisyVelocityTracker.obtain();
            case 1:
                return PlatformVelocityTracker.obtain();
            default:
                throw new IllegalStateException("Invalid tracker: " + string);
        }
    }
}
