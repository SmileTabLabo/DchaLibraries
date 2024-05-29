package com.android.browser;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.net.WebAddress;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.URLUtil;
import android.widget.Toast;
import com.mediatek.browser.ext.IBrowserDownloadExt;
import com.mediatek.storage.StorageManagerEx;
import java.io.File;
import java.net.URI;
/* loaded from: b.zip:com/android/browser/DownloadHandler.class */
public class DownloadHandler {
    private static IBrowserDownloadExt sBrowserDownloadExt = null;

    private static String encodePath(String str) {
        boolean z;
        char[] charArray = str.toCharArray();
        int length = charArray.length;
        int i = 0;
        while (true) {
            z = false;
            if (i >= length) {
                break;
            }
            char c = charArray[i];
            if (c == '[' || c == ']' || c == '|') {
                break;
            }
            i++;
        }
        z = true;
        if (z) {
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
        return str;
    }

    public static void onDownloadStart(Activity activity, String str, String str2, String str3, String str4, String str5, boolean z, long j) {
        onDownloadStartNoStream(activity, str, str2, str3, str4, str5, z, j);
    }

    /* JADX WARN: Type inference failed for: r0v52, types: [com.android.browser.DownloadHandler$1] */
    public static void onDownloadStartNoStream(Activity activity, String str, String str2, String str3, String str4, String str5, boolean z, long j) {
        int indexOf;
        String string;
        int i;
        String str6 = str4;
        if (str4 != null) {
            str6 = str4;
            if (str4.startsWith("\"")) {
                str6 = str4;
                if (str4.endsWith("\"")) {
                    str6 = str4;
                    if (str4.length() > 2) {
                        str6 = str4.substring(1, str4.length() - 1);
                    }
                }
            }
        }
        String guessFileName = URLUtil.guessFileName(str, str3, str6);
        Log.d("browser/DLHandler", "Guess file name is: " + guessFileName + " mimetype is: " + str6);
        String externalStorageState = Environment.getExternalStorageState();
        if (!externalStorageState.equals("mounted")) {
            if (externalStorageState.equals("shared")) {
                string = activity.getString(2131493220);
                i = 2131493219;
            } else {
                string = activity.getString(2131493218, new Object[]{guessFileName});
                i = 2131493217;
            }
            new AlertDialog.Builder(activity).setTitle(i).setIconAttribute(16843605).setMessage(string).setPositiveButton(2131492963, (DialogInterface.OnClickListener) null).show();
            return;
        }
        String downloadPath = BrowserSettings.getInstance().getDownloadPath();
        if (downloadPath.startsWith("/storage/") && (indexOf = downloadPath.indexOf("/", "/storage/".length())) > 0) {
            String substring = downloadPath.substring(0, indexOf);
            Log.d("browser/DLHandler", "rootPath = " + substring);
            if (StorageManagerEx.isExternalSDCard(substring) && !new File(substring).canWrite()) {
                Log.d("browser/DLHandler", "  DownloadPath " + downloadPath + " can't write!");
                new AlertDialog.Builder(activity).setTitle(2131492868).setIcon(17301543).setMessage(activity.getString(2131492869)).setPositiveButton(2131492963, (DialogInterface.OnClickListener) null).show();
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
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(webAddress2));
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
                    request.setUserAgent(str2);
                    if (str6 != null) {
                        new Thread("Browser download", (DownloadManager) activity.getSystemService("download"), request) { // from class: com.android.browser.DownloadHandler.1
                            final DownloadManager val$manager;
                            final DownloadManager.Request val$request;

                            {
                                this.val$manager = r5;
                                this.val$request = request;
                            }

                            @Override // java.lang.Thread, java.lang.Runnable
                            public void run() {
                                this.val$manager.enqueue(this.val$request);
                            }
                        }.start();
                    } else if (TextUtils.isEmpty(webAddress2)) {
                        return;
                    } else {
                        try {
                            URI.create(webAddress2);
                            new FetchUrlMimeType(activity, request, webAddress2, cookie, str2).start();
                        } catch (IllegalArgumentException e) {
                            Toast.makeText(activity, 2131493221, 0).show();
                            return;
                        }
                    }
                    sBrowserDownloadExt.showToastWithFileSize(activity, j, activity.getResources().getString(2131493222));
                    Intent intent = new Intent("android.intent.action.VIEW_DOWNLOADS");
                    intent.setFlags(268468224);
                    activity.startActivity(intent);
                } catch (IllegalStateException e2) {
                    Log.w("DLHandler", "Exception trying to create Download dir:", e2);
                    Toast.makeText(activity, 2131493219, 0).show();
                }
            } catch (IllegalArgumentException e3) {
                Toast.makeText(activity, 2131493221, 0).show();
            }
        } catch (Exception e4) {
            Log.e("DLHandler", "Exception trying to parse url:" + str);
        }
    }
}
