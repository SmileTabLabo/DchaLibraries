package com.android.systemui.statusbar.phone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.BenesseExtension;
import android.util.AttributeSet;
import android.util.EventLog;
import android.util.MathUtils;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.view.accessibility.AccessibilityEvent;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.internal.logging.MetricsLogger;
import com.android.keyguard.KeyguardStatusView;
import com.android.keyguard.R$id;
import com.android.systemui.AutoReinflateContainer;
import com.android.systemui.DejankUtils;
import com.android.systemui.EventLogTags;
import com.android.systemui.Interpolators;
import com.android.systemui.classifier.FalsingManager;
import com.android.systemui.qs.QSContainer;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.ExpandableView;
import com.android.systemui.statusbar.FlingAnimationUtils;
import com.android.systemui.statusbar.GestureRecorder;
import com.android.systemui.statusbar.KeyguardAffordanceView;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.phone.KeyguardAffordanceHelper;
import com.android.systemui.statusbar.phone.KeyguardClockPositionAlgorithm;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import com.android.systemui.statusbar.policy.KeyguardUserSwitcher;
import com.android.systemui.statusbar.stack.NotificationStackScrollLayout;
import java.util.List;
/* loaded from: a.zip:com/android/systemui/statusbar/phone/NotificationPanelView.class */
public class NotificationPanelView extends PanelView implements ExpandableView.OnHeightChangedListener, View.OnClickListener, NotificationStackScrollLayout.OnOverscrollTopChangedListener, KeyguardAffordanceHelper.Callback, NotificationStackScrollLayout.OnEmptySpaceClickListener, HeadsUpManager.OnHeadsUpChangedListener {
    private static final Rect mDummyDirtyRect = new Rect(0, 0, 1, 1);
    private KeyguardAffordanceHelper mAfforanceHelper;
    private final Runnable mAnimateKeyguardBottomAreaInvisibleEndRunnable;
    private final Runnable mAnimateKeyguardStatusBarInvisibleEndRunnable;
    private final Runnable mAnimateKeyguardStatusViewInvisibleEndRunnable;
    private final Runnable mAnimateKeyguardStatusViewVisibleEndRunnable;
    private boolean mAnimateNextTopPaddingChange;
    private boolean mBlockTouches;
    private int mClockAnimationTarget;
    private ObjectAnimator mClockAnimator;
    private KeyguardClockPositionAlgorithm mClockPositionAlgorithm;
    private KeyguardClockPositionAlgorithm.Result mClockPositionResult;
    private TextView mClockView;
    private boolean mClosingWithAlphaFadeOut;
    private boolean mCollapsedOnDown;
    private boolean mConflictingQsExpansionGesture;
    private boolean mDozing;
    private boolean mDozingOnDown;
    private float mEmptyDragAmount;
    private boolean mExpandingFromHeadsUp;
    private FalsingManager mFalsingManager;
    private FlingAnimationUtils mFlingAnimationUtils;
    private NotificationGroupManager mGroupManager;
    private boolean mHeadsUpAnimatingAway;
    private Runnable mHeadsUpExistenceChangedRunnable;
    private HeadsUpTouchHelper mHeadsUpTouchHelper;
    private float mInitialHeightOnTouch;
    private float mInitialTouchX;
    private float mInitialTouchY;
    private boolean mIntercepting;
    private boolean mIsExpanding;
    private boolean mIsExpansionFromHeadsUp;
    private boolean mIsLaunchTransitionFinished;
    private boolean mIsLaunchTransitionRunning;
    private boolean mKeyguardShowing;
    private KeyguardStatusBarView mKeyguardStatusBar;
    private float mKeyguardStatusBarAnimateAlpha;
    private KeyguardStatusView mKeyguardStatusView;
    private boolean mKeyguardStatusViewAnimating;
    private KeyguardUserSwitcher mKeyguardUserSwitcher;
    private boolean mLastAnnouncementWasQuickSettings;
    private String mLastCameraLaunchSource;
    private int mLastOrientation;
    private float mLastOverscroll;
    private float mLastTouchX;
    private float mLastTouchY;
    private Runnable mLaunchAnimationEndRunnable;
    private boolean mLaunchingAffordance;
    private boolean mListenForHeadsUp;
    private int mNavigationBarBottomHeight;
    protected NotificationsQuickSettingsContainer mNotificationContainerParent;
    private int mNotificationScrimWaitDistance;
    protected NotificationStackScrollLayout mNotificationStackScroller;
    private int mNotificationsHeaderCollideDistance;
    private int mOldLayoutDirection;
    private boolean mOnlyAffordanceInThisMotion;
    private boolean mPanelExpanded;
    private int mPositionMinSideMargin;
    private boolean mQsAnimatorExpand;
    private AutoReinflateContainer mQsAutoReinflateContainer;
    protected QSContainer mQsContainer;
    private boolean mQsExpandImmediate;
    private boolean mQsExpanded;
    private boolean mQsExpandedWhenExpandingStarted;
    private ValueAnimator mQsExpansionAnimator;
    protected boolean mQsExpansionEnabled;
    private boolean mQsExpansionFromOverscroll;
    protected float mQsExpansionHeight;
    private int mQsFalsingThreshold;
    private boolean mQsFullyExpanded;
    protected int mQsMaxExpansionHeight;
    protected int mQsMinExpansionHeight;
    private View mQsNavbarScrim;
    private int mQsPeekHeight;
    private boolean mQsScrimEnabled;
    private ValueAnimator mQsSizeChangeAnimator;
    private boolean mQsTouchAboveFalsingThreshold;
    private boolean mQsTracking;
    private boolean mShadeEmpty;
    private boolean mStackScrollerOverscrolling;
    private final ValueAnimator.AnimatorUpdateListener mStatusBarAnimateAlphaListener;
    private int mStatusBarMinHeight;
    private int mStatusBarState;
    private int mTopPaddingAdjustment;
    private int mTrackingPointer;
    private boolean mTwoFingerQsExpandPossible;
    private boolean mUnlockIconActive;
    private int mUnlockMoveDistance;
    private final Runnable mUpdateHeader;
    private VelocityTracker mVelocityTracker;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.android.systemui.statusbar.phone.NotificationPanelView$11  reason: invalid class name */
    /* loaded from: a.zip:com/android/systemui/statusbar/phone/NotificationPanelView$11.class */
    public class AnonymousClass11 implements ViewTreeObserver.OnPreDrawListener {
        final NotificationPanelView this$0;

        AnonymousClass11(NotificationPanelView notificationPanelView) {
            this.this$0 = notificationPanelView;
        }

        @Override // android.view.ViewTreeObserver.OnPreDrawListener
        public boolean onPreDraw() {
            this.this$0.getViewTreeObserver().removeOnPreDrawListener(this);
            if (this.this$0.mClockAnimator != null) {
                this.this$0.mClockAnimator.removeAllListeners();
                this.this$0.mClockAnimator.cancel();
            }
            this.this$0.mClockAnimator = ObjectAnimator.ofFloat(this.this$0.mKeyguardStatusView, View.Y, this.this$0.mClockAnimationTarget);
            this.this$0.mClockAnimator.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
            this.this$0.mClockAnimator.setDuration(360L);
            this.this$0.mClockAnimator.addListener(new AnimatorListenerAdapter(this) { // from class: com.android.systemui.statusbar.phone.NotificationPanelView.11.1
                final AnonymousClass11 this$1;

                {
                    this.this$1 = this;
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    this.this$1.this$0.mClockAnimator = null;
                    this.this$1.this$0.mClockAnimationTarget = -1;
                }
            });
            this.this$0.mClockAnimator.start();
            return true;
        }
    }

    /* renamed from: com.android.systemui.statusbar.phone.NotificationPanelView$8  reason: invalid class name */
    /* loaded from: a.zip:com/android/systemui/statusbar/phone/NotificationPanelView$8.class */
    class AnonymousClass8 implements AutoReinflateContainer.InflateListener {
        final NotificationPanelView this$0;

        AnonymousClass8(NotificationPanelView notificationPanelView) {
            this.this$0 = notificationPanelView;
        }

        @Override // com.android.systemui.AutoReinflateContainer.InflateListener
        public void onInflated(View view) {
            this.this$0.mQsContainer = (QSContainer) view.findViewById(2131886585);
            this.this$0.mQsContainer.setPanelView(this.this$0);
            this.this$0.mQsContainer.getHeader().findViewById(2131886599).setOnClickListener(this.this$0);
            this.this$0.mQsContainer.addOnLayoutChangeListener(new View.OnLayoutChangeListener(this) { // from class: com.android.systemui.statusbar.phone.NotificationPanelView.8.1
                final AnonymousClass8 this$1;

                {
                    this.this$1 = this;
                }

                @Override // android.view.View.OnLayoutChangeListener
                public void onLayoutChange(View view2, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                    if (i4 - i2 != i8 - i6) {
                        this.this$1.this$0.onQsHeightChanged();
                    }
                }
            });
            this.this$0.mNotificationStackScroller.setQsContainer(this.this$0.mQsContainer);
        }
    }

    public NotificationPanelView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mQsExpansionEnabled = true;
        this.mClockAnimationTarget = -1;
        this.mClockPositionAlgorithm = new KeyguardClockPositionAlgorithm();
        this.mClockPositionResult = new KeyguardClockPositionAlgorithm.Result();
        this.mQsScrimEnabled = true;
        this.mKeyguardStatusBarAnimateAlpha = 1.0f;
        this.mLastOrientation = -1;
        this.mLastCameraLaunchSource = "lockscreen_affordance";
        this.mHeadsUpExistenceChangedRunnable = new Runnable(this) { // from class: com.android.systemui.statusbar.phone.NotificationPanelView.1
            final NotificationPanelView this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.mHeadsUpAnimatingAway = false;
                this.this$0.notifyBarPanelExpansionChanged();
            }
        };
        this.mAnimateKeyguardStatusViewInvisibleEndRunnable = new Runnable(this) { // from class: com.android.systemui.statusbar.phone.NotificationPanelView.2
            final NotificationPanelView this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.mKeyguardStatusViewAnimating = false;
                this.this$0.mKeyguardStatusView.setVisibility(8);
            }
        };
        this.mAnimateKeyguardStatusViewVisibleEndRunnable = new Runnable(this) { // from class: com.android.systemui.statusbar.phone.NotificationPanelView.3
            final NotificationPanelView this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.mKeyguardStatusViewAnimating = false;
            }
        };
        this.mAnimateKeyguardStatusBarInvisibleEndRunnable = new Runnable(this) { // from class: com.android.systemui.statusbar.phone.NotificationPanelView.4
            final NotificationPanelView this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.mKeyguardStatusBar.setVisibility(4);
                this.this$0.mKeyguardStatusBar.setAlpha(1.0f);
                this.this$0.mKeyguardStatusBarAnimateAlpha = 1.0f;
            }
        };
        this.mStatusBarAnimateAlphaListener = new ValueAnimator.AnimatorUpdateListener(this) { // from class: com.android.systemui.statusbar.phone.NotificationPanelView.5
            final NotificationPanelView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                this.this$0.mKeyguardStatusBarAnimateAlpha = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                this.this$0.updateHeaderKeyguardAlpha();
            }
        };
        this.mAnimateKeyguardBottomAreaInvisibleEndRunnable = new Runnable(this) { // from class: com.android.systemui.statusbar.phone.NotificationPanelView.6
            final NotificationPanelView this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.mKeyguardBottomArea.setVisibility(8);
            }
        };
        this.mUpdateHeader = new Runnable(this) { // from class: com.android.systemui.statusbar.phone.NotificationPanelView.7
            final NotificationPanelView this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.mQsContainer.getHeader().updateEverything();
            }
        };
        setWillNotDraw(true);
        this.mFalsingManager = FalsingManager.getInstance(context);
    }

    private void animateKeyguardStatusBarIn(long j) {
        this.mKeyguardStatusBar.setVisibility(0);
        this.mKeyguardStatusBar.setAlpha(0.0f);
        ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        ofFloat.addUpdateListener(this.mStatusBarAnimateAlphaListener);
        ofFloat.setDuration(j);
        ofFloat.setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN);
        ofFloat.start();
    }

    private void animateKeyguardStatusBarOut() {
        ValueAnimator ofFloat = ValueAnimator.ofFloat(this.mKeyguardStatusBar.getAlpha(), 0.0f);
        ofFloat.addUpdateListener(this.mStatusBarAnimateAlphaListener);
        ofFloat.setStartDelay(this.mStatusBar.isKeyguardFadingAway() ? this.mStatusBar.getKeyguardFadingAwayDelay() : 0L);
        ofFloat.setDuration(this.mStatusBar.isKeyguardFadingAway() ? this.mStatusBar.getKeyguardFadingAwayDuration() / 2 : 360L);
        ofFloat.setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN);
        ofFloat.addListener(new AnimatorListenerAdapter(this) { // from class: com.android.systemui.statusbar.phone.NotificationPanelView.13
            final NotificationPanelView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                this.this$0.mAnimateKeyguardStatusBarInvisibleEndRunnable.run();
            }
        });
        ofFloat.start();
    }

    private int calculatePanelHeightQsExpanded() {
        float height = (this.mNotificationStackScroller.getHeight() - this.mNotificationStackScroller.getEmptyBottomMargin()) - this.mNotificationStackScroller.getTopPadding();
        float f = height;
        if (this.mNotificationStackScroller.getNotGoneChildCount() == 0) {
            f = height;
            if (this.mShadeEmpty) {
                f = this.mNotificationStackScroller.getEmptyShadeViewHeight() + this.mNotificationStackScroller.getBottomStackPeekSize() + this.mNotificationStackScroller.getBottomStackSlowDownHeight();
            }
        }
        int i = this.mQsMaxExpansionHeight;
        if (this.mQsSizeChangeAnimator != null) {
            i = ((Integer) this.mQsSizeChangeAnimator.getAnimatedValue()).intValue();
        }
        float max = Math.max(i, this.mStatusBarState == 1 ? this.mClockPositionResult.stackScrollerPadding - this.mTopPaddingAdjustment : 0) + f;
        float f2 = max;
        if (max > this.mNotificationStackScroller.getHeight()) {
            f2 = Math.max(this.mNotificationStackScroller.getLayoutMinHeight() + i, this.mNotificationStackScroller.getHeight());
        }
        return (int) f2;
    }

    private int calculatePanelHeightShade() {
        return (int) (((this.mNotificationStackScroller.getHeight() - this.mNotificationStackScroller.getEmptyBottomMargin()) - this.mTopPaddingAdjustment) + this.mNotificationStackScroller.getTopPaddingOverflow());
    }

    private float calculateQsTopPadding() {
        if (!this.mKeyguardShowing || (!this.mQsExpandImmediate && (!this.mIsExpanding || !this.mQsExpandedWhenExpandingStarted))) {
            return this.mQsSizeChangeAnimator != null ? ((Integer) this.mQsSizeChangeAnimator.getAnimatedValue()).intValue() : this.mKeyguardShowing ? interpolate(getQsExpansionFraction(), this.mNotificationStackScroller.getIntrinsicPadding(), this.mQsMaxExpansionHeight) : this.mQsExpansionHeight;
        }
        int i = this.mClockPositionResult.stackScrollerPadding;
        int i2 = this.mClockPositionResult.stackScrollerPaddingAdjustment;
        int tempQsMaxExpansion = getTempQsMaxExpansion();
        if (this.mStatusBarState == 1) {
            tempQsMaxExpansion = Math.max(i - i2, tempQsMaxExpansion);
        }
        return (int) interpolate(getExpandedFraction(), this.mQsMinExpansionHeight, tempQsMaxExpansion);
    }

    private void cancelQsAnimation() {
        if (this.mQsExpansionAnimator != null) {
            this.mQsExpansionAnimator.cancel();
        }
    }

    private boolean flingExpandsQs(float f) {
        boolean z = true;
        if (isFalseTouch()) {
            return false;
        }
        if (Math.abs(f) >= this.mFlingAnimationUtils.getMinVelocityPxPerSecond()) {
            return f > 0.0f;
        }
        if (getQsExpansionFraction() <= 0.5f) {
            z = false;
        }
        return z;
    }

    private void flingQsWithCurrentVelocity(float f, boolean z) {
        float currentVelocity = getCurrentVelocity();
        boolean flingExpandsQs = flingExpandsQs(currentVelocity);
        if (flingExpandsQs) {
            logQsSwipeDown(f);
        }
        boolean z2 = false;
        if (flingExpandsQs) {
            z2 = !z;
        }
        flingSettings(currentVelocity, z2);
    }

    private void flingSettings(float f, boolean z) {
        flingSettings(f, z, null, false);
    }

    private void flingSettings(float f, boolean z, Runnable runnable, boolean z2) {
        float f2 = z ? this.mQsMaxExpansionHeight : this.mQsMinExpansionHeight;
        if (f2 == this.mQsExpansionHeight) {
            if (runnable != null) {
                runnable.run();
                return;
            }
            return;
        }
        boolean isFalseTouch = isFalseTouch();
        if (isFalseTouch) {
            f = 0.0f;
        }
        ValueAnimator ofFloat = ValueAnimator.ofFloat(this.mQsExpansionHeight, f2);
        if (z2) {
            ofFloat.setInterpolator(Interpolators.TOUCH_RESPONSE);
            ofFloat.setDuration(368L);
        } else {
            this.mFlingAnimationUtils.apply(ofFloat, this.mQsExpansionHeight, f2, f);
        }
        if (isFalseTouch) {
            ofFloat.setDuration(350L);
        }
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(this) { // from class: com.android.systemui.statusbar.phone.NotificationPanelView.14
            final NotificationPanelView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                this.this$0.setQsExpansion(((Float) valueAnimator.getAnimatedValue()).floatValue());
            }
        });
        ofFloat.addListener(new AnimatorListenerAdapter(this, runnable) { // from class: com.android.systemui.statusbar.phone.NotificationPanelView.15
            final NotificationPanelView this$0;
            final Runnable val$onFinishRunnable;

            {
                this.this$0 = this;
                this.val$onFinishRunnable = runnable;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                this.this$0.mQsExpansionAnimator = null;
                if (this.val$onFinishRunnable != null) {
                    this.val$onFinishRunnable.run();
                }
            }
        });
        ofFloat.start();
        this.mQsExpansionAnimator = ofFloat;
        this.mQsAnimatorExpand = z;
    }

    private float getCurrentVelocity() {
        if (this.mVelocityTracker == null) {
            return 0.0f;
        }
        this.mVelocityTracker.computeCurrentVelocity(1000);
        return this.mVelocityTracker.getYVelocity();
    }

    private float getFadeoutAlpha() {
        return (float) Math.pow(Math.max(0.0f, Math.min((getNotificationsTopY() + this.mNotificationStackScroller.getFirstItemMinHeight()) / ((this.mQsMinExpansionHeight + this.mNotificationStackScroller.getBottomStackPeekSize()) - this.mNotificationStackScroller.getBottomStackSlowDownHeight()), 1.0f)), 0.75d);
    }

    private int getFalsingThreshold() {
        return (int) (this.mQsFalsingThreshold * (this.mStatusBar.isWakeUpComingFromTouch() ? 1.5f : 1.0f));
    }

    private float getKeyguardContentsAlpha() {
        return (float) Math.pow(MathUtils.constrain(this.mStatusBar.getBarState() == 1 ? getNotificationsTopY() / (this.mKeyguardStatusBar.getHeight() + this.mNotificationsHeaderCollideDistance) : getNotificationsTopY() / this.mKeyguardStatusBar.getHeight(), 0.0f, 1.0f), 0.75d);
    }

    private String getKeyguardOrLockScreenString() {
        return this.mQsContainer.isCustomizing() ? getContext().getString(2131493901) : this.mStatusBarState == 1 ? getContext().getString(2131493445) : getContext().getString(2131493443);
    }

    private float getNotificationsTopY() {
        return this.mNotificationStackScroller.getNotGoneChildCount() == 0 ? getExpandedHeight() : this.mNotificationStackScroller.getNotificationsTopY();
    }

    private float getQsExpansionFraction() {
        return Math.min(1.0f, (this.mQsExpansionHeight - this.mQsMinExpansionHeight) / (getTempQsMaxExpansion() - this.mQsMinExpansionHeight));
    }

    private int getTempQsMaxExpansion() {
        return this.mQsMaxExpansionHeight;
    }

    private void handleQsDown(MotionEvent motionEvent) {
        if (motionEvent.getActionMasked() == 0 && shouldQuickSettingsIntercept(motionEvent.getX(), motionEvent.getY(), -1.0f)) {
            this.mFalsingManager.onQsDown();
            this.mQsTracking = true;
            onQsExpansionStarted();
            this.mInitialHeightOnTouch = this.mQsExpansionHeight;
            this.mInitialTouchY = motionEvent.getX();
            this.mInitialTouchX = motionEvent.getY();
            notifyExpandingFinished();
        }
    }

    private boolean handleQsTouch(MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == 0 && getExpandedFraction() == 1.0f && this.mStatusBar.getBarState() != 1 && !this.mQsExpanded && this.mQsExpansionEnabled) {
            this.mQsTracking = true;
            this.mConflictingQsExpansionGesture = true;
            onQsExpansionStarted();
            this.mInitialHeightOnTouch = this.mQsExpansionHeight;
            this.mInitialTouchY = motionEvent.getX();
            this.mInitialTouchX = motionEvent.getY();
        }
        if (!isFullyCollapsed()) {
            handleQsDown(motionEvent);
        }
        if (!this.mQsExpandImmediate && this.mQsTracking) {
            onQsTouch(motionEvent);
            if (!this.mConflictingQsExpansionGesture) {
                return true;
            }
        }
        if (actionMasked == 3 || actionMasked == 1) {
            this.mConflictingQsExpansionGesture = false;
        }
        if (actionMasked == 0 && isFullyCollapsed() && this.mQsExpansionEnabled) {
            this.mTwoFingerQsExpandPossible = true;
        }
        if (this.mTwoFingerQsExpandPossible && isOpenQsEvent(motionEvent) && motionEvent.getY(motionEvent.getActionIndex()) < this.mStatusBarMinHeight) {
            MetricsLogger.count(this.mContext, "panel_open_qs", 1);
            this.mQsExpandImmediate = true;
            requestPanelHeightUpdate();
            setListening(true);
            return false;
        }
        return false;
    }

    private void initDownStates(MotionEvent motionEvent) {
        boolean z = false;
        if (motionEvent.getActionMasked() == 0) {
            this.mOnlyAffordanceInThisMotion = false;
            this.mQsTouchAboveFalsingThreshold = this.mQsFullyExpanded;
            this.mDozingOnDown = isDozing();
            this.mCollapsedOnDown = isFullyCollapsed();
            if (this.mCollapsedOnDown) {
                z = this.mHeadsUpManager.hasPinnedHeadsUp();
            }
            this.mListenForHeadsUp = z;
        }
    }

    private void initVelocityTracker() {
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.recycle();
        }
        this.mVelocityTracker = VelocityTracker.obtain();
    }

    private static float interpolate(float f, float f2, float f3) {
        return ((1.0f - f) * f2) + (f * f3);
    }

    private boolean isFalseTouch() {
        boolean z = false;
        if (needsAntiFalsing()) {
            if (this.mFalsingManager.isClassiferEnabled()) {
                return this.mFalsingManager.isFalseTouch();
            }
            if (!this.mQsTouchAboveFalsingThreshold) {
                z = true;
            }
            return z;
        }
        return false;
    }

    private boolean isForegroundApp(String str) {
        boolean z = false;
        List<ActivityManager.RunningTaskInfo> runningTasks = ((ActivityManager) getContext().getSystemService(ActivityManager.class)).getRunningTasks(1);
        if (!runningTasks.isEmpty()) {
            z = str.equals(runningTasks.get(0).topActivity.getPackageName());
        }
        return z;
    }

    private boolean isInQsArea(float f, float f2) {
        boolean z;
        if (f < this.mQsAutoReinflateContainer.getX() || f > this.mQsAutoReinflateContainer.getX() + this.mQsAutoReinflateContainer.getWidth()) {
            z = false;
        } else {
            z = true;
            if (f2 > this.mNotificationStackScroller.getBottomMostNotificationBottom()) {
                z = f2 <= this.mQsContainer.getY() + ((float) this.mQsContainer.getHeight());
            }
        }
        return z;
    }

    private boolean isInSettings() {
        return this.mQsExpanded;
    }

    private boolean isOpenQsEvent(MotionEvent motionEvent) {
        int pointerCount = motionEvent.getPointerCount();
        int actionMasked = motionEvent.getActionMasked();
        boolean z = actionMasked == 5 ? pointerCount == 2 : false;
        boolean isButtonPressed = actionMasked == 0 ? !motionEvent.isButtonPressed(32) ? motionEvent.isButtonPressed(64) : true : false;
        boolean isButtonPressed2 = actionMasked == 0 ? !motionEvent.isButtonPressed(2) ? motionEvent.isButtonPressed(4) : true : false;
        if (z || isButtonPressed) {
            isButtonPressed2 = true;
        }
        return isButtonPressed2;
    }

    private void logQsSwipeDown(float f) {
        EventLogTags.writeSysuiLockscreenGesture(this.mStatusBarState == 1 ? 8 : 9, (int) ((f - this.mInitialTouchY) / this.mStatusBar.getDisplayDensity()), (int) (getCurrentVelocity() / this.mStatusBar.getDisplayDensity()));
    }

    private void onQsExpansionStarted() {
        onQsExpansionStarted(0);
    }

    private void onQsExpansionStarted(int i) {
        cancelQsAnimation();
        cancelHeightAnimator();
        setQsExpansion(this.mQsExpansionHeight - i);
        requestPanelHeightUpdate();
    }

    private boolean onQsIntercept(MotionEvent motionEvent) {
        boolean z = true;
        int findPointerIndex = motionEvent.findPointerIndex(this.mTrackingPointer);
        int i = findPointerIndex;
        if (findPointerIndex < 0) {
            i = 0;
            this.mTrackingPointer = motionEvent.getPointerId(0);
        }
        float x = motionEvent.getX(i);
        float y = motionEvent.getY(i);
        switch (motionEvent.getActionMasked()) {
            case 0:
                this.mIntercepting = true;
                this.mInitialTouchY = y;
                this.mInitialTouchX = x;
                initVelocityTracker();
                trackMovement(motionEvent);
                if (shouldQuickSettingsIntercept(this.mInitialTouchX, this.mInitialTouchY, 0.0f)) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                if (this.mQsExpansionAnimator != null) {
                    onQsExpansionStarted();
                    this.mInitialHeightOnTouch = this.mQsExpansionHeight;
                    this.mQsTracking = true;
                    this.mIntercepting = false;
                    this.mNotificationStackScroller.removeLongPressCallback();
                    return false;
                }
                return false;
            case 1:
            case 3:
                trackMovement(motionEvent);
                if (this.mQsTracking) {
                    if (motionEvent.getActionMasked() != 3) {
                        z = false;
                    }
                    flingQsWithCurrentVelocity(y, z);
                    this.mQsTracking = false;
                }
                this.mIntercepting = false;
                return false;
            case 2:
                float f = y - this.mInitialTouchY;
                trackMovement(motionEvent);
                if (this.mQsTracking) {
                    setQsExpansion(this.mInitialHeightOnTouch + f);
                    trackMovement(motionEvent);
                    this.mIntercepting = false;
                    return true;
                } else if (Math.abs(f) <= this.mTouchSlop || Math.abs(f) <= Math.abs(x - this.mInitialTouchX) || !shouldQuickSettingsIntercept(this.mInitialTouchX, this.mInitialTouchY, f)) {
                    return false;
                } else {
                    this.mQsTracking = true;
                    onQsExpansionStarted();
                    notifyExpandingFinished();
                    this.mInitialHeightOnTouch = this.mQsExpansionHeight;
                    this.mInitialTouchY = y;
                    this.mInitialTouchX = x;
                    this.mIntercepting = false;
                    this.mNotificationStackScroller.removeLongPressCallback();
                    return true;
                }
            case 4:
            case 5:
            default:
                return false;
            case 6:
                int pointerId = motionEvent.getPointerId(motionEvent.getActionIndex());
                if (this.mTrackingPointer == pointerId) {
                    int i2 = 1;
                    if (motionEvent.getPointerId(0) != pointerId) {
                        i2 = 0;
                    }
                    this.mTrackingPointer = motionEvent.getPointerId(i2);
                    this.mInitialTouchX = motionEvent.getX(i2);
                    this.mInitialTouchY = motionEvent.getY(i2);
                    return false;
                }
                return false;
        }
    }

    private void onQsTouch(MotionEvent motionEvent) {
        boolean z = true;
        int findPointerIndex = motionEvent.findPointerIndex(this.mTrackingPointer);
        int i = findPointerIndex;
        if (findPointerIndex < 0) {
            i = 0;
            this.mTrackingPointer = motionEvent.getPointerId(0);
        }
        float y = motionEvent.getY(i);
        float x = motionEvent.getX(i);
        float f = y - this.mInitialTouchY;
        switch (motionEvent.getActionMasked()) {
            case 0:
                this.mQsTracking = true;
                this.mInitialTouchY = y;
                this.mInitialTouchX = x;
                onQsExpansionStarted();
                this.mInitialHeightOnTouch = this.mQsExpansionHeight;
                initVelocityTracker();
                trackMovement(motionEvent);
                return;
            case 1:
            case 3:
                this.mQsTracking = false;
                this.mTrackingPointer = -1;
                trackMovement(motionEvent);
                if (getQsExpansionFraction() != 0.0f || y >= this.mInitialTouchY) {
                    if (motionEvent.getActionMasked() != 3) {
                        z = false;
                    }
                    flingQsWithCurrentVelocity(y, z);
                }
                if (this.mVelocityTracker != null) {
                    this.mVelocityTracker.recycle();
                    this.mVelocityTracker = null;
                    return;
                }
                return;
            case 2:
                setQsExpansion(this.mInitialHeightOnTouch + f);
                if (f >= getFalsingThreshold()) {
                    this.mQsTouchAboveFalsingThreshold = true;
                }
                trackMovement(motionEvent);
                return;
            case 4:
            case 5:
            default:
                return;
            case 6:
                int pointerId = motionEvent.getPointerId(motionEvent.getActionIndex());
                if (this.mTrackingPointer == pointerId) {
                    int i2 = motionEvent.getPointerId(0) != pointerId ? 0 : 1;
                    float y2 = motionEvent.getY(i2);
                    float x2 = motionEvent.getX(i2);
                    this.mTrackingPointer = motionEvent.getPointerId(i2);
                    this.mInitialHeightOnTouch = this.mQsExpansionHeight;
                    this.mInitialTouchY = y2;
                    this.mInitialTouchX = x2;
                    return;
                }
                return;
        }
    }

    private void positionClockAndNotifications() {
        int i;
        boolean isAddOrRemoveAnimationPending = this.mNotificationStackScroller.isAddOrRemoveAnimationPending();
        if (this.mStatusBarState != 1) {
            i = this.mStatusBarState == 0 ? this.mQsContainer.getHeader().getHeight() + this.mQsPeekHeight : this.mKeyguardStatusBar.getHeight();
            this.mTopPaddingAdjustment = 0;
        } else {
            this.mClockPositionAlgorithm.setup(this.mStatusBar.getMaxKeyguardNotifications(), getMaxPanelHeight(), getExpandedHeight(), this.mNotificationStackScroller.getNotGoneChildCount(), getHeight(), this.mKeyguardStatusView.getHeight(), this.mEmptyDragAmount);
            this.mClockPositionAlgorithm.run(this.mClockPositionResult);
            if (isAddOrRemoveAnimationPending || this.mClockAnimator != null) {
                startClockAnimation(this.mClockPositionResult.clockY);
            } else {
                this.mKeyguardStatusView.setY(this.mClockPositionResult.clockY);
            }
            updateClock(this.mClockPositionResult.clockAlpha, this.mClockPositionResult.clockScale);
            i = this.mClockPositionResult.stackScrollerPadding;
            this.mTopPaddingAdjustment = this.mClockPositionResult.stackScrollerPaddingAdjustment;
        }
        this.mNotificationStackScroller.setIntrinsicPadding(i);
        requestScrollerTopPaddingUpdate(isAddOrRemoveAnimationPending);
    }

    private void resetVerticalPanelPosition() {
        setVerticalPanelTranslation(0.0f);
    }

    private void setClosingWithAlphaFadeout(boolean z) {
        this.mClosingWithAlphaFadeOut = z;
        this.mNotificationStackScroller.forceNoOverlappingRendering(z);
    }

    private void setKeyguardBottomAreaVisibility(int i, boolean z) {
        this.mKeyguardBottomArea.animate().cancel();
        if (z) {
            this.mKeyguardBottomArea.animate().alpha(0.0f).setStartDelay(this.mStatusBar.getKeyguardFadingAwayDelay()).setDuration(this.mStatusBar.getKeyguardFadingAwayDuration() / 2).setInterpolator(Interpolators.ALPHA_OUT).withEndAction(this.mAnimateKeyguardBottomAreaInvisibleEndRunnable).start();
        } else if (i != 1 && i != 2) {
            this.mKeyguardBottomArea.setVisibility(8);
            this.mKeyguardBottomArea.setAlpha(1.0f);
        } else {
            if (!this.mDozing) {
                this.mKeyguardBottomArea.setVisibility(0);
            }
            this.mKeyguardBottomArea.setAlpha(1.0f);
        }
    }

    private void setKeyguardStatusViewVisibility(int i, boolean z, boolean z2) {
        if ((!z && this.mStatusBarState == 1 && i != 1) || z2) {
            this.mKeyguardStatusView.animate().cancel();
            this.mKeyguardStatusViewAnimating = true;
            this.mKeyguardStatusView.animate().alpha(0.0f).setStartDelay(0L).setDuration(160L).setInterpolator(Interpolators.ALPHA_OUT).withEndAction(this.mAnimateKeyguardStatusViewInvisibleEndRunnable);
            if (z) {
                this.mKeyguardStatusView.animate().setStartDelay(this.mStatusBar.getKeyguardFadingAwayDelay()).setDuration(this.mStatusBar.getKeyguardFadingAwayDuration() / 2).start();
            }
        } else if (this.mStatusBarState == 2 && i == 1) {
            this.mKeyguardStatusView.animate().cancel();
            this.mKeyguardStatusView.setVisibility(0);
            this.mKeyguardStatusViewAnimating = true;
            this.mKeyguardStatusView.setAlpha(0.0f);
            this.mKeyguardStatusView.animate().alpha(1.0f).setStartDelay(0L).setDuration(320L).setInterpolator(Interpolators.ALPHA_IN).withEndAction(this.mAnimateKeyguardStatusViewVisibleEndRunnable);
        } else if (i == 1) {
            this.mKeyguardStatusView.animate().cancel();
            this.mKeyguardStatusViewAnimating = false;
            this.mKeyguardStatusView.setVisibility(0);
            this.mKeyguardStatusView.setAlpha(1.0f);
        } else {
            this.mKeyguardStatusView.animate().cancel();
            this.mKeyguardStatusViewAnimating = false;
            this.mKeyguardStatusView.setVisibility(8);
            this.mKeyguardStatusView.setAlpha(1.0f);
        }
    }

    private void setLaunchingAffordance(boolean z) {
        getLeftIcon().setLaunchingAffordance(z);
        getRightIcon().setLaunchingAffordance(z);
        getCenterIcon().setLaunchingAffordance(z);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setListening(boolean z) {
        this.mQsContainer.setListening(z);
        this.mKeyguardStatusBar.setListening(z);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setOverScrolling(boolean z) {
        this.mStackScrollerOverscrolling = z;
        this.mQsContainer.setOverscrolling(z);
    }

    private void setQsExpanded(boolean z) {
        if (this.mQsExpanded != z) {
            this.mQsExpanded = z;
            updateQsState();
            requestPanelHeightUpdate();
            this.mFalsingManager.setQsExpanded(z);
            this.mStatusBar.setQsExpanded(z);
            this.mNotificationContainerParent.setQsExpanded(z);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setQsExpansion(float f) {
        float min = Math.min(Math.max(f, this.mQsMinExpansionHeight), this.mQsMaxExpansionHeight);
        this.mQsFullyExpanded = min == ((float) this.mQsMaxExpansionHeight) && this.mQsMaxExpansionHeight != 0;
        if (min > this.mQsMinExpansionHeight && !this.mQsExpanded && !this.mStackScrollerOverscrolling) {
            setQsExpanded(true);
        } else if (min <= this.mQsMinExpansionHeight && this.mQsExpanded) {
            setQsExpanded(false);
            if (this.mLastAnnouncementWasQuickSettings && !this.mTracking && !isCollapsing()) {
                announceForAccessibility(getKeyguardOrLockScreenString());
                this.mLastAnnouncementWasQuickSettings = false;
            }
        }
        this.mQsExpansionHeight = min;
        updateQsExpansion();
        requestScrollerTopPaddingUpdate(false);
        if (this.mKeyguardShowing) {
            updateHeaderKeyguardAlpha();
        }
        if (this.mStatusBarState == 2 || this.mStatusBarState == 1) {
            updateKeyguardBottomAreaAlpha();
        }
        if (this.mStatusBarState == 0 && this.mQsExpanded && !this.mStackScrollerOverscrolling && this.mQsScrimEnabled) {
            this.mQsNavbarScrim.setAlpha(getQsExpansionFraction());
        }
        if (min != 0.0f && this.mQsFullyExpanded && !this.mLastAnnouncementWasQuickSettings) {
            announceForAccessibility(getContext().getString(2131493444));
            this.mLastAnnouncementWasQuickSettings = true;
        }
        if (this.mQsFullyExpanded && this.mFalsingManager.shouldEnforceBouncer()) {
            this.mStatusBar.executeRunnableDismissingKeyguard(null, null, false, true, false);
        }
    }

    private boolean shouldQuickSettingsIntercept(float f, float f2, float f3) {
        boolean z;
        if (!this.mQsExpansionEnabled || this.mCollapsedOnDown) {
            return false;
        }
        RelativeLayout header = this.mKeyguardShowing ? this.mKeyguardStatusBar : this.mQsContainer.getHeader();
        boolean z2 = (f < this.mQsAutoReinflateContainer.getX() || f > this.mQsAutoReinflateContainer.getX() + ((float) this.mQsAutoReinflateContainer.getWidth()) || f2 < ((float) header.getTop())) ? false : f2 <= ((float) header.getBottom());
        if (this.mQsExpanded) {
            if (z2) {
                z = true;
            } else {
                z = false;
                if (f3 < 0.0f) {
                    z = isInQsArea(f, f2);
                }
            }
            return z;
        }
        return z2;
    }

    private void startClockAnimation(int i) {
        if (this.mClockAnimationTarget == i) {
            return;
        }
        this.mClockAnimationTarget = i;
        getViewTreeObserver().addOnPreDrawListener(new AnonymousClass11(this));
    }

    private void startHighlightIconAnimation(KeyguardAffordanceView keyguardAffordanceView) {
        keyguardAffordanceView.setImageAlpha(1.0f, true, 200L, Interpolators.FAST_OUT_SLOW_IN, new Runnable(this, keyguardAffordanceView) { // from class: com.android.systemui.statusbar.phone.NotificationPanelView.20
            final NotificationPanelView this$0;
            final KeyguardAffordanceView val$icon;

            {
                this.this$0 = this;
                this.val$icon = keyguardAffordanceView;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.val$icon.setImageAlpha(this.val$icon.getRestingAlpha(), true, 200L, Interpolators.FAST_OUT_SLOW_IN, null);
            }
        });
    }

    private void startQsSizeChangeAnimation(int i, int i2) {
        if (this.mQsSizeChangeAnimator != null) {
            i = ((Integer) this.mQsSizeChangeAnimator.getAnimatedValue()).intValue();
            this.mQsSizeChangeAnimator.cancel();
        }
        this.mQsSizeChangeAnimator = ValueAnimator.ofInt(i, i2);
        this.mQsSizeChangeAnimator.setDuration(300L);
        this.mQsSizeChangeAnimator.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
        this.mQsSizeChangeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(this) { // from class: com.android.systemui.statusbar.phone.NotificationPanelView.9
            final NotificationPanelView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                this.this$0.requestScrollerTopPaddingUpdate(false);
                this.this$0.requestPanelHeightUpdate();
                this.this$0.mQsContainer.setHeightOverride(((Integer) this.this$0.mQsSizeChangeAnimator.getAnimatedValue()).intValue());
            }
        });
        this.mQsSizeChangeAnimator.addListener(new AnimatorListenerAdapter(this) { // from class: com.android.systemui.statusbar.phone.NotificationPanelView.10
            final NotificationPanelView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                this.this$0.mQsSizeChangeAnimator = null;
            }
        });
        this.mQsSizeChangeAnimator.start();
    }

    private void trackMovement(MotionEvent motionEvent) {
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.addMovement(motionEvent);
        }
        this.mLastTouchX = motionEvent.getX();
        this.mLastTouchY = motionEvent.getY();
    }

    private void updateClock(float f, float f2) {
        if (!this.mKeyguardStatusViewAnimating) {
            this.mKeyguardStatusView.setAlpha(f);
        }
        this.mKeyguardStatusView.setScaleX(f2);
        this.mKeyguardStatusView.setScaleY(f2);
    }

    private void updateDozingVisibilities(boolean z) {
        if (this.mDozing) {
            this.mKeyguardStatusBar.setVisibility(4);
            this.mKeyguardBottomArea.setVisibility(4);
            return;
        }
        this.mKeyguardBottomArea.setVisibility(0);
        this.mKeyguardStatusBar.setVisibility(0);
        if (z) {
            animateKeyguardStatusBarIn(700L);
            this.mKeyguardBottomArea.startFinishDozeAnimation();
        }
    }

    private void updateEmptyShadeView() {
        NotificationStackScrollLayout notificationStackScrollLayout = this.mNotificationStackScroller;
        boolean z = false;
        if (this.mShadeEmpty) {
            z = !this.mQsExpanded;
        }
        notificationStackScrollLayout.updateEmptyShadeView(z);
    }

    private void updateHeader() {
        if (this.mStatusBar.getBarState() == 1) {
            updateHeaderKeyguardAlpha();
        }
        updateQsExpansion();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateHeaderKeyguardAlpha() {
        this.mKeyguardStatusBar.setAlpha(Math.min(getKeyguardContentsAlpha(), 1.0f - Math.min(1.0f, getQsExpansionFraction() * 2.0f)) * this.mKeyguardStatusBarAnimateAlpha);
        this.mKeyguardStatusBar.setVisibility((this.mKeyguardStatusBar.getAlpha() == 0.0f || this.mDozing) ? 4 : 0);
    }

    private void updateKeyguardBottomAreaAlpha() {
        float min = Math.min(getKeyguardContentsAlpha(), 1.0f - getQsExpansionFraction());
        this.mKeyguardBottomArea.setAlpha(min);
        this.mKeyguardBottomArea.setImportantForAccessibility(min == 0.0f ? 4 : 0);
    }

    private void updateMaxHeadsUpTranslation() {
        this.mNotificationStackScroller.setHeadsUpBoundaries(getHeight(), this.mNavigationBarBottomHeight);
    }

    private void updateNotificationTranslucency() {
        float f = 1.0f;
        if (this.mClosingWithAlphaFadeOut) {
            if (this.mExpandingFromHeadsUp) {
                f = 1.0f;
            } else {
                f = 1.0f;
                if (!this.mHeadsUpManager.hasPinnedHeadsUp()) {
                    f = getFadeoutAlpha();
                }
            }
        }
        this.mNotificationStackScroller.setAlpha(f);
    }

    private void updatePanelExpanded() {
        boolean z = !isFullyCollapsed();
        if (this.mPanelExpanded != z) {
            this.mHeadsUpManager.setIsExpanded(z);
            this.mStatusBar.setPanelExpanded(z);
            this.mPanelExpanded = z;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateQsState() {
        int i = 0;
        this.mQsContainer.setExpanded(this.mQsExpanded);
        this.mNotificationStackScroller.setScrollingEnabled(this.mStatusBarState != 1 ? this.mQsExpanded ? this.mQsExpansionFromOverscroll : true : false);
        updateEmptyShadeView();
        View view = this.mQsNavbarScrim;
        if (this.mStatusBarState != 0 || !this.mQsExpanded || this.mStackScrollerOverscrolling || !this.mQsScrimEnabled) {
            i = 4;
        }
        view.setVisibility(i);
        if (this.mKeyguardUserSwitcher == null || !this.mQsExpanded || this.mStackScrollerOverscrolling) {
            return;
        }
        this.mKeyguardUserSwitcher.hideIfNotSimple(true);
    }

    private void updateUnlockIcon() {
        if (this.mStatusBar.getBarState() == 1 || this.mStatusBar.getBarState() == 2) {
            boolean z = ((float) getMaxPanelHeight()) - getExpandedHeight() > ((float) this.mUnlockMoveDistance);
            LockIcon lockIcon = this.mKeyguardBottomArea.getLockIcon();
            if (z && !this.mUnlockIconActive && this.mTracking) {
                lockIcon.setImageAlpha(1.0f, true, 150L, Interpolators.FAST_OUT_LINEAR_IN, null);
                lockIcon.setImageScale(1.2f, true, 150L, Interpolators.FAST_OUT_LINEAR_IN);
            } else if (!z && this.mUnlockIconActive && this.mTracking) {
                lockIcon.setImageAlpha(lockIcon.getRestingAlpha(), true, 150L, Interpolators.FAST_OUT_LINEAR_IN, null);
                lockIcon.setImageScale(1.0f, true, 150L, Interpolators.FAST_OUT_LINEAR_IN);
            }
            this.mUnlockIconActive = z;
        }
    }

    public void animateCloseQs() {
        if (this.mQsExpansionAnimator != null) {
            if (!this.mQsAnimatorExpand) {
                return;
            }
            float f = this.mQsExpansionHeight;
            this.mQsExpansionAnimator.cancel();
            setQsExpansion(f);
        }
        flingSettings(0.0f, false);
    }

    public void animateToFullShade(long j) {
        this.mAnimateNextTopPaddingChange = true;
        this.mNotificationStackScroller.goToFullShade(j);
        requestLayout();
    }

    public boolean canCameraGestureBeLaunched(boolean z) {
        if (!this.mStatusBar.isCameraAllowedByAdmin()) {
            EventLog.writeEvent(1397638484, "63787722", -1, "");
            return false;
        }
        ResolveInfo resolveCameraIntent = this.mKeyguardBottomArea.resolveCameraIntent();
        String str = null;
        if (resolveCameraIntent != null) {
            str = resolveCameraIntent.activityInfo == null ? null : resolveCameraIntent.activityInfo.packageName;
        }
        boolean z2 = false;
        if (str != null) {
            if (z || !isForegroundApp(str)) {
                z2 = false;
                if (!this.mAfforanceHelper.isSwipingInProgress()) {
                    z2 = true;
                }
            } else {
                z2 = false;
            }
        }
        return z2;
    }

    public void clearNotificationEffects() {
        this.mStatusBar.clearNotificationEffects();
    }

    public void closeQs() {
        cancelQsAnimation();
        setQsExpansion(this.mQsMinExpansionHeight);
    }

    public void closeQsDetail() {
        this.mQsContainer.getQsPanel().closeDetail();
    }

    public int computeMaxKeyguardNotifications(int i) {
        float f;
        int i2;
        float minStackScrollerPadding = this.mClockPositionAlgorithm.getMinStackScrollerPadding(getHeight(), this.mKeyguardStatusView.getHeight());
        int max = Math.max(1, getResources().getDimensionPixelSize(2131689876));
        float height = ((this.mNotificationStackScroller.getHeight() - minStackScrollerPadding) - getResources().getDimensionPixelSize(2131689790)) - this.mNotificationStackScroller.getKeyguardBottomStackSize();
        int i3 = 0;
        int i4 = 0;
        while (i4 < this.mNotificationStackScroller.getChildCount()) {
            ExpandableView expandableView = (ExpandableView) this.mNotificationStackScroller.getChildAt(i4);
            if (expandableView instanceof ExpandableNotificationRow) {
                ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) expandableView;
                f = height;
                i2 = i3;
                if (this.mGroupManager.isSummaryOfSuppressedGroup(expandableNotificationRow.getStatusBarNotification())) {
                    continue;
                } else {
                    f = height;
                    i2 = i3;
                    if (this.mStatusBar.shouldShowOnKeyguard(expandableNotificationRow.getStatusBarNotification())) {
                        f = height;
                        i2 = i3;
                        if (expandableNotificationRow.isRemoved()) {
                            continue;
                        } else {
                            f = height - (expandableView.getMinHeight() + max);
                            if (f < 0.0f || i3 >= i) {
                                return i3;
                            }
                            i2 = i3 + 1;
                        }
                    } else {
                        continue;
                    }
                }
            } else {
                i2 = i3;
                f = height;
            }
            i4++;
            height = f;
            i3 = i2;
        }
        return i3;
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
    }

    public boolean dispatchPopulateAccessibilityEventInternal(AccessibilityEvent accessibilityEvent) {
        if (accessibilityEvent.getEventType() == 32) {
            accessibilityEvent.getText().add(getKeyguardOrLockScreenString());
            this.mLastAnnouncementWasQuickSettings = false;
            return true;
        }
        return super.dispatchPopulateAccessibilityEventInternal(accessibilityEvent);
    }

    @Override // com.android.systemui.statusbar.phone.PanelView
    public void expand(boolean z) {
        super.expand(z);
        setListening(true);
    }

    public void expandWithQs() {
        if (this.mQsExpansionEnabled) {
            this.mQsExpandImmediate = true;
        }
        expand(true);
    }

    @Override // com.android.systemui.statusbar.phone.PanelView
    public void fling(float f, boolean z) {
        GestureRecorder gestureRecorder = ((PhoneStatusBarView) this.mBar).mBar.getGestureRecorder();
        if (gestureRecorder != null) {
            gestureRecorder.tag("fling " + (f > 0.0f ? "open" : "closed"), "notifications,v=" + f);
        }
        super.fling(f, z);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelView
    public boolean flingExpands(float f, float f2, float f3, float f4) {
        boolean flingExpands = super.flingExpands(f, f2, f3, f4);
        if (this.mQsExpansionAnimator != null) {
            flingExpands = true;
        }
        return flingExpands;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelView
    public void flingToHeight(float f, boolean z, float f2, float f3, boolean z2) {
        this.mHeadsUpTouchHelper.notifyFling(!z);
        boolean z3 = false;
        if (!z) {
            z3 = false;
            if (getFadeoutAlpha() == 1.0f) {
                z3 = true;
            }
        }
        setClosingWithAlphaFadeout(z3);
        super.flingToHeight(f, z, f2, f3, z2);
    }

    @Override // com.android.systemui.statusbar.stack.NotificationStackScrollLayout.OnOverscrollTopChangedListener
    public void flingTopOverscroll(float f, boolean z) {
        this.mLastOverscroll = 0.0f;
        this.mQsExpansionFromOverscroll = false;
        setQsExpansion(this.mQsExpansionHeight);
        float f2 = f;
        if (!this.mQsExpansionEnabled) {
            f2 = f;
            if (z) {
                f2 = 0.0f;
            }
        }
        flingSettings(f2, z ? this.mQsExpansionEnabled : false, new Runnable(this) { // from class: com.android.systemui.statusbar.phone.NotificationPanelView.12
            final NotificationPanelView this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.mStackScrollerOverscrolling = false;
                this.this$0.setOverScrolling(false);
                this.this$0.updateQsState();
            }
        }, false);
    }

    @Override // com.android.systemui.statusbar.phone.PanelView
    protected boolean fullyExpandedClearAllVisible() {
        boolean z = false;
        if (this.mNotificationStackScroller.isDismissViewNotGone()) {
            z = false;
            if (this.mNotificationStackScroller.isScrolledToBottom()) {
                z = !this.mQsExpandImmediate;
            }
        }
        return z;
    }

    @Override // com.android.systemui.statusbar.phone.KeyguardAffordanceHelper.Callback
    public float getAffordanceFalsingFactor() {
        return this.mStatusBar.isWakeUpComingFromTouch() ? 1.5f : 1.0f;
    }

    @Override // com.android.systemui.statusbar.phone.PanelView
    protected float getCannedFlingDurationFactor() {
        return this.mQsExpanded ? 0.7f : 0.6f;
    }

    @Override // com.android.systemui.statusbar.phone.KeyguardAffordanceHelper.Callback
    public KeyguardAffordanceView getCenterIcon() {
        return this.mKeyguardBottomArea.getLockIcon();
    }

    @Override // com.android.systemui.statusbar.phone.PanelView
    protected int getClearAllHeight() {
        return this.mNotificationStackScroller.getDismissViewHeight();
    }

    protected float getHeaderTranslation() {
        if (this.mStatusBar.getBarState() == 1) {
            return 0.0f;
        }
        if (this.mNotificationStackScroller.getNotGoneChildCount() == 0) {
            return Math.min(0.0f, (this.mExpandedHeight / 2.05f) - this.mQsMinExpansionHeight);
        }
        float stackTranslation = this.mNotificationStackScroller.getStackTranslation();
        float f = stackTranslation / 2.05f;
        if (this.mHeadsUpManager.hasPinnedHeadsUp() || this.mIsExpansionFromHeadsUp) {
            f = (this.mNotificationStackScroller.getTopPadding() + stackTranslation) - this.mQsMinExpansionHeight;
        }
        return Math.min(0.0f, f);
    }

    @Override // com.android.systemui.statusbar.phone.KeyguardAffordanceHelper.Callback
    public KeyguardAffordanceView getLeftIcon() {
        return getLayoutDirection() == 1 ? this.mKeyguardBottomArea.getRightView() : this.mKeyguardBottomArea.getLeftView();
    }

    @Override // com.android.systemui.statusbar.phone.KeyguardAffordanceHelper.Callback
    public View getLeftPreview() {
        return getLayoutDirection() == 1 ? this.mKeyguardBottomArea.getRightPreview() : this.mKeyguardBottomArea.getLeftPreview();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelView
    public int getMaxPanelHeight() {
        int i = this.mStatusBarMinHeight;
        int i2 = i;
        if (this.mStatusBar.getBarState() != 1) {
            i2 = i;
            if (this.mNotificationStackScroller.getNotGoneChildCount() == 0) {
                i2 = Math.max(i, (int) ((this.mQsMinExpansionHeight + getOverExpansionAmount()) * 2.05f));
            }
        }
        return Math.max((this.mQsExpandImmediate || this.mQsExpanded || (this.mIsExpanding && this.mQsExpandedWhenExpandingStarted)) ? calculatePanelHeightQsExpanded() : calculatePanelHeightShade(), i2);
    }

    @Override // com.android.systemui.statusbar.phone.KeyguardAffordanceHelper.Callback
    public float getMaxTranslationDistance() {
        return (float) Math.hypot(getWidth(), getHeight());
    }

    @Override // com.android.systemui.statusbar.phone.PanelView
    protected float getOverExpansionAmount() {
        return this.mNotificationStackScroller.getCurrentOverScrollAmount(true);
    }

    @Override // com.android.systemui.statusbar.phone.PanelView
    protected float getOverExpansionPixels() {
        return this.mNotificationStackScroller.getCurrentOverScrolledPixels(true);
    }

    @Override // com.android.systemui.statusbar.phone.PanelView
    protected float getPeekHeight() {
        return this.mNotificationStackScroller.getNotGoneChildCount() > 0 ? this.mNotificationStackScroller.getPeekHeight() : this.mQsMinExpansionHeight * 2.05f;
    }

    @Override // com.android.systemui.statusbar.phone.KeyguardAffordanceHelper.Callback
    public KeyguardAffordanceView getRightIcon() {
        return getLayoutDirection() == 1 ? this.mKeyguardBottomArea.getLeftView() : this.mKeyguardBottomArea.getRightView();
    }

    @Override // com.android.systemui.statusbar.phone.KeyguardAffordanceHelper.Callback
    public View getRightPreview() {
        return getLayoutDirection() == 1 ? this.mKeyguardBottomArea.getLeftPreview() : this.mKeyguardBottomArea.getRightPreview();
    }

    @Override // com.android.systemui.statusbar.phone.PanelView
    protected boolean hasConflictingGestures() {
        boolean z = false;
        if (this.mStatusBar.getBarState() != 0) {
            z = true;
        }
        return z;
    }

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return !this.mDozing;
    }

    @Override // com.android.systemui.statusbar.phone.PanelView
    protected boolean isClearAllVisible() {
        return this.mNotificationStackScroller.isDismissViewVisible();
    }

    public boolean isDozing() {
        return this.mDozing;
    }

    @Override // com.android.systemui.statusbar.phone.PanelView
    protected boolean isInContentBounds(float f, float f2) {
        float x = this.mNotificationStackScroller.getX();
        boolean z = false;
        if (!this.mNotificationStackScroller.isBelowLastNotification(f - x, f2)) {
            z = false;
            if (x < f) {
                z = false;
                if (f < this.mNotificationStackScroller.getWidth() + x) {
                    z = true;
                }
            }
        }
        return z;
    }

    public boolean isLaunchTransitionFinished() {
        return this.mIsLaunchTransitionFinished;
    }

    public boolean isLaunchTransitionRunning() {
        return this.mIsLaunchTransitionRunning;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelView
    public boolean isPanelVisibleBecauseOfHeadsUp() {
        return !this.mHeadsUpManager.hasPinnedHeadsUp() ? this.mHeadsUpAnimatingAway : true;
    }

    public boolean isQsDetailShowing() {
        return this.mQsContainer.isShowingDetail();
    }

    public boolean isQsExpanded() {
        return this.mQsExpanded;
    }

    @Override // com.android.systemui.statusbar.phone.PanelView
    protected boolean isScrolledToBottom() {
        boolean z = true;
        if (isInSettings()) {
            return true;
        }
        if (this.mStatusBar.getBarState() != 1) {
            z = this.mNotificationStackScroller.isScrolledToBottom();
        }
        return z;
    }

    @Override // com.android.systemui.statusbar.phone.PanelView
    protected boolean isTrackingBlocked() {
        return this.mConflictingQsExpansionGesture ? this.mQsExpanded : false;
    }

    public void launchCamera(boolean z, int i) {
        boolean z2 = true;
        if (i == 1) {
            this.mLastCameraLaunchSource = "power_double_tap";
        } else if (i == 0) {
            this.mLastCameraLaunchSource = "wiggle_gesture";
        } else {
            this.mLastCameraLaunchSource = "lockscreen_affordance";
        }
        if (isFullyCollapsed()) {
            z = false;
        } else {
            this.mLaunchingAffordance = true;
            setLaunchingAffordance(true);
        }
        KeyguardAffordanceHelper keyguardAffordanceHelper = this.mAfforanceHelper;
        if (getLayoutDirection() != 1) {
            z2 = false;
        }
        keyguardAffordanceHelper.launchAffordance(z, z2);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelView
    public void loadDimens() {
        super.loadDimens();
        this.mFlingAnimationUtils = new FlingAnimationUtils(getContext(), 0.4f);
        this.mStatusBarMinHeight = getResources().getDimensionPixelSize(17104919);
        this.mQsPeekHeight = getResources().getDimensionPixelSize(2131689864);
        this.mNotificationsHeaderCollideDistance = getResources().getDimensionPixelSize(2131689892);
        this.mUnlockMoveDistance = getResources().getDimensionPixelOffset(2131689893);
        this.mClockPositionAlgorithm.loadDimens(getResources());
        this.mNotificationScrimWaitDistance = getResources().getDimensionPixelSize(2131689894);
        this.mQsFalsingThreshold = getResources().getDimensionPixelSize(2131689882);
        this.mPositionMinSideMargin = getResources().getDimensionPixelSize(2131689967);
    }

    @Override // com.android.systemui.statusbar.phone.KeyguardAffordanceHelper.Callback
    public boolean needsAntiFalsing() {
        boolean z = true;
        if (this.mStatusBarState != 1) {
            z = false;
        }
        return z;
    }

    public void onAffordanceLaunchEnded() {
        this.mLaunchingAffordance = false;
        setLaunchingAffordance(false);
    }

    @Override // com.android.systemui.statusbar.phone.KeyguardAffordanceHelper.Callback
    public void onAnimationToSideEnded() {
        this.mIsLaunchTransitionRunning = false;
        this.mIsLaunchTransitionFinished = true;
        if (this.mLaunchAnimationEndRunnable != null) {
            this.mLaunchAnimationEndRunnable.run();
            this.mLaunchAnimationEndRunnable = null;
        }
    }

    @Override // com.android.systemui.statusbar.phone.KeyguardAffordanceHelper.Callback
    public void onAnimationToSideStarted(boolean z, float f, float f2) {
        if (getLayoutDirection() != 1) {
            z = !z;
        }
        this.mIsLaunchTransitionRunning = true;
        this.mLaunchAnimationEndRunnable = null;
        float displayDensity = this.mStatusBar.getDisplayDensity();
        int abs = Math.abs((int) (f / displayDensity));
        int abs2 = Math.abs((int) (f2 / displayDensity));
        if (z) {
            EventLogTags.writeSysuiLockscreenGesture(5, abs, abs2);
            this.mFalsingManager.onLeftAffordanceOn();
            if (this.mFalsingManager.shouldEnforceBouncer()) {
                this.mStatusBar.executeRunnableDismissingKeyguard(new Runnable(this) { // from class: com.android.systemui.statusbar.phone.NotificationPanelView.18
                    final NotificationPanelView this$0;

                    {
                        this.this$0 = this;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        this.this$0.mKeyguardBottomArea.launchLeftAffordance();
                    }
                }, null, true, false, true);
            } else {
                this.mKeyguardBottomArea.launchLeftAffordance();
            }
        } else {
            if ("lockscreen_affordance".equals(this.mLastCameraLaunchSource)) {
                EventLogTags.writeSysuiLockscreenGesture(4, abs, abs2);
            }
            this.mFalsingManager.onCameraOn();
            if (this.mFalsingManager.shouldEnforceBouncer()) {
                this.mStatusBar.executeRunnableDismissingKeyguard(new Runnable(this) { // from class: com.android.systemui.statusbar.phone.NotificationPanelView.19
                    final NotificationPanelView this$0;

                    {
                        this.this$0 = this;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        this.this$0.mKeyguardBottomArea.launchCamera(this.this$0.mLastCameraLaunchSource);
                    }
                }, null, true, false, true);
            } else {
                this.mKeyguardBottomArea.launchCamera(this.mLastCameraLaunchSource);
            }
        }
        this.mStatusBar.startLaunchTransitionTimeout();
        this.mBlockTouches = true;
    }

    @Override // android.view.View
    public WindowInsets onApplyWindowInsets(WindowInsets windowInsets) {
        this.mNavigationBarBottomHeight = windowInsets.getStableInsetBottom();
        updateMaxHeadsUpTranslation();
        return windowInsets;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        if (view.getId() == 2131886599) {
            onQsExpansionStarted();
            if (this.mQsExpanded) {
                flingSettings(0.0f, false, null, true);
            } else if (this.mQsExpansionEnabled) {
                EventLogTags.writeSysuiLockscreenGesture(10, 0, 0);
                flingSettings(0.0f, true, null, true);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelView
    public void onClosingFinished() {
        super.onClosingFinished();
        resetVerticalPanelPosition();
        setClosingWithAlphaFadeout(false);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelView, android.view.View
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        this.mAfforanceHelper.onConfigurationChanged();
        if (configuration.orientation != this.mLastOrientation) {
            resetVerticalPanelPosition();
        }
        this.mLastOrientation = configuration.orientation;
    }

    @Override // com.android.systemui.statusbar.stack.NotificationStackScrollLayout.OnEmptySpaceClickListener
    public void onEmptySpaceClicked(float f, float f2) {
        onEmptySpaceClick(f);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelView
    public void onExpandingFinished() {
        super.onExpandingFinished();
        this.mNotificationStackScroller.onExpansionStopped();
        this.mHeadsUpManager.onExpandingFinished();
        this.mIsExpanding = false;
        if (isFullyCollapsed()) {
            DejankUtils.postAfterTraversal(new Runnable(this) { // from class: com.android.systemui.statusbar.phone.NotificationPanelView.16
                final NotificationPanelView this$0;

                {
                    this.this$0 = this;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$0.setListening(false);
                }
            });
            postOnAnimation(new Runnable(this) { // from class: com.android.systemui.statusbar.phone.NotificationPanelView.17
                final NotificationPanelView this$0;

                {
                    this.this$0 = this;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$0.getParent().invalidateChild(this.this$0, NotificationPanelView.mDummyDirtyRect);
                }
            });
        } else {
            setListening(true);
        }
        this.mQsExpandImmediate = false;
        this.mTwoFingerQsExpandPossible = false;
        this.mIsExpansionFromHeadsUp = false;
        this.mNotificationStackScroller.setTrackingHeadsUp(false);
        this.mExpandingFromHeadsUp = false;
        setPanelScrimMinFraction(0.0f);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelView
    public void onExpandingStarted() {
        super.onExpandingStarted();
        this.mNotificationStackScroller.onExpansionStarted();
        this.mIsExpanding = true;
        this.mQsExpandedWhenExpandingStarted = this.mQsFullyExpanded;
        if (this.mQsExpanded) {
            onQsExpansionStarted();
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelView, android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mKeyguardStatusBar = (KeyguardStatusBarView) findViewById(2131886348);
        this.mKeyguardStatusView = (KeyguardStatusView) findViewById(2131886355);
        this.mClockView = (TextView) findViewById(R$id.clock_view);
        this.mNotificationContainerParent = (NotificationsQuickSettingsContainer) findViewById(2131886682);
        this.mNotificationStackScroller = (NotificationStackScrollLayout) findViewById(2131886684);
        this.mNotificationStackScroller.setOnHeightChangedListener(this);
        this.mNotificationStackScroller.setOverscrollTopChangedListener(this);
        this.mNotificationStackScroller.setOnEmptySpaceClickListener(this);
        this.mKeyguardBottomArea = (KeyguardBottomAreaView) findViewById(2131886291);
        this.mQsNavbarScrim = findViewById(2131886686);
        this.mAfforanceHelper = new KeyguardAffordanceHelper(this, getContext());
        this.mLastOrientation = getResources().getConfiguration().orientation;
        this.mQsAutoReinflateContainer = (AutoReinflateContainer) findViewById(2131886683);
        this.mQsAutoReinflateContainer.addInflateListener(new AnonymousClass8(this));
    }

    @Override // com.android.systemui.statusbar.policy.HeadsUpManager.OnHeadsUpChangedListener
    public void onHeadsUpPinned(ExpandableNotificationRow expandableNotificationRow) {
        this.mNotificationStackScroller.generateHeadsUpAnimation(expandableNotificationRow, true);
    }

    @Override // com.android.systemui.statusbar.policy.HeadsUpManager.OnHeadsUpChangedListener
    public void onHeadsUpPinnedModeChanged(boolean z) {
        if (z) {
            this.mHeadsUpExistenceChangedRunnable.run();
            updateNotificationTranslucency();
            return;
        }
        this.mHeadsUpAnimatingAway = true;
        this.mNotificationStackScroller.runAfterAnimationFinished(this.mHeadsUpExistenceChangedRunnable);
    }

    @Override // com.android.systemui.statusbar.policy.HeadsUpManager.OnHeadsUpChangedListener
    public void onHeadsUpStateChanged(NotificationData.Entry entry, boolean z) {
        this.mNotificationStackScroller.generateHeadsUpAnimation(entry.row, z);
    }

    @Override // com.android.systemui.statusbar.policy.HeadsUpManager.OnHeadsUpChangedListener
    public void onHeadsUpUnPinned(ExpandableNotificationRow expandableNotificationRow) {
    }

    @Override // com.android.systemui.statusbar.ExpandableView.OnHeightChangedListener
    public void onHeightChanged(ExpandableView expandableView, boolean z) {
        if (expandableView == null && this.mQsExpanded) {
            return;
        }
        requestPanelHeightUpdate();
    }

    @Override // com.android.systemui.statusbar.phone.PanelView
    protected void onHeightUpdated(float f) {
        float calculatePanelHeightQsExpanded;
        if (!this.mQsExpanded || this.mQsExpandImmediate || (this.mIsExpanding && this.mQsExpandedWhenExpandingStarted)) {
            positionClockAndNotifications();
        }
        if (this.mQsExpandImmediate || (this.mQsExpanded && !this.mQsTracking && this.mQsExpansionAnimator == null && !this.mQsExpansionFromOverscroll)) {
            if (this.mKeyguardShowing) {
                calculatePanelHeightQsExpanded = f / getMaxPanelHeight();
            } else {
                float intrinsicPadding = this.mNotificationStackScroller.getIntrinsicPadding() + this.mNotificationStackScroller.getLayoutMinHeight();
                calculatePanelHeightQsExpanded = (f - intrinsicPadding) / (calculatePanelHeightQsExpanded() - intrinsicPadding);
            }
            setQsExpansion(this.mQsMinExpansionHeight + ((getTempQsMaxExpansion() - this.mQsMinExpansionHeight) * calculatePanelHeightQsExpanded));
        }
        updateStackHeight(f);
        updateHeader();
        updateUnlockIcon();
        updateNotificationTranslucency();
        updatePanelExpanded();
        this.mNotificationStackScroller.setShadeExpanded(!isFullyCollapsed());
    }

    @Override // com.android.systemui.statusbar.phone.KeyguardAffordanceHelper.Callback
    public void onIconClicked(boolean z) {
        if (this.mHintAnimationRunning) {
            return;
        }
        this.mHintAnimationRunning = true;
        this.mAfforanceHelper.startHintAnimation(z, new Runnable(this) { // from class: com.android.systemui.statusbar.phone.NotificationPanelView.21
            final NotificationPanelView this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.mHintAnimationRunning = false;
                this.this$0.mStatusBar.onHintFinished();
            }
        });
        boolean z2 = z;
        if (getLayoutDirection() == 1) {
            z2 = !z;
        }
        if (z2) {
            this.mStatusBar.onCameraHintStarted();
        } else if (this.mKeyguardBottomArea.isLeftVoiceAssist()) {
            this.mStatusBar.onVoiceAssistHintStarted();
        } else {
            this.mStatusBar.onPhoneHintStarted();
        }
    }

    @Override // com.android.systemui.statusbar.phone.PanelView, android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        if (BenesseExtension.getDchaState() != 0 || this.mBlockTouches || this.mQsContainer.isCustomizing()) {
            return false;
        }
        initDownStates(motionEvent);
        if (this.mHeadsUpTouchHelper.onInterceptTouchEvent(motionEvent)) {
            this.mIsExpansionFromHeadsUp = true;
            MetricsLogger.count(this.mContext, "panel_open", 1);
            MetricsLogger.count(this.mContext, "panel_open_peek", 1);
            return true;
        } else if (isFullyCollapsed() || !onQsIntercept(motionEvent)) {
            return super.onInterceptTouchEvent(motionEvent);
        } else {
            return true;
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelView, android.widget.FrameLayout, android.view.ViewGroup, android.view.View
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        this.mKeyguardStatusView.setPivotX(getWidth() / 2);
        this.mKeyguardStatusView.setPivotY(this.mClockView.getTextSize() * 0.34521484f);
        int i5 = this.mQsMaxExpansionHeight;
        this.mQsMinExpansionHeight = this.mKeyguardShowing ? 0 : this.mQsContainer.getQsMinExpansionHeight();
        this.mQsMaxExpansionHeight = this.mQsContainer.getDesiredHeight();
        positionClockAndNotifications();
        if (this.mQsExpanded && this.mQsFullyExpanded) {
            this.mQsExpansionHeight = this.mQsMaxExpansionHeight;
            requestScrollerTopPaddingUpdate(false);
            requestPanelHeightUpdate();
            if (this.mQsMaxExpansionHeight != i5) {
                startQsSizeChangeAnimation(i5, this.mQsMaxExpansionHeight);
            }
        } else if (!this.mQsExpanded) {
            setQsExpansion(this.mQsMinExpansionHeight + this.mLastOverscroll);
        }
        updateStackHeight(getExpandedHeight());
        updateHeader();
        if (this.mQsSizeChangeAnimator == null) {
            this.mQsContainer.setHeightOverride(this.mQsContainer.getDesiredHeight());
        }
        updateMaxHeadsUpTranslation();
    }

    @Override // com.android.systemui.statusbar.phone.PanelView
    protected boolean onMiddleClicked() {
        switch (this.mStatusBar.getBarState()) {
            case 0:
                post(this.mPostCollapseRunnable);
                return false;
            case 1:
                if (this.mDozingOnDown) {
                    return true;
                }
                EventLogTags.writeSysuiLockscreenGesture(3, 0, 0);
                startUnlockHintAnimation();
                return true;
            case 2:
                if (this.mQsExpanded) {
                    return true;
                }
                this.mStatusBar.goToKeyguard();
                return true;
            default:
                return true;
        }
    }

    @Override // com.android.systemui.statusbar.stack.NotificationStackScrollLayout.OnOverscrollTopChangedListener
    public void onOverscrollTopChanged(float f, boolean z) {
        cancelQsAnimation();
        if (!this.mQsExpansionEnabled) {
            f = 0.0f;
        }
        if (f < 1.0f) {
            f = 0.0f;
        }
        if (f == 0.0f) {
            z = false;
        }
        setOverScrolling(z);
        boolean z2 = false;
        if (f != 0.0f) {
            z2 = true;
        }
        this.mQsExpansionFromOverscroll = z2;
        this.mLastOverscroll = f;
        updateQsState();
        setQsExpansion(this.mQsMinExpansionHeight + f);
    }

    public void onQsHeightChanged() {
        this.mQsMaxExpansionHeight = this.mQsContainer.getDesiredHeight();
        if (this.mQsExpanded && this.mQsFullyExpanded) {
            this.mQsExpansionHeight = this.mQsMaxExpansionHeight;
            requestScrollerTopPaddingUpdate(false);
            requestPanelHeightUpdate();
        }
    }

    @Override // com.android.systemui.statusbar.ExpandableView.OnHeightChangedListener
    public void onReset(ExpandableView expandableView) {
    }

    @Override // android.view.View
    public void onRtlPropertiesChanged(int i) {
        if (i != this.mOldLayoutDirection) {
            this.mAfforanceHelper.onRtlPropertiesChanged();
            this.mOldLayoutDirection = i;
        }
    }

    public void onScreenTurningOn() {
        this.mKeyguardStatusView.refreshTime();
    }

    @Override // com.android.systemui.statusbar.phone.KeyguardAffordanceHelper.Callback
    public void onSwipingAborted() {
        this.mFalsingManager.onAffordanceSwipingAborted();
        this.mKeyguardBottomArea.unbindCameraPrewarmService(false);
    }

    @Override // com.android.systemui.statusbar.phone.KeyguardAffordanceHelper.Callback
    public void onSwipingStarted(boolean z) {
        this.mFalsingManager.onAffordanceSwipingStarted(z);
        if (getLayoutDirection() == 1) {
            z = !z;
        }
        if (z) {
            this.mKeyguardBottomArea.bindCameraPrewarmService();
        }
        requestDisallowInterceptTouchEvent(true);
        this.mOnlyAffordanceInThisMotion = true;
        this.mQsTracking = false;
    }

    @Override // com.android.systemui.statusbar.phone.PanelView, android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (BenesseExtension.getDchaState() != 0 || this.mBlockTouches || this.mQsContainer.isCustomizing()) {
            return false;
        }
        initDownStates(motionEvent);
        if (this.mListenForHeadsUp && !this.mHeadsUpTouchHelper.isTrackingHeadsUp() && this.mHeadsUpTouchHelper.onInterceptTouchEvent(motionEvent)) {
            this.mIsExpansionFromHeadsUp = true;
            MetricsLogger.count(this.mContext, "panel_open_peek", 1);
        }
        if ((!this.mIsExpanding || this.mHintAnimationRunning) && !this.mQsExpanded && this.mStatusBar.getBarState() != 0) {
            this.mAfforanceHelper.onTouchEvent(motionEvent);
        }
        if (this.mOnlyAffordanceInThisMotion) {
            return true;
        }
        this.mHeadsUpTouchHelper.onTouchEvent(motionEvent);
        if (this.mHeadsUpTouchHelper.isTrackingHeadsUp() || !handleQsTouch(motionEvent)) {
            if (motionEvent.getActionMasked() == 0 && isFullyCollapsed()) {
                MetricsLogger.count(this.mContext, "panel_open", 1);
                updateVerticalPanelPosition(motionEvent.getX());
            }
            super.onTouchEvent(motionEvent);
            return true;
        }
        return true;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelView
    public void onTrackingStarted() {
        this.mFalsingManager.onTrackingStarted();
        super.onTrackingStarted();
        if (this.mQsFullyExpanded) {
            this.mQsExpandImmediate = true;
        }
        if (this.mStatusBar.getBarState() == 1 || this.mStatusBar.getBarState() == 2) {
            this.mAfforanceHelper.animateHideLeftRightIcon();
        }
        this.mNotificationStackScroller.onPanelTrackingStarted();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelView
    public void onTrackingStopped(boolean z) {
        this.mFalsingManager.onTrackingStopped();
        super.onTrackingStopped(z);
        if (z) {
            this.mNotificationStackScroller.setOverScrolledPixels(0.0f, true, true);
        }
        this.mNotificationStackScroller.onPanelTrackingStopped();
        if (z && ((this.mStatusBar.getBarState() == 1 || this.mStatusBar.getBarState() == 2) && !this.mHintAnimationRunning)) {
            this.mAfforanceHelper.reset(true);
        }
        if (z) {
            return;
        }
        if (this.mStatusBar.getBarState() == 1 || this.mStatusBar.getBarState() == 2) {
            LockIcon lockIcon = this.mKeyguardBottomArea.getLockIcon();
            lockIcon.setImageAlpha(0.0f, true, 100L, Interpolators.FAST_OUT_LINEAR_IN, null);
            lockIcon.setImageScale(2.0f, true, 100L, Interpolators.FAST_OUT_LINEAR_IN);
        }
    }

    protected void requestScrollerTopPaddingUpdate(boolean z) {
        boolean z2;
        NotificationStackScrollLayout notificationStackScrollLayout = this.mNotificationStackScroller;
        float calculateQsTopPadding = calculateQsTopPadding();
        boolean z3 = !this.mAnimateNextTopPaddingChange ? z : true;
        if (this.mKeyguardShowing) {
            z2 = true;
            if (!this.mQsExpandImmediate) {
                z2 = this.mIsExpanding ? this.mQsExpandedWhenExpandingStarted : false;
            }
        } else {
            z2 = false;
        }
        notificationStackScrollLayout.updateTopPadding(calculateQsTopPadding, z3, z2);
        this.mAnimateNextTopPaddingChange = false;
    }

    @Override // com.android.systemui.statusbar.phone.PanelView
    public void resetViews() {
        this.mIsLaunchTransitionFinished = false;
        this.mBlockTouches = false;
        this.mUnlockIconActive = false;
        if (!this.mLaunchingAffordance) {
            this.mAfforanceHelper.reset(false);
            this.mLastCameraLaunchSource = "lockscreen_affordance";
        }
        closeQs();
        this.mStatusBar.dismissPopups();
        this.mNotificationStackScroller.setOverScrollAmount(0.0f, true, false, true);
        this.mNotificationStackScroller.resetScrollPosition();
    }

    @Override // android.view.View
    public void setAlpha(float f) {
        super.setAlpha(f);
        this.mNotificationStackScroller.setParentFadingOut(f != 1.0f);
    }

    public void setBarState(int i, boolean z, boolean z2) {
        int i2 = this.mStatusBarState;
        boolean z3 = i == 1;
        setKeyguardStatusViewVisibility(i, z, z2);
        setKeyguardBottomAreaVisibility(i, z2);
        this.mStatusBarState = i;
        this.mKeyguardShowing = z3;
        this.mQsContainer.setKeyguardShowing(this.mKeyguardShowing);
        if (z2 || (i2 == 1 && i == 2)) {
            animateKeyguardStatusBarOut();
            this.mQsContainer.animateHeaderSlidingIn(this.mStatusBarState == 2 ? 0L : this.mStatusBar.calculateGoingToFullShadeDelay());
        } else if (i2 == 2 && i == 1) {
            animateKeyguardStatusBarIn(360L);
            this.mQsContainer.animateHeaderSlidingOut();
        } else {
            this.mKeyguardStatusBar.setAlpha(1.0f);
            this.mKeyguardStatusBar.setVisibility(z3 ? 0 : 4);
            if (z3 && i2 != this.mStatusBarState) {
                this.mKeyguardBottomArea.updateLeftAffordance();
                this.mAfforanceHelper.updatePreviews();
            }
        }
        if (z3) {
            updateDozingVisibilities(false);
        }
        resetVerticalPanelPosition();
        updateQsState();
    }

    public void setDozing(boolean z, boolean z2) {
        if (z == this.mDozing) {
            return;
        }
        this.mDozing = z;
        if (this.mStatusBarState == 1) {
            updateDozingVisibilities(z2);
        }
    }

    public void setEmptyDragAmount(float f) {
        float f2 = 0.8f;
        if (this.mNotificationStackScroller.getNotGoneChildCount() > 0) {
            f2 = 0.4f;
        } else if (!this.mStatusBar.hasActiveNotifications()) {
            f2 = 0.4f;
        }
        this.mEmptyDragAmount = f * f2;
        positionClockAndNotifications();
    }

    public void setGroupManager(NotificationGroupManager notificationGroupManager) {
        this.mGroupManager = notificationGroupManager;
    }

    @Override // com.android.systemui.statusbar.phone.PanelView
    public void setHeadsUpManager(HeadsUpManager headsUpManager) {
        super.setHeadsUpManager(headsUpManager);
        this.mHeadsUpTouchHelper = new HeadsUpTouchHelper(headsUpManager, this.mNotificationStackScroller, this);
    }

    public void setKeyguardUserSwitcher(KeyguardUserSwitcher keyguardUserSwitcher) {
        this.mKeyguardUserSwitcher = keyguardUserSwitcher;
    }

    public void setLaunchTransitionEndRunnable(Runnable runnable) {
        this.mLaunchAnimationEndRunnable = runnable;
    }

    @Override // com.android.systemui.statusbar.phone.PanelView
    protected void setOverExpansion(float f, boolean z) {
        if (this.mConflictingQsExpansionGesture || this.mQsExpandImmediate || this.mStatusBar.getBarState() == 1) {
            return;
        }
        this.mNotificationStackScroller.setOnHeightChangedListener(null);
        if (z) {
            this.mNotificationStackScroller.setOverScrolledPixels(f, true, false);
        } else {
            this.mNotificationStackScroller.setOverScrollAmount(f, true, false);
        }
        this.mNotificationStackScroller.setOnHeightChangedListener(this);
    }

    public void setPanelScrimMinFraction(float f) {
        this.mBar.panelScrimMinFractionChanged(f);
    }

    public void setQsExpansionEnabled(boolean z) {
        this.mQsExpansionEnabled = z;
        this.mQsContainer.setHeaderClickable(z);
    }

    public void setQsScrimEnabled(boolean z) {
        boolean z2 = this.mQsScrimEnabled != z;
        this.mQsScrimEnabled = z;
        if (z2) {
            updateQsState();
        }
    }

    public void setShadeEmpty(boolean z) {
        this.mShadeEmpty = z;
        updateEmptyShadeView();
    }

    public void setStatusBar(PhoneStatusBar phoneStatusBar) {
        this.mStatusBar = phoneStatusBar;
    }

    public void setTrackingHeadsUp(boolean z) {
        if (z) {
            this.mNotificationStackScroller.setTrackingHeadsUp(true);
            this.mExpandingFromHeadsUp = true;
        }
    }

    protected void setVerticalPanelTranslation(float f) {
        this.mNotificationStackScroller.setTranslationX(f);
        this.mQsAutoReinflateContainer.setTranslationX(f);
    }

    @Override // android.widget.FrameLayout, android.view.ViewGroup
    public boolean shouldDelayChildPressedState() {
        return true;
    }

    @Override // com.android.systemui.statusbar.phone.PanelView
    protected boolean shouldGestureIgnoreXTouchSlop(float f, float f2) {
        return !this.mAfforanceHelper.isOnAffordanceIcon(f, f2);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelView
    public void startUnlockHintAnimation() {
        super.startUnlockHintAnimation();
        startHighlightIconAnimation(getCenterIcon());
    }

    protected void updateQsExpansion() {
        this.mQsContainer.setQsExpansion(getQsExpansionFraction(), getHeaderTranslation());
    }

    public void updateResources() {
        int dimensionPixelSize = getResources().getDimensionPixelSize(2131689816);
        int integer = getResources().getInteger(2131755093);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.mQsAutoReinflateContainer.getLayoutParams();
        if (layoutParams.width != dimensionPixelSize) {
            layoutParams.width = dimensionPixelSize;
            layoutParams.gravity = integer;
            this.mQsAutoReinflateContainer.setLayoutParams(layoutParams);
            this.mQsContainer.post(this.mUpdateHeader);
        }
        FrameLayout.LayoutParams layoutParams2 = (FrameLayout.LayoutParams) this.mNotificationStackScroller.getLayoutParams();
        if (layoutParams2.width != dimensionPixelSize) {
            layoutParams2.width = dimensionPixelSize;
            layoutParams2.gravity = integer;
            this.mNotificationStackScroller.setLayoutParams(layoutParams2);
        }
    }

    protected void updateStackHeight(float f) {
        this.mNotificationStackScroller.setStackHeight(f);
        updateKeyguardBottomAreaAlpha();
    }

    protected void updateVerticalPanelPosition(float f) {
        if (this.mNotificationStackScroller.getWidth() <= 0 || this.mNotificationStackScroller.getWidth() * 1.75f > getWidth()) {
            resetVerticalPanelPosition();
            return;
        }
        float width = this.mPositionMinSideMargin + (this.mNotificationStackScroller.getWidth() / 2);
        float width2 = (getWidth() - this.mPositionMinSideMargin) - (this.mNotificationStackScroller.getWidth() / 2);
        float f2 = f;
        if (Math.abs(f - (getWidth() / 2)) < this.mNotificationStackScroller.getWidth() / 4) {
            f2 = getWidth() / 2;
        }
        setVerticalPanelTranslation(Math.min(width2, Math.max(width, f2)) - (this.mNotificationStackScroller.getLeft() + (this.mNotificationStackScroller.getWidth() / 2)));
    }
}
