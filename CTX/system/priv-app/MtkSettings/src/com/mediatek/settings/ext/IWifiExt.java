package com.mediatek.settings.ext;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceScreen;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.settingslib.wifi.AccessPoint;
/* loaded from: classes.dex */
public interface IWifiExt {
    void addDisconnectButton(AlertDialog alertDialog, boolean z, NetworkInfo.DetailedState detailedState, WifiConfiguration wifiConfiguration);

    void addPreferenceController(Object obj, Object obj2);

    Object createWifiPreferenceController(Context context, Object obj);

    int getEapMethodbySpinnerPos(int i, String str, int i2);

    int getPosByEapMethod(int i, String str, int i2);

    int getPriority(int i);

    void hideWifiConfigInfo(Builder builder, Context context);

    void initConnectView(Activity activity, PreferenceScreen preferenceScreen);

    void initNetworkInfoView(PreferenceScreen preferenceScreen);

    void initPreference(ContentResolver contentResolver);

    void refreshNetworkInfoView();

    void setAPNetworkId(WifiConfiguration wifiConfiguration);

    void setAPPriority(int i);

    void setEapMethodArray(ArrayAdapter arrayAdapter, String str, int i);

    void setPriorityView(LinearLayout linearLayout, WifiConfiguration wifiConfiguration, boolean z);

    void setProxyText(TextView textView);

    void setSecurityText(TextView textView);

    void setSleepPolicyPreference(ListPreference listPreference, String[] strArr, String[] strArr2);

    /* loaded from: classes.dex */
    public static class Builder {
        private AccessPoint mAccessPoint;
        private boolean mEdit;
        private View mView;

        public Builder setAccessPoint(AccessPoint accessPoint) {
            this.mAccessPoint = accessPoint;
            return this;
        }

        public AccessPoint getAccessPoint() {
            return this.mAccessPoint;
        }

        public Builder setEdit(boolean z) {
            this.mEdit = z;
            return this;
        }

        public boolean getEdit() {
            return this.mEdit;
        }

        public Builder setViews(View view) {
            this.mView = view;
            return this;
        }

        public View getViews() {
            return this.mView;
        }
    }
}
