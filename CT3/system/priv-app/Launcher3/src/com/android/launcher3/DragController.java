package com.android.launcher3;

import android.content.ComponentName;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Handler;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.inputmethod.InputMethodManager;
import com.android.launcher3.DropTarget;
import com.android.launcher3.accessibility.DragViewStateAnnouncer;
import com.android.launcher3.compat.PackageInstallerCompat;
import com.mediatek.launcher3.LauncherLog;
import java.util.ArrayList;
import java.util.HashSet;
/* loaded from: a.zip:com/android/launcher3/DragController.class */
public class DragController {
    private DropTarget.DragObject mDragObject;
    DragScroller mDragScroller;
    private boolean mDragging;
    private DropTarget mFlingToDeleteDropTarget;
    protected int mFlingToDeleteThresholdVelocity;
    private Handler mHandler;
    private InputMethodManager mInputMethodManager;
    private boolean mIsAccessibleDrag;
    private final boolean mIsRtl;
    private DropTarget mLastDropTarget;
    Launcher mLauncher;
    private int mMotionDownX;
    private int mMotionDownY;
    private View mMoveTarget;
    private View mScrollView;
    private int mScrollZone;
    private VelocityTracker mVelocityTracker;
    private IBinder mWindowToken;
    public static int DRAG_ACTION_MOVE = 0;
    public static int DRAG_ACTION_COPY = 1;
    private Rect mRectTemp = new Rect();
    private final int[] mCoordinatesTemp = new int[2];
    private ArrayList<DropTarget> mDropTargets = new ArrayList<>();
    private ArrayList<DragListener> mListeners = new ArrayList<>();
    int mScrollState = 0;
    private ScrollRunnable mScrollRunnable = new ScrollRunnable(this);
    int[] mLastTouch = new int[2];
    long mLastTouchUpTime = -1;
    int mDistanceSinceScroll = 0;
    private int[] mTmpPoint = new int[2];
    private Rect mDragLayerRect = new Rect();

    /* loaded from: a.zip:com/android/launcher3/DragController$DragListener.class */
    public interface DragListener {
        void onDragEnd();

        void onDragStart(DragSource dragSource, Object obj, int i);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/launcher3/DragController$ScrollRunnable.class */
    public class ScrollRunnable implements Runnable {
        private int mDirection;
        final DragController this$0;

        ScrollRunnable(DragController dragController) {
            this.this$0 = dragController;
        }

        @Override // java.lang.Runnable
        public void run() {
            if (this.this$0.mDragScroller != null) {
                if (this.mDirection == 0) {
                    this.this$0.mDragScroller.scrollLeft();
                } else {
                    this.this$0.mDragScroller.scrollRight();
                }
                this.this$0.mScrollState = 0;
                this.this$0.mDistanceSinceScroll = 0;
                this.this$0.mDragScroller.onExitScrollArea();
                this.this$0.mLauncher.getDragLayer().onExitScrollArea();
                if (this.this$0.isDragging()) {
                    this.this$0.checkScrollState(this.this$0.mLastTouch[0], this.this$0.mLastTouch[1]);
                }
            }
        }

        void setDirection(int i) {
            this.mDirection = i;
        }
    }

    public DragController(Launcher launcher) {
        Resources resources = launcher.getResources();
        this.mLauncher = launcher;
        this.mHandler = new Handler();
        this.mScrollZone = resources.getDimensionPixelSize(2131230798);
        this.mVelocityTracker = VelocityTracker.obtain();
        this.mFlingToDeleteThresholdVelocity = (int) (resources.getInteger(2131427330) * resources.getDisplayMetrics().density);
        this.mIsRtl = Utilities.isRtl(resources);
    }

    private void acquireVelocityTrackerAndAddMovement(MotionEvent motionEvent) {
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
        this.mVelocityTracker.addMovement(motionEvent);
    }

    private void checkTouchMove(DropTarget dropTarget) {
        if (dropTarget != null) {
            if (this.mLastDropTarget != dropTarget) {
                if (this.mLastDropTarget != null) {
                    this.mLastDropTarget.onDragExit(this.mDragObject);
                }
                dropTarget.onDragEnter(this.mDragObject);
            }
            dropTarget.onDragOver(this.mDragObject);
        } else if (this.mLastDropTarget != null) {
            this.mLastDropTarget.onDragExit(this.mDragObject);
        }
        this.mLastDropTarget = dropTarget;
    }

    private void clearScrollRunnable() {
        this.mHandler.removeCallbacks(this.mScrollRunnable);
        if (this.mScrollState == 1) {
            this.mScrollState = 0;
            this.mScrollRunnable.setDirection(1);
            this.mDragScroller.onExitScrollArea();
            this.mLauncher.getDragLayer().onExitScrollArea();
        }
    }

    private void drop(float f, float f2) {
        int[] iArr = this.mCoordinatesTemp;
        DropTarget findDropTarget = findDropTarget((int) f, (int) f2, iArr);
        this.mDragObject.x = iArr[0];
        this.mDragObject.y = iArr[1];
        if (LauncherLog.DEBUG_DRAG) {
            LauncherLog.d("Launcher.DragController", "drop: x = " + f + ", y = " + f2 + ", mDragObject.x = " + this.mDragObject.x + ", mDragObject.y = " + this.mDragObject.y + ", dropTarget = " + findDropTarget);
        }
        boolean z = false;
        if (findDropTarget != null) {
            this.mDragObject.dragComplete = true;
            findDropTarget.onDragExit(this.mDragObject);
            z = false;
            if (findDropTarget.acceptDrop(this.mDragObject)) {
                findDropTarget.onDrop(this.mDragObject);
                z = true;
            }
        }
        this.mDragObject.dragSource.onDropCompleted((View) findDropTarget, this.mDragObject, false, z);
    }

    private void dropOnFlingToDeleteTarget(float f, float f2, PointF pointF) {
        int[] iArr = this.mCoordinatesTemp;
        this.mDragObject.x = iArr[0];
        this.mDragObject.y = iArr[1];
        if (this.mLastDropTarget != null && this.mFlingToDeleteDropTarget != this.mLastDropTarget) {
            this.mLastDropTarget.onDragExit(this.mDragObject);
        }
        boolean z = false;
        this.mFlingToDeleteDropTarget.onDragEnter(this.mDragObject);
        this.mDragObject.dragComplete = true;
        this.mFlingToDeleteDropTarget.onDragExit(this.mDragObject);
        if (this.mFlingToDeleteDropTarget.acceptDrop(this.mDragObject)) {
            this.mFlingToDeleteDropTarget.onFlingToDelete(this.mDragObject, pointF);
            z = true;
        }
        this.mDragObject.dragSource.onDropCompleted((View) this.mFlingToDeleteDropTarget, this.mDragObject, true, z);
    }

    private void endDrag() {
        if (LauncherLog.DEBUG_DRAG) {
            LauncherLog.d("Launcher.DragController", "endDrag: mDragging = " + this.mDragging + ", mDragObject = " + this.mDragObject);
        }
        if (this.mDragging) {
            this.mDragging = false;
            this.mIsAccessibleDrag = false;
            clearScrollRunnable();
            boolean z = false;
            if (this.mDragObject.dragView != null) {
                z = this.mDragObject.deferDragViewCleanupPostAnimation;
                if (!z) {
                    this.mDragObject.dragView.remove();
                }
                this.mDragObject.dragView = null;
            }
            if (!z) {
                for (DragListener dragListener : new ArrayList(this.mListeners)) {
                    dragListener.onDragEnd();
                }
            }
        }
        releaseVelocityTracker();
    }

    private DropTarget findDropTarget(int i, int i2, int[] iArr) {
        Rect rect = this.mRectTemp;
        ArrayList<DropTarget> arrayList = this.mDropTargets;
        for (int size = arrayList.size() - 1; size >= 0; size--) {
            DropTarget dropTarget = arrayList.get(size);
            if (dropTarget.isDropEnabled()) {
                dropTarget.getHitRectRelativeToDragLayer(rect);
                this.mDragObject.x = i;
                this.mDragObject.y = i2;
                if (rect.contains(i, i2)) {
                    iArr[0] = i;
                    iArr[1] = i2;
                    this.mLauncher.getDragLayer().mapCoordInSelfToDescendent((View) dropTarget, iArr);
                    return dropTarget;
                }
            }
        }
        return null;
    }

    private int[] getClampedDragLayerPos(float f, float f2) {
        this.mLauncher.getDragLayer().getLocalVisibleRect(this.mDragLayerRect);
        this.mTmpPoint[0] = (int) Math.max(this.mDragLayerRect.left, Math.min(f, this.mDragLayerRect.right - 1));
        this.mTmpPoint[1] = (int) Math.max(this.mDragLayerRect.top, Math.min(f2, this.mDragLayerRect.bottom - 1));
        return this.mTmpPoint;
    }

    private void handleMoveEvent(int i, int i2) {
        this.mDragObject.dragView.move(i, i2);
        int[] iArr = this.mCoordinatesTemp;
        DropTarget findDropTarget = findDropTarget(i, i2, iArr);
        this.mDragObject.x = iArr[0];
        this.mDragObject.y = iArr[1];
        if (LauncherLog.DEBUG_DRAG) {
            LauncherLog.d("Launcher.DragController", "handleMoveEvent: x = " + i + ", y = " + i2 + ", dragView = " + this.mDragObject.dragView + ", dragX = " + this.mDragObject.x + ", dragY = " + this.mDragObject.y);
        }
        checkTouchMove(findDropTarget);
        this.mDistanceSinceScroll = (int) (this.mDistanceSinceScroll + Math.hypot(this.mLastTouch[0] - i, this.mLastTouch[1] - i2));
        this.mLastTouch[0] = i;
        this.mLastTouch[1] = i2;
        checkScrollState(i, i2);
    }

    private PointF isFlingingToDelete(DragSource dragSource) {
        if (this.mFlingToDeleteDropTarget != null && dragSource.supportsFlingToDelete()) {
            this.mVelocityTracker.computeCurrentVelocity(1000, ViewConfiguration.get(this.mLauncher).getScaledMaximumFlingVelocity());
            if (this.mVelocityTracker.getYVelocity() < this.mFlingToDeleteThresholdVelocity) {
                PointF pointF = new PointF(this.mVelocityTracker.getXVelocity(), this.mVelocityTracker.getYVelocity());
                PointF pointF2 = new PointF(0.0f, -1.0f);
                if (((float) Math.acos(((pointF.x * pointF2.x) + (pointF.y * pointF2.y)) / (pointF.length() * pointF2.length()))) <= Math.toRadians(35.0d)) {
                    return pointF;
                }
                return null;
            }
            return null;
        }
        return null;
    }

    private void releaseVelocityTracker() {
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.recycle();
            this.mVelocityTracker = null;
        }
    }

    public void addDragListener(DragListener dragListener) {
        this.mListeners.add(dragListener);
    }

    public void addDropTarget(DropTarget dropTarget) {
        this.mDropTargets.add(dropTarget);
    }

    public void cancelDrag() {
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher.DragController", "cancelDrag: mDragging = " + this.mDragging + ", mLastDropTarget = " + this.mLastDropTarget);
        }
        if (this.mDragging) {
            if (this.mLastDropTarget != null) {
                this.mLastDropTarget.onDragExit(this.mDragObject);
            }
            this.mDragObject.deferDragViewCleanupPostAnimation = false;
            this.mDragObject.cancelled = true;
            this.mDragObject.dragComplete = true;
            this.mDragObject.dragSource.onDropCompleted(null, this.mDragObject, false, false);
        }
        endDrag();
    }

    void checkScrollState(int i, int i2) {
        int i3 = this.mDistanceSinceScroll < ViewConfiguration.get(this.mLauncher).getScaledWindowTouchSlop() ? 900 : 500;
        DragLayer dragLayer = this.mLauncher.getDragLayer();
        int i4 = this.mIsRtl ? 1 : 0;
        int i5 = this.mIsRtl ? 0 : 1;
        if (i < this.mScrollZone) {
            if (this.mScrollState == 0) {
                this.mScrollState = 1;
                if (this.mDragScroller.onEnterScrollArea(i, i2, i4)) {
                    dragLayer.onEnterScrollArea(i4);
                    this.mScrollRunnable.setDirection(i4);
                    this.mHandler.postDelayed(this.mScrollRunnable, i3);
                }
            }
        } else if (i <= this.mScrollView.getWidth() - this.mScrollZone) {
            clearScrollRunnable();
        } else if (this.mScrollState == 0) {
            this.mScrollState = 1;
            if (this.mDragScroller.onEnterScrollArea(i, i2, i5)) {
                dragLayer.onEnterScrollArea(i5);
                this.mScrollRunnable.setDirection(i5);
                this.mHandler.postDelayed(this.mScrollRunnable, i3);
            }
        }
    }

    public void completeAccessibleDrag(int[] iArr) {
        int[] iArr2 = this.mCoordinatesTemp;
        DropTarget findDropTarget = findDropTarget(iArr[0], iArr[1], iArr2);
        this.mDragObject.x = iArr2[0];
        this.mDragObject.y = iArr2[1];
        checkTouchMove(findDropTarget);
        findDropTarget.prepareAccessibilityDrop();
        drop(iArr[0], iArr[1]);
        endDrag();
    }

    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        if (LauncherLog.DEBUG_KEY) {
            LauncherLog.d("Launcher.DragController", "dispatchKeyEvent: keycode = " + keyEvent.getKeyCode() + ", action = " + keyEvent.getAction() + ", mDragging = " + this.mDragging);
        }
        return this.mDragging;
    }

    public boolean dispatchUnhandledMove(View view, int i) {
        if (LauncherLog.DEBUG_KEY) {
            LauncherLog.d("Launcher.DragController", "dispatchUnhandledMove: focused = " + view + ", direction = " + i);
        }
        return this.mMoveTarget != null ? this.mMoveTarget.dispatchUnhandledMove(view, i) : false;
    }

    public void forceTouchMove() {
        int[] iArr = this.mCoordinatesTemp;
        DropTarget findDropTarget = findDropTarget(this.mLastTouch[0], this.mLastTouch[1], iArr);
        this.mDragObject.x = iArr[0];
        this.mDragObject.y = iArr[1];
        checkTouchMove(findDropTarget);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public long getLastGestureUpTime() {
        return this.mDragging ? System.currentTimeMillis() : this.mLastTouchUpTime;
    }

    public boolean isDragging() {
        return this.mDragging;
    }

    public void onAppsRemoved(HashSet<String> hashSet, HashSet<ComponentName> hashSet2) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher.DragController", "onAppsRemoved: mDragging = " + this.mDragging + ", mDragObject = " + this.mDragObject);
        }
        if (this.mDragObject != null) {
            Object obj = this.mDragObject.dragInfo;
            if (obj instanceof ShortcutInfo) {
                ShortcutInfo shortcutInfo = (ShortcutInfo) obj;
                for (ComponentName componentName : hashSet2) {
                    if (shortcutInfo.intent != null) {
                        ComponentName component = shortcutInfo.intent.getComponent();
                        if (component != null ? !component.equals(componentName) ? hashSet.contains(component.getPackageName()) : true : false) {
                            cancelDrag();
                            return;
                        }
                    }
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void onDeferredEndDrag(DragView dragView) {
        dragView.remove();
        if (this.mDragObject.deferDragViewCleanupPostAnimation) {
            for (DragListener dragListener : new ArrayList(this.mListeners)) {
                dragListener.onDragEnd();
            }
        }
    }

    public void onDeferredEndFling(DropTarget.DragObject dragObject) {
        dragObject.dragSource.onFlingToDeleteCompleted();
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        if (this.mIsAccessibleDrag) {
            return false;
        }
        acquireVelocityTrackerAndAddMovement(motionEvent);
        int action = motionEvent.getAction();
        int[] clampedDragLayerPos = getClampedDragLayerPos(motionEvent.getX(), motionEvent.getY());
        int i = clampedDragLayerPos[0];
        int i2 = clampedDragLayerPos[1];
        if (LauncherLog.DEBUG_MOTION) {
            LauncherLog.d("Launcher.DragController", "onInterceptTouchEvent: action = " + action + ", mDragging = " + this.mDragging + ", dragLayerX = " + i + ", dragLayerY = " + i2);
        }
        switch (action) {
            case PackageInstallerCompat.STATUS_INSTALLED /* 0 */:
                this.mMotionDownX = i;
                this.mMotionDownY = i2;
                this.mLastDropTarget = null;
                break;
            case 1:
                this.mLastTouchUpTime = System.currentTimeMillis();
                if (this.mDragging) {
                    PointF isFlingingToDelete = isFlingingToDelete(this.mDragObject.dragSource);
                    if (!DeleteDropTarget.supportsDrop(this.mDragObject.dragInfo)) {
                        isFlingingToDelete = null;
                    }
                    if (isFlingingToDelete != null) {
                        dropOnFlingToDeleteTarget(i, i2, isFlingingToDelete);
                    } else {
                        drop(i, i2);
                    }
                }
                endDrag();
                break;
            case 3:
                cancelDrag();
                break;
        }
        return this.mDragging;
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (!this.mDragging || this.mIsAccessibleDrag) {
            return false;
        }
        acquireVelocityTrackerAndAddMovement(motionEvent);
        int action = motionEvent.getAction();
        int[] clampedDragLayerPos = getClampedDragLayerPos(motionEvent.getX(), motionEvent.getY());
        int i = clampedDragLayerPos[0];
        int i2 = clampedDragLayerPos[1];
        if (LauncherLog.DEBUG_MOTION) {
            LauncherLog.d("Launcher.DragController", "onTouchEvent: action = " + action + ", dragLayerX = " + i + ", dragLayerY = " + i2 + ", mMotionDownX = " + this.mMotionDownX + ", mMotionDownY = " + this.mMotionDownY + ", mScrollState = " + this.mScrollState);
        }
        switch (action) {
            case PackageInstallerCompat.STATUS_INSTALLED /* 0 */:
                this.mMotionDownX = i;
                this.mMotionDownY = i2;
                if (i < this.mScrollZone || i > this.mScrollView.getWidth() - this.mScrollZone) {
                    this.mScrollState = 1;
                    this.mHandler.postDelayed(this.mScrollRunnable, 500L);
                } else {
                    this.mScrollState = 0;
                }
                handleMoveEvent(i, i2);
                return true;
            case 1:
                handleMoveEvent(i, i2);
                this.mHandler.removeCallbacks(this.mScrollRunnable);
                if (this.mDragging) {
                    PointF isFlingingToDelete = isFlingingToDelete(this.mDragObject.dragSource);
                    if (!DeleteDropTarget.supportsDrop(this.mDragObject.dragInfo)) {
                        isFlingingToDelete = null;
                    }
                    if (isFlingingToDelete != null) {
                        dropOnFlingToDeleteTarget(i, i2, isFlingingToDelete);
                    } else {
                        drop(i, i2);
                    }
                }
                endDrag();
                return true;
            case 2:
                handleMoveEvent(i, i2);
                return true;
            case 3:
                this.mHandler.removeCallbacks(this.mScrollRunnable);
                cancelDrag();
                return true;
            default:
                return true;
        }
    }

    public void prepareAccessibleDrag(int i, int i2) {
        this.mMotionDownX = i;
        this.mMotionDownY = i2;
        this.mLastDropTarget = null;
    }

    public void removeDragListener(DragListener dragListener) {
        this.mListeners.remove(dragListener);
    }

    public void removeDropTarget(DropTarget dropTarget) {
        this.mDropTargets.remove(dropTarget);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void resetLastGestureUpTime() {
        this.mLastTouchUpTime = -1L;
    }

    public void setDragScoller(DragScroller dragScroller) {
        this.mDragScroller = dragScroller;
    }

    public void setFlingToDeleteDropTarget(DropTarget dropTarget) {
        this.mFlingToDeleteDropTarget = dropTarget;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setMoveTarget(View view) {
        this.mMoveTarget = view;
    }

    public void setScrollView(View view) {
        this.mScrollView = view;
    }

    public void setWindowToken(IBinder iBinder) {
        this.mWindowToken = iBinder;
    }

    public DragView startDrag(Bitmap bitmap, int i, int i2, DragSource dragSource, Object obj, int i3, Point point, Rect rect, float f, boolean z) {
        if (this.mInputMethodManager == null) {
            this.mInputMethodManager = (InputMethodManager) this.mLauncher.getSystemService("input_method");
        }
        this.mInputMethodManager.hideSoftInputFromWindow(this.mWindowToken, 0);
        for (DragListener dragListener : this.mListeners) {
            dragListener.onDragStart(dragSource, obj, i3);
        }
        int i4 = this.mMotionDownX - i;
        int i5 = this.mMotionDownY - i2;
        if (LauncherLog.DEBUG_DRAG) {
            LauncherLog.d("Launcher.DragController", "startDrag: dragLayerX = " + i + ", dragLayerY = " + i2 + ", dragInfo = " + obj + ", registrationX = " + i4 + ", registrationY = " + i5 + ", dragRegion = " + rect);
        }
        int i6 = rect == null ? 0 : rect.left;
        int i7 = rect == null ? 0 : rect.top;
        this.mDragging = true;
        this.mIsAccessibleDrag = z;
        this.mDragObject = new DropTarget.DragObject();
        DragView dragView = new DragView(this.mLauncher, bitmap, i4, i5, 0, 0, bitmap.getWidth(), bitmap.getHeight(), f);
        this.mDragObject.dragView = dragView;
        this.mDragObject.dragComplete = false;
        if (this.mIsAccessibleDrag) {
            this.mDragObject.xOffset = bitmap.getWidth() / 2;
            this.mDragObject.yOffset = bitmap.getHeight() / 2;
            this.mDragObject.accessibleDrag = true;
        } else {
            this.mDragObject.xOffset = this.mMotionDownX - (i + i6);
            this.mDragObject.yOffset = this.mMotionDownY - (i2 + i7);
            this.mDragObject.stateAnnouncer = DragViewStateAnnouncer.createFor(dragView);
        }
        this.mDragObject.dragSource = dragSource;
        this.mDragObject.dragInfo = obj;
        if (point != null) {
            dragView.setDragVisualizeOffset(new Point(point));
        }
        if (rect != null) {
            dragView.setDragRegion(new Rect(rect));
        }
        this.mLauncher.getDragLayer().performHapticFeedback(0);
        dragView.show(this.mMotionDownX, this.mMotionDownY);
        handleMoveEvent(this.mMotionDownX, this.mMotionDownY);
        return dragView;
    }

    public void startDrag(View view, Bitmap bitmap, DragSource dragSource, Object obj, Rect rect, int i, float f) {
        int[] iArr = this.mCoordinatesTemp;
        this.mLauncher.getDragLayer().getLocationInDragLayer(view, iArr);
        startDrag(bitmap, iArr[0] + rect.left + ((int) (((bitmap.getWidth() * f) - bitmap.getWidth()) / 2.0f)), iArr[1] + rect.top + ((int) (((bitmap.getHeight() * f) - bitmap.getHeight()) / 2.0f)), dragSource, obj, i, null, null, f, false);
        if (i == DRAG_ACTION_MOVE) {
            view.setVisibility(8);
        }
    }
}
