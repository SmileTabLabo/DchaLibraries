package com.android.systemui.statusbar.policy;

import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.net.ConnectivityManager;
import android.net.IConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.net.LegacyVpnInfo;
import com.android.internal.net.VpnConfig;
import com.android.systemui.statusbar.policy.SecurityController;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
/* loaded from: a.zip:com/android/systemui/statusbar/policy/SecurityControllerImpl.class */
public class SecurityControllerImpl implements SecurityController {
    private static final boolean DEBUG = Log.isLoggable("SecurityController", 3);
    private static final NetworkRequest REQUEST = new NetworkRequest.Builder().removeCapability(15).removeCapability(13).removeCapability(14).build();
    private final ConnectivityManager mConnectivityManager;
    private final Context mContext;
    private int mCurrentUserId;
    private final DevicePolicyManager mDevicePolicyManager;
    private final UserManager mUserManager;
    private int mVpnUserId;
    @GuardedBy("mCallbacks")
    private final ArrayList<SecurityController.SecurityControllerCallback> mCallbacks = new ArrayList<>();
    private SparseArray<VpnConfig> mCurrentVpns = new SparseArray<>();
    private final ConnectivityManager.NetworkCallback mNetworkCallback = new ConnectivityManager.NetworkCallback(this) { // from class: com.android.systemui.statusbar.policy.SecurityControllerImpl.1
        final SecurityControllerImpl this$0;

        {
            this.this$0 = this;
        }

        @Override // android.net.ConnectivityManager.NetworkCallback
        public void onAvailable(Network network) {
            if (SecurityControllerImpl.DEBUG) {
                Log.d("SecurityController", "onAvailable " + network.netId);
            }
            this.this$0.updateState();
            this.this$0.fireCallbacks();
        }

        @Override // android.net.ConnectivityManager.NetworkCallback
        public void onLost(Network network) {
            if (SecurityControllerImpl.DEBUG) {
                Log.d("SecurityController", "onLost " + network.netId);
            }
            this.this$0.updateState();
            this.this$0.fireCallbacks();
        }
    };
    private final IConnectivityManager mConnectivityManagerService = IConnectivityManager.Stub.asInterface(ServiceManager.getService("connectivity"));

    public SecurityControllerImpl(Context context) {
        this.mContext = context;
        this.mDevicePolicyManager = (DevicePolicyManager) context.getSystemService("device_policy");
        this.mConnectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        this.mUserManager = (UserManager) context.getSystemService("user");
        this.mConnectivityManager.registerNetworkCallback(REQUEST, this.mNetworkCallback);
        onUserSwitched(ActivityManager.getCurrentUser());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void fireCallbacks() {
        synchronized (this.mCallbacks) {
            for (SecurityController.SecurityControllerCallback securityControllerCallback : this.mCallbacks) {
                securityControllerCallback.onStateChanged();
            }
        }
    }

    private String getNameForVpnConfig(VpnConfig vpnConfig, UserHandle userHandle) {
        if (vpnConfig.legacy) {
            return this.mContext.getString(2131493669);
        }
        String str = vpnConfig.user;
        try {
            return VpnConfig.getVpnLabel(this.mContext.createPackageContextAsUser(this.mContext.getPackageName(), 0, userHandle), str).toString();
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("SecurityController", "Package " + str + " is not present", e);
            return null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateState() {
        LegacyVpnInfo legacyVpnInfo;
        SparseArray<VpnConfig> sparseArray = new SparseArray<>();
        try {
            for (UserInfo userInfo : this.mUserManager.getUsers()) {
                VpnConfig vpnConfig = this.mConnectivityManagerService.getVpnConfig(userInfo.id);
                if (vpnConfig != null && (!vpnConfig.legacy || ((legacyVpnInfo = this.mConnectivityManagerService.getLegacyVpnInfo(userInfo.id)) != null && legacyVpnInfo.state == 3))) {
                    sparseArray.put(userInfo.id, vpnConfig);
                }
            }
            this.mCurrentVpns = sparseArray;
        } catch (RemoteException e) {
            Log.e("SecurityController", "Unable to list active VPNs", e);
        }
    }

    @Override // com.android.systemui.statusbar.policy.SecurityController
    public void addCallback(SecurityController.SecurityControllerCallback securityControllerCallback) {
        synchronized (this.mCallbacks) {
            if (securityControllerCallback != null) {
                if (!this.mCallbacks.contains(securityControllerCallback)) {
                    if (DEBUG) {
                        Log.d("SecurityController", "addCallback " + securityControllerCallback);
                    }
                    this.mCallbacks.add(securityControllerCallback);
                }
            }
        }
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("SecurityController state:");
        printWriter.print("  mCurrentVpns={");
        for (int i = 0; i < this.mCurrentVpns.size(); i++) {
            if (i > 0) {
                printWriter.print(", ");
            }
            printWriter.print(this.mCurrentVpns.keyAt(i));
            printWriter.print('=');
            printWriter.print(this.mCurrentVpns.valueAt(i).user);
        }
        printWriter.println("}");
    }

    @Override // com.android.systemui.statusbar.policy.SecurityController
    public String getDeviceOwnerName() {
        return this.mDevicePolicyManager.getDeviceOwnerNameOnAnyUser();
    }

    @Override // com.android.systemui.statusbar.policy.SecurityController
    public String getPrimaryVpnName() {
        VpnConfig vpnConfig = this.mCurrentVpns.get(this.mVpnUserId);
        if (vpnConfig != null) {
            return getNameForVpnConfig(vpnConfig, new UserHandle(this.mVpnUserId));
        }
        return null;
    }

    @Override // com.android.systemui.statusbar.policy.SecurityController
    public String getProfileOwnerName() {
        for (int i : this.mUserManager.getProfileIdsWithDisabled(this.mCurrentUserId)) {
            String profileOwnerNameAsUser = this.mDevicePolicyManager.getProfileOwnerNameAsUser(i);
            if (profileOwnerNameAsUser != null) {
                return profileOwnerNameAsUser;
            }
        }
        return null;
    }

    @Override // com.android.systemui.statusbar.policy.SecurityController
    public String getProfileVpnName() {
        int[] profileIdsWithDisabled;
        VpnConfig vpnConfig;
        for (int i : this.mUserManager.getProfileIdsWithDisabled(this.mVpnUserId)) {
            if (i != this.mVpnUserId && (vpnConfig = this.mCurrentVpns.get(i)) != null) {
                return getNameForVpnConfig(vpnConfig, UserHandle.of(i));
            }
        }
        return null;
    }

    @Override // com.android.systemui.statusbar.policy.SecurityController
    public boolean hasProfileOwner() {
        return this.mDevicePolicyManager.getProfileOwnerAsUser(this.mCurrentUserId) != null;
    }

    @Override // com.android.systemui.statusbar.policy.SecurityController
    public boolean isDeviceManaged() {
        return this.mDevicePolicyManager.isDeviceManaged();
    }

    @Override // com.android.systemui.statusbar.policy.SecurityController
    public boolean isVpnEnabled() {
        for (int i : this.mUserManager.getProfileIdsWithDisabled(this.mVpnUserId)) {
            if (this.mCurrentVpns.get(i) != null) {
                return true;
            }
        }
        return false;
    }

    @Override // com.android.systemui.statusbar.policy.SecurityController
    public boolean isVpnRestricted() {
        return !this.mUserManager.getUserInfo(this.mCurrentUserId).isRestricted() ? this.mUserManager.hasUserRestriction("no_config_vpn", new UserHandle(this.mCurrentUserId)) : true;
    }

    public void onUserSwitched(int i) {
        this.mCurrentUserId = i;
        UserInfo userInfo = this.mUserManager.getUserInfo(i);
        if (userInfo.isRestricted()) {
            this.mVpnUserId = userInfo.restrictedProfileParentId;
        } else {
            this.mVpnUserId = this.mCurrentUserId;
        }
        fireCallbacks();
    }

    @Override // com.android.systemui.statusbar.policy.SecurityController
    public void removeCallback(SecurityController.SecurityControllerCallback securityControllerCallback) {
        synchronized (this.mCallbacks) {
            if (securityControllerCallback == null) {
                return;
            }
            if (DEBUG) {
                Log.d("SecurityController", "removeCallback " + securityControllerCallback);
            }
            this.mCallbacks.remove(securityControllerCallback);
        }
    }
}
