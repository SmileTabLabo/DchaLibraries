package com.android.browser;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
/* loaded from: b.zip:com/android/browser/OmacpSettingReceiver.class */
public class OmacpSettingReceiver extends BroadcastReceiver {
    private void sendCapabilityResult(Context context) {
        Intent intent = new Intent("com.mediatek.omacp.capability.result");
        intent.putExtra("appId", "w2");
        intent.putExtra("browser", true);
        intent.putExtra("browser_bookmark_folder", true);
        intent.putExtra("browser_to_proxy", false);
        intent.putExtra("browser_to_napid", false);
        intent.putExtra("browser_bookmark_name", true);
        intent.putExtra("browser_bookmark", true);
        intent.putExtra("browser_username", false);
        intent.putExtra("browser_password", false);
        intent.putExtra("browser_homepage", true);
        Log.i("@M_browser/OmacpSettingReceiver", "Capability Broadcasting: " + intent);
        context.sendBroadcast(intent);
    }

    private void sendSettingResult(Context context, boolean z) {
        Intent intent = new Intent("com.mediatek.omacp.settings.result");
        intent.putExtra("appId", "w2");
        intent.putExtra("result", z);
        Log.i("@M_browser/OmacpSettingReceiver", "Setting Broadcasting: " + intent);
        context.sendBroadcast(intent);
    }

    private boolean setBookmarkAndHomePage(Context context, Intent intent, long j) {
        String fixUrl;
        boolean z = false;
        if (-1 == j) {
            return false;
        }
        context.getContentResolver();
        ArrayList<HashMap> arrayList = (ArrayList) intent.getSerializableExtra("RESOURCE");
        if (arrayList == null) {
            Log.i("@M_browser/OmacpSettingReceiver", "resourceMapList is null");
        } else {
            Log.i("@M_browser/OmacpSettingReceiver", "resourceMapList size:" + arrayList.size());
            boolean z2 = false;
            for (HashMap hashMap : arrayList) {
                String str = (String) hashMap.get("URI");
                String str2 = (String) hashMap.get("NAME");
                String str3 = (String) hashMap.get("STARTPAGE");
                if (str != null && (fixUrl = UrlUtils.fixUrl(str)) != null) {
                    String str4 = str2;
                    if (str2 == null) {
                        str4 = fixUrl;
                    }
                    Bookmarks.addBookmark(context, false, fixUrl, str4, null, j);
                    boolean z3 = z2;
                    if (!z2) {
                        z3 = z2;
                        if (str3 != null) {
                            z3 = z2;
                            if (str3.equals("1")) {
                                setHomePage(context, fixUrl);
                                z3 = true;
                            }
                        }
                    }
                    Log.i("@M_browser/OmacpSettingReceiver", "BOOKMARK_URI: " + fixUrl);
                    Log.i("@M_browser/OmacpSettingReceiver", "BOOKMARK_NAME: " + str4);
                    Log.i("@M_browser/OmacpSettingReceiver", "STARTPAGE: " + str3);
                    z2 = z3;
                }
            }
            z = true;
        }
        return z;
    }

    private boolean setHomePage(Context context, String str) {
        if (str == null || str.length() <= 0) {
            return false;
        }
        String str2 = str;
        if (!str.startsWith("http:")) {
            str2 = "http://" + str;
        }
        SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(context).edit();
        edit.putString("homepage", str2);
        edit.commit();
        return true;
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        boolean bookmarkAndHomePage;
        Log.d("@M_browser/OmacpSettingReceiver", "OmacpSettingReceiver action:" + intent.getAction());
        context.getContentResolver();
        if ("com.mediatek.omacp.settings".equals(intent.getAction())) {
            String stringExtra = intent.getStringExtra("NAME");
            if (stringExtra == null) {
                bookmarkAndHomePage = setBookmarkAndHomePage(context, intent, 1L);
            } else {
                Log.i("@M_browser/OmacpSettingReceiver", "folderName isn't null");
                bookmarkAndHomePage = setBookmarkAndHomePage(context, intent, AddBookmarkPage.addFolderToRoot(context, stringExtra));
            }
            sendSettingResult(context, bookmarkAndHomePage);
        }
        if ("com.mediatek.omacp.capability".equals(intent.getAction())) {
            sendCapabilityResult(context);
        }
    }
}
