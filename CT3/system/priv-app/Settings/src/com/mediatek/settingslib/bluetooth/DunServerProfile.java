package com.mediatek.settingslib.bluetooth;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothDun;
import android.bluetooth.BluetoothUuid;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;
import com.android.settingslib.R$drawable;
import com.android.settingslib.R$string;
import com.android.settingslib.bluetooth.LocalBluetoothProfile;
/* loaded from: classes.dex */
public final class DunServerProfile implements LocalBluetoothProfile {
    static final ParcelUuid[] DUN_CLIENT_UUIDS = {BluetoothUuid.DUN};
    private boolean mIsProfileReady;
    private BluetoothDun mService;

    /* loaded from: classes.dex */
    private final class DunServiceListener implements BluetoothDun.ServiceListener {
        /* synthetic */ DunServiceListener(DunServerProfile this$0, DunServiceListener dunServiceListener) {
            this();
        }

        private DunServiceListener() {
        }

        public void onServiceConnected(BluetoothDun proxy) {
            Log.d("DunServerProfile", "Bluetooth Dun service connected");
            DunServerProfile.this.mService = proxy;
            DunServerProfile.this.mIsProfileReady = true;
        }

        public void onServiceDisconnected() {
            Log.d("DunServerProfile", "Bluetooth Dun service disconnected");
            DunServerProfile.this.mIsProfileReady = false;
        }
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public boolean isProfileReady() {
        return this.mIsProfileReady;
    }

    public DunServerProfile(Context context) {
        new BluetoothDun(context, new DunServiceListener(this, null));
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
        return this.mService.disconnect(device);
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public int getConnectionStatus(BluetoothDevice device) {
        if (this.mService == null) {
            return 0;
        }
        return this.mService.getState(device);
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
        return "DUN Server";
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public int getNameResource(BluetoothDevice device) {
        return R$string.bluetooth_profile_dun;
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfile
    public int getDrawableResource(BluetoothClass btClass) {
        return R$drawable.ic_bt_cellphone;
    }

    protected void finalize() {
        Log.d("DunServerProfile", "finalize()");
        if (this.mService == null) {
            return;
        }
        try {
            this.mService.close();
            this.mService = null;
        } catch (Throwable t) {
            Log.w("DunServerProfile", "Error cleaning up PBAP proxy", t);
        }
    }
}
