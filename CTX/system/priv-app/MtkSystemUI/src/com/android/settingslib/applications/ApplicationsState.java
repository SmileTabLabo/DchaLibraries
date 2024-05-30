package com.android.settingslib.applications;

import android.app.ActivityManager;
import android.app.usage.StorageStats;
import android.app.usage.StorageStatsManager;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.IconDrawableFactory;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.util.ArrayUtils;
import com.android.settingslib.applications.ApplicationsState;
import java.io.File;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
/* loaded from: classes.dex */
public class ApplicationsState {
    final ArrayList<Session> mActiveSessions;
    final int mAdminRetrieveFlags;
    final ArrayList<AppEntry> mAppEntries;
    List<ApplicationInfo> mApplications;
    final BackgroundHandler mBackgroundHandler;
    final Context mContext;
    String mCurComputingSizePkg;
    int mCurComputingSizeUserId;
    UUID mCurComputingSizeUuid;
    long mCurId;
    final IconDrawableFactory mDrawableFactory;
    final SparseArray<HashMap<String, AppEntry>> mEntriesMap;
    boolean mHaveDisabledApps;
    boolean mHaveInstantApps;
    final InterestingConfigChanges mInterestingConfigChanges;
    final IPackageManager mIpm;
    final MainHandler mMainHandler;
    PackageIntentReceiver mPackageIntentReceiver;
    final PackageManager mPm;
    final ArrayList<Session> mRebuildingSessions;
    boolean mResumed;
    final int mRetrieveFlags;
    final ArrayList<Session> mSessions;
    boolean mSessionsChanged;
    final StorageStatsManager mStats;
    final UserManager mUm;
    static final Pattern REMOVE_DIACRITICALS_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    static final Object sLock = new Object();
    public static final Comparator<AppEntry> ALPHA_COMPARATOR = new Comparator<AppEntry>() { // from class: com.android.settingslib.applications.ApplicationsState.1
        private final Collator sCollator = Collator.getInstance();

        @Override // java.util.Comparator
        public int compare(AppEntry appEntry, AppEntry appEntry2) {
            int compare;
            int compare2 = this.sCollator.compare(appEntry.label, appEntry2.label);
            if (compare2 != 0) {
                return compare2;
            }
            if (appEntry.info != null && appEntry2.info != null && (compare = this.sCollator.compare(appEntry.info.packageName, appEntry2.info.packageName)) != 0) {
                return compare;
            }
            return appEntry.info.uid - appEntry2.info.uid;
        }
    };
    public static final Comparator<AppEntry> SIZE_COMPARATOR = new Comparator<AppEntry>() { // from class: com.android.settingslib.applications.ApplicationsState.2
        @Override // java.util.Comparator
        public int compare(AppEntry appEntry, AppEntry appEntry2) {
            if (appEntry.size < appEntry2.size) {
                return 1;
            }
            if (appEntry.size > appEntry2.size) {
                return -1;
            }
            return ApplicationsState.ALPHA_COMPARATOR.compare(appEntry, appEntry2);
        }
    };
    public static final Comparator<AppEntry> INTERNAL_SIZE_COMPARATOR = new Comparator<AppEntry>() { // from class: com.android.settingslib.applications.ApplicationsState.3
        @Override // java.util.Comparator
        public int compare(AppEntry appEntry, AppEntry appEntry2) {
            if (appEntry.internalSize < appEntry2.internalSize) {
                return 1;
            }
            if (appEntry.internalSize > appEntry2.internalSize) {
                return -1;
            }
            return ApplicationsState.ALPHA_COMPARATOR.compare(appEntry, appEntry2);
        }
    };
    public static final Comparator<AppEntry> EXTERNAL_SIZE_COMPARATOR = new Comparator<AppEntry>() { // from class: com.android.settingslib.applications.ApplicationsState.4
        @Override // java.util.Comparator
        public int compare(AppEntry appEntry, AppEntry appEntry2) {
            if (appEntry.externalSize < appEntry2.externalSize) {
                return 1;
            }
            if (appEntry.externalSize > appEntry2.externalSize) {
                return -1;
            }
            return ApplicationsState.ALPHA_COMPARATOR.compare(appEntry, appEntry2);
        }
    };
    public static final AppFilter FILTER_PERSONAL = new AppFilter() { // from class: com.android.settingslib.applications.ApplicationsState.5
        private int mCurrentUser;

        @Override // com.android.settingslib.applications.ApplicationsState.AppFilter
        public void init() {
            this.mCurrentUser = ActivityManager.getCurrentUser();
        }

        @Override // com.android.settingslib.applications.ApplicationsState.AppFilter
        public boolean filterApp(AppEntry appEntry) {
            return UserHandle.getUserId(appEntry.info.uid) == this.mCurrentUser;
        }
    };
    public static final AppFilter FILTER_WITHOUT_DISABLED_UNTIL_USED = new AppFilter() { // from class: com.android.settingslib.applications.ApplicationsState.6
        @Override // com.android.settingslib.applications.ApplicationsState.AppFilter
        public void init() {
        }

        @Override // com.android.settingslib.applications.ApplicationsState.AppFilter
        public boolean filterApp(AppEntry appEntry) {
            return appEntry.info.enabledSetting != 4;
        }
    };
    public static final AppFilter FILTER_WORK = new AppFilter() { // from class: com.android.settingslib.applications.ApplicationsState.7
        private int mCurrentUser;

        @Override // com.android.settingslib.applications.ApplicationsState.AppFilter
        public void init() {
            this.mCurrentUser = ActivityManager.getCurrentUser();
        }

        @Override // com.android.settingslib.applications.ApplicationsState.AppFilter
        public boolean filterApp(AppEntry appEntry) {
            return UserHandle.getUserId(appEntry.info.uid) != this.mCurrentUser;
        }
    };
    public static final AppFilter FILTER_DOWNLOADED_AND_LAUNCHER = new AppFilter() { // from class: com.android.settingslib.applications.ApplicationsState.8
        @Override // com.android.settingslib.applications.ApplicationsState.AppFilter
        public void init() {
        }

        @Override // com.android.settingslib.applications.ApplicationsState.AppFilter
        public boolean filterApp(AppEntry appEntry) {
            if (AppUtils.isInstant(appEntry.info)) {
                return false;
            }
            if (ApplicationsState.hasFlag(appEntry.info.flags, 128) || !ApplicationsState.hasFlag(appEntry.info.flags, 1) || appEntry.hasLauncherEntry) {
                return true;
            }
            return ApplicationsState.hasFlag(appEntry.info.flags, 1) && appEntry.isHomeApp;
        }
    };
    public static final AppFilter FILTER_DOWNLOADED_AND_LAUNCHER_AND_INSTANT = new AppFilter() { // from class: com.android.settingslib.applications.ApplicationsState.9
        @Override // com.android.settingslib.applications.ApplicationsState.AppFilter
        public void init() {
        }

        @Override // com.android.settingslib.applications.ApplicationsState.AppFilter
        public boolean filterApp(AppEntry appEntry) {
            return AppUtils.isInstant(appEntry.info) || ApplicationsState.FILTER_DOWNLOADED_AND_LAUNCHER.filterApp(appEntry);
        }
    };
    public static final AppFilter FILTER_THIRD_PARTY = new AppFilter() { // from class: com.android.settingslib.applications.ApplicationsState.10
        @Override // com.android.settingslib.applications.ApplicationsState.AppFilter
        public void init() {
        }

        @Override // com.android.settingslib.applications.ApplicationsState.AppFilter
        public boolean filterApp(AppEntry appEntry) {
            return ApplicationsState.hasFlag(appEntry.info.flags, 128) || !ApplicationsState.hasFlag(appEntry.info.flags, 1);
        }
    };
    public static final AppFilter FILTER_DISABLED = new AppFilter() { // from class: com.android.settingslib.applications.ApplicationsState.11
        @Override // com.android.settingslib.applications.ApplicationsState.AppFilter
        public void init() {
        }

        @Override // com.android.settingslib.applications.ApplicationsState.AppFilter
        public boolean filterApp(AppEntry appEntry) {
            return (appEntry.info.enabled || AppUtils.isInstant(appEntry.info)) ? false : true;
        }
    };
    public static final AppFilter FILTER_INSTANT = new AppFilter() { // from class: com.android.settingslib.applications.ApplicationsState.12
        @Override // com.android.settingslib.applications.ApplicationsState.AppFilter
        public void init() {
        }

        @Override // com.android.settingslib.applications.ApplicationsState.AppFilter
        public boolean filterApp(AppEntry appEntry) {
            return AppUtils.isInstant(appEntry.info);
        }
    };
    public static final AppFilter FILTER_ALL_ENABLED = new AppFilter() { // from class: com.android.settingslib.applications.ApplicationsState.13
        @Override // com.android.settingslib.applications.ApplicationsState.AppFilter
        public void init() {
        }

        @Override // com.android.settingslib.applications.ApplicationsState.AppFilter
        public boolean filterApp(AppEntry appEntry) {
            return appEntry.info.enabled && !AppUtils.isInstant(appEntry.info);
        }
    };
    public static final AppFilter FILTER_EVERYTHING = new AppFilter() { // from class: com.android.settingslib.applications.ApplicationsState.14
        @Override // com.android.settingslib.applications.ApplicationsState.AppFilter
        public void init() {
        }

        @Override // com.android.settingslib.applications.ApplicationsState.AppFilter
        public boolean filterApp(AppEntry appEntry) {
            return true;
        }
    };
    public static final AppFilter FILTER_WITH_DOMAIN_URLS = new AppFilter() { // from class: com.android.settingslib.applications.ApplicationsState.15
        @Override // com.android.settingslib.applications.ApplicationsState.AppFilter
        public void init() {
        }

        @Override // com.android.settingslib.applications.ApplicationsState.AppFilter
        public boolean filterApp(AppEntry appEntry) {
            return !AppUtils.isInstant(appEntry.info) && ApplicationsState.hasFlag(appEntry.info.privateFlags, 16);
        }
    };
    public static final AppFilter FILTER_NOT_HIDE = new AppFilter() { // from class: com.android.settingslib.applications.ApplicationsState.16
        private String[] mHidePackageNames;

        @Override // com.android.settingslib.applications.ApplicationsState.AppFilter
        public void init(Context context) {
            this.mHidePackageNames = context.getResources().getStringArray(17236012);
        }

        @Override // com.android.settingslib.applications.ApplicationsState.AppFilter
        public void init() {
        }

        @Override // com.android.settingslib.applications.ApplicationsState.AppFilter
        public boolean filterApp(AppEntry appEntry) {
            if (ArrayUtils.contains(this.mHidePackageNames, appEntry.info.packageName)) {
                return appEntry.info.enabled && appEntry.info.enabledSetting != 4;
            }
            return true;
        }
    };
    public static final AppFilter FILTER_GAMES = new AppFilter() { // from class: com.android.settingslib.applications.ApplicationsState.17
        @Override // com.android.settingslib.applications.ApplicationsState.AppFilter
        public void init() {
        }

        @Override // com.android.settingslib.applications.ApplicationsState.AppFilter
        public boolean filterApp(AppEntry appEntry) {
            boolean z;
            synchronized (appEntry.info) {
                z = ApplicationsState.hasFlag(appEntry.info.flags, 33554432) || appEntry.info.category == 0;
            }
            return z;
        }
    };
    public static final AppFilter FILTER_AUDIO = new AppFilter() { // from class: com.android.settingslib.applications.ApplicationsState.18
        @Override // com.android.settingslib.applications.ApplicationsState.AppFilter
        public void init() {
        }

        @Override // com.android.settingslib.applications.ApplicationsState.AppFilter
        public boolean filterApp(AppEntry appEntry) {
            boolean z;
            synchronized (appEntry) {
                z = true;
                if (appEntry.info.category != 1) {
                    z = false;
                }
            }
            return z;
        }
    };
    public static final AppFilter FILTER_MOVIES = new AppFilter() { // from class: com.android.settingslib.applications.ApplicationsState.19
        @Override // com.android.settingslib.applications.ApplicationsState.AppFilter
        public void init() {
        }

        @Override // com.android.settingslib.applications.ApplicationsState.AppFilter
        public boolean filterApp(AppEntry appEntry) {
            boolean z;
            synchronized (appEntry) {
                z = appEntry.info.category == 2;
            }
            return z;
        }
    };
    public static final AppFilter FILTER_PHOTOS = new AppFilter() { // from class: com.android.settingslib.applications.ApplicationsState.20
        @Override // com.android.settingslib.applications.ApplicationsState.AppFilter
        public void init() {
        }

        @Override // com.android.settingslib.applications.ApplicationsState.AppFilter
        public boolean filterApp(AppEntry appEntry) {
            boolean z;
            synchronized (appEntry) {
                z = appEntry.info.category == 3;
            }
            return z;
        }
    };
    public static final AppFilter FILTER_OTHER_APPS = new AppFilter() { // from class: com.android.settingslib.applications.ApplicationsState.21
        @Override // com.android.settingslib.applications.ApplicationsState.AppFilter
        public void init() {
        }

        @Override // com.android.settingslib.applications.ApplicationsState.AppFilter
        public boolean filterApp(AppEntry appEntry) {
            boolean z;
            synchronized (appEntry) {
                if (!ApplicationsState.FILTER_AUDIO.filterApp(appEntry) && !ApplicationsState.FILTER_GAMES.filterApp(appEntry) && !ApplicationsState.FILTER_MOVIES.filterApp(appEntry) && !ApplicationsState.FILTER_PHOTOS.filterApp(appEntry)) {
                    z = false;
                }
                z = true;
            }
            return !z;
        }
    };

    /* loaded from: classes.dex */
    public interface Callbacks {
        void onAllSizesComputed();

        void onLauncherInfoChanged();

        void onLoadEntriesCompleted();

        void onPackageIconChanged();

        void onPackageListChanged();

        void onPackageSizeChanged(String str);

        void onRebuildComplete(ArrayList<AppEntry> arrayList);

        void onRunningStateChanged(boolean z);
    }

    /* loaded from: classes.dex */
    public static class SizeInfo {
    }

    void doResumeIfNeededLocked() {
        if (this.mResumed) {
            return;
        }
        this.mResumed = true;
        if (this.mPackageIntentReceiver == null) {
            this.mPackageIntentReceiver = new PackageIntentReceiver();
            this.mPackageIntentReceiver.registerReceiver();
        }
        this.mApplications = new ArrayList();
        for (UserInfo userInfo : this.mUm.getProfiles(UserHandle.myUserId())) {
            try {
                if (this.mEntriesMap.indexOfKey(userInfo.id) < 0) {
                    this.mEntriesMap.put(userInfo.id, new HashMap<>());
                }
                this.mApplications.addAll(this.mIpm.getInstalledApplications(userInfo.isAdmin() ? this.mAdminRetrieveFlags : this.mRetrieveFlags, userInfo.id).getList());
            } catch (RemoteException e) {
            }
        }
        int i = 0;
        if (this.mInterestingConfigChanges.applyNewConfig(this.mContext.getResources())) {
            clearEntries();
        } else {
            for (int i2 = 0; i2 < this.mAppEntries.size(); i2++) {
                this.mAppEntries.get(i2).sizeStale = true;
            }
        }
        this.mHaveDisabledApps = false;
        this.mHaveInstantApps = false;
        while (i < this.mApplications.size()) {
            ApplicationInfo applicationInfo = this.mApplications.get(i);
            if (!applicationInfo.enabled) {
                if (applicationInfo.enabledSetting != 3) {
                    this.mApplications.remove(i);
                    i--;
                    i++;
                } else {
                    this.mHaveDisabledApps = true;
                }
            }
            if (!this.mHaveInstantApps && AppUtils.isInstant(applicationInfo)) {
                this.mHaveInstantApps = true;
            }
            AppEntry appEntry = this.mEntriesMap.get(UserHandle.getUserId(applicationInfo.uid)).get(applicationInfo.packageName);
            if (appEntry != null) {
                appEntry.info = applicationInfo;
            }
            i++;
        }
        if (this.mAppEntries.size() > this.mApplications.size()) {
            clearEntries();
        }
        this.mCurComputingSizePkg = null;
        if (!this.mBackgroundHandler.hasMessages(2)) {
            this.mBackgroundHandler.sendEmptyMessage(2);
        }
    }

    void clearEntries() {
        for (int i = 0; i < this.mEntriesMap.size(); i++) {
            this.mEntriesMap.valueAt(i).clear();
        }
        this.mAppEntries.clear();
    }

    void doPauseIfNeededLocked() {
        if (!this.mResumed) {
            return;
        }
        for (int i = 0; i < this.mSessions.size(); i++) {
            if (this.mSessions.get(i).mResumed) {
                return;
            }
        }
        doPauseLocked();
    }

    void doPauseLocked() {
        this.mResumed = false;
        if (this.mPackageIntentReceiver != null) {
            this.mPackageIntentReceiver.unregisterReceiver();
            this.mPackageIntentReceiver = null;
        }
    }

    int indexOfApplicationInfoLocked(String str, int i) {
        for (int size = this.mApplications.size() - 1; size >= 0; size--) {
            ApplicationInfo applicationInfo = this.mApplications.get(size);
            if (applicationInfo.packageName.equals(str) && UserHandle.getUserId(applicationInfo.uid) == i) {
                return size;
            }
        }
        return -1;
    }

    void addPackage(String str, int i) {
        try {
            synchronized (this.mEntriesMap) {
                if (this.mResumed) {
                    if (indexOfApplicationInfoLocked(str, i) >= 0) {
                        return;
                    }
                    ApplicationInfo applicationInfo = this.mIpm.getApplicationInfo(str, this.mUm.isUserAdmin(i) ? this.mAdminRetrieveFlags : this.mRetrieveFlags, i);
                    if (applicationInfo == null) {
                        return;
                    }
                    if (!applicationInfo.enabled) {
                        if (applicationInfo.enabledSetting != 3) {
                            return;
                        }
                        this.mHaveDisabledApps = true;
                    }
                    if (AppUtils.isInstant(applicationInfo)) {
                        this.mHaveInstantApps = true;
                    }
                    this.mApplications.add(applicationInfo);
                    if (!this.mBackgroundHandler.hasMessages(2)) {
                        this.mBackgroundHandler.sendEmptyMessage(2);
                    }
                    if (!this.mMainHandler.hasMessages(2)) {
                        this.mMainHandler.sendEmptyMessage(2);
                    }
                }
            }
        } catch (RemoteException e) {
        }
    }

    public void removePackage(String str, int i) {
        synchronized (this.mEntriesMap) {
            int indexOfApplicationInfoLocked = indexOfApplicationInfoLocked(str, i);
            if (indexOfApplicationInfoLocked >= 0) {
                AppEntry appEntry = this.mEntriesMap.get(i).get(str);
                if (appEntry != null) {
                    this.mEntriesMap.get(i).remove(str);
                    this.mAppEntries.remove(appEntry);
                }
                ApplicationInfo applicationInfo = this.mApplications.get(indexOfApplicationInfoLocked);
                this.mApplications.remove(indexOfApplicationInfoLocked);
                if (!applicationInfo.enabled) {
                    this.mHaveDisabledApps = false;
                    Iterator<ApplicationInfo> it = this.mApplications.iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        } else if (!it.next().enabled) {
                            this.mHaveDisabledApps = true;
                            break;
                        }
                    }
                }
                if (AppUtils.isInstant(applicationInfo)) {
                    this.mHaveInstantApps = false;
                    Iterator<ApplicationInfo> it2 = this.mApplications.iterator();
                    while (true) {
                        if (!it2.hasNext()) {
                            break;
                        } else if (AppUtils.isInstant(it2.next())) {
                            this.mHaveInstantApps = true;
                            break;
                        }
                    }
                }
                if (!this.mMainHandler.hasMessages(2)) {
                    this.mMainHandler.sendEmptyMessage(2);
                }
            }
        }
    }

    public void invalidatePackage(String str, int i) {
        removePackage(str, i);
        addPackage(str, i);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void addUser(int i) {
        if (ArrayUtils.contains(this.mUm.getProfileIdsWithDisabled(UserHandle.myUserId()), i)) {
            synchronized (this.mEntriesMap) {
                this.mEntriesMap.put(i, new HashMap<>());
                if (this.mResumed) {
                    doPauseLocked();
                    doResumeIfNeededLocked();
                }
                if (!this.mMainHandler.hasMessages(2)) {
                    this.mMainHandler.sendEmptyMessage(2);
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void removeUser(int i) {
        synchronized (this.mEntriesMap) {
            HashMap<String, AppEntry> hashMap = this.mEntriesMap.get(i);
            if (hashMap != null) {
                for (AppEntry appEntry : hashMap.values()) {
                    this.mAppEntries.remove(appEntry);
                    this.mApplications.remove(appEntry.info);
                }
                this.mEntriesMap.remove(i);
                if (!this.mMainHandler.hasMessages(2)) {
                    this.mMainHandler.sendEmptyMessage(2);
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public AppEntry getEntryLocked(ApplicationInfo applicationInfo) {
        int userId = UserHandle.getUserId(applicationInfo.uid);
        AppEntry appEntry = this.mEntriesMap.get(userId).get(applicationInfo.packageName);
        if (appEntry == null) {
            Context context = this.mContext;
            long j = this.mCurId;
            this.mCurId = 1 + j;
            AppEntry appEntry2 = new AppEntry(context, applicationInfo, j);
            this.mEntriesMap.get(userId).put(applicationInfo.packageName, appEntry2);
            this.mAppEntries.add(appEntry2);
            return appEntry2;
        } else if (appEntry.info != applicationInfo) {
            appEntry.info = applicationInfo;
            return appEntry;
        } else {
            return appEntry;
        }
    }

    void rebuildActiveSessions() {
        synchronized (this.mEntriesMap) {
            if (this.mSessionsChanged) {
                this.mActiveSessions.clear();
                for (int i = 0; i < this.mSessions.size(); i++) {
                    Session session = this.mSessions.get(i);
                    if (session.mResumed) {
                        this.mActiveSessions.add(session);
                    }
                }
            }
        }
    }

    /* loaded from: classes.dex */
    public class Session implements LifecycleObserver {
        final Callbacks mCallbacks;
        private int mFlags;
        private final boolean mHasLifecycle;
        ArrayList<AppEntry> mLastAppList;
        boolean mRebuildAsync;
        Comparator<AppEntry> mRebuildComparator;
        AppFilter mRebuildFilter;
        boolean mRebuildForeground;
        boolean mRebuildRequested;
        ArrayList<AppEntry> mRebuildResult;
        final Object mRebuildSync;
        boolean mResumed;
        final /* synthetic */ ApplicationsState this$0;

        @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
        public void onResume() {
            synchronized (this.this$0.mEntriesMap) {
                if (!this.mResumed) {
                    this.mResumed = true;
                    this.this$0.mSessionsChanged = true;
                    this.this$0.doResumeIfNeededLocked();
                }
            }
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        public void onPause() {
            synchronized (this.this$0.mEntriesMap) {
                if (this.mResumed) {
                    this.mResumed = false;
                    this.this$0.mSessionsChanged = true;
                    this.this$0.mBackgroundHandler.removeMessages(1, this);
                    this.this$0.doPauseIfNeededLocked();
                }
            }
        }

        void handleRebuildList() {
            ArrayList arrayList;
            synchronized (this.mRebuildSync) {
                if (this.mRebuildRequested) {
                    AppFilter appFilter = this.mRebuildFilter;
                    Comparator<AppEntry> comparator = this.mRebuildComparator;
                    this.mRebuildRequested = false;
                    this.mRebuildFilter = null;
                    this.mRebuildComparator = null;
                    if (this.mRebuildForeground) {
                        Process.setThreadPriority(-2);
                        this.mRebuildForeground = false;
                    }
                    if (appFilter != null) {
                        appFilter.init(this.this$0.mContext);
                    }
                    synchronized (this.this$0.mEntriesMap) {
                        arrayList = new ArrayList(this.this$0.mAppEntries);
                    }
                    ArrayList<AppEntry> arrayList2 = new ArrayList<>();
                    for (int i = 0; i < arrayList.size(); i++) {
                        AppEntry appEntry = (AppEntry) arrayList.get(i);
                        if (appEntry != null && (appFilter == null || appFilter.filterApp(appEntry))) {
                            synchronized (this.this$0.mEntriesMap) {
                                if (comparator != null) {
                                    try {
                                        appEntry.ensureLabel(this.this$0.mContext);
                                    } finally {
                                    }
                                }
                                arrayList2.add(appEntry);
                            }
                        }
                    }
                    if (comparator != null) {
                        synchronized (this.this$0.mEntriesMap) {
                            Collections.sort(arrayList2, comparator);
                        }
                    }
                    synchronized (this.mRebuildSync) {
                        if (!this.mRebuildRequested) {
                            this.mLastAppList = arrayList2;
                            if (!this.mRebuildAsync) {
                                this.mRebuildResult = arrayList2;
                                this.mRebuildSync.notifyAll();
                            } else if (!this.this$0.mMainHandler.hasMessages(1, this)) {
                                this.this$0.mMainHandler.sendMessage(this.this$0.mMainHandler.obtainMessage(1, this));
                            }
                        }
                    }
                    Process.setThreadPriority(10);
                }
            }
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        public void onDestroy() {
            if (!this.mHasLifecycle) {
                onPause();
            }
            synchronized (this.this$0.mEntriesMap) {
                this.this$0.mSessions.remove(this);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public class MainHandler extends Handler {
        final /* synthetic */ ApplicationsState this$0;

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            this.this$0.rebuildActiveSessions();
            int i = 0;
            switch (message.what) {
                case 1:
                    Session session = (Session) message.obj;
                    if (this.this$0.mActiveSessions.contains(session)) {
                        session.mCallbacks.onRebuildComplete(session.mLastAppList);
                        return;
                    }
                    return;
                case 2:
                    while (i < this.this$0.mActiveSessions.size()) {
                        this.this$0.mActiveSessions.get(i).mCallbacks.onPackageListChanged();
                        i++;
                    }
                    return;
                case 3:
                    while (i < this.this$0.mActiveSessions.size()) {
                        this.this$0.mActiveSessions.get(i).mCallbacks.onPackageIconChanged();
                        i++;
                    }
                    return;
                case 4:
                    while (i < this.this$0.mActiveSessions.size()) {
                        this.this$0.mActiveSessions.get(i).mCallbacks.onPackageSizeChanged((String) message.obj);
                        i++;
                    }
                    return;
                case 5:
                    while (i < this.this$0.mActiveSessions.size()) {
                        this.this$0.mActiveSessions.get(i).mCallbacks.onAllSizesComputed();
                        i++;
                    }
                    return;
                case 6:
                    for (int i2 = 0; i2 < this.this$0.mActiveSessions.size(); i2++) {
                        this.this$0.mActiveSessions.get(i2).mCallbacks.onRunningStateChanged(message.arg1 != 0);
                    }
                    return;
                case 7:
                    while (i < this.this$0.mActiveSessions.size()) {
                        this.this$0.mActiveSessions.get(i).mCallbacks.onLauncherInfoChanged();
                        i++;
                    }
                    return;
                case 8:
                    while (i < this.this$0.mActiveSessions.size()) {
                        this.this$0.mActiveSessions.get(i).mCallbacks.onLoadEntriesCompleted();
                        i++;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class BackgroundHandler extends Handler {
        boolean mRunning;
        final IPackageStatsObserver.Stub mStatsObserver;
        final /* synthetic */ ApplicationsState this$0;

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            ArrayList arrayList;
            int i;
            int i2;
            synchronized (this.this$0.mRebuildingSessions) {
                if (this.this$0.mRebuildingSessions.size() > 0) {
                    arrayList = new ArrayList(this.this$0.mRebuildingSessions);
                    this.this$0.mRebuildingSessions.clear();
                } else {
                    arrayList = null;
                }
            }
            int i3 = 0;
            if (arrayList != null) {
                for (int i4 = 0; i4 < arrayList.size(); i4++) {
                    ((Session) arrayList.get(i4)).handleRebuildList();
                }
            }
            int combinedSessionFlags = getCombinedSessionFlags(this.this$0.mSessions);
            boolean z = true;
            switch (message.what) {
                case 1:
                default:
                    return;
                case 2:
                    synchronized (this.this$0.mEntriesMap) {
                        i = 0;
                        for (int i5 = 0; i5 < this.this$0.mApplications.size() && i < 6; i5++) {
                            if (!this.mRunning) {
                                this.mRunning = true;
                                this.this$0.mMainHandler.sendMessage(this.this$0.mMainHandler.obtainMessage(6, 1));
                            }
                            ApplicationInfo applicationInfo = this.this$0.mApplications.get(i5);
                            int userId = UserHandle.getUserId(applicationInfo.uid);
                            if (this.this$0.mEntriesMap.get(userId).get(applicationInfo.packageName) == null) {
                                i++;
                                this.this$0.getEntryLocked(applicationInfo);
                            }
                            if (userId != 0) {
                                if (this.this$0.mEntriesMap.indexOfKey(0) >= 0) {
                                    AppEntry appEntry = this.this$0.mEntriesMap.get(0).get(applicationInfo.packageName);
                                    if (appEntry != null && !ApplicationsState.hasFlag(appEntry.info.flags, 8388608)) {
                                        this.this$0.mEntriesMap.get(0).remove(applicationInfo.packageName);
                                        this.this$0.mAppEntries.remove(appEntry);
                                    }
                                }
                            }
                        }
                    }
                    if (i >= 6) {
                        sendEmptyMessage(2);
                        return;
                    }
                    if (!this.this$0.mMainHandler.hasMessages(8)) {
                        this.this$0.mMainHandler.sendEmptyMessage(8);
                    }
                    sendEmptyMessage(3);
                    return;
                case 3:
                    if (ApplicationsState.hasFlag(combinedSessionFlags, 1)) {
                        ArrayList<ResolveInfo> arrayList2 = new ArrayList();
                        this.this$0.mPm.getHomeActivities(arrayList2);
                        synchronized (this.this$0.mEntriesMap) {
                            int size = this.this$0.mEntriesMap.size();
                            for (int i6 = 0; i6 < size; i6++) {
                                HashMap<String, AppEntry> valueAt = this.this$0.mEntriesMap.valueAt(i6);
                                for (ResolveInfo resolveInfo : arrayList2) {
                                    AppEntry appEntry2 = valueAt.get(resolveInfo.activityInfo.packageName);
                                    if (appEntry2 != null) {
                                        appEntry2.isHomeApp = true;
                                    }
                                }
                            }
                        }
                    }
                    sendEmptyMessage(4);
                    return;
                case 4:
                case 5:
                    if ((message.what == 4 && ApplicationsState.hasFlag(combinedSessionFlags, 8)) || (message.what == 5 && ApplicationsState.hasFlag(combinedSessionFlags, 16))) {
                        Intent intent = new Intent("android.intent.action.MAIN", (Uri) null);
                        intent.addCategory(message.what == 4 ? "android.intent.category.LAUNCHER" : "android.intent.category.LEANBACK_LAUNCHER");
                        int i7 = 0;
                        while (i7 < this.this$0.mEntriesMap.size()) {
                            int keyAt = this.this$0.mEntriesMap.keyAt(i7);
                            List queryIntentActivitiesAsUser = this.this$0.mPm.queryIntentActivitiesAsUser(intent, 786944, keyAt);
                            synchronized (this.this$0.mEntriesMap) {
                                HashMap<String, AppEntry> valueAt2 = this.this$0.mEntriesMap.valueAt(i7);
                                int size2 = queryIntentActivitiesAsUser.size();
                                int i8 = i3;
                                while (i8 < size2) {
                                    ResolveInfo resolveInfo2 = (ResolveInfo) queryIntentActivitiesAsUser.get(i8);
                                    String str = resolveInfo2.activityInfo.packageName;
                                    AppEntry appEntry3 = valueAt2.get(str);
                                    if (appEntry3 != null) {
                                        appEntry3.hasLauncherEntry = z;
                                        appEntry3.launcherEntryEnabled = resolveInfo2.activityInfo.enabled | appEntry3.launcherEntryEnabled;
                                    } else {
                                        Log.w("ApplicationsState", "Cannot find pkg: " + str + " on user " + keyAt);
                                    }
                                    i8++;
                                    z = true;
                                }
                            }
                            i7++;
                            i3 = 0;
                            z = true;
                        }
                        if (!this.this$0.mMainHandler.hasMessages(7)) {
                            this.this$0.mMainHandler.sendEmptyMessage(7);
                        }
                    }
                    if (message.what == 4) {
                        sendEmptyMessage(5);
                        return;
                    } else {
                        sendEmptyMessage(6);
                        return;
                    }
                case 6:
                    if (ApplicationsState.hasFlag(combinedSessionFlags, 2)) {
                        synchronized (this.this$0.mEntriesMap) {
                            i2 = 0;
                            while (i3 < this.this$0.mAppEntries.size() && i2 < 2) {
                                AppEntry appEntry4 = this.this$0.mAppEntries.get(i3);
                                if (appEntry4.icon == null || !appEntry4.mounted) {
                                    synchronized (appEntry4) {
                                        if (appEntry4.ensureIconLocked(this.this$0.mContext, this.this$0.mDrawableFactory)) {
                                            if (!this.mRunning) {
                                                this.mRunning = true;
                                                this.this$0.mMainHandler.sendMessage(this.this$0.mMainHandler.obtainMessage(6, 1));
                                            }
                                            i2++;
                                        }
                                    }
                                }
                                i3++;
                            }
                        }
                        if (i2 > 0 && !this.this$0.mMainHandler.hasMessages(3)) {
                            this.this$0.mMainHandler.sendEmptyMessage(3);
                        }
                        if (i2 >= 2) {
                            sendEmptyMessage(6);
                            return;
                        }
                    }
                    sendEmptyMessage(7);
                    return;
                case 7:
                    if (ApplicationsState.hasFlag(combinedSessionFlags, 4)) {
                        synchronized (this.this$0.mEntriesMap) {
                            if (this.this$0.mCurComputingSizePkg != null) {
                                return;
                            }
                            long uptimeMillis = SystemClock.uptimeMillis();
                            for (int i9 = 0; i9 < this.this$0.mAppEntries.size(); i9++) {
                                AppEntry appEntry5 = this.this$0.mAppEntries.get(i9);
                                if (ApplicationsState.hasFlag(appEntry5.info.flags, 8388608) && (appEntry5.size == -1 || appEntry5.sizeStale)) {
                                    if (appEntry5.sizeLoadStart == 0 || appEntry5.sizeLoadStart < uptimeMillis - 20000) {
                                        if (!this.mRunning) {
                                            this.mRunning = true;
                                            this.this$0.mMainHandler.sendMessage(this.this$0.mMainHandler.obtainMessage(6, 1));
                                        }
                                        appEntry5.sizeLoadStart = uptimeMillis;
                                        this.this$0.mCurComputingSizeUuid = appEntry5.info.storageUuid;
                                        this.this$0.mCurComputingSizePkg = appEntry5.info.packageName;
                                        this.this$0.mCurComputingSizeUserId = UserHandle.getUserId(appEntry5.info.uid);
                                        this.this$0.mBackgroundHandler.post(new Runnable() { // from class: com.android.settingslib.applications.-$$Lambda$ApplicationsState$BackgroundHandler$7jhXQzAcRoT6ACDzmPBTQMi7Ldc
                                            @Override // java.lang.Runnable
                                            public final void run() {
                                                ApplicationsState.BackgroundHandler.lambda$handleMessage$0(ApplicationsState.BackgroundHandler.this);
                                            }
                                        });
                                    }
                                    return;
                                }
                            }
                            if (!this.this$0.mMainHandler.hasMessages(5)) {
                                this.this$0.mMainHandler.sendEmptyMessage(5);
                                this.mRunning = false;
                                this.this$0.mMainHandler.sendMessage(this.this$0.mMainHandler.obtainMessage(6, 0));
                            }
                            return;
                        }
                    }
                    return;
            }
        }

        public static /* synthetic */ void lambda$handleMessage$0(BackgroundHandler backgroundHandler) {
            try {
                StorageStats queryStatsForPackage = backgroundHandler.this$0.mStats.queryStatsForPackage(backgroundHandler.this$0.mCurComputingSizeUuid, backgroundHandler.this$0.mCurComputingSizePkg, UserHandle.of(backgroundHandler.this$0.mCurComputingSizeUserId));
                PackageStats packageStats = new PackageStats(backgroundHandler.this$0.mCurComputingSizePkg, backgroundHandler.this$0.mCurComputingSizeUserId);
                packageStats.codeSize = queryStatsForPackage.getCodeBytes();
                packageStats.dataSize = queryStatsForPackage.getDataBytes();
                packageStats.cacheSize = queryStatsForPackage.getCacheBytes();
                try {
                    backgroundHandler.mStatsObserver.onGetStatsCompleted(packageStats, true);
                } catch (RemoteException e) {
                }
            } catch (PackageManager.NameNotFoundException | IOException e2) {
                Log.w("ApplicationsState", "Failed to query stats: " + e2);
                try {
                    backgroundHandler.mStatsObserver.onGetStatsCompleted((PackageStats) null, false);
                } catch (RemoteException e3) {
                }
            }
        }

        private int getCombinedSessionFlags(List<Session> list) {
            int i;
            synchronized (this.this$0.mEntriesMap) {
                i = 0;
                for (Session session : list) {
                    i |= session.mFlags;
                }
            }
            return i;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class PackageIntentReceiver extends BroadcastReceiver {
        private PackageIntentReceiver() {
        }

        void registerReceiver() {
            IntentFilter intentFilter = new IntentFilter("android.intent.action.PACKAGE_ADDED");
            intentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
            intentFilter.addAction("android.intent.action.PACKAGE_CHANGED");
            intentFilter.addDataScheme("package");
            ApplicationsState.this.mContext.registerReceiver(this, intentFilter);
            IntentFilter intentFilter2 = new IntentFilter();
            intentFilter2.addAction("android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE");
            intentFilter2.addAction("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE");
            ApplicationsState.this.mContext.registerReceiver(this, intentFilter2);
            IntentFilter intentFilter3 = new IntentFilter();
            intentFilter3.addAction("android.intent.action.USER_ADDED");
            intentFilter3.addAction("android.intent.action.USER_REMOVED");
            ApplicationsState.this.mContext.registerReceiver(this, intentFilter3);
        }

        void unregisterReceiver() {
            ApplicationsState.this.mContext.unregisterReceiver(this);
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int i = 0;
            if ("android.intent.action.PACKAGE_ADDED".equals(action)) {
                String encodedSchemeSpecificPart = intent.getData().getEncodedSchemeSpecificPart();
                while (i < ApplicationsState.this.mEntriesMap.size()) {
                    ApplicationsState.this.addPackage(encodedSchemeSpecificPart, ApplicationsState.this.mEntriesMap.keyAt(i));
                    i++;
                }
            } else if ("android.intent.action.PACKAGE_REMOVED".equals(action)) {
                String encodedSchemeSpecificPart2 = intent.getData().getEncodedSchemeSpecificPart();
                while (i < ApplicationsState.this.mEntriesMap.size()) {
                    ApplicationsState.this.removePackage(encodedSchemeSpecificPart2, ApplicationsState.this.mEntriesMap.keyAt(i));
                    i++;
                }
            } else if ("android.intent.action.PACKAGE_CHANGED".equals(action)) {
                String encodedSchemeSpecificPart3 = intent.getData().getEncodedSchemeSpecificPart();
                while (i < ApplicationsState.this.mEntriesMap.size()) {
                    ApplicationsState.this.invalidatePackage(encodedSchemeSpecificPart3, ApplicationsState.this.mEntriesMap.keyAt(i));
                    i++;
                }
            } else if ("android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE".equals(action) || "android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE".equals(action)) {
                String[] stringArrayExtra = intent.getStringArrayExtra("android.intent.extra.changed_package_list");
                if (stringArrayExtra != null && stringArrayExtra.length != 0 && "android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE".equals(action)) {
                    for (String str : stringArrayExtra) {
                        for (int i2 = 0; i2 < ApplicationsState.this.mEntriesMap.size(); i2++) {
                            ApplicationsState.this.invalidatePackage(str, ApplicationsState.this.mEntriesMap.keyAt(i2));
                        }
                    }
                }
            } else if ("android.intent.action.USER_ADDED".equals(action)) {
                ApplicationsState.this.addUser(intent.getIntExtra("android.intent.extra.user_handle", -10000));
            } else if ("android.intent.action.USER_REMOVED".equals(action)) {
                ApplicationsState.this.removeUser(intent.getIntExtra("android.intent.extra.user_handle", -10000));
            }
        }
    }

    /* loaded from: classes.dex */
    public static class AppEntry extends SizeInfo {
        public final File apkFile;
        public long externalSize;
        public boolean hasLauncherEntry;
        public Drawable icon;
        public final long id;
        public ApplicationInfo info;
        public long internalSize;
        public boolean isHomeApp;
        public String label;
        public boolean launcherEntryEnabled;
        public boolean mounted;
        public long sizeLoadStart;
        public long size = -1;
        public boolean sizeStale = true;

        public AppEntry(Context context, ApplicationInfo applicationInfo, long j) {
            this.apkFile = new File(applicationInfo.sourceDir);
            this.id = j;
            this.info = applicationInfo;
            ensureLabel(context);
        }

        public void ensureLabel(Context context) {
            if (this.label == null || !this.mounted) {
                if (!this.apkFile.exists()) {
                    this.mounted = false;
                    this.label = this.info.packageName;
                    return;
                }
                this.mounted = true;
                CharSequence loadLabel = this.info.loadLabel(context.getPackageManager());
                this.label = loadLabel != null ? loadLabel.toString() : this.info.packageName;
            }
        }

        boolean ensureIconLocked(Context context, IconDrawableFactory iconDrawableFactory) {
            if (this.icon == null) {
                if (this.apkFile.exists()) {
                    this.icon = iconDrawableFactory.getBadgedIcon(this.info);
                    return true;
                }
                this.mounted = false;
                this.icon = context.getDrawable(17303558);
            } else if (!this.mounted && this.apkFile.exists()) {
                this.mounted = true;
                this.icon = iconDrawableFactory.getBadgedIcon(this.info);
                return true;
            }
            return false;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static boolean hasFlag(int i, int i2) {
        return (i & i2) != 0;
    }

    /* loaded from: classes.dex */
    public interface AppFilter {
        boolean filterApp(AppEntry appEntry);

        void init();

        default void init(Context context) {
            init();
        }
    }
}
