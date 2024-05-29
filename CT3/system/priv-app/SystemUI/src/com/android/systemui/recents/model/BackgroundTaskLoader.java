package com.android.systemui.recents.model;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.RecentsConfiguration;
import com.android.systemui.recents.misc.SystemServicesProxy;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: a.zip:com/android/systemui/recents/model/BackgroundTaskLoader.class */
public class BackgroundTaskLoader implements Runnable {
    boolean mCancelled;
    Context mContext;
    BitmapDrawable mDefaultIcon;
    Bitmap mDefaultThumbnail;
    TaskKeyLruCache<Drawable> mIconCache;
    TaskResourceLoadQueue mLoadQueue;
    Handler mLoadThreadHandler;
    TaskKeyLruCache<ThumbnailData> mThumbnailCache;
    boolean mWaitingOnLoadQueue;
    static String TAG = "TaskResourceLoader";
    static boolean DEBUG = false;
    Handler mMainThreadHandler = new Handler();
    HandlerThread mLoadThread = new HandlerThread("Recents-TaskResourceLoader", 10);

    public BackgroundTaskLoader(TaskResourceLoadQueue taskResourceLoadQueue, TaskKeyLruCache<Drawable> taskKeyLruCache, TaskKeyLruCache<ThumbnailData> taskKeyLruCache2, Bitmap bitmap, BitmapDrawable bitmapDrawable) {
        this.mLoadQueue = taskResourceLoadQueue;
        this.mIconCache = taskKeyLruCache;
        this.mThumbnailCache = taskKeyLruCache2;
        this.mDefaultThumbnail = bitmap;
        this.mDefaultIcon = bitmapDrawable;
        this.mLoadThread.start();
        this.mLoadThreadHandler = new Handler(this.mLoadThread.getLooper());
        this.mLoadThreadHandler.post(this);
    }

    @Override // java.lang.Runnable
    public void run() {
        Task nextTask;
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
                RecentsConfiguration configuration = Recents.getConfiguration();
                SystemServicesProxy systemServices = Recents.getSystemServices();
                if (systemServices != null && (nextTask = this.mLoadQueue.nextTask()) != null) {
                    Drawable drawable = this.mIconCache.get(nextTask.key);
                    ThumbnailData thumbnailData = this.mThumbnailCache.get(nextTask.key);
                    BitmapDrawable bitmapDrawable = drawable;
                    if (drawable == null) {
                        Drawable badgedTaskDescriptionIcon = systemServices.getBadgedTaskDescriptionIcon(nextTask.taskDescription, nextTask.key.userId, this.mContext.getResources());
                        Drawable drawable2 = badgedTaskDescriptionIcon;
                        if (badgedTaskDescriptionIcon == null) {
                            ActivityInfo activityInfo = systemServices.getActivityInfo(nextTask.key.getComponent(), nextTask.key.userId);
                            drawable2 = badgedTaskDescriptionIcon;
                            if (activityInfo != null) {
                                if (DEBUG) {
                                    Log.d(TAG, "Loading icon: " + nextTask.key);
                                }
                                drawable2 = systemServices.getBadgedActivityIcon(activityInfo, nextTask.key.userId);
                            }
                        }
                        bitmapDrawable = drawable2;
                        if (drawable2 == null) {
                            bitmapDrawable = this.mDefaultIcon;
                        }
                        this.mIconCache.put(nextTask.key, bitmapDrawable);
                    }
                    ThumbnailData thumbnailData2 = thumbnailData;
                    if (thumbnailData == null) {
                        ThumbnailData thumbnailData3 = thumbnailData;
                        if (configuration.svelteLevel < 3) {
                            if (DEBUG) {
                                Log.d(TAG, "Loading thumbnail: " + nextTask.key);
                            }
                            thumbnailData3 = systemServices.getTaskThumbnail(nextTask.key.id);
                        }
                        if (thumbnailData3.thumbnail == null) {
                            thumbnailData3.thumbnail = this.mDefaultThumbnail;
                        }
                        thumbnailData2 = thumbnailData3;
                        if (configuration.svelteLevel < 1) {
                            this.mThumbnailCache.put(nextTask.key, thumbnailData3);
                            thumbnailData2 = thumbnailData3;
                        }
                    }
                    if (!this.mCancelled) {
                        this.mMainThreadHandler.post(new Runnable(this, nextTask, thumbnailData2, bitmapDrawable) { // from class: com.android.systemui.recents.model.BackgroundTaskLoader.1
                            final BackgroundTaskLoader this$0;
                            final Drawable val$newIcon;
                            final ThumbnailData val$newThumbnailData;
                            final Task val$t;

                            {
                                this.this$0 = this;
                                this.val$t = nextTask;
                                this.val$newThumbnailData = thumbnailData2;
                                this.val$newIcon = bitmapDrawable;
                            }

                            @Override // java.lang.Runnable
                            public void run() {
                                this.val$t.notifyTaskDataLoaded(this.val$newThumbnailData.thumbnail, this.val$newIcon, this.val$newThumbnailData.thumbnailInfo);
                            }
                        });
                    }
                }
                if (!this.mCancelled && this.mLoadQueue.isEmpty()) {
                    synchronized (this.mLoadQueue) {
                        try {
                            this.mWaitingOnLoadQueue = true;
                            this.mLoadQueue.wait();
                            this.mWaitingOnLoadQueue = false;
                        } catch (InterruptedException e2) {
                            e2.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void start(Context context) {
        this.mContext = context;
        this.mCancelled = false;
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
}
