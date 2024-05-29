package com.android.settings;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserManager;
import android.util.Log;
import android.webkit.IWebViewUpdateService;
import android.webkit.WebViewProviderInfo;
import java.util.ArrayList;
/* loaded from: classes.dex */
public class WebViewImplementation extends InstrumentedActivity implements DialogInterface.OnCancelListener, DialogInterface.OnDismissListener {
    private IWebViewUpdateService mWebViewUpdateService;

    @Override // android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!UserManager.get(this).isAdminUser()) {
            finish();
            return;
        }
        this.mWebViewUpdateService = IWebViewUpdateService.Stub.asInterface(ServiceManager.getService("webviewupdate"));
        try {
            WebViewProviderInfo[] providers = this.mWebViewUpdateService.getValidWebViewPackages();
            if (providers == null) {
                Log.e("WebViewImplementation", "No WebView providers available");
                finish();
                return;
            }
            String currentValue = this.mWebViewUpdateService.getCurrentWebViewPackageName();
            if (currentValue == null) {
                currentValue = "";
            }
            int currentIndex = -1;
            ArrayList<String> options = new ArrayList<>();
            final ArrayList<String> values = new ArrayList<>();
            for (WebViewProviderInfo provider : providers) {
                if (Utils.isPackageEnabled(this, provider.packageName)) {
                    options.add(provider.description);
                    values.add(provider.packageName);
                    if (currentValue.contentEquals(provider.packageName)) {
                        currentIndex = values.size() - 1;
                    }
                }
            }
            new AlertDialog.Builder(this).setTitle(R.string.select_webview_provider_dialog_title).setSingleChoiceItems((CharSequence[]) options.toArray(new String[0]), currentIndex, new DialogInterface.OnClickListener() { // from class: com.android.settings.WebViewImplementation.1
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        WebViewImplementation.this.mWebViewUpdateService.changeProviderAndSetting((String) values.get(which));
                    } catch (RemoteException e) {
                        Log.w("WebViewImplementation", "Problem reaching webviewupdate service", e);
                    }
                    WebViewImplementation.this.finish();
                }
            }).setNegativeButton(17039360, (DialogInterface.OnClickListener) null).setOnCancelListener(this).setOnDismissListener(this).show();
        } catch (RemoteException e) {
            Log.w("WebViewImplementation", "Problem reaching webviewupdate service", e);
            finish();
        }
    }

    @Override // com.android.settings.InstrumentedActivity
    protected int getMetricsCategory() {
        return 405;
    }

    @Override // android.content.DialogInterface.OnCancelListener
    public void onCancel(DialogInterface dialog) {
        finish();
    }

    @Override // android.content.DialogInterface.OnDismissListener
    public void onDismiss(DialogInterface dialog) {
        finish();
    }
}
