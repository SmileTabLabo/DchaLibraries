package com.android.settingslib.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothInputDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;
import com.android.settingslib.R$drawable;
import com.android.settingslib.R$string;
import java.util.List;
/* loaded from: classes.dex */
public final class HidProfile implements LocalBluetoothProfile {
    private static boolean V = true;
    private final CachedBluetoothDeviceManager mDeviceManager;
    private boolean mIsProfileReady;
    private final LocalBluetoothAdapter mLocalAdapter;
    private final LocalBluetoothProfileManager mProfileManager;
    private BluetoothInputDevice mService;

    /* loaded from: classes.dex */
    private final class InputDeviceServiceListener implements BluetoothProfile.ServiceListener {
        /* synthetic */ InputDeviceServiceListener(HidProfile this$0, InputDeviceServiceListener inputDeviceServiceListener) {
            this();
        }

        private InputDeviceServiceListener() {
        }

        @Override // android.bluetooth.BluetoothProfile.ServiceListener
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (HidProfile.V) {
                Log.d("HidProfile", "Bluetooth service connected");
            }
            HidProfile.this.mService = (BluetoothInputDevice) proxy;
            List<BluetoothDevice> deviceList = HidProfile.this.mService.getConnectedDevices();
            while (!deviceList.isEmpty()) {
                BluetoothDevice nextDevice = deviceList.remove(0);
                CachedBluetoothDevice device = HidProfile.this.mDeviceManager.findDevice(nextDevice);
                if (device == null) {
                    Log.w("HidProfile", "HidProfile found new device: " + nextDevice);
                    device = HidProfile.this.mDeviceManager.addDevice(HidProfile.this.mLocalAdapter, HidProfile.this.mProfileManager, nextDevice);
                }
                device.onProfileStateChanged(HidProfile.this, 2);
                device.refresh();
            }
            HidProfile.this.mIsProfileReady = true;
        }

        @Override // android.bluetooth.BluetoothProfile.ServiceListener
        public void onServiceDisconnected(int profile) {
            if (HidProfile.V) {
                Log.d("HidProfile", "Bluetooth service disconnected");
            }
            HidProfile.this.mIsProfileReady = false;
        }
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public boolean isProfileReady() {
        return this.mIsProfileReady;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public HidProfile(Context context, LocalBluetoothAdapter adapter, CachedBluetoothDeviceManager deviceManager, LocalBluetoothProfileManager profileManager) {
        this.mLocalAdapter = adapter;
        this.mDeviceManager = deviceManager;
        this.mProfileManager = profileManager;
        adapter.getProfileProxy(context, new InputDeviceServiceListener(this, null), 4);
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public boolean isConnectable() {
        return true;
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public boolean isAutoConnectable() {
        return true;
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public boolean connect(BluetoothDevice device) {
        if (this.mService == null) {
            return false;
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
        List<BluetoothDevice> deviceList = this.mService.getConnectedDevices();
        if (deviceList.isEmpty() || !deviceList.get(0).equals(device)) {
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
        return "HID";
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public int getNameResource(BluetoothDevice device) {
        return R$string.bluetooth_profile_hid;
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public int getDrawableResource(BluetoothClass btClass) {
        if (btClass == null) {
            return R$drawable.ic_lockscreen_ime;
        }
        return getHidClassDrawable(btClass);
    }

    public static int getHidClassDrawable(BluetoothClass btClass) {
        switch (btClass.getDeviceClass()) {
            case 1344:
            case 1472:
                return R$drawable.ic_lockscreen_ime;
            case 1408:
                return R$drawable.ic_bt_pointing_hid;
            default:
                return R$drawable.ic_bt_misc_hid;
        }
    }

    protected void finalize() {
        if (V) {
            Log.d("HidProfile", "finalize()");
        }
        if (this.mService == null) {
            return;
        }
        try {
            BluetoothAdapter.getDefaultAdapter().closeProfileProxy(4, this.mService);
            this.mService = null;
        } catch (Throwable t) {
            Log.w("HidProfile", "Error cleaning up HID proxy", t);
        }
    }
}
