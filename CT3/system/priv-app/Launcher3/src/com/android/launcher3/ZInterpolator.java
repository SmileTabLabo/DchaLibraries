package com.android.launcher3;

import android.animation.TimeInterpolator;
/* loaded from: a.zip:com/android/launcher3/ZInterpolator.class */
class ZInterpolator implements TimeInterpolator {
    private float focalLength;

    public ZInterpolator(float f) {
        this.focalLength = f;
    }

    @Override // android.animation.TimeInterpolator
    public float getInterpolation(float f) {
        return (1.0f - (this.focalLength / (this.focalLength + f))) / (1.0f - (this.focalLength / (this.focalLength + 1.0f)));
    }
}
