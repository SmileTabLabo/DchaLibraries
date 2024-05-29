package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.ArrayMap;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.R;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.function.Function;
import java.util.stream.Stream;
/* loaded from: classes.dex */
public class HeadsUpManager {
    protected final Context mContext;
    protected boolean mHasPinnedNotification;
    protected int mHeadsUpNotificationDecay;
    protected int mMinimumDisplayTime;
    protected int mSnoozeLengthMs;
    private final ArrayMap<String, Long> mSnoozedPackages;
    protected int mTouchAcceptanceDelay;
    protected int mUser;
    protected final Clock mClock = new Clock();
    protected final HashSet<OnHeadsUpChangedListener> mListeners = new HashSet<>();
    protected final Handler mHandler = new Handler(Looper.getMainLooper());
    private final HashMap<String, HeadsUpEntry> mHeadsUpEntries = new HashMap<>();

    public HeadsUpManager(final Context context) {
        this.mContext = context;
        Resources resources = context.getResources();
        this.mMinimumDisplayTime = resources.getInteger(R.integer.heads_up_notification_minimum_time);
        this.mHeadsUpNotificationDecay = resources.getInteger(R.integer.heads_up_notification_decay);
        this.mTouchAcceptanceDelay = resources.getInteger(R.integer.touch_acceptance_delay);
        this.mSnoozedPackages = new ArrayMap<>();
        this.mSnoozeLengthMs = Settings.Global.getInt(context.getContentResolver(), "heads_up_snooze_length_ms", resources.getInteger(R.integer.heads_up_default_snooze_length_ms));
        context.getContentResolver().registerContentObserver(Settings.Global.getUriFor("heads_up_snooze_length_ms"), false, new ContentObserver(this.mHandler) { // from class: com.android.systemui.statusbar.policy.HeadsUpManager.1
            @Override // android.database.ContentObserver
            public void onChange(boolean z) {
                int i = Settings.Global.getInt(context.getContentResolver(), "heads_up_snooze_length_ms", -1);
                if (i > -1 && i != HeadsUpManager.this.mSnoozeLengthMs) {
                    HeadsUpManager.this.mSnoozeLengthMs = i;
                }
            }
        });
    }

    public void addListener(OnHeadsUpChangedListener onHeadsUpChangedListener) {
        this.mListeners.add(onHeadsUpChangedListener);
    }

    public void removeListener(OnHeadsUpChangedListener onHeadsUpChangedListener) {
        this.mListeners.remove(onHeadsUpChangedListener);
    }

    public void showNotification(NotificationData.Entry entry) {
        addHeadsUpEntry(entry);
        updateNotification(entry, true);
        entry.setInterruption();
    }

    public void updateNotification(NotificationData.Entry entry, boolean z) {
        HeadsUpEntry headsUpEntry;
        entry.row.sendAccessibilityEvent(2048);
        if (!z || (headsUpEntry = this.mHeadsUpEntries.get(entry.key)) == null) {
            return;
        }
        headsUpEntry.updateEntry(true);
        setEntryPinned(headsUpEntry, shouldHeadsUpBecomePinned(entry));
    }

    private void addHeadsUpEntry(NotificationData.Entry entry) {
        HeadsUpEntry createHeadsUpEntry = createHeadsUpEntry();
        createHeadsUpEntry.setEntry(entry);
        this.mHeadsUpEntries.put(entry.key, createHeadsUpEntry);
        entry.row.setHeadsUp(true);
        setEntryPinned(createHeadsUpEntry, shouldHeadsUpBecomePinned(entry));
        Iterator<OnHeadsUpChangedListener> it = this.mListeners.iterator();
        while (it.hasNext()) {
            it.next().onHeadsUpStateChanged(entry, true);
        }
        entry.row.sendAccessibilityEvent(2048);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean shouldHeadsUpBecomePinned(NotificationData.Entry entry) {
        return hasFullScreenIntent(entry);
    }

    protected boolean hasFullScreenIntent(NotificationData.Entry entry) {
        return entry.notification.getNotification().fullScreenIntent != null;
    }

    protected void setEntryPinned(HeadsUpEntry headsUpEntry, boolean z) {
        ExpandableNotificationRow expandableNotificationRow = headsUpEntry.entry.row;
        if (expandableNotificationRow.isPinned() != z) {
            expandableNotificationRow.setPinned(z);
            updatePinnedMode();
            Iterator<OnHeadsUpChangedListener> it = this.mListeners.iterator();
            while (it.hasNext()) {
                OnHeadsUpChangedListener next = it.next();
                if (z) {
                    next.onHeadsUpPinned(expandableNotificationRow);
                } else {
                    next.onHeadsUpUnPinned(expandableNotificationRow);
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void removeHeadsUpEntry(NotificationData.Entry entry) {
        onHeadsUpEntryRemoved(this.mHeadsUpEntries.remove(entry.key));
    }

    protected void onHeadsUpEntryRemoved(HeadsUpEntry headsUpEntry) {
        NotificationData.Entry entry = headsUpEntry.entry;
        entry.row.sendAccessibilityEvent(2048);
        entry.row.setHeadsUp(false);
        setEntryPinned(headsUpEntry, false);
        Iterator<OnHeadsUpChangedListener> it = this.mListeners.iterator();
        while (it.hasNext()) {
            it.next().onHeadsUpStateChanged(entry, false);
        }
        releaseHeadsUpEntry(headsUpEntry);
    }

    protected void updatePinnedMode() {
        boolean hasPinnedNotificationInternal = hasPinnedNotificationInternal();
        if (hasPinnedNotificationInternal == this.mHasPinnedNotification) {
            return;
        }
        this.mHasPinnedNotification = hasPinnedNotificationInternal;
        if (this.mHasPinnedNotification) {
            MetricsLogger.count(this.mContext, "note_peek", 1);
        }
        Iterator<OnHeadsUpChangedListener> it = this.mListeners.iterator();
        while (it.hasNext()) {
            it.next().onHeadsUpPinnedModeChanged(hasPinnedNotificationInternal);
        }
    }

    public boolean removeNotification(String str, boolean z) {
        releaseImmediately(str);
        return true;
    }

    public boolean isHeadsUp(String str) {
        return this.mHeadsUpEntries.containsKey(str);
    }

    public void releaseAllImmediately() {
        Iterator<HeadsUpEntry> it = this.mHeadsUpEntries.values().iterator();
        while (it.hasNext()) {
            it.remove();
            onHeadsUpEntryRemoved(it.next());
        }
    }

    public void releaseImmediately(String str) {
        HeadsUpEntry headsUpEntry = getHeadsUpEntry(str);
        if (headsUpEntry == null) {
            return;
        }
        removeHeadsUpEntry(headsUpEntry.entry);
    }

    public boolean isSnoozed(String str) {
        Long l = this.mSnoozedPackages.get(snoozeKey(str, this.mUser));
        if (l != null) {
            if (l.longValue() > this.mClock.currentTimeMillis()) {
                return true;
            }
            this.mSnoozedPackages.remove(str);
            return false;
        }
        return false;
    }

    public void snooze() {
        for (String str : this.mHeadsUpEntries.keySet()) {
            this.mSnoozedPackages.put(snoozeKey(this.mHeadsUpEntries.get(str).entry.notification.getPackageName(), this.mUser), Long.valueOf(this.mClock.currentTimeMillis() + this.mSnoozeLengthMs));
        }
    }

    private static String snoozeKey(String str, int i) {
        return i + "," + str;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public HeadsUpEntry getHeadsUpEntry(String str) {
        return this.mHeadsUpEntries.get(str);
    }

    public NotificationData.Entry getEntry(String str) {
        HeadsUpEntry headsUpEntry = this.mHeadsUpEntries.get(str);
        if (headsUpEntry != null) {
            return headsUpEntry.entry;
        }
        return null;
    }

    public Stream<NotificationData.Entry> getAllEntries() {
        return this.mHeadsUpEntries.values().stream().map(new Function() { // from class: com.android.systemui.statusbar.policy.-$$Lambda$HeadsUpManager$Q03ExeWuSMcpx0F3Hl5vkzUbslA
            @Override // java.util.function.Function
            public final Object apply(Object obj) {
                NotificationData.Entry entry;
                entry = ((HeadsUpManager.HeadsUpEntry) obj).entry;
                return entry;
            }
        });
    }

    public NotificationData.Entry getTopEntry() {
        HeadsUpEntry topHeadsUpEntry = getTopHeadsUpEntry();
        if (topHeadsUpEntry != null) {
            return topHeadsUpEntry.entry;
        }
        return null;
    }

    public boolean hasHeadsUpNotifications() {
        return !this.mHeadsUpEntries.isEmpty();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public HeadsUpEntry getTopHeadsUpEntry() {
        HeadsUpEntry headsUpEntry = null;
        if (this.mHeadsUpEntries.isEmpty()) {
            return null;
        }
        for (HeadsUpEntry headsUpEntry2 : this.mHeadsUpEntries.values()) {
            if (headsUpEntry == null || headsUpEntry2.compareTo(headsUpEntry) < 0) {
                headsUpEntry = headsUpEntry2;
            }
        }
        return headsUpEntry;
    }

    public void setUser(int i) {
        this.mUser = i;
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("HeadsUpManager state:");
        dumpInternal(fileDescriptor, printWriter, strArr);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void dumpInternal(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.print("  mTouchAcceptanceDelay=");
        printWriter.println(this.mTouchAcceptanceDelay);
        printWriter.print("  mSnoozeLengthMs=");
        printWriter.println(this.mSnoozeLengthMs);
        printWriter.print("  now=");
        printWriter.println(this.mClock.currentTimeMillis());
        printWriter.print("  mUser=");
        printWriter.println(this.mUser);
        for (HeadsUpEntry headsUpEntry : this.mHeadsUpEntries.values()) {
            printWriter.print("  HeadsUpEntry=");
            printWriter.println(headsUpEntry.entry);
        }
        int size = this.mSnoozedPackages.size();
        printWriter.println("  snoozed packages: " + size);
        for (int i = 0; i < size; i++) {
            printWriter.print("    ");
            printWriter.print(this.mSnoozedPackages.valueAt(i));
            printWriter.print(", ");
            printWriter.println(this.mSnoozedPackages.keyAt(i));
        }
    }

    public boolean hasPinnedHeadsUp() {
        return this.mHasPinnedNotification;
    }

    private boolean hasPinnedNotificationInternal() {
        for (String str : this.mHeadsUpEntries.keySet()) {
            if (this.mHeadsUpEntries.get(str).entry.row.isPinned()) {
                return true;
            }
        }
        return false;
    }

    public void unpinAll() {
        for (String str : this.mHeadsUpEntries.keySet()) {
            HeadsUpEntry headsUpEntry = this.mHeadsUpEntries.get(str);
            setEntryPinned(headsUpEntry, false);
            headsUpEntry.updateEntry(false);
        }
    }

    public boolean isTrackingHeadsUp() {
        return false;
    }

    public int compare(NotificationData.Entry entry, NotificationData.Entry entry2) {
        HeadsUpEntry headsUpEntry = getHeadsUpEntry(entry.key);
        HeadsUpEntry headsUpEntry2 = getHeadsUpEntry(entry2.key);
        if (headsUpEntry == null || headsUpEntry2 == null) {
            return headsUpEntry == null ? 1 : -1;
        }
        return headsUpEntry.compareTo(headsUpEntry2);
    }

    public void setExpanded(NotificationData.Entry entry, boolean z) {
        HeadsUpEntry headsUpEntry = this.mHeadsUpEntries.get(entry.key);
        if (headsUpEntry != null && entry.row.isPinned()) {
            headsUpEntry.expanded(z);
        }
    }

    protected HeadsUpEntry createHeadsUpEntry() {
        return new HeadsUpEntry();
    }

    protected void releaseHeadsUpEntry(HeadsUpEntry headsUpEntry) {
        headsUpEntry.reset();
    }

    public void onDensityOrFontScaleChanged() {
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* loaded from: classes.dex */
    public class HeadsUpEntry implements Comparable<HeadsUpEntry> {
        public long earliestRemovaltime;
        public NotificationData.Entry entry;
        public boolean expanded;
        private Runnable mRemoveHeadsUpRunnable;
        public long postTime;
        public boolean remoteInputActive;

        /* JADX INFO: Access modifiers changed from: protected */
        public HeadsUpEntry() {
        }

        public void setEntry(NotificationData.Entry entry) {
            setEntry(entry, null);
        }

        public void setEntry(NotificationData.Entry entry, Runnable runnable) {
            this.entry = entry;
            this.mRemoveHeadsUpRunnable = runnable;
            this.postTime = HeadsUpManager.this.mClock.currentTimeMillis() + HeadsUpManager.this.mTouchAcceptanceDelay;
            updateEntry(true);
        }

        public void updateEntry(boolean z) {
            long currentTimeMillis = HeadsUpManager.this.mClock.currentTimeMillis();
            this.earliestRemovaltime = HeadsUpManager.this.mMinimumDisplayTime + currentTimeMillis;
            if (z) {
                this.postTime = Math.max(this.postTime, currentTimeMillis);
            }
            removeAutoRemovalCallbacks();
            if (!isSticky()) {
                HeadsUpManager.this.mHandler.postDelayed(this.mRemoveHeadsUpRunnable, Math.max((this.postTime + HeadsUpManager.this.mHeadsUpNotificationDecay) - currentTimeMillis, HeadsUpManager.this.mMinimumDisplayTime));
            }
        }

        private boolean isSticky() {
            return (this.entry.row.isPinned() && this.expanded) || this.remoteInputActive || HeadsUpManager.this.hasFullScreenIntent(this.entry);
        }

        @Override // java.lang.Comparable
        public int compareTo(HeadsUpEntry headsUpEntry) {
            boolean isPinned = this.entry.row.isPinned();
            boolean isPinned2 = headsUpEntry.entry.row.isPinned();
            if (!isPinned || isPinned2) {
                if (!isPinned && isPinned2) {
                    return 1;
                }
                boolean hasFullScreenIntent = HeadsUpManager.this.hasFullScreenIntent(this.entry);
                boolean hasFullScreenIntent2 = HeadsUpManager.this.hasFullScreenIntent(headsUpEntry.entry);
                if (hasFullScreenIntent && !hasFullScreenIntent2) {
                    return -1;
                }
                if (!hasFullScreenIntent && hasFullScreenIntent2) {
                    return 1;
                }
                if (this.remoteInputActive && !headsUpEntry.remoteInputActive) {
                    return -1;
                }
                if ((this.remoteInputActive || !headsUpEntry.remoteInputActive) && this.postTime >= headsUpEntry.postTime) {
                    if (this.postTime == headsUpEntry.postTime) {
                        return this.entry.key.compareTo(headsUpEntry.entry.key);
                    }
                    return -1;
                }
                return 1;
            }
            return -1;
        }

        public void expanded(boolean z) {
            this.expanded = z;
        }

        public void reset() {
            this.entry = null;
            this.expanded = false;
            this.remoteInputActive = false;
            removeAutoRemovalCallbacks();
            this.mRemoveHeadsUpRunnable = null;
        }

        public void removeAutoRemovalCallbacks() {
            if (this.mRemoveHeadsUpRunnable != null) {
                HeadsUpManager.this.mHandler.removeCallbacks(this.mRemoveHeadsUpRunnable);
            }
        }

        public void removeAsSoonAsPossible() {
            if (this.mRemoveHeadsUpRunnable != null) {
                removeAutoRemovalCallbacks();
                HeadsUpManager.this.mHandler.postDelayed(this.mRemoveHeadsUpRunnable, this.earliestRemovaltime - HeadsUpManager.this.mClock.currentTimeMillis());
            }
        }
    }

    /* loaded from: classes.dex */
    public static class Clock {
        public long currentTimeMillis() {
            return SystemClock.elapsedRealtime();
        }
    }
}
