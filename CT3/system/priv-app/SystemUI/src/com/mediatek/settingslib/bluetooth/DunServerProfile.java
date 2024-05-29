package com.mediatek.settingslib.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothDun;
import android.bluetooth.BluetoothUuid;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;
import com.android.settingslib.bluetooth.LocalBluetoothProfile;
/* loaded from: a.zip:com/mediatek/settingslib/bluetooth/DunServerProfile.class */
public final class DunServerProfile implements LocalBluetoothProfile {
    static final ParcelUuid[] DUN_CLIENT_UUIDS = {BluetoothUuid.DUN};
    private boolean mIsProfileReady;
    private BluetoothDun mService;

    /* loaded from: a.zip:com/mediatek/settingslib/bluetooth/DunServerProfile$DunServiceListener.class */
    private final class DunServiceListener implements BluetoothDun.ServiceListener {
        final DunServerProfile this$0;

        private DunServiceListener(DunServerProfile dunServerProfile) {
            this.this$0 = dunServerProfile;
        }

        /* synthetic */ DunServiceListener(DunServerProfile dunServerProfile, DunServiceListener dunServiceListener) {
            this(dunServerProfile);
        }

        public void onServiceConnected(BluetoothDun bluetoothDun) {
            Log.d("DunServerProfile", "Bluetooth Dun service connected");
            this.this$0.mService = bluetoothDun;
            this.this$0.mIsProfileReady = true;
        }

        public void onServiceDisconnected() {
            Log.d("DunServerProfile", "Bluetooth Dun service disconnected");
            this.this$0.mIsProfileReady = false;
        }
    }

    public DunServerProfile(Context context) {
        new BluetoothDun(context, new DunServiceListener(this, null));
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public boolean connect(BluetoothDevice bluetoothDevice) {
        return false;
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public boolean disconnect(BluetoothDevice bluetoothDevice) {
        if (this.mService == null) {
            return false;
        }
        return this.mService.disconnect(bluetoothDevice);
    }

    protected void finalize() {
        Log.d("DunServerProfile", "finalize()");
        if (this.mService != null) {
            try {
                this.mService.close();
                this.mService = null;
            } catch (Throwable th) {
                Log.w("DunServerProfile", "Error cleaning up PBAP proxy", th);
            }
        }
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public int getConnectionStatus(BluetoothDevice bluetoothDevice) {
        if (this.mService == null) {
            return 0;
        }
        return this.mService.getState(bluetoothDevice);
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public boolean isAutoConnectable() {
        return false;
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public boolean isConnectable() {
        return true;
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public boolean isPreferred(BluetoothDevice bluetoothDevice) {
        return false;
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public void setPreferred(BluetoothDevice bluetoothDevice, boolean z) {
    }

    public String toString() {
        return "DUN Server";
    }
}
