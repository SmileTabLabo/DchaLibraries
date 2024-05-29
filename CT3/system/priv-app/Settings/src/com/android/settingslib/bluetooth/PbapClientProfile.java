package com.android.settingslib.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothPbapClient;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothUuid;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;
import com.android.settingslib.R$drawable;
import com.android.settingslib.R$string;
import java.util.ArrayList;
import java.util.List;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public final class PbapClientProfile implements LocalBluetoothProfile {
    private final CachedBluetoothDeviceManager mDeviceManager;
    private boolean mIsProfileReady;
    private final LocalBluetoothAdapter mLocalAdapter;
    private final LocalBluetoothProfileManager mProfileManager;
    private BluetoothPbapClient mService;
    private static boolean V = false;
    static final ParcelUuid[] SRC_UUIDS = {BluetoothUuid.PBAP_PSE};

    /* loaded from: classes.dex */
    private final class PbapClientServiceListener implements BluetoothProfile.ServiceListener {
        /* synthetic */ PbapClientServiceListener(PbapClientProfile this$0, PbapClientServiceListener pbapClientServiceListener) {
            this();
        }

        private PbapClientServiceListener() {
        }

        @Override // android.bluetooth.BluetoothProfile.ServiceListener
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (PbapClientProfile.V) {
                Log.d("PbapClientProfile", "Bluetooth service connected");
            }
            PbapClientProfile.this.mService = (BluetoothPbapClient) proxy;
            List<BluetoothDevice> deviceList = PbapClientProfile.this.mService.getConnectedDevices();
            while (!deviceList.isEmpty()) {
                BluetoothDevice nextDevice = deviceList.remove(0);
                CachedBluetoothDevice device = PbapClientProfile.this.mDeviceManager.findDevice(nextDevice);
                if (device == null) {
                    Log.w("PbapClientProfile", "PbapClientProfile found new device: " + nextDevice);
                    device = PbapClientProfile.this.mDeviceManager.addDevice(PbapClientProfile.this.mLocalAdapter, PbapClientProfile.this.mProfileManager, nextDevice);
                }
                device.onProfileStateChanged(PbapClientProfile.this, 2);
                device.refresh();
            }
            PbapClientProfile.this.mIsProfileReady = true;
        }

        @Override // android.bluetooth.BluetoothProfile.ServiceListener
        public void onServiceDisconnected(int profile) {
            if (PbapClientProfile.V) {
                Log.d("PbapClientProfile", "Bluetooth service disconnected");
            }
            PbapClientProfile.this.mIsProfileReady = false;
        }
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public boolean isProfileReady() {
        return this.mIsProfileReady;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public PbapClientProfile(Context context, LocalBluetoothAdapter adapter, CachedBluetoothDeviceManager deviceManager, LocalBluetoothProfileManager profileManager) {
        this.mLocalAdapter = adapter;
        this.mDeviceManager = deviceManager;
        this.mProfileManager = profileManager;
        this.mLocalAdapter.getProfileProxy(context, new PbapClientServiceListener(this, null), 17);
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public boolean isConnectable() {
        return true;
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public boolean isAutoConnectable() {
        return true;
    }

    public List<BluetoothDevice> getConnectedDevices() {
        if (this.mService == null) {
            return new ArrayList(0);
        }
        return this.mService.getDevicesMatchingConnectionStates(new int[]{2, 1, 3});
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public boolean connect(BluetoothDevice device) {
        if (V) {
            Log.d("PbapClientProfile", "PBAPClientProfile got connect request");
        }
        if (this.mService == null) {
            return false;
        }
        List<BluetoothDevice> srcs = getConnectedDevices();
        if (srcs != null) {
            for (BluetoothDevice src : srcs) {
                if (src.equals(device)) {
                    Log.d("PbapClientProfile", "Ignoring Connect");
                    return true;
                }
            }
            for (BluetoothDevice bluetoothDevice : srcs) {
                this.mService.disconnect(device);
            }
        }
        Log.d("PbapClientProfile", "PBAPClientProfile attempting to connect to " + device.getAddress());
        return this.mService.connect(device);
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public boolean disconnect(BluetoothDevice device) {
        if (V) {
            Log.d("PbapClientProfile", "PBAPClientProfile got disconnect request");
        }
        if (this.mService == null) {
            return false;
        }
        return this.mService.disconnect(device);
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public int getConnectionStatus(BluetoothDevice device) {
        if (this.mService == null) {
            return 0;
        }
        return this.mService.getConnectionState(device);
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public boolean isPreferred(BluetoothDevice device) {
        return this.mService != null && this.mService.getPriority(device) > 0;
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public int getPreferred(BluetoothDevice device) {
        if (this.mService == null) {
            return 0;
        }
        return this.mService.getPriority(device);
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public void setPreferred(BluetoothDevice device, boolean preferred) {
        if (this.mService == null) {
            return;
        }
        if (preferred) {
            if (this.mService.getPriority(device) >= 100) {
                return;
            }
            this.mService.setPriority(device, 100);
            return;
        }
        this.mService.setPriority(device, 0);
    }

    public String toString() {
        return "PbapClient";
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public int getNameResource(BluetoothDevice device) {
        return R$string.bluetooth_profile_pbap;
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public int getDrawableResource(BluetoothClass btClass) {
        return R$drawable.ic_bt_cellphone;
    }

    protected void finalize() {
        if (V) {
            Log.d("PbapClientProfile", "finalize()");
        }
        if (this.mService == null) {
            return;
        }
        try {
            BluetoothAdapter.getDefaultAdapter().closeProfileProxy(17, this.mService);
            this.mService = null;
        } catch (Throwable t) {
            Log.w("PbapClientProfile", "Error cleaning up PBAP Client proxy", t);
        }
    }
}
