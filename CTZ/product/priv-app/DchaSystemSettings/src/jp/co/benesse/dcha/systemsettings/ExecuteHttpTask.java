package jp.co.benesse.dcha.systemsettings;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import jp.co.benesse.dcha.util.Logger;
/* loaded from: classes.dex */
public class ExecuteHttpTask extends Thread {
    protected CountDownLatch countDownLatch;
    private HttpResponse httpResponse = null;
    private final Object lock = new Object();
    private final int timeout;
    private final String url;

    public ExecuteHttpTask(String str, int i) {
        Logger.d("ExecuteHttpTask", "ExecuteHttpTask start");
        this.url = str;
        this.timeout = i;
        this.countDownLatch = new CountDownLatch(1);
        Logger.d("ExecuteHttpTask", "ExecuteHttpTask end");
    }

    /* JADX WARN: Removed duplicated region for block: B:13:0x004d  */
    @Override // java.lang.Thread, java.lang.Runnable
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public void run() {
        HttpURLConnection httpURLConnection;
        String str;
        Object[] objArr;
        Logger.d("ExecuteHttpTask", "run start");
        if (this.url != null) {
            Logger.d("ExecuteHttpTask", "run 001");
            try {
                httpURLConnection = (HttpURLConnection) new URL(this.url).openConnection();
            } catch (MalformedURLException e) {
                Logger.d("ExecuteHttpTask", "run 002", e);
            } catch (IOException e2) {
                Logger.d("ExecuteHttpTask", "run 003", e2);
            }
            if (httpURLConnection != null) {
                Logger.d("ExecuteHttpTask", "run 004");
                try {
                    try {
                        httpURLConnection.setRequestMethod("GET");
                        httpURLConnection.setRequestProperty("Connection", "close");
                        httpURLConnection.setRequestProperty("Content-Type", "text/html; charset=UTF-8");
                        httpURLConnection.setRequestProperty("Content-Type", "application/octet-stream");
                        httpURLConnection.setInstanceFollowRedirects(true);
                        httpURLConnection.setConnectTimeout(this.timeout * 1000);
                        httpURLConnection.setReadTimeout(this.timeout * 1000);
                        httpURLConnection.connect();
                        StringBuffer processResponse = processResponse(httpURLConnection);
                        Logger.d("ExecuteHttpTask", "run 005");
                        synchronized (this.lock) {
                            Logger.d("ExecuteHttpTask", "run 006");
                            this.httpResponse = new HttpResponse(httpURLConnection.getResponseCode(), processResponse);
                        }
                        str = "ExecuteHttpTask";
                        objArr = new Object[]{"run 009"};
                    } catch (Throwable th) {
                        Logger.d("ExecuteHttpTask", "run 009");
                        httpURLConnection.disconnect();
                        throw th;
                    }
                } catch (ProtocolException e3) {
                    Logger.d("ExecuteHttpTask", "run 007", e3);
                    str = "ExecuteHttpTask";
                    objArr = new Object[]{"run 009"};
                } catch (IOException e4) {
                    Logger.d("ExecuteHttpTask", "run 008", e4);
                    str = "ExecuteHttpTask";
                    objArr = new Object[]{"run 009"};
                }
                Logger.d(str, objArr);
                httpURLConnection.disconnect();
            }
            this.countDownLatch.countDown();
            Logger.d("ExecuteHttpTask", "run end");
        }
        httpURLConnection = null;
        if (httpURLConnection != null) {
        }
        this.countDownLatch.countDown();
        Logger.d("ExecuteHttpTask", "run end");
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r2v21, types: [java.lang.Object[]] */
    /* JADX WARN: Type inference failed for: r3v11, types: [java.lang.String] */
    private StringBuffer processResponse(HttpURLConnection httpURLConnection) {
        String str;
        Object[] objArr;
        Logger.d("ExecuteHttpTask", "processResponse start");
        StringBuffer stringBuffer = new StringBuffer();
        BufferedReader bufferedReader = null;
        int i = 2;
        i = 2;
        i = 2;
        i = 2;
        i = 2;
        i = 2;
        i = 2;
        i = 2;
        i = 2;
        i = 2;
        try {
            try {
                Logger.d("ExecuteHttpTask", "processResponse 001");
                BufferedReader bufferedReader2 = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "UTF-8"));
                while (true) {
                    try {
                        String readLine = bufferedReader2.readLine();
                        if (readLine == null) {
                            break;
                        }
                        stringBuffer.append(readLine);
                        stringBuffer.append("\n");
                    } catch (FileNotFoundException e) {
                        e = e;
                        bufferedReader = bufferedReader2;
                        Logger.d("ExecuteHttpTask", "processResponse 005", e);
                        Logger.d("ExecuteHttpTask", "processResponse 006");
                        bufferedReader = bufferedReader;
                        if (bufferedReader != null) {
                            Logger.d("ExecuteHttpTask", "processResponse 007");
                            try {
                                bufferedReader.close();
                                bufferedReader = bufferedReader;
                            } catch (IOException e2) {
                                str = "ExecuteHttpTask";
                                objArr = new Object[]{"processResponse 008", e2};
                                Logger.d(str, objArr);
                                Logger.d("ExecuteHttpTask", "processResponse end");
                                return stringBuffer;
                            }
                        }
                        Logger.d("ExecuteHttpTask", "processResponse end");
                        return stringBuffer;
                    } catch (UnsupportedEncodingException e3) {
                        e = e3;
                        bufferedReader = bufferedReader2;
                        Logger.d("ExecuteHttpTask", "processResponse 003", e);
                        Logger.d("ExecuteHttpTask", "processResponse 006");
                        bufferedReader = bufferedReader;
                        if (bufferedReader != null) {
                            Logger.d("ExecuteHttpTask", "processResponse 007");
                            try {
                                bufferedReader.close();
                                bufferedReader = bufferedReader;
                            } catch (IOException e4) {
                                str = "ExecuteHttpTask";
                                objArr = new Object[]{"processResponse 008", e4};
                                Logger.d(str, objArr);
                                Logger.d("ExecuteHttpTask", "processResponse end");
                                return stringBuffer;
                            }
                        }
                        Logger.d("ExecuteHttpTask", "processResponse end");
                        return stringBuffer;
                    } catch (UnknownHostException e5) {
                        e = e5;
                        bufferedReader = bufferedReader2;
                        Logger.d("ExecuteHttpTask", "processResponse 004", e);
                        Logger.d("ExecuteHttpTask", "processResponse 006");
                        bufferedReader = bufferedReader;
                        if (bufferedReader != null) {
                            Logger.d("ExecuteHttpTask", "processResponse 007");
                            try {
                                bufferedReader.close();
                                bufferedReader = bufferedReader;
                            } catch (IOException e6) {
                                str = "ExecuteHttpTask";
                                objArr = new Object[]{"processResponse 008", e6};
                                Logger.d(str, objArr);
                                Logger.d("ExecuteHttpTask", "processResponse end");
                                return stringBuffer;
                            }
                        }
                        Logger.d("ExecuteHttpTask", "processResponse end");
                        return stringBuffer;
                    } catch (IOException e7) {
                        e = e7;
                        bufferedReader = bufferedReader2;
                        Logger.d("ExecuteHttpTask", "processResponse 005", e);
                        Logger.d("ExecuteHttpTask", "processResponse 006");
                        bufferedReader = bufferedReader;
                        if (bufferedReader != null) {
                            Logger.d("ExecuteHttpTask", "processResponse 007");
                            try {
                                bufferedReader.close();
                                bufferedReader = bufferedReader;
                            } catch (IOException e8) {
                                str = "ExecuteHttpTask";
                                objArr = new Object[]{"processResponse 008", e8};
                                Logger.d(str, objArr);
                                Logger.d("ExecuteHttpTask", "processResponse end");
                                return stringBuffer;
                            }
                        }
                        Logger.d("ExecuteHttpTask", "processResponse end");
                        return stringBuffer;
                    } catch (Throwable th) {
                        th = th;
                        bufferedReader = bufferedReader2;
                        Logger.d("ExecuteHttpTask", "processResponse 006");
                        if (bufferedReader != null) {
                            Logger.d("ExecuteHttpTask", "processResponse 007");
                            try {
                                bufferedReader.close();
                            } catch (IOException e9) {
                                Object[] objArr2 = new Object[i];
                                objArr2[0] = "processResponse 008";
                                objArr2[1] = e9;
                                Logger.d("ExecuteHttpTask", objArr2);
                            }
                        }
                        throw th;
                    }
                }
                bufferedReader2.close();
                Logger.d("ExecuteHttpTask", "processResponse 002");
                ?? r2 = {"processResponse 006"};
                Logger.d("ExecuteHttpTask", r2);
                bufferedReader = r2;
                i = "processResponse 006";
            } catch (Throwable th2) {
                th = th2;
            }
        } catch (FileNotFoundException e10) {
            e = e10;
        } catch (UnsupportedEncodingException e11) {
            e = e11;
        } catch (UnknownHostException e12) {
            e = e12;
        } catch (IOException e13) {
            e = e13;
        }
        Logger.d("ExecuteHttpTask", "processResponse end");
        return stringBuffer;
    }

    public void execute() {
        Logger.d("ExecuteHttpTask", "execute start");
        start();
        try {
            Logger.d("ExecuteHttpTask", "execute 001");
            this.countDownLatch.await(this.timeout, TimeUnit.SECONDS);
            Logger.d("ExecuteHttpTask", "execute 002");
        } catch (InterruptedException e) {
            Logger.d("ExecuteHttpTask", "execute 003", e);
        }
        Logger.d("ExecuteHttpTask", "execute end");
    }

    public HttpResponse getResponse() {
        HttpResponse httpResponse;
        Logger.d("ExecuteHttpTask", "getResponse start");
        synchronized (this.lock) {
            Logger.d("ExecuteHttpTask", "getResponse 001");
            if (this.httpResponse != null && 200 == this.httpResponse.getStatusCode()) {
                Logger.d("ExecuteHttpTask", "getResponse 002");
                httpResponse = this.httpResponse;
            } else {
                httpResponse = null;
            }
        }
        Logger.d("ExecuteHttpTask", "getResponse end");
        return httpResponse;
    }
}
