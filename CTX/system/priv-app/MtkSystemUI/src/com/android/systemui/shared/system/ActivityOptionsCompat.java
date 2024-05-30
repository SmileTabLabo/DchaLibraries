package com.android.systemui.shared.system;

import android.app.ActivityOptions;
/* loaded from: classes.dex */
public abstract class ActivityOptionsCompat {
    public static ActivityOptions makeSplitScreenOptions(boolean z) {
        int i;
        ActivityOptions makeBasic = ActivityOptions.makeBasic();
        makeBasic.setLaunchWindowingMode(3);
        if (z) {
            i = 0;
        } else {
            i = 1;
        }
        makeBasic.setSplitScreenCreateMode(i);
        return makeBasic;
    }
}
