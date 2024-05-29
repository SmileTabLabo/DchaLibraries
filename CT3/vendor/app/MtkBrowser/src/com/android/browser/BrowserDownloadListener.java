package com.android.browser;

import android.webkit.DownloadListener;
/* loaded from: b.zip:com/android/browser/BrowserDownloadListener.class */
public abstract class BrowserDownloadListener implements DownloadListener {
    @Override // android.webkit.DownloadListener
    public void onDownloadStart(String str, String str2, String str3, String str4, long j) {
        onDownloadStart(str, str2, str3, str4, null, j);
    }

    public abstract void onDownloadStart(String str, String str2, String str3, String str4, String str5, long j);
}
