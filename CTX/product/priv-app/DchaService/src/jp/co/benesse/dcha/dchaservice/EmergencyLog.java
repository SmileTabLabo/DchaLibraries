package jp.co.benesse.dcha.dchaservice;

import android.content.Context;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import jp.co.benesse.dcha.dchaservice.util.Log;
/* loaded from: classes.dex */
public class EmergencyLog {
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd kk:mm:ss.SSS", Locale.JAPAN);

    public static synchronized void write(Context context, String str, String str2) {
        String format;
        synchronized (EmergencyLog.class) {
            Log.d("EmergencyLog", "write 0001");
            Log.d("EmergencyLog", "write without time");
            synchronized (DATE_FORMAT) {
                format = DATE_FORMAT.format(new Date());
            }
            Log.d("EmergencyLog", "write 0002");
            write(context, format, str, str2, true);
        }
    }

    public static synchronized void write(Context context, String str, String str2, String str3) {
        synchronized (EmergencyLog.class) {
            Log.d("EmergencyLog", "write 0003");
            Log.d("EmergencyLog", "write with time");
            write(context, str, str2, str3, true);
        }
    }

    public static synchronized void write(Context context, String str, String str2, String str3, boolean z) {
        synchronized (EmergencyLog.class) {
            Log.d("EmergencyLog", "write 0004");
            Log.d("EmergencyLog", "write with time");
            StringBuffer stringBuffer = new StringBuffer(str);
            stringBuffer.append(" ");
            stringBuffer.append(str2);
            stringBuffer.append(" ");
            stringBuffer.append(str3);
            String stringBuffer2 = stringBuffer.toString();
            Log.d("EmergencyLog", "write 0005");
            writeLog(stringBuffer2);
        }
    }

    /* JADX WARN: Can't wrap try/catch for region: R(11:1|(2:3|(3:5|(1:7)|8))|9|(2:10|11)|(4:13|14|15|16)|17|19|20|21|22|(1:(0))) */
    /* JADX WARN: Code restructure failed: missing block: B:33:0x00cf, code lost:
        r6 = move-exception;
     */
    /* JADX WARN: Code restructure failed: missing block: B:34:0x00d0, code lost:
        jp.co.benesse.dcha.dchaservice.util.Log.e("EmergencyLog", "writeLog 0011", r6);
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private static void writeLog(String str) {
        FileWriter fileWriter;
        Log.d("EmergencyLog", "writeLog 0001");
        File file = new File("/factory/log/jp.co.benesse.dcha.dchaservice_000.txt");
        File file2 = new File("/factory/log/jp.co.benesse.dcha.dchaservice_001.txt");
        if (file.exists()) {
            Log.d("EmergencyLog", "writeLog 0002");
            if (file.length() > 102400) {
                Log.d("EmergencyLog", "writeLog 0003");
                if (file2.exists()) {
                    Log.d("EmergencyLog", "writeLog 0004");
                    file2.delete();
                }
                Log.d("EmergencyLog", "writeLog 0005");
                Log.d("EmergencyLog", "change log");
                file.renameTo(file2);
            }
        }
        Log.d("EmergencyLog", "writeLog 0006");
        FileWriter fileWriter2 = null;
        try {
            try {
                try {
                    fileWriter = new FileWriter(file, true);
                } catch (IOException e) {
                    Log.e("EmergencyLog", "writeLog 0010", e);
                }
            } catch (Exception e2) {
                e = e2;
            }
        } catch (Throwable th) {
            th = th;
            fileWriter = fileWriter2;
        }
        try {
            fileWriter.write(str + System.getProperty("line.separator"));
            Log.d("EmergencyLog", "end writeLog");
            Log.d("EmergencyLog", "writeLog 0008");
            Log.d("EmergencyLog", "writeLog 0009");
            fileWriter.close();
        } catch (Exception e3) {
            e = e3;
            fileWriter2 = fileWriter;
            Log.e("EmergencyLog", "writeLog 0007", e);
            Log.d("EmergencyLog", "writeLog 0008");
            if (fileWriter2 != null) {
                Log.d("EmergencyLog", "writeLog 0009");
                fileWriter2.close();
            }
            new File("/factory/log/jp.co.benesse.dcha.dchaservice_000.txt").setReadable(true, false);
            Log.d("EmergencyLog", "writeLog 0012");
        } catch (Throwable th2) {
            th = th2;
            Log.d("EmergencyLog", "writeLog 0008");
            if (fileWriter != null) {
                try {
                    Log.d("EmergencyLog", "writeLog 0009");
                    fileWriter.close();
                } catch (IOException e4) {
                    Log.e("EmergencyLog", "writeLog 0010", e4);
                }
            }
            throw th;
        }
        new File("/factory/log/jp.co.benesse.dcha.dchaservice_000.txt").setReadable(true, false);
        Log.d("EmergencyLog", "writeLog 0012");
    }
}
