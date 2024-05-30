package com.android.systemui.qs;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.android.systemui.R;
import java.lang.ref.WeakReference;
/* loaded from: classes.dex */
public class PseudoGridView extends ViewGroup {
    private int mHorizontalSpacing;
    private int mNumColumns;
    private int mVerticalSpacing;

    public PseudoGridView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mNumColumns = 3;
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.PseudoGridView);
        int indexCount = obtainStyledAttributes.getIndexCount();
        for (int i = 0; i < indexCount; i++) {
            int index = obtainStyledAttributes.getIndex(i);
            switch (index) {
                case 0:
                    this.mHorizontalSpacing = obtainStyledAttributes.getDimensionPixelSize(index, 0);
                    break;
                case 1:
                    this.mNumColumns = obtainStyledAttributes.getInt(index, 3);
                    break;
                case 2:
                    this.mVerticalSpacing = obtainStyledAttributes.getDimensionPixelSize(index, 0);
                    break;
            }
        }
        obtainStyledAttributes.recycle();
    }

    @Override // android.view.View
    protected void onMeasure(int i, int i2) {
        if (View.MeasureSpec.getMode(i) == 0) {
            throw new UnsupportedOperationException("Needs a maximum width");
        }
        int size = View.MeasureSpec.getSize(i);
        int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec((size - ((this.mNumColumns - 1) * this.mHorizontalSpacing)) / this.mNumColumns, 1073741824);
        int childCount = getChildCount();
        int i3 = ((this.mNumColumns + childCount) - 1) / this.mNumColumns;
        int i4 = 0;
        for (int i5 = 0; i5 < i3; i5++) {
            int i6 = this.mNumColumns * i5;
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
            i4 += i7;
            if (i5 > 0) {
                i4 += this.mVerticalSpacing;
            }
        }
        setMeasuredDimension(size, resolveSizeAndState(i4, i2, 0));
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        boolean isLayoutRtl = isLayoutRtl();
        int childCount = getChildCount();
        int i5 = ((this.mNumColumns + childCount) - 1) / this.mNumColumns;
        int i6 = 0;
        for (int i7 = 0; i7 < i5; i7++) {
            int width = isLayoutRtl ? getWidth() : 0;
            int i8 = this.mNumColumns * i7;
            int min = Math.min(this.mNumColumns + i8, childCount);
            int i9 = width;
            int i10 = 0;
            while (i8 < min) {
                View childAt = getChildAt(i8);
                int measuredWidth = childAt.getMeasuredWidth();
                int measuredHeight = childAt.getMeasuredHeight();
                if (isLayoutRtl) {
                    i9 -= measuredWidth;
                }
                childAt.layout(i9, i6, i9 + measuredWidth, i6 + measuredHeight);
                i10 = Math.max(i10, measuredHeight);
                if (isLayoutRtl) {
                    i9 -= this.mHorizontalSpacing;
                } else {
                    i9 += measuredWidth + this.mHorizontalSpacing;
                }
                i8++;
            }
            i6 += i10;
            if (i7 > 0) {
                i6 += this.mVerticalSpacing;
            }
        }
    }

    /* loaded from: classes.dex */
    public static class ViewGroupAdapterBridge extends DataSetObserver {
        private final BaseAdapter mAdapter;
        private boolean mReleased = false;
        private final WeakReference<ViewGroup> mViewGroup;

        public static void link(ViewGroup viewGroup, BaseAdapter baseAdapter) {
            new ViewGroupAdapterBridge(viewGroup, baseAdapter);
        }

        private ViewGroupAdapterBridge(ViewGroup viewGroup, BaseAdapter baseAdapter) {
            this.mViewGroup = new WeakReference<>(viewGroup);
            this.mAdapter = baseAdapter;
            this.mAdapter.registerDataSetObserver(this);
            refresh();
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
                    View view = null;
                    if (i < childCount) {
                        view = viewGroup.getChildAt(i);
                    }
                    View view2 = this.mAdapter.getView(i, view, viewGroup);
                    if (view == null) {
                        viewGroup.addView(view2);
                    } else if (view != view2) {
                        viewGroup.removeViewAt(i);
                        viewGroup.addView(view2, i);
                    }
                } else {
                    viewGroup.removeViewAt(viewGroup.getChildCount() - 1);
                }
            }
        }

        @Override // android.database.DataSetObserver
        public void onChanged() {
            refresh();
        }

        @Override // android.database.DataSetObserver
        public void onInvalidated() {
            release();
        }

        private void release() {
            if (!this.mReleased) {
                this.mReleased = true;
                this.mAdapter.unregisterDataSetObserver(this);
            }
        }
    }
}
