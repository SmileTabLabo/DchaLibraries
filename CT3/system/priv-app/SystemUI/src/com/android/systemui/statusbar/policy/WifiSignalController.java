package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import com.android.internal.util.AsyncChannel;
import com.android.settingslib.wifi.WifiStatusTracker;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.statusbar.policy.SignalController;
import com.mediatek.systemui.PluginManager;
import com.mediatek.systemui.ext.IMobileIconExt;
import com.mediatek.systemui.statusbar.util.FeatureOptions;
import java.util.BitSet;
import java.util.Objects;
/* loaded from: a.zip:com/android/systemui/statusbar/policy/WifiSignalController.class */
public class WifiSignalController extends SignalController<WifiState, SignalController.IconGroup> {
    private final boolean mHasMobileData;
    private IMobileIconExt mMobileIconExt;
    private final AsyncChannel mWifiChannel;
    private final WifiManager mWifiManager;
    private final WifiStatusTracker mWifiTracker;

    /* loaded from: a.zip:com/android/systemui/statusbar/policy/WifiSignalController$WifiHandler.class */
    private class WifiHandler extends Handler {
        final WifiSignalController this$0;

        private WifiHandler(WifiSignalController wifiSignalController) {
            this.this$0 = wifiSignalController;
        }

        /* synthetic */ WifiHandler(WifiSignalController wifiSignalController, WifiHandler wifiHandler) {
            this(wifiSignalController);
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    this.this$0.setActivity(message.arg1);
                    return;
                case 69632:
                    if (message.arg1 == 0) {
                        this.this$0.mWifiChannel.sendMessage(Message.obtain(this, 69633));
                        return;
                    } else {
                        Log.e(this.this$0.mTag, "Failed to connect to wifi");
                        return;
                    }
                default:
                    return;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: a.zip:com/android/systemui/statusbar/policy/WifiSignalController$WifiState.class */
    public static class WifiState extends SignalController.State {
        String ssid;

        WifiState() {
        }

        @Override // com.android.systemui.statusbar.policy.SignalController.State
        public void copyFrom(SignalController.State state) {
            super.copyFrom(state);
            this.ssid = ((WifiState) state).ssid;
        }

        @Override // com.android.systemui.statusbar.policy.SignalController.State
        public boolean equals(Object obj) {
            return super.equals(obj) ? Objects.equals(((WifiState) obj).ssid, this.ssid) : false;
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // com.android.systemui.statusbar.policy.SignalController.State
        public void toString(StringBuilder sb) {
            super.toString(sb);
            sb.append(',').append("ssid=").append(this.ssid);
        }
    }

    public WifiSignalController(Context context, boolean z, CallbackHandler callbackHandler, NetworkControllerImpl networkControllerImpl) {
        super("WifiSignalController", context, 1, callbackHandler, networkControllerImpl);
        this.mWifiManager = (WifiManager) context.getSystemService("wifi");
        this.mWifiTracker = new WifiStatusTracker(this.mWifiManager);
        this.mHasMobileData = z;
        WifiHandler wifiHandler = new WifiHandler(this, null);
        this.mWifiChannel = new AsyncChannel();
        Messenger wifiServiceMessenger = this.mWifiManager.getWifiServiceMessenger();
        if (wifiServiceMessenger != null) {
            this.mWifiChannel.connect(context, wifiHandler, wifiServiceMessenger);
        }
        WifiState wifiState = (WifiState) this.mCurrentState;
        SignalController.IconGroup iconGroup = new SignalController.IconGroup("Wi-Fi Icons", WifiIcons.WIFI_SIGNAL_STRENGTH, WifiIcons.QS_WIFI_SIGNAL_STRENGTH, AccessibilityContentDescriptions.WIFI_CONNECTION_STRENGTH, 2130838332, 2130837768, 2130838332, 2130837768, 2131492926);
        ((WifiState) this.mLastState).iconGroup = iconGroup;
        wifiState.iconGroup = iconGroup;
        this.mMobileIconExt = PluginManager.getMobileIconExt(context);
    }

    private int getActiveType() {
        int i = 0;
        if (((WifiState) this.mCurrentState).activityIn && ((WifiState) this.mCurrentState).activityOut) {
            i = 3;
        } else if (((WifiState) this.mCurrentState).activityIn) {
            i = 1;
        } else if (((WifiState) this.mCurrentState).activityOut) {
            i = 2;
        }
        return i;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.policy.SignalController
    public WifiState cleanState() {
        return new WifiState();
    }

    /* JADX WARN: Code restructure failed: missing block: B:11:0x0038, code lost:
        if (((com.android.systemui.statusbar.policy.WifiSignalController.WifiState) r4.mCurrentState).activityOut != false) goto L12;
     */
    @Override // com.android.systemui.statusbar.policy.SignalController
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public int getCurrentIconId() {
        if (FeatureOptions.MTK_A1_SUPPORT) {
            return super.getCurrentIconId();
        }
        int currentIconId = super.getCurrentIconId();
        int i = currentIconId;
        if (((WifiState) this.mCurrentState).connected) {
            if (!((WifiState) this.mCurrentState).activityIn) {
                i = currentIconId;
            }
            int activeType = getActiveType();
            i = currentIconId;
            if (activeType < WifiIcons.WIFI_SIGNAL_STRENGTH_INOUT[0].length) {
                i = WifiIcons.WIFI_SIGNAL_STRENGTH_INOUT[((WifiState) this.mCurrentState).level][activeType];
            }
        }
        return i;
    }

    public void handleBroadcast(Intent intent) {
        this.mWifiTracker.handleBroadcast(intent);
        ((WifiState) this.mCurrentState).enabled = this.mWifiTracker.enabled;
        ((WifiState) this.mCurrentState).connected = this.mWifiTracker.connected;
        ((WifiState) this.mCurrentState).ssid = this.mWifiTracker.ssid;
        ((WifiState) this.mCurrentState).rssi = this.mWifiTracker.rssi;
        ((WifiState) this.mCurrentState).level = this.mWifiTracker.level;
        notifyListenersIfNecessary();
    }

    @Override // com.android.systemui.statusbar.policy.SignalController
    public void notifyListeners(NetworkController.SignalCallback signalCallback) {
        boolean z;
        boolean z2 = false;
        if (((WifiState) this.mCurrentState).enabled) {
            z = true;
            if (!((WifiState) this.mCurrentState).connected) {
                z = true;
                if (this.mHasMobileData) {
                    z = false;
                }
            }
        } else {
            z = false;
        }
        String str = z ? ((WifiState) this.mCurrentState).ssid : null;
        boolean z3 = z && ((WifiState) this.mCurrentState).ssid != null;
        String stringIfExists = getStringIfExists(getContentDescription());
        String str2 = stringIfExists;
        if (((WifiState) this.mCurrentState).inetCondition == 0) {
            str2 = stringIfExists + "," + this.mContext.getString(2131493909);
        }
        NetworkController.IconState iconState = new NetworkController.IconState(z, getCurrentIconId(), str2);
        NetworkController.IconState iconState2 = new NetworkController.IconState(((WifiState) this.mCurrentState).connected, getQsCurrentIconId(), str2);
        boolean z4 = ((WifiState) this.mCurrentState).enabled;
        boolean z5 = z3 ? ((WifiState) this.mCurrentState).activityIn : false;
        if (z3) {
            z2 = ((WifiState) this.mCurrentState).activityOut;
        }
        signalCallback.setWifiIndicators(z4, iconState, iconState2, z5, z2, str);
    }

    void setActivity(int i) {
        ((WifiState) this.mCurrentState).activityIn = i != 3 ? i == 1 : true;
        WifiState wifiState = (WifiState) this.mCurrentState;
        boolean z = true;
        if (i != 3) {
            z = i == 2;
        }
        wifiState.activityOut = z;
        notifyListenersIfNecessary();
    }

    @Override // com.android.systemui.statusbar.policy.SignalController
    public void updateConnectivity(BitSet bitSet, BitSet bitSet2) {
        ((WifiState) this.mCurrentState).inetCondition = bitSet2.get(this.mTransportType) ? 1 : 0;
        Log.d("WifiSignalController", "mCurrentState.inetCondition = " + ((WifiState) this.mCurrentState).inetCondition);
        ((WifiState) this.mCurrentState).inetCondition = this.mMobileIconExt.customizeWifiNetCondition(((WifiState) this.mCurrentState).inetCondition);
        notifyListenersIfNecessary();
    }
}
