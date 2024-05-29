package com.android.systemui.recents.views;
/* loaded from: a.zip:com/android/systemui/recents/views/Range.class */
class Range {
    float max;
    float min;
    float origin;
    final float relativeMax;
    final float relativeMin;

    public Range(float f, float f2) {
        this.relativeMin = f;
        this.min = f;
        this.relativeMax = f2;
        this.max = f2;
    }

    public float getAbsoluteX(float f) {
        return f < 0.5f ? ((f - 0.5f) / 0.5f) * (-this.relativeMin) : ((f - 0.5f) / 0.5f) * this.relativeMax;
    }

    public float getNormalizedX(float f) {
        return f < this.origin ? (((f - this.origin) * 0.5f) / (-this.relativeMin)) + 0.5f : (((f - this.origin) * 0.5f) / this.relativeMax) + 0.5f;
    }

    public boolean isInRange(float f) {
        boolean z = false;
        if (f >= Math.floor(this.min)) {
            z = false;
            if (f <= Math.ceil(this.max)) {
                z = true;
            }
        }
        return z;
    }

    public void offset(float f) {
        this.origin = f;
        this.min = this.relativeMin + f;
        this.max = this.relativeMax + f;
    }
}
