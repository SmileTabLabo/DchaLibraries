package com.mediatek.settings.ext;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceScreen;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.mediatek.settings.ext.IWifiExt;
/* loaded from: classes.dex */
public class DefaultWifiExt implements IWifiExt {
    private static final String TAG = "DefaultWifiExt";
    private Context mContext;

    public DefaultWifiExt(Context context) {
        this.mContext = context;
    }

    @Override // com.mediatek.settings.ext.IWifiExt
    public void setAPNetworkId(WifiConfiguration wifiConfig) {
    }

    @Override // com.mediatek.settings.ext.IWifiExt
    public void setAPPriority(int apPriority) {
    }

    @Override // com.mediatek.settings.ext.IWifiExt
    public void setPriorityView(LinearLayout priorityLayout, WifiConfiguration wifiConfig, boolean isEdit) {
    }

    @Override // com.mediatek.settings.ext.IWifiExt
    public void setSecurityText(TextView view) {
    }

    @Override // com.mediatek.settings.ext.IWifiExt
    public void addDisconnectButton(AlertDialog dialog, boolean edit, NetworkInfo.DetailedState state, WifiConfiguration wifiConfig) {
    }

    @Override // com.mediatek.settings.ext.IWifiExt
    public int getPriority(int priority) {
        return priority;
    }

    @Override // com.mediatek.settings.ext.IWifiExt
    public void setProxyText(TextView view) {
    }

    @Override // com.mediatek.settings.ext.IWifiExt
    public void initConnectView(Activity activity, PreferenceScreen screen) {
    }

    @Override // com.mediatek.settings.ext.IWifiExt
    public void initNetworkInfoView(PreferenceScreen screen) {
    }

    @Override // com.mediatek.settings.ext.IWifiExt
    public void refreshNetworkInfoView() {
    }

    @Override // com.mediatek.settings.ext.IWifiExt
    public void initPreference(ContentResolver contentResolver) {
    }

    @Override // com.mediatek.settings.ext.IWifiExt
    public void setSleepPolicyPreference(ListPreference sleepPolicyPref, String[] sleepPolicyEntries, String[] sleepPolicyValues) {
    }

    @Override // com.mediatek.settings.ext.IWifiExt
    public void hideWifiConfigInfo(IWifiExt.Builder builder, Context context) {
    }

    @Override // com.mediatek.settings.ext.IWifiExt
    public void setEapMethodArray(ArrayAdapter adapter, String ssid, int security) {
    }

    @Override // com.mediatek.settings.ext.IWifiExt
    public int getEapMethodbySpinnerPos(int spinnerPos, String ssid, int security) {
        return spinnerPos;
    }

    @Override // com.mediatek.settings.ext.IWifiExt
    public int getPosByEapMethod(int spinnerPos, String ssid, int security) {
        return spinnerPos;
    }
}
