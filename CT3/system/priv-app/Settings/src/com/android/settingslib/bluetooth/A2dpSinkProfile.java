package com.android.settingslib.bluetooth;

import android.bluetooth.BluetoothA2dpSink;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
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
public final class A2dpSinkProfile implements LocalBluetoothProfile {
    private final CachedBluetoothDeviceManager mDeviceManager;
    private boolean mIsProfileReady;
    private final LocalBluetoothAdapter mLocalAdapter;
    private final LocalBluetoothProfileManager mProfileManager;
    private BluetoothA2dpSink mService;
    private static boolean V = true;
    static final ParcelUuid[] SRC_UUIDS = {BluetoothUuid.AudioSource, BluetoothUuid.AdvAudioDist};

    /* loaded from: classes.dex */
    private final class A2dpSinkServiceListener implements BluetoothProfile.ServiceListener {
        /* synthetic */ A2dpSinkServiceListener(A2dpSinkProfile this$0, A2dpSinkServiceListener a2dpSinkServiceListener) {
            this();
        }

        private A2dpSinkServiceListener() {
        }

        @Override // android.bluetooth.BluetoothProfile.ServiceListener
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (A2dpSinkProfile.V) {
                Log.d("A2dpSinkProfile", "Bluetooth service connected");
            }
            A2dpSinkProfile.this.mService = (BluetoothA2dpSink) proxy;
            List<BluetoothDevice> deviceList = A2dpSinkProfile.this.mService.getConnectedDevices();
            while (!deviceList.isEmpty()) {
                BluetoothDevice nextDevice = deviceList.remove(0);
                CachedBluetoothDevice device = A2dpSinkProfile.this.mDeviceManager.findDevice(nextDevice);
                if (device == null) {
                    Log.w("A2dpSinkProfile", "A2dpSinkProfile found new device: " + nextDevice);
                    device = A2dpSinkProfile.this.mDeviceManager.addDevice(A2dpSinkProfile.this.mLocalAdapter, A2dpSinkProfile.this.mProfileManager, nextDevice);
                }
                device.onProfileStateChanged(A2dpSinkProfile.this, 2);
                device.refresh();
            }
            A2dpSinkProfile.this.mIsProfileReady = true;
        }

        @Override // android.bluetooth.BluetoothProfile.ServiceListener
        public void onServiceDisconnected(int profile) {
            if (A2dpSinkProfile.V) {
                Log.d("A2dpSinkProfile", "Bluetooth service disconnected");
            }
            A2dpSinkProfile.this.mIsProfileReady = false;
        }
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public boolean isProfileReady() {
        return this.mIsProfileReady;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public A2dpSinkProfile(Context context, LocalBluetoothAdapter adapter, CachedBluetoothDeviceManager deviceManager, LocalBluetoothProfileManager profileManager) {
        this.mLocalAdapter = adapter;
        this.mDeviceManager = deviceManager;
        this.mProfileManager = profileManager;
        this.mLocalAdapter.getProfileProxy(context, new A2dpSinkServiceListener(this, null), 11);
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
        return this.mService == null ? new ArrayList(0) : this.mService.getDevicesMatchingConnectionStates(new int[]{2, 1, 3});
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public boolean connect(BluetoothDevice device) {
        if (this.mService == null) {
            return false;
        }
        List<BluetoothDevice> srcs = getConnectedDevices();
        if (srcs != null) {
            for (BluetoothDevice src : srcs) {
                if (src.equals(device)) {
                    Log.d("A2dpSinkProfile", "Ignoring Connect");
                    return true;
                }
            }
            for (BluetoothDevice src2 : srcs) {
                this.mService.disconnect(src2);
            }
        }
        return this.mService.connect(device);
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public boolean disconnect(BluetoothDevice device) {
        if (this.mService == null) {
            return false;
        }
        if (this.mService.getPriority(device) > 100) {
            this.mService.setPriority(device, 100);
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

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean isA2dpPlaying() {
        if (this.mService == null) {
            return false;
        }
        List<BluetoothDevice> srcs = this.mService.getConnectedDevices();
        return !srcs.isEmpty() && this.mService.isA2dpPlaying(srcs.get(0));
    }

    public String toString() {
        return "A2DPSink";
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public int getNameResource(BluetoothDevice device) {
        return R$string.bluetooth_profile_a2dp;
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public int getDrawableResource(BluetoothClass btClass) {
        return R$drawable.ic_bt_headphones_a2dp;
    }

    protected void finalize() {
        if (V) {
            Log.d("A2dpSinkProfile", "finalize()");
        }
        if (this.mService == null) {
            return;
        }
        try {
            BluetoothAdapter.getDefaultAdapter().closeProfileProxy(11, this.mService);
            this.mService = null;
        } catch (Throwable t) {
            Log.w("A2dpSinkProfile", "Error cleaning up A2DP proxy", t);
        }
    }
}
