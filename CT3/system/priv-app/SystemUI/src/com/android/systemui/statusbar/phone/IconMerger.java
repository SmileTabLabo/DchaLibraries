package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
/* loaded from: a.zip:com/android/systemui/statusbar/phone/IconMerger.class */
public class IconMerger extends LinearLayout {
    private int mIconHPadding;
    private int mIconSize;
    private View mMoreView;

    public IconMerger(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        reloadDimens();
    }

    private void checkOverflow(int i) {
        boolean z = true;
        if (this.mMoreView == null) {
            return;
        }
        int childCount = getChildCount();
        int i2 = 0;
        int i3 = 0;
        while (i3 < childCount) {
            int i4 = i2;
            if (getChildAt(i3).getVisibility() != 8) {
                i4 = i2 + 1;
            }
            i3++;
            i2 = i4;
        }
        boolean z2 = this.mMoreView.getVisibility() == 0;
        int i5 = i2;
        if (z2) {
            i5 = i2 - 1;
        }
        if (getFullIconWidth() * i5 <= i) {
            z = false;
        }
        if (z != z2) {
            post(new Runnable(this, z) { // from class: com.android.systemui.statusbar.phone.IconMerger.1
                final IconMerger this$0;
                final boolean val$moreRequired;

                {
                    this.this$0 = this;
                    this.val$moreRequired = z;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$0.mMoreView.setVisibility(this.val$moreRequired ? 0 : 8);
                }
            });
        }
    }

    private int getFullIconWidth() {
        return this.mIconSize + (this.mIconHPadding * 2);
    }

    private void reloadDimens() {
        Resources resources = this.mContext.getResources();
        this.mIconSize = resources.getDimensionPixelSize(2131689776);
        this.mIconHPadding = resources.getDimensionPixelSize(2131689796);
    }

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        reloadDimens();
    }

    @Override // android.widget.LinearLayout, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        checkOverflow(i3 - i);
    }

    @Override // android.widget.LinearLayout, android.view.View
    protected void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        int measuredWidth = getMeasuredWidth();
        setMeasuredDimension(measuredWidth - (measuredWidth % getFullIconWidth()), getMeasuredHeight());
    }

    public void setOverflowIndicator(View view) {
        this.mMoreView = view;
    }
}
