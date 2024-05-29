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
/* loaded from: d.zip:jp/co/benesse/dcha/dchaservice/TimeSetLogSender.class */
public class TimeSetLogSender {
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd kk:mm:ss.SSS", Locale.JAPAN);

    public static void send(Context context) {
        String format;
        FileLock fileLock;
        RandomAccessFile randomAccessFile;
        synchronized (TimeSetLogSender.class) {
            try {
                Log.d("TimeSetLogSender", "send 0001");
                synchronized (DATE_FORMAT) {
                    format = DATE_FORMAT.format(new Date());
                }
                RandomAccessFile randomAccessFile2 = null;
                FileLock fileLock2 = null;
                FileLock fileLock3 = null;
                RandomAccessFile randomAccessFile3 = null;
                try {
                    try {
                        File file = new File("/data/data/jp.co.benesse.dcha.dchaservice/TimeSetLog.log");
                        if (file.exists()) {
                            Log.d("TimeSetLogSender", "send 0002");
                            randomAccessFile2 = new RandomAccessFile(file, "rw");
                            FileLock fileLock4 = null;
                            FileLock fileLock5 = null;
                            try {
                                fileLock2 = randomAccessFile2.getChannel().tryLock();
                                if (fileLock2 != null) {
                                    Log.d("TimeSetLogSender", "send 0003");
                                    int i = 0;
                                    while (randomAccessFile2.readLine() != null) {
                                        i++;
                                    }
                                    randomAccessFile2.seek(0L);
                                    int i2 = 100 >= i ? 0 : i - 100;
                                    int i3 = 0;
                                    StringBuffer stringBuffer = new StringBuffer();
                                    while (true) {
                                        fileLock4 = fileLock2;
                                        fileLock5 = fileLock2;
                                        String readLine = randomAccessFile2.readLine();
                                        if (readLine == null) {
                                            break;
                                        }
                                        int i4 = i3 + 1;
                                        i3 = i4;
                                        if (i4 > i2) {
                                            Log.d("TimeSetLogSender", "send 0004");
                                            stringBuffer.append(String.format("L%1$04d: ", Integer.valueOf(i4)));
                                            int length = readLine.length();
                                            int i5 = length;
                                            if (255 < length) {
                                                Log.d("TimeSetLogSender", "send 0005");
                                                i5 = 255;
                                            }
                                            stringBuffer.append(readLine.substring(0, i5));
                                            EmergencyLog.write(context, format, "ELK012", stringBuffer.toString(), false);
                                            stringBuffer.setLength(0);
                                            i3 = i4;
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                e = e;
                                randomAccessFile = randomAccessFile2;
                                fileLock = fileLock4;
                                Log.e("TimeSetLogSender", "send 0006", e);
                                Log.d("TimeSetLogSender", "send 0007");
                                if (fileLock != null) {
                                    try {
                                        Log.d("TimeSetLogSender", "send 0008");
                                        fileLock.release();
                                    } catch (IOException e2) {
                                        Log.e("TimeSetLogSender", "send 0009", e2);
                                    }
                                }
                                if (randomAccessFile != null) {
                                    try {
                                        Log.d("TimeSetLogSender", "send 0010");
                                        randomAccessFile.close();
                                    } catch (IOException e3) {
                                        Log.e("TimeSetLogSender", "send 0011", e3);
                                    }
                                }
                            } catch (Throwable th) {
                                th = th;
                                fileLock3 = fileLock5;
                                randomAccessFile3 = randomAccessFile2;
                                Log.d("TimeSetLogSender", "send 0007");
                                if (fileLock3 != null) {
                                    try {
                                        Log.d("TimeSetLogSender", "send 0008");
                                        fileLock3.release();
                                    } catch (IOException e4) {
                                        Log.e("TimeSetLogSender", "send 0009", e4);
                                    }
                                }
                                if (randomAccessFile3 != null) {
                                    try {
                                        Log.d("TimeSetLogSender", "send 0010");
                                        randomAccessFile3.close();
                                    } catch (IOException e5) {
                                        Log.e("TimeSetLogSender", "send 0011", e5);
                                    }
                                }
                                throw th;
                            }
                        }
                        Log.d("TimeSetLogSender", "send 0007");
                        if (fileLock2 != null) {
                            try {
                                Log.d("TimeSetLogSender", "send 0008");
                                fileLock2.release();
                            } catch (IOException e6) {
                                Log.e("TimeSetLogSender", "send 0009", e6);
                            }
                        }
                        if (randomAccessFile2 != null) {
                            try {
                                Log.d("TimeSetLogSender", "send 0010");
                                randomAccessFile2.close();
                            } catch (IOException e7) {
                                Log.e("TimeSetLogSender", "send 0011", e7);
                            }
                        }
                    } catch (Throwable th2) {
                        th = th2;
                    }
                } catch (Exception e8) {
                    e = e8;
                    fileLock = null;
                    randomAccessFile = null;
                }
            } catch (Throwable th3) {
                throw th3;
            }
        }
    }
}
