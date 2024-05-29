package com.android.systemui.recents.views;

import android.content.Context;
import android.view.View;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.RecentsActivity;
import com.android.systemui.recents.events.activity.ConfigurationChangedEvent;
import com.android.systemui.recents.events.activity.DismissRecentsToHomeAnimationStarted;
import com.android.systemui.recents.events.activity.EnterRecentsWindowAnimationCompletedEvent;
import com.android.systemui.recents.events.activity.MultiWindowStateChangedEvent;
import com.android.systemui.recents.events.ui.DismissAllTaskViewsEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragEndCancelledEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragEndEvent;
import com.android.systemui.shared.recents.utilities.AnimationProps;
/* loaded from: classes.dex */
public class SystemBarScrimViews {
    private Context mContext;
    private boolean mHasDockedTasks;
    private boolean mHasNavBarScrim;
    private boolean mHasTransposedNavBar;
    private int mNavBarScrimEnterDuration;
    private View mNavBarScrimView;
    private boolean mShouldAnimateNavBarScrim;

    public SystemBarScrimViews(RecentsActivity recentsActivity) {
        this.mContext = recentsActivity;
        this.mNavBarScrimView = recentsActivity.findViewById(R.id.nav_bar_scrim);
        this.mNavBarScrimView.forceHasOverlappingRendering(false);
        this.mNavBarScrimEnterDuration = recentsActivity.getResources().getInteger(R.integer.recents_nav_bar_scrim_enter_duration);
        this.mHasNavBarScrim = Recents.getSystemServices().hasTransposedNavigationBar();
        this.mHasDockedTasks = Recents.getSystemServices().hasDockedTask();
    }

    public void updateNavBarScrim(boolean z, boolean z2, AnimationProps animationProps) {
        prepareEnterRecentsAnimation(isNavBarScrimRequired(z2), z);
        if (z && animationProps != null) {
            animateNavBarScrimVisibility(true, animationProps);
        }
    }

    private void prepareEnterRecentsAnimation(boolean z, boolean z2) {
        this.mHasNavBarScrim = z;
        this.mShouldAnimateNavBarScrim = z2;
        this.mNavBarScrimView.setVisibility((!this.mHasNavBarScrim || this.mShouldAnimateNavBarScrim) ? 4 : 0);
    }

    private void animateNavBarScrimVisibility(boolean z, AnimationProps animationProps) {
        int i = 0;
        if (z) {
            this.mNavBarScrimView.setVisibility(0);
            this.mNavBarScrimView.setTranslationY(this.mNavBarScrimView.getMeasuredHeight());
        } else {
            i = this.mNavBarScrimView.getMeasuredHeight();
        }
        if (animationProps != AnimationProps.IMMEDIATE) {
            this.mNavBarScrimView.animate().translationY(i).setDuration(animationProps.getDuration(6)).setInterpolator(animationProps.getInterpolator(6)).start();
        } else {
            this.mNavBarScrimView.setTranslationY(i);
        }
    }

    private boolean isNavBarScrimRequired(boolean z) {
        return (!z || this.mHasTransposedNavBar || this.mHasDockedTasks) ? false : true;
    }

    public final void onBusEvent(EnterRecentsWindowAnimationCompletedEvent enterRecentsWindowAnimationCompletedEvent) {
        AnimationProps animationProps;
        if (this.mHasNavBarScrim) {
            if (this.mShouldAnimateNavBarScrim) {
                animationProps = new AnimationProps().setDuration(6, this.mNavBarScrimEnterDuration).setInterpolator(6, Interpolators.DECELERATE_QUINT);
            } else {
                animationProps = AnimationProps.IMMEDIATE;
            }
            animateNavBarScrimVisibility(true, animationProps);
        }
    }

    public final void onBusEvent(DismissRecentsToHomeAnimationStarted dismissRecentsToHomeAnimationStarted) {
        if (this.mHasNavBarScrim) {
            animateNavBarScrimVisibility(false, createBoundsAnimation(200));
        }
    }

    public final void onBusEvent(DismissAllTaskViewsEvent dismissAllTaskViewsEvent) {
        if (this.mHasNavBarScrim) {
            animateNavBarScrimVisibility(false, createBoundsAnimation(200));
        }
    }

    public final void onBusEvent(ConfigurationChangedEvent configurationChangedEvent) {
        if (configurationChangedEvent.fromDeviceOrientationChange) {
            this.mHasNavBarScrim = Recents.getSystemServices().hasTransposedNavigationBar();
        }
        animateScrimToCurrentNavBarState(configurationChangedEvent.hasStackTasks);
    }

    public final void onBusEvent(MultiWindowStateChangedEvent multiWindowStateChangedEvent) {
        this.mHasDockedTasks = multiWindowStateChangedEvent.inMultiWindow;
        animateScrimToCurrentNavBarState(multiWindowStateChangedEvent.stack.getTaskCount() > 0);
    }

    public final void onBusEvent(DragEndEvent dragEndEvent) {
        if (dragEndEvent.dropTarget instanceof DockState) {
            animateScrimToCurrentNavBarState(false);
        }
    }

    public final void onBusEvent(DragEndCancelledEvent dragEndCancelledEvent) {
        animateScrimToCurrentNavBarState(dragEndCancelledEvent.stack.getTaskCount() > 0);
    }

    private void animateScrimToCurrentNavBarState(boolean z) {
        AnimationProps animationProps;
        boolean isNavBarScrimRequired = isNavBarScrimRequired(z);
        if (this.mHasNavBarScrim != isNavBarScrimRequired) {
            if (isNavBarScrimRequired) {
                animationProps = createBoundsAnimation(150);
            } else {
                animationProps = AnimationProps.IMMEDIATE;
            }
            animateNavBarScrimVisibility(isNavBarScrimRequired, animationProps);
        }
        this.mHasNavBarScrim = isNavBarScrimRequired;
    }

    private AnimationProps createBoundsAnimation(int i) {
        return new AnimationProps().setDuration(6, i).setInterpolator(6, Interpolators.FAST_OUT_SLOW_IN);
    }
}
