package com.android.settingslib.wifi;

import android.app.AppGlobals;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.IWifiManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.TtsSpan;
import android.util.Log;
import android.util.LruCache;
import com.android.settingslib.R$array;
import com.android.settingslib.R$string;
import com.mediatek.settingslib.wifi.AccessPointExt;
import java.util.ArrayList;
import java.util.Map;
/* loaded from: classes.dex */
public class AccessPoint implements Comparable<AccessPoint>, Cloneable {
    private String bssid;
    public AccessPointExt mAccessPointExt;
    private AccessPointListener mAccessPointListener;
    private WifiConfiguration mConfig;
    private final Context mContext;
    private WifiInfo mInfo;
    private NetworkInfo mNetworkInfo;
    private int mRssi;
    public LruCache<String, ScanResult> mScanResultCache;
    private long mSeen;
    private Object mTag;
    private int networkId;
    private int pskType;
    private int security;
    private String ssid;

    /* loaded from: classes.dex */
    public interface AccessPointListener {
        void onAccessPointChanged(AccessPoint accessPoint);

        void onLevelChanged(AccessPoint accessPoint);
    }

    public AccessPoint(Context context, Bundle savedState) {
        this.mScanResultCache = new LruCache<>(32);
        this.networkId = -1;
        this.pskType = 0;
        this.mRssi = Integer.MAX_VALUE;
        this.mSeen = 0L;
        this.mContext = context;
        this.mAccessPointExt = new AccessPointExt(context);
        this.mConfig = (WifiConfiguration) savedState.getParcelable("key_config");
        if (this.mConfig != null) {
            loadConfig(this.mConfig);
        }
        if (savedState.containsKey("key_ssid")) {
            this.ssid = savedState.getString("key_ssid");
        }
        if (savedState.containsKey("key_security")) {
            this.security = savedState.getInt("key_security");
        }
        if (savedState.containsKey("key_psktype")) {
            this.pskType = savedState.getInt("key_psktype");
        }
        this.mInfo = (WifiInfo) savedState.getParcelable("key_wifiinfo");
        if (savedState.containsKey("key_networkinfo")) {
            this.mNetworkInfo = (NetworkInfo) savedState.getParcelable("key_networkinfo");
        }
        if (savedState.containsKey("key_scanresultcache")) {
            ArrayList<ScanResult> scanResultArrayList = savedState.getParcelableArrayList("key_scanresultcache");
            this.mScanResultCache.evictAll();
            for (ScanResult result : scanResultArrayList) {
                this.mScanResultCache.put(result.BSSID, result);
            }
        }
        update(this.mConfig, this.mInfo, this.mNetworkInfo);
        this.mRssi = getRssi();
        this.mSeen = getSeen();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public AccessPoint(Context context, ScanResult result) {
        this.mScanResultCache = new LruCache<>(32);
        this.networkId = -1;
        this.pskType = 0;
        this.mRssi = Integer.MAX_VALUE;
        this.mSeen = 0L;
        this.mContext = context;
        this.mAccessPointExt = new AccessPointExt(context);
        initWithScanResult(result);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public AccessPoint(Context context, WifiConfiguration config) {
        this.mScanResultCache = new LruCache<>(32);
        this.networkId = -1;
        this.pskType = 0;
        this.mRssi = Integer.MAX_VALUE;
        this.mSeen = 0L;
        this.mContext = context;
        this.mAccessPointExt = new AccessPointExt(context);
        loadConfig(config);
    }

    public Object clone() {
        try {
            AccessPoint object = (AccessPoint) super.clone();
            return object;
        } catch (CloneNotSupportedException e) {
            Log.e("SettingsLib.AccessPoint", "CloneNotSupportedException happens in clone()");
            return null;
        }
    }

    @Override // java.lang.Comparable
    public int compareTo(@NonNull AccessPoint other) {
        if (!isActive() || other.isActive()) {
            if (isActive() || !other.isActive()) {
                if (this.mRssi == Integer.MAX_VALUE || other.mRssi != Integer.MAX_VALUE) {
                    if (this.mRssi != Integer.MAX_VALUE || other.mRssi == Integer.MAX_VALUE) {
                        if (this.networkId == -1 || other.networkId != -1) {
                            if (this.networkId != -1 || other.networkId == -1) {
                                int difference = WifiManager.calculateSignalLevel(other.mRssi, 4) - WifiManager.calculateSignalLevel(this.mRssi, 4);
                                if (difference != 0) {
                                    return difference;
                                }
                                int securityDiff = other.security - this.security;
                                if (securityDiff != 0) {
                                    return securityDiff;
                                }
                                return this.ssid.compareToIgnoreCase(other.ssid);
                            }
                            return 1;
                        }
                        return -1;
                    }
                    return 1;
                }
                return -1;
            }
            return 1;
        }
        return -1;
    }

    public boolean equals(Object other) {
        return (other instanceof AccessPoint) && compareTo((AccessPoint) other) == 0;
    }

    public int hashCode() {
        int result = this.mInfo != null ? (this.mInfo.hashCode() * 13) + 0 : 0;
        return result + (this.mRssi * 19) + (this.networkId * 23) + (this.ssid.hashCode() * 29);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder().append("AccessPoint(").append(this.ssid);
        if (isSaved()) {
            builder.append(',').append("saved");
        }
        if (isActive()) {
            builder.append(',').append("active");
        }
        if (isEphemeral()) {
            builder.append(',').append("ephemeral");
        }
        if (isConnectable()) {
            builder.append(',').append("connectable");
        }
        if (this.security != 0) {
            builder.append(',').append(securityToString(this.security, this.pskType));
        }
        return builder.append(')').toString();
    }

    public boolean matches(ScanResult result) {
        return this.ssid.equals(result.SSID) && this.security == getSecurity(result);
    }

    public boolean matches(WifiConfiguration config) {
        if (config.isPasspoint() && this.mConfig != null && this.mConfig.isPasspoint()) {
            return config.FQDN.equals(this.mConfig.providerFriendlyName);
        }
        if (this.ssid.equals(removeDoubleQuotes(config.SSID)) && this.security == getSecurity(config)) {
            return this.mConfig == null || this.mConfig.shared == config.shared;
        }
        return false;
    }

    public WifiConfiguration getConfig() {
        return this.mConfig;
    }

    public void clearConfig() {
        this.mConfig = null;
        this.networkId = -1;
        this.mRssi = Integer.MAX_VALUE;
    }

    public WifiInfo getInfo() {
        return this.mInfo;
    }

    public int getLevel() {
        if (this.mRssi == Integer.MAX_VALUE) {
            return -1;
        }
        return WifiManager.calculateSignalLevel(this.mRssi, 4);
    }

    public int getRssi() {
        int rssi = Integer.MIN_VALUE;
        for (ScanResult result : this.mScanResultCache.snapshot().values()) {
            if (result.level > rssi) {
                rssi = result.level;
            }
        }
        return rssi;
    }

    public long getSeen() {
        long seen = 0;
        for (ScanResult result : this.mScanResultCache.snapshot().values()) {
            if (result.timestamp > seen) {
                seen = result.timestamp;
            }
        }
        return seen;
    }

    public NetworkInfo getNetworkInfo() {
        return this.mNetworkInfo;
    }

    public int getSecurity() {
        return this.security;
    }

    public String getSecurityString(boolean concise) {
        Context context = this.mContext;
        String securityString = this.mAccessPointExt.getSecurityString(this.security, context);
        if (securityString != null) {
            return securityString;
        }
        if (this.mConfig != null && this.mConfig.isPasspoint()) {
            return concise ? context.getString(R$string.wifi_security_short_eap) : context.getString(R$string.wifi_security_eap);
        }
        switch (this.security) {
            case 1:
                return concise ? context.getString(R$string.wifi_security_short_wep) : context.getString(R$string.wifi_security_wep);
            case 2:
                switch (this.pskType) {
                    case 1:
                        return concise ? context.getString(R$string.wifi_security_short_wpa) : context.getString(R$string.wifi_security_wpa);
                    case 2:
                        return concise ? context.getString(R$string.wifi_security_short_wpa2) : context.getString(R$string.wifi_security_wpa2);
                    case 3:
                        return concise ? context.getString(R$string.wifi_security_short_wpa_wpa2) : context.getString(R$string.wifi_security_wpa_wpa2);
                    default:
                        return concise ? context.getString(R$string.wifi_security_short_psk_generic) : context.getString(R$string.wifi_security_psk_generic);
                }
            case 3:
                return concise ? context.getString(R$string.wifi_security_short_eap) : context.getString(R$string.wifi_security_eap);
            default:
                return concise ? "" : context.getString(R$string.wifi_security_none);
        }
    }

    public String getSsidStr() {
        return this.ssid;
    }

    public String getBssid() {
        return this.bssid;
    }

    public CharSequence getSsid() {
        SpannableString str = new SpannableString(this.ssid);
        str.setSpan(new TtsSpan.VerbatimBuilder(this.ssid).build(), 0, this.ssid.length(), 18);
        return str;
    }

    public String getConfigName() {
        if (this.mConfig != null && this.mConfig.isPasspoint()) {
            return this.mConfig.providerFriendlyName;
        }
        return this.ssid;
    }

    public NetworkInfo.DetailedState getDetailedState() {
        if (this.mNetworkInfo != null) {
            return this.mNetworkInfo.getDetailedState();
        }
        return null;
    }

    public String getSavedNetworkSummary() {
        if (this.mConfig != null) {
            PackageManager pm = this.mContext.getPackageManager();
            String systemName = pm.getNameForUid(1000);
            int userId = UserHandle.getUserId(this.mConfig.creatorUid);
            ApplicationInfo appInfo = null;
            if (this.mConfig.creatorName != null && this.mConfig.creatorName.equals(systemName)) {
                appInfo = this.mContext.getApplicationInfo();
            } else {
                try {
                    IPackageManager ipm = AppGlobals.getPackageManager();
                    appInfo = ipm.getApplicationInfo(this.mConfig.creatorName, 0, userId);
                } catch (RemoteException e) {
                }
            }
            return (appInfo == null || appInfo.packageName.equals(this.mContext.getString(R$string.settings_package)) || appInfo.packageName.equals(this.mContext.getString(R$string.certinstaller_package))) ? "" : this.mContext.getString(R$string.saved_network, appInfo.loadLabel(pm));
        }
        return "";
    }

    public String getSettingsSummary() {
        StringBuilder summary = new StringBuilder();
        if (isActive() && this.mConfig != null && this.mConfig.isPasspoint()) {
            summary.append(getSummary(this.mContext, getDetailedState(), false, this.mConfig.providerFriendlyName));
        } else if (isActive()) {
            summary.append(getSummary(this.mContext, getDetailedState(), this.mInfo != null ? this.mInfo.isEphemeral() : false));
        } else if (this.mConfig != null && this.mConfig.isPasspoint()) {
            String format = this.mContext.getString(R$string.available_via_passpoint);
            summary.append(String.format(format, this.mConfig.providerFriendlyName));
        } else if (this.mConfig != null && this.mConfig.hasNoInternetAccess()) {
            summary.append(this.mContext.getString(R$string.wifi_no_internet));
        } else if (this.mConfig != null && !this.mConfig.getNetworkSelectionStatus().isNetworkEnabled()) {
            switch (this.mConfig.getNetworkSelectionStatus().getNetworkSelectionDisableReason()) {
                case 2:
                    summary.append(this.mContext.getString(R$string.wifi_disabled_generic));
                    break;
                case 3:
                    summary.append(this.mContext.getString(R$string.wifi_disabled_password_failure));
                    break;
                case 4:
                case 5:
                    summary.append(this.mContext.getString(R$string.wifi_disabled_network_failure));
                    break;
            }
        } else if (this.mRssi == Integer.MAX_VALUE) {
            summary.append(this.mContext.getString(R$string.wifi_not_in_range));
        } else if (this.mConfig != null) {
            summary.append(this.mContext.getString(R$string.wifi_remembered));
        }
        if (WifiTracker.sVerboseLogging > 0) {
            if (this.mInfo != null && this.mNetworkInfo != null) {
                summary.append(" f=").append(Integer.toString(this.mInfo.getFrequency()));
            }
            summary.append(" ").append(getVisibilityStatus());
            if (this.mConfig != null && !this.mConfig.getNetworkSelectionStatus().isNetworkEnabled()) {
                summary.append(" (").append(this.mConfig.getNetworkSelectionStatus().getNetworkStatusString());
                if (this.mConfig.getNetworkSelectionStatus().getDisableTime() > 0) {
                    long now = System.currentTimeMillis();
                    long diff = (now - this.mConfig.getNetworkSelectionStatus().getDisableTime()) / 1000;
                    long sec = diff % 60;
                    long min = (diff / 60) % 60;
                    long hour = (min / 60) % 60;
                    summary.append(", ");
                    if (hour > 0) {
                        summary.append(Long.toString(hour)).append("h ");
                    }
                    summary.append(Long.toString(min)).append("m ");
                    summary.append(Long.toString(sec)).append("s ");
                }
                summary.append(")");
            }
            if (this.mConfig != null) {
                WifiConfiguration.NetworkSelectionStatus networkStatus = this.mConfig.getNetworkSelectionStatus();
                for (int index = 0; index < 11; index++) {
                    if (networkStatus.getDisableReasonCounter(index) != 0) {
                        summary.append(" ").append(WifiConfiguration.NetworkSelectionStatus.getNetworkDisableReasonString(index)).append("=").append(networkStatus.getDisableReasonCounter(index));
                    }
                }
            }
        }
        return summary.toString();
    }

    private String getVisibilityStatus() {
        StringBuilder visibility = new StringBuilder();
        StringBuilder scans24GHz = null;
        StringBuilder scans5GHz = null;
        String bssid = null;
        System.currentTimeMillis();
        if (this.mInfo != null) {
            bssid = this.mInfo.getBSSID();
            if (bssid != null) {
                visibility.append(" ").append(bssid);
            }
            visibility.append(" rssi=").append(this.mInfo.getRssi());
            visibility.append(" ");
            visibility.append(" score=").append(this.mInfo.score);
            visibility.append(String.format(" tx=%.1f,", Double.valueOf(this.mInfo.txSuccessRate)));
            visibility.append(String.format("%.1f,", Double.valueOf(this.mInfo.txRetriesRate)));
            visibility.append(String.format("%.1f ", Double.valueOf(this.mInfo.txBadRate)));
            visibility.append(String.format("rx=%.1f", Double.valueOf(this.mInfo.rxSuccessRate)));
        }
        int rssi5 = WifiConfiguration.INVALID_RSSI;
        int rssi24 = WifiConfiguration.INVALID_RSSI;
        int num5 = 0;
        int num24 = 0;
        int n24 = 0;
        int n5 = 0;
        Map<String, ScanResult> list = this.mScanResultCache.snapshot();
        for (ScanResult result : list.values()) {
            if (result.frequency >= 4900 && result.frequency <= 5900) {
                num5++;
            } else if (result.frequency >= 2400 && result.frequency <= 2500) {
                num24++;
            }
            if (result.frequency >= 4900 && result.frequency <= 5900) {
                if (result.level > rssi5) {
                    rssi5 = result.level;
                }
                if (n5 < 4) {
                    if (scans5GHz == null) {
                        scans5GHz = new StringBuilder();
                    }
                    scans5GHz.append(" \n{").append(result.BSSID);
                    if (bssid != null && result.BSSID.equals(bssid)) {
                        scans5GHz.append("*");
                    }
                    scans5GHz.append("=").append(result.frequency);
                    scans5GHz.append(",").append(result.level);
                    scans5GHz.append("}");
                    n5++;
                }
            } else if (result.frequency >= 2400 && result.frequency <= 2500) {
                if (result.level > rssi24) {
                    rssi24 = result.level;
                }
                if (n24 < 4) {
                    if (scans24GHz == null) {
                        scans24GHz = new StringBuilder();
                    }
                    scans24GHz.append(" \n{").append(result.BSSID);
                    if (bssid != null && result.BSSID.equals(bssid)) {
                        scans24GHz.append("*");
                    }
                    scans24GHz.append("=").append(result.frequency);
                    scans24GHz.append(",").append(result.level);
                    scans24GHz.append("}");
                    n24++;
                }
            }
        }
        visibility.append(" [");
        if (num24 > 0) {
            visibility.append("(").append(num24).append(")");
            if (n24 <= 4) {
                if (scans24GHz != null) {
                    visibility.append(scans24GHz.toString());
                }
            } else {
                visibility.append("max=").append(rssi24);
                if (scans24GHz != null) {
                    visibility.append(",").append(scans24GHz.toString());
                }
            }
        }
        visibility.append(";");
        if (num5 > 0) {
            visibility.append("(").append(num5).append(")");
            if (n5 <= 4) {
                if (scans5GHz != null) {
                    visibility.append(scans5GHz.toString());
                }
            } else {
                visibility.append("max=").append(rssi5);
                if (scans5GHz != null) {
                    visibility.append(",").append(scans5GHz.toString());
                }
            }
        }
        visibility.append("]");
        return visibility.toString();
    }

    public boolean isActive() {
        if (this.mNetworkInfo != null) {
            return (this.networkId == -1 && this.mNetworkInfo.getState() == NetworkInfo.State.DISCONNECTED) ? false : true;
        }
        return false;
    }

    public boolean isConnectable() {
        return getLevel() != -1 && getDetailedState() == null;
    }

    public boolean isEphemeral() {
        return (this.mInfo == null || !this.mInfo.isEphemeral() || this.mNetworkInfo == null || this.mNetworkInfo.getState() == NetworkInfo.State.DISCONNECTED) ? false : true;
    }

    public boolean isPasspoint() {
        if (this.mConfig != null) {
            return this.mConfig.isPasspoint();
        }
        return false;
    }

    private boolean isInfoForThisAccessPoint(WifiConfiguration config, WifiInfo info) {
        if (!isPasspoint() && this.networkId != -1) {
            return this.networkId == info.getNetworkId();
        } else if (config != null) {
            return matches(config);
        } else {
            return this.ssid.equals(removeDoubleQuotes(info.getSSID()));
        }
    }

    public boolean isSaved() {
        return this.networkId != -1;
    }

    public Object getTag() {
        return this.mTag;
    }

    public void setTag(Object tag) {
        this.mTag = tag;
    }

    public void generateOpenNetworkConfig() {
        if (this.security != 0) {
            throw new IllegalStateException();
        }
        if (this.mConfig != null) {
            return;
        }
        this.mConfig = new WifiConfiguration();
        this.mConfig.SSID = convertToQuotedString(this.ssid);
        this.mConfig.allowedKeyManagement.set(0);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void loadConfig(WifiConfiguration config) {
        if (config.isPasspoint()) {
            this.ssid = config.providerFriendlyName;
        } else {
            this.ssid = config.SSID == null ? "" : removeDoubleQuotes(config.SSID);
        }
        this.bssid = config.BSSID;
        this.security = getSecurity(config);
        this.networkId = config.networkId;
        this.mConfig = config;
    }

    private void initWithScanResult(ScanResult result) {
        this.ssid = result.SSID;
        this.bssid = result.BSSID;
        this.security = getSecurity(result);
        if (this.security == 2) {
            this.pskType = getPskType(result);
        }
        this.mRssi = result.level;
        this.mSeen = result.timestamp;
    }

    public void saveWifiState(Bundle savedState) {
        if (this.ssid != null) {
            savedState.putString("key_ssid", getSsidStr());
        }
        savedState.putInt("key_security", this.security);
        savedState.putInt("key_psktype", this.pskType);
        if (this.mConfig != null) {
            savedState.putParcelable("key_config", this.mConfig);
        }
        savedState.putParcelable("key_wifiinfo", this.mInfo);
        savedState.putParcelableArrayList("key_scanresultcache", new ArrayList<>(this.mScanResultCache.snapshot().values()));
        if (this.mNetworkInfo == null) {
            return;
        }
        savedState.putParcelable("key_networkinfo", this.mNetworkInfo);
    }

    public void setListener(AccessPointListener listener) {
        this.mAccessPointListener = listener;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean update(ScanResult result) {
        if (matches(result)) {
            this.mScanResultCache.get(result.BSSID);
            this.mScanResultCache.put(result.BSSID, result);
            int oldLevel = getLevel();
            int oldRssi = getRssi();
            this.mSeen = getSeen();
            this.mRssi = (getRssi() + oldRssi) / 2;
            int newLevel = getLevel();
            if (newLevel > 0 && newLevel != oldLevel && this.mAccessPointListener != null) {
                this.mAccessPointListener.onLevelChanged(this);
            }
            if (this.security == 2) {
                this.pskType = getPskType(result);
            }
            if (this.mAccessPointListener != null) {
                this.mAccessPointListener.onAccessPointChanged(this);
                return true;
            }
            return true;
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean update(WifiConfiguration config, WifiInfo info, NetworkInfo networkInfo) {
        boolean reorder = false;
        if (info != null && isInfoForThisAccessPoint(config, info)) {
            reorder = this.mInfo == null;
            this.mRssi = info.getRssi();
            this.mInfo = info;
            this.mNetworkInfo = networkInfo;
            if (this.mAccessPointListener != null) {
                this.mAccessPointListener.onAccessPointChanged(this);
            }
        } else if (this.mInfo != null) {
            reorder = true;
            this.mInfo = null;
            this.mNetworkInfo = null;
            if (this.mAccessPointListener != null) {
                this.mAccessPointListener.onAccessPointChanged(this);
            }
        }
        return reorder;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void update(WifiConfiguration config) {
        this.mConfig = config;
        this.networkId = config.networkId;
        if (this.mAccessPointListener == null) {
            return;
        }
        this.mAccessPointListener.onAccessPointChanged(this);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setRssi(int rssi) {
        this.mRssi = rssi;
    }

    public static String getSummary(Context context, String ssid, NetworkInfo.DetailedState state, boolean isEphemeral, String passpointProvider) {
        Network network;
        if (state == NetworkInfo.DetailedState.CONNECTED && ssid == null) {
            if (!TextUtils.isEmpty(passpointProvider)) {
                String format = context.getString(R$string.connected_via_passpoint);
                return String.format(format, passpointProvider);
            } else if (isEphemeral) {
                return context.getString(R$string.connected_via_wfa);
            }
        }
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService("connectivity");
        if (state == NetworkInfo.DetailedState.CONNECTED) {
            IWifiManager wifiManager = IWifiManager.Stub.asInterface(ServiceManager.getService("wifi"));
            try {
                network = wifiManager.getCurrentNetwork();
            } catch (RemoteException e) {
                network = null;
            }
            NetworkCapabilities nc = cm.getNetworkCapabilities(network);
            if (nc != null && !nc.hasCapability(16)) {
                return context.getString(R$string.wifi_connected_no_internet);
            }
        }
        String[] formats = context.getResources().getStringArray(ssid == null ? R$array.wifi_status : R$array.wifi_status_with_ssid);
        int index = state.ordinal();
        return (index >= formats.length || formats[index].length() == 0) ? "" : String.format(formats[index], ssid);
    }

    public static String getSummary(Context context, NetworkInfo.DetailedState state, boolean isEphemeral) {
        return getSummary(context, null, state, isEphemeral, null);
    }

    public static String getSummary(Context context, NetworkInfo.DetailedState state, boolean isEphemeral, String passpointProvider) {
        return getSummary(context, null, state, isEphemeral, passpointProvider);
    }

    public static String convertToQuotedString(String string) {
        return "\"" + string + "\"";
    }

    private static int getPskType(ScanResult result) {
        boolean wpa = result.capabilities.contains("WPA-PSK");
        boolean wpa2 = result.capabilities.contains("WPA2-PSK");
        if (wpa2 && wpa) {
            return 3;
        }
        if (wpa2) {
            return 2;
        }
        if (wpa) {
            return 1;
        }
        Log.w("SettingsLib.AccessPoint", "Received abnormal flag string: " + result.capabilities);
        return 0;
    }

    private static int getSecurity(ScanResult result) {
        int security = AccessPointExt.getSecurity(result);
        if (security != -1) {
            return security;
        }
        if (result.capabilities.contains("WEP")) {
            return 1;
        }
        if (result.capabilities.contains("PSK")) {
            return 2;
        }
        if (result.capabilities.contains("EAP")) {
            return 3;
        }
        return 0;
    }

    static int getSecurity(WifiConfiguration config) {
        int security = AccessPointExt.getSecurity(config);
        if (security != -1) {
            return security;
        }
        if (config.allowedKeyManagement.get(1)) {
            return 2;
        }
        if (config.allowedKeyManagement.get(2) || config.allowedKeyManagement.get(3)) {
            return 3;
        }
        return config.wepKeys[0] != null ? 1 : 0;
    }

    public static String securityToString(int security, int pskType) {
        if (security == 1) {
            return "WEP";
        }
        if (security == 2) {
            if (pskType == 1) {
                return "WPA";
            }
            if (pskType == 2) {
                return "WPA2";
            }
            if (pskType == 3) {
                return "WPA_WPA2";
            }
            return "PSK";
        } else if (security == 3) {
            return "EAP";
        } else {
            return "NONE";
        }
    }

    static String removeDoubleQuotes(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        int length = string.length();
        if (length > 1 && string.charAt(0) == '\"' && string.charAt(length - 1) == '\"') {
            return string.substring(1, length - 1);
        }
        return string;
    }
}
