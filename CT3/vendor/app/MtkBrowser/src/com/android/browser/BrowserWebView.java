package com.android.browser;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
/* loaded from: b.zip:com/android/browser/BrowserWebView.class */
public class BrowserWebView extends WebView {
    private boolean mBackgroundRemoved;
    private OnScrollChangedListener mOnScrollChangedListener;
    private TitleBar mTitleBar;
    private WebChromeClient mWebChromeClient;
    private WebViewClient mWebViewClient;

    /* loaded from: b.zip:com/android/browser/BrowserWebView$OnScrollChangedListener.class */
    public interface OnScrollChangedListener {
        void onScrollChanged(int i, int i2, int i3, int i4);
    }

    public BrowserWebView(Context context, AttributeSet attributeSet, int i, boolean z) {
        super(context, attributeSet, i, z);
        this.mBackgroundRemoved = false;
    }

    @Override // android.webkit.WebView
    public void destroy() {
        BrowserSettings.getInstance().stopManagingSettings(getSettings());
        super.destroy();
    }

    public void drawContent(Canvas canvas) {
        onDraw(canvas);
    }

    public int getTitleHeight() {
        return this.mTitleBar != null ? this.mTitleBar.getEmbeddedHeight() : 0;
    }

    @Override // android.webkit.WebView
    public WebChromeClient getWebChromeClient() {
        return this.mWebChromeClient;
    }

    @Override // android.webkit.WebView
    public WebViewClient getWebViewClient() {
        return this.mWebViewClient;
    }

    @Override // android.webkit.WebView, android.view.View
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.mBackgroundRemoved || getRootView().getBackground() == null) {
            return;
        }
        this.mBackgroundRemoved = true;
        post(new Runnable(this) { // from class: com.android.browser.BrowserWebView.1
            final BrowserWebView this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.getRootView().setBackgroundDrawable(null);
            }
        });
    }

    @Override // android.webkit.WebView, android.view.View
    protected void onScrollChanged(int i, int i2, int i3, int i4) {
        super.onScrollChanged(i, i2, i3, i4);
        if (this.mTitleBar != null) {
            this.mTitleBar.onScrollChanged();
        }
        if (this.mOnScrollChangedListener != null) {
            this.mOnScrollChangedListener.onScrollChanged(i, i2, i3, i4);
        }
    }

    public void setOnScrollChangedListener(OnScrollChangedListener onScrollChangedListener) {
        this.mOnScrollChangedListener = onScrollChangedListener;
    }

    public void setTitleBar(TitleBar titleBar) {
        this.mTitleBar = titleBar;
    }

    @Override // android.webkit.WebView
    public void setWebChromeClient(WebChromeClient webChromeClient) {
        this.mWebChromeClient = webChromeClient;
        super.setWebChromeClient(webChromeClient);
    }

    @Override // android.webkit.WebView
    public void setWebViewClient(WebViewClient webViewClient) {
        this.mWebViewClient = webViewClient;
        super.setWebViewClient(webViewClient);
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public boolean showContextMenuForChild(View view) {
        return false;
    }
}
