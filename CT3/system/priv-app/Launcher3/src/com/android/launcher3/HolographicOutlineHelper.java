package com.android.launcher3;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;
/* loaded from: a.zip:com/android/launcher3/HolographicOutlineHelper.class */
public class HolographicOutlineHelper {
    private static HolographicOutlineHelper sInstance;
    private final BlurMaskFilter mMediumInnerBlurMaskFilter;
    private final BlurMaskFilter mMediumOuterBlurMaskFilter;
    private final BlurMaskFilter mShadowBlurMaskFilter;
    private final BlurMaskFilter mThinOuterBlurMaskFilter;
    private final Canvas mCanvas = new Canvas();
    private final Paint mDrawPaint = new Paint();
    private final Paint mBlurPaint = new Paint();
    private final Paint mErasePaint = new Paint();
    private final SparseArray<Bitmap> mBitmapCache = new SparseArray<>(4);

    private HolographicOutlineHelper(Context context) {
        Resources resources = context.getResources();
        float dimension = resources.getDimension(2131230807);
        this.mMediumOuterBlurMaskFilter = new BlurMaskFilter(dimension, BlurMaskFilter.Blur.OUTER);
        this.mMediumInnerBlurMaskFilter = new BlurMaskFilter(dimension, BlurMaskFilter.Blur.NORMAL);
        this.mThinOuterBlurMaskFilter = new BlurMaskFilter(resources.getDimension(2131230806), BlurMaskFilter.Blur.OUTER);
        this.mShadowBlurMaskFilter = new BlurMaskFilter(resources.getDimension(2131230808), BlurMaskFilter.Blur.NORMAL);
        this.mDrawPaint.setFilterBitmap(true);
        this.mDrawPaint.setAntiAlias(true);
        this.mBlurPaint.setFilterBitmap(true);
        this.mBlurPaint.setAntiAlias(true);
        this.mErasePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        this.mErasePaint.setFilterBitmap(true);
        this.mErasePaint.setAntiAlias(true);
    }

    public static HolographicOutlineHelper obtain(Context context) {
        if (sInstance == null) {
            sInstance = new HolographicOutlineHelper(context);
        }
        return sInstance;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void applyExpensiveOutlineWithBlur(Bitmap bitmap, Canvas canvas, int i, int i2) {
        applyExpensiveOutlineWithBlur(bitmap, canvas, i, i2, true);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void applyExpensiveOutlineWithBlur(Bitmap bitmap, Canvas canvas, int i, int i2, boolean z) {
        int[] iArr;
        int[] iArr2;
        int[] iArr3;
        if (z) {
            int[] iArr4 = new int[bitmap.getWidth() * bitmap.getHeight()];
            bitmap.getPixels(iArr4, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
            for (int i3 = 0; i3 < iArr4.length; i3++) {
                if ((iArr4[i3] >>> 24) < 188) {
                    iArr4[i3] = 0;
                }
            }
            bitmap.setPixels(iArr4, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        }
        Bitmap extractAlpha = bitmap.extractAlpha();
        this.mBlurPaint.setMaskFilter(this.mMediumOuterBlurMaskFilter);
        Bitmap extractAlpha2 = extractAlpha.extractAlpha(this.mBlurPaint, new int[2]);
        this.mBlurPaint.setMaskFilter(this.mThinOuterBlurMaskFilter);
        Bitmap extractAlpha3 = extractAlpha.extractAlpha(this.mBlurPaint, new int[2]);
        canvas.setBitmap(extractAlpha);
        canvas.drawColor(-16777216, PorterDuff.Mode.SRC_OUT);
        this.mBlurPaint.setMaskFilter(this.mMediumInnerBlurMaskFilter);
        Bitmap extractAlpha4 = extractAlpha.extractAlpha(this.mBlurPaint, new int[2]);
        canvas.setBitmap(extractAlpha4);
        canvas.drawBitmap(extractAlpha, -iArr3[0], -iArr3[1], this.mErasePaint);
        canvas.drawRect(0.0f, 0.0f, -iArr3[0], extractAlpha4.getHeight(), this.mErasePaint);
        canvas.drawRect(0.0f, 0.0f, extractAlpha4.getWidth(), -iArr3[1], this.mErasePaint);
        canvas.setBitmap(bitmap);
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        this.mDrawPaint.setColor(i);
        canvas.drawBitmap(extractAlpha4, iArr3[0], iArr3[1], this.mDrawPaint);
        canvas.drawBitmap(extractAlpha2, iArr[0], iArr[1], this.mDrawPaint);
        this.mDrawPaint.setColor(i2);
        canvas.drawBitmap(extractAlpha3, iArr2[0], iArr2[1], this.mDrawPaint);
        canvas.setBitmap(null);
        extractAlpha3.recycle();
        extractAlpha2.recycle();
        extractAlpha4.recycle();
        extractAlpha.recycle();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public Bitmap createMediumDropShadow(BubbleTextView bubbleTextView) {
        Drawable icon = bubbleTextView.getIcon();
        if (icon == null) {
            return null;
        }
        Rect bounds = icon.getBounds();
        int width = (int) (bounds.width() * bubbleTextView.getScaleX());
        int height = (int) (bounds.height() * bubbleTextView.getScaleY());
        if (height <= 0 || width <= 0) {
            return null;
        }
        int i = (width << 16) | height;
        Bitmap bitmap = this.mBitmapCache.get(i);
        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            this.mCanvas.setBitmap(bitmap);
            this.mBitmapCache.put(i, bitmap);
        } else {
            this.mCanvas.setBitmap(bitmap);
            this.mCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        }
        this.mCanvas.save(1);
        this.mCanvas.scale(bubbleTextView.getScaleX(), bubbleTextView.getScaleY());
        this.mCanvas.translate(-bounds.left, -bounds.top);
        icon.draw(this.mCanvas);
        this.mCanvas.restore();
        this.mCanvas.setBitmap(null);
        this.mBlurPaint.setMaskFilter(this.mShadowBlurMaskFilter);
        return bitmap.extractAlpha(this.mBlurPaint, null);
    }
}
