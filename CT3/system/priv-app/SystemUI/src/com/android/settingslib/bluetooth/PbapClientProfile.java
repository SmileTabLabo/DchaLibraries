package com.android.settingslib.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothPbapClient;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothUuid;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: a.zip:com/android/settingslib/bluetooth/PbapClientProfile.class */
public final class PbapClientProfile implements LocalBluetoothProfile {
    private final CachedBluetoothDeviceManager mDeviceManager;
    private boolean mIsProfileReady;
    private final LocalBluetoothAdapter mLocalAdapter;
    private final LocalBluetoothProfileManager mProfileManager;
    private BluetoothPbapClient mService;
    private static boolean V = false;
    static final ParcelUuid[] SRC_UUIDS = {BluetoothUuid.PBAP_PSE};

    /* loaded from: a.zip:com/android/settingslib/bluetooth/PbapClientProfile$PbapClientServiceListener.class */
    private final class PbapClientServiceListener implements BluetoothProfile.ServiceListener {
        final PbapClientProfile this$0;

        private PbapClientServiceListener(PbapClientProfile pbapClientProfile) {
            this.this$0 = pbapClientProfile;
        }

        /* synthetic */ PbapClientServiceListener(PbapClientProfile pbapClientProfile, PbapClientServiceListener pbapClientServiceListener) {
            this(pbapClientProfile);
        }

        @Override // android.bluetooth.BluetoothProfile.ServiceListener
        public void onServiceConnected(int i, BluetoothProfile bluetoothProfile) {
            if (PbapClientProfile.V) {
                Log.d("PbapClientProfile", "Bluetooth service connected");
            }
            this.this$0.mService = (BluetoothPbapClient) bluetoothProfile;
            List connectedDevices = this.this$0.mService.getConnectedDevices();
            while (!connectedDevices.isEmpty()) {
                BluetoothDevice bluetoothDevice = (BluetoothDevice) connectedDevices.remove(0);
                CachedBluetoothDevice findDevice = this.this$0.mDeviceManager.findDevice(bluetoothDevice);
                CachedBluetoothDevice cachedBluetoothDevice = findDevice;
                if (findDevice == null) {
                    Log.w("PbapClientProfile", "PbapClientProfile found new device: " + bluetoothDevice);
                    cachedBluetoothDevice = this.this$0.mDeviceManager.addDevice(this.this$0.mLocalAdapter, this.this$0.mProfileManager, bluetoothDevice);
                }
                cachedBluetoothDevice.onProfileStateChanged(this.this$0, 2);
                cachedBluetoothDevice.refresh();
            }
            this.this$0.mIsProfileReady = true;
        }

        @Override // android.bluetooth.BluetoothProfile.ServiceListener
        public void onServiceDisconnected(int i) {
            if (PbapClientProfile.V) {
                Log.d("PbapClientProfile", "Bluetooth service disconnected");
            }
            this.this$0.mIsProfileReady = false;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public PbapClientProfile(Context context, LocalBluetoothAdapter localBluetoothAdapter, CachedBluetoothDeviceManager cachedBluetoothDeviceManager, LocalBluetoothProfileManager localBluetoothProfileManager) {
        this.mLocalAdapter = localBluetoothAdapter;
        this.mDeviceManager = cachedBluetoothDeviceManager;
        this.mProfileManager = localBluetoothProfileManager;
        this.mLocalAdapter.getProfileProxy(context, new PbapClientServiceListener(this, null), 17);
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public boolean connect(BluetoothDevice bluetoothDevice) {
        if (V) {
            Log.d("PbapClientProfile", "PBAPClientProfile got connect request");
        }
        if (this.mService == null) {
            return false;
        }
        List<BluetoothDevice> connectedDevices = getConnectedDevices();
        if (connectedDevices != null) {
            for (BluetoothDevice bluetoothDevice2 : connectedDevices) {
                if (bluetoothDevice2.equals(bluetoothDevice)) {
                    Log.d("PbapClientProfile", "Ignoring Connect");
                    return true;
                }
            }
            for (BluetoothDevice bluetoothDevice3 : connectedDevices) {
                this.mService.disconnect(bluetoothDevice);
            }
        }
        Log.d("PbapClientProfile", "PBAPClientProfile attempting to connect to " + bluetoothDevice.getAddress());
        return this.mService.connect(bluetoothDevice);
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public boolean disconnect(BluetoothDevice bluetoothDevice) {
        if (V) {
            Log.d("PbapClientProfile", "PBAPClientProfile got disconnect request");
        }
        if (this.mService == null) {
            return false;
        }
        return this.mService.disconnect(bluetoothDevice);
    }

    protected void finalize() {
        if (V) {
            Log.d("PbapClientProfile", "finalize()");
        }
        if (this.mService != null) {
            try {
                BluetoothAdapter.getDefaultAdapter().closeProfileProxy(17, this.mService);
                this.mService = null;
            } catch (Throwable th) {
                Log.w("PbapClientProfile", "Error cleaning up PBAP Client proxy", th);
            }
        }
    }

    public List<BluetoothDevice> getConnectedDevices() {
        return this.mService == null ? new ArrayList(0) : this.mService.getDevicesMatchingConnectionStates(new int[]{2, 1, 3});
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public int getConnectionStatus(BluetoothDevice bluetoothDevice) {
        if (this.mService == null) {
            return 0;
        }
        return this.mService.getConnectionState(bluetoothDevice);
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public boolean isAutoConnectable() {
        return true;
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public boolean isConnectable() {
        return true;
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public boolean isPreferred(BluetoothDevice bluetoothDevice) {
        boolean z = false;
        if (this.mService == null) {
            return false;
        }
        if (this.mService.getPriority(bluetoothDevice) > 0) {
            z = true;
        }
        return z;
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public void setPreferred(BluetoothDevice bluetoothDevice, boolean z) {
        if (this.mService == null) {
            return;
        }
        if (!z) {
            this.mService.setPriority(bluetoothDevice, 0);
        } else if (this.mService.getPriority(bluetoothDevice) < 100) {
            this.mService.setPriority(bluetoothDevice, 100);
        }
    }

    public String toString() {
        return "PbapClient";
    }
}
