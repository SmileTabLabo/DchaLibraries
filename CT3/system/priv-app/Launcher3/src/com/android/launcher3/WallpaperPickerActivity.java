package com.android.launcher3;

import android.animation.LayoutTransition;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Pair;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.android.gallery3d.common.BitmapCropTask;
import com.android.gallery3d.common.BitmapUtils;
import com.android.gallery3d.common.Utils;
import com.android.launcher3.CropView;
import com.android.launcher3.WallpaperCropActivity;
import com.android.photos.BitmapRegionTileSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
/* loaded from: a.zip:com/android/launcher3/WallpaperPickerActivity.class */
public class WallpaperPickerActivity extends WallpaperCropActivity {
    ActionMode mActionMode;
    ActionMode.Callback mActionModeCallback;
    boolean mIgnoreNextTap;
    View.OnLongClickListener mLongClickListener;
    private SavedWallpaperImages mSavedImages;
    View mSelectedTile;
    View.OnClickListener mThumbnailOnClickListener;
    private float mWallpaperParallaxOffset;
    HorizontalScrollView mWallpaperScrollContainer;
    View mWallpaperStrip;
    LinearLayout mWallpapersView;
    ArrayList<Uri> mTempWallpaperTiles = new ArrayList<>();
    int mSelectedIndex = -1;

    /* renamed from: com.android.launcher3.WallpaperPickerActivity$2  reason: invalid class name */
    /* loaded from: a.zip:com/android/launcher3/WallpaperPickerActivity$2.class */
    class AnonymousClass2 implements CropView.TouchCallback {
        ViewPropertyAnimator mAnim;
        final WallpaperPickerActivity this$0;

        AnonymousClass2(WallpaperPickerActivity wallpaperPickerActivity) {
            this.this$0 = wallpaperPickerActivity;
        }

        @Override // com.android.launcher3.CropView.TouchCallback
        public void onTap() {
            boolean z = this.this$0.mIgnoreNextTap;
            this.this$0.mIgnoreNextTap = false;
            if (z) {
                return;
            }
            if (this.mAnim != null) {
                this.mAnim.cancel();
            }
            this.this$0.mWallpaperStrip.setVisibility(0);
            this.mAnim = this.this$0.mWallpaperStrip.animate();
            this.mAnim.alpha(1.0f).setDuration(150L).setInterpolator(new DecelerateInterpolator(0.75f));
            this.mAnim.start();
        }

        @Override // com.android.launcher3.CropView.TouchCallback
        public void onTouchDown() {
            if (this.mAnim != null) {
                this.mAnim.cancel();
            }
            if (this.this$0.mWallpaperStrip.getAlpha() == 1.0f) {
                this.this$0.mIgnoreNextTap = true;
            }
            this.mAnim = this.this$0.mWallpaperStrip.animate();
            this.mAnim.alpha(0.0f).setDuration(150L).withEndAction(new Runnable(this) { // from class: com.android.launcher3.WallpaperPickerActivity.2.1
                final AnonymousClass2 this$1;

                {
                    this.this$1 = this;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$1.this$0.mWallpaperStrip.setVisibility(4);
                }
            });
            this.mAnim.setInterpolator(new AccelerateInterpolator(0.75f));
            this.mAnim.start();
        }

        @Override // com.android.launcher3.CropView.TouchCallback
        public void onTouchUp() {
            this.this$0.mIgnoreNextTap = false;
        }
    }

    @TargetApi(19)
    /* loaded from: a.zip:com/android/launcher3/WallpaperPickerActivity$DefaultWallpaperInfo.class */
    public static class DefaultWallpaperInfo extends WallpaperTileInfo {
        public DefaultWallpaperInfo(Drawable drawable) {
            this.mThumb = drawable;
        }

        @NonNull
        private BitmapCropTask getDefaultWallpaperCropTask(WallpaperPickerActivity wallpaperPickerActivity, BitmapCropTask.OnEndCropHandler onEndCropHandler) {
            return new BitmapCropTask(this, wallpaperPickerActivity, null, null, -1, -1, -1, true, false, onEndCropHandler, wallpaperPickerActivity) { // from class: com.android.launcher3.WallpaperPickerActivity.DefaultWallpaperInfo.2
                final DefaultWallpaperInfo this$1;
                final WallpaperPickerActivity val$a;

                {
                    this.this$1 = this;
                    this.val$a = wallpaperPickerActivity;
                }

                /* JADX INFO: Access modifiers changed from: protected */
                @Override // com.android.gallery3d.common.BitmapCropTask
                public Boolean doInBackground(Integer... numArr) {
                    boolean z;
                    int intValue = numArr[0].intValue();
                    try {
                        if (intValue == 2) {
                            Bitmap bitmap = ((BitmapDrawable) WallpaperManager.getInstance(this.val$a.getApplicationContext()).getBuiltInDrawable()).getBitmap();
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(2048);
                            z = true;
                            if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)) {
                                NycWallpaperUtils.setStream(this.val$a.getApplicationContext(), new ByteArrayInputStream(byteArrayOutputStream.toByteArray()), null, true, 2);
                                z = true;
                            }
                        } else {
                            NycWallpaperUtils.clear(this.val$a, intValue);
                            z = true;
                        }
                    } catch (IOException e) {
                        Log.e("WallpaperPickerActivity", "Setting wallpaper to default threw exception", e);
                        z = false;
                    } catch (SecurityException e2) {
                        Log.w("WallpaperPickerActivity", "Setting wallpaper to default threw exception", e2);
                        z = true;
                    }
                    return Boolean.valueOf(z);
                }
            };
        }

        @Override // com.android.launcher3.WallpaperPickerActivity.WallpaperTileInfo
        public boolean isNamelessWallpaper() {
            return true;
        }

        @Override // com.android.launcher3.WallpaperPickerActivity.WallpaperTileInfo
        public boolean isSelectable() {
            return true;
        }

        @Override // com.android.launcher3.WallpaperPickerActivity.WallpaperTileInfo
        public void onClick(WallpaperPickerActivity wallpaperPickerActivity) {
            CropView cropView = wallpaperPickerActivity.getCropView();
            if (wallpaperPickerActivity.mProgressView != null) {
                Log.d("WallpaperPickerActivity", "DefaultWallpaperInfo.onClick(),a.mProgressView.setVisibility(View.INVISIBLE)");
                wallpaperPickerActivity.mProgressView.setVisibility(4);
            }
            Drawable builtInDrawable = WallpaperManager.getInstance(wallpaperPickerActivity.getContext()).getBuiltInDrawable(cropView.getWidth(), cropView.getHeight(), false, 0.5f, 0.5f);
            if (builtInDrawable == null) {
                Log.w("WallpaperPickerActivity", "Null default wallpaper encountered.");
                cropView.setTileSource(null, null);
                return;
            }
            WallpaperCropActivity.LoadRequest loadRequest = new WallpaperCropActivity.LoadRequest();
            loadRequest.moveToLeft = false;
            loadRequest.touchEnabled = false;
            loadRequest.scaleAndOffsetProvider = new WallpaperCropActivity.CropViewScaleAndOffsetProvider();
            loadRequest.result = new DrawableTileSource(wallpaperPickerActivity.getContext(), builtInDrawable, 1024);
            wallpaperPickerActivity.onLoadRequestComplete(loadRequest, true);
        }

        @Override // com.android.launcher3.WallpaperPickerActivity.WallpaperTileInfo
        public void onSave(WallpaperPickerActivity wallpaperPickerActivity) {
            if (Utilities.ATLEAST_N) {
                NycWallpaperUtils.executeCropTaskAfterPrompt(wallpaperPickerActivity, getDefaultWallpaperCropTask(wallpaperPickerActivity, new BitmapCropTask.OnEndCropHandler(this, wallpaperPickerActivity) { // from class: com.android.launcher3.WallpaperPickerActivity.DefaultWallpaperInfo.1
                    final DefaultWallpaperInfo this$1;
                    final WallpaperPickerActivity val$a;

                    {
                        this.this$1 = this;
                        this.val$a = wallpaperPickerActivity;
                    }

                    @Override // com.android.gallery3d.common.BitmapCropTask.OnEndCropHandler
                    public void run(boolean z) {
                        if (z) {
                            this.val$a.setResult(-1);
                        }
                        this.val$a.finish();
                    }
                }), wallpaperPickerActivity.getOnDialogCancelListener());
                return;
            }
            try {
                WallpaperManager.getInstance(wallpaperPickerActivity.getContext()).clear();
                wallpaperPickerActivity.setResult(-1);
            } catch (IOException e) {
                Log.e("WallpaperPickerActivity", "Setting wallpaper to default threw exception", e);
            } catch (SecurityException e2) {
                Log.w("WallpaperPickerActivity", "Setting wallpaper to default threw exception", e2);
                wallpaperPickerActivity.setResult(-1);
            }
            wallpaperPickerActivity.finish();
        }
    }

    /* loaded from: a.zip:com/android/launcher3/WallpaperPickerActivity$FileWallpaperInfo.class */
    public static class FileWallpaperInfo extends WallpaperTileInfo {
        protected File mFile;

        public FileWallpaperInfo(File file, Drawable drawable) {
            this.mFile = file;
            this.mThumb = drawable;
        }

        protected WallpaperCropActivity.CropViewScaleAndOffsetProvider getCropViewScaleAndOffsetProvider() {
            return null;
        }

        @Override // com.android.launcher3.WallpaperPickerActivity.WallpaperTileInfo
        public boolean isNamelessWallpaper() {
            return true;
        }

        @Override // com.android.launcher3.WallpaperPickerActivity.WallpaperTileInfo
        public boolean isSelectable() {
            return true;
        }

        @Override // com.android.launcher3.WallpaperPickerActivity.WallpaperTileInfo
        public void onClick(WallpaperPickerActivity wallpaperPickerActivity) {
            wallpaperPickerActivity.setWallpaperButtonEnabled(false);
            BitmapRegionTileSource.UriBitmapSource uriBitmapSource = new BitmapRegionTileSource.UriBitmapSource(wallpaperPickerActivity.getContext(), Uri.fromFile(this.mFile));
            wallpaperPickerActivity.setCropViewTileSource(uriBitmapSource, false, true, getCropViewScaleAndOffsetProvider(), new Runnable(this, uriBitmapSource, wallpaperPickerActivity) { // from class: com.android.launcher3.WallpaperPickerActivity.FileWallpaperInfo.1
                final FileWallpaperInfo this$1;
                final WallpaperPickerActivity val$a;
                final BitmapRegionTileSource.UriBitmapSource val$bitmapSource;

                {
                    this.this$1 = this;
                    this.val$bitmapSource = uriBitmapSource;
                    this.val$a = wallpaperPickerActivity;
                }

                @Override // java.lang.Runnable
                public void run() {
                    if (this.val$bitmapSource.getLoadingState() == BitmapRegionTileSource.BitmapSource.State.LOADED) {
                        this.val$a.setWallpaperButtonEnabled(true);
                    }
                }
            });
        }

        @Override // com.android.launcher3.WallpaperPickerActivity.WallpaperTileInfo
        public void onSave(WallpaperPickerActivity wallpaperPickerActivity) {
            wallpaperPickerActivity.setWallpaper(Uri.fromFile(this.mFile), true, wallpaperPickerActivity.getWallpaperParallaxOffset() == 0.0f);
        }
    }

    /* loaded from: a.zip:com/android/launcher3/WallpaperPickerActivity$PickImageInfo.class */
    public static class PickImageInfo extends WallpaperTileInfo {
        @Override // com.android.launcher3.WallpaperPickerActivity.WallpaperTileInfo
        public void onClick(WallpaperPickerActivity wallpaperPickerActivity) {
            Intent intent = new Intent("android.intent.action.GET_CONTENT");
            intent.setType("image/*");
            intent.putExtra("android.intent.extra.drm_level", 1);
            wallpaperPickerActivity.startActivityForResultSafely(intent, 5);
        }
    }

    /* loaded from: a.zip:com/android/launcher3/WallpaperPickerActivity$ResourceWallpaperInfo.class */
    public static class ResourceWallpaperInfo extends WallpaperTileInfo {
        private int mResId;
        private Resources mResources;

        public ResourceWallpaperInfo(Resources resources, int i, Drawable drawable) {
            this.mResources = resources;
            this.mResId = i;
            this.mThumb = drawable;
        }

        @Override // com.android.launcher3.WallpaperPickerActivity.WallpaperTileInfo
        public boolean isNamelessWallpaper() {
            return true;
        }

        @Override // com.android.launcher3.WallpaperPickerActivity.WallpaperTileInfo
        public boolean isSelectable() {
            return true;
        }

        @Override // com.android.launcher3.WallpaperPickerActivity.WallpaperTileInfo
        public void onClick(WallpaperPickerActivity wallpaperPickerActivity) {
            wallpaperPickerActivity.setWallpaperButtonEnabled(false);
            BitmapRegionTileSource.ResourceBitmapSource resourceBitmapSource = new BitmapRegionTileSource.ResourceBitmapSource(this.mResources, this.mResId);
            wallpaperPickerActivity.setCropViewTileSource(resourceBitmapSource, false, false, new WallpaperCropActivity.CropViewScaleAndOffsetProvider(this, wallpaperPickerActivity) { // from class: com.android.launcher3.WallpaperPickerActivity.ResourceWallpaperInfo.1
                final ResourceWallpaperInfo this$1;
                final WallpaperPickerActivity val$a;

                {
                    this.this$1 = this;
                    this.val$a = wallpaperPickerActivity;
                }

                @Override // com.android.launcher3.WallpaperCropActivity.CropViewScaleAndOffsetProvider
                public float getParallaxOffset() {
                    return this.val$a.getWallpaperParallaxOffset();
                }

                @Override // com.android.launcher3.WallpaperCropActivity.CropViewScaleAndOffsetProvider
                public float getScale(Point point, RectF rectF) {
                    return point.x / rectF.width();
                }
            }, new Runnable(this, resourceBitmapSource, wallpaperPickerActivity) { // from class: com.android.launcher3.WallpaperPickerActivity.ResourceWallpaperInfo.2
                final ResourceWallpaperInfo this$1;
                final WallpaperPickerActivity val$a;
                final BitmapRegionTileSource.ResourceBitmapSource val$bitmapSource;

                {
                    this.this$1 = this;
                    this.val$bitmapSource = resourceBitmapSource;
                    this.val$a = wallpaperPickerActivity;
                }

                @Override // java.lang.Runnable
                public void run() {
                    if (this.val$bitmapSource.getLoadingState() == BitmapRegionTileSource.BitmapSource.State.LOADED) {
                        this.val$a.setWallpaperButtonEnabled(true);
                    }
                }
            });
        }

        @Override // com.android.launcher3.WallpaperPickerActivity.WallpaperTileInfo
        public void onSave(WallpaperPickerActivity wallpaperPickerActivity) {
            wallpaperPickerActivity.cropImageAndSetWallpaper(this.mResources, this.mResId, true, true);
        }
    }

    /* loaded from: a.zip:com/android/launcher3/WallpaperPickerActivity$SimpleWallpapersAdapter.class */
    private static class SimpleWallpapersAdapter extends ArrayAdapter<WallpaperTileInfo> {
        private final LayoutInflater mLayoutInflater;

        SimpleWallpapersAdapter(Context context, ArrayList<WallpaperTileInfo> arrayList) {
            super(context, 2130968606, arrayList);
            this.mLayoutInflater = LayoutInflater.from(context);
        }

        @Override // android.widget.ArrayAdapter, android.widget.Adapter
        public View getView(int i, View view, ViewGroup viewGroup) {
            Drawable drawable = getItem(i).mThumb;
            if (drawable == null) {
                Log.e("WallpaperPickerActivity", "Error decoding thumbnail for wallpaper #" + i);
            }
            return WallpaperPickerActivity.createImageTileView(this.mLayoutInflater, view, viewGroup, drawable);
        }
    }

    /* loaded from: a.zip:com/android/launcher3/WallpaperPickerActivity$UriWallpaperInfo.class */
    public static class UriWallpaperInfo extends WallpaperTileInfo {
        private Uri mUri;

        public UriWallpaperInfo(Uri uri) {
            this.mUri = uri;
        }

        @Override // com.android.launcher3.WallpaperPickerActivity.WallpaperTileInfo
        public boolean isNamelessWallpaper() {
            return true;
        }

        @Override // com.android.launcher3.WallpaperPickerActivity.WallpaperTileInfo
        public boolean isSelectable() {
            return true;
        }

        @Override // com.android.launcher3.WallpaperPickerActivity.WallpaperTileInfo
        public void onClick(WallpaperPickerActivity wallpaperPickerActivity) {
            wallpaperPickerActivity.setWallpaperButtonEnabled(false);
            BitmapRegionTileSource.UriBitmapSource uriBitmapSource = new BitmapRegionTileSource.UriBitmapSource(wallpaperPickerActivity.getContext(), this.mUri);
            wallpaperPickerActivity.setCropViewTileSource(uriBitmapSource, true, false, null, new Runnable(this, uriBitmapSource, wallpaperPickerActivity) { // from class: com.android.launcher3.WallpaperPickerActivity.UriWallpaperInfo.1
                final UriWallpaperInfo this$1;
                final WallpaperPickerActivity val$a;
                final BitmapRegionTileSource.UriBitmapSource val$bitmapSource;

                {
                    this.this$1 = this;
                    this.val$bitmapSource = uriBitmapSource;
                    this.val$a = wallpaperPickerActivity;
                }

                @Override // java.lang.Runnable
                public void run() {
                    if (this.val$bitmapSource.getLoadingState() == BitmapRegionTileSource.BitmapSource.State.LOADED) {
                        this.val$a.selectTile(this.this$1.mView);
                        this.val$a.setWallpaperButtonEnabled(true);
                        return;
                    }
                    ViewGroup viewGroup = (ViewGroup) this.this$1.mView.getParent();
                    if (viewGroup != null) {
                        viewGroup.removeView(this.this$1.mView);
                        Toast.makeText(this.val$a.getContext(), 2131558547, 0).show();
                    }
                }
            });
        }

        @Override // com.android.launcher3.WallpaperPickerActivity.WallpaperTileInfo
        public void onSave(WallpaperPickerActivity wallpaperPickerActivity) {
            wallpaperPickerActivity.cropImageAndSetWallpaper(this.mUri, new BitmapCropTask.OnBitmapCroppedHandler(this, wallpaperPickerActivity) { // from class: com.android.launcher3.WallpaperPickerActivity.UriWallpaperInfo.2
                final UriWallpaperInfo this$1;
                final WallpaperPickerActivity val$a;

                {
                    this.this$1 = this;
                    this.val$a = wallpaperPickerActivity;
                }

                @Override // com.android.gallery3d.common.BitmapCropTask.OnBitmapCroppedHandler
                public void onBitmapCropped(byte[] bArr, Rect rect) {
                    Point defaultThumbnailSize = WallpaperPickerActivity.getDefaultThumbnailSize(this.val$a.getResources());
                    if (bArr != null) {
                        this.val$a.getSavedImages().writeImage(WallpaperPickerActivity.createThumbnail(defaultThumbnailSize, null, null, bArr, null, 0, 0, true), bArr);
                        return;
                    }
                    Bitmap bitmap = null;
                    try {
                        Point defaultThumbnailSize2 = WallpaperPickerActivity.getDefaultThumbnailSize(this.val$a.getResources());
                        Rect rect2 = new Rect();
                        Utils.getMaxCropRect(rect.width(), rect.height(), defaultThumbnailSize2.x, defaultThumbnailSize2.y, false).roundOut(rect2);
                        rect2.offset(rect.left, rect.top);
                        InputStream openInputStream = this.val$a.getContentResolver().openInputStream(this.this$1.mUri);
                        BitmapRegionDecoder newInstance = BitmapRegionDecoder.newInstance(openInputStream, true);
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = rect2.width() / defaultThumbnailSize2.x;
                        Bitmap decodeRegion = newInstance.decodeRegion(rect2, options);
                        newInstance.recycle();
                        Utils.closeSilently(openInputStream);
                        bitmap = decodeRegion;
                        if (decodeRegion != null) {
                            bitmap = decodeRegion;
                            bitmap = Bitmap.createScaledBitmap(decodeRegion, defaultThumbnailSize2.x, defaultThumbnailSize2.y, true);
                        }
                    } catch (IOException e) {
                    }
                    PointF center = this.val$a.mCropView.getCenter();
                    this.val$a.getSavedImages().writeImage(bitmap, this.this$1.mUri, new Float[]{Float.valueOf(this.val$a.mCropView.getScale()), Float.valueOf(center.x), Float.valueOf(center.y)});
                }
            }, true, wallpaperPickerActivity.getWallpaperParallaxOffset() == 0.0f);
        }
    }

    /* loaded from: a.zip:com/android/launcher3/WallpaperPickerActivity$WallpaperTileInfo.class */
    public static abstract class WallpaperTileInfo {
        public Drawable mThumb;
        protected View mView;

        public boolean isNamelessWallpaper() {
            return false;
        }

        public boolean isSelectable() {
            return false;
        }

        public void onClick(WallpaperPickerActivity wallpaperPickerActivity) {
        }

        public void onDelete(WallpaperPickerActivity wallpaperPickerActivity) {
        }

        public void onIndexUpdated(CharSequence charSequence) {
            if (isNamelessWallpaper()) {
                this.mView.setContentDescription(charSequence);
            }
        }

        public void onSave(WallpaperPickerActivity wallpaperPickerActivity) {
        }

        public void setView(View view) {
            this.mView = view;
        }
    }

    private void addLongPressHandler(View view) {
        view.setOnLongClickListener(this.mLongClickListener);
        view.setOnTouchListener(new View.OnTouchListener(this, new StylusEventHelper(view)) { // from class: com.android.launcher3.WallpaperPickerActivity.11
            final WallpaperPickerActivity this$0;
            final StylusEventHelper val$stylusEventHelper;

            {
                this.this$0 = this;
                this.val$stylusEventHelper = r5;
            }

            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view2, MotionEvent motionEvent) {
                return this.val$stylusEventHelper.checkAndPerformStylusEvent(motionEvent);
            }
        });
    }

    /* JADX WARN: Type inference failed for: r0v21, types: [com.android.launcher3.WallpaperPickerActivity$10] */
    private void addTemporaryWallpaperTile(Uri uri, boolean z) {
        FrameLayout frameLayout;
        FrameLayout frameLayout2;
        int i = 0;
        while (true) {
            frameLayout = null;
            if (i >= this.mWallpapersView.getChildCount()) {
                break;
            }
            frameLayout = (FrameLayout) this.mWallpapersView.getChildAt(i);
            Object tag = frameLayout.getTag();
            if ((tag instanceof UriWallpaperInfo) && ((UriWallpaperInfo) tag).mUri.equals(uri)) {
                break;
            }
            i++;
        }
        if (frameLayout != null) {
            this.mWallpapersView.removeViewAt(i);
            this.mWallpapersView.addView(frameLayout, 0);
            frameLayout2 = frameLayout;
        } else {
            frameLayout2 = (FrameLayout) getLayoutInflater().inflate(2130968606, (ViewGroup) this.mWallpapersView, false);
            frameLayout2.setVisibility(8);
            this.mWallpapersView.addView(frameLayout2, 0);
            this.mTempWallpaperTiles.add(uri);
        }
        new AsyncTask<Void, Bitmap, Bitmap>(this, getContext(), uri, getDefaultThumbnailSize(getResources()), (ImageView) frameLayout2.findViewById(2131296324), frameLayout2) { // from class: com.android.launcher3.WallpaperPickerActivity.10
            final WallpaperPickerActivity this$0;
            final Context val$context;
            final Point val$defaultSize;
            final ImageView val$image;
            final FrameLayout val$pickedImageThumbnail;
            final Uri val$uri;

            {
                this.this$0 = this;
                this.val$context = r5;
                this.val$uri = uri;
                this.val$defaultSize = r7;
                this.val$image = r8;
                this.val$pickedImageThumbnail = frameLayout2;
            }

            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public Bitmap doInBackground(Void... voidArr) {
                try {
                    return WallpaperPickerActivity.createThumbnail(this.val$defaultSize, this.val$context, this.val$uri, null, null, 0, BitmapUtils.getRotationFromExif(this.val$context, this.val$uri), false);
                } catch (SecurityException e) {
                    if (this.this$0.isActivityDestroyed()) {
                        cancel(false);
                        return null;
                    }
                    throw e;
                }
            }

            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public void onPostExecute(Bitmap bitmap) {
                if (isCancelled() || bitmap == null) {
                    Log.e("WallpaperPickerActivity", "Error loading thumbnail for uri=" + this.val$uri);
                    return;
                }
                this.val$image.setImageBitmap(bitmap);
                this.val$image.getDrawable().setDither(true);
                this.val$pickedImageThumbnail.setVisibility(0);
            }
        }.execute(new Void[0]);
        UriWallpaperInfo uriWallpaperInfo = new UriWallpaperInfo(uri);
        frameLayout2.setTag(uriWallpaperInfo);
        uriWallpaperInfo.setView(frameLayout2);
        addLongPressHandler(frameLayout2);
        updateTileIndices();
        frameLayout2.setOnClickListener(this.mThumbnailOnClickListener);
        if (z) {
            return;
        }
        this.mThumbnailOnClickListener.onClick(frameLayout2);
    }

    private void addWallpapers(ArrayList<WallpaperTileInfo> arrayList, Resources resources, String str, int i) {
        String[] stringArray = resources.getStringArray(i);
        for (String str2 : stringArray) {
            int identifier = resources.getIdentifier(str2, "drawable", str);
            if (identifier != 0) {
                int identifier2 = resources.getIdentifier(str2 + "_small", "drawable", str);
                if (identifier2 != 0) {
                    arrayList.add(new ResourceWallpaperInfo(resources, identifier, resources.getDrawable(identifier2)));
                }
            } else {
                Log.e("WallpaperPickerActivity", "Couldn't find wallpaper " + str2);
            }
        }
    }

    public static View createImageTileView(LayoutInflater layoutInflater, View view, ViewGroup viewGroup, Drawable drawable) {
        View inflate = view == null ? layoutInflater.inflate(2130968606, viewGroup, false) : view;
        ImageView imageView = (ImageView) inflate.findViewById(2131296324);
        if (drawable != null) {
            imageView.setImageDrawable(drawable);
            drawable.setDither(true);
        }
        return inflate;
    }

    static Bitmap createThumbnail(Point point, Context context, Uri uri, byte[] bArr, Resources resources, int i, int i2, boolean z) {
        int i3 = point.x;
        int i4 = point.y;
        BitmapCropTask bitmapCropTask = uri != null ? new BitmapCropTask(context, uri, null, i2, i3, i4, false, true, null) : bArr != null ? new BitmapCropTask(bArr, null, i2, i3, i4, false, true, null) : new BitmapCropTask(context, resources, i, null, i2, i3, i4, false, true, null);
        Point imageBounds = bitmapCropTask.getImageBounds();
        if (imageBounds == null || imageBounds.x == 0 || imageBounds.y == 0) {
            return null;
        }
        Matrix matrix = new Matrix();
        matrix.setRotate(i2);
        float[] fArr = {imageBounds.x, imageBounds.y};
        matrix.mapPoints(fArr);
        fArr[0] = Math.abs(fArr[0]);
        fArr[1] = Math.abs(fArr[1]);
        bitmapCropTask.setCropBounds(Utils.getMaxCropRect((int) fArr[0], (int) fArr[1], i3, i4, z));
        if (bitmapCropTask.cropBitmap(1)) {
            return bitmapCropTask.getCroppedBitmap();
        }
        return null;
    }

    private ArrayList<WallpaperTileInfo> findBundledWallpapers() {
        File[] listFiles;
        Bitmap decodeFile;
        PackageManager packageManager = getContext().getPackageManager();
        ArrayList<WallpaperTileInfo> arrayList = new ArrayList<>(24);
        Partner partner = Partner.get(packageManager);
        if (partner != null) {
            Resources resources = partner.getResources();
            int identifier = resources.getIdentifier("partner_wallpapers", "array", partner.getPackageName());
            if (identifier != 0) {
                addWallpapers(arrayList, resources, partner.getPackageName(), identifier);
            }
            File wallpaperDirectory = partner.getWallpaperDirectory();
            if (wallpaperDirectory != null && wallpaperDirectory.isDirectory()) {
                for (File file : wallpaperDirectory.listFiles()) {
                    if (file.isFile()) {
                        String name = file.getName();
                        int lastIndexOf = name.lastIndexOf(46);
                        String str = "";
                        String str2 = name;
                        if (lastIndexOf >= -1) {
                            str = name.substring(lastIndexOf);
                            str2 = name.substring(0, lastIndexOf);
                        }
                        if (!str2.endsWith("_small") && (decodeFile = BitmapFactory.decodeFile(new File(wallpaperDirectory, str2 + "_small" + str).getAbsolutePath())) != null) {
                            arrayList.add(new FileWallpaperInfo(file, new BitmapDrawable(decodeFile)));
                        }
                    }
                }
            }
        }
        Pair<ApplicationInfo, Integer> wallpaperArrayResourceId = getWallpaperArrayResourceId();
        if (wallpaperArrayResourceId != null) {
            try {
                addWallpapers(arrayList, getContext().getPackageManager().getResourcesForApplication((ApplicationInfo) wallpaperArrayResourceId.first), ((ApplicationInfo) wallpaperArrayResourceId.first).packageName, ((Integer) wallpaperArrayResourceId.second).intValue());
            } catch (PackageManager.NameNotFoundException e) {
            }
        }
        if (partner == null || !partner.hideDefaultWallpaper()) {
            DefaultWallpaperInfo defaultWallpaper = Utilities.ATLEAST_KITKAT ? getDefaultWallpaper() : getPreKKDefaultWallpaperInfo();
            if (defaultWallpaper != null) {
                arrayList.add(0, defaultWallpaper);
            }
        }
        return arrayList;
    }

    private File getDefaultThumbFile() {
        return new File(getContext().getFilesDir(), Build.VERSION.SDK_INT + "_default_thumb2.jpg");
    }

    static Point getDefaultThumbnailSize(Resources resources) {
        return new Point(resources.getDimensionPixelSize(2131230812), resources.getDimensionPixelSize(2131230813));
    }

    @TargetApi(19)
    private DefaultWallpaperInfo getDefaultWallpaper() {
        Bitmap bitmap;
        File defaultThumbFile = getDefaultThumbFile();
        Bitmap bitmap2 = null;
        boolean z = false;
        if (defaultThumbFile.exists()) {
            bitmap = BitmapFactory.decodeFile(defaultThumbFile.getAbsolutePath());
            z = true;
        } else {
            Point defaultThumbnailSize = getDefaultThumbnailSize(getResources());
            Drawable builtInDrawable = WallpaperManager.getInstance(getContext()).getBuiltInDrawable(defaultThumbnailSize.x, defaultThumbnailSize.y, true, 0.5f, 0.5f);
            if (builtInDrawable != null) {
                bitmap2 = Bitmap.createBitmap(defaultThumbnailSize.x, defaultThumbnailSize.y, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap2);
                builtInDrawable.setBounds(0, 0, defaultThumbnailSize.x, defaultThumbnailSize.y);
                builtInDrawable.draw(canvas);
                canvas.setBitmap(null);
            }
            bitmap = bitmap2;
            if (bitmap2 != null) {
                z = saveDefaultWallpaperThumb(bitmap2);
                bitmap = bitmap2;
            }
        }
        if (z) {
            return new DefaultWallpaperInfo(new BitmapDrawable(bitmap));
        }
        return null;
    }

    private ResourceWallpaperInfo getPreKKDefaultWallpaperInfo() {
        Bitmap bitmap;
        Resources system = Resources.getSystem();
        int identifier = system.getIdentifier("default_wallpaper", "drawable", "android");
        File defaultThumbFile = getDefaultThumbFile();
        boolean z = false;
        if (defaultThumbFile.exists()) {
            bitmap = BitmapFactory.decodeFile(defaultThumbFile.getAbsolutePath());
            z = true;
        } else {
            Resources resources = getResources();
            Bitmap createThumbnail = createThumbnail(getDefaultThumbnailSize(resources), getContext(), null, null, system, identifier, BitmapUtils.getRotationFromExif(resources, identifier), false);
            bitmap = createThumbnail;
            if (createThumbnail != null) {
                z = saveDefaultWallpaperThumb(createThumbnail);
                bitmap = createThumbnail;
            }
        }
        if (z) {
            return new ResourceWallpaperInfo(system, identifier, new BitmapDrawable(bitmap));
        }
        return null;
    }

    private boolean saveDefaultWallpaperThumb(Bitmap bitmap) {
        new File(getContext().getFilesDir(), "default_thumb.jpg").delete();
        new File(getContext().getFilesDir(), "default_thumb2.jpg").delete();
        for (int i = 16; i < Build.VERSION.SDK_INT; i++) {
            new File(getContext().getFilesDir(), i + "_default_thumb2.jpg").delete();
        }
        return writeImageToFileAsJpeg(getDefaultThumbFile(), bitmap);
    }

    private boolean writeImageToFileAsJpeg(File file, Bitmap bitmap) {
        try {
            file.createNewFile();
            FileOutputStream openFileOutput = getContext().openFileOutput(file.getName(), 0);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, openFileOutput);
            openFileOutput.close();
            return true;
        } catch (IOException e) {
            Log.e("WallpaperPickerActivity", "Error while writing bitmap to file " + e);
            file.delete();
            return false;
        }
    }

    void changeWallpaperFlags(boolean z) {
        int i = z ? 1048576 : 0;
        if (i != (getWindow().getAttributes().flags & 1048576)) {
            getWindow().setFlags(i, 1048576);
        }
    }

    @Override // com.android.launcher3.WallpaperCropActivity
    public boolean enableRotation() {
        return true;
    }

    public CropView getCropView() {
        return this.mCropView;
    }

    public SavedWallpaperImages getSavedImages() {
        return this.mSavedImages;
    }

    protected Bitmap getThumbnailOfLastPhoto() {
        if (getActivity().checkPermission("android.permission.READ_EXTERNAL_STORAGE", Process.myPid(), Process.myUid()) == 0) {
            Cursor query = MediaStore.Images.Media.query(getContext().getContentResolver(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{"_id", "datetaken"}, null, null, "datetaken DESC LIMIT 1");
            Bitmap bitmap = null;
            if (query != null) {
                bitmap = null;
                if (query.moveToNext()) {
                    bitmap = MediaStore.Images.Thumbnails.getThumbnail(getContext().getContentResolver(), query.getInt(0), 1, null);
                }
                query.close();
            }
            return bitmap;
        }
        return null;
    }

    public Pair<ApplicationInfo, Integer> getWallpaperArrayResourceId() {
        try {
            return new Pair<>(getContext().getPackageManager().getApplicationInfo(getResources().getResourcePackageName(2131623938), 0), 2131623938);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    public float getWallpaperParallaxOffset() {
        return this.mWallpaperParallaxOffset;
    }

    @Override // com.android.launcher3.WallpaperCropActivity
    protected void init() {
        setContentView(2130968604);
        this.mCropView = (CropView) findViewById(2131296316);
        this.mCropView.setVisibility(4);
        this.mProgressView = findViewById(2131296317);
        this.mWallpaperScrollContainer = (HorizontalScrollView) findViewById(2131296319);
        this.mWallpaperStrip = findViewById(2131296318);
        this.mCropView.setTouchCallback(new AnonymousClass2(this));
        this.mThumbnailOnClickListener = new View.OnClickListener(this) { // from class: com.android.launcher3.WallpaperPickerActivity.3
            final WallpaperPickerActivity this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                if (this.this$0.mActionMode != null) {
                    if (view.isLongClickable()) {
                        this.this$0.mLongClickListener.onLongClick(view);
                        return;
                    }
                    return;
                }
                WallpaperTileInfo wallpaperTileInfo = (WallpaperTileInfo) view.getTag();
                if (wallpaperTileInfo == null) {
                    return;
                }
                if (wallpaperTileInfo.isSelectable() && view.getVisibility() == 0) {
                    this.this$0.selectTile(view);
                    this.this$0.setWallpaperButtonEnabled(true);
                }
                wallpaperTileInfo.onClick(this.this$0);
            }
        };
        this.mLongClickListener = new View.OnLongClickListener(this) { // from class: com.android.launcher3.WallpaperPickerActivity.4
            final WallpaperPickerActivity this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.OnLongClickListener
            public boolean onLongClick(View view) {
                ((CheckableFrameLayout) view).toggle();
                if (this.this$0.mActionMode != null) {
                    this.this$0.mActionMode.invalidate();
                    return true;
                }
                this.this$0.mActionMode = this.this$0.startActionMode(this.this$0.mActionModeCallback);
                this.this$0.mActionMode.invalidate();
                int childCount = this.this$0.mWallpapersView.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    this.this$0.mWallpapersView.getChildAt(i).setSelected(false);
                }
                return true;
            }
        };
        this.mWallpaperParallaxOffset = getIntent().getFloatExtra("com.android.launcher3.WALLPAPER_OFFSET", 0.0f);
        ArrayList<WallpaperTileInfo> findBundledWallpapers = findBundledWallpapers();
        this.mWallpapersView = (LinearLayout) findViewById(2131296321);
        populateWallpapersFromAdapter(this.mWallpapersView, new SimpleWallpapersAdapter(getContext(), findBundledWallpapers), false);
        this.mSavedImages = new SavedWallpaperImages(getContext());
        this.mSavedImages.loadThumbnailsAndImageIdList();
        populateWallpapersFromAdapter(this.mWallpapersView, this.mSavedImages, true);
        LinearLayout linearLayout = (LinearLayout) findViewById(2131296322);
        LiveWallpaperListAdapter liveWallpaperListAdapter = new LiveWallpaperListAdapter(getContext());
        liveWallpaperListAdapter.registerDataSetObserver(new DataSetObserver(this, linearLayout, liveWallpaperListAdapter) { // from class: com.android.launcher3.WallpaperPickerActivity.5
            final WallpaperPickerActivity this$0;
            final LiveWallpaperListAdapter val$a;
            final LinearLayout val$liveWallpapersView;

            {
                this.this$0 = this;
                this.val$liveWallpapersView = linearLayout;
                this.val$a = liveWallpaperListAdapter;
            }

            @Override // android.database.DataSetObserver
            public void onChanged() {
                this.val$liveWallpapersView.removeAllViews();
                this.this$0.populateWallpapersFromAdapter(this.val$liveWallpapersView, this.val$a, false);
                this.this$0.initializeScrollForRtl();
                this.this$0.updateTileIndices();
            }
        });
        populateWallpapersFromAdapter((LinearLayout) findViewById(2131296323), new ThirdPartyWallpaperPickerListAdapter(getContext()), false);
        LinearLayout linearLayout2 = (LinearLayout) findViewById(2131296320);
        FrameLayout frameLayout = (FrameLayout) getLayoutInflater().inflate(2130968605, (ViewGroup) linearLayout2, false);
        linearLayout2.addView(frameLayout, 0);
        Bitmap thumbnailOfLastPhoto = getThumbnailOfLastPhoto();
        if (thumbnailOfLastPhoto != null) {
            ImageView imageView = (ImageView) frameLayout.findViewById(2131296324);
            imageView.setImageBitmap(thumbnailOfLastPhoto);
            imageView.setColorFilter(getResources().getColor(2131361811), PorterDuff.Mode.SRC_ATOP);
        }
        PickImageInfo pickImageInfo = new PickImageInfo();
        frameLayout.setTag(pickImageInfo);
        pickImageInfo.setView(frameLayout);
        frameLayout.setOnClickListener(this.mThumbnailOnClickListener);
        this.mCropView.addOnLayoutChangeListener(new View.OnLayoutChangeListener(this) { // from class: com.android.launcher3.WallpaperPickerActivity.6
            final WallpaperPickerActivity this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.OnLayoutChangeListener
            public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                if (i3 - i <= 0 || i4 - i2 <= 0) {
                    return;
                }
                if (this.this$0.mSelectedIndex >= 0 && this.this$0.mSelectedIndex < this.this$0.mWallpapersView.getChildCount()) {
                    this.this$0.mThumbnailOnClickListener.onClick(this.this$0.mWallpapersView.getChildAt(this.this$0.mSelectedIndex));
                    this.this$0.setSystemWallpaperVisiblity(false);
                }
                view.removeOnLayoutChangeListener(this);
            }
        });
        updateTileIndices();
        initializeScrollForRtl();
        LayoutTransition layoutTransition = new LayoutTransition();
        layoutTransition.setDuration(200L);
        layoutTransition.setStartDelay(1, 0L);
        layoutTransition.setAnimator(3, null);
        this.mWallpapersView.setLayoutTransition(layoutTransition);
        ActionBar actionBar = getActionBar();
        actionBar.setCustomView(2130968576);
        actionBar.getCustomView().setOnClickListener(new View.OnClickListener(this, actionBar) { // from class: com.android.launcher3.WallpaperPickerActivity.7
            final WallpaperPickerActivity this$0;
            final ActionBar val$actionBar;

            {
                this.this$0 = this;
                this.val$actionBar = actionBar;
            }

            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                if (this.this$0.mSelectedTile == null || this.this$0.mCropView.getTileSource() == null) {
                    Log.w("WallpaperPickerActivity", "\"Set wallpaper\" was clicked when no tile was selected");
                    return;
                }
                this.this$0.mWallpaperStrip.setVisibility(8);
                this.val$actionBar.hide();
                ((WallpaperTileInfo) this.this$0.mSelectedTile.getTag()).onSave(this.this$0);
            }
        });
        this.mSetWallpaperButton = findViewById(2131296272);
        this.mActionModeCallback = new ActionMode.Callback(this) { // from class: com.android.launcher3.WallpaperPickerActivity.8
            final WallpaperPickerActivity this$0;

            {
                this.this$0 = this;
            }

            private int numCheckedItems() {
                int childCount = this.this$0.mWallpapersView.getChildCount();
                int i = 0;
                int i2 = 0;
                while (i2 < childCount) {
                    int i3 = i;
                    if (((CheckableFrameLayout) this.this$0.mWallpapersView.getChildAt(i2)).isChecked()) {
                        i3 = i + 1;
                    }
                    i2++;
                    i = i3;
                }
                return i;
            }

            @Override // android.view.ActionMode.Callback
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                if (menuItem.getItemId() == 2131296335) {
                    int childCount = this.this$0.mWallpapersView.getChildCount();
                    ArrayList<View> arrayList = new ArrayList();
                    boolean z = false;
                    int i = 0;
                    while (i < childCount) {
                        CheckableFrameLayout checkableFrameLayout = (CheckableFrameLayout) this.this$0.mWallpapersView.getChildAt(i);
                        boolean z2 = z;
                        if (checkableFrameLayout.isChecked()) {
                            ((WallpaperTileInfo) checkableFrameLayout.getTag()).onDelete(this.this$0);
                            arrayList.add(checkableFrameLayout);
                            z2 = z;
                            if (i == this.this$0.mSelectedIndex) {
                                z2 = true;
                            }
                        }
                        i++;
                        z = z2;
                    }
                    for (View view : arrayList) {
                        this.this$0.mWallpapersView.removeView(view);
                    }
                    if (z) {
                        this.this$0.mSelectedIndex = -1;
                        this.this$0.mSelectedTile = null;
                    }
                    this.this$0.updateTileIndices();
                    actionMode.finish();
                    if (z) {
                        this.this$0.mThumbnailOnClickListener.onClick(this.this$0.mWallpapersView.getChildAt(0));
                        return true;
                    }
                    return true;
                }
                return false;
            }

            @Override // android.view.ActionMode.Callback
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                actionMode.getMenuInflater().inflate(2131886080, menu);
                return true;
            }

            @Override // android.view.ActionMode.Callback
            public void onDestroyActionMode(ActionMode actionMode) {
                int childCount = this.this$0.mWallpapersView.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    ((CheckableFrameLayout) this.this$0.mWallpapersView.getChildAt(i)).setChecked(false);
                }
                if (this.this$0.mSelectedTile != null) {
                    this.this$0.mSelectedTile.setSelected(true);
                }
                this.this$0.mActionMode = null;
            }

            @Override // android.view.ActionMode.Callback
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                int numCheckedItems = numCheckedItems();
                if (numCheckedItems == 0) {
                    actionMode.finish();
                    return true;
                }
                actionMode.setTitle(this.this$0.getResources().getQuantityString(2131820544, numCheckedItems, Integer.valueOf(numCheckedItems)));
                return true;
            }
        };
    }

    void initializeScrollForRtl() {
        if (Utilities.isRtl(getResources())) {
            this.mWallpaperScrollContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener(this) { // from class: com.android.launcher3.WallpaperPickerActivity.9
                final WallpaperPickerActivity this$0;

                {
                    this.this$0 = this;
                }

                @Override // android.view.ViewTreeObserver.OnGlobalLayoutListener
                public void onGlobalLayout() {
                    this.this$0.mWallpaperScrollContainer.scrollTo(((LinearLayout) this.this$0.findViewById(2131296320)).getWidth(), 0);
                    this.this$0.mWallpaperScrollContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
        }
    }

    @Override // android.app.Activity
    public void onActivityResult(int i, int i2, Intent intent) {
        if (i == 5 && i2 == -1) {
            if (intent == null || intent.getData() == null) {
                return;
            }
            addTemporaryWallpaperTile(intent.getData(), false);
        } else if (i == 6 && i2 == -1) {
            setResult(-1);
            finish();
        }
    }

    @Override // com.android.launcher3.WallpaperCropActivity
    protected void onLoadRequestComplete(WallpaperCropActivity.LoadRequest loadRequest, boolean z) {
        super.onLoadRequestComplete(loadRequest, z);
        if (z) {
            setSystemWallpaperVisiblity(false);
        }
    }

    @Override // android.app.Activity
    protected void onRestoreInstanceState(Bundle bundle) {
        for (Uri uri : bundle.getParcelableArrayList("TEMP_WALLPAPER_TILES")) {
            addTemporaryWallpaperTile(uri, true);
        }
        this.mSelectedIndex = bundle.getInt("SELECTED_INDEX", -1);
    }

    @Override // android.app.Activity
    public void onSaveInstanceState(Bundle bundle) {
        bundle.putParcelableArrayList("TEMP_WALLPAPER_TILES", this.mTempWallpaperTiles);
        bundle.putInt("SELECTED_INDEX", this.mSelectedIndex);
    }

    @Override // android.app.Activity
    public void onStop() {
        super.onStop();
        this.mWallpaperStrip = findViewById(2131296318);
        if (this.mWallpaperStrip == null || this.mWallpaperStrip.getAlpha() >= 1.0f) {
            return;
        }
        this.mWallpaperStrip.setAlpha(1.0f);
        this.mWallpaperStrip.setVisibility(0);
    }

    void populateWallpapersFromAdapter(ViewGroup viewGroup, BaseAdapter baseAdapter, boolean z) {
        for (int i = 0; i < baseAdapter.getCount(); i++) {
            FrameLayout frameLayout = (FrameLayout) baseAdapter.getView(i, null, viewGroup);
            viewGroup.addView(frameLayout, i);
            WallpaperTileInfo wallpaperTileInfo = (WallpaperTileInfo) baseAdapter.getItem(i);
            frameLayout.setTag(wallpaperTileInfo);
            wallpaperTileInfo.setView(frameLayout);
            if (z) {
                addLongPressHandler(frameLayout);
            }
            frameLayout.setOnClickListener(this.mThumbnailOnClickListener);
        }
    }

    void selectTile(View view) {
        if (this.mSelectedTile != null) {
            this.mSelectedTile.setSelected(false);
            this.mSelectedTile = null;
        }
        this.mSelectedTile = view;
        view.setSelected(true);
        this.mSelectedIndex = this.mWallpapersView.indexOfChild(view);
        view.announceForAccessibility(getContext().getString(2131558551, view.getContentDescription()));
    }

    protected void setSystemWallpaperVisiblity(boolean z) {
        if (z) {
            changeWallpaperFlags(z);
        } else {
            this.mCropView.setVisibility(0);
        }
        this.mCropView.postDelayed(new Runnable(this, z) { // from class: com.android.launcher3.WallpaperPickerActivity.1
            final WallpaperPickerActivity this$0;
            final boolean val$visible;

            {
                this.this$0 = this;
                this.val$visible = z;
            }

            @Override // java.lang.Runnable
            public void run() {
                if (this.val$visible) {
                    this.this$0.mCropView.setVisibility(4);
                } else {
                    this.this$0.changeWallpaperFlags(this.val$visible);
                }
            }
        }, 200L);
    }

    public void setWallpaperButtonEnabled(boolean z) {
        this.mSetWallpaperButton.setEnabled(z);
    }

    public void startActivityForResultSafely(Intent intent, int i) {
        Utilities.startActivityForResultSafely(getActivity(), intent, i);
    }

    void updateTileIndices() {
        LinearLayout linearLayout;
        int i;
        int childCount;
        LinearLayout linearLayout2 = (LinearLayout) findViewById(2131296320);
        int childCount2 = linearLayout2.getChildCount();
        Resources resources = getResources();
        int i2 = 0;
        for (int i3 = 0; i3 < 2; i3++) {
            int i4 = 0;
            for (int i5 = 0; i5 < childCount2; i5++) {
                View childAt = linearLayout2.getChildAt(i5);
                if (childAt.getTag() instanceof WallpaperTileInfo) {
                    linearLayout = linearLayout2;
                    i = i5;
                    childCount = i5 + 1;
                } else {
                    linearLayout = (LinearLayout) childAt;
                    i = 0;
                    childCount = linearLayout.getChildCount();
                }
                while (i < childCount) {
                    WallpaperTileInfo wallpaperTileInfo = (WallpaperTileInfo) linearLayout.getChildAt(i).getTag();
                    int i6 = i2;
                    int i7 = i4;
                    if (wallpaperTileInfo != null) {
                        i6 = i2;
                        i7 = i4;
                        if (wallpaperTileInfo.isNamelessWallpaper()) {
                            if (i3 == 0) {
                                i6 = i2 + 1;
                                i7 = i4;
                            } else {
                                i7 = i4 + 1;
                                wallpaperTileInfo.onIndexUpdated(resources.getString(2131558550, Integer.valueOf(i7), Integer.valueOf(i2)));
                                i6 = i2;
                            }
                        }
                    }
                    i++;
                    i2 = i6;
                    i4 = i7;
                }
            }
        }
    }
}
