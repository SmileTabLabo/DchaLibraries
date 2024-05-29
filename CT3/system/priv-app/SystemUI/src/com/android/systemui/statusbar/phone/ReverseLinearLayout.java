package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import java.util.ArrayList;
/* loaded from: a.zip:com/android/systemui/statusbar/phone/ReverseLinearLayout.class */
public class ReverseLinearLayout extends LinearLayout {
    private boolean mIsLayoutRtl;

    public ReverseLinearLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    private void reversParams(ViewGroup.LayoutParams layoutParams) {
        if (layoutParams == null) {
            return;
        }
        int i = layoutParams.width;
        layoutParams.width = layoutParams.height;
        layoutParams.height = i;
    }

    private void updateRTLOrder() {
        boolean z = getResources().getConfiguration().getLayoutDirection() == 1;
        if (this.mIsLayoutRtl != z) {
            int childCount = getChildCount();
            ArrayList arrayList = new ArrayList(childCount);
            for (int i = 0; i < childCount; i++) {
                arrayList.add(getChildAt(i));
            }
            removeAllViews();
            for (int i2 = childCount - 1; i2 >= 0; i2--) {
                super.addView((View) arrayList.get(i2));
            }
            this.mIsLayoutRtl = z;
        }
    }

    @Override // android.view.ViewGroup
    public void addView(View view) {
        reversParams(view.getLayoutParams());
        if (this.mIsLayoutRtl) {
            super.addView(view);
        } else {
            super.addView(view, 0);
        }
    }

    @Override // android.view.ViewGroup, android.view.ViewManager
    public void addView(View view, ViewGroup.LayoutParams layoutParams) {
        reversParams(layoutParams);
        if (this.mIsLayoutRtl) {
            super.addView(view, layoutParams);
        } else {
            super.addView(view, 0, layoutParams);
        }
    }

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        updateRTLOrder();
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        boolean z = true;
        super.onFinishInflate();
        if (getResources().getConfiguration().getLayoutDirection() != 1) {
            z = false;
        }
        this.mIsLayoutRtl = z;
    }
}
