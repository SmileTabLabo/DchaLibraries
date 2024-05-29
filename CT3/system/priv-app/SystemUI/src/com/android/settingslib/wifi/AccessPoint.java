package com.android.settingslib.wifi;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.IWifiManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.TtsSpan;
import android.util.Log;
import android.util.LruCache;
import com.android.settingslib.R$array;
import com.android.settingslib.R$string;
import com.mediatek.settingslib.wifi.AccessPointExt;
/* loaded from: a.zip:com/android/settingslib/wifi/AccessPoint.class */
public class AccessPoint implements Comparable<AccessPoint>, Cloneable {
    private String bssid;
    public AccessPointExt mAccessPointExt;
    private AccessPointListener mAccessPointListener;
    private WifiConfiguration mConfig;
    private final Context mContext;
    private WifiInfo mInfo;
    private NetworkInfo mNetworkInfo;
    private int security;
    private String ssid;
    public LruCache<String, ScanResult> mScanResultCache = new LruCache<>(32);
    private int networkId = -1;
    private int pskType = 0;
    private int mRssi = Integer.MAX_VALUE;
    private long mSeen = 0;

    /* loaded from: a.zip:com/android/settingslib/wifi/AccessPoint$AccessPointListener.class */
    public interface AccessPointListener {
        void onAccessPointChanged(AccessPoint accessPoint);

        void onLevelChanged(AccessPoint accessPoint);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public AccessPoint(Context context, ScanResult scanResult) {
        this.mContext = context;
        this.mAccessPointExt = new AccessPointExt(context);
        initWithScanResult(scanResult);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public AccessPoint(Context context, WifiConfiguration wifiConfiguration) {
        this.mContext = context;
        this.mAccessPointExt = new AccessPointExt(context);
        loadConfig(wifiConfiguration);
    }

    public static String convertToQuotedString(String str) {
        return "\"" + str + "\"";
    }

    private static int getPskType(ScanResult scanResult) {
        boolean contains = scanResult.capabilities.contains("WPA-PSK");
        boolean contains2 = scanResult.capabilities.contains("WPA2-PSK");
        if (contains2 && contains) {
            return 3;
        }
        if (contains2) {
            return 2;
        }
        if (contains) {
            return 1;
        }
        Log.w("SettingsLib.AccessPoint", "Received abnormal flag string: " + scanResult.capabilities);
        return 0;
    }

    private static int getSecurity(ScanResult scanResult) {
        int security = AccessPointExt.getSecurity(scanResult);
        if (security != -1) {
            return security;
        }
        if (scanResult.capabilities.contains("WEP")) {
            return 1;
        }
        if (scanResult.capabilities.contains("PSK")) {
            return 2;
        }
        return scanResult.capabilities.contains("EAP") ? 3 : 0;
    }

    static int getSecurity(WifiConfiguration wifiConfiguration) {
        int i = 1;
        int security = AccessPointExt.getSecurity(wifiConfiguration);
        if (security != -1) {
            return security;
        }
        if (wifiConfiguration.allowedKeyManagement.get(1)) {
            return 2;
        }
        if (wifiConfiguration.allowedKeyManagement.get(2) || wifiConfiguration.allowedKeyManagement.get(3)) {
            return 3;
        }
        if (wifiConfiguration.wepKeys[0] == null) {
            i = 0;
        }
        return i;
    }

    public static String getSummary(Context context, NetworkInfo.DetailedState detailedState, boolean z) {
        return getSummary(context, null, detailedState, z, null);
    }

    public static String getSummary(Context context, NetworkInfo.DetailedState detailedState, boolean z, String str) {
        return getSummary(context, null, detailedState, z, str);
    }

    public static String getSummary(Context context, String str, NetworkInfo.DetailedState detailedState, boolean z, String str2) {
        Network network;
        if (detailedState == NetworkInfo.DetailedState.CONNECTED && str == null) {
            if (!TextUtils.isEmpty(str2)) {
                return String.format(context.getString(R$string.connected_via_passpoint), str2);
            }
            if (z) {
                return context.getString(R$string.connected_via_wfa);
            }
        }
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        if (detailedState == NetworkInfo.DetailedState.CONNECTED) {
            try {
                network = IWifiManager.Stub.asInterface(ServiceManager.getService("wifi")).getCurrentNetwork();
            } catch (RemoteException e) {
                network = null;
            }
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
            if (networkCapabilities != null && !networkCapabilities.hasCapability(16)) {
                return context.getString(R$string.wifi_connected_no_internet);
            }
        }
        String[] stringArray = context.getResources().getStringArray(str == null ? R$array.wifi_status : R$array.wifi_status_with_ssid);
        int ordinal = detailedState.ordinal();
        return (ordinal >= stringArray.length || stringArray[ordinal].length() == 0) ? "" : String.format(stringArray[ordinal], str);
    }

    private String getVisibilityStatus() {
        int i;
        int i2;
        StringBuilder sb = new StringBuilder();
        StringBuilder sb2 = null;
        StringBuilder sb3 = null;
        String str = null;
        System.currentTimeMillis();
        if (this.mInfo != null) {
            str = this.mInfo.getBSSID();
            if (str != null) {
                sb.append(" ").append(str);
            }
            sb.append(" rssi=").append(this.mInfo.getRssi());
            sb.append(" ");
            sb.append(" score=").append(this.mInfo.score);
            sb.append(String.format(" tx=%.1f,", Double.valueOf(this.mInfo.txSuccessRate)));
            sb.append(String.format("%.1f,", Double.valueOf(this.mInfo.txRetriesRate)));
            sb.append(String.format("%.1f ", Double.valueOf(this.mInfo.txBadRate)));
            sb.append(String.format("rx=%.1f", Double.valueOf(this.mInfo.rxSuccessRate)));
        }
        int i3 = WifiConfiguration.INVALID_RSSI;
        int i4 = WifiConfiguration.INVALID_RSSI;
        int i5 = 0;
        int i6 = 0;
        int i7 = 0;
        int i8 = 0;
        for (ScanResult scanResult : this.mScanResultCache.snapshot().values()) {
            if (scanResult.frequency < 4900 || scanResult.frequency > 5900) {
                i = i6;
                i2 = i5;
                if (scanResult.frequency >= 2400) {
                    i = i6;
                    i2 = i5;
                    if (scanResult.frequency <= 2500) {
                        i = i6 + 1;
                        i2 = i5;
                    }
                }
            } else {
                i2 = i5 + 1;
                i = i6;
            }
            if (scanResult.frequency < 4900 || scanResult.frequency > 5900) {
                i6 = i;
                i5 = i2;
                if (scanResult.frequency >= 2400) {
                    i6 = i;
                    i5 = i2;
                    if (scanResult.frequency <= 2500) {
                        int i9 = i4;
                        if (scanResult.level > i4) {
                            i9 = scanResult.level;
                        }
                        i6 = i;
                        i5 = i2;
                        i4 = i9;
                        if (i7 < 4) {
                            StringBuilder sb4 = sb2;
                            if (sb2 == null) {
                                sb4 = new StringBuilder();
                            }
                            sb4.append(" \n{").append(scanResult.BSSID);
                            if (str != null && scanResult.BSSID.equals(str)) {
                                sb4.append("*");
                            }
                            sb4.append("=").append(scanResult.frequency);
                            sb4.append(",").append(scanResult.level);
                            sb4.append("}");
                            i7++;
                            i6 = i;
                            i5 = i2;
                            i4 = i9;
                            sb2 = sb4;
                        }
                    }
                }
            } else {
                int i10 = i3;
                if (scanResult.level > i3) {
                    i10 = scanResult.level;
                }
                i6 = i;
                i5 = i2;
                i3 = i10;
                if (i8 < 4) {
                    StringBuilder sb5 = sb3;
                    if (sb3 == null) {
                        sb5 = new StringBuilder();
                    }
                    sb5.append(" \n{").append(scanResult.BSSID);
                    if (str != null && scanResult.BSSID.equals(str)) {
                        sb5.append("*");
                    }
                    sb5.append("=").append(scanResult.frequency);
                    sb5.append(",").append(scanResult.level);
                    sb5.append("}");
                    i8++;
                    i6 = i;
                    i5 = i2;
                    i3 = i10;
                    sb3 = sb5;
                }
            }
        }
        sb.append(" [");
        if (i6 > 0) {
            sb.append("(").append(i6).append(")");
            if (i7 > 4) {
                sb.append("max=").append(i4);
                if (sb2 != null) {
                    sb.append(",").append(sb2.toString());
                }
            } else if (sb2 != null) {
                sb.append(sb2.toString());
            }
        }
        sb.append(";");
        if (i5 > 0) {
            sb.append("(").append(i5).append(")");
            if (i8 > 4) {
                sb.append("max=").append(i3);
                if (sb3 != null) {
                    sb.append(",").append(sb3.toString());
                }
            } else if (sb3 != null) {
                sb.append(sb3.toString());
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private void initWithScanResult(ScanResult scanResult) {
        this.ssid = scanResult.SSID;
        this.bssid = scanResult.BSSID;
        this.security = getSecurity(scanResult);
        if (this.security == 2) {
            this.pskType = getPskType(scanResult);
        }
        this.mRssi = scanResult.level;
        this.mSeen = scanResult.timestamp;
    }

    private boolean isInfoForThisAccessPoint(WifiConfiguration wifiConfiguration, WifiInfo wifiInfo) {
        if (isPasspoint() || this.networkId == -1) {
            return wifiConfiguration != null ? matches(wifiConfiguration) : this.ssid.equals(removeDoubleQuotes(wifiInfo.getSSID()));
        }
        return this.networkId == wifiInfo.getNetworkId();
    }

    static String removeDoubleQuotes(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        int length = str.length();
        return (length > 1 && str.charAt(0) == '\"' && str.charAt(length - 1) == '\"') ? str.substring(1, length - 1) : str;
    }

    public static String securityToString(int i, int i2) {
        return i == 1 ? "WEP" : i == 2 ? i2 == 1 ? "WPA" : i2 == 2 ? "WPA2" : i2 == 3 ? "WPA_WPA2" : "PSK" : i == 3 ? "EAP" : "NONE";
    }

    public void clearConfig() {
        this.mConfig = null;
        this.networkId = -1;
        this.mRssi = Integer.MAX_VALUE;
    }

    public Object clone() {
        AccessPoint accessPoint = null;
        try {
            accessPoint = (AccessPoint) super.clone();
        } catch (CloneNotSupportedException e) {
            Log.e("SettingsLib.AccessPoint", "CloneNotSupportedException happens in clone()");
        }
        return accessPoint;
    }

    @Override // java.lang.Comparable
    public int compareTo(@NonNull AccessPoint accessPoint) {
        if (!isActive() || accessPoint.isActive()) {
            if (isActive() || !accessPoint.isActive()) {
                if (this.mRssi == Integer.MAX_VALUE || accessPoint.mRssi != Integer.MAX_VALUE) {
                    if (this.mRssi != Integer.MAX_VALUE || accessPoint.mRssi == Integer.MAX_VALUE) {
                        if (this.networkId == -1 || accessPoint.networkId != -1) {
                            if (this.networkId != -1 || accessPoint.networkId == -1) {
                                int calculateSignalLevel = WifiManager.calculateSignalLevel(accessPoint.mRssi, 4) - WifiManager.calculateSignalLevel(this.mRssi, 4);
                                if (calculateSignalLevel != 0) {
                                    return calculateSignalLevel;
                                }
                                int i = accessPoint.security - this.security;
                                return i != 0 ? i : this.ssid.compareToIgnoreCase(accessPoint.ssid);
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

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj instanceof AccessPoint) {
            if (compareTo((AccessPoint) obj) == 0) {
                z = true;
            }
            return z;
        }
        return false;
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

    public WifiConfiguration getConfig() {
        return this.mConfig;
    }

    public NetworkInfo.DetailedState getDetailedState() {
        NetworkInfo.DetailedState detailedState = null;
        if (this.mNetworkInfo != null) {
            detailedState = this.mNetworkInfo.getDetailedState();
        }
        return detailedState;
    }

    public int getLevel() {
        if (this.mRssi == Integer.MAX_VALUE) {
            return -1;
        }
        return WifiManager.calculateSignalLevel(this.mRssi, 4);
    }

    public int getRssi() {
        int i = Integer.MIN_VALUE;
        for (ScanResult scanResult : this.mScanResultCache.snapshot().values()) {
            if (scanResult.level > i) {
                i = scanResult.level;
            }
        }
        return i;
    }

    public int getSecurity() {
        return this.security;
    }

    public long getSeen() {
        long j = 0;
        for (ScanResult scanResult : this.mScanResultCache.snapshot().values()) {
            if (scanResult.timestamp > j) {
                j = scanResult.timestamp;
            }
        }
        return j;
    }

    public String getSettingsSummary() {
        StringBuilder sb = new StringBuilder();
        if (isActive() && this.mConfig != null && this.mConfig.isPasspoint()) {
            sb.append(getSummary(this.mContext, getDetailedState(), false, this.mConfig.providerFriendlyName));
        } else if (isActive()) {
            sb.append(getSummary(this.mContext, getDetailedState(), this.mInfo != null ? this.mInfo.isEphemeral() : false));
        } else if (this.mConfig != null && this.mConfig.isPasspoint()) {
            sb.append(String.format(this.mContext.getString(R$string.available_via_passpoint), this.mConfig.providerFriendlyName));
        } else if (this.mConfig != null && this.mConfig.hasNoInternetAccess()) {
            sb.append(this.mContext.getString(R$string.wifi_no_internet));
        } else if (this.mConfig != null && !this.mConfig.getNetworkSelectionStatus().isNetworkEnabled()) {
            switch (this.mConfig.getNetworkSelectionStatus().getNetworkSelectionDisableReason()) {
                case 2:
                    sb.append(this.mContext.getString(R$string.wifi_disabled_generic));
                    break;
                case 3:
                    sb.append(this.mContext.getString(R$string.wifi_disabled_password_failure));
                    break;
                case 4:
                case 5:
                    sb.append(this.mContext.getString(R$string.wifi_disabled_network_failure));
                    break;
            }
        } else if (this.mRssi == Integer.MAX_VALUE) {
            sb.append(this.mContext.getString(R$string.wifi_not_in_range));
        } else if (this.mConfig != null) {
            sb.append(this.mContext.getString(R$string.wifi_remembered));
        }
        if (WifiTracker.sVerboseLogging > 0) {
            if (this.mInfo != null && this.mNetworkInfo != null) {
                sb.append(" f=").append(Integer.toString(this.mInfo.getFrequency()));
            }
            sb.append(" ").append(getVisibilityStatus());
            if (this.mConfig != null && !this.mConfig.getNetworkSelectionStatus().isNetworkEnabled()) {
                sb.append(" (").append(this.mConfig.getNetworkSelectionStatus().getNetworkStatusString());
                if (this.mConfig.getNetworkSelectionStatus().getDisableTime() > 0) {
                    long currentTimeMillis = (System.currentTimeMillis() - this.mConfig.getNetworkSelectionStatus().getDisableTime()) / 1000;
                    long j = (currentTimeMillis / 60) % 60;
                    long j2 = (j / 60) % 60;
                    sb.append(", ");
                    if (j2 > 0) {
                        sb.append(Long.toString(j2)).append("h ");
                    }
                    sb.append(Long.toString(j)).append("m ");
                    sb.append(Long.toString(currentTimeMillis % 60)).append("s ");
                }
                sb.append(")");
            }
            if (this.mConfig != null) {
                WifiConfiguration.NetworkSelectionStatus networkSelectionStatus = this.mConfig.getNetworkSelectionStatus();
                for (int i = 0; i < 11; i++) {
                    if (networkSelectionStatus.getDisableReasonCounter(i) != 0) {
                        sb.append(" ").append(WifiConfiguration.NetworkSelectionStatus.getNetworkDisableReasonString(i)).append("=").append(networkSelectionStatus.getDisableReasonCounter(i));
                    }
                }
            }
        }
        return sb.toString();
    }

    public CharSequence getSsid() {
        SpannableString spannableString = new SpannableString(this.ssid);
        spannableString.setSpan(new TtsSpan.VerbatimBuilder(this.ssid).build(), 0, this.ssid.length(), 18);
        return spannableString;
    }

    public String getSsidStr() {
        return this.ssid;
    }

    public String getSummary() {
        return getSettingsSummary();
    }

    public int hashCode() {
        int i = 0;
        if (this.mInfo != null) {
            i = (this.mInfo.hashCode() * 13) + 0;
        }
        return i + (this.mRssi * 19) + (this.networkId * 23) + (this.ssid.hashCode() * 29);
    }

    public boolean isActive() {
        boolean z;
        if (this.mNetworkInfo != null) {
            z = true;
            if (this.networkId == -1) {
                z = this.mNetworkInfo.getState() != NetworkInfo.State.DISCONNECTED;
            }
        } else {
            z = false;
        }
        return z;
    }

    public boolean isConnectable() {
        boolean z = false;
        if (getLevel() != -1) {
            z = false;
            if (getDetailedState() == null) {
                z = true;
            }
        }
        return z;
    }

    public boolean isEphemeral() {
        boolean z = false;
        if (this.mInfo != null) {
            z = false;
            if (this.mInfo.isEphemeral()) {
                z = false;
                if (this.mNetworkInfo != null) {
                    z = false;
                    if (this.mNetworkInfo.getState() != NetworkInfo.State.DISCONNECTED) {
                        z = true;
                    }
                }
            }
        }
        return z;
    }

    public boolean isPasspoint() {
        return this.mConfig != null ? this.mConfig.isPasspoint() : false;
    }

    public boolean isSaved() {
        return this.networkId != -1;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void loadConfig(WifiConfiguration wifiConfiguration) {
        if (wifiConfiguration.isPasspoint()) {
            this.ssid = wifiConfiguration.providerFriendlyName;
        } else {
            this.ssid = wifiConfiguration.SSID == null ? "" : removeDoubleQuotes(wifiConfiguration.SSID);
        }
        this.bssid = wifiConfiguration.BSSID;
        this.security = getSecurity(wifiConfiguration);
        this.networkId = wifiConfiguration.networkId;
        this.mConfig = wifiConfiguration;
    }

    public boolean matches(ScanResult scanResult) {
        boolean z = false;
        if (this.ssid.equals(scanResult.SSID)) {
            z = false;
            if (this.security == getSecurity(scanResult)) {
                z = true;
            }
        }
        return z;
    }

    public boolean matches(WifiConfiguration wifiConfiguration) {
        boolean z;
        if (wifiConfiguration.isPasspoint() && this.mConfig != null && this.mConfig.isPasspoint()) {
            return wifiConfiguration.FQDN.equals(this.mConfig.providerFriendlyName);
        }
        if (this.ssid.equals(removeDoubleQuotes(wifiConfiguration.SSID)) && this.security == getSecurity(wifiConfiguration)) {
            z = true;
            if (this.mConfig != null) {
                z = this.mConfig.shared == wifiConfiguration.shared;
            }
        } else {
            z = false;
        }
        return z;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setRssi(int i) {
        this.mRssi = i;
    }

    public String toString() {
        StringBuilder append = new StringBuilder().append("AccessPoint(").append(this.ssid);
        if (isSaved()) {
            append.append(',').append("saved");
        }
        if (isActive()) {
            append.append(',').append("active");
        }
        if (isEphemeral()) {
            append.append(',').append("ephemeral");
        }
        if (isConnectable()) {
            append.append(',').append("connectable");
        }
        if (this.security != 0) {
            append.append(',').append(securityToString(this.security, this.pskType));
        }
        return append.append(')').toString();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void update(WifiConfiguration wifiConfiguration) {
        this.mConfig = wifiConfiguration;
        this.networkId = wifiConfiguration.networkId;
        if (this.mAccessPointListener != null) {
            this.mAccessPointListener.onAccessPointChanged(this);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean update(ScanResult scanResult) {
        if (matches(scanResult)) {
            this.mScanResultCache.get(scanResult.BSSID);
            this.mScanResultCache.put(scanResult.BSSID, scanResult);
            int level = getLevel();
            int rssi = getRssi();
            this.mSeen = getSeen();
            this.mRssi = (getRssi() + rssi) / 2;
            int level2 = getLevel();
            if (level2 > 0 && level2 != level && this.mAccessPointListener != null) {
                this.mAccessPointListener.onLevelChanged(this);
            }
            if (this.security == 2) {
                this.pskType = getPskType(scanResult);
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
    public boolean update(WifiConfiguration wifiConfiguration, WifiInfo wifiInfo, NetworkInfo networkInfo) {
        boolean z = false;
        if (wifiInfo != null && isInfoForThisAccessPoint(wifiConfiguration, wifiInfo)) {
            boolean z2 = this.mInfo == null;
            this.mRssi = wifiInfo.getRssi();
            this.mInfo = wifiInfo;
            this.mNetworkInfo = networkInfo;
            z = z2;
            if (this.mAccessPointListener != null) {
                this.mAccessPointListener.onAccessPointChanged(this);
                z = z2;
            }
        } else if (this.mInfo != null) {
            this.mInfo = null;
            this.mNetworkInfo = null;
            z = true;
            if (this.mAccessPointListener != null) {
                this.mAccessPointListener.onAccessPointChanged(this);
                z = true;
            }
        }
        return z;
    }
}
