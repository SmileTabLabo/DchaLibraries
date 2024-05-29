package com.android.settings.applications;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
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
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
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
    long mBackgroundProcessSwapMemory;
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
        public int compare(MergedItem lhs, MergedItem rhs) {
            if (lhs.mUserId != rhs.mUserId) {
                if (lhs.mUserId == RunningState.this.mMyUserId) {
                    return -1;
                }
                return (rhs.mUserId != RunningState.this.mMyUserId && lhs.mUserId < rhs.mUserId) ? -1 : 1;
            } else if (lhs.mProcess == rhs.mProcess) {
                if (lhs.mLabel == rhs.mLabel) {
                    return 0;
                }
                if (lhs.mLabel != null) {
                    return lhs.mLabel.compareTo(rhs.mLabel);
                }
                return -1;
            } else if (lhs.mProcess == null) {
                return -1;
            } else {
                if (rhs.mProcess == null) {
                    return 1;
                }
                ActivityManager.RunningAppProcessInfo lhsInfo = lhs.mProcess.mRunningProcessInfo;
                ActivityManager.RunningAppProcessInfo rhsInfo = rhs.mProcess.mRunningProcessInfo;
                boolean lhsBg = lhsInfo.importance >= 400;
                boolean rhsBg = rhsInfo.importance >= 400;
                if (lhsBg != rhsBg) {
                    return lhsBg ? 1 : -1;
                }
                boolean lhsA = (lhsInfo.flags & 4) != 0;
                boolean rhsA = (rhsInfo.flags & 4) != 0;
                if (lhsA != rhsA) {
                    return lhsA ? -1 : 1;
                } else if (lhsInfo.lru != rhsInfo.lru) {
                    return lhsInfo.lru < rhsInfo.lru ? -1 : 1;
                } else if (lhs.mProcess.mLabel == rhs.mProcess.mLabel) {
                    return 0;
                } else {
                    if (lhs.mProcess.mLabel == null) {
                        return 1;
                    }
                    if (rhs.mProcess.mLabel == null) {
                        return -1;
                    }
                    return lhs.mProcess.mLabel.compareTo(rhs.mProcess.mLabel);
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
        public void handleMessage(Message msg) {
            int i;
            switch (msg.what) {
                case 3:
                    if (msg.arg1 != 0) {
                        i = 2;
                    } else {
                        i = 1;
                    }
                    this.mNextUpdate = i;
                    return;
                case 4:
                    synchronized (RunningState.this.mLock) {
                        if (!RunningState.this.mResumed) {
                            return;
                        }
                        removeMessages(4);
                        Message m = obtainMessage(4);
                        sendMessageDelayed(m, 1000L);
                        if (RunningState.this.mRefreshUiListener == null) {
                            return;
                        }
                        RunningState.this.mRefreshUiListener.onRefreshUi(this.mNextUpdate);
                        this.mNextUpdate = 0;
                        return;
                    }
                default:
                    return;
            }
        }
    };
    private final UserManagerBroadcastReceiver mUmBroadcastReceiver = new UserManagerBroadcastReceiver(this, null);
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

        AppProcessInfo(ActivityManager.RunningAppProcessInfo _info) {
            this.info = _info;
        }
    }

    /* loaded from: classes.dex */
    final class BackgroundHandler extends Handler {
        public BackgroundHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    RunningState.this.reset();
                    return;
                case 2:
                    synchronized (RunningState.this.mLock) {
                        if (!RunningState.this.mResumed) {
                            return;
                        }
                        Message cmd = RunningState.this.mHandler.obtainMessage(3);
                        cmd.arg1 = RunningState.this.update(RunningState.this.mApplicationContext, RunningState.this.mAm) ? 1 : 0;
                        RunningState.this.mHandler.sendMessage(cmd);
                        removeMessages(2);
                        Message msg2 = obtainMessage(2);
                        sendMessageDelayed(msg2, 2000L);
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

        /* synthetic */ UserManagerBroadcastReceiver(RunningState this$0, UserManagerBroadcastReceiver userManagerBroadcastReceiver) {
            this();
        }

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
            boolean oldValue = this.usersChanged;
            this.usersChanged = false;
            return oldValue;
        }

        void register(Context context) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.USER_STOPPED");
            filter.addAction("android.intent.action.USER_STARTED");
            filter.addAction("android.intent.action.USER_INFO_CHANGED");
            context.registerReceiverAsUser(this, UserHandle.ALL, filter, null, null);
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

        public BaseItem(boolean isProcess, int userId) {
            this.mIsProcess = isProcess;
            this.mUserId = userId;
        }

        public Drawable loadIcon(Context context, RunningState state) {
            if (this.mPackageInfo != null) {
                Drawable unbadgedIcon = this.mPackageInfo.loadUnbadgedIcon(state.mPm);
                Drawable icon = state.mPm.getUserBadgedIcon(unbadgedIcon, new UserHandle(this.mUserId));
                return icon;
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

        public ServiceItem(int userId) {
            super(false, userId);
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

        public ProcessItem(Context context, int uid, String processName) {
            super(true, UserHandle.getUserId(uid));
            this.mServices = new HashMap<>();
            this.mDependentProcesses = new SparseArray<>();
            this.mDescription = context.getResources().getString(R.string.service_process_name, processName);
            this.mUid = uid;
            this.mProcessName = processName;
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public void ensureLabel(PackageManager pm) {
            CharSequence nm;
            if (this.mLabel != null) {
                return;
            }
            try {
                ApplicationInfo ai = pm.getApplicationInfo(this.mProcessName, 8192);
                if (ai.uid == this.mUid) {
                    this.mDisplayLabel = ai.loadLabel(pm);
                    this.mLabel = this.mDisplayLabel.toString();
                    this.mPackageInfo = ai;
                    return;
                }
            } catch (PackageManager.NameNotFoundException e) {
            }
            String[] pkgs = pm.getPackagesForUid(this.mUid);
            if (pkgs.length == 1) {
                try {
                    ApplicationInfo ai2 = pm.getApplicationInfo(pkgs[0], 8192);
                    this.mDisplayLabel = ai2.loadLabel(pm);
                    this.mLabel = this.mDisplayLabel.toString();
                    this.mPackageInfo = ai2;
                    return;
                } catch (PackageManager.NameNotFoundException e2) {
                }
            }
            for (String name : pkgs) {
                try {
                    PackageInfo pi = pm.getPackageInfo(name, 0);
                    if (pi.sharedUserLabel != 0 && (nm = pm.getText(name, pi.sharedUserLabel, pi.applicationInfo)) != null) {
                        this.mDisplayLabel = nm;
                        this.mLabel = nm.toString();
                        this.mPackageInfo = pi.applicationInfo;
                        return;
                    }
                } catch (PackageManager.NameNotFoundException e3) {
                }
            }
            if (this.mServices.size() > 0) {
                this.mPackageInfo = this.mServices.values().iterator().next().mServiceInfo.applicationInfo;
                this.mDisplayLabel = this.mPackageInfo.loadLabel(pm);
                this.mLabel = this.mDisplayLabel.toString();
                return;
            }
            try {
                ApplicationInfo ai3 = pm.getApplicationInfo(pkgs[0], 8192);
                this.mDisplayLabel = ai3.loadLabel(pm);
                this.mLabel = this.mDisplayLabel.toString();
                this.mPackageInfo = ai3;
            } catch (PackageManager.NameNotFoundException e4) {
            }
        }

        boolean updateService(Context context, ActivityManager.RunningServiceInfo service) {
            PackageManager pm = context.getPackageManager();
            boolean changed = false;
            ServiceItem si = this.mServices.get(service.service);
            if (si == null) {
                changed = true;
                si = new ServiceItem(this.mUserId);
                si.mRunningService = service;
                try {
                    si.mServiceInfo = ActivityThread.getPackageManager().getServiceInfo(service.service, 8192, UserHandle.getUserId(service.uid));
                    if (si.mServiceInfo == null) {
                        Log.d("RunningService", "getServiceInfo returned null for: " + service.service);
                        return false;
                    }
                } catch (RemoteException e) {
                }
                si.mDisplayLabel = RunningState.makeLabel(pm, si.mRunningService.service.getClassName(), si.mServiceInfo);
                this.mLabel = this.mDisplayLabel != null ? this.mDisplayLabel.toString() : null;
                si.mPackageInfo = si.mServiceInfo.applicationInfo;
                this.mServices.put(service.service, si);
            }
            si.mCurSeq = this.mCurSeq;
            si.mRunningService = service;
            long activeSince = service.restarting == 0 ? service.activeSince : -1L;
            if (si.mActiveSince != activeSince) {
                si.mActiveSince = activeSince;
                changed = true;
            }
            if (service.clientPackage != null && service.clientLabel != 0) {
                if (si.mShownAsStarted) {
                    si.mShownAsStarted = false;
                    changed = true;
                }
                try {
                    Resources clientr = pm.getResourcesForApplication(service.clientPackage);
                    String label = clientr.getString(service.clientLabel);
                    si.mDescription = context.getResources().getString(R.string.service_client_name, label);
                } catch (PackageManager.NameNotFoundException e2) {
                    si.mDescription = null;
                }
            } else {
                if (!si.mShownAsStarted) {
                    si.mShownAsStarted = true;
                    changed = true;
                }
                si.mDescription = context.getResources().getString(R.string.service_started_by_app);
            }
            return changed;
        }

        boolean updateSize(Context context, long pss, int curSeq) {
            this.mSize = 1024 * pss;
            if (this.mCurSeq == curSeq) {
                String sizeStr = Formatter.formatShortFileSize(context, this.mSize);
                if (!sizeStr.equals(this.mSizeStr)) {
                    this.mSizeStr = sizeStr;
                    return false;
                }
            }
            return false;
        }

        boolean buildDependencyChain(Context context, PackageManager pm, int curSeq) {
            int NP = this.mDependentProcesses.size();
            boolean changed = false;
            for (int i = 0; i < NP; i++) {
                ProcessItem proc = this.mDependentProcesses.valueAt(i);
                if (proc.mClient != this) {
                    changed = true;
                    proc.mClient = this;
                }
                proc.mCurSeq = curSeq;
                proc.ensureLabel(pm);
                changed |= proc.buildDependencyChain(context, pm, curSeq);
            }
            if (this.mLastNumDependentProcesses != this.mDependentProcesses.size()) {
                this.mLastNumDependentProcesses = this.mDependentProcesses.size();
                return true;
            }
            return changed;
        }

        void addDependentProcesses(ArrayList<BaseItem> dest, ArrayList<ProcessItem> destProc) {
            int NP = this.mDependentProcesses.size();
            for (int i = 0; i < NP; i++) {
                ProcessItem proc = this.mDependentProcesses.valueAt(i);
                proc.addDependentProcesses(dest, destProc);
                dest.add(proc);
                if (proc.mPid > 0) {
                    destProc.add(proc);
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

        MergedItem(int userId) {
            super(false, userId);
            this.mOtherProcesses = new ArrayList<>();
            this.mServices = new ArrayList<>();
            this.mChildren = new ArrayList<>();
            this.mLastNumProcesses = -1;
            this.mLastNumServices = -1;
        }

        private void setDescription(Context context, int numProcesses, int numServices) {
            if (this.mLastNumProcesses == numProcesses && this.mLastNumServices == numServices) {
                return;
            }
            this.mLastNumProcesses = numProcesses;
            this.mLastNumServices = numServices;
            int resid = R.string.running_processes_item_description_s_s;
            if (numProcesses != 1) {
                if (numServices != 1) {
                    resid = R.string.running_processes_item_description_p_p;
                } else {
                    resid = R.string.running_processes_item_description_p_s;
                }
            } else if (numServices != 1) {
                resid = R.string.running_processes_item_description_s_p;
            }
            this.mDescription = context.getResources().getString(resid, Integer.valueOf(numProcesses), Integer.valueOf(numServices));
        }

        boolean update(Context context, boolean background) {
            this.mBackground = background;
            if (this.mUser != null) {
                MergedItem child0 = this.mChildren.get(0);
                this.mPackageInfo = child0.mProcess.mPackageInfo;
                this.mLabel = this.mUser != null ? this.mUser.mLabel : null;
                this.mDisplayLabel = this.mLabel;
                int numProcesses = 0;
                int numServices = 0;
                this.mActiveSince = -1L;
                for (int i = 0; i < this.mChildren.size(); i++) {
                    MergedItem child = this.mChildren.get(i);
                    numProcesses += child.mLastNumProcesses;
                    numServices += child.mLastNumServices;
                    if (child.mActiveSince >= 0 && this.mActiveSince < child.mActiveSince) {
                        this.mActiveSince = child.mActiveSince;
                    }
                }
                if (!this.mBackground) {
                    setDescription(context, numProcesses, numServices);
                    return false;
                }
                return false;
            }
            this.mPackageInfo = this.mProcess.mPackageInfo;
            this.mDisplayLabel = this.mProcess.mDisplayLabel;
            this.mLabel = this.mProcess.mLabel;
            if (!this.mBackground) {
                setDescription(context, (this.mProcess.mPid > 0 ? 1 : 0) + this.mOtherProcesses.size(), this.mServices.size());
            }
            this.mActiveSince = -1L;
            for (int i2 = 0; i2 < this.mServices.size(); i2++) {
                ServiceItem si = this.mServices.get(i2);
                if (si.mActiveSince >= 0 && this.mActiveSince < si.mActiveSince) {
                    this.mActiveSince = si.mActiveSince;
                }
            }
            return false;
        }

        boolean updateSize(Context context) {
            if (this.mUser != null) {
                this.mSize = 0L;
                for (int i = 0; i < this.mChildren.size(); i++) {
                    MergedItem child = this.mChildren.get(i);
                    child.updateSize(context);
                    this.mSize += child.mSize;
                }
            } else {
                this.mSize = this.mProcess.mSize;
                for (int i2 = 0; i2 < this.mOtherProcesses.size(); i2++) {
                    this.mSize += this.mOtherProcesses.get(i2).mSize;
                }
            }
            String sizeStr = Formatter.formatShortFileSize(context, this.mSize);
            if (sizeStr.equals(this.mSizeStr)) {
                return false;
            }
            this.mSizeStr = sizeStr;
            return false;
        }

        @Override // com.android.settings.applications.RunningState.BaseItem
        public Drawable loadIcon(Context context, RunningState state) {
            if (this.mUser == null) {
                return super.loadIcon(context, state);
            }
            if (this.mUser.mIcon != null) {
                Drawable.ConstantState constState = this.mUser.mIcon.getConstantState();
                if (constState == null) {
                    return this.mUser.mIcon;
                }
                return constState.newDrawable();
            }
            return context.getDrawable(17302453);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public class ServiceProcessComparator implements Comparator<ProcessItem> {
        ServiceProcessComparator() {
        }

        @Override // java.util.Comparator
        public int compare(ProcessItem object1, ProcessItem object2) {
            if (object1.mUserId != object2.mUserId) {
                if (object1.mUserId == RunningState.this.mMyUserId) {
                    return -1;
                }
                return (object2.mUserId != RunningState.this.mMyUserId && object1.mUserId < object2.mUserId) ? -1 : 1;
            } else if (object1.mIsStarted != object2.mIsStarted) {
                return object1.mIsStarted ? -1 : 1;
            } else if (object1.mIsSystem != object2.mIsSystem) {
                return object1.mIsSystem ? 1 : -1;
            } else if (object1.mActiveSince != object2.mActiveSince) {
                return object1.mActiveSince > object2.mActiveSince ? -1 : 1;
            } else {
                return 0;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static CharSequence makeLabel(PackageManager pm, String className, PackageItemInfo item) {
        CharSequence label;
        if (item != null && ((item.labelRes != 0 || item.nonLocalizedLabel != null) && (label = item.loadLabel(pm)) != null)) {
            return label;
        }
        int tail = className.lastIndexOf(46);
        if (tail < 0) {
            return className;
        }
        return className.substring(tail + 1, className.length());
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
    public void resume(OnRefreshUiListener listener) {
        synchronized (this.mLock) {
            this.mResumed = true;
            this.mRefreshUiListener = listener;
            boolean usersChanged = this.mUmBroadcastReceiver.checkUsersChangedLocked();
            boolean configChanged = this.mInterestingConfigChanges.applyNewConfig(this.mApplicationContext.getResources());
            if (usersChanged || configChanged) {
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

    private boolean isInterestingProcess(ActivityManager.RunningAppProcessInfo pi) {
        if ((pi.flags & 1) != 0) {
            return true;
        }
        return (pi.flags & 2) == 0 && pi.importance >= 100 && pi.importance < 170 && pi.importanceReasonCode == 0;
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

    private void addOtherUserItem(Context context, ArrayList<MergedItem> newMergedItems, SparseArray<MergedItem> userItems, MergedItem newItem) {
        boolean first = true;
        MergedItem userItem = userItems.get(newItem.mUserId);
        if (userItem != null && userItem.mCurSeq == this.mSequence) {
            first = false;
        }
        if (first) {
            UserInfo info = this.mUm.getUserInfo(newItem.mUserId);
            if (info == null) {
                return;
            }
            if (this.mHideManagedProfiles && info.isManagedProfile()) {
                return;
            }
            if (userItem == null) {
                userItem = new MergedItem(newItem.mUserId);
                userItems.put(newItem.mUserId, userItem);
            } else {
                userItem.mChildren.clear();
            }
            userItem.mCurSeq = this.mSequence;
            userItem.mUser = new UserState();
            userItem.mUser.mInfo = info;
            userItem.mUser.mIcon = Utils.getUserIcon(context, this.mUm, info);
            userItem.mUser.mLabel = Utils.getUserLabel(context, info);
            newMergedItems.add(userItem);
        }
        userItem.mChildren.add(newItem);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean update(Context context, ActivityManager am) {
        int i;
        MergedItem mergedItem;
        AppProcessInfo ainfo;
        AppProcessInfo ainfo2;
        PackageManager pm = context.getPackageManager();
        this.mSequence++;
        boolean changed = false;
        List<ActivityManager.RunningServiceInfo> services = am.getRunningServices(100);
        int NS = services != null ? services.size() : 0;
        int i2 = 0;
        while (i2 < NS) {
            ActivityManager.RunningServiceInfo si = services.get(i2);
            if (!si.started && si.clientLabel == 0) {
                services.remove(i2);
                i2--;
                NS--;
            } else if ((si.flags & 8) != 0) {
                services.remove(i2);
                i2--;
                NS--;
            }
            i2++;
        }
        List<ActivityManager.RunningAppProcessInfo> processes = am.getRunningAppProcesses();
        int NP = processes != null ? processes.size() : 0;
        this.mTmpAppProcesses.clear();
        for (int i3 = 0; i3 < NP; i3++) {
            ActivityManager.RunningAppProcessInfo pi = processes.get(i3);
            this.mTmpAppProcesses.put(pi.pid, new AppProcessInfo(pi));
        }
        for (int i4 = 0; i4 < NS; i4++) {
            ActivityManager.RunningServiceInfo si2 = services.get(i4);
            if (si2.restarting == 0 && si2.pid > 0 && (ainfo2 = this.mTmpAppProcesses.get(si2.pid)) != null) {
                ainfo2.hasServices = true;
                if (si2.foreground) {
                    ainfo2.hasForegroundServices = true;
                }
            }
        }
        while (i < NS) {
            ActivityManager.RunningServiceInfo si3 = services.get(i);
            if (si3.restarting == 0 && si3.pid > 0 && (ainfo = this.mTmpAppProcesses.get(si3.pid)) != null && !ainfo.hasForegroundServices && ainfo.info.importance < 300) {
                boolean skip = false;
                AppProcessInfo ainfo3 = this.mTmpAppProcesses.get(ainfo.info.importanceReasonPid);
                while (ainfo3 != null) {
                    if (ainfo3.hasServices || isInterestingProcess(ainfo3.info)) {
                        skip = true;
                        break;
                    }
                    ainfo3 = this.mTmpAppProcesses.get(ainfo3.info.importanceReasonPid);
                }
                i = skip ? i + 1 : 0;
            }
            HashMap<String, ProcessItem> procs = this.mServiceProcessesByName.get(si3.uid);
            if (procs == null) {
                procs = new HashMap<>();
                this.mServiceProcessesByName.put(si3.uid, procs);
            }
            ProcessItem proc = procs.get(si3.process);
            if (proc == null) {
                changed = true;
                proc = new ProcessItem(context, si3.uid, si3.process);
                procs.put(si3.process, proc);
            }
            if (proc.mCurSeq != this.mSequence) {
                int pid = si3.restarting == 0 ? si3.pid : 0;
                if (pid != proc.mPid) {
                    changed = true;
                    if (proc.mPid != pid) {
                        if (proc.mPid != 0) {
                            this.mServiceProcessesByPid.remove(proc.mPid);
                        }
                        if (pid != 0) {
                            this.mServiceProcessesByPid.put(pid, proc);
                        }
                        proc.mPid = pid;
                    }
                }
                proc.mDependentProcesses.clear();
                proc.mCurSeq = this.mSequence;
            }
            changed |= proc.updateService(context, si3);
        }
        for (int i5 = 0; i5 < NP; i5++) {
            ActivityManager.RunningAppProcessInfo pi2 = processes.get(i5);
            ProcessItem proc2 = this.mServiceProcessesByPid.get(pi2.pid);
            if (proc2 == null) {
                proc2 = this.mRunningProcesses.get(pi2.pid);
                if (proc2 == null) {
                    changed = true;
                    proc2 = new ProcessItem(context, pi2.uid, pi2.processName);
                    proc2.mPid = pi2.pid;
                    this.mRunningProcesses.put(pi2.pid, proc2);
                }
                proc2.mDependentProcesses.clear();
            }
            if (isInterestingProcess(pi2)) {
                if (!this.mInterestingProcesses.contains(proc2)) {
                    changed = true;
                    this.mInterestingProcesses.add(proc2);
                }
                proc2.mCurSeq = this.mSequence;
                proc2.mInteresting = true;
                proc2.ensureLabel(pm);
            } else {
                proc2.mInteresting = false;
            }
            proc2.mRunningSeq = this.mSequence;
            proc2.mRunningProcessInfo = pi2;
        }
        int NRP = this.mRunningProcesses.size();
        int i6 = 0;
        while (i6 < NRP) {
            ProcessItem proc3 = this.mRunningProcesses.valueAt(i6);
            if (proc3.mRunningSeq == this.mSequence) {
                int clientPid = proc3.mRunningProcessInfo.importanceReasonPid;
                if (clientPid != 0) {
                    ProcessItem client = this.mServiceProcessesByPid.get(clientPid);
                    if (client == null) {
                        client = this.mRunningProcesses.get(clientPid);
                    }
                    if (client != null) {
                        client.mDependentProcesses.put(proc3.mPid, proc3);
                    }
                } else {
                    proc3.mClient = null;
                }
                i6++;
            } else {
                changed = true;
                this.mRunningProcesses.remove(this.mRunningProcesses.keyAt(i6));
                NRP--;
            }
        }
        int NHP = this.mInterestingProcesses.size();
        int i7 = 0;
        while (i7 < NHP) {
            ProcessItem proc4 = this.mInterestingProcesses.get(i7);
            if (!proc4.mInteresting || this.mRunningProcesses.get(proc4.mPid) == null) {
                changed = true;
                this.mInterestingProcesses.remove(i7);
                i7--;
                NHP--;
            }
            i7++;
        }
        int NAP = this.mServiceProcessesByPid.size();
        for (int i8 = 0; i8 < NAP; i8++) {
            ProcessItem proc5 = this.mServiceProcessesByPid.valueAt(i8);
            if (proc5.mCurSeq == this.mSequence) {
                changed |= proc5.buildDependencyChain(context, pm, this.mSequence);
            }
        }
        ArrayList<Integer> uidToDelete = null;
        for (int i9 = 0; i9 < this.mServiceProcessesByName.size(); i9++) {
            HashMap<String, ProcessItem> procs2 = this.mServiceProcessesByName.valueAt(i9);
            Iterator<ProcessItem> pit = procs2.values().iterator();
            while (pit.hasNext()) {
                ProcessItem pi3 = pit.next();
                if (pi3.mCurSeq == this.mSequence) {
                    pi3.ensureLabel(pm);
                    if (pi3.mPid == 0) {
                        pi3.mDependentProcesses.clear();
                    }
                    Iterator<ServiceItem> sit = pi3.mServices.values().iterator();
                    while (sit.hasNext()) {
                        if (sit.next().mCurSeq != this.mSequence) {
                            changed = true;
                            sit.remove();
                        }
                    }
                } else {
                    changed = true;
                    pit.remove();
                    if (procs2.size() == 0) {
                        if (uidToDelete == null) {
                            uidToDelete = new ArrayList<>();
                        }
                        uidToDelete.add(Integer.valueOf(this.mServiceProcessesByName.keyAt(i9)));
                    }
                    if (pi3.mPid != 0) {
                        this.mServiceProcessesByPid.remove(pi3.mPid);
                    }
                }
            }
        }
        if (uidToDelete != null) {
            for (int i10 = 0; i10 < uidToDelete.size(); i10++) {
                int uid = uidToDelete.get(i10).intValue();
                this.mServiceProcessesByName.remove(uid);
            }
        }
        if (changed) {
            ArrayList<ProcessItem> sortedProcesses = new ArrayList<>();
            for (int i11 = 0; i11 < this.mServiceProcessesByName.size(); i11++) {
                for (ProcessItem pi4 : this.mServiceProcessesByName.valueAt(i11).values()) {
                    pi4.mIsSystem = false;
                    pi4.mIsStarted = true;
                    pi4.mActiveSince = Long.MAX_VALUE;
                    for (ServiceItem si4 : pi4.mServices.values()) {
                        if (si4.mServiceInfo != null && (si4.mServiceInfo.applicationInfo.flags & 1) != 0) {
                            pi4.mIsSystem = true;
                        }
                        if (si4.mRunningService != null && si4.mRunningService.clientLabel != 0) {
                            pi4.mIsStarted = false;
                            if (pi4.mActiveSince > si4.mRunningService.activeSince) {
                                pi4.mActiveSince = si4.mRunningService.activeSince;
                            }
                        }
                    }
                    sortedProcesses.add(pi4);
                }
            }
            Collections.sort(sortedProcesses, this.mServiceProcessComparator);
            ArrayList<BaseItem> newItems = new ArrayList<>();
            ArrayList<MergedItem> newMergedItems = new ArrayList<>();
            this.mProcessItems.clear();
            for (int i12 = 0; i12 < sortedProcesses.size(); i12++) {
                ProcessItem pi5 = sortedProcesses.get(i12);
                pi5.mNeedDivider = false;
                int firstProc = this.mProcessItems.size();
                pi5.addDependentProcesses(newItems, this.mProcessItems);
                newItems.add(pi5);
                if (pi5.mPid > 0) {
                    this.mProcessItems.add(pi5);
                }
                MergedItem mergedItem2 = null;
                boolean haveAllMerged = false;
                boolean needDivider = false;
                for (ServiceItem si5 : pi5.mServices.values()) {
                    si5.mNeedDivider = needDivider;
                    needDivider = true;
                    newItems.add(si5);
                    if (si5.mMergedItem != null) {
                        if (mergedItem2 != null && mergedItem2 != si5.mMergedItem) {
                            haveAllMerged = false;
                        }
                        mergedItem2 = si5.mMergedItem;
                    } else {
                        haveAllMerged = false;
                    }
                }
                if (!haveAllMerged || mergedItem2 == null || mergedItem2.mServices.size() != pi5.mServices.size()) {
                    mergedItem2 = new MergedItem(pi5.mUserId);
                    for (ServiceItem si6 : pi5.mServices.values()) {
                        mergedItem2.mServices.add(si6);
                        si6.mMergedItem = mergedItem2;
                    }
                    mergedItem2.mProcess = pi5;
                    mergedItem2.mOtherProcesses.clear();
                    for (int mpi = firstProc; mpi < this.mProcessItems.size() - 1; mpi++) {
                        mergedItem2.mOtherProcesses.add(this.mProcessItems.get(mpi));
                    }
                }
                mergedItem2.update(context, false);
                if (mergedItem2.mUserId != this.mMyUserId) {
                    addOtherUserItem(context, newMergedItems, this.mOtherUserMergedItems, mergedItem2);
                } else {
                    newMergedItems.add(mergedItem2);
                }
            }
            int NHP2 = this.mInterestingProcesses.size();
            for (int i13 = 0; i13 < NHP2; i13++) {
                ProcessItem proc6 = this.mInterestingProcesses.get(i13);
                if (proc6.mClient == null && proc6.mServices.size() <= 0) {
                    if (proc6.mMergedItem == null) {
                        proc6.mMergedItem = new MergedItem(proc6.mUserId);
                        proc6.mMergedItem.mProcess = proc6;
                    }
                    proc6.mMergedItem.update(context, false);
                    if (proc6.mMergedItem.mUserId != this.mMyUserId) {
                        addOtherUserItem(context, newMergedItems, this.mOtherUserMergedItems, proc6.mMergedItem);
                    } else {
                        newMergedItems.add(0, proc6.mMergedItem);
                    }
                    this.mProcessItems.add(proc6);
                }
            }
            int NU = this.mOtherUserMergedItems.size();
            for (int i14 = 0; i14 < NU; i14++) {
                MergedItem user = this.mOtherUserMergedItems.valueAt(i14);
                if (user.mCurSeq == this.mSequence) {
                    user.update(context, false);
                }
            }
            synchronized (this.mLock) {
                this.mItems = newItems;
                this.mMergedItems = newMergedItems;
            }
        }
        this.mAllProcessItems.clear();
        this.mAllProcessItems.addAll(this.mProcessItems);
        int numBackgroundProcesses = 0;
        int numForegroundProcesses = 0;
        int numServiceProcesses = 0;
        int NRP2 = this.mRunningProcesses.size();
        for (int i15 = 0; i15 < NRP2; i15++) {
            ProcessItem proc7 = this.mRunningProcesses.valueAt(i15);
            if (proc7.mCurSeq == this.mSequence) {
                numServiceProcesses++;
            } else if (proc7.mRunningProcessInfo.importance >= 400) {
                numBackgroundProcesses++;
                this.mAllProcessItems.add(proc7);
            } else if (proc7.mRunningProcessInfo.importance <= 200) {
                numForegroundProcesses++;
                this.mAllProcessItems.add(proc7);
            } else {
                Log.i("RunningState", "Unknown non-service process: " + proc7.mProcessName + " #" + proc7.mPid);
            }
        }
        long backgroundProcessMemory = 0;
        long backgroundProcessSwapMemory = 0;
        long foregroundProcessMemory = 0;
        long serviceProcessMemory = 0;
        ArrayList<MergedItem> newBackgroundItems = null;
        ArrayList<MergedItem> newUserBackgroundItems = null;
        boolean diffUsers = false;
        try {
            int numProc = this.mAllProcessItems.size();
            int[] pids = new int[numProc];
            for (int i16 = 0; i16 < numProc; i16++) {
                pids[i16] = this.mAllProcessItems.get(i16).mPid;
            }
            long[] pss = ActivityManagerNative.getDefault().getProcessPss(pids);
            long[] pswap = ActivityManagerNative.getDefault().getProcessPswap(pids);
            float zramCompressRatio = Process.getZramCompressRatio();
            int bgIndex = 0;
            int i17 = 0;
            ArrayList<MergedItem> newBackgroundItems2 = null;
            while (true) {
                try {
                    if (i17 >= pids.length) {
                        break;
                    }
                    ProcessItem proc8 = this.mAllProcessItems.get(i17);
                    long procSize = ((float) pss[i17]) + (((float) pswap[i17]) / zramCompressRatio);
                    changed |= proc8.updateSize(context, procSize, this.mSequence);
                    if (proc8.mCurSeq == this.mSequence) {
                        serviceProcessMemory += proc8.mSize;
                        newBackgroundItems = newBackgroundItems2;
                    } else if (proc8.mRunningProcessInfo.importance >= 400) {
                        backgroundProcessMemory += proc8.mSize;
                        backgroundProcessSwapMemory += pswap[i17] * 1024;
                        if (newBackgroundItems2 != null) {
                            mergedItem = new MergedItem(proc8.mUserId);
                            proc8.mMergedItem = mergedItem;
                            proc8.mMergedItem.mProcess = proc8;
                            diffUsers |= mergedItem.mUserId != this.mMyUserId;
                            newBackgroundItems2.add(mergedItem);
                            newBackgroundItems = newBackgroundItems2;
                        } else if (bgIndex >= this.mBackgroundItems.size() || this.mBackgroundItems.get(bgIndex).mProcess != proc8) {
                            newBackgroundItems = new ArrayList<>(numBackgroundProcesses);
                            for (int bgi = 0; bgi < bgIndex; bgi++) {
                                MergedItem mergedItem3 = this.mBackgroundItems.get(bgi);
                                diffUsers |= mergedItem3.mUserId != this.mMyUserId;
                                newBackgroundItems.add(mergedItem3);
                            }
                            mergedItem = new MergedItem(proc8.mUserId);
                            proc8.mMergedItem = mergedItem;
                            proc8.mMergedItem.mProcess = proc8;
                            diffUsers |= mergedItem.mUserId != this.mMyUserId;
                            newBackgroundItems.add(mergedItem);
                        } else {
                            mergedItem = this.mBackgroundItems.get(bgIndex);
                            newBackgroundItems = newBackgroundItems2;
                        }
                        mergedItem.update(context, true);
                        mergedItem.updateSize(context);
                        bgIndex++;
                    } else if (proc8.mRunningProcessInfo.importance <= 200) {
                        foregroundProcessMemory += proc8.mSize;
                        newBackgroundItems = newBackgroundItems2;
                    } else {
                        newBackgroundItems = newBackgroundItems2;
                    }
                    i17++;
                    newBackgroundItems2 = newBackgroundItems;
                } catch (RemoteException e) {
                    newBackgroundItems = newBackgroundItems2;
                }
            }
            newBackgroundItems = newBackgroundItems2;
        } catch (RemoteException e2) {
        }
        if (newBackgroundItems == null && this.mBackgroundItems.size() > numBackgroundProcesses) {
            newBackgroundItems = new ArrayList<>(numBackgroundProcesses);
            for (int bgi2 = 0; bgi2 < numBackgroundProcesses; bgi2++) {
                MergedItem mergedItem4 = this.mBackgroundItems.get(bgi2);
                diffUsers |= mergedItem4.mUserId != this.mMyUserId;
                newBackgroundItems.add(mergedItem4);
            }
        }
        if (newBackgroundItems != null) {
            if (diffUsers) {
                newUserBackgroundItems = new ArrayList<>();
                int NB = newBackgroundItems.size();
                for (int i18 = 0; i18 < NB; i18++) {
                    MergedItem mergedItem5 = newBackgroundItems.get(i18);
                    if (mergedItem5.mUserId != this.mMyUserId) {
                        addOtherUserItem(context, newUserBackgroundItems, this.mOtherUserBackgroundItems, mergedItem5);
                    } else {
                        newUserBackgroundItems.add(mergedItem5);
                    }
                }
                int NU2 = this.mOtherUserBackgroundItems.size();
                for (int i19 = 0; i19 < NU2; i19++) {
                    MergedItem user2 = this.mOtherUserBackgroundItems.valueAt(i19);
                    if (user2.mCurSeq == this.mSequence) {
                        user2.update(context, true);
                        user2.updateSize(context);
                    }
                }
            } else {
                newUserBackgroundItems = newBackgroundItems;
            }
        }
        for (int i20 = 0; i20 < this.mMergedItems.size(); i20++) {
            this.mMergedItems.get(i20).updateSize(context);
        }
        synchronized (this.mLock) {
            this.mNumBackgroundProcesses = numBackgroundProcesses;
            this.mNumForegroundProcesses = numForegroundProcesses;
            this.mNumServiceProcesses = numServiceProcesses;
            this.mBackgroundProcessMemory = backgroundProcessMemory;
            this.mBackgroundProcessSwapMemory = backgroundProcessSwapMemory;
            this.mForegroundProcessMemory = foregroundProcessMemory;
            this.mServiceProcessMemory = serviceProcessMemory;
            if (newBackgroundItems != null) {
                this.mBackgroundItems = newBackgroundItems;
                this.mUserBackgroundItems = newUserBackgroundItems;
                if (this.mWatchingBackgroundItems) {
                    changed = true;
                }
            }
            if (!this.mHaveData) {
                this.mHaveData = true;
                this.mLock.notifyAll();
            }
        }
        return changed;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setWatchingBackgroundItems(boolean watching) {
        synchronized (this.mLock) {
            this.mWatchingBackgroundItems = watching;
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
