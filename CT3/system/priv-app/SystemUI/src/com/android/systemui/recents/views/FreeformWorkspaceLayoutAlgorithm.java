package com.android.systemui.recents.views;

import android.content.Context;
import android.graphics.RectF;
import android.util.ArrayMap;
import com.android.systemui.recents.model.Task;
import java.util.Collections;
import java.util.List;
/* loaded from: a.zip:com/android/systemui/recents/views/FreeformWorkspaceLayoutAlgorithm.class */
public class FreeformWorkspaceLayoutAlgorithm {
    private int mTaskPadding;
    private ArrayMap<Task.TaskKey, RectF> mTaskRectMap = new ArrayMap<>();

    public FreeformWorkspaceLayoutAlgorithm(Context context) {
        reloadOnConfigurationChange(context);
    }

    public TaskViewTransform getTransform(Task task, TaskViewTransform taskViewTransform, TaskStackLayoutAlgorithm taskStackLayoutAlgorithm) {
        if (this.mTaskRectMap.containsKey(task.key)) {
            RectF rectF = this.mTaskRectMap.get(task.key);
            taskViewTransform.scale = 1.0f;
            taskViewTransform.alpha = 1.0f;
            taskViewTransform.translationZ = taskStackLayoutAlgorithm.mMaxTranslationZ;
            taskViewTransform.dimAlpha = 0.0f;
            taskViewTransform.viewOutlineAlpha = 2.0f;
            taskViewTransform.rect.set(rectF);
            taskViewTransform.rect.offset(taskStackLayoutAlgorithm.mFreeformRect.left, taskStackLayoutAlgorithm.mFreeformRect.top);
            taskViewTransform.visible = true;
            return taskViewTransform;
        }
        return null;
    }

    public boolean isTransformAvailable(Task task, TaskStackLayoutAlgorithm taskStackLayoutAlgorithm) {
        if (taskStackLayoutAlgorithm.mNumFreeformTasks == 0 || task == null) {
            return false;
        }
        return this.mTaskRectMap.containsKey(task.key);
    }

    public void reloadOnConfigurationChange(Context context) {
        this.mTaskPadding = context.getResources().getDimensionPixelSize(2131690011) / 2;
    }

    public void update(List<Task> list, TaskStackLayoutAlgorithm taskStackLayoutAlgorithm) {
        Task task;
        Collections.reverse(list);
        this.mTaskRectMap.clear();
        int i = taskStackLayoutAlgorithm.mNumFreeformTasks;
        if (list.isEmpty()) {
            return;
        }
        int width = taskStackLayoutAlgorithm.mFreeformRect.width();
        int height = taskStackLayoutAlgorithm.mFreeformRect.height();
        float f = width / height;
        float[] fArr = new float[i];
        for (int i2 = 0; i2 < i; i2++) {
            fArr[i2] = Math.min(list.get(i2).bounds != null ? task.bounds.width() / task.bounds.height() : f, f);
        }
        float f2 = 0.85f;
        float f3 = 0.0f;
        float f4 = 0.0f;
        int i3 = 1;
        int i4 = 0;
        while (i4 < i) {
            float f5 = fArr[i4] * f2;
            if (f3 + f5 <= f) {
                f3 += f5;
                i4++;
            } else if ((i3 + 1) * f2 > 1.0f) {
                f2 = Math.min(f / (f3 + f5), 1.0f / (i3 + 1));
                i3 = 1;
                f3 = 0.0f;
                i4 = 0;
            } else {
                f3 = f5;
                i3++;
                i4++;
            }
            f4 = Math.max(f3, f4);
        }
        float f6 = ((1.0f - (f4 / f)) * width) / 2.0f;
        int i5 = (int) f6;
        float f7 = ((1.0f - (i3 * f2)) * height) / 2.0f;
        float f8 = f2 * height;
        int i6 = 0;
        while (i6 < i) {
            Task task2 = list.get(i6);
            int i7 = (int) (fArr[i6] * f8);
            int i8 = i5;
            float f9 = f7;
            if (i5 + i7 > width) {
                f9 = f7 + f8;
                i8 = (int) f6;
            }
            RectF rectF = new RectF(i8, f9, i8 + i7, f9 + f8);
            rectF.inset(this.mTaskPadding, this.mTaskPadding);
            i5 = i8 + i7;
            this.mTaskRectMap.put(task2.key, rectF);
            i6++;
            f7 = f9;
        }
    }
}
