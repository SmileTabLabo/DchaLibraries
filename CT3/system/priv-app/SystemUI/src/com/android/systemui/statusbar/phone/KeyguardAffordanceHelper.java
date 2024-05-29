package com.android.systemui.statusbar.phone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import com.android.systemui.Interpolators;
import com.android.systemui.classifier.FalsingManager;
import com.android.systemui.statusbar.FlingAnimationUtils;
import com.android.systemui.statusbar.KeyguardAffordanceView;
/* loaded from: a.zip:com/android/systemui/statusbar/phone/KeyguardAffordanceHelper.class */
public class KeyguardAffordanceHelper {
    private final Callback mCallback;
    private KeyguardAffordanceView mCenterIcon;
    private final Context mContext;
    private FalsingManager mFalsingManager;
    private FlingAnimationUtils mFlingAnimationUtils;
    private int mHintGrowAmount;
    private float mInitialTouchX;
    private float mInitialTouchY;
    private KeyguardAffordanceView mLeftIcon;
    private int mMinBackgroundRadius;
    private int mMinFlingVelocity;
    private int mMinTranslationAmount;
    private boolean mMotionCancelled;
    private KeyguardAffordanceView mRightIcon;
    private Animator mSwipeAnimator;
    private boolean mSwipingInProgress;
    private View mTargetedView;
    private int mTouchSlop;
    private boolean mTouchSlopExeeded;
    private int mTouchTargetSize;
    private float mTranslation;
    private float mTranslationOnDown;
    private VelocityTracker mVelocityTracker;
    private AnimatorListenerAdapter mFlingEndListener = new AnimatorListenerAdapter(this) { // from class: com.android.systemui.statusbar.phone.KeyguardAffordanceHelper.1
        final KeyguardAffordanceHelper this$0;

        {
            this.this$0 = this;
        }

        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animator) {
            this.this$0.mSwipeAnimator = null;
            this.this$0.mSwipingInProgress = false;
            this.this$0.mTargetedView = null;
        }
    };
    private Runnable mAnimationEndRunnable = new Runnable(this) { // from class: com.android.systemui.statusbar.phone.KeyguardAffordanceHelper.2
        final KeyguardAffordanceHelper this$0;

        {
            this.this$0 = this;
        }

        @Override // java.lang.Runnable
        public void run() {
            this.this$0.mCallback.onAnimationToSideEnded();
        }
    };

    /* loaded from: a.zip:com/android/systemui/statusbar/phone/KeyguardAffordanceHelper$Callback.class */
    public interface Callback {
        float getAffordanceFalsingFactor();

        KeyguardAffordanceView getCenterIcon();

        KeyguardAffordanceView getLeftIcon();

        View getLeftPreview();

        float getMaxTranslationDistance();

        KeyguardAffordanceView getRightIcon();

        View getRightPreview();

        boolean needsAntiFalsing();

        void onAnimationToSideEnded();

        void onAnimationToSideStarted(boolean z, float f, float f2);

        void onIconClicked(boolean z);

        void onSwipingAborted();

        void onSwipingStarted(boolean z);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public KeyguardAffordanceHelper(Callback callback, Context context) {
        this.mContext = context;
        this.mCallback = callback;
        initIcons();
        updateIcon(this.mLeftIcon, 0.0f, this.mLeftIcon.getRestingAlpha(), false, false, true, false);
        updateIcon(this.mCenterIcon, 0.0f, this.mCenterIcon.getRestingAlpha(), false, false, true, false);
        updateIcon(this.mRightIcon, 0.0f, this.mRightIcon.getRestingAlpha(), false, false, true, false);
        initDimens();
    }

    private void cancelAnimation() {
        if (this.mSwipeAnimator != null) {
            this.mSwipeAnimator.cancel();
        }
    }

    private void endMotion(boolean z, float f, float f2) {
        if (this.mSwipingInProgress) {
            flingWithCurrentVelocity(z, f, f2);
        } else {
            this.mTargetedView = null;
        }
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.recycle();
            this.mVelocityTracker = null;
        }
    }

    private void fling(float f, boolean z, boolean z2) {
        float maxTranslationDistance = z2 ? -this.mCallback.getMaxTranslationDistance() : this.mCallback.getMaxTranslationDistance();
        if (z) {
            maxTranslationDistance = 0.0f;
        }
        ValueAnimator ofFloat = ValueAnimator.ofFloat(this.mTranslation, maxTranslationDistance);
        this.mFlingAnimationUtils.apply(ofFloat, this.mTranslation, maxTranslationDistance, f);
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(this) { // from class: com.android.systemui.statusbar.phone.KeyguardAffordanceHelper.6
            final KeyguardAffordanceHelper this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                this.this$0.mTranslation = ((Float) valueAnimator.getAnimatedValue()).floatValue();
            }
        });
        ofFloat.addListener(this.mFlingEndListener);
        if (z) {
            reset(true);
        } else {
            startFinishingCircleAnimation(0.375f * f, this.mAnimationEndRunnable, z2);
            this.mCallback.onAnimationToSideStarted(z2, this.mTranslation, f);
        }
        ofFloat.start();
        this.mSwipeAnimator = ofFloat;
        if (z) {
            this.mCallback.onSwipingAborted();
        }
    }

    private void flingWithCurrentVelocity(boolean z, float f, float f2) {
        float currentVelocity = getCurrentVelocity(f, f2);
        boolean z2 = false;
        if (this.mCallback.needsAntiFalsing()) {
            z2 = 0 == 0 ? this.mFalsingManager.isFalseTouch() : true;
        }
        boolean isBelowFalsingThreshold = !z2 ? isBelowFalsingThreshold() : true;
        boolean z3 = this.mTranslation * currentVelocity < 0.0f;
        boolean z4 = isBelowFalsingThreshold | (Math.abs(currentVelocity) > ((float) this.mMinFlingVelocity) ? z3 : false);
        if (z4 ^ z3) {
            currentVelocity = 0.0f;
        }
        if (z4) {
            z = true;
        }
        fling(currentVelocity, z, this.mTranslation < 0.0f);
    }

    private ValueAnimator getAnimatorToRadius(boolean z, int i) {
        KeyguardAffordanceView keyguardAffordanceView = z ? this.mRightIcon : this.mLeftIcon;
        ValueAnimator ofFloat = ValueAnimator.ofFloat(keyguardAffordanceView.getCircleRadius(), i);
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(this, keyguardAffordanceView, z) { // from class: com.android.systemui.statusbar.phone.KeyguardAffordanceHelper.5
            final KeyguardAffordanceHelper this$0;
            final boolean val$right;
            final KeyguardAffordanceView val$targetView;

            {
                this.this$0 = this;
                this.val$targetView = keyguardAffordanceView;
                this.val$right = z;
            }

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                this.val$targetView.setCircleRadiusWithoutAnimation(floatValue);
                float translationFromRadius = this.this$0.getTranslationFromRadius(floatValue);
                KeyguardAffordanceHelper keyguardAffordanceHelper = this.this$0;
                float f = translationFromRadius;
                if (this.val$right) {
                    f = -translationFromRadius;
                }
                keyguardAffordanceHelper.mTranslation = f;
                this.this$0.updateIconsFromTranslation(this.val$targetView);
            }
        });
        return ofFloat;
    }

    private float getCurrentVelocity(float f, float f2) {
        if (this.mVelocityTracker == null) {
            return 0.0f;
        }
        this.mVelocityTracker.computeCurrentVelocity(1000);
        float xVelocity = this.mVelocityTracker.getXVelocity();
        float yVelocity = this.mVelocityTracker.getYVelocity();
        float f3 = f - this.mInitialTouchX;
        float f4 = f2 - this.mInitialTouchY;
        float hypot = ((xVelocity * f3) + (yVelocity * f4)) / ((float) Math.hypot(f3, f4));
        float f5 = hypot;
        if (this.mTargetedView == this.mRightIcon) {
            f5 = -hypot;
        }
        return f5;
    }

    private View getIconAtPosition(float f, float f2) {
        if (leftSwipePossible() && isOnIcon(this.mLeftIcon, f, f2)) {
            return this.mLeftIcon;
        }
        if (rightSwipePossible() && isOnIcon(this.mRightIcon, f, f2)) {
            return this.mRightIcon;
        }
        return null;
    }

    private int getMinTranslationAmount() {
        return (int) (this.mMinTranslationAmount * this.mCallback.getAffordanceFalsingFactor());
    }

    private float getRadiusFromTranslation(float f) {
        if (f <= this.mTouchSlop) {
            return 0.0f;
        }
        return ((f - this.mTouchSlop) * 0.25f) + this.mMinBackgroundRadius;
    }

    private float getScale(float f, KeyguardAffordanceView keyguardAffordanceView) {
        return Math.min(((f / keyguardAffordanceView.getRestingAlpha()) * 0.2f) + 0.8f, 1.5f);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public float getTranslationFromRadius(float f) {
        float f2 = (f - this.mMinBackgroundRadius) / 0.25f;
        float f3 = 0.0f;
        if (f2 > 0.0f) {
            f3 = this.mTouchSlop + f2;
        }
        return f3;
    }

    private void initDimens() {
        ViewConfiguration viewConfiguration = ViewConfiguration.get(this.mContext);
        this.mTouchSlop = viewConfiguration.getScaledPagingTouchSlop();
        this.mMinFlingVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
        this.mMinTranslationAmount = this.mContext.getResources().getDimensionPixelSize(2131689888);
        this.mMinBackgroundRadius = this.mContext.getResources().getDimensionPixelSize(2131689889);
        this.mTouchTargetSize = this.mContext.getResources().getDimensionPixelSize(2131689890);
        this.mHintGrowAmount = this.mContext.getResources().getDimensionPixelSize(2131689891);
        this.mFlingAnimationUtils = new FlingAnimationUtils(this.mContext, 0.4f);
        this.mFalsingManager = FalsingManager.getInstance(this.mContext);
    }

    private void initIcons() {
        this.mLeftIcon = this.mCallback.getLeftIcon();
        this.mCenterIcon = this.mCallback.getCenterIcon();
        this.mRightIcon = this.mCallback.getRightIcon();
        updatePreviews();
    }

    private void initVelocityTracker() {
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.recycle();
        }
        this.mVelocityTracker = VelocityTracker.obtain();
    }

    private boolean isBelowFalsingThreshold() {
        return Math.abs(this.mTranslation) < Math.abs(this.mTranslationOnDown) + ((float) getMinTranslationAmount());
    }

    private boolean isOnIcon(View view, float f, float f2) {
        return Math.hypot((double) (f - (view.getX() + (((float) view.getWidth()) / 2.0f))), (double) (f2 - (view.getY() + (((float) view.getHeight()) / 2.0f)))) <= ((double) (this.mTouchTargetSize / 2));
    }

    private boolean leftSwipePossible() {
        boolean z = false;
        if (this.mLeftIcon.getVisibility() == 0) {
            z = true;
        }
        return z;
    }

    private boolean rightSwipePossible() {
        boolean z = false;
        if (this.mRightIcon.getVisibility() == 0) {
            z = true;
        }
        return z;
    }

    private void setTranslation(float f, boolean z, boolean z2) {
        if (!rightSwipePossible()) {
            f = Math.max(0.0f, f);
        }
        if (!leftSwipePossible()) {
            f = Math.min(0.0f, f);
        }
        float abs = Math.abs(f);
        if (f != this.mTranslation || z) {
            KeyguardAffordanceView keyguardAffordanceView = f > 0.0f ? this.mLeftIcon : this.mRightIcon;
            KeyguardAffordanceView keyguardAffordanceView2 = f > 0.0f ? this.mRightIcon : this.mLeftIcon;
            float minTranslationAmount = abs / getMinTranslationAmount();
            float max = Math.max(1.0f - minTranslationAmount, 0.0f);
            boolean z3 = z ? z2 : false;
            boolean z4 = z && !z2;
            float radiusFromTranslation = getRadiusFromTranslation(abs);
            boolean isBelowFalsingThreshold = z ? isBelowFalsingThreshold() : false;
            if (z) {
                updateIcon(keyguardAffordanceView, 0.0f, max * keyguardAffordanceView.getRestingAlpha(), z3, isBelowFalsingThreshold, false, z4);
            } else {
                updateIcon(keyguardAffordanceView, radiusFromTranslation, minTranslationAmount + (keyguardAffordanceView.getRestingAlpha() * max), false, false, false, false);
            }
            updateIcon(keyguardAffordanceView2, 0.0f, max * keyguardAffordanceView2.getRestingAlpha(), z3, isBelowFalsingThreshold, false, z4);
            updateIcon(this.mCenterIcon, 0.0f, max * this.mCenterIcon.getRestingAlpha(), z3, isBelowFalsingThreshold, false, z4);
            this.mTranslation = f;
        }
    }

    private void startFinishingCircleAnimation(float f, Runnable runnable, boolean z) {
        (z ? this.mRightIcon : this.mLeftIcon).finishAnimation(f, runnable);
    }

    private void startHintAnimationPhase1(boolean z, Runnable runnable) {
        KeyguardAffordanceView keyguardAffordanceView = z ? this.mRightIcon : this.mLeftIcon;
        ValueAnimator animatorToRadius = getAnimatorToRadius(z, this.mHintGrowAmount);
        animatorToRadius.addListener(new AnimatorListenerAdapter(this, runnable, z) { // from class: com.android.systemui.statusbar.phone.KeyguardAffordanceHelper.3
            private boolean mCancelled;
            final KeyguardAffordanceHelper this$0;
            final Runnable val$onFinishedListener;
            final boolean val$right;

            {
                this.this$0 = this;
                this.val$onFinishedListener = runnable;
                this.val$right = z;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
                this.mCancelled = true;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                if (!this.mCancelled) {
                    this.this$0.startUnlockHintAnimationPhase2(this.val$right, this.val$onFinishedListener);
                    return;
                }
                this.this$0.mSwipeAnimator = null;
                this.this$0.mTargetedView = null;
                this.val$onFinishedListener.run();
            }
        });
        animatorToRadius.setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN);
        animatorToRadius.setDuration(200L);
        animatorToRadius.start();
        this.mSwipeAnimator = animatorToRadius;
        this.mTargetedView = keyguardAffordanceView;
    }

    private void startSwiping(View view) {
        this.mCallback.onSwipingStarted(view == this.mRightIcon);
        this.mSwipingInProgress = true;
        this.mTargetedView = view;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startUnlockHintAnimationPhase2(boolean z, Runnable runnable) {
        ValueAnimator animatorToRadius = getAnimatorToRadius(z, 0);
        animatorToRadius.addListener(new AnimatorListenerAdapter(this, runnable) { // from class: com.android.systemui.statusbar.phone.KeyguardAffordanceHelper.4
            final KeyguardAffordanceHelper this$0;
            final Runnable val$onFinishedListener;

            {
                this.this$0 = this;
                this.val$onFinishedListener = runnable;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                this.this$0.mSwipeAnimator = null;
                this.this$0.mTargetedView = null;
                this.val$onFinishedListener.run();
            }
        });
        animatorToRadius.setInterpolator(Interpolators.FAST_OUT_LINEAR_IN);
        animatorToRadius.setDuration(350L);
        animatorToRadius.setStartDelay(500L);
        animatorToRadius.start();
        this.mSwipeAnimator = animatorToRadius;
    }

    private void trackMovement(MotionEvent motionEvent) {
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.addMovement(motionEvent);
        }
    }

    private void updateIcon(KeyguardAffordanceView keyguardAffordanceView, float f, float f2, boolean z, boolean z2, boolean z3, boolean z4) {
        if (keyguardAffordanceView.getVisibility() == 0 || z3) {
            if (z4) {
                keyguardAffordanceView.setCircleRadiusWithoutAnimation(f);
            } else {
                keyguardAffordanceView.setCircleRadius(f, z2);
            }
            updateIconAlpha(keyguardAffordanceView, f2, z);
        }
    }

    private void updateIconAlpha(KeyguardAffordanceView keyguardAffordanceView, float f, boolean z) {
        float scale = getScale(f, keyguardAffordanceView);
        keyguardAffordanceView.setImageAlpha(Math.min(1.0f, f), z);
        keyguardAffordanceView.setImageScale(scale, z);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateIconsFromTranslation(KeyguardAffordanceView keyguardAffordanceView) {
        float abs = Math.abs(this.mTranslation) / getMinTranslationAmount();
        float max = Math.max(0.0f, 1.0f - abs);
        KeyguardAffordanceView keyguardAffordanceView2 = keyguardAffordanceView == this.mRightIcon ? this.mLeftIcon : this.mRightIcon;
        updateIconAlpha(keyguardAffordanceView, (keyguardAffordanceView.getRestingAlpha() * max) + abs, false);
        updateIconAlpha(keyguardAffordanceView2, keyguardAffordanceView2.getRestingAlpha() * max, false);
        updateIconAlpha(this.mCenterIcon, this.mCenterIcon.getRestingAlpha() * max, false);
    }

    public void animateHideLeftRightIcon() {
        cancelAnimation();
        updateIcon(this.mRightIcon, 0.0f, 0.0f, true, false, false, false);
        updateIcon(this.mLeftIcon, 0.0f, 0.0f, true, false, false, false);
    }

    public boolean isOnAffordanceIcon(float f, float f2) {
        return !isOnIcon(this.mLeftIcon, f, f2) ? isOnIcon(this.mRightIcon, f, f2) : true;
    }

    public boolean isSwipingInProgress() {
        return this.mSwipingInProgress;
    }

    public void launchAffordance(boolean z, boolean z2) {
        if (this.mSwipingInProgress) {
            return;
        }
        KeyguardAffordanceView keyguardAffordanceView = z2 ? this.mLeftIcon : this.mRightIcon;
        KeyguardAffordanceView keyguardAffordanceView2 = z2 ? this.mRightIcon : this.mLeftIcon;
        startSwiping(keyguardAffordanceView);
        if (z) {
            fling(0.0f, false, !z2);
            updateIcon(keyguardAffordanceView2, 0.0f, 0.0f, true, false, true, false);
            updateIcon(this.mCenterIcon, 0.0f, 0.0f, true, false, true, false);
            return;
        }
        this.mCallback.onAnimationToSideStarted(!z2, this.mTranslation, 0.0f);
        this.mTranslation = z2 ? this.mCallback.getMaxTranslationDistance() : this.mCallback.getMaxTranslationDistance();
        updateIcon(this.mCenterIcon, 0.0f, 0.0f, false, false, true, false);
        updateIcon(keyguardAffordanceView2, 0.0f, 0.0f, false, false, true, false);
        keyguardAffordanceView.instantFinishAnimation();
        this.mFlingEndListener.onAnimationEnd(null);
        this.mAnimationEndRunnable.run();
    }

    public void onConfigurationChanged() {
        initDimens();
        initIcons();
    }

    public void onRtlPropertiesChanged() {
        initIcons();
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    public boolean onTouchEvent(MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        if (!this.mMotionCancelled || actionMasked == 0) {
            float y = motionEvent.getY();
            float x = motionEvent.getX();
            boolean z = false;
            switch (actionMasked) {
                case 0:
                    View iconAtPosition = getIconAtPosition(x, y);
                    if (iconAtPosition == null || !(this.mTargetedView == null || this.mTargetedView == iconAtPosition)) {
                        this.mMotionCancelled = true;
                        return false;
                    }
                    if (this.mTargetedView != null) {
                        cancelAnimation();
                    } else {
                        this.mTouchSlopExeeded = false;
                    }
                    startSwiping(iconAtPosition);
                    this.mInitialTouchX = x;
                    this.mInitialTouchY = y;
                    this.mTranslationOnDown = this.mTranslation;
                    initVelocityTracker();
                    trackMovement(motionEvent);
                    this.mMotionCancelled = false;
                    return true;
                case 1:
                    z = true;
                    break;
                case 2:
                    trackMovement(motionEvent);
                    float hypot = (float) Math.hypot(x - this.mInitialTouchX, y - this.mInitialTouchY);
                    if (!this.mTouchSlopExeeded && hypot > this.mTouchSlop) {
                        this.mTouchSlopExeeded = true;
                    }
                    if (this.mSwipingInProgress) {
                        setTranslation(this.mTargetedView == this.mRightIcon ? Math.min(0.0f, this.mTranslationOnDown - hypot) : Math.max(0.0f, hypot + this.mTranslationOnDown), false, false);
                        return true;
                    }
                    return true;
                case 3:
                    break;
                case 4:
                default:
                    return true;
                case 5:
                    this.mMotionCancelled = true;
                    endMotion(true, x, y);
                    return true;
            }
            boolean z2 = this.mTargetedView == this.mRightIcon;
            trackMovement(motionEvent);
            endMotion(!z, x, y);
            if (this.mTouchSlopExeeded || !z) {
                return true;
            }
            this.mCallback.onIconClicked(z2);
            return true;
        }
        return false;
    }

    public void reset(boolean z) {
        cancelAnimation();
        setTranslation(0.0f, true, z);
        this.mMotionCancelled = true;
        if (this.mSwipingInProgress) {
            this.mCallback.onSwipingAborted();
            this.mSwipingInProgress = false;
        }
    }

    public void startHintAnimation(boolean z, Runnable runnable) {
        cancelAnimation();
        startHintAnimationPhase1(z, runnable);
    }

    public void updatePreviews() {
        this.mLeftIcon.setPreviewView(this.mCallback.getLeftPreview());
        this.mRightIcon.setPreviewView(this.mCallback.getRightPreview());
    }
}
