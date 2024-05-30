package com.android.systemui.shared.recents.model;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import com.android.systemui.shared.system.ActivityManagerWrapper;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class BackgroundTaskLoader implements Runnable {
    private boolean mCancelled;
    private Context mContext;
    private final IconLoader mIconLoader;
    private final TaskResourceLoadQueue mLoadQueue;
    private final Handler mLoadThreadHandler;
    private final OnIdleChangedListener mOnIdleChangedListener;
    private boolean mStarted;
    private boolean mWaitingOnLoadQueue;
    static String TAG = "BackgroundTaskLoader";
    static boolean DEBUG = false;
    private final Handler mMainThreadHandler = new Handler();
    private final HandlerThread mLoadThread = new HandlerThread("Recents-TaskResourceLoader", 10);

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public interface OnIdleChangedListener {
        void onIdleChanged(boolean z);
    }

    public BackgroundTaskLoader(TaskResourceLoadQueue taskResourceLoadQueue, IconLoader iconLoader, OnIdleChangedListener onIdleChangedListener) {
        this.mLoadQueue = taskResourceLoadQueue;
        this.mIconLoader = iconLoader;
        this.mOnIdleChangedListener = onIdleChangedListener;
        this.mLoadThread.start();
        this.mLoadThreadHandler = new Handler(this.mLoadThread.getLooper());
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void start(Context context) {
        this.mContext = context;
        this.mCancelled = false;
        if (!this.mStarted) {
            this.mStarted = true;
            this.mLoadThreadHandler.post(this);
            return;
        }
        synchronized (this.mLoadThread) {
            this.mLoadThread.notifyAll();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void stop() {
        this.mCancelled = true;
        if (this.mWaitingOnLoadQueue) {
            this.mContext = null;
        }
    }

    @Override // java.lang.Runnable
    public void run() {
        while (true) {
            if (this.mCancelled) {
                this.mContext = null;
                synchronized (this.mLoadThread) {
                    try {
                        this.mLoadThread.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                processLoadQueueItem();
                if (!this.mCancelled && this.mLoadQueue.isEmpty()) {
                    synchronized (this.mLoadQueue) {
                        try {
                            this.mWaitingOnLoadQueue = true;
                            this.mMainThreadHandler.post(new Runnable() { // from class: com.android.systemui.shared.recents.model.-$$Lambda$BackgroundTaskLoader$gaMb8n3irXHj3SpODGi50cngupE
                                @Override // java.lang.Runnable
                                public final void run() {
                                    BackgroundTaskLoader.this.mOnIdleChangedListener.onIdleChanged(true);
                                }
                            });
                            this.mLoadQueue.wait();
                            this.mMainThreadHandler.post(new Runnable() { // from class: com.android.systemui.shared.recents.model.-$$Lambda$BackgroundTaskLoader$XRsMGIp0x8MAJ36UKSTd3DJ9dTg
                                @Override // java.lang.Runnable
                                public final void run() {
                                    BackgroundTaskLoader.this.mOnIdleChangedListener.onIdleChanged(false);
                                }
                            });
                            this.mWaitingOnLoadQueue = false;
                        } catch (InterruptedException e2) {
                            e2.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private void processLoadQueueItem() {
        final Task nextTask = this.mLoadQueue.nextTask();
        if (nextTask != null) {
            final Drawable icon = this.mIconLoader.getIcon(nextTask);
            if (DEBUG) {
                String str = TAG;
                Log.d(str, "Loading thumbnail: " + nextTask.key);
            }
            final ThumbnailData taskThumbnail = ActivityManagerWrapper.getInstance().getTaskThumbnail(nextTask.key.id, true);
            if (!this.mCancelled) {
                this.mMainThreadHandler.post(new Runnable() { // from class: com.android.systemui.shared.recents.model.-$$Lambda$BackgroundTaskLoader$mJeiv3P4w5EJwXqKPoDi48s7tFI
                    @Override // java.lang.Runnable
                    public final void run() {
                        Task.this.notifyTaskDataLoaded(taskThumbnail, icon);
                    }
                });
            }
        }
    }
}
