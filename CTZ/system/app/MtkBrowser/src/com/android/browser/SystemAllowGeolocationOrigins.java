package com.android.browser;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.webkit.GeolocationPermissions;
import android.webkit.ValueCallback;
import java.util.HashSet;
import java.util.Set;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class SystemAllowGeolocationOrigins {
    private final Context mContext;
    private Runnable mMaybeApplySetting = new Runnable() { // from class: com.android.browser.SystemAllowGeolocationOrigins.1
        @Override // java.lang.Runnable
        public void run() {
            String systemSetting = SystemAllowGeolocationOrigins.this.getSystemSetting();
            SharedPreferences preferences = BrowserSettings.getInstance().getPreferences();
            String string = preferences.getString("last_read_allow_geolocation_origins", "");
            if (TextUtils.equals(string, systemSetting)) {
                return;
            }
            preferences.edit().putString("last_read_allow_geolocation_origins", systemSetting).apply();
            HashSet parseAllowGeolocationOrigins = SystemAllowGeolocationOrigins.parseAllowGeolocationOrigins(string);
            HashSet parseAllowGeolocationOrigins2 = SystemAllowGeolocationOrigins.parseAllowGeolocationOrigins(systemSetting);
            Set minus = SystemAllowGeolocationOrigins.this.setMinus(parseAllowGeolocationOrigins2, parseAllowGeolocationOrigins);
            SystemAllowGeolocationOrigins.this.removeOrigins(SystemAllowGeolocationOrigins.this.setMinus(parseAllowGeolocationOrigins, parseAllowGeolocationOrigins2));
            SystemAllowGeolocationOrigins.this.addOrigins(minus);
        }
    };
    private final SettingObserver mSettingObserver = new SettingObserver();

    public SystemAllowGeolocationOrigins(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public void start() {
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("allowed_geolocation_origins"), false, this.mSettingObserver);
        maybeApplySettingAsync();
    }

    public void stop() {
        this.mContext.getContentResolver().unregisterContentObserver(this.mSettingObserver);
    }

    void maybeApplySettingAsync() {
        BackgroundHandler.execute(this.mMaybeApplySetting);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static HashSet<String> parseAllowGeolocationOrigins(String str) {
        String[] split;
        HashSet<String> hashSet = new HashSet<>();
        if (!TextUtils.isEmpty(str)) {
            for (String str2 : str.split("\\s+")) {
                if (!TextUtils.isEmpty(str2)) {
                    hashSet.add(str2);
                }
            }
        }
        return hashSet;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public <A> Set<A> setMinus(Set<A> set, Set<A> set2) {
        HashSet hashSet = new HashSet(set.size());
        for (A a : set) {
            if (!set2.contains(a)) {
                hashSet.add(a);
            }
        }
        return hashSet;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String getSystemSetting() {
        String string = Settings.Secure.getString(this.mContext.getContentResolver(), "allowed_geolocation_origins");
        return string == null ? "" : string;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void addOrigins(Set<String> set) {
        for (String str : set) {
            GeolocationPermissions.getInstance().allow(str);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void removeOrigins(Set<String> set) {
        for (final String str : set) {
            GeolocationPermissions.getInstance().getAllowed(str, new ValueCallback<Boolean>() { // from class: com.android.browser.SystemAllowGeolocationOrigins.2
                @Override // android.webkit.ValueCallback
                public void onReceiveValue(Boolean bool) {
                    if (bool != null && bool.booleanValue()) {
                        GeolocationPermissions.getInstance().clear(str);
                    }
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class SettingObserver extends ContentObserver {
        SettingObserver() {
            super(new Handler());
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z) {
            SystemAllowGeolocationOrigins.this.maybeApplySettingAsync();
        }
    }
}
