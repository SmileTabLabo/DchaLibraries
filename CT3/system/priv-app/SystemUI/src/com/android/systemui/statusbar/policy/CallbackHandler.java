package com.android.systemui.statusbar.policy;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.SubscriptionInfo;
import com.android.systemui.statusbar.policy.NetworkController;
import java.util.ArrayList;
import java.util.List;
/* loaded from: a.zip:com/android/systemui/statusbar/policy/CallbackHandler.class */
public class CallbackHandler extends Handler implements NetworkController.EmergencyListener, NetworkController.SignalCallback {
    private final ArrayList<NetworkController.EmergencyListener> mEmergencyListeners;
    private final ArrayList<NetworkController.SignalCallback> mSignalCallbacks;

    public CallbackHandler() {
        this.mEmergencyListeners = new ArrayList<>();
        this.mSignalCallbacks = new ArrayList<>();
    }

    CallbackHandler(Looper looper) {
        super(looper);
        this.mEmergencyListeners = new ArrayList<>();
        this.mSignalCallbacks = new ArrayList<>();
    }

    @Override // android.os.Handler
    public void handleMessage(Message message) {
        switch (message.what) {
            case 0:
                for (NetworkController.EmergencyListener emergencyListener : this.mEmergencyListeners) {
                    emergencyListener.setEmergencyCallsOnly(message.arg1 != 0);
                }
                return;
            case 1:
                for (NetworkController.SignalCallback signalCallback : this.mSignalCallbacks) {
                    signalCallback.setSubs((List) message.obj);
                }
                return;
            case 2:
                for (NetworkController.SignalCallback signalCallback2 : this.mSignalCallbacks) {
                    signalCallback2.setNoSims(message.arg1 != 0);
                }
                return;
            case 3:
                for (NetworkController.SignalCallback signalCallback3 : this.mSignalCallbacks) {
                    signalCallback3.setEthernetIndicators((NetworkController.IconState) message.obj);
                }
                return;
            case 4:
                for (NetworkController.SignalCallback signalCallback4 : this.mSignalCallbacks) {
                    signalCallback4.setIsAirplaneMode((NetworkController.IconState) message.obj);
                }
                return;
            case 5:
                for (NetworkController.SignalCallback signalCallback5 : this.mSignalCallbacks) {
                    signalCallback5.setMobileDataEnabled(message.arg1 != 0);
                }
                return;
            case 6:
                if (message.arg1 != 0) {
                    this.mEmergencyListeners.add((NetworkController.EmergencyListener) message.obj);
                    return;
                } else {
                    this.mEmergencyListeners.remove((NetworkController.EmergencyListener) message.obj);
                    return;
                }
            case 7:
                if (message.arg1 != 0) {
                    this.mSignalCallbacks.add((NetworkController.SignalCallback) message.obj);
                    return;
                } else {
                    this.mSignalCallbacks.remove((NetworkController.SignalCallback) message.obj);
                    return;
                }
            default:
                return;
        }
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.EmergencyListener
    public void setEmergencyCallsOnly(boolean z) {
        obtainMessage(0, z ? 1 : 0, 0).sendToTarget();
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setEthernetIndicators(NetworkController.IconState iconState) {
        obtainMessage(3, iconState).sendToTarget();
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setIsAirplaneMode(NetworkController.IconState iconState) {
        obtainMessage(4, iconState).sendToTarget();
    }

    public void setListening(NetworkController.EmergencyListener emergencyListener, boolean z) {
        obtainMessage(6, z ? 1 : 0, 0, emergencyListener).sendToTarget();
    }

    public void setListening(NetworkController.SignalCallback signalCallback, boolean z) {
        obtainMessage(7, z ? 1 : 0, 0, signalCallback).sendToTarget();
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setMobileDataEnabled(boolean z) {
        obtainMessage(5, z ? 1 : 0, 0).sendToTarget();
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setMobileDataIndicators(NetworkController.IconState iconState, NetworkController.IconState iconState2, int i, int i2, int i3, int i4, boolean z, boolean z2, String str, String str2, boolean z3, int i5) {
        post(new Runnable(this, iconState, iconState2, i, i2, i3, i4, z, z2, str, str2, z3, i5) { // from class: com.android.systemui.statusbar.policy.CallbackHandler.2
            final CallbackHandler this$0;
            final boolean val$activityIn;
            final boolean val$activityOut;
            final String val$description;
            final boolean val$isWide;
            final int val$networkIcon;
            final NetworkController.IconState val$qsIcon;
            final int val$qsType;
            final NetworkController.IconState val$statusIcon;
            final int val$statusType;
            final int val$subId;
            final String val$typeContentDescription;
            final int val$volteType;

            {
                this.this$0 = this;
                this.val$statusIcon = iconState;
                this.val$qsIcon = iconState2;
                this.val$statusType = i;
                this.val$networkIcon = i2;
                this.val$volteType = i3;
                this.val$qsType = i4;
                this.val$activityIn = z;
                this.val$activityOut = z2;
                this.val$typeContentDescription = str;
                this.val$description = str2;
                this.val$isWide = z3;
                this.val$subId = i5;
            }

            @Override // java.lang.Runnable
            public void run() {
                for (NetworkController.SignalCallback signalCallback : this.this$0.mSignalCallbacks) {
                    signalCallback.setMobileDataIndicators(this.val$statusIcon, this.val$qsIcon, this.val$statusType, this.val$networkIcon, this.val$volteType, this.val$qsType, this.val$activityIn, this.val$activityOut, this.val$typeContentDescription, this.val$description, this.val$isWide, this.val$subId);
                }
            }
        });
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setNoSims(boolean z) {
        obtainMessage(2, z ? 1 : 0, 0).sendToTarget();
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setSubs(List<SubscriptionInfo> list) {
        obtainMessage(1, list).sendToTarget();
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setWifiIndicators(boolean z, NetworkController.IconState iconState, NetworkController.IconState iconState2, boolean z2, boolean z3, String str) {
        post(new Runnable(this, z, iconState, iconState2, z2, z3, str) { // from class: com.android.systemui.statusbar.policy.CallbackHandler.1
            final CallbackHandler this$0;
            final boolean val$activityIn;
            final boolean val$activityOut;
            final String val$description;
            final boolean val$enabled;
            final NetworkController.IconState val$qsIcon;
            final NetworkController.IconState val$statusIcon;

            {
                this.this$0 = this;
                this.val$enabled = z;
                this.val$statusIcon = iconState;
                this.val$qsIcon = iconState2;
                this.val$activityIn = z2;
                this.val$activityOut = z3;
                this.val$description = str;
            }

            @Override // java.lang.Runnable
            public void run() {
                for (NetworkController.SignalCallback signalCallback : this.this$0.mSignalCallbacks) {
                    signalCallback.setWifiIndicators(this.val$enabled, this.val$statusIcon, this.val$qsIcon, this.val$activityIn, this.val$activityOut, this.val$description);
                }
            }
        });
    }
}
