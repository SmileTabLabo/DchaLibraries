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
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.Interpolators;
import com.android.systemui.Prefs;
import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.events.activity.CancelEnterRecentsWindowAnimationEvent;
import com.android.systemui.recents.events.activity.ConfigurationChangedEvent;
import com.android.systemui.recents.events.activity.DebugFlagsChangedEvent;
import com.android.systemui.recents.events.activity.DismissRecentsToHomeAnimationStarted;
import com.android.systemui.recents.events.activity.DockedFirstAnimationFrameEvent;
import com.android.systemui.recents.events.activity.DockedTopTaskEvent;
import com.android.systemui.recents.events.activity.EnterRecentsWindowAnimationCompletedEvent;
import com.android.systemui.recents.events.activity.EnterRecentsWindowLastAnimationFrameEvent;
import com.android.systemui.recents.events.activity.ExitRecentsWindowFirstAnimationFrameEvent;
import com.android.systemui.recents.events.activity.HideRecentsEvent;
import com.android.systemui.recents.events.activity.IterateRecentsEvent;
import com.android.systemui.recents.events.activity.LaunchTaskFailedEvent;
import com.android.systemui.recents.events.activity.LaunchTaskSucceededEvent;
import com.android.systemui.recents.events.activity.MultiWindowStateChangedEvent;
import com.android.systemui.recents.events.activity.ToggleRecentsEvent;
import com.android.systemui.recents.events.component.RecentsVisibilityChangedEvent;
import com.android.systemui.recents.events.component.ScreenPinningRequestEvent;
import com.android.systemui.recents.events.ui.AllTaskViewsDismissedEvent;
import com.android.systemui.recents.events.ui.DeleteTaskDataEvent;
import com.android.systemui.recents.events.ui.HideIncompatibleAppOverlayEvent;
import com.android.systemui.recents.events.ui.RecentsDrawnEvent;
import com.android.systemui.recents.events.ui.ShowApplicationInfoEvent;
import com.android.systemui.recents.events.ui.ShowIncompatibleAppOverlayEvent;
import com.android.systemui.recents.events.ui.StackViewScrolledEvent;
import com.android.systemui.recents.events.ui.UpdateFreeformTaskViewVisibilityEvent;
import com.android.systemui.recents.events.ui.UserInteractionEvent;
import com.android.systemui.recents.events.ui.focus.DismissFocusedTaskViewEvent;
import com.android.systemui.recents.events.ui.focus.FocusNextTaskViewEvent;
import com.android.systemui.recents.events.ui.focus.FocusPreviousTaskViewEvent;
import com.android.systemui.recents.misc.DozeTrigger;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.misc.Utilities;
import com.android.systemui.recents.model.RecentsPackageMonitor;
import com.android.systemui.recents.model.RecentsTaskLoadPlan;
import com.android.systemui.recents.model.RecentsTaskLoader;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.model.TaskStack;
import com.android.systemui.recents.views.RecentsView;
import com.android.systemui.recents.views.SystemBarScrimViews;
import java.io.FileDescriptor;
import java.io.PrintWriter;
/* loaded from: a.zip:com/android/systemui/recents/RecentsActivity.class */
public class RecentsActivity extends Activity implements ViewTreeObserver.OnPreDrawListener {
    private boolean mFinishedOnStartup;
    private int mFocusTimerDuration;
    private Intent mHomeIntent;
    private boolean mIgnoreAltTabRelease;
    private View mIncompatibleAppOverlay;
    private boolean mIsVisible;
    private DozeTrigger mIterateTrigger;
    private int mLastDisplayDensity;
    private long mLastTabKeyEventTime;
    private RecentsPackageMonitor mPackageMonitor;
    private boolean mReceivedNewIntent;
    private RecentsView mRecentsView;
    private SystemBarScrimViews mScrimViews;
    private Handler mHandler = new Handler();
    private int mLastDeviceOrientation = 0;
    private final UserInteractionEvent mUserInteractionEvent = new UserInteractionEvent();
    private final Runnable mSendEnterWindowAnimationCompleteRunnable = new Runnable() { // from class: com.android.systemui.recents.RecentsActivity._void__init___LambdaImpl0
        @Override // java.lang.Runnable
        public void run() {
            RecentsActivity.m1229com_android_systemui_recents_RecentsActivity_lambda$2();
        }
    };
    final BroadcastReceiver mSystemBroadcastReceiver = new BroadcastReceiver(this) { // from class: com.android.systemui.recents.RecentsActivity.1
        final RecentsActivity this$0;

        {
            this.this$0 = this;
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.intent.action.SCREEN_OFF")) {
                this.this$0.dismissRecentsToHomeIfVisible(false);
            } else if (action.equals("android.intent.action.TIME_SET")) {
                Prefs.putLong(this.this$0, "OverviewLastStackTaskActiveTime", 0L);
            }
        }
    };
    private final ViewTreeObserver.OnPreDrawListener mRecentsDrawnEventListener = new ViewTreeObserver.OnPreDrawListener(this) { // from class: com.android.systemui.recents.RecentsActivity.2
        final RecentsActivity this$0;

        {
            this.this$0 = this;
        }

        @Override // android.view.ViewTreeObserver.OnPreDrawListener
        public boolean onPreDraw() {
            this.this$0.mRecentsView.getViewTreeObserver().removeOnPreDrawListener(this);
            EventBus.getDefault().post(new RecentsDrawnEvent());
            return true;
        }
    };

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: a.zip:com/android/systemui/recents/RecentsActivity$LaunchHomeRunnable.class */
    public class LaunchHomeRunnable implements Runnable {
        Intent mLaunchIntent;
        ActivityOptions mOpts;
        final RecentsActivity this$0;

        public LaunchHomeRunnable(RecentsActivity recentsActivity, Intent intent, ActivityOptions activityOptions) {
            this.this$0 = recentsActivity;
            this.mLaunchIntent = intent;
            this.mOpts = activityOptions;
        }

        /* renamed from: -com_android_systemui_recents_RecentsActivity$LaunchHomeRunnable_lambda$1  reason: not valid java name */
        /* synthetic */ void m1232x203d731f() {
            ActivityOptions activityOptions = this.mOpts;
            ActivityOptions activityOptions2 = activityOptions;
            if (activityOptions == null) {
                activityOptions2 = ActivityOptions.makeCustomAnimation(this.this$0, 2131034298, 2131034299);
            }
            this.this$0.startActivityAsUser(this.mLaunchIntent, activityOptions2.toBundle(), UserHandle.CURRENT);
        }

        @Override // java.lang.Runnable
        public void run() {
            try {
                this.this$0.mHandler.post(new Runnable(this) { // from class: com.android.systemui.recents.RecentsActivity.LaunchHomeRunnable._void_run__LambdaImpl0
                    private LaunchHomeRunnable val$this;

                    {
                        this.val$this = this;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        this.val$this.m1232x203d731f();
                    }
                });
            } catch (Exception e) {
                Log.e("RecentsActivity", this.this$0.getString(2131493584, new Object[]{"Home"}), e);
            }
        }
    }

    /* renamed from: -com_android_systemui_recents_RecentsActivity_lambda$2  reason: not valid java name */
    static /* synthetic */ void m1229com_android_systemui_recents_RecentsActivity_lambda$2() {
        EventBus.getDefault().send(new EnterRecentsWindowAnimationCompletedEvent());
    }

    private void reloadStackView() {
        RecentsTaskLoader taskLoader = Recents.getTaskLoader();
        RecentsTaskLoadPlan consumeInstanceLoadPlan = RecentsImpl.consumeInstanceLoadPlan();
        RecentsTaskLoadPlan recentsTaskLoadPlan = consumeInstanceLoadPlan;
        if (consumeInstanceLoadPlan == null) {
            recentsTaskLoadPlan = taskLoader.createLoadPlan(this);
        }
        RecentsActivityLaunchState launchState = Recents.getConfiguration().getLaunchState();
        if (!recentsTaskLoadPlan.hasTasks()) {
            taskLoader.preloadTasks(recentsTaskLoadPlan, launchState.launchedToTaskId, !launchState.launchedFromHome);
        }
        RecentsTaskLoadPlan.Options options = new RecentsTaskLoadPlan.Options();
        options.runningTaskId = launchState.launchedToTaskId;
        options.numVisibleTasks = launchState.launchedNumVisibleTasks;
        options.numVisibleTaskThumbnails = launchState.launchedNumVisibleThumbnails;
        taskLoader.loadTasks(this, recentsTaskLoadPlan, options);
        TaskStack taskStack = recentsTaskLoadPlan.getTaskStack();
        this.mRecentsView.onReload(this.mIsVisible, taskStack.getTaskCount() == 0);
        this.mRecentsView.updateStack(taskStack, true);
        this.mScrimViews.updateNavBarScrim(!launchState.launchedViaDockGesture, taskStack.getTaskCount() > 0, null);
        if (!launchState.launchedFromHome ? !launchState.launchedFromApp : false) {
            EventBus.getDefault().send(new EnterRecentsWindowAnimationCompletedEvent());
        }
        if (launchState.launchedWithAltTab) {
            MetricsLogger.count(this, "overview_trigger_alttab", 1);
        } else {
            MetricsLogger.count(this, "overview_trigger_nav_btn", 1);
        }
        if (launchState.launchedFromApp) {
            Task launchTarget = taskStack.getLaunchTarget();
            int indexOfStackTask = launchTarget != null ? taskStack.indexOfStackTask(launchTarget) : 0;
            MetricsLogger.count(this, "overview_source_app", 1);
            MetricsLogger.histogram(this, "overview_source_app_index", indexOfStackTask);
        } else {
            MetricsLogger.count(this, "overview_source_home", 1);
        }
        MetricsLogger.histogram(this, "overview_task_count", this.mRecentsView.getStack().getTaskCount());
        this.mIsVisible = true;
    }

    boolean dismissRecentsToFocusedTask(int i) {
        return Recents.getSystemServices().isRecentsActivityVisible() && this.mRecentsView.launchFocusedTask(i);
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
        dismissRecentsToHomeAnimationStarted.addPostAnimationCallback(new LaunchHomeRunnable(this, this.mHomeIntent, activityOptions));
        Recents.getSystemServices().sendCloseSystemWindows("homekey");
        EventBus.getDefault().send(dismissRecentsToHomeAnimationStarted);
    }

    boolean dismissRecentsToHomeIfVisible(boolean z) {
        if (Recents.getSystemServices().isRecentsActivityVisible()) {
            dismissRecentsToHome(z);
            return true;
        }
        return false;
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
        printWriter.print(" [0x");
        printWriter.print(hexString);
        printWriter.print("]");
        printWriter.println();
        if (this.mRecentsView != null) {
            this.mRecentsView.dump(str, printWriter);
        }
    }

    @Override // android.app.Activity, android.view.Window.Callback
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        EventBus.getDefault().register(this.mScrimViews, 2);
    }

    @Override // android.app.Activity
    public void onBackPressed() {
        EventBus.getDefault().send(new ToggleRecentsEvent());
    }

    public final void onBusEvent(CancelEnterRecentsWindowAnimationEvent cancelEnterRecentsWindowAnimationEvent) {
        RecentsActivityLaunchState launchState = Recents.getConfiguration().getLaunchState();
        int i = launchState.launchedToTaskId;
        if (i != -1) {
            if (cancelEnterRecentsWindowAnimationEvent.launchTask == null || i != cancelEnterRecentsWindowAnimationEvent.launchTask.key.id) {
                SystemServicesProxy systemServices = Recents.getSystemServices();
                systemServices.cancelWindowTransition(launchState.launchedToTaskId);
                systemServices.cancelThumbnailTransition(getTaskId());
            }
        }
    }

    public final void onBusEvent(DebugFlagsChangedEvent debugFlagsChangedEvent) {
        finish();
    }

    public final void onBusEvent(DockedFirstAnimationFrameEvent dockedFirstAnimationFrameEvent) {
        this.mRecentsView.getViewTreeObserver().addOnPreDrawListener(this);
        this.mRecentsView.invalidate();
    }

    public final void onBusEvent(DockedTopTaskEvent dockedTopTaskEvent) {
        this.mRecentsView.getViewTreeObserver().addOnPreDrawListener(this.mRecentsDrawnEventListener);
        this.mRecentsView.invalidate();
    }

    public final void onBusEvent(EnterRecentsWindowLastAnimationFrameEvent enterRecentsWindowLastAnimationFrameEvent) {
        EventBus.getDefault().send(new UpdateFreeformTaskViewVisibilityEvent(true));
        this.mRecentsView.getViewTreeObserver().addOnPreDrawListener(this);
        this.mRecentsView.invalidate();
    }

    public final void onBusEvent(ExitRecentsWindowFirstAnimationFrameEvent exitRecentsWindowFirstAnimationFrameEvent) {
        if (this.mRecentsView.isLastTaskLaunchedFreeform()) {
            EventBus.getDefault().send(new UpdateFreeformTaskViewVisibilityEvent(false));
        }
        this.mRecentsView.getViewTreeObserver().addOnPreDrawListener(this);
        this.mRecentsView.invalidate();
    }

    public final void onBusEvent(HideRecentsEvent hideRecentsEvent) {
        if (hideRecentsEvent.triggeredFromAltTab) {
            if (this.mIgnoreAltTabRelease) {
                return;
            }
            dismissRecentsToFocusedTaskOrHome();
        } else if (hideRecentsEvent.triggeredFromHomeKey) {
            dismissRecentsToHome(true);
            EventBus.getDefault().send(this.mUserInteractionEvent);
        }
    }

    public final void onBusEvent(IterateRecentsEvent iterateRecentsEvent) {
        int i = 0;
        if (Recents.getDebugFlags().isFastToggleRecentsEnabled()) {
            i = getResources().getInteger(2131755068);
            this.mIterateTrigger.setDozeDuration(i);
            if (this.mIterateTrigger.isDozing()) {
                this.mIterateTrigger.poke();
            } else {
                this.mIterateTrigger.startDozing();
            }
        }
        EventBus.getDefault().send(new FocusNextTaskViewEvent(i));
        MetricsLogger.action(this, 276);
    }

    public final void onBusEvent(LaunchTaskFailedEvent launchTaskFailedEvent) {
        dismissRecentsToHome(true);
        MetricsLogger.count(this, "overview_task_launch_failed", 1);
    }

    public final void onBusEvent(LaunchTaskSucceededEvent launchTaskSucceededEvent) {
        MetricsLogger.histogram(this, "overview_task_launch_index", launchTaskSucceededEvent.taskIndexFromStackFront);
    }

    public final void onBusEvent(ToggleRecentsEvent toggleRecentsEvent) {
        if (Recents.getConfiguration().getLaunchState().launchedFromHome) {
            dismissRecentsToHome(true);
        } else {
            dismissRecentsToLaunchTargetTaskOrHome();
        }
    }

    public final void onBusEvent(ScreenPinningRequestEvent screenPinningRequestEvent) {
        MetricsLogger.count(this, "overview_screen_pinned", 1);
    }

    public final void onBusEvent(AllTaskViewsDismissedEvent allTaskViewsDismissedEvent) {
        if (Recents.getSystemServices().hasDockedTask()) {
            this.mRecentsView.showEmptyView(allTaskViewsDismissedEvent.msgResId);
        } else {
            dismissRecentsToHome(false);
        }
        MetricsLogger.count(this, "overview_task_all_dismissed", 1);
    }

    public final void onBusEvent(DeleteTaskDataEvent deleteTaskDataEvent) {
        Recents.getTaskLoader().deleteTaskData(deleteTaskDataEvent.task, false);
        Recents.getSystemServices().removeTask(deleteTaskDataEvent.task.key.id);
    }

    public final void onBusEvent(HideIncompatibleAppOverlayEvent hideIncompatibleAppOverlayEvent) {
        if (this.mIncompatibleAppOverlay != null) {
            this.mIncompatibleAppOverlay.animate().alpha(0.0f).setDuration(150L).setInterpolator(Interpolators.ALPHA_OUT).start();
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
            this.mIncompatibleAppOverlay = Utilities.findViewStubById(this, 2131886607).inflate();
            this.mIncompatibleAppOverlay.setWillNotDraw(false);
            this.mIncompatibleAppOverlay.setVisibility(0);
        }
        this.mIncompatibleAppOverlay.animate().alpha(1.0f).setDuration(150L).setInterpolator(Interpolators.ALPHA_IN).start();
    }

    public final void onBusEvent(StackViewScrolledEvent stackViewScrolledEvent) {
        this.mIgnoreAltTabRelease = true;
    }

    public final void onBusEvent(UserInteractionEvent userInteractionEvent) {
        this.mIterateTrigger.stopDozing();
    }

    @Override // android.app.Activity, android.content.ComponentCallbacks
    public void onConfigurationChanged(Configuration configuration) {
        boolean z = true;
        super.onConfigurationChanged(configuration);
        Configuration appConfiguration = Utilities.getAppConfiguration(this);
        int stackTaskCount = this.mRecentsView.getStack().getStackTaskCount();
        EventBus eventBus = EventBus.getDefault();
        boolean z2 = this.mLastDeviceOrientation != appConfiguration.orientation;
        boolean z3 = this.mLastDisplayDensity != appConfiguration.densityDpi;
        if (stackTaskCount <= 0) {
            z = false;
        }
        eventBus.send(new ConfigurationChangedEvent(false, z2, z3, z));
        this.mLastDeviceOrientation = appConfiguration.orientation;
        this.mLastDisplayDensity = appConfiguration.densityDpi;
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
        this.mPackageMonitor = new RecentsPackageMonitor();
        this.mPackageMonitor.register(this);
        setContentView(2130968776);
        takeKeyEvents(true);
        this.mRecentsView = (RecentsView) findViewById(2131886606);
        this.mRecentsView.setSystemUiVisibility(1792);
        this.mScrimViews = new SystemBarScrimViews(this);
        getWindow().getAttributes().privateFlags |= 16384;
        Configuration appConfiguration = Utilities.getAppConfiguration(this);
        this.mLastDeviceOrientation = appConfiguration.orientation;
        this.mLastDisplayDensity = appConfiguration.densityDpi;
        this.mFocusTimerDuration = getResources().getInteger(2131755067);
        this.mIterateTrigger = new DozeTrigger(this.mFocusTimerDuration, new Runnable(this) { // from class: com.android.systemui.recents.RecentsActivity.3
            final RecentsActivity this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.dismissRecentsToFocusedTask(288);
            }
        });
        getWindow().setBackgroundDrawable(this.mRecentsView.getBackgroundScrim());
        this.mHomeIntent = new Intent("android.intent.action.MAIN", (Uri) null);
        this.mHomeIntent.addCategory("android.intent.category.HOME");
        this.mHomeIntent.addFlags(270532608);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        intentFilter.addAction("android.intent.action.TIME_SET");
        registerReceiver(this.mSystemBroadcastReceiver, intentFilter);
        getWindow().addPrivateFlags(64);
        reloadStackView();
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
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this.mScrimViews);
    }

    @Override // android.app.Activity
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();
        this.mHandler.removeCallbacks(this.mSendEnterWindowAnimationCompleteRunnable);
        if (this.mReceivedNewIntent) {
            this.mSendEnterWindowAnimationCompleteRunnable.run();
        } else {
            this.mHandler.post(this.mSendEnterWindowAnimationCompleteRunnable);
        }
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    @Override // android.app.Activity, android.view.KeyEvent.Callback
    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        switch (i) {
            case 19:
                EventBus.getDefault().send(new FocusNextTaskViewEvent(0));
                return true;
            case 20:
                EventBus.getDefault().send(new FocusPreviousTaskViewEvent());
                return true;
            case 61:
                boolean z = SystemClock.elapsedRealtime() - this.mLastTabKeyEventTime > ((long) getResources().getInteger(2131755069));
                if (keyEvent.getRepeatCount() <= 0 || z) {
                    if (keyEvent.isShiftPressed()) {
                        EventBus.getDefault().send(new FocusPreviousTaskViewEvent());
                    } else {
                        EventBus.getDefault().send(new FocusNextTaskViewEvent(0));
                    }
                    this.mLastTabKeyEventTime = SystemClock.elapsedRealtime();
                    if (keyEvent.isAltPressed()) {
                        this.mIgnoreAltTabRelease = false;
                        return true;
                    }
                    return true;
                }
                return true;
            case 67:
            case 112:
                if (keyEvent.getRepeatCount() <= 0) {
                    EventBus.getDefault().send(new DismissFocusedTaskViewEvent());
                    MetricsLogger.histogram(this, "overview_task_dismissed_source", 0);
                    return true;
                }
                break;
        }
        return super.onKeyDown(i, keyEvent);
    }

    @Override // android.app.Activity
    public void onMultiWindowModeChanged(boolean z) {
        super.onMultiWindowModeChanged(z);
        RecentsActivityLaunchState launchState = Recents.getConfiguration().getLaunchState();
        RecentsTaskLoader taskLoader = Recents.getTaskLoader();
        RecentsTaskLoadPlan createLoadPlan = taskLoader.createLoadPlan(this);
        taskLoader.preloadTasks(createLoadPlan, -1, false);
        RecentsTaskLoadPlan.Options options = new RecentsTaskLoadPlan.Options();
        options.numVisibleTasks = launchState.launchedNumVisibleTasks;
        options.numVisibleTaskThumbnails = launchState.launchedNumVisibleThumbnails;
        taskLoader.loadTasks(this, createLoadPlan, options);
        TaskStack taskStack = createLoadPlan.getTaskStack();
        int stackTaskCount = taskStack.getStackTaskCount();
        boolean z2 = stackTaskCount > 0;
        EventBus.getDefault().send(new ConfigurationChangedEvent(true, false, false, stackTaskCount > 0));
        EventBus.getDefault().send(new MultiWindowStateChangedEvent(z, z2, taskStack));
    }

    @Override // android.app.Activity
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        this.mReceivedNewIntent = true;
        reloadStackView();
    }

    @Override // android.app.Activity
    protected void onPause() {
        super.onPause();
        this.mIgnoreAltTabRelease = false;
        this.mIterateTrigger.stopDozing();
    }

    @Override // android.view.ViewTreeObserver.OnPreDrawListener
    public boolean onPreDraw() {
        this.mRecentsView.getViewTreeObserver().removeOnPreDrawListener(this);
        this.mRecentsView.post(new Runnable(this) { // from class: com.android.systemui.recents.RecentsActivity.4
            final RecentsActivity this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                Recents.getSystemServices().endProlongedAnimations();
            }
        });
        return true;
    }

    @Override // android.app.Activity
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().send(new RecentsVisibilityChangedEvent(this, true));
        MetricsLogger.visible(this, 224);
        this.mRecentsView.getViewTreeObserver().addOnPreDrawListener(this.mRecentsDrawnEventListener);
    }

    @Override // android.app.Activity
    protected void onStop() {
        super.onStop();
        this.mIsVisible = false;
        this.mReceivedNewIntent = false;
        EventBus.getDefault().send(new RecentsVisibilityChangedEvent(this, false));
        MetricsLogger.hidden(this, 224);
        Recents.getConfiguration().getLaunchState().reset();
    }

    @Override // android.app.Activity, android.content.ComponentCallbacks2
    public void onTrimMemory(int i) {
        RecentsTaskLoader taskLoader = Recents.getTaskLoader();
        if (taskLoader != null) {
            taskLoader.onTrimMemory(i);
        }
    }

    @Override // android.app.Activity
    public void onUserInteraction() {
        EventBus.getDefault().send(this.mUserInteractionEvent);
    }
}
