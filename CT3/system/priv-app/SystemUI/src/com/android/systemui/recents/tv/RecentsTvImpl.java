package com.android.systemui.recents.tv;

import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.SystemClock;
import android.os.UserHandle;
import com.android.systemui.SystemUIApplication;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.RecentsActivityLaunchState;
import com.android.systemui.recents.RecentsImpl;
import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.events.activity.RecentsActivityStartingEvent;
import com.android.systemui.recents.model.RecentsTaskLoader;
import com.android.systemui.recents.model.TaskStack;
import com.android.systemui.recents.model.ThumbnailData;
import com.android.systemui.recents.tv.views.TaskCardView;
import com.android.systemui.statusbar.tv.TvStatusBar;
import com.android.systemui.tv.pip.PipManager;
/* loaded from: a.zip:com/android/systemui/recents/tv/RecentsTvImpl.class */
public class RecentsTvImpl extends RecentsImpl {
    private static final PipManager mPipManager = PipManager.getInstance();

    public RecentsTvImpl(Context context) {
        super(context);
    }

    private ActivityOptions getThumbnailTransitionActivityOptionsForTV(ActivityManager.RunningTaskInfo runningTaskInfo, int i) {
        Rect startingCardThumbnailRect = TaskCardView.getStartingCardThumbnailRect(this.mContext, !mPipManager.isPipShown(), i);
        ThumbnailData taskThumbnail = Recents.getSystemServices().getTaskThumbnail(runningTaskInfo.id);
        if (taskThumbnail.thumbnail != null) {
            return ActivityOptions.makeThumbnailAspectScaleDownAnimation(this.mDummyStackView, Bitmap.createScaledBitmap(taskThumbnail.thumbnail, startingCardThumbnailRect.width(), startingCardThumbnailRect.height(), false), startingCardThumbnailRect.left, startingCardThumbnailRect.top, startingCardThumbnailRect.width(), startingCardThumbnailRect.height(), this.mHandler, null);
        }
        return getUnknownTransitionActivityOptions();
    }

    @Override // com.android.systemui.recents.RecentsImpl
    public void onVisibilityChanged(Context context, boolean z) {
        TvStatusBar tvStatusBar = (TvStatusBar) ((SystemUIApplication) context).getComponent(TvStatusBar.class);
        if (tvStatusBar != null) {
            tvStatusBar.updateRecentsVisibility(z);
        }
    }

    protected void startRecentsActivity(ActivityManager.RunningTaskInfo runningTaskInfo, ActivityOptions activityOptions, boolean z, boolean z2) {
        RecentsActivityLaunchState launchState = Recents.getConfiguration().getLaunchState();
        launchState.launchedFromHome = z;
        launchState.launchedFromApp = z2;
        launchState.launchedToTaskId = runningTaskInfo != null ? runningTaskInfo.id : -1;
        launchState.launchedWithAltTab = this.mTriggeredFromAltTab;
        Intent intent = new Intent();
        intent.setClassName("com.android.systemui", "com.android.systemui.recents.tv.RecentsTvActivity");
        intent.setFlags(276840448);
        if (activityOptions != null) {
            this.mContext.startActivityAsUser(intent, activityOptions.toBundle(), UserHandle.CURRENT);
        } else {
            this.mContext.startActivityAsUser(intent, UserHandle.CURRENT);
        }
        EventBus.getDefault().send(new RecentsActivityStartingEvent());
    }

    @Override // com.android.systemui.recents.RecentsImpl
    protected void startRecentsActivity(ActivityManager.RunningTaskInfo runningTaskInfo, boolean z, boolean z2, int i) {
        RecentsTaskLoader taskLoader = Recents.getTaskLoader();
        if (this.mTriggeredFromAltTab || sInstanceLoadPlan == null) {
            sInstanceLoadPlan = taskLoader.createLoadPlan(this.mContext);
        }
        if (this.mTriggeredFromAltTab || !sInstanceLoadPlan.hasTasks()) {
            taskLoader.preloadTasks(sInstanceLoadPlan, runningTaskInfo.id, !z);
        }
        TaskStack taskStack = sInstanceLoadPlan.getTaskStack();
        if (!z2) {
            startRecentsActivity(runningTaskInfo, ActivityOptions.makeCustomAnimation(this.mContext, -1, -1), false, false);
            return;
        }
        boolean z3 = taskStack.getTaskCount() > 0;
        if (runningTaskInfo == null || z) {
            z3 = false;
        }
        boolean z4 = z3;
        if (z3) {
            ActivityOptions thumbnailTransitionActivityOptionsForTV = getThumbnailTransitionActivityOptionsForTV(runningTaskInfo, taskStack.getTaskCount());
            if (thumbnailTransitionActivityOptionsForTV != null) {
                startRecentsActivity(runningTaskInfo, thumbnailTransitionActivityOptionsForTV, false, true);
                z4 = z3;
            } else {
                z4 = false;
            }
        }
        if (!z4) {
            startRecentsActivity(runningTaskInfo, (ActivityOptions) null, true, false);
        }
        this.mLastToggleTime = SystemClock.elapsedRealtime();
    }
}
