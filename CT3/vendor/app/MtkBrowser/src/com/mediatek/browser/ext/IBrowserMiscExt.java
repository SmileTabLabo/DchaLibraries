package com.mediatek.browser.ext;

import android.app.Activity;
import android.content.Intent;
import android.webkit.WebView;
/* loaded from: b.zip:com/mediatek/browser/ext/IBrowserMiscExt.class */
public interface IBrowserMiscExt {
    void onActivityResult(int i, int i2, Intent intent, Object obj);

    void processNetworkNotify(WebView webView, Activity activity, boolean z);
}
