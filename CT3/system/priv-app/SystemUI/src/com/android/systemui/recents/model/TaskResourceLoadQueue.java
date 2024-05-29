package com.android.systemui.recents.model;

import java.util.concurrent.ConcurrentLinkedQueue;
/* loaded from: a.zip:com/android/systemui/recents/model/TaskResourceLoadQueue.class */
class TaskResourceLoadQueue {
    ConcurrentLinkedQueue<Task> mQueue = new ConcurrentLinkedQueue<>();

    /* JADX INFO: Access modifiers changed from: package-private */
    public void addTask(Task task) {
        if (!this.mQueue.contains(task)) {
            this.mQueue.add(task);
        }
        synchronized (this) {
            notifyAll();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void clearTasks() {
        this.mQueue.clear();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean isEmpty() {
        return this.mQueue.isEmpty();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public Task nextTask() {
        return this.mQueue.poll();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void removeTask(Task task) {
        this.mQueue.remove(task);
    }
}
