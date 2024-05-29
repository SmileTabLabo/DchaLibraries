package com.android.systemui.statusbar.phone;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AnimationUtils;
import com.android.keyguard.AlphaOptimizedImageButton;
import com.android.systemui.Interpolators;
/* loaded from: a.zip:com/android/systemui/statusbar/phone/SettingsButton.class */
public class SettingsButton extends AlphaOptimizedImageButton {
    private ObjectAnimator mAnimator;
    private final Runnable mLongPressCallback;
    private float mSlop;
    private boolean mUpToSpeed;

    public SettingsButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mLongPressCallback = new Runnable(this) { // from class: com.android.systemui.statusbar.phone.SettingsButton.1
            final SettingsButton this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.startAccelSpin();
            }
        };
        this.mSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    private void cancelAnimation() {
        if (this.mAnimator != null) {
            this.mAnimator.removeAllListeners();
            this.mAnimator.cancel();
            this.mAnimator = null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void cancelLongClick() {
        cancelAnimation();
        this.mUpToSpeed = false;
    }

    private void startExitAnimation() {
        animate().translationX(((View) getParent().getParent()).getWidth() - getX()).alpha(0.0f).setDuration(350L).setInterpolator(AnimationUtils.loadInterpolator(this.mContext, 17563650)).setListener(new Animator.AnimatorListener(this) { // from class: com.android.systemui.statusbar.phone.SettingsButton.2
            final SettingsButton this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                this.this$0.setAlpha(1.0f);
                this.this$0.setTranslationX(0.0f);
                this.this$0.cancelLongClick();
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationRepeat(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
            }
        }).start();
    }

    public boolean isAnimating() {
        return this.mAnimator != null ? this.mAnimator.isRunning() : false;
    }

    public boolean isTunerClick() {
        return this.mUpToSpeed;
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        switch (motionEvent.getActionMasked()) {
            case 1:
                if (!this.mUpToSpeed) {
                    cancelLongClick();
                    break;
                } else {
                    startExitAnimation();
                    break;
                }
            case 2:
                float x = motionEvent.getX();
                float y = motionEvent.getY();
                if (x < (-this.mSlop) || y < (-this.mSlop) || x > getWidth() + this.mSlop || y > getHeight() + this.mSlop) {
                    cancelLongClick();
                    break;
                }
                break;
            case 3:
                cancelLongClick();
                break;
        }
        return super.onTouchEvent(motionEvent);
    }

    protected void startAccelSpin() {
        cancelAnimation();
        this.mAnimator = ObjectAnimator.ofFloat(this, View.ROTATION, 0.0f, 360.0f);
        this.mAnimator.setInterpolator(AnimationUtils.loadInterpolator(this.mContext, 17563648));
        this.mAnimator.setDuration(750L);
        this.mAnimator.addListener(new Animator.AnimatorListener(this) { // from class: com.android.systemui.statusbar.phone.SettingsButton.3
            final SettingsButton this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                this.this$0.startContinuousSpin();
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationRepeat(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
            }
        });
        this.mAnimator.start();
    }

    protected void startContinuousSpin() {
        cancelAnimation();
        performHapticFeedback(0);
        this.mUpToSpeed = true;
        this.mAnimator = ObjectAnimator.ofFloat(this, View.ROTATION, 0.0f, 360.0f);
        this.mAnimator.setInterpolator(Interpolators.LINEAR);
        this.mAnimator.setDuration(375L);
        this.mAnimator.setRepeatCount(-1);
        this.mAnimator.start();
    }
}
