package com.android.launcher3.badge;

import com.android.launcher3.notification.NotificationKeyData;
import com.android.launcher3.util.PackageUserKey;
import java.util.ArrayList;
import java.util.List;
/* loaded from: classes.dex */
public class BadgeInfo {
    public static final int MAX_COUNT = 999;
    private List<NotificationKeyData> mNotificationKeys = new ArrayList();
    private PackageUserKey mPackageUserKey;
    private int mTotalCount;

    public BadgeInfo(PackageUserKey packageUserKey) {
        this.mPackageUserKey = packageUserKey;
    }

    public boolean addOrUpdateNotificationKey(NotificationKeyData notificationKeyData) {
        int indexOf = this.mNotificationKeys.indexOf(notificationKeyData);
        NotificationKeyData notificationKeyData2 = indexOf == -1 ? null : this.mNotificationKeys.get(indexOf);
        if (notificationKeyData2 != null) {
            if (notificationKeyData2.count == notificationKeyData.count) {
                return false;
            }
            this.mTotalCount -= notificationKeyData2.count;
            this.mTotalCount += notificationKeyData.count;
            notificationKeyData2.count = notificationKeyData.count;
            return true;
        }
        boolean add = this.mNotificationKeys.add(notificationKeyData);
        if (add) {
            this.mTotalCount += notificationKeyData.count;
        }
        return add;
    }

    public boolean removeNotificationKey(NotificationKeyData notificationKeyData) {
        boolean remove = this.mNotificationKeys.remove(notificationKeyData);
        if (remove) {
            this.mTotalCount -= notificationKeyData.count;
        }
        return remove;
    }

    public List<NotificationKeyData> getNotificationKeys() {
        return this.mNotificationKeys;
    }

    public int getNotificationCount() {
        return Math.min(this.mTotalCount, (int) MAX_COUNT);
    }

    public boolean shouldBeInvalidated(BadgeInfo badgeInfo) {
        return this.mPackageUserKey.equals(badgeInfo.mPackageUserKey) && getNotificationCount() != badgeInfo.getNotificationCount();
    }
}
