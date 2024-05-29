package jp.co.benesse.dcha.dchaservice;

import android.content.Context;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import jp.co.benesse.dcha.dchaservice.util.Log;
/* loaded from: classes.dex */
public class TimeSetLogSender {
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd kk:mm:ss.SSS", Locale.JAPAN);

    public static synchronized void send(Context context) {
        String format;
        FileLock fileLock;
        RandomAccessFile randomAccessFile;
        String str;
        String str2;
        synchronized (TimeSetLogSender.class) {
            Log.d("TimeSetLogSender", "send 0001");
            synchronized (DATE_FORMAT) {
                format = DATE_FORMAT.format(new Date());
            }
            FileLock fileLock2 = null;
            try {
                try {
                    File file = new File("/factory/log/TimeSetLog.log");
                    if (file.exists()) {
                        Log.d("TimeSetLogSender", "send 0002");
                        randomAccessFile = new RandomAccessFile(file, "rw");
                        try {
                            fileLock = randomAccessFile.getChannel().tryLock();
                            if (fileLock != null) {
                                try {
                                    Log.d("TimeSetLogSender", "send 0003");
                                    int i = 0;
                                    while (randomAccessFile.readLine() != null) {
                                        i++;
                                    }
                                    randomAccessFile.seek(0L);
                                    int i2 = 100 >= i ? 0 : i - 100;
                                    StringBuffer stringBuffer = new StringBuffer();
                                    int i3 = 0;
                                    while (true) {
                                        String readLine = randomAccessFile.readLine();
                                        if (readLine == null) {
                                            break;
                                        }
                                        i3++;
                                        if (i3 > i2) {
                                            Log.d("TimeSetLogSender", "send 0004");
                                            stringBuffer.append(String.format("L%1$04d: ", Integer.valueOf(i3)));
                                            int length = readLine.length();
                                            if (255 < length) {
                                                Log.d("TimeSetLogSender", "send 0005");
                                                length = 255;
                                            }
                                            stringBuffer.append(readLine.substring(0, length));
                                            EmergencyLog.write(context, format, "ELK012", stringBuffer.toString(), false);
                                            stringBuffer.setLength(0);
                                        }
                                    }
                                } catch (Exception e) {
                                    e = e;
                                    fileLock2 = fileLock;
                                    Log.e("TimeSetLogSender", "send 0006", e);
                                    Log.d("TimeSetLogSender", "send 0007");
                                    if (fileLock2 != null) {
                                        try {
                                            Log.d("TimeSetLogSender", "send 0008");
                                            fileLock2.release();
                                        } catch (IOException e2) {
                                            Log.e("TimeSetLogSender", "send 0009", e2);
                                        }
                                    }
                                    if (randomAccessFile != null) {
                                        try {
                                            Log.d("TimeSetLogSender", "send 0010");
                                            randomAccessFile.close();
                                        } catch (IOException e3) {
                                            e = e3;
                                            str = "TimeSetLogSender";
                                            str2 = "send 0011";
                                            Log.e(str, str2, e);
                                        }
                                    }
                                } catch (Throwable th) {
                                    th = th;
                                    Log.d("TimeSetLogSender", "send 0007");
                                    if (fileLock != null) {
                                        try {
                                            Log.d("TimeSetLogSender", "send 0008");
                                            fileLock.release();
                                        } catch (IOException e4) {
                                            Log.e("TimeSetLogSender", "send 0009", e4);
                                        }
                                    }
                                    if (randomAccessFile != null) {
                                        try {
                                            Log.d("TimeSetLogSender", "send 0010");
                                            randomAccessFile.close();
                                        } catch (IOException e5) {
                                            Log.e("TimeSetLogSender", "send 0011", e5);
                                        }
                                    }
                                    throw th;
                                }
                            }
                            fileLock2 = fileLock;
                        } catch (Exception e6) {
                            e = e6;
                        }
                    } else {
                        randomAccessFile = null;
                    }
                    Log.d("TimeSetLogSender", "send 0007");
                    if (fileLock2 != null) {
                        try {
                            Log.d("TimeSetLogSender", "send 0008");
                            fileLock2.release();
                        } catch (IOException e7) {
                            Log.e("TimeSetLogSender", "send 0009", e7);
                        }
                    }
                    if (randomAccessFile != null) {
                        try {
                            Log.d("TimeSetLogSender", "send 0010");
                            randomAccessFile.close();
                        } catch (IOException e8) {
                            e = e8;
                            str = "TimeSetLogSender";
                            str2 = "send 0011";
                            Log.e(str, str2, e);
                        }
                    }
                } catch (Throwable th2) {
                    th = th2;
                    fileLock = null;
                }
            } catch (Exception e9) {
                e = e9;
                randomAccessFile = null;
            } catch (Throwable th3) {
                th = th3;
                fileLock = null;
                randomAccessFile = null;
            }
        }
    }
}
