package com.android.photos;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import java.io.InputStream;
/* loaded from: a.zip:com/android/photos/DumbBitmapRegionDecoder.class */
class DumbBitmapRegionDecoder implements SimpleBitmapRegionDecoder {
    Bitmap mBuffer;
    Canvas mTempCanvas;
    Paint mTempPaint;

    private DumbBitmapRegionDecoder(Bitmap bitmap) {
        this.mBuffer = bitmap;
    }

    public static DumbBitmapRegionDecoder newInstance(InputStream inputStream) {
        try {
            Bitmap decodeStream = BitmapFactory.decodeStream(inputStream);
            if (decodeStream != null) {
                return new DumbBitmapRegionDecoder(decodeStream);
            }
            return null;
        } catch (OutOfMemoryError e) {
            Log.e("BitmapRegionTileSource", "Failed to decodeStreamI " + inputStream, e);
            return null;
        }
    }

    @Override // com.android.photos.SimpleBitmapRegionDecoder
    public Bitmap decodeRegion(Rect rect, BitmapFactory.Options options) {
        if (this.mTempCanvas == null) {
            this.mTempCanvas = new Canvas();
            this.mTempPaint = new Paint();
            this.mTempPaint.setFilterBitmap(true);
        }
        int max = Math.max(options.inSampleSize, 1);
        Bitmap createBitmap = Bitmap.createBitmap(rect.width() / max, rect.height() / max, Bitmap.Config.ARGB_8888);
        this.mTempCanvas.setBitmap(createBitmap);
        this.mTempCanvas.save();
        this.mTempCanvas.scale(1.0f / max, 1.0f / max);
        this.mTempCanvas.drawBitmap(this.mBuffer, -rect.left, -rect.top, this.mTempPaint);
        this.mTempCanvas.restore();
        this.mTempCanvas.setBitmap(null);
        return createBitmap;
    }

    @Override // com.android.photos.SimpleBitmapRegionDecoder
    public int getHeight() {
        return this.mBuffer.getHeight();
    }

    @Override // com.android.photos.SimpleBitmapRegionDecoder
    public int getWidth() {
        return this.mBuffer.getWidth();
    }
}
