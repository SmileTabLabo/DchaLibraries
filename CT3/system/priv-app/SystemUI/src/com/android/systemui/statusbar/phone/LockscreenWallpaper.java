package com.android.systemui.statusbar.phone;

import android.app.ActivityManager;
import android.app.IWallpaperManager;
import android.app.IWallpaperManagerCallback;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableWrapper;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.util.Log;
import libcore.io.IoUtils;
/* loaded from: a.zip:com/android/systemui/statusbar/phone/LockscreenWallpaper.class */
public class LockscreenWallpaper extends IWallpaperManagerCallback.Stub implements Runnable {
    private final PhoneStatusBar mBar;
    private Bitmap mCache;
    private boolean mCached;
    private int mCurrentUserId = ActivityManager.getCurrentUser();
    private final Handler mH;
    private AsyncTask<Void, Void, LoaderResult> mLoader;
    private UserHandle mSelectedUser;
    private final WallpaperManager mWallpaperManager;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/statusbar/phone/LockscreenWallpaper$LoaderResult.class */
    public static class LoaderResult {
        public final Bitmap bitmap;
        public final boolean success;

        LoaderResult(boolean z, Bitmap bitmap) {
            this.success = z;
            this.bitmap = bitmap;
        }

        static LoaderResult fail() {
            return new LoaderResult(false, null);
        }

        static LoaderResult success(Bitmap bitmap) {
            return new LoaderResult(true, bitmap);
        }
    }

    /* loaded from: a.zip:com/android/systemui/statusbar/phone/LockscreenWallpaper$WallpaperDrawable.class */
    public static class WallpaperDrawable extends DrawableWrapper {
        private final ConstantState mState;
        private final Rect mTmpRect;

        /* JADX INFO: Access modifiers changed from: package-private */
        /* loaded from: a.zip:com/android/systemui/statusbar/phone/LockscreenWallpaper$WallpaperDrawable$ConstantState.class */
        public static class ConstantState extends Drawable.ConstantState {
            private final Bitmap mBackground;

            ConstantState(Bitmap bitmap) {
                this.mBackground = bitmap;
            }

            @Override // android.graphics.drawable.Drawable.ConstantState
            public int getChangingConfigurations() {
                return 0;
            }

            @Override // android.graphics.drawable.Drawable.ConstantState
            public Drawable newDrawable() {
                return newDrawable(null);
            }

            @Override // android.graphics.drawable.Drawable.ConstantState
            public Drawable newDrawable(Resources resources) {
                return new WallpaperDrawable(resources, this, null);
            }
        }

        public WallpaperDrawable(Resources resources, Bitmap bitmap) {
            this(resources, new ConstantState(bitmap));
        }

        private WallpaperDrawable(Resources resources, ConstantState constantState) {
            super(new BitmapDrawable(resources, constantState.mBackground));
            this.mTmpRect = new Rect();
            this.mState = constantState;
        }

        /* synthetic */ WallpaperDrawable(Resources resources, ConstantState constantState, WallpaperDrawable wallpaperDrawable) {
            this(resources, constantState);
        }

        @Override // android.graphics.drawable.DrawableWrapper, android.graphics.drawable.Drawable
        public ConstantState getConstantState() {
            return this.mState;
        }

        @Override // android.graphics.drawable.DrawableWrapper, android.graphics.drawable.Drawable
        public int getIntrinsicHeight() {
            return -1;
        }

        @Override // android.graphics.drawable.DrawableWrapper, android.graphics.drawable.Drawable
        public int getIntrinsicWidth() {
            return -1;
        }

        @Override // android.graphics.drawable.DrawableWrapper, android.graphics.drawable.Drawable
        protected void onBoundsChange(Rect rect) {
            int width = getBounds().width();
            int height = getBounds().height();
            int width2 = this.mState.mBackground.getWidth();
            int height2 = this.mState.mBackground.getHeight();
            float f = width2 * height > width * height2 ? height / height2 : width / width2;
            float f2 = f;
            if (f <= 1.0f) {
                f2 = 1.0f;
            }
            float f3 = (height - (height2 * f2)) * 0.5f;
            this.mTmpRect.set(rect.left, rect.top + Math.round(f3), rect.left + Math.round(width2 * f2), rect.top + Math.round((height2 * f2) + f3));
            super.onBoundsChange(this.mTmpRect);
        }
    }

    public LockscreenWallpaper(Context context, PhoneStatusBar phoneStatusBar, Handler handler) {
        this.mBar = phoneStatusBar;
        this.mH = handler;
        this.mWallpaperManager = (WallpaperManager) context.getSystemService("wallpaper");
        try {
            IWallpaperManager.Stub.asInterface(ServiceManager.getService("wallpaper")).setLockWallpaperCallback(this);
        } catch (RemoteException e) {
            Log.e("LockscreenWallpaper", "System dead?" + e);
        }
    }

    private void postUpdateWallpaper() {
        this.mH.removeCallbacks(this);
        this.mH.post(this);
    }

    public Bitmap getBitmap() {
        if (this.mCached) {
            return this.mCache;
        }
        if (!this.mWallpaperManager.isWallpaperSupported()) {
            this.mCached = true;
            this.mCache = null;
            return null;
        }
        LoaderResult loadBitmap = loadBitmap(this.mCurrentUserId, this.mSelectedUser);
        if (loadBitmap.success) {
            this.mCached = true;
            this.mCache = loadBitmap.bitmap;
        }
        return this.mCache;
    }

    public LoaderResult loadBitmap(int i, UserHandle userHandle) {
        ParcelFileDescriptor wallpaperFile = this.mWallpaperManager.getWallpaperFile(2, userHandle != null ? userHandle.getIdentifier() : i);
        try {
            if (wallpaperFile != null) {
                return LoaderResult.success(BitmapFactory.decodeFileDescriptor(wallpaperFile.getFileDescriptor(), null, new BitmapFactory.Options()));
            }
            return (userHandle == null || userHandle.getIdentifier() == i) ? LoaderResult.success(null) : LoaderResult.success(this.mWallpaperManager.getBitmapAsUser(userHandle.getIdentifier()));
        } catch (OutOfMemoryError e) {
            Log.w("LockscreenWallpaper", "Can't decode file", e);
            return LoaderResult.fail();
        } finally {
            IoUtils.closeQuietly(wallpaperFile);
        }
    }

    public void onWallpaperChanged() {
        postUpdateWallpaper();
    }

    /* JADX WARN: Type inference failed for: r1v0, types: [com.android.systemui.statusbar.phone.LockscreenWallpaper$1] */
    @Override // java.lang.Runnable
    public void run() {
        if (this.mLoader != null) {
            this.mLoader.cancel(false);
        }
        this.mLoader = new AsyncTask<Void, Void, LoaderResult>(this, this.mCurrentUserId, this.mSelectedUser) { // from class: com.android.systemui.statusbar.phone.LockscreenWallpaper.1
            final LockscreenWallpaper this$0;
            final int val$currentUser;
            final UserHandle val$selectedUser;

            {
                this.this$0 = this;
                this.val$currentUser = r5;
                this.val$selectedUser = r6;
            }

            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public LoaderResult doInBackground(Void... voidArr) {
                return this.this$0.loadBitmap(this.val$currentUser, this.val$selectedUser);
            }

            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public void onPostExecute(LoaderResult loaderResult) {
                super.onPostExecute((AnonymousClass1) loaderResult);
                if (isCancelled()) {
                    return;
                }
                if (loaderResult.success) {
                    this.this$0.mCached = true;
                    this.this$0.mCache = loaderResult.bitmap;
                    this.this$0.mBar.updateMediaMetaData(true, true);
                }
                this.this$0.mLoader = null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
    }

    public void setCurrentUser(int i) {
        if (i != this.mCurrentUserId) {
            this.mCached = false;
            this.mCurrentUserId = i;
        }
    }
}
