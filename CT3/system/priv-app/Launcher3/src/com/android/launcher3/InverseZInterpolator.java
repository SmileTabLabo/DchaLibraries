package com.android.launcher3;

import android.animation.TimeInterpolator;
/* loaded from: a.zip:com/android/launcher3/InverseZInterpolator.class */
class InverseZInterpolator implements TimeInterpolator {
    private ZInterpolator zInterpolator;

    public InverseZInterpolator(float f) {
        this.zInterpolator = new ZInterpolator(f);
    }

    @Override // android.animation.TimeInterpolator
    public float getInterpolation(float f) {
        return 1.0f - this.zInterpolator.getInterpolation(1.0f - f);
    }
}
