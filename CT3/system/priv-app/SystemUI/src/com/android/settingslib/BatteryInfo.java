package com.android.settingslib;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.BatteryStats;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.format.Formatter;
import android.util.SparseIntArray;
import com.android.internal.os.BatteryStatsHelper;
import com.android.settingslib.graph.UsageView;
/* loaded from: a.zip:com/android/settingslib/BatteryInfo.class */
public class BatteryInfo {
    public String batteryPercentString;
    public int mBatteryLevel;
    public String mChargeLabelString;
    private boolean mCharging;
    private BatteryStats mStats;
    public String remainingLabel;
    private long timePeriod;
    public boolean mDischarging = true;
    public long remainingTimeUs = 0;

    /* loaded from: a.zip:com/android/settingslib/BatteryInfo$BatteryDataParser.class */
    public interface BatteryDataParser {
        void onDataGap();

        void onDataPoint(long j, BatteryStats.HistoryItem historyItem);

        void onParsingDone();

        void onParsingStarted(long j, long j2);
    }

    /* loaded from: a.zip:com/android/settingslib/BatteryInfo$Callback.class */
    public interface Callback {
        void onBatteryInfoLoaded(BatteryInfo batteryInfo);
    }

    public static BatteryInfo getBatteryInfo(Context context, Intent intent, BatteryStats batteryStats, long j, boolean z) {
        BatteryInfo batteryInfo = new BatteryInfo();
        batteryInfo.mStats = batteryStats;
        batteryInfo.mBatteryLevel = Utils.getBatteryLevel(intent);
        batteryInfo.batteryPercentString = Utils.formatPercentage(batteryInfo.mBatteryLevel);
        batteryInfo.mCharging = intent.getIntExtra("plugged", 0) != 0;
        Resources resources = context.getResources();
        if (batteryInfo.mCharging) {
            long computeChargeTimeRemaining = batteryStats.computeChargeTimeRemaining(j);
            String batteryStatus = Utils.getBatteryStatus(resources, intent, z);
            int intExtra = intent.getIntExtra("status", 1);
            if (computeChargeTimeRemaining <= 0 || intExtra == 5) {
                batteryInfo.remainingLabel = batteryStatus;
                batteryInfo.mChargeLabelString = resources.getString(R$string.power_charging, batteryInfo.batteryPercentString, batteryStatus);
            } else {
                batteryInfo.mDischarging = false;
                batteryInfo.remainingTimeUs = computeChargeTimeRemaining;
                String formatShortElapsedTime = Formatter.formatShortElapsedTime(context, computeChargeTimeRemaining / 1000);
                int intExtra2 = intent.getIntExtra("plugged", 0);
                int i = intExtra2 == 1 ? z ? R$string.power_charging_duration_ac_short : R$string.power_charging_duration_ac : intExtra2 == 2 ? z ? R$string.power_charging_duration_usb_short : R$string.power_charging_duration_usb : intExtra2 == 4 ? z ? R$string.power_charging_duration_wireless_short : R$string.power_charging_duration_wireless : z ? R$string.power_charging_duration_short : R$string.power_charging_duration;
                batteryInfo.remainingLabel = resources.getString(R$string.power_remaining_duration_only, formatShortElapsedTime);
                batteryInfo.mChargeLabelString = resources.getString(i, batteryInfo.batteryPercentString, formatShortElapsedTime);
            }
        } else {
            long computeBatteryTimeRemaining = batteryStats.computeBatteryTimeRemaining(j);
            if (computeBatteryTimeRemaining > 0) {
                batteryInfo.remainingTimeUs = computeBatteryTimeRemaining;
                String formatShortElapsedTime2 = Formatter.formatShortElapsedTime(context, computeBatteryTimeRemaining / 1000);
                batteryInfo.remainingLabel = resources.getString(z ? R$string.power_remaining_duration_only_short : R$string.power_remaining_duration_only, formatShortElapsedTime2);
                batteryInfo.mChargeLabelString = resources.getString(z ? R$string.power_discharging_duration_short : R$string.power_discharging_duration, batteryInfo.batteryPercentString, formatShortElapsedTime2);
            } else {
                batteryInfo.remainingLabel = null;
                batteryInfo.mChargeLabelString = batteryInfo.batteryPercentString;
            }
        }
        return batteryInfo;
    }

    public static void getBatteryInfo(Context context, Callback callback) {
        getBatteryInfo(context, callback, false);
    }

    /* JADX WARN: Type inference failed for: r0v0, types: [com.android.settingslib.BatteryInfo$2] */
    public static void getBatteryInfo(Context context, Callback callback, boolean z) {
        new AsyncTask<Void, Void, BatteryStats>(context, z, callback) { // from class: com.android.settingslib.BatteryInfo.2
            final Callback val$callback;
            final Context val$context;
            final boolean val$shortString;

            {
                this.val$context = context;
                this.val$shortString = z;
                this.val$callback = callback;
            }

            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public BatteryStats doInBackground(Void... voidArr) {
                BatteryStatsHelper batteryStatsHelper = new BatteryStatsHelper(this.val$context, true);
                batteryStatsHelper.create((Bundle) null);
                return batteryStatsHelper.getStats();
            }

            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public void onPostExecute(BatteryStats batteryStats) {
                long elapsedRealtime = SystemClock.elapsedRealtime();
                this.val$callback.onBatteryInfoLoaded(BatteryInfo.getBatteryInfo(this.val$context, this.val$context.registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED")), batteryStats, elapsedRealtime * 1000, this.val$shortString));
            }
        }.execute(new Void[0]);
    }

    /* JADX WARN: Code restructure failed: missing block: B:13:0x009f, code lost:
        if (r0.cmd == 7) goto L25;
     */
    /* JADX WARN: Code restructure failed: missing block: B:55:0x0225, code lost:
        if (r0.cmd == 7) goto L76;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private static void parse(BatteryStats batteryStats, long j, BatteryDataParser... batteryDataParserArr) {
        long j2;
        long j3;
        long j4;
        long j5;
        long j6;
        long j7;
        long j8 = 0;
        long j9 = 0;
        long j10 = 0;
        byte b = -1;
        long j11 = 0;
        long j12 = 0;
        int i = 0;
        int i2 = 0;
        int i3 = 0;
        boolean z = true;
        long j13 = 0;
        long j14 = 0;
        long j15 = 0;
        long j16 = 0;
        long j17 = 0;
        if (batteryStats.startIteratingHistoryLocked()) {
            BatteryStats.HistoryItem historyItem = new BatteryStats.HistoryItem();
            while (true) {
                j13 = j10;
                j14 = j9;
                i = i2;
                j15 = j12;
                j16 = j11;
                j17 = j8;
                if (!batteryStats.getNextHistoryLocked(historyItem)) {
                    break;
                }
                int i4 = i3 + 1;
                boolean z2 = z;
                long j18 = j9;
                if (z) {
                    z2 = false;
                    j18 = historyItem.time;
                }
                if (historyItem.cmd != 5) {
                    j5 = j12;
                    j6 = j11;
                    j7 = j8;
                }
                j8 = (historyItem.currentTime > 15552000000L + j11 || historyItem.time < 300000 + j18) ? 0L : 0L;
                long j19 = historyItem.currentTime;
                long j20 = historyItem.time;
                j5 = j20;
                j6 = j19;
                j7 = j8;
                if (j8 == 0) {
                    j7 = j19 - (j20 - j18);
                    j6 = j19;
                    j5 = j20;
                }
                z = z2;
                j9 = j18;
                j12 = j5;
                j11 = j6;
                i3 = i4;
                j8 = j7;
                if (historyItem.isDeltaData()) {
                    if (historyItem.batteryLevel != b || i4 == 1) {
                        b = historyItem.batteryLevel;
                    }
                    i2 = i4;
                    j10 = historyItem.time;
                    z = z2;
                    j9 = j18;
                    j12 = j5;
                    j11 = j6;
                    i3 = i4;
                    j8 = j7;
                }
            }
        }
        batteryStats.finishIteratingHistoryLocked();
        long j21 = (j16 + j13) - j15;
        long j22 = j / 1000;
        for (BatteryDataParser batteryDataParser : batteryDataParserArr) {
            batteryDataParser.onParsingStarted(j17, j21 + j22);
        }
        if (j21 > j17 && batteryStats.startIteratingHistoryLocked()) {
            BatteryStats.HistoryItem historyItem2 = new BatteryStats.HistoryItem();
            long j23 = j15;
            int i5 = 0;
            long j24 = 0;
            while (true) {
                long j25 = j24;
                if (!batteryStats.getNextHistoryLocked(historyItem2) || i5 >= i) {
                    break;
                }
                if (historyItem2.isDeltaData()) {
                    long j26 = j25 + (historyItem2.time - j23);
                    long j27 = historyItem2.time;
                    long j28 = j26 - j17;
                    long j29 = j28;
                    if (j28 < 0) {
                        j29 = 0;
                    }
                    int i6 = 0;
                    while (true) {
                        j4 = j26;
                        j23 = j27;
                        if (i6 < batteryDataParserArr.length) {
                            batteryDataParserArr[i6].onDataPoint(j29, historyItem2);
                            i6++;
                        }
                    }
                } else {
                    if (historyItem2.cmd != 5) {
                        j2 = j25;
                        j3 = j23;
                    }
                    j2 = historyItem2.currentTime >= j17 ? historyItem2.currentTime : j17 + (historyItem2.time - j14);
                    j3 = historyItem2.time;
                    long j30 = j2;
                    j4 = j30;
                    j23 = j3;
                    if (historyItem2.cmd != 6) {
                        if (historyItem2.cmd == 5) {
                            j4 = j30;
                            j23 = j3;
                            if (Math.abs(j25 - j30) <= 3600000) {
                            }
                        }
                        int i7 = 0;
                        while (true) {
                            j4 = j30;
                            j23 = j3;
                            if (i7 < batteryDataParserArr.length) {
                                batteryDataParserArr[i7].onDataGap();
                                i7++;
                            }
                        }
                    }
                }
                i5++;
                j24 = j4;
            }
        }
        batteryStats.finishIteratingHistoryLocked();
        for (BatteryDataParser batteryDataParser2 : batteryDataParserArr) {
            batteryDataParser2.onParsingDone();
        }
    }

    public void bindHistory(UsageView usageView, BatteryDataParser... batteryDataParserArr) {
        BatteryDataParser batteryDataParser = new BatteryDataParser(this, usageView) { // from class: com.android.settingslib.BatteryInfo.1
            SparseIntArray points = new SparseIntArray();
            final BatteryInfo this$0;
            final UsageView val$view;

            {
                this.this$0 = this;
                this.val$view = usageView;
            }

            @Override // com.android.settingslib.BatteryInfo.BatteryDataParser
            public void onDataGap() {
                if (this.points.size() > 1) {
                    this.val$view.addPath(this.points);
                }
                this.points.clear();
            }

            @Override // com.android.settingslib.BatteryInfo.BatteryDataParser
            public void onDataPoint(long j, BatteryStats.HistoryItem historyItem) {
                this.points.put((int) j, historyItem.batteryLevel);
            }

            @Override // com.android.settingslib.BatteryInfo.BatteryDataParser
            public void onParsingDone() {
                if (this.points.size() > 1) {
                    this.val$view.addPath(this.points);
                }
            }

            @Override // com.android.settingslib.BatteryInfo.BatteryDataParser
            public void onParsingStarted(long j, long j2) {
                this.this$0.timePeriod = (j2 - j) - (this.this$0.remainingTimeUs / 1000);
                this.val$view.clearPaths();
                this.val$view.configureGraph((int) (j2 - j), 100, this.this$0.remainingTimeUs != 0, this.this$0.mCharging);
            }
        };
        BatteryDataParser[] batteryDataParserArr2 = new BatteryDataParser[batteryDataParserArr.length + 1];
        for (int i = 0; i < batteryDataParserArr.length; i++) {
            batteryDataParserArr2[i] = batteryDataParserArr[i];
        }
        batteryDataParserArr2[batteryDataParserArr.length] = batteryDataParser;
        parse(this.mStats, this.remainingTimeUs, batteryDataParserArr2);
        Context context = usageView.getContext();
        usageView.setBottomLabels(new CharSequence[]{context.getString(R$string.charge_length_format, Formatter.formatShortElapsedTime(context, this.timePeriod)), this.remainingTimeUs != 0 ? context.getString(R$string.remaining_length_format, Formatter.formatShortElapsedTime(context, this.remainingTimeUs / 1000)) : ""});
    }
}
