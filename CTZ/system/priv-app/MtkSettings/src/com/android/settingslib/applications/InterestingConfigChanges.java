package com.android.settingslib.applications;

import android.content.res.Configuration;
import android.content.res.Resources;
/* loaded from: classes.dex */
public class InterestingConfigChanges {
    private final int mFlags;
    private final Configuration mLastConfiguration;
    private int mLastDensity;

    public InterestingConfigChanges() {
        this(-2147482876);
    }

    public InterestingConfigChanges(int i) {
        this.mLastConfiguration = new Configuration();
        this.mFlags = i;
    }

    public boolean applyNewConfig(Resources resources) {
        int updateFrom = this.mLastConfiguration.updateFrom(Configuration.generateDelta(this.mLastConfiguration, resources.getConfiguration()));
        if ((this.mLastDensity != resources.getDisplayMetrics().densityDpi) || (updateFrom & this.mFlags) != 0) {
            this.mLastDensity = resources.getDisplayMetrics().densityDpi;
            return true;
        }
        return false;
    }
}
