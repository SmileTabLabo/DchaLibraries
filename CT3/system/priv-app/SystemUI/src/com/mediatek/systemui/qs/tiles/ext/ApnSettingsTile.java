package com.mediatek.systemui.qs.tiles.ext;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.BenesseExtension;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.systemui.qs.QSTile;
import com.mediatek.systemui.PluginManager;
import com.mediatek.systemui.statusbar.extcb.IconIdWrapper;
import com.mediatek.systemui.statusbar.util.SIMHelper;
/* loaded from: a.zip:com/mediatek/systemui/qs/tiles/ext/ApnSettingsTile.class */
public class ApnSettingsTile extends QSTile<QSTile.State> {
    private static final Intent APN_SETTINGS = new Intent().setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$ApnSettingsActivity"));
    private static final boolean DEBUG = true;
    private static final String TAG = "ApnSettingsTile";
    private boolean mApnSettingsEnabled;
    private String mApnStateLabel;
    private final IconIdWrapper mDisableApnStateIconWrapper;
    private final IconIdWrapper mEnableApnStateIconWrapper;
    private boolean mIsAirplaneMode;
    private boolean mIsWifiOnly;
    private boolean mListening;
    private final PhoneStateListener mPhoneStateListener;
    private final BroadcastReceiver mReceiver;
    private final SubscriptionManager mSubscriptionManager;
    private CharSequence mTileLabel;
    private final UserManager mUm;

    public ApnSettingsTile(QSTile.Host host) {
        super(host);
        this.mEnableApnStateIconWrapper = new IconIdWrapper();
        this.mDisableApnStateIconWrapper = new IconIdWrapper();
        this.mApnStateLabel = "";
        this.mApnSettingsEnabled = false;
        this.mReceiver = new BroadcastReceiver(this) { // from class: com.mediatek.systemui.qs.tiles.ext.ApnSettingsTile.1
            final ApnSettingsTile this$0;

            {
                this.this$0 = this;
            }

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.d(ApnSettingsTile.TAG, "onReceive(), action: " + action);
                if (action.equals("android.intent.action.AIRPLANE_MODE")) {
                    Log.d(ApnSettingsTile.TAG, "onReceive(), airline mode changed: state is " + intent.getBooleanExtra("state", false));
                    this.this$0.updateState();
                } else if (action.equals("android.intent.action.ACTION_EF_CSP_CONTENT_NOTIFY") || action.equals("android.intent.action.MSIM_MODE") || action.equals("android.intent.action.ACTION_MD_TYPE_CHANGE") || action.equals("mediatek.intent.action.LOCATED_PLMN_CHANGED") || action.equals("android.intent.action.ACTION_SET_PHONE_RAT_FAMILY_DONE") || action.equals("android.intent.action.ACTION_SUBINFO_CONTENT_CHANGE") || action.equals("android.intent.action.ACTION_SUBINFO_RECORD_UPDATED")) {
                    this.this$0.updateState();
                }
            }
        };
        this.mPhoneStateListener = new PhoneStateListener(this) { // from class: com.mediatek.systemui.qs.tiles.ext.ApnSettingsTile.2
            final ApnSettingsTile this$0;

            {
                this.this$0 = this;
            }

            @Override // android.telephony.PhoneStateListener
            public void onCallStateChanged(int i, String str) {
                Log.d(ApnSettingsTile.TAG, "onCallStateChanged call state is " + i);
                switch (i) {
                    case 0:
                        this.this$0.updateState();
                        return;
                    default:
                        return;
                }
            }
        };
        this.mSubscriptionManager = SubscriptionManager.from(this.mContext);
        this.mUm = (UserManager) this.mContext.getSystemService("user");
        this.mIsWifiOnly = !((ConnectivityManager) this.mContext.getSystemService("connectivity")).isNetworkSupported(0);
        updateState();
    }

    private boolean isAllRadioOff() {
        int i = 0;
        int[] activeSubscriptionIdList = this.mSubscriptionManager.getActiveSubscriptionIdList();
        boolean z = true;
        if (activeSubscriptionIdList != null) {
            z = true;
            if (activeSubscriptionIdList.length > 0) {
                int length = activeSubscriptionIdList.length;
                while (true) {
                    z = true;
                    if (i >= length) {
                        break;
                    } else if (SIMHelper.isRadioOn(activeSubscriptionIdList[i])) {
                        z = false;
                        break;
                    } else {
                        i++;
                    }
                }
            }
        }
        return z;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void updateState() {
        boolean z;
        boolean z2 = false;
        if (Settings.Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) != 0) {
            z2 = true;
        }
        this.mIsAirplaneMode = z2;
        boolean z3 = UserHandle.myUserId() == 0 ? ActivityManager.getCurrentUser() != 0 : true;
        boolean hasUserRestriction = this.mUm.hasUserRestriction("no_config_mobile_networks");
        if (this.mIsWifiOnly || z3 || hasUserRestriction) {
            Log.d(TAG, "updateState(), isSecondaryUser = " + z3 + ", mIsWifiOnly = " + this.mIsWifiOnly + ", isRestricted = " + hasUserRestriction);
            z = false;
        } else {
            int activeSubscriptionInfoCount = this.mSubscriptionManager.getActiveSubscriptionInfoCount();
            int callState = TelephonyManager.getDefault().getCallState();
            boolean z4 = callState == 0;
            z = false;
            if (!this.mIsAirplaneMode) {
                z = false;
                if (activeSubscriptionInfoCount > 0) {
                    z = false;
                    if (z4) {
                        z = !isAllRadioOff();
                    }
                }
            }
            Log.d(TAG, "updateState(), mIsAirplaneMode = " + this.mIsAirplaneMode + ", simNum = " + activeSubscriptionInfoCount + ", callstate = " + callState + ", isIdle = " + z4);
        }
        this.mApnSettingsEnabled = z;
        Log.d(TAG, "updateState(), mApnSettingsEnabled = " + this.mApnSettingsEnabled);
        updateStateResources();
        refreshState();
    }

    private final void updateStateResources() {
        if (this.mApnSettingsEnabled) {
            this.mApnStateLabel = PluginManager.getQuickSettingsPlugin(this.mContext).customizeApnSettingsTile(this.mApnSettingsEnabled, this.mEnableApnStateIconWrapper, this.mApnStateLabel);
        } else {
            this.mApnStateLabel = PluginManager.getQuickSettingsPlugin(this.mContext).customizeApnSettingsTile(this.mApnSettingsEnabled, this.mDisableApnStateIconWrapper, this.mApnStateLabel);
        }
    }

    @Override // com.android.systemui.qs.QSTile
    public Intent getLongClickIntent() {
        return null;
    }

    @Override // com.android.systemui.qs.QSTile
    public int getMetricsCategory() {
        return 111;
    }

    @Override // com.android.systemui.qs.QSTile
    public CharSequence getTileLabel() {
        this.mTileLabel = PluginManager.getQuickSettingsPlugin(this.mContext).getTileLabel("apnsettings");
        return this.mTileLabel;
    }

    @Override // com.android.systemui.qs.QSTile
    protected void handleClick() {
        updateState();
        Log.d(TAG, "handleClick(), mApnSettingsEnabled = " + this.mApnSettingsEnabled);
        if (this.mApnSettingsEnabled && BenesseExtension.getDchaState() == 0) {
            APN_SETTINGS.putExtra("sub_id", SubscriptionManager.getDefaultDataSubscriptionId());
            Log.d(TAG, "handleClick(), " + APN_SETTINGS);
            this.mHost.startActivityDismissingKeyguard(APN_SETTINGS);
        }
    }

    @Override // com.android.systemui.qs.QSTile
    protected void handleLongClick() {
        handleClick();
    }

    @Override // com.android.systemui.qs.QSTile
    protected void handleUpdateState(QSTile.State state, Object obj) {
        if (this.mApnSettingsEnabled) {
            state.icon = QsIconWrapper.get(this.mEnableApnStateIconWrapper.getIconId(), this.mEnableApnStateIconWrapper);
        } else {
            state.icon = QsIconWrapper.get(this.mDisableApnStateIconWrapper.getIconId(), this.mDisableApnStateIconWrapper);
        }
        state.label = this.mApnStateLabel;
        state.contentDescription = this.mApnStateLabel;
    }

    @Override // com.android.systemui.qs.QSTile
    public QSTile.State newTileState() {
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.qs.QSTile
    public void setListening(boolean z) {
        Log.d(TAG, "setListening(), listening = " + z);
        if (this.mListening == z) {
            return;
        }
        this.mListening = z;
        if (!z) {
            this.mContext.unregisterReceiver(this.mReceiver);
            TelephonyManager.getDefault().listen(this.mPhoneStateListener, 0);
            return;
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.AIRPLANE_MODE");
        intentFilter.addAction("android.intent.action.ACTION_EF_CSP_CONTENT_NOTIFY");
        intentFilter.addAction("android.intent.action.MSIM_MODE");
        intentFilter.addAction("android.intent.action.ACTION_MD_TYPE_CHANGE");
        intentFilter.addAction("mediatek.intent.action.LOCATED_PLMN_CHANGED");
        intentFilter.addAction("android.intent.action.ACTION_SET_PHONE_RAT_FAMILY_DONE");
        intentFilter.addAction("android.intent.action.ACTION_SUBINFO_CONTENT_CHANGE");
        intentFilter.addAction("android.intent.action.ACTION_SUBINFO_RECORD_UPDATED");
        this.mContext.registerReceiver(this.mReceiver, intentFilter);
        TelephonyManager.getDefault().listen(this.mPhoneStateListener, 32);
    }
}
