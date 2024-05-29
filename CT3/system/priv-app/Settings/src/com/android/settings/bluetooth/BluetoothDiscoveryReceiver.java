package com.android.settings.bluetooth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
/* loaded from: classes.dex */
public final class BluetoothDiscoveryReceiver extends BroadcastReceiver {
    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.v("BluetoothDiscoveryReceiver", "Received: " + action);
        if (!action.equals("android.bluetooth.adapter.action.DISCOVERY_STARTED") && !action.equals("android.bluetooth.adapter.action.DISCOVERY_FINISHED")) {
            return;
        }
        LocalBluetoothPreferences.persistDiscoveringTimestamp(context);
    }
}
