package android.support.v17.leanback.widget;

import android.content.Context;
import android.support.v17.leanback.R;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
/* loaded from: classes.dex */
public final class ListRowView extends LinearLayout {
    private HorizontalGridView mGridView;

    public ListRowView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ListRowView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.lb_list_row, this);
        this.mGridView = (HorizontalGridView) findViewById(R.id.row_content);
        this.mGridView.setHasFixedSize(false);
        setOrientation(1);
        setDescendantFocusability(262144);
    }
}
