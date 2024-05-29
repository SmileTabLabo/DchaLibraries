package com.android.keyguard;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.view.ViewHierarchyEncoder;
import android.widget.FrameLayout;
import android.widget.ViewFlipper;
import com.android.internal.widget.LockPatternUtils;
/* loaded from: a.zip:com/android/keyguard/KeyguardSecurityViewFlipper.class */
public class KeyguardSecurityViewFlipper extends ViewFlipper implements KeyguardSecurityView {
    private Rect mTempRect;

    /* loaded from: a.zip:com/android/keyguard/KeyguardSecurityViewFlipper$LayoutParams.class */
    public static class LayoutParams extends FrameLayout.LayoutParams {
        @ViewDebug.ExportedProperty(category = "layout")
        public int maxHeight;
        @ViewDebug.ExportedProperty(category = "layout")
        public int maxWidth;

        public LayoutParams(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
            TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.KeyguardSecurityViewFlipper_Layout, 0, 0);
            this.maxWidth = obtainStyledAttributes.getDimensionPixelSize(R$styleable.KeyguardSecurityViewFlipper_Layout_layout_maxWidth, 0);
            this.maxHeight = obtainStyledAttributes.getDimensionPixelSize(R$styleable.KeyguardSecurityViewFlipper_Layout_layout_maxHeight, 0);
            obtainStyledAttributes.recycle();
        }

        public LayoutParams(ViewGroup.LayoutParams layoutParams) {
            super(layoutParams);
        }

        public LayoutParams(LayoutParams layoutParams) {
            super((FrameLayout.LayoutParams) layoutParams);
            this.maxWidth = layoutParams.maxWidth;
            this.maxHeight = layoutParams.maxHeight;
        }

        protected void encodeProperties(ViewHierarchyEncoder viewHierarchyEncoder) {
            super.encodeProperties(viewHierarchyEncoder);
            viewHierarchyEncoder.addProperty("layout:maxWidth", this.maxWidth);
            viewHierarchyEncoder.addProperty("layout:maxHeight", this.maxHeight);
        }
    }

    public KeyguardSecurityViewFlipper(Context context) {
        this(context, null);
    }

    public KeyguardSecurityViewFlipper(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mTempRect = new Rect();
    }

    private int makeChildMeasureSpec(int i, int i2) {
        int i3;
        int i4;
        switch (i2) {
            case -2:
                i3 = i;
                i4 = Integer.MIN_VALUE;
                break;
            case -1:
                i3 = i;
                i4 = 1073741824;
                break;
            default:
                i3 = Math.min(i, i2);
                i4 = 1073741824;
                break;
        }
        return View.MeasureSpec.makeMeasureSpec(i3, i4);
    }

    @Override // android.widget.FrameLayout, android.view.ViewGroup
    protected boolean checkLayoutParams(ViewGroup.LayoutParams layoutParams) {
        return layoutParams instanceof LayoutParams;
    }

    @Override // android.widget.FrameLayout, android.view.ViewGroup
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams layoutParams) {
        return layoutParams instanceof LayoutParams ? new LayoutParams((LayoutParams) layoutParams) : new LayoutParams(layoutParams);
    }

    @Override // android.widget.FrameLayout, android.view.ViewGroup
    public LayoutParams generateLayoutParams(AttributeSet attributeSet) {
        return new LayoutParams(getContext(), attributeSet);
    }

    KeyguardSecurityView getSecurityView() {
        View childAt = getChildAt(getDisplayedChild());
        if (childAt instanceof KeyguardSecurityView) {
            return (KeyguardSecurityView) childAt;
        }
        return null;
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public boolean needsInput() {
        KeyguardSecurityView securityView = getSecurityView();
        return securityView != null ? securityView.needsInput() : false;
    }

    @Override // android.widget.FrameLayout, android.view.View
    protected void onMeasure(int i, int i2) {
        int mode = View.MeasureSpec.getMode(i);
        int mode2 = View.MeasureSpec.getMode(i2);
        if (mode != Integer.MIN_VALUE) {
            Log.w("KeyguardSecurityViewFlipper", "onMeasure: widthSpec " + View.MeasureSpec.toString(i) + " should be AT_MOST");
        }
        if (mode2 != Integer.MIN_VALUE) {
            Log.w("KeyguardSecurityViewFlipper", "onMeasure: heightSpec " + View.MeasureSpec.toString(i2) + " should be AT_MOST");
        }
        int size = View.MeasureSpec.getSize(i);
        int size2 = View.MeasureSpec.getSize(i2);
        int i3 = size;
        int i4 = size2;
        int childCount = getChildCount();
        int i5 = 0;
        while (i5 < childCount) {
            LayoutParams layoutParams = (LayoutParams) getChildAt(i5).getLayoutParams();
            int i6 = i3;
            if (layoutParams.maxWidth > 0) {
                i6 = i3;
                if (layoutParams.maxWidth < i3) {
                    i6 = layoutParams.maxWidth;
                }
            }
            int i7 = i4;
            if (layoutParams.maxHeight > 0) {
                i7 = i4;
                if (layoutParams.maxHeight < i4) {
                    i7 = layoutParams.maxHeight;
                }
            }
            i5++;
            i4 = i7;
            i3 = i6;
        }
        int paddingLeft = getPaddingLeft() + getPaddingRight();
        int paddingTop = getPaddingTop() + getPaddingBottom();
        int max = Math.max(0, i3 - paddingLeft);
        int max2 = Math.max(0, i4 - paddingTop);
        int i8 = mode == 1073741824 ? size : 0;
        int i9 = mode2 == 1073741824 ? size2 : 0;
        int i10 = i8;
        for (int i11 = 0; i11 < childCount; i11++) {
            View childAt = getChildAt(i11);
            LayoutParams layoutParams2 = (LayoutParams) childAt.getLayoutParams();
            childAt.measure(makeChildMeasureSpec(max, layoutParams2.width), makeChildMeasureSpec(max2, layoutParams2.height));
            i10 = Math.max(i10, Math.min(childAt.getMeasuredWidth(), size - paddingLeft));
            i9 = Math.max(i9, Math.min(childAt.getMeasuredHeight(), size2 - paddingTop));
        }
        setMeasuredDimension(i10 + paddingLeft, i9 + paddingTop);
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void onPause() {
        KeyguardSecurityView securityView = getSecurityView();
        if (securityView != null) {
            securityView.onPause();
        }
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void onResume(int i) {
        KeyguardSecurityView securityView = getSecurityView();
        if (securityView != null) {
            securityView.onResume(i);
        }
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        boolean onTouchEvent = super.onTouchEvent(motionEvent);
        this.mTempRect.set(0, 0, 0, 0);
        int i = 0;
        while (i < getChildCount()) {
            View childAt = getChildAt(i);
            boolean z = onTouchEvent;
            if (childAt.getVisibility() == 0) {
                offsetRectIntoDescendantCoords(childAt, this.mTempRect);
                motionEvent.offsetLocation(this.mTempRect.left, this.mTempRect.top);
                if (childAt.dispatchTouchEvent(motionEvent)) {
                    onTouchEvent = true;
                }
                motionEvent.offsetLocation(-this.mTempRect.left, -this.mTempRect.top);
                z = onTouchEvent;
            }
            i++;
            onTouchEvent = z;
        }
        return onTouchEvent;
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void setKeyguardCallback(KeyguardSecurityCallback keyguardSecurityCallback) {
        KeyguardSecurityView securityView = getSecurityView();
        if (securityView != null) {
            securityView.setKeyguardCallback(keyguardSecurityCallback);
        }
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void setLockPatternUtils(LockPatternUtils lockPatternUtils) {
        KeyguardSecurityView securityView = getSecurityView();
        if (securityView != null) {
            securityView.setLockPatternUtils(lockPatternUtils);
        }
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void showMessage(String str, int i) {
        KeyguardSecurityView securityView = getSecurityView();
        if (securityView != null) {
            securityView.showMessage(str, i);
        }
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void showPromptReason(int i) {
        KeyguardSecurityView securityView = getSecurityView();
        if (securityView != null) {
            securityView.showPromptReason(i);
        }
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void startAppearAnimation() {
        KeyguardSecurityView securityView = getSecurityView();
        if (securityView != null) {
            securityView.startAppearAnimation();
        }
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public boolean startDisappearAnimation(Runnable runnable) {
        KeyguardSecurityView securityView = getSecurityView();
        if (securityView != null) {
            return securityView.startDisappearAnimation(runnable);
        }
        return false;
    }
}
