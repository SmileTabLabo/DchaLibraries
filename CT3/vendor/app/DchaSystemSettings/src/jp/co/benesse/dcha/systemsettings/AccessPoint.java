package jp.co.benesse.dcha.systemsettings;

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
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.TtsSpan;
import android.util.LruCache;
import java.util.ArrayList;
import jp.co.benesse.dcha.util.Logger;
/* loaded from: s.zip:jp/co/benesse/dcha/systemsettings/AccessPoint.class */
public class AccessPoint implements Comparable<AccessPoint>, Cloneable {
    private String bssid;
    private AccessPointListener mAccessPointListener;
    private WifiConfiguration mConfig;
    private final Context mContext;
    private WifiInfo mInfo;
    private NetworkInfo mNetworkInfo;
    private int mRssi;
    public LruCache<String, ScanResult> mScanResultCache;
    boolean mWpsAvailable;
    private int networkId;
    private int pskType;
    private int security;
    private String ssid;

    /* loaded from: s.zip:jp/co/benesse/dcha/systemsettings/AccessPoint$AccessPointListener.class */
    public interface AccessPointListener {
        void onAccessPointChanged(AccessPoint accessPoint);

        void onLevelChanged(AccessPoint accessPoint);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public AccessPoint(Context context, ScanResult scanResult) {
        this.mScanResultCache = new LruCache<>(32);
        this.networkId = -1;
        this.pskType = 0;
        this.mRssi = Integer.MAX_VALUE;
        this.mWpsAvailable = false;
        Logger.d("AccessPoint", "AccessPoint 0010");
        this.mContext = context;
        initWithScanResult(scanResult);
        Logger.d("AccessPoint", "AccessPoint 0011");
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public AccessPoint(Context context, WifiConfiguration wifiConfiguration) {
        this.mScanResultCache = new LruCache<>(32);
        this.networkId = -1;
        this.pskType = 0;
        this.mRssi = Integer.MAX_VALUE;
        this.mWpsAvailable = false;
        Logger.d("AccessPoint", "AccessPoint 0012");
        this.mContext = context;
        loadConfig(wifiConfiguration);
        Logger.d("AccessPoint", "AccessPoint 0013");
    }

    public AccessPoint(Context context, Bundle bundle) {
        this.mScanResultCache = new LruCache<>(32);
        this.networkId = -1;
        this.pskType = 0;
        this.mRssi = Integer.MAX_VALUE;
        this.mWpsAvailable = false;
        Logger.d("AccessPoint", "AccessPoint 0001");
        this.mContext = context;
        this.mConfig = (WifiConfiguration) bundle.getParcelable("key_config");
        if (this.mConfig != null) {
            Logger.d("AccessPoint", "AccessPoint 0002");
            loadConfig(this.mConfig);
        }
        if (bundle.containsKey("key_ssid")) {
            Logger.d("AccessPoint", "AccessPoint 0003");
            this.ssid = bundle.getString("key_ssid");
        }
        if (bundle.containsKey("key_security")) {
            Logger.d("AccessPoint", "AccessPoint 0004");
            this.security = bundle.getInt("key_security");
        }
        if (bundle.containsKey("key_psktype")) {
            Logger.d("AccessPoint", "AccessPoint 0005");
            this.pskType = bundle.getInt("key_psktype");
        }
        this.mInfo = (WifiInfo) bundle.getParcelable("key_wifiinfo");
        if (bundle.containsKey("key_networkinfo")) {
            Logger.d("AccessPoint", "AccessPoint 0006");
            this.mNetworkInfo = (NetworkInfo) bundle.getParcelable("key_networkinfo");
        }
        if (bundle.containsKey("key_scanresultcache")) {
            Logger.d("AccessPoint", "AccessPoint 0007");
            ArrayList<ScanResult> parcelableArrayList = bundle.getParcelableArrayList("key_scanresultcache");
            this.mScanResultCache.evictAll();
            for (ScanResult scanResult : parcelableArrayList) {
                Logger.d("AccessPoint", "AccessPoint 0008");
                this.mScanResultCache.put(scanResult.BSSID, scanResult);
            }
        }
        update(this.mConfig, this.mInfo, this.mNetworkInfo);
        this.mRssi = getRssi();
        Logger.d("AccessPoint", "AccessPoint 0009");
    }

    public static String convertToQuotedString(String str) {
        Logger.d("AccessPoint", "convertToQuotedString 0001");
        return "\"" + str + "\"";
    }

    private static int getPskType(ScanResult scanResult) {
        Logger.d("AccessPoint", "getPskType 0001");
        boolean contains = scanResult.capabilities.contains("WPA-PSK");
        boolean contains2 = scanResult.capabilities.contains("WPA2-PSK");
        if (contains2 && contains) {
            Logger.d("AccessPoint", "getPskType 0002");
            return 3;
        } else if (contains2) {
            Logger.d("AccessPoint", "getPskType 0003");
            return 2;
        } else if (contains) {
            Logger.d("AccessPoint", "getPskType 0004");
            return 1;
        } else {
            Logger.d("AccessPoint", "getPskType 0005");
            Logger.w("AccessPoint", "Received abnormal flag string: " + scanResult.capabilities);
            return 0;
        }
    }

    private static int getSecurity(ScanResult scanResult) {
        Logger.d("AccessPoint", "getSecurity 0001");
        if (scanResult.capabilities.contains("WEP")) {
            Logger.d("AccessPoint", "getSecurity 0002");
            return 1;
        } else if (scanResult.capabilities.contains("PSK")) {
            Logger.d("AccessPoint", "getSecurity 0003");
            return 2;
        } else if (scanResult.capabilities.contains("EAP")) {
            Logger.d("AccessPoint", "getSecurity 0004");
            return 3;
        } else {
            Logger.d("AccessPoint", "getSecurity 0005");
            return 0;
        }
    }

    static int getSecurity(WifiConfiguration wifiConfiguration) {
        int i = 1;
        Logger.d("AccessPoint", "getSecurity 0006");
        if (wifiConfiguration.allowedKeyManagement.get(1)) {
            Logger.d("AccessPoint", "getSecurity 0007");
            return 2;
        } else if (wifiConfiguration.allowedKeyManagement.get(2) || wifiConfiguration.allowedKeyManagement.get(3)) {
            Logger.d("AccessPoint", "getSecurity 0008");
            return 3;
        } else {
            Logger.d("AccessPoint", "getSecurity 0009");
            if (wifiConfiguration.wepKeys[0] == null) {
                i = 0;
            }
            return i;
        }
    }

    public static String getSummary(Context context, NetworkInfo.DetailedState detailedState, boolean z) {
        Logger.d("AccessPoint", "getSummary 0011");
        return getSummary(context, null, detailedState, z, null);
    }

    public static String getSummary(Context context, NetworkInfo.DetailedState detailedState, boolean z, String str) {
        Logger.d("AccessPoint", "getSummary 0012");
        return getSummary(context, null, detailedState, z, str);
    }

    public static String getSummary(Context context, String str, NetworkInfo.DetailedState detailedState, boolean z, String str2) {
        Network network;
        Logger.d("AccessPoint", "getSummary 0002");
        if (detailedState == NetworkInfo.DetailedState.CONNECTED && str == null) {
            Logger.d("AccessPoint", "getSummary 0003");
            if (!TextUtils.isEmpty(str2)) {
                Logger.d("AccessPoint", "getSummary 0004");
                return String.format(context.getString(2131230943), str2);
            } else if (z) {
                Logger.d("AccessPoint", "getSummary 0005");
                return context.getString(2131230942);
            }
        }
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        if (detailedState == NetworkInfo.DetailedState.CONNECTED) {
            Logger.d("AccessPoint", "getSummary 0006");
            try {
                network = IWifiManager.Stub.asInterface(ServiceManager.getService("wifi")).getCurrentNetwork();
            } catch (RemoteException e) {
                Logger.d("AccessPoint", "getSummary 0007");
                Logger.d("AccessPoint", "RemoteException", e);
                network = null;
            }
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
            if (networkCapabilities != null && !networkCapabilities.hasCapability(16)) {
                Logger.d("AccessPoint", "getSummary 0008");
                return context.getString(2131230945);
            }
        }
        String[] stringArray = context.getResources().getStringArray(str == null ? 2131034112 : 2131034113);
        int ordinal = detailedState.ordinal();
        if (ordinal >= stringArray.length || stringArray[ordinal].length() == 0) {
            Logger.d("AccessPoint", "getSummary 0009");
            return "";
        }
        Logger.d("AccessPoint", "getSummary 0010");
        return String.format(stringArray[ordinal], str);
    }

    private String getVisibilityStatus() {
        int i;
        int i2;
        Logger.d("AccessPoint", "getVisibilityStatus 0001");
        StringBuilder sb = new StringBuilder();
        StringBuilder sb2 = null;
        StringBuilder sb3 = null;
        String str = null;
        if (this.mInfo != null) {
            Logger.d("AccessPoint", "getVisibilityStatus 0002");
            str = this.mInfo.getBSSID();
            if (str != null) {
                Logger.d("AccessPoint", "getVisibilityStatus 0003");
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
            if (i7 <= 4) {
                Logger.d("AccessPoint", "getVisibilityStatus 0004");
                if (sb2 != null) {
                    Logger.d("AccessPoint", "getVisibilityStatus 0005");
                    sb.append(sb2.toString());
                }
            } else {
                Logger.d("AccessPoint", "getVisibilityStatus 0006");
                sb.append("max=").append(i4);
                if (sb2 != null) {
                    Logger.d("AccessPoint", "getVisibilityStatus 0007");
                    sb.append(",").append(sb2.toString());
                }
            }
        }
        sb.append(";");
        if (i5 > 0) {
            Logger.d("AccessPoint", "getVisibilityStatus 0008");
            sb.append("(").append(i5).append(")");
            if (i8 <= 4) {
                Logger.d("AccessPoint", "getVisibilityStatus 0009");
                if (sb3 != null) {
                    Logger.d("AccessPoint", "getVisibilityStatus 0010");
                    sb.append(sb3.toString());
                }
            } else {
                Logger.d("AccessPoint", "getVisibilityStatus 0011");
                sb.append("max=").append(i3);
                if (sb3 != null) {
                    Logger.d("AccessPoint", "getVisibilityStatus 0012");
                    sb.append(",").append(sb3.toString());
                }
            }
        }
        sb.append("]");
        Logger.d("AccessPoint", "getVisibilityStatus 0014");
        return sb.toString();
    }

    private void initWithScanResult(ScanResult scanResult) {
        Logger.d("AccessPoint", "initWithScanResult 0001");
        this.ssid = scanResult.SSID;
        this.bssid = scanResult.BSSID;
        this.security = getSecurity(scanResult);
        this.mWpsAvailable = this.security != 3 ? scanResult.capabilities.contains("WPS") : false;
        if (this.security == 2) {
            Logger.d("AccessPoint", "initWithScanResult 0002");
            this.pskType = getPskType(scanResult);
        }
        this.mRssi = scanResult.level;
        Logger.d("AccessPoint", "initWithScanResult 0003");
    }

    private boolean isInfoForThisAccessPoint(WifiConfiguration wifiConfiguration, WifiInfo wifiInfo) {
        boolean z = true;
        Logger.d("AccessPoint", "isInfoForThisAccessPoint 0001");
        if (!isPasspoint() && this.networkId != -1) {
            Logger.d("AccessPoint", "isInfoForThisAccessPoint 0002");
            if (this.networkId != wifiInfo.getNetworkId()) {
                z = false;
            }
            return z;
        } else if (wifiConfiguration != null) {
            Logger.d("AccessPoint", "isInfoForThisAccessPoint 0003");
            return matches(wifiConfiguration);
        } else {
            Logger.d("AccessPoint", "isInfoForThisAccessPoint 0004");
            return this.ssid.equals(removeDoubleQuotes(wifiInfo.getSSID()));
        }
    }

    static String removeDoubleQuotes(String str) {
        Logger.d("AccessPoint", "removeDoubleQuotes 0001");
        if (TextUtils.isEmpty(str)) {
            Logger.d("AccessPoint", "removeDoubleQuotes 0002");
            return "";
        }
        int length = str.length();
        if (length > 1 && str.charAt(0) == '\"' && str.charAt(length - 1) == '\"') {
            Logger.d("AccessPoint", "removeDoubleQuotes 0003");
            return str.substring(1, length - 1);
        }
        Logger.d("AccessPoint", "removeDoubleQuotes 0004");
        return str;
    }

    public static String securityToString(int i, int i2) {
        Logger.d("AccessPoint", "securityToString 0001");
        if (i == 1) {
            Logger.d("AccessPoint", "securityToString 0002");
            return "WEP";
        } else if (i != 2) {
            if (i == 3) {
                Logger.d("AccessPoint", "securityToString 0008");
                return "EAP";
            }
            Logger.d("AccessPoint", "securityToString 0009");
            return "NONE";
        } else {
            Logger.d("AccessPoint", "securityToString 0003");
            if (i2 == 1) {
                Logger.d("AccessPoint", "securityToString 0004");
                return "WPA";
            } else if (i2 == 2) {
                Logger.d("AccessPoint", "securityToString 0005");
                return "WPA2";
            } else if (i2 == 3) {
                Logger.d("AccessPoint", "securityToString 0006");
                return "WPA_WPA2";
            } else {
                Logger.d("AccessPoint", "securityToString 0007");
                return "PSK";
            }
        }
    }

    public void clearConfig() {
        Logger.d("AccessPoint", "clearConfig 0001");
        this.mConfig = null;
        this.networkId = -1;
        Logger.d("AccessPoint", "clearConfig 0002");
    }

    public Object clone() {
        AccessPoint accessPoint = null;
        try {
            Logger.d("AccessPoint", "clone 0001");
            accessPoint = (AccessPoint) super.clone();
        } catch (CloneNotSupportedException e) {
            Logger.d("AccessPoint", "clone 0002");
            Logger.e("AccessPoint", "CloneNotSupportedException happens in clone()");
        }
        Logger.d("AccessPoint", "clone 0003");
        return accessPoint;
    }

    @Override // java.lang.Comparable
    public int compareTo(AccessPoint accessPoint) {
        Logger.d("AccessPoint", "compareTo 0001");
        if (isActive() && !accessPoint.isActive()) {
            Logger.d("AccessPoint", "compareTo 0002");
            return -1;
        } else if (!isActive() && accessPoint.isActive()) {
            Logger.d("AccessPoint", "compareTo 0003");
            return 1;
        } else if (this.mRssi != Integer.MAX_VALUE && accessPoint.mRssi == Integer.MAX_VALUE) {
            Logger.d("AccessPoint", "compareTo 0004");
            return -1;
        } else if (this.mRssi == Integer.MAX_VALUE && accessPoint.mRssi != Integer.MAX_VALUE) {
            Logger.d("AccessPoint", "compareTo 0005");
            return 1;
        } else if (this.networkId != -1 && accessPoint.networkId == -1) {
            Logger.d("AccessPoint", "compareTo 0006");
            return -1;
        } else if (this.networkId == -1 && accessPoint.networkId != -1) {
            Logger.d("AccessPoint", "compareTo 0007");
            return 1;
        } else {
            int calculateSignalLevel = WifiManager.calculateSignalLevel(accessPoint.mRssi, 4) - WifiManager.calculateSignalLevel(this.mRssi, 4);
            if (calculateSignalLevel != 0) {
                Logger.d("AccessPoint", "compareTo 0008");
                return calculateSignalLevel;
            }
            Logger.d("AccessPoint", "compareTo 0009");
            return this.ssid.compareToIgnoreCase(accessPoint.ssid);
        }
    }

    public boolean equals(Object obj) {
        boolean z = true;
        Logger.d("AccessPoint", "equals 0001");
        if (!(obj instanceof AccessPoint)) {
            Logger.d("AccessPoint", "equals 0002");
            return false;
        }
        Logger.d("AccessPoint", "equals 0003");
        if (compareTo((AccessPoint) obj) != 0) {
            z = false;
        }
        return z;
    }

    public WifiConfiguration getConfig() {
        Logger.d("AccessPoint", "getConfig 0001");
        return this.mConfig;
    }

    public NetworkInfo.DetailedState getDetailedState() {
        NetworkInfo.DetailedState detailedState = null;
        Logger.d("AccessPoint", "getDetailedState 0001");
        if (this.mNetworkInfo != null) {
            detailedState = this.mNetworkInfo.getDetailedState();
        }
        return detailedState;
    }

    public WifiInfo getInfo() {
        Logger.d("AccessPoint", "getInfo 0001");
        return this.mInfo;
    }

    public int getLevel() {
        Logger.d("AccessPoint", "getLevel 0001");
        if (this.mRssi == Integer.MAX_VALUE) {
            Logger.d("AccessPoint", "getLevel 0002");
            return -1;
        }
        Logger.d("AccessPoint", "getLevel 0003");
        return WifiManager.calculateSignalLevel(this.mRssi, 4);
    }

    public NetworkInfo getNetworkInfo() {
        Logger.d("AccessPoint", "getNetworkInfo 0001");
        return this.mNetworkInfo;
    }

    public int getRssi() {
        Logger.d("AccessPoint", "getRssi 0001");
        int i = Integer.MIN_VALUE;
        for (ScanResult scanResult : this.mScanResultCache.snapshot().values()) {
            if (scanResult.level > i) {
                i = scanResult.level;
            }
        }
        Logger.d("AccessPoint", "getRssi 0002");
        return i;
    }

    public int getSecurity() {
        Logger.d("AccessPoint", "getSecurity 0001");
        return this.security;
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    public String getSecurityString(boolean z) {
        Logger.d("AccessPoint", "getSecurityString 0001");
        Context context = this.mContext;
        if (this.mConfig != null && this.mConfig.isPasspoint()) {
            Logger.d("AccessPoint", "getSecurityString 0002");
            return z ? context.getString(2131230761) : context.getString(2131230767);
        }
        switch (this.security) {
            case 0:
                Logger.d("AccessPoint", "getSecurityString 0011");
                break;
            case 1:
                Logger.d("AccessPoint", "getSecurityString 0010");
                return z ? context.getString(2131230756) : context.getString(2131230762);
            case 2:
                Logger.d("AccessPoint", "getSecurityString 0004");
                switch (this.pskType) {
                    case 0:
                        Logger.d("AccessPoint", "getSecurityString 0008");
                        break;
                    case 1:
                        Logger.d("AccessPoint", "getSecurityString 0005");
                        return z ? context.getString(2131230757) : context.getString(2131230763);
                    case 2:
                        Logger.d("AccessPoint", "getSecurityString 0006");
                        return z ? context.getString(2131230758) : context.getString(2131230764);
                    case 3:
                        Logger.d("AccessPoint", "getSecurityString 0007");
                        return z ? context.getString(2131230759) : context.getString(2131230765);
                }
                Logger.d("AccessPoint", "getSecurityString 0009");
                return z ? context.getString(2131230760) : context.getString(2131230766);
            case 3:
                Logger.d("AccessPoint", "getSecurityString 0003");
                return z ? context.getString(2131230761) : context.getString(2131230767);
        }
        Logger.d("AccessPoint", "getSecurityString 0012");
        return z ? "" : context.getString(2131230934);
    }

    public String getSettingsSummary() {
        String string;
        Logger.d("AccessPoint", "getSettingsSummary 0001");
        StringBuilder sb = new StringBuilder();
        if (isActive() && this.mConfig != null && this.mConfig.isPasspoint()) {
            Logger.d("AccessPoint", "getSettingsSummary 0002");
            sb.append(getSummary(this.mContext, getDetailedState(), false, this.mConfig.providerFriendlyName));
        } else if (isActive()) {
            Logger.d("AccessPoint", "getSettingsSummary 0003");
            sb.append(getSummary(this.mContext, getDetailedState(), this.mInfo != null ? this.mInfo.isEphemeral() : false));
        } else if (this.mConfig != null && this.mConfig.isPasspoint()) {
            Logger.d("AccessPoint", "getSettingsSummary 0004");
            sb.append(String.format(this.mContext.getString(2131230944), this.mConfig.providerFriendlyName));
        } else if (this.mConfig != null && this.mConfig.hasNoInternetAccess()) {
            Logger.d("AccessPoint", "getSettingsSummary 0005");
            sb.append(this.mContext.getString(2131230940));
        } else if (this.mConfig != null && !this.mConfig.getNetworkSelectionStatus().isNetworkEnabled()) {
            Logger.d("AccessPoint", "getSettingsSummary 0006");
            switch (this.mConfig.getNetworkSelectionStatus().getNetworkSelectionDisableReason()) {
                case 2:
                    Logger.d("AccessPoint", "getSettingsSummary 0010");
                    sb.append(this.mContext.getString(2131230936));
                    break;
                case 3:
                    Logger.d("AccessPoint", "getSettingsSummary 0007");
                    sb.append(this.mContext.getString(2131230938));
                    break;
                case 4:
                    Logger.d("AccessPoint", "getSettingsSummary 0008");
                case 5:
                    Logger.d("AccessPoint", "getSettingsSummary 0009");
                    sb.append(this.mContext.getString(2131230937));
                    break;
            }
        } else if (this.mRssi == Integer.MAX_VALUE) {
            Logger.d("AccessPoint", "getSettingsSummary 0011");
            sb.append(this.mContext.getString(2131230939));
        } else {
            Logger.d("AccessPoint", "getSettingsSummary 0012");
            if (this.mConfig != null) {
                Logger.d("AccessPoint", "getSettingsSummary 0013");
                sb.append(this.mContext.getString(2131230935));
            }
            if (this.security != 0) {
                Logger.d("AccessPoint", "getSettingsSummary 0014");
                if (sb.length() == 0) {
                    Logger.d("AccessPoint", "getSettingsSummary 0015");
                    string = this.mContext.getString(2131230946);
                } else {
                    Logger.d("AccessPoint", "getSettingsSummary 0016");
                    string = this.mContext.getString(2131230947);
                }
                sb.append(String.format(string, getSecurityString(true)));
            }
            if (this.mConfig == null && this.mWpsAvailable) {
                Logger.d("AccessPoint", "getSettingsSummary 0017");
                if (sb.length() == 0) {
                    sb.append(this.mContext.getString(2131230853));
                    Logger.d("AccessPoint", "getSettingsSummary 0018");
                } else {
                    sb.append(this.mContext.getString(2131230854));
                    Logger.d("AccessPoint", "getSettingsSummary 0019");
                }
            }
        }
        if (WifiTracker.sVerboseLogging > 0) {
            Logger.d("AccessPoint", "getSettingsSummary 0020");
            if (this.mInfo != null && this.mNetworkInfo != null) {
                Logger.d("AccessPoint", "getSettingsSummary 0021");
                sb.append(" f=").append(Integer.toString(this.mInfo.getFrequency()));
            }
            sb.append(" ").append(getVisibilityStatus());
            if (this.mConfig != null && !this.mConfig.getNetworkSelectionStatus().isNetworkEnabled()) {
                Logger.d("AccessPoint", "getSettingsSummary 0022");
                sb.append(" (").append(this.mConfig.getNetworkSelectionStatus().getNetworkStatusString());
                if (this.mConfig.getNetworkSelectionStatus().getDisableTime() > 0) {
                    Logger.d("AccessPoint", "getSettingsSummary 0023");
                    long currentTimeMillis = (System.currentTimeMillis() - this.mConfig.getNetworkSelectionStatus().getDisableTime()) / 1000;
                    long j = (currentTimeMillis / 60) % 60;
                    long j2 = (j / 60) % 60;
                    sb.append(", ");
                    if (j2 > 0) {
                        Logger.d("AccessPoint", "getSettingsSummary 0024");
                        sb.append(Long.toString(j2)).append("h ");
                    }
                    sb.append(Long.toString(j)).append("m ");
                    sb.append(Long.toString(currentTimeMillis % 60)).append("s ");
                }
                sb.append(")");
            }
            if (this.mConfig != null) {
                Logger.d("AccessPoint", "getSettingsSummary 0025");
                WifiConfiguration.NetworkSelectionStatus networkSelectionStatus = this.mConfig.getNetworkSelectionStatus();
                for (int i = 0; i < 11; i++) {
                    if (networkSelectionStatus.getDisableReasonCounter(i) != 0) {
                        sb.append(" ").append(WifiConfiguration.NetworkSelectionStatus.getNetworkDisableReasonString(i)).append("=").append(networkSelectionStatus.getDisableReasonCounter(i));
                    }
                }
            }
        }
        Logger.d("AccessPoint", "getSettingsSummary 0026");
        return sb.toString();
    }

    public CharSequence getSsid() {
        Logger.d("AccessPoint", "getSsid 0001");
        SpannableString spannableString = new SpannableString(this.ssid);
        spannableString.setSpan(new TtsSpan.VerbatimBuilder(this.ssid).build(), 0, this.ssid.length(), 18);
        Logger.d("AccessPoint", "getSsid 0002");
        return spannableString;
    }

    public String getSsidStr() {
        Logger.d("AccessPoint", "getSsidStr 0001");
        return this.ssid;
    }

    public String getSummary() {
        Logger.d("AccessPoint", "getSummary 0001");
        return getSettingsSummary();
    }

    public int hashCode() {
        Logger.d("AccessPoint", "hashCode 0001");
        int i = 0;
        if (this.mInfo != null) {
            Logger.d("AccessPoint", "hashCode 0002");
            i = (this.mInfo.hashCode() * 13) + 0;
        }
        int i2 = this.mRssi;
        int i3 = this.networkId;
        int hashCode = this.ssid.hashCode();
        Logger.d("AccessPoint", "hashCode 0003");
        return i + (i2 * 19) + (i3 * 23) + (hashCode * 29);
    }

    public boolean isActive() {
        boolean z;
        Logger.d("AccessPoint", "isActive 0001");
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
        boolean z = true;
        Logger.d("AccessPoint", "isConnectable 0001");
        if (getLevel() == -1 || getDetailedState() != null) {
            z = false;
        }
        return z;
    }

    public boolean isEphemeral() {
        boolean z = true;
        Logger.d("AccessPoint", "isEphemeral 0001");
        if (this.mInfo == null || !this.mInfo.isEphemeral() || this.mNetworkInfo == null) {
            z = false;
        } else if (this.mNetworkInfo.getState() == NetworkInfo.State.DISCONNECTED) {
            z = false;
        }
        return z;
    }

    public boolean isPasspoint() {
        boolean z = false;
        Logger.d("AccessPoint", "isPasspoint 0001");
        if (this.mConfig != null) {
            z = this.mConfig.isPasspoint();
        }
        return z;
    }

    public boolean isSaved() {
        boolean z = true;
        Logger.d("AccessPoint", "isSaved 0001");
        if (this.networkId == -1) {
            z = false;
        }
        return z;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void loadConfig(WifiConfiguration wifiConfiguration) {
        Logger.d("AccessPoint", "loadConfig 0001");
        if (wifiConfiguration.isPasspoint()) {
            Logger.d("AccessPoint", "loadConfig 0002");
            this.ssid = wifiConfiguration.providerFriendlyName;
        } else {
            Logger.d("AccessPoint", "loadConfig 0003");
            this.ssid = wifiConfiguration.SSID == null ? "" : removeDoubleQuotes(wifiConfiguration.SSID);
        }
        this.bssid = wifiConfiguration.BSSID;
        this.security = getSecurity(wifiConfiguration);
        this.networkId = wifiConfiguration.networkId;
        this.mConfig = wifiConfiguration;
        Logger.d("AccessPoint", "loadConfig 0004");
    }

    public boolean matches(ScanResult scanResult) {
        boolean z = true;
        Logger.d("AccessPoint", "matches 0001");
        if (!this.ssid.equals(scanResult.SSID) || this.security != getSecurity(scanResult)) {
            z = false;
        }
        return z;
    }

    public boolean matches(WifiConfiguration wifiConfiguration) {
        boolean z;
        Logger.d("AccessPoint", "matches 0002");
        if (wifiConfiguration.isPasspoint() && this.mConfig != null && this.mConfig.isPasspoint()) {
            Logger.d("AccessPoint", "matches 0003");
            return wifiConfiguration.FQDN.equals(this.mConfig.providerFriendlyName);
        }
        Logger.d("AccessPoint", "matches 0004");
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

    public void saveWifiState(Bundle bundle) {
        Logger.d("AccessPoint", "saveWifiState 0001");
        if (this.ssid != null) {
            Logger.d("AccessPoint", "saveWifiState 0002");
            bundle.putString("key_ssid", getSsidStr());
        }
        bundle.putInt("key_security", this.security);
        bundle.putInt("key_psktype", this.pskType);
        if (this.mConfig != null) {
            Logger.d("AccessPoint", "saveWifiState 0003");
            bundle.putParcelable("key_config", this.mConfig);
        }
        bundle.putParcelable("key_wifiinfo", this.mInfo);
        bundle.putParcelableArrayList("key_scanresultcache", new ArrayList<>(this.mScanResultCache.snapshot().values()));
        if (this.mNetworkInfo != null) {
            Logger.d("AccessPoint", "saveWifiState 0004");
            bundle.putParcelable("key_networkinfo", this.mNetworkInfo);
        }
        Logger.d("AccessPoint", "saveWifiState 0005");
    }

    public void setListener(AccessPointListener accessPointListener) {
        Logger.d("AccessPoint", "setListener 0001");
        this.mAccessPointListener = accessPointListener;
        Logger.d("AccessPoint", "setListener 0002");
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setRssi(int i) {
        Logger.d("AccessPoint", "setRssi 0001");
        this.mRssi = i;
        Logger.d("AccessPoint", "setRssi 0002");
    }

    public String toString() {
        Logger.d("AccessPoint", "toString 0001");
        StringBuilder append = new StringBuilder().append("AccessPoint(").append(this.ssid);
        if (isSaved()) {
            Logger.d("AccessPoint", "toString 0002");
            append.append(',').append("saved");
        }
        if (isActive()) {
            Logger.d("AccessPoint", "toString 0003");
            append.append(',').append("active");
        }
        if (isEphemeral()) {
            Logger.d("AccessPoint", "toString 0004");
            append.append(',').append("ephemeral");
        }
        if (isConnectable()) {
            Logger.d("AccessPoint", "toString 0005");
            append.append(',').append("connectable");
        }
        if (this.security != 0) {
            Logger.d("AccessPoint", "toString 0006");
            append.append(',').append(securityToString(this.security, this.pskType));
        }
        Logger.d("AccessPoint", "toString 0007");
        return append.append(')').toString();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void update(WifiConfiguration wifiConfiguration) {
        Logger.d("AccessPoint", "update 0014");
        this.mConfig = wifiConfiguration;
        this.networkId = wifiConfiguration.networkId;
        if (this.mAccessPointListener != null) {
            Logger.d("AccessPoint", "update 0015");
            this.mAccessPointListener.onAccessPointChanged(this);
        }
        Logger.d("AccessPoint", "update 0016");
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean update(ScanResult scanResult) {
        Logger.d("AccessPoint", "update 0001");
        if (!matches(scanResult)) {
            Logger.d("AccessPoint", "update 0007");
            return false;
        }
        Logger.d("AccessPoint", "update 0002");
        this.mScanResultCache.get(scanResult.BSSID);
        this.mScanResultCache.put(scanResult.BSSID, scanResult);
        int level = getLevel();
        this.mRssi = (getRssi() + getRssi()) / 2;
        int level2 = getLevel();
        if (level2 > 0 && level2 != level && this.mAccessPointListener != null) {
            Logger.d("AccessPoint", "update 0003");
            this.mAccessPointListener.onLevelChanged(this);
        }
        if (this.security == 2) {
            Logger.d("AccessPoint", "update 0004");
            this.pskType = getPskType(scanResult);
        }
        if (this.mAccessPointListener != null) {
            Logger.d("AccessPoint", "update 0005");
            this.mAccessPointListener.onAccessPointChanged(this);
        }
        Logger.d("AccessPoint", "update 0006");
        return true;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean update(WifiConfiguration wifiConfiguration, WifiInfo wifiInfo, NetworkInfo networkInfo) {
        Logger.d("AccessPoint", "update 0008");
        boolean z = false;
        if (wifiInfo != null && isInfoForThisAccessPoint(wifiConfiguration, wifiInfo)) {
            Logger.d("AccessPoint", "update 0009");
            boolean z2 = this.mInfo == null;
            this.mRssi = wifiInfo.getRssi();
            this.mInfo = wifiInfo;
            this.mNetworkInfo = networkInfo;
            z = z2;
            if (this.mAccessPointListener != null) {
                Logger.d("AccessPoint", "update 0010");
                this.mAccessPointListener.onAccessPointChanged(this);
                z = z2;
            }
        } else if (this.mInfo != null) {
            Logger.d("AccessPoint", "update 0011");
            this.mInfo = null;
            this.mNetworkInfo = null;
            z = true;
            if (this.mAccessPointListener != null) {
                Logger.d("AccessPoint", "update 0012");
                this.mAccessPointListener.onAccessPointChanged(this);
                z = true;
            }
        }
        Logger.d("AccessPoint", "update 0013");
        return z;
    }
}
