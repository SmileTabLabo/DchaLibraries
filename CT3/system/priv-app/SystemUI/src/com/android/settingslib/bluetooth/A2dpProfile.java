package com.android.settingslib.bluetooth;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothUuid;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
/* loaded from: a.zip:com/android/settingslib/bluetooth/A2dpProfile.class */
public final class A2dpProfile implements LocalBluetoothProfile {
    private final CachedBluetoothDeviceManager mDeviceManager;
    private boolean mIsProfileReady;
    private final LocalBluetoothAdapter mLocalAdapter;
    private final LocalBluetoothProfileManager mProfileManager;
    private BluetoothA2dp mService;
    private static boolean V = true;
    static final ParcelUuid[] SINK_UUIDS = {BluetoothUuid.AudioSink, BluetoothUuid.AdvAudioDist};

    /* loaded from: a.zip:com/android/settingslib/bluetooth/A2dpProfile$A2dpServiceListener.class */
    private final class A2dpServiceListener implements BluetoothProfile.ServiceListener {
        final A2dpProfile this$0;

        private A2dpServiceListener(A2dpProfile a2dpProfile) {
            this.this$0 = a2dpProfile;
        }

        /* synthetic */ A2dpServiceListener(A2dpProfile a2dpProfile, A2dpServiceListener a2dpServiceListener) {
            this(a2dpProfile);
        }

        @Override // android.bluetooth.BluetoothProfile.ServiceListener
        public void onServiceConnected(int i, BluetoothProfile bluetoothProfile) {
            if (A2dpProfile.V) {
                Log.d("A2dpProfile", "Bluetooth service connected");
            }
            this.this$0.mService = (BluetoothA2dp) bluetoothProfile;
            List<BluetoothDevice> connectedDevices = this.this$0.mService.getConnectedDevices();
            while (!connectedDevices.isEmpty()) {
                BluetoothDevice remove = connectedDevices.remove(0);
                CachedBluetoothDevice findDevice = this.this$0.mDeviceManager.findDevice(remove);
                CachedBluetoothDevice cachedBluetoothDevice = findDevice;
                if (findDevice == null) {
                    Log.w("A2dpProfile", "A2dpProfile found new device: " + remove);
                    cachedBluetoothDevice = this.this$0.mDeviceManager.addDevice(this.this$0.mLocalAdapter, this.this$0.mProfileManager, remove);
                }
                cachedBluetoothDevice.onProfileStateChanged(this.this$0, 2);
                cachedBluetoothDevice.refresh();
            }
            this.this$0.mIsProfileReady = true;
        }

        @Override // android.bluetooth.BluetoothProfile.ServiceListener
        public void onServiceDisconnected(int i) {
            if (A2dpProfile.V) {
                Log.d("A2dpProfile", "Bluetooth service disconnected");
            }
            this.this$0.mIsProfileReady = false;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public A2dpProfile(Context context, LocalBluetoothAdapter localBluetoothAdapter, CachedBluetoothDeviceManager cachedBluetoothDeviceManager, LocalBluetoothProfileManager localBluetoothProfileManager) {
        this.mLocalAdapter = localBluetoothAdapter;
        this.mDeviceManager = cachedBluetoothDeviceManager;
        this.mProfileManager = localBluetoothProfileManager;
        this.mLocalAdapter.getProfileProxy(context, new A2dpServiceListener(this, null), 2);
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public boolean connect(BluetoothDevice bluetoothDevice) {
        if (this.mService == null) {
            return false;
        }
        List<BluetoothDevice> connectedDevices = getConnectedDevices();
        if (connectedDevices != null) {
            for (BluetoothDevice bluetoothDevice2 : connectedDevices) {
                if (bluetoothDevice2 == null || bluetoothDevice == null || !((getConnectionStatus(bluetoothDevice2) == 1 || getConnectionStatus(bluetoothDevice2) == 2) && bluetoothDevice2.getAddress().equals(bluetoothDevice.getAddress()))) {
                    this.mService.disconnect(bluetoothDevice2);
                } else {
                    Log.d("A2dpProfile", "The target device is connecting or connected");
                }
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
            Log.d("A2dpProfile", "finalize()");
        }
        if (this.mService != null) {
            try {
                BluetoothAdapter.getDefaultAdapter().closeProfileProxy(2, this.mService);
                this.mService = null;
            } catch (Throwable th) {
                Log.w("A2dpProfile", "Error cleaning up A2DP proxy", th);
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
        return "A2DP";
    }
}
