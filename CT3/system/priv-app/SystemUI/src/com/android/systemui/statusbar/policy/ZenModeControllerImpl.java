package com.android.systemui.statusbar.policy;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.service.notification.Condition;
import android.service.notification.IConditionListener;
import android.service.notification.ZenModeConfig;
import android.util.Log;
import android.util.Slog;
import com.android.systemui.qs.GlobalSetting;
import com.android.systemui.statusbar.policy.ZenModeController;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Objects;
/* loaded from: a.zip:com/android/systemui/statusbar/policy/ZenModeControllerImpl.class */
public class ZenModeControllerImpl implements ZenModeController {
    private static final boolean DEBUG = Log.isLoggable("ZenModeController", 3);
    private final AlarmManager mAlarmManager;
    private ZenModeConfig mConfig;
    private final GlobalSetting mConfigSetting;
    private final Context mContext;
    private final GlobalSetting mModeSetting;
    private final NotificationManager mNoMan;
    private boolean mRegistered;
    private boolean mRequesting;
    private final SetupObserver mSetupObserver;
    private int mUserId;
    private final UserManager mUserManager;
    private final ArrayList<ZenModeController.Callback> mCallbacks = new ArrayList<>();
    private final LinkedHashMap<Uri, Condition> mConditions = new LinkedHashMap<>();
    private final IConditionListener mListener = new IConditionListener.Stub(this) { // from class: com.android.systemui.statusbar.policy.ZenModeControllerImpl.1
        final ZenModeControllerImpl this$0;

        {
            this.this$0 = this;
        }

        public void onConditionsReceived(Condition[] conditionArr) {
            if (ZenModeControllerImpl.DEBUG) {
                Slog.d("ZenModeController", "onConditionsReceived " + (conditionArr == null ? 0 : conditionArr.length) + " mRequesting=" + this.this$0.mRequesting);
            }
            if (this.this$0.mRequesting) {
                this.this$0.updateConditions(conditionArr);
            }
        }
    };
    private final BroadcastReceiver mReceiver = new BroadcastReceiver(this) { // from class: com.android.systemui.statusbar.policy.ZenModeControllerImpl.2
        final ZenModeControllerImpl this$0;

        {
            this.this$0 = this;
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if ("android.app.action.NEXT_ALARM_CLOCK_CHANGED".equals(intent.getAction())) {
                this.this$0.fireNextAlarmChanged();
            }
            if ("android.os.action.ACTION_EFFECTS_SUPPRESSOR_CHANGED".equals(intent.getAction())) {
                this.this$0.fireEffectsSuppressorChanged();
            }
        }
    };

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/statusbar/policy/ZenModeControllerImpl$SetupObserver.class */
    public final class SetupObserver extends ContentObserver {
        private boolean mRegistered;
        private final ContentResolver mResolver;
        final ZenModeControllerImpl this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public SetupObserver(ZenModeControllerImpl zenModeControllerImpl, Handler handler) {
            super(handler);
            this.this$0 = zenModeControllerImpl;
            this.mResolver = zenModeControllerImpl.mContext.getContentResolver();
        }

        public boolean isDeviceProvisioned() {
            boolean z = false;
            if (Settings.Global.getInt(this.mResolver, "device_provisioned", 0) != 0) {
                z = true;
            }
            return z;
        }

        public boolean isUserSetup() {
            boolean z = false;
            if (Settings.Secure.getIntForUser(this.mResolver, "user_setup_complete", 0, this.this$0.mUserId) != 0) {
                z = true;
            }
            return z;
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z, Uri uri) {
            if (Settings.Global.getUriFor("device_provisioned").equals(uri) || Settings.Secure.getUriFor("user_setup_complete").equals(uri)) {
                this.this$0.fireZenAvailableChanged(this.this$0.isZenAvailable());
            }
        }

        public void register() {
            if (this.mRegistered) {
                this.mResolver.unregisterContentObserver(this);
            }
            this.mResolver.registerContentObserver(Settings.Global.getUriFor("device_provisioned"), false, this);
            this.mResolver.registerContentObserver(Settings.Secure.getUriFor("user_setup_complete"), false, this, this.this$0.mUserId);
            this.this$0.fireZenAvailableChanged(this.this$0.isZenAvailable());
        }
    }

    public ZenModeControllerImpl(Context context, Handler handler) {
        this.mContext = context;
        this.mModeSetting = new GlobalSetting(this, this.mContext, handler, "zen_mode") { // from class: com.android.systemui.statusbar.policy.ZenModeControllerImpl.3
            final ZenModeControllerImpl this$0;

            {
                this.this$0 = this;
            }

            @Override // com.android.systemui.qs.GlobalSetting
            protected void handleValueChanged(int i) {
                this.this$0.fireZenChanged(i);
            }
        };
        this.mConfigSetting = new GlobalSetting(this, this.mContext, handler, "zen_mode_config_etag") { // from class: com.android.systemui.statusbar.policy.ZenModeControllerImpl.4
            final ZenModeControllerImpl this$0;

            {
                this.this$0 = this;
            }

            @Override // com.android.systemui.qs.GlobalSetting
            protected void handleValueChanged(int i) {
                this.this$0.updateZenModeConfig();
            }
        };
        this.mNoMan = (NotificationManager) context.getSystemService("notification");
        this.mConfig = this.mNoMan.getZenModeConfig();
        this.mModeSetting.setListening(true);
        this.mConfigSetting.setListening(true);
        this.mAlarmManager = (AlarmManager) context.getSystemService("alarm");
        this.mSetupObserver = new SetupObserver(this, handler);
        this.mSetupObserver.register();
        this.mUserManager = (UserManager) context.getSystemService(UserManager.class);
    }

    private void fireConditionsChanged(Condition[] conditionArr) {
        for (ZenModeController.Callback callback : this.mCallbacks) {
            callback.onConditionsChanged(conditionArr);
        }
    }

    private void fireConfigChanged(ZenModeConfig zenModeConfig) {
        for (ZenModeController.Callback callback : this.mCallbacks) {
            callback.onConfigChanged(zenModeConfig);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void fireEffectsSuppressorChanged() {
        for (ZenModeController.Callback callback : this.mCallbacks) {
            callback.onEffectsSupressorChanged();
        }
    }

    private void fireManualRuleChanged(ZenModeConfig.ZenRule zenRule) {
        for (ZenModeController.Callback callback : this.mCallbacks) {
            callback.onManualRuleChanged(zenRule);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void fireNextAlarmChanged() {
        for (ZenModeController.Callback callback : this.mCallbacks) {
            callback.onNextAlarmChanged();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void fireZenAvailableChanged(boolean z) {
        for (ZenModeController.Callback callback : this.mCallbacks) {
            callback.onZenAvailableChanged(z);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void fireZenChanged(int i) {
        for (ZenModeController.Callback callback : this.mCallbacks) {
            callback.onZenChanged(i);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateConditions(Condition[] conditionArr) {
        if (conditionArr == null || conditionArr.length == 0) {
            return;
        }
        for (Condition condition : conditionArr) {
            if ((condition.flags & 1) != 0) {
                this.mConditions.put(condition.id, condition);
            }
        }
        fireConditionsChanged((Condition[]) this.mConditions.values().toArray(new Condition[this.mConditions.values().size()]));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateZenModeConfig() {
        ZenModeConfig.ZenRule zenRule = null;
        ZenModeConfig zenModeConfig = this.mNoMan.getZenModeConfig();
        if (Objects.equals(zenModeConfig, this.mConfig)) {
            return;
        }
        ZenModeConfig.ZenRule zenRule2 = this.mConfig != null ? this.mConfig.manualRule : null;
        this.mConfig = zenModeConfig;
        fireConfigChanged(zenModeConfig);
        if (zenModeConfig != null) {
            zenRule = zenModeConfig.manualRule;
        }
        if (Objects.equals(zenRule2, zenRule)) {
            return;
        }
        fireManualRuleChanged(zenRule);
    }

    @Override // com.android.systemui.statusbar.policy.ZenModeController
    public void addCallback(ZenModeController.Callback callback) {
        this.mCallbacks.add(callback);
    }

    @Override // com.android.systemui.statusbar.policy.ZenModeController
    public ZenModeConfig getConfig() {
        return this.mConfig;
    }

    @Override // com.android.systemui.statusbar.policy.ZenModeController
    public int getCurrentUser() {
        return ActivityManager.getCurrentUser();
    }

    @Override // com.android.systemui.statusbar.policy.ZenModeController
    public ZenModeConfig.ZenRule getManualRule() {
        ZenModeConfig.ZenRule zenRule = null;
        if (this.mConfig != null) {
            zenRule = this.mConfig.manualRule;
        }
        return zenRule;
    }

    @Override // com.android.systemui.statusbar.policy.ZenModeController
    public long getNextAlarm() {
        AlarmManager.AlarmClockInfo nextAlarmClock = this.mAlarmManager.getNextAlarmClock(this.mUserId);
        return nextAlarmClock != null ? nextAlarmClock.getTriggerTime() : 0L;
    }

    @Override // com.android.systemui.statusbar.policy.ZenModeController
    public int getZen() {
        return this.mModeSetting.getValue();
    }

    @Override // com.android.systemui.statusbar.policy.ZenModeController
    public boolean isCountdownConditionSupported() {
        return NotificationManager.from(this.mContext).isSystemConditionProviderEnabled("countdown");
    }

    @Override // com.android.systemui.statusbar.policy.ZenModeController
    public boolean isVolumeRestricted() {
        return this.mUserManager.hasUserRestriction("no_adjust_volume", new UserHandle(this.mUserId));
    }

    public boolean isZenAvailable() {
        return this.mSetupObserver.isDeviceProvisioned() ? this.mSetupObserver.isUserSetup() : false;
    }

    @Override // com.android.systemui.statusbar.policy.ZenModeController
    public void removeCallback(ZenModeController.Callback callback) {
        this.mCallbacks.remove(callback);
    }

    @Override // com.android.systemui.statusbar.policy.ZenModeController
    public void setUserId(int i) {
        this.mUserId = i;
        if (this.mRegistered) {
            this.mContext.unregisterReceiver(this.mReceiver);
        }
        IntentFilter intentFilter = new IntentFilter("android.app.action.NEXT_ALARM_CLOCK_CHANGED");
        intentFilter.addAction("android.os.action.ACTION_EFFECTS_SUPPRESSOR_CHANGED");
        this.mContext.registerReceiverAsUser(this.mReceiver, new UserHandle(this.mUserId), intentFilter, null, null);
        this.mRegistered = true;
        this.mSetupObserver.register();
    }

    @Override // com.android.systemui.statusbar.policy.ZenModeController
    public void setZen(int i, Uri uri, String str) {
        this.mNoMan.setZenMode(i, uri, str);
    }
}
