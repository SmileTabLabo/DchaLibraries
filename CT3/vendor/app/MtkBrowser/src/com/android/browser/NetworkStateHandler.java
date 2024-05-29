package com.android.browser;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.webkit.WebView;
import com.mediatek.browser.ext.INetworkStateHandlerExt;
/* loaded from: b.zip:com/android/browser/NetworkStateHandler.class */
public class NetworkStateHandler {
    Activity mActivity;
    Controller mController;
    private boolean mIsNetworkUp;
    private INetworkStateHandlerExt mNetworkExt;
    private IntentFilter mNetworkStateChangedFilter;
    private BroadcastReceiver mNetworkStateIntentReceiver;

    public NetworkStateHandler(Activity activity, Controller controller) {
        this.mActivity = activity;
        this.mController = controller;
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) this.mActivity.getSystemService("connectivity")).getActiveNetworkInfo();
        if (activeNetworkInfo != null) {
            this.mIsNetworkUp = activeNetworkInfo.isAvailable();
        } else {
            this.mNetworkExt = Extensions.getNetworkStateHandlerPlugin(this.mActivity);
            this.mNetworkExt.promptUserToEnableData(this.mActivity);
        }
        this.mNetworkStateChangedFilter = new IntentFilter();
        this.mNetworkStateChangedFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        this.mNetworkStateIntentReceiver = new BroadcastReceiver(this) { // from class: com.android.browser.NetworkStateHandler.1
            final NetworkStateHandler this$0;

            {
                this.this$0 = this;
            }

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                    NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    String typeName = networkInfo.getTypeName();
                    String subtypeName = networkInfo.getSubtypeName();
                    this.this$0.sendNetworkType(typeName.toLowerCase(), subtypeName != null ? subtypeName.toLowerCase() : "");
                    BrowserSettings.getInstance().updateConnectionType();
                    this.this$0.onNetworkToggle(!intent.getBooleanExtra("noConnectivity", false));
                }
            }
        };
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void sendNetworkType(String str, String str2) {
        this.mController.getCurrentWebView();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean isNetworkUp() {
        return this.mIsNetworkUp;
    }

    void onNetworkToggle(boolean z) {
        if (z == this.mIsNetworkUp) {
            return;
        }
        this.mIsNetworkUp = z;
        WebView currentWebView = this.mController.getCurrentWebView();
        if (currentWebView != null) {
            currentWebView.setNetworkAvailable(z);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void onPause() {
        this.mActivity.unregisterReceiver(this.mNetworkStateIntentReceiver);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void onResume() {
        this.mActivity.registerReceiver(this.mNetworkStateIntentReceiver, this.mNetworkStateChangedFilter);
        BrowserSettings.getInstance().updateConnectionType();
    }
}
