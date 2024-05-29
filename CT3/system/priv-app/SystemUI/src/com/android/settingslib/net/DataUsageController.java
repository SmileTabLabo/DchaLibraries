package com.android.settingslib.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.INetworkStatsService;
import android.net.INetworkStatsSession;
import android.net.NetworkPolicy;
import android.net.NetworkPolicyManager;
import android.net.NetworkStatsHistory;
import android.net.NetworkTemplate;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;
/* loaded from: a.zip:com/android/settingslib/net/DataUsageController.class */
public class DataUsageController {
    private static final boolean DEBUG = Log.isLoggable("DataUsageController", 3);
    private static final StringBuilder PERIOD_BUILDER = new StringBuilder(50);
    private static final Formatter PERIOD_FORMATTER = new Formatter(PERIOD_BUILDER, Locale.getDefault());
    private Callback mCallback;
    private final ConnectivityManager mConnectivityManager;
    private final Context mContext;
    private NetworkNameProvider mNetworkController;
    private final NetworkPolicyManager mPolicyManager;
    private INetworkStatsSession mSession;
    private final INetworkStatsService mStatsService = INetworkStatsService.Stub.asInterface(ServiceManager.getService("netstats"));
    private final TelephonyManager mTelephonyManager;

    /* loaded from: a.zip:com/android/settingslib/net/DataUsageController$Callback.class */
    public interface Callback {
        void onMobileDataEnabled(boolean z);
    }

    /* loaded from: a.zip:com/android/settingslib/net/DataUsageController$DataUsageInfo.class */
    public static class DataUsageInfo {
        public String carrier;
        public long limitLevel;
        public String period;
        public long startDate;
        public long usageLevel;
        public long warningLevel;
    }

    /* loaded from: a.zip:com/android/settingslib/net/DataUsageController$NetworkNameProvider.class */
    public interface NetworkNameProvider {
        String getMobileDataNetworkName();
    }

    public DataUsageController(Context context) {
        this.mContext = context;
        this.mTelephonyManager = TelephonyManager.from(context);
        this.mConnectivityManager = ConnectivityManager.from(context);
        this.mPolicyManager = NetworkPolicyManager.from(this.mContext);
    }

    private static Time addMonth(Time time, int i) {
        Time time2 = new Time(time);
        time2.set(time.monthDay, time.month + i, time.year);
        time2.normalize(false);
        return time2;
    }

    private NetworkPolicy findNetworkPolicy(NetworkTemplate networkTemplate) {
        NetworkPolicy[] networkPolicies;
        if (this.mPolicyManager == null || networkTemplate == null || (networkPolicies = this.mPolicyManager.getNetworkPolicies()) == null) {
            return null;
        }
        for (NetworkPolicy networkPolicy : networkPolicies) {
            if (networkPolicy != null && networkTemplate.equals(networkPolicy.template)) {
                return networkPolicy;
            }
        }
        return null;
    }

    private String formatDateRange(long j, long j2) {
        String formatter;
        synchronized (PERIOD_BUILDER) {
            PERIOD_BUILDER.setLength(0);
            formatter = DateUtils.formatDateRange(this.mContext, PERIOD_FORMATTER, j, j2, 65552, null).toString();
        }
        return formatter;
    }

    private static String getActiveSubscriberId(Context context) {
        return TelephonyManager.from(context).getSubscriberId(SubscriptionManager.getDefaultDataSubscriptionId());
    }

    private INetworkStatsSession getSession() {
        if (this.mSession == null) {
            try {
                this.mSession = this.mStatsService.openSession();
            } catch (RemoteException e) {
                Log.w("DataUsageController", "Failed to open stats session", e);
            } catch (RuntimeException e2) {
                Log.w("DataUsageController", "Failed to open stats session", e2);
            }
        }
        return this.mSession;
    }

    private static String historyEntryToString(NetworkStatsHistory.Entry entry) {
        return entry == null ? null : "Entry[bucketDuration=" + entry.bucketDuration + ",bucketStart=" + entry.bucketStart + ",activeTime=" + entry.activeTime + ",rxBytes=" + entry.rxBytes + ",rxPackets=" + entry.rxPackets + ",txBytes=" + entry.txBytes + ",txPackets=" + entry.txPackets + ",operations=" + entry.operations + ']';
    }

    private DataUsageInfo warn(String str) {
        Log.w("DataUsageController", "Failed to get data usage, " + str);
        return null;
    }

    public DataUsageInfo getDataUsageInfo() {
        String activeSubscriberId = getActiveSubscriberId(this.mContext);
        return activeSubscriberId == null ? warn("no subscriber id") : getDataUsageInfo(NetworkTemplate.normalize(NetworkTemplate.buildTemplateMobileAll(activeSubscriberId), this.mTelephonyManager.getMergedSubscriberIds()));
    }

    public DataUsageInfo getDataUsageInfo(NetworkTemplate networkTemplate) {
        long j;
        long j2;
        INetworkStatsSession session = getSession();
        if (session == null) {
            return warn("no stats session");
        }
        NetworkPolicy findNetworkPolicy = findNetworkPolicy(networkTemplate);
        try {
            NetworkStatsHistory historyForNetwork = session.getHistoryForNetwork(networkTemplate, 10);
            long currentTimeMillis = System.currentTimeMillis();
            if (findNetworkPolicy == null || findNetworkPolicy.cycleDay <= 0) {
                j = currentTimeMillis;
                j2 = currentTimeMillis - 2419200000L;
            } else {
                if (DEBUG) {
                    Log.d("DataUsageController", "Cycle day=" + findNetworkPolicy.cycleDay + " tz=" + findNetworkPolicy.cycleTimezone);
                }
                Time time = new Time(findNetworkPolicy.cycleTimezone);
                time.setToNow();
                Time time2 = new Time(time);
                time2.set(findNetworkPolicy.cycleDay, time2.month, time2.year);
                time2.normalize(false);
                if (time.after(time2)) {
                    j2 = time2.toMillis(false);
                    j = addMonth(time2, 1).toMillis(false);
                } else {
                    j2 = addMonth(time2, -1).toMillis(false);
                    j = time2.toMillis(false);
                }
            }
            long currentTimeMillis2 = System.currentTimeMillis();
            NetworkStatsHistory.Entry values = historyForNetwork.getValues(j2, j, currentTimeMillis, (NetworkStatsHistory.Entry) null);
            long currentTimeMillis3 = System.currentTimeMillis();
            if (DEBUG) {
                Log.d("DataUsageController", String.format("history call from %s to %s now=%s took %sms: %s", new Date(j2), new Date(j), new Date(currentTimeMillis), Long.valueOf(currentTimeMillis3 - currentTimeMillis2), historyEntryToString(values)));
            }
            if (values == null) {
                return warn("no entry data");
            }
            long j3 = values.rxBytes;
            long j4 = values.txBytes;
            DataUsageInfo dataUsageInfo = new DataUsageInfo();
            dataUsageInfo.startDate = j2;
            dataUsageInfo.usageLevel = j3 + j4;
            dataUsageInfo.period = formatDateRange(j2, j);
            if (findNetworkPolicy != null) {
                dataUsageInfo.limitLevel = findNetworkPolicy.limitBytes > 0 ? findNetworkPolicy.limitBytes : 0L;
                dataUsageInfo.warningLevel = findNetworkPolicy.warningBytes > 0 ? findNetworkPolicy.warningBytes : 0L;
            } else {
                dataUsageInfo.warningLevel = 2147483648L;
            }
            if (dataUsageInfo != null && this.mNetworkController != null) {
                dataUsageInfo.carrier = this.mNetworkController.getMobileDataNetworkName();
            }
            return dataUsageInfo;
        } catch (RemoteException e) {
            return warn("remote call failed");
        }
    }

    public boolean isMobileDataEnabled() {
        return this.mTelephonyManager.getDataEnabled();
    }

    public boolean isMobileDataSupported() {
        boolean z = false;
        if (this.mConnectivityManager.isNetworkSupported(0)) {
            z = false;
            if (this.mTelephonyManager.getSimState() == 5) {
                z = true;
            }
        }
        return z;
    }

    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }

    public void setMobileDataEnabled(boolean z) {
        Log.d("DataUsageController", "setMobileDataEnabled: enabled=" + z);
        this.mTelephonyManager.setDataEnabled(z);
        if (this.mCallback != null) {
            this.mCallback.onMobileDataEnabled(z);
        }
    }

    public void setNetworkController(NetworkNameProvider networkNameProvider) {
        this.mNetworkController = networkNameProvider;
    }
}
