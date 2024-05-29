package com.android.settingslib.bluetooth;

import android.bluetooth.BluetoothA2dpSink;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothUuid;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: a.zip:com/android/settingslib/bluetooth/A2dpSinkProfile.class */
public final class A2dpSinkProfile implements LocalBluetoothProfile {
    private final CachedBluetoothDeviceManager mDeviceManager;
    private boolean mIsProfileReady;
    private final LocalBluetoothAdapter mLocalAdapter;
    private final LocalBluetoothProfileManager mProfileManager;
    private BluetoothA2dpSink mService;
    private static boolean V = true;
    static final ParcelUuid[] SRC_UUIDS = {BluetoothUuid.AudioSource, BluetoothUuid.AdvAudioDist};

    /* loaded from: a.zip:com/android/settingslib/bluetooth/A2dpSinkProfile$A2dpSinkServiceListener.class */
    private final class A2dpSinkServiceListener implements BluetoothProfile.ServiceListener {
        final A2dpSinkProfile this$0;

        private A2dpSinkServiceListener(A2dpSinkProfile a2dpSinkProfile) {
            this.this$0 = a2dpSinkProfile;
        }

        /* synthetic */ A2dpSinkServiceListener(A2dpSinkProfile a2dpSinkProfile, A2dpSinkServiceListener a2dpSinkServiceListener) {
            this(a2dpSinkProfile);
        }

        @Override // android.bluetooth.BluetoothProfile.ServiceListener
        public void onServiceConnected(int i, BluetoothProfile bluetoothProfile) {
            if (A2dpSinkProfile.V) {
                Log.d("A2dpSinkProfile", "Bluetooth service connected");
            }
            this.this$0.mService = (BluetoothA2dpSink) bluetoothProfile;
            List connectedDevices = this.this$0.mService.getConnectedDevices();
            while (!connectedDevices.isEmpty()) {
                BluetoothDevice bluetoothDevice = (BluetoothDevice) connectedDevices.remove(0);
                CachedBluetoothDevice findDevice = this.this$0.mDeviceManager.findDevice(bluetoothDevice);
                CachedBluetoothDevice cachedBluetoothDevice = findDevice;
                if (findDevice == null) {
                    Log.w("A2dpSinkProfile", "A2dpSinkProfile found new device: " + bluetoothDevice);
                    cachedBluetoothDevice = this.this$0.mDeviceManager.addDevice(this.this$0.mLocalAdapter, this.this$0.mProfileManager, bluetoothDevice);
                }
                cachedBluetoothDevice.onProfileStateChanged(this.this$0, 2);
                cachedBluetoothDevice.refresh();
            }
            this.this$0.mIsProfileReady = true;
        }

        @Override // android.bluetooth.BluetoothProfile.ServiceListener
        public void onServiceDisconnected(int i) {
            if (A2dpSinkProfile.V) {
                Log.d("A2dpSinkProfile", "Bluetooth service disconnected");
            }
            this.this$0.mIsProfileReady = false;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public A2dpSinkProfile(Context context, LocalBluetoothAdapter localBluetoothAdapter, CachedBluetoothDeviceManager cachedBluetoothDeviceManager, LocalBluetoothProfileManager localBluetoothProfileManager) {
        this.mLocalAdapter = localBluetoothAdapter;
        this.mDeviceManager = cachedBluetoothDeviceManager;
        this.mProfileManager = localBluetoothProfileManager;
        this.mLocalAdapter.getProfileProxy(context, new A2dpSinkServiceListener(this, null), 11);
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public boolean connect(BluetoothDevice bluetoothDevice) {
        if (this.mService == null) {
            return false;
        }
        List<BluetoothDevice> connectedDevices = getConnectedDevices();
        if (connectedDevices != null) {
            for (BluetoothDevice bluetoothDevice2 : connectedDevices) {
                if (bluetoothDevice2.equals(bluetoothDevice)) {
                    Log.d("A2dpSinkProfile", "Ignoring Connect");
                    return true;
                }
            }
            for (BluetoothDevice bluetoothDevice3 : connectedDevices) {
                this.mService.disconnect(bluetoothDevice3);
            }
        }
        return this.mService.connect(bluetoothDevice);
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public boolean disconnect(BluetoothDevice bluetoothDevice) {
        if (this.mService == null) {
            return false;
        }
        if (this.mService.getPriority(bluetoothDevice) > 100) {
            this.mService.setPriority(bluetoothDevice, 100);
        }
        return this.mService.disconnect(bluetoothDevice);
    }

    protected void finalize() {
        if (V) {
            Log.d("A2dpSinkProfile", "finalize()");
        }
        if (this.mService != null) {
            try {
                BluetoothAdapter.getDefaultAdapter().closeProfileProxy(11, this.mService);
                this.mService = null;
            } catch (Throwable th) {
                Log.w("A2dpSinkProfile", "Error cleaning up A2DP proxy", th);
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
        return "A2DPSink";
    }
}
