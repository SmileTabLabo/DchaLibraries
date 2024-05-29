package com.android.systemui.statusbar.phone;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.AlarmManager;
import android.app.SynchronousUserSwitchObserver;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.UserInfo;
import android.hardware.display.WifiDisplayStatus;
import android.media.AudioManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Handler;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;
import com.android.internal.telephony.IccCardConstants;
import com.android.systemui.qs.tiles.DndTile;
import com.android.systemui.qs.tiles.RotationLockTile;
import com.android.systemui.statusbar.policy.BluetoothController;
import com.android.systemui.statusbar.policy.CastController;
import com.android.systemui.statusbar.policy.DataSaverController;
import com.android.systemui.statusbar.policy.HotspotController;
import com.android.systemui.statusbar.policy.RotationLockController;
import com.android.systemui.statusbar.policy.UserInfoController;
import java.util.Iterator;
/* loaded from: a.zip:com/android/systemui/statusbar/phone/PhoneStatusBarPolicy.class */
public class PhoneStatusBarPolicy implements BluetoothController.Callback, RotationLockController.RotationLockControllerCallback, DataSaverController.Listener {
    private static final boolean DEBUG = Log.isLoggable("PhoneStatusBarPolicy", 3);
    private final AlarmManager mAlarmManager;
    private BluetoothController mBluetooth;
    private final CastController mCast;
    private final Context mContext;
    private boolean mCurrentUserSetup;
    private final DataSaverController mDataSaver;
    private final HotspotController mHotspot;
    private final StatusBarIconController mIconController;
    private final RotationLockController mRotationLockController;
    private final String mSlotAlarmClock;
    private final String mSlotBluetooth;
    private final String mSlotCast;
    private final String mSlotDataSaver;
    private final String mSlotHeadset;
    private final String mSlotHotspot;
    private final String mSlotManagedProfile;
    private final String mSlotRotate;
    private final String mSlotTty;
    private final String mSlotVolume;
    private final String mSlotZen;
    private StatusBarKeyguardViewManager mStatusBarKeyguardViewManager;
    private final UserInfoController mUserInfoController;
    private final UserManager mUserManager;
    private boolean mVolumeVisible;
    private int mZen;
    private boolean mZenVisible;
    private final Handler mHandler = new Handler();
    IccCardConstants.State mSimState = IccCardConstants.State.READY;
    private boolean mManagedProfileFocused = false;
    private boolean mManagedProfileIconVisible = false;
    private boolean mManagedProfileInQuietMode = false;
    private final SynchronousUserSwitchObserver mUserSwitchListener = new AnonymousClass1(this);
    private final HotspotController.Callback mHotspotCallback = new HotspotController.Callback(this) { // from class: com.android.systemui.statusbar.phone.PhoneStatusBarPolicy.2
        final PhoneStatusBarPolicy this$0;

        {
            this.this$0 = this;
        }

        @Override // com.android.systemui.statusbar.policy.HotspotController.Callback
        public void onHotspotChanged(boolean z) {
            this.this$0.mIconController.setIconVisibility(this.this$0.mSlotHotspot, z);
        }
    };
    private final CastController.Callback mCastCallback = new CastController.Callback(this) { // from class: com.android.systemui.statusbar.phone.PhoneStatusBarPolicy.3
        final PhoneStatusBarPolicy this$0;

        {
            this.this$0 = this;
        }

        @Override // com.android.systemui.statusbar.policy.CastController.Callback
        public void onCastDevicesChanged() {
            this.this$0.updateCast();
        }

        @Override // com.android.systemui.statusbar.policy.CastController.Callback
        public void onWfdStatusChanged(WifiDisplayStatus wifiDisplayStatus, boolean z) {
        }

        @Override // com.android.systemui.statusbar.policy.CastController.Callback
        public void onWifiP2pDeviceChanged(WifiP2pDevice wifiP2pDevice) {
        }
    };
    BroadcastReceiver mIntentReceiver = new BroadcastReceiver(this) { // from class: com.android.systemui.statusbar.phone.PhoneStatusBarPolicy.4
        final PhoneStatusBarPolicy this$0;

        {
            this.this$0 = this;
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.app.action.NEXT_ALARM_CLOCK_CHANGED")) {
                this.this$0.updateAlarm();
            } else if (action.equals("android.media.RINGER_MODE_CHANGED") || action.equals("android.media.INTERNAL_RINGER_MODE_CHANGED_ACTION")) {
                this.this$0.updateVolumeZen();
            } else if (action.equals("android.intent.action.SIM_STATE_CHANGED")) {
                this.this$0.updateSimState(intent);
            } else if (action.equals("android.telecom.action.CURRENT_TTY_MODE_CHANGED")) {
                this.this$0.updateTTY(intent);
            } else if (action.equals("android.intent.action.MANAGED_PROFILE_AVAILABLE") || action.equals("android.intent.action.MANAGED_PROFILE_UNAVAILABLE") || action.equals("android.intent.action.MANAGED_PROFILE_REMOVED")) {
                this.this$0.updateQuietState();
                this.this$0.updateManagedProfile();
            } else if (action.equals("android.intent.action.HEADSET_PLUG")) {
                this.this$0.updateHeadsetPlug(intent);
            } else if (action.equals("android.intent.action.USER_SWITCHED")) {
                this.this$0.updateAlarm();
                this.this$0.registerAlarmClockChanged(intent.getIntExtra("android.intent.extra.user_handle", -1), true);
            }
        }
    };
    private Runnable mRemoveCastIconRunnable = new Runnable(this) { // from class: com.android.systemui.statusbar.phone.PhoneStatusBarPolicy.5
        final PhoneStatusBarPolicy this$0;

        {
            this.this$0 = this;
        }

        @Override // java.lang.Runnable
        public void run() {
            if (PhoneStatusBarPolicy.DEBUG) {
                Log.v("PhoneStatusBarPolicy", "updateCast: hiding icon NOW");
            }
            this.this$0.mIconController.setIconVisibility(this.this$0.mSlotCast, false);
        }
    };
    private BroadcastReceiver mAlarmIntentReceiver = new BroadcastReceiver(this) { // from class: com.android.systemui.statusbar.phone.PhoneStatusBarPolicy.6
        final PhoneStatusBarPolicy this$0;

        {
            this.this$0 = this;
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("PhoneStatusBarPolicy", "onReceive:" + action);
            if (action.equals("android.app.action.NEXT_ALARM_CLOCK_CHANGED")) {
                this.this$0.updateAlarm();
            }
        }
    };

    /* renamed from: com.android.systemui.statusbar.phone.PhoneStatusBarPolicy$1  reason: invalid class name */
    /* loaded from: a.zip:com/android/systemui/statusbar/phone/PhoneStatusBarPolicy$1.class */
    class AnonymousClass1 extends SynchronousUserSwitchObserver {
        final PhoneStatusBarPolicy this$0;

        AnonymousClass1(PhoneStatusBarPolicy phoneStatusBarPolicy) {
            this.this$0 = phoneStatusBarPolicy;
        }

        public void onForegroundProfileSwitch(int i) {
            this.this$0.mHandler.post(new Runnable(this, i) { // from class: com.android.systemui.statusbar.phone.PhoneStatusBarPolicy.1.3
                final AnonymousClass1 this$1;
                final int val$newProfileId;

                {
                    this.this$1 = this;
                    this.val$newProfileId = i;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$1.this$0.profileChanged(this.val$newProfileId);
                }
            });
        }

        public void onUserSwitchComplete(int i) throws RemoteException {
            this.this$0.mHandler.post(new Runnable(this, i) { // from class: com.android.systemui.statusbar.phone.PhoneStatusBarPolicy.1.2
                final AnonymousClass1 this$1;
                final int val$newUserId;

                {
                    this.this$1 = this;
                    this.val$newUserId = i;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$1.this$0.updateAlarm();
                    this.this$1.this$0.profileChanged(this.val$newUserId);
                    this.this$1.this$0.updateQuietState();
                    this.this$1.this$0.updateManagedProfile();
                }
            });
        }

        public void onUserSwitching(int i) throws RemoteException {
            this.this$0.mHandler.post(new Runnable(this) { // from class: com.android.systemui.statusbar.phone.PhoneStatusBarPolicy.1.1
                final AnonymousClass1 this$1;

                {
                    this.this$1 = this;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$1.this$0.mUserInfoController.reloadUserInfo();
                }
            });
        }
    }

    public PhoneStatusBarPolicy(Context context, StatusBarIconController statusBarIconController, CastController castController, HotspotController hotspotController, UserInfoController userInfoController, BluetoothController bluetoothController, RotationLockController rotationLockController, DataSaverController dataSaverController) {
        this.mContext = context;
        this.mIconController = statusBarIconController;
        this.mCast = castController;
        this.mHotspot = hotspotController;
        this.mBluetooth = bluetoothController;
        this.mBluetooth.addStateChangedCallback(this);
        this.mAlarmManager = (AlarmManager) context.getSystemService("alarm");
        this.mUserInfoController = userInfoController;
        this.mUserManager = (UserManager) this.mContext.getSystemService("user");
        this.mRotationLockController = rotationLockController;
        this.mDataSaver = dataSaverController;
        this.mSlotCast = context.getString(17039392);
        this.mSlotHotspot = context.getString(17039393);
        this.mSlotBluetooth = context.getString(17039395);
        this.mSlotTty = context.getString(17039397);
        this.mSlotZen = context.getString(17039399);
        this.mSlotVolume = context.getString(17039401);
        this.mSlotAlarmClock = context.getString(17039408);
        this.mSlotManagedProfile = context.getString(17039388);
        this.mSlotRotate = context.getString(17039385);
        this.mSlotHeadset = context.getString(17039386);
        this.mSlotDataSaver = context.getString(17039387);
        this.mRotationLockController.addRotationLockControllerCallback(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.media.RINGER_MODE_CHANGED");
        intentFilter.addAction("android.media.INTERNAL_RINGER_MODE_CHANGED_ACTION");
        intentFilter.addAction("android.intent.action.HEADSET_PLUG");
        intentFilter.addAction("android.intent.action.SIM_STATE_CHANGED");
        intentFilter.addAction("android.telecom.action.CURRENT_TTY_MODE_CHANGED");
        intentFilter.addAction("android.intent.action.MANAGED_PROFILE_AVAILABLE");
        intentFilter.addAction("android.intent.action.MANAGED_PROFILE_UNAVAILABLE");
        intentFilter.addAction("android.intent.action.USER_SWITCHED");
        intentFilter.addAction("android.intent.action.MANAGED_PROFILE_REMOVED");
        this.mContext.registerReceiver(this.mIntentReceiver, intentFilter, null, this.mHandler);
        registerAlarmClockChanged(0, false);
        try {
            ActivityManagerNative.getDefault().registerUserSwitchObserver(this.mUserSwitchListener);
        } catch (RemoteException e) {
        }
        this.mIconController.setIcon(this.mSlotTty, 2130838306, null);
        this.mIconController.setIconVisibility(this.mSlotTty, false);
        updateBluetooth();
        this.mIconController.setIcon(this.mSlotAlarmClock, 2130838253, null);
        this.mIconController.setIconVisibility(this.mSlotAlarmClock, false);
        this.mIconController.setIcon(this.mSlotZen, 2130838333, null);
        this.mIconController.setIconVisibility(this.mSlotZen, false);
        this.mIconController.setIcon(this.mSlotVolume, 2130838290, null);
        this.mIconController.setIconVisibility(this.mSlotVolume, false);
        updateVolumeZen();
        if (this.mCast != null) {
            this.mIconController.setIcon(this.mSlotCast, 2130838257, null);
            this.mIconController.setIconVisibility(this.mSlotCast, false);
            this.mCast.addCallback(this.mCastCallback);
        }
        this.mIconController.setIcon(this.mSlotHotspot, 2130838277, this.mContext.getString(2131493729));
        this.mIconController.setIconVisibility(this.mSlotHotspot, this.mHotspot.isHotspotEnabled());
        this.mHotspot.addCallback(this.mHotspotCallback);
        this.mIconController.setIcon(this.mSlotManagedProfile, 2130838281, this.mContext.getString(2131493730));
        this.mIconController.setIconVisibility(this.mSlotManagedProfile, this.mManagedProfileIconVisible);
        this.mIconController.setIcon(this.mSlotDataSaver, 2130838270, context.getString(2131493853));
        this.mIconController.setIconVisibility(this.mSlotDataSaver, false);
        this.mDataSaver.addListener(this);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void profileChanged(int i) {
        UserInfo userInfo = null;
        if (i == -2) {
            try {
                userInfo = ActivityManagerNative.getDefault().getCurrentUser();
            } catch (RemoteException e) {
            }
        } else {
            userInfo = this.mUserManager.getUserInfo(i);
        }
        this.mManagedProfileFocused = userInfo != null ? userInfo.isManagedProfile() : false;
        if (DEBUG) {
            Log.v("PhoneStatusBarPolicy", "profileChanged: mManagedProfileFocused: " + this.mManagedProfileFocused);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void registerAlarmClockChanged(int i, boolean z) {
        if (z) {
            this.mContext.unregisterReceiver(this.mAlarmIntentReceiver);
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.app.action.NEXT_ALARM_CLOCK_CHANGED");
        Log.d("PhoneStatusBarPolicy", "registerAlarmClockChanged:" + i);
        this.mContext.registerReceiverAsUser(this.mAlarmIntentReceiver, new UserHandle(i), intentFilter, null, this.mHandler);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateAlarm() {
        AlarmManager.AlarmClockInfo nextAlarmClock = this.mAlarmManager.getNextAlarmClock(-2);
        boolean z = nextAlarmClock != null && nextAlarmClock.getTriggerTime() > 0;
        this.mIconController.setIcon(this.mSlotAlarmClock, this.mZen == 2 ? 2130838254 : 2130838253, null);
        StatusBarIconController statusBarIconController = this.mIconController;
        String str = this.mSlotAlarmClock;
        if (!this.mCurrentUserSetup) {
            z = false;
        }
        statusBarIconController.setIconVisibility(str, z);
    }

    private final void updateBluetooth() {
        String string = this.mContext.getString(2131493468);
        boolean z = false;
        String str = string;
        int i = 2130838258;
        if (this.mBluetooth != null) {
            boolean isBluetoothEnabled = this.mBluetooth.isBluetoothEnabled();
            z = isBluetoothEnabled;
            str = string;
            i = 2130838258;
            if (this.mBluetooth.isBluetoothConnected()) {
                i = 2130838259;
                str = this.mContext.getString(2131493366);
                z = isBluetoothEnabled;
            }
        }
        this.mIconController.setIcon(this.mSlotBluetooth, i, str);
        this.mIconController.setIconVisibility(this.mSlotBluetooth, z);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateCast() {
        boolean z;
        CastController.CastDevice castDevice;
        Iterator<T> it = this.mCast.getCastDevices().iterator();
        do {
            z = false;
            if (!it.hasNext()) {
                break;
            }
            castDevice = (CastController.CastDevice) it.next();
            if (castDevice.state == 1) {
                break;
            }
        } while (castDevice.state != 2);
        z = true;
        if (DEBUG) {
            Log.v("PhoneStatusBarPolicy", "updateCast: isCasting: " + z);
        }
        this.mHandler.removeCallbacks(this.mRemoveCastIconRunnable);
        if (z) {
            this.mIconController.setIcon(this.mSlotCast, 2130838257, this.mContext.getString(2131493434));
            this.mIconController.setIconVisibility(this.mSlotCast, true);
            return;
        }
        if (DEBUG) {
            Log.v("PhoneStatusBarPolicy", "updateCast: hiding icon in 3 sec...");
        }
        this.mHandler.postDelayed(this.mRemoveCastIconRunnable, 3000L);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateHeadsetPlug(Intent intent) {
        boolean z = intent.getIntExtra("state", 0) != 0;
        boolean z2 = intent.getIntExtra("microphone", 0) != 0;
        Log.d("PhoneStatusBarPolicy", "updateHeadsetPlug connected:" + z + ",hasMic:" + z2);
        if (!z) {
            this.mIconController.setIconVisibility(this.mSlotHeadset, false);
            return;
        }
        this.mIconController.setIcon(this.mSlotHeadset, z2 ? 2130837654 : 2130837653, this.mContext.getString(z2 ? 2131493851 : 2131493850));
        this.mIconController.setIconVisibility(this.mSlotHeadset, true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateManagedProfile() {
        boolean z;
        if (DEBUG) {
            Log.v("PhoneStatusBarPolicy", "updateManagedProfile: mManagedProfileFocused: " + this.mManagedProfileFocused);
        }
        if (this.mManagedProfileFocused && !this.mStatusBarKeyguardViewManager.isShowing()) {
            z = true;
            this.mIconController.setIcon(this.mSlotManagedProfile, 2130838281, this.mContext.getString(2131493730));
        } else if (this.mManagedProfileInQuietMode) {
            z = true;
            this.mIconController.setIcon(this.mSlotManagedProfile, 2130838282, this.mContext.getString(2131493730));
        } else {
            z = false;
        }
        if (this.mManagedProfileIconVisible != z) {
            this.mIconController.setIconVisibility(this.mSlotManagedProfile, z);
            this.mManagedProfileIconVisible = z;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateQuietState() {
        this.mManagedProfileInQuietMode = false;
        for (UserInfo userInfo : this.mUserManager.getEnabledProfiles(ActivityManager.getCurrentUser())) {
            if (userInfo.isManagedProfile() && userInfo.isQuietModeEnabled()) {
                this.mManagedProfileInQuietMode = true;
                return;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void updateSimState(Intent intent) {
        String stringExtra = intent.getStringExtra("ss");
        if ("ABSENT".equals(stringExtra)) {
            this.mSimState = IccCardConstants.State.ABSENT;
        } else if ("CARD_IO_ERROR".equals(stringExtra)) {
            this.mSimState = IccCardConstants.State.CARD_IO_ERROR;
        } else if ("READY".equals(stringExtra)) {
            this.mSimState = IccCardConstants.State.READY;
        } else if (!"LOCKED".equals(stringExtra)) {
            this.mSimState = IccCardConstants.State.UNKNOWN;
        } else {
            String stringExtra2 = intent.getStringExtra("reason");
            if ("PIN".equals(stringExtra2)) {
                this.mSimState = IccCardConstants.State.PIN_REQUIRED;
            } else if ("PUK".equals(stringExtra2)) {
                this.mSimState = IccCardConstants.State.PUK_REQUIRED;
            } else {
                this.mSimState = IccCardConstants.State.NETWORK_LOCKED;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void updateVolumeZen() {
        String str;
        int i;
        boolean z;
        AudioManager audioManager = (AudioManager) this.mContext.getSystemService("audio");
        boolean z2 = false;
        int i2 = 0;
        String str2 = null;
        if (DndTile.isVisible(this.mContext) || DndTile.isCombinedIcon(this.mContext)) {
            z2 = this.mZen != 0;
            i2 = this.mZen == 2 ? 2130838272 : 2130838271;
            str2 = this.mContext.getString(2131493522);
        } else if (this.mZen == 2) {
            z2 = true;
            i2 = 2130838334;
            str2 = this.mContext.getString(2131493613);
        } else if (this.mZen == 1) {
            z2 = true;
            i2 = 2130838333;
            str2 = this.mContext.getString(2131493614);
        }
        if (DndTile.isVisible(this.mContext) && !DndTile.isCombinedIcon(this.mContext) && audioManager.getRingerModeInternal() == 0) {
            z = true;
            i = 2130838289;
            str = this.mContext.getString(2131493433);
        } else {
            str = null;
            i = 0;
            z = false;
            if (this.mZen != 2) {
                str = null;
                i = 0;
                z = false;
                if (this.mZen != 3) {
                    str = null;
                    i = 0;
                    z = false;
                    if (audioManager.getRingerModeInternal() == 1) {
                        z = true;
                        i = 2130838290;
                        str = this.mContext.getString(2131493432);
                    }
                }
            }
        }
        if (z2) {
            this.mIconController.setIcon(this.mSlotZen, i2, str2);
        }
        if (z2 != this.mZenVisible) {
            this.mIconController.setIconVisibility(this.mSlotZen, z2);
            this.mZenVisible = z2;
        }
        if (z) {
            this.mIconController.setIcon(this.mSlotVolume, i, str);
        }
        if (z != this.mVolumeVisible) {
            this.mIconController.setIconVisibility(this.mSlotVolume, z);
            this.mVolumeVisible = z;
        }
        updateAlarm();
    }

    public void appTransitionStarting(long j, long j2) {
        updateManagedProfile();
    }

    public void notifyKeyguardShowingChanged() {
        updateManagedProfile();
    }

    @Override // com.android.systemui.statusbar.policy.BluetoothController.Callback
    public void onBluetoothDevicesChanged() {
        updateBluetooth();
    }

    @Override // com.android.systemui.statusbar.policy.BluetoothController.Callback
    public void onBluetoothStateChange(boolean z) {
        updateBluetooth();
    }

    @Override // com.android.systemui.statusbar.policy.DataSaverController.Listener
    public void onDataSaverChanged(boolean z) {
        this.mIconController.setIconVisibility(this.mSlotDataSaver, z);
    }

    @Override // com.android.systemui.statusbar.policy.RotationLockController.RotationLockControllerCallback
    public void onRotationLockStateChanged(boolean z, boolean z2) {
        boolean isCurrentOrientationLockPortrait = RotationLockTile.isCurrentOrientationLockPortrait(this.mRotationLockController, this.mContext);
        if (!z) {
            this.mIconController.setIconVisibility(this.mSlotRotate, false);
            return;
        }
        if (isCurrentOrientationLockPortrait) {
            this.mIconController.setIcon(this.mSlotRotate, 2130838292, this.mContext.getString(2131493515));
        } else {
            this.mIconController.setIcon(this.mSlotRotate, 2130838291, this.mContext.getString(2131493514));
        }
        this.mIconController.setIconVisibility(this.mSlotRotate, true);
    }

    public void setCurrentUserSetup(boolean z) {
        if (this.mCurrentUserSetup == z) {
            return;
        }
        this.mCurrentUserSetup = z;
        updateAlarm();
        updateQuietState();
    }

    public void setStatusBarKeyguardViewManager(StatusBarKeyguardViewManager statusBarKeyguardViewManager) {
        this.mStatusBarKeyguardViewManager = statusBarKeyguardViewManager;
    }

    public void setZenMode(int i) {
        this.mZen = i;
        updateVolumeZen();
    }

    protected void updateTTY(Intent intent) {
        boolean z = intent.getIntExtra("android.telecom.intent.extra.CURRENT_TTY_MODE", 0) != 0;
        if (DEBUG) {
            Log.v("PhoneStatusBarPolicy", "updateTTY: enabled: " + z);
        }
        if (!z) {
            if (DEBUG) {
                Log.v("PhoneStatusBarPolicy", "updateTTY: set TTY off");
            }
            this.mIconController.setIconVisibility(this.mSlotTty, false);
            return;
        }
        if (DEBUG) {
            Log.v("PhoneStatusBarPolicy", "updateTTY: set TTY on");
        }
        this.mIconController.setIcon(this.mSlotTty, 2130838306, this.mContext.getString(2131493431));
        this.mIconController.setIconVisibility(this.mSlotTty, true);
    }
}
