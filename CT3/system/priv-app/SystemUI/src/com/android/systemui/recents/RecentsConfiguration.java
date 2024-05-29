package com.android.systemui.recents;

import android.content.Context;
import android.content.res.Resources;
import com.android.systemui.recents.misc.SystemServicesProxy;
/* loaded from: a.zip:com/android/systemui/recents/RecentsConfiguration.class */
public class RecentsConfiguration {
    public boolean fakeShadows;
    public final boolean isLargeScreen;
    public final boolean isXLargeScreen;
    public RecentsActivityLaunchState mLaunchState = new RecentsActivityLaunchState();
    public final int smallestWidth;
    public int svelteLevel;

    public RecentsConfiguration(Context context) {
        SystemServicesProxy systemServices = Recents.getSystemServices();
        Resources resources = context.getApplicationContext().getResources();
        this.fakeShadows = resources.getBoolean(2131623943);
        this.svelteLevel = resources.getInteger(2131755070);
        float f = context.getResources().getDisplayMetrics().density;
        this.smallestWidth = systemServices.getDeviceSmallestWidth();
        this.isLargeScreen = this.smallestWidth >= ((int) (600.0f * f));
        this.isXLargeScreen = this.smallestWidth >= ((int) (720.0f * f));
    }

    public RecentsActivityLaunchState getLaunchState() {
        return this.mLaunchState;
    }
}
