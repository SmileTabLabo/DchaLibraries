package com.android.systemui.shared.recents.model;

import java.util.concurrent.ConcurrentLinkedQueue;
/* loaded from: classes.dex */
class TaskResourceLoadQueue {
    private final ConcurrentLinkedQueue<Task> mQueue = new ConcurrentLinkedQueue<>();

    /* JADX INFO: Access modifiers changed from: package-private */
    public void addTask(Task t) {
        if (!this.mQueue.contains(t)) {
            this.mQueue.add(t);
        }
        synchronized (this) {
            notifyAll();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public Task nextTask() {
        return this.mQueue.poll();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void removeTask(Task t) {
        this.mQueue.remove(t);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void clearTasks() {
        this.mQueue.clear();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean isEmpty() {
        return this.mQueue.isEmpty();
    }
}
