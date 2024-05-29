package com.android.settingslib.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothInputDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;
import java.util.List;
/* loaded from: a.zip:com/android/settingslib/bluetooth/HidProfile.class */
public final class HidProfile implements LocalBluetoothProfile {
    private static boolean V = true;
    private final CachedBluetoothDeviceManager mDeviceManager;
    private boolean mIsProfileReady;
    private final LocalBluetoothAdapter mLocalAdapter;
    private final LocalBluetoothProfileManager mProfileManager;
    private BluetoothInputDevice mService;

    /* loaded from: a.zip:com/android/settingslib/bluetooth/HidProfile$InputDeviceServiceListener.class */
    private final class InputDeviceServiceListener implements BluetoothProfile.ServiceListener {
        final HidProfile this$0;

        private InputDeviceServiceListener(HidProfile hidProfile) {
            this.this$0 = hidProfile;
        }

        /* synthetic */ InputDeviceServiceListener(HidProfile hidProfile, InputDeviceServiceListener inputDeviceServiceListener) {
            this(hidProfile);
        }

        @Override // android.bluetooth.BluetoothProfile.ServiceListener
        public void onServiceConnected(int i, BluetoothProfile bluetoothProfile) {
            if (HidProfile.V) {
                Log.d("HidProfile", "Bluetooth service connected");
            }
            this.this$0.mService = (BluetoothInputDevice) bluetoothProfile;
            List connectedDevices = this.this$0.mService.getConnectedDevices();
            while (!connectedDevices.isEmpty()) {
                BluetoothDevice bluetoothDevice = (BluetoothDevice) connectedDevices.remove(0);
                CachedBluetoothDevice findDevice = this.this$0.mDeviceManager.findDevice(bluetoothDevice);
                CachedBluetoothDevice cachedBluetoothDevice = findDevice;
                if (findDevice == null) {
                    Log.w("HidProfile", "HidProfile found new device: " + bluetoothDevice);
                    cachedBluetoothDevice = this.this$0.mDeviceManager.addDevice(this.this$0.mLocalAdapter, this.this$0.mProfileManager, bluetoothDevice);
                }
                cachedBluetoothDevice.onProfileStateChanged(this.this$0, 2);
                cachedBluetoothDevice.refresh();
            }
            this.this$0.mIsProfileReady = true;
        }

        @Override // android.bluetooth.BluetoothProfile.ServiceListener
        public void onServiceDisconnected(int i) {
            if (HidProfile.V) {
                Log.d("HidProfile", "Bluetooth service disconnected");
            }
            this.this$0.mIsProfileReady = false;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public HidProfile(Context context, LocalBluetoothAdapter localBluetoothAdapter, CachedBluetoothDeviceManager cachedBluetoothDeviceManager, LocalBluetoothProfileManager localBluetoothProfileManager) {
        this.mLocalAdapter = localBluetoothAdapter;
        this.mDeviceManager = cachedBluetoothDeviceManager;
        this.mProfileManager = localBluetoothProfileManager;
        localBluetoothAdapter.getProfileProxy(context, new InputDeviceServiceListener(this, null), 4);
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public boolean connect(BluetoothDevice bluetoothDevice) {
        if (this.mService == null) {
            return false;
        }
        return this.mService.connect(bluetoothDevice);
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public boolean disconnect(BluetoothDevice bluetoothDevice) {
        if (this.mService == null) {
            return false;
        }
        return this.mService.disconnect(bluetoothDevice);
    }

    protected void finalize() {
        if (V) {
            Log.d("HidProfile", "finalize()");
        }
        if (this.mService != null) {
            try {
                BluetoothAdapter.getDefaultAdapter().closeProfileProxy(4, this.mService);
                this.mService = null;
            } catch (Throwable th) {
                Log.w("HidProfile", "Error cleaning up HID proxy", th);
            }
        }
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public int getConnectionStatus(BluetoothDevice bluetoothDevice) {
        if (this.mService == null) {
            return 0;
        }
        List connectedDevices = this.mService.getConnectedDevices();
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
        return "HID";
    }
}
