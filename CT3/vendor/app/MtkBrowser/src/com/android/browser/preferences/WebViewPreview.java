package com.android.browser.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
/* loaded from: b.zip:com/android/browser/preferences/WebViewPreview.class */
public abstract class WebViewPreview extends Preference implements SharedPreferences.OnSharedPreferenceChangeListener {
    protected WebView mWebView;

    public WebViewPreview(Context context) {
        super(context);
        init(context);
    }

    public WebViewPreview(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context);
    }

    public WebViewPreview(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init(context);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void init(Context context) {
        setLayoutResource(2130968636);
    }

    @Override // android.preference.Preference
    protected void onAttachedToHierarchy(PreferenceManager preferenceManager) {
        super.onAttachedToHierarchy(preferenceManager);
        getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override // android.preference.Preference
    protected void onBindView(View view) {
        super.onBindView(view);
        this.mWebView = (WebView) view.findViewById(2131558560);
        updatePreview(true);
    }

    @Override // android.preference.Preference
    protected View onCreateView(ViewGroup viewGroup) {
        View onCreateView = super.onCreateView(viewGroup);
        WebView webView = (WebView) onCreateView.findViewById(2131558560);
        webView.setFocusable(false);
        webView.setFocusableInTouchMode(false);
        webView.setClickable(false);
        webView.setLongClickable(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setVerticalScrollBarEnabled(false);
        setupWebView(webView);
        return onCreateView;
    }

    @Override // android.preference.Preference
    protected void onPrepareForRemoval() {
        getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPrepareForRemoval();
    }

    @Override // android.content.SharedPreferences.OnSharedPreferenceChangeListener
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String str) {
        updatePreview(false);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void setupWebView(WebView webView) {
    }

    protected abstract void updatePreview(boolean z);
}
