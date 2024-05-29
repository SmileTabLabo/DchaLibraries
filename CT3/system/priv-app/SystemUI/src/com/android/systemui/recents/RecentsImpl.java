package com.android.systemui.recents;

import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.Log;
import android.util.MutableBoolean;
import android.view.AppTransitionAnimationSpec;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.policy.DockedDividerUtils;
import com.android.systemui.SystemUIApplication;
import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.events.activity.DockedTopTaskEvent;
import com.android.systemui.recents.events.activity.EnterRecentsWindowLastAnimationFrameEvent;
import com.android.systemui.recents.events.activity.HideRecentsEvent;
import com.android.systemui.recents.events.activity.IterateRecentsEvent;
import com.android.systemui.recents.events.activity.LaunchNextTaskRequestEvent;
import com.android.systemui.recents.events.activity.RecentsActivityStartingEvent;
import com.android.systemui.recents.events.activity.ToggleRecentsEvent;
import com.android.systemui.recents.events.ui.DraggingInRecentsEndedEvent;
import com.android.systemui.recents.events.ui.DraggingInRecentsEvent;
import com.android.systemui.recents.misc.DozeTrigger;
import com.android.systemui.recents.misc.ForegroundThread;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.model.RecentsTaskLoadPlan;
import com.android.systemui.recents.model.RecentsTaskLoader;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.model.TaskGrouping;
import com.android.systemui.recents.model.TaskStack;
import com.android.systemui.recents.views.TaskStackLayoutAlgorithm;
import com.android.systemui.recents.views.TaskStackView;
import com.android.systemui.recents.views.TaskStackViewScroller;
import com.android.systemui.recents.views.TaskViewHeader;
import com.android.systemui.recents.views.TaskViewTransform;
import com.android.systemui.statusbar.phone.PhoneStatusBar;
import java.util.ArrayList;
/* loaded from: a.zip:com/android/systemui/recents/RecentsImpl.class */
public class RecentsImpl implements ActivityOptions.OnAnimationFinishedListener {
    protected static RecentsTaskLoadPlan sInstanceLoadPlan;
    protected Context mContext;
    boolean mDraggingInRecents;
    protected TaskStackView mDummyStackView;
    TaskViewHeader mHeaderBar;
    protected long mLastToggleTime;
    boolean mLaunchedWhileDocking;
    int mNavBarHeight;
    int mNavBarWidth;
    int mStatusBarHeight;
    int mTaskBarHeight;
    TaskStackListenerImpl mTaskStackListener;
    protected Bitmap mThumbTransitionBitmapCache;
    protected boolean mTriggeredFromAltTab;
    Rect mTaskStackBounds = new Rect();
    TaskViewTransform mTmpTransform = new TaskViewTransform();
    final Object mHeaderBarLock = new Object();
    DozeTrigger mFastAltTabTrigger = new DozeTrigger(225, new Runnable(this) { // from class: com.android.systemui.recents.RecentsImpl.1
        final RecentsImpl this$0;

        {
            this.this$0 = this;
        }

        @Override // java.lang.Runnable
        public void run() {
            this.this$0.showRecents(this.this$0.mTriggeredFromAltTab, false, true, false, false, -1);
        }
    });
    protected Handler mHandler = new Handler();

    /* loaded from: a.zip:com/android/systemui/recents/RecentsImpl$TaskStackListenerImpl.class */
    class TaskStackListenerImpl extends SystemServicesProxy.TaskStackListener {
        final RecentsImpl this$0;

        TaskStackListenerImpl(RecentsImpl recentsImpl) {
            this.this$0 = recentsImpl;
        }

        @Override // com.android.systemui.recents.misc.SystemServicesProxy.TaskStackListener
        public void onTaskStackChanged() {
            if (Recents.getConfiguration().svelteLevel == 0) {
                RecentsTaskLoader taskLoader = Recents.getTaskLoader();
                ActivityManager.RunningTaskInfo runningTask = Recents.getSystemServices().getRunningTask();
                RecentsTaskLoadPlan createLoadPlan = taskLoader.createLoadPlan(this.this$0.mContext);
                taskLoader.preloadTasks(createLoadPlan, -1, false);
                RecentsTaskLoadPlan.Options options = new RecentsTaskLoadPlan.Options();
                if (runningTask != null) {
                    options.runningTaskId = runningTask.id;
                }
                options.numVisibleTasks = 2;
                options.numVisibleTaskThumbnails = 2;
                options.onlyLoadForCache = true;
                options.onlyLoadPausedActivities = true;
                taskLoader.loadTasks(this.this$0.mContext, createLoadPlan, options);
            }
        }
    }

    public RecentsImpl(Context context) {
        this.mContext = context;
        ForegroundThread.get();
        this.mTaskStackListener = new TaskStackListenerImpl(this);
        Recents.getSystemServices().registerTaskStackListener(this.mTaskStackListener);
        LayoutInflater from = LayoutInflater.from(this.mContext);
        this.mDummyStackView = new TaskStackView(this.mContext);
        this.mHeaderBar = (TaskViewHeader) from.inflate(2130968783, (ViewGroup) null, false);
        reloadResources();
    }

    private void calculateWindowStableInsets(Rect rect, Rect rect2) {
        Rect rect3 = new Rect(Recents.getSystemServices().getDisplayRect());
        rect3.inset(rect);
        Rect rect4 = new Rect(rect2);
        rect4.intersect(rect3);
        rect.left = rect4.left - rect2.left;
        rect.top = rect4.top - rect2.top;
        rect.right = rect2.right - rect4.right;
        rect.bottom = rect2.bottom - rect4.bottom;
    }

    public static RecentsTaskLoadPlan consumeInstanceLoadPlan() {
        RecentsTaskLoadPlan recentsTaskLoadPlan = sInstanceLoadPlan;
        sInstanceLoadPlan = null;
        return recentsTaskLoadPlan;
    }

    private Bitmap drawThumbnailTransitionBitmap(Task task, TaskViewTransform taskViewTransform, Bitmap bitmap) {
        SystemServicesProxy systemServices = Recents.getSystemServices();
        if (taskViewTransform == null || task.key == null || bitmap == null) {
            return null;
        }
        synchronized (this.mHeaderBarLock) {
            boolean isInSafeMode = !task.isSystemApp ? systemServices.isInSafeMode() : false;
            this.mHeaderBar.onTaskViewSizeChanged((int) taskViewTransform.rect.width(), (int) taskViewTransform.rect.height());
            bitmap.eraseColor(0);
            Canvas canvas = new Canvas(bitmap);
            Drawable drawable = this.mHeaderBar.getIconView().getDrawable();
            if (drawable != null) {
                drawable.setCallback(null);
            }
            this.mHeaderBar.bindToTask(task, false, isInSafeMode);
            this.mHeaderBar.onTaskDataLoaded();
            this.mHeaderBar.setDimAlpha(taskViewTransform.dimAlpha);
            this.mHeaderBar.draw(canvas);
            canvas.setBitmap(null);
        }
        return bitmap.createAshmemBitmap();
    }

    private ActivityOptions getThumbnailTransitionActivityOptions(ActivityManager.RunningTaskInfo runningTaskInfo, TaskStackView taskStackView, Rect rect) {
        if (runningTaskInfo == null || runningTaskInfo.stackId != 2) {
            Task task = new Task();
            TaskViewTransform thumbnailTransitionTransform = getThumbnailTransitionTransform(taskStackView, task, rect);
            Bitmap drawThumbnailTransitionBitmap = drawThumbnailTransitionBitmap(task, thumbnailTransitionTransform, this.mThumbTransitionBitmapCache);
            if (drawThumbnailTransitionBitmap != null) {
                RectF rectF = thumbnailTransitionTransform.rect;
                return ActivityOptions.makeThumbnailAspectScaleDownAnimation(this.mDummyStackView, drawThumbnailTransitionBitmap, (int) rectF.left, (int) rectF.top, (int) rectF.width(), (int) rectF.height(), this.mHandler, null);
            }
            return getUnknownTransitionActivityOptions();
        }
        ArrayList arrayList = new ArrayList();
        ArrayList<Task> stackTasks = taskStackView.getStack().getStackTasks();
        TaskStackLayoutAlgorithm stackAlgorithm = taskStackView.getStackAlgorithm();
        TaskStackViewScroller scroller = taskStackView.getScroller();
        taskStackView.updateLayoutAlgorithm(true);
        taskStackView.updateToInitialState();
        for (int size = stackTasks.size() - 1; size >= 0; size--) {
            Task task2 = stackTasks.get(size);
            if (task2.isFreeformTask()) {
                this.mTmpTransform = stackAlgorithm.getStackTransformScreenCoordinates(task2, scroller.getStackScroll(), this.mTmpTransform, null, rect);
                Bitmap drawThumbnailTransitionBitmap2 = drawThumbnailTransitionBitmap(task2, this.mTmpTransform, this.mThumbTransitionBitmapCache);
                Rect rect2 = new Rect();
                this.mTmpTransform.rect.round(rect2);
                arrayList.add(new AppTransitionAnimationSpec(task2.key.id, drawThumbnailTransitionBitmap2, rect2));
            }
        }
        AppTransitionAnimationSpec[] appTransitionAnimationSpecArr = new AppTransitionAnimationSpec[arrayList.size()];
        arrayList.toArray(appTransitionAnimationSpecArr);
        return ActivityOptions.makeThumbnailAspectScaleDownAnimation(this.mDummyStackView, appTransitionAnimationSpecArr, this.mHandler, null, this);
    }

    private TaskViewTransform getThumbnailTransitionTransform(TaskStackView taskStackView, Task task, Rect rect) {
        Task task2;
        TaskStack stack = taskStackView.getStack();
        Task launchTarget = stack.getLaunchTarget();
        if (launchTarget != null) {
            task.copyFrom(launchTarget);
            task2 = launchTarget;
        } else {
            Task stackFrontMostTask = stack.getStackFrontMostTask(true);
            task.copyFrom(stackFrontMostTask);
            task2 = stackFrontMostTask;
        }
        taskStackView.updateLayoutAlgorithm(true);
        taskStackView.updateToInitialState();
        taskStackView.getStackAlgorithm().getStackTransformScreenCoordinates(task2, taskStackView.getScroller().getStackScroll(), this.mTmpTransform, null, rect);
        return this.mTmpTransform;
    }

    private Rect getWindowRectOverride(int i) {
        if (i == -1) {
            return null;
        }
        Rect rect = new Rect();
        Rect displayRect = Recents.getSystemServices().getDisplayRect();
        DockedDividerUtils.calculateBoundsForPosition(i, 4, rect, displayRect.width(), displayRect.height(), Recents.getSystemServices().getDockedDividerSize(this.mContext));
        return rect;
    }

    private void preloadIcon(int i) {
        RecentsTaskLoadPlan.Options options = new RecentsTaskLoadPlan.Options();
        options.runningTaskId = i;
        options.loadThumbnails = false;
        options.onlyLoadForCache = true;
        Recents.getTaskLoader().loadTasks(this.mContext, sInstanceLoadPlan, options);
    }

    private void reloadResources() {
        Resources resources = this.mContext.getResources();
        this.mStatusBarHeight = resources.getDimensionPixelSize(17104919);
        this.mNavBarHeight = resources.getDimensionPixelSize(17104920);
        this.mNavBarWidth = resources.getDimensionPixelSize(17104922);
        this.mTaskBarHeight = TaskStackLayoutAlgorithm.getDimensionForDevice(this.mContext, 2131690012, 2131690012, 2131690012, 2131690013, 2131690012, 2131690013);
    }

    private void startRecentsActivity(ActivityOptions activityOptions) {
        Intent intent = new Intent();
        intent.setClassName("com.android.systemui", "com.android.systemui.recents.RecentsActivity");
        intent.setFlags(276840448);
        if (activityOptions != null) {
            this.mContext.startActivityAsUser(intent, activityOptions.toBundle(), UserHandle.CURRENT);
        } else {
            this.mContext.startActivityAsUser(intent, UserHandle.CURRENT);
        }
        EventBus.getDefault().send(new RecentsActivityStartingEvent());
    }

    private void updateHeaderBarLayout(TaskStack taskStack, Rect rect) {
        SystemServicesProxy systemServices = Recents.getSystemServices();
        Rect displayRect = systemServices.getDisplayRect();
        Rect rect2 = new Rect();
        systemServices.getStableInsets(rect2);
        Rect rect3 = rect != null ? new Rect(rect) : systemServices.getWindowRect();
        if (systemServices.hasDockedTask()) {
            rect3.bottom -= rect2.bottom;
            rect2.bottom = 0;
        }
        calculateWindowStableInsets(rect2, rect3);
        rect3.offsetTo(0, 0);
        TaskStackLayoutAlgorithm stackAlgorithm = this.mDummyStackView.getStackAlgorithm();
        stackAlgorithm.setSystemInsets(rect2);
        if (taskStack != null) {
            stackAlgorithm.getTaskStackBounds(displayRect, rect3, rect2.top, rect2.right, this.mTaskStackBounds);
            stackAlgorithm.reset();
            stackAlgorithm.initialize(displayRect, rect3, this.mTaskStackBounds, TaskStackLayoutAlgorithm.StackState.getStackStateForStack(taskStack));
            this.mDummyStackView.setTasks(taskStack, false);
            Rect untransformedTaskViewBounds = stackAlgorithm.getUntransformedTaskViewBounds();
            if (untransformedTaskViewBounds.isEmpty()) {
                return;
            }
            int width = untransformedTaskViewBounds.width();
            synchronized (this.mHeaderBarLock) {
                if (this.mHeaderBar.getMeasuredWidth() != width || this.mHeaderBar.getMeasuredHeight() != this.mTaskBarHeight) {
                    this.mHeaderBar.measure(View.MeasureSpec.makeMeasureSpec(width, 1073741824), View.MeasureSpec.makeMeasureSpec(this.mTaskBarHeight, 1073741824));
                }
                this.mHeaderBar.layout(0, 0, width, this.mTaskBarHeight);
            }
            if (this.mThumbTransitionBitmapCache != null && this.mThumbTransitionBitmapCache.getWidth() == width && this.mThumbTransitionBitmapCache.getHeight() == this.mTaskBarHeight) {
                return;
            }
            this.mThumbTransitionBitmapCache = Bitmap.createBitmap(width, this.mTaskBarHeight, Bitmap.Config.ARGB_8888);
        }
    }

    public void cancelPreloadingRecents() {
    }

    public void dockTopTask(int i, int i2, int i3, Rect rect) {
        if (Recents.getSystemServices().moveTaskToDockedStack(i, i3, rect)) {
            EventBus.getDefault().send(new DockedTopTaskEvent(i2, rect));
            showRecents(false, i2 == 0, false, true, false, -1);
        }
    }

    protected ActivityOptions getHomeTransitionActivityOptions() {
        return ActivityOptions.makeCustomAnimation(this.mContext, 2131034288, 2131034289, this.mHandler, null);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public ActivityOptions getUnknownTransitionActivityOptions() {
        return ActivityOptions.makeCustomAnimation(this.mContext, 2131034290, 2131034291, this.mHandler, null);
    }

    public void hideRecents(boolean z, boolean z2) {
        if (!z || !this.mFastAltTabTrigger.isDozing()) {
            EventBus.getDefault().post(new HideRecentsEvent(z, z2));
            return;
        }
        showNextTask();
        this.mFastAltTabTrigger.stopDozing();
    }

    public void onAnimationFinished() {
        EventBus.getDefault().post(new EnterRecentsWindowLastAnimationFrameEvent());
    }

    public void onBootCompleted() {
        RecentsTaskLoader taskLoader = Recents.getTaskLoader();
        RecentsTaskLoadPlan createLoadPlan = taskLoader.createLoadPlan(this.mContext);
        taskLoader.preloadTasks(createLoadPlan, -1, false);
        RecentsTaskLoadPlan.Options options = new RecentsTaskLoadPlan.Options();
        options.numVisibleTasks = taskLoader.getIconCacheSize();
        options.numVisibleTaskThumbnails = taskLoader.getThumbnailCacheSize();
        options.onlyLoadForCache = true;
        taskLoader.loadTasks(this.mContext, createLoadPlan, options);
    }

    public void onConfigurationChanged() {
        reloadResources();
        this.mDummyStackView.reloadOnConfigurationChange();
        this.mHeaderBar.onConfigurationChanged();
    }

    public void onDraggingInRecents(float f) {
        EventBus.getDefault().sendOntoMainThread(new DraggingInRecentsEvent(f));
    }

    public void onDraggingInRecentsEnded(float f) {
        EventBus.getDefault().sendOntoMainThread(new DraggingInRecentsEndedEvent(f));
    }

    public void onStartScreenPinning(Context context, int i) {
        PhoneStatusBar phoneStatusBar = (PhoneStatusBar) ((SystemUIApplication) context).getComponent(PhoneStatusBar.class);
        if (phoneStatusBar != null) {
            phoneStatusBar.showScreenPinningRequest(i, false);
        }
    }

    public void onVisibilityChanged(Context context, boolean z) {
        PhoneStatusBar phoneStatusBar = (PhoneStatusBar) ((SystemUIApplication) context).getComponent(PhoneStatusBar.class);
        if (phoneStatusBar != null) {
            phoneStatusBar.updateRecentsVisibility(z);
        }
    }

    public void preloadRecents() {
        SystemServicesProxy systemServices = Recents.getSystemServices();
        MutableBoolean mutableBoolean = new MutableBoolean(true);
        if (systemServices.isRecentsActivityVisible(mutableBoolean)) {
            return;
        }
        ActivityManager.RunningTaskInfo runningTask = systemServices.getRunningTask();
        if (runningTask == null) {
            Log.e("RecentsImpl", "preloadRecents runningTask is null");
            return;
        }
        RecentsTaskLoader taskLoader = Recents.getTaskLoader();
        sInstanceLoadPlan = taskLoader.createLoadPlan(this.mContext);
        sInstanceLoadPlan.preloadRawTasks(!mutableBoolean.value);
        taskLoader.preloadTasks(sInstanceLoadPlan, runningTask.id, !mutableBoolean.value);
        TaskStack taskStack = sInstanceLoadPlan.getTaskStack();
        if (taskStack.getTaskCount() > 0) {
            preloadIcon(runningTask.id);
            updateHeaderBarLayout(taskStack, null);
        }
    }

    public void showNextAffiliatedTask() {
        MetricsLogger.count(this.mContext, "overview_affiliated_task_next", 1);
        showRelativeAffiliatedTask(true);
    }

    public void showNextTask() {
        ActivityManager.RunningTaskInfo runningTask;
        ActivityOptions activityOptions;
        Task task;
        SystemServicesProxy systemServices = Recents.getSystemServices();
        RecentsTaskLoader taskLoader = Recents.getTaskLoader();
        RecentsTaskLoadPlan createLoadPlan = taskLoader.createLoadPlan(this.mContext);
        taskLoader.preloadTasks(createLoadPlan, -1, false);
        TaskStack taskStack = createLoadPlan.getTaskStack();
        if (taskStack == null || taskStack.getTaskCount() == 0 || (runningTask = systemServices.getRunningTask()) == null) {
            return;
        }
        boolean isHomeStack = SystemServicesProxy.isHomeStack(runningTask.stackId);
        ArrayList<Task> stackTasks = taskStack.getStackTasks();
        int size = stackTasks.size() - 1;
        while (true) {
            activityOptions = null;
            task = null;
            if (size < 1) {
                break;
            }
            Task task2 = stackTasks.get(size);
            if (isHomeStack) {
                task = stackTasks.get(size - 1);
                activityOptions = ActivityOptions.makeCustomAnimation(this.mContext, 2131034294, 2131034287);
                break;
            } else if (task2.key.id == runningTask.id) {
                task = stackTasks.get(size - 1);
                activityOptions = ActivityOptions.makeCustomAnimation(this.mContext, 2131034297, 2131034296);
                break;
            } else {
                size--;
            }
        }
        if (task == null) {
            systemServices.startInPlaceAnimationOnFrontMostApplication(ActivityOptions.makeCustomInPlaceAnimation(this.mContext, 2131034295));
        } else {
            systemServices.startActivityFromRecents(this.mContext, task.key, task.title, activityOptions);
        }
    }

    public void showPrevAffiliatedTask() {
        MetricsLogger.count(this.mContext, "overview_affiliated_task_prev", 1);
        showRelativeAffiliatedTask(false);
    }

    public void showRecents(boolean z, boolean z2, boolean z3, boolean z4, boolean z5, int i) {
        this.mTriggeredFromAltTab = z;
        this.mDraggingInRecents = z2;
        this.mLaunchedWhileDocking = z4;
        if (this.mFastAltTabTrigger.isAsleep()) {
            this.mFastAltTabTrigger.stopDozing();
        } else if (this.mFastAltTabTrigger.isDozing()) {
            if (!z) {
                return;
            }
            this.mFastAltTabTrigger.stopDozing();
        } else if (z) {
            this.mFastAltTabTrigger.startDozing();
            return;
        }
        try {
            SystemServicesProxy systemServices = Recents.getSystemServices();
            if (z4) {
                z2 = true;
            }
            MutableBoolean mutableBoolean = new MutableBoolean(z2);
            if (z2 || !systemServices.isRecentsActivityVisible(mutableBoolean)) {
                ActivityManager.RunningTaskInfo runningTask = systemServices.getRunningTask();
                if (mutableBoolean.value) {
                    z5 = true;
                }
                startRecentsActivity(runningTask, z5, z3, i);
            }
        } catch (ActivityNotFoundException e) {
            Log.e("RecentsImpl", "Failed to launch RecentsActivity", e);
        }
    }

    public void showRelativeAffiliatedTask(boolean z) {
        ActivityManager.RunningTaskInfo runningTask;
        ActivityOptions activityOptions;
        int i;
        Task task;
        Task.TaskKey prevTaskInGroup;
        SystemServicesProxy systemServices = Recents.getSystemServices();
        RecentsTaskLoader taskLoader = Recents.getTaskLoader();
        RecentsTaskLoadPlan createLoadPlan = taskLoader.createLoadPlan(this.mContext);
        taskLoader.preloadTasks(createLoadPlan, -1, false);
        TaskStack taskStack = createLoadPlan.getTaskStack();
        if (taskStack == null || taskStack.getTaskCount() == 0 || (runningTask = systemServices.getRunningTask()) == null || SystemServicesProxy.isHomeStack(runningTask.stackId)) {
            return;
        }
        ArrayList<Task> stackTasks = taskStack.getStackTasks();
        int size = stackTasks.size();
        int i2 = 0;
        while (true) {
            activityOptions = null;
            i = 0;
            task = null;
            if (i2 >= size) {
                break;
            }
            Task task2 = stackTasks.get(i2);
            if (task2.key.id == runningTask.id) {
                TaskGrouping taskGrouping = task2.group;
                if (z) {
                    prevTaskInGroup = taskGrouping.getNextTaskInGroup(task2);
                    activityOptions = ActivityOptions.makeCustomAnimation(this.mContext, 2131034294, 2131034293);
                } else {
                    prevTaskInGroup = taskGrouping.getPrevTaskInGroup(task2);
                    activityOptions = ActivityOptions.makeCustomAnimation(this.mContext, 2131034297, 2131034296);
                }
                task = null;
                if (prevTaskInGroup != null) {
                    task = taskStack.findTaskWithId(prevTaskInGroup.id);
                }
                i = taskGrouping.getTaskCount();
            } else {
                i2++;
            }
        }
        if (task != null) {
            MetricsLogger.count(this.mContext, "overview_affiliated_task_launch", 1);
            systemServices.startActivityFromRecents(this.mContext, task.key, task.title, activityOptions);
        } else if (i > 1) {
            if (z) {
                systemServices.startInPlaceAnimationOnFrontMostApplication(ActivityOptions.makeCustomInPlaceAnimation(this.mContext, 2131034292));
            } else {
                systemServices.startInPlaceAnimationOnFrontMostApplication(ActivityOptions.makeCustomInPlaceAnimation(this.mContext, 2131034295));
            }
        }
    }

    protected void startRecentsActivity(ActivityManager.RunningTaskInfo runningTaskInfo, boolean z, boolean z2, int i) {
        RecentsTaskLoader taskLoader = Recents.getTaskLoader();
        RecentsActivityLaunchState launchState = Recents.getConfiguration().getLaunchState();
        int i2 = (this.mLaunchedWhileDocking || runningTaskInfo == null) ? -1 : runningTaskInfo.id;
        if (this.mLaunchedWhileDocking || this.mTriggeredFromAltTab || sInstanceLoadPlan == null) {
            sInstanceLoadPlan = taskLoader.createLoadPlan(this.mContext);
        }
        if (this.mLaunchedWhileDocking || this.mTriggeredFromAltTab || !sInstanceLoadPlan.hasTasks()) {
            taskLoader.preloadTasks(sInstanceLoadPlan, i2, !z);
        }
        TaskStack taskStack = sInstanceLoadPlan.getTaskStack();
        boolean z3 = taskStack.getTaskCount() > 0;
        boolean z4 = (runningTaskInfo == null || z) ? false : z3;
        launchState.launchedFromHome = (z4 || this.mLaunchedWhileDocking) ? false : true;
        launchState.launchedFromApp = !z4 ? this.mLaunchedWhileDocking : true;
        launchState.launchedViaDockGesture = this.mLaunchedWhileDocking;
        launchState.launchedViaDragGesture = this.mDraggingInRecents;
        launchState.launchedToTaskId = i2;
        launchState.launchedWithAltTab = this.mTriggeredFromAltTab;
        preloadIcon(i2);
        Rect windowRectOverride = getWindowRectOverride(i);
        updateHeaderBarLayout(taskStack, windowRectOverride);
        TaskStackLayoutAlgorithm.VisibilityReport computeStackVisibilityReport = this.mDummyStackView.computeStackVisibilityReport();
        launchState.launchedNumVisibleTasks = computeStackVisibilityReport.numVisibleTasks;
        launchState.launchedNumVisibleThumbnails = computeStackVisibilityReport.numVisibleThumbnails;
        if (!z2) {
            startRecentsActivity(ActivityOptions.makeCustomAnimation(this.mContext, -1, -1));
            return;
        }
        startRecentsActivity(z4 ? getThumbnailTransitionActivityOptions(runningTaskInfo, this.mDummyStackView, windowRectOverride) : z3 ? getHomeTransitionActivityOptions() : getUnknownTransitionActivityOptions());
        this.mLastToggleTime = SystemClock.elapsedRealtime();
    }

    public void toggleRecents(int i) {
        if (this.mFastAltTabTrigger.isDozing()) {
            return;
        }
        this.mDraggingInRecents = false;
        this.mLaunchedWhileDocking = false;
        this.mTriggeredFromAltTab = false;
        try {
            SystemServicesProxy systemServices = Recents.getSystemServices();
            MutableBoolean mutableBoolean = new MutableBoolean(true);
            long elapsedRealtime = SystemClock.elapsedRealtime() - this.mLastToggleTime;
            if (!systemServices.isRecentsActivityVisible(mutableBoolean)) {
                if (elapsedRealtime < 350) {
                    return;
                }
                startRecentsActivity(systemServices.getRunningTask(), mutableBoolean.value, true, i);
                systemServices.sendCloseSystemWindows("recentapps");
                this.mLastToggleTime = SystemClock.elapsedRealtime();
                return;
            }
            RecentsDebugFlags debugFlags = Recents.getDebugFlags();
            if (Recents.getConfiguration().getLaunchState().launchedWithAltTab) {
                if (elapsedRealtime < 350) {
                    return;
                }
                EventBus.getDefault().post(new ToggleRecentsEvent());
                this.mLastToggleTime = SystemClock.elapsedRealtime();
            } else if (debugFlags.isPagingEnabled() && (ViewConfiguration.getDoubleTapMinTime() >= elapsedRealtime || elapsedRealtime >= ViewConfiguration.getDoubleTapTimeout())) {
                EventBus.getDefault().post(new IterateRecentsEvent());
            } else {
                EventBus.getDefault().post(new LaunchNextTaskRequestEvent());
            }
        } catch (ActivityNotFoundException e) {
            Log.e("RecentsImpl", "Failed to launch RecentsActivity", e);
        }
    }
}
