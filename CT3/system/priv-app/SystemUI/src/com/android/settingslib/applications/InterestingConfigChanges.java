package com.android.settingslib.applications;

import android.content.res.Configuration;
import android.content.res.Resources;
/* loaded from: a.zip:com/android/settingslib/applications/InterestingConfigChanges.class */
public class InterestingConfigChanges {
    private final Configuration mLastConfiguration = new Configuration();
    private int mLastDensity;

    public boolean applyNewConfig(Resources resources) {
        int updateFrom = this.mLastConfiguration.updateFrom(resources.getConfiguration());
        if ((this.mLastDensity != resources.getDisplayMetrics().densityDpi) || (updateFrom & 772) != 0) {
            this.mLastDensity = resources.getDisplayMetrics().densityDpi;
            return true;
        }
        return false;
    }
}
