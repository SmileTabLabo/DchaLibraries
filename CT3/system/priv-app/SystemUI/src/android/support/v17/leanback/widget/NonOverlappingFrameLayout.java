package android.support.v17.leanback.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
/* loaded from: a.zip:android/support/v17/leanback/widget/NonOverlappingFrameLayout.class */
class NonOverlappingFrameLayout extends FrameLayout {
    public NonOverlappingFrameLayout(Context context) {
        this(context, null);
    }

    public NonOverlappingFrameLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet, 0);
    }

    public NonOverlappingFrameLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }
}
