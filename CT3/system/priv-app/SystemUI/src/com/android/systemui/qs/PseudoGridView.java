package com.android.systemui.qs;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.android.systemui.R$styleable;
import java.lang.ref.WeakReference;
/* loaded from: a.zip:com/android/systemui/qs/PseudoGridView.class */
public class PseudoGridView extends ViewGroup {
    private int mHorizontalSpacing;
    private int mNumColumns;
    private int mVerticalSpacing;

    /* loaded from: a.zip:com/android/systemui/qs/PseudoGridView$ViewGroupAdapterBridge.class */
    public static class ViewGroupAdapterBridge extends DataSetObserver {
        private final BaseAdapter mAdapter;
        private boolean mReleased = false;
        private final WeakReference<ViewGroup> mViewGroup;

        private ViewGroupAdapterBridge(ViewGroup viewGroup, BaseAdapter baseAdapter) {
            this.mViewGroup = new WeakReference<>(viewGroup);
            this.mAdapter = baseAdapter;
            this.mAdapter.registerDataSetObserver(this);
            refresh();
        }

        public static void link(ViewGroup viewGroup, BaseAdapter baseAdapter) {
            new ViewGroupAdapterBridge(viewGroup, baseAdapter);
        }

        private void refresh() {
            if (this.mReleased) {
                return;
            }
            ViewGroup viewGroup = this.mViewGroup.get();
            if (viewGroup == null) {
                release();
                return;
            }
            int childCount = viewGroup.getChildCount();
            int count = this.mAdapter.getCount();
            int max = Math.max(childCount, count);
            for (int i = 0; i < max; i++) {
                if (i < count) {
                    View childAt = i < childCount ? viewGroup.getChildAt(i) : null;
                    View view = this.mAdapter.getView(i, childAt, viewGroup);
                    if (childAt == null) {
                        viewGroup.addView(view);
                    } else if (childAt != view) {
                        viewGroup.removeViewAt(i);
                        viewGroup.addView(view, i);
                    }
                } else {
                    viewGroup.removeViewAt(viewGroup.getChildCount() - 1);
                }
            }
        }

        private void release() {
            if (this.mReleased) {
                return;
            }
            this.mReleased = true;
            this.mAdapter.unregisterDataSetObserver(this);
        }

        @Override // android.database.DataSetObserver
        public void onChanged() {
            refresh();
        }

        @Override // android.database.DataSetObserver
        public void onInvalidated() {
            release();
        }
    }

    public PseudoGridView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mNumColumns = 3;
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.PseudoGridView);
        int indexCount = obtainStyledAttributes.getIndexCount();
        for (int i = 0; i < indexCount; i++) {
            int index = obtainStyledAttributes.getIndex(i);
            switch (index) {
                case 0:
                    this.mNumColumns = obtainStyledAttributes.getInt(index, 3);
                    break;
                case 1:
                    this.mVerticalSpacing = obtainStyledAttributes.getDimensionPixelSize(index, 0);
                    break;
                case 2:
                    this.mHorizontalSpacing = obtainStyledAttributes.getDimensionPixelSize(index, 0);
                    break;
            }
        }
        obtainStyledAttributes.recycle();
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        boolean isLayoutRtl = isLayoutRtl();
        int childCount = getChildCount();
        int i5 = ((this.mNumColumns + childCount) - 1) / this.mNumColumns;
        int i6 = 0;
        int i7 = 0;
        while (i7 < i5) {
            int width = isLayoutRtl ? getWidth() : 0;
            int i8 = 0;
            int i9 = i7 * this.mNumColumns;
            int min = Math.min(this.mNumColumns + i9, childCount);
            while (i9 < min) {
                View childAt = getChildAt(i9);
                int measuredWidth = childAt.getMeasuredWidth();
                int measuredHeight = childAt.getMeasuredHeight();
                int i10 = width;
                if (isLayoutRtl) {
                    i10 = width - measuredWidth;
                }
                childAt.layout(i10, i6, i10 + measuredWidth, i6 + measuredHeight);
                i8 = Math.max(i8, measuredHeight);
                width = isLayoutRtl ? i10 - this.mHorizontalSpacing : i10 + this.mHorizontalSpacing + measuredWidth;
                i9++;
            }
            int i11 = i6 + i8;
            int i12 = i11;
            if (i7 > 0) {
                i12 = i11 + this.mVerticalSpacing;
            }
            i7++;
            i6 = i12;
        }
    }

    @Override // android.view.View
    protected void onMeasure(int i, int i2) {
        if (View.MeasureSpec.getMode(i) == 0) {
            throw new UnsupportedOperationException("Needs a maximum width");
        }
        int size = View.MeasureSpec.getSize(i);
        int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec((size - ((this.mNumColumns - 1) * this.mHorizontalSpacing)) / this.mNumColumns, 1073741824);
        int i3 = 0;
        int childCount = getChildCount();
        int i4 = ((this.mNumColumns + childCount) - 1) / this.mNumColumns;
        for (int i5 = 0; i5 < i4; i5++) {
            int i6 = i5 * this.mNumColumns;
            int min = Math.min(this.mNumColumns + i6, childCount);
            int i7 = 0;
            for (int i8 = i6; i8 < min; i8++) {
                View childAt = getChildAt(i8);
                childAt.measure(makeMeasureSpec, 0);
                i7 = Math.max(i7, childAt.getMeasuredHeight());
            }
            int makeMeasureSpec2 = View.MeasureSpec.makeMeasureSpec(i7, 1073741824);
            while (i6 < min) {
                View childAt2 = getChildAt(i6);
                if (childAt2.getMeasuredHeight() != i7) {
                    childAt2.measure(makeMeasureSpec, makeMeasureSpec2);
                }
                i6++;
            }
            int i9 = i3 + i7;
            i3 = i9;
            if (i5 > 0) {
                i3 = i9 + this.mVerticalSpacing;
            }
        }
        setMeasuredDimension(size, resolveSizeAndState(i3, i2, 0));
    }
}
