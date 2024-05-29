package com.android.systemui.statusbar;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
/* loaded from: a.zip:com/android/systemui/statusbar/AlphaOptimizedFrameLayout.class */
public class AlphaOptimizedFrameLayout extends FrameLayout {
    public AlphaOptimizedFrameLayout(Context context) {
        super(context);
    }

    public AlphaOptimizedFrameLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public AlphaOptimizedFrameLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public AlphaOptimizedFrameLayout(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
    }

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }
}
