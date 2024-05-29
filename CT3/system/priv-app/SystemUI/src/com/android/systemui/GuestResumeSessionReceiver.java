package com.android.systemui;

import android.app.ActivityManagerNative;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.UserInfo;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.util.Log;
import android.view.WindowManagerGlobal;
import com.android.systemui.statusbar.phone.SystemUIDialog;
/* loaded from: a.zip:com/android/systemui/GuestResumeSessionReceiver.class */
public class GuestResumeSessionReceiver extends BroadcastReceiver {
    private Dialog mNewSessionDialog;

    /* loaded from: a.zip:com/android/systemui/GuestResumeSessionReceiver$ResetSessionDialog.class */
    private static class ResetSessionDialog extends SystemUIDialog implements DialogInterface.OnClickListener {
        private final int mUserId;

        public ResetSessionDialog(Context context, int i) {
            super(context);
            setTitle(context.getString(2131493634));
            setMessage(context.getString(2131493635));
            setCanceledOnTouchOutside(false);
            setButton(-2, context.getString(2131493636), this);
            setButton(-1, context.getString(2131493637), this);
            this.mUserId = i;
        }

        @Override // android.content.DialogInterface.OnClickListener
        public void onClick(DialogInterface dialogInterface, int i) {
            if (i == -2) {
                GuestResumeSessionReceiver.wipeGuestSession(getContext(), this.mUserId);
                dismiss();
            } else if (i == -1) {
                cancel();
            }
        }
    }

    private void cancelDialog() {
        if (this.mNewSessionDialog == null || !this.mNewSessionDialog.isShowing()) {
            return;
        }
        this.mNewSessionDialog.cancel();
        this.mNewSessionDialog = null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void wipeGuestSession(Context context, int i) {
        UserManager userManager = (UserManager) context.getSystemService("user");
        try {
            UserInfo currentUser = ActivityManagerNative.getDefault().getCurrentUser();
            if (currentUser.id != i) {
                Log.w("GuestResumeSessionReceiver", "User requesting to start a new session (" + i + ") is not current user (" + currentUser.id + ")");
            } else if (!currentUser.isGuest()) {
                Log.w("GuestResumeSessionReceiver", "User requesting to start a new session (" + i + ") is not a guest");
            } else if (!userManager.markGuestForDeletion(currentUser.id)) {
                Log.w("GuestResumeSessionReceiver", "Couldn't mark the guest for deletion for user " + i);
            } else {
                UserInfo createGuest = userManager.createGuest(context, currentUser.name);
                try {
                    if (createGuest != null) {
                        ActivityManagerNative.getDefault().switchUser(createGuest.id);
                        userManager.removeUser(currentUser.id);
                        return;
                    }
                    Log.e("GuestResumeSessionReceiver", "Could not create new guest, switching back to system user");
                    ActivityManagerNative.getDefault().switchUser(0);
                    userManager.removeUser(currentUser.id);
                    WindowManagerGlobal.getWindowManagerService().lockNow((Bundle) null);
                } catch (RemoteException e) {
                    Log.e("GuestResumeSessionReceiver", "Couldn't wipe session because ActivityManager or WindowManager is dead");
                }
            }
        } catch (RemoteException e2) {
            Log.e("GuestResumeSessionReceiver", "Couldn't wipe session because ActivityManager is dead");
        }
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.USER_SWITCHED".equals(intent.getAction())) {
            cancelDialog();
            int intExtra = intent.getIntExtra("android.intent.extra.user_handle", -10000);
            if (intExtra == -10000) {
                Log.e("GuestResumeSessionReceiver", intent + " sent to GuestResumeSessionReceiver without EXTRA_USER_HANDLE");
                return;
            }
            try {
                if (ActivityManagerNative.getDefault().getCurrentUser().isGuest()) {
                    ContentResolver contentResolver = context.getContentResolver();
                    if (Settings.System.getIntForUser(contentResolver, "systemui.guest_has_logged_in", 0, intExtra) == 0) {
                        Settings.System.putIntForUser(contentResolver, "systemui.guest_has_logged_in", 1, intExtra);
                        return;
                    }
                    this.mNewSessionDialog = new ResetSessionDialog(context, intExtra);
                    this.mNewSessionDialog.show();
                }
            } catch (RemoteException e) {
            }
        }
    }

    public void register(Context context) {
        context.registerReceiverAsUser(this, UserHandle.SYSTEM, new IntentFilter("android.intent.action.USER_SWITCHED"), null, null);
    }
}
