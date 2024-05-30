package com.android.launcher3;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Process;
import android.os.SystemClock;
import android.os.UserHandle;
import android.support.annotation.NonNull;
import android.support.v4.graphics.ColorUtils;
import android.text.TextUtils;
import android.util.Log;
import com.android.launcher3.IconCache;
import com.android.launcher3.LauncherSettings;
import com.android.launcher3.compat.LauncherAppsCompat;
import com.android.launcher3.compat.UserManagerCompat;
import com.android.launcher3.graphics.BitmapInfo;
import com.android.launcher3.graphics.BitmapRenderer;
import com.android.launcher3.graphics.LauncherIcons;
import com.android.launcher3.model.PackageItemInfo;
import com.android.launcher3.util.ComponentKey;
import com.android.launcher3.util.InstantAppResolver;
import com.android.launcher3.util.Preconditions;
import com.android.launcher3.util.Provider;
import com.android.launcher3.util.SQLiteCacheHelper;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
/* loaded from: classes.dex */
public class IconCache {
    private static final boolean DEBUG = false;
    private static final boolean DEBUG_IGNORE_CACHE = false;
    public static final String EMPTY_CLASS_NAME = ".";
    static final Object ICON_UPDATE_TOKEN = new Object();
    private static final int INITIAL_ICON_CACHE_CAPACITY = 50;
    private static final int LOW_RES_SCALE_FACTOR = 5;
    private static final String TAG = "Launcher.IconCache";
    private final Context mContext;
    private final BitmapFactory.Options mHighResOptions;
    final IconDB mIconDb;
    private final int mIconDpi;
    private final IconProvider mIconProvider;
    private final InstantAppResolver mInstantAppResolver;
    private final LauncherAppsCompat mLauncherApps;
    private final PackageManager mPackageManager;
    final UserManagerCompat mUserManager;
    private final HashMap<UserHandle, BitmapInfo> mDefaultIcons = new HashMap<>();
    final MainThreadExecutor mMainThreadExecutor = new MainThreadExecutor();
    private final HashMap<ComponentKey, CacheEntry> mCache = new HashMap<>((int) INITIAL_ICON_CACHE_CAPACITY);
    private int mPendingIconRequestCount = 0;
    final Handler mWorkerHandler = new Handler(LauncherModel.getWorkerLooper());
    private final BitmapFactory.Options mLowResOptions = new BitmapFactory.Options();

    /* loaded from: classes.dex */
    public static class CacheEntry extends BitmapInfo {
        public boolean isLowResIcon;
        public CharSequence title = "";
        public CharSequence contentDescription = "";
    }

    /* loaded from: classes.dex */
    public interface ItemInfoUpdateReceiver {
        void reapplyItemInfo(ItemInfoWithIcon itemInfoWithIcon);
    }

    public IconCache(Context context, InvariantDeviceProfile invariantDeviceProfile) {
        this.mContext = context;
        this.mPackageManager = context.getPackageManager();
        this.mUserManager = UserManagerCompat.getInstance(this.mContext);
        this.mLauncherApps = LauncherAppsCompat.getInstance(this.mContext);
        this.mInstantAppResolver = InstantAppResolver.newInstance(this.mContext);
        this.mIconDpi = invariantDeviceProfile.fillResIconDpi;
        this.mIconDb = new IconDB(context, invariantDeviceProfile.iconBitmapSize);
        this.mIconProvider = IconProvider.newInstance(context);
        this.mLowResOptions.inPreferredConfig = Bitmap.Config.RGB_565;
        if (BitmapRenderer.USE_HARDWARE_BITMAP) {
            this.mHighResOptions = new BitmapFactory.Options();
            this.mHighResOptions.inPreferredConfig = Bitmap.Config.HARDWARE;
            return;
        }
        this.mHighResOptions = null;
    }

    private Drawable getFullResDefaultActivityIcon() {
        return getFullResIcon(Resources.getSystem(), Utilities.ATLEAST_OREO ? 17301651 : 17629184);
    }

    private Drawable getFullResIcon(Resources resources, int i) {
        Drawable drawable;
        try {
            drawable = resources.getDrawableForDensity(i, this.mIconDpi);
        } catch (Resources.NotFoundException e) {
            drawable = null;
        }
        return drawable != null ? drawable : getFullResDefaultActivityIcon();
    }

    public Drawable getFullResIcon(String str, int i) {
        Resources resources;
        try {
            resources = this.mPackageManager.getResourcesForApplication(str);
        } catch (PackageManager.NameNotFoundException e) {
            resources = null;
        }
        if (resources != null && i != 0) {
            return getFullResIcon(resources, i);
        }
        return getFullResDefaultActivityIcon();
    }

    public Drawable getFullResIcon(ActivityInfo activityInfo) {
        Resources resources;
        int iconResource;
        try {
            resources = this.mPackageManager.getResourcesForApplication(activityInfo.applicationInfo);
        } catch (PackageManager.NameNotFoundException e) {
            resources = null;
        }
        if (resources != null && (iconResource = activityInfo.getIconResource()) != 0) {
            return getFullResIcon(resources, iconResource);
        }
        return getFullResDefaultActivityIcon();
    }

    public Drawable getFullResIcon(LauncherActivityInfo launcherActivityInfo) {
        return getFullResIcon(launcherActivityInfo, true);
    }

    public Drawable getFullResIcon(LauncherActivityInfo launcherActivityInfo, boolean z) {
        return this.mIconProvider.getIcon(launcherActivityInfo, this.mIconDpi, z);
    }

    protected BitmapInfo makeDefaultIcon(UserHandle userHandle) {
        LauncherIcons obtain = LauncherIcons.obtain(this.mContext);
        try {
            BitmapInfo createBadgedIconBitmap = obtain.createBadgedIconBitmap(getFullResDefaultActivityIcon(), userHandle, Build.VERSION.SDK_INT);
            if (obtain != null) {
                obtain.close();
            }
            return createBadgedIconBitmap;
        } catch (Throwable th) {
            try {
                throw th;
            } catch (Throwable th2) {
                if (obtain != null) {
                    if (th != null) {
                        try {
                            obtain.close();
                        } catch (Throwable th3) {
                            th.addSuppressed(th3);
                        }
                    } else {
                        obtain.close();
                    }
                }
                throw th2;
            }
        }
    }

    public synchronized void remove(ComponentName componentName, UserHandle userHandle) {
        this.mCache.remove(new ComponentKey(componentName, userHandle));
    }

    private void removeFromMemCacheLocked(String str, UserHandle userHandle) {
        HashSet hashSet = new HashSet();
        for (ComponentKey componentKey : this.mCache.keySet()) {
            if (componentKey.componentName.getPackageName().equals(str) && componentKey.user.equals(userHandle)) {
                hashSet.add(componentKey);
            }
        }
        Iterator it = hashSet.iterator();
        while (it.hasNext()) {
            this.mCache.remove((ComponentKey) it.next());
        }
    }

    public synchronized void updateIconsForPkg(String str, UserHandle userHandle) {
        removeIconsForPkg(str, userHandle);
        try {
            PackageInfo packageInfo = this.mPackageManager.getPackageInfo(str, 8192);
            long serialNumberForUser = this.mUserManager.getSerialNumberForUser(userHandle);
            for (LauncherActivityInfo launcherActivityInfo : this.mLauncherApps.getActivityList(str, userHandle)) {
                addIconToDBAndMemCache(launcherActivityInfo, packageInfo, serialNumberForUser, false);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, "Package not found", e);
        }
    }

    public synchronized void removeIconsForPkg(String str, UserHandle userHandle) {
        removeFromMemCacheLocked(str, userHandle);
        long serialNumberForUser = this.mUserManager.getSerialNumberForUser(userHandle);
        IconDB iconDB = this.mIconDb;
        iconDB.delete("componentName LIKE ? AND profileId = ?", new String[]{str + "/%", Long.toString(serialNumberForUser)});
    }

    public void updateDbIcons(Set<String> set) {
        UserHandle next;
        List<LauncherActivityInfo> activityList;
        Set<String> emptySet;
        this.mWorkerHandler.removeCallbacksAndMessages(ICON_UPDATE_TOKEN);
        this.mIconProvider.updateSystemStateString(this.mContext);
        Iterator<UserHandle> it = this.mUserManager.getUserProfiles().iterator();
        while (it.hasNext() && (activityList = this.mLauncherApps.getActivityList(null, (next = it.next()))) != null && !activityList.isEmpty()) {
            if (Process.myUserHandle().equals(next)) {
                emptySet = set;
            } else {
                emptySet = Collections.emptySet();
            }
            updateDBIcons(next, activityList, emptySet);
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:57:0x015d  */
    /* JADX WARN: Removed duplicated region for block: B:67:0x0191  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private void updateDBIcons(UserHandle userHandle, List<LauncherActivityInfo> list, Set<String> set) {
        Cursor cursor;
        long j;
        Cursor cursor2;
        int i;
        long serialNumberForUser = this.mUserManager.getSerialNumberForUser(userHandle);
        PackageManager packageManager = this.mContext.getPackageManager();
        HashMap hashMap = new HashMap();
        for (PackageInfo packageInfo : packageManager.getInstalledPackages(8192)) {
            hashMap.put(packageInfo.packageName, packageInfo);
        }
        HashMap hashMap2 = new HashMap();
        for (LauncherActivityInfo launcherActivityInfo : list) {
            hashMap2.put(launcherActivityInfo.getComponentName(), launcherActivityInfo);
        }
        HashSet hashSet = new HashSet();
        Stack stack = new Stack();
        try {
            cursor = this.mIconDb.query(new String[]{"rowid", "componentName", "lastUpdated", "version", "system_state"}, "profileId = ? ", new String[]{Long.toString(serialNumberForUser)});
            try {
                try {
                    int columnIndex = cursor.getColumnIndex("componentName");
                    int columnIndex2 = cursor.getColumnIndex("lastUpdated");
                    int columnIndex3 = cursor.getColumnIndex("version");
                    int columnIndex4 = cursor.getColumnIndex("rowid");
                    int columnIndex5 = cursor.getColumnIndex("system_state");
                    while (cursor.moveToNext()) {
                        ComponentName unflattenFromString = ComponentName.unflattenFromString(cursor.getString(columnIndex));
                        PackageInfo packageInfo2 = (PackageInfo) hashMap.get(unflattenFromString.getPackageName());
                        if (packageInfo2 == null) {
                            i = columnIndex;
                            if (!set.contains(unflattenFromString.getPackageName())) {
                                remove(unflattenFromString, userHandle);
                                hashSet.add(Integer.valueOf(cursor.getInt(columnIndex4)));
                            }
                        } else {
                            i = columnIndex;
                            if ((packageInfo2.applicationInfo.flags & 16777216) == 0) {
                                long j2 = cursor.getLong(columnIndex2);
                                int i2 = cursor.getInt(columnIndex3);
                                int i3 = columnIndex2;
                                LauncherActivityInfo launcherActivityInfo2 = (LauncherActivityInfo) hashMap2.remove(unflattenFromString);
                                int i4 = columnIndex3;
                                if (i2 == packageInfo2.versionCode) {
                                    j = serialNumberForUser;
                                    try {
                                        if (j2 == packageInfo2.lastUpdateTime && TextUtils.equals(cursor.getString(columnIndex5), this.mIconProvider.getIconSystemState(packageInfo2.packageName))) {
                                            columnIndex = i;
                                            columnIndex2 = i3;
                                            columnIndex3 = i4;
                                            serialNumberForUser = j;
                                        }
                                    } catch (SQLiteException e) {
                                        e = e;
                                        cursor2 = cursor;
                                        try {
                                            Log.d(TAG, "Error reading icon cache", e);
                                            if (cursor2 != null) {
                                                cursor2.close();
                                            }
                                            if (!hashSet.isEmpty()) {
                                            }
                                            if (hashMap2.isEmpty()) {
                                            }
                                            Stack stack2 = new Stack();
                                            stack2.addAll(hashMap2.values());
                                            new SerializedIconUpdateTask(j, hashMap, stack2, stack).scheduleNext();
                                        } catch (Throwable th) {
                                            th = th;
                                            cursor = cursor2;
                                            if (cursor != null) {
                                                cursor.close();
                                            }
                                            throw th;
                                        }
                                    }
                                } else {
                                    j = serialNumberForUser;
                                }
                                if (launcherActivityInfo2 == null) {
                                    remove(unflattenFromString, userHandle);
                                    hashSet.add(Integer.valueOf(cursor.getInt(columnIndex4)));
                                } else {
                                    stack.add(launcherActivityInfo2);
                                }
                                columnIndex = i;
                                columnIndex2 = i3;
                                columnIndex3 = i4;
                                serialNumberForUser = j;
                            }
                        }
                        columnIndex = i;
                    }
                    j = serialNumberForUser;
                    if (cursor != null) {
                        cursor.close();
                    }
                } catch (SQLiteException e2) {
                    e = e2;
                    j = serialNumberForUser;
                }
            } catch (Throwable th2) {
                th = th2;
                if (cursor != null) {
                }
                throw th;
            }
        } catch (SQLiteException e3) {
            e = e3;
            j = serialNumberForUser;
            cursor2 = null;
        } catch (Throwable th3) {
            th = th3;
            cursor = null;
        }
        if (!hashSet.isEmpty()) {
            this.mIconDb.delete(Utilities.createDbSelectionQuery("rowid", hashSet), null);
        }
        if (hashMap2.isEmpty() || !stack.isEmpty()) {
            Stack stack22 = new Stack();
            stack22.addAll(hashMap2.values());
            new SerializedIconUpdateTask(j, hashMap, stack22, stack).scheduleNext();
        }
    }

    /* JADX WARN: Code restructure failed: missing block: B:15:0x002a, code lost:
        r1 = new com.android.launcher3.IconCache.CacheEntry();
        r2 = com.android.launcher3.graphics.LauncherIcons.obtain(r8.mContext);
        r2.createBadgedIconBitmap(getFullResIcon(r9), r9.getUser(), r9.getApplicationInfo().targetSdkVersion).applyTo(r1);
        r2.recycle();
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    synchronized void addIconToDBAndMemCache(LauncherActivityInfo launcherActivityInfo, PackageInfo packageInfo, long j, boolean z) {
        CacheEntry cacheEntry;
        ComponentKey componentKey = new ComponentKey(launcherActivityInfo.getComponentName(), launcherActivityInfo.getUser());
        CacheEntry cacheEntry2 = null;
        if (!z && (cacheEntry = this.mCache.get(componentKey)) != null && !cacheEntry.isLowResIcon && cacheEntry.icon != null) {
            cacheEntry2 = cacheEntry;
        }
        cacheEntry2.title = launcherActivityInfo.getLabel();
        cacheEntry2.contentDescription = this.mUserManager.getBadgedLabelForUser(cacheEntry2.title, launcherActivityInfo.getUser());
        this.mCache.put(componentKey, cacheEntry2);
        addIconToDB(newContentValues(cacheEntry2.icon, generateLowResIcon(cacheEntry2.icon), cacheEntry2.color, cacheEntry2.title.toString(), launcherActivityInfo.getApplicationInfo().packageName), launcherActivityInfo.getComponentName(), packageInfo, j);
    }

    private void addIconToDB(ContentValues contentValues, ComponentName componentName, PackageInfo packageInfo, long j) {
        contentValues.put("componentName", componentName.flattenToString());
        contentValues.put(LauncherSettings.Favorites.PROFILE_ID, Long.valueOf(j));
        contentValues.put("lastUpdated", Long.valueOf(packageInfo.lastUpdateTime));
        contentValues.put("version", Integer.valueOf(packageInfo.versionCode));
        this.mIconDb.insertOrReplace(contentValues);
    }

    public IconLoadRequest updateIconInBackground(ItemInfoUpdateReceiver itemInfoUpdateReceiver, ItemInfoWithIcon itemInfoWithIcon) {
        Preconditions.assertUIThread();
        if (this.mPendingIconRequestCount <= 0) {
            LauncherModel.setWorkerPriority(-2);
        }
        this.mPendingIconRequestCount++;
        AnonymousClass1 anonymousClass1 = new AnonymousClass1(this.mWorkerHandler, new Runnable() { // from class: com.android.launcher3.-$$Lambda$IconCache$i9cUcxPyZLyL2CkO-mTsWx7iQ94
            @Override // java.lang.Runnable
            public final void run() {
                IconCache.this.onIconRequestEnd();
            }
        }, itemInfoWithIcon, itemInfoUpdateReceiver);
        Utilities.postAsyncCallback(this.mWorkerHandler, anonymousClass1);
        return anonymousClass1;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.android.launcher3.IconCache$1  reason: invalid class name */
    /* loaded from: classes.dex */
    public class AnonymousClass1 extends IconLoadRequest {
        final /* synthetic */ ItemInfoUpdateReceiver val$caller;
        final /* synthetic */ ItemInfoWithIcon val$info;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        AnonymousClass1(Handler handler, Runnable runnable, ItemInfoWithIcon itemInfoWithIcon, ItemInfoUpdateReceiver itemInfoUpdateReceiver) {
            super(handler, runnable);
            this.val$info = itemInfoWithIcon;
            this.val$caller = itemInfoUpdateReceiver;
        }

        @Override // java.lang.Runnable
        public void run() {
            if ((this.val$info instanceof AppInfo) || (this.val$info instanceof ShortcutInfo)) {
                IconCache.this.getTitleAndIcon(this.val$info, false);
            } else if (this.val$info instanceof PackageItemInfo) {
                IconCache.this.getTitleAndIconForApp((PackageItemInfo) this.val$info, false);
            }
            MainThreadExecutor mainThreadExecutor = IconCache.this.mMainThreadExecutor;
            final ItemInfoUpdateReceiver itemInfoUpdateReceiver = this.val$caller;
            final ItemInfoWithIcon itemInfoWithIcon = this.val$info;
            mainThreadExecutor.execute(new Runnable() { // from class: com.android.launcher3.-$$Lambda$IconCache$1$TK_u5pHJBKCYnl2rjJTHPBeKfeI
                @Override // java.lang.Runnable
                public final void run() {
                    IconCache.AnonymousClass1.lambda$run$0(IconCache.AnonymousClass1.this, itemInfoUpdateReceiver, itemInfoWithIcon);
                }
            });
        }

        public static /* synthetic */ void lambda$run$0(AnonymousClass1 anonymousClass1, ItemInfoUpdateReceiver itemInfoUpdateReceiver, ItemInfoWithIcon itemInfoWithIcon) {
            itemInfoUpdateReceiver.reapplyItemInfo(itemInfoWithIcon);
            anonymousClass1.onEnd();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onIconRequestEnd() {
        this.mPendingIconRequestCount--;
        if (this.mPendingIconRequestCount <= 0) {
            LauncherModel.setWorkerPriority(10);
        }
    }

    public synchronized void updateTitleAndIcon(AppInfo appInfo) {
        CacheEntry cacheLocked = cacheLocked(appInfo.componentName, Provider.of(null), appInfo.user, false, appInfo.usingLowResIcon);
        if (cacheLocked.icon != null && !isDefaultIcon(cacheLocked.icon, appInfo.user)) {
            applyCacheEntry(cacheLocked, appInfo);
        }
    }

    public synchronized void getTitleAndIcon(ItemInfoWithIcon itemInfoWithIcon, LauncherActivityInfo launcherActivityInfo, boolean z) {
        getTitleAndIcon(itemInfoWithIcon, Provider.of(launcherActivityInfo), false, z);
    }

    public synchronized void getTitleAndIcon(ItemInfoWithIcon itemInfoWithIcon, boolean z) {
        if (itemInfoWithIcon.getTargetComponent() == null) {
            getDefaultIcon(itemInfoWithIcon.user).applyTo(itemInfoWithIcon);
            itemInfoWithIcon.title = "";
            itemInfoWithIcon.contentDescription = "";
            itemInfoWithIcon.usingLowResIcon = false;
        } else {
            getTitleAndIcon(itemInfoWithIcon, new ActivityInfoProvider(itemInfoWithIcon.getIntent(), itemInfoWithIcon.user), true, z);
        }
    }

    private synchronized void getTitleAndIcon(@NonNull ItemInfoWithIcon itemInfoWithIcon, @NonNull Provider<LauncherActivityInfo> provider, boolean z, boolean z2) {
        applyCacheEntry(cacheLocked(itemInfoWithIcon.getTargetComponent(), provider, itemInfoWithIcon.user, z, z2), itemInfoWithIcon);
    }

    public synchronized void getTitleAndIconForApp(PackageItemInfo packageItemInfo, boolean z) {
        applyCacheEntry(getEntryForPackageLocked(packageItemInfo.packageName, packageItemInfo.user, z), packageItemInfo);
    }

    private void applyCacheEntry(CacheEntry cacheEntry, ItemInfoWithIcon itemInfoWithIcon) {
        itemInfoWithIcon.title = Utilities.trim(cacheEntry.title);
        itemInfoWithIcon.contentDescription = cacheEntry.contentDescription;
        itemInfoWithIcon.usingLowResIcon = cacheEntry.isLowResIcon;
        Bitmap bitmap = cacheEntry.icon;
        CacheEntry cacheEntry2 = cacheEntry;
        if (bitmap == null) {
            cacheEntry2 = getDefaultIcon(itemInfoWithIcon.user);
        }
        cacheEntry2.applyTo(itemInfoWithIcon);
    }

    public synchronized BitmapInfo getDefaultIcon(UserHandle userHandle) {
        if (!this.mDefaultIcons.containsKey(userHandle)) {
            this.mDefaultIcons.put(userHandle, makeDefaultIcon(userHandle));
        }
        return this.mDefaultIcons.get(userHandle);
    }

    public boolean isDefaultIcon(Bitmap bitmap, UserHandle userHandle) {
        return getDefaultIcon(userHandle).icon == bitmap;
    }

    protected CacheEntry cacheLocked(@NonNull ComponentName componentName, @NonNull Provider<LauncherActivityInfo> provider, UserHandle userHandle, boolean z, boolean z2) {
        boolean z3;
        CacheEntry entryForPackageLocked;
        Preconditions.assertWorkerThread();
        ComponentKey componentKey = new ComponentKey(componentName, userHandle);
        CacheEntry cacheEntry = this.mCache.get(componentKey);
        if (cacheEntry == null || (cacheEntry.isLowResIcon && !z2)) {
            cacheEntry = new CacheEntry();
            this.mCache.put(componentKey, cacheEntry);
            LauncherActivityInfo launcherActivityInfo = null;
            if (!getEntryFromDB(componentKey, cacheEntry, z2)) {
                launcherActivityInfo = provider.get();
                z3 = true;
                if (launcherActivityInfo != null) {
                    LauncherIcons obtain = LauncherIcons.obtain(this.mContext);
                    obtain.createBadgedIconBitmap(getFullResIcon(launcherActivityInfo), launcherActivityInfo.getUser(), launcherActivityInfo.getApplicationInfo().targetSdkVersion).applyTo(cacheEntry);
                    obtain.recycle();
                } else {
                    if (z && (entryForPackageLocked = getEntryForPackageLocked(componentName.getPackageName(), userHandle, false)) != null) {
                        entryForPackageLocked.applyTo(cacheEntry);
                        cacheEntry.title = entryForPackageLocked.title;
                        cacheEntry.contentDescription = entryForPackageLocked.contentDescription;
                    }
                    if (cacheEntry.icon == null) {
                        getDefaultIcon(userHandle).applyTo(cacheEntry);
                    }
                }
            } else {
                z3 = false;
            }
            if (TextUtils.isEmpty(cacheEntry.title)) {
                if (launcherActivityInfo == null && !z3) {
                    launcherActivityInfo = provider.get();
                }
                if (launcherActivityInfo != null) {
                    cacheEntry.title = launcherActivityInfo.getLabel();
                    cacheEntry.contentDescription = this.mUserManager.getBadgedLabelForUser(cacheEntry.title, userHandle);
                }
            }
        }
        return cacheEntry;
    }

    public synchronized void clear() {
        Preconditions.assertWorkerThread();
        this.mIconDb.clear();
    }

    public synchronized void cachePackageInstallInfo(String str, UserHandle userHandle, Bitmap bitmap, CharSequence charSequence) {
        removeFromMemCacheLocked(str, userHandle);
        ComponentKey packageKey = getPackageKey(str, userHandle);
        CacheEntry cacheEntry = this.mCache.get(packageKey);
        if (cacheEntry == null) {
            cacheEntry = new CacheEntry();
        }
        if (!TextUtils.isEmpty(charSequence)) {
            cacheEntry.title = charSequence;
        }
        if (bitmap != null) {
            LauncherIcons obtain = LauncherIcons.obtain(this.mContext);
            obtain.createIconBitmap(bitmap).applyTo(cacheEntry);
            obtain.recycle();
        }
        if (!TextUtils.isEmpty(charSequence) && cacheEntry.icon != null) {
            this.mCache.put(packageKey, cacheEntry);
        }
    }

    private static ComponentKey getPackageKey(String str, UserHandle userHandle) {
        return new ComponentKey(new ComponentName(str, str + EMPTY_CLASS_NAME), userHandle);
    }

    private CacheEntry getEntryForPackageLocked(String str, UserHandle userHandle, boolean z) {
        int i;
        Preconditions.assertWorkerThread();
        ComponentKey packageKey = getPackageKey(str, userHandle);
        CacheEntry cacheEntry = this.mCache.get(packageKey);
        if (cacheEntry == null || (cacheEntry.isLowResIcon && !z)) {
            CacheEntry cacheEntry2 = new CacheEntry();
            boolean z2 = true;
            if (!getEntryFromDB(packageKey, cacheEntry2, z)) {
                try {
                    if (!Process.myUserHandle().equals(userHandle)) {
                        i = 8192;
                    } else {
                        i = 0;
                    }
                    PackageInfo packageInfo = this.mPackageManager.getPackageInfo(str, i);
                    ApplicationInfo applicationInfo = packageInfo.applicationInfo;
                    if (applicationInfo == null) {
                        throw new PackageManager.NameNotFoundException("ApplicationInfo is null");
                    }
                    LauncherIcons obtain = LauncherIcons.obtain(this.mContext);
                    BitmapInfo createBadgedIconBitmap = obtain.createBadgedIconBitmap(applicationInfo.loadIcon(this.mPackageManager), userHandle, applicationInfo.targetSdkVersion, this.mInstantAppResolver.isInstantApp(applicationInfo));
                    obtain.recycle();
                    Bitmap generateLowResIcon = generateLowResIcon(createBadgedIconBitmap.icon);
                    cacheEntry2.title = applicationInfo.loadLabel(this.mPackageManager);
                    cacheEntry2.contentDescription = this.mUserManager.getBadgedLabelForUser(cacheEntry2.title, userHandle);
                    cacheEntry2.icon = z ? generateLowResIcon : createBadgedIconBitmap.icon;
                    cacheEntry2.color = createBadgedIconBitmap.color;
                    cacheEntry2.isLowResIcon = z;
                    addIconToDB(newContentValues(createBadgedIconBitmap.icon, generateLowResIcon, cacheEntry2.color, cacheEntry2.title.toString(), str), packageKey.componentName, packageInfo, this.mUserManager.getSerialNumberForUser(userHandle));
                } catch (PackageManager.NameNotFoundException e) {
                    z2 = false;
                }
            }
            if (z2) {
                this.mCache.put(packageKey, cacheEntry2);
            }
            return cacheEntry2;
        }
        return cacheEntry;
    }

    private boolean getEntryFromDB(ComponentKey componentKey, CacheEntry cacheEntry, boolean z) {
        Cursor query;
        boolean moveToNext;
        Cursor cursor = null;
        try {
            try {
                IconDB iconDB = this.mIconDb;
                String[] strArr = new String[3];
                strArr[0] = z ? "icon_low_res" : LauncherSettings.BaseLauncherColumns.ICON;
                strArr[1] = "icon_color";
                strArr[2] = "label";
                query = iconDB.query(strArr, "componentName = ? AND profileId = ?", new String[]{componentKey.componentName.flattenToString(), Long.toString(this.mUserManager.getSerialNumberForUser(componentKey.user))});
            } catch (Throwable th) {
                th = th;
            }
        } catch (SQLiteException e) {
            e = e;
        }
        try {
            moveToNext = query.moveToNext();
            cursor = moveToNext;
        } catch (SQLiteException e2) {
            e = e2;
            cursor = query;
            Log.d(TAG, "Error reading icon cache", e);
            cursor = cursor;
            if (cursor != null) {
                cursor.close();
                cursor = cursor;
            }
            return false;
        } catch (Throwable th2) {
            th = th2;
            cursor = query;
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
        if (!moveToNext) {
            if (query != null) {
                query.close();
                cursor = moveToNext;
            }
            return false;
        }
        cacheEntry.icon = loadIconNoResize(query, 0, z ? this.mLowResOptions : this.mHighResOptions);
        cacheEntry.color = ColorUtils.setAlphaComponent(query.getInt(1), 255);
        cacheEntry.isLowResIcon = z;
        cacheEntry.title = query.getString(2);
        if (cacheEntry.title == null) {
            cacheEntry.title = "";
            cacheEntry.contentDescription = "";
        } else {
            cacheEntry.contentDescription = this.mUserManager.getBadgedLabelForUser(cacheEntry.title, componentKey.user);
        }
        if (query != null) {
            query.close();
        }
        return true;
    }

    /* loaded from: classes.dex */
    public static abstract class IconLoadRequest implements Runnable {
        private final Runnable mEndRunnable;
        private boolean mEnded = false;
        private final Handler mHandler;

        IconLoadRequest(Handler handler, Runnable runnable) {
            this.mHandler = handler;
            this.mEndRunnable = runnable;
        }

        public void cancel() {
            this.mHandler.removeCallbacks(this);
            onEnd();
        }

        public void onEnd() {
            if (!this.mEnded) {
                this.mEnded = true;
                this.mEndRunnable.run();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public class SerializedIconUpdateTask implements Runnable {
        private final Stack<LauncherActivityInfo> mAppsToAdd;
        private final Stack<LauncherActivityInfo> mAppsToUpdate;
        private final HashMap<String, PackageInfo> mPkgInfoMap;
        private final HashSet<String> mUpdatedPackages = new HashSet<>();
        private final long mUserSerial;

        SerializedIconUpdateTask(long j, HashMap<String, PackageInfo> hashMap, Stack<LauncherActivityInfo> stack, Stack<LauncherActivityInfo> stack2) {
            this.mUserSerial = j;
            this.mPkgInfoMap = hashMap;
            this.mAppsToAdd = stack;
            this.mAppsToUpdate = stack2;
        }

        @Override // java.lang.Runnable
        public void run() {
            if (this.mAppsToUpdate.isEmpty()) {
                if (!this.mAppsToAdd.isEmpty()) {
                    LauncherActivityInfo pop = this.mAppsToAdd.pop();
                    PackageInfo packageInfo = this.mPkgInfoMap.get(pop.getComponentName().getPackageName());
                    if (packageInfo != null) {
                        IconCache.this.addIconToDBAndMemCache(pop, packageInfo, this.mUserSerial, false);
                    }
                    if (!this.mAppsToAdd.isEmpty()) {
                        scheduleNext();
                        return;
                    }
                    return;
                }
                return;
            }
            LauncherActivityInfo pop2 = this.mAppsToUpdate.pop();
            String packageName = pop2.getComponentName().getPackageName();
            IconCache.this.addIconToDBAndMemCache(pop2, this.mPkgInfoMap.get(packageName), this.mUserSerial, true);
            this.mUpdatedPackages.add(packageName);
            if (this.mAppsToUpdate.isEmpty() && !this.mUpdatedPackages.isEmpty()) {
                LauncherAppState.getInstance(IconCache.this.mContext).getModel().onPackageIconsUpdated(this.mUpdatedPackages, IconCache.this.mUserManager.getUserForSerialNumber(this.mUserSerial));
            }
            scheduleNext();
        }

        public void scheduleNext() {
            IconCache.this.mWorkerHandler.postAtTime(this, IconCache.ICON_UPDATE_TOKEN, SystemClock.uptimeMillis() + 1);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static final class IconDB extends SQLiteCacheHelper {
        private static final String COLUMN_COMPONENT = "componentName";
        private static final String COLUMN_ICON = "icon";
        private static final String COLUMN_ICON_COLOR = "icon_color";
        private static final String COLUMN_ICON_LOW_RES = "icon_low_res";
        private static final String COLUMN_LABEL = "label";
        private static final String COLUMN_LAST_UPDATED = "lastUpdated";
        private static final String COLUMN_ROWID = "rowid";
        private static final String COLUMN_SYSTEM_STATE = "system_state";
        private static final String COLUMN_USER = "profileId";
        private static final String COLUMN_VERSION = "version";
        private static final int RELEASE_VERSION = 22;
        private static final String TABLE_NAME = "icons";

        public IconDB(Context context, int i) {
            super(context, LauncherFiles.APP_ICONS_DB, 1441792 + i, TABLE_NAME);
        }

        @Override // com.android.launcher3.util.SQLiteCacheHelper
        protected void onCreateTable(SQLiteDatabase sQLiteDatabase) {
            sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS icons (componentName TEXT NOT NULL, profileId INTEGER NOT NULL, lastUpdated INTEGER NOT NULL DEFAULT 0, version INTEGER NOT NULL DEFAULT 0, icon BLOB, icon_low_res BLOB, icon_color INTEGER NOT NULL DEFAULT 0, label TEXT, system_state TEXT, PRIMARY KEY (componentName, profileId) );");
        }
    }

    private ContentValues newContentValues(Bitmap bitmap, Bitmap bitmap2, int i, String str, String str2) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(LauncherSettings.BaseLauncherColumns.ICON, Utilities.flattenBitmap(bitmap));
        contentValues.put("icon_low_res", Utilities.flattenBitmap(bitmap2));
        contentValues.put("icon_color", Integer.valueOf(i));
        contentValues.put("label", str);
        contentValues.put("system_state", this.mIconProvider.getIconSystemState(str2));
        return contentValues;
    }

    private Bitmap generateLowResIcon(Bitmap bitmap) {
        return Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() / 5, bitmap.getHeight() / 5, true);
    }

    private static Bitmap loadIconNoResize(Cursor cursor, int i, BitmapFactory.Options options) {
        byte[] blob = cursor.getBlob(i);
        try {
            return BitmapFactory.decodeByteArray(blob, 0, blob.length, options);
        } catch (Exception e) {
            return null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class ActivityInfoProvider extends Provider<LauncherActivityInfo> {
        private final Intent mIntent;
        private final UserHandle mUser;

        public ActivityInfoProvider(Intent intent, UserHandle userHandle) {
            this.mIntent = intent;
            this.mUser = userHandle;
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // com.android.launcher3.util.Provider
        public LauncherActivityInfo get() {
            return IconCache.this.mLauncherApps.resolveActivity(this.mIntent, this.mUser);
        }
    }
}
