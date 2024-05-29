package com.android.browser;

import android.app.Application;
import android.os.Build;
import android.os.SystemProperties;
import android.webkit.CookieSyncManager;
/* loaded from: b.zip:com/android/browser/Browser.class */
public class Browser extends Application {
    static final boolean DEBUG;

    static {
        DEBUG = Build.TYPE.equals("eng") ? true : SystemProperties.getBoolean("ro.debug.browser", false);
    }

    @Override // android.app.Application
    public void onCreate() {
        super.onCreate();
        CookieSyncManager.createInstance(this);
        BrowserSettings.initialize(getApplicationContext());
        Preloader.initialize(getApplicationContext());
        Extensions.getRegionalPhonePlugin(getApplicationContext()).updateBookmarks(getApplicationContext());
        BrowserSettings.getInstance().updateSearchEngineSetting();
    }
}
