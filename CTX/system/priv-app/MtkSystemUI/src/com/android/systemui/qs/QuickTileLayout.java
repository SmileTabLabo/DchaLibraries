package com.android.systemui.qs;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
/* loaded from: classes.dex */
public class QuickTileLayout extends LinearLayout {
    public QuickTileLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setGravity(17);
    }

    @Override // android.view.ViewGroup
    public void addView(View view, int i, ViewGroup.LayoutParams layoutParams) {
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(layoutParams.height, layoutParams.height);
        layoutParams2.weight = 1.0f;
        super.addView(view, i, layoutParams2);
    }
}
