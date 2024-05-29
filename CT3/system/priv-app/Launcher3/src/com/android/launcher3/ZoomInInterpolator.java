package com.android.launcher3;

import android.animation.TimeInterpolator;
import android.view.animation.DecelerateInterpolator;
/* loaded from: a.zip:com/android/launcher3/ZoomInInterpolator.class */
class ZoomInInterpolator implements TimeInterpolator {
    private final InverseZInterpolator inverseZInterpolator = new InverseZInterpolator(0.35f);
    private final DecelerateInterpolator decelerate = new DecelerateInterpolator(3.0f);

    @Override // android.animation.TimeInterpolator
    public float getInterpolation(float f) {
        return this.decelerate.getInterpolation(this.inverseZInterpolator.getInterpolation(f));
    }
}
