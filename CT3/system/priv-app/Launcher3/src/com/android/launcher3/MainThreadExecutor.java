package com.android.launcher3;

import android.os.Handler;
import android.os.Looper;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;
/* loaded from: a.zip:com/android/launcher3/MainThreadExecutor.class */
public class MainThreadExecutor extends AbstractExecutorService {
    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Override // java.util.concurrent.ExecutorService
    @Deprecated
    public boolean awaitTermination(long j, TimeUnit timeUnit) throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    @Override // java.util.concurrent.Executor
    public void execute(Runnable runnable) {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            runnable.run();
        } else {
            this.mHandler.post(runnable);
        }
    }

    @Override // java.util.concurrent.ExecutorService
    public boolean isShutdown() {
        return false;
    }

    @Override // java.util.concurrent.ExecutorService
    public boolean isTerminated() {
        return false;
    }

    @Override // java.util.concurrent.ExecutorService
    @Deprecated
    public void shutdown() {
        throw new UnsupportedOperationException();
    }

    @Override // java.util.concurrent.ExecutorService
    @Deprecated
    public List<Runnable> shutdownNow() {
        throw new UnsupportedOperationException();
    }
}
