package com.android.launcher3;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import com.android.gallery3d.glrenderer.BasicTexture;
import com.android.gallery3d.glrenderer.BitmapTexture;
import com.android.photos.views.TiledImageRenderer;
/* loaded from: a.zip:com/android/launcher3/DrawableTileSource.class */
public class DrawableTileSource implements TiledImageRenderer.TileSource {
    private Drawable mDrawable;
    private BitmapTexture mPreview;
    private int mPreviewSize;
    private int mTileSize;

    public DrawableTileSource(Context context, Drawable drawable, int i) {
        this.mTileSize = TiledImageRenderer.suggestedTileSize(context);
        this.mDrawable = drawable;
        this.mPreviewSize = Math.min(i, 1024);
    }

    @Override // com.android.photos.views.TiledImageRenderer.TileSource
    public int getImageHeight() {
        return this.mDrawable.getIntrinsicHeight();
    }

    @Override // com.android.photos.views.TiledImageRenderer.TileSource
    public int getImageWidth() {
        return this.mDrawable.getIntrinsicWidth();
    }

    @Override // com.android.photos.views.TiledImageRenderer.TileSource
    public BasicTexture getPreview() {
        float f;
        if (this.mPreviewSize == 0) {
            return null;
        }
        if (this.mPreview == null) {
            float imageWidth = getImageWidth();
            float imageHeight = getImageHeight();
            while (true) {
                f = imageHeight;
                if (imageWidth <= 1024.0f && f <= 1024.0f) {
                    break;
                }
                imageWidth /= 2.0f;
                imageHeight = f / 2.0f;
            }
            Bitmap createBitmap = Bitmap.createBitmap((int) imageWidth, (int) f, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(createBitmap);
            this.mDrawable.setBounds(new Rect(0, 0, (int) imageWidth, (int) f));
            this.mDrawable.draw(canvas);
            canvas.setBitmap(null);
            this.mPreview = new BitmapTexture(createBitmap);
        }
        return this.mPreview;
    }

    @Override // com.android.photos.views.TiledImageRenderer.TileSource
    public int getRotation() {
        return 0;
    }

    @Override // com.android.photos.views.TiledImageRenderer.TileSource
    public Bitmap getTile(int i, int i2, int i3, Bitmap bitmap) {
        int tileSize = getTileSize();
        Bitmap bitmap2 = bitmap;
        if (bitmap == null) {
            bitmap2 = Bitmap.createBitmap(tileSize, tileSize, Bitmap.Config.ARGB_8888);
        }
        Canvas canvas = new Canvas(bitmap2);
        Rect rect = new Rect(0, 0, getImageWidth(), getImageHeight());
        rect.offset(-i2, -i3);
        this.mDrawable.setBounds(rect);
        this.mDrawable.draw(canvas);
        canvas.setBitmap(null);
        return bitmap2;
    }

    @Override // com.android.photos.views.TiledImageRenderer.TileSource
    public int getTileSize() {
        return this.mTileSize;
    }
}
