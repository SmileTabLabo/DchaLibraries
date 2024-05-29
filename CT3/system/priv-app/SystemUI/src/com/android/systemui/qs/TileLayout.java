package com.android.systemui.qs;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import com.android.systemui.qs.QSPanel;
import java.util.ArrayList;
/* loaded from: a.zip:com/android/systemui/qs/TileLayout.class */
public class TileLayout extends ViewGroup implements QSPanel.QSTileLayout {
    protected int mCellHeight;
    protected int mCellMargin;
    private int mCellMarginTop;
    protected int mCellWidth;
    protected int mColumns;
    private boolean mListening;
    protected final ArrayList<QSPanel.TileRecord> mRecords;

    public TileLayout(Context context) {
        this(context, null);
    }

    public TileLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mRecords = new ArrayList<>();
        setFocusableInTouchMode(true);
        updateResources();
    }

    private static int exactly(int i) {
        return View.MeasureSpec.makeMeasureSpec(i, 1073741824);
    }

    private int getColumnStart(int i) {
        return ((this.mCellWidth + this.mCellMargin) * i) + this.mCellMargin;
    }

    private int getRowTop(int i) {
        return ((this.mCellHeight + this.mCellMargin) * i) + this.mCellMarginTop;
    }

    @Override // com.android.systemui.qs.QSPanel.QSTileLayout
    public void addTile(QSPanel.TileRecord tileRecord) {
        this.mRecords.add(tileRecord);
        tileRecord.tile.setListening(this, this.mListening);
        addView(tileRecord.tileView);
    }

    @Override // com.android.systemui.qs.QSPanel.QSTileLayout
    public int getOffsetTop(QSPanel.TileRecord tileRecord) {
        return getTop();
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int i5;
        int width = getWidth();
        boolean z2 = getLayoutDirection() == 1;
        int i6 = 0;
        int i7 = 0;
        int i8 = 0;
        while (i8 < this.mRecords.size()) {
            int i9 = i7;
            int i10 = i6;
            if (i7 == this.mColumns) {
                i10 = i6 + 1;
                i9 = i7 - this.mColumns;
            }
            QSPanel.TileRecord tileRecord = this.mRecords.get(i8);
            int columnStart = getColumnStart(i9);
            int rowTop = getRowTop(i10);
            if (z2) {
                i5 = width - columnStart;
                columnStart = i5 - this.mCellWidth;
            } else {
                i5 = columnStart + this.mCellWidth;
            }
            tileRecord.tileView.layout(columnStart, rowTop, i5, tileRecord.tileView.getMeasuredHeight() + rowTop);
            i8++;
            i7 = i9 + 1;
            i6 = i10;
        }
    }

    @Override // android.view.View
    protected void onMeasure(int i, int i2) {
        int size = this.mRecords.size();
        int size2 = View.MeasureSpec.getSize(i);
        int i3 = ((this.mColumns + size) - 1) / this.mColumns;
        this.mCellWidth = (size2 - (this.mCellMargin * (this.mColumns + 1))) / this.mColumns;
        TileLayout tileLayout = this;
        for (QSPanel.TileRecord tileRecord : this.mRecords) {
            if (tileRecord.tileView.getVisibility() != 8) {
                tileRecord.tileView.measure(exactly(this.mCellWidth), exactly(this.mCellHeight));
                tileLayout = tileRecord.tileView.updateAccessibilityOrder(tileLayout);
            }
        }
        setMeasuredDimension(size2, ((this.mCellHeight + this.mCellMargin) * i3) + (this.mCellMarginTop - this.mCellMargin));
    }

    @Override // android.view.ViewGroup
    public void removeAllViews() {
        for (QSPanel.TileRecord tileRecord : this.mRecords) {
            tileRecord.tile.setListening(this, false);
        }
        this.mRecords.clear();
        super.removeAllViews();
    }

    @Override // com.android.systemui.qs.QSPanel.QSTileLayout
    public void removeTile(QSPanel.TileRecord tileRecord) {
        this.mRecords.remove(tileRecord);
        tileRecord.tile.setListening(this, false);
        removeView(tileRecord.tileView);
    }

    @Override // com.android.systemui.qs.QSPanel.QSTileLayout
    public void setListening(boolean z) {
        if (this.mListening == z) {
            return;
        }
        this.mListening = z;
        for (QSPanel.TileRecord tileRecord : this.mRecords) {
            tileRecord.tile.setListening(this, this.mListening);
        }
    }

    public boolean updateResources() {
        Resources resources = this.mContext.getResources();
        int max = Math.max(1, resources.getInteger(2131755052));
        this.mCellHeight = this.mContext.getResources().getDimensionPixelSize(2131689823);
        this.mCellMargin = resources.getDimensionPixelSize(2131689824);
        this.mCellMarginTop = resources.getDimensionPixelSize(2131689825);
        if (this.mColumns != max) {
            this.mColumns = max;
            requestLayout();
            return true;
        }
        return false;
    }
}
