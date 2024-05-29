package com.android.browser;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.net.WebAddress;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.URLUtil;
import android.widget.Toast;
import com.mediatek.browser.ext.IBrowserDownloadExt;
import java.io.File;
import java.net.URI;
/* loaded from: classes.dex */
public class DownloadHandler {
    private static final boolean DEBUG = Browser.DEBUG;
    private static IBrowserDownloadExt sBrowserDownloadExt = null;

    public static void onDownloadStart(Activity activity, String str, String str2, String str3, String str4, String str5, boolean z, long j) {
        onDownloadStartNoStream(activity, str, str2, str3, str4, str5, z, j);
    }

    private static String encodePath(String str) {
        boolean z;
        char[] charArray = str.toCharArray();
        for (char c : charArray) {
            if (c == '[' || c == ']' || c == '|') {
                z = true;
                break;
            }
        }
        z = false;
        if (!z) {
            return str;
        }
        StringBuilder sb = new StringBuilder("");
        for (char c2 : charArray) {
            if (c2 == '[' || c2 == ']' || c2 == '|') {
                sb.append('%');
                sb.append(Integer.toHexString(c2));
            } else {
                sb.append(c2);
            }
        }
        return sb.toString();
    }

    /* JADX WARN: Type inference failed for: r1v6, types: [com.android.browser.DownloadHandler$1] */
    public static void onDownloadStartNoStream(Activity activity, String str, String str2, String str3, String str4, String str5, boolean z, long j) {
        int indexOf;
        String string;
        String str6 = str4;
        String[] strArr = {"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"};
        for (int i = 0; i < strArr.length; i++) {
            if (activity.checkSelfPermission(strArr[i]) != 0) {
                if (DEBUG) {
                    Log.d("browser/DLHandler", "no permission: " + strArr[i]);
                    return;
                } else {
                    return;
                }
            }
        }
        if (str6 != null && str6.startsWith("\"") && str6.endsWith("\"") && str4.length() > 2) {
            str6 = str6.substring(1, str4.length() - 1);
        }
        String guessFileName = URLUtil.guessFileName(str, str3, str6);
        if (DEBUG) {
            Log.d("browser/DLHandler", "Guess file name is: " + guessFileName + " mimetype is: " + str6);
        }
        String externalStorageState = Environment.getExternalStorageState();
        boolean equals = externalStorageState.equals("mounted");
        int i2 = R.string.download_sdcard_busy_dlg_title;
        if (!equals) {
            if (externalStorageState.equals("shared")) {
                string = activity.getString(R.string.download_sdcard_busy_dlg_msg);
            } else {
                string = activity.getString(R.string.download_no_sdcard_dlg_msg, new Object[]{guessFileName});
                i2 = R.string.download_no_sdcard_dlg_title;
            }
            new AlertDialog.Builder(activity).setTitle(i2).setIconAttribute(16843605).setMessage(string).setPositiveButton(R.string.ok, (DialogInterface.OnClickListener) null).show();
            return;
        }
        String downloadPath = BrowserSettings.getInstance().getDownloadPath();
        if (downloadPath.startsWith("/storage/") && (indexOf = downloadPath.indexOf("/", "/storage/".length())) > 0) {
            String substring = downloadPath.substring(0, indexOf);
            if (DEBUG) {
                Log.d("browser/DLHandler", "rootPath = " + substring);
            }
            StorageVolume storageVolume = ((StorageManager) activity.getSystemService("storage")).getStorageVolume(new File(substring));
            if (storageVolume == null) {
                if (DEBUG) {
                    Log.d("browser/DLHandler", "volume is null: " + downloadPath);
                }
            } else if (storageVolume.isRemovable() && !new File(substring).canWrite()) {
                if (DEBUG) {
                    Log.d("browser/DLHandler", "  DownloadPath " + downloadPath + " can't write!");
                }
                new AlertDialog.Builder(activity).setTitle(R.string.download_path_unavailable_dlg_title).setIcon(17301543).setMessage(activity.getString(R.string.download_path_unavailable_dlg_msg)).setPositiveButton(R.string.ok, (DialogInterface.OnClickListener) null).show();
                return;
            }
        }
        sBrowserDownloadExt = Extensions.getDownloadPlugin(activity);
        if (sBrowserDownloadExt.checkStorageBeforeDownload(activity, downloadPath, j)) {
            return;
        }
        try {
            WebAddress webAddress = new WebAddress(str);
            webAddress.setPath(encodePath(webAddress.getPath()));
            String webAddress2 = webAddress.toString();
            try {
                final DownloadManager.Request request = new DownloadManager.Request(Uri.parse(webAddress2));
                request.setMimeType(str6);
                try {
                    sBrowserDownloadExt.setRequestDestinationDir(BrowserSettings.getInstance().getDownloadPath(), request, guessFileName, str6);
                    request.allowScanningByMediaScanner();
                    request.setDescription(webAddress.getHost());
                    String cookie = CookieManager.getInstance().getCookie(str, z);
                    request.addRequestHeader("cookie", cookie);
                    request.addRequestHeader("User-Agent", str2);
                    request.addRequestHeader("Referer", str5);
                    request.setNotificationVisibility(1);
                    if (str6 == null) {
                        if (TextUtils.isEmpty(webAddress2)) {
                            return;
                        }
                        try {
                            URI.create(webAddress2);
                            new FetchUrlMimeType(activity, request, webAddress2, cookie, str2).start();
                        } catch (IllegalArgumentException e) {
                            Toast.makeText(activity, (int) R.string.cannot_download, 0).show();
                            return;
                        }
                    } else {
                        final DownloadManager downloadManager = (DownloadManager) activity.getSystemService("download");
                        new Thread("Browser download") { // from class: com.android.browser.DownloadHandler.1
                            @Override // java.lang.Thread, java.lang.Runnable
                            public void run() {
                                downloadManager.enqueue(request);
                            }
                        }.start();
                    }
                    sBrowserDownloadExt.showToastWithFileSize(activity, j, activity.getResources().getString(R.string.download_pending));
                    Intent intent = new Intent("android.intent.action.VIEW_DOWNLOADS");
                    intent.setFlags(268468224);
                    activity.startActivity(intent);
                } catch (IllegalStateException e2) {
                    Log.w("DLHandler", "Exception trying to create Download dir:", e2);
                    Toast.makeText(activity, (int) R.string.download_sdcard_busy_dlg_title, 0).show();
                }
            } catch (IllegalArgumentException e3) {
                Toast.makeText(activity, (int) R.string.cannot_download, 0).show();
            }
        } catch (Exception e4) {
            if (DEBUG) {
                Log.e("DLHandler", "Exception trying to parse url:" + str);
            }
        }
    }
}
