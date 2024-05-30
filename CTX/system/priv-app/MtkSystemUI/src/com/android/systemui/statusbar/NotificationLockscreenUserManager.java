package com.android.systemui.statusbar;

import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.UserInfo;
import android.database.ContentObserver;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.widget.TextView;
import android.widget.Toast;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.statusbar.NotificationVisibility;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.Dependency;
import com.android.systemui.Dumpable;
import com.android.systemui.OverviewProxyService;
import com.android.systemui.R;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import java.io.FileDescriptor;
import java.io.PrintWriter;
/* loaded from: classes.dex */
public class NotificationLockscreenUserManager implements Dumpable {
    private boolean mAllowLockscreenRemoteInput;
    protected final Context mContext;
    protected int mCurrentUserId;
    private final DevicePolicyManager mDevicePolicyManager;
    protected NotificationEntryManager mEntryManager;
    protected ContentObserver mLockscreenSettingsObserver;
    protected NotificationPresenter mPresenter;
    protected ContentObserver mSettingsObserver;
    private boolean mShowLockscreenNotifications;
    private final UserManager mUserManager;
    private final SparseBooleanArray mLockscreenPublicMode = new SparseBooleanArray();
    private final SparseBooleanArray mUsersAllowingPrivateNotifications = new SparseBooleanArray();
    private final SparseBooleanArray mUsersAllowingNotifications = new SparseBooleanArray();
    private final DeviceProvisionedController mDeviceProvisionedController = (DeviceProvisionedController) Dependency.get(DeviceProvisionedController.class);
    protected final BroadcastReceiver mAllUsersReceiver = new BroadcastReceiver() { // from class: com.android.systemui.statusbar.NotificationLockscreenUserManager.1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int intExtra = intent.getIntExtra("android.intent.extra.user_handle", -10000);
            if ("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED".equals(action) && NotificationLockscreenUserManager.this.isCurrentProfile(getSendingUserId())) {
                NotificationLockscreenUserManager.this.mUsersAllowingPrivateNotifications.clear();
                NotificationLockscreenUserManager.this.updateLockscreenNotificationSetting();
                NotificationLockscreenUserManager.this.mEntryManager.updateNotifications();
            } else if ("android.intent.action.DEVICE_LOCKED_CHANGED".equals(action) && intExtra != NotificationLockscreenUserManager.this.mCurrentUserId && NotificationLockscreenUserManager.this.isCurrentProfile(intExtra)) {
                NotificationLockscreenUserManager.this.mPresenter.onWorkChallengeChanged();
            }
        }
    };
    protected final BroadcastReceiver mBaseBroadcastReceiver = new BroadcastReceiver() { // from class: com.android.systemui.statusbar.NotificationLockscreenUserManager.2
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.USER_SWITCHED".equals(action)) {
                NotificationLockscreenUserManager.this.mCurrentUserId = intent.getIntExtra("android.intent.extra.user_handle", -1);
                NotificationLockscreenUserManager.this.updateCurrentProfilesCache();
                Log.v("LockscreenUserManager", "userId " + NotificationLockscreenUserManager.this.mCurrentUserId + " is in the house");
                NotificationLockscreenUserManager.this.updateLockscreenNotificationSetting();
                NotificationLockscreenUserManager.this.mPresenter.onUserSwitched(NotificationLockscreenUserManager.this.mCurrentUserId);
            } else if ("android.intent.action.USER_ADDED".equals(action)) {
                NotificationLockscreenUserManager.this.updateCurrentProfilesCache();
            } else if ("android.intent.action.USER_UNLOCKED".equals(action)) {
                ((OverviewProxyService) Dependency.get(OverviewProxyService.class)).startConnectionToCurrentUser();
            } else if ("android.intent.action.USER_PRESENT".equals(action)) {
                try {
                    if (NotificationLockscreenUserManager.this.mUserManager.isManagedProfile(ActivityManager.getService().getLastResumedActivityUserId())) {
                        NotificationLockscreenUserManager.this.showForegroundManagedProfileActivityToast();
                    }
                } catch (RemoteException e) {
                }
            } else if ("com.android.systemui.statusbar.work_challenge_unlocked_notification_action".equals(action)) {
                IntentSender intentSender = (IntentSender) intent.getParcelableExtra("android.intent.extra.INTENT");
                String stringExtra = intent.getStringExtra("android.intent.extra.INDEX");
                if (intentSender != null) {
                    try {
                        NotificationLockscreenUserManager.this.mContext.startIntentSender(intentSender, null, 0, 0, 0);
                    } catch (IntentSender.SendIntentException e2) {
                    }
                }
                if (stringExtra != null) {
                    try {
                        NotificationLockscreenUserManager.this.mBarService.onNotificationClick(stringExtra, NotificationVisibility.obtain(stringExtra, NotificationLockscreenUserManager.this.mEntryManager.getNotificationData().getRank(stringExtra), NotificationLockscreenUserManager.this.mEntryManager.getNotificationData().getActiveNotifications().size(), true));
                    } catch (RemoteException e3) {
                    }
                }
            }
        }
    };
    protected final SparseArray<UserInfo> mCurrentProfiles = new SparseArray<>();
    private final IStatusBarService mBarService = IStatusBarService.Stub.asInterface(ServiceManager.getService("statusbar"));

    public NotificationLockscreenUserManager(Context context) {
        this.mCurrentUserId = 0;
        this.mContext = context;
        this.mDevicePolicyManager = (DevicePolicyManager) this.mContext.getSystemService("device_policy");
        this.mUserManager = (UserManager) this.mContext.getSystemService("user");
        this.mCurrentUserId = ActivityManager.getCurrentUser();
    }

    public void setUpWithPresenter(NotificationPresenter notificationPresenter, NotificationEntryManager notificationEntryManager) {
        this.mPresenter = notificationPresenter;
        this.mEntryManager = notificationEntryManager;
        this.mLockscreenSettingsObserver = new ContentObserver(this.mPresenter.getHandler()) { // from class: com.android.systemui.statusbar.NotificationLockscreenUserManager.3
            @Override // android.database.ContentObserver
            public void onChange(boolean z) {
                NotificationLockscreenUserManager.this.mUsersAllowingPrivateNotifications.clear();
                NotificationLockscreenUserManager.this.mUsersAllowingNotifications.clear();
                NotificationLockscreenUserManager.this.updateLockscreenNotificationSetting();
                NotificationLockscreenUserManager.this.mEntryManager.updateNotifications();
            }
        };
        this.mSettingsObserver = new ContentObserver(this.mPresenter.getHandler()) { // from class: com.android.systemui.statusbar.NotificationLockscreenUserManager.4
            @Override // android.database.ContentObserver
            public void onChange(boolean z) {
                NotificationLockscreenUserManager.this.updateLockscreenNotificationSetting();
                if (NotificationLockscreenUserManager.this.mDeviceProvisionedController.isDeviceProvisioned()) {
                    NotificationLockscreenUserManager.this.mEntryManager.updateNotifications();
                }
            }
        };
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("lock_screen_show_notifications"), false, this.mLockscreenSettingsObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("lock_screen_allow_private_notifications"), true, this.mLockscreenSettingsObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("zen_mode"), false, this.mSettingsObserver);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED");
        intentFilter.addAction("android.intent.action.DEVICE_LOCKED_CHANGED");
        this.mContext.registerReceiverAsUser(this.mAllUsersReceiver, UserHandle.ALL, intentFilter, null, null);
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction("android.intent.action.USER_SWITCHED");
        intentFilter2.addAction("android.intent.action.USER_ADDED");
        intentFilter2.addAction("android.intent.action.USER_PRESENT");
        intentFilter2.addAction("android.intent.action.USER_UNLOCKED");
        this.mContext.registerReceiver(this.mBaseBroadcastReceiver, intentFilter2);
        IntentFilter intentFilter3 = new IntentFilter();
        intentFilter3.addAction("com.android.systemui.statusbar.work_challenge_unlocked_notification_action");
        this.mContext.registerReceiver(this.mBaseBroadcastReceiver, intentFilter3, "com.android.systemui.permission.SELF", null);
        updateCurrentProfilesCache();
        this.mSettingsObserver.onChange(false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showForegroundManagedProfileActivityToast() {
        Toast makeText = Toast.makeText(this.mContext, (int) R.string.managed_profile_foreground_toast, 0);
        TextView textView = (TextView) makeText.getView().findViewById(16908299);
        textView.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.stat_sys_managed_profile_status, 0, 0, 0);
        textView.setCompoundDrawablePadding(this.mContext.getResources().getDimensionPixelSize(R.dimen.managed_profile_toast_padding));
        makeText.show();
    }

    public boolean shouldShowLockscreenNotifications() {
        return this.mShowLockscreenNotifications;
    }

    public boolean shouldAllowLockscreenRemoteInput() {
        return this.mAllowLockscreenRemoteInput;
    }

    public boolean isCurrentProfile(int i) {
        boolean z;
        synchronized (this.mCurrentProfiles) {
            if (i != -1) {
                try {
                    if (this.mCurrentProfiles.get(i) == null) {
                        z = false;
                    }
                } finally {
                }
            }
            z = true;
        }
        return z;
    }

    private boolean shouldTemporarilyHideNotifications(int i) {
        if (i == -1) {
            i = this.mCurrentUserId;
        }
        return KeyguardUpdateMonitor.getInstance(this.mContext).isUserInLockdown(i);
    }

    public boolean shouldHideNotifications(int i) {
        return (isLockscreenPublicMode(i) && !userAllowsNotificationsInPublic(i)) || (i != this.mCurrentUserId && shouldHideNotifications(this.mCurrentUserId)) || shouldTemporarilyHideNotifications(i);
    }

    public boolean shouldHideNotifications(String str) {
        if (this.mEntryManager != null) {
            return isLockscreenPublicMode(this.mCurrentUserId) && this.mEntryManager.getNotificationData().getVisibilityOverride(str) == -1;
        }
        Log.wtf("LockscreenUserManager", "mEntryManager was null!", new Throwable());
        return true;
    }

    public boolean shouldShowOnKeyguard(StatusBarNotification statusBarNotification) {
        if (this.mEntryManager != null) {
            return this.mShowLockscreenNotifications && !this.mEntryManager.getNotificationData().isAmbient(statusBarNotification.getKey());
        }
        Log.wtf("LockscreenUserManager", "mEntryManager was null!", new Throwable());
        return false;
    }

    private void setShowLockscreenNotifications(boolean z) {
        this.mShowLockscreenNotifications = z;
    }

    private void setLockscreenAllowRemoteInput(boolean z) {
        this.mAllowLockscreenRemoteInput = z;
    }

    protected void updateLockscreenNotificationSetting() {
        boolean z = true;
        boolean z2 = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "lock_screen_show_notifications", 1, this.mCurrentUserId) != 0;
        boolean z3 = (this.mDevicePolicyManager.getKeyguardDisabledFeatures(null, this.mCurrentUserId) & 4) == 0;
        if (!z2 || !z3) {
            z = false;
        }
        setShowLockscreenNotifications(z);
        setLockscreenAllowRemoteInput(false);
    }

    public boolean userAllowsPrivateNotificationsInPublic(int i) {
        boolean z = true;
        if (i == -1) {
            return true;
        }
        if (this.mUsersAllowingPrivateNotifications.indexOfKey(i) < 0) {
            boolean z2 = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "lock_screen_allow_private_notifications", 0, i) != 0;
            boolean adminAllowsKeyguardFeature = adminAllowsKeyguardFeature(i, 8);
            if (!z2 || !adminAllowsKeyguardFeature) {
                z = false;
            }
            this.mUsersAllowingPrivateNotifications.append(i, z);
            return z;
        }
        return this.mUsersAllowingPrivateNotifications.get(i);
    }

    private boolean adminAllowsKeyguardFeature(int i, int i2) {
        return i == -1 || (this.mDevicePolicyManager.getKeyguardDisabledFeatures(null, i) & i2) == 0;
    }

    public void setLockscreenPublicMode(boolean z, int i) {
        this.mLockscreenPublicMode.put(i, z);
    }

    public boolean isLockscreenPublicMode(int i) {
        return i == -1 ? this.mLockscreenPublicMode.get(this.mCurrentUserId, false) : this.mLockscreenPublicMode.get(i, false);
    }

    private boolean userAllowsNotificationsInPublic(int i) {
        boolean z = true;
        if (!isCurrentProfile(i) || i == this.mCurrentUserId) {
            if (this.mUsersAllowingNotifications.indexOfKey(i) < 0) {
                boolean z2 = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "lock_screen_show_notifications", 0, i) != 0;
                boolean adminAllowsKeyguardFeature = adminAllowsKeyguardFeature(i, 4);
                if (!z2 || !adminAllowsKeyguardFeature) {
                    z = false;
                }
                this.mUsersAllowingNotifications.append(i, z);
                return z;
            }
            return this.mUsersAllowingNotifications.get(i);
        }
        return true;
    }

    public boolean needsRedaction(NotificationData.Entry entry) {
        boolean z = (userAllowsPrivateNotificationsInPublic(this.mCurrentUserId) ^ true) || (userAllowsPrivateNotificationsInPublic(entry.notification.getUserId()) ^ true);
        boolean z2 = entry.notification.getNotification().visibility == 0;
        if (packageHasVisibilityOverride(entry.notification.getKey())) {
            return true;
        }
        return z2 && z;
    }

    private boolean packageHasVisibilityOverride(String str) {
        if (this.mEntryManager != null) {
            return this.mEntryManager.getNotificationData().getVisibilityOverride(str) == 0;
        }
        Log.wtf("LockscreenUserManager", "mEntryManager was null!", new Throwable());
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateCurrentProfilesCache() {
        synchronized (this.mCurrentProfiles) {
            this.mCurrentProfiles.clear();
            if (this.mUserManager != null) {
                for (UserInfo userInfo : this.mUserManager.getProfiles(this.mCurrentUserId)) {
                    this.mCurrentProfiles.put(userInfo.id, userInfo);
                }
            }
        }
    }

    public boolean isAnyProfilePublicMode() {
        for (int size = this.mCurrentProfiles.size() - 1; size >= 0; size--) {
            if (isLockscreenPublicMode(this.mCurrentProfiles.valueAt(size).id)) {
                return true;
            }
        }
        return false;
    }

    public int getCurrentUserId() {
        return this.mCurrentUserId;
    }

    public SparseArray<UserInfo> getCurrentProfiles() {
        return this.mCurrentProfiles;
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("NotificationLockscreenUserManager state:");
        printWriter.print("  mCurrentUserId=");
        printWriter.println(this.mCurrentUserId);
        printWriter.print("  mShowLockscreenNotifications=");
        printWriter.println(this.mShowLockscreenNotifications);
        printWriter.print("  mAllowLockscreenRemoteInput=");
        printWriter.println(this.mAllowLockscreenRemoteInput);
        printWriter.print("  mCurrentProfiles=");
        for (int size = this.mCurrentProfiles.size() - 1; size >= 0; size += -1) {
            printWriter.print("" + this.mCurrentProfiles.valueAt(size).id + " ");
        }
        printWriter.println();
    }
}
