package com.android.keyguard;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
/* loaded from: a.zip:com/android/keyguard/AlphaOptimizedLinearLayout.class */
public class AlphaOptimizedLinearLayout extends LinearLayout {
    public AlphaOptimizedLinearLayout(Context context) {
        super(context);
    }

    public AlphaOptimizedLinearLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public AlphaOptimizedLinearLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public AlphaOptimizedLinearLayout(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
    }

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }
}
