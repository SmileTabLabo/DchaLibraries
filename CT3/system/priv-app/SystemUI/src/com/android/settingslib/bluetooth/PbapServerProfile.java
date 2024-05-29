package com.android.settingslib.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothPbap;
import android.bluetooth.BluetoothUuid;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;
/* loaded from: a.zip:com/android/settingslib/bluetooth/PbapServerProfile.class */
public final class PbapServerProfile implements LocalBluetoothProfile {
    private boolean mIsProfileReady;
    private BluetoothPbap mService;
    private static boolean V = true;
    static final ParcelUuid[] PBAB_CLIENT_UUIDS = {BluetoothUuid.HSP, BluetoothUuid.Handsfree, BluetoothUuid.PBAP_PCE};

    /* loaded from: a.zip:com/android/settingslib/bluetooth/PbapServerProfile$PbapServiceListener.class */
    private final class PbapServiceListener implements BluetoothPbap.ServiceListener {
        final PbapServerProfile this$0;

        private PbapServiceListener(PbapServerProfile pbapServerProfile) {
            this.this$0 = pbapServerProfile;
        }

        /* synthetic */ PbapServiceListener(PbapServerProfile pbapServerProfile, PbapServiceListener pbapServiceListener) {
            this(pbapServerProfile);
        }

        public void onServiceConnected(BluetoothPbap bluetoothPbap) {
            if (PbapServerProfile.V) {
                Log.d("PbapServerProfile", "Bluetooth service connected");
            }
            this.this$0.mService = bluetoothPbap;
            this.this$0.mIsProfileReady = true;
        }

        public void onServiceDisconnected() {
            if (PbapServerProfile.V) {
                Log.d("PbapServerProfile", "Bluetooth service disconnected");
            }
            this.this$0.mIsProfileReady = false;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public PbapServerProfile(Context context) {
        new BluetoothPbap(context, new PbapServiceListener(this, null));
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
        return this.mService.disconnect();
    }

    protected void finalize() {
        if (V) {
            Log.d("PbapServerProfile", "finalize()");
        }
        if (this.mService != null) {
            try {
                this.mService.close();
                this.mService = null;
            } catch (Throwable th) {
                Log.w("PbapServerProfile", "Error cleaning up PBAP proxy", th);
            }
        }
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public int getConnectionStatus(BluetoothDevice bluetoothDevice) {
        return (this.mService != null && this.mService.isConnected(bluetoothDevice)) ? 2 : 0;
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
        return "PBAP Server";
    }
}
