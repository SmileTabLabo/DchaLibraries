package com.mediatek.browser.ext;

import android.app.Activity;
import android.app.DownloadManager;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;
import java.io.File;
/* loaded from: b.zip:com/mediatek/browser/ext/DefaultBrowserDownloadExt.class */
public class DefaultBrowserDownloadExt implements IBrowserDownloadExt {
    private static final String TAG = "DefaultBrowserDownloadExt";

    @Override // com.mediatek.browser.ext.IBrowserDownloadExt
    public boolean checkStorageBeforeDownload(Activity activity, String str, long j) {
        Log.i("@M_DefaultBrowserDownloadExt", "Enter: checkStorageBeforeDownload --default implement");
        return false;
    }

    @Override // com.mediatek.browser.ext.IBrowserDownloadExt
    public void setRequestDestinationDir(String str, DownloadManager.Request request, String str2, String str3) {
        Log.i("@M_DefaultBrowserDownloadExt", "Enter: setRequestDestinationDir --default implement");
        String str4 = "file://" + str + File.separator + str2;
        request.setDestinationUri(Uri.parse(str4));
        Log.d("@M_DefaultBrowserDownloadExt", "mRequest.setDestinationUri, dir: " + str4);
    }

    @Override // com.mediatek.browser.ext.IBrowserDownloadExt
    public void showToastWithFileSize(Activity activity, long j, String str) {
        Log.i("@M_DefaultBrowserDownloadExt", "Enter: showToastWithFileSize --default implement");
        Toast.makeText(activity, str, 0).show();
    }
}
