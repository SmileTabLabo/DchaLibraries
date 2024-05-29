package com.android.systemui.statusbar.policy;

import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import java.util.Collection;
/* loaded from: a.zip:com/android/systemui/statusbar/policy/BluetoothController.class */
public interface BluetoothController {

    /* loaded from: a.zip:com/android/systemui/statusbar/policy/BluetoothController$Callback.class */
    public interface Callback {
        void onBluetoothDevicesChanged();

        void onBluetoothStateChange(boolean z);
    }

    void addStateChangedCallback(Callback callback);

    boolean canConfigBluetooth();

    void connect(CachedBluetoothDevice cachedBluetoothDevice);

    void disconnect(CachedBluetoothDevice cachedBluetoothDevice);

    Collection<CachedBluetoothDevice> getDevices();

    String getLastDeviceName();

    boolean isBluetoothConnected();

    boolean isBluetoothConnecting();

    boolean isBluetoothEnabled();

    boolean isBluetoothSupported();

    void removeStateChangedCallback(Callback callback);

    void setBluetoothEnabled(boolean z);
}
