package com.android.systemui.recents.views;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
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
import com.android.systemui.R;
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
import com.android.systemui.shared.recents.model.Task;
import com.android.systemui.shared.recents.model.ThumbnailData;
import com.android.systemui.shared.recents.utilities.AnimationProps;
import com.android.systemui.shared.recents.utilities.Utilities;
import com.android.systemui.shared.recents.view.AnimateableViewBounds;
import java.io.PrintWriter;
import java.util.ArrayList;
/* loaded from: classes.dex */
public class TaskView extends FixedSizeFrameLayout implements View.OnClickListener, View.OnLongClickListener, Task.TaskCallbacks {
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
    protected TaskViewHeader mHeaderView;
    private View mIncompatibleAppToastView;
    @ViewDebug.ExportedProperty(category = "recents")
    private boolean mIsDisabledInSafeMode;
    private ObjectAnimator mOutlineAnimator;
    private final TaskViewTransform mTargetAnimationTransform;
    @ViewDebug.ExportedProperty(deepExport = true, prefix = "task_")
    private Task mTask;
    private boolean mTaskBound;
    @ViewDebug.ExportedProperty(deepExport = true, prefix = "thumbnail_")
    protected TaskViewThumbnail mThumbnailView;
    private ArrayList<Animator> mTmpAnimators;
    @ViewDebug.ExportedProperty(category = "recents")
    private boolean mTouchExplorationEnabled;
    private AnimatorSet mTransformAnimation;
    @ViewDebug.ExportedProperty(deepExport = true, prefix = "view_bounds_")
    private AnimateableViewBounds mViewBounds;
    public static final Property<TaskView, Float> DIM_ALPHA_WITHOUT_HEADER = new FloatProperty<TaskView>("dimAlphaWithoutHeader") { // from class: com.android.systemui.recents.views.TaskView.1
        @Override // android.util.FloatProperty
        public void setValue(TaskView taskView, float f) {
            taskView.setDimAlphaWithoutHeader(f);
        }

        @Override // android.util.Property
        public Float get(TaskView taskView) {
            return Float.valueOf(taskView.getDimAlpha());
        }
    };
    public static final Property<TaskView, Float> DIM_ALPHA = new FloatProperty<TaskView>("dimAlpha") { // from class: com.android.systemui.recents.views.TaskView.2
        @Override // android.util.FloatProperty
        public void setValue(TaskView taskView, float f) {
            taskView.setDimAlpha(f);
        }

        @Override // android.util.Property
        public Float get(TaskView taskView) {
            return Float.valueOf(taskView.getDimAlpha());
        }
    };
    public static final Property<TaskView, Float> VIEW_OUTLINE_ALPHA = new FloatProperty<TaskView>("viewOutlineAlpha") { // from class: com.android.systemui.recents.views.TaskView.3
        @Override // android.util.FloatProperty
        public void setValue(TaskView taskView, float f) {
            taskView.getViewBounds().setAlpha(f);
        }

        @Override // android.util.Property
        public Float get(TaskView taskView) {
            return Float.valueOf(taskView.getViewBounds().getAlpha());
        }
    };

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
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
        this.mViewBounds = createOutlineProvider();
        if (configuration.fakeShadows) {
            setBackground(new FakeShadowDrawable(resources, configuration));
        }
        setOutlineProvider(this.mViewBounds);
        setOnLongClickListener(this);
        setAccessibilityDelegate(new TaskViewAccessibilityDelegate(this));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setCallbacks(TaskViewCallbacks taskViewCallbacks) {
        this.mCb = taskViewCallbacks;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void onReload(boolean z) {
        resetNoUserInteractionState();
        if (!z) {
            resetViewProperties();
        }
    }

    public Task getTask() {
        return this.mTask;
    }

    protected AnimateableViewBounds createOutlineProvider() {
        return new AnimateableViewBounds(this, this.mContext.getResources().getDimensionPixelSize(R.dimen.recents_task_view_shadow_rounded_corners_radius));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public AnimateableViewBounds getViewBounds() {
        return this.mViewBounds;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        this.mHeaderView = (TaskViewHeader) findViewById(R.id.task_view_bar);
        this.mThumbnailView = (TaskViewThumbnail) findViewById(R.id.task_view_thumbnail);
        this.mThumbnailView.updateClipToTaskBar(this.mHeaderView);
        this.mActionButtonView = findViewById(R.id.lock_to_app_fab);
        this.mActionButtonView.setOutlineProvider(new ViewOutlineProvider() { // from class: com.android.systemui.recents.views.TaskView.4
            @Override // android.view.ViewOutlineProvider
            public void getOutline(View view, Outline outline) {
                outline.setOval(0, 0, TaskView.this.mActionButtonView.getWidth(), TaskView.this.mActionButtonView.getHeight());
                outline.setAlpha(0.35f);
            }
        });
        this.mActionButtonView.setOnClickListener(this);
        this.mActionButtonTranslationZ = this.mActionButtonView.getTranslationZ();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void onConfigurationChanged() {
        this.mHeaderView.onConfigurationChanged();
    }

    @Override // android.view.View
    protected void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        if (i > 0 && i2 > 0) {
            this.mHeaderView.onTaskViewSizeChanged(i, i2);
            this.mThumbnailView.onTaskViewSizeChanged(i, i2);
            this.mActionButtonView.setTranslationX(i - getMeasuredWidth());
            this.mActionButtonView.setTranslationY(i2 - getMeasuredHeight());
        }
    }

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getAction() == 0) {
            this.mDownTouchPos.set((int) (motionEvent.getX() * getScaleX()), (int) (motionEvent.getY() * getScaleY()));
        }
        return super.onInterceptTouchEvent(motionEvent);
    }

    @Override // com.android.systemui.recents.views.FixedSizeFrameLayout
    protected void measureContents(int i, int i2) {
        measureChildren(View.MeasureSpec.makeMeasureSpec((i - this.mPaddingLeft) - this.mPaddingRight, 1073741824), View.MeasureSpec.makeMeasureSpec((i2 - this.mPaddingTop) - this.mPaddingBottom, 1073741824));
        setMeasuredDimension(i, i2);
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
    public boolean isAnimatingTo(TaskViewTransform taskViewTransform) {
        return this.mTransformAnimation != null && this.mTransformAnimation.isStarted() && this.mTargetAnimationTransform.isSame(taskViewTransform);
    }

    public void cancelTransformAnimation() {
        cancelDimAnimationIfExists();
        Utilities.cancelAnimationWithoutCallbacks(this.mTransformAnimation);
        Utilities.cancelAnimationWithoutCallbacks(this.mOutlineAnimator);
    }

    private void cancelDimAnimationIfExists() {
        if (this.mDimAnimator != null) {
            this.mDimAnimator.cancel();
        }
    }

    public void setTouchEnabled(boolean z) {
        setOnClickListener(z ? this : null);
    }

    public void startNoUserInteractionAnimation() {
        this.mHeaderView.startNoUserInteractionAnimation();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setNoUserInteractionState() {
        this.mHeaderView.setNoUserInteractionState();
    }

    void resetNoUserInteractionState() {
        this.mHeaderView.resetNoUserInteractionState();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void dismissTask() {
        DismissTaskViewEvent dismissTaskViewEvent = new DismissTaskViewEvent(this);
        dismissTaskViewEvent.addPostAnimationCallback(new Runnable() { // from class: com.android.systemui.recents.views.TaskView.5
            @Override // java.lang.Runnable
            public void run() {
                EventBus.getDefault().send(new TaskViewDismissedEvent(TaskView.this.mTask, this, new AnimationProps(200, Interpolators.FAST_OUT_SLOW_IN)));
            }
        });
        EventBus.getDefault().send(dismissTaskViewEvent);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean shouldClipViewInStack() {
        if (getVisibility() != 0 || Recents.getConfiguration().isLowRamDevice) {
            return false;
        }
        return this.mClipViewInStack;
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

    public TaskViewHeader getHeaderView() {
        return this.mHeaderView;
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

    public float getDimAlpha() {
        return this.mDimAlpha;
    }

    public void setFocusedState(boolean z, boolean z2) {
        if (z) {
            if (z2 && !isFocused()) {
                requestFocus();
            }
        } else if (isAccessibilityFocused() && this.mTouchExplorationEnabled) {
            clearAccessibilityFocus();
        }
    }

    public void showActionButton(boolean z, int i) {
        this.mActionButtonView.setVisibility(0);
        if (!z || this.mActionButtonView.getAlpha() >= 1.0f) {
            this.mActionButtonView.setScaleX(1.0f);
            this.mActionButtonView.setScaleY(1.0f);
            this.mActionButtonView.setAlpha(1.0f);
            this.mActionButtonView.setTranslationZ(this.mActionButtonTranslationZ);
            return;
        }
        this.mActionButtonView.animate().alpha(1.0f).scaleX(1.0f).scaleY(1.0f).setDuration(i).setInterpolator(Interpolators.ALPHA_IN).start();
    }

    public void hideActionButton(boolean z, int i, boolean z2, final Animator.AnimatorListener animatorListener) {
        if (!z || this.mActionButtonView.getAlpha() <= 0.0f) {
            this.mActionButtonView.setAlpha(0.0f);
            this.mActionButtonView.setVisibility(4);
            if (animatorListener != null) {
                animatorListener.onAnimationEnd(null);
                return;
            }
            return;
        }
        if (z2) {
            this.mActionButtonView.animate().scaleX(0.9f).scaleY(0.9f);
        }
        this.mActionButtonView.animate().alpha(0.0f).setDuration(i).setInterpolator(Interpolators.ALPHA_OUT).withEndAction(new Runnable() { // from class: com.android.systemui.recents.views.TaskView.6
            @Override // java.lang.Runnable
            public void run() {
                if (animatorListener != null) {
                    animatorListener.onAnimationEnd(null);
                }
                TaskView.this.mActionButtonView.setVisibility(4);
            }
        }).start();
    }

    public void onPrepareLaunchTargetForEnterAnimation() {
        setDimAlphaWithoutHeader(0.0f);
        this.mActionButtonView.setAlpha(0.0f);
        if (this.mIncompatibleAppToastView != null && this.mIncompatibleAppToastView.getVisibility() == 0) {
            this.mIncompatibleAppToastView.setAlpha(0.0f);
        }
    }

    public void onStartLaunchTargetEnterAnimation(TaskViewTransform taskViewTransform, int i, boolean z, ReferenceCountedTrigger referenceCountedTrigger) {
        cancelDimAnimationIfExists();
        referenceCountedTrigger.increment();
        this.mDimAnimator = (ObjectAnimator) new AnimationProps(i, Interpolators.ALPHA_OUT).apply(7, ObjectAnimator.ofFloat(this, DIM_ALPHA_WITHOUT_HEADER, getDimAlpha(), taskViewTransform.dimAlpha));
        this.mDimAnimator.addListener(referenceCountedTrigger.decrementOnAnimationEnd());
        this.mDimAnimator.start();
        if (z) {
            showActionButton(true, i);
        }
        if (this.mIncompatibleAppToastView != null && this.mIncompatibleAppToastView.getVisibility() == 0) {
            this.mIncompatibleAppToastView.animate().alpha(1.0f).setDuration(i).setInterpolator(Interpolators.ALPHA_IN).start();
        }
    }

    public void onStartLaunchTargetLaunchAnimation(int i, boolean z, ReferenceCountedTrigger referenceCountedTrigger) {
        Utilities.cancelAnimationWithoutCallbacks(this.mDimAnimator);
        this.mDimAnimator = (ObjectAnimator) new AnimationProps(i, Interpolators.ALPHA_OUT).apply(7, ObjectAnimator.ofFloat(this, DIM_ALPHA, getDimAlpha(), 0.0f));
        this.mDimAnimator.start();
        referenceCountedTrigger.increment();
        hideActionButton(true, i, !z, referenceCountedTrigger.decrementOnAnimationEnd());
    }

    public void onStartFrontTaskEnterAnimation(boolean z) {
        if (z) {
            showActionButton(false, 0);
        }
    }

    public void onTaskBound(Task task, boolean z, int i, Rect rect) {
        SystemServicesProxy systemServices = Recents.getSystemServices();
        this.mTouchExplorationEnabled = z;
        this.mTask = task;
        boolean z2 = true;
        this.mTaskBound = true;
        this.mTask.addCallback(this);
        if (this.mTask.isSystemApp || !systemServices.isInSafeMode()) {
            z2 = false;
        }
        this.mIsDisabledInSafeMode = z2;
        this.mThumbnailView.bindToTask(this.mTask, this.mIsDisabledInSafeMode, i, rect);
        this.mHeaderView.bindToTask(this.mTask, this.mTouchExplorationEnabled, this.mIsDisabledInSafeMode);
        if (!task.isDockable && systemServices.hasDockedTask()) {
            if (this.mIncompatibleAppToastView == null) {
                this.mIncompatibleAppToastView = Utilities.findViewStubById(this, (int) R.id.incompatible_app_toast_stub).inflate();
                ((TextView) findViewById(16908299)).setText(R.string.dock_non_resizeble_failed_to_dock_text);
            }
            this.mIncompatibleAppToastView.setVisibility(0);
        } else if (this.mIncompatibleAppToastView != null) {
            this.mIncompatibleAppToastView.setVisibility(4);
        }
    }

    @Override // com.android.systemui.shared.recents.model.Task.TaskCallbacks
    public void onTaskDataLoaded(Task task, ThumbnailData thumbnailData) {
        if (this.mTaskBound) {
            this.mThumbnailView.onTaskDataLoaded(thumbnailData);
            this.mHeaderView.onTaskDataLoaded();
        }
    }

    @Override // com.android.systemui.shared.recents.model.Task.TaskCallbacks
    public void onTaskDataUnloaded() {
        this.mTask.removeCallback(this);
        this.mThumbnailView.unbindFromTask();
        this.mHeaderView.unbindFromTask(this.mTouchExplorationEnabled);
        this.mTaskBound = false;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        boolean z = true;
        if (this.mIsDisabledInSafeMode) {
            Context context = getContext();
            String string = context.getString(R.string.recents_launch_disabled_message, this.mTask.title);
            if (this.mDisabledAppToast != null) {
                this.mDisabledAppToast.cancel();
            }
            this.mDisabledAppToast = Toast.makeText(context, string, 0);
            this.mDisabledAppToast.show();
            return;
        }
        if (view == this.mActionButtonView) {
            this.mActionButtonView.setTranslationZ(0.0f);
        } else {
            z = false;
        }
        EventBus.getDefault().send(new LaunchTaskEvent(this, this.mTask, null, z));
        MetricsLogger.action(view.getContext(), 277, this.mTask.key.getComponent().toString());
    }

    @Override // android.view.View.OnLongClickListener
    public boolean onLongClick(View view) {
        boolean z;
        if (Recents.getConfiguration().dragToSplitEnabled) {
            SystemServicesProxy systemServices = Recents.getSystemServices();
            Rect rect = new Rect(this.mViewBounds.getClipBounds());
            if (!rect.isEmpty()) {
                rect.scale(getScaleX());
                z = rect.contains(this.mDownTouchPos.x, this.mDownTouchPos.y);
            } else {
                z = this.mDownTouchPos.x <= getWidth() && this.mDownTouchPos.y <= getHeight();
            }
            if (view == this && z && !systemServices.hasDockedTask()) {
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
        return false;
    }

    public final void onBusEvent(DragEndEvent dragEndEvent) {
        if (!(dragEndEvent.dropTarget instanceof DockState)) {
            dragEndEvent.addPostAnimationCallback(new Runnable() { // from class: com.android.systemui.recents.views.-$$Lambda$TaskView$-pg3FhkTHYE_OFyYVD2c3tjmUu4
                @Override // java.lang.Runnable
                public final void run() {
                    TaskView.this.setClipViewInStack(true);
                }
            });
        }
        EventBus.getDefault().unregister(this);
    }

    public final void onBusEvent(DragEndCancelledEvent dragEndCancelledEvent) {
        dragEndCancelledEvent.addPostAnimationCallback(new Runnable() { // from class: com.android.systemui.recents.views.-$$Lambda$TaskView$5tk674oyueWivSnbNCm15r4dSjc
            @Override // java.lang.Runnable
            public final void run() {
                TaskView.this.setClipViewInStack(true);
            }
        });
    }

    public void dump(String str, PrintWriter printWriter) {
        printWriter.print(str);
        printWriter.print("TaskView");
        printWriter.print(" mTask=");
        printWriter.print(this.mTask.key.id);
        printWriter.println();
        this.mThumbnailView.dump(str + "  ", printWriter);
    }
}
