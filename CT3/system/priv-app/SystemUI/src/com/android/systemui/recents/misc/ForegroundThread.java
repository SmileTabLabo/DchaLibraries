package com.android.systemui.recents.misc;

import android.os.Handler;
import android.os.HandlerThread;
/* loaded from: a.zip:com/android/systemui/recents/misc/ForegroundThread.class */
public final class ForegroundThread extends HandlerThread {
    private static Handler sHandler;
    private static ForegroundThread sInstance;

    private ForegroundThread() {
        super("recents.fg");
    }

    private static void ensureThreadLocked() {
        if (sInstance == null) {
            sInstance = new ForegroundThread();
            sInstance.start();
            sHandler = new Handler(sInstance.getLooper());
        }
    }

    public static ForegroundThread get() {
        ForegroundThread foregroundThread;
        synchronized (ForegroundThread.class) {
            try {
                ensureThreadLocked();
                foregroundThread = sInstance;
            } catch (Throwable th) {
                throw th;
            }
        }
        return foregroundThread;
    }

    public static Handler getHandler() {
        Handler handler;
        synchronized (ForegroundThread.class) {
            try {
                ensureThreadLocked();
                handler = sHandler;
            } catch (Throwable th) {
                throw th;
            }
        }
        return handler;
    }
}
