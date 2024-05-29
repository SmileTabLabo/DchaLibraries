package com.android.gallery3d.glrenderer;

import android.graphics.Bitmap;
import android.opengl.GLUtils;
import com.android.gallery3d.common.Utils;
import java.util.HashMap;
/* loaded from: a.zip:com/android/gallery3d/glrenderer/UploadedTexture.class */
public abstract class UploadedTexture extends BasicTexture {
    private static int sUploadedCount;
    protected Bitmap mBitmap;
    private int mBorder;
    private boolean mContentValid;
    private boolean mIsUploading;
    private boolean mOpaque;
    private boolean mThrottled;
    private static HashMap<BorderKey, Bitmap> sBorderLines = new HashMap<>();
    private static BorderKey sBorderKey = new BorderKey();

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: a.zip:com/android/gallery3d/glrenderer/UploadedTexture$BorderKey.class */
    public static class BorderKey implements Cloneable {
        public Bitmap.Config config;
        public int length;
        public boolean vertical;

        BorderKey() {
        }

        /* renamed from: clone */
        public BorderKey m153clone() {
            try {
                return (BorderKey) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new AssertionError(e);
            }
        }

        public boolean equals(Object obj) {
            if (obj instanceof BorderKey) {
                BorderKey borderKey = (BorderKey) obj;
                boolean z = false;
                if (this.vertical == borderKey.vertical) {
                    z = false;
                    if (this.config == borderKey.config) {
                        z = false;
                        if (this.length == borderKey.length) {
                            z = true;
                        }
                    }
                }
                return z;
            }
            return false;
        }

        public int hashCode() {
            int hashCode = this.config.hashCode() ^ this.length;
            if (!this.vertical) {
                hashCode = -hashCode;
            }
            return hashCode;
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public UploadedTexture() {
        this(false);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public UploadedTexture(boolean z) {
        super(null, 0, 0);
        this.mContentValid = true;
        this.mIsUploading = false;
        this.mOpaque = true;
        this.mThrottled = false;
        if (z) {
            setBorder(true);
            this.mBorder = 1;
        }
    }

    private void freeBitmap() {
        Utils.assertTrue(this.mBitmap != null);
        onFreeBitmap(this.mBitmap);
        this.mBitmap = null;
    }

    private Bitmap getBitmap() {
        if (this.mBitmap == null) {
            this.mBitmap = onGetBitmap();
            int width = this.mBitmap.getWidth();
            int i = this.mBorder;
            int height = this.mBitmap.getHeight();
            int i2 = this.mBorder;
            if (this.mWidth == -1) {
                setSize(width + (i * 2), height + (i2 * 2));
            }
        }
        return this.mBitmap;
    }

    private static Bitmap getBorderLine(boolean z, Bitmap.Config config, int i) {
        BorderKey borderKey = sBorderKey;
        borderKey.vertical = z;
        borderKey.config = config;
        borderKey.length = i;
        Bitmap bitmap = sBorderLines.get(borderKey);
        Bitmap bitmap2 = bitmap;
        if (bitmap == null) {
            Bitmap createBitmap = z ? Bitmap.createBitmap(1, i, config) : Bitmap.createBitmap(i, 1, config);
            sBorderLines.put(borderKey.m153clone(), createBitmap);
            bitmap2 = createBitmap;
        }
        return bitmap2;
    }

    private void uploadToCanvas(GLCanvas gLCanvas) {
        Bitmap bitmap = getBitmap();
        if (bitmap == null) {
            this.mState = -1;
            throw new RuntimeException("Texture load fail, no bitmap");
        }
        try {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int i = this.mBorder;
            int i2 = this.mBorder;
            int textureWidth = getTextureWidth();
            int textureHeight = getTextureHeight();
            Utils.assertTrue(width <= textureWidth && height <= textureHeight);
            this.mId = gLCanvas.getGLId().generateTexture();
            gLCanvas.setTextureParameters(this);
            if (width == textureWidth && height == textureHeight) {
                gLCanvas.initializeTexture(this, bitmap);
            } else {
                int internalFormat = GLUtils.getInternalFormat(bitmap);
                int type = GLUtils.getType(bitmap);
                Bitmap.Config config = bitmap.getConfig();
                gLCanvas.initializeTextureSize(this, internalFormat, type);
                gLCanvas.texSubImage2D(this, this.mBorder, this.mBorder, bitmap, internalFormat, type);
                if (this.mBorder > 0) {
                    gLCanvas.texSubImage2D(this, 0, 0, getBorderLine(true, config, textureHeight), internalFormat, type);
                    gLCanvas.texSubImage2D(this, 0, 0, getBorderLine(false, config, textureWidth), internalFormat, type);
                }
                if (this.mBorder + width < textureWidth) {
                    gLCanvas.texSubImage2D(this, this.mBorder + width, 0, getBorderLine(true, config, textureHeight), internalFormat, type);
                }
                if (this.mBorder + height < textureHeight) {
                    gLCanvas.texSubImage2D(this, 0, this.mBorder + height, getBorderLine(false, config, textureWidth), internalFormat, type);
                }
            }
            freeBitmap();
            setAssociatedCanvas(gLCanvas);
            this.mState = 1;
            this.mContentValid = true;
        } catch (Throwable th) {
            freeBitmap();
            throw th;
        }
    }

    @Override // com.android.gallery3d.glrenderer.BasicTexture
    public int getHeight() {
        if (this.mWidth == -1) {
            getBitmap();
        }
        return this.mHeight;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.gallery3d.glrenderer.BasicTexture
    public int getTarget() {
        return 3553;
    }

    @Override // com.android.gallery3d.glrenderer.BasicTexture
    public int getWidth() {
        if (this.mWidth == -1) {
            getBitmap();
        }
        return this.mWidth;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void invalidateContent() {
        if (this.mBitmap != null) {
            freeBitmap();
        }
        this.mContentValid = false;
        this.mWidth = -1;
        this.mHeight = -1;
    }

    public boolean isContentValid() {
        return isLoaded() ? this.mContentValid : false;
    }

    @Override // com.android.gallery3d.glrenderer.Texture
    public boolean isOpaque() {
        return this.mOpaque;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.gallery3d.glrenderer.BasicTexture
    public boolean onBind(GLCanvas gLCanvas) {
        updateContent(gLCanvas);
        return isContentValid();
    }

    protected abstract void onFreeBitmap(Bitmap bitmap);

    protected abstract Bitmap onGetBitmap();

    @Override // com.android.gallery3d.glrenderer.BasicTexture
    public void recycle() {
        super.recycle();
        if (this.mBitmap != null) {
            freeBitmap();
        }
    }

    public void updateContent(GLCanvas gLCanvas) {
        if (!isLoaded()) {
            if (this.mThrottled) {
                int i = sUploadedCount + 1;
                sUploadedCount = i;
                if (i > 100) {
                    return;
                }
            }
            uploadToCanvas(gLCanvas);
        } else if (this.mContentValid) {
        } else {
            Bitmap bitmap = getBitmap();
            gLCanvas.texSubImage2D(this, this.mBorder, this.mBorder, bitmap, GLUtils.getInternalFormat(bitmap), GLUtils.getType(bitmap));
            freeBitmap();
            this.mContentValid = true;
        }
    }
}
