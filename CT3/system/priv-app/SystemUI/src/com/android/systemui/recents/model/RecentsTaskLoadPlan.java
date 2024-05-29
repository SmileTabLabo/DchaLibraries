package com.android.systemui.recents.model;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.os.UserManager;
import android.util.ArraySet;
import android.util.SparseArray;
import android.util.SparseIntArray;
import com.android.systemui.Prefs;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.RecentsConfiguration;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.model.Task;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/* loaded from: a.zip:com/android/systemui/recents/model/RecentsTaskLoadPlan.class */
public class RecentsTaskLoadPlan {
    private static int MIN_NUM_TASKS = 5;
    private static int SESSION_BEGIN_TIME = 21600000;
    Context mContext;
    ArraySet<Integer> mCurrentQuietProfiles = new ArraySet<>();
    List<ActivityManager.RecentTaskInfo> mRawTasks;
    TaskStack mStack;

    /* loaded from: a.zip:com/android/systemui/recents/model/RecentsTaskLoadPlan$Options.class */
    public static class Options {
        public int runningTaskId = -1;
        public boolean loadIcons = true;
        public boolean loadThumbnails = true;
        public boolean onlyLoadForCache = false;
        public boolean onlyLoadPausedActivities = false;
        public int numVisibleTasks = 0;
        public int numVisibleTaskThumbnails = 0;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public RecentsTaskLoadPlan(Context context) {
        this.mContext = context;
    }

    private boolean isHistoricalTask(ActivityManager.RecentTaskInfo recentTaskInfo) {
        return recentTaskInfo.lastActiveTime < System.currentTimeMillis() - ((long) SESSION_BEGIN_TIME);
    }

    private void updateCurrentQuietProfilesCache(int i) {
        this.mCurrentQuietProfiles.clear();
        int i2 = i;
        if (i == -2) {
            i2 = ActivityManager.getCurrentUser();
        }
        List profiles = ((UserManager) this.mContext.getSystemService("user")).getProfiles(i2);
        if (profiles != null) {
            for (int i3 = 0; i3 < profiles.size(); i3++) {
                UserInfo userInfo = (UserInfo) profiles.get(i3);
                if (userInfo.isManagedProfile() && userInfo.isQuietModeEnabled()) {
                    this.mCurrentQuietProfiles.add(Integer.valueOf(userInfo.id));
                }
            }
        }
    }

    public void executePlan(Options options, RecentsTaskLoader recentsTaskLoader, TaskResourceLoadQueue taskResourceLoadQueue) {
        synchronized (this) {
            RecentsConfiguration configuration = Recents.getConfiguration();
            Resources resources = this.mContext.getResources();
            ArrayList<Task> stackTasks = this.mStack.getStackTasks();
            int size = stackTasks.size();
            int i = 0;
            while (i < size) {
                Task task = stackTasks.get(i);
                Task.TaskKey taskKey = task.key;
                boolean z = task.key.id == options.runningTaskId;
                boolean z2 = i >= size - options.numVisibleTasks;
                boolean z3 = i >= size - options.numVisibleTaskThumbnails;
                if (!options.onlyLoadPausedActivities || !z) {
                    if (options.loadIcons && ((z || z2) && task.icon == null)) {
                        task.icon = recentsTaskLoader.getAndUpdateActivityIcon(taskKey, task.taskDescription, resources, true);
                    }
                    if (options.loadThumbnails && ((z || z3) && (task.thumbnail == null || z))) {
                        if (configuration.svelteLevel <= 1) {
                            task.thumbnail = recentsTaskLoader.getAndUpdateThumbnail(taskKey, true);
                        } else if (configuration.svelteLevel == 2) {
                            taskResourceLoadQueue.addTask(task);
                        }
                    }
                }
                i++;
            }
        }
    }

    public TaskStack getTaskStack() {
        return this.mStack;
    }

    public boolean hasTasks() {
        boolean z = false;
        if (this.mStack != null) {
            if (this.mStack.getTaskCount() > 0) {
                z = true;
            }
            return z;
        }
        return false;
    }

    public void preloadPlan(RecentsTaskLoader recentsTaskLoader, int i, boolean z) {
        synchronized (this) {
            Resources resources = this.mContext.getResources();
            ArrayList arrayList = new ArrayList();
            if (this.mRawTasks == null) {
                preloadRawTasks(z);
            }
            SparseArray sparseArray = new SparseArray();
            SparseIntArray sparseIntArray = new SparseIntArray();
            String string = this.mContext.getString(2131493436);
            String string2 = this.mContext.getString(2131493439);
            long j = Prefs.getLong(this.mContext, "OverviewLastStackTaskActiveTime", 0L);
            long j2 = -1;
            int size = this.mRawTasks.size();
            int i2 = 0;
            while (i2 < size) {
                ActivityManager.RecentTaskInfo recentTaskInfo = this.mRawTasks.get(i2);
                Task.TaskKey taskKey = new Task.TaskKey(recentTaskInfo.persistentId, recentTaskInfo.stackId, recentTaskInfo.baseIntent, recentTaskInfo.userId, recentTaskInfo.firstActiveTime, recentTaskInfo.lastActiveTime);
                boolean z2 = (SystemServicesProxy.isFreeformStack(recentTaskInfo.stackId) || !isHistoricalTask(recentTaskInfo)) ? true : recentTaskInfo.lastActiveTime >= j && i2 >= size - MIN_NUM_TASKS;
                boolean z3 = taskKey.id == i;
                long j3 = j2;
                if (z2) {
                    j3 = j2;
                    if (j2 < 0) {
                        j3 = recentTaskInfo.lastActiveTime;
                    }
                }
                ActivityInfo andUpdateActivityInfo = recentsTaskLoader.getAndUpdateActivityInfo(taskKey);
                String andUpdateActivityTitle = recentsTaskLoader.getAndUpdateActivityTitle(taskKey, recentTaskInfo.taskDescription);
                String andUpdateContentDescription = recentsTaskLoader.getAndUpdateContentDescription(taskKey, resources);
                arrayList.add(new Task(taskKey, recentTaskInfo.affiliatedTaskId, recentTaskInfo.affiliatedTaskColor, z2 ? recentsTaskLoader.getAndUpdateActivityIcon(taskKey, recentTaskInfo.taskDescription, resources, false) : null, recentsTaskLoader.getAndUpdateThumbnail(taskKey, false), andUpdateActivityTitle, andUpdateContentDescription, String.format(string, andUpdateContentDescription), String.format(string2, andUpdateContentDescription), recentsTaskLoader.getActivityPrimaryColor(recentTaskInfo.taskDescription), recentsTaskLoader.getActivityBackgroundColor(recentTaskInfo.taskDescription), z3, z2, andUpdateActivityInfo != null ? (andUpdateActivityInfo.applicationInfo.flags & 1) != 0 : false, recentTaskInfo.isDockable, recentTaskInfo.bounds, recentTaskInfo.taskDescription, recentTaskInfo.resizeMode, recentTaskInfo.topActivity));
                sparseIntArray.put(taskKey.id, sparseIntArray.get(taskKey.id, 0) + 1);
                sparseArray.put(taskKey.id, taskKey);
                i2++;
                j2 = j3;
            }
            if (j2 != -1) {
                Prefs.putLong(this.mContext, "OverviewLastStackTaskActiveTime", j2);
            }
            this.mStack = new TaskStack();
            this.mStack.setTasks(this.mContext, arrayList, false);
        }
    }

    public void preloadRawTasks(boolean z) {
        synchronized (this) {
            updateCurrentQuietProfilesCache(-2);
            this.mRawTasks = Recents.getSystemServices().getRecentTasks(ActivityManager.getMaxRecentTasksStatic(), -2, z, this.mCurrentQuietProfiles);
            Collections.reverse(this.mRawTasks);
        }
    }
}
