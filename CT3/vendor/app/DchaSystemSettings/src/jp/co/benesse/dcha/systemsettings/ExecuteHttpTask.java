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
/* loaded from: s.zip:jp/co/benesse/dcha/systemsettings/ExecuteHttpTask.class */
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

    private StringBuffer processResponse(HttpURLConnection httpURLConnection) {
        BufferedReader bufferedReader;
        IOException iOException;
        BufferedReader bufferedReader2;
        UnknownHostException unknownHostException;
        BufferedReader bufferedReader3;
        UnsupportedEncodingException unsupportedEncodingException;
        BufferedReader bufferedReader4;
        FileNotFoundException fileNotFoundException;
        Logger.d("ExecuteHttpTask", "processResponse start");
        StringBuffer stringBuffer = new StringBuffer();
        BufferedReader bufferedReader5 = null;
        try {
            try {
                Logger.d("ExecuteHttpTask", "processResponse 001");
                BufferedReader bufferedReader6 = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "UTF-8"));
                while (true) {
                    try {
                        String readLine = bufferedReader6.readLine();
                        if (readLine == null) {
                            break;
                        }
                        stringBuffer.append(readLine).append("\n");
                    } catch (FileNotFoundException e) {
                        bufferedReader4 = bufferedReader6;
                        fileNotFoundException = e;
                        bufferedReader5 = bufferedReader4;
                        Logger.d("ExecuteHttpTask", "processResponse 005", fileNotFoundException);
                        Logger.d("ExecuteHttpTask", "processResponse 006");
                        if (bufferedReader4 != null) {
                            Logger.d("ExecuteHttpTask", "processResponse 007");
                            try {
                                bufferedReader4.close();
                            } catch (IOException e2) {
                                Logger.d("ExecuteHttpTask", "processResponse 008", e2);
                            }
                        }
                        Logger.d("ExecuteHttpTask", "processResponse end");
                        return stringBuffer;
                    } catch (UnsupportedEncodingException e3) {
                        bufferedReader3 = bufferedReader6;
                        unsupportedEncodingException = e3;
                        bufferedReader5 = bufferedReader3;
                        Logger.d("ExecuteHttpTask", "processResponse 003", unsupportedEncodingException);
                        Logger.d("ExecuteHttpTask", "processResponse 006");
                        if (bufferedReader3 != null) {
                            Logger.d("ExecuteHttpTask", "processResponse 007");
                            try {
                                bufferedReader3.close();
                            } catch (IOException e4) {
                                Logger.d("ExecuteHttpTask", "processResponse 008", e4);
                            }
                        }
                        Logger.d("ExecuteHttpTask", "processResponse end");
                        return stringBuffer;
                    } catch (UnknownHostException e5) {
                        bufferedReader2 = bufferedReader6;
                        unknownHostException = e5;
                        bufferedReader5 = bufferedReader2;
                        Logger.d("ExecuteHttpTask", "processResponse 004", unknownHostException);
                        Logger.d("ExecuteHttpTask", "processResponse 006");
                        if (bufferedReader2 != null) {
                            Logger.d("ExecuteHttpTask", "processResponse 007");
                            try {
                                bufferedReader2.close();
                            } catch (IOException e6) {
                                Logger.d("ExecuteHttpTask", "processResponse 008", e6);
                            }
                        }
                        Logger.d("ExecuteHttpTask", "processResponse end");
                        return stringBuffer;
                    } catch (IOException e7) {
                        iOException = e7;
                        bufferedReader = bufferedReader6;
                        bufferedReader5 = bufferedReader;
                        Logger.d("ExecuteHttpTask", "processResponse 005", iOException);
                        Logger.d("ExecuteHttpTask", "processResponse 006");
                        if (bufferedReader != null) {
                            Logger.d("ExecuteHttpTask", "processResponse 007");
                            try {
                                bufferedReader.close();
                            } catch (IOException e8) {
                                Logger.d("ExecuteHttpTask", "processResponse 008", e8);
                            }
                        }
                        Logger.d("ExecuteHttpTask", "processResponse end");
                        return stringBuffer;
                    } catch (Throwable th) {
                        th = th;
                        bufferedReader5 = bufferedReader6;
                        Logger.d("ExecuteHttpTask", "processResponse 006");
                        if (bufferedReader5 != null) {
                            Logger.d("ExecuteHttpTask", "processResponse 007");
                            try {
                                bufferedReader5.close();
                            } catch (IOException e9) {
                                Logger.d("ExecuteHttpTask", "processResponse 008", e9);
                            }
                        }
                        throw th;
                    }
                }
                bufferedReader6.close();
                bufferedReader5 = null;
                Logger.d("ExecuteHttpTask", "processResponse 002");
                Logger.d("ExecuteHttpTask", "processResponse 006");
            } catch (Throwable th2) {
                th = th2;
            }
        } catch (FileNotFoundException unused) {
            bufferedReader4 = null;
            fileNotFoundException = null;
        } catch (UnsupportedEncodingException unused2) {
            bufferedReader3 = null;
            unsupportedEncodingException = null;
        } catch (UnknownHostException unused3) {
            bufferedReader2 = null;
            unknownHostException = null;
        } catch (IOException unused4) {
            bufferedReader = null;
            iOException = null;
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
            httpResponse = null;
            if (this.httpResponse != null) {
                httpResponse = null;
                if (200 == this.httpResponse.getStatusCode()) {
                    Logger.d("ExecuteHttpTask", "getResponse 002");
                    httpResponse = this.httpResponse;
                }
            }
        }
        Logger.d("ExecuteHttpTask", "getResponse end");
        return httpResponse;
    }

    @Override // java.lang.Thread, java.lang.Runnable
    public void run() {
        Logger.d("ExecuteHttpTask", "run start");
        HttpURLConnection httpURLConnection = null;
        if (this.url != null) {
            Logger.d("ExecuteHttpTask", "run 001");
            try {
                httpURLConnection = (HttpURLConnection) new URL(this.url).openConnection();
            } catch (MalformedURLException e) {
                Logger.d("ExecuteHttpTask", "run 002", e);
                httpURLConnection = null;
            } catch (IOException e2) {
                Logger.d("ExecuteHttpTask", "run 003", e2);
                httpURLConnection = null;
            }
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
                    Logger.d("ExecuteHttpTask", "run 009");
                    httpURLConnection.disconnect();
                } catch (ProtocolException e3) {
                    Logger.d("ExecuteHttpTask", "run 007", e3);
                    Logger.d("ExecuteHttpTask", "run 009");
                    httpURLConnection.disconnect();
                } catch (IOException e4) {
                    Logger.d("ExecuteHttpTask", "run 008", e4);
                    Logger.d("ExecuteHttpTask", "run 009");
                    httpURLConnection.disconnect();
                }
            } catch (Throwable th) {
                Logger.d("ExecuteHttpTask", "run 009");
                httpURLConnection.disconnect();
                throw th;
            }
        }
        this.countDownLatch.countDown();
        Logger.d("ExecuteHttpTask", "run end");
    }
}
