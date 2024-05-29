package com.android.settings.applications;

import android.app.ActivityManager;
import android.app.ActivityThread;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.content.pm.UserInfo;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.text.format.Formatter;
import android.util.Log;
import android.util.SparseArray;
import com.android.settings.R;
import com.android.settingslib.Utils;
import com.android.settingslib.applications.InterestingConfigChanges;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
/* loaded from: classes.dex */
public class RunningState {
    static Object sGlobalLock = new Object();
    static RunningState sInstance;
    final ActivityManager mAm;
    final Context mApplicationContext;
    final BackgroundHandler mBackgroundHandler;
    long mBackgroundProcessMemory;
    final HandlerThread mBackgroundThread;
    long mForegroundProcessMemory;
    boolean mHaveData;
    final boolean mHideManagedProfiles;
    int mNumBackgroundProcesses;
    int mNumForegroundProcesses;
    int mNumServiceProcesses;
    final PackageManager mPm;
    OnRefreshUiListener mRefreshUiListener;
    boolean mResumed;
    long mServiceProcessMemory;
    final UserManager mUm;
    boolean mWatchingBackgroundItems;
    final InterestingConfigChanges mInterestingConfigChanges = new InterestingConfigChanges();
    final SparseArray<HashMap<String, ProcessItem>> mServiceProcessesByName = new SparseArray<>();
    final SparseArray<ProcessItem> mServiceProcessesByPid = new SparseArray<>();
    final ServiceProcessComparator mServiceProcessComparator = new ServiceProcessComparator();
    final ArrayList<ProcessItem> mInterestingProcesses = new ArrayList<>();
    final SparseArray<ProcessItem> mRunningProcesses = new SparseArray<>();
    final ArrayList<ProcessItem> mProcessItems = new ArrayList<>();
    final ArrayList<ProcessItem> mAllProcessItems = new ArrayList<>();
    final SparseArray<MergedItem> mOtherUserMergedItems = new SparseArray<>();
    final SparseArray<MergedItem> mOtherUserBackgroundItems = new SparseArray<>();
    final SparseArray<AppProcessInfo> mTmpAppProcesses = new SparseArray<>();
    int mSequence = 0;
    final Comparator<MergedItem> mBackgroundComparator = new Comparator<MergedItem>() { // from class: com.android.settings.applications.RunningState.1
        @Override // java.util.Comparator
        public int compare(MergedItem mergedItem, MergedItem mergedItem2) {
            if (mergedItem.mUserId != mergedItem2.mUserId) {
                if (mergedItem.mUserId == RunningState.this.mMyUserId) {
                    return -1;
                }
                return (mergedItem2.mUserId != RunningState.this.mMyUserId && mergedItem.mUserId < mergedItem2.mUserId) ? -1 : 1;
            } else if (mergedItem.mProcess == mergedItem2.mProcess) {
                if (mergedItem.mLabel == mergedItem2.mLabel) {
                    return 0;
                }
                if (mergedItem.mLabel != null) {
                    return mergedItem.mLabel.compareTo(mergedItem2.mLabel);
                }
                return -1;
            } else if (mergedItem.mProcess == null) {
                return -1;
            } else {
                if (mergedItem2.mProcess == null) {
                    return 1;
                }
                ActivityManager.RunningAppProcessInfo runningAppProcessInfo = mergedItem.mProcess.mRunningProcessInfo;
                ActivityManager.RunningAppProcessInfo runningAppProcessInfo2 = mergedItem2.mProcess.mRunningProcessInfo;
                boolean z = runningAppProcessInfo.importance >= 400;
                if (z != (runningAppProcessInfo2.importance >= 400)) {
                    return z ? 1 : -1;
                }
                boolean z2 = (runningAppProcessInfo.flags & 4) != 0;
                if (z2 != ((runningAppProcessInfo2.flags & 4) != 0)) {
                    return z2 ? -1 : 1;
                } else if (runningAppProcessInfo.lru != runningAppProcessInfo2.lru) {
                    return runningAppProcessInfo.lru < runningAppProcessInfo2.lru ? -1 : 1;
                } else if (mergedItem.mProcess.mLabel == mergedItem2.mProcess.mLabel) {
                    return 0;
                } else {
                    if (mergedItem.mProcess.mLabel == null) {
                        return 1;
                    }
                    if (mergedItem2.mProcess.mLabel == null) {
                        return -1;
                    }
                    return mergedItem.mProcess.mLabel.compareTo(mergedItem2.mProcess.mLabel);
                }
            }
        }
    };
    final Object mLock = new Object();
    ArrayList<BaseItem> mItems = new ArrayList<>();
    ArrayList<MergedItem> mMergedItems = new ArrayList<>();
    ArrayList<MergedItem> mBackgroundItems = new ArrayList<>();
    ArrayList<MergedItem> mUserBackgroundItems = new ArrayList<>();
    final Handler mHandler = new Handler() { // from class: com.android.settings.applications.RunningState.2
        int mNextUpdate = 0;

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            int i;
            switch (message.what) {
                case 3:
                    if (message.arg1 != 0) {
                        i = 2;
                    } else {
                        i = 1;
                    }
                    this.mNextUpdate = i;
                    return;
                case 4:
                    synchronized (RunningState.this.mLock) {
                        if (RunningState.this.mResumed) {
                            removeMessages(4);
                            sendMessageDelayed(obtainMessage(4), 1000L);
                            if (RunningState.this.mRefreshUiListener != null) {
                                RunningState.this.mRefreshUiListener.onRefreshUi(this.mNextUpdate);
                                this.mNextUpdate = 0;
                                return;
                            }
                            return;
                        }
                        return;
                    }
                default:
                    return;
            }
        }
    };
    private final UserManagerBroadcastReceiver mUmBroadcastReceiver = new UserManagerBroadcastReceiver();
    final int mMyUserId = UserHandle.myUserId();

    /* loaded from: classes.dex */
    interface OnRefreshUiListener {
        void onRefreshUi(int i);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static class AppProcessInfo {
        boolean hasForegroundServices;
        boolean hasServices;
        final ActivityManager.RunningAppProcessInfo info;

        AppProcessInfo(ActivityManager.RunningAppProcessInfo runningAppProcessInfo) {
            this.info = runningAppProcessInfo;
        }
    }

    /* loaded from: classes.dex */
    final class BackgroundHandler extends Handler {
        public BackgroundHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    RunningState.this.reset();
                    return;
                case 2:
                    synchronized (RunningState.this.mLock) {
                        if (RunningState.this.mResumed) {
                            Message obtainMessage = RunningState.this.mHandler.obtainMessage(3);
                            obtainMessage.arg1 = RunningState.this.update(RunningState.this.mApplicationContext, RunningState.this.mAm) ? 1 : 0;
                            RunningState.this.mHandler.sendMessage(obtainMessage);
                            removeMessages(2);
                            sendMessageDelayed(obtainMessage(2), 2000L);
                            return;
                        }
                        return;
                    }
                default:
                    return;
            }
        }
    }

    /* loaded from: classes.dex */
    private final class UserManagerBroadcastReceiver extends BroadcastReceiver {
        private volatile boolean usersChanged;

        private UserManagerBroadcastReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            synchronized (RunningState.this.mLock) {
                if (RunningState.this.mResumed) {
                    RunningState.this.mHaveData = false;
                    RunningState.this.mBackgroundHandler.removeMessages(1);
                    RunningState.this.mBackgroundHandler.sendEmptyMessage(1);
                    RunningState.this.mBackgroundHandler.removeMessages(2);
                    RunningState.this.mBackgroundHandler.sendEmptyMessage(2);
                } else {
                    this.usersChanged = true;
                }
            }
        }

        public boolean checkUsersChangedLocked() {
            boolean z = this.usersChanged;
            this.usersChanged = false;
            return z;
        }

        void register(Context context) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.USER_STOPPED");
            intentFilter.addAction("android.intent.action.USER_STARTED");
            intentFilter.addAction("android.intent.action.USER_INFO_CHANGED");
            context.registerReceiverAsUser(this, UserHandle.ALL, intentFilter, null, null);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static class UserState {
        Drawable mIcon;
        UserInfo mInfo;
        String mLabel;

        UserState() {
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static class BaseItem {
        long mActiveSince;
        boolean mBackground;
        int mCurSeq;
        String mCurSizeStr;
        String mDescription;
        CharSequence mDisplayLabel;
        final boolean mIsProcess;
        String mLabel;
        boolean mNeedDivider;
        PackageItemInfo mPackageInfo;
        long mSize;
        String mSizeStr;
        final int mUserId;

        public BaseItem(boolean z, int i) {
            this.mIsProcess = z;
            this.mUserId = i;
        }

        public Drawable loadIcon(Context context, RunningState runningState) {
            if (this.mPackageInfo != null) {
                return runningState.mPm.getUserBadgedIcon(this.mPackageInfo.loadUnbadgedIcon(runningState.mPm), new UserHandle(this.mUserId));
            }
            return null;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static class ServiceItem extends BaseItem {
        MergedItem mMergedItem;
        ActivityManager.RunningServiceInfo mRunningService;
        ServiceInfo mServiceInfo;
        boolean mShownAsStarted;

        public ServiceItem(int i) {
            super(false, i);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static class ProcessItem extends BaseItem {
        long mActiveSince;
        ProcessItem mClient;
        final SparseArray<ProcessItem> mDependentProcesses;
        boolean mInteresting;
        boolean mIsStarted;
        boolean mIsSystem;
        int mLastNumDependentProcesses;
        MergedItem mMergedItem;
        int mPid;
        final String mProcessName;
        ActivityManager.RunningAppProcessInfo mRunningProcessInfo;
        int mRunningSeq;
        final HashMap<ComponentName, ServiceItem> mServices;
        final int mUid;

        public ProcessItem(Context context, int i, String str) {
            super(true, UserHandle.getUserId(i));
            this.mServices = new HashMap<>();
            this.mDependentProcesses = new SparseArray<>();
            this.mDescription = context.getResources().getString(R.string.service_process_name, str);
            this.mUid = i;
            this.mProcessName = str;
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public void ensureLabel(PackageManager packageManager) {
            CharSequence text;
            if (this.mLabel != null) {
                return;
            }
            try {
                ApplicationInfo applicationInfo = packageManager.getApplicationInfo(this.mProcessName, 4194304);
                if (applicationInfo.uid == this.mUid) {
                    this.mDisplayLabel = applicationInfo.loadLabel(packageManager);
                    this.mLabel = this.mDisplayLabel.toString();
                    this.mPackageInfo = applicationInfo;
                    return;
                }
            } catch (PackageManager.NameNotFoundException e) {
            }
            String[] packagesForUid = packageManager.getPackagesForUid(this.mUid);
            if (packagesForUid.length == 1) {
                try {
                    ApplicationInfo applicationInfo2 = packageManager.getApplicationInfo(packagesForUid[0], 4194304);
                    this.mDisplayLabel = applicationInfo2.loadLabel(packageManager);
                    this.mLabel = this.mDisplayLabel.toString();
                    this.mPackageInfo = applicationInfo2;
                    return;
                } catch (PackageManager.NameNotFoundException e2) {
                }
            }
            for (String str : packagesForUid) {
                try {
                    PackageInfo packageInfo = packageManager.getPackageInfo(str, 0);
                    if (packageInfo.sharedUserLabel != 0 && (text = packageManager.getText(str, packageInfo.sharedUserLabel, packageInfo.applicationInfo)) != null) {
                        this.mDisplayLabel = text;
                        this.mLabel = text.toString();
                        this.mPackageInfo = packageInfo.applicationInfo;
                        return;
                    }
                } catch (PackageManager.NameNotFoundException e3) {
                }
            }
            if (this.mServices.size() <= 0) {
                try {
                    ApplicationInfo applicationInfo3 = packageManager.getApplicationInfo(packagesForUid[0], 4194304);
                    this.mDisplayLabel = applicationInfo3.loadLabel(packageManager);
                    this.mLabel = this.mDisplayLabel.toString();
                    this.mPackageInfo = applicationInfo3;
                    return;
                } catch (PackageManager.NameNotFoundException e4) {
                    return;
                }
            }
            this.mPackageInfo = this.mServices.values().iterator().next().mServiceInfo.applicationInfo;
            this.mDisplayLabel = this.mPackageInfo.loadLabel(packageManager);
            this.mLabel = this.mDisplayLabel.toString();
        }

        boolean updateService(Context context, ActivityManager.RunningServiceInfo runningServiceInfo) {
            boolean z;
            PackageManager packageManager = context.getPackageManager();
            ServiceItem serviceItem = this.mServices.get(runningServiceInfo.service);
            if (serviceItem == null) {
                serviceItem = new ServiceItem(this.mUserId);
                serviceItem.mRunningService = runningServiceInfo;
                try {
                    serviceItem.mServiceInfo = ActivityThread.getPackageManager().getServiceInfo(runningServiceInfo.service, 4194304, UserHandle.getUserId(runningServiceInfo.uid));
                    if (serviceItem.mServiceInfo == null) {
                        Log.d("RunningService", "getServiceInfo returned null for: " + runningServiceInfo.service);
                        return false;
                    }
                } catch (RemoteException e) {
                }
                serviceItem.mDisplayLabel = RunningState.makeLabel(packageManager, serviceItem.mRunningService.service.getClassName(), serviceItem.mServiceInfo);
                this.mLabel = this.mDisplayLabel != null ? this.mDisplayLabel.toString() : null;
                serviceItem.mPackageInfo = serviceItem.mServiceInfo.applicationInfo;
                this.mServices.put(runningServiceInfo.service, serviceItem);
                z = true;
            } else {
                z = false;
            }
            serviceItem.mCurSeq = this.mCurSeq;
            serviceItem.mRunningService = runningServiceInfo;
            long j = runningServiceInfo.restarting == 0 ? runningServiceInfo.activeSince : -1L;
            if (serviceItem.mActiveSince != j) {
                serviceItem.mActiveSince = j;
                z = true;
            }
            if (runningServiceInfo.clientPackage != null && runningServiceInfo.clientLabel != 0) {
                if (serviceItem.mShownAsStarted) {
                    serviceItem.mShownAsStarted = false;
                    z = true;
                }
                try {
                    serviceItem.mDescription = context.getResources().getString(R.string.service_client_name, packageManager.getResourcesForApplication(runningServiceInfo.clientPackage).getString(runningServiceInfo.clientLabel));
                } catch (PackageManager.NameNotFoundException e2) {
                    serviceItem.mDescription = null;
                }
            } else {
                if (!serviceItem.mShownAsStarted) {
                    serviceItem.mShownAsStarted = true;
                    z = true;
                }
                serviceItem.mDescription = context.getResources().getString(R.string.service_started_by_app);
            }
            return z;
        }

        boolean updateSize(Context context, long j, int i) {
            this.mSize = j * 1024;
            if (this.mCurSeq == i) {
                String formatShortFileSize = Formatter.formatShortFileSize(context, this.mSize);
                if (!formatShortFileSize.equals(this.mSizeStr)) {
                    this.mSizeStr = formatShortFileSize;
                    return false;
                }
            }
            return false;
        }

        boolean buildDependencyChain(Context context, PackageManager packageManager, int i) {
            int size = this.mDependentProcesses.size();
            boolean z = false;
            for (int i2 = 0; i2 < size; i2++) {
                ProcessItem valueAt = this.mDependentProcesses.valueAt(i2);
                if (valueAt.mClient != this) {
                    valueAt.mClient = this;
                    z = true;
                }
                valueAt.mCurSeq = i;
                valueAt.ensureLabel(packageManager);
                z |= valueAt.buildDependencyChain(context, packageManager, i);
            }
            if (this.mLastNumDependentProcesses != this.mDependentProcesses.size()) {
                this.mLastNumDependentProcesses = this.mDependentProcesses.size();
                return true;
            }
            return z;
        }

        void addDependentProcesses(ArrayList<BaseItem> arrayList, ArrayList<ProcessItem> arrayList2) {
            int size = this.mDependentProcesses.size();
            for (int i = 0; i < size; i++) {
                ProcessItem valueAt = this.mDependentProcesses.valueAt(i);
                valueAt.addDependentProcesses(arrayList, arrayList2);
                arrayList.add(valueAt);
                if (valueAt.mPid > 0) {
                    arrayList2.add(valueAt);
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static class MergedItem extends BaseItem {
        final ArrayList<MergedItem> mChildren;
        private int mLastNumProcesses;
        private int mLastNumServices;
        final ArrayList<ProcessItem> mOtherProcesses;
        ProcessItem mProcess;
        final ArrayList<ServiceItem> mServices;
        UserState mUser;

        MergedItem(int i) {
            super(false, i);
            this.mOtherProcesses = new ArrayList<>();
            this.mServices = new ArrayList<>();
            this.mChildren = new ArrayList<>();
            this.mLastNumProcesses = -1;
            this.mLastNumServices = -1;
        }

        private void setDescription(Context context, int i, int i2) {
            if (this.mLastNumProcesses != i || this.mLastNumServices != i2) {
                this.mLastNumProcesses = i;
                this.mLastNumServices = i2;
                int i3 = R.string.running_processes_item_description_s_s;
                if (i != 1) {
                    if (i2 != 1) {
                        i3 = R.string.running_processes_item_description_p_p;
                    } else {
                        i3 = R.string.running_processes_item_description_p_s;
                    }
                } else if (i2 != 1) {
                    i3 = R.string.running_processes_item_description_s_p;
                }
                this.mDescription = context.getResources().getString(i3, Integer.valueOf(i), Integer.valueOf(i2));
            }
        }

        boolean update(Context context, boolean z) {
            this.mBackground = z;
            if (this.mUser != null) {
                this.mPackageInfo = this.mChildren.get(0).mProcess.mPackageInfo;
                this.mLabel = this.mUser != null ? this.mUser.mLabel : null;
                this.mDisplayLabel = this.mLabel;
                this.mActiveSince = -1L;
                int i = 0;
                int i2 = 0;
                for (int i3 = 0; i3 < this.mChildren.size(); i3++) {
                    MergedItem mergedItem = this.mChildren.get(i3);
                    i += mergedItem.mLastNumProcesses;
                    i2 += mergedItem.mLastNumServices;
                    if (mergedItem.mActiveSince >= 0 && this.mActiveSince < mergedItem.mActiveSince) {
                        this.mActiveSince = mergedItem.mActiveSince;
                    }
                }
                if (!this.mBackground) {
                    setDescription(context, i, i2);
                }
            } else {
                this.mPackageInfo = this.mProcess.mPackageInfo;
                this.mDisplayLabel = this.mProcess.mDisplayLabel;
                this.mLabel = this.mProcess.mLabel;
                if (!this.mBackground) {
                    setDescription(context, (this.mProcess.mPid > 0 ? 1 : 0) + this.mOtherProcesses.size(), this.mServices.size());
                }
                this.mActiveSince = -1L;
                for (int i4 = 0; i4 < this.mServices.size(); i4++) {
                    ServiceItem serviceItem = this.mServices.get(i4);
                    if (serviceItem.mActiveSince >= 0 && this.mActiveSince < serviceItem.mActiveSince) {
                        this.mActiveSince = serviceItem.mActiveSince;
                    }
                }
            }
            return false;
        }

        boolean updateSize(Context context) {
            if (this.mUser != null) {
                this.mSize = 0L;
                for (int i = 0; i < this.mChildren.size(); i++) {
                    MergedItem mergedItem = this.mChildren.get(i);
                    mergedItem.updateSize(context);
                    this.mSize += mergedItem.mSize;
                }
            } else {
                this.mSize = this.mProcess.mSize;
                for (int i2 = 0; i2 < this.mOtherProcesses.size(); i2++) {
                    this.mSize += this.mOtherProcesses.get(i2).mSize;
                }
            }
            String formatShortFileSize = Formatter.formatShortFileSize(context, this.mSize);
            if (formatShortFileSize.equals(this.mSizeStr)) {
                return false;
            }
            this.mSizeStr = formatShortFileSize;
            return false;
        }

        @Override // com.android.settings.applications.RunningState.BaseItem
        public Drawable loadIcon(Context context, RunningState runningState) {
            if (this.mUser == null) {
                return super.loadIcon(context, runningState);
            }
            if (this.mUser.mIcon != null) {
                Drawable.ConstantState constantState = this.mUser.mIcon.getConstantState();
                if (constantState == null) {
                    return this.mUser.mIcon;
                }
                return constantState.newDrawable();
            }
            return context.getDrawable(17302624);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public class ServiceProcessComparator implements Comparator<ProcessItem> {
        ServiceProcessComparator() {
        }

        @Override // java.util.Comparator
        public int compare(ProcessItem processItem, ProcessItem processItem2) {
            if (processItem.mUserId != processItem2.mUserId) {
                if (processItem.mUserId == RunningState.this.mMyUserId) {
                    return -1;
                }
                return (processItem2.mUserId != RunningState.this.mMyUserId && processItem.mUserId < processItem2.mUserId) ? -1 : 1;
            } else if (processItem.mIsStarted != processItem2.mIsStarted) {
                return processItem.mIsStarted ? -1 : 1;
            } else if (processItem.mIsSystem != processItem2.mIsSystem) {
                return processItem.mIsSystem ? 1 : -1;
            } else if (processItem.mActiveSince != processItem2.mActiveSince) {
                return processItem.mActiveSince > processItem2.mActiveSince ? -1 : 1;
            } else {
                return 0;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static CharSequence makeLabel(PackageManager packageManager, String str, PackageItemInfo packageItemInfo) {
        CharSequence loadLabel;
        if (packageItemInfo != null && ((packageItemInfo.labelRes != 0 || packageItemInfo.nonLocalizedLabel != null) && (loadLabel = packageItemInfo.loadLabel(packageManager)) != null)) {
            return loadLabel;
        }
        int lastIndexOf = str.lastIndexOf(46);
        if (lastIndexOf >= 0) {
            return str.substring(lastIndexOf + 1, str.length());
        }
        return str;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static RunningState getInstance(Context context) {
        RunningState runningState;
        synchronized (sGlobalLock) {
            if (sInstance == null) {
                sInstance = new RunningState(context);
            }
            runningState = sInstance;
        }
        return runningState;
    }

    private RunningState(Context context) {
        this.mApplicationContext = context.getApplicationContext();
        this.mAm = (ActivityManager) this.mApplicationContext.getSystemService("activity");
        this.mPm = this.mApplicationContext.getPackageManager();
        this.mUm = (UserManager) this.mApplicationContext.getSystemService("user");
        UserInfo userInfo = this.mUm.getUserInfo(this.mMyUserId);
        this.mHideManagedProfiles = userInfo == null || !userInfo.canHaveProfile();
        this.mResumed = false;
        this.mBackgroundThread = new HandlerThread("RunningState:Background");
        this.mBackgroundThread.start();
        this.mBackgroundHandler = new BackgroundHandler(this.mBackgroundThread.getLooper());
        this.mUmBroadcastReceiver.register(this.mApplicationContext);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void resume(OnRefreshUiListener onRefreshUiListener) {
        synchronized (this.mLock) {
            this.mResumed = true;
            this.mRefreshUiListener = onRefreshUiListener;
            boolean checkUsersChangedLocked = this.mUmBroadcastReceiver.checkUsersChangedLocked();
            boolean applyNewConfig = this.mInterestingConfigChanges.applyNewConfig(this.mApplicationContext.getResources());
            if (checkUsersChangedLocked || applyNewConfig) {
                this.mHaveData = false;
                this.mBackgroundHandler.removeMessages(1);
                this.mBackgroundHandler.removeMessages(2);
                this.mBackgroundHandler.sendEmptyMessage(1);
            }
            if (!this.mBackgroundHandler.hasMessages(2)) {
                this.mBackgroundHandler.sendEmptyMessage(2);
            }
            this.mHandler.sendEmptyMessage(4);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void updateNow() {
        synchronized (this.mLock) {
            this.mBackgroundHandler.removeMessages(2);
            this.mBackgroundHandler.sendEmptyMessage(2);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean hasData() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mHaveData;
        }
        return z;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void waitForData() {
        synchronized (this.mLock) {
            while (!this.mHaveData) {
                try {
                    this.mLock.wait(0L);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void pause() {
        synchronized (this.mLock) {
            this.mResumed = false;
            this.mRefreshUiListener = null;
            this.mHandler.removeMessages(4);
        }
    }

    private boolean isInterestingProcess(ActivityManager.RunningAppProcessInfo runningAppProcessInfo) {
        if ((runningAppProcessInfo.flags & 1) != 0) {
            return true;
        }
        return (runningAppProcessInfo.flags & 2) == 0 && runningAppProcessInfo.importance >= 100 && runningAppProcessInfo.importance < 350 && runningAppProcessInfo.importanceReasonCode == 0;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void reset() {
        this.mServiceProcessesByName.clear();
        this.mServiceProcessesByPid.clear();
        this.mInterestingProcesses.clear();
        this.mRunningProcesses.clear();
        this.mProcessItems.clear();
        this.mAllProcessItems.clear();
    }

    private void addOtherUserItem(Context context, ArrayList<MergedItem> arrayList, SparseArray<MergedItem> sparseArray, MergedItem mergedItem) {
        MergedItem mergedItem2 = sparseArray.get(mergedItem.mUserId);
        if (mergedItem2 == null || mergedItem2.mCurSeq != this.mSequence) {
            UserInfo userInfo = this.mUm.getUserInfo(mergedItem.mUserId);
            if (userInfo == null) {
                return;
            }
            if (this.mHideManagedProfiles && userInfo.isManagedProfile()) {
                return;
            }
            if (mergedItem2 == null) {
                mergedItem2 = new MergedItem(mergedItem.mUserId);
                sparseArray.put(mergedItem.mUserId, mergedItem2);
            } else {
                mergedItem2.mChildren.clear();
            }
            mergedItem2.mCurSeq = this.mSequence;
            mergedItem2.mUser = new UserState();
            mergedItem2.mUser.mInfo = userInfo;
            mergedItem2.mUser.mIcon = Utils.getUserIcon(context, this.mUm, userInfo);
            mergedItem2.mUser.mLabel = Utils.getUserLabel(context, userInfo);
            arrayList.add(mergedItem2);
        }
        mergedItem2.mChildren.add(mergedItem);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Removed duplicated region for block: B:354:0x06df  */
    /* JADX WARN: Removed duplicated region for block: B:358:0x06ef  */
    /* JADX WARN: Removed duplicated region for block: B:365:0x070b  */
    /* JADX WARN: Removed duplicated region for block: B:381:0x0769  */
    /* JADX WARN: Removed duplicated region for block: B:385:0x0775 A[LOOP:25: B:383:0x076d->B:385:0x0775, LOOP_END] */
    /* JADX WARN: Removed duplicated region for block: B:421:0x0786 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public boolean update(Context context, ActivityManager activityManager) {
        int i;
        boolean z;
        long j;
        long j2;
        long j3;
        ArrayList<MergedItem> arrayList;
        int i2;
        int i3;
        ArrayList<MergedItem> arrayList2;
        int i4;
        int i5;
        long[] jArr;
        int[] iArr;
        char c;
        MergedItem mergedItem;
        MergedItem mergedItem2;
        int[] iArr2;
        List<ActivityManager.RunningAppProcessInfo> list;
        AppProcessInfo appProcessInfo;
        boolean z2;
        AppProcessInfo appProcessInfo2;
        PackageManager packageManager = context.getPackageManager();
        boolean z3 = true;
        this.mSequence++;
        List<ActivityManager.RunningServiceInfo> runningServices = activityManager.getRunningServices(100);
        int size = runningServices != null ? runningServices.size() : 0;
        int i6 = 0;
        while (i6 < size) {
            ActivityManager.RunningServiceInfo runningServiceInfo = runningServices.get(i6);
            if (!runningServiceInfo.started && runningServiceInfo.clientLabel == 0) {
                runningServices.remove(i6);
                i6--;
                size--;
            } else if ((runningServiceInfo.flags & 8) != 0) {
                runningServices.remove(i6);
                i6--;
                size--;
            }
            i6++;
        }
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = activityManager.getRunningAppProcesses();
        int size2 = runningAppProcesses != null ? runningAppProcesses.size() : 0;
        this.mTmpAppProcesses.clear();
        for (int i7 = 0; i7 < size2; i7++) {
            ActivityManager.RunningAppProcessInfo runningAppProcessInfo = runningAppProcesses.get(i7);
            this.mTmpAppProcesses.put(runningAppProcessInfo.pid, new AppProcessInfo(runningAppProcessInfo));
        }
        for (int i8 = 0; i8 < size; i8++) {
            ActivityManager.RunningServiceInfo runningServiceInfo2 = runningServices.get(i8);
            if (runningServiceInfo2.restarting == 0 && runningServiceInfo2.pid > 0 && (appProcessInfo2 = this.mTmpAppProcesses.get(runningServiceInfo2.pid)) != null) {
                appProcessInfo2.hasServices = true;
                if (runningServiceInfo2.foreground) {
                    appProcessInfo2.hasForegroundServices = true;
                }
            }
        }
        int i9 = 0;
        boolean z4 = false;
        while (i9 < size) {
            ActivityManager.RunningServiceInfo runningServiceInfo3 = runningServices.get(i9);
            if (runningServiceInfo3.restarting == 0 && runningServiceInfo3.pid > 0 && (appProcessInfo = this.mTmpAppProcesses.get(runningServiceInfo3.pid)) != null && !appProcessInfo.hasForegroundServices && appProcessInfo.info.importance < 300) {
                AppProcessInfo appProcessInfo3 = this.mTmpAppProcesses.get(appProcessInfo.info.importanceReasonPid);
                while (appProcessInfo3 != null) {
                    if (appProcessInfo3.hasServices || isInterestingProcess(appProcessInfo3.info)) {
                        z2 = z3;
                        break;
                    }
                    appProcessInfo3 = this.mTmpAppProcesses.get(appProcessInfo3.info.importanceReasonPid);
                }
                z2 = false;
                if (z2) {
                    list = runningAppProcesses;
                    i9++;
                    runningAppProcesses = list;
                    z3 = true;
                }
            }
            HashMap<String, ProcessItem> hashMap = this.mServiceProcessesByName.get(runningServiceInfo3.uid);
            if (hashMap == null) {
                hashMap = new HashMap<>();
                this.mServiceProcessesByName.put(runningServiceInfo3.uid, hashMap);
            }
            ProcessItem processItem = hashMap.get(runningServiceInfo3.process);
            if (processItem == null) {
                processItem = new ProcessItem(context, runningServiceInfo3.uid, runningServiceInfo3.process);
                hashMap.put(runningServiceInfo3.process, processItem);
                z4 = z3;
            }
            if (processItem.mCurSeq != this.mSequence) {
                list = runningAppProcesses;
                int i10 = runningServiceInfo3.restarting == 0 ? runningServiceInfo3.pid : 0;
                if (i10 != processItem.mPid) {
                    if (processItem.mPid != i10) {
                        if (processItem.mPid != 0) {
                            this.mServiceProcessesByPid.remove(processItem.mPid);
                        }
                        if (i10 != 0) {
                            this.mServiceProcessesByPid.put(i10, processItem);
                        }
                        processItem.mPid = i10;
                    }
                    z4 = true;
                }
                processItem.mDependentProcesses.clear();
                processItem.mCurSeq = this.mSequence;
            } else {
                list = runningAppProcesses;
            }
            z4 |= processItem.updateService(context, runningServiceInfo3);
            i9++;
            runningAppProcesses = list;
            z3 = true;
        }
        List<ActivityManager.RunningAppProcessInfo> list2 = runningAppProcesses;
        boolean z5 = z4;
        int i11 = 0;
        while (i11 < size2) {
            List<ActivityManager.RunningAppProcessInfo> list3 = list2;
            ActivityManager.RunningAppProcessInfo runningAppProcessInfo2 = list3.get(i11);
            ProcessItem processItem2 = this.mServiceProcessesByPid.get(runningAppProcessInfo2.pid);
            if (processItem2 == null) {
                processItem2 = this.mRunningProcesses.get(runningAppProcessInfo2.pid);
                if (processItem2 == null) {
                    ProcessItem processItem3 = new ProcessItem(context, runningAppProcessInfo2.uid, runningAppProcessInfo2.processName);
                    processItem3.mPid = runningAppProcessInfo2.pid;
                    this.mRunningProcesses.put(runningAppProcessInfo2.pid, processItem3);
                    processItem2 = processItem3;
                    z5 = true;
                }
                processItem2.mDependentProcesses.clear();
            }
            if (isInterestingProcess(runningAppProcessInfo2)) {
                if (!this.mInterestingProcesses.contains(processItem2)) {
                    this.mInterestingProcesses.add(processItem2);
                    z5 = true;
                }
                processItem2.mCurSeq = this.mSequence;
                processItem2.mInteresting = true;
                processItem2.ensureLabel(packageManager);
            } else {
                processItem2.mInteresting = false;
            }
            processItem2.mRunningSeq = this.mSequence;
            processItem2.mRunningProcessInfo = runningAppProcessInfo2;
            i11++;
            list2 = list3;
        }
        boolean z6 = z5;
        int size3 = this.mRunningProcesses.size();
        int i12 = 0;
        while (i12 < size3) {
            ProcessItem valueAt = this.mRunningProcesses.valueAt(i12);
            if (valueAt.mRunningSeq == this.mSequence) {
                int i13 = valueAt.mRunningProcessInfo.importanceReasonPid;
                if (i13 != 0) {
                    ProcessItem processItem4 = this.mServiceProcessesByPid.get(i13);
                    if (processItem4 == null) {
                        processItem4 = this.mRunningProcesses.get(i13);
                    }
                    if (processItem4 != null) {
                        processItem4.mDependentProcesses.put(valueAt.mPid, valueAt);
                    }
                } else {
                    valueAt.mClient = null;
                }
                i12++;
            } else {
                this.mRunningProcesses.remove(this.mRunningProcesses.keyAt(i12));
                size3--;
                z6 = true;
            }
        }
        int size4 = this.mInterestingProcesses.size();
        int i14 = 0;
        while (i14 < size4) {
            ProcessItem processItem5 = this.mInterestingProcesses.get(i14);
            if (!processItem5.mInteresting || this.mRunningProcesses.get(processItem5.mPid) == null) {
                this.mInterestingProcesses.remove(i14);
                i14--;
                size4--;
                z6 = true;
            }
            i14++;
        }
        int size5 = this.mServiceProcessesByPid.size();
        for (int i15 = 0; i15 < size5; i15++) {
            ProcessItem valueAt2 = this.mServiceProcessesByPid.valueAt(i15);
            if (valueAt2.mCurSeq == this.mSequence) {
                z6 |= valueAt2.buildDependencyChain(context, packageManager, this.mSequence);
            }
        }
        ArrayList arrayList3 = null;
        for (int i16 = 0; i16 < this.mServiceProcessesByName.size(); i16++) {
            HashMap<String, ProcessItem> valueAt3 = this.mServiceProcessesByName.valueAt(i16);
            Iterator<ProcessItem> it = valueAt3.values().iterator();
            while (it.hasNext()) {
                ProcessItem next = it.next();
                if (next.mCurSeq == this.mSequence) {
                    next.ensureLabel(packageManager);
                    if (next.mPid == 0) {
                        next.mDependentProcesses.clear();
                    }
                    Iterator<ServiceItem> it2 = next.mServices.values().iterator();
                    while (it2.hasNext()) {
                        if (it2.next().mCurSeq != this.mSequence) {
                            it2.remove();
                            z6 = true;
                        }
                    }
                } else {
                    it.remove();
                    if (valueAt3.size() == 0) {
                        if (arrayList3 == null) {
                            arrayList3 = new ArrayList();
                        }
                        arrayList3.add(Integer.valueOf(this.mServiceProcessesByName.keyAt(i16)));
                    }
                    if (next.mPid != 0) {
                        this.mServiceProcessesByPid.remove(next.mPid);
                    }
                    z6 = true;
                }
            }
        }
        if (arrayList3 != null) {
            for (int i17 = 0; i17 < arrayList3.size(); i17++) {
                this.mServiceProcessesByName.remove(((Integer) arrayList3.get(i17)).intValue());
            }
        }
        if (z6) {
            ArrayList arrayList4 = new ArrayList();
            for (int i18 = 0; i18 < this.mServiceProcessesByName.size(); i18++) {
                for (ProcessItem processItem6 : this.mServiceProcessesByName.valueAt(i18).values()) {
                    processItem6.mIsSystem = false;
                    processItem6.mIsStarted = true;
                    processItem6.mActiveSince = Long.MAX_VALUE;
                    for (ServiceItem serviceItem : processItem6.mServices.values()) {
                        if (serviceItem.mServiceInfo != null && (serviceItem.mServiceInfo.applicationInfo.flags & 1) != 0) {
                            processItem6.mIsSystem = true;
                        }
                        if (serviceItem.mRunningService != null && serviceItem.mRunningService.clientLabel != 0) {
                            processItem6.mIsStarted = false;
                            if (processItem6.mActiveSince > serviceItem.mRunningService.activeSince) {
                                processItem6.mActiveSince = serviceItem.mRunningService.activeSince;
                            }
                        }
                    }
                    arrayList4.add(processItem6);
                }
            }
            Collections.sort(arrayList4, this.mServiceProcessComparator);
            ArrayList<BaseItem> arrayList5 = new ArrayList<>();
            ArrayList<MergedItem> arrayList6 = new ArrayList<>();
            this.mProcessItems.clear();
            for (int i19 = 0; i19 < arrayList4.size(); i19++) {
                ProcessItem processItem7 = (ProcessItem) arrayList4.get(i19);
                processItem7.mNeedDivider = false;
                processItem7.addDependentProcesses(arrayList5, this.mProcessItems);
                arrayList5.add(processItem7);
                if (processItem7.mPid > 0) {
                    this.mProcessItems.add(processItem7);
                }
                MergedItem mergedItem3 = null;
                boolean z7 = false;
                for (ServiceItem serviceItem2 : processItem7.mServices.values()) {
                    serviceItem2.mNeedDivider = z7;
                    arrayList5.add(serviceItem2);
                    if (serviceItem2.mMergedItem != null) {
                        if (mergedItem3 == null || mergedItem3 == serviceItem2.mMergedItem) {
                        }
                        mergedItem3 = serviceItem2.mMergedItem;
                    }
                    z7 = true;
                }
                MergedItem mergedItem4 = new MergedItem(processItem7.mUserId);
                for (ServiceItem serviceItem3 : processItem7.mServices.values()) {
                    mergedItem4.mServices.add(serviceItem3);
                    serviceItem3.mMergedItem = mergedItem4;
                }
                mergedItem4.mProcess = processItem7;
                mergedItem4.mOtherProcesses.clear();
                for (int size6 = this.mProcessItems.size(); size6 < this.mProcessItems.size() - 1; size6++) {
                    mergedItem4.mOtherProcesses.add(this.mProcessItems.get(size6));
                }
                mergedItem4.update(context, false);
                if (mergedItem4.mUserId != this.mMyUserId) {
                    addOtherUserItem(context, arrayList6, this.mOtherUserMergedItems, mergedItem4);
                } else {
                    arrayList6.add(mergedItem4);
                }
            }
            int size7 = this.mInterestingProcesses.size();
            for (int i20 = 0; i20 < size7; i20++) {
                ProcessItem processItem8 = this.mInterestingProcesses.get(i20);
                if (processItem8.mClient == null && processItem8.mServices.size() <= 0) {
                    if (processItem8.mMergedItem == null) {
                        processItem8.mMergedItem = new MergedItem(processItem8.mUserId);
                        processItem8.mMergedItem.mProcess = processItem8;
                    }
                    processItem8.mMergedItem.update(context, false);
                    if (processItem8.mMergedItem.mUserId != this.mMyUserId) {
                        addOtherUserItem(context, arrayList6, this.mOtherUserMergedItems, processItem8.mMergedItem);
                    } else {
                        arrayList6.add(0, processItem8.mMergedItem);
                    }
                    this.mProcessItems.add(processItem8);
                }
            }
            int size8 = this.mOtherUserMergedItems.size();
            for (int i21 = 0; i21 < size8; i21++) {
                MergedItem valueAt4 = this.mOtherUserMergedItems.valueAt(i21);
                if (valueAt4.mCurSeq == this.mSequence) {
                    valueAt4.update(context, false);
                }
            }
            i = 0;
            synchronized (this.mLock) {
                this.mItems = arrayList5;
                this.mMergedItems = arrayList6;
            }
        } else {
            i = 0;
        }
        this.mAllProcessItems.clear();
        this.mAllProcessItems.addAll(this.mProcessItems);
        int size9 = this.mRunningProcesses.size();
        int i22 = i;
        int i23 = i22;
        int i24 = i23;
        int i25 = i24;
        while (i22 < size9) {
            ProcessItem valueAt5 = this.mRunningProcesses.valueAt(i22);
            if (valueAt5.mCurSeq == this.mSequence) {
                i23++;
            } else if (valueAt5.mRunningProcessInfo.importance >= 400) {
                i24++;
                this.mAllProcessItems.add(valueAt5);
            } else if (valueAt5.mRunningProcessInfo.importance <= 200) {
                i25++;
                this.mAllProcessItems.add(valueAt5);
            } else {
                Log.i("RunningState", "Unknown non-service process: " + valueAt5.mProcessName + " #" + valueAt5.mPid);
            }
            i22++;
        }
        try {
            int size10 = this.mAllProcessItems.size();
            int[] iArr3 = new int[size10];
            for (int i26 = i; i26 < size10; i26++) {
                try {
                    iArr3[i26] = this.mAllProcessItems.get(i26).mPid;
                } catch (RemoteException e) {
                    z = z6;
                    j = 0;
                    j2 = 0;
                    j3 = 0;
                    arrayList = null;
                    i2 = i;
                    i3 = i2;
                    long j4 = j2;
                    long j5 = j3;
                    if (arrayList == null) {
                    }
                    if (arrayList != null) {
                    }
                    while (i4 < this.mMergedItems.size()) {
                    }
                    synchronized (this.mLock) {
                    }
                }
            }
            long[] processPss = ActivityManager.getService().getProcessPss(iArr3);
            j = 0;
            j2 = 0;
            j3 = 0;
            arrayList = null;
            boolean z8 = z6;
            int i27 = i;
            i2 = i27;
            while (i27 < iArr3.length) {
                try {
                    ProcessItem processItem9 = this.mAllProcessItems.get(i27);
                    long j6 = j;
                    try {
                        z8 |= processItem9.updateSize(context, processPss[i27], this.mSequence);
                        if (processItem9.mCurSeq == this.mSequence) {
                            j3 += processItem9.mSize;
                            jArr = processPss;
                            iArr = iArr3;
                            z = z8;
                            j = j6;
                        } else if (processItem9.mRunningProcessInfo.importance >= 400) {
                            long j7 = j6 + processItem9.mSize;
                            try {
                                if (arrayList != null) {
                                    try {
                                        jArr = processPss;
                                        mergedItem = new MergedItem(processItem9.mUserId);
                                        processItem9.mMergedItem = mergedItem;
                                        processItem9.mMergedItem.mProcess = processItem9;
                                        i2 |= mergedItem.mUserId != this.mMyUserId ? 1 : 0;
                                        arrayList.add(mergedItem);
                                    } catch (RemoteException e2) {
                                        z = z8;
                                        j = j7;
                                        i3 = i2;
                                        long j42 = j2;
                                        long j52 = j3;
                                        if (arrayList == null) {
                                        }
                                        if (arrayList != null) {
                                        }
                                        while (i4 < this.mMergedItems.size()) {
                                        }
                                        synchronized (this.mLock) {
                                        }
                                    }
                                } else {
                                    jArr = processPss;
                                    if (i < this.mBackgroundItems.size() && this.mBackgroundItems.get(i).mProcess == processItem9) {
                                        mergedItem = this.mBackgroundItems.get(i);
                                    }
                                    ArrayList<MergedItem> arrayList7 = new ArrayList<>(i24);
                                    int i28 = i2;
                                    int i29 = 0;
                                    while (i29 < i) {
                                        try {
                                            mergedItem2 = this.mBackgroundItems.get(i29);
                                            iArr2 = iArr3;
                                            z = z8;
                                        } catch (RemoteException e3) {
                                            z = z8;
                                        }
                                        try {
                                            i28 |= mergedItem2.mUserId != this.mMyUserId ? 1 : 0;
                                            arrayList7.add(mergedItem2);
                                            i29++;
                                            iArr3 = iArr2;
                                            z8 = z;
                                        } catch (RemoteException e4) {
                                            i2 = i28;
                                            arrayList = arrayList7;
                                            j = j7;
                                            i3 = i2;
                                            long j422 = j2;
                                            long j522 = j3;
                                            if (arrayList == null) {
                                            }
                                            if (arrayList != null) {
                                            }
                                            while (i4 < this.mMergedItems.size()) {
                                            }
                                            synchronized (this.mLock) {
                                            }
                                        }
                                    }
                                    iArr = iArr3;
                                    z = z8;
                                    MergedItem mergedItem5 = new MergedItem(processItem9.mUserId);
                                    processItem9.mMergedItem = mergedItem5;
                                    processItem9.mMergedItem.mProcess = processItem9;
                                    int i30 = i28 | (mergedItem5.mUserId != this.mMyUserId ? 1 : 0);
                                    arrayList7.add(mergedItem5);
                                    i2 = i30;
                                    arrayList = arrayList7;
                                    mergedItem = mergedItem5;
                                    mergedItem.update(context, true);
                                    mergedItem.updateSize(context);
                                    i++;
                                    j = j7;
                                }
                                mergedItem.update(context, true);
                                mergedItem.updateSize(context);
                                i++;
                                j = j7;
                            } catch (RemoteException e5) {
                                j = j7;
                                i3 = i2;
                                long j4222 = j2;
                                long j5222 = j3;
                                if (arrayList == null) {
                                    ArrayList<MergedItem> arrayList8 = new ArrayList<>(i24);
                                    while (i5 < i24) {
                                    }
                                    arrayList = arrayList8;
                                }
                                if (arrayList != null) {
                                }
                                while (i4 < this.mMergedItems.size()) {
                                }
                                synchronized (this.mLock) {
                                }
                            }
                            iArr = iArr3;
                            z = z8;
                        } else {
                            jArr = processPss;
                            iArr = iArr3;
                            z = z8;
                            try {
                                c = 200;
                                if (processItem9.mRunningProcessInfo.importance <= 200) {
                                    j2 += processItem9.mSize;
                                }
                                j = j6;
                                i27++;
                                processPss = jArr;
                                iArr3 = iArr;
                                z8 = z;
                            } catch (RemoteException e6) {
                                j = j6;
                                i3 = i2;
                                long j42222 = j2;
                                long j52222 = j3;
                                if (arrayList == null) {
                                }
                                if (arrayList != null) {
                                }
                                while (i4 < this.mMergedItems.size()) {
                                }
                                synchronized (this.mLock) {
                                }
                            }
                        }
                        c = 200;
                        i27++;
                        processPss = jArr;
                        iArr3 = iArr;
                        z8 = z;
                    } catch (RemoteException e7) {
                        z = z8;
                    }
                } catch (RemoteException e8) {
                    z = z8;
                }
            }
            i3 = i2;
            z = z8;
        } catch (RemoteException e9) {
            z = z6;
            j = 0;
            j2 = 0;
            j3 = 0;
            arrayList = null;
            i2 = 0;
        }
        long j422222 = j2;
        long j522222 = j3;
        if (arrayList == null && this.mBackgroundItems.size() > i24) {
            ArrayList<MergedItem> arrayList82 = new ArrayList<>(i24);
            for (i5 = 0; i5 < i24; i5++) {
                MergedItem mergedItem6 = this.mBackgroundItems.get(i5);
                i3 |= mergedItem6.mUserId != this.mMyUserId ? 1 : 0;
                arrayList82.add(mergedItem6);
            }
            arrayList = arrayList82;
        }
        if (arrayList != null) {
            arrayList2 = arrayList;
            arrayList = null;
        } else if (i3 == 0) {
            arrayList2 = arrayList;
        } else {
            ArrayList<MergedItem> arrayList9 = new ArrayList<>();
            int size11 = arrayList.size();
            int i31 = 0;
            while (i31 < size11) {
                MergedItem mergedItem7 = arrayList.get(i31);
                int i32 = size11;
                ArrayList<MergedItem> arrayList10 = arrayList;
                if (mergedItem7.mUserId != this.mMyUserId) {
                    addOtherUserItem(context, arrayList9, this.mOtherUserBackgroundItems, mergedItem7);
                } else {
                    arrayList9.add(mergedItem7);
                }
                i31++;
                size11 = i32;
                arrayList = arrayList10;
            }
            arrayList2 = arrayList;
            int size12 = this.mOtherUserBackgroundItems.size();
            int i33 = 0;
            while (i33 < size12) {
                MergedItem valueAt6 = this.mOtherUserBackgroundItems.valueAt(i33);
                ArrayList<MergedItem> arrayList11 = arrayList9;
                if (valueAt6.mCurSeq == this.mSequence) {
                    valueAt6.update(context, true);
                    valueAt6.updateSize(context);
                }
                i33++;
                arrayList9 = arrayList11;
            }
            arrayList = arrayList9;
        }
        for (i4 = 0; i4 < this.mMergedItems.size(); i4++) {
            this.mMergedItems.get(i4).updateSize(context);
        }
        synchronized (this.mLock) {
            this.mNumBackgroundProcesses = i24;
            this.mNumForegroundProcesses = i25;
            this.mNumServiceProcesses = i23;
            this.mBackgroundProcessMemory = j;
            this.mForegroundProcessMemory = j422222;
            this.mServiceProcessMemory = j522222;
            if (arrayList2 != null) {
                this.mBackgroundItems = arrayList2;
                this.mUserBackgroundItems = arrayList;
                if (this.mWatchingBackgroundItems) {
                    z = true;
                }
            }
            if (!this.mHaveData) {
                this.mHaveData = true;
                this.mLock.notifyAll();
            }
        }
        return z;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setWatchingBackgroundItems(boolean z) {
        synchronized (this.mLock) {
            this.mWatchingBackgroundItems = z;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public ArrayList<MergedItem> getCurrentMergedItems() {
        ArrayList<MergedItem> arrayList;
        synchronized (this.mLock) {
            arrayList = this.mMergedItems;
        }
        return arrayList;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public ArrayList<MergedItem> getCurrentBackgroundItems() {
        ArrayList<MergedItem> arrayList;
        synchronized (this.mLock) {
            arrayList = this.mUserBackgroundItems;
        }
        return arrayList;
    }
}
