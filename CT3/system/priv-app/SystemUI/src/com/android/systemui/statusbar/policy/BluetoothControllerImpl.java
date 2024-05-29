package com.android.systemui.statusbar.policy;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;
import com.android.settingslib.bluetooth.BluetoothCallback;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import com.android.systemui.statusbar.policy.BluetoothController;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
/* loaded from: a.zip:com/android/systemui/statusbar/policy/BluetoothControllerImpl.class */
public class BluetoothControllerImpl implements BluetoothController, BluetoothCallback, CachedBluetoothDevice.Callback {
    private static final boolean DEBUG = Log.isLoggable("BluetoothController", 3);
    private final int mCurrentUser;
    private boolean mEnabled;
    private CachedBluetoothDevice mLastDevice;
    private final LocalBluetoothManager mLocalBluetoothManager;
    private final UserManager mUserManager;
    private int mConnectionState = 0;
    private final H mHandler = new H(this, null);

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/statusbar/policy/BluetoothControllerImpl$H.class */
    public final class H extends Handler {
        private final ArrayList<BluetoothController.Callback> mCallbacks;
        final BluetoothControllerImpl this$0;

        private H(BluetoothControllerImpl bluetoothControllerImpl) {
            this.this$0 = bluetoothControllerImpl;
            this.mCallbacks = new ArrayList<>();
        }

        /* synthetic */ H(BluetoothControllerImpl bluetoothControllerImpl, H h) {
            this(bluetoothControllerImpl);
        }

        private void firePairedDevicesChanged() {
            for (BluetoothController.Callback callback : this.mCallbacks) {
                callback.onBluetoothDevicesChanged();
            }
        }

        private void fireStateChange() {
            for (BluetoothController.Callback callback : this.mCallbacks) {
                fireStateChange(callback);
            }
        }

        private void fireStateChange(BluetoothController.Callback callback) {
            callback.onBluetoothStateChange(this.this$0.mEnabled);
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    firePairedDevicesChanged();
                    return;
                case 2:
                    fireStateChange();
                    return;
                case 3:
                    this.mCallbacks.add((BluetoothController.Callback) message.obj);
                    return;
                case 4:
                    this.mCallbacks.remove((BluetoothController.Callback) message.obj);
                    return;
                default:
                    return;
            }
        }
    }

    public BluetoothControllerImpl(Context context, Looper looper) {
        this.mLocalBluetoothManager = LocalBluetoothManager.getInstance(context, null);
        if (this.mLocalBluetoothManager != null) {
            this.mLocalBluetoothManager.getEventManager().setReceiverHandler(new Handler(looper));
            this.mLocalBluetoothManager.getEventManager().registerCallback(this);
            onBluetoothStateChanged(this.mLocalBluetoothManager.getBluetoothAdapter().getBluetoothState());
        }
        this.mUserManager = (UserManager) context.getSystemService("user");
        this.mCurrentUser = ActivityManager.getCurrentUser();
    }

    private String getDeviceString(CachedBluetoothDevice cachedBluetoothDevice) {
        return cachedBluetoothDevice.getName() + " " + cachedBluetoothDevice.getBondState() + " " + cachedBluetoothDevice.isConnected();
    }

    private static String stateToString(int i) {
        switch (i) {
            case 0:
                return "DISCONNECTED";
            case 1:
                return "CONNECTING";
            case 2:
                return "CONNECTED";
            case 3:
                return "DISCONNECTING";
            default:
                return "UNKNOWN(" + i + ")";
        }
    }

    private void updateConnected() {
        int connectionState = this.mLocalBluetoothManager.getBluetoothAdapter().getConnectionState();
        if (connectionState != this.mConnectionState) {
            this.mConnectionState = connectionState;
            this.mHandler.sendEmptyMessage(2);
        }
        if (this.mLastDevice == null || !this.mLastDevice.isConnected()) {
            this.mLastDevice = null;
            for (CachedBluetoothDevice cachedBluetoothDevice : getDevices()) {
                if (cachedBluetoothDevice.isConnected()) {
                    this.mLastDevice = cachedBluetoothDevice;
                }
            }
            if (this.mLastDevice == null && this.mConnectionState == 2) {
                this.mConnectionState = 0;
                this.mHandler.sendEmptyMessage(2);
            }
        }
    }

    @Override // com.android.systemui.statusbar.policy.BluetoothController
    public void addStateChangedCallback(BluetoothController.Callback callback) {
        this.mHandler.obtainMessage(3, callback).sendToTarget();
        this.mHandler.sendEmptyMessage(2);
    }

    @Override // com.android.systemui.statusbar.policy.BluetoothController
    public boolean canConfigBluetooth() {
        return !this.mUserManager.hasUserRestriction("no_config_bluetooth", UserHandle.of(this.mCurrentUser));
    }

    @Override // com.android.systemui.statusbar.policy.BluetoothController
    public void connect(CachedBluetoothDevice cachedBluetoothDevice) {
        if (this.mLocalBluetoothManager == null || cachedBluetoothDevice == null) {
            return;
        }
        cachedBluetoothDevice.connect(true);
    }

    @Override // com.android.systemui.statusbar.policy.BluetoothController
    public void disconnect(CachedBluetoothDevice cachedBluetoothDevice) {
        if (this.mLocalBluetoothManager == null || cachedBluetoothDevice == null) {
            return;
        }
        cachedBluetoothDevice.disconnect();
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("BluetoothController state:");
        printWriter.print("  mLocalBluetoothManager=");
        printWriter.println(this.mLocalBluetoothManager);
        if (this.mLocalBluetoothManager == null) {
            return;
        }
        printWriter.print("  mEnabled=");
        printWriter.println(this.mEnabled);
        printWriter.print("  mConnectionState=");
        printWriter.println(stateToString(this.mConnectionState));
        printWriter.print("  mLastDevice=");
        printWriter.println(this.mLastDevice);
        printWriter.print("  mCallbacks.size=");
        printWriter.println(this.mHandler.mCallbacks.size());
        printWriter.println("  Bluetooth Devices:");
        Iterator<T> it = this.mLocalBluetoothManager.getCachedDeviceManager().getCachedDevicesCopy().iterator();
        while (it.hasNext()) {
            printWriter.println("    " + getDeviceString((CachedBluetoothDevice) it.next()));
        }
    }

    @Override // com.android.systemui.statusbar.policy.BluetoothController
    public Collection<CachedBluetoothDevice> getDevices() {
        Collection<CachedBluetoothDevice> collection = null;
        if (this.mLocalBluetoothManager != null) {
            collection = this.mLocalBluetoothManager.getCachedDeviceManager().getCachedDevicesCopy();
        }
        return collection;
    }

    @Override // com.android.systemui.statusbar.policy.BluetoothController
    public String getLastDeviceName() {
        String str = null;
        if (this.mLastDevice != null) {
            str = this.mLastDevice.getName();
        }
        return str;
    }

    @Override // com.android.systemui.statusbar.policy.BluetoothController
    public boolean isBluetoothConnected() {
        return this.mConnectionState == 2;
    }

    @Override // com.android.systemui.statusbar.policy.BluetoothController
    public boolean isBluetoothConnecting() {
        boolean z = true;
        if (this.mConnectionState != 1) {
            z = false;
        }
        return z;
    }

    @Override // com.android.systemui.statusbar.policy.BluetoothController
    public boolean isBluetoothEnabled() {
        return this.mEnabled;
    }

    @Override // com.android.systemui.statusbar.policy.BluetoothController
    public boolean isBluetoothSupported() {
        return this.mLocalBluetoothManager != null;
    }

    @Override // com.android.settingslib.bluetooth.BluetoothCallback
    public void onBluetoothStateChanged(int i) {
        this.mEnabled = i == 12;
        this.mHandler.sendEmptyMessage(2);
    }

    @Override // com.android.settingslib.bluetooth.BluetoothCallback
    public void onConnectionStateChanged(CachedBluetoothDevice cachedBluetoothDevice, int i) {
        this.mLastDevice = cachedBluetoothDevice;
        updateConnected();
        this.mConnectionState = i;
        this.mHandler.sendEmptyMessage(2);
    }

    @Override // com.android.settingslib.bluetooth.BluetoothCallback
    public void onDeviceAdded(CachedBluetoothDevice cachedBluetoothDevice) {
        cachedBluetoothDevice.registerCallback(this);
        updateConnected();
        this.mHandler.sendEmptyMessage(1);
    }

    @Override // com.android.settingslib.bluetooth.CachedBluetoothDevice.Callback
    public void onDeviceAttributesChanged() {
        updateConnected();
        this.mHandler.sendEmptyMessage(1);
    }

    @Override // com.android.settingslib.bluetooth.BluetoothCallback
    public void onDeviceBondStateChanged(CachedBluetoothDevice cachedBluetoothDevice, int i) {
        updateConnected();
        this.mHandler.sendEmptyMessage(1);
    }

    @Override // com.android.settingslib.bluetooth.BluetoothCallback
    public void onDeviceDeleted(CachedBluetoothDevice cachedBluetoothDevice) {
        updateConnected();
        this.mHandler.sendEmptyMessage(1);
    }

    @Override // com.android.settingslib.bluetooth.BluetoothCallback
    public void onScanningStateChanged(boolean z) {
    }

    @Override // com.android.systemui.statusbar.policy.BluetoothController
    public void removeStateChangedCallback(BluetoothController.Callback callback) {
        this.mHandler.obtainMessage(4, callback).sendToTarget();
    }

    @Override // com.android.systemui.statusbar.policy.BluetoothController
    public void setBluetoothEnabled(boolean z) {
        if (this.mLocalBluetoothManager != null) {
            this.mLocalBluetoothManager.getBluetoothAdapter().setBluetoothEnabled(z);
        }
    }
}
