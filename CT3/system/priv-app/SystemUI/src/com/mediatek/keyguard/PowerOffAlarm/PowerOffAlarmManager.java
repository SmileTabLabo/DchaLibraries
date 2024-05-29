package com.mediatek.keyguard.PowerOffAlarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.ViewMediatorCallback;
/* loaded from: a.zip:com/mediatek/keyguard/PowerOffAlarm/PowerOffAlarmManager.class */
public class PowerOffAlarmManager {
    private static PowerOffAlarmManager sInstance;
    private Context mContext;
    private LockPatternUtils mLockPatternUtils;
    private ViewMediatorCallback mViewMediatorCallback;
    private boolean mSystemReady = false;
    private boolean mNeedToShowAlarmView = false;
    private final BroadcastReceiver mBroadcastReceiver = new AnonymousClass1(this);
    private Handler mHandler = new AnonymousClass2(this, Looper.myLooper(), null, true);
    private Runnable mSendRemoveIPOWinBroadcastRunnable = new Runnable(this) { // from class: com.mediatek.keyguard.PowerOffAlarm.PowerOffAlarmManager.3
        final PowerOffAlarmManager this$0;

        {
            this.this$0 = this;
        }

        @Override // java.lang.Runnable
        public void run() {
            Log.d("PowerOffAlarmManager", "sendRemoveIPOWinBroadcast ... ");
            this.this$0.mContext.sendBroadcast(new Intent("alarm.boot.remove.ipowin"));
        }
    };

    /* renamed from: com.mediatek.keyguard.PowerOffAlarm.PowerOffAlarmManager$1  reason: invalid class name */
    /* loaded from: a.zip:com/mediatek/keyguard/PowerOffAlarm/PowerOffAlarmManager$1.class */
    class AnonymousClass1 extends BroadcastReceiver {
        final PowerOffAlarmManager this$0;

        AnonymousClass1(PowerOffAlarmManager powerOffAlarmManager) {
            this.this$0 = powerOffAlarmManager;
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.LAUNCH_POWEROFF_ALARM".equals(action)) {
                Log.d("PowerOffAlarmManager", "LAUNCH_PWROFF_ALARM: " + action);
                this.this$0.mHandler.sendEmptyMessageDelayed(115, 1500L);
            } else if ("android.intent.action.normal.boot".equals(action)) {
                Log.d("PowerOffAlarmManager", "NORMAL_BOOT_ACTION: " + action);
                this.this$0.mHandler.sendEmptyMessageDelayed(116, 2500L);
            } else if ("android.intent.action.normal.shutdown".equals(action)) {
                Log.w("PowerOffAlarmManager", "ACTION_SHUTDOWN: " + action);
                this.this$0.mHandler.postDelayed(new Runnable(this) { // from class: com.mediatek.keyguard.PowerOffAlarm.PowerOffAlarmManager.1.1
                    final AnonymousClass1 this$1;

                    {
                        this.this$1 = this;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        this.this$1.this$0.mViewMediatorCallback.hideLocked();
                    }
                }, 1500L);
            }
        }
    }

    /* renamed from: com.mediatek.keyguard.PowerOffAlarm.PowerOffAlarmManager$2  reason: invalid class name */
    /* loaded from: a.zip:com/mediatek/keyguard/PowerOffAlarm/PowerOffAlarmManager$2.class */
    class AnonymousClass2 extends Handler {
        final PowerOffAlarmManager this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        AnonymousClass2(PowerOffAlarmManager powerOffAlarmManager, Looper looper, Handler.Callback callback, boolean z) {
            super(looper, callback, z);
            this.this$0 = powerOffAlarmManager;
        }

        private String getMessageString(Message message) {
            switch (message.what) {
                case 115:
                    return "ALARM_BOOT";
                case 116:
                    return "RESHOW_KEYGUARD_LOCK";
                default:
                    return null;
            }
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            Log.d("PowerOffAlarmManager", "handleMessage enter msg name=" + getMessageString(message));
            switch (message.what) {
                case 115:
                    this.this$0.handleAlarmBoot();
                    break;
                case 116:
                    this.this$0.mViewMediatorCallback.setSuppressPlaySoundFlag();
                    this.this$0.mViewMediatorCallback.hideLocked();
                    postDelayed(new Runnable(this) { // from class: com.mediatek.keyguard.PowerOffAlarm.PowerOffAlarmManager.2.1
                        final AnonymousClass2 this$1;

                        {
                            this.this$1 = this;
                        }

                        @Override // java.lang.Runnable
                        public void run() {
                            if (!this.this$1.this$0.mLockPatternUtils.isLockScreenDisabled(KeyguardUpdateMonitor.getCurrentUser()) || this.this$1.this$0.mViewMediatorCallback.isSecure()) {
                                this.this$1.this$0.mViewMediatorCallback.setSuppressPlaySoundFlag();
                                this.this$1.this$0.mViewMediatorCallback.showLocked(null);
                            }
                        }
                    }, 2000L);
                    postDelayed(new Runnable(this) { // from class: com.mediatek.keyguard.PowerOffAlarm.PowerOffAlarmManager.2.2
                        final AnonymousClass2 this$1;

                        {
                            this.this$1 = this;
                        }

                        @Override // java.lang.Runnable
                        public void run() {
                            this.this$1.this$0.mContext.sendBroadcast(new Intent("android.intent.action.normal.boot.done"));
                        }
                    }, 4000L);
                    break;
            }
            Log.d("PowerOffAlarmManager", "handleMessage exit msg name=" + getMessageString(message));
        }
    }

    public PowerOffAlarmManager(Context context, ViewMediatorCallback viewMediatorCallback, LockPatternUtils lockPatternUtils) {
        this.mContext = context;
        this.mViewMediatorCallback = viewMediatorCallback;
        this.mLockPatternUtils = lockPatternUtils;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.normal.shutdown");
        intentFilter.addAction("android.intent.action.LAUNCH_POWEROFF_ALARM");
        intentFilter.addAction("android.intent.action.normal.boot");
        this.mContext.registerReceiver(this.mBroadcastReceiver, intentFilter);
    }

    public static PowerOffAlarmManager getInstance(Context context, ViewMediatorCallback viewMediatorCallback, LockPatternUtils lockPatternUtils) {
        if (sInstance == null) {
            sInstance = new PowerOffAlarmManager(context, viewMediatorCallback, lockPatternUtils);
        }
        return sInstance;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleAlarmBoot() {
        Log.d("PowerOffAlarmManager", "handleAlarmBoot");
        this.mNeedToShowAlarmView = true;
        maybeShowAlarmView();
    }

    public static boolean isAlarmBoot() {
        String str = SystemProperties.get("sys.boot.reason");
        return str != null && str.equals("1");
    }

    private void maybeShowAlarmView() {
        if (this.mSystemReady && this.mNeedToShowAlarmView) {
            this.mNeedToShowAlarmView = false;
            Log.d("PowerOffAlarmManager", "maybeShowAlarmView start to showLocked");
            if (this.mViewMediatorCallback.isShowing()) {
                this.mViewMediatorCallback.setSuppressPlaySoundFlag();
                this.mViewMediatorCallback.hideLocked();
            }
            this.mViewMediatorCallback.showLocked(null);
        }
    }

    private void startAlarmService() {
        Intent intent = new Intent("com.android.deskclock.START_ALARM");
        intent.putExtra("isAlarmBoot", true);
        intent.setPackage("com.android.deskclock");
        this.mContext.startService(intent);
    }

    public void onSystemReady() {
        this.mSystemReady = true;
        maybeShowAlarmView();
    }

    public void startAlarm() {
        startAlarmService();
        this.mHandler.postDelayed(this.mSendRemoveIPOWinBroadcastRunnable, 1500L);
    }
}
