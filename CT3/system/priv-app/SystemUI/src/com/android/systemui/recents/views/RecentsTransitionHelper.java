package com.android.systemui.recents.views;

import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.IRemoteCallback;
import android.os.RemoteException;
import android.util.Log;
import android.view.AppTransitionAnimationSpec;
import android.view.IAppTransitionAnimationSpecsFuture;
import com.android.internal.annotations.GuardedBy;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.events.activity.CancelEnterRecentsWindowAnimationEvent;
import com.android.systemui.recents.events.activity.ExitRecentsWindowFirstAnimationFrameEvent;
import com.android.systemui.recents.events.activity.LaunchTaskFailedEvent;
import com.android.systemui.recents.events.activity.LaunchTaskStartedEvent;
import com.android.systemui.recents.events.activity.LaunchTaskSucceededEvent;
import com.android.systemui.recents.events.component.ScreenPinningRequestEvent;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.model.TaskStack;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/* loaded from: a.zip:com/android/systemui/recents/views/RecentsTransitionHelper.class */
public class RecentsTransitionHelper {
    private static final List<AppTransitionAnimationSpec> SPECS_WAITING = new ArrayList();
    private Context mContext;
    @GuardedBy("this")
    private List<AppTransitionAnimationSpec> mAppTransitionAnimationSpecs = SPECS_WAITING;
    private TaskViewTransform mTmpTransform = new TaskViewTransform();
    private StartScreenPinningRunnableRunnable mStartScreenPinningRunnable = new StartScreenPinningRunnableRunnable(this, null);
    private Handler mHandler = new Handler();

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.android.systemui.recents.views.RecentsTransitionHelper$6  reason: invalid class name */
    /* loaded from: a.zip:com/android/systemui/recents/views/RecentsTransitionHelper$6.class */
    public class AnonymousClass6 extends IAppTransitionAnimationSpecsFuture.Stub {
        final RecentsTransitionHelper this$0;
        final AnimationSpecComposer val$composer;

        AnonymousClass6(RecentsTransitionHelper recentsTransitionHelper, AnimationSpecComposer animationSpecComposer) {
            this.this$0 = recentsTransitionHelper;
            this.val$composer = animationSpecComposer;
        }

        public AppTransitionAnimationSpec[] get() throws RemoteException {
            this.this$0.mHandler.post(new Runnable(this, this.val$composer) { // from class: com.android.systemui.recents.views.RecentsTransitionHelper.6.1
                final AnonymousClass6 this$1;
                final AnimationSpecComposer val$composer;

                {
                    this.this$1 = this;
                    this.val$composer = r5;
                }

                @Override // java.lang.Runnable
                public void run() {
                    synchronized (this.this$1.this$0) {
                        this.this$1.this$0.mAppTransitionAnimationSpecs = this.val$composer.composeSpecs();
                        this.this$1.this$0.notifyAll();
                    }
                }
            });
            synchronized (this.this$0) {
                while (this.this$0.mAppTransitionAnimationSpecs == RecentsTransitionHelper.SPECS_WAITING) {
                    try {
                        this.this$0.wait();
                    } catch (InterruptedException e) {
                    }
                }
                if (this.this$0.mAppTransitionAnimationSpecs == null) {
                    return null;
                }
                AppTransitionAnimationSpec[] appTransitionAnimationSpecArr = new AppTransitionAnimationSpec[this.this$0.mAppTransitionAnimationSpecs.size()];
                this.this$0.mAppTransitionAnimationSpecs.toArray(appTransitionAnimationSpecArr);
                this.this$0.mAppTransitionAnimationSpecs = RecentsTransitionHelper.SPECS_WAITING;
                return appTransitionAnimationSpecArr;
            }
        }
    }

    /* loaded from: a.zip:com/android/systemui/recents/views/RecentsTransitionHelper$AnimationSpecComposer.class */
    public interface AnimationSpecComposer {
        List<AppTransitionAnimationSpec> composeSpecs();
    }

    /* loaded from: a.zip:com/android/systemui/recents/views/RecentsTransitionHelper$StartScreenPinningRunnableRunnable.class */
    private class StartScreenPinningRunnableRunnable implements Runnable {
        private int taskId;
        final RecentsTransitionHelper this$0;

        private StartScreenPinningRunnableRunnable(RecentsTransitionHelper recentsTransitionHelper) {
            this.this$0 = recentsTransitionHelper;
            this.taskId = -1;
        }

        /* synthetic */ StartScreenPinningRunnableRunnable(RecentsTransitionHelper recentsTransitionHelper, StartScreenPinningRunnableRunnable startScreenPinningRunnableRunnable) {
            this(recentsTransitionHelper);
        }

        @Override // java.lang.Runnable
        public void run() {
            EventBus.getDefault().send(new ScreenPinningRequestEvent(this.this$0.mContext, this.taskId));
        }
    }

    public RecentsTransitionHelper(Context context) {
        this.mContext = context;
    }

    private static AppTransitionAnimationSpec composeAnimationSpec(TaskStackView taskStackView, TaskView taskView, TaskViewTransform taskViewTransform, boolean z) {
        Bitmap bitmap = null;
        if (z) {
            Bitmap composeHeaderBitmap = composeHeaderBitmap(taskView, taskViewTransform);
            bitmap = composeHeaderBitmap;
            if (composeHeaderBitmap == null) {
                return null;
            }
        }
        Rect rect = new Rect();
        taskViewTransform.rect.round(rect);
        if (taskStackView.getStack().getStackFrontMostTask(false) != taskView.getTask()) {
            rect.bottom = rect.top + taskStackView.getMeasuredHeight();
        }
        return new AppTransitionAnimationSpec(taskView.getTask().key.id, bitmap, rect);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public List<AppTransitionAnimationSpec> composeAnimationSpecs(Task task, TaskStackView taskStackView, int i) {
        if (i == -1) {
            i = task.key.stackId;
        }
        if (ActivityManager.StackId.useAnimationSpecForAppTransition(i)) {
            TaskView childViewForTask = taskStackView.getChildViewForTask(task);
            TaskStackLayoutAlgorithm stackAlgorithm = taskStackView.getStackAlgorithm();
            Rect rect = new Rect();
            stackAlgorithm.getFrontOfStackTransform().rect.round(rect);
            ArrayList arrayList = new ArrayList();
            if (i == 1 || i == 3 || i == -1) {
                if (childViewForTask == null) {
                    arrayList.add(composeOffscreenAnimationSpec(task, rect));
                } else {
                    this.mTmpTransform.fillIn(childViewForTask);
                    stackAlgorithm.transformToScreenCoordinates(this.mTmpTransform, null);
                    AppTransitionAnimationSpec composeAnimationSpec = composeAnimationSpec(taskStackView, childViewForTask, this.mTmpTransform, true);
                    if (composeAnimationSpec != null) {
                        arrayList.add(composeAnimationSpec);
                    }
                }
                return arrayList;
            }
            ArrayList<Task> stackTasks = taskStackView.getStack().getStackTasks();
            for (int size = stackTasks.size() - 1; size >= 0; size--) {
                Task task2 = stackTasks.get(size);
                if (task2.isFreeformTask() || i == 2) {
                    TaskView childViewForTask2 = taskStackView.getChildViewForTask(task2);
                    if (childViewForTask2 == null) {
                        arrayList.add(composeOffscreenAnimationSpec(task2, rect));
                    } else {
                        this.mTmpTransform.fillIn(childViewForTask);
                        stackAlgorithm.transformToScreenCoordinates(this.mTmpTransform, null);
                        AppTransitionAnimationSpec composeAnimationSpec2 = composeAnimationSpec(taskStackView, childViewForTask2, this.mTmpTransform, true);
                        if (composeAnimationSpec2 != null) {
                            arrayList.add(composeAnimationSpec2);
                        }
                    }
                }
            }
            return arrayList;
        }
        return null;
    }

    private static Bitmap composeHeaderBitmap(TaskView taskView, TaskViewTransform taskViewTransform) {
        float f = taskViewTransform.scale;
        int width = (int) taskViewTransform.rect.width();
        int measuredHeight = (int) (taskView.mHeaderView.getMeasuredHeight() * f);
        if (width == 0 || measuredHeight == 0) {
            return null;
        }
        Bitmap createBitmap = Bitmap.createBitmap(width, measuredHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(createBitmap);
        canvas.scale(f, f);
        taskView.mHeaderView.draw(canvas);
        canvas.setBitmap(null);
        return createBitmap.createAshmemBitmap();
    }

    private static AppTransitionAnimationSpec composeOffscreenAnimationSpec(Task task, Rect rect) {
        return new AppTransitionAnimationSpec(task.key.id, (Bitmap) null, rect);
    }

    public static Bitmap composeTaskBitmap(TaskView taskView, TaskViewTransform taskViewTransform) {
        float f = taskViewTransform.scale;
        int width = (int) (taskViewTransform.rect.width() * f);
        int height = (int) (taskViewTransform.rect.height() * f);
        if (width == 0 || height == 0) {
            Log.e("RecentsTransitionHelper", "Could not compose thumbnail for task: " + taskView.getTask() + " at transform: " + taskViewTransform);
            Bitmap createBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
            createBitmap.eraseColor(0);
            return createBitmap;
        }
        Bitmap createBitmap2 = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(createBitmap2);
        canvas.scale(f, f);
        taskView.draw(canvas);
        canvas.setBitmap(null);
        return createBitmap2.createAshmemBitmap();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startTaskActivity(TaskStack taskStack, Task task, TaskView taskView, ActivityOptions activityOptions, IAppTransitionAnimationSpecsFuture iAppTransitionAnimationSpecsFuture, ActivityOptions.OnAnimationStartedListener onAnimationStartedListener) {
        SystemServicesProxy systemServices = Recents.getSystemServices();
        if (systemServices.startActivityFromRecents(this.mContext, task.key, task.title, activityOptions)) {
            int i = 0;
            int indexOfStackTask = taskStack.indexOfStackTask(task);
            if (indexOfStackTask > -1) {
                i = (taskStack.getTaskCount() - indexOfStackTask) - 1;
            }
            EventBus.getDefault().send(new LaunchTaskSucceededEvent(i));
        } else {
            if (taskView != null) {
                taskView.dismissTask();
            }
            EventBus.getDefault().send(new LaunchTaskFailedEvent());
        }
        if (iAppTransitionAnimationSpecsFuture != null) {
            systemServices.overridePendingAppTransitionMultiThumbFuture(iAppTransitionAnimationSpecsFuture, wrapStartedListener(onAnimationStartedListener), true);
        }
    }

    public List<AppTransitionAnimationSpec> composeDockAnimationSpec(TaskView taskView, Rect rect) {
        this.mTmpTransform.fillIn(taskView);
        Task task = taskView.getTask();
        return Collections.singletonList(new AppTransitionAnimationSpec(task.key.id, composeTaskBitmap(taskView, this.mTmpTransform), rect));
    }

    public IAppTransitionAnimationSpecsFuture getAppTransitionFuture(AnimationSpecComposer animationSpecComposer) {
        synchronized (this) {
            this.mAppTransitionAnimationSpecs = SPECS_WAITING;
        }
        return new AnonymousClass6(this, animationSpecComposer);
    }

    public void launchTaskFromRecents(TaskStack taskStack, Task task, TaskStackView taskStackView, TaskView taskView, boolean z, Rect rect, int i) {
        IAppTransitionAnimationSpecsFuture iAppTransitionAnimationSpecsFuture;
        ActivityOptions.OnAnimationStartedListener onAnimationStartedListener;
        ActivityOptions makeBasic = ActivityOptions.makeBasic();
        if (rect != null) {
            Rect rect2 = rect;
            if (rect.isEmpty()) {
                rect2 = null;
            }
            makeBasic.setLaunchBounds(rect2);
        }
        if (taskView != null) {
            iAppTransitionAnimationSpecsFuture = getAppTransitionFuture(new AnimationSpecComposer(this, task, taskStackView, i) { // from class: com.android.systemui.recents.views.RecentsTransitionHelper.1
                final RecentsTransitionHelper this$0;
                final int val$destinationStack;
                final TaskStackView val$stackView;
                final Task val$task;

                {
                    this.this$0 = this;
                    this.val$task = task;
                    this.val$stackView = taskStackView;
                    this.val$destinationStack = i;
                }

                @Override // com.android.systemui.recents.views.RecentsTransitionHelper.AnimationSpecComposer
                public List<AppTransitionAnimationSpec> composeSpecs() {
                    return this.this$0.composeAnimationSpecs(this.val$task, this.val$stackView, this.val$destinationStack);
                }
            });
            onAnimationStartedListener = new ActivityOptions.OnAnimationStartedListener(this, task, taskStackView, z) { // from class: com.android.systemui.recents.views.RecentsTransitionHelper.2
                final RecentsTransitionHelper this$0;
                final boolean val$screenPinningRequested;
                final TaskStackView val$stackView;
                final Task val$task;

                {
                    this.this$0 = this;
                    this.val$task = task;
                    this.val$stackView = taskStackView;
                    this.val$screenPinningRequested = z;
                }

                public void onAnimationStarted() {
                    EventBus.getDefault().send(new CancelEnterRecentsWindowAnimationEvent(this.val$task));
                    EventBus.getDefault().send(new ExitRecentsWindowFirstAnimationFrameEvent());
                    this.val$stackView.cancelAllTaskViewAnimations();
                    if (this.val$screenPinningRequested) {
                        this.this$0.mStartScreenPinningRunnable.taskId = this.val$task.key.id;
                        this.this$0.mHandler.postDelayed(this.this$0.mStartScreenPinningRunnable, 350L);
                    }
                }
            };
        } else {
            iAppTransitionAnimationSpecsFuture = null;
            onAnimationStartedListener = new ActivityOptions.OnAnimationStartedListener(this, task, taskStackView) { // from class: com.android.systemui.recents.views.RecentsTransitionHelper.3
                final RecentsTransitionHelper this$0;
                final TaskStackView val$stackView;
                final Task val$task;

                {
                    this.this$0 = this;
                    this.val$task = task;
                    this.val$stackView = taskStackView;
                }

                public void onAnimationStarted() {
                    EventBus.getDefault().send(new CancelEnterRecentsWindowAnimationEvent(this.val$task));
                    EventBus.getDefault().send(new ExitRecentsWindowFirstAnimationFrameEvent());
                    this.val$stackView.cancelAllTaskViewAnimations();
                }
            };
        }
        if (taskView == null) {
            startTaskActivity(taskStack, task, taskView, makeBasic, iAppTransitionAnimationSpecsFuture, onAnimationStartedListener);
        } else {
            LaunchTaskStartedEvent launchTaskStartedEvent = new LaunchTaskStartedEvent(taskView, z);
            if (task.group == null || task.group.isFrontMostTask(task)) {
                EventBus.getDefault().send(launchTaskStartedEvent);
                startTaskActivity(taskStack, task, taskView, makeBasic, iAppTransitionAnimationSpecsFuture, onAnimationStartedListener);
            } else {
                launchTaskStartedEvent.addPostAnimationCallback(new Runnable(this, taskStack, task, taskView, makeBasic, iAppTransitionAnimationSpecsFuture, onAnimationStartedListener) { // from class: com.android.systemui.recents.views.RecentsTransitionHelper.4
                    final RecentsTransitionHelper this$0;
                    final ActivityOptions.OnAnimationStartedListener val$animStartedListener;
                    final ActivityOptions val$opts;
                    final TaskStack val$stack;
                    final Task val$task;
                    final TaskView val$taskView;
                    final IAppTransitionAnimationSpecsFuture val$transitionFuture;

                    {
                        this.this$0 = this;
                        this.val$stack = taskStack;
                        this.val$task = task;
                        this.val$taskView = taskView;
                        this.val$opts = makeBasic;
                        this.val$transitionFuture = iAppTransitionAnimationSpecsFuture;
                        this.val$animStartedListener = onAnimationStartedListener;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        this.this$0.startTaskActivity(this.val$stack, this.val$task, this.val$taskView, this.val$opts, this.val$transitionFuture, this.val$animStartedListener);
                    }
                });
                EventBus.getDefault().send(launchTaskStartedEvent);
            }
        }
        Recents.getSystemServices().sendCloseSystemWindows("homekey");
    }

    public IRemoteCallback wrapStartedListener(ActivityOptions.OnAnimationStartedListener onAnimationStartedListener) {
        if (onAnimationStartedListener == null) {
            return null;
        }
        return new IRemoteCallback.Stub(this, onAnimationStartedListener) { // from class: com.android.systemui.recents.views.RecentsTransitionHelper.5
            final RecentsTransitionHelper this$0;
            final ActivityOptions.OnAnimationStartedListener val$listener;

            {
                this.this$0 = this;
                this.val$listener = onAnimationStartedListener;
            }

            public void sendResult(Bundle bundle) throws RemoteException {
                this.this$0.mHandler.post(new Runnable(this, this.val$listener) { // from class: com.android.systemui.recents.views.RecentsTransitionHelper.5.1
                    final AnonymousClass5 this$1;
                    final ActivityOptions.OnAnimationStartedListener val$listener;

                    {
                        this.this$1 = this;
                        this.val$listener = r5;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        this.val$listener.onAnimationStarted();
                    }
                });
            }
        };
    }
}
