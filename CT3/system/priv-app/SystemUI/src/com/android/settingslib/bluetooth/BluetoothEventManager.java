package com.android.settingslib.bluetooth;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import com.android.settingslib.R$string;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
/* loaded from: a.zip:com/android/settingslib/bluetooth/BluetoothEventManager.class */
public final class BluetoothEventManager {
    private Context mContext;
    private final CachedBluetoothDeviceManager mDeviceManager;
    private final LocalBluetoothAdapter mLocalAdapter;
    private LocalBluetoothProfileManager mProfileManager;
    private android.os.Handler mReceiverHandler;
    private final Collection<BluetoothCallback> mCallbacks = new ArrayList();
    final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver(this) { // from class: com.android.settingslib.bluetooth.BluetoothEventManager.1
        final BluetoothEventManager this$0;

        {
            this.this$0 = this;
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            Log.v("BluetoothEventManager", "Received " + intent.getAction());
            String action = intent.getAction();
            BluetoothDevice bluetoothDevice = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
            Handler handler = (Handler) this.this$0.mHandlerMap.get(action);
            if (handler != null) {
                handler.onReceive(context, intent, bluetoothDevice);
            }
        }
    };
    private final IntentFilter mAdapterIntentFilter = new IntentFilter();
    private final IntentFilter mProfileIntentFilter = new IntentFilter();
    private final Map<String, Handler> mHandlerMap = new HashMap();

    /* loaded from: a.zip:com/android/settingslib/bluetooth/BluetoothEventManager$AdapterStateChangedHandler.class */
    private class AdapterStateChangedHandler implements Handler {
        final BluetoothEventManager this$0;

        private AdapterStateChangedHandler(BluetoothEventManager bluetoothEventManager) {
            this.this$0 = bluetoothEventManager;
        }

        /* synthetic */ AdapterStateChangedHandler(BluetoothEventManager bluetoothEventManager, AdapterStateChangedHandler adapterStateChangedHandler) {
            this(bluetoothEventManager);
        }

        @Override // com.android.settingslib.bluetooth.BluetoothEventManager.Handler
        public void onReceive(Context context, Intent intent, BluetoothDevice bluetoothDevice) {
            int intExtra = intent.getIntExtra("android.bluetooth.adapter.extra.STATE", Integer.MIN_VALUE);
            this.this$0.mLocalAdapter.setBluetoothStateInt(intExtra);
            synchronized (this.this$0.mCallbacks) {
                for (BluetoothCallback bluetoothCallback : this.this$0.mCallbacks) {
                    bluetoothCallback.onBluetoothStateChanged(intExtra);
                }
            }
            this.this$0.mDeviceManager.onBluetoothStateChanged(intExtra);
        }
    }

    /* loaded from: a.zip:com/android/settingslib/bluetooth/BluetoothEventManager$BondStateChangedHandler.class */
    private class BondStateChangedHandler implements Handler {
        final BluetoothEventManager this$0;

        private BondStateChangedHandler(BluetoothEventManager bluetoothEventManager) {
            this.this$0 = bluetoothEventManager;
        }

        /* synthetic */ BondStateChangedHandler(BluetoothEventManager bluetoothEventManager, BondStateChangedHandler bondStateChangedHandler) {
            this(bluetoothEventManager);
        }

        private void showUnbondMessage(Context context, String str, int i) {
            int i2;
            switch (i) {
                case 1:
                    i2 = R$string.bluetooth_pairing_pin_error_message;
                    break;
                case 2:
                    i2 = R$string.bluetooth_pairing_rejected_error_message;
                    break;
                case 3:
                default:
                    Log.w("BluetoothEventManager", "showUnbondMessage: Not displaying any message for reason: " + i);
                    return;
                case 4:
                    i2 = R$string.bluetooth_pairing_device_down_error_message;
                    break;
                case 5:
                case 6:
                case 7:
                case 8:
                    i2 = R$string.bluetooth_pairing_error_message;
                    break;
            }
            Utils.showError(context, str, i2);
        }

        @Override // com.android.settingslib.bluetooth.BluetoothEventManager.Handler
        public void onReceive(Context context, Intent intent, BluetoothDevice bluetoothDevice) {
            if (bluetoothDevice == null) {
                Log.e("BluetoothEventManager", "ACTION_BOND_STATE_CHANGED with no EXTRA_DEVICE");
                return;
            }
            int intExtra = intent.getIntExtra("android.bluetooth.device.extra.BOND_STATE", Integer.MIN_VALUE);
            CachedBluetoothDevice findDevice = this.this$0.mDeviceManager.findDevice(bluetoothDevice);
            CachedBluetoothDevice cachedBluetoothDevice = findDevice;
            if (findDevice == null) {
                Log.w("BluetoothEventManager", "CachedBluetoothDevice for device " + bluetoothDevice + " not found, calling readPairedDevices().");
                if (!this.this$0.readPairedDevices()) {
                    Log.e("BluetoothEventManager", "Got bonding state changed for " + bluetoothDevice + ", but we have no record of that device.");
                    return;
                }
                CachedBluetoothDevice findDevice2 = this.this$0.mDeviceManager.findDevice(bluetoothDevice);
                cachedBluetoothDevice = findDevice2;
                if (findDevice2 == null) {
                    Log.e("BluetoothEventManager", "Got bonding state changed for " + bluetoothDevice + ", but device not added in cache.");
                    return;
                }
            }
            synchronized (this.this$0.mCallbacks) {
                for (BluetoothCallback bluetoothCallback : this.this$0.mCallbacks) {
                    bluetoothCallback.onDeviceBondStateChanged(cachedBluetoothDevice, intExtra);
                }
            }
            cachedBluetoothDevice.onBondingStateChanged(intExtra);
            if (intExtra == 10) {
                int intExtra2 = intent.getIntExtra("android.bluetooth.device.extra.REASON", Integer.MIN_VALUE);
                Log.d("BluetoothEventManager", cachedBluetoothDevice.getName() + " show unbond message for " + intExtra2);
                showUnbondMessage(context, cachedBluetoothDevice.getName(), intExtra2);
            }
        }
    }

    /* loaded from: a.zip:com/android/settingslib/bluetooth/BluetoothEventManager$ClassChangedHandler.class */
    private class ClassChangedHandler implements Handler {
        final BluetoothEventManager this$0;

        private ClassChangedHandler(BluetoothEventManager bluetoothEventManager) {
            this.this$0 = bluetoothEventManager;
        }

        /* synthetic */ ClassChangedHandler(BluetoothEventManager bluetoothEventManager, ClassChangedHandler classChangedHandler) {
            this(bluetoothEventManager);
        }

        @Override // com.android.settingslib.bluetooth.BluetoothEventManager.Handler
        public void onReceive(Context context, Intent intent, BluetoothDevice bluetoothDevice) {
            this.this$0.mDeviceManager.onBtClassChanged(bluetoothDevice);
        }
    }

    /* loaded from: a.zip:com/android/settingslib/bluetooth/BluetoothEventManager$ConnectionStateChangedHandler.class */
    private class ConnectionStateChangedHandler implements Handler {
        final BluetoothEventManager this$0;

        private ConnectionStateChangedHandler(BluetoothEventManager bluetoothEventManager) {
            this.this$0 = bluetoothEventManager;
        }

        /* synthetic */ ConnectionStateChangedHandler(BluetoothEventManager bluetoothEventManager, ConnectionStateChangedHandler connectionStateChangedHandler) {
            this(bluetoothEventManager);
        }

        @Override // com.android.settingslib.bluetooth.BluetoothEventManager.Handler
        public void onReceive(Context context, Intent intent, BluetoothDevice bluetoothDevice) {
            this.this$0.dispatchConnectionStateChanged(this.this$0.mDeviceManager.findDevice(bluetoothDevice), intent.getIntExtra("android.bluetooth.adapter.extra.CONNECTION_STATE", Integer.MIN_VALUE));
        }
    }

    /* loaded from: a.zip:com/android/settingslib/bluetooth/BluetoothEventManager$DeviceDisappearedHandler.class */
    private class DeviceDisappearedHandler implements Handler {
        final BluetoothEventManager this$0;

        private DeviceDisappearedHandler(BluetoothEventManager bluetoothEventManager) {
            this.this$0 = bluetoothEventManager;
        }

        /* synthetic */ DeviceDisappearedHandler(BluetoothEventManager bluetoothEventManager, DeviceDisappearedHandler deviceDisappearedHandler) {
            this(bluetoothEventManager);
        }

        @Override // com.android.settingslib.bluetooth.BluetoothEventManager.Handler
        public void onReceive(Context context, Intent intent, BluetoothDevice bluetoothDevice) {
            CachedBluetoothDevice findDevice = this.this$0.mDeviceManager.findDevice(bluetoothDevice);
            if (findDevice == null) {
                Log.w("BluetoothEventManager", "received ACTION_DISAPPEARED for an unknown device: " + bluetoothDevice);
            } else if (CachedBluetoothDeviceManager.onDeviceDisappeared(findDevice)) {
                synchronized (this.this$0.mCallbacks) {
                    for (BluetoothCallback bluetoothCallback : this.this$0.mCallbacks) {
                        bluetoothCallback.onDeviceDeleted(findDevice);
                    }
                }
            }
        }
    }

    /* loaded from: a.zip:com/android/settingslib/bluetooth/BluetoothEventManager$DeviceFoundHandler.class */
    private class DeviceFoundHandler implements Handler {
        final BluetoothEventManager this$0;

        private DeviceFoundHandler(BluetoothEventManager bluetoothEventManager) {
            this.this$0 = bluetoothEventManager;
        }

        /* synthetic */ DeviceFoundHandler(BluetoothEventManager bluetoothEventManager, DeviceFoundHandler deviceFoundHandler) {
            this(bluetoothEventManager);
        }

        @Override // com.android.settingslib.bluetooth.BluetoothEventManager.Handler
        public void onReceive(Context context, Intent intent, BluetoothDevice bluetoothDevice) {
            Integer num = null;
            short shortExtra = intent.getShortExtra("android.bluetooth.device.extra.RSSI", Short.MIN_VALUE);
            BluetoothClass bluetoothClass = (BluetoothClass) intent.getParcelableExtra("android.bluetooth.device.extra.CLASS");
            String stringExtra = intent.getStringExtra("android.bluetooth.device.extra.NAME");
            StringBuilder append = new StringBuilder().append("Device ").append(stringExtra).append(" ,Class: ");
            if (bluetoothClass != null) {
                num = Integer.valueOf(bluetoothClass.getMajorDeviceClass());
            }
            Log.d("BluetoothEventManager", append.append(num).toString());
            CachedBluetoothDevice findDevice = this.this$0.mDeviceManager.findDevice(bluetoothDevice);
            CachedBluetoothDevice cachedBluetoothDevice = findDevice;
            if (findDevice == null) {
                cachedBluetoothDevice = this.this$0.mDeviceManager.addDevice(this.this$0.mLocalAdapter, this.this$0.mProfileManager, bluetoothDevice);
                Log.d("BluetoothEventManager", "DeviceFoundHandler created new CachedBluetoothDevice: " + cachedBluetoothDevice);
            }
            cachedBluetoothDevice.setRssi(shortExtra);
            cachedBluetoothDevice.setBtClass(bluetoothClass);
            cachedBluetoothDevice.setNewName(stringExtra);
            cachedBluetoothDevice.setVisible(true);
        }
    }

    /* loaded from: a.zip:com/android/settingslib/bluetooth/BluetoothEventManager$DockEventHandler.class */
    private class DockEventHandler implements Handler {
        final BluetoothEventManager this$0;

        private DockEventHandler(BluetoothEventManager bluetoothEventManager) {
            this.this$0 = bluetoothEventManager;
        }

        /* synthetic */ DockEventHandler(BluetoothEventManager bluetoothEventManager, DockEventHandler dockEventHandler) {
            this(bluetoothEventManager);
        }

        @Override // com.android.settingslib.bluetooth.BluetoothEventManager.Handler
        public void onReceive(Context context, Intent intent, BluetoothDevice bluetoothDevice) {
            CachedBluetoothDevice findDevice;
            if (intent.getIntExtra("android.intent.extra.DOCK_STATE", 1) != 0 || bluetoothDevice == null || bluetoothDevice.getBondState() != 10 || (findDevice = this.this$0.mDeviceManager.findDevice(bluetoothDevice)) == null) {
                return;
            }
            findDevice.setVisible(false);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: a.zip:com/android/settingslib/bluetooth/BluetoothEventManager$Handler.class */
    public interface Handler {
        void onReceive(Context context, Intent intent, BluetoothDevice bluetoothDevice);
    }

    /* loaded from: a.zip:com/android/settingslib/bluetooth/BluetoothEventManager$NameChangedHandler.class */
    private class NameChangedHandler implements Handler {
        final BluetoothEventManager this$0;

        private NameChangedHandler(BluetoothEventManager bluetoothEventManager) {
            this.this$0 = bluetoothEventManager;
        }

        /* synthetic */ NameChangedHandler(BluetoothEventManager bluetoothEventManager, NameChangedHandler nameChangedHandler) {
            this(bluetoothEventManager);
        }

        @Override // com.android.settingslib.bluetooth.BluetoothEventManager.Handler
        public void onReceive(Context context, Intent intent, BluetoothDevice bluetoothDevice) {
            this.this$0.mDeviceManager.onDeviceNameUpdated(bluetoothDevice);
        }
    }

    /* loaded from: a.zip:com/android/settingslib/bluetooth/BluetoothEventManager$PairingCancelHandler.class */
    private class PairingCancelHandler implements Handler {
        final BluetoothEventManager this$0;

        private PairingCancelHandler(BluetoothEventManager bluetoothEventManager) {
            this.this$0 = bluetoothEventManager;
        }

        /* synthetic */ PairingCancelHandler(BluetoothEventManager bluetoothEventManager, PairingCancelHandler pairingCancelHandler) {
            this(bluetoothEventManager);
        }

        @Override // com.android.settingslib.bluetooth.BluetoothEventManager.Handler
        public void onReceive(Context context, Intent intent, BluetoothDevice bluetoothDevice) {
            if (bluetoothDevice == null) {
                Log.e("BluetoothEventManager", "ACTION_PAIRING_CANCEL with no EXTRA_DEVICE");
                return;
            }
            CachedBluetoothDevice findDevice = this.this$0.mDeviceManager.findDevice(bluetoothDevice);
            if (findDevice == null) {
                Log.e("BluetoothEventManager", "ACTION_PAIRING_CANCEL with no cached device");
                return;
            }
            int i = R$string.bluetooth_pairing_error_message;
            if (context == null || findDevice == null) {
                return;
            }
            Utils.showError(context, findDevice.getName(), i);
        }
    }

    /* loaded from: a.zip:com/android/settingslib/bluetooth/BluetoothEventManager$ScanningStateChangedHandler.class */
    private class ScanningStateChangedHandler implements Handler {
        private final boolean mStarted;
        final BluetoothEventManager this$0;

        ScanningStateChangedHandler(BluetoothEventManager bluetoothEventManager, boolean z) {
            this.this$0 = bluetoothEventManager;
            this.mStarted = z;
        }

        @Override // com.android.settingslib.bluetooth.BluetoothEventManager.Handler
        public void onReceive(Context context, Intent intent, BluetoothDevice bluetoothDevice) {
            synchronized (this.this$0.mCallbacks) {
                for (BluetoothCallback bluetoothCallback : this.this$0.mCallbacks) {
                    bluetoothCallback.onScanningStateChanged(this.mStarted);
                }
            }
            Log.d("BluetoothEventManager", "scanning state change to " + this.mStarted);
            this.this$0.mDeviceManager.onScanningStateChanged(this.mStarted);
        }
    }

    /* loaded from: a.zip:com/android/settingslib/bluetooth/BluetoothEventManager$UuidChangedHandler.class */
    private class UuidChangedHandler implements Handler {
        final BluetoothEventManager this$0;

        private UuidChangedHandler(BluetoothEventManager bluetoothEventManager) {
            this.this$0 = bluetoothEventManager;
        }

        /* synthetic */ UuidChangedHandler(BluetoothEventManager bluetoothEventManager, UuidChangedHandler uuidChangedHandler) {
            this(bluetoothEventManager);
        }

        @Override // com.android.settingslib.bluetooth.BluetoothEventManager.Handler
        public void onReceive(Context context, Intent intent, BluetoothDevice bluetoothDevice) {
            this.this$0.mDeviceManager.onUuidChanged(bluetoothDevice);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public BluetoothEventManager(LocalBluetoothAdapter localBluetoothAdapter, CachedBluetoothDeviceManager cachedBluetoothDeviceManager, Context context) {
        this.mLocalAdapter = localBluetoothAdapter;
        this.mDeviceManager = cachedBluetoothDeviceManager;
        this.mContext = context;
        addHandler("android.bluetooth.adapter.action.STATE_CHANGED", new AdapterStateChangedHandler(this, null));
        addHandler("android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED", new ConnectionStateChangedHandler(this, null));
        addHandler("android.bluetooth.adapter.action.DISCOVERY_STARTED", new ScanningStateChangedHandler(this, true));
        addHandler("android.bluetooth.adapter.action.DISCOVERY_FINISHED", new ScanningStateChangedHandler(this, false));
        addHandler("android.bluetooth.device.action.FOUND", new DeviceFoundHandler(this, null));
        addHandler("android.bluetooth.device.action.DISAPPEARED", new DeviceDisappearedHandler(this, null));
        addHandler("android.bluetooth.device.action.NAME_CHANGED", new NameChangedHandler(this, null));
        addHandler("android.bluetooth.device.action.ALIAS_CHANGED", new NameChangedHandler(this, null));
        addHandler("android.bluetooth.device.action.BOND_STATE_CHANGED", new BondStateChangedHandler(this, null));
        addHandler("android.bluetooth.device.action.PAIRING_CANCEL", new PairingCancelHandler(this, null));
        addHandler("android.bluetooth.device.action.CLASS_CHANGED", new ClassChangedHandler(this, null));
        addHandler("android.bluetooth.device.action.UUID", new UuidChangedHandler(this, null));
        addHandler("android.intent.action.DOCK_EVENT", new DockEventHandler(this, null));
        this.mContext.registerReceiver(this.mBroadcastReceiver, this.mAdapterIntentFilter, null, this.mReceiverHandler);
    }

    private void addHandler(String str, Handler handler) {
        this.mHandlerMap.put(str, handler);
        this.mAdapterIntentFilter.addAction(str);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void dispatchConnectionStateChanged(CachedBluetoothDevice cachedBluetoothDevice, int i) {
        synchronized (this.mCallbacks) {
            for (BluetoothCallback bluetoothCallback : this.mCallbacks) {
                bluetoothCallback.onConnectionStateChanged(cachedBluetoothDevice, i);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void addProfileHandler(String str, Handler handler) {
        this.mHandlerMap.put(str, handler);
        this.mProfileIntentFilter.addAction(str);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void dispatchDeviceAdded(CachedBluetoothDevice cachedBluetoothDevice) {
        synchronized (this.mCallbacks) {
            for (BluetoothCallback bluetoothCallback : this.mCallbacks) {
                bluetoothCallback.onDeviceAdded(cachedBluetoothDevice);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean readPairedDevices() {
        Set<BluetoothDevice> bondedDevices = this.mLocalAdapter.getBondedDevices();
        if (bondedDevices == null) {
            return false;
        }
        boolean z = false;
        for (BluetoothDevice bluetoothDevice : bondedDevices) {
            if (this.mDeviceManager.findDevice(bluetoothDevice) == null) {
                dispatchDeviceAdded(this.mDeviceManager.addDevice(this.mLocalAdapter, this.mProfileManager, bluetoothDevice));
                z = true;
            }
        }
        return z;
    }

    public void registerCallback(BluetoothCallback bluetoothCallback) {
        synchronized (this.mCallbacks) {
            this.mCallbacks.add(bluetoothCallback);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void registerProfileIntentReceiver() {
        this.mContext.registerReceiver(this.mBroadcastReceiver, this.mProfileIntentFilter, null, this.mReceiverHandler);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setProfileManager(LocalBluetoothProfileManager localBluetoothProfileManager) {
        this.mProfileManager = localBluetoothProfileManager;
    }

    public void setReceiverHandler(android.os.Handler handler) {
        this.mContext.unregisterReceiver(this.mBroadcastReceiver);
        this.mReceiverHandler = handler;
        this.mContext.registerReceiver(this.mBroadcastReceiver, this.mAdapterIntentFilter, null, this.mReceiverHandler);
        registerProfileIntentReceiver();
    }
}
