package com.android.systemui.recents.views;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Outline;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.FloatProperty;
import android.util.Property;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewOutlineProvider;
import android.widget.TextView;
import android.widget.Toast;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.Interpolators;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.RecentsConfiguration;
import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.events.activity.LaunchTaskEvent;
import com.android.systemui.recents.events.ui.DismissTaskViewEvent;
import com.android.systemui.recents.events.ui.TaskViewDismissedEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragEndCancelledEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragEndEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragStartEvent;
import com.android.systemui.recents.misc.ReferenceCountedTrigger;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.misc.Utilities;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.model.TaskStack;
import java.util.ArrayList;
/* loaded from: a.zip:com/android/systemui/recents/views/TaskView.class */
public class TaskView extends FixedSizeFrameLayout implements Task.TaskCallbacks, View.OnClickListener, View.OnLongClickListener {
    private float mActionButtonTranslationZ;
    private View mActionButtonView;
    private TaskViewCallbacks mCb;
    @ViewDebug.ExportedProperty(category = "recents")
    private boolean mClipViewInStack;
    @ViewDebug.ExportedProperty(category = "recents")
    private float mDimAlpha;
    private ObjectAnimator mDimAnimator;
    private Toast mDisabledAppToast;
    @ViewDebug.ExportedProperty(category = "recents")
    private Point mDownTouchPos;
    @ViewDebug.ExportedProperty(deepExport = true, prefix = "header_")
    TaskViewHeader mHeaderView;
    private View mIncompatibleAppToastView;
    @ViewDebug.ExportedProperty(category = "recents")
    private boolean mIsDisabledInSafeMode;
    private ObjectAnimator mOutlineAnimator;
    private final TaskViewTransform mTargetAnimationTransform;
    @ViewDebug.ExportedProperty(deepExport = true, prefix = "task_")
    private Task mTask;
    @ViewDebug.ExportedProperty(deepExport = true, prefix = "thumbnail_")
    TaskViewThumbnail mThumbnailView;
    private ArrayList<Animator> mTmpAnimators;
    @ViewDebug.ExportedProperty(category = "recents")
    private boolean mTouchExplorationEnabled;
    private AnimatorSet mTransformAnimation;
    @ViewDebug.ExportedProperty(deepExport = true, prefix = "view_bounds_")
    private AnimateableViewBounds mViewBounds;
    public static final Property<TaskView, Float> DIM_ALPHA_WITHOUT_HEADER = new FloatProperty<TaskView>("dimAlphaWithoutHeader") { // from class: com.android.systemui.recents.views.TaskView.1
        @Override // android.util.Property
        public Float get(TaskView taskView) {
            return Float.valueOf(taskView.getDimAlpha());
        }

        @Override // android.util.FloatProperty
        public void setValue(TaskView taskView, float f) {
            taskView.setDimAlphaWithoutHeader(f);
        }
    };
    public static final Property<TaskView, Float> DIM_ALPHA = new FloatProperty<TaskView>("dimAlpha") { // from class: com.android.systemui.recents.views.TaskView.2
        @Override // android.util.Property
        public Float get(TaskView taskView) {
            return Float.valueOf(taskView.getDimAlpha());
        }

        @Override // android.util.FloatProperty
        public void setValue(TaskView taskView, float f) {
            taskView.setDimAlpha(f);
        }
    };
    public static final Property<TaskView, Float> VIEW_OUTLINE_ALPHA = new FloatProperty<TaskView>("viewOutlineAlpha") { // from class: com.android.systemui.recents.views.TaskView.3
        @Override // android.util.Property
        public Float get(TaskView taskView) {
            return Float.valueOf(taskView.getViewBounds().getAlpha());
        }

        @Override // android.util.FloatProperty
        public void setValue(TaskView taskView, float f) {
            taskView.getViewBounds().setAlpha(f);
        }
    };

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: a.zip:com/android/systemui/recents/views/TaskView$TaskViewCallbacks.class */
    public interface TaskViewCallbacks {
        void onTaskViewClipStateChanged(TaskView taskView);
    }

    public TaskView(Context context) {
        this(context, null);
    }

    public TaskView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public TaskView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public TaskView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mClipViewInStack = true;
        this.mTargetAnimationTransform = new TaskViewTransform();
        this.mTmpAnimators = new ArrayList<>();
        this.mDownTouchPos = new Point();
        RecentsConfiguration configuration = Recents.getConfiguration();
        Resources resources = context.getResources();
        this.mViewBounds = new AnimateableViewBounds(this, resources.getDimensionPixelSize(2131690017));
        if (configuration.fakeShadows) {
            setBackground(new FakeShadowDrawable(resources, configuration));
        }
        setOutlineProvider(this.mViewBounds);
        setOnLongClickListener(this);
    }

    /* renamed from: -com_android_systemui_recents_views_TaskView_lambda$1  reason: not valid java name */
    /* synthetic */ void m1307com_android_systemui_recents_views_TaskView_lambda$1() {
        setClipViewInStack(true);
    }

    /* renamed from: -com_android_systemui_recents_views_TaskView_lambda$2  reason: not valid java name */
    /* synthetic */ void m1308com_android_systemui_recents_views_TaskView_lambda$2() {
        setClipViewInStack(true);
    }

    public void cancelTransformAnimation() {
        Utilities.cancelAnimationWithoutCallbacks(this.mTransformAnimation);
        Utilities.cancelAnimationWithoutCallbacks(this.mDimAnimator);
        Utilities.cancelAnimationWithoutCallbacks(this.mOutlineAnimator);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void dismissTask() {
        DismissTaskViewEvent dismissTaskViewEvent = new DismissTaskViewEvent(this);
        dismissTaskViewEvent.addPostAnimationCallback(new Runnable(this, this) { // from class: com.android.systemui.recents.views.TaskView.5
            final TaskView this$0;
            final TaskView val$tv;

            {
                this.this$0 = this;
                this.val$tv = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                EventBus.getDefault().send(new TaskViewDismissedEvent(this.this$0.mTask, this.val$tv, new AnimationProps(200, Interpolators.FAST_OUT_SLOW_IN)));
            }
        });
        EventBus.getDefault().send(dismissTaskViewEvent);
    }

    public float getDimAlpha() {
        return this.mDimAlpha;
    }

    public TaskViewHeader getHeaderView() {
        return this.mHeaderView;
    }

    public Task getTask() {
        return this.mTask;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public AnimateableViewBounds getViewBounds() {
        return this.mViewBounds;
    }

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }

    public void hideActionButton(boolean z, int i, boolean z2, Animator.AnimatorListener animatorListener) {
        if (z && this.mActionButtonView.getAlpha() > 0.0f) {
            if (z2) {
                this.mActionButtonView.animate().scaleX(0.9f).scaleY(0.9f);
            }
            this.mActionButtonView.animate().alpha(0.0f).setDuration(i).setInterpolator(Interpolators.ALPHA_OUT).withEndAction(new Runnable(this, animatorListener) { // from class: com.android.systemui.recents.views.TaskView.6
                final TaskView this$0;
                final Animator.AnimatorListener val$animListener;

                {
                    this.this$0 = this;
                    this.val$animListener = animatorListener;
                }

                @Override // java.lang.Runnable
                public void run() {
                    if (this.val$animListener != null) {
                        this.val$animListener.onAnimationEnd(null);
                    }
                    this.this$0.mActionButtonView.setVisibility(4);
                }
            }).start();
            return;
        }
        this.mActionButtonView.setAlpha(0.0f);
        this.mActionButtonView.setVisibility(4);
        if (animatorListener != null) {
            animatorListener.onAnimationEnd(null);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean isAnimatingTo(TaskViewTransform taskViewTransform) {
        return (this.mTransformAnimation == null || !this.mTransformAnimation.isStarted()) ? false : this.mTargetAnimationTransform.isSame(taskViewTransform);
    }

    @Override // com.android.systemui.recents.views.FixedSizeFrameLayout
    protected void measureContents(int i, int i2) {
        int i3 = this.mPaddingLeft;
        int i4 = this.mPaddingRight;
        int i5 = this.mPaddingTop;
        measureChildren(View.MeasureSpec.makeMeasureSpec((i - i3) - i4, 1073741824), View.MeasureSpec.makeMeasureSpec((i2 - i5) - this.mPaddingBottom, 1073741824));
        setMeasuredDimension(i, i2);
    }

    public final void onBusEvent(DragEndCancelledEvent dragEndCancelledEvent) {
        dragEndCancelledEvent.addPostAnimationCallback(new Runnable(this) { // from class: com.android.systemui.recents.views.TaskView._void_onBusEvent_com_android_systemui_recents_events_ui_dragndrop_DragEndCancelledEvent_event_LambdaImpl0
            private TaskView val$this;

            {
                this.val$this = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.val$this.m1308com_android_systemui_recents_views_TaskView_lambda$2();
            }
        });
    }

    public final void onBusEvent(DragEndEvent dragEndEvent) {
        if (!(dragEndEvent.dropTarget instanceof TaskStack.DockState)) {
            dragEndEvent.addPostAnimationCallback(new Runnable(this) { // from class: com.android.systemui.recents.views.TaskView._void_onBusEvent_com_android_systemui_recents_events_ui_dragndrop_DragEndEvent_event_LambdaImpl0
                private TaskView val$this;

                {
                    this.val$this = this;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.val$this.m1307com_android_systemui_recents_views_TaskView_lambda$1();
                }
            });
        }
        EventBus.getDefault().unregister(this);
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        if (this.mIsDisabledInSafeMode) {
            Context context = getContext();
            String string = context.getString(2131493585, this.mTask.title);
            if (this.mDisabledAppToast != null) {
                this.mDisabledAppToast.cancel();
            }
            this.mDisabledAppToast = Toast.makeText(context, string, 0);
            this.mDisabledAppToast.show();
            return;
        }
        boolean z = false;
        if (view == this.mActionButtonView) {
            this.mActionButtonView.setTranslationZ(0.0f);
            z = true;
        }
        EventBus.getDefault().send(new LaunchTaskEvent(this, this.mTask, null, -1, z));
        MetricsLogger.action(view.getContext(), 277, this.mTask.key.getComponent().toString());
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void onConfigurationChanged() {
        this.mHeaderView.onConfigurationChanged();
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        this.mHeaderView = (TaskViewHeader) findViewById(2131886617);
        this.mThumbnailView = (TaskViewThumbnail) findViewById(2131886613);
        this.mThumbnailView.updateClipToTaskBar(this.mHeaderView);
        this.mActionButtonView = findViewById(2131886614);
        this.mActionButtonView.setOutlineProvider(new ViewOutlineProvider(this) { // from class: com.android.systemui.recents.views.TaskView.4
            final TaskView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.ViewOutlineProvider
            public void getOutline(View view, Outline outline) {
                outline.setOval(0, 0, this.this$0.mActionButtonView.getWidth(), this.this$0.mActionButtonView.getHeight());
                outline.setAlpha(0.35f);
            }
        });
        this.mActionButtonView.setOnClickListener(this);
        this.mActionButtonTranslationZ = this.mActionButtonView.getTranslationZ();
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getAction() == 0) {
            this.mDownTouchPos.set((int) (motionEvent.getX() * getScaleX()), (int) (motionEvent.getY() * getScaleY()));
        }
        return super.onInterceptTouchEvent(motionEvent);
    }

    @Override // android.view.View.OnLongClickListener
    public boolean onLongClick(View view) {
        SystemServicesProxy systemServices = Recents.getSystemServices();
        Rect rect = new Rect(this.mViewBounds.mClipBounds);
        rect.scale(getScaleX());
        boolean contains = rect.contains(this.mDownTouchPos.x, this.mDownTouchPos.y);
        if (view == this && contains && !systemServices.hasDockedTask()) {
            setClipViewInStack(false);
            Point point = this.mDownTouchPos;
            point.x = (int) (point.x + (((1.0f - getScaleX()) * getWidth()) / 2.0f));
            Point point2 = this.mDownTouchPos;
            point2.y = (int) (point2.y + (((1.0f - getScaleY()) * getHeight()) / 2.0f));
            EventBus.getDefault().register(this, 3);
            EventBus.getDefault().send(new DragStartEvent(this.mTask, this, this.mDownTouchPos));
            return true;
        }
        return false;
    }

    public void onPrepareLaunchTargetForEnterAnimation() {
        setDimAlphaWithoutHeader(0.0f);
        this.mActionButtonView.setAlpha(0.0f);
        if (this.mIncompatibleAppToastView == null || this.mIncompatibleAppToastView.getVisibility() != 0) {
            return;
        }
        this.mIncompatibleAppToastView.setAlpha(0.0f);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void onReload(boolean z) {
        resetNoUserInteractionState();
        if (z) {
            return;
        }
        resetViewProperties();
    }

    @Override // android.view.View
    protected void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        if (i <= 0 || i2 <= 0) {
            return;
        }
        this.mHeaderView.onTaskViewSizeChanged(i, i2);
        this.mThumbnailView.onTaskViewSizeChanged(i, i2);
        this.mActionButtonView.setTranslationX(i - getMeasuredWidth());
        this.mActionButtonView.setTranslationY(i2 - getMeasuredHeight());
    }

    public void onStartFrontTaskEnterAnimation(boolean z) {
        if (z) {
            showActionButton(false, 0);
        }
    }

    public void onStartLaunchTargetEnterAnimation(TaskViewTransform taskViewTransform, int i, boolean z, ReferenceCountedTrigger referenceCountedTrigger) {
        Utilities.cancelAnimationWithoutCallbacks(this.mDimAnimator);
        referenceCountedTrigger.increment();
        this.mDimAnimator = (ObjectAnimator) new AnimationProps(i, Interpolators.ALPHA_OUT).apply(7, ObjectAnimator.ofFloat(this, DIM_ALPHA_WITHOUT_HEADER, getDimAlpha(), taskViewTransform.dimAlpha));
        this.mDimAnimator.addListener(referenceCountedTrigger.decrementOnAnimationEnd());
        this.mDimAnimator.start();
        if (z) {
            showActionButton(true, i);
        }
        if (this.mIncompatibleAppToastView == null || this.mIncompatibleAppToastView.getVisibility() != 0) {
            return;
        }
        this.mIncompatibleAppToastView.animate().alpha(1.0f).setDuration(i).setInterpolator(Interpolators.ALPHA_IN).start();
    }

    public void onStartLaunchTargetLaunchAnimation(int i, boolean z, ReferenceCountedTrigger referenceCountedTrigger) {
        Utilities.cancelAnimationWithoutCallbacks(this.mDimAnimator);
        this.mDimAnimator = (ObjectAnimator) new AnimationProps(i, Interpolators.ALPHA_OUT).apply(7, ObjectAnimator.ofFloat(this, DIM_ALPHA, getDimAlpha(), 0.0f));
        this.mDimAnimator.start();
        referenceCountedTrigger.increment();
        hideActionButton(true, i, !z, referenceCountedTrigger.decrementOnAnimationEnd());
    }

    public void onTaskBound(Task task, boolean z, int i, Rect rect) {
        SystemServicesProxy systemServices = Recents.getSystemServices();
        this.mTouchExplorationEnabled = z;
        this.mTask = task;
        this.mTask.addCallback(this);
        this.mIsDisabledInSafeMode = !this.mTask.isSystemApp ? systemServices.isInSafeMode() : false;
        this.mThumbnailView.bindToTask(this.mTask, this.mIsDisabledInSafeMode, i, rect);
        this.mHeaderView.bindToTask(this.mTask, this.mTouchExplorationEnabled, this.mIsDisabledInSafeMode);
        if (task.isDockable || !systemServices.hasDockedTask()) {
            if (this.mIncompatibleAppToastView != null) {
                this.mIncompatibleAppToastView.setVisibility(4);
                return;
            }
            return;
        }
        if (this.mIncompatibleAppToastView == null) {
            this.mIncompatibleAppToastView = Utilities.findViewStubById(this, 2131886615).inflate();
            ((TextView) findViewById(16908299)).setText(2131493587);
        }
        this.mIncompatibleAppToastView.setVisibility(0);
    }

    @Override // com.android.systemui.recents.model.Task.TaskCallbacks
    public void onTaskDataLoaded(Task task, ActivityManager.TaskThumbnailInfo taskThumbnailInfo) {
        this.mThumbnailView.onTaskDataLoaded(taskThumbnailInfo);
        this.mHeaderView.onTaskDataLoaded();
    }

    @Override // com.android.systemui.recents.model.Task.TaskCallbacks
    public void onTaskDataUnloaded() {
        this.mTask.removeCallback(this);
        this.mThumbnailView.unbindFromTask();
        this.mHeaderView.unbindFromTask(this.mTouchExplorationEnabled);
    }

    @Override // com.android.systemui.recents.model.Task.TaskCallbacks
    public void onTaskStackIdChanged() {
        this.mHeaderView.bindToTask(this.mTask, this.mTouchExplorationEnabled, this.mIsDisabledInSafeMode);
        this.mHeaderView.onTaskDataLoaded();
    }

    void resetNoUserInteractionState() {
        this.mHeaderView.resetNoUserInteractionState();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void resetViewProperties() {
        cancelTransformAnimation();
        setDimAlpha(0.0f);
        setVisibility(0);
        getViewBounds().reset();
        getHeaderView().reset();
        TaskViewTransform.reset(this);
        this.mActionButtonView.setScaleX(1.0f);
        this.mActionButtonView.setScaleY(1.0f);
        this.mActionButtonView.setAlpha(0.0f);
        this.mActionButtonView.setTranslationX(0.0f);
        this.mActionButtonView.setTranslationY(0.0f);
        this.mActionButtonView.setTranslationZ(this.mActionButtonTranslationZ);
        if (this.mIncompatibleAppToastView != null) {
            this.mIncompatibleAppToastView.setVisibility(4);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setCallbacks(TaskViewCallbacks taskViewCallbacks) {
        this.mCb = taskViewCallbacks;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setClipViewInStack(boolean z) {
        if (z != this.mClipViewInStack) {
            this.mClipViewInStack = z;
            if (this.mCb != null) {
                this.mCb.onTaskViewClipStateChanged(this);
            }
        }
    }

    public void setDimAlpha(float f) {
        this.mDimAlpha = f;
        this.mThumbnailView.setDimAlpha(f);
        this.mHeaderView.setDimAlpha(f);
    }

    public void setDimAlphaWithoutHeader(float f) {
        this.mDimAlpha = f;
        this.mThumbnailView.setDimAlpha(f);
    }

    public void setFocusedState(boolean z, boolean z2) {
        if (z) {
            if (!z2 || isFocused()) {
                return;
            }
            requestFocus();
        } else if (isAccessibilityFocused() && this.mTouchExplorationEnabled) {
            clearAccessibilityFocus();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setNoUserInteractionState() {
        this.mHeaderView.setNoUserInteractionState();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setTouchEnabled(boolean z) {
        setOnClickListener(z ? this : null);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean shouldClipViewInStack() {
        if (this.mTask.isFreeformTask() || getVisibility() != 0) {
            return false;
        }
        return this.mClipViewInStack;
    }

    public void showActionButton(boolean z, int i) {
        this.mActionButtonView.setVisibility(0);
        if (z && this.mActionButtonView.getAlpha() < 1.0f) {
            this.mActionButtonView.animate().alpha(1.0f).scaleX(1.0f).scaleY(1.0f).setDuration(i).setInterpolator(Interpolators.ALPHA_IN).start();
            return;
        }
        this.mActionButtonView.setScaleX(1.0f);
        this.mActionButtonView.setScaleY(1.0f);
        this.mActionButtonView.setAlpha(1.0f);
        this.mActionButtonView.setTranslationZ(this.mActionButtonTranslationZ);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void startNoUserInteractionAnimation() {
        this.mHeaderView.startNoUserInteractionAnimation();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void updateViewPropertiesToTaskTransform(TaskViewTransform taskViewTransform, AnimationProps animationProps, ValueAnimator.AnimatorUpdateListener animatorUpdateListener) {
        RecentsConfiguration configuration = Recents.getConfiguration();
        cancelTransformAnimation();
        this.mTmpAnimators.clear();
        taskViewTransform.applyToTaskView(this, this.mTmpAnimators, animationProps, !configuration.fakeShadows);
        if (animationProps.isImmediate()) {
            if (Float.compare(getDimAlpha(), taskViewTransform.dimAlpha) != 0) {
                setDimAlpha(taskViewTransform.dimAlpha);
            }
            if (Float.compare(this.mViewBounds.getAlpha(), taskViewTransform.viewOutlineAlpha) != 0) {
                this.mViewBounds.setAlpha(taskViewTransform.viewOutlineAlpha);
            }
            if (animationProps.getListener() != null) {
                animationProps.getListener().onAnimationEnd(null);
            }
            if (animatorUpdateListener != null) {
                animatorUpdateListener.onAnimationUpdate(null);
                return;
            }
            return;
        }
        if (Float.compare(getDimAlpha(), taskViewTransform.dimAlpha) != 0) {
            this.mDimAnimator = ObjectAnimator.ofFloat(this, DIM_ALPHA, getDimAlpha(), taskViewTransform.dimAlpha);
            this.mTmpAnimators.add(animationProps.apply(6, this.mDimAnimator));
        }
        if (Float.compare(this.mViewBounds.getAlpha(), taskViewTransform.viewOutlineAlpha) != 0) {
            this.mOutlineAnimator = ObjectAnimator.ofFloat(this, VIEW_OUTLINE_ALPHA, this.mViewBounds.getAlpha(), taskViewTransform.viewOutlineAlpha);
            this.mTmpAnimators.add(animationProps.apply(6, this.mOutlineAnimator));
        }
        if (animatorUpdateListener != null) {
            ValueAnimator ofInt = ValueAnimator.ofInt(0, 1);
            ofInt.addUpdateListener(animatorUpdateListener);
            this.mTmpAnimators.add(animationProps.apply(6, ofInt));
        }
        this.mTransformAnimation = animationProps.createAnimator(this.mTmpAnimators);
        this.mTransformAnimation.start();
        this.mTargetAnimationTransform.copyFrom(taskViewTransform);
    }
}
