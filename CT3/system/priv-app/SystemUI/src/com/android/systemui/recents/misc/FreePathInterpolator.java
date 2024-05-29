package com.android.systemui.recents.misc;

import android.graphics.Path;
import android.view.animation.BaseInterpolator;
/* loaded from: a.zip:com/android/systemui/recents/misc/FreePathInterpolator.class */
public class FreePathInterpolator extends BaseInterpolator {
    private float mArcLength;
    private float[] mX;
    private float[] mY;

    public FreePathInterpolator(Path path) {
        initPath(path);
    }

    private void initPath(Path path) {
        float[] approximate = path.approximate(0.002f);
        int length = approximate.length / 3;
        this.mX = new float[length];
        this.mY = new float[length];
        this.mArcLength = 0.0f;
        float f = 0.0f;
        float f2 = 0.0f;
        float f3 = 0.0f;
        int i = 0;
        int i2 = 0;
        while (i < length) {
            int i3 = i2 + 1;
            float f4 = approximate[i2];
            int i4 = i3 + 1;
            float f5 = approximate[i3];
            float f6 = approximate[i4];
            if (f4 == f3 && f5 != f) {
                throw new IllegalArgumentException("The Path cannot have discontinuity in the X axis.");
            }
            if (f5 < f) {
                throw new IllegalArgumentException("The Path cannot loop back on itself.");
            }
            this.mX[i] = f5;
            this.mY[i] = f6;
            this.mArcLength = (float) (this.mArcLength + Math.hypot(f5 - f, f6 - f2));
            f = f5;
            f2 = f6;
            f3 = f4;
            i++;
            i2 = i4 + 1;
        }
    }

    public float getArcLength() {
        return this.mArcLength;
    }

    @Override // android.animation.TimeInterpolator
    public float getInterpolation(float f) {
        int i = 0;
        int length = this.mX.length - 1;
        if (f <= 0.0f) {
            return this.mY[0];
        }
        int i2 = length;
        if (f >= 1.0f) {
            return this.mY[length];
        }
        while (i2 - i > 1) {
            int i3 = (i + i2) / 2;
            if (f < this.mX[i3]) {
                i2 = i3;
            } else {
                i = i3;
            }
        }
        float f2 = this.mX[i2] - this.mX[i];
        if (f2 == 0.0f) {
            return this.mY[i];
        }
        float f3 = (f - this.mX[i]) / f2;
        float f4 = this.mY[i];
        return ((this.mY[i2] - f4) * f3) + f4;
    }

    public float getX(float f) {
        int i = 0;
        int length = this.mY.length - 1;
        if (f <= 0.0f) {
            return this.mX[length];
        }
        if (f >= 1.0f) {
            return this.mX[0];
        }
        while (length - i > 1) {
            int i2 = (i + length) / 2;
            if (f < this.mY[i2]) {
                i = i2;
            } else {
                length = i2;
            }
        }
        float f2 = this.mY[length] - this.mY[i];
        if (f2 == 0.0f) {
            return this.mX[i];
        }
        float f3 = (f - this.mY[i]) / f2;
        float f4 = this.mX[i];
        return ((this.mX[length] - f4) * f3) + f4;
    }
}
