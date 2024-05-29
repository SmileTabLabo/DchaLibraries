package com.android.systemui.recents.tv.views;

import android.animation.Animator;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.events.activity.LaunchTvTaskEvent;
import com.android.systemui.recents.events.ui.DeleteTaskDataEvent;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.views.AnimationProps;
import java.util.ArrayList;
import java.util.List;
/* loaded from: a.zip:com/android/systemui/recents/tv/views/TaskStackHorizontalViewAdapter.class */
public class TaskStackHorizontalViewAdapter extends RecyclerView.Adapter<ViewHolder> {
    private TaskStackHorizontalGridView mGridView;
    private List<Task> mTaskList;

    /* loaded from: a.zip:com/android/systemui/recents/tv/views/TaskStackHorizontalViewAdapter$ViewHolder.class */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Task mTask;
        private TaskCardView mTaskCardView;
        final TaskStackHorizontalViewAdapter this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public ViewHolder(TaskStackHorizontalViewAdapter taskStackHorizontalViewAdapter, View view) {
            super(view);
            this.this$0 = taskStackHorizontalViewAdapter;
            this.mTaskCardView = (TaskCardView) view;
        }

        private Animator.AnimatorListener getRemoveAtListener(int i, Task task) {
            return new Animator.AnimatorListener(this, task) { // from class: com.android.systemui.recents.tv.views.TaskStackHorizontalViewAdapter.ViewHolder.1
                final ViewHolder this$1;
                final Task val$task;

                {
                    this.this$1 = this;
                    this.val$task = task;
                }

                @Override // android.animation.Animator.AnimatorListener
                public void onAnimationCancel(Animator animator) {
                }

                @Override // android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    this.this$1.this$0.removeTask(this.val$task);
                    EventBus.getDefault().send(new DeleteTaskDataEvent(this.val$task));
                }

                @Override // android.animation.Animator.AnimatorListener
                public void onAnimationRepeat(Animator animator) {
                }

                @Override // android.animation.Animator.AnimatorListener
                public void onAnimationStart(Animator animator) {
                }
            };
        }

        public void init(Task task) {
            this.mTaskCardView.init(task);
            this.mTask = task;
            this.mTaskCardView.setOnClickListener(this);
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            try {
                if (this.mTaskCardView.isInDismissState()) {
                    this.mTaskCardView.startDismissTaskAnimation(getRemoveAtListener(getAdapterPosition(), this.mTaskCardView.getTask()));
                } else {
                    EventBus.getDefault().send(new LaunchTvTaskEvent(this.mTaskCardView, this.mTask, null, -1));
                }
            } catch (Exception e) {
                Log.e("TaskStackViewAdapter", view.getContext().getString(2131493584, this.mTask.title), e);
            }
        }
    }

    public TaskStackHorizontalViewAdapter(List list) {
        this.mTaskList = new ArrayList(list);
    }

    public void addTaskAt(Task task, int i) {
        this.mTaskList.add(i, task);
        notifyItemInserted(i);
    }

    @Override // android.support.v7.widget.RecyclerView.Adapter
    public int getItemCount() {
        return this.mTaskList.size();
    }

    public int getPositionOfTask(Task task) {
        int indexOf = this.mTaskList.indexOf(task);
        if (indexOf < 0) {
            indexOf = 0;
        }
        return indexOf;
    }

    @Override // android.support.v7.widget.RecyclerView.Adapter
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        Task task = this.mTaskList.get(i);
        Recents.getTaskLoader().loadTaskData(task);
        viewHolder.init(task);
    }

    @Override // android.support.v7.widget.RecyclerView.Adapter
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(this, LayoutInflater.from(viewGroup.getContext()).inflate(2130968788, viewGroup, false));
    }

    public void removeTask(Task task) {
        int indexOf = this.mTaskList.indexOf(task);
        if (indexOf >= 0) {
            this.mTaskList.remove(indexOf);
            notifyItemRemoved(indexOf);
            if (this.mGridView != null) {
                this.mGridView.getStack().removeTask(task, AnimationProps.IMMEDIATE, false);
            }
        }
    }

    public void setNewStackTasks(List list) {
        this.mTaskList.clear();
        this.mTaskList.addAll(list);
        notifyDataSetChanged();
    }

    public void setTaskStackHorizontalGridView(TaskStackHorizontalGridView taskStackHorizontalGridView) {
        this.mGridView = taskStackHorizontalGridView;
    }
}
