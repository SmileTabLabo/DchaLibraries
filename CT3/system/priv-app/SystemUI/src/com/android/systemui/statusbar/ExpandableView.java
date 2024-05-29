package com.android.systemui.statusbar;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import java.util.ArrayList;
/* loaded from: a.zip:com/android/systemui/statusbar/ExpandableView.class */
public abstract class ExpandableView extends FrameLayout {
    private static Rect mClipRect = new Rect();
    private int mActualHeight;
    private boolean mChangingPosition;
    private boolean mClipToActualHeight;
    protected int mClipTopAmount;
    private boolean mDark;
    private ArrayList<View> mMatchParentViews;
    private int mMinClipTopAmount;
    protected OnHeightChangedListener mOnHeightChangedListener;
    private ViewGroup mTransientContainer;
    private boolean mWillBeGone;

    /* loaded from: a.zip:com/android/systemui/statusbar/ExpandableView$OnHeightChangedListener.class */
    public interface OnHeightChangedListener {
        void onHeightChanged(ExpandableView expandableView, boolean z);

        void onReset(ExpandableView expandableView);
    }

    public ExpandableView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mMatchParentViews = new ArrayList<>();
        this.mMinClipTopAmount = 0;
        this.mClipToActualHeight = true;
        this.mChangingPosition = false;
    }

    private void updateClipping() {
        if (!this.mClipToActualHeight) {
            setClipBounds(null);
            return;
        }
        int clipTopAmount = getClipTopAmount();
        int i = clipTopAmount;
        if (clipTopAmount >= getActualHeight()) {
            i = getActualHeight() - 1;
        }
        mClipRect.set(0, i, getWidth(), getActualHeight() + getExtraBottomPadding());
        setClipBounds(mClipRect);
    }

    public boolean areChildrenExpanded() {
        return false;
    }

    public int getActualHeight() {
        return this.mActualHeight;
    }

    public void getBoundsOnScreen(Rect rect, boolean z) {
        super.getBoundsOnScreen(rect, z);
        if (getTop() + getTranslationY() < 0.0f) {
            rect.top = (int) (rect.top + getTop() + getTranslationY());
        }
        rect.bottom = rect.top + getActualHeight();
        rect.top += getClipTopAmount();
    }

    public int getClipTopAmount() {
        return this.mClipTopAmount;
    }

    public int getCollapsedHeight() {
        return getHeight();
    }

    @Override // android.view.View
    public void getDrawingRect(Rect rect) {
        super.getDrawingRect(rect);
        rect.left = (int) (rect.left + getTranslationX());
        rect.right = (int) (rect.right + getTranslationX());
        rect.bottom = (int) (rect.top + getTranslationY() + getActualHeight());
        rect.top = (int) (rect.top + getTranslationY() + getClipTopAmount());
    }

    public int getExtraBottomPadding() {
        return 0;
    }

    public float getIncreasedPaddingAmount() {
        return 0.0f;
    }

    public int getIntrinsicHeight() {
        return getHeight();
    }

    public int getMaxContentHeight() {
        return getHeight();
    }

    public int getMinHeight() {
        return getHeight();
    }

    public float getOutlineAlpha() {
        return 0.0f;
    }

    public int getOutlineTranslation() {
        return 0;
    }

    public float getShadowAlpha() {
        return 0.0f;
    }

    public ViewGroup getTransientContainer() {
        return this.mTransientContainer;
    }

    public float getTranslation() {
        return getTranslationX();
    }

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        boolean z = false;
        if (super.hasOverlappingRendering()) {
            z = false;
            if (getActualHeight() <= getHeight()) {
                z = true;
            }
        }
        return z;
    }

    public boolean isChangingPosition() {
        return this.mChangingPosition;
    }

    public boolean isChildInGroup() {
        return false;
    }

    public boolean isContentExpandable() {
        return false;
    }

    public boolean isDark() {
        return this.mDark;
    }

    public boolean isGroupExpanded() {
        return false;
    }

    public boolean isGroupExpansionChanging() {
        return false;
    }

    public boolean isSummaryWithChildren() {
        return false;
    }

    public boolean isTransparent() {
        return false;
    }

    public boolean mustStayOnScreen() {
        return false;
    }

    public void notifyHeightChanged(boolean z) {
        if (this.mOnHeightChangedListener != null) {
            this.mOnHeightChangedListener.onHeightChanged(this, z);
        }
    }

    public void onHeightReset() {
        if (this.mOnHeightChangedListener != null) {
            this.mOnHeightChangedListener.onReset(this);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.widget.FrameLayout, android.view.ViewGroup, android.view.View
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        updateClipping();
    }

    @Override // android.widget.FrameLayout, android.view.View
    protected void onMeasure(int i, int i2) {
        int size = View.MeasureSpec.getSize(i2);
        int mode = View.MeasureSpec.getMode(i2);
        int i3 = Integer.MAX_VALUE;
        if (mode != 0) {
            i3 = Integer.MAX_VALUE;
            if (size != 0) {
                i3 = Math.min(size, Integer.MAX_VALUE);
            }
        }
        int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(i3, Integer.MIN_VALUE);
        int i4 = 0;
        int childCount = getChildCount();
        for (int i5 = 0; i5 < childCount; i5++) {
            View childAt = getChildAt(i5);
            if (childAt.getVisibility() != 8) {
                int i6 = makeMeasureSpec;
                ViewGroup.LayoutParams layoutParams = childAt.getLayoutParams();
                if (layoutParams.height != -1) {
                    if (layoutParams.height >= 0) {
                        i6 = layoutParams.height > i3 ? View.MeasureSpec.makeMeasureSpec(i3, 1073741824) : View.MeasureSpec.makeMeasureSpec(layoutParams.height, 1073741824);
                    }
                    childAt.measure(getChildMeasureSpec(i, 0, layoutParams.width), i6);
                    i4 = Math.max(i4, childAt.getMeasuredHeight());
                } else {
                    this.mMatchParentViews.add(childAt);
                }
            }
        }
        int min = mode == 1073741824 ? size : Math.min(i3, i4);
        int makeMeasureSpec2 = View.MeasureSpec.makeMeasureSpec(min, 1073741824);
        for (View view : this.mMatchParentViews) {
            view.measure(getChildMeasureSpec(i, 0, view.getLayoutParams().width), makeMeasureSpec2);
        }
        this.mMatchParentViews.clear();
        setMeasuredDimension(View.MeasureSpec.getSize(i), min);
    }

    public abstract void performAddAnimation(long j, long j2);

    public abstract void performRemoveAnimation(long j, float f, Runnable runnable);

    public boolean pointInView(float f, float f2, float f3) {
        float f4 = this.mClipTopAmount;
        float f5 = this.mActualHeight;
        boolean z = false;
        if (f >= (-f3)) {
            z = false;
            if (f2 >= f4 - f3) {
                z = false;
                if (f < (this.mRight - this.mLeft) + f3) {
                    z = false;
                    if (f2 < f5 + f3) {
                        z = true;
                    }
                }
            }
        }
        return z;
    }

    public void setActualHeight(int i) {
        setActualHeight(i, true);
    }

    public void setActualHeight(int i, boolean z) {
        this.mActualHeight = i;
        updateClipping();
        if (z) {
            notifyHeightChanged(false);
        }
    }

    public void setActualHeightAnimating(boolean z) {
    }

    public void setBelowSpeedBump(boolean z) {
    }

    public void setChangingPosition(boolean z) {
        this.mChangingPosition = z;
    }

    public void setClipToActualHeight(boolean z) {
        this.mClipToActualHeight = z;
        updateClipping();
    }

    public void setClipTopAmount(int i) {
        this.mClipTopAmount = i;
        updateClipping();
    }

    public void setDark(boolean z, boolean z2, long j) {
        this.mDark = z;
    }

    public void setDimmed(boolean z, boolean z2) {
    }

    public void setFakeShadowIntensity(float f, float f2, int i, int i2) {
    }

    public void setHideSensitive(boolean z, boolean z2, long j, long j2) {
    }

    public void setHideSensitiveForIntrinsicHeight(boolean z) {
    }

    @Override // android.view.View
    public void setLayerType(int i, Paint paint) {
        if (hasOverlappingRendering()) {
            super.setLayerType(i, paint);
        }
    }

    public void setMinClipTopAmount(int i) {
        this.mMinClipTopAmount = i;
    }

    public void setOnHeightChangedListener(OnHeightChangedListener onHeightChangedListener) {
        this.mOnHeightChangedListener = onHeightChangedListener;
    }

    public void setShadowAlpha(float f) {
    }

    public void setTransientContainer(ViewGroup viewGroup) {
        this.mTransientContainer = viewGroup;
    }

    public void setTranslation(float f) {
        setTranslationX(f);
    }

    public void setWillBeGone(boolean z) {
        this.mWillBeGone = z;
    }

    public boolean willBeGone() {
        return this.mWillBeGone;
    }
}
