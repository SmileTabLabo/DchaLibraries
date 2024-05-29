package jp.co.benesse.dcha.dchaservice;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import jp.co.benesse.dcha.dchaservice.util.Log;
/* loaded from: d.zip:jp/co/benesse/dcha/dchaservice/UpdateLog.class */
public class UpdateLog {
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd kk:mm:ss.SSS", Locale.JAPAN);

    public static boolean exists() {
        boolean exists;
        synchronized (UpdateLog.class) {
            try {
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
            } catch (Throwable th) {
                throw th;
            }
        }
        return exists;
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r4v11, types: [java.lang.Throwable, java.io.IOException] */
    /* JADX WARN: Type inference failed for: r4v7, types: [java.lang.Throwable, java.io.IOException] */
    public static void write() {
        String format;
        FileWriter fileWriter;
        FileWriter fileWriter2;
        FileWriter fileWriter3;
        synchronized (UpdateLog.class) {
            try {
                Log.d("UpdateLog", "write 0001");
                synchronized (DATE_FORMAT) {
                    format = DATE_FORMAT.format(new Date());
                }
                FileWriter fileWriter4 = null;
                try {
                    try {
                        fileWriter3 = null;
                        fileWriter = new FileWriter(new File("/data/data/jp.co.benesse.dcha.dchaservice/update.log"), false);
                    } catch (Throwable th) {
                        th = th;
                        fileWriter2 = fileWriter4;
                    }
                } catch (Exception e) {
                    e = e;
                    fileWriter = null;
                }
                try {
                    fileWriter.write(format);
                    Log.d("UpdateLog", "write 0002");
                    Log.d("UpdateLog", "write update.log");
                    Log.d("UpdateLog", "write 0004");
                    fileWriter4 = fileWriter3;
                    if (fileWriter != null) {
                        try {
                            Log.d("UpdateLog", "write 0005");
                            fileWriter.close();
                            fileWriter4 = fileWriter3;
                        } catch (IOException e2) {
                            Log.d("UpdateLog", "write 0006");
                            Log.e("UpdateLog", "write", e2);
                            fileWriter4 = e2;
                        }
                    }
                } catch (Exception e3) {
                    e = e3;
                    Log.d("UpdateLog", "write 0003");
                    FileWriter fileWriter5 = fileWriter;
                    Log.e("UpdateLog", "write", e);
                    Log.d("UpdateLog", "write 0004");
                    fileWriter4 = fileWriter5;
                    if (fileWriter != null) {
                        try {
                            Log.d("UpdateLog", "write 0005");
                            fileWriter.close();
                            fileWriter4 = fileWriter5;
                        } catch (IOException e4) {
                            Log.d("UpdateLog", "write 0006");
                            Log.e("UpdateLog", "write", e4);
                            fileWriter4 = e4;
                        }
                    }
                    Log.d("UpdateLog", "write 0007");
                } catch (Throwable th2) {
                    fileWriter2 = fileWriter;
                    th = th2;
                    Log.d("UpdateLog", "write 0004");
                    if (fileWriter2 != null) {
                        try {
                            Log.d("UpdateLog", "write 0005");
                            fileWriter2.close();
                        } catch (IOException e5) {
                            Log.d("UpdateLog", "write 0006");
                            Log.e("UpdateLog", "write", e5);
                        }
                    }
                    throw th;
                }
                Log.d("UpdateLog", "write 0007");
            } catch (Throwable th3) {
                throw th3;
            }
        }
    }
}
