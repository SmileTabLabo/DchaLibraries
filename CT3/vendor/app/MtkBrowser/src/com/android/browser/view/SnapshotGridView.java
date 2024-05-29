package com.android.browser.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.GridView;
/* loaded from: b.zip:com/android/browser/view/SnapshotGridView.class */
public class SnapshotGridView extends GridView {
    private int mColWidth;

    public SnapshotGridView(Context context) {
        super(context);
    }

    public SnapshotGridView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public SnapshotGridView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    @Override // android.widget.GridView, android.widget.AbsListView, android.view.View
    protected void onMeasure(int i, int i2) {
        int size = View.MeasureSpec.getSize(i);
        int mode = View.MeasureSpec.getMode(i);
        int i3 = i;
        if (size > 0) {
            i3 = i;
            if (this.mColWidth > 0) {
                i3 = View.MeasureSpec.makeMeasureSpec(Math.min(Math.min(size / this.mColWidth, 5) * this.mColWidth, size), mode);
            }
        }
        super.onMeasure(i3, i2);
    }

    @Override // android.widget.GridView
    public void setColumnWidth(int i) {
        this.mColWidth = i;
        super.setColumnWidth(i);
    }
}
