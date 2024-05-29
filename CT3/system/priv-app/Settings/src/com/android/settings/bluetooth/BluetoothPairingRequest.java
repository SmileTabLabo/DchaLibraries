package com.android.settings.bluetooth;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.PowerManager;
import android.text.TextUtils;
import com.android.settings.R;
/* loaded from: classes.dex */
public final class BluetoothPairingRequest extends BroadcastReceiver {
    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (!action.equals("android.bluetooth.device.action.PAIRING_REQUEST")) {
            if (action.equals("android.bluetooth.device.action.PAIRING_CANCEL")) {
                NotificationManager manager = (NotificationManager) context.getSystemService("notification");
                manager.cancel(17301632);
                return;
            } else if ("android.bluetooth.device.action.BOND_STATE_CHANGED".equals(action)) {
                int bondState = intent.getIntExtra("android.bluetooth.device.extra.BOND_STATE", Integer.MIN_VALUE);
                int oldState = intent.getIntExtra("android.bluetooth.device.extra.PREVIOUS_BOND_STATE", Integer.MIN_VALUE);
                if (oldState == 11 && bondState == 10) {
                    NotificationManager manager2 = (NotificationManager) context.getSystemService("notification");
                    manager2.cancel(17301632);
                    return;
                }
                return;
            } else {
                return;
            }
        }
        BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
        int type = intent.getIntExtra("android.bluetooth.device.extra.PAIRING_VARIANT", Integer.MIN_VALUE);
        Intent pairingIntent = new Intent();
        pairingIntent.setClass(context, BluetoothPairingDialog.class);
        pairingIntent.putExtra("android.bluetooth.device.extra.DEVICE", device);
        pairingIntent.putExtra("android.bluetooth.device.extra.PAIRING_VARIANT", type);
        if (type == 2 || type == 4 || type == 5) {
            int pairingKey = intent.getIntExtra("android.bluetooth.device.extra.PAIRING_KEY", Integer.MIN_VALUE);
            pairingIntent.putExtra("android.bluetooth.device.extra.PAIRING_KEY", pairingKey);
        }
        pairingIntent.setAction("android.bluetooth.device.action.PAIRING_REQUEST");
        pairingIntent.setFlags(268435456);
        PowerManager powerManager = (PowerManager) context.getSystemService("power");
        String address = device != null ? device.getAddress() : null;
        String deviceName = device != null ? device.getName() : null;
        boolean shouldShowDialog = LocalBluetoothPreferences.shouldShowDialogInForeground(context, address, deviceName);
        if (powerManager.isInteractive() && shouldShowDialog) {
            context.startActivity(pairingIntent);
            return;
        }
        Resources res = context.getResources();
        Notification.Builder builder = new Notification.Builder(context).setSmallIcon(17301632).setTicker(res.getString(R.string.bluetooth_notif_ticker));
        PendingIntent pending = PendingIntent.getActivity(context, 0, pairingIntent, 1073741824);
        String name = intent.getStringExtra("android.bluetooth.device.extra.NAME");
        if (TextUtils.isEmpty(name)) {
            name = device != null ? device.getAliasName() : context.getString(17039374);
        }
        builder.setContentTitle(res.getString(R.string.bluetooth_notif_title)).setContentText(res.getString(R.string.bluetooth_notif_message, name)).setContentIntent(pending).setAutoCancel(true).setDefaults(1).setColor(context.getColor(17170521));
        NotificationManager manager3 = (NotificationManager) context.getSystemService("notification");
        manager3.notify(17301632, builder.getNotification());
    }
}
