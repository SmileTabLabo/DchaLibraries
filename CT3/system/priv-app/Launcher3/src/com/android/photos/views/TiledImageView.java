package com.android.photos.views;

import android.content.Context;
import android.graphics.RectF;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.Choreographer;
import android.widget.FrameLayout;
import com.android.gallery3d.glrenderer.BasicTexture;
import com.android.gallery3d.glrenderer.GLES20Canvas;
import com.android.photos.views.TiledImageRenderer;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
/* loaded from: a.zip:com/android/photos/views/TiledImageView.class */
public class TiledImageView extends FrameLayout {
    private Choreographer.FrameCallback mFrameCallback;
    private Runnable mFreeTextures;
    GLSurfaceView mGLSurfaceView;
    boolean mInvalPending;
    protected Object mLock;
    private boolean mNeedFreeTextures;
    protected ImageRendererWrapper mRenderer;
    private RectF mTempRectF;
    private float[] mValues;

    /* JADX INFO: Access modifiers changed from: protected */
    /* loaded from: a.zip:com/android/photos/views/TiledImageView$ImageRendererWrapper.class */
    public static class ImageRendererWrapper {
        public int centerX;
        public int centerY;
        TiledImageRenderer image;
        Runnable isReadyCallback;
        public int rotation;
        public float scale;
        public TiledImageRenderer.TileSource source;

        protected ImageRendererWrapper() {
        }
    }

    /* loaded from: a.zip:com/android/photos/views/TiledImageView$TileRenderer.class */
    class TileRenderer implements GLSurfaceView.Renderer {
        private GLES20Canvas mCanvas;
        final TiledImageView this$0;

        TileRenderer(TiledImageView tiledImageView) {
            this.this$0 = tiledImageView;
        }

        @Override // android.opengl.GLSurfaceView.Renderer
        public void onDrawFrame(GL10 gl10) {
            Runnable runnable;
            this.mCanvas.deleteRecycledResources();
            this.mCanvas.clearBuffer();
            synchronized (this.this$0.mLock) {
                runnable = this.this$0.mRenderer.isReadyCallback;
                this.this$0.mRenderer.image.setModel(this.this$0.mRenderer.source, this.this$0.mRenderer.rotation);
                this.this$0.mRenderer.image.setPosition(this.this$0.mRenderer.centerX, this.this$0.mRenderer.centerY, this.this$0.mRenderer.scale);
            }
            if (!this.this$0.mRenderer.image.draw(this.mCanvas) || runnable == null) {
                return;
            }
            synchronized (this.this$0.mLock) {
                if (this.this$0.mRenderer.isReadyCallback == runnable) {
                    this.this$0.mRenderer.isReadyCallback = null;
                }
            }
            if (runnable != null) {
                this.this$0.post(runnable);
            }
        }

        @Override // android.opengl.GLSurfaceView.Renderer
        public void onSurfaceChanged(GL10 gl10, int i, int i2) {
            this.mCanvas.setSize(i, i2);
            this.this$0.mRenderer.image.setViewSize(i, i2);
        }

        @Override // android.opengl.GLSurfaceView.Renderer
        public void onSurfaceCreated(GL10 gl10, EGLConfig eGLConfig) {
            this.mCanvas = new GLES20Canvas();
            BasicTexture.invalidateAllTextures();
            this.this$0.mRenderer.image.setModel(this.this$0.mRenderer.source, this.this$0.mRenderer.rotation);
        }
    }

    public TiledImageView(Context context) {
        this(context, null);
    }

    public TiledImageView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mInvalPending = false;
        this.mNeedFreeTextures = false;
        this.mValues = new float[9];
        this.mLock = new Object();
        this.mFreeTextures = new Runnable(this) { // from class: com.android.photos.views.TiledImageView.1
            final TiledImageView this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.mRenderer.image.freeTextures();
                this.this$0.mNeedFreeTextures = false;
            }
        };
        this.mTempRectF = new RectF();
        this.mRenderer = new ImageRendererWrapper();
        this.mRenderer.image = new TiledImageRenderer(this);
        this.mGLSurfaceView = new GLSurfaceView(context);
        this.mGLSurfaceView.setEGLContextClientVersion(2);
        this.mGLSurfaceView.setRenderer(new TileRenderer(this));
        this.mGLSurfaceView.setRenderMode(0);
        addView(this.mGLSurfaceView, new FrameLayout.LayoutParams(-1, -1));
    }

    private void invalOnVsync() {
        if (this.mInvalPending) {
            return;
        }
        this.mInvalPending = true;
        if (this.mFrameCallback == null) {
            this.mFrameCallback = new Choreographer.FrameCallback(this) { // from class: com.android.photos.views.TiledImageView.2
                final TiledImageView this$0;

                {
                    this.this$0 = this;
                }

                @Override // android.view.Choreographer.FrameCallback
                public void doFrame(long j) {
                    this.this$0.mInvalPending = false;
                    this.this$0.mGLSurfaceView.requestRender();
                }
            };
        }
        Choreographer.getInstance().postFrameCallback(this.mFrameCallback);
    }

    private void updateScaleIfNecessaryLocked(ImageRendererWrapper imageRendererWrapper) {
        if (imageRendererWrapper == null || imageRendererWrapper.source == null || imageRendererWrapper.scale > 0.0f || getWidth() == 0) {
            return;
        }
        imageRendererWrapper.scale = Math.min(getWidth() / imageRendererWrapper.source.getImageWidth(), getHeight() / imageRendererWrapper.source.getImageHeight());
    }

    public void destroy() {
        this.mNeedFreeTextures = true;
        this.mGLSurfaceView.queueEvent(this.mFreeTextures);
    }

    public TiledImageRenderer.TileSource getTileSource() {
        return this.mRenderer.source;
    }

    @Override // android.view.View
    public void invalidate() {
        invalOnVsync();
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mNeedFreeTextures) {
            this.mRenderer.image.freeTextures();
            this.mNeedFreeTextures = false;
        }
    }

    @Override // android.widget.FrameLayout, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        synchronized (this.mLock) {
            updateScaleIfNecessaryLocked(this.mRenderer);
        }
    }

    public void setTileSource(TiledImageRenderer.TileSource tileSource, Runnable runnable) {
        synchronized (this.mLock) {
            this.mRenderer.source = tileSource;
            this.mRenderer.isReadyCallback = runnable;
            this.mRenderer.centerX = tileSource != null ? tileSource.getImageWidth() / 2 : 0;
            this.mRenderer.centerY = tileSource != null ? tileSource.getImageHeight() / 2 : 0;
            ImageRendererWrapper imageRendererWrapper = this.mRenderer;
            int i = 0;
            if (tileSource != null) {
                i = tileSource.getRotation();
            }
            imageRendererWrapper.rotation = i;
            this.mRenderer.scale = 0.0f;
            updateScaleIfNecessaryLocked(this.mRenderer);
        }
        invalidate();
    }

    @Override // android.view.View
    public void setVisibility(int i) {
        super.setVisibility(i);
        this.mGLSurfaceView.setVisibility(i);
    }
}
