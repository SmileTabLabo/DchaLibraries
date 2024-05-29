package android.support.v7.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.appcompat.R$styleable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
/* loaded from: a.zip:android/support/v7/widget/LinearLayoutCompat.class */
public class LinearLayoutCompat extends ViewGroup {
    private boolean mBaselineAligned;
    private int mBaselineAlignedChildIndex;
    private int mBaselineChildTop;
    private Drawable mDivider;
    private int mDividerHeight;
    private int mDividerPadding;
    private int mDividerWidth;
    private int mGravity;
    private int[] mMaxAscent;
    private int[] mMaxDescent;
    private int mOrientation;
    private int mShowDividers;
    private int mTotalLength;
    private boolean mUseLargestChild;
    private float mWeightSum;

    /* loaded from: a.zip:android/support/v7/widget/LinearLayoutCompat$LayoutParams.class */
    public static class LayoutParams extends ViewGroup.MarginLayoutParams {
        public int gravity;
        public float weight;

        public LayoutParams(int i, int i2) {
            super(i, i2);
            this.gravity = -1;
            this.weight = 0.0f;
        }

        public LayoutParams(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
            this.gravity = -1;
            TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.LinearLayoutCompat_Layout);
            this.weight = obtainStyledAttributes.getFloat(R$styleable.LinearLayoutCompat_Layout_android_layout_weight, 0.0f);
            this.gravity = obtainStyledAttributes.getInt(R$styleable.LinearLayoutCompat_Layout_android_layout_gravity, -1);
            obtainStyledAttributes.recycle();
        }

        public LayoutParams(ViewGroup.LayoutParams layoutParams) {
            super(layoutParams);
            this.gravity = -1;
        }
    }

    public LinearLayoutCompat(Context context) {
        this(context, null);
    }

    public LinearLayoutCompat(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public LinearLayoutCompat(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mBaselineAligned = true;
        this.mBaselineAlignedChildIndex = -1;
        this.mBaselineChildTop = 0;
        this.mGravity = 8388659;
        TintTypedArray obtainStyledAttributes = TintTypedArray.obtainStyledAttributes(context, attributeSet, R$styleable.LinearLayoutCompat, i, 0);
        int i2 = obtainStyledAttributes.getInt(R$styleable.LinearLayoutCompat_android_orientation, -1);
        if (i2 >= 0) {
            setOrientation(i2);
        }
        int i3 = obtainStyledAttributes.getInt(R$styleable.LinearLayoutCompat_android_gravity, -1);
        if (i3 >= 0) {
            setGravity(i3);
        }
        boolean z = obtainStyledAttributes.getBoolean(R$styleable.LinearLayoutCompat_android_baselineAligned, true);
        if (!z) {
            setBaselineAligned(z);
        }
        this.mWeightSum = obtainStyledAttributes.getFloat(R$styleable.LinearLayoutCompat_android_weightSum, -1.0f);
        this.mBaselineAlignedChildIndex = obtainStyledAttributes.getInt(R$styleable.LinearLayoutCompat_android_baselineAlignedChildIndex, -1);
        this.mUseLargestChild = obtainStyledAttributes.getBoolean(R$styleable.LinearLayoutCompat_measureWithLargestChild, false);
        setDividerDrawable(obtainStyledAttributes.getDrawable(R$styleable.LinearLayoutCompat_divider));
        this.mShowDividers = obtainStyledAttributes.getInt(R$styleable.LinearLayoutCompat_showDividers, 0);
        this.mDividerPadding = obtainStyledAttributes.getDimensionPixelSize(R$styleable.LinearLayoutCompat_dividerPadding, 0);
        obtainStyledAttributes.recycle();
    }

    private void forceUniformHeight(int i, int i2) {
        int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(getMeasuredHeight(), 1073741824);
        for (int i3 = 0; i3 < i; i3++) {
            View virtualChildAt = getVirtualChildAt(i3);
            if (virtualChildAt.getVisibility() != 8) {
                LayoutParams layoutParams = (LayoutParams) virtualChildAt.getLayoutParams();
                if (layoutParams.height == -1) {
                    int i4 = layoutParams.width;
                    layoutParams.width = virtualChildAt.getMeasuredWidth();
                    measureChildWithMargins(virtualChildAt, i2, 0, makeMeasureSpec, 0);
                    layoutParams.width = i4;
                }
            }
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

    private void setChildFrame(View view, int i, int i2, int i3, int i4) {
        view.layout(i, i2, i + i3, i2 + i4);
    }

    @Override // android.view.ViewGroup
    protected boolean checkLayoutParams(ViewGroup.LayoutParams layoutParams) {
        return layoutParams instanceof LayoutParams;
    }

    void drawDividersHorizontal(Canvas canvas) {
        int left;
        int virtualChildCount = getVirtualChildCount();
        boolean isLayoutRtl = ViewUtils.isLayoutRtl(this);
        for (int i = 0; i < virtualChildCount; i++) {
            View virtualChildAt = getVirtualChildAt(i);
            if (virtualChildAt != null && virtualChildAt.getVisibility() != 8 && hasDividerBeforeChildAt(i)) {
                LayoutParams layoutParams = (LayoutParams) virtualChildAt.getLayoutParams();
                drawVerticalDivider(canvas, isLayoutRtl ? virtualChildAt.getRight() + layoutParams.rightMargin : (virtualChildAt.getLeft() - layoutParams.leftMargin) - this.mDividerWidth);
            }
        }
        if (hasDividerBeforeChildAt(virtualChildCount)) {
            View virtualChildAt2 = getVirtualChildAt(virtualChildCount - 1);
            if (virtualChildAt2 == null) {
                left = isLayoutRtl ? getPaddingLeft() : (getWidth() - getPaddingRight()) - this.mDividerWidth;
            } else {
                LayoutParams layoutParams2 = (LayoutParams) virtualChildAt2.getLayoutParams();
                left = isLayoutRtl ? (virtualChildAt2.getLeft() - layoutParams2.leftMargin) - this.mDividerWidth : virtualChildAt2.getRight() + layoutParams2.rightMargin;
            }
            drawVerticalDivider(canvas, left);
        }
    }

    void drawDividersVertical(Canvas canvas) {
        int virtualChildCount = getVirtualChildCount();
        for (int i = 0; i < virtualChildCount; i++) {
            View virtualChildAt = getVirtualChildAt(i);
            if (virtualChildAt != null && virtualChildAt.getVisibility() != 8 && hasDividerBeforeChildAt(i)) {
                drawHorizontalDivider(canvas, (virtualChildAt.getTop() - ((LayoutParams) virtualChildAt.getLayoutParams()).topMargin) - this.mDividerHeight);
            }
        }
        if (hasDividerBeforeChildAt(virtualChildCount)) {
            View virtualChildAt2 = getVirtualChildAt(virtualChildCount - 1);
            drawHorizontalDivider(canvas, virtualChildAt2 == null ? (getHeight() - getPaddingBottom()) - this.mDividerHeight : virtualChildAt2.getBottom() + ((LayoutParams) virtualChildAt2.getLayoutParams()).bottomMargin);
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

    @Override // android.view.ViewGroup
    public LayoutParams generateLayoutParams(AttributeSet attributeSet) {
        return new LayoutParams(getContext(), attributeSet);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public LayoutParams generateLayoutParams(ViewGroup.LayoutParams layoutParams) {
        return new LayoutParams(layoutParams);
    }

    @Override // android.view.View
    public int getBaseline() {
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
        int i = this.mBaselineChildTop;
        int i2 = i;
        if (this.mOrientation == 1) {
            int i3 = this.mGravity & 112;
            i2 = i;
            if (i3 != 48) {
                switch (i3) {
                    case 16:
                        i2 = i + (((((getBottom() - getTop()) - getPaddingTop()) - getPaddingBottom()) - this.mTotalLength) / 2);
                        break;
                    case 80:
                        i2 = ((getBottom() - getTop()) - getPaddingBottom()) - this.mTotalLength;
                        break;
                    default:
                        i2 = i;
                        break;
                }
            }
        }
        return ((LayoutParams) childAt.getLayoutParams()).topMargin + i2 + baseline;
    }

    int getChildrenSkipCount(View view, int i) {
        return 0;
    }

    public int getDividerWidth() {
        return this.mDividerWidth;
    }

    int getLocationOffset(View view) {
        return 0;
    }

    int getNextLocationOffset(View view) {
        return 0;
    }

    View getVirtualChildAt(int i) {
        return getChildAt(i);
    }

    int getVirtualChildCount() {
        return getChildCount();
    }

    protected boolean hasDividerBeforeChildAt(int i) {
        boolean z;
        boolean z2 = true;
        if (i == 0) {
            if ((this.mShowDividers & 1) == 0) {
                z2 = false;
            }
            return z2;
        } else if (i == getChildCount()) {
            return (this.mShowDividers & 4) != 0;
        } else if ((this.mShowDividers & 2) != 0) {
            while (true) {
                i--;
                z = false;
                if (i < 0) {
                    break;
                } else if (getChildAt(i).getVisibility() != 8) {
                    z = true;
                    break;
                }
            }
            return z;
        } else {
            return false;
        }
    }

    void layoutHorizontal(int i, int i2, int i3, int i4) {
        int paddingLeft;
        int i5;
        int i6;
        boolean isLayoutRtl = ViewUtils.isLayoutRtl(this);
        int paddingTop = getPaddingTop();
        int i7 = i4 - i2;
        int paddingBottom = getPaddingBottom();
        int paddingBottom2 = getPaddingBottom();
        int virtualChildCount = getVirtualChildCount();
        int i8 = this.mGravity;
        int i9 = this.mGravity;
        boolean z = this.mBaselineAligned;
        int[] iArr = this.mMaxAscent;
        int[] iArr2 = this.mMaxDescent;
        switch (GravityCompat.getAbsoluteGravity(i8 & 8388615, ViewCompat.getLayoutDirection(this))) {
            case 1:
                paddingLeft = getPaddingLeft() + (((i3 - i) - this.mTotalLength) / 2);
                break;
            case 5:
                paddingLeft = ((getPaddingLeft() + i3) - i) - this.mTotalLength;
                break;
            default:
                paddingLeft = getPaddingLeft();
                break;
        }
        int i10 = 0;
        int i11 = 1;
        if (isLayoutRtl) {
            i10 = virtualChildCount - 1;
            i11 = -1;
        }
        int i12 = 0;
        while (true) {
            int i13 = i12;
            int i14 = paddingLeft;
            if (i13 >= virtualChildCount) {
                return;
            }
            int i15 = i10 + (i11 * i13);
            View virtualChildAt = getVirtualChildAt(i15);
            if (virtualChildAt == null) {
                paddingLeft = i14 + measureNullChild(i15);
                i5 = i13;
            } else {
                paddingLeft = i14;
                i5 = i13;
                if (virtualChildAt.getVisibility() != 8) {
                    int measuredWidth = virtualChildAt.getMeasuredWidth();
                    int measuredHeight = virtualChildAt.getMeasuredHeight();
                    LayoutParams layoutParams = (LayoutParams) virtualChildAt.getLayoutParams();
                    int i16 = -1;
                    if (z) {
                        i16 = -1;
                        if (layoutParams.height != -1) {
                            i16 = virtualChildAt.getBaseline();
                        }
                    }
                    int i17 = layoutParams.gravity;
                    int i18 = i17;
                    if (i17 < 0) {
                        i18 = i9 & 112;
                    }
                    switch (i18 & 112) {
                        case 16:
                            i6 = ((((((i7 - paddingTop) - paddingBottom2) - measuredHeight) / 2) + paddingTop) + layoutParams.topMargin) - layoutParams.bottomMargin;
                            break;
                        case 48:
                            int i19 = paddingTop + layoutParams.topMargin;
                            i6 = i19;
                            if (i16 != -1) {
                                i6 = i19 + (iArr[1] - i16);
                                break;
                            }
                            break;
                        case 80:
                            int i20 = ((i7 - paddingBottom) - measuredHeight) - layoutParams.bottomMargin;
                            i6 = i20;
                            if (i16 != -1) {
                                i6 = i20 - (iArr2[2] - (virtualChildAt.getMeasuredHeight() - i16));
                                break;
                            }
                            break;
                        default:
                            i6 = paddingTop;
                            break;
                    }
                    int i21 = i14;
                    if (hasDividerBeforeChildAt(i15)) {
                        i21 = i14 + this.mDividerWidth;
                    }
                    int i22 = i21 + layoutParams.leftMargin;
                    setChildFrame(virtualChildAt, i22 + getLocationOffset(virtualChildAt), i6, measuredWidth, measuredHeight);
                    paddingLeft = i22 + layoutParams.rightMargin + measuredWidth + getNextLocationOffset(virtualChildAt);
                    i5 = i13 + getChildrenSkipCount(virtualChildAt, i15);
                }
            }
            i12 = i5 + 1;
        }
    }

    void layoutVertical(int i, int i2, int i3, int i4) {
        int paddingTop;
        int i5;
        int i6;
        int i7;
        int paddingLeft = getPaddingLeft();
        int i8 = i3 - i;
        int paddingRight = getPaddingRight();
        int paddingRight2 = getPaddingRight();
        int virtualChildCount = getVirtualChildCount();
        int i9 = this.mGravity;
        int i10 = this.mGravity;
        switch (i9 & 112) {
            case 16:
                paddingTop = getPaddingTop() + (((i4 - i2) - this.mTotalLength) / 2);
                break;
            case 80:
                paddingTop = ((getPaddingTop() + i4) - i2) - this.mTotalLength;
                break;
            default:
                paddingTop = getPaddingTop();
                break;
        }
        int i11 = 0;
        while (i11 < virtualChildCount) {
            View virtualChildAt = getVirtualChildAt(i11);
            if (virtualChildAt == null) {
                i5 = paddingTop + measureNullChild(i11);
                i6 = i11;
            } else {
                i5 = paddingTop;
                i6 = i11;
                if (virtualChildAt.getVisibility() != 8) {
                    int measuredWidth = virtualChildAt.getMeasuredWidth();
                    int measuredHeight = virtualChildAt.getMeasuredHeight();
                    LayoutParams layoutParams = (LayoutParams) virtualChildAt.getLayoutParams();
                    int i12 = layoutParams.gravity;
                    int i13 = i12;
                    if (i12 < 0) {
                        i13 = i10 & 8388615;
                    }
                    switch (GravityCompat.getAbsoluteGravity(i13, ViewCompat.getLayoutDirection(this)) & 7) {
                        case 1:
                            i7 = ((((((i8 - paddingLeft) - paddingRight2) - measuredWidth) / 2) + paddingLeft) + layoutParams.leftMargin) - layoutParams.rightMargin;
                            break;
                        case 5:
                            i7 = ((i8 - paddingRight) - measuredWidth) - layoutParams.rightMargin;
                            break;
                        default:
                            i7 = paddingLeft + layoutParams.leftMargin;
                            break;
                    }
                    int i14 = paddingTop;
                    if (hasDividerBeforeChildAt(i11)) {
                        i14 = paddingTop + this.mDividerHeight;
                    }
                    int i15 = i14 + layoutParams.topMargin;
                    setChildFrame(virtualChildAt, i7, i15 + getLocationOffset(virtualChildAt), measuredWidth, measuredHeight);
                    i5 = i15 + layoutParams.bottomMargin + measuredHeight + getNextLocationOffset(virtualChildAt);
                    i6 = i11 + getChildrenSkipCount(virtualChildAt, i11);
                }
            }
            i11 = i6 + 1;
            paddingTop = i5;
        }
    }

    void measureChildBeforeLayout(View view, int i, int i2, int i3, int i4, int i5) {
        measureChildWithMargins(view, i2, i3, i4, i5);
    }

    /* JADX WARN: Code restructure failed: missing block: B:114:0x0428, code lost:
        if (r0[3] != (-1)) goto L237;
     */
    /* JADX WARN: Code restructure failed: missing block: B:204:0x0886, code lost:
        if (r0[3] != (-1)) goto L235;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    void measureHorizontal(int i, int i2) {
        int max;
        int max2;
        int i3;
        int i4;
        boolean z;
        int i5;
        boolean z2;
        int baseline;
        this.mTotalLength = 0;
        int i6 = 0;
        int i7 = 0;
        int i8 = 0;
        int i9 = 0;
        boolean z3 = true;
        float f = 0.0f;
        int virtualChildCount = getVirtualChildCount();
        int mode = View.MeasureSpec.getMode(i);
        int mode2 = View.MeasureSpec.getMode(i2);
        boolean z4 = false;
        boolean z5 = false;
        if (this.mMaxAscent == null || this.mMaxDescent == null) {
            this.mMaxAscent = new int[4];
            this.mMaxDescent = new int[4];
        }
        int[] iArr = this.mMaxAscent;
        int[] iArr2 = this.mMaxDescent;
        iArr[3] = -1;
        iArr[2] = -1;
        iArr[1] = -1;
        iArr[0] = -1;
        iArr2[3] = -1;
        iArr2[2] = -1;
        iArr2[1] = -1;
        iArr2[0] = -1;
        boolean z6 = this.mBaselineAligned;
        boolean z7 = this.mUseLargestChild;
        boolean z8 = mode == 1073741824;
        int i10 = Integer.MIN_VALUE;
        int i11 = 0;
        while (i11 < virtualChildCount) {
            View virtualChildAt = getVirtualChildAt(i11);
            if (virtualChildAt == null) {
                this.mTotalLength += measureNullChild(i11);
                i5 = i10;
            } else if (virtualChildAt.getVisibility() == 8) {
                i11 += getChildrenSkipCount(virtualChildAt, i11);
                i5 = i10;
            } else {
                if (hasDividerBeforeChildAt(i11)) {
                    this.mTotalLength += this.mDividerWidth;
                }
                LayoutParams layoutParams = (LayoutParams) virtualChildAt.getLayoutParams();
                f += layoutParams.weight;
                if (mode == 1073741824 && layoutParams.width == 0 && layoutParams.weight > 0.0f) {
                    if (z8) {
                        this.mTotalLength += layoutParams.leftMargin + layoutParams.rightMargin;
                    } else {
                        int i12 = this.mTotalLength;
                        this.mTotalLength = Math.max(i12, layoutParams.leftMargin + i12 + layoutParams.rightMargin);
                    }
                    if (z6) {
                        int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, 0);
                        virtualChildAt.measure(makeMeasureSpec, makeMeasureSpec);
                        z2 = z5;
                        i5 = i10;
                    } else {
                        z2 = true;
                        i5 = i10;
                    }
                } else {
                    int i13 = Integer.MIN_VALUE;
                    if (layoutParams.width == 0) {
                        i13 = Integer.MIN_VALUE;
                        if (layoutParams.weight > 0.0f) {
                            i13 = 0;
                            layoutParams.width = -2;
                        }
                    }
                    measureChildBeforeLayout(virtualChildAt, i11, i, f == 0.0f ? this.mTotalLength : 0, i2, 0);
                    if (i13 != Integer.MIN_VALUE) {
                        layoutParams.width = i13;
                    }
                    int measuredWidth = virtualChildAt.getMeasuredWidth();
                    if (z8) {
                        this.mTotalLength += layoutParams.leftMargin + measuredWidth + layoutParams.rightMargin + getNextLocationOffset(virtualChildAt);
                    } else {
                        int i14 = this.mTotalLength;
                        this.mTotalLength = Math.max(i14, i14 + measuredWidth + layoutParams.leftMargin + layoutParams.rightMargin + getNextLocationOffset(virtualChildAt));
                    }
                    i5 = i10;
                    z2 = z5;
                    if (z7) {
                        i5 = Math.max(measuredWidth, i10);
                        z2 = z5;
                    }
                }
                boolean z9 = z4;
                boolean z10 = false;
                if (mode2 != 1073741824) {
                    z9 = z4;
                    z10 = false;
                    if (layoutParams.height == -1) {
                        z9 = true;
                        z10 = true;
                    }
                }
                int i15 = layoutParams.topMargin + layoutParams.bottomMargin;
                int measuredHeight = virtualChildAt.getMeasuredHeight() + i15;
                int combineMeasuredStates = ViewUtils.combineMeasuredStates(i7, ViewCompat.getMeasuredState(virtualChildAt));
                if (z6 && (baseline = virtualChildAt.getBaseline()) != -1) {
                    int i16 = ((((layoutParams.gravity < 0 ? this.mGravity : layoutParams.gravity) & 112) >> 4) & (-2)) >> 1;
                    iArr[i16] = Math.max(iArr[i16], baseline);
                    iArr2[i16] = Math.max(iArr2[i16], measuredHeight - baseline);
                }
                i6 = Math.max(i6, measuredHeight);
                z3 = z3 && layoutParams.height == -1;
                if (layoutParams.weight > 0.0f) {
                    if (!z10) {
                        i15 = measuredHeight;
                    }
                    i9 = Math.max(i9, i15);
                } else {
                    if (!z10) {
                        i15 = measuredHeight;
                    }
                    i8 = Math.max(i8, i15);
                }
                i11 += getChildrenSkipCount(virtualChildAt, i11);
                i7 = combineMeasuredStates;
                z4 = z9;
                z5 = z2;
            }
            i11++;
            i10 = i5;
        }
        if (this.mTotalLength > 0 && hasDividerBeforeChildAt(virtualChildCount)) {
            this.mTotalLength += this.mDividerWidth;
        }
        if (iArr[1] == -1 && iArr[0] == -1 && iArr[2] == -1) {
            max = i6;
        }
        max = Math.max(i6, Math.max(iArr[3], Math.max(iArr[0], Math.max(iArr[1], iArr[2]))) + Math.max(iArr2[3], Math.max(iArr2[0], Math.max(iArr2[1], iArr2[2]))));
        if (z7 && (mode == Integer.MIN_VALUE || mode == 0)) {
            this.mTotalLength = 0;
            int i17 = 0;
            while (i17 < virtualChildCount) {
                View virtualChildAt2 = getVirtualChildAt(i17);
                if (virtualChildAt2 == null) {
                    this.mTotalLength += measureNullChild(i17);
                } else if (virtualChildAt2.getVisibility() == 8) {
                    i17 += getChildrenSkipCount(virtualChildAt2, i17);
                } else {
                    LayoutParams layoutParams2 = (LayoutParams) virtualChildAt2.getLayoutParams();
                    if (z8) {
                        this.mTotalLength += layoutParams2.leftMargin + i10 + layoutParams2.rightMargin + getNextLocationOffset(virtualChildAt2);
                    } else {
                        int i18 = this.mTotalLength;
                        this.mTotalLength = Math.max(i18, i18 + i10 + layoutParams2.leftMargin + layoutParams2.rightMargin + getNextLocationOffset(virtualChildAt2));
                    }
                }
                i17++;
            }
        }
        this.mTotalLength += getPaddingLeft() + getPaddingRight();
        int resolveSizeAndState = ViewCompat.resolveSizeAndState(Math.max(this.mTotalLength, getSuggestedMinimumWidth()), i, 0);
        int i19 = (resolveSizeAndState & 16777215) - this.mTotalLength;
        if (z5 || (i19 != 0 && f > 0.0f)) {
            float f2 = this.mWeightSum > 0.0f ? this.mWeightSum : f;
            iArr[3] = -1;
            iArr[2] = -1;
            iArr[1] = -1;
            iArr[0] = -1;
            iArr2[3] = -1;
            iArr2[2] = -1;
            iArr2[1] = -1;
            iArr2[0] = -1;
            int i20 = -1;
            this.mTotalLength = 0;
            int i21 = 0;
            int i22 = i8;
            while (i21 < virtualChildCount) {
                View virtualChildAt3 = getVirtualChildAt(i21);
                boolean z11 = z3;
                int i23 = i22;
                int i24 = i7;
                int i25 = i19;
                int i26 = i20;
                float f3 = f2;
                if (virtualChildAt3 != null) {
                    if (virtualChildAt3.getVisibility() == 8) {
                        f3 = f2;
                        i26 = i20;
                        i25 = i19;
                        i24 = i7;
                        i23 = i22;
                        z11 = z3;
                    } else {
                        LayoutParams layoutParams3 = (LayoutParams) virtualChildAt3.getLayoutParams();
                        float f4 = layoutParams3.weight;
                        int i27 = i7;
                        int i28 = i19;
                        float f5 = f2;
                        if (f4 > 0.0f) {
                            int i29 = (int) ((i19 * f4) / f2);
                            f5 = f2 - f4;
                            int i30 = i19 - i29;
                            int childMeasureSpec = getChildMeasureSpec(i2, getPaddingTop() + getPaddingBottom() + layoutParams3.topMargin + layoutParams3.bottomMargin, layoutParams3.height);
                            if (layoutParams3.width == 0 && mode == 1073741824) {
                                if (i29 <= 0) {
                                    i29 = 0;
                                }
                                virtualChildAt3.measure(View.MeasureSpec.makeMeasureSpec(i29, 1073741824), childMeasureSpec);
                            } else {
                                int measuredWidth2 = virtualChildAt3.getMeasuredWidth() + i29;
                                int i31 = measuredWidth2;
                                if (measuredWidth2 < 0) {
                                    i31 = 0;
                                }
                                virtualChildAt3.measure(View.MeasureSpec.makeMeasureSpec(i31, 1073741824), childMeasureSpec);
                            }
                            i28 = i30;
                            i27 = ViewUtils.combineMeasuredStates(i7, ViewCompat.getMeasuredState(virtualChildAt3) & (-16777216));
                        }
                        if (z8) {
                            this.mTotalLength += virtualChildAt3.getMeasuredWidth() + layoutParams3.leftMargin + layoutParams3.rightMargin + getNextLocationOffset(virtualChildAt3);
                        } else {
                            int i32 = this.mTotalLength;
                            this.mTotalLength = Math.max(i32, virtualChildAt3.getMeasuredWidth() + i32 + layoutParams3.leftMargin + layoutParams3.rightMargin + getNextLocationOffset(virtualChildAt3));
                        }
                        boolean z12 = mode2 != 1073741824 ? layoutParams3.height == -1 : false;
                        int i33 = layoutParams3.topMargin + layoutParams3.bottomMargin;
                        int measuredHeight2 = virtualChildAt3.getMeasuredHeight() + i33;
                        int max3 = Math.max(i20, measuredHeight2);
                        int max4 = Math.max(i22, z12 ? i33 : measuredHeight2);
                        boolean z13 = z3 && layoutParams3.height == -1;
                        z11 = z13;
                        i23 = max4;
                        i24 = i27;
                        i25 = i28;
                        i26 = max3;
                        f3 = f5;
                        if (z6) {
                            int baseline2 = virtualChildAt3.getBaseline();
                            z11 = z13;
                            i23 = max4;
                            i24 = i27;
                            i25 = i28;
                            i26 = max3;
                            f3 = f5;
                            if (baseline2 != -1) {
                                int i34 = ((((layoutParams3.gravity < 0 ? this.mGravity : layoutParams3.gravity) & 112) >> 4) & (-2)) >> 1;
                                iArr[i34] = Math.max(iArr[i34], baseline2);
                                iArr2[i34] = Math.max(iArr2[i34], measuredHeight2 - baseline2);
                                z11 = z13;
                                i23 = max4;
                                i24 = i27;
                                i25 = i28;
                                i26 = max3;
                                f3 = f5;
                            }
                        }
                    }
                }
                i21++;
                z3 = z11;
                i22 = i23;
                i7 = i24;
                i19 = i25;
                i20 = i26;
                f2 = f3;
            }
            this.mTotalLength += getPaddingLeft() + getPaddingRight();
            if (iArr[1] == -1 && iArr[0] == -1 && iArr[2] == -1) {
                z = z3;
                i4 = i22;
                i3 = i7;
                max2 = i20;
            }
            max2 = Math.max(i20, Math.max(iArr[3], Math.max(iArr[0], Math.max(iArr[1], iArr[2]))) + Math.max(iArr2[3], Math.max(iArr2[0], Math.max(iArr2[1], iArr2[2]))));
            i3 = i7;
            i4 = i22;
            z = z3;
        } else {
            int max5 = Math.max(i8, i9);
            z = z3;
            i4 = max5;
            i3 = i7;
            max2 = max;
            if (z7) {
                z = z3;
                i4 = max5;
                i3 = i7;
                max2 = max;
                if (mode != 1073741824) {
                    int i35 = 0;
                    while (true) {
                        z = z3;
                        i4 = max5;
                        i3 = i7;
                        max2 = max;
                        if (i35 >= virtualChildCount) {
                            break;
                        }
                        View virtualChildAt4 = getVirtualChildAt(i35);
                        if (virtualChildAt4 != null && virtualChildAt4.getVisibility() != 8 && ((LayoutParams) virtualChildAt4.getLayoutParams()).weight > 0.0f) {
                            virtualChildAt4.measure(View.MeasureSpec.makeMeasureSpec(i10, 1073741824), View.MeasureSpec.makeMeasureSpec(virtualChildAt4.getMeasuredHeight(), 1073741824));
                        }
                        i35++;
                    }
                }
            }
        }
        int i36 = max2;
        if (!z) {
            i36 = max2;
            if (mode2 != 1073741824) {
                i36 = i4;
            }
        }
        setMeasuredDimension(((-16777216) & i3) | resolveSizeAndState, ViewCompat.resolveSizeAndState(Math.max(i36 + getPaddingTop() + getPaddingBottom(), getSuggestedMinimumHeight()), i2, i3 << 16));
        if (z4) {
            forceUniformHeight(virtualChildCount, i);
        }
    }

    int measureNullChild(int i) {
        return 0;
    }

    void measureVertical(int i, int i2) {
        int i3;
        int i4;
        boolean z;
        int i5;
        int i6;
        int max;
        boolean z2;
        int i7;
        boolean z3;
        this.mTotalLength = 0;
        int i8 = 0;
        int i9 = 0;
        int i10 = 0;
        int i11 = 0;
        boolean z4 = true;
        float f = 0.0f;
        int virtualChildCount = getVirtualChildCount();
        int mode = View.MeasureSpec.getMode(i);
        int mode2 = View.MeasureSpec.getMode(i2);
        boolean z5 = false;
        boolean z6 = false;
        int i12 = this.mBaselineAlignedChildIndex;
        boolean z7 = this.mUseLargestChild;
        int i13 = Integer.MIN_VALUE;
        int i14 = 0;
        while (i14 < virtualChildCount) {
            View virtualChildAt = getVirtualChildAt(i14);
            if (virtualChildAt == null) {
                this.mTotalLength += measureNullChild(i14);
                i7 = i13;
            } else if (virtualChildAt.getVisibility() == 8) {
                i14 += getChildrenSkipCount(virtualChildAt, i14);
                i7 = i13;
            } else {
                if (hasDividerBeforeChildAt(i14)) {
                    this.mTotalLength += this.mDividerHeight;
                }
                LayoutParams layoutParams = (LayoutParams) virtualChildAt.getLayoutParams();
                f += layoutParams.weight;
                if (mode2 == 1073741824 && layoutParams.height == 0 && layoutParams.weight > 0.0f) {
                    int i15 = this.mTotalLength;
                    this.mTotalLength = Math.max(i15, layoutParams.topMargin + i15 + layoutParams.bottomMargin);
                    z3 = true;
                    i7 = i13;
                } else {
                    int i16 = Integer.MIN_VALUE;
                    if (layoutParams.height == 0) {
                        i16 = Integer.MIN_VALUE;
                        if (layoutParams.weight > 0.0f) {
                            i16 = 0;
                            layoutParams.height = -2;
                        }
                    }
                    measureChildBeforeLayout(virtualChildAt, i14, i, 0, i2, f == 0.0f ? this.mTotalLength : 0);
                    if (i16 != Integer.MIN_VALUE) {
                        layoutParams.height = i16;
                    }
                    int measuredHeight = virtualChildAt.getMeasuredHeight();
                    int i17 = this.mTotalLength;
                    this.mTotalLength = Math.max(i17, i17 + measuredHeight + layoutParams.topMargin + layoutParams.bottomMargin + getNextLocationOffset(virtualChildAt));
                    i7 = i13;
                    z3 = z6;
                    if (z7) {
                        i7 = Math.max(measuredHeight, i13);
                        z3 = z6;
                    }
                }
                if (i12 >= 0 && i12 == i14 + 1) {
                    this.mBaselineChildTop = this.mTotalLength;
                }
                if (i14 < i12 && layoutParams.weight > 0.0f) {
                    throw new RuntimeException("A child of LinearLayout with index less than mBaselineAlignedChildIndex has weight > 0, which won't work.  Either remove the weight, or don't set mBaselineAlignedChildIndex.");
                }
                boolean z8 = z5;
                boolean z9 = false;
                if (mode != 1073741824) {
                    z8 = z5;
                    z9 = false;
                    if (layoutParams.width == -1) {
                        z8 = true;
                        z9 = true;
                    }
                }
                int i18 = layoutParams.leftMargin + layoutParams.rightMargin;
                int measuredWidth = virtualChildAt.getMeasuredWidth() + i18;
                i8 = Math.max(i8, measuredWidth);
                i9 = ViewUtils.combineMeasuredStates(i9, ViewCompat.getMeasuredState(virtualChildAt));
                z4 = z4 && layoutParams.width == -1;
                if (layoutParams.weight > 0.0f) {
                    if (!z9) {
                        i18 = measuredWidth;
                    }
                    i11 = Math.max(i11, i18);
                } else {
                    if (z9) {
                        measuredWidth = i18;
                    }
                    i10 = Math.max(i10, measuredWidth);
                }
                i14 += getChildrenSkipCount(virtualChildAt, i14);
                z5 = z8;
                z6 = z3;
            }
            i14++;
            i13 = i7;
        }
        if (this.mTotalLength > 0 && hasDividerBeforeChildAt(virtualChildCount)) {
            this.mTotalLength += this.mDividerHeight;
        }
        if (z7 && (mode2 == Integer.MIN_VALUE || mode2 == 0)) {
            this.mTotalLength = 0;
            int i19 = 0;
            while (i19 < virtualChildCount) {
                View virtualChildAt2 = getVirtualChildAt(i19);
                if (virtualChildAt2 == null) {
                    this.mTotalLength += measureNullChild(i19);
                } else if (virtualChildAt2.getVisibility() == 8) {
                    i19 += getChildrenSkipCount(virtualChildAt2, i19);
                } else {
                    LayoutParams layoutParams2 = (LayoutParams) virtualChildAt2.getLayoutParams();
                    int i20 = this.mTotalLength;
                    this.mTotalLength = Math.max(i20, i20 + i13 + layoutParams2.topMargin + layoutParams2.bottomMargin + getNextLocationOffset(virtualChildAt2));
                }
                i19++;
            }
        }
        this.mTotalLength += getPaddingTop() + getPaddingBottom();
        int resolveSizeAndState = ViewCompat.resolveSizeAndState(Math.max(this.mTotalLength, getSuggestedMinimumHeight()), i2, 0);
        int i21 = (resolveSizeAndState & 16777215) - this.mTotalLength;
        if (z6 || (i21 != 0 && f > 0.0f)) {
            if (this.mWeightSum > 0.0f) {
                f = this.mWeightSum;
            }
            this.mTotalLength = 0;
            int i22 = 0;
            int i23 = i8;
            while (i22 < virtualChildCount) {
                View virtualChildAt3 = getVirtualChildAt(i22);
                if (virtualChildAt3.getVisibility() == 8) {
                    i6 = i21;
                    i5 = i9;
                    max = i10;
                    z2 = z4;
                } else {
                    LayoutParams layoutParams3 = (LayoutParams) virtualChildAt3.getLayoutParams();
                    float f2 = layoutParams3.weight;
                    i5 = i9;
                    i6 = i21;
                    float f3 = f;
                    if (f2 > 0.0f) {
                        int i24 = (int) ((i21 * f2) / f);
                        f3 = f - f2;
                        int i25 = i21 - i24;
                        int childMeasureSpec = getChildMeasureSpec(i, getPaddingLeft() + getPaddingRight() + layoutParams3.leftMargin + layoutParams3.rightMargin, layoutParams3.width);
                        if (layoutParams3.height == 0 && mode2 == 1073741824) {
                            if (i24 <= 0) {
                                i24 = 0;
                            }
                            virtualChildAt3.measure(childMeasureSpec, View.MeasureSpec.makeMeasureSpec(i24, 1073741824));
                        } else {
                            int measuredHeight2 = virtualChildAt3.getMeasuredHeight() + i24;
                            int i26 = measuredHeight2;
                            if (measuredHeight2 < 0) {
                                i26 = 0;
                            }
                            virtualChildAt3.measure(childMeasureSpec, View.MeasureSpec.makeMeasureSpec(i26, 1073741824));
                        }
                        i6 = i25;
                        i5 = ViewUtils.combineMeasuredStates(i9, ViewCompat.getMeasuredState(virtualChildAt3) & (-256));
                    }
                    int i27 = layoutParams3.leftMargin + layoutParams3.rightMargin;
                    int measuredWidth2 = virtualChildAt3.getMeasuredWidth() + i27;
                    i23 = Math.max(i23, measuredWidth2);
                    max = Math.max(i10, mode != 1073741824 ? layoutParams3.width == -1 : false ? i27 : measuredWidth2);
                    z2 = z4 && layoutParams3.width == -1;
                    int i28 = this.mTotalLength;
                    this.mTotalLength = Math.max(i28, virtualChildAt3.getMeasuredHeight() + i28 + layoutParams3.topMargin + layoutParams3.bottomMargin + getNextLocationOffset(virtualChildAt3));
                    f = f3;
                }
                i22++;
                z4 = z2;
                i10 = max;
                i9 = i5;
                i21 = i6;
            }
            this.mTotalLength += getPaddingTop() + getPaddingBottom();
            i3 = i23;
            i4 = i9;
            z = z4;
        } else {
            int max2 = Math.max(i10, i11);
            z = z4;
            i10 = max2;
            i4 = i9;
            i3 = i8;
            if (z7) {
                z = z4;
                i10 = max2;
                i4 = i9;
                i3 = i8;
                if (mode2 != 1073741824) {
                    int i29 = 0;
                    while (true) {
                        z = z4;
                        i10 = max2;
                        i4 = i9;
                        i3 = i8;
                        if (i29 >= virtualChildCount) {
                            break;
                        }
                        View virtualChildAt4 = getVirtualChildAt(i29);
                        if (virtualChildAt4 != null && virtualChildAt4.getVisibility() != 8 && ((LayoutParams) virtualChildAt4.getLayoutParams()).weight > 0.0f) {
                            virtualChildAt4.measure(View.MeasureSpec.makeMeasureSpec(virtualChildAt4.getMeasuredWidth(), 1073741824), View.MeasureSpec.makeMeasureSpec(i13, 1073741824));
                        }
                        i29++;
                    }
                }
            }
        }
        int i30 = i3;
        if (!z) {
            i30 = i3;
            if (mode != 1073741824) {
                i30 = i10;
            }
        }
        setMeasuredDimension(ViewCompat.resolveSizeAndState(Math.max(i30 + getPaddingLeft() + getPaddingRight(), getSuggestedMinimumWidth()), i, i4), resolveSizeAndState);
        if (z5) {
            forceUniformWidth(virtualChildCount, i2);
        }
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

    @Override // android.view.View
    public void onInitializeAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        if (Build.VERSION.SDK_INT >= 14) {
            super.onInitializeAccessibilityEvent(accessibilityEvent);
            accessibilityEvent.setClassName(LinearLayoutCompat.class.getName());
        }
    }

    @Override // android.view.View
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        if (Build.VERSION.SDK_INT >= 14) {
            super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
            accessibilityNodeInfo.setClassName(LinearLayoutCompat.class.getName());
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.view.ViewGroup, android.view.View
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        if (this.mOrientation == 1) {
            layoutVertical(i, i2, i3, i4);
        } else {
            layoutHorizontal(i, i2, i3, i4);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.view.View
    public void onMeasure(int i, int i2) {
        if (this.mOrientation == 1) {
            measureVertical(i, i2);
        } else {
            measureHorizontal(i, i2);
        }
    }

    public void setBaselineAligned(boolean z) {
        this.mBaselineAligned = z;
    }

    public void setDividerDrawable(Drawable drawable) {
        boolean z = false;
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
        if (drawable == null) {
            z = true;
        }
        setWillNotDraw(z);
        requestLayout();
    }

    public void setGravity(int i) {
        if (this.mGravity != i) {
            int i2 = i;
            if ((8388615 & i) == 0) {
                i2 = i | 8388611;
            }
            int i3 = i2;
            if ((i2 & 112) == 0) {
                i3 = i2 | 48;
            }
            this.mGravity = i3;
            requestLayout();
        }
    }

    public void setOrientation(int i) {
        if (this.mOrientation != i) {
            this.mOrientation = i;
            requestLayout();
        }
    }

    @Override // android.view.ViewGroup
    public boolean shouldDelayChildPressedState() {
        return false;
    }
}
