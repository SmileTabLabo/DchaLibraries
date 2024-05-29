package com.android.systemui.recents.views;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.IntDef;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.MutableBoolean;
import android.util.Property;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.Interpolators;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.events.activity.CancelEnterRecentsWindowAnimationEvent;
import com.android.systemui.recents.events.activity.ConfigurationChangedEvent;
import com.android.systemui.recents.events.activity.DismissRecentsToHomeAnimationStarted;
import com.android.systemui.recents.events.activity.EnterRecentsTaskStackAnimationCompletedEvent;
import com.android.systemui.recents.events.activity.EnterRecentsWindowAnimationCompletedEvent;
import com.android.systemui.recents.events.activity.HideRecentsEvent;
import com.android.systemui.recents.events.activity.HideStackActionButtonEvent;
import com.android.systemui.recents.events.activity.IterateRecentsEvent;
import com.android.systemui.recents.events.activity.LaunchNextTaskRequestEvent;
import com.android.systemui.recents.events.activity.LaunchTaskEvent;
import com.android.systemui.recents.events.activity.LaunchTaskStartedEvent;
import com.android.systemui.recents.events.activity.MultiWindowStateChangedEvent;
import com.android.systemui.recents.events.activity.PackagesChangedEvent;
import com.android.systemui.recents.events.activity.ShowStackActionButtonEvent;
import com.android.systemui.recents.events.ui.AllTaskViewsDismissedEvent;
import com.android.systemui.recents.events.ui.DeleteTaskDataEvent;
import com.android.systemui.recents.events.ui.DismissAllTaskViewsEvent;
import com.android.systemui.recents.events.ui.DismissTaskViewEvent;
import com.android.systemui.recents.events.ui.RecentsGrowingEvent;
import com.android.systemui.recents.events.ui.TaskViewDismissedEvent;
import com.android.systemui.recents.events.ui.UpdateFreeformTaskViewVisibilityEvent;
import com.android.systemui.recents.events.ui.UserInteractionEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragDropTargetChangedEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragEndCancelledEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragEndEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragStartEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragStartInitializeDropTargetsEvent;
import com.android.systemui.recents.events.ui.focus.DismissFocusedTaskViewEvent;
import com.android.systemui.recents.events.ui.focus.FocusNextTaskViewEvent;
import com.android.systemui.recents.events.ui.focus.FocusPreviousTaskViewEvent;
import com.android.systemui.recents.misc.DozeTrigger;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.misc.Utilities;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.model.TaskStack;
import com.android.systemui.recents.views.TaskStackLayoutAlgorithm;
import com.android.systemui.recents.views.TaskStackViewScroller;
import com.android.systemui.recents.views.TaskView;
import com.android.systemui.recents.views.ViewPool;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
/* loaded from: a.zip:com/android/systemui/recents/views/TaskStackView.class */
public class TaskStackView extends FrameLayout implements TaskStack.TaskStackCallbacks, TaskView.TaskViewCallbacks, TaskStackViewScroller.TaskStackViewScrollerCallbacks, TaskStackLayoutAlgorithm.TaskStackLayoutAlgorithmCallbacks, ViewPool.ViewPoolConsumer<TaskView, Task> {
    private TaskStackAnimationHelper mAnimationHelper;
    @ViewDebug.ExportedProperty(category = "recents")
    private boolean mAwaitingFirstLayout;
    private ArrayList<TaskViewTransform> mCurrentTaskTransforms;
    private AnimationProps mDeferredTaskViewLayoutAnimation;
    @ViewDebug.ExportedProperty(category = "recents")
    private int mDisplayOrientation;
    @ViewDebug.ExportedProperty(category = "recents")
    private Rect mDisplayRect;
    private int mDividerSize;
    @ViewDebug.ExportedProperty(category = "recents")
    private boolean mEnterAnimationComplete;
    @ViewDebug.ExportedProperty(deepExport = true, prefix = "focused_task_")
    private Task mFocusedTask;
    private GradientDrawable mFreeformWorkspaceBackground;
    private ObjectAnimator mFreeformWorkspaceBackgroundAnimator;
    private DropTarget mFreeformWorkspaceDropTarget;
    private ArraySet<Task.TaskKey> mIgnoreTasks;
    @ViewDebug.ExportedProperty(category = "recents")
    private boolean mInMeasureLayout;
    private LayoutInflater mInflater;
    @ViewDebug.ExportedProperty(category = "recents")
    private int mInitialState;
    private int mLastHeight;
    private int mLastWidth;
    @ViewDebug.ExportedProperty(deepExport = true, prefix = "layout_")
    TaskStackLayoutAlgorithm mLayoutAlgorithm;
    private ValueAnimator.AnimatorUpdateListener mRequestUpdateClippingListener;
    private boolean mResetToInitialStateWhenResized;
    @ViewDebug.ExportedProperty(category = "recents")
    boolean mScreenPinningEnabled;
    private TaskStackLayoutAlgorithm mStableLayoutAlgorithm;
    @ViewDebug.ExportedProperty(category = "recents")
    private Rect mStableStackBounds;
    @ViewDebug.ExportedProperty(category = "recents")
    private Rect mStableWindowRect;
    private TaskStack mStack;
    @ViewDebug.ExportedProperty(category = "recents")
    private Rect mStackBounds;
    private DropTarget mStackDropTarget;
    @ViewDebug.ExportedProperty(deepExport = true, prefix = "scroller_")
    private TaskStackViewScroller mStackScroller;
    private int mStartTimerIndicatorDuration;
    private int mTaskCornerRadiusPx;
    private ArrayList<TaskView> mTaskViews;
    @ViewDebug.ExportedProperty(category = "recents")
    private boolean mTaskViewsClipDirty;
    private int[] mTmpIntPair;
    private Rect mTmpRect;
    private ArrayMap<Task.TaskKey, TaskView> mTmpTaskViewMap;
    private List<TaskView> mTmpTaskViews;
    private TaskViewTransform mTmpTransform;
    @ViewDebug.ExportedProperty(category = "recents")
    boolean mTouchExplorationEnabled;
    @ViewDebug.ExportedProperty(deepExport = true, prefix = "touch_")
    private TaskStackViewTouchHandler mTouchHandler;
    @ViewDebug.ExportedProperty(deepExport = true, prefix = "doze_")
    private DozeTrigger mUIDozeTrigger;
    private ViewPool<TaskView, Task> mViewPool;
    @ViewDebug.ExportedProperty(category = "recents")
    private Rect mWindowRect;

    @IntDef({0, 1, 2})
    @Retention(RetentionPolicy.SOURCE)
    /* loaded from: a.zip:com/android/systemui/recents/views/TaskStackView$InitialStateAction.class */
    public @interface InitialStateAction {
    }

    public TaskStackView(Context context) {
        super(context);
        this.mStack = new TaskStack();
        this.mTaskViews = new ArrayList<>();
        this.mCurrentTaskTransforms = new ArrayList<>();
        this.mIgnoreTasks = new ArraySet<>();
        this.mDeferredTaskViewLayoutAnimation = null;
        this.mTaskViewsClipDirty = true;
        this.mAwaitingFirstLayout = true;
        this.mInitialState = 1;
        this.mInMeasureLayout = false;
        this.mEnterAnimationComplete = false;
        this.mStableStackBounds = new Rect();
        this.mStackBounds = new Rect();
        this.mStableWindowRect = new Rect();
        this.mWindowRect = new Rect();
        this.mDisplayRect = new Rect();
        this.mDisplayOrientation = 0;
        this.mTmpRect = new Rect();
        this.mTmpTaskViewMap = new ArrayMap<>();
        this.mTmpTaskViews = new ArrayList();
        this.mTmpTransform = new TaskViewTransform();
        this.mTmpIntPair = new int[2];
        this.mRequestUpdateClippingListener = new ValueAnimator.AnimatorUpdateListener(this) { // from class: com.android.systemui.recents.views.TaskStackView.1
            final TaskStackView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (this.this$0.mTaskViewsClipDirty) {
                    return;
                }
                this.this$0.mTaskViewsClipDirty = true;
                this.this$0.invalidate();
            }
        };
        this.mFreeformWorkspaceDropTarget = new DropTarget(this) { // from class: com.android.systemui.recents.views.TaskStackView.2
            final TaskStackView this$0;

            {
                this.this$0 = this;
            }

            @Override // com.android.systemui.recents.views.DropTarget
            public boolean acceptsDrop(int i, int i2, int i3, int i4, boolean z) {
                if (z) {
                    return false;
                }
                return this.this$0.mLayoutAlgorithm.mFreeformRect.contains(i, i2);
            }
        };
        this.mStackDropTarget = new DropTarget(this) { // from class: com.android.systemui.recents.views.TaskStackView.3
            final TaskStackView this$0;

            {
                this.this$0 = this;
            }

            @Override // com.android.systemui.recents.views.DropTarget
            public boolean acceptsDrop(int i, int i2, int i3, int i4, boolean z) {
                if (z) {
                    return false;
                }
                return this.this$0.mLayoutAlgorithm.mStackRect.contains(i, i2);
            }
        };
        SystemServicesProxy systemServices = Recents.getSystemServices();
        Resources resources = context.getResources();
        this.mStack.setCallbacks(this);
        this.mViewPool = new ViewPool<>(context, this);
        this.mInflater = LayoutInflater.from(context);
        this.mLayoutAlgorithm = new TaskStackLayoutAlgorithm(context, this);
        this.mStableLayoutAlgorithm = new TaskStackLayoutAlgorithm(context, null);
        this.mStackScroller = new TaskStackViewScroller(context, this, this.mLayoutAlgorithm);
        this.mTouchHandler = new TaskStackViewTouchHandler(context, this, this.mStackScroller);
        this.mAnimationHelper = new TaskStackAnimationHelper(context, this);
        this.mTaskCornerRadiusPx = resources.getDimensionPixelSize(2131690016);
        this.mDividerSize = systemServices.getDockedDividerSize(context);
        this.mDisplayOrientation = Utilities.getAppConfiguration(this.mContext).orientation;
        this.mDisplayRect = systemServices.getDisplayRect();
        this.mUIDozeTrigger = new DozeTrigger(getResources().getInteger(2131755061), new Runnable(this) { // from class: com.android.systemui.recents.views.TaskStackView.4
            final TaskStackView this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                List<TaskView> taskViews = this.this$0.getTaskViews();
                int size = taskViews.size();
                for (int i = 0; i < size; i++) {
                    taskViews.get(i).startNoUserInteractionAnimation();
                }
            }
        });
        setImportantForAccessibility(1);
        this.mFreeformWorkspaceBackground = (GradientDrawable) getContext().getDrawable(2130837981);
        this.mFreeformWorkspaceBackground.setCallback(this);
        if (systemServices.hasFreeformWorkspaceSupport()) {
            this.mFreeformWorkspaceBackground.setColor(getContext().getColor(2131558546));
        }
    }

    private void animateFreeformWorkspaceBackgroundAlpha(int i, AnimationProps animationProps) {
        if (this.mFreeformWorkspaceBackground.getAlpha() == i) {
            return;
        }
        Utilities.cancelAnimationWithoutCallbacks(this.mFreeformWorkspaceBackgroundAnimator);
        this.mFreeformWorkspaceBackgroundAnimator = ObjectAnimator.ofInt(this.mFreeformWorkspaceBackground, (Property<GradientDrawable, Integer>) Utilities.DRAWABLE_ALPHA, this.mFreeformWorkspaceBackground.getAlpha(), i);
        this.mFreeformWorkspaceBackgroundAnimator.setStartDelay(animationProps.getDuration(4));
        this.mFreeformWorkspaceBackgroundAnimator.setDuration(animationProps.getDuration(4));
        this.mFreeformWorkspaceBackgroundAnimator.setInterpolator(animationProps.getInterpolator(4));
        this.mFreeformWorkspaceBackgroundAnimator.start();
    }

    private void bindTaskView(TaskView taskView, Task task) {
        taskView.onTaskBound(task, this.mTouchExplorationEnabled, this.mDisplayOrientation, this.mDisplayRect);
        Recents.getTaskLoader().loadTaskData(task);
    }

    private void clipTaskViews() {
        TaskView taskView;
        List<TaskView> taskViews = getTaskViews();
        TaskView taskView2 = null;
        int size = taskViews.size();
        for (int i = 0; i < size; i++) {
            TaskView taskView3 = taskViews.get(i);
            if (isIgnoredTask(taskView3.getTask()) && taskView2 != null) {
                taskView3.setTranslationZ(Math.max(taskView3.getTranslationZ(), taskView2.getTranslationZ() + 0.1f));
            }
            int i2 = 0;
            if (i < size - 1) {
                i2 = 0;
                if (taskView3.shouldClipViewInStack()) {
                    int i3 = i + 1;
                    while (true) {
                        taskView = null;
                        if (i3 >= size) {
                            break;
                        }
                        taskView = taskViews.get(i3);
                        if (taskView.shouldClipViewInStack()) {
                            break;
                        }
                        i3++;
                    }
                    i2 = 0;
                    if (taskView != null) {
                        float bottom = taskView3.getBottom();
                        float top = taskView.getTop();
                        i2 = 0;
                        if (top < bottom) {
                            i2 = ((int) (bottom - top)) - this.mTaskCornerRadiusPx;
                        }
                    }
                }
            }
            taskView3.getViewBounds().setClipBottom(i2);
            taskView3.mThumbnailView.updateThumbnailVisibility(i2 - taskView3.getPaddingBottom());
            taskView2 = taskView3;
        }
        this.mTaskViewsClipDirty = false;
    }

    private int findTaskViewInsertIndex(Task task, int i) {
        boolean z;
        if (i != -1) {
            List<TaskView> taskViews = getTaskViews();
            boolean z2 = false;
            int size = taskViews.size();
            int i2 = 0;
            while (i2 < size) {
                Task task2 = taskViews.get(i2).getTask();
                if (task2 == task) {
                    z = true;
                } else {
                    z = z2;
                    if (i < this.mStack.indexOfStackTask(task2)) {
                        return z2 ? i2 - 1 : i2;
                    }
                }
                i2++;
                z2 = z;
            }
            return -1;
        }
        return -1;
    }

    private TaskView getFrontMostTaskView(boolean z) {
        List<TaskView> taskViews = getTaskViews();
        for (int size = taskViews.size() - 1; size >= 0; size--) {
            TaskView taskView = taskViews.get(size);
            Task task = taskView.getTask();
            if (!z || !task.isFreeformTask()) {
                return taskView;
            }
        }
        return null;
    }

    private void layoutTaskView(boolean z, TaskView taskView) {
        if (!z) {
            taskView.layout(taskView.getLeft(), taskView.getTop(), taskView.getRight(), taskView.getBottom());
            return;
        }
        Rect rect = new Rect();
        if (taskView.getBackground() != null) {
            taskView.getBackground().getPadding(rect);
        }
        this.mTmpRect.set(this.mStableLayoutAlgorithm.mTaskRect);
        this.mTmpRect.union(this.mLayoutAlgorithm.mTaskRect);
        taskView.cancelTransformAnimation();
        taskView.layout(this.mTmpRect.left - rect.left, this.mTmpRect.top - rect.top, this.mTmpRect.right + rect.right, this.mTmpRect.bottom + rect.bottom);
    }

    private void measureTaskView(TaskView taskView) {
        Rect rect = new Rect();
        if (taskView.getBackground() != null) {
            taskView.getBackground().getPadding(rect);
        }
        this.mTmpRect.set(this.mStableLayoutAlgorithm.mTaskRect);
        this.mTmpRect.union(this.mLayoutAlgorithm.mTaskRect);
        taskView.measure(View.MeasureSpec.makeMeasureSpec(this.mTmpRect.width() + rect.left + rect.right, 1073741824), View.MeasureSpec.makeMeasureSpec(this.mTmpRect.height() + rect.top + rect.bottom, 1073741824));
    }

    private void readSystemFlags() {
        boolean z = false;
        SystemServicesProxy systemServices = Recents.getSystemServices();
        this.mTouchExplorationEnabled = systemServices.isTouchExplorationEnabled();
        if (systemServices.getSystemSetting(getContext(), "lock_to_app_enabled") != 0) {
            z = true;
        }
        this.mScreenPinningEnabled = z;
    }

    private void relayoutTaskViews(AnimationProps animationProps, ArrayMap<Task, AnimationProps> arrayMap, boolean z) {
        AnimationProps animationProps2;
        cancelDeferredTaskViewLayoutAnimation();
        bindVisibleTaskViews(this.mStackScroller.getStackScroll(), z);
        List<TaskView> taskViews = getTaskViews();
        int size = taskViews.size();
        int i = 0;
        while (i < size) {
            TaskView taskView = taskViews.get(i);
            Task task = taskView.getTask();
            int indexOfStackTask = this.mStack.indexOfStackTask(task);
            if (indexOfStackTask == -1) {
                animationProps2 = animationProps;
            } else {
                TaskViewTransform taskViewTransform = this.mCurrentTaskTransforms.get(indexOfStackTask);
                animationProps2 = animationProps;
                if (!this.mIgnoreTasks.contains(task.key)) {
                    animationProps2 = animationProps;
                    if (arrayMap != null) {
                        animationProps2 = animationProps;
                        if (arrayMap.containsKey(task)) {
                            animationProps2 = arrayMap.get(task);
                        }
                    }
                    updateTaskViewToTransform(taskView, taskViewTransform, animationProps2);
                }
            }
            i++;
            animationProps = animationProps2;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean setFocusedTask(int i, boolean z, boolean z2) {
        return setFocusedTask(i, z, z2, 0);
    }

    private boolean setFocusedTask(int i, boolean z, boolean z2, int i2) {
        TaskView childViewForTask;
        int clamp = this.mStack.getTaskCount() > 0 ? Utilities.clamp(i, 0, this.mStack.getTaskCount() - 1) : -1;
        Task task = clamp != -1 ? this.mStack.getStackTasks().get(clamp) : null;
        if (this.mFocusedTask != null) {
            if (i2 > 0 && (childViewForTask = getChildViewForTask(this.mFocusedTask)) != null) {
                childViewForTask.getHeaderView().cancelFocusTimerIndicator();
            }
            resetFocusedTask(this.mFocusedTask);
        }
        this.mFocusedTask = task;
        boolean z3 = false;
        if (task != null) {
            if (i2 > 0) {
                TaskView childViewForTask2 = getChildViewForTask(this.mFocusedTask);
                if (childViewForTask2 != null) {
                    childViewForTask2.getHeaderView().startFocusTimerIndicator(i2);
                } else {
                    this.mStartTimerIndicatorDuration = i2;
                }
            }
            if (z) {
                if (!this.mEnterAnimationComplete) {
                    cancelAllTaskViewAnimations();
                }
                this.mLayoutAlgorithm.clearUnfocusedTaskOverrides();
                z3 = this.mAnimationHelper.startScrollToFocusedTaskAnimation(task, z2);
            } else {
                TaskView childViewForTask3 = getChildViewForTask(task);
                z3 = false;
                if (childViewForTask3 != null) {
                    childViewForTask3.setFocusedState(true, z2);
                    z3 = false;
                }
            }
        }
        return z3;
    }

    private void unbindTaskView(TaskView taskView, Task task) {
        Recents.getTaskLoader().unloadTaskData(task);
    }

    private void updateLayoutToStableBounds() {
        this.mWindowRect.set(this.mStableWindowRect);
        this.mStackBounds.set(this.mStableStackBounds);
        this.mLayoutAlgorithm.setSystemInsets(this.mStableLayoutAlgorithm.mSystemInsets);
        this.mLayoutAlgorithm.initialize(this.mDisplayRect, this.mWindowRect, this.mStackBounds, TaskStackLayoutAlgorithm.StackState.getStackStateForStack(this.mStack));
        updateLayoutAlgorithm(true);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void addIgnoreTask(Task task) {
        this.mIgnoreTasks.add(task.key);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void bindVisibleTaskViews(float f) {
        bindVisibleTaskViews(f, false);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void bindVisibleTaskViews(float f, boolean z) {
        ArrayList<Task> stackTasks = this.mStack.getStackTasks();
        int[] computeVisibleTaskTransforms = computeVisibleTaskTransforms(this.mCurrentTaskTransforms, stackTasks, this.mStackScroller.getStackScroll(), f, this.mIgnoreTasks, z);
        this.mTmpTaskViewMap.clear();
        List<TaskView> taskViews = getTaskViews();
        int i = -1;
        for (int size = taskViews.size() - 1; size >= 0; size--) {
            TaskView taskView = taskViews.get(size);
            Task task = taskView.getTask();
            if (!this.mIgnoreTasks.contains(task.key)) {
                int indexOfStackTask = this.mStack.indexOfStackTask(task);
                TaskViewTransform taskViewTransform = indexOfStackTask != -1 ? this.mCurrentTaskTransforms.get(indexOfStackTask) : null;
                if (task.isFreeformTask() || (taskViewTransform != null && taskViewTransform.visible)) {
                    this.mTmpTaskViewMap.put(task.key, taskView);
                } else {
                    int i2 = i;
                    if (this.mTouchExplorationEnabled) {
                        i2 = i;
                        if (Utilities.isDescendentAccessibilityFocused(taskView)) {
                            i2 = indexOfStackTask;
                            resetFocusedTask(task);
                        }
                    }
                    this.mViewPool.returnViewToPool(taskView);
                    i = i2;
                }
            }
        }
        for (int size2 = stackTasks.size() - 1; size2 >= 0; size2--) {
            Task task2 = stackTasks.get(size2);
            TaskViewTransform taskViewTransform2 = this.mCurrentTaskTransforms.get(size2);
            if (!this.mIgnoreTasks.contains(task2.key) && (task2.isFreeformTask() || taskViewTransform2.visible)) {
                TaskView taskView2 = this.mTmpTaskViewMap.get(task2.key);
                if (taskView2 == null) {
                    TaskView pickUpViewFromPool = this.mViewPool.pickUpViewFromPool(task2, task2);
                    if (task2.isFreeformTask()) {
                        updateTaskViewToTransform(pickUpViewFromPool, taskViewTransform2, AnimationProps.IMMEDIATE);
                    } else if (taskViewTransform2.rect.top <= this.mLayoutAlgorithm.mStackRect.top) {
                        updateTaskViewToTransform(pickUpViewFromPool, this.mLayoutAlgorithm.getBackOfStackTransform(), AnimationProps.IMMEDIATE);
                    } else {
                        updateTaskViewToTransform(pickUpViewFromPool, this.mLayoutAlgorithm.getFrontOfStackTransform(), AnimationProps.IMMEDIATE);
                    }
                } else {
                    int findTaskViewInsertIndex = findTaskViewInsertIndex(task2, this.mStack.indexOfStackTask(task2));
                    if (findTaskViewInsertIndex != getTaskViews().indexOf(taskView2)) {
                        detachViewFromParent(taskView2);
                        attachViewToParent(taskView2, findTaskViewInsertIndex, taskView2.getLayoutParams());
                        updateTaskViewsList();
                    }
                }
            }
        }
        if (i != -1) {
            setFocusedTask(i < computeVisibleTaskTransforms[1] ? computeVisibleTaskTransforms[1] : computeVisibleTaskTransforms[0], false, true);
            TaskView childViewForTask = getChildViewForTask(this.mFocusedTask);
            if (childViewForTask != null) {
                childViewForTask.requestAccessibilityFocus();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void cancelAllTaskViewAnimations() {
        List<TaskView> taskViews = getTaskViews();
        for (int size = taskViews.size() - 1; size >= 0; size--) {
            TaskView taskView = taskViews.get(size);
            if (!this.mIgnoreTasks.contains(taskView.getTask().key)) {
                taskView.cancelTransformAnimation();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void cancelDeferredTaskViewLayoutAnimation() {
        this.mDeferredTaskViewLayoutAnimation = null;
    }

    @Override // android.view.View
    public void computeScroll() {
        if (this.mStackScroller.computeScroll()) {
            sendAccessibilityEvent(4096);
        }
        if (this.mDeferredTaskViewLayoutAnimation != null) {
            relayoutTaskViews(this.mDeferredTaskViewLayoutAnimation);
            this.mTaskViewsClipDirty = true;
            this.mDeferredTaskViewLayoutAnimation = null;
        }
        if (this.mTaskViewsClipDirty) {
            clipTaskViews();
        }
    }

    public TaskStackLayoutAlgorithm.VisibilityReport computeStackVisibilityReport() {
        return this.mLayoutAlgorithm.computeStackVisibilityReport(this.mStack.getStackTasks());
    }

    int[] computeVisibleTaskTransforms(ArrayList<TaskViewTransform> arrayList, ArrayList<Task> arrayList2, float f, float f2, ArraySet<Task.TaskKey> arraySet, boolean z) {
        int size = arrayList2.size();
        int[] iArr = this.mTmpIntPair;
        iArr[0] = -1;
        iArr[1] = -1;
        boolean z2 = Float.compare(f, f2) != 0;
        Utilities.matchTaskListSize(arrayList2, arrayList);
        TaskViewTransform taskViewTransform = null;
        TaskViewTransform taskViewTransform2 = null;
        TaskViewTransform taskViewTransform3 = null;
        int i = size - 1;
        while (i >= 0) {
            Task task = arrayList2.get(i);
            TaskViewTransform stackTransform = this.mLayoutAlgorithm.getStackTransform(task, f, arrayList.get(i), taskViewTransform, z);
            TaskViewTransform taskViewTransform4 = taskViewTransform3;
            if (z2) {
                if (stackTransform.visible) {
                    taskViewTransform4 = taskViewTransform3;
                } else {
                    TaskViewTransform stackTransform2 = this.mLayoutAlgorithm.getStackTransform(task, f2, new TaskViewTransform(), taskViewTransform2);
                    taskViewTransform4 = stackTransform2;
                    if (stackTransform2.visible) {
                        stackTransform.copyFrom(stackTransform2);
                        taskViewTransform4 = stackTransform2;
                    }
                }
            }
            if (!arraySet.contains(task.key) && !task.isFreeformTask()) {
                TaskViewTransform taskViewTransform5 = taskViewTransform4;
                taskViewTransform = stackTransform;
                taskViewTransform2 = taskViewTransform5;
                if (stackTransform.visible) {
                    if (iArr[0] < 0) {
                        iArr[0] = i;
                    }
                    iArr[1] = i;
                    taskViewTransform = stackTransform;
                    taskViewTransform2 = taskViewTransform5;
                }
            }
            i--;
            taskViewTransform3 = taskViewTransform4;
        }
        return iArr;
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // com.android.systemui.recents.views.ViewPool.ViewPoolConsumer
    public TaskView createView(Context context) {
        return (TaskView) this.mInflater.inflate(2130968782, (ViewGroup) this, false);
    }

    public void dump(String str, PrintWriter printWriter) {
        String str2 = str + "  ";
        String hexString = Integer.toHexString(System.identityHashCode(this));
        printWriter.print(str);
        printWriter.print("TaskStackView");
        printWriter.print(" hasDefRelayout=");
        printWriter.print(this.mDeferredTaskViewLayoutAnimation != null ? "Y" : "N");
        printWriter.print(" clipDirty=");
        printWriter.print(this.mTaskViewsClipDirty ? "Y" : "N");
        printWriter.print(" awaitingFirstLayout=");
        printWriter.print(this.mAwaitingFirstLayout ? "Y" : "N");
        printWriter.print(" initialState=");
        printWriter.print(this.mInitialState);
        printWriter.print(" inMeasureLayout=");
        printWriter.print(this.mInMeasureLayout ? "Y" : "N");
        printWriter.print(" enterAnimCompleted=");
        printWriter.print(this.mEnterAnimationComplete ? "Y" : "N");
        printWriter.print(" touchExplorationOn=");
        printWriter.print(this.mTouchExplorationEnabled ? "Y" : "N");
        printWriter.print(" screenPinningOn=");
        printWriter.print(this.mScreenPinningEnabled ? "Y" : "N");
        printWriter.print(" numIgnoreTasks=");
        printWriter.print(this.mIgnoreTasks.size());
        printWriter.print(" numViewPool=");
        printWriter.print(this.mViewPool.getViews().size());
        printWriter.print(" stableStackBounds=");
        printWriter.print(Utilities.dumpRect(this.mStableStackBounds));
        printWriter.print(" stackBounds=");
        printWriter.print(Utilities.dumpRect(this.mStackBounds));
        printWriter.print(" stableWindow=");
        printWriter.print(Utilities.dumpRect(this.mStableWindowRect));
        printWriter.print(" window=");
        printWriter.print(Utilities.dumpRect(this.mWindowRect));
        printWriter.print(" display=");
        printWriter.print(Utilities.dumpRect(this.mDisplayRect));
        printWriter.print(" orientation=");
        printWriter.print(this.mDisplayOrientation);
        printWriter.print(" [0x");
        printWriter.print(hexString);
        printWriter.print("]");
        printWriter.println();
        if (this.mFocusedTask != null) {
            printWriter.print(str2);
            printWriter.print("Focused task: ");
            this.mFocusedTask.dump("", printWriter);
        }
        this.mLayoutAlgorithm.dump(str2, printWriter);
        this.mStackScroller.dump(str2, printWriter);
    }

    public Task findAnchorTask(List<Task> list, MutableBoolean mutableBoolean) {
        for (int size = list.size() - 1; size >= 0; size--) {
            Task task = list.get(size);
            if (!isIgnoredTask(task)) {
                return task;
            }
            if (size == list.size() - 1) {
                mutableBoolean.value = true;
            }
        }
        return null;
    }

    @Override // android.widget.FrameLayout, android.view.ViewGroup, android.view.View
    public CharSequence getAccessibilityClassName() {
        return ScrollView.class.getName();
    }

    Task getAccessibilityFocusedTask() {
        List<TaskView> taskViews = getTaskViews();
        int size = taskViews.size();
        for (int i = 0; i < size; i++) {
            TaskView taskView = taskViews.get(i);
            if (Utilities.isDescendentAccessibilityFocused(taskView)) {
                return taskView.getTask();
            }
        }
        TaskView frontMostTaskView = getFrontMostTaskView(true);
        if (frontMostTaskView != null) {
            return frontMostTaskView.getTask();
        }
        return null;
    }

    public TaskView getChildViewForTask(Task task) {
        List<TaskView> taskViews = getTaskViews();
        int size = taskViews.size();
        for (int i = 0; i < size; i++) {
            TaskView taskView = taskViews.get(i);
            if (taskView.getTask() == task) {
                return taskView;
            }
        }
        return null;
    }

    public void getCurrentTaskTransforms(ArrayList<Task> arrayList, ArrayList<TaskViewTransform> arrayList2) {
        Utilities.matchTaskListSize(arrayList, arrayList2);
        int focusState = this.mLayoutAlgorithm.getFocusState();
        for (int size = arrayList.size() - 1; size >= 0; size--) {
            Task task = arrayList.get(size);
            TaskViewTransform taskViewTransform = arrayList2.get(size);
            TaskView childViewForTask = getChildViewForTask(task);
            if (childViewForTask != null) {
                taskViewTransform.fillIn(childViewForTask);
            } else {
                this.mLayoutAlgorithm.getStackTransform(task, this.mStackScroller.getStackScroll(), focusState, taskViewTransform, null, true, false);
            }
            taskViewTransform.visible = true;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public Task getFocusedTask() {
        return this.mFocusedTask;
    }

    public void getLayoutTaskTransforms(float f, int i, ArrayList<Task> arrayList, boolean z, ArrayList<TaskViewTransform> arrayList2) {
        Utilities.matchTaskListSize(arrayList, arrayList2);
        for (int size = arrayList.size() - 1; size >= 0; size--) {
            Task task = arrayList.get(size);
            TaskViewTransform taskViewTransform = arrayList2.get(size);
            this.mLayoutAlgorithm.getStackTransform(task, f, i, taskViewTransform, null, true, z);
            taskViewTransform.visible = true;
        }
    }

    public TaskStackViewScroller getScroller() {
        return this.mStackScroller;
    }

    public TaskStack getStack() {
        return this.mStack;
    }

    public TaskStackLayoutAlgorithm getStackAlgorithm() {
        return this.mLayoutAlgorithm;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public List<TaskView> getTaskViews() {
        return this.mTaskViews;
    }

    public TaskStackViewTouchHandler getTouchHandler() {
        return this.mTouchHandler;
    }

    @Override // com.android.systemui.recents.views.ViewPool.ViewPoolConsumer
    public boolean hasPreferredData(TaskView taskView, Task task) {
        return taskView.getTask() == task;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean isIgnoredTask(Task task) {
        return this.mIgnoreTasks.contains(task.key);
    }

    public boolean isTouchPointInView(float f, float f2, TaskView taskView) {
        this.mTmpRect.set(taskView.getLeft(), taskView.getTop(), taskView.getRight(), taskView.getBottom());
        this.mTmpRect.offset((int) taskView.getTranslationX(), (int) taskView.getTranslationY());
        return this.mTmpRect.contains((int) f, (int) f2);
    }

    public boolean launchFreeformTasks() {
        Task task;
        ArrayList<Task> freeformTasks = this.mStack.getFreeformTasks();
        if (freeformTasks.isEmpty() || (task = freeformTasks.get(freeformTasks.size() - 1)) == null || !task.isFreeformTask()) {
            return false;
        }
        EventBus.getDefault().send(new LaunchTaskEvent(getChildViewForTask(task), task, null, -1, false));
        return true;
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        EventBus.getDefault().register(this, 3);
        super.onAttachedToWindow();
        readSystemFlags();
    }

    public final void onBusEvent(ConfigurationChangedEvent configurationChangedEvent) {
        if (configurationChangedEvent.fromDeviceOrientationChange) {
            this.mDisplayOrientation = Utilities.getAppConfiguration(this.mContext).orientation;
            this.mDisplayRect = Recents.getSystemServices().getDisplayRect();
            this.mStackScroller.stopScroller();
        }
        reloadOnConfigurationChange();
        if (!configurationChangedEvent.fromMultiWindow) {
            this.mTmpTaskViews.clear();
            this.mTmpTaskViews.addAll(getTaskViews());
            this.mTmpTaskViews.addAll(this.mViewPool.getViews());
            int size = this.mTmpTaskViews.size();
            for (int i = 0; i < size; i++) {
                this.mTmpTaskViews.get(i).onConfigurationChanged();
            }
        }
        if (configurationChangedEvent.fromMultiWindow) {
            this.mInitialState = 2;
            requestLayout();
        } else if (configurationChangedEvent.fromDeviceOrientationChange) {
            this.mInitialState = 1;
            requestLayout();
        }
    }

    public final void onBusEvent(DismissRecentsToHomeAnimationStarted dismissRecentsToHomeAnimationStarted) {
        this.mTouchHandler.cancelNonDismissTaskAnimations();
        this.mStackScroller.stopScroller();
        this.mStackScroller.stopBoundScrollAnimation();
        cancelDeferredTaskViewLayoutAnimation();
        this.mAnimationHelper.startExitToHomeAnimation(dismissRecentsToHomeAnimationStarted.animated, dismissRecentsToHomeAnimationStarted.getAnimationTrigger());
        animateFreeformWorkspaceBackgroundAlpha(0, new AnimationProps(200, Interpolators.FAST_OUT_SLOW_IN));
    }

    public final void onBusEvent(EnterRecentsWindowAnimationCompletedEvent enterRecentsWindowAnimationCompletedEvent) {
        this.mEnterAnimationComplete = true;
        if (this.mStack.getTaskCount() > 0) {
            this.mAnimationHelper.startEnterAnimation(enterRecentsWindowAnimationCompletedEvent.getAnimationTrigger());
            enterRecentsWindowAnimationCompletedEvent.addPostAnimationCallback(new Runnable(this) { // from class: com.android.systemui.recents.views.TaskStackView.8
                final TaskStackView this$0;

                {
                    this.this$0 = this;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$0.mUIDozeTrigger.startDozing();
                    if (this.this$0.mFocusedTask != null) {
                        this.this$0.setFocusedTask(this.this$0.mStack.indexOfStackTask(this.this$0.mFocusedTask), false, Recents.getConfiguration().getLaunchState().launchedWithAltTab);
                        TaskView childViewForTask = this.this$0.getChildViewForTask(this.this$0.mFocusedTask);
                        if (this.this$0.mTouchExplorationEnabled && childViewForTask != null) {
                            childViewForTask.requestAccessibilityFocus();
                        }
                    }
                    EventBus.getDefault().send(new EnterRecentsTaskStackAnimationCompletedEvent());
                }
            });
        }
    }

    public final void onBusEvent(IterateRecentsEvent iterateRecentsEvent) {
        if (this.mEnterAnimationComplete) {
            return;
        }
        EventBus.getDefault().send(new CancelEnterRecentsWindowAnimationEvent(null));
    }

    public final void onBusEvent(LaunchNextTaskRequestEvent launchNextTaskRequestEvent) {
        int indexOfStackTask = this.mStack.indexOfStackTask(this.mStack.getLaunchTarget());
        int max = indexOfStackTask != -1 ? Math.max(0, indexOfStackTask - 1) : this.mStack.getTaskCount() - 1;
        if (max == -1) {
            if (this.mStack.getTaskCount() == 0) {
                EventBus.getDefault().send(new HideRecentsEvent(false, true));
                return;
            }
            return;
        }
        cancelAllTaskViewAnimations();
        Task task = this.mStack.getStackTasks().get(max);
        float stackScroll = this.mStackScroller.getStackScroll();
        float stackScrollForTaskAtInitialOffset = this.mLayoutAlgorithm.getStackScrollForTaskAtInitialOffset(task);
        float abs = Math.abs(stackScrollForTaskAtInitialOffset - stackScroll);
        if (getChildViewForTask(task) == null || abs > 0.35f) {
            this.mStackScroller.animateScroll(stackScrollForTaskAtInitialOffset, (int) ((32.0f * abs) + 216.0f), new Runnable(this, task) { // from class: com.android.systemui.recents.views.TaskStackView.5
                final TaskStackView this$0;
                final Task val$launchTask;

                {
                    this.this$0 = this;
                    this.val$launchTask = task;
                }

                @Override // java.lang.Runnable
                public void run() {
                    EventBus.getDefault().send(new LaunchTaskEvent(this.this$0.getChildViewForTask(this.val$launchTask), this.val$launchTask, null, -1, false));
                }
            });
        } else {
            EventBus.getDefault().send(new LaunchTaskEvent(getChildViewForTask(task), task, null, -1, false));
        }
        MetricsLogger.action(getContext(), 318, task.key.getComponent().toString());
    }

    public final void onBusEvent(LaunchTaskEvent launchTaskEvent) {
        this.mUIDozeTrigger.stopDozing();
    }

    public final void onBusEvent(LaunchTaskStartedEvent launchTaskStartedEvent) {
        this.mAnimationHelper.startLaunchTaskAnimation(launchTaskStartedEvent.taskView, launchTaskStartedEvent.screenPinningRequested, launchTaskStartedEvent.getAnimationTrigger());
    }

    public final void onBusEvent(MultiWindowStateChangedEvent multiWindowStateChangedEvent) {
        if (multiWindowStateChangedEvent.inMultiWindow || !multiWindowStateChangedEvent.showDeferredAnimation) {
            setTasks(multiWindowStateChangedEvent.stack, true);
            return;
        }
        Recents.getConfiguration().getLaunchState().reset();
        multiWindowStateChangedEvent.getAnimationTrigger().increment();
        post(new Runnable(this, multiWindowStateChangedEvent) { // from class: com.android.systemui.recents.views.TaskStackView.9
            final TaskStackView this$0;
            final MultiWindowStateChangedEvent val$event;

            {
                this.this$0 = this;
                this.val$event = multiWindowStateChangedEvent;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.mAnimationHelper.startNewStackScrollAnimation(this.val$event.stack, this.val$event.getAnimationTrigger());
                this.val$event.getAnimationTrigger().decrement();
            }
        });
    }

    public final void onBusEvent(PackagesChangedEvent packagesChangedEvent) {
        ArraySet<ComponentName> computeComponentsRemoved = this.mStack.computeComponentsRemoved(packagesChangedEvent.packageName, packagesChangedEvent.userId);
        ArrayList<Task> stackTasks = this.mStack.getStackTasks();
        for (int size = stackTasks.size() - 1; size >= 0; size--) {
            Task task = stackTasks.get(size);
            if (computeComponentsRemoved.contains(task.key.getComponent())) {
                TaskView childViewForTask = getChildViewForTask(task);
                if (childViewForTask != null) {
                    childViewForTask.dismissTask();
                } else {
                    this.mStack.removeTask(task, AnimationProps.IMMEDIATE, false);
                }
            }
        }
    }

    public final void onBusEvent(DismissAllTaskViewsEvent dismissAllTaskViewsEvent) {
        ArrayList arrayList = new ArrayList(this.mStack.getStackTasks());
        this.mAnimationHelper.startDeleteAllTasksAnimation(getTaskViews(), dismissAllTaskViewsEvent.getAnimationTrigger());
        dismissAllTaskViewsEvent.addPostAnimationCallback(new Runnable(this, arrayList) { // from class: com.android.systemui.recents.views.TaskStackView.6
            final TaskStackView this$0;
            final ArrayList val$tasks;

            {
                this.this$0 = this;
                this.val$tasks = arrayList;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.announceForAccessibility(this.this$0.getContext().getString(2131493438));
                this.this$0.mStack.removeAllTasks();
                for (int size = this.val$tasks.size() - 1; size >= 0; size--) {
                    EventBus.getDefault().send(new DeleteTaskDataEvent((Task) this.val$tasks.get(size)));
                }
                MetricsLogger.action(this.this$0.getContext(), 357);
            }
        });
    }

    public final void onBusEvent(DismissTaskViewEvent dismissTaskViewEvent) {
        this.mAnimationHelper.startDeleteTaskAnimation(dismissTaskViewEvent.taskView, dismissTaskViewEvent.getAnimationTrigger());
    }

    public final void onBusEvent(RecentsGrowingEvent recentsGrowingEvent) {
        this.mResetToInitialStateWhenResized = true;
    }

    public final void onBusEvent(TaskViewDismissedEvent taskViewDismissedEvent) {
        announceForAccessibility(getContext().getString(2131493437, taskViewDismissedEvent.task.title));
        this.mStack.removeTask(taskViewDismissedEvent.task, taskViewDismissedEvent.animation, false);
        EventBus.getDefault().send(new DeleteTaskDataEvent(taskViewDismissedEvent.task));
        MetricsLogger.action(getContext(), 289, taskViewDismissedEvent.task.key.getComponent().toString());
    }

    public final void onBusEvent(UpdateFreeformTaskViewVisibilityEvent updateFreeformTaskViewVisibilityEvent) {
        List<TaskView> taskViews = getTaskViews();
        int size = taskViews.size();
        for (int i = 0; i < size; i++) {
            TaskView taskView = taskViews.get(i);
            if (taskView.getTask().isFreeformTask()) {
                taskView.setVisibility(updateFreeformTaskViewVisibilityEvent.visible ? 0 : 4);
            }
        }
    }

    public final void onBusEvent(UserInteractionEvent userInteractionEvent) {
        TaskView childViewForTask;
        this.mUIDozeTrigger.poke();
        if (!Recents.getDebugFlags().isFastToggleRecentsEnabled() || this.mFocusedTask == null || (childViewForTask = getChildViewForTask(this.mFocusedTask)) == null) {
            return;
        }
        childViewForTask.getHeaderView().cancelFocusTimerIndicator();
    }

    public final void onBusEvent(DragDropTargetChangedEvent dragDropTargetChangedEvent) {
        AnimationProps animationProps = new AnimationProps(250, Interpolators.FAST_OUT_SLOW_IN);
        boolean z = false;
        if (dragDropTargetChangedEvent.dropTarget instanceof TaskStack.DockState) {
            TaskStack.DockState dockState = (TaskStack.DockState) dragDropTargetChangedEvent.dropTarget;
            Rect rect = new Rect(this.mStableLayoutAlgorithm.mSystemInsets);
            int measuredHeight = getMeasuredHeight();
            int i = rect.bottom;
            rect.bottom = 0;
            this.mStackBounds.set(dockState.getDockedTaskStackBounds(this.mDisplayRect, getMeasuredWidth(), measuredHeight - i, this.mDividerSize, rect, this.mLayoutAlgorithm, getResources(), this.mWindowRect));
            this.mLayoutAlgorithm.setSystemInsets(rect);
            this.mLayoutAlgorithm.initialize(this.mDisplayRect, this.mWindowRect, this.mStackBounds, TaskStackLayoutAlgorithm.StackState.getStackStateForStack(this.mStack));
            updateLayoutAlgorithm(true);
            z = true;
        } else {
            removeIgnoreTask(dragDropTargetChangedEvent.task);
            updateLayoutToStableBounds();
            addIgnoreTask(dragDropTargetChangedEvent.task);
        }
        relayoutTaskViews(animationProps, null, z);
    }

    public final void onBusEvent(DragEndCancelledEvent dragEndCancelledEvent) {
        removeIgnoreTask(dragEndCancelledEvent.task);
        updateLayoutToStableBounds();
        Utilities.setViewFrameFromTranslation(dragEndCancelledEvent.taskView);
        new ArrayMap().put(dragEndCancelledEvent.task, new AnimationProps(250, Interpolators.FAST_OUT_SLOW_IN, dragEndCancelledEvent.getAnimationTrigger().decrementOnAnimationEnd()));
        relayoutTaskViews(new AnimationProps(250, Interpolators.FAST_OUT_SLOW_IN));
        dragEndCancelledEvent.getAnimationTrigger().increment();
    }

    public final void onBusEvent(DragEndEvent dragEndEvent) {
        boolean z;
        if (dragEndEvent.dropTarget instanceof TaskStack.DockState) {
            this.mLayoutAlgorithm.clearUnfocusedTaskOverrides();
            return;
        }
        boolean isFreeformTask = dragEndEvent.task.isFreeformTask();
        if (isFreeformTask || dragEndEvent.dropTarget != this.mFreeformWorkspaceDropTarget) {
            z = false;
            if (isFreeformTask) {
                z = false;
                if (dragEndEvent.dropTarget == this.mStackDropTarget) {
                    z = true;
                }
            }
        } else {
            z = true;
        }
        if (z) {
            if (dragEndEvent.dropTarget == this.mFreeformWorkspaceDropTarget) {
                this.mStack.moveTaskToStack(dragEndEvent.task, 2);
            } else if (dragEndEvent.dropTarget == this.mStackDropTarget) {
                this.mStack.moveTaskToStack(dragEndEvent.task, 1);
            }
            updateLayoutAlgorithm(true);
            dragEndEvent.addPostAnimationCallback(new Runnable(this, dragEndEvent) { // from class: com.android.systemui.recents.views.TaskStackView.7
                final TaskStackView this$0;
                final DragEndEvent val$event;

                {
                    this.this$0 = this;
                    this.val$event = dragEndEvent;
                }

                @Override // java.lang.Runnable
                public void run() {
                    Recents.getSystemServices().moveTaskToStack(this.val$event.task.key.id, this.val$event.task.key.stackId);
                }
            });
        }
        removeIgnoreTask(dragEndEvent.task);
        Utilities.setViewFrameFromTranslation(dragEndEvent.taskView);
        new ArrayMap().put(dragEndEvent.task, new AnimationProps(250, Interpolators.FAST_OUT_SLOW_IN, dragEndEvent.getAnimationTrigger().decrementOnAnimationEnd()));
        relayoutTaskViews(new AnimationProps(250, Interpolators.FAST_OUT_SLOW_IN));
        dragEndEvent.getAnimationTrigger().increment();
    }

    public final void onBusEvent(DragStartEvent dragStartEvent) {
        addIgnoreTask(dragStartEvent.task);
        if (dragStartEvent.task.isFreeformTask()) {
            this.mStackScroller.animateScroll(this.mLayoutAlgorithm.mInitialScrollP, null);
        }
        float scaleX = dragStartEvent.taskView.getScaleX();
        this.mLayoutAlgorithm.getStackTransform(dragStartEvent.task, getScroller().getStackScroll(), this.mTmpTransform, null);
        this.mTmpTransform.scale = scaleX * 1.05f;
        this.mTmpTransform.translationZ = this.mLayoutAlgorithm.mMaxTranslationZ + 1;
        this.mTmpTransform.dimAlpha = 0.0f;
        updateTaskViewToTransform(dragStartEvent.taskView, this.mTmpTransform, new AnimationProps(175, Interpolators.FAST_OUT_SLOW_IN));
    }

    public final void onBusEvent(DragStartInitializeDropTargetsEvent dragStartInitializeDropTargetsEvent) {
        if (Recents.getSystemServices().hasFreeformWorkspaceSupport()) {
            dragStartInitializeDropTargetsEvent.handler.registerDropTargetForCurrentDrag(this.mStackDropTarget);
            dragStartInitializeDropTargetsEvent.handler.registerDropTargetForCurrentDrag(this.mFreeformWorkspaceDropTarget);
        }
    }

    public final void onBusEvent(DismissFocusedTaskViewEvent dismissFocusedTaskViewEvent) {
        if (this.mFocusedTask != null) {
            TaskView childViewForTask = getChildViewForTask(this.mFocusedTask);
            if (childViewForTask != null) {
                childViewForTask.dismissTask();
            }
            resetFocusedTask(this.mFocusedTask);
        }
    }

    public final void onBusEvent(FocusNextTaskViewEvent focusNextTaskViewEvent) {
        this.mStackScroller.stopScroller();
        this.mStackScroller.stopBoundScrollAnimation();
        setRelativeFocusedTask(true, false, true, false, focusNextTaskViewEvent.timerIndicatorDuration);
    }

    public final void onBusEvent(FocusPreviousTaskViewEvent focusPreviousTaskViewEvent) {
        this.mStackScroller.stopScroller();
        this.mStackScroller.stopBoundScrollAnimation();
        setRelativeFocusedTask(false, false, true);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!Recents.getSystemServices().hasFreeformWorkspaceSupport() || this.mFreeformWorkspaceBackground.getAlpha() <= 0) {
            return;
        }
        this.mFreeformWorkspaceBackground.draw(canvas);
    }

    void onFirstLayout() {
        this.mAnimationHelper.prepareForEnterAnimation();
        animateFreeformWorkspaceBackgroundAlpha(this.mLayoutAlgorithm.getStackState().freeformBackgroundAlpha, new AnimationProps(150, Interpolators.FAST_OUT_SLOW_IN));
        int initialFocusTaskIndex = Recents.getConfiguration().getLaunchState().getInitialFocusTaskIndex(this.mStack.getTaskCount());
        if (initialFocusTaskIndex != -1) {
            setFocusedTask(initialFocusTaskIndex, false, false);
        }
        if (this.mStackScroller.getStackScroll() >= 0.3f || this.mStack.getTaskCount() <= 0) {
            EventBus.getDefault().send(new HideStackActionButtonEvent());
        } else {
            EventBus.getDefault().send(new ShowStackActionButtonEvent(false));
        }
    }

    @Override // com.android.systemui.recents.views.TaskStackLayoutAlgorithm.TaskStackLayoutAlgorithmCallbacks
    public void onFocusStateChanged(int i, int i2) {
        if (this.mDeferredTaskViewLayoutAnimation == null) {
            this.mUIDozeTrigger.poke();
            relayoutTaskViewsOnNextFrame(AnimationProps.IMMEDIATE);
        }
    }

    @Override // android.view.View
    public boolean onGenericMotionEvent(MotionEvent motionEvent) {
        return this.mTouchHandler.onGenericMotionEvent(motionEvent);
    }

    @Override // android.view.View
    public void onInitializeAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        super.onInitializeAccessibilityEvent(accessibilityEvent);
        List<TaskView> taskViews = getTaskViews();
        int size = taskViews.size();
        if (size > 0) {
            TaskView taskView = taskViews.get(0);
            TaskView taskView2 = taskViews.get(size - 1);
            accessibilityEvent.setFromIndex(this.mStack.indexOfStackTask(taskView.getTask()));
            accessibilityEvent.setToIndex(this.mStack.indexOfStackTask(taskView2.getTask()));
            accessibilityEvent.setContentDescription(taskView2.getTask().title);
        }
        accessibilityEvent.setItemCount(this.mStack.getTaskCount());
        int height = this.mLayoutAlgorithm.mStackRect.height();
        accessibilityEvent.setScrollY((int) (this.mStackScroller.getStackScroll() * height));
        accessibilityEvent.setMaxScrollY((int) (this.mLayoutAlgorithm.mMaxScrollP * height));
    }

    @Override // android.view.View
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
        if (getTaskViews().size() > 1) {
            Task accessibilityFocusedTask = getAccessibilityFocusedTask();
            accessibilityNodeInfo.setScrollable(true);
            int indexOfStackTask = this.mStack.indexOfStackTask(accessibilityFocusedTask);
            if (indexOfStackTask > 0) {
                accessibilityNodeInfo.addAction(8192);
            }
            if (indexOfStackTask < 0 || indexOfStackTask >= this.mStack.getTaskCount() - 1) {
                return;
            }
            accessibilityNodeInfo.addAction(4096);
        }
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        return this.mTouchHandler.onInterceptTouchEvent(motionEvent);
    }

    @Override // android.widget.FrameLayout, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        this.mTmpTaskViews.clear();
        this.mTmpTaskViews.addAll(getTaskViews());
        this.mTmpTaskViews.addAll(this.mViewPool.getViews());
        int size = this.mTmpTaskViews.size();
        for (int i5 = 0; i5 < size; i5++) {
            layoutTaskView(z, this.mTmpTaskViews.get(i5));
        }
        if (z && this.mStackScroller.isScrollOutOfBounds()) {
            this.mStackScroller.boundScroll();
        }
        relayoutTaskViews(AnimationProps.IMMEDIATE);
        clipTaskViews();
        if (this.mAwaitingFirstLayout || !this.mEnterAnimationComplete) {
            this.mAwaitingFirstLayout = false;
            this.mInitialState = 0;
            onFirstLayout();
        }
    }

    @Override // android.widget.FrameLayout, android.view.View
    protected void onMeasure(int i, int i2) {
        this.mInMeasureLayout = true;
        int size = View.MeasureSpec.getSize(i);
        int size2 = View.MeasureSpec.getSize(i2);
        this.mLayoutAlgorithm.getTaskStackBounds(this.mDisplayRect, new Rect(0, 0, size, size2), this.mLayoutAlgorithm.mSystemInsets.top, this.mLayoutAlgorithm.mSystemInsets.right, this.mTmpRect);
        if (!this.mTmpRect.equals(this.mStableStackBounds)) {
            this.mStableStackBounds.set(this.mTmpRect);
            this.mStackBounds.set(this.mTmpRect);
            this.mStableWindowRect.set(0, 0, size, size2);
            this.mWindowRect.set(0, 0, size, size2);
        }
        this.mStableLayoutAlgorithm.initialize(this.mDisplayRect, this.mStableWindowRect, this.mStableStackBounds, TaskStackLayoutAlgorithm.StackState.getStackStateForStack(this.mStack));
        this.mLayoutAlgorithm.initialize(this.mDisplayRect, this.mWindowRect, this.mStackBounds, TaskStackLayoutAlgorithm.StackState.getStackStateForStack(this.mStack));
        updateLayoutAlgorithm(false);
        boolean z = (size == this.mLastWidth && size2 == this.mLastHeight) ? false : this.mResetToInitialStateWhenResized;
        if (this.mAwaitingFirstLayout || this.mInitialState != 0 || z) {
            if (this.mInitialState != 2 || z) {
                updateToInitialState();
                this.mResetToInitialStateWhenResized = false;
            }
            if (!this.mAwaitingFirstLayout) {
                this.mInitialState = 0;
            }
        }
        bindVisibleTaskViews(this.mStackScroller.getStackScroll(), false);
        this.mTmpTaskViews.clear();
        this.mTmpTaskViews.addAll(getTaskViews());
        this.mTmpTaskViews.addAll(this.mViewPool.getViews());
        int size3 = this.mTmpTaskViews.size();
        for (int i3 = 0; i3 < size3; i3++) {
            measureTaskView(this.mTmpTaskViews.get(i3));
        }
        setMeasuredDimension(size, size2);
        this.mLastWidth = size;
        this.mLastHeight = size2;
        this.mInMeasureLayout = false;
    }

    @Override // com.android.systemui.recents.views.ViewPool.ViewPoolConsumer
    public void onPickUpViewFromPool(TaskView taskView, Task task, boolean z) {
        int findTaskViewInsertIndex = findTaskViewInsertIndex(task, this.mStack.indexOfStackTask(task));
        if (!z) {
            attachViewToParent(taskView, findTaskViewInsertIndex, taskView.getLayoutParams());
        } else if (this.mInMeasureLayout) {
            addView(taskView, findTaskViewInsertIndex);
        } else {
            ViewGroup.LayoutParams layoutParams = taskView.getLayoutParams();
            FrameLayout.LayoutParams layoutParams2 = layoutParams;
            if (layoutParams == null) {
                layoutParams2 = generateDefaultLayoutParams();
            }
            addViewInLayout(taskView, findTaskViewInsertIndex, layoutParams2, true);
            measureTaskView(taskView);
            layoutTaskView(true, taskView);
        }
        updateTaskViewsList();
        bindTaskView(taskView, task);
        if (this.mUIDozeTrigger.isAsleep()) {
            taskView.setNoUserInteractionState();
        }
        taskView.setCallbacks(this);
        taskView.setTouchEnabled(true);
        taskView.setClipViewInStack(true);
        if (this.mFocusedTask == task) {
            taskView.setFocusedState(true, false);
            if (this.mStartTimerIndicatorDuration > 0) {
                taskView.getHeaderView().startFocusTimerIndicator(this.mStartTimerIndicatorDuration);
                this.mStartTimerIndicatorDuration = 0;
            }
        }
        if (this.mScreenPinningEnabled && taskView.getTask() == this.mStack.getStackFrontMostTask(false)) {
            taskView.showActionButton(false, 0);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void onReload(boolean z) {
        if (!z) {
            resetFocusedTask(getFocusedTask());
        }
        ArrayList arrayList = new ArrayList();
        arrayList.addAll(getTaskViews());
        arrayList.addAll(this.mViewPool.getViews());
        for (int size = arrayList.size() - 1; size >= 0; size--) {
            ((TaskView) arrayList.get(size)).onReload(z);
        }
        readSystemFlags();
        this.mTaskViewsClipDirty = true;
        this.mEnterAnimationComplete = false;
        this.mUIDozeTrigger.stopDozing();
        if (z) {
            animateFreeformWorkspaceBackgroundAlpha(this.mLayoutAlgorithm.getStackState().freeformBackgroundAlpha, new AnimationProps(150, Interpolators.FAST_OUT_SLOW_IN));
        } else {
            this.mStackScroller.reset();
            this.mStableLayoutAlgorithm.reset();
            this.mLayoutAlgorithm.reset();
        }
        this.mAwaitingFirstLayout = true;
        this.mInitialState = 1;
        requestLayout();
    }

    @Override // com.android.systemui.recents.views.ViewPool.ViewPoolConsumer
    public void onReturnViewToPool(TaskView taskView) {
        unbindTaskView(taskView, taskView.getTask());
        taskView.clearAccessibilityFocus();
        taskView.resetViewProperties();
        taskView.setFocusedState(false, false);
        taskView.setClipViewInStack(false);
        if (this.mScreenPinningEnabled) {
            taskView.hideActionButton(false, 0, false, null);
        }
        detachViewFromParent(taskView);
        updateTaskViewsList();
    }

    @Override // com.android.systemui.recents.views.TaskStackViewScroller.TaskStackViewScrollerCallbacks
    public void onStackScrollChanged(float f, float f2, AnimationProps animationProps) {
        this.mUIDozeTrigger.poke();
        if (animationProps != null) {
            relayoutTaskViewsOnNextFrame(animationProps);
        }
        if (this.mEnterAnimationComplete) {
            if (f > 0.3f && f2 <= 0.3f && this.mStack.getTaskCount() > 0) {
                EventBus.getDefault().send(new ShowStackActionButtonEvent(true));
            } else if (f >= 0.3f || f2 < 0.3f) {
            } else {
                EventBus.getDefault().send(new HideStackActionButtonEvent());
            }
        }
    }

    @Override // com.android.systemui.recents.model.TaskStack.TaskStackCallbacks
    public void onStackTaskAdded(TaskStack taskStack, Task task) {
        updateLayoutAlgorithm(true);
        relayoutTaskViews(this.mAwaitingFirstLayout ? AnimationProps.IMMEDIATE : new AnimationProps(200, Interpolators.FAST_OUT_SLOW_IN));
    }

    @Override // com.android.systemui.recents.model.TaskStack.TaskStackCallbacks
    public void onStackTaskRemoved(TaskStack taskStack, Task task, Task task2, AnimationProps animationProps, boolean z) {
        TaskView childViewForTask;
        if (this.mFocusedTask == task) {
            resetFocusedTask(task);
        }
        TaskView childViewForTask2 = getChildViewForTask(task);
        if (childViewForTask2 != null) {
            this.mViewPool.returnViewToPool(childViewForTask2);
        }
        removeIgnoreTask(task);
        if (animationProps != null) {
            updateLayoutAlgorithm(true);
            relayoutTaskViews(animationProps);
        }
        if (this.mScreenPinningEnabled && task2 != null && (childViewForTask = getChildViewForTask(task2)) != null) {
            childViewForTask.showActionButton(true, 200);
        }
        if (this.mStack.getTaskCount() == 0) {
            EventBus.getDefault().send(new AllTaskViewsDismissedEvent(z ? 2131493579 : 2131493580));
        }
    }

    @Override // com.android.systemui.recents.model.TaskStack.TaskStackCallbacks
    public void onStackTasksRemoved(TaskStack taskStack) {
        resetFocusedTask(getFocusedTask());
        ArrayList arrayList = new ArrayList();
        arrayList.addAll(getTaskViews());
        for (int size = arrayList.size() - 1; size >= 0; size--) {
            this.mViewPool.returnViewToPool((TaskView) arrayList.get(size));
        }
        this.mIgnoreTasks.clear();
        EventBus.getDefault().send(new AllTaskViewsDismissedEvent(2131493580));
    }

    @Override // com.android.systemui.recents.model.TaskStack.TaskStackCallbacks
    public void onStackTasksUpdated(TaskStack taskStack) {
        updateLayoutAlgorithm(false);
        relayoutTaskViews(AnimationProps.IMMEDIATE);
        List<TaskView> taskViews = getTaskViews();
        int size = taskViews.size();
        for (int i = 0; i < size; i++) {
            TaskView taskView = taskViews.get(i);
            bindTaskView(taskView, taskView.getTask());
        }
    }

    @Override // com.android.systemui.recents.views.TaskView.TaskViewCallbacks
    public void onTaskViewClipStateChanged(TaskView taskView) {
        if (this.mTaskViewsClipDirty) {
            return;
        }
        this.mTaskViewsClipDirty = true;
        invalidate();
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        return this.mTouchHandler.onTouchEvent(motionEvent);
    }

    @Override // android.view.View
    public boolean performAccessibilityAction(int i, Bundle bundle) {
        if (super.performAccessibilityAction(i, bundle)) {
            return true;
        }
        int indexOfStackTask = this.mStack.indexOfStackTask(getAccessibilityFocusedTask());
        if (indexOfStackTask < 0 || indexOfStackTask >= this.mStack.getTaskCount()) {
            return false;
        }
        switch (i) {
            case 4096:
                setFocusedTask(indexOfStackTask + 1, true, true, 0);
                return true;
            case 8192:
                setFocusedTask(indexOfStackTask - 1, true, true, 0);
                return true;
            default:
                return false;
        }
    }

    public void relayoutTaskViews(AnimationProps animationProps) {
        relayoutTaskViews(animationProps, null, false);
    }

    void relayoutTaskViewsOnNextFrame(AnimationProps animationProps) {
        this.mDeferredTaskViewLayoutAnimation = animationProps;
        invalidate();
    }

    public void reloadOnConfigurationChange() {
        this.mStableLayoutAlgorithm.reloadOnConfigurationChange(getContext());
        this.mLayoutAlgorithm.reloadOnConfigurationChange(getContext());
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void removeIgnoreTask(Task task) {
        this.mIgnoreTasks.remove(task.key);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void resetFocusedTask(Task task) {
        TaskView childViewForTask;
        if (task != null && (childViewForTask = getChildViewForTask(task)) != null) {
            childViewForTask.setFocusedState(false, false);
        }
        this.mFocusedTask = null;
    }

    public void setRelativeFocusedTask(boolean z, boolean z2, boolean z3) {
        setRelativeFocusedTask(z, z2, z3, false, 0);
    }

    public void setRelativeFocusedTask(boolean z, boolean z2, boolean z3, boolean z4, int i) {
        int i2;
        Task focusedTask = getFocusedTask();
        int indexOfStackTask = this.mStack.indexOfStackTask(focusedTask);
        if (focusedTask == null) {
            float stackScroll = this.mStackScroller.getStackScroll();
            ArrayList<Task> stackTasks = this.mStack.getStackTasks();
            int size = stackTasks.size();
            if (!z) {
                int i3 = 0;
                while (true) {
                    i2 = i3;
                    if (i3 >= size) {
                        break;
                    }
                    i2 = i3;
                    if (Float.compare(this.mLayoutAlgorithm.getStackScrollForTask(stackTasks.get(i3)), stackScroll) >= 0) {
                        break;
                    }
                    i3++;
                }
            } else {
                int i4 = size - 1;
                while (true) {
                    i2 = i4;
                    if (i4 < 0) {
                        break;
                    }
                    i2 = i4;
                    if (Float.compare(this.mLayoutAlgorithm.getStackScrollForTask(stackTasks.get(i4)), stackScroll) <= 0) {
                        break;
                    }
                    i4--;
                }
            }
        } else if (z2) {
            ArrayList<Task> stackTasks2 = this.mStack.getStackTasks();
            if (focusedTask.isFreeformTask()) {
                TaskView frontMostTaskView = getFrontMostTaskView(z2);
                i2 = indexOfStackTask;
                if (frontMostTaskView != null) {
                    i2 = this.mStack.indexOfStackTask(frontMostTaskView.getTask());
                }
            } else {
                int i5 = indexOfStackTask + (z ? -1 : 1);
                i2 = indexOfStackTask;
                if (i5 >= 0) {
                    i2 = indexOfStackTask;
                    if (i5 < stackTasks2.size()) {
                        i2 = indexOfStackTask;
                        if (!stackTasks2.get(i5).isFreeformTask()) {
                            i2 = i5;
                        }
                    }
                }
            }
        } else {
            int taskCount = this.mStack.getTaskCount();
            i2 = (((z ? -1 : 1) + indexOfStackTask) + taskCount) % taskCount;
        }
        if (i2 != -1 && setFocusedTask(i2, true, true, i) && z4) {
            EventBus.getDefault().send(new CancelEnterRecentsWindowAnimationEvent(null));
        }
    }

    public void setSystemInsets(Rect rect) {
        if (this.mStableLayoutAlgorithm.setSystemInsets(rect) || this.mLayoutAlgorithm.setSystemInsets(rect)) {
            requestLayout();
        }
    }

    public void setTasks(TaskStack taskStack, boolean z) {
        this.mStack.setTasks(getContext(), taskStack.computeAllTasksList(), z ? this.mLayoutAlgorithm.isInitialized() : false);
    }

    public void updateLayoutAlgorithm(boolean z) {
        this.mLayoutAlgorithm.update(this.mStack, this.mIgnoreTasks);
        if (Recents.getSystemServices().hasFreeformWorkspaceSupport()) {
            this.mTmpRect.set(this.mLayoutAlgorithm.mFreeformRect);
            this.mFreeformWorkspaceBackground.setBounds(this.mTmpRect);
        }
        if (z) {
            this.mStackScroller.boundScroll();
        }
    }

    public void updateTaskViewToTransform(TaskView taskView, TaskViewTransform taskViewTransform, AnimationProps animationProps) {
        if (taskView.isAnimatingTo(taskViewTransform)) {
            return;
        }
        taskView.cancelTransformAnimation();
        taskView.updateViewPropertiesToTaskTransform(taskViewTransform, animationProps, this.mRequestUpdateClippingListener);
    }

    void updateTaskViewsList() {
        this.mTaskViews.clear();
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = getChildAt(i);
            if (childAt instanceof TaskView) {
                this.mTaskViews.add((TaskView) childAt);
            }
        }
    }

    public void updateToInitialState() {
        this.mStackScroller.setStackScrollToInitialState();
        this.mLayoutAlgorithm.setTaskOverridesForInitialState(this.mStack, false);
    }

    @Override // android.view.View
    protected boolean verifyDrawable(Drawable drawable) {
        if (drawable == this.mFreeformWorkspaceBackground) {
            return true;
        }
        return super.verifyDrawable(drawable);
    }
}
