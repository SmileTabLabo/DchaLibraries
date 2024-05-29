package com.android.systemui.recents.model;

import android.util.ArrayMap;
import com.android.systemui.recents.model.Task;
import java.util.ArrayList;
/* loaded from: a.zip:com/android/systemui/recents/model/TaskGrouping.class */
public class TaskGrouping {
    int affiliation;
    long latestActiveTimeInGroup;
    Task.TaskKey mFrontMostTaskKey;
    ArrayList<Task.TaskKey> mTaskKeys = new ArrayList<>();
    ArrayMap<Task.TaskKey, Integer> mTaskKeyIndices = new ArrayMap<>();

    public TaskGrouping(int i) {
        this.affiliation = i;
    }

    private void updateTaskIndices() {
        if (this.mTaskKeys.isEmpty()) {
            this.mFrontMostTaskKey = null;
            this.mTaskKeyIndices.clear();
            return;
        }
        int size = this.mTaskKeys.size();
        this.mFrontMostTaskKey = this.mTaskKeys.get(this.mTaskKeys.size() - 1);
        this.mTaskKeyIndices.clear();
        for (int i = 0; i < size; i++) {
            this.mTaskKeyIndices.put(this.mTaskKeys.get(i), Integer.valueOf(i));
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void addTask(Task task) {
        this.mTaskKeys.add(task.key);
        if (task.key.lastActiveTime > this.latestActiveTimeInGroup) {
            this.latestActiveTimeInGroup = task.key.lastActiveTime;
        }
        task.setGroup(this);
        updateTaskIndices();
    }

    public Task.TaskKey getNextTaskInGroup(Task task) {
        int indexOf = indexOf(task);
        if (indexOf + 1 < getTaskCount()) {
            return this.mTaskKeys.get(indexOf + 1);
        }
        return null;
    }

    public Task.TaskKey getPrevTaskInGroup(Task task) {
        int indexOf = indexOf(task);
        if (indexOf - 1 >= 0) {
            return this.mTaskKeys.get(indexOf - 1);
        }
        return null;
    }

    public int getTaskCount() {
        return this.mTaskKeys.size();
    }

    public int indexOf(Task task) {
        return this.mTaskKeyIndices.get(task.key).intValue();
    }

    public boolean isFrontMostTask(Task task) {
        return task.key == this.mFrontMostTaskKey;
    }

    public boolean isTaskAboveTask(Task task, Task task2) {
        return (this.mTaskKeyIndices.containsKey(task.key) && this.mTaskKeyIndices.containsKey(task2.key)) ? this.mTaskKeyIndices.get(task.key).intValue() > this.mTaskKeyIndices.get(task2.key).intValue() : false;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void removeTask(Task task) {
        this.mTaskKeys.remove(task.key);
        this.latestActiveTimeInGroup = 0L;
        int size = this.mTaskKeys.size();
        for (int i = 0; i < size; i++) {
            long j = this.mTaskKeys.get(i).lastActiveTime;
            if (j > this.latestActiveTimeInGroup) {
                this.latestActiveTimeInGroup = j;
            }
        }
        task.setGroup(null);
        updateTaskIndices();
    }
}
