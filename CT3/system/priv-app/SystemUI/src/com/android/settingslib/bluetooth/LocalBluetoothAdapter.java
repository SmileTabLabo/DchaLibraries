package com.android.settingslib.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.os.ParcelUuid;
import java.util.Set;
/* loaded from: a.zip:com/android/settingslib/bluetooth/LocalBluetoothAdapter.class */
public class LocalBluetoothAdapter {
    private static LocalBluetoothAdapter sInstance;
    private final BluetoothAdapter mAdapter;
    private LocalBluetoothProfileManager mProfileManager;
    private int mState = Integer.MIN_VALUE;

    private LocalBluetoothAdapter(BluetoothAdapter bluetoothAdapter) {
        this.mAdapter = bluetoothAdapter;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static LocalBluetoothAdapter getInstance() {
        LocalBluetoothAdapter localBluetoothAdapter;
        BluetoothAdapter defaultAdapter;
        synchronized (LocalBluetoothAdapter.class) {
            try {
                if (sInstance == null && (defaultAdapter = BluetoothAdapter.getDefaultAdapter()) != null) {
                    sInstance = new LocalBluetoothAdapter(defaultAdapter);
                }
                localBluetoothAdapter = sInstance;
            } catch (Throwable th) {
                throw th;
            }
        }
        return localBluetoothAdapter;
    }

    public void cancelDiscovery() {
        this.mAdapter.cancelDiscovery();
    }

    public boolean enable() {
        return this.mAdapter.enable();
    }

    public BluetoothLeScanner getBluetoothLeScanner() {
        return this.mAdapter.getBluetoothLeScanner();
    }

    public int getBluetoothState() {
        int i;
        synchronized (this) {
            syncBluetoothState();
            i = this.mState;
        }
        return i;
    }

    public Set<BluetoothDevice> getBondedDevices() {
        return this.mAdapter.getBondedDevices();
    }

    public int getConnectionState() {
        return this.mAdapter.getConnectionState();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void getProfileProxy(Context context, BluetoothProfile.ServiceListener serviceListener, int i) {
        this.mAdapter.getProfileProxy(context, serviceListener, i);
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

    public boolean setBluetoothEnabled(boolean z) {
        boolean enable = z ? this.mAdapter.enable() : this.mAdapter.disable();
        if (enable) {
            setBluetoothStateInt(z ? 11 : 13);
        } else {
            syncBluetoothState();
        }
        return enable;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setBluetoothStateInt(int i) {
        synchronized (this) {
            this.mState = i;
            if (i == 12 && this.mProfileManager != null) {
                this.mProfileManager.setBluetoothStateOn();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setProfileManager(LocalBluetoothProfileManager localBluetoothProfileManager) {
        this.mProfileManager = localBluetoothProfileManager;
    }

    boolean syncBluetoothState() {
        if (this.mAdapter.getState() != this.mState) {
            setBluetoothStateInt(this.mAdapter.getState());
            return true;
        }
        return false;
    }
}
