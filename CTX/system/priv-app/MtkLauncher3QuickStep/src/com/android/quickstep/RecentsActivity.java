package com.android.quickstep;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import com.android.launcher3.AbstractFloatingView;
import com.android.launcher3.BaseDraggingActivity;
import com.android.launcher3.DeviceProfile;
import com.android.launcher3.InvariantDeviceProfile;
import com.android.launcher3.ItemInfo;
import com.android.launcher3.LauncherAnimationRunner;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.R;
import com.android.launcher3.anim.Interpolators;
import com.android.launcher3.badge.BadgeInfo;
import com.android.launcher3.uioverrides.UiFactory;
import com.android.launcher3.util.Themes;
import com.android.launcher3.views.BaseDragLayer;
import com.android.quickstep.fallback.FallbackRecentsView;
import com.android.quickstep.fallback.RecentsRootView;
import com.android.quickstep.util.ClipAnimationHelper;
import com.android.quickstep.views.RecentsViewContainer;
import com.android.quickstep.views.TaskView;
import com.android.systemui.shared.system.ActivityOptionsCompat;
import com.android.systemui.shared.system.RemoteAnimationAdapterCompat;
import com.android.systemui.shared.system.RemoteAnimationTargetCompat;
import java.io.FileDescriptor;
import java.io.PrintWriter;
/* loaded from: classes.dex */
public class RecentsActivity extends BaseDraggingActivity {
    private FallbackRecentsView mFallbackRecentsView;
    private Configuration mOldConfig;
    private RecentsViewContainer mOverviewPanelContainer;
    private RecentsRootView mRecentsRootView;
    private Handler mUiHandler = new Handler(Looper.getMainLooper());

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.launcher3.BaseDraggingActivity, android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mOldConfig = new Configuration(getResources().getConfiguration());
        initDeviceProfile();
        setContentView(R.layout.fallback_recents_activity);
        this.mRecentsRootView = (RecentsRootView) findViewById(R.id.drag_layer);
        this.mFallbackRecentsView = (FallbackRecentsView) findViewById(R.id.overview_panel);
        this.mOverviewPanelContainer = (RecentsViewContainer) findViewById(R.id.overview_panel_container);
        this.mRecentsRootView.setup();
        getSystemUiController().updateUiState(0, Themes.getAttrBoolean(this, R.attr.isWorkspaceDarkText));
        RecentsActivityTracker.onRecentsActivityCreate(this);
    }

    @Override // android.app.Activity, android.content.ComponentCallbacks
    public void onConfigurationChanged(Configuration configuration) {
        if ((configuration.diff(this.mOldConfig) & 1152) != 0) {
            onHandleConfigChanged();
        }
        this.mOldConfig.setTo(configuration);
        super.onConfigurationChanged(configuration);
    }

    @Override // com.android.launcher3.BaseActivity, android.app.Activity
    public void onMultiWindowModeChanged(boolean z, Configuration configuration) {
        onHandleConfigChanged();
        super.onMultiWindowModeChanged(z, configuration);
    }

    public void onRootViewSizeChanged() {
        if (isInMultiWindowModeCompat()) {
            onHandleConfigChanged();
        }
    }

    private void onHandleConfigChanged() {
        this.mUserEventDispatcher = null;
        initDeviceProfile();
        AbstractFloatingView.closeOpenViews(this, true, 399);
        dispatchDeviceProfileChanged();
        this.mRecentsRootView.setup();
        reapplyUi();
    }

    @Override // com.android.launcher3.BaseDraggingActivity
    protected void reapplyUi() {
        this.mRecentsRootView.dispatchInsets();
    }

    private void initDeviceProfile() {
        DeviceProfile copy;
        LauncherAppState instanceNoCreate = LauncherAppState.getInstanceNoCreate();
        if (isInMultiWindowModeCompat()) {
            DeviceProfile deviceProfile = (instanceNoCreate == null ? new InvariantDeviceProfile(this) : instanceNoCreate.getInvariantDeviceProfile()).getDeviceProfile(this);
            this.mDeviceProfile = this.mRecentsRootView == null ? deviceProfile.copy(this) : deviceProfile.getMultiWindowProfile(this, this.mRecentsRootView.getLastKnownSize());
        } else {
            if (instanceNoCreate == null) {
                copy = new InvariantDeviceProfile(this).getDeviceProfile(this);
            } else {
                copy = instanceNoCreate.getInvariantDeviceProfile().getDeviceProfile(this).copy(this);
            }
            this.mDeviceProfile = copy;
        }
        onDeviceProfileInitiated();
    }

    @Override // com.android.launcher3.BaseDraggingActivity
    public BaseDragLayer getDragLayer() {
        return this.mRecentsRootView;
    }

    @Override // com.android.launcher3.BaseDraggingActivity
    public View getRootView() {
        return this.mRecentsRootView;
    }

    @Override // com.android.launcher3.BaseDraggingActivity
    public <T extends View> T getOverviewPanel() {
        return this.mFallbackRecentsView;
    }

    public RecentsViewContainer getOverviewPanelContainer() {
        return this.mOverviewPanelContainer;
    }

    @Override // com.android.launcher3.BaseDraggingActivity
    public BadgeInfo getBadgeInfoForItem(ItemInfo itemInfo) {
        return null;
    }

    @Override // com.android.launcher3.BaseDraggingActivity
    public ActivityOptions getActivityLaunchOptions(View view) {
        if (!(view instanceof TaskView)) {
            return null;
        }
        final TaskView taskView = (TaskView) view;
        return ActivityOptionsCompat.makeRemoteAnimation(new RemoteAnimationAdapterCompat(new LauncherAnimationRunner(this.mUiHandler, true) { // from class: com.android.quickstep.RecentsActivity.1
            @Override // com.android.launcher3.LauncherAnimationRunner
            public void onCreateAnimation(RemoteAnimationTargetCompat[] remoteAnimationTargetCompatArr, LauncherAnimationRunner.AnimationResult animationResult) {
                animationResult.setAnimation(RecentsActivity.this.composeRecentsLaunchAnimator(taskView, remoteAnimationTargetCompatArr));
            }
        }, 336L, 216L));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public AnimatorSet composeRecentsLaunchAnimator(TaskView taskView, RemoteAnimationTargetCompat[] remoteAnimationTargetCompatArr) {
        AnimatorSet animatorSet = new AnimatorSet();
        boolean taskIsATargetWithMode = TaskUtils.taskIsATargetWithMode(remoteAnimationTargetCompatArr, getTaskId(), 1);
        ClipAnimationHelper clipAnimationHelper = new ClipAnimationHelper();
        animatorSet.play(TaskUtils.getRecentsWindowAnimator(taskView, !taskIsATargetWithMode, remoteAnimationTargetCompatArr, clipAnimationHelper).setDuration(336L));
        if (taskIsATargetWithMode) {
            AnimatorSet createAdjacentPageAnimForTaskLaunch = this.mFallbackRecentsView.createAdjacentPageAnimForTaskLaunch(taskView, clipAnimationHelper);
            createAdjacentPageAnimForTaskLaunch.setInterpolator(Interpolators.TOUCH_RESPONSE_INTERPOLATOR);
            createAdjacentPageAnimForTaskLaunch.setDuration(336L);
            createAdjacentPageAnimForTaskLaunch.addListener(new AnimatorListenerAdapter() { // from class: com.android.quickstep.RecentsActivity.2
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    RecentsActivity.this.mFallbackRecentsView.resetTaskVisuals();
                }
            });
            animatorSet.play(createAdjacentPageAnimForTaskLaunch);
        }
        return animatorSet;
    }

    @Override // com.android.launcher3.BaseDraggingActivity
    public void invalidateParent(ItemInfo itemInfo) {
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.launcher3.BaseDraggingActivity, com.android.launcher3.BaseActivity, android.app.Activity
    public void onStart() {
        this.mFallbackRecentsView.setContentAlpha(1.0f);
        super.onStart();
        UiFactory.onStart(this);
        this.mFallbackRecentsView.resetTaskVisuals();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.launcher3.BaseActivity, android.app.Activity
    public void onStop() {
        super.onStop();
        onTrimMemory(20);
    }

    @Override // android.app.Activity, android.content.ComponentCallbacks2
    public void onTrimMemory(int i) {
        super.onTrimMemory(i);
        UiFactory.onTrimMemory(this, i);
    }

    @Override // android.app.Activity
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        RecentsActivityTracker.onRecentsActivityNewIntent(this);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.launcher3.BaseDraggingActivity, android.app.Activity
    public void onDestroy() {
        super.onDestroy();
        RecentsActivityTracker.onRecentsActivityDestroy(this);
    }

    @Override // android.app.Activity
    public void onBackPressed() {
        startHome();
    }

    public void startHome() {
        startActivity(new Intent("android.intent.action.MAIN").addCategory("android.intent.category.HOME").setFlags(268435456));
    }

    @Override // com.android.launcher3.BaseActivity, android.app.Activity
    public void dump(String str, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        super.dump(str, fileDescriptor, printWriter, strArr);
        printWriter.println(str + "Misc:");
        dumpMisc(printWriter);
    }
}
