package com.android.setupwizardlib.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.android.settingslib.wifi.AccessPoint;
import com.android.setupwizardlib.R;
/* loaded from: classes.dex */
public class FillContentLayout extends FrameLayout {
    private int mMaxHeight;
    private int mMaxWidth;

    public FillContentLayout(Context context) {
        this(context, null);
    }

    public FillContentLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, R.attr.suwFillContentLayoutStyle);
    }

    public FillContentLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init(context, attributeSet, i);
    }

    private void init(Context context, AttributeSet attributeSet, int i) {
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.SuwFillContentLayout, i, 0);
        this.mMaxHeight = obtainStyledAttributes.getDimensionPixelSize(R.styleable.SuwFillContentLayout_android_maxHeight, -1);
        this.mMaxWidth = obtainStyledAttributes.getDimensionPixelSize(R.styleable.SuwFillContentLayout_android_maxWidth, -1);
        obtainStyledAttributes.recycle();
    }

    @Override // android.widget.FrameLayout, android.view.View
    protected void onMeasure(int i, int i2) {
        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), i), getDefaultSize(getSuggestedMinimumHeight(), i2));
        int childCount = getChildCount();
        for (int i3 = 0; i3 < childCount; i3++) {
            measureIllustrationChild(getChildAt(i3), getMeasuredWidth(), getMeasuredHeight());
        }
    }

    private void measureIllustrationChild(View view, int i, int i2) {
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        view.measure(getMaxSizeMeasureSpec(Math.min(this.mMaxWidth, i), getPaddingLeft() + getPaddingRight() + marginLayoutParams.leftMargin + marginLayoutParams.rightMargin, marginLayoutParams.width), getMaxSizeMeasureSpec(Math.min(this.mMaxHeight, i2), getPaddingTop() + getPaddingBottom() + marginLayoutParams.topMargin + marginLayoutParams.bottomMargin, marginLayoutParams.height));
    }

    private static int getMaxSizeMeasureSpec(int i, int i2, int i3) {
        int max = Math.max(0, i - i2);
        if (i3 >= 0) {
            return View.MeasureSpec.makeMeasureSpec(i3, 1073741824);
        }
        if (i3 == -1) {
            return View.MeasureSpec.makeMeasureSpec(max, 1073741824);
        }
        if (i3 == -2) {
            return View.MeasureSpec.makeMeasureSpec(max, AccessPoint.UNREACHABLE_RSSI);
        }
        return 0;
    }
}
