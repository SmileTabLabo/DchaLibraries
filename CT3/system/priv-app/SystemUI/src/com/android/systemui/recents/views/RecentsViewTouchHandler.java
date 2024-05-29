package com.android.systemui.recents.views;

import android.app.ActivityManager;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewDebug;
import com.android.internal.policy.DividerSnapAlgorithm;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.RecentsConfiguration;
import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.events.activity.ConfigurationChangedEvent;
import com.android.systemui.recents.events.ui.HideIncompatibleAppOverlayEvent;
import com.android.systemui.recents.events.ui.ShowIncompatibleAppOverlayEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragDropTargetChangedEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragEndEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragStartEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragStartInitializeDropTargetsEvent;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.model.TaskStack;
import java.util.ArrayList;
import java.util.Iterator;
/* loaded from: a.zip:com/android/systemui/recents/views/RecentsViewTouchHandler.class */
public class RecentsViewTouchHandler {
    private DividerSnapAlgorithm mDividerSnapAlgorithm;
    @ViewDebug.ExportedProperty(category = "recents")
    private boolean mDragRequested;
    private float mDragSlop;
    @ViewDebug.ExportedProperty(deepExport = true, prefix = "drag_task")
    private Task mDragTask;
    @ViewDebug.ExportedProperty(category = "recents")
    private boolean mIsDragging;
    private DropTarget mLastDropTarget;
    private RecentsView mRv;
    @ViewDebug.ExportedProperty(deepExport = true, prefix = "drag_task_view_")
    private TaskView mTaskView;
    @ViewDebug.ExportedProperty(category = "recents")
    private Point mTaskViewOffset = new Point();
    @ViewDebug.ExportedProperty(category = "recents")
    private Point mDownPos = new Point();
    private ArrayList<DropTarget> mDropTargets = new ArrayList<>();
    private ArrayList<TaskStack.DockState> mVisibleDockStates = new ArrayList<>();

    public RecentsViewTouchHandler(RecentsView recentsView) {
        this.mRv = recentsView;
        this.mDragSlop = ViewConfiguration.get(recentsView.getContext()).getScaledTouchSlop();
        updateSnapAlgorithm();
    }

    private void handleTouchEvent(MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        switch (actionMasked) {
            case 0:
                this.mDownPos.set((int) motionEvent.getX(), (int) motionEvent.getY());
                return;
            case 1:
            case 3:
                if (this.mDragRequested) {
                    boolean z = actionMasked == 3;
                    if (z) {
                        EventBus.getDefault().send(new DragDropTargetChangedEvent(this.mDragTask, null));
                    }
                    EventBus.getDefault().send(new DragEndEvent(this.mDragTask, this.mTaskView, !z ? this.mLastDropTarget : null));
                    return;
                }
                return;
            case 2:
                float x = motionEvent.getX();
                float y = motionEvent.getY();
                float f = this.mTaskViewOffset.x;
                float f2 = this.mTaskViewOffset.y;
                if (this.mDragRequested) {
                    if (!this.mIsDragging) {
                        this.mIsDragging = Math.hypot((double) (x - ((float) this.mDownPos.x)), (double) (y - ((float) this.mDownPos.y))) > ((double) this.mDragSlop);
                    }
                    if (this.mIsDragging) {
                        int measuredWidth = this.mRv.getMeasuredWidth();
                        int measuredHeight = this.mRv.getMeasuredHeight();
                        DropTarget dropTarget = null;
                        if (this.mLastDropTarget != null) {
                            dropTarget = null;
                            if (this.mLastDropTarget.acceptsDrop((int) x, (int) y, measuredWidth, measuredHeight, true)) {
                                dropTarget = this.mLastDropTarget;
                            }
                        }
                        DropTarget dropTarget2 = dropTarget;
                        if (dropTarget == null) {
                            Iterator<T> it = this.mDropTargets.iterator();
                            do {
                                dropTarget2 = dropTarget;
                                if (it.hasNext()) {
                                    dropTarget2 = (DropTarget) it.next();
                                }
                            } while (!dropTarget2.acceptsDrop((int) x, (int) y, measuredWidth, measuredHeight, false));
                        }
                        if (this.mLastDropTarget != dropTarget2) {
                            this.mLastDropTarget = dropTarget2;
                            EventBus.getDefault().send(new DragDropTargetChangedEvent(this.mDragTask, dropTarget2));
                        }
                    }
                    this.mTaskView.setTranslationX(x - f);
                    this.mTaskView.setTranslationY(y - f2);
                    return;
                }
                return;
            default:
                return;
        }
    }

    private void updateSnapAlgorithm() {
        Rect rect = new Rect();
        SystemServicesProxy.getInstance(this.mRv.getContext()).getStableInsets(rect);
        this.mDividerSnapAlgorithm = DividerSnapAlgorithm.create(this.mRv.getContext(), rect);
    }

    public TaskStack.DockState[] getDockStatesForCurrentOrientation() {
        boolean z = this.mRv.getResources().getConfiguration().orientation == 2;
        RecentsConfiguration configuration = Recents.getConfiguration();
        return z ? configuration.isLargeScreen ? DockRegion.TABLET_LANDSCAPE : DockRegion.PHONE_LANDSCAPE : configuration.isLargeScreen ? DockRegion.TABLET_PORTRAIT : DockRegion.PHONE_PORTRAIT;
    }

    public ArrayList<TaskStack.DockState> getVisibleDockStates() {
        return this.mVisibleDockStates;
    }

    public final void onBusEvent(ConfigurationChangedEvent configurationChangedEvent) {
        if (configurationChangedEvent.fromDisplayDensityChange || configurationChangedEvent.fromDeviceOrientationChange) {
            updateSnapAlgorithm();
        }
    }

    public final void onBusEvent(DragEndEvent dragEndEvent) {
        if (!this.mDragTask.isDockable) {
            EventBus.getDefault().send(new HideIncompatibleAppOverlayEvent());
        }
        this.mDragRequested = false;
        this.mDragTask = null;
        this.mTaskView = null;
        this.mLastDropTarget = null;
    }

    public final void onBusEvent(DragStartEvent dragStartEvent) {
        TaskStack.DockState[] dockStatesForCurrentOrientation;
        SystemServicesProxy systemServices = Recents.getSystemServices();
        this.mRv.getParent().requestDisallowInterceptTouchEvent(true);
        this.mDragRequested = true;
        this.mIsDragging = false;
        this.mDragTask = dragStartEvent.task;
        this.mTaskView = dragStartEvent.taskView;
        this.mDropTargets.clear();
        int[] iArr = new int[2];
        this.mRv.getLocationInWindow(iArr);
        this.mTaskViewOffset.set((this.mTaskView.getLeft() - iArr[0]) + dragStartEvent.tlOffset.x, (this.mTaskView.getTop() - iArr[1]) + dragStartEvent.tlOffset.y);
        float f = this.mDownPos.x - this.mTaskViewOffset.x;
        float f2 = this.mDownPos.y - this.mTaskViewOffset.y;
        this.mTaskView.setTranslationX(f);
        this.mTaskView.setTranslationY(f2);
        this.mVisibleDockStates.clear();
        if (ActivityManager.supportsMultiWindow() && !systemServices.hasDockedTask() && this.mDividerSnapAlgorithm.isSplitScreenFeasible()) {
            Recents.logDockAttempt(this.mRv.getContext(), dragStartEvent.task.getTopComponent(), dragStartEvent.task.resizeMode);
            if (dragStartEvent.task.isDockable) {
                for (TaskStack.DockState dockState : getDockStatesForCurrentOrientation()) {
                    registerDropTargetForCurrentDrag(dockState);
                    dockState.update(this.mRv.getContext());
                    this.mVisibleDockStates.add(dockState);
                }
            } else {
                EventBus.getDefault().send(new ShowIncompatibleAppOverlayEvent());
            }
        }
        EventBus.getDefault().send(new DragStartInitializeDropTargetsEvent(dragStartEvent.task, dragStartEvent.taskView, this));
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        handleTouchEvent(motionEvent);
        return this.mDragRequested;
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        handleTouchEvent(motionEvent);
        return this.mDragRequested;
    }

    public void registerDropTargetForCurrentDrag(DropTarget dropTarget) {
        this.mDropTargets.add(dropTarget);
    }
}
