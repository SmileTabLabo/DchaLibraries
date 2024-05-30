package jp.co.benesse.dcha.setupwizard.http;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import jp.co.benesse.dcha.setupwizard.http.Request;
import jp.co.benesse.dcha.util.Logger;
/* loaded from: classes.dex */
public class HttpThread extends Thread {
    private static final String TAG = HttpThread.class.getSimpleName();
    protected volatile boolean mIsRunning = false;
    protected Condition mListCondition;
    protected Lock mListLock;
    protected Collection<Request> mRequestList;
    protected Request.ResponseListener mResponseListener;
    protected Condition mRetryWaitCondition;
    protected Lock mRetryWaitLock;

    public HttpThread() {
        Logger.d(TAG, "HttpThread 0001");
        this.mRequestList = new ArrayList();
        this.mListLock = new ReentrantLock();
        this.mListCondition = this.mListLock.newCondition();
        this.mRetryWaitLock = new ReentrantLock();
        this.mRetryWaitCondition = this.mRetryWaitLock.newCondition();
        this.mResponseListener = null;
        Logger.d(TAG, "HttpThread 0002");
    }

    @Override // java.lang.Thread
    public void start() {
        this.mIsRunning = true;
        super.start();
    }

    /* JADX WARN: Removed duplicated region for block: B:22:0x008e  */
    /* JADX WARN: Removed duplicated region for block: B:64:0x014e A[Catch: all -> 0x0199, TryCatch #7 {all -> 0x0199, blocks: (B:23:0x009a, B:25:0x00a0, B:27:0x00a6, B:30:0x00be, B:39:0x00e4, B:41:0x00ec, B:42:0x00f8, B:46:0x011c, B:47:0x011e, B:52:0x0130, B:56:0x013a, B:57:0x013d, B:58:0x013e, B:60:0x0144, B:62:0x0148, B:64:0x014e, B:66:0x0161, B:67:0x0172, B:45:0x0100, B:51:0x0125), top: B:94:0x009a }] */
    /* JADX WARN: Removed duplicated region for block: B:65:0x015f  */
    @Override // java.lang.Thread, java.lang.Runnable
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public void run() {
        Request request;
        HttpURLConnection httpURLConnection;
        Throwable th;
        Lock lock;
        Logger.d(TAG, "run 0001");
        super.run();
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);
        do {
            Response response = null;
            try {
                try {
                } finally {
                }
            } catch (InterruptedException unused) {
                Logger.d(TAG, "run 0005");
                this.mListLock.unlock();
                request = null;
            }
            if (this.mListLock.tryLock(100L, TimeUnit.MILLISECONDS)) {
                Logger.d(TAG, "run 0002");
                if (isRunning() && this.mRequestList.isEmpty()) {
                    Logger.d(TAG, "run 0003");
                    this.mListCondition.await();
                }
                Iterator<Request> it = this.mRequestList.iterator();
                if (it.hasNext()) {
                    Logger.d(TAG, "run 0004");
                    request = it.next();
                    if (request != null) {
                        Logger.d(TAG, "run 0006");
                        int i = 0;
                        while (true) {
                            try {
                                if (!isRunning() || request.isCancelled()) {
                                    break;
                                }
                                Logger.d(TAG, "run 0007");
                                try {
                                    httpURLConnection = openConnection(request.url);
                                    try {
                                        try {
                                            sendRequest(httpURLConnection, request);
                                            response = receiveResponse(httpURLConnection, request);
                                            disconnect(httpURLConnection);
                                            break;
                                        } catch (Throwable th2) {
                                            th = th2;
                                            disconnect(httpURLConnection);
                                            throw th;
                                        }
                                    } catch (Exception e) {
                                        e = e;
                                        Logger.d(TAG, "run 0008");
                                        Logger.d(TAG, "run", e);
                                        disconnect(httpURLConnection);
                                        i++;
                                        if (i > request.maxNumRetries) {
                                            Logger.d(TAG, "run 0009");
                                            if (isRunning()) {
                                                if (!request.isCancelled()) {
                                                }
                                            }
                                            this.mListLock.lock();
                                            this.mRequestList.remove(request);
                                            if (!isRunning()) {
                                                Logger.d(TAG, "run 0015");
                                            }
                                        } else if (request.retryInterval > 0) {
                                            try {
                                                Logger.d(TAG, "run 0010");
                                                this.mRetryWaitLock.lock();
                                                this.mRetryWaitCondition.await(request.retryInterval * i, TimeUnit.MILLISECONDS);
                                                lock = this.mRetryWaitLock;
                                            } catch (InterruptedException unused2) {
                                                Logger.d(TAG, "run 0011");
                                                lock = this.mRetryWaitLock;
                                            }
                                            lock.unlock();
                                        }
                                    }
                                } catch (Exception e2) {
                                    e = e2;
                                    httpURLConnection = response;
                                } catch (Throwable th3) {
                                    httpURLConnection = response;
                                    th = th3;
                                }
                            } catch (Throwable th4) {
                                try {
                                    this.mListLock.lock();
                                    this.mRequestList.remove(request);
                                    throw th4;
                                } finally {
                                }
                            }
                        }
                        if (isRunning() && request.responseListener != null) {
                            if (!request.isCancelled()) {
                                Logger.d(TAG, "run 0012");
                                request.responseListener.onHttpCancelled(request);
                            } else if (response == null) {
                                Logger.d(TAG, "run 0013");
                                request.responseListener.onHttpError(request);
                            } else {
                                Logger.d(TAG, "run 0014");
                                request.responseListener.onHttpResponse(response);
                            }
                        }
                        try {
                            this.mListLock.lock();
                            this.mRequestList.remove(request);
                        } finally {
                        }
                    }
                }
            }
            request = null;
            if (request != null) {
            }
        } while (!isRunning());
        Logger.d(TAG, "run 0015");
    }

    public boolean isRunning() {
        Logger.d(TAG, "isRunning 0001");
        return this.mIsRunning;
    }

    public void stopRunning() {
        Logger.d(TAG, "stopRunning 0001");
        this.mIsRunning = false;
        cancel();
    }

    public void cancel() {
        Logger.d(TAG, "cancel 0001");
        try {
            this.mListLock.lock();
            for (Request request : this.mRequestList) {
                Logger.d(TAG, "cancel 0002");
                request.cancel();
            }
            this.mListCondition.signal();
            try {
                this.mRetryWaitLock.lock();
                this.mRetryWaitCondition.signal();
                this.mRetryWaitLock.unlock();
                Logger.d(TAG, "cancel 0003");
            } catch (Throwable th) {
                this.mRetryWaitLock.unlock();
                throw th;
            }
        } finally {
            this.mListLock.unlock();
        }
    }

    public void postRequest(Request request) {
        Logger.d(TAG, "postRequest 0001");
        try {
            this.mListLock.lock();
            request.responseListener = this.mResponseListener;
            this.mRequestList.add(request);
            this.mListCondition.signal();
            this.mListLock.unlock();
            Logger.d(TAG, "postRequest 0002");
        } catch (Throwable th) {
            this.mListLock.unlock();
            throw th;
        }
    }

    public void setResponseListener(Request.ResponseListener responseListener) {
        Logger.d(TAG, "setResponseListener 0001");
        this.mResponseListener = responseListener;
        Logger.d(TAG, "setResponseListener 0002");
    }

    protected void sendRequest(HttpURLConnection httpURLConnection, Request request) throws IOException {
        Logger.d(TAG, "sendRequest 0001");
        httpURLConnection.setReadTimeout(request.readTimeout);
        httpURLConnection.setConnectTimeout(request.connectTimeout);
        httpURLConnection.setRequestMethod(request.method);
        httpURLConnection.setInstanceFollowRedirects(request.followRedirects);
        httpURLConnection.setDoInput(request.doInput);
        httpURLConnection.setDoOutput(request.doOutput);
        httpURLConnection.setAllowUserInteraction(request.allowUserInteraction);
        httpURLConnection.setUseCaches(request.useCaches);
        httpURLConnection.setRequestProperty("Connection", "close");
        for (Map.Entry<String, String> entry : request.requestProperty.entrySet()) {
            String str = TAG;
            Logger.d(str, "sendRequest 0002 key:" + entry.getKey() + " value:" + entry.getValue());
            httpURLConnection.setRequestProperty(entry.getKey(), entry.getValue());
        }
        httpURLConnection.connect();
        request.onSendData(httpURLConnection);
        Logger.d(TAG, "sendRequest 0003");
    }

    protected Response receiveResponse(HttpURLConnection httpURLConnection, Request request) throws IOException {
        Logger.d(TAG, "receiveResponse 0001");
        Response newResponseInstance = newResponseInstance(request.getResponseClass());
        newResponseInstance.request = request;
        newResponseInstance.responseCode = httpURLConnection.getResponseCode();
        if (newResponseInstance.isSuccess()) {
            Logger.d(TAG, "receiveResponse 0002");
            Map<String, List<String>> headerFields = httpURLConnection.getHeaderFields();
            newResponseInstance.contentLength = getContentLength(headerFields);
            newResponseInstance.contentType = getContentType(headerFields);
            newResponseInstance.onReceiveData(httpURLConnection);
        }
        Logger.d(TAG, "receiveResponse 0003");
        return newResponseInstance;
    }

    protected Response newResponseInstance(Class<? extends Response> cls) {
        Response response;
        Logger.d(TAG, "newResponseInstance 0001");
        try {
            response = cls.getConstructor(new Class[0]).newInstance(new Object[0]);
        } catch (Exception e) {
            Logger.d(TAG, "newResponseInstance 0002");
            Logger.d(TAG, "newResponseInstance", e);
            response = null;
        }
        Logger.d(TAG, "newResponseInstance 0003");
        return response;
    }

    protected long getContentLength(Map<String, List<String>> map) {
        long j;
        Logger.d(TAG, "getContentLength 0001");
        List<String> list = map.get("Content-Length");
        if (list == null || list.isEmpty()) {
            j = 0;
        } else {
            Logger.d(TAG, "getContentLength 0002");
            j = Long.parseLong(list.get(0));
        }
        Logger.d(TAG, "getContentLength 0003");
        return j;
    }

    protected String getContentType(Map<String, List<String>> map) {
        String str;
        Logger.d(TAG, "getContentType 0001");
        List<String> list = map.get("Content-Type");
        if (list == null || list.isEmpty()) {
            str = null;
        } else {
            Logger.d(TAG, "getContentType 0002");
            str = list.get(0);
        }
        Logger.d(TAG, "getContentType 0003");
        return str;
    }

    protected HttpURLConnection openConnection(URL url) throws IOException {
        return (HttpURLConnection) url.openConnection();
    }

    protected void disconnect(HttpURLConnection httpURLConnection) {
        if (httpURLConnection != null) {
            httpURLConnection.disconnect();
        }
    }
}
