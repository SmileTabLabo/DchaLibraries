package com.android.settingslib.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;
import com.android.settingslib.R$string;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
/* loaded from: a.zip:com/android/settingslib/wifi/WifiTracker.class */
public class WifiTracker {
    private ArrayList<AccessPoint> mAccessPoints;
    private final AtomicBoolean mConnected;
    private final ConnectivityManager mConnectivityManager;
    private final Context mContext;
    private final IntentFilter mFilter;
    private final boolean mIncludePasspoints;
    private final boolean mIncludeSaved;
    private final boolean mIncludeScans;
    private WifiInfo mLastInfo;
    private NetworkInfo mLastNetworkInfo;
    private final WifiListener mListener;
    private final MainHandler mMainHandler;
    private WifiTrackerNetworkCallback mNetworkCallback;
    private final NetworkRequest mNetworkRequest;
    final BroadcastReceiver mReceiver;
    private boolean mRegistered;
    private boolean mSavedNetworksExist;
    private Integer mScanId;
    private HashMap<String, ScanResult> mScanResultCache;
    Scanner mScanner;
    private HashMap<String, Integer> mSeenBssids;
    private final WifiManager mWifiManager;
    private final WorkHandler mWorkHandler;
    public static int sVerboseLogging = 0;
    private static final ReadWriteLock mReadWriteLock = new ReentrantReadWriteLock();

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/settingslib/wifi/WifiTracker$MainHandler.class */
    public final class MainHandler extends Handler {
        final WifiTracker this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public MainHandler(WifiTracker wifiTracker, Looper looper) {
            super(looper);
            this.this$0 = wifiTracker;
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            if (this.this$0.mListener == null) {
                return;
            }
            switch (message.what) {
                case 0:
                    this.this$0.mListener.onConnectedChanged();
                    return;
                case 1:
                    this.this$0.mListener.onWifiStateChanged(message.arg1);
                    return;
                case 2:
                    this.this$0.mListener.onAccessPointsChanged();
                    return;
                case 3:
                    if (this.this$0.mScanner != null) {
                        this.this$0.mScanner.resume();
                        return;
                    }
                    return;
                case 4:
                    if (this.this$0.mScanner != null) {
                        this.this$0.mScanner.pause();
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/settingslib/wifi/WifiTracker$Multimap.class */
    public static class Multimap<K, V> {
        private final HashMap<K, List<V>> store;

        private Multimap() {
            this.store = new HashMap<>();
        }

        /* synthetic */ Multimap(Multimap multimap) {
            this();
        }

        List<V> getAll(K k) {
            List<V> list = this.store.get(k);
            if (list == null) {
                list = Collections.emptyList();
            }
            return list;
        }

        void put(K k, V v) {
            List<V> list = this.store.get(k);
            ArrayList arrayList = list;
            if (list == null) {
                arrayList = new ArrayList(3);
                this.store.put(k, arrayList);
            }
            arrayList.add(v);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: a.zip:com/android/settingslib/wifi/WifiTracker$Scanner.class */
    public class Scanner extends Handler {
        private int mRetry = 0;
        final WifiTracker this$0;

        Scanner(WifiTracker wifiTracker) {
            this.this$0 = wifiTracker;
        }

        void forceScan() {
            removeMessages(0);
            sendEmptyMessage(0);
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            if (message.what != 0) {
                return;
            }
            if (this.this$0.mWifiManager.startScan()) {
                this.mRetry = 0;
            } else {
                int i = this.mRetry + 1;
                this.mRetry = i;
                if (i >= 3) {
                    this.mRetry = 0;
                    if (this.this$0.mContext != null) {
                        Toast.makeText(this.this$0.mContext, R$string.wifi_fail_to_scan, 1).show();
                        return;
                    }
                    return;
                }
            }
            sendEmptyMessageDelayed(0, 10000L);
        }

        boolean isScanning() {
            return hasMessages(0);
        }

        void pause() {
            this.mRetry = 0;
            removeMessages(0);
        }

        void resume() {
            if (hasMessages(0)) {
                return;
            }
            sendEmptyMessage(0);
        }
    }

    /* loaded from: a.zip:com/android/settingslib/wifi/WifiTracker$WifiListener.class */
    public interface WifiListener {
        void onAccessPointsChanged();

        void onConnectedChanged();

        void onWifiStateChanged(int i);
    }

    /* loaded from: a.zip:com/android/settingslib/wifi/WifiTracker$WifiTrackerNetworkCallback.class */
    private final class WifiTrackerNetworkCallback extends ConnectivityManager.NetworkCallback {
        final WifiTracker this$0;

        private WifiTrackerNetworkCallback(WifiTracker wifiTracker) {
            this.this$0 = wifiTracker;
        }

        /* synthetic */ WifiTrackerNetworkCallback(WifiTracker wifiTracker, WifiTrackerNetworkCallback wifiTrackerNetworkCallback) {
            this(wifiTracker);
        }

        @Override // android.net.ConnectivityManager.NetworkCallback
        public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
            if (network.equals(this.this$0.mWifiManager.getCurrentNetwork())) {
                this.this$0.mWorkHandler.sendEmptyMessage(1);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/settingslib/wifi/WifiTracker$WorkHandler.class */
    public final class WorkHandler extends Handler {
        final WifiTracker this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public WorkHandler(WifiTracker wifiTracker, Looper looper) {
            super(looper);
            this.this$0 = wifiTracker;
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            switch (message.what) {
                case 0:
                    this.this$0.updateAccessPoints();
                    return;
                case 1:
                    this.this$0.updateNetworkInfo((NetworkInfo) message.obj);
                    return;
                case 2:
                    this.this$0.handleResume();
                    return;
                case 3:
                    if (message.arg1 != 3) {
                        WifiTracker.mReadWriteLock.writeLock().lock();
                        try {
                            this.this$0.mLastInfo = null;
                            this.this$0.mLastNetworkInfo = null;
                            WifiTracker.mReadWriteLock.writeLock().unlock();
                            if (this.this$0.mScanner != null) {
                                this.this$0.mScanner.pause();
                            }
                        } catch (Throwable th) {
                            WifiTracker.mReadWriteLock.writeLock().unlock();
                            throw th;
                        }
                    } else if (this.this$0.mScanner != null) {
                        this.this$0.mScanner.resume();
                    }
                    this.this$0.mMainHandler.obtainMessage(1, message.arg1, 0).sendToTarget();
                    return;
                default:
                    return;
            }
        }
    }

    public WifiTracker(Context context, WifiListener wifiListener, Looper looper, boolean z, boolean z2) {
        this(context, wifiListener, looper, z, z2, false);
    }

    public WifiTracker(Context context, WifiListener wifiListener, Looper looper, boolean z, boolean z2, boolean z3) {
        this(context, wifiListener, looper, z, z2, z3, (WifiManager) context.getSystemService(WifiManager.class), (ConnectivityManager) context.getSystemService(ConnectivityManager.class), Looper.myLooper());
    }

    WifiTracker(Context context, WifiListener wifiListener, Looper looper, boolean z, boolean z2, boolean z3, WifiManager wifiManager, ConnectivityManager connectivityManager, Looper looper2) {
        this.mConnected = new AtomicBoolean(false);
        this.mAccessPoints = new ArrayList<>();
        this.mSeenBssids = new HashMap<>();
        this.mScanResultCache = new HashMap<>();
        this.mScanId = 0;
        this.mReceiver = new BroadcastReceiver(this) { // from class: com.android.settingslib.wifi.WifiTracker.1
            final WifiTracker this$0;

            {
                this.this$0 = this;
            }

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context2, Intent intent) {
                String action = intent.getAction();
                if ("android.net.wifi.WIFI_STATE_CHANGED".equals(action)) {
                    this.this$0.updateWifiState(intent.getIntExtra("wifi_state", 4));
                } else if ("android.net.wifi.SCAN_RESULTS".equals(action) || "android.net.wifi.CONFIGURED_NETWORKS_CHANGE".equals(action) || "android.net.wifi.LINK_CONFIGURATION_CHANGED".equals(action)) {
                    this.this$0.mWorkHandler.sendEmptyMessage(0);
                } else if ("android.net.wifi.STATE_CHANGE".equals(action)) {
                    NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    this.this$0.mConnected.set(networkInfo.isConnected());
                    this.this$0.mMainHandler.sendEmptyMessage(0);
                    this.this$0.mWorkHandler.sendEmptyMessage(0);
                    this.this$0.mWorkHandler.obtainMessage(1, networkInfo).sendToTarget();
                }
            }
        };
        if (!z && !z2) {
            throw new IllegalArgumentException("Must include either saved or scans");
        }
        this.mContext = context;
        Looper mainLooper = looper2 == null ? Looper.getMainLooper() : looper2;
        this.mMainHandler = new MainHandler(this, mainLooper);
        this.mWorkHandler = new WorkHandler(this, looper != null ? looper : mainLooper);
        this.mWifiManager = wifiManager;
        this.mIncludeSaved = z;
        this.mIncludeScans = z2;
        this.mIncludePasspoints = z3;
        this.mListener = wifiListener;
        this.mConnectivityManager = connectivityManager;
        sVerboseLogging = this.mWifiManager.getVerboseLoggingLevel();
        this.mFilter = new IntentFilter();
        this.mFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        this.mFilter.addAction("android.net.wifi.SCAN_RESULTS");
        this.mFilter.addAction("android.net.wifi.NETWORK_IDS_CHANGED");
        this.mFilter.addAction("android.net.wifi.supplicant.STATE_CHANGE");
        this.mFilter.addAction("android.net.wifi.CONFIGURED_NETWORKS_CHANGE");
        this.mFilter.addAction("android.net.wifi.LINK_CONFIGURATION_CHANGED");
        this.mFilter.addAction("android.net.wifi.STATE_CHANGE");
        this.mNetworkRequest = new NetworkRequest.Builder().clearCapabilities().addTransportType(1).build();
    }

    private Collection<ScanResult> fetchScanResults() {
        this.mScanId = Integer.valueOf(this.mScanId.intValue() + 1);
        for (ScanResult scanResult : this.mWifiManager.getScanResults()) {
            if (scanResult.SSID != null && !scanResult.SSID.isEmpty()) {
                this.mScanResultCache.put(scanResult.BSSID, scanResult);
                this.mSeenBssids.put(scanResult.BSSID, this.mScanId);
            }
        }
        if (this.mScanId.intValue() > 3) {
            int intValue = this.mScanId.intValue();
            Iterator<Map.Entry<String, Integer>> it = this.mSeenBssids.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Integer> next = it.next();
                if (next.getValue().intValue() < Integer.valueOf(intValue - 3).intValue()) {
                    this.mScanResultCache.get(next.getKey());
                    this.mScanResultCache.remove(next.getKey());
                    it.remove();
                }
            }
        }
        return this.mScanResultCache.values();
    }

    private AccessPoint getCachedOrCreate(ScanResult scanResult, List<AccessPoint> list) {
        int size = list.size();
        for (int i = 0; i < size; i++) {
            if (list.get(i).matches(scanResult)) {
                AccessPoint remove = list.remove(i);
                remove.update(scanResult);
                return remove;
            }
        }
        return new AccessPoint(this.mContext, scanResult);
    }

    private AccessPoint getCachedOrCreate(WifiConfiguration wifiConfiguration, List<AccessPoint> list) {
        int size = list.size();
        for (int i = 0; i < size; i++) {
            if (list.get(i).matches(wifiConfiguration)) {
                AccessPoint remove = list.remove(i);
                remove.loadConfig(wifiConfiguration);
                return remove;
            }
        }
        return new AccessPoint(this.mContext, wifiConfiguration);
    }

    private WifiConfiguration getWifiConfigurationForNetworkId(int i) {
        List<WifiConfiguration> configuredNetworks = this.mWifiManager.getConfiguredNetworks();
        if (configuredNetworks != null) {
            for (WifiConfiguration wifiConfiguration : configuredNetworks) {
                if (this.mLastInfo != null && i == wifiConfiguration.networkId && (!wifiConfiguration.selfAdded || wifiConfiguration.numAssociation != 0)) {
                    return wifiConfiguration;
                }
            }
            return null;
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleResume() {
        this.mScanResultCache.clear();
        this.mSeenBssids.clear();
        this.mScanId = 0;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateAccessPoints() {
        boolean z;
        boolean z2;
        WifiConfiguration matchingWifiConfig;
        boolean z3;
        List<AccessPoint> accessPoints = getAccessPoints();
        ArrayList<AccessPoint> arrayList = new ArrayList<>();
        for (AccessPoint accessPoint : accessPoints) {
            accessPoint.clearConfig();
        }
        mReadWriteLock.readLock().lock();
        try {
            Multimap multimap = new Multimap(null);
            WifiConfiguration wifiConfigurationForNetworkId = this.mLastInfo != null ? getWifiConfigurationForNetworkId(this.mLastInfo.getNetworkId()) : null;
            Collection<ScanResult> fetchScanResults = fetchScanResults();
            List<WifiConfiguration> configuredNetworks = this.mWifiManager.getConfiguredNetworks();
            if (configuredNetworks != null) {
                this.mSavedNetworksExist = configuredNetworks.size() != 0;
                for (WifiConfiguration wifiConfiguration : configuredNetworks) {
                    if (!wifiConfiguration.selfAdded || wifiConfiguration.numAssociation != 0) {
                        AccessPoint cachedOrCreate = getCachedOrCreate(wifiConfiguration, accessPoints);
                        if (this.mLastInfo != null && this.mLastNetworkInfo != null && !wifiConfiguration.isPasspoint()) {
                            cachedOrCreate.update(wifiConfigurationForNetworkId, this.mLastInfo, this.mLastNetworkInfo);
                        }
                        if (this.mIncludeSaved) {
                            if (!wifiConfiguration.isPasspoint() || this.mIncludePasspoints) {
                                Iterator<T> it = fetchScanResults.iterator();
                                while (true) {
                                    z3 = false;
                                    if (it.hasNext()) {
                                        if (((ScanResult) it.next()).SSID.equals(cachedOrCreate.getSsidStr())) {
                                            z3 = true;
                                            break;
                                        }
                                    } else {
                                        break;
                                    }
                                }
                                if (!z3) {
                                    cachedOrCreate.setRssi(Integer.MAX_VALUE);
                                }
                                arrayList.add(cachedOrCreate);
                            }
                            if (!wifiConfiguration.isPasspoint()) {
                                multimap.put(cachedOrCreate.getSsidStr(), cachedOrCreate);
                            }
                        } else {
                            accessPoints.add(cachedOrCreate);
                        }
                    }
                }
            }
            if (fetchScanResults != null) {
                for (ScanResult scanResult : fetchScanResults) {
                    if (scanResult.SSID != null && scanResult.SSID.length() != 0 && !scanResult.capabilities.contains("[IBSS]")) {
                        Iterator it2 = multimap.getAll(scanResult.SSID).iterator();
                        while (true) {
                            z2 = false;
                            if (it2.hasNext()) {
                                if (((AccessPoint) it2.next()).update(scanResult)) {
                                    z2 = true;
                                    break;
                                }
                            } else {
                                break;
                            }
                        }
                        if (!z2 && this.mIncludeScans) {
                            AccessPoint cachedOrCreate2 = getCachedOrCreate(scanResult, accessPoints);
                            if (this.mLastInfo != null && this.mLastNetworkInfo != null) {
                                cachedOrCreate2.update(wifiConfigurationForNetworkId, this.mLastInfo, this.mLastNetworkInfo);
                            }
                            if (scanResult.isPasspointNetwork() && (matchingWifiConfig = this.mWifiManager.getMatchingWifiConfig(scanResult)) != null) {
                                cachedOrCreate2.update(matchingWifiConfig);
                            }
                            if (this.mLastInfo != null && this.mLastInfo.getBSSID() != null && this.mLastInfo.getBSSID().equals(scanResult.BSSID) && wifiConfigurationForNetworkId != null && wifiConfigurationForNetworkId.isPasspoint()) {
                                cachedOrCreate2.update(wifiConfigurationForNetworkId);
                            }
                            arrayList.add(cachedOrCreate2);
                            multimap.put(cachedOrCreate2.getSsidStr(), cachedOrCreate2);
                        }
                    }
                }
            }
            mReadWriteLock.readLock().unlock();
            Collections.sort(arrayList);
            for (AccessPoint accessPoint2 : this.mAccessPoints) {
                if (accessPoint2.getSsid() != null) {
                    String ssidStr = accessPoint2.getSsidStr();
                    Iterator<T> it3 = arrayList.iterator();
                    while (true) {
                        z = false;
                        if (!it3.hasNext()) {
                            break;
                        }
                        AccessPoint accessPoint3 = (AccessPoint) it3.next();
                        if (accessPoint3.getSsid() != null && accessPoint3.getSsid().equals(ssidStr)) {
                            z = true;
                            break;
                        }
                    }
                    if (z) {
                    }
                }
            }
            this.mAccessPoints = arrayList;
            this.mMainHandler.sendEmptyMessage(2);
        } catch (Throwable th) {
            mReadWriteLock.readLock().unlock();
            throw th;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateNetworkInfo(NetworkInfo networkInfo) {
        if (!this.mWifiManager.isWifiEnabled()) {
            this.mMainHandler.sendEmptyMessage(4);
            return;
        }
        if (networkInfo == null || networkInfo.getDetailedState() != NetworkInfo.DetailedState.OBTAINING_IPADDR) {
            this.mMainHandler.sendEmptyMessage(3);
        } else {
            this.mMainHandler.sendEmptyMessage(4);
        }
        mReadWriteLock.writeLock().lock();
        if (networkInfo != null) {
            try {
                this.mLastNetworkInfo = networkInfo;
            } catch (Throwable th) {
                mReadWriteLock.writeLock().unlock();
                throw th;
            }
        }
        mReadWriteLock.writeLock().unlock();
        boolean z = false;
        mReadWriteLock.readLock().lock();
        try {
            this.mLastInfo = this.mWifiManager.getConnectionInfo();
            WifiConfiguration wifiConfigurationForNetworkId = this.mLastInfo != null ? getWifiConfigurationForNetworkId(this.mLastInfo.getNetworkId()) : null;
            for (int size = this.mAccessPoints.size() - 1; size >= 0; size--) {
                if (this.mAccessPoints.get(size).update(wifiConfigurationForNetworkId, this.mLastInfo, this.mLastNetworkInfo)) {
                    z = true;
                }
            }
            mReadWriteLock.readLock().unlock();
            if (z) {
                synchronized (this.mAccessPoints) {
                    Collections.sort(this.mAccessPoints);
                }
                this.mMainHandler.sendEmptyMessage(2);
            }
        } catch (Throwable th2) {
            mReadWriteLock.readLock().unlock();
            throw th2;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateWifiState(int i) {
        this.mWorkHandler.obtainMessage(3, i, 0).sendToTarget();
    }

    public void dump(PrintWriter printWriter) {
        printWriter.println("  - wifi tracker ------");
        Iterator<T> it = getAccessPoints().iterator();
        while (it.hasNext()) {
            printWriter.println("  " + ((AccessPoint) it.next()));
        }
    }

    public void forceScan() {
        if (!this.mWifiManager.isWifiEnabled() || this.mScanner == null) {
            return;
        }
        this.mScanner.forceScan();
    }

    public List<AccessPoint> getAccessPoints() {
        ArrayList arrayList;
        synchronized (this.mAccessPoints) {
            arrayList = new ArrayList();
            for (AccessPoint accessPoint : this.mAccessPoints) {
                arrayList.add((AccessPoint) accessPoint.clone());
            }
        }
        return arrayList;
    }

    public WifiManager getManager() {
        return this.mWifiManager;
    }

    public void pauseScanning() {
        if (this.mScanner != null) {
            this.mScanner.pause();
            this.mScanner = null;
        }
    }

    public void resumeScanning() {
        if (this.mScanner == null) {
            this.mScanner = new Scanner(this);
        }
        this.mWorkHandler.sendEmptyMessage(2);
        if (this.mWifiManager.isWifiEnabled()) {
            this.mScanner.resume();
        }
        this.mWorkHandler.sendEmptyMessage(0);
    }

    public void startTracking() {
        resumeScanning();
        if (this.mRegistered) {
            return;
        }
        this.mContext.registerReceiver(this.mReceiver, this.mFilter);
        this.mNetworkCallback = new WifiTrackerNetworkCallback(this, null);
        this.mConnectivityManager.registerNetworkCallback(this.mNetworkRequest, this.mNetworkCallback);
        this.mRegistered = true;
    }

    public void stopTracking() {
        if (this.mRegistered) {
            this.mWorkHandler.removeMessages(0);
            this.mWorkHandler.removeMessages(1);
            this.mContext.unregisterReceiver(this.mReceiver);
            this.mConnectivityManager.unregisterNetworkCallback(this.mNetworkCallback);
            this.mRegistered = false;
        }
        pauseScanning();
    }
}
