package com.android.systemui.recents.views;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.ActivityOptions;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.ArraySet;
import android.util.AttributeSet;
import android.view.AppTransitionAnimationSpec;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewPropertyAnimator;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.Interpolators;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.RecentsActivityLaunchState;
import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.events.activity.DismissRecentsToHomeAnimationStarted;
import com.android.systemui.recents.events.activity.DockedFirstAnimationFrameEvent;
import com.android.systemui.recents.events.activity.EnterRecentsWindowAnimationCompletedEvent;
import com.android.systemui.recents.events.activity.HideStackActionButtonEvent;
import com.android.systemui.recents.events.activity.LaunchTaskEvent;
import com.android.systemui.recents.events.activity.MultiWindowStateChangedEvent;
import com.android.systemui.recents.events.activity.ShowStackActionButtonEvent;
import com.android.systemui.recents.events.ui.AllTaskViewsDismissedEvent;
import com.android.systemui.recents.events.ui.DismissAllTaskViewsEvent;
import com.android.systemui.recents.events.ui.DraggingInRecentsEndedEvent;
import com.android.systemui.recents.events.ui.DraggingInRecentsEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragDropTargetChangedEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragEndCancelledEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragEndEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragStartEvent;
import com.android.systemui.recents.misc.ReferenceCountedTrigger;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.misc.Utilities;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.model.TaskStack;
import com.android.systemui.recents.views.RecentsTransitionHelper;
import com.android.systemui.stackdivider.WindowManagerProxy;
import com.android.systemui.statusbar.FlingAnimationUtils;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
/* loaded from: a.zip:com/android/systemui/recents/views/RecentsView.class */
public class RecentsView extends FrameLayout {
    private boolean mAwaitingFirstLayout;
    private Drawable mBackgroundScrim;
    private Animator mBackgroundScrimAnimator;
    private int mDividerSize;
    private TextView mEmptyView;
    private final FlingAnimationUtils mFlingAnimationUtils;
    private boolean mLastTaskLaunchedWasFreeform;
    private TaskStack mStack;
    private TextView mStackActionButton;
    @ViewDebug.ExportedProperty(category = "recents")
    private Rect mSystemInsets;
    private TaskStackView mTaskStackView;
    @ViewDebug.ExportedProperty(deepExport = true, prefix = "touch_")
    private RecentsViewTouchHandler mTouchHandler;
    private RecentsTransitionHelper mTransitionHelper;

    public RecentsView(Context context) {
        this(context, null);
    }

    public RecentsView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public RecentsView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public RecentsView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mAwaitingFirstLayout = true;
        this.mSystemInsets = new Rect();
        this.mBackgroundScrim = new ColorDrawable(Color.argb(84, 0, 0, 0)).mutate();
        setWillNotDraw(false);
        SystemServicesProxy systemServices = Recents.getSystemServices();
        this.mTransitionHelper = new RecentsTransitionHelper(getContext());
        this.mDividerSize = systemServices.getDockedDividerSize(context);
        this.mTouchHandler = new RecentsViewTouchHandler(this);
        this.mFlingAnimationUtils = new FlingAnimationUtils(context, 0.3f);
        LayoutInflater from = LayoutInflater.from(context);
        float dimensionPixelSize = context.getResources().getDimensionPixelSize(2131690016);
        this.mStackActionButton = (TextView) from.inflate(2130968781, (ViewGroup) this, false);
        this.mStackActionButton.setOnClickListener(new View.OnClickListener(this) { // from class: com.android.systemui.recents.views.RecentsView.1
            final RecentsView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                EventBus.getDefault().send(new DismissAllTaskViewsEvent());
            }
        });
        addView(this.mStackActionButton);
        this.mStackActionButton.setClipToOutline(true);
        this.mStackActionButton.setOutlineProvider(new ViewOutlineProvider(this, dimensionPixelSize) { // from class: com.android.systemui.recents.views.RecentsView.2
            final RecentsView this$0;
            final float val$cornerRadius;

            {
                this.this$0 = this;
                this.val$cornerRadius = dimensionPixelSize;
            }

            @Override // android.view.ViewOutlineProvider
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), this.val$cornerRadius);
            }
        });
        this.mEmptyView = (TextView) from.inflate(2130968777, (ViewGroup) this, false);
        addView(this.mEmptyView);
    }

    private void animateBackgroundScrim(float f, int i) {
        Utilities.cancelAnimationWithoutCallbacks(this.mBackgroundScrimAnimator);
        int alpha = (int) ((this.mBackgroundScrim.getAlpha() / 84.15f) * 255.0f);
        int i2 = (int) (f * 255.0f);
        this.mBackgroundScrimAnimator = ObjectAnimator.ofInt(this.mBackgroundScrim, Utilities.DRAWABLE_ALPHA, alpha, i2);
        this.mBackgroundScrimAnimator.setDuration(i);
        this.mBackgroundScrimAnimator.setInterpolator(i2 > alpha ? Interpolators.ALPHA_IN : Interpolators.ALPHA_OUT);
        this.mBackgroundScrimAnimator.start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Rect getStackActionButtonBoundsFromStackLayout() {
        Rect rect = new Rect(this.mTaskStackView.mLayoutAlgorithm.mStackActionButtonRect);
        int paddingLeft = isLayoutRtl() ? rect.left - this.mStackActionButton.getPaddingLeft() : (rect.right + this.mStackActionButton.getPaddingRight()) - this.mStackActionButton.getMeasuredWidth();
        int height = rect.top + ((rect.height() - this.mStackActionButton.getMeasuredHeight()) / 2);
        rect.set(paddingLeft, height, this.mStackActionButton.getMeasuredWidth() + paddingLeft, this.mStackActionButton.getMeasuredHeight() + height);
        return rect;
    }

    private Rect getTaskRect(TaskView taskView) {
        int[] locationOnScreen = taskView.getLocationOnScreen();
        int i = locationOnScreen[0];
        int i2 = locationOnScreen[1];
        return new Rect(i, i2, (int) (i + (taskView.getWidth() * taskView.getScaleX())), (int) (i2 + (taskView.getHeight() * taskView.getScaleY())));
    }

    private void hideStackActionButton(int i, boolean z) {
        ReferenceCountedTrigger referenceCountedTrigger = new ReferenceCountedTrigger();
        hideStackActionButton(i, z, referenceCountedTrigger);
        referenceCountedTrigger.flushLastDecrementRunnables();
    }

    private void hideStackActionButton(int i, boolean z, ReferenceCountedTrigger referenceCountedTrigger) {
        if (this.mStackActionButton.getVisibility() == 0) {
            if (z) {
                this.mStackActionButton.animate().translationY((-this.mStackActionButton.getMeasuredHeight()) * 0.25f);
            }
            this.mStackActionButton.animate().alpha(0.0f).setDuration(i).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).withEndAction(new Runnable(this, referenceCountedTrigger) { // from class: com.android.systemui.recents.views.RecentsView.8
                final RecentsView this$0;
                final ReferenceCountedTrigger val$postAnimationTrigger;

                {
                    this.this$0 = this;
                    this.val$postAnimationTrigger = referenceCountedTrigger;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$0.mStackActionButton.setVisibility(4);
                    this.val$postAnimationTrigger.decrement();
                }
            }).start();
            referenceCountedTrigger.increment();
        }
    }

    private void showStackActionButton(int i, boolean z) {
        ReferenceCountedTrigger referenceCountedTrigger = new ReferenceCountedTrigger();
        if (this.mStackActionButton.getVisibility() == 4) {
            this.mStackActionButton.setVisibility(0);
            this.mStackActionButton.setAlpha(0.0f);
            if (z) {
                this.mStackActionButton.setTranslationY((-this.mStackActionButton.getMeasuredHeight()) * 0.25f);
            } else {
                this.mStackActionButton.setTranslationY(0.0f);
            }
            referenceCountedTrigger.addLastDecrementRunnable(new Runnable(this, z, i) { // from class: com.android.systemui.recents.views.RecentsView.7
                final RecentsView this$0;
                final int val$duration;
                final boolean val$translate;

                {
                    this.this$0 = this;
                    this.val$translate = z;
                    this.val$duration = i;
                }

                @Override // java.lang.Runnable
                public void run() {
                    if (this.val$translate) {
                        this.this$0.mStackActionButton.animate().translationY(0.0f);
                    }
                    this.this$0.mStackActionButton.animate().alpha(1.0f).setDuration(this.val$duration).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).start();
                }
            });
        }
        referenceCountedTrigger.flushLastDecrementRunnables();
    }

    private void updateVisibleDockRegions(TaskStack.DockState[] dockStateArr, boolean z, int i, int i2, boolean z2, boolean z3) {
        ArraySet arrayToSet = Utilities.arrayToSet(dockStateArr, new ArraySet());
        ArrayList<TaskStack.DockState> visibleDockStates = this.mTouchHandler.getVisibleDockStates();
        for (int size = visibleDockStates.size() - 1; size >= 0; size--) {
            TaskStack.DockState dockState = visibleDockStates.get(size);
            TaskStack.DockState.ViewState viewState = dockState.viewState;
            if (dockStateArr == null || !arrayToSet.contains(dockState)) {
                viewState.startAnimation(null, 0, 0, 250, Interpolators.FAST_OUT_SLOW_IN, z2, z3);
            } else {
                int i3 = i != -1 ? i : viewState.dockAreaAlpha;
                int i4 = i2 != -1 ? i2 : viewState.hintTextAlpha;
                Rect preDockedBounds = z ? dockState.getPreDockedBounds(getMeasuredWidth(), getMeasuredHeight()) : dockState.getDockedBounds(getMeasuredWidth(), getMeasuredHeight(), this.mDividerSize, this.mSystemInsets, getResources());
                if (viewState.dockAreaOverlay.getCallback() != this) {
                    viewState.dockAreaOverlay.setCallback(this);
                    viewState.dockAreaOverlay.setBounds(preDockedBounds);
                }
                viewState.startAnimation(preDockedBounds, i3, i4, 250, Interpolators.FAST_OUT_SLOW_IN, z2, z3);
            }
        }
    }

    public void dump(String str, PrintWriter printWriter) {
        String str2 = str + "  ";
        String hexString = Integer.toHexString(System.identityHashCode(this));
        printWriter.print(str);
        printWriter.print("RecentsView");
        printWriter.print(" awaitingFirstLayout=");
        printWriter.print(this.mAwaitingFirstLayout ? "Y" : "N");
        printWriter.print(" insets=");
        printWriter.print(Utilities.dumpRect(this.mSystemInsets));
        printWriter.print(" [0x");
        printWriter.print(hexString);
        printWriter.print("]");
        printWriter.println();
        if (this.mStack != null) {
            this.mStack.dump(str2, printWriter);
        }
        if (this.mTaskStackView != null) {
            this.mTaskStackView.dump(str2, printWriter);
        }
    }

    public Drawable getBackgroundScrim() {
        return this.mBackgroundScrim;
    }

    public TaskStack getStack() {
        return this.mStack;
    }

    public void hideEmptyView() {
        this.mEmptyView.setVisibility(4);
        this.mTaskStackView.setVisibility(0);
        this.mTaskStackView.bringToFront();
        this.mStackActionButton.bringToFront();
    }

    public boolean isLastTaskLaunchedFreeform() {
        return this.mLastTaskLaunchedWasFreeform;
    }

    public boolean launchFocusedTask(int i) {
        Task focusedTask;
        if (this.mTaskStackView == null || (focusedTask = this.mTaskStackView.getFocusedTask()) == null) {
            return false;
        }
        EventBus.getDefault().send(new LaunchTaskEvent(this.mTaskStackView.getChildViewForTask(focusedTask), focusedTask, null, -1, false));
        if (i != 0) {
            MetricsLogger.action(getContext(), i, focusedTask.key.getComponent().toString());
            return true;
        }
        return true;
    }

    public boolean launchPreviousTask() {
        Task launchTarget;
        if (this.mTaskStackView == null || (launchTarget = this.mTaskStackView.getStack().getLaunchTarget()) == null) {
            return false;
        }
        EventBus.getDefault().send(new LaunchTaskEvent(this.mTaskStackView.getChildViewForTask(launchTarget), launchTarget, null, -1, false));
        return true;
    }

    @Override // android.view.View
    public WindowInsets onApplyWindowInsets(WindowInsets windowInsets) {
        this.mSystemInsets.set(windowInsets.getSystemWindowInsets());
        this.mTaskStackView.setSystemInsets(this.mSystemInsets);
        requestLayout();
        return windowInsets;
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        EventBus.getDefault().register(this, 3);
        EventBus.getDefault().register(this.mTouchHandler, 4);
        super.onAttachedToWindow();
    }

    public final void onBusEvent(DismissRecentsToHomeAnimationStarted dismissRecentsToHomeAnimationStarted) {
        hideStackActionButton(200, false);
        animateBackgroundScrim(0.0f, 200);
    }

    public final void onBusEvent(EnterRecentsWindowAnimationCompletedEvent enterRecentsWindowAnimationCompletedEvent) {
        RecentsActivityLaunchState launchState = Recents.getConfiguration().getLaunchState();
        if (launchState.launchedViaDockGesture || launchState.launchedFromApp || this.mStack.getTaskCount() <= 0) {
            return;
        }
        animateBackgroundScrim(1.0f, 300);
    }

    public final void onBusEvent(HideStackActionButtonEvent hideStackActionButtonEvent) {
        hideStackActionButton(100, true);
    }

    public final void onBusEvent(LaunchTaskEvent launchTaskEvent) {
        this.mLastTaskLaunchedWasFreeform = launchTaskEvent.task.isFreeformTask();
        this.mTransitionHelper.launchTaskFromRecents(this.mStack, launchTaskEvent.task, this.mTaskStackView, launchTaskEvent.taskView, launchTaskEvent.screenPinningRequested, launchTaskEvent.targetTaskBounds, launchTaskEvent.targetTaskStack);
    }

    public final void onBusEvent(MultiWindowStateChangedEvent multiWindowStateChangedEvent) {
        updateStack(multiWindowStateChangedEvent.stack, false);
    }

    public final void onBusEvent(ShowStackActionButtonEvent showStackActionButtonEvent) {
        showStackActionButton(134, showStackActionButtonEvent.translate);
    }

    public final void onBusEvent(AllTaskViewsDismissedEvent allTaskViewsDismissedEvent) {
        hideStackActionButton(100, true);
    }

    public final void onBusEvent(DismissAllTaskViewsEvent dismissAllTaskViewsEvent) {
        if (Recents.getSystemServices().hasDockedTask()) {
            return;
        }
        animateBackgroundScrim(0.0f, 200);
    }

    public final void onBusEvent(DraggingInRecentsEndedEvent draggingInRecentsEndedEvent) {
        ViewPropertyAnimator animate = animate();
        if (draggingInRecentsEndedEvent.velocity > this.mFlingAnimationUtils.getMinVelocityPxPerSecond()) {
            animate.translationY(getHeight());
            animate.withEndAction(new Runnable(this) { // from class: com.android.systemui.recents.views.RecentsView.6
                final RecentsView this$0;

                {
                    this.this$0 = this;
                }

                @Override // java.lang.Runnable
                public void run() {
                    WindowManagerProxy.getInstance().maximizeDockedStack();
                }
            });
            this.mFlingAnimationUtils.apply(animate, getTranslationY(), getHeight(), draggingInRecentsEndedEvent.velocity);
        } else {
            animate.translationY(0.0f);
            animate.setListener(null);
            this.mFlingAnimationUtils.apply(animate, getTranslationY(), 0.0f, draggingInRecentsEndedEvent.velocity);
        }
        animate.start();
    }

    public final void onBusEvent(DraggingInRecentsEvent draggingInRecentsEvent) {
        if (this.mTaskStackView.getTaskViews().size() > 0) {
            setTranslationY(draggingInRecentsEvent.distanceFromTop - this.mTaskStackView.getTaskViews().get(0).getY());
        }
    }

    public final void onBusEvent(DragDropTargetChangedEvent dragDropTargetChangedEvent) {
        if (dragDropTargetChangedEvent.dropTarget == null || !(dragDropTargetChangedEvent.dropTarget instanceof TaskStack.DockState)) {
            updateVisibleDockRegions(this.mTouchHandler.getDockStatesForCurrentOrientation(), true, TaskStack.DockState.NONE.viewState.dockAreaAlpha, TaskStack.DockState.NONE.viewState.hintTextAlpha, true, true);
        } else {
            updateVisibleDockRegions(new TaskStack.DockState[]{(TaskStack.DockState) dragDropTargetChangedEvent.dropTarget}, false, -1, -1, true, true);
        }
        if (this.mStackActionButton != null) {
            dragDropTargetChangedEvent.addPostAnimationCallback(new Runnable(this) { // from class: com.android.systemui.recents.views.RecentsView.3
                final RecentsView this$0;

                {
                    this.this$0 = this;
                }

                @Override // java.lang.Runnable
                public void run() {
                    Rect stackActionButtonBoundsFromStackLayout = this.this$0.getStackActionButtonBoundsFromStackLayout();
                    this.this$0.mStackActionButton.setLeftTopRightBottom(stackActionButtonBoundsFromStackLayout.left, stackActionButtonBoundsFromStackLayout.top, stackActionButtonBoundsFromStackLayout.right, stackActionButtonBoundsFromStackLayout.bottom);
                }
            });
        }
    }

    public final void onBusEvent(DragEndCancelledEvent dragEndCancelledEvent) {
        updateVisibleDockRegions(null, true, -1, -1, true, false);
    }

    public final void onBusEvent(DragEndEvent dragEndEvent) {
        if (dragEndEvent.dropTarget instanceof TaskStack.DockState) {
            TaskStack.DockState dockState = (TaskStack.DockState) dragEndEvent.dropTarget;
            updateVisibleDockRegions(null, false, -1, -1, false, false);
            Utilities.setViewFrameFromTranslation(dragEndEvent.taskView);
            SystemServicesProxy systemServices = Recents.getSystemServices();
            if (systemServices.startTaskInDockedMode(dragEndEvent.task.key.id, dockState.createMode)) {
                ActivityOptions.OnAnimationStartedListener onAnimationStartedListener = new ActivityOptions.OnAnimationStartedListener(this, dragEndEvent) { // from class: com.android.systemui.recents.views.RecentsView.4
                    final RecentsView this$0;
                    final DragEndEvent val$event;

                    {
                        this.this$0 = this;
                        this.val$event = dragEndEvent;
                    }

                    public void onAnimationStarted() {
                        EventBus.getDefault().send(new DockedFirstAnimationFrameEvent());
                        this.this$0.mTaskStackView.getStack().removeTask(this.val$event.task, null, true);
                    }
                };
                systemServices.overridePendingAppTransitionMultiThumbFuture(this.mTransitionHelper.getAppTransitionFuture(new RecentsTransitionHelper.AnimationSpecComposer(this, dragEndEvent, getTaskRect(dragEndEvent.taskView)) { // from class: com.android.systemui.recents.views.RecentsView.5
                    final RecentsView this$0;
                    final DragEndEvent val$event;
                    final Rect val$taskRect;

                    {
                        this.this$0 = this;
                        this.val$event = dragEndEvent;
                        this.val$taskRect = r6;
                    }

                    @Override // com.android.systemui.recents.views.RecentsTransitionHelper.AnimationSpecComposer
                    public List<AppTransitionAnimationSpec> composeSpecs() {
                        return this.this$0.mTransitionHelper.composeDockAnimationSpec(this.val$event.taskView, this.val$taskRect);
                    }
                }), this.mTransitionHelper.wrapStartedListener(onAnimationStartedListener), true);
                MetricsLogger.action(this.mContext, 270, dragEndEvent.task.getTopComponent().flattenToShortString());
            } else {
                EventBus.getDefault().send(new DragEndCancelledEvent(this.mStack, dragEndEvent.task, dragEndEvent.taskView));
            }
        } else {
            updateVisibleDockRegions(null, true, -1, -1, true, false);
        }
        if (this.mStackActionButton != null) {
            this.mStackActionButton.animate().alpha(1.0f).setDuration(134L).setInterpolator(Interpolators.ALPHA_IN).start();
        }
    }

    public final void onBusEvent(DragStartEvent dragStartEvent) {
        updateVisibleDockRegions(this.mTouchHandler.getDockStatesForCurrentOrientation(), true, TaskStack.DockState.NONE.viewState.dockAreaAlpha, TaskStack.DockState.NONE.viewState.hintTextAlpha, true, false);
        if (this.mStackActionButton != null) {
            this.mStackActionButton.animate().alpha(0.0f).setDuration(100L).setInterpolator(Interpolators.ALPHA_OUT).start();
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
        EventBus.getDefault().unregister(this.mTouchHandler);
    }

    @Override // android.view.View
    public void onDrawForeground(Canvas canvas) {
        super.onDrawForeground(canvas);
        ArrayList<TaskStack.DockState> visibleDockStates = this.mTouchHandler.getVisibleDockStates();
        for (int size = visibleDockStates.size() - 1; size >= 0; size--) {
            visibleDockStates.get(size).viewState.draw(canvas);
        }
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        return this.mTouchHandler.onInterceptTouchEvent(motionEvent);
    }

    @Override // android.widget.FrameLayout, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        if (this.mTaskStackView.getVisibility() != 8) {
            this.mTaskStackView.layout(i, i2, getMeasuredWidth() + i, getMeasuredHeight() + i2);
        }
        if (this.mEmptyView.getVisibility() != 8) {
            int i5 = this.mSystemInsets.left;
            int i6 = this.mSystemInsets.right;
            int i7 = this.mSystemInsets.top;
            int i8 = this.mSystemInsets.bottom;
            int measuredWidth = this.mEmptyView.getMeasuredWidth();
            int measuredHeight = this.mEmptyView.getMeasuredHeight();
            int max = this.mSystemInsets.left + i + (Math.max(0, ((i3 - i) - (i5 + i6)) - measuredWidth) / 2);
            int max2 = this.mSystemInsets.top + i2 + (Math.max(0, ((i4 - i2) - (i7 + i8)) - measuredHeight) / 2);
            this.mEmptyView.layout(max, max2, max + measuredWidth, max2 + measuredHeight);
        }
        Rect stackActionButtonBoundsFromStackLayout = getStackActionButtonBoundsFromStackLayout();
        this.mStackActionButton.layout(stackActionButtonBoundsFromStackLayout.left, stackActionButtonBoundsFromStackLayout.top, stackActionButtonBoundsFromStackLayout.right, stackActionButtonBoundsFromStackLayout.bottom);
        if (this.mAwaitingFirstLayout) {
            this.mAwaitingFirstLayout = false;
            if (Recents.getConfiguration().getLaunchState().launchedViaDragGesture) {
                setTranslationY(getMeasuredHeight());
            } else {
                setTranslationY(0.0f);
            }
        }
    }

    @Override // android.widget.FrameLayout, android.view.View
    protected void onMeasure(int i, int i2) {
        int size = View.MeasureSpec.getSize(i);
        int size2 = View.MeasureSpec.getSize(i2);
        if (this.mTaskStackView.getVisibility() != 8) {
            this.mTaskStackView.measure(i, i2);
        }
        if (this.mEmptyView.getVisibility() != 8) {
            measureChild(this.mEmptyView, View.MeasureSpec.makeMeasureSpec(size, Integer.MIN_VALUE), View.MeasureSpec.makeMeasureSpec(size2, Integer.MIN_VALUE));
        }
        Rect rect = this.mTaskStackView.mLayoutAlgorithm.mStackActionButtonRect;
        measureChild(this.mStackActionButton, View.MeasureSpec.makeMeasureSpec(rect.width(), Integer.MIN_VALUE), View.MeasureSpec.makeMeasureSpec(rect.height(), Integer.MIN_VALUE));
        setMeasuredDimension(size, size2);
    }

    public void onReload(boolean z, boolean z2) {
        RecentsActivityLaunchState launchState = Recents.getConfiguration().getLaunchState();
        if (this.mTaskStackView == null) {
            z = false;
            this.mTaskStackView = new TaskStackView(getContext());
            this.mTaskStackView.setSystemInsets(this.mSystemInsets);
            addView(this.mTaskStackView);
        }
        this.mAwaitingFirstLayout = !z;
        this.mLastTaskLaunchedWasFreeform = false;
        this.mTaskStackView.onReload(z);
        if (z) {
            animateBackgroundScrim(1.0f, 200);
        } else if (launchState.launchedViaDockGesture || launchState.launchedFromApp || z2) {
            this.mBackgroundScrim.setAlpha(255);
        } else {
            this.mBackgroundScrim.setAlpha(0);
        }
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        return this.mTouchHandler.onTouchEvent(motionEvent);
    }

    public void showEmptyView(int i) {
        this.mTaskStackView.setVisibility(4);
        this.mEmptyView.setText(i);
        this.mEmptyView.setVisibility(0);
        this.mEmptyView.bringToFront();
        this.mStackActionButton.bringToFront();
    }

    public void updateStack(TaskStack taskStack, boolean z) {
        this.mStack = taskStack;
        if (z) {
            this.mTaskStackView.setTasks(taskStack, true);
        }
        if (taskStack.getTaskCount() > 0) {
            hideEmptyView();
        } else {
            showEmptyView(2131493579);
        }
    }

    @Override // android.view.View
    protected boolean verifyDrawable(Drawable drawable) {
        ArrayList<TaskStack.DockState> visibleDockStates = this.mTouchHandler.getVisibleDockStates();
        for (int size = visibleDockStates.size() - 1; size >= 0; size--) {
            if (visibleDockStates.get(size).viewState.dockAreaOverlay == drawable) {
                return true;
            }
        }
        return super.verifyDrawable(drawable);
    }
}
