package com.android.settingslib.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.ParcelUuid;
import com.android.settingslib.wifi.AccessPoint;
import java.util.List;
import java.util.Set;
/* loaded from: classes.dex */
public class LocalBluetoothAdapter {
    private static LocalBluetoothAdapter sInstance;
    private final BluetoothAdapter mAdapter;
    private long mLastScan;
    private LocalBluetoothProfileManager mProfileManager;
    private int mState = AccessPoint.UNREACHABLE_RSSI;

    private LocalBluetoothAdapter(BluetoothAdapter bluetoothAdapter) {
        this.mAdapter = bluetoothAdapter;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setProfileManager(LocalBluetoothProfileManager localBluetoothProfileManager) {
        this.mProfileManager = localBluetoothProfileManager;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static synchronized LocalBluetoothAdapter getInstance() {
        LocalBluetoothAdapter localBluetoothAdapter;
        BluetoothAdapter defaultAdapter;
        synchronized (LocalBluetoothAdapter.class) {
            if (sInstance == null && (defaultAdapter = BluetoothAdapter.getDefaultAdapter()) != null) {
                sInstance = new LocalBluetoothAdapter(defaultAdapter);
            }
            localBluetoothAdapter = sInstance;
        }
        return localBluetoothAdapter;
    }

    public void cancelDiscovery() {
        this.mAdapter.cancelDiscovery();
    }

    public boolean enable() {
        return this.mAdapter.enable();
    }

    public boolean disable() {
        return this.mAdapter.disable();
    }

    public String getAddress() {
        return this.mAdapter.getAddress();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void getProfileProxy(Context context, BluetoothProfile.ServiceListener serviceListener, int i) {
        this.mAdapter.getProfileProxy(context, serviceListener, i);
    }

    public Set<BluetoothDevice> getBondedDevices() {
        return this.mAdapter.getBondedDevices();
    }

    public String getName() {
        return this.mAdapter.getName();
    }

    public int getScanMode() {
        return this.mAdapter.getScanMode();
    }

    public int getState() {
        return this.mAdapter.getState();
    }

    public ParcelUuid[] getUuids() {
        return this.mAdapter.getUuids();
    }

    public boolean isDiscovering() {
        return this.mAdapter.isDiscovering();
    }

    public boolean isEnabled() {
        return this.mAdapter.isEnabled();
    }

    public long getDiscoveryEndMillis() {
        return this.mAdapter.getDiscoveryEndMillis();
    }

    public void setName(String str) {
        this.mAdapter.setName(str);
    }

    public void setScanMode(int i) {
        this.mAdapter.setScanMode(i);
    }

    public boolean setScanMode(int i, int i2) {
        return this.mAdapter.setScanMode(i, i2);
    }

    public void startScanning(boolean z) {
        if (!this.mAdapter.isDiscovering()) {
            if (!z) {
                if (this.mLastScan + 300000 > System.currentTimeMillis()) {
                    return;
                }
                A2dpProfile a2dpProfile = this.mProfileManager.getA2dpProfile();
                if (a2dpProfile != null && a2dpProfile.isA2dpPlaying()) {
                    return;
                }
                A2dpSinkProfile a2dpSinkProfile = this.mProfileManager.getA2dpSinkProfile();
                if (a2dpSinkProfile != null && a2dpSinkProfile.isA2dpPlaying()) {
                    return;
                }
            }
            if (this.mAdapter.startDiscovery()) {
                this.mLastScan = System.currentTimeMillis();
            }
        }
    }

    public void stopScanning() {
        if (this.mAdapter.isDiscovering()) {
            this.mAdapter.cancelDiscovery();
        }
    }

    public synchronized int getBluetoothState() {
        syncBluetoothState();
        return this.mState;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setBluetoothStateInt(int i) {
        synchronized (this) {
            if (this.mState == i) {
                return;
            }
            this.mState = i;
            if (i == 12 && this.mProfileManager != null) {
                this.mProfileManager.setBluetoothStateOn();
            }
        }
    }

    boolean syncBluetoothState() {
        if (this.mAdapter.getState() != this.mState) {
            setBluetoothStateInt(this.mAdapter.getState());
            return true;
        }
        return false;
    }

    public boolean setBluetoothEnabled(boolean z) {
        boolean disable;
        int i;
        if (z) {
            disable = this.mAdapter.enable();
        } else {
            disable = this.mAdapter.disable();
        }
        if (disable) {
            if (z) {
                i = 11;
            } else {
                i = 13;
            }
            setBluetoothStateInt(i);
        } else {
            syncBluetoothState();
        }
        return disable;
    }

    public BluetoothDevice getRemoteDevice(String str) {
        return this.mAdapter.getRemoteDevice(str);
    }

    public int getMaxConnectedAudioDevices() {
        return this.mAdapter.getMaxConnectedAudioDevices();
    }

    public List<Integer> getSupportedProfiles() {
        return this.mAdapter.getSupportedProfiles();
    }
}
