package com.android.launcher3;

import android.appwidget.AppWidgetProviderInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Parcelable;
import android.os.Process;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.Pair;
import com.android.launcher3.compat.AppWidgetManagerCompat;
import com.android.launcher3.compat.LauncherActivityInfoCompat;
import com.android.launcher3.compat.LauncherAppsCompat;
import com.android.launcher3.compat.PackageInstallerCompat;
import com.android.launcher3.compat.UserHandleCompat;
import com.android.launcher3.compat.UserManagerCompat;
import com.android.launcher3.model.GridSizeMigrationTask;
import com.android.launcher3.model.WidgetsModel;
import com.android.launcher3.util.ComponentKey;
import com.android.launcher3.util.CursorIconInfo;
import com.android.launcher3.util.FlagOp;
import com.android.launcher3.util.LongArrayMap;
import com.android.launcher3.util.ManagedProfileHeuristic;
import com.android.launcher3.util.PackageManagerHelper;
import com.android.launcher3.util.StringFilter;
import com.mediatek.launcher3.LauncherLog;
import java.lang.ref.WeakReference;
import java.net.URISyntaxException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
/* loaded from: a.zip:com/android/launcher3/LauncherModel.class */
public class LauncherModel extends BroadcastReceiver implements LauncherAppsCompat.OnAppsChangedCallbackCompat {
    static final ArrayList<Runnable> mBindCompleteRunnables;
    static final ArrayList<Runnable> mDeferredBindRunnables;
    static final ArrayList<LauncherAppWidgetInfo> sBgAppWidgets;
    static final LongArrayMap<FolderInfo> sBgFolders;
    static final LongArrayMap<ItemInfo> sBgItemsIdMap;
    static final Object sBgLock;
    static final ArrayList<ItemInfo> sBgWorkspaceItems;
    static final ArrayList<Long> sBgWorkspaceScreens;
    static final HashMap<UserHandleCompat, HashSet<String>> sPendingPackages;
    static final Handler sWorker;
    static final HandlerThread sWorkerThread = new HandlerThread("launcher-loader");
    boolean mAllAppsLoaded;
    final LauncherAppState mApp;
    private final AllAppsList mBgAllAppsList;
    private final WidgetsModel mBgWidgetsModel;
    WeakReference<Callbacks> mCallbacks;
    boolean mHasLoaderCompletedOnce;
    IconCache mIconCache;
    boolean mIsLoaderTaskRunning;
    final LauncherAppsCompat mLauncherApps;
    LoaderTask mLoaderTask;
    private final boolean mOldContentProviderExists;
    final UserManagerCompat mUserManager;
    boolean mWorkspaceLoaded;
    final Object mLock = new Object();
    DeferredHandler mHandler = new DeferredHandler();

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.android.launcher3.LauncherModel$1  reason: invalid class name */
    /* loaded from: a.zip:com/android/launcher3/LauncherModel$1.class */
    public class AnonymousClass1 implements Runnable {
        final LauncherModel this$0;
        final PackageInstallerCompat.PackageInstallInfo val$installInfo;

        AnonymousClass1(LauncherModel launcherModel, PackageInstallerCompat.PackageInstallInfo packageInstallInfo) {
            this.this$0 = launcherModel;
            this.val$installInfo = packageInstallInfo;
        }

        @Override // java.lang.Runnable
        public void run() {
            synchronized (LauncherModel.sBgLock) {
                HashSet hashSet = new HashSet();
                if (this.val$installInfo.state == 0) {
                    return;
                }
                for (ItemInfo itemInfo : LauncherModel.sBgItemsIdMap) {
                    if (itemInfo instanceof ShortcutInfo) {
                        ShortcutInfo shortcutInfo = (ShortcutInfo) itemInfo;
                        ComponentName targetComponent = shortcutInfo.getTargetComponent();
                        if (shortcutInfo.isPromise() && targetComponent != null && this.val$installInfo.packageName.equals(targetComponent.getPackageName())) {
                            shortcutInfo.setInstallProgress(this.val$installInfo.progress);
                            if (this.val$installInfo.state == 2) {
                                shortcutInfo.status &= -5;
                            }
                            hashSet.add(shortcutInfo);
                        }
                    }
                }
                for (LauncherAppWidgetInfo launcherAppWidgetInfo : LauncherModel.sBgAppWidgets) {
                    if (launcherAppWidgetInfo.providerName.getPackageName().equals(this.val$installInfo.packageName)) {
                        launcherAppWidgetInfo.installProgress = this.val$installInfo.progress;
                        hashSet.add(launcherAppWidgetInfo);
                    }
                }
                if (!hashSet.isEmpty()) {
                    this.this$0.mHandler.post(new Runnable(this, hashSet) { // from class: com.android.launcher3.LauncherModel.1.1
                        final AnonymousClass1 this$1;
                        final HashSet val$updates;

                        {
                            this.this$1 = this;
                            this.val$updates = hashSet;
                        }

                        @Override // java.lang.Runnable
                        public void run() {
                            Callbacks callback = this.this$1.this$0.getCallback();
                            if (callback != null) {
                                callback.bindRestoreItemsChange(this.val$updates);
                            }
                        }
                    });
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.android.launcher3.LauncherModel$2  reason: invalid class name */
    /* loaded from: a.zip:com/android/launcher3/LauncherModel$2.class */
    public class AnonymousClass2 implements Runnable {
        final LauncherModel this$0;
        final String val$packageName;

        AnonymousClass2(LauncherModel launcherModel, String str) {
            this.this$0 = launcherModel;
            this.val$packageName = str;
        }

        @Override // java.lang.Runnable
        public void run() {
            synchronized (LauncherModel.sBgLock) {
                ArrayList arrayList = new ArrayList();
                UserHandleCompat myUserHandle = UserHandleCompat.myUserHandle();
                for (ItemInfo itemInfo : LauncherModel.sBgItemsIdMap) {
                    if (itemInfo instanceof ShortcutInfo) {
                        ShortcutInfo shortcutInfo = (ShortcutInfo) itemInfo;
                        ComponentName targetComponent = shortcutInfo.getTargetComponent();
                        if (shortcutInfo.isPromise() && targetComponent != null && this.val$packageName.equals(targetComponent.getPackageName())) {
                            if (shortcutInfo.hasStatusFlag(2)) {
                                this.this$0.mIconCache.getTitleAndIcon(shortcutInfo, shortcutInfo.promisedIntent, myUserHandle, shortcutInfo.shouldUseLowResIcon());
                            } else {
                                shortcutInfo.updateIcon(this.this$0.mIconCache);
                            }
                            arrayList.add(shortcutInfo);
                        }
                    }
                }
                if (!arrayList.isEmpty()) {
                    this.this$0.mHandler.post(new Runnable(this, arrayList, myUserHandle) { // from class: com.android.launcher3.LauncherModel.2.1
                        final AnonymousClass2 this$1;
                        final ArrayList val$updates;
                        final UserHandleCompat val$user;

                        {
                            this.this$1 = this;
                            this.val$updates = arrayList;
                            this.val$user = myUserHandle;
                        }

                        @Override // java.lang.Runnable
                        public void run() {
                            Callbacks callback = this.this$1.this$0.getCallback();
                            if (callback != null) {
                                callback.bindShortcutsChanged(this.val$updates, new ArrayList<>(), this.val$user);
                            }
                        }
                    });
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.android.launcher3.LauncherModel$3  reason: invalid class name */
    /* loaded from: a.zip:com/android/launcher3/LauncherModel$3.class */
    public class AnonymousClass3 implements Runnable {
        final LauncherModel this$0;
        final ArrayList val$allAppsApps;
        final Callbacks val$callbacks;

        AnonymousClass3(LauncherModel launcherModel, ArrayList arrayList, Callbacks callbacks) {
            this.this$0 = launcherModel;
            this.val$allAppsApps = arrayList;
            this.val$callbacks = callbacks;
        }

        @Override // java.lang.Runnable
        public void run() {
            if (this.val$allAppsApps == null || this.val$allAppsApps.isEmpty()) {
                return;
            }
            this.this$0.runOnMainThread(new Runnable(this, this.val$callbacks, this.val$allAppsApps) { // from class: com.android.launcher3.LauncherModel.3.1
                final AnonymousClass3 this$1;
                final ArrayList val$allAppsApps;
                final Callbacks val$callbacks;

                {
                    this.this$1 = this;
                    this.val$callbacks = r5;
                    this.val$allAppsApps = r6;
                }

                @Override // java.lang.Runnable
                public void run() {
                    Callbacks callback = this.this$1.this$0.getCallback();
                    if (this.val$callbacks != callback || callback == null) {
                        return;
                    }
                    this.val$callbacks.bindAppsAdded(null, null, null, this.val$allAppsApps);
                }
            });
        }
    }

    /* renamed from: com.android.launcher3.LauncherModel$4  reason: invalid class name */
    /* loaded from: a.zip:com/android/launcher3/LauncherModel$4.class */
    class AnonymousClass4 implements Runnable {
        final LauncherModel this$0;
        final Callbacks val$callbacks;
        final Context val$context;
        final ArrayList val$workspaceApps;

        AnonymousClass4(LauncherModel launcherModel, Context context, ArrayList arrayList, Callbacks callbacks) {
            this.this$0 = launcherModel;
            this.val$context = context;
            this.val$workspaceApps = arrayList;
            this.val$callbacks = callbacks;
        }

        @Override // java.lang.Runnable
        public void run() {
            ArrayList arrayList = new ArrayList();
            ArrayList<Long> arrayList2 = new ArrayList<>();
            ArrayList<Long> loadWorkspaceScreensDb = LauncherModel.loadWorkspaceScreensDb(this.val$context);
            synchronized (LauncherModel.sBgLock) {
                for (ShortcutInfo shortcutInfo : this.val$workspaceApps) {
                    if (!(shortcutInfo instanceof ShortcutInfo) || !this.this$0.shortcutExists(this.val$context, shortcutInfo.getIntent(), shortcutInfo.user)) {
                        Pair<Long, int[]> findSpaceForItem = this.this$0.findSpaceForItem(this.val$context, loadWorkspaceScreensDb, arrayList2, 1, 1);
                        long longValue = ((Long) findSpaceForItem.first).longValue();
                        int[] iArr = (int[]) findSpaceForItem.second;
                        if (!(shortcutInfo instanceof ShortcutInfo) && !(shortcutInfo instanceof FolderInfo)) {
                            if (!(shortcutInfo instanceof AppInfo)) {
                                throw new RuntimeException("Unexpected info type");
                            }
                            shortcutInfo = ((AppInfo) shortcutInfo).makeShortcut();
                        }
                        LauncherModel.addItemToDatabase(this.val$context, shortcutInfo, -100L, longValue, iArr[0], iArr[1]);
                        arrayList.add(shortcutInfo);
                    }
                }
            }
            this.this$0.updateWorkspaceScreenOrder(this.val$context, loadWorkspaceScreensDb);
            if (arrayList.isEmpty()) {
                return;
            }
            this.this$0.runOnMainThread(new Runnable(this, this.val$callbacks, arrayList, arrayList2) { // from class: com.android.launcher3.LauncherModel.4.1
                final AnonymousClass4 this$1;
                final ArrayList val$addedShortcutsFinal;
                final ArrayList val$addedWorkspaceScreensFinal;
                final Callbacks val$callbacks;

                {
                    this.this$1 = this;
                    this.val$callbacks = r5;
                    this.val$addedShortcutsFinal = arrayList;
                    this.val$addedWorkspaceScreensFinal = arrayList2;
                }

                @Override // java.lang.Runnable
                public void run() {
                    Callbacks callback = this.this$1.this$0.getCallback();
                    if (this.val$callbacks != callback || callback == null) {
                        return;
                    }
                    ArrayList<ItemInfo> arrayList3 = new ArrayList<>();
                    ArrayList<ItemInfo> arrayList4 = new ArrayList<>();
                    if (!this.val$addedShortcutsFinal.isEmpty()) {
                        long j = ((ItemInfo) this.val$addedShortcutsFinal.get(this.val$addedShortcutsFinal.size() - 1)).screenId;
                        for (ItemInfo itemInfo : this.val$addedShortcutsFinal) {
                            if (itemInfo.screenId == j) {
                                arrayList3.add(itemInfo);
                            } else {
                                arrayList4.add(itemInfo);
                            }
                        }
                    }
                    this.val$callbacks.bindAppsAdded(this.val$addedWorkspaceScreensFinal, arrayList4, arrayList3, null);
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: a.zip:com/android/launcher3/LauncherModel$AppsAvailabilityCheck.class */
    public class AppsAvailabilityCheck extends BroadcastReceiver {
        final LauncherModel this$0;

        AppsAvailabilityCheck(LauncherModel launcherModel) {
            this.this$0 = launcherModel;
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            synchronized (LauncherModel.sBgLock) {
                LauncherAppsCompat launcherAppsCompat = LauncherAppsCompat.getInstance(this.this$0.mApp.getContext());
                PackageManager packageManager = context.getPackageManager();
                ArrayList arrayList = new ArrayList();
                ArrayList arrayList2 = new ArrayList();
                Iterator<T> it = LauncherModel.sPendingPackages.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry entry = (Map.Entry) it.next();
                    UserHandleCompat userHandleCompat = (UserHandleCompat) entry.getKey();
                    arrayList.clear();
                    arrayList2.clear();
                    for (String str : (HashSet) entry.getValue()) {
                        if (!launcherAppsCompat.isPackageEnabledForProfile(str, userHandleCompat)) {
                            if (PackageManagerHelper.isAppOnSdcard(packageManager, str)) {
                                arrayList2.add(str);
                            } else {
                                Launcher.addDumpLog("Launcher.Model", "Package not found: " + str, true);
                                arrayList.add(str);
                            }
                        }
                    }
                    if (!arrayList.isEmpty()) {
                        this.this$0.enqueuePackageUpdated(new PackageUpdatedTask(this.this$0, 3, (String[]) arrayList.toArray(new String[arrayList.size()]), userHandleCompat));
                    }
                    if (!arrayList2.isEmpty()) {
                        this.this$0.enqueuePackageUpdated(new PackageUpdatedTask(this.this$0, 4, (String[]) arrayList2.toArray(new String[arrayList2.size()]), userHandleCompat));
                    }
                }
                LauncherModel.sPendingPackages.clear();
            }
        }
    }

    /* loaded from: a.zip:com/android/launcher3/LauncherModel$Callbacks.class */
    public interface Callbacks {
        void bindAllApplications(ArrayList<AppInfo> arrayList);

        void bindAppInfosRemoved(ArrayList<AppInfo> arrayList);

        void bindAppWidget(LauncherAppWidgetInfo launcherAppWidgetInfo);

        void bindAppsAdded(ArrayList<Long> arrayList, ArrayList<ItemInfo> arrayList2, ArrayList<ItemInfo> arrayList3, ArrayList<AppInfo> arrayList4);

        void bindAppsUpdated(ArrayList<AppInfo> arrayList);

        void bindFolders(LongArrayMap<FolderInfo> longArrayMap);

        void bindItems(ArrayList<ItemInfo> arrayList, int i, int i2, boolean z);

        void bindRestoreItemsChange(HashSet<ItemInfo> hashSet);

        void bindScreens(ArrayList<Long> arrayList);

        void bindSearchProviderChanged();

        void bindShortcutsChanged(ArrayList<ShortcutInfo> arrayList, ArrayList<ShortcutInfo> arrayList2, UserHandleCompat userHandleCompat);

        void bindWidgetsModel(WidgetsModel widgetsModel);

        void bindWidgetsRestored(ArrayList<LauncherAppWidgetInfo> arrayList);

        void bindWorkspaceComponentsRemoved(HashSet<String> hashSet, HashSet<ComponentName> hashSet2, UserHandleCompat userHandleCompat);

        void finishBindingItems();

        int getCurrentWorkspaceScreen();

        boolean isAllAppsButtonRank(int i);

        void notifyWidgetProvidersChanged();

        void onPageBoundSynchronously(int i);

        boolean setLoadOnResume();

        void startBinding();
    }

    /* loaded from: a.zip:com/android/launcher3/LauncherModel$ItemInfoFilter.class */
    public interface ItemInfoFilter {
        boolean filterItem(ItemInfo itemInfo, ItemInfo itemInfo2, ComponentName componentName);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/launcher3/LauncherModel$LoaderTask.class */
    public class LoaderTask implements Runnable {
        private Context mContext;
        private int mFlags;
        boolean mIsLoadingAndBindingWorkspace;
        boolean mLoadAndBindStepFinished;
        private boolean mStopped;
        final LauncherModel this$0;

        LoaderTask(LauncherModel launcherModel, Context context, int i) {
            this.this$0 = launcherModel;
            this.mContext = context;
            this.mFlags = i;
        }

        private void bindWorkspace(int i) {
            LongArrayMap<FolderInfo> clone;
            LongArrayMap<ItemInfo> clone2;
            long uptimeMillis = SystemClock.uptimeMillis();
            Callbacks callbacks = this.this$0.mCallbacks.get();
            if (callbacks == null) {
                Log.w("Launcher.Model", "LoaderTask running with no launcher");
                return;
            }
            ArrayList<ItemInfo> arrayList = new ArrayList<>();
            ArrayList<LauncherAppWidgetInfo> arrayList2 = new ArrayList<>();
            ArrayList<Long> arrayList3 = new ArrayList<>();
            synchronized (LauncherModel.sBgLock) {
                arrayList.addAll(LauncherModel.sBgWorkspaceItems);
                arrayList2.addAll(LauncherModel.sBgAppWidgets);
                arrayList3.addAll(LauncherModel.sBgWorkspaceScreens);
                clone = LauncherModel.sBgFolders.clone();
                clone2 = LauncherModel.sBgItemsIdMap.clone();
            }
            boolean z = i != -1001;
            if (!z) {
                i = callbacks.getCurrentWorkspaceScreen();
            }
            int i2 = i;
            if (i >= arrayList3.size()) {
                i2 = -1001;
            }
            long longValue = i2 < 0 ? -1L : arrayList3.get(i2).longValue();
            this.this$0.unbindWorkspaceItemsOnMainThread();
            ArrayList<ItemInfo> arrayList4 = new ArrayList<>();
            ArrayList<ItemInfo> arrayList5 = new ArrayList<>();
            ArrayList<LauncherAppWidgetInfo> arrayList6 = new ArrayList<>();
            ArrayList<LauncherAppWidgetInfo> arrayList7 = new ArrayList<>();
            LongArrayMap<FolderInfo> longArrayMap = new LongArrayMap<>();
            LongArrayMap<FolderInfo> longArrayMap2 = new LongArrayMap<>();
            filterCurrentWorkspaceItems(longValue, arrayList, arrayList4, arrayList5);
            filterCurrentAppWidgets(longValue, arrayList2, arrayList6, arrayList7);
            filterCurrentFolders(longValue, clone2, clone, longArrayMap, longArrayMap2);
            sortWorkspaceItemsSpatially(arrayList4);
            sortWorkspaceItemsSpatially(arrayList5);
            this.this$0.runOnMainThread(new Runnable(this, callbacks) { // from class: com.android.launcher3.LauncherModel.LoaderTask.8
                final LoaderTask this$1;
                final Callbacks val$oldCallbacks;

                {
                    this.this$1 = this;
                    this.val$oldCallbacks = callbacks;
                }

                @Override // java.lang.Runnable
                public void run() {
                    Callbacks tryGetCallbacks = this.this$1.tryGetCallbacks(this.val$oldCallbacks);
                    if (tryGetCallbacks != null) {
                        tryGetCallbacks.startBinding();
                    }
                }
            });
            bindWorkspaceScreens(callbacks, arrayList3);
            bindWorkspaceItems(callbacks, arrayList4, arrayList6, longArrayMap, null);
            if (z) {
                this.this$0.runOnMainThread(new Runnable(this, callbacks, i2) { // from class: com.android.launcher3.LauncherModel.LoaderTask.9
                    final LoaderTask this$1;
                    final int val$currentScreen;
                    final Callbacks val$oldCallbacks;

                    {
                        this.this$1 = this;
                        this.val$oldCallbacks = callbacks;
                        this.val$currentScreen = i2;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        Callbacks tryGetCallbacks = this.this$1.tryGetCallbacks(this.val$oldCallbacks);
                        if (tryGetCallbacks == null || this.val$currentScreen == -1001) {
                            return;
                        }
                        tryGetCallbacks.onPageBoundSynchronously(this.val$currentScreen);
                    }
                });
            }
            synchronized (LauncherModel.mDeferredBindRunnables) {
                LauncherModel.mDeferredBindRunnables.clear();
            }
            bindWorkspaceItems(callbacks, arrayList5, arrayList7, longArrayMap2, z ? LauncherModel.mDeferredBindRunnables : null);
            Runnable runnable = new Runnable(this, callbacks, uptimeMillis) { // from class: com.android.launcher3.LauncherModel.LoaderTask.10
                final LoaderTask this$1;
                final Callbacks val$oldCallbacks;
                final long val$t;

                {
                    this.this$1 = this;
                    this.val$oldCallbacks = callbacks;
                    this.val$t = uptimeMillis;
                }

                @Override // java.lang.Runnable
                public void run() {
                    Callbacks tryGetCallbacks = this.this$1.tryGetCallbacks(this.val$oldCallbacks);
                    if (tryGetCallbacks != null) {
                        tryGetCallbacks.finishBindingItems();
                    }
                    this.this$1.mIsLoadingAndBindingWorkspace = false;
                    if (LauncherModel.mBindCompleteRunnables.isEmpty()) {
                        return;
                    }
                    synchronized (LauncherModel.mBindCompleteRunnables) {
                        for (Runnable runnable2 : LauncherModel.mBindCompleteRunnables) {
                            LauncherModel.runOnWorkerThread(runnable2);
                        }
                        LauncherModel.mBindCompleteRunnables.clear();
                    }
                }
            };
            if (!z) {
                this.this$0.runOnMainThread(runnable);
                return;
            }
            synchronized (LauncherModel.mDeferredBindRunnables) {
                LauncherModel.mDeferredBindRunnables.add(runnable);
            }
        }

        private void bindWorkspaceItems(Callbacks callbacks, ArrayList<ItemInfo> arrayList, ArrayList<LauncherAppWidgetInfo> arrayList2, LongArrayMap<FolderInfo> longArrayMap, ArrayList<Runnable> arrayList3) {
            boolean z = arrayList3 != null;
            int size = arrayList.size();
            for (int i = 0; i < size; i += 6) {
                Runnable runnable = new Runnable(this, callbacks, arrayList, i, i + 6 <= size ? 6 : size - i) { // from class: com.android.launcher3.LauncherModel.LoaderTask.5
                    final LoaderTask this$1;
                    final int val$chunkSize;
                    final Callbacks val$oldCallbacks;
                    final int val$start;
                    final ArrayList val$workspaceItems;

                    {
                        this.this$1 = this;
                        this.val$oldCallbacks = callbacks;
                        this.val$workspaceItems = arrayList;
                        this.val$start = i;
                        this.val$chunkSize = r8;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        Callbacks tryGetCallbacks = this.this$1.tryGetCallbacks(this.val$oldCallbacks);
                        if (tryGetCallbacks != null) {
                            tryGetCallbacks.bindItems(this.val$workspaceItems, this.val$start, this.val$start + this.val$chunkSize, false);
                        }
                    }
                };
                if (z) {
                    synchronized (arrayList3) {
                        arrayList3.add(runnable);
                    }
                } else {
                    this.this$0.runOnMainThread(runnable);
                }
            }
            if (!longArrayMap.isEmpty()) {
                Runnable runnable2 = new Runnable(this, callbacks, longArrayMap) { // from class: com.android.launcher3.LauncherModel.LoaderTask.6
                    final LoaderTask this$1;
                    final LongArrayMap val$folders;
                    final Callbacks val$oldCallbacks;

                    {
                        this.this$1 = this;
                        this.val$oldCallbacks = callbacks;
                        this.val$folders = longArrayMap;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        Callbacks tryGetCallbacks = this.this$1.tryGetCallbacks(this.val$oldCallbacks);
                        if (tryGetCallbacks != null) {
                            tryGetCallbacks.bindFolders(this.val$folders);
                        }
                    }
                };
                if (z) {
                    synchronized (arrayList3) {
                        arrayList3.add(runnable2);
                    }
                } else {
                    this.this$0.runOnMainThread(runnable2);
                }
            }
            int size2 = arrayList2.size();
            for (int i2 = 0; i2 < size2; i2++) {
                Runnable runnable3 = new Runnable(this, callbacks, arrayList2.get(i2)) { // from class: com.android.launcher3.LauncherModel.LoaderTask.7
                    final LoaderTask this$1;
                    final Callbacks val$oldCallbacks;
                    final LauncherAppWidgetInfo val$widget;

                    {
                        this.this$1 = this;
                        this.val$oldCallbacks = callbacks;
                        this.val$widget = r6;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        Callbacks tryGetCallbacks = this.this$1.tryGetCallbacks(this.val$oldCallbacks);
                        if (tryGetCallbacks != null) {
                            tryGetCallbacks.bindAppWidget(this.val$widget);
                        }
                    }
                };
                if (z) {
                    arrayList3.add(runnable3);
                } else {
                    this.this$0.runOnMainThread(runnable3);
                }
            }
        }

        private void bindWorkspaceScreens(Callbacks callbacks, ArrayList<Long> arrayList) {
            this.this$0.runOnMainThread(new Runnable(this, callbacks, arrayList) { // from class: com.android.launcher3.LauncherModel.LoaderTask.4
                final LoaderTask this$1;
                final Callbacks val$oldCallbacks;
                final ArrayList val$orderedScreens;

                {
                    this.this$1 = this;
                    this.val$oldCallbacks = callbacks;
                    this.val$orderedScreens = arrayList;
                }

                @Override // java.lang.Runnable
                public void run() {
                    Callbacks tryGetCallbacks = this.this$1.tryGetCallbacks(this.val$oldCallbacks);
                    if (tryGetCallbacks != null) {
                        tryGetCallbacks.bindScreens(this.val$orderedScreens);
                    }
                }
            });
        }

        private boolean checkItemPlacement(LongArrayMap<ItemInfo[][]> longArrayMap, ItemInfo itemInfo, ArrayList<Long> arrayList) {
            InvariantDeviceProfile invariantDeviceProfile = LauncherAppState.getInstance().getInvariantDeviceProfile();
            int i = invariantDeviceProfile.numColumns;
            int i2 = invariantDeviceProfile.numRows;
            long j = itemInfo.screenId;
            if (itemInfo.container == -101) {
                if (this.this$0.mCallbacks == null || this.this$0.mCallbacks.get().isAllAppsButtonRank((int) itemInfo.screenId)) {
                    Log.e("Launcher.Model", "Error loading shortcut into hotseat " + itemInfo + " into position (" + itemInfo.screenId + ":" + itemInfo.cellX + "," + itemInfo.cellY + ") occupied by all apps");
                    return false;
                }
                ItemInfo[][] itemInfoArr = longArrayMap.get(-101L);
                if (itemInfo.screenId >= invariantDeviceProfile.numHotseatIcons) {
                    Log.e("Launcher.Model", "Error loading shortcut " + itemInfo + " into hotseat position " + itemInfo.screenId + ", position out of bounds: (0 to " + (invariantDeviceProfile.numHotseatIcons - 1) + ")");
                    return false;
                } else if (itemInfoArr == null) {
                    ItemInfo[][] itemInfoArr2 = new ItemInfo[invariantDeviceProfile.numHotseatIcons][1];
                    itemInfoArr2[(int) itemInfo.screenId][0] = itemInfo;
                    longArrayMap.put(-101L, itemInfoArr2);
                    return true;
                } else if (itemInfoArr[(int) itemInfo.screenId][0] != null) {
                    Log.e("Launcher.Model", "Error loading shortcut into hotseat " + itemInfo + " into position (" + itemInfo.screenId + ":" + itemInfo.cellX + "," + itemInfo.cellY + ") occupied by " + longArrayMap.get(-101L)[(int) itemInfo.screenId][0]);
                    return false;
                } else {
                    itemInfoArr[(int) itemInfo.screenId][0] = itemInfo;
                    return true;
                }
            } else if (itemInfo.container == -100) {
                if (arrayList.contains(Long.valueOf(itemInfo.screenId))) {
                    if (!longArrayMap.containsKey(itemInfo.screenId)) {
                        longArrayMap.put(itemInfo.screenId, new ItemInfo[i + 1][i2 + 1]);
                    }
                    ItemInfo[][] itemInfoArr3 = longArrayMap.get(itemInfo.screenId);
                    if ((itemInfo.container == -100 && itemInfo.cellX < 0) || itemInfo.cellY < 0 || itemInfo.cellX + itemInfo.spanX > i || itemInfo.cellY + itemInfo.spanY > i2) {
                        Log.e("Launcher.Model", "Error loading shortcut " + itemInfo + " into cell (" + j + "-" + itemInfo.screenId + ":" + itemInfo.cellX + "," + itemInfo.cellY + ") out of screen bounds ( " + i + "x" + i2 + ")");
                        return false;
                    }
                    for (int i3 = itemInfo.cellX; i3 < itemInfo.cellX + itemInfo.spanX; i3++) {
                        for (int i4 = itemInfo.cellY; i4 < itemInfo.cellY + itemInfo.spanY; i4++) {
                            if (itemInfoArr3[i3][i4] != null) {
                                Log.e("Launcher.Model", "Error loading shortcut " + itemInfo + " into cell (" + j + "-" + itemInfo.screenId + ":" + i3 + "," + i4 + ") occupied by " + itemInfoArr3[i3][i4]);
                                return false;
                            }
                        }
                    }
                    for (int i5 = itemInfo.cellX; i5 < itemInfo.cellX + itemInfo.spanX; i5++) {
                        for (int i6 = itemInfo.cellY; i6 < itemInfo.cellY + itemInfo.spanY; i6++) {
                            itemInfoArr3[i5][i6] = itemInfo;
                        }
                    }
                    return true;
                }
                return false;
            } else {
                return true;
            }
        }

        private void clearSBgDataStructures() {
            synchronized (LauncherModel.sBgLock) {
                LauncherModel.sBgWorkspaceItems.clear();
                LauncherModel.sBgAppWidgets.clear();
                LauncherModel.sBgFolders.clear();
                LauncherModel.sBgItemsIdMap.clear();
                LauncherModel.sBgWorkspaceScreens.clear();
            }
        }

        private void filterCurrentAppWidgets(long j, ArrayList<LauncherAppWidgetInfo> arrayList, ArrayList<LauncherAppWidgetInfo> arrayList2, ArrayList<LauncherAppWidgetInfo> arrayList3) {
            for (LauncherAppWidgetInfo launcherAppWidgetInfo : arrayList) {
                if (launcherAppWidgetInfo != null) {
                    if (launcherAppWidgetInfo.container == -100 && launcherAppWidgetInfo.screenId == j) {
                        arrayList2.add(launcherAppWidgetInfo);
                    } else {
                        arrayList3.add(launcherAppWidgetInfo);
                    }
                }
            }
        }

        private void filterCurrentFolders(long j, LongArrayMap<ItemInfo> longArrayMap, LongArrayMap<FolderInfo> longArrayMap2, LongArrayMap<FolderInfo> longArrayMap3, LongArrayMap<FolderInfo> longArrayMap4) {
            int size = longArrayMap2.size();
            for (int i = 0; i < size; i++) {
                long keyAt = longArrayMap2.keyAt(i);
                FolderInfo valueAt = longArrayMap2.valueAt(i);
                ItemInfo itemInfo = longArrayMap.get(keyAt);
                if (itemInfo != null && valueAt != null) {
                    if (itemInfo.container == -100 && itemInfo.screenId == j) {
                        longArrayMap3.put(keyAt, valueAt);
                    } else {
                        longArrayMap4.put(keyAt, valueAt);
                    }
                }
            }
        }

        private void filterCurrentWorkspaceItems(long j, ArrayList<ItemInfo> arrayList, ArrayList<ItemInfo> arrayList2, ArrayList<ItemInfo> arrayList3) {
            Iterator<ItemInfo> it = arrayList.iterator();
            while (it.hasNext()) {
                if (it.next() == null) {
                    it.remove();
                }
            }
            HashSet hashSet = new HashSet();
            Collections.sort(arrayList, new Comparator<ItemInfo>(this) { // from class: com.android.launcher3.LauncherModel.LoaderTask.2
                final LoaderTask this$1;

                {
                    this.this$1 = this;
                }

                @Override // java.util.Comparator
                public int compare(ItemInfo itemInfo, ItemInfo itemInfo2) {
                    return Utilities.longCompare(itemInfo.container, itemInfo2.container);
                }
            });
            for (ItemInfo itemInfo : arrayList) {
                if (itemInfo.container == -100) {
                    if (itemInfo.screenId == j) {
                        arrayList2.add(itemInfo);
                        hashSet.add(Long.valueOf(itemInfo.id));
                    } else {
                        arrayList3.add(itemInfo);
                    }
                } else if (itemInfo.container == -101) {
                    arrayList2.add(itemInfo);
                    hashSet.add(Long.valueOf(itemInfo.id));
                } else if (hashSet.contains(Long.valueOf(itemInfo.container))) {
                    arrayList2.add(itemInfo);
                    hashSet.add(Long.valueOf(itemInfo.id));
                } else {
                    arrayList3.add(itemInfo);
                }
            }
        }

        private void loadAllApps() {
            Callbacks callbacks = this.this$0.mCallbacks.get();
            if (callbacks == null) {
                Log.w("Launcher.Model", "LoaderTask running with no launcher (loadAllApps)");
                return;
            }
            List<UserHandleCompat> userProfiles = this.this$0.mUserManager.getUserProfiles();
            this.this$0.mBgAllAppsList.clear();
            for (UserHandleCompat userHandleCompat : userProfiles) {
                List<LauncherActivityInfoCompat> activityList = this.this$0.mLauncherApps.getActivityList(null, userHandleCompat);
                if (activityList == null || activityList.isEmpty()) {
                    return;
                }
                boolean isQuietModeEnabled = this.this$0.mUserManager.isQuietModeEnabled(userHandleCompat);
                for (int i = 0; i < activityList.size(); i++) {
                    this.this$0.mBgAllAppsList.add(new AppInfo(this.mContext, activityList.get(i), userHandleCompat, this.this$0.mIconCache, isQuietModeEnabled));
                }
                ManagedProfileHeuristic managedProfileHeuristic = ManagedProfileHeuristic.get(this.mContext, userHandleCompat);
                if (managedProfileHeuristic != null) {
                    this.this$0.runOnMainThread(new Runnable(this, new Runnable(this, managedProfileHeuristic, activityList) { // from class: com.android.launcher3.LauncherModel.LoaderTask.12
                        final LoaderTask this$1;
                        final List val$apps;
                        final ManagedProfileHeuristic val$heuristic;

                        {
                            this.this$1 = this;
                            this.val$heuristic = managedProfileHeuristic;
                            this.val$apps = activityList;
                        }

                        @Override // java.lang.Runnable
                        public void run() {
                            this.val$heuristic.processUserApps(this.val$apps);
                        }
                    }) { // from class: com.android.launcher3.LauncherModel.LoaderTask.13
                        final LoaderTask this$1;
                        final Runnable val$r;

                        {
                            this.this$1 = this;
                            this.val$r = r5;
                        }

                        @Override // java.lang.Runnable
                        public void run() {
                            if (!this.this$1.mIsLoadingAndBindingWorkspace) {
                                LauncherModel.runOnWorkerThread(this.val$r);
                                return;
                            }
                            synchronized (LauncherModel.mBindCompleteRunnables) {
                                LauncherModel.mBindCompleteRunnables.add(this.val$r);
                            }
                        }
                    });
                }
            }
            ArrayList<AppInfo> arrayList = this.this$0.mBgAllAppsList.added;
            this.this$0.mBgAllAppsList.added = new ArrayList<>();
            this.this$0.mHandler.post(new Runnable(this, callbacks, arrayList) { // from class: com.android.launcher3.LauncherModel.LoaderTask.14
                final LoaderTask this$1;
                final ArrayList val$added;
                final Callbacks val$oldCallbacks;

                {
                    this.this$1 = this;
                    this.val$oldCallbacks = callbacks;
                    this.val$added = arrayList;
                }

                @Override // java.lang.Runnable
                public void run() {
                    SystemClock.uptimeMillis();
                    Callbacks tryGetCallbacks = this.this$1.tryGetCallbacks(this.val$oldCallbacks);
                    if (tryGetCallbacks != null) {
                        tryGetCallbacks.bindAllApplications(this.val$added);
                    } else {
                        Log.i("Launcher.Model", "not binding apps: no Launcher activity");
                    }
                }
            });
            ManagedProfileHeuristic.processAllUsers(userProfiles, this.mContext);
        }

        private void loadAndBindAllApps() {
            if (LauncherLog.DEBUG_LOADER) {
                LauncherLog.d("Launcher.Model", "loadAndBindAllApps: mAllAppsLoaded =" + this.this$0.mAllAppsLoaded + ", mStopped = " + this.mStopped + ", this = " + this);
            }
            if (this.this$0.mAllAppsLoaded) {
                onlyBindAllApps();
                return;
            }
            loadAllApps();
            synchronized (this) {
                if (this.mStopped) {
                    return;
                }
                updateIconCache();
                synchronized (this) {
                    if (this.mStopped) {
                        return;
                    }
                    this.this$0.mAllAppsLoaded = true;
                }
            }
        }

        private void loadAndBindWorkspace() {
            this.mIsLoadingAndBindingWorkspace = true;
            if (!this.this$0.mWorkspaceLoaded) {
                loadWorkspace();
                synchronized (this) {
                    if (this.mStopped) {
                        LauncherLog.d("Launcher.Model", "loadAndBindWorkspace returned by stop flag.");
                        return;
                    }
                    this.this$0.mWorkspaceLoaded = true;
                }
            }
            bindWorkspace(-1);
        }

        /* JADX WARN: Code restructure failed: missing block: B:591:0x10d7, code lost:
            if (r44.restoreStatus != r0) goto L597;
         */
        /* JADX WARN: Code restructure failed: missing block: B:731:0x0253, code lost:
            continue;
         */
        /*
            Code decompiled incorrectly, please refer to instructions dump.
        */
        private void loadWorkspace() {
            HashMap<ComponentKey, AppWidgetProviderInfo> hashMap;
            int i;
            int i2;
            boolean z;
            int i3;
            LauncherAppWidgetInfo launcherAppWidgetInfo;
            Intent intent;
            int i4;
            boolean z2;
            int i5;
            boolean z3;
            boolean z4;
            String str;
            ItemInfo appShortcutInfo;
            Intent intent2;
            ComponentName targetComponent;
            Context context = this.mContext;
            ContentResolver contentResolver = context.getContentResolver();
            PackageManager packageManager = context.getPackageManager();
            boolean isSafeMode = packageManager.isSafeMode();
            LauncherAppsCompat launcherAppsCompat = LauncherAppsCompat.getInstance(context);
            boolean z5 = context.registerReceiver(null, new IntentFilter("com.android.launcher3.SYSTEM_READY")) != null;
            InvariantDeviceProfile invariantDeviceProfile = LauncherAppState.getInstance().getInvariantDeviceProfile();
            int i6 = invariantDeviceProfile.numColumns;
            int i7 = invariantDeviceProfile.numRows;
            if (GridSizeMigrationTask.ENABLED && !GridSizeMigrationTask.migrateGridIfNeeded(this.mContext)) {
                this.mFlags |= 1;
            }
            if ((this.mFlags & 1) != 0) {
                Launcher.addDumpLog("Launcher.Model", "loadWorkspace: resetting launcher database", true);
                LauncherAppState.getLauncherProvider().deleteDatabase();
            }
            if ((this.mFlags & 2) != 0) {
                Launcher.addDumpLog("Launcher.Model", "loadWorkspace: migrating from launcher2", true);
                LauncherAppState.getLauncherProvider().migrateLauncher2Shortcuts();
            } else {
                Launcher.addDumpLog("Launcher.Model", "loadWorkspace: loading default favorites", false);
                LauncherAppState.getLauncherProvider().loadDefaultFavoritesIfNecessary();
            }
            synchronized (LauncherModel.sBgLock) {
                clearSBgDataStructures();
                HashMap<String, Integer> updateAndGetActiveSessionCache = PackageInstallerCompat.getInstance(this.mContext).updateAndGetActiveSessionCache();
                LauncherModel.sBgWorkspaceScreens.addAll(LauncherModel.loadWorkspaceScreensDb(this.mContext));
                ArrayList arrayList = new ArrayList();
                ArrayList arrayList2 = new ArrayList();
                Cursor query = contentResolver.query(LauncherSettings$Favorites.CONTENT_URI, null, null, null, null);
                LongArrayMap<ItemInfo[][]> longArrayMap = new LongArrayMap<>();
                int columnIndexOrThrow = query.getColumnIndexOrThrow("_id");
                int columnIndexOrThrow2 = query.getColumnIndexOrThrow("intent");
                int columnIndexOrThrow3 = query.getColumnIndexOrThrow("title");
                int columnIndexOrThrow4 = query.getColumnIndexOrThrow("container");
                int columnIndexOrThrow5 = query.getColumnIndexOrThrow("itemType");
                int columnIndexOrThrow6 = query.getColumnIndexOrThrow("appWidgetId");
                int columnIndexOrThrow7 = query.getColumnIndexOrThrow("appWidgetProvider");
                int columnIndexOrThrow8 = query.getColumnIndexOrThrow("screen");
                int columnIndexOrThrow9 = query.getColumnIndexOrThrow("cellX");
                int columnIndexOrThrow10 = query.getColumnIndexOrThrow("cellY");
                int columnIndexOrThrow11 = query.getColumnIndexOrThrow("spanX");
                int columnIndexOrThrow12 = query.getColumnIndexOrThrow("spanY");
                int columnIndexOrThrow13 = query.getColumnIndexOrThrow("rank");
                int columnIndexOrThrow14 = query.getColumnIndexOrThrow("restored");
                int columnIndexOrThrow15 = query.getColumnIndexOrThrow("profileId");
                int columnIndexOrThrow16 = query.getColumnIndexOrThrow("options");
                CursorIconInfo cursorIconInfo = new CursorIconInfo(query);
                LongSparseArray longSparseArray = new LongSparseArray();
                LongSparseArray longSparseArray2 = new LongSparseArray();
                Iterator<T> it = this.this$0.mUserManager.getUserProfiles().iterator();
                while (true) {
                    hashMap = null;
                    if (!it.hasNext()) {
                        break;
                    }
                    UserHandleCompat userHandleCompat = (UserHandleCompat) it.next();
                    long serialNumberForUser = this.this$0.mUserManager.getSerialNumberForUser(userHandleCompat);
                    longSparseArray.put(serialNumberForUser, userHandleCompat);
                    longSparseArray2.put(serialNumberForUser, Boolean.valueOf(this.this$0.mUserManager.isQuietModeEnabled(userHandleCompat)));
                }
                while (!this.mStopped && query.moveToNext()) {
                    HashMap<ComponentKey, AppWidgetProviderInfo> hashMap2 = hashMap;
                    try {
                        i2 = query.getInt(columnIndexOrThrow5);
                        HashMap<ComponentKey, AppWidgetProviderInfo> hashMap3 = hashMap;
                        z = query.getInt(columnIndexOrThrow14) != 0;
                        i3 = query.getInt(columnIndexOrThrow4);
                    } catch (Exception e) {
                        Launcher.addDumpLog("Launcher.Model", "Desktop items loading interrupted", e, true);
                        hashMap = hashMap2;
                    }
                    switch (i2) {
                        case PackageInstallerCompat.STATUS_INSTALLED /* 0 */:
                        case 1:
                            long j = query.getLong(columnIndexOrThrow);
                            HashMap<ComponentKey, AppWidgetProviderInfo> hashMap4 = hashMap;
                            String string = query.getString(columnIndexOrThrow2);
                            HashMap<ComponentKey, AppWidgetProviderInfo> hashMap5 = hashMap;
                            long j2 = query.getInt(columnIndexOrThrow15);
                            HashMap<ComponentKey, AppWidgetProviderInfo> hashMap6 = hashMap;
                            UserHandleCompat userHandleCompat2 = (UserHandleCompat) longSparseArray.get(j2);
                            HashMap<ComponentKey, AppWidgetProviderInfo> hashMap7 = hashMap;
                            int i8 = query.getInt(columnIndexOrThrow14);
                            if (userHandleCompat2 == null) {
                                arrayList.add(Long.valueOf(j));
                            } else {
                                try {
                                    Intent parseUri = Intent.parseUri(string, 0);
                                    HashMap<ComponentKey, AppWidgetProviderInfo> hashMap8 = hashMap;
                                    ComponentName component = parseUri.getComponent();
                                    if (component == null || component.getPackageName() == null) {
                                        intent = parseUri;
                                        i4 = i8;
                                        z2 = false;
                                        i5 = 0;
                                        z3 = false;
                                        z4 = z;
                                        str = null;
                                        if (component == null) {
                                            arrayList2.add(Long.valueOf(j));
                                            z4 = false;
                                            intent = parseUri;
                                            i4 = i8;
                                            z2 = false;
                                            i5 = 0;
                                            z3 = false;
                                            str = null;
                                        }
                                    } else {
                                        HashMap<ComponentKey, AppWidgetProviderInfo> hashMap9 = hashMap;
                                        boolean isPackageEnabledForProfile = launcherAppsCompat.isPackageEnabledForProfile(component.getPackageName(), userHandleCompat2);
                                        boolean isActivityEnabledForProfile = isPackageEnabledForProfile ? launcherAppsCompat.isActivityEnabledForProfile(component, userHandleCompat2) : false;
                                        String packageName = isPackageEnabledForProfile ? component.getPackageName() : null;
                                        if (isActivityEnabledForProfile) {
                                            boolean z6 = z;
                                            if (z) {
                                                arrayList2.add(Long.valueOf(j));
                                                z6 = false;
                                            }
                                            intent = parseUri;
                                            i4 = i8;
                                            z2 = false;
                                            i5 = 0;
                                            z3 = false;
                                            z4 = z6;
                                            str = packageName;
                                            if (((Boolean) longSparseArray2.get(j2)).booleanValue()) {
                                                i5 = 8;
                                                str = packageName;
                                                z4 = z6;
                                                z3 = false;
                                                z2 = false;
                                                i4 = i8;
                                                intent = parseUri;
                                            }
                                        } else if (isPackageEnabledForProfile) {
                                            intent = null;
                                            if ((i8 & 2) != 0) {
                                                Intent launchIntentForPackage = packageManager.getLaunchIntentForPackage(component.getPackageName());
                                                intent = launchIntentForPackage;
                                                if (launchIntentForPackage != null) {
                                                    HashMap<ComponentKey, AppWidgetProviderInfo> hashMap10 = hashMap;
                                                    ContentValues contentValues = new ContentValues();
                                                    HashMap<ComponentKey, AppWidgetProviderInfo> hashMap11 = hashMap;
                                                    contentValues.put("intent", launchIntentForPackage.toUri(0));
                                                    HashMap<ComponentKey, AppWidgetProviderInfo> hashMap12 = hashMap;
                                                    updateItem(j, contentValues);
                                                    intent = launchIntentForPackage;
                                                }
                                            }
                                            if (intent == null) {
                                                HashMap<ComponentKey, AppWidgetProviderInfo> hashMap13 = hashMap;
                                                HashMap<ComponentKey, AppWidgetProviderInfo> hashMap14 = hashMap;
                                                Launcher.addDumpLog("Launcher.Model", "Invalid component removed: " + component, true);
                                                HashMap<ComponentKey, AppWidgetProviderInfo> hashMap15 = hashMap;
                                                arrayList.add(Long.valueOf(j));
                                            } else {
                                                arrayList2.add(Long.valueOf(j));
                                                z4 = false;
                                                i4 = i8;
                                                z2 = false;
                                                i5 = 0;
                                                z3 = false;
                                                str = packageName;
                                            }
                                        } else if (z) {
                                            HashMap<ComponentKey, AppWidgetProviderInfo> hashMap16 = hashMap;
                                            HashMap<ComponentKey, AppWidgetProviderInfo> hashMap17 = hashMap;
                                            Launcher.addDumpLog("Launcher.Model", "package not yet restored: " + component, true);
                                            intent = parseUri;
                                            i4 = i8;
                                            z2 = false;
                                            i5 = 0;
                                            z3 = false;
                                            z4 = z;
                                            str = packageName;
                                            if ((i8 & 8) == 0) {
                                                if (updateAndGetActiveSessionCache.containsKey(component.getPackageName())) {
                                                    i4 = i8 | 8;
                                                    HashMap<ComponentKey, AppWidgetProviderInfo> hashMap18 = hashMap;
                                                    ContentValues contentValues2 = new ContentValues();
                                                    HashMap<ComponentKey, AppWidgetProviderInfo> hashMap19 = hashMap;
                                                    contentValues2.put("restored", Integer.valueOf(i4));
                                                    HashMap<ComponentKey, AppWidgetProviderInfo> hashMap20 = hashMap;
                                                    updateItem(j, contentValues2);
                                                    intent = parseUri;
                                                    z2 = false;
                                                    i5 = 0;
                                                    z3 = false;
                                                    z4 = z;
                                                    str = packageName;
                                                } else if ((i8 & 240) != 0) {
                                                    int decodeItemTypeFromFlag = CommonAppTypeParser.decodeItemTypeFromFlag(i8);
                                                    HashMap<ComponentKey, AppWidgetProviderInfo> hashMap21 = hashMap;
                                                    HashMap<ComponentKey, AppWidgetProviderInfo> hashMap22 = hashMap;
                                                    CommonAppTypeParser commonAppTypeParser = new CommonAppTypeParser(j, decodeItemTypeFromFlag, context);
                                                    HashMap<ComponentKey, AppWidgetProviderInfo> hashMap23 = hashMap;
                                                    if (commonAppTypeParser.findDefaultApp()) {
                                                        HashMap<ComponentKey, AppWidgetProviderInfo> hashMap24 = hashMap;
                                                        intent = commonAppTypeParser.parsedIntent;
                                                        HashMap<ComponentKey, AppWidgetProviderInfo> hashMap25 = hashMap;
                                                        intent.getComponent();
                                                        HashMap<ComponentKey, AppWidgetProviderInfo> hashMap26 = hashMap;
                                                        ContentValues contentValues3 = commonAppTypeParser.parsedValues;
                                                        HashMap<ComponentKey, AppWidgetProviderInfo> hashMap27 = hashMap;
                                                        contentValues3.put("restored", (Integer) 0);
                                                        HashMap<ComponentKey, AppWidgetProviderInfo> hashMap28 = hashMap;
                                                        updateItem(j, contentValues3);
                                                        z4 = false;
                                                        z3 = true;
                                                        i4 = i8;
                                                        z2 = false;
                                                        i5 = 0;
                                                        str = packageName;
                                                    } else {
                                                        HashMap<ComponentKey, AppWidgetProviderInfo> hashMap29 = hashMap;
                                                        HashMap<ComponentKey, AppWidgetProviderInfo> hashMap30 = hashMap;
                                                        Launcher.addDumpLog("Launcher.Model", "Unrestored package removed: " + component, true);
                                                        HashMap<ComponentKey, AppWidgetProviderInfo> hashMap31 = hashMap;
                                                        arrayList.add(Long.valueOf(j));
                                                    }
                                                } else {
                                                    HashMap<ComponentKey, AppWidgetProviderInfo> hashMap32 = hashMap;
                                                    HashMap<ComponentKey, AppWidgetProviderInfo> hashMap33 = hashMap;
                                                    Launcher.addDumpLog("Launcher.Model", "Unrestored package removed: " + component, true);
                                                    HashMap<ComponentKey, AppWidgetProviderInfo> hashMap34 = hashMap;
                                                    arrayList.add(Long.valueOf(j));
                                                }
                                            }
                                        } else if (PackageManagerHelper.isAppOnSdcard(packageManager, component.getPackageName())) {
                                            z2 = true;
                                            i5 = 2;
                                            intent = parseUri;
                                            i4 = i8;
                                            z3 = false;
                                            z4 = z;
                                            str = packageName;
                                        } else if (z5) {
                                            HashMap<ComponentKey, AppWidgetProviderInfo> hashMap35 = hashMap;
                                            HashMap<ComponentKey, AppWidgetProviderInfo> hashMap36 = hashMap;
                                            Launcher.addDumpLog("Launcher.Model", "Invalid package removed: " + component, true);
                                            HashMap<ComponentKey, AppWidgetProviderInfo> hashMap37 = hashMap;
                                            arrayList.add(Long.valueOf(j));
                                            continue;
                                        } else {
                                            HashMap<ComponentKey, AppWidgetProviderInfo> hashMap38 = hashMap;
                                            HashMap<ComponentKey, AppWidgetProviderInfo> hashMap39 = hashMap;
                                            Launcher.addDumpLog("Launcher.Model", "Invalid package: " + component + " (check again later)", true);
                                            HashMap<ComponentKey, AppWidgetProviderInfo> hashMap40 = hashMap;
                                            HashSet<String> hashSet = LauncherModel.sPendingPackages.get(userHandleCompat2);
                                            HashSet<String> hashSet2 = hashSet;
                                            if (hashSet == null) {
                                                HashMap<ComponentKey, AppWidgetProviderInfo> hashMap41 = hashMap;
                                                hashSet2 = new HashSet<>();
                                                HashMap<ComponentKey, AppWidgetProviderInfo> hashMap42 = hashMap;
                                                LauncherModel.sPendingPackages.put(userHandleCompat2, hashSet2);
                                            }
                                            HashMap<ComponentKey, AppWidgetProviderInfo> hashMap43 = hashMap;
                                            hashSet2.add(component.getPackageName());
                                            z2 = true;
                                            intent = parseUri;
                                            i4 = i8;
                                            i5 = 0;
                                            z3 = false;
                                            z4 = z;
                                            str = packageName;
                                        }
                                    }
                                    boolean z7 = i3 >= 0 ? query.getInt(columnIndexOrThrow13) >= 3 : false;
                                    if (z3) {
                                        if (userHandleCompat2.equals(UserHandleCompat.myUserHandle())) {
                                            HashMap<ComponentKey, AppWidgetProviderInfo> hashMap44 = hashMap;
                                            appShortcutInfo = this.this$0.getAppShortcutInfo(intent, userHandleCompat2, context, null, cursorIconInfo.iconIndex, columnIndexOrThrow3, false, z7);
                                            intent2 = intent;
                                        } else {
                                            arrayList.add(Long.valueOf(j));
                                        }
                                    } else if (z4) {
                                        if (userHandleCompat2.equals(UserHandleCompat.myUserHandle())) {
                                            HashMap<ComponentKey, AppWidgetProviderInfo> hashMap45 = hashMap;
                                            Launcher.addDumpLog("Launcher.Model", "constructing info for partially restored package", true);
                                            HashMap<ComponentKey, AppWidgetProviderInfo> hashMap46 = hashMap;
                                            appShortcutInfo = this.this$0.getRestoredItemInfo(query, columnIndexOrThrow3, intent, i4, i2, cursorIconInfo, context);
                                            HashMap<ComponentKey, AppWidgetProviderInfo> hashMap47 = hashMap;
                                            intent2 = this.this$0.getRestoredItemIntent(query, context, intent);
                                        } else {
                                            arrayList.add(Long.valueOf(j));
                                        }
                                    } else if (i2 == 0) {
                                        appShortcutInfo = this.this$0.getAppShortcutInfo(intent, userHandleCompat2, context, query, cursorIconInfo.iconIndex, columnIndexOrThrow3, z2, z7);
                                        intent2 = intent;
                                    } else {
                                        ShortcutInfo shortcutInfo = this.this$0.getShortcutInfo(query, context, columnIndexOrThrow3, cursorIconInfo);
                                        HashMap<ComponentKey, AppWidgetProviderInfo> hashMap48 = hashMap;
                                        int i9 = i5;
                                        if (PackageManagerHelper.isAppSuspended(packageManager, str)) {
                                            i9 = i5 | 4;
                                        }
                                        intent2 = intent;
                                        i5 = i9;
                                        appShortcutInfo = shortcutInfo;
                                        if (intent.getAction() != null) {
                                            HashMap<ComponentKey, AppWidgetProviderInfo> hashMap49 = hashMap;
                                            intent2 = intent;
                                            i5 = i9;
                                            appShortcutInfo = shortcutInfo;
                                            if (intent.getCategories() != null) {
                                                HashMap<ComponentKey, AppWidgetProviderInfo> hashMap50 = hashMap;
                                                intent2 = intent;
                                                i5 = i9;
                                                appShortcutInfo = shortcutInfo;
                                                if (intent.getAction().equals("android.intent.action.MAIN")) {
                                                    HashMap<ComponentKey, AppWidgetProviderInfo> hashMap51 = hashMap;
                                                    intent2 = intent;
                                                    i5 = i9;
                                                    appShortcutInfo = shortcutInfo;
                                                    if (intent.getCategories().contains("android.intent.category.LAUNCHER")) {
                                                        HashMap<ComponentKey, AppWidgetProviderInfo> hashMap52 = hashMap;
                                                        intent.addFlags(270532608);
                                                        intent2 = intent;
                                                        i5 = i9;
                                                        appShortcutInfo = shortcutInfo;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (appShortcutInfo == null) {
                                        HashMap<ComponentKey, AppWidgetProviderInfo> hashMap53 = hashMap;
                                        hashMap2 = hashMap;
                                        throw new RuntimeException("Unexpected null ShortcutInfo");
                                        break;
                                    } else {
                                        appShortcutInfo.id = j;
                                        HashMap<ComponentKey, AppWidgetProviderInfo> hashMap54 = hashMap;
                                        appShortcutInfo.intent = intent2;
                                        HashMap<ComponentKey, AppWidgetProviderInfo> hashMap55 = hashMap;
                                        appShortcutInfo.container = i3;
                                        HashMap<ComponentKey, AppWidgetProviderInfo> hashMap56 = hashMap;
                                        appShortcutInfo.screenId = query.getInt(columnIndexOrThrow8);
                                        HashMap<ComponentKey, AppWidgetProviderInfo> hashMap57 = hashMap;
                                        appShortcutInfo.cellX = query.getInt(columnIndexOrThrow9);
                                        HashMap<ComponentKey, AppWidgetProviderInfo> hashMap58 = hashMap;
                                        appShortcutInfo.cellY = query.getInt(columnIndexOrThrow10);
                                        HashMap<ComponentKey, AppWidgetProviderInfo> hashMap59 = hashMap;
                                        appShortcutInfo.rank = query.getInt(columnIndexOrThrow13);
                                        HashMap<ComponentKey, AppWidgetProviderInfo> hashMap60 = hashMap;
                                        appShortcutInfo.spanX = 1;
                                        HashMap<ComponentKey, AppWidgetProviderInfo> hashMap61 = hashMap;
                                        appShortcutInfo.spanY = 1;
                                        HashMap<ComponentKey, AppWidgetProviderInfo> hashMap62 = hashMap;
                                        appShortcutInfo.intent.putExtra("profile", j2);
                                        HashMap<ComponentKey, AppWidgetProviderInfo> hashMap63 = hashMap;
                                        if (appShortcutInfo.promisedIntent != null) {
                                            HashMap<ComponentKey, AppWidgetProviderInfo> hashMap64 = hashMap;
                                            appShortcutInfo.promisedIntent.putExtra("profile", j2);
                                        }
                                        HashMap<ComponentKey, AppWidgetProviderInfo> hashMap65 = hashMap;
                                        appShortcutInfo.isDisabled |= i5;
                                        if (isSafeMode && !Utilities.isSystemApp(context, intent2)) {
                                            appShortcutInfo.isDisabled |= 1;
                                        }
                                        HashMap<ComponentKey, AppWidgetProviderInfo> hashMap66 = hashMap;
                                        if (checkItemPlacement(longArrayMap, appShortcutInfo, LauncherModel.sBgWorkspaceScreens)) {
                                            if (z4 && (targetComponent = appShortcutInfo.getTargetComponent()) != null) {
                                                Integer num = updateAndGetActiveSessionCache.get(targetComponent.getPackageName());
                                                if (num != null) {
                                                    appShortcutInfo.setInstallProgress(num.intValue());
                                                } else {
                                                    appShortcutInfo.status &= -5;
                                                }
                                            }
                                            switch (i3) {
                                                case -101:
                                                case -100:
                                                    LauncherModel.sBgWorkspaceItems.add(appShortcutInfo);
                                                    break;
                                                default:
                                                    LauncherModel.findOrMakeFolder(LauncherModel.sBgFolders, i3).add(appShortcutInfo);
                                                    break;
                                            }
                                            HashMap<ComponentKey, AppWidgetProviderInfo> hashMap67 = hashMap;
                                            LauncherModel.sBgItemsIdMap.put(appShortcutInfo.id, appShortcutInfo);
                                        } else {
                                            HashMap<ComponentKey, AppWidgetProviderInfo> hashMap68 = hashMap;
                                            arrayList.add(Long.valueOf(j));
                                        }
                                    }
                                } catch (URISyntaxException e2) {
                                    HashMap<ComponentKey, AppWidgetProviderInfo> hashMap69 = hashMap;
                                    HashMap<ComponentKey, AppWidgetProviderInfo> hashMap70 = hashMap;
                                    Launcher.addDumpLog("Launcher.Model", "Invalid uri: " + string, true);
                                    HashMap<ComponentKey, AppWidgetProviderInfo> hashMap71 = hashMap;
                                    arrayList.add(Long.valueOf(j));
                                }
                            }
                            break;
                        case 2:
                            HashMap<ComponentKey, AppWidgetProviderInfo> hashMap72 = hashMap;
                            long j3 = query.getLong(columnIndexOrThrow);
                            HashMap<ComponentKey, AppWidgetProviderInfo> hashMap73 = hashMap;
                            FolderInfo findOrMakeFolder = LauncherModel.findOrMakeFolder(LauncherModel.sBgFolders, j3);
                            HashMap<ComponentKey, AppWidgetProviderInfo> hashMap74 = hashMap;
                            findOrMakeFolder.title = query.getString(columnIndexOrThrow3);
                            HashMap<ComponentKey, AppWidgetProviderInfo> hashMap75 = hashMap;
                            findOrMakeFolder.id = j3;
                            HashMap<ComponentKey, AppWidgetProviderInfo> hashMap76 = hashMap;
                            findOrMakeFolder.container = i3;
                            HashMap<ComponentKey, AppWidgetProviderInfo> hashMap77 = hashMap;
                            findOrMakeFolder.screenId = query.getInt(columnIndexOrThrow8);
                            HashMap<ComponentKey, AppWidgetProviderInfo> hashMap78 = hashMap;
                            findOrMakeFolder.cellX = query.getInt(columnIndexOrThrow9);
                            HashMap<ComponentKey, AppWidgetProviderInfo> hashMap79 = hashMap;
                            findOrMakeFolder.cellY = query.getInt(columnIndexOrThrow10);
                            HashMap<ComponentKey, AppWidgetProviderInfo> hashMap80 = hashMap;
                            findOrMakeFolder.spanX = 1;
                            HashMap<ComponentKey, AppWidgetProviderInfo> hashMap81 = hashMap;
                            findOrMakeFolder.spanY = 1;
                            HashMap<ComponentKey, AppWidgetProviderInfo> hashMap82 = hashMap;
                            findOrMakeFolder.options = query.getInt(columnIndexOrThrow16);
                            HashMap<ComponentKey, AppWidgetProviderInfo> hashMap83 = hashMap;
                            if (checkItemPlacement(longArrayMap, findOrMakeFolder, LauncherModel.sBgWorkspaceScreens)) {
                                switch (i3) {
                                    case -101:
                                    case -100:
                                        LauncherModel.sBgWorkspaceItems.add(findOrMakeFolder);
                                        break;
                                }
                                if (z) {
                                    arrayList2.add(Long.valueOf(j3));
                                }
                                HashMap<ComponentKey, AppWidgetProviderInfo> hashMap84 = hashMap;
                                LauncherModel.sBgItemsIdMap.put(findOrMakeFolder.id, findOrMakeFolder);
                                HashMap<ComponentKey, AppWidgetProviderInfo> hashMap85 = hashMap;
                                LauncherModel.sBgFolders.put(findOrMakeFolder.id, findOrMakeFolder);
                                HashMap<ComponentKey, AppWidgetProviderInfo> hashMap86 = hashMap;
                                if (LauncherLog.DEBUG) {
                                    HashMap<ComponentKey, AppWidgetProviderInfo> hashMap87 = hashMap;
                                    HashMap<ComponentKey, AppWidgetProviderInfo> hashMap88 = hashMap;
                                    HashMap<ComponentKey, AppWidgetProviderInfo> hashMap89 = hashMap;
                                    LauncherLog.d("Launcher.Model", "loadWorkspace sBgItemsIdMap.put = " + findOrMakeFolder);
                                } else {
                                    continue;
                                }
                            } else {
                                HashMap<ComponentKey, AppWidgetProviderInfo> hashMap90 = hashMap;
                                arrayList.add(Long.valueOf(j3));
                            }
                        case 4:
                        case 5:
                            boolean z8 = i2 == 5;
                            int i10 = query.getInt(columnIndexOrThrow6);
                            HashMap<ComponentKey, AppWidgetProviderInfo> hashMap91 = hashMap;
                            long j4 = query.getLong(columnIndexOrThrow15);
                            HashMap<ComponentKey, AppWidgetProviderInfo> hashMap92 = hashMap;
                            String string2 = query.getString(columnIndexOrThrow7);
                            HashMap<ComponentKey, AppWidgetProviderInfo> hashMap93 = hashMap;
                            long j5 = query.getLong(columnIndexOrThrow);
                            HashMap<ComponentKey, AppWidgetProviderInfo> hashMap94 = hashMap;
                            UserHandleCompat userHandleCompat3 = (UserHandleCompat) longSparseArray.get(j4);
                            if (userHandleCompat3 == null) {
                                arrayList.add(Long.valueOf(j5));
                            } else {
                                ComponentName unflattenFromString = ComponentName.unflattenFromString(string2);
                                HashMap<ComponentKey, AppWidgetProviderInfo> hashMap95 = hashMap;
                                int i11 = query.getInt(columnIndexOrThrow14);
                                boolean z9 = (i11 & 1) == 0;
                                boolean z10 = (i11 & 2) == 0;
                                HashMap<ComponentKey, AppWidgetProviderInfo> hashMap96 = hashMap;
                                if (hashMap == null) {
                                    hashMap96 = AppWidgetManagerCompat.getInstance(this.mContext).getAllProvidersMap();
                                }
                                HashMap<ComponentKey, AppWidgetProviderInfo> hashMap97 = hashMap96;
                                HashMap<ComponentKey, AppWidgetProviderInfo> hashMap98 = hashMap96;
                                ComponentKey componentKey = new ComponentKey(ComponentName.unflattenFromString(string2), userHandleCompat3);
                                HashMap<ComponentKey, AppWidgetProviderInfo> hashMap99 = hashMap96;
                                AppWidgetProviderInfo appWidgetProviderInfo = hashMap96.get(componentKey);
                                HashMap<ComponentKey, AppWidgetProviderInfo> hashMap100 = hashMap96;
                                boolean isValidProvider = LauncherModel.isValidProvider(appWidgetProviderInfo);
                                if (isSafeMode || z8 || !z10 || isValidProvider) {
                                    if (isValidProvider) {
                                        HashMap<ComponentKey, AppWidgetProviderInfo> hashMap101 = hashMap96;
                                        launcherAppWidgetInfo = new LauncherAppWidgetInfo(i10, appWidgetProviderInfo.provider);
                                        int i12 = i11 & (-9);
                                        int i13 = i12;
                                        if (!z10) {
                                            i13 = z9 ? 4 : i12 & (-3);
                                        }
                                        launcherAppWidgetInfo.restoreStatus = i13;
                                    } else {
                                        HashMap<ComponentKey, AppWidgetProviderInfo> hashMap102 = hashMap96;
                                        HashMap<ComponentKey, AppWidgetProviderInfo> hashMap103 = hashMap96;
                                        Log.v("Launcher.Model", "Widget restore pending id=" + j5 + " appWidgetId=" + i10 + " status =" + i11);
                                        HashMap<ComponentKey, AppWidgetProviderInfo> hashMap104 = hashMap96;
                                        HashMap<ComponentKey, AppWidgetProviderInfo> hashMap105 = hashMap96;
                                        launcherAppWidgetInfo = new LauncherAppWidgetInfo(i10, unflattenFromString);
                                        HashMap<ComponentKey, AppWidgetProviderInfo> hashMap106 = hashMap96;
                                        launcherAppWidgetInfo.restoreStatus = i11;
                                        HashMap<ComponentKey, AppWidgetProviderInfo> hashMap107 = hashMap96;
                                        Integer num2 = updateAndGetActiveSessionCache.get(unflattenFromString.getPackageName());
                                        if ((i11 & 8) == 0) {
                                            if (num2 != null) {
                                                launcherAppWidgetInfo.restoreStatus |= 8;
                                            } else if (!isSafeMode) {
                                                HashMap<ComponentKey, AppWidgetProviderInfo> hashMap108 = hashMap96;
                                                HashMap<ComponentKey, AppWidgetProviderInfo> hashMap109 = hashMap96;
                                                Launcher.addDumpLog("Launcher.Model", "Unrestored widget removed: " + unflattenFromString, true);
                                                HashMap<ComponentKey, AppWidgetProviderInfo> hashMap110 = hashMap96;
                                                arrayList.add(Long.valueOf(j5));
                                                hashMap = hashMap96;
                                                continue;
                                            }
                                        }
                                        launcherAppWidgetInfo.installProgress = num2 == null ? 0 : num2.intValue();
                                    }
                                    HashMap<ComponentKey, AppWidgetProviderInfo> hashMap111 = hashMap96;
                                    launcherAppWidgetInfo.id = j5;
                                    HashMap<ComponentKey, AppWidgetProviderInfo> hashMap112 = hashMap96;
                                    launcherAppWidgetInfo.screenId = query.getInt(columnIndexOrThrow8);
                                    HashMap<ComponentKey, AppWidgetProviderInfo> hashMap113 = hashMap96;
                                    launcherAppWidgetInfo.cellX = query.getInt(columnIndexOrThrow9);
                                    HashMap<ComponentKey, AppWidgetProviderInfo> hashMap114 = hashMap96;
                                    launcherAppWidgetInfo.cellY = query.getInt(columnIndexOrThrow10);
                                    HashMap<ComponentKey, AppWidgetProviderInfo> hashMap115 = hashMap96;
                                    launcherAppWidgetInfo.spanX = query.getInt(columnIndexOrThrow11);
                                    HashMap<ComponentKey, AppWidgetProviderInfo> hashMap116 = hashMap96;
                                    launcherAppWidgetInfo.spanY = query.getInt(columnIndexOrThrow12);
                                    HashMap<ComponentKey, AppWidgetProviderInfo> hashMap117 = hashMap96;
                                    launcherAppWidgetInfo.user = userHandleCompat3;
                                    if (i3 == -100 || i3 == -101) {
                                        launcherAppWidgetInfo.container = i3;
                                        HashMap<ComponentKey, AppWidgetProviderInfo> hashMap118 = hashMap96;
                                        if (checkItemPlacement(longArrayMap, launcherAppWidgetInfo, LauncherModel.sBgWorkspaceScreens)) {
                                            if (!z8) {
                                                String flattenToString = launcherAppWidgetInfo.providerName.flattenToString();
                                                HashMap<ComponentKey, AppWidgetProviderInfo> hashMap119 = hashMap96;
                                                if (flattenToString.equals(string2)) {
                                                    HashMap<ComponentKey, AppWidgetProviderInfo> hashMap120 = hashMap96;
                                                    break;
                                                }
                                                HashMap<ComponentKey, AppWidgetProviderInfo> hashMap121 = hashMap96;
                                                HashMap<ComponentKey, AppWidgetProviderInfo> hashMap122 = hashMap96;
                                                ContentValues contentValues4 = new ContentValues();
                                                HashMap<ComponentKey, AppWidgetProviderInfo> hashMap123 = hashMap96;
                                                contentValues4.put("appWidgetProvider", flattenToString);
                                                HashMap<ComponentKey, AppWidgetProviderInfo> hashMap124 = hashMap96;
                                                contentValues4.put("restored", Integer.valueOf(launcherAppWidgetInfo.restoreStatus));
                                                HashMap<ComponentKey, AppWidgetProviderInfo> hashMap125 = hashMap96;
                                                updateItem(j5, contentValues4);
                                            }
                                            HashMap<ComponentKey, AppWidgetProviderInfo> hashMap126 = hashMap96;
                                            LauncherModel.sBgItemsIdMap.put(launcherAppWidgetInfo.id, launcherAppWidgetInfo);
                                            HashMap<ComponentKey, AppWidgetProviderInfo> hashMap127 = hashMap96;
                                            LauncherModel.sBgAppWidgets.add(launcherAppWidgetInfo);
                                            hashMap = hashMap96;
                                        } else {
                                            HashMap<ComponentKey, AppWidgetProviderInfo> hashMap128 = hashMap96;
                                            arrayList.add(Long.valueOf(j5));
                                            hashMap = hashMap96;
                                        }
                                    } else {
                                        Log.e("Launcher.Model", "Widget found where container != CONTAINER_DESKTOP nor CONTAINER_HOTSEAT - ignoring!");
                                        HashMap<ComponentKey, AppWidgetProviderInfo> hashMap129 = hashMap96;
                                        arrayList.add(Long.valueOf(j5));
                                        hashMap = hashMap96;
                                    }
                                } else {
                                    HashMap<ComponentKey, AppWidgetProviderInfo> hashMap130 = hashMap96;
                                    HashMap<ComponentKey, AppWidgetProviderInfo> hashMap131 = hashMap96;
                                    String str2 = "Deleting widget that isn't installed anymore: id=" + j5 + " appWidgetId=" + i10;
                                    HashMap<ComponentKey, AppWidgetProviderInfo> hashMap132 = hashMap96;
                                    Log.e("Launcher.Model", str2);
                                    HashMap<ComponentKey, AppWidgetProviderInfo> hashMap133 = hashMap96;
                                    Launcher.addDumpLog("Launcher.Model", str2, false);
                                    HashMap<ComponentKey, AppWidgetProviderInfo> hashMap134 = hashMap96;
                                    arrayList.add(Long.valueOf(j5));
                                    hashMap = hashMap96;
                                }
                            }
                            break;
                    }
                    Launcher.addDumpLog("Launcher.Model", "Desktop items loading interrupted", e, true);
                    hashMap = hashMap2;
                }
                if (query != null) {
                    query.close();
                }
                if (this.mStopped) {
                    clearSBgDataStructures();
                    return;
                }
                if (arrayList.size() > 0) {
                    contentResolver.delete(LauncherSettings$Favorites.CONTENT_URI, Utilities.createDbSelectionQuery("_id", arrayList), null);
                    for (Long l : LauncherAppState.getLauncherProvider().deleteEmptyFolders()) {
                        long longValue = l.longValue();
                        LauncherModel.sBgWorkspaceItems.remove(LauncherModel.sBgFolders.get(longValue));
                        LauncherModel.sBgFolders.remove(longValue);
                        LauncherModel.sBgItemsIdMap.remove(longValue);
                    }
                }
                for (FolderInfo folderInfo : LauncherModel.sBgFolders) {
                    Collections.sort(folderInfo.contents, Folder.ITEM_POS_COMPARATOR);
                    int i14 = 0;
                    Iterator<T> it2 = folderInfo.contents.iterator();
                    do {
                        if (it2.hasNext()) {
                            ShortcutInfo shortcutInfo2 = (ShortcutInfo) it2.next();
                            if (shortcutInfo2.usingLowResIcon) {
                                shortcutInfo2.updateIcon(this.this$0.mIconCache, false);
                            }
                            i = i14 + 1;
                            i14 = i;
                        }
                    } while (i < 3);
                }
                if (arrayList2.size() > 0) {
                    ContentValues contentValues5 = new ContentValues();
                    contentValues5.put("restored", (Integer) 0);
                    contentResolver.update(LauncherSettings$Favorites.CONTENT_URI, contentValues5, Utilities.createDbSelectionQuery("_id", arrayList2), null);
                }
                if (!z5 && !LauncherModel.sPendingPackages.isEmpty()) {
                    context.registerReceiver(new AppsAvailabilityCheck(this.this$0), new IntentFilter("com.android.launcher3.SYSTEM_READY"), null, LauncherModel.sWorker);
                }
                ArrayList arrayList3 = new ArrayList(LauncherModel.sBgWorkspaceScreens);
                for (ItemInfo itemInfo : LauncherModel.sBgItemsIdMap) {
                    long j6 = itemInfo.screenId;
                    if (itemInfo.container == -100 && arrayList3.contains(Long.valueOf(j6))) {
                        arrayList3.remove(Long.valueOf(j6));
                    }
                }
                if (arrayList3.size() != 0) {
                    LauncherModel.sBgWorkspaceScreens.removeAll(arrayList3);
                    this.this$0.updateWorkspaceScreenOrder(context, LauncherModel.sBgWorkspaceScreens);
                }
            }
        }

        private void onlyBindAllApps() {
            Callbacks callbacks = this.this$0.mCallbacks.get();
            if (callbacks == null) {
                Log.w("Launcher.Model", "LoaderTask running with no launcher (onlyBindAllApps)");
                return;
            }
            Runnable runnable = new Runnable(this, callbacks, (ArrayList) this.this$0.mBgAllAppsList.data.clone()) { // from class: com.android.launcher3.LauncherModel.LoaderTask.11
                final LoaderTask this$1;
                final ArrayList val$list;
                final Callbacks val$oldCallbacks;

                {
                    this.this$1 = this;
                    this.val$oldCallbacks = callbacks;
                    this.val$list = r6;
                }

                @Override // java.lang.Runnable
                public void run() {
                    SystemClock.uptimeMillis();
                    Callbacks tryGetCallbacks = this.this$1.tryGetCallbacks(this.val$oldCallbacks);
                    if (tryGetCallbacks != null) {
                        tryGetCallbacks.bindAllApplications(this.val$list);
                    }
                }
            };
            if (LauncherModel.sWorkerThread.getThreadId() != Process.myTid()) {
                runnable.run();
            } else {
                this.this$0.mHandler.post(runnable);
            }
        }

        private void sortWorkspaceItemsSpatially(ArrayList<ItemInfo> arrayList) {
            Collections.sort(arrayList, new Comparator<ItemInfo>(this, LauncherAppState.getInstance().getInvariantDeviceProfile()) { // from class: com.android.launcher3.LauncherModel.LoaderTask.3
                final LoaderTask this$1;
                final InvariantDeviceProfile val$profile;

                {
                    this.this$1 = this;
                    this.val$profile = r5;
                }

                @Override // java.util.Comparator
                public int compare(ItemInfo itemInfo, ItemInfo itemInfo2) {
                    int i = this.val$profile.numColumns;
                    int i2 = i * this.val$profile.numRows;
                    int i3 = i2 * 6;
                    return Utilities.longCompare((itemInfo.container * i3) + (itemInfo.screenId * i2) + (itemInfo.cellY * i) + itemInfo.cellX, (itemInfo2.container * i3) + (itemInfo2.screenId * i2) + (itemInfo2.cellY * i) + itemInfo2.cellX);
                }
            });
        }

        private void updateIconCache() {
            HashSet hashSet = new HashSet();
            synchronized (LauncherModel.sBgLock) {
                for (ItemInfo itemInfo : LauncherModel.sBgItemsIdMap) {
                    if (itemInfo instanceof ShortcutInfo) {
                        ShortcutInfo shortcutInfo = (ShortcutInfo) itemInfo;
                        if (shortcutInfo.isPromise() && shortcutInfo.getTargetComponent() != null) {
                            hashSet.add(shortcutInfo.getTargetComponent().getPackageName());
                        }
                    } else if (itemInfo instanceof LauncherAppWidgetInfo) {
                        LauncherAppWidgetInfo launcherAppWidgetInfo = (LauncherAppWidgetInfo) itemInfo;
                        if (launcherAppWidgetInfo.hasRestoreFlag(2)) {
                            hashSet.add(launcherAppWidgetInfo.providerName.getPackageName());
                        }
                    }
                }
            }
            this.this$0.mIconCache.updateDbIcons(hashSet);
        }

        private void updateItem(long j, ContentValues contentValues) {
            this.mContext.getContentResolver().update(LauncherSettings$Favorites.CONTENT_URI, contentValues, "_id= ?", new String[]{Long.toString(j)});
        }

        private void waitForIdle() {
            synchronized (this) {
                this.this$0.mHandler.postIdle(new Runnable(this) { // from class: com.android.launcher3.LauncherModel.LoaderTask.1
                    final LoaderTask this$1;

                    {
                        this.this$1 = this;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        synchronized (this.this$1) {
                            this.this$1.mLoadAndBindStepFinished = true;
                            this.this$1.notify();
                        }
                    }
                });
                while (!this.mStopped && !this.mLoadAndBindStepFinished) {
                    try {
                        wait(1000L);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }

        public void dumpState() {
            synchronized (LauncherModel.sBgLock) {
                Log.d("Launcher.Model", "mLoaderTask.mContext=" + this.mContext);
                Log.d("Launcher.Model", "mLoaderTask.mStopped=" + this.mStopped);
                Log.d("Launcher.Model", "mLoaderTask.mLoadAndBindStepFinished=" + this.mLoadAndBindStepFinished);
                Log.d("Launcher.Model", "mItems size=" + LauncherModel.sBgWorkspaceItems.size());
            }
        }

        @Override // java.lang.Runnable
        public void run() {
            synchronized (this.this$0.mLock) {
                if (this.mStopped) {
                    return;
                }
                this.this$0.mIsLoaderTaskRunning = true;
                loadAndBindWorkspace();
                if (this.mStopped) {
                    LauncherLog.d("Launcher.Model", "LoadTask break in the middle, this = " + this);
                } else {
                    waitForIdle();
                    loadAndBindAllApps();
                }
                this.mContext = null;
                synchronized (this.this$0.mLock) {
                    if (this.this$0.mLoaderTask == this) {
                        this.this$0.mLoaderTask = null;
                    }
                    this.this$0.mIsLoaderTaskRunning = false;
                    this.this$0.mHasLoaderCompletedOnce = true;
                }
            }
        }

        void runBindSynchronousPage(int i) {
            if (LauncherLog.DEBUG) {
                LauncherLog.d("Launcher.Model", "runBindSynchronousPage: mAllAppsLoaded = " + this.this$0.mAllAppsLoaded + ",mWorkspaceLoaded = " + this.this$0.mWorkspaceLoaded + ",synchronousBindPage = " + i + ",mIsLoaderTaskRunning = " + this.this$0.mIsLoaderTaskRunning + ",mStopped = " + this.mStopped + ",this = " + this);
            }
            if (i == -1001) {
                throw new RuntimeException("Should not call runBindSynchronousPage() without valid page index");
            }
            if (!this.this$0.mAllAppsLoaded || !this.this$0.mWorkspaceLoaded) {
                throw new RuntimeException("Expecting AllApps and Workspace to be loaded");
            }
            synchronized (this.this$0.mLock) {
                if (this.this$0.mIsLoaderTaskRunning) {
                    throw new RuntimeException("Error! Background loading is already running");
                }
            }
            this.this$0.mHandler.flush();
            bindWorkspace(i);
            onlyBindAllApps();
        }

        public void stopLocked() {
            synchronized (this) {
                this.mStopped = true;
                notify();
            }
        }

        Callbacks tryGetCallbacks(Callbacks callbacks) {
            synchronized (this.this$0.mLock) {
                if (this.mStopped) {
                    LauncherLog.d("Launcher.Model", "tryGetCallbacks returned null by stop flag.");
                    return null;
                } else if (this.this$0.mCallbacks == null) {
                    return null;
                } else {
                    Callbacks callbacks2 = this.this$0.mCallbacks.get();
                    if (callbacks2 != callbacks) {
                        return null;
                    }
                    if (callbacks2 == null) {
                        Log.w("Launcher.Model", "no mCallbacks");
                        return null;
                    }
                    return callbacks2;
                }
            }
        }
    }

    /* loaded from: a.zip:com/android/launcher3/LauncherModel$PackageUpdatedTask.class */
    private class PackageUpdatedTask implements Runnable {
        int mOp;
        String[] mPackages;
        UserHandleCompat mUser;
        final LauncherModel this$0;

        public PackageUpdatedTask(LauncherModel launcherModel, int i, String[] strArr, UserHandleCompat userHandleCompat) {
            this.this$0 = launcherModel;
            this.mOp = i;
            this.mPackages = strArr;
            this.mUser = userHandleCompat;
        }

        @Override // java.lang.Runnable
        public void run() {
            FlagOp addFlag;
            StringFilter matchesAll;
            if (this.this$0.mHasLoaderCompletedOnce) {
                Context context = this.this$0.mApp.getContext();
                String[] strArr = this.mPackages;
                int length = strArr.length;
                FlagOp flagOp = FlagOp.NO_OP;
                StringFilter of = StringFilter.of(new HashSet(Arrays.asList(strArr)));
                switch (this.mOp) {
                    case 1:
                        for (int i = 0; i < length; i++) {
                            this.this$0.mIconCache.updateIconsForPkg(strArr[i], this.mUser);
                            this.this$0.mBgAllAppsList.addPackage(context, strArr[i], this.mUser);
                        }
                        ManagedProfileHeuristic managedProfileHeuristic = ManagedProfileHeuristic.get(context, this.mUser);
                        addFlag = flagOp;
                        matchesAll = of;
                        if (managedProfileHeuristic != null) {
                            managedProfileHeuristic.processPackageAdd(this.mPackages);
                            addFlag = flagOp;
                            matchesAll = of;
                            break;
                        }
                        break;
                    case 2:
                        for (int i2 = 0; i2 < length; i2++) {
                            this.this$0.mIconCache.updateIconsForPkg(strArr[i2], this.mUser);
                            this.this$0.mBgAllAppsList.updatePackage(context, strArr[i2], this.mUser);
                            this.this$0.mApp.getWidgetCache().removePackage(strArr[i2], this.mUser);
                        }
                        addFlag = FlagOp.removeFlag(2);
                        matchesAll = of;
                        break;
                    case 3:
                        ManagedProfileHeuristic managedProfileHeuristic2 = ManagedProfileHeuristic.get(context, this.mUser);
                        if (managedProfileHeuristic2 != null) {
                            managedProfileHeuristic2.processPackageRemoved(this.mPackages);
                        }
                        for (String str : strArr) {
                            this.this$0.mIconCache.removeIconsForPkg(str, this.mUser);
                        }
                    case 4:
                        for (int i3 = 0; i3 < length; i3++) {
                            this.this$0.mBgAllAppsList.removePackage(strArr[i3], this.mUser);
                            this.this$0.mApp.getWidgetCache().removePackage(strArr[i3], this.mUser);
                        }
                        addFlag = FlagOp.addFlag(2);
                        matchesAll = of;
                        break;
                    case 5:
                    case 6:
                        addFlag = this.mOp == 5 ? FlagOp.addFlag(4) : FlagOp.removeFlag(4);
                        this.this$0.mBgAllAppsList.updatePackageFlags(of, this.mUser, addFlag);
                        matchesAll = of;
                        break;
                    case 7:
                        addFlag = UserManagerCompat.getInstance(context).isQuietModeEnabled(this.mUser) ? FlagOp.addFlag(8) : FlagOp.removeFlag(8);
                        matchesAll = StringFilter.matchesAll();
                        this.this$0.mBgAllAppsList.updatePackageFlags(matchesAll, this.mUser, addFlag);
                        break;
                    default:
                        matchesAll = of;
                        addFlag = flagOp;
                        break;
                }
                ArrayList<AppInfo> arrayList = null;
                ArrayList<AppInfo> arrayList2 = null;
                ArrayList<AppInfo> arrayList3 = new ArrayList();
                if (this.this$0.mBgAllAppsList.added.size() > 0) {
                    arrayList = new ArrayList<>(this.this$0.mBgAllAppsList.added);
                    this.this$0.mBgAllAppsList.added.clear();
                }
                if (this.this$0.mBgAllAppsList.modified.size() > 0) {
                    arrayList2 = new ArrayList(this.this$0.mBgAllAppsList.modified);
                    this.this$0.mBgAllAppsList.modified.clear();
                }
                if (this.this$0.mBgAllAppsList.removed.size() > 0) {
                    arrayList3.addAll(this.this$0.mBgAllAppsList.removed);
                    this.this$0.mBgAllAppsList.removed.clear();
                }
                HashMap hashMap = new HashMap();
                if (arrayList != null) {
                    this.this$0.addAppsToAllApps(context, arrayList);
                    for (AppInfo appInfo : arrayList) {
                        hashMap.put(appInfo.componentName, appInfo);
                    }
                }
                if (arrayList2 != null) {
                    Callbacks callback = this.this$0.getCallback();
                    for (AppInfo appInfo2 : arrayList2) {
                        hashMap.put(appInfo2.componentName, appInfo2);
                    }
                    this.this$0.mHandler.post(new Runnable(this, callback, arrayList2) { // from class: com.android.launcher3.LauncherModel.PackageUpdatedTask.1
                        final PackageUpdatedTask this$1;
                        final Callbacks val$callbacks;
                        final ArrayList val$modifiedFinal;

                        {
                            this.this$1 = this;
                            this.val$callbacks = callback;
                            this.val$modifiedFinal = arrayList2;
                        }

                        @Override // java.lang.Runnable
                        public void run() {
                            Callbacks callback2 = this.this$1.this$0.getCallback();
                            if (this.val$callbacks != callback2 || callback2 == null) {
                                return;
                            }
                            this.val$callbacks.bindAppsUpdated(this.val$modifiedFinal);
                        }
                    });
                }
                if (this.mOp == 1 || addFlag != FlagOp.NO_OP) {
                    ArrayList arrayList4 = new ArrayList();
                    ArrayList arrayList5 = new ArrayList();
                    ArrayList arrayList6 = new ArrayList();
                    synchronized (LauncherModel.sBgLock) {
                        for (ItemInfo itemInfo : LauncherModel.sBgItemsIdMap) {
                            if ((itemInfo instanceof ShortcutInfo) && this.mUser.equals(itemInfo.user)) {
                                ShortcutInfo shortcutInfo = (ShortcutInfo) itemInfo;
                                boolean z = false;
                                if (shortcutInfo.iconResource != null) {
                                    z = false;
                                    if (matchesAll.matches(shortcutInfo.iconResource.packageName)) {
                                        Bitmap createIconBitmap = Utilities.createIconBitmap(shortcutInfo.iconResource.packageName, shortcutInfo.iconResource.resourceName, context);
                                        z = false;
                                        if (createIconBitmap != null) {
                                            shortcutInfo.setIcon(createIconBitmap);
                                            shortcutInfo.usingFallbackIcon = false;
                                            z = true;
                                        }
                                    }
                                }
                                ComponentName targetComponent = shortcutInfo.getTargetComponent();
                                boolean z2 = z;
                                boolean z3 = false;
                                if (targetComponent != null) {
                                    z2 = z;
                                    z3 = false;
                                    if (matchesAll.matches(targetComponent.getPackageName())) {
                                        AppInfo appInfo3 = (AppInfo) hashMap.get(targetComponent);
                                        AppInfo appInfo4 = appInfo3;
                                        boolean z4 = z;
                                        if (shortcutInfo.isPromise()) {
                                            appInfo4 = appInfo3;
                                            if (shortcutInfo.hasStatusFlag(2)) {
                                                PackageManager packageManager = context.getPackageManager();
                                                appInfo4 = appInfo3;
                                                if (packageManager.resolveActivity(new Intent("android.intent.action.MAIN").setComponent(targetComponent).addCategory("android.intent.category.LAUNCHER"), 65536) == null) {
                                                    Intent launchIntentForPackage = packageManager.getLaunchIntentForPackage(targetComponent.getPackageName());
                                                    if (launchIntentForPackage != null) {
                                                        appInfo3 = (AppInfo) hashMap.get(launchIntentForPackage.getComponent());
                                                    }
                                                    if (launchIntentForPackage == null || appInfo3 == null) {
                                                        arrayList5.add(shortcutInfo);
                                                    } else {
                                                        shortcutInfo.promisedIntent = launchIntentForPackage;
                                                        appInfo4 = appInfo3;
                                                    }
                                                }
                                            }
                                            if (appInfo4 != null) {
                                                shortcutInfo.flags = appInfo4.flags;
                                            }
                                            shortcutInfo.intent = shortcutInfo.promisedIntent;
                                            shortcutInfo.promisedIntent = null;
                                            shortcutInfo.status = 0;
                                            z4 = true;
                                            shortcutInfo.updateIcon(this.this$0.mIconCache);
                                        }
                                        boolean z5 = z4;
                                        if (appInfo4 != null) {
                                            z5 = z4;
                                            if ("android.intent.action.MAIN".equals(shortcutInfo.intent.getAction())) {
                                                z5 = z4;
                                                if (shortcutInfo.itemType == 0) {
                                                    shortcutInfo.updateIcon(this.this$0.mIconCache);
                                                    shortcutInfo.title = Utilities.trim(appInfo4.title);
                                                    shortcutInfo.contentDescription = appInfo4.contentDescription;
                                                    z5 = true;
                                                }
                                            }
                                        }
                                        int i4 = shortcutInfo.isDisabled;
                                        shortcutInfo.isDisabled = addFlag.apply(shortcutInfo.isDisabled);
                                        z2 = z5;
                                        z3 = false;
                                        if (shortcutInfo.isDisabled != i4) {
                                            z3 = true;
                                            z2 = z5;
                                        }
                                    }
                                }
                                if (z2 || z3) {
                                    arrayList4.add(shortcutInfo);
                                }
                                if (z2) {
                                    LauncherModel.updateItemInDatabase(context, shortcutInfo);
                                }
                            } else if ((itemInfo instanceof LauncherAppWidgetInfo) && this.mOp == 1) {
                                LauncherAppWidgetInfo launcherAppWidgetInfo = (LauncherAppWidgetInfo) itemInfo;
                                if (this.mUser.equals(launcherAppWidgetInfo.user) && launcherAppWidgetInfo.hasRestoreFlag(2) && matchesAll.matches(launcherAppWidgetInfo.providerName.getPackageName())) {
                                    launcherAppWidgetInfo.restoreStatus &= -11;
                                    launcherAppWidgetInfo.restoreStatus |= 4;
                                    arrayList6.add(launcherAppWidgetInfo);
                                    LauncherModel.updateItemInDatabase(context, launcherAppWidgetInfo);
                                }
                            }
                        }
                    }
                    if (!arrayList4.isEmpty() || !arrayList5.isEmpty()) {
                        this.this$0.mHandler.post(new Runnable(this, this.this$0.getCallback(), arrayList4, arrayList5) { // from class: com.android.launcher3.LauncherModel.PackageUpdatedTask.2
                            final PackageUpdatedTask this$1;
                            final Callbacks val$callbacks;
                            final ArrayList val$removedShortcuts;
                            final ArrayList val$updatedShortcuts;

                            {
                                this.this$1 = this;
                                this.val$callbacks = r5;
                                this.val$updatedShortcuts = arrayList4;
                                this.val$removedShortcuts = arrayList5;
                            }

                            @Override // java.lang.Runnable
                            public void run() {
                                Callbacks callback2 = this.this$1.this$0.getCallback();
                                if (this.val$callbacks != callback2 || callback2 == null) {
                                    return;
                                }
                                this.val$callbacks.bindShortcutsChanged(this.val$updatedShortcuts, this.val$removedShortcuts, this.this$1.mUser);
                            }
                        });
                        if (!arrayList5.isEmpty()) {
                            LauncherModel.deleteItemsFromDatabase(context, arrayList5);
                        }
                    }
                    if (!arrayList6.isEmpty()) {
                        this.this$0.mHandler.post(new Runnable(this, this.this$0.getCallback(), arrayList6) { // from class: com.android.launcher3.LauncherModel.PackageUpdatedTask.3
                            final PackageUpdatedTask this$1;
                            final Callbacks val$callbacks;
                            final ArrayList val$widgets;

                            {
                                this.this$1 = this;
                                this.val$callbacks = r5;
                                this.val$widgets = arrayList6;
                            }

                            @Override // java.lang.Runnable
                            public void run() {
                                Callbacks callback2 = this.this$1.this$0.getCallback();
                                if (this.val$callbacks != callback2 || callback2 == null) {
                                    return;
                                }
                                this.val$callbacks.bindWidgetsRestored(this.val$widgets);
                            }
                        });
                    }
                }
                HashSet<String> hashSet = new HashSet();
                HashSet<ComponentName> hashSet2 = new HashSet();
                if (this.mOp == 3) {
                    Collections.addAll(hashSet, strArr);
                } else if (this.mOp == 2) {
                    for (int i5 = 0; i5 < length; i5++) {
                        if (LauncherModel.isPackageDisabled(context, strArr[i5], this.mUser)) {
                            hashSet.add(strArr[i5]);
                        }
                    }
                    for (AppInfo appInfo5 : arrayList3) {
                        hashSet2.add(appInfo5.componentName);
                    }
                }
                if (!hashSet.isEmpty() || !hashSet2.isEmpty()) {
                    for (String str2 : hashSet) {
                        LauncherModel.deletePackageFromDatabase(context, str2, this.mUser);
                    }
                    for (ComponentName componentName : hashSet2) {
                        LauncherModel.deleteItemsFromDatabase(context, this.this$0.getItemInfoForComponentName(componentName, this.mUser));
                    }
                    InstallShortcutReceiver.removeFromInstallQueue(context, hashSet, this.mUser);
                    this.this$0.mHandler.post(new Runnable(this, this.this$0.getCallback(), hashSet, hashSet2) { // from class: com.android.launcher3.LauncherModel.PackageUpdatedTask.4
                        final PackageUpdatedTask this$1;
                        final Callbacks val$callbacks;
                        final HashSet val$removedComponents;
                        final HashSet val$removedPackages;

                        {
                            this.this$1 = this;
                            this.val$callbacks = r5;
                            this.val$removedPackages = hashSet;
                            this.val$removedComponents = hashSet2;
                        }

                        @Override // java.lang.Runnable
                        public void run() {
                            Callbacks callback2 = this.this$1.this$0.getCallback();
                            if (this.val$callbacks != callback2 || callback2 == null) {
                                return;
                            }
                            this.val$callbacks.bindWorkspaceComponentsRemoved(this.val$removedPackages, this.val$removedComponents, this.this$1.mUser);
                        }
                    });
                }
                if (!arrayList3.isEmpty()) {
                    this.this$0.mHandler.post(new Runnable(this, this.this$0.getCallback(), arrayList3) { // from class: com.android.launcher3.LauncherModel.PackageUpdatedTask.5
                        final PackageUpdatedTask this$1;
                        final Callbacks val$callbacks;
                        final ArrayList val$removedApps;

                        {
                            this.this$1 = this;
                            this.val$callbacks = r5;
                            this.val$removedApps = arrayList3;
                        }

                        @Override // java.lang.Runnable
                        public void run() {
                            Callbacks callback2 = this.this$1.this$0.getCallback();
                            if (this.val$callbacks != callback2 || callback2 == null) {
                                return;
                            }
                            this.val$callbacks.bindAppInfosRemoved(this.val$removedApps);
                        }
                    });
                }
                if (Utilities.ATLEAST_MARSHMALLOW) {
                    return;
                }
                if (this.mOp == 1 || this.mOp == 3 || this.mOp == 2) {
                    this.this$0.mHandler.post(new Runnable(this, this.this$0.getCallback()) { // from class: com.android.launcher3.LauncherModel.PackageUpdatedTask.6
                        final PackageUpdatedTask this$1;
                        final Callbacks val$callbacks;

                        {
                            this.this$1 = this;
                            this.val$callbacks = r5;
                        }

                        @Override // java.lang.Runnable
                        public void run() {
                            Callbacks callback2 = this.this$1.this$0.getCallback();
                            if (this.val$callbacks != callback2 || callback2 == null) {
                                return;
                            }
                            this.val$callbacks.notifyWidgetProvidersChanged();
                        }
                    });
                }
            }
        }
    }

    static {
        sWorkerThread.start();
        sWorker = new Handler(sWorkerThread.getLooper());
        mDeferredBindRunnables = new ArrayList<>();
        mBindCompleteRunnables = new ArrayList<>();
        sBgLock = new Object();
        sBgItemsIdMap = new LongArrayMap<>();
        sBgWorkspaceItems = new ArrayList<>();
        sBgAppWidgets = new ArrayList<>();
        sBgFolders = new LongArrayMap<>();
        sBgWorkspaceScreens = new ArrayList<>();
        sPendingPackages = new HashMap<>();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public LauncherModel(LauncherAppState launcherAppState, IconCache iconCache, AppFilter appFilter) {
        Context context = launcherAppState.getContext();
        String string = context.getString(2131558401);
        String authority = Uri.parse(string).getAuthority();
        ProviderInfo resolveContentProvider = context.getPackageManager().resolveContentProvider("com.android.launcher2.settings", 0);
        ProviderInfo resolveContentProvider2 = context.getPackageManager().resolveContentProvider(authority, 0);
        Log.d("Launcher.Model", "Old launcher provider: " + string);
        boolean z = false;
        if (resolveContentProvider != null) {
            z = false;
            if (resolveContentProvider2 != null) {
                z = true;
            }
        }
        this.mOldContentProviderExists = z;
        if (this.mOldContentProviderExists) {
            Log.d("Launcher.Model", "Old launcher provider exists.");
        } else {
            Log.d("Launcher.Model", "Old launcher provider does not exist.");
        }
        this.mApp = launcherAppState;
        this.mBgAllAppsList = new AllAppsList(iconCache, appFilter);
        this.mBgWidgetsModel = new WidgetsModel(context, iconCache, appFilter);
        this.mIconCache = iconCache;
        this.mLauncherApps = LauncherAppsCompat.getInstance(context);
        this.mUserManager = UserManagerCompat.getInstance(context);
    }

    public static void addItemToDatabase(Context context, ItemInfo itemInfo, long j, long j2, int i, int i2) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher.Model", "addItemToDatabase item = " + itemInfo + ", container = " + j + ", screenId = " + j2 + ", cellX " + i + ", cellY = " + i2);
        }
        itemInfo.container = j;
        itemInfo.cellX = i;
        itemInfo.cellY = i2;
        if ((context instanceof Launcher) && j2 < 0 && j == -101) {
            itemInfo.screenId = ((Launcher) context).getHotseat().getOrderInHotseat(i, i2);
        } else {
            itemInfo.screenId = j2;
        }
        ContentValues contentValues = new ContentValues();
        ContentResolver contentResolver = context.getContentResolver();
        itemInfo.onAddToDatabase(context, contentValues);
        itemInfo.id = LauncherAppState.getLauncherProvider().generateNewItemId();
        contentValues.put("_id", Long.valueOf(itemInfo.id));
        runOnWorkerThread(new Runnable(contentResolver, contentValues, itemInfo, new Throwable().getStackTrace()) { // from class: com.android.launcher3.LauncherModel.9
            final ContentResolver val$cr;
            final ItemInfo val$item;
            final StackTraceElement[] val$stackTrace;
            final ContentValues val$values;

            {
                this.val$cr = contentResolver;
                this.val$values = contentValues;
                this.val$item = itemInfo;
                this.val$stackTrace = r7;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.val$cr.insert(LauncherSettings$Favorites.CONTENT_URI, this.val$values);
                synchronized (LauncherModel.sBgLock) {
                    LauncherModel.checkItemInfoLocked(this.val$item.id, this.val$item, this.val$stackTrace);
                    LauncherModel.sBgItemsIdMap.put(this.val$item.id, this.val$item);
                    if (LauncherLog.DEBUG) {
                        LauncherLog.d("Launcher.Model", "addItemToDatabase sBgItemsIdMap.put = " + this.val$item.id + ", item = " + this.val$item);
                    }
                    switch (this.val$item.itemType) {
                        case 2:
                            LauncherModel.sBgFolders.put(this.val$item.id, (FolderInfo) this.val$item);
                        case PackageInstallerCompat.STATUS_INSTALLED /* 0 */:
                        case 1:
                            if (this.val$item.container != -100 && this.val$item.container != -101) {
                                if (!LauncherModel.sBgFolders.containsKey(this.val$item.container)) {
                                    Log.e("Launcher.Model", "adding item: " + this.val$item + " to a folder that  doesn't exist");
                                    break;
                                }
                            } else {
                                LauncherModel.sBgWorkspaceItems.add(this.val$item);
                                break;
                            }
                            break;
                        case 4:
                            LauncherModel.sBgAppWidgets.add((LauncherAppWidgetInfo) this.val$item);
                            if (LauncherLog.DEBUG) {
                                LauncherLog.d("Launcher.Model", "addItemToDatabase sAppWidgets.add = " + this.val$item);
                                break;
                            }
                            break;
                    }
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void addOrMoveItemInDatabase(Context context, ItemInfo itemInfo, long j, long j2, int i, int i2) {
        if (itemInfo.container == -1) {
            addItemToDatabase(context, itemInfo, j, j2, i, i2);
        } else {
            moveItemInDatabase(context, itemInfo, j, j2, i, i2);
        }
    }

    private void assertWorkspaceLoaded() {
        if (LauncherAppState.isDogfoodBuild()) {
            synchronized (this.mLock) {
                if (!this.mHasLoaderCompletedOnce || (this.mLoaderTask != null && this.mLoaderTask.mIsLoadingAndBindingWorkspace)) {
                    throw new RuntimeException("Trying to add shortcut while loader is running");
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void bindWidgetsModel(Callbacks callbacks, WidgetsModel widgetsModel) {
        this.mHandler.post(new Runnable(this, callbacks, widgetsModel) { // from class: com.android.launcher3.LauncherModel.16
            final LauncherModel this$0;
            final Callbacks val$callbacks;
            final WidgetsModel val$model;

            {
                this.this$0 = this;
                this.val$callbacks = callbacks;
                this.val$model = widgetsModel;
            }

            @Override // java.lang.Runnable
            public void run() {
                Callbacks callback = this.this$0.getCallback();
                if (this.val$callbacks != callback || callback == null) {
                    return;
                }
                this.val$callbacks.bindWidgetsModel(this.val$model);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void checkItemInfo(ItemInfo itemInfo) {
        runOnWorkerThread(new Runnable(itemInfo.id, itemInfo, new Throwable().getStackTrace()) { // from class: com.android.launcher3.LauncherModel.6
            final ItemInfo val$item;
            final long val$itemId;
            final StackTraceElement[] val$stackTrace;

            {
                this.val$itemId = r5;
                this.val$item = itemInfo;
                this.val$stackTrace = r8;
            }

            @Override // java.lang.Runnable
            public void run() {
                synchronized (LauncherModel.sBgLock) {
                    LauncherModel.checkItemInfoLocked(this.val$itemId, this.val$item, this.val$stackTrace);
                }
            }
        });
    }

    static void checkItemInfoLocked(long j, ItemInfo itemInfo, StackTraceElement[] stackTraceElementArr) {
        ItemInfo itemInfo2 = sBgItemsIdMap.get(j);
        if (itemInfo2 == null || itemInfo == itemInfo2) {
            return;
        }
        if ((itemInfo2 instanceof ShortcutInfo) && (itemInfo instanceof ShortcutInfo)) {
            ShortcutInfo shortcutInfo = (ShortcutInfo) itemInfo2;
            ShortcutInfo shortcutInfo2 = (ShortcutInfo) itemInfo;
            if (shortcutInfo.title.toString().equals(shortcutInfo2.title.toString()) && shortcutInfo.intent.filterEquals(shortcutInfo2.intent) && shortcutInfo.id == shortcutInfo2.id && shortcutInfo.itemType == shortcutInfo2.itemType && shortcutInfo.container == shortcutInfo2.container && shortcutInfo.screenId == shortcutInfo2.screenId && shortcutInfo.cellX == shortcutInfo2.cellX && shortcutInfo.cellY == shortcutInfo2.cellY && shortcutInfo.spanX == shortcutInfo2.spanX && shortcutInfo.spanY == shortcutInfo2.spanY) {
                if (shortcutInfo.dropPos == null && shortcutInfo2.dropPos == null) {
                    return;
                }
                if (shortcutInfo.dropPos != null && shortcutInfo2.dropPos != null && shortcutInfo.dropPos[0] == shortcutInfo2.dropPos[0] && shortcutInfo.dropPos[1] == shortcutInfo2.dropPos[1]) {
                    return;
                }
            }
        }
        RuntimeException runtimeException = new RuntimeException("item: " + (itemInfo != null ? itemInfo.toString() : "null") + "modelItem: " + (itemInfo2 != null ? itemInfo2.toString() : "null") + "Error: ItemInfo passed to checkItemInfo doesn't match original");
        if (stackTraceElementArr != null) {
            runtimeException.setStackTrace(stackTraceElementArr);
        }
        throw runtimeException;
    }

    public static void deleteFolderAndContentsFromDatabase(Context context, FolderInfo folderInfo) {
        runOnWorkerThread(new Runnable(context.getContentResolver(), folderInfo) { // from class: com.android.launcher3.LauncherModel.13
            final ContentResolver val$cr;
            final FolderInfo val$info;

            {
                this.val$cr = r4;
                this.val$info = folderInfo;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.val$cr.delete(LauncherSettings$Favorites.getContentUri(this.val$info.id), null, null);
                synchronized (LauncherModel.sBgLock) {
                    LauncherModel.sBgItemsIdMap.remove(this.val$info.id);
                    LauncherModel.sBgFolders.remove(this.val$info.id);
                    LauncherModel.sBgWorkspaceItems.remove(this.val$info);
                    if (LauncherLog.DEBUG) {
                        LauncherLog.d("Launcher.Model", "deleteFolderContentsFromDatabase sBgItemsIdMap.remove = " + this.val$info.id);
                    }
                }
                this.val$cr.delete(LauncherSettings$Favorites.CONTENT_URI, "container=" + this.val$info.id, null);
                synchronized (LauncherModel.sBgLock) {
                    for (ShortcutInfo shortcutInfo : this.val$info.contents) {
                        LauncherModel.sBgItemsIdMap.remove(shortcutInfo.id);
                    }
                }
            }
        });
    }

    public static void deleteItemFromDatabase(Context context, ItemInfo itemInfo) {
        ArrayList arrayList = new ArrayList();
        arrayList.add(itemInfo);
        deleteItemsFromDatabase(context, arrayList);
    }

    static void deleteItemsFromDatabase(Context context, ArrayList<? extends ItemInfo> arrayList) {
        runOnWorkerThread(new Runnable(arrayList, context.getContentResolver()) { // from class: com.android.launcher3.LauncherModel.11
            final ContentResolver val$cr;
            final ArrayList val$items;

            {
                this.val$items = arrayList;
                this.val$cr = r5;
            }

            @Override // java.lang.Runnable
            public void run() {
                for (ItemInfo itemInfo : this.val$items) {
                    this.val$cr.delete(LauncherSettings$Favorites.getContentUri(itemInfo.id), null, null);
                    synchronized (LauncherModel.sBgLock) {
                        switch (itemInfo.itemType) {
                            case PackageInstallerCompat.STATUS_INSTALLED /* 0 */:
                            case 1:
                                LauncherModel.sBgWorkspaceItems.remove(itemInfo);
                                break;
                            case 2:
                                LauncherModel.sBgFolders.remove(itemInfo.id);
                                for (ItemInfo itemInfo2 : LauncherModel.sBgItemsIdMap) {
                                    if (itemInfo2.container == itemInfo.id) {
                                        Log.e("Launcher.Model", "deleting a folder (" + itemInfo + ") which still contains items (" + itemInfo2 + ")");
                                    }
                                }
                                LauncherModel.sBgWorkspaceItems.remove(itemInfo);
                                break;
                            case 4:
                                LauncherModel.sBgAppWidgets.remove((LauncherAppWidgetInfo) itemInfo);
                                break;
                        }
                        LauncherModel.sBgItemsIdMap.remove(itemInfo.id);
                    }
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void deletePackageFromDatabase(Context context, String str, UserHandleCompat userHandleCompat) {
        deleteItemsFromDatabase(context, getItemsByPackageName(str, userHandleCompat));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static ArrayList<ItemInfo> filterItemInfos(Iterable<ItemInfo> iterable, ItemInfoFilter itemInfoFilter) {
        LauncherAppWidgetInfo launcherAppWidgetInfo;
        ComponentName componentName;
        HashSet hashSet = new HashSet();
        for (ItemInfo itemInfo : iterable) {
            if (itemInfo instanceof ShortcutInfo) {
                ShortcutInfo shortcutInfo = (ShortcutInfo) itemInfo;
                ComponentName targetComponent = shortcutInfo.getTargetComponent();
                if (targetComponent != null && itemInfoFilter.filterItem(null, shortcutInfo, targetComponent)) {
                    hashSet.add(shortcutInfo);
                }
            } else if (itemInfo instanceof FolderInfo) {
                FolderInfo folderInfo = (FolderInfo) itemInfo;
                for (ShortcutInfo shortcutInfo2 : folderInfo.contents) {
                    ComponentName targetComponent2 = shortcutInfo2.getTargetComponent();
                    if (targetComponent2 != null && itemInfoFilter.filterItem(folderInfo, shortcutInfo2, targetComponent2)) {
                        hashSet.add(shortcutInfo2);
                    }
                }
            } else if ((itemInfo instanceof LauncherAppWidgetInfo) && (componentName = (launcherAppWidgetInfo = (LauncherAppWidgetInfo) itemInfo).providerName) != null && itemInfoFilter.filterItem(null, launcherAppWidgetInfo, componentName)) {
                hashSet.add(launcherAppWidgetInfo);
            }
        }
        return new ArrayList<>(hashSet);
    }

    private static boolean findNextAvailableIconSpaceInScreen(ArrayList<ItemInfo> arrayList, int[] iArr, int i, int i2) {
        InvariantDeviceProfile invariantDeviceProfile = LauncherAppState.getInstance().getInvariantDeviceProfile();
        int i3 = invariantDeviceProfile.numColumns;
        int i4 = invariantDeviceProfile.numRows;
        boolean[][] zArr = new boolean[i3][i4];
        if (arrayList != null) {
            for (ItemInfo itemInfo : arrayList) {
                int i5 = itemInfo.cellX;
                int i6 = itemInfo.spanX;
                int i7 = itemInfo.cellY;
                int i8 = itemInfo.spanY;
                for (int i9 = itemInfo.cellX; i9 >= 0 && i9 < i5 + i6 && i9 < i3; i9++) {
                    for (int i10 = itemInfo.cellY; i10 >= 0 && i10 < i7 + i8 && i10 < i4; i10++) {
                        zArr[i9][i10] = true;
                    }
                }
            }
        }
        return Utilities.findVacantCell(iArr, i, i2, i3, i4, zArr);
    }

    static FolderInfo findOrMakeFolder(LongArrayMap<FolderInfo> longArrayMap, long j) {
        FolderInfo folderInfo = longArrayMap.get(j);
        FolderInfo folderInfo2 = folderInfo;
        if (folderInfo == null) {
            folderInfo2 = new FolderInfo();
            longArrayMap.put(j, folderInfo2);
        }
        return folderInfo2;
    }

    private static ArrayList<ItemInfo> getItemsByPackageName(String str, UserHandleCompat userHandleCompat) {
        return filterItemInfos(sBgItemsIdMap, new ItemInfoFilter(str, userHandleCompat) { // from class: com.android.launcher3.LauncherModel.10
            final String val$pn;
            final UserHandleCompat val$user;

            {
                this.val$pn = str;
                this.val$user = userHandleCompat;
            }

            @Override // com.android.launcher3.LauncherModel.ItemInfoFilter
            public boolean filterItem(ItemInfo itemInfo, ItemInfo itemInfo2, ComponentName componentName) {
                return componentName.getPackageName().equals(this.val$pn) ? itemInfo2.user.equals(this.val$user) : false;
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static Intent getMarketIntent(String str) {
        return new Intent("android.intent.action.VIEW").setData(new Uri.Builder().scheme("market").authority("details").appendQueryParameter("id", str).build());
    }

    public static Looper getWorkerLooper() {
        return sWorkerThread.getLooper();
    }

    static boolean isPackageDisabled(Context context, String str, UserHandleCompat userHandleCompat) {
        return !LauncherAppsCompat.getInstance(context).isPackageEnabledForProfile(str, userHandleCompat);
    }

    public static boolean isValidPackage(Context context, String str, UserHandleCompat userHandleCompat) {
        if (str == null) {
            return false;
        }
        return LauncherAppsCompat.getInstance(context).isPackageEnabledForProfile(str, userHandleCompat);
    }

    public static boolean isValidPackageActivity(Context context, ComponentName componentName, UserHandleCompat userHandleCompat) {
        if (componentName == null) {
            return false;
        }
        LauncherAppsCompat launcherAppsCompat = LauncherAppsCompat.getInstance(context);
        if (launcherAppsCompat.isPackageEnabledForProfile(componentName.getPackageName(), userHandleCompat)) {
            return launcherAppsCompat.isActivityEnabledForProfile(componentName, userHandleCompat);
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static boolean isValidProvider(AppWidgetProviderInfo appWidgetProviderInfo) {
        boolean z = false;
        if (appWidgetProviderInfo != null) {
            z = false;
            if (appWidgetProviderInfo.provider != null) {
                z = false;
                if (appWidgetProviderInfo.provider.getPackageName() != null) {
                    z = true;
                }
            }
        }
        return z;
    }

    public static ArrayList<Long> loadWorkspaceScreensDb(Context context) {
        Cursor query = context.getContentResolver().query(LauncherSettings$WorkspaceScreens.CONTENT_URI, null, null, null, "screenRank");
        ArrayList<Long> arrayList = new ArrayList<>();
        try {
            int columnIndexOrThrow = query.getColumnIndexOrThrow("_id");
            while (query.moveToNext()) {
                try {
                    arrayList.add(Long.valueOf(query.getLong(columnIndexOrThrow)));
                } catch (Exception e) {
                    Launcher.addDumpLog("Launcher.Model", "Desktop items loading interrupted - invalid screens: " + e, true);
                }
            }
            return arrayList;
        } finally {
            query.close();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void modifyItemInDatabase(Context context, ItemInfo itemInfo, long j, long j2, int i, int i2, int i3, int i4) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher.Model", "modifyItemInDatabase: item = " + itemInfo + ", container = " + j + ", screenId = " + j2 + ", cellX = " + i + ", cellY = " + i2 + ", spanX = " + i3 + ", spanY = " + i4);
        }
        itemInfo.container = j;
        itemInfo.cellX = i;
        itemInfo.cellY = i2;
        itemInfo.spanX = i3;
        itemInfo.spanY = i4;
        if ((context instanceof Launcher) && j2 < 0 && j == -101) {
            itemInfo.screenId = ((Launcher) context).getHotseat().getOrderInHotseat(i, i2);
        } else {
            itemInfo.screenId = j2;
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put("container", Long.valueOf(itemInfo.container));
        contentValues.put("cellX", Integer.valueOf(itemInfo.cellX));
        contentValues.put("cellY", Integer.valueOf(itemInfo.cellY));
        contentValues.put("rank", Integer.valueOf(itemInfo.rank));
        contentValues.put("spanX", Integer.valueOf(itemInfo.spanX));
        contentValues.put("spanY", Integer.valueOf(itemInfo.spanY));
        contentValues.put("screen", Long.valueOf(itemInfo.screenId));
        updateItemInDatabaseHelper(context, contentValues, itemInfo, "modifyItemInDatabase");
    }

    public static void moveItemInDatabase(Context context, ItemInfo itemInfo, long j, long j2, int i, int i2) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher.Model", "moveItemInDatabase: item = " + itemInfo + ", container = " + j + ", screenId = " + j2 + ", cellX = " + i + ", cellY = " + i2 + ", context = " + context);
        }
        itemInfo.container = j;
        itemInfo.cellX = i;
        itemInfo.cellY = i2;
        if ((context instanceof Launcher) && j2 < 0 && j == -101) {
            itemInfo.screenId = ((Launcher) context).getHotseat().getOrderInHotseat(i, i2);
        } else {
            itemInfo.screenId = j2;
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put("container", Long.valueOf(itemInfo.container));
        contentValues.put("cellX", Integer.valueOf(itemInfo.cellX));
        contentValues.put("cellY", Integer.valueOf(itemInfo.cellY));
        contentValues.put("rank", Integer.valueOf(itemInfo.rank));
        contentValues.put("screen", Long.valueOf(itemInfo.screenId));
        updateItemInDatabaseHelper(context, contentValues, itemInfo, "moveItemInDatabase");
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void moveItemsInDatabase(Context context, ArrayList<ItemInfo> arrayList, long j, int i) {
        ArrayList arrayList2 = new ArrayList();
        int size = arrayList.size();
        for (int i2 = 0; i2 < size; i2++) {
            ItemInfo itemInfo = arrayList.get(i2);
            itemInfo.container = j;
            if ((context instanceof Launcher) && i < 0 && j == -101) {
                itemInfo.screenId = ((Launcher) context).getHotseat().getOrderInHotseat(itemInfo.cellX, itemInfo.cellY);
            } else {
                itemInfo.screenId = i;
            }
            ContentValues contentValues = new ContentValues();
            contentValues.put("container", Long.valueOf(itemInfo.container));
            contentValues.put("cellX", Integer.valueOf(itemInfo.cellX));
            contentValues.put("cellY", Integer.valueOf(itemInfo.cellY));
            contentValues.put("rank", Integer.valueOf(itemInfo.rank));
            contentValues.put("screen", Long.valueOf(itemInfo.screenId));
            arrayList2.add(contentValues);
        }
        updateItemsInDatabaseHelper(context, arrayList2, arrayList, "moveItemInDatabase");
    }

    static void runOnWorkerThread(Runnable runnable) {
        if (sWorkerThread.getThreadId() == Process.myTid()) {
            runnable.run();
        } else {
            sWorker.post(runnable);
        }
    }

    private void stopLoaderLocked() {
        LoaderTask loaderTask = this.mLoaderTask;
        if (loaderTask != null) {
            loaderTask.stopLocked();
        }
    }

    private void unbindItemInfosAndClearQueuedBindRunnables() {
        if (sWorkerThread.getThreadId() == Process.myTid()) {
            throw new RuntimeException("Expected unbindLauncherItemInfos() to be called from the main thread");
        }
        synchronized (mDeferredBindRunnables) {
            mDeferredBindRunnables.clear();
        }
        this.mHandler.cancelAll();
        unbindWorkspaceItemsOnMainThread();
    }

    static void updateItemArrays(ItemInfo itemInfo, long j, StackTraceElement[] stackTraceElementArr) {
        synchronized (sBgLock) {
            checkItemInfoLocked(j, itemInfo, stackTraceElementArr);
            if (itemInfo.container != -100 && itemInfo.container != -101 && !sBgFolders.containsKey(itemInfo.container)) {
                Log.e("Launcher.Model", "item: " + itemInfo + " container being set to: " + itemInfo.container + ", not in the list of folders");
            }
            ItemInfo itemInfo2 = sBgItemsIdMap.get(j);
            if (itemInfo2 != null && (itemInfo2.container == -100 || itemInfo2.container == -101)) {
                switch (itemInfo2.itemType) {
                    case PackageInstallerCompat.STATUS_INSTALLED /* 0 */:
                    case 1:
                    case 2:
                        if (!sBgWorkspaceItems.contains(itemInfo2)) {
                            sBgWorkspaceItems.add(itemInfo2);
                            break;
                        }
                        break;
                }
            } else {
                sBgWorkspaceItems.remove(itemInfo2);
            }
        }
    }

    public static void updateItemInDatabase(Context context, ItemInfo itemInfo) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher.Model", "updateItemInDatabase: item = " + itemInfo);
        }
        ContentValues contentValues = new ContentValues();
        itemInfo.onAddToDatabase(context, contentValues);
        updateItemInDatabaseHelper(context, contentValues, itemInfo, "updateItemInDatabase");
    }

    static void updateItemInDatabaseHelper(Context context, ContentValues contentValues, ItemInfo itemInfo, String str) {
        long j = itemInfo.id;
        Uri contentUri = LauncherSettings$Favorites.getContentUri(j);
        ContentResolver contentResolver = context.getContentResolver();
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher.Model", "updateItemInDatabaseHelper values = " + contentValues + ", item = " + itemInfo);
        }
        runOnWorkerThread(new Runnable(contentResolver, contentUri, contentValues, itemInfo, j, new Throwable().getStackTrace()) { // from class: com.android.launcher3.LauncherModel.7
            final ContentResolver val$cr;
            final ItemInfo val$item;
            final long val$itemId;
            final StackTraceElement[] val$stackTrace;
            final Uri val$uri;
            final ContentValues val$values;

            {
                this.val$cr = contentResolver;
                this.val$uri = contentUri;
                this.val$values = contentValues;
                this.val$item = itemInfo;
                this.val$itemId = j;
                this.val$stackTrace = r11;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.val$cr.update(this.val$uri, this.val$values, null, null);
                LauncherModel.updateItemArrays(this.val$item, this.val$itemId, this.val$stackTrace);
            }
        });
    }

    static void updateItemsInDatabaseHelper(Context context, ArrayList<ContentValues> arrayList, ArrayList<ItemInfo> arrayList2, String str) {
        runOnWorkerThread(new Runnable(arrayList2, arrayList, new Throwable().getStackTrace(), context.getContentResolver()) { // from class: com.android.launcher3.LauncherModel.8
            final ContentResolver val$cr;
            final ArrayList val$items;
            final StackTraceElement[] val$stackTrace;
            final ArrayList val$valuesList;

            {
                this.val$items = arrayList2;
                this.val$valuesList = arrayList;
                this.val$stackTrace = r6;
                this.val$cr = r7;
            }

            @Override // java.lang.Runnable
            public void run() {
                ArrayList<ContentProviderOperation> arrayList3 = new ArrayList<>();
                int size = this.val$items.size();
                for (int i = 0; i < size; i++) {
                    ItemInfo itemInfo = (ItemInfo) this.val$items.get(i);
                    long j = itemInfo.id;
                    arrayList3.add(ContentProviderOperation.newUpdate(LauncherSettings$Favorites.getContentUri(j)).withValues((ContentValues) this.val$valuesList.get(i)).build());
                    LauncherModel.updateItemArrays(itemInfo, j, this.val$stackTrace);
                }
                try {
                    this.val$cr.applyBatch(LauncherProvider.AUTHORITY, arrayList3);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void addAndBindAddedWorkspaceItems(Context context, ArrayList<? extends ItemInfo> arrayList) {
        Callbacks callback = getCallback();
        if (arrayList.isEmpty()) {
            return;
        }
        runOnWorkerThread(new AnonymousClass4(this, context, arrayList, callback));
    }

    public void addAppsToAllApps(Context context, ArrayList<AppInfo> arrayList) {
        Callbacks callback = getCallback();
        if (arrayList == null) {
            throw new RuntimeException("allAppsApps must not be null");
        }
        if (arrayList.isEmpty()) {
            return;
        }
        runOnWorkerThread(new AnonymousClass3(this, arrayList, callback));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void bindRemainingSynchronousPages() {
        Runnable[] runnableArr;
        if (mDeferredBindRunnables.isEmpty()) {
            return;
        }
        synchronized (mDeferredBindRunnables) {
            runnableArr = (Runnable[]) mDeferredBindRunnables.toArray(new Runnable[mDeferredBindRunnables.size()]);
            mDeferredBindRunnables.clear();
        }
        for (Runnable runnable : runnableArr) {
            this.mHandler.post(runnable);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean canMigrateFromOldLauncherDb(Launcher launcher) {
        boolean z = false;
        if (this.mOldContentProviderExists) {
            z = !launcher.isLauncherPreinstalled();
        }
        return z;
    }

    public void dumpState() {
        Log.d("Launcher.Model", "mCallbacks=" + this.mCallbacks);
        AppInfo.dumpApplicationInfoList("Launcher.Model", "mAllAppsList.data", this.mBgAllAppsList.data);
        AppInfo.dumpApplicationInfoList("Launcher.Model", "mAllAppsList.added", this.mBgAllAppsList.added);
        AppInfo.dumpApplicationInfoList("Launcher.Model", "mAllAppsList.removed", this.mBgAllAppsList.removed);
        AppInfo.dumpApplicationInfoList("Launcher.Model", "mAllAppsList.modified", this.mBgAllAppsList.modified);
        if (this.mLoaderTask != null) {
            this.mLoaderTask.dumpState();
        } else {
            Log.d("Launcher.Model", "mLoaderTask=null");
        }
    }

    void enqueuePackageUpdated(PackageUpdatedTask packageUpdatedTask) {
        sWorker.post(packageUpdatedTask);
    }

    public FolderInfo findFolderById(Long l) {
        FolderInfo folderInfo;
        synchronized (sBgLock) {
            folderInfo = sBgFolders.get(l.longValue());
        }
        return folderInfo;
    }

    Pair<Long, int[]> findSpaceForItem(Context context, ArrayList<Long> arrayList, ArrayList<Long> arrayList2, int i, int i2) {
        LongSparseArray longSparseArray = new LongSparseArray();
        assertWorkspaceLoaded();
        synchronized (sBgLock) {
            for (ItemInfo itemInfo : sBgItemsIdMap) {
                if (itemInfo.container == -100) {
                    ArrayList arrayList3 = (ArrayList) longSparseArray.get(itemInfo.screenId);
                    ArrayList arrayList4 = arrayList3;
                    if (arrayList3 == null) {
                        arrayList4 = new ArrayList();
                        longSparseArray.put(itemInfo.screenId, arrayList4);
                    }
                    arrayList4.add(itemInfo);
                }
            }
        }
        long j = 0;
        int[] iArr = new int[2];
        boolean z = false;
        int size = arrayList.size();
        int i3 = arrayList.isEmpty() ? 0 : 1;
        if (i3 < size) {
            j = arrayList.get(i3).longValue();
            z = findNextAvailableIconSpaceInScreen((ArrayList) longSparseArray.get(j), iArr, i, i2);
        }
        boolean z2 = z;
        long j2 = j;
        if (!z) {
            int i4 = 1;
            while (true) {
                z2 = z;
                j2 = j;
                if (i4 >= size) {
                    break;
                }
                j = arrayList.get(i4).longValue();
                if (findNextAvailableIconSpaceInScreen((ArrayList) longSparseArray.get(j), iArr, i, i2)) {
                    z2 = true;
                    j2 = j;
                    break;
                }
                i4++;
            }
        }
        if (!z2) {
            long generateNewScreenId = LauncherAppState.getLauncherProvider().generateNewScreenId();
            arrayList.add(Long.valueOf(generateNewScreenId));
            arrayList2.add(Long.valueOf(generateNewScreenId));
            j2 = generateNewScreenId;
            if (!findNextAvailableIconSpaceInScreen((ArrayList) longSparseArray.get(generateNewScreenId), iArr, i, i2)) {
                throw new RuntimeException("Can't find space to add the item");
            }
        }
        return Pair.create(Long.valueOf(j2), iArr);
    }

    void forceReload() {
        resetLoadedState(true, true);
        startLoaderFromBackground();
    }

    public ShortcutInfo getAppShortcutInfo(Intent intent, UserHandleCompat userHandleCompat, Context context, Cursor cursor, int i, int i2, boolean z, boolean z2) {
        if (userHandleCompat == null) {
            Log.d("Launcher.Model", "Null user found in getShortcutInfo");
            return null;
        }
        ComponentName component = intent.getComponent();
        if (component == null) {
            Log.d("Launcher.Model", "Missing component found in getShortcutInfo");
            return null;
        }
        Intent intent2 = new Intent(intent.getAction(), (Uri) null);
        intent2.addCategory("android.intent.category.LAUNCHER");
        intent2.setComponent(component);
        LauncherActivityInfoCompat resolveActivity = this.mLauncherApps.resolveActivity(intent2, userHandleCompat);
        if (resolveActivity == null && !z) {
            Log.d("Launcher.Model", "Missing activity found in getShortcutInfo: " + component);
            return null;
        }
        ShortcutInfo shortcutInfo = new ShortcutInfo();
        this.mIconCache.getTitleAndIcon(shortcutInfo, component, resolveActivity, userHandleCompat, false, z2);
        if (this.mIconCache.isDefaultIcon(shortcutInfo.getIcon(this.mIconCache), userHandleCompat) && cursor != null) {
            Bitmap createIconBitmap = Utilities.createIconBitmap(cursor, i, context);
            Bitmap bitmap = createIconBitmap;
            if (createIconBitmap == null) {
                bitmap = this.mIconCache.getDefaultIcon(userHandleCompat);
            }
            shortcutInfo.setIcon(bitmap);
        }
        if (TextUtils.isEmpty(shortcutInfo.title) && resolveActivity != null) {
            shortcutInfo.title = resolveActivity.getLabel();
        }
        if (resolveActivity != null && PackageManagerHelper.isAppSuspended(resolveActivity.getApplicationInfo())) {
            shortcutInfo.isDisabled = 4;
        }
        if (TextUtils.isEmpty(shortcutInfo.title) && cursor != null) {
            shortcutInfo.title = Utilities.trim(cursor.getString(i2));
        }
        if (shortcutInfo.title == null) {
            shortcutInfo.title = component.getClassName();
        }
        shortcutInfo.itemType = 0;
        shortcutInfo.user = userHandleCompat;
        shortcutInfo.contentDescription = this.mUserManager.getBadgedLabelForUser(shortcutInfo.title, shortcutInfo.user);
        if (resolveActivity != null) {
            shortcutInfo.flags = AppInfo.initFlags(resolveActivity);
        }
        return shortcutInfo;
    }

    public Callbacks getCallback() {
        Callbacks callbacks = null;
        if (this.mCallbacks != null) {
            callbacks = this.mCallbacks.get();
        }
        return callbacks;
    }

    ArrayList<ItemInfo> getItemInfoForComponentName(ComponentName componentName, UserHandleCompat userHandleCompat) {
        return filterItemInfos(sBgItemsIdMap, new ItemInfoFilter(this, componentName, userHandleCompat) { // from class: com.android.launcher3.LauncherModel.18
            final LauncherModel this$0;
            final ComponentName val$cname;
            final UserHandleCompat val$user;

            {
                this.this$0 = this;
                this.val$cname = componentName;
                this.val$user = userHandleCompat;
            }

            @Override // com.android.launcher3.LauncherModel.ItemInfoFilter
            public boolean filterItem(ItemInfo itemInfo, ItemInfo itemInfo2, ComponentName componentName2) {
                if (itemInfo2.user == null) {
                    return componentName2.equals(this.val$cname);
                }
                return componentName2.equals(this.val$cname) ? itemInfo2.user.equals(this.val$user) : false;
            }
        });
    }

    public ShortcutInfo getRestoredItemInfo(Cursor cursor, int i, Intent intent, int i2, int i3, CursorIconInfo cursorIconInfo, Context context) {
        ShortcutInfo shortcutInfo = new ShortcutInfo();
        shortcutInfo.user = UserHandleCompat.myUserHandle();
        Bitmap loadIcon = cursorIconInfo.loadIcon(cursor, shortcutInfo, context);
        if (loadIcon == null) {
            this.mIconCache.getTitleAndIcon(shortcutInfo, intent, shortcutInfo.user, false);
        } else {
            shortcutInfo.setIcon(loadIcon);
        }
        if ((i2 & 1) != 0) {
            String str = null;
            if (cursor != null) {
                str = cursor.getString(i);
            }
            if (!TextUtils.isEmpty(str)) {
                shortcutInfo.title = Utilities.trim(str);
            }
        } else if ((i2 & 2) == 0) {
            throw new InvalidParameterException("Invalid restoreType " + i2);
        } else {
            if (TextUtils.isEmpty(shortcutInfo.title)) {
                shortcutInfo.title = cursor != null ? Utilities.trim(cursor.getString(i)) : "";
            }
        }
        shortcutInfo.contentDescription = this.mUserManager.getBadgedLabelForUser(shortcutInfo.title, shortcutInfo.user);
        shortcutInfo.itemType = i3;
        shortcutInfo.promisedIntent = intent;
        shortcutInfo.status = i2;
        return shortcutInfo;
    }

    Intent getRestoredItemIntent(Cursor cursor, Context context, Intent intent) {
        return getMarketIntent(intent.getComponent().getPackageName());
    }

    ShortcutInfo getShortcutInfo(Cursor cursor, Context context, int i, CursorIconInfo cursorIconInfo) {
        ShortcutInfo shortcutInfo = new ShortcutInfo();
        shortcutInfo.user = UserHandleCompat.myUserHandle();
        shortcutInfo.itemType = 1;
        shortcutInfo.title = Utilities.trim(cursor.getString(i));
        Bitmap loadIcon = cursorIconInfo.loadIcon(cursor, shortcutInfo, context);
        Bitmap bitmap = loadIcon;
        if (loadIcon == null) {
            bitmap = this.mIconCache.getDefaultIcon(shortcutInfo.user);
            shortcutInfo.usingFallbackIcon = true;
        }
        shortcutInfo.setIcon(bitmap);
        return shortcutInfo;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public ShortcutInfo infoFromShortcutIntent(Context context, Intent intent) {
        boolean z;
        Bitmap bitmap;
        Intent intent2 = (Intent) intent.getParcelableExtra("android.intent.extra.shortcut.INTENT");
        String stringExtra = intent.getStringExtra("android.intent.extra.shortcut.NAME");
        Parcelable parcelableExtra = intent.getParcelableExtra("android.intent.extra.shortcut.ICON");
        if (intent2 == null) {
            Log.e("Launcher.Model", "Can't construct ShorcutInfo with null intent");
            return null;
        }
        Intent.ShortcutIconResource shortcutIconResource = null;
        if (parcelableExtra instanceof Bitmap) {
            bitmap = Utilities.createIconBitmap((Bitmap) parcelableExtra, context);
            z = true;
        } else {
            Parcelable parcelableExtra2 = intent.getParcelableExtra("android.intent.extra.shortcut.ICON_RESOURCE");
            z = false;
            bitmap = null;
            if (parcelableExtra2 instanceof Intent.ShortcutIconResource) {
                shortcutIconResource = (Intent.ShortcutIconResource) parcelableExtra2;
                bitmap = Utilities.createIconBitmap(shortcutIconResource.packageName, shortcutIconResource.resourceName, context);
                z = false;
            }
        }
        ShortcutInfo shortcutInfo = new ShortcutInfo();
        shortcutInfo.user = UserHandleCompat.myUserHandle();
        Bitmap bitmap2 = bitmap;
        if (bitmap == null) {
            bitmap2 = this.mIconCache.getDefaultIcon(shortcutInfo.user);
            shortcutInfo.usingFallbackIcon = true;
        }
        shortcutInfo.setIcon(bitmap2);
        shortcutInfo.title = Utilities.trim(stringExtra);
        shortcutInfo.contentDescription = this.mUserManager.getBadgedLabelForUser(shortcutInfo.title, shortcutInfo.user);
        shortcutInfo.intent = intent2;
        shortcutInfo.customIcon = z;
        shortcutInfo.iconResource = shortcutIconResource;
        return shortcutInfo;
    }

    public void initialize(Callbacks callbacks) {
        synchronized (this.mLock) {
            unbindItemInfosAndClearQueuedBindRunnables();
            this.mCallbacks = new WeakReference<>(callbacks);
        }
    }

    public boolean isCurrentCallbacks(Callbacks callbacks) {
        boolean z = false;
        if (this.mCallbacks != null) {
            z = false;
            if (this.mCallbacks.get() == callbacks) {
                z = true;
            }
        }
        return z;
    }

    @Override // com.android.launcher3.compat.LauncherAppsCompat.OnAppsChangedCallbackCompat
    public void onPackageAdded(String str, UserHandleCompat userHandleCompat) {
        enqueuePackageUpdated(new PackageUpdatedTask(this, 1, new String[]{str}, userHandleCompat));
    }

    @Override // com.android.launcher3.compat.LauncherAppsCompat.OnAppsChangedCallbackCompat
    public void onPackageChanged(String str, UserHandleCompat userHandleCompat) {
        enqueuePackageUpdated(new PackageUpdatedTask(this, 2, new String[]{str}, userHandleCompat));
    }

    public void onPackageIconsUpdated(HashSet<String> hashSet, UserHandleCompat userHandleCompat) {
        ShortcutInfo shortcutInfo;
        ComponentName targetComponent;
        Callbacks callback = getCallback();
        ArrayList<AppInfo> arrayList = new ArrayList<>();
        ArrayList arrayList2 = new ArrayList();
        synchronized (sBgLock) {
            for (ItemInfo itemInfo : sBgItemsIdMap) {
                if ((itemInfo instanceof ShortcutInfo) && userHandleCompat.equals(itemInfo.user) && itemInfo.itemType == 0 && (targetComponent = (shortcutInfo = (ShortcutInfo) itemInfo).getTargetComponent()) != null && hashSet.contains(targetComponent.getPackageName())) {
                    shortcutInfo.updateIcon(this.mIconCache);
                    arrayList2.add(shortcutInfo);
                }
            }
            this.mBgAllAppsList.updateIconsAndLabels(hashSet, userHandleCompat, arrayList);
        }
        if (!arrayList2.isEmpty()) {
            this.mHandler.post(new Runnable(this, callback, arrayList2, userHandleCompat) { // from class: com.android.launcher3.LauncherModel.14
                final LauncherModel this$0;
                final Callbacks val$callbacks;
                final ArrayList val$updatedShortcuts;
                final UserHandleCompat val$userFinal;

                {
                    this.this$0 = this;
                    this.val$callbacks = callback;
                    this.val$updatedShortcuts = arrayList2;
                    this.val$userFinal = userHandleCompat;
                }

                @Override // java.lang.Runnable
                public void run() {
                    Callbacks callback2 = this.this$0.getCallback();
                    if (callback2 == null || this.val$callbacks != callback2) {
                        return;
                    }
                    callback2.bindShortcutsChanged(this.val$updatedShortcuts, new ArrayList<>(), this.val$userFinal);
                }
            });
        }
        if (arrayList.isEmpty()) {
            return;
        }
        this.mHandler.post(new Runnable(this, callback, arrayList) { // from class: com.android.launcher3.LauncherModel.15
            final LauncherModel this$0;
            final Callbacks val$callbacks;
            final ArrayList val$updatedApps;

            {
                this.this$0 = this;
                this.val$callbacks = callback;
                this.val$updatedApps = arrayList;
            }

            @Override // java.lang.Runnable
            public void run() {
                Callbacks callback2 = this.this$0.getCallback();
                if (callback2 == null || this.val$callbacks != callback2) {
                    return;
                }
                callback2.bindAppsUpdated(this.val$updatedApps);
            }
        });
    }

    @Override // com.android.launcher3.compat.LauncherAppsCompat.OnAppsChangedCallbackCompat
    public void onPackageRemoved(String str, UserHandleCompat userHandleCompat) {
        enqueuePackageUpdated(new PackageUpdatedTask(this, 3, new String[]{str}, userHandleCompat));
    }

    @Override // com.android.launcher3.compat.LauncherAppsCompat.OnAppsChangedCallbackCompat
    public void onPackagesAvailable(String[] strArr, UserHandleCompat userHandleCompat, boolean z) {
        enqueuePackageUpdated(new PackageUpdatedTask(this, 2, strArr, userHandleCompat));
    }

    @Override // com.android.launcher3.compat.LauncherAppsCompat.OnAppsChangedCallbackCompat
    public void onPackagesSuspended(String[] strArr, UserHandleCompat userHandleCompat) {
        enqueuePackageUpdated(new PackageUpdatedTask(this, 5, strArr, userHandleCompat));
    }

    @Override // com.android.launcher3.compat.LauncherAppsCompat.OnAppsChangedCallbackCompat
    public void onPackagesUnavailable(String[] strArr, UserHandleCompat userHandleCompat, boolean z) {
        if (z) {
            return;
        }
        enqueuePackageUpdated(new PackageUpdatedTask(this, 4, strArr, userHandleCompat));
    }

    @Override // com.android.launcher3.compat.LauncherAppsCompat.OnAppsChangedCallbackCompat
    public void onPackagesUnsuspended(String[] strArr, UserHandleCompat userHandleCompat) {
        enqueuePackageUpdated(new PackageUpdatedTask(this, 6, strArr, userHandleCompat));
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        UserHandleCompat fromIntent;
        String action = intent.getAction();
        if ("android.intent.action.LOCALE_CHANGED".equals(action)) {
            forceReload();
        } else if ("android.search.action.GLOBAL_SEARCH_ACTIVITY_CHANGED".equals(action)) {
            Callbacks callback = getCallback();
            if (callback != null) {
                callback.bindSearchProviderChanged();
            }
        } else if (LauncherAppsCompat.ACTION_MANAGED_PROFILE_ADDED.equals(action) || LauncherAppsCompat.ACTION_MANAGED_PROFILE_REMOVED.equals(action)) {
            UserManagerCompat.getInstance(context).enableAndResetCache();
            forceReload();
        } else if ((LauncherAppsCompat.ACTION_MANAGED_PROFILE_AVAILABLE.equals(action) || LauncherAppsCompat.ACTION_MANAGED_PROFILE_UNAVAILABLE.equals(action)) && (fromIntent = UserHandleCompat.fromIntent(intent)) != null) {
            enqueuePackageUpdated(new PackageUpdatedTask(this, 7, new String[0], fromIntent));
        }
    }

    public void refreshAndBindWidgetsAndShortcuts(Callbacks callbacks, boolean z) {
        runOnWorkerThread(new Runnable(this, z, callbacks) { // from class: com.android.launcher3.LauncherModel.17
            final LauncherModel this$0;
            final boolean val$bindFirst;
            final Callbacks val$callbacks;

            {
                this.this$0 = this;
                this.val$bindFirst = z;
                this.val$callbacks = callbacks;
            }

            @Override // java.lang.Runnable
            public void run() {
                if (this.val$bindFirst && !this.this$0.mBgWidgetsModel.isEmpty()) {
                    this.this$0.bindWidgetsModel(this.val$callbacks, this.this$0.mBgWidgetsModel.m241clone());
                }
                WidgetsModel updateAndClone = this.this$0.mBgWidgetsModel.updateAndClone(this.this$0.mApp.getContext());
                this.this$0.bindWidgetsModel(this.val$callbacks, updateAndClone);
                LauncherAppState.getInstance().getWidgetCache().removeObsoletePreviews(updateAndClone.getRawList());
            }
        });
    }

    public void resetLoadedState(boolean z, boolean z2) {
        synchronized (this.mLock) {
            if (LauncherLog.DEBUG_LOADER) {
                LauncherLog.d("Launcher.Model", "resetLoadedState: mLoaderTask =" + this.mLoaderTask + ", this = " + this);
            }
            stopLoaderLocked();
            if (z) {
                this.mAllAppsLoaded = false;
            }
            if (z2) {
                this.mWorkspaceLoaded = false;
            }
        }
    }

    void runOnMainThread(Runnable runnable) {
        if (sWorkerThread.getThreadId() == Process.myTid()) {
            this.mHandler.post(runnable);
        } else {
            runnable.run();
        }
    }

    public void setPackageState(PackageInstallerCompat.PackageInstallInfo packageInstallInfo) {
        runOnWorkerThread(new AnonymousClass1(this, packageInstallInfo));
    }

    boolean shortcutExists(Context context, Intent intent, UserHandleCompat userHandleCompat) {
        String uri;
        String uri2;
        assertWorkspaceLoaded();
        if (intent.getComponent() != null) {
            String packageName = intent.getComponent().getPackageName();
            if (intent.getPackage() != null) {
                uri = intent.toUri(0);
                uri2 = new Intent(intent).setPackage(null).toUri(0);
            } else {
                uri = new Intent(intent).setPackage(packageName).toUri(0);
                uri2 = intent.toUri(0);
            }
        } else {
            uri = intent.toUri(0);
            uri2 = intent.toUri(0);
        }
        synchronized (sBgLock) {
            for (ItemInfo itemInfo : sBgItemsIdMap) {
                if (itemInfo instanceof ShortcutInfo) {
                    ShortcutInfo shortcutInfo = (ShortcutInfo) itemInfo;
                    Intent intent2 = shortcutInfo.promisedIntent == null ? shortcutInfo.intent : shortcutInfo.promisedIntent;
                    if (intent2 != null && shortcutInfo.user.equals(userHandleCompat)) {
                        String uri3 = intent2.toUri(0);
                        if (uri.equals(uri3) || uri2.equals(uri3)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }

    public void startLoader(int i) {
        startLoader(i, 0);
    }

    public void startLoader(int i, int i2) {
        InstallShortcutReceiver.enableInstallQueue();
        synchronized (this.mLock) {
            synchronized (mDeferredBindRunnables) {
                mDeferredBindRunnables.clear();
            }
            if (this.mCallbacks != null && this.mCallbacks.get() != null) {
                stopLoaderLocked();
                this.mLoaderTask = new LoaderTask(this, this.mApp.getContext(), i2);
                if (LauncherLog.DEBUG) {
                    LauncherLog.d("Launcher.Model", "startLoader: mAllAppsLoaded = " + this.mAllAppsLoaded + ",mWorkspaceLoaded = " + this.mWorkspaceLoaded + ",synchronousBindPage = " + i + ",mIsLoaderTaskRunning = " + this.mIsLoaderTaskRunning + ",mLoaderTask = " + this.mLoaderTask);
                }
                if (i == -1001 || !this.mAllAppsLoaded || !this.mWorkspaceLoaded || this.mIsLoaderTaskRunning) {
                    sWorkerThread.setPriority(5);
                    sWorker.post(this.mLoaderTask);
                } else {
                    this.mLoaderTask.runBindSynchronousPage(i);
                }
            }
        }
    }

    public void startLoaderFromBackground() {
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher.Model", "startLoaderFromBackground: mCallbacks = " + this.mCallbacks + ", this = " + this);
        }
        Callbacks callback = getCallback();
        boolean z = false;
        if (callback != null) {
            if (LauncherLog.DEBUG) {
                LauncherLog.d("Launcher.Model", "startLoaderFromBackground: callbacks.setLoadOnResume() = " + callback.setLoadOnResume() + ", this = " + this);
            }
            z = false;
            if (!callback.setLoadOnResume()) {
                z = true;
            }
        }
        if (z) {
            startLoader(-1001);
        }
    }

    public void stopLoader() {
        synchronized (this.mLock) {
            if (this.mLoaderTask != null) {
                if (LauncherLog.DEBUG) {
                    LauncherLog.d("Launcher.Model", "stopLoader: mLoaderTask = " + this.mLoaderTask + ",mIsLoaderTaskRunning = " + this.mIsLoaderTaskRunning);
                }
                this.mLoaderTask.stopLocked();
            }
        }
    }

    void unbindWorkspaceItemsOnMainThread() {
        ArrayList arrayList = new ArrayList();
        synchronized (sBgLock) {
            arrayList.addAll(sBgWorkspaceItems);
            arrayList.addAll(sBgAppWidgets);
        }
        runOnMainThread(new Runnable(this, arrayList) { // from class: com.android.launcher3.LauncherModel.5
            final LauncherModel this$0;
            final ArrayList val$tmpItems;

            {
                this.this$0 = this;
                this.val$tmpItems = arrayList;
            }

            @Override // java.lang.Runnable
            public void run() {
                for (ItemInfo itemInfo : this.val$tmpItems) {
                    itemInfo.unbind();
                }
            }
        });
    }

    public void updateSessionDisplayInfo(String str) {
        runOnWorkerThread(new AnonymousClass2(this, str));
    }

    public void updateWorkspaceScreenOrder(Context context, ArrayList<Long> arrayList) {
        ArrayList arrayList2 = new ArrayList(arrayList);
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = LauncherSettings$WorkspaceScreens.CONTENT_URI;
        Iterator it = arrayList2.iterator();
        while (it.hasNext()) {
            if (((Long) it.next()).longValue() < 0) {
                it.remove();
            }
        }
        runOnWorkerThread(new Runnable(this, uri, arrayList2, contentResolver) { // from class: com.android.launcher3.LauncherModel.12
            final LauncherModel this$0;
            final ContentResolver val$cr;
            final ArrayList val$screensCopy;
            final Uri val$uri;

            {
                this.this$0 = this;
                this.val$uri = uri;
                this.val$screensCopy = arrayList2;
                this.val$cr = contentResolver;
            }

            @Override // java.lang.Runnable
            public void run() {
                ArrayList<ContentProviderOperation> arrayList3 = new ArrayList<>();
                arrayList3.add(ContentProviderOperation.newDelete(this.val$uri).build());
                int size = this.val$screensCopy.size();
                for (int i = 0; i < size; i++) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("_id", Long.valueOf(((Long) this.val$screensCopy.get(i)).longValue()));
                    contentValues.put("screenRank", Integer.valueOf(i));
                    arrayList3.add(ContentProviderOperation.newInsert(this.val$uri).withValues(contentValues).build());
                }
                try {
                    this.val$cr.applyBatch(LauncherProvider.AUTHORITY, arrayList3);
                    synchronized (LauncherModel.sBgLock) {
                        LauncherModel.sBgWorkspaceScreens.clear();
                        LauncherModel.sBgWorkspaceScreens.addAll(this.val$screensCopy);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
