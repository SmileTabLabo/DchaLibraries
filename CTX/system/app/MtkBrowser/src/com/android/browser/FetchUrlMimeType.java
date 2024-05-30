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
/* loaded from: classes.dex */
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

    /* JADX WARN: Removed duplicated region for block: B:48:0x00c7  */
    /* JADX WARN: Removed duplicated region for block: B:51:0x00cf  */
    @Override // java.lang.Thread, java.lang.Runnable
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public void run() {
        HttpURLConnection httpURLConnection;
        Throwable th;
        HttpURLConnection httpURLConnection2;
        String str;
        String str2;
        String mimeTypeFromExtension;
        String str3 = null;
        try {
            try {
                httpURLConnection2 = (HttpURLConnection) new URL(this.mUri).openConnection();
                try {
                    httpURLConnection2.setRequestMethod("HEAD");
                    if (this.mUserAgent != null) {
                        httpURLConnection2.addRequestProperty("User-Agent", this.mUserAgent);
                    }
                    if (this.mCookies != null && this.mCookies.length() > 0) {
                        httpURLConnection2.addRequestProperty("Cookie", this.mCookies);
                    }
                    if (httpURLConnection2.getResponseCode() == 501 || httpURLConnection2.getResponseCode() == 400) {
                        Log.d("Browser/FetchMimeType", "FetchUrlMimeType:  use Get method");
                        httpURLConnection2.disconnect();
                        HttpURLConnection httpURLConnection3 = (HttpURLConnection) new URL(this.mUri).openConnection();
                        try {
                            if (this.mUserAgent != null) {
                                httpURLConnection3.addRequestProperty("User-Agent", this.mUserAgent);
                            }
                            httpURLConnection2 = httpURLConnection3;
                        } catch (IOException e) {
                            str = null;
                            e = e;
                            httpURLConnection2 = httpURLConnection3;
                            Log.e("FetchUrlMimeType", "Download failed: " + e);
                            if (httpURLConnection2 != null) {
                            }
                            String str4 = str;
                            str2 = null;
                            str3 = str4;
                            if (str3 != null) {
                            }
                            String guessFileName = URLUtil.guessFileName(this.mUri, str2, str3);
                            Log.d("Browser/FetchMimeType", "FetchUrlMimeType: Guess file name is: " + guessFileName + " mimeType is: " + str3);
                            this.mBrowserDownloadExt = Extensions.getDownloadPlugin(this.mContext);
                            this.mBrowserDownloadExt.setRequestDestinationDir(BrowserSettings.getInstance().getDownloadPath(), this.mRequest, guessFileName, str3);
                            ((DownloadManager) this.mContext.getSystemService("download")).enqueue(this.mRequest);
                        } catch (Throwable th2) {
                            th = th2;
                            httpURLConnection = httpURLConnection3;
                            if (httpURLConnection != null) {
                                httpURLConnection.disconnect();
                            }
                            throw th;
                        }
                    }
                    if (httpURLConnection2.getResponseCode() == 200) {
                        String contentType = httpURLConnection2.getContentType();
                        if (contentType != null) {
                            try {
                                int indexOf = contentType.indexOf(59);
                                if (indexOf != -1) {
                                    contentType = contentType.substring(0, indexOf);
                                }
                            } catch (IOException e2) {
                                str = contentType;
                                e = e2;
                                Log.e("FetchUrlMimeType", "Download failed: " + e);
                                if (httpURLConnection2 != null) {
                                    httpURLConnection2.disconnect();
                                }
                                String str42 = str;
                                str2 = null;
                                str3 = str42;
                                if (str3 != null) {
                                    this.mRequest.setMimeType(mimeTypeFromExtension);
                                    str3 = mimeTypeFromExtension;
                                }
                                String guessFileName2 = URLUtil.guessFileName(this.mUri, str2, str3);
                                Log.d("Browser/FetchMimeType", "FetchUrlMimeType: Guess file name is: " + guessFileName2 + " mimeType is: " + str3);
                                this.mBrowserDownloadExt = Extensions.getDownloadPlugin(this.mContext);
                                this.mBrowserDownloadExt.setRequestDestinationDir(BrowserSettings.getInstance().getDownloadPath(), this.mRequest, guessFileName2, str3);
                                ((DownloadManager) this.mContext.getSystemService("download")).enqueue(this.mRequest);
                            }
                        }
                        str2 = httpURLConnection2.getHeaderField("Content-Disposition");
                        str3 = contentType;
                    } else {
                        str2 = null;
                    }
                    if (httpURLConnection2 != null) {
                        httpURLConnection2.disconnect();
                    }
                } catch (IOException e3) {
                    e = e3;
                    str = null;
                }
            } catch (Throwable th3) {
                th = th3;
            }
        } catch (IOException e4) {
            e = e4;
            httpURLConnection2 = null;
            str = null;
        } catch (Throwable th4) {
            httpURLConnection = null;
            th = th4;
        }
        if (str3 != null && ((str3.equalsIgnoreCase("text/plain") || str3.equalsIgnoreCase("application/octet-stream")) && (mimeTypeFromExtension = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(this.mUri))) != null)) {
            this.mRequest.setMimeType(mimeTypeFromExtension);
            str3 = mimeTypeFromExtension;
        }
        String guessFileName22 = URLUtil.guessFileName(this.mUri, str2, str3);
        Log.d("Browser/FetchMimeType", "FetchUrlMimeType: Guess file name is: " + guessFileName22 + " mimeType is: " + str3);
        this.mBrowserDownloadExt = Extensions.getDownloadPlugin(this.mContext);
        this.mBrowserDownloadExt.setRequestDestinationDir(BrowserSettings.getInstance().getDownloadPath(), this.mRequest, guessFileName22, str3);
        ((DownloadManager) this.mContext.getSystemService("download")).enqueue(this.mRequest);
    }
}
