package com.android.settingslib.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
/* loaded from: a.zip:com/android/settingslib/bluetooth/CachedBluetoothDeviceManager.class */
public class CachedBluetoothDeviceManager {
    private final LocalBluetoothManager mBtManager;
    private final List<CachedBluetoothDevice> mCachedDevices = new ArrayList();
    private Context mContext;

    /* JADX INFO: Access modifiers changed from: package-private */
    public CachedBluetoothDeviceManager(Context context, LocalBluetoothManager localBluetoothManager) {
        this.mContext = context;
        this.mBtManager = localBluetoothManager;
    }

    public static boolean onDeviceDisappeared(CachedBluetoothDevice cachedBluetoothDevice) {
        boolean z = false;
        cachedBluetoothDevice.setVisible(false);
        if (cachedBluetoothDevice.getBondState() == 10) {
            z = true;
        }
        return z;
    }

    public CachedBluetoothDevice addDevice(LocalBluetoothAdapter localBluetoothAdapter, LocalBluetoothProfileManager localBluetoothProfileManager, BluetoothDevice bluetoothDevice) {
        CachedBluetoothDevice cachedBluetoothDevice = new CachedBluetoothDevice(this.mContext, localBluetoothAdapter, localBluetoothProfileManager, bluetoothDevice);
        synchronized (this.mCachedDevices) {
            this.mCachedDevices.add(cachedBluetoothDevice);
            this.mBtManager.getEventManager().dispatchDeviceAdded(cachedBluetoothDevice);
        }
        return cachedBluetoothDevice;
    }

    public void clearNonBondedDevices() {
        CachedBluetoothDevice cachedBluetoothDevice;
        synchronized (this) {
            for (int size = this.mCachedDevices.size() - 1; size >= 0; size--) {
                if (this.mCachedDevices.get(size).getBondState() != 12) {
                    this.mCachedDevices.remove(size);
                    Log.d("CachedBluetoothDeviceManager", "Clear NonBondedDevices : " + cachedBluetoothDevice.getBondState() + "     and device name is : " + cachedBluetoothDevice.getName());
                }
            }
        }
    }

    public CachedBluetoothDevice findDevice(BluetoothDevice bluetoothDevice) {
        for (CachedBluetoothDevice cachedBluetoothDevice : this.mCachedDevices) {
            if (cachedBluetoothDevice.getDevice().equals(bluetoothDevice)) {
                return cachedBluetoothDevice;
            }
        }
        return null;
    }

    public Collection<CachedBluetoothDevice> getCachedDevicesCopy() {
        ArrayList arrayList;
        synchronized (this) {
            arrayList = new ArrayList(this.mCachedDevices);
        }
        return arrayList;
    }

    public void onBluetoothStateChanged(int i) {
        synchronized (this) {
            if (i == 13) {
                for (int size = this.mCachedDevices.size() - 1; size >= 0; size--) {
                    CachedBluetoothDevice cachedBluetoothDevice = this.mCachedDevices.get(size);
                    if (cachedBluetoothDevice.getBondState() != 12) {
                        cachedBluetoothDevice.setVisible(false);
                        Log.d("CachedBluetoothDeviceManager", "Remove device for bond state : " + cachedBluetoothDevice.getBondState() + "     and device name is : " + cachedBluetoothDevice.getName());
                        this.mCachedDevices.remove(size);
                    } else {
                        cachedBluetoothDevice.clearProfileConnectionState();
                    }
                }
            }
        }
    }

    public void onBtClassChanged(BluetoothDevice bluetoothDevice) {
        synchronized (this) {
            CachedBluetoothDevice findDevice = findDevice(bluetoothDevice);
            if (findDevice != null) {
                findDevice.refreshBtClass();
            }
        }
    }

    public void onDeviceNameUpdated(BluetoothDevice bluetoothDevice) {
        CachedBluetoothDevice findDevice = findDevice(bluetoothDevice);
        if (findDevice != null) {
            findDevice.refreshName();
        }
    }

    public void onScanningStateChanged(boolean z) {
        synchronized (this) {
            if (z) {
                for (int size = this.mCachedDevices.size() - 1; size >= 0; size--) {
                    this.mCachedDevices.get(size).setVisible(false);
                }
            }
        }
    }

    public void onUuidChanged(BluetoothDevice bluetoothDevice) {
        synchronized (this) {
            CachedBluetoothDevice findDevice = findDevice(bluetoothDevice);
            if (findDevice != null) {
                findDevice.onUuidChanged();
            }
        }
    }
}
