package com.android.systemui.recents.views;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewDebug;
import com.android.systemui.recents.model.Task;
import com.mediatek.systemui.statusbar.util.FeatureOptions;
/* loaded from: a.zip:com/android/systemui/recents/views/TaskViewThumbnail.class */
public class TaskViewThumbnail extends View {
    private Paint mBgFillPaint;
    private BitmapShader mBitmapShader;
    private int mCornerRadius;
    @ViewDebug.ExportedProperty(category = "recents")
    private float mDimAlpha;
    @ViewDebug.ExportedProperty(category = "recents")
    private boolean mDisabledInSafeMode;
    private int mDisplayOrientation;
    private Rect mDisplayRect;
    private Paint mDrawPaint;
    private float mFullscreenThumbnailScale;
    @ViewDebug.ExportedProperty(category = "recents")
    private boolean mInvisible;
    private LightingColorFilter mLightingColorFilter;
    private Matrix mScaleMatrix;
    private Task mTask;
    private View mTaskBar;
    @ViewDebug.ExportedProperty(category = "recents")
    private Rect mTaskViewRect;
    private ActivityManager.TaskThumbnailInfo mThumbnailInfo;
    @ViewDebug.ExportedProperty(category = "recents")
    private Rect mThumbnailRect;
    @ViewDebug.ExportedProperty(category = "recents")
    private float mThumbnailScale;
    private static final ColorMatrix TMP_FILTER_COLOR_MATRIX = new ColorMatrix();
    private static final ColorMatrix TMP_BRIGHTNESS_COLOR_MATRIX = new ColorMatrix();

    public TaskViewThumbnail(Context context) {
        this(context, null);
    }

    public TaskViewThumbnail(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public TaskViewThumbnail(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public TaskViewThumbnail(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mDisplayOrientation = 0;
        this.mDisplayRect = new Rect();
        this.mTaskViewRect = new Rect();
        this.mThumbnailRect = new Rect();
        this.mScaleMatrix = new Matrix();
        this.mDrawPaint = new Paint();
        this.mBgFillPaint = new Paint();
        this.mLightingColorFilter = new LightingColorFilter(-1, 0);
        this.mDrawPaint.setColorFilter(this.mLightingColorFilter);
        this.mDrawPaint.setFilterBitmap(true);
        this.mDrawPaint.setAntiAlias(true);
        this.mCornerRadius = getResources().getDimensionPixelSize(2131690016);
        this.mBgFillPaint.setColor(-1);
        this.mFullscreenThumbnailScale = context.getResources().getFraction(18022404, 1, 1);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void bindToTask(Task task, boolean z, int i, Rect rect) {
        this.mTask = task;
        this.mDisabledInSafeMode = z;
        this.mDisplayOrientation = i;
        this.mDisplayRect.set(rect);
        if (task.colorBackground != 0) {
            this.mBgFillPaint.setColor(task.colorBackground);
        }
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        if (this.mInvisible) {
            return;
        }
        int width = this.mTaskViewRect.width();
        int height = this.mTaskViewRect.height();
        int min = Math.min(width, (int) (this.mThumbnailRect.width() * this.mThumbnailScale));
        int min2 = Math.min(height, (int) (this.mThumbnailRect.height() * this.mThumbnailScale));
        if (this.mBitmapShader == null || min <= 0 || min2 <= 0) {
            canvas.drawRoundRect(0.0f, 0.0f, width, height, this.mCornerRadius, this.mCornerRadius, this.mBgFillPaint);
            return;
        }
        int height2 = this.mTaskBar != null ? this.mTaskBar.getHeight() - this.mCornerRadius : 0;
        if (min < width) {
            canvas.drawRoundRect(Math.max(0, min - this.mCornerRadius), height2, width, height, this.mCornerRadius, this.mCornerRadius, this.mBgFillPaint);
        }
        if (min2 < height) {
            canvas.drawRoundRect(0.0f, Math.max(height2, min2 - this.mCornerRadius), width, height, this.mCornerRadius, this.mCornerRadius, this.mBgFillPaint);
        }
        canvas.drawRoundRect(0.0f, height2, min, min2, this.mCornerRadius, this.mCornerRadius, this.mDrawPaint);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void onTaskDataLoaded(ActivityManager.TaskThumbnailInfo taskThumbnailInfo) {
        if (this.mTask.thumbnail != null) {
            setThumbnail(this.mTask.thumbnail, taskThumbnailInfo);
        } else {
            setThumbnail(null, null);
        }
    }

    public void onTaskViewSizeChanged(int i, int i2) {
        if (this.mTaskViewRect.width() == i && this.mTaskViewRect.height() == i2) {
            return;
        }
        this.mTaskViewRect.set(0, 0, i, i2);
        setLeftTopRightBottom(0, 0, i, i2);
        updateThumbnailScale();
    }

    public void setDimAlpha(float f) {
        this.mDimAlpha = f;
        updateThumbnailPaintFilter();
    }

    void setThumbnail(Bitmap bitmap, ActivityManager.TaskThumbnailInfo taskThumbnailInfo) {
        if (bitmap == null) {
            this.mBitmapShader = null;
            this.mDrawPaint.setShader(null);
            this.mThumbnailRect.setEmpty();
            this.mThumbnailInfo = null;
            return;
        }
        this.mBitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        this.mDrawPaint.setShader(this.mBitmapShader);
        this.mThumbnailRect.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
        this.mThumbnailInfo = taskThumbnailInfo;
        updateThumbnailScale();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void unbindFromTask() {
        this.mTask = null;
        setThumbnail(null, null);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void updateClipToTaskBar(View view) {
        this.mTaskBar = view;
        invalidate();
    }

    void updateThumbnailPaintFilter() {
        if (this.mInvisible) {
            return;
        }
        int i = (int) ((1.0f - this.mDimAlpha) * 255.0f);
        if (this.mBitmapShader == null) {
            this.mDrawPaint.setColorFilter(null);
            this.mDrawPaint.setColor(Color.argb(255, i, i, i));
        } else if (this.mDisabledInSafeMode) {
            TMP_FILTER_COLOR_MATRIX.setSaturation(0.0f);
            float f = 1.0f - this.mDimAlpha;
            float[] array = TMP_BRIGHTNESS_COLOR_MATRIX.getArray();
            array[0] = f;
            array[6] = f;
            array[12] = f;
            array[4] = this.mDimAlpha * 255.0f;
            array[9] = this.mDimAlpha * 255.0f;
            array[14] = this.mDimAlpha * 255.0f;
            TMP_FILTER_COLOR_MATRIX.preConcat(TMP_BRIGHTNESS_COLOR_MATRIX);
            ColorMatrixColorFilter colorMatrixColorFilter = new ColorMatrixColorFilter(TMP_FILTER_COLOR_MATRIX);
            this.mDrawPaint.setColorFilter(colorMatrixColorFilter);
            this.mBgFillPaint.setColorFilter(colorMatrixColorFilter);
        } else {
            this.mLightingColorFilter.setColorMultiply(Color.argb(255, i, i, i));
            this.mDrawPaint.setColorFilter(this.mLightingColorFilter);
            this.mDrawPaint.setColor(-1);
            this.mBgFillPaint.setColorFilter(this.mLightingColorFilter);
        }
        if (this.mInvisible) {
            return;
        }
        invalidate();
    }

    public void updateThumbnailScale() {
        this.mThumbnailScale = 1.0f;
        if (this.mBitmapShader != null) {
            boolean z = !this.mTask.isFreeformTask() || this.mTask.bounds == null;
            if (this.mTaskViewRect.isEmpty() || this.mThumbnailInfo == null || this.mThumbnailInfo.taskWidth == 0 || this.mThumbnailInfo.taskHeight == 0) {
                this.mThumbnailScale = 0.0f;
            } else if (z) {
                float f = 1.0f / this.mFullscreenThumbnailScale;
                float f2 = f;
                if (FeatureOptions.LOW_RAM_SUPPORT) {
                    f2 = f * 2.0f;
                }
                if (this.mDisplayOrientation != 1) {
                    this.mThumbnailScale = f2;
                } else if (this.mThumbnailInfo.screenOrientation == 1) {
                    this.mThumbnailScale = this.mTaskViewRect.width() / this.mThumbnailRect.width();
                } else {
                    this.mThumbnailScale = (this.mTaskViewRect.width() / this.mDisplayRect.width()) * f2;
                }
            } else {
                this.mThumbnailScale = Math.min(this.mTaskViewRect.width() / this.mThumbnailRect.width(), this.mTaskViewRect.height() / this.mThumbnailRect.height());
            }
            this.mScaleMatrix.setScale(this.mThumbnailScale, this.mThumbnailScale);
            this.mBitmapShader.setLocalMatrix(this.mScaleMatrix);
        }
        if (this.mInvisible) {
            return;
        }
        invalidate();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void updateThumbnailVisibility(int i) {
        boolean z = this.mTaskBar != null && getHeight() - i <= this.mTaskBar.getHeight();
        if (z != this.mInvisible) {
            this.mInvisible = z;
            if (this.mInvisible) {
                return;
            }
            updateThumbnailPaintFilter();
        }
    }
}
