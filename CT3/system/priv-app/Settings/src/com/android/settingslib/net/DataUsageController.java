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
/* loaded from: classes.dex */
public class DataUsageController {
    private static final boolean DEBUG = Log.isLoggable("DataUsageController", 3);
    private static final StringBuilder PERIOD_BUILDER = new StringBuilder(50);
    private static final Formatter PERIOD_FORMATTER = new Formatter(PERIOD_BUILDER, Locale.getDefault());
    private final ConnectivityManager mConnectivityManager;
    private final Context mContext;
    private NetworkNameProvider mNetworkController;
    private final NetworkPolicyManager mPolicyManager;
    private INetworkStatsSession mSession;
    private final INetworkStatsService mStatsService = INetworkStatsService.Stub.asInterface(ServiceManager.getService("netstats"));
    private final TelephonyManager mTelephonyManager;

    /* loaded from: classes.dex */
    public static class DataUsageInfo {
        public String carrier;
        public long limitLevel;
        public String period;
        public long startDate;
        public long usageLevel;
        public long warningLevel;
    }

    /* loaded from: classes.dex */
    public interface NetworkNameProvider {
        String getMobileDataNetworkName();
    }

    public DataUsageController(Context context) {
        this.mContext = context;
        this.mTelephonyManager = TelephonyManager.from(context);
        this.mConnectivityManager = ConnectivityManager.from(context);
        this.mPolicyManager = NetworkPolicyManager.from(this.mContext);
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

    private DataUsageInfo warn(String msg) {
        Log.w("DataUsageController", "Failed to get data usage, " + msg);
        return null;
    }

    private static Time addMonth(Time t, int months) {
        Time rt = new Time(t);
        rt.set(t.monthDay, t.month + months, t.year);
        rt.normalize(false);
        return rt;
    }

    public DataUsageInfo getDataUsageInfo() {
        String subscriberId = getActiveSubscriberId(this.mContext);
        if (subscriberId == null) {
            return warn("no subscriber id");
        }
        NetworkTemplate template = NetworkTemplate.buildTemplateMobileAll(subscriberId);
        return getDataUsageInfo(NetworkTemplate.normalize(template, this.mTelephonyManager.getMergedSubscriberIds()));
    }

    public DataUsageInfo getDataUsageInfo(NetworkTemplate template) {
        long end;
        long start;
        INetworkStatsSession session = getSession();
        if (session == null) {
            return warn("no stats session");
        }
        NetworkPolicy policy = findNetworkPolicy(template);
        try {
            NetworkStatsHistory history = session.getHistoryForNetwork(template, 10);
            long now = System.currentTimeMillis();
            if (policy != null && policy.cycleDay > 0) {
                if (DEBUG) {
                    Log.d("DataUsageController", "Cycle day=" + policy.cycleDay + " tz=" + policy.cycleTimezone);
                }
                Time nowTime = new Time(policy.cycleTimezone);
                nowTime.setToNow();
                Time policyTime = new Time(nowTime);
                policyTime.set(policy.cycleDay, policyTime.month, policyTime.year);
                policyTime.normalize(false);
                if (nowTime.after(policyTime)) {
                    start = policyTime.toMillis(false);
                    end = addMonth(policyTime, 1).toMillis(false);
                } else {
                    start = addMonth(policyTime, -1).toMillis(false);
                    end = policyTime.toMillis(false);
                }
            } else {
                end = now;
                start = now - 2419200000L;
            }
            long callStart = System.currentTimeMillis();
            NetworkStatsHistory.Entry entry = history.getValues(start, end, now, (NetworkStatsHistory.Entry) null);
            long callEnd = System.currentTimeMillis();
            if (DEBUG) {
                Log.d("DataUsageController", String.format("history call from %s to %s now=%s took %sms: %s", new Date(start), new Date(end), new Date(now), Long.valueOf(callEnd - callStart), historyEntryToString(entry)));
            }
            if (entry == null) {
                return warn("no entry data");
            }
            long totalBytes = entry.rxBytes + entry.txBytes;
            DataUsageInfo usage = new DataUsageInfo();
            usage.startDate = start;
            usage.usageLevel = totalBytes;
            usage.period = formatDateRange(start, end);
            if (policy != null) {
                usage.limitLevel = policy.limitBytes > 0 ? policy.limitBytes : 0L;
                usage.warningLevel = policy.warningBytes > 0 ? policy.warningBytes : 0L;
            } else {
                usage.warningLevel = 2147483648L;
            }
            if (usage != null && this.mNetworkController != null) {
                usage.carrier = this.mNetworkController.getMobileDataNetworkName();
            }
            return usage;
        } catch (RemoteException e) {
            return warn("remote call failed");
        }
    }

    private NetworkPolicy findNetworkPolicy(NetworkTemplate template) {
        NetworkPolicy[] policies;
        if (this.mPolicyManager == null || template == null || (policies = this.mPolicyManager.getNetworkPolicies()) == null) {
            return null;
        }
        for (NetworkPolicy policy : policies) {
            if (policy != null && template.equals(policy.template)) {
                return policy;
            }
        }
        return null;
    }

    private static String historyEntryToString(NetworkStatsHistory.Entry entry) {
        if (entry == null) {
            return null;
        }
        return "Entry[bucketDuration=" + entry.bucketDuration + ",bucketStart=" + entry.bucketStart + ",activeTime=" + entry.activeTime + ",rxBytes=" + entry.rxBytes + ",rxPackets=" + entry.rxPackets + ",txBytes=" + entry.txBytes + ",txPackets=" + entry.txPackets + ",operations=" + entry.operations + ']';
    }

    private static String getActiveSubscriberId(Context context) {
        TelephonyManager tele = TelephonyManager.from(context);
        String actualSubscriberId = tele.getSubscriberId(SubscriptionManager.getDefaultDataSubscriptionId());
        return actualSubscriberId;
    }

    private String formatDateRange(long start, long end) {
        String formatter;
        synchronized (PERIOD_BUILDER) {
            PERIOD_BUILDER.setLength(0);
            formatter = DateUtils.formatDateRange(this.mContext, PERIOD_FORMATTER, start, end, 65552, null).toString();
        }
        return formatter;
    }
}
