package com.android.systemui.statusbar.phone;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AppGlobals;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.SynchronousUserSwitchObserver;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.graphics.drawable.Icon;
import android.media.AudioManager;
import android.net.Uri;
import android.os.BenesseExtension;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.service.notification.StatusBarNotification;
import android.service.notification.ZenModeConfig;
import android.telecom.TelecomManager;
import android.text.format.DateFormat;
import android.util.ArraySet;
import android.util.Log;
import android.util.Pair;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.IccCardConstants;
import com.android.systemui.Dependency;
import com.android.systemui.DockedStackExistsListener;
import com.android.systemui.R;
import com.android.systemui.SysUiServiceProvider;
import com.android.systemui.UiOffloadThread;
import com.android.systemui.qs.tiles.DndTile;
import com.android.systemui.qs.tiles.RotationLockTile;
import com.android.systemui.recents.misc.SysUiTaskStackChangeListener;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.phone.PhoneStatusBarPolicy;
import com.android.systemui.statusbar.policy.BluetoothController;
import com.android.systemui.statusbar.policy.CastController;
import com.android.systemui.statusbar.policy.DataSaverController;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.android.systemui.statusbar.policy.HotspotController;
import com.android.systemui.statusbar.policy.KeyguardMonitor;
import com.android.systemui.statusbar.policy.LocationController;
import com.android.systemui.statusbar.policy.NextAlarmController;
import com.android.systemui.statusbar.policy.RotationLockController;
import com.android.systemui.statusbar.policy.UserInfoController;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.android.systemui.util.NotificationChannels;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
/* loaded from: classes.dex */
public class PhoneStatusBarPolicy implements CommandQueue.Callbacks, BluetoothController.Callback, DataSaverController.Listener, DeviceProvisionedController.DeviceProvisionedListener, KeyguardMonitor.Callback, LocationController.LocationChangeCallback, RotationLockController.RotationLockControllerCallback, ZenModeController.Callback {
    private static final boolean DEBUG = Log.isLoggable("PhoneStatusBarPolicy", 3);
    private final AlarmManager mAlarmManager;
    private final Context mContext;
    private boolean mCurrentUserSetup;
    private boolean mDockedStackExists;
    private final StatusBarIconController mIconController;
    private AlarmManager.AlarmClockInfo mNextAlarm;
    private final String mSlotAlarmClock;
    private final String mSlotBluetooth;
    private final String mSlotCast;
    private final String mSlotDataSaver;
    private final String mSlotHeadset;
    private final String mSlotHotspot;
    private final String mSlotLocation;
    private final String mSlotManagedProfile;
    private final String mSlotRotate;
    private final String mSlotTty;
    private final String mSlotVolume;
    private final String mSlotZen;
    private final UserManager mUserManager;
    private boolean mVolumeVisible;
    private boolean mZenVisible;
    private final Handler mHandler = new Handler();
    private final ArraySet<Pair<String, Integer>> mCurrentNotifs = new ArraySet<>();
    private final UiOffloadThread mUiOffloadThread = (UiOffloadThread) Dependency.get(UiOffloadThread.class);
    IccCardConstants.State mSimState = IccCardConstants.State.READY;
    private boolean mManagedProfileIconVisible = false;
    private final SynchronousUserSwitchObserver mUserSwitchListener = new AnonymousClass1();
    private final HotspotController.Callback mHotspotCallback = new HotspotController.Callback() { // from class: com.android.systemui.statusbar.phone.PhoneStatusBarPolicy.2
        @Override // com.android.systemui.statusbar.policy.HotspotController.Callback
        public void onHotspotChanged(boolean z, int i) {
            PhoneStatusBarPolicy.this.mIconController.setIconVisibility(PhoneStatusBarPolicy.this.mSlotHotspot, z);
        }
    };
    private final CastController.Callback mCastCallback = new CastController.Callback() { // from class: com.android.systemui.statusbar.phone.PhoneStatusBarPolicy.3
        @Override // com.android.systemui.statusbar.policy.CastController.Callback
        public void onCastDevicesChanged() {
            PhoneStatusBarPolicy.this.updateCast();
        }
    };
    private final NextAlarmController.NextAlarmChangeCallback mNextAlarmCallback = new NextAlarmController.NextAlarmChangeCallback() { // from class: com.android.systemui.statusbar.phone.PhoneStatusBarPolicy.4
        @Override // com.android.systemui.statusbar.policy.NextAlarmController.NextAlarmChangeCallback
        public void onNextAlarmChanged(AlarmManager.AlarmClockInfo alarmClockInfo) {
            PhoneStatusBarPolicy.this.mNextAlarm = alarmClockInfo;
            PhoneStatusBarPolicy.this.updateAlarm();
        }
    };
    private final SysUiTaskStackChangeListener mTaskListener = new SysUiTaskStackChangeListener() { // from class: com.android.systemui.statusbar.phone.PhoneStatusBarPolicy.5
        @Override // com.android.systemui.shared.system.TaskStackChangeListener
        public void onTaskStackChanged() {
            PhoneStatusBarPolicy.this.updateForegroundInstantApps();
        }
    };
    @VisibleForTesting
    BroadcastReceiver mIntentReceiver = new BroadcastReceiver() { // from class: com.android.systemui.statusbar.phone.PhoneStatusBarPolicy.6
        /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            char c;
            String action = intent.getAction();
            switch (action.hashCode()) {
                case -1676458352:
                    if (action.equals("android.intent.action.HEADSET_PLUG")) {
                        c = 7;
                        break;
                    }
                    c = 65535;
                    break;
                case -1238404651:
                    if (action.equals("android.intent.action.MANAGED_PROFILE_UNAVAILABLE")) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                case -998320851:
                    if (action.equals("mediatek.intent.action.EMBMS_SESSION_STATUS_CHANGED")) {
                        c = '\t';
                        break;
                    }
                    c = 65535;
                    break;
                case -864107122:
                    if (action.equals("android.intent.action.MANAGED_PROFILE_AVAILABLE")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case -229777127:
                    if (action.equals("android.intent.action.SIM_STATE_CHANGED")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case 100931828:
                    if (action.equals("android.media.INTERNAL_RINGER_MODE_CHANGED_ACTION")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case 959232034:
                    if (action.equals("android.intent.action.USER_SWITCHED")) {
                        c = '\b';
                        break;
                    }
                    c = 65535;
                    break;
                case 1051344550:
                    if (action.equals("android.telecom.action.CURRENT_TTY_MODE_CHANGED")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case 1051477093:
                    if (action.equals("android.intent.action.MANAGED_PROFILE_REMOVED")) {
                        c = 6;
                        break;
                    }
                    c = 65535;
                    break;
                case 2070024785:
                    if (action.equals("android.media.RINGER_MODE_CHANGED")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                case 1:
                    PhoneStatusBarPolicy.this.updateVolumeZen();
                    return;
                case 2:
                    if (!intent.getBooleanExtra("rebroadcastOnUnlock", false)) {
                        PhoneStatusBarPolicy.this.updateSimState(intent);
                        return;
                    }
                    return;
                case 3:
                    PhoneStatusBarPolicy.this.updateTTY(intent.getIntExtra("android.telecom.intent.extra.CURRENT_TTY_MODE", 0));
                    return;
                case 4:
                case 5:
                case 6:
                    PhoneStatusBarPolicy.this.updateManagedProfile();
                    return;
                case 7:
                    PhoneStatusBarPolicy.this.updateHeadsetPlug(intent);
                    return;
                case '\b':
                    PhoneStatusBarPolicy.this.updateAlarm();
                    PhoneStatusBarPolicy.this.registerAlarmClockChanged(intent.getIntExtra("android.intent.extra.user_handle", -1), true);
                    return;
                case '\t':
                    PhoneStatusBarPolicy.this.updateEmbmsState(intent);
                    return;
                default:
                    return;
            }
        }
    };
    private Runnable mRemoveCastIconRunnable = new Runnable() { // from class: com.android.systemui.statusbar.phone.PhoneStatusBarPolicy.7
        @Override // java.lang.Runnable
        public void run() {
            if (PhoneStatusBarPolicy.DEBUG) {
                Log.v("PhoneStatusBarPolicy", "updateCast: hiding icon NOW");
            }
            PhoneStatusBarPolicy.this.mIconController.setIconVisibility(PhoneStatusBarPolicy.this.mSlotCast, false);
        }
    };
    private BroadcastReceiver mAlarmIntentReceiver = new BroadcastReceiver() { // from class: com.android.systemui.statusbar.phone.PhoneStatusBarPolicy.8
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("PhoneStatusBarPolicy", "onReceive:" + action);
            if (action.equals("android.app.action.NEXT_ALARM_CLOCK_CHANGED")) {
                PhoneStatusBarPolicy.this.updateAlarm();
            }
        }
    };
    private final CastController mCast = (CastController) Dependency.get(CastController.class);
    private final HotspotController mHotspot = (HotspotController) Dependency.get(HotspotController.class);
    private BluetoothController mBluetooth = (BluetoothController) Dependency.get(BluetoothController.class);
    private final NextAlarmController mNextAlarmController = (NextAlarmController) Dependency.get(NextAlarmController.class);
    private final UserInfoController mUserInfoController = (UserInfoController) Dependency.get(UserInfoController.class);
    private final RotationLockController mRotationLockController = (RotationLockController) Dependency.get(RotationLockController.class);
    private final DataSaverController mDataSaver = (DataSaverController) Dependency.get(DataSaverController.class);
    private final ZenModeController mZenController = (ZenModeController) Dependency.get(ZenModeController.class);
    private final DeviceProvisionedController mProvisionedController = (DeviceProvisionedController) Dependency.get(DeviceProvisionedController.class);
    private final KeyguardMonitor mKeyguardMonitor = (KeyguardMonitor) Dependency.get(KeyguardMonitor.class);
    private final LocationController mLocationController = (LocationController) Dependency.get(LocationController.class);
    private final String mSlotEmbms = "embms";

    @VisibleForTesting
    public PhoneStatusBarPolicy(Context context, StatusBarIconController statusBarIconController) {
        StatusBarNotification[] activeNotifications;
        this.mContext = context;
        this.mIconController = statusBarIconController;
        this.mAlarmManager = (AlarmManager) context.getSystemService("alarm");
        this.mUserManager = (UserManager) this.mContext.getSystemService("user");
        this.mSlotCast = context.getString(17040912);
        this.mSlotHotspot = context.getString(17040919);
        this.mSlotBluetooth = context.getString(17040911);
        this.mSlotTty = context.getString(17040933);
        this.mSlotZen = context.getString(17040937);
        this.mSlotVolume = context.getString(17040934);
        this.mSlotAlarmClock = context.getString(17040909);
        this.mSlotManagedProfile = context.getString(17040922);
        this.mSlotRotate = context.getString(17040928);
        this.mSlotHeadset = context.getString(17040918);
        this.mSlotDataSaver = context.getString(17040916);
        this.mSlotLocation = context.getString(17040921);
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
        intentFilter.addAction("mediatek.intent.action.EMBMS_SESSION_STATUS_CHANGED");
        this.mContext.registerReceiver(this.mIntentReceiver, intentFilter, null, this.mHandler);
        registerAlarmClockChanged(0, false);
        try {
            ActivityManager.getService().registerUserSwitchObserver(this.mUserSwitchListener, "PhoneStatusBarPolicy");
        } catch (RemoteException e) {
        }
        updateTTY();
        updateBluetooth();
        this.mIconController.setIcon(this.mSlotEmbms, R.drawable.stat_sys_embms, null);
        this.mIconController.setIconVisibility(this.mSlotEmbms, false);
        this.mIconController.setIcon(this.mSlotAlarmClock, R.drawable.stat_sys_alarm, null);
        this.mIconController.setIconVisibility(this.mSlotAlarmClock, false);
        this.mIconController.setIcon(this.mSlotZen, R.drawable.stat_sys_zen_important, null);
        this.mIconController.setIconVisibility(this.mSlotZen, false);
        this.mIconController.setIcon(this.mSlotVolume, R.drawable.stat_sys_ringer_vibrate, null);
        this.mIconController.setIconVisibility(this.mSlotVolume, false);
        updateVolumeZen();
        this.mIconController.setIcon(this.mSlotCast, R.drawable.stat_sys_cast, null);
        this.mIconController.setIconVisibility(this.mSlotCast, false);
        this.mIconController.setIcon(this.mSlotHotspot, R.drawable.stat_sys_hotspot, this.mContext.getString(R.string.accessibility_status_bar_hotspot));
        this.mIconController.setIconVisibility(this.mSlotHotspot, this.mHotspot.isHotspotEnabled());
        this.mIconController.setIcon(this.mSlotManagedProfile, R.drawable.stat_sys_managed_profile_status, this.mContext.getString(R.string.accessibility_managed_profile));
        this.mIconController.setIconVisibility(this.mSlotManagedProfile, this.mManagedProfileIconVisible);
        this.mIconController.setIcon(this.mSlotDataSaver, R.drawable.stat_sys_data_saver, context.getString(R.string.accessibility_data_saver_on));
        this.mIconController.setIconVisibility(this.mSlotDataSaver, false);
        this.mRotationLockController.addCallback(this);
        this.mBluetooth.addCallback(this);
        this.mProvisionedController.addCallback(this);
        this.mZenController.addCallback(this);
        this.mCast.addCallback(this.mCastCallback);
        this.mHotspot.addCallback(this.mHotspotCallback);
        this.mNextAlarmController.addCallback(this.mNextAlarmCallback);
        this.mDataSaver.addCallback(this);
        this.mKeyguardMonitor.addCallback(this);
        this.mLocationController.addCallback(this);
        ((CommandQueue) SysUiServiceProvider.getComponent(this.mContext, CommandQueue.class)).addCallbacks(this);
        ActivityManagerWrapper.getInstance().registerTaskStackListener(this.mTaskListener);
        NotificationManager notificationManager = (NotificationManager) this.mContext.getSystemService(NotificationManager.class);
        for (StatusBarNotification statusBarNotification : notificationManager.getActiveNotifications()) {
            if (statusBarNotification.getId() == 7) {
                notificationManager.cancel(statusBarNotification.getTag(), statusBarNotification.getId());
            }
        }
        DockedStackExistsListener.register(new Consumer() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$PhoneStatusBarPolicy$eamN1_naA3onhFWH3wlclHRzu6s
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                PhoneStatusBarPolicy.lambda$new$0(PhoneStatusBarPolicy.this, (Boolean) obj);
            }
        });
    }

    public static /* synthetic */ void lambda$new$0(PhoneStatusBarPolicy phoneStatusBarPolicy, Boolean bool) {
        phoneStatusBarPolicy.mDockedStackExists = bool.booleanValue();
        phoneStatusBarPolicy.updateForegroundInstantApps();
    }

    @Override // com.android.systemui.statusbar.policy.ZenModeController.Callback
    public void onZenChanged(int i) {
        updateVolumeZen();
    }

    @Override // com.android.systemui.statusbar.policy.ZenModeController.Callback
    public void onConfigChanged(ZenModeConfig zenModeConfig) {
        updateVolumeZen();
    }

    @Override // com.android.systemui.statusbar.policy.LocationController.LocationChangeCallback
    public void onLocationActiveChanged(boolean z) {
        updateLocation();
    }

    private void updateLocation() {
        if (this.mLocationController.isLocationActive()) {
            this.mIconController.setIcon(this.mSlotLocation, R.drawable.stat_sys_location, this.mContext.getString(R.string.accessibility_location_active));
        } else {
            this.mIconController.removeAllIconsForSlot(this.mSlotLocation);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateAlarm() {
        AlarmManager.AlarmClockInfo nextAlarmClock = this.mAlarmManager.getNextAlarmClock(-2);
        boolean z = false;
        boolean z2 = nextAlarmClock != null && nextAlarmClock.getTriggerTime() > 0;
        this.mIconController.setIcon(this.mSlotAlarmClock, this.mZenController.getZen() == 2 ? R.drawable.stat_sys_alarm_dim : R.drawable.stat_sys_alarm, buildAlarmContentDescription());
        StatusBarIconController statusBarIconController = this.mIconController;
        String str = this.mSlotAlarmClock;
        if (this.mCurrentUserSetup && z2) {
            z = true;
        }
        statusBarIconController.setIconVisibility(str, z);
    }

    private String buildAlarmContentDescription() {
        if (this.mNextAlarm == null) {
            return this.mContext.getString(R.string.status_bar_alarm);
        }
        return formatNextAlarm(this.mNextAlarm, this.mContext);
    }

    private static String formatNextAlarm(AlarmManager.AlarmClockInfo alarmClockInfo, Context context) {
        if (alarmClockInfo == null) {
            return "";
        }
        return context.getString(R.string.accessibility_quick_settings_alarm, DateFormat.format(DateFormat.getBestDateTimePattern(Locale.getDefault(), DateFormat.is24HourFormat(context, ActivityManager.getCurrentUser()) ? "EHm" : "Ehma"), alarmClockInfo.getTriggerTime()).toString());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void updateSimState(Intent intent) {
        String stringExtra = intent.getStringExtra("ss");
        if ("ABSENT".equals(stringExtra)) {
            this.mSimState = IccCardConstants.State.ABSENT;
        } else if ("CARD_IO_ERROR".equals(stringExtra)) {
            this.mSimState = IccCardConstants.State.CARD_IO_ERROR;
        } else if ("CARD_RESTRICTED".equals(stringExtra)) {
            this.mSimState = IccCardConstants.State.CARD_RESTRICTED;
        } else if ("READY".equals(stringExtra)) {
            this.mSimState = IccCardConstants.State.READY;
        } else if ("LOCKED".equals(stringExtra)) {
            String stringExtra2 = intent.getStringExtra("reason");
            if ("PIN".equals(stringExtra2)) {
                this.mSimState = IccCardConstants.State.PIN_REQUIRED;
            } else if ("PUK".equals(stringExtra2)) {
                this.mSimState = IccCardConstants.State.PUK_REQUIRED;
            } else {
                this.mSimState = IccCardConstants.State.NETWORK_LOCKED;
            }
        } else {
            this.mSimState = IccCardConstants.State.UNKNOWN;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Removed duplicated region for block: B:28:0x009d  */
    /* JADX WARN: Removed duplicated region for block: B:31:0x00a8  */
    /* JADX WARN: Removed duplicated region for block: B:33:0x00b3  */
    /* JADX WARN: Removed duplicated region for block: B:36:0x00be  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public final void updateVolumeZen() {
        boolean z;
        int i;
        String str;
        int i2;
        AudioManager audioManager = (AudioManager) this.mContext.getSystemService("audio");
        int zen = this.mZenController.getZen();
        String str2 = null;
        int i3 = 0;
        boolean z2 = true;
        if (DndTile.isVisible(this.mContext) || DndTile.isCombinedIcon(this.mContext)) {
            z = zen != 0;
            String string = this.mContext.getString(R.string.quick_settings_dnd_label);
            i = R.drawable.stat_sys_dnd;
            str = string;
        } else {
            if (zen == 2) {
                i2 = R.drawable.stat_sys_zen_none;
                str = this.mContext.getString(R.string.interruption_level_none);
            } else if (zen == 1) {
                i2 = R.drawable.stat_sys_zen_important;
                str = this.mContext.getString(R.string.interruption_level_priority);
            } else {
                str = null;
                z = false;
                i = 0;
            }
            i = i2;
            z = true;
        }
        if (!ZenModeConfig.isZenOverridingRinger(zen, this.mZenController.getConfig())) {
            if (audioManager.getRingerModeInternal() == 1) {
                i3 = R.drawable.stat_sys_ringer_vibrate;
                str2 = this.mContext.getString(R.string.accessibility_ringer_vibrate);
            } else if (audioManager.getRingerModeInternal() == 0) {
                i3 = R.drawable.stat_sys_ringer_silent;
                str2 = this.mContext.getString(R.string.accessibility_ringer_silent);
            }
            if (z) {
                this.mIconController.setIcon(this.mSlotZen, i, str);
            }
            if (z != this.mZenVisible) {
                this.mIconController.setIconVisibility(this.mSlotZen, z);
                this.mZenVisible = z;
            }
            if (z2) {
                this.mIconController.setIcon(this.mSlotVolume, i3, str2);
            }
            if (z2 != this.mVolumeVisible) {
                this.mIconController.setIconVisibility(this.mSlotVolume, z2);
                this.mVolumeVisible = z2;
            }
            updateAlarm();
        }
        z2 = false;
        if (z) {
        }
        if (z != this.mZenVisible) {
        }
        if (z2) {
        }
        if (z2 != this.mVolumeVisible) {
        }
        updateAlarm();
    }

    @Override // com.android.systemui.statusbar.policy.BluetoothController.Callback
    public void onBluetoothDevicesChanged() {
        updateBluetooth();
    }

    @Override // com.android.systemui.statusbar.policy.BluetoothController.Callback
    public void onBluetoothStateChange(boolean z) {
        updateBluetooth();
    }

    private final void updateBluetooth() {
        boolean z;
        String str;
        int i;
        String string = this.mContext.getString(R.string.accessibility_quick_settings_bluetooth_on);
        if (this.mBluetooth != null && this.mBluetooth.isBluetoothConnected()) {
            i = R.drawable.stat_sys_data_bluetooth_connected;
            str = this.mContext.getString(R.string.accessibility_bluetooth_connected);
            z = this.mBluetooth.isBluetoothEnabled();
        } else {
            z = false;
            str = string;
            i = R.drawable.stat_sys_data_bluetooth;
        }
        this.mIconController.setIcon(this.mSlotBluetooth, i, str);
        this.mIconController.setIconVisibility(this.mSlotBluetooth, z);
    }

    private final void updateTTY() {
        TelecomManager telecomManager = (TelecomManager) this.mContext.getSystemService("telecom");
        if (telecomManager == null) {
            updateTTY(0);
        } else {
            updateTTY(telecomManager.getCurrentTtyMode());
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void updateTTY(int i) {
        boolean z = i != 0;
        if (DEBUG) {
            Log.v("PhoneStatusBarPolicy", "updateTTY: enabled: " + z);
        }
        if (z) {
            if (DEBUG) {
                Log.v("PhoneStatusBarPolicy", "updateTTY: set TTY on");
            }
            this.mIconController.setIcon(this.mSlotTty, R.drawable.stat_sys_tty_mode, this.mContext.getString(R.string.accessibility_tty_enabled));
            this.mIconController.setIconVisibility(this.mSlotTty, true);
            return;
        }
        if (DEBUG) {
            Log.v("PhoneStatusBarPolicy", "updateTTY: set TTY off");
        }
        this.mIconController.setIconVisibility(this.mSlotTty, false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Removed duplicated region for block: B:5:0x0012  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public void updateCast() {
        boolean z;
        for (CastController.CastDevice castDevice : this.mCast.getCastDevices()) {
            if (castDevice.state == 1 || castDevice.state == 2) {
                z = true;
                break;
            }
            while (r0.hasNext()) {
            }
        }
        z = false;
        if (DEBUG) {
            Log.v("PhoneStatusBarPolicy", "updateCast: isCasting: " + z);
        }
        this.mHandler.removeCallbacks(this.mRemoveCastIconRunnable);
        if (z) {
            this.mIconController.setIcon(this.mSlotCast, R.drawable.stat_sys_cast, this.mContext.getString(R.string.accessibility_casting));
            this.mIconController.setIconVisibility(this.mSlotCast, true);
            return;
        }
        if (DEBUG) {
            Log.v("PhoneStatusBarPolicy", "updateCast: hiding icon in 3 sec...");
        }
        this.mHandler.postDelayed(this.mRemoveCastIconRunnable, 3000L);
    }

    protected void updateEmbmsState(Intent intent) {
        int intExtra = intent.getIntExtra("isActived", 0);
        if (DEBUG) {
            Log.d("PhoneStatusBarPolicy", "updateEmbmsState  active = " + intExtra);
        }
        if (intExtra == 1) {
            this.mIconController.setIcon(this.mSlotEmbms, R.drawable.stat_sys_embms, this.mContext.getString(R.string.accessibility_embms_enabled));
            this.mIconController.setIconVisibility(this.mSlotEmbms, true);
            return;
        }
        this.mIconController.setIconVisibility(this.mSlotEmbms, false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateManagedProfile() {
        this.mUiOffloadThread.submit(new Runnable() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$PhoneStatusBarPolicy$Z14_E32gJZONWu0xka6llWomWrI
            @Override // java.lang.Runnable
            public final void run() {
                PhoneStatusBarPolicy.lambda$updateManagedProfile$3(PhoneStatusBarPolicy.this);
            }
        });
    }

    public static /* synthetic */ void lambda$updateManagedProfile$3(final PhoneStatusBarPolicy phoneStatusBarPolicy) {
        try {
            final boolean isManagedProfile = phoneStatusBarPolicy.mUserManager.isManagedProfile(ActivityManager.getService().getLastResumedActivityUserId());
            phoneStatusBarPolicy.mHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$PhoneStatusBarPolicy$t6hzEfridDOsU9CjkO4yADRJvjA
                @Override // java.lang.Runnable
                public final void run() {
                    PhoneStatusBarPolicy.lambda$updateManagedProfile$2(PhoneStatusBarPolicy.this, isManagedProfile);
                }
            });
        } catch (RemoteException e) {
            Log.w("PhoneStatusBarPolicy", "updateManagedProfile: ", e);
        }
    }

    public static /* synthetic */ void lambda$updateManagedProfile$2(PhoneStatusBarPolicy phoneStatusBarPolicy, boolean z) {
        boolean z2;
        if (z && (!phoneStatusBarPolicy.mKeyguardMonitor.isShowing() || phoneStatusBarPolicy.mKeyguardMonitor.isOccluded())) {
            z2 = true;
            phoneStatusBarPolicy.mIconController.setIcon(phoneStatusBarPolicy.mSlotManagedProfile, R.drawable.stat_sys_managed_profile_status, phoneStatusBarPolicy.mContext.getString(R.string.accessibility_managed_profile));
        } else {
            z2 = false;
        }
        if (phoneStatusBarPolicy.mManagedProfileIconVisible != z2) {
            phoneStatusBarPolicy.mIconController.setIconVisibility(phoneStatusBarPolicy.mSlotManagedProfile, z2);
            phoneStatusBarPolicy.mManagedProfileIconVisible = z2;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateForegroundInstantApps() {
        final NotificationManager notificationManager = (NotificationManager) this.mContext.getSystemService(NotificationManager.class);
        final ArraySet arraySet = new ArraySet((ArraySet) this.mCurrentNotifs);
        final IPackageManager packageManager = AppGlobals.getPackageManager();
        this.mCurrentNotifs.clear();
        this.mUiOffloadThread.submit(new Runnable() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$PhoneStatusBarPolicy$LDjOSwhc6lGaPoGcAB44Ceqmrn0
            @Override // java.lang.Runnable
            public final void run() {
                PhoneStatusBarPolicy.lambda$updateForegroundInstantApps$5(PhoneStatusBarPolicy.this, arraySet, notificationManager, packageManager);
            }
        });
    }

    public static /* synthetic */ void lambda$updateForegroundInstantApps$5(PhoneStatusBarPolicy phoneStatusBarPolicy, ArraySet arraySet, final NotificationManager notificationManager, IPackageManager iPackageManager) {
        int windowingMode;
        try {
            ActivityManager.StackInfo focusedStackInfo = ActivityManager.getService().getFocusedStackInfo();
            if (focusedStackInfo != null && ((windowingMode = focusedStackInfo.configuration.windowConfiguration.getWindowingMode()) == 1 || windowingMode == 4)) {
                phoneStatusBarPolicy.checkStack(focusedStackInfo, arraySet, notificationManager, iPackageManager);
            }
            if (phoneStatusBarPolicy.mDockedStackExists) {
                phoneStatusBarPolicy.checkStack(3, 0, arraySet, notificationManager, iPackageManager);
            }
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
        }
        arraySet.forEach(new Consumer() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$PhoneStatusBarPolicy$wi5igeUw1IMOscl7OYC68EMvMy0
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                notificationManager.cancelAsUser((String) r2.first, 7, new UserHandle(((Integer) ((Pair) obj).second).intValue()));
            }
        });
    }

    private void checkStack(int i, int i2, ArraySet<Pair<String, Integer>> arraySet, NotificationManager notificationManager, IPackageManager iPackageManager) {
        try {
            checkStack(ActivityManager.getService().getStackInfo(i, i2), arraySet, notificationManager, iPackageManager);
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
        }
    }

    private void checkStack(ActivityManager.StackInfo stackInfo, ArraySet<Pair<String, Integer>> arraySet, NotificationManager notificationManager, IPackageManager iPackageManager) {
        if (stackInfo == null) {
            return;
        }
        try {
            if (stackInfo.topActivity == null) {
                return;
            }
            String packageName = stackInfo.topActivity.getPackageName();
            if (!hasNotif(arraySet, packageName, stackInfo.userId)) {
                ApplicationInfo applicationInfo = iPackageManager.getApplicationInfo(packageName, 8192, stackInfo.userId);
                if (applicationInfo.isInstantApp()) {
                    postEphemeralNotif(packageName, stackInfo.userId, applicationInfo, notificationManager, stackInfo.taskIds[stackInfo.taskIds.length - 1]);
                }
            }
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
        }
    }

    private void postEphemeralNotif(String str, int i, ApplicationInfo applicationInfo, NotificationManager notificationManager, int i2) {
        ComponentName componentName;
        Bundle bundle = new Bundle();
        bundle.putString("android.substName", this.mContext.getString(R.string.instant_apps));
        this.mCurrentNotifs.add(new Pair<>(str, Integer.valueOf(i)));
        String string = this.mContext.getString(R.string.instant_apps_message);
        PendingIntent activity = BenesseExtension.getDchaState() != 0 ? null : PendingIntent.getActivity(this.mContext, 0, new Intent("android.settings.APPLICATION_DETAILS_SETTINGS").setData(Uri.fromParts("package", str, null)), 67108864);
        Notification.Action build = new Notification.Action.Builder((Icon) null, this.mContext.getString(R.string.app_info), activity).build();
        Intent taskIntent = getTaskIntent(i2, i);
        Notification.Builder builder = new Notification.Builder(this.mContext, NotificationChannels.GENERAL);
        if (taskIntent != null && taskIntent.isWebIntent()) {
            taskIntent.setComponent(null).setPackage(null).addFlags(512).addFlags(268435456);
            PendingIntent activity2 = PendingIntent.getActivity(this.mContext, 0, taskIntent, 67108864);
            try {
                componentName = AppGlobals.getPackageManager().getInstantAppInstallerComponent();
            } catch (RemoteException e) {
                e.rethrowFromSystemServer();
                componentName = null;
            }
            builder.addAction(new Notification.Action.Builder((Icon) null, this.mContext.getString(R.string.go_to_web), PendingIntent.getActivity(this.mContext, 0, new Intent().setComponent(componentName).setAction("android.intent.action.VIEW").addCategory("android.intent.category.BROWSABLE").addCategory("unique:" + System.currentTimeMillis()).putExtra("android.intent.extra.PACKAGE_NAME", applicationInfo.packageName).putExtra("android.intent.extra.VERSION_CODE", applicationInfo.versionCode & Integer.MAX_VALUE).putExtra("android.intent.extra.LONG_VERSION_CODE", applicationInfo.versionCode).putExtra("android.intent.extra.EPHEMERAL_FAILURE", activity2).putExtra("android.intent.extra.INSTANT_APP_FAILURE", activity2), 67108864)).build());
        }
        notificationManager.notifyAsUser(str, 7, builder.addExtras(bundle).addAction(build).setContentIntent(activity).setColor(this.mContext.getColor(R.color.instant_apps_color)).setContentTitle(applicationInfo.loadLabel(this.mContext.getPackageManager())).setLargeIcon(Icon.createWithResource(str, applicationInfo.icon)).setSmallIcon(Icon.createWithResource(this.mContext.getPackageName(), (int) R.drawable.instant_icon)).setContentText(string).setOngoing(true).build(), new UserHandle(i));
    }

    private Intent getTaskIntent(int i, int i2) {
        try {
            List list = ActivityManager.getService().getRecentTasks(5, 0, i2).getList();
            for (int i3 = 0; i3 < list.size(); i3++) {
                if (((ActivityManager.RecentTaskInfo) list.get(i3)).id == i) {
                    return ((ActivityManager.RecentTaskInfo) list.get(i3)).baseIntent;
                }
            }
            return null;
        } catch (RemoteException e) {
            return null;
        }
    }

    private boolean hasNotif(ArraySet<Pair<String, Integer>> arraySet, String str, int i) {
        Pair<String, Integer> pair = new Pair<>(str, Integer.valueOf(i));
        if (arraySet.remove(pair)) {
            this.mCurrentNotifs.add(pair);
            return true;
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.android.systemui.statusbar.phone.PhoneStatusBarPolicy$1  reason: invalid class name */
    /* loaded from: classes.dex */
    public class AnonymousClass1 extends SynchronousUserSwitchObserver {
        AnonymousClass1() {
        }

        public void onUserSwitching(int i) throws RemoteException {
            PhoneStatusBarPolicy.this.mHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$PhoneStatusBarPolicy$1$4_BI5ieR2ylfAj9z5SwNfbqaqk4
                @Override // java.lang.Runnable
                public final void run() {
                    PhoneStatusBarPolicy.this.mUserInfoController.reloadUserInfo();
                }
            });
        }

        public void onUserSwitchComplete(int i) throws RemoteException {
            PhoneStatusBarPolicy.this.mHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$PhoneStatusBarPolicy$1$lONTSmykfPe64DIHRuLayVCRwlI
                @Override // java.lang.Runnable
                public final void run() {
                    PhoneStatusBarPolicy.AnonymousClass1.lambda$onUserSwitchComplete$1(PhoneStatusBarPolicy.AnonymousClass1.this);
                }
            });
        }

        public static /* synthetic */ void lambda$onUserSwitchComplete$1(AnonymousClass1 anonymousClass1) {
            PhoneStatusBarPolicy.this.updateAlarm();
            PhoneStatusBarPolicy.this.updateManagedProfile();
            PhoneStatusBarPolicy.this.updateForegroundInstantApps();
        }
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void appTransitionStarting(long j, long j2, boolean z) {
        updateManagedProfile();
        updateForegroundInstantApps();
    }

    @Override // com.android.systemui.statusbar.policy.KeyguardMonitor.Callback
    public void onKeyguardShowingChanged() {
        updateManagedProfile();
        updateForegroundInstantApps();
    }

    @Override // com.android.systemui.statusbar.policy.DeviceProvisionedController.DeviceProvisionedListener
    public void onUserSetupChanged() {
        boolean isUserSetup = this.mProvisionedController.isUserSetup(this.mProvisionedController.getCurrentUser());
        if (this.mCurrentUserSetup == isUserSetup) {
            return;
        }
        this.mCurrentUserSetup = isUserSetup;
        updateAlarm();
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void preloadRecentApps() {
        updateForegroundInstantApps();
    }

    @Override // com.android.systemui.statusbar.policy.RotationLockController.RotationLockControllerCallback
    public void onRotationLockStateChanged(boolean z, boolean z2) {
        boolean isCurrentOrientationLockPortrait = RotationLockTile.isCurrentOrientationLockPortrait(this.mRotationLockController, this.mContext);
        if (z) {
            if (isCurrentOrientationLockPortrait) {
                this.mIconController.setIcon(this.mSlotRotate, R.drawable.stat_sys_rotate_portrait, this.mContext.getString(R.string.accessibility_rotation_lock_on_portrait));
            } else {
                this.mIconController.setIcon(this.mSlotRotate, R.drawable.stat_sys_rotate_landscape, this.mContext.getString(R.string.accessibility_rotation_lock_on_landscape));
            }
            this.mIconController.setIconVisibility(this.mSlotRotate, true);
            return;
        }
        this.mIconController.setIconVisibility(this.mSlotRotate, false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateHeadsetPlug(Intent intent) {
        int i;
        boolean z = intent.getIntExtra("state", 0) != 0;
        boolean z2 = intent.getIntExtra("microphone", 0) != 0;
        Log.d("PhoneStatusBarPolicy", "updateHeadsetPlug connected:" + z + ",hasMic:" + z2);
        if (z) {
            Context context = this.mContext;
            if (z2) {
                i = R.string.accessibility_status_bar_headset;
            } else {
                i = R.string.accessibility_status_bar_headphones;
            }
            this.mIconController.setIcon(this.mSlotHeadset, z2 ? R.drawable.ic_headset_mic : R.drawable.ic_headset, context.getString(i));
            this.mIconController.setIconVisibility(this.mSlotHeadset, true);
            return;
        }
        this.mIconController.setIconVisibility(this.mSlotHeadset, false);
    }

    @Override // com.android.systemui.statusbar.policy.DataSaverController.Listener
    public void onDataSaverChanged(boolean z) {
        this.mIconController.setIconVisibility(this.mSlotDataSaver, z);
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
}
