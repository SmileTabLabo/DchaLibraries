package com.android.launcher3.popup;

import android.content.ComponentName;
import android.service.notification.StatusBarNotification;
import android.support.annotation.NonNull;
import com.android.launcher3.ItemInfo;
import com.android.launcher3.Launcher;
import com.android.launcher3.Utilities;
import com.android.launcher3.badge.BadgeInfo;
import com.android.launcher3.model.WidgetItem;
import com.android.launcher3.notification.NotificationKeyData;
import com.android.launcher3.notification.NotificationListener;
import com.android.launcher3.popup.SystemShortcut;
import com.android.launcher3.shortcuts.DeepShortcutManager;
import com.android.launcher3.util.ComponentKey;
import com.android.launcher3.util.MultiHashMap;
import com.android.launcher3.util.PackageUserKey;
import com.android.launcher3.widget.WidgetListRowEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
/* loaded from: classes.dex */
public class PopupDataProvider implements NotificationListener.NotificationsChangedListener {
    private static final boolean LOGD = false;
    private static final SystemShortcut[] SYSTEM_SHORTCUTS = {new SystemShortcut.AppInfo(), new SystemShortcut.Widgets(), new SystemShortcut.Install()};
    private static final String TAG = "PopupDataProvider";
    private final Launcher mLauncher;
    private MultiHashMap<ComponentKey, String> mDeepShortcutMap = new MultiHashMap<>();
    private Map<PackageUserKey, BadgeInfo> mPackageUserToBadgeInfos = new HashMap();
    private ArrayList<WidgetListRowEntry> mAllWidgets = new ArrayList<>();

    public PopupDataProvider(Launcher launcher) {
        this.mLauncher = launcher;
    }

    @Override // com.android.launcher3.notification.NotificationListener.NotificationsChangedListener
    public void onNotificationPosted(PackageUserKey packageUserKey, NotificationKeyData notificationKeyData, boolean z) {
        boolean addOrUpdateNotificationKey;
        BadgeInfo badgeInfo = this.mPackageUserToBadgeInfos.get(packageUserKey);
        if (badgeInfo == null) {
            if (!z) {
                BadgeInfo badgeInfo2 = new BadgeInfo(packageUserKey);
                badgeInfo2.addOrUpdateNotificationKey(notificationKeyData);
                this.mPackageUserToBadgeInfos.put(packageUserKey, badgeInfo2);
                addOrUpdateNotificationKey = true;
            } else {
                addOrUpdateNotificationKey = false;
            }
        } else {
            if (z) {
                addOrUpdateNotificationKey = badgeInfo.removeNotificationKey(notificationKeyData);
            } else {
                addOrUpdateNotificationKey = badgeInfo.addOrUpdateNotificationKey(notificationKeyData);
            }
            if (badgeInfo.getNotificationKeys().size() == 0) {
                this.mPackageUserToBadgeInfos.remove(packageUserKey);
            }
        }
        if (addOrUpdateNotificationKey) {
            this.mLauncher.updateIconBadges(Utilities.singletonHashSet(packageUserKey));
        }
    }

    @Override // com.android.launcher3.notification.NotificationListener.NotificationsChangedListener
    public void onNotificationRemoved(PackageUserKey packageUserKey, NotificationKeyData notificationKeyData) {
        BadgeInfo badgeInfo = this.mPackageUserToBadgeInfos.get(packageUserKey);
        if (badgeInfo != null && badgeInfo.removeNotificationKey(notificationKeyData)) {
            if (badgeInfo.getNotificationKeys().size() == 0) {
                this.mPackageUserToBadgeInfos.remove(packageUserKey);
            }
            this.mLauncher.updateIconBadges(Utilities.singletonHashSet(packageUserKey));
            trimNotifications(this.mPackageUserToBadgeInfos);
        }
    }

    @Override // com.android.launcher3.notification.NotificationListener.NotificationsChangedListener
    public void onNotificationFullRefresh(List<StatusBarNotification> list) {
        if (list == null) {
            return;
        }
        HashMap hashMap = new HashMap(this.mPackageUserToBadgeInfos);
        this.mPackageUserToBadgeInfos.clear();
        for (StatusBarNotification statusBarNotification : list) {
            PackageUserKey fromNotification = PackageUserKey.fromNotification(statusBarNotification);
            BadgeInfo badgeInfo = this.mPackageUserToBadgeInfos.get(fromNotification);
            if (badgeInfo == null) {
                badgeInfo = new BadgeInfo(fromNotification);
                this.mPackageUserToBadgeInfos.put(fromNotification, badgeInfo);
            }
            badgeInfo.addOrUpdateNotificationKey(NotificationKeyData.fromNotification(statusBarNotification));
        }
        for (PackageUserKey packageUserKey : this.mPackageUserToBadgeInfos.keySet()) {
            BadgeInfo badgeInfo2 = (BadgeInfo) hashMap.get(packageUserKey);
            BadgeInfo badgeInfo3 = this.mPackageUserToBadgeInfos.get(packageUserKey);
            if (badgeInfo2 == null) {
                hashMap.put(packageUserKey, badgeInfo3);
            } else if (!badgeInfo2.shouldBeInvalidated(badgeInfo3)) {
                hashMap.remove(packageUserKey);
            }
        }
        if (!hashMap.isEmpty()) {
            this.mLauncher.updateIconBadges(hashMap.keySet());
        }
        trimNotifications(hashMap);
    }

    private void trimNotifications(Map<PackageUserKey, BadgeInfo> map) {
        PopupContainerWithArrow open = PopupContainerWithArrow.getOpen(this.mLauncher);
        if (open != null) {
            open.trimNotifications(map);
        }
    }

    public void setDeepShortcutMap(MultiHashMap<ComponentKey, String> multiHashMap) {
        this.mDeepShortcutMap = multiHashMap;
    }

    public List<String> getShortcutIdsForItem(ItemInfo itemInfo) {
        if (!DeepShortcutManager.supportsShortcuts(itemInfo)) {
            return Collections.EMPTY_LIST;
        }
        ComponentName targetComponent = itemInfo.getTargetComponent();
        if (targetComponent == null) {
            return Collections.EMPTY_LIST;
        }
        List<String> list = (List) this.mDeepShortcutMap.get(new ComponentKey(targetComponent, itemInfo.user));
        return list == null ? Collections.EMPTY_LIST : list;
    }

    public BadgeInfo getBadgeInfoForItem(ItemInfo itemInfo) {
        if (!DeepShortcutManager.supportsShortcuts(itemInfo)) {
            return null;
        }
        return this.mPackageUserToBadgeInfos.get(PackageUserKey.fromItemInfo(itemInfo));
    }

    @NonNull
    public List<NotificationKeyData> getNotificationKeysForItem(ItemInfo itemInfo) {
        BadgeInfo badgeInfoForItem = getBadgeInfoForItem(itemInfo);
        return badgeInfoForItem == null ? Collections.EMPTY_LIST : badgeInfoForItem.getNotificationKeys();
    }

    @NonNull
    public List<StatusBarNotification> getStatusBarNotificationsForKeys(List<NotificationKeyData> list) {
        NotificationListener instanceIfConnected = NotificationListener.getInstanceIfConnected();
        return instanceIfConnected == null ? Collections.EMPTY_LIST : instanceIfConnected.getNotificationsForKeys(list);
    }

    @NonNull
    public List<SystemShortcut> getEnabledSystemShortcutsForItem(ItemInfo itemInfo) {
        SystemShortcut[] systemShortcutArr;
        ArrayList arrayList = new ArrayList();
        for (SystemShortcut systemShortcut : SYSTEM_SHORTCUTS) {
            if (systemShortcut.getOnClickListener(this.mLauncher, itemInfo) != null) {
                arrayList.add(systemShortcut);
            }
        }
        return arrayList;
    }

    public void cancelNotification(String str) {
        NotificationListener instanceIfConnected = NotificationListener.getInstanceIfConnected();
        if (instanceIfConnected == null) {
            return;
        }
        instanceIfConnected.cancelNotificationFromLauncher(str);
    }

    public void setAllWidgets(ArrayList<WidgetListRowEntry> arrayList) {
        this.mAllWidgets = arrayList;
    }

    public ArrayList<WidgetListRowEntry> getAllWidgets() {
        return this.mAllWidgets;
    }

    public List<WidgetItem> getWidgetsForPackageUser(PackageUserKey packageUserKey) {
        Iterator<WidgetListRowEntry> it = this.mAllWidgets.iterator();
        while (it.hasNext()) {
            WidgetListRowEntry next = it.next();
            if (next.pkgItem.packageName.equals(packageUserKey.mPackageName)) {
                ArrayList arrayList = new ArrayList(next.widgets);
                Iterator it2 = arrayList.iterator();
                while (it2.hasNext()) {
                    if (!((WidgetItem) it2.next()).user.equals(packageUserKey.mUser)) {
                        it2.remove();
                    }
                }
                if (arrayList.isEmpty()) {
                    return null;
                }
                return arrayList;
            }
        }
        return null;
    }
}
