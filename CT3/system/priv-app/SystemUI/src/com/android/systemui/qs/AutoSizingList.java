package com.android.systemui.qs;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import com.android.systemui.R$styleable;
/* loaded from: a.zip:com/android/systemui/qs/AutoSizingList.class */
public class AutoSizingList extends LinearLayout {
    private ListAdapter mAdapter;
    private final Runnable mBindChildren;
    private int mCount;
    private final DataSetObserver mDataObserver;
    private final Handler mHandler;
    private final int mItemSize;

    public AutoSizingList(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mBindChildren = new Runnable(this) { // from class: com.android.systemui.qs.AutoSizingList.1
            final AutoSizingList this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.rebindChildren();
            }
        };
        this.mDataObserver = new DataSetObserver(this) { // from class: com.android.systemui.qs.AutoSizingList.2
            final AutoSizingList this$0;

            {
                this.this$0 = this;
            }

            @Override // android.database.DataSetObserver
            public void onChanged() {
                if (this.this$0.mCount > this.this$0.getDesiredCount()) {
                    this.this$0.mCount = this.this$0.getDesiredCount();
                }
                this.this$0.postRebindChildren();
            }

            @Override // android.database.DataSetObserver
            public void onInvalidated() {
                this.this$0.postRebindChildren();
            }
        };
        this.mHandler = new Handler();
        this.mItemSize = context.obtainStyledAttributes(attributeSet, R$styleable.AutoSizingList).getDimensionPixelSize(0, 0);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int getDesiredCount() {
        return this.mAdapter != null ? this.mAdapter.getCount() : 0;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void postRebindChildren() {
        this.mHandler.post(this.mBindChildren);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void rebindChildren() {
        if (this.mAdapter == null) {
            return;
        }
        int i = 0;
        while (i < this.mCount) {
            View childAt = i < getChildCount() ? getChildAt(i) : null;
            View view = this.mAdapter.getView(i, childAt, this);
            if (view != childAt) {
                if (childAt != null) {
                    removeView(childAt);
                }
                addView(view, i);
            }
            i++;
        }
        while (getChildCount() > this.mCount) {
            removeViewAt(getChildCount() - 1);
        }
    }

    @Override // android.widget.LinearLayout, android.view.View
    protected void onMeasure(int i, int i2) {
        int min;
        int size = View.MeasureSpec.getSize(i2);
        if (size != 0 && this.mCount != (min = Math.min(size / this.mItemSize, getDesiredCount()))) {
            postRebindChildren();
            this.mCount = min;
        }
        super.onMeasure(i, i2);
    }

    public void setAdapter(ListAdapter listAdapter) {
        if (this.mAdapter != null) {
            this.mAdapter.unregisterDataSetObserver(this.mDataObserver);
        }
        this.mAdapter = listAdapter;
        if (listAdapter != null) {
            listAdapter.registerDataSetObserver(this.mDataObserver);
        }
    }
}
