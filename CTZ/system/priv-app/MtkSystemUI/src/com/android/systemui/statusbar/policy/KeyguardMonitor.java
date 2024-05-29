package com.android.systemui.statusbar.policy;
/* loaded from: classes.dex */
public interface KeyguardMonitor extends CallbackController<Callback> {

    /* loaded from: classes.dex */
    public interface Callback {
        void onKeyguardShowingChanged();
    }

    boolean canSkipBouncer();

    long getKeyguardFadingAwayDelay();

    long getKeyguardFadingAwayDuration();

    boolean isKeyguardFadingAway();

    boolean isKeyguardGoingAway();

    boolean isOccluded();

    boolean isSecure();

    boolean isShowing();
}
