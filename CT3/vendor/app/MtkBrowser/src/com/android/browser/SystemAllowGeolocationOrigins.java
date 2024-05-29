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
/* loaded from: b.zip:com/android/browser/SystemAllowGeolocationOrigins.class */
public class SystemAllowGeolocationOrigins {
    private final Context mContext;
    private Runnable mMaybeApplySetting = new Runnable(this) { // from class: com.android.browser.SystemAllowGeolocationOrigins.1
        final SystemAllowGeolocationOrigins this$0;

        {
            this.this$0 = this;
        }

        @Override // java.lang.Runnable
        public void run() {
            String systemSetting = this.this$0.getSystemSetting();
            SharedPreferences preferences = BrowserSettings.getInstance().getPreferences();
            String string = preferences.getString("last_read_allow_geolocation_origins", "");
            if (TextUtils.equals(string, systemSetting)) {
                return;
            }
            preferences.edit().putString("last_read_allow_geolocation_origins", systemSetting).apply();
            HashSet parseAllowGeolocationOrigins = SystemAllowGeolocationOrigins.parseAllowGeolocationOrigins(string);
            HashSet parseAllowGeolocationOrigins2 = SystemAllowGeolocationOrigins.parseAllowGeolocationOrigins(systemSetting);
            Set minus = this.this$0.setMinus(parseAllowGeolocationOrigins2, parseAllowGeolocationOrigins);
            this.this$0.removeOrigins(this.this$0.setMinus(parseAllowGeolocationOrigins, parseAllowGeolocationOrigins2));
            this.this$0.addOrigins(minus);
        }
    };
    private final SettingObserver mSettingObserver = new SettingObserver(this);

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: b.zip:com/android/browser/SystemAllowGeolocationOrigins$SettingObserver.class */
    public class SettingObserver extends ContentObserver {
        final SystemAllowGeolocationOrigins this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        SettingObserver(SystemAllowGeolocationOrigins systemAllowGeolocationOrigins) {
            super(new Handler());
            this.this$0 = systemAllowGeolocationOrigins;
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z) {
            this.this$0.maybeApplySettingAsync();
        }
    }

    public SystemAllowGeolocationOrigins(Context context) {
        this.mContext = context.getApplicationContext();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void addOrigins(Set<String> set) {
        for (String str : set) {
            GeolocationPermissions.getInstance().allow(str);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String getSystemSetting() {
        String string = Settings.Secure.getString(this.mContext.getContentResolver(), "allowed_geolocation_origins");
        String str = string;
        if (string == null) {
            str = "";
        }
        return str;
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
    public void removeOrigins(Set<String> set) {
        for (String str : set) {
            GeolocationPermissions.getInstance().getAllowed(str, new ValueCallback<Boolean>(this, str) { // from class: com.android.browser.SystemAllowGeolocationOrigins.2
                final SystemAllowGeolocationOrigins this$0;
                final String val$origin;

                {
                    this.this$0 = this;
                    this.val$origin = str;
                }

                @Override // android.webkit.ValueCallback
                public void onReceiveValue(Boolean bool) {
                    if (bool == null || !bool.booleanValue()) {
                        return;
                    }
                    GeolocationPermissions.getInstance().clear(this.val$origin);
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public <A> Set<A> setMinus(Set<A> set, Set<A> set2) {
        HashSet hashSet = new HashSet(set.size());
        for (Object obj : set) {
            if (!set2.contains(obj)) {
                hashSet.add(obj);
            }
        }
        return hashSet;
    }

    void maybeApplySettingAsync() {
        BackgroundHandler.execute(this.mMaybeApplySetting);
    }

    public void start() {
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("allowed_geolocation_origins"), false, this.mSettingObserver);
        maybeApplySettingAsync();
    }

    public void stop() {
        this.mContext.getContentResolver().unregisterContentObserver(this.mSettingObserver);
    }
}
