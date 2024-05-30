package com.android.settings.network;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.internal.util.ArrayUtils;
import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;
/* loaded from: classes.dex */
public class PrivateDnsPreferenceController extends BasePreferenceController implements PreferenceControllerMixin, LifecycleObserver, OnStart, OnStop {
    private static final String KEY_PRIVATE_DNS_SETTINGS = "private_dns_settings";
    private static final Uri[] SETTINGS_URIS = {Settings.Global.getUriFor("private_dns_mode"), Settings.Global.getUriFor("private_dns_default_mode"), Settings.Global.getUriFor("private_dns_specifier")};
    private final ConnectivityManager mConnectivityManager;
    private final Handler mHandler;
    private LinkProperties mLatestLinkProperties;
    private final ConnectivityManager.NetworkCallback mNetworkCallback;
    private Preference mPreference;
    private final ContentObserver mSettingsObserver;

    public PrivateDnsPreferenceController(Context context) {
        super(context, KEY_PRIVATE_DNS_SETTINGS);
        this.mNetworkCallback = new ConnectivityManager.NetworkCallback() { // from class: com.android.settings.network.PrivateDnsPreferenceController.1
            @Override // android.net.ConnectivityManager.NetworkCallback
            public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {
                PrivateDnsPreferenceController.this.mLatestLinkProperties = linkProperties;
                if (PrivateDnsPreferenceController.this.mPreference != null) {
                    PrivateDnsPreferenceController.this.updateState(PrivateDnsPreferenceController.this.mPreference);
                }
            }

            @Override // android.net.ConnectivityManager.NetworkCallback
            public void onLost(Network network) {
                PrivateDnsPreferenceController.this.mLatestLinkProperties = null;
                if (PrivateDnsPreferenceController.this.mPreference != null) {
                    PrivateDnsPreferenceController.this.updateState(PrivateDnsPreferenceController.this.mPreference);
                }
            }
        };
        this.mHandler = new Handler(Looper.getMainLooper());
        this.mSettingsObserver = new PrivateDnsSettingsObserver(this.mHandler);
        this.mConnectivityManager = (ConnectivityManager) context.getSystemService(ConnectivityManager.class);
    }

    @Override // com.android.settings.core.BasePreferenceController, com.android.settingslib.core.AbstractPreferenceController
    public String getPreferenceKey() {
        return KEY_PRIVATE_DNS_SETTINGS;
    }

    @Override // com.android.settings.core.BasePreferenceController
    public int getAvailabilityStatus() {
        return 0;
    }

    @Override // com.android.settings.core.BasePreferenceController, com.android.settingslib.core.AbstractPreferenceController
    public void displayPreference(PreferenceScreen preferenceScreen) {
        super.displayPreference(preferenceScreen);
        this.mPreference = preferenceScreen.findPreference(getPreferenceKey());
    }

    @Override // com.android.settingslib.core.lifecycle.events.OnStart
    public void onStart() {
        for (Uri uri : SETTINGS_URIS) {
            this.mContext.getContentResolver().registerContentObserver(uri, false, this.mSettingsObserver);
        }
        Network activeNetwork = this.mConnectivityManager.getActiveNetwork();
        if (activeNetwork != null) {
            this.mLatestLinkProperties = this.mConnectivityManager.getLinkProperties(activeNetwork);
        }
        this.mConnectivityManager.registerDefaultNetworkCallback(this.mNetworkCallback, this.mHandler);
    }

    @Override // com.android.settingslib.core.lifecycle.events.OnStop
    public void onStop() {
        this.mContext.getContentResolver().unregisterContentObserver(this.mSettingsObserver);
        this.mConnectivityManager.unregisterNetworkCallback(this.mNetworkCallback);
    }

    /* JADX WARN: Code restructure failed: missing block: B:20:0x004f, code lost:
        if (r2.equals("opportunistic") != false) goto L13;
     */
    @Override // com.android.settingslib.core.AbstractPreferenceController
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public CharSequence getSummary() {
        Resources resources = this.mContext.getResources();
        ContentResolver contentResolver = this.mContext.getContentResolver();
        String modeFromSettings = PrivateDnsModeDialogPreference.getModeFromSettings(contentResolver);
        LinkProperties linkProperties = this.mLatestLinkProperties;
        char c = 1;
        boolean z = !ArrayUtils.isEmpty(linkProperties == null ? null : linkProperties.getValidatedPrivateDnsServers());
        int hashCode = modeFromSettings.hashCode();
        if (hashCode != -539229175) {
            if (hashCode != -299803597) {
                if (hashCode == 109935 && modeFromSettings.equals("off")) {
                    c = 0;
                }
                c = 65535;
            } else {
                if (modeFromSettings.equals("hostname")) {
                    c = 2;
                }
                c = 65535;
            }
        }
        switch (c) {
            case 0:
                return resources.getString(R.string.private_dns_mode_off);
            case 1:
                return z ? resources.getString(R.string.switch_on_text) : resources.getString(R.string.private_dns_mode_opportunistic);
            case 2:
                if (z) {
                    return PrivateDnsModeDialogPreference.getHostnameFromSettings(contentResolver);
                }
                return resources.getString(R.string.private_dns_mode_provider_failure);
            default:
                return "";
        }
    }

    /* loaded from: classes.dex */
    private class PrivateDnsSettingsObserver extends ContentObserver {
        public PrivateDnsSettingsObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z) {
            if (PrivateDnsPreferenceController.this.mPreference != null) {
                PrivateDnsPreferenceController.this.updateState(PrivateDnsPreferenceController.this.mPreference);
            }
        }
    }
}
