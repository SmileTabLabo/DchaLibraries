package com.android.settingslib.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothPan;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;
import java.util.HashMap;
import java.util.List;
/* loaded from: a.zip:com/android/settingslib/bluetooth/PanProfile.class */
public final class PanProfile implements LocalBluetoothProfile {
    private static boolean V = true;
    private final HashMap<BluetoothDevice, Integer> mDeviceRoleMap = new HashMap<>();
    private boolean mIsProfileReady;
    private BluetoothPan mService;

    /* loaded from: a.zip:com/android/settingslib/bluetooth/PanProfile$PanServiceListener.class */
    private final class PanServiceListener implements BluetoothProfile.ServiceListener {
        final PanProfile this$0;

        private PanServiceListener(PanProfile panProfile) {
            this.this$0 = panProfile;
        }

        /* synthetic */ PanServiceListener(PanProfile panProfile, PanServiceListener panServiceListener) {
            this(panProfile);
        }

        @Override // android.bluetooth.BluetoothProfile.ServiceListener
        public void onServiceConnected(int i, BluetoothProfile bluetoothProfile) {
            if (PanProfile.V) {
                Log.d("PanProfile", "Bluetooth service connected");
            }
            this.this$0.mService = (BluetoothPan) bluetoothProfile;
            this.this$0.mIsProfileReady = true;
        }

        @Override // android.bluetooth.BluetoothProfile.ServiceListener
        public void onServiceDisconnected(int i) {
            if (PanProfile.V) {
                Log.d("PanProfile", "Bluetooth service disconnected");
            }
            this.this$0.mIsProfileReady = false;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public PanProfile(Context context) {
        BluetoothAdapter.getDefaultAdapter().getProfileProxy(context, new PanServiceListener(this, null), 5);
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public boolean connect(BluetoothDevice bluetoothDevice) {
        if (this.mService == null) {
            return false;
        }
        List<BluetoothDevice> connectedDevices = this.mService.getConnectedDevices();
        if (connectedDevices != null) {
            for (BluetoothDevice bluetoothDevice2 : connectedDevices) {
                this.mService.disconnect(bluetoothDevice2);
            }
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
            Log.d("PanProfile", "finalize()");
        }
        if (this.mService != null) {
            try {
                BluetoothAdapter.getDefaultAdapter().closeProfileProxy(5, this.mService);
                this.mService = null;
            } catch (Throwable th) {
                Log.w("PanProfile", "Error cleaning up PAN proxy", th);
            }
        }
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
        return false;
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public boolean isConnectable() {
        return true;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean isLocalRoleNap(BluetoothDevice bluetoothDevice) {
        if (this.mDeviceRoleMap.containsKey(bluetoothDevice)) {
            return this.mDeviceRoleMap.get(bluetoothDevice).intValue() == 1;
        }
        return false;
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public boolean isPreferred(BluetoothDevice bluetoothDevice) {
        return true;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setLocalRole(BluetoothDevice bluetoothDevice, int i) {
        this.mDeviceRoleMap.put(bluetoothDevice, Integer.valueOf(i));
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public void setPreferred(BluetoothDevice bluetoothDevice, boolean z) {
    }

    public String toString() {
        return "PAN";
    }
}
