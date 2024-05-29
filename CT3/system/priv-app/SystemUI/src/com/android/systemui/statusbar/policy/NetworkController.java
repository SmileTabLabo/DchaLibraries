package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.content.Intent;
import android.telephony.SubscriptionInfo;
import com.android.settingslib.net.DataUsageController;
import com.android.settingslib.wifi.AccessPoint;
import java.util.List;
/* loaded from: a.zip:com/android/systemui/statusbar/policy/NetworkController.class */
public interface NetworkController {

    /* loaded from: a.zip:com/android/systemui/statusbar/policy/NetworkController$AccessPointController.class */
    public interface AccessPointController {

        /* loaded from: a.zip:com/android/systemui/statusbar/policy/NetworkController$AccessPointController$AccessPointCallback.class */
        public interface AccessPointCallback {
            void onAccessPointsChanged(List<AccessPoint> list);

            void onSettingsActivityTriggered(Intent intent);
        }

        void addAccessPointCallback(AccessPointCallback accessPointCallback);

        boolean canConfigWifi();

        boolean connect(AccessPoint accessPoint);

        int getIcon(AccessPoint accessPoint);

        void removeAccessPointCallback(AccessPointCallback accessPointCallback);

        void scanForAccessPoints();
    }

    /* loaded from: a.zip:com/android/systemui/statusbar/policy/NetworkController$EmergencyListener.class */
    public interface EmergencyListener {
        void setEmergencyCallsOnly(boolean z);
    }

    /* loaded from: a.zip:com/android/systemui/statusbar/policy/NetworkController$IconState.class */
    public static class IconState {
        public final String contentDescription;
        public final int icon;
        public final boolean visible;

        public IconState(boolean z, int i, int i2, Context context) {
            this(z, i, context.getString(i2));
        }

        public IconState(boolean z, int i, String str) {
            this.visible = z;
            this.icon = i;
            this.contentDescription = str;
        }
    }

    /* loaded from: a.zip:com/android/systemui/statusbar/policy/NetworkController$SignalCallback.class */
    public interface SignalCallback {
        void setEthernetIndicators(IconState iconState);

        void setIsAirplaneMode(IconState iconState);

        void setMobileDataEnabled(boolean z);

        void setMobileDataIndicators(IconState iconState, IconState iconState2, int i, int i2, int i3, int i4, boolean z, boolean z2, String str, String str2, boolean z3, int i5);

        void setNoSims(boolean z);

        void setSubs(List<SubscriptionInfo> list);

        void setWifiIndicators(boolean z, IconState iconState, IconState iconState2, boolean z2, boolean z3, String str);
    }

    void addEmergencyListener(EmergencyListener emergencyListener);

    void addSignalCallback(SignalCallback signalCallback);

    AccessPointController getAccessPointController();

    DataSaverController getDataSaverController();

    DataUsageController getMobileDataController();

    boolean hasMobileDataFeature();

    boolean hasVoiceCallingFeature();

    void removeEmergencyListener(EmergencyListener emergencyListener);

    void removeSignalCallback(SignalCallback signalCallback);

    void setWifiEnabled(boolean z);
}
