package com.android.systemui.classifier;

import android.app.ActivityThread;
import android.app.Application;
import android.os.Build;
import android.os.SystemProperties;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Locale;
/* loaded from: a.zip:com/android/systemui/classifier/FalsingLog.class */
public class FalsingLog {
    public static final boolean ENABLED = SystemProperties.getBoolean("debug.falsing_log", Build.IS_DEBUGGABLE);
    private static final boolean LOGCAT = SystemProperties.getBoolean("debug.falsing_logcat", false);
    private static final int MAX_SIZE = SystemProperties.getInt("debug.falsing_log_size", 100);
    private static FalsingLog sInstance;
    private final ArrayDeque<String> mLog = new ArrayDeque<>(MAX_SIZE);
    private final SimpleDateFormat mFormat = new SimpleDateFormat("MM-dd HH:mm:ss", Locale.US);

    private FalsingLog() {
    }

    public static void dump(PrintWriter printWriter) {
        synchronized (FalsingLog.class) {
            try {
                printWriter.println("FALSING LOG:");
                if (!ENABLED) {
                    printWriter.println("Disabled, to enable: setprop debug.falsing_log 1");
                    printWriter.println();
                } else if (sInstance == null || sInstance.mLog.isEmpty()) {
                    printWriter.println("<empty>");
                    printWriter.println();
                } else {
                    for (String str : sInstance.mLog) {
                        printWriter.println(str);
                    }
                    printWriter.println();
                }
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    public static void e(String str, String str2) {
        if (LOGCAT) {
            Log.e("FalsingLog", str + "\t" + str2);
        }
        log("E", str, str2);
    }

    public static void i(String str, String str2) {
        if (LOGCAT) {
            Log.i("FalsingLog", str + "\t" + str2);
        }
        log("I", str, str2);
    }

    public static void log(String str, String str2, String str3) {
        synchronized (FalsingLog.class) {
            try {
                if (ENABLED) {
                    if (sInstance == null) {
                        sInstance = new FalsingLog();
                    }
                    if (sInstance.mLog.size() >= MAX_SIZE) {
                        sInstance.mLog.removeFirst();
                    }
                    sInstance.mLog.add(sInstance.mFormat.format(new Date()) + " " + str + " " + str2 + " " + str3);
                }
            } finally {
            }
        }
    }

    public static void wtf(String str, String str2) {
        String str3;
        File file;
        PrintWriter printWriter;
        synchronized (FalsingLog.class) {
            try {
                if (ENABLED) {
                    e(str, str2);
                    Application currentApplication = ActivityThread.currentApplication();
                    if (!Build.IS_DEBUGGABLE || currentApplication == null) {
                        Log.e("FalsingLog", "Unable to write log, build must be debuggable.");
                        str3 = "";
                    } else {
                        PrintWriter printWriter2 = null;
                        try {
                            try {
                                printWriter = new PrintWriter(new File(currentApplication.getDataDir(), "falsing-" + new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date()) + ".txt"));
                            } catch (IOException e) {
                                e = e;
                                printWriter = null;
                            }
                        } catch (Throwable th) {
                            th = th;
                        }
                        try {
                            dump(printWriter);
                            printWriter.close();
                            String str4 = "Log written to " + file.getAbsolutePath();
                            str3 = str4;
                            if (printWriter != null) {
                                printWriter.close();
                                str3 = str4;
                            }
                        } catch (IOException e2) {
                            e = e2;
                            printWriter2 = printWriter;
                            Log.e("FalsingLog", "Unable to write falsing log", e);
                            str3 = "";
                            if (printWriter != null) {
                                printWriter.close();
                                str3 = "";
                            }
                            Log.e("FalsingLog", str + " " + str2 + "; " + str3);
                        } catch (Throwable th2) {
                            th = th2;
                            printWriter2 = printWriter;
                            if (printWriter2 != null) {
                                printWriter2.close();
                            }
                            throw th;
                        }
                    }
                    Log.e("FalsingLog", str + " " + str2 + "; " + str3);
                }
            } catch (Throwable th3) {
                throw th3;
            }
        }
    }
}
