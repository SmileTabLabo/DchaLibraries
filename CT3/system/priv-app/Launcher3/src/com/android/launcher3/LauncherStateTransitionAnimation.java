package com.android.launcher3;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import com.android.launcher3.Launcher;
import com.android.launcher3.Workspace;
import com.android.launcher3.allapps.AllAppsContainerView;
import com.android.launcher3.util.UiThreadCircularReveal;
import com.android.launcher3.widget.WidgetsContainerView;
import java.util.HashMap;
/* loaded from: a.zip:com/android/launcher3/LauncherStateTransitionAnimation.class */
public class LauncherStateTransitionAnimation {
    AnimatorSet mCurrentAnimation;
    Launcher mLauncher;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/launcher3/LauncherStateTransitionAnimation$PrivateTransitionCallbacks.class */
    public static class PrivateTransitionCallbacks {
        private final float materialRevealViewFinalAlpha;

        PrivateTransitionCallbacks(float f) {
            this.materialRevealViewFinalAlpha = f;
        }

        AnimatorListenerAdapter getMaterialRevealViewAnimatorListener(View view, View view2) {
            return null;
        }

        float getMaterialRevealViewStartFinalRadius() {
            return 0.0f;
        }

        void onTransitionComplete() {
        }
    }

    public LauncherStateTransitionAnimation(Launcher launcher) {
        this.mLauncher = launcher;
    }

    private void cancelAnimation() {
        if (this.mCurrentAnimation != null) {
            this.mCurrentAnimation.setDuration(0L);
            this.mCurrentAnimation.cancel();
            this.mCurrentAnimation = null;
        }
    }

    private Animator dispatchOnLauncherTransitionStepAnim(View view, View view2) {
        ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(this, view, view2) { // from class: com.android.launcher3.LauncherStateTransitionAnimation.4
            final LauncherStateTransitionAnimation this$0;
            final View val$fromView;
            final View val$toView;

            {
                this.this$0 = this;
                this.val$fromView = view;
                this.val$toView = view2;
            }

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                this.this$0.dispatchOnLauncherTransitionStep(this.val$fromView, valueAnimator.getAnimatedFraction());
                this.this$0.dispatchOnLauncherTransitionStep(this.val$toView, valueAnimator.getAnimatedFraction());
            }
        });
        return ofFloat;
    }

    @SuppressLint({"NewApi"})
    private AnimatorSet startAnimationToOverlay(Workspace.State state, Workspace.State state2, View view, BaseContainerView baseContainerView, boolean z, PrivateTransitionCallbacks privateTransitionCallbacks) {
        float f;
        float f2;
        float f3;
        AnimatorSet createAnimatorSet = LauncherAnimUtils.createAnimatorSet();
        Resources resources = this.mLauncher.getResources();
        boolean z2 = Utilities.ATLEAST_LOLLIPOP;
        int integer = resources.getInteger(2131427336);
        int integer2 = resources.getInteger(2131427338);
        View workspace = this.mLauncher.getWorkspace();
        HashMap<View, Integer> hashMap = new HashMap<>();
        boolean z3 = view != null;
        cancelAnimation();
        Animator startWorkspaceStateChangeAnimation = this.mLauncher.startWorkspaceStateChangeAnimation(state2, -1, z, hashMap);
        startWorkspaceSearchBarAnimation(state2, z ? integer : 0, createAnimatorSet);
        Animator dispatchOnLauncherTransitionStepAnim = dispatchOnLauncherTransitionStepAnim(workspace, baseContainerView);
        View contentView = baseContainerView.getContentView();
        if (!z || !z3) {
            baseContainerView.setTranslationX(0.0f);
            baseContainerView.setTranslationY(0.0f);
            baseContainerView.setScaleX(1.0f);
            baseContainerView.setScaleY(1.0f);
            baseContainerView.setVisibility(0);
            baseContainerView.bringToFront();
            contentView.setVisibility(0);
            dispatchOnLauncherTransitionPrepare(workspace, z, false);
            dispatchOnLauncherTransitionStart(workspace, z, false);
            dispatchOnLauncherTransitionEnd(workspace, z, false);
            dispatchOnLauncherTransitionPrepare(baseContainerView, z, false);
            dispatchOnLauncherTransitionStart(baseContainerView, z, false);
            dispatchOnLauncherTransitionEnd(baseContainerView, z, false);
            privateTransitionCallbacks.onTransitionComplete();
            return null;
        }
        View revealView = baseContainerView.getRevealView();
        int measuredWidth = revealView.getMeasuredWidth();
        int measuredHeight = revealView.getMeasuredHeight();
        float hypot = (float) Math.hypot(measuredWidth / 2, measuredHeight / 2);
        revealView.setVisibility(0);
        revealView.setAlpha(0.0f);
        revealView.setTranslationY(0.0f);
        revealView.setTranslationX(0.0f);
        if (z2) {
            int[] centerDeltaInScreenSpace = Utilities.getCenterDeltaInScreenSpace(revealView, view, null);
            f = privateTransitionCallbacks.materialRevealViewFinalAlpha;
            f2 = centerDeltaInScreenSpace[1];
            f3 = centerDeltaInScreenSpace[0];
        } else {
            f = 0.0f;
            f2 = (measuredHeight * 2) / 3;
            f3 = 0.0f;
        }
        ObjectAnimator ofPropertyValuesHolder = ObjectAnimator.ofPropertyValuesHolder(revealView, PropertyValuesHolder.ofFloat("alpha", f, 1.0f), PropertyValuesHolder.ofFloat("translationY", f2, 0.0f), PropertyValuesHolder.ofFloat("translationX", f3, 0.0f));
        ofPropertyValuesHolder.setDuration(integer);
        ofPropertyValuesHolder.setInterpolator(new LogDecelerateInterpolator(100, 0));
        hashMap.put(revealView, 1);
        createAnimatorSet.play(ofPropertyValuesHolder);
        contentView.setVisibility(0);
        contentView.setAlpha(0.0f);
        contentView.setTranslationY(f2);
        hashMap.put(contentView, 1);
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(contentView, "translationY", f2, 0.0f);
        ofFloat.setDuration(integer);
        ofFloat.setInterpolator(new LogDecelerateInterpolator(100, 0));
        ofFloat.setStartDelay(integer2);
        createAnimatorSet.play(ofFloat);
        ObjectAnimator ofFloat2 = ObjectAnimator.ofFloat(contentView, "alpha", 0.0f, 1.0f);
        ofFloat2.setDuration(integer);
        ofFloat2.setInterpolator(new AccelerateInterpolator(1.5f));
        ofFloat2.setStartDelay(integer2);
        createAnimatorSet.play(ofFloat2);
        if (z2) {
            float materialRevealViewStartFinalRadius = privateTransitionCallbacks.getMaterialRevealViewStartFinalRadius();
            Animator.AnimatorListener materialRevealViewAnimatorListener = privateTransitionCallbacks.getMaterialRevealViewAnimatorListener(revealView, view);
            Animator createCircularReveal = UiThreadCircularReveal.createCircularReveal(revealView, measuredWidth / 2, measuredHeight / 2, materialRevealViewStartFinalRadius, hypot);
            createCircularReveal.setDuration(integer);
            createCircularReveal.setInterpolator(new LogDecelerateInterpolator(100, 0));
            if (materialRevealViewAnimatorListener != null) {
                createCircularReveal.addListener(materialRevealViewAnimatorListener);
            }
            createAnimatorSet.play(createCircularReveal);
        }
        createAnimatorSet.addListener(new AnimatorListenerAdapter(this, workspace, z, baseContainerView, revealView, hashMap, privateTransitionCallbacks) { // from class: com.android.launcher3.LauncherStateTransitionAnimation.2
            final LauncherStateTransitionAnimation this$0;
            final boolean val$animated;
            final View val$fromView;
            final HashMap val$layerViews;
            final PrivateTransitionCallbacks val$pCb;
            final View val$revealView;
            final BaseContainerView val$toView;

            {
                this.this$0 = this;
                this.val$fromView = workspace;
                this.val$animated = z;
                this.val$toView = baseContainerView;
                this.val$revealView = revealView;
                this.val$layerViews = hashMap;
                this.val$pCb = privateTransitionCallbacks;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                this.this$0.dispatchOnLauncherTransitionEnd(this.val$fromView, this.val$animated, false);
                this.this$0.dispatchOnLauncherTransitionEnd(this.val$toView, this.val$animated, false);
                this.val$revealView.setVisibility(4);
                for (View view2 : this.val$layerViews.keySet()) {
                    if (((Integer) this.val$layerViews.get(view2)).intValue() == 1) {
                        view2.setLayerType(0, null);
                    }
                }
                this.this$0.cleanupAnimation();
                this.val$pCb.onTransitionComplete();
            }
        });
        if (startWorkspaceStateChangeAnimation != null) {
            createAnimatorSet.play(startWorkspaceStateChangeAnimation);
        }
        createAnimatorSet.play(dispatchOnLauncherTransitionStepAnim);
        dispatchOnLauncherTransitionPrepare(workspace, z, false);
        dispatchOnLauncherTransitionPrepare(baseContainerView, z, false);
        Runnable runnable = new Runnable(this, createAnimatorSet, workspace, z, baseContainerView, hashMap) { // from class: com.android.launcher3.LauncherStateTransitionAnimation.3
            final LauncherStateTransitionAnimation this$0;
            final boolean val$animated;
            final View val$fromView;
            final HashMap val$layerViews;
            final AnimatorSet val$stateAnimation;
            final BaseContainerView val$toView;

            {
                this.this$0 = this;
                this.val$stateAnimation = createAnimatorSet;
                this.val$fromView = workspace;
                this.val$animated = z;
                this.val$toView = baseContainerView;
                this.val$layerViews = hashMap;
            }

            @Override // java.lang.Runnable
            public void run() {
                if (this.this$0.mCurrentAnimation != this.val$stateAnimation) {
                    return;
                }
                this.this$0.dispatchOnLauncherTransitionStart(this.val$fromView, this.val$animated, false);
                this.this$0.dispatchOnLauncherTransitionStart(this.val$toView, this.val$animated, false);
                for (View view2 : this.val$layerViews.keySet()) {
                    if (((Integer) this.val$layerViews.get(view2)).intValue() == 1) {
                        view2.setLayerType(2, null);
                    }
                    if (Utilities.ATLEAST_LOLLIPOP && Utilities.isViewAttachedToWindow(view2)) {
                        view2.buildLayer();
                    }
                }
                this.val$toView.requestFocus();
                this.val$stateAnimation.start();
            }
        };
        baseContainerView.bringToFront();
        baseContainerView.setVisibility(0);
        baseContainerView.post(runnable);
        return createAnimatorSet;
    }

    private void startAnimationToWorkspaceFromAllApps(Workspace.State state, Workspace.State state2, int i, boolean z, Runnable runnable) {
        this.mCurrentAnimation = startAnimationToWorkspaceFromOverlay(state, state2, i, this.mLauncher.getAllAppsButton(), this.mLauncher.getAppsView(), z, runnable, new PrivateTransitionCallbacks(this, 1.0f) { // from class: com.android.launcher3.LauncherStateTransitionAnimation.5
            final LauncherStateTransitionAnimation this$0;

            {
                this.this$0 = this;
            }

            @Override // com.android.launcher3.LauncherStateTransitionAnimation.PrivateTransitionCallbacks
            public AnimatorListenerAdapter getMaterialRevealViewAnimatorListener(View view, View view2) {
                return new AnimatorListenerAdapter(this, view2, view) { // from class: com.android.launcher3.LauncherStateTransitionAnimation.5.1
                    final AnonymousClass5 this$1;
                    final View val$allAppsButtonView;
                    final View val$revealView;

                    {
                        this.this$1 = this;
                        this.val$allAppsButtonView = view2;
                        this.val$revealView = view;
                    }

                    @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                    public void onAnimationEnd(Animator animator) {
                        this.val$revealView.setVisibility(4);
                        this.val$allAppsButtonView.setAlpha(1.0f);
                    }

                    @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                    public void onAnimationStart(Animator animator) {
                        this.val$allAppsButtonView.setVisibility(0);
                        this.val$allAppsButtonView.setAlpha(0.0f);
                    }
                };
            }

            @Override // com.android.launcher3.LauncherStateTransitionAnimation.PrivateTransitionCallbacks
            float getMaterialRevealViewStartFinalRadius() {
                return this.this$0.mLauncher.getDeviceProfile().allAppsButtonVisualSize / 2;
            }
        });
    }

    private AnimatorSet startAnimationToWorkspaceFromOverlay(Workspace.State state, Workspace.State state2, int i, View view, BaseContainerView baseContainerView, boolean z, Runnable runnable, PrivateTransitionCallbacks privateTransitionCallbacks) {
        float f;
        float f2;
        AnimatorSet createAnimatorSet = LauncherAnimUtils.createAnimatorSet();
        Resources resources = this.mLauncher.getResources();
        boolean z2 = Utilities.ATLEAST_LOLLIPOP;
        int integer = resources.getInteger(2131427336);
        int integer2 = resources.getInteger(2131427338);
        View workspace = this.mLauncher.getWorkspace();
        HashMap<View, Integer> hashMap = new HashMap<>();
        boolean z3 = view != null;
        cancelAnimation();
        Animator startWorkspaceStateChangeAnimation = this.mLauncher.startWorkspaceStateChangeAnimation(state2, i, z, hashMap);
        startWorkspaceSearchBarAnimation(state2, z ? integer : 0, createAnimatorSet);
        Animator dispatchOnLauncherTransitionStepAnim = dispatchOnLauncherTransitionStepAnim(baseContainerView, workspace);
        if (!z || !z3) {
            baseContainerView.setVisibility(8);
            dispatchOnLauncherTransitionPrepare(baseContainerView, z, true);
            dispatchOnLauncherTransitionStart(baseContainerView, z, true);
            dispatchOnLauncherTransitionEnd(baseContainerView, z, true);
            dispatchOnLauncherTransitionPrepare(workspace, z, true);
            dispatchOnLauncherTransitionStart(workspace, z, true);
            dispatchOnLauncherTransitionEnd(workspace, z, true);
            privateTransitionCallbacks.onTransitionComplete();
            if (runnable != null) {
                runnable.run();
                return null;
            }
            return null;
        }
        if (startWorkspaceStateChangeAnimation != null) {
            createAnimatorSet.play(startWorkspaceStateChangeAnimation);
        }
        createAnimatorSet.play(dispatchOnLauncherTransitionStepAnim);
        View revealView = baseContainerView.getRevealView();
        View contentView = baseContainerView.getContentView();
        if (baseContainerView.getVisibility() == 0) {
            int measuredWidth = revealView.getMeasuredWidth();
            int measuredHeight = revealView.getMeasuredHeight();
            float hypot = (float) Math.hypot(measuredWidth / 2, measuredHeight / 2);
            revealView.setVisibility(0);
            revealView.setAlpha(1.0f);
            revealView.setTranslationY(0.0f);
            hashMap.put(revealView, 1);
            if (z2) {
                int[] centerDeltaInScreenSpace = Utilities.getCenterDeltaInScreenSpace(revealView, view, null);
                f = centerDeltaInScreenSpace[1];
                f2 = centerDeltaInScreenSpace[0];
            } else {
                f = (measuredHeight * 2) / 3;
                f2 = 0.0f;
            }
            TimeInterpolator logDecelerateInterpolator = z2 ? new LogDecelerateInterpolator(100, 0) : new DecelerateInterpolator(1.0f);
            ObjectAnimator ofFloat = ObjectAnimator.ofFloat(revealView, "translationY", 0.0f, f);
            ofFloat.setDuration(integer - 16);
            ofFloat.setStartDelay(integer2 + 16);
            ofFloat.setInterpolator(logDecelerateInterpolator);
            createAnimatorSet.play(ofFloat);
            ObjectAnimator ofFloat2 = ObjectAnimator.ofFloat(revealView, "translationX", 0.0f, f2);
            ofFloat2.setDuration(integer - 16);
            ofFloat2.setStartDelay(integer2 + 16);
            ofFloat2.setInterpolator(logDecelerateInterpolator);
            createAnimatorSet.play(ofFloat2);
            float f3 = !z2 ? 0.0f : privateTransitionCallbacks.materialRevealViewFinalAlpha;
            if (f3 != 1.0f) {
                ObjectAnimator ofFloat3 = ObjectAnimator.ofFloat(revealView, "alpha", 1.0f, f3);
                ofFloat3.setDuration(z2 ? integer : 150);
                ofFloat3.setStartDelay(z2 ? 0 : integer2 + 16);
                ofFloat3.setInterpolator(logDecelerateInterpolator);
                createAnimatorSet.play(ofFloat3);
            }
            hashMap.put(contentView, 1);
            ObjectAnimator ofFloat4 = ObjectAnimator.ofFloat(contentView, "translationY", 0.0f, f);
            contentView.setTranslationY(0.0f);
            ofFloat4.setDuration(integer - 16);
            ofFloat4.setInterpolator(logDecelerateInterpolator);
            ofFloat4.setStartDelay(integer2 + 16);
            createAnimatorSet.play(ofFloat4);
            contentView.setAlpha(1.0f);
            ObjectAnimator ofFloat5 = ObjectAnimator.ofFloat(contentView, "alpha", 1.0f, 0.0f);
            ofFloat5.setDuration(100L);
            ofFloat5.setInterpolator(logDecelerateInterpolator);
            createAnimatorSet.play(ofFloat5);
            if (z2) {
                float materialRevealViewStartFinalRadius = privateTransitionCallbacks.getMaterialRevealViewStartFinalRadius();
                Animator.AnimatorListener materialRevealViewAnimatorListener = privateTransitionCallbacks.getMaterialRevealViewAnimatorListener(revealView, view);
                Animator createCircularReveal = UiThreadCircularReveal.createCircularReveal(revealView, measuredWidth / 2, measuredHeight / 2, hypot, materialRevealViewStartFinalRadius);
                createCircularReveal.setInterpolator(new LogDecelerateInterpolator(100, 0));
                createCircularReveal.setDuration(integer);
                createCircularReveal.setStartDelay(integer2);
                if (materialRevealViewAnimatorListener != null) {
                    createCircularReveal.addListener(materialRevealViewAnimatorListener);
                }
                createAnimatorSet.play(createCircularReveal);
            }
        }
        dispatchOnLauncherTransitionPrepare(baseContainerView, z, true);
        dispatchOnLauncherTransitionPrepare(workspace, z, true);
        createAnimatorSet.addListener(new AnimatorListenerAdapter(this, baseContainerView, z, workspace, runnable, hashMap, contentView, privateTransitionCallbacks) { // from class: com.android.launcher3.LauncherStateTransitionAnimation.7
            final LauncherStateTransitionAnimation this$0;
            final boolean val$animated;
            final View val$contentView;
            final BaseContainerView val$fromView;
            final HashMap val$layerViews;
            final Runnable val$onCompleteRunnable;
            final PrivateTransitionCallbacks val$pCb;
            final View val$toView;

            {
                this.this$0 = this;
                this.val$fromView = baseContainerView;
                this.val$animated = z;
                this.val$toView = workspace;
                this.val$onCompleteRunnable = runnable;
                this.val$layerViews = hashMap;
                this.val$contentView = contentView;
                this.val$pCb = privateTransitionCallbacks;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                this.val$fromView.setVisibility(8);
                this.this$0.dispatchOnLauncherTransitionEnd(this.val$fromView, this.val$animated, true);
                this.this$0.dispatchOnLauncherTransitionEnd(this.val$toView, this.val$animated, true);
                if (this.val$onCompleteRunnable != null) {
                    this.val$onCompleteRunnable.run();
                }
                for (View view2 : this.val$layerViews.keySet()) {
                    if (((Integer) this.val$layerViews.get(view2)).intValue() == 1) {
                        view2.setLayerType(0, null);
                    }
                }
                if (this.val$contentView != null) {
                    this.val$contentView.setTranslationX(0.0f);
                    this.val$contentView.setTranslationY(0.0f);
                    this.val$contentView.setAlpha(1.0f);
                }
                this.this$0.cleanupAnimation();
                this.val$pCb.onTransitionComplete();
            }
        });
        baseContainerView.post(new Runnable(this, createAnimatorSet, baseContainerView, z, workspace, hashMap) { // from class: com.android.launcher3.LauncherStateTransitionAnimation.8
            final LauncherStateTransitionAnimation this$0;
            final boolean val$animated;
            final BaseContainerView val$fromView;
            final HashMap val$layerViews;
            final AnimatorSet val$stateAnimation;
            final View val$toView;

            {
                this.this$0 = this;
                this.val$stateAnimation = createAnimatorSet;
                this.val$fromView = baseContainerView;
                this.val$animated = z;
                this.val$toView = workspace;
                this.val$layerViews = hashMap;
            }

            @Override // java.lang.Runnable
            public void run() {
                if (this.this$0.mCurrentAnimation != this.val$stateAnimation) {
                    return;
                }
                this.this$0.dispatchOnLauncherTransitionStart(this.val$fromView, this.val$animated, false);
                this.this$0.dispatchOnLauncherTransitionStart(this.val$toView, this.val$animated, false);
                for (View view2 : this.val$layerViews.keySet()) {
                    if (((Integer) this.val$layerViews.get(view2)).intValue() == 1) {
                        view2.setLayerType(2, null);
                    }
                    if (Utilities.ATLEAST_LOLLIPOP && Utilities.isViewAttachedToWindow(view2)) {
                        view2.buildLayer();
                    }
                }
                this.val$stateAnimation.start();
            }
        });
        return createAnimatorSet;
    }

    private void startAnimationToWorkspaceFromWidgets(Workspace.State state, Workspace.State state2, int i, boolean z, Runnable runnable) {
        this.mCurrentAnimation = startAnimationToWorkspaceFromOverlay(state, state2, i, this.mLauncher.getWidgetsButton(), this.mLauncher.getWidgetsView(), z, runnable, new PrivateTransitionCallbacks(this, 0.3f) { // from class: com.android.launcher3.LauncherStateTransitionAnimation.6
            final LauncherStateTransitionAnimation this$0;

            {
                this.this$0 = this;
            }

            @Override // com.android.launcher3.LauncherStateTransitionAnimation.PrivateTransitionCallbacks
            public AnimatorListenerAdapter getMaterialRevealViewAnimatorListener(View view, View view2) {
                return new AnimatorListenerAdapter(this, view) { // from class: com.android.launcher3.LauncherStateTransitionAnimation.6.1
                    final AnonymousClass6 this$1;
                    final View val$revealView;

                    {
                        this.this$1 = this;
                        this.val$revealView = view;
                    }

                    @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                    public void onAnimationEnd(Animator animator) {
                        this.val$revealView.setVisibility(4);
                    }
                };
            }
        });
    }

    private void startWorkspaceSearchBarAnimation(Workspace.State state, int i, AnimatorSet animatorSet) {
        this.mLauncher.getSearchDropTargetBar().animateToState(state.searchDropTargetBarState, i, animatorSet);
    }

    void cleanupAnimation() {
        this.mCurrentAnimation = null;
    }

    void dispatchOnLauncherTransitionEnd(View view, boolean z, boolean z2) {
        if (view instanceof LauncherTransitionable) {
            ((LauncherTransitionable) view).onLauncherTransitionEnd(this.mLauncher, z, z2);
        }
        dispatchOnLauncherTransitionStep(view, 1.0f);
    }

    void dispatchOnLauncherTransitionPrepare(View view, boolean z, boolean z2) {
        if (view instanceof LauncherTransitionable) {
            ((LauncherTransitionable) view).onLauncherTransitionPrepare(this.mLauncher, z, z2);
        }
    }

    void dispatchOnLauncherTransitionStart(View view, boolean z, boolean z2) {
        if (view instanceof LauncherTransitionable) {
            ((LauncherTransitionable) view).onLauncherTransitionStart(this.mLauncher, z, z2);
        }
        dispatchOnLauncherTransitionStep(view, 0.0f);
    }

    void dispatchOnLauncherTransitionStep(View view, float f) {
        if (view instanceof LauncherTransitionable) {
            ((LauncherTransitionable) view).onLauncherTransitionStep(this.mLauncher, f);
        }
    }

    public void startAnimationToAllApps(Workspace.State state, boolean z, boolean z2) {
        AllAppsContainerView appsView = this.mLauncher.getAppsView();
        this.mCurrentAnimation = startAnimationToOverlay(state, Workspace.State.NORMAL_HIDDEN, this.mLauncher.getAllAppsButton(), appsView, z, new PrivateTransitionCallbacks(this, 1.0f, z2, appsView) { // from class: com.android.launcher3.LauncherStateTransitionAnimation.1
            final LauncherStateTransitionAnimation this$0;
            final boolean val$startSearchAfterTransition;
            final AllAppsContainerView val$toView;

            {
                this.this$0 = this;
                this.val$startSearchAfterTransition = z2;
                this.val$toView = appsView;
            }

            @Override // com.android.launcher3.LauncherStateTransitionAnimation.PrivateTransitionCallbacks
            public AnimatorListenerAdapter getMaterialRevealViewAnimatorListener(View view, View view2) {
                return new AnimatorListenerAdapter(this, view2) { // from class: com.android.launcher3.LauncherStateTransitionAnimation.1.1
                    final AnonymousClass1 this$1;
                    final View val$allAppsButtonView;

                    {
                        this.this$1 = this;
                        this.val$allAppsButtonView = view2;
                    }

                    @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                    public void onAnimationEnd(Animator animator) {
                        this.val$allAppsButtonView.setVisibility(0);
                    }

                    @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                    public void onAnimationStart(Animator animator) {
                        this.val$allAppsButtonView.setVisibility(4);
                    }
                };
            }

            @Override // com.android.launcher3.LauncherStateTransitionAnimation.PrivateTransitionCallbacks
            public float getMaterialRevealViewStartFinalRadius() {
                return this.this$0.mLauncher.getDeviceProfile().allAppsButtonVisualSize / 2;
            }

            @Override // com.android.launcher3.LauncherStateTransitionAnimation.PrivateTransitionCallbacks
            void onTransitionComplete() {
                if (this.val$startSearchAfterTransition) {
                    this.val$toView.startAppsSearch();
                }
            }
        });
    }

    public void startAnimationToWidgets(Workspace.State state, boolean z) {
        WidgetsContainerView widgetsView = this.mLauncher.getWidgetsView();
        this.mCurrentAnimation = startAnimationToOverlay(state, Workspace.State.OVERVIEW_HIDDEN, this.mLauncher.getWidgetsButton(), widgetsView, z, new PrivateTransitionCallbacks(0.3f));
    }

    public void startAnimationToWorkspace(Launcher.State state, Workspace.State state2, Workspace.State state3, int i, boolean z, Runnable runnable) {
        if (state3 != Workspace.State.NORMAL && state3 != Workspace.State.SPRING_LOADED && state3 != Workspace.State.OVERVIEW) {
            Log.e("LSTAnimation", "Unexpected call to startAnimationToWorkspace");
        }
        if (state == Launcher.State.APPS || state == Launcher.State.APPS_SPRING_LOADED) {
            startAnimationToWorkspaceFromAllApps(state2, state3, i, z, runnable);
        } else {
            startAnimationToWorkspaceFromWidgets(state2, state3, i, z, runnable);
        }
    }
}
