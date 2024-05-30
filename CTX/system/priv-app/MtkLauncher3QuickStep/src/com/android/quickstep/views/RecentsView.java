package com.android.quickstep.views;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.ArraySet;
import android.util.AttributeSet;
import android.util.Property;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ListView;
import com.android.launcher3.BaseActivity;
import com.android.launcher3.DeviceProfile;
import com.android.launcher3.Insettable;
import com.android.launcher3.PagedView;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.anim.AnimatorPlaybackController;
import com.android.launcher3.anim.Interpolators;
import com.android.launcher3.anim.PropertyListBuilder;
import com.android.launcher3.util.PendingAnimation;
import com.android.launcher3.util.SystemUiController;
import com.android.launcher3.util.Themes;
import com.android.quickstep.OverviewCallbacks;
import com.android.quickstep.QuickScrubController;
import com.android.quickstep.RecentsModel;
import com.android.quickstep.TaskUtils;
import com.android.quickstep.util.ClipAnimationHelper;
import com.android.quickstep.util.TaskViewDrawable;
import com.android.quickstep.views.RecentsView;
import com.android.systemui.shared.recents.model.RecentsTaskLoadPlan;
import com.android.systemui.shared.recents.model.RecentsTaskLoader;
import com.android.systemui.shared.recents.model.Task;
import com.android.systemui.shared.recents.model.TaskStack;
import com.android.systemui.shared.recents.model.ThumbnailData;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.shared.system.BackgroundExecutor;
import com.android.systemui.shared.system.PackageManagerWrapper;
import com.android.systemui.shared.system.TaskStackChangeListener;
import java.util.ArrayList;
import java.util.function.Consumer;
@TargetApi(28)
/* loaded from: classes.dex */
public abstract class RecentsView<T extends BaseActivity> extends PagedView implements Insettable {
    private static final int DISMISS_TASK_DURATION = 300;
    private static final float UPDATE_SYSUI_FLAGS_THRESHOLD = 0.6f;
    protected final T mActivity;
    private View mClearAllButton;
    @ViewDebug.ExportedProperty(category = "launcher")
    private float mContentAlpha;
    private final Drawable mEmptyIcon;
    private final CharSequence mEmptyMessage;
    private final int mEmptyMessagePadding;
    private final TextPaint mEmptyMessagePaint;
    private Layout mEmptyTextLayout;
    private final float mFastFlingVelocity;
    private boolean mHandleTaskStackChanges;
    private final SparseBooleanArray mHasVisibleTaskData;
    private ArraySet<TaskView> mIgnoreResetTaskViews;
    private boolean mIsClearAllButtonFullyRevealed;
    private final Point mLastMeasureSize;
    private int mLoadPlanId;
    private final RecentsModel mModel;
    private BaseActivity.MultiWindowModeChangedListener mMultiWindowModeChangedListener;
    private Runnable mNextPageSwitchRunnable;
    private boolean mOverviewStateEnabled;
    private PendingAnimation mPendingAnimation;
    private final QuickScrubController mQuickScrubController;
    private boolean mRunningTaskIconScaledDown;
    private int mRunningTaskId;
    private boolean mRunningTaskTileHidden;
    private final ScrollState mScrollState;
    private boolean mShowEmptyMessage;
    private boolean mSwipeDownShouldLaunchApp;
    private final TaskStackChangeListener mTaskStackListener;
    private final int mTaskTopMargin;
    private final Rect mTempRect;
    private Task mTmpRunningTask;
    private static final String TAG = RecentsView.class.getSimpleName();
    private static final float[] sTempFloatArray = new float[3];

    /* loaded from: classes.dex */
    public static class ScrollState {
        public float linearInterpolation;
    }

    protected abstract void getTaskSize(DeviceProfile deviceProfile, Rect rect);

    protected abstract void onAllTasksRemoved();

    public abstract boolean shouldUseMultiWindowTaskSizeStrategy();

    /* renamed from: com.android.quickstep.views.RecentsView$1 */
    /* loaded from: classes.dex */
    public class AnonymousClass1 extends TaskStackChangeListener {
        AnonymousClass1() {
            RecentsView.this = r1;
        }

        @Override // com.android.systemui.shared.system.TaskStackChangeListener
        public void onTaskSnapshotChanged(int i, ThumbnailData thumbnailData) {
            if (!RecentsView.this.mHandleTaskStackChanges) {
                return;
            }
            RecentsView.this.updateThumbnail(i, thumbnailData);
        }

        @Override // com.android.systemui.shared.system.TaskStackChangeListener
        public void onActivityPinned(String str, int i, int i2, int i3) {
            TaskView taskView;
            if (RecentsView.this.mHandleTaskStackChanges && TaskUtils.checkCurrentOrManagedUserId(i, RecentsView.this.getContext()) && (taskView = RecentsView.this.getTaskView(i2)) != null) {
                RecentsView.this.removeView(taskView);
            }
        }

        @Override // com.android.systemui.shared.system.TaskStackChangeListener
        public void onActivityUnpinned() {
            if (!RecentsView.this.mHandleTaskStackChanges) {
                return;
            }
            RecentsView.this.reloadIfNeeded();
        }

        @Override // com.android.systemui.shared.system.TaskStackChangeListener
        public void onTaskRemoved(final int i) {
            if (!RecentsView.this.mHandleTaskStackChanges) {
                return;
            }
            BackgroundExecutor.get().submit(new Runnable() { // from class: com.android.quickstep.views.-$$Lambda$RecentsView$1$33q0Vx0rJJiNf-2oiC6R0mvbHw0
                @Override // java.lang.Runnable
                public final void run() {
                    RecentsView.AnonymousClass1.lambda$onTaskRemoved$2(RecentsView.AnonymousClass1.this, i);
                }
            });
        }

        public static /* synthetic */ void lambda$onTaskRemoved$2(AnonymousClass1 anonymousClass1, int i) {
            Handler handler;
            final TaskView taskView = RecentsView.this.getTaskView(i);
            if (taskView == null || (handler = taskView.getHandler()) == null) {
                return;
            }
            Task.TaskKey taskKey = taskView.getTask().key;
            if (PackageManagerWrapper.getInstance().getActivityInfo(taskKey.getComponent(), taskKey.userId) == null) {
                handler.post(new Runnable() { // from class: com.android.quickstep.views.-$$Lambda$RecentsView$1$r9L02FCZWuaGZM7l0gaRuH0U05U
                    @Override // java.lang.Runnable
                    public final void run() {
                        RecentsView.this.dismissTask(taskView, true, false);
                    }
                });
                return;
            }
            RecentsTaskLoadPlan recentsTaskLoadPlan = new RecentsTaskLoadPlan(RecentsView.this.getContext());
            RecentsTaskLoadPlan.PreloadOptions preloadOptions = new RecentsTaskLoadPlan.PreloadOptions();
            preloadOptions.loadTitles = false;
            recentsTaskLoadPlan.preloadPlan(preloadOptions, RecentsView.this.mModel.getRecentsTaskLoader(), -1, UserHandle.myUserId());
            if (recentsTaskLoadPlan.getTaskStack().findTaskWithId(i) == null) {
                handler.post(new Runnable() { // from class: com.android.quickstep.views.-$$Lambda$RecentsView$1$q8v5oD8qbHuIEk79BWIrapSXuXk
                    @Override // java.lang.Runnable
                    public final void run() {
                        RecentsView.this.dismissTask(taskView, true, false);
                    }
                });
            }
        }

        @Override // com.android.systemui.shared.system.TaskStackChangeListener
        public void onPinnedStackAnimationStarted() {
            RecentsView.this.mActivity.clearForceInvisibleFlag(1);
        }
    }

    public static /* synthetic */ void lambda$new$0(RecentsView recentsView, boolean z) {
        if (!z && recentsView.mOverviewStateEnabled) {
            recentsView.reloadIfNeeded();
        }
    }

    public RecentsView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mTempRect = new Rect();
        this.mScrollState = new ScrollState();
        this.mHasVisibleTaskData = new SparseBooleanArray();
        this.mTaskStackListener = new AnonymousClass1();
        this.mLoadPlanId = -1;
        this.mRunningTaskId = -1;
        this.mRunningTaskIconScaledDown = false;
        this.mContentAlpha = 1.0f;
        this.mIgnoreResetTaskViews = new ArraySet<>();
        this.mLastMeasureSize = new Point();
        this.mMultiWindowModeChangedListener = new BaseActivity.MultiWindowModeChangedListener() { // from class: com.android.quickstep.views.-$$Lambda$RecentsView$cuqrsa-tyQadsC84Z2h0Rmwf57k
            @Override // com.android.launcher3.BaseActivity.MultiWindowModeChangedListener
            public final void onMultiWindowModeChanged(boolean z) {
                RecentsView.lambda$new$0(RecentsView.this, z);
            }
        };
        setPageSpacing(getResources().getDimensionPixelSize(R.dimen.recents_page_spacing));
        enableFreeScroll(true);
        setClipToOutline(true);
        this.mFastFlingVelocity = getResources().getDimensionPixelSize(R.dimen.recents_fast_fling_velocity);
        this.mActivity = (T) BaseActivity.fromContext(context);
        this.mQuickScrubController = new QuickScrubController(this.mActivity, this);
        this.mModel = RecentsModel.getInstance(context);
        this.mIsRtl = true ^ Utilities.isRtl(getResources());
        setLayoutDirection(this.mIsRtl ? 1 : 0);
        this.mTaskTopMargin = getResources().getDimensionPixelSize(R.dimen.task_thumbnail_top_margin);
        this.mEmptyIcon = context.getDrawable(R.drawable.ic_empty_recents);
        this.mEmptyIcon.setCallback(this);
        this.mEmptyMessage = context.getText(R.string.recents_empty_message);
        this.mEmptyMessagePaint = new TextPaint();
        this.mEmptyMessagePaint.setColor(Themes.getAttrColor(context, 16842806));
        this.mEmptyMessagePaint.setTextSize(getResources().getDimension(R.dimen.recents_empty_message_text_size));
        this.mEmptyMessagePadding = getResources().getDimensionPixelSize(R.dimen.recents_empty_message_text_padding);
        setWillNotDraw(false);
        updateEmptyMessage();
        setFocusable(false);
    }

    public boolean isRtl() {
        return this.mIsRtl;
    }

    public TaskView updateThumbnail(int i, ThumbnailData thumbnailData) {
        TaskView taskView = getTaskView(i);
        if (taskView != null) {
            taskView.onTaskDataLoaded(taskView.getTask(), thumbnailData);
        }
        return taskView;
    }

    @Override // android.view.View
    protected void onWindowVisibilityChanged(int i) {
        super.onWindowVisibilityChanged(i);
        updateTaskStackListenerState();
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        updateTaskStackListenerState();
        this.mActivity.addMultiWindowModeChangedListener(this.mMultiWindowModeChangedListener);
        ActivityManagerWrapper.getInstance().registerTaskStackListener(this.mTaskStackListener);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        updateTaskStackListenerState();
        this.mActivity.removeMultiWindowModeChangedListener(this.mMultiWindowModeChangedListener);
        ActivityManagerWrapper.getInstance().unregisterTaskStackListener(this.mTaskStackListener);
    }

    @Override // com.android.launcher3.PagedView, android.view.ViewGroup
    public void onViewRemoved(View view) {
        super.onViewRemoved(view);
        Task task = ((TaskView) view).getTask();
        if (this.mHasVisibleTaskData.get(task.key.id)) {
            this.mHasVisibleTaskData.delete(task.key.id);
            RecentsTaskLoader recentsTaskLoader = this.mModel.getRecentsTaskLoader();
            recentsTaskLoader.unloadTaskData(task);
            recentsTaskLoader.getHighResThumbnailLoader().onTaskInvisible(task);
        }
        onChildViewsChanged();
    }

    public boolean isTaskViewVisible(TaskView taskView) {
        return Math.abs(indexOfChild(taskView) - getNextPage()) <= 1;
    }

    public TaskView getTaskView(int i) {
        for (int i2 = 0; i2 < getChildCount(); i2++) {
            TaskView taskView = (TaskView) getChildAt(i2);
            if (taskView.getTask().key.id == i) {
                return taskView;
            }
        }
        return null;
    }

    public void setOverviewStateEnabled(boolean z) {
        this.mOverviewStateEnabled = z;
        updateTaskStackListenerState();
    }

    public void setNextPageSwitchRunnable(Runnable runnable) {
        this.mNextPageSwitchRunnable = runnable;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.launcher3.PagedView
    public void onPageEndTransition() {
        super.onPageEndTransition();
        if (this.mNextPageSwitchRunnable != null) {
            this.mNextPageSwitchRunnable.run();
            this.mNextPageSwitchRunnable = null;
        }
        if (getNextPage() > 0) {
            setSwipeDownShouldLaunchApp(true);
        }
    }

    private int getScrollEnd() {
        if (this.mIsRtl) {
            return 0;
        }
        return this.mMaxScrollX;
    }

    private float calculateClearAllButtonAlpha() {
        int childCount = getChildCount();
        if (this.mShowEmptyMessage || childCount == 0 || this.mPageScrolls == null || childCount != this.mPageScrolls.length) {
            return 0.0f;
        }
        int scrollEnd = getScrollEnd();
        int scrollForPage = getScrollForPage(childCount - 1);
        int i = scrollEnd - scrollForPage;
        if (i == 0) {
            return 0.0f;
        }
        float scrollX = (getScrollX() - scrollForPage) / i;
        if (scrollX > 1.0f) {
            return 0.0f;
        }
        return Math.max(scrollX, 0.0f);
    }

    private void updateClearAllButtonAlpha() {
        boolean z;
        if (this.mClearAllButton != null) {
            float calculateClearAllButtonAlpha = calculateClearAllButtonAlpha();
            if (calculateClearAllButtonAlpha != 1.0f) {
                z = false;
            } else {
                z = true;
            }
            if (this.mIsClearAllButtonFullyRevealed != z) {
                this.mIsClearAllButtonFullyRevealed = z;
                this.mClearAllButton.setImportantForAccessibility(z ? 1 : 2);
            }
            this.mClearAllButton.setAlpha(calculateClearAllButtonAlpha * this.mContentAlpha);
        }
    }

    @Override // android.view.View
    protected void onScrollChanged(int i, int i2, int i3, int i4) {
        super.onScrollChanged(i, i2, i3, i4);
        updateClearAllButtonAlpha();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.launcher3.PagedView
    public void restoreScrollOnLayout() {
        if (this.mIsClearAllButtonFullyRevealed) {
            scrollAndForceFinish(getScrollEnd());
        } else {
            super.restoreScrollOnLayout();
        }
    }

    @Override // com.android.launcher3.PagedView, android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getAction() == 0 && this.mTouchState == 0 && this.mScroller.isFinished() && this.mIsClearAllButtonFullyRevealed) {
            this.mClearAllButton.getHitRect(this.mTempRect);
            this.mTempRect.offset(-getLeft(), -getTop());
            if (this.mTempRect.contains((int) motionEvent.getX(), (int) motionEvent.getY())) {
                return false;
            }
        }
        if (motionEvent.getAction() == 1 && this.mShowEmptyMessage) {
            onAllTasksRemoved();
        }
        return super.onTouchEvent(motionEvent);
    }

    public void applyLoadPlan(final RecentsTaskLoadPlan recentsTaskLoadPlan) {
        if (this.mPendingAnimation != null) {
            this.mPendingAnimation.addEndListener(new Consumer() { // from class: com.android.quickstep.views.-$$Lambda$RecentsView$AenGcjDqD14z7boU9lHV0QeB4c8
                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    PendingAnimation.OnEndListener onEndListener = (PendingAnimation.OnEndListener) obj;
                    RecentsView.this.applyLoadPlan(recentsTaskLoadPlan);
                }
            });
            return;
        }
        TaskStack taskStack = recentsTaskLoadPlan != null ? recentsTaskLoadPlan.getTaskStack() : null;
        if (taskStack == null) {
            removeAllViews();
            onTaskStackUpdated();
            return;
        }
        int childCount = getChildCount();
        LayoutInflater from = LayoutInflater.from(getContext());
        ArrayList arrayList = new ArrayList(taskStack.getTasks());
        int size = arrayList.size();
        for (int childCount2 = getChildCount(); childCount2 < size; childCount2++) {
            addView((TaskView) from.inflate(R.layout.task, (ViewGroup) this, false));
        }
        while (getChildCount() > size) {
            removeView((TaskView) getChildAt(getChildCount() - 1));
        }
        unloadVisibleTaskData();
        for (int i = size - 1; i >= 0; i--) {
            ((TaskView) getChildAt((size - i) - 1)).bind((Task) arrayList.get(i));
        }
        resetTaskVisuals();
        if (childCount != getChildCount()) {
            this.mQuickScrubController.snapToNextTaskIfAvailable();
        }
        onTaskStackUpdated();
    }

    protected void onTaskStackUpdated() {
    }

    public void resetTaskVisuals() {
        for (int childCount = getChildCount() - 1; childCount >= 0; childCount--) {
            TaskView taskView = (TaskView) getChildAt(childCount);
            if (!this.mIgnoreResetTaskViews.contains(taskView)) {
                taskView.resetVisualProperties();
            }
        }
        if (this.mRunningTaskTileHidden) {
            setRunningTaskHidden(this.mRunningTaskTileHidden);
        }
        applyIconScale(false);
        updateCurveProperties();
        loadVisibleTaskData();
    }

    private void updateTaskStackListenerState() {
        boolean z = this.mOverviewStateEnabled && isAttachedToWindow() && getWindowVisibility() == 0;
        if (z != this.mHandleTaskStackChanges) {
            this.mHandleTaskStackChanges = z;
            if (z) {
                reloadIfNeeded();
            }
        }
    }

    @Override // com.android.launcher3.Insettable
    public void setInsets(Rect rect) {
        this.mInsets.set(rect);
        DeviceProfile deviceProfile = this.mActivity.getDeviceProfile();
        getTaskSize(deviceProfile, this.mTempRect);
        this.mTempRect.top -= this.mTaskTopMargin;
        setPadding(this.mTempRect.left - this.mInsets.left, this.mTempRect.top - this.mInsets.top, (deviceProfile.availableWidthPx + this.mInsets.left) - this.mTempRect.right, (deviceProfile.availableHeightPx + this.mInsets.top) - this.mTempRect.bottom);
    }

    public void getTaskSize(Rect rect) {
        getTaskSize(this.mActivity.getDeviceProfile(), rect);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.launcher3.PagedView
    public boolean computeScrollHelper() {
        boolean computeScrollHelper = super.computeScrollHelper();
        updateCurveProperties();
        boolean z = true;
        boolean z2 = false;
        if (computeScrollHelper || this.mTouchState == 1) {
            if (computeScrollHelper) {
                if (this.mScroller.getCurrVelocity() <= this.mFastFlingVelocity) {
                    z = false;
                }
                z2 = z;
            }
            loadVisibleTaskData();
        }
        this.mModel.getRecentsTaskLoader().getHighResThumbnailLoader().setFlingingFast(z2);
        return computeScrollHelper;
    }

    public void updateCurveProperties() {
        if (getPageCount() != 0) {
            if (getPageAt(0).getMeasuredWidth() == 0) {
                return;
            }
            int normalChildWidth = getNormalChildWidth() / 2;
            int paddingLeft = this.mInsets.left + getPaddingLeft() + getScrollX() + normalChildWidth;
            int measuredWidth = getMeasuredWidth() / 2;
            int i = this.mPageSpacing;
            int pageCount = getPageCount();
            for (int i2 = 0; i2 < pageCount; i2++) {
                TaskView pageAt = getPageAt(i2);
                this.mScrollState.linearInterpolation = Math.min(1.0f, Math.abs(paddingLeft - ((pageAt.getLeft() + pageAt.getTranslationX()) + normalChildWidth)) / ((measuredWidth + normalChildWidth) + i));
                pageAt.onPageScroll(this.mScrollState);
            }
        }
    }

    public void loadVisibleTaskData() {
        if (!this.mOverviewStateEnabled) {
            return;
        }
        RecentsTaskLoader recentsTaskLoader = this.mModel.getRecentsTaskLoader();
        int pageNearestToCenterOfScreen = getPageNearestToCenterOfScreen();
        int max = Math.max(0, pageNearestToCenterOfScreen - 2);
        int min = Math.min(pageNearestToCenterOfScreen + 2, getChildCount() - 1);
        int childCount = getChildCount();
        int i = 0;
        while (i < childCount) {
            Task task = ((TaskView) getChildAt(i)).getTask();
            boolean z = max <= i && i <= min;
            if (z) {
                if (task != this.mTmpRunningTask) {
                    if (!this.mHasVisibleTaskData.get(task.key.id)) {
                        recentsTaskLoader.loadTaskData(task);
                        recentsTaskLoader.getHighResThumbnailLoader().onTaskVisible(task);
                    }
                    this.mHasVisibleTaskData.put(task.key.id, z);
                }
            } else {
                if (this.mHasVisibleTaskData.get(task.key.id)) {
                    recentsTaskLoader.unloadTaskData(task);
                    recentsTaskLoader.getHighResThumbnailLoader().onTaskInvisible(task);
                }
                this.mHasVisibleTaskData.delete(task.key.id);
            }
            i++;
        }
    }

    private void unloadVisibleTaskData() {
        RecentsTaskLoader recentsTaskLoader = this.mModel.getRecentsTaskLoader();
        for (int i = 0; i < this.mHasVisibleTaskData.size(); i++) {
            if (this.mHasVisibleTaskData.valueAt(i)) {
                Task task = getTaskView(this.mHasVisibleTaskData.keyAt(i)).getTask();
                recentsTaskLoader.unloadTaskData(task);
                recentsTaskLoader.getHighResThumbnailLoader().onTaskInvisible(task);
            }
        }
        this.mHasVisibleTaskData.clear();
    }

    public void reset() {
        this.mRunningTaskId = -1;
        this.mRunningTaskTileHidden = false;
        unloadVisibleTaskData();
        setCurrentPage(0);
        OverviewCallbacks.get(getContext()).onResetOverview();
    }

    public void reloadIfNeeded() {
        if (!this.mModel.isLoadPlanValid(this.mLoadPlanId)) {
            this.mLoadPlanId = this.mModel.loadTasks(this.mRunningTaskId, new $$Lambda$RecentsView$w02bBzSWizaR4dIzSj9kQ73I7BA(this));
        }
    }

    public void showTask(int i) {
        if (getChildCount() == 0) {
            TaskView taskView = (TaskView) LayoutInflater.from(getContext()).inflate(R.layout.task, (ViewGroup) this, false);
            addView(taskView);
            this.mTmpRunningTask = new Task(new Task.TaskKey(i, 0, new Intent(), 0, 0L), null, null, "", "", 0, 0, false, true, false, false, new ActivityManager.TaskDescription(), 0, new ComponentName("", ""), false);
            taskView.bind(this.mTmpRunningTask);
        }
        setCurrentTask(i);
    }

    public void setRunningTaskHidden(boolean z) {
        this.mRunningTaskTileHidden = z;
        TaskView taskView = getTaskView(this.mRunningTaskId);
        if (taskView != null) {
            taskView.setAlpha(z ? 0.0f : this.mContentAlpha);
        }
    }

    public void setCurrentTask(int i) {
        boolean z = this.mRunningTaskTileHidden;
        boolean z2 = this.mRunningTaskIconScaledDown;
        setRunningTaskIconScaledDown(false, false);
        setRunningTaskHidden(false);
        this.mRunningTaskId = i;
        setRunningTaskIconScaledDown(z2, false);
        setRunningTaskHidden(z);
        setCurrentPage(0);
        this.mLoadPlanId = this.mModel.loadTasks(i, new $$Lambda$RecentsView$w02bBzSWizaR4dIzSj9kQ73I7BA(this));
    }

    public void showNextTask() {
        TaskView taskView = getTaskView(this.mRunningTaskId);
        if (taskView == null) {
            if (getChildCount() > 0) {
                ((TaskView) getChildAt(0)).launchTask(true);
                return;
            }
            return;
        }
        int max = Math.max(0, Math.min(getChildCount() - 1, indexOfChild(taskView) + 1));
        if (max < getChildCount()) {
            ((TaskView) getChildAt(max)).launchTask(true);
        }
    }

    public QuickScrubController getQuickScrubController() {
        return this.mQuickScrubController;
    }

    public void setRunningTaskIconScaledDown(boolean z, boolean z2) {
        if (this.mRunningTaskIconScaledDown == z) {
            return;
        }
        this.mRunningTaskIconScaledDown = z;
        applyIconScale(z2);
    }

    private void applyIconScale(boolean z) {
        float f = this.mRunningTaskIconScaledDown ? 0.0f : 1.0f;
        TaskView taskView = getTaskView(this.mRunningTaskId);
        if (taskView != null) {
            if (z) {
                taskView.animateIconToScaleAndDim(f);
            } else {
                taskView.setIconScaleAndDim(f);
            }
        }
    }

    public void setSwipeDownShouldLaunchApp(boolean z) {
        this.mSwipeDownShouldLaunchApp = z;
    }

    public boolean shouldSwipeDownLaunchApp() {
        return this.mSwipeDownShouldLaunchApp;
    }

    /* loaded from: classes.dex */
    public interface PageCallbacks {
        default void onPageScroll(ScrollState scrollState) {
        }
    }

    public void addIgnoreResetTask(TaskView taskView) {
        this.mIgnoreResetTaskViews.add(taskView);
    }

    public void removeIgnoreResetTask(TaskView taskView) {
        this.mIgnoreResetTaskViews.remove(taskView);
    }

    private void addDismissedTaskAnimations(View view, AnimatorSet animatorSet, long j) {
        addAnim(ObjectAnimator.ofFloat(view, ALPHA, 0.0f), j, Interpolators.ACCEL_2, animatorSet);
        addAnim(ObjectAnimator.ofFloat(view, TRANSLATION_Y, -view.getHeight()), j, Interpolators.LINEAR, animatorSet);
    }

    private void removeTask(Task task, int i, PendingAnimation.OnEndListener onEndListener, boolean z) {
        if (task != null) {
            ActivityManagerWrapper.getInstance().removeTask(task.key.id);
            if (z) {
                this.mActivity.getUserEventDispatcher().logTaskLaunchOrDismiss(onEndListener.logAction, 1, i, TaskUtils.getComponentKeyForTask(task.key));
            }
        }
    }

    public PendingAnimation createTaskDismissAnimation(final TaskView taskView, boolean z, final boolean z2, long j) {
        int i;
        int i2;
        int i3;
        int i4;
        int i5;
        int[] iArr;
        boolean z3;
        int i6;
        AnimatorSet animatorSet = new AnimatorSet();
        PendingAnimation pendingAnimation = new PendingAnimation(animatorSet);
        int childCount = getChildCount();
        if (childCount == 0) {
            return pendingAnimation;
        }
        int[] iArr2 = new int[childCount];
        int i7 = 0;
        getPageScrolls(iArr2, false, SIMPLE_SCROLL_LOGIC);
        int[] iArr3 = new int[childCount];
        getPageScrolls(iArr3, false, new PagedView.ComputePageScrollsLogic() { // from class: com.android.quickstep.views.-$$Lambda$RecentsView$22olkO-xTCGy8V4w_dNEM5CXDkY
            @Override // com.android.launcher3.PagedView.ComputePageScrollsLogic
            public final boolean shouldIncludeView(View view) {
                return RecentsView.lambda$createTaskDismissAnimation$2(TaskView.this, view);
            }
        });
        int i8 = this.mIsRtl ? childCount - 1 : 0;
        if (!this.mIsRtl) {
            i = childCount - 1;
        } else {
            i = 0;
        }
        if (childCount > 1) {
            if (!this.mIsRtl) {
                i6 = childCount - 2;
            } else {
                i6 = 1;
            }
            i2 = iArr2[i] - iArr2[i6];
        } else {
            i2 = 0;
        }
        final int indexOfChild = indexOfChild(taskView);
        boolean z4 = false;
        while (i7 < childCount) {
            View childAt = getChildAt(i7);
            if (childAt == taskView) {
                if (z) {
                    addDismissedTaskAnimations(taskView, animatorSet, j);
                }
                i3 = childCount;
                iArr = iArr2;
                i4 = i8;
                i5 = i;
            } else {
                i3 = childCount;
                int i9 = this.mIsRtl ? i2 : 0;
                i4 = i8;
                if (this.mCurrentPage == indexOfChild) {
                    i5 = i;
                    if (this.mCurrentPage == (this.mIsRtl ? i4 : i)) {
                        i9 += this.mIsRtl ? -i2 : i2;
                    }
                } else {
                    i5 = i;
                    if (indexOfChild == this.mCurrentPage - 1) {
                        i9 += this.mIsRtl ? -i2 : i2;
                    }
                }
                int i10 = (iArr3[i7] - iArr2[i7]) + i9;
                if (i10 != 0) {
                    iArr = iArr2;
                    z3 = true;
                    addAnim(ObjectAnimator.ofFloat(childAt, TRANSLATION_X, i10), j, Interpolators.ACCEL, animatorSet);
                    z4 = true;
                    i7++;
                    childCount = i3;
                    i8 = i4;
                    i = i5;
                    iArr2 = iArr;
                } else {
                    iArr = iArr2;
                }
            }
            z3 = true;
            i7++;
            childCount = i3;
            i8 = i4;
            i = i5;
            iArr2 = iArr;
        }
        if (z4) {
            ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
            ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.quickstep.views.-$$Lambda$RecentsView$O8Draeq9XtDwd0zxi6MdnjpVeKc
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    RecentsView.this.updateCurveProperties();
                }
            });
            animatorSet.play(ofFloat);
        }
        if (z) {
            taskView.setTranslationZ(0.1f);
        }
        this.mPendingAnimation = pendingAnimation;
        this.mPendingAnimation.addEndListener(new Consumer() { // from class: com.android.quickstep.views.-$$Lambda$RecentsView$W4rpBvRw7hjG-nky8UfZ86QDTwk
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                RecentsView.lambda$createTaskDismissAnimation$4(RecentsView.this, z2, taskView, indexOfChild, (PendingAnimation.OnEndListener) obj);
            }
        });
        return pendingAnimation;
    }

    public static /* synthetic */ boolean lambda$createTaskDismissAnimation$2(TaskView taskView, View view) {
        return (view.getVisibility() == 8 || view == taskView) ? false : true;
    }

    public static /* synthetic */ void lambda$createTaskDismissAnimation$4(RecentsView recentsView, boolean z, TaskView taskView, int i, PendingAnimation.OnEndListener onEndListener) {
        if (onEndListener.isSuccess) {
            if (z) {
                recentsView.removeTask(taskView.getTask(), i, onEndListener, true);
            }
            int i2 = recentsView.mCurrentPage;
            if (i < i2) {
                i2--;
            }
            recentsView.removeView(taskView);
            if (recentsView.getChildCount() == 0) {
                recentsView.onAllTasksRemoved();
            } else if (!recentsView.mIsClearAllButtonFullyRevealed) {
                recentsView.snapToPageImmediately(i2);
            }
        }
        recentsView.resetTaskVisuals();
        recentsView.mPendingAnimation = null;
    }

    public PendingAnimation createAllTasksDismissAnimation(long j) {
        AnimatorSet animatorSet = new AnimatorSet();
        PendingAnimation pendingAnimation = new PendingAnimation(animatorSet);
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            addDismissedTaskAnimations(getChildAt(i), animatorSet, j);
        }
        this.mPendingAnimation = pendingAnimation;
        this.mPendingAnimation.addEndListener(new Consumer() { // from class: com.android.quickstep.views.-$$Lambda$RecentsView$vaMQpg8ISJL-eHsXQ8-CM1-N9b0
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                RecentsView.lambda$createAllTasksDismissAnimation$5(RecentsView.this, (PendingAnimation.OnEndListener) obj);
            }
        });
        return pendingAnimation;
    }

    public static /* synthetic */ void lambda$createAllTasksDismissAnimation$5(RecentsView recentsView, PendingAnimation.OnEndListener onEndListener) {
        if (onEndListener.isSuccess) {
            while (recentsView.getChildCount() != 0) {
                TaskView pageAt = recentsView.getPageAt(recentsView.getChildCount() - 1);
                recentsView.removeTask(pageAt.getTask(), -1, onEndListener, false);
                recentsView.removeView(pageAt);
            }
            recentsView.onAllTasksRemoved();
        }
        recentsView.mPendingAnimation = null;
    }

    private static void addAnim(ObjectAnimator objectAnimator, long j, TimeInterpolator timeInterpolator, AnimatorSet animatorSet) {
        objectAnimator.setDuration(j).setInterpolator(timeInterpolator);
        animatorSet.play(objectAnimator);
    }

    private boolean snapToPageRelative(int i, boolean z) {
        if (getPageCount() == 0) {
            return false;
        }
        int nextPage = getNextPage() + i;
        if (z || (nextPage >= 0 && nextPage < getChildCount())) {
            snapToPage((nextPage + getPageCount()) % getPageCount());
            return true;
        }
        return false;
    }

    private void runDismissAnimation(final PendingAnimation pendingAnimation) {
        AnimatorPlaybackController wrap = AnimatorPlaybackController.wrap(pendingAnimation.anim, 300L);
        wrap.dispatchOnStart();
        wrap.setEndAction(new Runnable() { // from class: com.android.quickstep.views.-$$Lambda$RecentsView$dgGE3h8VV7RqwRWGJ-rZYKXyLCQ
            @Override // java.lang.Runnable
            public final void run() {
                PendingAnimation.this.finish(true, 3);
            }
        });
        wrap.getAnimationPlayer().setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
        wrap.start();
    }

    public void dismissTask(TaskView taskView, boolean z, boolean z2) {
        runDismissAnimation(createTaskDismissAnimation(taskView, z, z2, 300L));
    }

    public void dismissAllTasks() {
        runDismissAnimation(createAllTasksDismissAnimation(300L));
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        if (keyEvent.getAction() == 0) {
            int keyCode = keyEvent.getKeyCode();
            if (keyCode == 61) {
                return snapToPageRelative(keyEvent.isShiftPressed() ? -1 : 1, keyEvent.isAltPressed());
            } else if (keyCode == 67 || keyCode == 112) {
                dismissTask((TaskView) getChildAt(getNextPage()), true, true);
                return true;
            } else if (keyCode != 158) {
                switch (keyCode) {
                    case 21:
                        return snapToPageRelative(this.mIsRtl ? 1 : -1, false);
                    case 22:
                        return snapToPageRelative(this.mIsRtl ? -1 : 1, false);
                }
            } else if (keyEvent.isAltPressed()) {
                dismissTask((TaskView) getChildAt(getNextPage()), true, true);
                return true;
            }
        }
        return super.dispatchKeyEvent(keyEvent);
    }

    @Override // android.view.View
    protected void onFocusChanged(boolean z, int i, @Nullable Rect rect) {
        super.onFocusChanged(z, i, rect);
        if (z && getChildCount() > 0) {
            if (i != 17 && i != 66) {
                switch (i) {
                    case 1:
                        break;
                    case 2:
                        setCurrentPage(0);
                        return;
                    default:
                        return;
                }
            }
            setCurrentPage(getChildCount() - 1);
        }
    }

    public float getContentAlpha() {
        return this.mContentAlpha;
    }

    public void setContentAlpha(float f) {
        float boundToRange = Utilities.boundToRange(f, 0.0f, 1.0f);
        this.mContentAlpha = boundToRange;
        for (int childCount = getChildCount() - 1; childCount >= 0; childCount--) {
            TaskView pageAt = getPageAt(childCount);
            if (!this.mRunningTaskTileHidden || pageAt.getTask().key.id != this.mRunningTaskId) {
                getChildAt(childCount).setAlpha(boundToRange);
            }
        }
        int round = Math.round(boundToRange * 255.0f);
        this.mEmptyMessagePaint.setAlpha(round);
        this.mEmptyIcon.setAlpha(round);
        updateClearAllButtonAlpha();
    }

    private float[] getAdjacentScaleAndTranslation(TaskView taskView, float f, float f2) {
        float width = taskView.getWidth() * (f - taskView.getCurveScale());
        sTempFloatArray[0] = f;
        float[] fArr = sTempFloatArray;
        if (this.mIsRtl) {
            width = -width;
        }
        fArr[1] = width;
        sTempFloatArray[2] = f2;
        return sTempFloatArray;
    }

    @Override // com.android.launcher3.PagedView, android.view.ViewGroup
    public void onViewAdded(View view) {
        super.onViewAdded(view);
        view.setAlpha(this.mContentAlpha);
        onChildViewsChanged();
    }

    @Override // com.android.launcher3.PagedView
    public TaskView getPageAt(int i) {
        return (TaskView) getChildAt(i);
    }

    public void updateEmptyMessage() {
        boolean z = false;
        boolean z2 = getChildCount() == 0;
        if (this.mLastMeasureSize.x != getWidth() || this.mLastMeasureSize.y != getHeight()) {
            z = true;
        }
        if (z2 == this.mShowEmptyMessage && !z) {
            return;
        }
        setContentDescription(z2 ? this.mEmptyMessage : "");
        this.mShowEmptyMessage = z2;
        updateEmptyStateUi(z);
        invalidate();
    }

    @Override // com.android.launcher3.PagedView, android.view.ViewGroup, android.view.View
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        updateEmptyStateUi(z);
        setPivotY((((this.mInsets.top + getPaddingTop()) + this.mTaskTopMargin) + ((getHeight() - this.mInsets.bottom) - getPaddingBottom())) / 2);
        setPivotX(((this.mInsets.left + getPaddingLeft()) + ((getWidth() - this.mInsets.right) - getPaddingRight())) / 2);
    }

    private void updateEmptyStateUi(boolean z) {
        boolean z2 = getWidth() > 0 && getHeight() > 0;
        if (z && z2) {
            this.mEmptyTextLayout = null;
            this.mLastMeasureSize.set(getWidth(), getHeight());
        }
        updateClearAllButtonAlpha();
        if (this.mShowEmptyMessage && z2 && this.mEmptyTextLayout == null) {
            this.mEmptyTextLayout = StaticLayout.Builder.obtain(this.mEmptyMessage, 0, this.mEmptyMessage.length(), this.mEmptyMessagePaint, (this.mLastMeasureSize.x - this.mEmptyMessagePadding) - this.mEmptyMessagePadding).setAlignment(Layout.Alignment.ALIGN_CENTER).build();
            int height = (this.mLastMeasureSize.y - ((this.mEmptyTextLayout.getHeight() + this.mEmptyMessagePadding) + this.mEmptyIcon.getIntrinsicHeight())) / 2;
            int intrinsicWidth = (this.mLastMeasureSize.x - this.mEmptyIcon.getIntrinsicWidth()) / 2;
            this.mEmptyIcon.setBounds(intrinsicWidth, height, this.mEmptyIcon.getIntrinsicWidth() + intrinsicWidth, this.mEmptyIcon.getIntrinsicHeight() + height);
        }
    }

    @Override // android.view.View
    protected boolean verifyDrawable(Drawable drawable) {
        return super.verifyDrawable(drawable) || (this.mShowEmptyMessage && drawable == this.mEmptyIcon);
    }

    public void maybeDrawEmptyMessage(Canvas canvas) {
        if (this.mShowEmptyMessage && this.mEmptyTextLayout != null) {
            this.mTempRect.set(this.mInsets.left + getPaddingLeft(), this.mInsets.top + getPaddingTop(), this.mInsets.right + getPaddingRight(), this.mInsets.bottom + getPaddingBottom());
            canvas.save();
            canvas.translate(getScrollX() + ((this.mTempRect.left - this.mTempRect.right) / 2), (this.mTempRect.top - this.mTempRect.bottom) / 2);
            this.mEmptyIcon.draw(canvas);
            canvas.translate(this.mEmptyMessagePadding, this.mEmptyIcon.getBounds().bottom + this.mEmptyMessagePadding);
            this.mEmptyTextLayout.draw(canvas);
            canvas.restore();
        }
    }

    public AnimatorSet createAdjacentPageAnimForTaskLaunch(TaskView taskView, ClipAnimationHelper clipAnimationHelper) {
        AnimatorSet animatorSet = new AnimatorSet();
        int indexOfChild = indexOfChild(taskView);
        int currentPage = getCurrentPage();
        boolean z = indexOfChild == currentPage;
        float width = clipAnimationHelper.getSourceRect().width() / clipAnimationHelper.getTargetRect().width();
        float centerY = clipAnimationHelper.getSourceRect().centerY() - clipAnimationHelper.getTargetRect().centerY();
        if (z) {
            TaskView pageAt = getPageAt(currentPage);
            int i = indexOfChild - 1;
            if (i >= 0) {
                TaskView pageAt2 = getPageAt(i);
                float[] adjacentScaleAndTranslation = getAdjacentScaleAndTranslation(pageAt, width, centerY);
                adjacentScaleAndTranslation[1] = -adjacentScaleAndTranslation[1];
                animatorSet.play(createAnimForChild(pageAt2, adjacentScaleAndTranslation));
            }
            int i2 = indexOfChild + 1;
            if (i2 < getPageCount()) {
                animatorSet.play(createAnimForChild(getPageAt(i2), getAdjacentScaleAndTranslation(pageAt, width, centerY)));
            }
        } else {
            float width2 = taskView.getWidth() * (width - taskView.getCurveScale());
            TaskView pageAt3 = getPageAt(currentPage);
            Property property = TRANSLATION_X;
            float[] fArr = new float[1];
            fArr[0] = this.mIsRtl ? -width2 : width2;
            animatorSet.play(ObjectAnimator.ofFloat(pageAt3, property, fArr));
            int i3 = currentPage + (currentPage - indexOfChild);
            if (i3 >= 0 && i3 < getPageCount()) {
                TaskView pageAt4 = getPageAt(i3);
                PropertyListBuilder propertyListBuilder = new PropertyListBuilder();
                if (this.mIsRtl) {
                    width2 = -width2;
                }
                animatorSet.play(ObjectAnimator.ofPropertyValuesHolder(pageAt4, propertyListBuilder.translationX(width2).scale(1.0f).build()));
            }
        }
        return animatorSet;
    }

    private Animator createAnimForChild(TaskView taskView, float[] fArr) {
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(ObjectAnimator.ofFloat(taskView, TaskView.ZOOM_SCALE, fArr[0]));
        animatorSet.play(ObjectAnimator.ofPropertyValuesHolder(taskView, new PropertyListBuilder().translationX(fArr[1]).translationY(fArr[2]).build()));
        return animatorSet;
    }

    public PendingAnimation createTaskLauncherAnimation(final TaskView taskView, long j) {
        if (getChildCount() == 0) {
            return new PendingAnimation(new AnimatorSet());
        }
        taskView.setVisibility(4);
        final int sysUiStatusNavFlags = taskView.getThumbnail().getSysUiStatusNavFlags();
        final TaskViewDrawable taskViewDrawable = new TaskViewDrawable(taskView, this);
        getOverlay().add(taskViewDrawable);
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(taskViewDrawable, TaskViewDrawable.PROGRESS, 1.0f, 0.0f);
        ofFloat.setInterpolator(Interpolators.LINEAR);
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.quickstep.views.-$$Lambda$RecentsView$6rY-CbVfaW6Tllsto9JMEtZpvcc
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                RecentsView.lambda$createTaskLauncherAnimation$7(RecentsView.this, sysUiStatusNavFlags, valueAnimator);
            }
        });
        AnimatorSet createAdjacentPageAnimForTaskLaunch = createAdjacentPageAnimForTaskLaunch(taskView, taskViewDrawable.getClipAnimationHelper());
        createAdjacentPageAnimForTaskLaunch.play(ofFloat);
        createAdjacentPageAnimForTaskLaunch.setDuration(j);
        final Consumer consumer = new Consumer() { // from class: com.android.quickstep.views.-$$Lambda$RecentsView$Wo_ajyBoMmxYcPL1Ygm-K_4O_JM
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                RecentsView.lambda$createTaskLauncherAnimation$8(RecentsView.this, taskView, taskViewDrawable, (Boolean) obj);
            }
        };
        this.mPendingAnimation = new PendingAnimation(createAdjacentPageAnimForTaskLaunch);
        this.mPendingAnimation.addEndListener(new Consumer() { // from class: com.android.quickstep.views.-$$Lambda$RecentsView$K9aSmLDhytYuCKnL74rPfPFTYQ8
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                RecentsView.lambda$createTaskLauncherAnimation$10(RecentsView.this, consumer, taskView, (PendingAnimation.OnEndListener) obj);
            }
        });
        return this.mPendingAnimation;
    }

    public static /* synthetic */ void lambda$createTaskLauncherAnimation$7(RecentsView recentsView, int i, ValueAnimator valueAnimator) {
        SystemUiController systemUiController = recentsView.mActivity.getSystemUiController();
        if (valueAnimator.getAnimatedFraction() <= UPDATE_SYSUI_FLAGS_THRESHOLD) {
            i = 0;
        }
        systemUiController.updateUiState(4, i);
    }

    public static /* synthetic */ void lambda$createTaskLauncherAnimation$8(RecentsView recentsView, TaskView taskView, TaskViewDrawable taskViewDrawable, Boolean bool) {
        recentsView.onTaskLaunched(bool.booleanValue());
        taskView.setVisibility(0);
        recentsView.getOverlay().remove(taskViewDrawable);
    }

    public static /* synthetic */ void lambda$createTaskLauncherAnimation$10(RecentsView recentsView, final Consumer consumer, final TaskView taskView, PendingAnimation.OnEndListener onEndListener) {
        if (onEndListener.isSuccess) {
            taskView.launchTask(false, new Consumer() { // from class: com.android.quickstep.views.-$$Lambda$RecentsView$iVOkJBP7MRf48RCoVcMgAgn_rF8
                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    RecentsView.lambda$createTaskLauncherAnimation$9(consumer, taskView, (Boolean) obj);
                }
            }, recentsView.getHandler());
            Task task = taskView.getTask();
            if (task != null) {
                recentsView.mActivity.getUserEventDispatcher().logTaskLaunchOrDismiss(onEndListener.logAction, 2, recentsView.indexOfChild(taskView), TaskUtils.getComponentKeyForTask(task.key));
            }
        } else {
            consumer.accept(false);
        }
        recentsView.mPendingAnimation = null;
    }

    public static /* synthetic */ void lambda$createTaskLauncherAnimation$9(Consumer consumer, TaskView taskView, Boolean bool) {
        consumer.accept(bool);
        if (!bool.booleanValue()) {
            taskView.notifyTaskLaunchFailed(TAG);
        }
    }

    public void onTaskLaunched(boolean z) {
        resetTaskVisuals();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.launcher3.PagedView
    public void notifyPageSwitchListener(int i) {
        super.notifyPageSwitchListener(i);
        loadVisibleTaskData();
    }

    @Override // com.android.launcher3.PagedView
    protected String getCurrentPageDescription() {
        return "";
    }

    private int additionalScrollForClearAllButton() {
        return ((int) getResources().getDimension(R.dimen.clear_all_container_width)) - getPaddingEnd();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.launcher3.PagedView
    public int computeMaxScrollX() {
        if (getChildCount() == 0) {
            return super.computeMaxScrollX();
        }
        return super.computeMaxScrollX() + (this.mIsRtl ? 0 : additionalScrollForClearAllButton());
    }

    @Override // com.android.launcher3.PagedView
    protected int offsetForPageScrolls() {
        if (this.mIsRtl) {
            return additionalScrollForClearAllButton();
        }
        return 0;
    }

    public void setClearAllButton(View view) {
        this.mClearAllButton = view;
        updateClearAllButtonAlpha();
    }

    private void onChildViewsChanged() {
        int childCount = getChildCount();
        this.mClearAllButton.setVisibility(childCount == 0 ? 4 : 0);
        setFocusable(childCount != 0);
    }

    public void revealClearAllButton() {
        setCurrentPage(getChildCount() - 1);
        scrollTo(this.mIsRtl ? 0 : computeMaxScrollX(), 0);
    }

    @Override // com.android.launcher3.PagedView, android.view.View
    public boolean performAccessibilityAction(int i, Bundle bundle) {
        if (getChildCount() > 0) {
            if (i != 4096) {
                if (i == 8192) {
                    if (!this.mIsClearAllButtonFullyRevealed && getCurrentPage() == getPageCount() - 1) {
                        revealClearAllButton();
                        return true;
                    }
                }
            }
            if (this.mIsClearAllButtonFullyRevealed) {
                setCurrentPage(getChildCount() - 1);
                return true;
            }
        }
        return super.performAccessibilityAction(i, bundle);
    }

    @Override // android.view.ViewGroup, android.view.View
    public void addChildrenForAccessibility(ArrayList<View> arrayList) {
        arrayList.add(this.mClearAllButton);
        for (int childCount = getChildCount() - 1; childCount >= 0; childCount--) {
            arrayList.add(getChildAt(childCount));
        }
    }

    @Override // com.android.launcher3.PagedView, android.view.View
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        int i;
        super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
        if (getChildCount() > 0) {
            if (this.mIsClearAllButtonFullyRevealed) {
                i = 4096;
            } else {
                i = 8192;
            }
            accessibilityNodeInfo.addAction(i);
            accessibilityNodeInfo.setScrollable(true);
        }
        accessibilityNodeInfo.setCollectionInfo(AccessibilityNodeInfo.CollectionInfo.obtain(1, getChildCount(), false, 0));
    }

    @Override // com.android.launcher3.PagedView, android.view.View
    public void onInitializeAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        super.onInitializeAccessibilityEvent(accessibilityEvent);
        accessibilityEvent.setScrollable(getPageCount() > 0);
        if (!this.mIsClearAllButtonFullyRevealed && accessibilityEvent.getEventType() == 4096) {
            int childCount = getChildCount();
            int[] visibleChildrenRange = getVisibleChildrenRange();
            accessibilityEvent.setFromIndex((childCount - visibleChildrenRange[1]) - 1);
            accessibilityEvent.setToIndex((childCount - visibleChildrenRange[0]) - 1);
            accessibilityEvent.setItemCount(childCount);
        }
    }

    @Override // com.android.launcher3.PagedView, android.view.ViewGroup, android.view.View
    public CharSequence getAccessibilityClassName() {
        return ListView.class.getName();
    }

    @Override // com.android.launcher3.PagedView
    protected boolean isPageOrderFlipped() {
        return true;
    }

    public boolean performTaskAccessibilityActionExtra(int i) {
        return false;
    }
}
