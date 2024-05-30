package com.android.launcher3;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import com.android.launcher3.DeviceProfile;
import com.android.launcher3.InsettableFrameLayout;
import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherAnimationRunner;
import com.android.launcher3.LauncherAppTransitionManagerImpl;
import com.android.launcher3.allapps.AllAppsContainerView;
import com.android.launcher3.allapps.AllAppsTransitionController;
import com.android.launcher3.anim.AnimatorPlaybackController;
import com.android.launcher3.anim.Interpolators;
import com.android.launcher3.dragndrop.DragLayer;
import com.android.launcher3.graphics.DrawableFactory;
import com.android.launcher3.shortcuts.DeepShortcutView;
import com.android.launcher3.util.MultiValueAlpha;
import com.android.quickstep.TaskUtils;
import com.android.quickstep.util.ClipAnimationHelper;
import com.android.quickstep.util.MultiValueUpdateListener;
import com.android.quickstep.util.RemoteAnimationProvider;
import com.android.quickstep.views.RecentsView;
import com.android.quickstep.views.TaskView;
import com.android.systemui.shared.system.ActivityCompat;
import com.android.systemui.shared.system.ActivityOptionsCompat;
import com.android.systemui.shared.system.RemoteAnimationAdapterCompat;
import com.android.systemui.shared.system.RemoteAnimationDefinitionCompat;
import com.android.systemui.shared.system.RemoteAnimationRunnerCompat;
import com.android.systemui.shared.system.RemoteAnimationTargetCompat;
import com.android.systemui.shared.system.TransactionCompat;
@TargetApi(26)
/* loaded from: classes.dex */
public class LauncherAppTransitionManagerImpl extends LauncherAppTransitionManager implements DeviceProfile.OnDeviceProfileChangeListener {
    public static final float ALL_APPS_PROGRESS_OFF_SCREEN = 1.3059858f;
    private static final int APP_LAUNCH_ALPHA_DURATION = 50;
    private static final int APP_LAUNCH_ALPHA_START_DELAY = 32;
    private static final int APP_LAUNCH_CURVED_DURATION = 250;
    private static final float APP_LAUNCH_DOWN_DUR_SCALE_FACTOR = 0.8f;
    private static final int APP_LAUNCH_DURATION = 500;
    private static final int CLOSING_TRANSITION_DURATION_MS = 250;
    private static final String CONTROL_REMOTE_APP_TRANSITION_PERMISSION = "android.permission.CONTROL_REMOTE_APP_TRANSITION_ANIMATIONS";
    private static final int LAUNCHER_RESUME_START_DELAY = 100;
    public static final int RECENTS_LAUNCH_DURATION = 336;
    public static final int RECENTS_QUICKSCRUB_LAUNCH_DURATION = 300;
    public static final int STATUS_BAR_TRANSITION_DURATION = 120;
    private static final String TAG = "LauncherTransition";
    private final float mClosingWindowTransY;
    private final float mContentTransY;
    private DeviceProfile mDeviceProfile;
    private final DragLayer mDragLayer;
    private final MultiValueAlpha.AlphaProperty mDragLayerAlpha;
    private View mFloatingView;
    private final AnimatorListenerAdapter mForceInvisibleListener = new AnimatorListenerAdapter() { // from class: com.android.launcher3.LauncherAppTransitionManagerImpl.1
        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
        public void onAnimationStart(Animator animator) {
            LauncherAppTransitionManagerImpl.this.mLauncher.addForceInvisibleFlag(2);
        }

        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animator) {
            LauncherAppTransitionManagerImpl.this.mLauncher.clearForceInvisibleFlag(2);
        }
    };
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final boolean mIsRtl;
    private final Launcher mLauncher;
    private RemoteAnimationProvider mRemoteAnimationProvider;
    private final float mWorkspaceTransY;

    public LauncherAppTransitionManagerImpl(Context context) {
        this.mLauncher = Launcher.getLauncher(context);
        this.mDragLayer = this.mLauncher.getDragLayer();
        this.mDragLayerAlpha = this.mDragLayer.getAlphaProperty(2);
        this.mIsRtl = Utilities.isRtl(this.mLauncher.getResources());
        this.mDeviceProfile = this.mLauncher.getDeviceProfile();
        Resources resources = this.mLauncher.getResources();
        this.mContentTransY = resources.getDimensionPixelSize(R.dimen.content_trans_y);
        this.mWorkspaceTransY = resources.getDimensionPixelSize(R.dimen.workspace_trans_y);
        this.mClosingWindowTransY = resources.getDimensionPixelSize(R.dimen.closing_window_trans_y);
        this.mLauncher.addOnDeviceProfileChangeListener(this);
        registerRemoteAnimations();
    }

    @Override // com.android.launcher3.DeviceProfile.OnDeviceProfileChangeListener
    public void onDeviceProfileChanged(DeviceProfile deviceProfile) {
        this.mDeviceProfile = deviceProfile;
    }

    @Override // com.android.launcher3.LauncherAppTransitionManager
    public ActivityOptions getActivityLaunchOptions(Launcher launcher, final View view) {
        if (hasControlRemoteAppTransitionPermission()) {
            LauncherAnimationRunner launcherAnimationRunner = new LauncherAnimationRunner(this.mHandler, true) { // from class: com.android.launcher3.LauncherAppTransitionManagerImpl.2
                @Override // com.android.launcher3.LauncherAnimationRunner
                public void onCreateAnimation(RemoteAnimationTargetCompat[] remoteAnimationTargetCompatArr, LauncherAnimationRunner.AnimationResult animationResult) {
                    AnimatorSet animatorSet = new AnimatorSet();
                    boolean launcherIsATargetWithMode = LauncherAppTransitionManagerImpl.this.launcherIsATargetWithMode(remoteAnimationTargetCompatArr, 1);
                    if (!LauncherAppTransitionManagerImpl.this.composeRecentsLaunchAnimator(view, remoteAnimationTargetCompatArr, animatorSet)) {
                        LauncherAppTransitionManagerImpl.this.mLauncher.getStateManager().setCurrentAnimation(animatorSet, new Animator[0]);
                        Rect windowTargetBounds = LauncherAppTransitionManagerImpl.this.getWindowTargetBounds(remoteAnimationTargetCompatArr);
                        animatorSet.play(LauncherAppTransitionManagerImpl.this.getIconAnimator(view, windowTargetBounds));
                        if (launcherIsATargetWithMode) {
                            final Pair launcherContentAnimator = LauncherAppTransitionManagerImpl.this.getLauncherContentAnimator(true);
                            animatorSet.play((Animator) launcherContentAnimator.first);
                            animatorSet.addListener(new AnimatorListenerAdapter() { // from class: com.android.launcher3.LauncherAppTransitionManagerImpl.2.1
                                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                                public void onAnimationEnd(Animator animator) {
                                    ((Runnable) launcherContentAnimator.second).run();
                                }
                            });
                        }
                        animatorSet.play(LauncherAppTransitionManagerImpl.this.getOpeningWindowAnimators(view, remoteAnimationTargetCompatArr, windowTargetBounds));
                    }
                    if (launcherIsATargetWithMode) {
                        animatorSet.addListener(LauncherAppTransitionManagerImpl.this.mForceInvisibleListener);
                    }
                    animationResult.setAnimation(animatorSet);
                }
            };
            int i = TaskUtils.findTaskViewToLaunch(launcher, view, null) != null ? RECENTS_LAUNCH_DURATION : 500;
            return ActivityOptionsCompat.makeRemoteAnimation(new RemoteAnimationAdapterCompat(launcherAnimationRunner, i, i - 120));
        }
        return super.getActivityLaunchOptions(launcher, view);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Rect getWindowTargetBounds(RemoteAnimationTargetCompat[] remoteAnimationTargetCompatArr) {
        Rect rect = new Rect(0, 0, this.mDeviceProfile.widthPx, this.mDeviceProfile.heightPx);
        if (this.mLauncher.isInMultiWindowModeCompat()) {
            for (RemoteAnimationTargetCompat remoteAnimationTargetCompat : remoteAnimationTargetCompatArr) {
                if (remoteAnimationTargetCompat.mode == 0) {
                    rect.set(remoteAnimationTargetCompat.sourceContainerBounds);
                    rect.offsetTo(remoteAnimationTargetCompat.position.x, remoteAnimationTargetCompat.position.y);
                    return rect;
                }
            }
        }
        return rect;
    }

    public void setRemoteAnimationProvider(final RemoteAnimationProvider remoteAnimationProvider, CancellationSignal cancellationSignal) {
        this.mRemoteAnimationProvider = remoteAnimationProvider;
        cancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener() { // from class: com.android.launcher3.-$$Lambda$LauncherAppTransitionManagerImpl$4p4xS8cHBNcMDf68XfTcNBW0l80
            @Override // android.os.CancellationSignal.OnCancelListener
            public final void onCancel() {
                LauncherAppTransitionManagerImpl.lambda$setRemoteAnimationProvider$0(LauncherAppTransitionManagerImpl.this, remoteAnimationProvider);
            }
        });
    }

    public static /* synthetic */ void lambda$setRemoteAnimationProvider$0(LauncherAppTransitionManagerImpl launcherAppTransitionManagerImpl, RemoteAnimationProvider remoteAnimationProvider) {
        if (remoteAnimationProvider == launcherAppTransitionManagerImpl.mRemoteAnimationProvider) {
            launcherAppTransitionManagerImpl.mRemoteAnimationProvider = null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean composeRecentsLaunchAnimator(View view, RemoteAnimationTargetCompat[] remoteAnimationTargetCompatArr, AnimatorSet animatorSet) {
        int i;
        Animator duration;
        AnimatorListenerAdapter animatorListenerAdapter;
        if (this.mLauncher.getStateManager().getState().overviewUi) {
            RecentsView recentsView = (RecentsView) this.mLauncher.getOverviewPanel();
            boolean launcherIsATargetWithMode = launcherIsATargetWithMode(remoteAnimationTargetCompatArr, 1);
            boolean z = !launcherIsATargetWithMode;
            boolean isWaitingForTaskLaunch = recentsView.getQuickScrubController().isWaitingForTaskLaunch();
            TaskView findTaskViewToLaunch = TaskUtils.findTaskViewToLaunch(this.mLauncher, view, remoteAnimationTargetCompatArr);
            if (findTaskViewToLaunch == null) {
                return false;
            }
            if (isWaitingForTaskLaunch) {
                i = 300;
            } else {
                i = RECENTS_LAUNCH_DURATION;
            }
            ClipAnimationHelper clipAnimationHelper = new ClipAnimationHelper();
            ValueAnimator recentsWindowAnimator = TaskUtils.getRecentsWindowAnimator(findTaskViewToLaunch, z, remoteAnimationTargetCompatArr, clipAnimationHelper);
            long j = i;
            animatorSet.play(recentsWindowAnimator.setDuration(j));
            AnimatorSet animatorSet2 = null;
            if (launcherIsATargetWithMode) {
                duration = recentsView.createAdjacentPageAnimForTaskLaunch(findTaskViewToLaunch, clipAnimationHelper);
                duration.setInterpolator(Interpolators.TOUCH_RESPONSE_INTERPOLATOR);
                duration.setDuration(j);
                animatorListenerAdapter = new AnimatorListenerAdapter() { // from class: com.android.launcher3.LauncherAppTransitionManagerImpl.3
                    @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                    public void onAnimationEnd(Animator animator) {
                        LauncherAppTransitionManagerImpl.this.mLauncher.getStateManager().moveToRestState();
                        LauncherAppTransitionManagerImpl.this.mLauncher.getStateManager().reapplyState();
                    }
                };
            } else {
                AnimatorPlaybackController createAnimationToNewWorkspace = this.mLauncher.getStateManager().createAnimationToNewWorkspace(LauncherState.NORMAL, j);
                createAnimationToNewWorkspace.dispatchOnStart();
                animatorSet2 = createAnimationToNewWorkspace.getTarget();
                duration = createAnimationToNewWorkspace.getAnimationPlayer().setDuration(j);
                animatorListenerAdapter = new AnimatorListenerAdapter() { // from class: com.android.launcher3.LauncherAppTransitionManagerImpl.4
                    @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                    public void onAnimationEnd(Animator animator) {
                        LauncherAppTransitionManagerImpl.this.mLauncher.getStateManager().goToState(LauncherState.NORMAL, false);
                    }
                };
            }
            animatorSet.play(duration);
            this.mLauncher.getStateManager().setCurrentAnimation(animatorSet, animatorSet2);
            animatorSet.addListener(animatorListenerAdapter);
            return true;
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Pair<AnimatorSet, Runnable> getLauncherContentAnimator(boolean z) {
        float[] fArr;
        Runnable runnable;
        AnimatorSet animatorSet = new AnimatorSet();
        if (z) {
            fArr = new float[]{1.0f, 0.0f};
        } else {
            fArr = new float[]{0.0f, 1.0f};
        }
        float[] fArr2 = z ? new float[]{0.0f, this.mContentTransY} : new float[]{-this.mContentTransY, 0.0f};
        if (this.mLauncher.isInState(LauncherState.ALL_APPS)) {
            final AllAppsContainerView appsView = this.mLauncher.getAppsView();
            final float alpha = appsView.getAlpha();
            final float translationY = appsView.getTranslationY();
            appsView.setAlpha(fArr[0]);
            appsView.setTranslationY(fArr2[0]);
            ObjectAnimator ofFloat = ObjectAnimator.ofFloat(appsView, View.ALPHA, fArr);
            ofFloat.setDuration(217L);
            ofFloat.setInterpolator(Interpolators.LINEAR);
            appsView.setLayerType(2, null);
            ofFloat.addListener(new AnimatorListenerAdapter() { // from class: com.android.launcher3.LauncherAppTransitionManagerImpl.5
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    appsView.setLayerType(0, null);
                }
            });
            ObjectAnimator ofFloat2 = ObjectAnimator.ofFloat(appsView, View.TRANSLATION_Y, fArr2);
            ofFloat2.setInterpolator(Interpolators.AGGRESSIVE_EASE);
            ofFloat2.setDuration(350L);
            animatorSet.play(ofFloat);
            animatorSet.play(ofFloat2);
            runnable = new Runnable() { // from class: com.android.launcher3.-$$Lambda$LauncherAppTransitionManagerImpl$b-WVfYAQuY7K2hhmZ7JZtscWUZM
                @Override // java.lang.Runnable
                public final void run() {
                    LauncherAppTransitionManagerImpl.lambda$getLauncherContentAnimator$1(appsView, alpha, translationY);
                }
            };
        } else if (this.mLauncher.isInState(LauncherState.OVERVIEW)) {
            AllAppsTransitionController allAppsController = this.mLauncher.getAllAppsController();
            animatorSet.play(ObjectAnimator.ofFloat(allAppsController, AllAppsTransitionController.ALL_APPS_PROGRESS, allAppsController.getProgress(), 1.3059858f));
            final View overviewPanelContainer = this.mLauncher.getOverviewPanelContainer();
            ObjectAnimator ofFloat3 = ObjectAnimator.ofFloat(overviewPanelContainer, View.ALPHA, fArr);
            ofFloat3.setDuration(217L);
            ofFloat3.setInterpolator(Interpolators.LINEAR);
            animatorSet.play(ofFloat3);
            ObjectAnimator ofFloat4 = ObjectAnimator.ofFloat(overviewPanelContainer, View.TRANSLATION_Y, fArr2);
            ofFloat4.setInterpolator(Interpolators.AGGRESSIVE_EASE);
            ofFloat4.setDuration(350L);
            animatorSet.play(ofFloat4);
            overviewPanelContainer.setLayerType(2, null);
            runnable = new Runnable() { // from class: com.android.launcher3.-$$Lambda$LauncherAppTransitionManagerImpl$5joujdgfS6qraD-6emJ6YJoRCGo
                @Override // java.lang.Runnable
                public final void run() {
                    LauncherAppTransitionManagerImpl.lambda$getLauncherContentAnimator$2(LauncherAppTransitionManagerImpl.this, overviewPanelContainer);
                }
            };
        } else {
            this.mDragLayerAlpha.setValue(fArr[0]);
            ObjectAnimator ofFloat5 = ObjectAnimator.ofFloat(this.mDragLayerAlpha, MultiValueAlpha.VALUE, fArr);
            ofFloat5.setDuration(217L);
            ofFloat5.setInterpolator(Interpolators.LINEAR);
            animatorSet.play(ofFloat5);
            this.mDragLayer.setTranslationY(fArr2[0]);
            ObjectAnimator ofFloat6 = ObjectAnimator.ofFloat(this.mDragLayer, View.TRANSLATION_Y, fArr2);
            ofFloat6.setInterpolator(Interpolators.AGGRESSIVE_EASE);
            ofFloat6.setDuration(350L);
            animatorSet.play(ofFloat6);
            this.mDragLayer.getScrim().hideSysUiScrim(true);
            this.mLauncher.getWorkspace().getPageIndicator().pauseAnimations();
            this.mDragLayer.setLayerType(2, null);
            runnable = new Runnable() { // from class: com.android.launcher3.-$$Lambda$LauncherAppTransitionManagerImpl$ENtSMyc93RRzOSFM9n0F1vfO_B0
                @Override // java.lang.Runnable
                public final void run() {
                    LauncherAppTransitionManagerImpl.this.resetContentView();
                }
            };
        }
        return new Pair<>(animatorSet, runnable);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ void lambda$getLauncherContentAnimator$1(View view, float f, float f2) {
        view.setAlpha(f);
        view.setTranslationY(f2);
        view.setLayerType(0, null);
    }

    public static /* synthetic */ void lambda$getLauncherContentAnimator$2(LauncherAppTransitionManagerImpl launcherAppTransitionManagerImpl, View view) {
        view.setLayerType(0, null);
        view.setAlpha(1.0f);
        view.setTranslationY(0.0f);
        launcherAppTransitionManagerImpl.mLauncher.getStateManager().reapplyState();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public AnimatorSet getIconAnimator(final View view, Rect rect) {
        int i;
        float marginStart;
        boolean z = view instanceof BubbleTextView;
        this.mFloatingView = new View(this.mLauncher);
        if (z && (view.getTag() instanceof ItemInfoWithIcon)) {
            this.mFloatingView.setBackground(DrawableFactory.get(this.mLauncher).newIcon((ItemInfoWithIcon) view.getTag()));
        }
        Rect rect2 = new Rect();
        boolean z2 = view.getParent() instanceof DeepShortcutView;
        if (z2) {
            this.mDragLayer.getDescendantRectRelativeToSelf(((DeepShortcutView) view.getParent()).getIconView(), rect2);
        } else {
            this.mDragLayer.getDescendantRectRelativeToSelf(view, rect2);
        }
        int i2 = rect2.left;
        int i3 = rect2.top;
        float f = 1.0f;
        if (!z || z2) {
            rect2.set(0, 0, rect2.width(), rect2.height());
        } else {
            BubbleTextView bubbleTextView = (BubbleTextView) view;
            bubbleTextView.getIconBounds(rect2);
            Drawable icon = bubbleTextView.getIcon();
            if (icon instanceof FastBitmapDrawable) {
                f = ((FastBitmapDrawable) icon).getAnimatedScale();
            }
        }
        int i4 = i2 + rect2.left;
        int i5 = i3 + rect2.top;
        if (this.mIsRtl) {
            i = rect.width() - rect2.right;
        } else {
            i = i4;
        }
        InsettableFrameLayout.LayoutParams layoutParams = new InsettableFrameLayout.LayoutParams(rect2.width(), rect2.height());
        layoutParams.ignoreInsets = true;
        layoutParams.setMarginStart(i);
        layoutParams.topMargin = i5;
        this.mFloatingView.setLayoutParams(layoutParams);
        this.mFloatingView.setLeft(i4);
        this.mFloatingView.setTop(i5);
        this.mFloatingView.setRight(i4 + rect2.width());
        this.mFloatingView.setBottom(i5 + rect2.height());
        ((ViewGroup) this.mDragLayer.getParent()).addView(this.mFloatingView);
        view.setVisibility(4);
        AnimatorSet animatorSet = new AnimatorSet();
        int[] iArr = new int[2];
        this.mDragLayer.getLocationOnScreen(iArr);
        float centerX = rect.centerX() - iArr[0];
        float centerY = rect.centerY() - iArr[1];
        if (this.mIsRtl) {
            marginStart = (rect.width() - layoutParams.getMarginStart()) - rect2.width();
        } else {
            marginStart = layoutParams.getMarginStart();
        }
        float f2 = (centerX - marginStart) - (layoutParams.width / 2);
        float f3 = (centerY - layoutParams.topMargin) - (layoutParams.height / 2);
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(this.mFloatingView, View.TRANSLATION_X, 0.0f, f2);
        ObjectAnimator ofFloat2 = ObjectAnimator.ofFloat(this.mFloatingView, View.TRANSLATION_Y, 0.0f, f3);
        boolean z3 = ((float) layoutParams.topMargin) > centerY || Math.abs(f3) < ((float) this.mLauncher.getDeviceProfile().cellHeightPx);
        if (z3) {
            ofFloat.setDuration(250L);
            ofFloat2.setDuration(500L);
        } else {
            ofFloat.setDuration(400L);
            ofFloat2.setDuration(200L);
        }
        ofFloat.setInterpolator(Interpolators.AGGRESSIVE_EASE);
        ofFloat2.setInterpolator(Interpolators.AGGRESSIVE_EASE);
        animatorSet.play(ofFloat);
        animatorSet.play(ofFloat2);
        ObjectAnimator ofFloat3 = ObjectAnimator.ofFloat(this.mFloatingView, LauncherAnimUtils.SCALE_PROPERTY, f, Math.max(rect.width() / rect2.width(), rect.height() / rect2.height()));
        ofFloat3.setDuration(500L).setInterpolator(Interpolators.EXAGGERATED_EASE);
        animatorSet.play(ofFloat3);
        ObjectAnimator ofFloat4 = ObjectAnimator.ofFloat(this.mFloatingView, View.ALPHA, 1.0f, 0.0f);
        if (z3) {
            ofFloat4.setStartDelay(32L);
            ofFloat4.setDuration(50L);
        } else {
            ofFloat4.setStartDelay(25L);
            ofFloat4.setDuration(40L);
        }
        ofFloat4.setInterpolator(Interpolators.LINEAR);
        animatorSet.play(ofFloat4);
        animatorSet.addListener(new AnimatorListenerAdapter() { // from class: com.android.launcher3.LauncherAppTransitionManagerImpl.6
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                view.setVisibility(0);
                ((ViewGroup) LauncherAppTransitionManagerImpl.this.mDragLayer.getParent()).removeView(LauncherAppTransitionManagerImpl.this.mFloatingView);
            }
        });
        return animatorSet;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public ValueAnimator getOpeningWindowAnimators(View view, final RemoteAnimationTargetCompat[] remoteAnimationTargetCompatArr, final Rect rect) {
        final Rect rect2 = new Rect();
        if (view.getParent() instanceof DeepShortcutView) {
            this.mDragLayer.getDescendantRectRelativeToSelf(((DeepShortcutView) view.getParent()).getIconView(), rect2);
        } else if (view instanceof BubbleTextView) {
            ((BubbleTextView) view).getIconBounds(rect2);
        } else {
            this.mDragLayer.getDescendantRectRelativeToSelf(view, rect2);
        }
        final int[] iArr = new int[2];
        final Rect rect3 = new Rect();
        final Matrix matrix = new Matrix();
        ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        ofFloat.setDuration(500L);
        ofFloat.addUpdateListener(new MultiValueUpdateListener() { // from class: com.android.launcher3.LauncherAppTransitionManagerImpl.7
            MultiValueUpdateListener.FloatProp mAlpha = new MultiValueUpdateListener.FloatProp(0.0f, 1.0f, 0.0f, 60.0f, Interpolators.LINEAR);
            boolean isFirstFrame = true;

            @Override // com.android.quickstep.util.MultiValueUpdateListener
            public void onUpdate(float f) {
                int height;
                RemoteAnimationTargetCompat[] remoteAnimationTargetCompatArr2;
                Surface surface = com.android.systemui.shared.recents.utilities.Utilities.getSurface(LauncherAppTransitionManagerImpl.this.mFloatingView);
                if ((surface != null ? com.android.systemui.shared.recents.utilities.Utilities.getNextFrameNumber(surface) : -1L) == -1) {
                    Log.w(LauncherAppTransitionManagerImpl.TAG, "Failed to animate, surface got destroyed.");
                    return;
                }
                float interpolation = Interpolators.AGGRESSIVE_EASE.getInterpolation(f);
                float width = rect2.width() * LauncherAppTransitionManagerImpl.this.mFloatingView.getScaleX();
                float height2 = rect2.height() * LauncherAppTransitionManagerImpl.this.mFloatingView.getScaleY();
                float min = Math.min(1.0f, Math.min(width / rect.width(), height2 / rect.height()));
                matrix.setScale(min, min);
                int width2 = rect.width();
                float f2 = width2;
                float height3 = rect.height();
                LauncherAppTransitionManagerImpl.this.mFloatingView.getLocationOnScreen(iArr);
                matrix.postTranslate(iArr[0] - (((f2 * min) - width) / 2.0f), iArr[1] - (((min * height3) - height2) / 2.0f));
                float f3 = 1.0f - interpolation;
                rect3.left = 0;
                rect3.top = (int) (((height - width2) / 2.0f) * f3);
                rect3.right = width2;
                rect3.bottom = (int) (rect3.top + (height3 * interpolation) + (f2 * f3));
                TransactionCompat transactionCompat = new TransactionCompat();
                if (this.isFirstFrame) {
                    RemoteAnimationProvider.prepareTargetsForFirstFrame(remoteAnimationTargetCompatArr, transactionCompat, 0);
                    this.isFirstFrame = false;
                }
                for (RemoteAnimationTargetCompat remoteAnimationTargetCompat : remoteAnimationTargetCompatArr) {
                    if (remoteAnimationTargetCompat.mode == 0) {
                        transactionCompat.setAlpha(remoteAnimationTargetCompat.leash, this.mAlpha.value);
                        transactionCompat.setMatrix(remoteAnimationTargetCompat.leash, matrix);
                        transactionCompat.setWindowCrop(remoteAnimationTargetCompat.leash, rect3);
                        transactionCompat.deferTransactionUntil(remoteAnimationTargetCompat.leash, surface, com.android.systemui.shared.recents.utilities.Utilities.getNextFrameNumber(surface));
                    }
                }
                transactionCompat.setEarlyWakeup();
                transactionCompat.apply();
                matrix.reset();
            }
        });
        return ofFloat;
    }

    private void registerRemoteAnimations() {
        if (hasControlRemoteAppTransitionPermission()) {
            RemoteAnimationDefinitionCompat remoteAnimationDefinitionCompat = new RemoteAnimationDefinitionCompat();
            remoteAnimationDefinitionCompat.addRemoteAnimation(13, 1, new RemoteAnimationAdapterCompat(getWallpaperOpenRunner(), 250L, 0L));
            new ActivityCompat(this.mLauncher).registerRemoteAnimations(remoteAnimationDefinitionCompat);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean launcherIsATargetWithMode(RemoteAnimationTargetCompat[] remoteAnimationTargetCompatArr, int i) {
        return TaskUtils.taskIsATargetWithMode(remoteAnimationTargetCompatArr, this.mLauncher.getTaskId(), i);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.android.launcher3.LauncherAppTransitionManagerImpl$8  reason: invalid class name */
    /* loaded from: classes.dex */
    public class AnonymousClass8 extends LauncherAnimationRunner {
        AnonymousClass8(Handler handler, boolean z) {
            super(handler, z);
        }

        @Override // com.android.launcher3.LauncherAnimationRunner
        public void onCreateAnimation(final RemoteAnimationTargetCompat[] remoteAnimationTargetCompatArr, final LauncherAnimationRunner.AnimationResult animationResult) {
            if (!LauncherAppTransitionManagerImpl.this.mLauncher.hasBeenResumed()) {
                LauncherAppTransitionManagerImpl.this.mLauncher.setOnResumeCallback(new Launcher.OnResumeCallback() { // from class: com.android.launcher3.-$$Lambda$LauncherAppTransitionManagerImpl$8$kAnTPiu41D0fuaoxlPfkV_Q5SmE
                    @Override // com.android.launcher3.Launcher.OnResumeCallback
                    public final void onLauncherResume() {
                        Utilities.postAsyncCallback(LauncherAppTransitionManagerImpl.this.mHandler, new Runnable() { // from class: com.android.launcher3.-$$Lambda$LauncherAppTransitionManagerImpl$8$GQmbGoe81_q3EVbQ25_-MYD2OhM
                            @Override // java.lang.Runnable
                            public final void run() {
                                LauncherAppTransitionManagerImpl.AnonymousClass8.this.onCreateAnimation(r2, r3);
                            }
                        });
                    }
                });
                return;
            }
            AnimatorSet animatorSet = null;
            RemoteAnimationProvider remoteAnimationProvider = LauncherAppTransitionManagerImpl.this.mRemoteAnimationProvider;
            if (remoteAnimationProvider != null) {
                animatorSet = remoteAnimationProvider.createWindowAnimation(remoteAnimationTargetCompatArr);
            }
            if (animatorSet == null) {
                animatorSet = new AnimatorSet();
                animatorSet.play(LauncherAppTransitionManagerImpl.this.getClosingWindowAnimators(remoteAnimationTargetCompatArr));
                if (LauncherAppTransitionManagerImpl.this.launcherIsATargetWithMode(remoteAnimationTargetCompatArr, 0) || LauncherAppTransitionManagerImpl.this.mLauncher.isForceInvisible()) {
                    LauncherAppTransitionManagerImpl.this.mLauncher.getStateManager().setCurrentAnimation(animatorSet, new Animator[0]);
                    LauncherAppTransitionManagerImpl.this.createLauncherResumeAnimation(animatorSet);
                }
            }
            LauncherAppTransitionManagerImpl.this.mLauncher.clearForceInvisibleFlag(3);
            animationResult.setAnimation(animatorSet);
        }
    }

    private RemoteAnimationRunnerCompat getWallpaperOpenRunner() {
        return new AnonymousClass8(this.mHandler, false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Animator getClosingWindowAnimators(final RemoteAnimationTargetCompat[] remoteAnimationTargetCompatArr) {
        final Matrix matrix = new Matrix();
        ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        ofFloat.setDuration(250);
        ofFloat.addUpdateListener(new MultiValueUpdateListener() { // from class: com.android.launcher3.LauncherAppTransitionManagerImpl.9
            MultiValueUpdateListener.FloatProp mDy;
            MultiValueUpdateListener.FloatProp mScale;
            MultiValueUpdateListener.FloatProp mAlpha = new MultiValueUpdateListener.FloatProp(1.0f, 0.0f, 25.0f, 125.0f, Interpolators.LINEAR);
            boolean isFirstFrame = true;

            {
                this.mDy = new MultiValueUpdateListener.FloatProp(0.0f, LauncherAppTransitionManagerImpl.this.mClosingWindowTransY, 0.0f, r10, Interpolators.DEACCEL_1_7);
                this.mScale = new MultiValueUpdateListener.FloatProp(1.0f, 1.0f, 0.0f, r10, Interpolators.DEACCEL_1_7);
            }

            @Override // com.android.quickstep.util.MultiValueUpdateListener
            public void onUpdate(float f) {
                RemoteAnimationTargetCompat[] remoteAnimationTargetCompatArr2;
                TransactionCompat transactionCompat = new TransactionCompat();
                if (this.isFirstFrame) {
                    RemoteAnimationProvider.prepareTargetsForFirstFrame(remoteAnimationTargetCompatArr, transactionCompat, 1);
                    this.isFirstFrame = false;
                }
                for (RemoteAnimationTargetCompat remoteAnimationTargetCompat : remoteAnimationTargetCompatArr) {
                    if (remoteAnimationTargetCompat.mode == 1) {
                        transactionCompat.setAlpha(remoteAnimationTargetCompat.leash, this.mAlpha.value);
                        matrix.setScale(this.mScale.value, this.mScale.value, remoteAnimationTargetCompat.sourceContainerBounds.centerX(), remoteAnimationTargetCompat.sourceContainerBounds.centerY());
                        matrix.postTranslate(0.0f, this.mDy.value);
                        matrix.postTranslate(remoteAnimationTargetCompat.position.x, remoteAnimationTargetCompat.position.y);
                        transactionCompat.setMatrix(remoteAnimationTargetCompat.leash, matrix);
                    }
                }
                transactionCompat.setEarlyWakeup();
                transactionCompat.apply();
                matrix.reset();
            }
        });
        return ofFloat;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void createLauncherResumeAnimation(AnimatorSet animatorSet) {
        if (this.mLauncher.isInState(LauncherState.ALL_APPS)) {
            final Pair<AnimatorSet, Runnable> launcherContentAnimator = getLauncherContentAnimator(false);
            ((AnimatorSet) launcherContentAnimator.first).setStartDelay(100L);
            animatorSet.play((Animator) launcherContentAnimator.first);
            animatorSet.addListener(new AnimatorListenerAdapter() { // from class: com.android.launcher3.LauncherAppTransitionManagerImpl.10
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    ((Runnable) launcherContentAnimator.second).run();
                }
            });
            return;
        }
        AnimatorSet animatorSet2 = new AnimatorSet();
        this.mDragLayer.setTranslationY(-this.mWorkspaceTransY);
        animatorSet2.play(ObjectAnimator.ofFloat(this.mDragLayer, View.TRANSLATION_Y, -this.mWorkspaceTransY, 0.0f));
        this.mDragLayerAlpha.setValue(0.0f);
        animatorSet2.play(ObjectAnimator.ofFloat(this.mDragLayerAlpha, MultiValueAlpha.VALUE, 0.0f, 1.0f));
        animatorSet2.setStartDelay(100L);
        animatorSet2.setDuration(333L);
        animatorSet2.setInterpolator(Interpolators.DEACCEL_1_7);
        this.mDragLayer.getScrim().hideSysUiScrim(true);
        this.mLauncher.getWorkspace().getPageIndicator().pauseAnimations();
        this.mDragLayer.setLayerType(2, null);
        animatorSet2.addListener(new AnimatorListenerAdapter() { // from class: com.android.launcher3.LauncherAppTransitionManagerImpl.11
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                LauncherAppTransitionManagerImpl.this.resetContentView();
            }
        });
        animatorSet.play(animatorSet2);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void resetContentView() {
        this.mLauncher.getWorkspace().getPageIndicator().skipAnimationsToEnd();
        this.mDragLayerAlpha.setValue(1.0f);
        this.mDragLayer.setLayerType(0, null);
        this.mDragLayer.setTranslationY(0.0f);
        this.mDragLayer.getScrim().hideSysUiScrim(false);
    }

    private boolean hasControlRemoteAppTransitionPermission() {
        return this.mLauncher.checkSelfPermission(CONTROL_REMOTE_APP_TRANSITION_PERMISSION) == 0;
    }
}
