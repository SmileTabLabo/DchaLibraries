package com.android.settingslib.applications;

import android.content.res.Configuration;
import android.content.res.Resources;
/* loaded from: classes.dex */
public class InterestingConfigChanges {
    private final Configuration mLastConfiguration = new Configuration();
    private int mLastDensity;

    public boolean applyNewConfig(Resources res) {
        int configChanges = this.mLastConfiguration.updateFrom(res.getConfiguration());
        boolean densityChanged = this.mLastDensity != res.getDisplayMetrics().densityDpi;
        if (densityChanged || (configChanges & 772) != 0) {
            this.mLastDensity = res.getDisplayMetrics().densityDpi;
            return true;
        }
        return false;
    }
}
