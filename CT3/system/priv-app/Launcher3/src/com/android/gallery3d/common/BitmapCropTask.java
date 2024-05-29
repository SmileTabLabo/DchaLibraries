package com.android.gallery3d.common;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import com.android.launcher3.NycWallpaperUtils;
import com.android.launcher3.Utilities;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
/* loaded from: a.zip:com/android/gallery3d/common/BitmapCropTask.class */
public class BitmapCropTask extends AsyncTask<Integer, Void, Boolean> {
    Context mContext;
    RectF mCropBounds;
    Bitmap mCroppedBitmap;
    String mInFilePath;
    byte[] mInImageBytes;
    int mInResId;
    Uri mInUri;
    boolean mNoCrop;
    OnBitmapCroppedHandler mOnBitmapCroppedHandler;
    OnEndCropHandler mOnEndCropHandler;
    int mOutHeight;
    int mOutWidth;
    Resources mResources;
    int mRotation;
    boolean mSaveCroppedBitmap;
    boolean mSetWallpaper;

    /* loaded from: a.zip:com/android/gallery3d/common/BitmapCropTask$OnBitmapCroppedHandler.class */
    public interface OnBitmapCroppedHandler {
        void onBitmapCropped(byte[] bArr, Rect rect);
    }

    /* loaded from: a.zip:com/android/gallery3d/common/BitmapCropTask$OnEndCropHandler.class */
    public interface OnEndCropHandler {
        void run(boolean z);
    }

    public BitmapCropTask(Context context, Resources resources, int i, RectF rectF, int i2, int i3, int i4, boolean z, boolean z2, OnEndCropHandler onEndCropHandler) {
        this.mInUri = null;
        this.mInResId = 0;
        this.mCropBounds = null;
        this.mContext = context;
        this.mInResId = i;
        this.mResources = resources;
        init(rectF, i2, i3, i4, z, z2, onEndCropHandler);
    }

    public BitmapCropTask(Context context, Uri uri, RectF rectF, int i, int i2, int i3, boolean z, boolean z2, OnEndCropHandler onEndCropHandler) {
        this.mInUri = null;
        this.mInResId = 0;
        this.mCropBounds = null;
        this.mContext = context;
        this.mInUri = uri;
        init(rectF, i, i2, i3, z, z2, onEndCropHandler);
    }

    public BitmapCropTask(byte[] bArr, RectF rectF, int i, int i2, int i3, boolean z, boolean z2, OnEndCropHandler onEndCropHandler) {
        this.mInUri = null;
        this.mInResId = 0;
        this.mCropBounds = null;
        this.mInImageBytes = bArr;
        init(rectF, i, i2, i3, z, z2, onEndCropHandler);
    }

    private void init(RectF rectF, int i, int i2, int i3, boolean z, boolean z2, OnEndCropHandler onEndCropHandler) {
        this.mCropBounds = rectF;
        this.mRotation = i;
        this.mOutWidth = i2;
        this.mOutHeight = i3;
        this.mSetWallpaper = z;
        this.mSaveCroppedBitmap = z2;
        this.mOnEndCropHandler = onEndCropHandler;
    }

    private InputStream regenerateInputStream() {
        if (this.mInUri == null && this.mInResId == 0 && this.mInFilePath == null && this.mInImageBytes == null) {
            Log.w("BitmapCropTask", "cannot read original file, no input URI, resource ID, or image byte array given");
            return null;
        }
        try {
            return this.mInUri != null ? new BufferedInputStream(this.mContext.getContentResolver().openInputStream(this.mInUri)) : this.mInFilePath != null ? this.mContext.openFileInput(this.mInFilePath) : this.mInImageBytes != null ? new BufferedInputStream(new ByteArrayInputStream(this.mInImageBytes)) : new BufferedInputStream(this.mResources.openRawResource(this.mInResId));
        } catch (FileNotFoundException e) {
            Log.w("BitmapCropTask", "cannot read file: " + this.mInUri.toString(), e);
            return null;
        } catch (SecurityException e2) {
            Log.w("BitmapCropTask", "security exception: " + this.mInUri.toString(), e2);
            return null;
        }
    }

    private void setWallpaper(InputStream inputStream, Rect rect, int i) throws IOException {
        if (Utilities.ATLEAST_N) {
            NycWallpaperUtils.setStream(this.mContext, inputStream, rect, true, i);
        } else {
            WallpaperManager.getInstance(this.mContext.getApplicationContext()).setStream(inputStream);
        }
    }

    /* JADX WARN: Code restructure failed: missing block: B:145:0x05f0, code lost:
        if (r8.mRotation > 0) goto L110;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public boolean cropBitmap(int i) {
        Bitmap bitmap;
        boolean z;
        InputStream regenerateInputStream;
        boolean z2;
        if (this.mSetWallpaper && this.mNoCrop) {
            try {
                setWallpaper(regenerateInputStream(), null, i);
                z2 = false;
            } catch (IOException e) {
                Log.w("BitmapCropTask", "cannot write stream to wallpaper", e);
                z2 = true;
            }
            return !z2;
        }
        Rect rect = new Rect();
        Matrix matrix = new Matrix();
        Matrix matrix2 = new Matrix();
        Point imageBounds = getImageBounds();
        if (this.mRotation > 0) {
            matrix.setRotate(this.mRotation);
            matrix2.setRotate(-this.mRotation);
            this.mCropBounds.roundOut(rect);
            this.mCropBounds = new RectF(rect);
            if (imageBounds == null) {
                Log.w("BitmapCropTask", "cannot get bounds for image");
                return false;
            }
            float[] fArr = {imageBounds.x, imageBounds.y};
            matrix.mapPoints(fArr);
            fArr[0] = Math.abs(fArr[0]);
            fArr[1] = Math.abs(fArr[1]);
            this.mCropBounds.offset((-fArr[0]) / 2.0f, (-fArr[1]) / 2.0f);
            matrix2.mapRect(this.mCropBounds);
            this.mCropBounds.offset(imageBounds.x / 2, imageBounds.y / 2);
        }
        this.mCropBounds.roundOut(rect);
        if (rect.width() <= 0 || rect.height() <= 0) {
            Log.w("BitmapCropTask", "crop has bad values for full size image");
            return false;
        }
        if (this.mOutWidth <= 0) {
            Log.w("BitmapCropTask", "mOutWidth is zero, mOutWidth:" + this.mOutWidth);
            this.mOutWidth = 1;
        }
        if (this.mOutHeight <= 0) {
            Log.w("BitmapCropTask", "mOutHeight is zero, mOutHeight:" + this.mOutHeight);
            this.mOutHeight = 1;
        }
        int max = Math.max(1, Math.min(rect.width() / this.mOutWidth, rect.height() / this.mOutHeight));
        BitmapRegionDecoder bitmapRegionDecoder = null;
        try {
            try {
                regenerateInputStream = regenerateInputStream();
            } catch (IOException e2) {
                Log.w("BitmapCropTask", "cannot open region decoder for file: " + this.mInUri.toString(), e2);
                Utils.closeSilently(null);
            }
            if (regenerateInputStream == null) {
                Log.w("BitmapCropTask", "cannot get input stream for uri=" + this.mInUri.toString());
                Utils.closeSilently(regenerateInputStream);
                return false;
            }
            BitmapRegionDecoder newInstance = BitmapRegionDecoder.newInstance(regenerateInputStream, false);
            Utils.closeSilently(regenerateInputStream);
            bitmapRegionDecoder = newInstance;
            Bitmap bitmap2 = null;
            if (bitmapRegionDecoder != null) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                if (max > 1) {
                    options.inSampleSize = max;
                }
                bitmap2 = bitmapRegionDecoder.decodeRegion(rect, options);
                bitmapRegionDecoder.recycle();
            }
            Bitmap bitmap3 = bitmap2;
            if (bitmap2 == null) {
                regenerateInputStream = regenerateInputStream();
                Bitmap bitmap4 = null;
                if (regenerateInputStream != null) {
                    BitmapFactory.Options options2 = new BitmapFactory.Options();
                    if (max > 1) {
                        options2.inSampleSize = max;
                    }
                    try {
                        try {
                            bitmap4 = BitmapFactory.decodeStream(regenerateInputStream, null, options2);
                        } catch (OutOfMemoryError e3) {
                            Log.e("BitmapCropTask", "Failed to decodeStreamI " + regenerateInputStream, e3);
                            Utils.closeSilently(regenerateInputStream);
                            return false;
                        }
                    } finally {
                        Utils.closeSilently(regenerateInputStream);
                    }
                }
                bitmap3 = bitmap2;
                if (bitmap4 != null) {
                    int width = imageBounds.x / bitmap4.getWidth();
                    this.mCropBounds.left /= width;
                    this.mCropBounds.top /= width;
                    this.mCropBounds.bottom /= width;
                    this.mCropBounds.right /= width;
                    this.mCropBounds.roundOut(rect);
                    if (rect.width() > bitmap4.getWidth()) {
                        rect.right = rect.left + bitmap4.getWidth();
                    }
                    if (rect.right > bitmap4.getWidth()) {
                        rect.offset(-(rect.right - bitmap4.getWidth()), 0);
                    }
                    if (rect.height() > bitmap4.getHeight()) {
                        rect.bottom = rect.top + bitmap4.getHeight();
                    }
                    if (rect.bottom > bitmap4.getHeight()) {
                        rect.offset(0, -(rect.bottom - bitmap4.getHeight()));
                    }
                    if (rect.width() <= 0 || rect.height() <= 0) {
                        Log.w("BitmapCropTask", "crop has bad values for full size image");
                        return false;
                    }
                    try {
                        bitmap3 = Bitmap.createBitmap(bitmap4, rect.left, rect.top, rect.width(), rect.height());
                    } catch (OutOfMemoryError e4) {
                        Log.w("BitmapCropTask", "Wallpaper too large, createBitmap fail");
                        bitmap3 = bitmap2;
                    }
                }
            }
            if (bitmap3 == null) {
                Log.w("BitmapCropTask", "cannot decode file: " + this.mInUri.toString());
                return false;
            }
            if (this.mOutWidth <= 0 || this.mOutHeight <= 0) {
                bitmap = bitmap3;
            }
            float[] fArr2 = {bitmap3.getWidth(), bitmap3.getHeight()};
            matrix.mapPoints(fArr2);
            fArr2[0] = Math.abs(fArr2[0]);
            fArr2[1] = Math.abs(fArr2[1]);
            if (this.mOutWidth <= 0 || this.mOutHeight <= 0) {
                this.mOutWidth = Math.round(fArr2[0]);
                this.mOutHeight = Math.round(fArr2[1]);
            }
            RectF rectF = new RectF(0.0f, 0.0f, fArr2[0], fArr2[1]);
            RectF rectF2 = new RectF(0.0f, 0.0f, this.mOutWidth, this.mOutHeight);
            Matrix matrix3 = new Matrix();
            if (this.mRotation == 0) {
                matrix3.setRectToRect(rectF, rectF2, Matrix.ScaleToFit.FILL);
            } else {
                Matrix matrix4 = new Matrix();
                matrix4.setTranslate((-bitmap3.getWidth()) / 2.0f, (-bitmap3.getHeight()) / 2.0f);
                Matrix matrix5 = new Matrix();
                matrix5.setRotate(this.mRotation);
                Matrix matrix6 = new Matrix();
                matrix6.setTranslate(fArr2[0] / 2.0f, fArr2[1] / 2.0f);
                Matrix matrix7 = new Matrix();
                matrix7.setRectToRect(rectF, rectF2, Matrix.ScaleToFit.FILL);
                Matrix matrix8 = new Matrix();
                matrix8.setConcat(matrix5, matrix4);
                Matrix matrix9 = new Matrix();
                matrix9.setConcat(matrix7, matrix6);
                matrix3.setConcat(matrix9, matrix8);
            }
            Bitmap createBitmap = Bitmap.createBitmap((int) rectF2.width(), (int) rectF2.height(), Bitmap.Config.ARGB_8888);
            bitmap = bitmap3;
            if (createBitmap != null) {
                Canvas canvas = new Canvas(createBitmap);
                Paint paint = new Paint();
                paint.setFilterBitmap(true);
                canvas.drawBitmap(bitmap3, matrix3, paint);
                bitmap = createBitmap;
            }
            if (this.mSaveCroppedBitmap) {
                this.mCroppedBitmap = bitmap;
            }
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(2048);
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream)) {
                z = false;
                if (this.mSetWallpaper) {
                    try {
                        byte[] byteArray = byteArrayOutputStream.toByteArray();
                        setWallpaper(new ByteArrayInputStream(byteArray), null, i);
                        z = false;
                        if (this.mOnBitmapCroppedHandler != null) {
                            this.mOnBitmapCroppedHandler.onBitmapCropped(byteArray, new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()));
                            z = false;
                        }
                    } catch (IOException e5) {
                        Log.w("BitmapCropTask", "cannot write stream to wallpaper", e5);
                        z = true;
                    }
                }
            } else {
                Log.w("BitmapCropTask", "cannot compress bitmap");
                z = true;
            }
            return !z;
        } catch (Throwable th) {
            Utils.closeSilently(null);
            throw th;
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.os.AsyncTask
    public Boolean doInBackground(Integer... numArr) {
        return Boolean.valueOf(cropBitmap(numArr.length == 0 ? 1 : numArr[0].intValue()));
    }

    public Bitmap getCroppedBitmap() {
        return this.mCroppedBitmap;
    }

    public Point getImageBounds() {
        InputStream regenerateInputStream = regenerateInputStream();
        if (regenerateInputStream != null) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(regenerateInputStream, null, options);
            Utils.closeSilently(regenerateInputStream);
            if (options.outWidth == 0 || options.outHeight == 0) {
                return null;
            }
            return new Point(options.outWidth, options.outHeight);
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.os.AsyncTask
    public void onPostExecute(Boolean bool) {
        if (!bool.booleanValue()) {
            Toast.makeText(this.mContext, 2131558549, 0).show();
        }
        if (this.mOnEndCropHandler != null) {
            this.mOnEndCropHandler.run(bool.booleanValue());
        }
    }

    public void setCropBounds(RectF rectF) {
        this.mCropBounds = rectF;
    }

    public void setNoCrop(boolean z) {
        this.mNoCrop = z;
    }

    public void setOnBitmapCropped(OnBitmapCroppedHandler onBitmapCroppedHandler) {
        this.mOnBitmapCroppedHandler = onBitmapCroppedHandler;
    }

    public void setOnEndRunnable(OnEndCropHandler onEndCropHandler) {
        this.mOnEndCropHandler = onEndCropHandler;
    }
}
