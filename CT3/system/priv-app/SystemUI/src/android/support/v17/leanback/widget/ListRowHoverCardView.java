package android.support.v17.leanback.widget;

import android.content.Context;
import android.support.v17.leanback.R$id;
import android.support.v17.leanback.R$layout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
/* loaded from: a.zip:android/support/v17/leanback/widget/ListRowHoverCardView.class */
public final class ListRowHoverCardView extends LinearLayout {
    private final TextView mDescriptionView;
    private final TextView mTitleView;

    public ListRowHoverCardView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public ListRowHoverCardView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        LayoutInflater.from(context).inflate(R$layout.lb_list_row_hovercard, this);
        this.mTitleView = (TextView) findViewById(R$id.title);
        this.mDescriptionView = (TextView) findViewById(R$id.description);
    }
}
