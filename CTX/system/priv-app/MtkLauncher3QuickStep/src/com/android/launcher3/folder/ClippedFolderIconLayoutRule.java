package com.android.launcher3.folder;
/* loaded from: classes.dex */
public class ClippedFolderIconLayoutRule {
    public static final int ENTER_INDEX = -3;
    public static final int EXIT_INDEX = -2;
    private static final float ITEM_RADIUS_SCALE_FACTOR = 1.33f;
    public static final int MAX_NUM_ITEMS_IN_PREVIEW = 4;
    private static final float MAX_RADIUS_DILATION = 0.15f;
    private static final float MAX_SCALE = 0.58f;
    private static final int MIN_NUM_ITEMS_IN_PREVIEW = 2;
    private static final float MIN_SCALE = 0.48f;
    private float mAvailableSpace;
    private float mBaselineIconScale;
    private float mIconSize;
    private boolean mIsRtl;
    private float mRadius;
    private float[] mTmpPoint = new float[2];

    public void init(int i, float f, boolean z) {
        float f2 = i;
        this.mAvailableSpace = f2;
        this.mRadius = (ITEM_RADIUS_SCALE_FACTOR * f2) / 2.0f;
        this.mIconSize = f;
        this.mIsRtl = z;
        this.mBaselineIconScale = f2 / (f * 1.0f);
    }

    public PreviewItemDrawingParams computePreviewItemDrawingParams(int i, int i2, PreviewItemDrawingParams previewItemDrawingParams) {
        float scaleForItem = scaleForItem(i2);
        if (i == -2) {
            getGridPosition(0, 2, this.mTmpPoint);
        } else if (i == -3) {
            getGridPosition(1, 2, this.mTmpPoint);
        } else if (i >= 4) {
            float[] fArr = this.mTmpPoint;
            float[] fArr2 = this.mTmpPoint;
            float f = (this.mAvailableSpace / 2.0f) - ((this.mIconSize * scaleForItem) / 2.0f);
            fArr2[1] = f;
            fArr[0] = f;
        } else {
            getPosition(i, i2, this.mTmpPoint);
        }
        float f2 = this.mTmpPoint[0];
        float f3 = this.mTmpPoint[1];
        if (previewItemDrawingParams == null) {
            return new PreviewItemDrawingParams(f2, f3, scaleForItem, 0.0f);
        }
        previewItemDrawingParams.update(f2, f3, scaleForItem);
        previewItemDrawingParams.overlayAlpha = 0.0f;
        return previewItemDrawingParams;
    }

    private void getGridPosition(int i, int i2, float[] fArr) {
        getPosition(0, 4, fArr);
        float f = fArr[0];
        float f2 = fArr[1];
        getPosition(3, 4, fArr);
        fArr[0] = f + (i2 * (fArr[0] - f));
        fArr[1] = f2 + (i * (fArr[1] - f2));
    }

    private void getPosition(int i, int i2, float[] fArr) {
        double d;
        int i3;
        int max = Math.max(i2, 2);
        double d2 = 0.0d;
        if (!this.mIsRtl) {
            d = 3.141592653589793d;
        } else {
            d = 0.0d;
        }
        if (!this.mIsRtl) {
            i3 = -1;
        } else {
            i3 = 1;
        }
        int i4 = 3;
        if (max == 3) {
            d2 = 0.5235987755982988d;
        } else if (max == 4) {
            d2 = 0.7853981633974483d;
        }
        double d3 = i3;
        double d4 = d + (d2 * d3);
        if (max != 4 || i != 3) {
            if (max != 4 || i != 2) {
                i4 = i;
            }
        } else {
            i4 = 2;
        }
        float f = this.mRadius * (1.0f + ((MAX_RADIUS_DILATION * (max - 2)) / 2.0f));
        double d5 = d4 + (i4 * (6.283185307179586d / max) * d3);
        float scaleForItem = (this.mIconSize * scaleForItem(max)) / 2.0f;
        fArr[0] = ((this.mAvailableSpace / 2.0f) + ((float) ((f * Math.cos(d5)) / 2.0d))) - scaleForItem;
        fArr[1] = ((this.mAvailableSpace / 2.0f) + ((float) (((-f) * Math.sin(d5)) / 2.0d))) - scaleForItem;
    }

    public float scaleForItem(int i) {
        float f;
        if (i <= 2) {
            f = MAX_SCALE;
        } else if (i == 3) {
            f = 0.53f;
        } else {
            f = MIN_SCALE;
        }
        return f * this.mBaselineIconScale;
    }

    public float getIconSize() {
        return this.mIconSize;
    }
}
