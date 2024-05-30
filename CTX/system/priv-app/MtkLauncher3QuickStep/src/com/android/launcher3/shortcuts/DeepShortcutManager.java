package com.android.launcher3.shortcuts;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.LauncherApps;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.UserHandle;
import android.util.Log;
import com.android.launcher3.ItemInfo;
import com.android.launcher3.ShortcutInfo;
import com.android.launcher3.Utilities;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/* loaded from: classes.dex */
public class DeepShortcutManager {
    private static final int FLAG_GET_ALL = 11;
    private static final String TAG = "DeepShortcutManager";
    private static DeepShortcutManager sInstance;
    private static final Object sInstanceLock = new Object();
    private final LauncherApps mLauncherApps;
    private boolean mWasLastCallSuccess;

    public static DeepShortcutManager getInstance(Context context) {
        DeepShortcutManager deepShortcutManager;
        synchronized (sInstanceLock) {
            if (sInstance == null) {
                sInstance = new DeepShortcutManager(context.getApplicationContext());
            }
            deepShortcutManager = sInstance;
        }
        return deepShortcutManager;
    }

    private DeepShortcutManager(Context context) {
        this.mLauncherApps = (LauncherApps) context.getSystemService("launcherapps");
    }

    public static boolean supportsShortcuts(ItemInfo itemInfo) {
        return (itemInfo.itemType != 0 || itemInfo.isDisabled() || ((itemInfo instanceof ShortcutInfo) && ((ShortcutInfo) itemInfo).hasPromiseIconUi())) ? false : true;
    }

    public boolean wasLastCallSuccess() {
        return this.mWasLastCallSuccess;
    }

    public void onShortcutsChanged(List<ShortcutInfoCompat> list) {
    }

    public List<ShortcutInfoCompat> queryForFullDetails(String str, List<String> list, UserHandle userHandle) {
        return query(11, str, null, list, userHandle);
    }

    public List<ShortcutInfoCompat> queryForShortcutsContainer(ComponentName componentName, List<String> list, UserHandle userHandle) {
        return query(9, componentName.getPackageName(), componentName, list, userHandle);
    }

    @TargetApi(25)
    public void unpinShortcut(ShortcutKey shortcutKey) {
        if (Utilities.ATLEAST_NOUGAT_MR1) {
            String packageName = shortcutKey.componentName.getPackageName();
            String id = shortcutKey.getId();
            UserHandle userHandle = shortcutKey.user;
            List<String> extractIds = extractIds(queryForPinnedShortcuts(packageName, userHandle));
            extractIds.remove(id);
            try {
                this.mLauncherApps.pinShortcuts(packageName, extractIds, userHandle);
                this.mWasLastCallSuccess = true;
            } catch (IllegalStateException | SecurityException e) {
                Log.w(TAG, "Failed to unpin shortcut", e);
                this.mWasLastCallSuccess = false;
            }
        }
    }

    @TargetApi(25)
    public void pinShortcut(ShortcutKey shortcutKey) {
        if (Utilities.ATLEAST_NOUGAT_MR1) {
            String packageName = shortcutKey.componentName.getPackageName();
            String id = shortcutKey.getId();
            UserHandle userHandle = shortcutKey.user;
            List<String> extractIds = extractIds(queryForPinnedShortcuts(packageName, userHandle));
            extractIds.add(id);
            try {
                this.mLauncherApps.pinShortcuts(packageName, extractIds, userHandle);
                this.mWasLastCallSuccess = true;
            } catch (IllegalStateException | SecurityException e) {
                Log.w(TAG, "Failed to pin shortcut", e);
                this.mWasLastCallSuccess = false;
            }
        }
    }

    @TargetApi(25)
    public void startShortcut(String str, String str2, Rect rect, Bundle bundle, UserHandle userHandle) {
        if (Utilities.ATLEAST_NOUGAT_MR1) {
            try {
                this.mLauncherApps.startShortcut(str, str2, rect, bundle, userHandle);
                this.mWasLastCallSuccess = true;
            } catch (IllegalStateException | SecurityException e) {
                Log.e(TAG, "Failed to start shortcut", e);
                this.mWasLastCallSuccess = false;
            }
        }
    }

    @TargetApi(25)
    public Drawable getShortcutIconDrawable(ShortcutInfoCompat shortcutInfoCompat, int i) {
        if (Utilities.ATLEAST_NOUGAT_MR1) {
            try {
                Drawable shortcutIconDrawable = this.mLauncherApps.getShortcutIconDrawable(shortcutInfoCompat.getShortcutInfo(), i);
                this.mWasLastCallSuccess = true;
                return shortcutIconDrawable;
            } catch (IllegalStateException | SecurityException e) {
                Log.e(TAG, "Failed to get shortcut icon", e);
                this.mWasLastCallSuccess = false;
                return null;
            }
        }
        return null;
    }

    public List<ShortcutInfoCompat> queryForPinnedShortcuts(String str, UserHandle userHandle) {
        return query(2, str, null, null, userHandle);
    }

    public List<ShortcutInfoCompat> queryForAllShortcuts(UserHandle userHandle) {
        return query(11, null, null, null, userHandle);
    }

    private List<String> extractIds(List<ShortcutInfoCompat> list) {
        ArrayList arrayList = new ArrayList(list.size());
        for (ShortcutInfoCompat shortcutInfoCompat : list) {
            arrayList.add(shortcutInfoCompat.getId());
        }
        return arrayList;
    }

    /* JADX WARN: Removed duplicated region for block: B:17:0x0036  */
    /* JADX WARN: Removed duplicated region for block: B:19:0x0039  */
    @TargetApi(25)
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private List<ShortcutInfoCompat> query(int i, String str, ComponentName componentName, List<String> list, UserHandle userHandle) {
        List<android.content.pm.ShortcutInfo> shortcuts;
        if (Utilities.ATLEAST_NOUGAT_MR1) {
            LauncherApps.ShortcutQuery shortcutQuery = new LauncherApps.ShortcutQuery();
            shortcutQuery.setQueryFlags(i);
            if (str != null) {
                shortcutQuery.setPackage(str);
                shortcutQuery.setActivity(componentName);
                shortcutQuery.setShortcutIds(list);
            }
            List<android.content.pm.ShortcutInfo> list2 = null;
            try {
                shortcuts = this.mLauncherApps.getShortcuts(shortcutQuery, userHandle);
            } catch (IllegalStateException | SecurityException e) {
                e = e;
            }
            try {
                this.mWasLastCallSuccess = true;
                list2 = shortcuts;
            } catch (IllegalStateException | SecurityException e2) {
                e = e2;
                list2 = shortcuts;
                Log.e(TAG, "Failed to query for shortcuts", e);
                this.mWasLastCallSuccess = false;
                if (list2 != null) {
                }
            }
            if (list2 != null) {
                return Collections.EMPTY_LIST;
            }
            ArrayList arrayList = new ArrayList(list2.size());
            for (android.content.pm.ShortcutInfo shortcutInfo : list2) {
                arrayList.add(new ShortcutInfoCompat(shortcutInfo));
            }
            return arrayList;
        }
        return Collections.EMPTY_LIST;
    }

    @TargetApi(25)
    public boolean hasHostPermission() {
        if (Utilities.ATLEAST_NOUGAT_MR1) {
            try {
                return this.mLauncherApps.hasShortcutHostPermission();
            } catch (IllegalStateException | SecurityException e) {
                Log.e(TAG, "Failed to make shortcut manager call", e);
                return false;
            }
        }
        return false;
    }
}
