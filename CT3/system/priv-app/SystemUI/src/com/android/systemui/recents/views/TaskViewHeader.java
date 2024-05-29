package com.android.systemui.recents.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.CountDownTimer;
import android.support.v4.graphics.ColorUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewDebug;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.Interpolators;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.events.activity.LaunchTaskEvent;
import com.android.systemui.recents.events.ui.ShowApplicationInfoEvent;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.misc.Utilities;
import com.android.systemui.recents.model.Task;
import com.mediatek.multiwindow.MultiWindowManager;
/* loaded from: a.zip:com/android/systemui/recents/views/TaskViewHeader.class */
public class TaskViewHeader extends FrameLayout implements View.OnClickListener, View.OnLongClickListener {
    ImageView mAppIconView;
    ImageView mAppInfoView;
    FrameLayout mAppOverlayView;
    TextView mAppTitleView;
    private HighlightColorDrawable mBackground;
    int mCornerRadius;
    Drawable mDarkDismissDrawable;
    Drawable mDarkFreeformIcon;
    Drawable mDarkFullscreenIcon;
    Drawable mDarkInfoIcon;
    @ViewDebug.ExportedProperty(category = "recents")
    float mDimAlpha;
    private Paint mDimLayerPaint;
    int mDisabledTaskBarBackgroundColor;
    ImageView mDismissButton;
    private CountDownTimer mFocusTimerCountDown;
    ProgressBar mFocusTimerIndicator;
    int mHeaderBarHeight;
    int mHeaderButtonPadding;
    int mHighlightHeight;
    ImageView mIconView;
    Drawable mLightDismissDrawable;
    Drawable mLightFreeformIcon;
    Drawable mLightFullscreenIcon;
    Drawable mLightInfoIcon;
    ImageView mMoveTaskButton;
    int mMoveTaskTargetStackId;
    private HighlightColorDrawable mOverlayBackground;
    Task mTask;
    int mTaskBarViewDarkTextColor;
    int mTaskBarViewLightTextColor;
    @ViewDebug.ExportedProperty(category = "recents")
    Rect mTaskViewRect;
    TextView mTitleView;
    private float[] mTmpHSL;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/recents/views/TaskViewHeader$HighlightColorDrawable.class */
    public class HighlightColorDrawable extends Drawable {
        private int mColor;
        private float mDimAlpha;
        final TaskViewHeader this$0;
        private Paint mHighlightPaint = new Paint();
        private Paint mBackgroundPaint = new Paint();

        public HighlightColorDrawable(TaskViewHeader taskViewHeader) {
            this.this$0 = taskViewHeader;
            this.mBackgroundPaint.setColor(Color.argb(255, 0, 0, 0));
            this.mBackgroundPaint.setAntiAlias(true);
            this.mHighlightPaint.setColor(Color.argb(255, 255, 255, 255));
            this.mHighlightPaint.setAntiAlias(true);
        }

        @Override // android.graphics.drawable.Drawable
        public void draw(Canvas canvas) {
            canvas.drawRoundRect(0.0f, 0.0f, this.this$0.mTaskViewRect.width(), Math.max(this.this$0.mHighlightHeight, this.this$0.mCornerRadius) * 2, this.this$0.mCornerRadius, this.this$0.mCornerRadius, this.mHighlightPaint);
            canvas.drawRoundRect(0.0f, this.this$0.mHighlightHeight, this.this$0.mTaskViewRect.width(), this.this$0.getHeight() + this.this$0.mCornerRadius, this.this$0.mCornerRadius, this.this$0.mCornerRadius, this.mBackgroundPaint);
        }

        public int getColor() {
            return this.mColor;
        }

        @Override // android.graphics.drawable.Drawable
        public int getOpacity() {
            return -1;
        }

        @Override // android.graphics.drawable.Drawable
        public void setAlpha(int i) {
        }

        public void setColorAndDim(int i, float f) {
            if (this.mColor == i && Float.compare(this.mDimAlpha, f) == 0) {
                return;
            }
            this.mColor = i;
            this.mDimAlpha = f;
            this.mBackgroundPaint.setColor(i);
            ColorUtils.colorToHSL(i, this.this$0.mTmpHSL);
            this.this$0.mTmpHSL[2] = Math.min(1.0f, this.this$0.mTmpHSL[2] + ((1.0f - f) * 0.075f));
            this.mHighlightPaint.setColor(ColorUtils.HSLToColor(this.this$0.mTmpHSL));
            invalidateSelf();
        }

        @Override // android.graphics.drawable.Drawable
        public void setColorFilter(ColorFilter colorFilter) {
        }
    }

    public TaskViewHeader(Context context) {
        this(context, null);
    }

    public TaskViewHeader(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public TaskViewHeader(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public TaskViewHeader(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mTaskViewRect = new Rect();
        this.mMoveTaskTargetStackId = -1;
        this.mTmpHSL = new float[3];
        this.mDimLayerPaint = new Paint();
        setWillNotDraw(false);
        Resources resources = context.getResources();
        this.mLightDismissDrawable = context.getDrawable(2130837979);
        this.mDarkDismissDrawable = context.getDrawable(2130837978);
        this.mCornerRadius = resources.getDimensionPixelSize(2131690016);
        this.mHighlightHeight = resources.getDimensionPixelSize(2131690018);
        this.mTaskBarViewLightTextColor = context.getColor(2131558540);
        this.mTaskBarViewDarkTextColor = context.getColor(2131558541);
        this.mLightFreeformIcon = context.getDrawable(2130837988);
        this.mDarkFreeformIcon = context.getDrawable(2130837987);
        this.mLightFullscreenIcon = context.getDrawable(2130837990);
        this.mDarkFullscreenIcon = context.getDrawable(2130837989);
        this.mLightInfoIcon = context.getDrawable(2130837983);
        this.mDarkInfoIcon = context.getDrawable(2130837982);
        this.mDisabledTaskBarBackgroundColor = context.getColor(2131558537);
        this.mBackground = new HighlightColorDrawable(this);
        this.mBackground.setColorAndDim(Color.argb(255, 0, 0, 0), 0.0f);
        setBackground(this.mBackground);
        this.mOverlayBackground = new HighlightColorDrawable(this);
        this.mDimLayerPaint.setColor(Color.argb(255, 0, 0, 0));
        this.mDimLayerPaint.setAntiAlias(true);
    }

    private void hideAppOverlay(boolean z) {
        if (this.mAppOverlayView == null) {
            return;
        }
        if (z) {
            this.mAppOverlayView.setVisibility(8);
            return;
        }
        Animator createCircularReveal = ViewAnimationUtils.createCircularReveal(this.mAppOverlayView, this.mIconView.getLeft() + (this.mIconView.getWidth() / 2), this.mIconView.getTop() + (this.mIconView.getHeight() / 2), getWidth(), 0.0f);
        createCircularReveal.setDuration(250L);
        createCircularReveal.setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN);
        createCircularReveal.addListener(new AnimatorListenerAdapter(this) { // from class: com.android.systemui.recents.views.TaskViewHeader.2
            final TaskViewHeader this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                this.this$0.mAppOverlayView.setVisibility(8);
            }
        });
        createCircularReveal.start();
    }

    private void showAppOverlay() {
        SystemServicesProxy systemServices = Recents.getSystemServices();
        ComponentName component = this.mTask.key.getComponent();
        int i = this.mTask.key.userId;
        ActivityInfo activityInfo = systemServices.getActivityInfo(component, i);
        if (activityInfo == null) {
            return;
        }
        if (this.mAppOverlayView == null) {
            this.mAppOverlayView = (FrameLayout) Utilities.findViewStubById(this, 2131886622).inflate();
            this.mAppOverlayView.setBackground(this.mOverlayBackground);
            this.mAppIconView = (ImageView) this.mAppOverlayView.findViewById(2131886545);
            this.mAppIconView.setOnClickListener(this);
            this.mAppIconView.setOnLongClickListener(this);
            this.mAppInfoView = (ImageView) this.mAppOverlayView.findViewById(2131886625);
            this.mAppInfoView.setOnClickListener(this);
            this.mAppTitleView = (TextView) this.mAppOverlayView.findViewById(2131886624);
            updateLayoutParams(this.mAppIconView, this.mAppTitleView, null, this.mAppInfoView);
        }
        this.mAppTitleView.setText(systemServices.getBadgedApplicationLabel(activityInfo.applicationInfo, i));
        this.mAppTitleView.setTextColor(this.mTask.useLightOnPrimaryColor ? this.mTaskBarViewLightTextColor : this.mTaskBarViewDarkTextColor);
        this.mAppIconView.setImageDrawable(systemServices.getBadgedApplicationIcon(activityInfo.applicationInfo, i));
        this.mAppInfoView.setImageDrawable(this.mTask.useLightOnPrimaryColor ? this.mLightInfoIcon : this.mDarkInfoIcon);
        this.mAppOverlayView.setVisibility(0);
        Animator createCircularReveal = ViewAnimationUtils.createCircularReveal(this.mAppOverlayView, this.mIconView.getLeft() + (this.mIconView.getWidth() / 2), this.mIconView.getTop() + (this.mIconView.getHeight() / 2), 0.0f, getWidth());
        createCircularReveal.setDuration(250L);
        createCircularReveal.setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN);
        createCircularReveal.start();
    }

    private void updateBackgroundColor(int i, float f) {
        if (this.mTask != null) {
            this.mBackground.setColorAndDim(i, f);
            ColorUtils.colorToHSL(i, this.mTmpHSL);
            this.mTmpHSL[2] = Math.min(1.0f, this.mTmpHSL[2] + ((1.0f - f) * (-0.0625f)));
            this.mOverlayBackground.setColorAndDim(ColorUtils.HSLToColor(this.mTmpHSL), f);
            this.mDimLayerPaint.setAlpha((int) (255.0f * f));
            invalidate();
        }
    }

    private void updateLayoutParams(View view, View view2, View view3, View view4) {
        setLayoutParams(new FrameLayout.LayoutParams(-1, this.mHeaderBarHeight, 48));
        view.setLayoutParams(new FrameLayout.LayoutParams(this.mHeaderBarHeight, this.mHeaderBarHeight, 8388611));
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-1, -2, 8388627);
        layoutParams.setMarginStart(this.mHeaderBarHeight);
        layoutParams.setMarginEnd(this.mMoveTaskButton != null ? this.mHeaderBarHeight * 2 : this.mHeaderBarHeight);
        view2.setLayoutParams(layoutParams);
        if (view3 != null) {
            FrameLayout.LayoutParams layoutParams2 = new FrameLayout.LayoutParams(this.mHeaderBarHeight, this.mHeaderBarHeight, 8388613);
            layoutParams2.setMarginEnd(this.mHeaderBarHeight);
            view3.setLayoutParams(layoutParams2);
            view3.setPadding(this.mHeaderButtonPadding, this.mHeaderButtonPadding, this.mHeaderButtonPadding, this.mHeaderButtonPadding);
        }
        view4.setLayoutParams(new FrameLayout.LayoutParams(this.mHeaderBarHeight, this.mHeaderBarHeight, 8388613));
        view4.setPadding(this.mHeaderButtonPadding, this.mHeaderButtonPadding, this.mHeaderButtonPadding, this.mHeaderButtonPadding);
    }

    public void bindToTask(Task task, boolean z, boolean z2) {
        this.mTask = task;
        int i = z2 ? this.mDisabledTaskBarBackgroundColor : task.colorPrimary;
        if (this.mBackground.getColor() != i) {
            updateBackgroundColor(i, this.mDimAlpha);
        }
        if (!this.mTitleView.getText().toString().equals(task.title)) {
            this.mTitleView.setText(task.title);
        }
        this.mTitleView.setContentDescription(task.titleDescription);
        this.mTitleView.setTextColor(task.useLightOnPrimaryColor ? this.mTaskBarViewLightTextColor : this.mTaskBarViewDarkTextColor);
        this.mDismissButton.setImageDrawable(task.useLightOnPrimaryColor ? this.mLightDismissDrawable : this.mDarkDismissDrawable);
        this.mDismissButton.setContentDescription(task.dismissDescription);
        this.mDismissButton.setOnClickListener(this);
        this.mDismissButton.setClickable(false);
        ((RippleDrawable) this.mDismissButton.getBackground()).setForceSoftware(true);
        if (this.mMoveTaskButton != null) {
            if (task.isFreeformTask()) {
                this.mMoveTaskTargetStackId = 1;
                this.mMoveTaskButton.setImageDrawable(task.useLightOnPrimaryColor ? this.mLightFullscreenIcon : this.mDarkFullscreenIcon);
            } else {
                this.mMoveTaskTargetStackId = 2;
                this.mMoveTaskButton.setImageDrawable(task.useLightOnPrimaryColor ? this.mLightFreeformIcon : this.mDarkFreeformIcon);
            }
            this.mMoveTaskButton.setOnClickListener(this);
            this.mMoveTaskButton.setClickable(false);
            ((RippleDrawable) this.mMoveTaskButton.getBackground()).setForceSoftware(true);
        }
        if (Recents.getDebugFlags().isFastToggleRecentsEnabled()) {
            if (this.mFocusTimerIndicator == null) {
                this.mFocusTimerIndicator = (ProgressBar) Utilities.findViewStubById(this, 2131886620).inflate();
            }
            this.mFocusTimerIndicator.getProgressDrawable().setColorFilter(getSecondaryColor(task.colorPrimary, task.useLightOnPrimaryColor), PorterDuff.Mode.SRC_IN);
        }
        if (z) {
            this.mIconView.setContentDescription(task.appInfoDescription);
            this.mIconView.setOnClickListener(this);
            this.mIconView.setClickable(true);
        }
    }

    public void cancelFocusTimerIndicator() {
        if (this.mFocusTimerIndicator == null || this.mFocusTimerCountDown == null) {
            return;
        }
        this.mFocusTimerCountDown.cancel();
        this.mFocusTimerIndicator.setProgress(0);
        this.mFocusTimerIndicator.setVisibility(4);
    }

    public ImageView getIconView() {
        return this.mIconView;
    }

    int getSecondaryColor(int i, boolean z) {
        return Utilities.getColorWithOverlay(i, z ? -1 : -16777216, 0.8f);
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        if (view == this.mIconView) {
            EventBus.getDefault().send(new ShowApplicationInfoEvent(this.mTask));
        } else if (view == this.mDismissButton) {
            ((TaskView) Utilities.findParent(this, TaskView.class)).dismissTask();
            MetricsLogger.histogram(getContext(), "overview_task_dismissed_source", 2);
        } else if (view == this.mMoveTaskButton) {
            EventBus.getDefault().send(new LaunchTaskEvent((TaskView) Utilities.findParent(this, TaskView.class), this.mTask, this.mMoveTaskTargetStackId == 2 ? new Rect(this.mTaskViewRect) : new Rect(), this.mMoveTaskTargetStackId, false));
        } else if (view == this.mAppInfoView) {
            EventBus.getDefault().send(new ShowApplicationInfoEvent(this.mTask));
        } else if (view == this.mAppIconView) {
            hideAppOverlay(false);
        }
    }

    public void onConfigurationChanged() {
        getResources();
        int dimensionForDevice = TaskStackLayoutAlgorithm.getDimensionForDevice(getContext(), 2131690012, 2131690012, 2131690012, 2131690013, 2131690012, 2131690013);
        int dimensionForDevice2 = TaskStackLayoutAlgorithm.getDimensionForDevice(getContext(), 2131690014, 2131690014, 2131690014, 2131690015, 2131690014, 2131690015);
        if (dimensionForDevice == this.mHeaderBarHeight && dimensionForDevice2 == this.mHeaderButtonPadding) {
            return;
        }
        this.mHeaderBarHeight = dimensionForDevice;
        this.mHeaderButtonPadding = dimensionForDevice2;
        updateLayoutParams(this.mIconView, this.mTitleView, this.mMoveTaskButton, this.mDismissButton);
        if (this.mAppOverlayView != null) {
            updateLayoutParams(this.mAppIconView, this.mAppTitleView, null, this.mAppInfoView);
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    protected int[] onCreateDrawableState(int i) {
        return new int[0];
    }

    @Override // android.view.View
    public void onDrawForeground(Canvas canvas) {
        super.onDrawForeground(canvas);
        canvas.drawRoundRect(0.0f, 0.0f, this.mTaskViewRect.width(), getHeight() + this.mCornerRadius, this.mCornerRadius, this.mCornerRadius, this.mDimLayerPaint);
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        SystemServicesProxy systemServices = Recents.getSystemServices();
        this.mIconView = (ImageView) findViewById(2131886211);
        this.mIconView.setOnLongClickListener(this);
        this.mTitleView = (TextView) findViewById(2131886212);
        this.mDismissButton = (ImageView) findViewById(2131886619);
        if (systemServices.hasFreeformWorkspaceSupport()) {
            this.mMoveTaskButton = (ImageView) findViewById(2131886618);
            if (MultiWindowManager.isSupported()) {
                if (MultiWindowManager.DEBUG) {
                    Log.d("BMW", "onFinishInflate, ssp.hasDockedTask() = " + systemServices.hasDockedTask());
                }
                if (systemServices.hasDockedTask()) {
                    this.mMoveTaskButton.setVisibility(4);
                } else {
                    this.mMoveTaskButton.setVisibility(0);
                }
            }
        }
        onConfigurationChanged();
    }

    @Override // android.widget.FrameLayout, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        onTaskViewSizeChanged(this.mTaskViewRect.width(), this.mTaskViewRect.height());
    }

    @Override // android.view.View.OnLongClickListener
    public boolean onLongClick(View view) {
        if (view == this.mIconView) {
            showAppOverlay();
            return true;
        } else if (view == this.mAppIconView) {
            hideAppOverlay(false);
            return true;
        } else {
            return false;
        }
    }

    public void onTaskDataLoaded() {
        if (this.mTask.icon != null) {
            this.mIconView.setImageDrawable(this.mTask.icon);
        }
    }

    public void onTaskViewSizeChanged(int i, int i2) {
        this.mTaskViewRect.set(0, 0, i, i2);
        int measuredWidth = i - getMeasuredWidth();
        boolean z = true;
        boolean z2 = true;
        boolean z3 = true;
        if (this.mTask != null) {
            z = true;
            z2 = true;
            z3 = true;
            if (this.mTask.isFreeformTask()) {
                int measuredWidth2 = this.mIconView.getMeasuredWidth();
                int measureText = (int) this.mTitleView.getPaint().measureText(this.mTask.title);
                int measuredWidth3 = this.mDismissButton.getMeasuredWidth();
                int measuredWidth4 = this.mMoveTaskButton != null ? this.mMoveTaskButton.getMeasuredWidth() : 0;
                z3 = i >= ((measuredWidth2 + measuredWidth3) + measuredWidth4) + measureText;
                z2 = i >= (measuredWidth2 + measuredWidth3) + measuredWidth4;
                z = i >= measuredWidth2 + measuredWidth3;
            }
        }
        this.mTitleView.setVisibility(z3 ? 0 : 4);
        if (this.mMoveTaskButton != null) {
            boolean z4 = z2;
            if (MultiWindowManager.isSupported()) {
                SystemServicesProxy systemServices = Recents.getSystemServices();
                if (MultiWindowManager.DEBUG) {
                    Log.d("BMW", "onFinishInflate, ssp.hasDockedTask() = " + systemServices.hasDockedTask());
                }
                z4 = z2;
                if (systemServices.hasDockedTask()) {
                    z4 = z2;
                    if (z2) {
                        z4 = false;
                    }
                }
            }
            this.mMoveTaskButton.setVisibility(z4 ? 0 : 4);
            this.mMoveTaskButton.setTranslationX(measuredWidth);
        }
        this.mDismissButton.setVisibility(z ? 0 : 4);
        this.mDismissButton.setTranslationX(measuredWidth);
        setLeftTopRightBottom(0, 0, i, getMeasuredHeight());
    }

    public void reset() {
        hideAppOverlay(true);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void resetNoUserInteractionState() {
        this.mDismissButton.setVisibility(4);
        this.mDismissButton.setAlpha(0.0f);
        this.mDismissButton.setClickable(false);
        if (this.mMoveTaskButton != null) {
            this.mMoveTaskButton.setVisibility(4);
            this.mMoveTaskButton.setAlpha(0.0f);
            this.mMoveTaskButton.setClickable(false);
        }
    }

    public void setDimAlpha(float f) {
        if (Float.compare(this.mDimAlpha, f) != 0) {
            this.mDimAlpha = f;
            this.mTitleView.setAlpha(1.0f - f);
            updateBackgroundColor(this.mBackground.getColor(), f);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setNoUserInteractionState() {
        this.mDismissButton.setVisibility(0);
        this.mDismissButton.animate().cancel();
        this.mDismissButton.setAlpha(1.0f);
        this.mDismissButton.setClickable(true);
        if (this.mMoveTaskButton != null) {
            if (MultiWindowManager.isSupported() && Recents.getSystemServices().hasDockedTask()) {
                return;
            }
            this.mMoveTaskButton.setVisibility(0);
            this.mMoveTaskButton.animate().cancel();
            this.mMoveTaskButton.setAlpha(1.0f);
            this.mMoveTaskButton.setClickable(true);
        }
    }

    /* JADX WARN: Type inference failed for: r1v3, types: [com.android.systemui.recents.views.TaskViewHeader$1] */
    public void startFocusTimerIndicator(int i) {
        if (this.mFocusTimerIndicator == null) {
            return;
        }
        this.mFocusTimerIndicator.setVisibility(0);
        this.mFocusTimerIndicator.setMax(i);
        this.mFocusTimerIndicator.setProgress(i);
        if (this.mFocusTimerCountDown != null) {
            this.mFocusTimerCountDown.cancel();
        }
        this.mFocusTimerCountDown = new CountDownTimer(this, i, 30L) { // from class: com.android.systemui.recents.views.TaskViewHeader.1
            final TaskViewHeader this$0;

            {
                this.this$0 = this;
            }

            @Override // android.os.CountDownTimer
            public void onFinish() {
            }

            @Override // android.os.CountDownTimer
            public void onTick(long j) {
                this.this$0.mFocusTimerIndicator.setProgress((int) j);
            }
        }.start();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void startNoUserInteractionAnimation() {
        int integer = getResources().getInteger(2131755062);
        this.mDismissButton.setVisibility(0);
        this.mDismissButton.setClickable(true);
        if (this.mDismissButton.getVisibility() == 0) {
            this.mDismissButton.animate().alpha(1.0f).setInterpolator(Interpolators.FAST_OUT_LINEAR_IN).setDuration(integer).start();
        } else {
            this.mDismissButton.setAlpha(1.0f);
        }
        if (this.mMoveTaskButton != null) {
            if (this.mMoveTaskButton.getVisibility() != 0) {
                this.mMoveTaskButton.setAlpha(1.0f);
                return;
            }
            this.mMoveTaskButton.setVisibility(0);
            this.mMoveTaskButton.setClickable(true);
            this.mMoveTaskButton.animate().alpha(1.0f).setInterpolator(Interpolators.FAST_OUT_LINEAR_IN).setDuration(integer).start();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void unbindFromTask(boolean z) {
        this.mTask = null;
        this.mIconView.setImageDrawable(null);
        if (z) {
            this.mIconView.setClickable(false);
        }
    }
}
