package com.android.launcher3.testing;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.util.LongSparseArray;
import java.util.ArrayList;
/* loaded from: a.zip:com/android/launcher3/testing/MemoryTracker.class */
public class MemoryTracker extends Service {
    public static final String TAG = MemoryTracker.class.getSimpleName();
    ActivityManager mAm;
    public final LongSparseArray<ProcessMemInfo> mData = new LongSparseArray<>();
    public final ArrayList<Long> mPids = new ArrayList<>();
    private int[] mPidsArray = new int[0];
    private final Object mLock = new Object();
    Handler mHandler = new Handler(this) { // from class: com.android.launcher3.testing.MemoryTracker.1
        final MemoryTracker this$0;

        {
            this.this$0 = this;
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    this.this$0.mHandler.removeMessages(3);
                    this.this$0.mHandler.sendEmptyMessage(3);
                    return;
                case 2:
                    this.this$0.mHandler.removeMessages(3);
                    return;
                case 3:
                    this.this$0.update();
                    this.this$0.mHandler.removeMessages(3);
                    this.this$0.mHandler.sendEmptyMessageDelayed(3, 5000L);
                    return;
                default:
                    return;
            }
        }
    };
    private final IBinder mBinder = new MemoryTrackerInterface(this);

    /* loaded from: a.zip:com/android/launcher3/testing/MemoryTracker$MemoryTrackerInterface.class */
    public class MemoryTrackerInterface extends Binder {
        final MemoryTracker this$0;

        public MemoryTrackerInterface(MemoryTracker memoryTracker) {
            this.this$0 = memoryTracker;
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public MemoryTracker getService() {
            return this.this$0;
        }
    }

    /* loaded from: a.zip:com/android/launcher3/testing/MemoryTracker$ProcessMemInfo.class */
    public static class ProcessMemInfo {
        public long currentPss;
        public long currentUss;
        public String name;
        public int pid;
        public long startTime;
        public long[] pss = new long[256];
        public long[] uss = new long[256];
        public long max = 1;
        public int head = 0;

        public ProcessMemInfo(int i, String str, long j) {
            this.pid = i;
            this.name = str;
            this.startTime = j;
        }

        public long getUptime() {
            return System.currentTimeMillis() - this.startTime;
        }
    }

    public ProcessMemInfo getMemInfo(int i) {
        return this.mData.get(i);
    }

    public int[] getTrackedProcesses() {
        return this.mPidsArray;
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        this.mHandler.sendEmptyMessage(1);
        return this.mBinder;
    }

    @Override // android.app.Service
    public void onCreate() {
        this.mAm = (ActivityManager) getSystemService("activity");
        for (ActivityManager.RunningServiceInfo runningServiceInfo : this.mAm.getRunningServices(256)) {
            if (runningServiceInfo.service.getPackageName().equals(getPackageName())) {
                Log.v(TAG, "discovered running service: " + runningServiceInfo.process + " (" + runningServiceInfo.pid + ")");
                startTrackingProcess(runningServiceInfo.pid, runningServiceInfo.process, System.currentTimeMillis() - (SystemClock.elapsedRealtime() - runningServiceInfo.activeSince));
            }
        }
        for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : this.mAm.getRunningAppProcesses()) {
            String str = runningAppProcessInfo.processName;
            if (str.startsWith(getPackageName())) {
                Log.v(TAG, "discovered other running process: " + str + " (" + runningAppProcessInfo.pid + ")");
                startTrackingProcess(runningAppProcessInfo.pid, str, System.currentTimeMillis());
            }
        }
    }

    @Override // android.app.Service
    public void onDestroy() {
        this.mHandler.sendEmptyMessage(2);
    }

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int i, int i2) {
        Log.v(TAG, "Received start id " + i2 + ": " + intent);
        if (intent != null && "com.android.launcher3.action.START_TRACKING".equals(intent.getAction())) {
            startTrackingProcess(intent.getIntExtra("pid", -1), intent.getStringExtra("name"), intent.getLongExtra("start", System.currentTimeMillis()));
        }
        this.mHandler.sendEmptyMessage(1);
        return 1;
    }

    public void startTrackingProcess(int i, String str, long j) {
        synchronized (this.mLock) {
            Long valueOf = Long.valueOf(i);
            if (this.mPids.contains(valueOf)) {
                return;
            }
            this.mPids.add(valueOf);
            updatePidsArrayL();
            this.mData.put(i, new ProcessMemInfo(i, str, j));
        }
    }

    void update() {
        synchronized (this.mLock) {
            Debug.MemoryInfo[] processMemoryInfo = this.mAm.getProcessMemoryInfo(this.mPidsArray);
            int i = 0;
            while (true) {
                if (i >= processMemoryInfo.length) {
                    break;
                }
                Debug.MemoryInfo memoryInfo = processMemoryInfo[i];
                if (i > this.mPids.size()) {
                    Log.e(TAG, "update: unknown process info received: " + memoryInfo);
                    break;
                }
                long intValue = this.mPids.get(i).intValue();
                ProcessMemInfo processMemInfo = this.mData.get(intValue);
                processMemInfo.head = (processMemInfo.head + 1) % processMemInfo.pss.length;
                long[] jArr = processMemInfo.pss;
                int i2 = processMemInfo.head;
                int totalPss = memoryInfo.getTotalPss();
                processMemInfo.currentPss = totalPss;
                jArr[i2] = totalPss;
                long[] jArr2 = processMemInfo.uss;
                int i3 = processMemInfo.head;
                int totalPrivateDirty = memoryInfo.getTotalPrivateDirty();
                processMemInfo.currentUss = totalPrivateDirty;
                jArr2[i3] = totalPrivateDirty;
                if (processMemInfo.currentPss > processMemInfo.max) {
                    processMemInfo.max = processMemInfo.currentPss;
                }
                if (processMemInfo.currentUss > processMemInfo.max) {
                    processMemInfo.max = processMemInfo.currentUss;
                }
                if (processMemInfo.currentPss == 0) {
                    Log.v(TAG, "update: pid " + intValue + " has pss=0, it probably died");
                    this.mData.remove(intValue);
                }
                i++;
            }
            for (int size = this.mPids.size() - 1; size >= 0; size--) {
                if (this.mData.get(this.mPids.get(size).intValue()) == null) {
                    this.mPids.remove(size);
                    updatePidsArrayL();
                }
            }
        }
    }

    void updatePidsArrayL() {
        int size = this.mPids.size();
        this.mPidsArray = new int[size];
        StringBuffer stringBuffer = new StringBuffer("Now tracking processes: ");
        for (int i = 0; i < size; i++) {
            int intValue = this.mPids.get(i).intValue();
            this.mPidsArray[i] = intValue;
            stringBuffer.append(intValue);
            stringBuffer.append(" ");
        }
        Log.v(TAG, stringBuffer.toString());
    }
}
