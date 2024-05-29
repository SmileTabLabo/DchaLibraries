package android.support.v17.leanback.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
/* loaded from: a.zip:android/support/v17/leanback/widget/NonOverlappingLinearLayout.class */
class NonOverlappingLinearLayout extends LinearLayout {
    public NonOverlappingLinearLayout(Context context) {
        this(context, null);
    }

    public NonOverlappingLinearLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public NonOverlappingLinearLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }
}
