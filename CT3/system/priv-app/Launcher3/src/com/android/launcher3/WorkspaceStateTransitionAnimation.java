package com.android.launcher3;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.DecelerateInterpolator;
import com.android.launcher3.Workspace;
import java.util.HashMap;
/* loaded from: a.zip:com/android/launcher3/WorkspaceStateTransitionAnimation.class */
public class WorkspaceStateTransitionAnimation {
    int mAllAppsTransitionTime;
    final Launcher mLauncher;
    float[] mNewAlphas;
    float[] mNewBackgroundAlphas;
    float mNewScale;
    float[] mOldAlphas;
    float[] mOldBackgroundAlphas;
    int mOverlayTransitionTime;
    float mOverviewModeShrinkFactor;
    int mOverviewTransitionTime;
    float mSpringLoadedShrinkFactor;
    AnimatorSet mStateAnimator;
    final Workspace mWorkspace;
    boolean mWorkspaceFadeInAdjacentScreens;
    float mWorkspaceScrimAlpha;
    int mLastChildCount = -1;
    final ZoomInInterpolator mZoomInInterpolator = new ZoomInInterpolator();

    public WorkspaceStateTransitionAnimation(Launcher launcher, Workspace workspace) {
        this.mLauncher = launcher;
        this.mWorkspace = workspace;
        DeviceProfile deviceProfile = this.mLauncher.getDeviceProfile();
        Resources resources = launcher.getResources();
        this.mAllAppsTransitionTime = resources.getInteger(2131427332);
        this.mOverviewTransitionTime = resources.getInteger(2131427333);
        this.mOverlayTransitionTime = resources.getInteger(2131427337);
        this.mSpringLoadedShrinkFactor = resources.getInteger(2131427334) / 100.0f;
        this.mOverviewModeShrinkFactor = resources.getInteger(2131427335) / 100.0f;
        this.mWorkspaceScrimAlpha = resources.getInteger(2131427331) / 100.0f;
        this.mWorkspaceFadeInAdjacentScreens = deviceProfile.shouldFadeAdjacentWorkspaceScreens();
    }

    private void animateBackgroundGradient(TransitionStates transitionStates, boolean z, int i) {
        DragLayer dragLayer = this.mLauncher.getDragLayer();
        float backgroundAlpha = dragLayer.getBackgroundAlpha();
        float f = transitionStates.stateIsNormal ? 0.0f : this.mWorkspaceScrimAlpha;
        if (f != backgroundAlpha) {
            if (!z) {
                dragLayer.setBackgroundAlpha(f);
                return;
            }
            ValueAnimator ofFloat = LauncherAnimUtils.ofFloat(this.mWorkspace, backgroundAlpha, f);
            ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(this, dragLayer) { // from class: com.android.launcher3.WorkspaceStateTransitionAnimation.2
                final WorkspaceStateTransitionAnimation this$0;
                final DragLayer val$dragLayer;

                {
                    this.this$0 = this;
                    this.val$dragLayer = dragLayer;
                }

                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    this.val$dragLayer.setBackgroundAlpha(((Float) valueAnimator.getAnimatedValue()).floatValue());
                }
            });
            ofFloat.setInterpolator(new DecelerateInterpolator(1.5f));
            ofFloat.setDuration(i);
            this.mStateAnimator.play(ofFloat);
        }
    }

    /* JADX WARN: Code restructure failed: missing block: B:50:0x0114, code lost:
        if (r9.allAppsToWorkspace != false) goto L63;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private void animateWorkspace(TransitionStates transitionStates, int i, boolean z, int i2, HashMap<View, Integer> hashMap, boolean z2) {
        LauncherViewPropertyAnimator ofFloat;
        reinitializeAnimationArrays();
        cancelAnimation();
        if (z) {
            this.mStateAnimator = LauncherAnimUtils.createAnimatorSet();
        }
        float f = (transitionStates.stateIsSpringLoaded || transitionStates.stateIsOverview) ? 1.0f : 0.0f;
        float f2 = (transitionStates.stateIsNormal || transitionStates.stateIsSpringLoaded) ? 1.0f : 0.0f;
        float f3 = transitionStates.stateIsOverview ? 1.0f : 0.0f;
        float overviewModeTranslationY = (transitionStates.stateIsOverview || transitionStates.stateIsOverviewHidden) ? this.mWorkspace.getOverviewModeTranslationY() : 0;
        int childCount = this.mWorkspace.getChildCount();
        int numCustomPages = this.mWorkspace.numCustomPages();
        this.mNewScale = 1.0f;
        if (transitionStates.oldStateIsOverview) {
            this.mWorkspace.disableFreeScroll();
        } else if (transitionStates.stateIsOverview) {
            this.mWorkspace.enableFreeScroll();
        }
        if (!transitionStates.stateIsNormal) {
            if (transitionStates.stateIsSpringLoaded) {
                this.mNewScale = this.mSpringLoadedShrinkFactor;
            } else if (transitionStates.stateIsOverview || transitionStates.stateIsOverviewHidden) {
                this.mNewScale = this.mOverviewModeShrinkFactor;
            }
        }
        int i3 = i;
        if (i == -1) {
            i3 = this.mWorkspace.getPageNearestToCenterOfScreen();
        }
        this.mWorkspace.snapToPage(i3, i2, this.mZoomInInterpolator);
        int i4 = 0;
        while (i4 < childCount) {
            CellLayout cellLayout = (CellLayout) this.mWorkspace.getChildAt(i4);
            boolean z3 = i4 == i3;
            float alpha = cellLayout.getShortcutsAndWidgets().getAlpha();
            float f4 = (transitionStates.stateIsNormalHidden || transitionStates.stateIsOverviewHidden) ? 0.0f : (transitionStates.stateIsNormal && this.mWorkspaceFadeInAdjacentScreens) ? (i4 == i3 || i4 < numCustomPages) ? 1.0f : 0.0f : 1.0f;
            float f5 = f4;
            float f6 = alpha;
            if (!this.mWorkspace.isSwitchingState()) {
                if (!transitionStates.workspaceToAllApps) {
                    f5 = f4;
                    f6 = alpha;
                }
                if (transitionStates.allAppsToWorkspace && z3) {
                    alpha = 0.0f;
                } else if (!z3) {
                    f4 = 0.0f;
                    alpha = 0.0f;
                }
                cellLayout.setShortcutAndWidgetAlpha(alpha);
                f6 = alpha;
                f5 = f4;
            }
            this.mOldAlphas[i4] = f6;
            this.mNewAlphas[i4] = f5;
            if (z) {
                this.mOldBackgroundAlphas[i4] = cellLayout.getBackgroundAlpha();
                this.mNewBackgroundAlphas[i4] = f;
            } else {
                cellLayout.setBackgroundAlpha(f);
                cellLayout.setShortcutAndWidgetAlpha(f5);
                cellLayout.invalidate();
            }
            i4++;
        }
        ViewGroup overviewPanel = this.mLauncher.getOverviewPanel();
        View hotseat = this.mLauncher.getHotseat();
        PageIndicator pageIndicator = this.mWorkspace.getPageIndicator();
        if (!z) {
            overviewPanel.setAlpha(f3);
            AlphaUpdateListener.updateVisibility(overviewPanel, z2);
            hotseat.setAlpha(f2);
            AlphaUpdateListener.updateVisibility(hotseat, z2);
            if (pageIndicator != null) {
                pageIndicator.setAlpha(f2);
                AlphaUpdateListener.updateVisibility(pageIndicator, z2);
            }
            this.mWorkspace.updateCustomContentVisibility();
            this.mWorkspace.setScaleX(this.mNewScale);
            this.mWorkspace.setScaleY(this.mNewScale);
            this.mWorkspace.setTranslationY(overviewModeTranslationY);
            if (z2 && overviewPanel.getVisibility() == 0) {
                overviewPanel.getChildAt(0).performAccessibilityAction(64, null);
                return;
            }
            return;
        }
        LauncherViewPropertyAnimator launcherViewPropertyAnimator = new LauncherViewPropertyAnimator(this.mWorkspace);
        launcherViewPropertyAnimator.scaleX(this.mNewScale).scaleY(this.mNewScale).translationY(overviewModeTranslationY).setDuration(i2).setInterpolator(this.mZoomInInterpolator);
        this.mStateAnimator.play(launcherViewPropertyAnimator);
        for (int i5 = 0; i5 < childCount; i5++) {
            CellLayout cellLayout2 = (CellLayout) this.mWorkspace.getChildAt(i5);
            float alpha2 = cellLayout2.getShortcutsAndWidgets().getAlpha();
            if (this.mOldAlphas[i5] == 0.0f && this.mNewAlphas[i5] == 0.0f) {
                cellLayout2.setBackgroundAlpha(this.mNewBackgroundAlphas[i5]);
                cellLayout2.setShortcutAndWidgetAlpha(this.mNewAlphas[i5]);
            } else {
                if (hashMap != null) {
                    hashMap.put(cellLayout2, 0);
                }
                if (this.mOldAlphas[i5] != this.mNewAlphas[i5] || alpha2 != this.mNewAlphas[i5]) {
                    LauncherViewPropertyAnimator launcherViewPropertyAnimator2 = new LauncherViewPropertyAnimator(cellLayout2.getShortcutsAndWidgets());
                    launcherViewPropertyAnimator2.alpha(this.mNewAlphas[i5]).setDuration(i2).setInterpolator(this.mZoomInInterpolator);
                    this.mStateAnimator.play(launcherViewPropertyAnimator2);
                }
                if (this.mOldBackgroundAlphas[i5] != 0.0f || this.mNewBackgroundAlphas[i5] != 0.0f) {
                    ObjectAnimator ofFloat2 = ObjectAnimator.ofFloat(cellLayout2, "backgroundAlpha", this.mOldBackgroundAlphas[i5], this.mNewBackgroundAlphas[i5]);
                    LauncherAnimUtils.ofFloat(cellLayout2, 0.0f, 1.0f);
                    ofFloat2.setInterpolator(this.mZoomInInterpolator);
                    ofFloat2.setDuration(i2);
                    this.mStateAnimator.play(ofFloat2);
                }
            }
        }
        if (pageIndicator != null) {
            LauncherViewPropertyAnimator withLayer = new LauncherViewPropertyAnimator(pageIndicator).alpha(f2).withLayer();
            withLayer.addListener(new AlphaUpdateListener(pageIndicator, z2));
            ofFloat = withLayer;
        } else {
            ofFloat = ValueAnimator.ofFloat(0.0f, 0.0f);
        }
        LauncherViewPropertyAnimator alpha3 = new LauncherViewPropertyAnimator(hotseat).alpha(f2);
        alpha3.addListener(new AlphaUpdateListener(hotseat, z2));
        LauncherViewPropertyAnimator alpha4 = new LauncherViewPropertyAnimator(overviewPanel).alpha(f3);
        alpha4.addListener(new AlphaUpdateListener(overviewPanel, z2));
        hotseat.setLayerType(2, null);
        overviewPanel.setLayerType(2, null);
        if (hashMap != null) {
            hashMap.put(hotseat, 1);
            hashMap.put(overviewPanel, 1);
        } else {
            alpha3.withLayer();
            alpha4.withLayer();
        }
        if (transitionStates.workspaceToOverview) {
            ofFloat.setInterpolator(new DecelerateInterpolator(2.0f));
            alpha3.setInterpolator(new DecelerateInterpolator(2.0f));
            alpha4.setInterpolator(null);
        } else if (transitionStates.overviewToWorkspace) {
            ofFloat.setInterpolator(null);
            alpha3.setInterpolator(null);
            alpha4.setInterpolator(new DecelerateInterpolator(2.0f));
        }
        alpha4.setDuration(i2);
        ofFloat.setDuration(i2);
        alpha3.setDuration(i2);
        this.mStateAnimator.play(alpha4);
        this.mStateAnimator.play(alpha3);
        this.mStateAnimator.play(ofFloat);
        this.mStateAnimator.addListener(new AnimatorListenerAdapter(this, z2, overviewPanel) { // from class: com.android.launcher3.WorkspaceStateTransitionAnimation.1
            final WorkspaceStateTransitionAnimation this$0;
            final boolean val$accessibilityEnabled;
            final ViewGroup val$overviewPanel;

            {
                this.this$0 = this;
                this.val$accessibilityEnabled = z2;
                this.val$overviewPanel = overviewPanel;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                this.this$0.mStateAnimator = null;
                if (this.val$accessibilityEnabled && this.val$overviewPanel.getVisibility() == 0) {
                    this.val$overviewPanel.getChildAt(0).performAccessibilityAction(64, null);
                }
            }
        });
    }

    private void cancelAnimation() {
        if (this.mStateAnimator != null) {
            this.mStateAnimator.setDuration(0L);
            this.mStateAnimator.cancel();
        }
        this.mStateAnimator = null;
    }

    private int getAnimationDuration(TransitionStates transitionStates) {
        return (transitionStates.workspaceToAllApps || transitionStates.overviewToAllApps) ? this.mAllAppsTransitionTime : (transitionStates.workspaceToOverview || transitionStates.overviewToWorkspace) ? this.mOverviewTransitionTime : this.mOverlayTransitionTime;
    }

    private void reinitializeAnimationArrays() {
        int childCount = this.mWorkspace.getChildCount();
        if (this.mLastChildCount == childCount) {
            return;
        }
        this.mOldBackgroundAlphas = new float[childCount];
        this.mOldAlphas = new float[childCount];
        this.mNewBackgroundAlphas = new float[childCount];
        this.mNewAlphas = new float[childCount];
    }

    public AnimatorSet getAnimationToState(Workspace.State state, Workspace.State state2, int i, boolean z, HashMap<View, Integer> hashMap) {
        boolean isEnabled = ((AccessibilityManager) this.mLauncher.getSystemService("accessibility")).isEnabled();
        TransitionStates transitionStates = new TransitionStates(state, state2);
        animateWorkspace(transitionStates, i, z, getAnimationDuration(transitionStates), hashMap, isEnabled);
        animateBackgroundGradient(transitionStates, z, 350);
        return this.mStateAnimator;
    }

    public float getFinalScale() {
        return this.mNewScale;
    }
}
