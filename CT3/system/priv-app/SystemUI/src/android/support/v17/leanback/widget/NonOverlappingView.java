package android.support.v17.leanback.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
/* loaded from: a.zip:android/support/v17/leanback/widget/NonOverlappingView.class */
class NonOverlappingView extends View {
    public NonOverlappingView(Context context) {
        this(context, null);
    }

    public NonOverlappingView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet, 0);
    }

    public NonOverlappingView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }
}
