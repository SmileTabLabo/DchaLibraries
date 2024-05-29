package com.android.systemui.recents.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import com.android.systemui.Interpolators;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.RecentsActivityLaunchState;
import com.android.systemui.recents.misc.ReferenceCountedTrigger;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.model.TaskStack;
import java.util.ArrayList;
import java.util.List;
/* loaded from: a.zip:com/android/systemui/recents/views/TaskStackAnimationHelper.class */
public class TaskStackAnimationHelper {
    private TaskStackView mStackView;
    private static final Interpolator ENTER_FROM_HOME_TRANSLATION_INTERPOLATOR = Interpolators.LINEAR_OUT_SLOW_IN;
    private static final Interpolator ENTER_FROM_HOME_ALPHA_INTERPOLATOR = Interpolators.LINEAR;
    private static final Interpolator EXIT_TO_HOME_TRANSLATION_INTERPOLATOR = new PathInterpolator(0.4f, 0.0f, 0.6f, 1.0f);
    private static final Interpolator DISMISS_ALL_TRANSLATION_INTERPOLATOR = new PathInterpolator(0.4f, 0.0f, 1.0f, 1.0f);
    private static final Interpolator FOCUS_NEXT_TASK_INTERPOLATOR = new PathInterpolator(0.4f, 0.0f, 0.0f, 1.0f);
    private static final Interpolator FOCUS_IN_FRONT_NEXT_TASK_INTERPOLATOR = new PathInterpolator(0.0f, 0.0f, 0.0f, 1.0f);
    private static final Interpolator FOCUS_BEHIND_NEXT_TASK_INTERPOLATOR = Interpolators.LINEAR_OUT_SLOW_IN;
    private static final Interpolator ENTER_WHILE_DOCKING_INTERPOLATOR = Interpolators.LINEAR_OUT_SLOW_IN;
    private TaskViewTransform mTmpTransform = new TaskViewTransform();
    private ArrayList<TaskViewTransform> mTmpCurrentTaskTransforms = new ArrayList<>();
    private ArrayList<TaskViewTransform> mTmpFinalTaskTransforms = new ArrayList<>();

    /* renamed from: -com_android_systemui_recents_views_TaskStackAnimationHelper_lambda$2  reason: not valid java name */
    static /* synthetic */ void m1289x884e26cc(TaskView taskView, float f, TaskStackViewTouchHandler taskStackViewTouchHandler, ValueAnimator valueAnimator) {
        float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        taskView.setTranslationX(floatValue * f);
        taskStackViewTouchHandler.updateSwipeProgress(taskView, true, floatValue);
    }

    public TaskStackAnimationHelper(Context context, TaskStackView taskStackView) {
        this.mStackView = taskStackView;
    }

    private int calculateStaggeredAnimDuration(int i) {
        return Math.max(100, ((i - 1) * 50) + 100);
    }

    public void prepareForEnterAnimation() {
        RecentsActivityLaunchState launchState = Recents.getConfiguration().getLaunchState();
        Resources resources = this.mStackView.getResources();
        Resources resources2 = this.mStackView.getContext().getApplicationContext().getResources();
        TaskStackLayoutAlgorithm stackAlgorithm = this.mStackView.getStackAlgorithm();
        TaskStackViewScroller scroller = this.mStackView.getScroller();
        TaskStack stack = this.mStackView.getStack();
        Task launchTarget = stack.getLaunchTarget();
        if (stack.getTaskCount() == 0) {
            return;
        }
        int height = stackAlgorithm.mStackRect.height();
        int dimensionPixelSize = resources.getDimensionPixelSize(2131690024);
        int dimensionPixelSize2 = resources.getDimensionPixelSize(2131690025);
        boolean z = resources2.getConfiguration().orientation == 2;
        List<TaskView> taskViews = this.mStackView.getTaskViews();
        for (int size = taskViews.size() - 1; size >= 0; size--) {
            TaskView taskView = taskViews.get(size);
            Task task = taskView.getTask();
            boolean isTaskAboveTask = (launchTarget == null || launchTarget.group == null) ? false : launchTarget.group.isTaskAboveTask(task, launchTarget);
            boolean isFreeformTask = (launchTarget == null || !launchTarget.isFreeformTask()) ? false : task.isFreeformTask();
            stackAlgorithm.getStackTransform(task, scroller.getStackScroll(), this.mTmpTransform, null);
            if (isFreeformTask) {
                taskView.setVisibility(4);
            } else if (!launchState.launchedFromApp || launchState.launchedViaDockGesture) {
                if (launchState.launchedFromHome) {
                    this.mTmpTransform.rect.offset(0.0f, height);
                    this.mTmpTransform.alpha = 0.0f;
                    this.mStackView.updateTaskViewToTransform(taskView, this.mTmpTransform, AnimationProps.IMMEDIATE);
                } else if (launchState.launchedViaDockGesture) {
                    this.mTmpTransform.rect.offset(0.0f, z ? dimensionPixelSize2 : (int) (height * 0.9f));
                    this.mTmpTransform.alpha = 0.0f;
                    this.mStackView.updateTaskViewToTransform(taskView, this.mTmpTransform, AnimationProps.IMMEDIATE);
                }
            } else if (task.isLaunchTarget) {
                taskView.onPrepareLaunchTargetForEnterAnimation();
            } else if (isTaskAboveTask) {
                this.mTmpTransform.rect.offset(0.0f, dimensionPixelSize);
                this.mTmpTransform.alpha = 0.0f;
                this.mStackView.updateTaskViewToTransform(taskView, this.mTmpTransform, AnimationProps.IMMEDIATE);
                taskView.setClipViewInStack(false);
            }
        }
    }

    public void startDeleteAllTasksAnimation(List<TaskView> list, ReferenceCountedTrigger referenceCountedTrigger) {
        TaskStackLayoutAlgorithm stackAlgorithm = this.mStackView.getStackAlgorithm();
        int measuredWidth = this.mStackView.getMeasuredWidth();
        int i = stackAlgorithm.mTaskRect.left;
        int size = list.size();
        for (int i2 = size - 1; i2 >= 0; i2--) {
            TaskView taskView = list.get(i2);
            taskView.setClipViewInStack(false);
            AnimationProps animationProps = new AnimationProps(((size - i2) - 1) * 33, 200, DISMISS_ALL_TRANSLATION_INTERPOLATOR, new AnimatorListenerAdapter(this, referenceCountedTrigger, taskView) { // from class: com.android.systemui.recents.views.TaskStackAnimationHelper.4
                final TaskStackAnimationHelper this$0;
                final ReferenceCountedTrigger val$postAnimationTrigger;
                final TaskView val$tv;

                {
                    this.this$0 = this;
                    this.val$postAnimationTrigger = referenceCountedTrigger;
                    this.val$tv = taskView;
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    this.val$postAnimationTrigger.decrement();
                    this.val$tv.setClipViewInStack(true);
                }
            });
            referenceCountedTrigger.increment();
            this.mTmpTransform.fillIn(taskView);
            this.mTmpTransform.rect.offset(measuredWidth - i, 0.0f);
            this.mStackView.updateTaskViewToTransform(taskView, this.mTmpTransform, animationProps);
        }
    }

    public void startDeleteTaskAnimation(TaskView taskView, ReferenceCountedTrigger referenceCountedTrigger) {
        TaskStackViewTouchHandler touchHandler = this.mStackView.getTouchHandler();
        touchHandler.onBeginManualDrag(taskView);
        referenceCountedTrigger.increment();
        referenceCountedTrigger.addLastDecrementRunnable(new Runnable(touchHandler, taskView) { // from class: com.android.systemui.recents.views.TaskStackAnimationHelper._void_startDeleteTaskAnimation_com_android_systemui_recents_views_TaskView_deleteTaskView_com_android_systemui_recents_misc_ReferenceCountedTrigger_postAnimationTrigger_LambdaImpl0
            private TaskView val$deleteTaskView;
            private TaskStackViewTouchHandler val$touchHandler;

            {
                this.val$touchHandler = touchHandler;
                this.val$deleteTaskView = taskView;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.val$touchHandler.onChildDismissed(this.val$deleteTaskView);
            }
        });
        float scaledDismissSize = touchHandler.getScaledDismissSize();
        ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        ofFloat.setDuration(400L);
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(taskView, scaledDismissSize, touchHandler) { // from class: com.android.systemui.recents.views.TaskStackAnimationHelper._void_startDeleteTaskAnimation_com_android_systemui_recents_views_TaskView_deleteTaskView_com_android_systemui_recents_misc_ReferenceCountedTrigger_postAnimationTrigger_LambdaImpl1
            private TaskView val$deleteTaskView;
            private float val$dismissSize;
            private TaskStackViewTouchHandler val$touchHandler;

            {
                this.val$deleteTaskView = taskView;
                this.val$dismissSize = scaledDismissSize;
                this.val$touchHandler = touchHandler;
            }

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                TaskStackAnimationHelper.m1289x884e26cc(this.val$deleteTaskView, this.val$dismissSize, this.val$touchHandler, valueAnimator);
            }
        });
        ofFloat.addListener(new AnimatorListenerAdapter(this, referenceCountedTrigger) { // from class: com.android.systemui.recents.views.TaskStackAnimationHelper.3
            final TaskStackAnimationHelper this$0;
            final ReferenceCountedTrigger val$postAnimationTrigger;

            {
                this.this$0 = this;
                this.val$postAnimationTrigger = referenceCountedTrigger;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                this.val$postAnimationTrigger.decrement();
            }
        });
        ofFloat.start();
    }

    public void startEnterAnimation(ReferenceCountedTrigger referenceCountedTrigger) {
        RecentsActivityLaunchState launchState = Recents.getConfiguration().getLaunchState();
        Resources resources = this.mStackView.getResources();
        Resources resources2 = this.mStackView.getContext().getApplicationContext().getResources();
        TaskStackLayoutAlgorithm stackAlgorithm = this.mStackView.getStackAlgorithm();
        TaskStackViewScroller scroller = this.mStackView.getScroller();
        TaskStack stack = this.mStackView.getStack();
        Task launchTarget = stack.getLaunchTarget();
        if (stack.getTaskCount() == 0) {
            return;
        }
        int integer = resources.getInteger(2131755062);
        int integer2 = resources.getInteger(2131755063);
        int integer3 = resources2.getInteger(2131755071);
        List<TaskView> taskViews = this.mStackView.getTaskViews();
        int size = taskViews.size();
        for (int i = size - 1; i >= 0; i--) {
            int i2 = (size - i) - 1;
            TaskView taskView = taskViews.get(i);
            Task task = taskView.getTask();
            boolean isTaskAboveTask = (launchTarget == null || launchTarget.group == null) ? false : launchTarget.group.isTaskAboveTask(task, launchTarget);
            stackAlgorithm.getStackTransform(task, scroller.getStackScroll(), this.mTmpTransform, null);
            if (!launchState.launchedFromApp || launchState.launchedViaDockGesture) {
                if (launchState.launchedFromHome) {
                    AnimationProps listener = new AnimationProps().setInitialPlayTime(6, Math.min(5, i2) * 33).setStartDelay(4, Math.min(5, i2) * 16).setDuration(6, 300).setDuration(4, 100).setInterpolator(6, ENTER_FROM_HOME_TRANSLATION_INTERPOLATOR).setInterpolator(4, ENTER_FROM_HOME_ALPHA_INTERPOLATOR).setListener(referenceCountedTrigger.decrementOnAnimationEnd());
                    referenceCountedTrigger.increment();
                    this.mStackView.updateTaskViewToTransform(taskView, this.mTmpTransform, listener);
                    if (i == size - 1) {
                        taskView.onStartFrontTaskEnterAnimation(this.mStackView.mScreenPinningEnabled);
                    }
                } else if (launchState.launchedViaDockGesture) {
                    AnimationProps listener2 = new AnimationProps().setDuration(6, (i * 33) + integer3).setInterpolator(6, ENTER_WHILE_DOCKING_INTERPOLATOR).setStartDelay(6, 48).setListener(referenceCountedTrigger.decrementOnAnimationEnd());
                    referenceCountedTrigger.increment();
                    this.mStackView.updateTaskViewToTransform(taskView, this.mTmpTransform, listener2);
                }
            } else if (task.isLaunchTarget) {
                taskView.onStartLaunchTargetEnterAnimation(this.mTmpTransform, integer, this.mStackView.mScreenPinningEnabled, referenceCountedTrigger);
            } else if (isTaskAboveTask) {
                AnimationProps animationProps = new AnimationProps(integer2, Interpolators.ALPHA_IN, new AnimatorListenerAdapter(this, referenceCountedTrigger, taskView) { // from class: com.android.systemui.recents.views.TaskStackAnimationHelper.1
                    final TaskStackAnimationHelper this$0;
                    final ReferenceCountedTrigger val$postAnimationTrigger;
                    final TaskView val$tv;

                    {
                        this.this$0 = this;
                        this.val$postAnimationTrigger = referenceCountedTrigger;
                        this.val$tv = taskView;
                    }

                    @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                    public void onAnimationEnd(Animator animator) {
                        this.val$postAnimationTrigger.decrement();
                        this.val$tv.setClipViewInStack(true);
                    }
                });
                referenceCountedTrigger.increment();
                this.mStackView.updateTaskViewToTransform(taskView, this.mTmpTransform, animationProps);
            }
        }
    }

    public void startExitToHomeAnimation(boolean z, ReferenceCountedTrigger referenceCountedTrigger) {
        AnimationProps animationProps;
        TaskStackLayoutAlgorithm stackAlgorithm = this.mStackView.getStackAlgorithm();
        if (this.mStackView.getStack().getTaskCount() == 0) {
            return;
        }
        int height = stackAlgorithm.mStackRect.height();
        List<TaskView> taskViews = this.mStackView.getTaskViews();
        int size = taskViews.size();
        for (int i = 0; i < size; i++) {
            TaskView taskView = taskViews.get(i);
            if (!this.mStackView.isIgnoredTask(taskView.getTask())) {
                if (z) {
                    animationProps = new AnimationProps().setStartDelay(6, Math.min(5, (size - i) - 1) * 33).setDuration(6, 200).setInterpolator(6, EXIT_TO_HOME_TRANSLATION_INTERPOLATOR).setListener(referenceCountedTrigger.decrementOnAnimationEnd());
                    referenceCountedTrigger.increment();
                } else {
                    animationProps = AnimationProps.IMMEDIATE;
                }
                this.mTmpTransform.fillIn(taskView);
                this.mTmpTransform.rect.offset(0.0f, height);
                this.mStackView.updateTaskViewToTransform(taskView, this.mTmpTransform, animationProps);
            }
        }
    }

    public void startLaunchTaskAnimation(TaskView taskView, boolean z, ReferenceCountedTrigger referenceCountedTrigger) {
        Resources resources = this.mStackView.getResources();
        int integer = resources.getInteger(2131755064);
        int dimensionPixelSize = resources.getDimensionPixelSize(2131690024);
        Task task = taskView.getTask();
        List<TaskView> taskViews = this.mStackView.getTaskViews();
        int size = taskViews.size();
        for (int i = 0; i < size; i++) {
            TaskView taskView2 = taskViews.get(i);
            boolean isTaskAboveTask = (task == null || task.group == null) ? false : task.group.isTaskAboveTask(taskView2.getTask(), task);
            if (taskView2 == taskView) {
                taskView2.setClipViewInStack(false);
                referenceCountedTrigger.addLastDecrementRunnable(new Runnable(this, taskView2) { // from class: com.android.systemui.recents.views.TaskStackAnimationHelper.2
                    final TaskStackAnimationHelper this$0;
                    final TaskView val$tv;

                    {
                        this.this$0 = this;
                        this.val$tv = taskView2;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        this.val$tv.setClipViewInStack(true);
                    }
                });
                taskView2.onStartLaunchTargetLaunchAnimation(integer, z, referenceCountedTrigger);
            } else if (isTaskAboveTask) {
                AnimationProps animationProps = new AnimationProps(integer, Interpolators.ALPHA_OUT, referenceCountedTrigger.decrementOnAnimationEnd());
                referenceCountedTrigger.increment();
                this.mTmpTransform.fillIn(taskView2);
                this.mTmpTransform.alpha = 0.0f;
                this.mTmpTransform.rect.offset(0.0f, dimensionPixelSize);
                this.mStackView.updateTaskViewToTransform(taskView2, this.mTmpTransform, animationProps);
            }
        }
    }

    public void startNewStackScrollAnimation(TaskStack taskStack, ReferenceCountedTrigger referenceCountedTrigger) {
        int indexOf;
        TaskStackLayoutAlgorithm stackAlgorithm = this.mStackView.getStackAlgorithm();
        TaskStackViewScroller scroller = this.mStackView.getScroller();
        ArrayList<Task> stackTasks = taskStack.getStackTasks();
        this.mStackView.getCurrentTaskTransforms(stackTasks, this.mTmpCurrentTaskTransforms);
        this.mStackView.setTasks(taskStack, false);
        this.mStackView.updateLayoutAlgorithm(false);
        float f = stackAlgorithm.mInitialScrollP;
        this.mStackView.bindVisibleTaskViews(f);
        stackAlgorithm.setFocusState(0);
        stackAlgorithm.setTaskOverridesForInitialState(taskStack, true);
        scroller.setStackScroll(f);
        this.mStackView.cancelDeferredTaskViewLayoutAnimation();
        this.mStackView.getLayoutTaskTransforms(f, stackAlgorithm.getFocusState(), stackTasks, false, this.mTmpFinalTaskTransforms);
        Task stackFrontMostTask = taskStack.getStackFrontMostTask(false);
        TaskView childViewForTask = this.mStackView.getChildViewForTask(stackFrontMostTask);
        TaskViewTransform taskViewTransform = this.mTmpFinalTaskTransforms.get(stackTasks.indexOf(stackFrontMostTask));
        if (childViewForTask != null) {
            this.mStackView.updateTaskViewToTransform(childViewForTask, stackAlgorithm.getFrontOfStackTransform(), AnimationProps.IMMEDIATE);
        }
        referenceCountedTrigger.addLastDecrementRunnable(new Runnable(this, f, childViewForTask, taskViewTransform) { // from class: com.android.systemui.recents.views.TaskStackAnimationHelper.6
            final TaskStackAnimationHelper this$0;
            final TaskView val$frontMostTaskView;
            final TaskViewTransform val$frontMostTransform;
            final float val$newScroll;

            {
                this.this$0 = this;
                this.val$newScroll = f;
                this.val$frontMostTaskView = childViewForTask;
                this.val$frontMostTransform = taskViewTransform;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.mStackView.bindVisibleTaskViews(this.val$newScroll);
                if (this.val$frontMostTaskView != null) {
                    this.this$0.mStackView.updateTaskViewToTransform(this.val$frontMostTaskView, this.val$frontMostTransform, new AnimationProps(75, 250, TaskStackAnimationHelper.FOCUS_BEHIND_NEXT_TASK_INTERPOLATOR));
                }
            }
        });
        List<TaskView> taskViews = this.mStackView.getTaskViews();
        int size = taskViews.size();
        for (int i = 0; i < size; i++) {
            TaskView taskView = taskViews.get(i);
            Task task = taskView.getTask();
            if (!this.mStackView.isIgnoredTask(task) && ((task != stackFrontMostTask || childViewForTask == null) && (indexOf = stackTasks.indexOf(task)) != -1)) {
                TaskViewTransform taskViewTransform2 = this.mTmpCurrentTaskTransforms.get(indexOf);
                TaskViewTransform taskViewTransform3 = this.mTmpFinalTaskTransforms.get(indexOf);
                this.mStackView.updateTaskViewToTransform(taskView, taskViewTransform2, AnimationProps.IMMEDIATE);
                AnimationProps listener = new AnimationProps().setDuration(6, calculateStaggeredAnimDuration(i)).setInterpolator(6, FOCUS_BEHIND_NEXT_TASK_INTERPOLATOR).setListener(referenceCountedTrigger.decrementOnAnimationEnd());
                referenceCountedTrigger.increment();
                this.mStackView.updateTaskViewToTransform(taskView, taskViewTransform3, listener);
            }
        }
    }

    public boolean startScrollToFocusedTaskAnimation(Task task, boolean z) {
        int i;
        Interpolator interpolator;
        TaskStackLayoutAlgorithm stackAlgorithm = this.mStackView.getStackAlgorithm();
        TaskStackViewScroller scroller = this.mStackView.getScroller();
        TaskStack stack = this.mStackView.getStack();
        float stackScroll = scroller.getStackScroll();
        float boundedStackScroll = scroller.getBoundedStackScroll(stackAlgorithm.getStackScrollForTask(task));
        boolean z2 = boundedStackScroll > stackScroll;
        boolean z3 = Float.compare(boundedStackScroll, stackScroll) != 0;
        int size = this.mStackView.getTaskViews().size();
        ArrayList<Task> stackTasks = stack.getStackTasks();
        this.mStackView.getCurrentTaskTransforms(stackTasks, this.mTmpCurrentTaskTransforms);
        this.mStackView.bindVisibleTaskViews(boundedStackScroll);
        stackAlgorithm.setFocusState(1);
        scroller.setStackScroll(boundedStackScroll, null);
        this.mStackView.cancelDeferredTaskViewLayoutAnimation();
        this.mStackView.getLayoutTaskTransforms(boundedStackScroll, stackAlgorithm.getFocusState(), stackTasks, true, this.mTmpFinalTaskTransforms);
        TaskView childViewForTask = this.mStackView.getChildViewForTask(task);
        if (childViewForTask == null) {
            Log.e("TaskStackAnimationHelper", "b/27389156 null-task-view prebind:" + size + " postbind:" + this.mStackView.getTaskViews().size() + " prescroll:" + stackScroll + " postscroll: " + boundedStackScroll);
            return false;
        }
        childViewForTask.setFocusedState(true, z);
        ReferenceCountedTrigger referenceCountedTrigger = new ReferenceCountedTrigger();
        referenceCountedTrigger.addLastDecrementRunnable(new Runnable(this, boundedStackScroll) { // from class: com.android.systemui.recents.views.TaskStackAnimationHelper.5
            final TaskStackAnimationHelper this$0;
            final float val$newScroll;

            {
                this.this$0 = this;
                this.val$newScroll = boundedStackScroll;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.mStackView.bindVisibleTaskViews(this.val$newScroll);
            }
        });
        List<TaskView> taskViews = this.mStackView.getTaskViews();
        int size2 = taskViews.size();
        int indexOf = taskViews.indexOf(childViewForTask);
        for (int i2 = 0; i2 < size2; i2++) {
            TaskView taskView = taskViews.get(i2);
            Task task2 = taskView.getTask();
            if (!this.mStackView.isIgnoredTask(task2)) {
                int indexOf2 = stackTasks.indexOf(task2);
                TaskViewTransform taskViewTransform = this.mTmpCurrentTaskTransforms.get(indexOf2);
                TaskViewTransform taskViewTransform2 = this.mTmpFinalTaskTransforms.get(indexOf2);
                this.mStackView.updateTaskViewToTransform(taskView, taskViewTransform, AnimationProps.IMMEDIATE);
                if (z2) {
                    i = calculateStaggeredAnimDuration(i2);
                    interpolator = FOCUS_BEHIND_NEXT_TASK_INTERPOLATOR;
                } else if (i2 < indexOf) {
                    i = (((indexOf - i2) - 1) * 50) + 150;
                    interpolator = FOCUS_BEHIND_NEXT_TASK_INTERPOLATOR;
                } else if (i2 > indexOf) {
                    i = Math.max(100, 150 - (((i2 - indexOf) - 1) * 50));
                    interpolator = FOCUS_IN_FRONT_NEXT_TASK_INTERPOLATOR;
                } else {
                    i = 200;
                    interpolator = FOCUS_NEXT_TASK_INTERPOLATOR;
                }
                AnimationProps listener = new AnimationProps().setDuration(6, i).setInterpolator(6, interpolator).setListener(referenceCountedTrigger.decrementOnAnimationEnd());
                referenceCountedTrigger.increment();
                this.mStackView.updateTaskViewToTransform(taskView, taskViewTransform2, listener);
            }
        }
        return z3;
    }
}
