package com.android.browser;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.webkit.WebView;
/* loaded from: b.zip:com/android/browser/DeviceAccountLogin.class */
public class DeviceAccountLogin implements AccountManagerCallback<Bundle> {
    private final AccountManager mAccountManager;
    Account[] mAccounts;
    private final Activity mActivity;
    private String mAuthToken;
    private AutoLoginCallback mCallback;
    private int mState = 0;
    private final Tab mTab;
    private final WebView mWebView;
    private final WebViewController mWebViewController;

    /* loaded from: b.zip:com/android/browser/DeviceAccountLogin$AutoLoginCallback.class */
    public interface AutoLoginCallback {
        void loginFailed();
    }

    public DeviceAccountLogin(Activity activity, WebView webView, Tab tab, WebViewController webViewController) {
        this.mActivity = activity;
        this.mWebView = webView;
        this.mTab = tab;
        this.mWebViewController = webViewController;
        this.mAccountManager = AccountManager.get(activity);
    }

    private void displayLoginUi() {
        this.mTab.setDeviceAccountLogin(this);
        if (this.mTab.inForeground()) {
            this.mWebViewController.showAutoLogin(this.mTab);
        }
    }

    private void loginFailed() {
        this.mState = 1;
        if (this.mTab.getDeviceAccountLogin() == null) {
            displayLoginUi();
        } else if (this.mCallback != null) {
            this.mCallback.loginFailed();
        }
    }

    public void cancel() {
        this.mTab.setDeviceAccountLogin(null);
    }

    public String[] getAccountNames() {
        String[] strArr = new String[this.mAccounts.length];
        for (int i = 0; i < this.mAccounts.length; i++) {
            strArr[i] = this.mAccounts[i].name;
        }
        return strArr;
    }

    public int getState() {
        return this.mState;
    }

    public void handleLogin(String str, String str2, String str3) {
        Account[] accountArr;
        this.mAccounts = this.mAccountManager.getAccountsByType(str);
        this.mAuthToken = "weblogin:" + str3;
        if (this.mAccounts.length == 0) {
            return;
        }
        for (Account account : this.mAccounts) {
            if (account.name.equals(str2)) {
                this.mAccountManager.getAuthToken(account, this.mAuthToken, (Bundle) null, this.mActivity, this, (Handler) null);
                return;
            }
        }
        displayLoginUi();
    }

    public void login(int i, AutoLoginCallback autoLoginCallback) {
        this.mState = 2;
        this.mCallback = autoLoginCallback;
        this.mAccountManager.getAuthToken(this.mAccounts[i], this.mAuthToken, (Bundle) null, this.mActivity, this, (Handler) null);
    }

    @Override // android.accounts.AccountManagerCallback
    public void run(AccountManagerFuture<Bundle> accountManagerFuture) {
        try {
            String string = accountManagerFuture.getResult().getString("authtoken");
            if (string == null) {
                loginFailed();
            } else {
                this.mWebView.loadUrl(string);
                this.mTab.setDeviceAccountLogin(null);
                if (this.mTab.inForeground()) {
                    this.mWebViewController.hideAutoLogin(this.mTab);
                }
            }
        } catch (Exception e) {
            loginFailed();
        }
    }
}
