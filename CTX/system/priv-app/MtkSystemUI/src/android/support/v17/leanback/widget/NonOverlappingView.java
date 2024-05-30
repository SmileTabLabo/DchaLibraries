package android.support.v17.leanback.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
/* loaded from: classes.dex */
class NonOverlappingView extends View {
    public NonOverlappingView(Context context) {
        this(context, null);
    }

    public NonOverlappingView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public NonOverlappingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }
}
