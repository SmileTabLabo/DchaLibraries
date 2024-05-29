package com.android.launcher3.graphics;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.internal.view.SupportMenu;
import android.support.v4.view.ViewCompat;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.Utilities;
import com.android.launcher3.dragndrop.FolderAdaptiveIcon;
import java.nio.ByteBuffer;
/* loaded from: classes.dex */
public class IconNormalizer {
    private static final float BOUND_RATIO_MARGIN = 0.05f;
    private static final float CIRCLE_AREA_BY_RECT = 0.7853982f;
    private static final boolean DEBUG = false;
    public static final float ICON_VISIBLE_AREA_FACTOR = 0.92f;
    private static final float LINEAR_SCALE_SLOPE = 0.040449437f;
    private static final float MAX_CIRCLE_AREA_FACTOR = 0.6597222f;
    private static final float MAX_SQUARE_AREA_FACTOR = 0.6510417f;
    private static final int MIN_VISIBLE_ALPHA = 40;
    private static final float PIXEL_DIFF_PERCENTAGE_THRESHOLD = 0.005f;
    private static final float SCALE_NOT_INITIALIZED = 0.0f;
    private static final String TAG = "IconNormalizer";
    private float mAdaptiveIconScale;
    private final Bitmap mBitmap;
    private final Canvas mCanvas;
    private final float[] mLeftBorder;
    private final Matrix mMatrix;
    private final int mMaxSize;
    private final Paint mPaintMaskShapeOutline;
    private final byte[] mPixels;
    private final float[] mRightBorder;
    private final Path mShapePath;
    private final Rect mBounds = new Rect();
    private final Rect mAdaptiveIconBounds = new Rect();
    private final Paint mPaintMaskShape = new Paint();

    /* JADX INFO: Access modifiers changed from: package-private */
    public IconNormalizer(Context context) {
        this.mMaxSize = LauncherAppState.getIDP(context).iconBitmapSize * 2;
        this.mBitmap = Bitmap.createBitmap(this.mMaxSize, this.mMaxSize, Bitmap.Config.ALPHA_8);
        this.mCanvas = new Canvas(this.mBitmap);
        this.mPixels = new byte[this.mMaxSize * this.mMaxSize];
        this.mLeftBorder = new float[this.mMaxSize];
        this.mRightBorder = new float[this.mMaxSize];
        this.mPaintMaskShape.setColor(SupportMenu.CATEGORY_MASK);
        this.mPaintMaskShape.setStyle(Paint.Style.FILL);
        this.mPaintMaskShape.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.XOR));
        this.mPaintMaskShapeOutline = new Paint();
        this.mPaintMaskShapeOutline.setStrokeWidth(2.0f * context.getResources().getDisplayMetrics().density);
        this.mPaintMaskShapeOutline.setStyle(Paint.Style.STROKE);
        this.mPaintMaskShapeOutline.setColor(ViewCompat.MEASURED_STATE_MASK);
        this.mPaintMaskShapeOutline.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        this.mShapePath = new Path();
        this.mMatrix = new Matrix();
        this.mAdaptiveIconScale = 0.0f;
    }

    private boolean isShape(Path path) {
        if (Math.abs((this.mBounds.width() / this.mBounds.height()) - 1.0f) > BOUND_RATIO_MARGIN) {
            return false;
        }
        this.mMatrix.reset();
        this.mMatrix.setScale(this.mBounds.width(), this.mBounds.height());
        this.mMatrix.postTranslate(this.mBounds.left, this.mBounds.top);
        path.transform(this.mMatrix, this.mShapePath);
        this.mCanvas.drawPath(this.mShapePath, this.mPaintMaskShape);
        this.mCanvas.drawPath(this.mShapePath, this.mPaintMaskShapeOutline);
        return isTransparentBitmap();
    }

    private boolean isTransparentBitmap() {
        ByteBuffer wrap = ByteBuffer.wrap(this.mPixels);
        wrap.rewind();
        this.mBitmap.copyPixelsToBuffer(wrap);
        int i = this.mBounds.top;
        int i2 = this.mMaxSize * i;
        int i3 = this.mMaxSize - this.mBounds.right;
        int i4 = 0;
        while (i < this.mBounds.bottom) {
            int i5 = i2 + this.mBounds.left;
            for (int i6 = this.mBounds.left; i6 < this.mBounds.right; i6++) {
                if ((this.mPixels[i5] & 255) > 40) {
                    i4++;
                }
                i5++;
            }
            i2 = i5 + i3;
            i++;
        }
        return ((float) i4) / ((float) (this.mBounds.width() * this.mBounds.height())) < PIXEL_DIFF_PERCENTAGE_THRESHOLD;
    }

    /* JADX WARN: Code restructure failed: missing block: B:29:0x005c, code lost:
        if (r4 <= r18.mMaxSize) goto L93;
     */
    /* JADX WARN: Removed duplicated region for block: B:37:0x0098  */
    /* JADX WARN: Removed duplicated region for block: B:58:0x00fc A[Catch: all -> 0x01a3, TryCatch #0 {, blocks: (B:4:0x0009, B:6:0x000e, B:8:0x0012, B:11:0x001a, B:12:0x001f, B:15:0x0023, B:17:0x0027, B:18:0x0034, B:22:0x0041, B:24:0x0045, B:35:0x0068, B:39:0x009f, B:45:0x00b1, B:46:0x00b8, B:50:0x00ce, B:51:0x00d9, B:56:0x00eb, B:58:0x00fc, B:62:0x0112, B:61:0x0107, B:63:0x0115, B:67:0x0135, B:69:0x0147, B:71:0x016a, B:73:0x016d, B:74:0x0176, B:76:0x017d, B:77:0x0185, B:79:0x0189, B:81:0x018f, B:83:0x0196, B:66:0x012a, B:26:0x0049, B:28:0x005a, B:32:0x0062, B:34:0x0066, B:30:0x005e), top: B:91:0x0009 }] */
    /* JADX WARN: Removed duplicated region for block: B:65:0x0126  */
    /* JADX WARN: Removed duplicated region for block: B:66:0x012a A[Catch: all -> 0x01a3, TryCatch #0 {, blocks: (B:4:0x0009, B:6:0x000e, B:8:0x0012, B:11:0x001a, B:12:0x001f, B:15:0x0023, B:17:0x0027, B:18:0x0034, B:22:0x0041, B:24:0x0045, B:35:0x0068, B:39:0x009f, B:45:0x00b1, B:46:0x00b8, B:50:0x00ce, B:51:0x00d9, B:56:0x00eb, B:58:0x00fc, B:62:0x0112, B:61:0x0107, B:63:0x0115, B:67:0x0135, B:69:0x0147, B:71:0x016a, B:73:0x016d, B:74:0x0176, B:76:0x017d, B:77:0x0185, B:79:0x0189, B:81:0x018f, B:83:0x0196, B:66:0x012a, B:26:0x0049, B:28:0x005a, B:32:0x0062, B:34:0x0066, B:30:0x005e), top: B:91:0x0009 }] */
    /* JADX WARN: Removed duplicated region for block: B:69:0x0147 A[Catch: all -> 0x01a3, TryCatch #0 {, blocks: (B:4:0x0009, B:6:0x000e, B:8:0x0012, B:11:0x001a, B:12:0x001f, B:15:0x0023, B:17:0x0027, B:18:0x0034, B:22:0x0041, B:24:0x0045, B:35:0x0068, B:39:0x009f, B:45:0x00b1, B:46:0x00b8, B:50:0x00ce, B:51:0x00d9, B:56:0x00eb, B:58:0x00fc, B:62:0x0112, B:61:0x0107, B:63:0x0115, B:67:0x0135, B:69:0x0147, B:71:0x016a, B:73:0x016d, B:74:0x0176, B:76:0x017d, B:77:0x0185, B:79:0x0189, B:81:0x018f, B:83:0x0196, B:66:0x012a, B:26:0x0049, B:28:0x005a, B:32:0x0062, B:34:0x0066, B:30:0x005e), top: B:91:0x0009 }] */
    /* JADX WARN: Removed duplicated region for block: B:76:0x017d A[Catch: all -> 0x01a3, TryCatch #0 {, blocks: (B:4:0x0009, B:6:0x000e, B:8:0x0012, B:11:0x001a, B:12:0x001f, B:15:0x0023, B:17:0x0027, B:18:0x0034, B:22:0x0041, B:24:0x0045, B:35:0x0068, B:39:0x009f, B:45:0x00b1, B:46:0x00b8, B:50:0x00ce, B:51:0x00d9, B:56:0x00eb, B:58:0x00fc, B:62:0x0112, B:61:0x0107, B:63:0x0115, B:67:0x0135, B:69:0x0147, B:71:0x016a, B:73:0x016d, B:74:0x0176, B:76:0x017d, B:77:0x0185, B:79:0x0189, B:81:0x018f, B:83:0x0196, B:66:0x012a, B:26:0x0049, B:28:0x005a, B:32:0x0062, B:34:0x0066, B:30:0x005e), top: B:91:0x0009 }] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public synchronized float getScale(@NonNull Drawable drawable, @Nullable RectF rectF, @Nullable Path path, @Nullable boolean[] zArr) {
        int i;
        int i2;
        int i3;
        int i4;
        int i5;
        int i6;
        float f;
        float f2;
        float f3;
        float sqrt;
        Drawable drawable2 = drawable;
        synchronized (this) {
            if (Utilities.ATLEAST_OREO && (drawable2 instanceof AdaptiveIconDrawable)) {
                if (this.mAdaptiveIconScale != 0.0f) {
                    if (rectF != null) {
                        rectF.set(this.mAdaptiveIconBounds);
                    }
                    return this.mAdaptiveIconScale;
                } else if (drawable2 instanceof FolderAdaptiveIcon) {
                    drawable2 = new AdaptiveIconDrawable(new ColorDrawable(ViewCompat.MEASURED_STATE_MASK), null);
                }
            }
            int intrinsicWidth = drawable2.getIntrinsicWidth();
            int intrinsicHeight = drawable2.getIntrinsicHeight();
            if (intrinsicWidth > 0 && intrinsicHeight > 0) {
                if (intrinsicWidth > this.mMaxSize || intrinsicHeight > this.mMaxSize) {
                    int max = Math.max(intrinsicWidth, intrinsicHeight);
                    intrinsicWidth = (this.mMaxSize * intrinsicWidth) / max;
                    intrinsicHeight = (this.mMaxSize * intrinsicHeight) / max;
                }
                int i7 = 0;
                this.mBitmap.eraseColor(0);
                drawable2.setBounds(0, 0, intrinsicWidth, intrinsicHeight);
                drawable2.draw(this.mCanvas);
                ByteBuffer wrap = ByteBuffer.wrap(this.mPixels);
                wrap.rewind();
                this.mBitmap.copyPixelsToBuffer(wrap);
                int i8 = this.mMaxSize - intrinsicWidth;
                i = this.mMaxSize + 1;
                i2 = 0;
                int i9 = 0;
                i3 = -1;
                i4 = -1;
                i5 = -1;
                while (i2 < intrinsicHeight) {
                    int i10 = i9;
                    int i11 = -1;
                    int i12 = i7;
                    int i13 = -1;
                    while (i12 < intrinsicWidth) {
                        Drawable drawable3 = drawable2;
                        if ((this.mPixels[i10] & 255) > 40) {
                            if (i13 == -1) {
                                i13 = i12;
                            }
                            i11 = i12;
                        }
                        i10++;
                        i12++;
                        drawable2 = drawable3;
                    }
                    Drawable drawable4 = drawable2;
                    i9 = i10 + i8;
                    this.mLeftBorder[i2] = i13;
                    this.mRightBorder[i2] = i11;
                    if (i13 != -1) {
                        if (i3 == -1) {
                            i3 = i2;
                        }
                        int min = Math.min(i, i13);
                        i4 = Math.max(i4, i11);
                        i5 = i2;
                        i = min;
                    }
                    i2++;
                    drawable2 = drawable4;
                    i7 = 0;
                }
                Drawable drawable5 = drawable2;
                if (i3 != -1 && i4 != -1) {
                    convertToConvexArray(this.mLeftBorder, 1, i3, i5);
                    convertToConvexArray(this.mRightBorder, -1, i3, i5);
                    float f4 = 0.0f;
                    for (i6 = 0; i6 < intrinsicHeight; i6++) {
                        if (this.mLeftBorder[i6] > -1.0f) {
                            f4 += (this.mRightBorder[i6] - this.mLeftBorder[i6]) + 1.0f;
                        }
                    }
                    f = f4 / (((i5 + 1) - i3) * ((i4 + 1) - i));
                    if (f >= CIRCLE_AREA_BY_RECT) {
                        f2 = MAX_CIRCLE_AREA_FACTOR;
                    } else {
                        f2 = MAX_SQUARE_AREA_FACTOR + (LINEAR_SCALE_SLOPE * (1.0f - f));
                    }
                    this.mBounds.left = i;
                    this.mBounds.right = i4;
                    this.mBounds.top = i3;
                    this.mBounds.bottom = i5;
                    if (rectF != null) {
                        float f5 = intrinsicWidth;
                        float f6 = intrinsicHeight;
                        rectF.set(this.mBounds.left / f5, this.mBounds.top / f6, 1.0f - (this.mBounds.right / f5), 1.0f - (this.mBounds.bottom / f6));
                    }
                    if (zArr != null && zArr.length > 0) {
                        zArr[0] = isShape(path);
                    }
                    sqrt = f4 / (intrinsicWidth * intrinsicHeight) > f2 ? (float) Math.sqrt(f2 / f3) : 1.0f;
                    if (Utilities.ATLEAST_OREO && (drawable5 instanceof AdaptiveIconDrawable) && this.mAdaptiveIconScale == 0.0f) {
                        this.mAdaptiveIconScale = sqrt;
                        this.mAdaptiveIconBounds.set(this.mBounds);
                    }
                    return sqrt;
                }
                return 1.0f;
            }
            intrinsicWidth = this.mMaxSize;
            if (intrinsicHeight <= 0 || intrinsicHeight > this.mMaxSize) {
                intrinsicHeight = this.mMaxSize;
            }
            int i72 = 0;
            this.mBitmap.eraseColor(0);
            drawable2.setBounds(0, 0, intrinsicWidth, intrinsicHeight);
            drawable2.draw(this.mCanvas);
            ByteBuffer wrap2 = ByteBuffer.wrap(this.mPixels);
            wrap2.rewind();
            this.mBitmap.copyPixelsToBuffer(wrap2);
            int i82 = this.mMaxSize - intrinsicWidth;
            i = this.mMaxSize + 1;
            i2 = 0;
            int i92 = 0;
            i3 = -1;
            i4 = -1;
            i5 = -1;
            while (i2 < intrinsicHeight) {
            }
            Drawable drawable52 = drawable2;
            if (i3 != -1) {
                convertToConvexArray(this.mLeftBorder, 1, i3, i5);
                convertToConvexArray(this.mRightBorder, -1, i3, i5);
                float f42 = 0.0f;
                while (i6 < intrinsicHeight) {
                }
                f = f42 / (((i5 + 1) - i3) * ((i4 + 1) - i));
                if (f >= CIRCLE_AREA_BY_RECT) {
                }
                this.mBounds.left = i;
                this.mBounds.right = i4;
                this.mBounds.top = i3;
                this.mBounds.bottom = i5;
                if (rectF != null) {
                }
                if (zArr != null) {
                    zArr[0] = isShape(path);
                }
                if (f42 / (intrinsicWidth * intrinsicHeight) > f2) {
                }
                if (Utilities.ATLEAST_OREO) {
                    this.mAdaptiveIconScale = sqrt;
                    this.mAdaptiveIconBounds.set(this.mBounds);
                }
                return sqrt;
            }
            return 1.0f;
        }
    }

    private static void convertToConvexArray(float[] fArr, int i, int i2, int i3) {
        float[] fArr2 = new float[fArr.length - 1];
        int i4 = -1;
        float f = Float.MAX_VALUE;
        for (int i5 = i2 + 1; i5 <= i3; i5++) {
            if (fArr[i5] > -1.0f) {
                if (f != Float.MAX_VALUE) {
                    float f2 = ((fArr[i5] - fArr[i4]) / (i5 - i4)) - f;
                    float f3 = i;
                    if (f2 * f3 < 0.0f) {
                        while (i4 > i2) {
                            i4--;
                            if ((((fArr[i5] - fArr[i4]) / (i5 - i4)) - fArr2[i4]) * f3 >= 0.0f) {
                                break;
                            }
                        }
                    }
                } else {
                    i4 = i2;
                }
                f = (fArr[i5] - fArr[i4]) / (i5 - i4);
                for (int i6 = i4; i6 < i5; i6++) {
                    fArr2[i6] = f;
                    fArr[i6] = fArr[i4] + ((i6 - i4) * f);
                }
                i4 = i5;
            }
        }
    }

    public static int getNormalizedCircleSize(int i) {
        return (int) Math.round(Math.sqrt((4.0f * ((i * i) * MAX_CIRCLE_AREA_FACTOR)) / 3.141592653589793d));
    }
}
