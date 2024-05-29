package com.android.systemui.stackdivider;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.ArraySet;
import android.widget.Toast;
import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.events.activity.AppTransitionFinishedEvent;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.stackdivider.events.StartedDragingEvent;
import com.android.systemui.stackdivider.events.StoppedDragingEvent;
/* loaded from: a.zip:com/android/systemui/stackdivider/ForcedResizableInfoActivityController.class */
public class ForcedResizableInfoActivityController {
    private final Context mContext;
    private boolean mDividerDraging;
    private final Handler mHandler = new Handler();
    private final ArraySet<Integer> mPendingTaskIds = new ArraySet<>();
    private final ArraySet<String> mPackagesShownInSession = new ArraySet<>();
    private final Runnable mTimeoutRunnable = new Runnable(this) { // from class: com.android.systemui.stackdivider.ForcedResizableInfoActivityController.1
        final ForcedResizableInfoActivityController this$0;

        {
            this.this$0 = this;
        }

        @Override // java.lang.Runnable
        public void run() {
            this.this$0.showPending();
        }
    };

    public ForcedResizableInfoActivityController(Context context) {
        this.mContext = context;
        EventBus.getDefault().register(this);
        SystemServicesProxy.getInstance(context).registerTaskStackListener(new SystemServicesProxy.TaskStackListener(this) { // from class: com.android.systemui.stackdivider.ForcedResizableInfoActivityController.2
            final ForcedResizableInfoActivityController this$0;

            {
                this.this$0 = this;
            }

            @Override // com.android.systemui.recents.misc.SystemServicesProxy.TaskStackListener
            public void onActivityDismissingDockedStack() {
                this.this$0.activityDismissingDockedStack();
            }

            @Override // com.android.systemui.recents.misc.SystemServicesProxy.TaskStackListener
            public void onActivityForcedResizable(String str, int i) {
                this.this$0.activityForcedResizable(str, i);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void activityDismissingDockedStack() {
        Toast.makeText(this.mContext, 2131493904, 0).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void activityForcedResizable(String str, int i) {
        if (debounce(str)) {
            return;
        }
        this.mPendingTaskIds.add(Integer.valueOf(i));
        postTimeout();
    }

    private boolean debounce(String str) {
        if (str == null) {
            return false;
        }
        if ("com.android.systemui".equals(str)) {
            return true;
        }
        boolean contains = this.mPackagesShownInSession.contains(str);
        this.mPackagesShownInSession.add(str);
        return contains;
    }

    private void postTimeout() {
        this.mHandler.removeCallbacks(this.mTimeoutRunnable);
        this.mHandler.postDelayed(this.mTimeoutRunnable, 1000L);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showPending() {
        this.mHandler.removeCallbacks(this.mTimeoutRunnable);
        for (int size = this.mPendingTaskIds.size() - 1; size >= 0; size--) {
            Intent intent = new Intent(this.mContext, ForcedResizableInfoActivity.class);
            ActivityOptions makeBasic = ActivityOptions.makeBasic();
            makeBasic.setLaunchTaskId(this.mPendingTaskIds.valueAt(size).intValue());
            makeBasic.setTaskOverlay(true);
            this.mContext.startActivity(intent, makeBasic.toBundle());
        }
        this.mPendingTaskIds.clear();
    }

    public void notifyDockedStackExistsChanged(boolean z) {
        if (z) {
            return;
        }
        this.mPackagesShownInSession.clear();
    }

    public final void onBusEvent(AppTransitionFinishedEvent appTransitionFinishedEvent) {
        if (this.mDividerDraging) {
            return;
        }
        showPending();
    }

    public final void onBusEvent(StartedDragingEvent startedDragingEvent) {
        this.mDividerDraging = true;
        this.mHandler.removeCallbacks(this.mTimeoutRunnable);
    }

    public final void onBusEvent(StoppedDragingEvent stoppedDragingEvent) {
        this.mDividerDraging = false;
        showPending();
    }
}
