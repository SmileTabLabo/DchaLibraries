package com.android.settingslib.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothUuid;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;
import java.util.Iterator;
import java.util.List;
/* loaded from: a.zip:com/android/settingslib/bluetooth/HeadsetProfile.class */
public final class HeadsetProfile implements LocalBluetoothProfile {
    private final CachedBluetoothDeviceManager mDeviceManager;
    private boolean mIsProfileReady;
    private final LocalBluetoothAdapter mLocalAdapter;
    private final LocalBluetoothProfileManager mProfileManager;
    private BluetoothHeadset mService;
    private static boolean V = true;
    static final ParcelUuid[] UUIDS = {BluetoothUuid.HSP, BluetoothUuid.Handsfree};

    /* loaded from: a.zip:com/android/settingslib/bluetooth/HeadsetProfile$HeadsetServiceListener.class */
    private final class HeadsetServiceListener implements BluetoothProfile.ServiceListener {
        final HeadsetProfile this$0;

        private HeadsetServiceListener(HeadsetProfile headsetProfile) {
            this.this$0 = headsetProfile;
        }

        /* synthetic */ HeadsetServiceListener(HeadsetProfile headsetProfile, HeadsetServiceListener headsetServiceListener) {
            this(headsetProfile);
        }

        @Override // android.bluetooth.BluetoothProfile.ServiceListener
        public void onServiceConnected(int i, BluetoothProfile bluetoothProfile) {
            if (HeadsetProfile.V) {
                Log.d("HeadsetProfile", "Bluetooth service connected");
            }
            this.this$0.mService = (BluetoothHeadset) bluetoothProfile;
            List<BluetoothDevice> connectedDevices = this.this$0.mService.getConnectedDevices();
            while (!connectedDevices.isEmpty()) {
                BluetoothDevice remove = connectedDevices.remove(0);
                CachedBluetoothDevice findDevice = this.this$0.mDeviceManager.findDevice(remove);
                CachedBluetoothDevice cachedBluetoothDevice = findDevice;
                if (findDevice == null) {
                    Log.w("HeadsetProfile", "HeadsetProfile found new device: " + remove);
                    cachedBluetoothDevice = this.this$0.mDeviceManager.addDevice(this.this$0.mLocalAdapter, this.this$0.mProfileManager, remove);
                }
                cachedBluetoothDevice.onProfileStateChanged(this.this$0, 2);
                cachedBluetoothDevice.refresh();
            }
            this.this$0.mProfileManager.callServiceConnectedListeners();
            this.this$0.mIsProfileReady = true;
        }

        @Override // android.bluetooth.BluetoothProfile.ServiceListener
        public void onServiceDisconnected(int i) {
            if (HeadsetProfile.V) {
                Log.d("HeadsetProfile", "Bluetooth service disconnected");
            }
            this.this$0.mProfileManager.callServiceDisconnectedListeners();
            this.this$0.mIsProfileReady = false;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public HeadsetProfile(Context context, LocalBluetoothAdapter localBluetoothAdapter, CachedBluetoothDeviceManager cachedBluetoothDeviceManager, LocalBluetoothProfileManager localBluetoothProfileManager) {
        this.mLocalAdapter = localBluetoothAdapter;
        this.mDeviceManager = cachedBluetoothDeviceManager;
        this.mProfileManager = localBluetoothProfileManager;
        this.mLocalAdapter.getProfileProxy(context, new HeadsetServiceListener(this, null), 1);
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public boolean connect(BluetoothDevice bluetoothDevice) {
        if (this.mService == null) {
            return false;
        }
        List<BluetoothDevice> connectedDevices = this.mService.getConnectedDevices();
        if (connectedDevices != null) {
            Iterator<T> it = connectedDevices.iterator();
            while (it.hasNext()) {
                Log.d("HeadsetProfile", "Not disconnecting device = " + ((BluetoothDevice) it.next()));
            }
        }
        return this.mService.connect(bluetoothDevice);
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public boolean disconnect(BluetoothDevice bluetoothDevice) {
        if (this.mService == null) {
            return false;
        }
        List<BluetoothDevice> connectedDevices = this.mService.getConnectedDevices();
        if (connectedDevices.isEmpty()) {
            return false;
        }
        for (BluetoothDevice bluetoothDevice2 : connectedDevices) {
            if (bluetoothDevice2.equals(bluetoothDevice)) {
                if (V) {
                    Log.d("HeadsetProfile", "Downgrade priority as useris disconnecting the headset");
                }
                if (this.mService.getPriority(bluetoothDevice) > 100) {
                    this.mService.setPriority(bluetoothDevice, 100);
                }
                return this.mService.disconnect(bluetoothDevice);
            }
        }
        return false;
    }

    protected void finalize() {
        if (V) {
            Log.d("HeadsetProfile", "finalize()");
        }
        if (this.mService != null) {
            try {
                BluetoothAdapter.getDefaultAdapter().closeProfileProxy(1, this.mService);
                this.mService = null;
            } catch (Throwable th) {
                Log.w("HeadsetProfile", "Error cleaning up HID proxy", th);
            }
        }
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public int getConnectionStatus(BluetoothDevice bluetoothDevice) {
        if (this.mService == null) {
            return 0;
        }
        List<BluetoothDevice> connectedDevices = this.mService.getConnectedDevices();
        if (connectedDevices.isEmpty()) {
            return 0;
        }
        for (BluetoothDevice bluetoothDevice2 : connectedDevices) {
            if (bluetoothDevice2.equals(bluetoothDevice)) {
                return this.mService.getConnectionState(bluetoothDevice);
            }
        }
        return 0;
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
        return "HEADSET";
    }
}
