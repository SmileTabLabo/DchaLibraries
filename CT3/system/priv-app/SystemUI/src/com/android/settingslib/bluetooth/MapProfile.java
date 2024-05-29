package com.android.settingslib.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothMap;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothUuid;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;
import java.util.List;
/* loaded from: a.zip:com/android/settingslib/bluetooth/MapProfile.class */
public final class MapProfile implements LocalBluetoothProfile {
    private final CachedBluetoothDeviceManager mDeviceManager;
    private boolean mIsProfileReady;
    private final LocalBluetoothAdapter mLocalAdapter;
    private final LocalBluetoothProfileManager mProfileManager;
    private BluetoothMap mService;
    private static boolean V = true;
    static final ParcelUuid[] UUIDS = {BluetoothUuid.MAP, BluetoothUuid.MNS, BluetoothUuid.MAS};

    /* loaded from: a.zip:com/android/settingslib/bluetooth/MapProfile$MapServiceListener.class */
    private final class MapServiceListener implements BluetoothProfile.ServiceListener {
        final MapProfile this$0;

        private MapServiceListener(MapProfile mapProfile) {
            this.this$0 = mapProfile;
        }

        /* synthetic */ MapServiceListener(MapProfile mapProfile, MapServiceListener mapServiceListener) {
            this(mapProfile);
        }

        @Override // android.bluetooth.BluetoothProfile.ServiceListener
        public void onServiceConnected(int i, BluetoothProfile bluetoothProfile) {
            if (MapProfile.V) {
                Log.d("MapProfile", "Bluetooth service connected");
            }
            this.this$0.mService = (BluetoothMap) bluetoothProfile;
            List connectedDevices = this.this$0.mService.getConnectedDevices();
            while (!connectedDevices.isEmpty()) {
                BluetoothDevice bluetoothDevice = (BluetoothDevice) connectedDevices.remove(0);
                CachedBluetoothDevice findDevice = this.this$0.mDeviceManager.findDevice(bluetoothDevice);
                CachedBluetoothDevice cachedBluetoothDevice = findDevice;
                if (findDevice == null) {
                    Log.w("MapProfile", "MapProfile found new device: " + bluetoothDevice);
                    cachedBluetoothDevice = this.this$0.mDeviceManager.addDevice(this.this$0.mLocalAdapter, this.this$0.mProfileManager, bluetoothDevice);
                }
                cachedBluetoothDevice.onProfileStateChanged(this.this$0, 2);
                cachedBluetoothDevice.refresh();
            }
            this.this$0.mProfileManager.callServiceConnectedListeners();
            this.this$0.mIsProfileReady = true;
        }

        @Override // android.bluetooth.BluetoothProfile.ServiceListener
        public void onServiceDisconnected(int i) {
            if (MapProfile.V) {
                Log.d("MapProfile", "Bluetooth service disconnected");
            }
            this.this$0.mProfileManager.callServiceDisconnectedListeners();
            this.this$0.mIsProfileReady = false;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public MapProfile(Context context, LocalBluetoothAdapter localBluetoothAdapter, CachedBluetoothDeviceManager cachedBluetoothDeviceManager, LocalBluetoothProfileManager localBluetoothProfileManager) {
        this.mLocalAdapter = localBluetoothAdapter;
        this.mDeviceManager = cachedBluetoothDeviceManager;
        this.mProfileManager = localBluetoothProfileManager;
        this.mLocalAdapter.getProfileProxy(context, new MapServiceListener(this, null), 9);
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public boolean connect(BluetoothDevice bluetoothDevice) {
        if (V) {
            Log.d("MapProfile", "connect() - should not get called");
            return false;
        }
        return false;
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public boolean disconnect(BluetoothDevice bluetoothDevice) {
        if (this.mService == null) {
            return false;
        }
        List connectedDevices = this.mService.getConnectedDevices();
        if (connectedDevices.isEmpty() || !((BluetoothDevice) connectedDevices.get(0)).equals(bluetoothDevice)) {
            return false;
        }
        if (this.mService.getPriority(bluetoothDevice) > 100) {
            this.mService.setPriority(bluetoothDevice, 100);
        }
        return this.mService.disconnect(bluetoothDevice);
    }

    protected void finalize() {
        if (V) {
            Log.d("MapProfile", "finalize()");
        }
        if (this.mService != null) {
            try {
                BluetoothAdapter.getDefaultAdapter().closeProfileProxy(9, this.mService);
                this.mService = null;
            } catch (Throwable th) {
                Log.w("MapProfile", "Error cleaning up MAP proxy", th);
            }
        }
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public int getConnectionStatus(BluetoothDevice bluetoothDevice) {
        if (this.mService == null) {
            return 0;
        }
        List connectedDevices = this.mService.getConnectedDevices();
        if (V) {
            Log.d("MapProfile", "getConnectionStatus: status is: " + this.mService.getConnectionState(bluetoothDevice));
        }
        return (connectedDevices.isEmpty() || !((BluetoothDevice) connectedDevices.get(0)).equals(bluetoothDevice)) ? 0 : this.mService.getConnectionState(bluetoothDevice);
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
        return "MAP";
    }
}
