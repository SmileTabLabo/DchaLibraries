package com.android.systemui.recents.model;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.LruCache;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.RecentsConfiguration;
import com.android.systemui.recents.events.activity.PackagesChangedEvent;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.model.RecentsTaskLoadPlan;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.model.TaskKeyLruCache;
import java.io.PrintWriter;
/* loaded from: a.zip:com/android/systemui/recents/model/RecentsTaskLoader.class */
public class RecentsTaskLoader {
    private final LruCache<ComponentName, ActivityInfo> mActivityInfoCache;
    private final TaskKeyLruCache<String> mActivityLabelCache;
    private TaskKeyLruCache.EvictionCallback mClearActivityInfoOnEviction = new TaskKeyLruCache.EvictionCallback(this) { // from class: com.android.systemui.recents.model.RecentsTaskLoader.1
        final RecentsTaskLoader this$0;

        {
            this.this$0 = this;
        }

        @Override // com.android.systemui.recents.model.TaskKeyLruCache.EvictionCallback
        public void onEntryEvicted(Task.TaskKey taskKey) {
            if (taskKey != null) {
                this.this$0.mActivityInfoCache.remove(taskKey.getComponent());
            }
        }
    };
    private final TaskKeyLruCache<String> mContentDescriptionCache;
    BitmapDrawable mDefaultIcon;
    int mDefaultTaskBarBackgroundColor;
    int mDefaultTaskViewBackgroundColor;
    Bitmap mDefaultThumbnail;
    private final TaskKeyLruCache<Drawable> mIconCache;
    private final TaskResourceLoadQueue mLoadQueue;
    private final BackgroundTaskLoader mLoader;
    private final int mMaxIconCacheSize;
    private final int mMaxThumbnailCacheSize;
    private int mNumVisibleTasksLoaded;
    private int mNumVisibleThumbnailsLoaded;
    private final TaskKeyLruCache<ThumbnailData> mThumbnailCache;

    public RecentsTaskLoader(Context context) {
        Resources resources = context.getResources();
        this.mDefaultTaskBarBackgroundColor = context.getColor(2131558538);
        this.mDefaultTaskViewBackgroundColor = context.getColor(2131558539);
        this.mMaxThumbnailCacheSize = resources.getInteger(2131755044);
        this.mMaxIconCacheSize = resources.getInteger(2131755045);
        int i = this.mMaxIconCacheSize;
        int i2 = this.mMaxThumbnailCacheSize;
        Bitmap createBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ALPHA_8);
        createBitmap.eraseColor(0);
        this.mDefaultThumbnail = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        this.mDefaultThumbnail.setHasAlpha(false);
        this.mDefaultThumbnail.eraseColor(-1);
        this.mDefaultIcon = new BitmapDrawable(context.getResources(), createBitmap);
        int maxRecentTasksStatic = ActivityManager.getMaxRecentTasksStatic();
        this.mLoadQueue = new TaskResourceLoadQueue();
        this.mIconCache = new TaskKeyLruCache<>(i, this.mClearActivityInfoOnEviction);
        this.mThumbnailCache = new TaskKeyLruCache<>(i2);
        this.mActivityLabelCache = new TaskKeyLruCache<>(maxRecentTasksStatic, this.mClearActivityInfoOnEviction);
        this.mContentDescriptionCache = new TaskKeyLruCache<>(maxRecentTasksStatic, this.mClearActivityInfoOnEviction);
        this.mActivityInfoCache = new LruCache<>(maxRecentTasksStatic);
        this.mLoader = new BackgroundTaskLoader(this.mLoadQueue, this.mIconCache, this.mThumbnailCache, this.mDefaultThumbnail, this.mDefaultIcon);
    }

    private void stopLoader() {
        this.mLoader.stop();
        this.mLoadQueue.clearTasks();
    }

    public RecentsTaskLoadPlan createLoadPlan(Context context) {
        return new RecentsTaskLoadPlan(context);
    }

    public void deleteTaskData(Task task, boolean z) {
        this.mLoadQueue.removeTask(task);
        this.mThumbnailCache.remove(task.key);
        this.mIconCache.remove(task.key);
        this.mActivityLabelCache.remove(task.key);
        this.mContentDescriptionCache.remove(task.key);
        if (z) {
            task.notifyTaskDataUnloaded(null, this.mDefaultIcon);
        }
    }

    public void dump(String str, PrintWriter printWriter) {
        String str2 = str + "  ";
        printWriter.print(str);
        printWriter.println("RecentsTaskLoader");
        printWriter.print(str);
        printWriter.println("Icon Cache");
        this.mIconCache.dump(str2, printWriter);
        printWriter.print(str);
        printWriter.println("Thumbnail Cache");
        this.mThumbnailCache.dump(str2, printWriter);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int getActivityBackgroundColor(ActivityManager.TaskDescription taskDescription) {
        return (taskDescription == null || taskDescription.getBackgroundColor() == 0) ? this.mDefaultTaskViewBackgroundColor : taskDescription.getBackgroundColor();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int getActivityPrimaryColor(ActivityManager.TaskDescription taskDescription) {
        return (taskDescription == null || taskDescription.getPrimaryColor() == 0) ? this.mDefaultTaskBarBackgroundColor : taskDescription.getPrimaryColor();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public Drawable getAndUpdateActivityIcon(Task.TaskKey taskKey, ActivityManager.TaskDescription taskDescription, Resources resources, boolean z) {
        Drawable badgedActivityIcon;
        SystemServicesProxy systemServices = Recents.getSystemServices();
        Drawable andInvalidateIfModified = this.mIconCache.getAndInvalidateIfModified(taskKey);
        if (andInvalidateIfModified != null) {
            return andInvalidateIfModified;
        }
        if (z) {
            Drawable badgedTaskDescriptionIcon = systemServices.getBadgedTaskDescriptionIcon(taskDescription, taskKey.userId, resources);
            if (badgedTaskDescriptionIcon != null) {
                this.mIconCache.put(taskKey, badgedTaskDescriptionIcon);
                return badgedTaskDescriptionIcon;
            }
            ActivityInfo andUpdateActivityInfo = getAndUpdateActivityInfo(taskKey);
            if (andUpdateActivityInfo == null || (badgedActivityIcon = systemServices.getBadgedActivityIcon(andUpdateActivityInfo, taskKey.userId)) == null) {
                return null;
            }
            this.mIconCache.put(taskKey, badgedActivityIcon);
            return badgedActivityIcon;
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public ActivityInfo getAndUpdateActivityInfo(Task.TaskKey taskKey) {
        SystemServicesProxy systemServices = Recents.getSystemServices();
        ComponentName component = taskKey.getComponent();
        ActivityInfo activityInfo = this.mActivityInfoCache.get(component);
        ActivityInfo activityInfo2 = activityInfo;
        if (activityInfo == null) {
            activityInfo2 = systemServices.getActivityInfo(component, taskKey.userId);
            if (component == null || activityInfo2 == null) {
                Log.e("RecentsTaskLoader", "Unexpected null component name or activity info: " + component + ", " + activityInfo2);
                return null;
            }
            this.mActivityInfoCache.put(component, activityInfo2);
        }
        return activityInfo2;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public String getAndUpdateActivityTitle(Task.TaskKey taskKey, ActivityManager.TaskDescription taskDescription) {
        SystemServicesProxy systemServices = Recents.getSystemServices();
        if (taskDescription == null || taskDescription.getLabel() == null) {
            String andInvalidateIfModified = this.mActivityLabelCache.getAndInvalidateIfModified(taskKey);
            if (andInvalidateIfModified != null) {
                return andInvalidateIfModified;
            }
            ActivityInfo andUpdateActivityInfo = getAndUpdateActivityInfo(taskKey);
            if (andUpdateActivityInfo != null) {
                String badgedActivityLabel = systemServices.getBadgedActivityLabel(andUpdateActivityInfo, taskKey.userId);
                this.mActivityLabelCache.put(taskKey, badgedActivityLabel);
                return badgedActivityLabel;
            }
            return "";
        }
        return taskDescription.getLabel();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public String getAndUpdateContentDescription(Task.TaskKey taskKey, Resources resources) {
        SystemServicesProxy systemServices = Recents.getSystemServices();
        String andInvalidateIfModified = this.mContentDescriptionCache.getAndInvalidateIfModified(taskKey);
        if (andInvalidateIfModified != null) {
            return andInvalidateIfModified;
        }
        ActivityInfo andUpdateActivityInfo = getAndUpdateActivityInfo(taskKey);
        if (andUpdateActivityInfo != null) {
            String badgedContentDescription = systemServices.getBadgedContentDescription(andUpdateActivityInfo, taskKey.userId, resources);
            this.mContentDescriptionCache.put(taskKey, badgedContentDescription);
            return badgedContentDescription;
        }
        return "";
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public Bitmap getAndUpdateThumbnail(Task.TaskKey taskKey, boolean z) {
        SystemServicesProxy systemServices = Recents.getSystemServices();
        ThumbnailData andInvalidateIfModified = this.mThumbnailCache.getAndInvalidateIfModified(taskKey);
        if (andInvalidateIfModified != null) {
            return andInvalidateIfModified.thumbnail;
        }
        if (!z || Recents.getConfiguration().svelteLevel >= 3) {
            return null;
        }
        ThumbnailData taskThumbnail = systemServices.getTaskThumbnail(taskKey.id);
        if (taskThumbnail.thumbnail != null) {
            this.mThumbnailCache.put(taskKey, taskThumbnail);
            return taskThumbnail.thumbnail;
        }
        return null;
    }

    public int getIconCacheSize() {
        return this.mMaxIconCacheSize;
    }

    public int getThumbnailCacheSize() {
        return this.mMaxThumbnailCacheSize;
    }

    public void loadTaskData(Task task) {
        BitmapDrawable andInvalidateIfModified = this.mIconCache.getAndInvalidateIfModified(task.key);
        Bitmap bitmap = null;
        ActivityManager.TaskThumbnailInfo taskThumbnailInfo = null;
        ThumbnailData andInvalidateIfModified2 = this.mThumbnailCache.getAndInvalidateIfModified(task.key);
        if (andInvalidateIfModified2 != null) {
            bitmap = andInvalidateIfModified2.thumbnail;
            taskThumbnailInfo = andInvalidateIfModified2.thumbnailInfo;
        }
        boolean z = andInvalidateIfModified == null || bitmap == null;
        if (andInvalidateIfModified == null) {
            andInvalidateIfModified = this.mDefaultIcon;
        }
        if (z) {
            this.mLoadQueue.addTask(task);
        }
        Bitmap bitmap2 = bitmap;
        if (bitmap == this.mDefaultThumbnail) {
            bitmap2 = null;
        }
        task.notifyTaskDataLoaded(bitmap2, andInvalidateIfModified, taskThumbnailInfo);
    }

    public void loadTasks(Context context, RecentsTaskLoadPlan recentsTaskLoadPlan, RecentsTaskLoadPlan.Options options) {
        if (options == null) {
            throw new RuntimeException("Requires load options");
        }
        recentsTaskLoadPlan.executePlan(options, this, this.mLoadQueue);
        if (options.onlyLoadForCache) {
            return;
        }
        this.mNumVisibleTasksLoaded = options.numVisibleTasks;
        this.mNumVisibleThumbnailsLoaded = options.numVisibleTaskThumbnails;
        this.mLoader.start(context);
    }

    public final void onBusEvent(PackagesChangedEvent packagesChangedEvent) {
        for (ComponentName componentName : this.mActivityInfoCache.snapshot().keySet()) {
            if (componentName.getPackageName().equals(packagesChangedEvent.packageName)) {
                this.mActivityInfoCache.remove(componentName);
            }
        }
    }

    public void onTrimMemory(int i) {
        RecentsConfiguration configuration = Recents.getConfiguration();
        switch (i) {
            case 5:
            case 40:
                this.mThumbnailCache.trimToSize(Math.max(1, this.mMaxThumbnailCacheSize / 2));
                this.mIconCache.trimToSize(Math.max(1, this.mMaxIconCacheSize / 2));
                this.mActivityInfoCache.trimToSize(Math.max(1, ActivityManager.getMaxRecentTasksStatic() / 2));
                return;
            case 10:
            case 60:
                this.mThumbnailCache.trimToSize(Math.max(1, this.mMaxThumbnailCacheSize / 4));
                this.mIconCache.trimToSize(Math.max(1, this.mMaxIconCacheSize / 4));
                this.mActivityInfoCache.trimToSize(Math.max(1, ActivityManager.getMaxRecentTasksStatic() / 4));
                return;
            case 15:
            case 80:
                this.mThumbnailCache.evictAll();
                this.mIconCache.evictAll();
                this.mActivityInfoCache.evictAll();
                this.mActivityLabelCache.evictAll();
                this.mContentDescriptionCache.evictAll();
                return;
            case 20:
                stopLoader();
                if (configuration.svelteLevel == 0) {
                    this.mThumbnailCache.trimToSize(Math.max(this.mNumVisibleTasksLoaded, this.mMaxThumbnailCacheSize / 2));
                } else if (configuration.svelteLevel == 1) {
                    this.mThumbnailCache.trimToSize(this.mNumVisibleThumbnailsLoaded);
                } else if (configuration.svelteLevel >= 2) {
                    this.mThumbnailCache.evictAll();
                }
                this.mIconCache.trimToSize(Math.max(this.mNumVisibleTasksLoaded, this.mMaxIconCacheSize / 2));
                return;
            default:
                return;
        }
    }

    public void preloadTasks(RecentsTaskLoadPlan recentsTaskLoadPlan, int i, boolean z) {
        recentsTaskLoadPlan.preloadPlan(this, i, z);
    }

    public void unloadTaskData(Task task) {
        this.mLoadQueue.removeTask(task);
        task.notifyTaskDataUnloaded(null, this.mDefaultIcon);
    }
}
