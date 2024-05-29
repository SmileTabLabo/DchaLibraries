package com.android.systemui.recents.views;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.ArrayMap;
import android.util.Log;
import android.util.MutableBoolean;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewDebug;
import android.view.ViewParent;
import android.view.animation.Interpolator;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.Interpolators;
import com.android.systemui.SwipeHelper;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.events.activity.HideRecentsEvent;
import com.android.systemui.recents.events.ui.StackViewScrolledEvent;
import com.android.systemui.recents.events.ui.TaskViewDismissedEvent;
import com.android.systemui.recents.misc.FreePathInterpolator;
import com.android.systemui.recents.misc.Utilities;
import com.android.systemui.recents.model.Task;
import com.android.systemui.statusbar.FlingAnimationUtils;
import java.util.ArrayList;
import java.util.List;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: a.zip:com/android/systemui/recents/views/TaskStackViewTouchHandler.class */
public class TaskStackViewTouchHandler implements SwipeHelper.Callback {
    private static final Interpolator OVERSCROLL_INTERP;
    Context mContext;
    float mDownScrollP;
    int mDownX;
    int mDownY;
    FlingAnimationUtils mFlingAnimUtils;
    boolean mInterceptedBySwipeHelper;
    @ViewDebug.ExportedProperty(category = "recents")
    boolean mIsScrolling;
    int mLastY;
    int mMaximumVelocity;
    int mMinimumVelocity;
    int mOverscrollSize;
    ValueAnimator mScrollFlingAnimator;
    int mScrollTouchSlop;
    TaskStackViewScroller mScroller;
    TaskStackView mSv;
    SwipeHelper mSwipeHelper;
    private float mTargetStackScroll;
    VelocityTracker mVelocityTracker;
    final int mWindowTouchSlop;
    int mActivePointerId = -1;
    TaskView mActiveTaskView = null;
    private final StackViewScrolledEvent mStackViewScrolledEvent = new StackViewScrolledEvent();
    private ArrayList<Task> mCurrentTasks = new ArrayList<>();
    private ArrayList<TaskViewTransform> mCurrentTaskTransforms = new ArrayList<>();
    private ArrayList<TaskViewTransform> mFinalTaskTransforms = new ArrayList<>();
    private ArrayMap<View, Animator> mSwipeHelperAnimations = new ArrayMap<>();
    private TaskViewTransform mTmpTransform = new TaskViewTransform();

    static {
        Path path = new Path();
        path.moveTo(0.0f, 0.0f);
        path.cubicTo(0.2f, 0.175f, 0.25f, 0.3f, 1.0f, 0.3f);
        OVERSCROLL_INTERP = new FreePathInterpolator(path);
    }

    public TaskStackViewTouchHandler(Context context, TaskStackView taskStackView, TaskStackViewScroller taskStackViewScroller) {
        Resources resources = context.getResources();
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        this.mContext = context;
        this.mSv = taskStackView;
        this.mScroller = taskStackViewScroller;
        this.mMinimumVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
        this.mMaximumVelocity = viewConfiguration.getScaledMaximumFlingVelocity();
        this.mScrollTouchSlop = viewConfiguration.getScaledTouchSlop();
        this.mWindowTouchSlop = viewConfiguration.getScaledWindowTouchSlop();
        this.mFlingAnimUtils = new FlingAnimationUtils(context, 0.2f);
        this.mOverscrollSize = resources.getDimensionPixelSize(2131690021);
        this.mSwipeHelper = new SwipeHelper(this, 0, this, context) { // from class: com.android.systemui.recents.views.TaskStackViewTouchHandler.1
            final TaskStackViewTouchHandler this$0;

            {
                this.this$0 = this;
            }

            @Override // com.android.systemui.SwipeHelper
            protected long getMaxEscapeAnimDuration() {
                return 700L;
            }

            /* JADX INFO: Access modifiers changed from: protected */
            @Override // com.android.systemui.SwipeHelper
            public float getSize(View view) {
                return this.this$0.getScaledDismissSize();
            }

            @Override // com.android.systemui.SwipeHelper
            protected float getUnscaledEscapeVelocity() {
                return 800.0f;
            }

            @Override // com.android.systemui.SwipeHelper
            protected void prepareDismissAnimation(View view, Animator animator) {
                this.this$0.mSwipeHelperAnimations.put(view, animator);
            }

            @Override // com.android.systemui.SwipeHelper
            protected void prepareSnapBackAnimation(View view, Animator animator) {
                animator.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
                this.this$0.mSwipeHelperAnimations.put(view, animator);
            }
        };
        this.mSwipeHelper.setDisableHardwareLayers(true);
    }

    private TaskView findViewAtPoint(int i, int i2) {
        ArrayList<Task> stackTasks = this.mSv.getStack().getStackTasks();
        for (int size = stackTasks.size() - 1; size >= 0; size--) {
            TaskView childViewForTask = this.mSv.getChildViewForTask(stackTasks.get(size));
            if (childViewForTask != null && childViewForTask.getVisibility() == 0 && this.mSv.isTouchPointInView(i, i2, childViewForTask)) {
                return childViewForTask;
            }
        }
        return null;
    }

    /* JADX WARN: Code restructure failed: missing block: B:34:0x0209, code lost:
        if (r0 > r0) goto L36;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private boolean handleTouchEvent(MotionEvent motionEvent) {
        float signum;
        if (this.mSv.getTaskViews().size() == 0) {
            return false;
        }
        TaskStackLayoutAlgorithm taskStackLayoutAlgorithm = this.mSv.mLayoutAlgorithm;
        switch (motionEvent.getAction() & 255) {
            case 0:
                this.mScroller.stopScroller();
                this.mScroller.stopBoundScrollAnimation();
                this.mScroller.resetDeltaScroll();
                cancelNonDismissTaskAnimations();
                this.mSv.cancelDeferredTaskViewLayoutAnimation();
                this.mDownX = (int) motionEvent.getX();
                this.mDownY = (int) motionEvent.getY();
                this.mLastY = this.mDownY;
                this.mDownScrollP = this.mScroller.getStackScroll();
                this.mActivePointerId = motionEvent.getPointerId(0);
                this.mActiveTaskView = findViewAtPoint(this.mDownX, this.mDownY);
                initOrResetVelocityTracker();
                this.mVelocityTracker.addMovement(motionEvent);
                break;
            case 1:
                this.mVelocityTracker.addMovement(motionEvent);
                this.mVelocityTracker.computeCurrentVelocity(1000, this.mMaximumVelocity);
                int y = (int) motionEvent.getY(motionEvent.findPointerIndex(this.mActivePointerId));
                int yVelocity = (int) this.mVelocityTracker.getYVelocity(this.mActivePointerId);
                if (this.mIsScrolling) {
                    if (this.mScroller.isScrollOutOfBounds()) {
                        this.mScroller.animateBoundScroll();
                    } else if (Math.abs(yVelocity) > this.mMinimumVelocity) {
                        this.mScroller.fling(this.mDownScrollP, this.mDownY, y, yVelocity, this.mDownY + taskStackLayoutAlgorithm.getYForDeltaP(this.mDownScrollP, taskStackLayoutAlgorithm.mMaxScrollP), this.mDownY + taskStackLayoutAlgorithm.getYForDeltaP(this.mDownScrollP, taskStackLayoutAlgorithm.mMinScrollP), this.mOverscrollSize);
                        this.mSv.invalidate();
                    }
                    if (!this.mSv.mTouchExplorationEnabled) {
                        this.mSv.resetFocusedTask(this.mSv.getFocusedTask());
                    }
                } else if (this.mActiveTaskView == null) {
                    maybeHideRecentsFromBackgroundTap((int) motionEvent.getX(), (int) motionEvent.getY());
                }
                this.mActivePointerId = -1;
                this.mIsScrolling = false;
                recycleVelocityTracker();
                break;
            case 2:
                int findPointerIndex = motionEvent.findPointerIndex(this.mActivePointerId);
                if (findPointerIndex < 0) {
                    Log.d("TaskStackViewTouchHandler", "findPointerIndex failed");
                    this.mActivePointerId = -1;
                    break;
                } else {
                    int y2 = (int) motionEvent.getY(findPointerIndex);
                    int x = (int) motionEvent.getX(findPointerIndex);
                    if (!this.mIsScrolling) {
                        int abs = Math.abs(y2 - this.mDownY);
                        int abs2 = Math.abs(x - this.mDownX);
                        if (Math.abs(y2 - this.mDownY) > this.mScrollTouchSlop && abs > abs2) {
                            this.mIsScrolling = true;
                            float stackScroll = this.mScroller.getStackScroll();
                            List<TaskView> taskViews = this.mSv.getTaskViews();
                            for (int size = taskViews.size() - 1; size >= 0; size--) {
                                taskStackLayoutAlgorithm.addUnfocusedTaskOverride(taskViews.get(size).getTask(), stackScroll);
                            }
                            taskStackLayoutAlgorithm.setFocusState(0);
                            ViewParent parent = this.mSv.getParent();
                            if (parent != null) {
                                parent.requestDisallowInterceptTouchEvent(true);
                            }
                            MetricsLogger.action(this.mSv.getContext(), 287);
                        }
                    }
                    if (this.mIsScrolling) {
                        float deltaPForY = taskStackLayoutAlgorithm.getDeltaPForY(this.mDownY, y2);
                        float f = taskStackLayoutAlgorithm.mMinScrollP;
                        float f2 = taskStackLayoutAlgorithm.mMaxScrollP;
                        float f3 = this.mDownScrollP + deltaPForY;
                        if (f3 >= f) {
                            signum = f3;
                            break;
                        }
                        float clamp = Utilities.clamp(f3, f, f2);
                        float f4 = f3 - clamp;
                        signum = clamp + (Math.signum(f4) * 2.3333333f * OVERSCROLL_INTERP.getInterpolation(Math.abs(f4) / 2.3333333f));
                        this.mDownScrollP += this.mScroller.setDeltaStackScroll(this.mDownScrollP, signum - this.mDownScrollP);
                        this.mStackViewScrolledEvent.updateY(y2 - this.mLastY);
                        EventBus.getDefault().send(this.mStackViewScrolledEvent);
                    }
                    this.mLastY = y2;
                    this.mVelocityTracker.addMovement(motionEvent);
                    break;
                }
                break;
            case 3:
                this.mActivePointerId = -1;
                this.mIsScrolling = false;
                recycleVelocityTracker();
                break;
            case 5:
                int actionIndex = motionEvent.getActionIndex();
                this.mActivePointerId = motionEvent.getPointerId(actionIndex);
                this.mDownX = (int) motionEvent.getX(actionIndex);
                this.mDownY = (int) motionEvent.getY(actionIndex);
                this.mLastY = this.mDownY;
                this.mDownScrollP = this.mScroller.getStackScroll();
                this.mScroller.resetDeltaScroll();
                this.mVelocityTracker.addMovement(motionEvent);
                break;
            case 6:
                int actionIndex2 = motionEvent.getActionIndex();
                if (motionEvent.getPointerId(actionIndex2) == this.mActivePointerId) {
                    this.mActivePointerId = motionEvent.getPointerId(actionIndex2 == 0 ? 1 : 0);
                    this.mDownX = (int) motionEvent.getX(actionIndex2);
                    this.mDownY = (int) motionEvent.getY(actionIndex2);
                    this.mLastY = this.mDownY;
                    this.mDownScrollP = this.mScroller.getStackScroll();
                }
                this.mVelocityTracker.addMovement(motionEvent);
                break;
        }
        return this.mIsScrolling;
    }

    private void updateTaskViewTransforms(float f) {
        int indexOf;
        List<TaskView> taskViews = this.mSv.getTaskViews();
        int size = taskViews.size();
        for (int i = 0; i < size; i++) {
            TaskView taskView = taskViews.get(i);
            Task task = taskView.getTask();
            if (!this.mSv.isIgnoredTask(task) && (indexOf = this.mCurrentTasks.indexOf(task)) != -1) {
                TaskViewTransform taskViewTransform = this.mCurrentTaskTransforms.get(indexOf);
                TaskViewTransform taskViewTransform2 = this.mFinalTaskTransforms.get(indexOf);
                this.mTmpTransform.copyFrom(taskViewTransform);
                this.mTmpTransform.rect.set(Utilities.RECTF_EVALUATOR.evaluate(f, taskViewTransform.rect, taskViewTransform2.rect));
                this.mTmpTransform.dimAlpha = taskViewTransform.dimAlpha + ((taskViewTransform2.dimAlpha - taskViewTransform.dimAlpha) * f);
                this.mTmpTransform.viewOutlineAlpha = taskViewTransform.viewOutlineAlpha + ((taskViewTransform2.viewOutlineAlpha - taskViewTransform.viewOutlineAlpha) * f);
                this.mTmpTransform.translationZ = taskViewTransform.translationZ + ((taskViewTransform2.translationZ - taskViewTransform.translationZ) * f);
                this.mSv.updateTaskViewToTransform(taskView, this.mTmpTransform, AnimationProps.IMMEDIATE);
            }
        }
    }

    @Override // com.android.systemui.SwipeHelper.Callback
    public boolean canChildBeDismissed(View view) {
        Task task = ((TaskView) view).getTask();
        boolean z = false;
        if (!this.mSwipeHelperAnimations.containsKey(view)) {
            z = false;
            if (this.mSv.getStack().indexOfStackTask(task) != -1) {
                z = true;
            }
        }
        return z;
    }

    public void cancelNonDismissTaskAnimations() {
        Utilities.cancelAnimationWithoutCallbacks(this.mScrollFlingAnimator);
        if (!this.mSwipeHelperAnimations.isEmpty()) {
            List<TaskView> taskViews = this.mSv.getTaskViews();
            for (int size = taskViews.size() - 1; size >= 0; size--) {
                TaskView taskView = taskViews.get(size);
                if (!this.mSv.isIgnoredTask(taskView.getTask())) {
                    taskView.cancelTransformAnimation();
                    this.mSv.getStackAlgorithm().addUnfocusedTaskOverride(taskView, this.mTargetStackScroll);
                }
            }
            this.mSv.getStackAlgorithm().setFocusState(0);
            this.mSv.getScroller().setStackScroll(this.mTargetStackScroll, null);
            this.mSwipeHelperAnimations.clear();
        }
        this.mActiveTaskView = null;
    }

    @Override // com.android.systemui.SwipeHelper.Callback
    public View getChildAtPosition(MotionEvent motionEvent) {
        TaskView findViewAtPoint = findViewAtPoint((int) motionEvent.getX(), (int) motionEvent.getY());
        if (findViewAtPoint == null || !canChildBeDismissed(findViewAtPoint)) {
            return null;
        }
        return findViewAtPoint;
    }

    @Override // com.android.systemui.SwipeHelper.Callback
    public float getFalsingThresholdFactor() {
        return 0.0f;
    }

    public float getScaledDismissSize() {
        return Math.max(this.mSv.getWidth(), this.mSv.getHeight()) * 1.5f;
    }

    void initOrResetVelocityTracker() {
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        } else {
            this.mVelocityTracker.clear();
        }
    }

    @Override // com.android.systemui.SwipeHelper.Callback
    public boolean isAntiFalsingNeeded() {
        return false;
    }

    void maybeHideRecentsFromBackgroundTap(int i, int i2) {
        int abs = Math.abs(this.mDownX - i);
        int abs2 = Math.abs(this.mDownY - i2);
        if (abs > this.mScrollTouchSlop || abs2 > this.mScrollTouchSlop) {
            return;
        }
        if (findViewAtPoint(i > (this.mSv.getRight() - this.mSv.getLeft()) / 2 ? i - this.mWindowTouchSlop : i + this.mWindowTouchSlop, i2) != null) {
            return;
        }
        if (i <= this.mSv.mLayoutAlgorithm.mStackRect.left || i >= this.mSv.mLayoutAlgorithm.mStackRect.right) {
            if (Recents.getSystemServices().hasFreeformWorkspaceSupport()) {
                Rect rect = this.mSv.mLayoutAlgorithm.mFreeformRect;
                if (rect.top <= i2 && i2 <= rect.bottom && this.mSv.launchFreeformTasks()) {
                    return;
                }
            }
            EventBus.getDefault().send(new HideRecentsEvent(false, true));
        }
    }

    @Override // com.android.systemui.SwipeHelper.Callback
    public void onBeginDrag(View view) {
        TaskView taskView = (TaskView) view;
        taskView.setClipViewInStack(false);
        taskView.setTouchEnabled(false);
        ViewParent parent = this.mSv.getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(true);
        }
        this.mSv.addIgnoreTask(taskView.getTask());
        this.mCurrentTasks = new ArrayList<>(this.mSv.getStack().getStackTasks());
        MutableBoolean mutableBoolean = new MutableBoolean(false);
        Task findAnchorTask = this.mSv.findAnchorTask(this.mCurrentTasks, mutableBoolean);
        TaskStackLayoutAlgorithm stackAlgorithm = this.mSv.getStackAlgorithm();
        TaskStackViewScroller scroller = this.mSv.getScroller();
        if (findAnchorTask != null) {
            this.mSv.getCurrentTaskTransforms(this.mCurrentTasks, this.mCurrentTaskTransforms);
            float f = 0.0f;
            boolean z = this.mCurrentTasks.size() > 0;
            if (z) {
                f = stackAlgorithm.getStackScrollForTask(findAnchorTask);
            }
            this.mSv.updateLayoutAlgorithm(false);
            float stackScroll = scroller.getStackScroll();
            if (mutableBoolean.value) {
                stackScroll = scroller.getBoundedStackScroll(stackScroll);
            } else if (z) {
                float stackScrollForTaskIgnoreOverrides = stackAlgorithm.getStackScrollForTaskIgnoreOverrides(findAnchorTask) - f;
                float f2 = stackScrollForTaskIgnoreOverrides;
                if (stackAlgorithm.getFocusState() != 1) {
                    f2 = stackScrollForTaskIgnoreOverrides * 0.75f;
                }
                stackScroll = scroller.getBoundedStackScroll(scroller.getStackScroll() + f2);
            }
            this.mSv.bindVisibleTaskViews(stackScroll, true);
            this.mSv.getLayoutTaskTransforms(stackScroll, 0, this.mCurrentTasks, true, this.mFinalTaskTransforms);
            this.mTargetStackScroll = stackScroll;
        }
    }

    public void onBeginManualDrag(TaskView taskView) {
        this.mActiveTaskView = taskView;
        this.mSwipeHelperAnimations.put(taskView, null);
        onBeginDrag(taskView);
    }

    @Override // com.android.systemui.SwipeHelper.Callback
    public void onChildDismissed(View view) {
        TaskView taskView = (TaskView) view;
        taskView.setClipViewInStack(true);
        taskView.setTouchEnabled(true);
        EventBus.getDefault().send(new TaskViewDismissedEvent(taskView.getTask(), taskView, this.mSwipeHelperAnimations.containsKey(view) ? new AnimationProps(200, Interpolators.FAST_OUT_SLOW_IN) : null));
        if (this.mSwipeHelperAnimations.containsKey(view)) {
            this.mSv.getScroller().setStackScroll(this.mTargetStackScroll, null);
            this.mSv.getStackAlgorithm().setFocusState(0);
            this.mSv.getStackAlgorithm().clearUnfocusedTaskOverrides();
            this.mSwipeHelperAnimations.remove(view);
        }
        MetricsLogger.histogram(taskView.getContext(), "overview_task_dismissed_source", 1);
    }

    @Override // com.android.systemui.SwipeHelper.Callback
    public void onChildSnappedBack(View view, float f) {
        TaskView taskView = (TaskView) view;
        taskView.setClipViewInStack(true);
        taskView.setTouchEnabled(true);
        this.mSv.removeIgnoreTask(taskView.getTask());
        this.mSv.updateLayoutAlgorithm(false);
        this.mSv.relayoutTaskViews(AnimationProps.IMMEDIATE);
        this.mSwipeHelperAnimations.remove(view);
    }

    @Override // com.android.systemui.SwipeHelper.Callback
    public void onDragCancelled(View view) {
    }

    public boolean onGenericMotionEvent(MotionEvent motionEvent) {
        if ((motionEvent.getSource() & 2) == 2) {
            switch (motionEvent.getAction() & 255) {
                case 8:
                    if (motionEvent.getAxisValue(9) > 0.0f) {
                        this.mSv.setRelativeFocusedTask(true, true, false);
                        return true;
                    }
                    this.mSv.setRelativeFocusedTask(false, true, false);
                    return true;
                default:
                    return false;
            }
        }
        return false;
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        this.mInterceptedBySwipeHelper = this.mSwipeHelper.onInterceptTouchEvent(motionEvent);
        if (this.mInterceptedBySwipeHelper) {
            return true;
        }
        return handleTouchEvent(motionEvent);
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (this.mInterceptedBySwipeHelper && this.mSwipeHelper.onTouchEvent(motionEvent)) {
            return true;
        }
        handleTouchEvent(motionEvent);
        return true;
    }

    void recycleVelocityTracker() {
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.recycle();
            this.mVelocityTracker = null;
        }
    }

    @Override // com.android.systemui.SwipeHelper.Callback
    public boolean updateSwipeProgress(View view, boolean z, float f) {
        if (this.mActiveTaskView == view || this.mSwipeHelperAnimations.containsKey(view)) {
            updateTaskViewTransforms(Interpolators.FAST_OUT_SLOW_IN.getInterpolation(f));
            return true;
        }
        return true;
    }
}
