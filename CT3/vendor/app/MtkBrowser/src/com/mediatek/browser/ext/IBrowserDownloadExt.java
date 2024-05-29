package com.mediatek.browser.ext;

import android.app.Activity;
import android.app.DownloadManager;
/* loaded from: b.zip:com/mediatek/browser/ext/IBrowserDownloadExt.class */
public interface IBrowserDownloadExt {
    boolean checkStorageBeforeDownload(Activity activity, String str, long j);

    void setRequestDestinationDir(String str, DownloadManager.Request request, String str2, String str3);

    void showToastWithFileSize(Activity activity, long j, String str);
}
