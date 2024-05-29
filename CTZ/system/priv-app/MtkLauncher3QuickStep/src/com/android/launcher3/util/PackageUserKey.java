package com.android.launcher3.util;

import android.os.UserHandle;
import android.service.notification.StatusBarNotification;
import com.android.launcher3.ItemInfo;
import com.android.launcher3.shortcuts.DeepShortcutManager;
import java.util.Arrays;
/* loaded from: classes.dex */
public class PackageUserKey {
    private int mHashCode;
    public String mPackageName;
    public UserHandle mUser;

    public static PackageUserKey fromItemInfo(ItemInfo itemInfo) {
        return new PackageUserKey(itemInfo.getTargetComponent().getPackageName(), itemInfo.user);
    }

    public static PackageUserKey fromNotification(StatusBarNotification statusBarNotification) {
        return new PackageUserKey(statusBarNotification.getPackageName(), statusBarNotification.getUser());
    }

    public PackageUserKey(String str, UserHandle userHandle) {
        update(str, userHandle);
    }

    private void update(String str, UserHandle userHandle) {
        this.mPackageName = str;
        this.mUser = userHandle;
        this.mHashCode = Arrays.hashCode(new Object[]{str, userHandle});
    }

    public boolean updateFromItemInfo(ItemInfo itemInfo) {
        if (DeepShortcutManager.supportsShortcuts(itemInfo)) {
            update(itemInfo.getTargetComponent().getPackageName(), itemInfo.user);
            return true;
        }
        return false;
    }

    public int hashCode() {
        return this.mHashCode;
    }

    public boolean equals(Object obj) {
        if (obj instanceof PackageUserKey) {
            PackageUserKey packageUserKey = (PackageUserKey) obj;
            return this.mPackageName.equals(packageUserKey.mPackageName) && this.mUser.equals(packageUserKey.mUser);
        }
        return false;
    }
}
