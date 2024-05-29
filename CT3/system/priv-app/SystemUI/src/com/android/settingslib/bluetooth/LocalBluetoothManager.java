package com.android.settingslib.bluetooth;

import android.content.Context;
/* loaded from: a.zip:com/android/settingslib/bluetooth/LocalBluetoothManager.class */
public final class LocalBluetoothManager {
    private static LocalBluetoothManager sInstance;
    private final CachedBluetoothDeviceManager mCachedDeviceManager;
    private final Context mContext;
    private final BluetoothEventManager mEventManager;
    private final LocalBluetoothAdapter mLocalAdapter;
    private final LocalBluetoothProfileManager mProfileManager;

    /* loaded from: a.zip:com/android/settingslib/bluetooth/LocalBluetoothManager$BluetoothManagerCallback.class */
    public interface BluetoothManagerCallback {
        void onBluetoothManagerInitialized(Context context, LocalBluetoothManager localBluetoothManager);
    }

    private LocalBluetoothManager(LocalBluetoothAdapter localBluetoothAdapter, Context context) {
        this.mContext = context;
        this.mLocalAdapter = localBluetoothAdapter;
        this.mCachedDeviceManager = new CachedBluetoothDeviceManager(context, this);
        this.mEventManager = new BluetoothEventManager(this.mLocalAdapter, this.mCachedDeviceManager, context);
        this.mProfileManager = new LocalBluetoothProfileManager(context, this.mLocalAdapter, this.mCachedDeviceManager, this.mEventManager);
    }

    public static LocalBluetoothManager getInstance(Context context, BluetoothManagerCallback bluetoothManagerCallback) {
        synchronized (LocalBluetoothManager.class) {
            try {
                if (sInstance == null) {
                    LocalBluetoothAdapter localBluetoothAdapter = LocalBluetoothAdapter.getInstance();
                    if (localBluetoothAdapter == null) {
                        return null;
                    }
                    Context applicationContext = context.getApplicationContext();
                    sInstance = new LocalBluetoothManager(localBluetoothAdapter, applicationContext);
                    if (bluetoothManagerCallback != null) {
                        bluetoothManagerCallback.onBluetoothManagerInitialized(applicationContext, sInstance);
                    }
                }
                return sInstance;
            } finally {
            }
        }
    }

    public LocalBluetoothAdapter getBluetoothAdapter() {
        return this.mLocalAdapter;
    }

    public CachedBluetoothDeviceManager getCachedDeviceManager() {
        return this.mCachedDeviceManager;
    }

    public BluetoothEventManager getEventManager() {
        return this.mEventManager;
    }

    public LocalBluetoothProfileManager getProfileManager() {
        return this.mProfileManager;
    }
}
