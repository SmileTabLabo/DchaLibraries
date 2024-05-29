package com.android.photos.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.util.Pools$Pool;
import android.support.v4.util.Pools$SimplePool;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.LongSparseArray;
import android.view.View;
import android.view.WindowManager;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.glrenderer.BasicTexture;
import com.android.gallery3d.glrenderer.GLCanvas;
import com.android.gallery3d.glrenderer.UploadedTexture;
/* loaded from: a.zip:com/android/photos/views/TiledImageRenderer.class */
public class TiledImageRenderer {
    static Pools$Pool<Bitmap> sTilePool = new Pools$SimplePool<T>(64) { // from class: android.support.v4.util.Pools$SynchronizedPool
        private final Object mLock = new Object();

        @Override // android.support.v4.util.Pools$SimplePool, android.support.v4.util.Pools$Pool
        public T acquire() {
            T t;
            synchronized (this.mLock) {
                t = (T) super.acquire();
            }
            return t;
        }

        @Override // android.support.v4.util.Pools$SimplePool, android.support.v4.util.Pools$Pool
        public boolean release(T t) {
            boolean release;
            synchronized (this.mLock) {
                release = super.release(t);
            }
            return release;
        }
    };
    private boolean mBackgroundTileUploaded;
    protected int mCenterX;
    protected int mCenterY;
    private boolean mLayoutTiles;
    protected int mLevelCount;
    TileSource mModel;
    private int mOffsetX;
    private int mOffsetY;
    private View mParent;
    private BasicTexture mPreview;
    private boolean mRenderComplete;
    protected int mRotation;
    protected float mScale;
    int mTileSize;
    private int mUploadQuota;
    private int mViewHeight;
    private int mViewWidth;
    int mLevel = 0;
    private final RectF mSourceRect = new RectF();
    private final RectF mTargetRect = new RectF();
    private final LongSparseArray<Tile> mActiveTiles = new LongSparseArray<>();
    final Object mQueueLock = new Object();
    private final TileQueue mRecycledQueue = new TileQueue();
    private final TileQueue mUploadQueue = new TileQueue();
    final TileQueue mDecodeQueue = new TileQueue();
    protected int mImageWidth = -1;
    protected int mImageHeight = -1;
    private final Rect mTileRange = new Rect();
    private final Rect[] mActiveRange = {new Rect(), new Rect()};
    private TileDecoder mTileDecoder = new TileDecoder(this);

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/photos/views/TiledImageRenderer$Tile.class */
    public class Tile extends UploadedTexture {
        public Bitmap mDecodedTile;
        public Tile mNext;
        public int mTileLevel;
        public volatile int mTileState = 1;
        public int mX;
        public int mY;
        final TiledImageRenderer this$0;

        public Tile(TiledImageRenderer tiledImageRenderer, int i, int i2, int i3) {
            this.this$0 = tiledImageRenderer;
            this.mX = i;
            this.mY = i2;
            this.mTileLevel = i3;
        }

        boolean decode() {
            try {
                Bitmap acquire = TiledImageRenderer.sTilePool.acquire();
                Bitmap bitmap = acquire;
                if (acquire != null) {
                    bitmap = acquire;
                    if (acquire.getWidth() != this.this$0.mTileSize) {
                        bitmap = null;
                    }
                }
                this.mDecodedTile = this.this$0.mModel.getTile(this.mTileLevel, this.mX, this.mY, bitmap);
            } catch (Throwable th) {
                Log.w("TiledImageRenderer", "fail to decode tile", th);
            }
            return this.mDecodedTile != null;
        }

        public Tile getParentTile() {
            if (this.mTileLevel + 1 == this.this$0.mLevelCount) {
                return null;
            }
            int i = this.this$0.mTileSize << (this.mTileLevel + 1);
            return this.this$0.getTile(i * (this.mX / i), i * (this.mY / i), this.mTileLevel + 1);
        }

        @Override // com.android.gallery3d.glrenderer.BasicTexture
        public int getTextureHeight() {
            return this.this$0.mTileSize;
        }

        @Override // com.android.gallery3d.glrenderer.BasicTexture
        public int getTextureWidth() {
            return this.this$0.mTileSize;
        }

        @Override // com.android.gallery3d.glrenderer.UploadedTexture
        protected void onFreeBitmap(Bitmap bitmap) {
            TiledImageRenderer.sTilePool.release(bitmap);
        }

        @Override // com.android.gallery3d.glrenderer.UploadedTexture
        protected Bitmap onGetBitmap() {
            Utils.assertTrue(this.mTileState == 8);
            int i = this.this$0.mImageWidth;
            int i2 = this.mX;
            setSize(Math.min(this.this$0.mTileSize, (i - i2) >> this.mTileLevel), Math.min(this.this$0.mTileSize, (this.this$0.mImageHeight - this.mY) >> this.mTileLevel));
            Bitmap bitmap = this.mDecodedTile;
            this.mDecodedTile = null;
            this.mTileState = 1;
            return bitmap;
        }

        public String toString() {
            return String.format("tile(%s, %s, %s / %s)", Integer.valueOf(this.mX / this.this$0.mTileSize), Integer.valueOf(this.mY / this.this$0.mTileSize), Integer.valueOf(this.this$0.mLevel), Integer.valueOf(this.this$0.mLevelCount));
        }

        public void update(int i, int i2, int i3) {
            this.mX = i;
            this.mY = i2;
            this.mTileLevel = i3;
            invalidateContent();
        }
    }

    /* loaded from: a.zip:com/android/photos/views/TiledImageRenderer$TileDecoder.class */
    class TileDecoder extends Thread {
        final TiledImageRenderer this$0;

        TileDecoder(TiledImageRenderer tiledImageRenderer) {
            this.this$0 = tiledImageRenderer;
        }

        private Tile waitForTile() throws InterruptedException {
            Tile pop;
            synchronized (this.this$0.mQueueLock) {
                while (true) {
                    pop = this.this$0.mDecodeQueue.pop();
                    if (pop == null) {
                        this.this$0.mQueueLock.wait();
                    }
                }
            }
            return pop;
        }

        public void finishAndWait() {
            interrupt();
            try {
                join();
            } catch (InterruptedException e) {
                Log.w("TiledImageRenderer", "Interrupted while waiting for TileDecoder thread to finish!");
            }
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            while (!isInterrupted()) {
                try {
                    this.this$0.decodeTile(waitForTile());
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: a.zip:com/android/photos/views/TiledImageRenderer$TileQueue.class */
    public static class TileQueue {
        private Tile mHead;

        TileQueue() {
        }

        private boolean contains(Tile tile) {
            Tile tile2 = this.mHead;
            while (true) {
                Tile tile3 = tile2;
                if (tile3 == null) {
                    return false;
                }
                if (tile3 == tile) {
                    return true;
                }
                tile2 = tile3.mNext;
            }
        }

        public void clean() {
            this.mHead = null;
        }

        public Tile pop() {
            Tile tile = this.mHead;
            if (tile != null) {
                this.mHead = tile.mNext;
            }
            return tile;
        }

        public boolean push(Tile tile) {
            if (contains(tile)) {
                Log.w("TiledImageRenderer", "Attempting to add a tile already in the queue!");
                return false;
            }
            boolean z = this.mHead == null;
            tile.mNext = this.mHead;
            this.mHead = tile;
            return z;
        }
    }

    /* loaded from: a.zip:com/android/photos/views/TiledImageRenderer$TileSource.class */
    public interface TileSource {
        int getImageHeight();

        int getImageWidth();

        BasicTexture getPreview();

        int getRotation();

        Bitmap getTile(int i, int i2, int i3, Bitmap bitmap);

        int getTileSize();
    }

    public TiledImageRenderer(View view) {
        this.mParent = view;
        this.mTileDecoder.start();
    }

    private void activateTile(int i, int i2, int i3) {
        long makeTileKey = makeTileKey(i, i2, i3);
        Tile tile = this.mActiveTiles.get(makeTileKey);
        if (tile == null) {
            this.mActiveTiles.put(makeTileKey, obtainTile(i, i2, i3));
        } else if (tile.mTileState == 2) {
            tile.mTileState = 1;
        }
    }

    private void calculateLevelCount() {
        if (this.mPreview != null) {
            this.mLevelCount = Math.max(0, Utils.ceilLog2(this.mImageWidth / this.mPreview.getWidth()));
            return;
        }
        int i = 1;
        int max = Math.max(this.mImageWidth, this.mImageHeight);
        int i2 = this.mTileSize;
        while (i2 < max) {
            i2 <<= 1;
            i++;
        }
        this.mLevelCount = i;
    }

    private void drawTile(GLCanvas gLCanvas, int i, int i2, int i3, float f, float f2, float f3) {
        RectF rectF = this.mSourceRect;
        RectF rectF2 = this.mTargetRect;
        rectF2.set(f, f2, f + f3, f2 + f3);
        rectF.set(0.0f, 0.0f, this.mTileSize, this.mTileSize);
        Tile tile = getTile(i, i2, i3);
        if (tile != null) {
            if (!tile.isContentValid()) {
                if (tile.mTileState == 8) {
                    if (this.mUploadQuota > 0) {
                        this.mUploadQuota--;
                        tile.updateContent(gLCanvas);
                    } else {
                        this.mRenderComplete = false;
                    }
                } else if (tile.mTileState != 16) {
                    this.mRenderComplete = false;
                    queueForDecode(tile);
                }
            }
            if (drawTile(tile, gLCanvas, rectF, rectF2)) {
                return;
            }
        }
        if (this.mPreview != null) {
            int i4 = this.mTileSize << i3;
            float width = this.mPreview.getWidth() / this.mImageWidth;
            float height = this.mPreview.getHeight() / this.mImageHeight;
            rectF.set(i * width, i2 * height, (i + i4) * width, (i2 + i4) * height);
            gLCanvas.drawTexture(this.mPreview, rectF, rectF2);
        }
    }

    private boolean drawTile(Tile tile, GLCanvas gLCanvas, RectF rectF, RectF rectF2) {
        while (!tile.isContentValid()) {
            Tile parentTile = tile.getParentTile();
            if (parentTile == null) {
                return false;
            }
            if (tile.mX == parentTile.mX) {
                rectF.left /= 2.0f;
                rectF.right /= 2.0f;
            } else {
                rectF.left = (this.mTileSize + rectF.left) / 2.0f;
                rectF.right = (this.mTileSize + rectF.right) / 2.0f;
            }
            if (tile.mY == parentTile.mY) {
                rectF.top /= 2.0f;
                rectF.bottom /= 2.0f;
            } else {
                rectF.top = (this.mTileSize + rectF.top) / 2.0f;
                rectF.bottom = (this.mTileSize + rectF.bottom) / 2.0f;
            }
            tile = parentTile;
        }
        gLCanvas.drawTexture(tile, rectF, rectF2);
        return true;
    }

    private void getRange(Rect rect, int i, int i2, int i3, float f, int i4) {
        double radians = Math.toRadians(-i4);
        double d = this.mViewWidth;
        double d2 = this.mViewHeight;
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        int ceil = (int) Math.ceil(Math.max(Math.abs((cos * d) - (sin * d2)), Math.abs((cos * d) + (sin * d2))));
        int ceil2 = (int) Math.ceil(Math.max(Math.abs((sin * d) + (cos * d2)), Math.abs((sin * d) - (cos * d2))));
        int floor = (int) Math.floor(i - (ceil / (2.0f * f)));
        int floor2 = (int) Math.floor(i2 - (ceil2 / (2.0f * f)));
        int ceil3 = (int) Math.ceil(floor + (ceil / f));
        int ceil4 = (int) Math.ceil(floor2 + (ceil2 / f));
        int i5 = this.mTileSize << i3;
        rect.set(Math.max(0, (floor / i5) * i5), Math.max(0, (floor2 / i5) * i5), Math.min(this.mImageWidth, ceil3), Math.min(this.mImageHeight, ceil4));
    }

    private void getRange(Rect rect, int i, int i2, int i3, int i4) {
        getRange(rect, i, i2, i3, 1.0f / (1 << (i3 + 1)), i4);
    }

    private void invalidate() {
        this.mParent.postInvalidate();
    }

    private void invalidateTiles() {
        synchronized (this.mQueueLock) {
            this.mDecodeQueue.clean();
            this.mUploadQueue.clean();
            int size = this.mActiveTiles.size();
            for (int i = 0; i < size; i++) {
                recycleTile(this.mActiveTiles.valueAt(i));
            }
            this.mActiveTiles.clear();
        }
    }

    private static boolean isHighResolution(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(displayMetrics);
        boolean z = true;
        if (displayMetrics.heightPixels <= 2048) {
            z = displayMetrics.widthPixels > 2048;
        }
        return z;
    }

    private void layoutTiles() {
        int i;
        Rect rect;
        if (this.mViewWidth == 0 || this.mViewHeight == 0 || !this.mLayoutTiles) {
            return;
        }
        this.mLayoutTiles = false;
        this.mLevel = Utils.clamp(Utils.floorLog2(1.0f / this.mScale), 0, this.mLevelCount);
        if (this.mLevel != this.mLevelCount) {
            getRange(this.mTileRange, this.mCenterX, this.mCenterY, this.mLevel, this.mScale, this.mRotation);
            this.mOffsetX = Math.round((this.mViewWidth / 2.0f) + ((rect.left - this.mCenterX) * this.mScale));
            this.mOffsetY = Math.round((this.mViewHeight / 2.0f) + ((rect.top - this.mCenterY) * this.mScale));
            i = this.mScale * ((float) (1 << this.mLevel)) > 0.75f ? this.mLevel - 1 : this.mLevel;
        } else {
            i = this.mLevel - 2;
            this.mOffsetX = Math.round((this.mViewWidth / 2.0f) - (this.mCenterX * this.mScale));
            this.mOffsetY = Math.round((this.mViewHeight / 2.0f) - (this.mCenterY * this.mScale));
        }
        int max = Math.max(0, Math.min(i, this.mLevelCount - 2));
        int min = Math.min(max + 2, this.mLevelCount);
        Rect[] rectArr = this.mActiveRange;
        for (int i2 = max; i2 < min; i2++) {
            getRange(rectArr[i2 - max], this.mCenterX, this.mCenterY, i2, this.mRotation);
        }
        if (this.mRotation % 90 != 0) {
            return;
        }
        synchronized (this.mQueueLock) {
            this.mDecodeQueue.clean();
            this.mUploadQueue.clean();
            this.mBackgroundTileUploaded = false;
            int size = this.mActiveTiles.size();
            int i3 = 0;
            while (i3 < size) {
                Tile valueAt = this.mActiveTiles.valueAt(i3);
                int i4 = valueAt.mTileLevel;
                if (i4 < max || i4 >= min || !rectArr[i4 - max].contains(valueAt.mX, valueAt.mY)) {
                    this.mActiveTiles.removeAt(i3);
                    i3--;
                    size--;
                    recycleTile(valueAt);
                }
                i3++;
            }
        }
        for (int i5 = max; i5 < min; i5++) {
            int i6 = this.mTileSize << i5;
            Rect rect2 = rectArr[i5 - max];
            int i7 = rect2.bottom;
            for (int i8 = rect2.top; i8 < i7; i8 += i6) {
                int i9 = rect2.right;
                for (int i10 = rect2.left; i10 < i9; i10 += i6) {
                    activateTile(i10, i8, i5);
                }
            }
        }
        invalidate();
    }

    private static long makeTileKey(int i, int i2, int i3) {
        return (((i << 16) | i2) << 16) | i3;
    }

    private Tile obtainTile(int i, int i2, int i3) {
        synchronized (this.mQueueLock) {
            Tile pop = this.mRecycledQueue.pop();
            if (pop == null) {
                return new Tile(this, i, i2, i3);
            }
            pop.mTileState = 1;
            pop.update(i, i2, i3);
            return pop;
        }
    }

    private void queueForDecode(Tile tile) {
        synchronized (this.mQueueLock) {
            if (tile.mTileState == 1) {
                tile.mTileState = 2;
                if (this.mDecodeQueue.push(tile)) {
                    this.mQueueLock.notifyAll();
                }
            }
        }
    }

    private void recycleTile(Tile tile) {
        synchronized (this.mQueueLock) {
            if (tile.mTileState == 4) {
                tile.mTileState = 32;
                return;
            }
            tile.mTileState = 64;
            if (tile.mDecodedTile != null) {
                sTilePool.release(tile.mDecodedTile);
                tile.mDecodedTile = null;
            }
            this.mRecycledQueue.push(tile);
        }
    }

    public static int suggestedTileSize(Context context) {
        return isHighResolution(context) ? 512 : 256;
    }

    private void uploadBackgroundTiles(GLCanvas gLCanvas) {
        this.mBackgroundTileUploaded = true;
        int size = this.mActiveTiles.size();
        for (int i = 0; i < size; i++) {
            Tile valueAt = this.mActiveTiles.valueAt(i);
            if (!valueAt.isContentValid()) {
                queueForDecode(valueAt);
            }
        }
    }

    private void uploadTiles(GLCanvas gLCanvas) {
        Tile tile;
        int i = 1;
        Tile tile2 = null;
        while (true) {
            tile = tile2;
            if (i <= 0) {
                break;
            }
            synchronized (this.mQueueLock) {
                tile = this.mUploadQueue.pop();
            }
            if (tile == null) {
                break;
            }
            tile2 = tile;
            if (!tile.isContentValid()) {
                if (tile.mTileState == 8) {
                    tile.updateContent(gLCanvas);
                    i--;
                    tile2 = tile;
                } else {
                    Log.w("TiledImageRenderer", "Tile in upload queue has invalid state: " + tile.mTileState);
                    tile2 = tile;
                }
            }
        }
        if (tile != null) {
            invalidate();
        }
    }

    void decodeTile(Tile tile) {
        synchronized (this.mQueueLock) {
            if (tile.mTileState != 2) {
                return;
            }
            tile.mTileState = 4;
            boolean decode = tile.decode();
            synchronized (this.mQueueLock) {
                if (tile.mTileState == 32) {
                    tile.mTileState = 64;
                    if (tile.mDecodedTile != null) {
                        sTilePool.release(tile.mDecodedTile);
                        tile.mDecodedTile = null;
                    }
                    this.mRecycledQueue.push(tile);
                    return;
                }
                tile.mTileState = decode ? 8 : 16;
                if (decode) {
                    this.mUploadQueue.push(tile);
                    invalidate();
                }
            }
        }
    }

    public boolean draw(GLCanvas gLCanvas) {
        int i;
        int i2;
        layoutTiles();
        uploadTiles(gLCanvas);
        this.mUploadQuota = 1;
        this.mRenderComplete = true;
        int i3 = this.mLevel;
        int i4 = this.mRotation;
        int i5 = 0;
        if (i4 != 0) {
            i5 = 2;
        }
        if (i5 != 0) {
            gLCanvas.save(i5);
            if (i4 != 0) {
                gLCanvas.translate(this.mViewWidth / 2, this.mViewHeight / 2);
                gLCanvas.rotate(i4, 0.0f, 0.0f, 1.0f);
                gLCanvas.translate(-i, -i2);
            }
        }
        try {
            if (i3 != this.mLevelCount) {
                int i6 = this.mTileSize << i3;
                float f = i6 * this.mScale;
                Rect rect = this.mTileRange;
                int i7 = rect.top;
                int i8 = 0;
                while (i7 < rect.bottom) {
                    float f2 = this.mOffsetY;
                    float f3 = i8;
                    int i9 = rect.left;
                    int i10 = 0;
                    while (i9 < rect.right) {
                        drawTile(gLCanvas, i9, i7, i3, this.mOffsetX + (i10 * f), f2 + (f3 * f), f);
                        i9 += i6;
                        i10++;
                    }
                    i7 += i6;
                    i8++;
                }
            } else if (this.mPreview != null) {
                this.mPreview.draw(gLCanvas, this.mOffsetX, this.mOffsetY, Math.round(this.mImageWidth * this.mScale), Math.round(this.mImageHeight * this.mScale));
            }
            if (!this.mRenderComplete) {
                invalidate();
            } else if (!this.mBackgroundTileUploaded) {
                uploadBackgroundTiles(gLCanvas);
            }
            return this.mRenderComplete || this.mPreview != null;
        } finally {
            if (i5 != 0) {
                gLCanvas.restore();
            }
        }
    }

    public void freeTextures() {
        this.mLayoutTiles = true;
        this.mTileDecoder.finishAndWait();
        synchronized (this.mQueueLock) {
            this.mUploadQueue.clean();
            this.mDecodeQueue.clean();
            Tile pop = this.mRecycledQueue.pop();
            while (pop != null) {
                pop.recycle();
                pop = this.mRecycledQueue.pop();
            }
        }
        int size = this.mActiveTiles.size();
        for (int i = 0; i < size; i++) {
            this.mActiveTiles.valueAt(i).recycle();
        }
        this.mActiveTiles.clear();
        this.mTileRange.set(0, 0, 0, 0);
        do {
        } while (sTilePool.acquire() != null);
    }

    Tile getTile(int i, int i2, int i3) {
        return this.mActiveTiles.get(makeTileKey(i, i2, i3));
    }

    public void notifyModelInvalidated() {
        invalidateTiles();
        if (this.mModel == null) {
            this.mImageWidth = 0;
            this.mImageHeight = 0;
            this.mLevelCount = 0;
            this.mPreview = null;
        } else {
            this.mImageWidth = this.mModel.getImageWidth();
            this.mImageHeight = this.mModel.getImageHeight();
            this.mPreview = this.mModel.getPreview();
            this.mTileSize = this.mModel.getTileSize();
            calculateLevelCount();
        }
        this.mLayoutTiles = true;
    }

    public void setModel(TileSource tileSource, int i) {
        if (this.mModel != tileSource) {
            this.mModel = tileSource;
            notifyModelInvalidated();
        }
        if (this.mRotation != i) {
            this.mRotation = i;
            this.mLayoutTiles = true;
        }
    }

    public void setPosition(int i, int i2, float f) {
        if (this.mCenterX == i && this.mCenterY == i2 && this.mScale == f) {
            return;
        }
        this.mCenterX = i;
        this.mCenterY = i2;
        this.mScale = f;
        this.mLayoutTiles = true;
    }

    public void setViewSize(int i, int i2) {
        this.mViewWidth = i;
        this.mViewHeight = i2;
    }
}
