package com.mediatek.systemui.qs.tiles.ext;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.mediatek.systemui.ext.IQuickSettingsPlugin;
import com.mediatek.systemui.ext.OpSystemUICustomizationFactoryBase;
import com.mediatek.systemui.statusbar.extcb.IconIdWrapper;
import com.mediatek.systemui.statusbar.util.SIMHelper;
/* loaded from: classes.dex */
public class ApnSettingsTile extends QSTileImpl<QSTile.BooleanState> {
    private static final Intent APN_SETTINGS = new Intent().setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$ApnSettingsActivity"));
    private boolean mApnSettingsEnabled;
    private String mApnStateLabel;
    private final IconIdWrapper mDisableApnStateIconWrapper;
    private final IconIdWrapper mEnableApnStateIconWrapper;
    private boolean mIsAirplaneMode;
    private boolean mIsWifiOnly;
    private boolean mListening;
    private final PhoneStateListener mPhoneStateListener;
    private IQuickSettingsPlugin mQuickSettingsExt;
    private final BroadcastReceiver mReceiver;
    private final SubscriptionManager mSubscriptionManager;
    private CharSequence mTileLabel;
    private final UserManager mUm;

    public ApnSettingsTile(QSHost qSHost) {
        super(qSHost);
        this.mEnableApnStateIconWrapper = new IconIdWrapper();
        this.mDisableApnStateIconWrapper = new IconIdWrapper();
        this.mApnStateLabel = "";
        this.mApnSettingsEnabled = false;
        this.mQuickSettingsExt = null;
        this.mReceiver = new BroadcastReceiver() { // from class: com.mediatek.systemui.qs.tiles.ext.ApnSettingsTile.1
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.d("ApnSettingsTile", "onReceive(), action: " + action);
                if (action.equals("android.intent.action.AIRPLANE_MODE")) {
                    boolean booleanExtra = intent.getBooleanExtra("state", false);
                    Log.d("ApnSettingsTile", "onReceive(), airline mode changed: state is " + booleanExtra);
                    ApnSettingsTile.this.updateState();
                } else if (action.equals("com.mediatek.phone.ACTION_EF_CSP_CONTENT_NOTIFY") || action.equals("com.mediatek.intent.action.MSIM_MODE") || action.equals("mediatek.intent.action.ACTION_MD_TYPE_CHANGE") || action.equals("mediatek.intent.action.LOCATED_PLMN_CHANGED") || action.equals("android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE") || action.equals("android.intent.action.ACTION_SUBINFO_CONTENT_CHANGE") || action.equals("android.intent.action.ACTION_SUBINFO_RECORD_UPDATED")) {
                    ApnSettingsTile.this.updateState();
                }
            }
        };
        this.mPhoneStateListener = new PhoneStateListener() { // from class: com.mediatek.systemui.qs.tiles.ext.ApnSettingsTile.2
            @Override // android.telephony.PhoneStateListener
            public void onCallStateChanged(int i, String str) {
                Log.d("ApnSettingsTile", "onCallStateChanged call state is " + i);
                if (i == 0) {
                    ApnSettingsTile.this.updateState();
                }
            }
        };
        this.mQuickSettingsExt = OpSystemUICustomizationFactoryBase.getOpFactory(this.mContext).makeQuickSettings(this.mContext);
        this.mSubscriptionManager = SubscriptionManager.from(this.mContext);
        this.mUm = (UserManager) this.mContext.getSystemService("user");
        this.mIsWifiOnly = !((ConnectivityManager) this.mContext.getSystemService("connectivity")).isNetworkSupported(0);
        updateState();
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        this.mTileLabel = this.mQuickSettingsExt.getTileLabel("apnsettings");
        return this.mTileLabel;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return null;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleSetListening(boolean z) {
        Log.d("ApnSettingsTile", "setListening(), listening = " + z);
        if (this.mListening == z) {
            return;
        }
        this.mListening = z;
        if (z) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.AIRPLANE_MODE");
            intentFilter.addAction("com.mediatek.phone.ACTION_EF_CSP_CONTENT_NOTIFY");
            intentFilter.addAction("com.mediatek.intent.action.MSIM_MODE");
            intentFilter.addAction("mediatek.intent.action.ACTION_MD_TYPE_CHANGE");
            intentFilter.addAction("mediatek.intent.action.LOCATED_PLMN_CHANGED");
            intentFilter.addAction("android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE");
            intentFilter.addAction("android.intent.action.ACTION_SUBINFO_CONTENT_CHANGE");
            intentFilter.addAction("android.intent.action.ACTION_SUBINFO_RECORD_UPDATED");
            this.mContext.registerReceiver(this.mReceiver, intentFilter);
            TelephonyManager.getDefault().listen(this.mPhoneStateListener, 32);
            return;
        }
        this.mContext.unregisterReceiver(this.mReceiver);
        TelephonyManager.getDefault().listen(this.mPhoneStateListener, 0);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public int getMetricsCategory() {
        return 111;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    protected void handleLongClick() {
        handleClick();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    protected void handleClick() {
        updateState();
        Log.d("ApnSettingsTile", "handleClick(), mApnSettingsEnabled = " + this.mApnSettingsEnabled);
        if (this.mApnSettingsEnabled) {
            APN_SETTINGS.putExtra("sub_id", SubscriptionManager.getDefaultDataSubscriptionId());
            Log.d("ApnSettingsTile", "handleClick(), " + APN_SETTINGS);
            ((ActivityStarter) Dependency.get(ActivityStarter.class)).postStartActivityDismissingKeyguard(APN_SETTINGS, 0);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        if (this.mApnSettingsEnabled) {
            booleanState.icon = QsIconWrapper.get(this.mEnableApnStateIconWrapper.getIconId(), this.mEnableApnStateIconWrapper);
        } else {
            booleanState.icon = QsIconWrapper.get(this.mDisableApnStateIconWrapper.getIconId(), this.mDisableApnStateIconWrapper);
        }
        booleanState.label = this.mApnStateLabel;
        booleanState.contentDescription = this.mApnStateLabel;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void updateState() {
        boolean z = false;
        this.mIsAirplaneMode = Settings.Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) != 0;
        boolean z2 = (UserHandle.myUserId() == 0 && ActivityManager.getCurrentUser() == 0) ? false : true;
        boolean hasUserRestriction = this.mUm.hasUserRestriction("no_config_mobile_networks");
        if (this.mIsWifiOnly || z2 || hasUserRestriction) {
            Log.d("ApnSettingsTile", "updateState(), isSecondaryUser = " + z2 + ", mIsWifiOnly = " + this.mIsWifiOnly + ", isRestricted = " + hasUserRestriction);
        } else {
            int activeSubscriptionInfoCount = this.mSubscriptionManager.getActiveSubscriptionInfoCount();
            int callState = TelephonyManager.getDefault().getCallState();
            boolean z3 = callState == 0;
            if (!this.mIsAirplaneMode && activeSubscriptionInfoCount > 0 && z3 && !isAllRadioOff()) {
                z = true;
            }
            Log.d("ApnSettingsTile", "updateState(), mIsAirplaneMode = " + this.mIsAirplaneMode + ", simNum = " + activeSubscriptionInfoCount + ", callstate = " + callState + ", isIdle = " + z3);
        }
        this.mApnSettingsEnabled = z;
        Log.d("ApnSettingsTile", "updateState(), mApnSettingsEnabled = " + this.mApnSettingsEnabled);
        updateStateResources();
        refreshState();
    }

    private final void updateStateResources() {
        if (this.mApnSettingsEnabled) {
            this.mApnStateLabel = this.mQuickSettingsExt.customizeApnSettingsTile(this.mApnSettingsEnabled, this.mEnableApnStateIconWrapper, this.mApnStateLabel);
        } else {
            this.mApnStateLabel = this.mQuickSettingsExt.customizeApnSettingsTile(this.mApnSettingsEnabled, this.mDisableApnStateIconWrapper, this.mApnStateLabel);
        }
    }

    private boolean isAllRadioOff() {
        int[] activeSubscriptionIdList = this.mSubscriptionManager.getActiveSubscriptionIdList();
        if (activeSubscriptionIdList != null && activeSubscriptionIdList.length > 0) {
            for (int i : activeSubscriptionIdList) {
                if (SIMHelper.isRadioOn(i)) {
                    return false;
                }
            }
        }
        return true;
    }
}
