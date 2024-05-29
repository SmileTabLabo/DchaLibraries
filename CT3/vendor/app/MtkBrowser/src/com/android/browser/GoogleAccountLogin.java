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
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import libcore.io.Streams;
import libcore.net.http.ResponseUtils;
/* loaded from: b.zip:com/android/browser/GoogleAccountLogin.class */
public class GoogleAccountLogin implements Runnable, AccountManagerCallback<Bundle>, DialogInterface.OnCancelListener {
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
        CookieSyncManager.getInstance().startSync();
        WebViewTimersControl.getInstance().onBrowserActivityResume(this.mWebView);
        this.mWebView.setWebViewClient(new WebViewClient(this) { // from class: com.android.browser.GoogleAccountLogin.1
            final GoogleAccountLogin this$0;

            {
                this.this$0 = this;
            }

            @Override // android.webkit.WebViewClient
            public void onPageFinished(WebView webView, String str) {
                this.this$0.done();
            }

            @Override // android.webkit.WebViewClient
            public boolean shouldOverrideUrlLoading(WebView webView, String str) {
                return false;
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void done() {
        synchronized (this) {
            if (this.mRunnable != null) {
                Log.d("BrowserLogin", "Finished login attempt for " + this.mAccount.name);
                this.mActivity.runOnUiThread(this.mRunnable);
                try {
                    this.mProgressDialog.dismiss();
                } catch (Exception e) {
                    Log.w("BrowserLogin", "Failed to dismiss mProgressDialog: " + e.getMessage());
                }
                this.mRunnable = null;
                this.mActivity.runOnUiThread(new Runnable(this) { // from class: com.android.browser.GoogleAccountLogin.3
                    final GoogleAccountLogin this$0;

                    {
                        this.this$0 = this;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        this.this$0.mWebView.destroy();
                    }
                });
            }
        }
    }

    private static Account[] getAccounts(Context context) {
        return AccountManager.get(context).getAccountsByType("com.google");
    }

    private void invalidateTokens() {
        AccountManager accountManager = AccountManager.get(this.mActivity);
        accountManager.invalidateAuthToken("com.google", this.mSid);
        accountManager.invalidateAuthToken("com.google", this.mLsid);
        this.mTokensInvalidated = true;
        this.mState = 1;
        accountManager.getAuthToken(this.mAccount, "SID", (Bundle) null, this.mActivity, this, (Handler) null);
    }

    private static boolean isLoggedIn() {
        return BrowserSettings.getInstance().getPreferences().getLong("last_autologin_time", -1L) != -1;
    }

    private void saveLoginTime() {
        SharedPreferences.Editor edit = BrowserSettings.getInstance().getPreferences().edit();
        edit.putLong("last_autologin_time", System.currentTimeMillis());
        edit.apply();
    }

    private void startLogin() {
        saveLoginTime();
        this.mProgressDialog = ProgressDialog.show(this.mActivity, this.mActivity.getString(2131493076), this.mActivity.getString(2131493077, new Object[]{this.mAccount.name}), true, true, this);
        this.mState = 1;
        AccountManager.get(this.mActivity).getAuthToken(this.mAccount, "SID", (Bundle) null, this.mActivity, this, (Handler) null);
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

    @Override // android.content.DialogInterface.OnCancelListener
    public void onCancel(DialogInterface dialogInterface) {
        done();
    }

    @Override // java.lang.Runnable
    public void run() {
        HttpURLConnection httpURLConnection = null;
        HttpURLConnection httpURLConnection2 = null;
        try {
            try {
                HttpURLConnection httpURLConnection3 = (HttpURLConnection) new URL(this.ISSUE_AUTH_TOKEN_URL.buildUpon().appendQueryParameter("SID", this.mSid).appendQueryParameter("LSID", this.mLsid).build().toString()).openConnection(Proxy.NO_PROXY);
                httpURLConnection3.setRequestMethod("POST");
                httpURLConnection3.setRequestProperty("User-Agent", this.mUserAgent);
                int responseCode = httpURLConnection3.getResponseCode();
                if (responseCode == 200) {
                    String str = new String(Streams.readFully(httpURLConnection3.getInputStream()), ResponseUtils.responseCharset(httpURLConnection3.getContentType()));
                    if (httpURLConnection3 != null) {
                        httpURLConnection3.disconnect();
                    }
                    this.mActivity.runOnUiThread(new Runnable(this, TOKEN_AUTH_URL.buildUpon().appendQueryParameter("source", "android-browser").appendQueryParameter("auth", str).appendQueryParameter("continue", BrowserSettings.getFactoryResetHomeUrl(this.mActivity)).build().toString()) { // from class: com.android.browser.GoogleAccountLogin.2
                        final GoogleAccountLogin this$0;
                        final String val$newUrl;

                        {
                            this.this$0 = this;
                            this.val$newUrl = r5;
                        }

                        @Override // java.lang.Runnable
                        public void run() {
                            synchronized (this.this$0) {
                                if (this.this$0.mRunnable == null) {
                                    return;
                                }
                                this.this$0.mWebView.loadUrl(this.val$newUrl);
                            }
                        }
                    });
                    return;
                }
                Log.d("BrowserLogin", "LOGIN_FAIL: Bad status from auth url " + responseCode + ": " + httpURLConnection3.getResponseMessage());
                if (responseCode != 403 || this.mTokensInvalidated) {
                    done();
                    if (httpURLConnection3 != null) {
                        httpURLConnection3.disconnect();
                        return;
                    }
                    return;
                }
                Log.d("BrowserLogin", "LOGIN_FAIL: Invalidating tokens...");
                invalidateTokens();
                if (httpURLConnection3 != null) {
                    httpURLConnection3.disconnect();
                }
            } catch (Exception e) {
                Log.d("BrowserLogin", "LOGIN_FAIL: Exception acquiring uber token " + e);
                done();
                if (0 != 0) {
                    httpURLConnection2.disconnect();
                }
            }
        } catch (Throwable th) {
            if (0 != 0) {
                httpURLConnection.disconnect();
            }
            throw th;
        }
    }

    @Override // android.accounts.AccountManagerCallback
    public void run(AccountManagerFuture<Bundle> accountManagerFuture) {
        try {
            String string = accountManagerFuture.getResult().getString("authtoken");
            switch (this.mState) {
                case 0:
                default:
                    throw new IllegalStateException("Impossible to get into this state");
                case 1:
                    this.mSid = string;
                    this.mState = 2;
                    AccountManager.get(this.mActivity).getAuthToken(this.mAccount, "LSID", (Bundle) null, this.mActivity, this, (Handler) null);
                    return;
                case 2:
                    this.mLsid = string;
                    new Thread(this).start();
                    return;
            }
        } catch (Exception e) {
            Log.d("BrowserLogin", "LOGIN_FAIL: Exception in state " + this.mState + " " + e);
            done();
        }
    }
}
