package com.android.systemui.statusbar.phone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import com.android.systemui.EventLogTags;
import com.android.systemui.Interpolators;
import com.android.systemui.classifier.FalsingManager;
import com.android.systemui.doze.DozeLog;
import com.android.systemui.statusbar.FlingAnimationUtils;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;
/* loaded from: a.zip:com/android/systemui/statusbar/phone/PanelView.class */
public abstract class PanelView extends FrameLayout {
    public static final String TAG = PanelView.class.getSimpleName();
    private boolean mAnimatingOnDown;
    PanelBar mBar;
    private Interpolator mBounceInterpolator;
    private boolean mClosing;
    private boolean mCollapseAfterPeek;
    private boolean mCollapsedAndHeadsUpOnDown;
    private float mExpandedFraction;
    protected float mExpandedHeight;
    protected boolean mExpanding;
    private FalsingManager mFalsingManager;
    private FlingAnimationUtils mFlingAnimationUtils;
    private final Runnable mFlingCollapseRunnable;
    private boolean mGestureWaitForTouchSlop;
    private boolean mHasLayoutedSinceDown;
    protected HeadsUpManager mHeadsUpManager;
    private ValueAnimator mHeightAnimator;
    protected boolean mHintAnimationRunning;
    private float mHintDistance;
    private boolean mIgnoreXTouchSlop;
    private float mInitialOffsetOnTouch;
    private float mInitialTouchX;
    private float mInitialTouchY;
    private boolean mInstantExpanding;
    private boolean mJustPeeked;
    protected KeyguardBottomAreaView mKeyguardBottomArea;
    private boolean mMotionAborted;
    private float mNextCollapseSpeedUpFactor;
    private boolean mOverExpandedBeforeFling;
    private boolean mPanelClosedOnDown;
    private ObjectAnimator mPeekAnimator;
    private float mPeekHeight;
    private boolean mPeekPending;
    private Runnable mPeekRunnable;
    private boolean mPeekTouching;
    protected final Runnable mPostCollapseRunnable;
    protected PhoneStatusBar mStatusBar;
    private boolean mTouchAboveFalsingThreshold;
    private boolean mTouchDisabled;
    protected int mTouchSlop;
    private boolean mTouchSlopExceeded;
    private boolean mTouchStartedInEmptyArea;
    protected boolean mTracking;
    private int mTrackingPointer;
    private int mUnlockFalsingThreshold;
    private boolean mUpdateFlingOnLayout;
    private float mUpdateFlingVelocity;
    private boolean mUpwardsWhenTresholdReached;
    private VelocityTrackerInterface mVelocityTracker;
    private String mViewName;

    public PanelView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mExpandedFraction = 0.0f;
        this.mExpandedHeight = 0.0f;
        this.mNextCollapseSpeedUpFactor = 1.0f;
        this.mPeekRunnable = new Runnable(this) { // from class: com.android.systemui.statusbar.phone.PanelView.1
            final PanelView this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.mPeekPending = false;
                this.this$0.runPeekAnimation();
            }
        };
        this.mFlingCollapseRunnable = new Runnable(this) { // from class: com.android.systemui.statusbar.phone.PanelView.2
            final PanelView this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.fling(0.0f, false, this.this$0.mNextCollapseSpeedUpFactor, false);
            }
        };
        this.mPostCollapseRunnable = new Runnable(this) { // from class: com.android.systemui.statusbar.phone.PanelView.3
            final PanelView this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.collapse(false, 1.0f);
            }
        };
        this.mFlingAnimationUtils = new FlingAnimationUtils(context, 0.6f);
        this.mBounceInterpolator = new BounceInterpolator();
        this.mFalsingManager = FalsingManager.getInstance(context);
    }

    private void abortAnimations() {
        cancelPeek();
        cancelHeightAnimator();
        removeCallbacks(this.mPostCollapseRunnable);
        removeCallbacks(this.mFlingCollapseRunnable);
    }

    private ValueAnimator createHeightAnimator(float f) {
        ValueAnimator ofFloat = ValueAnimator.ofFloat(this.mExpandedHeight, f);
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(this) { // from class: com.android.systemui.statusbar.phone.PanelView.11
            final PanelView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                this.this$0.setExpandedHeightInternal(((Float) valueAnimator.getAnimatedValue()).floatValue());
            }
        });
        return ofFloat;
    }

    private void endClosing() {
        if (this.mClosing) {
            this.mClosing = false;
            onClosingFinished();
        }
    }

    private void endMotionEvent(MotionEvent motionEvent, float f, float f2, boolean z) {
        this.mTrackingPointer = -1;
        if ((this.mTracking && this.mTouchSlopExceeded) || Math.abs(f - this.mInitialTouchX) > this.mTouchSlop || Math.abs(f2 - this.mInitialTouchY) > this.mTouchSlop || motionEvent.getActionMasked() == 3 || z) {
            float f3 = 0.0f;
            float f4 = 0.0f;
            if (this.mVelocityTracker != null) {
                this.mVelocityTracker.computeCurrentVelocity(1000);
                f3 = this.mVelocityTracker.getYVelocity();
                f4 = (float) Math.hypot(this.mVelocityTracker.getXVelocity(), this.mVelocityTracker.getYVelocity());
            }
            if ((flingExpands(f3, f4, f, f2) && this.mStatusBar.getBarState() != 1) || motionEvent.getActionMasked() == 3) {
                z = true;
            }
            DozeLog.traceFling(z, this.mTouchAboveFalsingThreshold, this.mStatusBar.isFalsingThresholdNeeded(), this.mStatusBar.isWakeUpComingFromTouch());
            if (!z && this.mStatusBar.getBarState() == 1) {
                float displayDensity = this.mStatusBar.getDisplayDensity();
                EventLogTags.writeSysuiLockscreenGesture(1, (int) Math.abs((f2 - this.mInitialTouchY) / displayDensity), (int) Math.abs(f3 / displayDensity));
            }
            fling(f3, z, isFalseTouch(f, f2));
            onTrackingStopped(z);
            this.mUpdateFlingOnLayout = z && this.mPanelClosedOnDown && !this.mHasLayoutedSinceDown;
            if (this.mUpdateFlingOnLayout) {
                this.mUpdateFlingVelocity = f3;
            }
        } else {
            onTrackingStopped(onEmptySpaceClick(this.mInitialTouchX));
        }
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.recycle();
            this.mVelocityTracker = null;
        }
        this.mPeekTouching = false;
    }

    private int getFalsingThreshold() {
        return (int) (this.mUnlockFalsingThreshold * (this.mStatusBar.isWakeUpComingFromTouch() ? 1.5f : 1.0f));
    }

    private void initVelocityTracker() {
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.recycle();
        }
        this.mVelocityTracker = VelocityTrackerFactory.obtain(getContext());
    }

    private boolean isDirectionUpwards(float f, float f2) {
        boolean z = false;
        float f3 = this.mInitialTouchX;
        float f4 = f2 - this.mInitialTouchY;
        if (f4 >= 0.0f) {
            return false;
        }
        if (Math.abs(f4) >= Math.abs(f - f3)) {
            z = true;
        }
        return z;
    }

    private boolean isFalseTouch(float f, float f2) {
        boolean z = false;
        if (this.mStatusBar.isFalsingThresholdNeeded()) {
            if (this.mFalsingManager.isClassiferEnabled()) {
                return this.mFalsingManager.isFalseTouch();
            }
            if (this.mTouchAboveFalsingThreshold) {
                if (this.mUpwardsWhenTresholdReached) {
                    return false;
                }
                if (!isDirectionUpwards(f, f2)) {
                    z = true;
                }
                return z;
            }
            return true;
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void notifyExpandingStarted() {
        if (this.mExpanding) {
            return;
        }
        this.mExpanding = true;
        onExpandingStarted();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void runPeekAnimation() {
        this.mPeekHeight = getPeekHeight();
        if (this.mHeightAnimator != null) {
            return;
        }
        this.mPeekAnimator = ObjectAnimator.ofFloat(this, "expandedHeight", this.mPeekHeight).setDuration(250L);
        this.mPeekAnimator.setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN);
        this.mPeekAnimator.addListener(new AnimatorListenerAdapter(this) { // from class: com.android.systemui.statusbar.phone.PanelView.4
            private boolean mCancelled;
            final PanelView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
                this.mCancelled = true;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                this.this$0.mPeekAnimator = null;
                if (this.this$0.mCollapseAfterPeek && !this.mCancelled) {
                    this.this$0.postOnAnimation(this.this$0.mPostCollapseRunnable);
                }
                this.this$0.mCollapseAfterPeek = false;
            }
        });
        notifyExpandingStarted();
        this.mPeekAnimator.start();
        this.mJustPeeked = true;
    }

    private void schedulePeek() {
        this.mPeekPending = true;
        postOnAnimationDelayed(this.mPeekRunnable, ViewConfiguration.getTapTimeout());
        notifyBarPanelExpansionChanged();
    }

    private void startUnlockHintAnimationPhase1(Runnable runnable) {
        ValueAnimator createHeightAnimator = createHeightAnimator(Math.max(0.0f, getMaxPanelHeight() - this.mHintDistance));
        createHeightAnimator.setDuration(250L);
        createHeightAnimator.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
        createHeightAnimator.addListener(new AnimatorListenerAdapter(this, runnable) { // from class: com.android.systemui.statusbar.phone.PanelView.8
            private boolean mCancelled;
            final PanelView this$0;
            final Runnable val$onAnimationFinished;

            {
                this.this$0 = this;
                this.val$onAnimationFinished = runnable;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
                this.mCancelled = true;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                if (!this.mCancelled) {
                    this.this$0.startUnlockHintAnimationPhase2(this.val$onAnimationFinished);
                    return;
                }
                this.this$0.mHeightAnimator = null;
                this.val$onAnimationFinished.run();
            }
        });
        createHeightAnimator.start();
        this.mHeightAnimator = createHeightAnimator;
        this.mKeyguardBottomArea.getIndicationView().animate().translationY(-this.mHintDistance).setDuration(250L).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).withEndAction(new Runnable(this) { // from class: com.android.systemui.statusbar.phone.PanelView.9
            final PanelView this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.mKeyguardBottomArea.getIndicationView().animate().translationY(0.0f).setDuration(450L).setInterpolator(this.this$0.mBounceInterpolator).start();
            }
        }).start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startUnlockHintAnimationPhase2(Runnable runnable) {
        ValueAnimator createHeightAnimator = createHeightAnimator(getMaxPanelHeight());
        createHeightAnimator.setDuration(450L);
        createHeightAnimator.setInterpolator(this.mBounceInterpolator);
        createHeightAnimator.addListener(new AnimatorListenerAdapter(this, runnable) { // from class: com.android.systemui.statusbar.phone.PanelView.10
            final PanelView this$0;
            final Runnable val$onAnimationFinished;

            {
                this.this$0 = this;
                this.val$onAnimationFinished = runnable;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                this.this$0.mHeightAnimator = null;
                this.val$onAnimationFinished.run();
                this.this$0.notifyBarPanelExpansionChanged();
            }
        });
        createHeightAnimator.start();
        this.mHeightAnimator = createHeightAnimator;
    }

    private void trackMovement(MotionEvent motionEvent) {
        float rawX = motionEvent.getRawX() - motionEvent.getX();
        float rawY = motionEvent.getRawY() - motionEvent.getY();
        motionEvent.offsetLocation(rawX, rawY);
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.addMovement(motionEvent);
        }
        motionEvent.offsetLocation(-rawX, -rawY);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void cancelHeightAnimator() {
        if (this.mHeightAnimator != null) {
            this.mHeightAnimator.cancel();
        }
        endClosing();
    }

    public void cancelPeek() {
        boolean z = this.mPeekPending;
        if (this.mPeekAnimator != null) {
            z = true;
            this.mPeekAnimator.cancel();
        }
        removeCallbacks(this.mPeekRunnable);
        this.mPeekPending = false;
        if (z) {
            notifyBarPanelExpansionChanged();
        }
    }

    public void collapse(boolean z, float f) {
        if (this.mPeekPending || this.mPeekAnimator != null) {
            this.mCollapseAfterPeek = true;
            if (this.mPeekPending) {
                removeCallbacks(this.mPeekRunnable);
                this.mPeekRunnable.run();
            }
        } else if (isFullyCollapsed() || this.mTracking || this.mClosing) {
        } else {
            cancelHeightAnimator();
            notifyExpandingStarted();
            this.mClosing = true;
            if (!z) {
                fling(0.0f, false, f, false);
                return;
            }
            this.mNextCollapseSpeedUpFactor = f;
            postDelayed(this.mFlingCollapseRunnable, 120L);
        }
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println(String.format("[PanelView(%s): expandedHeight=%f maxPanelHeight=%d closing=%s tracking=%s justPeeked=%s peekAnim=%s%s timeAnim=%s%s touchDisabled=%s]", getClass().getSimpleName(), Float.valueOf(getExpandedHeight()), Integer.valueOf(getMaxPanelHeight()), this.mClosing ? "T" : "f", this.mTracking ? "T" : "f", this.mJustPeeked ? "T" : "f", this.mPeekAnimator, (this.mPeekAnimator == null || !this.mPeekAnimator.isStarted()) ? "" : " (started)", this.mHeightAnimator, (this.mHeightAnimator == null || !this.mHeightAnimator.isStarted()) ? "" : " (started)", this.mTouchDisabled ? "T" : "f"));
    }

    public void expand(boolean z) {
        if (isFullyCollapsed() || isCollapsing()) {
            this.mInstantExpanding = true;
            this.mUpdateFlingOnLayout = false;
            abortAnimations();
            cancelPeek();
            if (this.mTracking) {
                onTrackingStopped(true);
            }
            if (this.mExpanding) {
                notifyExpandingFinished();
            }
            notifyBarPanelExpansionChanged();
            getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener(this, z) { // from class: com.android.systemui.statusbar.phone.PanelView.6
                final PanelView this$0;
                final boolean val$animate;

                {
                    this.this$0 = this;
                    this.val$animate = z;
                }

                @Override // android.view.ViewTreeObserver.OnGlobalLayoutListener
                public void onGlobalLayout() {
                    if (!this.this$0.mInstantExpanding) {
                        this.this$0.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } else if (this.this$0.mStatusBar.getStatusBarWindow().getHeight() != this.this$0.mStatusBar.getStatusBarHeight()) {
                        this.this$0.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        if (this.val$animate) {
                            this.this$0.notifyExpandingStarted();
                            this.this$0.fling(0.0f, true);
                        } else {
                            this.this$0.setExpandedFraction(1.0f);
                        }
                        this.this$0.mInstantExpanding = false;
                    }
                }
            });
            requestLayout();
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void fling(float f, boolean z) {
        fling(f, z, 1.0f, false);
    }

    protected void fling(float f, boolean z, float f2, boolean z2) {
        cancelPeek();
        float maxPanelHeight = z ? getMaxPanelHeight() : 0.0f;
        if (!z) {
            this.mClosing = true;
        }
        flingToHeight(f, z, maxPanelHeight, f2, z2);
    }

    protected void fling(float f, boolean z, boolean z2) {
        fling(f, z, 1.0f, z2);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean flingExpands(float f, float f2, float f3, float f4) {
        boolean z = true;
        if (isFalseTouch(f3, f4)) {
            return true;
        }
        if (Math.abs(f2) >= this.mFlingAnimationUtils.getMinVelocityPxPerSecond()) {
            return f > 0.0f;
        }
        if (getExpandedFraction() <= 0.5f) {
            z = false;
        }
        return z;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void flingToHeight(float f, boolean z, float f2, float f3, boolean z2) {
        boolean z3 = true;
        boolean z4 = (z && fullyExpandedClearAllVisible() && this.mExpandedHeight < ((float) (getMaxPanelHeight() - getClearAllHeight()))) ? !isClearAllVisible() : false;
        if (z4) {
            f2 = getMaxPanelHeight() - getClearAllHeight();
        }
        if (f2 == this.mExpandedHeight || (getOverExpansionAmount() > 0.0f && z)) {
            notifyExpandingFinished();
            return;
        }
        if (getOverExpansionAmount() <= 0.0f) {
            z3 = false;
        }
        this.mOverExpandedBeforeFling = z3;
        ValueAnimator createHeightAnimator = createHeightAnimator(f2);
        if (z) {
            if (z2) {
                f = 0.0f;
            }
            this.mFlingAnimationUtils.apply(createHeightAnimator, this.mExpandedHeight, f2, f, getHeight());
            if (z2) {
                createHeightAnimator.setDuration(350L);
            }
        } else {
            this.mFlingAnimationUtils.applyDismissing(createHeightAnimator, this.mExpandedHeight, f2, f, getHeight());
            createHeightAnimator.setDuration(48L);
            if (f == 0.0f) {
                createHeightAnimator.setDuration((((float) createHeightAnimator.getDuration()) * getCannedFlingDurationFactor()) / f3);
            }
        }
        createHeightAnimator.addListener(new AnimatorListenerAdapter(this, z4) { // from class: com.android.systemui.statusbar.phone.PanelView.5
            private boolean mCancelled;
            final PanelView this$0;
            final boolean val$clearAllExpandHack;

            {
                this.this$0 = this;
                this.val$clearAllExpandHack = z4;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
                this.mCancelled = true;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                if (this.val$clearAllExpandHack && !this.mCancelled) {
                    this.this$0.setExpandedHeightInternal(this.this$0.getMaxPanelHeight());
                }
                this.this$0.mHeightAnimator = null;
                if (!this.mCancelled) {
                    this.this$0.notifyExpandingFinished();
                }
                this.this$0.notifyBarPanelExpansionChanged();
            }
        });
        this.mHeightAnimator = createHeightAnimator;
        createHeightAnimator.start();
    }

    protected abstract boolean fullyExpandedClearAllVisible();

    protected abstract float getCannedFlingDurationFactor();

    protected abstract int getClearAllHeight();

    public float getExpandedFraction() {
        return this.mExpandedFraction;
    }

    public float getExpandedHeight() {
        return this.mExpandedHeight;
    }

    protected abstract int getMaxPanelHeight();

    protected abstract float getOverExpansionAmount();

    protected abstract float getOverExpansionPixels();

    protected abstract float getPeekHeight();

    protected abstract boolean hasConflictingGestures();

    public void instantCollapse() {
        abortAnimations();
        setExpandedFraction(0.0f);
        if (this.mExpanding) {
            notifyExpandingFinished();
        }
        if (this.mInstantExpanding) {
            this.mInstantExpanding = false;
            notifyBarPanelExpansionChanged();
        }
    }

    protected abstract boolean isClearAllVisible();

    public boolean isCollapsing() {
        return this.mClosing;
    }

    public boolean isFullyCollapsed() {
        return this.mExpandedHeight <= 0.0f;
    }

    public boolean isFullyExpanded() {
        return this.mExpandedHeight >= ((float) getMaxPanelHeight());
    }

    protected abstract boolean isInContentBounds(float f, float f2);

    protected abstract boolean isPanelVisibleBecauseOfHeadsUp();

    protected boolean isScrolledToBottom() {
        return true;
    }

    public boolean isTracking() {
        return this.mTracking;
    }

    protected abstract boolean isTrackingBlocked();

    /* JADX INFO: Access modifiers changed from: protected */
    public void loadDimens() {
        Resources resources = getContext().getResources();
        this.mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        this.mHintDistance = resources.getDimension(2131689895);
        this.mUnlockFalsingThreshold = resources.getDimensionPixelSize(2131689881);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void notifyBarPanelExpansionChanged() {
        PanelBar panelBar = this.mBar;
        float f = this.mExpandedFraction;
        boolean z = true;
        if (this.mExpandedFraction <= 0.0f) {
            z = true;
            if (!this.mPeekPending) {
                if (this.mPeekAnimator != null) {
                    z = true;
                } else {
                    z = true;
                    if (!this.mInstantExpanding) {
                        z = true;
                        if (!isPanelVisibleBecauseOfHeadsUp()) {
                            z = true;
                            if (!this.mTracking) {
                                z = true;
                                if (this.mHeightAnimator == null) {
                                    z = false;
                                }
                            }
                        }
                    }
                }
            }
        }
        panelBar.panelExpansionChanged(f, z);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public final void notifyExpandingFinished() {
        endClosing();
        if (this.mExpanding) {
            this.mExpanding = false;
            onExpandingFinished();
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mViewName = getResources().getResourceName(getId());
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void onClosingFinished() {
        this.mBar.onClosingFinished();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.view.View
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        loadDimens();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean onEmptySpaceClick(float f) {
        if (this.mHintAnimationRunning) {
            return true;
        }
        return onMiddleClicked();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void onExpandingFinished() {
        this.mBar.onExpandingFinished();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void onExpandingStarted() {
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        loadDimens();
    }

    protected abstract void onHeightUpdated(float f);

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        if (this.mInstantExpanding) {
            return false;
        }
        if (!this.mMotionAborted || motionEvent.getActionMasked() == 0) {
            int findPointerIndex = motionEvent.findPointerIndex(this.mTrackingPointer);
            int i = findPointerIndex;
            if (findPointerIndex < 0) {
                i = 0;
                this.mTrackingPointer = motionEvent.getPointerId(0);
            }
            float x = motionEvent.getX(i);
            float y = motionEvent.getY(i);
            boolean isScrolledToBottom = isScrolledToBottom();
            switch (motionEvent.getActionMasked()) {
                case 0:
                    this.mStatusBar.userActivity();
                    this.mAnimatingOnDown = this.mHeightAnimator != null;
                    if ((this.mAnimatingOnDown && this.mClosing && !this.mHintAnimationRunning) || this.mPeekPending || this.mPeekAnimator != null) {
                        cancelHeightAnimator();
                        cancelPeek();
                        this.mTouchSlopExceeded = true;
                        return true;
                    }
                    this.mInitialTouchY = y;
                    this.mInitialTouchX = x;
                    boolean z = true;
                    if (isInContentBounds(x, y)) {
                        z = false;
                    }
                    this.mTouchStartedInEmptyArea = z;
                    this.mTouchSlopExceeded = false;
                    this.mJustPeeked = false;
                    this.mMotionAborted = false;
                    this.mPanelClosedOnDown = isFullyCollapsed();
                    this.mCollapsedAndHeadsUpOnDown = false;
                    this.mHasLayoutedSinceDown = false;
                    this.mUpdateFlingOnLayout = false;
                    this.mTouchAboveFalsingThreshold = false;
                    initVelocityTracker();
                    trackMovement(motionEvent);
                    return false;
                case 1:
                case 3:
                    if (this.mVelocityTracker != null) {
                        this.mVelocityTracker.recycle();
                        this.mVelocityTracker = null;
                        return false;
                    }
                    return false;
                case 2:
                    float f = y - this.mInitialTouchY;
                    trackMovement(motionEvent);
                    if (isScrolledToBottom || this.mTouchStartedInEmptyArea || this.mAnimatingOnDown) {
                        float abs = Math.abs(f);
                        if ((f < (-this.mTouchSlop) || (this.mAnimatingOnDown && abs > this.mTouchSlop)) && abs > Math.abs(x - this.mInitialTouchX)) {
                            cancelHeightAnimator();
                            startExpandMotion(x, y, true, this.mExpandedHeight);
                            return true;
                        }
                        return false;
                    }
                    return false;
                case 4:
                default:
                    return false;
                case 5:
                    if (this.mStatusBar.getBarState() == 1) {
                        this.mMotionAborted = true;
                        if (this.mVelocityTracker != null) {
                            this.mVelocityTracker.recycle();
                            this.mVelocityTracker = null;
                            return false;
                        }
                        return false;
                    }
                    return false;
                case 6:
                    int pointerId = motionEvent.getPointerId(motionEvent.getActionIndex());
                    if (this.mTrackingPointer == pointerId) {
                        int i2 = motionEvent.getPointerId(0) != pointerId ? 0 : 1;
                        this.mTrackingPointer = motionEvent.getPointerId(i2);
                        this.mInitialTouchX = motionEvent.getX(i2);
                        this.mInitialTouchY = motionEvent.getY(i2);
                        return false;
                    }
                    return false;
            }
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.widget.FrameLayout, android.view.ViewGroup, android.view.View
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        this.mStatusBar.onPanelLaidOut();
        requestPanelHeightUpdate();
        this.mHasLayoutedSinceDown = true;
        if (this.mUpdateFlingOnLayout) {
            abortAnimations();
            fling(this.mUpdateFlingVelocity, true);
            this.mUpdateFlingOnLayout = false;
        }
    }

    protected abstract boolean onMiddleClicked();

    /* JADX WARN: Code restructure failed: missing block: B:91:0x0268, code lost:
        if (r6.mIgnoreXTouchSlop != false) goto L91;
     */
    @Override // android.view.View
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public boolean onTouchEvent(MotionEvent motionEvent) {
        boolean z;
        if (this.mInstantExpanding || this.mTouchDisabled) {
            return false;
        }
        if (!this.mMotionAborted || motionEvent.getActionMasked() == 0) {
            if (isFullyCollapsed() && motionEvent.isFromSource(8194)) {
                if (motionEvent.getAction() == 1) {
                    expand(true);
                    return true;
                }
                return true;
            }
            int findPointerIndex = motionEvent.findPointerIndex(this.mTrackingPointer);
            int i = findPointerIndex;
            if (findPointerIndex < 0) {
                i = 0;
                this.mTrackingPointer = motionEvent.getPointerId(0);
            }
            float x = motionEvent.getX(i);
            float y = motionEvent.getY(i);
            if (motionEvent.getActionMasked() == 0) {
                this.mGestureWaitForTouchSlop = !isFullyCollapsed() ? hasConflictingGestures() : true;
                this.mIgnoreXTouchSlop = !isFullyCollapsed() ? shouldGestureIgnoreXTouchSlop(x, y) : true;
            }
            switch (motionEvent.getActionMasked()) {
                case 0:
                    startExpandMotion(x, y, false, this.mExpandedHeight);
                    this.mJustPeeked = false;
                    this.mPanelClosedOnDown = isFullyCollapsed();
                    this.mHasLayoutedSinceDown = false;
                    this.mUpdateFlingOnLayout = false;
                    this.mMotionAborted = false;
                    this.mPeekTouching = this.mPanelClosedOnDown;
                    this.mTouchAboveFalsingThreshold = false;
                    this.mCollapsedAndHeadsUpOnDown = isFullyCollapsed() ? this.mHeadsUpManager.hasPinnedHeadsUp() : false;
                    if (this.mVelocityTracker == null) {
                        initVelocityTracker();
                    }
                    trackMovement(motionEvent);
                    if (!this.mGestureWaitForTouchSlop || ((this.mHeightAnimator != null && !this.mHintAnimationRunning) || this.mPeekPending || this.mPeekAnimator != null)) {
                        cancelHeightAnimator();
                        cancelPeek();
                        if ((this.mHeightAnimator == null || this.mHintAnimationRunning) && !this.mPeekPending) {
                            z = false;
                            if (this.mPeekAnimator != null) {
                                z = true;
                            }
                        } else {
                            z = true;
                        }
                        this.mTouchSlopExceeded = z;
                        onTrackingStarted();
                    }
                    if (isFullyCollapsed() && !this.mHeadsUpManager.hasPinnedHeadsUp()) {
                        schedulePeek();
                        break;
                    }
                    break;
                case 1:
                case 3:
                    trackMovement(motionEvent);
                    endMotionEvent(motionEvent, x, y, false);
                    break;
                case 2:
                    float f = y - this.mInitialTouchY;
                    float f2 = f;
                    if (Math.abs(f) > this.mTouchSlop) {
                        if (Math.abs(f) <= Math.abs(x - this.mInitialTouchX)) {
                            f2 = f;
                            break;
                        }
                        this.mTouchSlopExceeded = true;
                        f2 = f;
                        if (this.mGestureWaitForTouchSlop) {
                            if (this.mTracking) {
                                f2 = f;
                            } else {
                                f2 = f;
                                if (!this.mCollapsedAndHeadsUpOnDown) {
                                    f2 = f;
                                    if (!this.mJustPeeked) {
                                        f2 = f;
                                        if (this.mInitialOffsetOnTouch != 0.0f) {
                                            startExpandMotion(x, y, false, this.mExpandedHeight);
                                            f2 = 0.0f;
                                        }
                                    }
                                    cancelHeightAnimator();
                                    removeCallbacks(this.mPeekRunnable);
                                    this.mPeekPending = false;
                                    onTrackingStarted();
                                }
                            }
                        }
                    }
                    float max = Math.max(0.0f, this.mInitialOffsetOnTouch + f2);
                    if (max > this.mPeekHeight) {
                        if (this.mPeekAnimator != null) {
                            this.mPeekAnimator.cancel();
                        }
                        this.mJustPeeked = false;
                    }
                    if ((-f2) >= getFalsingThreshold()) {
                        this.mTouchAboveFalsingThreshold = true;
                        this.mUpwardsWhenTresholdReached = isDirectionUpwards(x, y);
                    }
                    if (!this.mJustPeeked && ((!this.mGestureWaitForTouchSlop || this.mTracking) && !isTrackingBlocked())) {
                        setExpandedHeightInternal(max);
                    }
                    trackMovement(motionEvent);
                    break;
                case 5:
                    if (this.mStatusBar.getBarState() == 1) {
                        this.mMotionAborted = true;
                        endMotionEvent(motionEvent, x, y, true);
                        return false;
                    }
                    break;
                case 6:
                    int pointerId = motionEvent.getPointerId(motionEvent.getActionIndex());
                    if (this.mTrackingPointer == pointerId) {
                        int i2 = motionEvent.getPointerId(0) != pointerId ? 0 : 1;
                        float y2 = motionEvent.getY(i2);
                        float x2 = motionEvent.getX(i2);
                        this.mTrackingPointer = motionEvent.getPointerId(i2);
                        startExpandMotion(x2, y2, true, this.mExpandedHeight);
                        break;
                    }
                    break;
            }
            boolean z2 = true;
            if (this.mGestureWaitForTouchSlop) {
                z2 = this.mTracking;
            }
            return z2;
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void onTrackingStarted() {
        endClosing();
        this.mTracking = true;
        this.mCollapseAfterPeek = false;
        this.mBar.onTrackingStarted();
        notifyExpandingStarted();
        notifyBarPanelExpansionChanged();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void onTrackingStopped(boolean z) {
        this.mTracking = false;
        this.mBar.onTrackingStopped(z);
        notifyBarPanelExpansionChanged();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void requestPanelHeightUpdate() {
        float maxPanelHeight = getMaxPanelHeight();
        if ((this.mTracking && !isTrackingBlocked()) || this.mHeightAnimator != null || isFullyCollapsed() || maxPanelHeight == this.mExpandedHeight || this.mPeekPending || this.mPeekAnimator != null || this.mPeekTouching) {
            return;
        }
        setExpandedHeight(maxPanelHeight);
    }

    public abstract void resetViews();

    public void setBar(PanelBar panelBar) {
        this.mBar = panelBar;
    }

    public void setExpandedFraction(float f) {
        setExpandedHeight(getMaxPanelHeight() * f);
    }

    public void setExpandedHeight(float f) {
        setExpandedHeightInternal(getOverExpansionPixels() + f);
    }

    public void setExpandedHeightInternal(float f) {
        float maxPanelHeight = getMaxPanelHeight() - getOverExpansionAmount();
        if (this.mHeightAnimator == null) {
            float max = Math.max(0.0f, f - maxPanelHeight);
            if (getOverExpansionPixels() != max && this.mTracking) {
                setOverExpansion(max, true);
            }
            this.mExpandedHeight = Math.min(f, maxPanelHeight) + getOverExpansionAmount();
        } else {
            this.mExpandedHeight = f;
            if (this.mOverExpandedBeforeFling) {
                setOverExpansion(Math.max(0.0f, f - maxPanelHeight), false);
            }
        }
        this.mExpandedHeight = Math.max(0.0f, this.mExpandedHeight);
        this.mExpandedFraction = Math.min(1.0f, maxPanelHeight == 0.0f ? 0.0f : this.mExpandedHeight / maxPanelHeight);
        onHeightUpdated(this.mExpandedHeight);
        notifyBarPanelExpansionChanged();
    }

    public void setHeadsUpManager(HeadsUpManager headsUpManager) {
        this.mHeadsUpManager = headsUpManager;
    }

    protected abstract void setOverExpansion(float f, boolean z);

    public void setTouchDisabled(boolean z) {
        this.mTouchDisabled = z;
    }

    protected abstract boolean shouldGestureIgnoreXTouchSlop(float f, float f2);

    /* JADX INFO: Access modifiers changed from: protected */
    public void startExpandMotion(float f, float f2, boolean z, float f3) {
        this.mInitialOffsetOnTouch = f3;
        this.mInitialTouchY = f2;
        this.mInitialTouchX = f;
        if (z) {
            this.mTouchSlopExceeded = true;
            setExpandedHeight(this.mInitialOffsetOnTouch);
            onTrackingStarted();
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void startUnlockHintAnimation() {
        if (this.mHeightAnimator != null || this.mTracking) {
            return;
        }
        cancelPeek();
        notifyExpandingStarted();
        startUnlockHintAnimationPhase1(new Runnable(this) { // from class: com.android.systemui.statusbar.phone.PanelView.7
            final PanelView this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.notifyExpandingFinished();
                this.this$0.mStatusBar.onHintFinished();
                this.this$0.mHintAnimationRunning = false;
            }
        });
        this.mStatusBar.onUnlockHintStarted();
        this.mHintAnimationRunning = true;
    }
}
