package com.mediatek.apn;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
/* loaded from: classes.dex */
public class OmacpApnReceiver extends BroadcastReceiver {
    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d("OmacpApnReceiver", "get action = " + action);
        if (context.getContentResolver() == null) {
            Log.e("OmacpApnReceiver", "FAILURE unable to get content resolver..");
        } else if (!"com.mediatek.omacp.settings".equals(action)) {
        } else {
            startOmacpService(context, intent);
        }
    }

    private void startOmacpService(Context context, Intent broadcastIntent) {
        Intent i = new Intent(context, OmacpApnReceiverService.class);
        i.setAction("com.mediatek.apn.action.start.omacpservice");
        i.putExtra("android.intent.extra.INTENT", broadcastIntent);
        Log.d("OmacpApnReceiver", "startService");
        context.startService(i);
    }
}
