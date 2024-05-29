package com.android.systemui.qs.tiles;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.BenesseExtension;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.android.internal.logging.MetricsLogger;
import com.android.keyguard.R$string;
import com.android.settingslib.net.DataUsageController;
import com.android.systemui.qs.QSIconView;
import com.android.systemui.qs.QSTile;
import com.android.systemui.qs.SignalTileView;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.statusbar.policy.SignalCallbackAdapter;
import com.mediatek.systemui.PluginManager;
import com.mediatek.systemui.ext.IQuickSettingsPlugin;
/* loaded from: a.zip:com/android/systemui/qs/tiles/CellularTile.class */
public class CellularTile extends QSTile<QSTile.SignalState> {
    static final Intent CELLULAR_SETTINGS = new Intent().setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$DataUsageSummaryActivity"));
    private final NetworkController mController;
    private final DataUsageController mDataController;
    private final CellularDetailAdapter mDetailAdapter;
    private boolean mDisplayDataUsage;
    private QSTile.Icon mIcon;
    private IQuickSettingsPlugin mQuickSettingsPlugin;
    private final CellSignalCallback mSignalCallback;
    private TelephonyManager mTelephonyManager;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/qs/tiles/CellularTile$CallbackInfo.class */
    public static final class CallbackInfo {
        boolean activityIn;
        boolean activityOut;
        boolean airplaneModeEnabled;
        String dataContentDescription;
        int dataTypeIconId;
        boolean enabled;
        String enabledDesc;
        boolean isDataTypeIconWide;
        int mobileSignalIconId;
        boolean noSim;
        String signalContentDescription;
        boolean wifiEnabled;

        private CallbackInfo() {
        }

        /* synthetic */ CallbackInfo(CallbackInfo callbackInfo) {
            this();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/qs/tiles/CellularTile$CellSignalCallback.class */
    public final class CellSignalCallback extends SignalCallbackAdapter {
        private final CallbackInfo mInfo;
        final CellularTile this$0;

        private CellSignalCallback(CellularTile cellularTile) {
            this.this$0 = cellularTile;
            this.mInfo = new CallbackInfo(null);
        }

        /* synthetic */ CellSignalCallback(CellularTile cellularTile, CellSignalCallback cellSignalCallback) {
            this(cellularTile);
        }

        @Override // com.android.systemui.statusbar.policy.SignalCallbackAdapter, com.android.systemui.statusbar.policy.NetworkController.SignalCallback
        public void setIsAirplaneMode(NetworkController.IconState iconState) {
            this.mInfo.airplaneModeEnabled = iconState.visible;
            this.this$0.refreshState(this.mInfo);
        }

        @Override // com.android.systemui.statusbar.policy.SignalCallbackAdapter, com.android.systemui.statusbar.policy.NetworkController.SignalCallback
        public void setMobileDataEnabled(boolean z) {
            this.this$0.mDetailAdapter.setMobileDataEnabled(z);
        }

        @Override // com.android.systemui.statusbar.policy.SignalCallbackAdapter, com.android.systemui.statusbar.policy.NetworkController.SignalCallback
        public void setMobileDataIndicators(NetworkController.IconState iconState, NetworkController.IconState iconState2, int i, int i2, int i3, int i4, boolean z, boolean z2, String str, String str2, boolean z3, int i5) {
            if (iconState2 == null) {
                Log.d("CellularTile", "setMobileDataIndicator qsIcon = null, Not data sim, don't display");
                return;
            }
            this.mInfo.enabled = iconState2.visible;
            this.mInfo.mobileSignalIconId = iconState2.icon;
            this.mInfo.signalContentDescription = iconState2.contentDescription;
            this.mInfo.dataTypeIconId = i4;
            this.mInfo.dataContentDescription = str;
            this.mInfo.activityIn = z;
            this.mInfo.activityOut = z2;
            this.mInfo.enabledDesc = str2;
            CallbackInfo callbackInfo = this.mInfo;
            if (i4 == 0) {
                z3 = false;
            }
            callbackInfo.isDataTypeIconWide = z3;
            Log.d("CellularTile", "setMobileDataIndicators info.enabled = " + this.mInfo.enabled + " mInfo.mobileSignalIconId = " + this.mInfo.mobileSignalIconId + " mInfo.signalContentDescription = " + this.mInfo.signalContentDescription + " mInfo.dataTypeIconId = " + this.mInfo.dataTypeIconId + " mInfo.dataContentDescription = " + this.mInfo.dataContentDescription + " mInfo.activityIn = " + this.mInfo.activityIn + " mInfo.activityOut = " + this.mInfo.activityOut + " mInfo.enabledDesc = " + this.mInfo.enabledDesc + " mInfo.isDataTypeIconWide = " + this.mInfo.isDataTypeIconWide);
            this.this$0.refreshState(this.mInfo);
        }

        @Override // com.android.systemui.statusbar.policy.SignalCallbackAdapter, com.android.systemui.statusbar.policy.NetworkController.SignalCallback
        public void setNoSims(boolean z) {
            Log.d("CellularTile", "setNoSims, noSim = " + z);
            this.mInfo.noSim = z;
            if (this.mInfo.noSim) {
                this.mInfo.mobileSignalIconId = 0;
                this.mInfo.dataTypeIconId = 0;
                this.mInfo.enabled = true;
                this.mInfo.enabledDesc = this.this$0.mContext.getString(R$string.keyguard_missing_sim_message_short);
                this.mInfo.signalContentDescription = this.mInfo.enabledDesc;
            }
            this.this$0.refreshState(this.mInfo);
        }

        @Override // com.android.systemui.statusbar.policy.SignalCallbackAdapter, com.android.systemui.statusbar.policy.NetworkController.SignalCallback
        public void setWifiIndicators(boolean z, NetworkController.IconState iconState, NetworkController.IconState iconState2, boolean z2, boolean z3, String str) {
            this.mInfo.wifiEnabled = z;
            this.this$0.refreshState(this.mInfo);
        }
    }

    /* loaded from: a.zip:com/android/systemui/qs/tiles/CellularTile$CellularDetailAdapter.class */
    private final class CellularDetailAdapter implements QSTile.DetailAdapter {
        final CellularTile this$0;

        private CellularDetailAdapter(CellularTile cellularTile) {
            this.this$0 = cellularTile;
        }

        /* synthetic */ CellularDetailAdapter(CellularTile cellularTile, CellularDetailAdapter cellularDetailAdapter) {
            this(cellularTile);
        }

        @Override // com.android.systemui.qs.QSTile.DetailAdapter
        public View createDetailView(Context context, View view, ViewGroup viewGroup) {
            DataUsageDetailView dataUsageDetailView = (DataUsageDetailView) (view != null ? view : LayoutInflater.from(this.this$0.mContext).inflate(2130968613, viewGroup, false));
            DataUsageController.DataUsageInfo dataUsageInfo = this.this$0.mDataController.getDataUsageInfo();
            if (dataUsageInfo == null) {
                return dataUsageDetailView;
            }
            dataUsageDetailView.bind(dataUsageInfo);
            return dataUsageDetailView;
        }

        @Override // com.android.systemui.qs.QSTile.DetailAdapter
        public int getMetricsCategory() {
            return 117;
        }

        @Override // com.android.systemui.qs.QSTile.DetailAdapter
        public Intent getSettingsIntent() {
            if (BenesseExtension.getDchaState() != 0) {
                return null;
            }
            return CellularTile.CELLULAR_SETTINGS;
        }

        @Override // com.android.systemui.qs.QSTile.DetailAdapter
        public CharSequence getTitle() {
            return this.this$0.mContext.getString(2131493571);
        }

        @Override // com.android.systemui.qs.QSTile.DetailAdapter
        public Boolean getToggleState() {
            return this.this$0.mDataController.isMobileDataSupported() ? Boolean.valueOf(this.this$0.mDataController.isMobileDataEnabled()) : null;
        }

        public void setMobileDataEnabled(boolean z) {
            this.this$0.fireToggleStateChanged(z);
        }

        @Override // com.android.systemui.qs.QSTile.DetailAdapter
        public void setToggleState(boolean z) {
            MetricsLogger.action(this.this$0.mContext, 155, z);
            this.this$0.mDataController.setMobileDataEnabled(z);
            if (z) {
                this.this$0.disableDataForOtherSubscriptions();
            }
        }
    }

    public CellularTile(QSTile.Host host) {
        super(host);
        this.mSignalCallback = new CellSignalCallback(this, null);
        this.mController = host.getNetworkController();
        this.mDataController = this.mController.getMobileDataController();
        this.mDetailAdapter = new CellularDetailAdapter(this, null);
        this.mQuickSettingsPlugin = PluginManager.getQuickSettingsPlugin(this.mContext);
        this.mDisplayDataUsage = this.mQuickSettingsPlugin.customizeDisplayDataUsage(false);
        this.mIcon = QSTile.ResourceIcon.get(2130837716);
        this.mTelephonyManager = TelephonyManager.from(this.mContext);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void disableDataForOtherSubscriptions() {
        int[] activeSubscriptionIdList = SubscriptionManager.from(this.mContext).getActiveSubscriptionIdList();
        int defaultDataSubscriptionId = SubscriptionManager.getDefaultDataSubscriptionId();
        for (int i : activeSubscriptionIdList) {
            if (i != defaultDataSubscriptionId && this.mTelephonyManager.getDataEnabled(i)) {
                Log.d("CellularTile", "Disable other sub's data : " + i);
                this.mTelephonyManager.setDataEnabled(i, false);
            }
        }
    }

    public static String removeTrailingPeriod(String str) {
        if (str == null) {
            return null;
        }
        return str.endsWith(".") ? str.substring(0, str.length() - 1) : str;
    }

    @Override // com.android.systemui.qs.QSTile
    public QSIconView createTileView(Context context) {
        return new SignalTileView(context);
    }

    @Override // com.android.systemui.qs.QSTile
    public QSTile.DetailAdapter getDetailAdapter() {
        return this.mDetailAdapter;
    }

    @Override // com.android.systemui.qs.QSTile
    public Intent getLongClickIntent() {
        if (BenesseExtension.getDchaState() != 0) {
            return null;
        }
        return CELLULAR_SETTINGS;
    }

    @Override // com.android.systemui.qs.QSTile
    public int getMetricsCategory() {
        return 115;
    }

    @Override // com.android.systemui.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mDisplayDataUsage ? this.mContext.getString(2131493271) : this.mContext.getString(2131493571);
    }

    @Override // com.android.systemui.qs.QSTile
    protected void handleClick() {
        MetricsLogger.action(this.mContext, getMetricsCategory());
        if (this.mDataController.isMobileDataSupported() && isDefaultDataSimExist()) {
            showDetail(true);
        } else if (BenesseExtension.getDchaState() == 0) {
            this.mHost.startActivityDismissingKeyguard(CELLULAR_SETTINGS);
        }
    }

    @Override // com.android.systemui.qs.QSTile
    protected void handleSecondaryClick() {
        Log.d("CellularTile", "handleSecondaryClick()");
        if (this.mDisplayDataUsage) {
            handleClick();
        } else if (isDefaultDataSimExist()) {
            boolean isMobileDataEnabled = this.mDataController.isMobileDataSupported() ? this.mDataController.isMobileDataEnabled() : false;
            MetricsLogger.action(this.mContext, 155, !isMobileDataEnabled);
            this.mDataController.setMobileDataEnabled(!isMobileDataEnabled);
            if (isMobileDataEnabled) {
                return;
            }
            disableDataForOtherSubscriptions();
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.QSTile
    public void handleUpdateState(QSTile.SignalState signalState, Object obj) {
        if (this.mDisplayDataUsage) {
            Log.i("CellularTile", "customize datausage, displayDataUsage = " + this.mDisplayDataUsage);
            signalState.icon = this.mIcon;
            signalState.label = this.mContext.getString(2131493271);
            signalState.contentDescription = this.mContext.getString(2131493271);
            return;
        }
        CallbackInfo callbackInfo = (CallbackInfo) obj;
        CallbackInfo callbackInfo2 = callbackInfo;
        if (callbackInfo == null) {
            callbackInfo2 = this.mSignalCallback.mInfo;
        }
        Resources resources = this.mContext.getResources();
        int i = callbackInfo2.noSim ? 2130837725 : (!callbackInfo2.enabled || callbackInfo2.airplaneModeEnabled) ? 2130837740 : callbackInfo2.mobileSignalIconId > 0 ? callbackInfo2.mobileSignalIconId : 2130837751;
        signalState.icon = QSTile.ResourceIcon.get(i);
        signalState.isOverlayIconWide = callbackInfo2.isDataTypeIconWide;
        signalState.autoMirrorDrawable = !callbackInfo2.noSim;
        signalState.overlayIconId = (!callbackInfo2.enabled || callbackInfo2.dataTypeIconId <= 0 || callbackInfo2.airplaneModeEnabled) ? 0 : callbackInfo2.dataTypeIconId;
        signalState.filter = i != 2130837725;
        signalState.activityIn = callbackInfo2.enabled ? callbackInfo2.activityIn : false;
        signalState.activityOut = callbackInfo2.enabled ? callbackInfo2.activityOut : false;
        signalState.label = callbackInfo2.enabled ? removeTrailingPeriod(callbackInfo2.enabledDesc) : resources.getString(2131493542);
        String string = (!callbackInfo2.enabled || callbackInfo2.mobileSignalIconId <= 0) ? resources.getString(2131493393) : callbackInfo2.signalContentDescription;
        if (callbackInfo2.noSim) {
            signalState.contentDescription = signalState.label;
        } else {
            signalState.contentDescription = resources.getString(2131493453, callbackInfo2.enabled ? resources.getString(2131493417) : resources.getString(2131493418), string, signalState.label);
            signalState.minimalContentDescription = resources.getString(2131493453, resources.getString(2131493416), string, signalState.label);
        }
        signalState.contentDescription += "," + resources.getString(2131493911, getTileLabel());
        String name = Button.class.getName();
        signalState.expandedAccessibilityClassName = name;
        signalState.minimalAccessibilityClassName = name;
        signalState.value = this.mDataController.isMobileDataSupported() ? this.mDataController.isMobileDataEnabled() : false;
        if (this.mTelephonyManager.getNetworkOperator() == null || callbackInfo2.noSim || isDefaultDataSimExist()) {
            return;
        }
        Log.d("CellularTile", "handleUpdateState(), default data sim not exist");
        signalState.icon = QSTile.ResourceIcon.get(2130837715);
        signalState.label = resources.getString(2131493272);
        signalState.overlayIconId = 0;
        signalState.filter = true;
        signalState.activityIn = false;
        signalState.activityOut = false;
    }

    @Override // com.android.systemui.qs.QSTile
    public boolean isAvailable() {
        return this.mController.hasMobileDataFeature();
    }

    public boolean isDefaultDataSimExist() {
        int[] activeSubscriptionIdList = SubscriptionManager.from(this.mContext).getActiveSubscriptionIdList();
        int defaultDataSubscriptionId = SubscriptionManager.getDefaultDataSubscriptionId();
        Log.d("CellularTile", "isDefaultDataSimExist, Default data sub id : " + defaultDataSubscriptionId);
        for (int i : activeSubscriptionIdList) {
            if (i == defaultDataSubscriptionId) {
                return true;
            }
        }
        return false;
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // com.android.systemui.qs.QSTile
    public QSTile.SignalState newTileState() {
        return new QSTile.SignalState();
    }

    @Override // com.android.systemui.qs.QSTile
    public void setListening(boolean z) {
        if (z) {
            this.mController.addSignalCallback(this.mSignalCallback);
        } else {
            this.mController.removeSignalCallback(this.mSignalCallback);
        }
    }
}
