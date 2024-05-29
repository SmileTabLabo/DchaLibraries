package com.mediatek.systemui.qs.tiles.ext;

import android.content.Intent;
import android.telephony.SubscriptionManager;
import android.util.Log;
import com.android.settingslib.net.DataUsageController;
import com.android.systemui.qs.QSTile;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.statusbar.policy.SignalCallbackAdapter;
import com.mediatek.systemui.PluginManager;
import com.mediatek.systemui.statusbar.extcb.IconIdWrapper;
import com.mediatek.systemui.statusbar.util.SIMHelper;
/* loaded from: a.zip:com/mediatek/systemui/qs/tiles/ext/MobileDataTile.class */
public class MobileDataTile extends QSTile<QSTile.SignalState> {
    private static final int AIRPLANE_DATA_CONNECT = 2;
    private static final int DATA_CONNECT = 1;
    private static final int DATA_CONNECT_DISABLE = 3;
    private static final int DATA_DISCONNECT = 0;
    private static final int DATA_RADIO_OFF = 4;
    private static final boolean DEBUG = true;
    private static final int QS_MOBILE_DISABLE = 2130837723;
    private static final int QS_MOBILE_ENABLE = 2130837724;
    private final MobileDataSignalCallback mCallback;
    private final NetworkController mController;
    private int mDataConnectionState;
    private final DataUsageController mDataController;
    private int mDataStateIconId;
    private final IconIdWrapper mDisableStateIconIdWrapper;
    private final IconIdWrapper mEnableStateIconIdWrapper;
    private boolean mEnabled;
    private CharSequence mTileLabel;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/mediatek/systemui/qs/tiles/ext/MobileDataTile$CallbackInfo.class */
    public static final class CallbackInfo {
        public boolean activityIn;
        public boolean activityOut;
        public boolean airplaneModeEnabled;
        public int dataTypeIconId;
        public boolean enabled;
        public String enabledDesc;
        public int mobileSignalIconId;
        public boolean noSim;
        public boolean wifiConnected;
        public boolean wifiEnabled;

        private CallbackInfo() {
        }

        /* synthetic */ CallbackInfo(CallbackInfo callbackInfo) {
            this();
        }

        public String toString() {
            return "CallbackInfo[enabled=" + this.enabled + ",wifiEnabled=" + this.wifiEnabled + ",wifiConnected=" + this.wifiConnected + ",airplaneModeEnabled=" + this.airplaneModeEnabled + ",mobileSignalIconId=" + this.mobileSignalIconId + ",dataTypeIconId=" + this.dataTypeIconId + ",activityIn=" + this.activityIn + ",activityOut=" + this.activityOut + ",enabledDesc=" + this.enabledDesc + ",noSim=" + this.noSim + ']';
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/mediatek/systemui/qs/tiles/ext/MobileDataTile$MobileDataSignalCallback.class */
    public final class MobileDataSignalCallback extends SignalCallbackAdapter {
        final CallbackInfo mInfo;
        final MobileDataTile this$0;

        private MobileDataSignalCallback(MobileDataTile mobileDataTile) {
            this.this$0 = mobileDataTile;
            this.mInfo = new CallbackInfo(null);
        }

        /* synthetic */ MobileDataSignalCallback(MobileDataTile mobileDataTile, MobileDataSignalCallback mobileDataSignalCallback) {
            this(mobileDataTile);
        }

        @Override // com.android.systemui.statusbar.policy.SignalCallbackAdapter, com.android.systemui.statusbar.policy.NetworkController.SignalCallback
        public void setIsAirplaneMode(NetworkController.IconState iconState) {
            this.mInfo.airplaneModeEnabled = iconState.visible;
            if (this.mInfo.airplaneModeEnabled) {
                this.mInfo.mobileSignalIconId = 0;
                this.mInfo.dataTypeIconId = 0;
                this.mInfo.enabled = false;
            }
            this.this$0.refreshState(this.mInfo);
        }

        @Override // com.android.systemui.statusbar.policy.SignalCallbackAdapter, com.android.systemui.statusbar.policy.NetworkController.SignalCallback
        public void setMobileDataEnabled(boolean z) {
            this.this$0.refreshState(this.mInfo);
        }

        @Override // com.android.systemui.statusbar.policy.SignalCallbackAdapter, com.android.systemui.statusbar.policy.NetworkController.SignalCallback
        public void setMobileDataIndicators(NetworkController.IconState iconState, NetworkController.IconState iconState2, int i, int i2, int i3, int i4, boolean z, boolean z2, String str, String str2, boolean z3, int i5) {
            if (iconState2 == null) {
                return;
            }
            this.mInfo.enabled = iconState2.visible;
            this.mInfo.mobileSignalIconId = iconState2.icon;
            this.mInfo.dataTypeIconId = i4;
            this.mInfo.activityIn = z;
            this.mInfo.activityOut = z2;
            this.mInfo.enabledDesc = str2;
            Log.d(this.this$0.TAG, "setMobileDataIndicators mInfo=" + this.mInfo);
            this.this$0.refreshState(this.mInfo);
        }

        @Override // com.android.systemui.statusbar.policy.SignalCallbackAdapter, com.android.systemui.statusbar.policy.NetworkController.SignalCallback
        public void setNoSims(boolean z) {
            this.mInfo.noSim = z;
            if (this.mInfo.noSim) {
                this.mInfo.mobileSignalIconId = 0;
                this.mInfo.dataTypeIconId = 0;
                this.mInfo.enabled = false;
                Log.d(this.this$0.TAG, "setNoSims noSim=" + z);
            }
            this.this$0.refreshState(this.mInfo);
        }

        @Override // com.android.systemui.statusbar.policy.SignalCallbackAdapter, com.android.systemui.statusbar.policy.NetworkController.SignalCallback
        public void setWifiIndicators(boolean z, NetworkController.IconState iconState, NetworkController.IconState iconState2, boolean z2, boolean z3, String str) {
            this.mInfo.wifiEnabled = z;
            this.mInfo.wifiConnected = iconState2.visible;
            this.this$0.refreshState(this.mInfo);
        }
    }

    public MobileDataTile(QSTile.Host host) {
        super(host);
        this.mDataConnectionState = 0;
        this.mDataStateIconId = QS_MOBILE_DISABLE;
        this.mEnableStateIconIdWrapper = new IconIdWrapper();
        this.mDisableStateIconIdWrapper = new IconIdWrapper();
        this.mCallback = new MobileDataSignalCallback(this, null);
        this.mController = host.getNetworkController();
        this.mDataController = this.mController.getMobileDataController();
        Log.d(this.TAG, "create MobileDataTile");
    }

    private final boolean isDefaultDataSimRadioOn() {
        int defaultDataSubscriptionId = SubscriptionManager.getDefaultDataSubscriptionId();
        boolean isRadioOn = defaultDataSubscriptionId >= 0 ? SIMHelper.isRadioOn(defaultDataSubscriptionId) : false;
        Log.d(this.TAG, "isDefaultDataSimRadioOn subId=" + defaultDataSubscriptionId + ", isRadioOn=" + isRadioOn);
        return isRadioOn;
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
        this.mTileLabel = PluginManager.getQuickSettingsPlugin(this.mContext).getTileLabel("mobiledata");
        return this.mTileLabel;
    }

    @Override // com.android.systemui.qs.QSTile
    protected void handleClick() {
        int defaultDataSubscriptionId;
        if (this.mDataController.isMobileDataSupported() && this.mEnabled) {
            if (((QSTile.SignalState) this.mState).connected || ((defaultDataSubscriptionId = SubscriptionManager.getDefaultDataSubscriptionId()) >= 0 && SIMHelper.isRadioOn(defaultDataSubscriptionId))) {
                this.mDataController.setMobileDataEnabled(!((QSTile.SignalState) this.mState).connected);
            }
        }
    }

    @Override // com.android.systemui.qs.QSTile
    protected void handleLongClick() {
        handleClick();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.QSTile
    public void handleUpdateState(QSTile.SignalState signalState, Object obj) {
        Log.d(this.TAG, "handleUpdateState arg=" + obj);
        CallbackInfo callbackInfo = (CallbackInfo) obj;
        CallbackInfo callbackInfo2 = callbackInfo;
        if (callbackInfo == null) {
            callbackInfo2 = this.mCallback.mInfo;
        }
        boolean isDefaultDataSimRadioOn = (!this.mDataController.isMobileDataSupported() || callbackInfo2.noSim || callbackInfo2.airplaneModeEnabled) ? false : isDefaultDataSimRadioOn();
        boolean z = (isDefaultDataSimRadioOn && this.mDataController.isMobileDataEnabled()) ? callbackInfo2.mobileSignalIconId > 0 : false;
        boolean z2 = callbackInfo2.mobileSignalIconId > 0 && callbackInfo2.enabledDesc == null;
        this.mEnabled = isDefaultDataSimRadioOn;
        signalState.connected = z;
        signalState.activityIn = callbackInfo2.enabled ? callbackInfo2.activityIn : false;
        signalState.activityOut = callbackInfo2.enabled ? callbackInfo2.activityOut : false;
        signalState.filter = true;
        this.mEnableStateIconIdWrapper.setResources(this.mContext.getResources());
        this.mDisableStateIconIdWrapper.setResources(this.mContext.getResources());
        if (!isDefaultDataSimRadioOn) {
            this.mDataConnectionState = 3;
            this.mDataStateIconId = QS_MOBILE_DISABLE;
            this.mDisableStateIconIdWrapper.setIconId(this.mDataStateIconId);
            signalState.label = PluginManager.getQuickSettingsPlugin(this.mContext).customizeDataConnectionTile(this.mDataConnectionState, this.mDisableStateIconIdWrapper, this.mContext.getString(2131493270));
            signalState.icon = QsIconWrapper.get(this.mDisableStateIconIdWrapper.getIconId(), this.mDisableStateIconIdWrapper);
        } else if (z) {
            this.mDataConnectionState = 1;
            this.mDataStateIconId = QS_MOBILE_ENABLE;
            this.mEnableStateIconIdWrapper.setIconId(this.mDataStateIconId);
            signalState.label = PluginManager.getQuickSettingsPlugin(this.mContext).customizeDataConnectionTile(this.mDataConnectionState, this.mEnableStateIconIdWrapper, this.mContext.getString(2131493270));
            signalState.icon = QsIconWrapper.get(this.mEnableStateIconIdWrapper.getIconId(), this.mEnableStateIconIdWrapper);
        } else if (z2) {
            this.mDataConnectionState = 0;
            this.mDataStateIconId = QS_MOBILE_DISABLE;
            this.mDisableStateIconIdWrapper.setIconId(this.mDataStateIconId);
            signalState.label = PluginManager.getQuickSettingsPlugin(this.mContext).customizeDataConnectionTile(this.mDataConnectionState, this.mDisableStateIconIdWrapper, this.mContext.getString(2131493270));
            signalState.icon = QsIconWrapper.get(this.mDisableStateIconIdWrapper.getIconId(), this.mDisableStateIconIdWrapper);
        } else {
            this.mDataConnectionState = 0;
            this.mDataStateIconId = QS_MOBILE_DISABLE;
            this.mDisableStateIconIdWrapper.setIconId(this.mDataStateIconId);
            signalState.label = PluginManager.getQuickSettingsPlugin(this.mContext).customizeDataConnectionTile(this.mDataConnectionState, this.mDisableStateIconIdWrapper, this.mContext.getString(2131493270));
            signalState.icon = QsIconWrapper.get(this.mDisableStateIconIdWrapper.getIconId(), this.mDisableStateIconIdWrapper);
        }
        this.mTileLabel = signalState.label;
        Log.d(this.TAG, "handleUpdateState state=" + signalState);
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // com.android.systemui.qs.QSTile
    public QSTile.SignalState newTileState() {
        return new QSTile.SignalState();
    }

    @Override // com.android.systemui.qs.QSTile
    public void setListening(boolean z) {
        Log.d(this.TAG, "setListening = " + z);
        if (z) {
            this.mController.addSignalCallback(this.mCallback);
        } else {
            this.mController.removeSignalCallback(this.mCallback);
        }
    }
}
