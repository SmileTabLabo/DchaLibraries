package com.android.launcher3;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.UserHandle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.LongSparseArray;
import com.android.launcher3.LauncherSettings;
import com.android.launcher3.compat.AppWidgetManagerCompat;
import com.android.launcher3.compat.ShortcutConfigActivityInfo;
import com.android.launcher3.compat.UserManagerCompat;
import com.android.launcher3.graphics.LauncherIcons;
import com.android.launcher3.graphics.ShadowGenerator;
import com.android.launcher3.model.WidgetItem;
import com.android.launcher3.util.ComponentKey;
import com.android.launcher3.util.PackageUserKey;
import com.android.launcher3.util.Preconditions;
import com.android.launcher3.util.SQLiteCacheHelper;
import com.android.launcher3.widget.WidgetCell;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
/* loaded from: classes.dex */
public class WidgetPreviewLoader {
    private static final boolean DEBUG = false;
    private static final String TAG = "WidgetPreviewLoader";
    private final Context mContext;
    private final CacheDb mDb;
    private final IconCache mIconCache;
    private final UserManagerCompat mUserManager;
    private final AppWidgetManagerCompat mWidgetManager;
    private final HashMap<String, long[]> mPackageVersions = new HashMap<>();
    final Set<Bitmap> mUnusedBitmaps = Collections.newSetFromMap(new WeakHashMap());
    private final MainThreadExecutor mMainThreadExecutor = new MainThreadExecutor();
    final Handler mWorkerHandler = new Handler(LauncherModel.getWorkerLooper());

    public WidgetPreviewLoader(Context context, IconCache iconCache) {
        this.mContext = context;
        this.mIconCache = iconCache;
        this.mWidgetManager = AppWidgetManagerCompat.getInstance(context);
        this.mUserManager = UserManagerCompat.getInstance(context);
        this.mDb = new CacheDb(context);
    }

    public CancellationSignal getPreview(WidgetItem widgetItem, int i, int i2, WidgetCell widgetCell) {
        PreviewLoadTask previewLoadTask = new PreviewLoadTask(new WidgetCacheKey(widgetItem.componentName, widgetItem.user, i + "x" + i2), widgetItem, i, i2, widgetCell);
        previewLoadTask.executeOnExecutor(Utilities.THREAD_POOL_EXECUTOR, new Void[0]);
        CancellationSignal cancellationSignal = new CancellationSignal();
        cancellationSignal.setOnCancelListener(previewLoadTask);
        return cancellationSignal;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class CacheDb extends SQLiteCacheHelper {
        private static final String COLUMN_COMPONENT = "componentName";
        private static final String COLUMN_LAST_UPDATED = "lastUpdated";
        private static final String COLUMN_PACKAGE = "packageName";
        private static final String COLUMN_PREVIEW_BITMAP = "preview_bitmap";
        private static final String COLUMN_SIZE = "size";
        private static final String COLUMN_USER = "profileId";
        private static final String COLUMN_VERSION = "version";
        private static final int DB_VERSION = 9;
        private static final String TABLE_NAME = "shortcut_and_widget_previews";

        public CacheDb(Context context) {
            super(context, LauncherFiles.WIDGET_PREVIEWS_DB, 9, TABLE_NAME);
        }

        @Override // com.android.launcher3.util.SQLiteCacheHelper
        public void onCreateTable(SQLiteDatabase sQLiteDatabase) {
            sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS shortcut_and_widget_previews (componentName TEXT NOT NULL, profileId INTEGER NOT NULL, size TEXT NOT NULL, packageName TEXT NOT NULL, lastUpdated INTEGER NOT NULL DEFAULT 0, version INTEGER NOT NULL DEFAULT 0, preview_bitmap BLOB, PRIMARY KEY (componentName, profileId, size) );");
        }
    }

    void writeToDb(WidgetCacheKey widgetCacheKey, long[] jArr, Bitmap bitmap) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("componentName", widgetCacheKey.componentName.flattenToShortString());
        contentValues.put(LauncherSettings.Favorites.PROFILE_ID, Long.valueOf(this.mUserManager.getSerialNumberForUser(widgetCacheKey.user)));
        contentValues.put("size", widgetCacheKey.size);
        contentValues.put("packageName", widgetCacheKey.componentName.getPackageName());
        contentValues.put("version", Long.valueOf(jArr[0]));
        contentValues.put("lastUpdated", Long.valueOf(jArr[1]));
        contentValues.put("preview_bitmap", Utilities.flattenBitmap(bitmap));
        this.mDb.insertOrReplace(contentValues);
    }

    public void removePackage(String str, UserHandle userHandle) {
        removePackage(str, userHandle, this.mUserManager.getSerialNumberForUser(userHandle));
    }

    private void removePackage(String str, UserHandle userHandle, long j) {
        synchronized (this.mPackageVersions) {
            this.mPackageVersions.remove(str);
        }
        this.mDb.delete("packageName = ? AND profileId = ?", new String[]{str, Long.toString(j)});
    }

    public void removeObsoletePreviews(ArrayList<? extends ComponentKey> arrayList, @Nullable PackageUserKey packageUserKey) {
        Cursor cursor;
        int i;
        Preconditions.assertWorkerThread();
        LongSparseArray longSparseArray = new LongSparseArray();
        Iterator<? extends ComponentKey> it = arrayList.iterator();
        while (it.hasNext()) {
            ComponentKey next = it.next();
            long serialNumberForUser = this.mUserManager.getSerialNumberForUser(next.user);
            HashSet hashSet = (HashSet) longSparseArray.get(serialNumberForUser);
            if (hashSet == null) {
                hashSet = new HashSet();
                longSparseArray.put(serialNumberForUser, hashSet);
            }
            hashSet.add(next.componentName.getPackageName());
        }
        LongSparseArray longSparseArray2 = new LongSparseArray();
        long serialNumberForUser2 = packageUserKey == null ? 0L : this.mUserManager.getSerialNumberForUser(packageUserKey.mUser);
        Cursor cursor2 = null;
        try {
            try {
                cursor = this.mDb.query(new String[]{LauncherSettings.Favorites.PROFILE_ID, "packageName", "lastUpdated", "version"}, null, null);
                while (true) {
                    try {
                        if (!cursor.moveToNext()) {
                            break;
                        }
                        long j = cursor.getLong(0);
                        String string = cursor.getString(1);
                        long j2 = cursor.getLong(2);
                        long j3 = cursor.getLong(3);
                        if (packageUserKey == null || (string.equals(packageUserKey.mPackageName) && j == serialNumberForUser2)) {
                            HashSet hashSet2 = (HashSet) longSparseArray.get(j);
                            if (hashSet2 != null && hashSet2.contains(string)) {
                                long[] packageVersion = getPackageVersion(string);
                                if (packageVersion[0] == j3 && packageVersion[1] == j2) {
                                }
                            }
                            HashSet hashSet3 = (HashSet) longSparseArray2.get(j);
                            if (hashSet3 == null) {
                                hashSet3 = new HashSet();
                                longSparseArray2.put(j, hashSet3);
                            }
                            hashSet3.add(string);
                        }
                    } catch (SQLException e) {
                        e = e;
                        cursor2 = cursor;
                        Log.e(TAG, "Error updating widget previews", e);
                        if (cursor2 != null) {
                            cursor2.close();
                            return;
                        }
                        return;
                    } catch (Throwable th) {
                        th = th;
                        if (cursor != null) {
                            cursor.close();
                        }
                        throw th;
                    }
                }
                for (i = 0; i < longSparseArray2.size(); i++) {
                    long keyAt = longSparseArray2.keyAt(i);
                    UserHandle userForSerialNumber = this.mUserManager.getUserForSerialNumber(keyAt);
                    Iterator it2 = ((HashSet) longSparseArray2.valueAt(i)).iterator();
                    while (it2.hasNext()) {
                        removePackage((String) it2.next(), userForSerialNumber, keyAt);
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
            } catch (SQLException e2) {
                e = e2;
            }
        } catch (Throwable th2) {
            th = th2;
            cursor = cursor2;
        }
    }

    /* JADX WARN: Code restructure failed: missing block: B:23:0x0068, code lost:
        if (r10 != null) goto L29;
     */
    /* JADX WARN: Code restructure failed: missing block: B:32:0x0078, code lost:
        if (r10 == null) goto L28;
     */
    /* JADX WARN: Code restructure failed: missing block: B:33:0x007a, code lost:
        r10.close();
     */
    /* JADX WARN: Code restructure failed: missing block: B:34:0x007d, code lost:
        return null;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    Bitmap readFromDb(WidgetCacheKey widgetCacheKey, Bitmap bitmap, PreviewLoadTask previewLoadTask) {
        Cursor cursor;
        Cursor cursor2 = null;
        try {
            cursor = this.mDb.query(new String[]{"preview_bitmap"}, "componentName = ? AND profileId = ? AND size = ?", new String[]{widgetCacheKey.componentName.flattenToShortString(), Long.toString(this.mUserManager.getSerialNumberForUser(widgetCacheKey.user)), widgetCacheKey.size});
            try {
                try {
                    if (previewLoadTask.isCancelled()) {
                        if (cursor != null) {
                            cursor.close();
                        }
                        return null;
                    } else if (cursor.moveToNext()) {
                        byte[] blob = cursor.getBlob(0);
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inBitmap = bitmap;
                        try {
                            if (!previewLoadTask.isCancelled()) {
                                Bitmap decodeByteArray = BitmapFactory.decodeByteArray(blob, 0, blob.length, options);
                                if (cursor != null) {
                                    cursor.close();
                                }
                                return decodeByteArray;
                            }
                        } catch (Exception e) {
                            if (cursor != null) {
                                cursor.close();
                            }
                            return null;
                        }
                    }
                } catch (SQLException e2) {
                    e = e2;
                    Log.w(TAG, "Error loading preview from DB", e);
                }
            } catch (Throwable th) {
                th = th;
                cursor2 = cursor;
                if (cursor2 != null) {
                    cursor2.close();
                }
                throw th;
            }
        } catch (SQLException e3) {
            e = e3;
            cursor = null;
        } catch (Throwable th2) {
            th = th2;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Bitmap generatePreview(BaseActivity baseActivity, WidgetItem widgetItem, Bitmap bitmap, int i, int i2) {
        if (widgetItem.widgetInfo != null) {
            return generateWidgetPreview(baseActivity, widgetItem.widgetInfo, i, bitmap, null);
        }
        return generateShortcutPreview(baseActivity, widgetItem.activityInfo, i, i2, bitmap);
    }

    public Bitmap generateWidgetPreview(BaseActivity baseActivity, LauncherAppWidgetProviderInfo launcherAppWidgetProviderInfo, int i, Bitmap bitmap, int[] iArr) {
        Drawable drawable;
        int i2;
        int i3;
        float f;
        Bitmap bitmap2 = bitmap;
        int i4 = i < 0 ? Integer.MAX_VALUE : i;
        if (launcherAppWidgetProviderInfo.previewImage != 0) {
            try {
                drawable = launcherAppWidgetProviderInfo.loadPreviewImage(this.mContext, 0);
            } catch (OutOfMemoryError e) {
                Log.w(TAG, "Error loading widget preview for: " + launcherAppWidgetProviderInfo.provider, e);
                drawable = null;
            }
            if (drawable != null) {
                drawable = mutateOnMainThread(drawable);
            } else {
                Log.w(TAG, "Can't load widget preview drawable 0x" + Integer.toHexString(launcherAppWidgetProviderInfo.previewImage) + " for provider: " + launcherAppWidgetProviderInfo.provider);
            }
        } else {
            drawable = null;
        }
        boolean z = drawable != null;
        int i5 = launcherAppWidgetProviderInfo.spanX;
        int i6 = launcherAppWidgetProviderInfo.spanY;
        if (z && drawable.getIntrinsicWidth() > 0 && drawable.getIntrinsicHeight() > 0) {
            i3 = drawable.getIntrinsicWidth();
            i2 = drawable.getIntrinsicHeight();
        } else {
            DeviceProfile deviceProfile = baseActivity.getDeviceProfile();
            int min = Math.min(deviceProfile.cellWidthPx, deviceProfile.cellHeightPx);
            i2 = min * i6;
            i3 = min * i5;
        }
        if (iArr != null) {
            iArr[0] = i3;
        }
        if (i3 > i4) {
            f = i4 / i3;
        } else {
            f = 1.0f;
        }
        if (f != 1.0f) {
            i3 = Math.max((int) (i3 * f), 1);
            i2 = Math.max((int) (i2 * f), 1);
        }
        Canvas canvas = new Canvas();
        if (bitmap2 == null) {
            bitmap2 = Bitmap.createBitmap(i3, i2, Bitmap.Config.ARGB_8888);
            canvas.setBitmap(bitmap2);
        } else {
            if (bitmap.getHeight() > i2) {
                bitmap2.reconfigure(bitmap.getWidth(), i2, bitmap.getConfig());
            }
            canvas.setBitmap(bitmap2);
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        }
        int width = (bitmap2.getWidth() - i3) / 2;
        if (z) {
            drawable.setBounds(width, 0, i3 + width, i2);
            drawable.draw(canvas);
        } else {
            RectF drawBoxWithShadow = drawBoxWithShadow(canvas, i3, i2);
            Paint paint = new Paint(1);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(this.mContext.getResources().getDimension(R.dimen.widget_preview_cell_divider_width));
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            float f2 = drawBoxWithShadow.left;
            float width2 = drawBoxWithShadow.width() / i5;
            float f3 = f2;
            int i7 = 1;
            while (i7 < i5) {
                float f4 = f3 + width2;
                canvas.drawLine(f4, 0.0f, f4, i2, paint);
                i7++;
                f3 = f4;
            }
            float f5 = drawBoxWithShadow.top;
            float height = drawBoxWithShadow.height() / i6;
            for (int i8 = 1; i8 < i6; i8++) {
                f5 += height;
                canvas.drawLine(0.0f, f5, i3, f5, paint);
            }
            try {
                Drawable fullResIcon = this.mIconCache.getFullResIcon(launcherAppWidgetProviderInfo.provider.getPackageName(), launcherAppWidgetProviderInfo.icon);
                if (fullResIcon != null) {
                    int min2 = (int) Math.min(baseActivity.getDeviceProfile().iconSizePx * f, Math.min(drawBoxWithShadow.width(), drawBoxWithShadow.height()));
                    Drawable mutateOnMainThread = mutateOnMainThread(fullResIcon);
                    int i9 = (i3 - min2) / 2;
                    int i10 = (i2 - min2) / 2;
                    mutateOnMainThread.setBounds(i9, i10, i9 + min2, min2 + i10);
                    mutateOnMainThread.draw(canvas);
                }
            } catch (Resources.NotFoundException e2) {
            }
            canvas.setBitmap(null);
        }
        return bitmap2;
    }

    private RectF drawBoxWithShadow(Canvas canvas, int i, int i2) {
        Resources resources = this.mContext.getResources();
        ShadowGenerator.Builder builder = new ShadowGenerator.Builder(-1);
        builder.shadowBlur = resources.getDimension(R.dimen.widget_preview_shadow_blur);
        builder.radius = resources.getDimension(R.dimen.widget_preview_corner_radius);
        builder.keyShadowDistance = resources.getDimension(R.dimen.widget_preview_key_shadow_distance);
        builder.bounds.set(builder.shadowBlur, builder.shadowBlur, i - builder.shadowBlur, (i2 - builder.shadowBlur) - builder.keyShadowDistance);
        builder.drawShadow(canvas);
        return builder.bounds;
    }

    private Bitmap generateShortcutPreview(BaseActivity baseActivity, ShortcutConfigActivityInfo shortcutConfigActivityInfo, int i, int i2, Bitmap bitmap) {
        int i3 = baseActivity.getDeviceProfile().iconSizePx;
        int dimensionPixelSize = baseActivity.getResources().getDimensionPixelSize(R.dimen.widget_preview_shortcut_padding);
        int i4 = (2 * dimensionPixelSize) + i3;
        if (i2 < i4 || i < i4) {
            throw new RuntimeException("Max size is too small for preview");
        }
        Canvas canvas = new Canvas();
        if (bitmap == null || bitmap.getWidth() < i4 || bitmap.getHeight() < i4) {
            bitmap = Bitmap.createBitmap(i4, i4, Bitmap.Config.ARGB_8888);
            canvas.setBitmap(bitmap);
        } else {
            if (bitmap.getWidth() > i4 || bitmap.getHeight() > i4) {
                bitmap.reconfigure(i4, i4, bitmap.getConfig());
            }
            canvas.setBitmap(bitmap);
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        }
        RectF drawBoxWithShadow = drawBoxWithShadow(canvas, i4, i4);
        LauncherIcons obtain = LauncherIcons.obtain(this.mContext);
        Bitmap createScaledBitmapWithoutShadow = obtain.createScaledBitmapWithoutShadow(mutateOnMainThread(shortcutConfigActivityInfo.getFullResIcon(this.mIconCache)), 0);
        obtain.recycle();
        Rect rect = new Rect(0, 0, createScaledBitmapWithoutShadow.getWidth(), createScaledBitmapWithoutShadow.getHeight());
        float f = i3;
        drawBoxWithShadow.set(0.0f, 0.0f, f, f);
        float f2 = dimensionPixelSize;
        drawBoxWithShadow.offset(f2, f2);
        canvas.drawBitmap(createScaledBitmapWithoutShadow, rect, drawBoxWithShadow, new Paint(3));
        canvas.setBitmap(null);
        return bitmap;
    }

    private Drawable mutateOnMainThread(final Drawable drawable) {
        try {
            return (Drawable) this.mMainThreadExecutor.submit(new Callable<Drawable>() { // from class: com.android.launcher3.WidgetPreviewLoader.1
                /* JADX WARN: Can't rename method to resolve collision */
                @Override // java.util.concurrent.Callable
                public Drawable call() throws Exception {
                    return drawable.mutate();
                }
            }).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (ExecutionException e2) {
            throw new RuntimeException(e2);
        }
    }

    long[] getPackageVersion(String str) {
        long[] jArr;
        synchronized (this.mPackageVersions) {
            jArr = this.mPackageVersions.get(str);
            if (jArr == null) {
                jArr = new long[2];
                try {
                    PackageInfo packageInfo = this.mContext.getPackageManager().getPackageInfo(str, 8192);
                    jArr[0] = packageInfo.versionCode;
                    jArr[1] = packageInfo.lastUpdateTime;
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e(TAG, "PackageInfo not found", e);
                }
                this.mPackageVersions.put(str, jArr);
            }
        }
        return jArr;
    }

    /* loaded from: classes.dex */
    public class PreviewLoadTask extends AsyncTask<Void, Void, Bitmap> implements CancellationSignal.OnCancelListener {
        private final BaseActivity mActivity;
        Bitmap mBitmapToRecycle;
        private final WidgetCell mCaller;
        private final WidgetItem mInfo;
        final WidgetCacheKey mKey;
        private final int mPreviewHeight;
        private final int mPreviewWidth;
        long[] mVersions;

        PreviewLoadTask(WidgetCacheKey widgetCacheKey, WidgetItem widgetItem, int i, int i2, WidgetCell widgetCell) {
            this.mKey = widgetCacheKey;
            this.mInfo = widgetItem;
            this.mPreviewHeight = i2;
            this.mPreviewWidth = i;
            this.mCaller = widgetCell;
            this.mActivity = BaseActivity.fromContext(this.mCaller.getContext());
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public Bitmap doInBackground(Void... voidArr) {
            Bitmap bitmap;
            if (isCancelled()) {
                return null;
            }
            synchronized (WidgetPreviewLoader.this.mUnusedBitmaps) {
                Iterator<Bitmap> it = WidgetPreviewLoader.this.mUnusedBitmaps.iterator();
                while (true) {
                    if (it.hasNext()) {
                        bitmap = it.next();
                        if (bitmap != null && bitmap.isMutable() && bitmap.getWidth() == this.mPreviewWidth && bitmap.getHeight() == this.mPreviewHeight) {
                            WidgetPreviewLoader.this.mUnusedBitmaps.remove(bitmap);
                            break;
                        }
                    } else {
                        bitmap = null;
                        break;
                    }
                }
            }
            if (bitmap == null) {
                bitmap = Bitmap.createBitmap(this.mPreviewWidth, this.mPreviewHeight, Bitmap.Config.ARGB_8888);
            }
            Bitmap bitmap2 = bitmap;
            if (isCancelled()) {
                return bitmap2;
            }
            Bitmap readFromDb = WidgetPreviewLoader.this.readFromDb(this.mKey, bitmap2, this);
            if (!isCancelled() && readFromDb == null) {
                this.mVersions = this.mInfo.activityInfo == null || this.mInfo.activityInfo.isPersistable() ? WidgetPreviewLoader.this.getPackageVersion(this.mKey.componentName.getPackageName()) : null;
                return WidgetPreviewLoader.this.generatePreview(this.mActivity, this.mInfo, bitmap2, this.mPreviewWidth, this.mPreviewHeight);
            }
            return readFromDb;
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onPostExecute(final Bitmap bitmap) {
            this.mCaller.applyPreview(bitmap);
            if (this.mVersions != null) {
                WidgetPreviewLoader.this.mWorkerHandler.post(new Runnable() { // from class: com.android.launcher3.WidgetPreviewLoader.PreviewLoadTask.1
                    @Override // java.lang.Runnable
                    public void run() {
                        if (!PreviewLoadTask.this.isCancelled()) {
                            WidgetPreviewLoader.this.writeToDb(PreviewLoadTask.this.mKey, PreviewLoadTask.this.mVersions, bitmap);
                            PreviewLoadTask.this.mBitmapToRecycle = bitmap;
                            return;
                        }
                        synchronized (WidgetPreviewLoader.this.mUnusedBitmaps) {
                            WidgetPreviewLoader.this.mUnusedBitmaps.add(bitmap);
                        }
                    }
                });
            } else {
                this.mBitmapToRecycle = bitmap;
            }
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onCancelled(final Bitmap bitmap) {
            if (bitmap != null) {
                WidgetPreviewLoader.this.mWorkerHandler.post(new Runnable() { // from class: com.android.launcher3.WidgetPreviewLoader.PreviewLoadTask.2
                    @Override // java.lang.Runnable
                    public void run() {
                        synchronized (WidgetPreviewLoader.this.mUnusedBitmaps) {
                            WidgetPreviewLoader.this.mUnusedBitmaps.add(bitmap);
                        }
                    }
                });
            }
        }

        @Override // android.os.CancellationSignal.OnCancelListener
        public void onCancel() {
            cancel(true);
            if (this.mBitmapToRecycle != null) {
                WidgetPreviewLoader.this.mWorkerHandler.post(new Runnable() { // from class: com.android.launcher3.WidgetPreviewLoader.PreviewLoadTask.3
                    @Override // java.lang.Runnable
                    public void run() {
                        synchronized (WidgetPreviewLoader.this.mUnusedBitmaps) {
                            WidgetPreviewLoader.this.mUnusedBitmaps.add(PreviewLoadTask.this.mBitmapToRecycle);
                        }
                        PreviewLoadTask.this.mBitmapToRecycle = null;
                    }
                });
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static final class WidgetCacheKey extends ComponentKey {
        final String size;

        public WidgetCacheKey(ComponentName componentName, UserHandle userHandle, String str) {
            super(componentName, userHandle);
            this.size = str;
        }

        @Override // com.android.launcher3.util.ComponentKey
        public int hashCode() {
            return super.hashCode() ^ this.size.hashCode();
        }

        @Override // com.android.launcher3.util.ComponentKey
        public boolean equals(Object obj) {
            return super.equals(obj) && ((WidgetCacheKey) obj).size.equals(this.size);
        }
    }
}
