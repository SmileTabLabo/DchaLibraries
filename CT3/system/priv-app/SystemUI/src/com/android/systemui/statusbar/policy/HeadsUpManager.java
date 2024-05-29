package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Pools;
import android.view.View;
import android.view.ViewTreeObserver;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.statusbar.phone.PhoneStatusBar;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;
/* loaded from: a.zip:com/android/systemui/statusbar/policy/HeadsUpManager.class */
public class HeadsUpManager implements ViewTreeObserver.OnComputeInternalInsetsListener {
    private PhoneStatusBar mBar;
    private Clock mClock;
    private final Context mContext;
    private final int mDefaultSnoozeLengthMs;
    private final NotificationGroupManager mGroupManager;
    private boolean mHasPinnedNotification;
    private boolean mHeadsUpGoingAway;
    private final int mHeadsUpNotificationDecay;
    private boolean mIsExpanded;
    private boolean mIsObserving;
    private final int mMinimumDisplayTime;
    private boolean mReleaseOnExpandFinish;
    private ContentObserver mSettingsObserver;
    private int mSnoozeLengthMs;
    private final ArrayMap<String, Long> mSnoozedPackages;
    private final int mStatusBarHeight;
    private final View mStatusBarWindowView;
    private final int mTouchAcceptanceDelay;
    private boolean mTrackingHeadsUp;
    private int mUser;
    private boolean mWaitingOnCollapseWhenGoingAway;
    private final HashSet<OnHeadsUpChangedListener> mListeners = new HashSet<>();
    private final Handler mHandler = new Handler();
    private final Pools.Pool<HeadsUpEntry> mEntryPool = new Pools.Pool<HeadsUpEntry>(this) { // from class: com.android.systemui.statusbar.policy.HeadsUpManager.1
        private Stack<HeadsUpEntry> mPoolObjects = new Stack<>();
        final HeadsUpManager this$0;

        {
            this.this$0 = this;
        }

        /* renamed from: acquire */
        public HeadsUpEntry m1863acquire() {
            return !this.mPoolObjects.isEmpty() ? this.mPoolObjects.pop() : new HeadsUpEntry(this.this$0);
        }

        public boolean release(HeadsUpEntry headsUpEntry) {
            headsUpEntry.reset();
            this.mPoolObjects.push(headsUpEntry);
            return true;
        }
    };
    private HashMap<String, HeadsUpEntry> mHeadsUpEntries = new HashMap<>();
    private HashSet<String> mSwipedOutKeys = new HashSet<>();
    private HashSet<NotificationData.Entry> mEntriesToRemoveAfterExpand = new HashSet<>();
    private int[] mTmpTwoArray = new int[2];

    /* loaded from: a.zip:com/android/systemui/statusbar/policy/HeadsUpManager$Clock.class */
    public static class Clock {
        public long currentTimeMillis() {
            return SystemClock.elapsedRealtime();
        }
    }

    /* loaded from: a.zip:com/android/systemui/statusbar/policy/HeadsUpManager$HeadsUpEntry.class */
    public class HeadsUpEntry implements Comparable<HeadsUpEntry> {
        public long earliestRemovaltime;
        public NotificationData.Entry entry;
        public boolean expanded;
        private Runnable mRemoveHeadsUpRunnable;
        public long postTime;
        public boolean remoteInputActive;
        final HeadsUpManager this$0;

        public HeadsUpEntry(HeadsUpManager headsUpManager) {
            this.this$0 = headsUpManager;
        }

        private boolean isSticky() {
            return ((this.entry.row.isPinned() && this.expanded) || this.remoteInputActive) ? true : this.this$0.hasFullScreenIntent(this.entry);
        }

        @Override // java.lang.Comparable
        public int compareTo(HeadsUpEntry headsUpEntry) {
            int i = 1;
            boolean isPinned = this.entry.row.isPinned();
            boolean isPinned2 = headsUpEntry.entry.row.isPinned();
            if (!isPinned || isPinned2) {
                if (isPinned || !isPinned2) {
                    boolean hasFullScreenIntent = this.this$0.hasFullScreenIntent(this.entry);
                    boolean hasFullScreenIntent2 = this.this$0.hasFullScreenIntent(headsUpEntry.entry);
                    if (!hasFullScreenIntent || hasFullScreenIntent2) {
                        if (hasFullScreenIntent || !hasFullScreenIntent2) {
                            if (!this.remoteInputActive || headsUpEntry.remoteInputActive) {
                                if (this.remoteInputActive || !headsUpEntry.remoteInputActive) {
                                    if (this.postTime >= headsUpEntry.postTime) {
                                        i = this.postTime == headsUpEntry.postTime ? this.entry.key.compareTo(headsUpEntry.entry.key) : -1;
                                    }
                                    return i;
                                }
                                return 1;
                            }
                            return -1;
                        }
                        return 1;
                    }
                    return -1;
                }
                return 1;
            }
            return -1;
        }

        public void removeAsSoonAsPossible() {
            removeAutoRemovalCallbacks();
            this.this$0.mHandler.postDelayed(this.mRemoveHeadsUpRunnable, this.earliestRemovaltime - this.this$0.mClock.currentTimeMillis());
        }

        public void removeAutoRemovalCallbacks() {
            this.this$0.mHandler.removeCallbacks(this.mRemoveHeadsUpRunnable);
        }

        public void reset() {
            removeAutoRemovalCallbacks();
            this.entry = null;
            this.mRemoveHeadsUpRunnable = null;
            this.expanded = false;
            this.remoteInputActive = false;
        }

        public void setEntry(NotificationData.Entry entry) {
            this.entry = entry;
            this.postTime = this.this$0.mClock.currentTimeMillis() + this.this$0.mTouchAcceptanceDelay;
            this.mRemoveHeadsUpRunnable = new Runnable(this, entry) { // from class: com.android.systemui.statusbar.policy.HeadsUpManager.HeadsUpEntry.1
                final HeadsUpEntry this$1;
                final NotificationData.Entry val$entry;

                {
                    this.this$1 = this;
                    this.val$entry = entry;
                }

                @Override // java.lang.Runnable
                public void run() {
                    if (this.this$1.this$0.mTrackingHeadsUp) {
                        this.this$1.this$0.mEntriesToRemoveAfterExpand.add(this.val$entry);
                    } else {
                        this.this$1.this$0.removeHeadsUpEntry(this.val$entry);
                    }
                }
            };
            updateEntry();
        }

        public void updateEntry() {
            updateEntry(true);
        }

        public void updateEntry(boolean z) {
            long currentTimeMillis = this.this$0.mClock.currentTimeMillis();
            this.earliestRemovaltime = this.this$0.mMinimumDisplayTime + currentTimeMillis;
            if (z) {
                this.postTime = Math.max(this.postTime, currentTimeMillis);
            }
            removeAutoRemovalCallbacks();
            if (this.this$0.mEntriesToRemoveAfterExpand.contains(this.entry)) {
                this.this$0.mEntriesToRemoveAfterExpand.remove(this.entry);
            }
            if (isSticky()) {
                return;
            }
            this.this$0.mHandler.postDelayed(this.mRemoveHeadsUpRunnable, Math.max((this.postTime + this.this$0.mHeadsUpNotificationDecay) - currentTimeMillis, this.this$0.mMinimumDisplayTime));
        }

        public boolean wasShownLongEnough() {
            return this.earliestRemovaltime < this.this$0.mClock.currentTimeMillis();
        }
    }

    /* loaded from: a.zip:com/android/systemui/statusbar/policy/HeadsUpManager$OnHeadsUpChangedListener.class */
    public interface OnHeadsUpChangedListener {
        void onHeadsUpPinned(ExpandableNotificationRow expandableNotificationRow);

        void onHeadsUpPinnedModeChanged(boolean z);

        void onHeadsUpStateChanged(NotificationData.Entry entry, boolean z);

        void onHeadsUpUnPinned(ExpandableNotificationRow expandableNotificationRow);
    }

    public HeadsUpManager(Context context, View view, NotificationGroupManager notificationGroupManager) {
        this.mContext = context;
        Resources resources = this.mContext.getResources();
        this.mTouchAcceptanceDelay = resources.getInteger(2131755060);
        this.mSnoozedPackages = new ArrayMap<>();
        this.mDefaultSnoozeLengthMs = resources.getInteger(2131755058);
        this.mSnoozeLengthMs = this.mDefaultSnoozeLengthMs;
        this.mMinimumDisplayTime = resources.getInteger(2131755059);
        this.mHeadsUpNotificationDecay = resources.getInteger(2131755057);
        this.mClock = new Clock();
        this.mSnoozeLengthMs = Settings.Global.getInt(context.getContentResolver(), "heads_up_snooze_length_ms", this.mDefaultSnoozeLengthMs);
        this.mSettingsObserver = new ContentObserver(this, this.mHandler, context) { // from class: com.android.systemui.statusbar.policy.HeadsUpManager.2
            final HeadsUpManager this$0;
            final Context val$context;

            {
                this.this$0 = this;
                this.val$context = context;
            }

            @Override // android.database.ContentObserver
            public void onChange(boolean z) {
                int i = Settings.Global.getInt(this.val$context.getContentResolver(), "heads_up_snooze_length_ms", -1);
                if (i <= -1 || i == this.this$0.mSnoozeLengthMs) {
                    return;
                }
                this.this$0.mSnoozeLengthMs = i;
                Log.v("HeadsUpManager", "mSnoozeLengthMs = " + this.this$0.mSnoozeLengthMs);
            }
        };
        context.getContentResolver().registerContentObserver(Settings.Global.getUriFor("heads_up_snooze_length_ms"), false, this.mSettingsObserver);
        this.mStatusBarWindowView = view;
        this.mGroupManager = notificationGroupManager;
        this.mStatusBarHeight = resources.getDimensionPixelSize(17104919);
    }

    private void addHeadsUpEntry(NotificationData.Entry entry) {
        HeadsUpEntry headsUpEntry = (HeadsUpEntry) this.mEntryPool.acquire();
        headsUpEntry.setEntry(entry);
        this.mHeadsUpEntries.put(entry.key, headsUpEntry);
        entry.row.setHeadsUp(true);
        setEntryPinned(headsUpEntry, shouldHeadsUpBecomePinned(entry));
        for (OnHeadsUpChangedListener onHeadsUpChangedListener : this.mListeners) {
            onHeadsUpChangedListener.onHeadsUpStateChanged(entry, true);
        }
        entry.row.sendAccessibilityEvent(2048);
    }

    private HeadsUpEntry getHeadsUpEntry(String str) {
        return this.mHeadsUpEntries.get(str);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean hasFullScreenIntent(NotificationData.Entry entry) {
        return entry.notification.getNotification().fullScreenIntent != null;
    }

    private boolean hasPinnedNotificationInternal() {
        for (String str : this.mHeadsUpEntries.keySet()) {
            if (this.mHeadsUpEntries.get(str).entry.row.isPinned()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isClickedHeadsUpNotification(View view) {
        Boolean bool = (Boolean) view.getTag(2131886148);
        return bool != null ? bool.booleanValue() : false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void removeHeadsUpEntry(NotificationData.Entry entry) {
        HeadsUpEntry remove = this.mHeadsUpEntries.remove(entry.key);
        entry.row.sendAccessibilityEvent(2048);
        entry.row.setHeadsUp(false);
        setEntryPinned(remove, false);
        for (OnHeadsUpChangedListener onHeadsUpChangedListener : this.mListeners) {
            onHeadsUpChangedListener.onHeadsUpStateChanged(entry, false);
        }
        this.mEntryPool.release(remove);
    }

    private void setEntryPinned(HeadsUpEntry headsUpEntry, boolean z) {
        ExpandableNotificationRow expandableNotificationRow = headsUpEntry.entry.row;
        if (expandableNotificationRow.isPinned() != z) {
            expandableNotificationRow.setPinned(z);
            updatePinnedMode();
            for (OnHeadsUpChangedListener onHeadsUpChangedListener : this.mListeners) {
                if (z) {
                    onHeadsUpChangedListener.onHeadsUpPinned(expandableNotificationRow);
                } else {
                    onHeadsUpChangedListener.onHeadsUpUnPinned(expandableNotificationRow);
                }
            }
        }
    }

    public static void setIsClickedNotification(View view, boolean z) {
        view.setTag(2131886148, z ? true : null);
    }

    private boolean shouldHeadsUpBecomePinned(NotificationData.Entry entry) {
        return this.mIsExpanded ? hasFullScreenIntent(entry) : true;
    }

    private static String snoozeKey(String str, int i) {
        return i + "," + str;
    }

    private void updatePinnedMode() {
        boolean hasPinnedNotificationInternal = hasPinnedNotificationInternal();
        if (hasPinnedNotificationInternal == this.mHasPinnedNotification) {
            return;
        }
        this.mHasPinnedNotification = hasPinnedNotificationInternal;
        if (this.mHasPinnedNotification) {
            MetricsLogger.count(this.mContext, "note_peek", 1);
        }
        updateTouchableRegionListener();
        for (OnHeadsUpChangedListener onHeadsUpChangedListener : this.mListeners) {
            onHeadsUpChangedListener.onHeadsUpPinnedModeChanged(hasPinnedNotificationInternal);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateTouchableRegionListener() {
        boolean z = (this.mHasPinnedNotification || this.mHeadsUpGoingAway) ? true : this.mWaitingOnCollapseWhenGoingAway;
        if (z == this.mIsObserving) {
            return;
        }
        if (z) {
            this.mStatusBarWindowView.getViewTreeObserver().addOnComputeInternalInsetsListener(this);
            this.mStatusBarWindowView.requestLayout();
        } else {
            this.mStatusBarWindowView.getViewTreeObserver().removeOnComputeInternalInsetsListener(this);
        }
        this.mIsObserving = z;
    }

    private void waitForStatusBarLayout() {
        this.mWaitingOnCollapseWhenGoingAway = true;
        this.mStatusBarWindowView.addOnLayoutChangeListener(new View.OnLayoutChangeListener(this) { // from class: com.android.systemui.statusbar.policy.HeadsUpManager.3
            final HeadsUpManager this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.OnLayoutChangeListener
            public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                if (this.this$0.mStatusBarWindowView.getHeight() <= this.this$0.mStatusBarHeight) {
                    this.this$0.mStatusBarWindowView.removeOnLayoutChangeListener(this);
                    this.this$0.mWaitingOnCollapseWhenGoingAway = false;
                    this.this$0.updateTouchableRegionListener();
                }
            }
        });
    }

    private boolean wasShownLongEnough(String str) {
        HeadsUpEntry headsUpEntry = getHeadsUpEntry(str);
        HeadsUpEntry topEntry = getTopEntry();
        if (this.mSwipedOutKeys.contains(str)) {
            this.mSwipedOutKeys.remove(str);
            return true;
        } else if (headsUpEntry != topEntry) {
            return true;
        } else {
            return headsUpEntry.wasShownLongEnough();
        }
    }

    public void addListener(OnHeadsUpChangedListener onHeadsUpChangedListener) {
        this.mListeners.add(onHeadsUpChangedListener);
    }

    public void addSwipedOutNotification(String str) {
        this.mSwipedOutKeys.add(str);
    }

    public int compare(NotificationData.Entry entry, NotificationData.Entry entry2) {
        HeadsUpEntry headsUpEntry = getHeadsUpEntry(entry.key);
        HeadsUpEntry headsUpEntry2 = getHeadsUpEntry(entry2.key);
        if (headsUpEntry == null || headsUpEntry2 == null) {
            return headsUpEntry == null ? 1 : -1;
        }
        return headsUpEntry.compareTo(headsUpEntry2);
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("HeadsUpManager state:");
        printWriter.print("  mTouchAcceptanceDelay=");
        printWriter.println(this.mTouchAcceptanceDelay);
        printWriter.print("  mSnoozeLengthMs=");
        printWriter.println(this.mSnoozeLengthMs);
        printWriter.print("  now=");
        printWriter.println(SystemClock.elapsedRealtime());
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

    public Collection<HeadsUpEntry> getAllEntries() {
        return this.mHeadsUpEntries.values();
    }

    public NotificationData.Entry getEntry(String str) {
        return this.mHeadsUpEntries.get(str).entry;
    }

    public HeadsUpEntry getTopEntry() {
        if (this.mHeadsUpEntries.isEmpty()) {
            return null;
        }
        HeadsUpEntry headsUpEntry = null;
        for (HeadsUpEntry headsUpEntry2 : this.mHeadsUpEntries.values()) {
            if (headsUpEntry == null || headsUpEntry2.compareTo(headsUpEntry) == -1) {
                headsUpEntry = headsUpEntry2;
            }
        }
        return headsUpEntry;
    }

    public int getTopHeadsUpPinnedHeight() {
        HeadsUpEntry topEntry = getTopEntry();
        if (topEntry == null || topEntry.entry == null) {
            return 0;
        }
        ExpandableNotificationRow expandableNotificationRow = topEntry.entry.row;
        ExpandableNotificationRow expandableNotificationRow2 = expandableNotificationRow;
        if (expandableNotificationRow.isChildInGroup()) {
            ExpandableNotificationRow groupSummary = this.mGroupManager.getGroupSummary(expandableNotificationRow.getStatusBarNotification());
            expandableNotificationRow2 = expandableNotificationRow;
            if (groupSummary != null) {
                expandableNotificationRow2 = groupSummary;
            }
        }
        return expandableNotificationRow2.getPinnedHeadsUpHeight(true);
    }

    public boolean hasPinnedHeadsUp() {
        return this.mHasPinnedNotification;
    }

    public boolean isHeadsUp(String str) {
        return this.mHeadsUpEntries.containsKey(str);
    }

    public boolean isSnoozed(String str) {
        String snoozeKey = snoozeKey(str, this.mUser);
        Long l = this.mSnoozedPackages.get(snoozeKey);
        if (l != null) {
            if (l.longValue() > SystemClock.elapsedRealtime()) {
                Log.v("HeadsUpManager", snoozeKey + " snoozed");
                return true;
            }
            this.mSnoozedPackages.remove(str);
            return false;
        }
        return false;
    }

    public boolean isTrackingHeadsUp() {
        return this.mTrackingHeadsUp;
    }

    public void onComputeInternalInsets(ViewTreeObserver.InternalInsetsInfo internalInsetsInfo) {
        if (this.mIsExpanded) {
            return;
        }
        if (!this.mHasPinnedNotification) {
            if (this.mHeadsUpGoingAway || this.mWaitingOnCollapseWhenGoingAway) {
                internalInsetsInfo.setTouchableInsets(3);
                internalInsetsInfo.touchableRegion.set(0, 0, this.mStatusBarWindowView.getWidth(), this.mStatusBarHeight);
                return;
            }
            return;
        }
        ExpandableNotificationRow expandableNotificationRow = getTopEntry().entry.row;
        ExpandableNotificationRow expandableNotificationRow2 = expandableNotificationRow;
        if (expandableNotificationRow.isChildInGroup()) {
            ExpandableNotificationRow groupSummary = this.mGroupManager.getGroupSummary(expandableNotificationRow.getStatusBarNotification());
            expandableNotificationRow2 = expandableNotificationRow;
            if (groupSummary != null) {
                expandableNotificationRow2 = groupSummary;
            }
        }
        expandableNotificationRow2.getLocationOnScreen(this.mTmpTwoArray);
        int i = this.mTmpTwoArray[0];
        int i2 = this.mTmpTwoArray[0];
        int width = expandableNotificationRow2.getWidth();
        int intrinsicHeight = expandableNotificationRow2.getIntrinsicHeight();
        internalInsetsInfo.setTouchableInsets(3);
        internalInsetsInfo.touchableRegion.set(i, 0, i2 + width, intrinsicHeight);
    }

    public void onExpandingFinished() {
        if (this.mReleaseOnExpandFinish) {
            releaseAllImmediately();
            this.mReleaseOnExpandFinish = false;
        } else {
            for (NotificationData.Entry entry : this.mEntriesToRemoveAfterExpand) {
                if (isHeadsUp(entry.key)) {
                    removeHeadsUpEntry(entry);
                }
            }
        }
        this.mEntriesToRemoveAfterExpand.clear();
    }

    public void releaseAllImmediately() {
        Log.v("HeadsUpManager", "releaseAllImmediately");
        for (String str : new ArrayList(this.mHeadsUpEntries.keySet())) {
            releaseImmediately(str);
        }
    }

    public void releaseImmediately(String str) {
        HeadsUpEntry headsUpEntry = getHeadsUpEntry(str);
        if (headsUpEntry == null) {
            return;
        }
        removeHeadsUpEntry(headsUpEntry.entry);
    }

    public boolean removeNotification(String str, boolean z) {
        Log.v("HeadsUpManager", "remove");
        if (wasShownLongEnough(str) || z) {
            releaseImmediately(str);
            return true;
        }
        getHeadsUpEntry(str).removeAsSoonAsPossible();
        return false;
    }

    public void setBar(PhoneStatusBar phoneStatusBar) {
        this.mBar = phoneStatusBar;
    }

    public void setExpanded(NotificationData.Entry entry, boolean z) {
        HeadsUpEntry headsUpEntry = this.mHeadsUpEntries.get(entry.key);
        if (headsUpEntry == null || headsUpEntry.expanded == z) {
            return;
        }
        headsUpEntry.expanded = z;
        if (z) {
            headsUpEntry.removeAutoRemovalCallbacks();
        } else {
            headsUpEntry.updateEntry(false);
        }
    }

    public void setHeadsUpGoingAway(boolean z) {
        if (z != this.mHeadsUpGoingAway) {
            this.mHeadsUpGoingAway = z;
            if (!z) {
                waitForStatusBarLayout();
            }
            updateTouchableRegionListener();
        }
    }

    public void setIsExpanded(boolean z) {
        if (z != this.mIsExpanded) {
            this.mIsExpanded = z;
            if (z) {
                this.mWaitingOnCollapseWhenGoingAway = false;
                this.mHeadsUpGoingAway = false;
                updateTouchableRegionListener();
            }
        }
    }

    public void setRemoteInputActive(NotificationData.Entry entry, boolean z) {
        HeadsUpEntry headsUpEntry = this.mHeadsUpEntries.get(entry.key);
        if (headsUpEntry == null || headsUpEntry.remoteInputActive == z) {
            return;
        }
        headsUpEntry.remoteInputActive = z;
        if (z) {
            headsUpEntry.removeAutoRemovalCallbacks();
        } else {
            headsUpEntry.updateEntry(false);
        }
    }

    public void setTrackingHeadsUp(boolean z) {
        this.mTrackingHeadsUp = z;
    }

    public void setUser(int i) {
        this.mUser = i;
    }

    public boolean shouldSwallowClick(String str) {
        HeadsUpEntry headsUpEntry = this.mHeadsUpEntries.get(str);
        return headsUpEntry != null && this.mClock.currentTimeMillis() < headsUpEntry.postTime;
    }

    public void showNotification(NotificationData.Entry entry) {
        Log.v("HeadsUpManager", "showNotification");
        addHeadsUpEntry(entry);
        updateNotification(entry, true);
        entry.setInterruption();
    }

    public void snooze() {
        for (String str : this.mHeadsUpEntries.keySet()) {
            this.mSnoozedPackages.put(snoozeKey(this.mHeadsUpEntries.get(str).entry.notification.getPackageName(), this.mUser), Long.valueOf(SystemClock.elapsedRealtime() + this.mSnoozeLengthMs));
        }
        this.mReleaseOnExpandFinish = true;
    }

    public void unpinAll() {
        for (String str : this.mHeadsUpEntries.keySet()) {
            HeadsUpEntry headsUpEntry = this.mHeadsUpEntries.get(str);
            setEntryPinned(headsUpEntry, false);
            headsUpEntry.updateEntry(false);
        }
    }

    public void updateNotification(NotificationData.Entry entry, boolean z) {
        HeadsUpEntry headsUpEntry;
        Log.v("HeadsUpManager", "updateNotification");
        entry.row.sendAccessibilityEvent(2048);
        if (!z || (headsUpEntry = this.mHeadsUpEntries.get(entry.key)) == null) {
            return;
        }
        headsUpEntry.updateEntry();
        setEntryPinned(headsUpEntry, shouldHeadsUpBecomePinned(entry));
    }
}
