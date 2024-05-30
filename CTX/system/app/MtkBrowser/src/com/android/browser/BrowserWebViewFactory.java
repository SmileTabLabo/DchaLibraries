package com.android.browser;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.AttributeSet;
import android.webkit.CookieManager;
import android.webkit.WebView;
/* loaded from: classes.dex */
public class BrowserWebViewFactory implements WebViewFactory {
    private final Context mContext;

    public BrowserWebViewFactory(Context context) {
        this.mContext = context;
    }

    protected BrowserWebView instantiateWebView(AttributeSet attributeSet, int i, boolean z) {
        return new BrowserWebView(this.mContext, attributeSet, i, z);
    }

    @Override // com.android.browser.WebViewFactory
    public WebView createWebView(boolean z) {
        BrowserWebView instantiateWebView = instantiateWebView(null, 16842885, z);
        initWebViewSettings(instantiateWebView);
        instantiateWebView.getSettings().setJavaScriptEnabled(true);
        instantiateWebView.addJavascriptInterface(new WebAppInterface(instantiateWebView), "injectedObject");
        return instantiateWebView;
    }

    protected void initWebViewSettings(WebView webView) {
        webView.setScrollbarFadingEnabled(true);
        webView.setScrollBarStyle(33554432);
        boolean z = false;
        webView.setMapTrackballToArrowKeys(false);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.setOverScrollMode(2);
        PackageManager packageManager = this.mContext.getPackageManager();
        if (packageManager.hasSystemFeature("android.hardware.touchscreen.multitouch") || packageManager.hasSystemFeature("android.hardware.faketouch.multitouch.distinct")) {
            z = true;
        }
        webView.getSettings().setDisplayZoomControls(!z);
        BrowserSettings.getInstance().startManagingSettings(webView.getSettings());
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptThirdPartyCookies(webView, cookieManager.acceptCookie());
        if (Build.VERSION.SDK_INT >= 19) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
    }
}
