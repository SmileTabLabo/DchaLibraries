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
import com.android.systemui.R;
import com.android.systemui.classifier.FalsingManager;
import com.android.systemui.statusbar.FlingAnimationUtils;
import com.android.systemui.statusbar.KeyguardAffordanceView;
/* loaded from: classes.dex */
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
    private AnimatorListenerAdapter mFlingEndListener = new AnimatorListenerAdapter() { // from class: com.android.systemui.statusbar.phone.KeyguardAffordanceHelper.1
        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animator) {
            KeyguardAffordanceHelper.this.mSwipeAnimator = null;
            KeyguardAffordanceHelper.this.mSwipingInProgress = false;
            KeyguardAffordanceHelper.this.mTargetedView = null;
        }
    };
    private Runnable mAnimationEndRunnable = new Runnable() { // from class: com.android.systemui.statusbar.phone.KeyguardAffordanceHelper.2
        @Override // java.lang.Runnable
        public void run() {
            KeyguardAffordanceHelper.this.mCallback.onAnimationToSideEnded();
        }
    };

    /* loaded from: classes.dex */
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

    private void initDimens() {
        ViewConfiguration viewConfiguration = ViewConfiguration.get(this.mContext);
        this.mTouchSlop = viewConfiguration.getScaledPagingTouchSlop();
        this.mMinFlingVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
        this.mMinTranslationAmount = this.mContext.getResources().getDimensionPixelSize(R.dimen.keyguard_min_swipe_amount);
        this.mMinBackgroundRadius = this.mContext.getResources().getDimensionPixelSize(R.dimen.keyguard_affordance_min_background_radius);
        this.mTouchTargetSize = this.mContext.getResources().getDimensionPixelSize(R.dimen.keyguard_affordance_touch_target_size);
        this.mHintGrowAmount = this.mContext.getResources().getDimensionPixelSize(R.dimen.hint_grow_amount_sideways);
        this.mFlingAnimationUtils = new FlingAnimationUtils(this.mContext, 0.4f);
        this.mFalsingManager = FalsingManager.getInstance(this.mContext);
    }

    private void initIcons() {
        this.mLeftIcon = this.mCallback.getLeftIcon();
        this.mCenterIcon = this.mCallback.getCenterIcon();
        this.mRightIcon = this.mCallback.getRightIcon();
        updatePreviews();
    }

    public void updatePreviews() {
        this.mLeftIcon.setPreviewView(this.mCallback.getLeftPreview());
        this.mRightIcon.setPreviewView(this.mCallback.getRightPreview());
    }

    /* JADX WARN: Removed duplicated region for block: B:26:0x0063  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public boolean onTouchEvent(MotionEvent motionEvent) {
        boolean z;
        float max;
        boolean z2;
        int actionMasked = motionEvent.getActionMasked();
        if (!this.mMotionCancelled || actionMasked == 0) {
            float y = motionEvent.getY();
            float x = motionEvent.getX();
            if (actionMasked != 5) {
                switch (actionMasked) {
                    case 0:
                        View iconAtPosition = getIconAtPosition(x, y);
                        if (iconAtPosition == null || (this.mTargetedView != null && this.mTargetedView != iconAtPosition)) {
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
                        break;
                    case 1:
                        z = true;
                        z2 = this.mTargetedView == this.mRightIcon;
                        trackMovement(motionEvent);
                        endMotion(!z, x, y);
                        if (!this.mTouchSlopExeeded && z) {
                            this.mCallback.onIconClicked(z2);
                            break;
                        }
                        break;
                    case 2:
                        trackMovement(motionEvent);
                        float hypot = (float) Math.hypot(x - this.mInitialTouchX, y - this.mInitialTouchY);
                        if (!this.mTouchSlopExeeded && hypot > this.mTouchSlop) {
                            this.mTouchSlopExeeded = true;
                        }
                        if (this.mSwipingInProgress) {
                            if (this.mTargetedView == this.mRightIcon) {
                                max = Math.min(0.0f, this.mTranslationOnDown - hypot);
                            } else {
                                max = Math.max(0.0f, this.mTranslationOnDown + hypot);
                            }
                            setTranslation(max, false, false);
                            break;
                        }
                        break;
                    case 3:
                        z = false;
                        if (this.mTargetedView == this.mRightIcon) {
                        }
                        trackMovement(motionEvent);
                        endMotion(!z, x, y);
                        if (!this.mTouchSlopExeeded) {
                            this.mCallback.onIconClicked(z2);
                            break;
                        }
                        break;
                }
            } else {
                this.mMotionCancelled = true;
                endMotion(true, x, y);
            }
            return true;
        }
        return false;
    }

    private void startSwiping(View view) {
        this.mCallback.onSwipingStarted(view == this.mRightIcon);
        this.mSwipingInProgress = true;
        this.mTargetedView = view;
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

    public boolean isOnAffordanceIcon(float f, float f2) {
        return isOnIcon(this.mLeftIcon, f, f2) || isOnIcon(this.mRightIcon, f, f2);
    }

    private boolean isOnIcon(View view, float f, float f2) {
        return Math.hypot((double) (f - (view.getX() + (((float) view.getWidth()) / 2.0f))), (double) (f2 - (view.getY() + (((float) view.getHeight()) / 2.0f)))) <= ((double) (this.mTouchTargetSize / 2));
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

    private boolean rightSwipePossible() {
        return this.mRightIcon.getVisibility() == 0;
    }

    private boolean leftSwipePossible() {
        return this.mLeftIcon.getVisibility() == 0;
    }

    public void startHintAnimation(boolean z, Runnable runnable) {
        cancelAnimation();
        startHintAnimationPhase1(z, runnable);
    }

    private void startHintAnimationPhase1(final boolean z, final Runnable runnable) {
        KeyguardAffordanceView keyguardAffordanceView = z ? this.mRightIcon : this.mLeftIcon;
        ValueAnimator animatorToRadius = getAnimatorToRadius(z, this.mHintGrowAmount);
        animatorToRadius.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.statusbar.phone.KeyguardAffordanceHelper.3
            private boolean mCancelled;

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
                this.mCancelled = true;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                if (this.mCancelled) {
                    KeyguardAffordanceHelper.this.mSwipeAnimator = null;
                    KeyguardAffordanceHelper.this.mTargetedView = null;
                    runnable.run();
                    return;
                }
                KeyguardAffordanceHelper.this.startUnlockHintAnimationPhase2(z, runnable);
            }
        });
        animatorToRadius.setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN);
        animatorToRadius.setDuration(200L);
        animatorToRadius.start();
        this.mSwipeAnimator = animatorToRadius;
        this.mTargetedView = keyguardAffordanceView;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startUnlockHintAnimationPhase2(boolean z, final Runnable runnable) {
        ValueAnimator animatorToRadius = getAnimatorToRadius(z, 0);
        animatorToRadius.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.statusbar.phone.KeyguardAffordanceHelper.4
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                KeyguardAffordanceHelper.this.mSwipeAnimator = null;
                KeyguardAffordanceHelper.this.mTargetedView = null;
                runnable.run();
            }
        });
        animatorToRadius.setInterpolator(Interpolators.FAST_OUT_LINEAR_IN);
        animatorToRadius.setDuration(350L);
        animatorToRadius.setStartDelay(500L);
        animatorToRadius.start();
        this.mSwipeAnimator = animatorToRadius;
    }

    private ValueAnimator getAnimatorToRadius(final boolean z, int i) {
        final KeyguardAffordanceView keyguardAffordanceView = z ? this.mRightIcon : this.mLeftIcon;
        ValueAnimator ofFloat = ValueAnimator.ofFloat(keyguardAffordanceView.getCircleRadius(), i);
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.statusbar.phone.KeyguardAffordanceHelper.5
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                keyguardAffordanceView.setCircleRadiusWithoutAnimation(floatValue);
                float translationFromRadius = KeyguardAffordanceHelper.this.getTranslationFromRadius(floatValue);
                KeyguardAffordanceHelper keyguardAffordanceHelper = KeyguardAffordanceHelper.this;
                if (z) {
                    translationFromRadius = -translationFromRadius;
                }
                keyguardAffordanceHelper.mTranslation = translationFromRadius;
                KeyguardAffordanceHelper.this.updateIconsFromTranslation(keyguardAffordanceView);
            }
        });
        return ofFloat;
    }

    private void cancelAnimation() {
        if (this.mSwipeAnimator != null) {
            this.mSwipeAnimator.cancel();
        }
    }

    private void flingWithCurrentVelocity(boolean z, float f, float f2) {
        float currentVelocity = getCurrentVelocity(f, f2);
        boolean z2 = (this.mCallback.needsAntiFalsing() && this.mFalsingManager.isFalseTouch()) || isBelowFalsingThreshold();
        boolean z3 = this.mTranslation * currentVelocity < 0.0f;
        boolean z4 = z2 | (Math.abs(currentVelocity) > ((float) this.mMinFlingVelocity) && z3);
        if (z3 ^ z4) {
            currentVelocity = 0.0f;
        }
        fling(currentVelocity, z4 || z, this.mTranslation < 0.0f);
    }

    private boolean isBelowFalsingThreshold() {
        return Math.abs(this.mTranslation) < Math.abs(this.mTranslationOnDown) + ((float) getMinTranslationAmount());
    }

    private int getMinTranslationAmount() {
        return (int) (this.mMinTranslationAmount * this.mCallback.getAffordanceFalsingFactor());
    }

    private void fling(float f, boolean z, boolean z2) {
        float maxTranslationDistance = z2 ? -this.mCallback.getMaxTranslationDistance() : this.mCallback.getMaxTranslationDistance();
        if (z) {
            maxTranslationDistance = 0.0f;
        }
        ValueAnimator ofFloat = ValueAnimator.ofFloat(this.mTranslation, maxTranslationDistance);
        this.mFlingAnimationUtils.apply(ofFloat, this.mTranslation, maxTranslationDistance, f);
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.statusbar.phone.KeyguardAffordanceHelper.6
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                KeyguardAffordanceHelper.this.mTranslation = ((Float) valueAnimator.getAnimatedValue()).floatValue();
            }
        });
        ofFloat.addListener(this.mFlingEndListener);
        if (!z) {
            startFinishingCircleAnimation(0.375f * f, this.mAnimationEndRunnable, z2);
            this.mCallback.onAnimationToSideStarted(z2, this.mTranslation, f);
        } else {
            reset(true);
        }
        ofFloat.start();
        this.mSwipeAnimator = ofFloat;
        if (z) {
            this.mCallback.onSwipingAborted();
        }
    }

    private void startFinishingCircleAnimation(float f, Runnable runnable, boolean z) {
        (z ? this.mRightIcon : this.mLeftIcon).finishAnimation(f, runnable);
    }

    private void setTranslation(float f, boolean z, boolean z2) {
        float max;
        if (!rightSwipePossible()) {
            max = Math.max(0.0f, f);
        } else {
            max = f;
        }
        if (!leftSwipePossible()) {
            max = Math.min(0.0f, max);
        }
        float f2 = max;
        float abs = Math.abs(f2);
        if (f2 != this.mTranslation || z) {
            int i = (f2 > 0.0f ? 1 : (f2 == 0.0f ? 0 : -1));
            KeyguardAffordanceView keyguardAffordanceView = i > 0 ? this.mLeftIcon : this.mRightIcon;
            KeyguardAffordanceView keyguardAffordanceView2 = i > 0 ? this.mRightIcon : this.mLeftIcon;
            float minTranslationAmount = abs / getMinTranslationAmount();
            float max2 = Math.max(1.0f - minTranslationAmount, 0.0f);
            boolean z3 = z && z2;
            boolean z4 = z && !z2;
            float radiusFromTranslation = getRadiusFromTranslation(abs);
            boolean z5 = z && isBelowFalsingThreshold();
            if (!z) {
                updateIcon(keyguardAffordanceView, radiusFromTranslation, minTranslationAmount + (keyguardAffordanceView.getRestingAlpha() * max2), false, false, false, false);
            } else {
                updateIcon(keyguardAffordanceView, 0.0f, max2 * keyguardAffordanceView.getRestingAlpha(), z3, z5, true, z4);
            }
            boolean z6 = z3;
            boolean z7 = z5;
            boolean z8 = z4;
            updateIcon(keyguardAffordanceView2, 0.0f, max2 * keyguardAffordanceView2.getRestingAlpha(), z6, z7, z, z8);
            updateIcon(this.mCenterIcon, 0.0f, max2 * this.mCenterIcon.getRestingAlpha(), z6, z7, z, z8);
            this.mTranslation = f2;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateIconsFromTranslation(KeyguardAffordanceView keyguardAffordanceView) {
        float abs = Math.abs(this.mTranslation) / getMinTranslationAmount();
        float max = Math.max(0.0f, 1.0f - abs);
        KeyguardAffordanceView keyguardAffordanceView2 = keyguardAffordanceView == this.mRightIcon ? this.mLeftIcon : this.mRightIcon;
        updateIconAlpha(keyguardAffordanceView, abs + (keyguardAffordanceView.getRestingAlpha() * max), false);
        updateIconAlpha(keyguardAffordanceView2, keyguardAffordanceView2.getRestingAlpha() * max, false);
        updateIconAlpha(this.mCenterIcon, max * this.mCenterIcon.getRestingAlpha(), false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public float getTranslationFromRadius(float f) {
        float f2 = (f - this.mMinBackgroundRadius) / 0.25f;
        if (f2 > 0.0f) {
            return this.mTouchSlop + f2;
        }
        return 0.0f;
    }

    private float getRadiusFromTranslation(float f) {
        if (f <= this.mTouchSlop) {
            return 0.0f;
        }
        return ((f - this.mTouchSlop) * 0.25f) + this.mMinBackgroundRadius;
    }

    public void animateHideLeftRightIcon() {
        cancelAnimation();
        updateIcon(this.mRightIcon, 0.0f, 0.0f, true, false, false, false);
        updateIcon(this.mLeftIcon, 0.0f, 0.0f, true, false, false, false);
    }

    private void updateIcon(KeyguardAffordanceView keyguardAffordanceView, float f, float f2, boolean z, boolean z2, boolean z3, boolean z4) {
        if (keyguardAffordanceView.getVisibility() != 0 && !z3) {
            return;
        }
        if (z4) {
            keyguardAffordanceView.setCircleRadiusWithoutAnimation(f);
        } else {
            keyguardAffordanceView.setCircleRadius(f, z2);
        }
        updateIconAlpha(keyguardAffordanceView, f2, z);
    }

    private void updateIconAlpha(KeyguardAffordanceView keyguardAffordanceView, float f, boolean z) {
        float scale = getScale(f, keyguardAffordanceView);
        keyguardAffordanceView.setImageAlpha(Math.min(1.0f, f), z);
        keyguardAffordanceView.setImageScale(scale, z);
    }

    private float getScale(float f, KeyguardAffordanceView keyguardAffordanceView) {
        return Math.min(((f / keyguardAffordanceView.getRestingAlpha()) * 0.2f) + 0.8f, 1.5f);
    }

    private void trackMovement(MotionEvent motionEvent) {
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.addMovement(motionEvent);
        }
    }

    private void initVelocityTracker() {
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.recycle();
        }
        this.mVelocityTracker = VelocityTracker.obtain();
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
        if (this.mTargetedView == this.mRightIcon) {
            return -hypot;
        }
        return hypot;
    }

    public void onConfigurationChanged() {
        initDimens();
        initIcons();
    }

    public void onRtlPropertiesChanged() {
        initIcons();
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
        if (keyguardAffordanceView.getVisibility() != 0 ? false : z) {
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
}
