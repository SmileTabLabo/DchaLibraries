package com.android.systemui.qs.tiles;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.BenesseExtension;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import com.android.internal.logging.MetricsLogger;
import com.android.settingslib.wifi.AccessPoint;
import com.android.systemui.qs.QSDetailItems;
import com.android.systemui.qs.QSIconView;
import com.android.systemui.qs.QSTile;
import com.android.systemui.qs.SignalTileView;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.statusbar.policy.SignalCallbackAdapter;
import java.util.List;
/* loaded from: a.zip:com/android/systemui/qs/tiles/WifiTile.class */
public class WifiTile extends QSTile<QSTile.SignalState> {
    private static final Intent WIFI_SETTINGS = new Intent("android.settings.WIFI_SETTINGS");
    private final NetworkController mController;
    private final WifiDetailAdapter mDetailAdapter;
    protected final WifiSignalCallback mSignalCallback;
    private final QSTile.SignalState mStateBeforeClick;
    private final NetworkController.AccessPointController mWifiController;

    /* JADX INFO: Access modifiers changed from: protected */
    /* loaded from: a.zip:com/android/systemui/qs/tiles/WifiTile$CallbackInfo.class */
    public static final class CallbackInfo {
        boolean activityIn;
        boolean activityOut;
        boolean connected;
        boolean enabled;
        String enabledDesc;
        String wifiSignalContentDescription;
        int wifiSignalIconId;

        protected CallbackInfo() {
        }

        public String toString() {
            return "CallbackInfo[enabled=" + this.enabled + ",connected=" + this.connected + ",wifiSignalIconId=" + this.wifiSignalIconId + ",enabledDesc=" + this.enabledDesc + ",activityIn=" + this.activityIn + ",activityOut=" + this.activityOut + ",wifiSignalContentDescription=" + this.wifiSignalContentDescription + ']';
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/qs/tiles/WifiTile$WifiDetailAdapter.class */
    public final class WifiDetailAdapter implements QSTile.DetailAdapter, NetworkController.AccessPointController.AccessPointCallback, QSDetailItems.Callback {
        private AccessPoint[] mAccessPoints;
        private QSDetailItems mItems;
        final WifiTile this$0;

        private WifiDetailAdapter(WifiTile wifiTile) {
            this.this$0 = wifiTile;
        }

        /* synthetic */ WifiDetailAdapter(WifiTile wifiTile, WifiDetailAdapter wifiDetailAdapter) {
            this(wifiTile);
        }

        private void updateItems() {
            if (this.mItems == null) {
                return;
            }
            QSDetailItems.Item[] itemArr = null;
            if (this.mAccessPoints != null) {
                QSDetailItems.Item[] itemArr2 = new QSDetailItems.Item[this.mAccessPoints.length];
                int i = 0;
                while (true) {
                    itemArr = itemArr2;
                    if (i >= this.mAccessPoints.length) {
                        break;
                    }
                    AccessPoint accessPoint = this.mAccessPoints[i];
                    QSDetailItems.Item item = new QSDetailItems.Item();
                    item.tag = accessPoint;
                    item.icon = this.this$0.mWifiController.getIcon(accessPoint);
                    item.line1 = accessPoint.getSsid();
                    item.line2 = accessPoint.isActive() ? accessPoint.getSummary() : null;
                    item.overlay = accessPoint.getSecurity() != 0 ? this.this$0.mContext.getDrawable(2130837972) : null;
                    itemArr2[i] = item;
                    i++;
                }
            }
            this.mItems.setItems(itemArr);
        }

        @Override // com.android.systemui.qs.QSTile.DetailAdapter
        public View createDetailView(Context context, View view, ViewGroup viewGroup) {
            if (WifiTile.DEBUG) {
                Log.d(this.this$0.TAG, "createDetailView convertView=" + (view != null));
            }
            this.mAccessPoints = null;
            this.this$0.mWifiController.scanForAccessPoints();
            this.this$0.fireScanStateChanged(true);
            this.mItems = QSDetailItems.convertOrInflate(context, view, viewGroup);
            this.mItems.setTagSuffix("Wifi");
            this.mItems.setCallback(this);
            this.mItems.setEmptyState(2130837760, 2131493553);
            updateItems();
            setItemsVisible(((QSTile.SignalState) this.this$0.mState).value);
            return this.mItems;
        }

        @Override // com.android.systemui.qs.QSTile.DetailAdapter
        public int getMetricsCategory() {
            return 152;
        }

        @Override // com.android.systemui.qs.QSTile.DetailAdapter
        public Intent getSettingsIntent() {
            if (BenesseExtension.getDchaState() != 0) {
                return null;
            }
            return WifiTile.WIFI_SETTINGS;
        }

        @Override // com.android.systemui.qs.QSTile.DetailAdapter
        public CharSequence getTitle() {
            return this.this$0.mContext.getString(2131493548);
        }

        @Override // com.android.systemui.qs.QSTile.DetailAdapter
        public Boolean getToggleState() {
            return Boolean.valueOf(((QSTile.SignalState) this.this$0.mState).value);
        }

        @Override // com.android.systemui.statusbar.policy.NetworkController.AccessPointController.AccessPointCallback
        public void onAccessPointsChanged(List<AccessPoint> list) {
            this.mAccessPoints = (AccessPoint[]) list.toArray(new AccessPoint[list.size()]);
            updateItems();
            if (list == null || list.size() <= 0) {
                return;
            }
            this.this$0.fireScanStateChanged(false);
        }

        @Override // com.android.systemui.qs.QSDetailItems.Callback
        public void onDetailItemClick(QSDetailItems.Item item) {
            if (item == null || item.tag == null) {
                return;
            }
            AccessPoint accessPoint = (AccessPoint) item.tag;
            if (!accessPoint.isActive() && this.this$0.mWifiController.connect(accessPoint)) {
                this.this$0.mHost.collapsePanels();
            }
            this.this$0.showDetail(false);
        }

        @Override // com.android.systemui.qs.QSDetailItems.Callback
        public void onDetailItemDisconnect(QSDetailItems.Item item) {
        }

        @Override // com.android.systemui.statusbar.policy.NetworkController.AccessPointController.AccessPointCallback
        public void onSettingsActivityTriggered(Intent intent) {
            this.this$0.mHost.startActivityDismissingKeyguard(intent);
        }

        public void setItemsVisible(boolean z) {
            if (this.mItems == null) {
                return;
            }
            this.mItems.setItemsVisible(z);
        }

        @Override // com.android.systemui.qs.QSTile.DetailAdapter
        public void setToggleState(boolean z) {
            if (WifiTile.DEBUG) {
                Log.d(this.this$0.TAG, "setToggleState " + z);
            }
            MetricsLogger.action(this.this$0.mContext, 153, z);
            this.this$0.mController.setWifiEnabled(z);
            this.this$0.showDetail(false);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* loaded from: a.zip:com/android/systemui/qs/tiles/WifiTile$WifiSignalCallback.class */
    public final class WifiSignalCallback extends SignalCallbackAdapter {
        final CallbackInfo mInfo = new CallbackInfo();
        final WifiTile this$0;

        protected WifiSignalCallback(WifiTile wifiTile) {
            this.this$0 = wifiTile;
        }

        @Override // com.android.systemui.statusbar.policy.SignalCallbackAdapter, com.android.systemui.statusbar.policy.NetworkController.SignalCallback
        public void setWifiIndicators(boolean z, NetworkController.IconState iconState, NetworkController.IconState iconState2, boolean z2, boolean z3, String str) {
            if (WifiTile.DEBUG) {
                Log.d(this.this$0.TAG, "onWifiSignalChanged enabled=" + z);
            }
            this.mInfo.enabled = z;
            this.mInfo.connected = iconState2.visible;
            this.mInfo.wifiSignalIconId = iconState2.icon;
            this.mInfo.enabledDesc = str;
            this.mInfo.activityIn = z2;
            this.mInfo.activityOut = z3;
            this.mInfo.wifiSignalContentDescription = iconState2.contentDescription;
            this.this$0.refreshState(this.mInfo);
        }
    }

    public WifiTile(QSTile.Host host) {
        super(host);
        this.mStateBeforeClick = newTileState();
        this.mSignalCallback = new WifiSignalCallback(this);
        this.mController = host.getNetworkController();
        this.mWifiController = this.mController.getAccessPointController();
        this.mDetailAdapter = new WifiDetailAdapter(this, null);
    }

    private static String removeDoubleQuotes(String str) {
        if (str == null) {
            return null;
        }
        int length = str.length();
        return (length > 1 && str.charAt(0) == '\"' && str.charAt(length - 1) == '\"') ? str.substring(1, length - 1) : str;
    }

    @Override // com.android.systemui.qs.QSTile
    protected String composeChangeAnnouncement() {
        return ((QSTile.SignalState) this.mState).value ? this.mContext.getString(2131493452) : this.mContext.getString(2131493451);
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
        return WIFI_SETTINGS;
    }

    @Override // com.android.systemui.qs.QSTile
    public int getMetricsCategory() {
        return 126;
    }

    @Override // com.android.systemui.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(2131493548);
    }

    @Override // com.android.systemui.qs.QSTile
    protected void handleClick() {
        if (!this.mWifiController.canConfigWifi() && BenesseExtension.getDchaState() == 0) {
            this.mHost.startActivityDismissingKeyguard(new Intent("android.settings.WIFI_SETTINGS"));
            return;
        }
        if (!((QSTile.SignalState) this.mState).value) {
            this.mController.setWifiEnabled(true);
            ((QSTile.SignalState) this.mState).value = true;
        }
        showDetail(true);
    }

    @Override // com.android.systemui.qs.QSTile
    protected void handleSecondaryClick() {
        ((QSTile.SignalState) this.mState).copyTo(this.mStateBeforeClick);
        MetricsLogger.action(this.mContext, getMetricsCategory(), !((QSTile.SignalState) this.mState).value);
        this.mController.setWifiEnabled(!((QSTile.SignalState) this.mState).value);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v54, types: [java.lang.CharSequence] */
    @Override // com.android.systemui.qs.QSTile
    public void handleUpdateState(QSTile.SignalState signalState, Object obj) {
        if (DEBUG) {
            Log.d(this.TAG, "handleUpdateState arg=" + obj);
        }
        CallbackInfo callbackInfo = (CallbackInfo) obj;
        CallbackInfo callbackInfo2 = callbackInfo;
        if (callbackInfo == null) {
            callbackInfo2 = this.mSignalCallback.mInfo;
        }
        boolean z = callbackInfo2.enabled && callbackInfo2.wifiSignalIconId > 0 && callbackInfo2.enabledDesc != null;
        boolean z2 = callbackInfo2.wifiSignalIconId > 0 && callbackInfo2.enabledDesc == null;
        if (signalState.value != callbackInfo2.enabled) {
            this.mDetailAdapter.setItemsVisible(callbackInfo2.enabled);
            fireToggleStateChanged(callbackInfo2.enabled);
        }
        signalState.value = callbackInfo2.enabled;
        signalState.connected = z;
        signalState.activityIn = callbackInfo2.enabled ? callbackInfo2.activityIn : false;
        signalState.activityOut = callbackInfo2.enabled ? callbackInfo2.activityOut : false;
        signalState.filter = true;
        StringBuffer stringBuffer = new StringBuffer();
        StringBuffer stringBuffer2 = new StringBuffer();
        Resources resources = this.mContext.getResources();
        if (!signalState.value) {
            signalState.icon = QSTile.ResourceIcon.get(2130837761);
            signalState.label = resources.getString(2131493548);
        } else if (z) {
            signalState.icon = QSTile.ResourceIcon.get(callbackInfo2.wifiSignalIconId);
            signalState.label = removeDoubleQuotes(callbackInfo2.enabledDesc);
        } else if (z2) {
            signalState.icon = QSTile.ResourceIcon.get(2130837762);
            signalState.label = resources.getString(2131493548);
        } else {
            signalState.icon = QSTile.ResourceIcon.get(2130837768);
            signalState.label = resources.getString(2131493548);
        }
        stringBuffer.append(this.mContext.getString(2131493548)).append(",");
        if (signalState.value) {
            stringBuffer2.append(resources.getString(2131493552)).append(",");
            if (z) {
                stringBuffer.append(callbackInfo2.wifiSignalContentDescription).append(",");
                stringBuffer.append(removeDoubleQuotes(callbackInfo2.enabledDesc));
                stringBuffer2.append(callbackInfo2.wifiSignalContentDescription).append(",");
                stringBuffer2.append(removeDoubleQuotes(callbackInfo2.enabledDesc));
            }
        } else {
            stringBuffer2.append(resources.getString(2131493551));
        }
        signalState.minimalContentDescription = stringBuffer;
        stringBuffer2.append(",").append(resources.getString(2131493911, getTileLabel()));
        signalState.contentDescription = stringBuffer2;
        String str = signalState.label;
        if (signalState.connected) {
            str = resources.getString(2131493383, signalState.label);
        }
        signalState.dualLabelContentDescription = str;
        signalState.expandedAccessibilityClassName = Button.class.getName();
        signalState.minimalAccessibilityClassName = Switch.class.getName();
    }

    @Override // com.android.systemui.qs.QSTile
    public boolean isAvailable() {
        return this.mContext.getPackageManager().hasSystemFeature("android.hardware.wifi");
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // com.android.systemui.qs.QSTile
    public QSTile.SignalState newTileState() {
        return new QSTile.SignalState();
    }

    @Override // com.android.systemui.qs.QSTile
    public void setDetailListening(boolean z) {
        if (z) {
            this.mWifiController.addAccessPointCallback(this.mDetailAdapter);
        } else {
            this.mWifiController.removeAccessPointCallback(this.mDetailAdapter);
        }
    }

    @Override // com.android.systemui.qs.QSTile
    public void setListening(boolean z) {
        if (z) {
            this.mController.addSignalCallback(this.mSignalCallback);
        } else {
            this.mController.removeSignalCallback(this.mSignalCallback);
        }
    }

    @Override // com.android.systemui.qs.QSTile
    protected boolean shouldAnnouncementBeDelayed() {
        return this.mStateBeforeClick.value == ((QSTile.SignalState) this.mState).value;
    }
}
