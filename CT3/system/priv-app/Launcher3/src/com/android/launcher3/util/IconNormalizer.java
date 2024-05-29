package com.android.launcher3.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import com.android.launcher3.LauncherAppState;
import java.nio.ByteBuffer;
/* loaded from: a.zip:com/android/launcher3/util/IconNormalizer.class */
public class IconNormalizer {
    private static final Object LOCK = new Object();
    private static IconNormalizer sIconNormalizer;
    private final int mMaxSize = LauncherAppState.getInstance().getInvariantDeviceProfile().iconBitmapSize * 2;
    private final Bitmap mBitmap = Bitmap.createBitmap(this.mMaxSize, this.mMaxSize, Bitmap.Config.ALPHA_8);
    private final Canvas mCanvas = new Canvas(this.mBitmap);
    private final byte[] mPixels = new byte[this.mMaxSize * this.mMaxSize];
    private final float[] mLeftBorder = new float[this.mMaxSize];
    private final float[] mRightBorder = new float[this.mMaxSize];

    private IconNormalizer() {
    }

    private static void convertToConvexArray(float[] fArr, int i, int i2, int i3) {
        int i4;
        float[] fArr2 = new float[fArr.length - 1];
        int i5 = -1;
        float f = Float.MAX_VALUE;
        for (int i6 = i2 + 1; i6 <= i3; i6++) {
            if (fArr[i6] > -1.0f) {
                if (f == Float.MAX_VALUE) {
                    i4 = i2;
                } else {
                    i4 = i5;
                    if ((((fArr[i6] - fArr[i5]) / (i6 - i5)) - f) * i < 0.0f) {
                        do {
                            i4 = i5;
                            if (i5 <= i2) {
                                break;
                            }
                            i4 = i5 - 1;
                            i5 = i4;
                        } while ((((fArr[i6] - fArr[i4]) / (i6 - i4)) - fArr2[i4]) * i < 0.0f);
                    }
                }
                f = (fArr[i6] - fArr[i4]) / (i6 - i4);
                for (int i7 = i4; i7 < i6; i7++) {
                    fArr2[i7] = f;
                    fArr[i7] = fArr[i4] + ((i7 - i4) * f);
                }
                i5 = i6;
            }
        }
    }

    public static IconNormalizer getInstance() {
        synchronized (LOCK) {
            if (sIconNormalizer == null) {
                sIconNormalizer = new IconNormalizer();
            }
        }
        return sIconNormalizer;
    }

    /* JADX WARN: Code restructure failed: missing block: B:12:0x0020, code lost:
        if (r0 > r6.mMaxSize) goto L96;
     */
    /* JADX WARN: Code restructure failed: missing block: B:19:0x0039, code lost:
        if (r0 > r6.mMaxSize) goto L93;
     */
    /* JADX WARN: Code restructure failed: missing block: B:48:0x00fe, code lost:
        if (r0 > r6.mMaxSize) goto L81;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public float getScale(Drawable drawable) {
        int i;
        int i2;
        int i3;
        float f;
        synchronized (this) {
            int intrinsicWidth = drawable.getIntrinsicWidth();
            int intrinsicHeight = drawable.getIntrinsicHeight();
            if (intrinsicWidth <= 0 || intrinsicHeight <= 0) {
                if (intrinsicWidth > 0) {
                    i = intrinsicWidth;
                }
                i = this.mMaxSize;
                if (intrinsicHeight > 0) {
                    i2 = intrinsicHeight;
                    i3 = i;
                }
                i2 = this.mMaxSize;
                i3 = i;
            } else {
                if (intrinsicWidth <= this.mMaxSize) {
                    i2 = intrinsicHeight;
                    i3 = intrinsicWidth;
                }
                int max = Math.max(intrinsicWidth, intrinsicHeight);
                i3 = (this.mMaxSize * intrinsicWidth) / max;
                i2 = (this.mMaxSize * intrinsicHeight) / max;
            }
            this.mBitmap.eraseColor(0);
            drawable.setBounds(0, 0, i3, i2);
            drawable.draw(this.mCanvas);
            ByteBuffer wrap = ByteBuffer.wrap(this.mPixels);
            wrap.rewind();
            this.mBitmap.copyPixelsToBuffer(wrap);
            int i4 = -1;
            int i5 = -1;
            int i6 = this.mMaxSize + 1;
            int i7 = -1;
            int i8 = 0;
            int i9 = this.mMaxSize;
            int i10 = 0;
            while (i10 < i2) {
                int i11 = -1;
                int i12 = 0;
                int i13 = i8;
                int i14 = -1;
                while (i12 < i3) {
                    int i15 = i14;
                    int i16 = i11;
                    if ((this.mPixels[i13] & 255) > 40) {
                        int i17 = i14;
                        if (i14 == -1) {
                            i17 = i12;
                        }
                        i16 = i12;
                        i15 = i17;
                    }
                    i13++;
                    i12++;
                    i14 = i15;
                    i11 = i16;
                }
                int i18 = i13 + (i9 - i3);
                this.mLeftBorder[i10] = i14;
                this.mRightBorder[i10] = i11;
                int i19 = i6;
                int i20 = i7;
                int i21 = i4;
                if (i14 != -1) {
                    i5 = i10;
                    i21 = i4;
                    if (i4 == -1) {
                        i21 = i10;
                    }
                    i19 = Math.min(i6, i14);
                    i20 = Math.max(i7, i11);
                }
                i10++;
                i8 = i18;
                i6 = i19;
                i7 = i20;
                i4 = i21;
            }
            if (i4 == -1 || i7 == -1) {
                return 1.0f;
            }
            convertToConvexArray(this.mLeftBorder, 1, i4, i5);
            convertToConvexArray(this.mRightBorder, -1, i4, i5);
            float f2 = 0.0f;
            for (int i22 = 0; i22 < i2; i22++) {
                if (this.mLeftBorder[i22] > -1.0f) {
                    f2 += (this.mRightBorder[i22] - this.mLeftBorder[i22]) + 1.0f;
                }
            }
            float f3 = f2 / (((i5 + 1) - i4) * ((i7 + 1) - i6));
            float f4 = f3 < 0.7853982f ? 0.6597222f : 0.6232639f + ((1.0f - f3) * 0.16988818f);
            return f2 / (i3 * i2) > f4 ? (float) Math.sqrt(f4 / f) : 1.0f;
        }
    }
}
