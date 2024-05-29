package com.android.settings.users;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;
import com.android.settings.Utils;
/* loaded from: classes.dex */
public class ProfileUpdateReceiver extends BroadcastReceiver {
    /* JADX WARN: Type inference failed for: r3v2, types: [com.android.settings.users.ProfileUpdateReceiver$1] */
    @Override // android.content.BroadcastReceiver
    public void onReceive(final Context context, Intent intent) {
        Log.d("ProfileUpdateReceiver", "Profile photo changed, get the PROFILE_CHANGED receiver.");
        new Thread() { // from class: com.android.settings.users.ProfileUpdateReceiver.1
            @Override // java.lang.Thread, java.lang.Runnable
            public void run() {
                UserSettings.copyMeProfilePhoto(context, null);
                String str = SystemProperties.get("ro.com.google.gmsversion", (String) null);
                if (str != null && !str.isEmpty()) {
                    ProfileUpdateReceiver.copyProfileName(context);
                }
            }
        }.start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void copyProfileName(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("profile", 0);
        if (sharedPreferences.contains("name_copied_once")) {
            return;
        }
        int myUserId = UserHandle.myUserId();
        UserManager userManager = (UserManager) context.getSystemService("user");
        String meProfileName = Utils.getMeProfileName(context, false);
        if (meProfileName != null && meProfileName.length() > 0) {
            userManager.setUserName(myUserId, meProfileName);
            sharedPreferences.edit().putBoolean("name_copied_once", true).commit();
        }
    }
}
