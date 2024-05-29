package android.support.v17.leanback.widget;

import android.content.Context;
import android.support.v17.leanback.R$attr;
import android.util.AttributeSet;
import android.widget.TextView;
/* loaded from: a.zip:android/support/v17/leanback/widget/RowHeaderView.class */
public final class RowHeaderView extends TextView {
    public RowHeaderView(Context context) {
        this(context, null);
    }

    public RowHeaderView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, R$attr.rowHeaderStyle);
    }

    public RowHeaderView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }
}
