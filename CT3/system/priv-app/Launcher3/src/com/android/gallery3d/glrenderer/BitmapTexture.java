package com.android.gallery3d.glrenderer;

import android.graphics.Bitmap;
import com.android.gallery3d.common.Utils;
/* loaded from: a.zip:com/android/gallery3d/glrenderer/BitmapTexture.class */
public class BitmapTexture extends UploadedTexture {
    protected Bitmap mContentBitmap;

    public BitmapTexture(Bitmap bitmap) {
        this(bitmap, false);
    }

    public BitmapTexture(Bitmap bitmap, boolean z) {
        super(z);
        Utils.assertTrue(bitmap != null ? !bitmap.isRecycled() : false);
        this.mContentBitmap = bitmap;
    }

    public Bitmap getBitmap() {
        return this.mContentBitmap;
    }

    @Override // com.android.gallery3d.glrenderer.UploadedTexture
    protected void onFreeBitmap(Bitmap bitmap) {
    }

    @Override // com.android.gallery3d.glrenderer.UploadedTexture
    protected Bitmap onGetBitmap() {
        return this.mContentBitmap;
    }
}
