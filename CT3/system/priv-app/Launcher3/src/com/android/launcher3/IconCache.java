package com.android.launcher3;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import com.android.launcher3.compat.LauncherActivityInfoCompat;
import com.android.launcher3.compat.LauncherAppsCompat;
import com.android.launcher3.compat.UserHandleCompat;
import com.android.launcher3.compat.UserManagerCompat;
import com.android.launcher3.config.FeatureFlags;
import com.android.launcher3.model.PackageItemInfo;
import com.android.launcher3.util.ComponentKey;
import com.android.launcher3.util.SQLiteCacheHelper;
import com.mediatek.launcher3.LauncherLog;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Stack;
/* loaded from: a.zip:com/android/launcher3/IconCache.class */
public class IconCache {
    static final Object ICON_UPDATE_TOKEN = new Object();
    private final int mActivityBgColor;
    private final Context mContext;
    final IconDB mIconDb;
    private final int mIconDpi;
    private final LauncherAppsCompat mLauncherApps;
    private Bitmap mLowResBitmap;
    private Canvas mLowResCanvas;
    private final BitmapFactory.Options mLowResOptions;
    private Paint mLowResPaint;
    private final int mPackageBgColor;
    private final PackageManager mPackageManager;
    private String mSystemState;
    final UserManagerCompat mUserManager;
    final Handler mWorkerHandler;
    private final HashMap<UserHandleCompat, Bitmap> mDefaultIcons = new HashMap<>();
    final MainThreadExecutor mMainThreadExecutor = new MainThreadExecutor();
    private final HashMap<ComponentKey, CacheEntry> mCache = new HashMap<>(50);

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: a.zip:com/android/launcher3/IconCache$CacheEntry.class */
    public static class CacheEntry {
        public Bitmap icon;
        public boolean isLowResIcon;
        public CharSequence title = "";
        public CharSequence contentDescription = "";

        CacheEntry() {
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/launcher3/IconCache$IconDB.class */
    public static final class IconDB extends SQLiteCacheHelper {
        private static final int RELEASE_VERSION;

        static {
            RELEASE_VERSION = (FeatureFlags.LAUNCHER3_ICON_NORMALIZATION ? 1 : 0) + 7;
        }

        public IconDB(Context context, int i) {
            super(context, "app_icons.db", (RELEASE_VERSION << 16) + i, "icons");
        }

        @Override // com.android.launcher3.util.SQLiteCacheHelper
        protected void onCreateTable(SQLiteDatabase sQLiteDatabase) {
            sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS icons (componentName TEXT NOT NULL, profileId INTEGER NOT NULL, lastUpdated INTEGER NOT NULL DEFAULT 0, version INTEGER NOT NULL DEFAULT 0, icon BLOB, icon_low_res BLOB, label TEXT, system_state TEXT, PRIMARY KEY (componentName, profileId) );");
        }
    }

    /* loaded from: a.zip:com/android/launcher3/IconCache$IconLoadRequest.class */
    public static class IconLoadRequest {
        private final Handler mHandler;
        private final Runnable mRunnable;

        IconLoadRequest(Runnable runnable, Handler handler) {
            this.mRunnable = runnable;
            this.mHandler = handler;
        }

        public void cancel() {
            this.mHandler.removeCallbacks(this.mRunnable);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: a.zip:com/android/launcher3/IconCache$SerializedIconUpdateTask.class */
    public class SerializedIconUpdateTask implements Runnable {
        private final Stack<LauncherActivityInfoCompat> mAppsToAdd;
        private final Stack<LauncherActivityInfoCompat> mAppsToUpdate;
        private final HashMap<String, PackageInfo> mPkgInfoMap;
        private final HashSet<String> mUpdatedPackages = new HashSet<>();
        private final long mUserSerial;
        final IconCache this$0;

        SerializedIconUpdateTask(IconCache iconCache, long j, HashMap<String, PackageInfo> hashMap, Stack<LauncherActivityInfoCompat> stack, Stack<LauncherActivityInfoCompat> stack2) {
            this.this$0 = iconCache;
            this.mUserSerial = j;
            this.mPkgInfoMap = hashMap;
            this.mAppsToAdd = stack;
            this.mAppsToUpdate = stack2;
        }

        @Override // java.lang.Runnable
        public void run() {
            if (!this.mAppsToUpdate.isEmpty()) {
                LauncherActivityInfoCompat pop = this.mAppsToUpdate.pop();
                String flattenToString = pop.getComponentName().flattenToString();
                this.this$0.mIconDb.update(this.this$0.updateCacheAndGetContentValues(pop, true), "componentName = ? AND profileId = ?", new String[]{flattenToString, Long.toString(this.mUserSerial)});
                this.mUpdatedPackages.add(pop.getComponentName().getPackageName());
                if (this.mAppsToUpdate.isEmpty() && !this.mUpdatedPackages.isEmpty()) {
                    LauncherAppState.getInstance().getModel().onPackageIconsUpdated(this.mUpdatedPackages, this.this$0.mUserManager.getUserForSerialNumber(this.mUserSerial));
                }
                scheduleNext();
            } else if (this.mAppsToAdd.isEmpty()) {
            } else {
                LauncherActivityInfoCompat pop2 = this.mAppsToAdd.pop();
                PackageInfo packageInfo = this.mPkgInfoMap.get(pop2.getComponentName().getPackageName());
                if (packageInfo != null) {
                    synchronized (this.this$0) {
                        this.this$0.addIconToDBAndMemCache(pop2, packageInfo, this.mUserSerial);
                    }
                }
                if (this.mAppsToAdd.isEmpty()) {
                    return;
                }
                scheduleNext();
            }
        }

        public void scheduleNext() {
            this.this$0.mWorkerHandler.postAtTime(this, IconCache.ICON_UPDATE_TOKEN, SystemClock.uptimeMillis() + 1);
        }
    }

    public IconCache(Context context, InvariantDeviceProfile invariantDeviceProfile) {
        this.mContext = context;
        this.mPackageManager = context.getPackageManager();
        this.mUserManager = UserManagerCompat.getInstance(this.mContext);
        this.mLauncherApps = LauncherAppsCompat.getInstance(this.mContext);
        this.mIconDpi = invariantDeviceProfile.fillResIconDpi;
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher.IconCache", "IconCache, mIconDpi = " + this.mIconDpi);
        }
        this.mIconDb = new IconDB(context, invariantDeviceProfile.iconBitmapSize);
        this.mWorkerHandler = new Handler(LauncherModel.getWorkerLooper());
        this.mActivityBgColor = context.getResources().getColor(2131361801);
        this.mPackageBgColor = context.getResources().getColor(2131361802);
        this.mLowResOptions = new BitmapFactory.Options();
        this.mLowResOptions.inPreferredConfig = Bitmap.Config.RGB_565;
        updateSystemStateString();
    }

    private void addIconToDB(ContentValues contentValues, ComponentName componentName, PackageInfo packageInfo, long j) {
        contentValues.put("componentName", componentName.flattenToString());
        contentValues.put("profileId", Long.valueOf(j));
        contentValues.put("lastUpdated", Long.valueOf(packageInfo.lastUpdateTime));
        contentValues.put("version", Integer.valueOf(packageInfo.versionCode));
        this.mIconDb.insertOrReplace(contentValues);
    }

    /* JADX WARN: Removed duplicated region for block: B:14:0x005e  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private CacheEntry cacheLocked(ComponentName componentName, LauncherActivityInfoCompat launcherActivityInfoCompat, UserHandleCompat userHandleCompat, boolean z, boolean z2) {
        CacheEntry cacheEntry;
        CacheEntry entryForPackageLocked;
        if (LauncherLog.DEBUG_LAYOUT) {
            LauncherLog.d("Launcher.IconCache", "cacheLocked: componentName = " + componentName + ", info = " + launcherActivityInfoCompat);
        }
        ComponentKey componentKey = new ComponentKey(componentName, userHandleCompat);
        CacheEntry cacheEntry2 = this.mCache.get(componentKey);
        if (cacheEntry2 != null) {
            cacheEntry = cacheEntry2;
            if (cacheEntry2.isLowResIcon) {
                if (z2) {
                    cacheEntry = cacheEntry2;
                }
            }
            if (launcherActivityInfoCompat != null) {
                cacheEntry.title = launcherActivityInfoCompat.getLabel();
            }
            return cacheEntry;
        }
        CacheEntry cacheEntry3 = new CacheEntry();
        this.mCache.put(componentKey, cacheEntry3);
        if (!getEntryFromDB(componentKey, cacheEntry3, z2)) {
            if (launcherActivityInfoCompat != null) {
                cacheEntry3.icon = Utilities.createBadgedIconBitmap(launcherActivityInfoCompat.getIcon(this.mIconDpi), launcherActivityInfoCompat.getUser(), this.mContext);
            } else {
                if (z && (entryForPackageLocked = getEntryForPackageLocked(componentName.getPackageName(), userHandleCompat, false)) != null) {
                    cacheEntry3.icon = entryForPackageLocked.icon;
                    cacheEntry3.title = entryForPackageLocked.title;
                    cacheEntry3.contentDescription = entryForPackageLocked.contentDescription;
                    if (LauncherLog.DEBUG_LOADERS) {
                        LauncherLog.d("Launcher.IconCache", "CacheLocked get title from pms: title = " + cacheEntry3.title);
                    }
                }
                if (cacheEntry3.icon == null) {
                    cacheEntry3.icon = getDefaultIcon(userHandleCompat);
                }
            }
        }
        cacheEntry = cacheEntry3;
        if (TextUtils.isEmpty(cacheEntry3.title)) {
            cacheEntry = cacheEntry3;
            if (launcherActivityInfoCompat != null) {
                cacheEntry3.title = launcherActivityInfoCompat.getLabel();
                cacheEntry3.contentDescription = this.mUserManager.getBadgedLabelForUser(cacheEntry3.title, userHandleCompat);
                cacheEntry = cacheEntry3;
            }
        }
        if (launcherActivityInfoCompat != null) {
        }
        return cacheEntry;
    }

    private CacheEntry getEntryForPackageLocked(String str, UserHandleCompat userHandleCompat, boolean z) {
        CacheEntry cacheEntry;
        ComponentKey packageKey = getPackageKey(str, userHandleCompat);
        CacheEntry cacheEntry2 = this.mCache.get(packageKey);
        if (cacheEntry2 != null) {
            cacheEntry = cacheEntry2;
            if (cacheEntry2.isLowResIcon) {
                if (z) {
                    cacheEntry = cacheEntry2;
                }
            }
            return cacheEntry;
        }
        CacheEntry cacheEntry3 = new CacheEntry();
        boolean z2 = true;
        if (!getEntryFromDB(packageKey, cacheEntry3, z)) {
            try {
                PackageInfo packageInfo = this.mPackageManager.getPackageInfo(str, UserHandleCompat.myUserHandle().equals(userHandleCompat) ? 0 : 8192);
                ApplicationInfo applicationInfo = packageInfo.applicationInfo;
                if (applicationInfo == null) {
                    throw new PackageManager.NameNotFoundException("ApplicationInfo is null");
                }
                cacheEntry3.icon = Utilities.createBadgedIconBitmap(applicationInfo.loadIcon(this.mPackageManager), userHandleCompat, this.mContext);
                cacheEntry3.title = applicationInfo.loadLabel(this.mPackageManager);
                cacheEntry3.contentDescription = this.mUserManager.getBadgedLabelForUser(cacheEntry3.title, userHandleCompat);
                cacheEntry3.isLowResIcon = false;
                addIconToDB(newContentValues(cacheEntry3.icon, cacheEntry3.title.toString(), this.mPackageBgColor), packageKey.componentName, packageInfo, this.mUserManager.getSerialNumberForUser(userHandleCompat));
                z2 = true;
            } catch (PackageManager.NameNotFoundException e) {
                z2 = false;
            }
        }
        cacheEntry = cacheEntry3;
        if (z2) {
            this.mCache.put(packageKey, cacheEntry3);
            cacheEntry = cacheEntry3;
        }
        return cacheEntry;
    }

    private boolean getEntryFromDB(ComponentKey componentKey, CacheEntry cacheEntry, boolean z) {
        BitmapFactory.Options options = null;
        Cursor cursor = null;
        Cursor cursor2 = null;
        try {
            try {
                Cursor query = this.mIconDb.query(new String[]{z ? "icon_low_res" : "icon", "label"}, "componentName = ? AND profileId = ?", new String[]{componentKey.componentName.flattenToString(), Long.toString(this.mUserManager.getSerialNumberForUser(componentKey.user))});
                if (!query.moveToNext()) {
                    if (query != null) {
                        query.close();
                        return false;
                    }
                    return false;
                }
                if (z) {
                    options = this.mLowResOptions;
                }
                cacheEntry.icon = loadIconNoResize(query, 0, options);
                cacheEntry.isLowResIcon = z;
                cacheEntry.title = query.getString(1);
                if (cacheEntry.title == null) {
                    cacheEntry.title = "";
                    cacheEntry.contentDescription = "";
                } else {
                    cacheEntry.contentDescription = this.mUserManager.getBadgedLabelForUser(cacheEntry.title, componentKey.user);
                }
                if (query != null) {
                    query.close();
                    return true;
                }
                return true;
            } catch (SQLiteException e) {
                Log.d("Launcher.IconCache", "Error reading icon cache", e);
                if (0 != 0) {
                    cursor2.close();
                    return false;
                }
                return false;
            }
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
    }

    private Drawable getFullResDefaultActivityIcon() {
        return getFullResIcon(Resources.getSystem(), 17629184);
    }

    private Drawable getFullResIcon(Resources resources, int i) {
        Drawable drawable;
        try {
            drawable = resources.getDrawableForDensity(i, this.mIconDpi);
        } catch (Resources.NotFoundException e) {
            drawable = null;
        }
        if (drawable == null) {
            drawable = getFullResDefaultActivityIcon();
        }
        return drawable;
    }

    private Bitmap getNonNullIcon(CacheEntry cacheEntry, UserHandleCompat userHandleCompat) {
        return cacheEntry.icon == null ? getDefaultIcon(userHandleCompat) : cacheEntry.icon;
    }

    private static ComponentKey getPackageKey(String str, UserHandleCompat userHandleCompat) {
        return new ComponentKey(new ComponentName(str, str + "."), userHandleCompat);
    }

    private static Bitmap loadIconNoResize(Cursor cursor, int i, BitmapFactory.Options options) {
        byte[] blob = cursor.getBlob(i);
        try {
            return BitmapFactory.decodeByteArray(blob, 0, blob.length, options);
        } catch (Exception e) {
            return null;
        }
    }

    private Bitmap makeDefaultIcon(UserHandleCompat userHandleCompat) {
        return Utilities.createBadgedIconBitmap(getFullResDefaultActivityIcon(), userHandleCompat, this.mContext);
    }

    private ContentValues newContentValues(Bitmap bitmap, String str, int i) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("icon", Utilities.flattenBitmap(bitmap));
        contentValues.put("label", str);
        contentValues.put("system_state", this.mSystemState);
        if (i == 0) {
            contentValues.put("icon_low_res", Utilities.flattenBitmap(Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() / 5, bitmap.getHeight() / 5, true)));
        } else {
            synchronized (this) {
                if (this.mLowResBitmap == null) {
                    this.mLowResBitmap = Bitmap.createBitmap(bitmap.getWidth() / 5, bitmap.getHeight() / 5, Bitmap.Config.RGB_565);
                    this.mLowResCanvas = new Canvas(this.mLowResBitmap);
                    this.mLowResPaint = new Paint(3);
                }
                this.mLowResCanvas.drawColor(i);
                this.mLowResCanvas.drawBitmap(bitmap, new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()), new Rect(0, 0, this.mLowResBitmap.getWidth(), this.mLowResBitmap.getHeight()), this.mLowResPaint);
                contentValues.put("icon_low_res", Utilities.flattenBitmap(this.mLowResBitmap));
            }
        }
        return contentValues;
    }

    private void removeFromMemCacheLocked(String str, UserHandleCompat userHandleCompat) {
        HashSet<ComponentKey> hashSet = new HashSet();
        for (ComponentKey componentKey : this.mCache.keySet()) {
            if (componentKey.componentName.getPackageName().equals(str) && componentKey.user.equals(userHandleCompat)) {
                hashSet.add(componentKey);
            }
        }
        for (ComponentKey componentKey2 : hashSet) {
            this.mCache.remove(componentKey2);
        }
    }

    private void updateDBIcons(UserHandleCompat userHandleCompat, List<LauncherActivityInfoCompat> list, Set<String> set) {
        long serialNumberForUser = this.mUserManager.getSerialNumberForUser(userHandleCompat);
        PackageManager packageManager = this.mContext.getPackageManager();
        HashMap hashMap = new HashMap();
        for (PackageInfo packageInfo : packageManager.getInstalledPackages(8192)) {
            hashMap.put(packageInfo.packageName, packageInfo);
        }
        HashMap hashMap2 = new HashMap();
        for (LauncherActivityInfoCompat launcherActivityInfoCompat : list) {
            hashMap2.put(launcherActivityInfoCompat.getComponentName(), launcherActivityInfoCompat);
        }
        HashSet hashSet = new HashSet();
        Stack stack = new Stack();
        Cursor cursor = null;
        Cursor cursor2 = null;
        try {
            try {
                Cursor query = this.mIconDb.query(new String[]{"rowid", "componentName", "lastUpdated", "version", "system_state"}, "profileId = ? ", new String[]{Long.toString(serialNumberForUser)});
                int columnIndex = query.getColumnIndex("componentName");
                int columnIndex2 = query.getColumnIndex("lastUpdated");
                int columnIndex3 = query.getColumnIndex("version");
                int columnIndex4 = query.getColumnIndex("rowid");
                int columnIndex5 = query.getColumnIndex("system_state");
                while (true) {
                    cursor = query;
                    cursor2 = query;
                    if (!query.moveToNext()) {
                        break;
                    }
                    ComponentName unflattenFromString = ComponentName.unflattenFromString(query.getString(columnIndex));
                    PackageInfo packageInfo2 = (PackageInfo) hashMap.get(unflattenFromString.getPackageName());
                    if (packageInfo2 == null) {
                        if (!set.contains(unflattenFromString.getPackageName())) {
                            remove(unflattenFromString, userHandleCompat);
                            hashSet.add(Integer.valueOf(query.getInt(columnIndex4)));
                        }
                    } else if ((packageInfo2.applicationInfo.flags & 16777216) == 0) {
                        long j = query.getLong(columnIndex2);
                        int i = query.getInt(columnIndex3);
                        LauncherActivityInfoCompat launcherActivityInfoCompat2 = (LauncherActivityInfoCompat) hashMap2.remove(unflattenFromString);
                        if (i != packageInfo2.versionCode || j != packageInfo2.lastUpdateTime || !TextUtils.equals(this.mSystemState, query.getString(columnIndex5))) {
                            if (launcherActivityInfoCompat2 == null) {
                                remove(unflattenFromString, userHandleCompat);
                                hashSet.add(Integer.valueOf(query.getInt(columnIndex4)));
                            } else {
                                stack.add(launcherActivityInfoCompat2);
                            }
                        }
                    }
                }
                if (query != null) {
                    query.close();
                }
            } catch (SQLiteException e) {
                Log.d("Launcher.IconCache", "Error reading icon cache", e);
                if (cursor != null) {
                    cursor.close();
                }
            }
            if (!hashSet.isEmpty()) {
                this.mIconDb.delete(Utilities.createDbSelectionQuery("rowid", hashSet), null);
            }
            if (hashMap2.isEmpty() && stack.isEmpty()) {
                return;
            }
            Stack stack2 = new Stack();
            stack2.addAll(hashMap2.values());
            new SerializedIconUpdateTask(this, serialNumberForUser, hashMap, stack2, stack).scheduleNext();
        } catch (Throwable th) {
            if (cursor2 != null) {
                cursor2.close();
            }
            throw th;
        }
    }

    private void updateSystemStateString() {
        this.mSystemState = Locale.getDefault().toString();
    }

    void addIconToDBAndMemCache(LauncherActivityInfoCompat launcherActivityInfoCompat, PackageInfo packageInfo, long j) {
        addIconToDB(updateCacheAndGetContentValues(launcherActivityInfoCompat, false), launcherActivityInfoCompat.getComponentName(), packageInfo, j);
    }

    public void cachePackageInstallInfo(String str, UserHandleCompat userHandleCompat, Bitmap bitmap, CharSequence charSequence) {
        synchronized (this) {
            removeFromMemCacheLocked(str, userHandleCompat);
            ComponentKey packageKey = getPackageKey(str, userHandleCompat);
            CacheEntry cacheEntry = this.mCache.get(packageKey);
            CacheEntry cacheEntry2 = cacheEntry;
            if (cacheEntry == null) {
                cacheEntry2 = new CacheEntry();
                this.mCache.put(packageKey, cacheEntry2);
            }
            if (!TextUtils.isEmpty(charSequence)) {
                cacheEntry2.title = charSequence;
            }
            if (bitmap != null) {
                cacheEntry2.icon = Utilities.createIconBitmap(bitmap, this.mContext);
            }
        }
    }

    public Bitmap getDefaultIcon(UserHandleCompat userHandleCompat) {
        Bitmap bitmap;
        synchronized (this) {
            if (!this.mDefaultIcons.containsKey(userHandleCompat)) {
                this.mDefaultIcons.put(userHandleCompat, makeDefaultIcon(userHandleCompat));
            }
            bitmap = this.mDefaultIcons.get(userHandleCompat);
        }
        return bitmap;
    }

    public Drawable getFullResIcon(ActivityInfo activityInfo) {
        Resources resources;
        int iconResource;
        try {
            resources = this.mPackageManager.getResourcesForApplication(activityInfo.applicationInfo);
        } catch (PackageManager.NameNotFoundException e) {
            resources = null;
        }
        return (resources == null || (iconResource = activityInfo.getIconResource()) == 0) ? getFullResDefaultActivityIcon() : getFullResIcon(resources, iconResource);
    }

    public Drawable getFullResIcon(String str, int i) {
        Resources resources;
        try {
            resources = this.mPackageManager.getResourcesForApplication(str);
        } catch (PackageManager.NameNotFoundException e) {
            resources = null;
        }
        return (resources == null || i == 0) ? getFullResDefaultActivityIcon() : getFullResIcon(resources, i);
    }

    public Bitmap getIcon(Intent intent, UserHandleCompat userHandleCompat) {
        synchronized (this) {
            ComponentName component = intent.getComponent();
            if (component == null) {
                return getDefaultIcon(userHandleCompat);
            }
            return cacheLocked(component, this.mLauncherApps.resolveActivity(intent, userHandleCompat), userHandleCompat, true, false).icon;
        }
    }

    public void getTitleAndIcon(AppInfo appInfo, LauncherActivityInfoCompat launcherActivityInfoCompat, boolean z) {
        synchronized (this) {
            UserHandleCompat user = launcherActivityInfoCompat == null ? appInfo.user : launcherActivityInfoCompat.getUser();
            CacheEntry cacheLocked = cacheLocked(appInfo.componentName, launcherActivityInfoCompat, user, false, z);
            appInfo.title = Utilities.trim(cacheLocked.title);
            appInfo.iconBitmap = getNonNullIcon(cacheLocked, user);
            appInfo.contentDescription = cacheLocked.contentDescription;
            appInfo.usingLowResIcon = cacheLocked.isLowResIcon;
        }
    }

    public void getTitleAndIcon(ShortcutInfo shortcutInfo, ComponentName componentName, LauncherActivityInfoCompat launcherActivityInfoCompat, UserHandleCompat userHandleCompat, boolean z, boolean z2) {
        synchronized (this) {
            CacheEntry cacheLocked = cacheLocked(componentName, launcherActivityInfoCompat, userHandleCompat, z, z2);
            shortcutInfo.setIcon(getNonNullIcon(cacheLocked, userHandleCompat));
            shortcutInfo.title = Utilities.trim(cacheLocked.title);
            shortcutInfo.usingFallbackIcon = isDefaultIcon(cacheLocked.icon, userHandleCompat);
            shortcutInfo.usingLowResIcon = cacheLocked.isLowResIcon;
        }
    }

    public void getTitleAndIcon(ShortcutInfo shortcutInfo, Intent intent, UserHandleCompat userHandleCompat, boolean z) {
        synchronized (this) {
            ComponentName component = intent.getComponent();
            if (component == null) {
                shortcutInfo.setIcon(getDefaultIcon(userHandleCompat));
                shortcutInfo.title = "";
                shortcutInfo.usingFallbackIcon = true;
                shortcutInfo.usingLowResIcon = false;
            } else {
                getTitleAndIcon(shortcutInfo, component, this.mLauncherApps.resolveActivity(intent, userHandleCompat), userHandleCompat, true, z);
            }
        }
    }

    public void getTitleAndIconForApp(String str, UserHandleCompat userHandleCompat, boolean z, PackageItemInfo packageItemInfo) {
        synchronized (this) {
            CacheEntry entryForPackageLocked = getEntryForPackageLocked(str, userHandleCompat, z);
            packageItemInfo.iconBitmap = getNonNullIcon(entryForPackageLocked, userHandleCompat);
            packageItemInfo.title = Utilities.trim(entryForPackageLocked.title);
            packageItemInfo.usingLowResIcon = entryForPackageLocked.isLowResIcon;
            packageItemInfo.contentDescription = entryForPackageLocked.contentDescription;
        }
    }

    public boolean isDefaultIcon(Bitmap bitmap, UserHandleCompat userHandleCompat) {
        return this.mDefaultIcons.get(userHandleCompat) == bitmap;
    }

    public void preloadIcon(ComponentName componentName, Bitmap bitmap, int i, String str, long j, InvariantDeviceProfile invariantDeviceProfile) {
        try {
            this.mContext.getPackageManager().getActivityIcon(componentName);
        } catch (PackageManager.NameNotFoundException e) {
            ContentValues newContentValues = newContentValues(Bitmap.createScaledBitmap(bitmap, invariantDeviceProfile.iconBitmapSize, invariantDeviceProfile.iconBitmapSize, true), str, 0);
            newContentValues.put("componentName", componentName.flattenToString());
            newContentValues.put("profileId", Long.valueOf(j));
            this.mIconDb.insertOrReplace(newContentValues);
        }
    }

    public void remove(ComponentName componentName, UserHandleCompat userHandleCompat) {
        synchronized (this) {
            this.mCache.remove(new ComponentKey(componentName, userHandleCompat));
        }
    }

    public void removeIconsForPkg(String str, UserHandleCompat userHandleCompat) {
        synchronized (this) {
            removeFromMemCacheLocked(str, userHandleCompat);
            this.mIconDb.delete("componentName LIKE ? AND profileId = ?", new String[]{str + "/%", Long.toString(this.mUserManager.getSerialNumberForUser(userHandleCompat))});
        }
    }

    /* JADX WARN: Code restructure failed: missing block: B:9:0x0035, code lost:
        if (r0.icon == null) goto L9;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    ContentValues updateCacheAndGetContentValues(LauncherActivityInfoCompat launcherActivityInfoCompat, boolean z) {
        ComponentKey componentKey = new ComponentKey(launcherActivityInfoCompat.getComponentName(), launcherActivityInfoCompat.getUser());
        CacheEntry cacheEntry = null;
        if (!z) {
            CacheEntry cacheEntry2 = this.mCache.get(componentKey);
            if (cacheEntry2 != null && !cacheEntry2.isLowResIcon) {
                cacheEntry = cacheEntry2;
            }
            cacheEntry = null;
        }
        CacheEntry cacheEntry3 = cacheEntry;
        if (cacheEntry == null) {
            cacheEntry3 = new CacheEntry();
            cacheEntry3.icon = Utilities.createBadgedIconBitmap(launcherActivityInfoCompat.getIcon(this.mIconDpi), launcherActivityInfoCompat.getUser(), this.mContext);
        }
        cacheEntry3.title = launcherActivityInfoCompat.getLabel();
        cacheEntry3.contentDescription = this.mUserManager.getBadgedLabelForUser(cacheEntry3.title, launcherActivityInfoCompat.getUser());
        this.mCache.put(new ComponentKey(launcherActivityInfoCompat.getComponentName(), launcherActivityInfoCompat.getUser()), cacheEntry3);
        return newContentValues(cacheEntry3.icon, cacheEntry3.title.toString(), this.mActivityBgColor);
    }

    public void updateDbIcons(Set<String> set) {
        UserHandleCompat userHandleCompat;
        List<LauncherActivityInfoCompat> activityList;
        this.mWorkerHandler.removeCallbacksAndMessages(ICON_UPDATE_TOKEN);
        updateSystemStateString();
        Iterator<T> it = this.mUserManager.getUserProfiles().iterator();
        while (it.hasNext() && (activityList = this.mLauncherApps.getActivityList(null, (userHandleCompat = (UserHandleCompat) it.next()))) != null && !activityList.isEmpty()) {
            updateDBIcons(userHandleCompat, activityList, UserHandleCompat.myUserHandle().equals(userHandleCompat) ? set : Collections.emptySet());
        }
    }

    public IconLoadRequest updateIconInBackground(BubbleTextView bubbleTextView, ItemInfo itemInfo) {
        Runnable runnable = new Runnable(this, itemInfo, bubbleTextView) { // from class: com.android.launcher3.IconCache.1
            final IconCache this$0;
            final BubbleTextView val$caller;
            final ItemInfo val$info;

            {
                this.this$0 = this;
                this.val$info = itemInfo;
                this.val$caller = bubbleTextView;
            }

            @Override // java.lang.Runnable
            public void run() {
                if (this.val$info instanceof AppInfo) {
                    this.this$0.getTitleAndIcon((AppInfo) this.val$info, null, false);
                } else if (this.val$info instanceof ShortcutInfo) {
                    ShortcutInfo shortcutInfo = (ShortcutInfo) this.val$info;
                    this.this$0.getTitleAndIcon(shortcutInfo, shortcutInfo.promisedIntent != null ? shortcutInfo.promisedIntent : shortcutInfo.intent, shortcutInfo.user, false);
                } else if (this.val$info instanceof PackageItemInfo) {
                    PackageItemInfo packageItemInfo = (PackageItemInfo) this.val$info;
                    this.this$0.getTitleAndIconForApp(packageItemInfo.packageName, packageItemInfo.user, false, packageItemInfo);
                }
                this.this$0.mMainThreadExecutor.execute(new Runnable(this, this.val$caller, this.val$info) { // from class: com.android.launcher3.IconCache.1.1
                    final AnonymousClass1 this$1;
                    final BubbleTextView val$caller;
                    final ItemInfo val$info;

                    {
                        this.this$1 = this;
                        this.val$caller = r5;
                        this.val$info = r6;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        this.val$caller.reapplyItemInfo(this.val$info);
                    }
                });
            }
        };
        this.mWorkerHandler.post(runnable);
        return new IconLoadRequest(runnable, this.mWorkerHandler);
    }

    public void updateIconsForPkg(String str, UserHandleCompat userHandleCompat) {
        synchronized (this) {
            removeIconsForPkg(str, userHandleCompat);
            try {
                PackageInfo packageInfo = this.mPackageManager.getPackageInfo(str, 8192);
                long serialNumberForUser = this.mUserManager.getSerialNumberForUser(userHandleCompat);
                for (LauncherActivityInfoCompat launcherActivityInfoCompat : this.mLauncherApps.getActivityList(str, userHandleCompat)) {
                    addIconToDBAndMemCache(launcherActivityInfoCompat, packageInfo, serialNumberForUser);
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.d("Launcher.IconCache", "Package not found", e);
            }
        }
    }

    public void updateTitleAndIcon(AppInfo appInfo) {
        synchronized (this) {
            CacheEntry cacheLocked = cacheLocked(appInfo.componentName, null, appInfo.user, false, appInfo.usingLowResIcon);
            if (cacheLocked.icon != null && !isDefaultIcon(cacheLocked.icon, appInfo.user)) {
                appInfo.title = Utilities.trim(cacheLocked.title);
                appInfo.iconBitmap = cacheLocked.icon;
                appInfo.contentDescription = cacheLocked.contentDescription;
                appInfo.usingLowResIcon = cacheLocked.isLowResIcon;
            }
        }
    }
}
