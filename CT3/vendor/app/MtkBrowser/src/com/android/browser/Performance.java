package com.android.browser;

import android.app.ActivityManager;
import android.content.Context;
import android.net.ParseException;
import android.net.WebAddress;
import android.os.Debug;
import android.util.Log;
import com.android.internal.util.MemInfoReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;
/* loaded from: b.zip:com/android/browser/Performance.class */
public class Performance {
    private static boolean mInTrace;
    private static ActivityManager.MemoryInfo mSysMemThreshold;
    private static final boolean LOGD_ENABLED = Browser.DEBUG;
    private static long mTotalMem = 0;
    private static long mVisibleAppThreshold = 0;
    private static final Object mLock = new Object();
    private static final int[] SYSTEM_CPU_FORMAT = {288, 8224, 8224, 8224, 8224, 8224, 8224, 8224};

    /* JADX INFO: Access modifiers changed from: package-private */
    public static boolean checkShouldReleaseTabs(int i, ArrayList<Integer> arrayList, boolean z, String str, CopyOnWriteArrayList<Integer> copyOnWriteArrayList, boolean z2) {
        Debug.MemoryInfo memoryInfo;
        boolean z3;
        NumberFormat numberFormat;
        synchronized (mLock) {
            String str2 = "MemoryDumpInfo" + System.currentTimeMillis();
            Log.d("browser", "Browser Current Memory Dump time = " + str2);
            if (LOGD_ENABLED) {
                if (z) {
                    if (!z2) {
                        Log.d("browser", str2 + " Performance#checkShouldReleaseTabs()-->tabPosition = " + arrayList + ", url = " + str);
                    }
                } else if (z2) {
                    Log.d("browser", str2 + " Perfromance#checkShouldReleaseTabs()--->removeTabIndex = " + arrayList);
                } else {
                    Log.d("browser", str2 + " Performance#checkShouldReleaseTabs()-->freeTabIndex = " + copyOnWriteArrayList);
                }
            }
            MemInfoReader memInfoReader = new MemInfoReader();
            memInfoReader.readMemInfo();
            if (LOGD_ENABLED) {
                printProcessMemInfo(memInfoReader, str2);
                printMemoryInfo(false, str2);
            }
            Debug.getMemoryInfo(new Debug.MemoryInfo());
            double totalPss = ((memoryInfo.getTotalPss() + memoryInfo.getSummaryTotalSwap()) * 1024.0d) / mTotalMem;
            if (LOGD_ENABLED) {
                NumberFormat.getInstance().setMaximumFractionDigits(3);
                Log.d("browser", str2 + " current porcess take up the memory percent is " + numberFormat.format(totalPss));
            }
            z3 = false;
            if (Math.max(memInfoReader.getFreeSize(), memInfoReader.getCachedSize()) < mVisibleAppThreshold) {
                if (LOGD_ENABLED) {
                    Log.d("browser", "Browser Pss =: " + (memoryInfo.getTotalPss() / 1024.0d) + " PSwap =: " + (memoryInfo.getTotalSwappedOut() / 1024.0f) + " SwappablePss =: " + (memoryInfo.getTotalSwappablePss() / 1024.0f));
                }
                z3 = false;
                if (totalPss > 0.4000000059604645d) {
                    z3 = false;
                    if (i > 5) {
                        z3 = false;
                        if (z) {
                            z3 = true;
                        }
                    }
                }
            }
        }
        return z3;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void dumpSystemMemInfo(Context context) {
        if (context == null || mSysMemThreshold != null) {
            return;
        }
        mSysMemThreshold = new ActivityManager.MemoryInfo();
        ((ActivityManager) context.getSystemService("activity")).getMemoryInfo(mSysMemThreshold);
        mTotalMem = mSysMemThreshold.totalMem;
        mVisibleAppThreshold = mSysMemThreshold.visibleAppThreshold;
        if (LOGD_ENABLED) {
            String str = "MemoryDumpInfo" + System.currentTimeMillis();
            Log.d("browser", "Browser Current Memory Dump time = " + str);
            printSysMemInfo(mSysMemThreshold, str);
        }
    }

    static String encodeToJSON(Debug.MemoryInfo memoryInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\r\n").append("    \"Browser app (MB)\": {\r\n").append("        \"Browser\": {\r\n").append("            \"Pss\": {\r\n").append(String.format("                \"DVM\": %.2f,\r\n", Double.valueOf(memoryInfo.dalvikPss / 1024.0d))).append(String.format("                \"Native\": %.2f,\r\n", Double.valueOf(memoryInfo.nativePss / 1024.0d))).append(String.format("                \"Other\": %.2f,\r\n", Double.valueOf(memoryInfo.otherPss / 1024.0d))).append(String.format("                \"Total\": %.2f\r\n", Double.valueOf(memoryInfo.getTotalPss() / 1024.0d))).append("            },\r\n").append("            \"Private\": {\r\n").append(String.format("                \"DVM\": %.2f,\r\n", Double.valueOf(memoryInfo.dalvikPrivateDirty / 1024.0d))).append(String.format("                \"Native\": %.2f,\r\n", Double.valueOf(memoryInfo.nativePrivateDirty / 1024.0d))).append(String.format("                \"Other\": %.2f,\r\n", Double.valueOf(memoryInfo.otherPrivateDirty / 1024.0d))).append(String.format("                \"Total\": %.2f\r\n", Double.valueOf(memoryInfo.getTotalPrivateDirty() / 1024.0d))).append("            },\r\n").append("            \"Swapped\": {\r\n").append(String.format("                \"DVM\": %.2f,\r\n", Double.valueOf(memoryInfo.dalvikSwappedOut / 1024.0d))).append(String.format("                \"Native\": %.2f,\r\n", Double.valueOf(memoryInfo.nativeSwappedOut / 1024.0d))).append(String.format("                \"Other\": %.2f,\r\n", Double.valueOf(memoryInfo.otherSwappedOut / 1024.0d))).append(String.format("                \"Total\": %.2f\r\n", Double.valueOf(memoryInfo.getTotalSwappedOut() / 1024.0d))).append("            },\r\n").append("            \"Shared\": {\r\n").append(String.format("                \"DVM\": %.2f,\r\n", Double.valueOf(memoryInfo.dalvikSharedDirty / 1024.0d))).append(String.format("                \"Native\": %.2f,\r\n", Double.valueOf(memoryInfo.nativeSharedDirty / 1024.0d))).append(String.format("                \"Other\": %.2f,\r\n", Double.valueOf(memoryInfo.otherSharedDirty / 1024.0d))).append(String.format("                \"Total\": %.2f\r\n", Double.valueOf(memoryInfo.getTotalSharedDirty() / 1024.0d))).append("            }\r\n").append("        },\r\n");
        for (int i = 0; i < 17; i++) {
            sb.append("        \"").append(Debug.MemoryInfo.getOtherLabel(i)).append("\": {\r\n").append("            \"Pss\": {\r\n").append(String.format("                \"Total\": %.2f\r\n", Double.valueOf(memoryInfo.getOtherPss(i) / 1024.0d))).append("            },\r\n").append("            \"Private\": {\r\n").append(String.format("                \"Total\": %.2f\r\n", Double.valueOf(memoryInfo.getOtherPrivateDirty(i) / 1024.0d))).append("            },\r\n").append("            \"Shared\": {\r\n").append(String.format("                \"Total\": %.2f\r\n", Double.valueOf(memoryInfo.getOtherSharedDirty(i) / 1024.0d))).append("            }\r\n");
            if (i + 1 == 17) {
                sb.append("        }\r\n").append("    }\r\n").append("}\r\n");
            } else {
                sb.append("        },\r\n");
            }
        }
        return sb.toString();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static String printMemoryInfo(boolean z, String str) {
        String str2;
        Debug.MemoryInfo memoryInfo = new Debug.MemoryInfo();
        Debug.getMemoryInfo(memoryInfo);
        StringBuilder sb = new StringBuilder();
        sb.append(str).append(" Browser Memory usage: (Total/DVM/Native/Other) \r\n").append(str).append(String.format(" Pss=%.2f/%.2f/%.2f/%.2f MB\r\n", Double.valueOf(memoryInfo.getTotalPss() / 1024.0d), Double.valueOf(memoryInfo.dalvikPss / 1024.0d), Double.valueOf(memoryInfo.nativePss / 1024.0d), Double.valueOf(memoryInfo.otherPss / 1024.0d))).append(str).append(String.format(" Private=%.2f/%.2f/%.2f/%.2f MB\r\n", Double.valueOf(memoryInfo.getTotalPrivateDirty() / 1024.0d), Double.valueOf(memoryInfo.dalvikPrivateDirty / 1024.0d), Double.valueOf(memoryInfo.nativePrivateDirty / 1024.0d), Double.valueOf(memoryInfo.otherPrivateDirty / 1024.0d))).append(str).append(String.format(" Shared=%.2f/%.2f/%.2f/%.2f MB\r\n", Double.valueOf(memoryInfo.getTotalSharedDirty() / 1024.0d), Double.valueOf(memoryInfo.dalvikSharedDirty / 1024.0d), Double.valueOf(memoryInfo.nativeSharedDirty / 1024.0d), Double.valueOf(memoryInfo.otherSharedDirty / 1024.0d))).append(str).append(String.format(" Swapped=%.2f/%.2f/%.2f/%.2f MB", Double.valueOf(memoryInfo.getTotalSwappedOut() / 1024.0d), Double.valueOf(memoryInfo.dalvikSwappedOut / 1024.0d), Double.valueOf(memoryInfo.nativeSwappedOut / 1024.0d), Double.valueOf(memoryInfo.otherSwappedOut / 1024.0d)));
        String str3 = "Browser other mem statistics: \r\n";
        for (int i = 0; i < 17; i++) {
            str3 = str3 + " [" + String.valueOf(i) + "] " + Debug.MemoryInfo.getOtherLabel(i) + ", pss=" + String.format("%.2fMB", Double.valueOf(memoryInfo.getOtherPss(i) / 1024.0d)) + ", private=" + String.format("%.2fMB", Double.valueOf(memoryInfo.getOtherPrivateDirty(i) / 1024.0d)) + ", shared=" + String.format("%.2fMB", Double.valueOf(memoryInfo.getOtherSharedDirty(i) / 1024.0d)) + "\r\n";
        }
        if (z) {
            try {
                str2 = "/storage/emulated/0/memDumpLog" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".txt";
                PrintWriter printWriter = new PrintWriter(str2);
                printWriter.print(encodeToJSON(memoryInfo));
                printWriter.close();
            } catch (IOException e) {
                Log.d("browser", "Failed to save memory logs to file, " + e.getMessage());
                str2 = "";
            }
        } else {
            Log.d("browser", sb.toString());
            Log.d("browser", str3);
            str2 = "";
        }
        return str2;
    }

    static void printProcessMemInfo(MemInfoReader memInfoReader, String str) {
        if (memInfoReader != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(str).append("{\r\n").append(str).append("    \"Process Memory Usage (MB)\": {\r\n").append(str).append(String.format("                TotalSize =: %.2f,\r\n", Double.valueOf((memInfoReader.getTotalSize() / 1024.0d) / 1024.0d))).append(str).append(String.format("                FreeSize =: %.2f,\r\n", Double.valueOf((memInfoReader.getFreeSize() / 1024.0d) / 1024.0d))).append(str).append(String.format("                MappedSize =: %.2f,\r\n", Double.valueOf((memInfoReader.getMappedSize() / 1024.0d) / 1024.0d))).append(str).append(String.format("                BuffersSize =: %.2f,\r\n", Double.valueOf((memInfoReader.getBuffersSize() / 1024.0d) / 1024.0d))).append(str).append(String.format("                CachedSize =: %.2f,\r\n", Double.valueOf((memInfoReader.getCachedSize() / 1024.0d) / 1024.0d))).append(str).append(String.format("                SwapTotalSizeKb =: %.2f,\r\n", Double.valueOf(memInfoReader.getSwapTotalSizeKb() / 1024.0d))).append(str).append(String.format("                SwapFreeSizeKb =: %.2f,\r\n", Double.valueOf(memInfoReader.getSwapFreeSizeKb() / 1024.0d))).append(str).append(String.format("                KernelUsedSize =: %.2f,\r\n", Double.valueOf((memInfoReader.getKernelUsedSize() / 1024.0d) / 1024.0d)));
            Log.d("browser", sb.toString());
        }
    }

    static void printSysMemInfo(ActivityManager.MemoryInfo memoryInfo, String str) {
        if (memoryInfo != null) {
            long j = memoryInfo.totalMem;
            long j2 = memoryInfo.threshold;
            long j3 = memoryInfo.availMem;
            long j4 = memoryInfo.hiddenAppThreshold;
            long j5 = memoryInfo.secondaryServerThreshold;
            long j6 = memoryInfo.visibleAppThreshold;
            long j7 = memoryInfo.foregroundAppThreshold;
            StringBuilder sb = new StringBuilder();
            sb.append("{\r\n").append(str).append("    \"System Memory Usage (MB)\": {\r\n").append(str).append(String.format("                total=: %.2f,\r\n", Double.valueOf((j / 1024.0d) / 1024.0d))).append(str).append(String.format("                threshold=: %.2f,\r\n", Double.valueOf((j2 / 1024.0d) / 1024.0d))).append(str).append(String.format("                availMem=: %.2f,\r\n", Double.valueOf((j3 / 1024.0d) / 1024.0d))).append(str).append(String.format("                hiddenAppThreshold=: %.2f,\r\n", Double.valueOf((j4 / 1024.0d) / 1024.0d))).append(str).append(String.format("                secondaryServerThreshold=: %.2f,\r\n", Double.valueOf((j5 / 1024.0d) / 1024.0d))).append(str).append(String.format("                visibleAppThreshold=: %.2f,\r\n", Double.valueOf((j6 / 1024.0d) / 1024.0d))).append(str).append(String.format("                foregroundAppThreshold=: %.2f,\r\n", Double.valueOf((j7 / 1024.0d) / 1024.0d)));
            Log.d("browser", sb.toString());
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void tracePageFinished() {
        if (mInTrace) {
            mInTrace = false;
            Debug.stopMethodTracing();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void tracePageStart(String str) {
        String str2;
        if (BrowserSettings.getInstance().isTracing()) {
            try {
                str2 = new WebAddress(str).getHost();
            } catch (ParseException e) {
                str2 = "browser";
            }
            mInTrace = true;
            Debug.startMethodTracing(str2.replace('.', '_') + ".trace", 20971520);
        }
    }
}
