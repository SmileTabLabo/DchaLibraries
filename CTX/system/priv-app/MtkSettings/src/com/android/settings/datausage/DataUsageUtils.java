package com.android.settings.datausage;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.INetworkStatsService;
import android.net.INetworkStatsSession;
import android.net.NetworkTemplate;
import android.net.TrafficStats;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.BidiFormatter;
import android.text.format.Formatter;
import java.util.List;
/* loaded from: classes.dex */
public final class DataUsageUtils {
    public static CharSequence formatDataUsage(Context context, long j) {
        Formatter.BytesResult formatBytes = Formatter.formatBytes(context.getResources(), j, 8);
        return BidiFormatter.getInstance().unicodeWrap(context.getString(17039893, formatBytes.value, formatBytes.units));
    }

    public static boolean hasEthernet(Context context) {
        long j;
        boolean isNetworkSupported = ConnectivityManager.from(context).isNetworkSupported(9);
        try {
            INetworkStatsSession openSession = INetworkStatsService.Stub.asInterface(ServiceManager.getService("netstats")).openSession();
            if (openSession != null) {
                j = openSession.getSummaryForNetwork(NetworkTemplate.buildTemplateEthernet(), Long.MIN_VALUE, Long.MAX_VALUE).getTotalBytes();
                TrafficStats.closeQuietly(openSession);
            } else {
                j = 0;
            }
            return isNetworkSupported && j > 0;
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean hasMobileData(Context context) {
        ConnectivityManager from = ConnectivityManager.from(context);
        return from != null && from.isNetworkSupported(0);
    }

    public static boolean hasWifiRadio(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(ConnectivityManager.class);
        return connectivityManager != null && connectivityManager.isNetworkSupported(1);
    }

    public static boolean hasSim(Context context) {
        int simState = ((TelephonyManager) context.getSystemService(TelephonyManager.class)).getSimState();
        return (simState == 1 || simState == 0) ? false : true;
    }

    public static int getDefaultSubscriptionId(Context context) {
        SubscriptionManager from = SubscriptionManager.from(context);
        if (from == null) {
            return -1;
        }
        SubscriptionInfo defaultDataSubscriptionInfo = from.getDefaultDataSubscriptionInfo();
        if (defaultDataSubscriptionInfo == null) {
            List<SubscriptionInfo> activeSubscriptionInfoList = from.getActiveSubscriptionInfoList();
            if (activeSubscriptionInfoList == null || activeSubscriptionInfoList.size() == 0) {
                return -1;
            }
            defaultDataSubscriptionInfo = activeSubscriptionInfoList.get(0);
        }
        return defaultDataSubscriptionInfo.getSubscriptionId();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static NetworkTemplate getDefaultTemplate(Context context, int i) {
        if (hasMobileData(context) && i != -1) {
            TelephonyManager from = TelephonyManager.from(context);
            return NetworkTemplate.normalize(NetworkTemplate.buildTemplateMobileAll(from.getSubscriberId(i)), from.getMergedSubscriberIds());
        } else if (hasWifiRadio(context)) {
            return NetworkTemplate.buildTemplateWifiWildcard();
        } else {
            return NetworkTemplate.buildTemplateEthernet();
        }
    }
}
