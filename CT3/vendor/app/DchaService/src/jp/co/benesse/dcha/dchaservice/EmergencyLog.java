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
/* loaded from: d.zip:jp/co/benesse/dcha/dchaservice/EmergencyLog.class */
public class EmergencyLog {
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd kk:mm:ss.SSS", Locale.JAPAN);

    public static void write(Context context, String str, String str2) {
        String format;
        synchronized (EmergencyLog.class) {
            try {
                Log.d("EmergencyLog", "write 0001");
                Log.d("EmergencyLog", "write without time");
                synchronized (DATE_FORMAT) {
                    format = DATE_FORMAT.format(new Date());
                }
                Log.d("EmergencyLog", "write 0002");
                write(context, format, str, str2, true);
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    public static void write(Context context, String str, String str2, String str3) {
        synchronized (EmergencyLog.class) {
            try {
                Log.d("EmergencyLog", "write 0003");
                Log.d("EmergencyLog", "write with time");
                write(context, str, str2, str3, true);
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    public static void write(Context context, String str, String str2, String str3, boolean z) {
        synchronized (EmergencyLog.class) {
            try {
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
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    private static void writeLog(String str) {
        FileWriter fileWriter;
        FileWriter fileWriter2;
        Log.d("EmergencyLog", "writeLog 0001");
        File file = new File("/data/data/jp.co.benesse.dcha.dchaservice/jp.co.benesse.dcha.dchaservice_000.txt");
        File file2 = new File("/data/data/jp.co.benesse.dcha.dchaservice/jp.co.benesse.dcha.dchaservice_001.txt");
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
        FileWriter fileWriter3 = null;
        try {
            try {
                fileWriter2 = new FileWriter(file, true);
            } catch (Exception e) {
                e = e;
                fileWriter = null;
            }
        } catch (Throwable th) {
            th = th;
        }
        try {
            fileWriter2.write(str + System.getProperty("line.separator"));
            Log.d("EmergencyLog", "end writeLog");
            Log.d("EmergencyLog", "writeLog 0008");
            if (fileWriter2 != null) {
                try {
                    Log.d("EmergencyLog", "writeLog 0009");
                    fileWriter2.close();
                } catch (IOException e2) {
                    Log.e("EmergencyLog", "writeLog 0010", e2);
                }
            }
        } catch (Exception e3) {
            e = e3;
            fileWriter = fileWriter2;
            fileWriter3 = fileWriter;
            Log.e("EmergencyLog", "writeLog 0007", e);
            Log.d("EmergencyLog", "writeLog 0008");
            if (fileWriter != null) {
                try {
                    Log.d("EmergencyLog", "writeLog 0009");
                    fileWriter.close();
                } catch (IOException e4) {
                    Log.e("EmergencyLog", "writeLog 0010", e4);
                }
            }
            Log.d("EmergencyLog", "writeLog 0011");
        } catch (Throwable th2) {
            th = th2;
            fileWriter3 = fileWriter2;
            Log.d("EmergencyLog", "writeLog 0008");
            if (fileWriter3 != null) {
                try {
                    Log.d("EmergencyLog", "writeLog 0009");
                    fileWriter3.close();
                } catch (IOException e5) {
                    Log.e("EmergencyLog", "writeLog 0010", e5);
                }
            }
            throw th;
        }
        Log.d("EmergencyLog", "writeLog 0011");
    }
}
