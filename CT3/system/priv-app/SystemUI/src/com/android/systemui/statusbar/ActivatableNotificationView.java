package com.android.systemui.statusbar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TimeAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewConfiguration;
import android.view.ViewPropertyAnimator;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import com.android.systemui.Interpolators;
import com.android.systemui.classifier.FalsingManager;
import com.android.systemui.statusbar.notification.FakeShadowView;
import com.android.systemui.statusbar.notification.NotificationUtils;
/* loaded from: a.zip:com/android/systemui/statusbar/ActivatableNotificationView.class */
public abstract class ActivatableNotificationView extends ExpandableOutlineView {
    private boolean mActivated;
    private float mAnimationTranslationY;
    private float mAppearAnimationFraction;
    private RectF mAppearAnimationRect;
    private float mAppearAnimationTranslation;
    private ValueAnimator mAppearAnimator;
    private ObjectAnimator mBackgroundAnimator;
    private ValueAnimator mBackgroundColorAnimator;
    private NotificationBackgroundView mBackgroundDimmed;
    private NotificationBackgroundView mBackgroundNormal;
    private ValueAnimator.AnimatorUpdateListener mBackgroundVisibilityUpdater;
    private float mBgAlpha;
    private int mBgTint;
    private Interpolator mCurrentAlphaInterpolator;
    private Interpolator mCurrentAppearInterpolator;
    private int mCurrentBackgroundTint;
    private boolean mDark;
    private boolean mDimmed;
    private float mDownX;
    private float mDownY;
    private boolean mDrawingAppearAnimation;
    private AnimatorListenerAdapter mFadeInEndListener;
    private ValueAnimator mFadeInFromDarkAnimator;
    private FakeShadowView mFakeShadow;
    private FalsingManager mFalsingManager;
    private boolean mIsBelowSpeedBump;
    private final int mLegacyColor;
    private final int mLowPriorityColor;
    private final int mLowPriorityRippleColor;
    private float mNormalBackgroundVisibilityAmount;
    private final int mNormalColor;
    protected final int mNormalRippleColor;
    private OnActivatedListener mOnActivatedListener;
    private float mShadowAlpha;
    private boolean mShowingLegacyBackground;
    private final Interpolator mSlowOutFastInInterpolator;
    private final Interpolator mSlowOutLinearInInterpolator;
    private int mStartTint;
    private final Runnable mTapTimeoutRunnable;
    private int mTargetTint;
    private final int mTintedRippleColor;
    private final float mTouchSlop;
    private boolean mTrackTouch;
    private ValueAnimator.AnimatorUpdateListener mUpdateOutlineListener;
    private static final Interpolator ACTIVATE_INVERSE_INTERPOLATOR = new PathInterpolator(0.6f, 0.0f, 0.5f, 1.0f);
    private static final Interpolator ACTIVATE_INVERSE_ALPHA_INTERPOLATOR = new PathInterpolator(0.0f, 0.0f, 0.5f, 1.0f);

    /* loaded from: a.zip:com/android/systemui/statusbar/ActivatableNotificationView$OnActivatedListener.class */
    public interface OnActivatedListener {
        void onActivated(ActivatableNotificationView activatableNotificationView);

        void onActivationReset(ActivatableNotificationView activatableNotificationView);
    }

    public ActivatableNotificationView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mBgTint = 0;
        this.mBgAlpha = 1.0f;
        this.mAppearAnimationRect = new RectF();
        this.mAppearAnimationFraction = -1.0f;
        this.mBackgroundVisibilityUpdater = new ValueAnimator.AnimatorUpdateListener(this) { // from class: com.android.systemui.statusbar.ActivatableNotificationView.1
            final ActivatableNotificationView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                this.this$0.setNormalBackgroundVisibilityAmount(this.this$0.mBackgroundNormal.getAlpha());
            }
        };
        this.mFadeInEndListener = new AnimatorListenerAdapter(this) { // from class: com.android.systemui.statusbar.ActivatableNotificationView.2
            final ActivatableNotificationView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                super.onAnimationEnd(animator);
                this.this$0.mFadeInFromDarkAnimator = null;
                this.this$0.updateBackground();
            }
        };
        this.mUpdateOutlineListener = new ValueAnimator.AnimatorUpdateListener(this) { // from class: com.android.systemui.statusbar.ActivatableNotificationView.3
            final ActivatableNotificationView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                this.this$0.updateOutlineAlpha();
            }
        };
        this.mShadowAlpha = 1.0f;
        this.mTapTimeoutRunnable = new Runnable(this) { // from class: com.android.systemui.statusbar.ActivatableNotificationView.4
            final ActivatableNotificationView this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.makeInactive(true);
            }
        };
        this.mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        this.mSlowOutFastInInterpolator = new PathInterpolator(0.8f, 0.0f, 0.6f, 1.0f);
        this.mSlowOutLinearInInterpolator = new PathInterpolator(0.8f, 0.0f, 1.0f, 1.0f);
        setClipChildren(false);
        setClipToPadding(false);
        this.mLegacyColor = context.getColor(2131558548);
        this.mNormalColor = context.getColor(2131558549);
        this.mLowPriorityColor = context.getColor(2131558551);
        this.mTintedRippleColor = context.getColor(2131558555);
        this.mLowPriorityRippleColor = context.getColor(2131558554);
        this.mNormalRippleColor = context.getColor(2131558553);
        this.mFalsingManager = FalsingManager.getInstance(context);
    }

    private int calculateBgColor(boolean z) {
        return (!z || this.mBgTint == 0) ? this.mShowingLegacyBackground ? this.mLegacyColor : this.mIsBelowSpeedBump ? this.mLowPriorityColor : this.mNormalColor : this.mBgTint;
    }

    private void cancelAppearAnimation() {
        if (this.mAppearAnimator != null) {
            this.mAppearAnimator.cancel();
            this.mAppearAnimator = null;
        }
    }

    private void cancelFadeAnimations() {
        if (this.mBackgroundAnimator != null) {
            this.mBackgroundAnimator.cancel();
        }
        this.mBackgroundDimmed.animate().cancel();
        this.mBackgroundNormal.animate().cancel();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void enableAppearDrawing(boolean z) {
        if (z != this.mDrawingAppearAnimation) {
            this.mDrawingAppearAnimation = z;
            if (!z) {
                setContentAlpha(1.0f);
                this.mAppearAnimationFraction = -1.0f;
                setOutlineRect(null);
            }
            invalidate();
        }
    }

    private void fadeDimmedBackground() {
        this.mBackgroundDimmed.animate().cancel();
        this.mBackgroundNormal.animate().cancel();
        if (this.mActivated) {
            updateBackground();
            return;
        }
        if (!shouldHideBackground()) {
            if (this.mDimmed) {
                this.mBackgroundDimmed.setVisibility(0);
            } else {
                this.mBackgroundNormal.setVisibility(0);
            }
        }
        float f = this.mDimmed ? 1.0f : 0.0f;
        float f2 = this.mDimmed ? 0.0f : 1.0f;
        int i = 220;
        if (this.mBackgroundAnimator != null) {
            f = ((Float) this.mBackgroundAnimator.getAnimatedValue()).floatValue();
            int currentPlayTime = (int) this.mBackgroundAnimator.getCurrentPlayTime();
            this.mBackgroundAnimator.removeAllListeners();
            this.mBackgroundAnimator.cancel();
            i = currentPlayTime;
            if (currentPlayTime <= 0) {
                updateBackground();
                return;
            }
        }
        this.mBackgroundNormal.setAlpha(f);
        this.mBackgroundAnimator = ObjectAnimator.ofFloat(this.mBackgroundNormal, View.ALPHA, f, f2);
        this.mBackgroundAnimator.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
        this.mBackgroundAnimator.setDuration(i);
        this.mBackgroundAnimator.addListener(new AnimatorListenerAdapter(this) { // from class: com.android.systemui.statusbar.ActivatableNotificationView.10
            final ActivatableNotificationView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                this.this$0.updateBackground();
                this.this$0.mBackgroundAnimator = null;
            }
        });
        this.mBackgroundAnimator.addUpdateListener(this.mBackgroundVisibilityUpdater);
        this.mBackgroundAnimator.start();
    }

    private void fadeInFromDark(long j) {
        NotificationBackgroundView notificationBackgroundView = this.mDimmed ? this.mBackgroundDimmed : this.mBackgroundNormal;
        notificationBackgroundView.setAlpha(0.0f);
        this.mBackgroundVisibilityUpdater.onAnimationUpdate(null);
        notificationBackgroundView.setPivotX(this.mBackgroundDimmed.getWidth() / 2.0f);
        notificationBackgroundView.setPivotY(getActualHeight() / 2.0f);
        notificationBackgroundView.setScaleX(0.93f);
        notificationBackgroundView.setScaleY(0.93f);
        notificationBackgroundView.animate().alpha(1.0f).scaleX(1.0f).scaleY(1.0f).setDuration(170L).setStartDelay(j).setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN).setListener(new AnimatorListenerAdapter(this, notificationBackgroundView) { // from class: com.android.systemui.statusbar.ActivatableNotificationView.9
            final ActivatableNotificationView this$0;
            final View val$background;

            {
                this.this$0 = this;
                this.val$background = notificationBackgroundView;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
                this.val$background.setScaleX(1.0f);
                this.val$background.setScaleY(1.0f);
                this.val$background.setAlpha(1.0f);
            }
        }).setUpdateListener(this.mBackgroundVisibilityUpdater).start();
        this.mFadeInFromDarkAnimator = TimeAnimator.ofFloat(0.0f, 1.0f);
        this.mFadeInFromDarkAnimator.setDuration(170L);
        this.mFadeInFromDarkAnimator.setStartDelay(j);
        this.mFadeInFromDarkAnimator.setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN);
        this.mFadeInFromDarkAnimator.addListener(this.mFadeInEndListener);
        this.mFadeInFromDarkAnimator.addUpdateListener(this.mUpdateOutlineListener);
        this.mFadeInFromDarkAnimator.start();
    }

    private boolean handleTouchEventDimmed(MotionEvent motionEvent) {
        switch (motionEvent.getActionMasked()) {
            case 0:
                this.mDownX = motionEvent.getX();
                this.mDownY = motionEvent.getY();
                this.mTrackTouch = true;
                if (this.mDownY > getActualHeight()) {
                    this.mTrackTouch = false;
                    break;
                }
                break;
            case 1:
                if (!isWithinTouchSlop(motionEvent)) {
                    makeInactive(true);
                    this.mTrackTouch = false;
                    break;
                } else if (handleSlideBack()) {
                    return true;
                } else {
                    if (!this.mActivated) {
                        makeActive();
                        postDelayed(this.mTapTimeoutRunnable, 1200L);
                        break;
                    } else if (!performClick()) {
                        return false;
                    }
                }
                break;
            case 2:
                if (!isWithinTouchSlop(motionEvent)) {
                    makeInactive(true);
                    this.mTrackTouch = false;
                    break;
                }
                break;
            case 3:
                makeInactive(true);
                this.mTrackTouch = false;
                break;
        }
        return this.mTrackTouch;
    }

    private boolean isWithinTouchSlop(MotionEvent motionEvent) {
        boolean z = false;
        if (Math.abs(motionEvent.getX() - this.mDownX) < this.mTouchSlop) {
            z = false;
            if (Math.abs(motionEvent.getY() - this.mDownY) < this.mTouchSlop) {
                z = true;
            }
        }
        return z;
    }

    private void makeActive() {
        this.mFalsingManager.onNotificationActive();
        startActivateAnimation(false);
        this.mActivated = true;
        if (this.mOnActivatedListener != null) {
            this.mOnActivatedListener.onActivated(this);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setBackgroundTintColor(int i) {
        this.mCurrentBackgroundTint = i;
        int i2 = i;
        if (i == this.mNormalColor) {
            i2 = 0;
        }
        this.mBackgroundDimmed.setTint(i2);
        this.mBackgroundNormal.setTint(i2);
    }

    private void setContentAlpha(float f) {
        View contentView = getContentView();
        if (contentView.hasOverlappingRendering()) {
            int i = (f == 0.0f || f == 1.0f) ? 0 : 2;
            if (contentView.getLayerType() != i) {
                contentView.setLayerType(i, null);
            }
        }
        contentView.setAlpha(f);
    }

    private void startActivateAnimation(boolean z) {
        Interpolator interpolator;
        Interpolator interpolator2;
        float f = 0.0f;
        if (isAttachedToWindow()) {
            int width = this.mBackgroundNormal.getWidth() / 2;
            int actualHeight = this.mBackgroundNormal.getActualHeight() / 2;
            float sqrt = (float) Math.sqrt((width * width) + (actualHeight * actualHeight));
            Animator createCircularReveal = z ? ViewAnimationUtils.createCircularReveal(this.mBackgroundNormal, width, actualHeight, sqrt, 0.0f) : ViewAnimationUtils.createCircularReveal(this.mBackgroundNormal, width, actualHeight, 0.0f, sqrt);
            this.mBackgroundNormal.setVisibility(0);
            if (z) {
                interpolator = ACTIVATE_INVERSE_INTERPOLATOR;
                interpolator2 = ACTIVATE_INVERSE_ALPHA_INTERPOLATOR;
            } else {
                interpolator = Interpolators.LINEAR_OUT_SLOW_IN;
                interpolator2 = Interpolators.LINEAR_OUT_SLOW_IN;
            }
            createCircularReveal.setInterpolator(interpolator);
            createCircularReveal.setDuration(220L);
            if (z) {
                this.mBackgroundNormal.setAlpha(1.0f);
                createCircularReveal.addListener(new AnimatorListenerAdapter(this) { // from class: com.android.systemui.statusbar.ActivatableNotificationView.5
                    final ActivatableNotificationView this$0;

                    {
                        this.this$0 = this;
                    }

                    @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                    public void onAnimationEnd(Animator animator) {
                        this.this$0.updateBackground();
                    }
                });
                createCircularReveal.start();
            } else {
                this.mBackgroundNormal.setAlpha(0.4f);
                createCircularReveal.start();
            }
            ViewPropertyAnimator animate = this.mBackgroundNormal.animate();
            if (!z) {
                f = 1.0f;
            }
            animate.alpha(f).setInterpolator(interpolator2).setUpdateListener(new ValueAnimator.AnimatorUpdateListener(this, z) { // from class: com.android.systemui.statusbar.ActivatableNotificationView.6
                final ActivatableNotificationView this$0;
                final boolean val$reverse;

                {
                    this.this$0 = this;
                    this.val$reverse = z;
                }

                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float animatedFraction = valueAnimator.getAnimatedFraction();
                    float f2 = animatedFraction;
                    if (this.val$reverse) {
                        f2 = 1.0f - animatedFraction;
                    }
                    this.this$0.setNormalBackgroundVisibilityAmount(f2);
                }
            }).setDuration(220L);
        }
    }

    private void startAppearAnimation(boolean z, float f, long j, long j2, Runnable runnable) {
        float f2;
        cancelAppearAnimation();
        this.mAnimationTranslationY = getActualHeight() * f;
        if (this.mAppearAnimationFraction == -1.0f) {
            if (z) {
                this.mAppearAnimationFraction = 0.0f;
                this.mAppearAnimationTranslation = this.mAnimationTranslationY;
            } else {
                this.mAppearAnimationFraction = 1.0f;
                this.mAppearAnimationTranslation = 0.0f;
            }
        }
        if (z) {
            this.mCurrentAppearInterpolator = this.mSlowOutFastInInterpolator;
            this.mCurrentAlphaInterpolator = Interpolators.LINEAR_OUT_SLOW_IN;
            f2 = 1.0f;
        } else {
            this.mCurrentAppearInterpolator = Interpolators.FAST_OUT_SLOW_IN;
            this.mCurrentAlphaInterpolator = this.mSlowOutLinearInInterpolator;
            f2 = 0.0f;
        }
        this.mAppearAnimator = ValueAnimator.ofFloat(this.mAppearAnimationFraction, f2);
        this.mAppearAnimator.setInterpolator(Interpolators.LINEAR);
        this.mAppearAnimator.setDuration(((float) j2) * Math.abs(this.mAppearAnimationFraction - f2));
        this.mAppearAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(this) { // from class: com.android.systemui.statusbar.ActivatableNotificationView.11
            final ActivatableNotificationView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                this.this$0.mAppearAnimationFraction = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                this.this$0.updateAppearAnimationAlpha();
                this.this$0.updateAppearRect();
                this.this$0.invalidate();
            }
        });
        if (j > 0) {
            updateAppearAnimationAlpha();
            updateAppearRect();
            this.mAppearAnimator.setStartDelay(j);
        }
        this.mAppearAnimator.addListener(new AnimatorListenerAdapter(this, runnable) { // from class: com.android.systemui.statusbar.ActivatableNotificationView.12
            private boolean mWasCancelled;
            final ActivatableNotificationView this$0;
            final Runnable val$onFinishedRunnable;

            {
                this.this$0 = this;
                this.val$onFinishedRunnable = runnable;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
                this.mWasCancelled = true;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                if (this.val$onFinishedRunnable != null) {
                    this.val$onFinishedRunnable.run();
                }
                if (this.mWasCancelled) {
                    return;
                }
                this.this$0.enableAppearDrawing(false);
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                this.mWasCancelled = false;
            }
        });
        this.mAppearAnimator.start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateAppearAnimationAlpha() {
        setContentAlpha(this.mCurrentAlphaInterpolator.getInterpolation(Math.min(1.0f, this.mAppearAnimationFraction / 1.0f)));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateAppearRect() {
        float f;
        float f2;
        float f3 = 1.0f - this.mAppearAnimationFraction;
        float interpolation = this.mCurrentAppearInterpolator.getInterpolation(f3) * this.mAnimationTranslationY;
        this.mAppearAnimationTranslation = interpolation;
        float width = getWidth() * 0.475f * this.mCurrentAppearInterpolator.getInterpolation(Math.min(1.0f, Math.max(0.0f, (f3 - 0.0f) / 0.8f)));
        float width2 = getWidth() - width;
        float interpolation2 = this.mCurrentAppearInterpolator.getInterpolation(Math.max(0.0f, (f3 - 0.0f) / 1.0f));
        int actualHeight = getActualHeight();
        if (this.mAnimationTranslationY > 0.0f) {
            f2 = (actualHeight - ((this.mAnimationTranslationY * interpolation2) * 0.1f)) - interpolation;
            f = f2 * interpolation2;
        } else {
            f = (((actualHeight + this.mAnimationTranslationY) * interpolation2) * 0.1f) - interpolation;
            f2 = (actualHeight * (1.0f - interpolation2)) + (f * interpolation2);
        }
        this.mAppearAnimationRect.set(width, f, width2, f2);
        setOutlineRect(width, this.mAppearAnimationTranslation + f, width2, this.mAppearAnimationTranslation + f2);
    }

    private void updateBackgroundTint(boolean z) {
        if (this.mBackgroundColorAnimator != null) {
            this.mBackgroundColorAnimator.cancel();
        }
        int rippleColor = getRippleColor();
        this.mBackgroundDimmed.setRippleColor(rippleColor);
        this.mBackgroundNormal.setRippleColor(rippleColor);
        int calculateBgColor = calculateBgColor();
        if (!z) {
            setBackgroundTintColor(calculateBgColor);
        } else if (calculateBgColor != this.mCurrentBackgroundTint) {
            this.mStartTint = this.mCurrentBackgroundTint;
            this.mTargetTint = calculateBgColor;
            this.mBackgroundColorAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
            this.mBackgroundColorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(this) { // from class: com.android.systemui.statusbar.ActivatableNotificationView.7
                final ActivatableNotificationView this$0;

                {
                    this.this$0 = this;
                }

                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    this.this$0.setBackgroundTintColor(NotificationUtils.interpolateColors(this.this$0.mStartTint, this.this$0.mTargetTint, valueAnimator.getAnimatedFraction()));
                }
            });
            this.mBackgroundColorAnimator.setDuration(360L);
            this.mBackgroundColorAnimator.setInterpolator(Interpolators.LINEAR);
            this.mBackgroundColorAnimator.addListener(new AnimatorListenerAdapter(this) { // from class: com.android.systemui.statusbar.ActivatableNotificationView.8
                final ActivatableNotificationView this$0;

                {
                    this.this$0 = this;
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    this.this$0.mBackgroundColorAnimator = null;
                }
            });
            this.mBackgroundColorAnimator.start();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateOutlineAlpha() {
        if (this.mDark) {
            setOutlineAlpha(0.0f);
            return;
        }
        float f = (0.7f + (0.3f * this.mNormalBackgroundVisibilityAmount)) * this.mShadowAlpha;
        float f2 = f;
        if (this.mFadeInFromDarkAnimator != null) {
            f2 = f * this.mFadeInFromDarkAnimator.getAnimatedFraction();
        }
        setOutlineAlpha(f2);
    }

    public int calculateBgColor() {
        return calculateBgColor(true);
    }

    public void cancelAppearDrawing() {
        cancelAppearAnimation();
        enableAppearDrawing(false);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean disallowSingleClick(MotionEvent motionEvent) {
        return false;
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void dispatchDraw(Canvas canvas) {
        if (this.mDrawingAppearAnimation) {
            canvas.save();
            canvas.translate(0.0f, this.mAppearAnimationTranslation);
        }
        super.dispatchDraw(canvas);
        if (this.mDrawingAppearAnimation) {
            canvas.restore();
        }
    }

    @Override // android.view.View
    public void drawableHotspotChanged(float f, float f2) {
        if (this.mDimmed) {
            return;
        }
        this.mBackgroundNormal.drawableHotspotChanged(f, f2);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (this.mDimmed) {
            this.mBackgroundDimmed.setState(getDrawableState());
        } else {
            this.mBackgroundNormal.setState(getDrawableState());
        }
    }

    public int getBackgroundColorWithoutTint() {
        return calculateBgColor(false);
    }

    protected abstract View getContentView();

    protected int getRippleColor() {
        if (this.mBgTint == 0 && !this.mShowingLegacyBackground) {
            return this.mIsBelowSpeedBump ? this.mLowPriorityRippleColor : this.mNormalRippleColor;
        }
        return this.mTintedRippleColor;
    }

    @Override // com.android.systemui.statusbar.ExpandableView
    public float getShadowAlpha() {
        return this.mShadowAlpha;
    }

    protected boolean handleSlideBack() {
        return false;
    }

    public void makeInactive(boolean z) {
        if (this.mActivated) {
            this.mActivated = false;
            if (this.mDimmed) {
                if (z) {
                    startActivateAnimation(true);
                } else {
                    updateBackground();
                }
            }
        }
        if (this.mOnActivatedListener != null) {
            this.mOnActivatedListener.onActivationReset(this);
        }
        removeCallbacks(this.mTapTimeoutRunnable);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mBackgroundNormal = (NotificationBackgroundView) findViewById(2131886692);
        this.mFakeShadow = (FakeShadowView) findViewById(2131886698);
        this.mBackgroundDimmed = (NotificationBackgroundView) findViewById(2131886693);
        this.mBackgroundNormal.setCustomBackground(2130837954);
        this.mBackgroundDimmed.setCustomBackground(2130837955);
        updateBackground();
        updateBackgroundTint();
        updateOutlineAlpha();
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        if (this.mDimmed && !this.mActivated && motionEvent.getActionMasked() == 0 && disallowSingleClick(motionEvent)) {
            return true;
        }
        return super.onInterceptTouchEvent(motionEvent);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.ExpandableView, android.widget.FrameLayout, android.view.ViewGroup, android.view.View
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        setPivotX(getWidth() / 2);
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        boolean onTouchEvent;
        if (this.mDimmed) {
            boolean z = this.mActivated;
            boolean handleTouchEventDimmed = handleTouchEventDimmed(motionEvent);
            onTouchEvent = handleTouchEventDimmed;
            if (z) {
                onTouchEvent = handleTouchEventDimmed;
                if (handleTouchEventDimmed) {
                    onTouchEvent = handleTouchEventDimmed;
                    if (motionEvent.getAction() == 1) {
                        this.mFalsingManager.onNotificationDoubleTap();
                        removeCallbacks(this.mTapTimeoutRunnable);
                        onTouchEvent = handleTouchEventDimmed;
                    }
                }
            }
        } else {
            onTouchEvent = super.onTouchEvent(motionEvent);
        }
        return onTouchEvent;
    }

    @Override // com.android.systemui.statusbar.ExpandableView
    public void performAddAnimation(long j, long j2) {
        enableAppearDrawing(true);
        if (this.mDrawingAppearAnimation) {
            startAppearAnimation(true, -1.0f, j, j2, null);
        }
    }

    @Override // com.android.systemui.statusbar.ExpandableView
    public void performRemoveAnimation(long j, float f, Runnable runnable) {
        enableAppearDrawing(true);
        if (this.mDrawingAppearAnimation) {
            startAppearAnimation(false, f, 0L, j, runnable);
        } else if (runnable != null) {
            runnable.run();
        }
    }

    public void reset() {
        setTintColor(0);
        resetBackgroundAlpha();
        setShowingLegacyBackground(false);
        setBelowSpeedBump(false);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void resetBackgroundAlpha() {
        updateBackgroundAlpha(0.0f);
    }

    @Override // com.android.systemui.statusbar.ExpandableOutlineView, com.android.systemui.statusbar.ExpandableView
    public void setActualHeight(int i, boolean z) {
        super.setActualHeight(i, z);
        setPivotY(i / 2);
        this.mBackgroundNormal.setActualHeight(i);
        this.mBackgroundDimmed.setActualHeight(i);
    }

    @Override // com.android.systemui.statusbar.ExpandableView
    public void setBelowSpeedBump(boolean z) {
        super.setBelowSpeedBump(z);
        if (z != this.mIsBelowSpeedBump) {
            this.mIsBelowSpeedBump = z;
            updateBackgroundTint();
        }
    }

    @Override // com.android.systemui.statusbar.ExpandableOutlineView, com.android.systemui.statusbar.ExpandableView
    public void setClipTopAmount(int i) {
        super.setClipTopAmount(i);
        this.mBackgroundNormal.setClipTopAmount(i);
        this.mBackgroundDimmed.setClipTopAmount(i);
    }

    @Override // com.android.systemui.statusbar.ExpandableView
    public void setDark(boolean z, boolean z2, long j) {
        super.setDark(z, z2, j);
        if (this.mDark == z) {
            return;
        }
        this.mDark = z;
        updateBackground();
        if (!z && z2 && !shouldHideBackground()) {
            fadeInFromDark(j);
        }
        updateOutlineAlpha();
    }

    @Override // com.android.systemui.statusbar.ExpandableView
    public void setDimmed(boolean z, boolean z2) {
        if (this.mDimmed != z) {
            this.mDimmed = z;
            resetBackgroundAlpha();
            if (z2) {
                fadeDimmedBackground();
            } else {
                updateBackground();
            }
        }
    }

    @Override // com.android.systemui.statusbar.ExpandableView
    public void setFakeShadowIntensity(float f, float f2, int i, int i2) {
        this.mFakeShadow.setFakeShadowTranslationZ((getTranslationZ() + 0.1f) * f, f2, i, i2);
    }

    public void setNormalBackgroundVisibilityAmount(float f) {
        this.mNormalBackgroundVisibilityAmount = f;
        updateOutlineAlpha();
    }

    public void setOnActivatedListener(OnActivatedListener onActivatedListener) {
        this.mOnActivatedListener = onActivatedListener;
    }

    @Override // com.android.systemui.statusbar.ExpandableView
    public void setShadowAlpha(float f) {
        if (f != this.mShadowAlpha) {
            this.mShadowAlpha = f;
            updateOutlineAlpha();
        }
    }

    public void setShowingLegacyBackground(boolean z) {
        this.mShowingLegacyBackground = z;
        updateBackgroundTint();
    }

    public void setTintColor(int i) {
        setTintColor(i, false);
    }

    public void setTintColor(int i, boolean z) {
        this.mBgTint = i;
        updateBackgroundTint(z);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean shouldHideBackground() {
        return this.mDark;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX WARN: Code restructure failed: missing block: B:21:0x0067, code lost:
        if (r6 != false) goto L24;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public void updateBackground() {
        int i;
        cancelFadeAnimations();
        if (shouldHideBackground()) {
            this.mBackgroundDimmed.setVisibility(4);
            this.mBackgroundNormal.setVisibility(4);
        } else if (this.mDimmed) {
            boolean isChildInGroup = isGroupExpansionChanging() ? isChildInGroup() : false;
            this.mBackgroundDimmed.setVisibility(isChildInGroup ? 4 : 0);
            NotificationBackgroundView notificationBackgroundView = this.mBackgroundNormal;
            if (!this.mActivated) {
                i = 4;
            }
            i = 0;
            notificationBackgroundView.setVisibility(i);
        } else {
            this.mBackgroundDimmed.setVisibility(4);
            this.mBackgroundNormal.setVisibility(0);
            this.mBackgroundNormal.setAlpha(1.0f);
            removeCallbacks(this.mTapTimeoutRunnable);
            makeInactive(false);
        }
        setNormalBackgroundVisibilityAmount(this.mBackgroundNormal.getVisibility() == 0 ? 1.0f : 0.0f);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void updateBackgroundAlpha(float f) {
        if (!isChildInGroup() || !this.mDimmed) {
            f = 1.0f;
        }
        this.mBgAlpha = f;
        this.mBackgroundDimmed.setAlpha(this.mBgAlpha);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void updateBackgroundTint() {
        updateBackgroundTint(false);
    }
}
