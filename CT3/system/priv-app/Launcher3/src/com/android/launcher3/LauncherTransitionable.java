package com.android.launcher3;
/* loaded from: a.zip:com/android/launcher3/LauncherTransitionable.class */
public interface LauncherTransitionable {
    void onLauncherTransitionEnd(Launcher launcher, boolean z, boolean z2);

    void onLauncherTransitionPrepare(Launcher launcher, boolean z, boolean z2);

    void onLauncherTransitionStart(Launcher launcher, boolean z, boolean z2);

    void onLauncherTransitionStep(Launcher launcher, float f);
}
