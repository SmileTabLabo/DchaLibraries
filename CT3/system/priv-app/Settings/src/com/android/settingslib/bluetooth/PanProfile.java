package com.android.settingslib.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothPan;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;
import com.android.settingslib.R$drawable;
import com.android.settingslib.R$string;
import java.util.HashMap;
import java.util.List;
/* loaded from: classes.dex */
public final class PanProfile implements LocalBluetoothProfile {
    private static boolean V = true;
    private final HashMap<BluetoothDevice, Integer> mDeviceRoleMap = new HashMap<>();
    private boolean mIsProfileReady;
    private BluetoothPan mService;

    /* loaded from: classes.dex */
    private final class PanServiceListener implements BluetoothProfile.ServiceListener {
        /* synthetic */ PanServiceListener(PanProfile this$0, PanServiceListener panServiceListener) {
            this();
        }

        private PanServiceListener() {
        }

        @Override // android.bluetooth.BluetoothProfile.ServiceListener
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (PanProfile.V) {
                Log.d("PanProfile", "Bluetooth service connected");
            }
            PanProfile.this.mService = (BluetoothPan) proxy;
            PanProfile.this.mIsProfileReady = true;
        }

        @Override // android.bluetooth.BluetoothProfile.ServiceListener
        public void onServiceDisconnected(int profile) {
            if (PanProfile.V) {
                Log.d("PanProfile", "Bluetooth service disconnected");
            }
            PanProfile.this.mIsProfileReady = false;
        }
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public boolean isProfileReady() {
        return this.mIsProfileReady;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public PanProfile(Context context) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        adapter.getProfileProxy(context, new PanServiceListener(this, null), 5);
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public boolean isConnectable() {
        return true;
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public boolean isAutoConnectable() {
        return false;
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public boolean connect(BluetoothDevice device) {
        if (this.mService == null) {
            return false;
        }
        List<BluetoothDevice> sinks = this.mService.getConnectedDevices();
        if (sinks != null) {
            for (BluetoothDevice sink : sinks) {
                this.mService.disconnect(sink);
            }
        }
        return this.mService.connect(device);
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public boolean disconnect(BluetoothDevice device) {
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
        return true;
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public int getPreferred(BluetoothDevice device) {
        return -1;
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public void setPreferred(BluetoothDevice device, boolean preferred) {
    }

    public String toString() {
        return "PAN";
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public int getNameResource(BluetoothDevice device) {
        if (isLocalRoleNap(device)) {
            return R$string.bluetooth_profile_pan_nap;
        }
        return R$string.bluetooth_profile_pan;
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public int getDrawableResource(BluetoothClass btClass) {
        return R$drawable.ic_bt_network_pan;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setLocalRole(BluetoothDevice device, int role) {
        this.mDeviceRoleMap.put(device, Integer.valueOf(role));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean isLocalRoleNap(BluetoothDevice device) {
        if (this.mDeviceRoleMap.containsKey(device) && this.mDeviceRoleMap.get(device).intValue() == 1) {
            return true;
        }
        return false;
    }

    protected void finalize() {
        if (V) {
            Log.d("PanProfile", "finalize()");
        }
        if (this.mService == null) {
            return;
        }
        try {
            BluetoothAdapter.getDefaultAdapter().closeProfileProxy(5, this.mService);
            this.mService = null;
        } catch (Throwable t) {
            Log.w("PanProfile", "Error cleaning up PAN proxy", t);
        }
    }
}
