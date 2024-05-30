package com.android.settings.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v7.appcompat.R;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.RemotableViewMethod;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.view.ViewHierarchyEncoder;
import com.android.settingslib.wifi.AccessPoint;
/* loaded from: classes.dex */
public class MatchParentShrinkingLinearLayout extends ViewGroup {
    @ViewDebug.ExportedProperty(category = "layout")
    private boolean mBaselineAligned;
    @ViewDebug.ExportedProperty(category = "layout")
    private int mBaselineAlignedChildIndex;
    @ViewDebug.ExportedProperty(category = "measurement")
    private int mBaselineChildTop;
    private Drawable mDivider;
    private int mDividerHeight;
    private int mDividerPadding;
    private int mDividerWidth;
    @ViewDebug.ExportedProperty(category = "measurement", flagMapping = {@ViewDebug.FlagToString(equals = -1, mask = -1, name = "NONE"), @ViewDebug.FlagToString(equals = 0, mask = 0, name = "NONE"), @ViewDebug.FlagToString(equals = 48, mask = 48, name = "TOP"), @ViewDebug.FlagToString(equals = R.styleable.AppCompatTheme_panelBackground, mask = R.styleable.AppCompatTheme_panelBackground, name = "BOTTOM"), @ViewDebug.FlagToString(equals = 3, mask = 3, name = "LEFT"), @ViewDebug.FlagToString(equals = 5, mask = 5, name = "RIGHT"), @ViewDebug.FlagToString(equals = 8388611, mask = 8388611, name = "START"), @ViewDebug.FlagToString(equals = 8388613, mask = 8388613, name = "END"), @ViewDebug.FlagToString(equals = 16, mask = 16, name = "CENTER_VERTICAL"), @ViewDebug.FlagToString(equals = R.styleable.AppCompatTheme_windowActionBarOverlay, mask = R.styleable.AppCompatTheme_windowActionBarOverlay, name = "FILL_VERTICAL"), @ViewDebug.FlagToString(equals = 1, mask = 1, name = "CENTER_HORIZONTAL"), @ViewDebug.FlagToString(equals = 7, mask = 7, name = "FILL_HORIZONTAL"), @ViewDebug.FlagToString(equals = 17, mask = 17, name = "CENTER"), @ViewDebug.FlagToString(equals = R.styleable.AppCompatTheme_windowMinWidthMinor, mask = R.styleable.AppCompatTheme_windowMinWidthMinor, name = "FILL"), @ViewDebug.FlagToString(equals = 8388608, mask = 8388608, name = "RELATIVE")}, formatToHexString = true)
    private int mGravity;
    private int mLayoutDirection;
    private int[] mMaxAscent;
    private int[] mMaxDescent;
    @ViewDebug.ExportedProperty(category = "measurement")
    private int mOrientation;
    private int mShowDividers;
    @ViewDebug.ExportedProperty(category = "measurement")
    private int mTotalLength;
    @ViewDebug.ExportedProperty(category = "layout")
    private boolean mUseLargestChild;
    @ViewDebug.ExportedProperty(category = "layout")
    private float mWeightSum;

    public MatchParentShrinkingLinearLayout(Context context) {
        this(context, null);
    }

    public MatchParentShrinkingLinearLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public MatchParentShrinkingLinearLayout(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public MatchParentShrinkingLinearLayout(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mBaselineAligned = true;
        this.mBaselineAlignedChildIndex = -1;
        this.mBaselineChildTop = 0;
        this.mGravity = 8388659;
        this.mLayoutDirection = -1;
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, com.android.internal.R.styleable.LinearLayout, i, i2);
        int i3 = obtainStyledAttributes.getInt(1, -1);
        if (i3 >= 0) {
            setOrientation(i3);
        }
        int i4 = obtainStyledAttributes.getInt(0, -1);
        if (i4 >= 0) {
            setGravity(i4);
        }
        boolean z = obtainStyledAttributes.getBoolean(2, true);
        if (!z) {
            setBaselineAligned(z);
        }
        this.mWeightSum = obtainStyledAttributes.getFloat(4, -1.0f);
        this.mBaselineAlignedChildIndex = obtainStyledAttributes.getInt(3, -1);
        this.mUseLargestChild = obtainStyledAttributes.getBoolean(6, false);
        setDividerDrawable(obtainStyledAttributes.getDrawable(5));
        this.mShowDividers = obtainStyledAttributes.getInt(7, 0);
        this.mDividerPadding = obtainStyledAttributes.getDimensionPixelSize(8, 0);
        obtainStyledAttributes.recycle();
    }

    public void setShowDividers(int i) {
        if (i != this.mShowDividers) {
            requestLayout();
        }
        this.mShowDividers = i;
    }

    @Override // android.view.ViewGroup
    public boolean shouldDelayChildPressedState() {
        return false;
    }

    public int getShowDividers() {
        return this.mShowDividers;
    }

    public Drawable getDividerDrawable() {
        return this.mDivider;
    }

    public void setDividerDrawable(Drawable drawable) {
        if (drawable == this.mDivider) {
            return;
        }
        this.mDivider = drawable;
        if (drawable != null) {
            this.mDividerWidth = drawable.getIntrinsicWidth();
            this.mDividerHeight = drawable.getIntrinsicHeight();
        } else {
            this.mDividerWidth = 0;
            this.mDividerHeight = 0;
        }
        setWillNotDraw(drawable == null);
        requestLayout();
    }

    public void setDividerPadding(int i) {
        this.mDividerPadding = i;
    }

    public int getDividerPadding() {
        return this.mDividerPadding;
    }

    public int getDividerWidth() {
        return this.mDividerWidth;
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        if (this.mDivider == null) {
            return;
        }
        if (this.mOrientation == 1) {
            drawDividersVertical(canvas);
        } else {
            drawDividersHorizontal(canvas);
        }
    }

    void drawDividersVertical(Canvas canvas) {
        int bottom;
        int virtualChildCount = getVirtualChildCount();
        for (int i = 0; i < virtualChildCount; i++) {
            View virtualChildAt = getVirtualChildAt(i);
            if (virtualChildAt != null && virtualChildAt.getVisibility() != 8 && hasDividerBeforeChildAt(i)) {
                drawHorizontalDivider(canvas, (virtualChildAt.getTop() - ((LayoutParams) virtualChildAt.getLayoutParams()).topMargin) - this.mDividerHeight);
            }
        }
        if (hasDividerBeforeChildAt(virtualChildCount)) {
            View virtualChildAt2 = getVirtualChildAt(virtualChildCount - 1);
            if (virtualChildAt2 == null) {
                bottom = (getHeight() - getPaddingBottom()) - this.mDividerHeight;
            } else {
                bottom = virtualChildAt2.getBottom() + ((LayoutParams) virtualChildAt2.getLayoutParams()).bottomMargin;
            }
            drawHorizontalDivider(canvas, bottom);
        }
    }

    void drawDividersHorizontal(Canvas canvas) {
        int right;
        int left;
        int virtualChildCount = getVirtualChildCount();
        boolean isLayoutRtl = isLayoutRtl();
        for (int i = 0; i < virtualChildCount; i++) {
            View virtualChildAt = getVirtualChildAt(i);
            if (virtualChildAt != null && virtualChildAt.getVisibility() != 8 && hasDividerBeforeChildAt(i)) {
                LayoutParams layoutParams = (LayoutParams) virtualChildAt.getLayoutParams();
                if (isLayoutRtl) {
                    left = virtualChildAt.getRight() + layoutParams.rightMargin;
                } else {
                    left = (virtualChildAt.getLeft() - layoutParams.leftMargin) - this.mDividerWidth;
                }
                drawVerticalDivider(canvas, left);
            }
        }
        if (hasDividerBeforeChildAt(virtualChildCount)) {
            View virtualChildAt2 = getVirtualChildAt(virtualChildCount - 1);
            if (virtualChildAt2 == null) {
                if (isLayoutRtl) {
                    right = getPaddingLeft();
                } else {
                    right = (getWidth() - getPaddingRight()) - this.mDividerWidth;
                }
            } else {
                LayoutParams layoutParams2 = (LayoutParams) virtualChildAt2.getLayoutParams();
                if (isLayoutRtl) {
                    right = (virtualChildAt2.getLeft() - layoutParams2.leftMargin) - this.mDividerWidth;
                } else {
                    right = virtualChildAt2.getRight() + layoutParams2.rightMargin;
                }
            }
            drawVerticalDivider(canvas, right);
        }
    }

    void drawHorizontalDivider(Canvas canvas, int i) {
        this.mDivider.setBounds(getPaddingLeft() + this.mDividerPadding, i, (getWidth() - getPaddingRight()) - this.mDividerPadding, this.mDividerHeight + i);
        this.mDivider.draw(canvas);
    }

    void drawVerticalDivider(Canvas canvas, int i) {
        this.mDivider.setBounds(i, getPaddingTop() + this.mDividerPadding, this.mDividerWidth + i, (getHeight() - getPaddingBottom()) - this.mDividerPadding);
        this.mDivider.draw(canvas);
    }

    @RemotableViewMethod
    public void setBaselineAligned(boolean z) {
        this.mBaselineAligned = z;
    }

    @RemotableViewMethod
    public void setMeasureWithLargestChildEnabled(boolean z) {
        this.mUseLargestChild = z;
    }

    @Override // android.view.View
    public int getBaseline() {
        int i;
        if (this.mBaselineAlignedChildIndex < 0) {
            return super.getBaseline();
        }
        if (getChildCount() <= this.mBaselineAlignedChildIndex) {
            throw new RuntimeException("mBaselineAlignedChildIndex of LinearLayout set to an index that is out of bounds.");
        }
        View childAt = getChildAt(this.mBaselineAlignedChildIndex);
        int baseline = childAt.getBaseline();
        if (baseline == -1) {
            if (this.mBaselineAlignedChildIndex == 0) {
                return -1;
            }
            throw new RuntimeException("mBaselineAlignedChildIndex of LinearLayout points to a View that doesn't know how to get its baseline.");
        }
        int i2 = this.mBaselineChildTop;
        if (this.mOrientation == 1 && (i = this.mGravity & R.styleable.AppCompatTheme_windowActionBarOverlay) != 48) {
            if (i == 16) {
                i2 += ((((this.mBottom - this.mTop) - this.mPaddingTop) - this.mPaddingBottom) - this.mTotalLength) / 2;
            } else if (i == 80) {
                i2 = ((this.mBottom - this.mTop) - this.mPaddingBottom) - this.mTotalLength;
            }
        }
        return i2 + ((LayoutParams) childAt.getLayoutParams()).topMargin + baseline;
    }

    public int getBaselineAlignedChildIndex() {
        return this.mBaselineAlignedChildIndex;
    }

    @RemotableViewMethod
    public void setBaselineAlignedChildIndex(int i) {
        if (i < 0 || i >= getChildCount()) {
            throw new IllegalArgumentException("base aligned child index out of range (0, " + getChildCount() + ")");
        }
        this.mBaselineAlignedChildIndex = i;
    }

    View getVirtualChildAt(int i) {
        return getChildAt(i);
    }

    int getVirtualChildCount() {
        return getChildCount();
    }

    public float getWeightSum() {
        return this.mWeightSum;
    }

    @RemotableViewMethod
    public void setWeightSum(float f) {
        this.mWeightSum = Math.max(0.0f, f);
    }

    @Override // android.view.View
    protected void onMeasure(int i, int i2) {
        if (this.mOrientation == 1) {
            measureVertical(i, i2);
        } else {
            measureHorizontal(i, i2);
        }
    }

    protected boolean hasDividerBeforeChildAt(int i) {
        if (i == 0) {
            return (this.mShowDividers & 1) != 0;
        } else if (i == getChildCount()) {
            return (this.mShowDividers & 4) != 0;
        } else if ((this.mShowDividers & 2) == 0) {
            return false;
        } else {
            for (int i2 = i - 1; i2 >= 0; i2--) {
                if (getChildAt(i2).getVisibility() != 8) {
                    return true;
                }
            }
            return false;
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:162:0x0393  */
    /* JADX WARN: Removed duplicated region for block: B:64:0x0188  */
    /* JADX WARN: Removed duplicated region for block: B:68:0x0195  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    void measureVertical(int i, int i2) {
        int i3;
        int i4;
        int i5;
        int i6;
        float f;
        int i7;
        int i8;
        boolean z;
        boolean z2;
        int i9;
        int i10;
        int i11;
        int i12;
        View view;
        int i13;
        int i14;
        int i15;
        LayoutParams layoutParams;
        int i16;
        int max;
        int i17;
        boolean z3;
        int max2;
        int i18;
        int i19 = i;
        int i20 = i2;
        int i21 = 0;
        this.mTotalLength = 0;
        int virtualChildCount = getVirtualChildCount();
        int mode = View.MeasureSpec.getMode(i);
        int mode2 = View.MeasureSpec.getMode(i2);
        int i22 = this.mBaselineAlignedChildIndex;
        boolean z4 = this.mUseLargestChild;
        int i23 = 0;
        int i24 = 0;
        int i25 = 0;
        int i26 = 0;
        boolean z5 = false;
        boolean z6 = false;
        float f2 = 0.0f;
        boolean z7 = true;
        int i27 = AccessPoint.UNREACHABLE_RSSI;
        while (true) {
            int i28 = 8;
            int i29 = i25;
            if (i26 >= virtualChildCount) {
                int i30 = i24;
                int i31 = i21;
                int i32 = virtualChildCount;
                int i33 = mode2;
                int i34 = i29;
                int i35 = i23;
                int i36 = i27;
                if (this.mTotalLength > 0) {
                    i3 = i32;
                    if (hasDividerBeforeChildAt(i3)) {
                        this.mTotalLength += this.mDividerHeight;
                    }
                } else {
                    i3 = i32;
                }
                if (z4) {
                    i4 = i33;
                    if (i4 == Integer.MIN_VALUE || i4 == 0) {
                        this.mTotalLength = 0;
                        int i37 = 0;
                        while (i37 < i3) {
                            View virtualChildAt = getVirtualChildAt(i37);
                            if (virtualChildAt == null) {
                                this.mTotalLength += measureNullChild(i37);
                            } else if (virtualChildAt.getVisibility() == i28) {
                                i37 += getChildrenSkipCount(virtualChildAt, i37);
                            } else {
                                LayoutParams layoutParams2 = (LayoutParams) virtualChildAt.getLayoutParams();
                                int i38 = this.mTotalLength;
                                this.mTotalLength = Math.max(i38, i38 + i36 + layoutParams2.topMargin + layoutParams2.bottomMargin + getNextLocationOffset(virtualChildAt));
                            }
                            i37++;
                            i28 = 8;
                        }
                    }
                } else {
                    i4 = i33;
                }
                this.mTotalLength += this.mPaddingTop + this.mPaddingBottom;
                int resolveSizeAndState = resolveSizeAndState(Math.max(this.mTotalLength, getSuggestedMinimumHeight()), i2, 0);
                int i39 = (16777215 & resolveSizeAndState) - this.mTotalLength;
                if (z5 || (i39 != 0 && f2 > 0.0f)) {
                    if (this.mWeightSum > 0.0f) {
                        f2 = this.mWeightSum;
                    }
                    this.mTotalLength = 0;
                    float f3 = f2;
                    int i40 = 0;
                    int i41 = i31;
                    int i42 = i39;
                    while (i40 < i3) {
                        View virtualChildAt2 = getVirtualChildAt(i40);
                        if (virtualChildAt2.getVisibility() == 8) {
                            f = f3;
                        } else {
                            LayoutParams layoutParams3 = (LayoutParams) virtualChildAt2.getLayoutParams();
                            float f4 = layoutParams3.weight;
                            if (f4 <= 0.0f || i42 <= 0) {
                                float f5 = f3;
                                if (i42 < 0) {
                                    f = f5;
                                    if (layoutParams3.height == -1) {
                                        int childMeasureSpec = getChildMeasureSpec(i, this.mPaddingLeft + this.mPaddingRight + layoutParams3.leftMargin + layoutParams3.rightMargin, layoutParams3.width);
                                        int measuredHeight = virtualChildAt2.getMeasuredHeight() + i42;
                                        if (measuredHeight < 0) {
                                            measuredHeight = 0;
                                        }
                                        virtualChildAt2.measure(childMeasureSpec, View.MeasureSpec.makeMeasureSpec(measuredHeight, 1073741824));
                                        i41 = combineMeasuredStates(i41, virtualChildAt2.getMeasuredState() & (-256));
                                        i7 = i42 - (measuredHeight - virtualChildAt2.getMeasuredHeight());
                                    }
                                } else {
                                    f = f5;
                                }
                                i7 = i42;
                            } else {
                                int i43 = (int) ((i42 * f4) / f3);
                                float f6 = f3 - f4;
                                i7 = i42 - i43;
                                int childMeasureSpec2 = getChildMeasureSpec(i, this.mPaddingLeft + this.mPaddingRight + layoutParams3.leftMargin + layoutParams3.rightMargin, layoutParams3.width);
                                if (layoutParams3.height == 0 && i4 == 1073741824) {
                                    if (i43 <= 0) {
                                        i43 = 0;
                                    }
                                    virtualChildAt2.measure(childMeasureSpec2, View.MeasureSpec.makeMeasureSpec(i43, 1073741824));
                                } else {
                                    int measuredHeight2 = virtualChildAt2.getMeasuredHeight() + i43;
                                    if (measuredHeight2 < 0) {
                                        measuredHeight2 = 0;
                                    }
                                    virtualChildAt2.measure(childMeasureSpec2, View.MeasureSpec.makeMeasureSpec(measuredHeight2, 1073741824));
                                }
                                i41 = combineMeasuredStates(i41, virtualChildAt2.getMeasuredState() & (-256));
                                f = f6;
                            }
                            int i44 = layoutParams3.leftMargin + layoutParams3.rightMargin;
                            int measuredWidth = virtualChildAt2.getMeasuredWidth() + i44;
                            i35 = Math.max(i35, measuredWidth);
                            if (mode != 1073741824) {
                                i8 = i44;
                                if (layoutParams3.width == -1) {
                                    z = true;
                                    if (z) {
                                        measuredWidth = i8;
                                    }
                                    int max3 = Math.max(i34, measuredWidth);
                                    if (z7 && layoutParams3.width == -1) {
                                        z2 = true;
                                        int i45 = this.mTotalLength;
                                        this.mTotalLength = Math.max(i45, i45 + virtualChildAt2.getMeasuredHeight() + layoutParams3.topMargin + layoutParams3.bottomMargin + getNextLocationOffset(virtualChildAt2));
                                        z7 = z2;
                                        i42 = i7;
                                        i34 = max3;
                                    }
                                    z2 = false;
                                    int i452 = this.mTotalLength;
                                    this.mTotalLength = Math.max(i452, i452 + virtualChildAt2.getMeasuredHeight() + layoutParams3.topMargin + layoutParams3.bottomMargin + getNextLocationOffset(virtualChildAt2));
                                    z7 = z2;
                                    i42 = i7;
                                    i34 = max3;
                                }
                            } else {
                                i8 = i44;
                            }
                            z = false;
                            if (z) {
                            }
                            int max32 = Math.max(i34, measuredWidth);
                            if (z7) {
                                z2 = true;
                                int i4522 = this.mTotalLength;
                                this.mTotalLength = Math.max(i4522, i4522 + virtualChildAt2.getMeasuredHeight() + layoutParams3.topMargin + layoutParams3.bottomMargin + getNextLocationOffset(virtualChildAt2));
                                z7 = z2;
                                i42 = i7;
                                i34 = max32;
                            }
                            z2 = false;
                            int i45222 = this.mTotalLength;
                            this.mTotalLength = Math.max(i45222, i45222 + virtualChildAt2.getMeasuredHeight() + layoutParams3.topMargin + layoutParams3.bottomMargin + getNextLocationOffset(virtualChildAt2));
                            z7 = z2;
                            i42 = i7;
                            i34 = max32;
                        }
                        i40++;
                        f3 = f;
                    }
                    i5 = i;
                    this.mTotalLength += this.mPaddingTop + this.mPaddingBottom;
                    i6 = i34;
                    i31 = i41;
                } else {
                    i6 = Math.max(i34, i30);
                    if (z4 && i4 != 1073741824) {
                        for (int i46 = 0; i46 < i3; i46++) {
                            View virtualChildAt3 = getVirtualChildAt(i46);
                            if (virtualChildAt3 != null && virtualChildAt3.getVisibility() != 8 && ((LayoutParams) virtualChildAt3.getLayoutParams()).weight > 0.0f) {
                                virtualChildAt3.measure(View.MeasureSpec.makeMeasureSpec(virtualChildAt3.getMeasuredWidth(), 1073741824), View.MeasureSpec.makeMeasureSpec(i36, 1073741824));
                            }
                        }
                    }
                    i5 = i;
                }
                if (!z7 && mode != 1073741824) {
                    i35 = i6;
                }
                setMeasuredDimension(resolveSizeAndState(Math.max(i35 + this.mPaddingLeft + this.mPaddingRight, getSuggestedMinimumWidth()), i5, i31), resolveSizeAndState);
                if (z6) {
                    forceUniformWidth(i3, i2);
                    return;
                }
                return;
            }
            View virtualChildAt4 = getVirtualChildAt(i26);
            if (virtualChildAt4 == null) {
                this.mTotalLength += measureNullChild(i26);
                i18 = i21;
                i12 = virtualChildCount;
                i10 = mode2;
                i25 = i29;
            } else {
                int i47 = i23;
                if (virtualChildAt4.getVisibility() == 8) {
                    i26 += getChildrenSkipCount(virtualChildAt4, i26);
                    i18 = i21;
                    i12 = virtualChildCount;
                    i10 = mode2;
                    i25 = i29;
                    i23 = i47;
                } else {
                    if (hasDividerBeforeChildAt(i26)) {
                        this.mTotalLength += this.mDividerHeight;
                    }
                    LayoutParams layoutParams4 = (LayoutParams) virtualChildAt4.getLayoutParams();
                    float f7 = f2 + layoutParams4.weight;
                    if (mode2 == 1073741824 && layoutParams4.height == 0 && layoutParams4.weight > 0.0f) {
                        int i48 = this.mTotalLength;
                        max = i27;
                        this.mTotalLength = Math.max(i48, layoutParams4.topMargin + i48 + layoutParams4.bottomMargin);
                        i13 = i24;
                        view = virtualChildAt4;
                        layoutParams = layoutParams4;
                        i16 = i21;
                        i12 = virtualChildCount;
                        i10 = mode2;
                        z5 = true;
                        i14 = i29;
                        i11 = i47;
                        i15 = i26;
                    } else {
                        int i49 = i27;
                        if (layoutParams4.height != 0 || layoutParams4.weight <= 0.0f) {
                            i9 = AccessPoint.UNREACHABLE_RSSI;
                        } else {
                            layoutParams4.height = -2;
                            i9 = 0;
                        }
                        i10 = mode2;
                        i11 = i47;
                        int i50 = i9;
                        int i51 = i26;
                        i12 = virtualChildCount;
                        int i52 = i24;
                        int i53 = i19;
                        view = virtualChildAt4;
                        i13 = i52;
                        i14 = i29;
                        i15 = i26;
                        int i54 = i20;
                        layoutParams = layoutParams4;
                        i16 = i21;
                        measureChildBeforeLayout(virtualChildAt4, i51, i53, 0, i54, f7 == 0.0f ? this.mTotalLength : 0);
                        if (i50 != Integer.MIN_VALUE) {
                            layoutParams.height = i50;
                        }
                        int measuredHeight3 = view.getMeasuredHeight();
                        int i55 = this.mTotalLength;
                        this.mTotalLength = Math.max(i55, i55 + measuredHeight3 + layoutParams.topMargin + layoutParams.bottomMargin + getNextLocationOffset(view));
                        max = z4 ? Math.max(measuredHeight3, i49) : i49;
                    }
                    if (i22 >= 0 && i22 == i15 + 1) {
                        this.mBaselineChildTop = this.mTotalLength;
                    }
                    if (i15 < i22 && layoutParams.weight > 0.0f) {
                        throw new RuntimeException("A child of LinearLayout with index less than mBaselineAlignedChildIndex has weight > 0, which won't work.  Either remove the weight, or don't set mBaselineAlignedChildIndex.");
                    }
                    if (mode != 1073741824) {
                        i17 = -1;
                        if (layoutParams.width == -1) {
                            z3 = true;
                            z6 = true;
                            int i56 = layoutParams.leftMargin + layoutParams.rightMargin;
                            int measuredWidth2 = view.getMeasuredWidth() + i56;
                            int max4 = Math.max(i11, measuredWidth2);
                            int combineMeasuredStates = combineMeasuredStates(i16, view.getMeasuredState());
                            boolean z8 = !z7 && layoutParams.width == i17;
                            if (layoutParams.weight <= 0.0f) {
                                if (!z3) {
                                    i56 = measuredWidth2;
                                }
                                i24 = Math.max(i13, i56);
                                max2 = i14;
                            } else {
                                int i57 = i13;
                                if (!z3) {
                                    i56 = measuredWidth2;
                                }
                                max2 = Math.max(i14, i56);
                                i24 = i57;
                            }
                            z7 = z8;
                            i18 = combineMeasuredStates;
                            i27 = max;
                            i26 = getChildrenSkipCount(view, i15) + i15;
                            i23 = max4;
                            i25 = max2;
                            f2 = f7;
                            i26++;
                            i21 = i18;
                            mode2 = i10;
                            virtualChildCount = i12;
                            i19 = i;
                            i20 = i2;
                        }
                    } else {
                        i17 = -1;
                    }
                    z3 = false;
                    int i562 = layoutParams.leftMargin + layoutParams.rightMargin;
                    int measuredWidth22 = view.getMeasuredWidth() + i562;
                    int max42 = Math.max(i11, measuredWidth22);
                    int combineMeasuredStates2 = combineMeasuredStates(i16, view.getMeasuredState());
                    if (z7) {
                    }
                    if (layoutParams.weight <= 0.0f) {
                    }
                    z7 = z8;
                    i18 = combineMeasuredStates2;
                    i27 = max;
                    i26 = getChildrenSkipCount(view, i15) + i15;
                    i23 = max42;
                    i25 = max2;
                    f2 = f7;
                    i26++;
                    i21 = i18;
                    mode2 = i10;
                    virtualChildCount = i12;
                    i19 = i;
                    i20 = i2;
                }
            }
            i26++;
            i21 = i18;
            mode2 = i10;
            virtualChildCount = i12;
            i19 = i;
            i20 = i2;
        }
    }

    private void forceUniformWidth(int i, int i2) {
        int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(getMeasuredWidth(), 1073741824);
        for (int i3 = 0; i3 < i; i3++) {
            View virtualChildAt = getVirtualChildAt(i3);
            if (virtualChildAt.getVisibility() != 8) {
                LayoutParams layoutParams = (LayoutParams) virtualChildAt.getLayoutParams();
                if (layoutParams.width == -1) {
                    int i4 = layoutParams.height;
                    layoutParams.height = virtualChildAt.getMeasuredHeight();
                    measureChildWithMargins(virtualChildAt, makeMeasureSpec, 0, i2, 0);
                    layoutParams.height = i4;
                }
            }
        }
    }

    void measureHorizontal(int i, int i2) {
        throw new IllegalStateException("horizontal mode not supported.");
    }

    int getChildrenSkipCount(View view, int i) {
        return 0;
    }

    int measureNullChild(int i) {
        return 0;
    }

    void measureChildBeforeLayout(View view, int i, int i2, int i3, int i4, int i5) {
        measureChildWithMargins(view, i2, i3, i4, i5);
    }

    int getLocationOffset(View view) {
        return 0;
    }

    int getNextLocationOffset(View view) {
        return 0;
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        if (this.mOrientation == 1) {
            layoutVertical(i, i2, i3, i4);
        } else {
            layoutHorizontal(i, i2, i3, i4);
        }
    }

    void layoutVertical(int i, int i2, int i3, int i4) {
        int i5;
        int i6;
        int i7 = this.mPaddingLeft;
        int i8 = i3 - i;
        int i9 = i8 - this.mPaddingRight;
        int i10 = (i8 - i7) - this.mPaddingRight;
        int virtualChildCount = getVirtualChildCount();
        int i11 = this.mGravity & R.styleable.AppCompatTheme_windowActionBarOverlay;
        int i12 = this.mGravity & 8388615;
        if (i11 == 16) {
            i5 = (((i4 - i2) - this.mTotalLength) / 2) + this.mPaddingTop;
        } else if (i11 == 80) {
            i5 = ((this.mPaddingTop + i4) - i2) - this.mTotalLength;
        } else {
            i5 = this.mPaddingTop;
        }
        int i13 = 0;
        while (i13 < virtualChildCount) {
            View virtualChildAt = getVirtualChildAt(i13);
            if (virtualChildAt == null) {
                i5 += measureNullChild(i13);
            } else if (virtualChildAt.getVisibility() != 8) {
                int measuredWidth = virtualChildAt.getMeasuredWidth();
                int measuredHeight = virtualChildAt.getMeasuredHeight();
                LayoutParams layoutParams = (LayoutParams) virtualChildAt.getLayoutParams();
                int i14 = layoutParams.gravity;
                if (i14 < 0) {
                    i14 = i12;
                }
                int absoluteGravity = Gravity.getAbsoluteGravity(i14, getLayoutDirection()) & 7;
                if (absoluteGravity == 1) {
                    i6 = ((((i10 - measuredWidth) / 2) + i7) + layoutParams.leftMargin) - layoutParams.rightMargin;
                } else if (absoluteGravity == 5) {
                    i6 = (i9 - measuredWidth) - layoutParams.rightMargin;
                } else {
                    i6 = layoutParams.leftMargin + i7;
                }
                int i15 = i6;
                if (hasDividerBeforeChildAt(i13)) {
                    i5 += this.mDividerHeight;
                }
                int i16 = i5 + layoutParams.topMargin;
                setChildFrame(virtualChildAt, i15, i16 + getLocationOffset(virtualChildAt), measuredWidth, measuredHeight);
                i13 += getChildrenSkipCount(virtualChildAt, i13);
                i5 = i16 + measuredHeight + layoutParams.bottomMargin + getNextLocationOffset(virtualChildAt);
            }
            i13++;
        }
    }

    @Override // android.view.View
    public void onRtlPropertiesChanged(int i) {
        super.onRtlPropertiesChanged(i);
        if (i != this.mLayoutDirection) {
            this.mLayoutDirection = i;
            if (this.mOrientation == 0) {
                requestLayout();
            }
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:30:0x00b1  */
    /* JADX WARN: Removed duplicated region for block: B:33:0x00ba  */
    /* JADX WARN: Removed duplicated region for block: B:49:0x00f4  */
    /* JADX WARN: Removed duplicated region for block: B:52:0x0109  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    void layoutHorizontal(int i, int i2, int i3, int i4) {
        int i5;
        int i6;
        int i7;
        int i8;
        int i9;
        boolean z;
        int i10;
        int i11;
        int i12;
        int i13;
        int i14;
        boolean isLayoutRtl = isLayoutRtl();
        int i15 = this.mPaddingTop;
        int i16 = i4 - i2;
        int i17 = i16 - this.mPaddingBottom;
        int i18 = (i16 - i15) - this.mPaddingBottom;
        int virtualChildCount = getVirtualChildCount();
        int i19 = this.mGravity & R.styleable.AppCompatTheme_windowActionBarOverlay;
        boolean z2 = this.mBaselineAligned;
        int[] iArr = this.mMaxAscent;
        int[] iArr2 = this.mMaxDescent;
        int absoluteGravity = Gravity.getAbsoluteGravity(this.mGravity & 8388615, getLayoutDirection());
        boolean z3 = true;
        int i20 = absoluteGravity != 1 ? absoluteGravity != 5 ? this.mPaddingLeft : ((this.mPaddingLeft + i3) - i) - this.mTotalLength : (((i3 - i) - this.mTotalLength) / 2) + this.mPaddingLeft;
        if (isLayoutRtl) {
            i5 = virtualChildCount - 1;
            i6 = -1;
        } else {
            i5 = 0;
            i6 = 1;
        }
        int i21 = 0;
        while (i21 < virtualChildCount) {
            int i22 = i5 + (i6 * i21);
            View virtualChildAt = getVirtualChildAt(i22);
            if (virtualChildAt == null) {
                i20 += measureNullChild(i22);
                z = z3;
                i7 = i15;
                i8 = virtualChildCount;
                i9 = i19;
            } else if (virtualChildAt.getVisibility() != 8) {
                int measuredWidth = virtualChildAt.getMeasuredWidth();
                int measuredHeight = virtualChildAt.getMeasuredHeight();
                LayoutParams layoutParams = (LayoutParams) virtualChildAt.getLayoutParams();
                if (z2) {
                    i10 = i21;
                    i8 = virtualChildCount;
                    if (layoutParams.height != -1) {
                        i11 = virtualChildAt.getBaseline();
                        i12 = layoutParams.gravity;
                        if (i12 < 0) {
                            i12 = i19;
                        }
                        i13 = i12 & R.styleable.AppCompatTheme_windowActionBarOverlay;
                        i9 = i19;
                        if (i13 != 16) {
                            z = true;
                            i14 = ((((i18 - measuredHeight) / 2) + i15) + layoutParams.topMargin) - layoutParams.bottomMargin;
                        } else if (i13 != 48) {
                            if (i13 != 80) {
                                i14 = i15;
                            } else {
                                int i23 = (i17 - measuredHeight) - layoutParams.bottomMargin;
                                if (i11 != -1) {
                                    i14 = i23 - (iArr2[2] - (virtualChildAt.getMeasuredHeight() - i11));
                                } else {
                                    i14 = i23;
                                    z = true;
                                }
                            }
                            z = true;
                        } else {
                            int i24 = layoutParams.topMargin + i15;
                            if (i11 != -1) {
                                z = true;
                                i24 += iArr[1] - i11;
                            } else {
                                z = true;
                            }
                            i14 = i24;
                        }
                        if (hasDividerBeforeChildAt(i22)) {
                            i20 += this.mDividerWidth;
                        }
                        int i25 = layoutParams.leftMargin + i20;
                        i7 = i15;
                        setChildFrame(virtualChildAt, i25 + getLocationOffset(virtualChildAt), i14, measuredWidth, measuredHeight);
                        i21 = i10 + getChildrenSkipCount(virtualChildAt, i22);
                        i20 = i25 + measuredWidth + layoutParams.rightMargin + getNextLocationOffset(virtualChildAt);
                        i21++;
                        z3 = z;
                        virtualChildCount = i8;
                        i19 = i9;
                        i15 = i7;
                    }
                } else {
                    i10 = i21;
                    i8 = virtualChildCount;
                }
                i11 = -1;
                i12 = layoutParams.gravity;
                if (i12 < 0) {
                }
                i13 = i12 & R.styleable.AppCompatTheme_windowActionBarOverlay;
                i9 = i19;
                if (i13 != 16) {
                }
                if (hasDividerBeforeChildAt(i22)) {
                }
                int i252 = layoutParams.leftMargin + i20;
                i7 = i15;
                setChildFrame(virtualChildAt, i252 + getLocationOffset(virtualChildAt), i14, measuredWidth, measuredHeight);
                i21 = i10 + getChildrenSkipCount(virtualChildAt, i22);
                i20 = i252 + measuredWidth + layoutParams.rightMargin + getNextLocationOffset(virtualChildAt);
                i21++;
                z3 = z;
                virtualChildCount = i8;
                i19 = i9;
                i15 = i7;
            } else {
                i7 = i15;
                i8 = virtualChildCount;
                i9 = i19;
                z = true;
            }
            i21++;
            z3 = z;
            virtualChildCount = i8;
            i19 = i9;
            i15 = i7;
        }
    }

    private void setChildFrame(View view, int i, int i2, int i3, int i4) {
        view.layout(i, i2, i3 + i, i4 + i2);
    }

    public void setOrientation(int i) {
        if (this.mOrientation != i) {
            this.mOrientation = i;
            requestLayout();
        }
    }

    public int getOrientation() {
        return this.mOrientation;
    }

    @RemotableViewMethod
    public void setGravity(int i) {
        if (this.mGravity != i) {
            if ((8388615 & i) == 0) {
                i |= 8388611;
            }
            if ((i & R.styleable.AppCompatTheme_windowActionBarOverlay) == 0) {
                i |= 48;
            }
            this.mGravity = i;
            requestLayout();
        }
    }

    @RemotableViewMethod
    public void setHorizontalGravity(int i) {
        int i2 = i & 8388615;
        if ((8388615 & this.mGravity) != i2) {
            this.mGravity = i2 | (this.mGravity & (-8388616));
            requestLayout();
        }
    }

    @RemotableViewMethod
    public void setVerticalGravity(int i) {
        int i2 = i & R.styleable.AppCompatTheme_windowActionBarOverlay;
        if ((this.mGravity & R.styleable.AppCompatTheme_windowActionBarOverlay) != i2) {
            this.mGravity = i2 | (this.mGravity & (-113));
            requestLayout();
        }
    }

    @Override // android.view.ViewGroup
    public LayoutParams generateLayoutParams(AttributeSet attributeSet) {
        return new LayoutParams(getContext(), attributeSet);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public LayoutParams generateDefaultLayoutParams() {
        if (this.mOrientation == 0) {
            return new LayoutParams(-2, -2);
        }
        if (this.mOrientation == 1) {
            return new LayoutParams(-1, -2);
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public LayoutParams generateLayoutParams(ViewGroup.LayoutParams layoutParams) {
        return new LayoutParams(layoutParams);
    }

    @Override // android.view.ViewGroup
    protected boolean checkLayoutParams(ViewGroup.LayoutParams layoutParams) {
        return layoutParams instanceof LayoutParams;
    }

    @Override // android.view.ViewGroup, android.view.View
    public CharSequence getAccessibilityClassName() {
        return MatchParentShrinkingLinearLayout.class.getName();
    }

    protected void encodeProperties(ViewHierarchyEncoder viewHierarchyEncoder) {
        super.encodeProperties(viewHierarchyEncoder);
        viewHierarchyEncoder.addProperty("layout:baselineAligned", this.mBaselineAligned);
        viewHierarchyEncoder.addProperty("layout:baselineAlignedChildIndex", this.mBaselineAlignedChildIndex);
        viewHierarchyEncoder.addProperty("measurement:baselineChildTop", this.mBaselineChildTop);
        viewHierarchyEncoder.addProperty("measurement:orientation", this.mOrientation);
        viewHierarchyEncoder.addProperty("measurement:gravity", this.mGravity);
        viewHierarchyEncoder.addProperty("measurement:totalLength", this.mTotalLength);
        viewHierarchyEncoder.addProperty("layout:totalLength", this.mTotalLength);
        viewHierarchyEncoder.addProperty("layout:useLargestChild", this.mUseLargestChild);
    }

    /* loaded from: classes.dex */
    public static class LayoutParams extends ViewGroup.MarginLayoutParams {
        @ViewDebug.ExportedProperty(category = "layout", mapping = {@ViewDebug.IntToString(from = -1, to = "NONE"), @ViewDebug.IntToString(from = 0, to = "NONE"), @ViewDebug.IntToString(from = 48, to = "TOP"), @ViewDebug.IntToString(from = R.styleable.AppCompatTheme_panelBackground, to = "BOTTOM"), @ViewDebug.IntToString(from = 3, to = "LEFT"), @ViewDebug.IntToString(from = 5, to = "RIGHT"), @ViewDebug.IntToString(from = 8388611, to = "START"), @ViewDebug.IntToString(from = 8388613, to = "END"), @ViewDebug.IntToString(from = 16, to = "CENTER_VERTICAL"), @ViewDebug.IntToString(from = R.styleable.AppCompatTheme_windowActionBarOverlay, to = "FILL_VERTICAL"), @ViewDebug.IntToString(from = 1, to = "CENTER_HORIZONTAL"), @ViewDebug.IntToString(from = 7, to = "FILL_HORIZONTAL"), @ViewDebug.IntToString(from = 17, to = "CENTER"), @ViewDebug.IntToString(from = R.styleable.AppCompatTheme_windowMinWidthMinor, to = "FILL")})
        public int gravity;
        @ViewDebug.ExportedProperty(category = "layout")
        public float weight;

        public LayoutParams(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
            this.gravity = -1;
            TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, com.android.internal.R.styleable.LinearLayout_Layout);
            this.weight = obtainStyledAttributes.getFloat(3, 0.0f);
            this.gravity = obtainStyledAttributes.getInt(0, -1);
            obtainStyledAttributes.recycle();
        }

        public LayoutParams(int i, int i2) {
            super(i, i2);
            this.gravity = -1;
            this.weight = 0.0f;
        }

        public LayoutParams(ViewGroup.LayoutParams layoutParams) {
            super(layoutParams);
            this.gravity = -1;
        }

        public String debug(String str) {
            return str + "MatchParentShrinkingLinearLayout.LayoutParams={width=" + sizeToString(this.width) + ", height=" + sizeToString(this.height) + " weight=" + this.weight + "}";
        }

        protected void encodeProperties(ViewHierarchyEncoder viewHierarchyEncoder) {
            super.encodeProperties(viewHierarchyEncoder);
            viewHierarchyEncoder.addProperty("layout:weight", this.weight);
            viewHierarchyEncoder.addProperty("layout:gravity", this.gravity);
        }
    }
}
