package com.android.settingslib;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Process;
import android.os.UserHandle;
/* loaded from: classes.dex */
public class SliceBroadcastRelay {
    public static void registerReceiver(Context context, Uri uri, Class<? extends BroadcastReceiver> cls, IntentFilter intentFilter) {
        Intent intent = new Intent("com.android.settingslib.action.REGISTER_SLICE_RECEIVER");
        intent.setPackage("com.android.systemui");
        intent.putExtra("uri", ContentProvider.maybeAddUserId(uri, Process.myUserHandle().getIdentifier()));
        intent.putExtra("receiver", new ComponentName(context.getPackageName(), cls.getName()));
        intent.putExtra("filter", intentFilter);
        context.sendBroadcastAsUser(intent, UserHandle.SYSTEM);
    }

    public static void unregisterReceivers(Context context, Uri uri) {
        Intent intent = new Intent("com.android.settingslib.action.UNREGISTER_SLICE_RECEIVER");
        intent.setPackage("com.android.systemui");
        intent.putExtra("uri", ContentProvider.maybeAddUserId(uri, Process.myUserHandle().getIdentifier()));
        context.sendBroadcastAsUser(intent, UserHandle.SYSTEM);
    }
}
