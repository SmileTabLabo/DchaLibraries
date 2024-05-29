package com.android.systemui.recents.tv;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.RecentsActivityLaunchState;
import com.android.systemui.recents.RecentsImpl;
import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.events.activity.CancelEnterRecentsWindowAnimationEvent;
import com.android.systemui.recents.events.activity.DismissRecentsToHomeAnimationStarted;
import com.android.systemui.recents.events.activity.EnterRecentsWindowAnimationCompletedEvent;
import com.android.systemui.recents.events.activity.HideRecentsEvent;
import com.android.systemui.recents.events.activity.LaunchTaskFailedEvent;
import com.android.systemui.recents.events.activity.ToggleRecentsEvent;
import com.android.systemui.recents.events.component.RecentsVisibilityChangedEvent;
import com.android.systemui.recents.events.ui.AllTaskViewsDismissedEvent;
import com.android.systemui.recents.events.ui.DeleteTaskDataEvent;
import com.android.systemui.recents.events.ui.UserInteractionEvent;
import com.android.systemui.recents.events.ui.focus.DismissFocusedTaskViewEvent;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.model.RecentsPackageMonitor;
import com.android.systemui.recents.model.RecentsTaskLoadPlan;
import com.android.systemui.recents.model.RecentsTaskLoader;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.model.TaskStack;
import com.android.systemui.recents.tv.animations.HomeRecentsEnterExitAnimationHolder;
import com.android.systemui.recents.tv.views.RecentsTvView;
import com.android.systemui.recents.tv.views.TaskCardView;
import com.android.systemui.recents.tv.views.TaskStackHorizontalGridView;
import com.android.systemui.recents.tv.views.TaskStackHorizontalViewAdapter;
import com.android.systemui.tv.pip.PipManager;
import com.android.systemui.tv.pip.PipRecentsOverlayManager;
import java.util.ArrayList;
import java.util.Collections;
/* loaded from: a.zip:com/android/systemui/recents/tv/RecentsTvActivity.class */
public class RecentsTvActivity extends Activity implements ViewTreeObserver.OnPreDrawListener {
    private FinishRecentsRunnable mFinishLaunchHomeRunnable;
    private boolean mFinishedOnStartup;
    private HomeRecentsEnterExitAnimationHolder mHomeRecentsEnterExitAnimationHolder;
    private boolean mIgnoreAltTabRelease;
    private boolean mLaunchedFromHome;
    private RecentsPackageMonitor mPackageMonitor;
    private PipRecentsOverlayManager mPipRecentsOverlayManager;
    private View mPipView;
    private RecentsTvView mRecentsView;
    private boolean mTalkBackEnabled;
    private TaskStackHorizontalGridView mTaskStackHorizontalGridView;
    private TaskStackHorizontalViewAdapter mTaskStackViewAdapter;
    private final PipManager mPipManager = PipManager.getInstance();
    private final PipManager.Listener mPipListener = new PipManager.Listener(this) { // from class: com.android.systemui.recents.tv.RecentsTvActivity.1
        final RecentsTvActivity this$0;

        {
            this.this$0 = this;
        }

        @Override // com.android.systemui.tv.pip.PipManager.Listener
        public void onMoveToFullscreen() {
            this.this$0.dismissRecentsToLaunchTargetTaskOrHome(false);
        }

        @Override // com.android.systemui.tv.pip.PipManager.Listener
        public void onPipActivityClosed() {
            this.this$0.updatePipUI();
        }

        @Override // com.android.systemui.tv.pip.PipManager.Listener
        public void onPipEntered() {
            this.this$0.updatePipUI();
        }

        @Override // com.android.systemui.tv.pip.PipManager.Listener
        public void onPipResizeAboutToStart() {
        }

        @Override // com.android.systemui.tv.pip.PipManager.Listener
        public void onShowPipMenu() {
            this.this$0.updatePipUI();
        }
    };
    private final PipRecentsOverlayManager.Callback mPipRecentsOverlayManagerCallback = new PipRecentsOverlayManager.Callback(this) { // from class: com.android.systemui.recents.tv.RecentsTvActivity.2
        final RecentsTvActivity this$0;

        {
            this.this$0 = this;
        }

        @Override // com.android.systemui.tv.pip.PipRecentsOverlayManager.Callback
        public void onBackPressed() {
            this.this$0.onBackPressed();
        }

        @Override // com.android.systemui.tv.pip.PipRecentsOverlayManager.Callback
        public void onClosed() {
            this.this$0.dismissRecentsToLaunchTargetTaskOrHome(true);
        }

        @Override // com.android.systemui.tv.pip.PipRecentsOverlayManager.Callback
        public void onRecentsFocused() {
            if (this.this$0.mTalkBackEnabled) {
                this.this$0.mTaskStackHorizontalGridView.requestFocus();
                this.this$0.mTaskStackHorizontalGridView.sendAccessibilityEvent(8);
            }
            this.this$0.mTaskStackHorizontalGridView.startFocusGainAnimation();
        }
    };
    private final View.OnFocusChangeListener mPipViewFocusChangeListener = new View.OnFocusChangeListener(this) { // from class: com.android.systemui.recents.tv.RecentsTvActivity.3
        final RecentsTvActivity this$0;

        {
            this.this$0 = this;
        }

        @Override // android.view.View.OnFocusChangeListener
        public void onFocusChange(View view, boolean z) {
            if (z) {
                this.this$0.requestPipControlsFocus();
            }
        }
    };

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: a.zip:com/android/systemui/recents/tv/RecentsTvActivity$FinishRecentsRunnable.class */
    public class FinishRecentsRunnable implements Runnable {
        Intent mLaunchIntent;
        final RecentsTvActivity this$0;

        public FinishRecentsRunnable(RecentsTvActivity recentsTvActivity, Intent intent) {
            this.this$0 = recentsTvActivity;
            this.mLaunchIntent = intent;
        }

        @Override // java.lang.Runnable
        public void run() {
            try {
                this.this$0.startActivityAsUser(this.mLaunchIntent, ActivityOptions.makeCustomAnimation(this.this$0, 2131034298, 2131034299).toBundle(), UserHandle.CURRENT);
            } catch (Exception e) {
                Log.e("RecentsTvActivity", this.this$0.getString(2131493584, new Object[]{"Home"}), e);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updatePipUI() {
        if (this.mPipManager.isPipShown()) {
            Log.w("RecentsTvActivity", "An activity entered PIP mode while Recents is shown");
            return;
        }
        this.mPipRecentsOverlayManager.removePipRecentsOverlayView();
        this.mTaskStackHorizontalGridView.startFocusLossAnimation();
    }

    private void updateRecentsTasks() {
        RecentsTaskLoader taskLoader = Recents.getTaskLoader();
        RecentsTaskLoadPlan consumeInstanceLoadPlan = RecentsImpl.consumeInstanceLoadPlan();
        RecentsTaskLoadPlan recentsTaskLoadPlan = consumeInstanceLoadPlan;
        if (consumeInstanceLoadPlan == null) {
            recentsTaskLoadPlan = taskLoader.createLoadPlan(this);
        }
        RecentsActivityLaunchState launchState = Recents.getConfiguration().getLaunchState();
        if (!recentsTaskLoadPlan.hasTasks()) {
            taskLoader.preloadTasks(recentsTaskLoadPlan, -1, !launchState.launchedFromHome);
        }
        int numberOfVisibleTasks = TaskCardView.getNumberOfVisibleTasks(getApplicationContext());
        this.mLaunchedFromHome = launchState.launchedFromHome;
        TaskStack taskStack = recentsTaskLoadPlan.getTaskStack();
        RecentsTaskLoadPlan.Options options = new RecentsTaskLoadPlan.Options();
        options.runningTaskId = launchState.launchedToTaskId;
        options.numVisibleTasks = numberOfVisibleTasks;
        options.numVisibleTaskThumbnails = numberOfVisibleTasks;
        taskLoader.loadTasks(this, recentsTaskLoadPlan, options);
        ArrayList<Task> stackTasks = taskStack.getStackTasks();
        Collections.reverse(stackTasks);
        if (this.mTaskStackViewAdapter == null) {
            this.mTaskStackViewAdapter = new TaskStackHorizontalViewAdapter(stackTasks);
            this.mTaskStackHorizontalGridView = this.mRecentsView.setTaskStackViewAdapter(this.mTaskStackViewAdapter);
            this.mHomeRecentsEnterExitAnimationHolder = new HomeRecentsEnterExitAnimationHolder(getApplicationContext(), this.mTaskStackHorizontalGridView);
        } else {
            this.mTaskStackViewAdapter.setNewStackTasks(stackTasks);
        }
        this.mRecentsView.init(taskStack);
        if (launchState.launchedToTaskId != -1) {
            ArrayList<Task> stackTasks2 = taskStack.getStackTasks();
            int size = stackTasks2.size();
            for (int i = 0; i < size; i++) {
                Task task = stackTasks2.get(i);
                if (task.key.id == launchState.launchedToTaskId) {
                    task.isLaunchTarget = true;
                    return;
                }
            }
        }
    }

    boolean dismissRecentsToFocusedTaskOrHome() {
        if (Recents.getSystemServices().isRecentsActivityVisible()) {
            if (this.mRecentsView.launchFocusedTask()) {
                return true;
            }
            dismissRecentsToHome(true);
            return true;
        }
        return false;
    }

    void dismissRecentsToHome(boolean z) {
        Runnable runnable = new Runnable(this) { // from class: com.android.systemui.recents.tv.RecentsTvActivity.4
            final RecentsTvActivity this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                Recents.getSystemServices().sendCloseSystemWindows("homekey");
            }
        };
        DismissRecentsToHomeAnimationStarted dismissRecentsToHomeAnimationStarted = new DismissRecentsToHomeAnimationStarted(z);
        dismissRecentsToHomeAnimationStarted.addPostAnimationCallback(this.mFinishLaunchHomeRunnable);
        dismissRecentsToHomeAnimationStarted.addPostAnimationCallback(runnable);
        if (this.mTaskStackHorizontalGridView.getChildCount() > 0 && z) {
            this.mHomeRecentsEnterExitAnimationHolder.startExitAnimation(dismissRecentsToHomeAnimationStarted);
            return;
        }
        runnable.run();
        this.mFinishLaunchHomeRunnable.run();
    }

    boolean dismissRecentsToLaunchTargetTaskOrHome(boolean z) {
        if (Recents.getSystemServices().isRecentsActivityVisible()) {
            if (this.mRecentsView.launchPreviousTask(z)) {
                return true;
            }
            dismissRecentsToHome(z);
            return false;
        }
        return false;
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

    public final void onBusEvent(HideRecentsEvent hideRecentsEvent) {
        if (hideRecentsEvent.triggeredFromAltTab) {
            if (this.mIgnoreAltTabRelease) {
                return;
            }
            dismissRecentsToFocusedTaskOrHome();
        } else if (hideRecentsEvent.triggeredFromHomeKey) {
            dismissRecentsToHome(true);
        }
    }

    public final void onBusEvent(LaunchTaskFailedEvent launchTaskFailedEvent) {
        dismissRecentsToHome(true);
    }

    public final void onBusEvent(ToggleRecentsEvent toggleRecentsEvent) {
        if (Recents.getConfiguration().getLaunchState().launchedFromHome) {
            dismissRecentsToHome(true);
        } else {
            dismissRecentsToLaunchTargetTaskOrHome(true);
        }
    }

    public final void onBusEvent(AllTaskViewsDismissedEvent allTaskViewsDismissedEvent) {
        if (!this.mPipManager.isPipShown()) {
            dismissRecentsToHome(false);
            return;
        }
        this.mRecentsView.showEmptyView();
        this.mPipRecentsOverlayManager.requestFocus(false);
    }

    public final void onBusEvent(DeleteTaskDataEvent deleteTaskDataEvent) {
        Recents.getTaskLoader().deleteTaskData(deleteTaskDataEvent.task, false);
        Recents.getSystemServices().removeTask(deleteTaskDataEvent.task.key.id);
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
        this.mPipRecentsOverlayManager = PipManager.getInstance().getPipRecentsOverlayManager();
        EventBus.getDefault().register(this, 2);
        this.mPackageMonitor = new RecentsPackageMonitor();
        this.mPackageMonitor.register(this);
        setContentView(2130968779);
        this.mRecentsView = (RecentsTvView) findViewById(2131886606);
        this.mRecentsView.setSystemUiVisibility(1792);
        this.mPipView = findViewById(2131886611);
        this.mPipView.setOnFocusChangeListener(this.mPipViewFocusChangeListener);
        Rect recentsFocusedPipBounds = this.mPipManager.getRecentsFocusedPipBounds();
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.mPipView.getLayoutParams();
        layoutParams.width = recentsFocusedPipBounds.width();
        layoutParams.height = recentsFocusedPipBounds.height();
        layoutParams.leftMargin = recentsFocusedPipBounds.left;
        layoutParams.topMargin = recentsFocusedPipBounds.top;
        this.mPipView.setLayoutParams(layoutParams);
        this.mPipRecentsOverlayManager.setCallback(this.mPipRecentsOverlayManagerCallback);
        getWindow().getAttributes().privateFlags |= 16384;
        Intent intent = new Intent("android.intent.action.MAIN", (Uri) null);
        intent.addCategory("android.intent.category.HOME");
        intent.addFlags(270532608);
        intent.putExtra("com.android.systemui.recents.tv.RecentsTvActivity.RECENTS_HOME_INTENT_EXTRA", true);
        this.mFinishLaunchHomeRunnable = new FinishRecentsRunnable(this, intent);
        this.mPipManager.addListener(this.mPipListener);
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
        this.mPipManager.removeListener(this.mPipListener);
        if (this.mFinishedOnStartup) {
            return;
        }
        this.mPackageMonitor.unregister();
        EventBus.getDefault().unregister(this);
    }

    @Override // android.app.Activity
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();
        if (this.mLaunchedFromHome) {
            this.mHomeRecentsEnterExitAnimationHolder.startEnterAnimation(this.mPipManager.isPipShown());
        }
        EventBus.getDefault().send(new EnterRecentsWindowAnimationCompletedEvent());
    }

    @Override // android.app.Activity, android.view.KeyEvent.Callback
    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        switch (i) {
            case 67:
            case 112:
                EventBus.getDefault().send(new DismissFocusedTaskViewEvent());
                return true;
            default:
                return super.onKeyDown(i, keyEvent);
        }
    }

    @Override // android.app.Activity
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override // android.app.Activity
    public void onPause() {
        super.onPause();
        this.mPipRecentsOverlayManager.onRecentsPaused();
    }

    @Override // android.view.ViewTreeObserver.OnPreDrawListener
    public boolean onPreDraw() {
        this.mRecentsView.getViewTreeObserver().removeOnPreDrawListener(this);
        if (this.mLaunchedFromHome) {
            this.mHomeRecentsEnterExitAnimationHolder.setEnterFromHomeStartingAnimationValues(this.mPipManager.isPipShown());
        } else {
            this.mHomeRecentsEnterExitAnimationHolder.setEnterFromAppStartingAnimationValues(this.mPipManager.isPipShown());
        }
        this.mRecentsView.post(new Runnable(this) { // from class: com.android.systemui.recents.tv.RecentsTvActivity.6
            final RecentsTvActivity this$0;

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
    public void onResume() {
        boolean z = true;
        super.onResume();
        this.mPipRecentsOverlayManager.onRecentsResumed();
        updateRecentsTasks();
        RecentsActivityLaunchState launchState = Recents.getConfiguration().getLaunchState();
        if (!launchState.launchedFromHome ? !launchState.launchedFromApp : false) {
            EventBus.getDefault().send(new EnterRecentsWindowAnimationCompletedEvent());
        }
        SystemServicesProxy systemServices = Recents.getSystemServices();
        EventBus.getDefault().send(new RecentsVisibilityChangedEvent(this, true));
        if (this.mTaskStackHorizontalGridView.getStack().getTaskCount() <= 1 || this.mLaunchedFromHome) {
            this.mTaskStackHorizontalGridView.setSelectedPosition(0);
        } else {
            this.mTaskStackHorizontalGridView.setSelectedPosition(1);
        }
        this.mRecentsView.getViewTreeObserver().addOnPreDrawListener(this);
        View findViewById = findViewById(2131886612);
        this.mTalkBackEnabled = systemServices.isTouchExplorationEnabled();
        if (this.mTalkBackEnabled) {
            findViewById.setAccessibilityTraversalBefore(2131886610);
            findViewById.setAccessibilityTraversalAfter(2131886612);
            this.mTaskStackHorizontalGridView.setAccessibilityTraversalAfter(2131886612);
            this.mTaskStackHorizontalGridView.setAccessibilityTraversalBefore(2131886611);
            findViewById.setOnClickListener(new View.OnClickListener(this) { // from class: com.android.systemui.recents.tv.RecentsTvActivity.5
                final RecentsTvActivity this$0;

                {
                    this.this$0 = this;
                }

                @Override // android.view.View.OnClickListener
                public void onClick(View view) {
                    this.this$0.mTaskStackHorizontalGridView.requestFocus();
                    this.this$0.mTaskStackHorizontalGridView.sendAccessibilityEvent(8);
                    Task focusedTask = this.this$0.mTaskStackHorizontalGridView.getFocusedTask();
                    if (focusedTask != null) {
                        this.this$0.mTaskStackViewAdapter.removeTask(focusedTask);
                        EventBus.getDefault().send(new DeleteTaskDataEvent(focusedTask));
                    }
                }
            });
        }
        if (!this.mPipManager.isPipShown()) {
            this.mPipView.setVisibility(8);
            this.mPipRecentsOverlayManager.removePipRecentsOverlayView();
            return;
        }
        if (this.mTalkBackEnabled) {
            this.mPipView.setVisibility(0);
        } else {
            this.mPipView.setVisibility(8);
        }
        PipRecentsOverlayManager pipRecentsOverlayManager = this.mPipRecentsOverlayManager;
        if (this.mTaskStackViewAdapter.getItemCount() <= 0) {
            z = false;
        }
        pipRecentsOverlayManager.requestFocus(z);
    }

    @Override // android.app.Activity
    protected void onStop() {
        super.onStop();
        this.mIgnoreAltTabRelease = false;
        EventBus.getDefault().send(new RecentsVisibilityChangedEvent(this, false));
        Recents.getConfiguration().getLaunchState().reset();
        finish();
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
        EventBus.getDefault().send(new UserInteractionEvent());
    }

    public void requestPipControlsFocus() {
        boolean z = false;
        if (this.mPipManager.isPipShown()) {
            this.mTaskStackHorizontalGridView.startFocusLossAnimation();
            PipRecentsOverlayManager pipRecentsOverlayManager = this.mPipRecentsOverlayManager;
            if (this.mTaskStackViewAdapter.getItemCount() > 0) {
                z = true;
            }
            pipRecentsOverlayManager.requestFocus(z);
        }
    }
}
