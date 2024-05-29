package com.android.systemui.recents.model;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.ViewDebug;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.misc.Utilities;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Objects;
/* loaded from: a.zip:com/android/systemui/recents/model/Task.class */
public class Task {
    @ViewDebug.ExportedProperty(category = "recents")
    public int affiliationColor;
    @ViewDebug.ExportedProperty(category = "recents")
    public int affiliationTaskId;
    @ViewDebug.ExportedProperty(category = "recents")
    public String appInfoDescription;
    @ViewDebug.ExportedProperty(category = "recents")
    public Rect bounds;
    @ViewDebug.ExportedProperty(category = "recents")
    public int colorBackground;
    @ViewDebug.ExportedProperty(category = "recents")
    public int colorPrimary;
    @ViewDebug.ExportedProperty(category = "recents")
    public String dismissDescription;
    @ViewDebug.ExportedProperty(deepExport = true, prefix = "group_")
    public TaskGrouping group;
    public Drawable icon;
    @ViewDebug.ExportedProperty(category = "recents")
    public boolean isDockable;
    @ViewDebug.ExportedProperty(category = "recents")
    public boolean isLaunchTarget;
    @ViewDebug.ExportedProperty(category = "recents")
    public boolean isStackTask;
    @ViewDebug.ExportedProperty(category = "recents")
    public boolean isSystemApp;
    @ViewDebug.ExportedProperty(deepExport = true, prefix = "key_")
    public TaskKey key;
    private ArrayList<TaskCallbacks> mCallbacks = new ArrayList<>();
    @ViewDebug.ExportedProperty(category = "recents")
    public int resizeMode;
    public ActivityManager.TaskDescription taskDescription;
    public int temporarySortIndexInStack;
    public Bitmap thumbnail;
    @ViewDebug.ExportedProperty(category = "recents")
    public String title;
    @ViewDebug.ExportedProperty(category = "recents")
    public String titleDescription;
    @ViewDebug.ExportedProperty(category = "recents")
    public ComponentName topActivity;
    @ViewDebug.ExportedProperty(category = "recents")
    public boolean useLightOnPrimaryColor;

    /* loaded from: a.zip:com/android/systemui/recents/model/Task$TaskCallbacks.class */
    public interface TaskCallbacks {
        void onTaskDataLoaded(Task task, ActivityManager.TaskThumbnailInfo taskThumbnailInfo);

        void onTaskDataUnloaded();

        void onTaskStackIdChanged();
    }

    /* loaded from: a.zip:com/android/systemui/recents/model/Task$TaskKey.class */
    public static class TaskKey {
        @ViewDebug.ExportedProperty(category = "recents")
        public final Intent baseIntent;
        @ViewDebug.ExportedProperty(category = "recents")
        public long firstActiveTime;
        @ViewDebug.ExportedProperty(category = "recents")
        public final int id;
        @ViewDebug.ExportedProperty(category = "recents")
        public long lastActiveTime;
        private int mHashCode;
        @ViewDebug.ExportedProperty(category = "recents")
        public int stackId;
        @ViewDebug.ExportedProperty(category = "recents")
        public final int userId;

        public TaskKey(int i, int i2, Intent intent, int i3, long j, long j2) {
            this.id = i;
            this.stackId = i2;
            this.baseIntent = intent;
            this.userId = i3;
            this.firstActiveTime = j;
            this.lastActiveTime = j2;
            updateHashCode();
        }

        private void updateHashCode() {
            this.mHashCode = Objects.hash(Integer.valueOf(this.id), Integer.valueOf(this.stackId), Integer.valueOf(this.userId));
        }

        public boolean equals(Object obj) {
            if (obj instanceof TaskKey) {
                TaskKey taskKey = (TaskKey) obj;
                boolean z = false;
                if (this.id == taskKey.id) {
                    z = false;
                    if (this.stackId == taskKey.stackId) {
                        z = false;
                        if (this.userId == taskKey.userId) {
                            z = true;
                        }
                    }
                }
                return z;
            }
            return false;
        }

        public ComponentName getComponent() {
            return this.baseIntent.getComponent();
        }

        public int hashCode() {
            return this.mHashCode;
        }

        public void setStackId(int i) {
            this.stackId = i;
            updateHashCode();
        }

        public String toString() {
            return "id=" + this.id + " stackId=" + this.stackId + " user=" + this.userId + " lastActiveTime=" + this.lastActiveTime + " component" + this.baseIntent.getComponent();
        }
    }

    public Task() {
    }

    public Task(TaskKey taskKey, int i, int i2, Drawable drawable, Bitmap bitmap, String str, String str2, String str3, String str4, int i3, int i4, boolean z, boolean z2, boolean z3, boolean z4, Rect rect, ActivityManager.TaskDescription taskDescription, int i5, ComponentName componentName) {
        boolean z5 = (i != taskKey.id) && i2 != 0;
        this.key = taskKey;
        this.affiliationTaskId = i;
        this.affiliationColor = i2;
        this.icon = drawable;
        this.thumbnail = bitmap;
        this.title = str;
        this.titleDescription = str2;
        this.dismissDescription = str3;
        this.appInfoDescription = str4;
        this.colorPrimary = z5 ? i2 : i3;
        this.colorBackground = i4;
        this.useLightOnPrimaryColor = Utilities.computeContrastBetweenColors(this.colorPrimary, -1) > 3.0f;
        this.bounds = rect;
        this.taskDescription = taskDescription;
        this.isLaunchTarget = z;
        this.isStackTask = z2;
        this.isSystemApp = z3;
        this.isDockable = z4;
        this.resizeMode = i5;
        this.topActivity = componentName;
    }

    public void addCallback(TaskCallbacks taskCallbacks) {
        if (this.mCallbacks.contains(taskCallbacks)) {
            return;
        }
        this.mCallbacks.add(taskCallbacks);
    }

    public void copyFrom(Task task) {
        this.key = task.key;
        this.group = task.group;
        this.affiliationTaskId = task.affiliationTaskId;
        this.affiliationColor = task.affiliationColor;
        this.icon = task.icon;
        this.thumbnail = task.thumbnail;
        this.title = task.title;
        this.titleDescription = task.titleDescription;
        this.dismissDescription = task.dismissDescription;
        this.appInfoDescription = task.appInfoDescription;
        this.colorPrimary = task.colorPrimary;
        this.colorBackground = task.colorBackground;
        this.useLightOnPrimaryColor = task.useLightOnPrimaryColor;
        this.bounds = task.bounds;
        this.taskDescription = task.taskDescription;
        this.isLaunchTarget = task.isLaunchTarget;
        this.isStackTask = task.isStackTask;
        this.isSystemApp = task.isSystemApp;
        this.isDockable = task.isDockable;
        this.resizeMode = task.resizeMode;
        this.topActivity = task.topActivity;
    }

    public void dump(String str, PrintWriter printWriter) {
        printWriter.print(str);
        printWriter.print(this.key);
        if (isAffiliatedTask()) {
            printWriter.print(" ");
            printWriter.print("affTaskId=" + this.affiliationTaskId);
        }
        if (!this.isDockable) {
            printWriter.print(" dockable=N");
        }
        if (this.isLaunchTarget) {
            printWriter.print(" launchTarget=Y");
        }
        if (isFreeformTask()) {
            printWriter.print(" freeform=Y");
        }
        printWriter.print(" ");
        printWriter.print(this.title);
        printWriter.println();
    }

    public boolean equals(Object obj) {
        return this.key.equals(((Task) obj).key);
    }

    public ComponentName getTopComponent() {
        return this.topActivity != null ? this.topActivity : this.key.baseIntent.getComponent();
    }

    public boolean isAffiliatedTask() {
        return this.key.id != this.affiliationTaskId;
    }

    public boolean isFreeformTask() {
        return Recents.getSystemServices().hasFreeformWorkspaceSupport() ? SystemServicesProxy.isFreeformStack(this.key.stackId) : false;
    }

    public void notifyTaskDataLoaded(Bitmap bitmap, Drawable drawable, ActivityManager.TaskThumbnailInfo taskThumbnailInfo) {
        this.icon = drawable;
        this.thumbnail = bitmap;
        int size = this.mCallbacks.size();
        for (int i = 0; i < size; i++) {
            this.mCallbacks.get(i).onTaskDataLoaded(this, taskThumbnailInfo);
        }
    }

    public void notifyTaskDataUnloaded(Bitmap bitmap, Drawable drawable) {
        this.icon = drawable;
        this.thumbnail = bitmap;
        for (int size = this.mCallbacks.size() - 1; size >= 0; size--) {
            this.mCallbacks.get(size).onTaskDataUnloaded();
        }
    }

    public void removeCallback(TaskCallbacks taskCallbacks) {
        this.mCallbacks.remove(taskCallbacks);
    }

    public void setGroup(TaskGrouping taskGrouping) {
        this.group = taskGrouping;
    }

    public void setStackId(int i) {
        this.key.setStackId(i);
        int size = this.mCallbacks.size();
        for (int i2 = 0; i2 < size; i2++) {
            this.mCallbacks.get(i2).onTaskStackIdChanged();
        }
    }

    public String toString() {
        return "[" + this.key.toString() + "] " + this.title;
    }
}
