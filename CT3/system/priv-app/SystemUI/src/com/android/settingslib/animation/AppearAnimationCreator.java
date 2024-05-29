package com.android.settingslib.animation;

import android.view.animation.Interpolator;
/* loaded from: a.zip:com/android/settingslib/animation/AppearAnimationCreator.class */
public interface AppearAnimationCreator<T> {
    void createAnimation(T t, long j, long j2, float f, boolean z, Interpolator interpolator, Runnable runnable);
}
