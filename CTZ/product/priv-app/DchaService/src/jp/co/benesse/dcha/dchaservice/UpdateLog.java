package jp.co.benesse.dcha.dchaservice;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import jp.co.benesse.dcha.dchaservice.util.Log;
/* loaded from: classes.dex */
public class UpdateLog {
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd kk:mm:ss.SSS", Locale.JAPAN);

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r3v20, types: [java.lang.String] */
    public static synchronized void write() {
        String format;
        String str;
        String str2;
        FileWriter fileWriter;
        synchronized (UpdateLog.class) {
            Log.d("UpdateLog", "write 0001");
            synchronized (DATE_FORMAT) {
                format = DATE_FORMAT.format(new Date());
            }
            File file = new File("/data/data/jp.co.benesse.dcha.dchaservice/update.log");
            FileWriter fileWriter2 = null;
            try {
                try {
                    fileWriter = new FileWriter(file, false);
                } catch (Throwable th) {
                    th = th;
                }
                try {
                    fileWriter.write(format);
                    Log.d("UpdateLog", "write 0002");
                    Log.d("UpdateLog", "write update.log");
                    Log.d("UpdateLog", "write 0004");
                    try {
                        Log.d("UpdateLog", "write 0005");
                        fileWriter.close();
                        fileWriter2 = "write 0005";
                    } catch (IOException e) {
                        e = e;
                        Log.d("UpdateLog", "write 0006");
                        str = "UpdateLog";
                        str2 = "write";
                        Log.e(str, str2, e);
                        file.setReadable(true, false);
                        Log.d("UpdateLog", "write 0008");
                    }
                } catch (Exception e2) {
                    e = e2;
                    fileWriter2 = fileWriter;
                    Log.d("UpdateLog", "write 0003");
                    Log.e("UpdateLog", "write", e);
                    Log.d("UpdateLog", "write 0004");
                    fileWriter2 = fileWriter2;
                    if (fileWriter2 != null) {
                        try {
                            Log.d("UpdateLog", "write 0005");
                            fileWriter2.close();
                            fileWriter2 = fileWriter2;
                        } catch (IOException e3) {
                            e = e3;
                            Log.d("UpdateLog", "write 0006");
                            str = "UpdateLog";
                            str2 = "write";
                            Log.e(str, str2, e);
                            file.setReadable(true, false);
                            Log.d("UpdateLog", "write 0008");
                        }
                    }
                    file.setReadable(true, false);
                    Log.d("UpdateLog", "write 0008");
                } catch (Throwable th2) {
                    th = th2;
                    fileWriter2 = fileWriter;
                    Log.d("UpdateLog", "write 0004");
                    if (fileWriter2 != null) {
                        try {
                            Log.d("UpdateLog", "write 0005");
                            fileWriter2.close();
                        } catch (IOException e4) {
                            Log.d("UpdateLog", "write 0006");
                            Log.e("UpdateLog", "write", e4);
                        }
                    }
                    throw th;
                }
            } catch (Exception e5) {
                e = e5;
            }
            try {
                file.setReadable(true, false);
            } catch (Exception e6) {
                Log.e("UpdateLog", "write 0007", e6);
            }
            Log.d("UpdateLog", "write 0008");
        }
    }

    public static synchronized boolean exists() {
        boolean exists;
        synchronized (UpdateLog.class) {
            Log.d("UpdateLog", "exists 0001");
            exists = new File("/data/data/jp.co.benesse.dcha.dchaservice/update.log").exists();
            if (exists) {
                Log.d("UpdateLog", "exists 0002");
                Log.d("UpdateLog", "exists true");
            } else {
                Log.d("UpdateLog", "exists 0003");
                Log.d("UpdateLog", "exists false");
            }
            Log.d("UpdateLog", "exists 0004");
        }
        return exists;
    }
}
