package com.android.systemui;

import android.R;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
/* loaded from: a.zip:com/android/systemui/ResizingSpace.class */
public class ResizingSpace extends View {
    private final int mHeight;
    private final int mWidth;

    public ResizingSpace(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (getVisibility() == 0) {
            setVisibility(4);
        }
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.ViewGroup_Layout);
        this.mWidth = obtainStyledAttributes.getResourceId(0, 0);
        this.mHeight = obtainStyledAttributes.getResourceId(1, 0);
    }

    private static int getDefaultSize2(int i, int i2) {
        int mode = View.MeasureSpec.getMode(i2);
        int size = View.MeasureSpec.getSize(i2);
        switch (mode) {
            case Integer.MIN_VALUE:
                i = Math.min(i, size);
                break;
            case 0:
                break;
            case 1073741824:
                i = size;
                break;
            default:
                i = i;
                break;
        }
        return i;
    }

    @Override // android.view.View
    public void draw(Canvas canvas) {
    }

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        boolean z = false;
        if (this.mWidth > 0) {
            int dimensionPixelOffset = getContext().getResources().getDimensionPixelOffset(this.mWidth);
            z = false;
            if (dimensionPixelOffset != layoutParams.width) {
                layoutParams.width = dimensionPixelOffset;
                z = true;
            }
        }
        boolean z2 = z;
        if (this.mHeight > 0) {
            int dimensionPixelOffset2 = getContext().getResources().getDimensionPixelOffset(this.mHeight);
            z2 = z;
            if (dimensionPixelOffset2 != layoutParams.height) {
                layoutParams.height = dimensionPixelOffset2;
                z2 = true;
            }
        }
        if (z2) {
            setLayoutParams(layoutParams);
        }
    }

    @Override // android.view.View
    protected void onMeasure(int i, int i2) {
        setMeasuredDimension(getDefaultSize2(getSuggestedMinimumWidth(), i), getDefaultSize2(getSuggestedMinimumHeight(), i2));
    }
}
