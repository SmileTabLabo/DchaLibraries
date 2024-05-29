package com.android.photos;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.opengl.GLUtils;
import android.util.Log;
import com.android.gallery3d.common.BitmapUtils;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.exif.ExifInterface;
import com.android.gallery3d.glrenderer.BasicTexture;
import com.android.gallery3d.glrenderer.BitmapTexture;
import com.android.photos.views.TiledImageRenderer;
import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
@TargetApi(15)
/* loaded from: a.zip:com/android/photos/BitmapRegionTileSource.class */
public class BitmapRegionTileSource implements TiledImageRenderer.TileSource {
    SimpleBitmapRegionDecoder mDecoder;
    int mHeight;
    private BitmapFactory.Options mOptions;
    private BasicTexture mPreview;
    private final int mRotation;
    int mTileSize;
    private Rect mWantRegion = new Rect();
    int mWidth;

    /* loaded from: a.zip:com/android/photos/BitmapRegionTileSource$BitmapSource.class */
    public static abstract class BitmapSource {
        private SimpleBitmapRegionDecoder mDecoder;
        private Bitmap mPreview;
        private int mRotation;
        private State mState = State.NOT_LOADED;

        /* loaded from: a.zip:com/android/photos/BitmapRegionTileSource$BitmapSource$InBitmapProvider.class */
        public interface InBitmapProvider {
            Bitmap forPixelCount(int i);
        }

        /* loaded from: a.zip:com/android/photos/BitmapRegionTileSource$BitmapSource$State.class */
        public enum State {
            NOT_LOADED,
            LOADED,
            ERROR_LOADING;

            /* renamed from: values  reason: to resolve conflict with enum method */
            public static State[] valuesCustom() {
                return values();
            }
        }

        private static Bitmap ensureGLCompatibleBitmap(Bitmap bitmap) {
            if (bitmap == null || bitmap.getConfig() != null) {
                return bitmap;
            }
            Bitmap copy = bitmap.copy(Bitmap.Config.ARGB_8888, false);
            bitmap.recycle();
            return copy;
        }

        public Bitmap decodePreview(Bitmap bitmap, int i) {
            Bitmap bitmap2 = bitmap;
            if (bitmap == null) {
                return null;
            }
            float max = i / Math.max(bitmap.getWidth(), bitmap.getHeight());
            if (max <= 0.5d) {
                bitmap2 = BitmapUtils.resizeBitmapByScale(bitmap, max, true);
            }
            return ensureGLCompatibleBitmap(bitmap2);
        }

        public SimpleBitmapRegionDecoder getBitmapRegionDecoder() {
            return this.mDecoder;
        }

        public State getLoadingState() {
            return this.mState;
        }

        public Bitmap getPreviewBitmap() {
            return this.mPreview;
        }

        public int getRotation() {
            return this.mRotation;
        }

        public abstract SimpleBitmapRegionDecoder loadBitmapRegionDecoder();

        public boolean loadInBackground(InBitmapProvider inBitmapProvider) {
            Bitmap forPixelCount;
            Integer tagIntValue;
            boolean z = true;
            ExifInterface exifInterface = new ExifInterface();
            if (readExif(exifInterface) && (tagIntValue = exifInterface.getTagIntValue(ExifInterface.TAG_ORIENTATION)) != null) {
                this.mRotation = ExifInterface.getRotationForOrientationValue(tagIntValue.shortValue());
            }
            this.mDecoder = loadBitmapRegionDecoder();
            if (this.mDecoder == null) {
                this.mState = State.ERROR_LOADING;
                return false;
            }
            int width = this.mDecoder.getWidth();
            int height = this.mDecoder.getHeight();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            options.inPreferQualityOverSpeed = true;
            options.inSampleSize = BitmapUtils.computeSampleSizeLarger(1024.0f / Math.max(width, height));
            options.inJustDecodeBounds = false;
            options.inMutable = true;
            if (inBitmapProvider != null && (forPixelCount = inBitmapProvider.forPixelCount((width / options.inSampleSize) * (height / options.inSampleSize))) != null) {
                options.inBitmap = forPixelCount;
                try {
                    this.mPreview = loadPreviewBitmap(options);
                } catch (IllegalArgumentException e) {
                    Log.d("BitmapRegionTileSource", "Unable to reuse bitmap", e);
                    options.inBitmap = null;
                    this.mPreview = null;
                }
            }
            if (this.mPreview == null) {
                this.mPreview = loadPreviewBitmap(options);
            }
            if (this.mPreview == null) {
                this.mState = State.ERROR_LOADING;
                return false;
            }
            if (this.mPreview != null) {
                this.mPreview = decodePreview(this.mPreview, 1024);
            }
            try {
                GLUtils.getInternalFormat(this.mPreview);
                GLUtils.getType(this.mPreview);
                this.mState = State.LOADED;
            } catch (IllegalArgumentException e2) {
                Log.d("BitmapRegionTileSource", "Image cannot be rendered on a GL surface", e2);
                this.mState = State.ERROR_LOADING;
            }
            if (this.mState != State.LOADED) {
                z = false;
            }
            return z;
        }

        public abstract Bitmap loadPreviewBitmap(BitmapFactory.Options options);

        public abstract boolean readExif(ExifInterface exifInterface);
    }

    /* loaded from: a.zip:com/android/photos/BitmapRegionTileSource$ResourceBitmapSource.class */
    public static class ResourceBitmapSource extends BitmapSource {
        private Resources mRes;
        private int mResId;

        public ResourceBitmapSource(Resources resources, int i) {
            this.mRes = resources;
            this.mResId = i;
        }

        private InputStream regenerateInputStream() {
            return new BufferedInputStream(this.mRes.openRawResource(this.mResId));
        }

        @Override // com.android.photos.BitmapRegionTileSource.BitmapSource
        public SimpleBitmapRegionDecoder loadBitmapRegionDecoder() {
            InputStream regenerateInputStream = regenerateInputStream();
            SimpleBitmapRegionDecoder newInstance = SimpleBitmapRegionDecoderWrapper.newInstance(regenerateInputStream, false);
            Utils.closeSilently(regenerateInputStream);
            SimpleBitmapRegionDecoder simpleBitmapRegionDecoder = newInstance;
            if (newInstance == null) {
                InputStream regenerateInputStream2 = regenerateInputStream();
                simpleBitmapRegionDecoder = DumbBitmapRegionDecoder.newInstance(regenerateInputStream2);
                Utils.closeSilently(regenerateInputStream2);
            }
            return simpleBitmapRegionDecoder;
        }

        @Override // com.android.photos.BitmapRegionTileSource.BitmapSource
        public Bitmap loadPreviewBitmap(BitmapFactory.Options options) {
            return BitmapFactory.decodeResource(this.mRes, this.mResId, options);
        }

        @Override // com.android.photos.BitmapRegionTileSource.BitmapSource
        public boolean readExif(ExifInterface exifInterface) {
            try {
                InputStream regenerateInputStream = regenerateInputStream();
                exifInterface.readExif(regenerateInputStream);
                Utils.closeSilently(regenerateInputStream);
                return true;
            } catch (IOException e) {
                Log.e("BitmapRegionTileSource", "Error reading resource", e);
                return false;
            }
        }
    }

    /* loaded from: a.zip:com/android/photos/BitmapRegionTileSource$UriBitmapSource.class */
    public static class UriBitmapSource extends BitmapSource {
        private Context mContext;
        private Uri mUri;

        public UriBitmapSource(Context context, Uri uri) {
            this.mContext = context;
            this.mUri = uri;
        }

        private InputStream regenerateInputStream() throws FileNotFoundException {
            return new BufferedInputStream(this.mContext.getContentResolver().openInputStream(this.mUri));
        }

        @Override // com.android.photos.BitmapRegionTileSource.BitmapSource
        public SimpleBitmapRegionDecoder loadBitmapRegionDecoder() {
            try {
                InputStream regenerateInputStream = regenerateInputStream();
                SimpleBitmapRegionDecoder newInstance = SimpleBitmapRegionDecoderWrapper.newInstance(regenerateInputStream, false);
                Utils.closeSilently(regenerateInputStream);
                SimpleBitmapRegionDecoder simpleBitmapRegionDecoder = newInstance;
                if (newInstance == null) {
                    InputStream regenerateInputStream2 = regenerateInputStream();
                    simpleBitmapRegionDecoder = DumbBitmapRegionDecoder.newInstance(regenerateInputStream2);
                    Utils.closeSilently(regenerateInputStream2);
                }
                return simpleBitmapRegionDecoder;
            } catch (FileNotFoundException e) {
                Log.e("BitmapRegionTileSource", "Failed to load URI " + this.mUri, e);
                return null;
            }
        }

        @Override // com.android.photos.BitmapRegionTileSource.BitmapSource
        public Bitmap loadPreviewBitmap(BitmapFactory.Options options) {
            try {
                InputStream regenerateInputStream = regenerateInputStream();
                Bitmap decodeStream = BitmapFactory.decodeStream(regenerateInputStream, null, options);
                Utils.closeSilently(regenerateInputStream);
                return decodeStream;
            } catch (FileNotFoundException | OutOfMemoryError e) {
                Log.e("BitmapRegionTileSource", "Failed to load URI " + this.mUri, e);
                return null;
            }
        }

        @Override // com.android.photos.BitmapRegionTileSource.BitmapSource
        public boolean readExif(ExifInterface exifInterface) {
            InputStream inputStream = null;
            InputStream inputStream2 = null;
            InputStream inputStream3 = null;
            InputStream inputStream4 = null;
            try {
                try {
                    try {
                        InputStream regenerateInputStream = regenerateInputStream();
                        exifInterface.readExif(regenerateInputStream);
                        inputStream4 = regenerateInputStream;
                        inputStream = regenerateInputStream;
                        inputStream2 = regenerateInputStream;
                        inputStream3 = regenerateInputStream;
                        Utils.closeSilently(regenerateInputStream);
                        Utils.closeSilently(regenerateInputStream);
                        return true;
                    } catch (IOException e) {
                        InputStream inputStream5 = inputStream;
                        InputStream inputStream6 = inputStream;
                        Log.d("BitmapRegionTileSource", "Failed to load URI " + this.mUri, e);
                        Utils.closeSilently(inputStream);
                        return false;
                    }
                } catch (FileNotFoundException e2) {
                    InputStream inputStream7 = inputStream2;
                    InputStream inputStream8 = inputStream2;
                    Log.d("BitmapRegionTileSource", "Failed to load URI " + this.mUri, e2);
                    Utils.closeSilently(inputStream2);
                    return false;
                } catch (NullPointerException e3) {
                    InputStream inputStream9 = inputStream4;
                    InputStream inputStream10 = inputStream4;
                    Log.d("BitmapRegionTileSource", "Failed to read EXIF for URI " + this.mUri, e3);
                    Utils.closeSilently(inputStream4);
                    return false;
                }
            } catch (Throwable th) {
                Utils.closeSilently(inputStream3);
                throw th;
            }
        }
    }

    public BitmapRegionTileSource(Context context, BitmapSource bitmapSource, byte[] bArr) {
        int i = -1;
        this.mTileSize = TiledImageRenderer.suggestedTileSize(context);
        this.mRotation = bitmapSource.getRotation();
        this.mDecoder = bitmapSource.getBitmapRegionDecoder();
        if (this.mDecoder != null) {
            this.mWidth = this.mDecoder.getWidth();
            this.mHeight = this.mDecoder.getHeight();
            this.mOptions = new BitmapFactory.Options();
            this.mOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
            this.mOptions.inPreferQualityOverSpeed = true;
            this.mOptions.inTempStorage = bArr;
            Bitmap previewBitmap = bitmapSource.getPreviewBitmap();
            if (previewBitmap == null || previewBitmap.getWidth() > 2048 || previewBitmap.getHeight() > 2048) {
                Log.w("BitmapRegionTileSource", String.format("Failed to create preview of apropriate size!  in: %dx%d, out: %dx%d", Integer.valueOf(this.mWidth), Integer.valueOf(this.mHeight), Integer.valueOf(previewBitmap == null ? -1 : previewBitmap.getWidth()), Integer.valueOf(previewBitmap != null ? previewBitmap.getHeight() : i)));
                return;
            }
            this.mPreview = new BitmapTexture(previewBitmap);
            if (this.mWidth == 0) {
                this.mWidth = previewBitmap.getWidth();
            }
            if (this.mHeight == 0) {
                this.mHeight = previewBitmap.getHeight();
            }
        }
    }

    public Bitmap getBitmap() {
        return this.mPreview instanceof BitmapTexture ? ((BitmapTexture) this.mPreview).getBitmap() : null;
    }

    @Override // com.android.photos.views.TiledImageRenderer.TileSource
    public int getImageHeight() {
        return this.mHeight;
    }

    @Override // com.android.photos.views.TiledImageRenderer.TileSource
    public int getImageWidth() {
        return this.mWidth;
    }

    @Override // com.android.photos.views.TiledImageRenderer.TileSource
    public BasicTexture getPreview() {
        return this.mPreview;
    }

    @Override // com.android.photos.views.TiledImageRenderer.TileSource
    public int getRotation() {
        return this.mRotation;
    }

    @Override // com.android.photos.views.TiledImageRenderer.TileSource
    public Bitmap getTile(int i, int i2, int i3, Bitmap bitmap) {
        int tileSize = getTileSize();
        int i4 = tileSize << i;
        this.mWantRegion.set(i2, i3, i2 + i4, i3 + i4);
        Bitmap bitmap2 = bitmap;
        if (bitmap == null) {
            bitmap2 = Bitmap.createBitmap(tileSize, tileSize, Bitmap.Config.ARGB_8888);
        }
        this.mOptions.inSampleSize = 1 << i;
        this.mOptions.inBitmap = bitmap2;
        try {
            Bitmap decodeRegion = this.mDecoder.decodeRegion(this.mWantRegion, this.mOptions);
            if (this.mOptions.inBitmap != decodeRegion && this.mOptions.inBitmap != null) {
                this.mOptions.inBitmap = null;
            }
            if (decodeRegion == null) {
                Log.w("BitmapRegionTileSource", "fail in decoding region");
            }
            return decodeRegion;
        } catch (Throwable th) {
            if (this.mOptions.inBitmap != bitmap2 && this.mOptions.inBitmap != null) {
                this.mOptions.inBitmap = null;
            }
            throw th;
        }
    }

    @Override // com.android.photos.views.TiledImageRenderer.TileSource
    public int getTileSize() {
        return this.mTileSize;
    }
}
