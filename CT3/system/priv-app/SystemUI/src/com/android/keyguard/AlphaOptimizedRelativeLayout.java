package com.android.keyguard;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
/* loaded from: a.zip:com/android/keyguard/AlphaOptimizedRelativeLayout.class */
public class AlphaOptimizedRelativeLayout extends RelativeLayout {
    public AlphaOptimizedRelativeLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }
}
