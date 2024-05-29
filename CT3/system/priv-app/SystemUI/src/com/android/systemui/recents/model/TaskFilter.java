package com.android.systemui.recents.model;

import android.util.SparseArray;
/* loaded from: a.zip:com/android/systemui/recents/model/TaskFilter.class */
interface TaskFilter {
    boolean acceptTask(SparseArray<Task> sparseArray, Task task, int i);
}
