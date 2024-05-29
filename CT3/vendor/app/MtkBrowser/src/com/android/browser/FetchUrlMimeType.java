package com.android.browser;

import android.app.DownloadManager;
import android.content.Context;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import com.mediatek.browser.ext.IBrowserDownloadExt;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: b.zip:com/android/browser/FetchUrlMimeType.class */
public class FetchUrlMimeType extends Thread {
    private IBrowserDownloadExt mBrowserDownloadExt = null;
    private Context mContext;
    private String mCookies;
    private DownloadManager.Request mRequest;
    private String mUri;
    private String mUserAgent;

    public FetchUrlMimeType(Context context, DownloadManager.Request request, String str, String str2, String str3) {
        this.mContext = context.getApplicationContext();
        this.mRequest = request;
        this.mUri = str;
        this.mCookies = str2;
        this.mUserAgent = str3;
    }

    /* JADX WARN: Code restructure failed: missing block: B:34:0x00ec, code lost:
        if (r0.getResponseCode() == 400) goto L69;
     */
    /* JADX WARN: Code restructure failed: missing block: B:79:0x022f, code lost:
        if (r15.equalsIgnoreCase("application/octet-stream") != false) goto L63;
     */
    @Override // java.lang.Thread, java.lang.Runnable
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public void run() {
        String str;
        String str2;
        HttpURLConnection httpURLConnection;
        String str3 = null;
        HttpURLConnection httpURLConnection2 = null;
        String str4 = null;
        HttpURLConnection httpURLConnection3 = null;
        try {
            try {
                HttpURLConnection httpURLConnection4 = (HttpURLConnection) new URL(this.mUri).openConnection();
                httpURLConnection4.setRequestMethod("HEAD");
                if (this.mUserAgent != null) {
                    httpURLConnection4.addRequestProperty("User-Agent", this.mUserAgent);
                }
                if (this.mCookies != null && this.mCookies.length() > 0) {
                    httpURLConnection4.addRequestProperty("Cookie", this.mCookies);
                }
                if (httpURLConnection4.getResponseCode() != 501) {
                    httpURLConnection = httpURLConnection4;
                }
                Log.d("Browser/FetchMimeType", "FetchUrlMimeType:  use Get method");
                httpURLConnection4.disconnect();
                HttpURLConnection httpURLConnection5 = (HttpURLConnection) new URL(this.mUri).openConnection();
                httpURLConnection = httpURLConnection5;
                if (this.mUserAgent != null) {
                    httpURLConnection5.addRequestProperty("User-Agent", this.mUserAgent);
                    httpURLConnection = httpURLConnection5;
                }
                String str5 = null;
                if (httpURLConnection.getResponseCode() == 200) {
                    HttpURLConnection httpURLConnection6 = httpURLConnection;
                    String contentType = httpURLConnection.getContentType();
                    String str6 = contentType;
                    if (contentType != null) {
                        int indexOf = contentType.indexOf(59);
                        str6 = contentType;
                        if (indexOf != -1) {
                            str6 = contentType.substring(0, indexOf);
                        }
                    }
                    httpURLConnection2 = httpURLConnection;
                    str4 = str6;
                    httpURLConnection3 = httpURLConnection;
                    str3 = str6;
                    str5 = httpURLConnection.getHeaderField("Content-Disposition");
                }
                str = str5;
                str2 = str3;
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                    str2 = str3;
                    str = str5;
                }
            } catch (IOException e) {
                HttpURLConnection httpURLConnection7 = httpURLConnection2;
                HttpURLConnection httpURLConnection8 = httpURLConnection2;
                Log.e("FetchUrlMimeType", "Download failed: " + e);
                str = null;
                str2 = str4;
                if (httpURLConnection2 != null) {
                    httpURLConnection2.disconnect();
                    str = null;
                    str2 = str4;
                }
            }
            String str7 = str2;
            if (str2 != null) {
                if (!str2.equalsIgnoreCase("text/plain")) {
                    str7 = str2;
                }
                String mimeTypeFromExtension = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(this.mUri));
                str7 = str2;
                if (mimeTypeFromExtension != null) {
                    str7 = mimeTypeFromExtension;
                    this.mRequest.setMimeType(mimeTypeFromExtension);
                }
            }
            String guessFileName = URLUtil.guessFileName(this.mUri, str, str7);
            Log.d("Browser/FetchMimeType", "FetchUrlMimeType: Guess file name is: " + guessFileName + " mimeType is: " + str7);
            this.mBrowserDownloadExt = Extensions.getDownloadPlugin(this.mContext);
            this.mBrowserDownloadExt.setRequestDestinationDir(BrowserSettings.getInstance().getDownloadPath(), this.mRequest, guessFileName, str7);
            ((DownloadManager) this.mContext.getSystemService("download")).enqueue(this.mRequest);
        } catch (Throwable th) {
            if (httpURLConnection3 != null) {
                httpURLConnection3.disconnect();
            }
            throw th;
        }
    }
}
