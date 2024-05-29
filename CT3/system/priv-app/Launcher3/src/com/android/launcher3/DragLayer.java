package com.android.launcher3;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.android.launcher3.CellLayout;
import com.android.launcher3.InsettableFrameLayout;
import com.android.launcher3.accessibility.LauncherAccessibilityDelegate;
import com.android.launcher3.compat.PackageInstallerCompat;
import com.mediatek.launcher3.LauncherLog;
import java.util.ArrayList;
/* loaded from: a.zip:com/android/launcher3/DragLayer.class */
public class DragLayer extends InsettableFrameLayout {
    View mAnchorView;
    int mAnchorViewInitialScrollX;
    private float mBackgroundAlpha;
    private int mChildCountOnLastUpdate;
    private final TimeInterpolator mCubicEaseOutInterpolator;
    private AppWidgetResizeFrame mCurrentResizeFrame;
    DragController mDragController;
    private ValueAnimator mDropAnim;
    DragView mDropView;
    private final Rect mHitRect;
    private boolean mHoverPointClosesFolder;
    private boolean mInScrollArea;
    private final boolean mIsRtl;
    private Launcher mLauncher;
    private Drawable mLeftHoverDrawable;
    private Drawable mLeftHoverDrawableActive;
    private View mOverlayView;
    private final ArrayList<AppWidgetResizeFrame> mResizeFrames;
    private Drawable mRightHoverDrawable;
    private Drawable mRightHoverDrawableActive;
    private final Rect mScrollChildPosition;
    private boolean mShowPageHints;
    private final int[] mTmpXY;
    private int mTopViewIndex;
    private TouchCompleteListener mTouchCompleteListener;
    private int mXDown;
    private int mYDown;

    /* loaded from: a.zip:com/android/launcher3/DragLayer$LayoutParams.class */
    public static class LayoutParams extends InsettableFrameLayout.LayoutParams {
        public boolean customPosition;
        public int x;
        public int y;

        public LayoutParams(int i, int i2) {
            super(i, i2);
            this.customPosition = false;
        }

        public LayoutParams(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
            this.customPosition = false;
        }

        public LayoutParams(ViewGroup.LayoutParams layoutParams) {
            super(layoutParams);
            this.customPosition = false;
        }

        public int getHeight() {
            return this.height;
        }

        public int getWidth() {
            return this.width;
        }

        public int getX() {
            return this.x;
        }

        public int getY() {
            return this.y;
        }

        public void setHeight(int i) {
            this.height = i;
        }

        public void setWidth(int i) {
            this.width = i;
        }

        public void setX(int i) {
            this.x = i;
        }

        public void setY(int i) {
            this.y = i;
        }
    }

    /* loaded from: a.zip:com/android/launcher3/DragLayer$TouchCompleteListener.class */
    public interface TouchCompleteListener {
        void onTouchComplete();
    }

    public DragLayer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mTmpXY = new int[2];
        this.mResizeFrames = new ArrayList<>();
        this.mDropAnim = null;
        this.mCubicEaseOutInterpolator = new DecelerateInterpolator(1.5f);
        this.mDropView = null;
        this.mAnchorViewInitialScrollX = 0;
        this.mAnchorView = null;
        this.mHoverPointClosesFolder = false;
        this.mHitRect = new Rect();
        this.mChildCountOnLastUpdate = -1;
        this.mBackgroundAlpha = 0.0f;
        this.mScrollChildPosition = new Rect();
        setMotionEventSplittingEnabled(false);
        setChildrenDrawingOrderEnabled(true);
        Resources resources = getResources();
        this.mLeftHoverDrawable = resources.getDrawable(2130837541);
        this.mRightHoverDrawable = resources.getDrawable(2130837543);
        this.mLeftHoverDrawableActive = resources.getDrawable(2130837542);
        this.mRightHoverDrawableActive = resources.getDrawable(2130837544);
        this.mIsRtl = Utilities.isRtl(resources);
    }

    private void drawPageHints(Canvas canvas) {
        if (this.mShowPageHints) {
            Workspace workspace = this.mLauncher.getWorkspace();
            int measuredWidth = getMeasuredWidth();
            int nextPage = workspace.getNextPage();
            CellLayout cellLayout = (CellLayout) workspace.getChildAt(this.mIsRtl ? nextPage + 1 : nextPage - 1);
            CellLayout cellLayout2 = (CellLayout) workspace.getChildAt(this.mIsRtl ? nextPage - 1 : nextPage + 1);
            if (cellLayout != null && cellLayout.isDragTarget()) {
                Drawable drawable = (this.mInScrollArea && cellLayout.getIsDragOverlapping()) ? this.mLeftHoverDrawableActive : this.mLeftHoverDrawable;
                drawable.setBounds(0, this.mScrollChildPosition.top, drawable.getIntrinsicWidth(), this.mScrollChildPosition.bottom);
                drawable.draw(canvas);
            }
            if (cellLayout2 == null || !cellLayout2.isDragTarget()) {
                return;
            }
            Drawable drawable2 = (this.mInScrollArea && cellLayout2.getIsDragOverlapping()) ? this.mRightHoverDrawableActive : this.mRightHoverDrawable;
            drawable2.setBounds(measuredWidth - drawable2.getIntrinsicWidth(), this.mScrollChildPosition.top, measuredWidth, this.mScrollChildPosition.bottom);
            drawable2.draw(canvas);
        }
    }

    private boolean handleTouchDown(MotionEvent motionEvent, boolean z) {
        Rect rect = new Rect();
        int x = (int) motionEvent.getX();
        int y = (int) motionEvent.getY();
        if (LauncherLog.DEBUG_MOTION) {
            LauncherLog.d("DragLayer", "handleTouchDown: x = " + x + ", y = " + y + ", intercept = " + z + ", mXDown = " + this.mXDown + ", mYDown = " + this.mYDown);
        }
        for (AppWidgetResizeFrame appWidgetResizeFrame : this.mResizeFrames) {
            appWidgetResizeFrame.getHitRect(rect);
            if (rect.contains(x, y) && appWidgetResizeFrame.beginResizeIfPointInRegion(x - appWidgetResizeFrame.getLeft(), y - appWidgetResizeFrame.getTop())) {
                this.mCurrentResizeFrame = appWidgetResizeFrame;
                this.mXDown = x;
                this.mYDown = y;
                requestDisallowInterceptTouchEvent(true);
                return true;
            }
        }
        Folder openFolder = this.mLauncher.getWorkspace().getOpenFolder();
        if (openFolder == null || !z) {
            return false;
        }
        if (openFolder.isEditingName() && !isEventOverFolderTextRegion(openFolder, motionEvent)) {
            openFolder.dismissEditingName();
            return true;
        } else if (isEventOverFolder(openFolder, motionEvent)) {
            return false;
        } else {
            if (isInAccessibleDrag()) {
                return !isEventOverDropTargetBar(motionEvent);
            }
            this.mLauncher.closeFolder();
            return true;
        }
    }

    private boolean isEventOverDropTargetBar(MotionEvent motionEvent) {
        getDescendantRectRelativeToSelf(this.mLauncher.getSearchDropTargetBar(), this.mHitRect);
        return this.mHitRect.contains((int) motionEvent.getX(), (int) motionEvent.getY());
    }

    private boolean isEventOverFolder(Folder folder, MotionEvent motionEvent) {
        getDescendantRectRelativeToSelf(folder, this.mHitRect);
        return this.mHitRect.contains((int) motionEvent.getX(), (int) motionEvent.getY());
    }

    private boolean isEventOverFolderTextRegion(Folder folder, MotionEvent motionEvent) {
        getDescendantRectRelativeToSelf(folder.getEditTextRegion(), this.mHitRect);
        return this.mHitRect.contains((int) motionEvent.getX(), (int) motionEvent.getY());
    }

    private boolean isInAccessibleDrag() {
        LauncherAccessibilityDelegate accessibilityDelegate = LauncherAppState.getInstance().getAccessibilityDelegate();
        return accessibilityDelegate != null ? accessibilityDelegate.isInAccessibleDrag() : false;
    }

    private void sendTapOutsideFolderAccessibilityEvent(boolean z) {
        AccessibilityManager accessibilityManager = (AccessibilityManager) getContext().getSystemService("accessibility");
        if (accessibilityManager.isEnabled()) {
            int i = z ? 2131558449 : 2131558448;
            AccessibilityEvent obtain = AccessibilityEvent.obtain(8);
            onInitializeAccessibilityEvent(obtain);
            obtain.getText().add(getContext().getString(i));
            accessibilityManager.sendAccessibilityEvent(obtain);
        }
    }

    private void updateChildIndices() {
        this.mTopViewIndex = -1;
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (getChildAt(i) instanceof DragView) {
                this.mTopViewIndex = i;
            }
        }
        this.mChildCountOnLastUpdate = childCount;
    }

    @Override // android.view.ViewGroup, android.view.View
    public void addChildrenForAccessibility(ArrayList<View> arrayList) {
        Folder openFolder = this.mLauncher.getWorkspace().getOpenFolder();
        if (openFolder == null) {
            super.addChildrenForAccessibility(arrayList);
            return;
        }
        arrayList.add(openFolder);
        if (isInAccessibleDrag()) {
            arrayList.add(this.mLauncher.getSearchDropTargetBar());
        }
    }

    public void addResizeFrame(ItemInfo itemInfo, LauncherAppWidgetHostView launcherAppWidgetHostView, CellLayout cellLayout) {
        AppWidgetResizeFrame appWidgetResizeFrame = new AppWidgetResizeFrame(getContext(), launcherAppWidgetHostView, cellLayout, this);
        if (LauncherLog.DEBUG) {
            LauncherLog.d("DragLayer", "addResizeFrame: itemInfo = " + itemInfo + ", widget = " + launcherAppWidgetHostView + ", resizeFrame = " + appWidgetResizeFrame);
        }
        LayoutParams layoutParams = new LayoutParams(-1, -1);
        layoutParams.customPosition = true;
        addView(appWidgetResizeFrame, layoutParams);
        this.mResizeFrames.add(appWidgetResizeFrame);
        appWidgetResizeFrame.snapToWidget(false);
    }

    public void animateView(DragView dragView, ValueAnimator.AnimatorUpdateListener animatorUpdateListener, int i, TimeInterpolator timeInterpolator, Runnable runnable, int i2, View view) {
        if (this.mDropAnim != null) {
            this.mDropAnim.cancel();
        }
        this.mDropView = dragView;
        this.mDropView.cancelAnimation();
        this.mDropView.resetLayoutParams();
        if (view != null) {
            this.mAnchorViewInitialScrollX = view.getScrollX();
        }
        this.mAnchorView = view;
        this.mDropAnim = new ValueAnimator();
        this.mDropAnim.setInterpolator(timeInterpolator);
        this.mDropAnim.setDuration(i);
        this.mDropAnim.setFloatValues(0.0f, 1.0f);
        this.mDropAnim.addUpdateListener(animatorUpdateListener);
        this.mDropAnim.addListener(new AnimatorListenerAdapter(this, runnable, i2) { // from class: com.android.launcher3.DragLayer.3
            final DragLayer this$0;
            final int val$animationEndStyle;
            final Runnable val$onCompleteRunnable;

            {
                this.this$0 = this;
                this.val$onCompleteRunnable = runnable;
                this.val$animationEndStyle = i2;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                if (this.val$onCompleteRunnable != null) {
                    this.val$onCompleteRunnable.run();
                }
                switch (this.val$animationEndStyle) {
                    case PackageInstallerCompat.STATUS_INSTALLED /* 0 */:
                        this.this$0.clearAnimatedView();
                        return;
                    case 1:
                    case 2:
                    default:
                        return;
                }
            }
        });
        this.mDropAnim.start();
    }

    public void animateView(DragView dragView, Rect rect, Rect rect2, float f, float f2, float f3, float f4, float f5, int i, Interpolator interpolator, Interpolator interpolator2, Runnable runnable, int i2, View view) {
        float hypot = (float) Math.hypot(rect2.left - rect.left, rect2.top - rect.top);
        Resources resources = getResources();
        float integer = resources.getInteger(2131427348);
        int i3 = i;
        if (i < 0) {
            int integer2 = resources.getInteger(2131427344);
            int i4 = integer2;
            if (hypot < integer) {
                i4 = (int) (integer2 * this.mCubicEaseOutInterpolator.getInterpolation(hypot / integer));
            }
            i3 = Math.max(i4, resources.getInteger(2131427343));
        }
        TimeInterpolator timeInterpolator = null;
        if (interpolator2 == null || interpolator == null) {
            timeInterpolator = this.mCubicEaseOutInterpolator;
        }
        animateView(dragView, new ValueAnimator.AnimatorUpdateListener(this, dragView, interpolator2, interpolator, f2, dragView.getScaleX(), f3, f4, f5, f, dragView.getAlpha(), rect, rect2) { // from class: com.android.launcher3.DragLayer.2
            final DragLayer this$0;
            final Interpolator val$alphaInterpolator;
            final float val$dropViewScale;
            final float val$finalAlpha;
            final float val$finalScaleX;
            final float val$finalScaleY;
            final Rect val$from;
            final float val$initAlpha;
            final float val$initScaleX;
            final float val$initScaleY;
            final Interpolator val$motionInterpolator;
            final Rect val$to;
            final DragView val$view;

            {
                this.this$0 = this;
                this.val$view = dragView;
                this.val$alphaInterpolator = interpolator2;
                this.val$motionInterpolator = interpolator;
                this.val$initScaleX = f2;
                this.val$dropViewScale = r9;
                this.val$initScaleY = f3;
                this.val$finalScaleX = f4;
                this.val$finalScaleY = f5;
                this.val$finalAlpha = f;
                this.val$initAlpha = r14;
                this.val$from = rect;
                this.val$to = rect2;
            }

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                int measuredWidth = this.val$view.getMeasuredWidth();
                int measuredHeight = this.val$view.getMeasuredHeight();
                float interpolation = this.val$alphaInterpolator == null ? floatValue : this.val$alphaInterpolator.getInterpolation(floatValue);
                float interpolation2 = this.val$motionInterpolator == null ? floatValue : this.val$motionInterpolator.getInterpolation(floatValue);
                float f6 = this.val$initScaleX * this.val$dropViewScale;
                float f7 = this.val$initScaleY * this.val$dropViewScale;
                float f8 = this.val$finalScaleX;
                float f9 = this.val$finalScaleY;
                float f10 = this.val$finalAlpha;
                float f11 = this.val$initAlpha;
                float f12 = this.val$from.left + (((f6 - 1.0f) * measuredWidth) / 2.0f);
                float f13 = this.val$from.top + (((f7 - 1.0f) * measuredHeight) / 2.0f);
                int round = (int) (Math.round((this.val$to.left - f12) * interpolation2) + f12);
                int round2 = (int) (Math.round((this.val$to.top - f13) * interpolation2) + f13);
                int scaleX = this.this$0.mAnchorView == null ? 0 : (int) (this.this$0.mAnchorView.getScaleX() * (this.this$0.mAnchorViewInitialScrollX - this.this$0.mAnchorView.getScrollX()));
                int scrollX = this.this$0.mDropView.getScrollX();
                int scrollY = this.this$0.mDropView.getScrollY();
                this.this$0.mDropView.setTranslationX((round - scrollX) + scaleX);
                this.this$0.mDropView.setTranslationY(round2 - scrollY);
                this.this$0.mDropView.setScaleX((f8 * floatValue) + ((1.0f - floatValue) * f6));
                this.this$0.mDropView.setScaleY((f9 * floatValue) + ((1.0f - floatValue) * f7));
                this.this$0.mDropView.setAlpha((f10 * interpolation) + (f11 * (1.0f - interpolation)));
            }
        }, i3, timeInterpolator, runnable, i2, view);
    }

    public void animateViewIntoPosition(DragView dragView, int i, int i2, int i3, int i4, float f, float f2, float f3, float f4, float f5, Runnable runnable, int i5, int i6, View view) {
        animateView(dragView, new Rect(i, i2, dragView.getMeasuredWidth() + i, dragView.getMeasuredHeight() + i2), new Rect(i3, i4, dragView.getMeasuredWidth() + i3, dragView.getMeasuredHeight() + i4), f, f2, f3, f4, f5, i6, null, null, runnable, i5, view);
    }

    public void animateViewIntoPosition(DragView dragView, View view, int i, Runnable runnable, View view2) {
        int round;
        int round2;
        ShortcutAndWidgetContainer shortcutAndWidgetContainer = (ShortcutAndWidgetContainer) view.getParent();
        CellLayout.LayoutParams layoutParams = (CellLayout.LayoutParams) view.getLayoutParams();
        shortcutAndWidgetContainer.measureChild(view);
        Rect rect = new Rect();
        getViewRectRelativeToSelf(dragView, rect);
        if (LauncherLog.DEBUG) {
            LauncherLog.d("DragLayer", "animateViewIntoPosition: dragView = " + dragView + ", r = " + rect + ", lp.x = " + layoutParams.x + ", lp.y = " + layoutParams.y);
        }
        float scaleX = view.getScaleX();
        int[] iArr = {layoutParams.x + ((int) ((view.getMeasuredWidth() * (1.0f - scaleX)) / 2.0f)), layoutParams.y + ((int) ((view.getMeasuredHeight() * (1.0f - scaleX)) / 2.0f))};
        float descendantCoordRelativeToSelf = getDescendantCoordRelativeToSelf((View) view.getParent(), iArr) * scaleX;
        int i2 = iArr[0];
        int i3 = iArr[1];
        float f = descendantCoordRelativeToSelf;
        if (view instanceof TextView) {
            f = descendantCoordRelativeToSelf / dragView.getIntrinsicIconScaleFactor();
            int round3 = (int) ((i3 + Math.round(((TextView) view).getPaddingTop() * f)) - ((dragView.getMeasuredHeight() * (1.0f - f)) / 2.0f));
            round = round3;
            if (dragView.getDragVisualizeOffset() != null) {
                round = round3 - Math.round(dragView.getDragVisualizeOffset().y * f);
            }
            round2 = i2 - ((dragView.getMeasuredWidth() - Math.round(view.getMeasuredWidth() * descendantCoordRelativeToSelf)) / 2);
        } else if (view instanceof FolderIcon) {
            round = (int) (((int) ((i3 + Math.round((view.getPaddingTop() - dragView.getDragRegionTop()) * descendantCoordRelativeToSelf)) - ((2.0f * descendantCoordRelativeToSelf) / 2.0f))) - (((1.0f - descendantCoordRelativeToSelf) * dragView.getMeasuredHeight()) / 2.0f));
            round2 = i2 - ((dragView.getMeasuredWidth() - Math.round(view.getMeasuredWidth() * descendantCoordRelativeToSelf)) / 2);
        } else {
            round = i3 - (Math.round((dragView.getHeight() - view.getMeasuredHeight()) * descendantCoordRelativeToSelf) / 2);
            round2 = i2 - (Math.round((dragView.getMeasuredWidth() - view.getMeasuredWidth()) * descendantCoordRelativeToSelf) / 2);
        }
        int i4 = rect.left;
        int i5 = rect.top;
        view.setVisibility(4);
        animateViewIntoPosition(dragView, i4, i5, round2, round, 1.0f, 1.0f, 1.0f, f, f, new Runnable(this, view, runnable) { // from class: com.android.launcher3.DragLayer.1
            final DragLayer this$0;
            final View val$child;
            final Runnable val$onFinishAnimationRunnable;

            {
                this.this$0 = this;
                this.val$child = view;
                this.val$onFinishAnimationRunnable = runnable;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.val$child.setVisibility(0);
                if (this.val$onFinishAnimationRunnable != null) {
                    this.val$onFinishAnimationRunnable.run();
                }
            }
        }, 0, i, view2);
    }

    public void animateViewIntoPosition(DragView dragView, View view, Runnable runnable, View view2) {
        animateViewIntoPosition(dragView, view, -1, runnable, view2);
    }

    public void animateViewIntoPosition(DragView dragView, int[] iArr, float f, float f2, float f3, int i, Runnable runnable, int i2) {
        Rect rect = new Rect();
        getViewRectRelativeToSelf(dragView, rect);
        animateViewIntoPosition(dragView, rect.left, rect.top, iArr[0], iArr[1], f, 1.0f, 1.0f, f2, f3, runnable, i, i2, null);
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public void bringChildToFront(View view) {
        super.bringChildToFront(view);
        if (view != this.mOverlayView && this.mOverlayView != null) {
            this.mOverlayView.bringToFront();
        }
        updateChildIndices();
    }

    @Override // com.android.launcher3.InsettableFrameLayout, android.widget.FrameLayout, android.view.ViewGroup
    protected boolean checkLayoutParams(ViewGroup.LayoutParams layoutParams) {
        return layoutParams instanceof LayoutParams;
    }

    public void clearAllResizeFrames() {
        if (this.mResizeFrames.size() > 0) {
            if (LauncherLog.DEBUG_DRAG) {
                LauncherLog.d("DragLayer", "clearAllResizeFrames: mResizeFrames size = " + this.mResizeFrames.size());
            }
            for (AppWidgetResizeFrame appWidgetResizeFrame : this.mResizeFrames) {
                appWidgetResizeFrame.commitResize();
                removeView(appWidgetResizeFrame);
            }
            this.mResizeFrames.clear();
        }
    }

    public void clearAnimatedView() {
        if (this.mDropAnim != null) {
            this.mDropAnim.cancel();
        }
        if (this.mDropView != null) {
            this.mDragController.onDeferredEndDrag(this.mDropView);
        }
        this.mDropView = null;
        invalidate();
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void dispatchDraw(Canvas canvas) {
        if (this.mBackgroundAlpha > 0.0f) {
            canvas.drawColor((((int) (this.mBackgroundAlpha * 255.0f)) << 24) | 0);
        }
        super.dispatchDraw(canvas);
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        boolean dispatchKeyEvent = !this.mDragController.dispatchKeyEvent(keyEvent) ? super.dispatchKeyEvent(keyEvent) : true;
        if (LauncherLog.DEBUG_KEY) {
            LauncherLog.d("DragLayer", "dispatchKeyEvent: keycode = " + keyEvent.getKeyCode() + ", action = " + keyEvent.getAction() + ", handled = " + dispatchKeyEvent);
        }
        return dispatchKeyEvent;
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchUnhandledMove(View view, int i) {
        if (LauncherLog.DEBUG_KEY) {
            LauncherLog.d("DragLayer", "dispatchUnhandledMove: focused = " + view + ", direction = " + i);
        }
        return this.mDragController.dispatchUnhandledMove(view, i);
    }

    @Override // android.view.ViewGroup
    protected boolean drawChild(Canvas canvas, View view, long j) {
        boolean drawChild = super.drawChild(canvas, view, j);
        if (view instanceof Workspace) {
            drawPageHints(canvas);
        }
        return drawChild;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.launcher3.InsettableFrameLayout, android.widget.FrameLayout, android.view.ViewGroup
    public LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(-2, -2);
    }

    @Override // com.android.launcher3.InsettableFrameLayout, android.widget.FrameLayout, android.view.ViewGroup
    public LayoutParams generateLayoutParams(AttributeSet attributeSet) {
        return new LayoutParams(getContext(), attributeSet);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.launcher3.InsettableFrameLayout, android.widget.FrameLayout, android.view.ViewGroup
    public LayoutParams generateLayoutParams(ViewGroup.LayoutParams layoutParams) {
        return new LayoutParams(layoutParams);
    }

    public View getAnimatedView() {
        return this.mDropView;
    }

    public float getBackgroundAlpha() {
        return this.mBackgroundAlpha;
    }

    @Override // android.view.ViewGroup
    protected int getChildDrawingOrder(int i, int i2) {
        if (this.mChildCountOnLastUpdate != i) {
            updateChildIndices();
        }
        return this.mTopViewIndex == -1 ? i2 : i2 == i - 1 ? this.mTopViewIndex : i2 < this.mTopViewIndex ? i2 : i2 + 1;
    }

    public float getDescendantCoordRelativeToSelf(View view, int[] iArr) {
        return getDescendantCoordRelativeToSelf(view, iArr, false);
    }

    public float getDescendantCoordRelativeToSelf(View view, int[] iArr, boolean z) {
        return Utilities.getDescendantCoordRelativeToParent(view, this, iArr, z);
    }

    public float getDescendantRectRelativeToSelf(View view, Rect rect) {
        this.mTmpXY[0] = 0;
        this.mTmpXY[1] = 0;
        float descendantCoordRelativeToSelf = getDescendantCoordRelativeToSelf(view, this.mTmpXY);
        rect.set(this.mTmpXY[0], this.mTmpXY[1], (int) (this.mTmpXY[0] + (view.getMeasuredWidth() * descendantCoordRelativeToSelf)), (int) (this.mTmpXY[1] + (view.getMeasuredHeight() * descendantCoordRelativeToSelf)));
        return descendantCoordRelativeToSelf;
    }

    public float getLocationInDragLayer(View view, int[] iArr) {
        iArr[0] = 0;
        iArr[1] = 0;
        return getDescendantCoordRelativeToSelf(view, iArr);
    }

    public void getViewRectRelativeToSelf(View view, Rect rect) {
        int[] iArr = new int[2];
        getLocationInWindow(iArr);
        int i = iArr[0];
        int i2 = iArr[1];
        view.getLocationInWindow(iArr);
        int i3 = iArr[0];
        int i4 = iArr[1];
        int i5 = i3 - i;
        int i6 = i4 - i2;
        rect.set(i5, i6, view.getMeasuredWidth() + i5, view.getMeasuredHeight() + i6);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void hidePageHints() {
        this.mShowPageHints = false;
        invalidate();
    }

    public float mapCoordInSelfToDescendent(View view, int[] iArr) {
        return Utilities.mapCoordInSelfToDescendent(view, this, iArr);
    }

    @Override // com.android.launcher3.InsettableFrameLayout, android.view.ViewGroup.OnHierarchyChangeListener
    public void onChildViewAdded(View view, View view2) {
        super.onChildViewAdded(view, view2);
        if (this.mOverlayView != null) {
            this.mOverlayView.bringToFront();
        }
        updateChildIndices();
    }

    @Override // com.android.launcher3.InsettableFrameLayout, android.view.ViewGroup.OnHierarchyChangeListener
    public void onChildViewRemoved(View view, View view2) {
        updateChildIndices();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void onEnterScrollArea(int i) {
        this.mInScrollArea = true;
        invalidate();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void onExitScrollArea() {
        this.mInScrollArea = false;
        invalidate();
    }

    @Override // android.view.View
    public boolean onHoverEvent(MotionEvent motionEvent) {
        return false;
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptHoverEvent(MotionEvent motionEvent) {
        Folder openFolder;
        if (this.mLauncher == null || this.mLauncher.getWorkspace() == null || (openFolder = this.mLauncher.getWorkspace().getOpenFolder()) == null || !((AccessibilityManager) getContext().getSystemService("accessibility")).isTouchExplorationEnabled()) {
            return false;
        }
        switch (motionEvent.getAction()) {
            case 7:
                boolean isEventOverDropTargetBar = !isEventOverFolder(openFolder, motionEvent) ? isInAccessibleDrag() ? isEventOverDropTargetBar(motionEvent) : false : true;
                if (!isEventOverDropTargetBar && !this.mHoverPointClosesFolder) {
                    sendTapOutsideFolderAccessibilityEvent(openFolder.isEditingName());
                    this.mHoverPointClosesFolder = true;
                    return true;
                } else if (isEventOverDropTargetBar) {
                    this.mHoverPointClosesFolder = false;
                    return false;
                } else {
                    return true;
                }
            case 8:
            default:
                return false;
            case 9:
                if (!isEventOverFolder(openFolder, motionEvent) ? isInAccessibleDrag() ? isEventOverDropTargetBar(motionEvent) : false : true) {
                    this.mHoverPointClosesFolder = false;
                    return false;
                }
                sendTapOutsideFolderAccessibilityEvent(openFolder.isEditingName());
                this.mHoverPointClosesFolder = true;
                return true;
        }
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        if (LauncherLog.DEBUG_MOTION) {
            LauncherLog.d("DragLayer", "onInterceptTouchEvent: action = " + motionEvent.getAction() + ", x = " + motionEvent.getX() + ", y = " + motionEvent.getY());
        }
        int action = motionEvent.getAction();
        if (action == 0) {
            if (handleTouchDown(motionEvent, true)) {
                return true;
            }
        } else if (action == 1 || action == 3) {
            if (this.mTouchCompleteListener != null) {
                this.mTouchCompleteListener.onTouchComplete();
            }
            this.mTouchCompleteListener = null;
        }
        clearAllResizeFrames();
        return this.mDragController.onInterceptTouchEvent(motionEvent);
    }

    @Override // android.widget.FrameLayout, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        int childCount = getChildCount();
        for (int i5 = 0; i5 < childCount; i5++) {
            View childAt = getChildAt(i5);
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) childAt.getLayoutParams();
            if (layoutParams instanceof LayoutParams) {
                LayoutParams layoutParams2 = (LayoutParams) layoutParams;
                if (layoutParams2.customPosition) {
                    childAt.layout(layoutParams2.x, layoutParams2.y, layoutParams2.x + layoutParams2.width, layoutParams2.y + layoutParams2.height);
                }
            }
        }
    }

    @Override // android.view.ViewGroup
    public boolean onRequestSendAccessibilityEvent(View view, AccessibilityEvent accessibilityEvent) {
        Folder openFolder = this.mLauncher.getWorkspace().getOpenFolder();
        if (openFolder != null && view != openFolder) {
            if (isInAccessibleDrag() && (view instanceof SearchDropTargetBar)) {
                return super.onRequestSendAccessibilityEvent(view, accessibilityEvent);
            }
            return false;
        }
        return super.onRequestSendAccessibilityEvent(view, accessibilityEvent);
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        boolean z = false;
        int action = motionEvent.getAction();
        int x = (int) motionEvent.getX();
        int y = (int) motionEvent.getY();
        if (action == 1) {
            LauncherLog.d("DragLayer", "[PerfTest --> drag widget] start process");
        }
        if (LauncherLog.DEBUG_MOTION) {
            LauncherLog.d("DragLayer", "onTouchEvent: action = " + action + ", x = " + x + ", y = " + y + ", mXDown = " + this.mXDown + ", mYDown = " + this.mYDown + ", mCurrentResizeFrame = " + this.mCurrentResizeFrame);
        }
        if (action == 0) {
            if (handleTouchDown(motionEvent, false)) {
                return true;
            }
        } else if (action == 1 || action == 3) {
            if (this.mTouchCompleteListener != null) {
                this.mTouchCompleteListener.onTouchComplete();
            }
            this.mTouchCompleteListener = null;
        }
        if (this.mCurrentResizeFrame != null) {
            z = true;
            switch (action) {
                case 1:
                case 3:
                    this.mCurrentResizeFrame.visualizeResizeForDelta(x - this.mXDown, y - this.mYDown);
                    this.mCurrentResizeFrame.onTouchUp();
                    this.mCurrentResizeFrame = null;
                    break;
                case 2:
                    this.mCurrentResizeFrame.visualizeResizeForDelta(x - this.mXDown, y - this.mYDown);
                    break;
            }
        }
        if (z) {
            return true;
        }
        return this.mDragController.onTouchEvent(motionEvent);
    }

    public void setBackgroundAlpha(float f) {
        if (f != this.mBackgroundAlpha) {
            this.mBackgroundAlpha = f;
            invalidate();
        }
    }

    public void setTouchCompleteListener(TouchCompleteListener touchCompleteListener) {
        this.mTouchCompleteListener = touchCompleteListener;
    }

    public void setup(Launcher launcher, DragController dragController) {
        this.mLauncher = launcher;
        this.mDragController = dragController;
    }

    public void showOverlayView(View view) {
        LayoutParams layoutParams = new LayoutParams(-1, -1);
        this.mOverlayView = view;
        addView(view, layoutParams);
        this.mOverlayView.bringToFront();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void showPageHints() {
        this.mShowPageHints = true;
        Workspace workspace = this.mLauncher.getWorkspace();
        getDescendantRectRelativeToSelf(workspace.getChildAt(workspace.numCustomPages()), this.mScrollChildPosition);
        invalidate();
    }
}
