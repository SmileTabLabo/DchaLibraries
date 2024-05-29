package com.android.systemui.recents.views;

import android.content.Context;
import android.view.View;
import com.android.systemui.Interpolators;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.RecentsActivity;
import com.android.systemui.recents.events.activity.ConfigurationChangedEvent;
import com.android.systemui.recents.events.activity.DismissRecentsToHomeAnimationStarted;
import com.android.systemui.recents.events.activity.EnterRecentsWindowAnimationCompletedEvent;
import com.android.systemui.recents.events.activity.MultiWindowStateChangedEvent;
import com.android.systemui.recents.events.ui.DismissAllTaskViewsEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragEndCancelledEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragEndEvent;
import com.android.systemui.recents.model.TaskStack;
/* loaded from: a.zip:com/android/systemui/recents/views/SystemBarScrimViews.class */
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
        this.mNavBarScrimView = recentsActivity.findViewById(2131886609);
        this.mNavBarScrimView.forceHasOverlappingRendering(false);
        this.mNavBarScrimEnterDuration = recentsActivity.getResources().getInteger(2131755065);
        this.mHasNavBarScrim = Recents.getSystemServices().hasTransposedNavigationBar();
        this.mHasDockedTasks = Recents.getSystemServices().hasDockedTask();
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

    private void animateScrimToCurrentNavBarState(boolean z) {
        boolean isNavBarScrimRequired = isNavBarScrimRequired(z);
        if (this.mHasNavBarScrim != isNavBarScrimRequired) {
            animateNavBarScrimVisibility(isNavBarScrimRequired, isNavBarScrimRequired ? createBoundsAnimation(150) : AnimationProps.IMMEDIATE);
        }
        this.mHasNavBarScrim = isNavBarScrimRequired;
    }

    private AnimationProps createBoundsAnimation(int i) {
        return new AnimationProps().setDuration(6, i).setInterpolator(6, Interpolators.FAST_OUT_SLOW_IN);
    }

    private boolean isNavBarScrimRequired(boolean z) {
        boolean z2 = false;
        if (z) {
            if (this.mHasTransposedNavBar) {
                z2 = false;
            } else {
                z2 = false;
                if (!this.mHasDockedTasks) {
                    z2 = true;
                }
            }
        }
        return z2;
    }

    private void prepareEnterRecentsAnimation(boolean z, boolean z2) {
        this.mHasNavBarScrim = z;
        this.mShouldAnimateNavBarScrim = z2;
        this.mNavBarScrimView.setVisibility((!this.mHasNavBarScrim || this.mShouldAnimateNavBarScrim) ? 4 : 0);
    }

    public final void onBusEvent(ConfigurationChangedEvent configurationChangedEvent) {
        if (configurationChangedEvent.fromDeviceOrientationChange) {
            this.mHasNavBarScrim = Recents.getSystemServices().hasTransposedNavigationBar();
        }
        animateScrimToCurrentNavBarState(configurationChangedEvent.hasStackTasks);
    }

    public final void onBusEvent(DismissRecentsToHomeAnimationStarted dismissRecentsToHomeAnimationStarted) {
        if (this.mHasNavBarScrim) {
            animateNavBarScrimVisibility(false, createBoundsAnimation(200));
        }
    }

    public final void onBusEvent(EnterRecentsWindowAnimationCompletedEvent enterRecentsWindowAnimationCompletedEvent) {
        if (this.mHasNavBarScrim) {
            animateNavBarScrimVisibility(true, this.mShouldAnimateNavBarScrim ? new AnimationProps().setDuration(6, this.mNavBarScrimEnterDuration).setInterpolator(6, Interpolators.DECELERATE_QUINT) : AnimationProps.IMMEDIATE);
        }
    }

    public final void onBusEvent(MultiWindowStateChangedEvent multiWindowStateChangedEvent) {
        boolean z = false;
        this.mHasDockedTasks = multiWindowStateChangedEvent.inMultiWindow;
        if (multiWindowStateChangedEvent.stack.getStackTaskCount() > 0) {
            z = true;
        }
        animateScrimToCurrentNavBarState(z);
    }

    public final void onBusEvent(DismissAllTaskViewsEvent dismissAllTaskViewsEvent) {
        if (this.mHasNavBarScrim) {
            animateNavBarScrimVisibility(false, createBoundsAnimation(200));
        }
    }

    public final void onBusEvent(DragEndCancelledEvent dragEndCancelledEvent) {
        boolean z = false;
        if (dragEndCancelledEvent.stack.getStackTaskCount() > 0) {
            z = true;
        }
        animateScrimToCurrentNavBarState(z);
    }

    public final void onBusEvent(DragEndEvent dragEndEvent) {
        if (dragEndEvent.dropTarget instanceof TaskStack.DockState) {
            animateScrimToCurrentNavBarState(false);
        }
    }

    public void updateNavBarScrim(boolean z, boolean z2, AnimationProps animationProps) {
        prepareEnterRecentsAnimation(isNavBarScrimRequired(z2), z);
        if (!z || animationProps == null) {
            return;
        }
        animateNavBarScrimVisibility(true, animationProps);
    }
}
