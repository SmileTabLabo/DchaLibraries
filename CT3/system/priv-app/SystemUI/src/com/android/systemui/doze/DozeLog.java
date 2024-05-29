package com.android.systemui.doze;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.util.TimeUtils;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
/* loaded from: a.zip:com/android/systemui/doze/DozeLog.class */
public class DozeLog {
    private static final boolean DEBUG = Log.isLoggable("DozeLog", 3);
    private static final SimpleDateFormat FORMAT;
    private static final int SIZE;
    private static int sCount;
    private static SummaryStats sEmergencyCallStats;
    private static final KeyguardUpdateMonitorCallback sKeyguardCallback;
    private static String[] sMessages;
    private static SummaryStats sNotificationPulseStats;
    private static SummaryStats sPickupPulseNearVibrationStats;
    private static SummaryStats sPickupPulseNotNearVibrationStats;
    private static int sPosition;
    private static SummaryStats[][] sProxStats;
    private static boolean sPulsing;
    private static SummaryStats sScreenOnNotPulsingStats;
    private static SummaryStats sScreenOnPulsingStats;
    private static long sSince;
    private static long[] sTimes;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/doze/DozeLog$SummaryStats.class */
    public static class SummaryStats {
        private int mCount;

        private SummaryStats() {
        }

        /* synthetic */ SummaryStats(SummaryStats summaryStats) {
            this();
        }

        public void append() {
            this.mCount++;
        }

        public void dump(PrintWriter printWriter, String str) {
            if (this.mCount == 0) {
                return;
            }
            printWriter.print("    ");
            printWriter.print(str);
            printWriter.print(": n=");
            printWriter.print(this.mCount);
            printWriter.print(" (");
            printWriter.print((this.mCount / (System.currentTimeMillis() - DozeLog.sSince)) * 1000.0d * 60.0d * 60.0d);
            printWriter.print("/hr)");
            printWriter.println();
        }
    }

    static {
        SIZE = Build.IS_DEBUGGABLE ? 400 : 50;
        FORMAT = new SimpleDateFormat("MM-dd HH:mm:ss.SSS");
        sKeyguardCallback = new KeyguardUpdateMonitorCallback() { // from class: com.android.systemui.doze.DozeLog.1
            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onEmergencyCallAction() {
                DozeLog.traceEmergencyCall();
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onFinishedGoingToSleep(int i) {
                DozeLog.traceScreenOff(i);
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onKeyguardBouncerChanged(boolean z) {
                DozeLog.traceKeyguardBouncerChanged(z);
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onKeyguardVisibilityChanged(boolean z) {
                DozeLog.traceKeyguard(z);
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onStartedWakingUp() {
                DozeLog.traceScreenOn();
            }
        };
    }

    public static void dump(PrintWriter printWriter) {
        synchronized (DozeLog.class) {
            try {
                if (sMessages == null) {
                    return;
                }
                printWriter.println("  Doze log:");
                int i = sPosition;
                int i2 = sCount;
                int i3 = SIZE;
                int i4 = SIZE;
                for (int i5 = 0; i5 < sCount; i5++) {
                    int i6 = ((((i - i2) + i3) % i4) + i5) % SIZE;
                    printWriter.print("    ");
                    printWriter.print(FORMAT.format(new Date(sTimes[i6])));
                    printWriter.print(' ');
                    printWriter.println(sMessages[i6]);
                }
                printWriter.print("  Doze summary stats (for ");
                TimeUtils.formatDuration(System.currentTimeMillis() - sSince, printWriter);
                printWriter.println("):");
                sPickupPulseNearVibrationStats.dump(printWriter, "Pickup pulse (near vibration)");
                sPickupPulseNotNearVibrationStats.dump(printWriter, "Pickup pulse (not near vibration)");
                sNotificationPulseStats.dump(printWriter, "Notification pulse");
                sScreenOnPulsingStats.dump(printWriter, "Screen on (pulsing)");
                sScreenOnNotPulsingStats.dump(printWriter, "Screen on (not pulsing)");
                sEmergencyCallStats.dump(printWriter, "Emergency call");
                for (int i7 = 0; i7 < 4; i7++) {
                    String pulseReasonToString = pulseReasonToString(i7);
                    sProxStats[i7][0].dump(printWriter, "Proximity near (" + pulseReasonToString + ")");
                    sProxStats[i7][1].dump(printWriter, "Proximity far (" + pulseReasonToString + ")");
                }
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    private static void init(Context context) {
        synchronized (DozeLog.class) {
            try {
                if (sMessages == null) {
                    sTimes = new long[SIZE];
                    sMessages = new String[SIZE];
                    sSince = System.currentTimeMillis();
                    sPickupPulseNearVibrationStats = new SummaryStats(null);
                    sPickupPulseNotNearVibrationStats = new SummaryStats(null);
                    sNotificationPulseStats = new SummaryStats(null);
                    sScreenOnPulsingStats = new SummaryStats(null);
                    sScreenOnNotPulsingStats = new SummaryStats(null);
                    sEmergencyCallStats = new SummaryStats(null);
                    sProxStats = new SummaryStats[4][2];
                    for (int i = 0; i < 4; i++) {
                        sProxStats[i][0] = new SummaryStats(null);
                        sProxStats[i][1] = new SummaryStats(null);
                    }
                    log("init");
                    KeyguardUpdateMonitor.getInstance(context).registerCallback(sKeyguardCallback);
                }
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    private static void log(String str) {
        synchronized (DozeLog.class) {
            try {
                if (sMessages == null) {
                    return;
                }
                sTimes[sPosition] = System.currentTimeMillis();
                sMessages[sPosition] = str;
                sPosition = (sPosition + 1) % SIZE;
                sCount = Math.min(sCount + 1, SIZE);
                if (DEBUG) {
                    Log.d("DozeLog", str);
                }
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    public static String pulseReasonToString(int i) {
        switch (i) {
            case 0:
                return "intent";
            case 1:
                return "notification";
            case 2:
                return "sigmotion";
            case 3:
                return "pickup";
            default:
                throw new IllegalArgumentException("bad reason: " + i);
        }
    }

    public static void traceDozing(Context context, boolean z) {
        sPulsing = false;
        init(context);
        log("dozing " + z);
    }

    public static void traceEmergencyCall() {
        log("emergencyCall");
        sEmergencyCallStats.append();
    }

    public static void traceFling(boolean z, boolean z2, boolean z3, boolean z4) {
        log("fling expand=" + z + " aboveThreshold=" + z2 + " thresholdNeeded=" + z3 + " screenOnFromTouch=" + z4);
    }

    public static void traceKeyguard(boolean z) {
        log("keyguard " + z);
        if (z) {
            return;
        }
        sPulsing = false;
    }

    public static void traceKeyguardBouncerChanged(boolean z) {
        log("bouncer " + z);
    }

    public static void traceNotificationPulse(long j) {
        log("notificationPulse instance=" + j);
        sNotificationPulseStats.append();
    }

    public static void tracePickupPulse(boolean z) {
        log("pickupPulse withinVibrationThreshold=" + z);
        (z ? sPickupPulseNearVibrationStats : sPickupPulseNotNearVibrationStats).append();
    }

    public static void traceProximityResult(Context context, boolean z, long j, int i) {
        log("proximityResult reason=" + pulseReasonToString(i) + " near=" + z + " millis=" + j);
        init(context);
        sProxStats[i][(z ? null : 1) == 1 ? 1 : 0].append();
    }

    public static void tracePulseFinish() {
        sPulsing = false;
        log("pulseFinish");
    }

    public static void tracePulseStart(int i) {
        sPulsing = true;
        log("pulseStart reason=" + pulseReasonToString(i));
    }

    public static void traceScreenOff(int i) {
        log("screenOff why=" + i);
    }

    public static void traceScreenOn() {
        log("screenOn pulsing=" + sPulsing);
        (sPulsing ? sScreenOnPulsingStats : sScreenOnNotPulsingStats).append();
        sPulsing = false;
    }
}
