package com.android.systemui.screenshot;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.UserManager;
import android.util.Log;
/* loaded from: a.zip:com/android/systemui/screenshot/TakeScreenshotService.class */
public class TakeScreenshotService extends Service {
    private static GlobalScreenshot mScreenshot;
    private Handler mHandler = new Handler(this) { // from class: com.android.systemui.screenshot.TakeScreenshotService.1
        final TakeScreenshotService this$0;

        {
            this.this$0 = this;
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            boolean z = true;
            Runnable runnable = new Runnable(this, message.replyTo) { // from class: com.android.systemui.screenshot.TakeScreenshotService.1.1
                final AnonymousClass1 this$1;
                final Messenger val$callback;

                {
                    this.this$1 = this;
                    this.val$callback = r5;
                }

                @Override // java.lang.Runnable
                public void run() {
                    try {
                        this.val$callback.send(Message.obtain((Handler) null, 1));
                    } catch (RemoteException e) {
                    }
                }
            };
            if (!((UserManager) this.this$0.getSystemService(UserManager.class)).isUserUnlocked()) {
                Log.w("TakeScreenshotService", "Skipping screenshot because storage is locked!");
                post(runnable);
                return;
            }
            if (TakeScreenshotService.mScreenshot == null) {
                GlobalScreenshot unused = TakeScreenshotService.mScreenshot = new GlobalScreenshot(this.this$0);
            }
            switch (message.what) {
                case 1:
                    GlobalScreenshot globalScreenshot = TakeScreenshotService.mScreenshot;
                    boolean z2 = message.arg1 > 0;
                    if (message.arg2 <= 0) {
                        z = false;
                    }
                    globalScreenshot.takeScreenshot(runnable, z2, z);
                    return;
                case 2:
                    TakeScreenshotService.mScreenshot.takeScreenshotPartial(runnable, message.arg1 > 0, message.arg2 > 0);
                    return;
                default:
                    return;
            }
        }
    };

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return new Messenger(this.mHandler).getBinder();
    }

    @Override // android.app.Service
    public boolean onUnbind(Intent intent) {
        if (mScreenshot != null) {
            mScreenshot.stopScreenshot();
            return true;
        }
        return true;
    }
}
