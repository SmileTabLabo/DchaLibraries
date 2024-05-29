package com.android.systemui.statusbar;

import android.app.Notification;
import android.content.Context;
import android.os.SystemClock;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.ArrayMap;
import android.view.View;
import android.widget.RemoteViews;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;
/* loaded from: a.zip:com/android/systemui/statusbar/NotificationData.class */
public class NotificationData {
    private final Environment mEnvironment;
    private NotificationGroupManager mGroupManager;
    private HeadsUpManager mHeadsUpManager;
    private NotificationListenerService.RankingMap mRankingMap;
    private final ArrayMap<String, Entry> mEntries = new ArrayMap<>();
    private final ArrayList<Entry> mSortedAndFiltered = new ArrayList<>();
    private final NotificationListenerService.Ranking mTmpRanking = new NotificationListenerService.Ranking();
    private final Comparator<Entry> mRankingComparator = new Comparator<Entry>(this) { // from class: com.android.systemui.statusbar.NotificationData.1
        private final NotificationListenerService.Ranking mRankingA = new NotificationListenerService.Ranking();
        private final NotificationListenerService.Ranking mRankingB = new NotificationListenerService.Ranking();
        final NotificationData this$0;

        {
            this.this$0 = this;
        }

        @Override // java.util.Comparator
        public int compare(Entry entry, Entry entry2) {
            StatusBarNotification statusBarNotification = entry.notification;
            StatusBarNotification statusBarNotification2 = entry2.notification;
            int i = 3;
            int i2 = 3;
            int i3 = 0;
            int i4 = 0;
            if (this.this$0.mRankingMap != null) {
                this.this$0.mRankingMap.getRanking(entry.key, this.mRankingA);
                this.this$0.mRankingMap.getRanking(entry2.key, this.mRankingB);
                i = this.mRankingA.getImportance();
                i2 = this.mRankingB.getImportance();
                i3 = this.mRankingA.getRank();
                i4 = this.mRankingB.getRank();
            }
            String currentMediaNotificationKey = this.this$0.mEnvironment.getCurrentMediaNotificationKey();
            boolean z = entry.key.equals(currentMediaNotificationKey) ? i > 1 : false;
            boolean z2 = entry2.key.equals(currentMediaNotificationKey) ? i2 > 1 : false;
            boolean isSystemNotification = i >= 5 ? NotificationData.isSystemNotification(statusBarNotification) : false;
            boolean isSystemNotification2 = i2 >= 5 ? NotificationData.isSystemNotification(statusBarNotification2) : false;
            boolean isHeadsUp = entry.row.isHeadsUp();
            if (isHeadsUp != entry2.row.isHeadsUp()) {
                return isHeadsUp ? -1 : 1;
            } else if (isHeadsUp) {
                return this.this$0.mHeadsUpManager.compare(entry, entry2);
            } else {
                if (z != z2) {
                    return z ? -1 : 1;
                } else if (isSystemNotification != isSystemNotification2) {
                    return isSystemNotification ? -1 : 1;
                } else {
                    return i3 != i4 ? i3 - i4 : (int) (statusBarNotification2.getNotification().when - statusBarNotification.getNotification().when);
                }
            }
        }
    };

    /* loaded from: a.zip:com/android/systemui/statusbar/NotificationData$Entry.class */
    public static final class Entry {
        public boolean autoRedacted;
        public RemoteViews cachedBigContentView;
        public RemoteViews cachedContentView;
        public RemoteViews cachedHeadsUpContentView;
        public RemoteViews cachedPublicContentView;
        public StatusBarIconView icon;
        private boolean interruption;
        public String key;
        private long lastFullScreenIntentLaunchTime = -2000;
        public boolean legacy;
        public StatusBarNotification notification;
        public CharSequence remoteInputText;
        public ExpandableNotificationRow row;
        public int targetSdk;

        public Entry(StatusBarNotification statusBarNotification, StatusBarIconView statusBarIconView) {
            this.key = statusBarNotification.getKey();
            this.notification = statusBarNotification;
            this.icon = statusBarIconView;
        }

        private boolean compareRemoteViews(RemoteViews remoteViews, RemoteViews remoteViews2) {
            boolean z = true;
            if (remoteViews != null || remoteViews2 != null) {
                if (remoteViews == null || remoteViews2 == null || remoteViews2.getPackage() == null || remoteViews.getPackage() == null || !remoteViews.getPackage().equals(remoteViews2.getPackage())) {
                    z = false;
                } else if (remoteViews.getLayoutId() != remoteViews2.getLayoutId()) {
                    z = false;
                }
            }
            return z;
        }

        public boolean cacheContentViews(Context context, Notification notification) {
            boolean z;
            if (notification != null) {
                Notification.Builder recoverBuilder = Notification.Builder.recoverBuilder(context, notification);
                RemoteViews createContentView = recoverBuilder.createContentView();
                RemoteViews createBigContentView = recoverBuilder.createBigContentView();
                RemoteViews createHeadsUpContentView = recoverBuilder.createHeadsUpContentView();
                RemoteViews makePublicContentView = recoverBuilder.makePublicContentView();
                z = Objects.equals(Boolean.valueOf(this.notification.getNotification().extras.getBoolean("android.contains.customView")), Boolean.valueOf(notification.extras.getBoolean("android.contains.customView")));
                if (!compareRemoteViews(this.cachedContentView, createContentView) || !compareRemoteViews(this.cachedBigContentView, createBigContentView) || !compareRemoteViews(this.cachedHeadsUpContentView, createHeadsUpContentView) || !compareRemoteViews(this.cachedPublicContentView, makePublicContentView)) {
                    z = false;
                }
                this.cachedPublicContentView = makePublicContentView;
                this.cachedHeadsUpContentView = createHeadsUpContentView;
                this.cachedBigContentView = createBigContentView;
                this.cachedContentView = createContentView;
            } else {
                Notification.Builder recoverBuilder2 = Notification.Builder.recoverBuilder(context, this.notification.getNotification());
                this.cachedContentView = recoverBuilder2.createContentView();
                this.cachedBigContentView = recoverBuilder2.createBigContentView();
                this.cachedHeadsUpContentView = recoverBuilder2.createHeadsUpContentView();
                this.cachedPublicContentView = recoverBuilder2.makePublicContentView();
                z = false;
            }
            return z;
        }

        public View getContentView() {
            return this.row.getPrivateLayout().getContractedChild();
        }

        public View getExpandedContentView() {
            return this.row.getPrivateLayout().getExpandedChild();
        }

        public View getHeadsUpContentView() {
            return this.row.getPrivateLayout().getHeadsUpChild();
        }

        public View getPublicContentView() {
            return this.row.getPublicLayout().getContractedChild();
        }

        public boolean hasInterrupted() {
            return this.interruption;
        }

        public boolean hasJustLaunchedFullScreenIntent() {
            return SystemClock.elapsedRealtime() < this.lastFullScreenIntentLaunchTime + 2000;
        }

        public void notifyFullScreenIntentLaunched() {
            this.lastFullScreenIntentLaunchTime = SystemClock.elapsedRealtime();
        }

        public void reset() {
            this.autoRedacted = false;
            this.legacy = false;
            this.lastFullScreenIntentLaunchTime = -2000L;
            if (this.row != null) {
                this.row.reset();
            }
        }

        public void setInterruption() {
            this.interruption = true;
        }
    }

    /* loaded from: a.zip:com/android/systemui/statusbar/NotificationData$Environment.class */
    public interface Environment {
        String getCurrentMediaNotificationKey();

        NotificationGroupManager getGroupManager();

        boolean isDeviceProvisioned();

        boolean isNotificationForCurrentProfiles(StatusBarNotification statusBarNotification);

        boolean onSecureLockScreen();

        boolean shouldHideNotifications(int i);

        boolean shouldHideNotifications(String str);
    }

    public NotificationData(Environment environment) {
        this.mEnvironment = environment;
        this.mGroupManager = environment.getGroupManager();
    }

    private void dumpEntry(PrintWriter printWriter, String str, int i, Entry entry) {
        this.mRankingMap.getRanking(entry.key, this.mTmpRanking);
        printWriter.print(str);
        printWriter.println("  [" + i + "] key=" + entry.key + " icon=" + entry.icon);
        StatusBarNotification statusBarNotification = entry.notification;
        printWriter.print(str);
        printWriter.println("      pkg=" + statusBarNotification.getPackageName() + " id=" + statusBarNotification.getId() + " importance=" + this.mTmpRanking.getImportance());
        printWriter.print(str);
        printWriter.println("      notification=" + statusBarNotification.getNotification());
        printWriter.print(str);
        printWriter.println("      tickerText=\"" + statusBarNotification.getNotification().tickerText + "\"");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static boolean isSystemNotification(StatusBarNotification statusBarNotification) {
        String packageName = statusBarNotification.getPackageName();
        return !"android".equals(packageName) ? "com.android.systemui".equals(packageName) : true;
    }

    public static boolean showNotificationEvenIfUnprovisioned(StatusBarNotification statusBarNotification) {
        return "android".equals(statusBarNotification.getPackageName()) ? statusBarNotification.getNotification().extras.getBoolean("android.allowDuringSetup") : false;
    }

    private void updateRankingAndSort(NotificationListenerService.RankingMap rankingMap) {
        if (rankingMap != null) {
            this.mRankingMap = rankingMap;
            synchronized (this.mEntries) {
                int size = this.mEntries.size();
                for (int i = 0; i < size; i++) {
                    Entry valueAt = this.mEntries.valueAt(i);
                    StatusBarNotification clone = valueAt.notification.clone();
                    String overrideGroupKey = getOverrideGroupKey(valueAt.key);
                    if (!Objects.equals(clone.getOverrideGroupKey(), overrideGroupKey)) {
                        valueAt.notification.setOverrideGroupKey(overrideGroupKey);
                        this.mGroupManager.onEntryUpdated(valueAt, clone);
                    }
                }
            }
        }
        filterAndSort();
    }

    public void add(Entry entry, NotificationListenerService.RankingMap rankingMap) {
        synchronized (this.mEntries) {
            this.mEntries.put(entry.notification.getKey(), entry);
        }
        this.mGroupManager.onEntryAdded(entry);
        updateRankingAndSort(rankingMap);
    }

    public void dump(PrintWriter printWriter, String str) {
        int size = this.mSortedAndFiltered.size();
        printWriter.print(str);
        printWriter.println("active notifications: " + size);
        int i = 0;
        while (i < size) {
            dumpEntry(printWriter, str, i, this.mSortedAndFiltered.get(i));
            i++;
        }
        synchronized (this.mEntries) {
            int size2 = this.mEntries.size();
            printWriter.print(str);
            printWriter.println("inactive notifications: " + (size2 - i));
            int i2 = 0;
            int i3 = 0;
            while (i3 < size2) {
                Entry valueAt = this.mEntries.valueAt(i3);
                int i4 = i2;
                if (!this.mSortedAndFiltered.contains(valueAt)) {
                    dumpEntry(printWriter, str, i2, valueAt);
                    i4 = i2 + 1;
                }
                i3++;
                i2 = i4;
            }
        }
    }

    public void filterAndSort() {
        this.mSortedAndFiltered.clear();
        synchronized (this.mEntries) {
            int size = this.mEntries.size();
            for (int i = 0; i < size; i++) {
                Entry valueAt = this.mEntries.valueAt(i);
                if (!shouldFilterOut(valueAt.notification)) {
                    this.mSortedAndFiltered.add(valueAt);
                }
            }
        }
        Collections.sort(this.mSortedAndFiltered, this.mRankingComparator);
    }

    public Entry get(String str) {
        return this.mEntries.get(str);
    }

    public ArrayList<Entry> getActiveNotifications() {
        return this.mSortedAndFiltered;
    }

    public int getImportance(String str) {
        if (this.mRankingMap != null) {
            this.mRankingMap.getRanking(str, this.mTmpRanking);
            return this.mTmpRanking.getImportance();
        }
        return -1000;
    }

    public String getOverrideGroupKey(String str) {
        if (this.mRankingMap != null) {
            this.mRankingMap.getRanking(str, this.mTmpRanking);
            return this.mTmpRanking.getOverrideGroupKey();
        }
        return null;
    }

    public int getVisibilityOverride(String str) {
        if (this.mRankingMap != null) {
            this.mRankingMap.getRanking(str, this.mTmpRanking);
            return this.mTmpRanking.getVisibilityOverride();
        }
        return -1000;
    }

    public boolean hasActiveClearableNotifications() {
        for (Entry entry : this.mSortedAndFiltered) {
            if (entry.getContentView() != null && entry.notification.isClearable()) {
                return true;
            }
        }
        return false;
    }

    public boolean isAmbient(String str) {
        if (this.mRankingMap != null) {
            this.mRankingMap.getRanking(str, this.mTmpRanking);
            return this.mTmpRanking.isAmbient();
        }
        return false;
    }

    public Entry remove(String str, NotificationListenerService.RankingMap rankingMap) {
        Entry remove;
        synchronized (this.mEntries) {
            remove = this.mEntries.remove(str);
        }
        if (remove == null) {
            return null;
        }
        this.mGroupManager.onEntryRemoved(remove);
        updateRankingAndSort(rankingMap);
        return remove;
    }

    public void setHeadsUpManager(HeadsUpManager headsUpManager) {
        this.mHeadsUpManager = headsUpManager;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean shouldFilterOut(StatusBarNotification statusBarNotification) {
        if ((!this.mEnvironment.isDeviceProvisioned() ? showNotificationEvenIfUnprovisioned(statusBarNotification) : true) && this.mEnvironment.isNotificationForCurrentProfiles(statusBarNotification)) {
            if (this.mEnvironment.onSecureLockScreen() && (statusBarNotification.getNotification().visibility == -1 || this.mEnvironment.shouldHideNotifications(statusBarNotification.getUserId()) || this.mEnvironment.shouldHideNotifications(statusBarNotification.getKey()))) {
                return true;
            }
            return !BaseStatusBar.ENABLE_CHILD_NOTIFICATIONS && this.mGroupManager.isChildInGroupWithSummary(statusBarNotification);
        }
        return true;
    }

    public boolean shouldSuppressScreenOff(String str) {
        boolean z = false;
        if (this.mRankingMap != null) {
            this.mRankingMap.getRanking(str, this.mTmpRanking);
            if ((this.mTmpRanking.getSuppressedVisualEffects() & 1) != 0) {
                z = true;
            }
            return z;
        }
        return false;
    }

    public boolean shouldSuppressScreenOn(String str) {
        boolean z = false;
        if (this.mRankingMap != null) {
            this.mRankingMap.getRanking(str, this.mTmpRanking);
            if ((this.mTmpRanking.getSuppressedVisualEffects() & 2) != 0) {
                z = true;
            }
            return z;
        }
        return false;
    }

    public void updateRanking(NotificationListenerService.RankingMap rankingMap) {
        updateRankingAndSort(rankingMap);
    }
}
