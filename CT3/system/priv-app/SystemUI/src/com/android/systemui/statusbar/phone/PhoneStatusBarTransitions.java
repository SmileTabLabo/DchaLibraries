package com.android.systemui.statusbar.phone;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import com.android.keyguard.R$id;
/* loaded from: a.zip:com/android/systemui/statusbar/phone/PhoneStatusBarTransitions.class */
public final class PhoneStatusBarTransitions extends BarTransitions {
    private View mBattery;
    private View mClock;
    private Animator mCurrentAnimation;
    private final float mIconAlphaWhenOpaque;
    private View mLeftSide;
    private View mSignalCluster;
    private View mStatusIcons;
    private final PhoneStatusBarView mView;

    public PhoneStatusBarTransitions(PhoneStatusBarView phoneStatusBarView) {
        super(phoneStatusBarView, 2130838335);
        this.mView = phoneStatusBarView;
        this.mIconAlphaWhenOpaque = this.mView.getContext().getResources().getFraction(2131689795, 1, 1);
    }

    private void applyMode(int i, boolean z) {
        if (this.mLeftSide == null) {
            return;
        }
        float nonBatteryClockAlphaFor = getNonBatteryClockAlphaFor(i);
        float batteryClockAlpha = getBatteryClockAlpha(i);
        if (this.mCurrentAnimation != null) {
            this.mCurrentAnimation.cancel();
        }
        if (!z) {
            this.mLeftSide.setAlpha(nonBatteryClockAlphaFor);
            this.mStatusIcons.setAlpha(nonBatteryClockAlphaFor);
            this.mSignalCluster.setAlpha(nonBatteryClockAlphaFor);
            this.mBattery.setAlpha(batteryClockAlpha);
            this.mClock.setAlpha(batteryClockAlpha);
            return;
        }
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animateTransitionTo(this.mLeftSide, nonBatteryClockAlphaFor), animateTransitionTo(this.mStatusIcons, nonBatteryClockAlphaFor), animateTransitionTo(this.mSignalCluster, nonBatteryClockAlphaFor), animateTransitionTo(this.mBattery, batteryClockAlpha), animateTransitionTo(this.mClock, batteryClockAlpha));
        if (isLightsOut(i)) {
            animatorSet.setDuration(750L);
        }
        animatorSet.start();
        this.mCurrentAnimation = animatorSet;
    }

    private float getBatteryClockAlpha(int i) {
        return isLightsOut(i) ? 0.5f : getNonBatteryClockAlphaFor(i);
    }

    private float getNonBatteryClockAlphaFor(int i) {
        return isLightsOut(i) ? 0.0f : !isOpaque(i) ? 1.0f : this.mIconAlphaWhenOpaque;
    }

    private boolean isOpaque(int i) {
        boolean z = true;
        if (i == 1 || i == 2 || i == 4) {
            z = false;
        } else if (i == 6) {
            z = false;
        }
        return z;
    }

    public ObjectAnimator animateTransitionTo(View view, float f) {
        return ObjectAnimator.ofFloat(view, "alpha", view.getAlpha(), f);
    }

    public void init() {
        this.mLeftSide = this.mView.findViewById(2131886677);
        this.mStatusIcons = this.mView.findViewById(2131886719);
        this.mSignalCluster = this.mView.findViewById(2131886656);
        this.mBattery = this.mView.findViewById(2131886720);
        this.mClock = this.mView.findViewById(R$id.clock);
        applyModeBackground(-1, getMode(), false);
        applyMode(getMode(), false);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.BarTransitions
    public void onTransition(int i, int i2, boolean z) {
        super.onTransition(i, i2, z);
        applyMode(i2, z);
    }
}
