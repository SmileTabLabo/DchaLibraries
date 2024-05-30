package com.android.browser.view;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.browser.BreadCrumbView;
import com.android.browser.BrowserBookmarksAdapter;
import com.android.browser.R;
import com.android.browser.provider.BrowserContract;
import com.android.internal.view.menu.MenuBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONException;
import org.json.JSONObject;
/* loaded from: classes.dex */
public class BookmarkExpandableView extends ExpandableListView implements BreadCrumbView.Controller {
    private BookmarkAccountAdapter mAdapter;
    private BreadCrumbView.Controller mBreadcrumbController;
    private View.OnClickListener mChildClickListener;
    private int mColumnWidth;
    private Context mContext;
    private ContextMenu.ContextMenuInfo mContextMenuInfo;
    private View.OnClickListener mGroupOnClickListener;
    private boolean mLongClickable;
    private int mMaxColumnCount;
    private ExpandableListView.OnChildClickListener mOnChildClickListener;
    private View.OnCreateContextMenuListener mOnCreateContextMenuListener;

    public BookmarkExpandableView(Context context) {
        super(context);
        this.mContextMenuInfo = null;
        this.mChildClickListener = new View.OnClickListener() { // from class: com.android.browser.view.BookmarkExpandableView.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                if (view.getVisibility() != 0) {
                    return;
                }
                int intValue = ((Integer) view.getTag(R.id.group_position)).intValue();
                int intValue2 = ((Integer) view.getTag(R.id.child_position)).intValue();
                if (BookmarkExpandableView.this.mAdapter.getGroupCount() > intValue && BookmarkExpandableView.this.mAdapter.mChildren.get(intValue).getCount() > intValue2) {
                    long itemId = BookmarkExpandableView.this.mAdapter.mChildren.get(intValue).getItemId(intValue2);
                    if (BookmarkExpandableView.this.mOnChildClickListener != null) {
                        BookmarkExpandableView.this.mOnChildClickListener.onChildClick(BookmarkExpandableView.this, view, intValue, intValue2, itemId);
                    }
                }
            }
        };
        this.mGroupOnClickListener = new View.OnClickListener() { // from class: com.android.browser.view.BookmarkExpandableView.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                int intValue = ((Integer) view.getTag(R.id.group_position)).intValue();
                if (BookmarkExpandableView.this.isGroupExpanded(intValue)) {
                    BookmarkExpandableView.this.collapseGroup(intValue);
                    return;
                }
                BookmarkExpandableView.this.hideAllGroups();
                BookmarkExpandableView.this.expandGroup(intValue, true);
            }
        };
        init(context);
    }

    public BookmarkExpandableView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mContextMenuInfo = null;
        this.mChildClickListener = new View.OnClickListener() { // from class: com.android.browser.view.BookmarkExpandableView.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                if (view.getVisibility() != 0) {
                    return;
                }
                int intValue = ((Integer) view.getTag(R.id.group_position)).intValue();
                int intValue2 = ((Integer) view.getTag(R.id.child_position)).intValue();
                if (BookmarkExpandableView.this.mAdapter.getGroupCount() > intValue && BookmarkExpandableView.this.mAdapter.mChildren.get(intValue).getCount() > intValue2) {
                    long itemId = BookmarkExpandableView.this.mAdapter.mChildren.get(intValue).getItemId(intValue2);
                    if (BookmarkExpandableView.this.mOnChildClickListener != null) {
                        BookmarkExpandableView.this.mOnChildClickListener.onChildClick(BookmarkExpandableView.this, view, intValue, intValue2, itemId);
                    }
                }
            }
        };
        this.mGroupOnClickListener = new View.OnClickListener() { // from class: com.android.browser.view.BookmarkExpandableView.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                int intValue = ((Integer) view.getTag(R.id.group_position)).intValue();
                if (BookmarkExpandableView.this.isGroupExpanded(intValue)) {
                    BookmarkExpandableView.this.collapseGroup(intValue);
                    return;
                }
                BookmarkExpandableView.this.hideAllGroups();
                BookmarkExpandableView.this.expandGroup(intValue, true);
            }
        };
        init(context);
    }

    public BookmarkExpandableView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mContextMenuInfo = null;
        this.mChildClickListener = new View.OnClickListener() { // from class: com.android.browser.view.BookmarkExpandableView.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                if (view.getVisibility() != 0) {
                    return;
                }
                int intValue = ((Integer) view.getTag(R.id.group_position)).intValue();
                int intValue2 = ((Integer) view.getTag(R.id.child_position)).intValue();
                if (BookmarkExpandableView.this.mAdapter.getGroupCount() > intValue && BookmarkExpandableView.this.mAdapter.mChildren.get(intValue).getCount() > intValue2) {
                    long itemId = BookmarkExpandableView.this.mAdapter.mChildren.get(intValue).getItemId(intValue2);
                    if (BookmarkExpandableView.this.mOnChildClickListener != null) {
                        BookmarkExpandableView.this.mOnChildClickListener.onChildClick(BookmarkExpandableView.this, view, intValue, intValue2, itemId);
                    }
                }
            }
        };
        this.mGroupOnClickListener = new View.OnClickListener() { // from class: com.android.browser.view.BookmarkExpandableView.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                int intValue = ((Integer) view.getTag(R.id.group_position)).intValue();
                if (BookmarkExpandableView.this.isGroupExpanded(intValue)) {
                    BookmarkExpandableView.this.collapseGroup(intValue);
                    return;
                }
                BookmarkExpandableView.this.hideAllGroups();
                BookmarkExpandableView.this.expandGroup(intValue, true);
            }
        };
        init(context);
    }

    void init(Context context) {
        this.mContext = context;
        setItemsCanFocus(true);
        setLongClickable(false);
        this.mMaxColumnCount = this.mContext.getResources().getInteger(R.integer.max_bookmark_columns);
        setScrollBarStyle(33554432);
        this.mAdapter = new BookmarkAccountAdapter(this.mContext);
        super.setAdapter(this.mAdapter);
    }

    @Override // android.widget.ListView, android.widget.AbsListView, android.view.View
    protected void onMeasure(int i, int i2) {
        int size = View.MeasureSpec.getSize(i);
        int mode = View.MeasureSpec.getMode(i);
        if (size > 0) {
            this.mAdapter.measureChildren(size);
            setPadding(this.mAdapter.mRowPadding, 0, this.mAdapter.mRowPadding, 0);
            i = View.MeasureSpec.makeMeasureSpec(size, mode);
        }
        super.onMeasure(i, i2);
        if (size != getMeasuredWidth()) {
            this.mAdapter.measureChildren(getMeasuredWidth());
        }
    }

    @Override // android.widget.ExpandableListView
    public void setAdapter(ExpandableListAdapter expandableListAdapter) {
        throw new RuntimeException("Not supported");
    }

    public void setColumnWidthFromLayout(int i) {
        View inflate = LayoutInflater.from(this.mContext).inflate(i, (ViewGroup) this, false);
        inflate.measure(0, 0);
        this.mColumnWidth = inflate.getMeasuredWidth();
    }

    public void clearAccounts() {
        this.mAdapter.clear();
    }

    public void addAccount(String str, BrowserBookmarksAdapter browserBookmarksAdapter, boolean z) {
        int indexOf = this.mAdapter.mGroups.indexOf(str);
        if (indexOf >= 0) {
            BrowserBookmarksAdapter browserBookmarksAdapter2 = this.mAdapter.mChildren.get(indexOf);
            if (browserBookmarksAdapter2 != browserBookmarksAdapter) {
                browserBookmarksAdapter2.unregisterDataSetObserver(this.mAdapter.mObserver);
                this.mAdapter.mChildren.remove(indexOf);
                this.mAdapter.mChildren.add(indexOf, browserBookmarksAdapter);
                browserBookmarksAdapter.registerDataSetObserver(this.mAdapter.mObserver);
            }
        } else {
            this.mAdapter.mGroups.add(str);
            this.mAdapter.mChildren.add(browserBookmarksAdapter);
            browserBookmarksAdapter.registerDataSetObserver(this.mAdapter.mObserver);
        }
        this.mAdapter.notifyDataSetChanged();
        if (z) {
            expandGroup(this.mAdapter.getGroupCount() - 1);
        }
    }

    public void hideAllGroups() {
        for (int i = 0; i < this.mAdapter.getGroupCount(); i++) {
            collapseGroup(i);
        }
    }

    @Override // android.widget.ExpandableListView
    public void setOnChildClickListener(ExpandableListView.OnChildClickListener onChildClickListener) {
        this.mOnChildClickListener = onChildClickListener;
    }

    @Override // android.view.View
    public void setOnCreateContextMenuListener(View.OnCreateContextMenuListener onCreateContextMenuListener) {
        this.mOnCreateContextMenuListener = onCreateContextMenuListener;
        if (!this.mLongClickable) {
            this.mLongClickable = true;
            if (this.mAdapter != null) {
                this.mAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override // android.view.View, android.view.ViewParent
    public void createContextMenu(ContextMenu contextMenu) {
        ContextMenu.ContextMenuInfo contextMenuInfo = getContextMenuInfo();
        MenuBuilder menuBuilder = (MenuBuilder) contextMenu;
        menuBuilder.setCurrentMenuInfo(contextMenuInfo);
        onCreateContextMenu(contextMenu);
        if (this.mOnCreateContextMenuListener != null) {
            this.mOnCreateContextMenuListener.onCreateContextMenu(contextMenu, this, contextMenuInfo);
        }
        menuBuilder.setCurrentMenuInfo((ContextMenu.ContextMenuInfo) null);
        if (this.mParent != null) {
            this.mParent.createContextMenu(contextMenu);
        }
    }

    @Override // android.widget.AbsListView, android.view.ViewGroup, android.view.ViewParent
    public boolean showContextMenuForChild(View view) {
        Integer num = (Integer) view.getTag(R.id.group_position);
        Integer num2 = (Integer) view.getTag(R.id.child_position);
        if (num == null || num2 == null) {
            return false;
        }
        this.mContextMenuInfo = new BookmarkContextMenuInfo(num2.intValue(), num.intValue());
        if (getParent() != null) {
            getParent().showContextMenuForChild(this);
            return true;
        }
        return true;
    }

    @Override // com.android.browser.BreadCrumbView.Controller
    public void onTop(BreadCrumbView breadCrumbView, int i, Object obj) {
        if (this.mBreadcrumbController != null) {
            this.mBreadcrumbController.onTop(breadCrumbView, i, obj);
        }
    }

    public void setBreadcrumbController(BreadCrumbView.Controller controller) {
        this.mBreadcrumbController = controller;
    }

    @Override // android.widget.AbsListView, android.view.View
    protected ContextMenu.ContextMenuInfo getContextMenuInfo() {
        return this.mContextMenuInfo;
    }

    public BrowserBookmarksAdapter getChildAdapter(int i) {
        return this.mAdapter.mChildren.get(i);
    }

    public BreadCrumbView getBreadCrumbs(int i) {
        return this.mAdapter.getBreadCrumbView(i);
    }

    public JSONObject saveGroupState() throws JSONException {
        JSONObject jSONObject = new JSONObject();
        int groupCount = this.mAdapter.getGroupCount();
        for (int i = 0; i < groupCount; i++) {
            String str = this.mAdapter.mGroups.get(i);
            if (!isGroupExpanded(i)) {
                if (str == null) {
                    str = "local";
                }
                jSONObject.put(str, false);
            }
        }
        return jSONObject;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public class BookmarkAccountAdapter extends BaseExpandableListAdapter {
        ArrayList<BrowserBookmarksAdapter> mChildren;
        ArrayList<String> mGroups;
        LayoutInflater mInflater;
        HashMap<Integer, BreadCrumbView> mBreadcrumbs = new HashMap<>();
        int mRowCount = 1;
        int mLastViewWidth = -1;
        int mRowPadding = -1;
        DataSetObserver mObserver = new DataSetObserver() { // from class: com.android.browser.view.BookmarkExpandableView.BookmarkAccountAdapter.1
            @Override // android.database.DataSetObserver
            public void onChanged() {
                BookmarkAccountAdapter.this.notifyDataSetChanged();
            }

            @Override // android.database.DataSetObserver
            public void onInvalidated() {
                BookmarkAccountAdapter.this.notifyDataSetInvalidated();
            }
        };

        public BookmarkAccountAdapter(Context context) {
            BookmarkExpandableView.this.mContext = context;
            this.mInflater = LayoutInflater.from(BookmarkExpandableView.this.mContext);
            this.mChildren = new ArrayList<>();
            this.mGroups = new ArrayList<>();
        }

        public void clear() {
            this.mGroups.clear();
            this.mChildren.clear();
            notifyDataSetChanged();
        }

        @Override // android.widget.ExpandableListAdapter
        public Object getChild(int i, int i2) {
            return this.mChildren.get(i).getItem(i2);
        }

        @Override // android.widget.ExpandableListAdapter
        public long getChildId(int i, int i2) {
            return i2;
        }

        @Override // android.widget.ExpandableListAdapter
        public View getChildView(int i, int i2, boolean z, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = this.mInflater.inflate(R.layout.bookmark_grid_row, viewGroup, false);
            }
            BrowserBookmarksAdapter browserBookmarksAdapter = this.mChildren.get(i);
            int i3 = this.mRowCount;
            LinearLayout linearLayout = (LinearLayout) view;
            if (linearLayout.getChildCount() > i3) {
                linearLayout.removeViews(i3, linearLayout.getChildCount() - i3);
            }
            for (int i4 = 0; i4 < i3; i4++) {
                View view2 = null;
                if (linearLayout.getChildCount() > i4) {
                    view2 = linearLayout.getChildAt(i4);
                }
                int i5 = (i2 * i3) + i4;
                if (i5 < browserBookmarksAdapter.getCount()) {
                    View view3 = browserBookmarksAdapter.getView(i5, view2, linearLayout);
                    view3.setTag(R.id.group_position, Integer.valueOf(i));
                    view3.setTag(R.id.child_position, Integer.valueOf(i5));
                    view3.setOnClickListener(BookmarkExpandableView.this.mChildClickListener);
                    view3.setLongClickable(BookmarkExpandableView.this.mLongClickable);
                    if (linearLayout.getChildCount() > 1) {
                        view3.setPadding(linearLayout.getChildAt(0).getPaddingLeft(), linearLayout.getChildAt(0).getPaddingTop(), linearLayout.getChildAt(0).getPaddingRight(), linearLayout.getChildAt(0).getPaddingBottom());
                    }
                    if (view2 == null) {
                        linearLayout.addView(view3);
                    } else if (view2 != view3) {
                        linearLayout.removeViewAt(i4);
                        linearLayout.addView(view3, i4);
                    } else {
                        view2.setVisibility(0);
                    }
                } else if (view2 != null) {
                    view2.setVisibility(8);
                }
            }
            return linearLayout;
        }

        @Override // android.widget.ExpandableListAdapter
        public int getChildrenCount(int i) {
            return (int) Math.ceil(this.mChildren.get(i).getCount() / this.mRowCount);
        }

        @Override // android.widget.ExpandableListAdapter
        public Object getGroup(int i) {
            return this.mChildren.get(i);
        }

        @Override // android.widget.ExpandableListAdapter
        public int getGroupCount() {
            return this.mGroups.size();
        }

        public void measureChildren(int i) {
            if (this.mLastViewWidth == i) {
                return;
            }
            int i2 = i / BookmarkExpandableView.this.mColumnWidth;
            if (BookmarkExpandableView.this.mMaxColumnCount > 0) {
                i2 = Math.min(i2, BookmarkExpandableView.this.mMaxColumnCount);
            }
            int i3 = (i - (BookmarkExpandableView.this.mColumnWidth * i2)) / 2;
            boolean z = (i2 == this.mRowCount && i3 == this.mRowPadding) ? false : true;
            this.mRowCount = i2;
            this.mRowPadding = i3;
            this.mLastViewWidth = i;
            if (z) {
                notifyDataSetChanged();
            }
        }

        @Override // android.widget.ExpandableListAdapter
        public long getGroupId(int i) {
            return i;
        }

        @Override // android.widget.ExpandableListAdapter
        public View getGroupView(int i, boolean z, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = this.mInflater.inflate(R.layout.bookmark_group_view, viewGroup, false);
                view.setOnClickListener(BookmarkExpandableView.this.mGroupOnClickListener);
            }
            view.setTag(R.id.group_position, Integer.valueOf(i));
            FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.crumb_holder);
            frameLayout.removeAllViews();
            BreadCrumbView breadCrumbView = getBreadCrumbView(i);
            if (breadCrumbView.getParent() != null) {
                ((ViewGroup) breadCrumbView.getParent()).removeView(breadCrumbView);
            }
            frameLayout.addView(breadCrumbView);
            TextView textView = (TextView) view.findViewById(R.id.group_name);
            String str = this.mGroups.get(i);
            if (str == null) {
                str = BookmarkExpandableView.this.mContext.getString(R.string.local_bookmarks);
            }
            textView.setText(str);
            return view;
        }

        public BreadCrumbView getBreadCrumbView(int i) {
            BreadCrumbView breadCrumbView = this.mBreadcrumbs.get(Integer.valueOf(i));
            if (breadCrumbView == null) {
                BreadCrumbView breadCrumbView2 = (BreadCrumbView) this.mInflater.inflate(R.layout.bookmarks_header, (ViewGroup) null);
                breadCrumbView2.setController(BookmarkExpandableView.this);
                breadCrumbView2.setUseBackButton(true);
                breadCrumbView2.setMaxVisible(2);
                breadCrumbView2.pushView(BookmarkExpandableView.this.mContext.getString(R.string.bookmarks), false, BrowserContract.Bookmarks.CONTENT_URI_DEFAULT_FOLDER);
                breadCrumbView2.setTag(R.id.group_position, Integer.valueOf(i));
                breadCrumbView2.setVisibility(8);
                this.mBreadcrumbs.put(Integer.valueOf(i), breadCrumbView2);
                return breadCrumbView2;
            }
            return breadCrumbView;
        }

        @Override // android.widget.ExpandableListAdapter
        public boolean hasStableIds() {
            return false;
        }

        @Override // android.widget.ExpandableListAdapter
        public boolean isChildSelectable(int i, int i2) {
            return true;
        }
    }

    /* loaded from: classes.dex */
    public static class BookmarkContextMenuInfo implements ContextMenu.ContextMenuInfo {
        public int childPosition;
        public int groupPosition;

        private BookmarkContextMenuInfo(int i, int i2) {
            this.childPosition = i;
            this.groupPosition = i2;
        }
    }
}
