package com.android.systemui.statusbar.policy;
/* loaded from: a.zip:com/android/systemui/statusbar/policy/SecurityController.class */
public interface SecurityController {

    /* loaded from: a.zip:com/android/systemui/statusbar/policy/SecurityController$SecurityControllerCallback.class */
    public interface SecurityControllerCallback {
        void onStateChanged();
    }

    void addCallback(SecurityControllerCallback securityControllerCallback);

    String getDeviceOwnerName();

    String getPrimaryVpnName();

    String getProfileOwnerName();

    String getProfileVpnName();

    boolean hasProfileOwner();

    boolean isDeviceManaged();

    boolean isVpnEnabled();

    boolean isVpnRestricted();

    void removeCallback(SecurityControllerCallback securityControllerCallback);
}
