package com.android.launcher3;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.util.LongSparseArray;
import com.android.launcher3.compat.AppWidgetManagerCompat;
import com.android.launcher3.compat.UserHandleCompat;
import com.android.launcher3.compat.UserManagerCompat;
import com.android.launcher3.util.ComponentKey;
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
/* loaded from: a.zip:com/android/launcher3/WidgetPreviewLoader.class */
public class WidgetPreviewLoader {
    private final Context mContext;
    private final CacheDb mDb;
    private final IconCache mIconCache;
    private final int mProfileBadgeMargin;
    private final UserManagerCompat mUserManager;
    private final AppWidgetManagerCompat mWidgetManager;
    private final HashMap<String, long[]> mPackageVersions = new HashMap<>();
    final Set<Bitmap> mUnusedBitmaps = Collections.newSetFromMap(new WeakHashMap());
    private final MainThreadExecutor mMainThreadExecutor = new MainThreadExecutor();
    final Handler mWorkerHandler = new Handler(LauncherModel.getWorkerLooper());

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/launcher3/WidgetPreviewLoader$CacheDb.class */
    public static class CacheDb extends SQLiteCacheHelper {
        public CacheDb(Context context) {
            super(context, "widgetpreviews.db", 4, "shortcut_and_widget_previews");
        }

        @Override // com.android.launcher3.util.SQLiteCacheHelper
        public void onCreateTable(SQLiteDatabase sQLiteDatabase) {
            sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS shortcut_and_widget_previews (componentName TEXT NOT NULL, profileId INTEGER NOT NULL, size TEXT NOT NULL, packageName TEXT NOT NULL, lastUpdated INTEGER NOT NULL DEFAULT 0, version INTEGER NOT NULL DEFAULT 0, preview_bitmap BLOB, PRIMARY KEY (componentName, profileId, size) );");
        }
    }

    /* loaded from: a.zip:com/android/launcher3/WidgetPreviewLoader$PreviewLoadRequest.class */
    public class PreviewLoadRequest {
        final PreviewLoadTask mTask;
        final WidgetPreviewLoader this$0;

        public PreviewLoadRequest(WidgetPreviewLoader widgetPreviewLoader, PreviewLoadTask previewLoadTask) {
            this.this$0 = widgetPreviewLoader;
            this.mTask = previewLoadTask;
        }

        public void cleanup() {
            if (this.mTask != null) {
                this.mTask.cancel(true);
            }
            if (this.mTask.mBitmapToRecycle != null) {
                this.this$0.mWorkerHandler.post(new Runnable(this) { // from class: com.android.launcher3.WidgetPreviewLoader.PreviewLoadRequest.1
                    final PreviewLoadRequest this$1;

                    {
                        this.this$1 = this;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        synchronized (this.this$1.this$0.mUnusedBitmaps) {
                            this.this$1.this$0.mUnusedBitmaps.add(this.this$1.mTask.mBitmapToRecycle);
                        }
                        this.this$1.mTask.mBitmapToRecycle = null;
                    }
                });
            }
        }
    }

    /* loaded from: a.zip:com/android/launcher3/WidgetPreviewLoader$PreviewLoadTask.class */
    public class PreviewLoadTask extends AsyncTask<Void, Void, Bitmap> {
        Bitmap mBitmapToRecycle;
        private final WidgetCell mCaller;
        private final Object mInfo;
        final WidgetCacheKey mKey;
        private final int mPreviewHeight;
        private final int mPreviewWidth;
        long[] mVersions;
        final WidgetPreviewLoader this$0;

        PreviewLoadTask(WidgetPreviewLoader widgetPreviewLoader, WidgetCacheKey widgetCacheKey, Object obj, int i, int i2, WidgetCell widgetCell) {
            this.this$0 = widgetPreviewLoader;
            this.mKey = widgetCacheKey;
            this.mInfo = obj;
            this.mPreviewHeight = i2;
            this.mPreviewWidth = i;
            this.mCaller = widgetCell;
        }

        /* JADX INFO: Access modifiers changed from: protected */
        /* JADX WARN: Code restructure failed: missing block: B:22:0x0062, code lost:
            r8 = r0;
         */
        /* JADX WARN: Code restructure failed: missing block: B:23:0x0063, code lost:
            r7.this$0.mUnusedBitmaps.remove(r0);
         */
        @Override // android.os.AsyncTask
        /*
            Code decompiled incorrectly, please refer to instructions dump.
        */
        public Bitmap doInBackground(Void... voidArr) {
            Bitmap bitmap;
            if (isCancelled()) {
                return null;
            }
            synchronized (this.this$0.mUnusedBitmaps) {
                Iterator<T> it = this.this$0.mUnusedBitmaps.iterator();
                while (true) {
                    bitmap = null;
                    if (!it.hasNext()) {
                        break;
                    }
                    Bitmap bitmap2 = (Bitmap) it.next();
                    if (bitmap2 != null && bitmap2.isMutable() && bitmap2.getWidth() == this.mPreviewWidth && bitmap2.getHeight() == this.mPreviewHeight) {
                        break;
                    }
                }
            }
            Bitmap bitmap3 = bitmap;
            if (bitmap == null) {
                bitmap3 = Bitmap.createBitmap(this.mPreviewWidth, this.mPreviewHeight, Bitmap.Config.ARGB_8888);
            }
            if (isCancelled()) {
                return bitmap3;
            }
            Bitmap readFromDb = this.this$0.readFromDb(this.mKey, bitmap3, this);
            Bitmap bitmap4 = readFromDb;
            if (!isCancelled()) {
                bitmap4 = readFromDb;
                if (readFromDb == null) {
                    this.mVersions = this.this$0.getPackageVersion(this.mKey.componentName.getPackageName());
                    bitmap4 = this.this$0.generatePreview((Launcher) this.mCaller.getContext(), this.mInfo, bitmap3, this.mPreviewWidth, this.mPreviewHeight);
                }
            }
            return bitmap4;
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onCancelled(Bitmap bitmap) {
            if (bitmap != null) {
                this.this$0.mWorkerHandler.post(new Runnable(this, bitmap) { // from class: com.android.launcher3.WidgetPreviewLoader.PreviewLoadTask.2
                    final PreviewLoadTask this$1;
                    final Bitmap val$preview;

                    {
                        this.this$1 = this;
                        this.val$preview = bitmap;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        synchronized (this.this$1.this$0.mUnusedBitmaps) {
                            this.this$1.this$0.mUnusedBitmaps.add(this.val$preview);
                        }
                    }
                });
            }
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onPostExecute(Bitmap bitmap) {
            this.mCaller.applyPreview(bitmap);
            if (this.mVersions != null) {
                this.this$0.mWorkerHandler.post(new Runnable(this, bitmap) { // from class: com.android.launcher3.WidgetPreviewLoader.PreviewLoadTask.1
                    final PreviewLoadTask this$1;
                    final Bitmap val$preview;

                    {
                        this.this$1 = this;
                        this.val$preview = bitmap;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        if (this.this$1.isCancelled()) {
                            synchronized (this.this$1.this$0.mUnusedBitmaps) {
                                this.this$1.this$0.mUnusedBitmaps.add(this.val$preview);
                            }
                            return;
                        }
                        this.this$1.this$0.writeToDb(this.this$1.mKey, this.this$1.mVersions, this.val$preview);
                        this.this$1.mBitmapToRecycle = this.val$preview;
                    }
                });
            } else {
                this.mBitmapToRecycle = bitmap;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/launcher3/WidgetPreviewLoader$WidgetCacheKey.class */
    public static final class WidgetCacheKey extends ComponentKey {
        final String size;

        public WidgetCacheKey(ComponentName componentName, UserHandleCompat userHandleCompat, String str) {
            super(componentName, userHandleCompat);
            this.size = str;
        }

        @Override // com.android.launcher3.util.ComponentKey
        public boolean equals(Object obj) {
            return super.equals(obj) ? ((WidgetCacheKey) obj).size.equals(this.size) : false;
        }

        @Override // com.android.launcher3.util.ComponentKey
        public int hashCode() {
            return super.hashCode() ^ this.size.hashCode();
        }
    }

    public WidgetPreviewLoader(Context context, IconCache iconCache) {
        this.mContext = context;
        this.mIconCache = iconCache;
        this.mWidgetManager = AppWidgetManagerCompat.getInstance(context);
        this.mUserManager = UserManagerCompat.getInstance(context);
        this.mDb = new CacheDb(context);
        this.mProfileBadgeMargin = context.getResources().getDimensionPixelSize(2131230804);
    }

    private Bitmap generateShortcutPreview(Launcher launcher, ResolveInfo resolveInfo, int i, int i2, Bitmap bitmap) {
        Canvas canvas = new Canvas();
        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(i, i2, Bitmap.Config.ARGB_8888);
            canvas.setBitmap(bitmap);
        } else if (bitmap.getWidth() != i || bitmap.getHeight() != i2) {
            throw new RuntimeException("Improperly sized bitmap passed as argument");
        } else {
            canvas.setBitmap(bitmap);
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        }
        Drawable mutateOnMainThread = mutateOnMainThread(this.mIconCache.getFullResIcon(resolveInfo.activityInfo));
        mutateOnMainThread.setFilterBitmap(true);
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0.0f);
        mutateOnMainThread.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        mutateOnMainThread.setAlpha(15);
        Resources resources = this.mContext.getResources();
        int dimensionPixelOffset = resources.getDimensionPixelOffset(2131230795);
        int dimensionPixelOffset2 = resources.getDimensionPixelOffset(2131230793);
        int dimensionPixelOffset3 = (i - dimensionPixelOffset2) - resources.getDimensionPixelOffset(2131230794);
        mutateOnMainThread.setBounds(dimensionPixelOffset2, dimensionPixelOffset, dimensionPixelOffset2 + dimensionPixelOffset3, dimensionPixelOffset + dimensionPixelOffset3);
        mutateOnMainThread.draw(canvas);
        int i3 = launcher.getDeviceProfile().iconSizePx;
        mutateOnMainThread.setAlpha(255);
        mutateOnMainThread.setColorFilter(null);
        mutateOnMainThread.setBounds(0, 0, i3, i3);
        mutateOnMainThread.draw(canvas);
        canvas.setBitmap(null);
        return bitmap;
    }

    private WidgetCacheKey getObjectKey(Object obj, String str) {
        if (obj instanceof LauncherAppWidgetProviderInfo) {
            LauncherAppWidgetProviderInfo launcherAppWidgetProviderInfo = (LauncherAppWidgetProviderInfo) obj;
            return new WidgetCacheKey(launcherAppWidgetProviderInfo.provider, this.mWidgetManager.getUser(launcherAppWidgetProviderInfo), str);
        }
        ResolveInfo resolveInfo = (ResolveInfo) obj;
        return new WidgetCacheKey(new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name), UserHandleCompat.myUserHandle(), str);
    }

    private Drawable mutateOnMainThread(Drawable drawable) {
        try {
            return (Drawable) this.mMainThreadExecutor.submit(new Callable<Drawable>(this, drawable) { // from class: com.android.launcher3.WidgetPreviewLoader.1
                final WidgetPreviewLoader this$0;
                final Drawable val$drawable;

                {
                    this.this$0 = this;
                    this.val$drawable = drawable;
                }

                /* JADX WARN: Can't rename method to resolve collision */
                @Override // java.util.concurrent.Callable
                public Drawable call() throws Exception {
                    return this.val$drawable.mutate();
                }
            }).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (ExecutionException e2) {
            throw new RuntimeException(e2);
        }
    }

    private void removePackage(String str, UserHandleCompat userHandleCompat, long j) {
        synchronized (this.mPackageVersions) {
            this.mPackageVersions.remove(str);
        }
        this.mDb.delete("packageName = ? AND profileId = ?", new String[]{str, Long.toString(j)});
    }

    Bitmap generatePreview(Launcher launcher, Object obj, Bitmap bitmap, int i, int i2) {
        return obj instanceof LauncherAppWidgetProviderInfo ? generateWidgetPreview(launcher, (LauncherAppWidgetProviderInfo) obj, i, bitmap, null) : generateShortcutPreview(launcher, (ResolveInfo) obj, i, i2, bitmap);
    }

    public Bitmap generateWidgetPreview(Launcher launcher, LauncherAppWidgetProviderInfo launcherAppWidgetProviderInfo, int i, Bitmap bitmap, int[] iArr) {
        int width;
        int height;
        int i2 = i;
        if (i < 0) {
            i2 = Integer.MAX_VALUE;
        }
        Drawable drawable = null;
        if (launcherAppWidgetProviderInfo.previewImage != 0) {
            drawable = this.mWidgetManager.loadPreview(launcherAppWidgetProviderInfo);
            if (drawable != null) {
                drawable = mutateOnMainThread(drawable);
            } else {
                Log.w("WidgetPreviewLoader", "Can't load widget preview drawable 0x" + Integer.toHexString(launcherAppWidgetProviderInfo.previewImage) + " for provider: " + launcherAppWidgetProviderInfo.provider);
            }
        }
        boolean z = drawable != null;
        int i3 = launcherAppWidgetProviderInfo.spanX;
        int i4 = launcherAppWidgetProviderInfo.spanY;
        Bitmap bitmap2 = null;
        if (z) {
            width = drawable.getIntrinsicWidth();
            height = drawable.getIntrinsicHeight();
        } else {
            bitmap2 = ((BitmapDrawable) this.mContext.getResources().getDrawable(2130837568)).getBitmap();
            width = bitmap2.getWidth() * i3;
            height = bitmap2.getHeight() * i4;
        }
        float f = 1.0f;
        if (iArr != null) {
            iArr[0] = width;
        }
        if (width > i2) {
            f = (i2 - (this.mProfileBadgeMargin * 2)) / width;
        }
        int i5 = height;
        int i6 = width;
        if (f != 1.0f) {
            i6 = (int) (width * f);
            i5 = (int) (height * f);
        }
        Canvas canvas = new Canvas();
        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(i6, i5, Bitmap.Config.ARGB_8888);
            canvas.setBitmap(bitmap);
        } else {
            canvas.setBitmap(bitmap);
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        }
        int width2 = (bitmap.getWidth() - i6) / 2;
        if (z) {
            drawable.setBounds(0, 0, i6, i5);
            drawable.draw(canvas);
        } else {
            Paint paint = new Paint();
            paint.setFilterBitmap(true);
            int i7 = launcher.getDeviceProfile().iconSizePx;
            Rect rect = new Rect(0, 0, bitmap2.getWidth(), bitmap2.getHeight());
            float width3 = f * bitmap2.getWidth();
            float height2 = f * bitmap2.getHeight();
            RectF rectF = new RectF(0.0f, 0.0f, width3, height2);
            float f2 = width2;
            int i8 = 0;
            while (i8 < i3) {
                float f3 = 0.0f;
                int i9 = 0;
                while (i9 < i4) {
                    rectF.offsetTo(f2, f3);
                    canvas.drawBitmap(bitmap2, rect, rectF, paint);
                    i9++;
                    f3 += height2;
                }
                i8++;
                f2 += width3;
            }
            float min = Math.min(Math.min(i6, i5) / ((((int) (i7 * 0.25f)) * 2) + i7), f);
            try {
                Drawable loadIcon = this.mWidgetManager.loadIcon(launcherAppWidgetProviderInfo, this.mIconCache);
                if (loadIcon != null) {
                    Drawable mutateOnMainThread = mutateOnMainThread(loadIcon);
                    int i10 = ((int) ((width3 - (i7 * min)) / 2.0f)) + width2;
                    int i11 = (int) ((height2 - (i7 * min)) / 2.0f);
                    mutateOnMainThread.setBounds(i10, i11, ((int) (i7 * min)) + i10, ((int) (i7 * min)) + i11);
                    mutateOnMainThread.draw(canvas);
                }
            } catch (Resources.NotFoundException e) {
            }
            canvas.setBitmap(null);
        }
        return this.mWidgetManager.getBadgeBitmap(launcherAppWidgetProviderInfo, bitmap, Math.min(bitmap.getWidth(), this.mProfileBadgeMargin + i6), Math.min(bitmap.getHeight(), this.mProfileBadgeMargin + i5));
    }

    long[] getPackageVersion(String str) {
        long[] jArr;
        synchronized (this.mPackageVersions) {
            long[] jArr2 = this.mPackageVersions.get(str);
            jArr = jArr2;
            if (jArr2 == null) {
                jArr = new long[2];
                try {
                    PackageInfo packageInfo = this.mContext.getPackageManager().getPackageInfo(str, 0);
                    jArr[0] = packageInfo.versionCode;
                    jArr[1] = packageInfo.lastUpdateTime;
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e("WidgetPreviewLoader", "PackageInfo not found", e);
                }
                this.mPackageVersions.put(str, jArr);
            }
        }
        return jArr;
    }

    public PreviewLoadRequest getPreview(Object obj, int i, int i2, WidgetCell widgetCell) {
        PreviewLoadTask previewLoadTask = new PreviewLoadTask(this, getObjectKey(obj, i + "x" + i2), obj, i, i2, widgetCell);
        previewLoadTask.executeOnExecutor(Utilities.THREAD_POOL_EXECUTOR, new Void[0]);
        return new PreviewLoadRequest(this, previewLoadTask);
    }

    Bitmap readFromDb(WidgetCacheKey widgetCacheKey, Bitmap bitmap, PreviewLoadTask previewLoadTask) {
        Cursor cursor = null;
        Cursor cursor2 = null;
        try {
            try {
                Cursor query = this.mDb.query(new String[]{"preview_bitmap"}, "componentName = ? AND profileId = ? AND size = ?", new String[]{widgetCacheKey.componentName.flattenToString(), Long.toString(this.mUserManager.getSerialNumberForUser(widgetCacheKey.user)), widgetCacheKey.size});
                if (previewLoadTask.isCancelled()) {
                    if (query != null) {
                        query.close();
                        return null;
                    }
                    return null;
                }
                if (query.moveToNext()) {
                    byte[] blob = query.getBlob(0);
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    cursor = query;
                    cursor2 = query;
                    options.inBitmap = bitmap;
                    try {
                        if (!previewLoadTask.isCancelled()) {
                            Bitmap decodeByteArray = BitmapFactory.decodeByteArray(blob, 0, blob.length, options);
                            if (query != null) {
                                query.close();
                            }
                            return decodeByteArray;
                        }
                    } catch (Exception e) {
                        if (query != null) {
                            query.close();
                            return null;
                        }
                        return null;
                    }
                }
                if (query != null) {
                    query.close();
                    return null;
                }
                return null;
            } catch (SQLException e2) {
                Log.w("WidgetPreviewLoader", "Error loading preview from DB", e2);
                if (cursor != null) {
                    cursor.close();
                    return null;
                }
                return null;
            }
        } catch (Throwable th) {
            if (cursor2 != null) {
                cursor2.close();
            }
            throw th;
        }
    }

    public void removeObsoletePreviews(ArrayList<Object> arrayList) {
        UserHandleCompat user;
        String packageName;
        Utilities.assertWorkerThread();
        LongSparseArray longSparseArray = new LongSparseArray();
        for (Object obj : arrayList) {
            if (obj instanceof ResolveInfo) {
                user = UserHandleCompat.myUserHandle();
                packageName = ((ResolveInfo) obj).activityInfo.packageName;
            } else {
                LauncherAppWidgetProviderInfo launcherAppWidgetProviderInfo = (LauncherAppWidgetProviderInfo) obj;
                user = this.mWidgetManager.getUser(launcherAppWidgetProviderInfo);
                packageName = launcherAppWidgetProviderInfo.provider.getPackageName();
            }
            long serialNumberForUser = this.mUserManager.getSerialNumberForUser(user);
            HashSet hashSet = (HashSet) longSparseArray.get(serialNumberForUser);
            HashSet hashSet2 = hashSet;
            if (hashSet == null) {
                hashSet2 = new HashSet();
                longSparseArray.put(serialNumberForUser, hashSet2);
            }
            hashSet2.add(packageName);
        }
        LongSparseArray longSparseArray2 = new LongSparseArray();
        Cursor cursor = null;
        Cursor cursor2 = null;
        try {
            try {
                Cursor query = this.mDb.query(new String[]{"profileId", "packageName", "lastUpdated", "version"}, null, null);
                while (query.moveToNext()) {
                    long j = query.getLong(0);
                    String string = query.getString(1);
                    long j2 = query.getLong(2);
                    long j3 = query.getLong(3);
                    HashSet hashSet3 = (HashSet) longSparseArray.get(j);
                    if (hashSet3 != null && hashSet3.contains(string)) {
                        long[] packageVersion = getPackageVersion(string);
                        if (packageVersion[0] == j3 && packageVersion[1] == j2) {
                        }
                    }
                    HashSet hashSet4 = (HashSet) longSparseArray2.get(j);
                    HashSet hashSet5 = hashSet4;
                    if (hashSet4 == null) {
                        hashSet5 = new HashSet();
                        longSparseArray2.put(j, hashSet5);
                    }
                    hashSet5.add(string);
                }
                int i = 0;
                while (true) {
                    cursor2 = query;
                    cursor = query;
                    if (i >= longSparseArray2.size()) {
                        break;
                    }
                    long keyAt = longSparseArray2.keyAt(i);
                    UserHandleCompat userForSerialNumber = this.mUserManager.getUserForSerialNumber(keyAt);
                    for (String str : (HashSet) longSparseArray2.valueAt(i)) {
                        removePackage(str, userForSerialNumber, keyAt);
                    }
                    i++;
                }
                if (query != null) {
                    query.close();
                }
            } catch (SQLException e) {
                Log.e("WidgetPreviewLoader", "Error updating widget previews", e);
                if (cursor2 != null) {
                    cursor2.close();
                }
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    public void removePackage(String str, UserHandleCompat userHandleCompat) {
        removePackage(str, userHandleCompat, this.mUserManager.getSerialNumberForUser(userHandleCompat));
    }

    void writeToDb(WidgetCacheKey widgetCacheKey, long[] jArr, Bitmap bitmap) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("componentName", widgetCacheKey.componentName.flattenToShortString());
        contentValues.put("profileId", Long.valueOf(this.mUserManager.getSerialNumberForUser(widgetCacheKey.user)));
        contentValues.put("size", widgetCacheKey.size);
        contentValues.put("packageName", widgetCacheKey.componentName.getPackageName());
        contentValues.put("version", Long.valueOf(jArr[0]));
        contentValues.put("lastUpdated", Long.valueOf(jArr[1]));
        contentValues.put("preview_bitmap", Utilities.flattenBitmap(bitmap));
        this.mDb.insertOrReplace(contentValues);
    }
}
