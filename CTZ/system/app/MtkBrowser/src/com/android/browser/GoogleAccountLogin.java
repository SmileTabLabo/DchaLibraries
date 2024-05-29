package com.android.browser;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.HashMap;
import libcore.io.Streams;
import libcore.net.http.ResponseUtils;
/* loaded from: classes.dex */
public class GoogleAccountLogin implements AccountManagerCallback<Bundle>, DialogInterface.OnCancelListener, Runnable {
    private static final boolean DEBUG = Browser.DEBUG;
    private static final Uri TOKEN_AUTH_URL = Uri.parse("https://www.google.com/accounts/TokenAuth");
    private Uri ISSUE_AUTH_TOKEN_URL = Uri.parse("https://www.google.com/accounts/IssueAuthToken?service=gaia&Session=false");
    private final Account mAccount;
    private final Activity mActivity;
    private String mLsid;
    private ProgressDialog mProgressDialog;
    private Runnable mRunnable;
    private String mSid;
    private int mState;
    private boolean mTokensInvalidated;
    private String mUserAgent;
    private final WebView mWebView;

    private GoogleAccountLogin(Activity activity, Account account, Runnable runnable) {
        this.mActivity = activity;
        this.mAccount = account;
        this.mWebView = new WebView(this.mActivity);
        this.mRunnable = runnable;
        this.mUserAgent = this.mWebView.getSettings().getUserAgentString();
        WebViewTimersControl.getInstance().onBrowserActivityResume(this.mWebView, ((BrowserActivity) this.mActivity).getController());
        this.mWebView.setWebViewClient(new WebViewClient() { // from class: com.android.browser.GoogleAccountLogin.1
            @Override // android.webkit.WebViewClient
            public boolean shouldOverrideUrlLoading(WebView webView, String str) {
                return false;
            }

            @Override // android.webkit.WebViewClient
            public void onPageFinished(WebView webView, String str) {
                GoogleAccountLogin.this.done();
            }
        });
    }

    private void saveLoginTime() {
        SharedPreferences.Editor edit = BrowserSettings.getInstance().getPreferences().edit();
        edit.putLong("last_autologin_time", System.currentTimeMillis());
        edit.apply();
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:36:0x0100  */
    /* JADX WARN: Type inference failed for: r0v10, types: [java.net.HttpURLConnection] */
    /* JADX WARN: Type inference failed for: r0v5, types: [java.lang.String] */
    /* JADX WARN: Type inference failed for: r0v7 */
    @Override // java.lang.Runnable
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public void run() {
        Throwable th;
        Exception e;
        HttpURLConnection httpURLConnection;
        ?? uri = this.ISSUE_AUTH_TOKEN_URL.buildUpon().appendQueryParameter("SID", this.mSid).appendQueryParameter("LSID", this.mLsid).build().toString();
        try {
            try {
                httpURLConnection = (HttpURLConnection) new URL(uri).openConnection(Proxy.NO_PROXY);
                try {
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setRequestProperty("User-Agent", this.mUserAgent);
                    int responseCode = httpURLConnection.getResponseCode();
                    if (responseCode == 200) {
                        String str = new String(Streams.readFully(httpURLConnection.getInputStream()), ResponseUtils.responseCharset(httpURLConnection.getContentType()));
                        if (httpURLConnection != null) {
                            httpURLConnection.disconnect();
                        }
                        final String uri2 = TOKEN_AUTH_URL.buildUpon().appendQueryParameter("source", "android-browser").appendQueryParameter("auth", str).appendQueryParameter("continue", BrowserSettings.getFactoryResetHomeUrl(this.mActivity)).build().toString();
                        this.mActivity.runOnUiThread(new Runnable() { // from class: com.android.browser.GoogleAccountLogin.2
                            @Override // java.lang.Runnable
                            public void run() {
                                synchronized (GoogleAccountLogin.this) {
                                    if (GoogleAccountLogin.this.mRunnable == null) {
                                        return;
                                    }
                                    HashMap hashMap = new HashMap();
                                    hashMap.put(Browser.HEADER, Browser.UAPROF);
                                    GoogleAccountLogin.this.mWebView.loadUrl(uri2, hashMap);
                                }
                            }
                        });
                        return;
                    }
                    Log.d("BrowserLogin", "LOGIN_FAIL: Bad status from auth url " + responseCode + ": " + httpURLConnection.getResponseMessage());
                    if (responseCode != 403 || this.mTokensInvalidated) {
                        done();
                        if (httpURLConnection != null) {
                            httpURLConnection.disconnect();
                            return;
                        }
                        return;
                    }
                    Log.d("BrowserLogin", "LOGIN_FAIL: Invalidating tokens...");
                    invalidateTokens();
                    if (httpURLConnection != null) {
                        httpURLConnection.disconnect();
                    }
                } catch (Exception e2) {
                    e = e2;
                    Log.d("BrowserLogin", "LOGIN_FAIL: Exception acquiring uber token " + e);
                    done();
                    if (httpURLConnection != null) {
                        httpURLConnection.disconnect();
                    }
                }
            } catch (Throwable th2) {
                th = th2;
                if (uri != 0) {
                    uri.disconnect();
                }
                throw th;
            }
        } catch (Exception e3) {
            e = e3;
            httpURLConnection = null;
        } catch (Throwable th3) {
            th = th3;
            uri = 0;
            if (uri != 0) {
            }
            throw th;
        }
    }

    private void invalidateTokens() {
        AccountManager accountManager = AccountManager.get(this.mActivity);
        accountManager.invalidateAuthToken("com.google", this.mSid);
        accountManager.invalidateAuthToken("com.google", this.mLsid);
        this.mTokensInvalidated = true;
        this.mState = 1;
        accountManager.getAuthToken(this.mAccount, "SID", (Bundle) null, this.mActivity, this, (Handler) null);
    }

    @Override // android.accounts.AccountManagerCallback
    public void run(AccountManagerFuture<Bundle> accountManagerFuture) {
        try {
            String string = accountManagerFuture.getResult().getString("authtoken");
            switch (this.mState) {
                case 1:
                    this.mSid = string;
                    this.mState = 2;
                    AccountManager.get(this.mActivity).getAuthToken(this.mAccount, "LSID", (Bundle) null, this.mActivity, this, (Handler) null);
                    break;
                case 2:
                    this.mLsid = string;
                    new Thread(this).start();
                    break;
                default:
                    throw new IllegalStateException("Impossible to get into this state");
            }
        } catch (Exception e) {
            Log.d("BrowserLogin", "LOGIN_FAIL: Exception in state " + this.mState + " " + e);
            done();
        }
    }

    public static void startLoginIfNeeded(Activity activity, Runnable runnable) {
        if (isLoggedIn()) {
            runnable.run();
            return;
        }
        Account[] accounts = getAccounts(activity);
        if (accounts == null || accounts.length == 0) {
            runnable.run();
        } else {
            new GoogleAccountLogin(activity, accounts[0], runnable).startLogin();
        }
    }

    private void startLogin() {
        saveLoginTime();
        this.mProgressDialog = ProgressDialog.show(this.mActivity, this.mActivity.getString(R.string.pref_autologin_title), this.mActivity.getString(R.string.pref_autologin_progress, new Object[]{this.mAccount.name}), true, true, this);
        this.mState = 1;
        AccountManager.get(this.mActivity).getAuthToken(this.mAccount, "SID", (Bundle) null, this.mActivity, this, (Handler) null);
    }

    private static Account[] getAccounts(Context context) {
        return AccountManager.get(context).getAccountsByType("com.google");
    }

    private static boolean isLoggedIn() {
        if (BrowserSettings.getInstance().getPreferences().getLong("last_autologin_time", -1L) == -1) {
            return false;
        }
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public synchronized void done() {
        if (this.mRunnable != null) {
            if (DEBUG) {
                Log.d("BrowserLogin", "Finished login attempt for " + this.mAccount.name);
            }
            this.mActivity.runOnUiThread(this.mRunnable);
            try {
                this.mProgressDialog.dismiss();
            } catch (Exception e) {
                Log.w("BrowserLogin", "Failed to dismiss mProgressDialog: " + e.getMessage());
            }
            this.mRunnable = null;
            this.mActivity.runOnUiThread(new Runnable() { // from class: com.android.browser.GoogleAccountLogin.3
                @Override // java.lang.Runnable
                public void run() {
                    GoogleAccountLogin.this.mWebView.destroy();
                }
            });
        }
    }

    @Override // android.content.DialogInterface.OnCancelListener
    public void onCancel(DialogInterface dialogInterface) {
        done();
    }
}
