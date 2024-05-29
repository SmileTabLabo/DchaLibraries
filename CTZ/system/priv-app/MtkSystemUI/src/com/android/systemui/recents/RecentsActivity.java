package com.android.systemui.recents;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.BenesseExtension;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import com.android.internal.colorextraction.ColorExtractor;
import com.android.internal.content.PackageMonitor;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.util.LatencyTracker;
import com.android.systemui.DejankUtils;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.colorextraction.SysuiColorExtractor;
import com.android.systemui.recents.RecentsActivity;
import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.events.activity.CancelEnterRecentsWindowAnimationEvent;
import com.android.systemui.recents.events.activity.ConfigurationChangedEvent;
import com.android.systemui.recents.events.activity.DismissRecentsToHomeAnimationStarted;
import com.android.systemui.recents.events.activity.DockedFirstAnimationFrameEvent;
import com.android.systemui.recents.events.activity.DockedTopTaskEvent;
import com.android.systemui.recents.events.activity.EnterRecentsWindowAnimationCompletedEvent;
import com.android.systemui.recents.events.activity.EnterRecentsWindowLastAnimationFrameEvent;
import com.android.systemui.recents.events.activity.ExitRecentsWindowFirstAnimationFrameEvent;
import com.android.systemui.recents.events.activity.HideRecentsEvent;
import com.android.systemui.recents.events.activity.LaunchTaskFailedEvent;
import com.android.systemui.recents.events.activity.LaunchTaskSucceededEvent;
import com.android.systemui.recents.events.activity.MultiWindowStateChangedEvent;
import com.android.systemui.recents.events.activity.PackagesChangedEvent;
import com.android.systemui.recents.events.activity.RecentsActivityStartingEvent;
import com.android.systemui.recents.events.activity.ToggleRecentsEvent;
import com.android.systemui.recents.events.component.ActivityUnpinnedEvent;
import com.android.systemui.recents.events.component.RecentsVisibilityChangedEvent;
import com.android.systemui.recents.events.component.ScreenPinningRequestEvent;
import com.android.systemui.recents.events.component.SetWaitingForTransitionStartEvent;
import com.android.systemui.recents.events.ui.AllTaskViewsDismissedEvent;
import com.android.systemui.recents.events.ui.DeleteTaskDataEvent;
import com.android.systemui.recents.events.ui.HideIncompatibleAppOverlayEvent;
import com.android.systemui.recents.events.ui.RecentsDrawnEvent;
import com.android.systemui.recents.events.ui.ShowApplicationInfoEvent;
import com.android.systemui.recents.events.ui.ShowIncompatibleAppOverlayEvent;
import com.android.systemui.recents.events.ui.StackViewScrolledEvent;
import com.android.systemui.recents.events.ui.TaskViewDismissedEvent;
import com.android.systemui.recents.events.ui.UserInteractionEvent;
import com.android.systemui.recents.events.ui.focus.DismissFocusedTaskViewEvent;
import com.android.systemui.recents.events.ui.focus.FocusNextTaskViewEvent;
import com.android.systemui.recents.events.ui.focus.FocusPreviousTaskViewEvent;
import com.android.systemui.recents.events.ui.focus.NavigateTaskViewEvent;
import com.android.systemui.recents.views.RecentsView;
import com.android.systemui.recents.views.SystemBarScrimViews;
import com.android.systemui.shared.recents.model.RecentsTaskLoadPlan;
import com.android.systemui.shared.recents.model.RecentsTaskLoader;
import com.android.systemui.shared.recents.model.Task;
import com.android.systemui.shared.recents.model.TaskStack;
import com.android.systemui.shared.recents.utilities.Utilities;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import java.io.FileDescriptor;
import java.io.PrintWriter;
/* loaded from: classes.dex */
public class RecentsActivity extends Activity implements ViewTreeObserver.OnPreDrawListener, ColorExtractor.OnColorsChangedListener {
    private SysuiColorExtractor mColorExtractor;
    private boolean mFinishedOnStartup;
    private Intent mHomeIntent;
    private boolean mIgnoreAltTabRelease;
    private View mIncompatibleAppOverlay;
    private boolean mIsVisible;
    private Configuration mLastConfig;
    private long mLastTabKeyEventTime;
    private boolean mRecentsStartRequested;
    private RecentsView mRecentsView;
    private SystemBarScrimViews mScrimViews;
    private boolean mUsingDarkText;
    private PackageMonitor mPackageMonitor = new PackageMonitor() { // from class: com.android.systemui.recents.RecentsActivity.1
        public void onPackageRemoved(String str, int i) {
            RecentsActivity.this.onPackageChanged(str, getChangingUserId());
        }

        public boolean onPackageChanged(String str, int i, String[] strArr) {
            RecentsActivity.this.onPackageChanged(str, getChangingUserId());
            return true;
        }

        public void onPackageModified(String str) {
            RecentsActivity.this.onPackageChanged(str, getChangingUserId());
        }
    };
    private Handler mHandler = new Handler();
    private final UserInteractionEvent mUserInteractionEvent = new UserInteractionEvent();
    final BroadcastReceiver mSystemBroadcastReceiver = new BroadcastReceiver() { // from class: com.android.systemui.recents.RecentsActivity.2
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.intent.action.SCREEN_OFF")) {
                RecentsActivity.this.dismissRecentsToHomeIfVisible(false);
            } else if (action.equals("android.intent.action.USER_SWITCHED")) {
                RecentsActivity.this.finish();
            }
        }
    };
    private final ViewTreeObserver.OnPreDrawListener mRecentsDrawnEventListener = new AnonymousClass3();

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public class LaunchHomeRunnable implements Runnable {
        Intent mLaunchIntent;
        ActivityOptions mOpts;

        public LaunchHomeRunnable(Intent intent, ActivityOptions activityOptions) {
            this.mLaunchIntent = intent;
            this.mOpts = activityOptions;
        }

        @Override // java.lang.Runnable
        public void run() {
            try {
                RecentsActivity.this.mHandler.post(new Runnable() { // from class: com.android.systemui.recents.-$$Lambda$RecentsActivity$LaunchHomeRunnable$K3jCoKVe41-EkTmSKH7i98xFt8k
                    @Override // java.lang.Runnable
                    public final void run() {
                        RecentsActivity.LaunchHomeRunnable.lambda$run$0(RecentsActivity.LaunchHomeRunnable.this);
                    }
                });
            } catch (Exception e) {
                Log.e("RecentsActivity", RecentsActivity.this.getString(R.string.recents_launch_error_message, new Object[]{"Home"}), e);
            }
        }

        public static /* synthetic */ void lambda$run$0(LaunchHomeRunnable launchHomeRunnable) {
            ActivityOptions activityOptions = launchHomeRunnable.mOpts;
            if (activityOptions == null) {
                activityOptions = ActivityOptions.makeCustomAnimation(RecentsActivity.this, R.anim.recents_to_launcher_enter, R.anim.recents_to_launcher_exit);
            }
            RecentsActivity.this.startActivityAsUser(launchHomeRunnable.mLaunchIntent, activityOptions.toBundle(), UserHandle.CURRENT);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.android.systemui.recents.RecentsActivity$3  reason: invalid class name */
    /* loaded from: classes.dex */
    public class AnonymousClass3 implements ViewTreeObserver.OnPreDrawListener {
        AnonymousClass3() {
        }

        @Override // android.view.ViewTreeObserver.OnPreDrawListener
        public boolean onPreDraw() {
            RecentsActivity.this.mRecentsView.getViewTreeObserver().removeOnPreDrawListener(this);
            EventBus.getDefault().post(new RecentsDrawnEvent());
            if (LatencyTracker.isEnabled(RecentsActivity.this.getApplicationContext())) {
                DejankUtils.postAfterTraversal(new Runnable() { // from class: com.android.systemui.recents.-$$Lambda$RecentsActivity$3$yqqbbfyTHFuJpHT3gETj09GBDFY
                    @Override // java.lang.Runnable
                    public final void run() {
                        LatencyTracker.getInstance(RecentsActivity.this.getApplicationContext()).onActionEnd(1);
                    }
                });
            }
            DejankUtils.postAfterTraversal(new Runnable() { // from class: com.android.systemui.recents.-$$Lambda$RecentsActivity$3$SXW_26Z-B_jvwD3qSXfVNpRowHM
                @Override // java.lang.Runnable
                public final void run() {
                    RecentsActivity.AnonymousClass3.lambda$onPreDraw$1(RecentsActivity.AnonymousClass3.this);
                }
            });
            return true;
        }

        public static /* synthetic */ void lambda$onPreDraw$1(AnonymousClass3 anonymousClass3) {
            Recents.getTaskLoader().startLoader(RecentsActivity.this);
            Recents.getTaskLoader().getHighResThumbnailLoader().setVisible(true);
        }
    }

    boolean dismissRecentsToLaunchTargetTaskOrHome() {
        if (Recents.getSystemServices().isRecentsActivityVisible()) {
            if (this.mRecentsView.launchPreviousTask()) {
                return true;
            }
            dismissRecentsToHome(true);
            return false;
        }
        return false;
    }

    boolean dismissRecentsToFocusedTaskOrHome() {
        if (Recents.getSystemServices().isRecentsActivityVisible()) {
            if (this.mRecentsView.launchFocusedTask(0)) {
                return true;
            }
            dismissRecentsToHome(true);
            return true;
        }
        return false;
    }

    void dismissRecentsToHome(boolean z) {
        dismissRecentsToHome(z, null);
    }

    void dismissRecentsToHome(boolean z, ActivityOptions activityOptions) {
        DismissRecentsToHomeAnimationStarted dismissRecentsToHomeAnimationStarted = new DismissRecentsToHomeAnimationStarted(z);
        dismissRecentsToHomeAnimationStarted.addPostAnimationCallback(new LaunchHomeRunnable(this.mHomeIntent, activityOptions));
        ActivityManagerWrapper.getInstance().closeSystemWindows("homekey");
        EventBus.getDefault().send(dismissRecentsToHomeAnimationStarted);
    }

    boolean dismissRecentsToHomeIfVisible(boolean z) {
        if (Recents.getSystemServices().isRecentsActivityVisible()) {
            dismissRecentsToHome(z);
            return true;
        }
        return false;
    }

    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mFinishedOnStartup = false;
        if (Recents.getSystemServices() == null) {
            this.mFinishedOnStartup = true;
            finish();
            return;
        }
        EventBus.getDefault().register(this, 2);
        this.mPackageMonitor.register(this, Looper.getMainLooper(), UserHandle.ALL, true);
        this.mColorExtractor = (SysuiColorExtractor) Dependency.get(SysuiColorExtractor.class);
        this.mColorExtractor.addOnColorsChangedListener(this);
        this.mUsingDarkText = this.mColorExtractor.getColors(1, 1, true).supportsDarkText();
        setTheme(this.mUsingDarkText ? com.android.systemui.plugins.R.style.RecentsTheme_Wallpaper_Light : com.android.systemui.plugins.R.style.RecentsTheme_Wallpaper);
        setContentView(R.layout.recents);
        takeKeyEvents(true);
        this.mRecentsView = (RecentsView) findViewById(R.id.recents_view);
        this.mScrimViews = new SystemBarScrimViews(this);
        getWindow().getAttributes().privateFlags |= 16384;
        if (Recents.getConfiguration().isLowRamDevice) {
            getWindow().addFlags(1024);
        }
        this.mLastConfig = new Configuration(Utilities.getAppConfiguration(this));
        this.mRecentsView.updateBackgroundScrim(getWindow(), isInMultiWindowMode());
        this.mHomeIntent = new Intent("android.intent.action.MAIN", (Uri) null);
        this.mHomeIntent.addCategory("android.intent.category.HOME");
        this.mHomeIntent.addFlags(270532608);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        intentFilter.addAction("android.intent.action.USER_SWITCHED");
        registerReceiver(this.mSystemBroadcastReceiver, intentFilter);
        getWindow().addPrivateFlags(64);
    }

    @Override // android.app.Activity
    protected void onStart() {
        super.onStart();
        reloadStackView();
        EventBus.getDefault().send(new RecentsVisibilityChangedEvent(this, true));
        MetricsLogger.visible(this, 224);
        this.mRecentsView.setScrimColors(this.mColorExtractor.getColors(1, 1, true), false);
        this.mRecentsView.getViewTreeObserver().addOnPreDrawListener(this.mRecentsDrawnEventListener);
        Object lastNonConfigurationInstance = getLastNonConfigurationInstance();
        if (lastNonConfigurationInstance != null && (lastNonConfigurationInstance instanceof Boolean) && ((Boolean) lastNonConfigurationInstance).booleanValue()) {
            RecentsActivityLaunchState launchState = Recents.getConfiguration().getLaunchState();
            launchState.launchedViaDockGesture = false;
            launchState.launchedFromApp = false;
            launchState.launchedFromHome = false;
            onEnterAnimationComplete();
        }
        this.mRecentsStartRequested = false;
    }

    public void onColorsChanged(ColorExtractor colorExtractor, int i) {
        if ((i & 1) != 0) {
            ColorExtractor.GradientColors colors = this.mColorExtractor.getColors(1, 1, true);
            boolean supportsDarkText = colors.supportsDarkText();
            if (supportsDarkText != this.mUsingDarkText) {
                this.mUsingDarkText = supportsDarkText;
                setTheme(this.mUsingDarkText ? com.android.systemui.plugins.R.style.RecentsTheme_Wallpaper_Light : com.android.systemui.plugins.R.style.RecentsTheme_Wallpaper);
                this.mRecentsView.reevaluateStyles();
            }
            this.mRecentsView.setScrimColors(colors, true);
        }
    }

    private void reloadStackView() {
        RecentsTaskLoader taskLoader = Recents.getTaskLoader();
        RecentsTaskLoadPlan consumeInstanceLoadPlan = RecentsImpl.consumeInstanceLoadPlan();
        if (consumeInstanceLoadPlan == null) {
            consumeInstanceLoadPlan = new RecentsTaskLoadPlan(this);
        }
        RecentsActivityLaunchState launchState = Recents.getConfiguration().getLaunchState();
        if (!consumeInstanceLoadPlan.hasTasks()) {
            taskLoader.preloadTasks(consumeInstanceLoadPlan, launchState.launchedToTaskId);
        }
        RecentsTaskLoadPlan.Options options = new RecentsTaskLoadPlan.Options();
        options.runningTaskId = launchState.launchedToTaskId;
        options.numVisibleTasks = launchState.launchedNumVisibleTasks;
        options.numVisibleTaskThumbnails = launchState.launchedNumVisibleThumbnails;
        taskLoader.loadTasks(consumeInstanceLoadPlan, options);
        TaskStack taskStack = consumeInstanceLoadPlan.getTaskStack();
        this.mRecentsView.onReload(taskStack, this.mIsVisible);
        this.mScrimViews.updateNavBarScrim(!launchState.launchedViaDockGesture, taskStack.getTaskCount() > 0, null);
        if ((launchState.launchedFromHome || launchState.launchedFromApp) ? false : true) {
            EventBus.getDefault().send(new EnterRecentsWindowAnimationCompletedEvent());
        }
        if (launchState.launchedWithAltTab) {
            MetricsLogger.count(this, "overview_trigger_alttab", 1);
        } else {
            MetricsLogger.count(this, "overview_trigger_nav_btn", 1);
        }
        if (!launchState.launchedFromApp) {
            MetricsLogger.count(this, "overview_source_home", 1);
        } else {
            Task launchTarget = taskStack.getLaunchTarget();
            int indexOfTask = launchTarget != null ? taskStack.indexOfTask(launchTarget) : 0;
            MetricsLogger.count(this, "overview_source_app", 1);
            MetricsLogger.histogram(this, "overview_source_app_index", indexOfTask);
        }
        MetricsLogger.histogram(this, "overview_task_count", this.mRecentsView.getStack().getTaskCount());
        this.mIsVisible = true;
    }

    @Override // android.app.Activity
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();
        EventBus.getDefault().send(new EnterRecentsWindowAnimationCompletedEvent());
        EventBus.getDefault().send(new SetWaitingForTransitionStartEvent(false));
    }

    @Override // android.app.Activity
    public Object onRetainNonConfigurationInstance() {
        return true;
    }

    @Override // android.app.Activity
    protected void onPause() {
        super.onPause();
        this.mIgnoreAltTabRelease = false;
    }

    @Override // android.app.Activity, android.content.ComponentCallbacks
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        Configuration appConfiguration = Utilities.getAppConfiguration(this);
        EventBus.getDefault().send(new ConfigurationChangedEvent(false, this.mLastConfig.orientation != appConfiguration.orientation, this.mLastConfig.densityDpi != appConfiguration.densityDpi, this.mRecentsView.getStack().getTaskCount() > 0));
        this.mLastConfig.updateFrom(appConfiguration);
    }

    @Override // android.app.Activity
    public void onMultiWindowModeChanged(boolean z) {
        super.onMultiWindowModeChanged(z);
        this.mRecentsView.updateBackgroundScrim(getWindow(), z);
        if (this.mIsVisible) {
            reloadTaskStack(z, true);
        }
    }

    @Override // android.app.Activity
    protected void onStop() {
        super.onStop();
        this.mIsVisible = false;
        EventBus.getDefault().send(new RecentsVisibilityChangedEvent(this, false));
        MetricsLogger.hidden(this, 224);
        Recents.getTaskLoader().getHighResThumbnailLoader().setVisible(false);
        if (!isChangingConfigurations() && !this.mRecentsStartRequested) {
            Recents.getConfiguration().getLaunchState().reset();
        }
        Recents.getSystemServices().gc();
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
        if (this.mFinishedOnStartup) {
            return;
        }
        unregisterReceiver(this.mSystemBroadcastReceiver);
        this.mPackageMonitor.unregister();
        EventBus.getDefault().unregister(this);
    }

    @Override // android.app.Activity, android.view.Window.Callback
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        EventBus.getDefault().register(this.mScrimViews, 2);
    }

    @Override // android.app.Activity, android.view.Window.Callback
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this.mScrimViews);
    }

    @Override // android.app.Activity, android.content.ComponentCallbacks2
    public void onTrimMemory(int i) {
        RecentsTaskLoader taskLoader = Recents.getTaskLoader();
        if (taskLoader != null) {
            taskLoader.onTrimMemory(i);
        }
    }

    @Override // android.app.Activity, android.view.KeyEvent.Callback
    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        if (i == 61) {
            boolean z = SystemClock.elapsedRealtime() - this.mLastTabKeyEventTime > ((long) getResources().getInteger(R.integer.recents_alt_tab_key_delay));
            if (keyEvent.getRepeatCount() <= 0 || z) {
                if (keyEvent.isShiftPressed()) {
                    EventBus.getDefault().send(new FocusPreviousTaskViewEvent());
                } else {
                    EventBus.getDefault().send(new FocusNextTaskViewEvent());
                }
                this.mLastTabKeyEventTime = SystemClock.elapsedRealtime();
                if (keyEvent.isAltPressed()) {
                    this.mIgnoreAltTabRelease = false;
                }
            }
            return true;
        }
        if (i != 67 && i != 112) {
            switch (i) {
                case 19:
                case 20:
                case 21:
                case 22:
                    EventBus.getDefault().send(new NavigateTaskViewEvent(NavigateTaskViewEvent.getDirectionFromKeyCode(i)));
                    return true;
            }
        } else if (keyEvent.getRepeatCount() <= 0) {
            EventBus.getDefault().send(new DismissFocusedTaskViewEvent());
            MetricsLogger.histogram(this, "overview_task_dismissed_source", 0);
            return true;
        }
        return super.onKeyDown(i, keyEvent);
    }

    @Override // android.app.Activity
    public void onUserInteraction() {
        EventBus.getDefault().send(this.mUserInteractionEvent);
    }

    @Override // android.app.Activity
    public void onBackPressed() {
        EventBus.getDefault().send(new ToggleRecentsEvent());
    }

    public final void onBusEvent(ToggleRecentsEvent toggleRecentsEvent) {
        if (Recents.getConfiguration().getLaunchState().launchedFromHome) {
            dismissRecentsToHome(true);
        } else {
            dismissRecentsToLaunchTargetTaskOrHome();
        }
    }

    public final void onBusEvent(RecentsActivityStartingEvent recentsActivityStartingEvent) {
        this.mRecentsStartRequested = true;
    }

    public final void onBusEvent(HideRecentsEvent hideRecentsEvent) {
        if (hideRecentsEvent.triggeredFromAltTab) {
            if (!this.mIgnoreAltTabRelease) {
                dismissRecentsToFocusedTaskOrHome();
            }
        } else if (hideRecentsEvent.triggeredFromHomeKey) {
            dismissRecentsToHome(true);
            EventBus.getDefault().send(this.mUserInteractionEvent);
        }
    }

    public final void onBusEvent(EnterRecentsWindowLastAnimationFrameEvent enterRecentsWindowLastAnimationFrameEvent) {
        this.mRecentsView.getViewTreeObserver().addOnPreDrawListener(this);
        this.mRecentsView.invalidate();
    }

    public final void onBusEvent(ExitRecentsWindowFirstAnimationFrameEvent exitRecentsWindowFirstAnimationFrameEvent) {
        this.mRecentsView.getViewTreeObserver().addOnPreDrawListener(this);
        this.mRecentsView.invalidate();
    }

    public final void onBusEvent(DockedFirstAnimationFrameEvent dockedFirstAnimationFrameEvent) {
        this.mRecentsView.getViewTreeObserver().addOnPreDrawListener(this);
        this.mRecentsView.invalidate();
    }

    public final void onBusEvent(CancelEnterRecentsWindowAnimationEvent cancelEnterRecentsWindowAnimationEvent) {
        RecentsActivityLaunchState launchState = Recents.getConfiguration().getLaunchState();
        int i = launchState.launchedToTaskId;
        if (i != -1) {
            if (cancelEnterRecentsWindowAnimationEvent.launchTask == null || i != cancelEnterRecentsWindowAnimationEvent.launchTask.key.id) {
                ActivityManagerWrapper.getInstance().cancelWindowTransition(launchState.launchedToTaskId);
            }
        }
    }

    public final void onBusEvent(ShowApplicationInfoEvent showApplicationInfoEvent) {
        if (BenesseExtension.getDchaState() != 0) {
            return;
        }
        Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS", Uri.fromParts("package", showApplicationInfoEvent.task.key.getComponent().getPackageName(), null));
        intent.setComponent(intent.resolveActivity(getPackageManager()));
        TaskStackBuilder.create(this).addNextIntentWithParentStack(intent).startActivities(null, new UserHandle(showApplicationInfoEvent.task.key.userId));
        MetricsLogger.count(this, "overview_app_info", 1);
    }

    public final void onBusEvent(ShowIncompatibleAppOverlayEvent showIncompatibleAppOverlayEvent) {
        if (this.mIncompatibleAppOverlay == null) {
            this.mIncompatibleAppOverlay = Utilities.findViewStubById(this, (int) R.id.incompatible_app_overlay_stub).inflate();
            this.mIncompatibleAppOverlay.setWillNotDraw(false);
            this.mIncompatibleAppOverlay.setVisibility(0);
        }
        this.mIncompatibleAppOverlay.animate().alpha(1.0f).setDuration(150L).setInterpolator(Interpolators.ALPHA_IN).start();
    }

    public final void onBusEvent(HideIncompatibleAppOverlayEvent hideIncompatibleAppOverlayEvent) {
        if (this.mIncompatibleAppOverlay != null) {
            this.mIncompatibleAppOverlay.animate().alpha(0.0f).setDuration(150L).setInterpolator(Interpolators.ALPHA_OUT).start();
        }
    }

    public final void onBusEvent(DeleteTaskDataEvent deleteTaskDataEvent) {
        Recents.getTaskLoader().deleteTaskData(deleteTaskDataEvent.task, false);
        ActivityManagerWrapper.getInstance().removeTask(deleteTaskDataEvent.task.key.id);
    }

    public final void onBusEvent(TaskViewDismissedEvent taskViewDismissedEvent) {
        this.mRecentsView.updateScrimOpacity();
    }

    public final void onBusEvent(AllTaskViewsDismissedEvent allTaskViewsDismissedEvent) {
        if (Recents.getSystemServices().hasDockedTask()) {
            this.mRecentsView.showEmptyView(allTaskViewsDismissedEvent.msgResId);
        } else {
            dismissRecentsToHome(false);
        }
        MetricsLogger.count(this, "overview_task_all_dismissed", 1);
    }

    public final void onBusEvent(LaunchTaskSucceededEvent launchTaskSucceededEvent) {
        MetricsLogger.histogram(this, "overview_task_launch_index", launchTaskSucceededEvent.taskIndexFromStackFront);
    }

    public final void onBusEvent(LaunchTaskFailedEvent launchTaskFailedEvent) {
        dismissRecentsToHome(true);
        MetricsLogger.count(this, "overview_task_launch_failed", 1);
    }

    public final void onBusEvent(ScreenPinningRequestEvent screenPinningRequestEvent) {
        MetricsLogger.count(this, "overview_screen_pinned", 1);
    }

    public final void onBusEvent(StackViewScrolledEvent stackViewScrolledEvent) {
        this.mIgnoreAltTabRelease = true;
    }

    public final void onBusEvent(DockedTopTaskEvent dockedTopTaskEvent) {
        this.mRecentsView.getViewTreeObserver().addOnPreDrawListener(this.mRecentsDrawnEventListener);
        this.mRecentsView.invalidate();
    }

    public final void onBusEvent(ActivityUnpinnedEvent activityUnpinnedEvent) {
        if (this.mIsVisible) {
            reloadTaskStack(isInMultiWindowMode(), false);
        }
    }

    private void reloadTaskStack(boolean z, boolean z2) {
        RecentsActivityLaunchState launchState = Recents.getConfiguration().getLaunchState();
        RecentsTaskLoader taskLoader = Recents.getTaskLoader();
        RecentsTaskLoadPlan recentsTaskLoadPlan = new RecentsTaskLoadPlan(this);
        taskLoader.preloadTasks(recentsTaskLoadPlan, -1);
        RecentsTaskLoadPlan.Options options = new RecentsTaskLoadPlan.Options();
        options.numVisibleTasks = launchState.launchedNumVisibleTasks;
        options.numVisibleTaskThumbnails = launchState.launchedNumVisibleThumbnails;
        taskLoader.loadTasks(recentsTaskLoadPlan, options);
        TaskStack taskStack = recentsTaskLoadPlan.getTaskStack();
        int taskCount = taskStack.getTaskCount();
        boolean z3 = taskCount > 0;
        if (z2) {
            EventBus.getDefault().send(new ConfigurationChangedEvent(true, false, false, taskCount > 0));
        }
        EventBus.getDefault().send(new MultiWindowStateChangedEvent(z, z3, taskStack));
    }

    @Override // android.view.ViewTreeObserver.OnPreDrawListener
    public boolean onPreDraw() {
        this.mRecentsView.getViewTreeObserver().removeOnPreDrawListener(this);
        return true;
    }

    public void onPackageChanged(String str, int i) {
        Recents.getTaskLoader().onPackageChanged(str);
        EventBus.getDefault().send(new PackagesChangedEvent(str, i));
    }

    @Override // android.app.Activity
    public void dump(String str, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        super.dump(str, fileDescriptor, printWriter, strArr);
        EventBus.getDefault().dump(str, printWriter);
        Recents.getTaskLoader().dump(str, printWriter);
        String hexString = Integer.toHexString(System.identityHashCode(this));
        printWriter.print(str);
        printWriter.print("RecentsActivity");
        printWriter.print(" visible=");
        printWriter.print(this.mIsVisible ? "Y" : "N");
        printWriter.print(" currentTime=");
        printWriter.print(System.currentTimeMillis());
        printWriter.print(" [0x");
        printWriter.print(hexString);
        printWriter.print("]");
        printWriter.println();
        if (this.mRecentsView != null) {
            this.mRecentsView.dump(str, printWriter);
        }
    }
}
