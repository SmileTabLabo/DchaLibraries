package android.support.v7.widget;

import android.content.Context;
import android.util.AttributeSet;
/* loaded from: classes.dex */
public class ActivityChooserView$InnerLayout extends LinearLayoutCompat {
    private static final int[] TINT_ATTRS = {16842964};

    public ActivityChooserView$InnerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TintTypedArray a = TintTypedArray.obtainStyledAttributes(context, attrs, TINT_ATTRS);
        setBackgroundDrawable(a.getDrawable(0));
        a.recycle();
    }
}
