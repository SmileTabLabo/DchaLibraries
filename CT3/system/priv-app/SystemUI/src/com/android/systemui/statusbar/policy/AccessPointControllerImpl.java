package com.android.systemui.statusbar.policy;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.BenesseExtension;
import android.os.Looper;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;
import com.android.settingslib.wifi.AccessPoint;
import com.android.settingslib.wifi.WifiTracker;
import com.android.systemui.statusbar.policy.NetworkController;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
/* loaded from: a.zip:com/android/systemui/statusbar/policy/AccessPointControllerImpl.class */
public class AccessPointControllerImpl implements NetworkController.AccessPointController, WifiTracker.WifiListener {
    private static final boolean DEBUG = Log.isLoggable("AccessPointController", 3);
    private static final int[] ICONS = {2130837763, 2130837764, 2130837765, 2130837766, 2130837767};
    private final Context mContext;
    private final UserManager mUserManager;
    private final WifiTracker mWifiTracker;
    private final ArrayList<NetworkController.AccessPointController.AccessPointCallback> mCallbacks = new ArrayList<>();
    private final WifiManager.ActionListener mConnectListener = new WifiManager.ActionListener(this) { // from class: com.android.systemui.statusbar.policy.AccessPointControllerImpl.1
        final AccessPointControllerImpl this$0;

        {
            this.this$0 = this;
        }

        public void onFailure(int i) {
            if (AccessPointControllerImpl.DEBUG) {
                Log.d("AccessPointController", "connect failure reason=" + i);
            }
        }

        public void onSuccess() {
            if (AccessPointControllerImpl.DEBUG) {
                Log.d("AccessPointController", "connect success");
            }
        }
    };
    private int mCurrentUser = ActivityManager.getCurrentUser();

    public AccessPointControllerImpl(Context context, Looper looper) {
        this.mContext = context;
        this.mUserManager = (UserManager) this.mContext.getSystemService("user");
        this.mWifiTracker = new WifiTracker(context, this, looper, false, true);
    }

    private void fireAcccessPointsCallback(List<AccessPoint> list) {
        for (NetworkController.AccessPointController.AccessPointCallback accessPointCallback : this.mCallbacks) {
            accessPointCallback.onAccessPointsChanged(list);
        }
    }

    private void fireSettingsIntentCallback(Intent intent) {
        for (NetworkController.AccessPointController.AccessPointCallback accessPointCallback : this.mCallbacks) {
            accessPointCallback.onSettingsActivityTriggered(intent);
        }
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.AccessPointController
    public void addAccessPointCallback(NetworkController.AccessPointController.AccessPointCallback accessPointCallback) {
        if (accessPointCallback == null || this.mCallbacks.contains(accessPointCallback)) {
            return;
        }
        if (DEBUG) {
            Log.d("AccessPointController", "addCallback " + accessPointCallback);
        }
        this.mCallbacks.add(accessPointCallback);
        if (this.mCallbacks.size() == 1) {
            this.mWifiTracker.startTracking();
        }
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.AccessPointController
    public boolean canConfigWifi() {
        return !this.mUserManager.hasUserRestriction("no_config_wifi", new UserHandle(this.mCurrentUser));
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.AccessPointController
    public boolean connect(AccessPoint accessPoint) {
        if (accessPoint == null) {
            return false;
        }
        if (accessPoint.isSaved()) {
            if (DEBUG) {
                Log.d("AccessPointController", "connect networkId=" + accessPoint.getConfig().networkId);
            }
            this.mWifiTracker.getManager().connect(accessPoint.getConfig().networkId, this.mConnectListener);
            return false;
        } else if (accessPoint.getSecurity() == 0 || BenesseExtension.getDchaState() != 0) {
            accessPoint.generateOpenNetworkConfig();
            this.mWifiTracker.getManager().connect(accessPoint.getConfig(), this.mConnectListener);
            return false;
        } else {
            Intent intent = new Intent("android.settings.WIFI_SETTINGS");
            intent.putExtra("wifi_start_connect_ssid", accessPoint.getSsidStr());
            intent.addFlags(268435456);
            fireSettingsIntentCallback(intent);
            return true;
        }
    }

    public void dump(PrintWriter printWriter) {
        this.mWifiTracker.dump(printWriter);
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.AccessPointController
    public int getIcon(AccessPoint accessPoint) {
        int level = accessPoint.getLevel();
        int[] iArr = ICONS;
        if (level < 0) {
            level = 0;
        }
        return iArr[level];
    }

    @Override // com.android.settingslib.wifi.WifiTracker.WifiListener
    public void onAccessPointsChanged() {
        fireAcccessPointsCallback(this.mWifiTracker.getAccessPoints());
    }

    @Override // com.android.settingslib.wifi.WifiTracker.WifiListener
    public void onConnectedChanged() {
        fireAcccessPointsCallback(this.mWifiTracker.getAccessPoints());
    }

    @Override // com.android.settingslib.wifi.WifiTracker.WifiListener
    public void onWifiStateChanged(int i) {
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.AccessPointController
    public void removeAccessPointCallback(NetworkController.AccessPointController.AccessPointCallback accessPointCallback) {
        if (accessPointCallback == null) {
            return;
        }
        if (DEBUG) {
            Log.d("AccessPointController", "removeCallback " + accessPointCallback);
        }
        this.mCallbacks.remove(accessPointCallback);
        if (this.mCallbacks.isEmpty()) {
            this.mWifiTracker.stopTracking();
        }
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.AccessPointController
    public void scanForAccessPoints() {
        if (DEBUG) {
            Log.d("AccessPointController", "scan!");
        }
        this.mWifiTracker.forceScan();
    }
}
