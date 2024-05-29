package com.android.launcher3;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
/* loaded from: a.zip:com/android/launcher3/InsettableFrameLayout.class */
public class InsettableFrameLayout extends FrameLayout implements ViewGroup.OnHierarchyChangeListener, Insettable {
    protected Rect mInsets;

    /* loaded from: a.zip:com/android/launcher3/InsettableFrameLayout$LayoutParams.class */
    public static class LayoutParams extends FrameLayout.LayoutParams {
        boolean ignoreInsets;

        public LayoutParams(int i, int i2) {
            super(i, i2);
            this.ignoreInsets = false;
        }

        public LayoutParams(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
            this.ignoreInsets = false;
            TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.InsettableFrameLayout_Layout);
            this.ignoreInsets = obtainStyledAttributes.getBoolean(0, false);
            obtainStyledAttributes.recycle();
        }

        public LayoutParams(ViewGroup.LayoutParams layoutParams) {
            super(layoutParams);
            this.ignoreInsets = false;
        }
    }

    public InsettableFrameLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mInsets = new Rect();
        setOnHierarchyChangeListener(this);
    }

    @Override // android.widget.FrameLayout, android.view.ViewGroup
    protected boolean checkLayoutParams(ViewGroup.LayoutParams layoutParams) {
        return layoutParams instanceof LayoutParams;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.widget.FrameLayout, android.view.ViewGroup
    public LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(-2, -2);
    }

    @Override // android.widget.FrameLayout, android.view.ViewGroup
    public LayoutParams generateLayoutParams(AttributeSet attributeSet) {
        return new LayoutParams(getContext(), attributeSet);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.widget.FrameLayout, android.view.ViewGroup
    public LayoutParams generateLayoutParams(ViewGroup.LayoutParams layoutParams) {
        return new LayoutParams(layoutParams);
    }

    public void onChildViewAdded(View view, View view2) {
        setFrameLayoutChildInsets(view2, this.mInsets, new Rect());
    }

    public void onChildViewRemoved(View view, View view2) {
    }

    public void setFrameLayoutChildInsets(View view, Rect rect, Rect rect2) {
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        if (view instanceof Insettable) {
            ((Insettable) view).setInsets(rect);
        } else if (!layoutParams.ignoreInsets) {
            layoutParams.topMargin += rect.top - rect2.top;
            layoutParams.leftMargin += rect.left - rect2.left;
            layoutParams.rightMargin += rect.right - rect2.right;
            layoutParams.bottomMargin += rect.bottom - rect2.bottom;
        }
        view.setLayoutParams(layoutParams);
    }

    @Override // com.android.launcher3.Insettable
    public void setInsets(Rect rect) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            setFrameLayoutChildInsets(getChildAt(i), rect, this.mInsets);
        }
        this.mInsets.set(rect);
    }
}
