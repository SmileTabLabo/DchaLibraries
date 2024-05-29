package com.android.systemui.recents.model;

import android.util.Log;
import android.util.LruCache;
import android.util.SparseArray;
import com.android.systemui.recents.model.Task;
import java.io.PrintWriter;
/* loaded from: a.zip:com/android/systemui/recents/model/TaskKeyLruCache.class */
public class TaskKeyLruCache<V> {
    private final LruCache<Integer, V> mCache;
    private final EvictionCallback mEvictionCallback;
    private final SparseArray<Task.TaskKey> mKeys;

    /* loaded from: a.zip:com/android/systemui/recents/model/TaskKeyLruCache$EvictionCallback.class */
    public interface EvictionCallback {
        void onEntryEvicted(Task.TaskKey taskKey);
    }

    public TaskKeyLruCache(int i) {
        this(i, null);
    }

    public TaskKeyLruCache(int i, EvictionCallback evictionCallback) {
        this.mKeys = new SparseArray<>();
        this.mEvictionCallback = evictionCallback;
        this.mCache = new LruCache<Integer, V>(this, i) { // from class: com.android.systemui.recents.model.TaskKeyLruCache.1
            final TaskKeyLruCache this$0;

            {
                this.this$0 = this;
            }

            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.util.LruCache
            public void entryRemoved(boolean z, Integer num, V v, V v2) {
                if (this.this$0.mEvictionCallback != null) {
                    this.this$0.mEvictionCallback.onEntryEvicted((Task.TaskKey) this.this$0.mKeys.get(num.intValue()));
                }
                this.this$0.mKeys.remove(num.intValue());
            }
        };
    }

    public void dump(String str, PrintWriter printWriter) {
        String str2 = str + "  ";
        printWriter.print(str);
        printWriter.print("TaskKeyLruCache");
        printWriter.print(" numEntries=");
        printWriter.print(this.mKeys.size());
        printWriter.println();
        int size = this.mKeys.size();
        for (int i = 0; i < size; i++) {
            printWriter.print(str2);
            printWriter.println(this.mKeys.get(this.mKeys.keyAt(i)));
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public final void evictAll() {
        this.mCache.evictAll();
        this.mKeys.clear();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public final V get(Task.TaskKey taskKey) {
        return this.mCache.get(Integer.valueOf(taskKey.id));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public final V getAndInvalidateIfModified(Task.TaskKey taskKey) {
        Task.TaskKey taskKey2 = this.mKeys.get(taskKey.id);
        if (taskKey2 == null || (taskKey2.stackId == taskKey.stackId && taskKey2.lastActiveTime == taskKey.lastActiveTime)) {
            return this.mCache.get(Integer.valueOf(taskKey.id));
        }
        remove(taskKey);
        return null;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public final void put(Task.TaskKey taskKey, V v) {
        if (taskKey == null || v == null) {
            Log.e("TaskKeyLruCache", "Unexpected null key or value: " + taskKey + ", " + v);
            return;
        }
        this.mKeys.put(taskKey.id, taskKey);
        this.mCache.put(Integer.valueOf(taskKey.id), v);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public final void remove(Task.TaskKey taskKey) {
        this.mCache.remove(Integer.valueOf(taskKey.id));
        this.mKeys.remove(taskKey.id);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public final void trimToSize(int i) {
        this.mCache.trimToSize(i);
    }
}
