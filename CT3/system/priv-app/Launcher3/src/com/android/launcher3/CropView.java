package com.android.launcher3;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import com.android.photos.views.TiledImageRenderer;
import com.android.photos.views.TiledImageView;
/* loaded from: a.zip:com/android/launcher3/CropView.class */
public class CropView extends TiledImageView implements ScaleGestureDetector.OnScaleGestureListener {
    private float mCenterX;
    private float mCenterY;
    private float mFirstX;
    private float mFirstY;
    Matrix mInverseRotateMatrix;
    private float mLastX;
    private float mLastY;
    private float mMinScale;
    Matrix mRotateMatrix;
    private ScaleGestureDetector mScaleGestureDetector;
    private float[] mTempAdjustment;
    private float[] mTempCoef;
    private RectF mTempEdges;
    private float[] mTempImageDims;
    private float[] mTempPoint;
    private float[] mTempRendererCenter;
    TouchCallback mTouchCallback;
    private long mTouchDownTime;
    private boolean mTouchEnabled;

    /* loaded from: a.zip:com/android/launcher3/CropView$TouchCallback.class */
    public interface TouchCallback {
        void onTap();

        void onTouchDown();

        void onTouchUp();
    }

    public CropView(Context context) {
        this(context, null);
    }

    public CropView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mTouchEnabled = true;
        this.mTempEdges = new RectF();
        this.mTempPoint = new float[]{0.0f, 0.0f};
        this.mTempCoef = new float[]{0.0f, 0.0f};
        this.mTempAdjustment = new float[]{0.0f, 0.0f};
        this.mTempImageDims = new float[]{0.0f, 0.0f};
        this.mTempRendererCenter = new float[]{0.0f, 0.0f};
        this.mScaleGestureDetector = new ScaleGestureDetector(context, this);
        this.mRotateMatrix = new Matrix();
        this.mInverseRotateMatrix = new Matrix();
    }

    private void getEdgesHelper(RectF rectF) {
        float width = getWidth();
        float height = getHeight();
        float[] imageDims = getImageDims();
        float f = imageDims[0];
        float f2 = imageDims[1];
        float[] fArr = this.mTempRendererCenter;
        fArr[0] = this.mCenterX - (this.mRenderer.source.getImageWidth() / 2.0f);
        fArr[1] = this.mCenterY - (this.mRenderer.source.getImageHeight() / 2.0f);
        this.mRotateMatrix.mapPoints(fArr);
        fArr[0] = fArr[0] + (f / 2.0f);
        fArr[1] = fArr[1] + (f2 / 2.0f);
        float f3 = this.mRenderer.scale;
        float f4 = ((((width / 2.0f) - fArr[0]) + ((f - width) / 2.0f)) * f3) + (width / 2.0f);
        float f5 = ((((height / 2.0f) - fArr[1]) + ((f2 - height) / 2.0f)) * f3) + (height / 2.0f);
        float f6 = f / 2.0f;
        rectF.left = f4 - (f6 * f3);
        rectF.right = f4 + ((f / 2.0f) * f3);
        rectF.top = f5 - ((f2 / 2.0f) * f3);
        rectF.bottom = f5 + ((f2 / 2.0f) * f3);
    }

    private float[] getImageDims() {
        float imageWidth = this.mRenderer.source.getImageWidth();
        float imageHeight = this.mRenderer.source.getImageHeight();
        float[] fArr = this.mTempImageDims;
        fArr[0] = imageWidth;
        fArr[1] = imageHeight;
        this.mRotateMatrix.mapPoints(fArr);
        fArr[0] = Math.abs(fArr[0]);
        fArr[1] = Math.abs(fArr[1]);
        return fArr;
    }

    private void updateCenter() {
        this.mRenderer.centerX = Math.round(this.mCenterX);
        this.mRenderer.centerY = Math.round(this.mCenterY);
    }

    private void updateMinScale(int i, int i2, TiledImageRenderer.TileSource tileSource, boolean z) {
        synchronized (this.mLock) {
            if (z) {
                this.mRenderer.scale = 1.0f;
            }
            if (tileSource != null) {
                float[] imageDims = getImageDims();
                this.mMinScale = Math.max(i / imageDims[0], i2 / imageDims[1]);
                this.mRenderer.scale = Math.max(this.mMinScale, z ? Float.MIN_VALUE : this.mRenderer.scale);
            }
        }
    }

    public PointF getCenter() {
        return new PointF(this.mCenterX, this.mCenterY);
    }

    public RectF getCrop() {
        RectF rectF = this.mTempEdges;
        getEdgesHelper(rectF);
        float f = this.mRenderer.scale;
        float f2 = (-rectF.left) / f;
        float f3 = (-rectF.top) / f;
        return new RectF(f2, f3, f2 + (getWidth() / f), f3 + (getHeight() / f));
    }

    public int getImageRotation() {
        return this.mRenderer.rotation;
    }

    public float getScale() {
        return this.mRenderer.scale;
    }

    public Point getSourceDimensions() {
        return new Point(this.mRenderer.source.getImageWidth(), this.mRenderer.source.getImageHeight());
    }

    public void moveToLeft() {
        if (getWidth() == 0 || getHeight() == 0) {
            getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener(this) { // from class: com.android.launcher3.CropView.1
                final CropView this$0;

                {
                    this.this$0 = this;
                }

                @Override // android.view.ViewTreeObserver.OnGlobalLayoutListener
                public void onGlobalLayout() {
                    this.this$0.moveToLeft();
                    this.this$0.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
        }
        RectF rectF = this.mTempEdges;
        getEdgesHelper(rectF);
        this.mCenterX = (float) (this.mCenterX + Math.ceil(rectF.left / this.mRenderer.scale));
        updateCenter();
    }

    @Override // android.view.ScaleGestureDetector.OnScaleGestureListener
    public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
        this.mRenderer.scale *= scaleGestureDetector.getScaleFactor();
        this.mRenderer.scale = Math.max(this.mMinScale, this.mRenderer.scale);
        invalidate();
        return true;
    }

    @Override // android.view.ScaleGestureDetector.OnScaleGestureListener
    public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
        return true;
    }

    @Override // android.view.ScaleGestureDetector.OnScaleGestureListener
    public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
    }

    @Override // android.view.View
    protected void onSizeChanged(int i, int i2, int i3, int i4) {
        updateMinScale(i, i2, this.mRenderer.source, false);
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        float y;
        int actionMasked = motionEvent.getActionMasked();
        boolean z = actionMasked == 6;
        int actionIndex = z ? motionEvent.getActionIndex() : -1;
        float f = 0.0f;
        float f2 = 0.0f;
        int pointerCount = motionEvent.getPointerCount();
        int i = 0;
        while (i < pointerCount) {
            if (actionIndex == i) {
                y = f2;
            } else {
                f += motionEvent.getX(i);
                y = f2 + motionEvent.getY(i);
            }
            i++;
            f = f;
            f2 = y;
        }
        int i2 = z ? pointerCount - 1 : pointerCount;
        float f3 = f / i2;
        float f4 = f2 / i2;
        if (actionMasked == 0) {
            this.mFirstX = f3;
            this.mFirstY = f4;
            this.mTouchDownTime = System.currentTimeMillis();
            if (this.mTouchCallback != null) {
                this.mTouchCallback.onTouchDown();
            }
        } else if (actionMasked == 1) {
            ViewConfiguration viewConfiguration = ViewConfiguration.get(getContext());
            float f5 = this.mFirstX;
            float f6 = this.mFirstX;
            float f7 = this.mFirstY;
            float f8 = this.mFirstY;
            float scaledTouchSlop = viewConfiguration.getScaledTouchSlop() * viewConfiguration.getScaledTouchSlop();
            long currentTimeMillis = System.currentTimeMillis();
            if (this.mTouchCallback != null) {
                if (((f5 - f3) * (f6 - f3)) + ((f7 - f4) * (f8 - f4)) < scaledTouchSlop && currentTimeMillis < this.mTouchDownTime + ViewConfiguration.getTapTimeout()) {
                    this.mTouchCallback.onTap();
                }
                this.mTouchCallback.onTouchUp();
            }
        }
        if (this.mTouchEnabled) {
            synchronized (this.mLock) {
                this.mScaleGestureDetector.onTouchEvent(motionEvent);
                switch (actionMasked) {
                    case 2:
                        float[] fArr = this.mTempPoint;
                        fArr[0] = (this.mLastX - f3) / this.mRenderer.scale;
                        fArr[1] = (this.mLastY - f4) / this.mRenderer.scale;
                        this.mInverseRotateMatrix.mapPoints(fArr);
                        this.mCenterX += fArr[0];
                        this.mCenterY += fArr[1];
                        updateCenter();
                        invalidate();
                        break;
                }
                if (this.mRenderer.source != null) {
                    RectF rectF = this.mTempEdges;
                    getEdgesHelper(rectF);
                    float f9 = this.mRenderer.scale;
                    float[] fArr2 = this.mTempCoef;
                    fArr2[0] = 1.0f;
                    fArr2[1] = 1.0f;
                    this.mRotateMatrix.mapPoints(fArr2);
                    float[] fArr3 = this.mTempAdjustment;
                    this.mTempAdjustment[0] = 0.0f;
                    this.mTempAdjustment[1] = 0.0f;
                    if (rectF.left > 0.0f) {
                        fArr3[0] = rectF.left / f9;
                    } else if (rectF.right < getWidth()) {
                        fArr3[0] = (rectF.right - getWidth()) / f9;
                    }
                    if (rectF.top > 0.0f) {
                        fArr3[1] = (float) Math.ceil(rectF.top / f9);
                    } else if (rectF.bottom < getHeight()) {
                        fArr3[1] = (rectF.bottom - getHeight()) / f9;
                    }
                    for (int i3 = 0; i3 <= 1; i3++) {
                        if (fArr2[i3] > 0.0f) {
                            fArr3[i3] = (float) Math.ceil(fArr3[i3]);
                        }
                    }
                    this.mInverseRotateMatrix.mapPoints(fArr3);
                    this.mCenterX += fArr3[0];
                    this.mCenterY += fArr3[1];
                    updateCenter();
                }
            }
            this.mLastX = f3;
            this.mLastY = f4;
            return true;
        }
        return true;
    }

    public void setScaleAndCenter(float f, float f2, float f3) {
        synchronized (this.mLock) {
            this.mRenderer.scale = f;
            this.mCenterX = f2;
            this.mCenterY = f3;
            updateCenter();
        }
    }

    @Override // com.android.photos.views.TiledImageView
    public void setTileSource(TiledImageRenderer.TileSource tileSource, Runnable runnable) {
        super.setTileSource(tileSource, runnable);
        this.mCenterX = this.mRenderer.centerX;
        this.mCenterY = this.mRenderer.centerY;
        this.mRotateMatrix.reset();
        this.mRotateMatrix.setRotate(this.mRenderer.rotation);
        this.mInverseRotateMatrix.reset();
        this.mInverseRotateMatrix.setRotate(-this.mRenderer.rotation);
        updateMinScale(getWidth(), getHeight(), tileSource, true);
    }

    public void setTouchCallback(TouchCallback touchCallback) {
        this.mTouchCallback = touchCallback;
    }

    public void setTouchEnabled(boolean z) {
        this.mTouchEnabled = z;
    }
}
