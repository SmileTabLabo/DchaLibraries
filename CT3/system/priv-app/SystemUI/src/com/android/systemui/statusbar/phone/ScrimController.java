package com.android.systemui.statusbar.phone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.ScrimView;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import com.android.systemui.statusbar.stack.StackStateAnimator;
/* loaded from: a.zip:com/android/systemui/statusbar/phone/ScrimController.class */
public class ScrimController implements ViewTreeObserver.OnPreDrawListener, HeadsUpManager.OnHeadsUpChangedListener {
    public static final Interpolator KEYGUARD_FADE_OUT_INTERPOLATOR = new PathInterpolator(0.0f, 0.0f, 0.7f, 1.0f);
    private boolean mAnimateChange;
    private boolean mAnimateKeyguardFadingOut;
    private long mAnimationDelay;
    protected boolean mBouncerShowing;
    private float mCurrentBehindAlpha;
    private float mCurrentInFrontAlpha;
    private boolean mDarkenWhileDragging;
    private boolean mDontAnimateBouncerChanges;
    private float mDozeBehindAlpha;
    private float mDozeInFrontAlpha;
    private boolean mDozing;
    private View mDraggedHeadsUpView;
    private boolean mExpanding;
    private boolean mForceHideScrims;
    private float mFraction;
    private final View mHeadsUpScrim;
    private ValueAnimator mKeyguardFadeoutAnimation;
    private boolean mKeyguardFadingOutInProgress;
    protected boolean mKeyguardShowing;
    private Runnable mOnAnimationFinished;
    private int mPinnedHeadsUpCount;
    protected final ScrimView mScrimBehind;
    private final ScrimView mScrimInFront;
    private boolean mSkipFirstFrame;
    private float mTopHeadsUpDragAmount;
    private final UnlockMethodCache mUnlockMethodCache;
    private boolean mUpdatePending;
    private boolean mWakeAndUnlocking;
    private float mScrimBehindAlpha = 0.62f;
    private float mScrimBehindAlphaKeyguard = 0.45f;
    private float mScrimBehindAlphaUnlocking = 0.2f;
    private long mDurationOverride = -1;
    private final Interpolator mInterpolator = new DecelerateInterpolator();
    private float mCurrentHeadsUpAlpha = 1.0f;

    public ScrimController(ScrimView scrimView, ScrimView scrimView2, View view) {
        this.mScrimBehind = scrimView;
        this.mScrimInFront = scrimView2;
        this.mHeadsUpScrim = view;
        this.mUnlockMethodCache = UnlockMethodCache.getInstance(scrimView.getContext());
        updateHeadsUpScrim(false);
    }

    private float calculateHeadsUpAlpha() {
        return (this.mPinnedHeadsUpCount >= 2 ? 1.0f : this.mPinnedHeadsUpCount == 0 ? 0.0f : 1.0f - this.mTopHeadsUpDragAmount) * Math.max(1.0f - this.mFraction, 0.0f);
    }

    private void endAnimateKeyguardFadingOut(boolean z) {
        this.mAnimateKeyguardFadingOut = false;
        if (z || !(isAnimating(this.mScrimInFront) || isAnimating(this.mScrimBehind))) {
            if (this.mOnAnimationFinished != null) {
                this.mOnAnimationFinished.run();
                this.mOnAnimationFinished = null;
            }
            this.mKeyguardFadingOutInProgress = false;
        }
    }

    private float getCurrentScrimAlpha(View view) {
        return view == this.mScrimBehind ? this.mCurrentBehindAlpha : view == this.mScrimInFront ? this.mCurrentInFrontAlpha : this.mCurrentHeadsUpAlpha;
    }

    private float getDozeAlpha(View view) {
        return view == this.mScrimBehind ? this.mDozeBehindAlpha : this.mDozeInFrontAlpha;
    }

    private Interpolator getInterpolator() {
        return this.mAnimateKeyguardFadingOut ? KEYGUARD_FADE_OUT_INTERPOLATOR : this.mInterpolator;
    }

    private boolean isAnimating(View view) {
        return view.getTag(2131886128) != null;
    }

    private void scheduleUpdate() {
        if (this.mUpdatePending) {
            return;
        }
        this.mScrimBehind.invalidate();
        this.mScrimBehind.getViewTreeObserver().addOnPreDrawListener(this);
        this.mUpdatePending = true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setCurrentScrimAlpha(View view, float f) {
        if (view == this.mScrimBehind) {
            this.mCurrentBehindAlpha = f;
        } else if (view == this.mScrimInFront) {
            this.mCurrentInFrontAlpha = f;
        } else {
            this.mCurrentHeadsUpAlpha = Math.max(0.0f, Math.min(1.0f, f));
        }
    }

    private void setScrimBehindColor(float f) {
        setScrimColor(this.mScrimBehind, f);
    }

    private void setScrimColor(View view, float f) {
        updateScrim(this.mAnimateChange, view, f, getCurrentScrimAlpha(view));
    }

    private void setScrimInFrontColor(float f) {
        boolean z = false;
        setScrimColor(this.mScrimInFront, f);
        if (f == 0.0f) {
            this.mScrimInFront.setClickable(false);
            return;
        }
        ScrimView scrimView = this.mScrimInFront;
        if (!this.mDozing) {
            z = true;
        }
        scrimView.setClickable(z);
    }

    private void startScrimAnimation(View view, float f) {
        ValueAnimator ofFloat = ValueAnimator.ofFloat(getCurrentScrimAlpha(view), f);
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(this, view) { // from class: com.android.systemui.statusbar.phone.ScrimController.1
            final ScrimController this$0;
            final View val$scrim;

            {
                this.this$0 = this;
                this.val$scrim = view;
            }

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                this.this$0.setCurrentScrimAlpha(this.val$scrim, ((Float) valueAnimator.getAnimatedValue()).floatValue());
                this.this$0.updateScrimColor(this.val$scrim);
            }
        });
        ofFloat.setInterpolator(getInterpolator());
        ofFloat.setStartDelay(this.mAnimationDelay);
        ofFloat.setDuration(this.mDurationOverride != -1 ? this.mDurationOverride : 220L);
        ofFloat.addListener(new AnimatorListenerAdapter(this, view) { // from class: com.android.systemui.statusbar.phone.ScrimController.2
            final ScrimController this$0;
            final View val$scrim;

            {
                this.this$0 = this;
                this.val$scrim = view;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                if (this.this$0.mOnAnimationFinished != null) {
                    this.this$0.mOnAnimationFinished.run();
                    this.this$0.mOnAnimationFinished = null;
                }
                if (this.this$0.mKeyguardFadingOutInProgress) {
                    this.this$0.mKeyguardFadeoutAnimation = null;
                    this.this$0.mKeyguardFadingOutInProgress = false;
                }
                this.val$scrim.setTag(2131886128, null);
                this.val$scrim.setTag(2131886129, null);
            }
        });
        ofFloat.start();
        if (this.mAnimateKeyguardFadingOut) {
            this.mKeyguardFadingOutInProgress = true;
            this.mKeyguardFadeoutAnimation = ofFloat;
        }
        if (this.mSkipFirstFrame) {
            ofFloat.setCurrentPlayTime(16L);
        }
        view.setTag(2131886128, ofFloat);
        view.setTag(2131886129, Float.valueOf(f));
    }

    private void updateHeadsUpScrim(boolean z) {
        updateScrim(z, this.mHeadsUpScrim, calculateHeadsUpAlpha(), this.mCurrentHeadsUpAlpha);
    }

    private void updateScrim(boolean z, View view, float f, float f2) {
        if (this.mKeyguardFadingOutInProgress) {
            return;
        }
        ValueAnimator valueAnimator = (ValueAnimator) StackStateAnimator.getChildTag(view, 2131886128);
        float f3 = -1.0f;
        if (valueAnimator != null) {
            if (z || f == f2) {
                valueAnimator.cancel();
                f3 = -1.0f;
            } else {
                f3 = ((Float) StackStateAnimator.getChildTag(view, 2131886131)).floatValue();
            }
        }
        if (f == f2 || f == f3) {
            return;
        }
        if (z) {
            startScrimAnimation(view, f);
            view.setTag(2131886130, Float.valueOf(f2));
            view.setTag(2131886131, Float.valueOf(f));
        } else if (valueAnimator == null) {
            setCurrentScrimAlpha(view, f);
            updateScrimColor(view);
        } else {
            float floatValue = ((Float) StackStateAnimator.getChildTag(view, 2131886130)).floatValue();
            float floatValue2 = ((Float) StackStateAnimator.getChildTag(view, 2131886131)).floatValue();
            PropertyValuesHolder[] values = valueAnimator.getValues();
            float max = Math.max(0.0f, Math.min(1.0f, floatValue + (f - floatValue2)));
            values[0].setFloatValues(max, f);
            view.setTag(2131886130, Float.valueOf(max));
            view.setTag(2131886131, Float.valueOf(f));
            valueAnimator.setCurrentPlayTime(valueAnimator.getCurrentPlayTime());
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateScrimColor(View view) {
        float currentScrimAlpha = getCurrentScrimAlpha(view);
        if (view instanceof ScrimView) {
            ((ScrimView) view).setScrimColor(Color.argb((int) (255.0f * Math.max(0.0f, Math.min(1.0f, 1.0f - ((1.0f - currentScrimAlpha) * (1.0f - getDozeAlpha(view)))))), 0, 0, 0));
        } else {
            view.setAlpha(currentScrimAlpha);
        }
    }

    private void updateScrimKeyguard() {
        if (this.mExpanding && this.mDarkenWhileDragging) {
            float max = Math.max(0.0f, Math.min(this.mFraction, 1.0f));
            setScrimInFrontColor(((float) Math.pow(1.0f - max, 0.800000011920929d)) * 0.75f);
            setScrimBehindColor(this.mScrimBehindAlphaKeyguard * ((float) Math.pow(max, 0.800000011920929d)));
        } else if (this.mBouncerShowing) {
            setScrimInFrontColor(0.75f);
            setScrimBehindColor(0.0f);
        } else {
            float max2 = Math.max(0.0f, Math.min(this.mFraction, 1.0f));
            setScrimInFrontColor(0.0f);
            setScrimBehindColor(((this.mScrimBehindAlphaKeyguard - this.mScrimBehindAlphaUnlocking) * max2) + this.mScrimBehindAlphaUnlocking);
        }
    }

    private void updateScrimNormal() {
        float f = (1.2f * this.mFraction) - 0.2f;
        if (f <= 0.0f) {
            setScrimBehindColor(0.0f);
            return;
        }
        setScrimBehindColor(this.mScrimBehindAlpha * ((float) (1.0d - ((1.0d - Math.cos(Math.pow(1.0f - f, 2.0d) * 3.141590118408203d)) * 0.5d))));
    }

    public void abortKeyguardFadingOut() {
        if (this.mAnimateKeyguardFadingOut) {
            endAnimateKeyguardFadingOut(true);
        }
    }

    public void animateGoingToFullShade(long j, long j2) {
        this.mDurationOverride = j2;
        this.mAnimationDelay = j;
        this.mAnimateChange = true;
        scheduleUpdate();
    }

    public void animateKeyguardFadingOut(long j, long j2, Runnable runnable, boolean z) {
        this.mWakeAndUnlocking = false;
        this.mAnimateKeyguardFadingOut = true;
        this.mDurationOverride = j2;
        this.mAnimationDelay = j;
        this.mAnimateChange = true;
        this.mSkipFirstFrame = z;
        this.mOnAnimationFinished = runnable;
        scheduleUpdate();
        onPreDraw();
    }

    public void animateNextChange() {
        this.mAnimateChange = true;
    }

    public void dontAnimateBouncerChangesUntilNextFrame() {
        this.mDontAnimateBouncerChanges = true;
    }

    public void forceHideScrims(boolean z) {
        this.mForceHideScrims = z;
        this.mAnimateChange = false;
        scheduleUpdate();
    }

    public float getDozeBehindAlpha() {
        return this.mDozeBehindAlpha;
    }

    public float getDozeInFrontAlpha() {
        return this.mDozeInFrontAlpha;
    }

    public int getScrimBehindColor() {
        return this.mScrimBehind.getScrimColorWithAlpha();
    }

    public void onDensityOrFontScaleChanged() {
        ViewGroup.LayoutParams layoutParams = this.mHeadsUpScrim.getLayoutParams();
        layoutParams.height = this.mHeadsUpScrim.getResources().getDimensionPixelSize(2131689887);
        this.mHeadsUpScrim.setLayoutParams(layoutParams);
    }

    public void onExpandingFinished() {
        this.mExpanding = false;
    }

    @Override // com.android.systemui.statusbar.policy.HeadsUpManager.OnHeadsUpChangedListener
    public void onHeadsUpPinned(ExpandableNotificationRow expandableNotificationRow) {
        this.mPinnedHeadsUpCount++;
        updateHeadsUpScrim(true);
    }

    @Override // com.android.systemui.statusbar.policy.HeadsUpManager.OnHeadsUpChangedListener
    public void onHeadsUpPinnedModeChanged(boolean z) {
    }

    @Override // com.android.systemui.statusbar.policy.HeadsUpManager.OnHeadsUpChangedListener
    public void onHeadsUpStateChanged(NotificationData.Entry entry, boolean z) {
    }

    @Override // com.android.systemui.statusbar.policy.HeadsUpManager.OnHeadsUpChangedListener
    public void onHeadsUpUnPinned(ExpandableNotificationRow expandableNotificationRow) {
        this.mPinnedHeadsUpCount--;
        if (expandableNotificationRow == this.mDraggedHeadsUpView) {
            this.mDraggedHeadsUpView = null;
            this.mTopHeadsUpDragAmount = 0.0f;
        }
        updateHeadsUpScrim(true);
    }

    @Override // android.view.ViewTreeObserver.OnPreDrawListener
    public boolean onPreDraw() {
        this.mScrimBehind.getViewTreeObserver().removeOnPreDrawListener(this);
        this.mUpdatePending = false;
        if (this.mDontAnimateBouncerChanges) {
            this.mDontAnimateBouncerChanges = false;
        }
        updateScrims();
        this.mDurationOverride = -1L;
        this.mAnimationDelay = 0L;
        this.mSkipFirstFrame = false;
        endAnimateKeyguardFadingOut(false);
        return true;
    }

    public void onTrackingStarted() {
        boolean z = true;
        this.mExpanding = true;
        if (this.mUnlockMethodCache.canSkipBouncer()) {
            z = false;
        }
        this.mDarkenWhileDragging = z;
    }

    public void setBouncerShowing(boolean z) {
        this.mBouncerShowing = z;
        boolean z2 = false;
        if (!this.mExpanding) {
            z2 = !this.mDontAnimateBouncerChanges;
        }
        this.mAnimateChange = z2;
        scheduleUpdate();
    }

    public void setDozeBehindAlpha(float f) {
        this.mDozeBehindAlpha = f;
        updateScrimColor(this.mScrimBehind);
    }

    public void setDozeInFrontAlpha(float f) {
        this.mDozeInFrontAlpha = f;
        updateScrimColor(this.mScrimInFront);
    }

    public void setDozing(boolean z) {
        if (this.mDozing != z) {
            this.mDozing = z;
            scheduleUpdate();
        }
    }

    public void setDrawBehindAsSrc(boolean z) {
        this.mScrimBehind.setDrawAsSrc(z);
    }

    public void setExcludedBackgroundArea(Rect rect) {
        this.mScrimBehind.setExcludedArea(rect);
    }

    public void setKeyguardShowing(boolean z) {
        this.mKeyguardShowing = z;
        scheduleUpdate();
    }

    public void setPanelExpansion(float f) {
        if (this.mFraction != f) {
            this.mFraction = f;
            scheduleUpdate();
            if (this.mPinnedHeadsUpCount != 0) {
                updateHeadsUpScrim(false);
            }
            if (this.mKeyguardFadeoutAnimation != null) {
                this.mKeyguardFadeoutAnimation.cancel();
            }
        }
    }

    public void setScrimBehindChangeRunnable(Runnable runnable) {
        this.mScrimBehind.setChangeRunnable(runnable);
    }

    public void setTopHeadsUpDragAmount(View view, float f) {
        this.mTopHeadsUpDragAmount = f;
        this.mDraggedHeadsUpView = view;
        updateHeadsUpScrim(false);
    }

    public void setWakeAndUnlocking() {
        this.mWakeAndUnlocking = true;
        scheduleUpdate();
    }

    protected void updateScrims() {
        if (this.mAnimateKeyguardFadingOut || this.mForceHideScrims) {
            setScrimInFrontColor(0.0f);
            setScrimBehindColor(0.0f);
        } else if (this.mWakeAndUnlocking) {
            if (this.mDozing) {
                setScrimInFrontColor(0.0f);
                setScrimBehindColor(1.0f);
            } else {
                setScrimInFrontColor(1.0f);
                setScrimBehindColor(0.0f);
            }
        } else if (this.mKeyguardShowing || this.mBouncerShowing) {
            updateScrimKeyguard();
        } else {
            updateScrimNormal();
            setScrimInFrontColor(0.0f);
        }
        this.mAnimateChange = false;
    }
}
