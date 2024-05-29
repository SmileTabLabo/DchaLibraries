package com.android.browser;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import java.util.HashMap;
import java.util.Iterator;
/* loaded from: b.zip:com/android/browser/PreloadRequestReceiver.class */
public class PreloadRequestReceiver extends BroadcastReceiver {
    private ConnectivityManager mConnectivityManager;

    private void handlePreload(Context context, Intent intent) {
        String smartUrlFilter = UrlUtils.smartUrlFilter(intent.getData());
        String stringExtra = intent.getStringExtra("preload_id");
        if (stringExtra == null) {
            Log.d("browser.preloader", "Preload request has no preload_id");
        } else if (intent.getBooleanExtra("preload_discard", false)) {
            Log.d("browser.preloader", "Got " + stringExtra + " preload discard request");
            Preloader.getInstance().discardPreload(stringExtra);
        } else if (intent.getBooleanExtra("searchbox_cancel", false)) {
            Log.d("browser.preloader", "Got " + stringExtra + " searchbox cancel request");
            Preloader.getInstance().cancelSearchBoxPreload(stringExtra);
        } else {
            Log.d("browser.preloader", "Got " + stringExtra + " preload request for " + smartUrlFilter);
            HashMap hashMap = null;
            if (smartUrlFilter != null) {
                hashMap = null;
                if (smartUrlFilter.startsWith("http")) {
                    Bundle bundleExtra = intent.getBundleExtra("com.android.browser.headers");
                    hashMap = null;
                    if (bundleExtra != null) {
                        if (!bundleExtra.isEmpty()) {
                            Iterator<String> it = bundleExtra.keySet().iterator();
                            HashMap hashMap2 = new HashMap();
                            while (true) {
                                hashMap = hashMap2;
                                if (!it.hasNext()) {
                                    break;
                                }
                                String next = it.next();
                                hashMap2.put(next, bundleExtra.getString(next));
                            }
                        } else {
                            hashMap = null;
                        }
                    }
                }
            }
            String stringExtra2 = intent.getStringExtra("searchbox_query");
            if (smartUrlFilter != null) {
                Log.d("browser.preloader", "Preload request(" + stringExtra + ", " + smartUrlFilter + ", " + hashMap + ", " + stringExtra2 + ")");
                Preloader.getInstance().handlePreloadRequest(stringExtra, smartUrlFilter, hashMap, stringExtra2);
            }
        }
    }

    private boolean isOnWifi(Context context) {
        if (this.mConnectivityManager == null) {
            this.mConnectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        }
        NetworkInfo activeNetworkInfo = this.mConnectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo == null) {
            return false;
        }
        switch (activeNetworkInfo.getType()) {
            case 0:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                return false;
            case 1:
            case 7:
            case 9:
                return true;
            case 8:
            default:
                return false;
        }
    }

    private boolean isPreloadEnabledOnCurrentNetwork(Context context) {
        String preloadEnabled = BrowserSettings.getInstance().getPreloadEnabled();
        Log.d("browser.preloader", "Preload setting: " + preloadEnabled);
        if (BrowserSettings.getPreloadAlwaysPreferenceString(context).equals(preloadEnabled)) {
            return true;
        }
        if (BrowserSettings.getPreloadOnWifiOnlyPreferenceString(context).equals(preloadEnabled)) {
            boolean isOnWifi = isOnWifi(context);
            Log.d("browser.preloader", "on wifi:" + isOnWifi);
            return isOnWifi;
        }
        return false;
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        Log.d("browser.preloader", "received intent " + intent);
        if (isPreloadEnabledOnCurrentNetwork(context) && intent.getAction().equals("android.intent.action.PRELOAD")) {
            handlePreload(context, intent);
        }
    }
}
