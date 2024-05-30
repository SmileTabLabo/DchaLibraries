package com.android.systemui.statusbar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TimeAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.MathUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewPropertyAnimator;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.classifier.FalsingManager;
import com.android.systemui.statusbar.notification.FakeShadowView;
import com.android.systemui.statusbar.notification.NotificationUtils;
import com.android.systemui.statusbar.phone.DoubleTapHelper;
import java.util.Objects;
/* loaded from: classes.dex */
public abstract class ActivatableNotificationView extends ExpandableOutlineView {
    private final AccessibilityManager mAccessibilityManager;
    private boolean mActivated;
    private float mAnimationTranslationY;
    private float mAppearAnimationFraction;
    private RectF mAppearAnimationRect;
    private float mAppearAnimationTranslation;
    private ValueAnimator mAppearAnimator;
    private ObjectAnimator mBackgroundAnimator;
    private ValueAnimator mBackgroundColorAnimator;
    private NotificationBackgroundView mBackgroundDimmed;
    protected NotificationBackgroundView mBackgroundNormal;
    private ValueAnimator.AnimatorUpdateListener mBackgroundVisibilityUpdater;
    private float mBgAlpha;
    protected int mBgTint;
    private boolean mBlockNextTouch;
    private Interpolator mCurrentAlphaInterpolator;
    private Interpolator mCurrentAppearInterpolator;
    private int mCurrentBackgroundTint;
    private boolean mDark;
    private boolean mDimmed;
    private int mDimmedAlpha;
    private float mDimmedBackgroundFadeInAmount;
    private final DoubleTapHelper mDoubleTapHelper;
    private boolean mDrawingAppearAnimation;
    private AnimatorListenerAdapter mFadeInEndListener;
    private ValueAnimator mFadeInFromDarkAnimator;
    private FakeShadowView mFakeShadow;
    private FalsingManager mFalsingManager;
    private int mHeadsUpAddStartLocation;
    private float mHeadsUpLocation;
    private boolean mIsAppearing;
    private boolean mIsBelowSpeedBump;
    private boolean mIsHeadsUpAnimation;
    private boolean mNeedsDimming;
    private float mNormalBackgroundVisibilityAmount;
    private final int mNormalColor;
    protected final int mNormalRippleColor;
    private OnActivatedListener mOnActivatedListener;
    private float mOverrideAmount;
    private int mOverrideTint;
    private float mShadowAlpha;
    private boolean mShadowHidden;
    private final Interpolator mSlowOutFastInInterpolator;
    private final Interpolator mSlowOutLinearInInterpolator;
    private int mStartTint;
    private final Runnable mTapTimeoutRunnable;
    private int mTargetTint;
    private final int mTintedRippleColor;
    private ValueAnimator.AnimatorUpdateListener mUpdateOutlineListener;
    private static final Interpolator ACTIVATE_INVERSE_INTERPOLATOR = new PathInterpolator(0.6f, 0.0f, 0.5f, 1.0f);
    private static final Interpolator ACTIVATE_INVERSE_ALPHA_INTERPOLATOR = new PathInterpolator(0.0f, 0.0f, 0.5f, 1.0f);

    /* loaded from: classes.dex */
    public interface OnActivatedListener {
        void onActivated(ActivatableNotificationView activatableNotificationView);

        void onActivationReset(ActivatableNotificationView activatableNotificationView);
    }

    protected abstract View getContentView();

    public ActivatableNotificationView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mBgTint = 0;
        this.mBgAlpha = 1.0f;
        this.mAppearAnimationRect = new RectF();
        this.mAppearAnimationFraction = -1.0f;
        this.mDimmedBackgroundFadeInAmount = -1.0f;
        this.mBackgroundVisibilityUpdater = new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.statusbar.ActivatableNotificationView.1
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                ActivatableNotificationView.this.setNormalBackgroundVisibilityAmount(ActivatableNotificationView.this.mBackgroundNormal.getAlpha());
                ActivatableNotificationView.this.mDimmedBackgroundFadeInAmount = ActivatableNotificationView.this.mBackgroundDimmed.getAlpha();
            }
        };
        this.mFadeInEndListener = new AnimatorListenerAdapter() { // from class: com.android.systemui.statusbar.ActivatableNotificationView.2
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                super.onAnimationEnd(animator);
                ActivatableNotificationView.this.mFadeInFromDarkAnimator = null;
                ActivatableNotificationView.this.mDimmedBackgroundFadeInAmount = -1.0f;
                ActivatableNotificationView.this.updateBackground();
            }
        };
        this.mUpdateOutlineListener = new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.statusbar.ActivatableNotificationView.3
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                ActivatableNotificationView.this.updateOutlineAlpha();
            }
        };
        this.mShadowAlpha = 1.0f;
        this.mTapTimeoutRunnable = new Runnable() { // from class: com.android.systemui.statusbar.ActivatableNotificationView.4
            @Override // java.lang.Runnable
            public void run() {
                ActivatableNotificationView.this.makeInactive(true);
            }
        };
        this.mSlowOutFastInInterpolator = new PathInterpolator(0.8f, 0.0f, 0.6f, 1.0f);
        this.mSlowOutLinearInInterpolator = new PathInterpolator(0.8f, 0.0f, 1.0f, 1.0f);
        setClipChildren(false);
        setClipToPadding(false);
        this.mNormalColor = context.getColor(R.color.notification_material_background_color);
        this.mTintedRippleColor = context.getColor(R.color.notification_ripple_tinted_color);
        this.mNormalRippleColor = context.getColor(R.color.notification_ripple_untinted_color);
        this.mFalsingManager = FalsingManager.getInstance(context);
        this.mAccessibilityManager = AccessibilityManager.getInstance(this.mContext);
        DoubleTapHelper.ActivationListener activationListener = new DoubleTapHelper.ActivationListener() { // from class: com.android.systemui.statusbar.-$$Lambda$ActivatableNotificationView$UY9VuuIVVnbnmWBGvyVyrd85Q1w
            @Override // com.android.systemui.statusbar.phone.DoubleTapHelper.ActivationListener
            public final void onActiveChanged(boolean z) {
                ActivatableNotificationView.lambda$new$0(ActivatableNotificationView.this, z);
            }
        };
        DoubleTapHelper.DoubleTapListener doubleTapListener = new DoubleTapHelper.DoubleTapListener() { // from class: com.android.systemui.statusbar.-$$Lambda$ActivatableNotificationView$s9Lg6gd_pJUKy0ZrSBDjqcS3dhE
            @Override // com.android.systemui.statusbar.phone.DoubleTapHelper.DoubleTapListener
            public final boolean onDoubleTap() {
                boolean performClick;
                performClick = super/*com.android.systemui.statusbar.ExpandableOutlineView*/.performClick();
                return performClick;
            }
        };
        DoubleTapHelper.SlideBackListener slideBackListener = new DoubleTapHelper.SlideBackListener() { // from class: com.android.systemui.statusbar.-$$Lambda$S0gLtMwWSvptmQSsp_EwTT5xQHY
            @Override // com.android.systemui.statusbar.phone.DoubleTapHelper.SlideBackListener
            public final boolean onSlideBack() {
                return ActivatableNotificationView.this.handleSlideBack();
            }
        };
        final FalsingManager falsingManager = this.mFalsingManager;
        Objects.requireNonNull(falsingManager);
        this.mDoubleTapHelper = new DoubleTapHelper(this, activationListener, doubleTapListener, slideBackListener, new DoubleTapHelper.DoubleTapLogListener() { // from class: com.android.systemui.statusbar.-$$Lambda$kuG3qm72pLcP-jShb_m2hOhoaKY
            @Override // com.android.systemui.statusbar.phone.DoubleTapHelper.DoubleTapLogListener
            public final void onDoubleTapLog(boolean z, float f, float f2) {
                FalsingManager.this.onNotificationDoubleTap(z, f, f2);
            }
        });
        initDimens();
    }

    public static /* synthetic */ void lambda$new$0(ActivatableNotificationView activatableNotificationView, boolean z) {
        if (z) {
            activatableNotificationView.makeActive();
        } else {
            activatableNotificationView.makeInactive(true);
        }
    }

    private void initDimens() {
        this.mHeadsUpAddStartLocation = getResources().getDimensionPixelSize(17105200);
    }

    @Override // com.android.systemui.statusbar.ExpandableOutlineView
    public void onDensityOrFontScaleChanged() {
        super.onDensityOrFontScaleChanged();
        initDimens();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mBackgroundNormal = (NotificationBackgroundView) findViewById(R.id.backgroundNormal);
        this.mFakeShadow = (FakeShadowView) findViewById(R.id.fake_shadow);
        this.mShadowHidden = this.mFakeShadow.getVisibility() != 0;
        this.mBackgroundDimmed = (NotificationBackgroundView) findViewById(R.id.backgroundDimmed);
        this.mDimmedAlpha = Color.alpha(this.mContext.getColor(R.color.notification_material_background_dimmed_color));
        initBackground();
        updateBackground();
        updateBackgroundTint();
        updateOutlineAlpha();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void initBackground() {
        this.mBackgroundNormal.setCustomBackground(R.drawable.notification_material_bg);
        this.mBackgroundDimmed.setCustomBackground(R.drawable.notification_material_bg_dim);
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        if (this.mNeedsDimming && motionEvent.getActionMasked() == 0 && disallowSingleClick(motionEvent) && !isTouchExplorationEnabled()) {
            if (!this.mActivated) {
                return true;
            }
            if (!this.mDoubleTapHelper.isWithinDoubleTapSlop(motionEvent)) {
                this.mBlockNextTouch = true;
                makeInactive(true);
                return true;
            }
        }
        return super.onInterceptTouchEvent(motionEvent);
    }

    private boolean isTouchExplorationEnabled() {
        return this.mAccessibilityManager.isTouchExplorationEnabled();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean disallowSingleClick(MotionEvent motionEvent) {
        return false;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean handleSlideBack() {
        return false;
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (this.mBlockNextTouch) {
            this.mBlockNextTouch = false;
            return false;
        } else if (this.mNeedsDimming && !isTouchExplorationEnabled() && isInteractive()) {
            boolean z = this.mActivated;
            boolean handleTouchEventDimmed = handleTouchEventDimmed(motionEvent);
            if (z && handleTouchEventDimmed && motionEvent.getAction() == 1) {
                removeCallbacks(this.mTapTimeoutRunnable);
                return handleTouchEventDimmed;
            }
            return handleTouchEventDimmed;
        } else {
            return super.onTouchEvent(motionEvent);
        }
    }

    protected boolean isInteractive() {
        return true;
    }

    @Override // android.view.View
    public void drawableHotspotChanged(float f, float f2) {
        if (!this.mDimmed) {
            this.mBackgroundNormal.drawableHotspotChanged(f, f2);
        }
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

    public void setRippleAllowed(boolean z) {
        this.mBackgroundNormal.setPressedAllowed(z);
    }

    private boolean handleTouchEventDimmed(MotionEvent motionEvent) {
        if (this.mNeedsDimming && !this.mDimmed) {
            super.onTouchEvent(motionEvent);
        }
        return this.mDoubleTapHelper.onTouchEvent(motionEvent, getActualHeight());
    }

    @Override // android.view.View
    public boolean performClick() {
        if (!this.mNeedsDimming || isTouchExplorationEnabled()) {
            return super.performClick();
        }
        return false;
    }

    private void makeActive() {
        this.mFalsingManager.onNotificationActive();
        startActivateAnimation(false);
        this.mActivated = true;
        if (this.mOnActivatedListener != null) {
            this.mOnActivatedListener.onActivated(this);
        }
    }

    private void startActivateAnimation(final boolean z) {
        Interpolator interpolator;
        Interpolator interpolator2;
        if (!isAttachedToWindow() || !isDimmable()) {
            return;
        }
        int width = this.mBackgroundNormal.getWidth() / 2;
        int actualHeight = this.mBackgroundNormal.getActualHeight() / 2;
        float sqrt = (float) Math.sqrt((width * width) + (actualHeight * actualHeight));
        Animator createCircularReveal = z ? ViewAnimationUtils.createCircularReveal(this.mBackgroundNormal, width, actualHeight, sqrt, 0.0f) : ViewAnimationUtils.createCircularReveal(this.mBackgroundNormal, width, actualHeight, 0.0f, sqrt);
        this.mBackgroundNormal.setVisibility(0);
        if (!z) {
            interpolator = Interpolators.LINEAR_OUT_SLOW_IN;
            interpolator2 = Interpolators.LINEAR_OUT_SLOW_IN;
        } else {
            interpolator = ACTIVATE_INVERSE_INTERPOLATOR;
            interpolator2 = ACTIVATE_INVERSE_ALPHA_INTERPOLATOR;
        }
        createCircularReveal.setInterpolator(interpolator);
        createCircularReveal.setDuration(220L);
        float f = 1.0f;
        if (z) {
            this.mBackgroundNormal.setAlpha(1.0f);
            createCircularReveal.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.statusbar.ActivatableNotificationView.5
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    ActivatableNotificationView.this.updateBackground();
                }
            });
            createCircularReveal.start();
        } else {
            this.mBackgroundNormal.setAlpha(0.4f);
            createCircularReveal.start();
        }
        ViewPropertyAnimator animate = this.mBackgroundNormal.animate();
        if (z) {
            f = 0.0f;
        }
        animate.alpha(f).setInterpolator(interpolator2).setUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.statusbar.ActivatableNotificationView.6
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float animatedFraction = valueAnimator.getAnimatedFraction();
                if (z) {
                    animatedFraction = 1.0f - animatedFraction;
                }
                ActivatableNotificationView.this.setNormalBackgroundVisibilityAmount(animatedFraction);
            }
        }).setDuration(220L);
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

    @Override // com.android.systemui.statusbar.ExpandableView
    public void setDimmed(boolean z, boolean z2) {
        this.mNeedsDimming = z;
        boolean isDimmable = z & isDimmable();
        if (this.mDimmed != isDimmable) {
            this.mDimmed = isDimmable;
            resetBackgroundAlpha();
            if (z2) {
                fadeDimmedBackground();
            } else {
                updateBackground();
            }
        }
    }

    public boolean isDimmable() {
        return true;
    }

    @Override // com.android.systemui.statusbar.ExpandableView
    public void setDark(boolean z, boolean z2, long j) {
        super.setDark(z, z2, j);
        if (this.mDark == z) {
            return;
        }
        this.mDark = z;
        updateBackground();
        updateBackgroundTint(false);
        if (!z && z2 && !shouldHideBackground()) {
            fadeInFromDark(j);
        }
        updateOutlineAlpha();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateOutlineAlpha() {
        if (this.mDark) {
            setOutlineAlpha(0.0f);
            return;
        }
        float f = (0.7f + (0.3f * this.mNormalBackgroundVisibilityAmount)) * this.mShadowAlpha;
        if (this.mFadeInFromDarkAnimator != null) {
            f *= this.mFadeInFromDarkAnimator.getAnimatedFraction();
        }
        setOutlineAlpha(f);
    }

    public void setNormalBackgroundVisibilityAmount(float f) {
        this.mNormalBackgroundVisibilityAmount = f;
        updateOutlineAlpha();
    }

    @Override // com.android.systemui.statusbar.ExpandableView
    public void setBelowSpeedBump(boolean z) {
        super.setBelowSpeedBump(z);
        if (z != this.mIsBelowSpeedBump) {
            this.mIsBelowSpeedBump = z;
            updateBackgroundTint();
            onBelowSpeedBumpChanged();
        }
    }

    protected void onBelowSpeedBumpChanged() {
    }

    public boolean isBelowSpeedBump() {
        return this.mIsBelowSpeedBump;
    }

    public void setTintColor(int i) {
        setTintColor(i, false);
    }

    public void setTintColor(int i, boolean z) {
        if (i != this.mBgTint) {
            this.mBgTint = i;
            updateBackgroundTint(z);
        }
    }

    @Override // com.android.systemui.statusbar.ExpandableOutlineView, com.android.systemui.statusbar.ExpandableView
    public void setDistanceToTopRoundness(float f) {
        super.setDistanceToTopRoundness(f);
        this.mBackgroundNormal.setDistanceToTopRoundness(f);
        this.mBackgroundDimmed.setDistanceToTopRoundness(f);
    }

    public void setOverrideTintColor(int i, float f) {
        if (this.mDark) {
            i = 0;
            f = 0.0f;
        }
        this.mOverrideTint = i;
        this.mOverrideAmount = f;
        setBackgroundTintColor(calculateBgColor());
        if (!isDimmable() && this.mNeedsDimming) {
            this.mBackgroundNormal.setDrawableAlpha((int) NotificationUtils.interpolate(255.0f, this.mDimmedAlpha, f));
        } else {
            this.mBackgroundNormal.setDrawableAlpha(255);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void updateBackgroundTint() {
        updateBackgroundTint(false);
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
            this.mBackgroundColorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.statusbar.ActivatableNotificationView.7
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    ActivatableNotificationView.this.setBackgroundTintColor(NotificationUtils.interpolateColors(ActivatableNotificationView.this.mStartTint, ActivatableNotificationView.this.mTargetTint, valueAnimator.getAnimatedFraction()));
                }
            });
            this.mBackgroundColorAnimator.setDuration(360L);
            this.mBackgroundColorAnimator.setInterpolator(Interpolators.LINEAR);
            this.mBackgroundColorAnimator.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.statusbar.ActivatableNotificationView.8
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    ActivatableNotificationView.this.mBackgroundColorAnimator = null;
                }
            });
            this.mBackgroundColorAnimator.start();
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void setBackgroundTintColor(int i) {
        if (i != this.mCurrentBackgroundTint) {
            this.mCurrentBackgroundTint = i;
            if (i == this.mNormalColor) {
                i = 0;
            }
            this.mBackgroundDimmed.setTint(i);
            this.mBackgroundNormal.setTint(i);
        }
    }

    private void fadeInFromDark(long j) {
        final NotificationBackgroundView notificationBackgroundView = this.mDimmed ? this.mBackgroundDimmed : this.mBackgroundNormal;
        notificationBackgroundView.setAlpha(0.0f);
        this.mBackgroundVisibilityUpdater.onAnimationUpdate(null);
        notificationBackgroundView.animate().alpha(1.0f).setDuration(500L).setStartDelay(j).setInterpolator(Interpolators.ALPHA_IN).setListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.statusbar.ActivatableNotificationView.9
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
                notificationBackgroundView.setAlpha(1.0f);
            }
        }).setUpdateListener(this.mBackgroundVisibilityUpdater).start();
        this.mFadeInFromDarkAnimator = TimeAnimator.ofFloat(0.0f, 1.0f);
        this.mFadeInFromDarkAnimator.setDuration(500L);
        this.mFadeInFromDarkAnimator.setStartDelay(j);
        this.mFadeInFromDarkAnimator.setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN);
        this.mFadeInFromDarkAnimator.addListener(this.mFadeInEndListener);
        this.mFadeInFromDarkAnimator.addUpdateListener(this.mUpdateOutlineListener);
        this.mFadeInFromDarkAnimator.start();
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
            i = (int) this.mBackgroundAnimator.getCurrentPlayTime();
            this.mBackgroundAnimator.removeAllListeners();
            this.mBackgroundAnimator.cancel();
            if (i <= 0) {
                updateBackground();
                return;
            }
        }
        this.mBackgroundNormal.setAlpha(f);
        this.mBackgroundAnimator = ObjectAnimator.ofFloat(this.mBackgroundNormal, View.ALPHA, f, f2);
        this.mBackgroundAnimator.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
        this.mBackgroundAnimator.setDuration(i);
        this.mBackgroundAnimator.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.statusbar.ActivatableNotificationView.10
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                ActivatableNotificationView.this.updateBackground();
                ActivatableNotificationView.this.mBackgroundAnimator = null;
                if (ActivatableNotificationView.this.mFadeInFromDarkAnimator == null) {
                    ActivatableNotificationView.this.mDimmedBackgroundFadeInAmount = -1.0f;
                }
            }
        });
        this.mBackgroundAnimator.addUpdateListener(this.mBackgroundVisibilityUpdater);
        this.mBackgroundAnimator.start();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void updateBackgroundAlpha(float f) {
        this.mBgAlpha = (isChildInGroup() && this.mDimmed) ? 1.0f : 1.0f;
        if (this.mDimmedBackgroundFadeInAmount != -1.0f) {
            this.mBgAlpha *= this.mDimmedBackgroundFadeInAmount;
        }
        this.mBackgroundDimmed.setAlpha(this.mBgAlpha);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void resetBackgroundAlpha() {
        updateBackgroundAlpha(0.0f);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void updateBackground() {
        cancelFadeAnimations();
        if (shouldHideBackground()) {
            this.mBackgroundDimmed.setVisibility(4);
            this.mBackgroundNormal.setVisibility(this.mActivated ? 0 : 4);
        } else if (this.mDimmed) {
            boolean z = isGroupExpansionChanging() && isChildInGroup();
            this.mBackgroundDimmed.setVisibility(z ? 4 : 0);
            NotificationBackgroundView notificationBackgroundView = this.mBackgroundNormal;
            if (this.mActivated || z) {
                r2 = 0;
            }
            notificationBackgroundView.setVisibility(r2);
        } else {
            this.mBackgroundDimmed.setVisibility(4);
            this.mBackgroundNormal.setVisibility(0);
            this.mBackgroundNormal.setAlpha(1.0f);
            removeCallbacks(this.mTapTimeoutRunnable);
            makeInactive(false);
        }
        setNormalBackgroundVisibilityAmount(this.mBackgroundNormal.getVisibility() != 0 ? 0.0f : 1.0f);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void updateBackgroundClipping() {
        this.mBackgroundNormal.setBottomAmountClips(!isChildInGroup());
        this.mBackgroundDimmed.setBottomAmountClips(!isChildInGroup());
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean shouldHideBackground() {
        return this.mDark;
    }

    private void cancelFadeAnimations() {
        if (this.mBackgroundAnimator != null) {
            this.mBackgroundAnimator.cancel();
        }
        this.mBackgroundDimmed.animate().cancel();
        this.mBackgroundNormal.animate().cancel();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.ExpandableView, android.widget.FrameLayout, android.view.ViewGroup, android.view.View
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        setPivotX(getWidth() / 2);
    }

    @Override // com.android.systemui.statusbar.ExpandableOutlineView, com.android.systemui.statusbar.ExpandableView
    public void setActualHeight(int i, boolean z) {
        super.setActualHeight(i, z);
        setPivotY(i / 2);
        this.mBackgroundNormal.setActualHeight(i);
        this.mBackgroundDimmed.setActualHeight(i);
    }

    @Override // com.android.systemui.statusbar.ExpandableOutlineView, com.android.systemui.statusbar.ExpandableView
    public void setClipTopAmount(int i) {
        super.setClipTopAmount(i);
        this.mBackgroundNormal.setClipTopAmount(i);
        this.mBackgroundDimmed.setClipTopAmount(i);
    }

    @Override // com.android.systemui.statusbar.ExpandableOutlineView, com.android.systemui.statusbar.ExpandableView
    public void setClipBottomAmount(int i) {
        super.setClipBottomAmount(i);
        this.mBackgroundNormal.setClipBottomAmount(i);
        this.mBackgroundDimmed.setClipBottomAmount(i);
    }

    @Override // com.android.systemui.statusbar.ExpandableView
    public void performRemoveAnimation(long j, long j2, float f, boolean z, float f2, Runnable runnable, AnimatorListenerAdapter animatorListenerAdapter) {
        enableAppearDrawing(true);
        this.mIsHeadsUpAnimation = z;
        this.mHeadsUpLocation = f2;
        if (this.mDrawingAppearAnimation) {
            startAppearAnimation(false, f, j2, j, runnable, animatorListenerAdapter);
        } else if (runnable != null) {
            runnable.run();
        }
    }

    @Override // com.android.systemui.statusbar.ExpandableView
    public void performAddAnimation(long j, long j2, boolean z) {
        enableAppearDrawing(true);
        this.mIsHeadsUpAnimation = z;
        this.mHeadsUpLocation = this.mHeadsUpAddStartLocation;
        if (this.mDrawingAppearAnimation) {
            startAppearAnimation(true, z ? 0.0f : -1.0f, j, j2, null, null);
        }
    }

    private void startAppearAnimation(final boolean z, float f, long j, long j2, final Runnable runnable, AnimatorListenerAdapter animatorListenerAdapter) {
        cancelAppearAnimation();
        this.mAnimationTranslationY = f * getActualHeight();
        float f2 = 1.0f;
        if (this.mAppearAnimationFraction == -1.0f) {
            if (z) {
                this.mAppearAnimationFraction = 0.0f;
                this.mAppearAnimationTranslation = this.mAnimationTranslationY;
            } else {
                this.mAppearAnimationFraction = 1.0f;
                this.mAppearAnimationTranslation = 0.0f;
            }
        }
        this.mIsAppearing = z;
        if (z) {
            this.mCurrentAppearInterpolator = this.mSlowOutFastInInterpolator;
            this.mCurrentAlphaInterpolator = Interpolators.LINEAR_OUT_SLOW_IN;
        } else {
            this.mCurrentAppearInterpolator = Interpolators.FAST_OUT_SLOW_IN;
            this.mCurrentAlphaInterpolator = this.mSlowOutLinearInInterpolator;
            f2 = 0.0f;
        }
        this.mAppearAnimator = ValueAnimator.ofFloat(this.mAppearAnimationFraction, f2);
        this.mAppearAnimator.setInterpolator(Interpolators.LINEAR);
        this.mAppearAnimator.setDuration(((float) j2) * Math.abs(this.mAppearAnimationFraction - f2));
        this.mAppearAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.statusbar.ActivatableNotificationView.11
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                ActivatableNotificationView.this.mAppearAnimationFraction = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                ActivatableNotificationView.this.updateAppearAnimationAlpha();
                ActivatableNotificationView.this.updateAppearRect();
                ActivatableNotificationView.this.invalidate();
            }
        });
        if (animatorListenerAdapter != null) {
            this.mAppearAnimator.addListener(animatorListenerAdapter);
        }
        if (j > 0) {
            updateAppearAnimationAlpha();
            updateAppearRect();
            this.mAppearAnimator.setStartDelay(j);
        }
        this.mAppearAnimator.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.statusbar.ActivatableNotificationView.12
            private boolean mWasCancelled;

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                if (runnable != null) {
                    runnable.run();
                }
                if (!this.mWasCancelled) {
                    ActivatableNotificationView.this.enableAppearDrawing(false);
                    ActivatableNotificationView.this.onAppearAnimationFinished(z);
                }
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                this.mWasCancelled = false;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
                this.mWasCancelled = true;
            }
        });
        this.mAppearAnimator.start();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void onAppearAnimationFinished(boolean z) {
    }

    private void cancelAppearAnimation() {
        if (this.mAppearAnimator != null) {
            this.mAppearAnimator.cancel();
            this.mAppearAnimator = null;
        }
    }

    public void cancelAppearDrawing() {
        cancelAppearAnimation();
        enableAppearDrawing(false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateAppearRect() {
        float width;
        float width2;
        float f;
        float f2;
        float f3 = 1.0f - this.mAppearAnimationFraction;
        float interpolation = this.mCurrentAppearInterpolator.getInterpolation(f3) * this.mAnimationTranslationY;
        this.mAppearAnimationTranslation = interpolation;
        float f4 = f3 - 0.0f;
        float interpolation2 = 1.0f - this.mCurrentAppearInterpolator.getInterpolation(Math.min(1.0f, Math.max(0.0f, f4 / 0.8f)));
        float lerp = MathUtils.lerp((!this.mIsHeadsUpAnimation || this.mIsAppearing) ? 0.05f : 0.0f, 1.0f, interpolation2) * getWidth();
        if (this.mIsHeadsUpAnimation) {
            width = MathUtils.lerp(this.mHeadsUpLocation, 0.0f, interpolation2);
            width2 = lerp + width;
        } else {
            width = (getWidth() * 0.5f) - (lerp / 2.0f);
            width2 = getWidth() - width;
        }
        float interpolation3 = this.mCurrentAppearInterpolator.getInterpolation(Math.max(0.0f, f4 / 1.0f));
        int actualHeight = getActualHeight();
        if (this.mAnimationTranslationY > 0.0f) {
            f = (actualHeight - ((this.mAnimationTranslationY * interpolation3) * 0.1f)) - interpolation;
            f2 = interpolation3 * f;
        } else {
            float f5 = actualHeight;
            float f6 = (((this.mAnimationTranslationY + f5) * interpolation3) * 0.1f) - interpolation;
            f = (f5 * (1.0f - interpolation3)) + (interpolation3 * f6);
            f2 = f6;
        }
        this.mAppearAnimationRect.set(width, f2, width2, f);
        setOutlineRect(width, f2 + this.mAppearAnimationTranslation, width2, f + this.mAppearAnimationTranslation);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateAppearAnimationAlpha() {
        setContentAlpha(this.mCurrentAlphaInterpolator.getInterpolation(Math.min(1.0f, this.mAppearAnimationFraction / 1.0f)));
    }

    private void setContentAlpha(float f) {
        int i;
        View contentView = getContentView();
        if (contentView.hasOverlappingRendering()) {
            if (f == 0.0f || f == 1.0f) {
                i = 0;
            } else {
                i = 2;
            }
            if (contentView.getLayerType() != i) {
                contentView.setLayerType(i, null);
            }
        }
        contentView.setAlpha(f);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.ExpandableOutlineView
    public void applyRoundness() {
        super.applyRoundness();
        applyBackgroundRoundness(getCurrentBackgroundRadiusTop(), getCurrentBackgroundRadiusBottom());
    }

    protected void applyBackgroundRoundness(float f, float f2) {
        this.mBackgroundDimmed.setRoundness(f, f2);
        this.mBackgroundNormal.setRoundness(f, f2);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.ExpandableOutlineView
    public void setBackgroundTop(int i) {
        this.mBackgroundDimmed.setBackgroundTop(i);
        this.mBackgroundNormal.setBackgroundTop(i);
    }

    public int calculateBgColor() {
        return calculateBgColor(true, true);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.ExpandableOutlineView
    public boolean childNeedsClipping(View view) {
        if ((view instanceof NotificationBackgroundView) && isClippingNeeded()) {
            return true;
        }
        return super.childNeedsClipping(view);
    }

    private int calculateBgColor(boolean z, boolean z2) {
        if (z && this.mDark) {
            return getContext().getColor(R.color.notification_material_background_dark_color);
        }
        if (z2 && this.mOverrideTint != 0) {
            return NotificationUtils.interpolateColors(calculateBgColor(z, false), this.mOverrideTint, this.mOverrideAmount);
        }
        if (z && this.mBgTint != 0) {
            return this.mBgTint;
        }
        return this.mNormalColor;
    }

    protected int getRippleColor() {
        if (this.mBgTint != 0) {
            return this.mTintedRippleColor;
        }
        return this.mNormalRippleColor;
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

    public boolean isDrawingAppearAnimation() {
        return this.mDrawingAppearAnimation;
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

    public void setOnActivatedListener(OnActivatedListener onActivatedListener) {
        this.mOnActivatedListener = onActivatedListener;
    }

    @Override // com.android.systemui.statusbar.ExpandableView
    public float getShadowAlpha() {
        return this.mShadowAlpha;
    }

    @Override // com.android.systemui.statusbar.ExpandableView
    public void setShadowAlpha(float f) {
        if (f != this.mShadowAlpha) {
            this.mShadowAlpha = f;
            updateOutlineAlpha();
        }
    }

    @Override // com.android.systemui.statusbar.ExpandableView
    public void setFakeShadowIntensity(float f, float f2, int i, int i2) {
        boolean z = this.mShadowHidden;
        this.mShadowHidden = f == 0.0f;
        if (!this.mShadowHidden || !z) {
            this.mFakeShadow.setFakeShadowTranslationZ(f * (getTranslationZ() + 0.1f), f2, i, i2);
        }
    }

    public int getBackgroundColorWithoutTint() {
        return calculateBgColor(false, false);
    }

    public boolean isPinned() {
        return false;
    }

    public boolean isHeadsUpAnimatingAway() {
        return false;
    }
}
