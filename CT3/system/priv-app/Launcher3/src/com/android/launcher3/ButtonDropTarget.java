package com.android.launcher3;

import android.animation.AnimatorSet;
import android.animation.FloatArrayEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;
import com.android.launcher3.DragController;
import com.android.launcher3.DropTarget;
/* loaded from: a.zip:com/android/launcher3/ButtonDropTarget.class */
public abstract class ButtonDropTarget extends TextView implements DropTarget, DragController.DragListener, View.OnClickListener {
    private static int DRAG_VIEW_DROP_DURATION = 285;
    protected boolean mActive;
    private int mBottomDragPadding;
    private AnimatorSet mCurrentColorAnim;
    ColorMatrix mCurrentFilter;
    protected Drawable mDrawable;
    ColorMatrix mDstFilter;
    protected int mHoverColor;
    protected Launcher mLauncher;
    protected ColorStateList mOriginalTextColor;
    protected SearchDropTargetBar mSearchDropTargetBar;
    ColorMatrix mSrcFilter;

    public ButtonDropTarget(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public ButtonDropTarget(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mHoverColor = 0;
        this.mBottomDragPadding = getResources().getDimensionPixelSize(2131230796);
    }

    @TargetApi(21)
    private void animateTextColor(int i) {
        if (this.mCurrentColorAnim != null) {
            this.mCurrentColorAnim.cancel();
        }
        this.mCurrentColorAnim = new AnimatorSet();
        this.mCurrentColorAnim.setDuration(DragView.COLOR_CHANGE_DURATION);
        if (this.mSrcFilter == null) {
            this.mSrcFilter = new ColorMatrix();
            this.mDstFilter = new ColorMatrix();
            this.mCurrentFilter = new ColorMatrix();
        }
        DragView.setColorScale(getTextColor(), this.mSrcFilter);
        DragView.setColorScale(i, this.mDstFilter);
        ValueAnimator ofObject = ValueAnimator.ofObject(new FloatArrayEvaluator(this.mCurrentFilter.getArray()), this.mSrcFilter.getArray(), this.mDstFilter.getArray());
        ofObject.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(this) { // from class: com.android.launcher3.ButtonDropTarget.1
            final ButtonDropTarget this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                this.this$0.mDrawable.setColorFilter(new ColorMatrixColorFilter(this.this$0.mCurrentFilter));
                this.this$0.invalidate();
            }
        });
        this.mCurrentColorAnim.play(ofObject);
        this.mCurrentColorAnim.play(ObjectAnimator.ofArgb(this, "textColor", i));
        this.mCurrentColorAnim.start();
    }

    @Override // com.android.launcher3.DropTarget
    public final boolean acceptDrop(DropTarget.DragObject dragObject) {
        return supportsDrop(dragObject.dragSource, dragObject.dragInfo);
    }

    abstract void completeDrop(DropTarget.DragObject dragObject);

    public void enableAccessibleDrag(boolean z) {
        setOnClickListener(z ? this : null);
    }

    @Override // com.android.launcher3.DropTarget
    public void getHitRectRelativeToDragLayer(Rect rect) {
        super.getHitRect(rect);
        rect.bottom += this.mBottomDragPadding;
        int[] iArr = new int[2];
        this.mLauncher.getDragLayer().getDescendantCoordRelativeToSelf(this, iArr);
        rect.offsetTo(iArr[0], iArr[1]);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public Rect getIconRect(int i, int i2, int i3, int i4) {
        int paddingLeft;
        int i5;
        DragLayer dragLayer = this.mLauncher.getDragLayer();
        Rect rect = new Rect();
        dragLayer.getViewRectRelativeToSelf(this, rect);
        if (Utilities.isRtl(getResources())) {
            i5 = rect.right - getPaddingRight();
            paddingLeft = i5 - i3;
        } else {
            paddingLeft = rect.left + getPaddingLeft();
            i5 = paddingLeft + i3;
        }
        int measuredHeight = rect.top + ((getMeasuredHeight() - i4) / 2);
        rect.set(paddingLeft, measuredHeight, i5, measuredHeight + i4);
        rect.offset((-(i - i3)) / 2, (-(i2 - i4)) / 2);
        return rect;
    }

    public int getTextColor() {
        return getTextColors().getDefaultColor();
    }

    @Override // com.android.launcher3.DropTarget
    public boolean isDropEnabled() {
        return this.mActive;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        LauncherAppState.getInstance().getAccessibilityDelegate().handleAccessibleDrop(this, null, null);
    }

    @Override // com.android.launcher3.DragController.DragListener
    public void onDragEnd() {
        this.mActive = false;
    }

    @Override // com.android.launcher3.DropTarget
    public final void onDragEnter(DropTarget.DragObject dragObject) {
        dragObject.dragView.setColor(this.mHoverColor);
        if (Utilities.ATLEAST_LOLLIPOP) {
            animateTextColor(this.mHoverColor);
        } else {
            if (this.mCurrentFilter == null) {
                this.mCurrentFilter = new ColorMatrix();
            }
            DragView.setColorScale(this.mHoverColor, this.mCurrentFilter);
            this.mDrawable.setColorFilter(new ColorMatrixColorFilter(this.mCurrentFilter));
            setTextColor(this.mHoverColor);
        }
        if (dragObject.stateAnnouncer != null) {
            dragObject.stateAnnouncer.cancel();
        }
        sendAccessibilityEvent(4);
    }

    @Override // com.android.launcher3.DropTarget
    public final void onDragExit(DropTarget.DragObject dragObject) {
        if (dragObject.dragComplete) {
            dragObject.dragView.setColor(this.mHoverColor);
            return;
        }
        dragObject.dragView.setColor(0);
        resetHoverColor();
    }

    @Override // com.android.launcher3.DropTarget
    public void onDragOver(DropTarget.DragObject dragObject) {
    }

    @Override // com.android.launcher3.DragController.DragListener
    public final void onDragStart(DragSource dragSource, Object obj, int i) {
        this.mActive = supportsDrop(dragSource, obj);
        this.mDrawable.setColorFilter(null);
        if (this.mCurrentColorAnim != null) {
            this.mCurrentColorAnim.cancel();
            this.mCurrentColorAnim = null;
        }
        setTextColor(this.mOriginalTextColor);
        ((ViewGroup) getParent()).setVisibility(this.mActive ? 0 : 8);
    }

    @Override // com.android.launcher3.DropTarget
    public void onDrop(DropTarget.DragObject dragObject) {
        DragLayer dragLayer = this.mLauncher.getDragLayer();
        Rect rect = new Rect();
        dragLayer.getViewRectRelativeToSelf(dragObject.dragView, rect);
        Rect iconRect = getIconRect(dragObject.dragView.getMeasuredWidth(), dragObject.dragView.getMeasuredHeight(), this.mDrawable.getIntrinsicWidth(), this.mDrawable.getIntrinsicHeight());
        float width = iconRect.width() / rect.width();
        this.mSearchDropTargetBar.deferOnDragEnd();
        dragLayer.animateView(dragObject.dragView, rect, iconRect, width, 1.0f, 1.0f, 0.1f, 0.1f, DRAG_VIEW_DROP_DURATION, new DecelerateInterpolator(2.0f), new LinearInterpolator(), new Runnable(this, dragObject) { // from class: com.android.launcher3.ButtonDropTarget.2
            final ButtonDropTarget this$0;
            final DropTarget.DragObject val$d;

            {
                this.this$0 = this;
                this.val$d = dragObject;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.completeDrop(this.val$d);
                this.this$0.mSearchDropTargetBar.onDragEnd();
                this.this$0.mLauncher.exitSpringLoadedDragModeDelayed(true, 0, null);
            }
        }, 0, null);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mOriginalTextColor = getTextColors();
        if (((Launcher) getContext()).getDeviceProfile().isVerticalBarLayout()) {
            setText("");
        }
    }

    @Override // com.android.launcher3.DropTarget
    public void onFlingToDelete(DropTarget.DragObject dragObject, PointF pointF) {
    }

    @Override // com.android.launcher3.DropTarget
    public void prepareAccessibilityDrop() {
    }

    protected void resetHoverColor() {
        if (Utilities.ATLEAST_LOLLIPOP) {
            animateTextColor(this.mOriginalTextColor.getDefaultColor());
            return;
        }
        this.mDrawable.setColorFilter(null);
        setTextColor(this.mOriginalTextColor);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @TargetApi(17)
    public void setDrawable(int i) {
        this.mDrawable = getResources().getDrawable(i);
        if (Utilities.ATLEAST_JB_MR1) {
            setCompoundDrawablesRelativeWithIntrinsicBounds(this.mDrawable, (Drawable) null, (Drawable) null, (Drawable) null);
        } else {
            setCompoundDrawablesWithIntrinsicBounds(this.mDrawable, (Drawable) null, (Drawable) null, (Drawable) null);
        }
    }

    public void setLauncher(Launcher launcher) {
        this.mLauncher = launcher;
    }

    public void setSearchDropTargetBar(SearchDropTargetBar searchDropTargetBar) {
        this.mSearchDropTargetBar = searchDropTargetBar;
    }

    protected abstract boolean supportsDrop(DragSource dragSource, Object obj);
}
