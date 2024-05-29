package com.android.browser;

import android.os.Looper;
import android.util.Log;
import android.webkit.WebView;
/* loaded from: b.zip:com/android/browser/WebViewTimersControl.class */
public class WebViewTimersControl {
    private static WebViewTimersControl sInstance;
    private boolean mBrowserActive;
    private boolean mPrerenderActive;

    private WebViewTimersControl() {
    }

    public static WebViewTimersControl getInstance() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new IllegalStateException("WebViewTimersControl.get() called on wrong thread");
        }
        if (sInstance == null) {
            sInstance = new WebViewTimersControl();
        }
        return sInstance;
    }

    private void maybePauseTimers(WebView webView) {
        if (this.mBrowserActive || this.mPrerenderActive || webView == null) {
            return;
        }
        Log.d("WebViewTimersControl", "Pausing webview timers, view=" + webView);
        webView.pauseTimers();
    }

    private void resumeTimers(WebView webView) {
        Log.d("WebViewTimersControl", "Resuming webview timers, view=" + webView);
        if (webView != null) {
            webView.resumeTimers();
        }
    }

    public void onBrowserActivityPause(WebView webView) {
        Log.d("WebViewTimersControl", "onBrowserActivityPause");
        this.mBrowserActive = false;
        maybePauseTimers(webView);
    }

    public void onBrowserActivityResume(WebView webView) {
        Log.d("WebViewTimersControl", "onBrowserActivityResume");
        this.mBrowserActive = true;
        resumeTimers(webView);
    }

    public void onPrerenderDone(WebView webView) {
        Log.d("WebViewTimersControl", "onPrerenderDone");
        this.mPrerenderActive = false;
        maybePauseTimers(webView);
    }

    public void onPrerenderStart(WebView webView) {
        Log.d("WebViewTimersControl", "onPrerenderStart");
        this.mPrerenderActive = true;
        resumeTimers(webView);
    }
}
