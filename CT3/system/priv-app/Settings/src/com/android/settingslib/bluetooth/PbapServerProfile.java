package com.android.settingslib.bluetooth;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothPbap;
import android.bluetooth.BluetoothUuid;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;
import com.android.settingslib.R$drawable;
import com.android.settingslib.R$string;
/* loaded from: classes.dex */
public final class PbapServerProfile implements LocalBluetoothProfile {
    private boolean mIsProfileReady;
    private BluetoothPbap mService;
    private static boolean V = true;
    static final ParcelUuid[] PBAB_CLIENT_UUIDS = {BluetoothUuid.HSP, BluetoothUuid.Handsfree, BluetoothUuid.PBAP_PCE};

    /* loaded from: classes.dex */
    private final class PbapServiceListener implements BluetoothPbap.ServiceListener {
        /* synthetic */ PbapServiceListener(PbapServerProfile this$0, PbapServiceListener pbapServiceListener) {
            this();
        }

        private PbapServiceListener() {
        }

        public void onServiceConnected(BluetoothPbap proxy) {
            if (PbapServerProfile.V) {
                Log.d("PbapServerProfile", "Bluetooth service connected");
            }
            PbapServerProfile.this.mService = proxy;
            PbapServerProfile.this.mIsProfileReady = true;
        }

        public void onServiceDisconnected() {
            if (PbapServerProfile.V) {
                Log.d("PbapServerProfile", "Bluetooth service disconnected");
            }
            PbapServerProfile.this.mIsProfileReady = false;
        }
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public boolean isProfileReady() {
        return this.mIsProfileReady;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public PbapServerProfile(Context context) {
        new BluetoothPbap(context, new PbapServiceListener(this, null));
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public boolean isConnectable() {
        return true;
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public boolean isAutoConnectable() {
        return false;
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public boolean connect(BluetoothDevice device) {
        return false;
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public boolean disconnect(BluetoothDevice device) {
        if (this.mService == null) {
            return false;
        }
        return this.mService.disconnect();
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public int getConnectionStatus(BluetoothDevice device) {
        return (this.mService != null && this.mService.isConnected(device)) ? 2 : 0;
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public boolean isPreferred(BluetoothDevice device) {
        return false;
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public int getPreferred(BluetoothDevice device) {
        return -1;
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public void setPreferred(BluetoothDevice device, boolean preferred) {
    }

    public String toString() {
        return "PBAP Server";
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public int getNameResource(BluetoothDevice device) {
        return R$string.bluetooth_profile_pbap;
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public int getDrawableResource(BluetoothClass btClass) {
        return R$drawable.ic_bt_cellphone;
    }

    protected void finalize() {
        if (V) {
            Log.d("PbapServerProfile", "finalize()");
        }
        if (this.mService == null) {
            return;
        }
        try {
            this.mService.close();
            this.mService = null;
        } catch (Throwable t) {
            Log.w("PbapServerProfile", "Error cleaning up PBAP proxy", t);
        }
    }
}
