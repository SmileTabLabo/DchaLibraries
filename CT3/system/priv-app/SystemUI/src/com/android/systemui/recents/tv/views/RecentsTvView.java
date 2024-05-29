package com.android.systemui.recents.tv.views;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.events.activity.CancelEnterRecentsWindowAnimationEvent;
import com.android.systemui.recents.events.activity.DismissRecentsToHomeAnimationStarted;
import com.android.systemui.recents.events.activity.LaunchTvTaskEvent;
import com.android.systemui.recents.events.component.RecentsVisibilityChangedEvent;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.model.TaskStack;
/* loaded from: a.zip:com/android/systemui/recents/tv/views/RecentsTvView.class */
public class RecentsTvView extends FrameLayout {
    private boolean mAwaitingFirstLayout;
    private View mDismissPlaceholder;
    private View mEmptyView;
    private final Handler mHandler;
    private RecyclerView.OnScrollListener mScrollListener;
    private TaskStack mStack;
    private Rect mSystemInsets;
    private TaskStackHorizontalGridView mTaskStackHorizontalView;
    private RecentsTvTransitionHelper mTransitionHelper;

    public RecentsTvView(Context context) {
        this(context, null);
    }

    public RecentsTvView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public RecentsTvView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public RecentsTvView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mAwaitingFirstLayout = true;
        this.mSystemInsets = new Rect();
        this.mHandler = new Handler();
        setWillNotDraw(false);
        this.mEmptyView = LayoutInflater.from(context).inflate(2130968787, (ViewGroup) this, false);
        addView(this.mEmptyView);
        this.mTransitionHelper = new RecentsTvTransitionHelper(this.mContext, this.mHandler);
    }

    private void launchTaskFomRecents(Task task, boolean z) {
        if (!z) {
            Recents.getSystemServices().startActivityFromRecents(getContext(), task.key, task.title, null);
            return;
        }
        this.mTaskStackHorizontalView.requestFocus();
        Task focusedTask = this.mTaskStackHorizontalView.getFocusedTask();
        if (focusedTask == null || task == focusedTask) {
            this.mTransitionHelper.launchTaskFromRecents(this.mStack, task, this.mTaskStackHorizontalView, this.mTaskStackHorizontalView.getChildViewForTask(task), null, -1);
            return;
        }
        if (this.mScrollListener != null) {
            this.mTaskStackHorizontalView.removeOnScrollListener(this.mScrollListener);
        }
        this.mScrollListener = new RecyclerView.OnScrollListener(this, task) { // from class: com.android.systemui.recents.tv.views.RecentsTvView.1
            final RecentsTvView this$0;
            final Task val$task;

            {
                this.this$0 = this;
                this.val$task = task;
            }

            @Override // android.support.v7.widget.RecyclerView.OnScrollListener
            public void onScrollStateChanged(RecyclerView recyclerView, int i) {
                super.onScrollStateChanged(recyclerView, i);
                if (i == 0) {
                    TaskCardView childViewForTask = this.this$0.mTaskStackHorizontalView.getChildViewForTask(this.val$task);
                    if (childViewForTask != null) {
                        this.this$0.mTransitionHelper.launchTaskFromRecents(this.this$0.mStack, this.val$task, this.this$0.mTaskStackHorizontalView, childViewForTask, null, -1);
                    } else {
                        Log.e("RecentsTvView", "Card view for task : " + this.val$task + ", returned null.");
                        Recents.getSystemServices().startActivityFromRecents(this.this$0.getContext(), this.val$task.key, this.val$task.title, null);
                    }
                    this.this$0.mTaskStackHorizontalView.removeOnScrollListener(this.this$0.mScrollListener);
                }
            }
        };
        this.mTaskStackHorizontalView.addOnScrollListener(this.mScrollListener);
        this.mTaskStackHorizontalView.setSelectedPositionSmooth(((TaskStackHorizontalViewAdapter) this.mTaskStackHorizontalView.getAdapter()).getPositionOfTask(task));
    }

    public void hideEmptyView() {
        this.mEmptyView.setVisibility(8);
        this.mTaskStackHorizontalView.setVisibility(0);
        if (Recents.getSystemServices().isTouchExplorationEnabled()) {
            this.mDismissPlaceholder.setVisibility(0);
        }
    }

    public void init(TaskStack taskStack) {
        Recents.getConfiguration().getLaunchState();
        this.mStack = taskStack;
        this.mTaskStackHorizontalView.init(taskStack);
        if (taskStack.getStackTaskCount() > 0) {
            hideEmptyView();
        } else {
            showEmptyView();
        }
        requestLayout();
    }

    public boolean launchFocusedTask() {
        Task focusedTask;
        if (this.mTaskStackHorizontalView == null || (focusedTask = this.mTaskStackHorizontalView.getFocusedTask()) == null) {
            return false;
        }
        launchTaskFomRecents(focusedTask, true);
        return true;
    }

    public boolean launchPreviousTask(boolean z) {
        Task launchTarget;
        if (this.mTaskStackHorizontalView == null || (launchTarget = this.mTaskStackHorizontalView.getStack().getLaunchTarget()) == null) {
            return false;
        }
        launchTaskFomRecents(launchTarget, z);
        return true;
    }

    @Override // android.view.View
    public WindowInsets onApplyWindowInsets(WindowInsets windowInsets) {
        this.mSystemInsets.set(windowInsets.getSystemWindowInsets());
        requestLayout();
        return windowInsets;
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        EventBus.getDefault().register(this, 3);
        super.onAttachedToWindow();
    }

    public final void onBusEvent(DismissRecentsToHomeAnimationStarted dismissRecentsToHomeAnimationStarted) {
        EventBus.getDefault().send(new CancelEnterRecentsWindowAnimationEvent(null));
    }

    public final void onBusEvent(LaunchTvTaskEvent launchTvTaskEvent) {
        this.mTransitionHelper.launchTaskFromRecents(this.mStack, launchTvTaskEvent.task, this.mTaskStackHorizontalView, launchTvTaskEvent.taskView, launchTvTaskEvent.targetTaskBounds, launchTvTaskEvent.targetTaskStack);
    }

    public final void onBusEvent(RecentsVisibilityChangedEvent recentsVisibilityChangedEvent) {
        if (recentsVisibilityChangedEvent.visible) {
            return;
        }
        this.mAwaitingFirstLayout = true;
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mDismissPlaceholder = findViewById(2131886612);
        this.mTaskStackHorizontalView = (TaskStackHorizontalGridView) findViewById(2131886610);
    }

    public TaskStackHorizontalGridView setTaskStackViewAdapter(TaskStackHorizontalViewAdapter taskStackHorizontalViewAdapter) {
        if (this.mTaskStackHorizontalView != null) {
            this.mTaskStackHorizontalView.setAdapter(taskStackHorizontalViewAdapter);
            taskStackHorizontalViewAdapter.setTaskStackHorizontalGridView(this.mTaskStackHorizontalView);
        }
        return this.mTaskStackHorizontalView;
    }

    public void showEmptyView() {
        this.mEmptyView.setVisibility(0);
        this.mTaskStackHorizontalView.setVisibility(8);
        if (Recents.getSystemServices().isTouchExplorationEnabled()) {
            this.mDismissPlaceholder.setVisibility(8);
        }
    }
}
