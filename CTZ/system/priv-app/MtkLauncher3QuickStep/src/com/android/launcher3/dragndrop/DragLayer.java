package com.android.launcher3.dragndrop;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.Interpolator;
import android.widget.TextView;
import com.android.launcher3.AbstractFloatingView;
import com.android.launcher3.CellLayout;
import com.android.launcher3.DropTargetBar;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.ShortcutAndWidgetContainer;
import com.android.launcher3.Workspace;
import com.android.launcher3.anim.Interpolators;
import com.android.launcher3.compat.AccessibilityManagerCompat;
import com.android.launcher3.folder.Folder;
import com.android.launcher3.folder.FolderIcon;
import com.android.launcher3.graphics.ViewScrim;
import com.android.launcher3.graphics.WorkspaceAndHotseatScrim;
import com.android.launcher3.keyboard.ViewGroupFocusHelper;
import com.android.launcher3.uioverrides.UiFactory;
import com.android.launcher3.views.BaseDragLayer;
import java.util.ArrayList;
/* loaded from: classes.dex */
public class DragLayer extends BaseDragLayer<Launcher> {
    private static final int ALPHA_CHANNEL_COUNT = 4;
    public static final int ALPHA_INDEX_LAUNCHER_LOAD = 1;
    public static final int ALPHA_INDEX_OVERLAY = 0;
    public static final int ALPHA_INDEX_SWIPE_UP = 3;
    public static final int ALPHA_INDEX_TRANSITIONS = 2;
    public static final int ANIMATION_END_DISAPPEAR = 0;
    public static final int ANIMATION_END_REMAIN_VISIBLE = 2;
    View mAnchorView;
    int mAnchorViewInitialScrollX;
    private int mChildCountOnLastUpdate;
    private final TimeInterpolator mCubicEaseOutInterpolator;
    DragController mDragController;
    private ValueAnimator mDropAnim;
    DragView mDropView;
    private final ViewGroupFocusHelper mFocusIndicatorHelper;
    private boolean mHoverPointClosesFolder;
    private final WorkspaceAndHotseatScrim mScrim;
    private int mTopViewIndex;

    public DragLayer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet, 4);
        this.mDropAnim = null;
        this.mCubicEaseOutInterpolator = Interpolators.DEACCEL_1_5;
        this.mDropView = null;
        this.mAnchorViewInitialScrollX = 0;
        this.mAnchorView = null;
        this.mHoverPointClosesFolder = false;
        this.mChildCountOnLastUpdate = -1;
        setMotionEventSplittingEnabled(false);
        setChildrenDrawingOrderEnabled(true);
        this.mFocusIndicatorHelper = new ViewGroupFocusHelper(this);
        this.mScrim = new WorkspaceAndHotseatScrim(this);
    }

    public void setup(DragController dragController, Workspace workspace) {
        this.mDragController = dragController;
        this.mScrim.setWorkspace(workspace);
        recreateControllers();
    }

    public void recreateControllers() {
        this.mControllers = UiFactory.createTouchControllers((Launcher) this.mActivity);
    }

    public ViewGroupFocusHelper getFocusIndicatorHelper() {
        return this.mFocusIndicatorHelper;
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        return this.mDragController.dispatchKeyEvent(keyEvent) || super.dispatchKeyEvent(keyEvent);
    }

    @Override // android.view.ViewGroup
    protected boolean drawChild(Canvas canvas, View view, long j) {
        ViewScrim viewScrim = ViewScrim.get(view);
        if (viewScrim != null) {
            viewScrim.draw(canvas, getWidth(), getHeight());
        }
        return super.drawChild(canvas, view, j);
    }

    @Override // com.android.launcher3.views.BaseDragLayer
    protected boolean findActiveController(MotionEvent motionEvent) {
        if (((Launcher) this.mActivity).getStateManager().getState().disableInteraction) {
            this.mActiveController = null;
            return true;
        }
        return super.findActiveController(motionEvent);
    }

    private boolean isEventOverAccessibleDropTargetBar(MotionEvent motionEvent) {
        return isInAccessibleDrag() && isEventOverView(((Launcher) this.mActivity).getDropTargetBar(), motionEvent);
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptHoverEvent(MotionEvent motionEvent) {
        if (this.mActivity == 0 || ((Launcher) this.mActivity).getWorkspace() == null) {
            return false;
        }
        AbstractFloatingView topOpenView = AbstractFloatingView.getTopOpenView(this.mActivity);
        if ((topOpenView instanceof Folder) && ((AccessibilityManager) getContext().getSystemService("accessibility")).isTouchExplorationEnabled()) {
            Folder folder = (Folder) topOpenView;
            int action = motionEvent.getAction();
            if (action == 7) {
                boolean z = isEventOverView(topOpenView, motionEvent) || isEventOverAccessibleDropTargetBar(motionEvent);
                if (!z && !this.mHoverPointClosesFolder) {
                    sendTapOutsideFolderAccessibilityEvent(folder.isEditingName());
                    this.mHoverPointClosesFolder = true;
                    return true;
                } else if (!z) {
                    return true;
                } else {
                    this.mHoverPointClosesFolder = false;
                }
            } else if (action == 9) {
                if (!(isEventOverView(topOpenView, motionEvent) || isEventOverAccessibleDropTargetBar(motionEvent))) {
                    sendTapOutsideFolderAccessibilityEvent(folder.isEditingName());
                    this.mHoverPointClosesFolder = true;
                    return true;
                }
                this.mHoverPointClosesFolder = false;
            }
        }
        return false;
    }

    private void sendTapOutsideFolderAccessibilityEvent(boolean z) {
        AccessibilityManagerCompat.sendCustomAccessibilityEvent(this, 8, getContext().getString(z ? R.string.folder_tap_to_rename : R.string.folder_tap_to_close));
    }

    @Override // android.view.View
    public boolean onHoverEvent(MotionEvent motionEvent) {
        return false;
    }

    private boolean isInAccessibleDrag() {
        return ((Launcher) this.mActivity).getAccessibilityDelegate().isInAccessibleDrag();
    }

    @Override // com.android.launcher3.views.BaseDragLayer, android.view.ViewGroup
    public boolean onRequestSendAccessibilityEvent(View view, AccessibilityEvent accessibilityEvent) {
        if (isInAccessibleDrag() && (view instanceof DropTargetBar)) {
            return true;
        }
        return super.onRequestSendAccessibilityEvent(view, accessibilityEvent);
    }

    @Override // com.android.launcher3.views.BaseDragLayer, android.view.ViewGroup, android.view.View
    public void addChildrenForAccessibility(ArrayList<View> arrayList) {
        AbstractFloatingView topOpenViewWithType = AbstractFloatingView.getTopOpenViewWithType(this.mActivity, AbstractFloatingView.TYPE_ACCESSIBLE);
        if (topOpenViewWithType != null) {
            addAccessibleChildToList(topOpenViewWithType, arrayList);
            if (isInAccessibleDrag()) {
                addAccessibleChildToList(((Launcher) this.mActivity).getDropTargetBar(), arrayList);
                return;
            }
            return;
        }
        super.addChildrenForAccessibility(arrayList);
    }

    @Override // com.android.launcher3.views.BaseDragLayer, android.view.ViewGroup, android.view.View
    public boolean dispatchUnhandledMove(View view, int i) {
        return super.dispatchUnhandledMove(view, i) || this.mDragController.dispatchUnhandledMove(view, i);
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        motionEvent.offsetLocation(getTranslationX(), 0.0f);
        try {
            return super.dispatchTouchEvent(motionEvent);
        } finally {
            motionEvent.offsetLocation(-getTranslationX(), 0.0f);
        }
    }

    public void animateViewIntoPosition(DragView dragView, int[] iArr, float f, float f2, float f3, int i, Runnable runnable, int i2) {
        Rect rect = new Rect();
        getViewRectRelativeToSelf(dragView, rect);
        animateViewIntoPosition(dragView, rect.left, rect.top, iArr[0], iArr[1], f, 1.0f, 1.0f, f2, f3, runnable, i, i2, null);
    }

    public void animateViewIntoPosition(DragView dragView, View view, View view2) {
        animateViewIntoPosition(dragView, view, -1, view2);
    }

    public void animateViewIntoPosition(DragView dragView, final View view, int i, View view2) {
        int round;
        int round2;
        float f;
        int i2;
        CellLayout.LayoutParams layoutParams = (CellLayout.LayoutParams) view.getLayoutParams();
        ((ShortcutAndWidgetContainer) view.getParent()).measureChild(view);
        Rect rect = new Rect();
        getViewRectRelativeToSelf(dragView, rect);
        float scaleX = view.getScaleX();
        float f2 = 1.0f - scaleX;
        int[] iArr = {layoutParams.x + ((int) ((view.getMeasuredWidth() * f2) / 2.0f)), layoutParams.y + ((int) ((view.getMeasuredHeight() * f2) / 2.0f))};
        float descendantCoordRelativeToSelf = getDescendantCoordRelativeToSelf((View) view.getParent(), iArr) * scaleX;
        int i3 = iArr[0];
        int i4 = iArr[1];
        if (view instanceof TextView) {
            float intrinsicIconScaleFactor = descendantCoordRelativeToSelf / dragView.getIntrinsicIconScaleFactor();
            int round3 = (int) ((i4 + Math.round(((TextView) view).getPaddingTop() * intrinsicIconScaleFactor)) - ((dragView.getMeasuredHeight() * (1.0f - intrinsicIconScaleFactor)) / 2.0f));
            if (dragView.getDragVisualizeOffset() != null) {
                round3 -= Math.round(dragView.getDragVisualizeOffset().y * intrinsicIconScaleFactor);
            }
            round2 = i3 - ((dragView.getMeasuredWidth() - Math.round(descendantCoordRelativeToSelf * view.getMeasuredWidth())) / 2);
            i2 = round3;
            f = intrinsicIconScaleFactor;
        } else {
            if (!(view instanceof FolderIcon)) {
                round = i4 - (Math.round((dragView.getHeight() - view.getMeasuredHeight()) * descendantCoordRelativeToSelf) / 2);
                round2 = i3 - (Math.round((dragView.getMeasuredWidth() - view.getMeasuredWidth()) * descendantCoordRelativeToSelf) / 2);
            } else {
                round = (int) (((int) ((i4 + Math.round((view.getPaddingTop() - dragView.getDragRegionTop()) * descendantCoordRelativeToSelf)) - ((dragView.getBlurSizeOutline() * descendantCoordRelativeToSelf) / 2.0f))) - (((1.0f - descendantCoordRelativeToSelf) * dragView.getMeasuredHeight()) / 2.0f));
                round2 = i3 - ((dragView.getMeasuredWidth() - Math.round(view.getMeasuredWidth() * descendantCoordRelativeToSelf)) / 2);
            }
            f = descendantCoordRelativeToSelf;
            i2 = round;
        }
        int i5 = rect.left;
        int i6 = rect.top;
        view.setVisibility(4);
        animateViewIntoPosition(dragView, i5, i6, round2, i2, 1.0f, 1.0f, 1.0f, f, f, new Runnable() { // from class: com.android.launcher3.dragndrop.-$$Lambda$DragLayer$4i4C7xaW5PxMjg4ve7qhaNJy2wk
            @Override // java.lang.Runnable
            public final void run() {
                view.setVisibility(0);
            }
        }, 0, i, view2);
    }

    public void animateViewIntoPosition(DragView dragView, int i, int i2, int i3, int i4, float f, float f2, float f3, float f4, float f5, Runnable runnable, int i5, int i6, View view) {
        animateView(dragView, new Rect(i, i2, dragView.getMeasuredWidth() + i, dragView.getMeasuredHeight() + i2), new Rect(i3, i4, dragView.getMeasuredWidth() + i3, dragView.getMeasuredHeight() + i4), f, f2, f3, f4, f5, i6, null, null, runnable, i5, view);
    }

    public void animateView(final DragView dragView, final Rect rect, final Rect rect2, final float f, final float f2, final float f3, final float f4, final float f5, int i, final Interpolator interpolator, final Interpolator interpolator2, Runnable runnable, int i2, View view) {
        int i3;
        float hypot = (float) Math.hypot(rect2.left - rect.left, rect2.top - rect.top);
        Resources resources = getResources();
        float integer = resources.getInteger(R.integer.config_dropAnimMaxDist);
        if (i < 0) {
            int integer2 = resources.getInteger(R.integer.config_dropAnimMaxDuration);
            if (hypot < integer) {
                integer2 = (int) (integer2 * this.mCubicEaseOutInterpolator.getInterpolation(hypot / integer));
            }
            i3 = Math.max(integer2, resources.getInteger(R.integer.config_dropAnimMinDuration));
        } else {
            i3 = i;
        }
        TimeInterpolator timeInterpolator = null;
        if (interpolator2 == null || interpolator == null) {
            timeInterpolator = this.mCubicEaseOutInterpolator;
        }
        TimeInterpolator timeInterpolator2 = timeInterpolator;
        final float alpha = dragView.getAlpha();
        final float scaleX = dragView.getScaleX();
        animateView(dragView, new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.launcher3.dragndrop.DragLayer.1
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float interpolation;
                float interpolation2;
                float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                int measuredWidth = dragView.getMeasuredWidth();
                int measuredHeight = dragView.getMeasuredHeight();
                if (interpolator2 != null) {
                    interpolation = interpolator2.getInterpolation(floatValue);
                } else {
                    interpolation = floatValue;
                }
                if (interpolator != null) {
                    interpolation2 = interpolator.getInterpolation(floatValue);
                } else {
                    interpolation2 = floatValue;
                }
                float f6 = f2 * scaleX;
                float f7 = f3 * scaleX;
                float f8 = 1.0f - floatValue;
                float f9 = (f4 * floatValue) + (f6 * f8);
                float f10 = (f5 * floatValue) + (f8 * f7);
                float f11 = (f * interpolation) + (alpha * (1.0f - interpolation));
                float f12 = rect.left + (((f6 - 1.0f) * measuredWidth) / 2.0f);
                float f13 = rect.top + (((f7 - 1.0f) * measuredHeight) / 2.0f);
                int round = (int) (f12 + Math.round((rect2.left - f12) * interpolation2));
                int round2 = (int) (f13 + Math.round((rect2.top - f13) * interpolation2));
                int scrollX = (round - DragLayer.this.mDropView.getScrollX()) + (DragLayer.this.mAnchorView == null ? 0 : (int) (DragLayer.this.mAnchorView.getScaleX() * (DragLayer.this.mAnchorViewInitialScrollX - DragLayer.this.mAnchorView.getScrollX())));
                int scrollY = round2 - DragLayer.this.mDropView.getScrollY();
                DragLayer.this.mDropView.setTranslationX(scrollX);
                DragLayer.this.mDropView.setTranslationY(scrollY);
                DragLayer.this.mDropView.setScaleX(f9);
                DragLayer.this.mDropView.setScaleY(f10);
                DragLayer.this.mDropView.setAlpha(f11);
            }
        }, i3, timeInterpolator2, runnable, i2, view);
    }

    public void animateView(DragView dragView, ValueAnimator.AnimatorUpdateListener animatorUpdateListener, int i, TimeInterpolator timeInterpolator, final Runnable runnable, final int i2, View view) {
        if (this.mDropAnim != null) {
            this.mDropAnim.cancel();
        }
        this.mDropView = dragView;
        this.mDropView.cancelAnimation();
        this.mDropView.requestLayout();
        if (view != null) {
            this.mAnchorViewInitialScrollX = view.getScrollX();
        }
        this.mAnchorView = view;
        this.mDropAnim = new ValueAnimator();
        this.mDropAnim.setInterpolator(timeInterpolator);
        this.mDropAnim.setDuration(i);
        this.mDropAnim.setFloatValues(0.0f, 1.0f);
        this.mDropAnim.addUpdateListener(animatorUpdateListener);
        this.mDropAnim.addListener(new AnimatorListenerAdapter() { // from class: com.android.launcher3.dragndrop.DragLayer.2
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                if (runnable != null) {
                    runnable.run();
                }
                if (i2 == 0) {
                    DragLayer.this.clearAnimatedView();
                }
                DragLayer.this.mDropAnim = null;
            }
        });
        this.mDropAnim.start();
    }

    public void clearAnimatedView() {
        if (this.mDropAnim != null) {
            this.mDropAnim.cancel();
        }
        this.mDropAnim = null;
        if (this.mDropView != null) {
            this.mDragController.onDeferredEndDrag(this.mDropView);
        }
        this.mDropView = null;
        invalidate();
    }

    public View getAnimatedView() {
        return this.mDropView;
    }

    @Override // com.android.launcher3.InsettableFrameLayout, android.view.ViewGroup
    public void onViewAdded(View view) {
        super.onViewAdded(view);
        updateChildIndices();
        UiFactory.onLauncherStateOrFocusChanged((Launcher) this.mActivity);
    }

    @Override // com.android.launcher3.views.BaseDragLayer, android.view.ViewGroup
    public void onViewRemoved(View view) {
        super.onViewRemoved(view);
        updateChildIndices();
        UiFactory.onLauncherStateOrFocusChanged((Launcher) this.mActivity);
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public void bringChildToFront(View view) {
        super.bringChildToFront(view);
        updateChildIndices();
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

    @Override // android.view.ViewGroup
    protected int getChildDrawingOrder(int i, int i2) {
        if (this.mChildCountOnLastUpdate != i) {
            updateChildIndices();
        }
        if (this.mTopViewIndex == -1) {
            return i2;
        }
        if (i2 == i - 1) {
            return this.mTopViewIndex;
        }
        if (i2 < this.mTopViewIndex) {
            return i2;
        }
        return i2 + 1;
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void dispatchDraw(Canvas canvas) {
        this.mScrim.draw(canvas);
        this.mFocusIndicatorHelper.draw(canvas);
        super.dispatchDraw(canvas);
    }

    @Override // android.view.View
    protected void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        this.mScrim.setSize(i, i2);
    }

    @Override // com.android.launcher3.InsettableFrameLayout, com.android.launcher3.Insettable
    public void setInsets(Rect rect) {
        super.setInsets(rect);
        this.mScrim.onInsetsChanged(rect);
    }

    public WorkspaceAndHotseatScrim getScrim() {
        return this.mScrim;
    }
}
