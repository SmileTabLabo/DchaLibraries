package android.support.v7.widget;

import android.content.Context;
import android.os.Build;
import android.support.v7.appcompat.R$attr;
import android.support.v7.appcompat.R$layout;
import android.support.v7.appcompat.R$styleable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.LinearLayout;
/* loaded from: a.zip:android/support/v7/widget/ActionBarContextView.class */
public class ActionBarContextView extends AbsActionBarView {
    private View mClose;
    private int mCloseItemLayout;
    private View mCustomView;
    private int mSubtitleStyleRes;
    private CharSequence mTitle;
    private LinearLayout mTitleLayout;
    private boolean mTitleOptional;
    private int mTitleStyleRes;

    public ActionBarContextView(Context context) {
        this(context, null);
    }

    public ActionBarContextView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, R$attr.actionModeStyle);
    }

    public ActionBarContextView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        TintTypedArray obtainStyledAttributes = TintTypedArray.obtainStyledAttributes(context, attributeSet, R$styleable.ActionMode, i, 0);
        setBackgroundDrawable(obtainStyledAttributes.getDrawable(R$styleable.ActionMode_background));
        this.mTitleStyleRes = obtainStyledAttributes.getResourceId(R$styleable.ActionMode_titleTextStyle, 0);
        this.mSubtitleStyleRes = obtainStyledAttributes.getResourceId(R$styleable.ActionMode_subtitleTextStyle, 0);
        this.mContentHeight = obtainStyledAttributes.getLayoutDimension(R$styleable.ActionMode_height, 0);
        this.mCloseItemLayout = obtainStyledAttributes.getResourceId(R$styleable.ActionMode_closeItemLayout, R$layout.abc_action_mode_close_item_material);
        obtainStyledAttributes.recycle();
    }

    @Override // android.view.ViewGroup
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new ViewGroup.MarginLayoutParams(-1, -2);
    }

    @Override // android.view.ViewGroup
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attributeSet) {
        return new ViewGroup.MarginLayoutParams(getContext(), attributeSet);
    }

    @Override // android.view.ViewGroup, android.view.View
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mActionMenuPresenter != null) {
            this.mActionMenuPresenter.hideOverflowMenu();
            this.mActionMenuPresenter.hideSubMenus();
        }
    }

    @Override // android.support.v7.widget.AbsActionBarView, android.view.View
    public /* bridge */ /* synthetic */ boolean onHoverEvent(MotionEvent motionEvent) {
        return super.onHoverEvent(motionEvent);
    }

    @Override // android.view.View
    public void onInitializeAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        if (Build.VERSION.SDK_INT >= 14) {
            if (accessibilityEvent.getEventType() != 32) {
                super.onInitializeAccessibilityEvent(accessibilityEvent);
                return;
            }
            accessibilityEvent.setSource(this);
            accessibilityEvent.setClassName(getClass().getName());
            accessibilityEvent.setPackageName(getContext().getPackageName());
            accessibilityEvent.setContentDescription(this.mTitle);
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        boolean isLayoutRtl = ViewUtils.isLayoutRtl(this);
        int paddingRight = isLayoutRtl ? (i3 - i) - getPaddingRight() : getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingTop2 = ((i4 - i2) - getPaddingTop()) - getPaddingBottom();
        int i5 = paddingRight;
        if (this.mClose != null) {
            i5 = paddingRight;
            if (this.mClose.getVisibility() != 8) {
                ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) this.mClose.getLayoutParams();
                int i6 = isLayoutRtl ? marginLayoutParams.rightMargin : marginLayoutParams.leftMargin;
                int i7 = isLayoutRtl ? marginLayoutParams.leftMargin : marginLayoutParams.rightMargin;
                int next = next(paddingRight, i6, isLayoutRtl);
                i5 = next(next + positionChild(this.mClose, next, paddingTop, paddingTop2, isLayoutRtl), i7, isLayoutRtl);
            }
        }
        int i8 = i5;
        if (this.mTitleLayout != null) {
            i8 = i5;
            if (this.mCustomView == null) {
                i8 = i5;
                if (this.mTitleLayout.getVisibility() != 8) {
                    i8 = i5 + positionChild(this.mTitleLayout, i5, paddingTop, paddingTop2, isLayoutRtl);
                }
            }
        }
        if (this.mCustomView != null) {
            positionChild(this.mCustomView, i8, paddingTop, paddingTop2, isLayoutRtl);
        }
        int paddingLeft = isLayoutRtl ? getPaddingLeft() : (i3 - i) - getPaddingRight();
        if (this.mMenuView != null) {
            positionChild(this.mMenuView, paddingLeft, paddingTop, paddingTop2, !isLayoutRtl);
        }
    }

    @Override // android.view.View
    protected void onMeasure(int i, int i2) {
        if (View.MeasureSpec.getMode(i) != 1073741824) {
            throw new IllegalStateException(getClass().getSimpleName() + " can only be used with android:layout_width=\"match_parent\" (or fill_parent)");
        }
        if (View.MeasureSpec.getMode(i2) == 0) {
            throw new IllegalStateException(getClass().getSimpleName() + " can only be used with android:layout_height=\"wrap_content\"");
        }
        int size = View.MeasureSpec.getSize(i);
        int size2 = this.mContentHeight > 0 ? this.mContentHeight : View.MeasureSpec.getSize(i2);
        int paddingTop = getPaddingTop() + getPaddingBottom();
        int paddingLeft = (size - getPaddingLeft()) - getPaddingRight();
        int i3 = size2 - paddingTop;
        int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(i3, Integer.MIN_VALUE);
        int i4 = paddingLeft;
        if (this.mClose != null) {
            int measureChildView = measureChildView(this.mClose, paddingLeft, makeMeasureSpec, 0);
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) this.mClose.getLayoutParams();
            i4 = measureChildView - (marginLayoutParams.leftMargin + marginLayoutParams.rightMargin);
        }
        int i5 = i4;
        if (this.mMenuView != null) {
            i5 = i4;
            if (this.mMenuView.getParent() == this) {
                i5 = measureChildView(this.mMenuView, i4, makeMeasureSpec, 0);
            }
        }
        int i6 = i5;
        if (this.mTitleLayout != null) {
            i6 = i5;
            if (this.mCustomView == null) {
                if (this.mTitleOptional) {
                    this.mTitleLayout.measure(View.MeasureSpec.makeMeasureSpec(0, 0), makeMeasureSpec);
                    int measuredWidth = this.mTitleLayout.getMeasuredWidth();
                    boolean z = measuredWidth <= i5;
                    i6 = i5;
                    if (z) {
                        i6 = i5 - measuredWidth;
                    }
                    this.mTitleLayout.setVisibility(z ? 0 : 8);
                } else {
                    i6 = measureChildView(this.mTitleLayout, i5, makeMeasureSpec, 0);
                }
            }
        }
        if (this.mCustomView != null) {
            ViewGroup.LayoutParams layoutParams = this.mCustomView.getLayoutParams();
            int i7 = layoutParams.width != -2 ? 1073741824 : Integer.MIN_VALUE;
            if (layoutParams.width >= 0) {
                i6 = Math.min(layoutParams.width, i6);
            }
            int i8 = layoutParams.height != -2 ? 1073741824 : Integer.MIN_VALUE;
            if (layoutParams.height >= 0) {
                i3 = Math.min(layoutParams.height, i3);
            }
            this.mCustomView.measure(View.MeasureSpec.makeMeasureSpec(i6, i7), View.MeasureSpec.makeMeasureSpec(i3, i8));
        }
        if (this.mContentHeight > 0) {
            setMeasuredDimension(size, size2);
            return;
        }
        int i9 = 0;
        int childCount = getChildCount();
        int i10 = 0;
        while (i10 < childCount) {
            int measuredHeight = getChildAt(i10).getMeasuredHeight() + paddingTop;
            int i11 = i9;
            if (measuredHeight > i9) {
                i11 = measuredHeight;
            }
            i10++;
            i9 = i11;
        }
        setMeasuredDimension(size, i9);
    }

    @Override // android.support.v7.widget.AbsActionBarView, android.view.View
    public /* bridge */ /* synthetic */ boolean onTouchEvent(MotionEvent motionEvent) {
        return super.onTouchEvent(motionEvent);
    }

    @Override // android.support.v7.widget.AbsActionBarView
    public void setContentHeight(int i) {
        this.mContentHeight = i;
    }

    @Override // android.support.v7.widget.AbsActionBarView, android.view.View
    public /* bridge */ /* synthetic */ void setVisibility(int i) {
        super.setVisibility(i);
    }

    @Override // android.view.ViewGroup
    public boolean shouldDelayChildPressedState() {
        return false;
    }
}
