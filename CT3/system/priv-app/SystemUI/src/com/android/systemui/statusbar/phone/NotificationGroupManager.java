package com.android.systemui.statusbar.phone;

import android.service.notification.StatusBarNotification;
import android.support.annotation.Nullable;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
/* loaded from: a.zip:com/android/systemui/statusbar/phone/NotificationGroupManager.class */
public class NotificationGroupManager implements HeadsUpManager.OnHeadsUpChangedListener {
    private HeadsUpManager mHeadsUpManager;
    private OnGroupChangeListener mListener;
    private final HashMap<String, NotificationGroup> mGroupMap = new HashMap<>();
    private int mBarState = -1;
    private HashMap<String, StatusBarNotification> mIsolatedEntries = new HashMap<>();

    /* loaded from: a.zip:com/android/systemui/statusbar/phone/NotificationGroupManager$NotificationGroup.class */
    public static class NotificationGroup {
        public final HashSet<NotificationData.Entry> children = new HashSet<>();
        public boolean expanded;
        public NotificationData.Entry summary;
        public boolean suppressed;

        public String toString() {
            Iterator<T> it;
            String str = ("    summary:\n      " + (this.summary != null ? this.summary.notification : "null")) + "\n    children size: " + this.children.size();
            while (this.children.iterator().hasNext()) {
                str = str + "\n      " + ((NotificationData.Entry) it.next()).notification;
            }
            return str;
        }
    }

    /* loaded from: a.zip:com/android/systemui/statusbar/phone/NotificationGroupManager$OnGroupChangeListener.class */
    public interface OnGroupChangeListener {
        void onGroupCreatedFromChildren(NotificationGroup notificationGroup);

        void onGroupExpansionChanged(ExpandableNotificationRow expandableNotificationRow, boolean z);

        void onGroupsChanged();
    }

    private String getGroupKey(StatusBarNotification statusBarNotification) {
        return isIsolated(statusBarNotification) ? statusBarNotification.getKey() : statusBarNotification.getGroupKey();
    }

    @Nullable
    private ExpandableNotificationRow getGroupSummary(String str) {
        ExpandableNotificationRow expandableNotificationRow;
        NotificationGroup notificationGroup = this.mGroupMap.get(str);
        if (notificationGroup == null) {
            expandableNotificationRow = null;
        } else {
            expandableNotificationRow = null;
            if (notificationGroup.summary != null) {
                expandableNotificationRow = notificationGroup.summary.row;
            }
        }
        return expandableNotificationRow;
    }

    private NotificationData.Entry getIsolatedChild(String str) {
        for (StatusBarNotification statusBarNotification : this.mIsolatedEntries.values()) {
            if (statusBarNotification.getGroupKey().equals(str) && isIsolated(statusBarNotification)) {
                return this.mGroupMap.get(statusBarNotification.getKey()).summary;
            }
        }
        return null;
    }

    private int getNumberOfIsolatedChildren(String str) {
        int i = 0;
        for (StatusBarNotification statusBarNotification : this.mIsolatedEntries.values()) {
            if (statusBarNotification.getGroupKey().equals(str) && isIsolated(statusBarNotification)) {
                i++;
            }
        }
        return i;
    }

    private int getTotalNumberOfChildren(StatusBarNotification statusBarNotification) {
        int numberOfIsolatedChildren = getNumberOfIsolatedChildren(statusBarNotification.getGroupKey());
        NotificationGroup notificationGroup = this.mGroupMap.get(statusBarNotification.getGroupKey());
        return numberOfIsolatedChildren + (notificationGroup != null ? notificationGroup.children.size() : 0);
    }

    private void handleSuppressedSummaryHeadsUpped(NotificationData.Entry entry) {
        NotificationData.Entry entry2 = null;
        StatusBarNotification statusBarNotification = entry.notification;
        if (isGroupSuppressed(statusBarNotification.getGroupKey()) && statusBarNotification.getNotification().isGroupSummary() && entry.row.isHeadsUp()) {
            NotificationGroup notificationGroup = this.mGroupMap.get(statusBarNotification.getGroupKey());
            if (notificationGroup != null) {
                Iterator<NotificationData.Entry> it = notificationGroup.children.iterator();
                if (it.hasNext()) {
                    entry2 = it.next();
                }
                NotificationData.Entry entry3 = entry2;
                if (entry2 == null) {
                    entry3 = getIsolatedChild(statusBarNotification.getGroupKey());
                }
                if (entry3 != null) {
                    if (this.mHeadsUpManager.isHeadsUp(entry3.key)) {
                        this.mHeadsUpManager.updateNotification(entry3, true);
                    } else {
                        this.mHeadsUpManager.showNotification(entry3);
                    }
                }
            }
            this.mHeadsUpManager.releaseImmediately(entry.key);
        }
    }

    private boolean hasIsolatedChildren(NotificationGroup notificationGroup) {
        boolean z = false;
        if (getNumberOfIsolatedChildren(notificationGroup.summary.notification.getGroupKey()) != 0) {
            z = true;
        }
        return z;
    }

    private boolean isGroupChild(StatusBarNotification statusBarNotification) {
        if (isIsolated(statusBarNotification)) {
            return false;
        }
        boolean z = false;
        if (statusBarNotification.isGroup()) {
            z = !statusBarNotification.getNotification().isGroupSummary();
        }
        return z;
    }

    private boolean isGroupNotFullyVisible(NotificationGroup notificationGroup) {
        boolean z = true;
        if (notificationGroup.summary != null) {
            if (notificationGroup.summary.row.getClipTopAmount() > 0) {
                z = true;
            } else {
                z = true;
                if (notificationGroup.summary.row.getTranslationY() >= 0.0f) {
                    z = false;
                }
            }
        }
        return z;
    }

    private boolean isGroupSummary(StatusBarNotification statusBarNotification) {
        if (isIsolated(statusBarNotification)) {
            return true;
        }
        return statusBarNotification.getNotification().isGroupSummary();
    }

    private boolean isGroupSuppressed(String str) {
        NotificationGroup notificationGroup = this.mGroupMap.get(str);
        return notificationGroup != null ? notificationGroup.suppressed : false;
    }

    private boolean isIsolated(StatusBarNotification statusBarNotification) {
        return this.mIsolatedEntries.containsKey(statusBarNotification.getKey());
    }

    private boolean isOnlyChild(StatusBarNotification statusBarNotification) {
        boolean z = true;
        if (statusBarNotification.getNotification().isGroupSummary()) {
            z = false;
        } else if (getTotalNumberOfChildren(statusBarNotification) != 1) {
            z = false;
        }
        return z;
    }

    private void onEntryBecomingChild(NotificationData.Entry entry) {
        if (entry.row.isHeadsUp()) {
            onHeadsUpStateChanged(entry, true);
        }
    }

    private void onEntryRemovedInternal(NotificationData.Entry entry, StatusBarNotification statusBarNotification) {
        String groupKey = getGroupKey(statusBarNotification);
        NotificationGroup notificationGroup = this.mGroupMap.get(groupKey);
        if (notificationGroup == null) {
            return;
        }
        if (isGroupChild(statusBarNotification)) {
            notificationGroup.children.remove(entry);
        } else {
            notificationGroup.summary = null;
        }
        updateSuppression(notificationGroup);
        if (notificationGroup.children.isEmpty() && notificationGroup.summary == null) {
            this.mGroupMap.remove(groupKey);
        }
    }

    private void setGroupExpanded(NotificationGroup notificationGroup, boolean z) {
        notificationGroup.expanded = z;
        if (notificationGroup.summary != null) {
            this.mListener.onGroupExpansionChanged(notificationGroup.summary.row, z);
        }
    }

    private boolean shouldIsolate(StatusBarNotification statusBarNotification) {
        NotificationGroup notificationGroup = this.mGroupMap.get(statusBarNotification.getGroupKey());
        return (!statusBarNotification.isGroup() || statusBarNotification.getNotification().isGroupSummary()) ? false : (statusBarNotification.getNotification().fullScreenIntent == null && notificationGroup != null && notificationGroup.expanded) ? isGroupNotFullyVisible(notificationGroup) : true;
    }

    private void updateSuppression(NotificationGroup notificationGroup) {
        boolean z = true;
        if (notificationGroup == null) {
            return;
        }
        boolean z2 = notificationGroup.suppressed;
        if (notificationGroup.summary == null || notificationGroup.expanded) {
            z = false;
        } else if (notificationGroup.children.size() != 1) {
            z = (notificationGroup.children.size() == 0 && notificationGroup.summary.notification.getNotification().isGroupSummary()) ? hasIsolatedChildren(notificationGroup) : false;
        }
        notificationGroup.suppressed = z;
        if (z2 != notificationGroup.suppressed) {
            if (notificationGroup.suppressed) {
                handleSuppressedSummaryHeadsUpped(notificationGroup.summary);
            }
            this.mListener.onGroupsChanged();
        }
    }

    public void collapseAllGroups() {
        ArrayList arrayList = new ArrayList(this.mGroupMap.values());
        int size = arrayList.size();
        for (int i = 0; i < size; i++) {
            NotificationGroup notificationGroup = (NotificationGroup) arrayList.get(i);
            if (notificationGroup.expanded) {
                setGroupExpanded(notificationGroup, false);
            }
            updateSuppression(notificationGroup);
        }
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("GroupManager state:");
        printWriter.println("  number of groups: " + this.mGroupMap.size());
        Iterator<T> it = this.mGroupMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            printWriter.println("\n    key: " + ((String) entry.getKey()));
            printWriter.println(entry.getValue());
        }
        printWriter.println("\n    isolated entries: " + this.mIsolatedEntries.size());
        Iterator<T> it2 = this.mIsolatedEntries.entrySet().iterator();
        while (it2.hasNext()) {
            Map.Entry entry2 = (Map.Entry) it2.next();
            printWriter.print("      ");
            printWriter.print((String) entry2.getKey());
            printWriter.print(", ");
            printWriter.println(entry2.getValue());
        }
    }

    public ExpandableNotificationRow getGroupSummary(StatusBarNotification statusBarNotification) {
        return getGroupSummary(getGroupKey(statusBarNotification));
    }

    public ExpandableNotificationRow getLogicalGroupSummary(StatusBarNotification statusBarNotification) {
        return getGroupSummary(statusBarNotification.getGroupKey());
    }

    public boolean isChildInGroupWithSummary(StatusBarNotification statusBarNotification) {
        NotificationGroup notificationGroup;
        return (!isGroupChild(statusBarNotification) || (notificationGroup = this.mGroupMap.get(getGroupKey(statusBarNotification))) == null || notificationGroup.summary == null || notificationGroup.suppressed || notificationGroup.children.isEmpty()) ? false : true;
    }

    public boolean isGroupExpanded(StatusBarNotification statusBarNotification) {
        NotificationGroup notificationGroup = this.mGroupMap.get(getGroupKey(statusBarNotification));
        if (notificationGroup == null) {
            return false;
        }
        return notificationGroup.expanded;
    }

    public boolean isOnlyChildInGroup(StatusBarNotification statusBarNotification) {
        if (isOnlyChild(statusBarNotification)) {
            ExpandableNotificationRow logicalGroupSummary = getLogicalGroupSummary(statusBarNotification);
            boolean z = false;
            if (logicalGroupSummary != null) {
                z = !logicalGroupSummary.getStatusBarNotification().equals(statusBarNotification);
            }
            return z;
        }
        return false;
    }

    public boolean isSummaryOfGroup(StatusBarNotification statusBarNotification) {
        NotificationGroup notificationGroup;
        boolean z = false;
        if (isGroupSummary(statusBarNotification) && (notificationGroup = this.mGroupMap.get(getGroupKey(statusBarNotification))) != null) {
            if (!notificationGroup.children.isEmpty()) {
                z = true;
            }
            return z;
        }
        return false;
    }

    public boolean isSummaryOfSuppressedGroup(StatusBarNotification statusBarNotification) {
        return isGroupSuppressed(getGroupKey(statusBarNotification)) ? statusBarNotification.getNotification().isGroupSummary() : false;
    }

    public void onEntryAdded(NotificationData.Entry entry) {
        StatusBarNotification statusBarNotification = entry.notification;
        boolean isGroupChild = isGroupChild(statusBarNotification);
        String groupKey = getGroupKey(statusBarNotification);
        NotificationGroup notificationGroup = this.mGroupMap.get(groupKey);
        NotificationGroup notificationGroup2 = notificationGroup;
        if (notificationGroup == null) {
            notificationGroup2 = new NotificationGroup();
            this.mGroupMap.put(groupKey, notificationGroup2);
        }
        if (isGroupChild) {
            notificationGroup2.children.add(entry);
            updateSuppression(notificationGroup2);
            return;
        }
        notificationGroup2.summary = entry;
        notificationGroup2.expanded = entry.row.areChildrenExpanded();
        updateSuppression(notificationGroup2);
        if (notificationGroup2.children.isEmpty()) {
            return;
        }
        for (NotificationData.Entry entry2 : (HashSet) notificationGroup2.children.clone()) {
            onEntryBecomingChild(entry2);
        }
        this.mListener.onGroupCreatedFromChildren(notificationGroup2);
    }

    public void onEntryRemoved(NotificationData.Entry entry) {
        onEntryRemovedInternal(entry, entry.notification);
        this.mIsolatedEntries.remove(entry.key);
    }

    public void onEntryUpdated(NotificationData.Entry entry, StatusBarNotification statusBarNotification) {
        if (this.mGroupMap.get(getGroupKey(statusBarNotification)) != null) {
            onEntryRemovedInternal(entry, statusBarNotification);
        }
        onEntryAdded(entry);
        if (!isIsolated(entry.notification)) {
            if (isGroupChild(statusBarNotification) || !isGroupChild(entry.notification)) {
                return;
            }
            onEntryBecomingChild(entry);
            return;
        }
        this.mIsolatedEntries.put(entry.key, entry.notification);
        String groupKey = statusBarNotification.getGroupKey();
        String groupKey2 = entry.notification.getGroupKey();
        if (groupKey.equals(groupKey2)) {
            return;
        }
        updateSuppression(this.mGroupMap.get(groupKey));
        updateSuppression(this.mGroupMap.get(groupKey2));
    }

    @Override // com.android.systemui.statusbar.policy.HeadsUpManager.OnHeadsUpChangedListener
    public void onHeadsUpPinned(ExpandableNotificationRow expandableNotificationRow) {
    }

    @Override // com.android.systemui.statusbar.policy.HeadsUpManager.OnHeadsUpChangedListener
    public void onHeadsUpPinnedModeChanged(boolean z) {
    }

    @Override // com.android.systemui.statusbar.policy.HeadsUpManager.OnHeadsUpChangedListener
    public void onHeadsUpStateChanged(NotificationData.Entry entry, boolean z) {
        StatusBarNotification statusBarNotification = entry.notification;
        if (!entry.row.isHeadsUp()) {
            if (this.mIsolatedEntries.containsKey(statusBarNotification.getKey())) {
                onEntryRemovedInternal(entry, entry.notification);
                this.mIsolatedEntries.remove(statusBarNotification.getKey());
                onEntryAdded(entry);
                this.mListener.onGroupsChanged();
            }
        } else if (!shouldIsolate(statusBarNotification)) {
            handleSuppressedSummaryHeadsUpped(entry);
        } else {
            onEntryRemovedInternal(entry, entry.notification);
            this.mIsolatedEntries.put(statusBarNotification.getKey(), statusBarNotification);
            onEntryAdded(entry);
            updateSuppression(this.mGroupMap.get(entry.notification.getGroupKey()));
            this.mListener.onGroupsChanged();
        }
    }

    @Override // com.android.systemui.statusbar.policy.HeadsUpManager.OnHeadsUpChangedListener
    public void onHeadsUpUnPinned(ExpandableNotificationRow expandableNotificationRow) {
    }

    public void setGroupExpanded(StatusBarNotification statusBarNotification, boolean z) {
        NotificationGroup notificationGroup = this.mGroupMap.get(getGroupKey(statusBarNotification));
        if (notificationGroup == null) {
            return;
        }
        setGroupExpanded(notificationGroup, z);
    }

    public void setHeadsUpManager(HeadsUpManager headsUpManager) {
        this.mHeadsUpManager = headsUpManager;
    }

    public void setOnGroupChangeListener(OnGroupChangeListener onGroupChangeListener) {
        this.mListener = onGroupChangeListener;
    }

    public void setStatusBarState(int i) {
        if (this.mBarState == i) {
            return;
        }
        this.mBarState = i;
        if (this.mBarState == 1) {
            collapseAllGroups();
        }
    }

    public boolean toggleGroupExpansion(StatusBarNotification statusBarNotification) {
        boolean z = false;
        NotificationGroup notificationGroup = this.mGroupMap.get(getGroupKey(statusBarNotification));
        if (notificationGroup == null) {
            return false;
        }
        if (!notificationGroup.expanded) {
            z = true;
        }
        setGroupExpanded(notificationGroup, z);
        return notificationGroup.expanded;
    }
}
