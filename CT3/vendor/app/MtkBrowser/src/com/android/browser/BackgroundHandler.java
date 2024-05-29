package com.android.browser;

import android.os.HandlerThread;
import android.os.Looper;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/* loaded from: b.zip:com/android/browser/BackgroundHandler.class */
public class BackgroundHandler {
    static ExecutorService mThreadPool;
    static HandlerThread sLooperThread = new HandlerThread("BackgroundHandler", 1);

    static {
        sLooperThread.start();
        mThreadPool = Executors.newCachedThreadPool();
    }

    private BackgroundHandler() {
    }

    public static void execute(Runnable runnable) {
        mThreadPool.execute(runnable);
    }

    public static Looper getLooper() {
        return sLooperThread.getLooper();
    }
}
