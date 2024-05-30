package com.android.settingslib.wifi;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkKey;
import android.net.NetworkScoreManager;
import android.net.NetworkScorerAppData;
import android.net.ScoredNetwork;
import android.net.wifi.IWifiManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkScoreCache;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.TtsSpan;
import android.util.ArraySet;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settingslib.R;
import com.android.settingslib.utils.ThreadUtils;
import com.mediatek.settingslib.wifi.AccessPointExt;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
/* loaded from: classes.dex */
public class AccessPoint implements Comparable<AccessPoint> {
    static final AtomicInteger sLastId = new AtomicInteger(0);
    private String bssid;
    public AccessPointExt mAccessPointExt;
    AccessPointListener mAccessPointListener;
    private int mCarrierApEapType;
    private String mCarrierName;
    private WifiConfiguration mConfig;
    private final Context mContext;
    private String mFqdn;
    int mId;
    private WifiInfo mInfo;
    private boolean mIsCarrierAp;
    private boolean mIsScoredNetworkMetered;
    private String mKey;
    private NetworkInfo mNetworkInfo;
    private String mProviderFriendlyName;
    private int mRssi;
    private final ArraySet<ScanResult> mScanResults;
    private final Map<String, TimestampedScoredNetwork> mScoredNetworkCache;
    private int mSpeed;
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

    public AccessPoint(Context context, WifiConfiguration wifiConfiguration) {
        this.mScanResults = new ArraySet<>();
        this.mScoredNetworkCache = new HashMap();
        this.networkId = -1;
        this.pskType = 0;
        this.mRssi = Integer.MIN_VALUE;
        this.mSpeed = 0;
        this.mIsScoredNetworkMetered = false;
        this.mIsCarrierAp = false;
        this.mCarrierApEapType = -1;
        this.mCarrierName = null;
        this.mContext = context;
        this.mAccessPointExt = new AccessPointExt(context);
        loadConfig(wifiConfiguration);
        this.mId = sLastId.incrementAndGet();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public AccessPoint(Context context, Collection<ScanResult> collection) {
        this.mScanResults = new ArraySet<>();
        this.mScoredNetworkCache = new HashMap();
        this.networkId = -1;
        this.pskType = 0;
        this.mRssi = Integer.MIN_VALUE;
        this.mSpeed = 0;
        this.mIsScoredNetworkMetered = false;
        this.mIsCarrierAp = false;
        this.mCarrierApEapType = -1;
        this.mCarrierName = null;
        this.mContext = context;
        this.mAccessPointExt = new AccessPointExt(context);
        if (collection.isEmpty()) {
            throw new IllegalArgumentException("Cannot construct with an empty ScanResult list");
        }
        this.mScanResults.addAll(collection);
        ScanResult next = collection.iterator().next();
        this.ssid = next.SSID;
        this.bssid = next.BSSID;
        this.security = getSecurity(next);
        if (this.security == 2) {
            this.pskType = getPskType(next);
        }
        updateKey();
        updateRssi();
        this.mIsCarrierAp = next.isCarrierAp;
        this.mCarrierApEapType = next.carrierApEapType;
        this.mCarrierName = next.carrierName;
        this.mId = sLastId.incrementAndGet();
    }

    @VisibleForTesting
    void loadConfig(WifiConfiguration wifiConfiguration) {
        this.ssid = wifiConfiguration.SSID == null ? "" : removeDoubleQuotes(wifiConfiguration.SSID);
        this.bssid = wifiConfiguration.BSSID;
        this.security = getSecurity(wifiConfiguration);
        updateKey();
        this.networkId = wifiConfiguration.networkId;
        this.mConfig = wifiConfiguration;
    }

    private void updateKey() {
        StringBuilder sb = new StringBuilder();
        if (TextUtils.isEmpty(getSsidStr())) {
            sb.append(getBssid());
        } else {
            sb.append(getSsidStr());
        }
        sb.append(',');
        sb.append(getSecurity());
        this.mKey = sb.toString();
    }

    @Override // java.lang.Comparable
    public int compareTo(AccessPoint accessPoint) {
        if (!isActive() || accessPoint.isActive()) {
            if (isActive() || !accessPoint.isActive()) {
                if (!isReachable() || accessPoint.isReachable()) {
                    if (isReachable() || !accessPoint.isReachable()) {
                        if (!isSaved() || accessPoint.isSaved()) {
                            if (isSaved() || !accessPoint.isSaved()) {
                                if (getSpeed() != accessPoint.getSpeed()) {
                                    return accessPoint.getSpeed() - getSpeed();
                                }
                                int calculateSignalLevel = WifiManager.calculateSignalLevel(accessPoint.mRssi, 5) - WifiManager.calculateSignalLevel(this.mRssi, 5);
                                if (calculateSignalLevel != 0) {
                                    return calculateSignalLevel;
                                }
                                int i = accessPoint.security - this.security;
                                if (i != 0) {
                                    return i;
                                }
                                int compareToIgnoreCase = getSsidStr().compareToIgnoreCase(accessPoint.getSsidStr());
                                if (compareToIgnoreCase != 0) {
                                    return compareToIgnoreCase;
                                }
                                return getSsidStr().compareTo(accessPoint.getSsidStr());
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
        return (obj instanceof AccessPoint) && compareTo((AccessPoint) obj) == 0;
    }

    public int hashCode() {
        return (this.mInfo != null ? 0 + (13 * this.mInfo.hashCode()) : 0) + (19 * this.mRssi) + (23 * this.networkId) + (29 * this.ssid.hashCode());
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("AccessPoint(");
        sb.append(this.ssid);
        if (this.bssid != null) {
            sb.append(":");
            sb.append(this.bssid);
        }
        if (isSaved()) {
            sb.append(',');
            sb.append("saved");
        }
        if (isActive()) {
            sb.append(',');
            sb.append("active");
        }
        if (isEphemeral()) {
            sb.append(',');
            sb.append("ephemeral");
        }
        if (isConnectable()) {
            sb.append(',');
            sb.append("connectable");
        }
        if (this.security != 0) {
            sb.append(',');
            sb.append(securityToString(this.security, this.pskType));
        }
        sb.append(",level=");
        sb.append(getLevel());
        if (this.mSpeed != 0) {
            sb.append(",speed=");
            sb.append(this.mSpeed);
        }
        sb.append(",metered=");
        sb.append(isMetered());
        if (isVerboseLoggingEnabled()) {
            sb.append(",rssi=");
            sb.append(this.mRssi);
            sb.append(",scan cache size=");
            sb.append(this.mScanResults.size());
        }
        sb.append(')');
        return sb.toString();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean update(WifiNetworkScoreCache wifiNetworkScoreCache, boolean z, long j) {
        boolean z2;
        if (z) {
            z2 = updateScores(wifiNetworkScoreCache, j);
        } else {
            z2 = false;
        }
        return updateMetered(wifiNetworkScoreCache) || z2;
    }

    private boolean updateScores(WifiNetworkScoreCache wifiNetworkScoreCache, long j) {
        long elapsedRealtime = SystemClock.elapsedRealtime();
        Iterator<ScanResult> it = this.mScanResults.iterator();
        while (it.hasNext()) {
            ScanResult next = it.next();
            ScoredNetwork scoredNetwork = wifiNetworkScoreCache.getScoredNetwork(next);
            if (scoredNetwork != null) {
                TimestampedScoredNetwork timestampedScoredNetwork = this.mScoredNetworkCache.get(next.BSSID);
                if (timestampedScoredNetwork == null) {
                    this.mScoredNetworkCache.put(next.BSSID, new TimestampedScoredNetwork(scoredNetwork, elapsedRealtime));
                } else {
                    timestampedScoredNetwork.update(scoredNetwork, elapsedRealtime);
                }
            }
        }
        final long j2 = elapsedRealtime - j;
        final Iterator<TimestampedScoredNetwork> it2 = this.mScoredNetworkCache.values().iterator();
        it2.forEachRemaining(new Consumer() { // from class: com.android.settingslib.wifi.-$$Lambda$AccessPoint$OIXfUc7y1PqI_zmQ3STe_086YzY
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                AccessPoint.lambda$updateScores$0(j2, it2, (TimestampedScoredNetwork) obj);
            }
        });
        return updateSpeed();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ void lambda$updateScores$0(long j, Iterator it, TimestampedScoredNetwork timestampedScoredNetwork) {
        if (timestampedScoredNetwork.getUpdatedTimestampMillis() < j) {
            it.remove();
        }
    }

    private boolean updateSpeed() {
        int i = this.mSpeed;
        this.mSpeed = generateAverageSpeedForSsid();
        boolean z = i != this.mSpeed;
        if (isVerboseLoggingEnabled() && z) {
            Log.i("SettingsLib.AccessPoint", String.format("%s: Set speed to %d", this.ssid, Integer.valueOf(this.mSpeed)));
        }
        return z;
    }

    private int generateAverageSpeedForSsid() {
        int i;
        if (this.mScoredNetworkCache.isEmpty()) {
            return 0;
        }
        if (Log.isLoggable("SettingsLib.AccessPoint", 3)) {
            Log.d("SettingsLib.AccessPoint", String.format("Generating fallbackspeed for %s using cache: %s", getSsidStr(), this.mScoredNetworkCache));
        }
        int i2 = 0;
        int i3 = 0;
        for (TimestampedScoredNetwork timestampedScoredNetwork : this.mScoredNetworkCache.values()) {
            int calculateBadge = timestampedScoredNetwork.getScore().calculateBadge(this.mRssi);
            if (calculateBadge != 0) {
                i2++;
                i3 += calculateBadge;
            }
        }
        if (i2 != 0) {
            i = i3 / i2;
        } else {
            i = 0;
        }
        if (isVerboseLoggingEnabled()) {
            Log.i("SettingsLib.AccessPoint", String.format("%s generated fallback speed is: %d", getSsidStr(), Integer.valueOf(i)));
        }
        return roundToClosestSpeedEnum(i);
    }

    private boolean updateMetered(WifiNetworkScoreCache wifiNetworkScoreCache) {
        boolean z = this.mIsScoredNetworkMetered;
        this.mIsScoredNetworkMetered = false;
        if (isActive() && this.mInfo != null) {
            ScoredNetwork scoredNetwork = wifiNetworkScoreCache.getScoredNetwork(NetworkKey.createFromWifiInfo(this.mInfo));
            if (scoredNetwork != null) {
                this.mIsScoredNetworkMetered = scoredNetwork.meteredHint | this.mIsScoredNetworkMetered;
            }
        } else {
            Iterator<ScanResult> it = this.mScanResults.iterator();
            while (it.hasNext()) {
                ScoredNetwork scoredNetwork2 = wifiNetworkScoreCache.getScoredNetwork(it.next());
                if (scoredNetwork2 != null) {
                    this.mIsScoredNetworkMetered = scoredNetwork2.meteredHint | this.mIsScoredNetworkMetered;
                }
            }
        }
        return z == this.mIsScoredNetworkMetered;
    }

    public static String getKey(ScanResult scanResult) {
        StringBuilder sb = new StringBuilder();
        if (TextUtils.isEmpty(scanResult.SSID)) {
            sb.append(scanResult.BSSID);
        } else {
            sb.append(scanResult.SSID);
        }
        sb.append(',');
        sb.append(getSecurity(scanResult));
        return sb.toString();
    }

    public static String getKey(WifiConfiguration wifiConfiguration) {
        StringBuilder sb = new StringBuilder();
        if (TextUtils.isEmpty(wifiConfiguration.SSID)) {
            sb.append(wifiConfiguration.BSSID);
        } else {
            sb.append(removeDoubleQuotes(wifiConfiguration.SSID));
        }
        sb.append(',');
        sb.append(getSecurity(wifiConfiguration));
        return sb.toString();
    }

    public String getKey() {
        return this.mKey;
    }

    public boolean matches(WifiConfiguration wifiConfiguration) {
        if (wifiConfiguration.isPasspoint() && this.mConfig != null && this.mConfig.isPasspoint()) {
            return this.ssid.equals(removeDoubleQuotes(wifiConfiguration.SSID)) && wifiConfiguration.FQDN.equals(this.mConfig.FQDN);
        } else if (this.ssid.equals(removeDoubleQuotes(wifiConfiguration.SSID)) && this.security == getSecurity(wifiConfiguration)) {
            return this.mConfig == null || this.mConfig.shared == wifiConfiguration.shared;
        } else {
            return false;
        }
    }

    public WifiConfiguration getConfig() {
        return this.mConfig;
    }

    public WifiInfo getInfo() {
        return this.mInfo;
    }

    public int getLevel() {
        return WifiManager.calculateSignalLevel(this.mRssi, 5);
    }

    public Set<ScanResult> getScanResults() {
        return this.mScanResults;
    }

    public Map<String, TimestampedScoredNetwork> getScoredNetworkCache() {
        return this.mScoredNetworkCache;
    }

    private void updateRssi() {
        if (isActive()) {
            return;
        }
        Iterator<ScanResult> it = this.mScanResults.iterator();
        int i = Integer.MIN_VALUE;
        while (it.hasNext()) {
            ScanResult next = it.next();
            if (next.level > i) {
                i = next.level;
            }
        }
        if (i != Integer.MIN_VALUE && this.mRssi != Integer.MIN_VALUE) {
            this.mRssi = (this.mRssi + i) / 2;
        } else {
            this.mRssi = i;
        }
    }

    public boolean isMetered() {
        return this.mIsScoredNetworkMetered || WifiConfiguration.isMetered(this.mConfig, this.mInfo);
    }

    public int getSecurity() {
        return this.security;
    }

    public String getSsidStr() {
        return this.ssid;
    }

    public String getBssid() {
        return this.bssid;
    }

    public CharSequence getSsid() {
        SpannableString spannableString = new SpannableString(this.ssid);
        spannableString.setSpan(new TtsSpan.TelephoneBuilder(this.ssid).build(), 0, this.ssid.length(), 18);
        return spannableString;
    }

    public String getConfigName() {
        if (this.mConfig != null && this.mConfig.isPasspoint()) {
            return this.mConfig.providerFriendlyName;
        }
        if (this.mFqdn != null) {
            return this.mProviderFriendlyName;
        }
        return this.ssid;
    }

    public NetworkInfo.DetailedState getDetailedState() {
        if (this.mNetworkInfo != null) {
            return this.mNetworkInfo.getDetailedState();
        }
        Log.w("SettingsLib.AccessPoint", "NetworkInfo is null, cannot return detailed state");
        return null;
    }

    public String getSummary() {
        return getSettingsSummary(this.mConfig);
    }

    private String getSettingsSummary(WifiConfiguration wifiConfiguration) {
        int i;
        StringBuilder sb = new StringBuilder();
        if (isActive() && wifiConfiguration != null && wifiConfiguration.isPasspoint()) {
            sb.append(getSummary(this.mContext, getDetailedState(), false, wifiConfiguration.providerFriendlyName));
        } else if (isActive() && wifiConfiguration != null && getDetailedState() == NetworkInfo.DetailedState.CONNECTED && this.mIsCarrierAp) {
            sb.append(String.format(this.mContext.getString(R.string.connected_via_carrier), this.mCarrierName));
        } else if (isActive()) {
            sb.append(getSummary(this.mContext, getDetailedState(), this.mInfo != null && this.mInfo.isEphemeral()));
        } else if (wifiConfiguration != null && wifiConfiguration.isPasspoint() && wifiConfiguration.getNetworkSelectionStatus().isNetworkEnabled()) {
            sb.append(String.format(this.mContext.getString(R.string.available_via_passpoint), wifiConfiguration.providerFriendlyName));
        } else if (wifiConfiguration != null && wifiConfiguration.hasNoInternetAccess()) {
            if (wifiConfiguration.getNetworkSelectionStatus().isNetworkPermanentlyDisabled()) {
                i = R.string.wifi_no_internet_no_reconnect;
            } else {
                i = R.string.wifi_no_internet;
            }
            sb.append(this.mContext.getString(i));
        } else if (wifiConfiguration != null && !wifiConfiguration.getNetworkSelectionStatus().isNetworkEnabled()) {
            int networkSelectionDisableReason = wifiConfiguration.getNetworkSelectionStatus().getNetworkSelectionDisableReason();
            if (networkSelectionDisableReason == 13) {
                sb.append(this.mContext.getString(R.string.wifi_check_password_try_again));
            } else {
                switch (networkSelectionDisableReason) {
                    case 2:
                        sb.append(this.mContext.getString(R.string.wifi_disabled_generic));
                        break;
                    case 3:
                        sb.append(this.mContext.getString(R.string.wifi_disabled_password_failure));
                        break;
                    case 4:
                    case 5:
                        sb.append(this.mContext.getString(R.string.wifi_disabled_network_failure));
                        break;
                }
            }
        } else if (wifiConfiguration != null && wifiConfiguration.getNetworkSelectionStatus().isNotRecommended()) {
            sb.append(this.mContext.getString(R.string.wifi_disabled_by_recommendation_provider));
        } else if (this.mIsCarrierAp) {
            sb.append(String.format(this.mContext.getString(R.string.available_via_carrier), this.mCarrierName));
        } else if (!isReachable()) {
            sb.append(this.mContext.getString(R.string.wifi_not_in_range));
        } else if (wifiConfiguration != null) {
            if (wifiConfiguration.recentFailure.getAssociationStatus() == 17) {
                sb.append(this.mContext.getString(R.string.wifi_ap_unable_to_handle_new_sta));
            } else {
                sb.append(this.mContext.getString(R.string.wifi_remembered));
            }
        }
        if (isVerboseLoggingEnabled()) {
            sb.append(WifiUtils.buildLoggingSummary(this, wifiConfiguration));
        }
        if (wifiConfiguration != null && (WifiUtils.isMeteredOverridden(wifiConfiguration) || wifiConfiguration.meteredHint)) {
            return this.mContext.getResources().getString(R.string.preference_summary_default_combination, WifiUtils.getMeteredLabel(this.mContext, wifiConfiguration), sb.toString());
        }
        if (getSpeedLabel() != null && sb.length() != 0) {
            return this.mContext.getResources().getString(R.string.preference_summary_default_combination, getSpeedLabel(), sb.toString());
        }
        if (getSpeedLabel() != null) {
            return getSpeedLabel();
        }
        return sb.toString();
    }

    public boolean isActive() {
        return (this.mNetworkInfo == null || (this.networkId == -1 && this.mNetworkInfo.getState() == NetworkInfo.State.DISCONNECTED)) ? false : true;
    }

    public boolean isConnectable() {
        return getLevel() != -1 && getDetailedState() == null;
    }

    public boolean isEphemeral() {
        return (this.mInfo == null || !this.mInfo.isEphemeral() || this.mNetworkInfo == null || this.mNetworkInfo.getState() == NetworkInfo.State.DISCONNECTED) ? false : true;
    }

    public boolean isPasspoint() {
        return this.mConfig != null && this.mConfig.isPasspoint();
    }

    private boolean isInfoForThisAccessPoint(WifiConfiguration wifiConfiguration, WifiInfo wifiInfo) {
        if (!isPasspoint() && this.networkId != -1) {
            return this.networkId == wifiInfo.getNetworkId();
        } else if (wifiConfiguration != null) {
            return matches(wifiConfiguration);
        } else {
            return this.ssid.equals(removeDoubleQuotes(wifiInfo.getSSID()));
        }
    }

    public boolean isSaved() {
        return this.networkId != -1;
    }

    public void setTag(Object obj) {
        this.mTag = obj;
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
    public void setScanResults(Collection<ScanResult> collection) {
        String key = getKey();
        for (ScanResult scanResult : collection) {
            String key2 = getKey(scanResult);
            if (!this.mKey.equals(key2)) {
                throw new IllegalArgumentException(String.format("ScanResult %s\nkey of %s did not match current AP key %s", scanResult, key2, key));
            }
        }
        int level = getLevel();
        this.mScanResults.clear();
        this.mScanResults.addAll(collection);
        updateRssi();
        int level2 = getLevel();
        if (level2 > 0 && level2 != level) {
            updateSpeed();
            ThreadUtils.postOnMainThread(new Runnable() { // from class: com.android.settingslib.wifi.-$$Lambda$AccessPoint$MkkIS1nUbezHicDMmYnviyiBJyo
                @Override // java.lang.Runnable
                public final void run() {
                    AccessPoint.lambda$setScanResults$1(AccessPoint.this);
                }
            });
        }
        ThreadUtils.postOnMainThread(new Runnable() { // from class: com.android.settingslib.wifi.-$$Lambda$AccessPoint$0Yq14aFJZLjPMzFGAvglLaxsblI
            @Override // java.lang.Runnable
            public final void run() {
                AccessPoint.lambda$setScanResults$2(AccessPoint.this);
            }
        });
        if (!collection.isEmpty()) {
            ScanResult next = collection.iterator().next();
            if (this.security == 2) {
                this.pskType = getPskType(next);
            }
            this.mIsCarrierAp = next.isCarrierAp;
            this.mCarrierApEapType = next.carrierApEapType;
            this.mCarrierName = next.carrierName;
        }
    }

    public static /* synthetic */ void lambda$setScanResults$1(AccessPoint accessPoint) {
        if (accessPoint.mAccessPointListener != null) {
            accessPoint.mAccessPointListener.onLevelChanged(accessPoint);
        }
    }

    public static /* synthetic */ void lambda$setScanResults$2(AccessPoint accessPoint) {
        if (accessPoint.mAccessPointListener != null) {
            accessPoint.mAccessPointListener.onAccessPointChanged(accessPoint);
        }
    }

    /* JADX WARN: Code restructure failed: missing block: B:23:0x0046, code lost:
        if (r4.mNetworkInfo.getDetailedState() != r7.getDetailedState()) goto L16;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public boolean update(WifiConfiguration wifiConfiguration, WifiInfo wifiInfo, NetworkInfo networkInfo) {
        int level = getLevel();
        boolean z = false;
        if (wifiInfo != null && isInfoForThisAccessPoint(wifiConfiguration, wifiInfo)) {
            if (this.mInfo == null) {
                z = true;
            }
            if (this.mConfig != wifiConfiguration) {
                update(wifiConfiguration);
            }
            if (this.mRssi != wifiInfo.getRssi() && wifiInfo.getRssi() != -127) {
                this.mRssi = wifiInfo.getRssi();
            } else {
                if (this.mNetworkInfo != null) {
                    if (networkInfo != null) {
                    }
                }
                this.mInfo = wifiInfo;
                this.mNetworkInfo = networkInfo;
            }
            z = true;
            this.mInfo = wifiInfo;
            this.mNetworkInfo = networkInfo;
        } else if (this.mInfo != null) {
            this.mInfo = null;
            this.mNetworkInfo = null;
            z = true;
        }
        if (z && this.mAccessPointListener != null) {
            ThreadUtils.postOnMainThread(new Runnable() { // from class: com.android.settingslib.wifi.-$$Lambda$AccessPoint$S7H59e_8IxpVPy0V68Oc2-zX-rg
                @Override // java.lang.Runnable
                public final void run() {
                    AccessPoint.lambda$update$3(AccessPoint.this);
                }
            });
            if (level != getLevel()) {
                ThreadUtils.postOnMainThread(new Runnable() { // from class: com.android.settingslib.wifi.-$$Lambda$AccessPoint$QW-1Uw0oxoaKqUtEtPO0oPvH5ng
                    @Override // java.lang.Runnable
                    public final void run() {
                        AccessPoint.lambda$update$4(AccessPoint.this);
                    }
                });
            }
        }
        return z;
    }

    public static /* synthetic */ void lambda$update$3(AccessPoint accessPoint) {
        if (accessPoint.mAccessPointListener != null) {
            accessPoint.mAccessPointListener.onAccessPointChanged(accessPoint);
        }
    }

    public static /* synthetic */ void lambda$update$4(AccessPoint accessPoint) {
        if (accessPoint.mAccessPointListener != null) {
            accessPoint.mAccessPointListener.onLevelChanged(accessPoint);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void update(WifiConfiguration wifiConfiguration) {
        this.mConfig = wifiConfiguration;
        this.networkId = wifiConfiguration != null ? wifiConfiguration.networkId : -1;
        ThreadUtils.postOnMainThread(new Runnable() { // from class: com.android.settingslib.wifi.-$$Lambda$AccessPoint$QyP0aXhFuWtm7lmBu1IY3qbfmBA
            @Override // java.lang.Runnable
            public final void run() {
                AccessPoint.lambda$update$5(AccessPoint.this);
            }
        });
    }

    public static /* synthetic */ void lambda$update$5(AccessPoint accessPoint) {
        if (accessPoint.mAccessPointListener != null) {
            accessPoint.mAccessPointListener.onAccessPointChanged(accessPoint);
        }
    }

    @VisibleForTesting
    void setRssi(int i) {
        this.mRssi = i;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int getSpeed() {
        return this.mSpeed;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public String getSpeedLabel() {
        return getSpeedLabel(this.mSpeed);
    }

    private static int roundToClosestSpeedEnum(int i) {
        if (i < 5) {
            return 0;
        }
        if (i < 7) {
            return 5;
        }
        if (i < 15) {
            return 10;
        }
        if (i < 25) {
            return 20;
        }
        return 30;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public String getSpeedLabel(int i) {
        return getSpeedLabel(this.mContext, i);
    }

    private static String getSpeedLabel(Context context, int i) {
        if (i != 5) {
            if (i != 10) {
                if (i != 20) {
                    if (i == 30) {
                        return context.getString(R.string.speed_label_very_fast);
                    }
                    return null;
                }
                return context.getString(R.string.speed_label_fast);
            }
            return context.getString(R.string.speed_label_okay);
        }
        return context.getString(R.string.speed_label_slow);
    }

    public static String getSpeedLabel(Context context, ScoredNetwork scoredNetwork, int i) {
        return getSpeedLabel(context, roundToClosestSpeedEnum(scoredNetwork.calculateBadge(i)));
    }

    public boolean isReachable() {
        return this.mRssi != Integer.MIN_VALUE;
    }

    public static String getSummary(Context context, String str, NetworkInfo.DetailedState detailedState, boolean z, String str2) {
        NetworkCapabilities networkCapabilities;
        if (detailedState == NetworkInfo.DetailedState.CONNECTED && str == null) {
            if (!TextUtils.isEmpty(str2)) {
                return String.format(context.getString(R.string.connected_via_passpoint), str2);
            }
            if (z) {
                NetworkScorerAppData activeScorer = ((NetworkScoreManager) context.getSystemService(NetworkScoreManager.class)).getActiveScorer();
                return (activeScorer == null || activeScorer.getRecommendationServiceLabel() == null) ? context.getString(R.string.connected_via_network_scorer_default) : String.format(context.getString(R.string.connected_via_network_scorer), activeScorer.getRecommendationServiceLabel());
            }
        }
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        if (detailedState == NetworkInfo.DetailedState.CONNECTED) {
            try {
                networkCapabilities = connectivityManager.getNetworkCapabilities(IWifiManager.Stub.asInterface(ServiceManager.getService("wifi")).getCurrentNetwork());
            } catch (RemoteException e) {
                networkCapabilities = null;
            }
            if (networkCapabilities != null) {
                if (networkCapabilities.hasCapability(17)) {
                    return context.getString(context.getResources().getIdentifier("network_available_sign_in", "string", "android"));
                }
                if (!networkCapabilities.hasCapability(16)) {
                    return context.getString(R.string.wifi_connected_no_internet);
                }
            }
        }
        if (detailedState == null) {
            Log.w("SettingsLib.AccessPoint", "state is null, returning empty summary");
            return "";
        }
        String[] stringArray = context.getResources().getStringArray(str == null ? R.array.wifi_status : R.array.wifi_status_with_ssid);
        int ordinal = detailedState.ordinal();
        return (ordinal >= stringArray.length || stringArray[ordinal].length() == 0) ? "" : String.format(stringArray[ordinal], str);
    }

    public static String getSummary(Context context, NetworkInfo.DetailedState detailedState, boolean z) {
        return getSummary(context, null, detailedState, z, null);
    }

    public static String getSummary(Context context, NetworkInfo.DetailedState detailedState, boolean z, String str) {
        return getSummary(context, null, detailedState, z, str);
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
        if (scanResult.capabilities.contains("EAP")) {
            return 3;
        }
        return 0;
    }

    static int getSecurity(WifiConfiguration wifiConfiguration) {
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
        return wifiConfiguration.wepKeys[0] != null ? 1 : 0;
    }

    public static String securityToString(int i, int i2) {
        if (i == 1) {
            return "WEP";
        }
        if (i == 2) {
            if (i2 == 1) {
                return "WPA";
            }
            if (i2 == 2) {
                return "WPA2";
            }
            if (i2 == 3) {
                return "WPA_WPA2";
            }
            return "PSK";
        } else if (i == 3) {
            return "EAP";
        } else {
            return "NONE";
        }
    }

    static String removeDoubleQuotes(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        int length = str.length();
        if (length > 1 && str.charAt(0) == '\"') {
            int i = length - 1;
            if (str.charAt(i) == '\"') {
                return str.substring(1, i);
            }
        }
        return str;
    }

    private static boolean isVerboseLoggingEnabled() {
        return WifiTracker.sVerboseLogging || Log.isLoggable("SettingsLib.AccessPoint", 2);
    }
}
