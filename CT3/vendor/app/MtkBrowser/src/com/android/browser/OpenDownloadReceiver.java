package com.android.browser;

import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
/* loaded from: b.zip:com/android/browser/OpenDownloadReceiver.class */
public class OpenDownloadReceiver extends BroadcastReceiver {
    private static Handler sAsyncHandler;

    static {
        HandlerThread handlerThread = new HandlerThread("Open browser download async");
        handlerThread.start();
        sAsyncHandler = new Handler(handlerThread.getLooper());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onReceiveAsync(Context context, long j) {
        DownloadManager downloadManager = (DownloadManager) context.getSystemService("download");
        Uri uriForDownloadedFile = downloadManager.getUriForDownloadedFile(j);
        if (uriForDownloadedFile == null) {
            openDownloadsPage(context);
            return;
        }
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setDataAndType(uriForDownloadedFile, downloadManager.getMimeTypeForDownloadedFile(j));
        intent.setFlags(268435456);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            openDownloadsPage(context);
        }
    }

    private void openDownloadsPage(Context context) {
        Intent intent = new Intent("android.intent.action.VIEW_DOWNLOADS");
        intent.setFlags(268435456);
        context.startActivity(intent);
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        if (!"android.intent.action.DOWNLOAD_NOTIFICATION_CLICKED".equals(intent.getAction())) {
            openDownloadsPage(context);
            return;
        }
        long[] longArrayExtra = intent.getLongArrayExtra("extra_click_download_ids");
        if (longArrayExtra == null || longArrayExtra.length == 0) {
            openDownloadsPage(context);
            return;
        }
        sAsyncHandler.post(new Runnable(this, context, longArrayExtra[0], goAsync()) { // from class: com.android.browser.OpenDownloadReceiver.1
            final OpenDownloadReceiver this$0;
            final Context val$context;
            final long val$id;
            final BroadcastReceiver.PendingResult val$result;

            {
                this.this$0 = this;
                this.val$context = context;
                this.val$id = r7;
                this.val$result = r9;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.onReceiveAsync(this.val$context, this.val$id);
                this.val$result.finish();
            }
        });
    }
}
