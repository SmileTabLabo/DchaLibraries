package com.android.browser;

import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.DateSorter;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
/* loaded from: b.zip:com/android/browser/DateSortedExpandableListAdapter.class */
public class DateSortedExpandableListAdapter extends BaseExpandableListAdapter {
    private Context mContext;
    private Cursor mCursor;
    private int mDateIndex;
    private DateSorter mDateSorter;
    private int[] mItemMap;
    private int mNumberOfBins;
    DataSetObserver mDataSetObserver = new DataSetObserver(this) { // from class: com.android.browser.DateSortedExpandableListAdapter.1
        final DateSortedExpandableListAdapter this$0;

        {
            this.this$0 = this;
        }

        @Override // android.database.DataSetObserver
        public void onChanged() {
            this.this$0.mDataValid = true;
            this.this$0.notifyDataSetChanged();
        }

        @Override // android.database.DataSetObserver
        public void onInvalidated() {
            this.this$0.mDataValid = false;
            this.this$0.notifyDataSetInvalidated();
        }
    };
    boolean mDataValid = false;
    private int mIdIndex = -1;

    public DateSortedExpandableListAdapter(Context context, int i) {
        this.mContext = context;
        this.mDateSorter = new DateSorter(context);
        this.mDateIndex = i;
    }

    private void buildMap() {
        int[] iArr = new int[5];
        for (int i = 0; i < 5; i++) {
            iArr[i] = 0;
        }
        this.mNumberOfBins = 0;
        int i2 = -1;
        if (this.mCursor.moveToFirst() && this.mCursor.getCount() > 0) {
            while (true) {
                if (this.mCursor.isAfterLast()) {
                    break;
                }
                int index = this.mDateSorter.getIndex(getLong(this.mDateIndex));
                int i3 = i2;
                if (index > i2) {
                    this.mNumberOfBins++;
                    if (index == 4) {
                        iArr[index] = this.mCursor.getCount() - this.mCursor.getPosition();
                        break;
                    }
                    i3 = index;
                }
                iArr[i3] = iArr[i3] + 1;
                this.mCursor.moveToNext();
                i2 = i3;
            }
        }
        this.mItemMap = iArr;
    }

    private int groupPositionToBin(int i) {
        if (this.mDataValid) {
            if (i < 0 || i >= 5) {
                throw new AssertionError("group position out of range");
            }
            if (5 == this.mNumberOfBins || this.mNumberOfBins == 0) {
                return i;
            }
            int i2 = -1;
            while (i > -1) {
                int i3 = i2 + 1;
                i2 = i3;
                if (this.mItemMap[i3] != 0) {
                    i--;
                    i2 = i3;
                }
            }
            return i2;
        }
        return -1;
    }

    @Override // android.widget.BaseExpandableListAdapter, android.widget.ExpandableListAdapter
    public boolean areAllItemsEnabled() {
        return true;
    }

    public void changeCursor(Cursor cursor) {
        if (cursor == this.mCursor) {
            return;
        }
        if (this.mCursor != null) {
            this.mCursor.unregisterDataSetObserver(this.mDataSetObserver);
            this.mCursor.close();
        }
        this.mCursor = cursor;
        if (cursor == null) {
            this.mIdIndex = -1;
            this.mDataValid = false;
            notifyDataSetInvalidated();
            return;
        }
        cursor.registerDataSetObserver(this.mDataSetObserver);
        this.mIdIndex = cursor.getColumnIndexOrThrow("_id");
        this.mDataValid = true;
        buildMap();
        notifyDataSetChanged();
    }

    @Override // android.widget.ExpandableListAdapter
    public Object getChild(int i, int i2) {
        return null;
    }

    @Override // android.widget.ExpandableListAdapter
    public long getChildId(int i, int i2) {
        if (this.mDataValid && moveCursorToChildPosition(i, i2)) {
            return getLong(this.mIdIndex);
        }
        return 0L;
    }

    @Override // android.widget.ExpandableListAdapter
    public View getChildView(int i, int i2, boolean z, View view, ViewGroup viewGroup) {
        if (this.mDataValid) {
            return null;
        }
        throw new IllegalStateException("Data is not valid");
    }

    @Override // android.widget.ExpandableListAdapter
    public int getChildrenCount(int i) {
        if (this.mDataValid) {
            return this.mItemMap[groupPositionToBin(i)];
        }
        return 0;
    }

    @Override // android.widget.BaseExpandableListAdapter, android.widget.ExpandableListAdapter
    public long getCombinedChildId(long j, long j2) {
        if (this.mDataValid) {
            return j2;
        }
        return 0L;
    }

    @Override // android.widget.BaseExpandableListAdapter, android.widget.ExpandableListAdapter
    public long getCombinedGroupId(long j) {
        if (this.mDataValid) {
            return j;
        }
        return 0L;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public Context getContext() {
        return this.mContext;
    }

    @Override // android.widget.ExpandableListAdapter
    public Object getGroup(int i) {
        return null;
    }

    @Override // android.widget.ExpandableListAdapter
    public int getGroupCount() {
        if (this.mDataValid) {
            return this.mNumberOfBins;
        }
        return 0;
    }

    @Override // android.widget.ExpandableListAdapter
    public long getGroupId(int i) {
        if (this.mDataValid) {
            return i;
        }
        return 0L;
    }

    @Override // android.widget.ExpandableListAdapter
    public View getGroupView(int i, boolean z, View view, ViewGroup viewGroup) {
        if (this.mDataValid) {
            TextView textView = (view == null || !(view instanceof TextView)) ? (TextView) LayoutInflater.from(this.mContext).inflate(2130968603, (ViewGroup) null) : (TextView) view;
            textView.setText(this.mDateSorter.getLabel(groupPositionToBin(i)));
            return textView;
        }
        throw new IllegalStateException("Data is not valid");
    }

    long getLong(int i) {
        if (this.mDataValid) {
            return this.mCursor.getLong(i);
        }
        return 0L;
    }

    @Override // android.widget.ExpandableListAdapter
    public boolean hasStableIds() {
        return true;
    }

    @Override // android.widget.ExpandableListAdapter
    public boolean isChildSelectable(int i, int i2) {
        return true;
    }

    @Override // android.widget.BaseExpandableListAdapter, android.widget.ExpandableListAdapter
    public boolean isEmpty() {
        boolean z = true;
        if (this.mDataValid) {
            if (this.mCursor == null) {
                z = true;
            } else {
                z = true;
                if (!this.mCursor.isClosed()) {
                    z = true;
                    if (this.mCursor.getCount() != 0) {
                        z = false;
                    }
                }
            }
        }
        return z;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean moveCursorToChildPosition(int i, int i2) {
        if (!this.mDataValid || this.mCursor.isClosed()) {
            return false;
        }
        int groupPositionToBin = groupPositionToBin(i);
        for (int i3 = 0; i3 < groupPositionToBin; i3++) {
            i2 += this.mItemMap[i3];
        }
        return this.mCursor.moveToPosition(i2);
    }

    @Override // android.widget.BaseExpandableListAdapter, android.widget.ExpandableListAdapter
    public void onGroupCollapsed(int i) {
    }

    @Override // android.widget.BaseExpandableListAdapter, android.widget.ExpandableListAdapter
    public void onGroupExpanded(int i) {
    }
}
