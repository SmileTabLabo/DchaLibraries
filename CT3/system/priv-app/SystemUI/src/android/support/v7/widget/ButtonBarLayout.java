package android.support.v7.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.v4.content.res.ConfigurationHelper;
import android.support.v4.view.ViewCompat;
import android.support.v7.appcompat.R$id;
import android.support.v7.appcompat.R$styleable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
/* loaded from: a.zip:android/support/v7/widget/ButtonBarLayout.class */
public class ButtonBarLayout extends LinearLayout {
    private boolean mAllowStacking;
    private int mLastWidthSize;

    public ButtonBarLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mLastWidthSize = -1;
        boolean z = ConfigurationHelper.getScreenHeightDp(getResources()) >= 320;
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.ButtonBarLayout);
        this.mAllowStacking = obtainStyledAttributes.getBoolean(R$styleable.ButtonBarLayout_allowStacking, z);
        obtainStyledAttributes.recycle();
    }

    private boolean isStacked() {
        boolean z = true;
        if (getOrientation() != 1) {
            z = false;
        }
        return z;
    }

    private void setStacked(boolean z) {
        setOrientation(z ? 1 : 0);
        setGravity(z ? 5 : 80);
        View findViewById = findViewById(R$id.spacer);
        if (findViewById != null) {
            findViewById.setVisibility(z ? 8 : 4);
        }
        for (int childCount = getChildCount() - 2; childCount >= 0; childCount--) {
            bringChildToFront(getChildAt(childCount));
        }
    }

    @Override // android.widget.LinearLayout, android.view.View
    protected void onMeasure(int i, int i2) {
        int i3;
        boolean z;
        int size = View.MeasureSpec.getSize(i);
        if (this.mAllowStacking) {
            if (size > this.mLastWidthSize && isStacked()) {
                setStacked(false);
            }
            this.mLastWidthSize = size;
        }
        boolean z2 = false;
        if (isStacked() || View.MeasureSpec.getMode(i) != 1073741824) {
            i3 = i;
        } else {
            i3 = View.MeasureSpec.makeMeasureSpec(size, Integer.MIN_VALUE);
            z2 = true;
        }
        super.onMeasure(i3, i2);
        boolean z3 = z2;
        if (this.mAllowStacking) {
            if (isStacked()) {
                z3 = z2;
            } else {
                if (Build.VERSION.SDK_INT >= 11) {
                    z = (ViewCompat.getMeasuredWidthAndState(this) & (-16777216)) == 16777216;
                } else {
                    int i4 = 0;
                    int childCount = getChildCount();
                    for (int i5 = 0; i5 < childCount; i5++) {
                        i4 += getChildAt(i5).getMeasuredWidth();
                    }
                    z = (getPaddingLeft() + i4) + getPaddingRight() > size;
                }
                z3 = z2;
                if (z) {
                    setStacked(true);
                    z3 = true;
                }
            }
        }
        if (z3) {
            super.onMeasure(i, i2);
        }
    }
}
