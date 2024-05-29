package com.android.launcher3;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.WallpaperManager;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Toast;
import com.android.gallery3d.common.BitmapCropTask;
import com.android.gallery3d.common.BitmapUtils;
import com.android.gallery3d.common.Utils;
import com.android.launcher3.base.BaseActivity;
import com.android.launcher3.util.WallpaperUtils;
import com.android.photos.BitmapRegionTileSource;
import com.android.photos.views.TiledImageRenderer;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
/* loaded from: a.zip:com/android/launcher3/WallpaperCropActivity.class */
public class WallpaperCropActivity extends BaseActivity implements Handler.Callback {
    protected CropView mCropView;
    LoadRequest mCurrentLoadRequest;
    private Handler mLoaderHandler;
    private HandlerThread mLoaderThread;
    protected View mProgressView;
    protected View mSetWallpaperButton;
    private byte[] mTempStorageForDecoding = new byte[16384];
    Set<Bitmap> mReusableBitmaps = Collections.newSetFromMap(new WeakHashMap());
    private final DialogInterface.OnCancelListener mOnDialogCancelListener = new DialogInterface.OnCancelListener(this) { // from class: com.android.launcher3.WallpaperCropActivity.1
        final WallpaperCropActivity this$0;

        {
            this.this$0 = this;
        }

        @Override // android.content.DialogInterface.OnCancelListener
        public void onCancel(DialogInterface dialogInterface) {
            this.this$0.getActionBar().show();
            View findViewById = this.this$0.findViewById(2131296318);
            if (findViewById != null) {
                findViewById.setVisibility(0);
            }
            if (this.this$0.mProgressView != null) {
                this.this$0.mProgressView.setVisibility(8);
            }
        }
    };

    /* loaded from: a.zip:com/android/launcher3/WallpaperCropActivity$CropViewScaleAndOffsetProvider.class */
    public static class CropViewScaleAndOffsetProvider {
        public float getParallaxOffset() {
            return 0.5f;
        }

        public float getScale(Point point, RectF rectF) {
            return 1.0f;
        }

        public void updateCropView(WallpaperCropActivity wallpaperCropActivity, TiledImageRenderer.TileSource tileSource) {
            Point defaultWallpaperSize = WallpaperUtils.getDefaultWallpaperSize(wallpaperCropActivity.getResources(), wallpaperCropActivity.getWindowManager());
            RectF maxCropRect = Utils.getMaxCropRect(tileSource.getImageWidth(), tileSource.getImageHeight(), defaultWallpaperSize.x, defaultWallpaperSize.y, false);
            float scale = getScale(defaultWallpaperSize, maxCropRect);
            PointF center = wallpaperCropActivity.mCropView.getCenter();
            float max = Math.max(0.0f, Math.min(getParallaxOffset(), 1.0f));
            float width = wallpaperCropActivity.mCropView.getWidth() / scale;
            center.x = (width / 2.0f) + ((maxCropRect.width() - width) * max) + maxCropRect.left;
            wallpaperCropActivity.mCropView.setScaleAndCenter(scale, center.x, center.y);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: a.zip:com/android/launcher3/WallpaperCropActivity$LoadRequest.class */
    public static class LoadRequest {
        boolean moveToLeft;
        Runnable postExecute;
        TiledImageRenderer.TileSource result;
        CropViewScaleAndOffsetProvider scaleAndOffsetProvider;
        BitmapRegionTileSource.BitmapSource src;
        boolean touchEnabled;
    }

    void addReusableBitmap(TiledImageRenderer.TileSource tileSource) {
        Bitmap bitmap;
        synchronized (this.mReusableBitmaps) {
            if (Utilities.ATLEAST_KITKAT && (tileSource instanceof BitmapRegionTileSource) && (bitmap = ((BitmapRegionTileSource) tileSource).getBitmap()) != null && bitmap.isMutable()) {
                this.mReusableBitmaps.add(bitmap);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void cropImageAndSetWallpaper(Resources resources, int i, boolean z, boolean z2) {
        int rotationFromExif = BitmapUtils.getRotationFromExif(resources, i);
        Point sourceDimensions = this.mCropView.getSourceDimensions();
        Point defaultWallpaperSize = WallpaperUtils.getDefaultWallpaperSize(getResources(), getWindowManager());
        NycWallpaperUtils.executeCropTaskAfterPrompt(this, new BitmapCropTask(getContext(), resources, i, Utils.getMaxCropRect(sourceDimensions.x, sourceDimensions.y, defaultWallpaperSize.x, defaultWallpaperSize.y, false), rotationFromExif, defaultWallpaperSize.x, defaultWallpaperSize.y, true, false, new BitmapCropTask.OnEndCropHandler(this, z, z2) { // from class: com.android.launcher3.WallpaperCropActivity.8
            final WallpaperCropActivity this$0;
            final boolean val$finishActivityWhenDone;
            final boolean val$shouldFadeOutOnFinish;

            {
                this.this$0 = this;
                this.val$finishActivityWhenDone = z;
                this.val$shouldFadeOutOnFinish = z2;
            }

            @Override // com.android.gallery3d.common.BitmapCropTask.OnEndCropHandler
            public void run(boolean z3) {
                this.this$0.updateWallpaperDimensions(0, 0);
                if (this.val$finishActivityWhenDone) {
                    this.this$0.setResult(-1);
                    this.this$0.finish();
                    if (z3 && this.val$shouldFadeOutOnFinish) {
                        this.this$0.overridePendingTransition(0, 2131034112);
                    }
                }
            }
        }), getOnDialogCancelListener());
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @TargetApi(17)
    public void cropImageAndSetWallpaper(Uri uri, BitmapCropTask.OnBitmapCroppedHandler onBitmapCroppedHandler, boolean z, boolean z2) {
        this.mProgressView.setVisibility(0);
        boolean z3 = getResources().getBoolean(2131492872);
        boolean z4 = this.mCropView.getLayoutDirection() == 0;
        Display defaultDisplay = getWindowManager().getDefaultDisplay();
        Point point = new Point();
        defaultDisplay.getSize(point);
        boolean z5 = point.x < point.y;
        Point defaultWallpaperSize = WallpaperUtils.getDefaultWallpaperSize(getResources(), getWindowManager());
        RectF crop = this.mCropView.getCrop();
        Point sourceDimensions = this.mCropView.getSourceDimensions();
        int imageRotation = this.mCropView.getImageRotation();
        float width = this.mCropView.getWidth() / crop.width();
        Matrix matrix = new Matrix();
        matrix.setRotate(imageRotation);
        float[] fArr = {sourceDimensions.x, sourceDimensions.y};
        matrix.mapPoints(fArr);
        fArr[0] = Math.abs(fArr[0]);
        fArr[1] = Math.abs(fArr[1]);
        crop.left = Math.max(0.0f, crop.left);
        crop.right = Math.min(fArr[0], crop.right);
        crop.top = Math.max(0.0f, crop.top);
        crop.bottom = Math.min(fArr[1], crop.bottom);
        float min = Math.min(z3 ? 2.0f * Math.min(fArr[0] - crop.right, crop.left) : z4 ? fArr[0] - crop.right : crop.left, (defaultWallpaperSize.x / width) - crop.width());
        if (z3) {
            crop.left -= min / 2.0f;
            crop.right += min / 2.0f;
        } else if (z4) {
            crop.right += min;
        } else {
            crop.left -= min;
        }
        if (z5) {
            crop.bottom = crop.top + (defaultWallpaperSize.y / width);
        } else {
            float min2 = Math.min(Math.min(fArr[1] - crop.bottom, crop.top), ((defaultWallpaperSize.y / width) - crop.height()) / 2.0f);
            crop.top -= min2;
            crop.bottom += min2;
        }
        int round = Math.round(crop.width() * width);
        int round2 = Math.round(crop.height() * width);
        BitmapCropTask bitmapCropTask = new BitmapCropTask(getContext(), uri, crop, imageRotation, round, round2, true, false, new BitmapCropTask.OnEndCropHandler(this, round, round2, z, z2) { // from class: com.android.launcher3.WallpaperCropActivity.9
            final WallpaperCropActivity this$0;
            final boolean val$finishActivityWhenDone;
            final int val$outHeight;
            final int val$outWidth;
            final boolean val$shouldFadeOutOnFinish;

            {
                this.this$0 = this;
                this.val$outWidth = round;
                this.val$outHeight = round2;
                this.val$finishActivityWhenDone = z;
                this.val$shouldFadeOutOnFinish = z2;
            }

            @Override // com.android.gallery3d.common.BitmapCropTask.OnEndCropHandler
            public void run(boolean z6) {
                this.this$0.updateWallpaperDimensions(this.val$outWidth, this.val$outHeight);
                if (this.val$finishActivityWhenDone) {
                    this.this$0.setResult(-1);
                    this.this$0.finish();
                }
                if (z6 && this.val$shouldFadeOutOnFinish) {
                    this.this$0.overridePendingTransition(0, 2131034112);
                }
            }
        });
        if (onBitmapCroppedHandler != null) {
            bitmapCropTask.setOnBitmapCropped(onBitmapCroppedHandler);
        }
        NycWallpaperUtils.executeCropTaskAfterPrompt(this, bitmapCropTask, getOnDialogCancelListener());
    }

    public boolean enableRotation() {
        return getResources().getBoolean(2131492867);
    }

    public DialogInterface.OnCancelListener getOnDialogCancelListener() {
        return this.mOnDialogCancelListener;
    }

    @Override // android.os.Handler.Callback
    public boolean handleMessage(Message message) {
        if (message.what == 1) {
            LoadRequest loadRequest = (LoadRequest) message.obj;
            try {
                loadRequest.src.loadInBackground(new BitmapRegionTileSource.BitmapSource.InBitmapProvider(this) { // from class: com.android.launcher3.WallpaperCropActivity.4
                    final WallpaperCropActivity this$0;

                    {
                        this.this$0 = this;
                    }

                    @Override // com.android.photos.BitmapRegionTileSource.BitmapSource.InBitmapProvider
                    public Bitmap forPixelCount(int i) {
                        Bitmap bitmap = null;
                        synchronized (this.this$0.mReusableBitmaps) {
                            int i2 = Integer.MAX_VALUE;
                            for (Bitmap bitmap2 : this.this$0.mReusableBitmaps) {
                                int width = bitmap2.getWidth() * bitmap2.getHeight();
                                if (width >= i && width < i2) {
                                    bitmap = bitmap2;
                                    i2 = width;
                                }
                            }
                            if (bitmap != null) {
                                this.this$0.mReusableBitmaps.remove(bitmap);
                            }
                        }
                        return bitmap;
                    }
                });
                loadRequest.result = new BitmapRegionTileSource(getContext(), loadRequest.src, this.mTempStorageForDecoding);
                runOnUiThread(new Runnable(this, loadRequest) { // from class: com.android.launcher3.WallpaperCropActivity.5
                    final WallpaperCropActivity this$0;
                    final LoadRequest val$req;

                    {
                        this.this$0 = this;
                        this.val$req = loadRequest;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        if (this.val$req == this.this$0.mCurrentLoadRequest) {
                            this.this$0.onLoadRequestComplete(this.val$req, this.val$req.src.getLoadingState() == BitmapRegionTileSource.BitmapSource.State.LOADED);
                        } else {
                            this.this$0.addReusableBitmap(this.val$req.result);
                        }
                    }
                });
                return true;
            } catch (SecurityException e) {
                if (isActivityDestroyed()) {
                    return true;
                }
                throw e;
            }
        }
        return false;
    }

    protected void init() {
        setContentView(2130968603);
        this.mCropView = (CropView) findViewById(2131296316);
        this.mProgressView = findViewById(2131296317);
        Uri data = getIntent().getData();
        if (data == null) {
            Log.e("Launcher3.CropActivity", "No URI passed in intent, exiting WallpaperCropActivity");
            finish();
            return;
        }
        ActionBar actionBar = getActionBar();
        actionBar.setCustomView(2130968576);
        actionBar.getCustomView().setOnClickListener(new View.OnClickListener(this, actionBar, data) { // from class: com.android.launcher3.WallpaperCropActivity.2
            final WallpaperCropActivity this$0;
            final ActionBar val$actionBar;
            final Uri val$imageUri;

            {
                this.this$0 = this;
                this.val$actionBar = actionBar;
                this.val$imageUri = data;
            }

            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                this.val$actionBar.hide();
                this.this$0.cropImageAndSetWallpaper(this.val$imageUri, (BitmapCropTask.OnBitmapCroppedHandler) null, true, false);
            }
        });
        this.mSetWallpaperButton = findViewById(2131296272);
        BitmapRegionTileSource.UriBitmapSource uriBitmapSource = new BitmapRegionTileSource.UriBitmapSource(getContext(), data);
        this.mSetWallpaperButton.setEnabled(false);
        setCropViewTileSource(uriBitmapSource, true, false, null, new Runnable(this, uriBitmapSource) { // from class: com.android.launcher3.WallpaperCropActivity.3
            final WallpaperCropActivity this$0;
            final BitmapRegionTileSource.UriBitmapSource val$bitmapSource;

            {
                this.this$0 = this;
                this.val$bitmapSource = uriBitmapSource;
            }

            @Override // java.lang.Runnable
            public void run() {
                if (this.val$bitmapSource.getLoadingState() == BitmapRegionTileSource.BitmapSource.State.LOADED) {
                    this.this$0.mSetWallpaperButton.setEnabled(true);
                    return;
                }
                Toast.makeText(this.this$0.getContext(), 2131558548, 1).show();
                this.this$0.finish();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @TargetApi(17)
    public boolean isActivityDestroyed() {
        return Utilities.ATLEAST_JB_MR1 ? isDestroyed() : false;
    }

    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mLoaderThread = new HandlerThread("wallpaper_loader");
        this.mLoaderThread.start();
        this.mLoaderHandler = new Handler(this.mLoaderThread.getLooper(), this);
        init();
        if (enableRotation()) {
            return;
        }
        setRequestedOrientation(1);
    }

    @Override // android.app.Activity
    public void onDestroy() {
        if (this.mCropView != null) {
            this.mCropView.destroy();
        }
        if (this.mLoaderThread != null) {
            this.mLoaderThread.quit();
        }
        super.onDestroy();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void onLoadRequestComplete(LoadRequest loadRequest, boolean z) {
        this.mCurrentLoadRequest = null;
        if (z) {
            TiledImageRenderer.TileSource tileSource = this.mCropView.getTileSource();
            this.mCropView.setTileSource(loadRequest.result, null);
            this.mCropView.setTouchEnabled(loadRequest.touchEnabled);
            if (loadRequest.moveToLeft) {
                this.mCropView.moveToLeft();
            }
            if (loadRequest.scaleAndOffsetProvider != null) {
                loadRequest.scaleAndOffsetProvider.updateCropView(this, loadRequest.result);
            }
            if (tileSource != null) {
                tileSource.getPreview().yield();
            }
            addReusableBitmap(tileSource);
        }
        if (loadRequest.postExecute != null) {
            loadRequest.postExecute.run();
        }
        this.mProgressView.setVisibility(8);
    }

    public final void setCropViewTileSource(BitmapRegionTileSource.BitmapSource bitmapSource, boolean z, boolean z2, CropViewScaleAndOffsetProvider cropViewScaleAndOffsetProvider, Runnable runnable) {
        LoadRequest loadRequest = new LoadRequest();
        loadRequest.moveToLeft = z2;
        loadRequest.src = bitmapSource;
        loadRequest.touchEnabled = z;
        loadRequest.postExecute = runnable;
        loadRequest.scaleAndOffsetProvider = cropViewScaleAndOffsetProvider;
        this.mCurrentLoadRequest = loadRequest;
        this.mLoaderHandler.removeMessages(1);
        Message.obtain(this.mLoaderHandler, 1, loadRequest).sendToTarget();
        this.mProgressView.postDelayed(new Runnable(this, loadRequest) { // from class: com.android.launcher3.WallpaperCropActivity.6
            final WallpaperCropActivity this$0;
            final LoadRequest val$req;

            {
                this.this$0 = this;
                this.val$req = loadRequest;
            }

            @Override // java.lang.Runnable
            public void run() {
                if (this.this$0.mCurrentLoadRequest == this.val$req) {
                    this.this$0.mProgressView.setVisibility(0);
                }
            }
        }, 1000L);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void setWallpaper(Uri uri, boolean z, boolean z2) {
        BitmapCropTask bitmapCropTask = new BitmapCropTask(getContext(), uri, null, BitmapUtils.getRotationFromExif(getContext(), uri), 0, 0, true, false, null);
        bitmapCropTask.setOnEndRunnable(new BitmapCropTask.OnEndCropHandler(this, bitmapCropTask.getImageBounds(), z, z2) { // from class: com.android.launcher3.WallpaperCropActivity.7
            final WallpaperCropActivity this$0;
            final Point val$bounds;
            final boolean val$finishActivityWhenDone;
            final boolean val$shouldFadeOutOnFinish;

            {
                this.this$0 = this;
                this.val$bounds = r5;
                this.val$finishActivityWhenDone = z;
                this.val$shouldFadeOutOnFinish = z2;
            }

            @Override // com.android.gallery3d.common.BitmapCropTask.OnEndCropHandler
            public void run(boolean z3) {
                this.this$0.updateWallpaperDimensions(this.val$bounds.x, this.val$bounds.y);
                if (this.val$finishActivityWhenDone) {
                    this.this$0.setResult(-1);
                    this.this$0.finish();
                    if (z3 && this.val$shouldFadeOutOnFinish) {
                        this.this$0.overridePendingTransition(0, 2131034112);
                    }
                }
            }
        });
        bitmapCropTask.setNoCrop(true);
        NycWallpaperUtils.executeCropTaskAfterPrompt(this, bitmapCropTask, getOnDialogCancelListener());
    }

    protected void updateWallpaperDimensions(int i, int i2) {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("com.android.launcher3.WallpaperCropActivity", 4);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        if (i == 0 || i2 == 0) {
            edit.remove("wallpaper.width");
            edit.remove("wallpaper.height");
        } else {
            edit.putInt("wallpaper.width", i);
            edit.putInt("wallpaper.height", i2);
        }
        edit.apply();
        WallpaperUtils.suggestWallpaperDimension(getResources(), sharedPreferences, getWindowManager(), WallpaperManager.getInstance(getContext()), true);
    }
}
