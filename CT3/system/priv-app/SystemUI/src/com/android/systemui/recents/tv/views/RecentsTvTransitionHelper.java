package com.android.systemui.recents.tv.views;

import android.app.ActivityOptions;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.IRemoteCallback;
import android.os.RemoteException;
import android.util.Log;
import android.view.WindowManagerGlobal;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.events.activity.CancelEnterRecentsWindowAnimationEvent;
import com.android.systemui.recents.events.activity.ExitRecentsWindowFirstAnimationFrameEvent;
import com.android.systemui.recents.events.activity.LaunchTaskFailedEvent;
import com.android.systemui.recents.events.activity.LaunchTaskSucceededEvent;
import com.android.systemui.recents.events.activity.LaunchTvTaskStartedEvent;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.model.TaskStack;
/* loaded from: a.zip:com/android/systemui/recents/tv/views/RecentsTvTransitionHelper.class */
public class RecentsTvTransitionHelper {
    private Context mContext;
    private Handler mHandler;

    public RecentsTvTransitionHelper(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
    }

    private void startTaskActivity(TaskStack taskStack, Task task, TaskCardView taskCardView, ActivityOptions activityOptions, ActivityOptions.OnAnimationStartedListener onAnimationStartedListener) {
        if (Recents.getSystemServices().startActivityFromRecents(this.mContext, task.key, task.title, activityOptions)) {
            int i = 0;
            int indexOfStackTask = taskStack.indexOfStackTask(task);
            if (indexOfStackTask > -1) {
                i = (taskStack.getTaskCount() - indexOfStackTask) - 1;
            }
            EventBus.getDefault().send(new LaunchTaskSucceededEvent(i));
        } else {
            EventBus.getDefault().send(new LaunchTaskFailedEvent());
        }
        Rect focusedThumbnailRect = taskCardView.getFocusedThumbnailRect();
        if (focusedThumbnailRect == null || task.thumbnail == null) {
            return;
        }
        IRemoteCallback iRemoteCallback = null;
        if (onAnimationStartedListener != null) {
            iRemoteCallback = new IRemoteCallback.Stub(this, onAnimationStartedListener) { // from class: com.android.systemui.recents.tv.views.RecentsTvTransitionHelper.3
                final RecentsTvTransitionHelper this$0;
                final ActivityOptions.OnAnimationStartedListener val$animStartedListener;

                {
                    this.this$0 = this;
                    this.val$animStartedListener = onAnimationStartedListener;
                }

                public void sendResult(Bundle bundle) throws RemoteException {
                    this.this$0.mHandler.post(new Runnable(this, this.val$animStartedListener) { // from class: com.android.systemui.recents.tv.views.RecentsTvTransitionHelper.3.1
                        final AnonymousClass3 this$1;
                        final ActivityOptions.OnAnimationStartedListener val$animStartedListener;

                        {
                            this.this$1 = this;
                            this.val$animStartedListener = r5;
                        }

                        @Override // java.lang.Runnable
                        public void run() {
                            if (this.val$animStartedListener != null) {
                                this.val$animStartedListener.onAnimationStarted();
                            }
                        }
                    });
                }
            };
        }
        try {
            WindowManagerGlobal.getWindowManagerService().overridePendingAppTransitionAspectScaledThumb(Bitmap.createScaledBitmap(task.thumbnail, focusedThumbnailRect.width(), focusedThumbnailRect.height(), false), focusedThumbnailRect.left, focusedThumbnailRect.top, focusedThumbnailRect.width(), focusedThumbnailRect.height(), iRemoteCallback, true);
        } catch (RemoteException e) {
            Log.w("RecentsTvTransitionHelper", "Failed to override transition: " + e);
        }
    }

    public void launchTaskFromRecents(TaskStack taskStack, Task task, TaskStackHorizontalGridView taskStackHorizontalGridView, TaskCardView taskCardView, Rect rect, int i) {
        ActivityOptions makeBasic = ActivityOptions.makeBasic();
        if (rect != null) {
            Rect rect2 = rect;
            if (rect.isEmpty()) {
                rect2 = null;
            }
            makeBasic.setLaunchBounds(rect2);
        }
        ActivityOptions.OnAnimationStartedListener onAnimationStartedListener = (task.thumbnail == null || task.thumbnail.getWidth() <= 0 || task.thumbnail.getHeight() <= 0) ? new ActivityOptions.OnAnimationStartedListener(this) { // from class: com.android.systemui.recents.tv.views.RecentsTvTransitionHelper.2
            final RecentsTvTransitionHelper this$0;

            {
                this.this$0 = this;
            }

            public void onAnimationStarted() {
                EventBus.getDefault().send(new ExitRecentsWindowFirstAnimationFrameEvent());
            }
        } : new ActivityOptions.OnAnimationStartedListener(this, task) { // from class: com.android.systemui.recents.tv.views.RecentsTvTransitionHelper.1
            final RecentsTvTransitionHelper this$0;
            final Task val$task;

            {
                this.this$0 = this;
                this.val$task = task;
            }

            public void onAnimationStarted() {
                EventBus.getDefault().send(new CancelEnterRecentsWindowAnimationEvent(this.val$task));
                EventBus.getDefault().send(new ExitRecentsWindowFirstAnimationFrameEvent());
            }
        };
        if (taskCardView == null) {
            startTaskActivity(taskStack, task, taskCardView, makeBasic, onAnimationStartedListener);
            return;
        }
        EventBus.getDefault().send(new LaunchTvTaskStartedEvent(taskCardView));
        startTaskActivity(taskStack, task, taskCardView, makeBasic, onAnimationStartedListener);
    }
}
