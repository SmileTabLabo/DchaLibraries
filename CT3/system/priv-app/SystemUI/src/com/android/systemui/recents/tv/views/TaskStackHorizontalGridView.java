package com.android.systemui.recents.tv.views;

import android.content.Context;
import android.support.v17.leanback.widget.HorizontalGridView;
import android.util.AttributeSet;
import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.events.ui.AllTaskViewsDismissedEvent;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.model.TaskStack;
import com.android.systemui.recents.views.AnimationProps;
/* loaded from: a.zip:com/android/systemui/recents/tv/views/TaskStackHorizontalGridView.class */
public class TaskStackHorizontalGridView extends HorizontalGridView implements TaskStack.TaskStackCallbacks {
    private Task mFocusedTask;
    private TaskStack mStack;

    public TaskStackHorizontalGridView(Context context) {
        this(context, null);
    }

    public TaskStackHorizontalGridView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public TaskCardView getChildViewForTask(Task task) {
        for (int i = 0; i < getChildCount(); i++) {
            TaskCardView taskCardView = (TaskCardView) getChildAt(i);
            if (taskCardView.getTask() == task) {
                return taskCardView;
            }
        }
        return null;
    }

    public Task getFocusedTask() {
        if (findFocus() != null) {
            this.mFocusedTask = ((TaskCardView) findFocus()).getTask();
        }
        return this.mFocusedTask;
    }

    public TaskStack getStack() {
        return this.mStack;
    }

    public void init(TaskStack taskStack) {
        this.mStack = taskStack;
        if (this.mStack != null) {
            this.mStack.setCallbacks(this);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.v7.widget.RecyclerView, android.view.ViewGroup, android.view.View
    public void onAttachedToWindow() {
        EventBus.getDefault().register(this, 3);
        setWindowAlignment(0);
        setImportantForAccessibility(1);
        super.onAttachedToWindow();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.v7.widget.RecyclerView, android.view.ViewGroup, android.view.View
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
    }

    @Override // com.android.systemui.recents.model.TaskStack.TaskStackCallbacks
    public void onStackTaskAdded(TaskStack taskStack, Task task) {
        ((TaskStackHorizontalViewAdapter) getAdapter()).addTaskAt(task, taskStack.indexOfStackTask(task));
    }

    @Override // com.android.systemui.recents.model.TaskStack.TaskStackCallbacks
    public void onStackTaskRemoved(TaskStack taskStack, Task task, Task task2, AnimationProps animationProps, boolean z) {
        boolean z2 = false;
        ((TaskStackHorizontalViewAdapter) getAdapter()).removeTask(task);
        if (this.mFocusedTask == task) {
            this.mFocusedTask = null;
        }
        if (this.mStack.getStackTaskCount() == 0) {
            if (this.mStack.getStackTaskCount() == 0) {
                z2 = true;
            }
            if (z2) {
                EventBus.getDefault().send(new AllTaskViewsDismissedEvent(z ? 2131493579 : 2131493580));
            }
        }
    }

    @Override // com.android.systemui.recents.model.TaskStack.TaskStackCallbacks
    public void onStackTasksRemoved(TaskStack taskStack) {
    }

    @Override // com.android.systemui.recents.model.TaskStack.TaskStackCallbacks
    public void onStackTasksUpdated(TaskStack taskStack) {
    }

    public void startFocusGainAnimation() {
        for (int i = 0; i < getChildCount(); i++) {
            TaskCardView taskCardView = (TaskCardView) getChildAt(i);
            if (taskCardView.hasFocus()) {
                taskCardView.getViewFocusAnimator().changeSize(true);
            }
            taskCardView.getRecentsRowFocusAnimationHolder().startFocusGainAnimation();
        }
    }

    public void startFocusLossAnimation() {
        for (int i = 0; i < getChildCount(); i++) {
            TaskCardView taskCardView = (TaskCardView) getChildAt(i);
            if (taskCardView.hasFocus()) {
                taskCardView.getViewFocusAnimator().changeSize(false);
            }
            taskCardView.getRecentsRowFocusAnimationHolder().startFocusLossAnimation();
        }
    }
}
