package com.android.systemui.qs;

import android.graphics.Path;
import android.view.animation.BaseInterpolator;
import android.view.animation.Interpolator;
/* loaded from: a.zip:com/android/systemui/qs/PathInterpolatorBuilder.class */
public class PathInterpolatorBuilder {
    private float[] mDist;
    private float[] mX;
    private float[] mY;

    /* loaded from: a.zip:com/android/systemui/qs/PathInterpolatorBuilder$PathInterpolator.class */
    private static class PathInterpolator extends BaseInterpolator {
        private final float[] mX;
        private final float[] mY;

        private PathInterpolator(float[] fArr, float[] fArr2) {
            this.mX = fArr;
            this.mY = fArr2;
        }

        /* synthetic */ PathInterpolator(float[] fArr, float[] fArr2, PathInterpolator pathInterpolator) {
            this(fArr, fArr2);
        }

        @Override // android.animation.TimeInterpolator
        public float getInterpolation(float f) {
            if (f <= 0.0f) {
                return 0.0f;
            }
            if (f >= 1.0f) {
                return 1.0f;
            }
            int i = 0;
            int length = this.mX.length - 1;
            while (length - i > 1) {
                int i2 = (i + length) / 2;
                if (f < this.mX[i2]) {
                    length = i2;
                } else {
                    i = i2;
                }
            }
            float f2 = this.mX[length] - this.mX[i];
            if (f2 == 0.0f) {
                return this.mY[i];
            }
            float f3 = (f - this.mX[i]) / f2;
            float f4 = this.mY[i];
            return ((this.mY[length] - f4) * f3) + f4;
        }
    }

    public PathInterpolatorBuilder(float f, float f2, float f3, float f4) {
        initCubic(f, f2, f3, f4);
    }

    private void initCubic(float f, float f2, float f3, float f4) {
        Path path = new Path();
        path.moveTo(0.0f, 0.0f);
        path.cubicTo(f, f2, f3, f4, 1.0f, 1.0f);
        initPath(path);
    }

    private void initPath(Path path) {
        float[] approximate = path.approximate(0.002f);
        int length = approximate.length / 3;
        if (approximate[1] != 0.0f || approximate[2] != 0.0f || approximate[approximate.length - 2] != 1.0f || approximate[approximate.length - 1] != 1.0f) {
            throw new IllegalArgumentException("The Path must start at (0,0) and end at (1,1)");
        }
        this.mX = new float[length];
        this.mY = new float[length];
        this.mDist = new float[length];
        float f = 0.0f;
        float f2 = 0.0f;
        int i = 0;
        int i2 = 0;
        while (i < length) {
            int i3 = i2 + 1;
            float f3 = approximate[i2];
            int i4 = i3 + 1;
            float f4 = approximate[i3];
            float f5 = approximate[i4];
            if (f3 == f2 && f4 != f) {
                throw new IllegalArgumentException("The Path cannot have discontinuity in the X axis.");
            }
            if (f4 < f) {
                throw new IllegalArgumentException("The Path cannot loop back on itself.");
            }
            this.mX[i] = f4;
            this.mY[i] = f5;
            if (i > 0) {
                float f6 = this.mX[i] - this.mX[i - 1];
                float f7 = this.mY[i] - this.mY[i - 1];
                this.mDist[i] = this.mDist[i - 1] + ((float) Math.sqrt((f6 * f6) + (f7 * f7)));
            }
            f = f4;
            f2 = f3;
            i++;
            i2 = i4 + 1;
        }
        float f8 = this.mDist[this.mDist.length - 1];
        for (int i5 = 0; i5 < length; i5++) {
            float[] fArr = this.mDist;
            fArr[i5] = fArr[i5] / f8;
        }
    }

    public Interpolator getXInterpolator() {
        return new PathInterpolator(this.mDist, this.mX, null);
    }

    public Interpolator getYInterpolator() {
        return new PathInterpolator(this.mDist, this.mY, null);
    }
}
